 package vinna.response;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vinna.exception.PassException;
 import vinna.http.VinnaRequestWrapper;
 import vinna.http.VinnaResponseWrapper;
 import vinna.util.MultivaluedHashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 public class ResponseBuilder implements Response {
     private static final Logger logger = LoggerFactory.getLogger(ResponseBuilder.class);
     private static final Response PASS_RESPONSE = new DoPass();
 
     private int status;
     private MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
     private String location;
     private InputStream body;
     private String encoding;
     private boolean isRedirect = false;
 
     public static ResponseBuilder withStatus(int status) {
         return new ResponseBuilder(status);
     }
 
     public static ResponseBuilder ok() {
         return new ResponseBuilder(HttpServletResponse.SC_OK);
     }
 
     public static ResponseBuilder notFound() {
         return new ResponseBuilder(HttpServletResponse.SC_NOT_FOUND);
     }
 
     public static Response pass() {
         return PASS_RESPONSE;
     }
 
     public ResponseBuilder(int status) {
         status(status);
     }
 
     public final ResponseBuilder status(int status) {
         this.status = status;
         return this;
     }
 
     public final ResponseBuilder redirect(String location) {
         this.location = location;
         this.isRedirect = true;
         return this;
     }
 
     public final ResponseBuilder type(String type) {
         header("Content-Type", type);
         return this;
     }
 
     public final ResponseBuilder encoding(String encoding) {
         this.encoding = encoding;
         header("Content-Encoding", encoding);
         return this;
     }
 
     public final ResponseBuilder language(String language) {
         header("Content-Language", language);
         return this;
     }
 
     public final ResponseBuilder variant(String variant) {
         header("Vary", variant);
         return this;
     }
 
     public final ResponseBuilder location(String location) {
         header("Location", location);
         return this;
     }
 
     public final ResponseBuilder etag(String etag) {
         header("ETag", etag);
         return this;
     }
 
     public final ResponseBuilder lastModified(Date lastModified) {
         header("Last-Modified", lastModified);
         return this;
     }
 
     public final ResponseBuilder cacheControl(String cacheControl) {
         header("Cache-Control", cacheControl);
         return this;
     }
 
     public final ResponseBuilder expires(Date expires) {
         header("Expires", expires);
         return this;
     }
 
     //FIXME: split into addHeader and setHeader (maybe call the latter header)
     public final ResponseBuilder header(String name, Object value) {
         headers.add(name, value);
         return this;
     }
 
     // TODO define parameters
     public final ResponseBuilder cookie() {
         // TODO
         return this;
     }
 
     public final ResponseBuilder body(InputStream body) {
         this.body = body;
         return this;
     }
 
     protected void writeBody(ServletOutputStream out) throws IOException {
         if (body != null) {
             int size = 512;//FIXME: make this configurable ?
             byte[] buffer = new byte[size];
             int len;
            while ((len = body.read(buffer)) >= 0) {
                 out.write(buffer, 0, len);
             }
             try {
                 body.close();
             } catch (IOException e) {
                 logger.warn("Cannot close the response body input stream", e);
             }
         }
     }
 
     public final int getStatus() {
         return status;
     }
 
     public final Object getFirstHeader(String header) {
         return headers.getFirst(header);
     }
 
     public final List<Object> getHeaders(String header) {
         return headers.get(header);
     }
 
     public final String getEncoding() {
         return encoding;
     }
 
     @Override
     public final void execute(VinnaRequestWrapper request, VinnaResponseWrapper response) throws IOException, ServletException {
         response.setStatus(status);
 
         for (Map.Entry<String, List<Object>> header : headers.entrySet()) {
             for (Object value : header.getValue()) {
                 //FIXME: properly handle multi-valued headers (using servletResponse.(add|set)Header)
                 response.addHeader(header.getKey(), value.toString());
             }
         }
 
         // FIXME: investigate how to properly handle redirect
         if (isRedirect) {
             if (this.location != null) {
                 String locationUrl = response.encodeRedirectURL(location);
                 if (!hasScheme(locationUrl)) {
                     StringBuilder buffer = new StringBuilder();
                     buffer.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
                     if (!locationUrl.startsWith("/")) {
                         buffer.append(request.getContextPath()).append("/");
                     }
                     buffer.append(locationUrl);
                     locationUrl = buffer.toString();
                 }
 
                 response.setHeader("Location", locationUrl);
             }
             return;
         }
 
         if (encoding != null) {
             response.setCharacterEncoding(encoding);
         }
 
         writeBody(response.getOutputStream());
         response.getOutputStream().flush();
     }
 
     private boolean hasScheme(String uri) {
         for (int i = 0; i < uri.length(); i++) {
             char c = uri.charAt(i);
             if (c == ':')
                 return true;
             if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || (i > 0 && (c >= '0' && c <= '9' || c == '.' || c == '+' || c == '-'))))
                 break;
         }
         return false;
     }
 
     private static class DoPass implements Response {
         @Override
         public void execute(VinnaRequestWrapper request, VinnaResponseWrapper response) throws IOException, ServletException {
             throw new PassException();
         }
     }
 }
