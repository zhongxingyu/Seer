 package com.weipai;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 
 import java.io.File;
 
 public class ShareActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.share);
 
         View playButton =  findViewById(R.id.play_button);
 
         playButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent viewMediaIntent = new Intent();
                 viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
 
                File path = Environment.getExternalStorageDirectory();
 
                 File file = new File(path, "weipai" + ".mp4");
 
                 viewMediaIntent.setDataAndType(Uri.fromFile(file), "video/*");
                 viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                 startActivity(viewMediaIntent);
             }
         });
 
     }
 }
