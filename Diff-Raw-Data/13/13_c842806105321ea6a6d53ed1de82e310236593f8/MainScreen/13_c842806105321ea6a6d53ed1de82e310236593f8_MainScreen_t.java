 package com.boredomblitzer.boredomblitzer;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 
 public class MainScreen extends Activity {
 	
 	private DataAdapter mDbHelper; 
 	
 	protected static final String TAG = "MainScreen";
 	
 	public final static String ACT_TITLE = "com.example.myfirstapp.ACT_TITLE";
 	public final static String CAT_ID = "com.example.myfirstapp.CAT_ID";
 	public final static String CAT_TITLE = "com.example.myfirstapp.CAT_TITLE";
 	public final static String CAT_IMAGE = "com.example.myfirstapp.CAT_IMAGE";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_screen);
         
         Context urContext = this;
 		mDbHelper = new DataAdapter(urContext);        
 		mDbHelper.createDatabase();      
 		mDbHelper.open();
 
 		//Cursor testdata = mDbHelper.getActivityFromID(2);
 
 		mDbHelper.close();
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main_screen, menu);
         return true;
     }
     
     public void blitzBtnPress(View view){
     	Intent intent = new Intent(this, ShowActivity.class);
     	mDbHelper.open();
     	
     	Random ran = new Random();
    	int randomNum = ran.nextInt(539)+1;
     	
     	Cursor testdata = mDbHelper.getActivityFromID(randomNum);
     	
     	//String actTitle = testdata.getString(testdata.getColumnIndex("Act_Title"));
 		// String catID = testdata.getString(testdata.getColumnIndex("Category"));
 		 Log.i(TAG, "actTitle: " + mDbHelper.actTitle + " catID: " + mDbHelper.catID);
 		 intent.putExtra(ACT_TITLE, mDbHelper.actTitle);
 		 intent.putExtra(CAT_ID, mDbHelper.catID);
 		 intent.putExtra(CAT_TITLE, mDbHelper.catTitle);
 		 intent.putExtra(CAT_IMAGE, mDbHelper.catImage);
 		 testdata.close();
 		 
     	mDbHelper.close();
     	//String logTag = "DBCursor";
 		//Log.i(logTag, "testdata: " + testdata);
     	startActivity(intent);
     }
     
 }
