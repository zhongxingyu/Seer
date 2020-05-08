 package com.home.giraffe.network;
 
 import com.google.inject.Inject;
 import com.home.giraffe.events.SettingsClearedEvent;
 import com.home.giraffe.interfaces.IConnector;
 import com.home.giraffe.interfaces.ISettingsManager;
 import com.home.giraffe.utils.Utils;
 import de.greenrobot.event.EventBus;
 import org.apache.http.*;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 public class Connector implements IConnector {
     private ISettingsManager mSettingsManager;
     private DefaultHttpClient mHttpClient;
 
     @Inject
     public Connector(ISettingsManager settingsManager, EventBus eventBus) {
         Utils.d("Connector created");
 
         mSettingsManager = settingsManager;
         eventBus.register(this);
 
         recreate();
     }
 
     public void recreate(){
         mHttpClient = getHttpClient();
         mHttpClient.setRedirectHandler(new DefaultRedirectHandler() {
             @Override
             public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                 return false;
             }
         });
 
         // Proxy settings
         // HttpHost proxy = new HttpHost("192.168.1.103", 8888);
         // mHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
     }
 
     public void onEvent(SettingsClearedEvent event) {
         Utils.d("Recreate HttpClient on SettingsClearedEvent");
 
         recreate();
     }
 
     private DefaultHttpClient getHttpClient() {
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 
         HttpParams params = new BasicHttpParams();
         params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(20));
         params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
         params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, false);
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 
         HttpConnectionParams.setTcpNoDelay(params, true);
         HttpConnectionParams.setConnectionTimeout(params, 15 * 1000);
         HttpConnectionParams.setSoTimeout(params, 3 * 60 * 1000);
 
         ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
         return new DefaultHttpClient(manager, params);
     }
 
     private String cutSecurityString(String response) {
         return response.replaceFirst("throw.*;\\s*", "");
     }
 
     private byte[] proceedBytesResponse(HttpResponse response) throws Exception {
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK &&
                 response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED &&
                 response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
             throw new Exception(response.getStatusLine().getReasonPhrase());
         }
 
         HttpEntity entity = getEntityFromResponse(response);
         return EntityUtils.toByteArray(entity);
     }
 
     private com.home.giraffe.network.HttpResponse proceedResponse(HttpResponse response) throws Exception {
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK &&
                 response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED &&
                 response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
             throw new Exception(response.getStatusLine().getReasonPhrase());
         }
 
         HttpEntity entity = getEntityFromResponse(response);
         CookieStore cookieStore = mHttpClient.getCookieStore();
         Cookie[] cookies = cookieStore.getCookies().toArray(new Cookie[cookieStore.getCookies().size()]);
         cookieStore.clear();
         String body = cutSecurityString(EntityUtils.toString(entity, HTTP.UTF_8));
         return new com.home.giraffe.network.HttpResponse(response.getAllHeaders(), cookies, body);
     }
 
     private HttpEntity getEntityFromResponse(HttpResponse response) {
         Header[] contentEncodings = response.getHeaders("Content-Encoding");
         if (contentEncodings != null) {
             for (Header header : contentEncodings) {
                 if (header.getValue().equalsIgnoreCase("gzip")) {
                     return new NetworkUtils.GzipDecompressingEntity(response.getEntity());
                 }
             }
         }
 
         return response.getEntity();
     }
 
     @Override
     public com.home.giraffe.network.HttpResponse getRequest(String url) throws Exception {
         Utils.d("Trying to make get request to url: " + url);
 
         HttpGet httpGet = new HttpGet(url);
         addCommonHeaders(httpGet);
         HttpResponse response = mHttpClient.execute(httpGet);
         return proceedResponse(response);
     }
 
     @Override
     public com.home.giraffe.network.HttpResponse postRequest(String url, String body) throws Exception {
         Utils.d("Trying to make post request to url: " + url);
 
         StringEntity entity = new StringEntity(body, HTTP.UTF_8);
         HttpPost httpPost = new HttpPost(url);
         httpPost.setEntity(entity);
         addCommonHeaders(httpPost);
 
         httpPost.addHeader("Content-Type",
                 mSettingsManager.isLoggedOn() ?
                         "application/json; charset=UTF-8" :
                         "application/x-www-form-urlencoded");
 
         HttpResponse response = mHttpClient.execute(httpPost);
         return proceedResponse(response);
     }
 
     @Override
     public byte[] getData(String url) throws Exception {
         Utils.d("Trying to get binary data from url: " + url);
 
         HttpGet httpGet = new HttpGet(url);
         addCommonHeaders(httpGet);
 
         httpGet.addHeader("Content-Type", "binary/octet-stream");
 
         HttpResponse response = mHttpClient.execute(httpGet);
         return proceedBytesResponse(response);
     }
 
     private void addCommonHeaders(HttpRequestBase requestBase) {
         requestBase.addHeader("Accept-Encoding", "gzip");
         requestBase.addHeader("User-Agent", "Giraffe/1.0");
         requestBase.addHeader("Accept", "*/*");
         requestBase.addHeader("Cookie", Utils.getAuthorizationCookie());
     }
 }
