 package de.fhb.mi.paperfly.service;
 
 import android.util.Log;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import com.google.gson.reflect.TypeToken;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.auth.DESEncryption;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.RegisterAccountDTO;
 import de.fhb.mi.paperfly.dto.RoomDTO;
 import de.fhb.mi.paperfly.dto.Status;
 import de.fhb.mi.paperfly.dto.TokenDTO;
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 
 /**
  * This is an implementation of {@link de.fhb.mi.paperfly.service.RestConsumer} implemented as singleton.
  *
  * @author Christoph Ott
  */
 @NoArgsConstructor(access = AccessLevel.PRIVATE)
 public class RestConsumerSingleton implements RestConsumer {
 
     public static final String LOCAL_IP = "10.0.2.2";
     public static final String AWS_IP = "46.137.173.175";
     public static final boolean CONNECT_LOCAL = false;
     public static final String PORT = "8080";
 
     public static final String URL_LOGIN_BASIC = "PaperFlyServer-web/secure/";
     public static final String URL_LOGIN = "PaperFlyServer-web/rest/v1/auth/login";
     public static final String URL_GET_ACCOUNT = "PaperFlyServer-web/rest/v1/account/";
     public static final String URL_LOGOUT = "PaperFlyServer-web/rest/v1/auth/logout";
 
     public static final String URL_REGISTER_ACCOUNT = "PaperFlyServer-web/rest/v1/account/register";
     public static final String URL_GET_MY_ACCOUNT = "PaperFlyServer-web/rest/v1/myaccount/get";
     public static final String URL_GET_MY_FRIENDLIST = "PaperFlyServer-web/rest/v1/myaccount/friendlist";
     public static final String URL_SEARCH_ACCOUNT = "PaperFlyServer-web/rest/v1/account/search/";
     public static final String URL_EDIT_ACCOUNT = "PaperFlyServer-web/rest/v1/myaccount/edit";
     public static final String URL_ADD_OR_REMOVE_FRIEND = "PaperFlyServer-web/rest/v1/myaccount/friend/";
     public static final String URL_ACCOUNTS_IN_ROOM = "PaperFlyServer-web/rest/v1/room/accounts/";
     public static final String URL_GET_ROOM = "PaperFlyServer-web/rest/v1/room/";
     public static final String URL_LOCATE_ACCOUNT = "PaperFlyServer-web/rest/v1/room/locateAccount/";
     public static final String URL_CHANGE_ACCOUNT_STATUS = "PaperFlyServer-web/rest/v1/myaccount/status/";
     public static final String URL_CHAT_GLOBAL = ChatService.URL_CHAT_BASE + "global";
 
     public static final String URL_GET_ALL_ACCOUNTS_IN_ROOM = "PaperFlyServer-web/rest/v1/room/accounts/";
 
     private static final String TAG = RestConsumerSingleton.class.getSimpleName();
 
     private PaperFlyApp application;
 
     //TODO consumer wird null gesetzt in der app, wenn man ausloggt. besser machen?
     @Getter
     @Setter
     private CommonsHttpOAuthConsumer consumer = null;
 
     /**
      * Gets the Singleton instance of the RestConsumer.
      *
      * @return the Instance of the RestConsumer
      */
     public static RestConsumerSingleton getInstance() {
         return SingletonHolder.INSTANCE;
     }
 
     @Override
     public AccountDTO addFriend(String friendUsername) throws RestConsumerException {
         Log.d(TAG, "addFriend");
 
         AccountDTO responseAccount = null;
         HttpUriRequest request = new HttpPost(getConnectionURL(URL_ADD_OR_REMOVE_FRIEND + friendUsername));
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
 
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
 
             responseAccount = gson.fromJson(responseObjAsString, AccountDTO.class);
             application.setAccount(responseAccount);
             return responseAccount;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Evaluates the httpStatus of a Request.
      *
      * @param response the response
      *
      * @throws RestConsumerException
      */
     private void analyzeHttpStatus(HttpResponse response) throws RestConsumerException {
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
             Log.d(TAG, "" + response.getStatusLine());
             switch (response.getStatusLine().getStatusCode()) {
                 case 412:
                     throw new RestConsumerException(RestConsumerException.INVALID_INPUT_MESSAGE);
                 case 500:
                     throw new RestConsumerException(RestConsumerException.INTERNAL_SERVER_MESSAGE);
                 default:
                     throw new RestConsumerException("Response:" + response.getStatusLine().getStatusCode());
             }
         }
     }
 
     @Override
     public AccountDTO editAccount(AccountDTO editedAccount) throws RestConsumerException, UnsupportedEncodingException {
         Log.d(TAG, "editAccount");
 
         AccountDTO responseAccount = null;
         HttpUriRequest request = new HttpPost(getConnectionURL(URL_EDIT_ACCOUNT));
 
         Gson sendMapper = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateSerializer()).create();
 
         String jsonToSend = sendMapper.toJson(editedAccount);
         StringEntity entityToSend = new StringEntity(jsonToSend);
         Log.d(TAG, jsonToSend);
 
         entityToSend.setContentEncoding("UTF-8");
         entityToSend.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 
         ((HttpPost) request).setEntity(entityToSend);
         request.addHeader("accept", "application/json");
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Log.d("json", responseObjAsString);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             responseAccount = gson.fromJson(responseObjAsString, AccountDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return responseAccount;
     }
 
     @Override
     public AccountDTO getAccountByUsername(String username) throws RestConsumerException {
         Log.d(TAG, "getAccountByUsername");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_GET_ACCOUNT) + username);
         AccountDTO account = null;
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             account = gson.fromJson(responseObjAsString, AccountDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return account;
     }
 
     @Override
     public List<AccountDTO> getAccountsInRoom(long roomID) throws RestConsumerException {
         Log.d(TAG, "getAccountsInRoom");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_ACCOUNTS_IN_ROOM) + roomID);
         List<AccountDTO> accountsInRoom = new ArrayList<AccountDTO>();
         Type collectionType = new TypeToken<ArrayList<AccountDTO>>() {
         }.getType();
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             accountsInRoom = gson.fromJson(responseObjAsString, collectionType);
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return accountsInRoom;
     }
 
     /**
      * Builds the connection-url depending of local-setting-value CONNECT_LOCAL.
      *
      * @param restURL the url
      *
      * @return the complete URL to connect to
      */
     private String getConnectionURL(String restURL) {
 
         StringBuilder urlToBuild = new StringBuilder();
         urlToBuild.append("http://");
 
         if (CONNECT_LOCAL) {
             urlToBuild.append(LOCAL_IP);
         } else {
             urlToBuild.append(AWS_IP);
         }
 
         urlToBuild.append(":" + PORT + "/");
         urlToBuild.append(restURL);
 
         return urlToBuild.toString();
     }
 
     public List<AccountDTO> getMyFriendList() throws RestConsumerException {
         Log.d(TAG, "getMyFriendList");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_GET_MY_FRIENDLIST));
         List<AccountDTO> myFriendlist = new ArrayList<AccountDTO>();
         Type collectionType = new TypeToken<ArrayList<AccountDTO>>() {
         }.getType();
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Log.d("json", "" + responseObjAsString);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             myFriendlist = gson.fromJson(responseObjAsString, collectionType);
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return (myFriendlist == null) ? new ArrayList<AccountDTO>() : myFriendlist;
     }
 
     @Override
     public RoomDTO getRoom(String roomID) throws RestConsumerException {
         Log.d(TAG, "getUsersInRoom");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_GET_ROOM) + roomID);
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             return gson.fromJson(responseObjAsString, RoomDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public List<AccountDTO> getUsersInRoom(Long roomID) throws RestConsumerException {
         Log.d(TAG, "getUsersInRoom");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_ACCOUNTS_IN_ROOM) + roomID);
         List<AccountDTO> usersInRoom = new ArrayList<AccountDTO>();
         Type collectionType = new TypeToken<ArrayList<AccountDTO>>() {
         }.getType();
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             usersInRoom = gson.fromJson(responseObjAsString, collectionType);
             application.setUsersInRoom(usersInRoom);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
        return (usersInRoom != null) ? usersInRoom : new ArrayList<AccountDTO>();
     }
 
     /**
      * Passes the {@link android.app.Application} to the Singleton.
      *
      * @param application the {@link android.app.Application}
      */
     public void init(PaperFlyApp application) {
         this.application = application;
     }
 
     @Override
     public RoomDTO locateAccount(String username) throws RestConsumerException {
         Log.d(TAG, "locateAccount");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_LOCATE_ACCOUNT) + username);
         RoomDTO room = null;
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             room = gson.fromJson(responseObjAsString, RoomDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return room;
 
     }
 
     @Override
     public TokenDTO login(String mail, String password) throws RestConsumerException {
         Log.d(TAG, "login");
         HttpGet request = new HttpGet(getConnectionURL(URL_LOGIN)); // Or HttpPost(), depends on your needs
         try {
             DESEncryption desEncryption = new DESEncryption();
 
             String usernamePassword = mail + ":" + password;
 
             request.addHeader("cred", desEncryption.encrypt(usernamePassword));
         } catch (Exception e) {
             e.printStackTrace();
         }
         Log.d(TAG, request.getRequestLine().toString());
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
             String responseObjAsString = readInEntity(response);
             Gson gson = new Gson();
 
             TokenDTO token = gson.fromJson(responseObjAsString, TokenDTO.class);
             consumer = new CommonsHttpOAuthConsumer(token.getConsumerKey(), token.getConsumerSecret());
             return token;
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public void logout() throws RestConsumerException {
         Log.d(TAG, "logout");
 
         AccountDTO responseAccount = null;
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_LOGOUT));
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
             consumer = null;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Reads in the response String.
      *
      * @param response the response
      *
      * @return a json string from the response
      * @throws IOException
      */
     private String readInEntity(HttpResponse response) throws IOException {
         InputStream is = response.getEntity().getContent();
         BufferedReader rd = new BufferedReader(new InputStreamReader(is));
         String line;
         StringBuilder responseObj = new StringBuilder();
         while ((line = rd.readLine()) != null) {
             responseObj.append(line);
             responseObj.append('\r');
         }
         rd.close();
         return responseObj.toString();
     }
 
     @Override
     public TokenDTO register(RegisterAccountDTO registerAccount) throws UnsupportedEncodingException, RestConsumerException {
 
         HttpUriRequest request = new HttpPut(getConnectionURL(URL_REGISTER_ACCOUNT));
 
         Gson sendMapper = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateSerializer()).create();
 
         String jsonToSend = sendMapper.toJson(registerAccount);
         StringEntity entityToSend = new StringEntity(jsonToSend);
         Log.d(TAG, jsonToSend);
 
         entityToSend.setContentEncoding("UTF-8");
         entityToSend.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 
         ((HttpPut) request).setEntity(entityToSend);
         request.addHeader("accept", "application/json");
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Log.d("json", responseObjAsString);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             return gson.fromJson(responseObjAsString, TokenDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public AccountDTO removeFriend(String friendUsername) throws RestConsumerException {
         Log.d(TAG, "removeFriend");
 
         AccountDTO responseAccount = null;
         HttpUriRequest request = new HttpDelete(getConnectionURL(URL_ADD_OR_REMOVE_FRIEND + friendUsername));
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
 
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
 
             responseAccount = gson.fromJson(responseObjAsString, AccountDTO.class);
             application.setAccount(responseAccount);
             return responseAccount;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public List<AccountDTO> searchAccount(String query) throws RestConsumerException {
         Log.d(TAG, "searchAccount");
 
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_SEARCH_ACCOUNT) + query);
         List<AccountDTO> searchResultList = new ArrayList<AccountDTO>();
         Type collectionType = new TypeToken<ArrayList<AccountDTO>>() {
         }.getType();
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             searchResultList = gson.fromJson(responseObjAsString, collectionType);
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return searchResultList;
     }
 
     @Override
     public AccountDTO setMyAccountStatus(Status status) throws RestConsumerException {
         Log.d(TAG, "setMyAccountStatus");
 
         HttpUriRequest request = new HttpPost(getConnectionURL(URL_CHANGE_ACCOUNT_STATUS) + status.name());
         AccountDTO account = null;
 
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             Log.d(TAG, "json: " + responseObjAsString);
 
             account = gson.fromJson(responseObjAsString, AccountDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         return account;
     }
 
     @Override
     public void updateMyAccount() throws RestConsumerException {
         Log.d(TAG, "updateMyAccount");
 
         AccountDTO responseAccount = null;
         HttpUriRequest request = new HttpGet(getConnectionURL(URL_GET_MY_ACCOUNT));
         Log.d(TAG, request.getRequestLine().toString());
 
         HttpClient httpclient = application.getHttpClient();
         HttpResponse response;
         try {
             consumer.sign(request);
             response = httpclient.execute(request);
             analyzeHttpStatus(response);
 
             String responseObjAsString = readInEntity(response);
             Log.d("json", responseObjAsString);
             Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();
             responseAccount = gson.fromJson(responseObjAsString, AccountDTO.class);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (OAuthExpectationFailedException e) {
             e.printStackTrace();
         } catch (OAuthCommunicationException e) {
             e.printStackTrace();
         } catch (OAuthMessageSignerException e) {
             e.printStackTrace();
         }
         application.setAccount(responseAccount);
     }
 
     private static class SingletonHolder {
         public static final RestConsumerSingleton INSTANCE = new RestConsumerSingleton();
     }
 
     /**
      * Deserializer for Gson with long date format
      */
     public class JsonDateDeserializer implements JsonDeserializer<Date> {
         public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
             String dateAsString = json.getAsJsonPrimitive().getAsString();
             long dateAsLong = Long.parseLong(dateAsString.substring(6, dateAsString.length() - 2));
             return new Date(dateAsLong);
         }
     }
 
     /**
      * Serializer for Gson with long date format
      */
     public class JsonDateSerializer implements JsonSerializer<Date> {
         public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
             return src == null ? null : new JsonPrimitive(src.getTime());
         }
     }
 }
 
