 package com.novel.subscription;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.util.Log;
 
 public class HtmlService {
 
 	public static String getHtmlByGet(String _url){
		Log.i("com.novel.subscription", "get: " + _url);
 		String result = "";
 		HttpClient client = new DefaultHttpClient();
 		HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);   // ֹinvalid cookie headerĴ
         try {
             HttpGet get = new HttpGet(_url);
             get.setHeader("Accept-Charset", "utf-8"); 
             HttpResponse response = client.execute(get); 
             HttpEntity resEntity = response.getEntity();
             if (resEntity != null) {    
             	result = EntityUtils.toString(resEntity,"utf-8");
             }
         } catch (Exception e) {
         	Log.i("ReadList", "In getHtmlByGet: " + e.toString());
             e.printStackTrace();
         } finally {
         	client.getConnectionManager().shutdown();
         }
         return result;
     }
 }
