 package io.triangle.reader.sample;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import io.triangle.reader.PaymentCard;
 import io.triangle.reader.ReaderActivity;
 
 /**
  * This class demonstrates how scanning payment cards via NFC is done via the Triangle APIs. It's important to note
  * that this class extends {@link ReaderActivity} which contains the necessary logic to interact with an RFID enabled
  * payment card. This class simply presents the obtained information to the user.
  */
 public class ReaderActivitySample extends ReaderActivity
 {
     Header header;
     LinearLayout root;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         this.setContentView(R.layout.main);
 
         this.header = (Header)this.findViewById(R.id.main_header);
         this.root = (LinearLayout)this.findViewById(R.id.main_LinearLayout_root);
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         // Ensure that the device's NFC sensor is on
         NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
         if (nfcAdapter != null && !nfcAdapter.isEnabled())
         {
             // Alert the user that NFC is off
             new AlertDialog.Builder(this)
                     .setTitle("NFC Sensor Turned Off")
                     .setMessage("In order to use this application, the NFC sensor must be turned on. Do you wish to turn it on?")
                     .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener()
                     {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i)
                         {
                             // Send the user to the settings page and hope they turn it on
                             if (android.os.Build.VERSION.SDK_INT >= 16)
                             {
                                 startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                             }
                             else
                             {
                                 startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                             }
                         }
                     })
                     .setNegativeButton("Do Nothing", new DialogInterface.OnClickListener()
                     {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i)
                         {
                             // Do nothing
                         }
                     })
                     .show();
         }
     }
 
     /**
      * This method is called before the card is scanned and allows you to react to the event.
      */
     @Override
     protected void onPreNFCExecute()
     {
         // Indicate to the user that card is being scanned
         this.header.minimize();
         this.header.startFading();
     }
 
     /**
      * This method is called after the card has been processed via NFC.
      * @param cardInformation will contain information obtained from the NFC card. If there was a failure processing
      *                        the card, this object will be null.
      */
     @Override
    protected void onPostNFCExecute(PaymentCard cardInformation)
     {
         // Indicate to the user that the scanning process is finished
         this.header.stopFading();
 
         if (cardInformation == null)
         {
            // The card could not be scanned properly. Inform the user as such.
            Toast.makeText(this, "Scanning failed, please try again.", Toast.LENGTH_SHORT).show();
         }
         else
         {
             // Card was successfully read, create a new card view and add it to the layout so that the user can see the
             // card information
             int index = this.root.getChildCount();
             this.root.addView(
                     new CardView(this.root, cardInformation, this),
                     index,
                     new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         }
     }
 
     /**
      * Allows the user to recommend this application to a friend.
      */
     public void recommend(final View view)
     {
         Intent recommendIntent = new Intent(Intent.ACTION_SEND);
 
         recommendIntent.setType("text/plain");
 
         recommendIntent.putExtra(Intent.EXTRA_TEXT, "Hey!\r\nCheckout this cool app, it scans your credit card when you tap it to your phone.\r\nhttp://play.google.com/store/apps/details?id=io.triangle.reader.sample");
         recommendIntent.putExtra(Intent.EXTRA_SUBJECT, "App that scans your credit card");
 
         this.startActivity(Intent.createChooser(recommendIntent, "Share App"));
     }
 }
