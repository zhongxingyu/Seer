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
 package com.janrain.android.engage.session;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.text.TextUtils;
 import android.util.Log;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.JREngageError.ConfigurationError;
 import com.janrain.android.engage.JREngageError.ErrorType;
 import com.janrain.android.engage.JREngageError.SocialPublishingError;
 import com.janrain.android.engage.JREnvironment;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.AndroidUtils;
 import com.janrain.android.engage.utils.Archiver;
 import com.janrain.android.engage.utils.ListUtils;
 import com.janrain.android.engage.utils.Prefs;
 import com.janrain.android.engage.utils.StringUtils;
 
 public class JRSession implements JRConnectionManagerDelegate {
     private static final String TAG = JRSession.class.getSimpleName();
 
     private static final JREnvironment ENVIRONMENT = JREnvironment.PRODUCTION;
     //private static final JREnvironment ENVIRONMENT = JREnvironment.STAGING;
     //private static final JREnvironment ENVIRONMENT = JREnvironment.LILLI;
     //private static final JREnvironment ENVIRONMENT = JREnvironment.NATHAN;
 
     private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
     private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
     private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
     private static final String ARCHIVE_AUTH_USERS_BY_PROVIDER = "jrAuthenticatedUsersByProvider";
 
     private static final String UNFORMATTED_CONFIG_URL =
             "%s/openid/mobile_config_and_baseurl?appId=%s&device=android&app_name=%s&version=%s";
     private static final String TAG_GET_CONFIGURATION = "getConfiguration";
 
     private static JRSession sInstance;
     public static final String USERDATA_ACTION_KEY = "action";
     public static final String USERDATA_ACTION_CALL_TOKEN_URL = "callTokenUrl";
     public static final String USERDATA_TOKEN_URL_KEY = "tokenUrl";
     public static final String USERDATA_PROVIDER_NAME_KEY = "providerName";
     public static final String USERDATA_ACTIVITY_KEY = "activity";
     public static final String USERDATA_ACTION_SHARE_ACTIVITY = "shareActivity";
 
     private ArrayList<JRSessionDelegate> mDelegates;
 
 	private JRProvider mCurrentlyAuthenticatingProvider;
     private JRProvider mCurrentlyPublishingProvider;
 
     private String mReturningBasicProvider;
 	private String mReturningSocialProvider;
 
 	private Map<String, JRProvider> mAllProviders;
 	private List<String> mBasicProviders;
     private List<String> mEnabledAuthenticationProviders = null;
     private List<String> mEnabledSharingProviders = null;
 	private List<String> mSocialProviders;
 	private Map<String, JRAuthenticatedUser> mAuthenticatedUsersByProvider;
 
 	private JRActivityObject mActivity;
 	private String mTokenUrl;
 	private String mAppId;
     private String mBaseUrl;
     private String mUrlEncodedAppName;
 
     private boolean mGetConfigDone = false;
     private String mOldEtag;
     private String mSavedConfigurationBlock = "";
     private String mSavedEtag;
     private String mNewEtag;
     private String mUrlEncodedLibraryVersion;
 
     private boolean mHidePoweredBy;
 	private boolean mAlwaysForceReauth;
     private boolean mSkipLandingPage;
     private int mUiShowingCount = 0;
 
     private JREngageError mError;
 
     public static JRSession getInstance() {
         return sInstance;
 	}
 
 	public static JRSession getInstance(String appId,
                                             String tokenUrl,
                                             JRSessionDelegate delegate) {
         if (sInstance != null) {
             if (sInstance.isUiShowing()) {
                 Log.e(TAG, "Cannot reinitialize JREngage while its UI is showing");
             } else {
                 JREngage.logd(TAG, "[getInstance] reinitializing, registered delegates will be unregistered");
                 sInstance.initialize(appId, tokenUrl, delegate);
             }
         } else {
             JREngage.logd(TAG, "[getInstance] returning new instance.");
             sInstance = new JRSession(appId, tokenUrl, delegate);
         }
 
 		return sInstance;
 	}
 
 	private JRSession(String appId, String tokenUrl, JRSessionDelegate delegate) {
         initialize(appId, tokenUrl, delegate);
     }
 
     /* We runtime type check the deserialized generics so we can safely ignore these unchecked
      * assignment warnings. */
     @SuppressWarnings("unchecked")
     private void initialize(String appId, String tokenUrl, JRSessionDelegate delegate) {
         JREngage.logd(TAG, "[initialize] initializing instance.");
 
 		mDelegates = new ArrayList<JRSessionDelegate>();
 		mDelegates.add(delegate);
 
 		mAppId = appId;
 		mTokenUrl = tokenUrl;
 
         ApplicationInfo ai = AndroidUtils.getApplicationInfo();
         String appName = getContext().getPackageManager().getApplicationLabel(ai).toString();
         appName += ":" + getContext().getPackageName();
         mUrlEncodedAppName = AndroidUtils.urlEncode(appName);
         mUrlEncodedLibraryVersion = AndroidUtils.urlEncode(getContext().getString(R.string.jr_git_describe));
 
         try {
             /* load the last used basic and social providers */
             mReturningSocialProvider = Prefs.getString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, "");
             mReturningBasicProvider = Prefs.getString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");
 
             /* Load the library state from disk */
             mAuthenticatedUsersByProvider = Archiver.load(ARCHIVE_AUTH_USERS_BY_PROVIDER);
             mAllProviders = Archiver.load(ARCHIVE_ALL_PROVIDERS);
 
             /* Fix up the provider objects with data that isn't serialized along with them */
             for (Object provider : mAllProviders.values()) ((JRProvider)provider).loadDynamicVariables();
 
             /* Load the list of basic providers */
             mBasicProviders = (ArrayList<String>)Archiver.load(ARCHIVE_BASIC_PROVIDERS);
             for (Object v : mBasicProviders) assert v instanceof String;
             JREngage.logd(TAG, "basic providers: [" + TextUtils.join(",", mBasicProviders) + "]");
 
             /* Load the list of social providers */
             mSocialProviders = (ArrayList<String>)Archiver.load(ARCHIVE_SOCIAL_PROVIDERS);
             for (Object v : mSocialProviders) assert v instanceof String;
             JREngage.logd(TAG, "social providers: [" + TextUtils.join(",", mSocialProviders) + "]");
 
             /* Load the base url */
             mBaseUrl = Prefs.getString(Prefs.KEY_JR_BASE_URL, "");
 
             /* Figure out of we're suppose to hide the powered by line */
             mHidePoweredBy = Prefs.getBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, false);
 
             /* If the configuration for this rp has changed, the etag will have changed, and we need
              * to update our current configuration information. */
             mOldEtag = Prefs.getString(Prefs.KEY_JR_CONFIGURATION_ETAG, "");
         } catch (Archiver.LoadException e) {
             /* Blank slate */
             mAuthenticatedUsersByProvider = new HashMap<String, JRAuthenticatedUser>();
             mAllProviders = new HashMap<String, JRProvider>();
             mBasicProviders = new ArrayList<String>();
             mSocialProviders = new ArrayList<String>();
             mBaseUrl = "";
             mHidePoweredBy = true;
             mReturningSocialProvider = "";
             mReturningBasicProvider = "";
             mOldEtag = "";
         }
 
         mError = startGetConfiguration();
 	}
 
     public JREngageError getError() {
         return mError;
     }
 
     public JRActivityObject getJRActivity() {
         return mActivity;
     }
 
     public void setJRActivity(JRActivityObject activity) {
         mActivity = activity;
     }
 
     public void setSkipLandingPage(boolean skipLandingPage) {
         mSkipLandingPage = skipLandingPage;
     }
 
     public boolean getAlwaysForceReauth() {
         return mAlwaysForceReauth;
     }
 
     public void setAlwaysForceReauth(boolean force) {
         mAlwaysForceReauth = force;
     }
 
     public void setTokenUrl(String tokenUrl) {
         mTokenUrl = tokenUrl;
     }
 
     public JRProvider getCurrentlyAuthenticatingProvider() {
         return mCurrentlyAuthenticatingProvider;
     }
 
     public void setCurrentlyAuthenticatingProvider(JRProvider provider) {
         JREngage.logd(TAG, "[setCurrentlyAuthenticatingProvider] to " +
                     (provider != null ? provider.getName() : null));
 
         mCurrentlyAuthenticatingProvider = provider;
     }
 
     public ArrayList<JRProvider> getBasicProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
 
         if ((mBasicProviders != null) && (mBasicProviders.size() > 0)) {
             for (String name : mBasicProviders) {
                 // Filter by enabled provider list if available
                 if (mEnabledAuthenticationProviders != null &&
                         !mEnabledAuthenticationProviders.contains(name)) continue;
                 providerList.add(mAllProviders.get(name));
             }
         }
 
         return providerList;
     }
 
     /**
      * Gets the configured and enabled social providers
      *
      * @return an ArrayList&lt;Provider>, does not return null.
      */
     public ArrayList<JRProvider> getSocialProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
 
         if ((mSocialProviders != null) && (mSocialProviders.size() > 0)) {
             for (String name : mSocialProviders) {
                 // Filter by enabled provider list if available
                 if (mEnabledSharingProviders != null &&
                         !mEnabledSharingProviders.contains(name)) continue;
                 providerList.add(mAllProviders.get(name));
             }
         }
 
         return providerList;
     }
 
     public String getReturningBasicProvider() {
         /* This is here so that when a calling application sets mSkipLandingPage, the dialog always opens
          * to the providers list. (See JRProviderListFragment.onCreate for an explanation of the flow control
          * when there's a "returning provider.") */
         if (mSkipLandingPage)
             return null;
 
         return mReturningBasicProvider;
     }
 
     public void setReturningBasicProvider(String returningBasicProvider) {
         if (TextUtils.isEmpty(returningBasicProvider)) returningBasicProvider = ""; // nulls -> ""s
         if (!getBasicProviders().contains(getProviderByName(returningBasicProvider))) {
             returningBasicProvider = "";
         }
 
         mReturningBasicProvider = returningBasicProvider;
         Prefs.putString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, returningBasicProvider);
     }
 
     public String getReturningSocialProvider() {
         return mReturningSocialProvider;
     }
 
     public void setReturningSocialProvider(String returningSocialProvider) {
         if (TextUtils.isEmpty(returningSocialProvider)) returningSocialProvider = ""; // nulls -> ""s
         if (!getSocialProviders().contains(getProviderByName(returningSocialProvider))) {
             returningSocialProvider = "";
         }
 
         mReturningSocialProvider = returningSocialProvider;
         Prefs.putString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, returningSocialProvider);
     }
 
     public String getBaseUrl() {
         return mBaseUrl;
     }
 
     public boolean getHidePoweredBy() {
         return mHidePoweredBy;
     }
 
     public void setUiIsShowing(boolean uiIsShowing) {
         if (uiIsShowing) {
             mUiShowingCount++;
         } else {
             mUiShowingCount--;
         }
 
         if (mUiShowingCount == 0 && !mSavedConfigurationBlock.equals("")) {
             String s = mSavedConfigurationBlock;
             mSavedConfigurationBlock = "";
             mNewEtag = mSavedEtag;
             mError = finishGetConfiguration(s);
         }
     }
 
     public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
         Log.e(TAG, "[connectionDidFail]");
 
         if (tag == null) {
             Log.e(TAG, "[connectionDidFail] unexpected null userdata");
         } else if (tag instanceof String) {
             if (tag.equals(TAG_GET_CONFIGURATION)) {
                 Log.e(TAG, "[connectionDidFail] for getConfiguration");
                 mError = new JREngageError(
                         getContext().getString(R.string.jr_getconfig_network_failure_message),
                         ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_FAILED,
                         ex);
                 mGetConfigDone = true;
                 triggerMobileConfigDidFinish();
             } else {
                 Log.e(TAG, "[connectionDidFail] unrecognized ConnectionManager tag: "
                         + tag + " with Exception: " + ex);
             }
         } else if (tag instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) tag;
             if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_CALL_TOKEN_URL)) {
                 Log.e(TAG, "[connectionDidFail] call to token url failed: " + ex);
                 JREngageError error = new JREngageError(
                         "Error: " + ex.getLocalizedMessage(),
                        JREngageError.AuthenticationError.AUTHENTICATION_TOKEN_URL_FAILED,
                        "Failed to reach authentication token URL",
                         ex);
                 for (JRSessionDelegate delegate : getDelegatesCopy()) {
                     delegate.authenticationCallToTokenUrlDidFail(
                             dictionary.getAsString(USERDATA_TOKEN_URL_KEY),
                             error,
                             dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                 }
             } else if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_SHARE_ACTIVITY)) {
                 Log.e(TAG, "[connectionDidFail] sharing activity failed: " + ex);
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 JREngageError error = new JREngageError(
                         "Error: " + ex.getLocalizedMessage(),
                         JREngageError.CODE_UNKNOWN,
                         "",
                         ex);
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     delegate.publishingJRActivityDidFail(
                             (JRActivityObject) dictionary.get(USERDATA_ACTIVITY_KEY),
                             error,
                             dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                 }
             }
         }
 	}
 
     private void processShareActivityResponse(String payload, JRDictionary userDataTag) {
         String providerName = userDataTag.getAsString(USERDATA_PROVIDER_NAME_KEY);
 
         JRDictionary responseDict;
         try {
             responseDict = JRDictionary.fromJSON(payload);
         } catch (JSONException e) {
             responseDict = null;
         }
 
         if (responseDict == null) {
             setCurrentlyPublishingProvider(null);
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.publishingJRActivityDidFail(
                         (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY),
                         new JREngageError(payload,
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED),
                                 providerName);
             }
         } else if (responseDict.containsKey("stat") && ("ok".equals(responseDict.get("stat")))) {
             setReturningSocialProvider(getCurrentlyPublishingProvider().getName());
             setCurrentlyPublishingProvider(null);
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.publishingJRActivityDidSucceed(mActivity, providerName);
             }
         } else {
             setCurrentlyPublishingProvider(null);
             JRDictionary errorDict = responseDict.getAsDictionary("err");
             JREngageError publishError;
 
             if (errorDict == null) {
                 publishError = new JREngageError(
                         getContext().getString(R.string.jr_problem_sharing),
                         SocialPublishingError.FAILED,
                         ErrorType.PUBLISH_FAILED);
             } else {
                 int code = (errorDict.containsKey("code")) ? errorDict.getAsInt("code") : 1000;
 
                 String errorMessage = errorDict.getAsString("msg", "");
 
                 switch (code) {
                     case 0: /* "Missing parameter: apiKey" */
                         publishError = new JREngageError(
                                 errorMessage,
                                 SocialPublishingError.MISSING_API_KEY,
                                 ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         break;
                     case 4: /* "Facebook Error: Invalid OAuth 2.0 Access Token" */
                         if (errorMessage.matches(".*nvalid ..uth.*") ||
                                 errorMessage.matches(".*session.*invalidated.*") ||
                                 errorMessage.matches(".*rror validating access token.*")) {
                             publishError = new JREngageError(
                                     errorMessage,
                                     SocialPublishingError.INVALID_OAUTH_TOKEN,
                                     ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         } else if (errorMessage.matches(".*eed action request limit.*")) {
                             publishError = new JREngageError(
                                     errorMessage,
                                     SocialPublishingError.FEED_ACTION_REQUEST_LIMIT,
                                     ErrorType.PUBLISH_FAILED);
                         } else {
                             publishError = new JREngageError(
                                     errorMessage,
                                     SocialPublishingError.FAILED,
                                     ErrorType.PUBLISH_FAILED);
                         }
                         break;
                     case 100: // TODO LinkedIn character limit error
                         publishError = new JREngageError(
                                 errorMessage,
                                 SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY);
                         break;
                     case 6:
                         if (errorMessage.matches(".witter.*uplicate.*")) {
                             publishError = new JREngageError(
                                     errorMessage,
                                     SocialPublishingError.DUPLICATE_TWITTER,
                                     ErrorType.PUBLISH_INVALID_ACTIVITY);
                         } else {
                             publishError = new JREngageError(
                                     errorMessage,
                                     SocialPublishingError.FAILED,
                                     ErrorType.PUBLISH_INVALID_ACTIVITY);
                         }
                         break;
                     case 1000: /* Extracting code failed; Fall through. */
                     default: // TODO Other errors (find them)
                         publishError = new JREngageError(
                                 getContext().getString(R.string.jr_problem_sharing),
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED);
                         break;
                 }
             }
 
             triggerPublishingJRActivityDidFail(publishError,
                     (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY),
                     providerName);
         }
     }
 
     public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                            String requestUrl, Object tag) {
         String payloadString = new String(payload);
         JREngage.logd(TAG, "[connectionDidFinishLoading] payload: " + payloadString);
 
         if (tag instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) tag;
             if (dictionary.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_SHARE_ACTIVITY)) {
                 processShareActivityResponse(payloadString, dictionary);
             } else if (dictionary.containsKey(USERDATA_TOKEN_URL_KEY)) {
                 for (JRSessionDelegate delegate : getDelegatesCopy()) {
                     delegate.authenticationDidReachTokenUrl(
                             dictionary.getAsString(USERDATA_TOKEN_URL_KEY),
                             headers,
                             payloadString,
                             dictionary.getAsString(USERDATA_PROVIDER_NAME_KEY));
                 }
             } else {
                 Log.e(TAG, "unexpected userdata found in ConnectionDidFinishLoading full");
             }
         } else if (tag instanceof String) {
             String s = (String) tag;
 
             if (s.equals("getConfiguration")) {
                 if (headers.getResponseCode() == HttpStatus.SC_NOT_MODIFIED) {
                     /* If the ETag matched, we're done. */
                     JREngage.logd(TAG, "[connectionDidFinishLoading] found HTTP_NOT_MODIFIED -> matched ETag");
 
                     mGetConfigDone = true;
                     return;
                 }
 
                 if (payloadString.contains("\"provider_info\":{")) {
                     mError = finishGetConfiguration(payloadString, headers.getETag());
                 } else {
                     Log.e(TAG, "failed to parse response for getConfiguration");
                     mError = new JREngageError(
                             getContext().getString(R.string.jr_getconfig_parse_error_message),
                             ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                             ErrorType.CONFIGURATION_FAILED);
                 }
             } else {
                 Log.e(TAG, "unexpected userData found in ConnectionDidFinishLoading full");
             }
         }
 	}
 
     public void addDelegate(JRSessionDelegate delegate) {
         JREngage.logd(TAG, "[addDelegate]");
 
         mDelegates.add(delegate);
     }
 
     public void removeDelegate(JRSessionDelegate delegate) {
         JREngage.logd(TAG, "[removeDelegate]");
 
         mDelegates.remove(delegate);
     }
 
     public void tryToReconfigureLibrary() {
         JREngage.logd(TAG, "[tryToReconfigureLibrary]");
 
         mGetConfigDone = false;
         mError = null;
         mError = startGetConfiguration();
     }
 
     private JREngageError startGetConfiguration() {
         String urlString = String.format(UNFORMATTED_CONFIG_URL,
                 ENVIRONMENT.getServerUrl(),
                 mAppId,
                 mUrlEncodedAppName,
                 mUrlEncodedLibraryVersion);
         JREngage.logd(TAG, "[startGetConfiguration] url: " + urlString);
 
         BasicNameValuePair eTagHeader = new BasicNameValuePair("If-None-Match", mOldEtag);
         List<NameValuePair> headerList = new ArrayList<NameValuePair>();
         headerList.add(eTagHeader);
 
         JRConnectionManager.createConnection(urlString, this, TAG_GET_CONFIGURATION, headerList);
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr) {
         JREngage.logd(TAG, "[finishGetConfiguration]");
 
         /* Attempt to parse the return string as JSON.*/
         JRDictionary jsonDict = null;
         Exception jsonEx = null;
         try {
             jsonDict = JRDictionary.fromJSON(dataStr);
         } catch (JSONException e) {
             Log.e(TAG, "[finishGetConfiguration] json error: ", e);
             jsonEx = e;
         }
 
         /* If the parsed dictionary object is null or an exception has occurred, return */
         if (jsonDict == null) {
             Log.e(TAG, "[finishGetConfiguration] failed.");
             return new JREngageError(
                     getContext().getString(R.string.jr_getconfig_parse_error_message),
                     ConfigurationError.JSON_ERROR,
                     ErrorType.CONFIGURATION_FAILED,
                     jsonEx);
         }
 
         /* Check to see if the base URL has changed */
         String baseUrl = jsonDict.getAsString("baseurl", "");
         if (!baseUrl.equals(mBaseUrl)) {
             /* Save the new base URL */
             mBaseUrl = StringUtils.chomp(baseUrl, "/");
             Prefs.putString(Prefs.KEY_JR_BASE_URL, mBaseUrl);
         }
 
         /* Get the providers out of the provider_info section.  These are likely to have changed. */
         JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
         mAllProviders = new HashMap<String, JRProvider>();
 
         /* For each provider */
         for (String name : providerInfo.keySet()) {
             /* Get its dictionary */
             JRDictionary dictionary = providerInfo.getAsDictionary(name);
             /* Use this to create a provider object */
             JRProvider provider = new JRProvider(name, dictionary);
             /* and finally add the object to our dictionary of providers */
             mAllProviders.put(name, provider);
         }
 
         /* Get the ordered list of basic providers */
         mBasicProviders = jsonDict.getAsListOfStrings("enabled_providers");
         /* Get the ordered list of social providers */
         mSocialProviders = jsonDict.getAsListOfStrings("social_providers");
 
         /* By redundantly calling these setters it is ensured that the returning basic and social providers,
          * if set, are members of the configured set of providers. */
         setReturningBasicProvider(mReturningBasicProvider);
         setReturningSocialProvider(mReturningSocialProvider);
 
         /* Done! */
 
         /* Save data to local store */
         Archiver.save(ARCHIVE_ALL_PROVIDERS, mAllProviders);
         Archiver.save(ARCHIVE_BASIC_PROVIDERS, mBasicProviders);
         Archiver.save(ARCHIVE_SOCIAL_PROVIDERS, mSocialProviders);
 
         /* Figure out of whether to hide the "powered by" line */
         mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
         Prefs.putBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, mHidePoweredBy);
 
         /* Once we know everything is parsed and saved, save the new etag */
         Prefs.putString(Prefs.KEY_JR_CONFIGURATION_ETAG, mNewEtag);
 
         /* 'git-tag'-like library version tag to prevent reloading stale data from disk */
         Prefs.putString(Prefs.KEY_JR_ENGAGE_LIBRARY_VERSION, mUrlEncodedLibraryVersion);
 
         mGetConfigDone = true;
         triggerMobileConfigDidFinish();
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr, String eTag) {
         JREngage.logd(TAG, "[finishGetConfiguration-etag]");
 
         if (!mOldEtag.equals(eTag)) {
 
             /* We can only update all of our data if the UI isn't currently using that
              * information.  Otherwise, the library may crash/behave inconsistently.  If a
              * dialog isn't showing, go ahead and update that information.  Or, in the case
              * where a dialog is showing but there isn't any data that it could be using (that
              * is, the lists of basic and social providers are nil), go ahead and update it too.
              * The dialogs won't try and do anything until we're done updating the lists. */
             if (!isUiShowing() ||
                     (ListUtils.isEmpty(mBasicProviders) && ListUtils.isEmpty(mSocialProviders))) {
                 mNewEtag = eTag;
                 return finishGetConfiguration(dataStr);
             }
 
             /* Otherwise, we have to save all this information for later.  The
              * UserInterfaceMaestro sends a signal to sessionData when the dialog closes (by
              * setting the boolean dialogIsShowing to false. In the setter function, sessionData
              * checks to see if there's anything stored in the savedConfigurationBlock, and
              * updates it then. */
             mSavedConfigurationBlock = dataStr;
             mSavedEtag = eTag;
         }
 
         mGetConfigDone = true;
         triggerMobileConfigDidFinish();
 
         return null;
     }
 
     private boolean isUiShowing() {
         return mUiShowingCount != 0;
     }
 
     private String getWelcomeMessageFromCookieString() {
         JREngage.logd(TAG, "[getWelcomeMessageFromCookieString]");
 
         String cookies = CookieManager.getInstance().getCookie(getBaseUrl());
         String cookieString = cookies.replaceAll(".*welcome_info=([^;]*).*", "$1");
 
         if (!TextUtils.isEmpty(cookieString)) {
             String[] parts = cookieString.split("%22");
             if (parts.length > 5) return "Sign in as " + AndroidUtils.urlDecode(parts[5]);
         }
 
         return null;
     }
 
     public void saveLastUsedBasicProvider() {
         JREngage.logd(TAG, "[saveLastUsedBasicProvider]");
 
         setReturningBasicProvider(mCurrentlyAuthenticatingProvider.getName());
     }
 
     public URL startUrlForCurrentlyAuthenticatingProvider() {
         JREngage.logd(TAG, "[startUrlForCurrentlyAuthenticatingProvider]");
 
         // What's this for?
         if (mCurrentlyAuthenticatingProvider == null) return null;
 
         String oid; /* open identifier */
 
         if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpenIdentifier())) {
             oid = String.format("openid_identifier=%s&", mCurrentlyAuthenticatingProvider.getOpenIdentifier());
             if (mCurrentlyAuthenticatingProvider.requiresInput()) {
                 oid = oid.replaceAll("%@", mCurrentlyAuthenticatingProvider.getUserInput());
             } else {
                 oid = oid.replaceAll("%@", "");
             }
         } else {
             oid = "";
         }
 
         String fullStartUrl;
 
         boolean forceReauth = mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth();
         if (getAuthenticatedUserForProvider(mCurrentlyAuthenticatingProvider) == null) forceReauth = true;
         if (forceReauth) {
             deleteWebViewCookiesForDomains(mCurrentlyAuthenticatingProvider.getCookieDomains());
         }
 
         fullStartUrl = String.format("%s%s?%s%sdevice=android&extended=true",
                 mBaseUrl,
                 mCurrentlyAuthenticatingProvider.getStartAuthenticationUrl(),
                 oid,
                 (forceReauth ? "force_reauth=true&" : "")
         );
 
         JREngage.logd(TAG, "[startUrlForCurrentlyAuthenticatingProvider] startUrl: " + fullStartUrl);
 
         URL url = null;
         try {
             url = new URL(fullStartUrl);
         } catch (MalformedURLException e) {
             Log.e(TAG, "[startUrlForCurrentlyAuthenticatingProvider] URL create failed for string: "
                     + fullStartUrl);
         }
         return url;
     }
 
     public JRAuthenticatedUser getAuthenticatedUserForProvider(JRProvider provider) {
         JREngage.logd(TAG, "[getAuthenticatedUserForProvider]");
 
         return mAuthenticatedUsersByProvider.get(provider.getName());
     }
 
     public void forgetAuthenticatedUserForProvider(String providerName) {
         JREngage.logd(TAG, "[forgetAuthenticatedUserForProvider]");
 
         JRProvider provider = mAllProviders.get(providerName);
         if (provider == null) {
             Log.e(TAG, "[forgetAuthenticatedUserForProvider] provider not found: " + providerName);
         } else {
             provider.setForceReauth(true);
         }
 
         if (mAuthenticatedUsersByProvider.containsKey(providerName)) {
             mAuthenticatedUsersByProvider.get(providerName).deleteCachedProfilePic();
             mAuthenticatedUsersByProvider.remove(providerName);
             Archiver.save(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
             triggerUserWasSignedOut(providerName);
         }
     }
 
     public void forgetAllAuthenticatedUsers() {
         JREngage.logd(TAG, "[forgetAllAuthenticatedUsers]");
 
         for (String p : mAllProviders.keySet()) forgetAuthenticatedUserForProvider(p);
     }
 
     private void deleteWebViewCookiesForDomains(Collection<String> domains) {
         for (String d : domains) {
             deleteWebViewCookiesForDomain(d, false);
             deleteWebViewCookiesForDomain(d, true);
         }
     }
 
     private void deleteWebViewCookiesForDomain(String domain, boolean secure) {
         CookieSyncManager csm = CookieSyncManager.createInstance(getContext());
         CookieManager cm = CookieManager.getInstance();
 
         /* http://code.google.com/p/android/issues/detail?id=19294 */
         if (AndroidUtils.SDK_INT >= 11) {
             // don't trim leading .s
         } else {
             /* Trim leading .s */
             if (domain.startsWith(".")) domain = domain.substring(1);
         }
 
         /* Cookies are stored by domain, and are not different for different schemes (i.e. http vs
          * https) (although they do have an optional 'secure' flag.) */
         domain = "http" + (secure ? "s" : "") + "://" + domain;
         String cookieGlob = cm.getCookie(domain);
         if (cookieGlob != null) {
             String[] cookies = cookieGlob.split(";");
             for (String cookieTuple : cookies) {
                 String[] cookieParts = cookieTuple.split("=");
 
                 /* setCookie has changed a lot between different versions of Android with respect to
                  * how it handles cookies like these, which are set in order to clear an existing
                  * cookie.  This way of invoking it seems to work on all versions. */
                 cm.setCookie(domain, cookieParts[0] + "=;");
 
                 /* These calls have worked for some subset of the the set of all versions of
                  * Android:
                  * cm.setCookie(domain, cookieParts[0] + "=");
                  * cm.setCookie(domain, cookieParts[0]); */
             }
             csm.sync();
         }
     }
 
     public JRProvider getProviderByName(String name) {
         return mAllProviders.get(name);
     }
 
     public void notifyEmailSmsShare(String method) {
         StringBuilder body = new StringBuilder();
         body.append("method=").append(method);
         body.append("&device=").append("android");
         body.append("&appId=").append(mAppId);
 
         String url = ENVIRONMENT.getServerUrl() + "/social/record_activity";
 
         JREngage.logd(TAG, "[notifyEmailSmsShare]: " + url + " data: " + body.toString());
 
         JRConnectionManagerDelegate jrcmd = new SimpleJRConnectionManagerDelegate() {
             @Override
             public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                    byte[] payload,
                                                    String requestUrl,
                                                    Object tag) {
                 JREngage.logd(TAG, "[notifyEmailSmsShare]: success");
             }
 
             @Override
             public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                 Log.e(TAG, "[notifyEmailSmsShare]: failure", ex);
             }
         };
 
         JRConnectionManager.createConnection(url, jrcmd, null, null, body.toString().getBytes());
     }
 
     public void shareActivityForUser(JRAuthenticatedUser user) {
         JREngage.logd(TAG, "[shareActivityForUser]");
 
         setCurrentlyPublishingProvider(user.getProviderName());
 
         /* Truncate the resource description if necessary */
         int descMaxChars = getCurrentlyPublishingProvider().getSocialSharingProperties()
                 .getAsInt(JRDictionary.KEY_DESC_MAX_CHARS, -1);
         if (descMaxChars > 0 && mActivity.getDescription().length() > descMaxChars) {
             mActivity.setDescription(mActivity.getDescription().substring(0, 255));
         }
 
         String deviceToken = user.getDeviceToken();
 
         String activityJson = mActivity.toJRDictionary().toJSON();
         
         String urlEncodedActivityJson = AndroidUtils.urlEncode(activityJson);
 
         StringBuilder body = new StringBuilder();
         body.append("activity=").append(urlEncodedActivityJson);
 
         /* These are undocumented parameters for the mobile library's use */
         body.append("&device_token=").append(deviceToken);
         body.append("&url_shortening=true");
         body.append("&provider=").append(user.getProviderName());
         body.append("&device=android");
         body.append("&app_name=").append(mUrlEncodedAppName);
 
         String url = ENVIRONMENT.getServerUrl() + "/api/v2/activity";
 
         JREngage.logd(TAG, "[shareActivityForUser]: " + url + " data: " + body);
 
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
         tag.put(USERDATA_ACTIVITY_KEY, mActivity);
         tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, this, tag, null, body.toString().getBytes());
 
         JREngage.logd(TAG, "[shareActivityForUser] connection started for url: " + url);
     }
 
     public void setStatusForUser(JRAuthenticatedUser user) {
         JREngage.logd(TAG, "[shareActivityForUser]");
 
         setCurrentlyPublishingProvider(user.getProviderName());
 
         String deviceToken = user.getDeviceToken();
         String status = mActivity.getUserGeneratedContent();
         // TODO: this should also include other pieces of the activity
 
         StringBuilder body = new StringBuilder();
         // TODO: include truncate parameter here?
         body.append("status=").append(status);
 
         /* These are [undocumented] parameters available for use by the mobile library when
          * making calls to the Engage API. */
         body.append("&device_token=").append(deviceToken);
         body.append("&device=android");
         body.append("&app_name=").append(mUrlEncodedAppName);
 
         String url = ENVIRONMENT.getServerUrl() + "/api/v2/set_status";
 
         JREngage.logd(TAG, "[setStatusForUser]: " + url + " data: " + body.toString());
 
         // TODO: same callback handler for status as activity?
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
         tag.put(USERDATA_ACTIVITY_KEY, mActivity);
         tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, this, tag, null, body.toString().getBytes());
 
         JREngage.logd(TAG, "[setStatusForUser] connection started for url: " + url);
     }
 
     private void makeCallToTokenUrl(String tokenUrl, String token, String providerName) {
         JREngage.logd(TAG, "[makeCallToTokenUrl] token: " + token);
         JREngage.logd(TAG, "[makeCallToTokenUrl] tokenUrl: " + tokenUrl);
 
         String body = "token=" + token;
         byte[] postData = body.getBytes();
 
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_CALL_TOKEN_URL);
         tag.put(USERDATA_TOKEN_URL_KEY, tokenUrl);
         tag.put(USERDATA_PROVIDER_NAME_KEY, providerName);
 
         JRConnectionManager.createConnection(tokenUrl, this, tag, null, postData);
     }
 
     public void triggerAuthenticationDidCompleteWithPayload(JRDictionary rpx_result) {
         JREngage.logd(TAG, "[triggerAuthenticationDidCompleteWithPayload]");
 
         /* Instantiate a user object, keep track of it. */
         JRAuthenticatedUser user = new JRAuthenticatedUser(
                 rpx_result,
                 mCurrentlyAuthenticatingProvider.getName(),
                 getWelcomeMessageFromCookieString());
         mAuthenticatedUsersByProvider.put(mCurrentlyAuthenticatingProvider.getName(), user);
         Archiver.save(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidComplete(
                     rpx_result.getAsDictionary("auth_info"),
                     mCurrentlyAuthenticatingProvider.getName());
         }
 
         String auth_info_token = rpx_result.getAsString("token");
         if (!TextUtils.isEmpty(mTokenUrl)) {
             makeCallToTokenUrl(mTokenUrl,
                                auth_info_token,
                                mCurrentlyAuthenticatingProvider.getName());
         }
 
         mCurrentlyAuthenticatingProvider.setForceReauth(false);
         setCurrentlyAuthenticatingProvider(null);
     }
 
     public void triggerAuthenticationDidFail(JREngageError error) {
         JREngage.logd(TAG, "[triggerAuthenticationDidFailWithError]");
 
         String providerName = mCurrentlyAuthenticatingProvider.getName();
         mCurrentlyAuthenticatingProvider.setForceReauth(true);
 
         setCurrentlyAuthenticatingProvider(null);
         mReturningBasicProvider = null;
         mReturningSocialProvider = null;
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidFail(error, providerName);
         }
     }
 
     public void triggerAuthenticationDidCancel() {
         JREngage.logd(TAG, "[triggerAuthenticationDidCancel]");
 
         setCurrentlyAuthenticatingProvider(null);
         setReturningBasicProvider(null);
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidCancel();
     }
 
     public void triggerAuthenticationDidRestart() {
         JREngage.logd(TAG, "[triggerAuthenticationDidRestart]");
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidRestart();
     }
 
     public void triggerUserWasSignedOut(String provider) {
         JREngage.logd(TAG, "[triggerUserWasSignedOut]");
 
         for (JRSessionDelegate d : getDelegatesCopy()) d.userWasSignedOut(provider);
     }
 
     public void triggerPublishingDidComplete() {
         JREngage.logd(TAG, "[triggerPublishingDidComplete]");
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidComplete();
     }
 
     public void triggerPublishingJRActivityDidFail(JREngageError error,
                                                    JRActivityObject activity,
                                                    String providerName) {
         JREngage.logd(TAG, "[triggerPublishingJRActivityDidFail]");
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingJRActivityDidFail(activity, error, providerName);
         }
     }
 
     /**
      * Informs delegates that the publishing dialog failed to display.
      * @param err
      *      The error to send to delegates
      */
     public void triggerPublishingDialogDidFail(JREngageError err) {
        JREngage.logd(TAG, "[triggerPublishingDialogDidFail]");
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDialogDidFail(err);
     }
 
     /**
      *
      */
     public void triggerPublishingDidCancel() {
         JREngage.logd(TAG, "[triggerPublishingDidCancel]");
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidCancel();
     }
 
     private void triggerMobileConfigDidFinish() {
         for (JRSessionDelegate d : getDelegatesCopy()) d.mobileConfigDidFinish();
     }
 
     private synchronized List<JRSessionDelegate> getDelegatesCopy() {
         return (mDelegates == null)
                 ? new ArrayList<JRSessionDelegate>()
                 : new ArrayList<JRSessionDelegate>(mDelegates);
     }
 
     public JRProvider getCurrentlyPublishingProvider() {
         return mCurrentlyPublishingProvider;
     }
 
     public void setCurrentlyPublishingProvider(String provider) {
         JREngage.logd(TAG, "[setCurrentlyPublishingProvider]: " + provider);
 
         mCurrentlyPublishingProvider = getProviderByName(provider);
     }
 
     public boolean isGetMobileConfigDone() {
         return mGetConfigDone;
     }
 
     public static JREnvironment getEnvironment() {
         return ENVIRONMENT;
     }
 
     public String getUrlEncodedAppName() {
         return mUrlEncodedAppName;
     }
 
     public void setEnabledAuthenticationProviders(List<String> enabledProviders) {
         mEnabledAuthenticationProviders = enabledProviders;
 
         // redundantly call the setter to ensure the provider is still available
         setReturningBasicProvider(mReturningBasicProvider);
     }
 
     public void setEnabledSharingProviders(List<String> enabledSharingProviders) {
         mEnabledSharingProviders = enabledSharingProviders;
 
         // redundantly call the setter to ensure the provider is still available
         setReturningSocialProvider(mReturningSocialProvider);
     }
 
     private Context getContext() {
         return JREngage.getActivity();
     }
 }
