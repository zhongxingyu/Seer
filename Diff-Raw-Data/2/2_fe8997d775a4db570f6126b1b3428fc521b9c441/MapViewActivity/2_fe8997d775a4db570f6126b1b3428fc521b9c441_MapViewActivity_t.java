 package uk.ac.bbk.dcs.ecoapp.activity;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.bbk.dcs.ecoapp.R;
 import uk.ac.bbk.dcs.ecoapp.SiteItemizedOverlay;
 import uk.ac.bbk.dcs.ecoapp.SiteOverlayItem;
 import uk.ac.bbk.dcs.ecoapp.db.EcoDatabaseHelper;
 import uk.ac.bbk.dcs.ecoapp.db.Site;
 import uk.ac.bbk.dcs.ecoapp.utility.AsynchImageLoader;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 
 public class MapViewActivity extends  MapActivity {
 	/** List of known Sites */
 	private List<Site> 			siteList_;
 
 
 	//private List<SiteOverlayItem> siteItems;
 	private MapView mapView;	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map_view);
 		mapView = (MapView) findViewById(R.id.map);  		
 		mapView.setBuiltInZoomControls(true);
 		initMapView();
 		mapSite();
 	}
 
 	private void initMapView() {
 		EcoDatabaseHelper dbHelper = new EcoDatabaseHelper(this); 
 		siteList_ =  dbHelper.getSites();
 	}
 
 
 	protected void mapSite() { 
 		ArrayList<SiteOverlayItem> siteItems = new ArrayList<SiteOverlayItem>();
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		Site siteToGo = new Site();
 
 		siteToGo = siteList_.get(0);
 
 		for (Site site : siteList_) {
 			Double convertedLatitude = site.getLatitude() * 1E6;
 			Double convertedLongitude = site.getLongitude() * 1E6;
 
 			GeoPoint point = new GeoPoint(convertedLatitude.intValue(),convertedLongitude.intValue());
 
 			SiteOverlayItem siteItem = new SiteOverlayItem(point, site);
 			siteItems.add(siteItem);
 		}
 
 		// TODO: Replace these with lazy loaded Site Images
 		//Drawable marker = this.getResources().getDrawable(R.drawable.flag);
		Drawable marker = getResources().getDrawable(R.drawable.site_icon);
 
 		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
 		//AddressOverlay addressOverlay = new AddressOverlay(siteToGo, marker, this);
 		SiteItemizedOverlay addressOverlay = new SiteItemizedOverlay(siteItems, marker, this);
 		//AddressOverlay addressOverlay = new AddressOverlay(siteToGo);
 		mapOverlays.add(addressOverlay);
 
 		mapView.invalidate();
 		final MapController mapController = mapView.getController();
 		//mapController.animateTo(addressOverlay.getGeopoint(), new Runnable() {
 		mapController.animateTo(siteItems.get(0).getPoint(), new Runnable() {
 			public void run() {
 				mapController.setZoom(17);
 			}
 		});
 
 
 
 
 	}
 	public void onSetHome(View v){
 		startActivity(new Intent(MapViewActivity.this, ListViewActivity.class));
 	}
 
 	public void onSetAboutUs(View v){
 		startActivity(new Intent(MapViewActivity.this, AboutUsActivity.class));    
 	}
 
 	public void onSetMap(View v){
 		Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show();
 	}
 
 	// Search Button Click
 	public void onSearch(View v){
 		onSearchRequested();
 	}
 
 	@Override
 	public boolean onSearchRequested() {
 		return super.onSearchRequested();
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
