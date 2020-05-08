 package com.encounterpc.clipboard;
 
 import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import static android.widget.Toast.makeText;
 
 public class ShareActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         if (extras != null && extras.getString(android.content.Intent.EXTRA_TEXT) != null) {
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(extras.getString(Intent.EXTRA_SUBJECT), extras.getString(Intent.EXTRA_TEXT)));
            makeText(this, "Copied to clipboard!", Toast.LENGTH_LONG).show();
            finish();
        } else makeText(this, "No shared content.", Toast.LENGTH_LONG).show();
     }
 }
