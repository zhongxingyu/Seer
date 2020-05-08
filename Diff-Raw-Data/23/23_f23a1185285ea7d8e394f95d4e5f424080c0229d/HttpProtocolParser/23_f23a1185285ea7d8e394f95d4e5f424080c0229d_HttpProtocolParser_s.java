 package ch.rollis.emma;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ch.rollis.emma.request.Request;
 
 public class HttpProtocolParser {
     private final BufferedReader reader;
     private Request request;
     private int majorVersion = 0;
     private int minorVersion = 9;
 
     private static final String SP = " ";
     private static final String HT = "\t";
 
     public HttpProtocolParser(InputStream input) {
         reader = new BufferedReader(new InputStreamReader(input));
     }
 
     public Request parse() throws HttpProtocolException, IOException {
         request = new Request();
 
         parseRequestLine();
 
         if (request.isFullRequest()) {
             parseHeaders();
             parseEntity();
         }
 
         validateRequest();
 
         return request;
     }
 
     private void parseRequestLine() throws HttpProtocolException, IOException {
         String reqLine = reader.readLine();
         String[] reqParts = reqLine.split(SP);
 
         if (reqParts.length == 2) {
             // Simple-Request
             try {
                 HttpMethod method = HttpMethod.valueOf(reqParts[0]);
                 // HTTP/0.9 only allows GET requests
                 if (method.compareTo(HttpMethod.GET) != 0) {
                     throw new HttpProtocolException("Illegl method " + method + ".");
                 }
                 request.setMethod(method);
             } catch (IllegalArgumentException e) {
                 throw new HttpProtocolException("Unknown method " + reqParts[0] + ".");
             }
             majorVersion = 0;
             minorVersion = 9;
             request.setProtocol(String.format("HTTP/" + majorVersion + "." + minorVersion));
         } else if (reqParts.length == 3) {
             // Full-Request
             try {
                 HttpMethod method = HttpMethod.valueOf(reqParts[0]);
                 request.setMethod(method);
             } catch (IllegalArgumentException e) {
                 throw new HttpProtocolException("Unknown method " + reqParts[0] + ".");
             }
             Matcher m = Pattern.compile("HTTP/(\\d+).(\\d+)").matcher(reqParts[2]);
             if (m.matches()) {
                 majorVersion = Integer.parseInt(m.group(1));
                 minorVersion = Integer.parseInt(m.group(2));
                 request.setProtocol(String.format("HTTP/" + majorVersion + "." + minorVersion));
             } else {
                 throw new HttpProtocolException("Invalid protocol " + reqParts[2] + ".");
             }
         } else {
             // Bad Request
             throw new HttpProtocolException("Malformed request line.");
         }
 
         try {
             request.setRequestURI(new URI(reqParts[1]));
         } catch (URISyntaxException e) {
             throw new HttpProtocolException("Illegal URI.", e);
         }
     }
 
     private void parseHeaders() throws HttpProtocolException, IOException {
         String line = null;
         String currentHeader = null;
         String currentValue = null;
 
         while ((line = reader.readLine()) != null) {
             // do we reach end of header section of request
             if (line.equals("")) {
                 break;
             }
 
             int colonPos = line.indexOf(":");
             if (colonPos > 0) {
                 // new header found - store previous if we have one
                 if (currentHeader != null) {
                     request.setHeader(currentHeader, currentValue);
                     currentHeader = currentValue = null;
                 }
 
                 // process new current
                 currentHeader = line.substring(0, colonPos).trim();
                 currentValue = line.substring(colonPos + 1).trim();
             } else {
                 // extend header field
                 if (currentHeader == null || !(line.startsWith(SP) || line.startsWith(HT))) {
                     throw new HttpProtocolException("Invalid extended header");
                 } else {
                     currentValue = currentValue.concat(line.trim());
                 }
             }
         }
         // !store the last as it has not been stored previously!
         if (currentHeader != null) {
             request.setHeader(currentHeader, currentValue);
         }
     }
 
     public void parseEntity() throws IOException {
         String cl = request.getHeader("Content-Length");
        if (cl != null || Integer.parseInt(cl) == 0) {
            int length = Integer.parseInt(cl);
            StringBuilder sb = new StringBuilder();

            while (sb.length() < length) {
                char[] charBuffer = new char[1024];
                reader.read(charBuffer);
                sb.append(charBuffer);
            }
            request.setEntity(sb.toString());
         }
 
     }
 
 
     public void validateRequest() throws HttpProtocolException {
         if (majorVersion >= 1 && minorVersion >= 1 && request.getHeader("Host") == null) {
             throw new HttpProtocolException("No Host header present for HTTP/1.1");
         }
     }
 }
