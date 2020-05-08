 /*
  * Copyright (C) 2010 The Android Open Source Project
  * Patched by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
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
 
 package com.android.systemui.statusbar;
 
 import com.android.internal.statusbar.IStatusBarService;
 import com.android.internal.statusbar.StatusBarIcon;
 import com.android.internal.statusbar.StatusBarIconList;
 import com.android.internal.statusbar.StatusBarNotification;
 import com.android.systemui.statusbar.batteries.CmBatteryMiniIcon;
 import com.android.systemui.statusbar.batteries.CmBatterySideBar;
 import com.android.systemui.statusbar.batteries.CmBatteryStatusBar;
 import com.android.systemui.statusbar.carrierlabels.CarrierLabel;
 import com.android.systemui.statusbar.carrierlabels.CarrierLabelBottom;
 import com.android.systemui.statusbar.carrierlabels.CarrierLabelExp;
 import com.android.systemui.statusbar.carrierlabels.CarrierLabelStatusBar;
 import com.android.systemui.statusbar.carrierlabels.CarrierLogo;
 import com.android.systemui.statusbar.carrierlabels.CenterCarrierLabelStatusBar;
 import com.android.systemui.statusbar.carrierlabels.CenterCarrierLogo;
 import com.android.systemui.statusbar.carrierlabels.LeftCarrierLabelStatusBar;
 import com.android.systemui.statusbar.carrierlabels.LeftCarrierLogo;
 import com.android.systemui.statusbar.clocks.CenterClock;
 import com.android.systemui.statusbar.clocks.Clock;
 import com.android.systemui.statusbar.clocks.LeftClock;
 import com.android.systemui.statusbar.clocks.PowerClock;
 import com.android.systemui.statusbar.clocks.ClockExpand;
 import com.android.systemui.statusbar.cmcustom.BackLogo;
 import com.android.systemui.statusbar.dates.DateView;
 import com.android.systemui.statusbar.dates.ExDateView;
 import com.android.systemui.statusbar.navbar.NavigationBarView;
 import com.android.systemui.statusbar.popups.BrightnessPanel;
 import com.android.systemui.statusbar.popups.QuickSettingsPopupWindow;
 import com.android.systemui.statusbar.popups.ShortcutPopupWindow;
 import com.android.systemui.statusbar.popups.WeatherPopup;
 import com.android.systemui.statusbar.powerwidget.PowerWidget;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetBottom;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetOne;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetTwo;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetThree;
 import com.android.systemui.statusbar.powerwidget.PowerWidgetFour;
 import com.android.systemui.statusbar.powerwidget.MusicControls;
 import com.android.systemui.statusbar.qwidgets.QwikWidgetsPanelView;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsContainerView;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsController;
 import com.android.systemui.statusbar.quicksettings.Tile3dFlipAnimation;
 import com.android.systemui.statusbar.policy.NetworkController;
 import com.android.systemui.statusbar.policy.PiePolicy;
 import com.android.systemui.statusbar.policy.StatusBarPolicy;
 import com.android.systemui.statusbar.policy.DataTraffics;
 import com.android.systemui.R;
 import android.os.IPowerManager;
 import android.provider.Settings.SettingNotFoundException;
 import android.animationing.Animator;
 import android.animationing.AnimatorListenerAdapter;
 import android.animationing.AnimatorSet;
 import android.animationing.ObjectAnimator;
 import android.animationing.TimeInterpolator;
 import android.app.ActivityManagerNative;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.app.Dialog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.StatusBarManager;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.CustomTheme;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.graphics.PixelFormat;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.NinePatchDrawable;
 import android.graphics.PorterDuff.Mode;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.os.SystemClock;
 import android.provider.CmSystem;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.util.DisplayMetrics;
 import android.util.Pair;
 import android.util.Log;
 import android.util.Slog;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.HapticFeedbackConstants;
 import android.view.IWindowManager;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.WindowManagerImpl;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.DecelerateInterpolator;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RemoteViews;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.ProgressBar;
 import android.net.Uri;
 import java.io.File;
 
 import java.io.FileDescriptor;
 import java.io.RandomAccessFile;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 public class StatusBarService extends Service implements CommandQueue.Callbacks {
     public static final String TAG = "StatusBarService";
     private static final boolean SPEW_ICONS = false;
     private static final boolean SPEW = false;
 
     public static final String ACTION_STATUSBAR_START
             = "com.android.internal.policy.statusbar.START";
 
     // values changed onCreate if its a bottomBar
     static int EXPANDED_LEAVE_ALONE = -10000;
     static int EXPANDED_FULL_OPEN = -10001;
 
     private static final float BRIGHTNESS_CONTROL_PADDING = 0.15f;
     private static final int BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT = 750; // ms
     private static final int BRIGHTNESS_CONTROL_LINGER_THRESHOLD = 20;
     private boolean mBrightnessControl;
 
     private static final int MSG_ANIMATE = 1000;
     private static final int MSG_ANIMATE_REVEAL = 1001;
     private int mClockColor;
 
     private static final int MSG_SHOW_INTRUDER = 1002;
     private static final int MSG_HIDE_INTRUDER = 1003;
 
     private static final int MSG_SHOW_WIDGETS_PANEL = 1004;
     private static final int MSG_HIDE_WIDGETS_PANEL = 1005;
 
     private static final int MSG_SHOW_RING_PANEL = 1006;
     private static final int MSG_HIDE_RING_PANEL = 1007;
 
     // will likely move to a resource or other tunable param at some point
     private static final int INTRUDER_ALERT_DECAY_MS = 3000;
 
     private StatusBarPolicy mIconPolicy;
     private NetworkController mNetworkPolicy;
     private PiePolicy mPiePolicy;
     private CommandQueue mCommandQueue;
     private IStatusBarService mBarService;
 
     /**
      * Shallow container for {@link #mStatusBarView} which is added to the
      * window manager impl as the actual status bar root view. This is done so
      * that the original status_bar layout can be reinflated into this container
      * on skin change.
      */
     private FrameLayout mStatusBarContainer;
 
     private int mIconSize;
     private Display mDisplay;
     private CmStatusBarView mStatusBarView;
     private H mHandler = new H();
     private Object mQueueLock = new Object();
 
     // last theme that was applied in order to detect theme change (as opposed
     // to some other configuration change).
     private CustomTheme mCurrentTheme;
     private DoNotDisturb mDoNotDisturb;
 
     // icons
     private LinearLayout mIcons;
     private LinearLayout mCenterClock;
     private LinearLayout mCenterClockex;
     private SignalClusterView mCenterIconex;
     private LinearLayout mLeftClock;
     private IconMerger mNotificationIcons;
     private LinearLayout mStatusIcons;
     private ImageView mSettingsIconButton;
 
     // expanded notifications
     private Dialog mExpandedDialog;
     private ExpandedView mExpandedView;
     private WindowManager.LayoutParams mExpandedParams;
     private ScrollView mScrollView;
     private ScrollView mBottomScrollView;
     private LinearLayout mNotificationLinearLayout;
     private LinearLayout mBottomNotificationLinearLayout;
     private View mExpandedContents;
     private View mExpandededContents;
     // top bar
     private TextView mNoNotificationsTitle;
     private TextView mNoNotificationsTitles;
     private ImageView mClearButton;
     private TextView mClearOldButton;
     private TextView mCompactClearButton;
     private CmBatteryMiniIcon mCmBatteryMiniIcon;
     // drag bar
     private CloseDragHandle mCloseView;
     // ongoing
     private NotificationData mOngoing = new NotificationData();
     private TextView mOngoingTitle;
     private LinearLayout mOngoingItems;
     // latest
     private NotificationData mLatest = new NotificationData();
     private NotificationData mNotifData = new NotificationData();
     private TextView mLatestTitle;
     private TextView mLatestTitles;
     private LinearLayout mLatestItems;
     private QuickSettingsController mQS;
     private QuickSettingsContainerView mQuickContainer;
     private ItemTouchDispatcher mTouchDispatcher;
     // position
     private int[] mPositionTmp = new int[2];
     public boolean mExpanded;
     private boolean mExpandedVisible;
 
     // Pie controls
     public PieControlPanel mPieControlPanel;
     public View mPieControlsTrigger;
     public View mContainer;
     private int mIndex;
 
     // the date view
     private DateView mDateView;
 
     // on-screen navigation buttons
     private NavigationBarView mNavigationBarView;
     
     // the tracker view
     private TrackingView mTrackingView;
     private View mNotificationBackgroundView;
     private ImageView mSettingsButton;
     private WindowManager.LayoutParams mTrackingParams;
     private int mTrackingPosition; // the position of the top of the tracking view.
     private boolean mPanelSlightlyVisible;
 
     // the power widget
     private PowerWidget mPowerWidget;
     private PowerWidgetBottom mPowerWidgetBottom;
     private PowerWidgetOne mPowerWidgetOne;
     private PowerWidgetTwo mPowerWidgetTwo;
     private PowerWidgetThree mPowerWidgetThree;
     private PowerWidgetFour mPowerWidgetFour;
     private QwikWidgetsPanelView mWidgetsPanel;
     private RingPanelView mRingPanel;
 
     private MusicControls mMusicControls;
 
     //Carrier label stuff
     private LinearLayout mCarrierLabelLayout;
     private CarrierLabelStatusBar mCarrierLabelStatusBarLayout;
     private CarrierLabelBottom mCarrierLabelBottomLayout;
     private CenterCarrierLabelStatusBar mCenterCarrierLabelStatusBarLayout;
     private LeftCarrierLabelStatusBar mLeftCarrierLabelStatusBarLayout;
     private CarrierLogo mCarrierLogoLayout;
     private CenterCarrierLogo mCarrierLogoCenterLayout;
     private LeftCarrierLogo mCarrierLogoLeftLayout;
     private BackLogo mBackLogoLayout;
     private CmBatteryStatusBar mCmBatteryStatusBar;
     private LinearLayout mCompactCarrierLayout;
     private LinearLayout mPowerAndCarrier;
     private FrameLayout mNaviBarContainer;
     private CarrierLabelExp mCarrierLabelExpLayout;
 
     // ticker
     private Ticker mTicker;
     private View mTickerView;
     private boolean mTicking;
     private TickerView mTickerText;
     private TextView mNotificationsToggle;
     private TextView mButtonsToggle;
     private LinearLayout mNotifications;
     private LinearLayout mPowerCarrier;
     private LinearLayout mButtons;
     public static ImageView mMusicToggleButton;
 
     private BrightnessPanel mBrightnessPanel = null;
 
     // notification color default variables
     private int mBlackColor = 0xFF000000;
     private int mWhiteColor = 0xFFFFFFFF;
 
     // notfication color temp variables
     private int mItemText = mBlackColor;
     private int mItemTime;
     private int mItemTitle;
     private int mDateColor;
     private int mButtonText = mBlackColor;
     private int mNotifyNone;
     private int mNotifyTicker;
     private int mNotifyLatest;
     private int mNotifyOngoing;
     private int mSettingsColor;
     private int IntruderTime;
 
     private LinearLayout mAvalMemLayout;
     private TextView memHeader;
     private ProgressBar avalMemPB;
     private double totalMemory;
     private double availableMemory;
 
     // Tracking finger for opening/closing.
     private int mEdgeBorder; // corresponds to R.dimen.status_bar_edge_ignore
     private boolean mTracking;
     private VelocityTracker mVelocityTracker;
 
     private static final int ANIM_FRAME_DURATION = (1000/60);
 
     private boolean mAnimating;
     private long mCurAnimationTime;
     private float mAnimY;
     private float mAnimVel;
     private float mAnimAccel;
     private long mAnimLastTime;
     private boolean mAnimatingReveal = false;
     private int mViewDelta;
     private int[] mAbsPos = new int[2];
 
     // for disabling the status bar
     private int mDisabled = 0;
 
     // tracking for the last visible power widget id so hide toggle works properly
     private int mLastPowerToggle = 1;
 
     // weather or not to show status bar on bottom
     private boolean mBottomBar;
     private boolean mButtonsLeft;
 
     private boolean mDeadZone;
     private boolean mHasSoftButtons;
     private boolean autoBrightness = false;
     private boolean shouldTick = false;
     private Context mContext;
     private int mStatusBarCarrier;
     private int mStatusBarCarrierLogo;
     private int mStatusBarClock;
     private boolean mShowDate = false;
     private boolean mShowNotif = false;
     private boolean mFirstis = true;
     private boolean mNaviShow = true;
     private boolean mPieEnable = true;
     private boolean mStatusBarHidden = false;
     private boolean mStatusBarReverse = false;
     private boolean mStatusBarTab = false;
     private boolean mStatusBarGrid = false;
     private boolean LogoStatusBar = false;
     private boolean mShowCmBatteryStatusBar = false;
     private boolean mShowCmBatterySideBar = false;
     private boolean mTinyExpanded = true;
     private boolean mMoreExpanded = true;
     private boolean mShowRam = true;
     private boolean mShowIconex = true;
     private boolean NotifEnable = true;
     private boolean isFullExpanded = false;
 
     private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
 
     // tracks changes to settings, so status bar is moved to top/bottom
     // as soon as cmparts setting is changed
     private class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_BOTTOM), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SOFT_BUTTONS_LEFT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SHOW_NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_DEAD_ZONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_CARRIER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_CLOCK), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_REVERSE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_ONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_TWO), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_THREE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.EXPANDED_VIEW_WIDGET_GRID_FOUR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_DATE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.CARRIER_LOGO_STATUS_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_CLOCKCOLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_SETTINGSCOLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_STATS_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_ICONS_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_NAVI_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_EXPANDED_SIZE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_TINY_EXPANDED), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_MORE_EXPANDED), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_DATE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_NONE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_LATEST), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ONGOING), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_TICKER_TEXT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_CLEAR_BUTTON), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TITLE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TEXT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.COLOR_NOTIFICATION_ITEM_TIME), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.TRANSPARENT_STATUS_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.TRANSPARENT_NAVI_BAR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BAR_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.USE_SOFT_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_INTRUDER_ALERT), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_NOTIF), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_SHOWRAM), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUS_BAR_SHOWICONEX), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.QUICK_SETTINGS_TILES), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.PIE_TRIGGER), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.PIE_CONTROL_ENABLE), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_ICONS_VISIBILITY), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             ContentResolver resolver = mContext.getContentResolver();
             int defValue;
             int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_BOTTOM_STATUS_BAR) ? 1 : 0);
             mBottomBar = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BOTTOM, defValue) == 1);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SOFT_BUTTONS_LEFT) ? 1 : 0);
             mButtonsLeft = (Settings.System.getInt(resolver, Settings.System.SOFT_BUTTONS_LEFT, defValue) == 1);
             defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_USE_DEAD_ZONE) ? 1 : 0);
             mDeadZone = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_DEAD_ZONE, defValue) == 1);
             mStatusBarCarrier = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CARRIER, 6);
             mStatusBarClock = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CLOCK, 1);
             mStatusBarCarrierLogo = Settings.System.getInt(resolver, Settings.System.CARRIER_LOGO, 0);
             mStatusBarReverse = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_REVERSE, 0) == 1);
             mShowCmBatteryStatusBar = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0) == 5);
             mShowDate = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_DATE, 0) == 1);
             mShowNotif = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_NOTIF, 1) == 1);
             mShowRam = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SHOWRAM, 1) == 1);
             mShowIconex = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SHOWICONEX, 1) == 1);
             mShowCmBatterySideBar = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0) == 4);
             mHasSoftButtons = (Settings.System.getInt(resolver, Settings.System.USE_SOFT_BUTTONS, 0) == 1);
             LogoStatusBar = (Settings.System.getInt(resolver, Settings.System.CARRIER_LOGO_STATUS_BAR, 0) == 1);
             shouldTick = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_INTRUDER_ALERT, 1) == 1);
             mClockColor = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CLOCKCOLOR, defValuesColor));
             mSettingsColor = (Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SETTINGSCOLOR, defValuesColor));
             mStatusBarTab = ((Settings.System.getInt(resolver, Settings.System.EXPANDED_VIEW_WIDGET, 5) == 4)
                       || (Settings.System.getInt(resolver, Settings.System.EXPANDED_VIEW_WIDGET, 5) == 5));
             mStatusBarGrid = (Settings.System.getInt(resolver, Settings.System.EXPANDED_VIEW_WIDGET, 5) == 3);
             mNaviShow = (Settings.System.getInt(resolver, Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
             mTinyExpanded = (Settings.System.getInt(resolver, Settings.System.STATUSBAR_TINY_EXPANDED, 1) == 1);
             mMoreExpanded = (Settings.System.getInt(resolver, Settings.System.STATUSBAR_MORE_EXPANDED, 1) == 1);
             autoBrightness = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0) ==
                     Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
             mBrightnessControl = !autoBrightness && Settings.System.getInt(resolver,
                     Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1;
             IntruderTime = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_INTRUDER_TIME, INTRUDER_ALERT_DECAY_MS);
             mPieEnable = (Settings.System.getInt(resolver, Settings.System.PIE_CONTROL_ENABLE, 1) == 1);
             mStatusBarHidden = (Settings.System.getInt(resolver, Settings.System.STATUSBAR_ICONS_VISIBILITY, 0) == 1);
             updateColors();
             updateLayout();
             updateCarrierLabel();
             updateCarrierLogo();
             updateSettings();
             updatePieControls();
             updateStateBackground();
         }
     }
 
     // for brightness control on status bar
     private int mLinger;
     private int mInitialTouchX;
     private int mInitialTouchY;
 
     private class ExpandedDialog extends Dialog {
         ExpandedDialog(Context context) {
             super(context, com.android.internal.R.style.Theme_Light_NoTitleBar);
         }
 
         @Override
         public boolean dispatchKeyEvent(KeyEvent event) {
             boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
             switch (event.getKeyCode()) {
             case KeyEvent.KEYCODE_BACK:
                 if (!down) {
                     animateCollapse();
                 }
                 return true;
             }
             return super.dispatchKeyEvent(event);
         }
     }
 
     private Runnable mLongPressBrightnessChange = new Runnable() {
         @Override
         public void run() {
             mStatusBarView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
             adjustBrightness(mInitialTouchX);
             mLinger = BRIGHTNESS_CONTROL_LINGER_THRESHOLD + 1;
         }
     };
 
     @Override
     public void onCreate() {
         // First set up our views and stuff.
         mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         CustomTheme currentTheme = getResources().getConfiguration().customTheme;
         if (currentTheme != null) {
             mCurrentTheme = (CustomTheme) currentTheme.clone();
         }
         makeStatusBarView(this);
 
         // reset vars for bottom bar
         if(mBottomBar){
             EXPANDED_LEAVE_ALONE *= -1;
             EXPANDED_FULL_OPEN *= -1;
         }
 
         // receive broadcasts
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
         filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
         filter.addAction(Intent.ACTION_SCREEN_OFF);
         registerReceiver(mBroadcastReceiver, filter);
 
         // Connect in to the status bar manager service
         StatusBarIconList iconList = new StatusBarIconList();
         ArrayList<IBinder> notificationKeys = new ArrayList<IBinder>();
         ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
         mCommandQueue = new CommandQueue(this, iconList);
         mBarService = IStatusBarService.Stub.asInterface(
                 ServiceManager.getService(Context.STATUS_BAR_SERVICE));
         int[] switches = new int[3];
         try {
             mBarService.registerStatusBar(mCommandQueue, iconList, notificationKeys, notifications,
                         switches);
         } catch (RemoteException ex) {
             // If the system process isn't there we're doomed anyway.
         }
 
         disable(switches[0]);
         setIMEVisible(switches[1] != 0);
         showNaviBar(switches[2] != 0);
 
         // Set up the initial icon state
         int N = iconList.size();
         int viewIndex = 0;
         for (int i=0; i<N; i++) {
             StatusBarIcon icon = iconList.getIcon(i);
             if (icon != null) {
                 addIcon(iconList.getSlot(i), i, viewIndex, icon);
                 viewIndex++;
             }
         }
 
         // Set up the initial notification state
         N = notificationKeys.size();
         if (N == notifications.size()) {
             for (int i=0; i<N; i++) {
                 addNotification(notificationKeys.get(i), notifications.get(i));
             }
         } else {
             Slog.e(TAG, "Notification list length mismatch: keys=" + N
                     + " notifications=" + notifications.size());
         }
 
         // Put up the view
         FrameLayout container = new FrameLayout(this);
         container.addView(mStatusBarView);
         mStatusBarContainer = container;
         addStatusBarView();
         addNavigationBar();
         addIntruderView();
         updateWidgetsPanel();
         updateRingsPanel();
         attachPies();
 
         // Lastly, call to the icon policy to install/update all the icons.
         mIconPolicy = new StatusBarPolicy(this);
         mNetworkPolicy = new NetworkController(this);
         mPiePolicy = new PiePolicy(this);
 
         mContext = getApplicationContext();
 
         mContext.getContentResolver().registerContentObserver(
                 Settings.System.getUriFor(Settings.System.PIE_GRAVITY), false, new ContentObserver(new Handler()) {
                     @Override
                     public void onChange(boolean selfChange) {
                         if (Settings.System.getInt(mContext.getContentResolver(),
                                 Settings.System.PIE_STICK, 0) == 0) {
                             updatePieControls();
                         }}});
 
         // set up settings observer
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         makeBatterySideBarViewLeft();
         makeBatterySideBarViewRight();
     }
 
     public IStatusBarService getServicesBar() {
         return mBarService;
     }
 
     @Override
     public void onDestroy() {
         // we're never destroyed
     }
 
     // for immersive activities
     private IntruderView mIntruderAlertView;
 
     /**
      * Nobody binds to us.
      */
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     // ================================================================================
     // Constructing the view
     // ================================================================================
     private void makeStatusBarView(Context context) {
         mContext = context;
         Resources res = context.getResources();
 
         updateDisplaySize();
 
         mTouchDispatcher = new ItemTouchDispatcher(this);
 
         //Check for compact carrier layout and apply if enabled
         InitializeSettings(context);
 
         if (!mStatusBarTab) {
             mExpandedView = (ExpandedView) View.inflate(context, R.layout.status_bar_expanded, null);
         } else {
             mExpandedView = (ExpandedView) View.inflate(context, R.layout.status_bar_expandedtab, null);
         }
         mExpandedView.mService = this;
         mExpandedView.mTouchDispatcher = mTouchDispatcher;
 
         if (!mStatusBarReverse) {
             mStatusBarView = (CmStatusBarView) View.inflate(context, R.layout.status_bar, null);
         } else {
             mStatusBarView = (CmStatusBarView) View.inflate(context, R.layout.status_bar_reverse, null);
         }
         mStatusBarView.mService = this;
 
         mIntruderAlertView = (IntruderView) View.inflate(context, R.layout.intruder_content, null);
         mIntruderAlertView.mService = this;
         mIntruderAlertView.mTouchDispatcher = mTouchDispatcher;
         mIntruderAlertView.setVisibility(View.GONE);
 
         mNavigationBarView = (NavigationBarView) View.inflate(context, R.layout.navigation_bar, null);
         mNavigationBarView.mServices = this;
         mNaviBarContainer = (FrameLayout) mNavigationBarView.findViewById(R.id.navibarBackground);
 
         mWidgetsPanel = (QwikWidgetsPanelView) View.inflate(context, R.layout.qwik_widgets_panel, null);
         mWidgetsPanel.setVisibility(View.GONE);
 
         mRingPanel = (RingPanelView) View.inflate(context, R.layout.ring_widget_panel, null);
         mRingPanel.mService = this;
         mRingPanel.setOnTouchListener(mRingPanelListener);
         mRingPanel.setVisibility(View.GONE);
 
         mBackLogoLayout = (BackLogo) mStatusBarView.findViewById(R.id.backlogo);
         mStatusIcons = (LinearLayout) mStatusBarView.findViewById(R.id.statusIcons);
         mStatusIcons.setOnClickListener(mIconButtonListener);
         mNotificationIcons = (IconMerger) mStatusBarView.findViewById(R.id.notificationIcons);
         mIcons = (LinearLayout)mStatusBarView.findViewById(R.id.icons);
         mCenterClock = (LinearLayout) mStatusBarView.findViewById(R.id.centerClock);
         mLeftClock = (LinearLayout) mStatusBarView.findViewById(R.id.clockLeft);
         mCarrierLabelStatusBarLayout = (CarrierLabelStatusBar) mStatusBarView.findViewById(R.id.carrier_label_status_bar_layout);
         mCenterCarrierLabelStatusBarLayout = (CenterCarrierLabelStatusBar) mStatusBarView.findViewById(R.id.carrier_label_status_bar_center_layout);
         mLeftCarrierLabelStatusBarLayout = (LeftCarrierLabelStatusBar) mStatusBarView.findViewById(R.id.carrier_label_status_bar_left_layout);
         mCarrierLogoLayout = (CarrierLogo) mStatusBarView.findViewById(R.id.carrier_logo);
         mCarrierLogoCenterLayout = (CenterCarrierLogo) mStatusBarView.findViewById(R.id.carrier_logo_center);
         mCarrierLogoLeftLayout = (LeftCarrierLogo) mStatusBarView.findViewById(R.id.carrier_logo_left);
         mCmBatteryStatusBar = (CmBatteryStatusBar) mStatusBarView.findViewById(R.id.batteryStatusBar);
         mTickerView = mStatusBarView.findViewById(R.id.ticker);
         mDateView = (DateView) mStatusBarView.findViewById(R.id.date);
         mCmBatteryMiniIcon = (CmBatteryMiniIcon) mStatusBarView.findViewById(R.id.CmBatteryMiniIcon);
 
         /* Destroy any existing widgets before recreating the expanded dialog
          * to ensure there are no lost context issues */
         if (mPowerWidget != null) {
             mPowerWidget.destroyWidget();
         }
 
         if (mPowerWidgetBottom != null) {
             mPowerWidgetBottom.destroyWidget();
         }
 
         if (mPowerWidgetOne != null) {
             mPowerWidgetOne.destroyWidget();
         }
 
         if (mPowerWidgetTwo != null) {
             mPowerWidgetTwo.destroyWidget();
         }
 
         if (mPowerWidgetThree != null) {
             mPowerWidgetThree.destroyWidget();
         }
 
         if (mPowerWidgetFour != null) {
             mPowerWidgetFour.destroyWidget();
         }
 
         mExpandedDialog = new ExpandedDialog(context);
         if (!mStatusBarTab) {
             mExpandedContents = mExpandedView.findViewById(R.id.notificationLinearLayout);
         } else {
             mExpandedContents = mExpandedView.findViewById(R.id.notifications_layout);
             mExpandededContents = mExpandedView.findViewById(R.id.power_and_carrier_layout);
         }
         mOngoingTitle = (TextView) mExpandedView.findViewById(R.id.ongoingTitle);
         mOngoingItems = (LinearLayout) mExpandedView.findViewById(R.id.ongoingItems);
         mLatestTitle = (TextView) mExpandedView.findViewById(R.id.latestTitle);
         mLatestItems = (LinearLayout) mExpandedView.findViewById(R.id.latestItems);
         mNoNotificationsTitle = (TextView) mExpandedView.findViewById(R.id.noNotificationsTitle);
         if (mStatusBarTab) {
             mLatestTitles = (TextView) mExpandedView.findViewById(R.id.latestTitles);
             mNoNotificationsTitles = (TextView) mExpandedView.findViewById(R.id.noNotificationsTitles);
         }
         mClearButton = (ImageView) mExpandedView.findViewById(R.id.clear_all_button);
         mClearButton.setOnClickListener(mClearButtonListener);
         mClearOldButton = (TextView) mExpandedView.findViewById(R.id.clear_old_button);
         mClearOldButton.setOnClickListener(mClearButtonListener);
         mCompactClearButton = (TextView) mExpandedView.findViewById(R.id.compact_clear_all_button);
         mCompactClearButton.setOnClickListener(mClearButtonListener);
         mCarrierLabelExpLayout = (CarrierLabelExp) mExpandedView.findViewById(R.id.carrierExp);
         mPowerAndCarrier = (LinearLayout) mExpandedView.findViewById(R.id.power_and_carrier);
         mScrollView = (ScrollView) mExpandedView.findViewById(R.id.scroll);
         mBottomScrollView = (ScrollView) mExpandedView.findViewById(R.id.bottomScroll);
         mNotificationLinearLayout = (LinearLayout) mExpandedView.findViewById(R.id.notificationLinearLayout);
         mBottomNotificationLinearLayout = (LinearLayout) mExpandedView.findViewById(R.id.bottomNotificationLinearLayout);
 	mMusicToggleButton = (ImageView) mExpandedView.findViewById(R.id.music_toggle_button);
         mMusicToggleButton.setOnClickListener(mMusicToggleButtonListener);
         mCenterClockex = (LinearLayout) mExpandedView.findViewById(R.id.centerClockex);
         mCenterIconex = (SignalClusterView) mExpandedView.findViewById(R.id.centerIconex);
         mSettingsIconButton = (ImageView) mExpandedView.findViewById(R.id.settingIcon);
         if (mStatusBarTab) {
             mSettingsIconButton.setImageResource(R.drawable.ic_qs_panel);
             NotifEnable = true;
         } else {
             mSettingsIconButton.setImageResource(R.drawable.ic_sysbar_set);
         }
         mSettingsIconButton.setOnClickListener(mSettingsIconButtonListener);
         if (mStatusBarTab) {
             mQuickContainer = (QuickSettingsContainerView) mExpandedView.findViewById(R.id.quick_settings_container);
             if (mQuickContainer != null) {
                 mQS = new QuickSettingsController(context, mQuickContainer, this);
             }
         }
 
         mExpandedView.setVisibility(View.GONE);
         mOngoingTitle.setVisibility(View.GONE);
         mLatestTitle.setVisibility(View.GONE);
         if (mStatusBarTab) {
             mLatestTitles.setVisibility(View.GONE);
         }
 
         mMusicControls = (MusicControls) mExpandedView.findViewById(R.id.exp_music_controls);
 
         mPowerWidget = (PowerWidget) mExpandedView.findViewById(R.id.exp_power_stat);
         mPowerWidget.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidget.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mPowerWidgetOne = (PowerWidgetOne) mExpandedView.findViewById(R.id.exp_power_stat_one);
         mPowerWidgetOne.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidgetOne.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mPowerWidgetTwo = (PowerWidgetTwo) mExpandedView.findViewById(R.id.exp_power_stat_two);
         mPowerWidgetTwo.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidgetTwo.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mPowerWidgetThree = (PowerWidgetThree) mExpandedView.findViewById(R.id.exp_power_stat_three);
         mPowerWidgetThree.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidgetThree.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mPowerWidgetFour = (PowerWidgetFour) mExpandedView.findViewById(R.id.exp_power_stat_four);
         mPowerWidgetFour.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidgetFour.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mCarrierLabelLayout = (LinearLayout) mExpandedView.findViewById(R.id.carrier_label_layout);
         mCompactCarrierLayout = (LinearLayout) mExpandedView.findViewById(R.id.compact_carrier_layout);
 
         mAvalMemLayout = (LinearLayout) mExpandedView.findViewById(R.id.memlabel_layout);
         mAvalMemLayout.setOnClickListener(mAvalMemLayoutListener);
         mAvalMemLayout.setOnLongClickListener(mAvalMemLayoutTaskListener);
 
         memHeader = (TextView) mExpandedView.findViewById(R.id.avail_mem_text);
         avalMemPB = (ProgressBar) mExpandedView.findViewById(R.id.aval_memos);
         mTicker = new MyTicker(context, mStatusBarView);
 
         mTickerText = (TickerView) mStatusBarView.findViewById(R.id.tickerText);
         mTickerText.mTicker = mTicker;
 
         if (!mStatusBarTab) {
             mTrackingView = (TrackingView) View.inflate(context, R.layout.status_bar_tracking, null);
         } else {
             mTrackingView = (TrackingView) View.inflate(context, R.layout.status_bar_trackingtab, null);
         }
         mTrackingView.mService = this;
         mCloseView = (CloseDragHandle) mTrackingView.findViewById(R.id.close);
         mCloseView.mService = this;
         mNotificationBackgroundView = (View) mTrackingView.findViewById(R.id.notificationBackground);
 
         if (mStatusBarTab) {
             mPowerCarrier = (LinearLayout) mExpandedView.findViewById(R.id.power_and_carrier_layout);
             mNotifications = (LinearLayout) mExpandedView.findViewById(R.id.notifications_layout);
             mNotificationsToggle = (TextView) mTrackingView.findViewById(R.id.statusbar_notification_toggle);
             mButtonsToggle = (TextView) mTrackingView.findViewById(R.id.statusbar_buttons_toggle);
             mButtonsToggle.setTextColor(mClockColor);
             mNotificationsToggle.setTextColor(Color.parseColor("#666666"));
             mNotifications.setVisibility(View.GONE);
         }
 
         mSettingsButton = (ImageView) mTrackingView.findViewById(R.id.settingUp);
         mSettingsButton.setOnLongClickListener(mSettingsButtonListener);
 
         mPowerWidgetBottom = (PowerWidgetBottom) mTrackingView.findViewById(R.id.exp_power_stat);
         mPowerWidgetBottom.setGlobalButtonOnClickListener(mPowerClickListener);
         mPowerWidgetBottom.setGlobalButtonOnLongClickListener(mPowerLongClickListener);
 
         mCarrierLabelBottomLayout = (CarrierLabelBottom) mTrackingView.findViewById(R.id.carrierlabel_bottom);
 
         if (mStatusBarTab) {
             mNotificationsToggle.setOnClickListener(mTogglePowerListener);
             mButtonsToggle.setOnClickListener(mToggleNotifListener);
         }
 
         updateStateBackground();
         initializeItemExpand();
         updateColors();
         updateLayout();
         updateCarrierLabel();
         updateCarrierLogo();
 
         mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
 
         // set the inital view visibility
         setAreThereNotifications();
         mDateView.setVisibility(View.INVISIBLE);
         showClock(Settings.System.getInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK, 1) != 0);
         mVelocityTracker = VelocityTracker.obtain();
         mDoNotDisturb = new DoNotDisturb(context);
         totalMemory = 0;
         availableMemory = 0;
         getMemInfo();
     }
 
     private void InitializeSettings(Context context) {
         int defValuesIconSize = context.getResources().getInteger(com.android.internal.R.integer.config_iconsize_default_cyanmobile);
         float mIconSizeval = (float) Settings.System.getInt(context.getContentResolver(),
                 Settings.System.STATUSBAR_ICONS_SIZE, defValuesIconSize);
         int IconSizepx = (int) (mDisplayMetrics.density * mIconSizeval);
         mIconSize = IconSizepx;
         mStatusBarCarrier = Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_CARRIER, 6);
         mStatusBarClock = Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_CLOCK, 1);
         mStatusBarCarrierLogo = Settings.System.getInt(context.getContentResolver(), Settings.System.CARRIER_LOGO, 0);
         mStatusBarReverse = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_REVERSE, 0) == 1);
         mShowCmBatteryStatusBar = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_BATTERY, 0) == 5);
         mShowDate = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_DATE, 0) == 1);
         mShowNotif = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_NOTIF, 1) == 1);
         mShowRam = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_SHOWRAM, 1) == 1);
         mShowIconex = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_SHOWICONEX, 1) == 1);
         mShowCmBatterySideBar = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_BATTERY, 0) == 4);
         mHasSoftButtons = (Settings.System.getInt(context.getContentResolver(), Settings.System.USE_SOFT_BUTTONS, 0) == 1);
         LogoStatusBar = (Settings.System.getInt(context.getContentResolver(), Settings.System.CARRIER_LOGO_STATUS_BAR, 0) == 1);
         shouldTick = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_INTRUDER_ALERT, 1) == 1);
         mClockColor = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_CLOCKCOLOR, defValuesColor()));
         mSettingsColor = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_SETTINGSCOLOR, defValuesColor()));
         mStatusBarTab = ((Settings.System.getInt(context.getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 5) == 4)
                      || (Settings.System.getInt(context.getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 5) == 5));
         mStatusBarGrid = (Settings.System.getInt(context.getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 5) == 3);
         mNaviShow = (Settings.System.getInt(context.getContentResolver(), Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
         mTinyExpanded = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUSBAR_TINY_EXPANDED, 1) == 1);
         mMoreExpanded = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUSBAR_TINY_EXPANDED, 1) == 1);
         autoBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) ==
                     Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
         mBrightnessControl = !autoBrightness && Settings.System.getInt(context.getContentResolver(),
                             Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1;
         IntruderTime = Settings.System.getInt(context.getContentResolver(), Settings.System.STATUS_BAR_INTRUDER_TIME, INTRUDER_ALERT_DECAY_MS);
         mPieEnable = (Settings.System.getInt(context.getContentResolver(), Settings.System.PIE_CONTROL_ENABLE, 1) == 1);
         mStatusBarHidden = (Settings.System.getInt(context.getContentResolver(), Settings.System.STATUSBAR_ICONS_VISIBILITY, 0) == 1);
     }
 
     private void updateStateBackground() {
         ContentResolver resolver = mContext.getContentResolver();
         Resources res = mContext.getResources();
 	// apply transparent status bar drawables
         int transStatusBar = Settings.System.getInt(resolver, Settings.System.TRANSPARENT_STATUS_BAR, 0);
         int statusBarColor = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_COLOR, defValuesColor());
         switch (transStatusBar) {
           case 0 : // theme, leave alone
             mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.statusbar_background));
             break;
           case 1 : // based on ROM
             mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.statusbar_background_black));
             break;
           case 2 : // semi transparent
             mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.statusbar_background_semi));
             break;
           case 3 : // gradient
             mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.statusbar_background_gradient));
             break;
           case 4 : // user defined argb hex color
             mStatusBarView.setBackgroundColor(statusBarColor);
             break;
           case 5 : // transparent
             mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.status_bar_transparent_background));
             break;
           case 6 : // transparent and BackLogo
                mStatusBarView.setBackgroundDrawable(res.getDrawable(R.drawable.status_bar_transparent_background));
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/bc_background"));
                if (savedImage != null) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                    Drawable bgrImage = new BitmapDrawable(bitmapImage);
                    mBackLogoLayout.setBackgroundDrawable(bgrImage);
                } else {
                    mBackLogoLayout.setBackgroundDrawable(res.getDrawable(R.drawable.statusbar_background_semi));
                }
             break;
         }
 
         // apply transparent navi bar drawables
         int transNaviBar = Settings.System.getInt(resolver, Settings.System.TRANSPARENT_NAVI_BAR, 0);
         int naviBarColor = Settings.System.getInt(resolver, Settings.System.NAVI_BAR_COLOR, defValuesColor());
         switch (transNaviBar) {
           case 0 : // theme, leave alone
             mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_background));
             break;
           case 1 : // based on ROM
             mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_background_black));
             break;
           case 2 : // semi transparent
             mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_background_semi));
             break;
           case 3 : // gradient
             mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_background_gradient));
             break;
           case 4 : // user defined argb hex color
             mNaviBarContainer.setBackgroundColor(naviBarColor);
             break;
           case 5 : // transparent
             mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_transparent_background));
             break;
           case 6 : // BackLogo
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/navb_background"));
                if (savedImage != null) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                    Drawable bgrImage = new BitmapDrawable(bitmapImage);
                    mNaviBarContainer.setBackgroundDrawable(bgrImage);
                } else {
                    mNaviBarContainer.setBackgroundDrawable(res.getDrawable(R.drawable.navibar_background_black));
                }
             break;
         }
 
         // apply logo drawables
         if (mStatusBarCarrierLogo == 1) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoLayout.setBackgroundDrawable(bgrImage);
             } else {
                mCarrierLogoLayout.setBackgroundDrawable(res.getDrawable(R.drawable.ic_statusbar_carrier_logos));
             }
         } else if (mStatusBarCarrierLogo == 2) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoCenterLayout.setBackgroundDrawable(bgrImage);
             } else {
                mCarrierLogoCenterLayout.setBackgroundDrawable(res.getDrawable(R.drawable.ic_statusbar_carrier_logos));
             }
         } else if (mStatusBarCarrierLogo == 3) {
             if (LogoStatusBar) {
                Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/lg_background"));
                Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                Drawable bgrImage = new BitmapDrawable(bitmapImage);
                mCarrierLogoLeftLayout.setBackgroundDrawable(bgrImage);
             } else {
                mCarrierLogoLeftLayout.setBackgroundDrawable(res.getDrawable(R.drawable.ic_statusbar_carrier_logos));
             }
         }
     }
 
     private void initializeItemExpand() {
         ContentResolver resolver = mContext.getContentResolver();
         Resources res = mContext.getResources();
         // apply transparent power widget drawables
         int transPowerAndCarrier = Settings.System.getInt(resolver, Settings.System.TRANSPARENT_PWR_CRR, 0);
         int PowerAndCarrierColor = Settings.System.getInt(resolver, Settings.System.PWR_CRR_COLOR, defValuesColor());
         switch (transPowerAndCarrier) {
           case 0 : // theme, leave alone
             mPowerAndCarrier.setBackgroundDrawable(res.getDrawable(R.drawable.title_bar_portrait));
             break;
           case 1 : // user defined argb hex color
             mPowerAndCarrier.setBackgroundColor(PowerAndCarrierColor);
             break;
           case 2 : // transparent
             break;
         }
 
         // apply transparent notification background drawables
         int transNotificationBackground = Settings.System.getInt(resolver, Settings.System.TRANSPARENT_NOTIFICATION_BACKGROUND, 0);
         int notificationBackgroundColor = Settings.System.getInt(resolver, Settings.System.NOTIFICATION_BACKGROUND_COLOR, defValuesColor());
         switch (transNotificationBackground) {
               case 0 : // theme, leave alone
                   mNotificationBackgroundView.setBackgroundDrawable(res.getDrawable(R.drawable.shade_bg));
                   break;
               case 1 : // default based on ROM
                   mNotificationBackgroundView.setBackgroundDrawable(res.getDrawable(R.drawable.shade_bg2));
                   break;
               case 2 : // user defined argb hex color
                   mNotificationBackgroundView.setBackgroundColor(notificationBackgroundColor);
                   break;
               case 3 : // semi transparent
                   mNotificationBackgroundView.setBackgroundDrawable(res.getDrawable(R.drawable.shade_trans_bg));
                   break;
               case 4 : // peeping android background image
                   mNotificationBackgroundView.setBackgroundDrawable(res.getDrawable(R.drawable.status_bar_special));
                   break;
               case 5 : // user selected background image
                   Uri savedImage = Uri.fromFile(new File("/data/data/com.cyanogenmod.cmparts/files/nb_background"));
                   if (savedImage != null) {
                       Bitmap bitmapImage = BitmapFactory.decodeFile(savedImage.getPath());
                       Drawable bgrImage = new BitmapDrawable(bitmapImage);
                       mNotificationBackgroundView.setBackgroundDrawable(bgrImage);
                   } else {
                       mNotificationBackgroundView.setBackgroundDrawable(res.getDrawable(R.drawable.status_bar_special));
                   }
                   break;
         }
 
         // apply transparent tracking view drawables
         int transSettingsButton = Settings.System.getInt(resolver, Settings.System.TRANSPARENT_STS_BTT, 0);
         int SettingsButtonColor = Settings.System.getInt(resolver, Settings.System.STS_BTT_COLOR, defValuesColor());
         switch (transSettingsButton) {
           case 0 : // theme, leave alone
             mSettingsButton.setImageBitmap(getNinePatch(R.drawable.status_bar_close_on, getExpandedWidth(), getStatBarSize(), mContext));
             break;
           case 1 : // user defined argb hex color
             mSettingsButton.setImageBitmap(getNinePatch(R.drawable.status_bar_transparent_background, getExpandedWidth(), getStatBarSize(), mContext));
             mSettingsButton.setBackgroundColor(SettingsButtonColor);
             break;
           case 2 : // transparent
             mSettingsButton.setImageBitmap(getNinePatch(R.drawable.status_bar_transparent_background, getExpandedWidth(), getStatBarSize(), mContext));
             break;
         }
     }
 
     private int defValuesColor() {
         return mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
     }
 
     private void updateColors() {
         ContentResolver resolver = mContext.getContentResolver();
         mItemText = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TEXT, mBlackColor);
         mItemTime = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TIME, defValuesColor());
         mItemTitle = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ITEM_TITLE, defValuesColor());
 
         mDateColor = Settings.System.getInt(resolver, Settings.System.COLOR_DATE, defValuesColor());
         mDateView.setTextColor(mDateColor);
         mClearButton.setColorFilter(mDateColor, Mode.MULTIPLY);
         mSettingsIconButton.setColorFilter(mDateColor, Mode.MULTIPLY);
 
         mButtonText = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_CLEAR_BUTTON, mBlackColor);
 
         if (mStatusBarTab) {
             mCompactClearButton.setTextColor(mButtonText);
         } else {
             if (mMoreExpanded) {
                 mClearOldButton.setTextColor(mButtonText);
             }
         }
 
         mNotifyNone = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_NONE, defValuesColor());
         mNoNotificationsTitle.setTextColor(mNotifyNone);
         if (mStatusBarTab && (mNoNotificationsTitles != null)) {
             mNoNotificationsTitles.setTextColor(mNotifyNone);
         }
         mNotifyTicker = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_TICKER_TEXT, defValuesColor());
         mTickerText.updateColor(mNotifyTicker);
 
         mNotifyLatest = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_LATEST, defValuesColor());
         mLatestTitle.setTextColor(mNotifyLatest);
         if (mStatusBarTab) {
             mLatestTitles.setTextColor(mNotifyLatest);
         }
 
         mNotifyOngoing = Settings.System.getInt(resolver, Settings.System.COLOR_NOTIFICATION_ONGOING, defValuesColor());
         mOngoingTitle.setTextColor(mNotifyOngoing);    
     }
 
     public void resetTextViewColors(View vw) {
         ViewGroup gv = (ViewGroup)vw;
         int ct = gv.getChildCount();
 
         if (ct > 0) {
             for (int i = 0; i < ct; i++) {
                 try {
                     setTextViewColors((TextView)gv.getChildAt(i));
                 } catch (Exception ex) { }
                 try {
                     resetTextViewColors((View)gv.getChildAt(i));
                 } catch (Exception ex) { }
             }
         }
     }
 
     public void setTextViewColors(TextView tc) {
         try {        
             int id = tc.getId();
             switch (id) {
                 case com.android.internal.R.id.text:
                     tc.setTextColor(mItemText);
                     break;
                 case com.android.internal.R.id.time:
                     tc.setTextColor(mItemTime);
                     break;
                 case com.android.internal.R.id.title:
                     tc.setTextColor(mItemTitle);
                     break;
                 default:
                     tc.setTextColor(mItemText);
                     break;
             }
         } catch (Exception e) {}    
     }
 
     private void updateSettings() {
         int changedVal = Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 5);
         // check that it's not 0 to not reset the variable
         // this should be the only place mLastPowerToggle is set
         if (changedVal != 0) {
             mLastPowerToggle = changedVal;
         }
 
     }
 
     private void makeBatterySideBarViewLeft() {
         if (!mShowCmBatterySideBar || mDisplayMetrics == null) {
             return;
         }
 
         CmBatterySideBar batterySideBarLeft = (CmBatterySideBar) View.inflate(this, R.layout.battery_sidebars, null);
 
         int width = getResources().getDimensionPixelSize(R.dimen.battery_sidebar_width);
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 width, mDisplayMetrics.heightPixels,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                 PixelFormat.RGBX_8888);
         lp.gravity = Gravity.LEFT;
         lp.setTitle("Battery SideBarLeft");
 
         WindowManagerImpl.getDefault().addView(batterySideBarLeft, lp);
     }
 
     private void makeBatterySideBarViewRight() {
         if (!mShowCmBatterySideBar || mDisplayMetrics == null) {
             return;
         }
 
         CmBatterySideBar batterySideBarRight = (CmBatterySideBar) View.inflate(this, R.layout.battery_sidebars, null);
 
         int width = getResources().getDimensionPixelSize(R.dimen.battery_sidebar_width);
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 width, mDisplayMetrics.heightPixels,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                 PixelFormat.RGBX_8888);
         lp.gravity = Gravity.RIGHT;
         lp.setTitle("Battery SideBarRight");
 
         WindowManagerImpl.getDefault().addView(batterySideBarRight, lp);
     }
 
     private void updateCarrierLogo() {
         if (mStatusBarCarrierLogo == 1) {
             mCarrierLogoLayout.setVisibility(View.VISIBLE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
         } else if (mStatusBarCarrierLogo == 2) {
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.VISIBLE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
         } else if (mStatusBarCarrierLogo == 3) {
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.VISIBLE);
         } else {
             mCarrierLogoLayout.setVisibility(View.GONE);
             mCarrierLogoCenterLayout.setVisibility(View.GONE);
             mCarrierLogoLeftLayout.setVisibility(View.GONE);
         }
     }
 
     private void updateCarrierLabel() {
         if (mStatusBarCarrier == 5) {
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             // Disable compact carrier when bottom bar is enabled for now
             // till we find a better solution (looks ugly alone at the top)
             if (mBottomBar) {
                 mCompactCarrierLayout.setVisibility(View.GONE);
             } else {
                 mCompactCarrierLayout.setVisibility(View.VISIBLE);
             }
         } else if (mStatusBarCarrier == 0) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             if (mShowIconex) {
                 mCarrierLabelExpLayout.setVisibility(getDataStates(mContext) ? View.VISIBLE : View.GONE);
             }
          } else if (mStatusBarCarrier == 1) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
         } else if (mStatusBarCarrier == 2) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
         } else if (mStatusBarCarrier == 3) {
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.VISIBLE);
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
         } else if (mStatusBarCarrier == 4) {
             mCarrierLabelLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
             if (mBottomBar) {
                 mCarrierLabelBottomLayout.setVisibility(View.GONE);
             } else {
                 mCarrierLabelBottomLayout.setVisibility(View.VISIBLE);
             }
         } else {
             mCarrierLabelLayout.setVisibility(View.VISIBLE);
             mCarrierLabelBottomLayout.setVisibility(View.GONE);
             mCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCenterCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mLeftCarrierLabelStatusBarLayout.setVisibility(View.GONE);
             mCompactCarrierLayout.setVisibility(View.GONE);
         }
     }
 
     private boolean getDataStates(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         return cm.getMobileDataEnabled();
     }
 
     private void updateLayout() {
         if(mTrackingView==null || mCloseView==null || mExpandedView==null)
             return;
 
         // handle trackingview
         mTrackingView.removeView(mCloseView);
         mTrackingView.addView(mCloseView, mBottomBar ? 0 : 1);
 
         // handle expanded view reording for bottom bar
         LinearLayout powerAndCarrier=(LinearLayout) mExpandedView.findViewById(R.id.power_and_carrier);
         PowerWidget power=(PowerWidget) mExpandedView.findViewById(R.id.exp_power_stat);
         PowerWidgetOne powerOne=(PowerWidgetOne) mExpandedView.findViewById(R.id.exp_power_stat_one);
         PowerWidgetTwo powerTwo=(PowerWidgetTwo) mExpandedView.findViewById(R.id.exp_power_stat_two);
         PowerWidgetThree powerThree=(PowerWidgetThree) mExpandedView.findViewById(R.id.exp_power_stat_three);
         PowerWidgetFour powerFour=(PowerWidgetFour) mExpandedView.findViewById(R.id.exp_power_stat_four);
 
         // remove involved views
         powerAndCarrier.removeView(power);
         if (mStatusBarGrid) {
             powerAndCarrier.removeView(powerOne);
             powerAndCarrier.removeView(powerTwo);
             powerAndCarrier.removeView(powerThree);
             powerAndCarrier.removeView(powerFour);
         }
         mExpandedView.removeView(powerAndCarrier);
 
         // readd in right order
         mExpandedView.addView(powerAndCarrier, mBottomBar ? 1 : 0);
         if (mStatusBarGrid) {
             powerAndCarrier.addView(powerFour, mBottomBar && mStatusBarCarrier != 5 ? 1 : 0);
             powerAndCarrier.addView(powerThree, mBottomBar && mStatusBarCarrier != 5 ? 1 : 0);
             powerAndCarrier.addView(powerTwo, mBottomBar && mStatusBarCarrier != 5 ? 1 : 0);
             powerAndCarrier.addView(powerOne, mBottomBar && mStatusBarCarrier != 5 ? 1 : 0);
         }
         powerAndCarrier.addView(power, mBottomBar && mStatusBarCarrier != 5 ? 1 : 0);
 
         // Remove all notification views
         mNotificationLinearLayout.removeAllViews();
         mBottomNotificationLinearLayout.removeAllViews();
 
         // Readd to correct scrollview depending on mBottomBar
         if (mBottomBar) {
             mScrollView.setVisibility(View.GONE);
             mBottomNotificationLinearLayout.addView(mCompactClearButton);
             mBottomNotificationLinearLayout.addView(mNoNotificationsTitle);
             mBottomNotificationLinearLayout.addView(mOngoingTitle);
             mBottomNotificationLinearLayout.addView(mOngoingItems);
             mBottomNotificationLinearLayout.addView(mLatestTitle);
             mBottomNotificationLinearLayout.addView(mLatestItems);
             mBottomScrollView.setVisibility(View.VISIBLE);
         } else {
             mBottomScrollView.setVisibility(View.GONE);
             mNotificationLinearLayout.addView(mNoNotificationsTitle);
             mNotificationLinearLayout.addView(mOngoingTitle);
             mNotificationLinearLayout.addView(mOngoingItems);
             mNotificationLinearLayout.addView(mLatestTitle);
             mNotificationLinearLayout.addView(mLatestItems);
             mNotificationLinearLayout.addView(mCompactClearButton);
             mScrollView.setVisibility(View.VISIBLE);
         }
 
         if ((mStatusBarTab) && (mQuickContainer != null)) {
              if (mQS != null) {
                  mQuickContainer.removeAllViews();
                  mQS.setupQuickSettings();
                  mQuickContainer.requestLayout();
              }
         }
     }
 
     private int getNavBarSize() {
         int defValuesNaviSize = mContext.getResources().getInteger(com.android.internal.R.integer.config_navibarsize_default_cyanmobile);
         float navSizeval = (float) Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_NAVI_SIZE, defValuesNaviSize);
         int navSizepx = (int) (mDisplayMetrics.density * navSizeval);
         return mNaviShow ? navSizepx : 0;
     }
 
     // For small-screen devices (read: phones) that lack hardware navigation buttons
     private void addNavigationBar() {
         mNavigationBarView.reorient();
         WindowManagerImpl.getDefault().addView(mNavigationBarView, getNavigationBarLayoutParams());
     }
 
     private void repositionNavigationBar() {
         mNavigationBarView.reorient();
         WindowManagerImpl.getDefault().updateViewLayout(mNavigationBarView, getNavigationBarLayoutParams());
     }
 
     private WindowManager.LayoutParams getNavigationBarLayoutParams() {
         final int rotation = mDisplay.getRotation();
         Resources res = mContext.getResources();
         final boolean sideways = (mNaviShow && (Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.NAVI_BUTTONS, 1) == 1));
         final boolean anotherways = 
             (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270);
         final int size = getNavBarSize();
         final int oldsize = getStatBarSize();
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 sideways ? (anotherways ? oldsize : size) : 0,
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                     | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                 PixelFormat.TRANSLUCENT);
 
         lp.setTitle("NavigationBar");
         lp.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
         lp.windowAnimations = 0;
 
         return lp;
     }
 
     private int getStatBarSize() {
         int defValuesStatsSize = mContext.getResources().getInteger(com.android.internal.R.integer.config_statbarsize_default_cyanmobile);
         float statSizeval = (float) Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_STATS_SIZE, defValuesStatsSize);
         int statSizepx = (int) (mDisplayMetrics.density * statSizeval);
         return statSizepx;
     }
 
     private boolean showPie() {
         return !mNaviShow && mPieEnable;
     }
 
     private void attachPies() {
         if(showPie()) {
             // Add pie (s), want some slice?
             int gravity = Settings.System.getInt(mContext.getContentResolver(),
                     Settings.System.PIE_GRAVITY, 3);
 
             switch(gravity) {
                 case 0:
                     addPieInLocation(Gravity.LEFT);
                     break;
                 case 1:
                     addPieInLocation(Gravity.TOP);
                     break;
                 case 2:
                     addPieInLocation(Gravity.RIGHT);
                     break;
                 default:
                     addPieInLocation(Gravity.BOTTOM);
                     break;
             }
         } else {
             mPieControlsTrigger = null;
             mPieControlPanel = null;
         }
     }
 
     private void addPieInLocation(int gravity) {
         // Quick navigation bar panel
         PieControlPanel panel = (PieControlPanel) View.inflate(mContext,
                 R.layout.pie_control_panel, null);
 
         // Quick navigation bar trigger area
         View pieControlsTrigger = new View(mContext);
 
         // Store our views for removing / adding
         mPieControlPanel = panel;
         mPieControlsTrigger = pieControlsTrigger;
 
         pieControlsTrigger.setOnTouchListener(new PieControlsTouchListener());
         WindowManagerImpl.getDefault().addView(pieControlsTrigger, getPieTriggerLayoutParams(mContext, gravity));
 
         panel.init(mHandler, this, pieControlsTrigger, gravity);
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                     | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                 PixelFormat.TRANSLUCENT);
         lp.setTitle("PieControlPanel");
         lp.windowAnimations = android.R.style.Animation;
 
         WindowManagerImpl.getDefault().addView(panel, lp);
     }
 
     public static WindowManager.LayoutParams getPieTriggerLayoutParams(Context context, int gravity) {
         final Resources res = context.getResources();
         final float mPieSize = Settings.System.getFloat(context.getContentResolver(),
                 Settings.System.PIE_TRIGGER, 1f);
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
               (gravity == Gravity.TOP || gravity == Gravity.BOTTOM ?
                     ViewGroup.LayoutParams.MATCH_PARENT : (int)(res.getDimensionPixelSize(R.dimen.pie_trigger_height)*mPieSize)),
               (gravity == Gravity.LEFT || gravity == Gravity.RIGHT ?
                     ViewGroup.LayoutParams.MATCH_PARENT : (int)(res.getDimensionPixelSize(R.dimen.pie_trigger_height)*mPieSize)),
               WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                     | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
               PixelFormat.TRANSLUCENT);
         lp.gravity = gravity;
         return lp;
     }
 
     private void addIntruderView() {
         final int height = getStatBarSize();
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 (mTinyExpanded ? getExpandedWidth() : ViewGroup.LayoutParams.MATCH_PARENT),
                (mBottomBar ? getExpandedHeight() : (getExpandedHeight()-getNavBarSize())),
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                     | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                 PixelFormat.TRANSLUCENT);
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.y += height * 1.2; // FIXME
         lp.setTitle("IntruderAlert");
         lp.windowAnimations = R.style.Animations_PopDownMenu_Center;
 
         WindowManagerImpl.getDefault().addView(mIntruderAlertView, lp);
     }
 
     private void updateWidgetsPanel() {
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                 0
                 | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                 | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING,
                 PixelFormat.TRANSLUCENT);
         lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
         lp.setTitle("QwiksWidget");
         lp.windowAnimations = R.style.Animations_PopDownMenu_Center;
 
         WindowManagerImpl.getDefault().addView(mWidgetsPanel, lp);
     }
 
     private void updateRingsPanel() {
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                     0
                     | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                     | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                 PixelFormat.TRANSLUCENT);
         lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
         lp.setTitle("RingPanel");
         lp.windowAnimations = R.style.Animations_PopDownMenu_Center;
         WindowManagerImpl.getDefault().addView(mRingPanel, lp);
     }
 
     protected void addStatusBarView() {
         final int height = getStatBarSize();
 
         final View view = mStatusBarContainer;
 
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 height,
                 WindowManager.LayoutParams.TYPE_STATUS_BAR,
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                     | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING,
                 PixelFormat.TRANSLUCENT);
         lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         lp.setTitle("StatusBar");
         lp.windowAnimations = com.android.internal.R.style.Animation_StatusBar;
 
         WindowManagerImpl.getDefault().addView(view, lp);
 
         //mRecentApps.setupRecentApps();
         mPowerWidget.setupWidget();
         mPowerWidgetBottom.setupWidget();
         mPowerWidgetOne.setupWidget();
         mPowerWidgetTwo.setupWidget();
         mPowerWidgetThree.setupWidget();
         mPowerWidgetFour.setupWidget();
         mMusicControls.setupControls();
         if ((mStatusBarTab) && (mQS != null)) mQS.setupQuickSettings();
     }
 
     public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "addIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                     + " icon=" + icon);
         }
         StatusBarIconView view = new StatusBarIconView(this, slot);
         view.set(icon);
         mStatusIcons.addView(view, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));
     }
 
     public void updateIcon(String slot, int index, int viewIndex,
             StatusBarIcon old, StatusBarIcon icon) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "updateIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                     + " old=" + old + " icon=" + icon);
         }
         StatusBarIconView view = (StatusBarIconView)mStatusIcons.getChildAt(viewIndex);
         view.set(icon);
     }
 
     public void removeIcon(String slot, int index, int viewIndex) {
         if (SPEW_ICONS) {
             Slog.d(TAG, "removeIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex);
         }
         mStatusIcons.removeViewAt(viewIndex);
     }
 
     public void addNotification(IBinder key, StatusBarNotification notification) {
 
         boolean shouldTicker = true;
         if (notification.notification.fullScreenIntent != null) {
             shouldTicker = false;
             Slog.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
             try {
                 notification.notification.fullScreenIntent.send();
             } catch (PendingIntent.CanceledException e) {
             }
         }
 
         StatusBarIconView iconView = addNotificationViews(key, notification);
         StatusBarIconView iconViewIntruder = mIntruderAlertView.addNotificationViews(key, notification);
         if ((iconView == null) || (iconViewIntruder == null)) return;
 
         if (shouldTicker && mShowNotif) {
             if (!shouldTick) {
                 tick(notification);
             } else {
                 IntroducerView();
             }
         }
 
         // Recalculate the position of the sliding windows and the titles.
         setAreThereNotifications();
         mIntruderAlertView.setAreThereNotifications();
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
     }
 
     public void IntroducerView() {
         mIntruderAlertView.updateLayout();
 
         mHandler.sendEmptyMessage(MSG_SHOW_INTRUDER);
         mHandler.removeMessages(MSG_HIDE_INTRUDER);
         mHandler.sendEmptyMessageDelayed(MSG_HIDE_INTRUDER, IntruderTime);
     }
 
     public void toggleShowIntroducerView() {
         mIntruderAlertView.updateLayout();
 
         mHandler.sendEmptyMessage(MSG_SHOW_INTRUDER);
     }
 
     public void toggleHideIntroducerViewTime() {
         mIntruderAlertView.updateLayout();
 
         mHandler.removeMessages(MSG_HIDE_INTRUDER);
         mHandler.sendEmptyMessageDelayed(MSG_HIDE_INTRUDER, IntruderTime);
     }
 
     public void toggleHideIntroducerView() {
         mIntruderAlertView.updateLayout();
 
         mHandler.sendEmptyMessage(MSG_HIDE_INTRUDER);
     }
 
     public void updateNotification(IBinder key, StatusBarNotification notification) {
         mIntruderAlertView.updateNotification(key, notification);
         NotificationData oldList;
         int oldIndex = mOngoing.findEntry(key);
         if (oldIndex >= 0) {
             oldList = mOngoing;
         } else {
             oldIndex = mLatest.findEntry(key);
             if (oldIndex < 0) {
                 Slog.w(TAG, "updateNotification for unknown key: " + key);
                 return;
             }
             oldList = mLatest;
         }
         setNotifNew(true);
         mNotifData = getNotifications(oldList);
         final NotificationData.Entry oldEntry = oldList.getEntryAt(oldIndex);
         final StatusBarNotification oldNotification = oldEntry.notification;
         final RemoteViews oldContentView = oldNotification.notification.contentView;
 
         final RemoteViews contentView = notification.notification.contentView;
 
         if (false) {
             Slog.d(TAG, "old notification: when=" + oldNotification.notification.when
                     + " ongoing=" + oldNotification.isOngoing()
                     + " expanded=" + oldEntry.expanded
                     + " contentView=" + oldContentView);
             Slog.d(TAG, "new notification: when=" + notification.notification.when
                     + " ongoing=" + oldNotification.isOngoing()
                     + " contentView=" + contentView);
         }
 
         // Can we just reapply the RemoteViews in place?  If when didn't change, the order
         // didn't change.
         if (notification.notification.when == oldNotification.notification.when
                 && notification.isOngoing() == oldNotification.isOngoing()
                 && oldEntry.expanded != null
                 && contentView != null && oldContentView != null
                 && contentView.getPackage() != null
                 && oldContentView.getPackage() != null
                 && oldContentView.getPackage().equals(contentView.getPackage())
                 && oldContentView.getLayoutId() == contentView.getLayoutId()) {
             if (SPEW) Slog.d(TAG, "reusing notification");
             oldEntry.notification = notification;
             try {
                 // Reapply the RemoteViews
                 contentView.reapply(this, oldEntry.content);
                 // update the contentIntent
                 final PendingIntent contentIntent = notification.notification.contentIntent;
                 if (contentIntent != null) {
                     oldEntry.content.setOnClickListener(makeLauncher(contentIntent,
                                 notification.pkg, notification.tag, notification.id));
                 }
                 // Update the icon.
                 final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                         notification.notification.icon, notification.notification.iconLevel,
                         notification.notification.number);
                 setLauncherNotif(notification);
                 if (!oldEntry.icon.set(ic)) {
                     handleNotificationError(key, notification, "Couldn't update icon: " + ic);
                     return;
                 }
             }
             catch (RuntimeException e) {
                 // It failed to add cleanly.  Log, and remove the view from the panel.
                 Slog.w(TAG, "Couldn't reapply views for package " + contentView.getPackage(), e);
                 removeNotificationViews(key);
                 addNotificationViews(key, notification);
                 setLauncherNotif(notification);
             }
         } else {
             if (SPEW) Slog.d(TAG, "not reusing notification");
             removeNotificationViews(key);
             addNotificationViews(key, notification);
             setLauncherNotif(notification);
         }
 
         // Restart the ticker if it's still running
         if (notification.notification.tickerText != null
                 && !TextUtils.equals(notification.notification.tickerText,
                     oldEntry.notification.notification.tickerText) && mShowNotif) {
             if (!shouldTick) {
                 tick(notification);
             } else {
                 toggleHideIntroducerView();
                 IntroducerView();
             }
         }
 
         // Recalculate the position of the sliding windows and the titles.
         setAreThereNotifications();
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
     }
 
     public void removeNotification(IBinder key) {
         StatusBarNotification old = removeNotificationViews(key);
         StatusBarNotification oldIntruder = mIntruderAlertView.removeNotificationViews(key);
         if (SPEW) Slog.d(TAG, "removeNotification key=" + key + " old=" + old);
 
         if (old != null || oldIntruder != null) {
             // Cancel the ticker if it's still running
             mTicker.removeEntry(old);
 
             // Recalculate the position of the sliding windows and the titles.
             setAreThereNotifications();
             mIntruderAlertView.setAreThereNotifications();
             updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
         }
     }
 
     private int chooseIconIndex(boolean isOngoing, int viewIndex) {
         final int latestSize = mLatest.size();
         if (isOngoing) {
             return latestSize + (mOngoing.size() - viewIndex);
         } else {
             return latestSize - viewIndex;
         }
     }
 
     View[] makeNotificationView(final IBinder key, final StatusBarNotification notification, ViewGroup parent) {
         Notification n = notification.notification;
         RemoteViews remoteViews = n.contentView;
         if (remoteViews == null) {
             return null;
         }
 
         // create the row view
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         LatestItemContainer row = (LatestItemContainer) inflater.inflate(R.layout.status_bar_latest_event, parent, false);
         if ((n.flags & Notification.FLAG_ONGOING_EVENT) == 0 && (n.flags & Notification.FLAG_NO_CLEAR) == 0) {
             row.setOnSwipeCallback(mTouchDispatcher, new Runnable() {
                 @Override
                 public void run() {
                     try {
                         mBarService.onNotificationClear(notification.pkg, notification.tag, notification.id);
                         setClearLauncherNotif(notification.pkg);
                         NotificationData list = mLatest;
                         int index = mLatest.findEntry(key);
                         if (index < 0) {
                             list = mOngoing;
                             index = mOngoing.findEntry(key);
                         }
                         if (index >= 0) {
                             list.getEntryAt(index).cancelled = true;
                         }
                     } catch (RemoteException e) {
                         // Skip it, don't crash.
                     }
                 }
             });
         }
 
         // bind the click event to the content area
         ViewGroup content = (ViewGroup) row.findViewById(R.id.content);
         content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         content.setOnFocusChangeListener(mFocusChangeListener);
         PendingIntent contentIntent = n.contentIntent;
         if (contentIntent != null) {
             content.setOnClickListener(makeLauncher(contentIntent, notification.pkg,
                         notification.tag, notification.id));
         }
 
         View expanded = null;
         Exception exception = null;
         try {
             expanded = remoteViews.apply(this, content);
         }
         catch (RuntimeException e) {
             exception = e;
         }
         if (expanded == null) {
             String ident = notification.pkg + "/0x" + Integer.toHexString(notification.id);
             Slog.e(TAG, "couldn't inflate view for notification " + ident, exception);
             return null;
         } else {
             resetTextViewColors(expanded);
             content.addView(expanded);
             row.setDrawingCacheEnabled(true);
         }
 
         return new View[] { row, content, expanded };
     }
 
     StatusBarIconView addNotificationViews(IBinder key, StatusBarNotification notification) {
         NotificationData list;
         ViewGroup parent;
         final boolean isOngoing = notification.isOngoing();
         if (isOngoing) {
             list = mOngoing;
             parent = mOngoingItems;
         } else {
             list = mLatest;
             parent = mLatestItems;
         }
         // Construct the expanded view.
         final View[] views = makeNotificationView(key, notification, parent);
         if (views == null) {
             handleNotificationError(key, notification, "Couldn't expand RemoteViews for: "
                     + notification);
             return null;
         }
         final View row = views[0];
         final View content = views[1];
         final View expanded = views[2];
         // Construct the icon.
         final StatusBarIconView iconView = new StatusBarIconView(this,
                 notification.pkg + "/0x" + Integer.toHexString(notification.id));
         final StatusBarIcon ic = new StatusBarIcon(notification.pkg, notification.notification.icon,
                     notification.notification.iconLevel, notification.notification.number);
         if (!iconView.set(ic)) {
             handleNotificationError(key, notification, "Coulding create icon: " + ic);
             return null;
         }
         // Add the expanded view.
         final int viewIndex = list.add(key, notification, row, content, expanded, iconView);
         parent.addView(row, viewIndex);
         // Add the icon.
         final int iconIndex = chooseIconIndex(isOngoing, viewIndex);
         mNotificationIcons.addView(iconView, iconIndex);
         setNotifNew(true);
         mNotifData = getNotifications(list);
         setLauncherNotif(notification);
         return iconView;
     }
 
     StatusBarNotification removeNotificationViews(IBinder key) {
         NotificationData.Entry entry = mOngoing.remove(key);
         if (entry == null) {
             entry = mLatest.remove(key);
             if (entry == null) {
                 Slog.w(TAG, "removeNotification for unknown key: " + key);
                 return null;
             }
         }
         // Remove the expanded view.
         ((ViewGroup)entry.row.getParent()).removeView(entry.row);
         // Remove the icon.
         ((ViewGroup)entry.icon.getParent()).removeView(entry.icon);
 
         if (entry.cancelled) {
             if (!mOngoing.hasClearableItems() && !mLatest.hasClearableItems()) {
                 setNotifNew(false);
                 animateCollapse();
             }
         }
 
         return entry.notification;
     }
 
     private void setLauncherNotif(StatusBarNotification notification) {
          Intent intentd = new Intent();
          intentd.setAction("org.adw.launcher.counter.SEND");
          intentd.putExtra("PNAME", notification.pkg);
          intentd.putExtra("COUNT", notification.notification.number);
          mContext.sendBroadcast(intentd);
     }
 
     public void setClearLauncherNotif(String pkg) {
          Intent intentdc = new Intent();
          intentdc.setAction("org.adw.launcher.counter.SEND");
          intentdc.putExtra("PNAME", pkg);
          intentdc.putExtra("COUNT", 0);
          mContext.sendBroadcast(intentdc);
     }
 
     private NotificationData getNotifications(NotificationData list) {
         if (mPieControlPanel != null) {
             mPieControlPanel.setNotifications(list);
         }
         return list;
     }
 
     private void setNotifNew(boolean notifnew) {
         if (mPieControlPanel != null) {
             mPieControlPanel.setNotifNew(notifnew);
         }
         if (mNavigationBarView != null) {
             mNavigationBarView.setNotifNew(notifnew);
         }
     }
 
     private void setAreThereNotifications() {
         boolean ongoing = mOngoing.hasVisibleItems();
         boolean latest = mLatest.hasVisibleItems();
 
         // (no ongoing notifications are clearable)
         if (mLatest.hasClearableItems()) {
             if (mStatusBarTab) {
                 mCompactClearButton.setVisibility(View.VISIBLE); 
                 mClearButton.setVisibility(View.INVISIBLE);
                 mClearOldButton.setVisibility(View.GONE);
             } else {
                 mCompactClearButton.setVisibility(View.GONE);
                 if (mMoreExpanded) {
                     mClearButton.setVisibility(View.VISIBLE);
                 } else {
                     mClearOldButton.setVisibility(View.VISIBLE);
                 }
             }
             setNotifNew(true);
         } else {
             mCompactClearButton.setVisibility(View.GONE);
             mClearButton.setVisibility(View.INVISIBLE);
             mClearOldButton.setVisibility(View.GONE);
             setNotifNew(false);
         }
 
         mOngoingTitle.setVisibility(ongoing ? View.VISIBLE : View.GONE);
         mLatestTitle.setVisibility(latest ? View.VISIBLE : View.GONE);
         if (mStatusBarTab) {
             mLatestTitles.setVisibility(latest ? View.VISIBLE : View.GONE);
         }
 
         if (latest) {
             if (mStatusBarTab) {
                 mNoNotificationsTitles.setVisibility(View.GONE);
             }
         } else {
             if (mStatusBarTab) {
                 mNoNotificationsTitles.setVisibility(View.VISIBLE);
             }
         }
         if (ongoing || latest) {
             mNoNotificationsTitle.setVisibility(View.GONE);
         } else {
             mNoNotificationsTitle.setVisibility(View.VISIBLE);
         }
     }
 
     public void showClock(boolean show) {
        if (mStatusBarView != null) {
           Clock clock = (Clock) mStatusBarView.findViewById(R.id.clock);
           if (clock != null) {
               clock.VisibilityChecks(show);
               clock.setOnClickListener(mCarrierButtonListener);
           }
           CenterClock centerClo = (CenterClock) mStatusBarView.findViewById(R.id.centerClo);
           if (centerClo != null) {
               centerClo.VisibilityChecks(show);
               centerClo.setOnClickListener(mCarrierButtonListener);
           }
           LeftClock clockLeft = (LeftClock) mStatusBarView.findViewById(R.id.clockLe);
           if (clockLeft != null) {
               clockLeft.VisibilityChecks(show);
               clockLeft.setOnClickListener(mCarrierButtonListener);
           }
           mHidingAllViews(show);
        }
     }
 
     public void showNaviBar(boolean show) {
       if (mNavigationBarView != null) {
           mNavigationBarView.setNaviVisible(show);
       }
     }
 
     private void mHidingAllViews(boolean show) {
         if (mStatusBarHidden) {
             mIcons.setVisibility(show ? View.VISIBLE : View.GONE);
         }
         if (mStatusBarCarrier == 1) {
             mCarrierLabelStatusBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         } else if (mStatusBarCarrier == 2) {
             mCenterCarrierLabelStatusBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         } else if (mStatusBarCarrier == 3) {
             mLeftCarrierLabelStatusBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         }
         if (mStatusBarCarrierLogo == 1) {
             mCarrierLogoLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         } else if (mStatusBarCarrierLogo == 2) {
             mCarrierLogoCenterLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         } else if (mStatusBarCarrierLogo == 3) {
             mCarrierLogoLeftLayout.setVisibility(show ? View.VISIBLE : View.GONE);
         }
         if (mStatusBarHidden && mShowCmBatteryStatusBar) {
             mCmBatteryStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
         }
     }
 
     /**
      * State is one or more of the DISABLE constants from StatusBarManager.
      */
     public void disable(int state) {
         final int old = mDisabled;
         final int diff = state ^ old;
         mDisabled = state;
 
         if ((diff & StatusBarManager.DISABLE_CLOCK) != 0) {
             boolean show = (state & StatusBarManager.DISABLE_CLOCK) == 0;
             Slog.d(TAG, "DISABLE_CLOCK: " + (show ? "no" : "yes"));
             showClock(show);
         }
         if ((diff & StatusBarManager.DISABLE_NAVIGATION) != 0) {
             boolean show = (state & StatusBarManager.DISABLE_NAVIGATION) == 0;
             Slog.d(TAG, "DISABLE_NAVIGATION: " + (show ? "no" : "yes"));
             showNaviBar(show);
         }
         if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
             if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_EXPAND: yes");
                 animateCollapse();
             }
         }
         if (((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0)) {
             if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                 if (mTicking) {
                     mTicker.halt();
                 } else {
                     setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                 }
             } else {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                 if (!mExpandedVisible) {
                     setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                 }
             }
         } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
             if (mTicking && (state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                 if (SPEW) Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: yes");
                 mTicker.halt();
             }
         }
     }
 
     /**
      * All changes to the status bar and notifications funnel through here and are batched.
      */
     private class H extends Handler {
         public void handleMessage(Message m) {
             switch (m.what) {
                 case MSG_ANIMATE:
                     doAnimation();
                     break;
                 case MSG_ANIMATE_REVEAL:
                     doRevealAnimation();
                     break;
                 case MSG_SHOW_INTRUDER:
                     setIntruderAlertVisibility(true);
                     break;
                 case MSG_HIDE_INTRUDER:
                     setIntruderAlertVisibility(false);
                     break;
                 case MSG_SHOW_WIDGETS_PANEL:
                     if (mWidgetsPanel != null) {
                         mWidgetsPanel.show(true, false);
                     }
                     break;
                 case MSG_HIDE_WIDGETS_PANEL:
                     if (mWidgetsPanel != null && mWidgetsPanel.isShowing()) {
                         mWidgetsPanel.show(false, false);
                     }
                 case MSG_SHOW_RING_PANEL:
                     if (mRingPanel != null) {
                         mRingPanel.show(true);
                     }
                     break;
                 case MSG_HIDE_RING_PANEL:
                     if (mRingPanel != null && mRingPanel.isShowing()) {
                         mRingPanel.show(false);
                     }
                     break;
             }
         }
     }
 
     private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
             // Because 'v' is a ViewGroup, all its children will be (un)selected
             // too, which allows marqueeing to work.
             v.setSelected(hasFocus);
         }
     };
 
     private void makeExpandedVisible() {
         if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
         if (mExpandedVisible) {
             return;
         }
         mExpandedVisible = true;
         visibilityChanged(true);
 
         //mRecentApps.setupRecentApps();
         mPowerWidget.updateAllButtons();
         mPowerWidgetBottom.updateAllButtons();
         mPowerWidgetOne.updateAllButtons();
         mPowerWidgetTwo.updateAllButtons();
         mPowerWidgetThree.updateAllButtons();
         mPowerWidgetFour.updateAllButtons();
         mMusicControls.updateControls();
 
         updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
         mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
         mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         mExpandedView.requestFocus(View.FOCUS_FORWARD);
         mTrackingView.setVisibility(View.VISIBLE);
         mExpandedView.setVisibility(View.VISIBLE);
 
         mCenterClockex.setVisibility(mMoreExpanded ? View.VISIBLE : View.GONE);
         mCenterIconex.setVisibility(mShowIconex ? View.VISIBLE : View.GONE);
         mAvalMemLayout.setVisibility(mShowRam ? View.VISIBLE : View.GONE);
 
         if (!mTicking) {
             setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
 	    setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
             setAllViewVisibility(false, com.android.internal.R.anim.fade_out);
         }
     }
 
     public void animateExpand() {
         toggleHideQwikWidgets();
         toggleHideRingPanel();
         if (SPEW) Slog.d(TAG, "Animate expand: expanded=" + mExpanded);
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return ;
         }
         if (mExpanded) {
             return;
         }
 
         prepareTracking(0, true);
         performFling(0, 2000.0f, true);
     }
 
     public void animateCollapse() {
         toggleHideQwikWidgets();
         toggleHideRingPanel();
         if (mPieControlPanel != null) {
             mPieControlPanel.resetStatus(-1);
         }
         if (mIntruderAlertView.getVisibility() == View.VISIBLE) {
             toggleHideIntroducerView();
         }
         if (SPEW) {
             Slog.d(TAG, "animateCollapse(): mExpanded=" + mExpanded
                     + " mExpandedVisible=" + mExpandedVisible
                     + " mExpanded=" + mExpanded
                     + " mAnimating=" + mAnimating
                     + " mAnimY=" + mAnimY
                     + " mAnimVel=" + mAnimVel);
         }
 
         if (!mExpandedVisible) {
             return;
         }
 
         int y;
         if (mAnimating) {
             y = (int)mAnimY;
         } else {
             if(mBottomBar)
                 y = 0;
             else
                 y = mDisplayMetrics.heightPixels-1;
         }
         // Let the fling think that we're open so it goes in the right direction
         // and doesn't try to re-open the windowshade.
         mExpanded = true;
         prepareTracking(y, false);
         performFling(y, -2000.0f, true);
     }
 
     void performExpand() {
         if (SPEW) Slog.d(TAG, "performExpand: mExpanded=" + mExpanded);
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return ;
         }
         if (mExpanded) {
             return;
         }
 
         mExpanded = true;
         mStatusBarView.updateQuickNaImage();
         makeExpandedVisible();
         updateExpandedViewPos(EXPANDED_FULL_OPEN);
         if (false) postStartTracing();
     }
 
     void performCollapse() {
         if (SPEW) Slog.d(TAG, "performCollapse: mExpanded=" + mExpanded
                 + " mExpandedVisible=" + mExpandedVisible
                 + " mTicking=" + mTicking);
 
         if (!mExpandedVisible) {
             return;
         }
         mExpandedVisible = false;
         visibilityChanged(false);
         mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
         mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         mTrackingView.setVisibility(View.GONE);
         mExpandedView.setVisibility(View.GONE);
 
         if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
             setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
         }
         if (mDateView.getVisibility() == View.VISIBLE) {
             setDateViewVisibility(false, com.android.internal.R.anim.fade_out);
             setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
         } else if (mDateView.getVisibility() == View.INVISIBLE || mDateView.getVisibility() == View.GONE) {
             setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
         }
         if (!mExpanded) {
             return;
         }
         mExpanded = false;
         mStatusBarView.updateQuickNaImage();
     }
 
     private void doAnimation() {
         if (mAnimating) {
             if (SPEW) Slog.d(TAG, "doAnimation");
             if (SPEW) Slog.d(TAG, "doAnimation before mAnimY=" + mAnimY);
             incrementAnim();
             if (SPEW) Slog.d(TAG, "doAnimation after  mAnimY=" + mAnimY);
             if ((!mBottomBar && mAnimY >= mDisplayMetrics.heightPixels-1) || (mBottomBar && mAnimY <= 0)) {
                 if (SPEW) Slog.d(TAG, "Animation completed to expanded state.");
                 mAnimating = false;
                 updateExpandedViewPos(EXPANDED_FULL_OPEN);
                 performExpand();
             }
             else if ((!mBottomBar && mAnimY < getStatBarSize())
                     || (mBottomBar && mAnimY > (mDisplayMetrics.heightPixels-(mShowDate ? getStatBarSize() : 0)))) {
                 if (SPEW) Slog.d(TAG, "Animation completed to collapsed state.");
                 mAnimating = false;
                 if(mBottomBar)
                     updateExpandedViewPos(mDisplayMetrics.heightPixels);
                 else
                     updateExpandedViewPos(0);
                 performCollapse();
             }
             else {
                 updateExpandedViewPos((int)mAnimY);
                 mCurAnimationTime += ANIM_FRAME_DURATION;
                 mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
             }
         }
     }
 
     private void stopTracking() {
         mTracking = false;
         mVelocityTracker.clear();
     }
 
     private void incrementAnim() {
         long now = SystemClock.uptimeMillis();
         float t = ((float)(now - mAnimLastTime)) / 1000;            // ms -> s
         final float y = mAnimY;
         final float v = mAnimVel;                                   // px/s
         final float a = mAnimAccel;                                 // px/s/s
         if(mBottomBar)
             mAnimY = y - (v*t) - (0.5f*a*t*t);                          // px
         else
             mAnimY = y + (v*t) + (0.5f*a*t*t);                          // px
         mAnimVel = v + (a*t);                                       // px/s
         mAnimLastTime = now;                                        // ms
         //Slog.d(TAG, "y=" + y + " v=" + v + " a=" + a + " t=" + t + " mAnimY=" + mAnimY
         //        + " mAnimAccel=" + mAnimAccel);
     }
 
     private void doRevealAnimation() {
         int h = mCloseView.getHeight() + (mShowDate ? getStatBarSize() : 0);
 
         if(mBottomBar)
             h = mDisplayMetrics.heightPixels - (mShowDate ? getStatBarSize() : 0);
         if (mAnimatingReveal && mAnimating &&
                 ((mBottomBar && mAnimY > h) || (!mBottomBar && mAnimY < h))) {
             incrementAnim();
             if ((mBottomBar && mAnimY <= h) || (!mBottomBar && mAnimY >=h)) {
                 mAnimY = h;
                 updateExpandedViewPos((int)mAnimY);
             } else {
                 updateExpandedViewPos((int)mAnimY);
                 mCurAnimationTime += ANIM_FRAME_DURATION;
                 mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL), mCurAnimationTime);
             }
         }
     }
 
     private void prepareTracking(int y, boolean opening) {
 
         updateExpandedHeight();
 
         mTracking = true;
         mVelocityTracker.clear();
         if (opening) {
             mAnimAccel = 2000.0f;
             mAnimVel = 200;
             mAnimY = mBottomBar ? mDisplayMetrics.heightPixels : (mShowDate ? getStatBarSize() : mDisplayMetrics.heightPixels);
             updateExpandedViewPos((int)mAnimY);
             mAnimating = true;
             mAnimatingReveal = true;
             mHandler.removeMessages(MSG_ANIMATE);
             mHandler.removeMessages(MSG_ANIMATE_REVEAL);
             long now = SystemClock.uptimeMillis();
             mAnimLastTime = now;
             mCurAnimationTime = now + ANIM_FRAME_DURATION;
             mAnimating = true;
             mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL), mCurAnimationTime);
             makeExpandedVisible();
         } else {
             // it's open, close it?
             if (mAnimating) {
                 mAnimating = false;
                 mHandler.removeMessages(MSG_ANIMATE);
             }
             updateExpandedViewPos(y + mViewDelta);
         }
     }
 
     private void performFling(int y, float vel, boolean always) {
         mAnimatingReveal = false;
 
         mAnimY = y;
         mAnimVel = vel;
 
         //Slog.d(TAG, "starting with mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel);
 
         if (mExpanded) {
             if (!always &&
                     ((mBottomBar && (vel < -200.0f || (y < 25 && vel < 200.0f))) ||
                     (!mBottomBar && (vel >  200.0f || (y > (mDisplayMetrics.heightPixels-25) && vel > -200.0f))))) {
                 // We are expanded, but they didn't move sufficiently to cause
                 // us to retract.  Animate back to the expanded position.
                 mAnimAccel = 2000.0f;
                 if (vel < 0) {
                     mAnimVel *= -1;
                 }
             }
             else {
                 // We are expanded and are now going to animate away.
                 mAnimAccel = -2000.0f;
                 if (vel > 0) {
                     mAnimVel *= -1;
                 }
             }
         } else {
             if (always
                     || ( mBottomBar && (vel < -200.0f || (y < (mDisplayMetrics.heightPixels/2) && vel <  200.0f)))
                     || (!mBottomBar && (vel >  200.0f || (y > (mDisplayMetrics.heightPixels/2) && vel > -200.0f)))) {
                 // We are collapsed, and they moved enough to allow us to
                 // expand.  Animate in the notifications.
                 mAnimAccel = 2000.0f;
                 if (vel < 0) {
                     mAnimVel *= -1;
                 }
             }
             else {
                 // We are collapsed, but they didn't move sufficiently to cause
                 // us to retract.  Animate back to the collapsed position.
                 mAnimAccel = -2000.0f;
                 if (vel > 0) {
                     mAnimVel *= -1;
                 }
             }
         }
         //Slog.d(TAG, "mAnimY=" + mAnimY + " mAnimVel=" + mAnimVel
         //        + " mAnimAccel=" + mAnimAccel);
 
         long now = SystemClock.uptimeMillis();
         mAnimLastTime = now;
         mCurAnimationTime = now + ANIM_FRAME_DURATION;
         mAnimating = true;
         mHandler.removeMessages(MSG_ANIMATE);
         mHandler.removeMessages(MSG_ANIMATE_REVEAL);
         mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
         stopTracking();
     }
 
     private void adjustBrightness(int x) {
         float screen_width = (float)(mContext.getResources().getDisplayMetrics().widthPixels);
         float raw = ((float) x) / screen_width;
         int minBrightness = 4;
         // Add a padding to the brightness control on both sides to
         // make it easier to reach min/max brightness
         float padded = Math.min(1.0f - BRIGHTNESS_CONTROL_PADDING,
                 Math.max(BRIGHTNESS_CONTROL_PADDING, raw));
         float value = (padded - BRIGHTNESS_CONTROL_PADDING) / (1 - (2.0f * BRIGHTNESS_CONTROL_PADDING));
         int newBrightness = minBrightness + (int) Math.round(value * (android.os.Power.BRIGHTNESS_ON - minBrightness));
         newBrightness = Math.min(newBrightness, android.os.Power.BRIGHTNESS_ON);
         newBrightness = Math.max(newBrightness, minBrightness);
 
        try {
             IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
             if (power != null) {
                 power.setBacklightBrightness(newBrightness);
                 Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newBrightness);
                 if (mBrightnessPanel == null) {
                     mBrightnessPanel = new BrightnessPanel(mContext);
                 }
                 mBrightnessPanel.postBrightnessChanged(newBrightness, android.os.Power.BRIGHTNESS_ON);
             }
         } catch (RemoteException e) {
             Slog.w(TAG, "Setting Brightness failed: " + e);
         }
     }
 
     private void brightnessControl(MotionEvent event) {
         if (mBrightnessControl) {
             final int statusBarSize = getStatBarSize();
             final int action = event.getAction();
             final int x = (int)event.getRawX();
             final int y = (int)event.getRawY();
             if (action == MotionEvent.ACTION_DOWN) {
                 mLinger = 0;
                 mInitialTouchX = x;
                 mInitialTouchY = y;
                 mHandler.removeCallbacks(mLongPressBrightnessChange);
                 mHandler.postDelayed(mLongPressBrightnessChange, BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT);
             } else if (action == MotionEvent.ACTION_MOVE) {
                 int minY = statusBarSize + mCloseView.getHeight();
                 if (mBottomBar) minY = mDisplayMetrics.heightPixels - statusBarSize - mCloseView.getHeight();
                 if ((!mBottomBar && y < minY) ||
                         (mBottomBar && y > minY)) {
                     mVelocityTracker.computeCurrentVelocity(1000);
                     float yVel = mVelocityTracker.getYVelocity();
                     if (yVel < 0) {
                         yVel = -yVel;
                     }
                     if (yVel < 50.0f) {
                         if (mLinger > BRIGHTNESS_CONTROL_LINGER_THRESHOLD) {
                             adjustBrightness(x);
                         } else {
                             mLinger++;
                         }
                     }
                     int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                     if (Math.abs(x - mInitialTouchX) > touchSlop || Math.abs(y - mInitialTouchY) > touchSlop) {
                         mHandler.removeCallbacks(mLongPressBrightnessChange);
                     }
                 } else {
                     mHandler.removeCallbacks(mLongPressBrightnessChange);
                 }
             } else if (action == MotionEvent.ACTION_UP) {
                 mHandler.removeCallbacks(mLongPressBrightnessChange);
                 mLinger = 0;
             }
         }
     }
 
     boolean interceptTouchEvent(MotionEvent event) {
         if (SPEW) {
             Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
                 + mDisabled);
         }
 
         final int statusBarSize = getStatBarSize();
         final int hitSize = (statusBarSize * 2);
         final int y = (int)event.getRawY();
         final int x = (int)event.getRawX();
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             if (!mExpanded) {
                 mViewDelta = mBottomBar ? mDisplayMetrics.heightPixels - y : statusBarSize - y;
             } else {
                 mTrackingView.getLocationOnScreen(mAbsPos);
                 mViewDelta = mAbsPos[1] + (mBottomBar ? 0 : mTrackingView.getHeight()) - y;
             }
         }
 
         brightnessControl(event);
 
         if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
             return false;
         }
 
         if (!mTrackingView.mIsAttachedToWindow) {
             return false;
         }
 
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             if ((!mBottomBar && ((!mExpanded && y < hitSize) || ( mExpanded && y > (mDisplayMetrics.heightPixels-hitSize)))) ||
                  (mBottomBar && (( mExpanded && y < hitSize) || (!mExpanded && y > (mDisplayMetrics.heightPixels-hitSize))))) {
 
                 // We drop events at the edge of the screen to make the windowshade come
                 // down by accident less, especially when pushing open a device with a keyboard
                 // that rotates (like g1 and droid)
 
                 final int edgeBorder = mEdgeBorder;
                 int edgeLeft = mButtonsLeft ? mStatusBarView.getSoftButtonsWidth() : 0;
                 int edgeRight = mButtonsLeft ? 0 : mStatusBarView.getSoftButtonsWidth();
 
                 final int w = mDisplayMetrics.widthPixels;
                 final int deadLeft = w / 2 - w / 4;  // left side of the dead zone
                 final int deadRight = w / 2 + w / 4; // right side of the dead zone
 
                 boolean expandedHit = (mExpanded && (x >= edgeBorder && x < w - edgeBorder));
                 boolean collapsedHit = (!mExpanded && (x >= edgeBorder + edgeLeft && x < w - edgeBorder - edgeRight)
                                 && (!mDeadZone || mDeadZone && (x < deadLeft || x > deadRight)));
 
                 if (expandedHit || collapsedHit) {
                     prepareTracking(y, !mExpanded);// opening if we're not already fully visible
                     trackMovement(event);
                 }
             }
         } else if (mTracking) {
             trackMovement(event);
             int minY = statusBarSize + mCloseView.getHeight();
             if (mBottomBar)
                 minY = mDisplayMetrics.heightPixels - statusBarSize - mCloseView.getHeight();
             if (event.getAction() == MotionEvent.ACTION_MOVE) {
                 if ((!mBottomBar && mAnimatingReveal && y < minY) || (mBottomBar && mAnimatingReveal && y > minY)) {
                      // nothing
                 } else  {
                     mAnimatingReveal = false;
                     updateExpandedViewPos(y + (mBottomBar ? -mViewDelta : mViewDelta));
                 }
                 mSettingsButton.setColorFilter(mSettingsColor, Mode.MULTIPLY);
                 getMemInfo();
             } else if (event.getAction() == MotionEvent.ACTION_UP) {
                 mVelocityTracker.computeCurrentVelocity(1000);
 
                 float yVel = mVelocityTracker.getYVelocity();
                 boolean negative = yVel < 0;
 
                 float xVel = mVelocityTracker.getXVelocity();
                 if (xVel < 0) {
                     xVel = -xVel;
                 }
                 if (xVel > 150.0f) {
                     xVel = 150.0f; // limit how much we care about the x axis
                 }
 
                 float vel = (float)Math.hypot(yVel, xVel);
                 if (negative) {
                     vel = -vel;
                 }
 
                 performFling(y + mViewDelta, vel, false);
                 mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSettingsButton.clearColorFilter();
                            getMemInfo();
                        }
                    }, 100);
             }
 
         }
         return false;
     }
 
     private void trackMovement(MotionEvent event) {
         // Add movement to velocity tracker using raw screen X and Y coordinates instead
         // of window coordinates because the window frame may be moving at the same time.
         float deltaX = event.getRawX() - event.getX();
         float deltaY = event.getRawY() - event.getY();
         event.offsetLocation(deltaX, deltaY);
         mVelocityTracker.addMovement(event);
         event.offsetLocation(-deltaX, -deltaY);
     }
 
     public Launcher makeLauncher(PendingIntent intent, String pkg, String tag, int id) {
         return new Launcher(intent, pkg, tag, id);
     }
 
     public class Launcher implements View.OnClickListener {
         private PendingIntent mIntent;
         private String mPkg;
         private String mTag;
         private int mId;
 
         Launcher(PendingIntent intent, String pkg, String tag, int id) {
             mIntent = intent;
             mPkg = pkg;
             mTag = tag;
             mId = id;
         }
 
         @Override
         public void onClick(View v) {
             try {
                 // The intent we are sending is for the application, which
                 // won't have permission to immediately start an activity after
                 // the user switches to home.  We know it is safe to do at this
                 // point, so make sure new activity switches are now allowed.
                 ActivityManagerNative.getDefault().resumeAppSwitches();
                 // Also, notifications can be launched from the lock screen,
                 // so dismiss the lock screen when the activity starts.
                 ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
             } catch (RemoteException e) {
             }
 
             if (mIntent != null) {
                 int[] pos = new int[2];
                 v.getLocationOnScreen(pos);
                 Intent overlay = new Intent();
                 overlay.setSourceBounds(new Rect(pos[0], pos[1], pos[0]+v.getWidth(), pos[1]+v.getHeight()));
                 try {
                     mIntent.send(StatusBarService.this, 0, overlay);
                 } catch (PendingIntent.CanceledException e) {
                     // the stack trace isn't very helpful here.  Just log the exception message.
                     Slog.w(TAG, "Sending contentIntent failed: " + e);
                 }
             }
 
             try {
                 mBarService.onNotificationClick(mPkg, mTag, mId);
             } catch (RemoteException ex) {
                 // system process is dead if we're here.
             }
 
             // close the shade if it was open
             animateCollapse();
 
             // If this click was on the intruder alert, hide that instead
             if (shouldTick && mShowNotif) {
                 toggleHideIntroducerView();
             }
             setClearLauncherNotif(mPkg);
         }
     }
 
     private void tick(StatusBarNotification n) {
         // Show the ticker if one is requested. Also don't do this
         // until status bar window is attached to the window manager,
         // because...  well, what's the point otherwise?  And trying to
         // run a ticker without being attached will crash!
         if (n.notification.tickerText != null && mStatusBarView.getWindowToken() != null) {
             if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                 if(!mHasSoftButtons || mStatusBarView.getSoftButtonsWidth() == 0)
                     mTicker.addEntry(n);
             }
         }
     }
 
     /**
      * Cancel this notification and tell the StatusBarManagerService / NotificationManagerService
      * about the failure.
      *
      * WARNING: this will call back into us.  Don't hold any locks.
      */
     public void handleNotificationError(IBinder key, StatusBarNotification n, String message) {
         removeNotification(key);
         try {
             mBarService.onNotificationError(n.pkg, n.tag, n.id, n.uid, n.initialPid, message);
         } catch (RemoteException ex) {
             // The end is nigh.
         }
     }
 
     private class MyTicker extends Ticker {
         MyTicker(Context context, CmStatusBarView mStatusBarView) {
             super(context, mStatusBarView);
         }
 
         @Override
         void tickerStarting() {
             if (SPEW) Slog.d(TAG, "tickerStarting");
             mTicking = true;
             mTickerView.setVisibility(View.VISIBLE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(false, com.android.internal.R.anim.push_up_out);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(false, com.android.internal.R.anim.push_up_out);
             }
         }
 
         @Override
         void tickerDone() {
             if (SPEW) Slog.d(TAG, "tickerDone");
             mTicking = false;
             mTickerView.setVisibility(View.GONE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(true, com.android.internal.R.anim.push_down_in);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(true, com.android.internal.R.anim.push_down_in);
             }
         }
 
         void tickerHalting() {
             if (SPEW) Slog.d(TAG, "tickerHalting");
             mTicking = false;
             mTickerView.setVisibility(View.GONE);
             mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.fade_out, null));
             if (!mExpandedVisible) {
                 setAllViewVisibility(true, com.android.internal.R.anim.fade_in);
             }
             if (mExpandedVisible) {
                 setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
             }
         }
     }
 
     private Animation loadAnim(int id, Animation.AnimationListener listener) {
         Animation anim = AnimationUtils.loadAnimation(StatusBarService.this, id);
         if (listener != null) {
             anim.setAnimationListener(listener);
         }
         return anim;
     }
 
     public String viewInfo(View v) {
         return "(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                 + " " + v.getWidth() + "x" + v.getHeight() + ")";
     }
 
     protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
         if (checkCallingOrSelfPermission(android.Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
             pw.println("Permission Denial: can't dump StatusBar from from pid=" + Binder.getCallingPid()
                     + ", uid=" + Binder.getCallingUid());
             return;
         }
 
         synchronized (mQueueLock) {
             pw.println("Current Status Bar state:");
             pw.println("  mExpanded=" + mExpanded + ", mExpandedVisible=" + mExpandedVisible);
             pw.println("  mTicking=" + mTicking);
             pw.println("  mTracking=" + mTracking);
             pw.println("  mAnimating=" + mAnimating + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                     + ", mAnimAccel=" + mAnimAccel);
             pw.println("  mCurAnimationTime=" + mCurAnimationTime + " mAnimLastTime=" + mAnimLastTime);
             pw.println(" mAnimatingReveal=" + mAnimatingReveal + " mViewDelta=" + mViewDelta);
             pw.println("  mDisplayMetrics=" + mDisplayMetrics);
             pw.println("  mExpandedParams: " + mExpandedParams);
             pw.println("  mExpandedView: " + viewInfo(mExpandedView));
             pw.println("  mExpandedDialog: " + mExpandedDialog);
             pw.println("  mTrackingParams: " + mTrackingParams);
             pw.println("  mTrackingView: " + viewInfo(mTrackingView));
             pw.println("  mOngoingTitle: " + viewInfo(mOngoingTitle));
             pw.println("  mOngoingItems: " + viewInfo(mOngoingItems));
             pw.println("  mLatestTitle: " + viewInfo(mLatestTitle));
             pw.println("  mLatestItems: " + viewInfo(mLatestItems));
             pw.println("  mNoNotificationsTitle: " + viewInfo(mNoNotificationsTitle));
             pw.println("  mCloseView: " + viewInfo(mCloseView));
             pw.println("  mTickerView: " + viewInfo(mTickerView));
             pw.println("  mScrollView: " + viewInfo(mScrollView)
                     + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
             pw.println("  mBottomScrollView: " + viewInfo(mBottomScrollView)
                     + " scroll " + mBottomScrollView.getScrollX() + "," + mBottomScrollView.getScrollY());
             pw.println("mNotificationLinearLayout: " + viewInfo(mNotificationLinearLayout));
             pw.println("mBottomNotificationLinearLayout: " + viewInfo(mBottomNotificationLinearLayout));
         }
 
         if (true) {
             // must happen on ui thread
             mHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         Slog.d(TAG, "mStatusIcons:");
                         mStatusIcons.debug();
                     }
                 });
         }
 
     }
 
     void onBarViewAttached() {
         WindowManager.LayoutParams lp;
 
         /// ---------- Tracking View --------------
         int pixelFormat = PixelFormat.TRANSLUCENT;
         lp = new WindowManager.LayoutParams(
                 mTinyExpanded ? getExpandedWidth() : ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                 0
                 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                 | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                 pixelFormat);
 
         if (mTinyExpanded) {
             lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
         } else {
             lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         }
         lp.setTitle("TrackingView");
         lp.y = mTrackingPosition;
         mTrackingParams = lp;
 
         WindowManagerImpl.getDefault().addView(mTrackingView, lp);
     }
 
     void onBarViewDetached() {
         WindowManagerImpl.getDefault().removeView(mTrackingView);
     }
 
     void onTrackingViewAttached() {
         WindowManager.LayoutParams lp;
 
         /// ---------- Expanded View --------------
         int mPixelFormat = PixelFormat.TRANSLUCENT;
         final int disph = mBottomBar ? mDisplayMetrics.heightPixels : (mDisplayMetrics.heightPixels-getNavBarSize());
         lp = mExpandedDialog.getWindow().getAttributes();
         lp.width = mTinyExpanded ? getExpandedWidth() : ViewGroup.LayoutParams.MATCH_PARENT;
         lp.height = mBottomBar ? getExpandedHeight() : (getExpandedHeight()-getNavBarSize());
         lp.x = 0;
         mTrackingPosition = lp.y = (mBottomBar ? disph : -disph); // sufficiently large positive
         lp.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
         lp.flags = 0
                 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                 | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                 | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                 | WindowManager.LayoutParams.FLAG_DITHER
                 | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         lp.format = mPixelFormat;
         if (mTinyExpanded) {
             lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
         } else {
             lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
         }
         lp.setTitle("StatusBarExpanded");
         mExpandedParams = lp;
         updateExpandedHeight();
         mExpandedDialog.getWindow().setFormat(mPixelFormat);
 
         mExpandedDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         mExpandedDialog.setContentView(mExpandedView,
                 new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));
         mExpandedDialog.getWindow().setBackgroundDrawable(null);
         mExpandedDialog.show();
     }
 
     void onTrackingViewDetached() {}
 
     private void setDateViewVisibility(boolean visible, int anim) {
         if(mHasSoftButtons && mButtonsLeft)
             return;
 
         if(!mShowDate)
             return;
 
         mDateView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
         mDateView.startAnimation(loadAnim(anim, null));
 
 	if (visible) {
             setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
         } else {
             setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
         }
     }
 
     private void setAllViewVisibility(boolean visible, int anim) {
         mIcons.setVisibility(visible ? View.VISIBLE : View.GONE);
         mIcons.startAnimation(loadAnim(anim, null));
         if (mStatusBarClock == 2) {
             mCenterClock.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCenterClock.startAnimation(loadAnim(anim, null));
         } else if (mStatusBarClock == 3) {
             mLeftClock.setVisibility(visible ? View.VISIBLE : View.GONE);
             mLeftClock.startAnimation(loadAnim(anim, null));
         }
         if (mStatusBarCarrier == 1) {
             mCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         } else if (mStatusBarCarrier == 2) {
             mCenterCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCenterCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         } else if (mStatusBarCarrier == 3) {
             mLeftCarrierLabelStatusBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mLeftCarrierLabelStatusBarLayout.startAnimation(loadAnim(anim, null));
         }
         if (mStatusBarCarrierLogo == 1) {
             mCarrierLogoLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCarrierLogoLayout.startAnimation(loadAnim(anim, null));
         } else if (mStatusBarCarrierLogo == 2) {
             mCarrierLogoCenterLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCarrierLogoCenterLayout.startAnimation(loadAnim(anim, null));
         } else if (mStatusBarCarrierLogo == 3) {
             mCarrierLogoLeftLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCarrierLogoLeftLayout.startAnimation(loadAnim(anim, null));
         }
         if (mShowCmBatteryStatusBar) {
             mCmBatteryStatusBar.setVisibility(visible ? View.VISIBLE : View.GONE);
             mCmBatteryStatusBar.startAnimation(loadAnim(anim, null));
         }
     }
 
     private void setNotificationIconVisibility(boolean visible, int anim) {
         int old = mNotificationIcons.getVisibility();
         int v = visible ? View.VISIBLE : View.INVISIBLE;
         if (old != v) {
            if (mStatusBarCarrierLogo == 2 && mStatusBarReverse) {
                mNotificationIcons.setVisibility(View.INVISIBLE);
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            } else if (mStatusBarCarrier == 2 && mStatusBarReverse) {
                mNotificationIcons.setVisibility(View.INVISIBLE);
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            } else {
                mNotificationIcons.setVisibility(v);
                mNotificationIcons.startAnimation(loadAnim(anim, null));
            }
         }
     }
 
     private void updateExpandedInvisiblePosition() {
          int disph = mBottomBar ? mDisplayMetrics.heightPixels : (mDisplayMetrics.heightPixels-getNavBarSize());
          if (mTrackingView != null) {
              mTrackingPosition = mBottomBar ? disph : -disph;
              if (mTrackingParams != null) {
                  mTrackingParams.y = mTrackingPosition;
                  WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
              }
          }
          if (mExpandedParams != null) {
              mExpandedParams.y = mBottomBar ? disph : -disph;
              mExpandedDialog.getWindow().setAttributes(mExpandedParams);
          }
     }
 
     public void updateExpandedViewPos(int expandedPosition) {
         if (SPEW) {
             Slog.d(TAG, "updateExpandedViewPos before expandedPosition=" + expandedPosition
                     + " mTrackingParams.y="
                     + ((mTrackingParams == null) ? "???" : mTrackingParams.y)
                     + " mTrackingPosition=" + mTrackingPosition);
         }
 
         int h = mBottomBar ? 0 : (mShowDate ? getStatBarSize() : 0);
         int disph = mBottomBar ? mDisplayMetrics.heightPixels : (mDisplayMetrics.heightPixels-getNavBarSize());
 
         // If the expanded view is not visible, make sure they're still off screen.
         // Maybe the view was resized.
         if (!mExpandedVisible) {
             updateExpandedInvisiblePosition();
             return;
         }
 
         // tracking view...
         int pos;
         if (expandedPosition == EXPANDED_FULL_OPEN) {
             pos = h;
             isFullExpanded = true;
         }
         else if (expandedPosition == EXPANDED_LEAVE_ALONE) {
             pos = mTrackingPosition;
             isFullExpanded = false;
         }
         else {
             if ((mBottomBar && expandedPosition >= 0) || (!mBottomBar && expandedPosition <= disph)) {
                 pos = expandedPosition;
             } else {
                 pos = disph;
             }
             pos -= mBottomBar ? mCloseView.getHeight() : disph-h;
             isFullExpanded = false;
         }
         if(mBottomBar && pos < 0)
             pos=0;
 
         mTrackingPosition = mTrackingParams.y = pos;
         mTrackingParams.height = disph-h;
         WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
 
         final float frac = 1.0f + (float)pos / mDisplayMetrics.heightPixels;
         final int color = ((int)(0xCC * frac * frac)) << 24;
         mExpandedView.setBackgroundColor(color);
 
         if (mExpandedParams != null) {
             mCloseView.getLocationInWindow(mPositionTmp);
             final int closePos = mPositionTmp[1];
             int conBot;
 
             if (!mStatusBarTab) {
                 mExpandedContents.getLocationInWindow(mPositionTmp);
                 conBot = mPositionTmp[1] + mExpandedContents.getHeight();
             } else {
                 mExpandededContents.getLocationInWindow(mPositionTmp);
                 conBot = mPositionTmp[1] + mExpandededContents.getHeight();
             }
 
             final int contentsBottom = conBot;
 
             if (expandedPosition != EXPANDED_LEAVE_ALONE) {
                 if(mBottomBar)
                     mExpandedParams.y = pos + mCloseView.getHeight();
                 else
                     mExpandedParams.y = pos + mTrackingView.getHeight() - (mTrackingParams.height-closePos) - contentsBottom;
                 int max = mBottomBar ? (mDisplayMetrics.heightPixels-getNavBarSize()) : h;
                 if (mExpandedParams.y > max) {
                     mExpandedParams.y = max;
                 }
                 int min = mBottomBar ? mCloseView.getHeight() : mTrackingPosition;
                 if (mExpandedParams.y < min) {
                     mExpandedParams.y = min;
                     if(mBottomBar)
                         mTrackingParams.y = 0;
                 }
 
                 boolean visible = mBottomBar ? mTrackingPosition < mDisplayMetrics.heightPixels : (mTrackingPosition + mTrackingView.getHeight()) > h;
                 if (!visible) {
                     // if the contents aren't visible, move the expanded view way off screen
                     // because the window itself extends below the content view.
                     mExpandedParams.y = mBottomBar ? disph : -disph;
                 }
                 mExpandedDialog.getWindow().setAttributes(mExpandedParams);
 
                 if (SPEW) Slog.d(TAG, "updateExpandedViewPos visibilityChanged(" + visible + ")");
                 visibilityChanged(visible);
             }
         }
 
         if (SPEW) {
             Slog.d(TAG, "updateExpandedViewPos after  expandedPosition=" + expandedPosition + " mTrackingParams.y=" + mTrackingParams.y
                     + " mTrackingView.getHeight=" + mTrackingView.getHeight() + " mTrackingPosition=" + mTrackingPosition
                     + " mExpandedParams.y=" + mExpandedParams.y + " mExpandedParams.height=" + mExpandedParams.height);
         }
     }
 
     public boolean isFullyExpanded() {
         return isFullExpanded;
     }
 
     private int getExpandedWidth() {
         int defValuesTinyExpSize = mContext.getResources().getInteger(com.android.internal.R.integer.config_tinyexpsize_default_cyanmobile);
         float expandedSizeval = (float) Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_EXPANDED_SIZE, defValuesTinyExpSize);
         int expandedSizepx = (int) (mDisplayMetrics.density * expandedSizeval);
         return (mDisplayMetrics.widthPixels-expandedSizepx);
     }
 
     private int getExpandedHeight() {
         return (mDisplayMetrics.heightPixels-(mShowDate ? getStatBarSize() : 0)-mCloseView.getHeight());
     }
 
     void updateDisplaySize() {
         mDisplay.getMetrics(mDisplayMetrics);
         updateExpandedHeight();
     }
 
     private void updateExpandedHeight() {
         if (mExpandedView != null && mExpandedParams != null && mDisplayMetrics != null) {
             mExpandedParams.height = mBottomBar ? getExpandedHeight() : (getExpandedHeight()-getNavBarSize());
             if (mTinyExpanded) {
                 mExpandedParams.width = getExpandedWidth();
             }
             mExpandedDialog.getWindow().setAttributes(mExpandedParams);
         }
     }
 
     /**
      * The LEDs are turned o)ff when the notification panel is shown, even just a little bit.
      * This was added last-minute and is inconsistent with the way the rest of the notifications
      * are handled, because the notification isn't really cancelled.  The lights are just
      * turned off.  If any other notifications happen, the lights will turn back on.  Steve says
      * this is what he wants. (see bug 1131461)
      */
     void visibilityChanged(boolean visible) {
         if (mPanelSlightlyVisible != visible) {
             mPanelSlightlyVisible = visible;
             try {
                 mBarService.onPanelRevealed();
             } catch (RemoteException ex) {
                 // Won't fail unless the world has ended.
             }
         }
     }
 
     private void performDisableActions(int net) {
         int old = mDisabled;
         int diff = net ^ old;
         mDisabled = net;
 
         // act accordingly
         if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
             if ((net & StatusBarManager.DISABLE_EXPAND) != 0) {
                 Slog.d(TAG, "DISABLE_EXPAND: yes");
                 animateCollapse();
             }
         }
         if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
             if ((net & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                 Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                 if (mTicking) {
                     mNotificationIcons.setVisibility(View.INVISIBLE);
                     mTicker.halt();
                 } else {
                     setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                 }
             } else {
                 Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                 if (!mExpandedVisible) {
                     setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                 }
             }
         } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
             Slog.d(TAG, "DISABLE_NOTIFICATION_TICKER: "
                 + (((net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0)
                     ? "yes" : "no"));
             if (mTicking && (net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                 mTicker.halt();
             }
         }
     }
 
     private View.OnClickListener mMusicToggleButtonListener = new View.OnClickListener() {
         @Override
 	public void onClick(View v) {
 	   mMusicControls.visibilityToggled();
 	}
     };
 
     private View.OnClickListener mPowerClickListener = new View.OnClickListener() {
         @Override
          public void onClick(View v) {
                 if (Settings.System.getInt(getContentResolver(),
                          Settings.System.EXPANDED_HIDE_ONCHANGE, 0) == 1) {
                    animateCollapse();
                 }
          }
     };
 
     private View.OnLongClickListener mPowerLongClickListener = new View.OnLongClickListener() {
          public boolean onLongClick(View v) {
                 animateCollapse();
                 return true;
          }
     };
 
     public void toggleClearNotif() {
         try {
             mBarService.onClearAllNotifications();
         } catch (RemoteException ex) {
            // system process is dead if we're here.
         }
     }
     
     private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             mClearButton.clearColorFilter();
             toggleClearNotif();
             mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mClearButton.setColorFilter(mSettingsColor, Mode.MULTIPLY);
                        }
                    }, 100);
             animateCollapse();
         }
     };
 
     private View.OnTouchListener mRingPanelListener = new View.OnTouchListener() {
         public boolean onTouch(View v, MotionEvent ev) {
             final int action = ev.getAction();
             if ((action == MotionEvent.ACTION_DOWN
                     && !mRingPanel.isInContentArea((int)ev.getX(), (int)ev.getY()))) {
                 mHandler.removeMessages(MSG_HIDE_RING_PANEL);
                 mHandler.sendEmptyMessage(MSG_HIDE_RING_PANEL);
                 return true;
             }
             return false;
         }
     };
 
     private View.OnClickListener mSettingsIconButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
               mSettingsIconButton.clearColorFilter();
               if (mStatusBarTab) {
                 if (NotifEnable) {
                     togglePower();
                 } else {
                     toggleNotif();
                 }
               } else {
                 CmStatusBarView.toggleSettingsApps(mContext);
                 animateCollapse();
               }
               mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSettingsIconButton.setColorFilter(mSettingsColor, Mode.MULTIPLY);
                        }
                    }, 100);
         }
     };
 
     private View.OnClickListener mAvalMemLayoutListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             CmStatusBarView.simulateKeypress(CmStatusBarView.KEYCODE_VIRTUAL_BACK_LONG);
             getMemInfo();
         }
     };
 
     private View.OnLongClickListener mAvalMemLayoutTaskListener = new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
              CmStatusBarView.runTaskManager(mContext);
              animateCollapse();
              return true;
         }
     };
 
     private View.OnClickListener mTogglePowerListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
              if (NotifEnable) {
                  togglePower();
              }
         }
     };
 
     private View.OnClickListener mToggleNotifListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (!NotifEnable) {
                 toggleNotif();
             }
         }
     };
 
     private Animator setVisibilityWhenDone(
             final Animator a, final View v, final int vis) {
         a.addListener(new AnimatorListenerAdapter() {
             @Override
             public void onAnimationEnd(Animator animation) {
                 v.setVisibility(vis);
             }
         });
         return a;
     }
 
     private Animator interpolator(TimeInterpolator ti, Animator a) {
         a.setInterpolator(ti);
         return a;
     }
 
     private Animator startDelay(int d, Animator a) {
         a.setStartDelay(d);
         return a;
     }
 
     private Animator start(Animator a) {
         a.start();
         return a;
     }
 
     private final TimeInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
     private final TimeInterpolator mDecelerateInterpolator = new DecelerateInterpolator();	
     private final int FLIP_DURATION_OUT = 125;
     private final int FLIP_DURATION_IN = 225;	
     private final int FLIP_DURATION = (FLIP_DURATION_IN + FLIP_DURATION_OUT);
 	
     private Animator mNotifViewAnim, mPowerViewAnim, mIntruderAlertAnim;
 
     public void toggleNotif() {
           if (!mStatusBarTab) return;
 
           mSettingsIconButton.setImageResource(R.drawable.ic_qs_panel);
           mButtonsToggle.setTextColor(mClockColor);
           mNotificationsToggle.setTextColor(Color.parseColor("#666666"));
           LinearLayout parent = (LinearLayout)mButtonsToggle.getParent();
           parent.setBackgroundResource(R.drawable.title_bar_portrait);
           if (mPowerCarrier.getVisibility() != View.VISIBLE) {
                 if (mPowerViewAnim != null) mPowerViewAnim.cancel();
                 if (mNotifViewAnim != null) mNotifViewAnim.cancel();
 
                 mPowerCarrier.setVisibility(View.VISIBLE);
                 mPowerCarrier.setScaleX(0f);
                 mPowerViewAnim = start(
                     startDelay(FLIP_DURATION_OUT,
                         interpolator(mDecelerateInterpolator,
                             ObjectAnimator.ofFloat(mPowerCarrier, View.SCALE_X, 0f, 1f)
                                 .setDuration(FLIP_DURATION_IN)
                             )));
                 mNotifViewAnim = start(
                     setVisibilityWhenDone(
                         interpolator(mAccelerateInterpolator,
                                 ObjectAnimator.ofFloat(mNotifications, View.SCALE_X, 1f, 0f)
                                 )
                             .setDuration(FLIP_DURATION_OUT),
                         mNotifications, View.INVISIBLE));
           }
           updateExpandedViewPos(EXPANDED_FULL_OPEN);
           NotifEnable = true;
     }
 
     public void togglePower() {
           if (!mStatusBarTab) return;
 
           mSettingsIconButton.setImageResource(R.drawable.ic_qs_notif);
           mNotificationsToggle.setTextColor(mClockColor);
           mButtonsToggle.setTextColor(Color.parseColor("#666666"));
           LinearLayout parent = (LinearLayout)mButtonsToggle.getParent();
           parent.setBackgroundResource(R.drawable.title_bar_portrait);
           if (mNotifications.getVisibility() != View.VISIBLE) {
                 if (mPowerViewAnim != null) mPowerViewAnim.cancel();
                 if (mNotifViewAnim != null) mNotifViewAnim.cancel();
 
                 mNotifications.setVisibility(View.VISIBLE);
                 mNotifViewAnim = start(
                     startDelay(FLIP_DURATION_OUT,
                         interpolator(mDecelerateInterpolator,
                             ObjectAnimator.ofFloat(mNotifications, View.SCALE_X, 0f, 1f)
                                 .setDuration(FLIP_DURATION_IN)
                             )));
                 mPowerViewAnim = start(
                     setVisibilityWhenDone(
                         interpolator(mAccelerateInterpolator,
                                 ObjectAnimator.ofFloat(mPowerCarrier, View.SCALE_X, 1f, 0f)
                                 )
                             .setDuration(FLIP_DURATION_OUT),
                         mPowerCarrier, View.INVISIBLE));
           }
           updateExpandedViewPos(EXPANDED_FULL_OPEN);
           NotifEnable = false;
     }
 
     private static Bitmap getNinePatch(int id,int x, int y, Context context){
         Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
 
         byte[] chunk = bitmap.getNinePatchChunk();
         NinePatchDrawable np_drawable = new NinePatchDrawable(bitmap, chunk, new Rect(), null);
         np_drawable.setBounds(0, 0,x, y);
 
         Bitmap output_bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(output_bitmap);
         np_drawable.draw(canvas);
 
         return output_bitmap;
     }
 
     private View.OnLongClickListener mSettingsButtonListener = new View.OnLongClickListener() {
         public boolean onLongClick(View v) {
             if (Settings.System.getInt(getContentResolver(), Settings.System.ENABLE_SETTING_BUTTON, 0) == 1) {
                 if (mFirstis) {
                     Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVI_BUTTONS, 0);
                     mFirstis = false;
                 } else {
                     Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVI_BUTTONS, 1);
                     mFirstis = true;
                 }
                 animateCollapse();
                 return true;
             } else if (Settings.System.getInt(getContentResolver(), Settings.System.ENABLE_SETTING_BUTTON, 0) == 2) {
                 WeatherPopup weatherWindow = new WeatherPopup(v);
                 weatherWindow.showWeatherAction();
                 animateCollapse();
                 return true;
             }
 
             return false;
         }
     };
 
     private View.OnClickListener mCarrierButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
              if(Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_VIEW_WIDGET, 5) == 0) {
                 QuickSettingsPopupWindow quickSettingsWindow = new QuickSettingsPopupWindow(v);
                 quickSettingsWindow.showLikeQuickAction();
              }
         }
     };
 
     public void setIMEVisible(boolean visible) {
          mNavigationBarView.setIMEVisible(visible);
     }
 
     public void toggleQwikWidgets() {
         int msg = (mWidgetsPanel.getVisibility() == View.VISIBLE)
             ? MSG_HIDE_WIDGETS_PANEL : MSG_SHOW_WIDGETS_PANEL;
         mHandler.removeMessages(msg);
         mHandler.sendEmptyMessage(msg);
     }
 
     public void toggleRingPanel() {
         int msg = (mRingPanel.getVisibility() == View.VISIBLE)
             ? MSG_HIDE_RING_PANEL : MSG_SHOW_RING_PANEL;
         mHandler.removeMessages(msg);
         mHandler.sendEmptyMessage(msg);
     }
 
     private void toggleHideQwikWidgets() {
         mHandler.removeMessages(MSG_HIDE_WIDGETS_PANEL);
         mHandler.sendEmptyMessage(MSG_HIDE_WIDGETS_PANEL);
     }
 
     private void toggleHideRingPanel() {
         mHandler.removeMessages(MSG_HIDE_RING_PANEL);
         mHandler.sendEmptyMessage(MSG_HIDE_RING_PANEL);
     }
 
     private View.OnClickListener mIconButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if(Settings.System.getInt(getContentResolver(), Settings.System.USE_CUSTOM_SHORTCUT_TOGGLE, 0) == 1) {
                if (mBrightnessControl) {
                    final View vW = v;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ShortcutPopupWindow shortCutWindow = new ShortcutPopupWindow(vW);
                            shortCutWindow.showLikeQuickAction();
                            animateCollapse();
                        }
                    }, (BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT + 50));
                } else {
                  ShortcutPopupWindow shortCutWindow = new ShortcutPopupWindow(v);
                  shortCutWindow.showLikeQuickAction();
                  animateCollapse();
                }
             }
         }
     };
 
     private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                     || Intent.ACTION_SCREEN_OFF.equals(action)) {
                 animateCollapse();
             } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                 // update clock for changing font
                 if (mStatusBarView != null) {
                     Clock clock = (Clock) mStatusBarView.findViewById(R.id.clock);
                     if (clock != null) {
                         clock.invalidate();
                     }
                     CenterClock centerClo = (CenterClock) mStatusBarView.findViewById(R.id.centerClo);
                     if (centerClo != null) {
                         centerClo.invalidate();
                     }
                     PowerClock centerCloex = (PowerClock) mExpandedView.findViewById(R.id.centerCloex);
                     if (centerCloex != null) {
                         centerCloex.invalidate();
                     }
                     ClockExpand clockExp = (ClockExpand) mExpandedView.findViewById(R.id.expclock_power);
                     if (clockExp != null) {
                         clockExp.invalidate();
                     }
                     ExDateView powDateView = (ExDateView) mExpandedView.findViewById(R.id.datestats);
                     if (powDateView != null) {
                         powDateView.invalidate();
                     }
                     LeftClock clockLeft = (LeftClock) mStatusBarView.findViewById(R.id.clockLe);
                     if (clockLeft != null) {
                         clockLeft.invalidate();
                     }
                     DataTraffics dataTraffics = (DataTraffics) mExpandedView.findViewById(R.id.dataTrafficsExp);
                     if (dataTraffics != null) {
                         dataTraffics.invalidate();
                     }
                 }
                 repositionNavigationBar();
                 updateResources();
             }
         }
     };
 
     public void setIntruderAlertVisibility(boolean vis) {
         if (mIntruderAlertAnim != null) mIntruderAlertAnim.cancel();
         if (mPieControlPanel != null && !vis) {
             mPieControlPanel.resetStatus(-1);
         }
         if (vis) {
             mIntruderAlertView.setAlpha(0f);
             mIntruderAlertView.setVisibility(View.VISIBLE);
         }
         mIntruderAlertAnim = start(setVisibilityWhenDone(startDelay(vis ? 500 : 0, 
                              interpolator(new AccelerateInterpolator(2.0f),
                              ObjectAnimator.ofFloat(mIntruderAlertView, "alpha", vis ? 0f : 1f, vis ? 1f : 0f))
                                            .setDuration(vis ? 1000 : 300)),
                              mIntruderAlertView, vis ? View.VISIBLE : View.GONE));
     }
 
     private void getMemInfo() {
         if (!mShowRam) return;
 
 	if(totalMemory == 0) {
 	   try {
 	      RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
 	      String load = reader.readLine();
 	      String[] memInfo = load.split(" ");
 	      totalMemory = Double.parseDouble(memInfo[9])/1024;
            } catch (IOException ex) {
 	      ex.printStackTrace();
 	   }
 	}
 
 	ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
 	MemoryInfo mi = new MemoryInfo();
 	activityManager.getMemoryInfo(mi);
 	availableMemory = mi.availMem / 1048576L;
 	memHeader.setText("Ram Info: Available: "+(int)(availableMemory)+"MB Total: "+(int)totalMemory+"MB.");
 	int progress = (int) (((totalMemory-availableMemory)/totalMemory)*100);
 	avalMemPB.setProgress(progress);
     }
 
     private class PieControlsTouchListener implements View.OnTouchListener {
         private int orient;
         private boolean actionDown = false;
         private boolean centerPie = true;
         private float initialX = 0;
         private float initialY = 0;
         int index;
 
         public PieControlsTouchListener() {
             orient = mPieControlPanel.getOrientation();
         }
 
         @Override
         public boolean onTouch(View v, MotionEvent event) {
             final int action = event.getAction();
             if (!mPieControlPanel.isShowing()) {
                 switch(action) {
                     case MotionEvent.ACTION_DOWN:
                         centerPie = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PIE_CENTER, 1) == 1;
                         actionDown = true;
                         initialX = event.getX();
                         initialY = event.getY();
                         break;
                     case MotionEvent.ACTION_MOVE:
                         if (!actionDown) break;
 
                         float deltaX = Math.abs(event.getX() - initialX);
                         float deltaY = Math.abs(event.getY() - initialY);
                         float distance = orient == Gravity.BOTTOM ||
                                 orient == Gravity.TOP ? deltaY : deltaX;
                         // Swipe up
                         if (distance > 10) {
                             orient = mPieControlPanel.getOrientation();
                             mPieControlPanel.show(centerPie ? -1 : (int)(orient == Gravity.BOTTOM ||
                                 orient == Gravity.TOP ? initialX : initialY));
                             event.setAction(MotionEvent.ACTION_DOWN);
                             mPieControlPanel.onTouchEvent(event);
                             actionDown = false;
                         }
                 }
             } else {
                 return mPieControlPanel.onTouchEvent(event);
             }
             return false;
         }
     }
 
     public void updatePieControls() {
         if (mPieControlsTrigger != null) {
             WindowManagerImpl.getDefault().removeView(mPieControlsTrigger);
         }
         if (mPieControlPanel != null) {
             WindowManagerImpl.getDefault().removeView(mPieControlPanel);
         }
         attachPies();
     }
 
     /**
      * Reload some of our resources when the configuration changes.
      *
      * We don't reload everything when the configuration changes -- we probably
      * should, but getting that smooth is tough.  Someday we'll fix that.  In the
      * meantime, just update the things that we know change.
      */
     public void updateResources() {
         Resources res = getResources();
 
         // detect theme change.
         CustomTheme newTheme = res.getConfiguration().customTheme;
         if (newTheme != null && (mCurrentTheme == null || !mCurrentTheme.equals(newTheme))) {
             mCurrentTheme = (CustomTheme)newTheme.clone();
             mCmBatteryMiniIcon.updateIconCache();
             mCmBatteryMiniIcon.updateMatrix();
             // restart system ui on theme change
             try {
                 Runtime.getRuntime().exec("pkill -TERM -f  com.android.systemui");
             } catch (IOException e) {
                 // we're screwed here fellas
             }
         } else {
             mOngoingTitle.setText(getText(R.string.status_bar_ongoing_events_title));
             mLatestTitle.setText(getText(R.string.status_bar_latest_events_title));
             if (mStatusBarTab) {
                mLatestTitles.setText(getText(R.string.status_bar_latestnews_events_title));
             }
             mNoNotificationsTitle.setText(getText(R.string.status_bar_no_notifications_title));
 
             // update clock for changing font
             if(mStatusBarView != null && mStatusBarView.mDate != null) {
                mStatusBarView.mDate.invalidate();
             }
 
             mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
         }
 
         if ((mStatusBarTab) && (mQS != null)) mQS.updateResources();
         if (mPieControlPanel != null) mPieControlPanel.configurationChanges();
 
         if (false) Slog.v(TAG, "updateResources");
     }
 
     //
     // tracing
     //
 
     public void postStartTracing() {
         mHandler.removeCallbacks(mStartTracing);
         mHandler.postDelayed(mStartTracing, 3000);
     }
 
     private void vibrate() {
         if (Settings.System.getInt(mContext.getContentResolver(),
                      Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1) {
             android.os.Vibrator vib = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
             vib.vibrate(250);
         }
     }
 
     private Runnable mStartTracing = new Runnable() {
         @Override
         public void run() {
             vibrate();
             SystemClock.sleep(250);
             Slog.d(TAG, "startTracing");
             android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
             mHandler.removeCallbacks(mStopTracing);
             mHandler.postDelayed(mStopTracing, 10000);
         }
     };
 
     private Runnable mStopTracing = new Runnable() {
         @Override
         public void run() {
             android.os.Debug.stopMethodTracing();
             Slog.d(TAG, "stopTracing");
             vibrate();
         }
     };
 }
