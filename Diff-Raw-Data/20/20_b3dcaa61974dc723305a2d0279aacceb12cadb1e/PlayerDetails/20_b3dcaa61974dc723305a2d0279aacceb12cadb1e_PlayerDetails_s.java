 package com.sqlitefun;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PlayerDetails extends Activity{
 	private DbAdapter mDb;
 	private Button btnBack;
 	private Button btnStop;
 	private Button btnStart;
 	private int currPlayerID;
 	private String strName;
 	private long startTime;
 	private TextView time = null;
 	private TextView name;
 	private Handler mHandler = new Handler();
 	private Runnable mUpdateTimeTask = new Runnable(){
     	
     	//the task for updating the time.
 		public void run() {
 			long lngMilli = System.currentTimeMillis() - startTime;
 
 			//updates the time label
 			time.setText(formatTime(lngMilli));
 			mHandler.postDelayed(this, 50);
 			//adds a the same task 50 ms in the future. This is an infinite loop
 		}
     	
     };
 	public void onCreate(Bundle savedinstancestate){
 		super.onCreate(savedinstancestate);
 		setContentView(R.layout.details);
 		mDb = new DbAdapter(this);
 		mDb.open();
 		time = (TextView)findViewById(R.id.timer_lbl);
 		name = (TextView)findViewById(R.id.player_name);
 		btnBack = (Button)findViewById(R.id.btn_back);
 		btnStop = (Button)findViewById(R.id.btn_stop);
 		btnStart = (Button)findViewById(R.id.btn_start);
 		
 		btnBack.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				finish();
 			}
 			
 		});
 		//start/stop buttons
 		btnStop.setOnClickListener(new OnClickListener(){
         	
         	public void onClick(View v){
 
         		mHandler.removeCallbacks(mUpdateTimeTask);
        		long newtime;
        		newtime = (System.currentTimeMillis()-startTime);
        		time.setText(formatTime(newtime));
        		mDb.addTime(newtime, currPlayerID);
         	}        	
         });
 		btnStart.setOnClickListener(new OnClickListener(){
 			/*
 			 * Stores the current system time, and then restarts the update timer task.
 			 */
 				public void onClick(View arg0) {
 					startTime = System.currentTimeMillis();
 					mHandler.removeCallbacks(mUpdateTimeTask);
 					mHandler.postDelayed(mUpdateTimeTask, 100);	
 				}	
 	        });
 		//load players:
 		Bundle extras = getIntent().getExtras();
 		currPlayerID = extras.getInt(DbAdapter.KEY_PLAYER_ID);
 		Cursor currPlayer = mDb.fetchPlayer(currPlayerID);
 		startManagingCursor(currPlayer);
 		
 		strName = currPlayer.getString(currPlayer.getColumnIndex(DbAdapter.KEY_NAME));
 		name.setText(strName);
 			//get name, get times
 	}
 	private String formatTime(long time){
 		int intSec = (int) (time / 1000);
 		int intMin = intSec / 60;
 		intSec = intSec % 60;
 		time = time % 1000;
 		return String.format("%d:%02d.%03d",intMin,intSec,time);
 	}
 	//update labels
 	public void finish(){
 		mDb.close();
 		super.finish();
 	}
 }
