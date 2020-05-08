 package com.dulvac.jerry.handlers;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpInetConnection;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.MethodNotSupportedException;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.Locale;
 
 /**
  * A simple HTTP file handler
  * Only supports GET and HEAD requests and text/html mime type
  * @see HttpRequestHandler
  */
 public class SimpleFileHandler implements HttpRequestHandler {
   private static final Logger logger = LoggerFactory.getLogger(SimpleFileHandler.class.getName());
   private final String filesRoot;
   private String[] welcomeFiles = {"index.html", "index.htm"};
 
   /**
    *
    * @return The array of index file names 
    */
   public String[] getWelcomeFiles() {
     return welcomeFiles;
   }
 
   /**
    *
    * @param welcomeFiles Array of file names to use when a request is done on a directory
    */
   public void setWelcomeFiles(String[] welcomeFiles) {
     this.welcomeFiles = welcomeFiles;
   }
 
   /**
    * Return the first index file which exists in this directory path, if it exists
    * @param dir The path for which to return the index file, if it exists.
    * @return The full path of first index file which exists in this directory path, if it exists, otherwise return dir
    */
   public File getWelcomeFile(File dir) {
     for (int i = 0; i < welcomeFiles.length; i++) {
       // TODO: This might be expensive
       File welcomeFile = new File(dir, welcomeFiles[i]);
       if (welcomeFile.exists() && !welcomeFile.isDirectory()) return welcomeFile;
     }
     return dir;
   }
 
   // TODO: Support more methods
   private static enum METHODS {
     GET,
     HEAD
   }
 
   /**
    *
    * @param request The client HTTP request
    * @return The HTTP type (e.g. GET) of this request
    * @throws MethodNotSupportedException If the method is not supported by this implementation
    */
   public static String getMethod(HttpRequest request) throws MethodNotSupportedException {
     String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
     try {
       METHODS.valueOf(method);
     } catch (IllegalArgumentException ex) {
        throw new MethodNotSupportedException(method + " method not supported");
     }
     return method;
   }
 
   private static final StringEntity notFoundHTML = new StringEntity("<html><body><h1>File not found</h1></body></html>",
                                                                    ContentType.create("text/html", "UTF-8"));
   private static final StringEntity forbiddenHTML = new StringEntity("<html><body><h1>Forbidden</h1></body></html>",
                                                                     ContentType.create("text/html", "UTF-8"));
 
   /**
    *
    * @param filesRoot The document root for the files to handle
    */
   public SimpleFileHandler(final String filesRoot) {
     super();
     this.filesRoot = filesRoot;
   }
 
   /**
    * @see HttpRequestHandler#handle(org.apache.http.HttpRequest, org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext) 
    * @param request
    * @param response
    * @param context
    * @throws HttpException
    * @throws IOException
    */
   public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
     throws HttpException, IOException {
 
     final String method = getMethod(request);
     final String target = request.getRequestLine().getUri();
     // get client address for logging TODO: ugly; make this lazy
     final String clientAddress = ((HttpInetConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION))
       .getRemoteAddress().getHostAddress();
     
     File file = new File(this.filesRoot, URLDecoder.decode(target, "UTF-8"));
     if (file.isDirectory()) {
      file = getWelcomeFile(file);
     }
     String filePath = file.getPath();
     // Build response
     if (!file.exists()) {
       response.setStatusCode(HttpStatus.SC_NOT_FOUND);
       response.setEntity(this.notFoundHTML);
       logger.info("Request from {}. File {} not found", clientAddress, filePath);
 
     } else if (!file.canRead()) {
       response.setStatusCode(HttpStatus.SC_FORBIDDEN);
       response.setEntity(this.forbiddenHTML);
       logger.info("Request from {}. Can't read file {}", clientAddress, filePath);
       
     } else {
       response.setStatusCode(HttpStatus.SC_OK);
       // TODO: Support different kinds of files/ mime-types
       FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
       response.setEntity(body);
       logger.info("Request from {}. Sending file {}", clientAddress, filePath);
     }
   }
 }
