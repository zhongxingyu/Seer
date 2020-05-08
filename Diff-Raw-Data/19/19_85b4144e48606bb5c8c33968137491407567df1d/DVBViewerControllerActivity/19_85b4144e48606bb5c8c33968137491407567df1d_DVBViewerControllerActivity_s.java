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
 import android.view.*;
 import android.widget.*;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import com.androidquery.util.XmlDom;
 import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
 import de.bennir.DVBViewerController.channels.DVBChannel;
 import de.bennir.DVBViewerController.channels.DVBChannelAdapter;
 import de.bennir.DVBViewerController.epg.EPGInfo;
 import de.bennir.DVBViewerController.timers.DVBTimer;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DVBViewerControllerActivity extends FragmentActivity {
     private static final String TAG = DVBViewerControllerActivity.class.toString();
     private static final String OPENED_KEY = "OPENED_KEY";
     public static String dvbHost = "";
     public static String dvbIp = "";
     public static String dvbPort = "";
     public static String recIp = "";
     public static String recPort = "";
     public static ArrayList<ArrayList<DVBChannel>> DVBChannels = new ArrayList<ArrayList<DVBChannel>>();
     public static ArrayList<String> groupNames = new ArrayList<String>();
     public static ArrayList<String> chanNames = new ArrayList<String>();
     public static ArrayList<DVBTimer> DVBTimers = new ArrayList<DVBTimer>();
     public static int currentGroup = -1;
     public static int currentEPGItem = -1;
     public AQuery aq;
     public Typeface robotoThin;
     public Typeface robotoLight;
     public Typeface robotoCondensed;
     public Fragment mContent;
     private ListView mDrawerList;
     private DrawerLayout mDrawerLayout;
     private LinearLayout mDrawer;
     private String mTitle;
     private ActionBarDrawerToggle mDrawerToggle;
     private SharedPreferences prefs = null;
     private Boolean opened = null;
 
     public static DVBChannel getChannelByName(String name) {
         DVBChannel ret = null;
 
         for (ArrayList<DVBChannel> chans : DVBChannels) {
             for (DVBChannel chan : chans) {
                 if (chan.name.toLowerCase().equals(name.toLowerCase())) {
                     ret = chan;
                     break;
                 }
             }
         }
 
         return ret;
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public static void downloadTimerCallback(String url, XmlDom xml, AjaxStatus ajax) {
         Log.d(TAG, "downloadTimerCallback");
 
         List<XmlDom> entries = xml.tags("Timer");
         DVBTimer timer;
 
         for (XmlDom entry : entries) {
             Log.d(TAG, "XmlDom entry: " + entry.text("Descr"));
 
             timer = new DVBTimer();
             timer.id = entry.text("ID");
             timer.name = entry.text("Descr");
             timer.channelId = entry.child("Channel").attr("ID");
             timer.enabled = !entry.attr("Enabled").equals("0");
             timer.date = entry.attr("Date");
             timer.start = entry.attr("Start");
             timer.duration = entry.attr("Dur");
             timer.end = entry.attr("End");
 
             DVBTimers.add(timer);
         }
         Crouton.cancelAllCroutons();
         TimerFragment.addTimersToListView();
     }
 
     private static void clearChannelLists() {
         if (ChannelFragment.lvAdapter != null) {
             ChannelFragment.lvAdapter.clear();
             ChannelFragment.lvAdapter.notifyDataSetChanged();
         }
 
         if (ChannelGroupFragment.lvAdapter != null) {
             ChannelGroupFragment.lvAdapter.clear();
             ChannelGroupFragment.lvAdapter.notifyDataSetChanged();
         }
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
         Log.d(TAG, "downloadChannelCallback");
         clearChannelLists();
 
         ArrayList<DVBChannel> dvbChans = new ArrayList<DVBChannel>();
 
         try {
             if (json != null) {
                 Log.d(TAG, "Received answer");
                 JSONArray channelsJSON = new JSONArray(
                         json.getString("channels"));
 
                 String currentGroup = "";
 
                 for (int i = 0; i < channelsJSON.length(); i++) {
                     JSONObject chan = channelsJSON.getJSONObject(i);
 
                     DVBChannel dvbChannel = new DVBChannel();
                     dvbChannel.name = chan.getString("name");
                     dvbChannel.favoriteId = chan.getString("id");
                     dvbChannel.channelId = chan.getString("channelid");
                     dvbChannel.epgInfo.title = URLDecoder.decode(chan.getString("epgtitle"));
                     dvbChannel.epgInfo.time = chan.getString("epgtime");
                     dvbChannel.epgInfo.duration = chan.getString("epgduration");
 
                     String group = chan.getString("group");
                     if (!group.equals(currentGroup)) {
                         if (i > 0) {
                             DVBViewerControllerActivity.DVBChannels.add(dvbChans);
                             dvbChans = new ArrayList<DVBChannel>();
                         }
                         DVBViewerControllerActivity.groupNames.add(group);
                         currentGroup = group;
                     }
                     chanNames.add(dvbChannel.name);
                     dvbChans.add(dvbChannel);
                 }
                 DVBViewerControllerActivity.DVBChannels.add(dvbChans);
 
                 ChannelFragment.lvAdapter = new ChanGroupAdapter(this, DVBViewerControllerActivity.groupNames);
 
                 try {
                     ArrayList<DVBChannel> chans = DVBViewerControllerActivity.DVBChannels.get(DVBViewerControllerActivity.currentGroup);
                     ChannelGroupFragment.lvAdapter = new DVBChannelAdapter(this, chans);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
 
                 ChannelFragment.addChannelsToListView();
                 ChannelGroupFragment.addChannelsToListView();
             }
         } catch (JSONException e) {
             e.printStackTrace();
             System.out.println(e.toString());
         } finally {
             Crouton.cancelAllCroutons();
         }
     }
 
     @Override
     protected void onDestroy() {
         // Workaround until there's a way to detach the Activity from Crouton while
         // there are still some in the Queue.
         Crouton.clearCroutonsForActivity(this);
         DVBChannels.clear();
         DVBTimers.clear();
         groupNames.clear();
         chanNames.clear();
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
 
         aq = new AQuery(this);
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             dvbHost = extras.getString("dvbHost");
             dvbIp = extras.getString("dvbIp");
             dvbPort = extras.getString("dvbPort");
         }
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         mTitle = getString(R.string.remote);
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
 
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,
                 mDrawerLayout,
                 R.drawable.ic_drawer,
                 R.string.open_drawer,
                 R.string.close_drawer
         ) {
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 if(mTitle.equals(getString(R.string.epg))) {
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
                 if(mTitle.equals(getString(R.string.epg))) {
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
         if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             activeProfile.setText(dvbHost);
         }
         activeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent mIntent = new Intent(getApplicationContext(), DeviceSelectionActivity.class);
                 startActivity(mIntent);
 
                 dvbHost = "";
                 dvbIp = "";
                 dvbPort = "";
                 recIp = "";
                 recPort = "";
 
                 DVBChannels.clear();
                 groupNames.clear();
                 DVBTimers.clear();
                 chanNames.clear();
 
                 DVBViewerControllerActivity.this.finish();
                 overridePendingTransition(R.anim.fadein, R.anim.slide_to_right);
             }
         });
 
         /**
          * Menu Items
          */
         MenuAdapter adapter = new MenuAdapter(this);
 
         adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote));
         adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels));
         adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg));
         adapter.add(new DVBMenuItem(getString(R.string.timer), R.drawable.ic_action_timers));
         adapter.add(new DVBMenuItem(getString(R.string.settings), R.drawable.ic_action_settings));
 
         mDrawerList.setAdapter(adapter);
 
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
 
         /**
          * Recording Service Loading
          */
         if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             if (DVBViewerControllerActivity.recIp.equals("") || DVBViewerControllerActivity.recPort.equals("")) {
                 Log.d(TAG, "Getting Recording Service");
 
                 String url = "http://" +
                         DVBViewerControllerActivity.dvbIp + ":" +
                         DVBViewerControllerActivity.dvbPort +
                         "/?getRecordingService";
                 Log.d(TAG, "URL=" + url);
                 aq.ajax(url, JSONObject.class, this, "getRecordingServiceCallback");
             }
         }
 
         /**
          * Channel Loading
          */
         if (DVBChannels.isEmpty()) {
             Log.d(TAG, "DVBChannels empty");
             updateChannelList();
         }
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
 
     @SuppressWarnings("UnusedDeclaration")
     public void getRecordingServiceCallback(String url, JSONObject json, AjaxStatus ajax) {
         try {
             if (json != null) {
                 JSONObject recordingService = json.getJSONObject("recordingService");
 
                 DVBViewerControllerActivity.recIp = recordingService.getString("ip");
                 DVBViewerControllerActivity.recPort = recordingService.getString("port");
 
                 Log.d(TAG, "RecordingService: " + DVBViewerControllerActivity.recIp + ":" + DVBViewerControllerActivity.recPort);
             }
         } catch (JSONException e) {
             Crouton.makeText(this, R.string.recservicefailed, Style.ALERT).show();
 
             e.printStackTrace();
         }
     }
 
     public void updateTimers() {
         Log.d(TAG, "updating channels");
         DVBViewerControllerActivity.DVBTimers.clear();
 
         if (DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             DVBTimer timer;
             for (int i = 1; i <= 5; i++) {
                 timer = new DVBTimer();
                 timer.id = "Demo" + i;
                 timer.name = "Timer " + i;
                 timer.date = "11.11.2011";
                 timer.enabled = i % 2 == 0;
                 timer.start = "20:15";
                 timer.end = "22:00";
                 timer.channelId = "|" + timer.name;
                 DVBViewerControllerActivity.DVBTimers.add(timer);
             }
 
             TimerFragment.addTimersToListView();
         } else {
             Style st = new Style.Builder()
                    // TODO: duration infinite
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(this, getString(R.string.loadingTimers), st).show();
 
             String url = "http://";
             url += recIp + ":" + recPort;
             url += "/api/timerlist.html?utf8=";
             aq.ajax(url, XmlDom.class, this, "downloadTimerCallback");
         }
 
     }
 
     public void updateChannelList() {
         Log.d(TAG, "updating channels");
         DVBViewerControllerActivity.groupNames.clear();
         DVBViewerControllerActivity.DVBChannels.clear();
 
         if (DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             clearChannelLists();
 
             DVBViewerControllerActivity.groupNames.add("ARD");
             ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();
 
             DVBChannel test = new DVBChannel();
             test.name = "Das Erste HD";
             EPGInfo epg = new EPGInfo();
             epg.channel = test.name;
             epg.desc = "Nachrichten";
             epg.time = "20:15";
             epg.title = "Nachrichten";
             test.epgInfo = epg;
             testChans.add(test);
             chanNames.add(test.name);
             for (int i = 0; i < 10; i++) {
                 test = new DVBChannel();
                 test.name = "NDR HD";
                 epg = new EPGInfo();
                 epg.channel = test.name;
                 epg.desc = "Nachrichten";
                 epg.time = "20:15";
                 epg.title = "Nachrichten";
                 test.epgInfo = epg;
                 testChans.add(test);
                 chanNames.add(test.name);
             }
             DVBViewerControllerActivity.DVBChannels.add(testChans);
 
             DVBViewerControllerActivity.groupNames.add("ZDF");
             testChans = new ArrayList<DVBChannel>();
 
             test = new DVBChannel();
             test.name = "ZDF HD";
             epg = new EPGInfo();
             epg.channel = test.name;
             epg.desc = "Nachrichten";
             epg.time = "20:15";
             epg.title = "Nachrichten";
             test.epgInfo = epg;
             testChans.add(test);
             chanNames.add(test.name);
             for (int i = 0; i < 10; i++) {
                 test = new DVBChannel();
                 test.name = "ZDF Kultur";
                 epg = new EPGInfo();
                 epg.channel = test.name;
                 epg.desc = "Nachrichten";
                 epg.time = "20:15";
                 epg.title = "Nachrichten";
                 test.epgInfo = epg;
                 testChans.add(test);
                 chanNames.add(test.name);
             }
             DVBViewerControllerActivity.DVBChannels.add(testChans);
 
 
             ChannelFragment.lvAdapter = new ChanGroupAdapter(this, DVBViewerControllerActivity.groupNames);
         } else {
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "/?getFavList";
             Log.d(TAG, "URL=" + url);
 
             Style st = new Style.Builder()
                    // TODO: duration infinite
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(this, R.string.loadingChannels, st).show();
 
             aq.ajax(url, JSONObject.class, this, "downloadChannelCallback");
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == 1)
             updateTimers();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         Log.d(TAG, "onSaveInstanceState");
         super.onSaveInstanceState(outState);
 
         getSupportFragmentManager().putFragment(outState, "mContent", mContent);
     }
 
     public void switchContent(Fragment fragment, int titleRes, int icon) {
         switchContent(fragment, getString(titleRes), icon);
     }
 
     public void switchContent(Fragment fragment, String title, int icon) {
         if(!title.equals(getString(R.string.epg)))
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
 
     public class MenuAdapter extends ArrayAdapter<DVBMenuItem> {
 
         public MenuAdapter(Context context) {
             super(context, 0);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = LayoutInflater.from(getContext()).inflate(
                         R.layout.menu_list_item, null);
             }
 
             CheckedTextView title = (CheckedTextView) convertView
                     .findViewById(R.id.row_title);
             title.setText(getItem(position).tag);
             title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getItem(position).iconRes), null, null, null);
             title.setCompoundDrawablePadding(30);
 
             return convertView;
         }
 
     }
 }
