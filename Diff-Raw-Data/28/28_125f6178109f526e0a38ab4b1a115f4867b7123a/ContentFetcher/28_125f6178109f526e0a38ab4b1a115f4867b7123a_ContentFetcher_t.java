 package com.thoughtworks.orteroid.utilities;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import com.thoughtworks.orteroid.Callback;
 import com.thoughtworks.orteroid.constants.Constants;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ContentFetcher extends AsyncTask<String, Void, List<String>> {
 
     private Callback<List<String>> callback;
     private String responseType;
 
     public ContentFetcher(Callback<List<String>> callback, String responseType) {
         this.callback = callback;
         this.responseType = responseType;
     }
 
     @Override
     protected List<String> doInBackground(String... urls) {
         List<String> list = new ArrayList<String>();
         for (String url : urls) {
             list.add(response(url));
         }
         return list;
     }
 
     public String response(String url) {
         HttpClient httpClient = new DefaultHttpClient();
         HttpRequestBase httpRequest = httpRequest(url);
         String result = null;
         try {
             HttpResponse httpResponse = httpClient.execute(httpRequest);
             StatusLine statusLine = httpResponse.getStatusLine();
             int statusCode = statusLine.getStatusCode();
             if (statusCode >= 200 && statusCode <= 210) {
                 HttpEntity httpEntity = httpResponse.getEntity();
                 InputStream content = httpEntity.getContent();
                 result = toString(content);
             }
         } catch (IOException httpResponseError) {
             Log.e("HTTP Response", "IO error");
             return "404 error";
         }
         return result;
     }
 
     private HttpRequestBase httpRequest(String url) {
         if (responseType.equals(Constants.GET)) {
             return new HttpGet(url);
         } else if (responseType.equals(Constants.POST)) {
             return new HttpPost(url);
         } else {
             return new HttpPut(url);
         }
     }
 
     @Override
     protected void onPostExecute(List<String> resultString) {
         super.onPostExecute(resultString);
         callback.execute(resultString);
     }
 
     private String toString(InputStream content) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(content));
         StringBuilder result = new StringBuilder();
         String line;
         try {
             while ((line = reader.readLine()) != null) {
                 result.append(line);
             }
         } catch (IOException readerException) {
             readerException.printStackTrace();
         }
         return result.toString();
     }
 }
