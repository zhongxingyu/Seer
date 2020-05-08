 package com.siu.android.andutils.http;
 
 import ch.boye.httpclientandroidlib.HttpResponse;
 import ch.boye.httpclientandroidlib.client.HttpClient;
 import ch.boye.httpclientandroidlib.client.methods.HttpRequestBase;
 import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
 import ch.boye.httpclientandroidlib.params.BasicHttpParams;
 import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
 import ch.boye.httpclientandroidlib.params.HttpParams;
 
 import java.io.IOException;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class HttpManager {
 
     private static HttpManager instance;
 
     private HttpClient httpClient;
     private boolean activeConnections;
 
     private HttpManager() {
     }
 
     public static synchronized HttpManager getInstance() {
         if (null == instance) {
             instance = new HttpManager();
         }
 
         return instance;
     }
 
     public HttpClient getHttpClient() {
         synchronized (this) {
             if (null == httpClient) {
                 HttpParams httpParams = new BasicHttpParams();
                 HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
                 httpClient = new DefaultHttpClient(httpParams);
             }
         }
 
         return httpClient;
     }
 
     public HttpResponse execute(HttpRequestBase request) throws IOException {
         activeConnections = true;
         return getHttpClient().execute(request);
     }
 
     public synchronized void closeActivesConnexions() {
         if (activeConnections) {
             getHttpClient().getConnectionManager().shutdown();
            activeConnections = false;
         }
     }
 }
