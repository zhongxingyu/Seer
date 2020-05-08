 package com.zsoleszpapper.mifarestuff;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.MifareClassic;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
     private static NfcAdapter mAdapter;
     private static PendingIntent mPendingIntent;
     private static String[][] mTechLists;
 
     private static TextView mTextView;
 
     private static int COMMAND_FORMAT = 1;
     private static int COMMAND_READ = 2;
     private static int COMMAND_DECREMENT = 3;
     private static int COMMAND_NOTHING = 255;
     private static int command = COMMAND_NOTHING;
 
     private static Button mFormatButton;
     private static Button mReadButton;
     private static Button mDecrementButton;
 
     private static final String TAG = "zsoleszpapper.mifarestuff";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mTextView = (TextView) findViewById(R.id.textView1);
         mTextView.setText("");
         
         mFormatButton = (Button) findViewById(R.id.button1);
         mFormatButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 command = COMMAND_FORMAT;
                 mTextView.setText("Ready to format, touch tag.");
             }
         });
 
         mReadButton = (Button) findViewById(R.id.button2);
         mReadButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 command = COMMAND_READ;
                 mTextView.setText("Ready to read, touch tag.");
             }
         });
 
         mDecrementButton = (Button) findViewById(R.id.button3);
         mDecrementButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 command = COMMAND_DECREMENT;
                 mTextView.setText("Ready to decrement, touch tag.");
             }
         });
 
         clear_command();
 
         mAdapter = NfcAdapter.getDefaultAdapter(this);
         mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
         mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
 
         Intent intent = getIntent();
         resolveIntent(intent);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mAdapter.enableForegroundDispatch(this, mPendingIntent, null, mTechLists);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mAdapter.disableForegroundDispatch(this);
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
         resolveIntent(intent);
     }
 
     private void forge_value_block(byte[] data, byte value, byte address) {
         data[0] = data[1] = data[2] = data[3] = (byte)0;
         data[0] = value;
         for (int i=0; i<4; i++) {
             data[i+4] = (byte)~(data[i]);
             data[i+8] = data[i];
         }
         data[12] = address;
         data[13] = (byte)~data[12];
         data[14] = data[12];
         data[15] = (byte)~data[12];
     }
 
     private void clear_command() {
         command = COMMAND_NOTHING;
         mTextView.setText("Select command");
     }
 
     private void mifare_format(MifareClassic mfc, int block) throws IOException {
         byte[] data = new byte[16];
         forge_value_block(data, (byte)64, (byte)0);
         mfc.writeBlock(block, data);
         mTextView.setText("Format OK.");
     }
 
     private void mifare_read(MifareClassic mfc, int block) throws IOException {
         byte[] data = new byte[16];
         data = mfc.readBlock(block);
         mTextView.setText(String.format("%d", data[0]));
     }
 
     private void mifare_decrement(MifareClassic mfc, int block) throws IOException {
         mfc.decrement(block, 1);
         mfc.transfer(block);
         mTextView.setText("Decrement OK.");
     }
 
     private void resolveIntent(Intent intent) {
         Log.i(TAG, "resolveIntent");
         String action = intent.getAction();
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
             Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             MifareClassic mfc = MifareClassic.get(tagFromIntent);
 
             try {
                 mfc.connect();
                 int sector = 1;
                 int block = mfc.sectorToBlock(sector);
 
                 boolean auth = mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);
                 if (!auth) { auth=mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY); }
                 if (!auth) { auth=mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_NFC_FORUM); }
                 if (auth) {
                     if (command == COMMAND_FORMAT) {
                         mifare_format(mfc, block);
                     } else if (command == COMMAND_READ) {
                         mifare_read(mfc, block);
                     } else if (command == COMMAND_DECREMENT) {
                         mifare_decrement(mfc, block);
                     } else {
                         clear_command();
                     }
                     command = COMMAND_NOTHING;
                 } else {
                     mTextView.setText("Authentication error");
                 }
             } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
             }
         }
     }
 }
