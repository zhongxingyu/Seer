 package org.quuux.eyecandy;
 
 import android.annotation.TargetApi;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.*;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 import org.quuux.eyecandy.utils.ViewServer;
 import org.quuux.orm.Query;
 
 public class MainActivity
         extends ActionBarActivity
         implements ActionBar.OnNavigationListener,
                    GalleryFragment.Listener,
                    SourcesFragment.Listener {
 
 
     private static final String TAG = "MainActivity";
 
     private static final String FRAG_RANDOM = "random";
     private static final String FRAG_GALLERY = "gallery-%s";
     private static final String FRAG_VIEWER = "viewer-%s";
     private static final String FRAG_SOURCES = "subreddits";
 
     public static final int MODE_SLIDE_SHOW = 0;
     public static final int MODE_BURNS = 1;
     public static final int MODE_SOURCES = 2;
     public static final int MODE_GALLERY = 3;
 
     final private Handler mHandler = new Handler();
 
     private GestureDetector mGestureDetector;
     private boolean mSquealch;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
         getWindow().addFlags(
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
         );
 
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         actionBar.setDisplayShowTitleEnabled(false);
 
         final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.modes, android.R.layout.simple_spinner_dropdown_item);
         actionBar.setListNavigationCallbacks(spinnerAdapter, this);
 
         actionBar.setSelectedNavigationItem(EyeCandyPreferences.getLastNavMode(this));
 
         mGestureDetector = new GestureDetector(this, mGestureListener);
 
         setupSystemUi();
 
         mHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(MainActivity.this, R.string.starting_scrape, Toast.LENGTH_SHORT).show();
 
                 final Intent intent = new Intent(MainActivity.this, ScrapeService.class);
                 startService(intent);
             }
        }, 500);
 
         ViewServer.get(this).addWindow(this);
 
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         ViewServer.get(this).removeWindow(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         ViewServer.get(this).setFocusedWindow(this);
 
         final IntentFilter filter = new IntentFilter();
         filter.addAction(ScrapeService.ACTION_SCRAPE_COMPLETE);
         registerReceiver(mBroadcastReceiver, filter);
 
         hideSystemUi();
         summon();
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(mBroadcastReceiver);
     }
 
     @Override
     public void onBackPressed() {
 
         final Fragment frag = getCurrentFragment();
         if (frag != null &&
                 frag instanceof OnBackPressedListener &&
                 ((OnBackPressedListener) frag).onBackPressed()) {
           return;
         }
 
         super.onBackPressed();
     }
 
     @Override
     public boolean dispatchTouchEvent(final MotionEvent ev) {
         mGestureDetector.onTouchEvent(ev);
         return super.dispatchTouchEvent(ev);
     }
 
     @Override
     public boolean onNavigationItemSelected(final int position, final long id) {
 
         if (mSquealch) {
             Log.d(TAG, "squealching nav item selection");
             mSquealch = false;
             return true;
         }
 
         switch (position) {
             case MODE_SLIDE_SHOW:
                onShowImage();
                 break;
 
             case MODE_BURNS:
                onShowRandom();
                 break;
 
             case MODE_SOURCES:
                onShowSources();
                 break;
 
             case MODE_GALLERY:
                onShowGallery();
                 break;
         }
 
         EyeCandyPreferences.setLastNavMode(this, position);
 
         return true;
     }
 
     @TargetApi(11)
     private void setupSystemUi() {
         final View v = findViewById(android.R.id.content);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             v.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                 @Override
                 public void onSystemUiVisibilityChange(final int visibility) {
                     final boolean isVisible = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                     if (isVisible);
                         summon();
                 }
             });
         }
     }
 
     @TargetApi(11)
     private void hideSystemUi() {
         final View v = findViewById(android.R.id.content);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             v.setSystemUiVisibility(
                     View.SYSTEM_UI_FLAG_LOW_PROFILE
             );
         }
 
     }
 
     private Fragment getCurrentFragment() {
         return getSupportFragmentManager().findFragmentById(android.R.id.content);
     }
 
     private void swapFrag(final Fragment fragment, final String tag, final boolean addToBackStack) {
         final FragmentManager frags = getSupportFragmentManager();
         final FragmentTransaction ft = frags.beginTransaction();
         ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
         //ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
         ft.replace(android.R.id.content, fragment, tag);
         if (addToBackStack)
             ft.addToBackStack(null);
         ft.commit();
     }
 
     private void swapFrag(final Fragment fragment, final String tag) {
         swapFrag(fragment, tag, false);
     }
 
     private Fragment getFrag(final String tag, Object... args) {
         final FragmentManager fm = getSupportFragmentManager();
         return fm.findFragmentByTag(String.format(tag, args));
     }
 
     private void onShowRandom() {
         Fragment frag = getFrag(FRAG_RANDOM);
         if (frag == null)
             frag = RandomFragment.newInstance();
         swapFrag(frag, FRAG_RANDOM, false);
     }
 
 
     public void showImage(final Query query, final int position, final boolean addToBackStack) {
 
 
 
         Fragment frag = getFrag(FRAG_VIEWER, query.toSql().hashCode());
         if (frag == null)
             frag = ViewerFragment.newInstance(query, position);
         swapFrag(frag, FRAG_VIEWER, addToBackStack);
     }
 
     public void showImage(final Query query, final int position) {
         showImage(query, position, true);
     }
 
     public void showImage(final Query query) {
         showImage(query, 0);
     }
 
     private void onShowImage() {
         final Query q = EyeCandyDatabase.getSession(this).query(Image.class).orderBy("timesShown, RANDOM()");
         showImage(q, 0, false);
     }
 
     public void showGallery(final Query query, final boolean addToBackStack) {
         Fragment frag = getFrag(FRAG_GALLERY, query != null ? query.toSql().hashCode() : 0);
         if (frag == null)
             frag = GalleryFragment.newInstance(query);
         swapFrag(frag, FRAG_GALLERY, addToBackStack);
     }
 
     public void showGallery(final Query query) {
         showGallery(query, true);
     }
 
     private void onShowGallery() {
         showGallery(null, false);
     }
 
     private void onShowSources() {
         Fragment frag = getFrag(FRAG_SOURCES);
         if (frag == null)
             frag = SourcesFragment.newInstance();
         swapFrag(frag, FRAG_SOURCES, false);
     }
 
     private void dismiss() {
         //Log.d(TAG, "dismiss ui");
         getSupportActionBar().hide();
         hideSystemUi();
     }
 
     private void dismissDelayed(long t) {
         mHandler.removeCallbacks(mDismissCallback);
         mHandler.postDelayed(mDismissCallback, t);
     }
 
     private void summon() {
         Log.d(TAG, "summon ui");
 
         //getSupportActionBar().show();
         //dismissDelayed(2500);
     }
 
     public void setSelectedNavigationItemSilent(final int pos) {
         final ActionBar ab = getSupportActionBar();
         if (ab == null)
             return;
        mSquealch = true;
         ab.setSelectedNavigationItem(pos);
     }
 
     final Runnable mDismissCallback = new Runnable() {
         @Override
         public void run() {
             dismiss();
         }
     };
 
 
     final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
 
         @Override
         public void onReceive(final Context context, final Intent intent) {
 
             final String action = intent.getAction();
 
             if (ScrapeService.ACTION_SCRAPE_COMPLETE.equals(action)) {
                 final int taskCount = intent.getIntExtra(ScrapeService.EXTRA_TASK_COUNT, -1);
 
                 Log.d(TAG, "scrape complete, task count = %s", taskCount);
 
                 if (taskCount == 0)
                     Toast.makeText(MainActivity.this, R.string.scrape_complete, Toast.LENGTH_SHORT).show();
 
             }
 
         }
     };
 
     GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {
         @Override
         public boolean onDown(final MotionEvent e) {
             return false;
         }
 
         @Override
         public void onShowPress(final MotionEvent e) {
 
         }
 
         @Override
         public boolean onSingleTapUp(final MotionEvent e) {
             supportInvalidateOptionsMenu();
             summon();
             return false;
         }
 
         @Override
         public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
             return false;
         }
 
         @Override
         public void onLongPress(final MotionEvent e) {
 
         }
 
         @Override
         public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
             return false;
         }
     };
 
     }
