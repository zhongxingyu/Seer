 package com.rogoapp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 
 public class MainScreenActivity extends Activity {
 
     Button nearYouButton;
     Button meetRandomButton;
     Button tipsButton;
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
         // replace with random string from meet_random.xml
         button.setText("TESTING RANDOM");
 
         //TODO
     }
 
     public void refreshTipsButton(View arg0){
         final Button button = (Button)findViewById(R.id.tips_button);
         // replace with random string from tips.xml
        //List<String> local = reloadTipsArray();
        Resources res = getResources();
        String[] local = res.getStringArray(R.array.tips_array);

         Random rand = new Random();
         int random = rand.nextInt(local.length - 1);
         String out = local[random];
         button.setText(out);
     }
 
 
 
     public void openSettingsScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, SettingsActivity.class);
         startActivity(intent);
     }
     
    public List<String> reloadTipsArray(){
         Resources res = getResources();
        String[] temp1 = res.getStringArray(R.array.tips_array);
        return Arrays.asList(temp1);
     }
     
     public void reloadMeetRandomArray(){
         Resources res = getResources();
         meetRandom = (ArrayList<String>) Arrays.asList(res.getStringArray(R.array.meetRandomArray));
     }
 }		
