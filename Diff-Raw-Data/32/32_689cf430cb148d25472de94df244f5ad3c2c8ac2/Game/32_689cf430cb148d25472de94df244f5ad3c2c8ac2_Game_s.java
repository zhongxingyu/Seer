 package com.ec.morsms;
 
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class Game extends Activity {
 
 	
 	static {
 		System.loadLibrary("morse");
 	}
 	
 	
 	public native String trans(String message_in, int unit_in, int delay_in);
 	
 	//locally store global variables, can't access otherwise in adapter view
 	int unitSpeed;	//not necessary if called from service
 	String word;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_game);
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_game, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     
     //if you press show
     public void onGetPhrase(View view){
     	//just reveal the phrase
     	word = ((Global) this.getApplication()).getLast();
     	Toast.makeText(getBaseContext(), "Last phrase: " + word, Toast.LENGTH_LONG).show();
     }
     
     public void onSetPhrase(View view){
     	word = "get from text field";
     	((Global) this.getApplication()).setLast(word);
     	
     	EditText editText = (EditText) findViewById(R.id.new_phrase);
     	word = editText.getText().toString();
     	
     	//send it to the vibration service.
     	Context context = getBaseContext();
     	Intent newIntent=new Intent(context,VibrationService.class);
         newIntent.putExtra("sms",word);
         context.startService(newIntent);
     	
     }
     
     //button pressed to generate new word
     public void onNewWord(View view){
     	
 
     	//declare the random array of words
     	int rand_phrase_size = 10;	//set to proper number of words there
     	String[] rand_array = {"Hello","Cat","Dog","Random","morSMS","Android","SOS", "Phone", "Hat",
     							"Shakespeare"};
     	
     	//generate random integer
    	int index = (int)(Math.random() * ((rand_phrase_size) + 1));
     	
     	//select random word
     	word = rand_array[index];
 
     	
     	//send it to the vibration service.
     	Context context = getBaseContext();
     	Intent newIntent=new Intent(context,VibrationService.class);
         newIntent.putExtra("sms",word);
         context.startService(newIntent);
     	
    	/*
    	unitSpeed = ((Global) this.getApplication()).getUnitSpeed();
    	//((Global) this.getApplication()).setLast(word);
        String backend_str = trans(word,unitSpeed,200);	//back end.
        
        //save this word as last "message", user convenience
        
        
        String[] backArray = backend_str.split("\\t?\\n");
        
        long[] backArrayLong = new long [backArray.length]; 
        
        //for (int i=0; i< newMax; i++){
        for (int i=0; i< backArray.length; i++){ //-TS
        	backArrayLong[i] = Long.valueOf(backArray[i]).longValue();
        }
        
        
        //vibrate this pattern once.
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(backArrayLong,-1);	//-1 is for no repeats
        */
     	
     	
     }
 
 }
