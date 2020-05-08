 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ca.ilanguage.oprime.activity;
 
 // Need the following import to get access to the app resources, since this
 // class is in a sub-package.
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Chronometer;
 import ca.ilanguage.oprime.R;
 import ca.ilanguage.oprime.content.OPrime;
 import ca.ilanguage.oprime.content.Stimulus;
 import ca.ilanguage.oprime.content.SubExperimentBlock;
 
 public class StopWatchSubExperiment extends Activity {
     Chronometer mChronometer;
     private long lastPause= 0;
     protected ArrayList<? extends Stimulus> mStimuli;
 	protected SubExperimentBlock mSubExperiment;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.stop_watch);
 
         Button button;
 
         mChronometer = (Chronometer) findViewById(R.id.chronometer);
 
         // Watch for button clicks.
         button = (Button) findViewById(R.id.start);
         button.setOnClickListener(mStartListener);
 
         button = (Button) findViewById(R.id.stop);
         button.setOnClickListener(mStopListener);
 
         button = (Button) findViewById(R.id.reset);
         button.setOnClickListener(mResetListener);
         
         /*
 		 * Prepare Stimuli
 		 */
 		mSubExperiment = (SubExperimentBlock) getIntent().getExtras()
 				.getSerializable(OPrime.EXTRA_SUB_EXPERIMENT);
 		this.setTitle(mSubExperiment.getTitle());
 		mStimuli = mSubExperiment.getStimuli();
 
 		if (mStimuli == null || mStimuli.size() == 0) {
 			ArrayList<Stimulus> ids = new ArrayList<Stimulus>();
 			ids.add(new Stimulus(R.drawable.androids_experimenter_kids));
 			mStimuli = ids;
 		}
 		
     }
     @Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			finishSubExperiment();
 		}
 		return super.onKeyDown(keyCode, event);
 
 	}
     public void finishSubExperiment() {
 		mSubExperiment.setDisplayedStimuli(mStimuli.size());
 		mSubExperiment.setStimuli(mStimuli);
 		Intent video = new Intent(OPrime.INTENT_STOP_VIDEO_RECORDING);
 	    sendBroadcast(video);
 	    Intent audio = new Intent(OPrime.INTENT_START_AUDIO_RECORDING);
 		stopService(audio);
 		 
 		Intent intent = new Intent(OPrime.INTENT_FINISHED_SUB_EXPERIMENT);
 		intent.putExtra(OPrime.EXTRA_SUB_EXPERIMENT, mSubExperiment);
 		setResult(OPrime.EXPERIMENT_COMPLETED, intent);
 		
 		finish();
 	}
     View.OnClickListener mStartListener = new OnClickListener() {
         public void onClick(View v) {
             if(lastPause == 0) {
             	mChronometer.setBase(SystemClock.elapsedRealtime());
 
             }else{
             	mChronometer.setBase(mChronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
             }
             
 
             mChronometer.start();
         }
     };
 
     View.OnClickListener mStopListener = new OnClickListener() {
         public void onClick(View v) {
         	lastPause = SystemClock.elapsedRealtime();
 
             mChronometer.stop();
     
         }
     };
 
     View.OnClickListener mResetListener = new OnClickListener() {
         public void onClick(View v) {
             mChronometer.setBase(SystemClock.elapsedRealtime());
         }
     };
 
 }
