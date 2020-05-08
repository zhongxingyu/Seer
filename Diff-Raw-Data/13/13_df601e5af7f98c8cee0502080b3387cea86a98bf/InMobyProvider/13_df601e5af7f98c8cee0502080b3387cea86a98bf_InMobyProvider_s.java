 package com.anjlab.ads.integrator;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.location.Location;
 import android.util.Log;
 import android.widget.LinearLayout;
 
 import com.inmobi.androidsdk.EducationType;
 import com.inmobi.androidsdk.EthnicityType;
 import com.inmobi.androidsdk.GenderType;
 import com.inmobi.androidsdk.InMobiAdDelegate;
 import com.inmobi.androidsdk.impl.InMobiAdView;
 
 public class InMobyProvider extends AbstractAdProvider implements InMobiAdDelegate {
 
 	private String publisherId;	
 	private InMobiAdView view;
 	
 	public InMobyProvider(String pubId){
 		this.publisherId = pubId;
 	}
 	
 	@Override
 	public void tryLoadAd(Activity actv, LinearLayout layout) {
 		view = InMobiAdView.requestAdUnitWithDelegate(actv.getApplicationContext(), this, actv, InMobiAdDelegate.INMOBI_AD_UNIT_320X48);
 		view.loadNewAd();
 	}
 
 	@Override
 	public void adRequestCompleted(InMobiAdView arg0) {
		Log.i("Ads", "got inmoby ad!");		
 	}
 
 	@Override
 	public void adRequestFailed(InMobiAdView arg0) {
 		Log.e("Ads", "failed to load inmoby ads");
 		onLoadFailed();
 	}
 
 	@Override
 	public int age() {
 		// TODO Auto-generated method stub
 		return -1;
 	}
 
 	@Override
 	public String areaCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Location currentLocation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Date dateOfBirth() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public EducationType education() {
 		// TODO Auto-generated method stub
 		return EducationType.Edu_None;
 	}
 
 	@Override
 	public EthnicityType ethnicity() {
 		// TODO Auto-generated method stub
 		return EthnicityType.Eth_None;
 	}
 
 	@Override
 	public GenderType gender() {
 		// TODO Auto-generated method stub
 		return GenderType.G_None;
 	}
 
 	@Override
 	public int income() {
 		// TODO Auto-generated method stub
 		return -1;
 	}
 
 	@Override
 	public String interests() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isLocationInquiryAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isPublisherProvidingLocation() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public String keywords() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String postalCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String searchString() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String siteId() {
 		// TODO Auto-generated method stub
 		return publisherId;
 	}
 
 	@Override
 	public boolean testMode() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void stop() {
		// TODO Auto-generated method stub
 	}
 
 }
