 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.engage;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.support.v4.app.FragmentActivity;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSession;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.ApiConnection;
 import com.janrain.android.utils.LogUtils;
 import com.janrain.android.utils.UiUtils;
 import org.json.JSONObject;
 
 public class JRNativeAuth {
     public static final int REQUEST_CODE_TRY_WEBVIEW = 9999;
 
     public static boolean canHandleProvider(JRProvider provider) {
         if (provider.getName().equals("facebook") && NativeFacebook.canHandleAuthentication()) {
             return true;
         } else if (provider.getName().equals("googleplus") && NativeGooglePlus.canHandleAuthentication()) {
             return true;
         }
 
         return false;
     }
 
     public static NativeProvider createNativeProvider(JRProvider provider, FragmentActivity activity,
                                                       NativeAuthCallback callback) {
         NativeProvider nativeProvider = null;
 
         if (provider.getName().equals("facebook")) {
             nativeProvider = new NativeFacebook(activity, callback);
         } else if (provider.getName().equals("googleplus")) {
             nativeProvider = new NativeGooglePlus(activity, callback);
         } else {
             throw new RuntimeException("Unexpected native auth provider " + provider);
         }
 
         return nativeProvider;
     }
 
     public static enum NativeAuthError {
         CANNOT_INVOKE_FACEBOOK_OPEN_SESSION_METHODS,
         FACEBOOK_SESSION_IS_CLOSED,
         ENGAGE_ERROR,
         LOGIN_CANCELED,
         GOOGLE_PLAY_UNAVAILABLE,
         CANNOT_INSTANTIATE_GOOGLE_PLAY_CLIENT,
         CANNOT_GET_GOOGLE_PLUS_ACCESS_TOKEN,
         GOOGLE_PLUS_DISCONNECTED,
         COULD_NOT_RESOLVE_GOOGLE_PLUS_RESULT
     }
 
     public static abstract class NativeAuthCallback {
         private boolean hasFailed = false;
 
         public abstract void onSuccess(JRDictionary payload);
 
         public boolean shouldTriggerAuthenticationDidCancel() {
             return false;
         }
 
         public void onFailure(String message, NativeAuthError errorCode, Exception exception) {
             onFailure(message, errorCode, exception, false);
         }
 
         public void onFailure(String message, NativeAuthError errorCode, boolean shouldTryWebView) {
             onFailure(message, errorCode, null, shouldTryWebView);
         }
 
         public void onFailure(String message, NativeAuthError errorCode) {
             onFailure(message, errorCode, null, false);
         }
 
         public abstract void tryWebViewAuthentication();
 
         public void onFailure(final String message, NativeAuthError errorCode, Exception exception,
                               boolean shouldTryWebViewAuthentication) {
             LogUtils.logd("Native Auth Error: " + errorCode + " " + message
                     + (exception != null ? " " + exception : ""));
 
             if (hasFailed) return;
             hasFailed = true;
 
             final JRSession session = JRSession.getInstance();
            if (errorCode.equals(JRNativeAuth.NativeAuthError.ENGAGE_ERROR)) {
                 session.triggerAuthenticationDidFail(new JREngageError(
                         message,
                         JREngageError.ConfigurationError.GENERIC_CONFIGURATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_FAILED));
            } else if (errorCode.equals((JRNativeAuth.NativeAuthError.GOOGLE_PLAY_UNAVAILABLE))) {
                 if (shouldTryWebViewAuthentication) {
                     tryWebViewAuthentication();
                 } else {
                     session.triggerAuthenticationDidFail(new JREngageError(
                             message,
                             JREngageError.ConfigurationError.GOOGLE_PLAY_UNAVAILABLE,
                             JREngageError.ErrorType.CONFIGURATION_FAILED));
                 }
            } else if (errorCode.equals(JRNativeAuth.NativeAuthError.LOGIN_CANCELED)) {
                 if (shouldTriggerAuthenticationDidCancel()) {
                     session.triggerAuthenticationDidCancel();
                 }
             } else {
                 session.triggerAuthenticationDidFail(new JREngageError(
                         message,
                         JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                        JREngageError.ErrorType.AUTHENTICATION_FAILED
                 ));
             }
         }
     }
 
     public static abstract class NativeProvider {
         /*package*/ NativeAuthCallback completion;
         /*package*/ FragmentActivity fromActivity;
 
         /*package*/ NativeProvider(FragmentActivity activity, JRNativeAuth.NativeAuthCallback callback) {
             completion = callback;
             fromActivity = activity;
         }
 
         /*package*/ static boolean canHandleAuthentication() {
             return false;
         }
 
         public abstract String provider();
 
         public abstract void startAuthentication();
 
         public void signOut() {
             // Optional
         }
 
         public void revoke() {
             // Optional
         }
 
         public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
 
         /*package*/ void getAuthInfoTokenForAccessToken(String accessToken) {
 
             ApiConnection.FetchJsonCallback handler = new ApiConnection.FetchJsonCallback() {
                 public void run(JSONObject json) {
 
                    if (json == null) triggerOnFailure("Bad Response", null);
 
                     String status = json.optString("stat");
 
                     if (json == null || json.optString("stat") == null || !json.optString("stat").equals("ok")) {
                         triggerOnFailure("Bad Json: " + json, NativeAuthError.ENGAGE_ERROR);
                         return;
                     }
 
                     String auth_token = json.optString("token");
 
                     JRDictionary payload = new JRDictionary();
                     payload.put("token", auth_token);
                     payload.put("auth_info", new JRDictionary());
 
                     triggerOnSuccess(payload);
                 }
             };
 
             ApiConnection connection =
                     new ApiConnection(JRSession.getInstance().getRpBaseUrl() + "/signin/oauth_token");
 
             connection.addAllToParams("token", accessToken, "provider", provider());
             connection.fetchResponseAsJson(handler);
 
         }
 
         /*package*/ void triggerOnSuccess(JRDictionary payload) {
             completion.onSuccess(payload);
         }
 
         /*package*/ void triggerOnFailure(String message, NativeAuthError errorCode, Exception exception) {
             triggerOnFailure(message, errorCode, exception, false);
         }
 
         /*package*/ void triggerOnFailure(String message, NativeAuthError errorCode, boolean shouldTryWebView) {
             triggerOnFailure(message, errorCode, null, shouldTryWebView);
         }
 
         /*package*/ void triggerOnFailure(String message, NativeAuthError errorCode) {
             triggerOnFailure(message, errorCode, null, false);
         }
 
         /*package*/ void triggerOnFailure(final String message, NativeAuthError errorCode, Exception exception,
                               boolean shouldTryWebViewAuthentication) {
             completion.onFailure(message, errorCode, exception, shouldTryWebViewAuthentication);
         }
     }
 
 }
 
