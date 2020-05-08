 package com.amgems.uwschedule.api.uw;
 
 import android.util.Log;
 import com.amgems.uwschedule.R;
 import com.amgems.uwschedule.services.LoginService;
 import com.amgems.uwschedule.util.NetUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by zac on 8/28/13.
  */
 public class LoginAuthenticator {
 
     private static final Pattern HIDDEN_PARAMS = Pattern.compile("<input type=\"hidden\" name=\"(.+)\" value=\"(.*)\">");
 
     private List<String> mCookies;
     private Response mResponse;
     private final String mUsername;
     private final String mPassword;
 
     public static enum Response {
         OK(R.string.login_response_ok),
         AUTHENTICATION_ERROR(R.string.login_response_auth_error),
         SERVER_ERROR(R.string.login_response_server_error),
         TIMEOUT_ERROR(R.string.login_response_timeout_error),
         NETWORK_ERROR(R.string.login_response_network_error);
 
         /**
          * Resource ID for a suitable string corresponding
          * to the given response
          */
         private final int mStringResId;
 
         Response(int stringResId) {
             mStringResId = stringResId;
         }
 
         public int getStringResId() {
             return mStringResId;
         }
     }
 
     LoginAuthenticator(String username, String password) {
         mUsername = username;
         mPassword = password;
     }
 
     public static LoginAuthenticator newInstance(String username, String password) {
         return new LoginAuthenticator(username, password);
     }
 
     public void execute() {
 
         try {
             URL loginUrl = new URL(NetUtils.LOGIN_REQUEST_URL);
             List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 
             HttpURLConnection connection = NetUtils.getInputConnection(loginUrl);
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
             try {
                 postParameters.add(new BasicNameValuePair("user", mUsername));
                 postParameters.add(new BasicNameValuePair("pass", mPassword));
                 captureHiddenParameters(reader, postParameters);
             } finally {
                 connection.disconnect();
                 reader.close();
             }
 
             connection = NetUtils.getOutputConnection(loginUrl);
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
 
             try {
                 List<String> cookie = getAuthCookies(connection, bufferedWriter, postParameters);
                 if (cookie != null) {
                     synchronized (cookie) {
                         mCookies = cookie;
                     }
                 }
             } finally {
                 connection.disconnect();
             }
 
         } catch (MalformedURLException e) {
             mResponse = Response.SERVER_ERROR;
         } catch (IOException e) {
             mResponse = Response.NETWORK_ERROR;
         }
 
         if (mResponse == null) {
             if (mCookies != null) {
                 mResponse = Response.OK;
             } else {
                 mResponse = Response.AUTHENTICATION_ERROR;
             }
         }
     }
 
     public String getCookie() {
         String cookieValue;
         if (mCookies != null) {
             synchronized (mCookies) {
                 cookieValue = mCookies.toString();
                mCookies = null;
             }
         } else {
             cookieValue = null;
         }
         return cookieValue;
     }
 
     public Response getResponse() {
         return mResponse;
     }
 
     private void captureHiddenParameters (BufferedReader reader,
                                                  List<? super NameValuePair> destHiddenParams) throws IOException {
         String line;
         while ((line = reader.readLine()) != null) {
             Matcher parameterMatcher = HIDDEN_PARAMS.matcher(line);
             if (parameterMatcher.matches()) {
                 destHiddenParams.add(new BasicNameValuePair(parameterMatcher.group(1), parameterMatcher.group(2)));
             }
         }
     }
 
     private List<String> getAuthCookies (HttpURLConnection connection, BufferedWriter writer,
                                                 List<? extends NameValuePair> postParams) throws IOException {
         writer.write(toQueryString(postParams));
         writer.flush();
 
         return connection.getHeaderFields().get("Set-Cookie");
     }
 
     private String toQueryString (List<? extends NameValuePair> postParameterPairs) {
         StringBuilder builder = new StringBuilder();
         boolean firstParameter = true;
 
         try {
             for (NameValuePair postParameterPair : postParameterPairs) {
                 if (!firstParameter)
                     builder.append("&");
                 firstParameter = false;
 
                 builder.append(URLEncoder.encode(postParameterPair.getName(), NetUtils.CHARSET));
                 builder.append("=");
                 builder.append(URLEncoder.encode(postParameterPair.getValue(), NetUtils.CHARSET));
             }
         } catch (UnsupportedEncodingException e) {
             Log.e(LoginService.class.getSimpleName(), e.getMessage());
         }
 
         return builder.toString();
     }
 }
