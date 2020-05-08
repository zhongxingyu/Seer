 package com.rogoapp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.http.NameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 //import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 
 public class MainScreenActivity extends SherlockActivity {
 
     static final String TIPS_FILE = "tips";
     static final String USER_TIPS = "uTips";
 
     Button nearYouButton;
     Button meetRandomButton;
     Button tipsButton;
     Button userButton;
 
     Button debugButton; //TODO REMOVE
 
     List<String> tips = new ArrayList<String>();
     List<String> meetRandom;
 
     CacheClient cache = new CacheClient(this);
     ServerClient server = new ServerClient();
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         setTheme(R.style.Theme_Sherlock_Light);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_screen);
 
         //Taylor ***
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         System.out.println(sharedPrefs.getString("radius", "RADIUS NOT FOUND"));
 
         //Adding some functionality to tips button
         textListener(this.findViewById(R.id.tips_edit_box));
         if(ServerClient.isNetworkAvailable())
         	storeTips();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         menu.add("User")
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
         menu.add("Settings")
         .setOnMenuItemClickListener(this.SettingsClickListener)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
         return true;
     }
 
     public void openUserSettings(View v){
         final Context context = this;
         Intent intent = new Intent(context, UserSettingsActivity.class);
         startActivity(intent);
     }
     OnMenuItemClickListener SettingsClickListener = new OnMenuItemClickListener(){
         @Override
         public boolean onMenuItemClick(MenuItem item){
             Intent intent = new Intent(MainScreenActivity.this, UserSettingsActivity.class);
             startActivity(intent);
             return false;
         }
     };
 
     //adds toast
     public void toaster(String bread){
         LayoutInflater inflater = getLayoutInflater();
 
         View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout_id));
 
         ImageView image = (ImageView) layout.findViewById(R.id.image);
         image.setImageResource(R.drawable.ic_launcher);
 
         TextView text = (TextView) layout.findViewById(R.id.text);
         if(!(bread==null || bread=="")){
             text.setText(bread);
         }
         else{
             text.setText(R.string.burnt_toast);
         }
 
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.BOTTOM, 0, 0);
         toast.setDuration(Toast.LENGTH_SHORT);
         toast.setView(layout);
         toast.show();
     }
     public void toaster(int bread){
         LayoutInflater inflater = getLayoutInflater();
 
         View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout_id));
 
         ImageView image = (ImageView) layout.findViewById(R.id.image);
         image.setImageResource(R.drawable.ic_launcher);
 
         TextView text = (TextView) layout.findViewById(R.id.text);
 
         if(!(bread == 0)){
             text.setText(bread);
         }
         else{
             text.setText(R.string.burnt_toast);
         }
 
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.BOTTOM, 0, 0);
         toast.setDuration(Toast.LENGTH_SHORT);
         toast.setView(layout);
         toast.show();
     }
 
 
     public void addListenerOnButton1() {
 
         nearYouButton = (Button) findViewById(R.id.near_you_button);
 
         nearYouButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 openNearYouScreen(arg0);
             }
 
         });
 
     }
 
     public void addListenerOnButton2() {
 
         nearYouButton = (Button) findViewById(R.id.meet_random_button);
 
         nearYouButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 refreshMeetRandomButton(arg0);
             }
 
         });
 
     }
 
     public void addListenerOnButton3() {
 
         nearYouButton = (Button) findViewById(R.id.tips_button);
 
         nearYouButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 refreshTipsButton(arg0);
             }
 
         });
 
     }
 
     // Navigates the user to the People Near You Screen
     public void openNearYouScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, NearYouActivity.class);
         startActivity(intent);
     }
 
     //refresh the text 
     public void refreshMeetRandomButton(View arg0){
         final Button button = (Button)findViewById(R.id.meet_random_button);
 
         if(meetRandom == null || meetRandom.isEmpty()){
             System.err.println("DEBUG: Reloading meetRandom array");
             reloadMeetRandomArray();
         }
 
         Random rand = new Random(System.currentTimeMillis());
         int random = rand.nextInt(meetRandom.size());
         String out = meetRandom.remove(random);
         button.setText(out);
     }
 
     public void refreshTipsButton(View arg0){
         final Button button = (Button)findViewById(R.id.tips_button);
         final EditText tipsText = (EditText) findViewById(R.id.tips_edit_box);
 
         // replace with random string from tips.xml
         if(tips == null || tips.isEmpty()){
             System.err.println("DEBUG: Reloading tips array");
             this.reloadTipsArray();
         }
 
         String text = button.getText().toString();
         if(text.equals("Add Tip!")){
             String what = tipsText.getText().toString();
             tips.add(what);
             button.setText(R.string.tips);
             if(cache.isEmpty(USER_TIPS)){
                 cache.addToFile(USER_TIPS,what);
             }
             else{
                 cache.addToFile(USER_TIPS,"\n"+what);
             }
             tipsText.setText("");
             if(cache.lines(USER_TIPS) <=5){
 
                 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(tipsText.getWindowToken(), 0);
 
                 toaster(R.string.self_improvement);
             }
 
         }        
         else{
             Random rand = new Random(System.currentTimeMillis());
             int random = rand.nextInt(tips.size());
             String out = tips.remove(random); // Remember that .remove also returns the removed element
             button.setText(out);
         }
     }
 
     /*
     public void openSettingsScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, SettingsActivity.class);
         startActivity(intent);
     }*/
 
     public void openUserScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, UserActivity.class);
         startActivity(intent);
     }
 
 
     public void parseJ(JSONObject jObject, String filename){
         //turns JObject into JArray and steps through the JArray to find all the tips 
         JSONArray jArray = null;
         try {
             //data is the head where the tips will start
             jArray = jObject.getJSONArray("data");
         } catch (JSONException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         cache.saveFile(filename, "");
         for (int i=0; i < jArray.length(); i++)
         {
             try {
                 JSONObject oneObject = jArray.getJSONObject(i);
                 // Pulling items from the array
                 // int objectInt = oneObject.getInt("tip_id");
                 String objectString = oneObject.getString("tip");
                 if(cache.isEmpty(filename)){
                     cache.addToFile(filename, objectString);
                 }
                 else{
                     cache.addToFile(filename, ("\n"+objectString));
                 }
             } catch (JSONException e) {
                 // Oops
             }
         } 
 
     }
 
     public boolean storeTips() {
         
         JSONObject json = server.genericPostRequest("tips", Collections.<NameValuePair>emptyList());
         if(json != null)
             parseJ(json, TIPS_FILE);
         return json != null;
     }
 
     public void reloadTipsArray(){
         //needed some tips so the file wasn't empty
         //if file is empty, it loads the Tips not available exception constantly
         if(cache.isEmpty(TIPS_FILE)){
             if(!storeTips()){
                 oneTimeTipBuffer();
             }
         }
 
         String[] _tips = cache.loadFile(TIPS_FILE).split("\n");
         Collections.addAll(tips, _tips);
         if(!cache.isEmpty(USER_TIPS)){
             String[] _uTips = cache.loadFile(USER_TIPS).split("\n");
             Collections.addAll(tips, _uTips);
         }
         return;
     }
     //needed some tips cached, so I made this
     public void oneTimeTipBuffer() {
 
         cache.saveFile(TIPS_FILE, "New tip!\nWhat a tip\nTipped over\nTipsy");
 
     }
     public void reloadMeetRandomArray(){
         Resources res = getResources();
         if(meetRandom == null){
             meetRandom = new ArrayList<String>();
         }
         String[] _randoms = res.getStringArray(R.array.meetRandomArray);
         Collections.addAll(meetRandom, _randoms);
     }
 
 
     public void onTextEnter(View V){
         Button tips = (Button) this.findViewById(R.id.tips_button);
 
         tips.setText("Add tip!");
     }
 
     public void textListener(View v){
 
         //Adds action event for when data is entered in an EditText
         //This is currently being used for the tips field
 
         final EditText myTextBox  = (EditText) v;
         final Button tips = (Button) findViewById(R.id.tips_button);
         myTextBox.addTextChangedListener(new TextWatcher(){
 
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count){
                 String check = "" + myTextBox.getText();
                 if(check == ""){// && !tips.isPressed()){
                     Button tips = (Button) findViewById(R.id.tips_button);
                     tips.setText("Tips");
                 }	
             }
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {
 
                 //When text box is entered, the tips button becomes an Add tip! button
 
                 tips.setText("Add Tip!");
             }
 
             @Override
             public void afterTextChanged(Editable arg0) {
                 // can implement if desired...
 
             }
         });
     }
     /*
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
     	super.onActivityResult(requestCode, resultCode, data);
 
     	switch(requestCode){
     	case RESULT_SETTINGS:
     		showUserSettings();
     		break;
     	}
     }
 
     private void showUserSettings(){
     	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     	StringBuilder builder = new StringBuilder();
     	builder.append("\n Username: "+sharedPrefs.getString("prefUsername", "NULL"));
     	builder.append("\n Send Report: "+sharedPrefs.getBoolean("prefSendReport", false));
     	builder.append("\n Radius: "+sharedPrefs.getString("prefRadius", "NULL"));
     	TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);
     	settingsTextView.setText(builder.toString());
     }
      */
 
     /* DEBUG SECTION REMOVE BEFORE FINAL*/
     //-------------------------------------------------------------------
     public void addListenerOnButton4() {
 
         debugButton = (Button) findViewById(R.id.debug_button);
 
         debugButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 openDebugScreen(arg0);
             }
 
         });
 
     }
 
     public void openDebugScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, DebugActivity.class);
         startActivity(intent);
     }
 
     // :3
     //-------------------------------------------------------------------
 
 }		
