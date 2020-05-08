 /**
  * @author <a href="mailto:tkucz@icis.pcz.pl">Tomasz Kuczynski</a>
  * @version 0.1 2004/03/10
  */
 package org.gridlab.gridsphere.servlets;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.util.Enumeration;
 import java.util.Date;
 import java.util.Calendar;
 
 import java.text.DateFormat;
 
 import org.apache.oro.text.perl.Perl5Util;
 
 public class SecureDirectory extends HttpServlet {
 
     private Perl5Util util = new Perl5Util();
     private final static int BUFFER_SIZE = 8 * 1024; //8 kB
     private final static boolean DEBUG = true; //leaving DEBUG=true helps to trace if somebody tries to break into ;-)
     private final static int EXPIRES = 15; //15 seconds, works only if strong protection is disabled
     private static String secureDirPath;
     private static boolean strongProtection = true;
     private static boolean inited = false;
     private DateFormat dateFormat = null;
 
     public void init() throws ServletException {
         if (!inited) {
             secureDirPath = getServletContext().getRealPath("/WEB-INF/secure");
             strongProtection = Boolean.valueOf(getInitParameter("strongProtection")).booleanValue();
             File secureDir = new File(secureDirPath);
             if (secureDirPath != null && secureDir.isDirectory()) {
                 inited = true;
                 if (DEBUG)
                     log("Initialization OK (Strong protection " + (strongProtection ? "enabled" : "DISABLED (better enable it check web.xml) !!!") + "). Setting secureDirPath to " + secureDirPath);
             } else {
                 if (DEBUG)
                    log("Initialization problem, please check if " + getServletContext().getRealPath("/") + getInitParameter("secureDirPath") + " exists and if it is directory !!!");
             }
         }
         dateFormat = DateFormat.getDateInstance();
         dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String userID = (String) request.getSession().getAttribute("org.gridlab.gridsphere.portlet.User");
         if (userID == null || userID.equals("")) {
             if (DEBUG)
                 log("Request blocked (userID=" + userID + ") !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
             response.setStatus(403);
         } else if (!inited) {
             response.setStatus(503);
         } else {
             String userDirPath = secureDirPath + "/" + userID;
             if (!(new File(userDirPath).isDirectory())) {
                 if (DEBUG)
                     log("Request blocked (userDirPath=" + userDirPath + " is not directory) !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
                 response.setStatus(403);
             } else {
                 String resourcePath = util.substitute("s!" + request.getContextPath() + request.getServletPath() + "!!", request.getRequestURI());
                 File resource = new File(userDirPath + resourcePath);
                 if (!resource.canRead() || resource.isDirectory()) {
                     if (DEBUG)
                         log("Request blocked (Not found, resource=" + userDirPath + resourcePath + ") !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
                     response.setStatus(404);
                 } else {
                     Enumeration params = request.getParameterNames();
                     String saveAs = null;
                     String contentType = null;
                     while (params.hasMoreElements()) {
                         String paramName = (String) params.nextElement();
                         if (util.match("/(.+_)?saveAs/", paramName)) {
                             saveAs = request.getParameter(paramName);
                             if (contentType != null)
                                 break;
                         } else if (util.match("/(.+_)?contentType/", paramName)) {
                             contentType = request.getParameter(paramName);
                             if (saveAs != null)
                                 break;
                         }
                     }
 
                     if (contentType == null)
                         contentType = getServletContext().getMimeType(resourcePath);
                     setHeaders(request, response, saveAs, contentType, resource.length());
 
                     ServletOutputStream output = response.getOutputStream();
                     FileInputStream input = new FileInputStream(resource);
                     rewrite(input, output);
                 }
             }
         }
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if (DEBUG)
             log("Request blocked (POST request) !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
         response.setStatus(403);  //to allow for HTTP POST requests comment this line (and 2 lines before) and uncomment next one
         //doGet(request,response);
     }
 
     private void setHeaders(HttpServletRequest request, HttpServletResponse response, String saveAs, String contentType, long size) {
         if (saveAs != null) {
             response.setContentType("application/octet-stream");
             response.setHeader("Content-Disposition", "attachment; filename=" + saveAs);
         } else {
             if (contentType == null)
                 contentType = "application/octet-stream";
             response.setContentType(contentType);
         }
         response.setHeader("Content-Length", new Long(size).toString());
         if (strongProtection) {
             response.setHeader("Cache-Control", "no-store");
         } else {
             response.setHeader("Cache-Control", "private, must-revalidate");
             response.setHeader("Expires", dateFormat.format(new Date(new Date().getTime() + EXPIRES * 1000)));
         }
         response.setHeader("Pragma", "no-cache");
     }
 
     public long getLastModified(HttpServletRequest request) {
         if (Calendar.getInstance().getTimeInMillis() > 0) return Calendar.getInstance().getTimeInMillis(); //comment this line if you want allow browser to check when resource was last modified
         String userID = (String) request.getSession().getAttribute("org.gridlab.gridsphere.portlet.User");
         if (userID == null || userID.equals("")) {
             if (DEBUG)
                 log("LastModifiedRequest blocked (userID=" + userID + ") !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
             return Calendar.getInstance().getTimeInMillis();
         } else if (!inited) {
             return Calendar.getInstance().getTimeInMillis();
         } else {
             String userDirPath = secureDirPath + "/" + userID;
             if (!(new File(userDirPath).isDirectory())) {
                 if (DEBUG)
                     log("LastModifiedRequest blocked (userDirPath=" + userDirPath + " is not directory) !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
                 return Calendar.getInstance().getTimeInMillis();
             } else {
                 String resourcePath = util.substitute("s!" + request.getContextPath() + request.getServletPath() + "!!", request.getRequestURI());
                 File resource = new File(userDirPath + resourcePath);
                 if (!resource.exists()) {
                     log("LastModifiedRequest blocked (Not found, resource=" + userDirPath + resourcePath + ") !!! Request: " + request.getRequestURI() + "\nIP: " + request.getRemoteAddr() + "\n");
                     return new Date().getTime();
                 } else {
                     return resource.lastModified();
                 }
             }
         }
     }
 
     private void rewrite(InputStream input, OutputStream output) throws IOException {
         int numRead;
         byte[] buf = new byte[BUFFER_SIZE];
         while (!((numRead = input.read(buf)) < 0)) {
             output.write(buf, 0, numRead);
         }
     }
 
 }
