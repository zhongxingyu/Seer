 package com.delin.speedlogger;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.location.Location;
 import android.os.Bundle;
 import android.widget.CheckBox;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ResultsActivity extends Activity {
 	TextView mMaxSpeed;
 	TextView mDistance;
 	CheckBox mStraightLine;
 	Button mButton;
 	String testline = "origin";
 	MeasurementResult mMeasurement;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.results);
 		mButton = (Button)findViewById(R.id.button1);
 		mButton.setOnClickListener(mOnClickListener);
 		mMaxSpeed = (TextView)findViewById(R.id.textMaxSpeed);
 		mMaxSpeed.setText(testline);
 		mDistance = (TextView)findViewById(R.id.textDistance);
         mStraightLine = (CheckBox)findViewById(R.id.cbStraightLine);
 		
 		mMeasurement = MeasurementResult.GetInstance();
 		mMeasurement.SaveToGPX("/mnt/sdcard/textlog.gpx");
 		
 		ShowResults();
 	}
 	
 	public void ShowResults(){
 		List<Location> mLocList = mMeasurement.GetLocations();
 		
 		// uncomment this to test StraightLine
 		//mLocList.get(3).setLatitude(mLocList.get(3).getLatitude() + 0.01); 
 		
 		Log.i("Results Activity","Locs in path: " + Integer.toString(mLocList.size()));
 		for(int i=0; i<mLocList.size(); ++i){
 			Log.i("Results Activity", "-----\n" + "#" + Integer.toString(i) + 
 				  "\n" + Logger.LocToStr(mLocList.get(i)));
 		}
 		
 		// max speed  TODO: we can get maxSpeed from TrackingSession
 		float maxSpeed = mLocList.get(0).getSpeed();
 		for (int i=1; i<mLocList.size(); i++){
 			if (maxSpeed < mLocList.get(i).getSpeed()){
 				maxSpeed = mLocList.get(i).getSpeed();
 			}
 		}
 		mMaxSpeed.setText(Double.toString(maxSpeed*3.6) + " kph"); // TODO: 3.6 must be a constant
 		// interpolate here
 
 		// distance
		Location origin = mLocList.get(1);
 		Location dest = mLocList.get(mLocList.size()-1);
 		//double distance = origin.distanceTo(dest);   // returns 87.4m
 		double distance = Geometry.DistBetweenLocs(origin,dest,false); // return 156m
 		mDistance.setText(Double.toString(distance));
 		
 		mStraightLine.setChecked(Geometry.StraightLine(mLocList,false));
 	}
 	
 	private void TestFunc(String input) {
 		input = "hardcode";
 	}
 	
     private OnClickListener mOnClickListener = new OnClickListener() {
         public void onClick(View v) {
         	TestFunc(testline);
         	mMaxSpeed.setText(testline);
         	mStraightLine.setChecked(!mStraightLine.isChecked());
         	mStraightLine.setText("hello kitty");
         	
         }
     };
 }
