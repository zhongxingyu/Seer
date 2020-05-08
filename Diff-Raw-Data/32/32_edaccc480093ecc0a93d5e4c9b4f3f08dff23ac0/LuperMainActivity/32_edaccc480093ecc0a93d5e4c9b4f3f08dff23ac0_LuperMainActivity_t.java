 // LuperApp.java
 // -------------
 
 // NOTE: ActionBarSherlock is a polyfill providing the new Android 4.2 ActionBar
 //       functionality to all versions of android.  To compile this project
 //       properly, the actionbarsherlock project must also be in your
 //       workspace and/or build path in addition to the Luper project.
 //       See README.md on github for more details.
 
 package com.teamluper.luper;
 
 // imports from the core android API
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.UiThread;
 
 import java.io.File;
 import java.util.List;
 
 // @EActivity = "Enhanced Activity", which turns on AndroidAnnotati1ons features
 @EActivity
 public class LuperMainActivity extends SherlockFragmentActivity {
 
   static LuperMainActivity instance;
 
   ViewPager mViewPager;
   TabsAdapter mTabsAdapter;
 
   // Additional local variables
   AccountManager am;
   SQLiteDataSource dataSource;
 
   @Override
     protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
     final ActionBar bar = getSupportActionBar();
     bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // Gives us Tabs!
     bar.setDisplayShowTitleEnabled(false);
    bar.setDisplayHomeAsUpEnabled(false);
 
     // set up the ViewPager, which we will use in conjunction with tabs.
     // this makes it possible to swipe left and right between the tabs.
     mViewPager = new ViewPager(this);
     mViewPager.setId(R.id.tabcontentpager);
     setContentView(mViewPager);
 
     dataSource = new SQLiteDataSource(this);
     dataSource.open();
 
     // FIXME this is slowing down the app launch dramatically.  Perhaps do it in background?
     //Creates a folder for Luper and associated clips and projects
     File nfile=new File(Environment.getExternalStorageDirectory()+"/LuperApp/Clips");
     File mfile=new File(Environment.getExternalStorageDirectory()+"/LuperApp/Projects");
     nfile.mkdir();
     mfile.mkdir();
 
     // now we set up the TabsAdapter, which is a special class borrowed from Google.
     // TabsAdapter.java takes care of all the guts of the Tab interactions, and
     // links it with our ViewPager for us.  The code below is all we need to
     // add some fragment content as tabs in the ActionBar!
     mTabsAdapter = new TabsAdapter(this, mViewPager);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Home"),
         TabHomeFragment_.class, null);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Projects"),
         TabProjectsFragment_.class, null);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Friends"),
         TabFriendsFragment_.class, null);
 
     //create a directory to save in
     File testdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/LuperApp/");
     testdir.mkdirs();
   }
 
   @Override
   protected void onStop() {
     if(dataSource.isOpen()) dataSource.close();
     super.onStop();
   }
 
   @Override
   protected void onResume() {
     if(!dataSource.isOpen()) dataSource.open();
     super.onResume();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inf = getSupportMenuInflater();
     inf.inflate(R.menu.activity_main, menu);
 
     // TODO remove this code if we want the New Project button to stay the way it is now.
     // because one of the action items is a custom view,
     // we need the next few lines to force it to use onOptionsItemSelected
     // when it's clicked.
     final MenuItem item = menu.findItem(R.id.menu_new_project);
     item.getActionView().setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         onOptionsItemSelected(item);
       }
     });
 
     // TODO replace this code with a better way to make sure when we're logged in we see "Logout" instead of "Login/Register"
     /*
     User activeUser = dataSource.getActiveUser();
     if(activeUser == null) {
       // we're logged out
       menu.add(0,1,0,"Log In");
       menu.add(0,2,0,"Register");
     } else {
       // we're logged in
       menu.add(0,3,0,"Log Out "+activeUser.getEmail());
     }
     */
 
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if(item.getItemId() == R.id.menu_new_project) {
       DialogFactory.prompt(this,"New Project",
         "Please type a name for your project.  You can change it later.",
         new Lambda.StringCallback() {
           public void go(String value) {
             newProject(value);
           }
         }
       );
     }
     if(item.getItemId() == R.id.menu_settings) {
       Intent intent = new Intent(this, LuperSettingsActivity_.class);
       startActivity(intent);
     }
     if(item.getItemId() == R.id.menu_logout) {
       Intent intent = new Intent(this, LuperLoginActivity_.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(intent);
       finish();
     }
     if(item.getItemId() == R.id.devtools) {
       Intent intent = new Intent(this, LuperDevToolsActivity_.class);
       startActivity(intent);
     }
 
     if(item.getItemId() == R.id.help) {
           Intent intent = new Intent(this, LuperHelp_.class);
           startActivity(intent);
     }
 
     return super.onOptionsItemSelected(item);
   }
 
   public SQLiteDataSource getDataSource() {
     return dataSource;
   }
 
   public static boolean deviceIsOnline(Context context) {
     // borrowed implementation from:
     // http://stackoverflow.com/questions/2789612/how-can-i-check-whether-an-android-device-is-connected-to-the-web
     ConnectivityManager cm =
       (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo ni = cm.getActiveNetworkInfo();
     if (ni == null) return false;
     return ni.isConnected();
   }
 
   @UiThread
   public void alert(String title, String message) {
     DialogFactory.alert(this, title, message);
   }
 
   public void newProject(String title) {
     Sequence newSequence = dataSource.createSequence(null, title);
     ListView lv = (ListView) findViewById(R.id.projectsListView);
     @SuppressWarnings("unchecked")
     ArrayAdapter<Sequence> adapter = (ArrayAdapter<Sequence>) lv.getAdapter();
     adapter.add(newSequence);
   }
 
   @Background
   public void devTools(View view) {
     Intent intent = new Intent(this, LuperDevToolsActivity_.class);
     startActivity(intent);
   }
 
 
   @Background
   public void launchProjectEditor(long projectId) {
     Intent intent = new Intent(this, LuperProjectEditorActivity_.class);
     if(projectId != -1)  intent.putExtra("selectedProjectId", projectId);
 		startActivity(intent);
   }
 
   // this will be removed too, it's checking the google account that the
   // device's user is already logged in with.  We'll likely ditch this in favor
   // of a Facebook-based login solution.
   @Background
   void testAccounts() {
     if(am == null) am = AccountManager.get(this);
     Account[] accounts = am.getAccountsByType("com.google");
     System.out.println("== LUPER ACCOUNTS TESTING ==  found "+accounts.length+" accounts");
     for(int i=0; i<accounts.length; i++) {
       System.out.println(accounts[i].toString());
     }
   }

   public TabsAdapter checkTabs() { return mTabsAdapter; }
 }
