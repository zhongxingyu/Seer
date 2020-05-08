 package com.whichwayis;
 
 import java.net.URI;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import org.apache.cordova.*;
 
 public class WhichWayIsActivity extends DroidGap {
	private Uri target;
 	
	public void setTarget(Uri target) {
 		this.target = target;
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
               
         super.loadUrl("file:///android_asset/www/index.html");
        
        Intent i = getIntent();
        if(i != null) {
        	Uri uri = i.getData();
        	
        }
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         super.onOptionsItemSelected(item);
         
         Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, target.toString());
         shareIntent.setType("text/plain");
         
         startActivity(shareIntent);
 
         return true;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuItem menuItem = menu.add(menu.NONE, menu.NONE, menu.NONE, R.string.menu_share);
 
     	return true;
     }
 }
