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
 
 
 // package.html type package documentation tag for Doxygen
 
 // removed from the docs until these features are implemented:
 //- Customize the sign-in experience by displaying native and social sign-in options on the same screen
 //- Match the look and feel of the iPhone app with customizable background colors, images, and navigation bar tints
 
 
 /**
  * @mainpage Janrain Engage Android
  *
  * <a href="http://rpxnow.com/docs/android">The
  * Janrain Engage for Android SDK</a> makes it easy to include third party authentication and
  * social publishing in your Android app.  This library includes the same key
  * features as Janrain Engage for the web, as well as additional features created specifically for
  * the mobile platform. With as few as three lines of code, you can authenticate your users with 
  * their accounts on Google, Yahoo!, Facebook, etc., and they can immediately publish their
  * activities to multiple social networks, including Facebook, Twitter, LinkedIn, MySpace,
  * and Yahoo, through one simple interface.
  *
  * Beyond authentication and social sharing, the latest release of the Engage for Android SDK
  * now allows mobile apps to:
  *   - Share content, activities, game scores or invitations via Email or SMS
  *   - Track popularity and click through rates on various links included in the
  *     shared email message with automatic URL shortening for up to 5 URLs
  *   - Provide an additional level of security with forced re-authentication when
  *     users are about to make a purchase or conduct a sensitive transaction
  *   - Configure and maintain separate lists of providers for mobile and web apps
  *
  * Before you begin, you need to have created a <a href="https://rpxnow.com/signup_createapp_plus">
  * Janrain Engage application</a>,
  * which you can do on <a href="http://rpxnow.com">http://rpxnow.com</a>
  *
  * For an overview of how the library works and how you can take advantage of the library's 
  * features, please see the <a href="http://rpxnow.com/docs/android#user_experience">"Overview"</a> 
  * section of our documentation.
  *
  * To begin using the SDK, please see the 
  * <a href="http://rpxnow.com/docs/android#quick">"Quick Start Guide"</a>.
  *
  * For more detailed documentation of the library's API, you can use
  * the <a href="http://rpxnow.com/docs/android_api/annotated.html">"JREngage API"</a> documentation.
  **/
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.view.View;
 import android.widget.FrameLayout;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.session.JRSessionData;
 import com.janrain.android.engage.session.JRSessionDelegate;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.ui.JRFragmentHostActivity;
 import com.janrain.android.engage.ui.JRPublishFragment;
 import com.janrain.android.engage.ui.JRUiFragment;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @brief
  * The JREngage class provides methods for configuring the Engage for Android library and
  * initiating authentication and social sharing.
  *
  * Prior to using the Engage for Android
  * library, you must already have an application on <a href="http://rpxnow.com">http://rpxnow.com</a>.
  * This is all that is required for basic authentication, although some providers require extra
  * configuration (which can be done through your application's <a href="http://rpxnow.com/relying_parties">Dashboard</a>).
  * For social publishing, you will need to configure your Engage application with the desired providers.
  *
  * You may optionally implement server-side authentication.
  * When configured, the Engage for Android library can post the user's authentication token to a URL on your server:
  * the token URL.  Your server can complete authentication, access more of the Engage web API, log the authentication, etc.
  * and the server's response will be passed back through to your Android application.
  *
  * To use JREngage:
  *  - Call JREngage.initInstance
  *  - Save the returned JREngage object
  *  - Invoke showAuthenticationDialog() or showSocialPublishingDialog(JRActivityObject activity)
  *  - You may implement JREngageDelegate to receive responses
  *
  * @nosubgrouping
  **/
 public class JREngage {
 	private static final String TAG = JREngage.class.getSimpleName();
 
     /* Singleton instance of this class */
 	private static JREngage sInstance;
 
     /* Application context */
     private Activity mActivity;
 
 	/* Holds configuration and state for the JREngage library */
 	private JRSessionData mSessionData;
 
 	/* Delegates (listeners) array */
 	private ArrayList<JREngageDelegate> mDelegates = new ArrayList<JREngageDelegate>();
 
     private JREngage() {}
 
 /**
  * @name Get the JREngage Instance
  * Methods that initialize and return the shared JREngage instance
  **/
 /*@{*/
     /**
      * Initializes and returns the singleton instance of JREngage.
      *
      * @param activity
      * 		The Android Activity, used for access to system resources (e.g. global
      * 		preferences).  This value cannot be null
      *
      * @param appId
      * 		Your 20-character application ID.  You can find this on your application's
      * 		Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
      * 		cannot be null
      *
      * @param tokenUrl
      * 		The url on your server where you wish to complete authentication, or null.  If provided,
      *   	the JREngage library will post the user's authentication token to this url where it can
      *   	used for further authentication and processing.  When complete, the library will pass
      *   	the server's response back to the your application
 
      * @param delegate
      * 		The delegate object that implements the JREngageDelegate protocol
      *
      * @return
      * 		The shared instance of the JREngage object initialized with the given
      *   	appId, tokenUrl, and delegate.  If the given appId is null, returns null
      **/
     public static JREngage initInstance(Activity activity,
                                         String appId,
                                         String tokenUrl,
                                         JREngageDelegate delegate) {
         if (activity == null) {
             Log.e(TAG, "[initialize] context parameter cannot be null.");
             throw new IllegalArgumentException("context parameter cannot be null.");
         }
 
         if (TextUtils.isEmpty(appId)) {
             Log.e(TAG, "[initialize] appId parameter cannot be null.");
             throw new IllegalArgumentException("appId parameter cannot be null.");
         }
 
         if (sInstance == null) sInstance = new JREngage();
         sInstance.initialize(activity, appId, tokenUrl, delegate);
 
         return sInstance;
     }
 
     /**
      * Initializes and returns the singleton instance of JREngage.
      *
      * @deprecated Use #initInstance(Activity, String, String, JREngageDelegate) instead.
      *
      * @param context
      * 		The Android Activity, used for access to system resources (e.g. global
      * 		preferences).  This value cannot be null
      *
      * @param appId
      * 		Your 20-character application ID.  You can find this on your application's
      * 		Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
      * 		cannot be null
      *
      * @param tokenUrl
      * 		The url on your server where you wish to complete authentication, or null.  If provided,
      *   	the JREngage library will post the user's authentication token to this url where it can
      *   	used for further authentication and processing.  When complete, the library will pass
      *   	the server's response back to the your application
 
      * @param delegate
      * 		The delegate object that implements the JREngageDelegate protocol
      *
      * @return
      * 		The shared instance of the JREngage object initialized with the given
      *   	appId, tokenUrl, and delegate.  If the given appId is null, returns null
      **/
     public static JREngage initInstance(Context context,
                                         String appId,
                                         String tokenUrl,
                                         JREngageDelegate delegate) {
         return initInstance((Activity) context, appId, tokenUrl, delegate);
     }
 
 	/**
 	 * Returns the singleton instance, provided it has been initialized.
 	 *
 	 * @return
 	 * 		The JREngage instance if properly initialized, null otherwise
 	 **/
 	public static JREngage getInstance() {
 		return sInstance;
 	}
  /*@}*/
 
 	/**
      * @internal
      * Returns the application context used to initialize the library.
 	 *
 	 * @return
 	 * 		The Context object used to initialize this library
 	 **/
     public static Context getActivity() {
         return (sInstance == null) ? null : sInstance.mActivity;
     }
 
     /**
      * @internal
      * @deprecated use setActivityContext(Activity) instead
      * Set the Activity used to start Engage dialogs from
      *
      * @param context
      *      An Activity from which startActivity will be called
      *
      **/
     public static void setContext(Context context) {
         sInstance.mActivity = (Activity) context;
     }
 
     /**
      * @internal
      * Returns the application context used to initialize the library.
      *
      * @param activity
      *      An Activity from which startActivity will be called
      **/
     public static void setActivityContext(Activity activity) {
         sInstance.mActivity = activity;
     }
 
 	private void initialize(Activity activity,
                             String appId,
                             String tokenUrl,
                             JREngageDelegate delegate) {
         mActivity = activity;
         mDelegates = new ArrayList<JREngageDelegate>();
         if (delegate != null) mDelegates.add(delegate);
         mSessionData = JRSessionData.getInstance(appId, tokenUrl, mJrsd);
 
         // Sign-in UI fragment is not yet embeddable
         //if (activity1 instanceof FragmentActivity) {
         //    FrameLayout fragmentContainer = (FrameLayout) activity1.findViewById(R.id.jr_signin_fragment);
         //    if (fragmentContainer != null) {
         //    }
         //}
 	}
 
     private JRSessionDelegate mJrsd = new JRSessionDelegate.SimpleJRSessionDelegate() {
         public void authenticationDidRestart() {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidRestart]");
         }
 
         public void authenticationDidCancel() {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrAuthenticationDidNotComplete();
         }
 
         public void authenticationDidComplete(JRDictionary profile, String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidComplete]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrAuthenticationDidSucceedForUser(profile, provider);
             }
         }
 
         public void authenticationDidFail(JREngageError error, String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidFail]");
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidFailWithError(error, provider);
             }
         }
 
         public void authenticationDidReachTokenUrl(String tokenUrl,
                                                    HttpResponseHeaders responseHeaders,
                                                    String payload,
                                                    String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidReachTokenUrl]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, responseHeaders, payload, provider);
             }
         }
 
         public void authenticationCallToTokenUrlDidFail(String tokenUrl,
                                                         JREngageError error,
                                                         String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationCallToTokenUrlDidFail]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationCallToTokenUrlDidFail(tokenUrl, error, provider);
             }
         }
 
         public void publishingDidCancel() {
             if (Config.LOGD) Log.d(TAG, "[publishingDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrSocialDidNotCompletePublishing();
             }
         }
 
         public void publishingDidComplete() {
             if (Config.LOGD) Log.d(TAG, "[publishingDidComplete]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrSocialDidCompletePublishing();
         }
 
         public void publishingDialogDidFail(JREngageError error) {
             engageDidFailWithError(error);
 
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 mSessionData.tryToReconfigureLibrary();
             }
         }
 
         public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
             if (Config.LOGD) Log.d(TAG, "[publishingJRActivityDidSucceed]");
 
             for (JREngageDelegate d : getDelegatesCopy()) d.jrSocialDidPublishJRActivity(activity, provider);
         }
 
         public void publishingJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
             if (Config.LOGD) Log.d(TAG, "[publishingJRActivityDidFail]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrSocialPublishJRActivityDidFail(activity, error, provider);
             }
         }
 
         // This doesn't work because JREngageDelegate doesn't have an appropriate method to announce an
         // error event independent of the display of a dialog
         //@Override
         //public void mobileConfigDidFinish() {
         //    JREngageError err = mSessionData.getError();
         //    if (err != null) {
         //        engageDidFailWithError(err);
         //    }
         //}
     };
 
 /**
  * @name Manage Authenticated Users
  * Methods that manage authenticated users remembered by the library
  **/
 /*@{*/
     /**
      * Remove the user's credentials for the given provider from the library.
      *
      * @param provider
      *   The name of the provider on which the user authenticated.
      *   For a list of possible strings, please see the
      *   <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
      *   List of Providers</a>
      **/
     public void signoutUserForProvider(String provider) {
         if (Config.LOGD) Log.d(TAG, "[signoutUserForProvider]");
         mSessionData.forgetAuthenticatedUserForProvider(provider);
     }
 
     /**
      * Remove the user's credentials for all providers from the library.
      **/
     public void signoutUserForAllProviders() {
         if (Config.LOGD) Log.d(TAG, "[signoutUserForAllProviders]");
         mSessionData.forgetAllAuthenticatedUsers();
     }
 
     /**
      * Specify whether the Engage for Android library will require the user to reauthenticate.
      * Reauthentication will require the user to re-enter their password.
      *
      * @param force
      *   \c true if the library should force reauthentication for all providers or \c false if the
      *   library should allow cached credentials to authenticate the user
      **/
     public void setAlwaysForceReauthentication(boolean force) {
         if (Config.LOGD) Log.d(TAG, "[setAlwaysForceReauthentication]");
         mSessionData.setAlwaysForceReauth(force);
     }
 /*@}*/
 
 /**
  * @name Cancel the JREngage Dialogs
  * Methods to cancel authentication and social publishing
  **/
 /*@{*/
     /**
      * Stops the authentication flow.  This finishes all Engage for Android activities and returns
      * the calling Activity to the top of the application's activity stack.
      **/
 
     public void cancelAuthentication() {
         if (Config.LOGD) Log.d(TAG, "[cancelAuthentication]");
 
         finishJrActivities();
 
         mSessionData.triggerAuthenticationDidCancel();
     }
 
     /**
      * Stops the publishing flow.  This finishes all Engage for Android activities and returns
      * the calling Activity to the top of the application's activity stack.
      **/
     public void cancelPublishing() {
         if (Config.LOGD) Log.d(TAG, "[cancelPublishing]");
 
         finishJrActivities();
 
         mSessionData.triggerPublishingDidCancel();
     }
 
     private void finishJrActivities() {
         Intent intent = new Intent(JRFragmentHostActivity.ACTION_FINISH_FRAGMENT);
         intent.putExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET,
                 JRFragmentHostActivity.FINISH_TARGET_ALL);
         mActivity.sendBroadcast(intent);
     }
 /*@}*/
 
 /**
  * @name Server-side Authentication
  * Methods to configure server-side authentication
  **/
 /*@{*/
 
     /**
      * Specify a token URL (potentially a different token URL than the one the library was
      * initialized with).
      *
      * @param newTokenUrl
      *   The new token URL you wish authentications to post the Engage \e auth_info \e token to
      **/
     public void setTokenUrl(String newTokenUrl) {
         if (Config.LOGD) Log.d(TAG, "[setTokenUrl]");
         mSessionData.setTokenUrl(newTokenUrl);
     }
 
     /**
      * Sets the list of providers that are enabled for authentication.  This does not supersede your
      * RP's deplyoment settings for Android sign-in, as configured on rpxnow.com, it is a supplemental
      * filter to that configuration.
      *
      * @param enabledProviders
      *  A list of providers which will be enabled. This set will be intersected with the set of
      *  providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(List<String> enabledProviders) {
         mSessionData.setEnabledAuthenticationProviders(enabledProviders);
     }
 
     /**
      * Convenience variant of setEnabledAuthenticationProviders(List&lt;String>)
      * @param enabledProviders
      *  An array of providers which will be enabled. This set will be intersected with the set of
      *  providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(String[] enabledProviders) {
         mSessionData.setEnabledAuthenticationProviders(Arrays.asList(enabledProviders));
     }
 
     /**
      * Sets the list of providers that are enabled for social sharing.  This does not supersede your
      * RP's deplyoment settings for Android social sharing, as configured on rpxnow.com, it is a
      * supplemental filter to that configuration.
      *
      * @param enabledSharingProviders
      *  Which providers to enable for authentication, null for all providers.
      *  A list of social sharing providers which will be enabled. This set will be intersected with the
      *  set of providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledSharingProviders(List<String> enabledSharingProviders) {
         mSessionData.setEnabledSharingProviders(enabledSharingProviders);
     }
 
     /**
      * Convenience variant of setEnabledSharingProviders(List&lt;String>)
      * @param enabledSharingProviders
      *  An array of social sharing providers which will be enabled. This set will be intersected with the
      *  set of providers configured on the Engage Dashboard, that intersection will be the providers that are
      *  actually available to the end-user.
      */
     public void setEnabledSharingProviders(String[] enabledSharingProviders) {
         mSessionData.setEnabledSharingProviders(Arrays.asList(enabledSharingProviders));
     }
 /*@}*/
 
 /**
  * @name Manage the JREngage Delegates
  * Add/remove delegates that implement the JREngageDelegate protocol
  **/
 /*@{*/
 
     /**
      * Add a JREngageDelegate to the library.
      *
      * @param delegate
      *   The object that implements the JREngageDelegate protocol
      **/
     public synchronized void addDelegate(JREngageDelegate delegate) {
 		if (Config.LOGD) Log.d(TAG, "[addDelegate]");
 		mDelegates.add(delegate);
 	}
 
     /**
      * Remove a JREngageDelegate from the library.
      *
      * @param delegate
      *   The object that implements the JREngageDelegate protocol
      **/
 	public synchronized void removeDelegate(JREngageDelegate delegate) {
 		if (Config.LOGD) Log.d(TAG, "[removeDelegate]");
 		mDelegates.remove(delegate);
 	}
 /*@}*/
 
     private void engageDidFailWithError(JREngageError error) {
         for (JREngageDelegate delegate : getDelegatesCopy()) {
             delegate.jrEngageDialogDidFailToShowWithError(error);
         }
     }
 
     private boolean checkSessionDataError() {
         /* If there was error configuring the library, sessionData.error will not be null. */
         JREngageError error = mSessionData.getError();
         if (error != null) {
             /* If there was an error, send a message to the delegates, then
               attempt to restart the configuration.  If, for example, the error was temporary
               (network issues, etc.) reattempting to configure the library could end successfully.
               Since configuration may happen before the user attempts to use the library, if the
               user attempts to use the library at all, we only try to reconfigure when the library
               is needed. */
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 engageDidFailWithError(error);
                 mSessionData.tryToReconfigureLibrary();
 
                 return true;
             }
         }
         return false;
     }
 
     private void checkNullJRActivity(JRActivityObject activity) {
         if (activity == null) {
             throw new IllegalArgumentException("Illegal null activity object");
         }
     }
 
 /** @anchor showMethods **/
 /**
  * @name Show the JREngage Dialogs
  * Methods that display the Engage for Android dialogs which initiate authentication and
  * social publishing
  **/
 /*@{*/
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      **/
     public void showAuthenticationDialog() {
         if (Config.LOGD) Log.d(TAG, "[showAuthenticationDialog]");
 
         showAuthenticationDialog(false);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param skipReturningUserLandingPage
      *  Prevents the dialog from opening to the returning-user landing page when \c true.  That is, the
      *  dialog will always open straight to the list of providers.  The dialog falls back to the default
      *  behavior when \c false
      *
      * @note
      *  If you always want to force the user to re=enter his/her credentials, pass \c true to the method
      *  setAlwaysForceReauthentication().
      **/
     public void showAuthenticationDialog(boolean skipReturningUserLandingPage) {
         if (Config.LOGD) Log.d(TAG, "[showAuthenticationDialog]: " + skipReturningUserLandingPage);
 
         if (checkSessionDataError()) return;
 
         mSessionData.setSkipLandingPage(skipReturningUserLandingPage);
 
         Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(mActivity, true);
         i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JRFragmentHostActivity.JR_PROVIDER_LIST);
         mActivity.startActivity(i);
     }
 
     /**
      * Begin social publishing.  The library will start a new Android \e Activity enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param activity
      *   The activity you wish to share
      **/
     public void showSocialPublishingDialog(JRActivityObject activity) {
         if (Config.LOGD) Log.d(TAG, "[showSocialPublishingDialog]");
         /* If there was error configuring the library, sessionData.error will not be null. */
         if (checkSessionDataError()) return;
         checkNullJRActivity(activity);
         mSessionData.setJRActivity(activity);
 
         Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(mActivity, false);
         i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JRFragmentHostActivity.JR_PUBLISH);
         mActivity.startActivity(i);
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param activity
      *   The activity you wish to share
      * @param hostActivity
      *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
      * @param containerId
      *   The resource ID of a FrameLayout to embed the publishing fragment in
      * @param addToBackStack
      *   True if the publishing fragment should be added to the back stack, false otherwise
      * @param transit
      *   Select a standard transition animation for this transaction. See FragmentTransaction#setTransition.
      *   Null for not set
      * @param transitRes
      *   Set a custom style resource that will be used for resolving transit animations. Null for not set
      * @param customEnterAnimation
      *   Set a custom enter animation. May be null if-and-only-if customExitAnimation is also null
      * @param customExitAnimation
      *   Set a custom exit animation.  May be null if-and-only-if customEnterAnimation is also null
      *
      * @throws IllegalArgumentException
      *  If the supplied activity object is null
      **/
     public void showSocialPublishingFragment(JRActivityObject activity,
                                              FragmentActivity hostActivity,
                                              int containerId,
                                              boolean addToBackStack,
                                              Integer transit,
                                              Integer transitRes,
                                              Integer customEnterAnimation,
                                              Integer customExitAnimation) {
         if (Config.LOGD) Log.d(TAG, "[showSocialPublishingFragment]");
         if (checkSessionDataError()) return;
 
         checkNullJRActivity(activity);
 
         View fragmentContainer = hostActivity.findViewById(containerId);
         if (!(fragmentContainer instanceof FrameLayout)) {
             throw new IllegalStateException("No FrameLayout with ID: " + containerId + ". Found: " +
                     fragmentContainer);
         }
 
         FragmentManager fm = hostActivity.getSupportFragmentManager();
 
         JRUiFragment f = createSocialPublishingFragment(activity);
         FragmentTransaction ft = fm.beginTransaction();
         if (transit != null) ft.setTransition(transit);
         if (transitRes != null) ft.setTransitionStyle(transitRes);
         if (customEnterAnimation != null || customExitAnimation != null) {
             //noinspection ConstantConditions
             ft.setCustomAnimations(customEnterAnimation, customExitAnimation);
         }
         ft.replace(fragmentContainer.getId(), f, f.getClass().getSimpleName());
         if (addToBackStack) ft.addToBackStack(JRPublishFragment.class.getSimpleName());
         ft.commit();
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      * This simple variant displays the Fragment, does not add it to the Fragment back stack, and uses default
      * animations.
      *
      * @param activity
      *   The activity you wish to share
      * @param hostActivity
      *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
      * @param containerId
      *   The resource ID of a FrameLayout to embed the publishing fragment in
      *
      * @throws IllegalArgumentException
      *  If the supplied activity object is null
      **/
     public void showSocialPublishingFragment(JRActivityObject activity,
                                              FragmentActivity hostActivity,
                                              int containerId) {
         showSocialPublishingFragment(activity, hostActivity, containerId, false, null, null, null, null);
     }
 
     /**
      * Create a new android.support.v4.Fragment for social publishing.  Use this if you wish to manage the
      * FragmentTransaction yourself.
      *
      * @param activity
      *  The JRActivityObject to share, may not be null
      *
      * @return
      *  The created Fragment, or null upon error (caused by library configuration failure)
      *  
      * @throws IllegalArgumentException
      *  If the supplied activity object is null
      */
     public JRPublishFragment createSocialPublishingFragment(JRActivityObject activity) {
         if (checkSessionDataError()) return null;
 
         checkNullJRActivity(activity);
 
         mSessionData.setJRActivity(activity);
        JRPublishFragment f = new JRPublishFragment();
        //f.setEmbeddedMode(true);

        return f;
     }
 /*@}*/
 
     private synchronized List<JREngageDelegate> getDelegatesCopy() {
         return new ArrayList<JREngageDelegate>(mDelegates);
     }
 }
