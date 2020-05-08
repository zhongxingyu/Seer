 package com.onelightstudio.velibnroses.ws;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class WSRequest extends AsyncTask<Void, Void, JSONObject> {
 
     private static final int TIMEOUT = 30;
 
     private Context context;
     private String resource;
     private List<NameValuePair> params;
    private WSHandler handler;
     private Exception exception;
 
     public WSRequest(Context context, String resource) {
         this.context = context;
         this.resource = resource;
         params = new ArrayList<NameValuePair>();
        handler = new WSDefaultHandler() {
            @Override
            public void onResult(Context context, JSONObject result) {
            }
        };
     }
 
     public void call() {
         execute();
     }
 
    public WSRequest handleWith(WSHandler handler) {
         if (handler != null) {
             this.handler = handler;
         }
         return this;
     }
 
     public WSRequest withParam(String name, Object value) {
         if (value != null) {
             params.add(new BasicNameValuePair(name, String.valueOf(value)));
         }
         return this;
     }
 
     @Override
     protected JSONObject doInBackground(Void... nothing) {
         JSONObject result = null;
         String uri = getURI();
         if (uri != null) {
             try {
                 HttpParams httpParams = new BasicHttpParams();
                 HttpConnectionParams.setConnectionTimeout(httpParams,
                         TIMEOUT * 1000);
                 HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT * 1000);
                 DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
                 HttpUriRequest request = new HttpGet(uri);
                 String resultStr = httpClient.execute(request,
                         new ResponseHandler<String>() {
 
                             @Override
                             public String handleResponse(HttpResponse response)
                                     throws ClientProtocolException, IOException {
                                 StatusLine statusLine = response
                                         .getStatusLine();
                                 if (statusLine.getStatusCode() >= 300) {
                                     throw new HttpResponseException(statusLine
                                             .getStatusCode(), statusLine
                                             .getReasonPhrase());
                                 }
                                 HttpEntity entity = response.getEntity();
 
                                 String res = null;
                                 if (entity != null) {
                                     res = EntityUtils.toString(entity,
                                             HTTP.UTF_8);
                                 }
                                 return res;
                             }
                         });
                 if (resultStr != null && !"".equals(resultStr.trim())) {
                     result = new JSONObject(resultStr);
                 }
             } catch (Exception e) {
                 exception = e;
             }
         }
         return result;
     }
 
     protected String getURI() {
         StringBuilder uriBuilder = new StringBuilder(resource);
         if (!params.isEmpty()) {
             uriBuilder.append("?");
             Iterator<NameValuePair> it = params.iterator();
             while (it.hasNext()) {
                 NameValuePair param = it.next();
                 try {
                     uriBuilder
                             .append(URLEncoder.encode(param.getName(), "UTF8"))
                             .append("=")
                             .append(URLEncoder.encode(param.getValue(), "UTF8"));
                 } catch (UnsupportedEncodingException e) {
                 }
                 if (it.hasNext()) {
                     uriBuilder.append("&");
                 }
             }
         }
         return uriBuilder.toString();
     }
 
     @Override
     protected void onCancelled() {
         handler.onCancelled(context);
     }
 
     @Override
     protected void onPostExecute(JSONObject result) {
         handler.doAfter(context);
         if (exception != null) {
             if (exception instanceof HttpResponseException) {
                 handler.onError(context,
                         ((HttpResponseException) exception).getStatusCode());
             } else {
                 handler.onException(context, exception);
             }
         } else {
             handler.onResult(context, result);
         }
     }
 
     @Override
     protected void onPreExecute() {
         handler.doBefore(context);
     }
 }
