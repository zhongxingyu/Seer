 package com.worthwhilegames.cardgames.shared;
 
 import android.app.Activity;
 import android.view.View;
 import com.google.android.gms.ads.AdRequest;
 import com.google.android.gms.ads.AdView;
 import com.worthwhilegames.cardgames.R;
 
 /**
  * All activities should extend AdActivity, so that
  * it is trivial to disable ads on an install.
  *
  * @author breber
  */
 public abstract class AdActivity extends Activity {
 
     private boolean mAdsHidden = false;
 
     @Override
     protected void onStart() {
         super.onStart();
 
         if (mAdsHidden) {
             View v = findViewById(R.id.adView);
 
             if (v != null) {
                 v.setVisibility(View.GONE);
             }
         } else {
             AdView adView = (AdView)this.findViewById(R.id.adView);
            if (adView != null) {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();
                adView.loadAd(adRequest);
            }
         }
     }
 
     @Override
     protected void onPause() {
         if (!mAdsHidden) {
             AdView adView = (AdView)this.findViewById(R.id.adView);
            if (adView != null) {
                adView.pause();
            }
         }
 
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (!mAdsHidden) {
             AdView adView = (AdView)this.findViewById(R.id.adView);
            if (adView != null) {
                adView.resume();
            }
         }
     }
 
     @Override
     protected void onDestroy() {
         if (!mAdsHidden) {
             AdView adView = (AdView)this.findViewById(R.id.adView);
            if (adView != null) {
                adView.destroy();
            }
         }
 
         super.onDestroy();
     }
 }
