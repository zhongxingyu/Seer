 /**
  * Copyright Adrian Hayes 2012
  * 
  * This file is part of BurpJS.
  * BurpJS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BurpJS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with BurpJS.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package burp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 
 /**
  * * 
  * @author adrian
  */
 public class HttpMessage {
 
     private boolean isRequest;
     private String url;
     private byte[] rawMessage;
     private final IBurpExtenderCallbacks burp;
     private final Context jsContext;
     private final Scriptable jsScope;
 
     public HttpMessage(Context jsContext, Scriptable jsScope, IBurpExtenderCallbacks burp, boolean isRequest, String url, byte[] rawMessage) {
         this.isRequest = isRequest;
         this.url = url;
         this.rawMessage = rawMessage;
         this.burp = burp;
         this.jsContext = jsContext;
         this.jsScope = jsScope;
     }
 
     /**
      * Does the message represent an http request or response
      */
     public boolean isRequest() {
         return isRequest;
     }
 
     /**
      * Get the raw byte array that is interpreted as an HTTP message
      */
     public byte[] getRawMessage() {
         return rawMessage;
     }
     
     /**
      * Get the string representation of the URL this message is being sent to or received from
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * Get the HTTP headers, including the first header which will be something like "GET / HTTP/1.1" or "HTTP/1.1 200 OK".
      * @throws Exception 
      */
     public Scriptable getHeaders() throws Exception {
         return jsContext.newObject(jsScope, "Array", burp.getHeaders(rawMessage));
     }
     
     /**
      * Set the HTTP headers, including the first header which will be something like "GET / HTTP/1.1" or "HTTP/1.1 200 OK".
      * @param headers String array of new headers
      * @throws UnsupportedEncodingException 
      */
     public void setHeaders(String[] headers) throws UnsupportedEncodingException {
         
         StringBuilder builder = new StringBuilder();
         for (String header : headers) {
             builder.append(header);
             builder.append("\r\n");
         }
         
         builder.append("\r\n");
         
         byte[] rawHeaders = builder.toString().getBytes("ASCII");
         byte[] rawBody = isolateBody(rawMessage);
         
         byte[] newRawMessage = new byte[rawHeaders.length + rawBody.length];
         System.arraycopy(rawHeaders, 0, newRawMessage, 0, rawHeaders.length);
         System.arraycopy(rawBody, 0, newRawMessage, rawHeaders.length, rawBody.length);
         
         rawMessage = newRawMessage;
     }
     
     /**
      * Appends a header to the bottom of the HTTP header block of this message
      * @param key the name of the header. Eg: Cookie
      * @param value the value of the header. Eg: sessionid=12345678
      * @throws Exception 
      */
     public void appendHeader(String key, String value) throws Exception {
         
         String[] oldHeaders = burp.getHeaders(rawMessage);
         String[] newHeaders = new String[oldHeaders.length + 1];
         
         System.arraycopy(oldHeaders, 0, newHeaders, 0, oldHeaders.length);
         newHeaders[newHeaders.length - 1] = key + ": " + value;
                 
         setHeaders(newHeaders);         
              
     }
     
     /**
      * Get the first instance of matching header.
      * Use 'getHeaders' if multi instances of header are required, ie two cookies set with 'Set-Cookie'
      * @param key The header key to match. Eg: Cookie
      * @return The first matching header, or an empty string if none are found.
      */
     public String getHeader(String key) throws Exception {
         
         String[] headers = burp.getHeaders(rawMessage);
         
         for (String header : headers) 
             if (header.startsWith(key+":"))
                     return header.substring(header.indexOf(": ") + 2);
         
         return "";
     }
     
     /**
      * Removes all matching headers from this message
      * @param key Name of the headers to remove. Eg: Cookie
      * @throws Exception 
      */
     public void removeHeaders(String key) throws Exception {
         
         String[] oldHeaders = burp.getHeaders(rawMessage);
         List<String> newHeaders = new ArrayList<String>(oldHeaders.length);
         for (String header : oldHeaders) 
             if (!header.startsWith(key+":"))
                 newHeaders.add(header);
         
         String[] headerArr = new String[newHeaders.size()];
         setHeaders(newHeaders.toArray(headerArr));
         
     }
     
     /**
      * Removes all headers matching key, then appends a new header.
      * @param key The header to remove and the name of the new header
      * @param value The value of the new header
      * @throws Exception 
      */
     public void replaceHeaders(String key, String value) throws Exception {
         removeHeaders(key);
         appendHeader(key, value);
     }
     
     
     /**
      * Get the body of the message, which is everything after the headers
      * @return A string representing the message body, decoded as UTF-8
      * @throws UnsupportedEncodingException 
      */
     public String getBody() throws UnsupportedEncodingException {
         return new String(isolateBody(rawMessage), "UTF-8");
     }
     
     /**
      * Set the body of the message, which is everything after the headers and whitespace separator.
      * Any Content-Length header (if present) is automatically updated.
      */
     public void setBody(String body) throws Exception {
         
         String[] headers = burp.getHeaders(rawMessage);
         StringBuilder message = new StringBuilder();
         for (String header : headers) {
             
             //auto update content length
             if (header.startsWith("Content-Length:"))
                 header = "Content-Length: " + body.getBytes("UTF-8").length;
             
             message.append(header);
             message.append("\r\n");
         }
         
         message.append("\r\n");
         message.append(body);
         
         setMessage(message.toString());
         
     }
 
     /**
      * Get the the entire message as a String
      * @return A string representing the entire message, decoded as UTF-8
      * @throws UnsupportedEncodingException 
      */
     public String getMessage() throws UnsupportedEncodingException {
         return new String(rawMessage, "UTF-8");
     }
     
     /**
      * Get the HTTP request verb used. Eg: GET, POST, HEAD etc.
      * 
      * This can only be used on request messages.
      * @return String representation of the verb
      * @throws Exception 
      */
     public String getVerb() throws Exception {
         if (!isRequest)
             throw new RuntimeException("Cannot get verb on HTTP response");
         
         String reqLine = burp.getHeaders(rawMessage)[0];
         return reqLine.substring(0, reqLine.indexOf(' '));
     }
     
     /**
      * Set the entire message content. Headers and all.
      * @param message String representation of the entire message content.
      * @throws UnsupportedEncodingException 
      */
     public void setMessage(String message) throws UnsupportedEncodingException {
         rawMessage = message.getBytes("UTF-8");
     }
 
     /**
      * Get HTTP parameters
      * 
     * @see <a href="http://portswigger.net/burp/extender/burp/IBurpExtenderCallbacks.html#getParameters(byte[])">IBurpExtenderCallbacks</a>
      */
     public String[][] getParameters() throws Exception {
         return burp.getParameters(rawMessage);
     }
 
     @Override
     public String toString() {
         return "HttpMessage{" + "isRequest=" + isRequest + ", url=" + url + " }";
     }
     
     /**
      * Dump this message's raw bytes to a file. If the file exists, this message is appended.
      * @param filePath Path to the file to write to.
      * @throws IOException 
      */
     public void dumpToFile(String filePath) throws IOException {
         dumpToFile(filePath, null);
     }
 
     /**
      * Dump this message's raw bytes to a file. If the file exists, this message is appended.
      * 
      * The optional appendString argument is available if you want to separate messages in a single file, with a few line breaks for example.
      * @param filePath Path to the file to write to.
      * @param appendString A string to appendString to the message as it is written to the file.
      * @throws IOException 
      */
     public void dumpToFile(String filePath, String appendString) throws IOException {
         
         FileOutputStream out = null;
         try {
             out = new FileOutputStream(new File(filePath), true);
             out.write(getRawMessage()); 
             
             if (appendString != null)
                 out.write(appendString.getBytes());
             
         } finally {
             if (out != null)
                 out.close();
         }   
         
     }
     
     
     private byte[] isolateBody(byte[] rawMessage) throws UnsupportedEncodingException {
         
         String message = new String(rawMessage, "ASCII");
         
         int idx = message.indexOf("\r\n\r\n");
         if (idx < 0)
             return new byte[0];
         
         return Arrays.copyOfRange(rawMessage, idx+4, rawMessage.length);
     }
    
 }
