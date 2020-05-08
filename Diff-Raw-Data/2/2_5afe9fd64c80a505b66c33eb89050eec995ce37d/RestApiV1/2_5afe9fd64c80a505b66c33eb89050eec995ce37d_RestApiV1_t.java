 package com.allplayers.rest;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.HttpURLConnection;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 public class RestApiV1 {
     private static String sCurrentUserUUID = "";
     private static CookieHandler sCookieHandler = new CookieManager();
 
     public RestApiV1() {
         // Create a trust manager that does not validate certificate chains
         TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                 public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                     return null;
                 }
 
                 public void checkClientTrusted(
                 java.security.cert.X509Certificate[] certs, String authType) {
                 }
 
                 public void checkServerTrusted(
                 java.security.cert.X509Certificate[] certs, String authType) {
                 }
             }
         };
 
         // Install the all-trusting trust manager
         try {
             SSLContext sc = SSLContext.getInstance("SSL");
             sc.init(null, trustAllCerts, new java.security.SecureRandom());
             HttpsURLConnection
             .setDefaultSSLSocketFactory(sc.getSocketFactory());
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/constructor/" + ex);
         }
 
         // Install CookieHandler
         CookieHandler.setDefault(sCookieHandler);
     }
 
     public static boolean isLoggedIn() {
         if (sCurrentUserUUID.equals("")) {
             return false;
         }
 
         // Check an authorized call
         try {
             URL url = new URL(
                 "https://www.allplayers.com/?q=api/v1/rest/users/"
                 + sCurrentUserUUID + ".json");
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             BufferedReader input = new BufferedReader(new InputStreamReader(
                         inStream));
 
             String line = "";
 
             String result = "";
 
             while ((line = input.readLine()) != null) {
                 result += line;
             }
 
             JSONObject jsonResult = new JSONObject(result);
             String retrievedUUID = jsonResult.getString("uuid");
 
             if (retrievedUUID.equals(sCurrentUserUUID)) {
                 return true;
             } else { // This case should not occur
                 return false;
             }
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/isLoggedIn/" + ex);
             return false;
         }
     }
 
     public static String deleteMessage(int threadId, String type) {
         // String[][] contents = new String[1][2];
         // Type: thread or message (default = thread)
 
         return makeAuthenticatedDelete("https://www.allplayers.com/?q=api/v1/rest/messages/"
                                        + threadId + ".json");
     }
 
     // Change read/unread status
     public static String putMessage(int threadId, int status, String type) {
         String[][] contents = new String[1][2];
         // Status: 1=unread, 0=read
         contents[0][0] = "status";
         contents[0][1] = "" + status;
         // Type: thread or message (default = thread)
 
         return makeAuthenticatedPut(
                    "https://www.allplayers.com/?q=api/v1/rest/messages/"
                    + threadId + ".json", contents);
     }
 
     public String validateLogin(String username, String password) {
         String[][] contents = new String[2][2];
         contents[0][0] = "username";
         contents[0][1] = username;
         contents[1][0] = "password";
         contents[1][1] = password;
 
         return makeAuthenticatedPost(
                    "https://www.allplayers.com/?q=api/v1/rest/users/login.json",
                    contents);
     }
 
     public static String postMessage(int threadId, String body) {
         String[][] contents = new String[2][2];
         contents[0][0] = "thread_id";
         contents[0][1] = "" + threadId;
         contents[1][0] = "body";
         contents[1][1] = body;
 
         return makeAuthenticatedPost(
                    "https://www.allplayers.com/?q=api/v1/rest/messages.json",
                    contents);
     }
 
     public static String searchGroups(String search, int zipcode, int distance) {
         String searchTerms = "https://www.allplayers.com/?q=api/v1/rest/groups.json";
             if(search.length() != 0) {
                 searchTerms += ("&search=\"" + search + "\"");
             }
            // As of right now, the input distance will only matter if a zipcode is given,
             // so it is only considered in that case.
             // TODO Add in considering the distance as "Distance from my location"
             if(zipcode != 0) {
                 searchTerms += ("&distance[postal_code]=" + zipcode
                                 + "&distance[search_distance]="
                                 + distance
                                 + "&distance[search_units]=mile");
             }
         return makeUnauthenticatedGet(searchTerms);
     }
 
     public static String getUserGroups() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/"
                                     + sCurrentUserUUID + "/groups.json");
     }
 
     public static String getUserFriends() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/"
                                     + sCurrentUserUUID + "/friends.json");
     }
 
     public static String getUserGroupmates() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/"
                                     + sCurrentUserUUID + "/groupmates.json");
     }
 
     public static String getUserEvents() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/"
                                     + sCurrentUserUUID + "/events/upcoming.json");
     }
 
     public static String getGroupInformationByGroupId(String group_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/"
                                     + group_uuid + ".json");
     }
 
     public static String getGroupAlbumsByGroupId(String group_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/"
                                     + group_uuid + "/albums.json");
     }
 
     public static String getGroupEventsByGroupId(String group_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/"
                                     + group_uuid + "/events/upcoming.json");
     }
 
     public static String getGroupMembersByGroupId(String group_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/"
                                     + group_uuid + "/members.json");
     }
 
     public static String getGroupPhotosByGroupId(String group_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/photos.json");
     }
 
     public static String getAlbumByAlbumId(String album_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/albums/"
                                     + album_uuid + ".json");
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/albums/"
                                     + album_uuid + "/photos.json");
     }
 
     public static String getPhotoByPhotoId(String photo_uuid) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/photos/"
                                     + photo_uuid + ".json");
     }
 
     public static String getUserInbox() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages.json&box=inbox");
     }
 
     public static String getUserSentBox() {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages.json&box=sent");
     }
 
     public static String getUserMessagesByThreadId(String thread_id) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages/"
                                     + thread_id + ".json");
     }
 
     public static String getEventByEventId(String event_id) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/events/"
                                     + event_id + ".json");
     }
 
     public static String getUserResourceByResourceId(String resource_id) {
         return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/resources/"
                                     + resource_id + ".json");
     }
 
     private static String makeAuthenticatedGet(String urlString) {
         if (!isLoggedIn()) {
             return "You are not logged in";
         }
 
         // Make and return from authenticated get call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             BufferedReader input = new BufferedReader(new InputStreamReader(
                         inStream));
 
             String line = "";
             String result = "";
             while ((line = input.readLine()) != null) {
                 result += line;
             }
 
             return result;
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeAuthenticatedGet/" + ex);
             return "error";
         }
     }
 
     private static String makeAuthenticatedDelete(String urlString) {
         if (!isLoggedIn()) {
             return "You are not logged in";
         }
 
         // Make and return from authenticated delete call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
 
             connection.setDoOutput(true);
             connection.setRequestMethod("DELETE");
             connection.setRequestProperty("Content-Type",
                                           "application/x-www-form-urlencoded");
 
             return "done";
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeAuthenticatedDelete/"
                                + ex);
             return ex.toString();
         }
     }
 
     private static String makeAuthenticatedPut(String urlString,
             String[][] contents) {
         if (!isLoggedIn()) {
             return "You are not logged in";
         }
 
         // Make and return from authenticated put call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
 
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setRequestMethod("PUT");
             connection.setRequestProperty("Content-Type",
                                           "application/x-www-form-urlencoded");
 
             DataOutputStream printout = new DataOutputStream(
                 connection.getOutputStream());
 
             // Send PUT output.
             String content = "";
             if (contents.length > 0) {
                 for (int i = 0; i < contents.length; i++) {
                     if (i > 0) {
                         content += "&";
                     }
 
                     content += contents[i][0] + "="
                                + URLEncoder.encode(contents[i][1], "UTF-8");
                 }
             }
 
             printout.writeBytes(content);
             printout.flush();
             printout.close();
 
             return "done";
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeAuthenticatedPut/" + ex);
             return ex.toString();
         }
     }
 
     private static String makeUnauthenticatedGet(String urlString) {
         // Make and return from unauthenticated get call
         try {
             URL url = new URL(urlString);
             URLConnection connection = url.openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             BufferedReader input = new BufferedReader(new InputStreamReader(
                         inStream));
 
             String line = "";
             String result = "";
             while ((line = input.readLine()) != null) {
                 result += line;
             }
 
             return result;
         } catch (Exception ex) {
             System.err
             .println("APCI_RestServices/makeUnauthenticatedGet/" + ex);
             return ex.toString();
         }
     }
 
     private static String makeAuthenticatedPost(String urlString,
             String[][] contents) {
         // Make and return from authenticated post call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
 
             connection.setDoInput(true);
             connection.setDoOutput(true);
             connection.setUseCaches(false);
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type",
                                           "application/x-www-form-urlencoded");
 
             // If not logging in, set the cookies in the header
             if (!urlString
                     .equals("https://www.allplayers.com/?q=api/v1/rest/users/login.json")) {
                 if (!isLoggedIn()) {
                     return "You are not logged in";
                 }
             }
 
             DataOutputStream printout = new DataOutputStream(
                 connection.getOutputStream());
 
             // Send POST output.
             String content = "";
             if (contents.length > 0) {
                 for (int i = 0; i < contents.length; i++) {
                     if (i > 0) {
                         content += "&";
                     }
 
                     content += contents[i][0] + "="
                                + URLEncoder.encode(contents[i][1], "UTF-8");
                 }
             }
 
             printout.writeBytes(content);
             printout.flush();
             printout.close();
 
             // Get response data.
             BufferedReader input = new BufferedReader(new InputStreamReader(
                         connection.getInputStream()));
             String str;
 
             String result = "";
             while ((str = input.readLine()) != null) {
                 result += str;
             }
 
             input.close();
 
             return result;
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeAuthenticatedPost/" + ex);
             return ex.toString();
         }
     }
 
     public static void logOut() {
         ((CookieManager) CookieHandler.getDefault()).getCookieStore().removeAll();
         sCurrentUserUUID = "";
     }
 
     /**
      * Get a Bitmap from a URL.
      *
      * TODO - Use same connection and cookies as REST requests.
      */
     public static Bitmap getRemoteImage(final String urlString) {
         try {
             HttpGet httpRequest = null;
 
             try {
                 httpRequest = new HttpGet(new URL(urlString).toURI());
             } catch (URISyntaxException ex) {
                 System.err.println("RestApiV1/getRemoteImage/" + ex);
             }
 
             HttpClient httpclient = new DefaultHttpClient();
             HttpResponse response = httpclient.execute(httpRequest);
             HttpEntity entity = response.getEntity();
             BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
             InputStream instream = bufHttpEntity.getContent();
             return BitmapFactory.decodeStream(instream);
         } catch (IOException ex) {
             System.err.println("RestApiV1/getRemoteImage/" + ex);
         }
 
         return null;
     }
 
     public void setCurrentUserUUID(String uuid) {
         sCurrentUserUUID = uuid;
     }
 
     public static String getCurrentUserUUID() {
         return sCurrentUserUUID;
     }
 }
