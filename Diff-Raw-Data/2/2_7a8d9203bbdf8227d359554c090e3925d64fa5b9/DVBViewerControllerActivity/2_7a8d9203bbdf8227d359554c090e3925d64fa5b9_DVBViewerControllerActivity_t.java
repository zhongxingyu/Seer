 package de.bennir.DVBViewerController;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import com.androidquery.util.XmlDom;
 import com.slidingmenu.lib.SlidingMenu;
 import de.bennir.DVBViewerController.channels.ChanGroupAdapter;
 import de.bennir.DVBViewerController.channels.DVBChannel;
 import de.bennir.DVBViewerController.timers.DVBTimer;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DVBViewerControllerActivity extends SherlockFragmentActivity {
     private static final String TAG = DVBViewerControllerActivity.class.toString();
     public static String dvbHost = "";
     public static String dvbIp = "";
     public static String dvbPort = "";
     public static String recIp = "";
     public static String recPort = "";
     public static ArrayList<ArrayList<DVBChannel>> DVBChannels = new ArrayList<ArrayList<DVBChannel>>();
     public static ArrayList<String> groupNames = new ArrayList<String>();
     public static ArrayList<DVBTimer> DVBTimers = new ArrayList<DVBTimer>();
     public static int currentGroup = -1;
     public SlidingMenu menu;
     public AQuery aq;
     public Typeface robotoThin;
     public Typeface robotoLight;
     public Typeface robotoCondensed;
     public Fragment mContent;
 
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
 
     @SuppressWarnings("UnusedDeclaration")
     public void downloadChannelCallback(String url, JSONObject json, AjaxStatus ajax) {
         Log.d(TAG, "downloadChannelCallback");
 
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
                     dvbChans.add(dvbChannel);
                 }
                 DVBViewerControllerActivity.DVBChannels.add(dvbChans);
 
                 ChannelFragment.lvAdapter = new ChanGroupAdapter(
                         this,
                         DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
                 );
 
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
         groupNames.clear();
         super.onDestroy();
     }
 
     @Override
     public void onBackPressed() {
         if (menu.isMenuShowing()) {
             Log.d(TAG, "Back with menu");
             menu.showContent(true);
             return;
         }
 
         super.onBackPressed();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         getWindow().setBackgroundDrawable(null);
 
         initFonts();
 
         aq = new AQuery(this);
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             dvbHost = extras.getString("dvbHost");
             dvbIp = extras.getString("dvbIp");
             dvbPort = extras.getString("dvbPort");
         }
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setTitle(R.string.remote);
         getSupportActionBar().setIcon(R.drawable.ic_action_remote);
 
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
         menu = new SlidingMenu(this);
         menu.setMenu(R.layout.menu);
         TextView activeProfile = (TextView) menu.findViewById(R.id.active_profile);
        activeProfile.setTypeface(robotoCondensed);
         activeProfile.setText(dvbHost);
         activeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                 Intent mIntent = new Intent(getApplicationContext(), DeviceSelectionActivity.class);
                 startActivity(mIntent);
 
                 dvbHost = "";
                 dvbIp = "";
                 dvbPort = "";
                 recIp = "";
                 recPort = "";
 
                 DVBViewerControllerActivity.this.finish();
                 overridePendingTransition(R.anim.fadein, R.anim.slide_to_right);
             }
         });
 
         /**
          * Menu Items
          */
         MenuAdapter adapter = new MenuAdapter(this);
         ListView lvMenu = (ListView) menu.findViewById(R.id.menu_list);
 
         adapter.add(new DVBMenuItem(getString(R.string.remote), R.drawable.ic_action_remote_dark));
         adapter.add(new DVBMenuItem(getString(R.string.channels), R.drawable.ic_action_channels_dark));
         adapter.add(new DVBMenuItem(getString(R.string.epg), R.drawable.ic_action_epg_dark));
         adapter.add(new DVBMenuItem(getString(R.string.timer), R.drawable.ic_action_timers_dark));
         adapter.add(new DVBMenuItem(getString(R.string.settings), R.drawable.ic_action_settings_dark));
 
         lvMenu.setAdapter(adapter);
 
         lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
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
                     getSupportFragmentManager().popBackStackImmediate();
                     switchContent(newContent, titleRes, icon);
                 }
             }
         });
 
 
         /**
          * SlideMenu Customize
          */
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         menu.setShadowWidthRes(R.dimen.shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
         menu.setBehindScrollScale(0.5f);
         menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
 
         /**
          * Recording Service Loading
          */
         if (!DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             if (DVBViewerControllerActivity.recIp.isEmpty() || DVBViewerControllerActivity.recPort.isEmpty()) {
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
             updateChannelList();
         }
     }
 
     private void initFonts() {
         robotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
         robotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
         robotoCondensed = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Bold.ttf");
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
             for (int i = 1; i <= 10; i++) {
                 timer = new DVBTimer();
                 timer.id = Integer.toString(i);
                 timer.name = "Timer " + i;
                 DVBViewerControllerActivity.DVBTimers.add(timer);
             }
 
             TimerFragment.addTimersToListView();
         } else {
             Style st = new Style.Builder()
                     .setDuration(Style.DURATION_INFINITE)
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(this, getString(R.string.loadingTimers), st).show();
 
             String url = "http://192.168.2.1:8089/api/timerlist.html?utf8=";
             aq.ajax(url, XmlDom.class, this, "downloadTimerCallback");
         }
 
     }
 
     public void updateChannelList() {
         Log.d(TAG, "updating channels");
         DVBViewerControllerActivity.groupNames.clear();
         DVBViewerControllerActivity.DVBChannels.clear();
 
         if (DVBViewerControllerActivity.dvbHost.equals("Demo Device")) {
             DVBViewerControllerActivity.groupNames.add("ARD");
             ArrayList<DVBChannel> testChans = new ArrayList<DVBChannel>();
 
             DVBChannel test = new DVBChannel();
             test.name = "Das Erste HD";
             testChans.add(test);
             test = new DVBChannel();
             test.name = "NDR HD";
             testChans.add(test);
             DVBViewerControllerActivity.DVBChannels.add(testChans);
 
             DVBViewerControllerActivity.groupNames.add("ZDF");
             testChans = new ArrayList<DVBChannel>();
 
             test = new DVBChannel();
             test.name = "ZDF HD";
             testChans.add(test);
             test = new DVBChannel();
             test.name = "ZDF Kultur";
             testChans.add(test);
             DVBViewerControllerActivity.DVBChannels.add(testChans);
 
             ChannelFragment.lvAdapter = new ChanGroupAdapter(
                     this,
                     DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
             );
         } else {
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "/?getFavList";
             Log.d(TAG, "URL=" + url);
 
             Style st = new Style.Builder()
                     .setDuration(Style.DURATION_INFINITE)
                     .setBackgroundColorValue(Style.holoBlueLight)
                     .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                     .build();
             Crouton.makeText(this, R.string.loadingChannels, st).show();
 
             aq.ajax(url, JSONObject.class, this, "downloadChannelCallback");
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.d(TAG, "onOptionsItemSelected");
         switch (item.getItemId()) {
             case android.R.id.home:
                 menu.toggle();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         getSupportFragmentManager().putFragment(outState, "mContent", mContent);
     }
 
     public void switchContent(Fragment fragment, int titleRes, int icon) {
         getSupportActionBar().setTitle(titleRes);
         getSupportActionBar().setIcon(icon);
         mContent = fragment;
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
         menu.showContent();
     }
 
     public void switchContent(Fragment fragment, int titleRes, int icon, boolean addToBackStack) {
         if (addToBackStack) {
             getSupportActionBar().setTitle(titleRes);
             getSupportActionBar().setIcon(icon);
             mContent = fragment;
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.content_frame, fragment)
                     .addToBackStack(null)
                     .commit();
             menu.showContent();
         } else {
             switchContent(fragment, titleRes, icon);
         }
     }
 
     public void switchContent(Fragment fragment, String title, int icon) {
         getSupportActionBar().setTitle(title);
         getSupportActionBar().setIcon(icon);
         mContent = fragment;
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
         menu.showContent();
     }
 
     @SuppressWarnings("SameParameterValue")
     public void switchContent(Fragment fragment, String title, int icon, boolean addToBackStack) {
         if (addToBackStack) {
             Log.d(TAG, "switchContent addToBackStack");
             getSupportActionBar().setTitle(title);
             getSupportActionBar().setIcon(icon);
             mContent = fragment;
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.content_frame, fragment)
                     .addToBackStack(null)
                     .commit();
             menu.showContent();
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
                         R.layout.row, null);
             }
 
             TextView title = (TextView) convertView
                     .findViewById(R.id.row_title);
             title.setText(getItem(position).tag);
             Drawable img = getContext().getResources().getDrawable(getItem(position).iconRes);
             img.setBounds(0, 0, 50, 50);
             title.setCompoundDrawables(img, null, null, null);
 
             return convertView;
         }
 
     }
 }
