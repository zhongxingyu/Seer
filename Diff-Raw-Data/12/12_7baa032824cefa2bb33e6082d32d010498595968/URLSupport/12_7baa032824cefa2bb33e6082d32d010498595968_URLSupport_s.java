 /*
  * yank - a maven artifact fetcher ant task
  * Copyright 2013 MeBigFatGuy.com
  * Copyright 2013 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.yank;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.URL;
 
 public class URLSupport {
 
     private URLSupport() {
     }
 
     public static HttpURLConnection openURL(URL url, String proxyUrl) throws IOException {
         if (proxyUrl.isEmpty()) {
             return (HttpURLConnection) url.openConnection();
         }
 
         String[] proxyParts = proxyUrl.split(":");
         String host = proxyParts[0];
         int port = 80;
        if (proxyParts.length > 0) {
             try {
                 port = Integer.parseInt(proxyParts[1]);
             } catch (NumberFormatException nfe) {
             }
         }
         InetSocketAddress sockAddr = new InetSocketAddress(host, port);
         Proxy proxy = new Proxy(Proxy.Type.HTTP, sockAddr);
 
         return (HttpURLConnection) url.openConnection(proxy);
     }
 }
