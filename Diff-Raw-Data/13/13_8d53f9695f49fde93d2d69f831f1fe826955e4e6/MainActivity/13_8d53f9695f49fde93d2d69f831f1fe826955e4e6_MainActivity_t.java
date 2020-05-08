 package com.bfio.sample;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 import com.bfio.ad.BFIOErrorCode;
 import com.bfio.ad.BFIOInterstitial;
 import com.bfio.ad.model.BFIOInterstitalAd;
 
 public class MainActivity extends Activity implements
 		BFIOInterstitial.InterstitialListener, OnClickListener {
 
 	BFIOInterstitial interstitial;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		findViewById(R.id.request).setOnClickListener(this);
 		interstitial = new BFIOInterstitial(MainActivity.this, this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		interstitial.onDestroy();
 		super.onDestroy();
 	}
 
 	@Override
 	public void onInterstitialFailed(BFIOErrorCode errorCode) {
 		Toast.makeText(MainActivity.this, "Interstitial not received",
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onInterstitialClicked() {
 		Toast.makeText(MainActivity.this, "Interstitial Clicked",
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onInterstitialDismissed() {
 		Toast.makeText(MainActivity.this, "Interstitial dismissed",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onReceiveInterstitial(BFIOInterstitalAd ad) {
 		Toast.makeText(MainActivity.this, "Received interstitial",
 				Toast.LENGTH_SHORT).show();
 		interstitial.showInterstitial(ad);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.request:
 			// interactive
			interstitial.requestInterstitial(
					"e04fd6b0-4eb2-4dc8-b8d3-accfb7cf8043", // appID
					"e4471497-53ec-42f8-af58-cba7464d9e5a"); // adUnitId
 			
 			// non interactive
 
			// interstitial.requestInterstitial(
			// "fd298c64-e28b-4a28-f3b6-410d24be3b73", // appID
			// "2f529a17-97ff-4b34-b02d-a75cdaa0f1a9"); // adUnitId
 			Toast.makeText(MainActivity.this, "Interstitial request sent",
 					Toast.LENGTH_SHORT).show();
 			break;
 
 		default:
 			break;
 		}
 
 	}
 
 	@Override
 	public void onInterstitialCompleted() {
 		Toast.makeText(MainActivity.this, "Interstitial play completed",
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onInterstitialStarted() {
 		Toast.makeText(MainActivity.this, "Interstitial started",
 				Toast.LENGTH_SHORT).show();
 	}
 
 }
