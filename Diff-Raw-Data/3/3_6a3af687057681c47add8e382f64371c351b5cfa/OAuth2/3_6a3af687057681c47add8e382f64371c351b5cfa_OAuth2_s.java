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
 
 import org.apache.commons.lang.StringUtils;
 import org.jetbrains.annotations.Nullable;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * OAuth constants
  *
  * @author Johnny Bufu
  */
 public class OAuth2 {
 
     // - PUBLIC
 
     // /authorize constants
     public static final String OAUTH2_AUTHZ_DIRECT_ERROR = "direct_error"; // internal code, not defined by OAuth
     public static final String OAUTH2_AUTHZ_ERROR_FIELD_NAME = "error";
     public static final String OAUTH2_AUTHZ_ERROR_DESC_FIELD_NAME = "error_description";
 
     public static final String OAUTH2_AUTHZ_INVALID_REQUEST = "invalid_request";
     public static final String OAUTH2_AUTHZ_UNAUTHORIZED_CLIENT = "unauthorized_client";
     public static final String OAUTH2_AUTHZ_ACCESS_DENIED = "access_denied";
     public static final String OAUTH2_AUTHZ_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
     public static final String OAUTH2_AUTHZ_INVALID_SCOPE = "invalid_scope";
     public static final String OAUTH2_AUTHZ_SERVER_ERROR = "server_error";
     public static final String OAUTH2_AUTHZ_TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
 
     public static final String OAUTH2_AUTHZ_INVALID_REQUEST_REDIRECT_URI = "invalid_request_redirect_uri";
     public static final String OAUTH2_AUTHZ_LOGIN_REQUIRED = "login_required";
     public static final String OAUTH2_AUTHZ_SESSION_SELECTION_REQUIRED = "session_selection_required";
     public static final String OAUTH2_AUTHZ_APPROVAL_REQUIRED = "approval_required";
     public static final String OAUTH2_AUTHZ_USER_MISMATCHED = "user_mismatched";
 
     public static final String OAUTH2_AUTHZ_RESPONSE_CODE = "code";
     public static final String OAUTH2_AUTHZ_RESPONSE_STATE = "state";
 
     // /token constants
     public static final String OAUTH2_TOKEN_ERROR_FIELD_NAME = "error";
     public static final String OAUTH2_TOKEN_ERROR_DESC_FIELD_NAME = "error_description";
 
    public static final String OAUTH2_TOKEN_RESPONSE_TYPE_CODE = "token";
     public static final String OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE = "authorization_code";
     public static final String OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
     public static final String OAUTH2_TOKEN_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
 
     public static final String OAUTH2_TOKEN_INVALID_REQUEST = "invalid_request";
     public static final String OAUTH2_TOKEN_INVALID_CLIENT = "invalid_client";
     public static final String OAUTH2_TOKEN_UNSUPPORTED_GRANT = "unsupported_grant_type";
     public static final String OAUTH2_TOKEN_INVALID_GRANT = "invalid_grant";
     public static final String OAUTH2_TOKEN_INVALID_SCOPE = "invalid_scope";
     public static final String OAUTH2_TOKEN_SERVER_ERROR = "server_error"; // not actually defined in OAuth2 5.2, what should one do?!
 
     public static final String OAUTH2_ACCESS_TOKEN_PARAM_NAME = "access_token";
     public static final String OAUTH2_REFRESH_TOKEN_PARAM_NAME = "refresh_token";
     public static final String OAUTH2_TOKEN_TYPE_PARAM_NAME = "token_type";
     public static final String OAUTH2_SCOPE_PARAM_NAME = "scope";
     public static final String OAUTH2_TOKEN_RESPONSE_EXPIRES =  "expires_in";
 
     public static final String OAUTH2_TOKEN_TYPE_BEARER =  "Bearer";
 
     public static void validateRedirectUri(String redirectUri) throws ValidationException {
         validateRedirectUri(redirectUri, null);
     }
 
     /**
      * @param redirectUri the redirect_uri (per OAuth2) to validate; may be null, which is permitted/valid
      *
      * @throws ValidationException if the supplied redirectUri fails the OAuth2 prescribed checks
      */
     public static void validateRedirectUri(String redirectUri, @Nullable String expected) throws ValidationException {
         if (StringUtils.isNotEmpty(redirectUri)) {
             try {
                 URL url = new URL(redirectUri);
                 if (StringUtils.isEmpty(url.getProtocol())) {
                     throw new ValidationException(OAUTH2_TOKEN_INVALID_REQUEST, "redirect_uri is not absolute: " + redirectUri);
                 }
                 if (StringUtils.isNotEmpty(url.getRef())) {
                     throw new ValidationException(OAUTH2_TOKEN_INVALID_REQUEST, "redirect_uri MUST not contain a fragment: " + redirectUri);
                 }
                 if (StringUtils.isNotEmpty(expected) && ! redirectUri.equals(expected)) {
                     throw new ValidationException(OAUTH2_TOKEN_INVALID_GRANT, "Redirect URI mismatch, expected: " +expected);
                 }
             } catch (MalformedURLException e) {
                 throw new ValidationException(OAUTH2_TOKEN_INVALID_REQUEST, "Invalid redirect_uri: " + e .getMessage());
             }
         }
     }
 
     // - PRIVATE
 
     private OAuth2() { }
 }
