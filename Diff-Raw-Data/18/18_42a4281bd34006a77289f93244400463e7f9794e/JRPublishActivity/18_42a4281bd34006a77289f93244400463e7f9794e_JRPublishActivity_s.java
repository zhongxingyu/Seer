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
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.ui;
 
 import android.app.*;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.*;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.*;
 import android.util.Config;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import android.widget.Button;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.session.*;
 import com.janrain.android.engage.types.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 import org.json.JSONTokener;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Publishing UI
  */
 public class JRPublishActivity extends TabActivity implements TabHost.OnTabChangeListener {
     private static final int DIALOG_FAILURE = 1;
     private static final int DIALOG_SUCCESS = 2;
     private static final int DIALOG_CONFIRM_SIGNOUT = 3;
 
     // ------------------------------------------------------------------------
     // TYPES
     // ------------------------------------------------------------------------
 
     /**
      * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
      * for iPhone-like ability to close this activity from the maestro class.
      */
 
     private class FinishReceiver extends BroadcastReceiver {
 
         private final String TAG = JRPublishActivity.TAG + "-" + FinishReceiver.class.getSimpleName();
 
         @Override
         public void onReceive(Context context, Intent intent) {
             String target = intent.getStringExtra(
                     JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
             if (JRPublishActivity.class.toString().equals(target)) {
                 tryToFinishActivity();
                 Log.i(TAG, "[onReceive] handled");
             } else if (Config.LOGD) {
                 Log.i(TAG, "[onReceive] ignored");
             }
         }
     }
 
     /**
      * UI display attributes for for each of the supported providers.
      * Used to map between
      */
 
 //    private static class ProviderDisplayInfo {
 //        // whether or not activity info section is displayed
 //        private boolean mIsMediaContentVisible;
 //        // provider icon resource id
 //        private int mIconResId;
 //        // share section background color
 //        private int mShareBgColorResId;
 //        // share button resource id
 //        private int mShareButtonResId;
 //
 //        ProviderDisplayInfo(boolean isInfoVisible, int iconResId, int shareBgColorResId, int shareBtnResId) {
 //            mIsMediaContentVisible = isInfoVisible;
 //            mIconResId = iconResId;
 //            mShareBgColorResId = shareBgColorResId;
 //            mShareButtonResId = shareBtnResId;
 //        }
 //
 //        boolean getIsMediaContentVisible() {
 //            return mIsMediaContentVisible;
 //        }
 //        int getIconResId() {
 //            return mIconResId;
 //        }
 //        int getShareBgColorResId() {
 //            return mShareBgColorResId;
 //        }
 //        int getShareBtnResId() {
 //            return mShareButtonResId;
 //        }
 //    }
 
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     private static final String TAG = JRPublishActivity.class.getSimpleName();
 
 //    private static HashMap<String, ProviderDisplayInfo> PROVIDER_MAP;
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
 //    static {
 //        PROVIDER_MAP = new HashMap<String, ProviderDisplayInfo>();
 //        PROVIDER_MAP.put("Facebook",
 //                new ProviderDisplayInfo(
 //                        true,
 //                        R.drawable.icon_facebook_30x30,
 //                        R.color.bg_clr_facebook,
 //                        R.drawable.button_facebook_280x40));
 //        PROVIDER_MAP.put("Twitter",
 //                new ProviderDisplayInfo(
 //                        false,
 //                        R.drawable.icon_twitter_30x30,
 //                        R.color.bg_clr_twitter,
 //                        R.drawable.button_twitter_280x40));
 //        PROVIDER_MAP.put("MySpace",
 //                new ProviderDisplayInfo(
 //                        false,
 //                        R.drawable.icon_myspace_30x30,
 //                        R.color.bg_clr_myspace,
 //                        R.drawable.button_myspace_280x40));
 //        PROVIDER_MAP.put("LinkedIn",
 //                new ProviderDisplayInfo(
 //                        false,
 //                        R.drawable.icon_linkedin_30x30,
 //                        R.color.bg_clr_linkedin,
 //                        R.drawable.button_linkedin_280x40));
 //    }
 
     // TODO: We need to make this dynamic for forward compatibility
     //private Map<String, Drawable> icon_drawables;
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     //private FinishReceiver mFinishReceiver;
 
     //reference to the library model
     private JRSessionData mSessionData;
     private JRSessionDelegate mSessionDelegate; //call backs for JRSessionData
 
     //JREngage objects we're operating with
     private JRProvider mSelectedProvider; //the provider for the selected tab
     private JRAuthenticatedUser mLoggedInUser; //the user (if logged in) for the selected tab
     private JRActivityObject mActivityObject;
 
     //UI properties
     private String mShortenedActivityURL = null; //null if it hasn't been shortened
     private int mMaxCharacters;
     private String mDialogErrorMessage;
 
     //UI state transitioning variables
     private boolean mUserHasEditedText = false;
     private boolean mWeHaveJustAuthenticated = false;
     private boolean mWeAreCurrentlyPostingSomething = false;
     private boolean mWeAreWaitingForMobileConfig = false;
 
     //UI views
     private RelativeLayout mMediaContentView;
     private TextView mCharacterCountView;
     private TextView mPreviewLabelView;
     private ImageView mProviderIcon;
     private EditText mUserCommentView;
     private TextView mUneditableUserCommentView;
     private ImageView mTriangleIconView;
     private LinearLayout mProfilePicAndButtonsHorizontalLayout; //I think we don't need a handle to this
     private LinearLayout mUserProfileInformationAndShareButtonContainer; //or a handle to this
     private ImageView mUserProfilePic;
     private LinearLayout mNameAndSignOutContainer;
     private TextView mUserNameView;
     private Button mSignOutButton;
     private Button mJustShareButton;
     private Button mConnectAndShareButton;
     private LinearLayout mSharedTextAndCheckMarkContainer;
 
     //a helper class used to control display of a nice loading dialog
     private SharedLayoutHelper mLayoutHelper;
 
     //a helper class for the JRUserInterfaceMaestro
     private FinishReceiver mFinishReceiver;
 
     private HashMap<String, Boolean> mProvidersThatHaveAlreadyShared;
 
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "Activity lifecycle onCreate");
 
         setContentView(R.layout.publish_activity);
 
         mSessionData = JRSessionData.getInstance();
         mActivityObject = mSessionData.getActivity();
 
         mSessionDelegate = createSessionDelegate();
         mSessionData.addDelegate(mSessionDelegate);
 
         if (mSessionData.getHidePoweredBy()) {
             TextView poweredBy = (TextView)findViewById(R.id.powered_by_text);
             poweredBy.setVisibility(View.GONE);
         }
 
 //        TextView title = (TextView)findViewById(R.id.header_text);
 //        title.setText(getString(R.string.publish_activity_title));
 
         //View References
         mMediaContentView = (RelativeLayout) findViewById(R.id.media_content_view);
         mCharacterCountView = (TextView) findViewById(R.id.character_count_view);
         mProviderIcon = (ImageView) findViewById(R.id.provider_icon);
         mUserCommentView = (EditText) findViewById(R.id.edit_comment);
         mUneditableUserCommentView = (TextView) findViewById(R.id.uneditable_comment);
         mPreviewLabelView = (TextView) findViewById(R.id.preview_text_view);
         mTriangleIconView = (ImageView) findViewById(R.id.triangle_icon_view);
         mUserProfileInformationAndShareButtonContainer = (LinearLayout) findViewById(R.id.user_profile_information_and_share_button_container);
         mProfilePicAndButtonsHorizontalLayout = (LinearLayout) findViewById(R.id.profile_pic_and_buttons_horizontal_layout);
         mUserProfilePic = (ImageView) findViewById(R.id.profile_pic);
         mNameAndSignOutContainer = (LinearLayout) findViewById(R.id.name_and_sign_out_container);
         mUserNameView = (TextView) findViewById(R.id.user_name);
         mSignOutButton = (Button) findViewById(R.id.sign_out_button);
         mJustShareButton = (Button) findViewById(R.id.just_share_button);
         mConnectAndShareButton = (Button) findViewById(R.id.connect_and_share_button);
         mSharedTextAndCheckMarkContainer = (LinearLayout) findViewById(R.id.shared_text_and_check_mark_horizontal_layout);
 
         //configure the uneditableusercomment to be uneditable
         //having two Views for the user comment is a workaround for bug
         //http://code.google.com/p/android/issues/detail?id=2771
         mUneditableUserCommentView.setEnabled(false);
         mUneditableUserCommentView.setFocusable(false);
         mUneditableUserCommentView.setClickable(false);
 
         //View listeners
         mConnectAndShareButton.setOnClickListener(mShareButtonListener);
         mJustShareButton.setOnClickListener(mShareButtonListener);
         mUserCommentView.addTextChangedListener(new TextWatcher() {
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             }
 
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             }
 
             public void afterTextChanged(Editable editable) {
                 updateUserCommentView();
                 updateCharacterCount();
             }
         });
         mSignOutButton.setOnClickListener(mSignoutButtonListener);
 
         mProvidersThatHaveAlreadyShared = new HashMap<String, Boolean>();
 
         //SharedLayoutHelper is a spinner dialog class
         mLayoutHelper = new SharedLayoutHelper(this);
 
         //JRUserInterfaceMaestro's hook into calling this.finish()
         if (mFinishReceiver == null) {
             mFinishReceiver = new FinishReceiver();
             registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
         }
 
         List<JRProvider>socialProviders = mSessionData.getSocialProviders();
 
         if ((socialProviders == null || socialProviders.size() == 0) && !mSessionData.isGetMobileConfigDone()) {
             mWeAreWaitingForMobileConfig = true;
            mLayoutHelper.showProgressDialog();
         } else {
             asldkfj();
         }
     }
 
     private void asldkfj() {
         List<JRProvider> socialProviders = mSessionData.getSocialProviders();
         if (socialProviders == null || socialProviders.size() == 0) {
             JREngageError err = new JREngageError("Cannot load the Publish Activity, no social providers are configured.",
                     JREngageError.ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                     JREngageError.ErrorType.CONFIGURATION_INFORMATION_MISSING);
             mSessionData.triggerPublishingDialogDidFail(err);
             return;
         }
 
         //configure the properties of the UI
         fetchShortenedURLs();
 
         // TODO consider the case of the first usage of the library when the config call hasn't yet returned
         // and display a noninteractive view of the activity or something like the iOS lib
         configureTabs();
         loadViewElementPropertiesWithActivityObject();
     }
 
     private void configureTabs() {
         // TODO: If no providers
 
         //Resources res = getResources(); // Resource object to get Drawables
         TabHost tabHost = getTabHost(); // The activity TabHost
         tabHost.setup();
         TabHost.TabSpec spec;           // Reused TabSpec for each tab
 
         List<JRProvider> socialProviders = mSessionData.getSocialProviders();
 
         int currentIndex = 0, indexOfLastUsedProvider = 0;
         for (JRProvider provider : socialProviders) {
             Drawable providerIconSet = provider.getTabSpecIndicatorDrawable(getApplicationContext());
 
             spec = tabHost.newTabSpec(provider.getName())
                                 .setIndicator(provider.getFriendlyName(), providerIconSet)
                                 .setContent(R.id.tab_view_content);
             tabHost.addTab(spec);
 
             mProvidersThatHaveAlreadyShared.put(provider.getName(), false);
 
             if (provider.getName().equals(mSessionData.getReturningSocialProvider()))
                 indexOfLastUsedProvider = currentIndex;
 
             currentIndex++;
         }
 
         tabHost.setOnTabChangedListener(this);
         tabHost.setCurrentTab(indexOfLastUsedProvider);
         onTabChanged(tabHost.getCurrentTabTag()); //when TabHost is constructed it defaults to tab 0, so if
                                                   //indexOfLastUsedProvider is 0, the tab change listener won't be
                                                   //invoked, so we call it manually to ensure it is called.  (it's
                                                   //idempotent)
     }
 
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "Activity lifecycle onStart");
     }
 
     protected void onRestart() {
         super.onRestart();
         Log.d(TAG, "Activity lifecycle onRestart");
 
         if (mLayoutHelper.getProgressDialogShowing()) {
             Log.e(TAG, "onRestart: progress dialog still showing");
             //mLayoutHelper.dismissProgressDialog();  this is wrong because we can be restarted after the webview is finished but while the async activity post is still operating
             //throw new RuntimeException("onRestart while progress dialog is still showing");
         }
     }
 
     protected void onResume() {
         super.onResume();
         Log.d(TAG, "Activity lifecycle onResume");
     }
 
     protected void onStop() {
         super.onStop();
         Log.d(TAG, "Activity lifecycle onStop");
     }
 
     protected void onPause() {
         super.onPause();
         Log.d(TAG, "Activity lifecycle onPause");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         Log.d(TAG, "Activity lifecycle onDestroy");
 
 
         mSessionData.removeDelegate(mSessionDelegate);
 //        unregisterReceiver(mFinishReceiver);
     }
 
     //UI listeners
 
     private View.OnClickListener mShareButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
             mWeAreCurrentlyPostingSomething = true;
 
             if (!mUserCommentView.getText().toString().equals(""))
                 mActivityObject.setUserGeneratedContent(mUserCommentView.getText().toString());
 
             //showViewIsLoading(true);
             mLayoutHelper.showProgressDialog();
 
             if (mLoggedInUser == null) {
                 authenticateUserForSharing();
             } else {
                 shareActivity();
             }
         }
     };
 
     private View.OnClickListener mSignoutButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
            showDialog(DIALOG_CONFIRM_SIGNOUT);
         }
     };
 
     public void updateCharacterCount() {
         //todo verify correctness of the 0 remaining characters edge case
         CharSequence characterCountText;
 
         if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
             //twitter, myspace, linkedin
             if (activityUrlAffectsCharacterCountForSelectedProvider() && mShortenedActivityURL == null) {
                 //twitter, myspace
                 characterCountText = getText(R.string.calculating_remaining_characters);
             } else {
                 int preview_length = mPreviewLabelView.getText().length();
                 int chars_remaining = mMaxCharacters - preview_length;
                 if (chars_remaining < 0)
                     characterCountText = Html.fromHtml("Remaining characters: <font color=red>" + chars_remaining + "</font>");
                 else
                     characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
             }
         } else { //facebook, yahoo
             int comment_length = mUserCommentView.getText().length();
             int chars_remaining = mMaxCharacters - comment_length;
             if (chars_remaining < 0)
                 characterCountText = Html.fromHtml("Remaining characters: <font color=red>" + chars_remaining + "</font>");
             else
                 characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
         }
 
         mCharacterCountView.setText(characterCountText);
         Log.d(TAG, "updateCharacterCount: " + characterCountText);
     }
 
     public void onTabChanged(String tabId) {
         Log.d(TAG, "[onTabChange]: " + tabId);
 
         mSelectedProvider = mSessionData.getProviderByName(tabId);
 
         //XXX TabHost is setting our FrameLayout's only child to GONE when loading
         //XXX could be a bug in the TabHost, or could be a misuse of the TabHost system, this is a workaround
         findViewById(R.id.tab_view_content).setVisibility(View.VISIBLE);
 
         configureViewElementsBasedOnProvider();
         configureLoggedInUserBasedOnProvider();
         configureSharedStatusBasedOnProvider();
 
         //updateCharacterCount();
     }
 
     private void configureSharedStatusBasedOnProvider() {
         if (mProvidersThatHaveAlreadyShared.get(mSelectedProvider.getName()))
             showActivityAsShared(true);
         else
             showActivityAsShared(false);
     }
 
     /**
      * Invoked by JRUserInterfaceMaestro via FinishReceiver to close this activity.
      */
 
     public void tryToFinishActivity() {
         Log.i(TAG, "[tryToFinishActivity]");
         finish();
     }
 
     //UI property updaters
 
     public void updateUserCommentView() {
         if (!mUserHasEditedText) mUserHasEditedText = true;
 
         if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
             //twitter, myspace, linkedin
             updatePreviewTextWhenContentReplacesAction();
         } //else yahoo, facebook
     }
 
     private void updatePreviewTextWhenContentReplacesAction() {
         String newText = (!mUserCommentView.getText().toString().equals("")) ?
                   mUserCommentView.getText().toString()
                 : mActivityObject.getAction();
 
         String userNameForPreview = getUserDisplayName();
 
         String shorteningText = getString(R.string.shortening_url);
 
         if (activityUrlAffectsCharacterCountForSelectedProvider()) { //twitter/myspace -> true
             mPreviewLabelView.setText(Html.fromHtml(
                     "<b>" + userNameForPreview + "</b> " + newText + " <font color=\"#808080\">" +
                     ((mShortenedActivityURL != null) ? mShortenedActivityURL : shorteningText) + "</font>"));
 
 //            SpannableStringBuilder str = new SpannableStringBuilder(userNameForPreview + " " + newText + " " +
 //                                            ((mShortenedActivityURL != null) ? mShortenedActivityURL : R.string.shortening_url));//mPreviewLabelView.getText();//.getText();
 //
 //            // Create our span sections, and assign a format to each.
 //            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, userNameForPreview.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //            str.setSpan(new ColorSpan(android.graphics.Typeface. 0xFFFFFF00), 8, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 21, str.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 //
 //            mPreviewLabelView.setText(,);
         } else {
             mPreviewLabelView.setText(Html.fromHtml("<b> " + userNameForPreview + "</b> " + newText));
         }
     }
 
     private void updatePreviewTextWhenContentDoesNotReplaceAction() {
         mPreviewLabelView.setText(Html.fromHtml("<b>" + getUserDisplayName() + "</b> " + mActivityObject.getAction()));
     }
 
     private String getUserDisplayName() {
         String userNameForPreview = "You";
         if (mLoggedInUser != null) userNameForPreview = mLoggedInUser.getPreferredUsername();
         return userNameForPreview;
     }
 
     private void loadUserNameAndProfilePicForUserForProvider(final JRAuthenticatedUser user, final String providerName) {
         Log.d(TAG, "loadUserNameAndProfilePicForUserForProvider");
 
         if (user == null || providerName == null) {
             mUserNameView.setText("");
             mUserProfilePic.setImageResource(R.drawable.profilepic_placeholder);
             //todo set muserprofilepic
             //[self setButtonImage:myProfilePic toData:null andSetLoading:myProfilePicActivityIndicator toLoading:NO];
             return;
         }
 
         mUserNameView.setText(user.getPreferredUsername());
 
         // TODO
         ///NSData *cachedProfilePic = [cachedProfilePics objectForKey:providerName];
         FileInputStream fis = null;
         try {
             fis = openFileInput("userpic~" + user.getCachedProfilePicKey());
         } catch (FileNotFoundException e) {
         } catch (UnsupportedOperationException e) {
         }
 
         Bitmap cachedProfilePic = BitmapFactory.decodeStream(fis);
 
         if (cachedProfilePic != null) {
             mUserProfilePic.setImageBitmap(cachedProfilePic);
         } else if (user.getPhoto() != null) {
             mUserProfilePic.setImageResource(R.drawable.profilepic_placeholder);
             new AsyncTask<Void, Void, Bitmap>() {
                 protected Bitmap doInBackground(Void... voids) {
                     try {
                         URL url = new URL(user.getPhoto());
                         URLConnection urlc = url.openConnection();
                         InputStream is = urlc.getInputStream();
                         BufferedInputStream bis = new BufferedInputStream(is);
                         bis.mark(urlc.getContentLength());
                         FileOutputStream fos = openFileOutput("userpic~" + user.getCachedProfilePicKey(), MODE_PRIVATE);
                         int x;
                         while ((x = bis.read()) != -1) fos.write(x);
                         fos.close();
                         bis.reset();
                         return BitmapFactory.decodeStream(bis);
                     } catch (IOException e) {
                         Log.d(TAG, "profile pic image loader exception: " + e.toString());
                         //todo set default profile pic?
                         return null;
                     }
                 }
 
                 protected void onPostExecute(Bitmap b) {
                     if (mSelectedProvider.getName().equals(providerName))
                         mUserProfilePic.setImageBitmap(b);
                 }
             }.execute();
         } else {
             mUserProfilePic.setImageResource(R.drawable.profilepic_placeholder);
         }
     }
 
     private void showActivityAsShared(boolean shared) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName() + ": " + shared);
 
         int visibleIfShared = shared ? View.VISIBLE : View.GONE;
         int visibleIfNotShared = !shared ? View.VISIBLE : View.GONE;
 
         mSharedTextAndCheckMarkContainer.setVisibility(visibleIfShared);
 
         mUneditableUserCommentView.setText(mUserCommentView.getText());
 
         mUserCommentView.setVisibility(visibleIfNotShared);
         mUneditableUserCommentView.setVisibility(visibleIfShared);
 
         if (mLoggedInUser != null)
             mJustShareButton.setVisibility(visibleIfNotShared);
         else
             mConnectAndShareButton.setVisibility(visibleIfNotShared);
     }
 
     private void showUserAsLoggedIn(boolean loggedIn) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
 
         int visibleIfLoggedIn = loggedIn ? View.VISIBLE : View.GONE;
         int visibleIfNotLoggedIn = !loggedIn ? View.VISIBLE : View.GONE;
 
         mJustShareButton.setVisibility(visibleIfLoggedIn);
         mUserProfilePic.setVisibility(visibleIfLoggedIn);
         mNameAndSignOutContainer.setVisibility(visibleIfLoggedIn);
 
         mConnectAndShareButton.setVisibility(visibleIfNotLoggedIn);
 
         if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action"))
             updatePreviewTextWhenContentReplacesAction();
         else
             updatePreviewTextWhenContentDoesNotReplaceAction();
 
         if (loggedIn) mTriangleIconView.setPadding(145,0,0,0);
         else mTriangleIconView.setPadding(0,0,0,0);
     }
 
     private void loadViewElementPropertiesWithActivityObject() {
         //todo shouldn't this be the action if content replaces action?
 //        if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action"))
 //            mUserCommentView.setHint(mActivityObject.getAction());
 //        else
             mUserCommentView.setHint(R.string.please_enter_text);
 
         //mPreviewLabelView.setText(mActivityObject.getAction());
 
         JRMediaObject mo = null;
         if (mActivityObject.getMedia().size() > 0) mo = mActivityObject.getMedia().get(0);
 
         ImageView mci = (ImageView) findViewById(R.id.media_content_image);
         TextView  mcd = (TextView)  findViewById(R.id.media_content_description);
         TextView  mct = (TextView)  findViewById(R.id.media_content_title);
 
         //set the media_content_view = a thumbnail of the media
         try {
             if (mo != null) if (mo.hasThumbnail()) {
                 mci.setImageBitmap(BitmapFactory.decodeStream((new URL(mo.getThumbnail())).openStream()));
                 Log.d(TAG, "media image url: " + mo.getThumbnail());
             }
         } catch (Exception e) {
             //throw new RuntimeException(e);
         }
 
         //set the media content description
         mcd.setText(mActivityObject.getDescription());
 
         //set the media content title
         mct.setText(mActivityObject.getTitle());
     }
 
     private void configureViewElementsBasedOnProvider() {
         // TODO:  check "hasEditedUserContentForActivityAlready"
 
         // TODO: make this match the docs for the iphone activity object:
         // https://rpxnow.com/docs/iphone_api/interface_j_r_activity_object.html#a2e4ff78f83d0f353f8e0c17ed48ce0ab
         JRDictionary socialSharingProperties = mSelectedProvider.getSocialSharingProperties();
 
         if (socialSharingProperties.getAsBoolean("content_replaces_action"))
             updatePreviewTextWhenContentReplacesAction();
         else
             updatePreviewTextWhenContentDoesNotReplaceAction();
 
         mMaxCharacters = mSelectedProvider.getSocialSharingProperties().getAsInt("max_characters");
         if (mMaxCharacters != -1) {
             mCharacterCountView.setVisibility(View.VISIBLE);
         } else
             mCharacterCountView.setVisibility(View.GONE);
 
         updateCharacterCount();
 
         boolean can_share_media = mSelectedProvider.getSocialSharingProperties().getAsBoolean("can_share_media");
 
         //switch on or off the media content view based on the presence of media and ability to display it
         boolean showMediaContentView = mActivityObject.getMedia().size() > 0 && can_share_media;
         mMediaContentView.setVisibility(showMediaContentView ? View.VISIBLE : View.GONE);
 
         //switch on or off the action label view based on the provider accepting an action
 //        boolean contentReplacesAction = socialSharingProperties.getAsBoolean("content_replaces_action");
 //        mPreviewLabelView.setVisibility(contentReplacesAction ? View.GONE : View.VISIBLE);
 
         mUserProfileInformationAndShareButtonContainer.setBackgroundColor(colorForProviderFromArray(socialSharingProperties.get("color_values")));
 
         mJustShareButton.setBackgroundDrawable(mSelectedProvider.getProviderButtonShort(getApplicationContext()));
         mConnectAndShareButton.setBackgroundDrawable(mSelectedProvider.getProviderButtonLong(getApplicationContext()));
 
         mProviderIcon.setImageDrawable(mSelectedProvider.getProviderListIconDrawable(getApplicationContext()));
     }
 
     private int colorForProviderFromArray(Object arrayOfColorStrings) {
 
         if (!(arrayOfColorStrings instanceof ArrayList))
             return 0x33074764; // If there's ever an error, just return Janrain blue (at 20% opacity)
 
         @SuppressWarnings("unchecked")
         ArrayList<Double> colorArray = new ArrayList<Double>((ArrayList<Double>)arrayOfColorStrings);
 
         /* We need to reorder the array (which is RGBA, the color format returned by Engage) to the color format
            used by Android (which is ARGB), by moving the last element (alpha) to the front */
         Double alphaValue = colorArray.remove(3);
         colorArray.add(0, alphaValue);
         
         int finalColor = 0;
         for (Object colorValue : colorArray) {
             if(!(colorValue instanceof Double))
                 return 0x33074764; // If there's ever an error, just return Janrain blue (at 20% opacity)
 
             double colorValue_Fraction = (Double)colorValue;
 
             /* First, get an int from the double, which is the decimal percentage of 255 */
             int colorValue_Int = (int)(colorValue_Fraction * 255.0);
 
             finalColor *= 256;
             finalColor += colorValue_Int;
 
         }
 
         return finalColor;
     }
 
     private void configureLoggedInUserBasedOnProvider() {
         mLoggedInUser = mSessionData.getAuthenticatedUserForProvider(mSelectedProvider);
 
         loadUserNameAndProfilePicForUserForProvider(mLoggedInUser, mSelectedProvider.getName());
 
         showUserAsLoggedIn(mLoggedInUser != null);
     }
 
     //UI ~state updaters
 
     public Dialog onCreateDialog(int id) {
         DialogInterface.OnClickListener successDismiss = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialogInterface, int i) {
                 finish();
             }
         };
         //todo make resources out of these strings
 
         switch (id) {
             case DIALOG_SUCCESS:
                 return new AlertDialog.Builder(JRPublishActivity.this)
                         .setMessage("Success!")
                         .setCancelable(false)
                         .setPositiveButton("Dismiss", null)
                         .create();
             case DIALOG_FAILURE:
                 return new AlertDialog.Builder(JRPublishActivity.this)
                         .setMessage(mDialogErrorMessage)
                         .setPositiveButton("Dismiss", null)
                         .create();
             case DIALOG_CONFIRM_SIGNOUT:
                 return new AlertDialog.Builder(JRPublishActivity.this)
                         .setMessage("Sign out of " + mSelectedProvider.getName() + "?")
                         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 logUserOutForProvider(mSelectedProvider.getName());
                                 showUserAsLoggedIn(false);
                                 mLoggedInUser = null; //todo do this? or don't? not sure which, may have side effects
                                                       //on the preview
                                 mProvidersThatHaveAlreadyShared.put(mSelectedProvider.getName(), false);
                                 onTabChanged(getTabHost().getCurrentTabTag());
                             }
                         })
                         .setNegativeButton("Cancel", null)
                         .create();
         }
         return null;
     }
 
     private void logUserOutForProvider(String provider) {
         Log.d(TAG, new Exception().getStackTrace()[0].getMethodName());
         mSessionData.forgetAuthenticatedUserForProvider(provider);
         mLoggedInUser = null;
     }
 
     private void authenticateUserForSharing() {
      /* Set weHaveJustAuthenticated to true, so that when this view returns (for whatever reason... successful auth
         user canceled, etc), the view will know that we just went through the authentication process. */
         mWeHaveJustAuthenticated = true;
         mSessionData.setCurrentlyAuthenticatingProvider(mSelectedProvider);
 
      /* If the selected provider requires input from the user, go to the user landing view. Or if
         the user started on the user landing page, went back to the list of providers, then selected
         the same provider as their last-used provider, go back to the user landing view. */
         if (mSelectedProvider.requiresInput()) {
             JRUserInterfaceMaestro.getInstance().showUserLanding();
         } else { /* Otherwise, go straight to the web view. */
             JRUserInterfaceMaestro.getInstance().showWebView();
         }
     }
 
     private void shareActivity() {
         Log.d(TAG, "shareActivity mLoggedInUser: " + mLoggedInUser.toString());
 
         mSessionData.shareActivityForUser(mLoggedInUser);
     }
 
     private void fetchShortenedURLs() {
         //todo fixme app_name
         try {
             final String jsonEncodedActivityUrl = (new JSONStringer()).array().value(mActivityObject.getUrl()).endArray().toString();
             String htmlEncodedJsonEncodedUrl = URLEncoder.encode(jsonEncodedActivityUrl, "UTF8");
             final String urlString = mSessionData.getBaseUrl() + "/openid/get_urls?urls=" + htmlEncodedJsonEncodedUrl + "&app_name=whatever&device=android";
 
             JRConnectionManagerDelegate jrcmd = new JRCMD() {
                 public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
                     String retval = mActivityObject.getUrl();
 
                     try {
                         Log.d(TAG, "fetchShortenedURLs connectionDidFinishLoading: " + payload);
                         JSONObject jso = (JSONObject) (new JSONTokener(payload)).nextValue();
                         jso = jso.getJSONObject("urls");
                         retval = jso.getString(mActivityObject.getUrl());
                     } catch (JSONException e) {
                     } catch (ClassCastException e) {
                     }
 
                     updateUI(retval);
                 }
 
                 private void updateUI(String shortenedURL) {
                     mShortenedActivityURL = shortenedURL;
 
                     if (mSelectedProvider == null) return;
 
                     if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
                         updatePreviewTextWhenContentReplacesAction();
                     } else {
                         updatePreviewTextWhenContentDoesNotReplaceAction();
                     }
                     updateCharacterCount();
                 }
 
                 public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
                     updateUI(mActivityObject.getUrl());
                 }
 
                 public void connectionWasStopped(Object userdata) {
                     updateUI(mActivityObject.getUrl());
                 }
             };
 
             JRConnectionManager.createConnection(urlString, jrcmd, false, null);
         } catch (JSONException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     //helper functions
 
     public boolean activityUrlAffectsCharacterCountForSelectedProvider() {
         //twitter + myspace -> true
         return (mSelectedProvider.getSocialSharingProperties().getAsBoolean("url_reduces_max_chars") &&
             mSelectedProvider.getSocialSharingProperties().getAsString("shows_url_as").equals("url"));
     }
 
     //JRSessionDelegate definition
 
     private JRSessionDelegate createSessionDelegate() {
         return new JRSD() {
             public void authenticationDidRestart() {
                 Log.d(TAG, "[authenticationDidRestart]");
 
                 mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
                 mLayoutHelper.dismissProgressDialog();//this is wrong because that just means they're back to the landing page?
                                                       //or does the landing page not trigger authDidRestart?
             }
 
             // TODO: Probably need to comment this out, as authenticationDidCancel is something that publish activity
             // should never have to worry about
             public void authenticationDidCancel() {
                 Log.d(TAG, "[authenticationDidCancel]");
                 mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
                 //mLayoutHelper.dismissProgressDialog();//this is wrong because there will never be an authDidCancel message from auth started by publish?
             }
 
             public void authenticationDidFail(JREngageError error, String provider) {
                 Log.d(TAG, "[authenticationDidFail]");
                 mWeHaveJustAuthenticated = false;
                 mWeAreCurrentlyPostingSomething = false;
                 //todo display an error?
                 //todo
                 //does this ever happen? why aren't we clearing the progress dialog? is this a reentry point for publish activity?
                 //yes, this can happen if the mobile endpoint URL fails to be read correctly
                 mLayoutHelper.dismissProgressDialog();
                 mDialogErrorMessage = error.getMessage();
                 showDialog(DIALOG_FAILURE);
             }
 
             public void authenticationDidComplete(JRDictionary profile, String provider) {
                 Log.d(TAG, "[authenticationDidComplete]");
                 //myLoadingLabel.text = @"Sharing...";
 
                 mLoggedInUser = mSessionData.getAuthenticatedUserForProvider(mSelectedProvider);
 
                 // QTS: Would we ever expect this to not be the case?
 //                if (mLoggedInUser != null) {
 
                 //showViewIsLoading(true);
                 mLayoutHelper.showProgressDialog();
                 loadUserNameAndProfilePicForUserForProvider(mLoggedInUser, provider);
                 showUserAsLoggedIn(true);
 
                 shareActivity();
 
 //                } else {
 ////                    UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"Shared"
 ////                                                                     message:@"There was an error while sharing this activity."
 ////                                                                    delegate:nil
 ////                                                           cancelButtonTitle:@"OK"
 ////                                                           otherButtonTitles:nil] autorelease];
 ////                    [alert show];
 //                    showViewIsLoading(false);
 //                    mWeAreCurrentlyPostingSomething = false;
 //                    mWeHaveJustAuthenticated = false;
 //                }
             }
 
             public void publishingDidRestart() { mWeAreCurrentlyPostingSomething = false; }
             public void publishingDidCancel() { mWeAreCurrentlyPostingSomething = false; }
             public void publishingDidComplete() { mWeAreCurrentlyPostingSomething = false; }  //nothing triggers this yet
 
             public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
                 Log.d(TAG, "[publishingJRActivityDidSucceed]");
 //                UIAlertView *alert = [[[UIAlertView alloc] initWithTitle:@"Shared"
 //                                                                 message:[String stringWithFormat:
 //                                                                          @"You have successfully shared this activity."]
 //                                                                delegate:nil
 //                                                       cancelButtonTitle:@"OK"
 //                                                       otherButtonTitles:nil] autorelease];
 //                [alert show];
 
 //                [alreadyShared addObject:provider];
 
                 mProvidersThatHaveAlreadyShared.put(provider, true);
 
                 //showViewIsLoading(false);
                 mLayoutHelper.dismissProgressDialog();
                 showActivityAsShared(true);
 
                 mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
 
                 showDialog(DIALOG_SUCCESS);
             }
 
             public void publishingJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
                 Log.d(TAG, "[publishingJRActivityDidFail]");
                 boolean reauthenticate = false;
 
                 //showViewIsLoading(false);
                 mLayoutHelper.dismissProgressDialog();
 
                 switch (error.getCode())
                 {// TODO: add strings to string resource file
                     case JREngageError.SocialPublishingError.FAILED:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         break;
                     case JREngageError.SocialPublishingError.DUPLICATE_TWITTER:
                         mDialogErrorMessage = "There was an error while sharing this activity: Twitter does not allow duplicate status updates.";
                         break;
                     case JREngageError.SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED:
                         mDialogErrorMessage = "There was an error while sharing this activity: Status was too long.";
                         break;
                     case JREngageError.SocialPublishingError.MISSING_API_KEY:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         reauthenticate = true;
                         break;
                     case JREngageError.SocialPublishingError.INVALID_OAUTH_TOKEN:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         reauthenticate = true;
                         break;
                     default:
                         mDialogErrorMessage = "There was an error while sharing this activity.";
                         break;
                 }
 
              /* OK, if this gets called right after authentication succeeds, then the navigation controller won't be done
                 animating back to this view.  If this view isn't loaded yet, and we call shareButtonPressed, then the library
                 will end up trying to push the webview controller onto the navigation controller while the navigation controller
                 is still trying to pop the webview.  This creates craziness, hence we check for [self isViewLoaded].
                 Also, this prevents an infinite loop of reauthing-failed publishing-reauthing-failed publishing.
                 So, only try and reauthenticate is the publishing activity view is already loaded, which will only happen if we didn't
                 JUST try and authorize, or if sharing took longer than the time it takes to pop the view controller. */
                 if (reauthenticate && !mWeHaveJustAuthenticated) {
                     Log.d(TAG, "reauthenticating user for sharing");
                     logUserOutForProvider(provider);
                     authenticateUserForSharing();
 
                     return;
                 }
 
                 mWeAreCurrentlyPostingSomething = false;
                 mWeHaveJustAuthenticated = false;
 
                 mDialogErrorMessage += error.getMessage();
                 showDialog(DIALOG_FAILURE);
             }
 
             public void mobileConfigDidFinish() {
                 if (mWeAreWaitingForMobileConfig) {
                    mLayoutHelper.dismissProgressDialog();
                    mWeAreCurrentlyPostingSomething = false;
                     asldkfj();
                 }
             }
         };
     }
 
     private abstract class JRCMD implements JRConnectionManagerDelegate {
 
         public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
             Log.d(TAG, "default connectionDidFinishLoading");
         }
         public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload, String requestUrl, Object userdata) {
             Log.d(TAG, "default connectionDidFinishLoading full");
         }
         public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
             Log.d(TAG, "default connectionDidFail");
         }
         public void connectionWasStopped(Object userdata) {
             Log.d(TAG, "default connectionWasStopped");
         }
     }
 
     private abstract class JRSD implements JRSessionDelegate {
         public void authenticationDidRestart() {}
         public void authenticationDidCancel() {}
         public void authenticationDidComplete(String token, String provider) {}
         public void authenticationDidComplete(JRDictionary profile, String provider) {}
         public void authenticationDidFail(JREngageError error, String provider) {}
         public void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String payload, String provider) {}
         public void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {}
         public void publishingDidRestart() {}
         public void publishingDidCancel() {}
         public void publishingDidComplete() {}
         public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {}
         public void publishingDialogDidFail(JREngageError error) {}
         public void publishingJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {}
         public void mobileConfigDidFinish() {}
     }
 }
