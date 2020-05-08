 package ch.rollis.emma.response;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.HashMap;
 
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.util.DateConverter;
 
 /**
  * The Response class encapsulates all data and behavior for managing a
  * response.
  * <p>
  * The response object can be manipulated in all aspects of a HTTP response i.e.
  * by setting the HTTP status, the entity body, headers and the like.
  * 
  * @author mrolli
  */
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
      * A single space; rfc1945.
      */
     private static final String SP = " ";
 
     /**
      * End of line sequence; rfc1945.
      */
     private static final String CRLF = "\r\n";
 
     /**
      * Class constructor sets up a vanilla HTTP/1.1 resopnse.
      */
     Response() {
         this("HTTP/1.1");
     }
 
     /**
      * Class constructor sets up a vanilla response with the given protocol.
      * 
      * @param proto
      *            The protocol version to use
      */
     Response(final String proto) {
         this.protocol = proto;
     }
 
     /**
      * Sets the protocol version to use for the response.
      * 
      * @param proto
      *            The protocol version to use
      */
     public void setProtocol(final String proto) {
         this.protocol = proto;
     }
 
     /**
      * Returns the protocol version of the response.
      * 
      * @return The protocol version set
      */
     public String getProtocol() {
         if (request != null) {
             return request.getProtocol();
         }
         return protocol;
     }
 
     /**
      * Returns the request the response is associated with.
      * 
      * @return The request object
      */
     public Request getRequest() {
         return request;
     }
 
     /**
      * Set the request the response is based on.
      * 
      * @param req
      *            The request the response answers
      */
     public void setRequest(final Request req) {
        this.request = req;
     }
 
     /**
      * @return the status
      */
     public ResponseStatus getStatus() {
         return status;
     }
 
     /**
      * Sets the response status of the response.
      * 
      * @param rspStatus
      *            The response status
      */
     public void setStatus(final ResponseStatus rspStatus) {
         this.status = rspStatus;
     }
 
     /**
      * Returns the value of the content type header of this response.
      * <p>
      * This is a convenience method.
      * 
      * @return The content type header value
      */
     public String getContentType() {
         return getHeader("Content-Type");
     }
 
     /**
      * Sets the content type header of this response.
      * <p>
      * This is a convenience method.
      * 
      * @param value
      *            The content type header value
      */
     public void setContentType(final String value) {
         setHeader("Content-Type", value);
     }
 
     /**
      * Returns the value of the content length header of this response.
      * <p>
      * This is a convenience method.
      * 
      * @return The content length header value
      */
     public String getContentLength() {
         return getHeader("Content-Length");
     }
 
     /**
      * Sets the content type length of this response.
      * <p>
      * This is a convenience method.
      * 
      * @param value
      *            The content type header value
      */
     public void setContentLength(final String value) {
         setHeader("Content-Length", value);
     }
 
     /**
      * Returns the value of the last modified header of this response.
      * <p>
      * This is a convenience method.
      * 
      * @return The last modified header value
      */
     public String getLastModified() {
         return getHeader("Last-modified");
     }
 
     /**
      * Sets the last modified header of this response.
      * <p>
      * This is a convenience method.
      * 
      * @param value
      *            The last modified header value
      */
     public void setLastModified(final String value) {
         setHeader("Last-modified", value);
     }
 
     /**
      * Returns the value of a header field.
      * 
      * @param headerField
      *            Header field to retrieve the value for
      * @return Value of header field
      */
     public String getHeader(final String headerField) {
         String key = headerField.toUpperCase();
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
      * @param headerField
      *            Header field
      * @param value
      *            Value of header field
      */
     public void setHeader(final String headerField, final String value) {
         String key = headerField.toUpperCase();
         if (headersGeneral.containsKey(key)) {
             headersGeneral.put(key, value);
         } else if (headersResponse.containsKey(key)) {
             headersResponse.put(key, value);
         } else {
             headersEntity.put(key, value);
         }
     }
 
     /**
      * Sets the entity body to send to the client.
      * 
      * @param entity
      *            The entity body as String type
      */
     public void setEntity(final String entity) {
         setEntity(entity.getBytes());
     }
 
     /**
      * Sets the entity body to send to the client.
      * 
      * @param entity
      *            The entity body as byte array type
      */
     public void setEntity(final byte[] entity) {
         this.entityBody = entity;
         this.setHeader("Content-Length", String.valueOf(entityBody.length));
     }
 
     /**
      * Send the response to the give output stream.
      * <p>
      * This method handles differences between simple and full requests and
      * additionally does not send an entity body if the request was a HEAD
      * request.
      * 
      * @param output
      *            The output stream to send the response to
      * @throws IOException
      *             In case of stream problems
      */
     public void send(final OutputStream output) throws IOException {
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
 
     /**
      * Sends the HTTP response status line to the output stream.
      * 
      * @param out
      *            The output stream to send the output to
      * @throws IOException
      *             In case of stream problems
      */
     private void sendStatusLine(final BufferedOutputStream out) throws IOException {
         StringBuilder sb = new StringBuilder();
         sb.append(protocol).append(SP).append(status.getCode()).append(SP)
         .append(status.getReasonPhrase()).append(CRLF);
         out.write(sb.toString().getBytes());
     }
 
     /**
      * Send all general headers to the output stream.
      * 
      * @param out
      *            The output stream to send the output to
      * @throws IOException
      *             In case of stream problems
      */
     private void sendGeneralHeaders(final BufferedOutputStream out) throws IOException {
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
 
     /**
      * Sends all response headers to the output stream.
      * 
      * @param out
      *            The output stream to send the output to
      * @throws IOException
      *             In case of stream problems
      */
     private void sendResponseHeaders(final BufferedOutputStream out) throws IOException {
         for (String fieldName : headersResponse.keySet()) {
             String fieldValue = headersResponse.get(fieldName);
             if (fieldValue != null) {
                 out.write(printHeaderLine(fieldName, fieldValue));
             }
         }
     }
 
     /**
      * Sends all entity headers to the output stream.
      * <p>
      * The trailing CRLF to indicate the end of the header section has to sent
      * by the caller and intentionally is not sent by this method.
      * 
      * @param out
      *            The output stream to send the output to
      * @throws IOException
      *             In case of stream problems
      */
     private void sendEntityHeaders(final BufferedOutputStream out) throws IOException {
         for (String fieldName : headersEntity.keySet()) {
             String fieldValue = headersEntity.get(fieldName);
             if (fieldValue != null) {
                 out.write(printHeaderLine(fieldName, fieldValue));
             }
         }
     }
 
     /**
      * Formats a header line for a given header field and its value.
      * <p>
      * Extended header fields are never generated by this method.
      * 
      * @param fieldName
      *            The heder field name
      * @param fieldValue
      *            The header field value
      * @return Formatted header line string
      */
     private byte[] printHeaderLine(final String fieldName, final String fieldValue) {
         return String.format("%s: %s" + CRLF, getPrettyHeaderFieldName(fieldName), fieldValue)
                 .getBytes();
     }
 
     /**
      * Converts uppercase header field names to header field names with correct
      * cased, pretty field names.
      * 
      * @param fieldName
      *            The header field name to pretty print
      * @return The pretty printed header field name
      */
     private String getPrettyHeaderFieldName(final String fieldName) {
         if (prettyFieldNames.containsKey(fieldName)) {
             return prettyFieldNames.get(fieldName);
         }
         return fieldName;
     }
 }
