 package com.rubixconsulting.walletcracker;
 
 import android.animation.Animator;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.google.android.apps.analytics.easytracking.TrackedActivity;
 import com.rubixconsulting.walletcracker.BGLoader.Progress;
 import com.rubixconsulting.walletcracker.BGLoader.Status;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class WalletCrackerMain extends TrackedActivity implements WalletListener, ViewPager.OnPageChangeListener {
   static protected final String TAG = WalletCrackerMain.class.getSimpleName();
   protected final String RESTORE = ", can restore state";
   protected ViewPagerAdapter adapter;
   private Boolean initialized = false;
   private LogoAnimationState logoAnimationState = LogoAnimationState.STOPPED;
   private View logoView;
   private ViewPager pager;
   static protected final Float MAX_LOGO_ALPHA = 0.05f;
   static protected final Float MIN_LOGO_ALPHA = 0.0f;
   static protected final Long ANIMATION_DURATION = 1000l;
 
   enum LogoAnimationState {
     STOPPED,
     VISIBLE,
     FADING_IN,
     FADING_OUT,
     HIDDEN,
     CANCELED
   }
 
   @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.pager);
 
     adapter = new ViewPagerAdapter(this);
     adapter.addTab(R.string.pin,   PinFragment.class,   null);
     adapter.addTab(R.string.data,  DataFragment.class,  null);
     adapter.addTab(R.string.about, AboutFragment.class, null);
 
     pager = (ViewPager) findViewById(R.id.pager);
     TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.titles);
     pager.setAdapter(adapter);
     indicator.setViewPager(pager);
     indicator.setOnPageChangeListener(this);
 
     logoView = findViewById(R.id.rubixLogo);
 
     Log.i(TAG, "onCreate");
 
     if (savedInstanceState != null) {
       restoreState(savedInstanceState);
     } else {
       Log.i(TAG, "no state to restore");
     }
   }
 
   @Override protected void onResume() {
     super.onResume();
     Log.i(TAG, "onResume");
     BGLoader.addListener(this);
   }
 
   private void hideLogo() {
     logoView.setVisibility(View.INVISIBLE);
     logoView.setAlpha(MIN_LOGO_ALPHA);
     logoAnimationState = LogoAnimationState.HIDDEN;
   }
 
   @Override protected void onPause() {
     super.onPause();
     Log.i(TAG, "onPause" + (isFinishing() ? "Finishing" : ""));
     BGLoader.removeListener(this);
     hideDialog(ProgressDialogFragment.TAG);
   }
 
   protected void restoreState(Bundle savedState) {
     final Bundle state = savedState.getBundle(TAG);
 
     if (state == null) {
       return;
     }
 
     initialized = state.getBoolean("initialized", false);
 
     Log.d(TAG, "restoring currentItem: "+state.getInt("currentItem"));
     pager.setCurrentItem(state.getInt("currentItem", 0), false);
 
     String tmpAnimState = state.getString("logoAnimationState");
     if (tmpAnimState != null) {
       for(LogoAnimationState animState : LogoAnimationState.values()) {
         if (animState.toString().equals(tmpAnimState)) {
           logoAnimationState = animState;
           break;
         }
       }
     }
 
     switch (logoAnimationState) {
       case FADING_IN:
       case VISIBLE:
         logoView.setAlpha(MAX_LOGO_ALPHA);
         logoView.setVisibility(View.VISIBLE);
         break;
     }
   }
 
   @Override protected void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
 
     final Bundle state = new Bundle();
     Log.d(TAG, "onSaveInstanceState");
     Log.d(TAG, "saving currentItem: "+pager.getCurrentItem());
     state.putBoolean("initialized", initialized);
     state.putInt("currentItem", pager.getCurrentItem());
     state.putString("logoAnimationState", logoAnimationState.toString());
 
     outState.putBundle(TAG, state);
   }
 
   @Override public boolean onCreateOptionsMenu(Menu menu) {
     final MenuInflater inflater = getMenuInflater();
     Log.i(TAG, "onCreateOptionsMenu");
     inflater.inflate(R.menu.mainmenu, menu);
     return true;
   }
 
   @Override public boolean onOptionsItemSelected(MenuItem item) {
     super.onOptionsItemSelected(item);
     Log.i(TAG, "onOptionsItemSelected");
     switch (item.getItemId()) {
       case R.id.rebuild:
         Log.i(TAG, "user asked for rebuild");
         forceRebuild();
         break;
       case R.id.prefs:
         Log.i(TAG, "user asked for prefs");
         startActivity(new Intent(this, PreferencesMain.class));
         break;
       case R.id.showLicenses:
         Log.i(TAG, "user asked for licenses");
         startActivity(new Intent(this, LicensesMain.class));
         break;
     }
     return true;
   }
 
   private void forceRebuild() {
     Log.i(TAG, "force rebuild");
     new BGLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this, true);
   }
 
   private void showError(Integer messageId) {
     if ((messageId == null) || (messageId == 0)) {
       Log.i(TAG, "Not showing invalid error");
       return;
     }
 
     Log.e(TAG, "Error: " + getString(messageId));
 
     FragmentTransaction ft = getFragmentManager().beginTransaction();
 
     // remove any previous error fragments
     Fragment prev = getFragmentManager().findFragmentByTag(ErrorDialogFragment.TAG);
     if (prev != null) {
       ft.remove(prev);
     }
 
     // add the new error
     ft.addToBackStack(null);
     ErrorDialogFragment fragment = ErrorDialogFragment.newInstance(messageId);
     fragment.show(ft, ErrorDialogFragment.TAG);
 
     // see note about this in showProgress
     getFragmentManager().executePendingTransactions();
   }
 
   private void showProgress(Integer messageId, Integer progress, Integer numSteps) {
     ProgressDialogFragment fragment = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
 
     if (fragment == null) {
       Log.d(TAG, "creating new progress dialog");
       FragmentTransaction ft = getFragmentManager().beginTransaction();
       fragment = ProgressDialogFragment.newInstance(messageId, progress, numSteps);
       fragment.show(ft, ProgressDialogFragment.TAG);
 
       /*
        * progress is updated frequently, and the show commits the transaction.
        * the commit only schedules the changes to run on the activity's UI
        * thread "as soon as the thread is able to do so". however, if another
        * progress update comes in before the commit actually completes, then
        * another dialog will be created which will cause errors later.
        * run the following command to immediately execute the changes made by
        * the commit.
        */
       getFragmentManager().executePendingTransactions();
       return;
     }
 
     Log.d(TAG, "updating existing progress dialog");
     fragment.update(messageId, progress, numSteps);
   }
 
   private void hideDialog(String tag) {
     DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag(tag);
     if (fragment != null) {
       Log.d(TAG, "dismissing "+tag);
       fragment.dismiss();
     }
   }
 
   @Override  public void walletLoaded(Status result, DeviceInfoParser parser) {
     hideDialog(ProgressDialogFragment.TAG);
 
     switch (result) {
       case SUCCESS:
         initialized = true;
         animateLogo();
         break;
       case NO_WALLET:
         showError(R.string.wallet_not_found);
         break;
       case NO_ROOT:
         showError(R.string.root_not_found);
         break;
     }
   }
 
   @Override public void walletProgress(Progress progress, Integer numSteps) {
     int stringId = R.string.loading;
 
     switch (progress) {
       case WALLET:
         stringId = R.string.loading_wallet;
         break;
       case ROOT:
         stringId = R.string.loading_root;
         break;
       case COPYING:
         stringId = R.string.loading_copying;
         break;
       case CRACKING:
         stringId = R.string.loading_cracking;
         break;
     }
 
     showProgress(stringId, ((progress == null) ? null : progress.ordinal()), numSteps);
   }
 
   @Override public void onPageScrollStateChanged(int state) {
     // do nothing
   }
 
   @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
     // do nothing
   }
 
   @Override public void onPageSelected(int position) {
     animateLogo();
   }
 
   private void animateLogo() {
     final Fragment item = adapter.getItem(pager.getCurrentItem());
 
     if (!initialized) {
       return;
     } else if (item instanceof AboutFragment) {
       logoFadeOut();
     } else {
       logoFadeIn();
     }
   }
 
   private void logoFadeOut() {
     switch (logoAnimationState) {
       case FADING_OUT:
       case HIDDEN:
         // logo is already fading out or invisible
         return;
       case FADING_IN:
         // stop the logo from fading in
         logoView.animate().cancel();
         break;
     }
 
     logoAnimationState = LogoAnimationState.FADING_OUT;
     logoView.animate().setDuration(ANIMATION_DURATION).alpha(MIN_LOGO_ALPHA).setListener(new fadeOutListener()).start();
   }
 
   private void logoFadeIn() {
     switch (logoAnimationState) {
       case FADING_IN:
       case VISIBLE:
         // logo is already fading in or visible
         return;
       case FADING_OUT:
         // stop the logo from fading out
         logoView.animate().cancel();
         break;
     }
 
     if (logoView.getVisibility() != View.VISIBLE) {
       logoView.setAlpha(MIN_LOGO_ALPHA);
       logoView.setVisibility(View.VISIBLE);
     }
 
     logoAnimationState = LogoAnimationState.FADING_IN;
     logoView.animate().setDuration(ANIMATION_DURATION).alpha(MAX_LOGO_ALPHA).setListener(new fadeInListener()).start();
   }
 
   abstract private class fadeListener implements Animator.AnimatorListener {
     @Override public void onAnimationCancel(Animator animation) {
       logoAnimationState = LogoAnimationState.CANCELED;
     }
 
     @Override public void onAnimationRepeat(Animator animation) {
     }
 
     @Override public void onAnimationStart(Animator animation) {
     }
   }
 
   private class fadeOutListener extends fadeListener {
     @Override public void onAnimationEnd(Animator animation) {
       if (logoAnimationState != LogoAnimationState.CANCELED) {
         hideLogo();
       }
     }
   }
 
   private class fadeInListener extends fadeListener {
     @Override public void onAnimationEnd(Animator animation) {
       if (logoAnimationState != LogoAnimationState.CANCELED) {
         logoAnimationState = LogoAnimationState.VISIBLE;
       }
     }
   }
}
