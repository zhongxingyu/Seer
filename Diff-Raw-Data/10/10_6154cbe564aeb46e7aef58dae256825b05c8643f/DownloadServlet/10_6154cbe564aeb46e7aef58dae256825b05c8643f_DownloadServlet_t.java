 package uk.ac.ebi.ae15.servlets;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import uk.ac.ebi.ae15.app.ApplicationServlet;
 import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
 import uk.ac.ebi.ae15.components.Experiments;
 import uk.ac.ebi.ae15.components.Users;
 import uk.ac.ebi.ae15.utils.CookieMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DownloadServlet extends ApplicationServlet
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     // buffer size
     private final int TRANSFER_BUFFER_SIZE = 8 * 1024 * 1024;
 
     // Respond to HTTP GET requests from browsers.
     public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
     {
         logRequest(request);
 
         Pattern p = Pattern.compile("/([^/]+)$");
         Matcher m = p.matcher(request.getRequestURL());
         String filename;
         try {
             if (m.find()) {
                 filename = m.group(1);
                 sendFile(filename, request, response);
             } else {
                 log.error("Unable to get a filename from [" + request.getRequestURL() + "]");
                 throw (new Exception());
             }
         } catch ( Throwable x ) {
             String name = x.getClass().getName();
             if (name.equals("org.apache.catalina.connector.ClientAbortException")) {
                 // generate log entry for client abortion
                 log.warn("Download aborted");
             } else {
                 log.debug("Caught an exception:", x);
                 if (!response.isCommitted())
                     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             }
         }
     }
 
     private void sendFile( String filename, HttpServletRequest request, HttpServletResponse response ) throws IOException
     {
         log.info("Requested download of [" + filename + "]");
         DownloadableFilesRegistry filesRegistry = (DownloadableFilesRegistry) getComponent("DownloadableFilesRegistry");
         Experiments experiments = (Experiments) getComponent("Experiments");
 
         String userId = "1";
         CookieMap cookies = new CookieMap(request.getCookies());
         if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
             Users users = (Users) getComponent("Users");
             String user = cookies.get("AeLoggedUser").getValue();
             String passwordHash = cookies.get("AeLoginToken").getValue();
            if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                 userId = String.valueOf(users.getUserRecord(user).getId());
             }
         }
 
 
         if (!filesRegistry.doesExist(filename)) {
             log.error("File [" + filename + "] is not in files registry");
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
         } else {
             String fileLocation = filesRegistry.getLocation(filename);
             String contentType = getServletContext().getMimeType(fileLocation);
             if (null != contentType) {
                 log.debug("Setting content type to [" + contentType + "]");
                 response.setContentType(contentType);
             } else {
                 log.warn("Download servlet was unable to determine content type for [" + fileLocation + "]");
             }
 
             log.debug("Checking file [" + fileLocation + "]");
             File file = new File(fileLocation);
             if (!file.exists()) {
                 log.error("File [" + fileLocation + "] does not exist");
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
             } else if (!experiments.isFileAccessible(fileLocation, userId)) {
                 log.error("Attempting to download file for the experiment that is not present in the index");
                 response.sendError(HttpServletResponse.SC_FORBIDDEN);
             } else {
                 FileInputStream fileInputStream = null;
                 ServletOutputStream servletOutStream = null;
                 try {
                     fileInputStream = new FileInputStream(file);
                    long size = file.length();
                    response.addHeader("Content-Length", Long.toString(size));
 
                     servletOutStream = response.getOutputStream();
 
                     int bytesRead;
                     byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
 
                     while ( true ) {
                         bytesRead = fileInputStream.read(buffer, 0, TRANSFER_BUFFER_SIZE);
                         if (bytesRead == -1) break;
                         servletOutStream.write(buffer, 0, bytesRead);
                         servletOutStream.flush();
                     }
                    log.info("Download of [" + filename + "] completed, sent [" + size + "] bytes");
                 } finally {
                     if (null != fileInputStream)
                         fileInputStream.close();
                     if (null != servletOutStream)
                         servletOutStream.close();
                 }
             }
         }
     }
 }
