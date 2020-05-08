 package com.rogoapp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
import android.widget.TextView;
 
 public class MainScreenActivity extends Activity {
 
     Button nearYouButton;
     Button meetRandomButton;
     Button tipsButton;
     Button userButton;
     
     Button debugButton; //TODO REMOVE
     
     List<String> tips;
     List<String> meetRandom;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main_screen);
         
         //tips = new ArrayList<String>();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main_screen, menu);
         return true;
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
         // replace with random string from tips.xml
         if(tips == null || tips.isEmpty()){
         	System.err.println("DEBUG: Reloading tips array");
             this.reloadTipsArray();
         }
         Random rand = new Random(System.currentTimeMillis());
         int random = rand.nextInt(tips.size());
         String out = tips.remove(random); // Remember that .remove also returns the removed element
         button.setText(out);
     }
 
 
     public void openSettingsScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, SettingsActivity.class);
         startActivity(intent);
     }
     
     public void openUserScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, UserActivity.class);
         startActivity(intent);
     }
     
     public void reloadTipsArray(){
         Resources res = getResources();
         if(tips == null){
         	tips = new ArrayList<String>();
         }
         String[] _tips = res.getStringArray(R.array.tips_array);
         Collections.addAll(tips, _tips);
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
    
//    Button tipsText = (Button) this.findViewById(R.id.tips_button);
//    TextView.OnEditorActionListener tipListener = new TextView.OnEditorActionListener(){
//    	@Override
//    	public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {        	
//        	tipsText.setText("Add tip!");
//    		   return true;
//    		}
//    };

     
     
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
