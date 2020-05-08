 /* Copyright (c) 2010 HomeAway, Inc.
  * All rights reserved.  http://jmonette/github.io/thunderhead
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.homeaway.aws.thunderhead.client.builder;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import org.apache.commons.lang.StringUtils;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * @author jmonette
  */
 public class CloudSearchWebResourceBuilder {
     public static final int DEFAULT_CONNECT_TIMEOUT = 2000;
     public static final int DEFAULT_READ_TIMEOUT = 3000;
 
     private String host;
     private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
     private int readTimeout = DEFAULT_READ_TIMEOUT;
     private boolean secure = false;
 
     private CloudSearchWebResourceBuilder() {}
 
     public static CloudSearchWebResourceBuilder newInstance() {
         return new CloudSearchWebResourceBuilder();
     }
 
     public CloudSearchWebResourceBuilder host(String host) {
         this.host = host;
         return this;
     }
 
     public CloudSearchWebResourceBuilder connectTimeout(int connectTimeout) {
         this.connectTimeout = connectTimeout;
         return this;
     }
 
     public CloudSearchWebResourceBuilder readTimeout(int readTimeout) {
         this.readTimeout = readTimeout;
         return this;
     }
 
     public CloudSearchWebResourceBuilder useHttps(boolean secure) {
         this.secure = secure;
         return this;
     }
 
     public WebResource build() throws URISyntaxException {
 
         if(StringUtils.isEmpty(this.host)) {
             throw new IllegalStateException("host must not be null or empty");
         }
 
        String scheme = (this.secure ? "https://" : "http://");
         URI uri = new URI(scheme + this.host);
         Client client = Client.create();
         client.setConnectTimeout(this.connectTimeout);
         client.setReadTimeout(this.readTimeout);
 
         return client.resource(uri);
     }
 }
