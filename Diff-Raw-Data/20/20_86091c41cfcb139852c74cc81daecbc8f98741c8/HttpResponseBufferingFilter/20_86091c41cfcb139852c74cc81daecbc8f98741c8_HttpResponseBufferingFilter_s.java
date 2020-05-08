 package au.com.miskinhill.web.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 import javax.ws.rs.core.MediaType;
 
 import org.springframework.web.filter.OncePerRequestFilter;
 
 /** Based on ContentBufferingFilter from SiteMesh 3. */
 public abstract class HttpResponseBufferingFilter extends OncePerRequestFilter {
     
     private final class BufferingHttpServletResponse extends HttpServletResponseWrapper {
         
         private StringWriter writer;
         private PrintWriter printWriter;
         private ByteArrayOutputStream outputStream;
         private ServletOutputStream servletOutputStream;
         private int status = HttpServletResponse.SC_OK;
         private String contentType;
         
         public BufferingHttpServletResponse(HttpServletResponse original) {
             super(original);
         }
         
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
             if (writer != null)
                 throw new IllegalStateException("Already called getWriter()");
             if (servletOutputStream == null) {
                 final OutputStream superOutputStream = super.getOutputStream();
                 servletOutputStream = new ServletOutputStream() {
                     private OutputStream underlying;
                     private void deferredInit() {
                         if (underlying == null) {
                             if (shouldBuffer()) {
                                 outputStream = new ByteArrayOutputStream();
                                 underlying = outputStream;
                             } else {
                                 underlying = superOutputStream; 
                             }
                         }
                     }
                     @Override
                     public void write(int b) throws IOException {
                         deferredInit();
                         underlying.write(b);
                     }
                     @Override
                     public void write(byte[] b) throws IOException {
                         deferredInit();
                         underlying.write(b);
                     }
                     @Override
                     public void write(byte[] b, int off, int len) throws IOException {
                         deferredInit();
                         underlying.write(b, off, len);
                     }
                     @Override
                     public void flush() throws IOException {
                         deferredInit();
                         underlying.flush();
                     }
                 };
             }
             return servletOutputStream;
         }
         
         @Override
         public PrintWriter getWriter() throws IOException {
             if (outputStream != null)
                 throw new IllegalStateException("Already called getOutputStream()");
             if (printWriter == null) {
                 if (shouldBuffer()) {
                     writer = new StringWriter();
                     printWriter = new PrintWriter(writer);
                 } else {
                     printWriter = super.getWriter();
                 }
             }
             return printWriter;
         }
         
         @Override
         public void setStatus(int sc) {
             super.setStatus(sc);
             status = sc;
         }
         
         @Override
         public void setStatus(int sc, String sm) {
             super.setStatus(sc, sm);
             status = sc;
         }
         
         @Override
         public void sendError(int sc) throws IOException {
             super.sendError(sc);
             status = sc;
         }
         
         @Override
         public void sendError(int sc, String msg) throws IOException {
             super.sendError(sc, msg);
             status = sc;
         }
         
         @Override
         public void sendRedirect(String location) throws IOException {
             super.sendRedirect(location);
             status = HttpServletResponse.SC_TEMPORARY_REDIRECT;
         }
         
         @Override
         public void setContentType(String type) {
             super.setContentType(type);
             contentType = type;
         }
         
         @Override
         public void setCharacterEncoding(String charset) {
             if (!charset.equals("UTF-8"))
                 throw new UnsupportedOperationException("Unacceptable charset " + charset);
             super.setCharacterEncoding(charset);
         }
         
         @Override
         public void addHeader(String name, String value) {
             super.addHeader(name, value);
             if (name.equalsIgnoreCase("Content-Type"))
                 contentType = value;
         }
         
         public String getBufferedResponse() {
             if (writer != null)
                 return writer.toString();
             if (outputStream != null) {
                 try {
                     return outputStream.toString("UTF-8");
                 } catch (UnsupportedEncodingException e) {
                     throw new RuntimeException(e);
                 }
             }
             return null;
         }
         
         public void writeThrough(String response) throws IOException {
             byte[] encodedResponse = response.getBytes("UTF-8");
             super.setContentLength(encodedResponse.length);
             if (outputStream != null) {
                 super.getOutputStream().write(encodedResponse);
             } else {
                 super.getWriter().write(response);
             }
         }
         
         @Override
         public String getContentType() {
             return contentType;
         }
         
         private boolean shouldBuffer() {
             return contentType != null &&
                     MediaType.TEXT_HTML_TYPE.isCompatible(MediaType.valueOf(contentType)) &&
                    status == 200; // XXX 206 and suchlike?
         }
         
     }
     
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
             throws ServletException, IOException {
         BufferingHttpServletResponse wrappedResponse = new BufferingHttpServletResponse((HttpServletResponse) response);
         filterChain.doFilter(request, wrappedResponse);
         String bufferedResponse = wrappedResponse.getBufferedResponse();
         if (bufferedResponse != null) {
             wrappedResponse.writeThrough(postprocessResponse(bufferedResponse));
         }
     }
     
     /**
      * Returns a post-processed version of the given buffered response.
      */
     protected abstract String postprocessResponse(String responseBody);
 
 }
