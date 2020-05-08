 package eu.cassandra.training.utils;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.net.ssl.SSLContext;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.util.EntityUtils;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 public class APIUtilities
 {
 
   static {
     // for localhost testing only
     javax.net.ssl.HttpsURLConnection
             .setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
 
               public boolean verify (String hostname,
                                      javax.net.ssl.SSLSession sslSession)
               {
                 return true;
               }
             });
   }
 
   private static String userID = "";
   private static String url;
   private static DefaultHttpClient httpclient = new DefaultHttpClient();
   private static SSLSocketFactory sf = null;
   private static SSLContext sslContext = null;
 
   // Add AuthCache to the execution context
   private static BasicHttpContext localcontext = new BasicHttpContext();
 
   public static void setUrl (String URLString) throws MalformedURLException
   {
     url = URLString;
   }
 
   public static String getUserID ()
   {
     return userID;
   }
 
   public static String sendEntity (String message, String suffix)
     throws IOException, AuthenticationException, NoSuchAlgorithmException
   {
 
     System.out.println(message);
     HttpPost httppost = new HttpPost(url + suffix);
 
     StringEntity entity = new StringEntity(message, "UTF-8");
     entity.setContentType("application/json");
     httppost.setEntity(entity);
     System.out.println("executing request: " + httppost.getRequestLine());
 
     HttpResponse response = httpclient.execute(httppost, localcontext);
     HttpEntity responseEntity = response.getEntity();
     String responseString = EntityUtils.toString(responseEntity, "UTF-8");
     System.out.println(responseString);
 
     DBObject dbo = (DBObject) JSON.parse(responseString);
 
     DBObject dataObj = (DBObject) dbo.get("data");
 
     return dataObj.get("_id").toString();
 
   }
 
   public static String updateEntity (String message, String suffix, String id)
     throws IOException, AuthenticationException, NoSuchAlgorithmException
   {
 
     System.out.println(message);
     HttpPut httpput = new HttpPut(url + suffix + "/" + id);
 
     StringEntity entity = new StringEntity(message, "UTF-8");
     entity.setContentType("application/json");
     httpput.setEntity(entity);
     System.out.println("executing request: " + httpput.getRequestLine());
 
     HttpResponse response = httpclient.execute(httpput, localcontext);
     HttpEntity responseEntity = response.getEntity();
     String responseString = EntityUtils.toString(responseEntity, "UTF-8");
     System.out.println(responseString);
 
     return "Done";
 
   }
 
   public static boolean sendUserCredentials (String username, char[] password)
     throws IOException, NoSuchAlgorithmException, AuthenticationException
   {
 
     try {
       sslContext = SSLContext.getInstance("TLS");
       sslContext.init(null, null, null);
       sf =
         new SSLSocketFactory(sslContext,
                              SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
     }
     catch (Exception e1) {
     }
 
     Scheme scheme = new Scheme("https", 8443, sf);
     httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
 
     String pass = String.valueOf(password);
 
     try {
       UsernamePasswordCredentials usernamePasswordCredentials =
         new UsernamePasswordCredentials(username, pass);
 
       HttpGet httpget = new HttpGet(url + "/usr");
       httpget.addHeader(new BasicScheme()
               .authenticate(usernamePasswordCredentials, httpget, localcontext));
 
       System.out.println("executing request: " + httpget.getRequestLine());
 
       HttpResponse response = httpclient.execute(httpget, localcontext);
       HttpEntity entity = response.getEntity();
       String responseString = EntityUtils.toString(entity, "UTF-8");
       System.out.println(responseString);
 
       DBObject dbo = (DBObject) JSON.parse(responseString);
 
      if (dbo.get("success") == "true") {
 
         BasicDBList dataObj = (BasicDBList) dbo.get("data");
 
         DBObject dbo2 = (DBObject) dataObj.get(0);
 
         userID = dbo2.get("usr_id").toString();
 
         System.out.println("userId: " + userID);
 
         return true;
       }
       else {
         System.out.println(false);
         return false;
       }
 
     }
     finally {
     }
 
   }
 
   public static boolean getUserID (String username, char[] password)
     throws Exception
   {
     return sendUserCredentials(username, password);
   }
 
 }
