 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import org.apache.commons.io.IOUtils;
 import org.apache.jackrabbit.extractor.HTMLTextExtractor;
 import org.apache.log4j.Logger;
 import org.postgresql.util.PSQLException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.*;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 /**
  * A class to do various things with a URL or its contents
  *
  * @author: kkj
  * Date: Apr 23, 2008
  * Time: 11:11:41 AM
  */
 public class URLActions { // Should this class extend HttpUrlConnection?
     private URL url;
     private HttpURLConnection con = null;
     private String ourcode = null; // This is the code we base our status on
     private String encoding = "ISO-8859-1";
     private static Logger logger = Logger.getLogger(URLActions.class);
     private DefaultSparulDispatcher sparulDispatcher;
 
 
     public URLActions(String u) {
         try {
             url = new URL(u);
         } catch (MalformedURLException e) {
             ourcode = "MALFORMED_URL";
         }
     }
 
     public URLActions(URL u) throws MalformedURLException {
         url = u;
     }
 
     public URL getUrl() {
         return url;
     }
 
     public String getEncoding() {
         return encoding;
     }
 
     public void setEncoding(String encoding) {
         this.encoding = encoding;
     }
 
     public HttpURLConnection getCon() {
         return con;
     }
 
     /**
      * Method to establish a connection
      * <p/>
      * Apparently, the underlying library needs one connection object for each thing to
      * retrieve from the connection. This seems very awkward, thus, methods in this class
      * resets the connection for each thing they do. This method will always refresh the object's
      * connection object.
      */
     public void connect() {
         if (con == null) {
             try {
                 con = (HttpURLConnection) url.openConnection();
             }
             catch (IOException e) {
                 ourcode = "IOEXCEPTION";
             }
         }
     }
 
     public InputStream readContentStream() {
         InputStream result = null;
 
         try {
             connect();
             result = con.getInputStream();
         }
         catch (MalformedURLException e) {
             ourcode = "MALFORMED_URL";
         }
         catch (IOException e) {
             ourcode = "IOEXCEPTION";
         }
         con = null;
         return result;
     }
 
     public String readContent() { // Sux0rz. Dude, where's my multiple return types?
         String result = null;
         try {
             InputStream content = readContentStream();
             result = IOUtils.toString(content);
         }
         catch (IOException e) {
             ourcode = "IOEXCEPTION";
         }
         con = null;
         return result;
 
     }
 
 
     /**
      * Method to get only the HTTP Code, or String representation of exception
      *
      * @return ourcode
      */
     public String getCode() {
         if (ourcode != null) {
             logger.debug("getCode() has already thrown exception ---> ");
             return ourcode;
         }
 
         logger.info("getCode() ---> " + url.toString());
 
         FutureTask<?> theTask = null;
         try {
             // create new task
             theTask = new FutureTask<Object>(new Runnable() {
                 public void run() {
                     try {
                         connect();
                         con.setConnectTimeout(6000);
                         ourcode = String.valueOf(con.getResponseCode());
                     }
                     catch (MalformedURLException e) {
                         ourcode = "MALFORMED_URL";
                     }
                     catch (ClassCastException e) {
                         ourcode = "UNSUPPORTED_PROTOCOL";
                     }
                     catch (UnknownHostException e) {
                         ourcode = "UNKNOWN_HOST";
                     }
                     catch (ConnectException e) {
                         ourcode = "CONNECTION_TIMEOUT";
                     }
                     catch (SocketTimeoutException e) {
                         ourcode = "CONNECTION_TIMEOUT";
                     }
                     catch (IOException e) {
                         ourcode = "IOEXCEPTION";
                     }
                 }
             }, null);
 
             // start task in a new thread
             new Thread(theTask).start();
 
             // wait for the execution to finish, timeout after 10 secs
             theTask.get(10L, TimeUnit.SECONDS);
         }
         catch (TimeoutException e) {
             ourcode = "CONNECTION_TIMEOUT";
         } catch (ExecutionException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         finally {
             try {
                 con.disconnect();
                 con = null;
             } catch (Exception e) {
                 logger.error("Could not close connection");
             }
         }
 
         logger.info("getCode() ---> " + url.toString() + " returned a " + ourcode);
 
         return ourcode;
     }
 
     /**
      * A method to check a URL. Returns the HTTP code.
      * In cases where the connection gives an exception
      * the exception is catched and a String representation
      * of the exception is returned as the http code
      *
      * @return A HashMap<String, String> where each key is an HTTP-header, but in lowercase,
      *         and represented in an appropriate namespace. The returned HTTP code is in the
      *         http:status field. In case of exceptions a String
      *         representation of the exception is used.
      */
     public HashMap<String, String> getHTTPmap() {
         final HashMap<String, String> result = new HashMap<String, String>();
 
         FutureTask<?> theTask = null;
         try {
             // create new task
             theTask = new FutureTask<Object>(new Runnable() {
                 public void run() {
                     try {
                         logger.info("getHTTPmap() ---> " + url.toString());
                         connect();
                         con.setConnectTimeout(6000);
                         for (String key : con.getHeaderFields().keySet()) {
                             if (key != null) {
                                 result.put("httph:" + key.toLowerCase(), con.getHeaderField(key));
                             }
 
                         }
 
                         ourcode = String.valueOf(con.getResponseCode());
                         con = null;
                     }
                     catch (MalformedURLException e) {
                         ourcode = "MALFORMED_URL";
                     }
                     catch (ClassCastException e) {
                         ourcode = "UNSUPPORTED_PROTOCOL";
                     }
                     catch (UnknownHostException e) {
                         ourcode = "UNKNOWN_HOST";
                     }
                     catch (ConnectException e) {
                         ourcode = "CONNECTION_TIMEOUT";
                     }
                     catch (SocketTimeoutException e) {
                         ourcode = "CONNECTION_TIMEOUT";
                     }
                     catch (IOException e) {
                         ourcode = "IOEXCEPTION";
                     }
                     result.put("http:status", ourcode);
                 }
             }, null);
 
             // start task in a new thread
             new Thread(theTask).start();
 
             // wait for the execution to finish, timeout after 10 secs
             theTask.get(10L, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             ourcode = "CONNECTION_TIMEOUT";
         }
         return result;
     }
 
     /**
      * Method that updates a resource based on the HTTP Code.
      * The resource can have one of three statuses: OK, CHECK or INACTIVE.
      * This list shows what HTTP Codes that gives what status.
      * <p/>
      * 2xx - OK
      * <p/>
      * 301 - Fetch new URL from HTTP Header, then CHECK
      * 302 - OK
      * 303 - OK
      * 304 - OK
      * 305 - OK
      * 306 - INACTIVE
      * 307 - OK
      * <p/>
      * 400 - INACTIVE
      * 401 - CHECK
      * 403 - INACTIVE
      * 404 - CHECK
      * 405 - INACTIVE
      * 406 - CHECK
      * 407 - CHECK
      * 408 - CHECK
      * 409 - CHECK
      * 410 - GONE
      * 411 to 417 - CHECK
      * <p/>
      * 5xx - CHECK
      * <p/>
      * MALFORMED_URL - INACTIVE
      * UNSUPPORTED_PROTOCOL - INACTIVE
      * UNKNOWN_HOST - CHECK
      * CONNECTION_TIMEOUT - CHECK
      * <p/>
      * Others - CHECK
      */
     public void getAndUpdateResourceStatus() {
         sparulDispatcher = new DefaultSparulDispatcher();
         String status = "";
 
         try {
 
             if (ourcode == null) {
                 getCode();
             }
 
             if ("302".equals(ourcode) ||
                     "303".equals(ourcode) ||
                     "304".equals(ourcode) ||
                     "305".equals(ourcode) ||
                     "307".equals(ourcode) ||
                     ourcode.startsWith("2")) {
                 status = "<http://sublima.computas.com/status/ok>";
 
                 // Update the external content of the resource
                 //updateResourceExternalContent();
 
             }
             // GONE
             else if ("410".equals(ourcode)) {
                 status = "<http://sublima.computas.com/status/gone>";
             }
             // INACTIVE
             else if ("306".equals(ourcode) ||
                     "400".equals(ourcode) ||
                     "403".equals(ourcode) ||
                     "405".equals(ourcode) ||
                     "MALFORMED_URL".equals(ourcode) ||
                     "UNSUPPORTED_PROTOCOL".equals(ourcode)) {
                 status = "<http://sublima.computas.com/status/inaktiv>";
             }
             // CHECK
             else {
                 status = "<http://sublima.computas.com/status/check>";
             }
 
         }
         catch (Exception e) {
             logger.info("Exception -- updateResourceStatus() ---> " + url.toString() + ":" + ourcode);
             e.printStackTrace();
             insertNewStatusForResource("<http://sublima.computas.com/status/check>");
         }
         // OK
 
         insertNewStatusForResource(status);
     }
 
     private void insertNewStatusForResource(String status) {
         String deleteString;
         String updateString;
 
         try {
 
             if (status.equalsIgnoreCase("<http://sublima.computas.com/status/ok>")) {
                 deleteString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                         "DELETE\n" +
                         "{\n" +
                         "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                         "}\n" +
                         "WHERE {\n" +
                         "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                         "}";
 
                 updateString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                         "INSERT\n" +
                         "{\n" +
                         "<" + url.toString() + "> sub:status " + status + ".\n" +
                         "}";
             } else {
                 deleteString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                         "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                         "DELETE\n" +
                         "{\n" +
                         "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                         "<" + url.toString() + "> wdr:describedBy ?oldstatus .\n" +
                         "}\n" +
                         "WHERE {\n" +
                         "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                         "<" + url.toString() + "> wdr:describedBy ?oldstatus .\n" +
                         "}";
 
                 updateString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                         "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                         "INSERT\n" +
                         "{\n" +
                         "<" + url.toString() + "> sub:status " + status + ".\n" +
                         "<" + url.toString() + "> wdr:describedBy " + status + ".\n" +
                         "}";
             }
 
             logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- SPARUL DELETE  --> " + deleteString);
 
             boolean success;
             success = sparulDispatcher.query(deleteString);
             logger.info("updateResourceStatus() ---> " + url.toString() + " with code " + ourcode + " -- DELETE OLD STATUS --> " + success);
             logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- SPARUL UPDATE  --> " + updateString);
 
             success = false;
 
             success = sparulDispatcher.query(updateString);
             logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- INSERT NEW STATUS --> " + success);
         } catch (Exception e) {
             logger.info("insertNewStatusForResource() ---> Gave an exception. Check if this URL is valid.");
         }
     }
 
     /**
      * @throws UnsupportedEncodingException
      * @throws PSQLException
      * @deprecated Deprecated because of new index method
      */
     public void updateResourceExternalContent() throws UnsupportedEncodingException, PSQLException {
         sparulDispatcher = new DefaultSparulDispatcher();
         String resourceExternalContent = readContent();
         SearchService searchService = new SearchService();
 
         String deleteString = "DELETE { ?response ?p ?o }" +
                 " WHERE { <" + url.toString() + "> <http://www.w3.org/2007/ont/link#request> ?response . }";
 
         boolean success = false;
         success = sparulDispatcher.query(deleteString);
         logger.info("updateResourceExternalContent() ---> " + url.toString() + " -- DELETE OLD CONTENT --> " + success);
 
         String requesturl = "<http://sublima.computas.com/latest-get/" + url.toString().hashCode() + "> ";
         StringBuilder updateString = new StringBuilder();
         updateString.append("PREFIX link: <http://www.w3.org/2007/ont/link#>\n" +
                 "PREFIX http: <http://www.w3.org/2007/ont/http#>\n" +
                 "PREFIX httph: <http://www.w3.org/2007/ont/httph#>\n" +
                 "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                 "INSERT\n{\n<" + url.toString() + "> link:request " + requesturl + ".\n" +
                 requesturl + "a http:ResponseMessage ; \n");
         HashMap<String, String> headers = getHTTPmap();
         for (String key : headers.keySet()) {
             updateString.append(key + " \"" + searchService.escapeString(headers.get(key)) + "\" ;\n");
         }
         updateString.append("sub:stripped \"\"\"" + searchService.escapeString(strippedContent(null)) + "\"\"\" .\n}");
         logger.trace("updateResourceExternalContent() ---> INSERT: " + updateString.toString());
 
         success = false;
 
         success = sparulDispatcher.query(updateString.toString());
         logger.info("updateResourceExternalContent() ---> " + url.toString() + " -- INSERT NEW CONTENT --> " + success);
     }
 
     public String strippedContent
             (final String
                     content) throws UnsupportedEncodingException {
 
         final StringBuilder sb = new StringBuilder();
         logger.info("strippedContent() ---> Getting external content");
         FutureTask<?> theTask = null;
         try {
             // create new task
             theTask = new FutureTask<Object>(new Runnable() {
                 public void run() {
 
                     connect();
                     if (con.getContentEncoding() != null) {
                         encoding = con.getContentEncoding();
                     }
 
                     InputStream stream = null;
                     if (content == null) {
                         stream = readContentStream();
                     } else {
                         stream = IOUtils.toInputStream(content);
                     }
 
                     HTMLTextExtractor textExtractor = new HTMLTextExtractor();
 
                     try {
                         connect();
                         String contentType = con.getContentType();
                        if ("text/html".equalsIgnoreCase(contentType) || "text/xhtml".equalsIgnoreCase(contentType) || "text/xml".equalsIgnoreCase(contentType)) {
                             Reader reader = textExtractor.extractText(stream, contentType, "UTF-8");
                             int charValue = 0;
 
                             while ((charValue = reader.read()) != -1) {
                                 sb.append((char) charValue);
                             }
                             logger.info("strippedContent() ---> TEXT:\n" + sb.toString());
                         }
                     } catch (Exception e) {
                         logger.warn("URLActions.strippedContent() gave Exception, returning \"\" ---> " + e.getMessage());
                         sb.append("");
                     }
 
                 }
             }, null);
 
             // start task in a new thread
             new Thread(theTask).start();
 
             // wait for the execution to finish, timeout after 10 secs
             theTask.get(10L, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             ourcode = "CONNECTION_TIMEOUT";
         }
         return sb.toString();
 
     }
 }
