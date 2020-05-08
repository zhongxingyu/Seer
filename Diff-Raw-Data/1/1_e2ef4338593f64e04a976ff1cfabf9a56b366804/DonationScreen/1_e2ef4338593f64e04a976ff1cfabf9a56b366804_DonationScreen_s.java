 /*
  * Copyright (c) 2012 Joe Rowley
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.mobileobservinglog;
 
 import com.mobileobservinglog.support.billing.BillingHandlerResult;
 import com.mobileobservinglog.support.billing.DonationBillingHandler;
 import com.mobileobservinglog.support.billing.Purchase;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DonationScreen extends ActivityBase {
 	//gather resources
 	RelativeLayout body;
 	Button oneDollarButton;
 	Button twoDollarsButton;
 	Button fiveDollarsButton;
 	
 	boolean setupFinished;
 	boolean purchaseSuccessful;
 	boolean resultHandled;
 	
 	RelativeLayout alertModal;
 	TextView alertText;
 	Button alertOk;
 	Button alertCancel;
 	
 	String billingKey;
 	DonationBillingHandler donationHandler;
 	
 	static final String SKU_ONE = "one";
 	static final String SKU_TWO = "two";
 	static final String SKU_FIVE = "five";
 
     // (arbitrary) request code for the purchase flow
     static final int RC_REQUEST = 10001;
 	
 	@Override
     public void onCreate(Bundle icicle) {
 		Log.d("JoeDebug", "InfoScreen onCreate. Current session mode is " + settingsRef.getSessionMode());
         super.onCreate(icicle);
         
         setupFinished = false;
         
         billingKey = this.getApplication().getResources().getString(R.string.base64Key);
         donationHandler = new DonationBillingHandler(this, billingKey);
 		Log.d("InAppPurchase", "Starting Setup from donation screen");
         donationHandler.startSetup(new DonationBillingHandler.OnSetupFinishedListener() {
             public void onSetupFinished(BillingHandlerResult result) {
                 if (!result.isSuccess()) {
         			Log.d("InAppPurchase", "Setup failed -- donation screen");
                     return;
                 } else {
         			Log.d("InAppPurchase", "Setup finished -- donation screen");
                 	setupFinished = true;
                 }
             }
         });
 
 		customizeBrightness.setDimButtons(settingsRef.getButtonBrightness());
         
         //setup the layout
         setContentView(settingsRef.getDonationScreenLayout());
         body = (RelativeLayout)findViewById(R.id.donation_root); 
 	}
 	
 	@Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if(donationHandler != null)
         	donationHandler.dispose();
         donationHandler = null;
     }
 
     //When we resume, we need to make sure we have the right layout set, in case the user has changed the session mode.
     @Override
     public void onResume() {
 		Log.d("JoeDebug", "InfoScreen onResume. Current session mode is " + settingsRef.getSessionMode());
         super.onResume();
         setLayout();
         if(!resultHandled) {
         	if(purchaseSuccessful) {
         		showThankYou();
         	} else {
         		showError();
         	}
         }
     }
 	
     //Used by the Toggle Mode menu item method in ActivityBase. Reset the layout and force the redraw
 	@Override
 	public void setLayout(){
 		setContentView(settingsRef.getDonationScreenLayout());
 		super.setLayout();
 		findButtonSetListener();
 		findModalElements();
 		body.postInvalidate();
 	}
 	
 	private void findButtonSetListener(){
 		oneDollarButton = (Button)findViewById(R.id.one_dollar);
 		oneDollarButton.setOnClickListener(oneDollarDonation);
 		twoDollarsButton = (Button)findViewById(R.id.two_dollars);
 		twoDollarsButton.setOnClickListener(twoDollarDonation);
 		fiveDollarsButton = (Button)findViewById(R.id.five_dollars);
 		fiveDollarsButton.setOnClickListener(fiveDollarDonation);
 	}
 	
 	private void findModalElements() {
 		alertModal = (RelativeLayout)findViewById(R.id.alert_modal);
 		alertText = (TextView)findViewById(R.id.alert_main_text);
 		alertOk = (Button)findViewById(R.id.alert_ok_button);
 		alertCancel = (Button)findViewById(R.id.alert_cancel_button);
 	}
 	
 	private void prepForModal() {
 		RelativeLayout blackOutLayer = (RelativeLayout)findViewById(R.id.settings_fog);
 		ListView listView = getListView();
 		
 		body.setEnabled(false);
 		listView.setEnabled(false);
 		oneDollarButton.setEnabled(false);
 		twoDollarsButton.setEnabled(false);
 		fiveDollarsButton.setEnabled(false);
 		blackOutLayer.setVisibility(View.VISIBLE);
 		
 		alertModal.setVisibility(View.VISIBLE);
 		alertText.setVisibility(View.VISIBLE);
 		alertOk.setVisibility(View.GONE);
 		alertCancel.setVisibility(View.GONE);
 	}
 	
 	private void tearDownModal() {
 		RelativeLayout blackOutLayer = (RelativeLayout)findViewById(R.id.settings_fog);
 		ListView listView = getListView();
 		
 		body.setEnabled(true);
 		listView.setEnabled(true);
 		oneDollarButton.setEnabled(true);
 		twoDollarsButton.setEnabled(true);
 		fiveDollarsButton.setEnabled(true);
 		blackOutLayer.setVisibility(View.GONE);
 		
 		alertModal.setVisibility(View.GONE);
 		alertText.setVisibility(View.GONE);
 		alertOk.setVisibility(View.GONE);
 		alertCancel.setVisibility(View.GONE);
 	}
 	
 	protected final Button.OnClickListener oneDollarDonation = new Button.OnClickListener(){
     	public void onClick(View view){
     		// launch the purchase UI flow.
             // We will be notified of completion via mPurchaseFinishedListener
     		if(setupFinished) {
     			Log.d("InAppPurchase", "Starting One Dollar Workflow");
 	            showInProgress();
     			Log.d("InAppPurchase", "Modal shown, calling launch purchase flow");
 	            donationHandler.launchPurchaseFlow(DonationScreen.this, SKU_ONE, RC_REQUEST, mPurchaseFinishedListener);
     		} else {
     			showError();
     		}
     	}
     };
 	
 	protected final Button.OnClickListener twoDollarDonation = new Button.OnClickListener(){
     	public void onClick(View view){
     		// launch the purchase UI flow.
             // We will be notified of completion via mPurchaseFinishedListener
     		if(setupFinished) {
     			Log.d("InAppPurchase", "Starting Two Dollar Workflow");
 	            showInProgress();
     			Log.d("InAppPurchase", "Modal shown, calling launch purchase flow");
 	            donationHandler.launchPurchaseFlow(DonationScreen.this, SKU_TWO, RC_REQUEST, mPurchaseFinishedListener);
     		} else {
     			showError();
     		}
     	}
     };
 	
 	protected final Button.OnClickListener fiveDollarDonation = new Button.OnClickListener(){
     	public void onClick(View view){
     		// launch the purchase UI flow.
             // We will be notified of completion via mPurchaseFinishedListener
     		if(setupFinished) {
     			Log.d("InAppPurchase", "Starting Three Dollar Workflow");
 	            showInProgress();
     			Log.d("InAppPurchase", "Modal shown, calling launch purchase flow");
 	            donationHandler.launchPurchaseFlow(DonationScreen.this, SKU_FIVE, RC_REQUEST, mPurchaseFinishedListener);
     		} else {
     			showError();
     		}
     	}
     };
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("InAppPurchase", "onActivityResult called -- Donation Screen");
         // Pass on the activity result to the helper for handling
         if (!donationHandler.handleActivityResult(requestCode, resultCode, data)) {
     		Log.d("InAppPurchase", "Uh Oh, something went bad. -- Donation Screen");
             // not handled, so handle it ourselves (here's where you'd
             // perform any handling of activity results not related to in-app
             // billing...
             super.onActivityResult(requestCode, resultCode, data);
         }
         else {
             Log.d("JoeDebug", "onActivityResult handled by BillingHandlerResult.");
         }
     }
     
     // Callback for when a purchase is finished
     DonationBillingHandler.OnPurchaseFinishedListener mPurchaseFinishedListener = new DonationBillingHandler.OnPurchaseFinishedListener() {
         public void onPurchaseFinished(BillingHandlerResult result, Purchase purchase) {
             Log.d("JoeDebug", "Purchase finished: " + result + ", purchase: " + purchase);
             if (result.isFailure()) {
                 // Oh noes!
                 purchaseSuccessful = false;
                 resultHandled = false;
             	showError();
                 return;
             }
 
             Log.d("JoeDebug", "Purchase successful.");
             purchaseSuccessful = true;
             resultHandled = false;
             showThankYou(); //Just in case we dont' go through onResume();
         }
     };
     
     private void showInProgress() {
     	prepForModal();
     	alertText.setText("Your Purchase is being handled by the Google Play Store. Please wait.");
     }
     
     private void showThankYou() {
     	prepForModal();
     	alertText.setText("Thank you for your generous donation. Enjoy the observing log.");
     	alertOk.setVisibility(View.VISIBLE);
     	alertOk.setOnClickListener(dismissModal);
     }
     
     private void showError() {
     	prepForModal();
     	alertText.setText("There was a problem with your donation. Please try again.");
     	alertOk.setVisibility(View.VISIBLE);
     	alertOk.setOnClickListener(dismissModal);
     }
     
     protected final Button.OnClickListener dismissModal = new Button.OnClickListener() {
 		public void onClick(View view){
 			tearDownModal();
 			resultHandled = true;
         }
     };
 }
