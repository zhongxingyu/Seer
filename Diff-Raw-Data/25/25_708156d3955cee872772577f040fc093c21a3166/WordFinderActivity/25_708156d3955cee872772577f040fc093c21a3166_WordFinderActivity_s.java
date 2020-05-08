 package com.WordFinder;
 
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.app.Activity;
 import android.os.Bundle;
 
/*
 * // -------------------------------------------------------------------------
  /**
  *  Main application activity.
  *
  *  @author John Mooring (jmooring)
  *  @version Oct 29, 2011
  */
 public class WordFinderActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.main);
     }
 
     /**
      * Constructs the context menu
      *
      * @returns boolean true to show menu
      */
     public boolean onCreateOptionsMenu(Menu menu) {
 	menu.add(0, 0, 0, "About");
 	return true;
     }
 
     /**
      * Called when a menu item is selected
      *
      * @returns boolean false to continue normal processing of event, true to
      *          consume it here
      */
     public boolean onOptionsItemSelected(MenuItem i) {
 	switch (i.getItemId()) {
 	case 0:
 	    startActivity(new Intent(this, About.class));
 	    break;
 	}
 	return true;
     }
 }
