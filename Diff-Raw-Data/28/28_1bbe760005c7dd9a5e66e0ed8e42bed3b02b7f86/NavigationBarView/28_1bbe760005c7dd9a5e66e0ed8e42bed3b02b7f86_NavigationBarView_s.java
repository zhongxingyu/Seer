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
 
 package com.android.systemui.statusbar;
 
 import java.net.URISyntaxException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ActivityNotFoundException;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.content.res.Resources;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.IWindowManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Surface;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import android.content.ContentResolver;
 import android.content.res.Configuration;
 import android.provider.Settings;
 import android.database.ContentObserver;
 import android.content.BroadcastReceiver;
 import android.util.Slog;
 import android.os.Handler;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 
 import com.android.systemui.statusbar.popups.ActionItem;
 import com.android.systemui.statusbar.popups.QuickAction;
 import com.android.systemui.R;
 
 public class NavigationBarView extends LinearLayout {
 
     private static final boolean DEBUG = false;
     private static final String TAG = "NavigationBarView";
 
     public static final int KEYCODE_VIRTUAL_HOME_LONG=KeyEvent.getMaxKeyCode()+1;
     public static final int KEYCODE_VIRTUAL_BACK_LONG=KeyEvent.getMaxKeyCode()+2;
 
     private static final int ID_APPLICATION = 1;
     private static final int ID_DISPLAY = 2;
     private static final int ID_INPUT = 3;
     private static final int ID_UIN = 4;
     private static final int ID_LOCKSCREEN = 5;	
     private static final int ID_PERFORMANCE = 6;
     private static final int ID_POWERSAVER = 7;
     private static final int ID_SOUND = 8;
     private static final int ID_TABLET = 9;
     private static final int ID_WIFI = 10;
     private static final int ID_BLUETOOTH = 11;
     private static final int ID_MOBILENETWORK = 12;
     private static final int ID_TETHERING = 13;
     private static final int ID_APPLICATIONS = 14;	
     private static final int ID_LOCSECURE = 15;
     private static final int ID_SOUNDS = 16;
     private static final int ID_DISPLAYS = 17;
     private static final int ID_CALLSET = 18;
     private static final int ID_STORAGE = 19;
     private static final int ID_PROFILE = 20;
     private static final int ID_PRIVACY = 21;
     private static final int ID_DATETIME = 22;	
     private static final int ID_LANGKEY = 23;
     private static final int ID_VOICEN = 24;
     private static final int ID_ACCESS = 25;
     private static final int ID_DEVELOP = 26;
     private static final int ID_ADWLAUNCHER = 27;
     private static final int ID_BACKILL = 28;
     private static final int ID_SCREENSHOT = 29;
 
     View mNaviBackground;
     View mNaviAdd;
     ViewGroup mSoftButtons;
     ImageButton mHomeButton;
     ImageButton mMenuButton;
     ImageButton mBackButton;
     ImageButton mSearchButton;
     ImageButton mVolUpButton;
     ImageButton mVolDownButton;
     ImageButton mQuickButton;
 
     private int mNVColor;
     private int mNVTrans;
     private boolean mNVShow;
     private boolean mShowNV;
     private boolean mShowVol;
     private int mShowHome;
     private int mShowMenu;
     private int mShowBack;
     private int mShowSearch;
     private int mShowQuicker;
     private boolean mLongPressBackKills;
     private boolean mInputShow;
     private Bitmap mCustomHomeIcon;
     private Bitmap mCustomMenuIcon;
     private Bitmap mCustomBackIcon;
     private Bitmap mCustomSearchIcon;
     private Bitmap mCustomQuickIcon;

     Handler mHandler;
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.SHOW_NAVI_BUTTONS), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTON_SHOW_HOME), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTON_SHOW_MENU), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTON_SHOW_BACK), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTON_SHOW_SEARCH), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVI_BUTTON_SHOW_QUICKER), false, this);
             resolver.registerContentObserver(
                     Settings.Secure.getUriFor(Settings.Secure.KILL_APP_LONGPRESS_BACK), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             ContentResolver resolver = mContext.getContentResolver();
 
             mNVShow = (Settings.System.getInt(resolver,
                     Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
             mShowNV = (Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTONS, 1) == 1);
             mShowHome = Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTON_SHOW_HOME, 1);
             mShowMenu = Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTON_SHOW_MENU, 4);
             mShowBack = Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTON_SHOW_BACK, 2);
             mShowSearch = Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTON_SHOW_SEARCH, 3);
             mShowQuicker = Settings.System.getInt(resolver,
                     Settings.System.NAVI_BUTTON_SHOW_QUICKER, 4);
             mLongPressBackKills = (Settings.Secure.getInt(resolver,
                     Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);
             updateNaviButtons();
         }
     }
 
     public NavigationBarView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override
     protected void onFinishInflate() {
         super.onFinishInflate();
 
         mHandler=new Handler();
 
         mNaviAdd = findViewById(R.id.navibarAdd);
         mNaviBackground = findViewById(R.id.navibarBackground);
         mSoftButtons = (ViewGroup)findViewById(R.id.navbuttons);
 
         ContentResolver resolver = mContext.getContentResolver();
         mNVTrans = (Settings.System.getInt(resolver,
                 Settings.System.TRANSPARENT_NAVI_BAR, 1));
          mNVShow = (Settings.System.getInt(resolver,
                     Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
 
         if ( mNVTrans == 4) {
            mNVColor = (Settings.System.getInt(resolver,
                    Settings.System.NAVI_BAR_COLOR, 0));
 
            mNaviBackground.setBackgroundColor(mNVColor);
         }
 
         if (mNVShow) {
 	    ActionItem appItem = new ActionItem(ID_APPLICATION, "Application");
 	    ActionItem dispItem = new ActionItem(ID_DISPLAY, "Display");
             ActionItem inpItem = new ActionItem(ID_INPUT, "Input");
             ActionItem uisItem = new ActionItem(ID_UIN, "Interface");
             ActionItem lockItem = new ActionItem(ID_LOCKSCREEN, "Lockscreen");
             ActionItem prfmItem = new ActionItem(ID_PERFORMANCE, "Performance");
             ActionItem pwrsItem = new ActionItem(ID_POWERSAVER, "Power saver");
             ActionItem sndItem = new ActionItem(ID_SOUND, "Sound");
             ActionItem tbltItem = new ActionItem(ID_TABLET, "Tablet tweaks");
 
             final QuickAction quickAction = new QuickAction(getContext(), QuickAction.VERTICAL);
 
             quickAction.addActionItem(appItem);
             quickAction.addActionItem(dispItem);
             quickAction.addActionItem(inpItem);
             quickAction.addActionItem(uisItem);
             quickAction.addActionItem(lockItem);
             quickAction.addActionItem(prfmItem);
             quickAction.addActionItem(pwrsItem);
             quickAction.addActionItem(sndItem);
             quickAction.addActionItem(tbltItem);
 
 		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
 			@Override
 			public void onItemClick(QuickAction source, int pos, int actionId) {				
 				ActionItem actionItem = quickAction.getActionItem(pos);
 				if (actionId == ID_APPLICATION) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.ApplicationActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_DISPLAY) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.DisplayActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_INPUT) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.InputActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_UIN) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.UIActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_LOCKSCREEN) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.LockscreenActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_PERFORMANCE) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.PerformanceSettingsActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_POWERSAVER) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.PowerSaverActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_SOUND) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.SoundActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_TABLET) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.cyanogenmod.cmparts", "com.cyanogenmod.cmparts.activities.TabletTweaksActivity");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				}
 			}
 		});
 		
 		quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {			
 			@Override
 			public void onDismiss() {
 			}
                 });
 
             ActionItem wifiItem = new ActionItem(ID_WIFI, "Wifi settings");
             ActionItem blueItem = new ActionItem(ID_BLUETOOTH, "Bluetooth settings");
             ActionItem mobileItem = new ActionItem(ID_MOBILENETWORK, "Mobile Networks");
             ActionItem tetherItem = new ActionItem(ID_TETHERING, "Tether settings");
             ActionItem appsItem = new ActionItem(ID_APPLICATIONS, "Applications");
             ActionItem locksecItem = new ActionItem(ID_LOCSECURE, "Location and Security");
             ActionItem soundsItem = new ActionItem(ID_SOUNDS, "Sound settings");
             ActionItem displayItem = new ActionItem(ID_DISPLAYS, "Display settings");
             ActionItem callsItem = new ActionItem(ID_CALLSET, "Call settings");
 
             final QuickAction quickActionss = new QuickAction(getContext(), QuickAction.VERTICAL);
 
             quickActionss.addActionItem(wifiItem);
             quickActionss.addActionItem(blueItem);
             quickActionss.addActionItem(mobileItem);
             quickActionss.addActionItem(tetherItem);
             quickActionss.addActionItem(appsItem);
             quickActionss.addActionItem(locksecItem);
             quickActionss.addActionItem(soundsItem);
             quickActionss.addActionItem(displayItem);
             quickActionss.addActionItem(callsItem);
 
 		quickActionss.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
 			@Override
 			public void onItemClick(QuickAction source, int pos, int actionId) {				
 				ActionItem actionItem = quickAction.getActionItem(pos);
 				if (actionId == ID_WIFI) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_BLUETOOTH) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_MOBILENETWORK) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.phone", "com.android.phone.Settings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_TETHERING) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_APPLICATIONS) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.ApplicationSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_LOCSECURE) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.SecuritySettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_SOUNDS) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.SoundSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_DISPLAYS) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.DisplaySettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_CALLSET) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.phone", "com.android.phone.CallFeaturesSetting");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				}
 			}
 		});
 		
 		quickActionss.setOnDismissListener(new QuickAction.OnDismissListener() {			
 			@Override
 			public void onDismiss() {
 			}
                 });
 
             ActionItem strgItem = new ActionItem(ID_STORAGE, "Storage settings");
             ActionItem prfleItem = new ActionItem(ID_PROFILE, "Profile settings");
             ActionItem prvcyItem = new ActionItem(ID_PRIVACY, "Privacy settings");
             ActionItem datetimeItem = new ActionItem(ID_DATETIME, "Date and Time");
             ActionItem langkeyItem = new ActionItem(ID_LANGKEY, "Language and Keyboard");
             ActionItem voicItem = new ActionItem(ID_VOICEN, "Voice input n output");
             ActionItem accsItem = new ActionItem(ID_ACCESS, "Accessibility");
             ActionItem dvlpItem = new ActionItem(ID_DEVELOP, "Development");
             ActionItem adwItem = new ActionItem(ID_ADWLAUNCHER, "ADW settings");
 
             final QuickAction quickActionrr = new QuickAction(getContext(), QuickAction.VERTICAL);
 
             quickActionrr.addActionItem(strgItem);
             quickActionrr.addActionItem(prfleItem);
             quickActionrr.addActionItem(prvcyItem);
             quickActionrr.addActionItem(datetimeItem);
             quickActionrr.addActionItem(langkeyItem);
             quickActionrr.addActionItem(voicItem);
             quickActionrr.addActionItem(accsItem);
             quickActionrr.addActionItem(dvlpItem);
             quickActionrr.addActionItem(adwItem);
 
 		quickActionrr.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
 			@Override
 			public void onItemClick(QuickAction source, int pos, int actionId) {				
 				ActionItem actionItem = quickAction.getActionItem(pos);
 				if (actionId == ID_STORAGE) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.Memory");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_PROFILE) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.ProfileList");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_PRIVACY) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.PrivacySettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_DATETIME) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.DateTimeSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_LANGKEY) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_VOICEN) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.VoiceInputOutputSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_ACCESS) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.AccessibilitySettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_DEVELOP) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.settings", "com.android.settings.DevelopmentSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				} else if (actionId == ID_ADWLAUNCHER) {
                                     Intent intent = new Intent(Intent.ACTION_MAIN);
                                     intent.setClassName("com.android.launcher", "com.android.launcher.MyLauncherSettings");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     getContext().startActivity(intent);
 				}
 			}
 		});
 		
 		quickActionrr.setOnDismissListener(new QuickAction.OnDismissListener() {			
 			@Override
 			public void onDismiss() {
 			}
                 });
 
             ActionItem bckItem = new ActionItem(ID_BACKILL, "KillAll app");
             ActionItem sscItem = new ActionItem(ID_SCREENSHOT, "Screenshots");
 
             final QuickAction quickActionmm = new QuickAction(getContext(), QuickAction.VERTICAL);
 
             quickActionmm.addActionItem(bckItem);
             quickActionmm.addActionItem(sscItem);
 
 		quickActionmm.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
 			@Override
 			public void onItemClick(QuickAction source, int pos, int actionId) {				
 				ActionItem actionItem = quickAction.getActionItem(pos);
 				if (actionId == ID_BACKILL) {
                                     if (mLongPressBackKills) {
                                         simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                                     } else {
                                         Toast toast = Toast.makeText(mContext, "Enable Kill app back button option to use this!",Toast.LENGTH_LONG);
                                         toast.show();
                                     }
 				} else if (actionId == ID_SCREENSHOT) {
                                     Intent intent = new Intent("android.intent.action.SCREENSHOT");
                                     getContext().sendBroadcast(intent);
 				}
 			}
 		});
 		
 		quickActionmm.setOnDismissListener(new QuickAction.OnDismissListener() {			
 			@Override
 			public void onDismiss() {
 			}
                 });
 
             mHomeButton = (ImageButton)findViewById(R.id.home);
             mHomeButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                       if(mShowHome == 1) {
                         if(DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       } else if(mShowHome == 4) {
                         if(DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       } else if(mShowHome == 2) {
                         if(DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       } else if(mShowHome == 3) {
                         if(DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       } else if(mShowHome == 5) {
                         if(DEBUG) Slog.i(TAG, "Quick clicked");
                         if (mShowVol) {
                            mShowVol = false;
                         } else {
                            mShowVol = true;
                         }
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       } else if(mShowHome == 6) {
                             boolean mCustomHomeAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_HOME_APP_TOGGLE, 0) == 1);
 
                             if(mCustomHomeAppToggle){
                                 runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_HOME_APP_ACTIVITY));
                             }
                         updateNaviButtons();
                         mHomeButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetHome, 70);
                       }
                     }
                 }
             );
             mHomeButton.setOnLongClickListener(
                 new ImageButton.OnLongClickListener() {
                     public boolean onLongClick(View v) {
                           if(mShowHome == 1) {
                              Intent intent = new Intent(Intent.ACTION_MAIN);
                              intent.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
                              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              getContext().startActivity(intent);
                              return true;
                           } else if(mShowHome == 4) {
                              quickActionss.show(v);
 			     quickActionss.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowHome == 2) {
                              quickActionmm.show(v);
 			     quickActionmm.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowHome == 3) {
                              quickActionrr.show(v);
 			     quickActionrr.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowHome == 5) {
                              quickAction.show(v);
 			     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else {
                              return false;
                           }
                     }
                 }
             );
             mMenuButton = (ImageButton)findViewById(R.id.menu);
             mMenuButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                       if(mShowMenu == 1) {
                         if(DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       } else if(mShowMenu == 4) {
                         if(DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       } else if(mShowMenu == 2) {
                         if(DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       } else if(mShowMenu == 3) {
                         if(DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       } else if(mShowMenu == 5) {
                         if(DEBUG) Slog.i(TAG, "Quick clicked");
                         if (mShowVol) {
                            mShowVol = false;
                         } else {
                            mShowVol = true;
                         }
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       } else if(mShowMenu == 6) {
                             boolean mCustomMenuAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_MENU_APP_TOGGLE, 0) == 1);
 
                             if(mCustomMenuAppToggle){
                                 runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_MENU_APP_ACTIVITY));
                             }
                         updateNaviButtons();
                         mMenuButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetMenu, 70);
                       }
                     }
                 }
             );
             mMenuButton.setOnLongClickListener(
                 new ImageButton.OnLongClickListener() {
                     public boolean onLongClick(View v) {
                           if(mShowMenu == 1) {
                              Intent intent = new Intent(Intent.ACTION_MAIN);
                              intent.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
                              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              getContext().startActivity(intent);
                              return true;
                           } else if(mShowMenu == 4) {
                              quickActionss.show(v);
 			     quickActionss.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowMenu == 2) {
                              quickActionmm.show(v);
 			     quickActionmm.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowMenu == 3) {
                              quickActionrr.show(v);
 			     quickActionrr.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowMenu == 5) {
                              quickAction.show(v);
 			     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else {
                              return false;
                           }
                     }
                 }
             );
             mBackButton = (ImageButton)findViewById(R.id.back);
             mBackButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                       if(mShowBack == 1) {
                         if(DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       } else if(mShowBack == 4) {
                         if(DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       } else if(mShowBack == 2) {
                         if(DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       } else if(mShowBack == 3) {
                         if(DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       } else if(mShowBack == 5) {
                         if(DEBUG) Slog.i(TAG, "Quick clicked");
                         if (mShowVol) {
                            mShowVol = false;
                         } else {
                            mShowVol = true;
                         }
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       } else if(mShowBack == 6) {
                             boolean mCustomBackAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_BACK_APP_TOGGLE, 0) == 1);
 
                             if(mCustomBackAppToggle){
                                 runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_BACK_APP_ACTIVITY));
                             }
                         updateNaviButtons();
                         mBackButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetBack, 70);
                       }
                     }
                 }
             );
             mBackButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if(mShowBack == 1) {
                              Intent intent = new Intent(Intent.ACTION_MAIN);
                              intent.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
                              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              getContext().startActivity(intent);
                              return true;
                           } else if(mShowBack == 4) {
                              quickActionss.show(v);
 			     quickActionss.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowBack == 2) {
                              quickActionmm.show(v);
 			     quickActionmm.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowBack == 3) {
                              quickActionrr.show(v);
 			     quickActionrr.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowBack == 5) {
                              quickAction.show(v);
 			     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mSearchButton = (ImageButton)findViewById(R.id.search);
             mSearchButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                       if(mShowSearch == 1) {
                         if(DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       } else if(mShowSearch == 4) {
                         if(DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       } else if(mShowSearch == 2) {
                         if(DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       } else if(mShowSearch == 3) {
                         if(DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       } else if(mShowSearch == 5) {
                         if(DEBUG) Slog.i(TAG, "Quick clicked");
                         if (mShowVol) {
                            mShowVol = false;
                         } else {
                            mShowVol = true;
                         }
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       } else if(mShowSearch == 6) {
                             boolean mCustomSearchAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_NAVISEARCH_APP_TOGGLE, 0) == 1);
 
                             if(mCustomSearchAppToggle){
                                 runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_NAVISEARCH_APP_ACTIVITY));
                             }
                         updateNaviButtons();
                         mSearchButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetSearch, 70);
                       }
                     }
                 }
             );
             mSearchButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if(mShowSearch == 1) {
                              Intent intent = new Intent(Intent.ACTION_MAIN);
                              intent.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
                              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              getContext().startActivity(intent);
                              return true;
                           } else if(mShowSearch == 4) {
                              quickActionss.show(v);
 			     quickActionss.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowSearch == 2) {
                              quickActionmm.show(v);
 			     quickActionmm.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowSearch == 3) {
                              quickActionrr.show(v);
 			     quickActionrr.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowSearch == 5) {
                              quickAction.show(v);
 			     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
             mVolUpButton = (ImageButton)findViewById(R.id.volup);
             mVolUpButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                         if(DEBUG) Slog.i(TAG, "VolUp clicked");
                         simulateKeypress(KeyEvent.KEYCODE_VOLUME_UP);
                         mVolUpButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetVolUp, 70);
                     }
                 }
             );
             mVolDownButton = (ImageButton)findViewById(R.id.voldown);
             mVolDownButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                         if(DEBUG) Slog.i(TAG, "VolDown clicked");
                         simulateKeypress(KeyEvent.KEYCODE_VOLUME_DOWN);
                         mVolDownButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetVolDown, 70);
                     }
                 }
             );
             mQuickButton = (ImageButton)findViewById(R.id.quicker);
             mQuickButton.setOnClickListener(
                 new ImageButton.OnClickListener() {
                     public void onClick(View v) {
                       if(mShowQuicker == 0) {
                         if(DEBUG) Slog.i(TAG, "Home clicked");
                         simulateKeypress(KeyEvent.KEYCODE_HOME);
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       } else if(mShowQuicker == 3) {
                         if(DEBUG) Slog.i(TAG, "Menu clicked");
                         simulateKeypress(KeyEvent.KEYCODE_MENU);
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       } else if(mShowQuicker == 1) {
                         if(DEBUG) Slog.i(TAG, "Back clicked");
                         simulateKeypress(KeyEvent.KEYCODE_BACK);
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       } else if(mShowQuicker == 2) {
                         if(DEBUG) Slog.i(TAG, "Search clicked");
                         simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       } else if(mShowQuicker == 4) {
                         if(DEBUG) Slog.i(TAG, "Quick clicked");
                         if (mShowVol) {
                            mShowVol = false;
                         } else {
                            mShowVol = true;
                         }
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       } else if(mShowQuicker == 5) {
                             boolean mCustomQuickerAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_QUICK_APP_TOGGLE, 0) == 1);
 
                             if(mCustomQuickerAppToggle){
                                 runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_QUICK_APP_ACTIVITY));
                             }
                         updateNaviButtons();
                         mQuickButton.setBackgroundResource(R.drawable.navibar_touch);
                         mHandler.postDelayed(mResetQuick, 70);
                       }
                     }
                 }
             );
             mQuickButton.setOnLongClickListener(
                     new ImageButton.OnLongClickListener() {
                         public boolean onLongClick(View v) {
                           if(mShowQuicker == 0) {
                              Intent intent = new Intent(Intent.ACTION_MAIN);
                              intent.setClassName("com.android.tmanager", "com.android.tmanager.TaskManagerActivity");
                              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                              getContext().startActivity(intent);
                              return true;
                           } else if(mShowQuicker == 3) {
                              quickActionss.show(v);
 			     quickActionss.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowQuicker == 1) {
                              quickActionmm.show(v);
 			     quickActionmm.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowQuicker == 2) {
                              quickActionrr.show(v);
 			     quickActionrr.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else if(mShowQuicker == 4) {
                              quickAction.show(v);
 			     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
                              return true;
                           } else {
                              return false;
                           }
                         }
                     }
                 );
 
             // set up settings observer
             SettingsObserver settingsObserver = new SettingsObserver(mHandler);
             settingsObserver.observe();
         }
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
     }
 
     private boolean isEventInButton(final ImageButton button, final MotionEvent event) {
         return mNVShow && button != null
             && button.getLeft() <= event.getRawX()
             && button.getRight() >= event.getRawX()
             && button.getTop() <= event.getRawY()
             && button.getBottom() >= event.getRawY();
         }
 
     public void reorient() {
          ContentResolver resolver = mContext.getContentResolver();
          mNVShow = (Settings.System.getInt(resolver,
                     Settings.System.SHOW_NAVI_BUTTONS, 1) == 1);
 
         if (mNVShow) {
            mNaviAdd.setVisibility(View.VISIBLE);
         } else {
            mNaviAdd.setVisibility(View.GONE);
         }
         updateNaviButtons();
     }
 
     public void setIMEVisible(boolean visible) {
         mInputShow = visible;
         updateNaviButtons();
     }
 
     public void setNaviVisible(boolean visible) {
       if (mShowNV) {
         if (visible) {
            mNaviBackground.setVisibility(View.VISIBLE);
         } else {
            mNaviBackground.setVisibility(View.GONE);
         }
         mVisible = visible;
         updateNaviButtons();
       }
     }
 
     public void setHidden(final boolean hide) {
         if (hide == mHidden) return;
 
         mHidden = hide;
       if (mShowNV) {
         if (!hide) {
            mSoftButtons.setVisibility(View.VISIBLE);
         } else {
            mSoftButtons.setVisibility(View.GONE);
         }
         updateNaviButtons();
       }
     }
 
     public boolean onTouchEvent(final MotionEvent event){
         if(!mNVShow)
             return super.onTouchEvent(event);
 
         if(isEventInButton(mHomeButton, event)) {
             mHomeButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mMenuButton, event)) {
             mMenuButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mBackButton, event)) {
             mBackButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mSearchButton, event)) {
             mSearchButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mVolUpButton, event)) {
             mVolUpButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mVolDownButton, event)) {
             mVolDownButton.onTouchEvent(event);
             return true;
         }
         if(isEventInButton(mQuickButton, event)) {
             mQuickButton.onTouchEvent(event);
             return true;
         }
 
         return super.onTouchEvent(event);
     }
 
     private void updateNaviButtons() {
 
         if (!mNVShow)
             return;
 
         // toggle visibility of buttons - at first, toggle all visible
         mHomeButton.setVisibility(View.VISIBLE);
         mMenuButton.setVisibility(View.VISIBLE);
         mBackButton.setVisibility(View.VISIBLE);
         mSearchButton.setVisibility(View.VISIBLE);
         mVolUpButton.setVisibility(View.VISIBLE);
         mVolDownButton.setVisibility(View.VISIBLE);
         mQuickButton.setVisibility(View.VISIBLE);
 
         if (mVisible && mShowNV) {
            mNaviBackground.setVisibility(View.VISIBLE);
            mSoftButtons.setVisibility(View.VISIBLE);
         }
 
         if(!mShowNV) {
            mNaviBackground.setVisibility(View.GONE);
            mVisible = true;
         }
 
         // now toggle off unneeded stuff
         if(mShowHome == 0)
             mHomeButton.setVisibility(View.INVISIBLE);
         
         if(mShowMenu == 0)
             mMenuButton.setVisibility(View.INVISIBLE);
         
         if(mShowBack == 0)
             mBackButton.setVisibility(View.INVISIBLE);
         
         if(mShowSearch == 0)
             mSearchButton.setVisibility(View.INVISIBLE);
 
         if(!mShowVol) {
             mVolUpButton.setVisibility(View.GONE);
             mVolDownButton.setVisibility(View.GONE);
         }
 
         mHandler.postDelayed(mResetHome, 10);
         mHandler.postDelayed(mResetMenu, 10);
         mHandler.postDelayed(mResetBack, 10);
         mHandler.postDelayed(mResetSearch, 10);
         mHandler.postDelayed(mResetQuick, 10);
     }
 
     private void runCustomApp(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                         | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                 getContext().startActivity(i);
             } catch (URISyntaxException e) {
 
             } catch (ActivityNotFoundException e) {
 
             }
         }
     }
 
     private void runCustomIconHome(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 PackageManager pm = getContext().getPackageManager();
                 ActivityInfo ai = i.resolveActivityInfo(pm,PackageManager.GET_ACTIVITIES);
                 if (ai != null) {
                         Bitmap iconBmp = ((BitmapDrawable)ai.loadIcon(pm)).getBitmap();
                         Bitmap jogBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_sysbar_apps);
                         int jogWidth = jogBmp.getWidth();
                         int sqSide = (int) (jogWidth / Math.sqrt(2));
                         mCustomHomeIcon = Bitmap.createScaledBitmap(iconBmp, sqSide, sqSide, true);
                 }
             } catch (URISyntaxException e) {
             }
         }
     }
 
     private void runCustomIconMenu(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 PackageManager pm = getContext().getPackageManager();
                 ActivityInfo ai = i.resolveActivityInfo(pm,PackageManager.GET_ACTIVITIES);
                 if (ai != null) {
                         Bitmap iconBmp = ((BitmapDrawable)ai.loadIcon(pm)).getBitmap();
                         Bitmap jogBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_sysbar_apps);
                         int jogWidth = jogBmp.getWidth();
                         int sqSide = (int) (jogWidth / Math.sqrt(2));
                         mCustomMenuIcon = Bitmap.createScaledBitmap(iconBmp, sqSide, sqSide, true);
                 }
             } catch (URISyntaxException e) {
             }
         }
     }
 
     private void runCustomIconBack(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 PackageManager pm = getContext().getPackageManager();
                 ActivityInfo ai = i.resolveActivityInfo(pm,PackageManager.GET_ACTIVITIES);
                 if (ai != null) {
                         Bitmap iconBmp = ((BitmapDrawable)ai.loadIcon(pm)).getBitmap();
                         Bitmap jogBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_sysbar_apps);
                         int jogWidth = jogBmp.getWidth();
                         int sqSide = (int) (jogWidth / Math.sqrt(2));
                         mCustomBackIcon = Bitmap.createScaledBitmap(iconBmp, sqSide, sqSide, true);
                 }
             } catch (URISyntaxException e) {
             }
         }
     }
 
     private void runCustomIconSearch(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 PackageManager pm = getContext().getPackageManager();
                 ActivityInfo ai = i.resolveActivityInfo(pm,PackageManager.GET_ACTIVITIES);
                 if (ai != null) {
                         Bitmap iconBmp = ((BitmapDrawable)ai.loadIcon(pm)).getBitmap();
                         Bitmap jogBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_sysbar_apps);
                         int jogWidth = jogBmp.getWidth();
                         int sqSide = (int) (jogWidth / Math.sqrt(2));
                         mCustomSearchIcon = Bitmap.createScaledBitmap(iconBmp, sqSide, sqSide, true);
                 }
             } catch (URISyntaxException e) {
             }
         }
     }
 
     private void runCustomIconQuick(String uri) {
         if (uri != null) {
             try {
                 Intent i = Intent.parseUri(uri, 0);
                 PackageManager pm = getContext().getPackageManager();
                 ActivityInfo ai = i.resolveActivityInfo(pm,PackageManager.GET_ACTIVITIES);
                 if (ai != null) {
                         Bitmap iconBmp = ((BitmapDrawable)ai.loadIcon(pm)).getBitmap();
                         Bitmap jogBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_sysbar_apps);
                         int jogWidth = jogBmp.getWidth();
                         int sqSide = (int) (jogWidth / Math.sqrt(2));
                         mCustomQuickIcon = Bitmap.createScaledBitmap(iconBmp, sqSide, sqSide, true);
                 }
             } catch (URISyntaxException e) {
             }
         }
     }
 
     Runnable mResetHome = new Runnable() {
         public void run() {
             if(mShowHome == 1) {
                mHomeButton.setImageBitmap(null);
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_home);
             } else if(mShowHome == 2) {
                mHomeButton.setImageBitmap(null);
                if (mInputShow) {
                    mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
                } else {
                    mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_back);
                }
             } else if(mShowHome == 3) {
                mHomeButton.setImageBitmap(null);
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_search);
             } else if(mShowHome == 4) {
                mHomeButton.setImageBitmap(null);
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_menu);
             } else if(mShowHome == 5) {
                mHomeButton.setImageBitmap(null);
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_quickna);
             } else if(mShowHome == 6) {
                boolean mCustomHomeAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.USE_CUSTOM_HOME_APP_TOGGLE, 0) == 1);
 
                if(mCustomHomeAppToggle){
                     runCustomIconHome(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_HOME_APP_ACTIVITY));
                     if (mCustomHomeIcon != null)
                         mHomeButton.setImageBitmap(mCustomHomeIcon);
                }
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_nihil);
             } else {
                mHomeButton.setImageBitmap(null);
                mHomeButton.setBackgroundResource(R.drawable.ic_sysbar_apps);
             }
         }
     };
 
     Runnable mResetBack = new Runnable() {
         public void run() {
             if(mShowBack == 1) {
                mBackButton.setImageBitmap(null);
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_home);
             } else if(mShowBack == 2) {
                mBackButton.setImageBitmap(null);
                if (mInputShow) {
                    mBackButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
                } else {
                    mBackButton.setBackgroundResource(R.drawable.ic_sysbar_back);
                }
             } else if(mShowBack == 3) {
                mBackButton.setImageBitmap(null);
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_search);
             } else if(mShowBack == 4) {
                mBackButton.setImageBitmap(null);
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_menu);
             } else if(mShowBack == 5) {
                mBackButton.setImageBitmap(null);
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_quickna);
             } else if(mShowBack == 6) {
                boolean mCustomBackAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.USE_CUSTOM_BACK_APP_TOGGLE, 0) == 1);
 
                if(mCustomBackAppToggle){
                     runCustomIconBack(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_BACK_APP_ACTIVITY));
                     if (mCustomBackIcon != null)
                         mBackButton.setImageBitmap(mCustomBackIcon);
                }
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_nihil);
             } else {
                mBackButton.setImageBitmap(null);
                mBackButton.setBackgroundResource(R.drawable.ic_sysbar_apps);
             }
         }
     };
 
     Runnable mResetSearch = new Runnable() {
         public void run() {
             if(mShowSearch == 1) {
                mSearchButton.setImageBitmap(null);
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_home);
             } else if(mShowSearch == 2) {
                mSearchButton.setImageBitmap(null);
                if (mInputShow) {
                    mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
                } else {
                    mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_back);
                }
             } else if(mShowSearch == 3) {
                mSearchButton.setImageBitmap(null);
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_search);
             } else if(mShowSearch == 4) {
                mSearchButton.setImageBitmap(null);
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_menu);
             } else if(mShowSearch == 5) {
                mSearchButton.setImageBitmap(null);
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_quickna);
             } else if(mShowSearch == 6) {
                boolean mCustomSearchAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.USE_CUSTOM_NAVISEARCH_APP_TOGGLE, 0) == 1);
 
                if(mCustomSearchAppToggle){
                     runCustomIconSearch(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_NAVISEARCH_APP_ACTIVITY));
                     if (mCustomSearchIcon != null)
                         mSearchButton.setImageBitmap(mCustomSearchIcon);
                }
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_nihil);
             } else {
                mSearchButton.setImageBitmap(null);
                mSearchButton.setBackgroundResource(R.drawable.ic_sysbar_apps);
             }
         }
     };
 
     Runnable mResetMenu = new Runnable() {
         public void run() {
             if(mShowMenu == 1) {
                mMenuButton.setImageBitmap(null);
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_home);
             } else if(mShowMenu == 2) {
                mMenuButton.setImageBitmap(null);
                if (mInputShow) {
                    mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
                } else {
                    mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_back);
                }
             } else if(mShowMenu == 3) {
                mMenuButton.setImageBitmap(null);
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_search);
             } else if(mShowMenu == 4) {
                mMenuButton.setImageBitmap(null);
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_menu);
             } else if(mShowMenu == 5) {
                mMenuButton.setImageBitmap(null);
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_quickna);
             } else if(mShowMenu == 6) {
                boolean mCustomMenuAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.USE_CUSTOM_MENU_APP_TOGGLE, 0) == 1);
 
                if(mCustomMenuAppToggle){
                     runCustomIconMenu(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_MENU_APP_ACTIVITY));
                     if (mCustomMenuIcon != null)
                         mMenuButton.setImageBitmap(mCustomMenuIcon);
                }
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_nihil);
             } else {
                mMenuButton.setImageBitmap(null);
                mMenuButton.setBackgroundResource(R.drawable.ic_sysbar_apps);
             }
         }
     };
 
     Runnable mResetQuick = new Runnable() {
         public void run() {
             if(mShowQuicker == 0) {
                mQuickButton.setImageBitmap(null);
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_home);
             } else if(mShowQuicker == 1) {
                mQuickButton.setImageBitmap(null);
                if (mInputShow) {
                    mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
                } else {
                    mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_back);
                }
             } else if(mShowQuicker == 2) {
                mQuickButton.setImageBitmap(null);
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_search);
             } else if(mShowQuicker == 3) {
                mQuickButton.setImageBitmap(null);
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_menu);
             } else if(mShowQuicker == 4) {
                mQuickButton.setImageBitmap(null);
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_quickna);
             } else if(mShowQuicker == 5) {
                boolean mCustomQuickerAppToggle = (Settings.System.getInt(getContext().getContentResolver(),
                         Settings.System.USE_CUSTOM_QUICK_APP_TOGGLE, 0) == 1);
 
                if(mCustomQuickerAppToggle){
                     runCustomIconQuick(Settings.System.getString(getContext().getContentResolver(),
                                     Settings.System.USE_CUSTOM_QUICK_APP_ACTIVITY));
                     if (mCustomQuickIcon != null)
                         mQuickButton.setImageBitmap(mCustomQuickIcon);
                }
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_nihil);
             } else {
                mQuickButton.setImageBitmap(null);
                mQuickButton.setBackgroundResource(R.drawable.ic_sysbar_apps);
             }
         }
     };
 
     Runnable mResetVolUp = new Runnable() {
         public void run() {
             mVolUpButton.setBackgroundResource(R.drawable.ic_sysbar_volup);
         }
     };
 
     Runnable mResetVolDown = new Runnable() {
         public void run() {
             mVolDownButton.setBackgroundResource(R.drawable.ic_sysbar_voldown);
         }
     };
 
     /**
      * Runnable to hold simulate a keypress.
      *
      * This is executed in a separate Thread to avoid blocking
      */
     private void simulateKeypress(final int keyCode) {
         new Thread( new KeyEventInjector( keyCode ) ).start();
     }
 
     private class KeyEventInjector implements Runnable {
         private int keyCode;
 
         KeyEventInjector(final int keyCode) {
             this.keyCode = keyCode;
         }
 
         public void run() {
             try {
                 if(! (IWindowManager.Stub
                     .asInterface(ServiceManager.getService("window")))
                          .injectKeyEvent(
                               new KeyEvent(KeyEvent.ACTION_DOWN, keyCode), true) ) {
                                    Slog.w(TAG, "Key down event not injected");
                                    return;
                               }
                 if(! (IWindowManager.Stub
                     .asInterface(ServiceManager.getService("window")))
                          .injectKeyEvent(
                              new KeyEvent(KeyEvent.ACTION_UP, keyCode), true) ) {
                                   Slog.w(TAG, "Key up event not injected");
                              }
            } catch(RemoteException ex) {
                Slog.w(TAG, "Error injecting key event", ex);
            }
         }
     }
 }
