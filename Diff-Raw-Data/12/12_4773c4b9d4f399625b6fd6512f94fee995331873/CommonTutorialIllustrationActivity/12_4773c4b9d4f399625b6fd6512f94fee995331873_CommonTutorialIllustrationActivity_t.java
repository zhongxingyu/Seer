 package com.uncopt.android.tutorials.reflection;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class CommonTutorialIllustrationActivity extends Activity {
 
  public static final int RESULT_FINISH = 2;

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
    startActivityForResult(intent, RESULT_FINISH);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == RESULT_FINISH) {
      finish();
    }
   }
 
 }
