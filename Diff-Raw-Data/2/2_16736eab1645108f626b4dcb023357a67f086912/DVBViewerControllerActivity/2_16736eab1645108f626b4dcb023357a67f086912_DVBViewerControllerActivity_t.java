 package de.bennir.DVBViewerController;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import de.bennir.DVBViewerController.service.DVBServer;
 import de.bennir.DVBViewerController.service.DVBService;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 public class DVBViewerControllerActivity extends FragmentActivity {
     private static final String TAG = DVBViewerControllerActivity.class.toString();
     private static final String OPENED_KEY = "OPENED_KEY";
     public static int currentGroup = -1;
     public static de.keyboardsurfer.android.widget.crouton.Configuration croutonInfinite = new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
             .setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE)
             .build();
     public DVBService mDVBService;
     public Typeface robotoThin;
     public static Typeface robotoLight;
     public Typeface robotoCondensed;
     public Fragment mContent;
     private ListView mDrawerList;
     private DrawerLayout mDrawerLayout;
     private LinearLayout mDrawer;
     private String mTitle;
     private ActionBarDrawerToggle mDrawerToggle;
     private SharedPreferences prefs = null;
     private Boolean opened = null;
 
     @Override
     protected void onDestroy() {
         // Workaround until there's a way to detach the Activity from Crouton while
         // there are still some in the Queue.
         Crouton.clearCroutonsForActivity(this);
         mDVBService.destroy();
         super.onDestroy();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (mDrawerLayout != null && mDrawer != null) {
             for (int i = 0; i < menu.size(); i++) {
                 MenuItem item = menu.getItem(i);
                 if (item != null) {
                     item.setVisible(!mDrawerLayout.isDrawerOpen(mDrawer));
                 }
             }
         }
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         initFonts();
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             DVBServer server = new DVBServer();
             server.host = extras.getString(DVBService.DVBHOST_KEY);
             server.ip = extras.getString(DVBService.DVBIP_KEY);
             server.port = extras.getString(DVBService.DVBPORT_KEY);
 
             mDVBService = DVBService.getInstance(getApplicationContext(), server);
         } else {
             try {
                 throw new Exception("No Bundle");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
 
         mTitle = getString(R.string.remote);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setTitle(R.string.remote);
         getActionBar().setIcon(R.drawable.ic_action_remote);
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
 
         /**
          * Above View
          */
         if (savedInstanceState != null)
             mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
         if (mContent == null)
             mContent = new RemoteFragment();
 
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, mContent)
                 .commit();
 
 
         /**
          * Behind View
          */
         mDrawerList = (ListView) findViewById(R.id.menu_list);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawer = (LinearLayout) findViewById(R.id.drawer);
 
         mDrawer.setBackgroundResource(R.color.DVBActionBar);
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.open_drawer,
                 R.string.close_drawer
         ) {
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 if (mTitle.equals(getString(R.string.epg))) {
                     getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                     getActionBar().setDisplayShowTitleEnabled(false);
                 }
                 getActionBar().setTitle(mTitle);
                 invalidateOptionsMenu();
                 if (opened != null && opened == false) {
                     opened = true;
                     if (prefs != null) {
                         SharedPreferences.Editor editor = prefs.edit();
                         editor.putBoolean(OPENED_KEY, true);
                         editor.apply();
                     }
                 }
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 if (mTitle.equals(getString(R.string.epg))) {
                     getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                     getActionBar().setDisplayShowTitleEnabled(true);
                 }
                 getActionBar().setTitle(R.string.app_name);
                 invalidateOptionsMenu();
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 prefs = getPreferences(MODE_PRIVATE);
                 opened = prefs.getBoolean(OPENED_KEY, false);
                 if (opened == false) {
                     mDrawerLayout.openDrawer(mDrawer);
                 }
             }
         }).start();
 
         TextView activeProfile = (TextView) findViewById(R.id.active_profile);
         activeProfile.setTypeface(robotoCondensed);
         if (!mDVBService.getDVBServer().host.equals(DVBService.DEMO_DEVICE)) {
             activeProfile.setText(mDVBService.getDVBServer().host);
         }
         activeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent mIntent = new Intent(getApplicationContext(), DeviceSelectionActivity.class);
                 startActivity(mIntent);
 
                 mDVBService.destroy();
 
                 DVBViewerControllerActivity.this.finish();
                 overridePendingTransition(R.anim.fadein, R.anim.slide_to_right);
             }
         });
 
         addMenuItems(getApplicationContext());
 
         /**
          * Channel Loading
          */
         if (mDVBService.getDVBChannels().isEmpty()) {
             Log.d(TAG, "DVBChannels empty");
             Style st = new Style.Builder()
                     .setConfiguration(DVBViewerControllerActivity.croutonInfinite)
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(this, R.string.loadingChannels, st).show();
             mDVBService.loadChannels();
         }
     }
 
     /**
      * Adds Menu Items to NavDrawer
      *
      * @param context Application Context
      */
     private void addMenuItems(Context context) {
         MenuAdapter adapter = new MenuAdapter(context);
 
         adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));
         adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels));
         adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg));
         adapter.add(new DVBMenuItem(getString(R.string.timer), R.drawable.ic_action_timers));
         adapter.add(new DVBMenuItem(getString(R.string.settings), R.drawable.ic_action_settings));
 
         mDrawerList.setAdapter(adapter);
         mDrawerList.setItemChecked(0, true);
 
         mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                 Fragment newContent = null;
                 int titleRes = 0;
                 int icon = 0;
 
                 switch (position) {
                     case 0:
                         // Remote
                         newContent = new RemoteFragment();
                         titleRes = R.string.remote;
                         icon = R.drawable.ic_action_remote;
                         break;
                     case 1:
                         // Channels
                         newContent = new ChannelFragment();
                         titleRes = R.string.channels;
                         icon = R.drawable.ic_action_channels;
                         break;
                     case 2:
                         // EPG
                         newContent = new EPGFragment();
                         titleRes = R.string.epg;
                         icon = R.drawable.ic_action_epg;
                         break;
                     case 3:
                         // Timers
                         newContent = new TimerFragment();
                         titleRes = R.string.timer;
                         icon = R.drawable.ic_action_timers;
                         break;
                     case 4:
                         // Settings
                         newContent = new SettingsFragment();
                         titleRes = R.string.settings;
                         icon = R.drawable.ic_action_settings;
                         break;
                 }
                 if (newContent != null) {
                     getActionBar().setDisplayShowTitleEnabled(true);
                     getSupportFragmentManager().popBackStackImmediate();
                     mTitle = getString(titleRes);
                     switchContent(newContent, titleRes, icon);
                 }
 
                 mDrawerList.setItemChecked(position, true);
                 mDrawerLayout.closeDrawer(mDrawer);
             }
         });
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         // Handle your other action bar items...
 
         return super.onOptionsItemSelected(item);
     }
 
     private void initFonts() {
         robotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
         robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
         robotoCondensed = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == 1)
             mDVBService.loadTimers();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         Log.d(TAG, "onSaveInstanceState");
         super.onSaveInstanceState(outState);
 
         getSupportFragmentManager().putFragment(outState, "mContent", mContent);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
             mDVBService.sendCommand("sendVolDown");
             return true;
         } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             mDVBService.sendCommand("sendVolUp");
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             return true;
         }
 
        return super.onKeyUp(keyCode, event);
     }
 
     public void switchContent(Fragment fragment, int titleRes, int icon) {
         switchContent(fragment, getString(titleRes), icon);
     }
 
     public void switchContent(Fragment fragment, String title, int icon) {
         if (!title.equals(getString(R.string.epg)))
             getActionBar().setTitle(title);
         getActionBar().setIcon(icon);
         mContent = fragment;
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
     }
 
     @SuppressWarnings("SameParameterValue")
     public void switchContent(Fragment fragment, String title, int icon, boolean addToBackStack) {
         if (addToBackStack) {
             Log.d(TAG, "switchContent addToBackStack");
             getActionBar().setTitle(title);
             getActionBar().setIcon(icon);
             mContent = fragment;
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.content_frame, fragment)
                     .addToBackStack(null)
                     .commit();
         } else {
             switchContent(fragment, title, icon);
         }
     }
 
     private class DVBMenuItem {
         public String tag;
         public int iconRes;
 
         public DVBMenuItem(String tag, int iconRes) {
             this.tag = tag;
             this.iconRes = iconRes;
         }
     }
 
     private class MenuAdapter extends ArrayAdapter<DVBMenuItem> {
 
         public MenuAdapter(Context context) {
             super(context, 0);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = LayoutInflater.from(getContext()).inflate(
                         R.layout.menu_list_item, null);
             }
 
             TextView title = (TextView) convertView
                     .findViewById(R.id.row_title);
             title.setText(getItem(position).tag);
             title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getItem(position).iconRes), null, null, null);
             title.setCompoundDrawablePadding(30);
 
             return convertView;
         }
 
     }
 }
