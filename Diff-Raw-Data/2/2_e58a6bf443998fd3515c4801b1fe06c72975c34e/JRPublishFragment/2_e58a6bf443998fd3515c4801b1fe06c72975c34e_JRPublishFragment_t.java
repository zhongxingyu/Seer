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
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.PorterDuff;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.StateListDrawable;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Config;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.TextView;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.session.JRAuthenticatedUser;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSessionDelegate;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.types.JREmailObject;
 import com.janrain.android.engage.types.JRMediaObject;
 import com.janrain.android.engage.types.JRSmsObject;
 import com.janrain.android.engage.utils.AndroidUtils;
 import com.janrain.android.engage.utils.Prefs;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @internal
  *
  * @class JRPublishActivity
  * Publishing UI
  */
 public class JRPublishFragment extends JRUiFragment implements TabHost.OnTabChangeListener {
     {
         TAG = JRPublishFragment.class.getSimpleName();
     }
 
     public static final String KEY_ACTIVITY_OBJECT = "jr_activity_object";
     public static final String KEY_PROVIDER_SHARE_MAP = "jr_provider_sharedness_map";
     public static final String KEY_DIALOG_ERROR_MESSAGE = "jr_dialog_error_message";
     public static final String KEY_DIALOG_PROVIDER_NAME = "jr_dialog_provider_name";
     public static final String KEY_SELECTED_TAB = "jr_selected_tab";
     private static final int DIALOG_FAILURE = 1;
     private static final int DIALOG_CONFIRM_SIGNOUT = 3;
     private static final int DIALOG_MOBILE_CONFIG_LOADING = 4;
     private static final int DIALOG_NO_EMAIL_CLIENT = 5;
     private static final int DIALOG_NO_SMS_CLIENT = 6;
     private static final String EMAIL_SMS_TAB_TAG = "email_sms";
 
     /* JREngage objects we're operating with */
     private JRProvider mSelectedProvider; //the provider for the selected tab
     private JRAuthenticatedUser mAuthenticatedUser; //the user (if logged in) for the selected tab
     private JRActivityObject mActivityObject;
     private List<JRProvider> mSocialProviders;
 
     /* UI state */
     private HashMap<String, Boolean> mProvidersThatHaveAlreadyShared = new HashMap<String, Boolean>();
     private boolean mSmsActivityExists;
     private boolean mEmailActivityExists;
     private String mSelectedTab = "";
 
     /* UI properties */
     private String mShortenedActivityURL = null; //null if it hasn't been shortened
     private int mMaxCharacters;
 
     /* UI state transitioning variables */
     private boolean mWeHaveJustAuthenticated = false;
     private boolean mWaitingForMobileConfig = false;
     private boolean mAuthenticatingForShare = false;
 
     /* UI views */
     private LinearLayout mPreviewBorder;
     private RelativeLayout mPreviewBox;
     private RelativeLayout mMediaContentView;
     private TextView mCharacterCountView;
     private TextView mPreviewLabelView;
     private ImageView mProviderIcon;
     private EditText mUserCommentView;
     private LinearLayout mUserProfileInformationAndShareButtonContainer;
     private LinearLayout mUserProfileContainer;
     private ImageView mUserProfilePic;
     private TextView mUserNameView;
     private Button mSignOutButton;
     private ColorButton mShareButton;
     private ColorButton mConnectAndShareButton;
     private LinearLayout mSharedTextAndCheckMarkContainer;
     private ColorButton mEmailButton;
     private ColorButton mSmsButton;
     private EditText mEmailSmsComment;
     private LinearLayout mEmailSmsButtonContainer;
     private TabHost mTabHost;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if (mSession == null) return null;
         if (container == null) {
             // We may have different layouts, and in one of them this
             // fragment's containing frame may not exist.  The fragment
             // may still be created from its saved state,
             return null;
         }
 
         View content = inflater.inflate(R.layout.jr_publish, container, false);
 
         initViews(content);
 
         return content;
     }
 
     private void initViews(View content) {
         /* View References */
         mPreviewBox = (RelativeLayout) content.findViewById(R.id.jr_preview_box);
         mPreviewBorder = (LinearLayout) content.findViewById(R.id.jr_preview_box_border);
         mMediaContentView = (RelativeLayout) content.findViewById(R.id.jr_media_content_view);
         mCharacterCountView = (TextView) content.findViewById(R.id.jr_character_count_view);
         mProviderIcon = (ImageView) content.findViewById(R.id.jr_provider_icon);
         mUserCommentView = (EditText) content.findViewById(R.id.jr_edit_comment);
         mPreviewLabelView = (TextView) content.findViewById(R.id.jr_preview_text_view);
         mUserProfileInformationAndShareButtonContainer = (LinearLayout) content.findViewById(
                 R.id.jr_user_profile_information_and_share_button_container);
         mUserProfileContainer = (LinearLayout) content.findViewById(R.id.jr_user_profile_container);
         mUserProfilePic = (ImageView) content.findViewById(R.id.jr_profile_pic);
         mUserNameView = (TextView) content.findViewById(R.id.jr_user_name);
         mSignOutButton = (Button) content.findViewById(R.id.jr_sign_out_button);
         mShareButton = (ColorButton) content.findViewById(R.id.jr_just_share_button);
         mConnectAndShareButton = (ColorButton) content.findViewById(R.id.jr_connect_and_share_button);
         mSharedTextAndCheckMarkContainer = (LinearLayout) content.findViewById(
                 R.id.jr_shared_text_and_check_mark_horizontal_layout);
         mEmailButton = (ColorButton) content.findViewById(R.id.jr_email_button);
         mSmsButton = (ColorButton) content.findViewById(R.id.jr_sms_button);
         mEmailSmsComment = (EditText) content.findViewById(R.id.jr_email_sms_edit_comment);
         mEmailSmsButtonContainer = (LinearLayout) content.findViewById(R.id.jr_email_sms_button_container);
 
         /* View listeners */
         mEmailButton.setOnClickListener(mEmailButtonListener);
         mSmsButton.setOnClickListener(mSmsButtonListener);
         mSignOutButton.setOnClickListener(mSignoutButtonListener);
         mConnectAndShareButton.setOnClickListener(mShareButtonListener);
         mShareButton.setOnClickListener(mShareButtonListener);
         mUserCommentView.addTextChangedListener(mUserCommentTextWatcher);
         mEmailSmsComment.addTextChangedListener(mEmailSmsCommentTextWatcher);
 
         mSmsButton.setColor(getColor(R.color.jr_janrain_darkblue_light_100percent));
         mEmailButton.setColor(getColor(R.color.jr_janrain_darkblue_light_100percent));
 
         configureEmailSmsButtons();
     }
 
     private void configureEmailSmsButtons() {
         List<ResolveInfo> resolves;
         Intent intent;
         intent = createSmsIntent("");
         resolves = getActivity().getPackageManager().queryIntentActivities(intent,
                 PackageManager.MATCH_DEFAULT_ONLY);
         mSmsActivityExists = resolves.size() > 0;
 
         intent = createEmailIntent("", "");
         resolves = getActivity().getPackageManager().queryIntentActivities(intent,
                 PackageManager.MATCH_DEFAULT_ONLY);
         mEmailActivityExists = resolves.size() > 0;
 
         showHideView(mEmailButton, mEmailActivityExists);
         showHideView(mSmsButton, mSmsActivityExists);
     }
 
     @Override
     @SuppressWarnings("unchecked") // for de-serializing the HashMap below
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         if (getView() == null) {
             // We may have different layouts, and in one of them this
             // fragment's containing frame may not exist.  The fragment
             // may still be created from its saved state,
             return;
         }
 
         mSession.addDelegate(mSessionDelegate);
         mActivityObject = mSession.getJRActivity();
 
         if (savedInstanceState != null) {
             mSelectedTab = savedInstanceState.getString(KEY_SELECTED_TAB);
             if (mSelectedTab == null) mSelectedTab = "";
 
             mProvidersThatHaveAlreadyShared =
                     (HashMap<String, Boolean>) savedInstanceState.getSerializable(KEY_PROVIDER_SHARE_MAP);
             if (mProvidersThatHaveAlreadyShared == null) {
                 mProvidersThatHaveAlreadyShared = new HashMap<String, Boolean>();
             }
         }
 
         if (mActivityObject == null && savedInstanceState != null) {
             mActivityObject = (JRActivityObject) savedInstanceState.getSerializable(KEY_ACTIVITY_OBJECT);
             mSession.setJRActivity(mActivityObject);
         }
         if (mActivityObject == null) {
             mActivityObject = new JRActivityObject("", null);
             Log.e(TAG, "Couldn't reload savedInstanceState or get an activity from JRSession, " +
                     "creating stub activity");
         }
         loadViewPropertiesWithActivityObject();
 
         /* Call by hand the configuration change listener so that it sets up correctly if this
          * activity started in landscape mode. */
         onConfigurationChanged(getResources().getConfiguration());
 
         mTabHost = (TabHost) getView().findViewById(android.R.id.tabhost);
         mTabHost.setup();
         mTabHost.addTab(mTabHost.newTabSpec("empty tab").setIndicator("")
                 .setContent(R.id.jr_tab_social_publish_content));
         mTabHost.getTabWidget().setVisibility(View.GONE);
 
         mSocialProviders = mSession.getSocialProviders();
         if (mSocialProviders.size() == 0 && !mSession.isGetMobileConfigDone()) {
             /* Hide the email/SMS tab so things look nice as we load the providers */
             getView().findViewById(R.id.jr_tab_email_sms_content).setVisibility(View.GONE);
             mWaitingForMobileConfig = true;
             showDialog(DIALOG_MOBILE_CONFIG_LOADING);
         } else {
             initializeWithProviderConfiguration();
         }
     }
 
     private void initializeWithProviderConfiguration() {
         /* Check for no suitable providers */
         if (mSocialProviders.size() == 0) {
             JREngageError err = mSession.getError();
             String errorMessage = (err == null) ? getString(R.string.jr_no_configured_social_providers) :
                     err.getMessage();
             if (err == null) {
                 err = new JREngageError(
                         getString(R.string.jr_no_social_providers),
                         JREngageError.ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_INFORMATION_MISSING);
             }
             mSession.triggerPublishingDialogDidFail(err);
             mConnectAndShareButton.setEnabled(false);
             mUserCommentView.setEnabled(false);
             getView().findViewById(R.id.jr_tab_email_sms_content).setVisibility(View.GONE);
             //if (isEmbeddedMode()) getFragmentManager().beginTransaction().hide(this).commit();
             Bundle options = new Bundle();
             options.putString(KEY_DIALOG_ERROR_MESSAGE, errorMessage);
             showDialog(DIALOG_FAILURE, options);
             return;
         }
 
         createTabs();
         initViews(mTabHost.getTabContentView());
         loadViewPropertiesWithActivityObject();
         onConfigurationChanged(getResources().getConfiguration());
         mTabHost.setOnTabChangedListener(this);
         onTabChanged(mTabHost.getCurrentTabTag());
 
         /* Configure the properties of the UI */
         mActivityObject.shortenUrls(new JRActivityObject.ShortenedUrlCallback() {
             public void setShortenedUrl(String shortenedUrl) {
                 mShortenedActivityURL = shortenedUrl;
 
                 if (mSelectedProvider == null) return;
                 if (!JRPublishFragment.this.isAdded()) return;
                 if (JRPublishFragment.this.isHidden()) return;
                 if (mSelectedProvider == null) return;
 
                 if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
                     updatePreviewTextWhenContentReplacesAction();
                 } else {
                     updatePreviewTextWhenContentDoesNotReplaceAction();
                 }
                 updateCharacterCount();
             }
         });
 
         /* Fire the text change listener so the character count is updated.
          * See also onCreateActivity */
         mUserCommentView.setText(mUserCommentView.getText());
     }
 
     private void createTabs() {
         mTabHost.clearAllTabs();
         mTabHost.getTabWidget().setVisibility(View.VISIBLE);
         LayoutInflater li = getLayoutInflater(null);
         li.inflate(R.layout.jr_publish_email_tab_content, mTabHost.getTabContentView());
         li.inflate(R.layout.jr_publish_provider_tab_content, mTabHost.getTabContentView());
 
         /* Make a tab for each social provider */
         for (JRProvider provider : mSocialProviders) {
             Drawable providerIconSet = provider.getTabSpecIndicatorDrawable(getActivity());
 
             TabHost.TabSpec spec = mTabHost.newTabSpec(provider.getName());
             spec.setContent(R.id.jr_tab_social_publish_content);
             String s = provider.getFriendlyName();
 
             setTabSpecIndicator(spec, providerIconSet, s);
 
             mTabHost.addTab(spec);
 
             if (!mProvidersThatHaveAlreadyShared.containsKey(provider.getName())) {
                 // If it's already there then we're reinitializing with saved state
                 mProvidersThatHaveAlreadyShared.put(provider.getName(), false);
             }
         }
 
         if (mSmsActivityExists || mEmailActivityExists) {
             /* Make a tab for email/SMS */
             TabHost.TabSpec emailSmsSpec = mTabHost.newTabSpec(EMAIL_SMS_TAB_TAG);
             Drawable d = getResources().getDrawable(R.drawable.jr_email_sms_tab_indicator);
             setTabSpecIndicator(emailSmsSpec, d, "Email/SMS");
             emailSmsSpec.setContent(R.id.jr_tab_email_sms_content);
             mTabHost.addTab(emailSmsSpec);
         } else {
             showHideView(getView().findViewById(R.id.jr_tab_email_sms_content), false);
         }
 
         /* XXX this works around a bug in TabHost where if you add new tabs after calling
          * clearAllTabs() the currently selected tab won't have it's indicator switch to the selected state.
          * (This is because when you call clearAllTabs and the current tab index is 0, it leaves the current
          * tab index at 0, then as you insert new tabs it keeps re-selecting tab 0 and no-oping the indicator
          * state change code :( )
          * TEN POINTS FROM GRY^H^HGOOGLEDOR
          */
         if (mTabHost.getTabWidget().getTabCount() > 1) {
             mTabHost.setCurrentTab(1);
             mTabHost.setCurrentTab(0);
         } // else meh
 
         if (mSelectedTab.equals("")) {
             JRProvider rp = mSession.getProviderByName(mSession.getReturningSocialProvider());
             if (rp != null) mTabHost.setCurrentTab(mSocialProviders.indexOf(rp));
         } else {
             mTabHost.setCurrentTabByTag(mSelectedTab);
         }
 
         /* XXX TabHost is setting our FrameLayout's only child to View.GONE when loading.
          * XXX That could be a bug in the TabHost, or it could be a misuse of the TabHost system.
          * See http://stackoverflow.com/questions/5109081/why-is-my-tabhosts-framelayouts-only-child-loaded-with-visibility-view-gone
          * XXX This is a workaround: */
         mTabHost.getCurrentView().setVisibility(View.VISIBLE);
     }
     
     @Override
     public void onDestroyView() {
        if (mUserCommentView != null && !mProvidersThatHaveAlreadyShared.values().contains(true)) {
             Prefs.putString(Prefs.KEY_JR_USER_COMMENT, mUserCommentView.getText().toString());
             Prefs.putLong(Prefs.KEY_JR_USER_COMMENT_TIME, new Date().getTime());
         }
 
         dismissProgressDialog();
         mSession.removeDelegate(mSessionDelegate);
 
         super.onDestroyView();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putSerializable(KEY_ACTIVITY_OBJECT, mActivityObject);
         outState.putSerializable(KEY_PROVIDER_SHARE_MAP, mProvidersThatHaveAlreadyShared);
         outState.putSerializable(KEY_SELECTED_TAB, mSelectedTab);
 
         super.onSaveInstanceState(outState);
     }
 
     private void showHideView(View v, boolean show) {
         if (show) v.setVisibility(View.VISIBLE);
         else v.setVisibility(View.GONE);
     }
 
     private void loadViewPropertiesWithActivityObject() {
         /* This sets up pieces of the UI before the provider configuration information
          * has been loaded */
         String savedComment = Prefs.getString(Prefs.KEY_JR_USER_COMMENT, "");
         long curTime = new Date().getTime();
         long commentTime = Prefs.getLong(Prefs.KEY_JR_USER_COMMENT_TIME, 0);
         if ((!"".equals(savedComment)) && ((curTime - commentTime) < 1000*60*60*24*7)) { /* one week */
             mUserCommentView.setText(savedComment);
         } else {
             mUserCommentView.setText(mActivityObject.getUserGeneratedContent());
         }
 
         JRMediaObject mo = null;
         if (mActivityObject.getMedia().size() > 0) mo = mActivityObject.getMedia().get(0);
 
         final ImageView mci = (ImageView) getView().findViewById(R.id.jr_media_content_image);
         final TextView  mcd = (TextView)  getView().findViewById(R.id.jr_media_content_description);
         final TextView  mct = (TextView)  getView().findViewById(R.id.jr_media_content_title);
 
         /* Set the media_content_view = a thumbnail of the media */
         if (mo != null) if (mo.hasThumbnail()) {
             if (Config.LOGD) Log.d(TAG, "media image url: " + mo.getThumbnail());
             mo.downloadThumbnail(new JRMediaObject.ThumbnailAvailableListener() {
                 public void onThumbnailAvailable(Bitmap bitmap) {
                     if (bitmap == null) {
                         mci.setVisibility(View.INVISIBLE);
                     } else {
                         mci.setVisibility(View.VISIBLE);
                     }
                     mci.setImageBitmap(bitmap);
             }});
         }
 
         /* Set the media content description */
         mcd.setText(mActivityObject.getDescription());
 
         /* Set the media content title */
         mct.setText(mActivityObject.getTitle());
     }
 
     private void setTabSpecIndicator(TabHost.TabSpec spec, Drawable iconSet, String label) {
         boolean doBasicTabs = false;
         try {
             //todo basic tabs? should be part of custom ui?
             if (AndroidUtils.getAndroidSdkInt() >= 11 && false) {
                 doBasicTabs = true;
             } else {
                 LinearLayout ll = createTabSpecIndicator(label, iconSet);
                 Method setIndicator = spec.getClass().getDeclaredMethod("setIndicator", View.class);
                 setIndicator.invoke(spec, ll);
             }
         } catch (NoSuchMethodException e) {
             doBasicTabs = true;
         } catch (IllegalAccessException e) {
             // Not expected
             Log.e(TAG, "Unexpected: " + e);
             doBasicTabs = true;
         } catch (InvocationTargetException e) {
             // Not expected
             Log.e(TAG, "Unexpected: " + e);
             doBasicTabs = true;
         }
 
         if (doBasicTabs) spec.setIndicator(label, iconSet);
     }
 
     private LinearLayout createTabSpecIndicator(String labelText, Drawable providerIconSet) {
         LinearLayout ll = new LinearLayout(getActivity());
         ll.setOrientation(LinearLayout.VERTICAL);
 
         /* Icon */
         ImageView icon = new ImageView(getActivity());
         icon.setImageDrawable(providerIconSet);
         icon.setPadding(
                 AndroidUtils.scaleDipToPixels(10),
                 AndroidUtils.scaleDipToPixels(10),
                 AndroidUtils.scaleDipToPixels(10),
                 AndroidUtils.scaleDipToPixels(3)
         );
         ll.addView(icon);
 
         /* Background */
         int selectedColor = android.R.color.transparent;
         int unselectedColor = android.R.color.darker_gray;
         StateListDrawable tabBackground = new StateListDrawable();
         tabBackground.addState(new int[]{android.R.attr.state_selected},
                 getResources().getDrawable(selectedColor));
         tabBackground.addState(new int[]{}, getResources().getDrawable(unselectedColor));
         ll.setBackgroundDrawable(tabBackground);
 
         /* Label */
         TextView label = new TextView(getActivity());
         label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
         label.setText(labelText);
         label.setGravity(Gravity.CENTER);
         label.setPadding(
                 AndroidUtils.scaleDipToPixels(0),
                 AndroidUtils.scaleDipToPixels(0),
                 AndroidUtils.scaleDipToPixels(0),
                 AndroidUtils.scaleDipToPixels(4)
         );
         ll.addView(label);
 
         return ll;
     }
 
     private void configureViewElementsBasedOnProvider() {
         JRDictionary socialSharingProperties = mSelectedProvider.getSocialSharingProperties();
 
         if (socialSharingProperties.getAsBoolean("content_replaces_action"))
             updatePreviewTextWhenContentReplacesAction();
         else
             updatePreviewTextWhenContentDoesNotReplaceAction();
 
         if (isPublishThunk()) {
             mMaxCharacters = socialSharingProperties
                     .getAsDictionary("set_status_properties").getAsInt("max_characters");
         } else {
             mMaxCharacters = socialSharingProperties.getAsInt("max_characters");
         }
 
         showHideView(mCharacterCountView, mMaxCharacters != -1);
 
         updateCharacterCount();
 
         boolean can_share_media = socialSharingProperties.getAsBoolean("can_share_media");
 
         /* Switch on or off the media content view based on the presence of media and ability to
          * display it */
         showHideView(mMediaContentView, mActivityObject.getMedia().size() > 0 && can_share_media);
 
         /* Switch on or off the action label view based on the provider accepting an action */
 
         int colorWithAlpha = mSelectedProvider.getProviderColor(true);
         int colorNoAlpha = mSelectedProvider.getProviderColor(false);
 
         mUserProfileInformationAndShareButtonContainer.setBackgroundColor(colorWithAlpha);
 
         mShareButton.setColor(colorNoAlpha);
         mConnectAndShareButton.setColor(colorNoAlpha);
         mPreviewBorder.getBackground().setColorFilter(colorNoAlpha, PorterDuff.Mode.SRC_ATOP);
 
         mProviderIcon.setImageDrawable(mSelectedProvider.getProviderIcon(getActivity()));
     }
 
     public void onTabChanged(String tabTag) {
         if (Config.LOGD) Log.d(TAG, "[onTabChange]: " + tabTag);
 
         if (!tabTag.equals(EMAIL_SMS_TAB_TAG)) {
             /* ... a "real" provider -- Facebook, Twitter, etc. */
             mSelectedProvider = mSession.getProviderByName(tabTag);
 
             configureViewElementsBasedOnProvider();
             configureLoggedInUserBasedOnProvider();
             showActivityAsShared(mProvidersThatHaveAlreadyShared.get(mSelectedProvider.getName()));
 
             mProviderIcon.setImageDrawable(mSelectedProvider.getProviderIcon(getActivity()));
         }
 
         mSelectedTab = tabTag;
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
 
         if (AndroidUtils.isSmallNormalOrLargeScreen()) {
             if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                 mPreviewBox.setVisibility(View.GONE);
                 mEmailSmsComment.setLines(3);
                 mEmailSmsButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
             } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                 mPreviewBox.setVisibility(View.VISIBLE);
                 mEmailSmsComment.setLines(4);
                 //mEmailSmsButtonContainer.setOrientation(LinearLayout.VERTICAL);
                 mEmailSmsButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
             }
         } else {
             showHideView(mPreviewBox, true);
             mEmailSmsComment.setLines(4);
         }
     }
 
     @Override
     public void onDestroy() {
         if (mSession != null && mSessionDelegate != null) {
             mSession.removeDelegate(mSessionDelegate);
         }
 
         super.onDestroy();
     }
 
     private View.OnClickListener mShareButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
             //mWeAreCurrentlyPostingSomething = true;
 
             if (mAuthenticatedUser == null) {
                 authenticateUserForSharing();
             } else {
                 shareActivity();
             }
         }
     };
 
     private View.OnClickListener mSignoutButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
             Bundle options = new Bundle();
             options.putString(KEY_DIALOG_PROVIDER_NAME, mSelectedProvider.getFriendlyName());
             showDialog(DIALOG_CONFIRM_SIGNOUT, options);
         }
     };
 
     private TextWatcher mUserCommentTextWatcher = new TextWatcher() {
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
 
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
 
         public void afterTextChanged(Editable editable) {
             mActivityObject.setUserGeneratedContent(mUserCommentView.getText().toString());
             if (!mEmailSmsComment.getText().toString().equals(editable.toString())) {
                 mEmailSmsComment.setText(editable.toString());
             }
 
             if (mSelectedProvider == null) return;
             if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
                 /* Twitter, MySpace, LinkedIn */
                 updatePreviewTextWhenContentReplacesAction();
             } /* ... else Yahoo or Facebook */
 
             updateCharacterCount();
 
             for (String k : mProvidersThatHaveAlreadyShared.keySet()) {
                 mProvidersThatHaveAlreadyShared.put(k, false);
             }
 
             showActivityAsShared(false);
         }
     };
 
     private TextWatcher mEmailSmsCommentTextWatcher = new TextWatcher() {
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
         public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
         public void afterTextChanged(Editable s) {
             if (!mUserCommentView.getText().toString().equals(s.toString())) {
                 mUserCommentView.setText(s.toString());
             }
         }
     };
 
     private Intent createEmailIntent(String subject, String body) {
         Intent intent = new Intent(android.content.Intent.ACTION_SEND);
 
         /* XXX hack
          * By setting this MIME type we cajole the right behavior out of the platform.  This
          * MIME type is not valid (normally it would be text/plain) but the email apps respond
          * to ACTION_SEND type so it works.
          * The reason that using ACTION_SENDTO with a URI with scheme mailto: does not work is
          * that the "Email" app fills the To: field with a single comma.
          * (Because it's expecting an actual email address in the URI, but we're not
          * supplying one, we're supplying only a scheme.) */
         intent.setType("plain/text");
         //intent.setData(Uri.parse("mailto:"));
         intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
         intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
 
         return intent;
     }
 
     private View.OnClickListener mEmailButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             JREmailObject jrEmail = mActivityObject.getEmail();
             String body, subject;
 
             if (jrEmail == null) {
                 body = mUserCommentView.getText().toString();
                 subject = getString(R.string.jr_default_email_share_subject);
             } else {
                 body =  jrEmail.getBody() + "\n\n" + mUserCommentView.getText().toString();
                 subject = TextUtils.isEmpty(jrEmail.getSubject()) ?
                         getString(R.string.jr_default_email_share_subject)
                         : jrEmail.getSubject();
             }
 
             Intent intent = createEmailIntent(subject, body);
 
             try {
                 //Intent chooser = Intent.createChooser(intent, getString(R.string.jr_choose_email_handler));
                 startActivityForResult(intent, 0);
                 mSession.notifyEmailSmsShare("email");
             } catch (ActivityNotFoundException exception) {
                 showDialog(DIALOG_NO_EMAIL_CLIENT);
             }
         }
     };
 
     private View.OnClickListener mSmsButtonListener = new View.OnClickListener() {
         public void onClick(View v) {
             JRSmsObject jrSms = mActivityObject.getSms();
             String body;
 
             if (jrSms == null) {
                 body = mUserCommentView.getText().toString();
             } else {
                 body = mUserCommentView.getText().toString() + "\n" + jrSms.getBody();
             }
 
             Intent intent = createSmsIntent(body);
             try {
                 startActivityForResult(intent, 0);
                 mSession.notifyEmailSmsShare("sms");
             } catch (ActivityNotFoundException exception) {
                 showDialog(DIALOG_NO_SMS_CLIENT);
             }
         }
     };
 
     private Intent createSmsIntent(String body) {
         /* Google Voice does not respect passing the body, so this Intent is constructed
          * specifically to be responded to only by Mms (the platform messaging app).
          * http://stackoverflow.com/questions/4646508/how-to-pass-text-to-google-voice-sms-programmatically */
 
         //intent = new Intent(android.content.Intent.ACTION_SEND);
         Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
         intent.setType("vnd.android-dir/mms-sms");
         //intent.setData(Uri.parse("smsto:"));
         //intent.setData(Uri.parse("sms:"));
         //intent.putExtra(android.content.Intent.EXTRA_TEXT, body.substring(0,130));
         intent.putExtra("sms_body", body.substring(0, Math.min(139, body.length())));
         intent.putExtra("exit_on_sent", true);
 
         return intent;
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle options) {
         // TODO: make resources out of these strings
 
         switch (id) {
             case DIALOG_FAILURE:
                 return new AlertDialog.Builder(getActivity())
                         .setMessage(options.getString(KEY_DIALOG_ERROR_MESSAGE))
                         .setPositiveButton("Dismiss", null)
                         .create();
             case DIALOG_CONFIRM_SIGNOUT:
                 return new AlertDialog.Builder(getActivity())
                         .setMessage(getString(R.string.jr_sign_out_dialog)
                                 + options.getString(KEY_DIALOG_PROVIDER_NAME) + "?")
                         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 signOutButtonHandler();
                             }
                         })
                         .setNegativeButton("Cancel", null)
                         .create();
             case DIALOG_MOBILE_CONFIG_LOADING:
                 ProgressDialog pd = new ProgressDialog(getActivity());
                 pd.setCancelable(false);
                 pd.setTitle("");
                 pd.setMessage("Loading configuration data.\nPlease wait...");
                 pd.setIndeterminate(true);
                 return pd;
             case DIALOG_NO_EMAIL_CLIENT:
                 return new AlertDialog.Builder(getActivity())
                         .setMessage("You do not have an email client configured.")
                         .setPositiveButton("OK", null)
                         .create();
             case DIALOG_NO_SMS_CLIENT:
                 return new AlertDialog.Builder(getActivity())
                         .setMessage("You do not have an sms client configured.")
                         .setPositiveButton("OK", null)
                         .create();
         }
 
         return super.onCreateDialog(id, options);
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog d, Bundle options) {
         switch (id) {
             case DIALOG_FAILURE:
                 ((AlertDialog) d).setMessage(options.getString(KEY_DIALOG_ERROR_MESSAGE));
                 return;
             case DIALOG_CONFIRM_SIGNOUT:
                 ((AlertDialog) d).setMessage("Sign out of " + mSelectedProvider.getFriendlyName() + "?");
                 return;
         }
 
         super.onPrepareDialog(id, d, options);
     }
 
     private void signOutButtonHandler() {
         String provider = mSelectedProvider.getName();
         mSession.forgetAuthenticatedUserForProvider(provider);
     }
 
     private void onUserSignOut() {
         showUserAsLoggedIn(false);
         mAuthenticatedUser = null;
         mProvidersThatHaveAlreadyShared.put(mSelectedProvider.getName(), false);
         onTabChanged(mTabHost.getCurrentTabTag());
     }
 
     /* UI property updaters */
 
     public void updateCharacterCount() {
         // TODO: verify correctness of the 0 remaining characters edge case
         CharSequence characterCountText;
 
         if (mSelectedProvider.getSocialSharingProperties()
                 .getAsBoolean("content_replaces_action")) {
             /* Twitter, MySpace, LinkedIn */
             if (doesActivityUrlAffectCharacterCountForSelectedProvider()
                     && mShortenedActivityURL == null) {
                 /* Twitter, MySpace */
                 characterCountText = getText(R.string.jr_calculating_remaining_characters);
             } else {
                 int preview_length = mPreviewLabelView.getText().length();
                 int chars_remaining = mMaxCharacters - preview_length;
                 if (chars_remaining < 0)
                     characterCountText = Html.fromHtml("Remaining characters: <font color=red>"
                             + chars_remaining + "</font>");
                 else
                     characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
             }
         } else { /* Facebook, Yahoo */
             int comment_length = mUserCommentView.getText().length();
             int chars_remaining = mMaxCharacters - comment_length;
             if (chars_remaining < 0)
                 characterCountText = Html.fromHtml(
                         "Remaining characters: <font color=red>" + chars_remaining + "</font>");
             else
                 characterCountText = Html.fromHtml("Remaining characters: " + chars_remaining);
         }
 
         mCharacterCountView.setText(characterCountText);
         if (Config.LOGD) Log.d(TAG, "updateCharacterCount: " + characterCountText);
     }
 
     private void updatePreviewTextWhenContentReplacesAction() {
         String newText;
         if (mUserCommentView.getText().toString().equals("")) {
             newText = mActivityObject.getAction();
         } else {
             newText = mUserCommentView.getText().toString();
         }
 
         String userNameForPreview = getUserDisplayName();
 
         String shorteningText = getString(R.string.jr_shortening_url);
 
         if (doesActivityUrlAffectCharacterCountForSelectedProvider()) { /* Twitter/MySpace -> true */
             mPreviewLabelView.setText(Html.fromHtml(
                     "<b>" + userNameForPreview + "</b> " + newText + " <font color=\"#808080\">" +
                     ((mShortenedActivityURL != null) ? mShortenedActivityURL : shorteningText) +
                     "</font>"));
         } else {
             mPreviewLabelView.setText(Html.fromHtml("<b> " + userNameForPreview + "</b> " + newText));
         }
     }
 
     private void updatePreviewTextWhenContentDoesNotReplaceAction() {
         mPreviewLabelView.setText(Html.fromHtml("<b>" + getUserDisplayName() + "</b> "
                 + mActivityObject.getAction()));
     }
 
     private String getUserDisplayName() {
         String userNameForPreview = "You";
         if (mAuthenticatedUser != null) userNameForPreview = mAuthenticatedUser.getPreferredUsername();
         return userNameForPreview;
     }
 
     private void loadUserNameAndProfilePicForUserForProvider(
             JRAuthenticatedUser user,
             final String providerName) {
         if (Config.LOGD) Log.d(TAG, "loadUserNameAndProfilePicForUserForProvider");
 
         if (user == null || providerName == null) {
             mUserNameView.setText("");
             mUserProfilePic.setImageResource(R.drawable.jr_profilepic_placeholder);
             return;
         }
 
         mUserNameView.setText(user.getPreferredUsername());
 
         mUserProfilePic.setImageResource(R.drawable.jr_profilepic_placeholder);
         user.downloadProfilePic(new JRAuthenticatedUser.ProfilePicAvailableListener() {
             public void onProfilePicAvailable(Bitmap b) {
                 if (mSelectedProvider.getName().equals(providerName))
                     mUserProfilePic.setImageBitmap(b);
             }
         });
     }
 
     private void showActivityAsShared(boolean shared) {
         if (Config.LOGD) Log.d(TAG, "[showActivityAsShared]: " + shared);
 
         int visibleIfShared = shared ? View.VISIBLE : View.GONE;
         int visibleIfNotShared = !shared ? View.VISIBLE : View.GONE;
 
         mSharedTextAndCheckMarkContainer.setVisibility(visibleIfShared);
 
         if (mAuthenticatedUser != null) {
             mShareButton.setVisibility(visibleIfNotShared);
         } else {
             mConnectAndShareButton.setVisibility(visibleIfNotShared);
         }
     }
 
     private void showUserAsLoggedIn(boolean loggedIn) {
         if (Config.LOGD) Log.d(TAG, "[showUserAsLoggedIn]: " + loggedIn);
 
         int visibleIfLoggedIn = loggedIn ? View.VISIBLE : View.INVISIBLE;
         int visibleIfNotLoggedIn = !loggedIn ? View.VISIBLE : View.INVISIBLE;
 
         mShareButton.setVisibility(visibleIfLoggedIn);
         mUserProfileContainer.setVisibility(visibleIfLoggedIn);
 
         mConnectAndShareButton.setVisibility(visibleIfNotLoggedIn);
 
         if (mSelectedProvider.getSocialSharingProperties().getAsBoolean("content_replaces_action")) {
             updatePreviewTextWhenContentReplacesAction();
         } else {
             updatePreviewTextWhenContentDoesNotReplaceAction();
         }
     }
 
     private void configureLoggedInUserBasedOnProvider() {
         mAuthenticatedUser = mSession.getAuthenticatedUserForProvider(mSelectedProvider);
 
         loadUserNameAndProfilePicForUserForProvider(mAuthenticatedUser, mSelectedProvider.getName());
 
         showUserAsLoggedIn(mAuthenticatedUser != null);
     }
 
     /* UI state updaters */
 
     private void authenticateUserForSharing() {
          /* Set weHaveJustAuthenticated to true, so that when this view returns (for whatever reason...
           * successful auth user canceled, etc), the view will know that we just went through the
           * authentication process. */
         mWeHaveJustAuthenticated = true;
         mAuthenticatingForShare = true;
         mSession.setCurrentlyAuthenticatingProvider(mSelectedProvider);
 
          /* If the selected provider requires input from the user, go to the user landing view. Or if
           * the user started on the user landing page, went back to the list of providers, then selected
           * the same provider as their last-used provider, go back to the user landing view. */
         if (mSelectedProvider.requiresInput()) {
             showUserLanding();
             /* XXX this doesn't pass in the social sharing sign-in mode flag, so flag consequent behavior
              * will be broken.  However, this code path is not used, since no sharing providers "require
              * input".
              */
         } else { /* Otherwise, go straight to the web view. */
             showWebView(true);
         }
     }
 
     private void shareActivity() {
         if (Config.LOGD) Log.d(TAG, "shareActivity mAuthenticatedUser: " + mAuthenticatedUser.toString());
         showProgressDialog();
 
         if (isPublishThunk()) {
             mSession.setStatusForUser(mAuthenticatedUser);
         } else {
             mSession.shareActivityForUser(mAuthenticatedUser);
         }
     }
 
     /* Helper functions */
 
     private boolean isPublishThunk() {
         return mActivityObject.getUrl().equals("") &&
                 mSelectedProvider.getSocialSharingProperties().getAsBoolean("uses_set_status_if_no_url");
     }
 
     public boolean doesActivityUrlAffectCharacterCountForSelectedProvider() {
         boolean url_reduces_max_chars = mSelectedProvider.getSocialSharingProperties()
                 .getAsBoolean("url_reduces_max_chars");
         boolean shows_url_as_url = mSelectedProvider.getSocialSharingProperties()
                 .getAsString("shows_url_as").equals("url");
 
         /* Twitter/MySpace -> true */
         return (url_reduces_max_chars && shows_url_as_url);
     }
 
     /* JRSessionDelegate definition */
     private JRSessionDelegate mSessionDelegate = new JRSessionDelegate.SimpleJRSessionDelegate() {
         @Override
         public void authenticationDidRestart() {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidRestart]");
 
             //mWeAreCurrentlyPostingSomething = false;
             mWeHaveJustAuthenticated = false;
             mAuthenticatingForShare = false;
         }
 
         @Override
         public void authenticationDidFail(JREngageError error, String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidFail]");
             /* This code path is followed if the mobile endpoint URL fails to be read without error,
              * or if Engage completes with an error (like rpxstaging via Facebook) or (maybe?) if
              * a provider is down. */
 
             mWeHaveJustAuthenticated = false;
             mAuthenticatingForShare = false;
 
             /* This UI doesn't need to show a dialog because the JRWebView has already shown one. */
         }
 
         @Override
         public void authenticationDidComplete(JRDictionary profile, String provider) {
             if (Config.LOGD) Log.d(TAG, "[authenticationDidComplete]");
 
             if (provider.equals(mSelectedProvider.getName())) {
                 JRAuthenticatedUser oldUser = mAuthenticatedUser;
                 mAuthenticatedUser = mSession.getAuthenticatedUserForProvider(mSelectedProvider);
 
                 if (oldUser != null && oldUser.getIdentifier().equals(mAuthenticatedUser.getIdentifier())) {
                     // leave the UI the same
                 } else {
                     // refresh the UI
                     mProvidersThatHaveAlreadyShared.put(mSelectedProvider.getName(), false);
                     onTabChanged(mTabHost.getCurrentTabTag());
                     loadUserNameAndProfilePicForUserForProvider(mAuthenticatedUser, provider);
                     showUserAsLoggedIn(true);
                 }
 
                 if (mAuthenticatingForShare) {
                     mAuthenticatingForShare = false;
                     shareActivity();
                 }
             }
         }
 
         @Override
         public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {
             if (Config.LOGD) Log.d(TAG, "[publishingJRActivityDidSucceed]");
 
             mProvidersThatHaveAlreadyShared.put(provider, true);
 
             dismissProgressDialog();
             showActivityAsShared(true);
             Prefs.remove(Prefs.KEY_JR_USER_COMMENT);
 
             //mWeAreCurrentlyPostingSomething = false;
             mWeHaveJustAuthenticated = false;
         }
 
         @Override
         public void publishingJRActivityDidFail(JRActivityObject activity,
                                                 JREngageError error,
                                                 String provider) {
             if (Config.LOGD) Log.d(TAG, "[publishingJRActivityDidFail]");
             boolean reauthenticate = false;
             String dialogErrorMessage;
 
             dismissProgressDialog();
 
             switch (error.getCode()) {
                 case JREngageError.SocialPublishingError.FAILED:
                     //mDialogErrorMessage = "There was an error while sharing this activity.";
                     dialogErrorMessage = error.getMessage();
                     break;
                 case JREngageError.SocialPublishingError.DUPLICATE_TWITTER:
                     dialogErrorMessage = getString(R.string.jr_error_twitter_no_duplicates_allowed);
                     break;
                 case JREngageError.SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED:
                     dialogErrorMessage = getString(R.string.jr_error_linkedin_too_long);
                     break;
                 case JREngageError.SocialPublishingError.MISSING_API_KEY:
                     // Fall through
                 case JREngageError.SocialPublishingError.INVALID_OAUTH_TOKEN:
                     dialogErrorMessage = getString(R.string.jr_error_generic_sharing);
                     reauthenticate = true;
                     break;
                 default:
                     dialogErrorMessage = error.getMessage();
                     break;
             }
 
             /* OK, if this gets called right after authentication succeeds, then the navigation
              * controller won't be done animating back to this view.  If this view isn't loaded
              * yet, and we call shareButtonPressed, then the library will end up trying to push the
              * webview controller onto the navigation controller while the navigation controller
              * is still trying to pop the webview.  This creates craziness, hence we check for
              * [self isViewLoaded]. Also, this prevents an infinite loop of reauthing-failed
              * publishing-reauthing-failed publishing. So, only try and reauthenticate is the
              * publishing activity view is already loaded, which will only happen if we didn't
              * JUST try and authorize, or if sharing took longer than the time it takes to pop the
              * view controller. */
             if (reauthenticate && !mWeHaveJustAuthenticated) {
                 if (Config.LOGD) Log.d(TAG, "reauthenticating user for sharing");
                 mSession.forgetAuthenticatedUserForProvider(provider);
                 authenticateUserForSharing();
 
                 return;
             }
 
             //mWeAreCurrentlyPostingSomething = false;
             mWeHaveJustAuthenticated = false;
 
             Bundle options = new Bundle();
             options.putString(KEY_DIALOG_ERROR_MESSAGE, dialogErrorMessage);
             showDialog(DIALOG_FAILURE, options);
         }
 
         @Override
         public void mobileConfigDidFinish() {
             if (mWaitingForMobileConfig) {
                 dismissDialog(DIALOG_MOBILE_CONFIG_LOADING);
                 mWaitingForMobileConfig = false;
                 mSocialProviders = mSession.getSocialProviders();
                 initializeWithProviderConfiguration();
             }
         }
 
         @Override
         public void userWasSignedOut(String provider) {
             if (provider.equals(mSelectedProvider.getName())) onUserSignOut();
         }
     };
 
     @Override
     protected void onBackPressed() {
         mSession.triggerPublishingDidComplete();
         getActivity().setResult(Activity.RESULT_OK);
         getActivity().finish();
     }
 }
