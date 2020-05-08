 package com.medieteknik.dyslexia.svp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Play extends Activity {
 
     int counter;
    String s = "S hr lite grann efter Hugo Chavez dd r det tv personer som pekas ut som mktigast bland arvtagarna. Den ene en fre detta busschauffr och fackfreningsman med stark vnsterideologisk profil. Den andre en pragmatisk och vlbrgad ingenjr med ett frflutet inom militren.";
     final String[] words = s.split("\\s+");
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         final TextView tx = (TextView) findViewById(R.id.text_box);
         counter = 0;
         tx.setText(words[counter]+" "+words[counter+1]+" "+words[counter+2]);
         
     Button b = (Button) findViewById(R.id.next_button);
 
     b.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
     	   try{
     		   counter+=3;
     		   
     		   if(words[counter]!=null & words[counter+1]!=null & words[counter+2]!=null){
     			   tx.setText(words[counter]+" "+words[counter+1]+" "+words[counter+2]);
     		   }
     		   else if(words[counter]!=null & words[counter+1]!=null){
     			   tx.setText(words[counter]+" "+words[counter+1]);
     		   }
     		   else{
     			   tx.setText(words[counter]);
     		   }
     	   }
     	   catch( ArrayIndexOutOfBoundsException e){
     		   tx.setText("End of text =(");
     	   }
        }
     });
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.play, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
