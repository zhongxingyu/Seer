 package com.pr13s7.notepad;
 
 import android.app.AlertDialog;
import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 
public class main extends Activity 
 {
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
     }
     
     
     //===== Click search button
     public void click_search(View view)
     {
     	onSearchRequested ();
     }
     
     //======= show add activity ======
     public void show_add_note(View view)
     {
     	Intent i = new Intent(this, add_note.class);
     	startActivity(i);
     }
     
     //======= create menu
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
     	return true;
     }
     
     @Override //==== Click on menu items
     public boolean onOptionsItemSelected(MenuItem item)
 	{
     	switch(item.getItemId())
     	{
     		case R.id.exit_menu :
     			finish();
     			return true;
     		case R.id.about_menu :
     			show_about();
     			return true;
     		default:
     			return super.onOptionsItemSelected(item);
     	}
 	}
     
     //======= show add activity
     public void show_about()
     {
     	AlertDialog.Builder alert_dlg = new AlertDialog.Builder(this);
     	alert_dlg.setMessage("Notepad v0.1b \n (c) pR13S7");
     	alert_dlg.setTitle("About");
     	
     	alert_dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() 
     		{
     	      public void onClick(DialogInterface dialog, int which) 
     	      {
     	    	  dialog.cancel();
     	      } 
     	     }
     	);
    	
     	alert_dlg.show();
     }
 }
