 package com.fabianofranz.wherestagram;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Queue;
 import java.util.Random;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.servlet.AsyncContext;
 import javax.servlet.AsyncEvent;
 import javax.servlet.AsyncListener;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Simple example to test the new Servlet 3 async support in a web app without
  * web.xml descriptor.
  *
  * @author fabianofranz
  */
 @WebServlet(name = "Servlet3AsyncTest", value = "/async", asyncSupported = true)
 public class WherestagramServlet extends HttpServlet {
 
     private final Executor executor = Executors.newFixedThreadPool(10);
     private List<AsyncContext> ctxs = new ArrayList<AsyncContext>();
 
     protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 
         res.setContentType("text/plain");
         res.setCharacterEncoding("utf-8");
         res.setHeader("Access-Control-Allow-Origin", "*");
 
         PrintWriter writer = res.getWriter();
         writer.print("2;Hi;");
         writer.flush();
 
         final AsyncContext ctx = req.startAsync();
 
         ctx.addListener(new AsyncListener() {
 
             @Override
             public void onStartAsync(AsyncEvent event) throws IOException {
             }
 
             @Override
             public void onTimeout(AsyncEvent event) throws IOException {
                 ctxs.remove(ctx);
             }
 
             @Override
             public void onError(AsyncEvent event) throws IOException {
                 ctxs.remove(ctx);
             }
 
             @Override
             public void onComplete(AsyncEvent event) throws IOException {
                 ctxs.remove(ctx);
             }
         });
 
         ctxs.add(ctx);
 
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("utf-8");
        res.setHeader("Access-Control-Allow-Origin", "*");
     }
 
     @Override
     public void init() throws ServletException {
 
         super.init();
 
         // print messages to all users
 
         new Thread(new Runnable() {
 
             @Override
             public void run() {
 
                 while (true) {
 
                     if (!Events.instance().isEmpty()) {
 
                         final String message = Events.instance().poll();
 
                         executor.execute(new Runnable() {
 
                             public void run() {
 
                                 for (AsyncContext ctx : ctxs) {
 
                                     try {
 
                                         HttpServletResponse res = (HttpServletResponse) ctx.getResponse();
                                         PrintWriter writer = res.getWriter();
 
                                         res.setCharacterEncoding("utf-8");
                                         res.setStatus(HttpServletResponse.SC_OK);
                                         res.setContentType("application/json");
 
                                         writer.print(message.length());
                                         writer.print(';');
                                         writer.print(message);
                                         writer.print(';');
                                         writer.flush();
 
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }
 
                                 }
 
                             }
                         });
                     } else {
                         try {
                             Thread.sleep(500);
                         } catch (Exception e) {
                         }
                     }
                 }
             }
         }).start();
     }
 }
