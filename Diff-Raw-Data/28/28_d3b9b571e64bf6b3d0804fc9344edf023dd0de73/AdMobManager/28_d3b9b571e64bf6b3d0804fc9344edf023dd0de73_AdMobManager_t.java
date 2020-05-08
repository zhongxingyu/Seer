 package gr.sullenart.ads;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.util.Log;
 import android.view.ViewGroup;
 
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class AdMobManager extends AdsNetwork implements AdListener {
 	
 	public AdMobManager(Activity activity,
 			AdsNetworkListener adsNetworklistener, String publisherId) {
 		super(activity, adsNetworklistener, publisherId);
 		
 		type = AdsNetworkType.AdMob;
 	}
 
 	private int adHeight = 0;
 	
	private AdView adView = null;
 	
 	public int getAdHeight() {
 		return adHeight;
 	}
 
 	@Override
 	public void addAdsView(ViewGroup layout) {
         int screenLayout = activity.getResources().getConfiguration().screenLayout;
         if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 4) {
         	adView = new AdView(activity, AdSize.IAB_LEADERBOARD, publisherId);
             adHeight = 90; // 728x90 size for xlarge screens (>= 960dp x 720dp)
            Log.d("AdMobManager", "Adding IAB LEADERBOARD " + 
            		((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)));
         }
         else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 3) {
         	adView = new AdView(activity, AdSize.IAB_BANNER , publisherId);
             adHeight = 75; // 468x60 size for large screens (>= 640dp x 480dp)
            Log.d("AdMobManager", "Adding IAB BANNER " + 
            		((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)));            
         }        
         else {
         	adView = new AdView(activity, AdSize.BANNER, publisherId);
             adHeight = 75; // 320x50 size for normal (>= 470dp x 320dp) and small screens
            Log.d("AdMobManager", "Adding BANNER " + 
            		((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)));            
         }        
         
         layout.addView(adView);
         AdRequest request = new AdRequest();
         request.addTestDevice(AdRequest.TEST_EMULATOR);
         request.addTestDevice("5EA5B374B0B6F1201A685AACAC300DDD");
         adView.loadAd(request);
 
         //Log.i(AdsManager.class.getName(), "Adding ads view");
 	}
 	
 	@Override
 	void removeAdsView(ViewGroup layout) {
        if (adView != null) {
            layout.removeView(adView);
            adHeight = 0;
            adView = null;
        }
 	}	
 
 	@Override
 	public void onDismissScreen(Ad arg0) {
 
 
 	}
 
 	@Override
 	public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
 		Log.e(AdMobManager.class.getName(), "Failed to receive ad (" + errorCode + ")");
 		adsNetworkListener.onAdFailed(type);
 	}
 
 	@Override
 	public void onLeaveApplication(Ad arg0) {
 
 
 	}
 
 	@Override
 	public void onPresentScreen(Ad arg0) {
 
 
 	}
 
 	@Override
 	public void onReceiveAd(Ad arg0) {
 		adsNetworkListener.onAdReceived(type);
 	}
 
 }
