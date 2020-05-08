 package com.artcom.y60.http;
 
 import java.util.Map;
 
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public abstract class AsyncHttpRequestWithBody extends AsyncHttpRequest {
     
     private static final String LOG_TAG = "AsyncHttpRequestWithBody";
     
     public AsyncHttpRequestWithBody(String pUrl) {
         super(pUrl);
     }
     
     public AsyncHttpRequestWithBody(String pUrl, DefaultHttpClient pHttpClient) {
         super(pUrl, pHttpClient);
     }
     
     public void setBody(String pStringData) {
        HttpHelper.insert(pStringData, "text/plain", getRequest());
     }
     
     public void setBody(Map<String, String> params) {
         HttpHelper.insert(HttpHelper.urlEncodeValues(params), "application/x-www-form-urlencoded",
                 getRequest());
     }
     
     @Override
     protected HttpEntityEnclosingRequestBase getRequest() {
         return (HttpEntityEnclosingRequestBase) super.getRequest();
     }
 }
