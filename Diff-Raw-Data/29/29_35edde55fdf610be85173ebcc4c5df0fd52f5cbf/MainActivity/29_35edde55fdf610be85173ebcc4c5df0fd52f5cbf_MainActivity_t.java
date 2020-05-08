package com.example.create32;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.example.create32.R;
 
 public class MainActivity extends Activity implements OnClickListener {
     private NfcAdapter mAdapter;
     private boolean mInWriteMode;
     private Button mWriteTagButton;
     private TextView mTextView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // grab our NFC Adapter
         mAdapter = NfcAdapter.getDefaultAdapter(this);
 
         // button that starts the tag-write procedure
         mWriteTagButton = (Button)findViewById(R.id.write_tag_button);
         mWriteTagButton.setOnClickListener(this);
 
         // TextView that we'll use to output messages to screen
         mTextView = (TextView)findViewById(R.id.text_view);
     }
 
     public void onClick(View v) {
         if(v.getId() == R.id.write_tag_button) {
             displayMessage("Touch and hold tag against phone to write.");
             enableWriteMode();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         disableWriteMode();
     }
 
     /**
      * Called when our blank tag is scanned executing the PendingIntent
      */
     @Override
     public void onNewIntent(Intent intent) {
         if(mInWriteMode) {
             mInWriteMode = false;
 
             // write to newly scanned tag
             Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             writeTag(tag);
         }
     }
 
     /**
      * Force this Activity to get NFC events first
      */
     private void enableWriteMode() {
         mInWriteMode = true;
 
         // set up a PendingIntent to open the app when a tag is scanned
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
         IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         IntentFilter[] filters = new IntentFilter[] { tagDetected };
 
         mAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
     }
 
     private void disableWriteMode() {
         mAdapter.disableForegroundDispatch(this);
     }
 
     /**
      * Format a tag and write our NDEF message
      */
     private boolean writeTag(Tag tag) {
         // record to launch Play Store if app is not installed
         NdefRecord appRecord = NdefRecord.createApplicationRecord("com.netmagazine.nfcdemo");
 
         // record that contains our custom "retro console" game data, using custom MIME_TYPE
         byte[] payload = getRandomConsole().getBytes();
        //So the MimeType is here.. not sure whats going on.. will research.
        byte[] mimeBytes = MimeType.NFC.getBytes(Charset.forName("US-ASCII"));
         NdefRecord cardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes,
                 new byte[0], payload);
         NdefMessage message = new NdefMessage(new NdefRecord[] { cardRecord, appRecord});
 
         try {
             // see if tag is already NDEF formatted
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
 
                 if (!ndef.isWritable()) {
                     displayMessage("Read-only tag.");
                     return false;
                 }
 
                 // work out how much space we need for the data
                 int size = message.toByteArray().length;
                 if (ndef.getMaxSize() < size) {
                     displayMessage("Tag doesn't have enough free space.");
                     return false;
                 }
 
                 ndef.writeNdefMessage(message);
                 displayMessage("Tag written successfully.");
                 return true;
             } else {
                 // attempt to format tag
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     try {
                         format.connect();
                         format.format(message);
                         displayMessage("Tag written successfully!\nClose this app and scan tag.");
                         return true;
                     } catch (IOException e) {
                         displayMessage("Unable to format tag to NDEF.");
                         return false;
                     }
                 } else {
                     displayMessage("Tag doesn't appear to support NDEF format.");
                     return false;
                 }
             }
         } catch (Exception e) {
             displayMessage("Failed to write tag");
         }
 
         return false;
     }
 
     private String getRandomConsole() {
         double rnd = Math.random();
         if(rnd<0.25) return "nes";
         else if(rnd<0.5) return "snes";
         else if(rnd<0.75) return "megadrive";
         else return "mastersystem";
     }
 
     private void displayMessage(String message) {
         mTextView.setText(message);
     }
 }
