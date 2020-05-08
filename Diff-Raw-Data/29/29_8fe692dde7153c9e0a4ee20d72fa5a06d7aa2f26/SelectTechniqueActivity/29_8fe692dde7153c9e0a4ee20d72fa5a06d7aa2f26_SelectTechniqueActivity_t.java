 package org.on.puz.photobombsquad;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 
 public class SelectTechniqueActivity extends Activity {
 
 public final static String EXTRA_MESSAGE = "com.photobomb.detector.MainActivity.MESSAGE";
 
 @Override
 protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_select_technique);
 }
 
 /**
  * Called when the user clicks the Replacements button
  */
 public void startReplacements(View view) {
     Intent intent = new Intent(this, SelectReplaceActivity.class);
     startActivity(intent);
 }
 
public void startBlur(View view) {
    Intent intent = new Intent(this, CameraActivity.class);
    intent.putExtra(EXTRA_MESSAGE, "?~");
    startActivity(intent);
}

 /**
  * Called when the user clicks the Remove button
  */
 public void startRemove(View view) {
     Intent intent = new Intent(this, CameraActivity.class);
     intent.putExtra(EXTRA_MESSAGE, "?");
     startActivity(intent);
 }
 
 
 }
