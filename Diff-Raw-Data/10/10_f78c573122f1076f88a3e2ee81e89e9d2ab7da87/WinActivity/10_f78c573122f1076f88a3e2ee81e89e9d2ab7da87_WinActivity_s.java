 package de.hsa.otma.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * Activity shown when user exits through the win door.
  */
 public class WinActivity extends Activity {
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setupClickableWinText();
     }
 
 
     private void setupClickableWinText(){
         TextView text = (TextView) findViewById(R.id.winClickText);
         text.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String url = "http://www.onthemove-academy.org/";
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(url));
                 startActivity(intent);
             }
         });
     }
 
 }
 
