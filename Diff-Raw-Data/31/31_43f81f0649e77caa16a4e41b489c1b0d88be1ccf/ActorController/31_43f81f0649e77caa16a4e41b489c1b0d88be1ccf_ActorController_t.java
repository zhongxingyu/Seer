 package com.wuhanyu.kilim.controller;
 
 
 import kilim.Pausable;
 import kilim.Task;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.AsyncContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @Controller
 @RequestMapping("/kilim")
 public class ActorController{
 	static int count = 0;
 	@RequestMapping(method = RequestMethod.GET)
     public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws javax.servlet.ServletException, java.io.IOException {
         final AsyncContext ac = request.startAsync(request, response);
 
         new Task(){
             public  void execute() throws Pausable, Exception {
                 try {
                 	String timestr = request.getParameter("sleeptime");
                	int time = (timestr == null)? 1000 : Integer.parseInt(timestr);
                     Task.sleep(time);
                     System.out.println("kilim" + (++count));
                 } catch (Exception e) {
                 	log.error("io error", e);
                 }finally{
                     ac.complete();
                 }
             }
         }.start();
     }
 
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
         doGet(req,resp);
     }
 
     public static Logger log = LoggerFactory.getLogger(ActorController.class);
 
 }
 
