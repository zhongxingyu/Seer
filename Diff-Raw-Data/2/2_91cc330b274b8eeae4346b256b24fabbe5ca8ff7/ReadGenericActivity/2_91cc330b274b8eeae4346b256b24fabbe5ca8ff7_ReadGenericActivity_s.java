 package com.cs261.vulnerapp;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.nfc.FormatException;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 
public class ReadUrlActivity extends Activity {
     /** Called when the activity is first created. */
   
   //NFC-related variables
   NfcAdapter mNfcAdapter;
   PendingIntent mNfcPendingIntent;
   IntentFilter[] mReadTagFilters;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
       
       super.onCreate(savedInstanceState);
       
       Tag myTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG); 
       
       Ndef myNDEF = Ndef.get(myTag);
       try {
         myNDEF.connect();
       } catch (IOException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
       }
       String data = null;      
       try {
         data = myNDEF.getNdefMessage().getRecords()[0].toString();
       } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         data = "ERROR";
       } catch (FormatException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         data = "ERROR";
       } catch (Exception e) {
         e.printStackTrace();
       }
      
       setContentView(R.layout.main);
       
       TextView tv1 = (TextView) findViewById(R.id.TextView01);
       tv1.setText(data);
 
   }
   
 }
