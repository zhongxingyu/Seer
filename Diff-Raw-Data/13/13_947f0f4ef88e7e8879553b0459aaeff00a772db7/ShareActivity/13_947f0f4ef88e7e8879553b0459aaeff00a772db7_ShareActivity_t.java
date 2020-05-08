 package com.encounterpc.clipboard;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.text.ClipboardManager;
 import android.widget.Toast;
 
 import static android.widget.Toast.makeText;
 
 public class ShareActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         if (extras != null && extras.getString(android.content.Intent.EXTRA_TEXT) != null) {
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setText(extras.getString(Intent.EXTRA_TEXT));
            makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show();
        } else makeText(this, getString(R.string.no_content), Toast.LENGTH_LONG).show();
        finish();
     }
 }
