 package com.allplayers.rest;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.HttpCookie;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.util.Log;
 
 public class RestApiV1 {
     private static final String ENDPOINT = "https://www.allplayers.com/?q=api/v1/rest/";
     private static final String CAP_TOKEN_NAME = "X-ALLPLAYERS-CAPTCHA-TOKEN";
     private static final String CAP_SOLUTION_NAME = "X-ALLPLAYERS-CAPTCHA-SOLUTION";
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
             URL url = new URL(ENDPOINT + "users/" + sCurrentUserUUID + ".json");
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
 
     public static String createNewUser(String firstName, String lastName,
                                        String email, String gender, String birthday, String password,
                                        String capToken, String capResponse) {
 
         String[][] contents = new String[6][2];
 
         // Set firstName
         contents[0][0] = "firstname";
         contents[0][1] = firstName;
 
         // Set lastName
         contents[1][0] = "lastname";
         contents[1][1] = lastName;
 
         // Set email
         contents[2][0] = "email";
         contents[2][1] = email;
 
         // Set gender (M or F)
         contents[3][0] = "gender";
         contents[3][1] = gender;
 
         // Set birthday (YYYY-MM-DD)
         contents[4][0] = "birthday";
         contents[4][1] = birthday;
 
         // Set password
         contents[5][0] = "password";
         contents[5][1] = password;
 
         return makeUnauthenticatedPost(ENDPOINT + "users.json", contents,
                                        capToken, capResponse);
     }
 
     public static String deleteMessageThread(String id) {
         return makeAuthenticatedDelete(ENDPOINT + "messages/" + id);
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
         return makeAuthenticatedPut(
                    ENDPOINT + "messages/" + threadId + ".json", contents);
     }
 
     public String validateLogin(String username, String password) {
         String[][] contents = new String[2][2];
         contents[0][0] = "username";
         contents[0][1] = username;
         contents[1][0] = "password";
         contents[1][1] = password;
 
         return makeAuthenticatedPost(ENDPOINT + "users/login.json", contents);
     }
 
     public static String postMessage(int threadId, String body) {
         String[][] contents = new String[2][2];
         contents[0][0] = "thread_id";
         contents[0][1] = "" + threadId;
         contents[1][0] = "body";
         contents[1][1] = body;
 
         return makeAuthenticatedPost(ENDPOINT + "messages.json", contents);
     }
 
     public static String createNewMessage(String[] uuids, String subject,
                                           String body) {
         String[][] contents = new String[uuids.length + 2][2];
         for (int i = 0; i < uuids.length; i++) {
             contents[i][0] = "recipients[" + i + "]";
             contents[i][1] = uuids[i];
         }
         contents[uuids.length][0] = "subject";
         contents[uuids.length][1] = subject;
         contents[uuids.length + 1][0] = "body";
         contents[uuids.length + 1][1] = body;
         return makeAuthenticatedPost(ENDPOINT + "messages.json", contents);
     }
 
     public static String searchGroups(String search, int zipcode, int distance) {
         String searchTerms = ENDPOINT + "groups.json";
         if (search.length() != 0) {
             searchTerms += ("&search=\"" + search + "\"");
         }
         // As of right now, the input distance will only matter if a zipcode is
         // given,
         // so it is only considered in that case.
         // TODO Add in considering the distance as "Distance from my location"
         if (zipcode != 0) {
             searchTerms += ("&distance[postal_code]=" + zipcode
                             + "&distance[search_distance]=" + distance + "&distance[search_units]=mile");
         }
         return makeUnauthenticatedGet(searchTerms);
     }
 
     /**
      * getUserGroups().
      * API call to fetch the currently logged in user's groups.
      *
      * @param offset: Determines at what point the API returns data (starts after 'offset' results).
      * @param limit: The number of results the API will return.
      * @param sort: The method that the data should be sorted by:
      *      "radioactivity": Sort the data by group activity (radioactivity).
      *      "alphabetical_ascending": Sort the data in alphabetically ascending order.
      *      "alphabetical_descending": Sort the data in alphabetically descending order.
      * @return: Result from API.
      *
      */
     public static String getUserGroups(int offset, int limit, String sort) {
         String query = ENDPOINT + "users/" + sCurrentUserUUID + "/groups.json";
         if (offset > 0) {
             query += ("&offset=" + offset);
         }
         if (limit > -1) {
             query += ("&limit=" + limit);
         }
         if (sort != null && !sort.equals("")) {
             query += ("&sort=" + sort);
         }
         return makeAuthenticatedGet(query);
     }
 
     public static String getUserFriends() {
         String jsonResult = makeAuthenticatedGet(ENDPOINT + "users/"
                             + sCurrentUserUUID + "/friends.json");
         return jsonResult.replaceAll("&#039;", "'");
     }
 
     public static String getUserGroupmates(int limit) {
         return makeAuthenticatedGet(ENDPOINT + "users/" + sCurrentUserUUID
                                     + "/groupmates.json&limit=" + limit);
     }
 
     public static String getUserGroupmates(int limit, int offset) {
         return makeAuthenticatedGet(ENDPOINT + "users/" + sCurrentUserUUID
                                     + "/groupmates.json&limit=" + limit + "&offset=" + offset);
     }
 
     public static String getUserEvents(int limit) {
         return makeAuthenticatedGet(ENDPOINT + "users/" + sCurrentUserUUID
                                     + "/events/upcoming.json&limit=" + limit);
     }
 
     public static String getUserEvents(int limit, int offset) {
         return makeAuthenticatedGet(ENDPOINT + "users/" + sCurrentUserUUID
                                     + "/events/upcoming.json&limit=" + limit + "&offset=" + offset);
     }
 
     public static String getGroupInformationByGroupId(String group_uuid) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid + ".json");
     }
 
     public static String getGroupAlbumsByGroupId(String group_uuid, int limit) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/albums.json&limit=" + limit);
     }
 
     public static String getGroupAlbumsByGroupId(String group_uuid, int limit,
             int offset) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/albums.json&limit=" + limit + "&offset=" + offset);
     }
 
     public static String getGroupEventsByGroupId(String group_uuid, int limit) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/events/upcoming.json&limit=" + limit);
     }
 
     public static String getGroupEventsByGroupId(String group_uuid, int limit,
             int offset) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/events/upcoming.json&limit=" + limit + "&offset=" + offset);
     }
 
     public static String getGroupMembersByGroupId(String group_uuid, int limit) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/members.json&limit=" + limit);
     }
 
     public static String getGroupMembersByGroupId(String group_uuid, int limit,
             int offset) {
         return makeAuthenticatedGet(ENDPOINT + "groups/" + group_uuid
                                     + "/members.json&limit=" + limit + "&offset=" + offset);
     }
 
     public static String getGroupPhotosByGroupId(String group_uuid) {
         return makeAuthenticatedGet(ENDPOINT + "groups/photos.json");
     }
 
     public static String getAlbumByAlbumId(String album_uuid) {
         return makeAuthenticatedGet(ENDPOINT + "albums/" + album_uuid + ".json");
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid, int limit) {
         return makeAuthenticatedGet(ENDPOINT + "albums/" + album_uuid
                                     + "/photos.json&limit=" + limit);
     }
 
     public static String getAlbumPhotosByAlbumId(String album_uuid, int limit,
             int offset) {
         return makeAuthenticatedGet(ENDPOINT + "albums/" + album_uuid
                                     + "/photos.json&offset=" + offset + "&limit=" + limit);
     }
 
     public static String getPhotoByPhotoId(String photo_uuid) {
         return makeAuthenticatedGet(ENDPOINT + "photos/" + photo_uuid + ".json");
     }
 
     public static String getUserInbox() {
         return makeAuthenticatedGet(ENDPOINT + "messages.json&box=inbox");
     }
 
     public static String getUserSentBox() {
         return makeAuthenticatedGet(ENDPOINT + "messages.json&box=sent");
     }
 
     public static String getUserMessagesByThreadId(String thread_id) {
         return makeAuthenticatedGet(ENDPOINT + "messages/" + thread_id
                                     + ".json");
     }
 
     public static String getEventByEventId(String event_id) {
         return makeAuthenticatedGet(ENDPOINT + "events/" + event_id + ".json");
     }
 
     public static String getUserResourceByResourceId(String resource_id) {
         return makeAuthenticatedGet(ENDPOINT + "resources/" + resource_id
                                     + ".json");
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
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
 
             connection.setRequestMethod("DELETE");
             connection.setRequestProperty("Content-Type",
                                           "application/x-www-form-urlencoded");
             connection.getResponseCode();
 
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
             return connection.getResponseMessage();
         } catch (Exception ex) {
             System.err.println("APCI_RestServices/makeAuthenticatedPut/" + ex);
             return ex.toString();
         }
     }
 
     private static String makeUnauthenticatedGet(String urlString) {
         // Make and return from unauthenticated get call
         try {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection) url
                                            .openConnection();
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
             if (!urlString.equals(ENDPOINT + "users/login.json")) {
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
 
     private static String makeUnauthenticatedPost(String urlString,
             String[][] contents, String captchaToken, String captchaResponse) {
 
         HttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(urlString);
         List<NameValuePair> request = new ArrayList<NameValuePair>();
         for (int i = 0; i < contents.length; i++) {
             request.add(new BasicNameValuePair(contents[i][0], contents[i][1]));
         }
         try {
             post.setEntity(new UrlEncodedFormEntity(request));
             if (captchaToken != null) {
                 post.addHeader(CAP_TOKEN_NAME, captchaToken);
                 post.addHeader(CAP_SOLUTION_NAME, captchaResponse);
             }
             HttpResponse response = client.execute(post);
             HttpEntity entity = response.getEntity();
             if (null != entity) {
                 String text = EntityUtils.toString(entity);
                 return text;
             }
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return "error";
     }
 
     public static void logOut() {
         try {
             CookieManager cm = ((CookieManager) CookieHandler.getDefault());
             URI uri = new URI(ENDPOINT + "users/login.json");
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
 
     public static void setCurrentUserUUID(String uuid) {
         sCurrentUserUUID = uuid;
     }
 
     public static String getCurrentUserUUID() {
         return sCurrentUserUUID;
     }
 
     public static void setCookieHandler(CookieHandler cookieHandler) {
         sCookieHandler = cookieHandler;
     }
 
     public static CookieHandler getCookieHandler() {
         return sCookieHandler;
     }
 }
