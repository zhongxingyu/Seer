 /*
  This file is part of NerdzApi-java.
 
     NerdzApi-java is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     NerdzApi-java is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
 */
 
 package eu.nerdz.api.impl.reverse;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import eu.nerdz.api.Application;
 import eu.nerdz.api.ContentException;
 import eu.nerdz.api.HttpException;
 import eu.nerdz.api.LoginException;
 import eu.nerdz.api.UserInfo;
 import eu.nerdz.api.WrongUserInfoTypeException;
 
 /**
  * A Reverse abstract implementation of an Application.
  */
 public abstract class AbstractReverseApplication implements Application {
 
     /**
      * Represents the domain in which all post/get requests are made.
      */
 
     public final static String PROTOCOL = "https";
     public final static String SUBDOMAIN = "www";
     public final static String SUBDOMAIN_FULL = AbstractReverseApplication.SUBDOMAIN + ".nerdz.eu";
     public final static String NERDZ_DOMAIN_NAME = AbstractReverseApplication.PROTOCOL + "://" + AbstractReverseApplication.SUBDOMAIN_FULL;
     /**
      *
      */
     private static final long serialVersionUID = -5784101258239287408L;
     /**
      * This is the DefaultHttpClient main instance. It will contain login informations, and all requests will pass through it.
      */
     protected DefaultHttpClient mHttpClient;
     private String mUserName;
 
     /**
      * the token, required for login.
      */
 
     /**
      * the constructor takes care of logging into NERDZ. The cookies gathered through the login process remains in mHttpClient, allowing for logged in requsts.
      *
      * @param user     username, unescaped
      * @param password password
      */
     protected AbstractReverseApplication(String user, String password) throws IOException, HttpException, LoginException {
 
         this.mUserName = user;
         this.mHttpClient = new DefaultHttpClient();
 
         String token;
 
         //fetch token.
         {
             String body = this.get();
 
             // token is hidden in an input tag. It's needed just for login/logout
             int start = body.indexOf("<input type=\"hidden\" value=\"") + 28;
             token = body.substring(start, start + 32);
         }
 
         Map<String, String> form = new HashMap<String, String>(4);
         form.put("setcookie", "on");
         form.put("username", user);
         form.put("password", password);
         form.put("tok", token);
 
         // login
         String responseBody = this.post("/pages/profile/login.json.php", form, null, true);
 
         //check for a wrong login.
         if (responseBody.contains("error")) {
             throw new LoginException();
         }
     }
 
     /**
      * This constructor creates an HttpClient with the already existing cookies.
      * Validity of loginData is not checked.
      *
      * @param loginData login data, stored in a ReverseUserInfo class.
      * @throws WrongUserInfoTypeException if loginData is not an AbstractReverseApplication.ReverseUserInfo instance
      */
     protected AbstractReverseApplication(UserInfo loginData) throws WrongUserInfoTypeException {
 
         ReverseUserInfo userInfo;
 
         try {
             userInfo = (ReverseUserInfo) loginData;
         } catch (ClassCastException e) {
             throw new WrongUserInfoTypeException("login data passed is not Reverse. ");
         }
 
         this.mUserName = userInfo.getUsername();
         this.mHttpClient = new DefaultHttpClient();
         this.mHttpClient.getCookieStore().addCookie(userInfo.getNerdzIdCookie());
         this.mHttpClient.getCookieStore().addCookie(userInfo.getNerdzUCookie());
 
     }
 
     /**
      * Checks if login data is valid.
      *
      * @return true if operations as logged user are possible. Exception if not
      * @throws IOException
      * @throws HttpException
      */
     @Override
     public boolean checkValidity() throws IOException, HttpException {
 
         if (this.get("/pages/pm/notify.json.php").contains("error")) {
             throw new LoginException("invalid token");
         }
 
         return true;
 
     }
 
     /**
      * Returns the username.
      *
      * @return a java.lang.String representing the username.
      */
     @Override
     public String getUsername() {
         return this.mUserName;
     }
 
     /**
      * Returns the NERDZ ID.
      *
      * @return an int representing the user ID
      */
     @Override
     public int getUserID() {
 
         for (Cookie cookie : this.mHttpClient.getCookieStore().getCookies())
             if (cookie.getName().equals("nerdz_id")) {
                 return Integer.parseInt(cookie.getValue());
             }
 
         return -1;
     }
 
     @Override
     public ReverseUserInfo getUserInfo() {
         return new ReverseUserInfo(this.mUserName, this.mHttpClient.getCookieStore());
     }
 
     /**
      * Executes a GET request on NERDZ.
      * This version returns the content of NERDZ_DOMAIN_NAME.
      *
      * @return a String containing the contents of NERDZ_DOMAIN_NAME.
      * @throws IOException
      * @throws HttpException
      */
     public String get() throws IOException, HttpException {
         return this.get("");
     }
 
     /**
      * Executes a GET request on NERDZ.
      * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
      *
      * @param url an address beginning with /
      * @return the content of NERDZ_DOMAIN_NAME + url.
      * @throws IOException
      * @throws HttpException
      */
     public String get(String url) throws IOException, HttpException {
         return this.get(url, false);
     }
 
     /**
      * Executes a GET request on NERDZ.
      * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
      *
      * @param url     an address beginning with /
      * @param consume if true, the entity associated with the response is consumed
      * @return the content of NERDZ_DOMAIN_NAME + url
      * @throws IOException
      * @throws HttpException
      */
     public String get(String url, boolean consume) throws IOException, HttpException {
 
         HttpGet get = new HttpGet(AbstractReverseApplication.NERDZ_DOMAIN_NAME + url);
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
         HttpResponse response = this.mHttpClient.execute(get);
         StatusLine statusLine = response.getStatusLine();
 
         int code = statusLine.getStatusCode();
         if (code != 200) {
             throw new HttpException(code, statusLine.getReasonPhrase());
         }
 
         String body = responseHandler.handleResponse(response);
 
         if (consume) {
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 entity.consumeContent();
             }
         }
 
         return body.trim();
     }
 
     /**
      * Issues a POST request to NERDZ.
      * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
      * form is urlencoded by post, so it should not be encoded before.
      *
      * @param url  an address beginning with /
      * @param form a Map<String,String> that represents a form
      * @return a String containing the response body
      * @throws IOException
      * @throws HttpException
      */
     public String post(String url, Map<String, String> form) throws IOException, HttpException {
         return this.post(url, form, null, false);
     }
 
     /**
      * Issues a POST request to NERDZ.
      * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
      * form is urlencoded by post, so it should not be encoded before.
      *
      * @param url     an address beginning with /
      * @param form    a Map<String,String> that represents a form
      * @param referer if not null, this string is used as the referer in the response.
      * @return a String containing the response body
      * @throws IOException
      * @throws HttpException
      */
     public String post(String url, Map<String, String> form, String referer) throws IOException, HttpException {
         return this.post(url, form, referer, false);
     }
 
     /**
      * Issues a POST request to NERDZ.
      * The given URL is automatically prepended with NERDZ_DOMAIN_NAME, so it should be something like /pages/pm/inbox.html.php.
      * form is urlencoded by post, so it should not be encoded before.
      *
      * @param url     an address beginning with /
      * @param form    a Map<String,String> that represents a form
      * @param referer if not null, this string is used as the referer in the response.
      * @param consume if true, the entity associated with the response is consumed
      * @return a String containing the response body
      * @throws IOException
      * @throws HttpException
      */
     public String post(String url, Map<String, String> form, String referer, boolean consume) throws IOException, HttpException {
 
         HttpPost post = new HttpPost(AbstractReverseApplication.NERDZ_DOMAIN_NAME + url);
 
         post.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
 
         if (referer != null) {
             post.addHeader("Referer", referer);
         }
 
         List<NameValuePair> formEntries = new ArrayList<NameValuePair>(form.size());
         for (Map.Entry<String, String> entry : form.entrySet())
             formEntries.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
         post.setEntity(new UrlEncodedFormEntity(formEntries, "UTF-8"));
 
         HttpResponse response = this.mHttpClient.execute(post);
         StatusLine statusLine = response.getStatusLine();
 
         int code = statusLine.getStatusCode();
         if (code != 200) {
             throw new HttpException(code, statusLine.getReasonPhrase());
         }
 
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
         String body = responseHandler.handleResponse(response);
 
         if (consume) {
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 entity.consumeContent();
             }
         }
 
 
         return body.trim();
     }
 
     @Override
     public void registerForPush(String service, String devId) throws IOException, HttpException, ContentException {
         Map<String,String> form = new HashMap<String, String>(2);
         form.put("service", service);
        form.put("deviceId", devId);
         String body = this.post("/push.php?action=subscribe",form);
 
         try {
             JSONObject jObj = new JSONObject(body);
 
             if(jObj.has("ERROR")) {
                 throw new ContentException("Cannot subscribe: " + jObj.getString("ERROR"));
             }
         } catch (JSONException e) {
             throw new ContentException("Invalid json in response");
         }
     }
 
     @Override
     public void unregisterFromPush(String service, String devId) throws IOException, HttpException, ContentException {
         Map<String,String> form = new HashMap<String, String>(2);
         form.put("service", service);
         form.put("devId", devId);
         String body = this.post("/push.php?action=unsubscribe",form);
 
         try {
             JSONObject jObj = new JSONObject(body);
 
             if(jObj.has("ERROR")) {
                 throw new ContentException("Cannot unsubscribe: " + jObj.getString("ERROR"));
             }
         } catch (JSONException e) {
             throw new ContentException("Invalid json in response");
         }
     }
 
     /**
      * Represents reverse login data.
      */
     public static class ReverseUserInfo implements UserInfo {
 
         /**
          *
          */
         private static final long serialVersionUID = -5768466751046728537L;
         transient private String mUserName;
         transient private Cookie mNerdzU;
         transient private Cookie mNerdzId;
 
         /**
          * Creates an instance, an fills it with preprocessed loginData.
          *
          * @param userName    a username
          * @param cookieStore an HttpClient CookieStore, initialized with a NERDZ login.
          * @throws ContentException
          */
         public ReverseUserInfo(String userName, CookieStore cookieStore) throws ContentException {
             this.mUserName = userName;
             for (Cookie cookie : cookieStore.getCookies()) {
                 if (cookie.getName().equals("nerdz_u")) {
                     BasicClientCookie nerdzU = new BasicClientCookie(cookie.getName(), cookie.getValue());
                     nerdzU.setExpiryDate(cookie.getExpiryDate());
                     nerdzU.setPath(cookie.getPath());
                     nerdzU.setDomain(cookie.getDomain());
                     nerdzU.setVersion(cookie.getVersion());
                     this.mNerdzU = nerdzU;
                 } else if (cookie.getName().equals("nerdz_id")) {
                     BasicClientCookie nerdzId = new BasicClientCookie(cookie.getName(), cookie.getValue());
                     nerdzId.setExpiryDate(cookie.getExpiryDate());
                     nerdzId.setPath(cookie.getPath());
                     nerdzId.setDomain(cookie.getDomain());
                     nerdzId.setVersion(cookie.getVersion());
                     this.mNerdzId = nerdzId;
                 }
             }
 
             if (this.mNerdzId == null || this.mNerdzU == null) {
                 throw new ContentException("malformed cookie store");
             }
         }
 
         /**
          * Creates an instance using external data.
          *
          * @param userName a username
          * @param nerdzId  an id as a String
          * @param nerdzU   a nerdz_u token
          */
         public ReverseUserInfo(String userName, String nerdzId, String nerdzU) {
 
             this.mUserName = userName;
             BasicClientCookie nerdzUCookie = new BasicClientCookie("nerdz_u", nerdzU);
             nerdzUCookie.setExpiryDate(new Date(new Date().getTime() + 1000L * 365L * 24L * 3600L * 1000L));
             nerdzUCookie.setPath("/");
             nerdzUCookie.setDomain('.' + AbstractReverseApplication.SUBDOMAIN_FULL);
             this.mNerdzU = nerdzUCookie;
             BasicClientCookie nerdzIdCookie = new BasicClientCookie("nerdz_id", nerdzId);
             nerdzIdCookie.setExpiryDate(new Date(new Date().getTime() + 1000L * 365L * 24L * 3600L * 1000L));
             nerdzIdCookie.setPath("/");
             nerdzIdCookie.setDomain('.' + AbstractReverseApplication.SUBDOMAIN_FULL);
             this.mNerdzId = nerdzIdCookie;
 
         }
 
         /**
          * Here just for Externalizable.
          */
 
         @SuppressWarnings("unused")
         public ReverseUserInfo() {
         }
 
         @Override
         public int getNerdzID() {
             return Integer.parseInt(this.mNerdzId.getValue());
         }
 
         @Override
         public String getUsername() {
             return this.mUserName;
         }
 
         public String getNerdzU() {
             return this.mNerdzU.getValue();
         }
 
         @Override
         public void writeExternal(ObjectOutput outputStream) throws IOException {
             outputStream.writeObject(this.mUserName);
             outputStream.writeObject(this.mNerdzId.getValue());
             outputStream.writeObject(this.mNerdzU.getValue());
 
         }
 
         @Override
         public void readExternal(ObjectInput inputStream) throws IOException, ClassNotFoundException {
             ReverseUserInfo info = new ReverseUserInfo((String) inputStream.readObject(), (String) inputStream.readObject(), (String) inputStream.readObject());
             this.mUserName = info.mUserName;
             this.mNerdzId = info.mNerdzId;
             this.mNerdzU = info.mNerdzU;
 
         }
 
         public Cookie getNerdzUCookie() {
             return this.mNerdzU;
         }
 
         public Cookie getNerdzIdCookie() {
             return this.mNerdzId;
         }
 
         @Override
         public String toString() {
             return "Nerdz Username: " + this.mUserName + ", ID: " + this.getNerdzID() + ", NerdzU: " + this.mNerdzU.getValue();
         }
 
     }
 }
 
 
