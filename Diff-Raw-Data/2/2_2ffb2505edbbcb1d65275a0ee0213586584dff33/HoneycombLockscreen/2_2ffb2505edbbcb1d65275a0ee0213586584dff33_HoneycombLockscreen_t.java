 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.internal.policy.impl;
 
 import com.android.internal.R;
 import com.android.internal.telephony.IccCard;
 import com.android.internal.telephony.TelephonyProperties;
 import com.android.internal.widget.DigitalClock;
 import com.android.internal.widget.FuzzyClock;
 import com.android.internal.widget.KanjiClock;
 import com.android.internal.widget.LockPatternUtils;
 import com.android.internal.widget.UnlockRing;
 import com.android.internal.widget.CircularSelector;
 import com.android.internal.widget.SenseLikeLock;
 
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.res.ColorStateList;
 import android.database.Cursor;
 import android.net.Uri;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.ColorFilter;
 import android.graphics.PixelFormat;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.media.AudioManager;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.os.SystemClock;
 import android.os.SystemProperties;
 import android.os.Vibrator;
 import android.preference.MultiSelectListPreference;
 import android.provider.Settings;
 import android.provider.CmSystem.LockscreenStyle;
 import android.provider.CallLog.Calls;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.PhoneLookup;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.content.ComponentName;
 import android.telephony.TelephonyManager;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 
 /**
  * The screen within {@link LockPatternKeyguardView} that shows general
  * information about the device depending on its state, and how to get past it,
  * as applicable.
  */
 class HoneycombLockscreen extends LinearLayout implements KeyguardScreen,
         KeyguardUpdateMonitor.InfoCallback, KeyguardUpdateMonitor.SimStateCallback,
         UnlockRing.OnTriggerListener, CircularSelector.OnCircularSelectorTriggerListener,
         SenseLikeLock.OnSenseLikeSelectorTriggerListener {
 
     private static final boolean DBG = false;
 
     private static final String TAG = "Honeycomb";
 
     private static final String ENABLE_MENU_KEY_FILE = "/data/local/enable_menu_key";
     private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
 
     private static final String SMS_CHANGED = "android.provider.Telephony.SMS_RECEIVED";
 
     static final int CARRIER_TYPE_DEFAULT = 0;
     static final int CARRIER_TYPE_SPN = 1;
     static final int CARRIER_TYPE_PLMN = 2;
     static final int CARRIER_TYPE_CUSTOM = 3;
 
     private Status mStatus = Status.Normal;
 
     private LockPatternUtils mLockPatternUtils;
     private KeyguardUpdateMonitor mUpdateMonitor;
     private KeyguardScreenCallback mCallback;
 
     private TextView mCarrier;
     private TextView mCusText;
     private DigitalClock mClock;
     private FuzzyClock mFuzzyClock;
     private KanjiClock mKanjiClock;
     private UnlockRing mSelector;
     private CircularSelector mCircularSelector;
     private SenseLikeLock mSenseRingSelector;
     private TextView mDate;
     private TextView mTime;
     private TextView mAmPm;
     private LinearLayout mStatusBox;
     private TextView mStatusCharging;
     private TextView mStatusAlarm;
     private TextView mStatusCalendar;
     private TextView mScreenLocked;
     private TextView mEmergencyCallText;
     private TextView mSmsCountView;
     private TextView mMissedCallCountView;
     private Button mEmergencyCallButton;
     private ImageButton mPlayIcon;
     private ImageButton mPauseIcon;
     private ImageButton mRewindIcon;
     private ImageButton mForwardIcon;
     private ImageButton mAlbumArt;
     private AudioManager am = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
     private boolean mWasMusicActive = am.isMusicActive();
     private boolean mIsMusicActive = false;
 
     private TextView mCustomMsg;
     private TextView mNowPlaying;
 
     // current configuration state of keyboard and display
     private int mKeyboardHidden;
     private int mCreationOrientation;
 
     // are we showing battery information?
     private boolean mShowingBatteryInfo = false;
 
     // last known plugged in state
     private boolean mPluggedIn = false;
 
     // last known battery level
     private int mBatteryLevel = 100;
 
     private String mNextAlarm = null;
     private String mNextCalendar = null;
     private String mCharging = null;
     private Drawable mChargingIcon = null;
 
     private boolean mSilentMode;
     private boolean mHideUnlockTab;
     private AudioManager mAudioManager;
     private String mDateFormatString;
     private boolean mEnableMenuKeyInLockScreen;
 
     private static final String TOGGLE_SILENT = "silent_mode";
 
     private String mCustomAppName;
 
     private Bitmap[] mCustomRingAppIcons = new Bitmap[4];
 
     private static final String TOGGLE_FLASHLIGHT = "net.cactii.flash2.TOGGLE_FLASHLIGHT";
 
     private boolean mTrackballUnlockScreen = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.TRACKBALL_UNLOCK_SCREEN, 0) == 1);
 
     private boolean mMenuUnlockScreen = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.MENU_UNLOCK_SCREEN, 0) == 1);
 
     private boolean mLockAlwaysBattery = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_ALWAYS_BATTERY, 0) == 1);
 
     private int mClockColor = (Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUS_BAR_LOCKSCREENCOLOR, 0xFF33B5E5)); // this value for color
 
     private int mCarrierColor = (Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUS_BAR_CARRIERCOLOR, 0xFF33B5E5)); // this value for color
 
     private boolean mLockCalendarAlarm = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_CALENDAR_ALARM, 0) == 1);
 
     private String[] mCalendars = MultiSelectListPreference.parseStoredValue(Settings.System.getString(
             mContext.getContentResolver(), Settings.System.LOCKSCREEN_CALENDARS));
 
     private boolean mLockCalendarRemindersOnly = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_CALENDAR_REMINDERS_ONLY, 0) == 1);
 
     private long mLockCalendarLookahead = Settings.System.getLong(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_CALENDAR_LOOKAHEAD, 10800000);
 
     private boolean mLockMusicControls = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_MUSIC_CONTROLS, 0) == 1);
 
     private boolean mNowPlayingToggle = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_NOW_PLAYING, 1) == 1);
 
     private boolean mAlbumArtToggle = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_ALBUM_ART, 1) == 1);
 
     private int mLockMusicHeadset = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_MUSIC_CONTROLS_HEADSET, 0));
 
     private boolean useLockMusicHeadsetWired = ((mLockMusicHeadset == 1) || (mLockMusicHeadset == 3));
     private boolean useLockMusicHeadsetBT = ((mLockMusicHeadset == 2) || (mLockMusicHeadset == 3));
 
     private boolean mLockAlwaysMusic = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_ALWAYS_MUSIC_CONTROLS, 0) == 1);
 
     private int mWidgetLayout = Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_WIDGETS_LAYOUT, 0);
 
     private int mCarrierLabelType = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.CARRIER_LABEL_LOCKSCREEN_TYPE, CARRIER_TYPE_DEFAULT));
 
     private String mCarrierLabelCustom = (Settings.System.getString(mContext.getContentResolver(),
             Settings.System.CARRIER_LABEL_LOCKSCREEN_CUSTOM_STRING));
 
     private String mCustomText = (Settings.System.getString(mContext.getContentResolver(),
             Settings.System.CUSTOM_TEXT_STRING));
 
     private boolean mCustomAppToggle = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, 0) == 1);
 
     private boolean mUseFuzzyClock = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_FUZZY_CLOCK, 1) == 1);
 
     private boolean mUseKanjiClock = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_FUZZY_CLOCK, 1) == 2) || (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_FUZZY_CLOCK, 1) == 3);
 
     private boolean mLockMessage = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_MESSAGE, 1) != 1);
 
     private int mLockscreenStyle = (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.LOCKSCREEN_STYLE_PREF, 6));
 
     private boolean mUseCircularLockscreen =
         LockscreenStyle.getStyleById(mLockscreenStyle) == LockscreenStyle.Circular;
 
     private boolean mUseSenseLockscreen =
         LockscreenStyle.getStyleById(mLockscreenStyle) == LockscreenStyle.Sense;
 
     private boolean mUseHoneyLockscreen =
         LockscreenStyle.getStyleById(mLockscreenStyle) == LockscreenStyle.Honeycomb;
 
     private String[] mCustomRingAppActivities = new String[] {
             Settings.System.getString(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[0]),
             Settings.System.getString(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[1]),
             Settings.System.getString(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[2]),
             Settings.System.getString(mContext.getContentResolver(),
                     Settings.System.LOCKSCREEN_CUSTOM_RING_APP_ACTIVITIES[3])
     };
 
     private int smsCount = 0;
     private int callCount = 0;
     private long messagesId = 0;
     private IntentFilter filter;
     private Handler mSmsCallHandler;
 
     private Intent[] mCustomApps = new Intent[4];
 
     /**
      * The status of this lock screen.
      */
     enum Status {
         /**
          * Normal case (sim card present, it's not locked)
          */
         Normal(true),
 
         /**
          * The sim card is 'network locked'.
          */
         NetworkLocked(true),
 
         /**
          * The sim card is missing.
          */
         SimMissing(false),
 
         /**
          * The sim card is missing, and this is the device isn't provisioned, so we don't let
          * them get past the screen.
          */
         SimMissingLocked(false),
 
         /**
          * The sim card is PUK locked, meaning they've entered the wrong sim unlock code too many
          * times.
          */
         SimPukLocked(false),
 
         /**
          * The sim card is locked.
          */
         SimLocked(true);
 
         private final boolean mShowStatusLines;
 
         Status(boolean mShowStatusLines) {
             this.mShowStatusLines = mShowStatusLines;
         }
 
         /**
          * @return Whether the status lines (battery level and / or next alarm) are shown while
          *         in this state.  Mostly dictated by whether this is room for them.
          */
         public boolean showStatusLines() {
             return mShowStatusLines;
         }
     }
 
     /**
      * In general, we enable unlocking the insecure key guard with the menu key. However, there are
      * some cases where we wish to disable it, notably when the menu button placement or technology
      * is prone to false positives.
      *
      * @return true if the menu key should be enabled
      */
     private boolean shouldEnableMenuKey() {
         final Resources res = getResources();
         final boolean configDisabled = res.getBoolean(R.bool.config_disableMenuKeyInLockScreen);
         final boolean isMonkey = SystemProperties.getBoolean("ro.monkey", false);
         final boolean fileOverride = (new File(ENABLE_MENU_KEY_FILE)).exists();
         return !configDisabled || isMonkey || fileOverride;
     }
 
     /**
      * @param context Used to setup the view.
      * @param configuration The current configuration. Used to use when selecting layout, etc.
      * @param lockPatternUtils Used to know the state of the lock pattern settings.
      * @param updateMonitor Used to register for updates on various keyguard related
      *    state, and query the initial state at setup.
      * @param callback Used to communicate back to the host keyguard view.
      */
     HoneycombLockscreen(Context context, Configuration configuration,
             LockPatternUtils lockPatternUtils, KeyguardUpdateMonitor updateMonitor,
             KeyguardScreenCallback callback) {
         super(context);
         mLockPatternUtils = lockPatternUtils;
         mUpdateMonitor = updateMonitor;
         mCallback = callback;
 
         filter = new IntentFilter();
         mSmsCallHandler = new Handler();
 
         int CColours = mClockColor;
 
         mEnableMenuKeyInLockScreen = shouldEnableMenuKey();
 
         mCreationOrientation = configuration.orientation;
 
         mKeyboardHidden = configuration.hardKeyboardHidden;
 
         if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
             Log.v(TAG, "***** CREATING LOCK SCREEN", new RuntimeException());
             Log.v(TAG, "Cur orient=" + mCreationOrientation
                     + " res orient=" + context.getResources().getConfiguration().orientation);
         }
 
         final LayoutInflater inflater = LayoutInflater.from(context);
         if (DBG) Log.v(TAG, "Creation orientation = " + mCreationOrientation);
         if (mUseFuzzyClock && mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE){
             inflater.inflate(R.layout.keyguard_screen_honey_fuzzyclock, this, true);
         } else if (mUseKanjiClock && mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE){
             inflater.inflate(R.layout.keyguard_screen_honey_kanjiclock, this, true);
         } else if (mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE) {
             inflater.inflate(R.layout.keyguard_screen_honey, this, true);
         } else if (mUseFuzzyClock && (mCreationOrientation == Configuration.ORIENTATION_LANDSCAPE)) {
             inflater.inflate(R.layout.keyguard_screen_honey_landscape_fuzzyclock, this, true);
         } else if (mUseKanjiClock && (mCreationOrientation == Configuration.ORIENTATION_LANDSCAPE)) {
             inflater.inflate(R.layout.keyguard_screen_honey_landscape_kanjiclock, this, true);
         } else {
             inflater.inflate(R.layout.keyguard_screen_honey_landscape, this, true);
         }
         ViewGroup lockWallpaper = (ViewGroup) findViewById(R.id.root);
         setBackground(mContext,lockWallpaper);
         mCarrier = (TextView) findViewById(R.id.carrier);
         // Required for Marquee to work
         mCarrier.setSelected(true);
         mCarrier.setTextColor(mCarrierColor);
         if ((Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER, 0) == 1) ||
             (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER_CENTER, 0) == 1) ||
             (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER_LEFT, 0) == 1)) {
         mCarrier.setVisibility(View.INVISIBLE);
         }
 
         Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 1);
 
         mCusText = (TextView) findViewById(R.id.custext);
         mCusText.setText(mCustomText);
         mCusText.setTextColor(CColours);
 
         if (mUseFuzzyClock){
             mFuzzyClock = (FuzzyClock) findViewById(R.id.time);
         } else if (mUseKanjiClock){
             mKanjiClock = (KanjiClock) findViewById(R.id.time);
         } else {
             mClock = (DigitalClock) findViewById(R.id.time);
         }
         mTime = (TextView) findViewById(R.id.timeDisplay);
         mTime.setTextColor(CColours);
         if (mUseKanjiClock) {
         } else {
             mAmPm = (TextView) findViewById(R.id.am_pm);
             mAmPm.setTextColor(CColours);
         }
         mDate = (TextView) findViewById(R.id.date);
         mDate.setTextColor(CColours);
 
         mStatusBox = (LinearLayout) findViewById(R.id.status_box);
         mStatusCharging = (TextView) findViewById(R.id.status_charging);
         mStatusCharging.setTextColor(CColours);
         mStatusAlarm = (TextView) findViewById(R.id.status_alarm);
         mStatusAlarm.setTextColor(CColours);
         mStatusCalendar = (TextView) findViewById(R.id.status_calendar);
         mStatusCalendar.setTextColor(CColours);
 
         mCustomMsg = (TextView) findViewById(R.id.customMsg);
 
         if (mCustomMsg != null) {
             if (mLockPatternUtils.isShowCustomMsg()) {
                 mCustomMsg.setVisibility(View.VISIBLE);
                 mCustomMsg.setText(mLockPatternUtils.getCustomMsg());
                 mCustomMsg.setTextColor(CColours);
             } else {
                 mCustomMsg.setVisibility(View.GONE);
             }
         }
 
         mPlayIcon = (ImageButton) findViewById(R.id.musicControlPlay);
         mPauseIcon = (ImageButton) findViewById(R.id.musicControlPause);
         mRewindIcon = (ImageButton) findViewById(R.id.musicControlPrevious);
         mForwardIcon = (ImageButton) findViewById(R.id.musicControlNext);
         mAlbumArt = (ImageButton) findViewById(R.id.albumArt);
         mNowPlaying = (TextView) findViewById(R.id.musicNowPlaying);
         mNowPlaying.setSelected(true); // set focus to TextView to allow scrolling
         mNowPlaying.setTextColor(CColours);
 
         mScreenLocked = (TextView) findViewById(R.id.screenLocked);
         mScreenLocked.setTextColor(CColours);
 
         mSelector = (UnlockRing) findViewById(R.id.unlock_ring);
         mCircularSelector = (CircularSelector) findViewById(R.id.circular_selector);
         mSenseRingSelector = (SenseLikeLock) findViewById(R.id.sense_selector);
 
         mSenseRingSelector.setOnSenseLikeSelectorTriggerListener(this);
         mCircularSelector.setOnCircularSelectorTriggerListener(this);
         mSelector.setOnTriggerListener(this);
 
         setupSenseLikeRingShortcuts();
 
         mEmergencyCallText = (TextView) findViewById(R.id.emergencyCallText);
         mEmergencyCallButton = (Button) findViewById(R.id.emergencyCallButton);
         mEmergencyCallButton.setText(R.string.lockscreen_emergency_call);
 
         mSmsCountView = (TextView) findViewById(R.id.smssWidget);
         mSmsCountView.setTextColor(CColours);
         mMissedCallCountView = (TextView) findViewById(R.id.callsWidget);
         mMissedCallCountView.setTextColor(CColours);
 
         if (mLockMessage) {
             mSmsCountView.setVisibility(View.INVISIBLE);
             mMissedCallCountView.setVisibility(View.INVISIBLE);
         }
 
         mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
         mEmergencyCallButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mCallback.takeEmergencyCallAction();
             }
         });
 
         mPlayIcon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mCallback.pokeWakelock();
                 refreshMusicStatus();
                 if (!am.isMusicActive()) {
                     mPauseIcon.setVisibility(View.VISIBLE);
                     mPlayIcon.setVisibility(View.GONE);
                     mRewindIcon.setVisibility(View.VISIBLE);
                     mForwardIcon.setVisibility(View.VISIBLE);
                     sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                 }
             }
         });
 
         mPauseIcon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mCallback.pokeWakelock();
                 refreshMusicStatus();
                 if (am.isMusicActive()) {
                     mPlayIcon.setVisibility(View.VISIBLE);
                     mPauseIcon.setVisibility(View.GONE);
                     mRewindIcon.setVisibility(View.GONE);
                     mForwardIcon.setVisibility(View.GONE);
                     sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                 }
             }
         });
 
         mRewindIcon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mCallback.pokeWakelock();
                 sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
             }
         });
 
         mForwardIcon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mCallback.pokeWakelock();
                 sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
             }
         });
 
         mAlbumArt.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent musicIntent = new Intent(Intent.ACTION_VIEW);
                 musicIntent.setClassName("com.android.music","com.android.music.MediaPlaybackActivity");
                 musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 getContext().startActivity(musicIntent);
                 mCallback.goToUnlockScreen();
                     Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
             }
         });
 
         setFocusable(true);
         setFocusableInTouchMode(true);
         setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
 
         mUpdateMonitor.registerInfoCallback(this);
         mUpdateMonitor.registerSimStateCallback(this);
 
         filter.addAction(SMS_CHANGED);
         filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
         getContext().registerReceiver(mSmsCallListener, filter);
         if (!mLockMessage) {
             smsCount = SmsCallWidgetHelper.getUnreadSmsCount(getContext());
             callCount = SmsCallWidgetHelper.getMissedCallCount(getContext());
             setSmsWidget();
             setCallWidget();
         }
 
         mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
         mSilentMode = isSilentMode();
 
         if (mWidgetLayout == 1) {
             setLenseWidgetsVisibility(View.INVISIBLE);
         }
 
         if (mTrackballUnlockScreen || mMenuUnlockScreen) {
             mHideUnlockTab = true;
         } else {
             mHideUnlockTab = false;
         }
 
         resetStatusInfo(updateMonitor);
         switch (mWidgetLayout) {
             case 2:
                 centerWidgets();
                 break;
             case 3:
                 alignWidgetsToRight();
                 break;
         }
     }
 
     private void centerWidgets() {
         RelativeLayout.LayoutParams layoutParams;
         layoutParams = (RelativeLayout.LayoutParams) mCarrier.getLayoutParams();
         layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
         mCarrier.setLayoutParams(layoutParams);
         mCarrier.setGravity(Gravity.CENTER_HORIZONTAL);
 
         mStatusBox.setGravity(Gravity.CENTER_HORIZONTAL);
 
         if (mUseFuzzyClock){
            centerWidget(mFuzzyClock);
         } else if (mUseKanjiClock) {
            centerWidget(mKanjiClock);
         } else {
            centerWidget(mClock);
         }
         centerWidget(mDate);
         centerWidget(mCusText);
         centerWidget(mSmsCountView);
         centerWidget(mMissedCallCountView);
         centerWidget(mStatusCharging);
         centerWidget(mStatusAlarm);
         centerWidget(mStatusCalendar);
     }
 
     private void centerWidget(View view) {
         ViewGroup.LayoutParams params = view.getLayoutParams();
         if (params instanceof RelativeLayout.LayoutParams) {
             RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) params;
             p.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
         } else if (params instanceof LinearLayout.LayoutParams) {
             LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) params;
             p.gravity = Gravity.CENTER_HORIZONTAL;
             p.leftMargin = 0;
             p.rightMargin = 0;
         }
         view.setLayoutParams(params);
     }
 
     private void alignWidgetsToRight() {
         RelativeLayout.LayoutParams layoutParams;
         layoutParams = (RelativeLayout.LayoutParams) mCarrier.getLayoutParams();
         layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
         mCarrier.setLayoutParams(layoutParams);
         mCarrier.setGravity(Gravity.LEFT);
 
         mStatusBox.setGravity(Gravity.LEFT);
 
         if (mUseFuzzyClock){
             alignWidgetToRight(mFuzzyClock);
         } else if (mUseKanjiClock) {
             alignWidgetToRight(mKanjiClock);
         } else {
             alignWidgetToRight(mClock);
         }
         alignWidgetToRight(mDate);
         alignWidgetToRight(mCusText);
         alignWidgetToRight(mSmsCountView);
         alignWidgetToRight(mMissedCallCountView);
         alignWidgetToRight(mStatusCharging);
         alignWidgetToRight(mStatusAlarm);
         alignWidgetToRight(mStatusCalendar);
     }
 
     private void alignWidgetToRight(View view) {
         ViewGroup.LayoutParams params = view.getLayoutParams();
         if (params instanceof RelativeLayout.LayoutParams) {
             RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) params;
             p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
             p.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
         } else if (params instanceof LinearLayout.LayoutParams) {
             LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) params;
             p.gravity = Gravity.RIGHT;
         }
         view.setLayoutParams(params);
     }
 
     static void setBackground(Context bcontext, ViewGroup layout){
         String mLockBack = Settings.System.getString(bcontext.getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND);
         if (mLockBack!=null){
             if (!mLockBack.isEmpty()){
                 try {
                     layout.setBackgroundColor(Integer.parseInt(mLockBack));
                 }catch(NumberFormatException e){
                 }
             }else{
                 String lockWallpaper = "";
                 try {
                     lockWallpaper = bcontext.createPackageContext("com.cyanogenmod.cmparts", 0).getFilesDir()+"/lockwallpaper";
                 } catch (NameNotFoundException e1) {
                 }
                 if (!lockWallpaper.isEmpty()){
                     Bitmap lockb = BitmapFactory.decodeFile(lockWallpaper);
                     layout.setBackgroundDrawable(new BitmapDrawable(lockb));
                 }
             }
         }
     }
 
     static void handleHomeLongPress(Context context) {
         int homeLongAction = (Settings.System.getInt(context.getContentResolver(),
                 Settings.System.LOCKSCREEN_LONG_HOME_ACTION, -1));
         if (homeLongAction == 1) {
            Intent intent = new Intent(HoneycombLockscreen.TOGGLE_FLASHLIGHT);
             intent.putExtra("strobe", false);
             intent.putExtra("period", 0);
             intent.putExtra("bright", false);
             context.sendBroadcast(intent);
         }
     }
 
     private boolean isSilentMode() {
         return mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
     }
 
     private boolean isAirplaneModeOn() {
       return (Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.AIRPLANE_MODE_ON, 0) == 1);
     }
 
     private void updateRightTabResources() {
         boolean vibe = mSilentMode
             && (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
 
     }
 
     private void resetStatusInfo(KeyguardUpdateMonitor updateMonitor) {
         mShowingBatteryInfo = updateMonitor.shouldShowBatteryInfo();
         mPluggedIn = updateMonitor.isDevicePluggedIn();
         mBatteryLevel = updateMonitor.getBatteryLevel();
         mIsMusicActive = am.isMusicActive();
 
         mStatus = getCurrentStatus(updateMonitor.getSimState());
         updateLayout(mStatus);
 
         refreshBatteryStringAndIcon();
         refreshAlarmDisplay();
         refreshCalendarDisplay();
         refreshMusicStatus();
         refreshPlayingTitle();
 
         mDateFormatString = getContext().getString(R.string.full_wday_month_day_no_year);
         refreshTimeAndDateDisplay();
         updateStatusLines();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mTrackballUnlockScreen)
                 || (keyCode == KeyEvent.KEYCODE_MENU && mMenuUnlockScreen)
                 || (keyCode == KeyEvent.KEYCODE_MENU && mEnableMenuKeyInLockScreen)) {
 
             mCallback.goToUnlockScreen();
                     Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
             return false;
         } else if (keyCode == KeyEvent.KEYCODE_HOME) {
             event.startTracking();
             return true;
         }
         return false;
     }
 
     @Override
     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_HOME) {
           handleHomeLongPress(mContext);
         }
         return false;
     }
 
     public void OnCircularSelectorGrabbedStateChanged(View v, int GrabState) {
         // TODO Auto-generated method stub
         mCallback.pokeWakelock();
 
     }
 
     /** {@inheritDoc} */
     public void onTrigger(View v, int whichHandle) {
         final String TOGGLE_SILENT = "silent_mode";
 
         if (whichHandle == UnlockRing.OnTriggerListener.UNLOCK_HANDLE) {
             mCallback.goToUnlockScreen();
             Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
         } else if (mCustomRingAppActivities[0] != null && mCustomAppToggle
                 && whichHandle == UnlockRing.OnTriggerListener.QUADRANT_1) {
             if (mCustomRingAppActivities[0].equals(TOGGLE_SILENT)) {
                 toggleSilentMode();
                 mCallback.pokeWakelock();
                 mSelector.reset(false);
             } else {
                 try {
                     Intent i = Intent.parseUri(mCustomRingAppActivities[0], 0);
                     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                     mContext.startActivity(i);
                     mCallback.goToUnlockScreen();
                         Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
                 } catch (Exception e) {
                 }
             }
         } else if (mCustomRingAppActivities[1] != null && mCustomAppToggle
                 && whichHandle == UnlockRing.OnTriggerListener.QUADRANT_2) {
             if (mCustomRingAppActivities[1].equals(TOGGLE_SILENT)) {
                 toggleSilentMode();
                 mSelector.reset(false);
                 mCallback.pokeWakelock();
             } else {
                 try {
                     Intent i = Intent.parseUri(mCustomRingAppActivities[1], 0);
                     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                     mContext.startActivity(i);
                     mCallback.goToUnlockScreen();
                         Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
                 } catch (Exception e) {
                 }
             }
         } else if (mCustomRingAppActivities[2] != null && mCustomAppToggle
                 && whichHandle == UnlockRing.OnTriggerListener.QUADRANT_3) {
             if (mCustomRingAppActivities[2].equals(TOGGLE_SILENT)) {
                 toggleSilentMode();
                 mSelector.reset(false);
                 mCallback.pokeWakelock();
             } else {
                 try {
                     Intent i = Intent.parseUri(mCustomRingAppActivities[2], 0);
                     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                     mContext.startActivity(i);
                     mCallback.goToUnlockScreen();
                         Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
                 } catch (Exception e) {
                 }
             }
         } else if (mCustomRingAppActivities[3] != null && mCustomAppToggle
                 && whichHandle == UnlockRing.OnTriggerListener.QUADRANT_4) {
             if (mCustomRingAppActivities[3].equals(TOGGLE_SILENT)) {
                 toggleSilentMode();
                 mSelector.reset(false);
                 mCallback.pokeWakelock();
             } else {
                 try {
                     Intent i = Intent.parseUri(mCustomRingAppActivities[3], 0);
                     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                     mContext.startActivity(i);
                     mCallback.goToUnlockScreen();
                         Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
                 } catch (Exception e) {
                 }
             }
         }
     }
 
     public void onCircularSelectorTrigger(View v, int Trigger) {
         mCallback.goToUnlockScreen();
           Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
     }
 
     /** {@inheritDoc} */
     public void onGrabbedStateChange(View v, int grabbedState) {
         if (grabbedState != UnlockRing.OnTriggerListener.NO_HANDLE) {
             mCallback.pokeWakelock();
         }
     }
 
     @Override
     public void OnSenseLikeSelectorGrabbedStateChanged(View v, int GrabState) {
     // TODO Auto-generated method stub
        mCallback.pokeWakelock();
 
     }
 
     @Override
     public void onSenseLikeSelectorTrigger(View v, int Trigger) {
     // TODO Auto-generated method stub  
       Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 100};
 
       switch(Trigger){
      case SenseLikeLock.OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_ONE_TRIGGERED:
        vibe.vibrate(pattern, -1);
        mCustomApps[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getContext().startActivity(mCustomApps[0]);
            mCallback.goToUnlockScreen();
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
        break;
      case SenseLikeLock.OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_TWO_TRIGGERED:
        vibe.vibrate(pattern, -1);
        mCustomApps[1].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getContext().startActivity(mCustomApps[1]);
            mCallback.goToUnlockScreen();
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
        break;
      case SenseLikeLock.OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_THREE_TRIGGERED:
        vibe.vibrate(pattern, -1);
        mCustomApps[2].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getContext().startActivity(mCustomApps[2]);
            mCallback.goToUnlockScreen();
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
        break;
      case SenseLikeLock.OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_FOUR_TRIGGERED:
        vibe.vibrate(pattern, -1);
        mCustomApps[3].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            getContext().startActivity(mCustomApps[3]);
            mCallback.goToUnlockScreen();
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
        break;
      case SenseLikeLock.OnSenseLikeSelectorTriggerListener.LOCK_ICON_TRIGGERED:
        mCallback.goToUnlockScreen();
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
        break;
      }
      
    }
 
    private void setupSenseLikeRingShortcuts(){
 	   
 	   int numapps = 0;
 	   Intent intent = new Intent();
 	   PackageManager pm = mContext.getPackageManager();
 	   mCustomApps = new Intent[4];
 	   
 	   Drawable[] shortcutsicons;
 	   for(int i = 0; i < mCustomRingAppActivities.length ; i++){
 		   if(mCustomRingAppActivities[i] != null && mCustomAppToggle){
 			   numapps++;
 		   }
 		}
 	   
 	   if(numapps != 4){
 		   mCustomApps = mSenseRingSelector.setDefaultIntents();
                    for(int i = 0; i < 4; i++){
                       if(mCustomRingAppActivities[i] != null && mCustomAppToggle){
                           try{
                                  intent = Intent.parseUri(mCustomRingAppActivities[i], 0);	
                              }catch (java.net.URISyntaxException ex) {
                                   // bogus; leave intent=null	
                           }
                       }
                    }
 		   numapps = 4;
 	   }else for(int i = 0; i < numapps ; i++){
 			  
 				try{
 					intent = Intent.parseUri(mCustomRingAppActivities[i], 0);
 				}catch (java.net.URISyntaxException ex) {
 					if (DBG) Log.w(TAG, "Invalid hotseat intent: " + mCustomRingAppActivities[i]);
 		                        ex.printStackTrace();
 		                }
 				
 				 
 				 ResolveInfo bestMatch = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
 		         List<ResolveInfo> allMatches = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
 		         
 		         if (DBG) { 
 		                if (DBG) {
                                    Log.d(TAG, "Best match for intent: " + bestMatch);
 		                   Log.d(TAG, "All matches: ");
                                 }
 		                for (ResolveInfo ri : allMatches) {
 		                    if (DBG)Log.d(TAG, "  --> " + ri);
 		                }
 		            }
 		         
 		         ComponentName com = new ComponentName(
 	                        bestMatch.activityInfo.applicationInfo.packageName,
 	                        bestMatch.activityInfo.name);
 		         
 		         mCustomApps[i] = new Intent(Intent.ACTION_MAIN).setComponent(com);
 
 	   }
 	   
 	   shortcutsicons = new Drawable[numapps];
 	   
 	  float iconScale =0.70f;
 	  
 	   for(int i = 0; i < numapps ; i++){
 		   try {
            	
 			   shortcutsicons[i] = pm.getActivityIcon(mCustomApps[i]);
 			   shortcutsicons[i] = scaledDrawable(shortcutsicons[i], mContext ,iconScale);
            } catch (ArrayIndexOutOfBoundsException ex) {
                if (DBG) Log.w(TAG, "Missing shortcut_icons array item #" + i);
                shortcutsicons[i] = null;
            } catch (PackageManager.NameNotFoundException e) {
                 e.printStackTrace();
                shortcutsicons[i] = null;
            	//Do-Nothing
            }
 	   }
 	   
     	 mSenseRingSelector.setShortCutsDrawables(shortcutsicons[0], shortcutsicons[1], shortcutsicons[2], shortcutsicons[3]);
 	   
    }
    
    private Drawable scaledDrawable(Drawable icon,Context context, float scale) {
 		final Resources resources=context.getResources();
 		int sIconHeight= (int) resources.getDimension(android.R.dimen.app_icon_size);
 		int sIconWidth = sIconHeight;
 
 		int width = sIconWidth;
 		int height = sIconHeight;
 		Bitmap original;
 		try{
 		    original= Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 		} catch (OutOfMemoryError e) {
 		   return icon;
 		}
 		Canvas canvas = new Canvas(original);
 		canvas.setBitmap(original);
 		icon.setBounds(0,0, width, height);
 		icon.draw(canvas);
 		try{
 		    Bitmap endImage=Bitmap.createScaledBitmap(original, (int)(width*scale), (int)(height*scale), true);
 		    original.recycle();
 		    return new FastBitmapDrawable(endImage);
 		} catch (OutOfMemoryError e) {
 		    return icon;
 		}
     }
 
     public class FastBitmapDrawable extends Drawable {
          private Bitmap mBitmap;
          private int mWidth;
          private int mHeight;
 
          public FastBitmapDrawable(Bitmap b) {
              mBitmap = b;
              if (b != null) {
                  mWidth = mBitmap.getWidth();
                  mHeight = mBitmap.getHeight();
              } else {
                  mWidth = mHeight = 0;
              }
          }
 
          @Override
          public void draw(Canvas canvas) {
              canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
          }
 
          @Override
          public int getOpacity() {
              return PixelFormat.TRANSLUCENT;
          }
 
          @Override
          public void setAlpha(int alpha) {
          }
 
          @Override
          public void setColorFilter(ColorFilter cf) {
          }
 
          @Override
          public int getIntrinsicWidth() {
              return mWidth;
          }
 
          @Override
          public int getIntrinsicHeight() {
              return mHeight;
          }
 
          @Override
          public int getMinimumWidth() {
              return mWidth;
          }
 
          @Override
          public int getMinimumHeight() {
              return mHeight;
          }
 
          public void setBitmap(Bitmap b) {
              mBitmap = b;
              if (b != null) {
                  mWidth = mBitmap.getWidth();
                  mHeight = mBitmap.getHeight();
              } else {
                  mWidth = mHeight = 0;
              }
          }
 
          public Bitmap getBitmap() {
              return mBitmap;
          }
      }
 
     /**
      * Displays a message in a text view and then restores the previous text.
      * @param textView The text view.
      * @param text The text.
      * @param color The color to apply to the text, or 0 if the existing color should be used.
      * @param iconResourceId The left hand icon.
      */
     private void toastMessage(final TextView textView, final String text, final int color, final int iconResourceId) {
         if (mPendingR1 != null) {
             textView.removeCallbacks(mPendingR1);
             mPendingR1 = null;
         }
         if (mPendingR2 != null) {
             mPendingR2.run(); // fire immediately, restoring non-toasted appearance
             textView.removeCallbacks(mPendingR2);
             mPendingR2 = null;
         }
 
         final String oldText = textView.getText().toString();
         final ColorStateList oldColors = textView.getTextColors();
 
         mPendingR1 = new Runnable() {
             public void run() {
                 textView.setText(text);
                 if (color != 0) {
                     textView.setTextColor(color);
                 }
                 textView.setCompoundDrawablesWithIntrinsicBounds(iconResourceId, 0, 0, 0);
             }
         };
 
         textView.postDelayed(mPendingR1, 0);
         mPendingR2 = new Runnable() {
             public void run() {
                 textView.setText(oldText);
                 textView.setTextColor(oldColors);
                 textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
             }
         };
         textView.postDelayed(mPendingR2, 3500);
     }
     private Runnable mPendingR1;
     private Runnable mPendingR2;
 
     private void refreshAlarmDisplay() {
         mNextAlarm = mLockPatternUtils.getNextAlarm();
         updateStatusLines();
     }
 
     private void refreshCalendarDisplay() {
         if (mLockCalendarAlarm) {
             mNextCalendar = mLockPatternUtils.getNextCalendarAlarm(mLockCalendarLookahead,
                     mCalendars, mLockCalendarRemindersOnly);
         } else {
             mNextCalendar = null;
         }
         updateStatusLines();
     }
 
     /** {@inheritDoc} */
     public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn,
             int batteryLevel) {
         if (DBG) Log.d(TAG, "onRefreshBatteryInfo(" + showBatteryInfo + ", " + pluggedIn + ")");
         mShowingBatteryInfo = showBatteryInfo;
         mPluggedIn = pluggedIn;
         mBatteryLevel = batteryLevel;
 
         refreshBatteryStringAndIcon();
         updateStatusLines();
     }
 
     private void refreshBatteryStringAndIcon() {
         if (!mShowingBatteryInfo && !mLockAlwaysBattery) {
             mCharging = null;
             return;
         }
 
         if (mPluggedIn) {
             mChargingIcon =
                 getContext().getResources().getDrawable(R.drawable.ic_lock_idle_charging);
             if (mUpdateMonitor.isDeviceCharged()) {
                 mCharging = getContext().getString(R.string.lockscreen_charged, mBatteryLevel);
             } else {
                 mCharging = getContext().getString(R.string.lockscreen_plugged_in, mBatteryLevel);
             }
         } else {
             if (mBatteryLevel <= 20) {
                 mChargingIcon =
                     getContext().getResources().getDrawable(R.drawable.ic_lock_idle_low_battery);
                 mCharging = getContext().getString(R.string.lockscreen_low_battery, mBatteryLevel);
             } else {
                 mChargingIcon =
                     getContext().getResources().getDrawable(R.drawable.ic_lock_idle_discharging);
                 mCharging = getContext().getString(R.string.lockscreen_discharging, mBatteryLevel);
             }
         }
     }
 
     private void refreshMusicStatus() {
         if ((mWasMusicActive || mIsMusicActive || mLockAlwaysMusic
             || (mAudioManager.isWiredHeadsetOn() && useLockMusicHeadsetWired)
             || (mAudioManager.isBluetoothA2dpOn() && useLockMusicHeadsetBT)) && (mLockMusicControls)) {
             if (am.isMusicActive()) {
                 mPauseIcon.setVisibility(View.VISIBLE);
                 mPlayIcon.setVisibility(View.GONE);
                 mRewindIcon.setVisibility(View.VISIBLE);
                 mForwardIcon.setVisibility(View.VISIBLE);
             } else {
                 mPlayIcon.setVisibility(View.VISIBLE);
                 mPauseIcon.setVisibility(View.GONE);
                 mRewindIcon.setVisibility(View.GONE);
                 mForwardIcon.setVisibility(View.GONE);
             }
         } else {
             mPlayIcon.setVisibility(View.GONE);
             mPauseIcon.setVisibility(View.GONE);
             mRewindIcon.setVisibility(View.GONE);
             mForwardIcon.setVisibility(View.GONE);
         }
     }
     private void refreshPlayingTitle() {
         String nowPlaying = KeyguardViewMediator.NowPlaying();
         boolean musicActive = am.isMusicActive() || am.isFmActive();
         mNowPlaying.setText(nowPlaying);
         mNowPlaying.setVisibility(View.GONE);
         mAlbumArt.setVisibility(View.GONE);
 
         if (musicActive && !TextUtils.isEmpty(nowPlaying) && mLockMusicControls) {
             if (mNowPlayingToggle) {
                 mNowPlaying.setVisibility(View.VISIBLE);
                 mNowPlaying.setSelected(true); // set focus to TextView to allow scrolling
             }
             // Set album art
             if (shouldShowAlbumArt()) {
                 Uri uri = getArtworkUri(getContext(), KeyguardViewMediator.SongId(),
                           KeyguardViewMediator.AlbumId());
                 if (uri != null) {
                     mAlbumArt.setImageURI(uri);
                     mAlbumArt.setVisibility(View.VISIBLE);
                 }
             }
         }
     }
 
     private boolean shouldShowAlbumArt() {
         if (!mAlbumArtToggle) {
             return false;
         }
         if (mHideUnlockTab) {
             return false;
         }
         if (mUseSenseLockscreen || mUseHoneyLockscreen || mUseCircularLockscreen) {
                 return false;
         }
         return true;
     }
 
     private void sendMediaButtonEvent(int code) {
         long eventtime = SystemClock.uptimeMillis();
 
         Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
         KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, code, 0);
         downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
         getContext().sendOrderedBroadcast(downIntent, null);
 
         Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
         KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, code, 0);
         upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
         getContext().sendOrderedBroadcast(upIntent, null);
     }
 
     /** {@inheritDoc} */
     public void onTimeChanged() {
         refreshTimeAndDateDisplay();
         mSmsCallHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               if (!mLockMessage) {
               // get a new count and set indicator
                   smsCount = SmsCallWidgetHelper.getUnreadSmsCount(getContext());
                   callCount = SmsCallWidgetHelper.getMissedCallCount(getContext());
                   setCallWidget();
                   setSmsWidget();
               }
            }
         },50);
     }
 
     /** {@inheritDoc} */
     public void onMusicChanged() {
         refreshPlayingTitle();
         mSmsCallHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               if (!mLockMessage) {
               // get a new count and set indicator
                   smsCount = SmsCallWidgetHelper.getUnreadSmsCount(getContext());
                   callCount = SmsCallWidgetHelper.getMissedCallCount(getContext());
                   setCallWidget();
                   setSmsWidget();
               }
            }
         },50);
     }
 
     private void refreshTimeAndDateDisplay() {
         mDate.setText(DateFormat.format(mDateFormatString, new Date()));
     }
 
     private void updateStatusLines() {
         if (!mStatus.showStatusLines() || mWidgetLayout == 1) {
             mStatusBox.setVisibility(INVISIBLE);
         } else {
             mStatusBox.setVisibility(VISIBLE);
 
             if (mCharging != null) {
                 mStatusCharging.setText(mCharging);
                 mStatusCharging.setCompoundDrawablesWithIntrinsicBounds(mChargingIcon, null, null, null);
                 mStatusCharging.setVisibility(VISIBLE);
             } else {
                 mStatusCharging.setVisibility(GONE);
             }
 
             if (mNextAlarm != null) {
                 mStatusAlarm.setText(mNextAlarm);
                 mStatusAlarm.setVisibility(VISIBLE);
             } else {
                 mStatusAlarm.setVisibility(GONE);
             }
 
             if (mNextCalendar != null) {
                 mStatusCalendar.setText(mNextCalendar);
                 mStatusCalendar.setVisibility(VISIBLE);
             } else {
                 mStatusCalendar.setVisibility(GONE);
             }
         }
         mSmsCallHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               if (!mLockMessage) {
               // get a new count and set indicator
                   smsCount = SmsCallWidgetHelper.getUnreadSmsCount(getContext());
                   callCount = SmsCallWidgetHelper.getMissedCallCount(getContext());
                   setCallWidget();
                   setSmsWidget();
               }
            }
         },50);
     }
 
     /** {@inheritDoc} */
     public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {
         if (DBG) Log.d(TAG, "onRefreshCarrierInfo(" + plmn + ", " + spn + ")");
         updateLayout(mStatus);
     }
 
     /**
      * Determine the current status of the lock screen given the sim state and other stuff.
      */
     private Status getCurrentStatus(IccCard.State simState) {
         boolean missingAndNotProvisioned = (!mUpdateMonitor.isDeviceProvisioned()
                 && simState == IccCard.State.ABSENT);
         if (missingAndNotProvisioned) {
             return Status.SimMissingLocked;
         }
 
         boolean presentButNotAvailable = isAirplaneModeOn();
         if (presentButNotAvailable) {
             return Status.Normal;
         }
 
         switch (simState) {
             case ABSENT:
                 return Status.SimMissing;
             case NETWORK_LOCKED:
                 return Status.SimMissingLocked;
             case NOT_READY:
                 return Status.SimMissing;
             case PIN_REQUIRED:
                 return Status.SimLocked;
             case PUK_REQUIRED:
                 return Status.SimPukLocked;
             case READY:
                 return Status.Normal;
             case UNKNOWN:
                 return Status.SimMissing;
         }
         return Status.SimMissing;
     }
 
     /**
      * Update the layout to match the current status.
      */
     private void updateLayout(Status status) {
         // The emergency call button no longer appears on this screen.
         if (DBG) Log.d(TAG, "updateLayout: status=" + status);
 
         mEmergencyCallButton.setVisibility(View.GONE); // in almost all cases
 
         String realPlmn = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
         String plmn = (String) mUpdateMonitor.getTelephonyPlmn();
         String spn = (String) mUpdateMonitor.getTelephonySpn();
 
         switch (status) {
             case Normal:
                 // text
                 if (plmn == null || plmn.equals(realPlmn)) {
                     mCarrier.setText(getCarrierString(
                             plmn, spn, mCarrierLabelType, mCarrierLabelCustom));
                 } else {
                     mCarrier.setText(getCarrierString(plmn, spn));
                 }
 
                 // Empty now, but used for sliding tab feedback
                 mScreenLocked.setText("");
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(true);
                 mEmergencyCallText.setVisibility(View.GONE);
                 break;
             case NetworkLocked:
                 // The carrier string shows both sim card status (i.e. No Sim Card) and
                 // carrier's name and/or "Emergency Calls Only" status
                 mCarrier.setText(
                         getCarrierString(
                                 mUpdateMonitor.getTelephonyPlmn(),
                                 getContext().getText(R.string.lockscreen_network_locked_message)));
                 mScreenLocked.setText(R.string.lockscreen_instructions_when_pattern_disabled);
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(true);
                 mEmergencyCallText.setVisibility(View.GONE);
                 break;
             case SimMissing:
                 // text
                 mCarrier.setText(R.string.lockscreen_missing_sim_message_short);
                 mScreenLocked.setText(R.string.lockscreen_missing_sim_instructions);
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(true);
                 mEmergencyCallText.setVisibility(View.VISIBLE);
                 // do not need to show the e-call button; user may unlock
                 break;
             case SimMissingLocked:
                 // text
                 mCarrier.setText(
                         getCarrierString(
                                 mUpdateMonitor.getTelephonyPlmn(),
                                 getContext().getText(R.string.lockscreen_missing_sim_message_short)));
                 mScreenLocked.setText(R.string.lockscreen_missing_sim_instructions);
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(false);
                 mEmergencyCallText.setVisibility(View.VISIBLE);
                 mEmergencyCallButton.setVisibility(View.VISIBLE);
                 break;
             case SimLocked:
                 // text
                 mCarrier.setText(
                         getCarrierString(
                                 mUpdateMonitor.getTelephonyPlmn(),
                                 getContext().getText(R.string.lockscreen_sim_locked_message)));
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(true);
                 mEmergencyCallText.setVisibility(View.GONE);
                 break;
             case SimPukLocked:
                 // text
                 mCarrier.setText(
                         getCarrierString(
                                 mUpdateMonitor.getTelephonyPlmn(),
                                 getContext().getText(R.string.lockscreen_sim_puk_locked_message)));
                 mScreenLocked.setText(R.string.lockscreen_sim_puk_locked_instructions);
 
                 // layout
                 mScreenLocked.setVisibility(View.VISIBLE);
                 setUnlockWidgetsState(false);
                 mEmergencyCallText.setVisibility(View.VISIBLE);
                 mEmergencyCallButton.setVisibility(View.VISIBLE);
                 break;
         }
     }
 
     private void setUnlockWidgetsState(boolean show) {
         if (show) {
             if (mUseSenseLockscreen) {
                 mSenseRingSelector.setVisibility(View.VISIBLE);
                 mCircularSelector.setVisibility(View.GONE);
                 mSelector.setVisibility(View.GONE);
             } else if (mUseCircularLockscreen) {
                 mSenseRingSelector.setVisibility(View.GONE);
                 mCircularSelector.setVisibility(View.VISIBLE);
                 mSelector.setVisibility(View.GONE);
             } else {
                 mSenseRingSelector.setVisibility(View.GONE);
                 mCircularSelector.setVisibility(View.GONE);
                 mSelector.setVisibility(View.VISIBLE);
             }
         } else {
             mSenseRingSelector.setVisibility(View.GONE);
             mCircularSelector.setVisibility(View.GONE);
             mSelector.setVisibility(View.GONE);
         }
     }
 
     static CharSequence getCarrierString(CharSequence telephonyPlmn, CharSequence telephonySpn) {
         return getCarrierString(telephonyPlmn, telephonySpn, CARRIER_TYPE_DEFAULT, "");
     }
 
     static CharSequence getCarrierString(CharSequence telephonyPlmn, CharSequence telephonySpn,
             int carrierLabelType, String carrierLabelCustom) {
         switch (carrierLabelType) {
             default:
             case CARRIER_TYPE_DEFAULT:
                 if (telephonyPlmn != null && TextUtils.isEmpty(telephonySpn)) {
                     return telephonyPlmn;
                 } else if (telephonySpn != null && TextUtils.isEmpty(telephonyPlmn)) {
                     return telephonySpn;
                 } else if (telephonyPlmn != null && telephonySpn != null) {
                     return telephonyPlmn + "|" + telephonySpn;
                 }
                 return "";
             case CARRIER_TYPE_SPN:
                 if (telephonySpn != null) {
                     return telephonySpn;
                  }
                  break;
             case CARRIER_TYPE_PLMN:
                 if (telephonyPlmn != null) {
                     return telephonyPlmn;
                 }
                 break;
             case CARRIER_TYPE_CUSTOM:
                 return carrierLabelCustom;
          }
          return "";
      }
 
     public void onSimStateChanged(IccCard.State simState) {
         if (DBG) Log.d(TAG, "onSimStateChanged(" + simState + ")");
         mStatus = getCurrentStatus(simState);
         updateLayout(mStatus);
         updateStatusLines();
     }
 
     void updateConfiguration() {
         Configuration newConfig = getResources().getConfiguration();
         if (newConfig.hardKeyboardHidden != mKeyboardHidden) {
             mKeyboardHidden = newConfig.hardKeyboardHidden;
             final boolean isKeyboardOpen = mKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
             if (mUpdateMonitor.isKeyguardBypassEnabled() && isKeyboardOpen) {
                 mCallback.goToUnlockScreen();
                 Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
                 return;
             }
         }
         if (newConfig.orientation != mCreationOrientation) {
             mCallback.recreateMe(newConfig);
         }
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
         if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
             Log.v(TAG, "***** LOCK ATTACHED TO WINDOW");
             Log.v(TAG, "Cur orient=" + mCreationOrientation
                     + ", new config=" + getResources().getConfiguration());
         }
         updateConfiguration();
     }
 
     /** {@inheritDoc} */
     @Override
     protected void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         if (LockPatternKeyguardView.DEBUG_CONFIGURATION) {
             Log.w(TAG, "***** LOCK CONFIG CHANGING", new RuntimeException());
             Log.v(TAG, "Cur orient=" + mCreationOrientation
                     + ", new config=" + newConfig);
         }
         updateConfiguration();
     }
 
     /** {@inheritDoc} */
     public boolean needsInput() {
         return false;
     }
 
     /** {@inheritDoc} */
     public void onPause() {
         mSelector.enableUnlockMode();
         if (mSmsCallListener != null) {
             getContext().unregisterReceiver(mSmsCallListener);
             mSmsCallListener = null;
         }
     }
 
     /** {@inheritDoc} */
     public void onResume() {
         if (mUseFuzzyClock){
             mFuzzyClock.updateTime();
         } else if (mUseKanjiClock) {
             mKanjiClock.updateTime();
         } else {
             mClock.updateTime();
         }
         resetStatusInfo(mUpdateMonitor);
         mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
         mSelector.enableUnlockMode();
         if (mSmsCallListener == null) {
             getContext().registerReceiver(mSmsCallListener, filter);
         }
     }
 
     private BroadcastReceiver mSmsCallListener = new BroadcastReceiver() {
             public void onReceive(Context context, Intent intent) {
                 String action = intent.getAction();
                 if (action.equals(SMS_CHANGED) && !mLockMessage) {
                     mSmsCallHandler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             if (!mLockMessage) {
                             // get a new count and set indicator
                                 smsCount = SmsCallWidgetHelper.getUnreadSmsCount(getContext());
                                 setSmsWidget();
                             }
                         }
                     },10);
                 } else if (action.equals(
                         TelephonyManager.
                         ACTION_PHONE_STATE_CHANGED) && !mLockMessage) {
                     mSmsCallHandler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             if (!mLockMessage) {
                             // get a new count and set indicator
                                 callCount = SmsCallWidgetHelper.getMissedCallCount(getContext());
                                 setCallWidget();
                             }
                         }
                     },10);
                 }
             };
     };
 
     private void setSmsWidget() {
         if (mLockMessage) {
             return;
         }
 
         messagesId = SmsCallWidgetHelper.getSmsId(getContext());
 
         if (smsCount > 1) {
              mSmsCountView.setText(Integer.toString(smsCount) + " Unread Messages");
         } else if (smsCount == 1) {
              mSmsCountView.setText("1 Unread Message");
         } else {
              mSmsCountView.setText("0 Unread Message");
         }
     }
 
     private void setCallWidget() {
         if (mLockMessage) {
             return;
         }
 
         if (callCount > 1) {
              mMissedCallCountView.setText(Integer.toString(callCount) + " Missed Calls");
         } else if (callCount == 1) {
              mMissedCallCountView.setText("1 Missed Call");
         } else {
              mMissedCallCountView.setText("0 Missed Call");
         }
     }
 
     /** {@inheritDoc} */
     public void cleanUp() {
         mUpdateMonitor.removeCallback(this); // this must be first
         mLockPatternUtils = null;
         mUpdateMonitor = null;
         mCallback = null;
     }
 
     /** {@inheritDoc} */
     public void onRingerModeChanged(int state) {
         boolean silent = AudioManager.RINGER_MODE_NORMAL != state;
         if (silent != mSilentMode) {
             mSilentMode = silent;
             updateRightTabResources();
         }
     }
 
     public void onPhoneStateChanged(String newState) {
         mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
     }
 
     private void toggleSilentMode() {
         // tri state silent<->vibrate<->ring if silent mode is enabled, otherwise toggle silent mode
         final boolean mVolumeControlSilent = Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.VOLUME_CONTROL_SILENT, 0) != 0;
         mSilentMode = mVolumeControlSilent
             ? ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) || !mSilentMode)
             : !mSilentMode;
         if (mSilentMode) {
             final boolean vibe = mVolumeControlSilent
             ? (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE)
             : (Settings.System.getInt(
                 getContext().getContentResolver(),
                 Settings.System.VIBRATE_IN_SILENT, 1) == 1);
 
             mAudioManager.setRingerMode(vibe
                 ? AudioManager.RINGER_MODE_VIBRATE
                 : AudioManager.RINGER_MODE_SILENT);
         } else {
             mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
         }
 
         String message = mSilentMode ?
                 getContext().getString(R.string.global_action_silent_mode_on_status) :
                 getContext().getString(R.string.global_action_silent_mode_off_status);
 
         final int toastIcon = mSilentMode
             ? R.drawable.ic_lock_ringer_off
             : R.drawable.ic_lock_ringer_on;
 
         final int toastColor = mSilentMode
             ? getContext().getResources().getColor(R.color.keyguard_text_color_soundoff)
             : getContext().getResources().getColor(R.color.keyguard_text_color_soundon);
         toastMessage(mScreenLocked, message, toastColor, toastIcon);
     }
 
     // shameless kang of music widgets
     public static Uri getArtworkUri(Context context, long song_id, long album_id) {
 
         if (album_id < 0) {
             // This is something that is not in the database, so get the album art directly
             // from the file.
             if (song_id >= 0) {
                 return getArtworkUriFromFile(context, song_id, -1);
             }
             return null;
         }
 
         ContentResolver res = context.getContentResolver();
         Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
         if (uri != null) {
             InputStream in = null;
             try {
                 in = res.openInputStream(uri);
                 return uri;
             } catch (FileNotFoundException ex) {
                 // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                 // maybe it never existed to begin with.
                 return getArtworkUriFromFile(context, song_id, album_id);
             } finally {
                 try {
                     if (in != null) {
                         in.close();
                     }
                 } catch (IOException ex) {
                 }
             }
         }
         return null;
     }
 
     private static Uri getArtworkUriFromFile(Context context, long songid, long albumid) {
 
         if (albumid < 0 && songid < 0) {
             return null;
         }
 
         try {
             if (albumid < 0) {
                 Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                 ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                 if (pfd != null) {
                     return uri;
                 }
             } else {
                 Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                 ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                 if (pfd != null) {
                     return uri;
                 }
             }
         } catch (FileNotFoundException ex) {
             //
         }
         return null;
     }
 
     /*
      * enables or disables visibility of most lockscreen widgets
      * depending on lense status
      */
     private void setLenseWidgetsVisibility(int visibility){
         if (mUseFuzzyClock){
             mFuzzyClock.setVisibility(visibility);
         } else if (mUseKanjiClock) {
             mKanjiClock.setVisibility(visibility);
         } else {
             mClock.setVisibility(visibility);
         }
         mDate.setVisibility(visibility);
         mCusText.setVisibility(visibility);
         if (!mLockMessage) {
            mSmsCountView.setVisibility(visibility);
            mMissedCallCountView.setVisibility(visibility);
         }
         mTime.setVisibility(visibility);
         if (mUseKanjiClock) {
         } else {
            mAmPm.setVisibility(visibility);
         }
         if ((Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER, 0) != 1) ||
             (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER_CENTER, 0) != 1) ||
             (Settings.System.getInt(mContext.getContentResolver(),
             Settings.System.STATUS_BAR_STATUSBAR_CARRIER_LEFT, 0) != 1)) {
         mCarrier.setVisibility(visibility);
         }
         mNowPlaying.setVisibility(visibility);
         mAlbumArt.setVisibility(visibility);
 
         if (DateFormat.is24HourFormat(mContext)) {
            if (mUseKanjiClock) {
            } else {
               mAmPm.setVisibility(View.INVISIBLE);
            }
         }
 
         mNowPlayingToggle = false;
         mAlbumArtToggle = false;
         if (visibility == View.VISIBLE
                 && (Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.LOCKSCREEN_NOW_PLAYING, 1) == 1))
             mNowPlayingToggle = true;
         if (visibility == View.VISIBLE
                 && (Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.LOCKSCREEN_ALBUM_ART, 1) == 1))
             mAlbumArtToggle = true;
     }
 
     private void runActivity(String uri) {
         try {
             Intent i = Intent.parseUri(uri, 0);
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                 | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
             mContext.startActivity(i);
             mCallback.goToUnlockScreen();
                     Settings.System.putInt(mContext.getContentResolver(), Settings.System.SHOW_STATUS_BAR_LOCK, 0);
         } catch (URISyntaxException e) {
         } catch (ActivityNotFoundException e) {
         }
     }
 }
