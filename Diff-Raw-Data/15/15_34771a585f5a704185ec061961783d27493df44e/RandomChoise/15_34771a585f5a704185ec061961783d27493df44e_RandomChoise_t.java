 package com.term;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class RandomChoise extends Activity {
 
 	private static final String TAG = "RandomChoise";
 	
 	Uri mUri;
 	Cursor mCursor;
 	int max = 0;
 	String random_result = "";
 	Random numx = new Random();
 	TextView tv;
 	Button buttonstart;
 	FoodProvider pro = new FoodProvider();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.random_choise);
 		Intent intent = getIntent();
 		
 		mUri = intent.getData();
 		tv = (TextView)findViewById(R.id.random_result);
 		buttonstart = (Button) findViewById(R.id.button_start);
 		buttonstart.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				mCursor = managedQuery(mUri, FoodColumn.PROJECTION, null, null, null);
 				mCursor.moveToFirst();
 				max = mCursor.getCount();
				long id = numx.nextInt(max);
				while(id>0){
					mCursor.moveToNext();
					id=id-1;
				}
				//Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
				//mCursor = managedQuery(uri, FoodColumn.PROJECTION, null, null, null);
				//Log.e(TAG + ":onCreate", mCursor.toString() + "  " + id + max);
 				//mCursor = pro.query(mUri, FoodColumn.PROJECTION, FoodColumn._ID, idnum, null);
				//mCursor.moveToFirst();
 				random_result = mCursor.getString(FoodColumn.NAME_COLUMN);
 				tv.setText(random_result);
 			}
 		});
 
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 }
