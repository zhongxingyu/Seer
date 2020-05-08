 package com.ninja_squad.jb.codestory;
 
 import com.google.common.base.Preconditions;
 
 import javax.annotation.Nonnull;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.util.Map;
 
 /**
  * An HTTP response
  * @author JB
  */
 public final class HttpResponse {
 
     public static enum Status {
         _200_OK(200, "OK"),
         _201_CREATED(201, "Created"),
         _400_BAD_REQUEST(400, "Bad Request"),
         _404_NOT_FOUND(404, "Not Found"),
         _500_INTERNAL_ERROR(500, "Internal Error");
 
         private final int code;
         private final String reason;
 
         private Status(int code, String reason) {
             this.code = code;
             this.reason = reason;
         }
 
         public int getCode() {
             return code;
         }
 
         public String getReason() {
             return reason;
         }
     }
 
     private static final byte[] NO_BODY = new byte[0];
 
     private final Status status;
     private final HttpHeaders headers;
     private final byte[] body;
 
     public HttpResponse(@Nonnull Status status, @Nonnull HttpHeaders headers, byte[] body) {
         this.status = Preconditions.checkNotNull(status);
         this.headers = Preconditions.checkNotNull(headers);
         this.body = body == null ? NO_BODY : body;
     }
 
     public static HttpResponse badRequest(@Nonnull String body) {
         return new HttpResponse(Status._400_BAD_REQUEST,
                                 HttpHeaders.PLAIN_ASCII_TEXT,
                                 body.getBytes(StandardCharsets.US_ASCII));
     }
 
     public static HttpResponse ok(@Nonnull String body) {
         return new HttpResponse(Status._200_OK,
                                 HttpHeaders.PLAIN_ASCII_TEXT,
                                 body.getBytes(StandardCharsets.US_ASCII));
     }
 
     public Status getStatus() {
         return status;
     }
 
     public HttpHeaders getHeaders() {
         return headers;
     }
 
     public byte[] getBody() {
         return body;
     }
 
     public String getBodyAsString(Charset charset) {
         return new String(body, charset);
     }
 
     public void send(OutputStream out) throws IOException {
         String EOL = "\r\n";
         Writer writer = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
         writer.write("HTTP/1.1 ");
         writer.write(String.valueOf(status.getCode()));
         writer.write(" ");
         writer.write(status.getReason());
         writer.write(EOL);
         writer.write("Server: JB's CodeStory Server");
         writer.write(EOL);
         if (body.length > 0) {
             writer.write("Content-Length:");
            writer.write(body.length);
             writer.write(EOL);
         }
 
         for (Map.Entry<String, String> header : headers.asMap().entrySet()) {
             writer.write(header.getKey());
             writer.write(':');
             writer.write(header.getValue());
             writer.write(EOL);
         }
         writer.write(EOL);
         writer.flush();
         out.write(body);
     }
 }
