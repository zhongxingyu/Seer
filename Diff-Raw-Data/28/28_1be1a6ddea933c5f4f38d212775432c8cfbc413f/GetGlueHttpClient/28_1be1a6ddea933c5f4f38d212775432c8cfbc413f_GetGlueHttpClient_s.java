 /*
  * Copyright 2013 Uwe Trottmann
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.uwetrottmann.getglue.client;
 
 import com.squareup.okhttp.OkHttpClient;
 
 import org.apache.oltu.oauth2.client.HttpClient;
 import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
 import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
 import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
 import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
 import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
 import org.apache.oltu.oauth2.common.utils.OAuthUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 
 /**
  * Using a custom {@link org.apache.oltu.oauth2.client.HttpClient} implementation which can follow
  * protocol redirects, as GetGlue redirects from https to http once the grant code was accepted.
  */
 public class GetGlueHttpClient implements HttpClient {
 
     @Override
     public <T extends OAuthClientResponse> T execute(OAuthClientRequest request,
             Map<String, String> headers, String requestMethod, Class<T> responseClass)
             throws OAuthSystemException, OAuthProblemException {
         String responseBody = null;
         HttpURLConnection connection = null;
         int responseCode = 0;
 
         try {
            OkHttpClient client = new OkHttpClient();
             connection = client.open(new URL(request.getLocationUri()));
             connection.setConnectTimeout(15 * 1000 /* milliseconds */);
             connection.setReadTimeout(20 * 1000 /* milliseconds */);
 
             responseCode = -1;
 
             if (headers != null && !headers.isEmpty()) {
                 for (Map.Entry<String, String> header : headers.entrySet()) {
                     connection.addRequestProperty(header.getKey(), header.getValue());
                 }
             }
 
             if (request.getHeaders() != null) {
                 for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                     connection.addRequestProperty(header.getKey(), header.getValue());
                 }
             }
 
             if (!OAuthUtils.isEmpty(requestMethod)) {
                 connection.setRequestMethod(requestMethod);
                 if (requestMethod.equals("POST")) {
                     connection.setDoOutput(true);
                     OutputStream ost = connection.getOutputStream();
                     PrintWriter pw = new PrintWriter(ost);
                     pw.print(request.getBody());
                     pw.flush();
                     pw.close();
                 }
             } else {
                 connection.setRequestMethod("GET");
             }
 
             connection.connect();
 
             InputStream inputStream;
             responseCode = connection.getResponseCode();
             if (responseCode == 400 || responseCode == 401) {
                 inputStream = connection.getErrorStream();
             } else {
                 inputStream = connection.getInputStream();
             }
 
             responseBody = OAuthUtils.saveStreamAsString(inputStream);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return OAuthClientResponseFactory
                 .createCustomResponse(responseBody,
                         connection.getContentType(),
                         responseCode, responseClass);
     }
 
     /**
      * Shut down the client and release the resources associated with the HttpClient
      */
     @Override
     public void shutdown() {
 
     }
 }
