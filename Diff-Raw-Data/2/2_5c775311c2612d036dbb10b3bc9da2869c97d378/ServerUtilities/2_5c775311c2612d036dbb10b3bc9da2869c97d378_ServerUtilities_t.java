 package com.omgren.apps.smsgcm.client;
 
 import android.content.Context;
 import android.util.Log;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.gson.Gson;
 import com.omgren.apps.smsgcm.common.SmsMessageDummy;
 import java.io.BufferedReader;
 import java.io.UnsupportedEncodingException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyStore;
 import java.security.SecureRandom;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManagerFactory;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.displayMessage;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.SERVER_URL;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.TAG;
 import static java.net.URLEncoder.encode;
 
 /**
  * Helper class used to communicate with the demo server.
  */
 public final class ServerUtilities {
 
   private static final int MAX_ATTEMPTS = 5;
   private static final int BACKOFF_MILLI_SECONDS = 2000;
   private static final Random random = new Random();
 
   /**
    * Register this account/device pair within the server.
    *
    * @param context
    * @param regId
    *
    * @return whether the registration succeeded or not.
    */
   public static boolean register(final Context context, final String regId) {
     Log.i(TAG, "registering device (regId = " + regId + ")");
     String serverUrl = SERVER_URL + "/register";
     Map<String, String> params = new HashMap<String, String>();
     params.put("regId", regId);
     long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
     // Once GCM returns a registration id, we need to register it in the
     // demo server. As the server might be down, we will retry it a couple
     // times.
     for (int i = 1; i <= MAX_ATTEMPTS; i++) {
       Log.d(TAG, "Attempt #" + i + " to register");
       try {
         displayMessage(context, context.getString(
               R.string.server_registering, i, MAX_ATTEMPTS));
         post(context, serverUrl, params);
         GCMRegistrar.setRegisteredOnServer(context, true);
         String message = context.getString(R.string.server_registered);
         CommonUtilities.displayMessage(context, message);
         return true;
       } catch (IOException e) {
         // Here we are simplifying and retrying on any error; in a real
         // application, it should retry only on unrecoverable errors
         // (like HTTP error code 503).
         Log.e(TAG, "Failed to register on attempt " + i, e);
         if (i == MAX_ATTEMPTS) {
           break;
         }
         try {
           Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
           Thread.sleep(backoff);
         } catch (InterruptedException e1) {
           // Activity finished before we complete - exit.
           Log.d(TAG, "Thread interrupted: abort remaining retries!");
           Thread.currentThread().interrupt();
           return false;
         }
         // increase backoff exponentially
         backoff *= 2;
       }
     }
     String message = context.getString(R.string.server_register_error,
         MAX_ATTEMPTS);
     CommonUtilities.displayMessage(context, message);
     return false;
   }
 
   /**
    * Unregister this account/device pair within the server.
    *
    * @param context
    * @param regId
    */
   public static void unregister(final Context context, final String regId) {
     Log.i(TAG, "unregistering device (regId = " + regId + ")");
     String serverUrl = SERVER_URL + "/unregister";
     Map<String, String> params = new HashMap<String, String>();
     params.put("regId", regId);
     try {
       post(context, serverUrl, params);
       GCMRegistrar.setRegisteredOnServer(context, false);
       String message = context.getString(R.string.server_unregistered);
       CommonUtilities.displayMessage(context, message);
     } catch (IOException e) {
       // At this point the device is unregistered from GCM, but still
       // registered in the server.
       // We could try to unregister again, but it is not necessary:
       // if the server tries to send a message to the device, it will get
       // a "NotRegistered" error message and should unregister the device.
       String message = context.getString(R.string.server_unregister_error,
           e.getMessage());
       CommonUtilities.displayMessage(context, message);
     }
   }
 
   /**
    * Issue a POST request to the server.
    *
    * @param endpoint POST address.
    * @param params request parameters.
    *
    * @throws IOException propagated from POST.
    */
   private static void post(final Context context, String endpoint, Map<String, String> params) throws IOException {
     byte[] bytes = makeQueryString(params);
     HttpsURLConnection conn = null;
     try {
       conn = urlConnect(context, endpoint);
       conn.setDoOutput(true);
       conn.setUseCaches(false);
       conn.setFixedLengthStreamingMode(bytes.length);
       conn.setRequestMethod("POST");
       conn.setRequestProperty("Content-Type",
           "application/x-www-form-urlencoded;charset=UTF-8");
       // post the request
       OutputStream out = conn.getOutputStream();
       out.write(bytes);
       out.close();
       // handle the response
       int status = conn.getResponseCode();
       if (status != 200) {
         throw new IOException("Post failed with error code " + status);
       }
     } finally {
       if (conn != null) {
         conn.disconnect();
       }
     }
   }
 
   /**
    * Something to make a query string quickly.
    *
    * @param params A map of parameters
    *
    * @return a byte array of the query string
    */
   private static byte[] makeQueryString(Map<String, String> params){
     StringBuilder bodyBuilder = new StringBuilder();
     Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
     // constructs the POST body using the parameters
     while (iterator.hasNext()) {
       Entry<String, String> param = iterator.next();
       try {
         bodyBuilder.append(encode(param.getKey(), "UTF-8")).append('=')
           .append(encode(param.getValue(), "UTF-8"));
       } catch (UnsupportedEncodingException e) {
         Log.e(TAG, "problem with encoding" + e);
       }
       if (iterator.hasNext()) {
         bodyBuilder.append('&');
       }
     }
     String body = bodyBuilder.toString();
     return body.getBytes();
   }
 
   /**
    * Opens a HttpURLConnection object from the URL
    *
    * @param endpoint A url to connect to
    *
    * @return an HttpURLConnection object
    *
    * @throws IOException
    */
   private static HttpsURLConnection urlConnect(final Context context, String endpoint) throws IOException {
     URL url;
     SSLContext sslContext = null;
     try {
       url = new URL(endpoint);
 
       InputStream truststoreLocation = context.getResources().openRawResource(R.raw.trust_store);
       InputStream keystoreLocation = context.getResources().openRawResource(R.raw.key_store);
 
       KeyStore truststore = KeyStore.getInstance("BKS");
       truststore.load(truststoreLocation, "blahblah".toCharArray());
 
       KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(keystoreLocation, "smsgcm".toCharArray());
 
       TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
       tmf.init(truststore);
 
       KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
       kmf.init(keystore, "".toCharArray());
 
       sslContext = SSLContext.getInstance("TLS");
       sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
 
     } catch (MalformedURLException e) {
       throw new IllegalArgumentException("invalid url: " + endpoint);
     } catch (Exception e) {
       throw new IOException("bad ssl stuff: " + e);
     }
 
     HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
 
     if( sslContext != null )
       conn.setSSLSocketFactory(sslContext.getSocketFactory());
 
     return conn;
   }
 
   /**
    * Downloads HTTP content.
    *
    * @param url URL address.
    *
    * @return content of url address.
    */
   private static String get(final Context context, String url){
     BufferedReader buf = null;
     try {
       HttpsURLConnection conn = urlConnect(context, url);
       buf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
       int status = conn.getResponseCode();
       if (status != 200) {
         throw new IOException("Get failed with error code " + status);
       }
 
       StringBuffer sb = new StringBuffer();
       String buffer;
       while((buffer = buf.readLine()) != null)
         sb.append(buffer);
 
       conn.disconnect();
       buf.close();
 
       return sb.toString();
 
     } catch (IOException e){
       Log.e(TAG, "IOException: " + e);
     } finally {
     }
 
     return null;
   }
 
   /**
    * Downloads messages queued by server to be sent over SMS.
    *
    * @return list of messages.
    */
   public static SmsMessageDummy[] downloadMessages(final Context context){
     String contents = get(context, SERVER_URL + "/messages");
     Log.i(TAG, "downloaded messages: " + contents);
 
     SmsMessageDummy[] derps = (new Gson()).fromJson(contents, SmsMessageDummy[].class);
 
     return derps;
   }
 
   /**
    * Uploads messages to server received by phone's SMS.
    *
    * @param msg message received by phone
    */
   public static void uploadMessage(final Context context, SmsMessageDummy msg){
     // post uses a HashMap
     Map<String, String> args = new HashMap<String, String>();
     args.put("name", msg.name);
     args.put("address", msg.address);
     args.put("message", msg.message);
     args.put("time", msg.time.toString());
 
     try {
       post(context, SERVER_URL + "/receiveMessage", args);
     } catch(IOException e){
       Log.e(TAG, "Error uploading message: " + e);
     }
   }
 }
