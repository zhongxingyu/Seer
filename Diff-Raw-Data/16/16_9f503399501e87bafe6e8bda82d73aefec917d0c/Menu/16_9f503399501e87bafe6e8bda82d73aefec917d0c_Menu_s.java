 package com.h.h.h;
 
 import com.h.h.h.Menu;
 import com.h.h.h.Settings;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class Menu extends Activity {
     /** Called when the activity is first created. */
 	
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.menu);
         
         
        
     }
 
     @Override
 	public boolean onCreateOptionsMenu(android.view.Menu menu) {
 		// TODO Auto-generated method stub
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.optionmenu,menu);
 	    return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		 // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.SettingItem:
	    	
 	    	Intent myIntent = new Intent(Menu.this, Settings.class);
 	    		
 			Menu.this.startActivity(myIntent);
 	    case R.id.AboutItem:
	    
	    	Intent myIntent1 = new Intent(Menu.this, About.class);
     		
			Menu.this.startActivity(myIntent1);
 	        return true;
	    
 	    default:
 		
 		return super.onOptionsItemSelected(item);
 	    }
 		
 	}
     
     
     
 }
