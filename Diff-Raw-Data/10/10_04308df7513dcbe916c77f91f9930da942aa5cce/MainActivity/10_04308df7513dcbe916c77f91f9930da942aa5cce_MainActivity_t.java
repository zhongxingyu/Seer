 package edu.mit.mobile.android.livingpostcards;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
 import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
 import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.ActionBar.TabListener;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.mit.mobile.android.livingpostcards.app.CardMapFragment;
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.auth.AuthenticatorActivity;
 import edu.mit.mobile.android.livingpostcards.data.Card;
 import edu.mit.mobile.android.locast.accounts.AbsLocastAuthenticatorActivity.LogoutHandler;
 import edu.mit.mobile.android.locast.data.Authorable;
 
 public class MainActivity extends SherlockFragmentActivity implements OnCreateOptionsMenuListener,
         OnOptionsItemSelectedListener, NoAccountFragment.OnLoggedInListener,
         OnPrepareOptionsMenuListener, TabListener {
     private static final String TAG = MainActivity.class.getSimpleName();
 
     private static final String TAG_SPLASH = "splash";
     private static final String TAG_NEW = "new";
     private static final String TAG_NEARBY = "nearby";
     private static final String TAG_UNPUBLISHED = "unpublished";
     private static final String TAG_MY = "my";
 
     private static final boolean DEBUG = BuildConfig.DEBUG;
 
     private static final String INSTANCE_CURRENT_TAB = "edu.mit.mobile.android.INSTANCE_CURRENT_TAB";
 
     private boolean mIsLoggedIn = false;
 
     private static final int NO_SAVED_TAB = -1;
     private int mSavedCurrentTab = NO_SAVED_TAB;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         setTitle(""); // as we use the logo
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null) {
             mSavedCurrentTab = savedInstanceState.getInt(INSTANCE_CURRENT_TAB, NO_SAVED_TAB);
         }

        final FragmentManager fm = getSupportFragmentManager();

        // start off with any fragments in a detached state
        final Fragment f = fm.findFragmentById(android.R.id.content);
        if (f != null && !f.isDetached()) {
            final FragmentTransaction ft = fm.beginTransaction();
            ft.detach(f);
            ft.commit();
        }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         showSplashOrMain();
 
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         outState.putInt(INSTANCE_CURRENT_TAB, getSupportActionBar().getSelectedNavigationIndex());
 
         super.onSaveInstanceState(outState);
     }
 
     /**
      * Check to see if there's an account and shows either the splash screen or the main screen.
      * It's safe to call this even if the appropriate fragment is already showing - it'll just leave
      * it alone.
      */
     private void showSplashOrMain() {
         mIsLoggedIn = Authenticator.hasRealAccount(this);
 
         if (mIsLoggedIn) {
             showMainScreen();
         } else {
             showSplash();
         }
         invalidateOptionsMenu();
     }
 
     /**
      * Replaces the current fragment with the splash screen. Removes any tabs.
      */
     private void showSplash() {
         final FragmentManager fm = getSupportFragmentManager();
         final FragmentTransaction ft = fm.beginTransaction();
         final Fragment f = fm.findFragmentById(android.R.id.content);
 
         if (f == null || !(f instanceof NoAccountFragment)) {
             final NoAccountFragment f2 = new NoAccountFragment();
 
             ft.replace(android.R.id.content, f2, TAG_SPLASH);
             ft.commit();
         }
 
         final ActionBar actionBar = getSupportActionBar();
         if (ActionBar.NAVIGATION_MODE_STANDARD != actionBar.getNavigationMode()) {
             actionBar.removeAllTabs();
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
         }
     }
 
     /**
      * Replaces the current fragment with the main interface.
      */
     private void showMainScreen() {
 
         final FragmentManager fm = getSupportFragmentManager();
         final Fragment f = fm.findFragmentById(android.R.id.content);
 
         if (f != null && f instanceof NoAccountFragment) {
             final FragmentTransaction ft = fm.beginTransaction();
             ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
             ft.remove(f);
             ft.commit();
         }
         final ActionBar actionBar = getSupportActionBar();
         if (ActionBar.NAVIGATION_MODE_TABS != actionBar.getNavigationMode()) {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
             actionBar.addTab(actionBar.newTab().setText(R.string.main_tab_whats_new)
                     .setTabListener(this).setTag(TAG_NEW));
 
             actionBar.addTab(actionBar.newTab().setText(R.string.main_tab_nearby)
                     .setTabListener(this).setTag(TAG_NEARBY));
 
             actionBar.addTab(actionBar.newTab().setText(R.string.main_tab_my_postcards)
                     .setTabListener(this).setTag(TAG_MY));
 
             actionBar.addTab(actionBar.newTab().setText(R.string.main_tab_unpublished)
                     .setTabListener(this).setTag(TAG_UNPUBLISHED));
         }
 
         if (mSavedCurrentTab != NO_SAVED_TAB) {
             actionBar.setSelectedNavigationItem(mSavedCurrentTab);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
 
             case R.id.log_out:
                 AuthenticatorActivity.createLogoutDialog(this, getText(R.string.app_name),
                         mOnLogoutHandler).show();
         }
         return false;
     }
 
     private final LogoutHandler mOnLogoutHandler = new LogoutHandler(this,
             Authenticator.ACCOUNT_TYPE) {
 
         @Override
         public void onAccountRemoved(boolean success) {
             if (success) {
                 showSplashOrMain();
             }
         }
     };
 
     private void createNewCard() {
 
         final Intent intent = new Intent(Intent.ACTION_INSERT, Card.CONTENT_URI);
         startActivity(intent);
     }
 
     @Override
     public void onLoggedIn() {
         showSplashOrMain();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 
         menu.findItem(R.id.log_out).setVisible(mIsLoggedIn);
 
         return true;
     }
 
     @Override
     public void onTabSelected(Tab tab, FragmentTransaction ft) {
         final FragmentManager fm = getSupportFragmentManager();
 
         Fragment f = fm.findFragmentById(android.R.id.content);
 
         final String tag = (String) tab.getTag();
 
         // first remove anything that's not what we want
         if (f != null && !tag.equals(f.getTag())) {
             ft.detach(f);
             f = null;
         }
 
         // search to see if there's one stashed away
         if (f == null) {
             f = fm.findFragmentByTag(tag);
         }
 
         // and reattach / create anything
         if (f != null) {
             ft.attach(f);
         } else {
             ft.add(android.R.id.content, instantiateFragment(tag), tag);
         }
     }
 
     /**
      * Given a tag, creates a new fragment with the default arguments.
      *
      * @param tag
      * @return
      */
     private Fragment instantiateFragment(String tag) {
         Fragment f;
         if (TAG_MY.equals(tag)) {
             f = CardListFragment.instantiate(Authorable.getAuthoredBy(Card.CONTENT_URI,
                             Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE)).buildUpon()
                     .appendQueryParameter(Card.COL_DRAFT + "!", "1").build());
 
         } else if (TAG_NEW.equals(tag)) {
             f = CardListFragment.instantiate(Card.CONTENT_NOT_DRAFT);
 
         } else if (TAG_UNPUBLISHED.equals(tag)) {
             f = CardListFragment.instantiate(Card.CONTENT_URI.buildUpon()
                     .appendQueryParameter(Card.COL_DRAFT, "1").build());
 
         } else if (TAG_NEARBY.equals(tag)) {
             f = CardMapFragment.instantiate(Card.CONTENT_NOT_DRAFT, true);
 
         } else {
             throw new IllegalArgumentException("cannot instantiate fragment for tag " + tag);
         }
         return f;
     }
 
     @Override
     public void onTabUnselected(Tab tab, FragmentTransaction ft) {
         final FragmentManager fm = getSupportFragmentManager();
         final String tag = (String) tab.getTag();
         final Fragment f = fm.findFragmentByTag(tag);
         if (f != null) {
             ft.detach(f);
         }
     }
 
     @Override
     public void onTabReselected(Tab tab, FragmentTransaction ft) {
 
     }
 }
