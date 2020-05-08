 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  */
 
 package org.trianacode.TrianaCloud.Broker;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.trianacode.TrianaCloud.Utils.Task;
 import org.trianacode.TrianaCloud.Utils.TaskDAO;
 import org.trianacode.TrianaCloud.Utils.TrianaCloudServlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Kieran David Evans
  * @version 1.0.0 Feb 26, 2012
  */
 public class Results extends TrianaCloudServlet {
 
     private Logger logger = Logger.getLogger(this.getClass());
 
     private TaskDAO td;
 
     private class TaskReturn {
         public String name;
         public String origin;
         public String totalTime;
         public String dataType;
         public String data;
         public String key;
         public String returnCode;
     }
 
     public void init() throws ServletException {
         try {
             /*resultMap = (ConcurrentHashMap<String, Task>) getServletContext().getAttribute("resultMap");
             if (resultMap == null) {
                 ServletException se = new ServletException("Couldn't get resultMap");
                 logger.error("Couldn't get the ResultMap", se);
                 throw se;
             } */
             td = new TaskDAO();
         } catch (Exception e) {
             ServletException se = new ServletException("Couldn't get resultMap");
             logger.error("Something Happened!", se);
             throw se;
         }
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         String name = "action";
         String value = request.getParameter(name);
         if (value == null) {
             // The request parameter 'param' was not present in the query string
             // e.g. http://hostname.com?a=b
             logger.error("Parameter \"action\" not defined in the request.");
             this.write404Error(response, "Parameter \"action\" not defined in the request.");
         } else if ("".equals(value)) {
             // The request parameter 'param' was present in the query string but has no value
             // e.g. http://hostname.com?param=&a=b
             logger.error("Parameter \"action\" is null");
             this.write404Error(response, "Parameter \"action\" is null.");
         }
 
         String pathInfo = isolatePath(request);
         String content = "";
         if (!pathInfo.equalsIgnoreCase("")) {
             write404Error(response, "Unknown endpoint");
             return;
         }
 
         if (value.equalsIgnoreCase("json")) {
             getJson(request, response);
         } else if (value.equalsIgnoreCase("file")) {
             getFile(request, response);
         } else if (value.equalsIgnoreCase("results")) {
             getResults(request, response);
         } else if (value.equalsIgnoreCase("byid")) {
             getJsonByID(request, response);
         }
     }
 
     public void getJsonByID(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         String pname = "uuid";
         String kvalue = request.getParameter(pname);
 
         if (kvalue == null) {
             logger.info("UUID is null");
             writeError(response, 500, "No ID specified.");
             return;
         }
         if (kvalue.equalsIgnoreCase("")) {
             logger.info("UUID is blank");
         }
         try {
             response.setStatus(200);
             response.setContentType("text/html");
 
             response.getWriter().println(makeJSONByID(kvalue));
         } catch (Exception e) {
             e.printStackTrace();
            log.error(e);
             StringBuffer stack = new StringBuffer("Error: " + e.getMessage() + "<br/>");
             StackTraceElement[] trace = e.getStackTrace();
             for (StackTraceElement element : trace) {
                 stack.append(element.toString()).append("<br/>");
             }
             writeError(response, 500, stack.toString());
         } catch (Throwable t) {
             writeThrowable(response, t);
         }
     }
 
     public String makeJSONByID(String UUID) throws IOException {
         ObjectMapper mapper = new ObjectMapper(new JsonFactory());
         StringBuffer b = new StringBuffer();
 
         Task t = td.getByUUID(UUID);//resultMap.get("UUID");
 
         if (t == null) {
             return "0";
         }
 
         //Iterator it = resultMap.entrySet().iterator();
 
         Set<TaskReturn> trs = new LinkedHashSet<TaskReturn>();
 
 
         TaskReturn r = new TaskReturn();
         r.key = UUID;
         r.name = t.getName();
         r.origin = t.getOrigin();
         r.dataType = t.getReturnDataType();
         r.totalTime = (t.getTotalTime().getTime() + "ms");
         r.returnCode = t.getReturnCode();
         if (r.dataType.equalsIgnoreCase("string")) {
             r.data = new String(t.getReturnData(), "UTF-8");
         } else {
             r.data = new String("" + t.getReturnData().length);
         }
 
         trs.add(r);
 
         b.append(mapper.writeValueAsString(trs));
 
         logger.info("JSON: " + b.toString());
         return b.toString();
     }
 
     public void getJson(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             response.setStatus(200);
             response.setContentType("text/html");
             response.getWriter().println(makeJSON());
         } catch (Exception e) {
             e.printStackTrace();
            log.error(e);
             StringBuffer stack = new StringBuffer("Error: " + e.getMessage() + "<br/>");
             StackTraceElement[] trace = e.getStackTrace();
             for (StackTraceElement element : trace) {
                 stack.append(element.toString()).append("<br/>");
             }
             writeError(response, 500, stack.toString());
         } catch (Throwable t) {
             writeThrowable(response, t);
         }
     }
 
     public String makeJSON() throws IOException {
         ObjectMapper mapper = new ObjectMapper(new JsonFactory());
         StringBuffer b = new StringBuffer();
 
         List<Task> tasks = td.list();
         Iterator it = tasks.iterator();
 
         Set<TaskReturn> trs = new LinkedHashSet<TaskReturn>();
 
         while (it.hasNext()) {
             Task t = (Task) it.next();
 
             if (t != null) {
                 TaskReturn r = new TaskReturn();
                 r.key = t.getUUID();
                 r.name = t.getName();
                 r.origin = t.getOrigin();
                 r.dataType = t.getReturnDataType();
                 r.totalTime = (t.getTotalTime().getTime() + "ms");
                 r.returnCode = t.getReturnCode();
                 if (r.dataType != null) {
                     if (r.dataType.equalsIgnoreCase("string")) {
                         r.data = new String(t.getReturnData(), "UTF-8");
                     }
                 }
                 trs.add(r);
             }
         }
 
         b.append(mapper.writeValueAsString(trs));
 
         logger.info("JSON: " + b.toString());
         return b.toString();
     }
 
     public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         ServletOutputStream op = response.getOutputStream();
         String pname = "key";
         String kvalue = request.getParameter(pname);
 
         if (kvalue == null) {
             logger.info("Key is null");
         }
         if (kvalue.equalsIgnoreCase("")) {
             logger.info("Key is blank");
         }
 
         Task t = td.getByUUID(kvalue);//resultMap.get(kvalue);
         if (t == null) {
             logger.info("Task is null");
         }
 
         byte[] data = t.getReturnData();
 
         response.setContentType("application/octet-stream");
         response.setContentLength((int) data.length);
         response.setHeader("Content-Disposition", "attachment; filename=" + t.getFileName());
 
         op.write(data);
 
         op.flush();
         op.close();
     }
 
     public void getResults(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             response.setStatus(200);
             response.setContentType("text/html");
 
             StringBuffer b = new StringBuffer();
             List<Task> tasks = td.list();
 
             Iterator it = tasks.iterator();
 
             while (it.hasNext()) {
                 Task t = (Task) it.next();
                 String key = t.getUUID();
 
                 if (t != null) {
                     b.append("<div class=\"Title\">");
                     b.append(t.getOrigin());
                     b.append(" ");
                     b.append(t.getTotalTime().getTime() + " ms");
                     b.append("</div>");
                     if (t.getReturnDataType().equalsIgnoreCase("string")) {
                         b.append("<div class=\"toggle\"><pre>");
                         b.append(new String(t.getReturnData(), "UTF-8").replace("\n", "<br>"));
                         b.append("</pre></div><p/>");
                     } else {
                         b.append("<a href=\"/Broker/results?action=file&key=");
                         b.append(key);
                         b.append("\" display=\"none\">");
                         b.append(t.getName());
                         b.append("</a><p/>");
                     }
                 }
             }
 
             response.getWriter().println(b.toString());
 
         } catch (Exception e) {
             e.printStackTrace();
            log.error(e);
             StringBuffer stack = new StringBuffer("Error: " + e.getMessage() + "<br/>");
             StackTraceElement[] trace = e.getStackTrace();
             for (StackTraceElement element : trace) {
                 stack.append(element.toString()).append("<br/>");
             }
             writeError(response, 500, stack.toString());
         } catch (Throwable t) {
             writeThrowable(response, t);
         }
     }
 
 }
