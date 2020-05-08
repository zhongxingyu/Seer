 package org.jboss.pressgang.ccms.restserver.servlet.filter;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 /**
  * A wrapper class to handle ServetResponse and compress the output.
  * <br /><br />
  * See: <a href="http://stackoverflow.com/questions/4755302/which-compression-is-gzip-the-most-popular-servlet-filter-would-you-suggest/11068672#11068672">which-compression-is-gzip-the-most-popular-servlet-filter-would-you-suggest</a>
  * and <a href="https://github.com/geoserver/geoserver/tree/master/src/main/src/main/java/org/geoserver/filters">https://github.com/geoserver/geoserver/</a>
  * for details on where this implementation came from.
  */
 public class GZIPResponseWrapper extends HttpServletResponseWrapper {
 
     private GZIPServletOutputStream GZIPStream = null;
     private ServletOutputStream outputStream = null;
     private PrintWriter printWriter = null;
     private final HttpServletResponse response;
     private final Set<Pattern> mimeTypes;
 
     public GZIPResponseWrapper(final HttpServletResponse response, final Set<Pattern> mimeTypes) {
         super(response);
         this.response = response;
         this.mimeTypes = mimeTypes;
     }
 
     public void finish() throws IOException {
         if (this.printWriter != null) {
             this.printWriter.close();
         }
         if (this.outputStream != null) {
             this.outputStream.close();
         }
     }
 
     @Override
     public void flushBuffer() throws IOException {
         if (this.printWriter != null) {
             this.printWriter.flush();
         }
         if (this.outputStream != null) {
             this.outputStream.flush();
         }
         super.flushBuffer();
     }
 
     @Override
     public ServletOutputStream getOutputStream() throws IOException {
         final String contentType = response.getContentType();
 
         // Check if the response should be compressed based on the mime-type
         if (isCompressible(contentType)) {
 
             if (this.printWriter != null)
                 throw new IllegalStateException("Print Writer already defined");
             if (this.outputStream == null) {
                 initGZIPStream();
                 this.outputStream = this.GZIPStream;
             }
         } else {
             this.outputStream = response.getOutputStream();
         }
 
         return this.outputStream;
     }
 
     @Override
     public PrintWriter getWriter() throws IOException {
         final String contentType = response.getContentType();
 
         // Check if the response should be compressed based on the mime-type
         if (isCompressible(contentType)) {
             if (this.outputStream != null)
                 throw new IllegalStateException("Print Writer already defined");
             if (this.printWriter == null) {
                 initGZIPStream();
                 this.printWriter = new PrintWriter(new OutputStreamWriter(this.GZIPStream, getResponse().getCharacterEncoding()));
             }
         } else {
             this.printWriter = super.getWriter();
         }
         return this.printWriter;
     }
 
     /**
      * Initialise the GZIP Stream to be used to encode the data, if it hasn't already been initialised.
      * 
      * @throws IOException
      */
     private void initGZIPStream() throws IOException {
         if (this.GZIPStream == null) {
             this.GZIPStream = new GZIPServletOutputStream(response);
         }
     }
 
     /**
      * Check if the response should be compressed based on the MIME type.
      * 
      * @param contentType The content type of the response.
      * @return True if the response should be compressed otherwise false.
      */
     protected boolean isCompressible(final String contentType) {
         // Strip away any extra details that are after the mime type
         final String stripped = stripParams(contentType);
 
         // Iterate over the mime types to see if any match the response mime type
         final Iterator<Pattern> it = mimeTypes.iterator();
         while (it.hasNext()) {
             final Pattern mimeTypePattern = it.next();
             final Matcher matcher = mimeTypePattern.matcher(stripped);
             if (matcher.matches())
                 return true;
         }
 
         return false;
     }
 
     /**
      * String away any extra parameters from a response Content-Type to find the MIME type.
      * 
      * @param contentType The content type of the response.
      * @return The MIME type of the response stripped of any extra variables.
      */
     protected String stripParams(final String contentType) {
         int firstSemicolon = contentType.indexOf(";");
 
         if (firstSemicolon != -1) {
             return contentType.substring(0, firstSemicolon);
         }
 
         return contentType;
     }
 }
