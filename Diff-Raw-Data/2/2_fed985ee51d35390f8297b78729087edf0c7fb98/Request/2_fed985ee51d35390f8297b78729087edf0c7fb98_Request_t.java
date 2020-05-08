 package caja.jcastulo.shout.http;
 
 import caja.jcastulo.shout.IllegalRequestException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Represents a client request
  * 
  * @author Carlos Juarez
  */
 public class Request {
 
     /**
      * Regular expression to find the mount point in the request
      */
     private static final Pattern getRegexp = Pattern.compile("GET\\s+([^\\s]+).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     
    private static final Pattern agentRegexp = Pattern.compile("User-Agent:\\s*(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     
     private static final Pattern icyMetadataRegexp = Pattern.compile("Icy-MetaData:\\s*(1).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     
     
 
     /**
      * Types of requests
      */
     public enum Type {
         HTTP
     }
     
     /**
      * The raw string request
      */
     private final String rawRequest;
     
     /**
      * Path that is used as the mount point
      */
     private String path;
     
     /**
      * Type of request
      */
     private Type type;
     
     private String userAgent;
     
     private boolean metadataRequested;
 
     /**
      * Constructs an instance of <code>Request</code> class
      * 
      * @param request - the raw request
      * @throws IllegalRequestException 
      */
     public Request(String request) throws IllegalRequestException {
         this.rawRequest = request.trim();
         processRequest();
     }
 
     /**
      * Parses and retrieve some properties from the raw request
      * 
      * @throws IllegalRequestException 
      */
     private void processRequest() throws IllegalRequestException {
         String[]lines = rawRequest.split("\\r?\\n");
         Matcher matcher;
         type = Type.HTTP;
         for(String line: lines){
             line = line.trim();
             if(line.startsWith("GET")){
                 matcher = getRegexp.matcher(line);
                 if (matcher.matches()) {
                     path = matcher.group(1);
                 }
             }else if(line.startsWith("User-Agent:")){
                 matcher = agentRegexp.matcher(line);
                 if (matcher.matches()) {
                     userAgent = matcher.group(1);
                 }
             }else if(line.startsWith("Icy-MetaData:")){
                 matcher = icyMetadataRegexp.matcher(line);
                 if(matcher.matches()){
                     metadataRequested = true;
                 }
             }
         }
         if(path==null){
             throw new IllegalRequestException("Illegal request path was not found [" + rawRequest + "]");
         }
         if(userAgent==null){
             throw new IllegalRequestException("Illegal request user agent was not found [" + rawRequest + "]");
         }
     }
 
     /**
      * @return the raw request
      */
     public String getRawRequest() {
         return rawRequest;
     }
 
     /**
      * @return the path
      */
     public String getPath() {
         return path;
     }
 
     /**
      * @return the type
      */
     public Type getType() {
         return type;
     }
 
     /**
      * @return user agent
      */
     public String getUserAgent(){
         return userAgent;
     }
     
     public boolean isBrowserUserAgent(){
         if(userAgent.contains("Mozilla/5.0")){
             return true;
         }
         return false;
     }
     public boolean isMetadataRequested(){
         return metadataRequested;
     }
     
     @Override
     public String toString() {
         return "Request{" + "path=" + path + ", type=" + type + ", browserUserAgent=" + isBrowserUserAgent() +
                 ", metadataRequested=" + metadataRequested + ", userAgent=" + userAgent + '}';
     }
     
 }
