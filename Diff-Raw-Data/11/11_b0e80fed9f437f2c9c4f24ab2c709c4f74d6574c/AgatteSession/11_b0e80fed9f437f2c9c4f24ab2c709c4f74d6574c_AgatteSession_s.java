 /*This file is part of AgatteClient.
 
     AgatteClient is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     AgatteClient is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with AgatteClient.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package com.agatteclient;
 
 
 import android.net.http.AndroidHttpClient;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A session with the Agatte server. It can be used multiple times.
  * <p/>
  * Created by Rémi Pannequin on 24/09/13.
  */
 public class AgatteSession {
 
     private final BasicCookieStore cookieStore;
     private String session_id;
     private String server;
     private long session_expire;
     private HttpGet login_rq;
     private HttpGet logout_rq;
     private HttpPost auth_rq;
     private HttpGet query_day_rq;
     private HttpGet query_top_ok_rq;
     private HttpPost exec_rq;
     private final List<NameValuePair> credentials;
     private HttpContext httpContext;
 
     static final String LOGIN_DIR = "/app/login.form";
     static final String LOGOUT_DIR = "/app/logout.form";
     static final String AUTH_DIR = "/j_acegi_security_check";
     static final String PUNCH_DIR = "/top/top.form";
     static final String PUNCH_OK_DIR = "/top/topOk.htm";
     static final String QUERY_DIR = "/";
     static final String USER = "j_username";
     static final String PASSWORD = "j_password";
     static final String AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
 
 
     /**
      * Create a new Agatte session.
      *
      * @throws URISyntaxException           If server YRL is not correct
      * @throws UnsupportedEncodingException if username and password include chars unsupported in this encoding
      */
     private AgatteSession() throws URISyntaxException, UnsupportedEncodingException {
         //super ("AgatteConnectionService");
         credentials = new ArrayList<NameValuePair>(2);
         httpContext = new BasicHttpContext();
         cookieStore = new BasicCookieStore();
         httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 
 
     }
 
     /**
      * Create a new session with parameters
      *
      * @param server the hostname of the server
      * @param user   the username to use
      * @param passwd the password to use
      * @throws URISyntaxException
      * @throws UnsupportedEncodingException
      */
     public AgatteSession(String server, String user, String passwd) throws URISyntaxException, UnsupportedEncodingException {
         //super ("AgatteConnectionService");
         this();
         this.setServer(server);
         this.setUser(user);
         this.setPassword(passwd);
     }
 
     /**
      * Send a logout to the server.
      *
      * @param client the HttpClient to use
      */
     private void logout(AndroidHttpClient client) {
 
         try {
             client.execute(logout_rq, httpContext);
             //this.session_expire = new Date(cookie.getMaxAge());
             //compute expiration date according to System.date(); ??
             this.session_id = null;
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Attempt to log in Agatte :ie open an agatte session
      *
      * @return false if login failed, true if no errors were detected
      */
     private boolean login(AndroidHttpClient client) throws IOException {
 
         HttpResponse response1 = client.execute(login_rq, httpContext);
         response1.getEntity().consumeContent();
         //test whether response is OK
         if (response1.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
             return false;
         }
         //update sessionID and expiration date
         boolean found_id = false;
         for (Cookie cookie : cookieStore.getCookies())
             if (cookie.getName().equals("JSESSIONID")) {
                 //this.session_expire = new Date(cookie.getMaxAge());
                 //compute expiration date according to System.date(); ??
                 this.session_expire = System.currentTimeMillis();
                 this.session_id = cookie.getValue();
                 found_id = true;
             }
         if (!found_id) {
             return false;
         }
 
         HttpResponse response2 = client.execute(auth_rq, httpContext);
         response2.getEntity().consumeContent();
         //if (response2.getStatusLine().getStatusCode() != HttpStatus.SC_TEMPORARY_REDIRECT) {
         //    return false;
         //}
         for (Header h : response2.getHeaders("Location")) {
             //value should be URL("https", server, "/");
             if (h.getValue().contains("login_error=1")) {
                 this.session_expire = 0;
                 this.session_id = null;
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Attempt to get the list of tops from the server (doing a login if necessary)
      *
      * @return an AgatteResponse instance
      */
     public AgatteResponse query_day() {
         AndroidHttpClient client = null;
         try {
             client = AndroidHttpClient.newInstance(AGENT);
             HttpParams httpParam = client.getParams();
             HttpConnectionParams.setConnectionTimeout(httpParam, 3000);
             HttpConnectionParams.setSoTimeout(httpParam, 5000);
             if (!mustLogin()) {
                 if (!login(client)) {
                     return new AgatteResponse(AgatteResponse.Code.LoginFailed);
                 }
             }
             client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
             client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
             HttpResponse response = client.execute(query_day_rq, httpContext);
             return AgatteParser.getInstance().parse_query_response(response);
         } catch (IOException e) {
             e.printStackTrace();
             return new AgatteResponse(AgatteResponse.Code.IOError, e);
         } finally {
             if (client != null) {
                 client.close();
             }
         }
     }
 
     /**
      * Send a "punch" to the server
      *
      * @return an AgatteResponse instance
      */
     public AgatteResponse doPunch() {
         AndroidHttpClient client = AndroidHttpClient.newInstance(AGENT);
         try {
             if (!mustLogin()) {
                 if (!login(client)) {
                     return new AgatteResponse(AgatteResponse.Code.LoginFailed);
                 }
             }
             HttpResponse response1 = client.execute(exec_rq, httpContext);
             //should be a redirect to topOk
             AgatteResponse.Code code = AgatteParser.getInstance().parse_punch_response(response1);
             switch (code) {
                 case TemporaryOK:
                     HttpResponse response2 = client.execute(query_top_ok_rq, httpContext);
                     return AgatteParser.getInstance().parse_query_response(response2);
                 case NetworkNotAuthorized:
                     return new AgatteResponse(code);
                 default:
                     return new AgatteResponse(AgatteResponse.Code.UnknownError);
             }
         } catch (IOException e) {
             e.printStackTrace();
             return new AgatteResponse(AgatteResponse.Code.IOError, e);
         } finally {
             client.close();
         }
     }
 
     /**
      * Return the host name of the agatte server
      *
      * @return a String like "agatte.univ-lorraine.fr" (without protocol string "https://)
      */
     String getServer() {
         return server;
     }
 
     /**
      * Set the hostname of the server to connect
      *
      * @param server a String like "agatte.univ-lorraine.fr" (without protocol string "https://)
      * @throws URISyntaxException
      */
     public void setServer(String server) throws URISyntaxException {
         this.server = server;
 
         this.login_rq = new HttpGet(new URI("https", this.getServer(), LOGIN_DIR, null));
         this.logout_rq = new HttpGet(new URI("https", this.getServer(), LOGOUT_DIR, null));
         this.auth_rq = new HttpPost(new URI("https", this.getServer(), AUTH_DIR, null));
         this.exec_rq = new HttpPost(new URI("https", this.getServer(), PUNCH_DIR, null));
         this.query_day_rq = new HttpGet(new URI("https", this.getServer(), QUERY_DIR, null));
         this.query_top_ok_rq = new HttpGet(new URI("https", this.getServer(), PUNCH_OK_DIR, null));
     }
 
     /**
      * Return the current user login
      *
      * @return the login
      */
     public String getUser() {
         return credentials.get(0).getValue();
     }
 
     /**
      * Set the username to use to connect
      *
      * @param user the username to set
      * @throws UnsupportedEncodingException
      */
     public void setUser(String user) throws UnsupportedEncodingException {
         credentials.add(0, new BasicNameValuePair(USER, user));
         auth_rq.setEntity(new UrlEncodedFormEntity(credentials));
     }
 
     /**
      * Get the current password
      *
      * @return the current password
      */
     public String getPassword() {
         return credentials.get(1).getValue();
     }
 
     /**
      * Set the password to use in the connection
      *
      * @param password the password
      * @throws UnsupportedEncodingException if password contain invalid
      */
     public void setPassword(String password) throws UnsupportedEncodingException {
         credentials.add(1, new BasicNameValuePair(PASSWORD, password));
         auth_rq.setEntity(new UrlEncodedFormEntity(credentials));
     }
 
     /**
      * Return the connection status of the agatte session.
      *
      * @return false if a login must be made
      */
     boolean mustLogin() {
         return ((this.session_id != null) && (this.session_expire < System.currentTimeMillis() + 120000));
     }
 }
