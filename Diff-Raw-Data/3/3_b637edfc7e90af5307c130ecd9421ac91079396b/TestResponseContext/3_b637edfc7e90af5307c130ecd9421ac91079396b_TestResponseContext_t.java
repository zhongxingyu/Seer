 package example.framework.test;
 
 import example.framework.Header;
 import example.framework.ResponseContext;
 import org.apache.commons.lang.Validate;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.Map;
 
 public class TestResponseContext implements ResponseContext {
 
     private Map<String, Object> attributes = Collections.emptyMap();
 
     private String contextPath = "";
     private String servletPath = "";
 
     private Header header;
 
     private int errorCode;
     private String message;
 
     private String redirectURL;
 
     private String contentType;
     private String charset;
 
     private StringWriter writer;
     private ByteArrayOutputStream stream;
 
     public Map<String, Object> getAttributes() {
         return attributes;
     }
 
     public String getContextPath() {
         return contextPath;
     }
 
     public String getServletPath() {
         return servletPath;
     }
 
     public void setHeader(Header header) {
         this.header = header;
     }
 
     public void sendError(int errorCode) throws IOException {
         this.errorCode = errorCode;
     }
 
     public void sendError(int errorCode, String message) throws IOException {
         this.errorCode = errorCode;
         this.message = message;
     }
 
     public void sendRedirect(String redirectURL) throws IOException {
         this.redirectURL = redirectURL;
     }
 
     public void setContentType(String contentType) {
         this.contentType = contentType;
     }
 
     public void setCharacterEncoding(String charset) {
         this.charset = charset;
     }
 
     public PrintWriter getWriter() throws IOException {
         Validate.isTrue(stream == null, "Response already passed to output stream");
         if (writer == null) {
             writer = new StringWriter();
         }
         return new PrintWriter(writer);
     }
 
     public OutputStream getOutputStream() throws IOException {
         Validate.isTrue(writer == null, "Response already passed to writer");
         if (stream == null) {
             stream = new ByteArrayOutputStream(4098);
         }
         return stream;
     }
 
     public void setAttributes(Map<String, Object> attributes) {
         this.attributes = attributes;
     }
 
     public void setContextPath(String contextPath) {
         this.contextPath = contextPath;
     }
 
     public void setServletPath(String servletPath) {
         this.servletPath = servletPath;
     }
 
     public Header getHeader() {
         return header;
     }
 
     public int getErrorCode() {
         return errorCode;
     }
 
     public String getMessage() {
         return message;
     }
 
     public String getRedirectURL() {
         return redirectURL;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public String getCharset() {
         return charset;
     }
 
     public String getResponseBodyText() {
        Validate.notNull(writer, "No response body text");
         return writer.toString();
     }
 
     public byte[] getResponseBodyBytes() {
        Validate.notNull(stream, "No response body bytes");
         return stream.toByteArray();
     }
 }
