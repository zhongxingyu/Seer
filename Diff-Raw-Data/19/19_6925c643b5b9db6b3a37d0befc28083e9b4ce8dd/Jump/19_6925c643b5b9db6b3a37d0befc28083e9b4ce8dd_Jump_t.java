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
 import com.janrain.android.capture.CaptureFlowUtils;
 import com.janrain.android.capture.CaptureRecord;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.ApiConnection;
 import com.janrain.android.utils.JsonUtils;
 import com.janrain.android.utils.LogUtils;
 import com.janrain.android.utils.ThreadUtils;
 import org.json.JSONObject;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.Map;
 
 import static com.janrain.android.Jump.SignInResultHandler.SignInError;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.AUTHENTICATION_CANCELED_BY_USER;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.CAPTURE_API_ERROR;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.ENGAGE_ERROR;
 import static com.janrain.android.Jump.SignInResultHandler.SignInError.FailureReason.JUMP_NOT_INITIALIZED;
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 /**
  * See jump.android/Jump_Integration_Guide.md for a developer's integration guide.
  */
 public class Jump {
     private static final String JR_CAPTURE_FLOW = "jr_capture_flow";
 
     /*package*/ enum State {
         STATE;
 
         // Computed values:
         /*package*/ CaptureRecord signedInUser;
         /*package*/ JREngage jrEngage;
         /*package*/ Map<String, Object> captureFlow;
 
         // Configured values:
         /*package*/ Context context;
         /*package*/ String captureAppId;
         /*package*/ String captureClientId;
         /*package*/ String captureDomain;
         /*package*/ boolean flowUsesTestingCdn;
         /*package*/ String captureFlowName;
         /*package*/ String captureFlowVersion;
         /*package*/ String captureLocale;
         /*package*/ boolean captureEnableThinRegistration;
         /*package*/ String captureTraditionalSignInFormName;
         /*package*/ String captureSocialRegistrationFormName;
         /*package*/ String captureTraditionalRegistrationFormName;
         /*package*/ TraditionalSignInType traditionalSignInType;
         /*package*/ String backplaneChannelUrl;
 
         // Transient state values:
         /*
          * Every method that performs a sign-in or registration must set signInHandler and call
         * fireHandlerOnFailure and fireHandlerOnSuccess when the operation has completed.
          */
         /*package*/ SignInResultHandler signInHandler;
         public boolean initCalled;
     }
 
     /*package*/ static final State state = State.STATE;
 
     private Jump() {}
 
     /**
      * @deprecated
      * Initialize the Jump library with you configuration data
      */
     public static void init(Context context,
                             String engageAppId,
                             String captureDomain,
                             String captureClientId,
                             String captureLocale,
                             String captureTraditionalSignInFormName,
                             TraditionalSignInType traditionalSignInType) {
         //j.showAuthenticationDialog();
         //j.addDelegate(); remove
         //j.cancelAuthentication();
         //j.createSocialPublishingFragment();
         //j.setAlwaysForceReauthentication();
         //j.setEnabledAuthenticationProviders();
         //j.setTokenUrl();
         //j.signoutUserForAllProviders();
         JumpConfig jumpConfig = new JumpConfig();
         jumpConfig.engageAppId = engageAppId;
         jumpConfig.captureDomain = captureDomain;
         jumpConfig.captureClientId = captureClientId;
         jumpConfig.captureLocale = captureLocale;
         jumpConfig.captureTraditionalSignInFormName = captureTraditionalSignInFormName;
         jumpConfig.traditionalSignInType = traditionalSignInType;
         init(context, jumpConfig);
     }
 
     /**
      * Initializes the Jump library. It is recommended to call this method from your Application object.
      * Initialization will cause some network and disk IO to be performed (on a background thread) so it
      * is recommended that the library be initialized before it is used.
      * @param context a context to perform IO from
      * @param jumpConfig an instance of JumpConfig which contains your configuration values. These values
      */
     public static synchronized void init(Context context, JumpConfig jumpConfig) {
         if (state.initCalled) throwDebugException(new IllegalStateException("Multiple Jump.init() calls"));
         state.initCalled = true;
 
         state.context = context;
         state.jrEngage = JREngage.initInstance(context.getApplicationContext(), jumpConfig.engageAppId,
                 null, null);
         state.captureSocialRegistrationFormName = jumpConfig.captureSocialRegistrationFormName;
         state.captureTraditionalRegistrationFormName = jumpConfig.captureTraditionalRegistrationFormName;
         state.captureEnableThinRegistration = jumpConfig.captureEnableThinRegistration;
         state.captureFlowName = jumpConfig.captureFlowName;
         state.captureFlowVersion = jumpConfig.captureFlowVersion;
         state.captureDomain = jumpConfig.captureDomain;
         state.captureAppId = jumpConfig.captureAppId;
         state.captureClientId = jumpConfig.captureClientId;
         state.traditionalSignInType = jumpConfig.traditionalSignInType;
         state.captureLocale = jumpConfig.captureLocale;
         state.captureTraditionalSignInFormName = jumpConfig.captureTraditionalSignInFormName;
         state.backplaneChannelUrl = jumpConfig.backplaneChannelUrl;
 
         final Context tempContext = context;
         ThreadUtils.executeInBg(new Runnable() {
             public void run() {
                 loadUserFromDiskInternal(tempContext);
 
                 if (state.captureLocale != null && state.captureFlowName != null &&
                         state.captureAppId != null) {
                     loadFlow();
                     downloadFlow();
                 }
             }
         });
     }
 
     public static String getCaptureDomain() {
         return state.captureDomain;
     }
 
     public static String getCaptureClientId() {
         return state.captureClientId;
     }
 
     public static String getCaptureLocale() {
         return state.captureLocale;
     }
 
     public static String getCaptureTraditionalSignInFormName() {
         return state.captureTraditionalSignInFormName;
     }
 
     /**
      * @deprecated use com.janrain.android.Jump#getCaptureTraditionalSignInFormName
      */
     public static String getCaptureFormName() {
         return getCaptureTraditionalSignInFormName();
     }
 
     public static String getCaptureSocialRegistrationFormName() {
         return state.captureSocialRegistrationFormName;
     }
 
     public static String getCaptureTraditionalRegistrationFormName() {
         return state.captureTraditionalRegistrationFormName;
     }
 
     public static String getResponseType() {
         SignInResultHandler handler = state.signInHandler;
 
         if (handler instanceof SignInCodeHandler) {
             return "code_and_token";
         }
         return "token";
     }
 
     public static String getCaptureAppId() {
         return state.captureAppId;
     }
 
     public static String getCaptureFlowName() {
         return state.captureFlowName;
     }
 
     public static Map<String, Object> getCaptureFlow() {
         return state.captureFlow;
     }
 
     public static String getRedirectUri() {
         return "http://android.library";
     }
 
     public static String getBackplaneChannelUrl() {
         return state.backplaneChannelUrl;
     }
 
     public static boolean getCaptureEnableThinRegistration() {
         return state.captureEnableThinRegistration;
     }
 
     /**
      * @return the currently signed-in user, or null
      */
     public static CaptureRecord getSignedInUser() {
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
                 handleEngageAuthenticationSuccess(auth_info, provider, mergeToken);
                 state.jrEngage.removeDelegate(this);
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
         });
 
         if (providerName != null) {
             state.jrEngage.showAuthenticationDialog(fromActivity, providerName);
         } else {
             state.jrEngage.showAuthenticationDialog(fromActivity, TradSignInUi.class);
         }
     }
 
     private static void handleEngageAuthenticationSuccess(JRDictionary auth_info, String provider,
                                                           String mergeToken) {
         String authInfoToken = auth_info.getAsString("token");
 
         Capture.performSocialSignIn(authInfoToken, new Capture.SignInResultHandler() {
             public void onSuccess(CaptureRecord record, JSONObject response) {
                 state.signedInUser = record;
                 Jump.fireHandlerOnSuccess(response);
             }
 
             public void onFailure(CaptureApiError error) {
                 Jump.fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null));
             }
         }, provider, mergeToken);
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
     /*package*/ static void fireHandlerOnSuccess(JSONObject response) {
         SignInResultHandler handler_ = state.signInHandler;
         state.signInHandler = null;
         if (handler_ != null) {
             handler_.onSuccess();
 
             if (handler_ instanceof SignInCodeHandler) {
                 String code = response.optString("authorization_code");
                 ((SignInCodeHandler)handler_).onCode(code);
             }
         }
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
 
         state.signInHandler = handler;
 
         Capture.performTraditionalSignIn(signInName, password, state.traditionalSignInType,
                 new Capture.SignInResultHandler() {
                     @Override
                     public void onSuccess(CaptureRecord record, JSONObject response) {
                         state.signedInUser = record;
                         fireHandlerOnSuccess(response);
                     }
 
                     @Override
                     public void onFailure(CaptureApiError error) {
                         fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null));
                     }
                 }, mergeToken);
     }
 
     /**
      * Registers a new user record with Capture. Used for both traditional registrations and social two-step
      * registrations.
      *
      * Requires:
      *  - a flow name be configured when calling Jump.init
      *  - a social registration form be configured
      *  - a traditional registration form be configured
      *  - the Capture app ID be configured
      *
      * @param newUser A JSON object (which matches the record schema) used to populate the fields of the
      *                registration form.
      * @param socialRegistrationToken A social registration token, or null to perform a traditional
      *                                registration
      * @param registrationResultHandler A handler for the registration result
      */
     public static void registerNewUser(JSONObject newUser,
                                        String socialRegistrationToken,
                                        final SignInResultHandler registrationResultHandler) {
         if (state.jrEngage == null || state.captureDomain == null || state.captureFlowName == null ||
                 state.captureSocialRegistrationFormName == null ||
                 state.captureTraditionalRegistrationFormName == null || state.captureAppId == null) {
             registrationResultHandler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
             return;
         }
 
         state.signInHandler = registrationResultHandler;
 
         Capture.performRegistration(newUser, socialRegistrationToken, new Capture.SignInResultHandler(){
             public void onSuccess(CaptureRecord registeredUser, JSONObject result) {
                 fireHandlerOnSuccess(result);
             }
 
             public void onFailure(CaptureApiError error) {
                 fireHandlerOnFailure(new SignInError(CAPTURE_API_ERROR, error, null));
             }
         });
     }
 
     /**
      * An interface to receive callbacks notifying the completion of a sign-in flow.
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
     * An interface to receive a callback which handles the Capture OAuth Access Code that is generated on
      * the completion of the sign-in flow. Implement this interface in your sign in result handler if you
      * would like to receive the code.
      * See the Start Sign-in section of the jump.android/Docs/Jump_Integration_Guide.md for more information.
      */
     public interface SignInCodeHandler {
         /**
          * Called when Capture sign-in has succeeded.
          *
          * @param code An OAuth Authorization Code, this short lived code can be used to get an Access Token
          *   for use with a server side application like the Capture Drupal Plugin.
          */
         void onCode(String code);
     }
 
     /**
      * @deprecated Loading state from disk is now done automatically from Jump.init
      */
     public static void loadFromDisk(Context context) {
         loadUserFromDiskInternal(context);
     }
 
     /*package*/ static void loadUserFromDiskInternal(Context context) {
         state.signedInUser = CaptureRecord.loadFromDisk(context);
     }
 
     private static void loadFlow() {
         FileInputStream fis = null;
         ObjectInputStream ois = null;
         try {
             fis = state.context.openFileInput(JR_CAPTURE_FLOW);
             ois = new ObjectInputStream(fis);
             state.captureFlow = (Map<String, Object>) ois.readObject();
         } catch (ClassCastException e) {
             throwDebugException(e);
         } catch (FileNotFoundException ignore) {
         } catch (StreamCorruptedException e) {
             throwDebugException(new RuntimeException(e));
         } catch (IOException e) {
             throwDebugException(new RuntimeException(e));
         } catch (ClassNotFoundException e) {
             throwDebugException(new RuntimeException(e));
         } finally {
             try {
                 if (fis != null) fis.close();
             } catch (IOException ignore) {
             }
 
             try {
                 if (ois != null) ois.close();
             } catch (IOException ignore) {
             }
         }
     }
 
     private static void downloadFlow() {
         String flowVersion = state.captureFlowVersion != null ? state.captureFlowVersion : "HEAD";
 
         String flowUrlString =
                 String.format("https://%s.cloudfront.net/widget_data/flows/%s/%s/%s/%s.json",
                         state.flowUsesTestingCdn ? "dlzjvycct5xka" : "d1lqe9temigv1p",
                         state.captureAppId, state.captureFlowName, flowVersion,
                         state.captureLocale);
 
         ApiConnection c = new ApiConnection(flowUrlString);
         c.method = ApiConnection.Method.GET;
         c.fetchResponseAsJson(new ApiConnection.FetchJsonCallback() {
             public void run(JSONObject jsonObject) {
                 state.captureFlow = JsonUtils.jsonToCollection(jsonObject);
                 LogUtils.logd("Parsed flow, version: " + CaptureFlowUtils.getFlowVersion(state.captureFlow));
                 storeCaptureFlow();
             }
         });
     }
 
     private static void storeCaptureFlow() {
         FileOutputStream fos = null;
         ObjectOutputStream oos = null;
         try {
             fos = state.context.openFileOutput(JR_CAPTURE_FLOW, 0);
             oos = new ObjectOutputStream(fos);
             oos.writeObject(state.captureFlow);
         } catch (FileNotFoundException e) {
             throwDebugException(new RuntimeException(e));
         } catch (IOException e) {
             throwDebugException(new RuntimeException(e));
         } finally {
             try {
                 if (oos != null) oos.close();
             } catch (IOException ignore) {
             }
 
             try {
                 if (fos != null) fos.close();
             } catch (IOException ignore) {
             }
         }
     }
 
     /**
      * To be called from Activity#onPause
      * @param context the application context, used to interact with the disk
      */
     public static void saveToDisk(Context context) {
         if (state.signedInUser != null) state.signedInUser.saveToDisk(context);
     }
 
     /**
      * @return the downloaded flow's version, if any
      */
     public static String getCaptureFlowVersion() {
         Map<String, Object> captureFlow = getCaptureFlow();
         if (captureFlow == null) return null;
         return CaptureFlowUtils.getFlowVersion(captureFlow);
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
         if (state.jrEngage == null || state.captureDomain == null) {
             signInResultHandler.onFailure(new SignInError(JUMP_NOT_INITIALIZED, null, null));
             return;
         }
 
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
