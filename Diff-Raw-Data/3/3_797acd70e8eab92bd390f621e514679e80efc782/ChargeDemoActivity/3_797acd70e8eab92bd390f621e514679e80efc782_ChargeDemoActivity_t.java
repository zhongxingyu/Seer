 //
 // ChargeDemoActivity.java
 // Sample Code
 //
 // An example activity for a simple sort of application which might
 // make use of ChargeRequest and ChargeResponse for processing credit
 // card charges using Inner Fence's Credit Card Terminal for Android.
 //
 // You may license this source code under the MIT License. See COPYING.
 //
 // Copyright (c) 2009 Inner Fence, LLC
 //
 package com.innerfence.chargedemo;
 
 import com.innerfence.chargeapi.ChargeRequest;
 import com.innerfence.chargeapi.ChargeResponse;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 
 public class ChargeDemoActivity extends Activity
 {
     // Here we set up an ChargeRequest object and submit it in order
     // to invoke Credit Card Terminal.
     protected final View.OnClickListener _chargeButtonClickListener =
         new View.OnClickListener()
         {
             @Override
             public final void onClick( View v )
             {
                 // Create the ChargeRequest using the default
                 // constructor.
                 ChargeRequest chargeRequest = new ChargeRequest();
 
                 // 2-way Integration
                 //
                 // Credit Card Terminal will return to the activity
                 // that started it when the transaction is complete.
                 //
                 // You can also include app-specific parameters by
                 // calling the setExtraParams() method and passing in
                 // a Bundle object. The extra params will be
                 // accessible to you when we return to your app.
                 //
                 // In this sample, we include an app-specific
                 // "record_id" parameter set to the value 123. You may
                 // call extra parameters anything you like.
                 Bundle extraParams = new Bundle();
                 extraParams.putString( "record_id", "123" );
                 chargeRequest.setExtraParams( extraParams );
 
                 // Finally, we can supply customer and transaction
                 // data so that it will be pre-filled for submission
                 // with the charge.
                 chargeRequest.setAddress("123 Test St");
                 chargeRequest.setAmount("50.00");
                 chargeRequest.setCurrency("USD");
                 chargeRequest.setCity("Nowhereville");
                 chargeRequest.setCompany("Company Inc");
                 chargeRequest.setCountry("US");
                 chargeRequest.setDescription("Test transaction");
                 chargeRequest.setEmail("john@example.com");
                 chargeRequest.setFirstName("John");
                 chargeRequest.setInvoiceNumber("321");
                 chargeRequest.setLastName("Doe");
                 chargeRequest.setPhone("555-1212");
                 chargeRequest.setState("HI");
                 chargeRequest.setZip("98021");
 
                 // Submitting the request will launch Credit Card
                 // Terminal from the passed in Activity
                 //
                 // If you want to handle the case where Credit Card
                 // Terminal is not installed, you can always check
                 // anytime by calling ChargeRequest.IsAppInstalled().
                 // Otherwise, we'll show an alert telling the user
                 // that Credit Card Terminal is not installed and
                 // provide a link to install it.
                 chargeRequest.submit( ChargeDemoActivity.this );
             }
         };
 
     @Override
     public void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
         setContentView( R.layout.main );
 
         Button chargeButton = (Button)findViewById( R.id.charge_button );
         chargeButton.setOnClickListener( _chargeButtonClickListener );
     }
 
     @Override
     public void onActivityResult( int requestCode, int resultCode, Intent data )
     {
         super.onActivityResult( requestCode, resultCode, data );
 
         // When the transaction is complete, the calling activity's
         // onActivityResult() method will be called. You can validate
         // that it's returning from our app by confirming the request
         // code matches ours.
         if( requestCode == ChargeRequest.CCTERMINAL_REQUEST_CODE )
         {
             ChargeResponse chargeResponse = new ChargeResponse( data );
 
             String message;
             String recordId = null;
 
             Bundle extraParams = chargeResponse.getExtraParams();
             if( null != extraParams )
             {
                 recordId = chargeResponse.getExtraParams().getString("record_id");
             }
 
             // You may want to perform different actions based on the
             // response code. This example shows an alert with the
             // response data when the charge is approved.
             if ( chargeResponse.getResponseCode() == ChargeResponse.Code.APPROVED )
             {
                 message = String.format(
                     "Charged!\n\n" +
                     "Record: %s\n" +
                     "Amount: %s %s\n" +
                     "Card Type: %s\n" +
                     "Redacted Number: %s",
                     recordId,
                     chargeResponse.getAmount(),
                     chargeResponse.getCurrency(),
                     chargeResponse.getCardType(),
                     chargeResponse.getRedactedCardNumber()
                 );
             }
             else // other response code values are documented in ChargeResponse.h
             {
                 message = String.format(
                     "Not Charged!\n\n" +
                     "Record: %s\n" +
                     "Code: %s\n" +
                     "Error Message: %s",
                     recordId,
                     chargeResponse.getResponseCode(),
                     chargeResponse.getErrorMessage()
                 );
             }
 
             // Generally you would do something app-specific here,
             // like load the record specified by recordId, record the
             // success or failure, etc. Since this sample doesn't
             // actually do much, we'll just pop an alert.
             Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
             toast.show();
         }
     }
 }
