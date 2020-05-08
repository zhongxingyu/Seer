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
 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  Author: lillialexis
  Date:   12/28/11
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage;
 
 import android.widget.Toast;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import org.apache.cordova.api.CallbackContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.util.Log;
 import org.apache.cordova.api.CordovaPlugin;
 import org.apache.cordova.api.PluginResult;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 
 import static com.janrain.android.engage.JREngageError.AuthenticationError;
 import static com.janrain.android.engage.JREngageError.ConfigurationError;
 import static com.janrain.android.engage.JREngageError.SocialPublishingError;
 import static org.apache.cordova.api.PluginResult.Status;
 
 public class JREngagePhonegapPlugin extends CordovaPlugin implements JREngageDelegate {
     private static final String TAG = JREngagePhonegapPlugin.class.getSimpleName();
 
     private JREngage mJREngage;
     private JRDictionary mAuthResponse;
     private JRDictionary mSharingResponse;
     private ArrayList<JRDictionary> mAuthBlobsDuringSharing;
     private ArrayList<JRDictionary> mShareBlobsDuringSharing;
 
     private boolean mSharingMode;
     private CallbackContext mCallback;
     private String mCurrentCmd;
 
     private static int instantiationCount = 0;
     {
         if (instantiationCount > 0) {
             JREngage.logd(TAG, "More than one instance, instantiation count: " + instantiationCount);
         }
         instantiationCount++;
     }
 
     @Override
     public boolean execute(final String cmd, final JSONArray args, final CallbackContext callback) {
         if (mCallback != null) {
             callback.sendPluginResult(buildJsonFailureResult(-1, "Error processing " + cmd + ", can't " +
                     "execute multiple JREngagePhonegapPlugin commands concurrently. Currently executing: " +
                     mCurrentCmd + " for callback ID: " + mCallback.getCallbackId()));
             return false;
         }
 
         mCallback = callback;
         mCurrentCmd = cmd;
 
         cordova.getActivity().runOnUiThread(new Runnable() {
             public void run() {
                 try {
                     if (cmd.equals("print")) {
                         showToast(args.getString(0));
                     } else if (cmd.equals("initializeJREngage")) {
                         String appId = args.get(0) == JSONObject.NULL ? null : args.getString(0);
                         String tokenUrl = args.get(1) == JSONObject.NULL ? null : args.getString(1);
                         initializeJREngage(appId, tokenUrl);
                     } else if (cmd.equals("showAuthenticationDialog")) {
                         mJREngage.addDelegate(JREngagePhonegapPlugin.this);
                         showAuthenticationDialog();
                     } else if (cmd.equals("showSharingDialog")) {
                         mAuthBlobsDuringSharing = new ArrayList<JRDictionary>();
                         mShareBlobsDuringSharing = new ArrayList<JRDictionary>();
                         mSharingResponse = new JRDictionary();
                         mJREngage.addDelegate(JREngagePhonegapPlugin.this);
                         showSharingDialog(args.getString(0));
                     } else { // TODO: Test these errors and verify that the single quotes are fine
                         postResultAndResetState(new PluginResult(Status.INVALID_ACTION,
                                 buildFailureString(-1, "Unknown action: " + cmd)));
                     }
                 } catch (JSONException e) {
                     postResultAndResetState(buildJsonFailureResult(-1, "Error parsing arguments for " +
                             cmd + ": " + e.getLocalizedMessage()));
                 }
             }
         });
 
         return true;
     }
 
     private void postResultAndResetState(PluginResult result) {
         mSharingMode = false;
         mAuthResponse = null;
         mSharingResponse = null;
         mAuthBlobsDuringSharing = null;
         mShareBlobsDuringSharing = null;
         mJREngage.removeDelegate(this);
         if (mCallback != null) mCallback.sendPluginResult(result);
         mCallback = null;
         mCurrentCmd = null;
     }
 
     private PluginResult buildSuccessResult(JRDictionary successDictionary) {
         String message = successDictionary.toJson();
 
         JREngage.logd("[buildSuccessResult]", message);
         return new PluginResult(Status.OK, message);
     }
 
     private PluginResult buildJsonFailureResult(int code, String message) {
         return new PluginResult(Status.JSON_EXCEPTION, buildFailureString(code, message));
     }
 
     private PluginResult buildFailureResult(JREngageError error) {
         return buildFailureResult(error.getCode(), error.getMessage());
     }
 
     private PluginResult buildFailureResult(int code, String message) {
         return new PluginResult(Status.ERROR, buildFailureString(code, message));
     }
 
     private String buildFailureString(int code, String message) {
         JRDictionary errorDictionary = new JRDictionary();
         errorDictionary.put("code", code);
         errorDictionary.put("message", message);
         errorDictionary.put("stat", "fail");
 
         return errorDictionary.toJson();
     }
 
     private void showToast(final String message) {
         Toast myToast = Toast.makeText(cordova.getActivity(), message, Toast.LENGTH_SHORT);
         myToast.show();
 
         postResultAndResetState(new PluginResult(Status.OK, message));
     }
 
     private void initializeJREngage(String appId, String tokenUrl) {
         if (appId == null || appId.equals("")) {
             postResultAndResetState(buildFailureResult(ConfigurationError.MISSING_APP_ID_ERROR,
                     "Missing appId in call to initialize"));
             return;
         }
 
         try {
             mJREngage = JREngage.initInstance(cordova.getActivity(), appId, tokenUrl, null);
         } catch (IllegalArgumentException e) {
             postResultAndResetState(buildFailureResult(ConfigurationError.GENERIC_CONFIGURATION_ERROR,
                     e.getLocalizedMessage()));
             return;
         }
 
         postResultAndResetState(new PluginResult(Status.OK,
                 "{'stat':'ok','message':'Initializing JREngage...'}"));
     }
 
     private void showAuthenticationDialog() {
         mJREngage.showAuthenticationDialog(cordova.getActivity());
     }
 
     private void showSharingDialog(String activityString) {
         mSharingMode = true;
 
         if (activityString == null || activityString.equals("")) {
             postResultAndResetState(buildFailureResult(SocialPublishingError.ACTIVITY_NULL,
                     "Activity object is required"));
             return;
         }
 
         JRActivityObject activity;
         try {
             activity = new JRActivityObject(JRDictionary.fromJsonString(activityString));
         } catch (JSONException e) {
             postResultAndResetState(buildJsonFailureResult(SocialPublishingError.BAD_ACTIVITY_JSON,
                     "The JSON passed was not a valid activity object: " + e.getLocalizedMessage()));
             return;
         } catch (IllegalArgumentException ignore) {
             postResultAndResetState(buildJsonFailureResult(SocialPublishingError.BAD_ACTIVITY_JSON,
                     "The JSON passed was not a valid activity object: " + ignore.getLocalizedMessage()));
             return;
         }
 
         mJREngage.showSocialPublishingDialog(cordova.getActivity(), activity);
     }
 
     public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
         JREngage.logd(TAG, "[jrEngageDialogDidFailToShowWithError] " + error);
         postResultAndResetState(buildFailureResult(error));
     }
 
     /* Happens on user backing out of authentication, so report user cancellation */
     // TODO: Test this when sharing
     public void jrAuthenticationDidNotComplete() {
         JREngage.logd(TAG, "[jrAuthenticationDidNotComplete] User Canceled");
         postResultAndResetState(buildFailureResult(AuthenticationError.AUTHENTICATION_CANCELED,
                 "User canceled authentication"));
     }
 
     public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidFailWithError] " + error);
         // TODO: Test this on Android (auth fails as well as sharing fails)
         if (!mSharingMode)
             postResultAndResetState(buildFailureResult(error));
     }
 
     public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                       JREngageError error,
                                                       String provider) {
         Log.e(TAG, "[jrAuthenticationCallToTokenUrlDidFail] " + error);
         if (!mSharingMode) {
             postResultAndResetState(buildFailureResult(error));
         }
     }
 
     public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidSucceedForUser]");
 
         auth_info.remove("stat");
 
         mAuthResponse = new JRDictionary();
         mAuthResponse.put("auth_info", auth_info);
         mAuthResponse.put("provider", provider);
        if (mJREngage.getTokenUrl() == null) populateAndMaybePostAuthResponse(null, null);
     }
 
    private void populateAndMaybePostAuthResponse(String tokenUrl, String tokenUrlPayload) {
         mAuthResponse.put("tokenUrl", tokenUrl);
         mAuthResponse.put("tokenUrlPayload", tokenUrlPayload);
         mAuthResponse.put("stat", "ok");
 
         if (mSharingMode) {
             if (mAuthBlobsDuringSharing == null) mAuthBlobsDuringSharing = new ArrayList<JRDictionary>();
 
             mAuthBlobsDuringSharing.add(mAuthResponse);
             mAuthResponse = null;
         } else {
             postResultAndResetState(buildSuccessResult(mAuthResponse));
         }
     }
 
     public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                 HttpResponseHeaders response,
                                                  String tokenUrlPayload,
                                                  String provider) {
         JREngage.logd(TAG, "[jrAuthenticationDidReachTokenUrl]");
 
        populateAndMaybePostAuthResponse(tokenUrl, tokenUrlPayload);
     }
 
     public void jrSocialDidNotCompletePublishing() {
         JREngage.logd(TAG, "[jrSocialDidNotCompletePublishing] User Canceled");
         postResultAndResetState(buildFailureResult(SocialPublishingError.CANCELED_ERROR,
                 "User canceled authentication"));
     }
 
     public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                  JREngageError error,
                                                  String provider) {
         JREngage.logd(TAG, "[jrSocialPublishJRActivityDidFail]");
         JRDictionary shareBlob = new JRDictionary();
 
         shareBlob.put("provider", provider);
         shareBlob.put("stat", "fail");
         shareBlob.put("code", error.getCode());
         shareBlob.put("message", error.getMessage());
 
         mShareBlobsDuringSharing.add(shareBlob);
     }
 
     public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
         JREngage.logd(TAG, "[jrSocialDidPublishJRActivity]");
         JRDictionary shareBlob = new JRDictionary();
 
         shareBlob.put("provider", provider);
         shareBlob.put("stat", "ok");
 
         mShareBlobsDuringSharing.add(shareBlob);
     }
 
     public void jrSocialDidCompletePublishing() {
         JREngage.logd(TAG, "[jrSocialDidCompletePublishing]");
 
         mSharingResponse.put("signIns", mAuthBlobsDuringSharing);
         mSharingResponse.put("shares", mShareBlobsDuringSharing);
 
         postResultAndResetState(buildSuccessResult(mSharingResponse));
     }
 }
 
 
 
 
