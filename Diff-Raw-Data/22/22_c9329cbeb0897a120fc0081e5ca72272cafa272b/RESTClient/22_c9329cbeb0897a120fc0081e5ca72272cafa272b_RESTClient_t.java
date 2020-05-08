 package edu.gatech.oad.fullhouse.findmystuff.client;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gson.Gson;
 
 import edu.gatech.oad.fullhouse.findmystuff.model.Settings;
 
 /**
  * A simple client to manipulate a REST resource on a server
  * 
  * @author Jesse Rosalia
  *
  * @param <T>
  */
 public class RESTClient<T> {
 
 //    public static void main(String[] args) {
 //        //test code
 //        Settings.instance().setServerUrl("http://localhost:3000");
 //        RESTClient client = new RESTClient(User.class);
 //        client.list();
 //        User user = new User();
 //        user.setUsername("jrosalia");
 //        user.setPassword("wouldntyouliketoknow");
 //        
 //        client.create(user);
 //        
 //        client.list();
 //    }
 //
     private Class<T> objectClass;
     private String resource;
 
     public RESTClient(Class<T> objectClass) {
         this(objectClass, getDefaultResourceName(objectClass));
         
     }
 
     /**
      * Helper method to get the most likely resource name, given the class.
      * 
      * @param clazz
      * @return
      */
     private static String getDefaultResourceName(Class<?> clazz) {
         String name = clazz.getSimpleName();
         StringBuilder builder = new StringBuilder();
         for (int ii = 0; ii < name.length(); ii++) {
             char chr = name.charAt(ii);
             
             if ((chr & 0x20) == 0) {
                 if (builder.length() > 0) {
                     builder.append('_');
                 }
                 
                 builder.append((char)((int)chr | 0x20));
             } else {
                 builder.append(chr);
             }
         }
         builder.append("s");
         return builder.toString();
     }
 
     public RESTClient(Class<T> objectClass, String resource) {
         this.objectClass = objectClass;
         this.resource    = resource;
     }
 
     public T get(long id) {
         Map<String, String> params = new HashMap<String, String>();
         String json = doGet(Long.toString(id), params);
         Gson gson = new Gson();
         return gson.fromJson(json, this.objectClass);
     }
 
     public List<T> list() {
         Map<String, String> params = new HashMap<String, String>();
         String json = doGet(null, params);
         Gson gson = new Gson();
         T[] array = (T[]) Array.newInstance(objectClass, 0);
         T[] objects = (T[]) gson.fromJson(json, array.getClass());
         return Arrays.asList(objects);
     }
 
     public void create(T obj) {
         Gson gson = new Gson();
         String json = gson.toJson(obj);
         doPost("create", json);
     }
 
     public void update(T obj, long id) {
         throw new UnsupportedOperationException("NYI");
     }
 
     public void destroy(T obj) {
         throw new UnsupportedOperationException("NYI");
     }
 
     /**
      * Execute a get request against the remote server.  This uses the resource passed in to
      * (or figured out in) the constructor, and executes a query with parameters against that resource.
      * 
      * @param query
      * @param params
      * @return A string containing JSON with the requested data
      * @throws RuntimeException if there is a problem or the server returns anything other than a HTTP 200
      */
     protected String doGet(String query, Map<String, String> params) {
         HttpURLConnection connection = null;
         try {
             //construct the query
             String urlStr = Settings.instance().getServerUrl() + "/" + resource + (query != null ? ("/" + query) : "") + ".json";
             //construct the params
             String paramStr = buildParams(params);
             //put the 2 together into a URL and open the stream
             URL url = new URL(urlStr + "?" + paramStr);
             connection = (HttpURLConnection) url.openConnection();
             int responseCode = connection.getResponseCode();
             if (responseCode == 200) {
                 return inputStreamToString(connection.getInputStream());
             } else {
                 throw new IOException("Http response: " + responseCode);
             }
             // this.retries = 0;
         } catch (IOException ex) {
             // this.retries++;
             // if (this.retries >= 5) {
             throw new RuntimeException(ex);
             // }
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
     
     /**
      * Post a json block to the server.
      * 
      * @param cmd
      * @param json
      * @throws RuntimeException if there is a problem or the server returns anything other than a HTTP 200
      */
     protected void doPost(String cmd, String json) {
         HttpURLConnection connection = null;
         try {
             URL url = new URL(Settings.instance().getServerUrl() + "/" + resource);
             connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Length",
                     Integer.toString(json.length()));
             connection.setRequestProperty("Content-Type", "application/json");
             connection.getOutputStream().write(json.getBytes());
             connection.connect();
             connection.getResponseCode();
             // this.retries = 0;
         } catch (IOException ex) {
             // this.retries++;
             // if (this.retries >= 5) {
             throw new RuntimeException(ex);
             // }
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
     
     public void doPut(String query, String json) {
         HttpURLConnection connection = null;
         try {
             URL url = new URL(Settings.instance().getServerUrl() + "/" + resource + "/" + query);
             connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setRequestMethod("PUT");
             connection.setRequestProperty("Content-Length",
                     Integer.toString(json.length()));
             connection.setRequestProperty("Content-Type", "application/json");
             connection.getOutputStream().write(json.getBytes());
             connection.connect();
             connection.getResponseCode();
             // this.retries = 0;
         } catch (IOException ex) {
             // this.retries++;
             // if (this.retries >= 5) {
             throw new RuntimeException(ex);
             // }
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
     
     protected void doDelete(String query) {
         HttpURLConnection connection = null;
         try {
             URL url = new URL(Settings.instance().getServerUrl() + "/" + resource + "/" + query);
             connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
             connection.setRequestMethod("DELETE");
             connection.connect();
             connection.getResponseCode();
             // this.retries = 0;
         } catch (IOException ex) {
             // this.retries++;
             // if (this.retries >= 5) {
             throw new RuntimeException(ex);
             // }
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
     
     /**
      * Helper method to build a param string for a HTTP GET request
      * 
      * @param params
      * @param ignore
      * @return
      */
     private String buildParams(Map<String, String> params, String ... ignore) {
         Set<String> ignoreSet = new HashSet<String>(Arrays.asList(ignore));
         StringBuilder paramsBuilder = new StringBuilder();
         for (String key : params.keySet()) {
             
             if (!ignoreSet.contains(key)) {
                 String val = params.get(key);
                 if (val != null) {
                     if (paramsBuilder.length() > 0) {
                         paramsBuilder.append("&");
                     }
                     paramsBuilder.append(key).append("=").append(val);
                 }
             }
         }
         return paramsBuilder.toString();
     }
 
     private String inputStreamToString(InputStream inputStream) throws IOException {
         byte[] buffer = new byte[1024];
         int bytes = 0;
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         while ((bytes = inputStream.read(buffer)) > 0) {
             baos.write(buffer, 0, bytes);
         }
         return baos.toString();
     }
 }
