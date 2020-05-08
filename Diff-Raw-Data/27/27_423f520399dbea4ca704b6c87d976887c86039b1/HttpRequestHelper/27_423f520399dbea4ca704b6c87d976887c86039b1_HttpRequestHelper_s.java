 package ru.terra.jbrss.network;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import roboguice.inject.ContextSingleton;
 import ru.terra.jbrss.constants.Constants;
 import ru.terra.jbrss.core.SettingsService;
 import ru.terra.jbrss.core.helper.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.List;
 
 //Синхронные вызовы rest сервисов, вызывать только внутри асинхронной таски
 @ContextSingleton
 public class HttpRequestHelper {
     private HttpClient hc;
     @Inject
     @Named("jbrssServer")
     private String baseAddress = "";
 
     @Inject
     private SettingsService settingsService;
 
     @Inject
     public HttpRequestHelper() {
         hc = new DefaultHttpClient();
         hc.getParams().setParameter("http.protocol.content-charset", "UTF-8");
     }
 
     public JsonResponce runSimpleJsonRequest(String uri) throws IOException, UnableToLoginException {
         HttpGet httpGet = new HttpGet(baseAddress + uri);
         return runRequest(httpGet);
     }
 
     private JsonResponce runRequest(HttpUriRequest httpRequest) throws UnableToLoginException, IOException {
         StringBuilder builder = new StringBuilder();
 
         httpRequest.setHeader("Cookie", "JSESSIONID=" + settingsService.getSetting(Constants.CONFIG_SESSION, ""));
         for (Header h : httpRequest.getAllHeaders()) {
             Logger.i("runRequest", "header: " + h.getName() + " = " + h.getValue());
         }
         Logger.i("runRequest", "header: " + httpRequest.getHeaders("Cookie"));
         HttpResponse response = null;
         try {
             response = hc.execute(httpRequest);
         } catch (ConnectException e) {
             Logger.w("HttpRequestHelper", "Connect exception " + e.getMessage());
             return null;
         } catch (IllegalStateException e) {
             Logger.w("HttpRequestHelper", "IllegalStateException " + e.getMessage());
             return null;
         }
         StatusLine statusLine = response.getStatusLine();
         int statusCode = statusLine.getStatusCode();
         // Logger.i("HttpRequestHelper", "Received status code " +
         // statusCode);
         JsonResponce ret = new JsonResponce();
         ret.code = statusCode;
         if (HttpStatus.SC_OK == statusCode) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             String line;
             while ((line = reader.readLine()) != null) {
                 builder.append(line);
             }
         } else {
 
         }
 
         ret.json = builder.toString();
         return ret;
     }
 
     public JsonResponce runJsonRequest(String uri, NameValuePair... params) throws IOException, UnableToLoginException {
         HttpPost request = new HttpPost(baseAddress + uri);
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         for (int i = 0; i < params.length; ++i) {
             nameValuePairs.add(params[i]);
         }
 
         request.addHeader("Content-Type", "application/x-www-form-urlencoded");
         UrlEncodedFormEntity entity;
         try {
             entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
             entity.setContentType("appplication/x-www-form-urlencoded");
             request.setEntity(entity);
 
             return runRequest(request);
         } catch (UnsupportedEncodingException e) {
             Logger.w("HttpRequestHelper", "Failed to form request content" + e.getMessage());
             return new JsonResponce("");
         }
     }
 
     public <T> T getForObject(String url, Class<T> targetClass, NameValuePair... params) throws IOException, UnableToLoginException {
         JsonResponce ret = runJsonRequest(url, params);
         if (ret.code == HttpStatus.SC_FORBIDDEN) {
             throw new UnableToLoginException();
         }
         String json = ret.json;
         Logger.i("getForObject", url + " => " + json);
         return new Gson().fromJson(json, targetClass);
     }
 }
