 package org.trianacode.TrianaCloud.Broker;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.trianacode.TrianaCloud.Utils.Task;
 import org.trianacode.TrianaCloud.Utils.TrianaCloudServlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class Results extends TrianaCloudServlet {
 
     private Logger logger = Logger.getLogger(this.getClass());
 
     public static ConcurrentHashMap<String, Task> resultMap;
 
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
             resultMap = (ConcurrentHashMap<String, Task>) getServletContext().getAttribute("resultMap");
             if (resultMap == null) {
                 ServletException se = new ServletException("Couldn't get resultMap");
                 logger.error("Couldn't get the ResultMap", se);
                 throw se;
             }
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
             System.out.println("UUID is null");
         }
         if (kvalue.equalsIgnoreCase("")) {
             System.out.println("UUID is blank");
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
 
         Task t = resultMap.get("UUID");
         Iterator it = resultMap.entrySet().iterator();
 
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
         }else{
            r.data = t.getReturnData().length;
         }
 
         b.append(mapper.writeValueAsString(trs));
 
         System.out.println("JSON: " + b.toString());
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
         Iterator it = resultMap.entrySet().iterator();
 
         Set<TaskReturn> trs = new LinkedHashSet<TaskReturn>();
 
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry) it.next();
             String key = (String) pairs.getKey();
             Task t = (Task) pairs.getValue();
 
             if (t != null) {
                 TaskReturn r = new TaskReturn();
                 r.key = key;
                 r.name = t.getName();
                 r.origin = t.getOrigin();
                 r.dataType = t.getReturnDataType();
                 r.totalTime = (t.getTotalTime().getTime() + "ms");
                 r.returnCode = t.getReturnCode();
                 if (r.dataType.equalsIgnoreCase("string")) {
                     r.data = new String(t.getReturnData(), "UTF-8");
                 }
                 trs.add(r);
             }
         }
 
         b.append(mapper.writeValueAsString(trs));
 
         System.out.println("JSON: " + b.toString());
         return b.toString();
     }
 
     public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         ServletOutputStream op = response.getOutputStream();
         String pname = "key";
         String kvalue = request.getParameter(pname);
 
         if (kvalue == null) {
             System.out.println("Key is null");
         }
         if (kvalue.equalsIgnoreCase("")) {
             System.out.println("Key is blank");
         }
 
         Task t = resultMap.get(kvalue);
         if (t == null) {
             System.out.println("Task is null");
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
             Iterator it = resultMap.entrySet().iterator();
 
             while (it.hasNext()) {
                 Map.Entry pairs = (Map.Entry) it.next();
                 String key = (String) pairs.getKey();
                 Task t = (Task) pairs.getValue();
 
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
