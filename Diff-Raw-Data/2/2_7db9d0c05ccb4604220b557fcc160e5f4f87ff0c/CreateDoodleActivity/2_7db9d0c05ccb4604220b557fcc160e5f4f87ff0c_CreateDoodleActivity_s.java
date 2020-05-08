 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package edu.osu.cse.doodleLock;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CreateDoodleActivity extends Activity
 {
     private static final int TRAINING_SESSION_COUNT_MAX = 10;
     private static final int TRAINING_SESSION_COUNT_MIN = 5;
     private static final float LENGTH_THRESHOLD = 120.0f;
 
     protected Gesture mGesture;
 
     /**
      * List of the gestures that the user has saved
      */
     protected ArrayList<Gesture> mSavedGestureList;
 
     /**
      * The button that is selected in order to finish the training session
      */
     protected View mFinishSessionButton;
 
     /**
      * The button that is selected in order to discard the doodle from the training session
      */
     protected View mDiscardDoodleButton;
 
     /**
      * The button that is selected in order to save a doodle
      */
     protected View mSaveDoodleButton;
 
     protected View mAuthenticateButton;
 
     /**
      * The view on which to draw gestures
      */
     protected GestureOverlayView mGestureOverlay;
 
     /**
      * The doodle that is being used to authenticate the user and verify the training gestures
      */
     protected Doodle mDoodle;
 
     /**
      * Name of the user passed in by the parent activity
      */
     protected String mUserName;
     protected String mActivityType;
 
     protected File mUserDir;
     protected File mUserFile;
 
     protected TextView mTrainingSessionName;
     protected TextView mAuthenticationSessionName;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.create_gesture);
 
         // Assign button layout elements to member variables
         mFinishSessionButton = findViewById(R.id.finishSession);
         mDiscardDoodleButton = findViewById(R.id.discardDoodle);
         mSaveDoodleButton = findViewById(R.id.saveDoodle);
         mAuthenticateButton = findViewById(R.id.authenticateDoodle);
 
         mTrainingSessionName = (TextView) findViewById(R.id.trainingSessionName);
 
         mGestureOverlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
 
         // Disable the Finish button until the min number of doodles have been trained
         mFinishSessionButton.setEnabled(false);
         mDiscardDoodleButton.setEnabled(false);
         mSaveDoodleButton.setEnabled(false);
 
         // Set the doodle as null for the first gesture
         mDoodle = null;
 
         // Get the user name passed with the intent
         mUserName = (String) this.getIntent().getExtras().get(getString(R.string.user_name));
         mActivityType = (String) this.getIntent().getExtras().get(getString(R.string.activity_type));
 
         // Make a directory for the user
         mUserDir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.root_dir) + "/" + mUserName + "/");
         mUserFile = new File(mUserDir.getAbsolutePath() + mUserName);
 
         if (mActivityType.equals(getString(R.string.train)))
         {
             mTrainingSessionName.setText(mUserName + "'s Training Session");
             mUserDir.mkdirs();
 
         }
         else if (mActivityType.equals(getString(R.string.retrain)))
         {
             mTrainingSessionName.setText("Retrain " + mUserName + "'s Doodle");
             deletePreviousSession();
         }
         else if (mActivityType.equals(getString(R.string.authenticate)))
         {
             setContentView(R.layout.authenticate_gesture);
             mAuthenticationSessionName = (TextView) findViewById(R.id.authenticationSessionName);
             mAuthenticationSessionName.setText("Authenticate with " + mUserName + "'s Doodle");
             mGestureOverlay = (GestureOverlayView) findViewById(R.id.authenticate_overlay);
             mAuthenticateButton = findViewById(R.id.authenticateDoodle);
             mAuthenticateButton.setEnabled(false);
         }
         else
         {
             Log.d(CreateDoodleActivity.class.getName().toString(), "ERROR: Invalid activity type");
             return;
         }
 
         mGestureOverlay.addOnGestureListener(new GesturesProcessor());
 
         mSavedGestureList = new ArrayList<Gesture>();
     }
 
     /**
      * Delete the files in the user's directory
      */
     private void deletePreviousSession()
     {
         for (File file : mUserDir.listFiles())
         {
             file.delete();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
 
         if (mGesture != null)
         {
             outState.putParcelable("gesture", mGesture);
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState)
     {
         super.onRestoreInstanceState(savedInstanceState);
 
         mGesture = savedInstanceState.getParcelable("gesture");
         if (mGesture != null)
         {
             final GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
             overlay.post(new Runnable() {
                 public void run()
                 {
                     overlay.setGesture(mGesture);
                 }
             });
 
             mFinishSessionButton.setEnabled(true);
         }
     }
 
     public void onAuthenticateButtonPress(View v)
     {
         GestureLibrary userStore = GestureLibraries.fromFile(mUserFile);
         userStore.load();
 
         ArrayList<Gesture> gesturesFromFile = new ArrayList<Gesture>();
         for (String entry : userStore.getGestureEntries())
         {
             gesturesFromFile.addAll(userStore.getGestures(entry));
         }
 
         Doodle prediction = new Doodle(gesturesFromFile);
 
         if (prediction.authenticate(mGesture))
         {
             Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
             mGestureOverlay.clear(false);
             mAuthenticateButton.setEnabled(false);
         }
         else
         {
             Toast.makeText(this, "Incorrect. Please try again.", Toast.LENGTH_SHORT).show();
             mGestureOverlay.clear(false);
             mAuthenticateButton.setEnabled(false);
         }
 
     }
 
     public void onSaveButtonPress(View v)
     {
         if (mGesture != null)
         {
             mSaveDoodleButton.setEnabled(false);
             mDiscardDoodleButton.setEnabled(false);
 
             final GestureLibrary store = GestureBuilderActivity.getStore();
 
             setResult(RESULT_OK);
 
             final String path = new File(Environment.getExternalStorageDirectory(), "gestures").getAbsolutePath();
 
             boolean haveMinDoodlesBeenDrawn = mSavedGestureList.size() >= TRAINING_SESSION_COUNT_MIN;
             boolean haveMaxDoodlesBeenDrawn = mSavedGestureList.size() >= TRAINING_SESSION_COUNT_MAX;
 
             // If the max number of sessions has not been met
             if (!haveMaxDoodlesBeenDrawn)
             {
                 // Don't authenticate attempts up to the minimum
                 if (!haveMinDoodlesBeenDrawn)
                 {
                     mSavedGestureList.add(mGesture);
                     mDoodle = new Doodle(mSavedGestureList);
                     store.save();
                     Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                 }
                 // Authenticate subsequent doodles
                 else if (haveMinDoodlesBeenDrawn)
                 {
                     // If the doodle matches the training set
                     if (mDoodle.authenticate(mGesture))
                     {
                         // mSavedGestureList.add(mGesture);
                         mDoodle = new Doodle(mSavedGestureList);
                         // store.addGesture(name.toString().concat("" +
                         // mSavedGestureList.size()), mGesture);
                         // store.save();
                         Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                     }
                     // Doodle failed to authenticate
                     else
                     {
                         // Alert the user to try again
                         Toast.makeText(this, getString(R.string.invalid_gesture), Toast.LENGTH_LONG).show();
                     }
                 }
             }
             // If the min number of session has been met
             if (mSavedGestureList.size() >= TRAINING_SESSION_COUNT_MIN)
             {
                 mFinishSessionButton.setEnabled(true);
             }
             mGestureOverlay.clear(false);
         }
         else
         {
             setResult(RESULT_CANCELED);
         }
     }
 
     public void onDiscardButtonPress(View v)
     {
         setResult(RESULT_CANCELED);
        mGestureOverlay.clear(true);
     }
 
     public void onFinishSessionButtonPress(View v)
     {
         GestureLibrary userStore = GestureLibraries.fromFile(mUserFile);
 
         for (Gesture gesture : mSavedGestureList)
         {
             userStore.addGesture(gesture.toString(), gesture);
         }
 
         userStore.save();
 
         finish();
     }
 
     private class GesturesProcessor implements GestureOverlayView.OnGestureListener
     {
         public void onGestureStarted(GestureOverlayView overlay, MotionEvent event)
         {
             mSaveDoodleButton.setEnabled(false);
             mDiscardDoodleButton.setEnabled(false);
             mGesture = null;
         }
 
         public void onGesture(GestureOverlayView overlay, MotionEvent event)
         {
         }
 
         public void onGestureEnded(GestureOverlayView overlay, MotionEvent event)
         {
             mGesture = overlay.getGesture();
             if (mGesture.getLength() < LENGTH_THRESHOLD)
             {
                 overlay.clear(false);
                 mGesture = null;
             }
             else
             {
                 mSaveDoodleButton.setEnabled(true);
                 mDiscardDoodleButton.setEnabled(true);
                 if (mAuthenticateButton != null)
                 {
                     mAuthenticateButton.setEnabled(true);
                 }
             }
 
         }
 
         public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event)
         {
         }
     }
 }
