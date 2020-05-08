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
 package com.janrain.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import com.janrain.android.capture.Capture;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.capture.CaptureRecord;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.types.JRDictionary;
 
 import static com.janrain.android.Jump.SignInResultHandler.SignInError;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.AUTHENTICATION_CANCELED_BY_USER;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.CAPTURE_API_ERROR;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.ENGAGE_ERROR;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.JUMP_NOT_INITIALIZED;
 
 /**
  * See engage.android/Jump_Integration_Guide.md for a developer's integration guide.
  */
 public class Jump {
     /*package*/ enum State {
         STATE;
 
         /*package*/ CaptureRecord signedInUser;
         /*package*/ JREngage jrEngage;
         /*package*/ String captureClientId;
         /*package*/ String captureDomain;
         /*package*/ String captureLocale;
         /*package*/ String captureFormName;
         /*package*/ SignInResultHandler signInHandler;
         /*package*/ TraditionalSignInType traditionalSignInType;
     }
 
     /*package*/ static final State state = State.STATE;
 
     private Jump() {}
 
     /**
      * Initialize the Jump library with you configuration data
      * @param context A context to perform some disk IO with
      * @param engageAppId The application ID of your Engage app, from the Engage Dashboard
      * @param captureDomain The domain of your Capture app, contact your deployment engineer for this
      * @param captureClientId The Capture API client ID for use with this mobile app.
      * @param captureLocale the name of the locale to use in the Capture flow
      * @param captureSignInFormName the name of the Capture sign-in form in the flow
      * @param traditionalSignInType The type of traditional sign-in i.e. username or email address based.
      */
     public static void init(Context context,
                             String engageAppId,
                             String captureDomain,
                             String captureClientId,
                             String captureLocale,
                             String captureSignInFormName,
                             TraditionalSignInType traditionalSignInType) {
         state.jrEngage = JREngage.initInstance(context.getApplicationContext(), engageAppId, null, null);
         state.captureDomain = captureDomain;
         state.captureClientId = captureClientId;
         state.traditionalSignInType = traditionalSignInType;
         state.captureLocale = captureLocale;
         state.captureFormName = captureSignInFormName;
         //j.showAuthenticationDialog();
         //j.addDelegate(); remove
         //j.cancelAuthentication();
         //j.createSocialPublishingFragment();
         //j.setAlwaysForceReauthentication();
         //j.setEnabledAuthenticationProviders();
         //j.setTokenUrl();
         //j.signoutUserForAllProviders();
     }
 
     /**
      * @return the configured Capture app domain
      */
     public static String getCaptureDomain() {
         return state.captureDomain;
     }
 
     /**
      * @return the configured Capture API client ID
      */
     public static String getCaptureClientId() {
         return state.captureClientId;
     }
 
     /**
      * @return the configured Capture locale
      */
     public static String getCaptureLocale() {
         return state.captureLocale;
     }
 
     /**
      * @return the configured Capture sign-in form name
      */
     public static String getCaptureFormName() {
         return state.captureFormName;
     }
 
     /**
      * @return the currently signed-in user, or null
      */
     public static CaptureRecord getSignedInUser() {
         //if (state.signedInUser == null && JREngage.getApplicationContext() != null) {
         //    state.signedInUser = CaptureRecord.loadFromDisk(JREngage.getApplicationContext());
         //}
         return state.signedInUser;
     }
 
     /**
      * Starts the Capture sign-in flow.
      *
      * If the providerName parameter is not null and is a valid provider name string then authentication
      * begins directly with that provider.
      *
      * If providerName is null than a list of available providers is displayed first.
      *
      * @param fromActivity the activity from which to start the dialog activity
      * @param providerName the name of the provider to show the sign-in flow for. May be null.
      *                     If null, a list of providers (and a traditional sign-in form) is displayed to the
      *                     end-user.
      * @param handler your result handler, called upon completion on the UI thread
      * @param mergeToken an Engage auth_info token retrieved from an EMAIL_ADDRESS_IN_USE Capture API error,
      *                   or null for none.
      */
     public static void showSignInDialog(Activity fromActivity, String providerName,
                                         SignInResultHandler handler, final String mergeToken) {
         if (state.jrEngage == null || state.captureDomain == null) {
             handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
             return;
         }
 
         state.signInHandler = handler;
         if ("capture".equals(providerName)) {
             TradSignInUi.showStandAloneDialog(fromActivity, mergeToken);
         } else {
             showSocialSignInDialog(fromActivity, providerName, mergeToken);
         }
     }
 
     private static void showSocialSignInDialog(Activity fromActivity, String providerName,
                                                final String mergeToken) {
         state.jrEngage.addDelegate(new JREngageDelegate.SimpleJREngageDelegate() {
             @Override
             public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
                 String authInfoToken = auth_info.getAsString("token");
 
                 Capture.performSocialSignIn(authInfoToken, new Capture.SignInResultHandler() {
                 //Capture.performLegacySocialSignIn(authInfoToken, new Capture.SignInResultHandler() {
                     public void onSuccess(CaptureRecord record) {
                         state.signedInUser = record;
                         fireHandlerSuccess();
                     }
 
                     public void onFailure(CaptureApiError error) {
                         fireHandlerFailure(new SignInError(CAPTURE_API_ERROR, error, null));
                     }
                 }, provider, mergeToken);
             }
 
             @Override
             public void jrAuthenticationDidNotComplete() {
                 fireHandlerFailure(new SignInError(AUTHENTICATION_CANCELED_BY_USER, null, null));
             }
 
             @Override
             public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
                 fireHandlerFailure(new SignInError(ENGAGE_ERROR, null, error));
             }
 
             @Override
             public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
                 fireHandlerFailure(new SignInError(ENGAGE_ERROR, null, error));
             }
 
             private void fireHandlerFailure(SignInError err) {
                 state.jrEngage.removeDelegate(this);
                 Jump.fireHandlerOnFailure(err);
             }
 
             private void fireHandlerSuccess() {
                 state.jrEngage.removeDelegate(this);
                 Jump.fireHandlerOnSuccess();
             }
         });
 
         if (providerName != null) {
             state.jrEngage.showAuthenticationDialog(fromActivity, providerName);
         } else {
             state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
         }
     }
 
     /**
      * @deprecated
      */
     public static void showSignInDialog(Activity fromActivity, String providerName,
                                         SignInResultHandler handler) {
         showSignInDialog(fromActivity, providerName, handler, null);
     }
 
     /**
      * Signs the signed-in user out, and removes their record from disk.
      * @param applicationContext the application context used to interact with the disk
      */
     public static void signOutCaptureUser(Context applicationContext) {
         state.signedInUser = null;
         CaptureRecord.deleteFromDisk(applicationContext);
     }
 
     /*package*/ static void fireHandlerOnFailure(SignInError failureParam) {
         SignInResultHandler handler_ = state.signInHandler;
         state.signInHandler = null;
         if (handler_ != null) handler_.onFailure(failureParam);
     }
 
     // Package level because of use from TradSignInUi:
     /*package*/ static void fireHandlerOnSuccess() {
         SignInResultHandler handler_ = state.signInHandler;
         state.signInHandler = null;
         if (handler_ != null) handler_.onSuccess();
 
     }
 
     public enum TraditionalSignInType { EMAIL, USERNAME }
 
     /**
      * Headless API for Capture traditional account sign-in
      * @param signInName the end user's user name or email address
      * @param password the end user's password
      * @param handler your callback handler, invoked upon completion in the UI thread
      * @param mergeToken an Engage auth_info token retrieved from an EMAIL_ADDRESS_IN_USE Capture API error,
      *                   or null for none.
      */
     public static void performTraditionalSignIn(String signInName, String password,
                                                 final SignInResultHandler handler, final String mergeToken) {
         if (state.jrEngage == null || state.captureDomain == null) {
             handler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
             return;
         }
 
         Capture.performTraditionalSignIn(signInName, password, state.traditionalSignInType,
                 new Capture.SignInResultHandler() {
                     @Override
                     public void onSuccess(CaptureRecord record) {
                         state.signedInUser = record;
                         handler.onSuccess();
                     }
 
                     @Override
                     public void onFailure(CaptureApiError error) {
                         handler.onFailure(new SignInError(CAPTURE_API_ERROR, error, null));
                     }
                 }, mergeToken);
     }
 
     /**
      * An interface to implement to receive callbacks notifying the completion of a sign-in flow.
      */
     public interface SignInResultHandler {
         /**
          * Errors that may be sent upon failure of the sign-in flow
          */
         public static class SignInError {
             public enum FailureReason {
                 /**
                  * A well formed response could not be retrieved from the Capture server
                  */
                 INVALID_CAPTURE_API_RESPONSE,
 
                 /**
                  * The Jump library has not been initialized
                  */
                 JUMP_NOT_INITIALIZED,
 
                 /**
                  * The user canceled sign-in the sign-in flow during authentication
                  */
                 AUTHENTICATION_CANCELED_BY_USER,
 
                 /**
                  * The password provided was invalid. Only generated by #performTraditionalSignIn(...)
                  */
                 INVALID_PASSWORD,
 
                 /**
                  * The sign-in failed with a well-formed Capture sign-in API error
                  */
                 CAPTURE_API_ERROR,
 
                 /**
                  * The sign-in failed with a JREngageError
                  */
                 ENGAGE_ERROR
             }
 
             public final FailureReason reason;
             public final CaptureApiError captureApiError;
             public final JREngageError engageError;
 
             /*package*/ SignInError(FailureReason reason, CaptureApiError captureApiError,
                                     JREngageError engageError) {
                 this.reason = reason;
                 this.captureApiError = captureApiError;
                 this.engageError = engageError;
             }
 
             public String toString() {
                 return "<" + super.toString() + " reason: " + reason + " captureApiError: " + captureApiError
                         + " engageError: " + engageError + ">";
             }
         }
 
         /**
          * Called when Capture sign-in has succeeded. At this point Jump.getCaptureUser will return the
          * CaptureRecord instance for the user.
          */
         void onSuccess();
 
         /**
          * Called when Capture sign-in has failed.
          * @param error the error which caused the failure
          */
         void onFailure(SignInError error);
     }
 
     /**
      * To be called from Application#onCreate()
      * @param context the application context, used to interact with the disk
      */
     public static void loadFromDisk(Context context) {
         state.signedInUser = CaptureRecord.loadFromDisk(context);
     }
 
     /**
      * To be called from Activity#onPause
      * @param context the application context, used to interact with the disk
      */
     public static void saveToDisk(Context context) {
         if (state.signedInUser != null) state.signedInUser.saveToDisk(context);
     }
 
     /**
      * The default merge-flow handler. Provides a baseline implementation of the merge-account flow UI
      *
      * @param fromActivity the Activity from which to launch subsequent Activities and Dialogs.
      * @param error the error received by your
      * @param signInResultHandler your sign-in result handler.
      */
     public static void startDefaultMergeFlowUi(final Activity fromActivity,
                                                SignInError error,
                                                final SignInResultHandler signInResultHandler) {
         final String mergeToken = error.captureApiError.getMergeToken();
         final String existingProvider = error.captureApiError.getExistingAccountIdentityProvider();
         String conflictingIdentityProvider = error.captureApiError.getConflictingIdentityProvider();
         String conflictingIdpNameLocalized = JRProvider.getLocalizedName(conflictingIdentityProvider);
        String existingIdpNameLocalized = JRProvider.getLocalizedName(existingProvider);
 
         AlertDialog alertDialog = new AlertDialog.Builder(fromActivity)
                 .setTitle(fromActivity.getString(R.string.jr_merge_flow_default_dialog_title))
                 .setCancelable(false)
                 .setMessage(fromActivity.getString(R.string.jr_merge_flow_default_dialog_message,
                         conflictingIdpNameLocalized,
                         existingIdpNameLocalized))
                 .setPositiveButton(fromActivity.getString(R.string.jr_merge_flow_default_merge_button),
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 // When existingProvider == "capture" you can also call ...
                                 //
                                 //     Jump.performTraditionalSignIn(String signInName, String password,
                                 //         final SignInResultHandler handler, final String mergeToken);
                                 //
                                 // ... instead of showSignInDialog if you wish to present your own dialog
                                 // and then use the headless API to perform the traditional sign-in.
                                 Jump.showSignInDialog(fromActivity,
                                         existingProvider,
                                         signInResultHandler,
                                         mergeToken);
                             }
                         })
                 .setNegativeButton(android.R.string.cancel, null)
                 .create();
 
         alertDialog.setCanceledOnTouchOutside(false);
         alertDialog.show();
     }
 }
