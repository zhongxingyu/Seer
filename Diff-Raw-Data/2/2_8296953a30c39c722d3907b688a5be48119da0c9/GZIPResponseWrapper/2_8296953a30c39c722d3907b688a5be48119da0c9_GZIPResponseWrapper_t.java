 /*
  * Copyright 2003 Jayson Falkner (jayson@jspinsider.com)
  * This code is from "Servlets and JavaServer pages; the J2EE Web Tier",
  * http://www.jspbook.com. You may freely use the code both commercially
  * and non-commercially. If you like the code, please pick up a copy of
  * the book and help support the authors, development of more free code,
  * and the JSP/Servlet/J2EE community.
  *
  * Modified by David Winslow <dwinslow@openplans.org>
  */
 package org.geoserver.filters;
 
 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 public class GZIPResponseWrapper extends HttpServletResponseWrapper {
     protected HttpServletResponse origResponse = null;
     protected ServletOutputStream stream = null;
     protected PrintWriter writer = null;
     protected Set preCompressedFormats;
     protected Logger logger = org.geotools.util.logging.Logging.getLogger("org.geoserver.filters");
 
     public GZIPResponseWrapper(HttpServletResponse response) {
         super(response);
         origResponse = response;
         // TODO: allow user-configured format list here
         preCompressedFormats = new HashSet();
         preCompressedFormats.add("application/pdf");
         preCompressedFormats.add("image/png");
         preCompressedFormats.add("image/gif");
     }
 
     public ServletOutputStream createOutputStream() throws IOException {
         String type = getContentType();
 
         if (type != null && preCompressedFormats.contains(type)){
             logger.info("Getting the plain writer for content type: " + type);
             return origResponse.getOutputStream();
         }
         logger.info("Getting a compressed writer for content type: " + type);
         return new GZIPResponseStream(origResponse);
     }
 
     public void setContentType(String type){
         if (stream != null){
             logger.warn("Setting mimetype after acquiring stream! was:" +
                    getContentType() + "; set to: "); 
         }
         origResponse.setContentType(type);
     }
 
     public void finishResponse() {
         try {
             if (writer != null) {
                 writer.close();
             } else {
                 if (stream != null) {
                     stream.close();
                 }
             }
         } catch (IOException e) {}
     }
 
     public void flushBuffer() throws IOException {
         if (writer!= null){
             writer.flush();
         } else if (stream != null) {
             stream.flush();
         }
     }
 
     public ServletOutputStream getOutputStream() throws IOException {
         if (writer != null) {
             throw new IllegalStateException("getWriter() has already been called!");
         }
 
         if (stream == null)
             stream = createOutputStream();
         return (stream);
     }
 
     public PrintWriter getWriter() throws IOException {
         if (writer != null) {
             return (writer);
         }
 
         if (stream != null) {
             throw new IllegalStateException("getOutputStream() has already been called!");
         }
 
         stream = createOutputStream();
         writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
         return (writer);
     }
 
     public void setContentLength(int length) {}
 }
