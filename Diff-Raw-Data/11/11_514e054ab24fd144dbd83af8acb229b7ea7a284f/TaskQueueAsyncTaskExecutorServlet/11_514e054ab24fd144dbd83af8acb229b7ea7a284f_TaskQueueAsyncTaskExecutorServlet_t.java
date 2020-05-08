 package com.clouway.asynctaskscheduler.gae;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Map;
 
 /**
  * Represents the HttpServlet that is executed form the added Task Queues and executes the Job that they specify.
  *
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public class TaskQueueAsyncTaskExecutorServlet extends HttpServlet {
   public static final String URL = "/worker/taskQueue";
   private final FooDispatcher dispatcher;
 
   @Inject
   public TaskQueueAsyncTaskExecutorServlet(FooDispatcher dispatcher) {
     this.dispatcher = dispatcher;
   }
 
   @Override
   protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
     doPost(httpServletRequest, httpServletResponse);
   }
 
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     try {
 
       String asyncTaskClass = getParameter(request, TaskQueueAsyncTaskScheduler.TASK_QUEUE);
 
       //event details
       String eventClassAsString = getParameter(request, TaskQueueAsyncTaskScheduler.EVENT);
      String eventAsJson = getParameter(request, TaskQueueAsyncTaskScheduler.EVENT_AS_JSON);
 
       //if event is passed then it should be dispatched to it's handler
       if (!Strings.isNullOrEmpty(eventClassAsString) && !Strings.isNullOrEmpty(eventAsJson)) {
 
         dispatcher.dispatchAsyncEvent(eventClassAsString, eventAsJson);
 
         // if asyncTask is provided it should be executed
       } else if (!Strings.isNullOrEmpty(asyncTaskClass)) {
 
         Map<String, String[]> params = Maps.newHashMap(request.getParameterMap());
 
        //todo do not pass map here;
         dispatcher.dispatchAsyncTask(params, asyncTaskClass);
 
       }
 
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
     }
 
   }
 
   private String getParameter(HttpServletRequest request, String pramName) throws UnsupportedEncodingException {
     String param = request.getParameter(pramName);
 
     if (param != null) {
 
       String decodedParam = URLDecoder.decode(param, "UTF8");
       return decodedParam;
 
     }
     return null;
   }
 
 }
 
