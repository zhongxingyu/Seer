 package com.pr13s7.notepad;
 
 import java.io.File;
 
 import android.app.AlertDialog;
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 
 
 public class main extends Activity 
 {	
	public static final String DATABASE_FILE_PATH = "/data/com.pr13s7.notepad";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
         
         //============ create folder for DB =================
        if(Environment.getExternalStorageState() != null)
         {
        	String path = Environment.getExternalStorageDirectory().toString()+DATABASE_FILE_PATH;
        	File db_dir = new File(path);
        	if(!db_dir.exists())
        		if(!db_dir.mkdirs())
        			show_error(path);
         }
     }
     
     @Override
     protected void onDestroy()
     {
     	super.onDestroy();
     	//my_db.close();
     }
     
     //===== Click search button
     public void click_search(View view)
     {
     	onSearchRequested ();
     }
     
     //===== Click sort button
     public void sort_items(View view)
     {
     	
     	LayoutInflater inflater = getLayoutInflater(); 
     	View layout = inflater.inflate(R.layout.ssort_toast_layout, (ViewGroup)findViewById(R.id.sort_dialog_layout)); 
     	
     	AlertDialog.Builder sort_dlg = new AlertDialog.Builder(this);
     	sort_dlg.setView(layout); 
     	sort_dlg.setTitle("Sort notes");
     	
     	sort_dlg.setPositiveButton("Sort", new DialogInterface.OnClickListener() 
 		{
 	      public void onClick(DialogInterface dialog, int which) 
 	      {
 	    	  dialog.cancel();
 	      } 
 	     }
     	);
     	
     	sort_dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
     		{
     	      public void onClick(DialogInterface dialog, int which) 
     	      {
     	    	  dialog.cancel();
     	      } 
     	     }
     	);
    	
     	sort_dlg.show();
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
     
     //======= show about dialog
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
     
   //======= show error dialog
     public void show_error(String err_msg)
     {
     	AlertDialog.Builder alert_dlg = new AlertDialog.Builder(this);
     	alert_dlg.setMessage(err_msg);
     	alert_dlg.setTitle("Error");
     	
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
