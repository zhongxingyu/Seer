 package com.project2.delivery_system;
 
 import com.project2.delivery_system.OrderViewActivity.Uploader;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.os.Parcelable;
 import android.content.IntentFilter;
 import android.view.View.OnClickListener;
 
 public class NFCActivity extends Activity implements OnClickListener{
     NfcAdapter mNfcAdapter;
     PendingIntent mPendingIntent; 
     TextView textView;
     private IntentFilter[] mFilters;
     private DeliveryApplication delivery;
     
     private final String MESSAGE_ID="com.project2.delivery_system.NFCActivity.MESSAGE";
     
     private String orderID;
     private String orderStatus;
     private String orderUser;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_nfc);
         
         delivery = (DeliveryApplication) getApplication();
         
         textView=(TextView)findViewById(R.id.textView_nfcDisplay);
      
         Button btnBack=(Button)findViewById(R.id.button_back);
         btnBack.setOnClickListener(this);
         
         mPendingIntent= PendingIntent.getActivity(this, 0, 
                             new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),0);
         IntentFilter ndef=new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
         
         try{
             ndef.addDataType("*/*");
         }catch (IntentFilter.MalformedMimeTypeException e){
             throw new RuntimeException("fail",e);
         }
         
         mFilters=new IntentFilter[]{ndef};
         // Check for available NFC Adapter
         
         mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter == null) {
             System.out.println("NFC on create");
             finish();
             return;
         }
         
         intentHandler(getIntent());
     }
     
     @Override
     protected void onPause() {
         // TODO Auto-generated method stub
         super.onPause();
         mNfcAdapter.disableForegroundNdefPush(this);
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         intentHandler(intent);
         setIntent(intent);
         
         super.onNewIntent(intent);
     }
 
     @Override
     protected void onResume() {
         // TODO Auto-generated method stub 
         
         // check if NFC is enabled on device
         if(!mNfcAdapter.isEnabled()){
             //TODO enable nfc
             System.out.println("NFC not enabled!");
         }
         mNfcAdapter.enableForegroundDispatch(this,  mPendingIntent, mFilters, null);   
         
         // if identity is CUSTOMER, start transmission
         if(delivery.getIdentity()==DeliveryApplication.Identity.CUSTOMER){
             System.out.println("Start Transmission..");
             
             // formatting message
             NdefMessage msg=new NdefMessage(
                     new NdefRecord[]{     
                             new NdefRecord(
                                 NdefRecord.TNF_WELL_KNOWN,
                                 NdefRecord.RTD_TEXT,
                                 MESSAGE_ID.getBytes(),
                                 new String("aaa").getBytes() ),
                             new NdefRecord(
                                 NdefRecord.TNF_WELL_KNOWN,
                                 NdefRecord.RTD_TEXT,
                                 new String("orderID").getBytes(),
                                 orderID.getBytes() ),
                             new NdefRecord(
                                 NdefRecord.TNF_WELL_KNOWN,
                                 NdefRecord.RTD_TEXT,
                                 new String("orderStatus").getBytes(),
                                 orderStatus.getBytes() ),
                             new NdefRecord(
                                 NdefRecord.TNF_WELL_KNOWN,
                                 NdefRecord.RTD_TEXT,
                                 new String("orderUser").getBytes(),
                                 orderUser.getBytes() )
                             }
                     );
             mNfcAdapter.enableForegroundNdefPush(this, msg);
         }
         
         super.onResume();
     }
 
     
     public void onClick(View v){
         
         if(v.getId()==R.id.button_back){
             finish();
             
         }
     }
 
     private void intentHandler(Intent intent){
         // TODO Auto-generated method stub
         
         Bundle bundle = intent.getExtras();
         if(bundle!=null){
             // set current order information
            if(bundle.getString("orderID")!=null) {
                 orderID = bundle.getString("orderID");
                 orderStatus = bundle.getString("orderStatus");
                 orderUser = bundle.getString("orderUser");
             }
         }
         
         String action = intent.getAction();
         if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
             System.out.println("NDEF Received!");
             
             textView=(TextView)findViewById(R.id.textView_nfcDisplay);
             
             Parcelable[] rawMsgs=intent.getParcelableArrayExtra(
                     NfcAdapter.EXTRA_NDEF_MESSAGES);
             NdefMessage msg=(NdefMessage) rawMsgs[0];
             
             String msg_id=new String(msg.getRecords()[0].getId());
             
             // if the id is good
             if(msg_id.contentEquals(MESSAGE_ID) )
             {
                 String receivedOrderID = new String(msg.getRecords()[1].getPayload());
                 String receivedOrderStatus = new String(msg.getRecords()[2].getPayload());
                 String receivedOrderUser = new String(msg.getRecords()[3].getPayload());
                 
                 System.out.println("Received! id: "+receivedOrderID+" Status: "+receivedOrderStatus+" User: "+receivedOrderUser);
                 // if the user identity is courier, sent data base upload request
                 if(delivery.getIdentity()==DeliveryApplication.Identity.COURIER &&
                         receivedOrderStatus.contentEquals(Order.STATUS_COUR_CONFIRMED)){
                     new Uploader().execute(receivedOrderID);
                     textView.setText(new String("Transaction Completed: ")+receivedOrderID);
                 }
             }
         }
     } 
     
     /***
      * Asynchronously posts to server, avoid blocking UI thread. The first data
      * type is used by doInBackground, the second by onProgressUpdate, and the
      * third by onPostExecute.
      */
     class Uploader extends AsyncTask<String, Integer, String> {
 
         // doInBackground() is the callback that specifies the actual work to be
         // done on the separate thread, as if it's executing in the background.
         @Override
         protected String doInBackground(String... in) {
             try {
                 
                 delivery.getWebAccessor().orderTransactionConfirm(in[0]);
                 return null;
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return null;    
         }
 
         // onProgressUpdate() is called whenever there's progress in the task
         // execution. The progress should be reported from the doInBackground() call.
         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);
         }
 
         // onPostExecute() is called when our task completes. This is our
         // callback method to update the user interface and tell the user 
         // that the task is done.
         @Override
         protected void onPostExecute(String result) {
         }
     }
 }
