 package edu.gatech.cs4261.LAWN;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import android.net.Uri;
 
 public class APIWifiDiscover extends DeviceDiscover {
     private static final String protocolType = "APIWifiDiscover"; 
     private static final String TAG = "APIWifiDiscover"; 
     
     public APIWifiDiscover() {
         Log.v(TAG, "Constructor called");
     }
     
     @Override
     public String getProtocolType() {        
         return protocolType;
     }
 
     @Override
     public boolean isServiceAvailable() {
         
         return false;
     }
 
     @Override
     public boolean scan(double lat, double lon) {
        String userPage = "Some userpage error";
        String apPage ="Some ap error";
         try {
            userPage = getUserData("hbaker3");
            apPage = getAPData("50-348");
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             Log.e(TAG, e.toString());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             Log.e(TAG, e.toString());
         }
        Log.d(TAG, userPage);
         return false;
     }
     
     private String getUserData(String username) throws ClientProtocolException, IOException{
         String urlBase = "http://gardener.gatech.edu/whereami/getUserAP.php?User=";
         //TODO escape things somewhere 
         String url = urlBase + username;
         HttpContext httpContext = new BasicHttpContext();
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(url);
         String page = "Page was not created check for an error";
         HttpResponse response = httpClient.execute(httpGet, httpContext);
         int statusCode = response.getStatusLine().getStatusCode();
         Log.d(TAG, "Status code: " +statusCode);
         HttpEntity entity = response.getEntity();
         page = EntityUtils.toString(entity);        
         return page;
         
     }
    
    private String getAPData(String apName) throws ClientProtocolException, IOException{
        String urlBase = "http://gardener.gatech.edu/whereami/getAPStatus.php?APName=";
        //TODO escape things somewhere 
        String url = urlBase + apName;
        String page = "Page was not created check for an error";
        HttpContext httpContext = new BasicHttpContext();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        int statusCode = response.getStatusLine().getStatusCode();
        Log.d(TAG, "Status code: " +statusCode);
        
        HttpEntity entity = response.getEntity();
        page = EntityUtils.toString(entity);        
        return page;
        
    }
 }
