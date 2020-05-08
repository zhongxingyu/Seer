 package com.weatherornot.weatherornot;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 
 public class DisplayWeatherActivity extends Activity {
 
     ListView mListView;
     Typeface font;
     String fontPathB = "fonts/playtime.ttf";
     String fontPathC = "fonts/edo.ttf";
     String mCurrentTemp;
 
     private final String CLOUDY = "CLOUDY";
     private final String CLEAR_DAY = "CLEAR-DAY";
     private final String CLEAR_NIGHT = "CLEAR-NIGHT";
     private final String RAIN = "RAIN";
     private final String SNOW = "SNOW";
     private final String SLEET = "SLEET";
     private final String WIND = "WIND";
     private final String FOG = "FOG";
     private final String PARTLY_CLOUDY_DAY = "PARTLY-CLOUDY-DAY";
     private final String PARTLY_CLOUDY_NIGHT = "PARTLY-CLOUDY-NIGHT";
     private final String HAIL = "HAIL";
     private final String THUNDERSTORMS = "THUNDERSTORMS";
     private final String TORNADO = "TORNADO";
 
 //    static final int SET_SERVICES = 0;
 
 //    public static void setTheIcon(int theIcon) {
 //        DisplayWeatherActivity.theIcon = theIcon;
 //    }
 
   
     static final String PREFERENCES = "temps";
     ProgressDialog waiting;
 
 
     ///1
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 
         setContentView(R.layout.activity_main);
         mListView = (ListView) findViewById(R.id.hourly);
 
 
         //setting date from phone
         Date now = new Date();
         Date giveDate = Calendar.getInstance().getTime();
         String nowAsString = new SimpleDateFormat("EEEE,  LLLLL  dd,  yyyy").format(now);
 
 
         TextView dateview = (TextView) findViewById(R.id.date);
 
         dateview.setText(nowAsString);
         getWeather();
 
 
         TextView templabel = (TextView) findViewById(R.id.tempLabel);
         font = Typeface.createFromAsset(getAssets(), fontPathC);
         templabel.setTypeface(font);
 
         TextView hourlyscroll = (TextView) findViewById(R.id.Hourlyscroll);
         font = Typeface.createFromAsset(getAssets(), fontPathC);
         hourlyscroll.setTypeface(font);
 
         TextView snarky = (TextView) findViewById(R.id.snarky);
         font = Typeface.createFromAsset(getAssets(), fontPathB);
         snarky.setTypeface(font);
 
     }
 
 
     ////////////////////////////////////////////
     public void getWeather() {       //asking for the weather
         new GetWeatherDataTask(this);
 
         //getWeather();
         waiting = new ProgressDialog(this);
         waiting.setTitle("Getting Weather Data!");
         waiting.setMessage("Good things come to those who wait.");
         waiting.setIndeterminate(true);
         waiting.show();
 
     }
 
     /////////////////////////////////////////////////////
     public void receiveWeatherData(PantsWeatherData myDataObject) throws ParseException {
 
         TextView textView = (TextView) findViewById(R.id.currenttemp);
         waiting.dismiss();
 
         mCurrentTemp = myDataObject.getmCurrentTempString();
         String roundedDouble = "";
         roundedDouble = mCurrentTemp.substring(0, mCurrentTemp.indexOf('.'));
         textView.setText(roundedDouble + "\u00B0");
 
         Log.e("LOOK--------------------------------------value of mCurrentTemp", String.valueOf(mCurrentTemp.toString()));
 
 
 
 
         mListView = (ListView) findViewById(R.id.hourly);
         ListAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.row, myDataObject.getmHourlyData());
 
         mListView.setAdapter(adapter);
 
         String icon = myDataObject.getIcon();
 
         Drawable theIcon;
 
         if (icon.equalsIgnoreCase(CLOUDY)) {
             theIcon = getResources().getDrawable(R.drawable.cloudy);
         } else if (icon.equalsIgnoreCase(CLEAR_DAY)) {
             theIcon = getResources().getDrawable(R.drawable.sunstandin);
         } else if (icon.equalsIgnoreCase(CLEAR_NIGHT)) {
             theIcon = getResources().getDrawable(R.drawable.clearnight);
         } else if (icon.equalsIgnoreCase(RAIN)) {
             theIcon = getResources().getDrawable(R.drawable.rain);
         } else if (icon.equalsIgnoreCase(SNOW)) {
             theIcon = getResources().getDrawable(R.drawable.snow);
         } else if (icon.equalsIgnoreCase(SLEET)) {
             theIcon = getResources().getDrawable(R.drawable.sleet);
         } else if (icon.equalsIgnoreCase(WIND)) {
             theIcon = getResources().getDrawable(R.drawable.windy);
         } else if (icon.equalsIgnoreCase(FOG)) {
             theIcon = getResources().getDrawable(R.drawable.fog);
         } else if (icon.equalsIgnoreCase(PARTLY_CLOUDY_DAY)) {
             theIcon = getResources().getDrawable(R.drawable.partlycloudyday);
         } else if (icon.equalsIgnoreCase(PARTLY_CLOUDY_NIGHT)) {
             theIcon = getResources().getDrawable(R.drawable.partlycloudynight);
         } else if (icon.equalsIgnoreCase(THUNDERSTORMS)) {
             theIcon = getResources().getDrawable(R.drawable.thunderstorms);
         } else if (icon.equalsIgnoreCase(HAIL)) {
             theIcon = getResources().getDrawable(R.drawable.hail);
         } else if (icon.equalsIgnoreCase(TORNADO)) {
             theIcon = getResources().getDrawable(R.drawable.tornado);
         } else theIcon = getResources().getDrawable(R.drawable.sunstandin);
 
         ImageView weatherIconView = (ImageView) findViewById(R.id.icon);
         weatherIconView.setImageDrawable(theIcon);
 
 
         new GeonameAPITask(this).execute(myDataObject.myGeoLocation);
 
 
         //oh, go and update with a snarky message
         //convert the temperature to an int
         int currentTemp = myDataObject.getmCurrentTemp().intValue();
 
 
         //pass it to the snark
         determineSnark(currentTemp);
 
         Log.e("LOOK----------------------------------------value of currentTemp", String.valueOf(currentTemp));
     }
 
 
     public void updateMyCity(String cityData) {
         TextView v = (TextView) findViewById(R.id.location);
         font = Typeface.createFromAsset(getAssets(), fontPathC);
         v.setTypeface(font);
         v.setText(cityData);
     }
 
 
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 //
 //
     //   Here is all the code from SnarkyDisplay moved over to this
     //   Activity so we can use it on our display.
     //
 
 
     public void determineSnark(int currentTemp) throws ParseException {
 
 
         String range;
 
 
         SharedPreferences myPrefs = getSharedPreferences(PREFERENCES, 0);
         SharedPreferences.Editor editor = myPrefs.edit();
         String a = myPrefs.getString("hot", "888");
         String b = myPrefs.getString("cold", "888");
         String c = myPrefs.getString("perfect", "888");
 
 
         int hot1 = Integer.parseInt(a);
         int cold1 = Integer.parseInt(b);
         int perfect1 = Integer.parseInt(c);
 
 
         Log.e("LOOK-----------------get pref strings", String.valueOf(a) + String.valueOf(b) + String.valueOf(c));
         Log.e("LOOK------------ PREFS CONVERTED TO INTEGERS", String.valueOf(hot1) + String.valueOf(cold1) + String.valueOf(c));
 
         //this is getting a reference to the view
         TextView myTextView = (TextView) findViewById(R.id.currenttemp);
 
 
 //        //creating a string of  the temp data from the api
 
         int mCTemp = currentTemp;
 
         Log.e("LOOK------------------------------what is the value of mCTemp?", String.valueOf(mCTemp));
 
         if (mCTemp < cold1) {    //bittercold
             range = "bittercold";
         } else if ((mCTemp <= cold1) && mCTemp <= (cold1 + 10)) {     //toocold
             range = "toocold";
         } else if ((mCTemp > (cold1 + 10) && (mCTemp <= (cold1 + 17)))) {      //good
             range = "good";
         } else if ((mCTemp > (cold1 + 10) && (mCTemp <= (perfect1 + 10)))) {   //perfect
             range = "perfect";
         } else if ((mCTemp > (perfect1 + 10) && (mCTemp <= (hot1 - 5)))) {   //warm
             range = "warm";
         } else if (mCTemp > (hot1 - 5) && (mCTemp <= (hot1 + 3))) { //too hot
             range = "toohot";
         } else if (mCTemp > (hot1 + 4)) {
             range = "toodanghot";
         } else {
             range = "toodanghot";
         }
 
         displaySnarkiness(range);
 
         Log.e("LOOK-----------------------------------------------------------------what is the range?", String.valueOf(range));
         myTextView = (TextView) findViewById(R.id.snarky);
     }
 
     public String getJSONFile() {
 
         //
         //  If you get a parsing error, use this website:
         //  http://jsonformatter.curiousconcept.com/
         //
 
         String json = null;
 
         try {
             InputStream is = getResources().openRawResource(R.raw.pantsjson02);
             int size = is.available();
             byte[] buffer = new byte[size];
             is.read(buffer);
             is.close();
             json = new String(buffer, "UTF-8");
 
         } catch (IOException ex) {
             ex.printStackTrace();
             return null;
         }
         return json;
     }
 
     public void displaySnarkiness(String snarkType) {
 
         try {
             JSONObject rootJSON = new JSONObject(getJSONFile());
             JSONArray snarkyCommentArray = rootJSON.getJSONArray(snarkType);
             int snarkyCommentArraySize = snarkyCommentArray.length();
             Random r = new Random();
 
             int randomObjectIndex = r.nextInt(snarkyCommentArraySize - 0) + 0;
             JSONObject selectedRandomObject = snarkyCommentArray.getJSONObject(randomObjectIndex);
             String text = selectedRandomObject.getString("text");
 
             TextView snarky = (TextView) findViewById(R.id.snarky);
             //here you have to actually set the text here
 
             snarky.setText(text);
             //here you have to set the image view
             String nameOfImage = selectedRandomObject.getString("image");
 
             //this code sets the image on myImageView
             ImageView myImageView = (ImageView) findViewById(R.id.pants);
             int resID = getResources().getIdentifier(nameOfImage, "drawable", getPackageName());
             myImageView.setImageResource(resID);
 
         } catch (JSONException e) {
             e.printStackTrace();
             Log.e("Look----------------------------------------------------------------------", e.getMessage(), e);
         }
 
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_settings:
                 Intent a;
                 a = new Intent(getApplicationContext(), DisplaySettingsActivity.class);
                 startActivity(a);
 
                 return true;
 
         }
         return super.onOptionsItemSelected(item);
     }
 
 
 }
 
 
