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
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.text.TextUtils;
 import android.webkit.CookieManager;
 import com.janrain.android.R;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.JREngageError.ConfigurationError;
 import com.janrain.android.engage.JREngageError.ErrorType;
 import com.janrain.android.engage.JREngageError.SocialPublishingError;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.AndroidUtils;
 import com.janrain.android.utils.Archiver;
 import com.janrain.android.utils.CollectionUtils;
 import com.janrain.android.utils.LogUtils;
 import com.janrain.android.utils.PrefUtils;
 import com.janrain.android.utils.StringUtils;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 public class JRSession implements JRConnectionManagerDelegate {
     private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
     private static final String ARCHIVE_AUTH_PROVIDERS = "authProviders";
     private static final String ARCHIVE_SHARING_PROVIDERS = "sharingProviders";
     private static final String ARCHIVE_AUTH_USERS_BY_PROVIDER = "jrAuthenticatedUsersByProvider";
 
     private static final String RPXNOW_BASE_URL = "https://rpxnow.com";
     private static String mEngageBaseUrl = RPXNOW_BASE_URL;
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
 
     private List<JRSessionDelegate> mDelegates;
 
     private JRProvider mCurrentlyAuthenticatingProvider;
     private JRProvider mCurrentlyPublishingProvider;
 
     private String mReturningAuthProvider;
     private String mReturningSharingProvider;
 
     private Map<String, JRProvider> mProviders;
    private Map<String, JRProvider> mAllProviders;
     private List<String> mAuthProviders;
     private List<String> mEnabledAuthenticationProviders;
     private List<String> mEnabledSharingProviders;
     private List<String> mSharingProviders;
     private Map<String, JRAuthenticatedUser> mAuthenticatedUsersByProvider;
     private List<JRProvider> mCustomProviders;
 
     private JRActivityObject mActivity;
     private String mTokenUrl;
     private String mAppId;
     private String mRpBaseUrl;
     private String mUrlEncodedAppName;
     private String mUniqueIdentifier;
 
     private boolean mConfigDone = false;
     private String mOldEtag;
     private String mSavedConfigurationBlock;
     private String mSavedEtag;
     private String mNewEtag;
     private String mUrlEncodedLibraryVersion;
 
     private boolean mHidePoweredBy;
     private boolean mAlwaysForceReauth;
     private boolean mSkipLandingPage;
     private int mUiShowingCount;
 
     private JREngageError mError;
 
     public static JRSession getInstance() {
         return sInstance;
     }
 
     public static JRSession getInstance(String appId, String tokenUrl, JRSessionDelegate delegate) {
         if (sInstance != null) {
             if (sInstance.isUiShowing()) {
                 LogUtils.loge("Cannot reinitialize JREngage while its UI is showing");
             } else {
                 LogUtils.logd("reinitializing, registered delegates will be unregistered");
                 sInstance.initialize(appId, tokenUrl, delegate);
             }
         } else {
             LogUtils.logd("returning new instance.");
             sInstance = new JRSession(appId, tokenUrl, delegate);
         }
 
         return sInstance;
     }
 
     public void setCustomProviders(Map<String, JRDictionary> customProviders) {
         if (customProviders == null) return;
 
         mCustomProviders = new ArrayList<JRProvider>();
 
         for (Map.Entry<String, JRDictionary> providerSpec : customProviders.entrySet()) {
             JRDictionary dict = providerSpec.getValue();
             if (dict.containsKey(JRProvider.KEY_SAML_PROVIDER)) {
                 mCustomProviders.add(customSamlProvider(providerSpec.getKey(), dict));
             } else if (dict.containsKey(JRProvider.KEY_OPENID_IDENTIFIER)) {
                 mCustomProviders.add(customOpenIdProvider(providerSpec.getKey(), dict));
             }
         }
     }
 
     private JRProvider customSamlProvider(String providerId, JRDictionary dictionary) {
         JRDictionary dict = new JRDictionary(dictionary);
         dict.put(JRProvider.KEY_URL, "/saml2/sso/start");
         dict.put(JRProvider.KEY_SAML_PROVIDER,
                  AndroidUtils.urlEncode(dict.getAsString(JRProvider.KEY_SAML_PROVIDER)));
 
         return new JRProvider(providerId, dict);
     }
 
     private JRProvider customOpenIdProvider(String providerId, JRDictionary dictionary) {
         JRDictionary dict = new JRDictionary(dictionary);
         dict.put(JRProvider.KEY_URL, "/openid/start");
         dict.put(JRProvider.KEY_OPENID_IDENTIFIER,
                  AndroidUtils.urlEncode(dict.getAsString(JRProvider.KEY_OPENID_IDENTIFIER)));
 
         if (dict.containsKey(JRProvider.KEY_OPX_BLOB)) {
             dict.put(JRProvider.KEY_OPX_BLOB,
                      AndroidUtils.urlEncode(dict.getAsString(JRProvider.KEY_OPX_BLOB)));
         }
 
         return new JRProvider(providerId, dict);
     }
 
     private JRSession(String appId, String tokenUrl, JRSessionDelegate delegate) {
         initialize(appId, tokenUrl, delegate);
     }
 
     /* We runtime type check the deserialized generics so we can safely ignore these unchecked
      * assignment warnings. */
     @SuppressWarnings("unchecked")
     private void initialize(String appId, String tokenUrl, JRSessionDelegate delegate) {
         LogUtils.logd("initializing instance.");
 
         // for configurability to test against e.g. staging
         String t = StringUtils.trim(AndroidUtils.readAsset(getApplicationContext(), "engage_base_url.txt"));
         if (t != null) mEngageBaseUrl = t;
 
         mDelegates = new ArrayList<JRSessionDelegate>();
         mDelegates.add(delegate);
 
         mAppId = appId;
         mTokenUrl = tokenUrl;
         mUniqueIdentifier = this.getUniqueIdentifier();
 
         ApplicationInfo ai = AndroidUtils.getApplicationInfo();
         String appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();
         appName += ":" + getApplicationContext().getPackageName();
         mUrlEncodedAppName = AndroidUtils.urlEncode(appName);
         mUrlEncodedLibraryVersion =
                 AndroidUtils.urlEncode(getApplicationContext().getString(R.string.jr_git_describe));
 
         try {
             if (!mUrlEncodedLibraryVersion.equals(PrefUtils.getString(PrefUtils.KEY_JR_ENGAGE_LIBRARY_VERSION,
                     ""))) {
                 // If the library versions don't match start with fresh state in order to break out of
                 // any invalid state.
                 throw new Archiver.LoadException("New library version with old serialized state");
             }
 
             /* load the last used auth and social providers */
             mReturningSharingProvider = PrefUtils.getString(PrefUtils.KEY_JR_LAST_USED_SHARING_PROVIDER, "");
             mReturningAuthProvider = PrefUtils.getString(PrefUtils.KEY_JR_LAST_USED_AUTH_PROVIDER, "");
 
             /* Load the library state from disk */
             mAuthenticatedUsersByProvider = Archiver.load(ARCHIVE_AUTH_USERS_BY_PROVIDER);
             mProviders = Archiver.load(ARCHIVE_ALL_PROVIDERS);
 
             /* Fix up the provider objects with data that isn't serialized along with them */
             for (Object provider : mProviders.values()) ((JRProvider)provider).loadDynamicVariables();
 
             /* Load the list of auth providers */
             mAuthProviders = Archiver.load(ARCHIVE_AUTH_PROVIDERS);
             for (Object v : mAuthProviders) {
                 if (!(v instanceof  String)) throw new Archiver.LoadException("Non String in mAuthProviders");
             }
             LogUtils.logd("auth providers: [" + TextUtils.join(",", mAuthProviders) + "]");
 
             /* Load the list of social providers */
             mSharingProviders = Archiver.load(ARCHIVE_SHARING_PROVIDERS);
             for (Object v : mSharingProviders) {
                 if (!(v instanceof  String)) {
                     throw new Archiver.LoadException("Non String in mSharingProviders");
                 }
             }
             LogUtils.logd("sharing providers: [" + TextUtils.join(",", mSharingProviders) + "]");
 
             /* Load the RP's base url */
             mRpBaseUrl = PrefUtils.getString(PrefUtils.KEY_JR_RP_BASE_URL, "");
 
             /* Figure out of we're suppose to hide the powered by line */
             mHidePoweredBy = PrefUtils.getBoolean(PrefUtils.KEY_JR_HIDE_POWERED_BY, false);
 
             /* If the configuration for this RP has changed, the etag will have changed, and we need
              * to update our current configuration information. */
             mOldEtag = PrefUtils.getString(PrefUtils.KEY_JR_CONFIGURATION_ETAG, "");
             //throw new Archiver.LoadException(null);
         } catch (Archiver.LoadException e) {
             //LogUtils.logd("LoadException loading serialized configuration, initializing from empty state. " +
             //         " Version: " + mUrlEncodedLibraryVersion + " LoadException: " +
             //        e.getStackTrace()[0].toString() + " Nested exception: " + e.getCause());
             /* Blank slate */
             mAuthenticatedUsersByProvider = new HashMap<String, JRAuthenticatedUser>();
             Archiver.asyncSave(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
 
             // Note that these values are removed from the settings when resetting state to prevent
             // uninitialized state from being read on startup as valid state
             mProviders = new HashMap<String, JRProvider>();
             Archiver.delete(ARCHIVE_ALL_PROVIDERS);
             mAuthProviders = new ArrayList<String>();
             Archiver.delete(ARCHIVE_AUTH_PROVIDERS);
             mSharingProviders = new ArrayList<String>();
             Archiver.delete(ARCHIVE_SHARING_PROVIDERS);
             mRpBaseUrl = "";
             PrefUtils.remove(PrefUtils.KEY_JR_RP_BASE_URL);
             mHidePoweredBy = true;
             PrefUtils.remove(PrefUtils.KEY_JR_HIDE_POWERED_BY);
             mOldEtag = "";
             PrefUtils.remove(PrefUtils.KEY_JR_CONFIGURATION_ETAG);
             
             // Note that these values are not removed from the Prefs, they can't result in invalid state
             // (The library is accepting of values not belonging to the set of enabled providers.)
             mReturningAuthProvider = PrefUtils.getString(PrefUtils.KEY_JR_LAST_USED_AUTH_PROVIDER, null);
             mReturningSharingProvider = PrefUtils.getString(PrefUtils.KEY_JR_LAST_USED_SHARING_PROVIDER,
                     null);
             //mConfigDone = false;
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
 
     public boolean getSkipLandingPage() {
         return mSkipLandingPage;
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
         mCurrentlyAuthenticatingProvider = provider;
     }
 
     public ArrayList<JRProvider> getAuthProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
 
         if ((mAuthProviders != null) && (mAuthProviders.size() > 0)) {
             for (String name : mAuthProviders) {
                 // Filter by enabled provider list if available
                 if (mEnabledAuthenticationProviders != null &&
                         !mEnabledAuthenticationProviders.contains(name)) continue;
                 providerList.add(mProviders.get(name));
             }
         }
         if (mCustomProviders != null) providerList.addAll(mCustomProviders);
 
         return providerList;
     }
 
     private Map<String, JRProvider> getAllProviders() {
        if (mAllProviders == null) {
            mAllProviders = new HashMap<String, JRProvider>();
            mAllProviders.putAll(mProviders);
 
            if (mCustomProviders != null) {
                for (JRProvider provider : mCustomProviders) {
                    mAllProviders.put(provider.getName(), provider);
                }
             }
         }
 
        return mAllProviders;
     }
 
     /**
      * Gets the configured and enabled sharing providers
      *
      * @return an ArrayList&lt;Provider>, does not return null.
      */
     public ArrayList<JRProvider> getSharingProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
 
         if ((mSharingProviders != null) && (mSharingProviders.size() > 0)) {
             for (String name : mSharingProviders) {
                 // Filter by enabled provider list if available
                 if (mEnabledSharingProviders != null &&
                         !mEnabledSharingProviders.contains(name)) continue;
                 providerList.add(getAllProviders().get(name));
             }
         }
 
         return providerList;
     }
 
     public String getReturningAuthProvider() {
         /* This is here so that when a calling application sets mSkipLandingPage, the dialog always opens
          * to the providers list. (See JRProviderListFragment.onCreate for an explanation of the flow control
          * when there's a "returning provider.") */
         if (mSkipLandingPage)
             return null;
 
         return mReturningAuthProvider;
     }
 
     public void setReturningAuthProvider(String returningAuthProvider) {
         if (TextUtils.isEmpty(returningAuthProvider)) returningAuthProvider = ""; // nulls -> ""s
         if (!getAuthProviders().contains(getProviderByName(returningAuthProvider))) {
             returningAuthProvider = "";
         }
 
         mReturningAuthProvider = returningAuthProvider;
         PrefUtils.putString(PrefUtils.KEY_JR_LAST_USED_AUTH_PROVIDER, returningAuthProvider);
     }
 
     public String getReturningSharingProvider() {
         return mReturningSharingProvider;
     }
 
     public void setReturningSharingProvider(String returningSharingProvider) {
         if (TextUtils.isEmpty(returningSharingProvider)) returningSharingProvider = ""; // nulls -> ""s
         if (!getSharingProviders().contains(getProviderByName(returningSharingProvider))) {
             returningSharingProvider = "";
         }
 
         mReturningSharingProvider = returningSharingProvider;
         PrefUtils.putString(PrefUtils.KEY_JR_LAST_USED_SHARING_PROVIDER, returningSharingProvider);
     }
 
     public String getRpBaseUrl() {
         return mRpBaseUrl;
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
 
         if (mUiShowingCount == 0 && mSavedConfigurationBlock != null) {
             String s = mSavedConfigurationBlock;
             mSavedConfigurationBlock = null;
             mNewEtag = mSavedEtag;
             finishGetConfiguration(s);
         }
     }
 
     public void connectionDidFail(Exception ex, HttpResponseHeaders responseHeaders, byte[] payload,
                                   String requestUrl, Object tag) {
         if (tag == null) {
             LogUtils.loge("unexpected null tag");
         } else if (tag instanceof String) {
             if (tag.equals(TAG_GET_CONFIGURATION)) {
                 LogUtils.loge("failure for getConfiguration");
                 mError = new JREngageError(
                         getApplicationContext().getString(R.string.jr_getconfig_network_failure_message),
                         ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_FAILED,
                         ex);
                 triggerConfigDidFinish();
             } else {
                 LogUtils.loge("unrecognized ConnectionManager tag: " + tag, ex);
             }
         } else if (tag instanceof JRDictionary) {
             JRDictionary tagAsDict = (JRDictionary) tag;
             if (tagAsDict.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_CALL_TOKEN_URL)) {
                 LogUtils.loge("call to token url failed: ", ex);
                 JREngageError error = new JREngageError(
                         "Error: " + ex.getLocalizedMessage(),
                         JREngageError.AuthenticationError.AUTHENTICATION_TOKEN_URL_FAILED,
                         "Failed to reach authentication token URL",
                         ex);
                 for (JRSessionDelegate delegate : getDelegatesCopy()) {
                     delegate.authenticationCallToTokenUrlDidFail(
                             tagAsDict.getAsString(USERDATA_TOKEN_URL_KEY),
                             error,
                             tagAsDict.getAsString(USERDATA_PROVIDER_NAME_KEY));
                 }
             } else if (tagAsDict.getAsString(USERDATA_ACTION_KEY).equals(USERDATA_ACTION_SHARE_ACTIVITY)) {
                 // set status uses this same connection handler.
                 processShareActivityResponse(new String(payload), tagAsDict);
             }
         }
     }
 
     private void processShareActivityResponse(String payload, JRDictionary userDataTag) {
         String providerName = userDataTag.getAsString(USERDATA_PROVIDER_NAME_KEY);
 
         JRDictionary responseDict;
         try {
             responseDict = JRDictionary.fromJsonString(payload);
         } catch (JSONException e) {
             // No JSON response
             setCurrentlyPublishingProvider(null);
             triggerPublishingJRActivityDidFail(
                     new JREngageError(payload, SocialPublishingError.FAILED, ErrorType.PUBLISH_FAILED),
                     (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY), providerName);
             return;
         }
 
         if (!"ok".equals(responseDict.get("stat"))) {
             // Bad or missing stat value
             setCurrentlyPublishingProvider(null);
             JRDictionary errorDict = responseDict.getAsDictionary("err");
             JREngageError publishError;
 
             if (errorDict != null) {
                 // report the error as found in the err dict
                 publishError = processShareActivityFailureResponse(errorDict);
             } else {
                 // bad api response
                 publishError = new JREngageError(
                         getApplicationContext().getString(R.string.jr_problem_sharing),
                         SocialPublishingError.FAILED,
                         ErrorType.PUBLISH_FAILED);
             }
 
             triggerPublishingJRActivityDidFail(publishError,
                     (JRActivityObject) userDataTag.get(USERDATA_ACTIVITY_KEY), providerName);
             return;
         }
 
         // No error
         setReturningSharingProvider(getCurrentlyPublishingProvider().getName());
         setCurrentlyPublishingProvider(null);
         triggerPublishingJRActivityDidSucceed(mActivity, providerName);
     }
 
     private JREngageError processShareActivityFailureResponse(JRDictionary errorDict) {
         JREngageError publishError;
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
             default:
                 publishError = new JREngageError(
                         getApplicationContext().getString(R.string.jr_problem_sharing),
                         SocialPublishingError.FAILED,
                         ErrorType.PUBLISH_FAILED);
                 break;
         }
 
         return publishError;
     }
 
     public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                            String requestUrl, Object tag) {
         String payloadString = new String(payload);
 
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
                 LogUtils.loge("unexpected userdata found in ConnectionDidFinishLoading: " + dictionary);
             }
         } else if (tag instanceof String) {
             if (tag.equals(TAG_GET_CONFIGURATION)) {
                 if (headers.getResponseCode() == HttpStatus.SC_NOT_MODIFIED) {
                     /* If the ETag matched, we're done. */
                     LogUtils.logd("[connectionDidFinishLoading] HTTP_NOT_MODIFIED -> matched ETag");
                     triggerConfigDidFinish();
                     return;
                 }
 
                 finishGetConfiguration(payloadString, headers.getETag());
             } else {
                 LogUtils.loge("unexpected userData found in ConnectionDidFinishLoading full");
             }
         }
     }
 
     public void addDelegate(JRSessionDelegate delegate) {
         mDelegates.add(delegate);
     }
 
     public void removeDelegate(JRSessionDelegate delegate) {
         mDelegates.remove(delegate);
     }
 
     public void tryToReconfigureLibrary() {
         mConfigDone = false;
         mError = null;
         mError = startGetConfiguration();
     }
 
     private JREngageError startGetConfiguration() {
         String urlString = String.format(UNFORMATTED_CONFIG_URL,
                 mEngageBaseUrl,
                 mAppId,
                 mUrlEncodedAppName,
                 mUrlEncodedLibraryVersion);
         BasicNameValuePair eTagHeader = new BasicNameValuePair("If-None-Match", mOldEtag);
         List<NameValuePair> headerList = new ArrayList<NameValuePair>();
         headerList.add(eTagHeader);
 
         JRConnectionManager.createConnection(urlString, this, TAG_GET_CONFIGURATION, headerList, null, false);
 
         return null;
     }
 
     private void finishGetConfiguration(String dataStr) {
         JRDictionary jsonDict;
         try {
             jsonDict = JRDictionary.fromJsonString(dataStr);
         } catch (JSONException e) {
             LogUtils.loge("failed", e);
             mError = new JREngageError(
                     getApplicationContext().getString(R.string.jr_getconfig_parse_error_message),
                     ConfigurationError.JSON_ERROR, ErrorType.CONFIGURATION_FAILED, e);
             return;
         }
 
         mRpBaseUrl = StringUtils.chomp(jsonDict.getAsString("baseurl", ""), "/");
         PrefUtils.putString(PrefUtils.KEY_JR_RP_BASE_URL, mRpBaseUrl);
 
         mProviders = new HashMap<String, JRProvider>();
         JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
         for (String name : providerInfo.keySet()) {
             mProviders.put(name, new JRProvider(name, providerInfo.getAsDictionary(name)));
         }
         Archiver.asyncSave(ARCHIVE_ALL_PROVIDERS, mProviders);
 
         mAuthProviders = jsonDict.getAsListOfStrings("enabled_providers");
         mSharingProviders = jsonDict.getAsListOfStrings("social_providers");
         Archiver.asyncSave(ARCHIVE_AUTH_PROVIDERS, mAuthProviders);
         Archiver.asyncSave(ARCHIVE_SHARING_PROVIDERS, mSharingProviders);
 
         mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
         PrefUtils.putBoolean(PrefUtils.KEY_JR_HIDE_POWERED_BY, mHidePoweredBy);
 
         /* Ensures that the returning auth and sharing providers,
          * if set, are members of the configured set of providers. */
         setReturningAuthProvider(mReturningAuthProvider);
         setReturningSharingProvider(mReturningSharingProvider);
 
         PrefUtils.putString(PrefUtils.KEY_JR_CONFIGURATION_ETAG, mNewEtag);
         PrefUtils.putString(PrefUtils.KEY_JR_ENGAGE_LIBRARY_VERSION, mUrlEncodedLibraryVersion);
 
         mError = null;
         triggerConfigDidFinish();
     }
 
     private void finishGetConfiguration(String dataStr, String eTag) {
         /* Only update all the config if the UI isn't currently displayed / using that
          * information.  Otherwise, the library may crash/behave inconsistently. In the case
          * where a dialog is showing but there isn't any config data that it could be using (that
          * is, the lists of auth and sharing providers are empty), update the config. */
         if (!isUiShowing() ||
                 (CollectionUtils.isEmpty(mAuthProviders) && CollectionUtils.isEmpty(mSharingProviders))) {
             mNewEtag = eTag;
             finishGetConfiguration(dataStr);
             return;
         }
 
         /* Otherwise, we have to save all this information for later. When no UI is displayed
          * mSavedConfigurationBlock is checked and the config is updated then. */
         mSavedConfigurationBlock = dataStr;
         mSavedEtag = eTag;
 
         mError = null;
     }
 
     private boolean isUiShowing() {
         return mUiShowingCount != 0;
     }
 
     private String getWelcomeMessageFromCookieString() {
         String cookies = CookieManager.getInstance().getCookie(getRpBaseUrl());
         String cookieString = cookies.replaceAll(".*welcome_info=([^;]*).*", "$1");
 
         if (!TextUtils.isEmpty(cookieString)) {
             String[] parts = cookieString.split("%22");
             if (parts.length > 5) return "Sign in as " + AndroidUtils.urlDecode(parts[5]);
         }
 
         return null;
     }
 
     public void saveLastUsedAuthProvider() {
         setReturningAuthProvider(mCurrentlyAuthenticatingProvider.getName());
     }
 
     public URL startUrlForCurrentlyAuthenticatingProvider() {
         // can happen on Android process restart:
         if (mCurrentlyAuthenticatingProvider == null) return null;
 
         String oid = ""; /* open identifier */
         String extraParamString = "";
 
         if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpenIdentifier())) {
             oid = String.format("openid_identifier=%s&",
                     mCurrentlyAuthenticatingProvider.getOpenIdentifier());
             if (mCurrentlyAuthenticatingProvider.requiresInput()) {
                 oid = oid.replaceAll("%@", mCurrentlyAuthenticatingProvider.getUserInput());
             } else {
                 oid = oid.replaceAll("%@", "");
             }
 
             if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpxBlob())) {
                 extraParamString =
                         String.format("opx_blob=%s&", mCurrentlyAuthenticatingProvider.getOpxBlob());
             }
 
         } else if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getSamlProvider())) {
             extraParamString =
                     String.format("saml_provider=%s&", mCurrentlyAuthenticatingProvider.getSamlProvider());
         }
 
         String fullStartUrl;
 
         boolean forceReauthUrlFlag = mAlwaysForceReauth ||
                 mCurrentlyAuthenticatingProvider.getForceReauthUrlFlag();
         if (forceReauthUrlFlag) {
             mCurrentlyAuthenticatingProvider.clearCookiesOnCookieDomains(getApplicationContext());
         }
 
         fullStartUrl = String.format("%s%s?%s%s%sdevice=android&extended=true&installation_id=%s",
                 mRpBaseUrl,
                 mCurrentlyAuthenticatingProvider.getStartAuthenticationUrl(),
                 oid,
                 extraParamString,
                 (forceReauthUrlFlag ? "force_reauth=true&" : ""),
                 AndroidUtils.urlEncode(mUniqueIdentifier)
         );
 
         LogUtils.logd("startUrl: " + fullStartUrl);
 
         URL url = null;
         try {
             url = new URL(fullStartUrl);
         } catch (MalformedURLException e) {
             throwDebugException(new RuntimeException("URL create failed for string: " + fullStartUrl, e));
         }
         return url;
     }
 
     private String getUniqueIdentifier() {
         String idString = PrefUtils.getString(PrefUtils.KEY_JR_UNIVERSALLY_UNIQUE_ID, null);
 
         if (idString == null) {
             UUID id = UUID.randomUUID();
             idString = id.toString();
 
             PrefUtils.putString(PrefUtils.KEY_JR_UNIVERSALLY_UNIQUE_ID, idString);
         }
 
         return idString;
     }
 
     public JRAuthenticatedUser getAuthenticatedUserForProvider(JRProvider provider) {
         return mAuthenticatedUsersByProvider.get(provider.getName());
     }
 
     public void signOutUserForProvider(String providerName) {
         if (getAllProviders() == null) {
             throwDebugException(new IllegalStateException());
             return;
         }
 
         JRProvider provider = getAllProviders().get(providerName);
         if (provider == null) {
             throwDebugException(new IllegalStateException("Unknown provider name:" + providerName));
             return;
         }
 
         provider.forceReauth(getApplicationContext()); // MOB-135 and MOB-181
 
         if (mAuthenticatedUsersByProvider == null) {
             throwDebugException(new IllegalStateException());
             return;
         }
 
         if (mAuthenticatedUsersByProvider.containsKey(providerName)) {
             mAuthenticatedUsersByProvider.get(providerName).deleteCachedProfilePic();
             mAuthenticatedUsersByProvider.remove(providerName);
             Archiver.asyncSave(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
             triggerUserWasSignedOut(providerName);
         }
     }
 
     public void signOutAllAuthenticatedUsers() {
         for (String p : getAllProviders().keySet()) signOutUserForProvider(p);
     }
 
     public JRProvider getProviderByName(String name) {
         return getAllProviders().get(name);
     }
 
     public void notifyEmailSmsShare(String method) {
         StringBuilder body = new StringBuilder();
         body.append("method=").append(method);
         body.append("&device=").append("android");
         body.append("&appId=").append(mAppId);
 
         String url = mEngageBaseUrl + "/social/record_activity";
 
         JRConnectionManagerDelegate jrcmd = new SimpleJRConnectionManagerDelegate() {
             @Override
             public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                    byte[] payload,
                                                    String requestUrl,
                                                    Object tag) {
             }
 
             @Override
             public void connectionDidFail(Exception ex,
                                           HttpResponseHeaders responseHeaders,
                                           byte[] payload, String requestUrl,
                                           Object tag) {
                 LogUtils.loge("notify failure", ex);
             }
         };
 
         JRConnectionManager.createConnection(url, jrcmd, null, null, body.toString().getBytes(), false);
     }
 
     public void shareActivityForUser(JRAuthenticatedUser user) {
         setCurrentlyPublishingProvider(user.getProviderName());
 
         /* Truncate the resource description if necessary */
         int descMaxChars = getCurrentlyPublishingProvider().getSocialSharingProperties()
                 .getAsInt(JRDictionary.KEY_DESC_MAX_CHARS, -1);
         if (descMaxChars > 0 && mActivity.getDescription().length() > descMaxChars) {
             mActivity.setDescription(mActivity.getDescription().substring(0, 255));
         }
 
         String deviceToken = user.getDeviceToken();
         String activityJson = mActivity.toJRDictionary().toJson();
         String urlEncodedActivityJson = AndroidUtils.urlEncode(activityJson);
 
         StringBuilder body = new StringBuilder();
 
         body.append("activity=").append(urlEncodedActivityJson);
         /* These are undocumented parameters for the mobile library's use */
         body.append("&device_token=").append(deviceToken);
         body.append("&url_shortening=true");
         body.append("&provider=").append(user.getProviderName());
         body.append("&device=android");
         body.append("&app_name=").append(mUrlEncodedAppName);
 
         String url = mEngageBaseUrl + "/api/v2/activity";
 
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
         tag.put(USERDATA_ACTIVITY_KEY, mActivity);
         tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, this, tag, null, body.toString().getBytes(), false);
     }
 
     public void setStatusForUser(JRAuthenticatedUser user) {
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
 
         String url = mEngageBaseUrl + "/api/v2/set_status";
 
         // TODO: same callback handler for status as activity?
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_SHARE_ACTIVITY);
         tag.put(USERDATA_ACTIVITY_KEY, mActivity);
         tag.put(USERDATA_PROVIDER_NAME_KEY, mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, this, tag, null, body.toString().getBytes(), false);
     }
 
     private void makeCallToTokenUrl(String tokenUrl, String token, String providerName) {
         String body = "token=" + token;
         byte[] postData = body.getBytes();
 
         JRDictionary tag = new JRDictionary();
         tag.put(USERDATA_ACTION_KEY, USERDATA_ACTION_CALL_TOKEN_URL);
         tag.put(USERDATA_TOKEN_URL_KEY, tokenUrl);
         tag.put(USERDATA_PROVIDER_NAME_KEY, providerName);
 
         JRConnectionManager.createConnection(tokenUrl, this, tag, null, postData, false);
     }
 
     public void triggerAuthenticationDidCompleteWithPayload(JRDictionary rpx_result) {
         JRAuthenticatedUser user = null;
         if (!rpx_result.getAsDictionary("auth_info").isEmpty()) {
             user = new JRAuthenticatedUser(
                     rpx_result,
                     mCurrentlyAuthenticatingProvider.getName(),
                     getWelcomeMessageFromCookieString());
             mAuthenticatedUsersByProvider.put(mCurrentlyAuthenticatingProvider.getName(), user);
             Archiver.asyncSave(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
         }
 
         String authInfoToken = rpx_result.getAsString("token");
         JRDictionary authInfoDict = rpx_result.getAsDictionary("auth_info");
         authInfoDict.put("token", authInfoToken);
         if (user != null) {
             authInfoDict.put("device_token", user.getDeviceToken());
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidComplete(
                     authInfoDict,
                     mCurrentlyAuthenticatingProvider.getName());
         }
 
         if (!TextUtils.isEmpty(mTokenUrl)) {
             makeCallToTokenUrl(mTokenUrl, authInfoToken, mCurrentlyAuthenticatingProvider.getName());
         }
 
         mCurrentlyAuthenticatingProvider.clearForceReauth();
         setCurrentlyAuthenticatingProvider(null);
     }
 
     public void triggerAuthenticationDidFail(JREngageError error) {
         if (mCurrentlyAuthenticatingProvider == null) {
             throwDebugException(new RuntimeException("Unexpected state"));
         }
 
         String providerName = mCurrentlyAuthenticatingProvider.getName();
         signOutUserForProvider(providerName);
 
         setCurrentlyAuthenticatingProvider(null);
         mReturningAuthProvider = null;
         mReturningSharingProvider = null;
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidFail(error, providerName);
         }
     }
 
     public void triggerAuthenticationDidCancel() {
         setCurrentlyAuthenticatingProvider(null);
         setReturningAuthProvider(null);
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidCancel();
     }
 
     public void triggerAuthenticationDidRestart() {
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.authenticationDidRestart();
     }
 
     public void triggerUserWasSignedOut(String provider) {
         for (JRSessionDelegate d : getDelegatesCopy()) d.userWasSignedOut(provider);
     }
 
     public void triggerPublishingDidComplete() {
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidComplete();
     }
 
     public void triggerPublishingJRActivityDidFail(JREngageError error,
                                                    JRActivityObject activity,
                                                    String providerName) {
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingJRActivityDidFail(activity, error, providerName);
         }
     }
 
     private void triggerPublishingJRActivityDidSucceed(JRActivityObject mActivity, String providerName) {
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingJRActivityDidSucceed(mActivity, providerName);
         }
     }
 
     /**
      * Informs delegates that the publishing dialog failed to display.
      * @param err
      *      The error to send to delegates
      */
     public void triggerPublishingDialogDidFail(JREngageError err) {
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDialogDidFail(err);
     }
 
     public void triggerPublishingDidCancel() {
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidCancel();
     }
 
     private void triggerConfigDidFinish() {
         mConfigDone = true;
         for (JRSessionDelegate d : getDelegatesCopy()) d.configDidFinish();
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
         mCurrentlyPublishingProvider = getProviderByName(provider);
     }
 
     public boolean isConfigDone() {
         return mConfigDone;
     }
 
     public String getUrlEncodedAppName() {
         return mUrlEncodedAppName;
     }
 
     public void setEnabledAuthenticationProviders(List<String> enabledProviders) {
         mEnabledAuthenticationProviders = enabledProviders;
 
         // redundantly call the setter to ensure the provider is still available
         setReturningAuthProvider(mReturningAuthProvider);
     }
 
     public List<String> getEnabledAuthenticationProviders() {
         if (mEnabledAuthenticationProviders == null) {
             return mAuthProviders;
         } else {
             return mEnabledAuthenticationProviders;
         }
     }
 
     public void setEnabledSharingProviders(List<String> enabledSharingProviders) {
         mEnabledSharingProviders = enabledSharingProviders;
 
         // redundantly call the setter to ensure the provider is still available
         setReturningSharingProvider(mReturningSharingProvider);
     }
     
     //public List<String> getEnabledSharingProviders() {
     //    if (mEnabledSharingProviders == null) {
     //        return mSharingProviders;
     //    } else {
     //        return mEnabledSharingProviders;
     //    }
     //}
 
     private Context getApplicationContext() {
         return JREngage.getApplicationContext();
     }
 
     /* package */ String getEngageBaseUrl() {
         return mEngageBaseUrl;
     }
 
     public String getTokenUrl() {
         return mTokenUrl;
     }
 }
