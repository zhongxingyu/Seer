 package org.aitek.controller.activities;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.SearchView;
 import android.widget.Toast;
 import org.aitek.controller.R;
 import org.aitek.controller.activities.fragment.MoviesFragment;
 import org.aitek.controller.activities.fragment.MusicFragment;
 import org.aitek.controller.activities.fragment.PlaylistFragment;
 import org.aitek.controller.activities.fragment.TabFragment;
 import org.aitek.controller.loaders.CacheLoaderTask;
 import org.aitek.controller.loaders.Mede8erLoaderTask;
 import org.aitek.controller.mede8er.Mede8erCommander;
 import org.aitek.controller.utils.Constants;
 import org.aitek.controller.utils.IoUtils;
 import org.aitek.controller.utils.Logger;
 
 import java.io.File;
 
 import static org.aitek.controller.mede8er.Mede8erStatus.*;
 
 public class MainActivity extends Activity implements SearchView.OnQueryTextListener {
 
     private SearchView searchView;
     private ActionBar actionBar;
     private Mede8erCommander mede8erCommander;
     private TabFragment currentTabFragment;
    private boolean isNoCache;
     private Handler dialogHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
 
             switch (msg.what) {
                 case DOWN:
                     showAlertDialog(getString(R.string.no_mede8er), getString(R.string.no_mede8er_message));
                     break;
                 case NO_JUKEBOX:
                     showAlertDialog(getString(R.string.nas_not_found), getString(R.string.nas_not_found_message));
                     break;
                 case FULLY_OPERATIONAL:
                    if (isNoCache) loadFromNetwork();
                     else loadFromCache();
                     break;
                 case SHOW_MOVIES:
                     createPage();
                     break;
             }
         }
 
         private void showAlertDialog(String title, String message) {
             AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
             builder.setMessage("Look at this dialog!")
                     .setCancelable(false)
                     .setTitle(title)
                     .setMessage(message)
                     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                         }
                     });
             AlertDialog alert = builder.create();
             alert.show();
         }
     };
 
     public Handler getDialogHandler() {
         return dialogHandler;
     }
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         actionBar = getActionBar();
 
 
         Constants.THUMBS_DIRECTORY = getApplicationContext().getFilesDir() + "/" + Constants.THUMBS_DIRECTORY_NAME;
         //readPrivateDir(true);
         File file = new File(Constants.THUMBS_DIRECTORY);
         file.mkdirs();
 
        isNoCache = !IoUtils.isFileExisting(this, Constants.MOVIES_FILE);
 
         mede8erCommander = Mede8erCommander.getInstance(this, false);
         if (!mede8erCommander.isConnected()) {
             mede8erCommander.launchConnector();
         }
         else {
             createPage();
         }
     }
 
     private void loadFromNetwork() {
         Mede8erLoaderTask mede8erLoaderTask = new Mede8erLoaderTask(this);
         mede8erLoaderTask.execute(new String[]{});
     }
 
     private void loadFromCache() {
         CacheLoaderTask cacheLoaderTask = new CacheLoaderTask(this);
         cacheLoaderTask.execute(new String[]{});
     }
 
     private void createPage() {
 
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         ActionBar.Tab moviesTab = actionBar.newTab().setText(getString(R.string.movies_tab));
         ActionBar.Tab musicTab = actionBar.newTab().setText(getString(R.string.music_tab));
         ActionBar.Tab playlistTab = actionBar.newTab().setText(getString(R.string.playlist_tab));
 
         Fragment moviesFragment = new MoviesFragment();
         Fragment musicFragment = new MusicFragment();
         Fragment playlistFragment = new PlaylistFragment();
 
         moviesTab.setTabListener(new MyTabsListener(moviesFragment));
         musicTab.setTabListener(new MyTabsListener(musicFragment));
         playlistTab.setTabListener(new MyTabsListener(playlistFragment));
 
         actionBar.addTab(moviesTab);
         actionBar.addTab(musicTab);
         actionBar.addTab(playlistTab);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_main, menu);
         MenuItem searchItem = menu.findItem(R.id.action_search);
         searchView = (SearchView) searchItem.getActionView();
         searchView.setIconifiedByDefault(false);
         searchView.setOnQueryTextListener(this);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.menu_settings:
 
                 Toast toast = Toast.makeText(getApplicationContext(), "Settings!", Toast.LENGTH_SHORT);
                 toast.show();
                 return true;
 
             case R.id.menu_scan_mediaplayer:
 //                searchingMede8erProgress = ProgressDialog.show(MainActivity.this, getString(R.string.network_discovery), getString(R.string.network_discovery_message));
                 readPrivateDir(true);
                 Mede8erLoaderTask task = new Mede8erLoaderTask(this);
                 task.execute(new String[]{});
                 return true;
 
             case R.id.menu_reset:
 
                 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                 preferences.edit().remove(Constants.PREFERENCES_MEDE8ER_IPADDRESS);
                 preferences.edit().commit();
                 readPrivateDir(true);
                 Logger.log("Data reset successful.");
                 return true;
 
             case R.id.menuSortByTitle:
                 mede8erCommander.getMoviesManager().setSortField("title");
                 currentTabFragment.notifyChangedGridData();
                 return true;
 
             case R.id.menuSortByDate:
                 mede8erCommander.getMoviesManager().setSortField("date");
                 currentTabFragment.notifyChangedGridData();
                 return true;
 
             case R.id.menu_mediaplayer_info:
                 Logger.log("Showing file listing: ");
                 readPrivateDir(false);
                 Logger.log("Movies file: ");
                 try {
                     Logger.log(IoUtils.readPrivateFile(Constants.MOVIES_FILE, this));
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                 }
 
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void readPrivateDir(boolean deleteFiles) {
         File dir = getFilesDir();
         File[] subFiles = dir.listFiles();
 
         if (subFiles != null) {
             for (File file : subFiles) {
                 if (deleteFiles) {
                     file.delete();
                 }
                 else {
                     Logger.log(file.getAbsolutePath());
                 }
             }
         }
 
         // delete thumbs
         dir = new File(Constants.THUMBS_DIRECTORY);
         File[] thumbs = dir.listFiles();
         if (thumbs != null) {
 
             Logger.log("Deleting thumbs..");
             for (File thumb : thumbs) {
                 if (deleteFiles) {
                     thumb.delete();
                 }
                 else {
                     Logger.log(thumb.getAbsolutePath());
                 }
             }
         }
     }
 
     @Override
     public boolean onQueryTextChange(String newText) {
 
         if (newText.length() == 0) {
             searchView.clearFocus();
         }
         mede8erCommander.getMoviesManager().setGenericFilter(newText);
         updateSearch(newText);
 
         return true;
     }
 
     @Override
     public boolean onQueryTextSubmit(String query) {
 
         updateSearch(query);
         searchView.clearFocus();
         return true;
     }
 
     private void updateSearch(String query) {
 
         String tabName = getString(R.string.movies_tab);
         if (actionBar.getSelectedTab() != null) {
             if (tabName.equals(actionBar.getSelectedTab().getText())) {
                 mede8erCommander.getMoviesManager().setGenericFilter(query);
                 currentTabFragment.notifyChangedGridData();
             }
             else if (tabName.equals(actionBar.getSelectedTab().getText())) {
                 //  mede8erCommander.getMusicManager().setGenericFilter(query);
                 currentTabFragment.notifyChangedGridData();
             }
         }
 
     }
 
     class MyTabsListener implements ActionBar.TabListener {
         public Fragment fragment;
 
         public MyTabsListener(Fragment fragment) {
             this.fragment = fragment;
         }
 
         @Override
         public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         }
 
         @Override
         public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
             fragmentTransaction.replace(R.id.fragment_container, fragment);
             currentTabFragment = (TabFragment) fragment;
         }
 
         @Override
         public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
             fragmentTransaction.remove(fragment);
         }
 
     }
 
 
 }
