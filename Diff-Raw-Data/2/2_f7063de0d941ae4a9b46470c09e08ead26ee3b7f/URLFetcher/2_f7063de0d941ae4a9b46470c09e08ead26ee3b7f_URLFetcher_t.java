 /*
  * polycasso - Cubism Artwork generator
  * Copyright 2009-2011 MeBigFatGuy.com
  * Copyright 2009-2011 Dave Brosius
  * Inspired by work by Roger Alsing
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
 package com.mebigfatguy.polycasso;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.Proxy.Type;
 import java.net.URL;
 
 import org.apache.commons.io.IOUtils;
 
 /**
  * manages downloading data from a url
  */
 public class URLFetcher {
 
     /**
      * private to avoid construction of this static access only class
      */
     private URLFetcher() {
     }
 
     /**
      * retrieve arbitrary data found at a specific url
      * - either http or file urls
      * for http requests, sets the user-agent to mozilla to avoid
      * sites being cranky about a java sniffer
      * 
      * @param url the url to retrieve
      * @param proxyHost the host to use for the proxy
      * @param proxyPort the port to use for the proxy
      * @return a byte array of the content
      * 
      * @throws IOException the site fails to respond
      */
     public static byte[] fetchURLData(String url, String proxyHost, int proxyPort) throws IOException {
         HttpURLConnection con = null;
         InputStream is = null;
 
         try {
             URL u = new URL(url);
             if (url.startsWith("file://")) {
                 is = new BufferedInputStream(u.openStream());
             } else {
                 Proxy proxy;
                 if (proxyHost != null) {
                     proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                 } else {
                    proxy = Proxy.NO_PROXY;
                 }
                 con = (HttpURLConnection)u.openConnection(proxy);
                 con.addRequestProperty("User-Agent", "Mozilla/4.76");
                 con.setDoInput(true);
                 con.setDoOutput(false);
                 con.connect();
 
                 is = new BufferedInputStream(con.getInputStream());
             }
 
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             IOUtils.copy(is, baos);
             return baos.toByteArray();
         } finally {
             IOUtils.closeQuietly(is);
             if (con != null) {
                 con.disconnect();
             }
         }
     }
 }
