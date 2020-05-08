 package com.mhacks.dealradar;
 
 import android.app.ActionBar;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Typeface;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.mhacks.dealradar.objects.Advertisement;
 import com.mhacks.dealradar.support.CustomAdapter;
 import com.mhacks.dealradar.support.DealAdapter;
 import com.mhacks.dealradar.support.WifiReceiver;
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 public class DealRadar extends Activity
 {
     public static ArrayList<Advertisement> advertisements;
     public static Typeface myriadProRegular, myriadProSemiBold;
     public static ListView dealList;
     public static ListView drawerList;
     public static ArrayList<String> listOfCategories =
             new ArrayList<String>(Arrays.asList("All", "Food", "Clothing", "Games", "Movies", "Pets", "Tech", "Toys"));
 
     ParseHandler handler;
     WifiManager mainWifi;
     WifiReceiver receiverWifi;
     ProgressDialog progressDialog;
     DrawerLayout drawerLayout;
     public static EditText searchBar;
     public static TextView noEvents;
     ImageButton drawerButton, settingsButton;
     ActionBarDrawerToggle drawerToggle;
     wifiscan scanThread;
     Context context;
     public static boolean firstLoad = true;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
     {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
         this.context = this;
         handler = new ParseHandler();
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         Parse.initialize(this,  Constants.PARSE_APPLICATION_ID, Constants.PARSE_CLIENT_KEY);
         ParseAnalytics.trackAppOpened(getIntent());
         progressDialog = new ProgressDialog(this);
         progressDialog.setTitle("");
         progressDialog.setCancelable(false);
         progressDialog.setMessage("Loading Deals...");
         ActionBar actionBar = getActionBar();
         actionBar.setCustomView(R.layout.action_bar);
         actionBar.setDisplayShowHomeEnabled(false);
         actionBar.setDisplayShowCustomEnabled(true);
         myriadProRegular = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Regular.otf");
         myriadProSemiBold = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Semibold.otf");
         TextView txtTitle = (TextView) findViewById(R.id.action_bar_title);
         noEvents = (TextView) findViewById(R.id.no_deals);
         noEvents.setTypeface(myriadProSemiBold);
         txtTitle.setTypeface(myriadProSemiBold);
 
         dealList = (ListView) findViewById(R.id.deal_list_view);
         drawerList = (ListView) findViewById(R.id.left_drawer);
         drawerList.setAdapter(new CustomAdapter(this, listOfCategories));
         drawerList.setOnItemClickListener(new DrawerItemClickListener());
         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.nav_button, R.string.app_name, R.string.app_name);
         drawerLayout.setDrawerListener(drawerToggle);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         DrawerItemClickListener drawerListener = new DrawerItemClickListener();
         drawerList.setOnItemClickListener(drawerListener);
         drawerButton = (ImageButton) findViewById(R.id.toggle_button);
         drawerButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (drawerLayout.isDrawerOpen(drawerList)) {
                     drawerLayout.closeDrawer(drawerList);
                 } else {
                     drawerLayout.openDrawer(drawerList);
                 }
             }
         });
 
 
 
         searchBar = (EditText) findViewById(R.id.search_bar);
         searchBar.setTypeface(myriadProRegular);
         searchBar.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence cs, int i, int i2, int i3) {}
 
             @Override
             public void onTextChanged(CharSequence cs, int i, int i2, int i3)
             {
                 if(!searchBar.getText().toString().isEmpty() && receiverWifi != null)
                 {
                     WifiReceiver.setInterrupts(true);
                     String search = searchBar.getText().toString().toUpperCase();
                     ArrayList<Advertisement> searchResults = new ArrayList<Advertisement>();
 
                     for(Advertisement ad : receiverWifi.matches)
                     {
                         if(ad.company.toUpperCase().contains(search) || ad.title.toUpperCase().contains(search))
                         {
                             searchResults.add(ad);
                         }
                     }
                     receiverWifi.adapter.setContent(searchResults);
                     receiverWifi.adapter.notifyDataSetChanged();
                 }
                 else
                 {
                     if(receiverWifi != null)
                     {
                         WifiReceiver.setInterrupts(false);
                         receiverWifi.adapter.setContent(receiverWifi.matches);
                         receiverWifi.adapter.notifyDataSetChanged();
                     }
                 }
             }
 
             @Override
             public void afterTextChanged(Editable editable)
             {
             }
         });
 
         settingsButton = (ImageButton) findViewById(R.id.settings_button);
         settingsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 Intent intent = new Intent(context, Settings.class);
                 intent.putExtra("HANDLER", new Messenger(handler));
                 if(intent != null)
                 {
                     startActivity(intent);
                 }
             }
         });
     }
 
     public void onResume()
     {
         super.onResume();
 
         if(mainWifi == null)
         {
             mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
             receiverWifi = new WifiReceiver(mainWifi);
             registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
         }
 
         if(firstLoad)
         {
             progressDialog.show();
             findMatches();
         }
         else
         {
             scanThread = new wifiscan();
             scanThread.start();
         }
     }
 
     private Date fixDate(Date date)
     {
         TimeZone tz = TimeZone.getDefault();
         Date fixed = new Date(date.getTime() - tz.getRawOffset());
 
         if(tz.inDaylightTime(fixed))
         {
             Date dst = new Date(fixed.getTime() - tz.getDSTSavings());
 
             if(tz.inDaylightTime(dst))
             {
                 fixed = dst;
             }
         }
 
         return fixed;
     }
 
     public void findMatches()
     {
         Log.d("fatal", "Refreshing Parse...");
 
         advertisements = new ArrayList<Advertisement>();
         ParseQuery<ParseObject> query = ParseQuery.getQuery("Routers");
         query.findInBackground(new FindCallback<ParseObject>() {
             public void done(List<ParseObject> result, ParseException e) {
                 if (e == null) {
                     for (ParseObject parse : result) {
                         Advertisement tmp = new Advertisement();
                         tmp.objectId = parse.getObjectId();
                         tmp.title = parse.getString("Deal_Title");
                         tmp.category = parse.getString("Category");
                        tmp.rating = parse.getInt("Rating");
 
                         if (parse.getDate("Exp_Date") != null) {
                             tmp.expDate = fixDate(parse.getDate("Exp_Date"));
                         }
                         tmp.company = parse.getString("Company");
                         tmp.BSSID = parse.getString("BSSID");
 
                         ParseFile coupon = parse.getParseFile("Deal_Image");
                         if (coupon != null) {
                             tmp.image_url = coupon.getUrl();
                         }
 
                         advertisements.add(tmp);
                     }
                     if(progressDialog.isShowing())
                     {
                         progressDialog.dismiss();
                     }
                     scanThread = new wifiscan();
                     scanThread.start();
                 } else {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     private class wifiscan extends Thread
     {
         boolean running = true;
 
         public void setRunning(boolean running)
         {
             this.running = running;
         }
 
         public void run()
         {
             while(running)
             {
                 mainWifi.startScan();
                 try
                 {
                     Thread.sleep(Constants.ASYNC_SCAN_TICK * 1000);
                 }
                 catch(Exception e){}
             }
         }
     }
 
     public void selectItem(int position)
     {
         if(receiverWifi != null)
         {
             String filter = "All";
             switch(position)
             {
                 case 0: //All
                     break;
                 case 1: //Food
                     filter = "Food";
                     break;
                 case 2: //Clothing
                     filter = "Clothing";
                     break;
                 case 3: //Games
                     filter = "Games";
                     break;
                 case 4: //Movies
                     filter = "Movies";
                     break;
                 case 5: //Pets
                     filter = "Pets";
                     break;
                 case 6: //Tech
                     filter = "Tech";
                     break;
                 case 7: //Toys
                     filter = "Toys";
                     break;
             }
 
             drawerLayout.closeDrawer(drawerList);
             receiverWifi.setCurrentFilter(filter);
         }
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id)
         {
             selectItem(position);
         }
     }
 
     private class ParseHandler extends Handler
     {
         public void handleMessage(Message msg)
         {
             findMatches();
         }
     }
 
 }
 
 
 
 
 
 
