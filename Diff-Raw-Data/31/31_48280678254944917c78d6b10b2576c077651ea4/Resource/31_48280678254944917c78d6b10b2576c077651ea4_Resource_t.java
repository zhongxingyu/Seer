 package rip;
 
 import java.io.IOException;
 import java.net.*;
 
 public class Resource {
 
     public static final String MIME_TYPE = "application/json";
 
     private URL url;
     private ResponseReader reader = new ResponseReader();
     private RequestWriter writer = new RequestWriter();
 
     Resource(URL url) {
         this.url = url;
     }
 
     public Resource path(String relativePath) {
        return new Resource(createUrl(url, relativePath));
     }
 
     public String get(){
         URLConnection connection = openConnection();
         connection.setRequestProperty("Accept", MIME_TYPE);
         return reader.read(connection);
     }
 
     public void delete() {
         HttpURLConnection connection = openConnection();
         setRequestMethod(connection, "DELETE");
         reader.read(connection);
     }
 
     public void put(String contents) {
         HttpURLConnection connection = openConnection();
         connection.setRequestProperty("Content-Type", MIME_TYPE);
         setRequestMethod(connection, "PUT");
         writer.write(connection, contents);
         reader.read(connection);
     }
 
     public String post(String contents) {
         HttpURLConnection connection = openConnection();
         connection.setRequestProperty("Content-Type", MIME_TYPE);
         connection.setRequestProperty("Accept", MIME_TYPE);
         setRequestMethod(connection, "POST");
         writer.write(connection, contents);
         return reader.read(connection);
     }
 
    private URL createUrl(URL context, String relativePath) {
         try {
            context = addSlashIfNecessary(context);
            return new URL(context, relativePath);
         } catch (MalformedURLException exception) {
             throw new InvalidUrl(exception);
         }
     }
 
    private URL addSlashIfNecessary(URL context) throws MalformedURLException {
        if (!context.getPath().endsWith("/")) {
            return new URL(
                    context.getProtocol(),
                    context.getHost(),
                    context.getPort(),
                    context.getPath() + "/");
        }
        return context;
    }

     private HttpURLConnection openConnection() {
         try {
             return (HttpURLConnection) url.openConnection();
         } catch (IOException exception) {
             throw new ConnectionFailure(exception);
         }
     }
 
     private void setRequestMethod(HttpURLConnection connection, String method) {
         try {
             connection.setRequestMethod(method);
         } catch (ProtocolException shouldNotHappen) {
             throw new Error(shouldNotHappen);
         }
     }
 
     @Override
     public boolean equals(Object that) {
         return (that instanceof Resource) &&
                 url.equals(((Resource) that).url);
     }
 
     @Override
     public int hashCode() {
         return url.hashCode();
     }
 }
