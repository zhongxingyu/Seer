 package com.akrivos.eos.http;
 
 import com.akrivos.eos.http.constants.HttpMethod;
 import com.akrivos.eos.http.constants.HttpResponseHeader;
 import com.akrivos.eos.http.constants.HttpStatusCode;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TimeZone;
 
 /**
  * A class that holds every information that describes an HTTP response.
  * Includes status code, headers and body. It also provides a method
  * to send the response over an {@link OutputStream}.
  */
 public class HttpResponse {
     private final Map<HttpResponseHeader, String> headers;
     private final DataOutputStream writer;
     private final SimpleDateFormat df;
     private HttpStatusCode statusCode;
     private byte[] body;
     private boolean wasHeadRequest;
 
     /**
      * Creates a new {@link HttpResponse} with an {@link HttpRequest} and
      * an {@link OutputStream} from the client's {@link java.net.Socket},
      * to send data to.
      *
      * @param request the {@link HttpRequest} to get information.
      * @param out     the {@link OutputStream} to send data to.
      */
     public HttpResponse(HttpRequest request, OutputStream out) {
         headers = new HashMap<HttpResponseHeader, String>();
         writer = new DataOutputStream(new BufferedOutputStream(out));
         wasHeadRequest = (request != null && request.getMethod() == HttpMethod.HEAD);
 
         // RFC 1123 date format
         df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
 
         // default status code
         setStatusCode(HttpStatusCode.OK);
     }
 
     /**
      * Returns the {@link HttpStatusCode}.
      *
      * @return the {@link HttpStatusCode}.
      */
     public HttpStatusCode getStatusCode() {
         return statusCode;
     }
 
     /**
      * Sets the {@link HttpStatusCode}.
      *
      * @param statusCode the {@link HttpStatusCode}.
      */
     public void setStatusCode(HttpStatusCode statusCode) {
         this.statusCode = statusCode;
     }
 
     /**
      * Returns the value of the given {@link HttpResponseHeader}.
      *
      * @param header the {@link HttpResponseHeader}.
      * @return the value of the {@link HttpResponseHeader} if found,
      *         null otherwise.
      */
     public String getHeader(HttpResponseHeader header) {
         return headers.get(header);
     }
 
     /**
      * Sets the value of the given {@link HttpResponseHeader}.
      *
      * @param header the {@link HttpResponseHeader}.
      * @param value  the {@link String} value.
      */
     public void setHeader(HttpResponseHeader header, String value) {
         headers.put(header, value);
     }
 
     /**
      * Returns the body data.
      *
      * @return the body data.
      */
     public byte[] getBody() {
         return body;
     }
 
     /**
     * Sets the body data from {@link byte[]}.
      * Also sets the Content-Length header to the data length.
      *
     * @param body the body data in {@link byte[]}.
      */
     public void setBody(byte[] body) {
         this.body = body;
         setHeader(HttpResponseHeader.ContentLength, String.valueOf(body.length));
     }
 
     /**
      * Sets the body data from {@link String}.
      * Also sets the Content-Length header to the data length.
      *
      * @param body the body data in {@link String}.
      * @throws UnsupportedEncodingException if UTF-8 is not the right encoding.
      */
     public void setBody(String body) throws UnsupportedEncodingException {
         this.body = body.getBytes("UTF-8");
         setHeader(HttpResponseHeader.ContentLength, String.valueOf(body.length()));
     }
 
     /**
      * Sets the Last-Modified header using the RFC 1123 format.
      *
      * @param date the last modified {@link Date}.
      */
     public void setLastModified(Date date) {
         setHeader(HttpResponseHeader.LastModified, df.format(date));
     }
 
     /**
      * Sends the {@link HttpResponse} in three parts: a) sends the
      * status line, b) sends the headers, and c) sends the body,
      * only if it was not a HEAD request.
      *
      * @throws Exception any exception that might occur.
      */
     public void send() throws Exception {
         setHeader(HttpResponseHeader.Date, df.format(new Date()));
         setHeader(HttpResponseHeader.Server, HttpServer.SERVER_NAME);
 
         writeStatusLine();
         writeHeaders();
         if (!wasHeadRequest) {
             writeBody();
         }
         writer.flush();
     }
 
     /**
      * Sends the status line to the client's {@link java.net.Socket}.
      * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF.
      *
      * @throws Exception any exception that might occur.
      */
     private void writeStatusLine() throws Exception {
         // Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
         writer.writeBytes(String.format("%s%s%s%s%s%s",
                 HttpServer.HTTP_VERSION,
                 HttpServer.SP,
                 statusCode.getStatusCode(),
                 HttpServer.SP,
                 statusCode.getReasonPhrase(),
                 HttpServer.CRLF));
     }
 
     /**
      * Sends all the HTTP headers to the client's {@link java.net.Socket}.
      *
      * @throws Exception any exception that might occur.
      */
     private void writeHeaders() throws Exception {
         for (Entry<HttpResponseHeader, String> header : headers.entrySet()) {
             String headerStr = String.format("%s: %s%s",
                     header.getKey().getName(),
                     header.getValue(), HttpServer.CRLF);
             writer.writeBytes(headerStr);
         }
         // line separating headers
         writer.writeBytes(HttpServer.CRLF);
     }
 
     /**
      * Sends the HTTP response body to the client's {@link java.net.Socket}.
      *
      * @throws Exception any exception that might occur.
      */
     private void writeBody() throws Exception {
         if (body != null) {
             writer.write(body);
         }
     }
 }
