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
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSession;
 import com.janrain.android.engage.session.JRSessionDelegate;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.ui.JRCustomInterface;
 import com.janrain.android.engage.ui.JRFragmentHostActivity;
 import com.janrain.android.engage.ui.JRPublishFragment;
 import com.janrain.android.engage.ui.JRUiFragment;
 import com.janrain.android.engage.utils.ThreadUtils;
 
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
  * This is all that is required for authentication, although some providers require
  * configuration (which can be done through your application's
  * <a href="http://rpxnow.com/relying_parties">Dashboard</a>). For social publishing, you will need to
  * configure your Engage application with the desired providers.
  *
  * You may optionally implement server-side authentication.
  * When configured, the Engage for Android library can post the user's authentication token to a URL on your
  * server: the token URL.  Your server can complete authentication, access more of the Engage web API, log
  * the authentication, etc. and the server's response will be passed back through to your Android application.
  *
  * To use JREngage:
  *  - Call JREngage.initInstance
  *  - Save the returned JREngage object
  *  - Invoke showAuthenticationDialog() or showSocialPublishingDialog(JRActivityObject activity)
  *  - You may implement JREngageDelegate to receive responses
  *
  * @nosubgrouping
  */
 public class JREngage {
     private static final String TAG = JREngage.class.getSimpleName();
 
     /**
      * If not set library logging is automatically controlled via the "debuggable" flag for the application
      * which is normally automatically set by the build system
      */
     public static Boolean sLoggingEnabled;
 
     private static boolean sInitalizationComplete = false;
     private static JREngage sInstance;
 
     private Context mApplicationContext;
     private Activity mActivityContext;
     private JRSession mSession;
     private final List<JREngageDelegate> mDelegates = new ArrayList<JREngageDelegate>();
     private final List<ConfigFinishListener> mConfigFinishListeners = new ArrayList<ConfigFinishListener>();
 
     private JREngage(Context context,
                      JREngageDelegate delegate) {
         mApplicationContext = context.getApplicationContext();
         if (context instanceof Activity) mActivityContext = (Activity) context;
         if (delegate != null && !mDelegates.contains(delegate)) mDelegates.add(delegate);
 
         // Sign-in UI fragment is not yet embeddable
         //if (activity1 instanceof FragmentActivity) {
         //    FrameLayout fragmentContainer = (FrameLayout) activity1.findViewById(R.id.jr_signin_fragment);
         //    if (fragmentContainer != null) {
         //    }
         //}
     }
 
 /**
  * @name Get the JREngage Instance
  * Methods that initialize and return the shared JREngage instance
  **/
 /*@{*/
 
     /**
      * Initializes and returns the singleton instance of JREngage.
      *
      * @param context  The Android Context used to access to system resources (e.g. global
      *                 preferences).  This value cannot be null
      * @param appId    Your 20-character application ID.  You can find this on your application's
      *                 Engage Dashboard at <a href="http://rpxnow.com">http://rpxnow.com</a>.  This value
      *                 cannot be null
      * @param tokenUrl The URL on your server where you wish to complete authentication, or null.  If
      *                 provided, the JREngage library will post the user's authentication token to this URL
      *                 where it can used for further authentication and processing.  When complete, the
      *                 library will pass the server's response back to the your application
      * @param delegate The delegate object that implements the JREngageDelegate interface
      * @return The shared instance of the JREngage object initialized with the given
      *         appId, tokenUrl, and delegate.  If the given appId is null, returns null
      */
     public static JREngage initInstance(final Context context,
                                         final String appId,
                                         final String tokenUrl,
                                         final JREngageDelegate delegate) {
         if (context == null) {
             Log.e(TAG, "[initialize] context parameter cannot be null.");
             throw new IllegalArgumentException("context parameter cannot be null.");
         }
 
         if (sLoggingEnabled == null) {
             sLoggingEnabled = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
         }
 
         if (TextUtils.isEmpty(appId)) {
             Log.e(TAG, "[initialize] appId parameter cannot be null.");
             throw new IllegalArgumentException("appId parameter cannot be null.");
         }
 
         logd(TAG, "[initInstance] git resource '" + context.getString(R.string.jr_git_describe) +
                 "' activity '" + context + "' appId '" + appId + "' tokenUrl '" + tokenUrl + "'");
 
         if (sInstance == null) {
             sInstance = new JREngage(context, delegate);
 
             // Initialize JRSession in background thread because it does a bunch of IO
             ThreadUtils.executeInBg(new Runnable() {
                 public void run() {
                     sInstance.mSession = JRSession.getInstance(appId, tokenUrl, sInstance.mJrsd);
 
                     // any use of the library is guarded by blockOnInitialization, which checks this ivar,
                     // acquires this lock and waits
                     synchronized (JREngage.class) {
                         sInitalizationComplete = true;
                         JREngage.class.notifyAll();
                     }
                 }
             });
         } else {
             Log.e(TAG, "Ignoring call which would reinitialize JREngage");
         }
 
         return sInstance;
     }
 
     /**
      * Returns the singleton instance, provided it has been initialized.
      *
      * @return The JREngage instance if properly initialized, null otherwise
      */
     public static JREngage getInstance() {
         return sInstance;
     }
     /*@}*/
 
     /**
      * @return The Activity object used to initialize this library
      * @internal Returns the Activity context used to initialize the library.
      */
     public static Context getApplicationContext() {
         return (sInstance == null) ? null : sInstance.mApplicationContext;
     }
 
     /**
      * @param context A Context to use to load resources. If an instance of Activity, from which
      *                startActivity will be called to show library Activities if not specified in the call to
      *                show*Dialog.
      * @internal
      * @deprecated specify the Activity context as a parameter to show*Dialog instead, and the application
      *             context as a parameter to JREngage.initInstance
      */
     public static void setContext(Context context) {
         sInstance.mApplicationContext = context;
         if (context instanceof Activity) sInstance.mActivityContext = (Activity) context;
     }
 
     /**
      * @param activity An Activity from which startActivity will be called
      * @internal
      * @deprecated specify the Activity context as a parameter to show*Dialog instead
      */
     public static void setActivityContext(Activity activity) {
         sInstance.mApplicationContext = activity.getApplicationContext();
         sInstance.mActivityContext = activity;
     }
 
     /**
      * Blocks the current thread until JRSession has finished initialization
      *
      * @internal
      * @hide
      */
     public synchronized static void blockOnInitialization() {
         if (sInitalizationComplete) return;
 
         try {
            JREngage.class.wait();
         } catch (InterruptedException e) {
             Log.e(TAG, "Unexpected InterruptedException");
         }
     }
 
 /**
  * @name Manage Authenticated Users
  * Methods that manage authenticated users remembered by the library
  **/
 /*@{*/
 
     /**
      * Remove the user's credentials for the given provider from the library.
      *
      * @param provider The name of the provider on which the user authenticated.
      *                 For a list of possible strings, please see the
      *                 <a href="http://documentation.janrain.com/engage/sdks/ios/mobile-providers#basicProviders">
      *                 List of Providers</a>
      */
     public void signoutUserForProvider(final String provider) {
         JREngage.logd(TAG, "[signoutUserForProvider]");
         blockOnInitialization();
         mSession.forgetAuthenticatedUserForProvider(provider);
     }
 
     /**
      * Remove the user's credentials for all providers from the library.
      */
     public void signoutUserForAllProviders() {
         JREngage.logd(TAG, "[signoutUserForAllProviders]");
         blockOnInitialization();
         mSession.forgetAllAuthenticatedUsers();
     }
 
     /**
      * Specify whether the Engage for Android library will require the user to reauthenticate.
      * Reauthentication will require the user to re-enter their password.
      *
      * @param force \c true if the library should force reauthentication for all providers or \c false if the
      *              library should allow cached credentials to authenticate the user
      */
     public void setAlwaysForceReauthentication(boolean force) {
         JREngage.logd(TAG, "[setAlwaysForceReauthentication]");
         blockOnInitialization();
         mSession.setAlwaysForceReauth(force);
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
      */
 
     public void cancelAuthentication() {
         JREngage.logd(TAG, "[cancelAuthentication]");
 
         finishJrActivities();
 
         mSession.triggerAuthenticationDidCancel();
     }
 
     /**
      * Stops the publishing flow.  This finishes all Engage for Android activities and returns
      * the calling Activity to the top of the application's activity stack.
      */
     public void cancelPublishing() {
         JREngage.logd(TAG, "[cancelPublishing]");
 
         finishJrActivities();
 
         mSession.triggerPublishingDidCancel();
     }
 /*@}*/
 
     /**
      * @internal
      */
     private void finishJrActivities() {
         Intent intent = new Intent(JRFragmentHostActivity.ACTION_FINISH_FRAGMENT);
         intent.putExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET,
                 JRFragmentHostActivity.FINISH_TARGET_ALL);
         mApplicationContext.sendBroadcast(intent);
     }
 
 /**
  * @name Server-side Authentication
  * Methods to configure server-side authentication
  **/
 /*@{*/
 
     /**
      * Specify a token URL (potentially a different token URL than the one the library was
      * initialized with).
      *
      * @param newTokenUrl The new token URL you wish authentications to post the Engage \e auth_info \e
      *                    token to
      */
     public void setTokenUrl(String newTokenUrl) {
         JREngage.logd(TAG, "[setTokenUrl]");
         mSession.setTokenUrl(newTokenUrl);
     }
 
 /*@}*/
 
 /**
  * @name Manage the JREngage Delegates
  * Add/remove delegates that implement the JREngageDelegate interface
  **/
 /*@{*/
 
     /**
      * Add a JREngageDelegate to the library.
      *
      * @param delegate The object that implements the JREngageDelegate interface
      */
     public synchronized void addDelegate(JREngageDelegate delegate) {
         JREngage.logd(TAG, "[addDelegate]");
         mDelegates.add(delegate);
     }
 
     /**
      * Remove a JREngageDelegate from the library.
      *
      * @param delegate The object that implements the JREngageDelegate interface
      */
     public synchronized void removeDelegate(JREngageDelegate delegate) {
         JREngage.logd(TAG, "[removeDelegate]");
         mDelegates.remove(delegate);
     }
 /*@}*/
 
     private void engageDidFailWithError(JREngageError error) {
         for (JREngageDelegate delegate : getDelegatesCopy()) {
             delegate.jrEngageDialogDidFailToShowWithError(error);
         }
     }
 
     private boolean checkSessionDataError() {
         JREngageError error = mSession.getError();
         if (error != null) {
             /* If an error, send a message to the delegates, then
               attempt to restart the configuration.  If, e.g., the error was temporary
               (network issues, etc.) restarting configuration could end successfully. */
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 engageDidFailWithError(error);
                 mSession.tryToReconfigureLibrary();
 
                 return true;
             }
         }
         return false;
     }
 
     private void checkNullJRActivity(JRActivityObject activity) {
         if (activity == null) throw new IllegalArgumentException("Illegal null activity object");
     }
 
 /** @anchor showMethods **/
 /**
  * @name Show the JREngage Dialogs
  * Methods that display the Engage for Android dialogs which initiate authentication and
  * social publishing
  **/
 /*@{*/
 
     /**
      * @deprecated use showAuthenticationDialog(Activity fromActivity) instead
      */
     public void showAuthenticationDialog() {
         JREngage.logd(TAG, "[showAuthenticationDialog]");
 
         showAuthenticationDialog(mActivityContext, false);
     }
 
     /**
      * Start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity The Activity from which to show the authentication dialog
      */
     public void showAuthenticationDialog(Activity fromActivity) {
         showAuthenticationDialog(fromActivity, false);
     }
 
     /**
      * Start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity    The Activity from which to show the authentication dialog
      * @param uiCustomization A Class reference to a subclass of JRCustomInterfaceConfiguration or J
      *                        RCustomInterfaceView
      *                        <p/>
      *                        If the reference is to a JRCustomView subclass then the an instance of that
      *                        custom view will be displayed as the header of the list of providers the user is
      *                        presented with. This header is the usual place to include custom
      *                        username/password authentication UI.
      *                        <p/>
      *                        If the reference is to a JRCustomInterfaceConfiguration subclass then all of
      *                        the customizations specified by an instance of that subclass will be applied.
      */
     public void showAuthenticationDialog(Activity fromActivity,
                                          Class<? extends JRCustomInterface> uiCustomization) {
         showAuthenticationDialog(fromActivity, false, null, uiCustomization);
     }
 
     /**
      * @param skipReturningUserLandingPage See
      *                                     showAuthenticationDialog(Activity fromActivity, boolean skipReturningUserLandingPage)
      * @deprecated use showAuthenticationDialog(Activity fromActivity, boolean skipReturningUserLandingPage)
      *             instead.
      */
     public void showAuthenticationDialog(boolean skipReturningUserLandingPage) {
         showAuthenticationDialog(mActivityContext, skipReturningUserLandingPage);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity                 The Activity from which to show the authentication dialog
      * @param skipReturningUserLandingPage Prevents the dialog from opening to the returning-user landing
      *                                     page when \c true.  That is, the dialog will always open straight
      *                                     to the list of providers.  The dialog falls back to the default
      *                                     behavior when \c false
      * @note If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      * setAlwaysForceReauthentication().
      */
     public void showAuthenticationDialog(Activity fromActivity, boolean skipReturningUserLandingPage) {
         showAuthenticationDialog(fromActivity, skipReturningUserLandingPage, null);
     }
 
     /**
      * @param provider See showAuthenticationDialog(Activity fromActivity, String provider)
      * @deprecated use showAuthenticationDialog(Activity fromActivity, String provider) instead
      */
     public void showAuthenticationDialog(String provider) {
         showAuthenticationDialog(mActivityContext, provider);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity The Activity from which to show the authentication dialog
      * @param provider     Specify a provider to start authentication with. No provider selection list will
      *                     be shown, the user will be brought directly to authentication with this provider.
      *                     If null the user will be shown the provider list as usual.
      * @note If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      * setAlwaysForceReauthentication().
      */
     public void showAuthenticationDialog(Activity fromActivity, String provider) {
         showAuthenticationDialog(fromActivity, null, provider, null);
     }
 
     /**
      * @param provider                     See showAuthenticationDialog(Activity fromActivity, String provider)
      * @param skipReturningUserLandingPage See showAuthenticationDialog(Activity fromActivity, String provider)
      * @deprecated use showAuthenticationDialog(Activity fromActivity, String provider) instead
      */
     public void showAuthenticationDialog(Boolean skipReturningUserLandingPage, String provider) {
         showAuthenticationDialog(mActivityContext, skipReturningUserLandingPage, provider);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity                 The Activity from which to show the authentication dialog
      * @param skipReturningUserLandingPage Prevents the dialog from opening to the returning-user landing
      *                                     page when \c true.  That is, the dialog will always open straight
      *                                     to the list of providers.  The dialog falls back to the default
      *                                     behavior when \c false
      * @param provider                     Specify a provider to start authentication with. No provider
      *                                     selection list will be shown, the user will be brought directly
      *                                     to authentication with this provider. If null the user will be
      *                                     shown the provider list as usual.
      * @note If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      * setAlwaysForceReauthentication().
      */
     public void showAuthenticationDialog(Activity fromActivity,
                                          Boolean skipReturningUserLandingPage,
                                          String provider) {
         showAuthenticationDialog(fromActivity, skipReturningUserLandingPage, provider, null);
     }
 
     /**
      * @param skipReturningUserLandingPage See the undeprecated method
      * @param provider                     See the undeprecated method
      * @param uiCustomization              See the undeprecated method
      * @deprecated use showAuthenticationDialog(Activity fromActivity, Boolean skipReturningUserLandingPage,
      *             String provider, Class&lt;? extends JRCustomInterface> uiCustomization) instead.
      */
     public void showAuthenticationDialog(Boolean skipReturningUserLandingPage,
                                          String provider,
                                          Class<? extends JRCustomInterface> uiCustomization) {
         showAuthenticationDialog(mActivityContext, skipReturningUserLandingPage, provider, uiCustomization);
     }
 
     /**
      * Begins authentication.  The library will
      * start a new Android Activity and take the user through the sign-in process.
      *
      * @param fromActivity                 The Activity from which to show the authentication dialog
      * @param skipReturningUserLandingPage Prevents the dialog from opening to the returning-user landing
      *                                     page when \c true.  That is, the dialog will always open straight
      *                                     to the list of providers.  The dialog falls back to the default
      *                                     behavior when \c false
      * @param provider                     Specify a provider to start authentication with. No provider
      *                                     selection list will be shown, the user will be brought directly
      *                                     to authentication with this provider. If null the user will be
      *                                     shown the provider list as usual.
      * @param uiCustomization              The custom sign-in object to display in the provider list. May be
      *                                     null for no custom sign-in.
      * @note If you always want to force the user to re-enter his/her credentials, pass \c true to the method
      * setAlwaysForceReauthentication().
      */
     public void showAuthenticationDialog(final Activity fromActivity,
                                          final Boolean skipReturningUserLandingPage,
                                          final String provider,
                                          final Class<? extends JRCustomInterface> uiCustomization) {
         blockOnInitialization();
         if (checkSessionDataError()) return;
 
         if (skipReturningUserLandingPage != null) {
             mSession.setSkipLandingPage(skipReturningUserLandingPage);
         }
 
         if (mSession.getProviderByName(provider) == null && !mSession.isConfigDone()) {
             final ProgressDialog pd = new ProgressDialog(fromActivity);
             pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             pd.setIndeterminate(true);
             pd.setCancelable(false);
             pd.show();
 
             // TODO add progress dialog customization
             // Fix up the progress dialog's appearance
             View message = pd.findViewById(android.R.id.message);
             if (message != null) message.setVisibility(View.GONE);
 
             View progressBar = pd.findViewById(android.R.id.progress);
             if (progressBar != null) collapseViewLayout(findViewHierarchyRoot(progressBar));
 
             mConfigFinishListeners.add(new ConfigFinishListener() {
                 public void configDidFinish() {
                     mConfigFinishListeners.remove(this);
                     checkSessionDataError();
                     showDirectProviderFlowInternal(fromActivity, provider, uiCustomization);
                     pd.dismiss();
                 }
             });
         } else {
             showDirectProviderFlowInternal(fromActivity, provider, uiCustomization);
         }
     }
 
     /**
      * @internal
      * @hide
      */
     private ViewGroup findViewHierarchyRoot(View v) {
         ViewParent parent = v.getParent();
         if (parent != null && parent instanceof ViewGroup) return findViewHierarchyRoot((View) parent);
         if (v instanceof ViewGroup) return (ViewGroup) v;
         return null;
     }
 
     /**
      * Dries out whitespace in layout hierarchies, for use with platform spinner dialog with too much
      *
      * @internal
      * @hide
      */
     private void collapseViewLayout(View v) {
         if (v.getLayoutParams() != null) {
             v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
             v.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
             if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                 ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMargins(0, 0, 0, 0);
             }
             if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                 ((LinearLayout.LayoutParams) v.getLayoutParams()).gravity = Gravity.CENTER;
             }
         }
 
         if (v instanceof ViewGroup) {
             int childCount = ((ViewGroup) v).getChildCount();
             for (int i = 0; i < childCount; i++) collapseViewLayout(((ViewGroup) v).getChildAt(i));
         }
     }
 
     /**
      * @internal
      * @hide
      */
     private interface ConfigFinishListener {
         void configDidFinish();
     }
 
     /**
      * @internal
      * @hide
      */
     private void showDirectProviderFlowInternal(final Activity fromActivity,
                                                 final String providerName,
                                                 final Class<? extends JRCustomInterface> uiCustomization) {
         Intent i;
         JRProvider provider = mSession.getProviderByName(providerName);
         if (provider != null) {
             if (provider.requiresInput()) {
                 i = JRFragmentHostActivity.createUserLandingIntent(fromActivity);
             } else {
                 i = JRFragmentHostActivity.createWebViewIntent(fromActivity);
             }
             i.putExtra(JRFragmentHostActivity.JR_PROVIDER, providerName);
             provider.setForceReauth(true);
             mSession.setCurrentlyAuthenticatingProvider(provider);
         } else {
             if (providerName != null) {
                 Log.e(TAG, "Provider " + providerName + " is not in the set of configured providers.");
             }
 
             i = JRFragmentHostActivity.createProviderListIntent(fromActivity);
             if (uiCustomization != null) {
                 i.putExtra(JRFragmentHostActivity.JR_UI_CUSTOMIZATION_CLASS, uiCustomization.getName());
             }
         }
 
         i.putExtra(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
         fromActivity.startActivity(i);
     }
 
     /**
      * @param jrActivity See the undeprecated method
      * @deprecated use showSocialPublishingDialog(Activity fromActivity, JRActivityObject activity) instead
      */
     public void showSocialPublishingDialog(JRActivityObject jrActivity) {
         showSocialPublishingDialog(mActivityContext, jrActivity);
     }
 
     /**
      * Begin social publishing.  The library will start a new Android \e Activity enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param fromActivity The Activity from which to show the sharing dialog
      * @param jrActivity   The activity you wish to share
      */
     public void showSocialPublishingDialog(Activity fromActivity, JRActivityObject jrActivity) {
         showSocialPublishingDialog(fromActivity, jrActivity, null);
     }
 
     /**
      * Begin social publishing.  The library will start a new Android \e Activity enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param fromActivity    The Activity from which to show the sharing dialog
      * @param jrActivity      The activity you wish to share
      * @param uiCustomization The custom sign-in object to display in the provider list. May be null for no
      *                        custom sign-in.
      */
     public void showSocialPublishingDialog(final Activity fromActivity,
                                            final JRActivityObject jrActivity,
                                            final Class<? extends JRCustomInterface> uiCustomization) {
         blockOnInitialization();
         JREngage.logd(TAG, "[showSocialPublishingDialog]");
         /* If there was error configuring the library, sessionData.error will not be null. */
         if (checkSessionDataError()) return;
         checkNullJRActivity(jrActivity);
         mSession.setJRActivity(jrActivity);
 
         Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(fromActivity, false);
         if (uiCustomization != null) {
             i.putExtra(JRFragmentHostActivity.JR_UI_CUSTOMIZATION_CLASS, uiCustomization.getName());
         }
         i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JRFragmentHostActivity.JR_PUBLISH);
         i.putExtra(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
         fromActivity.startActivity(i);
     }
 
     /**
      * Launch the beta direct share widget
      *
      * @param fromActivity The Activity from which to show the sharing dialog
      * @param jrActivity   The activity you wish to share
      */
     public void showBetaDirectShareDialog(Activity fromActivity,
                                           JRActivityObject jrActivity) {
         JREngage.logd(TAG, "[showBetaDirectShareDialog]");
         Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(fromActivity, false);
         i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JRFragmentHostActivity.JR_WEBVIEW);
         i.putExtra(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_BETA_DIRECT_SHARE);
         i.putExtra(JRUiFragment.JR_ACTIVITY_JSON, jrActivity.toJRDictionary().toJson());
         fromActivity.startActivity(i);
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      *
      * @param jrActivity           The activity you wish to share
      * @param hostActivity         The android.support.v4.app.FragmentActivity which will host the publishing
      *                             fragment
      * @param containerId          The resource ID of a FrameLayout to embed the publishing fragment in
      * @param addToBackStack       True if the publishing fragment should be added to the back stack, false
      *                             otherwise
      * @param transit              Select a standard transition animation for this transaction. See
      *                             FragmentTransaction#setTransition. Null for not set
      * @param transitRes           Set a custom style resource that will be used for resolving transit
      *                             animations. Null for not set
      * @param customEnterAnimation Set a custom enter animation. May be null if-and-only-if
      *                             customExitAnimation is also null
      * @param customExitAnimation  Set a custom exit animation.  May be null if-and-only-if
      *                             customEnterAnimation is also null
      */
     public void showSocialPublishingFragment(final JRActivityObject jrActivity,
                                              final FragmentActivity hostActivity,
                                              final int containerId,
                                              final boolean addToBackStack,
                                              final Integer transit,
                                              final Integer transitRes,
                                              final Integer customEnterAnimation,
                                              final Integer customExitAnimation) {
         JREngage.logd(TAG, "[showSocialPublishingFragment]");
         checkNullJRActivity(jrActivity);
         // does this need to block on initialization?
         blockOnInitialization();
 
         JRUiFragment f = createSocialPublishingFragment(jrActivity);
         Bundle arguments = new Bundle();
         arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
         f.setArguments(arguments);
         showFragment(f,
                 hostActivity,
                 containerId,
                 addToBackStack,
                 transit,
                 transitRes,
                 customEnterAnimation,
                 customExitAnimation);
     }
 
     /**
      * Begin social publishing.  The library will display a new Android \e Fragment enabling the user to
      * publish a social share.  The user will also be taken through the sign-in process, if necessary.
      * This simple variant displays the Fragment, does not add it to the Fragment back stack, and uses default
      * animations.
      *
      * @param jrActivity   The activity you wish to share
      * @param hostActivity The android.support.v4.app.FragmentActivity which will host the publishing fragment
      * @param containerId  The resource ID of a FrameLayout to embed the publishing fragment in
      */
     public void showSocialPublishingFragment(JRActivityObject jrActivity,
                                              FragmentActivity hostActivity,
                                              int containerId) {
         showSocialPublishingFragment(jrActivity, hostActivity, containerId, false, null, null, null, null);
     }
 
     /**
      * Create a new android.support.v4.Fragment for social publishing.  Use this if you wish to manage the
      * FragmentTransaction yourself.
      *
      * @param jrActivity The JRActivityObject to share, may not be null
      * @return The created Fragment, or null upon error (caused by library configuration failure)
      */
     public JRPublishFragment createSocialPublishingFragment(JRActivityObject jrActivity) {
         checkNullJRActivity(jrActivity);
 
         if (mSession != null) mSession.setJRActivity(jrActivity);
 
         Bundle arguments = new Bundle();
         arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_SHARING);
 
         JRPublishFragment f = new JRPublishFragment();
         f.setArguments(arguments);
         return f;
     }
 
     ///**
     // * Begin social sign-in.  The library will display a new Android \e Fragment enabling the user to
     // * sign in.
     // *
     // * @param hostActivity
     // *   The android.support.v4.app.FragmentActivity which will host the publishing fragment
     // * @param containerId
     // *   The resource ID of a FrameLayout to embed the publishing fragment in
     // * @param addToBackStack
     // *   True if the publishing fragment should be added to the back stack, false otherwise
     // * @param transit
     // *   Select a standard transition animation for this transaction. See FragmentTransaction#setTransition.
     // *   Null for not set
     // * @param transitRes
     // *   Set a custom style resource that will be used for resolving transit animations. Null for not set
     // * @param customEnterAnimation
     // *   Set a custom enter animation. May be null if-and-only-if customExitAnimation is also null
     // * @param customExitAnimation
     // *   Set a custom exit animation.  May be null if-and-only-if customEnterAnimation is also null
     // **/
     //public void showSocialSignInFragment(FragmentActivity hostActivity,
     //                                     int containerId,
     //                                     boolean addToBackStack,
     //                                     Integer transit,
     //                                     Integer transitRes,
     //                                     Integer customEnterAnimation,
     //                                     Integer customExitAnimation) {
     //    JREngage.logd(TAG, "[showSocialSignInFragment]");
     //
     //    JRUiFragment f = createSocialSignInFragment();
     //    Bundle arguments = new Bundle();
     //    arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
     //    f.setArguments(arguments);
     //    showFragment(f,
     //            hostActivity,
     //            containerId,
     //            addToBackStack,
     //            transit,
     //            transitRes,
     //            customEnterAnimation,
     //            customExitAnimation);
     //}
 
     ///**
     // * Begin social sign-in.  The library will display a new Android \e Fragment enabling the user to
     // * sign in.
     // * This simple variant displays the Fragment, does not add it to the Fragment back stack, and uses default
     // * animations.
     // *
     // * @param hostActivity The android.support.v4.app.FragmentActivity which will host the publishing fragment
     // * @param containerId  The resource ID of a FrameLayout to embed the publishing fragment in
     // */
     //public void showSocialSignInFragment(FragmentActivity hostActivity,
     //                                     int containerId) {
     //    showSocialSignInFragment(hostActivity, containerId, false, null, null, null, null);
     //}
 
     ///**
     // * Create a new android.support.v4.Fragment for social sign-in.  Use this if you wish to manage the
     // * FragmentTransaction yourself.
     // *
     // * @return The created Fragment, or null upon error (caused by library configuration failure)
     // */
     //public JRProviderListFragment createSocialSignInFragment() {
     //    if (checkSessionDataError()) return null;
     //
     //    JRProviderListFragment jplf = new JRProviderListFragment();
     //
     //    Bundle arguments = new Bundle();
     //    arguments.putInt(JRUiFragment.JR_FRAGMENT_FLOW_MODE, JRUiFragment.JR_FRAGMENT_FLOW_AUTH);
     //    jplf.setArguments(arguments);
     //    jplf.setArguments(arguments);
     //    return jplf;
     //}
 /*@}*/
 
     private void showFragment(Fragment fragment,
                               FragmentActivity hostActivity,
                               int containerId,
                               boolean addToBackStack,
                               Integer transit,
                               Integer transitRes,
                               Integer customEnterAnimation,
                               Integer customExitAnimation) {
         View fragmentContainer = hostActivity.findViewById(containerId);
         if (!(fragmentContainer instanceof FrameLayout)) {
             throw new IllegalStateException("No FrameLayout with ID: " + containerId + ". Found: " +
                     fragmentContainer);
         }
 
         FragmentManager fm = hostActivity.getSupportFragmentManager();
 
         FragmentTransaction ft = fm.beginTransaction();
         if (transit != null) ft.setTransition(transit);
         if (transitRes != null) ft.setTransitionStyle(transitRes);
         if (customEnterAnimation != null || customExitAnimation != null) {
             //noinspection ConstantConditions
             ft.setCustomAnimations(customEnterAnimation, customExitAnimation);
         }
         ft.replace(fragmentContainer.getId(), fragment, fragment.getClass().getSimpleName());
         if (addToBackStack) ft.addToBackStack(fragment.getClass().getSimpleName());
         ft.commit();
     }
 
 /** @anchor enableProviders **/
 /**
  * @name Enable a Subset of Providers
  * Methods that configure at runtime a subset of providers to use with the JREngage dialogs.  These methods
  * can only configure a subset of the configured and enabled providers found on your Engage application's
  * dashboard.
  **/
 /*@{*/
 
     /**
      * Sets the list of providers that are enabled for authentication.  This does not supersede your
      * RP's deplyoment settings for Android sign-in, as configured on rpxnow.com, it is a supplemental
      * filter to that configuration.
      *
      * @param enabledProviders A list of providers which will be enabled. This set will be intersected with
      *                         the set of providers configured on the Engage Dashboard, that intersection
      *                         will be the providers that are actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(final List<String> enabledProviders) {
         blockOnInitialization();
         mSession.setEnabledAuthenticationProviders(enabledProviders);
     }
 
     /**
      * Convenience variant of setEnabledAuthenticationProviders(List&lt;String>)
      *
      * @param enabledProviders An array of providers which will be enabled. This set will be intersected with
      *                         the set of providers configured on the Engage Dashboard, that intersection
      *                         will be the providers that are actually available to the end-user.
      */
     public void setEnabledAuthenticationProviders(final String[] enabledProviders) {
         blockOnInitialization();
         mSession.setEnabledAuthenticationProviders(Arrays.asList(enabledProviders));
     }
 
     /**
      * Sets the list of providers that are enabled for social sharing.  This does not supersede your
      * RP's deplyoment settings for Android social sharing, as configured on rpxnow.com, it is a
      * supplemental filter to that configuration.
      *
      * @param enabledSharingProviders Which providers to enable for authentication, null for all providers.
      *                                A list of social sharing providers which will be enabled. This set will
      *                                be intersected with the set of providers configured on the Engage
      *                                Dashboard, that intersection will be the providers that are actually
      *                                available to the end-user.
      */
     public void setEnabledSharingProviders(final List<String> enabledSharingProviders) {
         blockOnInitialization();
         mSession.setEnabledSharingProviders(enabledSharingProviders);
     }
 
     /**
      * Convenience variant of setEnabledSharingProviders(List&lt;String>)
      *
      * @param enabledSharingProviders An array of social sharing providers which will be enabled. This set
      *                                will be intersected with the set of providers configured on the Engage
      *                                Dashboard, that intersection will be the providers that are actually
      *                                available to the end-user.
      */
     public void setEnabledSharingProviders(final String[] enabledSharingProviders) {
         blockOnInitialization();
         mSession.setEnabledSharingProviders(Arrays.asList(enabledSharingProviders));
     }
 /*@}*/
 
     private JRSessionDelegate mJrsd = new JRSessionDelegate.SimpleJRSessionDelegate() {
         public void authenticationDidCancel() {
             JREngage.logd(TAG, "[authenticationDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrAuthenticationDidNotComplete();
         }
 
         public void authenticationDidComplete(JRDictionary profile, String provider) {
             JREngage.logd(TAG, "[authenticationDidComplete]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrAuthenticationDidSucceedForUser(profile, provider);
             }
         }
 
         public void authenticationDidFail(JREngageError error, String provider) {
             JREngage.logd(TAG, "[authenticationDidFail]");
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidFailWithError(error, provider);
             }
         }
 
         public void authenticationDidReachTokenUrl(String tokenUrl,
                                                    HttpResponseHeaders responseHeaders,
                                                    String payload,
                                                    String provider) {
             JREngage.logd(TAG, "[authenticationDidReachTokenUrl]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationDidReachTokenUrl(tokenUrl, responseHeaders, payload, provider);
             }
         }
 
         public void authenticationCallToTokenUrlDidFail(String tokenUrl,
                                                         JREngageError error,
                                                         String provider) {
             JREngage.logd(TAG, "[authenticationCallToTokenUrlDidFail]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) {
                 delegate.jrAuthenticationCallToTokenUrlDidFail(tokenUrl, error, provider);
             }
         }
 
         public void publishingDidCancel() {
             JREngage.logd(TAG, "[publishingDidCancel]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrSocialDidNotCompletePublishing();
         }
 
         public void publishingDidComplete() {
             JREngage.logd(TAG, "[publishingDidComplete]");
 
             for (JREngageDelegate delegate : getDelegatesCopy()) delegate.jrSocialDidCompletePublishing();
         }
 
         public void publishingDialogDidFail(JREngageError error) {
             engageDidFailWithError(error);
 
             if (JREngageError.ErrorType.CONFIGURATION_FAILED.equals(error.getType())) {
                 mSession.tryToReconfigureLibrary();
             }
         }
 
         public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
             JREngage.logd(TAG, "[publishingJRActivityDidSucceed]");
 
             for (JREngageDelegate d : getDelegatesCopy()) d.jrSocialDidPublishJRActivity(activity, provider);
         }
 
         public void publishingJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
             JREngage.logd(TAG, "[publishingJRActivityDidFail]");
 
             for (JREngageDelegate d : getDelegatesCopy()) {
                 d.jrSocialPublishJRActivityDidFail(activity, error, provider);
             }
         }
 
         @Override
         public void configDidFinish() {
             for (ConfigFinishListener cfl : new ArrayList<ConfigFinishListener>(mConfigFinishListeners)) {
                 cfl.configDidFinish();
             }
         }
     };
 
     private synchronized List<JREngageDelegate> getDelegatesCopy() {
         return new ArrayList<JREngageDelegate>(mDelegates);
     }
 
     public static void logd(String tag, String msg, Throwable tr) {
         if (sLoggingEnabled == null || sLoggingEnabled) Log.d(tag, msg, tr);
     }
 
     public static void logd(String tag, String msg) {
         if (sLoggingEnabled == null || sLoggingEnabled) Log.d(tag, msg);
     }
 }
 
 /**
  * @page Providers
  *
 @htmlonly
 <!-- Script to resize the iFrames; Only works because iFrames origin is on same domain and iFrame
       code contains script that calls this script -->
 <script type="text/javascript">
     function resize(width, height, id) {
         var iframe = document.getElementById(id);
         iframe.width = width;
         iframe.height = height + 50;
         iframe.scrolling = false;
         console.log(width);
         console.log(height);
     }
 </script>
 
 <!-- Redundant attributes to force scrolling to work across multiple browsers -->
 <iframe id="intro" src="../mobile_providers?list=intro&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
 </iframe>
 @endhtmlonly
 
 @anchor basicProviders
 @htmlonly
 <iframe id="basic" src="../mobile_providers?list=basic&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
   <a href="../mobile_providers?list=basic&device=android">List of Providers</a>
 </iframe></p>
 @endhtmlonly
 
 @anchor socialProviders
 @htmlonly
 <iframe id="social" src="../mobile_providers?list=social&device=android" width="100%" height="100%"
     style="border:none; overflow:hidden;" frameborder="0" scrolling="no">
   Your browser does not support iFrames.
   <a href="../mobile_providers?list=social&device=android">List of Social Providers</a>
 </iframe></p>
 @endhtmlonly
  *
  **/
