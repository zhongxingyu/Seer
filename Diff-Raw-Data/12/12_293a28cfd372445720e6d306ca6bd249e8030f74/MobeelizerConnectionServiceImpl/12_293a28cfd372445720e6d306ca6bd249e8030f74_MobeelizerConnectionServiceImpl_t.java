 // 
 // MobeelizerConnectionServiceImpl.java
 // 
 // Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
 //
 // Mobeelizer SDK is free software; you can redistribute it and/or modify it 
 // under the terms of the GNU Affero General Public License as published by 
 // the Free Software Foundation; either version 3 of the License, or (at your
 // option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 // for more details.
 //
 // You should have received a copy of the GNU Affero General Public License 
 // along with this program; if not, write to the Free Software Foundation, Inc., 
 // 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 //
 
 package com.mobeelizer.java.connection;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mobeelizer.java.api.MobeelizerMode;
 import com.mobeelizer.java.api.user.MobeelizerUser;
 
 public class MobeelizerConnectionServiceImpl implements MobeelizerConnectionService {
 
     private static final String DEFAULT_TEST_URL = "https://cloud.mobeelizer.com/sync";
 
    private static final String DEFAULT_PRODUCTION_URL = "https://cloud.mobeelizer.com/sync";
 
     private final MobeelizerConnectionServiceDelegate delegate;
 
     public MobeelizerConnectionServiceImpl(final MobeelizerConnectionServiceDelegate delegate) {
         this.delegate = delegate;
     }
 
     @Override
     public MobeelizerAuthenticateResponse authenticate(final String login, final String password) throws IOException {
         return authenticate(login, password, null);
     }
 
     @Override
     public MobeelizerAuthenticateResponse authenticate(final String login, final String password, final String token)
             throws IOException {
         try {
 
             String url;
             if (token != null) {
                 url = getUrl("/authenticate", "deviceToken", token, "deviceType", "android");
             } else {
                 url = getUrl("/authenticate");
             }
             HttpGet request = new HttpGet(url);
 
             setHeaders(request, true, false);
 
             request.setHeader("mas-user-name", login);
             request.setHeader("mas-user-password", password);
 
             JSONObject json = executeAndGetJsonObject(request).getJSONObject("content");
 
             return new MobeelizerAuthenticateResponseImpl(json.getString("role"), json.getString("instanceGuid"));
         } catch (IOException e) {
             if (e.getMessage().contains("Authentication failure")) {
                 return null;
             }
             throw e;
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public List<String> getGroups() throws IOException {
         try {
             JSONArray json = executeGetAndGetJsonObject("/client/user/groups").getJSONArray("content");
 
             List<String> groups = new ArrayList<String>();
 
             for (int i = 0; i < json.length(); i++) {
                 groups.add(json.getJSONObject(i).getString("name"));
             }
 
             return groups;
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public List<MobeelizerUser> getUsers() throws IOException {
         try {
             JSONArray json = executeGetAndGetJsonObject("/client/user/list").getJSONArray("content");
 
             List<MobeelizerUser> users = new ArrayList<MobeelizerUser>(json.length());
 
             for (int i = 0; i < json.length(); i++) {
                 users.add(jsonObjectToUser(json.getJSONObject(i)));
             }
 
             return users;
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public MobeelizerUser getUser(final String login) throws IOException {
         try {
             JSONObject json = executeGetAndGetJsonObject("/client/user/get", "login", login).getJSONObject("content");
             return jsonObjectToUser(json);
         } catch (IOException e) {
             if (e.getMessage().contains("User '" + login + "' not found for vendor")) {
                 return null;
             }
             throw e;
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public void createUser(final MobeelizerUser user) throws IOException {
         try {
             executePostAndGetJsonObject("/client/user/create", userToJsonObject(user));
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public void updateUser(final MobeelizerUser user) throws IOException {
         try {
             executePostAndGetJsonObject("/client/user/update", userToJsonObject(user));
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public boolean deleteUser(final String login) throws IOException {
         executePostAndGetJsonObject("/client/user/delete", "login", login);
         return true;
     }
 
     @Override
     public String sendSyncAllRequest() throws IOException {
         try {
             return executePostAndGetJsonObject("/synchronizeAll").getString("content");
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public String sendSyncDiffRequest(final File outputFile) throws IOException {
         try {
             return executePostAndGetJsonObject("/synchronize", "file", outputFile).getString("content");
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     @Override
     public MobeelizerConnectionResult waitUntilSyncRequestComplete(final String ticket) throws IOException {
         try {
             for (int i = 0; i < 100; i++) {
                 JSONObject json = executeGetAndGetJsonObject("/checkStatus", new String[] { "ticket", ticket });
 
                 String status = json.getJSONObject("content").getString("status");
 
                 delegate.logInfo("Check task status: " + status);
 
                 if ("REJECTED".toString().equals(status)) {
                     String message = "Check task status success: " + status + " with result "
                             + json.getJSONObject("content").getString("result") + " and message '"
                             + json.getJSONObject("content").getString("message") + "'";
                     delegate.logInfo(message);
                     return MobeelizerConnectionResult.failure(message);
                 } else if ("FINISHED".toString().equals(status)) {
                     return MobeelizerConnectionResult.success();
                 }
 
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e) {
                     throw new IOException(e.getMessage(), e);
                 }
             }
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
 
         return MobeelizerConnectionResult.failure(null);
     }
 
     @Override
     public File getSyncData(final String ticket) throws IOException {
         return executeGetForFile("/data", "ticket", ticket);
     }
 
     @Override
     public void confirmTask(final String ticket) throws IOException {
         executePostAndGetJsonObject("/confirm", "ticket", ticket);
     }
 
     @Override
     public void registerForRemoteNotifications(final String token) throws IOException {
         executePostAndGetJsonObject("/registerPushToken", "deviceToken", token, "deviceType", "android");
         delegate.logInfo("Registered for remote notifications with token: " + token);
     }
 
     @Override
     public void unregisterForRemoteNotifications(final String token) throws IOException {
         executePostAndGetJsonObject("/unregisterPushToken", "deviceToken", token, "deviceType", "android");
         delegate.logInfo("Unregistered for remote notifications with token: " + token);
     }
 
     @Override
     public void sendRemoteNotification(final String device, final String group, final List<String> users,
             final Map<String, String> notification) throws IOException {
         try {
             JSONObject object = new JSONObject();
             StringBuilder logBuilder = new StringBuilder();
             logBuilder.append("Sent remote notification ").append(notification).append(" to");
             if (device != null) {
                 object.put("device", device);
                 logBuilder.append(" device: ").append(device);
             }
             if (group != null) {
                 object.put("group", group);
                 logBuilder.append(" group: ").append(group);
             }
             if (users != null) {
                 object.put("users", new JSONArray(users));
                 logBuilder.append(" users: ").append(users);
             }
             if (device == null && group == null && users == null) {
                 logBuilder.append(" everyone");
             }
             object.put("notification", new JSONObject(notification));
             executePostAndGetJsonObject("/push", object);
             delegate.logInfo(logBuilder.toString());
         } catch (JSONException e) {
             throw new IOException(e.getMessage(), e);
         }
     }
 
     private MobeelizerUser jsonObjectToUser(final JSONObject json) throws JSONException {
         MobeelizerUser user = new MobeelizerUser();
         user.setLogin(json.getString("login"));
         user.setGroup(json.getString("group"));
         user.setMail(json.getString("mail"));
         user.setAdmin("true".equals(json.getString("admin")));
         return user;
     }
 
     private JSONObject userToJsonObject(final MobeelizerUser user) throws JSONException {
         JSONObject json = new JSONObject();
         json.put("login", user.getLogin());
         json.put("group", user.getGroup());
         json.put("password", user.getPassword());
         json.put("mail", user.getMail());
         json.put("admin", user.isAdmin() ? "true" : "false");
         return json;
     }
 
     private JSONObject executeGetAndGetJsonObject(final String path, final String... params) throws IOException {
         HttpGet request = new HttpGet(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetJsonObject(request);
     }
 
     private JSONObject executePostAndGetJsonObject(final String path, final String... params) throws IOException {
         HttpPost request = new HttpPost(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetJsonObject(request);
     }
 
     private File executeGetForFile(final String path, final String... params) throws IOException {
         HttpGet request = new HttpGet(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetFile(request);
     }
 
     private JSONObject executePostAndGetJsonObject(final String path, final JSONObject body) throws IOException {
         HttpPost request = new HttpPost(getUrl(path, new String[0]));
         request.setEntity(new StringEntity(body.toString(), "UTF-8"));
 
         setHeaders(request, true, true);
 
         return executeAndGetJsonObject(request);
     }
 
     private JSONObject executePostAndGetJsonObject(final String path, final String name, final File file) throws IOException {
         HttpPost request = new HttpPost(getUrl(path, new String[0]));
 
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         entity.addPart(name, new InputStreamBody(new FileInputStream(file), name));
         request.setEntity(entity);
 
         setHeaders(request, false, true);
 
         return executeAndGetJsonObject(request);
     }
 
     private void setHeaders(final HttpRequestBase request, final boolean setJsonContentType, final boolean setUserPassword) {
         if (setJsonContentType) {
             request.setHeader("content-type", "application/json");
         }
         request.setHeader("mas-vendor-name", delegate.getVendor());
         request.setHeader("mas-application-name", delegate.getApplication());
         request.setHeader("mas-application-instance-name", delegate.getInstance());
         request.setHeader("mas-definition-digest", delegate.getVersionDigest());
         request.setHeader("mas-device-name", delegate.getDevice());
         request.setHeader("mas-device-identifier", delegate.getDeviceIdentifier());
         if (setUserPassword) {
             request.setHeader("mas-user-name", delegate.getUser());
             request.setHeader("mas-user-password", delegate.getPassword());
         }
         request.setHeader("mas-sdk-version", delegate.getSdkVersion());
     }
 
     private String getUrl(final String path, final String... params) {
         return getUrl() + path + createQuery(params);
     }
 
     private String getUrl() {
         if (delegate.getUrl() != null) {
             return delegate.getUrl();
         } else if (delegate.getMode().equals(MobeelizerMode.PRODUCTION)) {
             return DEFAULT_PRODUCTION_URL;
         } else {
             return DEFAULT_TEST_URL;
         }
     }
 
     private String createQuery(final String[] params) {
         if (params.length > 0) {
             List<NameValuePair> qparams = new ArrayList<NameValuePair>();
             for (int i = 0; i < params.length; i += 2) {
                 qparams.add(new BasicNameValuePair(params[i], params[i + 1]));
             }
             return "?" + URLEncodedUtils.format(qparams, "UTF-8");
         } else {
             return "";
         }
     }
 
     private HttpClient httpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 10000);
        return new DefaultHttpClient(params);
     }
 
     private JSONObject executeAndGetJsonObject(final HttpRequestBase request) throws IOException {
         HttpClient client = httpClient();
 
         InputStream is = null;
         Reader reader = null;
 
         if (!delegate.isNetworkAvailable()) {
             throw new IOException("Cannot execute HTTP request, network connection not available");
         }
 
         delegate.setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 HttpEntity entity = response.getEntity();
 
                 if (entity == null) {
                     throw new IOException("Connection failure: entity not found.");
                 }
 
                 is = entity.getContent();
 
                 Writer writer = new StringWriter();
 
                 char[] buffer = new char[1024];
 
                 reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
 
                 JSONObject json = new JSONObject(writer.toString());
 
                 if ("OK".equals(json.getString("status"))) {
                     return json;
                 } else if ("ERROR".equals(json.getString("status"))) {
                     throw new IOException("Connection failure: " + json.getJSONObject("content").getString("message"));
                 } else {
                     throw new IOException("Connection failure: " + json.getString("content"));
                 }
             } else {
                 throw new IOException("Connection failure: " + response.getStatusLine().getStatusCode() + ".");
             }
         } catch (JSONException e) {
             throw new IOException("Connection failure: " + e.getMessage(), e);
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             client.getConnectionManager().shutdown();
         }
     }
 
     private File executeAndGetFile(final HttpRequestBase request) throws IOException {
         HttpClient client = httpClient();
 
         BufferedInputStream in = null;
         BufferedOutputStream out = null;
 
         if (!delegate.isNetworkAvailable()) {
             throw new IOException("Cannot execute HTTP request, network connection not available");
         }
 
         delegate.setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 HttpEntity entity = response.getEntity();
 
                 if (entity == null) {
                     throw new IOException("Connection failure: entity not found.");
                 }
 
                 in = new BufferedInputStream(entity.getContent());
 
                 File file = File.createTempFile("mobeelizer", "response");
 
                 out = new BufferedOutputStream(new FileOutputStream(file));
 
                 byte[] buffer = new byte[4096];
                 int n = -1;
 
                 while ((n = in.read(buffer)) != -1) {
                     out.write(buffer, 0, n);
                 }
 
                 return file;
             } else {
                 throw new IOException("Connection failure: " + response.getStatusLine().getStatusCode() + ".");
             }
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             client.getConnectionManager().shutdown();
         }
     }
 
 }
