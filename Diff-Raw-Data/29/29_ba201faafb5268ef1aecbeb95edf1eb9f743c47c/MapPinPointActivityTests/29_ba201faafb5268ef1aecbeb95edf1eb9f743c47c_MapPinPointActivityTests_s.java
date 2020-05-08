 package com.davelabs.wakemehome.test.functional;
 
 import android.app.Dialog;
 import android.app.Instrumentation;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 
 import com.davelabs.wakemehome.MapPinPointActivity;
 import com.davelabs.wakemehome.R;
 import com.davelabs.wakemehome.test.helpers.ViewHelper;
 import com.davelabs.wakemehome.test.helpers.WifiHelper;
 
 public class MapPinPointActivityTests extends ActivityInstrumentationTestCase2<MapPinPointActivity> {
 
 	private Instrumentation _ins;
 	
 	public MapPinPointActivityTests() {
 		super(MapPinPointActivity.class);
 		
 	}
 	
 	public void setUp()
 	{
 		_ins = getInstrumentation();
 	}
 	
 	public void testUnfindableAddress() {
 		setIntentSearchQuery("azazazazazazazaza");
 		
 		MapPinPointActivity a = (MapPinPointActivity) getActivity();
 	
 		View overlay = (View) a.findViewById(R.id.searchProgressOverlay);
 		ViewHelper.assertViewHides(overlay, 5);
 			
 		Dialog d = a.getSearchQueryNotFoundDialog();
 		assertTrue(d.isShowing());
 	}
 	
 	public void testNoInternetWhenLookingUpAddress() {
 		setIntentSearchQuery("EC1N 8NX");
 		MapPinPointActivity a = (MapPinPointActivity) getActivity();
 		
 		WifiHelper wifi = new WifiHelper(a);
 		wifi.setWifiOff();
 		
 		View overlay = (View) a.findViewById(R.id.searchProgressOverlay);
 		ViewHelper.assertViewHides(overlay, 5);
 			
		Dialog d = a.getSearchQueryLookupFailed();
 		assertTrue(d.isShowing());
 		
 		View loadingOverlay = (View) a.findViewById(R.id.searchProgressOverlay);
 		assertTrue(loadingOverlay.isShown());
 	}
 	
 	private void setIntentSearchQuery(String query) {
 		Intent intent = new Intent();
 		intent.putExtra("searchQuery", query);
 		setActivityIntent(intent);
 	}
 	
	private void testNoInternetPolls {
		MapPinPointActivity a = (MapPinPointActivity) getActivity();
		a.getSearchQueryLookupFailed();
		
		
	}
 }
