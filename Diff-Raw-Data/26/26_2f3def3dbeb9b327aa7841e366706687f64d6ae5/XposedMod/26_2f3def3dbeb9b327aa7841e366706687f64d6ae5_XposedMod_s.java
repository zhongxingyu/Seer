 package com.mohammadag.xperiaflipsettings;
 
 import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
 import static de.robv.android.xposed.XposedHelpers.findClass;
 import static de.robv.android.xposed.XposedHelpers.getBooleanField;
 import static de.robv.android.xposed.XposedHelpers.getObjectField;
 import static de.robv.android.xposed.XposedHelpers.setObjectField;
 import android.animation.Animator;
 import android.animation.Animator.AnimatorListener;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.ObjectAnimator;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.content.res.XModuleResources;
 import android.content.res.XmlResourceParser;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import de.robv.android.xposed.IXposedHookInitPackageResources;
 import de.robv.android.xposed.IXposedHookLoadPackage;
 import de.robv.android.xposed.IXposedHookZygoteInit;
 import de.robv.android.xposed.XC_MethodHook;
 import de.robv.android.xposed.XC_MethodReplacement;
 import de.robv.android.xposed.XSharedPreferences;
 import de.robv.android.xposed.XposedBridge;
 import de.robv.android.xposed.XposedHelpers;
 import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
 import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
 
 public class XposedMod implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
 
 	private static String MODULE_PATH = null;
 	private int mHeaderHeight;
 	private int mSettingsButtonsContainerId;
 	private XmlResourceParser mClearButtonXml;
 	private Drawable mClearButtonDrawable;
 	private Drawable mClearButtonBackground;
 	private int mNotificationLightsOutId;
 	private Drawable mSettingsButtonDrawable;
 	private XSharedPreferences mPreferences;
 
 	@Override
 	public void initZygote(StartupParam startupParam) throws Throwable {
 		MODULE_PATH = startupParam.modulePath;
 		mPreferences = new XSharedPreferences(this.getClass().getPackage().getName());
 		mPreferences.makeWorldReadable();
 	}
 
 	@Override
 	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
 		if (!lpparam.packageName.equals("com.android.systemui"))
 			return;
 
 		/* Hide the rows of Tools in the constructor */
 		Class<?> ToolsMain = findClass("com.sonymobile.systemui.statusbar.tools.ToolsMain", lpparam.classLoader);
 		XposedBridge.hookAllConstructors(ToolsMain, new XC_MethodHook() {
 			@Override
 			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
 				LinearLayout[] layouts = (LinearLayout[]) getObjectField(param.thisObject, "mRows");
 				layouts[0].setVisibility(View.GONE);
 				layouts[1].setVisibility(View.GONE);
 			}
 		});
 
 		/* The rows of Tools are shown in this method. We hide them again.
 		 * A better implementation is to completely disable Tools so the garbage collector can
 		 * dispose of them and free up memory. That might prove too complex if Sony doesn't
 		 * check for null values.
 		 */
 		findAndHookMethod(ToolsMain, "refreshLayout", new XC_MethodHook() {
 			@Override
 			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
 				LinearLayout[] layouts = (LinearLayout[]) getObjectField(param.thisObject, "mRows");
 				layouts[0].setVisibility(View.GONE);
 				layouts[1].setVisibility(View.GONE);
 
 				View view = (View) getObjectField(param.thisObject, "mDivider");
 				view.setVisibility(View.GONE);
 			}
 		});
 
 		/* The header's height is calculated in this method. This allows it to take into account
 		 * how many rows of Tools we have. Since we're disabling Tools, we simply return this constant
 		 * value.
 		 */
 		Class<?> PhoneStatusBar = findClass("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader);
 		findAndHookMethod(PhoneStatusBar, "getNotificationPanelHeaderHeight", new XC_MethodReplacement() {
 			@Override
 			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
 				return mHeaderHeight;
 			}
 		});
 
 		findAndHookMethod(PhoneStatusBar, "makeStatusBarView", new XC_MethodHook() {
 			@Override
 			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
 				final Context mContext = (Context) getObjectField(param.thisObject, "mContext");
 				/* The settings button itself is hidden. */
 				View settingsButton = (View) getObjectField(param.thisObject, "mSettingsButton");
 				settingsButton.setVisibility(View.VISIBLE);
 				if (mPreferences.getBoolean(Constants.SETTINGS_KEY_REPLACE_SETTINGS_ICON, true)) {
 					if (settingsButton instanceof ImageView) {
 						((ImageView) settingsButton).setImageDrawable(mSettingsButtonDrawable);
 					}
 				}
 
 				final Object mStatusBar = param.thisObject;
 				/* Why not? */
 				settingsButton.setOnLongClickListener(new OnLongClickListener() {	
 					@Override
 					public boolean onLongClick(View v) {
 						XposedHelpers.callMethod(mStatusBar, "startActivityDismissingKeyguard",
 								new Intent(android.provider.Settings.ACTION_SETTINGS), true);
 						return true;
 					}
 				});
 
 				/* The container holding it is hidden, show it. */
 				View mStatusBarWindow = (View) getObjectField(param.thisObject, "mStatusBarWindow");
 				mStatusBarWindow.findViewById(mSettingsButtonsContainerId).setVisibility(View.VISIBLE);
 
 				/* Now replace the ugly textual button with the AOSP one. */
 				View mClearButton = (View) getObjectField(param.thisObject, "mClearButton");
 				CharSequence contentDescription = mClearButton.getContentDescription();
 				ViewGroup parent = (ViewGroup) mClearButton.getParent();
 				int index = parent.indexOfChild(mClearButton);
 				parent.removeView(mClearButton);
 				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				ImageView aospClearButton = (ImageView) inflater.inflate(mClearButtonXml, parent, false);
 				parent.addView(aospClearButton, index);
 				aospClearButton.setOnClickListener((OnClickListener) getObjectField(param.thisObject,
 						"mClearButtonListener"));
 				aospClearButton.setAlpha(0f);
 				aospClearButton.setVisibility(View.INVISIBLE);
 				aospClearButton.setEnabled(false);
 				aospClearButton.setImageDrawable(mClearButtonDrawable);
 				aospClearButton.setBackground(mClearButtonBackground);
 				aospClearButton.setContentDescription(contentDescription);
 
 				/* Set the value for mClearButton, this works because it's of type View in
 				 * the original code, and both Button and ImageView extend View.
 				 */
 				setObjectField(param.thisObject, "mClearButton", aospClearButton);
 			}
 		});
 
 		/* The clear button gets hidden when we flip back to notifications from settings.
 		 * Although it would be cleaner to use an XC_MethodHook, this method is far too
 		 * complex to simply do things before or after it.
 		 * 
 		 * Much of this code is taken from AOSP's source, modified to use reflection.
 		 * Therefore, this method (setAreThereNotifications) replacement is technically
 		 * licensed under this license:
 		 */
 
 		/*
 		 * Copyright (C) 2010 The Android Open Source Project
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
 		findAndHookMethod(PhoneStatusBar, "setAreThereNotifications", new XC_MethodReplacement() {
 			@Override
 			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
 				Object mNotificationData = getObjectField(param.thisObject, "mNotificationData");
 				final boolean any = (Integer)XposedHelpers.callMethod(mNotificationData, "size") > 0;
 
 				final boolean clearable = any && (Boolean)XposedHelpers.callMethod(mNotificationData,
 						"hasClearableItems");
 
 				View mFlipSettingsView = (View) getObjectField(param.thisObject, "mFlipSettingsView");
 				View mScrollView = (View) getObjectField(param.thisObject, "mScrollView");
 				final View mClearButton = (View) getObjectField(param.thisObject, "mClearButton");
 
 				if (getBooleanField(param.thisObject, "mHasFlipSettings") 
 						&& mFlipSettingsView != null 
 						&& mFlipSettingsView.getVisibility() == View.VISIBLE
 						&& mScrollView.getVisibility() != View.VISIBLE) {
 					// the flip settings panel is unequivocally showing; we should not be shown
 					mClearButton.setVisibility(View.INVISIBLE);
 				} else if (mClearButton.isShown()) {
 					if (clearable != (mClearButton.getAlpha() == 1.0f)) {
 						ObjectAnimator clearAnimation = ObjectAnimator.ofFloat(
 								mClearButton, "alpha", clearable ? 1.0f : 0.0f).setDuration(250);
 						clearAnimation.addListener(new AnimatorListener() {
 							@Override
 							public void onAnimationEnd(Animator animation) {
 								if (mClearButton.getAlpha() <= 0.0f) {
 									mClearButton.setVisibility(View.INVISIBLE);
 								}
 							}
 
 							@Override
 							public void onAnimationStart(Animator animation) {
 								if (mClearButton.getAlpha() <= 0.0f) {
 									mClearButton.setVisibility(View.VISIBLE);
 								}
 							}
 
 							@Override
 							public void onAnimationCancel(Animator arg0) { }
 
 							@Override
 							public void onAnimationRepeat(Animator arg0) { }
 						});
 						clearAnimation.start();
 					}
 				} else {
 					mClearButton.setAlpha(clearable ? 1.0f : 0.0f);
 					mClearButton.setVisibility(clearable ? View.VISIBLE : View.INVISIBLE);
 				}
 				mClearButton.setEnabled(clearable);
 
 				View mStatusBarView = (View) getObjectField(param.thisObject, "mStatusBarView");
 				final View nlo = mStatusBarView.findViewById(mNotificationLightsOutId);
 				final boolean showDot = (any&&!(Boolean)XposedHelpers.callMethod(param.thisObject, "areLightsOn"));
 				if (showDot != (nlo.getAlpha() == 1.0f)) {
 					if (showDot) {
 						nlo.setAlpha(0f);
 						nlo.setVisibility(View.VISIBLE);
 					}
 					nlo.animate()
 					.alpha(showDot?1:0)
 					.setDuration(showDot?750:250)
 					.setInterpolator(new AccelerateInterpolator(2.0f))
 					.setListener(showDot ? null : new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator _a) {
 							nlo.setVisibility(View.GONE);
 						}
 					})
 					.start();
 				}
 				XposedHelpers.callMethod(param.thisObject, "updateCarrierLabelVisibility", false);
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
 		if (!resparam.packageName.equals("com.android.systemui")) 
 			return;
 
 		/* Since we're using reflection above, we can't access R.xxx.xxx, so we load the 
 		 * identifiers here.
 		 */
 		mSettingsButtonsContainerId = resparam.res.getIdentifier("settings_button_holder", "id", "com.android.systemui");
 		mNotificationLightsOutId = resparam.res.getIdentifier("notification_lights_out", "id", "com.android.systemui");
 
 		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);		
 		/* Resources for our AOSP-style clear button */
 		mClearButtonXml = modRes.getXml(R.layout.clear_button);
 		mClearButtonDrawable = modRes.getDrawable(R.drawable.ic_notify_clear);
 		mClearButtonBackground = modRes.getDrawable(R.drawable.ic_notify_button_bg);
 		mSettingsButtonDrawable = modRes.getDrawable(R.drawable.ic_notify_quicksettings);
 		mHeaderHeight = modRes.getDimensionPixelSize(R.dimen.notification_panel_header_height);
 
 		/* Set these configuration booleans to true, nice of Sony to leave the code that creates
 		 * the quick settings panels there and just simply disable them. Take some hints Samsung.
 		 */
 		resparam.res.setReplacement("com.android.systemui", "bool", "config_hasSettingsPanel", true);
 		resparam.res.setReplacement("com.android.systemui", "bool", "config_hasFlipSettingsPanel", true);
 
 		/* The header contains Sony's quick settings implementation (Tools). If we don't replace
 		 * these, then the header will have some black space. Replacing these isn't enough of though.
 		 * The black space is also calculated in a method hooked above.
 		 */
 		resparam.res.setReplacement("com.android.systemui", "dimen", "notification_panel_header_height",
 				modRes.fwd(R.dimen.notification_panel_header_height));
 		resparam.res.setReplacement("com.android.systemui", "dimen", "notification_panel_header_base_height",
 				modRes.fwd(R.dimen.notification_panel_header_base_height));
		
 		if (mPreferences.getBoolean(Constants.SETTINGS_KEY_REPLACE_HANDLE_BAR, true)) {
 			/* Replace the handle bar with the AOSP one */
 			resparam.res.setReplacement("com.android.systemui", "drawable", "status_bar_close",
 					modRes.fwd(R.drawable.status_bar_close));
 		}
		
 		if (mPreferences.getBoolean(Constants.SETTINGS_KEY_REPLACE_SETTINGS_ICON, true)) {
 			try {
 				resparam.res.setReplacement("com.android.systemui", "drawable", "ic_notify_setting",
 						modRes.fwd(R.drawable.ic_notify_settings));
 			} catch (Resources.NotFoundException e) { }
			
 			try {
 				resparam.res.setReplacement("com.android.systemui", "drawable", "ic_notify_settings",
 						modRes.fwd(R.drawable.ic_notify_settings));
 			} catch (Resources.NotFoundException e) {}
 		}
 
 		/* Not sure if this is needed, but since Sony left the original AOSP layouts available, we'll
 		 * replace theirs with the AOSP ones.
 		 */
 		Object superStatusBar =
 				resparam.res.getLayout(resparam.res.getIdentifier("super_status_bar", "layout", "com.android.systemui"));
 		resparam.res.setReplacement("com.android.systemui", "layout", "msim_super_status_bar", superStatusBar);
 
 		Object statusBar =
 				resparam.res.getLayout(resparam.res.getIdentifier("status_bar", "layout", "com.android.systemui"));
 		resparam.res.setReplacement("com.android.systemui", "layout", "msim_status_bar", statusBar);
 	}
 }
