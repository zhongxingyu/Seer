 package de.hdodenhof.holoreader.activities;
 
 import java.lang.ref.WeakReference;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.accounts.AccountManager;
 import android.annotation.SuppressLint;
 import android.app.NotificationManager;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.HeaderViewListAdapter;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.gms.auth.GoogleAuthUtil;
 import com.google.android.gms.common.AccountPicker;
 
 import de.hdodenhof.holoreader.Config;
 import de.hdodenhof.holoreader.R;
 import de.hdodenhof.holoreader.fragments.ArticleListFragment;
 import de.hdodenhof.holoreader.fragments.DynamicDialogFragment;
 import de.hdodenhof.holoreader.fragments.FeedListFragment;
 import de.hdodenhof.holoreader.gcm.GCMIntentService;
 import de.hdodenhof.holoreader.gcm.GCMServerUtilities;
 import de.hdodenhof.holoreader.listadapters.RSSAdapter;
 import de.hdodenhof.holoreader.listadapters.RSSArticleAdapter;
 import de.hdodenhof.holoreader.listadapters.RSSFeedAdapter;
 import de.hdodenhof.holoreader.misc.ChangelogDialog;
 import de.hdodenhof.holoreader.misc.FragmentCallback;
 import de.hdodenhof.holoreader.misc.Helpers;
 import de.hdodenhof.holoreader.misc.MarkReadRunnable;
 import de.hdodenhof.holoreader.provider.RSSContentProvider;
 import de.hdodenhof.holoreader.provider.SQLiteHelper.ArticleDAO;
 import de.hdodenhof.holoreader.provider.SQLiteHelper.FeedDAO;
 import de.hdodenhof.holoreader.services.RefreshFeedService;
 import de.hdodenhof.holoreader.tasks.AddFeedTask;
 
 /**
  * 
  * @author Henning Dodenhof
  * 
  */
 public class HomeActivity extends HoloReaderActivity implements FragmentCallback, OnItemClickListener {
 
     @SuppressWarnings("unused")
     private static final String TAG = HomeActivity.class.getSimpleName();
 
     private static final int ACCOUNT_REQUEST_CODE = 0x1;
 
     private SharedPreferences mPreferences;
     private Resources mResources;
     private ArticleListFragment mArticleListFragment;
     private DialogFragment mPendingDialogFragment;
     private FeedListFragment mFeedListFragment;
     private ProgressDialog mSpinner;
     private MenuItem mRefreshItem;
     private MenuItem mPushItem;
     private boolean mTwoPane = false;
     private boolean mUnreadOnly;
     private boolean mHidePushItem = false;
     private boolean mEnableActionView = false;
     private int mSelectedFeed = -1;
 
     /**
      * Handles messages from AsyncTasks started within this activity
      */
     private Handler mAsyncHandler = new AsynHandler(this);
 
     private static class AsynHandler extends Handler {
         private final WeakReference<HomeActivity> targetReference;
 
         AsynHandler(HomeActivity target) {
             targetReference = new WeakReference<HomeActivity>(target);
         }
 
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             HomeActivity target = targetReference.get();
 
             switch (msg.what) {
             case 1:
                 // return from adding a feed
                 target.callbackFeedAdded(msg.arg1);
                 break;
             case 8:
                 // return from adding a feed with error condition
                 target.callbackFeedAddedError(msg.arg1);
                 break;
             default:
                 break;
             }
         }
     };
 
     /**
      * 
      */
     private void callbackFeedAdded(int feedID) {
         try {
             mSpinner.dismiss();
             mSpinner = null;
         } catch (Exception e) {
         }
         if (mPendingDialogFragment != null) {
             try {
                 mPendingDialogFragment.dismiss();
             } catch (Exception e) {
             }
         }
         refreshFeed(feedID);
     }
 
     /**
      * 
      */
     private void callbackFeedAddedError(int returnCondition) {
         try {
             mSpinner.dismiss();
             mSpinner = null;
         } catch (Exception e) {
         }
         switch (returnCondition) {
         case AddFeedTask.ERROR_IOEXCEPTION:
             Helpers.showDialog(this, mResources.getString(R.string.AddFeedError), mResources.getString(R.string.AddFeedErrorConnection), "add_failed");
             break;
         case AddFeedTask.ERROR_NOFEED:
             Helpers.showDialog(this, mResources.getString(R.string.AddFeedError), mResources.getString(R.string.AddFeedErrorNoFeed), "add_failed");
             break;
         case AddFeedTask.ERROR_NOCONTENT:
             Helpers.showDialog(this, mResources.getString(R.string.AddFeedError), mResources.getString(R.string.AddFeedErrorIncompatibleFeed), "add_failed");
             break;
         case AddFeedTask.ERROR_XMLPULLPARSEREXCEPTION:
             Helpers.showDialog(this, mResources.getString(R.string.AddFeedError), mResources.getString(R.string.AddFeedErrorOther), "add_failed");
             break;
         default:
             break;
         }
     }
 
     private BroadcastReceiver mFeedsRefreshedReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             try {
                 mRefreshItem.getActionView().clearAnimation();
                 mRefreshItem.setActionView(null);
             } catch (NullPointerException e) {
 
             }
         }
     };
 
     private BroadcastReceiver mFeedsRefreshingReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             try {
                 mRefreshItem.setActionView(R.layout.actionview_refresh);
             } catch (NullPointerException e) {
 
             }
         }
     };
 
     private BroadcastReceiver mGCMRegisteredReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             try {
                 mSpinner.dismiss();
                 mSpinner = null;
             } catch (NullPointerException e) {
             }
 
             if (intent.getBooleanExtra("success", false)) {
                 mPushItem.setVisible(false);
                 Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnabledTitle),
                         mResources.getString(R.string.FeedsViaPushEnabledText), "push_registered");
             } else {
                 Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnableErrorTitle),
                         mResources.getString(R.string.FeedsViaPushEnableErrorText), "push_failed");
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_home);
 
         mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         mUnreadOnly = mPreferences.getBoolean("unreadonly", true);
 
         mResources = getResources();
 
         mFeedListFragment = (FeedListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_feedlist);
         mArticleListFragment = (ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_articlelist);
         if (mArticleListFragment != null) {
             mTwoPane = true;
         }
 
         Intent intent = getIntent();
         String action = intent.getAction();
         if (action == Intent.ACTION_VIEW) {
             String url = intent.getData().toString();
             addFeed(url);
         } else if (mPreferences.getBoolean("firstrun", true)) {
             firstRun();
         } else {
             maybeShowChangelog();
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
         savedInstanceState.putInt("selectedFeed", mSelectedFeed);
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mSelectedFeed = savedInstanceState.getInt("selectedFeed");
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         LocalBroadcastManager.getInstance(this).registerReceiver(mFeedsRefreshedReceiver, new IntentFilter(RefreshFeedService.BROADCAST_REFRESHED));
         LocalBroadcastManager.getInstance(this).registerReceiver(mFeedsRefreshingReceiver, new IntentFilter(RefreshFeedService.BROADCAST_REFRESHING));
 
         mUnreadOnly = mPreferences.getBoolean("unreadonly", true);
         invalidateOptionsMenu();
 
         mFeedListFragment.setUnreadOnly(mUnreadOnly);
         if (mTwoPane) {
             mArticleListFragment.setUnreadOnly(mUnreadOnly);
         }
 
         boolean refreshing = mPreferences.getBoolean("refreshing", false);
         if (refreshing) {
             if (mRefreshItem != null) {
                 mRefreshItem.setActionView(R.layout.actionview_refresh);
             } else {
                 mEnableActionView = true;
             }
         }
 
         if (mPreferences.getString("eMail", null) != null) {
             if (mPushItem != null) {
                 mPushItem.setVisible(false);
             } else {
                 mHidePushItem = true;
             }
         }
 
         mPreferences.edit().remove("newFeeds").commit();
         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.cancel(0x1);
     }
 
     @Override
     protected void onPause() {
         // workaround for orientation change issues
         FragmentManager fm = getSupportFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
 
         Fragment addDialog = fm.findFragmentByTag("add_dialog");
         Fragment changelogDialog = fm.findFragmentByTag("changelog");
 
         if (addDialog != null) {
             ft.remove(addDialog);
         }
 
         if (changelogDialog != null) {
             ft.remove(changelogDialog);
         }
         ft.commit();
 
         if (mSpinner != null) {
             try {
                 mSpinner.dismiss();
                 mSpinner = null;
             } catch (Exception e) {
             }
         }
 
         try {
             LocalBroadcastManager.getInstance(this).unregisterReceiver(mGCMRegisteredReceiver);
         } catch (IllegalArgumentException e) {
             // might not be registered
         }
         LocalBroadcastManager.getInstance(this).unregisterReceiver(mFeedsRefreshingReceiver);
         LocalBroadcastManager.getInstance(this).unregisterReceiver(mFeedsRefreshedReceiver);
         super.onPause();
     }
 
     @Override
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         if (requestCode == ACCOUNT_REQUEST_CODE && resultCode == RESULT_OK) {
             String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
             registerForPushMessaging(accountName);
         } else {
             Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnableCanceledTitle),
                     mResources.getString(R.string.FeedsViaPushEnableCanceledText), "push_canceled");
         }
     }
 
     /**
      * @see de.hdodenhof.holoreader.misc.FragmentCallback#onFragmentReady(android.support.v4.app.Fragment)
      */
     @Override
     public void onFragmentReady(Fragment fragment) {
         if (mTwoPane && fragment instanceof FeedListFragment) {
             ((FeedListFragment) fragment).setChoiceModeSingle();
         }
     }
 
     /**
      * @see de.hdodenhof.holoreader.misc.FragmentCallback#isDualPane()
      */
     @Override
     public boolean isDualPane() {
         return mTwoPane;
     }
 
     /**
      * @see de.hdodenhof.holoreader.misc.FragmentCallback#isPrimaryFragment(android.support.v4.app.Fragment)
      */
     @Override
     public boolean isPrimaryFragment(Fragment fragment) {
         return fragment instanceof FeedListFragment;
     }
 
     private void maybeShowChangelog() {
         int changelogShown = mPreferences.getInt("changelogShown", 0);
 
         if (changelogShown < getVersion()) {
             final ChangelogDialog changeLogDialog = new ChangelogDialog(this);
             changeLogDialog.show();
 
             mPreferences.edit().putInt("changelogShown", getVersion()).commit();
         }
     }
 
     private void firstRun() {
         DynamicDialogFragment dialogFragment = DynamicDialogFragment.Factory.getInstance(this);
 
         dialogFragment.setTitle(mResources.getString(R.string.AddDefaultFeedsDialogTitle));
         dialogFragment.setMessage(mResources.getString(R.string.AddDefaultFeedsDialogText));
         dialogFragment.setPositiveButtonListener(new DynamicDialogFragment.OnClickListener() {
             @Override
             public void onClick(DialogFragment df, String tag, SparseArray<String> map) {
                 addDefaultFeeds();
                 df.dismiss();
             }
         }, mResources.getString(R.string.AddDefaultFeedsDialogOk));
         dialogFragment.setNegativeButtonText(mResources.getString(R.string.AddDefaultFeedsDialogCancel));
 
         dialogFragment.show(getSupportFragmentManager(), "firstrun");
 
         SharedPreferences.Editor prefsEdit = mPreferences.edit();
         prefsEdit.putBoolean("firstrun", false);
         prefsEdit.putInt("changelogShown", getVersion());
         prefsEdit.commit();
     }
 
     private void addDefaultFeeds() {
         mSpinner = ProgressDialog.show(this, "", mResources.getString(R.string.AddDefaultFeedsSpinner), true);
 
         AsyncTask<Void, Void, Void> addDefaultFeedsTask = new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... params) {
                 ContentResolver contentResolver = getContentResolver();
                 ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();
 
                 for (String[] strings : Helpers.DEFAULTFEEDS) {
                     ContentValues contentValues = new ContentValues();
                     contentValues.put(FeedDAO.NAME, strings[0]);
                     contentValues.put(FeedDAO.URL, strings[1]);
                     contentValuesArrayList.add(contentValues);
                 }
 
                 ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
                 contentValuesArray = contentValuesArrayList.toArray(contentValuesArray);
 
                 contentResolver.bulkInsert(RSSContentProvider.URI_FEEDS, contentValuesArray);
                 return null;
             }
 
             @Override
             protected void onPostExecute(Void result) {
                 mSpinner.dismiss();
                 mSpinner = null;
                 Toast.makeText(HomeActivity.this, mResources.getString(R.string.AddDefaultFeedsToast), Toast.LENGTH_LONG).show();
                 refreshFeeds(true);
             }
         };
         addDefaultFeedsTask.execute();
     }
 
     /**
      * Starts an AsyncTask to fetch a new feed and add it to the database
      * 
      * @param feedUrl
      *            URL of the feed to fetch
      */
     private void addFeed(String url) {
         URL parsedUrl = parseUrl(url);
 
         if (parsedUrl != null) {
             mSpinner = ProgressDialog.show(this, "", mResources.getString(R.string.AddFeedSpinner), true);
             AddFeedTask addFeedTask = new AddFeedTask(mAsyncHandler, this);
             addFeedTask.execute(parsedUrl);
         } else {
             Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.AddFeedError), mResources.getString(R.string.AddFeedErrorInvalidUrl),
                     "add_invalid_url");
         }
     }
 
     /**
      * Spawns AsyncTasks to refresh all feeds
      * 
      * @param item
      *            MenuItem that holds the refresh animation
      */
     private void refreshFeeds(boolean manual) {
         boolean isConnected = Helpers.isConnected(this);
 
         if (isConnected) {
             HashSet<Integer> feedIDs = Helpers.queryFeeds(getContentResolver());
             if (!feedIDs.isEmpty())
                 for (Integer mFeedID : feedIDs) {
                     refreshFeed(mFeedID);
                 }
             else {
                 Toast.makeText(this, mResources.getString(R.string.ToastNothingToRefresh), Toast.LENGTH_SHORT).show();
             }
         } else {
             if (manual) {
                 Helpers.showDialog(this, mResources.getString(R.string.NoConnectionTitle), mResources.getString(R.string.NoConnectionText), "no_connection");
             }
         }
     }
 
     /**
      * Refreshes a single feed
      * 
      * @param feedID
      */
     @SuppressLint("NewApi")
     private void refreshFeed(int feedID) {
         boolean refreshing = mPreferences.getBoolean("refreshing", false);
         if (!refreshing) {
             mRefreshItem.setActionView(R.layout.actionview_refresh);
         }
 
         Intent intent = new Intent(this, RefreshFeedService.class);
 
         intent.putExtra("feedid", feedID);
         startService(intent);
     }
 
     /**
      * Shows a dialog to add a new feed URL
      */
     private void showAddDialog() {
         boolean isConnected = Helpers.isConnected(this);
 
         if (isConnected) {
             DynamicDialogFragment dialogFragment = DynamicDialogFragment.Factory.getInstance(this);
 
             dialogFragment.setTitle(mResources.getString(R.string.AddFeedDialogTitle));
             dialogFragment.setLayout(R.layout.fragment_dialog_add);
             dialogFragment.setPositiveButtonListener(new DynamicDialogFragment.OnClickListener() {
                 @Override
                 public void onClick(DialogFragment df, String tag, SparseArray<String> map) {
                     mPendingDialogFragment = df;
                     addFeed(map.get(R.id.enterUrl));
                 }
             }, mResources.getString(R.string.AddFeedDialogOk));
 
             dialogFragment.show(getSupportFragmentManager(), "add_dialog");
         } else {
             Helpers.showDialog(this, mResources.getString(R.string.NoConnectionTitle), mResources.getString(R.string.NoConnectionText), "no_connection");
         }
     }
 
     private void startGCMRegistrationFlow() {
         if (mPreferences.getString("uuid", null) == null) {
             UUID uuid = UUID.randomUUID();
             mPreferences.edit().putString("uuid", uuid.toString()).commit();
         }
 
         if (Helpers.isConnected(this)) {
             DynamicDialogFragment dialogFragment = DynamicDialogFragment.Factory.getInstance(this);
 
             dialogFragment.setTitle(mResources.getString(R.string.FeedsViaPushEnableDialogTitle));
             dialogFragment.setMessage(mResources.getString(R.string.FeedsViaPushEnableDialogText));
             dialogFragment.setPositiveButtonListener(new DynamicDialogFragment.OnClickListener() {
                 @Override
                 public void onClick(DialogFragment df, String tag, SparseArray<String> map) {
                     df.dismiss();
 
                     Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null,
                             null, null);
                     startActivityForResult(intent, ACCOUNT_REQUEST_CODE);
                 }
             }, mResources.getString(R.string.FeedsViaPushEnableDialogOK));
             dialogFragment.setNegativeButtonText(mResources.getString(R.string.FeedsViaPushEnableDialogNOK));
 
             dialogFragment.show(getSupportFragmentManager(), "enable_push");
         } else {
             Helpers.showDialog(this, mResources.getString(R.string.NoConnectionTitle), mResources.getString(R.string.NoConnectionText), "no_connection");
         }
     }
 
     /**
      * 
      * @param eMail
      */
     private void registerForPushMessaging(final String eMail) {
         final String uuid = mPreferences.getString("uuid", null);
        mPreferences.edit().putString("eMail", eMail).commit();
 
         GCMRegistrar.checkDevice(this);
         GCMRegistrar.checkManifest(this);
 
         final String registrationId = GCMRegistrar.getRegistrationId(this);
 
         if (registrationId.equals("")) {
             mSpinner = ProgressDialog.show(this, "", mResources.getString(R.string.PushRegistrationSpinner), true);
            LocalBroadcastManager.getInstance(this).registerReceiver(mGCMRegisteredReceiver, new IntentFilter(GCMIntentService.BROADCAST_REGISTERED));
             GCMRegistrar.register(this, Config.GCM_SENDER_ID);
         } else {
             if (!GCMRegistrar.isRegisteredOnServer(this)) {
                 mSpinner = ProgressDialog.show(this, "", mResources.getString(R.string.PushRegistrationSpinner), true);
                 AsyncTask<Void, Void, Boolean> registerForPushTask = new AsyncTask<Void, Void, Boolean>() {
                     @Override
                     protected Boolean doInBackground(Void... params) {
                         return GCMServerUtilities.registerOnServer(eMail, registrationId, uuid);
                     }
 
                     @Override
                     protected void onPostExecute(Boolean success) {
                         mSpinner.dismiss();
                         mSpinner = null;
                         if (success) {
                             GCMRegistrar.setRegisteredOnServer(HomeActivity.this, true);
                             mPushItem.setVisible(false);
                             Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnabledTitle),
                                     mResources.getString(R.string.FeedsViaPushEnabledText), "push_registered");
                         } else {
                            mPreferences.edit().remove("eMail").commit();
                             Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnableErrorTitle),
                                     mResources.getString(R.string.FeedsViaPushEnableErrorText), "push_failed");
                         }
                     }
                 };
                 registerForPushTask.execute();
             } else {
                 // This is impossible, show a message anyway
                 Helpers.showDialog(HomeActivity.this, mResources.getString(R.string.FeedsViaPushEnabledTitle),
                         mResources.getString(R.string.FeedsViaPushEnabledText), "push_registered");
             }
         }
     }
 
     /**
      * 
      * @param url
      * @return
      */
     private URL parseUrl(String url) {
         URL parsedUrl = null;
 
         if (url.length() < 7 || !url.substring(0, 7).equalsIgnoreCase("http://")) {
             url = "http://" + url;
         }
 
         Pattern pattern = Helpers.PATTERN_WEB;
         Matcher matcher = pattern.matcher(url);
 
         try {
             if (matcher.matches()) {
                 parsedUrl = new URL(url);
             }
         } catch (MalformedURLException e) {
             return null;
         }
 
         return parsedUrl;
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         RSSAdapter adapter;
         Cursor cursor;
 
         if (parent.getAdapter() instanceof HeaderViewListAdapter) {
             HeaderViewListAdapter wrappingAdapter = (HeaderViewListAdapter) parent.getAdapter();
             adapter = (RSSAdapter) wrappingAdapter.getWrappedAdapter();
         } else {
             adapter = (RSSAdapter) parent.getAdapter();
         }
 
         switch (adapter.getType()) {
         case RSSAdapter.TYPE_FEED:
             if (position == 0) {
                 mSelectedFeed = -1;
             } else {
                 cursor = ((RSSFeedAdapter) adapter).getCursor();
                 cursor.moveToPosition(position - 1);
                 mSelectedFeed = cursor.getInt(cursor.getColumnIndex(FeedDAO._ID));
             }
 
             if (mTwoPane) {
                 mArticleListFragment.selectFeed(mSelectedFeed);
             } else {
                 Intent intent = new Intent(this, DisplayFeedActivity.class);
                 intent.putExtra("feedid", mSelectedFeed);
                 startActivity(intent);
             }
 
             break;
 
         // DualPane layout only
         case RSSAdapter.TYPE_ARTICLE:
             cursor = ((RSSArticleAdapter) adapter).getCursor();
             cursor.moveToPosition(position);
             int articleID = cursor.getInt(cursor.getColumnIndex(ArticleDAO._ID));
 
             Intent intent = new Intent(this, DisplayFeedActivity.class);
             intent.putExtra("articleid", articleID);
             intent.putExtra("feedid", mSelectedFeed);
             intent.putExtra("unreadAfter", new Date());
             startActivity(intent);
             break;
 
         default:
             break;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getSupportMenuInflater();
         menuInflater.inflate(R.menu.main, menu);
 
         mRefreshItem = menu.getItem(0);
         mPushItem = menu.getItem(5);
 
         if (mEnableActionView) {
             mRefreshItem.setActionView(R.layout.actionview_refresh);
         }
 
         if (mHidePushItem) {
             mPushItem.setVisible(false);
         }
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.item_refresh:
             refreshFeeds(true);
             return true;
         case R.id.item_add:
             showAddDialog();
             return true;
         case R.id.item_toggle:
             mUnreadOnly = !mUnreadOnly;
 
             SharedPreferences.Editor editor = mPreferences.edit();
             editor.putBoolean("unreadonly", mUnreadOnly);
             editor.commit();
 
             if (mUnreadOnly) {
                 Toast.makeText(this, mResources.getString(R.string.ToastUnreadArticles), Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(this, mResources.getString(R.string.ToastAllArticles), Toast.LENGTH_SHORT).show();
             }
             mFeedListFragment.setUnreadOnly(mUnreadOnly);
             if (mTwoPane) {
                 mArticleListFragment.setUnreadOnly(mUnreadOnly);
             }
             return true;
         case R.id.item_markread:
             if (mTwoPane && mSelectedFeed != -1) {
                 MarkReadRunnable markReadRunnable = new MarkReadRunnable((Context) this);
                 markReadRunnable.setFeed(mSelectedFeed);
                 new Thread(markReadRunnable).start();
                 if (mUnreadOnly) {
                     mArticleListFragment.selectFeed(-1);
                 }
             } else {
                 new Thread(new MarkReadRunnable((Context) this)).start();
             }
             return true;
         case R.id.item_editfeeds:
             startActivity(new Intent(this, EditFeedsActivity.class));
             return true;
         case R.id.item_regpush:
             startGCMRegistrationFlow();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
