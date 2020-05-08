 /**
  * 
  */
 package ru.altruix.commons.android;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
import co.altruix.ccp.android.api.IWebServiceTaskHelper;

 import android.util.Log;
 
 /**
  * @author DP118M
  *
  */
 public class WebServiceTaskHelper implements IWebServiceTaskHelper {
     private static final String TAG = WebServiceTaskHelper.class.getSimpleName();
 
     private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
     
     private int taskType;
     
     @Override
 	public void addNameValuePair(String name, String value) {
         params.add(new BasicNameValuePair(name, value));
     }
 
     
     @Override
 	public HttpParams getHttpParams() {
          
         final HttpParams htpp = new BasicHttpParams();
          
         HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
         HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);
          
         return htpp;
     }
      
     @Override
     public HttpResponse doResponse(final String aUrl) {
          
         // Use our connection and data timeouts as parameters for our
         // DefaultHttpClient
         HttpClient httpclient = new DefaultHttpClient(getHttpParams());
 
         HttpResponse response = null;
 
         try {
             switch (taskType) {
 
             case POST_TASK:
                 HttpPost httppost = new HttpPost(aUrl);
                 // Add parameters
                 httppost.setEntity(new UrlEncodedFormEntity(params));
 
                 response = httpclient.execute(httppost);
                 break;
             case GET_TASK:
                 HttpGet httpget = new HttpGet(aUrl);
                 response = httpclient.execute(httpget);
                 break;
             }
         } catch (Exception e) {
 
             Log.e(TAG, e.getLocalizedMessage(), e);
 
         }
 
         return response;
     }
      
     @Override
     public String inputStreamToString(final InputStream is) {
         String line = "";
         final StringBuilder total = new StringBuilder();
 
         final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 
         try {
             while ((line = rd.readLine()) != null) {
                 total.append(line);
             }
         } catch (IOException e) {
             Log.e(TAG, e.getLocalizedMessage(), e);
         }
 
         return total.toString();
     }
 
 	@Override
 	public void setTaskType(int taskType) {
 		this.taskType = taskType;
 	}
 
 }
