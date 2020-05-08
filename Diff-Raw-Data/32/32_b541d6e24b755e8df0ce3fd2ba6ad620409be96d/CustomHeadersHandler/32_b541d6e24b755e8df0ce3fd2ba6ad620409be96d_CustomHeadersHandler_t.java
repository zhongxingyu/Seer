 /**
  * Copyright 2012 Terremark Worldwide Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.terremark.handlers;
 
 import javax.ws.rs.core.HttpHeaders;
 
 import org.apache.wink.client.ClientRequest;
 import org.apache.wink.client.ClientResponse;
 import org.apache.wink.client.handlers.ClientHandler;
 import org.apache.wink.client.handlers.HandlerContext;
 
 import com.terremark.impl.VersionAndDateProvider;
 
 /**
  * Custom HTTP header handler. This handler is responsible for injecting HTTP headers before the request is sent to the
  * API end point.
  *
  * @author <a href="mailto:spasam@terremark.com">Seshu Pasam</a>
  */
 public class CustomHeadersHandler implements ClientHandler {
     /** API version */
     private static final String VERSION = CustomHeadersHandler.class.getPackage().getImplementationVersion();
     /** User agent header value */
     private static final String USER_AGENT = "Terremark Java API " + (VERSION == null ? "Dev" : VERSION);
     /** Terremark version header */
     private static final String TERREMARK_VERSION_HEADER = "x-tmrk-version";
    /** Terremark nonce header */
    private static final String TERREMARK_NONCE_HEADER = "x-tmrk-nonce";
     /** API version and current date/time provider */
     private final VersionAndDateProvider versionAndDateProvider;
 
     /**
      * Default constructor. Takes instance of version and date/time provider.
      *
      * @param versionAndDateProvider Version and date/time provider.
      */
     public CustomHeadersHandler(final VersionAndDateProvider versionAndDateProvider) {
         this.versionAndDateProvider = versionAndDateProvider;
     }
 
     /**
      * Retrieves the version and date/time from the version and date/time provider and add's them as headers to the
      * request. User-agent information is also added.
      *
      * @param request Client request.
      * @param context Request context.
      * @return Client response.
      * @throws Exception If there is a problem invoking the chain.
      */
     @Override
     @SuppressWarnings("PMD.SignatureDeclareThrowsException")
     public final ClientResponse handle(final ClientRequest request, final HandlerContext context) throws Exception {
         final String version = versionAndDateProvider.getVersion();
         final String date = versionAndDateProvider.getDate();
 
        // Set the user-agent. Ideally this should be configurable to reduce unnecessary traffic
         request.getHeaders().putSingle(HttpHeaders.USER_AGENT, USER_AGENT);

        // If two requests for same resource come in at the same time, one of them will get rejected as replay attack
        // So we are using a custom Terremark header to add some random nonce. This is not a part of the API spec
        // Currently nonce is the current time in millis. We can also use some random garbage
        request.getHeaders().putSingle(TERREMARK_NONCE_HEADER, Long.toString(System.currentTimeMillis()));

         if (version != null) {
             request.getHeaders().putSingle(TERREMARK_VERSION_HEADER, version);
         }
         if (date != null) {
             request.getHeaders().putSingle(HttpHeaders.DATE, date); // x-tmrk-date can also be used as header name
         }
 
         return context.doChain(request);
     }
}
