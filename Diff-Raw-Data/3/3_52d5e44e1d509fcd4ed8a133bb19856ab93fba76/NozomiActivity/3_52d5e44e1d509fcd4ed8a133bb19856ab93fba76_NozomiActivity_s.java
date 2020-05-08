 package com.caesars.nozomi;
 
 import android.os.Bundle;
 
 import com.tapjoy.TapjoyConnect;
 
 import com.adeven.adjustio.AdjustIo;
 import com.amazon.inapp.purchasing.PurchasingManager;
 import com.caesars.lib.CaesarsActivity;
 
 import com.liyong.iap.AmazonIAP;
 import com.caesars.nozomiAmz.R;
 
 public class NozomiActivity extends CaesarsActivity {
 	AmazonIAP amazon;
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		//run on UI
 		TapjoyConnect.requestTapjoyConnect(this, "916fda06-a238-4541-829a-c055e6a8a2bc", "7UICyQW1iI8JHX8D72m9");
 		//TapjoyConnect.getTapjoyConnectInstance().showOffers();
 	}
 	
     static {
         System.loadLibrary("cocos2dlua");
     }
     @Override 
     protected void onStart() {
     	super.onStart();
     	amazon = new AmazonIAP(this);
     	PurchasingManager.registerObserver(amazon);
     }
     @Override
     protected void onPause() {
         super.onPause();
         //AdjustIo.onPause();
         TapjoyConnect.getTapjoyConnectInstance().appPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         //AdjustIo.onResume(getResources().getString(R.string.sg_adjust_token), this);
         TapjoyConnect.getTapjoyConnectInstance().appResume();
     }
 }
