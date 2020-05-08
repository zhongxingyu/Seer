 /*
  * project		WCWidget
  * 
  * package		com.jpennell.wcwidget
  * 
  * author		Jerry Pennell
  * 
  * date			Sep 17, 2013
  */
 package com.jpennell.wcwidget;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RemoteViews;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jpennell.library.DetailFragment;
 import com.jpennell.library.FileSystem;
 import com.jpennell.library.FormFragment;
 import com.jpennell.library.StorageParser;
 import com.jpennell.library.WeatherContentProvider;
 import com.jpennell.library.WeatherService;
 import com.jpennell.library.Web;
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MainActivity.
  */
 @SuppressLint("HandlerLeak")
 public class MainActivity extends Activity implements FormFragment.formListener{
 
     //Global variables
     /** The _context. */
     Context _context = this;
     
     /** The _is connected. */
     Boolean _isConnected = false;
     
     /** The _list view. */
     ListView _listView;
     
     /** The widget id. */
     int widgetId;
     
     /** The cc desc. */
     public static String ccDesc;
     
     /** The cc temp. */
     public static String ccTemp;
     
     /** The zip. */
     public static String zip;
     
     /** The Constant DAY_PICKED_REQUEST. */
     static final int DAY_PICKED_REQUEST = 1;
 
 
     /**
      * Handle service connection.
      */
     public void handleServiceConnection() {
         Button searchButton = (Button) findViewById(R.id.searchButton);
         Button setButton = (Button) findViewById(R.id.set_button);
 
         // Detect Network Connection
         _isConnected = Web.getConnectionStatus(_context);
         if (_isConnected) {
             Log.i("NETWORK CONNECTION", Web.getConnectionType(_context));
 
             // Enable button
             searchButton.setClickable(true);
             setButton.setClickable(true);
 
         } else {
             // Alert if not connected
             AlertDialog.Builder alert = new AlertDialog.Builder(_context);
             alert.setTitle("No Network Connection");
             alert.setMessage("Please check your network connections and try again.");
             alert.setCancelable(false);
             alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.cancel();
                 }
             });
             alert.show();
 
             // Disable button
             searchButton.setClickable(false);
             setButton.setClickable(false);
         }
     }
 
 
     /**
      * Display current.
      */
     public void displayCurrent() {
         // Display current condition via parsing stored json string
         String readStorage = FileSystem.readStringFile(_context, "temp", false);
 
         JSONObject json;
         JSONObject data;
         JSONArray cc;
 
         try {
             // Current condition
             json = new JSONObject(readStorage);
             data = json.getJSONObject("data");
             cc = data.getJSONArray("current_condition");
 
             JSONObject ccObject = cc.getJSONObject(0);
             // Get description
             ccDesc = ccObject.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
             // Get temp
             ccTemp = ccObject.getString("temp_F");
 
             // Convert description text with image and set current condition description image
             ((ImageView)findViewById(R.id.imageView)).setImageResource(StorageParser.getDescImage(ccDesc));
             ((TextView) findViewById(R.id.textView)).setText(ccTemp + " F");
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Display five day.
      *
      * @param cursor the cursor
      */
     public void displayFiveDay(Cursor cursor) {
         // Display weather data via the content provider
         ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
 
         cursor.moveToFirst();
 
         if (cursor.moveToFirst()) {
             for (int i = 0; i < cursor.getCount(); i++) {
                 HashMap<String, String> displayMap = new HashMap<String, String>();
 
                 // Get values for each day
                 displayMap.put("date", cursor.getString(1));
                 displayMap.put("desc", cursor.getString(2));
                 displayMap.put("hi", cursor.getString(3));
                 displayMap.put("low", cursor.getString(4));
                 displayMap.put("wind", cursor.getString(5));
 
                 cursor.moveToNext();
 
                 dataList.add(displayMap);
             }
         }
         SimpleAdapter adapter = new SimpleAdapter(_context, dataList, R.layout.list_row, new String[] {"desc", "hi", "low"}, new int[] {R.id.desc, R.id.tempLow, R.id.tempHi});
 
         // Set adapter to listView
         _listView.setAdapter(adapter);
     }
 
 
     /* (non-Javadoc)
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Set content from XML layout
         setContentView(R.layout.form_frag);
         _listView = (ListView) this.findViewById(R.id.list);
         View listHeader = this.getLayoutInflater().inflate(R.layout.list_header, null);
         _listView.addHeaderView(listHeader);
 
         // Call handleServiceConnection method
         handleServiceConnection();
 
         Bundle extras = getIntent().getExtras();
 
         if (extras != null) {
             // Get app widget id
             widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
         }
     }
 
 
     /* (non-Javadoc)
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //This functionality has to be moved to the fragment
         //getSupportMenuInflater().inflate(R.menu.main, menu);
     return true;
     }
 
 
     /* (non-Javadoc)
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();
 
         Log.i("item ID : ", "onOptionsItemSelected Item ID" + id);
 
         switch (item.getItemId()) {
             case R.id.action_refresh:
                 Intent intent = new Intent(this, MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
 
             case R.id.action_web:
                 Intent implicitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.worldweatheronline.com"));
 
                 startActivity(implicitIntent);
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     /* (non-Javadoc)
      * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // Check which request we're responding to
         if (requestCode == DAY_PICKED_REQUEST) {
             // Make sure the request was successful
             if (resultCode == RESULT_OK) {
                 Bundle result = data.getExtras();
                 if (result != null) {
                     String date = result.getString("date");
 
                     Toast toast = Toast.makeText(_context, "You checked the weather for " + date, Toast.LENGTH_SHORT);
                     toast.show();
                 }
             }
         }
     }
 
 
     // FormFragment methods
     /* (non-Javadoc)
      * @see com.jpennell.library.FormFragment.formListener#onWeatherSearch()
      */
     @Override
     public  void onWeatherSearch() {
         final EditText zipField = (EditText) findViewById(R.id.searchField);
         zip = zipField.getText().toString();
         Log.i("CLICK HANDLER", zip);
 
         Handler myHandler = new Handler() {
             @Override
             public void handleMessage(Message message) {
                 if (message.arg1 == RESULT_OK && message.obj != null) {
                     String confirmedURL = message.obj.toString();
                     Log.i("URL RESPONSE", confirmedURL);
 
                     try {
                         // Pull JSON data from API
                         JSONObject json = new JSONObject(confirmedURL);
                         JSONObject data = json.getJSONObject("data");
                         Boolean error = data.has("error");
                         if (error) {
                             // Create toast (popup)
                             Toast toast = Toast.makeText(_context,"Invalid location", Toast.LENGTH_SHORT);
                             toast.show();
                         } else {
                             // Get JSON data to determine correct zip code
                             String request = data.getJSONArray("request").getJSONObject(0).getString("query");
 
                             // Create toast (popup)
                             Toast toast = Toast.makeText(_context,"Valid location, " + request, Toast.LENGTH_SHORT);
                             toast.show();
 
                             // Query content provider
                             Cursor cursor = getContentResolver().query(WeatherContentProvider.WeatherData.CONTENT_URI, null, null, null, null);
 
                             // Call displayCurrent method
                             displayCurrent();
 
                            // Call displayFiveDay method  
                            if(cursor != null){
                               displayFiveDay(cursor);
                            }
 
                             if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                                 // Show "Set Widget" button
                                 Button setWidget = (Button) findViewById(R.id.set_button);
                                 setWidget.setVisibility(View.VISIBLE);
                             }
                             
                           //Close out Keyboard
                             InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                         	imm.hideSoftInputFromWindow(zipField.getWindowToken(), 0);
                         }
                     } catch (JSONException e) {
                         Log.e("JSON", e.toString());
                     }
                 }
             }
         };
         // Create Messenger Class
         Messenger myMessenger = new Messenger(myHandler);
 
         Intent intent = new Intent(_context,WeatherService.class);
         intent.putExtra("messenger", myMessenger);
         intent.putExtra("zip", zip);
 
         // Start Intent
         startService(intent);
         
         
     }
 
 
     /* (non-Javadoc)
      * @see com.jpennell.library.FormFragment.formListener#onSetWidget()
      */
     public void onSetWidget() {
         Log.i("SET_WIDGET", "Set button clicked!");
 
         if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
             RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);
 
             // Set text
             remoteViews.setTextViewText(R.id.now_temp, ccTemp);
             remoteViews.setTextViewText(R.id.zip_location, zip);
             remoteViews.setTextViewText(R.id.now_desc, ccDesc);
 
             Intent buttonIntent = new Intent(_context, MainActivity.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, buttonIntent ,0);
 
             remoteViews.setOnClickPendingIntent(R.id.go_button, pendingIntent);
 
             AppWidgetManager.getInstance(this).updateAppWidget(widgetId, remoteViews);
 
             Intent resultValue = new Intent();
             resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
             setResult(RESULT_OK, resultValue);
             finish();
         }
     }
 
     /* (non-Javadoc)
      * @see com.jpennell.library.FormFragment.formListener#onSingleDaySelected(java.lang.Integer)
      */
     @Override
     public void onSingleDaySelected(Integer i) {
         Log.i("ROW CLICKED", "Row " + i + " clicked");
 
         @SuppressWarnings("unchecked")
 		HashMap<String, String> detailMap = (HashMap<String, String>) _listView.getItemAtPosition(i);
 
         Intent detailIntent = new Intent(_context, DetailsActivity.class);
 
         String date = null;
         String desc = null;
         String hi = null;
         String low = null;
         String wind = null;
 
         // Put extra
         if (detailMap != null) {
             date = detailMap.get("date");
             desc = detailMap.get("desc");
             hi = detailMap.get("hi");
             low = detailMap.get("low");
             wind = detailMap.get("wind");
 
 
             detailIntent.putExtra("detailDate", date);
             detailIntent.putExtra("detailDesc", desc);
             detailIntent.putExtra("detailHi", hi);
             detailIntent.putExtra("detailLow", low);
             detailIntent.putExtra("detailWind", wind);
 
             Log.i("INTENT", "Works");
         }
 
         DetailFragment fragment = (DetailFragment) getFragmentManager().findFragmentById(R.id.detail_frag);
         if (fragment != null && fragment.isInLayout()) {
             fragment.displayDetails(date, desc, low, hi, wind);
         } else {
             // Start Activity for results
             startActivityForResult(detailIntent, DAY_PICKED_REQUEST);
         }
     }
 }
