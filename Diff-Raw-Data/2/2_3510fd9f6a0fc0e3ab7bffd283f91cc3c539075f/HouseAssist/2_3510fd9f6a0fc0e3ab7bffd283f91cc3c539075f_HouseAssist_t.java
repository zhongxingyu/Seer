 package com.markchung.HouseAssist;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.markchung.library.*;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 
 public class HouseAssist extends com.markchung.library.MainTabActivity {
 
 	public static final String TAG = "HouseAssist";
 
 	class MyAdRequest extends com.markchung.library.AdRequest{
		private static final String myAdID = "ca-app-pub-3822862541892480/5314153831";
 		private static final String myTestDevice = "BA76119486D364D047D0C789B4F61E46";
 		private AdView adview;
 
 		public MyAdRequest(){
 			
 		}
 		
 		public com.markchung.library.AdRequest getAd(){
 			MyAdRequest ad = new MyAdRequest();
 			return (com.markchung.library.AdRequest)ad;
 		}
 
 		@Override
 		public void CreateAdRequest(Activity activity,
 				LinearLayout view) {
 			adview = new AdView(activity, AdSize.BANNER,
 					myAdID);
 			view.addView(adview);
 			AdRequest adRequest = new AdRequest();
 			if(BuildConfig.DEBUG){
 				adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
 				adRequest.addTestDevice(myTestDevice);
 			}
 			adview.loadAd(adRequest);
 		}
 
 		@Override
 		public void Destroy() {
 			adview.destroy();
 		}		
 	}
 
 	public static TabHost tabHost;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		MainTabActivity.TAG = TAG;
 		setAdRequest(new MyAdRequest());
 		super.onCreate(savedInstanceState);
 	}
 }
