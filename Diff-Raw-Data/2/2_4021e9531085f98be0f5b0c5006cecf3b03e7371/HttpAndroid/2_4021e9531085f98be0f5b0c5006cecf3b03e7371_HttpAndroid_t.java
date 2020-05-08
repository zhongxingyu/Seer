 /**
  * Copyright 2012 The PlayN Authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package playn.android;
 
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import playn.core.AbstractPlatform;
 import playn.core.util.Callback;
 import playn.http.Http;
 import playn.http.HttpException;
 import playn.http.HttpMethod;
 import playn.http.HttpRequest;
 import playn.http.HttpResponse;
 
 /**
  * Android-specific implementation of {@link Http}.
  *
  * @author Inderjeet Singh
  */
 public class HttpAndroid extends Http {
 
   private final AbstractPlatform platform;
 
   public HttpAndroid(AbstractPlatform platform) {
     this.platform = platform;
   }
 
   @Override
   protected void doSend(final HttpRequest request, final Callback<HttpResponse> callback) {
     new Thread("HttpAndroid.doSend") {
       public void run() {
         HttpClient httpclient = new DefaultHttpClient();
         HttpRequestBase req;
         HttpMethod method = request.getMethod();
         String url = request.getUrl();
         switch (method) {
         case GET:
           req = new HttpGet(url);
           break;
         case POST:
           req = new HttpPost(url);
           break;
         case PUT:
           req = new HttpPut(url);
           break;
         default: throw new UnsupportedOperationException(method.toString());
         }
         String requestBody = request.getBody();
        if (requestBody != null && req instanceof HttpEntityEnclosingRequestBase) {
           try {
             HttpEntityEnclosingRequestBase op = (HttpEntityEnclosingRequestBase) req;
             op.setEntity(new StringEntity(requestBody));
           } catch (UnsupportedEncodingException e) {
             platform.notifyFailure(callback, e);
           }
         }
         for (Map.Entry<String, String> header : request.getHeaders()) {
           req.setHeader(header.getKey(), header.getValue());
         }
         int statusCode = -1;
         String statusLineMessage = null;
         Map<String, String> responseHeaders = new HashMap<String, String>();
         String responseBody = null;
         try {
           org.apache.http.HttpResponse response = httpclient.execute(req);
           StatusLine statusLine = response.getStatusLine();
           statusCode = statusLine.getStatusCode();
           statusLineMessage = statusLine.getReasonPhrase();
           for (Header header : response.getAllHeaders()) {
             responseHeaders.put(header.getName(), header.getValue());
           }
           responseBody = EntityUtils.toString(response.getEntity());
           HttpResponse httpResponse = new HttpResponse(
               statusCode, statusLineMessage, responseHeaders, responseBody);
           platform.notifySuccess(callback, httpResponse);
         } catch (Throwable cause) {
           HttpException reason = new HttpException(statusCode, statusLineMessage, responseBody, cause);
           platform.notifyFailure(callback, reason);
         }
       }
     }.start();
   }
 }
