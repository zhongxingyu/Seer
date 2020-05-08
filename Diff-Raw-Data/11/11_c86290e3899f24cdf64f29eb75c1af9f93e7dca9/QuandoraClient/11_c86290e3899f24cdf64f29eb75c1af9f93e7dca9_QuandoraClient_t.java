 /*
 Copyright 2013 Nicolas Joseph
 
 Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package com.quandora.plugins.REST.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 
 import com.quandora.plugins.REST.Admin.ConfigResource.Config;
 
 
 /**
  * @author Nicolas Joseph
  *
  */
 public class QuandoraClient {
 
     protected Config config;
 
     public QuandoraClient(Config config){
         this.config=config;
     }
 
     /**
      * The default implementation use the Java URL
      * @param request
      * @return
      */
     public QuandoraResponse execute(Request request) throws Exception {
         URL url = new URL((request).getUrl());
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setDoOutput(request.hasData());
         conn.setDoInput(true);
         conn.setUseCaches(false);
         for (Map.Entry<String,String> entry : request.getHeaders().entrySet()) {
             conn.addRequestProperty(entry.getKey(), entry.getValue());
         }
         if (request.hasData()) {
             OutputStream out = conn.getOutputStream();
             out.write(request.data.getBytes());
             out.flush();
         }
         QuandoraResponse resp = new QuandoraResponse();
         resp.status = conn.getResponseCode();
         String ctype = conn.getContentType();
         String charset = "UTF-8";
         if (ctype != null) {
             int i = ctype.indexOf("charset");
             if (i > -1) {
                 i += "charset".length();
                 //ctype.indexOf(i, '=')
             }
         }
         InputStream in = conn.getInputStream();
         InputStreamReader reader = new InputStreamReader(in, charset);
 
         resp.result = readAll(reader, 4096);
         //TODO get headers conn.get
         return resp;
     }
 
     public static String readAll(Reader reader, int bufSize) throws IOException {
         StringBuilder buf = new StringBuilder();
         int r = -1;
         char[] chars = new char[bufSize];
         try {
             while ((r = reader.read(chars)) > -1) {
                 if (r > 0) {
                     buf.append(chars, 0, r);
                 }
             }
             return buf.toString();
         } finally {
             reader.close();
         }
     }
 
     public Request createRequest(String url) {
         return createRequest(url, Request.GET);
     }
 
     protected Request createRequest(String url, String method) {
         Request req = new Request(url, method);
        req.addHeader("Authorization", "Basic "+config.getPass());
         return req;
     }
 }
