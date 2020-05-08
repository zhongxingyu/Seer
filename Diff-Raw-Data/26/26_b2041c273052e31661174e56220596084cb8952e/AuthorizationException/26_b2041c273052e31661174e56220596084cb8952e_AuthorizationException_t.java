 /*
  * Copyright 2012 Janrain, Inc.
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
 
 package com.janrain.oauth2;
 
 import com.janrain.backplane.server2.oauth2.model.AuthorizationRequest;
 import com.janrain.backplane.server2.oauth2.model.AuthorizationRequestFields;
 import org.jetbrains.annotations.Nullable;
import scala.Option;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * @author Johnny Bufu
  */
 public class AuthorizationException extends OAuth2Exception {
 
     public AuthorizationException(String oauthErrorCode, String message, HttpServletRequest request) {
         this(oauthErrorCode, message, request, null);
     }
 
     public AuthorizationException(String oauthErrorCode, String message, HttpServletRequest request, @Nullable Throwable cause) {
         super(oauthErrorCode, message, cause);
         this.redirectUri = request.getParameter(AuthorizationRequestFields.REDIRECT_URI().name());
         this.state = request.getParameter(AuthorizationRequestFields.STATE().name());
     }
 
     public AuthorizationException(String oauthErrorCode, String message, AuthorizationRequest request) {
         this(oauthErrorCode, message, request, null);
     }
 
     public AuthorizationException(String oauthErrorCode, String message, AuthorizationRequest request, @Nullable Throwable cause) {
         super(oauthErrorCode, message, cause);
        this.redirectUri = request.get(AuthorizationRequestFields.REDIRECT_URI().name()).getOrElse(null);
        Option<String> stateOption = request.get(AuthorizationRequestFields.STATE().name());
        this.state = stateOption.isDefined() ? stateOption.get() : null;
     }
 
     public String getRedirectUri() {
         return redirectUri;
     }
 
     public String getState() {
         return state;
     }
 
     private final String redirectUri;
     private final String state;
 }
