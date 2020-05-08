 package com.allplayers.rest;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.HttpCookie;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.List;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class RestApiV1 {
    private static String endpoint = "https://www.allplayers.com/?q=api/v1/rest/";
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
         if (sCurrentUserUUID.equals("") || sCurrentUserUUID.equals(null)) {
             logOut();
             return false;
         }
 
         // Check an authorized call
         try {
             URL url = new URL(endpoint + "users/" + sCurrentUserUUID + ".json");
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
 
         return makeAuthenticatedDelete(endpoint + "messages/" + threadId + ".json");
     }
 
     // Change read/unread status
     public static String putMessage(int threadId, int status, String type) {
         String[][] contents = new String[2][2];
         // Status: 1=unread, 0=read
         contents[0][0] = "status";
         contents[0][1] = "" + status;
         // Type: thread or msg (default = thread)
         contents[1][0] = "type";
         contents[1][1] = type;
         return makeAuthenticatedPut(endpoint + "messages/" + threadId + ".json", contents);
     }
 
     public String validateLogin(String username, String password) {
         String[][] contents = new String[2][2];
         contents[0][0] = "username";
         contents[0][1] = username;
         contents[1][0] = "password";
         contents[1][1] = password;
 
         return makeAuthenticatedPost(endpoint + "users/login.json", contents);
     }
 
     public static String postMessage(int threadId, String body) {
         String[][] contents = new String[2][2];
         contents[0][0] = "thread_id";
         contents[0][1] = "" + threadId;
         contents[1][0] = "body";
         contents[1][1] = body;
 
         return makeAuthenticatedPost(endpoint + "messages.json", contents);
     }
 
     public static String createNewMessage(String[] uuids, String subject, String body) {
         String[][] contents = new String[uuids.length + 2][2];
         for (int i = 0; i < uuids.length; i++) {
             contents[i][0] = "recipients[" + i + "]";
             contents[i][1] = uuids[i];
         }
         contents[uuids.length][0] = "subject";
         contents[uuids.length][1] = subject;
         contents[uuids.length + 1][0] = "body";
         contents[uuids.length + 1][1] = body;
         return makeAuthenticatedPost(endpoint + "messages.json", contents);
     }
 
     public static String searchGroups(String search, int zipcode, int distance) {
         String searchTerms = endpoint + "groups.json";
         if (search.length() != 0) {
             searchTerms += ("&search=\"" + search + "\"");
         }
         // As of right now, the input distance will only matter if a zipcode is given,
         // so it is only considered in that case.
         // TODO Add in considering the distance as "Distance from my location"
         if (zipcode != 0) {
             searchTerms += ("&distance[postal_code]=" + zipcode
                             + "&distance[search_distance]="
                             + distance
                             + "&distance[search_units]=mile");
         }
         return makeUnauthenticatedGet(searchTerms);
     }
 
     public static String getUserGroups() {
         return makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/groups.json");
     }
 
     public static String getUserGroups(int offset) {
         return makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/groups.json&offset=" + offset);
     }
 
     public static String getUserGroups(int offset, int limit) {
         return makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/groups.json&offset=" + offset + "&limit=" + limit);
     }
 
     public static String getUserFriends() {
         String jsonResult = makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/friends.json");
         return jsonResult.replaceAll("&#039;", "'");
     }
 
     public static String getUserGroupmates() {
         return makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/groupmates.json");
     }
 
     public static String getUserEvents() {
         return makeAuthenticatedGet(endpoint + "users/" + sCurrentUserUUID + "/events/upcoming.json");
     }
 
     public static String getGroupInformationByGroupId(String group_uuid) {
         return makeAuthenticatedGet(endpoint + "groups/" + group_uuid + ".json");
     }
 
     public static String getGroupAlbumsByGroupId(String group_uuid) {
         return makeAuthenticatedGet(endpoint + "groups/" + group_uuid + "/albums.json");
     }
 
     public static String getGroupEventsByGroupId(String group_uuid) {
         return makeAuthenticatedGet(endpoint + "groups/" + group_uuid + "/events/upcoming.json");
     }
 
     public static String getGroupMembersByGroupId(String group_uuid) {
         return makeAuthenticatedGet(endpoint + "groups/" + group_uuid + "/members.json");
     }
 
     public static String getGroupPhotosByGroupId(String group_uuid) {
         return makeAuthenticatedGet(endpoint + "groups/photos.json");
     }
 
     public static String getAlbumByAlbumId(String album_uuid) {
         return makeAuthenticatedGet(endpoint + "albums/" + album_uuid + ".json");
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid) {
         return makeAuthenticatedGet(endpoint + "albums/" + album_uuid + "/photos.json");
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid, int offset) {
         return makeAuthenticatedGet(endpoint + "albums/" + album_uuid + "/photos.json&offset=" + offset);
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid, int offset, int limit) {
         return makeAuthenticatedGet(endpoint + "albums/" + album_uuid + "/photos.json&offset=" + offset
                                     + "&limit=" + limit);
     }
 
     public static String getPhotoByPhotoId(String photo_uuid) {
         return makeAuthenticatedGet(endpoint + "photos/" + photo_uuid + ".json");
     }
 
     public static String getUserInbox() {
         return makeAuthenticatedGet(endpoint + "messages.json&box=inbox");
     }
 
     public static String getUserSentBox() {
         return makeAuthenticatedGet(endpoint + "messages.json&box=sent");
     }
 
     public static String getUserMessagesByThreadId(String thread_id) {
         return makeAuthenticatedGet(endpoint + "messages/" + thread_id + ".json");
     }
 
     public static String getEventByEventId(String event_id) {
         return makeAuthenticatedGet(endpoint + "events/" + event_id + ".json");
     }
 
     public static String getUserResourceByResourceId(String resource_id) {
         return makeAuthenticatedGet(endpoint + "resources/" + resource_id + ".json");
     }
 
     private static String makeAuthenticatedGet(String urlString) {
         if (!isLoggedIn()) {
             return "You are not logged in";
         }
 
         // Make and return from authenticated get call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             if (connection.getResponseCode() == 204) {
                 return "error";
             }
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
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
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
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
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
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             if (connection.getResponseCode() == 204) {
                 return "error";
             }
             BufferedReader input = new BufferedReader(new InputStreamReader(
                         inStream));
 
             String line = "";
             String result = "";
             while ((line = input.readLine()) != null) {
                 result += line;
             }
 
             return result;
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeUnauthenticatedGet/" + ex);
             return ex.toString();
         }
     }
 
     private static String makeAuthenticatedPost(String urlString,
             String[][] contents) {
         // Make and return from authenticated post call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
             connection.setDoInput(true);
             connection.setDoOutput(true);
             connection.setUseCaches(false);
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type",
                                           "application/x-www-form-urlencoded");
 
             // If not logging in, set the cookies in the header
             if (!urlString
                     .equals(endpoint + "users/login.json")) {
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
             return "error";
         }
     }
 
     public static void logOut() {
         try {
             CookieManager cm = ((CookieManager) CookieHandler.getDefault());
             URI uri = new URI(endpoint + "users/login.json");
             List<HttpCookie> myCookies = cm.getCookieStore().get(uri);
             for (int i = 0; i < myCookies.size(); i++) {
                 cm.getCookieStore().remove(uri, myCookies.get(i));
             }
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
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
