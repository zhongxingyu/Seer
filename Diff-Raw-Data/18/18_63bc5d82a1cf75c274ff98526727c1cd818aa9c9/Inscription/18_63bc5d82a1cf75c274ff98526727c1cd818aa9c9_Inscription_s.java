 package com.example.strangers.controler;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.example.strangers.R;
 
 public class Inscription extends Activity {
 
 	 @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.activity_inscription);
 	        
 	        //Permet d'autoriser la navigation par l'actionBar avec le up
 	        ActionBar actionBar = getActionBar();
 	        actionBar.setDisplayHomeAsUpEnabled(true);
 	        
 	    }
 
 	    
	    //Mthode permettant de capter les vennements de la bar de menu
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 
 	            case android.R.id.home:
 	    			NavUtils.navigateUpFromSameTask(this);
 	            	/*Intent intent = new Intent(this, Login.class);
 	            	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	    	    	startActivity(intent);*/
 	    	    	
 	                return true;
 
 	            default:
 	                return super.onOptionsItemSelected(item);
 	        }
 	    }
 	    
 	    public void subscribe(View v) {
 	    
 	    }
 	    
 }
