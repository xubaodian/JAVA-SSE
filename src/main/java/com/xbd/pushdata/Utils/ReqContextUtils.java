package com.xbd.pushdata.Utils;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ReqContextUtils {
    private static int DEFAULT_TIME_OUT = 60*60*1000;
    //存储订阅列表的请求引用
    private static HashMap<String, ArrayList<AsyncContext>> subscribeArray = new LinkedHashMap<>();

    //添加订阅消息
    public static void addSubscrib(String topic, HttpServletRequest request, HttpServletResponse response) {
        if (null == topic || "".equals(topic)) {
            return;
        }
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        //request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        AsyncContext actx = request.startAsync(request, response);
        actx.setTimeout(DEFAULT_TIME_OUT);
        actx.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                System.out.println("推送结束");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                System.out.println("推送超时");
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                System.out.println("推送错误");
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                System.out.println("推送开始");
            }
        });
        ArrayList<AsyncContext> actxList = subscribeArray.get(topic);
        if (null == actxList) {
            actxList = new ArrayList<AsyncContext>();
            subscribeArray.put(topic, actxList);
        }
        actxList.add(actx);
    }

    //获取订阅列表
    public static ArrayList<AsyncContext> getSubscribList(String topic) {
        return subscribeArray.get(topic);
    }

    //推送消息
    public static void publishMessage(String topic, String content) {
        ArrayList<AsyncContext> actxList = subscribeArray.get(topic);
        if (null != actxList) {
            for(AsyncContext actx :actxList) {
                try {
                    PrintWriter out = actx.getResponse().getWriter();
                    out.print(content);
                    actx.getResponse().flushBuffer();
                    //out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
