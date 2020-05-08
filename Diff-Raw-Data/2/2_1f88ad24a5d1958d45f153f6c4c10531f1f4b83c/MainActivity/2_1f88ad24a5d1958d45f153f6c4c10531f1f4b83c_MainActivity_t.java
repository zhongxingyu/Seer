 package com.bruinlyfe.bruinlyfe;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class MainActivity extends FragmentActivity {
     MenuLoader menuLoader;
     SharedPreferences prefs = null;
     MenuItem menuItem = null;
     final String hoursDeliminator = "\n";
 
     public DiningHall bcafe = new DiningHall("bcafe", R.id.timeViewBcafeBreakfast, R.id.timeViewBcafeLunch, R.id.timeViewBcafeDinner, R.id.timeViewBcafeLateNight);
     public DiningHall bplate = new DiningHall("bplate", R.id.timeViewBplateBreakfast, R.id.timeViewBplateLunch, R.id.timeViewBplateDinner, R.id.timeViewBcafeLateNight);
     public DiningHall nineteen = new DiningHall("nineteen", R.id.timeViewCafe1919Breakfast, R.id.timeViewCafe1919Lunch, R.id.timeViewCafe1919Dinner, R.id.timeViewCafe1919LateNight);
     public DiningHall covel = new DiningHall("covel", R.id.timeViewCovelBreakfast, R.id.timeViewCovelLunch, R.id.timeViewCovelDinner, R.id.timeViewCovelLateNight);
     public DiningHall deneve = new DiningHall("deneve", R.id.timeViewDeneveBreakfast, R.id.timeViewDeneveLunch, R.id.timeViewDeneveDinner, R.id.timeViewDeneveLateNight);
     public DiningHall feast = new DiningHall("feast", R.id.timeViewFeastBreakfast, R.id.timeViewFeastLunch, R.id.timeViewFeastDinner, R.id.timeViewFeastLateNight);
     public DiningHall hedrick = new DiningHall("hedrick", R.id.timeViewHedrickBreakfast, R.id.timeViewHedrickLunch, R.id.timeViewHedrickDinner, R.id.timeViewHedrickLateNight);
     public DiningHall rende = new DiningHall("rende", R.id.timeViewRenBreakfast, R.id.timeViewRenLunch, R.id.timeViewRenDinner, R.id.timeViewRenLateNight);
 
     List<DiningHall> halls = new ArrayList<DiningHall>();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.frag_dining_hours);
 
         Log.w("BruinLyfe", "!!!!!!!!!!!!Starting Application!!!!!!!!!!!!");
 
         //Build halls list
         halls.add(bcafe);
         halls.add(bplate);
         halls.add(nineteen);
         halls.add(covel);
         halls.add(deneve);
         halls.add(feast);
         halls.add(hedrick);
         halls.add(rende);
 
         prefs = getSharedPreferences("com.bruinlyfe.bruinlyfe", MODE_PRIVATE);
 
         menuLoader = new MenuLoader(halls);
         menuLoader.mainActivity = this;
 
         //Information is loaded when the options menu is created so that the progressbar
         //can appear in the menu!
 
         //Check if first run, and if so, then load the tutorial
         if(prefs.getBoolean("firstRun", true)) {
             Intent intent = new Intent(getApplicationContext(), TutorialActivity.class);
             startActivity(intent);
         }
     }
 
     //Dear Future Me or maintainer,
     //I apologize in advance for making this function start the useful part of this app.
     //But I had to in order to make the progress bar spin upon startup with having a NPE.
     //I could do it better in Qt or C++, but I don't know Java that well, yet.
     //Thanks for keeping the code up to date!
     //Past Me
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main_menu, menu);
 
         menuItem = menu.findItem(R.id.action_refresh);
         startProgressBar();
         loadInfo(false);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         switch (item.getItemId()) {
             case R.id.action_about:
                 Intent intentAbout = new Intent(getApplicationContext(), AboutActivity.class);
                 startActivity(intentAbout);
                 return true;
             case R.id.action_swipe_calculator:
                 Intent intentSwipeCalculator = new Intent(getApplicationContext(), SwipeCalculatorActivity.class);
                 startActivity(intentSwipeCalculator);
                 return true;
             case R.id.action_refresh:
                 loadInfo(true);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         //Force a menu update, which is basically the starting point of this app
         invalidateOptionsMenu();
     }
 
 
     public void loadInfo(boolean forceRefresh) {
         //Use cache if the cache is recent
         if(forceRefresh == false && prefs.getInt("dayCacheTime", -1) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                 prefs.getInt("dayCacheMenu", -1) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
             Log.w("BruinLyfe", "!!!!!!!!!!!!LOADING DATA FROM CACHE!!!!!!!!!!!!");
             stopProgressBar();
 
             //Load the dining hall hours
             String listOfHoursAsString = prefs.getString("hours", "default");
             if(listOfHoursAsString.equals("default")) {
                 loadInfo(true); //force a refresh
             }
             String[] items = listOfHoursAsString.split(hoursDeliminator);
             List<String> list = new ArrayList<String>();
             for(int i=0; i < items.length; i++){
                 list.add(items[i]);
             }
             fillDiningHours(list);
 
             //Load the menu data
             String menuData = prefs.getString("menuData", "default");
             if(menuData.equals("default")) {
                 loadInfo(true);
             }
             menuLoader.parseData(prefs.getString("menuData", ""));
 
             Log.w("BruinLyfe", "!!!!!!!!!!!!LOADED DATA FROM CACHE!!!!!!!!!!!!");
         }
         else {
             if(isNetworkAvailable()) {
                 Toast toast = Toast.makeText(getApplicationContext(), "Downloading Data...", Toast.LENGTH_SHORT);
                 toast.show();
                 //Load the hours data
                 DownloadTask dTask;
                 dTask = new DownloadTask();
                 dTask.myMainActivity = this;
                 dTask.message = "!!!!!!!!!!!!DOWNLOADING HOURS DATA!!!!!!!!!!!!";
                 startProgressBar();
                 dTask.execute("http://secure5.ha.ucla.edu/restauranthours/dining-hall-hours-by-day.cfm");
 
                 //Load the menu data
                 menuLoader.downloadMenuData();
             }
             else {
                Toast toast = Toast.makeText(getApplicationContext(), "No internet connection!", Toast.LENGTH_SHORT);
                 toast.show();
                 stopProgressBar();
             }
         }
     }
 
     public void startProgressBar() {
         try {
             menuItem.setActionView(R.layout.progressbar);
             menuItem.expandActionView();
             TimeView[] timeViews = initTimeViewList();
             for(int i=0;i<timeViews.length;i++) {
                 timeViews[i].setOpenTime("LOADING");
                 timeViews[i].setCloseTime("LOADING");
             }
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void stopProgressBar() {
         try {
             menuItem.collapseActionView();
             menuItem.setActionView(null);
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void fillDiningHours(List<String> stringList) {
         //Display the dining hall hours
         TimeView[] timeViews = initTimeViewList();
 
         int j = 1;
         for(int i=0;i<timeViews.length;i++)
         {
             if(stringList.size() <= j) {
                 loadInfo(true);
             }
             if(stringList.size() > j) {
                 timeViews[i].setOpenTime(stringList.get(j-1));
                 timeViews[i].setCloseTime(stringList.get(j));
                 //check to see if current time is in this time frame,
                 //and if so, color the text green
                 if(!stringList.get(j-1).contains("CLOSED") && !stringList.get(j).contains("CLOSED")) {
                     try {
                         Calendar currentDate = Calendar.getInstance();
                         SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma");
                         Calendar openDate = Calendar.getInstance();
                         openDate.setTime(dateFormat.parse(stringList.get(j-1), new ParsePosition(0)));
                         openDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
                         openDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
                         openDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH));
                         Calendar closeDate = Calendar.getInstance();
                         closeDate.setTime(dateFormat.parse(stringList.get(j), new ParsePosition(0)));
                         closeDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
                         closeDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH));
                         closeDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH));
                         if(closeDate.get(Calendar.HOUR_OF_DAY) < 5 && currentDate.get(Calendar.HOUR_OF_DAY) >= 5) {  //if less than 5am, then it is really the next day in the early morning, i.e. 2am
                             closeDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH) + 1);
                             //Log.w("BruinLyfe", "HAD TO ADD ONE DAY TO THE CLOSE TIME");
                         }
 
                         //If the openDate was really the day before
                         if(openDate.compareTo(closeDate) == 1) {    //if open date is after close date (i.e. late night)
                             openDate.set(Calendar.DAY_OF_MONTH, openDate.get(Calendar.DAY_OF_MONTH) - 1);
                         }
 
                         //Log.w("BruinLyfe", "-------------------------------------");
                         //Log.w("BruinLyfe", "OPEN DATE: " + openDate.get(Calendar.DAY_OF_MONTH) + '\t' + openDate.get(Calendar.HOUR_OF_DAY));
                         //Log.w("BruinLyfe", "CURRENT DATE: " + currentDate.get(Calendar.DAY_OF_MONTH) + '\t' + currentDate.get(Calendar.HOUR_OF_DAY));
                         //Log.w("BruinLyfe", "CLOSE DATE: " + closeDate.get(Calendar.DAY_OF_MONTH) + '\t' + closeDate.get(Calendar.HOUR_OF_DAY));
                         //If current time is in range [openTime, closeTime]
                         if(currentDate.compareTo(openDate) != -1 && currentDate.compareTo(closeDate) != 1)
                             timeViews[i].setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                     } catch(Exception e) {
                         e.printStackTrace();
                     }
                 }
                 j = j+2;
             }
         }
         stopProgressBar();
     }
 
     public List<String> findMatches(String htmlSource) {
 
         //Create the string array
         Pattern p = Pattern.compile("<strong>\\s*.*\\s*</strong>");
         Matcher m = p.matcher(htmlSource);
         List<String> matches = new ArrayList<String>();
         while(m.find()){
             matches.add(m.group());
         }
 
         //Remove the HTML tags
         for(int i=0;i<matches.size();i++) {
             matches.set(i, matches.get(i).replace("<strong>", ""));
             matches.set(i, matches.get(i).replace("</strong>", ""));
             matches.set(i, matches.get(i).replace(" -", ""));
         }
 
         List<String> cleanMatches = new ArrayList<String>();
         Pattern pClean = Pattern.compile("CLOSED|\\d*:\\d*\\w\\w");
 
         for(int i=0;i<matches.size();i++) {
             Matcher mClean = pClean.matcher(matches.get(i));
             while(mClean.find()) {
                 cleanMatches.add(mClean.group());
                 int index = cleanMatches.size() - 1;
                 if(cleanMatches.get(index).contains("CLOSED")) {
                     cleanMatches.add(mClean.group());
                 }
             }
         }
 
         return cleanMatches;
     }
 
     public void cacheHoursData(List<String> stringList) {
         //Cache the dining hall hours
         StringBuilder listAsString = new StringBuilder();
         for(String s : stringList){
             listAsString.append(s);
             listAsString.append(hoursDeliminator);
         }
         prefs.edit().putString("hours", listAsString.toString()).commit();
         prefs.edit().putInt("dayCacheTime", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).commit();    //record when the cache was created
     }
 
     public void cacheMenuData(String rawJsonResult) {
         prefs.edit().putString("menuData", rawJsonResult).commit();
         prefs.edit().putInt("dayCacheMenu", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).commit();    //record when the cache was created
     }
 
     private TimeView[] initTimeViewList() {
         TimeView[] timeViews;
         timeViews = new TimeView[32];
         timeViews[0] = (TimeView)findViewById(R.id.timeViewBcafeBreakfast);
         timeViews[1] = (TimeView)findViewById(R.id.timeViewBcafeLunch);
         timeViews[2] = (TimeView)findViewById(R.id.timeViewBcafeDinner);
         timeViews[3] = (TimeView)findViewById(R.id.timeViewBcafeLateNight);
 
         timeViews[4] = (TimeView)findViewById(R.id.timeViewBplateBreakfast);
         timeViews[5] = (TimeView)findViewById(R.id.timeViewBplateLunch);
         timeViews[6] = (TimeView)findViewById(R.id.timeViewBplateDinner);
         timeViews[7] = (TimeView)findViewById(R.id.timeViewBplateLateNight);
 
         timeViews[8] = (TimeView)findViewById(R.id.timeViewCafe1919Breakfast);
         timeViews[9] = (TimeView)findViewById(R.id.timeViewCafe1919Lunch);
         timeViews[10] = (TimeView)findViewById(R.id.timeViewCafe1919Dinner);
         timeViews[11] = (TimeView)findViewById(R.id.timeViewCafe1919LateNight);
 
         timeViews[12] = (TimeView)findViewById(R.id.timeViewCovelBreakfast);
         timeViews[13] = (TimeView)findViewById(R.id.timeViewCovelLunch);
         timeViews[14] = (TimeView)findViewById(R.id.timeViewCovelDinner);
         timeViews[15] = (TimeView)findViewById(R.id.timeViewCovelLateNight);
 
         timeViews[16] = (TimeView)findViewById(R.id.timeViewDeneveBreakfast);
         timeViews[17] = (TimeView)findViewById(R.id.timeViewDeneveLunch);
         timeViews[18] = (TimeView)findViewById(R.id.timeViewDeneveDinner);
         timeViews[19] = (TimeView)findViewById(R.id.timeViewDeneveLateNight);
 
         timeViews[20] = (TimeView)findViewById(R.id.timeViewFeastBreakfast);
         timeViews[21] = (TimeView)findViewById(R.id.timeViewFeastLunch);
         timeViews[22] = (TimeView)findViewById(R.id.timeViewFeastDinner);
         timeViews[23] = (TimeView)findViewById(R.id.timeViewFeastLateNight);
 
         timeViews[24] = (TimeView)findViewById(R.id.timeViewHedrickBreakfast);
         timeViews[25] = (TimeView)findViewById(R.id.timeViewHedrickLunch);
         timeViews[26] = (TimeView)findViewById(R.id.timeViewHedrickDinner);
         timeViews[27] = (TimeView)findViewById(R.id.timeViewHedrickLateNight);
 
         timeViews[28] = (TimeView)findViewById(R.id.timeViewRenBreakfast);
         timeViews[29] = (TimeView)findViewById(R.id.timeViewRenLunch);
         timeViews[30] = (TimeView)findViewById(R.id.timeViewRenDinner);
         timeViews[31] = (TimeView)findViewById(R.id.timeViewRenLateNight);
 
         return timeViews;
     }
 
     //Check if there is an internet connection
     //http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
     private boolean isNetworkAvailable() {
         ConnectivityManager connectivityManager
                 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
     }
 }
