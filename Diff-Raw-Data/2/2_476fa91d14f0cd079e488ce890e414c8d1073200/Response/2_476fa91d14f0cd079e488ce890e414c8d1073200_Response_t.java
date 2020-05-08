 package ch.rollis.emma.response;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.HashMap;
 
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.util.DateConverter;
 
 public class Response {
     /**
      * Protocol version to use for the response.
      */
     private String protocol;
 
     /**
      * Request this response answers.
      */
     private Request request;
 
     /**
      * Response status.
      */
     private ResponseStatus status;
 
     /**
      * Byte array of entityBody.
      */
     private byte[] entityBody;
 
     /**
      * Stores UPPERCASE field names to normal case relation for all
      * response headers.
      * 
      * This HashMap is used to write out the header fields in normal case
      * though header fields are case-insensitive. Looks better. :-)
      */
     private final HashMap<String, String> prettyFieldNames = new HashMap<String, String>();
 
     /**
      * Build the HashMap for all general headers.
      * 
      * Additionally registers all known header fields in prettyFieldNames
      * to enable pretty output formatting of header field names regarding case.
      */
     @SuppressWarnings("serial")
     private final HashMap<String, String> headersGeneral = new HashMap<String, String>() {
         {
             put("DATE", null);
             prettyFieldNames.put("DATE", "Date");
             put("PRAGMA", null);
             prettyFieldNames.put("PRAGMA", "Pragma");
         }
     };
 
     /**
      * Build the HashMap for all response headers.
      * 
      * Additionally registers all known header fields in prettyFieldNames
      * to enable pretty output formatting of header field names regarding case.
      */
     @SuppressWarnings("serial")
     private final HashMap<String, String> headersResponse = new HashMap<String, String>() {
         {
             put("LOCATION", null);
             prettyFieldNames.put("LOCATION", "Location");
             put("SERVER", null);
             prettyFieldNames.put("SERVER", "Server");
             put("WWW-AUTHENTICATE", null);
             prettyFieldNames.put("WWW-AUTHENTICATE", "WWW-Authenticate");
         }
     };
 
     /**
      * Build the HashMap for all entity headers.
      * 
      * Additionally registers all known header fields in prettyFieldNames
      * to enable pretty output formatting of header field names regarding case.
      */
     @SuppressWarnings("serial")
     private final HashMap<String, String> headersEntity = new HashMap<String, String>() {
         {
             put("ALLOW", null);
             prettyFieldNames.put("ALLOW", "Allow");
             put("CONTENT-ENCODING", null);
             prettyFieldNames.put("CONTENT-ENCODING", "Content-Encoding");
             put("CONTENT-LENGTH", null);
             prettyFieldNames.put("CONTENT-LENGTH", "Content-Length");
             put("CONTENT-TYPE", null);
             prettyFieldNames.put("CONTENT-TYPE", "Content-Type");
             put("EXPIRES", null);
             prettyFieldNames.put("EXPIRES", "Expires");
             put("LAST-MODIFIED", null);
             prettyFieldNames.put("LAST-MODIFIED", "Last-Modified");
         }
     };
 
     /**
      * A single space; rfc1945
      */
     private static final String SP = " ";
 
     /**
      * End of line sequence; rfc1945
      */
     private static final String CRLF = "\r\n";
 
     /**
      * A horizontal tab; rfc1945
      */
     private static final String HT = "\t";
 
     Response() {
         this("HTTP/1.1");
     }
 
     Response(String protocol) {
         this.protocol = protocol;
     }
 
     public void setProtocol(String protocol) {
         this.protocol = protocol;
     }
 
     public String getProtocol() {
         if (request != null) {
             return request.getProtocol();
         }
         return protocol;
     }
 
     public Request getRequest() {
         return request;
     }
 
     public void setRequest(Request request) {
         this.request = request;
     }
 
     /**
      * @return the status
      */
     public ResponseStatus getStatus() {
         return status;
     }
 
     public void setStatus(ResponseStatus status) {
         this.status = status;
     }
 
     public String getContentType() {
         return getHeader("Content-Type");
     }
 
     public void setContentType(String value) {
         setHeader("Content-Type", value);
     }
 
     public String getContentLength() {
         return getHeader("Content-Length");
     }
 
     public void setContentLength(String value) {
         setHeader("Content-Length", value);
     }
 
     public String getLastModified() {
         return getHeader("Last-modified");
     }
 
     public void setLastModified(String value) {
         setHeader("Last-modified", value);
     }
 
     /**
      * Returns the value of a general header field.
      * 
      * @param key
      *            Header field to retrieve the value for
      * @return Value of header field
      */
     public String getHeader(String key) {
         key = key.toUpperCase();
         if (headersGeneral.containsKey(key)) {
             return headersGeneral.get(key);
         } else if (headersResponse.containsKey(key)) {
             return headersResponse.get(key);
         } else if (headersEntity.containsKey(key)) {
             return headersEntity.get(key);
         }
         return null;
     }
 
     /**
      * Set the value of a header field to the response.
      * 
      * Values for unknown header fields are stored as entity headers, see
      * rfc1945.
      * 
      * @param key
      *            Header field
      * @param value
      *            Value of header field
      */
     public void setHeader(String key, String value) {
         key = key.toUpperCase();
         if (headersGeneral.containsKey(key)) {
             headersGeneral.put(key, value);
         } else if (headersResponse.containsKey(key)) {
             headersResponse.put(key, value);
         } else {
             headersEntity.put(key, value);
         }
     }
 
     public void setEntity(String entity) {
         setEntity(entity.getBytes());
     }
 
     public void setEntity(byte[] entity) {
         this.entityBody = entity;
         this.setHeader("Content-Length", String.valueOf(entityBody.length));
     }
 
     public void send(OutputStream output) throws IOException {
         BufferedOutputStream out = new BufferedOutputStream(output);
 
         if (getContentLength() == null) {
             setContentLength(String.valueOf(entityBody.length));
         }
 
         if (!"HTTP/0.9".equals(getProtocol())) {
             sendStatusLine(out);
             sendGeneralHeaders(out);
             sendResponseHeaders(out);
             sendEntityHeaders(out);
             out.write(CRLF.getBytes());
         }
 
         String cl = getContentLength();
        if ((request == null || (request != null && !request.isHead()))
                 && cl != null
                 && Integer.parseInt(cl) > 0) {
             out.write(entityBody);
         }
         out.flush();
     }
 
     private void sendStatusLine(BufferedOutputStream out) throws IOException {
         StringBuilder sb = new StringBuilder();
         sb.append(protocol).append(SP).append(status.getCode()).append(SP)
         .append(status.getReasonPhrase()).append(CRLF);
         out.write(sb.toString().getBytes());
     }
 
     private void sendGeneralHeaders(BufferedOutputStream out) throws IOException {
         if (getHeader("Date") == null) {
             setHeader("Date", DateConverter.formatRfc1123(new Date()));
         }
         for (String fieldName : headersGeneral.keySet()) {
             String fieldValue = headersGeneral.get(fieldName);
             if (fieldValue != null) {
                 out.write(printHeaderLine(fieldName, fieldValue));
             }
         }
     }
 
     private void sendResponseHeaders(BufferedOutputStream out) throws IOException {
         for (String fieldName : headersResponse.keySet()) {
             String fieldValue = headersResponse.get(fieldName);
             if (fieldValue != null) {
                 out.write(printHeaderLine(fieldName, fieldValue));
             }
         }
     }
 
     private void sendEntityHeaders(BufferedOutputStream out) throws IOException {
         for (String fieldName : headersEntity.keySet()) {
             String fieldValue = headersEntity.get(fieldName);
             if (fieldValue != null) {
                 out.write(printHeaderLine(fieldName, fieldValue));
             }
         }
     }
 
     private byte[] printHeaderLine(String fieldName, String fieldValue) {
         return String.format("%s: %s" + CRLF, getPrettyHeaderFieldName(fieldName), fieldValue)
                 .getBytes();
     }
 
     private String getPrettyHeaderFieldName(String fieldName) {
         if (prettyFieldNames.containsKey(fieldName)) {
             return prettyFieldNames.get(fieldName);
         }
         return fieldName;
     }
 }
