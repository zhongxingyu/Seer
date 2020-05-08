 package com.ntu.tryanddemo;
 
import java.util.Locale;

 import android.app.Activity;
 import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class TryAndDemoActivity extends Activity implements View.OnClickListener {
 	static final String TAG = "Try";
 	private Button mBtn1;
	private Button mBtn2;
 	private ImageView mImg1;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Log.d(TAG, "onCreate");
         
         mBtn1 = (Button) findViewById(R.id.button1);
         mBtn1.setOnClickListener(this);
        mBtn2 = (Button) findViewById(R.id.button2);
        mBtn2.setOnClickListener(this);
         mImg1 = (ImageView) findViewById(R.id.imageView1);
     }
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		Log.d(TAG, "onDestroy");
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		Log.d(TAG, "onPause");
 	}
 
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 		Log.d(TAG, "onRestart");
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		Log.d(TAG, "onResume");
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v.equals(mBtn1)) {
 			Log.d(TAG, "click button1");
 			mImg1.setImageResource(R.drawable.star);
 			mImg1.setOnTouchListener(new OnTouchListener(){
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					Intent intent = new Intent();
 					intent.setClass(TryAndDemoActivity.this, MultiTouchPainting.class);
 					startActivity(intent);
 					return false;
 				}
 			});
 		}
		else if(v.equals(mBtn2)) {
			Log.e(TAG, "Locale = "+Locale.getDefault());
			SQLiteDatabase db = null;
			db = SQLiteDatabase.openDatabase("/data/data/com.ntu.tryanddemo/databases/profiles.db", null, SQLiteDatabase.OPEN_READONLY);
			//db = SQLiteDatabase.openDatabase("/data/data/com.ntu.tryanddemo/databases/profiles.db", null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			
			Log.e(TAG, "path = "+db.getPath());
		}
 	}
     
 }
