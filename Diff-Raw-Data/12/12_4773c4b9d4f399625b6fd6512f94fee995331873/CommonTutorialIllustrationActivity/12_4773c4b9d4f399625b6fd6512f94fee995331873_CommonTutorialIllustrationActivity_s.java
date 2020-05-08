 package com.uncopt.android.tutorials.reflection;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class CommonTutorialIllustrationActivity extends Activity {
 
   @Override
   protected void onCreate(final Bundle state) {
     super.onCreate(state);
     Intent intent;
     try {
       Class.forName("android.app.Fragment");
       intent = new Intent(this, TutorialIllustrationActivity.class);
     }
     catch (ClassNotFoundException e) {
       intent = new Intent(this, SupportTutorialIllustrationActivity.class);
     }
    startActivity(intent);
   }
 
 }
