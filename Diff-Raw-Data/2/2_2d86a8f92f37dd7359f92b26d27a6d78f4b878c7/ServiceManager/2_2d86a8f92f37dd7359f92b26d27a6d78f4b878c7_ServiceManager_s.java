 /*
  * Copyright 2013 Uwe Trottmann
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
 package com.uwetrottmann.getglue;
 
 import com.uwetrottmann.getglue.services.InteractionService;
 import com.uwetrottmann.getglue.services.ObjectService;
 import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
 import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
 import org.apache.oltu.oauth2.common.message.types.GrantType;
 import org.apache.oltu.oauth2.common.message.types.ResponseType;
 import retrofit.RequestInterceptor;
 import retrofit.RestAdapter;
 
 public class ServiceManager {
 
     private static final String API_URL = "https://api.getglue.com/v3";
     private static final String OAUTH2_AUTHORIZATION_URL = "https://api.getglue.com/oauth2/authorize";
     private static final String OAUTH2_ACCESS_TOKEN_URL = "https://api.getglue.com/oauth2/access_token";
     private boolean mIsDebug;
     private String mAccessToken;
 
     public ServiceManager() {
     }
 
     public ServiceManager setIsDebug(boolean isDebug) {
         mIsDebug = isDebug;
         return this;
     }
 
     public OAuthClientRequest getAuthorizationRequest(String clientId, String redirectUri) throws OAuthSystemException {
         OAuthClientRequest request = OAuthClientRequest
                 .authorizationLocation(OAUTH2_AUTHORIZATION_URL)
                 .setScope("public read write")
                 .setResponseType(ResponseType.CODE.toString())
                 .setClientId(clientId)
                 .setRedirectURI(redirectUri)
                 .buildQueryMessage();
         return request;
     }
 
     public OAuthClientRequest getAccessTokenRequest(String clientId, String clientSecret, String redirectUri,
                                                     String authCode) throws OAuthSystemException {
         OAuthClientRequest request = OAuthClientRequest
                 .tokenLocation(OAUTH2_ACCESS_TOKEN_URL)
                 .setGrantType(GrantType.AUTHORIZATION_CODE)
                 .setClientId(clientId)
                 .setClientSecret(clientSecret)
                 .setRedirectURI(redirectUri)
                 .setCode(authCode)
                .buildBodyMessage();
         return request;
     }
 
     public void setAccessToken(String token) {
         mAccessToken = token;
     }
 
     private RestAdapter buildRestAdapter() {
         RestAdapter.Builder builder = new RestAdapter.Builder()
                 .setServer(API_URL);
 
         // Supply OAuth 2.0 access token
         builder.setRequestInterceptor(new RequestInterceptor() {
             @Override
             public void intercept(RequestFacade request) {
                 request.addQueryParam("access_token", mAccessToken);
             }
         });
 
         if (mIsDebug) {
             builder.setLogLevel(RestAdapter.LogLevel.FULL);
         }
 
         return builder.build();
     }
 
     public ObjectService objectService() {
         ObjectService service = buildRestAdapter().create(ObjectService.class);
         return service;
     }
 
     public InteractionService interactionService() {
         InteractionService service = buildRestAdapter().create(InteractionService.class);
         return service;
     }
 }
