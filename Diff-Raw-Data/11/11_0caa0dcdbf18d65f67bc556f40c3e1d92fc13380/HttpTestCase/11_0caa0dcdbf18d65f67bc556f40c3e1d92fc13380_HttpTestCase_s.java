package com.aeroheart.plurk.client.tests;
 
 import java.net.HttpURLConnection;
 import java.util.concurrent.CountDownLatch;
 
 import junit.framework.Assert;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.test.InstrumentationTestCase;
 import android.util.Log;
 
import com.aeroheart.http.Request;
import com.aeroheart.http.Response;
import com.aeroheart.http.model.Model;
import com.aeroheart.http.util.UrlHelper;
 
 public class HttpTestCase extends InstrumentationTestCase {
     public void ignoretestPercentEncoding() {
         String encodedReserved = "%20%21%23%24%25%26%27%28%29%2A%2B%2C%2F%3A%3B%3D%3F%40%5B%5D",
                decodedReserved = " !#$%&'()*+,/:;=?@[]",
                unreserved      = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyz"
                                + "0123456789-_.~";
         
         String expected, actual;
         
         expected = String.format("%s%s", unreserved, encodedReserved);
         actual   = String.format("%s%s", unreserved, decodedReserved);
         Assert.assertEquals(expected, UrlHelper.percentEncode(actual));
         
         expected = String.format("%s%s", unreserved, decodedReserved);
         actual   = String.format("%s%s", unreserved, encodedReserved);
         Assert.assertEquals(expected, UrlHelper.percentDecode(actual));
     }
     
     public void ignoretestURLManipulation() {
         String  url   = "http://dev.sample-server.com/get.php",
                 query = "a=b&c=d%20a";
         Request request;
         
         request = new Request(String.format("%s?%s", url, query), Request.Method.GET, Response.Mode.SINGLE);
         Assert.assertEquals(url, request.getUrl());
         Assert.assertFalse("" == request.getQueryParamString());
         
         request = new Request(url, Request.Method.GET, Response.Mode.SINGLE);
         Assert.assertEquals(url, request.getUrl());
         Assert.assertEquals("", request.getQueryParamString());
     }
     
     public void ignoretestRequestGET() throws Throwable {
         final CountDownLatch signal = new CountDownLatch(1);
         
         this.runTestOnUiThread(new Runnable() {
             public void run() {
                 Request request = new Request(
                     "http://test.server.com:8000/params?x=1&y=2&z=3",
                     Request.Method.GET,
                     Response.Mode.SINGLE
                 );
                 
                 Assert.assertEquals("http://test.server.com:8000/params", request.getUrl());
                 Assert.assertEquals(Request.Method.GET.name(), request.getMethod());
                 
                 request.execute(new Response.Callback() {
                     public void onComplete(Request request, Response response) {
                         signal.countDown();
                         
                         try {
                             JSONObject body = new JSONObject(response.getBody());
                             
                             Assert.assertEquals(body.getInt("x"), 1);
                             Assert.assertEquals(body.getInt("y"), 2);
                             Assert.assertEquals(body.getInt("z"), 3);
                         }
                         catch (JSONException exception){}
                     }
                     
                     public void onSuccess(Request request, Response response) {
                     }
                     
                     public void onError(Request request, Response response) {
                     }
                 }, Model.class);
             }
         });
 
         signal.await();
     }
     
     public void ignoretestRequestPOST() throws Throwable {
         final CountDownLatch signal = new CountDownLatch(1);
         
         this.runTestOnUiThread(new Runnable() {
             public void run() {
                 Request request = new Request(
                     "http://test.server.com:8000/params",
                     Request.Method.POST,
                     Response.Mode.SINGLE
                 );
                 
                 request.addPostParam("x", "1");
                 request.addPostParam("y", "2");
                 request.addPostParam("z", "3");
                 
                 Assert.assertEquals("http://test.server.com:8000/params", request.getUrl());
                 Assert.assertEquals(Request.Method.POST.name(), request.getMethod());
                 
                 request.execute(new Response.Callback() {
                     public void onComplete(Request request, Response response) {
                         signal.countDown();
                         
                         try {
                             JSONObject body = new JSONObject(response.getBody());
                             
                             Assert.assertEquals(body.getInt("x"), 1);
                             Assert.assertEquals(body.getInt("y"), 2);
                             Assert.assertEquals(body.getInt("z"), 3);
                         }
                         catch (JSONException exception){}
                     }
                     
                     public void onSuccess(Request request, Response response) {
                     }
                     
                     public void onError(Request request, Response response) {
                         for (String header : response.getHeaderNames())
                             Log.d("Plurk/Tests", response.getHeaderValue(header));
 
                         Assert.fail("Request was treated as failure");
                     }
                 }, Model.class);
             }
         });
 
         signal.await();
     }
     
     public void ignoretestRequestRedirect() throws Throwable {
         final CountDownLatch signal = new CountDownLatch(1);
         
         this.runTestOnUiThread(new Runnable() {
             public void run() {
                 String  url     = "http://test.server.com:8000/source";
                 Request request = new Request(url, Request.Method.GET, Response.Type.JSON, Response.Mode.SINGLE);
                 
                 request.execute(new Response.Callback() {
                     public void onComplete(Request request, Response response) {
                         signal.countDown();
                         
                         Assert.assertEquals("{\"data\": \"redirect-view\"}", response.getBody());
                     }
                     
                     public void onSuccess(Request request, Response response) {
                         Log.i("Plurk/Tests", "Request treated as success");
                         
                         Assert.assertEquals(
                             "http://test.server.com:8000/redirected",
                             request.getUrl()
                         );
                     }
                     
                     public void onError(Request request, Response response) {
                         Assert.fail("Request was treated as failure");
                     }
                 }, Model.class);
             }
         });
         
         signal.await();
     }
     
     public void ignoretestRequestAuth() throws Throwable {
         final CountDownLatch signal = new CountDownLatch(3);
         
         final Response.Callback rechkCallback = new Response.Callback() {
             public void onComplete(Request request, Response response) {
                 signal.countDown();
             }
             
             public void onSuccess(Request request, Response response) {
                 Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
                 Assert.assertEquals("LOL", response.getBody());
             }
             
             public void onError(Request request, Response response) {
                 Assert.fail("Request failed");
             }
         };
         final Response.Callback checkCallback = new Response.Callback() {
             public void onComplete(Request request, Response response) {
                 signal.countDown();
             }
             
             public void onSuccess(Request request, Response response) {
                 Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
                 Assert.assertEquals("user-test", response.getBody());
                 
                 request = new Request(
                     "http://test.server.com:8000/user",
                     Request.Method.GET,
                     Response.Type.JSON,
                     Response.Mode.SINGLE
                 );
                 request.removeCookies();
                 request.execute(rechkCallback, Model.class);
             }
             
             public void onError(Request request, Response response) {
                 Assert.fail("Request failed");
             }
         };
         final Response.Callback loginCallback = new Response.Callback() {
             public void onComplete(Request request, Response response) {
                 signal.countDown();
             }
             
             public void onSuccess(Request request, Response response) {
                 Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
                 Assert.assertEquals("Logged In", response.getBody());
                 
                 new Request(
                     "http://test.server.com:8000/user",
                     Request.Method.GET,
                     Response.Type.JSON,
                     Response.Mode.SINGLE
                 ).execute(checkCallback, Model.class);
             }
             
             public void onError(Request request, Response response) {
                 Log.d("Plurk/Tests", response.getBody());
                 
                 Assert.fail("Request failed");
             }
         };
         
         this.runTestOnUiThread(new Runnable() {
            public void run() {
                Request request = new Request(
                    "http://test.server.com:8000/user/login",
                    Request.Method.POST,
                    Response.Type.JSON,
                    Response.Mode.SINGLE
                );
                
                
                Request.setCookieEnabled(true);
                request
                    .addPostParam("username", "user-test")
                    .addPostParam("password", "user-test")
                    .execute(loginCallback, Model.class);
            }
         });
         
         signal.await();
     }
 }
