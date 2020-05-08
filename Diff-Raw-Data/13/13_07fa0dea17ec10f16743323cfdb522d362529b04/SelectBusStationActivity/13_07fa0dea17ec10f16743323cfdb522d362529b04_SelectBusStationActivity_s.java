 package com.papagiannis.tuberun;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.papagiannis.tuberun.fetchers.*;
 
 public class SelectBusStationActivity extends MeMapActivity implements OnClickListener, LocationListener, Observer{
 	StationsBusFetcher fetcher;
 	boolean has_moved=false;
 	boolean has_moved_accurate=false;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         
         fetcher=new StationsBusFetcher(this);
         fetcher.registerCallback(this);
         
 		showDialog(0);
 		
 		if (lastKnownLocation!=null) {
 			GeoPoint gp=new GeoPoint((int)(lastKnownLocation.getLatitude()*1000000), (int)(lastKnownLocation.getLongitude()*1000000));
 			mapController.setCenter(gp);
 			fetcher.setLocation(lastKnownLocation);
 			fetcher.update();
 		}
 		else {
 			mapController.setCenter(gp_london);
 		}
     }
     
     private Dialog wait_dialog;
     @Override
     protected Dialog onCreateDialog(int id) {
     	wait_dialog = ProgressDialog.show(this, "", 
                 "Fetching location. Please wait...", true);
     	return wait_dialog;
     }
     
     
     
     @Override
 	protected void onPause() {
 		super.onPause();
 		fetcher.abort();
 	}
 
 	@Override
     protected boolean isRouteDisplayed() {
         return true;
     }
     
     public void onClick (View v) {
     	Intent i=null;
     	switch (v.getId()) {
     	case R.id.button_status:
     		i=new Intent(this, StatusActivity.class);
     		startActivity(i);
     		break;
     	case R.id.button_departures:
         	i=new Intent(this, SelectLineActivity.class);
         	i.putExtra("type", "departures");
         	startActivity(i);
         	break;
         }
     }
     
 
 	ArrayList<BusStation> prev_result=new ArrayList<BusStation>();
     /** Called when the background thread has finished the calculation of nearby stations **/
 	@Override
 	public void update() {
 		wait_dialog.dismiss();
 		ArrayList<BusStation> result=fetcher.getResult();
 		if (prev_result.size()!=result.size()) {
 			List<Overlay> mapOverlays = mapView.getOverlays();
 			//mapOverlays.clear();
 	        Drawable drawable = this.getResources().getDrawable(R.drawable.buses);
 	        BusStationsOverlay itemizedoverlay = new BusStationsOverlay(drawable, this);
 	        
 	        for (BusStation s: result){
 	        	 GeoPoint point = new GeoPoint(s.getLatitude(),s.getLongtitude());
 	             OverlayItem overlayitem = new OverlayItem(point, s.getCode(), s.getName()+" (moving "+s.getHeading()+")");
 	             itemizedoverlay.addOverlay(overlayitem);
 	        }
 	        mapOverlays.add(itemizedoverlay);
 	        mapView.postInvalidate();
 		}
 		
 	}
 
 	
 }
