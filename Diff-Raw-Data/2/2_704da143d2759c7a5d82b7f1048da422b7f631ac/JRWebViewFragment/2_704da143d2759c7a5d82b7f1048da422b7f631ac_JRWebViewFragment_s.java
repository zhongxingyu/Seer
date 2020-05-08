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
 package com.janrain.android.engage.ui;
 
 import java.net.URL;
 import java.util.List;
 
 import android.app.Dialog;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.DownloadListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ProgressBar;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.AndroidUtils;
 
 /**
  * @internal
  *
  * @class JRWebViewActivity
  * Container for authentication web view.
  */
 public class JRWebViewFragment extends JRUiFragment {
     private static final String KEY_DIALOG_TITLE = "jr_dialog_title";
     private static final String KEY_DIALOG_MESSAGE = "jr_dialog_message";
     private static final String KEY_PROVIDER_NAME = "jr_saved_provider_name";
     private static final String KEY_IS_ALERT_SHOWING = "mIsAlertShowing";
     private static final String KEY_IS_FINISH_PENDING = "mIsFinishPending";
     private static final String KEY_IS_LOADING_MOBILE_ENDPOINT = "mIsLoadingMobileEndpoint";
     private static final String KEY_IS_SPINNER_ON = "jr_spinner_on";
     private static final String JR_RETAIN = "jr_retain_frag";
     private static final int KEY_ALERT_DIALOG = 1;
 
     public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER;
     public static final int RESULT_FAIL = Activity.RESULT_FIRST_USER + 1;
     public static final int RESULT_BAD_OPENID_URL = Activity.RESULT_FIRST_USER + 2;
     
     private WebView mWebView;
     private boolean mIsAlertShowing = false;
     private boolean mIsFinishPending = false;
     private boolean mIsLoadingMobileEndpoint = false;
 //    private boolean mUseDesktopUa = false;
     private JRProvider mProvider;
     private WebSettings mWebViewSettings;
     private ProgressBar mProgressSpinner;
     private RetainFragment mRetain;
 
     public JRWebViewFragment() {}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if (mSession == null) return null;
         View view = inflater.inflate(R.layout.jr_provider_webview, container, false);
 
         mWebView = (WebView)view.findViewById(R.id.jr_webview);
         mProgressSpinner = (ProgressBar)view.findViewById(R.id.jr_webview_progress);
 
         mWebViewSettings = mWebView.getSettings();
         mWebViewSettings.setSavePassword(false);
 
         // Shim some information about the OS version into the WebView for use by hax ala Yahoo:
         mWebView.addJavascriptInterface(new Object() {
         			// These functions may be invoked via the javascript binding, but they are
         			// never invoked from this Java code, so they will always generate compiler
         			// warnings, so those warnings are suppressed safely.
                     @SuppressWarnings("unused")
 					String getAndroidIncremental() {
                         return Build.VERSION.INCREMENTAL;
                     }
 
                     @SuppressWarnings("unused")
 					String getAndroidRelease() {
                         return Build.VERSION.RELEASE;
                     }
 
                     @SuppressWarnings("unused")
 					int getAndroidSdkInt() {
                         return AndroidUtils.SDK_INT;
                     }
                 }, "jrengage_mobile");
 
         mWebViewSettings.setBuiltInZoomControls(true);
         mWebViewSettings.setLoadsImagesAutomatically(true);
         mWebViewSettings.setJavaScriptEnabled(true);
         mWebViewSettings.setJavaScriptCanOpenWindowsAutomatically(false);
         mWebViewSettings.setSupportZoom(true);
 
         mWebView.setWebViewClient(mWebviewClient);
         mWebView.setWebChromeClient(mWebChromeClient);
         mWebView.setDownloadListener(mWebViewDownloadListener);
 
         if (savedInstanceState != null) {
             if (savedInstanceState.getBoolean(KEY_IS_SPINNER_ON)) {
                 showProgressSpinner();
             } else {
                 hideProgressSpinner();
             }
         }
 
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         if (mSession == null) return;
         mProvider = mSession.getCurrentlyAuthenticatingProvider();
         if (mProvider == null) return;
 
         if (savedInstanceState != null && savedInstanceState.containsKey(KEY_PROVIDER_NAME)) {
             mProvider = mSession.getProviderByName(savedInstanceState.getString(KEY_PROVIDER_NAME));
             mIsAlertShowing = savedInstanceState.getBoolean(KEY_IS_ALERT_SHOWING);
             mIsFinishPending = savedInstanceState.getBoolean(KEY_IS_FINISH_PENDING);
             mIsLoadingMobileEndpoint = savedInstanceState.getBoolean(KEY_IS_LOADING_MOBILE_ENDPOINT);
             configureWebViewUa();
             mWebView.restoreState(savedInstanceState);
         } else {
             mProvider = mSession.getCurrentlyAuthenticatingProvider();
             configureWebViewUa();
             URL startUrl = mSession.startUrlForCurrentlyAuthenticatingProvider();
             mWebView.loadUrl(startUrl.toString());
         }
 
         FragmentManager fm = getActivity().getSupportFragmentManager();
         mRetain = (RetainFragment) fm.findFragmentByTag(JR_RETAIN);
         if (mRetain == null) {
             mRetain = new RetainFragment();
             mRetain.setTargetFragment(this, 0);
            fm.beginTransaction().add(mRetain, JR_RETAIN);
         }
     }
     
     private void configureWebViewUa() {
         String customUa = mProvider.getWebViewOptions().getAsString(JRDictionary.KEY_USER_AGENT);
         //        if (mUseDesktopUa) mWebViewSettings.setUserAgentString(getString(R.string.jr_desktop_browser_ua));
         if (customUa != null) mWebViewSettings.setUserAgentString(customUa);
     }
 
     @Override
     public void onStop() {
         // onDestroyView may be called even if onCreateView never is, guard against NPEs
         if (mWebView != null) {
             mWebView.stopLoading();
 
             // This listener's callback assumes the activity is running, but if the user presses
             // the back button while the WebView is transitioning between pages the activity may
             // not be shown when this listener is fired, which would cause a crash, so we unset
             // the listener here.
             mWebView.setWebViewClient(null);
             mWebView.setDownloadListener(null);
         }
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
     }
 
     @Override
     protected void finishFragment() {
         getActivity().getSupportFragmentManager().beginTransaction().remove(mRetain).commit();
         super.finishFragment();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString(KEY_PROVIDER_NAME, mProvider.getName());
         outState.putBoolean(KEY_IS_ALERT_SHOWING, mIsAlertShowing);
         outState.putBoolean(KEY_IS_FINISH_PENDING, mIsFinishPending);
         outState.putBoolean(KEY_IS_LOADING_MOBILE_ENDPOINT, mIsLoadingMobileEndpoint);
         outState.putBoolean(KEY_IS_SPINNER_ON, mProgressSpinner.getVisibility() == View.VISIBLE);
 //        outState.putString(KEY_WEBVIEW_URL, );
         mWebView.saveState(outState);
 
         super.onSaveInstanceState(outState);
     }
 
     private void showAlertDialog(String title, String message) {
         Bundle options = new Bundle();
         options.putString(KEY_DIALOG_TITLE, title);
         options.putString(KEY_DIALOG_MESSAGE, message);
         showDialog(KEY_ALERT_DIALOG, options);
         mIsAlertShowing = true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle options) {
         if (id == KEY_ALERT_DIALOG) {
             return new AlertDialog.Builder(getActivity())
                     .setTitle(options.getString(KEY_DIALOG_TITLE))
                     .setMessage(options.getString(KEY_DIALOG_MESSAGE))
                     .setPositiveButton(getString(R.string.jr_dialog_ok), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             mIsAlertShowing = false;
                             if (mIsFinishPending) {
                                 mIsFinishPending = false;
                                 tryToFinishFragment();
                             }
                         }
                     }).create();
         }
         
         return super.onCreateDialog(id, options);
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog d, Bundle options) {
         if (id == KEY_ALERT_DIALOG) {
             d.setTitle(options.getString(KEY_DIALOG_TITLE));
             ((AlertDialog) d).setMessage(options.getString(KEY_DIALOG_MESSAGE));
             return;
         }
 
         super.onPrepareDialog(id, d, options);
     }
 
     @Override
     protected void tryToFinishFragment() {
         JREngage.logd(TAG, "[tryToFinishFragment]");
         if (mIsAlertShowing) {
             mIsFinishPending = true;
         } else {
             JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
             finishFragment();
         }
     }
 
     private boolean isMobileEndpointUrl(String url) {
         final String endpointUrl = mSession.getBaseUrl() + "/signin/device";
         return ((!TextUtils.isEmpty(url)) && (url.startsWith(endpointUrl)));
     }
 
     private void showProgressSpinner() {
         mProgressSpinner.setVisibility(View.VISIBLE);
     }
 
     private void hideProgressSpinner() {
         if (!mIsLoadingMobileEndpoint) mProgressSpinner.setVisibility(View.GONE);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
 
         menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.jr_menu_item_refresh));
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getTitle().toString().equals(getString(R.string.jr_menu_item_refresh))) {
             JREngage.logd(TAG, "refreshing WebView");
             mWebView.reload();
             return true;
         }
         
         return super.onOptionsItemSelected(item);
     }
 
     private void loadMobileEndpointUrl(String url) {
         mIsLoadingMobileEndpoint = true;
         showProgressSpinner();
 
         String urlToLoad = url + "&auth_info=true";
         JREngage.logd(TAG, "[loadMobileEndpointUrl] loading URL: " + urlToLoad);
 
 
         JRConnectionManager.createConnection(urlToLoad, mRetain.mConnectionDelegate, null);
     }
 
     private DownloadListener mWebViewDownloadListener = new DownloadListener() {
         /**
          * Used by pre 2.3 (2.2?) instead of shouldOverrideUrlLoading because of platform bugs.
          */
         public void onDownloadStart(String url,
                                     String userAgent,
                                     String contentDisposition,
                                     String mimetype,
                                     long contentLength) {
 
             JREngage.logd(TAG, "[onDownloadStart] URL: " + url + " | mimetype: " + mimetype
                     + " | length: " + contentLength);
 
             if (isMobileEndpointUrl(url)) loadMobileEndpointUrl(url);
         }
     };
 
     private WebViewClient mWebviewClient = new WebViewClient() {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             // Seems to be broken according to this:
             // http://code.google.com/p/android/issues/detail?id=2887
             // This method is getting called only once in the Engage flow, when there are at least two
             // redirects involved.
             // Another bug documents that this method isn't called on a form submission via POST
             // http://code.google.com/p/android/issues/detail?id=9122
             JREngage.logd(TAG, "[shouldOverrideUrlLoading]: " + view + ", " + url);
 
             if (isMobileEndpointUrl(url)) {
                 loadMobileEndpointUrl(url);
                 return true;
             }
 
             /* Intercept and sink mailto links because the webview auto-linkifies example email addresses
              * in the Google and Yahoo login pages and it's too easy to fat finger them :(
              */
             Uri uri = Uri.parse(url);
             if (uri.getScheme().equals("mailto")) {
                 return true;
             }
 
             return false;
         }
 
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             JREngage.logd(TAG, "[onPageStarted] url: " + url);
 
             /* Check for mobile endpoint URL. */
             if (isMobileEndpointUrl(url)) {
                 JREngage.logd(TAG, "[onPageStarted] looks like JR mobile endpoint URL");
                 loadMobileEndpointUrl(url);
                 mWebView.stopLoading();
                 mWebView.loadUrl("about:blank");
             }
 
             showProgressSpinner();
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             JREngage.logd(TAG, "[onPageFinished] URL: " + url);
 
             hideProgressSpinner();
 
             /* We inject some JS into the WebView. The JS is from the configuration pulled down from 
              * Engage. This way we can remotely fix up pages which render poorly/brokenly, like Yahoo!.
              */
             List<String> jsInjects =
                     mProvider.getWebViewOptions().getAsListOfStrings(JRDictionary.KEY_JS_INJECTIONS, true);
             for (String i : jsInjects) mWebView.loadUrl("javascript:" + i);
 
             boolean showZoomControl = 
                     mProvider.getWebViewOptions().getAsBoolean(JRDictionary.KEY_SHOW_ZOOM_CONTROL);
             if (showZoomControl) mWebView.invokeZoomPicker();
 
             super.onPageFinished(view, url);
         }
 
         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String url) {
             super.onReceivedError(view, errorCode, description, url);
             Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                     + " | URL: " + url);
 
             hideProgressSpinner();
 
             mIsFinishPending = true;
             setFragmentResult(RESULT_FAIL);
             showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                     getString(R.string.jr_webview_error_dialog_msg));
 
             JREngageError err = new JREngageError(
                     "Authentication failed: " + description,
                     JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                     JREngageError.ErrorType.AUTHENTICATION_FAILED);
             mSession.triggerAuthenticationDidFail(err);
         }
     };
 
     private WebChromeClient mWebChromeClient = new WebChromeClient() {
         @Override
         public void onProgressChanged(WebView view, int newProgress) {
             /* We hide the progress spinner if it's over half finished.
              * This is motivated by two things:
              *  - The page is usually loaded enough to interact with
              *  - Hyves keeps some page resource request open or something for several minutes
              *    (during which time the page is otherwise ~completely loaded.)
              */
             if (newProgress > 50) {
                 hideProgressSpinner();
             }
         }
     };
 
     @Override
     protected void onBackPressed() {
         JRConnectionManager.stopConnectionsForDelegate(mRetain.mConnectionDelegate);
         mSession.triggerAuthenticationDidRestart();
         finishFragmentWithResult(RESULT_RESTART);
     }
 
 //    public void setUseDesktopUa(boolean use) {
 //        mUseDesktopUa = use;
 //    }
 
     JRConnectionManagerDelegate mMobileEndPointConnectionDelegate =
             new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                 @Override
                 public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                        byte[] payload,
                                                        String requestUrl,
                                                        Object tag) {
                     String payloadString = new String(payload);
                     JREngage.logd(TAG, "[connectionDidFinishLoading] tag: " + tag + " | payload: " + payloadString);
 
                     hideProgressSpinner();
 
                     JRDictionary payloadDictionary;
                     String alertTitle, alertMessage, logMessage;
                     try {
                         payloadDictionary = JRDictionary.fromJSON(payloadString);
                     } catch (JSONException e) {
                         Log.e(TAG, "[connectionDidFinishLoading] failure: " + payloadString);
                         mIsFinishPending = true;
                         setFragmentResult(RESULT_FAIL);
                         showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                                 getString(R.string.jr_webview_error_dialog_msg));
                         return;
                     }
 
                     JRDictionary resultDictionary = payloadDictionary.getAsDictionary("rpx_result");
                     final String result = resultDictionary.getAsString("stat");
                     if ("ok".equals(result)) {
                         // TODO back button is no longer disabled because of the switch from modal dialog
                         // to progress spinner, fix the code path when the user hits the back button now.
 
                         if (!isSocialSharingFlow()) mSession.saveLastUsedBasicProvider();
                         mSession.triggerAuthenticationDidCompleteWithPayload(resultDictionary);
                         finishFragmentWithResult(Activity.RESULT_OK);
                     } else {
                         final String error = resultDictionary.getAsString("error");
 
                         if ("Discovery failed for the OpenID you entered".equals(error) ||
                                 "Your OpenID must be a URL".equals(error)) {
                             alertTitle = getString(R.string.jr_webview_bad_user_input_title);
                             if (mProvider.requiresInput()) {
                                 String s = mProvider.getUserInputDescriptor();
                                 alertMessage = getString(R.string.jr_webview_bad_user_input_message, s);
                             } else {
                                 alertMessage = getString(R.string.jr_webview_generic_auth_error_message);
                                 Log.e(TAG, "[connectionDidFinishLoading]: unrecognized openid error");
                             }
 
                             mIsFinishPending = true;
                             setFragmentResult(RESULT_BAD_OPENID_URL);
                             showAlertDialog(alertTitle, alertMessage);
                             //} else if ("The URL you entered does not appear to be an OpenID".equals(error)) {
                             // The error text changed :/
                         } else if (error.matches(".*you entered does not appear to be an OpenID")) {
                             alertTitle = getString(R.string.jr_webview_bad_user_input_title);
                             if (mProvider.requiresInput()) {
                                 alertMessage = getString(R.string.jr_webview_bad_user_input_message,
                                         mProvider.getUserInputDescriptor());
                             } else {
                                 alertMessage = getString(R.string.jr_webview_generic_auth_error_message);
                             }
 
                             logMessage = "The URL you entered does not appear to be an OpenID: ";
                             Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);
 
                             mIsFinishPending = true;
                             setFragmentResult(RESULT_BAD_OPENID_URL);
                             showAlertDialog(alertTitle, alertMessage);
                         } else if ("Please enter your OpenID".equals(error)) {
                             // Caused by entering a ~blank OpenID URL
 
                             mIsFinishPending = true;
                             setFragmentResult(RESULT_BAD_OPENID_URL);
                             // TODO resource-ify
                             showAlertDialog("OpenID Error", "The URL you entered does not appear to be an OpenID");
                         } else if ("canceled".equals(error)) {
                             mProvider.setForceReauth(true);
                             mSession.triggerAuthenticationDidRestart();
                             finishFragmentWithResult(RESULT_RESTART);
                         } else {
                             Log.e(TAG, "unrecognized error: " + error);
                             JREngageError err = new JREngageError(
                                     "Authentication failed: " + payloadString,
                                     JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                                     JREngageError.ErrorType.AUTHENTICATION_FAILED);
 
                             mSession.triggerAuthenticationDidFail(err);
                             setFragmentResult(RESULT_FAIL);
                             mIsFinishPending = true;
                             showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                                     getString(R.string.jr_webview_error_dialog_msg));
                         }
                     }
                 }
 
                 @Override
                 public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                     JREngage.logd(TAG, "[connectionDidFail] userdata: " + tag, ex);
 
                     if (hasView()) {
                         // This is designed to not run if the user pressed the back button after the MEU started
                         // loading but before it failed.
                         // The test is probably not quite right and that if the timing is bad both onBackPressed()
                         // and this method will call setResult
                         final JREngageError error = new JREngageError(
                                 "Authentication failed",
                                 JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                                 JREngageError.ErrorType.AUTHENTICATION_FAILED,
                                 ex);
                         mSession.triggerAuthenticationDidFail(error);
                         mIsFinishPending = true;
                         setFragmentResult(RESULT_FAIL);
                         showAlertDialog(getString(R.string.jr_webview_error_dialog_title),
                                 getString(R.string.jr_dialog_network_error));
                     }
                 }
             };
 
     /**
      * This class serves to respond to the MEU connection and delegates the result to the real fragment.
      * This is necessary because the real fragment can be destroyed and recreated if it's  in an Activity
      * which is destroyed and recreated because it cannot setRetainInstance(true) because it may be added to
      * the back stack.
       */
     private static class RetainFragment extends Fragment {
         private static final String TAG  = RetainFragment.class.getSimpleName();
         JRWebViewFragment mTarget;
 
         /* The deferred connectionDidFinishLoading message */
         HttpResponseHeaders mDeferredCdflH;
         byte[] mDeferredCdflBa;
         String mDeferredCdflS;
         Object mDeferredCdflO;
 
         /* The deferred connectionDidFail message */
         Exception mDeferredCdfE;
         String mDeferredCdfS;
         Object mDeferredCdfO;
         
         
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             setRetainInstance(true);
         }
 
         @Override
         public void onResume() {
             super.onResume();
             mTarget = (JRWebViewFragment) getTargetFragment();
             boolean a = isResumed();
             maybeDispatchMessages();
         }
 
         @Override
         public void onStop() {
             super.onStop();
             mTarget = null;
         }
 
         JRConnectionManagerDelegate mConnectionDelegate = new JRConnectionManagerDelegate() {
             public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                    byte[] payload,
                                                    String requestUrl,
                                                    Object tag) {
                 JREngage.logd(TAG, "[connectionDidFinishLoading]");
                 mDeferredCdflH = headers;
                 mDeferredCdflBa = payload;
                 mDeferredCdflS = requestUrl;
                 mDeferredCdflO = tag;
 
                 maybeDispatchMessages();
             }
 
             public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                 JREngage.logd(TAG, "[connectionDidFail]");
                 mDeferredCdfE = ex;
                 mDeferredCdfS = requestUrl;
                 mDeferredCdfO = tag;
 
                 maybeDispatchMessages();
             }
         };
 
         private void maybeDispatchMessages() {
             if (mTarget != null && isResumed()) {
                 if (mDeferredCdflH != null) {
                     mTarget.mMobileEndPointConnectionDelegate.connectionDidFinishLoading(
                             mDeferredCdflH, mDeferredCdflBa, mDeferredCdflS, mDeferredCdflO);
                     mDeferredCdflH = null;
                     mDeferredCdflBa = null;
                     mDeferredCdflS = null;
                     mDeferredCdflO = null;
                 }
 
                 if (mDeferredCdfE != null) {
                     mTarget.mMobileEndPointConnectionDelegate.connectionDidFail(
                             mDeferredCdfE, mDeferredCdfS, mDeferredCdfO);
                     mDeferredCdfE = null;
                     mDeferredCdfS = null;
                     mDeferredCdfO = null;
                 }
             }
         }
     }
 }
