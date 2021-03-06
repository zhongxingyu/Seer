 /* *************************************************************************
  *  Copyright 2012 The detlef developers                                   *
  *                                                                         *
  *  This program is free software: you can redistribute it and/or modify   *
  *  it under the terms of the GNU General Public License as published by   *
  *  the Free Software Foundation, either version 2 of the License, or      *
  *  (at your option) any later version.                                    *
  *                                                                         *
  *  This program is distributed in the hope that it will be useful,        *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
  *  GNU General Public License for more details.                           *
  *                                                                         *
  *  You should have received a copy of the GNU General Public License      *
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
  ************************************************************************* */
 
 package at.ac.tuwien.detlef.activities;
 
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.app.SearchableInfo;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SearchView;
 import android.widget.Toast;
 import at.ac.tuwien.detlef.DependencyAssistant;
 import at.ac.tuwien.detlef.R;
 import at.ac.tuwien.detlef.activities.callbacks.EpisodeSearchQueryTextListener;
 import at.ac.tuwien.detlef.callbacks.CallbackContainer;
 import at.ac.tuwien.detlef.db.PlaylistDAO;
 import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
 import at.ac.tuwien.detlef.db.PodcastDAO;
 import at.ac.tuwien.detlef.db.PodcastDAOImpl;
 import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
 import at.ac.tuwien.detlef.domain.Episode;
 import at.ac.tuwien.detlef.domain.EpisodeSortChoice;
 import at.ac.tuwien.detlef.domain.Podcast;
 import at.ac.tuwien.detlef.filter.NewFilter;
 import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
 import at.ac.tuwien.detlef.fragments.EpisodeListSortDialogFragment;
 import at.ac.tuwien.detlef.fragments.PlayerFragment;
 import at.ac.tuwien.detlef.fragments.PodListFragment;
 import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
 import at.ac.tuwien.detlef.gpodder.FeedSyncResultHandler;
 import at.ac.tuwien.detlef.gpodder.GPodderException;
 import at.ac.tuwien.detlef.gpodder.GPodderSync;
 import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
 import at.ac.tuwien.detlef.gpodder.PodcastSyncResultHandler;
 import at.ac.tuwien.detlef.gpodder.PullFeedAsyncTask;
 import at.ac.tuwien.detlef.gpodder.PullSubscriptionsAsyncTask;
 import at.ac.tuwien.detlef.gpodder.PushSubscriptionChangesResultHandler;
 import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;
 import at.ac.tuwien.detlef.gpodder.SyncEpisodeActionsAsyncTask;
 import at.ac.tuwien.detlef.mediaplayer.MediaPlayerNotification;
 import at.ac.tuwien.detlef.settings.GpodderSettings;
 
 public class MainActivity extends FragmentActivity
         implements ActionBar.TabListener, PodListFragment.OnPodcastSelectedListener,
         EpisodeListFragment.OnEpisodeSelectedListener,
         EpisodeListSortDialogFragment.NoticeDialogListener {
 
     private static final String TAG = MainActivity.class.getCanonicalName();
     private static final int PODCAST_ADD_REQUEST_CODE = 997;
 
     public enum RefreshDoneNotification {
         TOAST, DIALOG
     }
 
     private Menu menu;
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections. We use a
      * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
      * will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a
      * {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     private SectionsPagerAdapter mSectionsPagerAdapter;
 
     private ActionBar actionBar;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     private ViewPager mViewPager;
 
     private PlaylistDAO playlistDAO;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         startSettingsActivityIfNoDeviceIdSet();
 
         setContentView(R.layout.main_activity_layout);
 
         /* old Activity is recreated */
         if (savedInstanceState != null) {
             curPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_CUR_POD_SYNC, 0));
             numPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_NUM_POD_SYNC, -1));
         }
 
         MediaPlayerNotification.create(this, false, null);
 
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the action bar.
         actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab.
         // We can also use ActionBar.Tab#select() to do this if we have a
         // reference to the Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by
             // the adapter.
             // Also specify this Activity object, which implements the
             // TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(actionBar.newTab()
                     .setText(mSectionsPagerAdapter.getPageTitle(i))
                     .setTabListener(this));
         }
 
         /* Ready the progress dialog */
         progressDialog = new ProgressDialog(this);
         prepareProgressDialog();
         if (numPodSync.get() != -1) {
             progressDialog.show();
         }
 
         playlistDAO = PlaylistDAOImpl.i();
 
     }
 
     private void startSettingsActivityIfNoDeviceIdSet() {
 
         if (DependencyAssistant
                 .getDependencyAssistant()
                 .getGpodderSettings(this)
                 .getDeviceId() != null) {
             return;
         }
 
         final AlertDialog.Builder b = new AlertDialog.Builder(this);
         b.setTitle(R.string.detlef_says_hello);
         b.setMessage(R.string.detlef_is_not_set_up_yet);
 
         b.setPositiveButton(
                 android.R.string.yes, new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                         startActivityForResult(
                                 new Intent(
                                         getApplicationContext(),
                                         SettingsActivity.class
                                 )
                                         .putExtra(
                                                 PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                                                 SettingsGpodderNet.class.getName()
                                         )
                                         .putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
                                         .putExtra(SettingsGpodderNet.EXTRA_SETUPMODE, true),
                                 0
                         );
 
                     }
                 });
         b.setNegativeButton(
                 android.R.string.no,
                 new OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         Toast.makeText(
                                 getApplicationContext(),
                                 getString(R.string.you_can_setup_your_account_later),
                                 Toast.LENGTH_LONG
                                 ).show();
 
                     }
                 }
                 );
 
         b.show();
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         /* Register the Podcast- & FeedHandler. */
         cbCont.registerReceiver(this);
     }
 
     @Override
     public void onBackPressed() {
         switch (mViewPager.getCurrentItem()) {
             case SectionsPagerAdapter.POSITION_PODCASTS:
                 super.onBackPressed();
                 break;
             case SectionsPagerAdapter.POSITION_EPISODES:
                 actionBar.selectTab(actionBar.getTabAt(SectionsPagerAdapter.POSITION_PODCASTS));
                 break;
             case SectionsPagerAdapter.POSITION_PLAYER:
                 actionBar.selectTab(actionBar.getTabAt(SectionsPagerAdapter.POSITION_EPISODES));
                 break;
             default:
                 super.onBackPressed();
         }
     }
 
     @Override
     public void onPause() {
         /* Unregister the Podcast- & FeedHandler. */
         cbCont.unregisterReceiver();
 
         if (isFinishing()) {
             cbCont.clear();
         }
 
         super.onPause();
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
 
         savedInstanceState.putInt(KEY_NUM_POD_SYNC, numPodSync.get());
         savedInstanceState.putInt(KEY_CUR_POD_SYNC, curPodSync.get());
     }
 
     @Override
     public void onDestroy() {
         progressDialog.dismiss();
 
         super.onDestroy();
     }
 
     private void prepareProgressDialog() {
         progressDialog.setTitle(R.string.refreshing);
         progressDialog.setCancelable(false);
         if (numPodSync.get() > 0) {
             if (numPodSync.get() == curPodSync.get()) {
                 progressDialog.setMessage(getString(R.string.syncing_episode_actions));
             } else {
                 progressDialog.setMessage(String.format(
                         getString(R.string.refreshing_feed_x_of_y),
                         curPodSync.get() + 1, numPodSync.get()));
             }
         } else {
             progressDialog.setMessage(getString(R.string.refreshing_feed_list));
         }
     }
 
    private static final String KEY_PODCAST_HANDLER = "KEY_PODCAST_HANDLER";
    private static final String KEY_FEED_HANDLER = "KEY_FEED_HANDLER";
    private static final String KEY_SUBSCRIPTION_UPDATE_HANDLER = "KEY_SUBSCRIPTION_UPDATE_HANDLER";
    private static final String KEY_EPISODE_ACTION_HANDLER = "KEY_EPISODE_ACTION_HANDLER";

     /**
      * All callbacks this Activity receives are stored here. This allows us to
      * manage the Activity Lifecycle more easily.
      */
     private static final CallbackContainer<MainActivity> cbCont =
             new CallbackContainer<MainActivity>();
    static {
        cbCont.put(KEY_PODCAST_HANDLER, new PodcastHandler());
        cbCont.put(KEY_FEED_HANDLER, new FeedHandler());
        cbCont.put(KEY_SUBSCRIPTION_UPDATE_HANDLER, new SubscriptionUpdateHandler());
        cbCont.put(KEY_EPISODE_ACTION_HANDLER, new EpisodeActionHandler());
    }
 
     /**
      * The Tasks for the refresh are run on a single thread.
      */
     private static final ExecutorService refreshBg = Executors.newSingleThreadExecutor();
 
     /**
      * The Toast with the Output of the refresh operation is shown this long.
      */
     private static final int REFRESH_MSG_DURATION_MS = 5000;
 
     /**
      * The progress dialog displayed during a refresh.
      */
     private ProgressDialog progressDialog;
 
     private static final String KEY_NUM_POD_SYNC = "KEY_NUM_POD_SYNC";
 
     private static final String KEY_CUR_POD_SYNC = "KEY_CUR_POD_SYNC";
 
     private static final String KEY_SUBSCRIPTION_CHANGES = "KEY_SUBSCRIPTION_CHANGES";
 
     /**
      * if {@link #onActivityResult(int, int, Intent)} is called with an
      * {@link Intent} that has a boolean extra with this key, then the podcast
      * list will be refreshed. This is used during set up mode when the list
      * should be refreshed automatically after the user entered his user
      * credentials.
      */
     public static final String EXTRA_REFRESH_FEED_LIST = "REFRESH_FEED_LIST";
 
     /**
      * if {@link #onActivityResult(int, int, Intent)} is called with an
      * {@link Intent} that has a boolean extra with this key, then the podcast
      * list will be refreshed. This is used after adding new podcasts.
      */
     public static final String PODCAST_ADD_REFRESH_FEED_LIST = "PODCAST_ADD_REFRESH_FEED_LIST";
 
     /** Number of feeds to sync, -1 if no refresh is in progress. */
     private AtomicInteger numPodSync = new AtomicInteger(-1);
 
     /** Number of feeds already synchronized. */
     private AtomicInteger curPodSync = new AtomicInteger(0);
 
     /**
      * The Handler for receiving PullSubscriptionsAsyncTask's results.
      */
     private static final class PodcastHandler extends PodcastSyncResultHandler<MainActivity> {
 
         /**
          * Once the Podcast list is synchronized, update all feeds.
          */
         @Override
         public void handle() {
             PodcastDAO pDao = PodcastDAOImpl.i();
 
             final boolean showDialog = getBundle().getBoolean(EXTRA_REFRESH_FEED_LIST, false);
 
             synchronized (getRcv().numPodSync) {
 
                 for (Podcast p : pDao.getNonDeletedPodcasts()) {
 
                     FeedSyncResultHandler<? extends Activity> handler =
                             (FeedSyncResultHandler<? extends Activity>) cbCont
                                     .get(KEY_FEED_HANDLER);
                     handler.setBundle(getBundle());
                     refreshBg.execute(new PullFeedAsyncTask(handler, p));
                     getRcv().numPodSync.incrementAndGet();
                 }
 
                 if (getRcv().numPodSync.get() == 0) {
 
                     if (showDialog) {
                         getRcv().onRefreshDone(getRcv().getString(R.string.setup_finished),
                                 RefreshDoneNotification.DIALOG);
                     } else {
                         getRcv().onRefreshDone(getRcv().getString(R.string.refresh_successful));
                     }
                 }
 
                 getRcv().prepareProgressDialog();
             }
         }
 
         @Override
         public void handleFailure(GPodderException e) {
             getRcv().onRefreshDone(getRcv().getString(R.string.operation_failed) + ": "
                     + e.getMessage());
         }
     };
 
     private static class SubscriptionUpdateHandler
             extends ReliableResultHandler<MainActivity>
             implements PushSubscriptionChangesResultHandler<MainActivity> {
 
         @Override
         public void handleFailure(int errCode, final String errStr) {
             getRcv().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     getRcv().onRefreshDone(getRcv().getString(R.string.operation_failed) + ": "
                             + errStr);
                 }
             });
         }
 
         @Override
         public void handleSuccess(long timestamp, Map<String, String> updateUrls) {
             PodcastSyncResultHandler<? extends Activity> handler =
                     (PodcastSyncResultHandler<? extends Activity>) cbCont.get(KEY_PODCAST_HANDLER);
             handler.setBundle(getBundle());
 
             Log.d(TAG, "bundle: " + getBundle());
             Log.d(TAG, "handler: " + handler);
 
             EnhancedSubscriptionChanges changes = getBundle().getParcelable(
                     KEY_SUBSCRIPTION_CHANGES);
 
             DependencyAssistant.getDependencyAssistant().getPodcastDBAssistant()
                     .applySubscriptionChanges(getRcv(), changes);
 
             DependencyAssistant.getDependencyAssistant().getGpodderSettings(getRcv())
                     .setLastUpdate(timestamp);
 
             refreshBg.execute(new PullSubscriptionsAsyncTask(handler));
             getRcv().startService(new Intent().setClass(getRcv(),
                     PullSubscriptionsAsyncTask.class));
         }
     }
 
     private static class EpisodeActionHandler
     extends ReliableResultHandler<MainActivity>
     implements NoDataResultHandler<MainActivity> {
 
         @Override
         public void handleFailure(int errCode, final String errStr) {
             getRcv().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     getRcv().onRefreshDone(getRcv().getString(R.string.operation_failed) + ": "
                             + errStr);
                 }
             });
         }
 
         @Override
         public void handleSuccess() {
             getRcv().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     if (getBundle().getBoolean(EXTRA_REFRESH_FEED_LIST, false)) {
                         getRcv().onRefreshDone(
                                 getRcv().getString(R.string.setup_finished),
                                 RefreshDoneNotification.DIALOG
                                 );
                     } else {
                         getRcv().onRefreshDone(getRcv().getString(R.string.refresh_successful));
                     }
                 }
             });
         }
     }
 
     /**
      * The Handler for receiving PullFeedAsyncTask's results.
      */
     private static final class FeedHandler extends FeedSyncResultHandler<MainActivity> {
 
         @Override
         public void handle() {
             synchronized (getRcv().numPodSync) {
                 checkDone();
             }
         }
 
         @Override
         public void handleFailure(GPodderException e) {
             Toast.makeText(getRcv(), e.getMessage(), REFRESH_MSG_DURATION_MS).show();
 
             checkDone();
         }
 
         /**
          * Check whether we have refreshed all feeds and if yes call
          * onRefreshDone.
          */
         private void checkDone() {
             synchronized (getRcv().numPodSync) {
                 getRcv().curPodSync.incrementAndGet();
 
                 if (getRcv().curPodSync.get() == getRcv().numPodSync.get()) {
 
                     final boolean showDialog = getBundle().getBoolean(EXTRA_REFRESH_FEED_LIST,
                             false);
 
                     Log.d(TAG, "r bundle: " + getBundle());
                     Log.d(TAG, "r bundle extra: " + showDialog);
                     Log.d(TAG, "r handler: " + this);
 
                     EpisodeActionHandler handler = (EpisodeActionHandler) cbCont.get(
                             MainActivity.KEY_EPISODE_ACTION_HANDLER);
                     handler.setBundle(getBundle());
                     refreshBg.execute(new SyncEpisodeActionsAsyncTask(handler));
                 }
 
                 getRcv().prepareProgressDialog();
             }
         }
 
     };
 
     /**
      * Calls {@link #onRefreshPressed(Bundle)} with an empty Bundle.
      */
     private void onRefreshPressed() {
         onRefreshPressed(new Bundle());
     }
 
     /**
      * Called when the refresh button is pressed. Displays a progress dialog and
      * starts the {@link PullSubscriptionsAsyncTask}.
      * 
      * @param pBundle The {@link Bundle} that is passed to the
      *            {@link PodcastSyncResultHandler}.
      */
     private void onRefreshPressed(Bundle pBundle) {
 
         GpodderSettings settings = DependencyAssistant.getDependencyAssistant()
                 .getGpodderSettings(this);
 
         if (settings.getDeviceId() == null) {
             Toast.makeText(this, "Please set up your account first!", Toast.LENGTH_SHORT);
             Log.w(TAG, "Could not refresh due to missing account information");
             return;
         }
 
         synchronized (numPodSync) {
             if (numPodSync.get() != -1) {
                 return;
             }
 
             numPodSync.incrementAndGet();
             curPodSync.set(0);
         }
 
         PodcastDAO pDao = PodcastDAOImpl.i();
         EnhancedSubscriptionChanges changes = new EnhancedSubscriptionChanges(
                 pDao.getLocallyAddedPodcasts(), pDao.getLocallyDeletedPodcasts(),
                 settings.getLastUpdate());
         pBundle.putParcelable(KEY_SUBSCRIPTION_CHANGES, changes);
 
         SubscriptionUpdateHandler handler = (SubscriptionUpdateHandler)
                 cbCont.get(KEY_SUBSCRIPTION_UPDATE_HANDLER);
         handler.setBundle(pBundle);
 
         Log.d(TAG, "bundle: " + pBundle);
         Log.d(TAG, "handler: " + handler);
 
         GPodderSync gps = DependencyAssistant.getDependencyAssistant().getGPodderSync();
         gps.setDeviceName(settings.getDeviceId().toString());
         gps.setUsername(settings.getUsername());
         gps.setPassword(settings.getPassword());
         gps.addUpdateSubscriptionsJob(handler, changes);
 
         prepareProgressDialog();
         progressDialog.show();
     }
 
     /**
      * Called when refresh is done, dismisses the progress dialog and displays
      * msg in a Toast.
      * 
      * @param msg The message displayed in a Toast.
      */
     private void onRefreshDone(String msg) {
         onRefreshDone(msg, RefreshDoneNotification.TOAST);
     }
 
     private void onRefreshDone(String msg, RefreshDoneNotification notificationType) {
         numPodSync.set(-1);
         progressDialog.dismiss();
 
         Log.d(TAG, "notificationType: " + notificationType);
 
         switch (notificationType) {
             case TOAST:
             default:
                 Toast.makeText(this, msg, REFRESH_MSG_DURATION_MS).show();
                 break;
             case DIALOG:
                 final AlertDialog.Builder b = new AlertDialog.Builder(this);
                 b.setTitle("Refresh done.");
                 b.setMessage(msg);
                 b.setPositiveButton(android.R.string.ok, null);
                 b.show();
                 break;
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu pMenu) {
 
         Log.d(TAG, "onCreateOptionsMenu(" + pMenu + ")");
         Log.d(TAG, "currentItem: " + mViewPager.getCurrentItem());
 
         menu = pMenu;
 
         // on application startup, mViewPager is always 0.
         // however, if you are in a tab and the activity is re-created,
         // it can be <> 0. thus, you can restore the menu item's status
         // here.
         
         switch (mViewPager.getCurrentItem()) {
             case SectionsPagerAdapter.POSITION_PODCASTS:
                 getMenuInflater().inflate(R.menu.podcast_menu, menu);
                 break;
             case SectionsPagerAdapter.POSITION_EPISODES:
                 getMenuInflater().inflate(R.menu.episode_menu, menu);
                 setSearchManager();
                 updateEpisodeFilterUiStatus();
                 break;
             case SectionsPagerAdapter.POSITION_PLAYER:
                 getMenuInflater().inflate(R.menu.player_menu, menu);
                 break;
             default:
                 return false;
         }
         return true;
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         /* Intentionally left blank. */
     }
 
     @Override
     public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in
         // the ViewPager.
         if (menu != null) {
             menu.clear();
             switch (tab.getPosition()) {
                 case SectionsPagerAdapter.POSITION_PODCASTS:
                     getMenuInflater().inflate(R.menu.podcast_menu, menu);
                     break;
                 case SectionsPagerAdapter.POSITION_EPISODES:
                     getMenuInflater().inflate(R.menu.episode_menu, menu);
                     setSearchManager();
                     updateEpisodeFilterUiStatus();
                     break;
                 case SectionsPagerAdapter.POSITION_PLAYER:
                     getMenuInflater().inflate(R.menu.player_menu, menu);
                     break;
                 default:
                     Log.wtf(TAG, "Non-existent tab selected! Please fix");
             }
         }
         mViewPager.setCurrentItem(tab.getPosition());
     }
     
     /**
      * Updates the status of the episode filters, i.e. make all necessary UI 
      * changes to visualize the status of the currently set filters.
      */
     private void updateEpisodeFilterUiStatus() {
         
         if (menu == null || menu.findItem(R.id.menu_show_only_new_episodes) == null) {
             return;
         }
         MenuItem item = menu.findItem(R.id.menu_show_only_new_episodes);
         
         item.setChecked(getEpisodeListFragment().getFilter().contains(new NewFilter()));
     }
 
     /**
      * Get the {@link SearchView} and set the {@link SearchableInfo} 
      * configuration for the
      * {@link EpisodeListFragment#getFilter() episode keyword filter}.
      */
     private void setSearchManager() {
         String tmptag = "searchmanager";
         Log.d(tmptag, "setSearchManager()");
         
         if (menu.findItem(R.id.menu_search) == null) {
             Log.d(tmptag, "is null");
             return;
         }
 
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         
         
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(true);
         searchView.setOnQueryTextListener(new EpisodeSearchQueryTextListener(
                 getEpisodeListFragment()));
     }
 
     @Override
     public void onTabReselected(Tab tab, FragmentTransaction ft) {
         /* Intentionally left blank. */
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the primary sections of the app.
      */
     protected class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public static final int POSITION_PODCASTS = 0;
         public static final int POSITION_EPISODES = 1;
         public static final int POSITION_PLAYER = 2;
         public static final int TABCOUNT = POSITION_PLAYER + 1;
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
 
             /*
              * References to Fragments used within this adapter may not be kept
              * anywhere, since they are not guaranteed to stay valid. Therefore,
              * we need to create fragment instances within this method, and rely
              * on other means (for example the fragment manager) to retrieve
              * references.
              */
 
             switch (i) {
                 case POSITION_PODCASTS:
                     return new PodListFragment();
                 case POSITION_EPISODES:
                     return new EpisodeListFragment();
                 case POSITION_PLAYER:
                     return new PlayerFragment();
                 default:
                     throw new IndexOutOfBoundsException();
             }
         }
 
         @Override
         public int getCount() {
             return TABCOUNT;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case POSITION_PODCASTS:
                     return getString(R.string.podcasts).toUpperCase(Locale.getDefault());
                 case POSITION_EPISODES:
                     return getString(R.string.episodes).toUpperCase(Locale.getDefault());
                 case POSITION_PLAYER:
                     return getString(R.string.player).toUpperCase(Locale.getDefault());
                 default:
                     return null;
             }
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent;
         
         switch (item.getItemId()) {
             case R.id.licenses:
                 intent = new Intent(this, LicensesActivity.class);
                 startActivity(intent);
                 break;
             case R.id.settings:
                 intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 break;
             case R.id.playlist:
                 intent = new Intent(this, PlaylistActivity.class);
                 startActivity(intent);
                 break;
             case R.id.refresh:
                 onRefreshPressed();
                 break;
             case R.id.add_new_podcast:
                 intent = new Intent(this, AddPodcastActivity.class);
                 startActivityForResult(intent, PODCAST_ADD_REQUEST_CODE);
                 break;
             case R.id.menu_show_only_new_episodes:
                 item.setChecked(!item.isChecked());
                 getEpisodeListFragment().setReadFilter(item.isChecked());
                 //getEpisodeListFragment().set
                 break;
             case R.id.sort:
                 android.support.v4.app.DialogFragment dialog = new EpisodeListSortDialogFragment();
                 dialog.show(getSupportFragmentManager(), "EpisodeListSortDialogFragment");
                 break;
             default:
                 break;
         }
         return true;
     }
 
     /**
      * The user has selected a podcast in the podcasts list. Filters the episode
      * view and then switches to it. If podcast is null, all episodes are shown.
      */
     @Override
     public void onPodcastSelected(Podcast podcast) {
         getEpisodeListFragment().setPodcast(podcast);
         getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_EPISODES);
     }
 
     /**
      * Retrieves the currently active episode list fragment.
      */
     private EpisodeListFragment getEpisodeListFragment() {
         FragmentManager manager = getSupportFragmentManager();
         String tag = String.format("android:switcher:%d:%d", R.id.pager,
                 SectionsPagerAdapter.POSITION_EPISODES);
         return (EpisodeListFragment) manager.findFragmentByTag(tag);
     }
 
     private PlayerFragment getPlayerFragment() {
         FragmentManager manager = getSupportFragmentManager();
         String tag = String.format("android:switcher:%d:%d", R.id.pager,
                 SectionsPagerAdapter.POSITION_PLAYER);
         return (PlayerFragment) manager.findFragmentByTag(tag);
     }
 
     @Override
     public void onEpisodeSelected(Episode episode) {
         getPlayerFragment().setManualEpisode(episode);
         getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_PLAYER);
     }
 
     public void onDownloadTrashClick(View v) {
         getEpisodeListFragment().onDownloadTrashClick(v);
     }
 
     public void onAddToPlaylistClick(View v) {
         playlistDAO.addEpisodeToEndOfPlaylist((Episode) v.getTag());
     }
 
     public void onMarkReadUnreadClick(View v) {
         getEpisodeListFragment().onMarkReadUnreadClick(v);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
 
         Log.d(TAG, String.format("onActivityResult(%d, %d, %s)", requestCode, resultCode, data));
 
         if (data == null) {
             return;
         }
 
         if (data.getBooleanExtra(EXTRA_REFRESH_FEED_LIST, false)
                 || data.getBooleanExtra(PODCAST_ADD_REFRESH_FEED_LIST, false)) {
 
             if (resultCode == Activity.RESULT_OK) {
                 Bundle bundle = new Bundle();
                 if (data.getBooleanExtra(PODCAST_ADD_REFRESH_FEED_LIST, false)) {
                     bundle.putBoolean(PODCAST_ADD_REFRESH_FEED_LIST, true);
                 } else {
                     bundle.putBoolean(EXTRA_REFRESH_FEED_LIST, true);
                 }
                 onRefreshPressed(bundle);
             } else {
                 if (data.getBooleanExtra(EXTRA_REFRESH_FEED_LIST, false)) {
                     Toast.makeText(
                             this, getString(R.string.you_can_refresh_your_podcasts_later),
                             Toast.LENGTH_LONG
                             ).show();
                 }
             }
         }
 
     }
 
     @Override
     public void onEpisodeSortDialogPositiveClick(DialogFragment dialog, boolean ascending,
             EpisodeSortChoice choice) {
         Log.i(TAG, String.format("sortby: %s orderby: %s ", choice.toString(), ascending));
         this.getEpisodeListFragment().sortEpisodeList(choice, ascending);
     }
 
     @Override
     public void onEpisodeSortDialogNegativeClick(DialogFragment dialog) {
         // Nothing todo yet
     }
     
     @Override
     public boolean onSearchRequested() {
 
         if (mViewPager == null) {
             return false;
         }
         
         if (mViewPager.getCurrentItem() != SectionsPagerAdapter.POSITION_EPISODES) {
             return false;
         }
         
         menu.findItem(R.id.menu_search).expandActionView();
         
         return true;
     }
 
 }
