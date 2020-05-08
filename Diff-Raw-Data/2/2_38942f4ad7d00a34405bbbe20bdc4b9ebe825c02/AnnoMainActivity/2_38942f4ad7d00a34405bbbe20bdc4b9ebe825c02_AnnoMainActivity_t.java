 package co.usersource.annoplugin.view;
 
 import android.content.ContentUris;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import co.usersource.annoplugin.AnnoPlugin;
 import co.usersource.annoplugin.R;
 import co.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
 import co.usersource.annoplugin.model.AnnoContentProvider;
 import co.usersource.annoplugin.utils.PluginUtils;
 import co.usersource.annoplugin.utils.SystemUtils;
 
 /**
  * Home screen. Displays a list of all comments, by clicking any comment,
  * comment detail can be viewed.
  * 
  * @author topcircler
  * 
  */
 public class AnnoMainActivity extends FragmentActivity implements
     LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
   /*
    * consider using section list.
    * 
    * TODO: android-section-list: http://code.google.com/p/android-section-list/
    */
 
   private static final String TAG = AnnoMainActivity.class.getSimpleName();
 
   /**
    * which fields to display in the list.
    */
   private static final String[] PROJECTION = {
       TableCommentFeedbackAdapter.COL_ID,
       TableCommentFeedbackAdapter.COL_COMMENT };
   /**
    * id to represent a loader process.
    */
   private static final int URL_LOADER_COMMENTS = 0;
 
   // view components.
   private SimpleCursorAdapter adapter;
   private ListView feedbackListView;
   private Button btnCommunity;
 
   private int level;
 
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
       //Intent intent = new Intent(AnnoMainActivity.this,
              //IntroActivity.class);
       //AnnoMainActivity.this.startActivity(intent);
       super.onCreate(savedInstanceState);
       setContentView(R.layout.anno_home);
 
       setComponent();
       handleIntent();
 
       // load default preferences.
       AnnoPlugin.setEnableGesture(this, R.id.gestures, true);
       loadPreferences();
   }
 
   private void loadPreferences() {
     PreferenceManager.setDefaultValues(this, R.xml.anno_preferences, false);
   }
 
   private void handleIntent() {
     Intent intent = getIntent();
     level = intent.getIntExtra(PluginUtils.LEVEL, 0);
   }
 
   private void setComponent() {
     feedbackListView = (ListView) this.findViewById(R.id.feedbackList);
     String[] bindFrom = new String[] { TableCommentFeedbackAdapter.COL_COMMENT };
     int[] bindTo = new int[] { R.id.commentLabel };
     adapter = new SimpleCursorAdapter(getApplicationContext(),
         R.layout.comment_row, null, bindFrom, bindTo, 0);
     feedbackListView.setAdapter(adapter);
     feedbackListView.setOnItemClickListener(this);
 
     getActionBar().setLogo(R.drawable.anno_ic_launcher);
     if (PluginUtils.isAnno(this.getPackageName())) {
       getActionBar().setTitle(R.string.plugin_name);
     } else {
       String appName;
       try {
         appName = SystemUtils.getAppName(this);
         String appVersion = SystemUtils.getAppVersion(this);
         String pluginName = this.getResources().getString(R.string.plugin_name);
         String title = pluginName + " About " + appName + " " + appVersion;
         getActionBar().setTitle(title);
       } catch (NameNotFoundException e) {
         Log.e(TAG, "Failed to get app name or version.");
         Log.e(TAG, e.getMessage(), e);
         getActionBar().setTitle(R.string.plugin_name);
       }
     }
 
     btnCommunity = (Button) this.findViewById(R.id.btnCommunity);
     btnCommunity.setOnClickListener(new OnClickListener() {
 
       @Override
       public void onClick(View arg0) {
         Intent intent = new Intent(AnnoMainActivity.this,
             CommunityActivity.class);
         AnnoMainActivity.this.startActivity(intent);
       }
 
     });
   }
 
   /**
    * Loads all comments and display in this list once loads completes.
    */
   private void loadComments() {
     Log.d(TAG, "start query comments.");
     getSupportLoaderManager().restartLoader(URL_LOADER_COMMENTS, null, this);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.anno_main, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     // switch (item.getItemId()) {
     // case R.id.settings_menu:
     Intent i = new Intent(this, AnnoSettingActivity.class);
     startActivity(i);
     return true;
     // default:
     // return super.onOptionsItemSelected(item);
     // }
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     // no need to close cursor here, since it's already done in onLoaderReset().
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     loadComments();
   }
 
   @Override
   public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
     switch (loaderId) {
     case URL_LOADER_COMMENTS:
       return new CursorLoader(this, AnnoContentProvider.COMMENT_PATH_URI,
           PROJECTION, null, null, null);
     default:
       // An invalid id was passed in.
       return null;
     }
   }
 
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
     Log.d(TAG, "query finishes.");
     if (adapter != null && cursor != null) {
       adapter.swapCursor(cursor);
     }
   }
 
   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
     Log.d(TAG, "query reset.");
     if (adapter != null) {
       adapter.swapCursor(null);
     }
   }
 
   @Override
   public void onItemClick(AdapterView<?> adapterView, View v, int position,
       long id) {
     Log.d(TAG, String.format("item %s(id:%d) was clicked.", position, id));
     Intent intent = new Intent(this, FeedbackViewActivity.class);
     intent.putExtra(AnnoContentProvider.COMMENT_PATH,
         ContentUris.withAppendedId(AnnoContentProvider.COMMENT_PATH_URI, id));
     intent.putExtra(PluginUtils.LEVEL, level);
     startActivity(intent);
   }
 
   /**
    * @return the level
    */
   public int getLevel() {
     return level;
   }
 
 }
