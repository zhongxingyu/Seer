 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
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
 package com.meiste.greg.ptw;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdView;
 import com.google.android.gcm.GCMRegistrar;
 import com.meiste.greg.ptw.iab.IabHelper;
 import com.meiste.greg.ptw.iab.IabResult;
 import com.meiste.greg.ptw.iab.Inventory;
 import com.meiste.greg.ptw.iab.Purchase;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends SherlockFragmentActivity implements Eula.OnEulaAgreedTo {
 
     public static final String INTENT_TAB = "tab_select";
     private static final String LAST_TAB = "tab.last";
     private static final String SKU_AD_FREE = "ad_free";
     private static final int IAB_REQUEST = 10001;
 
     private IabHelper mHelper;
     private ViewPager mPager;
     private TitlePageIndicator mIndicator;
     private AdView mAdView;
     private AlertDialog mLegalDialog;
     private boolean mIsAdFree = false;
     private boolean mIabReady = false;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (BuildConfig.DEBUG) {
             GCMRegistrar.checkDevice(this);
             GCMRegistrar.checkManifest(this);
         }
 
         mHelper = new IabHelper(this, PTW.PUB_KEY);
         mHelper.enableDebugLogging(BuildConfig.DEBUG, PTW.TAG);
 
         // This has to be called before setContentView
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.main);
 
         // Need to explicitly set to false else it will incorrectly appear on
         // older Android versions. Must be after setContentView.
         setSupportProgressBarIndeterminateVisibility(false);
 
         if (Eula.show(this))
             onEulaAgreedTo();
 
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setOffscreenPageLimit(2);
         mPager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager(), this));
 
         mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
         mIndicator.setViewPager(mPager);
         mIndicator.setCurrentItem(getTab(getIntent()));
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         Util.log("Saving state: tab=" + mPager.getCurrentItem());
         Util.getState(this).edit().putInt(LAST_TAB, mPager.getCurrentItem()).commit();
 
         // Hide dialogs to prevent window leaks on orientation changes
         Eula.hide();
         if ((mLegalDialog != null) && (mLegalDialog.isShowing())) {
             mLegalDialog.dismiss();
         }
     }
 
     @Override
     public void onDestroy() {
         if (mAdView != null) {
             mAdView.removeAllViews();
             mAdView.destroy();
             mAdView = null;
         }
 
         GCMRegistrar.onDestroy(getApplicationContext());
 
         if (mHelper != null) {
             mHelper.dispose();
             mHelper = null;
         }
 
         super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(final Menu menu) {
         getSupportMenuInflater().inflate(R.menu.menu, menu);
 
         if (!mIsAdFree && mIabReady) {
             menu.add(Menu.NONE, R.string.ads_remove, Menu.NONE, R.string.ads_remove)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         }
 
         return true;
     }
 
     @SuppressLint("NewApi")
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
             startActivity(new Intent(this, EditPreferences.class));
             return true;
 
         case R.id.legal:
             final AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(R.string.legal);
             builder.setCancelable(true);
             builder.setPositiveButton(R.string.ok, null);
             builder.setMessage(R.string.legal_content);
             mLegalDialog = builder.create();
             mLegalDialog.show();
             return true;
 
         case R.string.ads_remove:
             try {
                 mHelper.launchPurchaseFlow(this, SKU_AD_FREE, IAB_REQUEST, mPurchaseFinishedListener);
             } catch (final NullPointerException e) {
                 // If the billing service disconnects (for example, when the
                 // Play Store auto-updates itself), then the helper class
                 // will attempt to dereference a null pointer (mService).
                 // Working around issue here instead of fixing Google code.
                 Util.log("Unable to launch purchase flow");
                 mIabReady = false;
                 invalidateOptionsMenu();
             }
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
             // Not handled by in-app billing
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     @Override
     public void onEulaAgreedTo() {
         RaceAlarm.set(this);
         QuestionAlarm.set(this);
 
         if (!GCMRegistrar.isRegistered(this)) {
             Util.log("Registering with GCM");
             GCMRegistrar.register(getApplicationContext(), PTW.GCM_SENDER_ID);
         } else {
             Util.log("Already registered with GCM: " + GCMRegistrar.getRegistrationId(getApplicationContext()));
         }
 
         mHelper.startSetup(mIabSetupListener);
     }
 
     private int getTab(final Intent intent) {
         // Recent applications caches intent with extras. Only want to listen
         // to INTENT_TAB extra if launched from notification.
         if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
             final int intent_tab = intent.getIntExtra(INTENT_TAB, -1);
             if (intent_tab >= 0) {
                 return intent_tab;
             }
         }
 
         return Util.getState(this).getInt(LAST_TAB, 0);
     }
 
     private void loadAd() {
         final AdRequest adRequest = new AdRequest();
         adRequest.addKeyword("NASCAR");
         adRequest.addKeyword("racing");
 
         if (BuildConfig.DEBUG) {
             adRequest.addTestDevice("CB529BCBD1E778FAD10EE145EE29045F"); // Atrix 4G
             adRequest.addTestDevice("36A52B9CBB347B995EA40ACDD0D36376"); // XOOM
             adRequest.addTestDevice("E64392AEFC7C9A13D2A6A76E9EA034C4"); // RAZR
         }
 
         mAdView = (AdView)findViewById(R.id.ad);
         mAdView.setAdListener(mAdListener);
         mAdView.loadAd(adRequest);
     }
 
     private final AdListener mAdListener = new AdListener() {
         @Override
         public void onReceiveAd(final Ad ad) {
             Util.log("onReceiveAd");
         }
 
         @Override
         public void onFailedToReceiveAd(final Ad ad, final ErrorCode err) {
             Util.log("onFailedToReceiveAd: " + err);
         }
 
         @Override
         public void onLeaveApplication(final Ad ad) {}
 
         @Override
         public void onPresentScreen(final Ad ad) {}
 
         @Override
         public void onDismissScreen(final Ad ad) {}
     };
 
     private final IabHelper.OnIabSetupFinishedListener mIabSetupListener =
             new IabHelper.OnIabSetupFinishedListener() {
         @Override
         public void onIabSetupFinished(final IabResult result) {
             if (result.isSuccess()) {
                mHelper.queryInventoryAsync(false, mGotInventoryListener);
             } else {
                 Util.log("Problem setting up in-app billing: " + result);
                 loadAd();
             }
         }
     };
 
     private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
             new IabHelper.QueryInventoryFinishedListener() {
         @SuppressLint("NewApi")
         @Override
         public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
             if (result.isFailure()) {
                 loadAd();
                 return;
             }
 
             mIsAdFree = inventory.hasPurchase(SKU_AD_FREE);
             mIabReady = true;
             invalidateOptionsMenu();
             if (!mIsAdFree) loadAd();
         }
     };
 
     private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
             new IabHelper.OnIabPurchaseFinishedListener() {
         @SuppressLint("NewApi")
         @Override
         public void onIabPurchaseFinished(final IabResult result, final Purchase purchase) {
             if (result.isFailure()) {
                 final String msg = MainActivity.this.getString(R.string.ads_error,
                         IabHelper.getResponseDesc(result.getResponse()));
                 Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                 return;
             }
 
             if (purchase.getSku().equals(SKU_AD_FREE)) {
                 Toast.makeText(MainActivity.this, R.string.ads_success, Toast.LENGTH_LONG).show();
                 mIsAdFree = true;
                 invalidateOptionsMenu();
 
                 if (mAdView != null) {
                     mAdView.removeAllViews();
                     mAdView.destroy();
                     mAdView = null;
                 }
             }
         }
     };
 }
