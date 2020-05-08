 package com.androidhelpers.networking.http;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ap4y
  * Date: 3/15/12
  * Time: 5:23 PM
  */
 public class HttpRequestAsync extends HttpRequest {
 
     public HttpRequestAsync(String url, Map<String, String> params, HttpMethods method) throws IOException {
         super(url, params, method);
 
         handler = new HttpHandler();
     }
 
     public Runnable create() {
         return new Runnable() {
             @Override
             public void run() {
            try {
                start();
            } catch (IOException e) {
                getHandler().postException(e);
            }
             }
         };
     }
 }
