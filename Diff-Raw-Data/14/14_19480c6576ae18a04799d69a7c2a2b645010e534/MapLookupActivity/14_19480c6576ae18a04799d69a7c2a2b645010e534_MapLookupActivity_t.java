 package com.barview;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import com.barview.constants.BarviewConstants;
 import com.barview.map.overlay.CustomItemizedOverlay;
 import com.barview.models.Bar;
 import com.barview.rest.RestClient;
 import com.barview.rest.RestClient.RequestMethod;
 import com.barview.utilities.BarviewUtilities;
 import com.barview.xml.NearbyBarXMLHandler;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class MapLookupActivity extends MapActivity implements LocationListener {
 	
 	LinearLayout linearLayout;
 	MapView mapView;
 	Button searchButton;
 	EditText text;
 	
 	MapLookupActivity me = this;
 	
 	Geocoder geocoder = null;
 	
 	MapController mapController;
 	
 	static ArrayList<Bar> bars;
 	
 	private boolean foundLocation = false;
 	
 	/**
 	 * The last point that the user successfully searched for.  We want to save this
 	 * because if after searching for a location the user moves to the Current Location
 	 * tab then they'll lose their place if moving back to this tab.
 	 */
 	private GeoPoint lastSearch;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.barlookup);
 		
 		lastSearch = null;
 		
 		bars = new ArrayList<Bar>();
 		
 		geocoder = new Geocoder(this);
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		
 		text = (EditText) findViewById(R.id.editText1);
 		
 		searchButton = (Button) findViewById(R.id.button1);
 		searchButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				try {
 					String val = text.getText().toString();
 					Log.i(MapLookupActivity.class.getName(), "Clicked search button. " + val);
 					
 					String response;
 					
 					List<Address> addressList = geocoder.getFromLocationName(val, 5);
 					if(addressList!=null && addressList.size()>0) {
 						double lat = addressList.get(0).getLatitude();
 						double lng = addressList.get(0).getLongitude();
 						
 						NearbyBarFetcher ff = new NearbyBarFetcher();
 						ff.execute(lat, lng);
 					}
 					else {
 						
 					}
 					
 					// hide virtual keyboard
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		            imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
 				}
 				catch(Exception e) {
 					Log.e("MapLookupActivity", e.getMessage());
 				}
 			}
 		});
 		
		mapController = mapView.getController();
        mapController.setZoom(14);
 	}
 	
 	/**
 	 * Override the onResume callback so we can turn the GPS updates back on.  The updates
 	 * are stopped whenever we leave this Activity.
 	 */
 	public void onResume() {
 		super.onResume();
 		Log.i("onResume", "In onResume, about to zoom to the last location the user searched for.");
 		
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 50.0f, this);
 		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 50.0f, this);
 
 		// Make sure we have an old search to refocus to. 
 		if(lastSearch != null) {
 			mapController.animateTo(lastSearch);
 			mapController.setZoom(14);
 		}
 	}
 	
 	/**
 	 * Shut off GPS updates whenever we leave this Activity.  This is necessary so we don't
 	 * kill the battery if/when the user leaves the application.
 	 */
 	public void onPause() {
 		super.onPause();
 		Log.i("onPause", "In onPause, about to shut down LocationManager updates.");
 		
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.removeUpdates(this);
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public void onLocationChanged(Location location) {
 		if (location != null && !foundLocation) {
			int lat = (int) (location.getLatitude() * BarviewConstants.GEOPOINT_MULT);
			int lng = (int) (location.getLongitude() * BarviewConstants.GEOPOINT_MULT);
			GeoPoint p = new GeoPoint(lat, lng);
 			mapController.animateTo(p);
 			
 			foundLocation = true;
 		}
 	}
 
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * An AsyncTask class that contacts the Barview server with the lat/lng to
 	 * get the nearby bars as an XML string.  Once retrieved, this string will
 	 * be parsed by a SAX parser into individual Bar objects.  Finally,
 	 * we'll grab the ArrayList of Bar objects so it can be put into the list.
 	 * 
 	 * @author dmaclean
 	 *
 	 */
 	class NearbyBarFetcher extends AsyncTask<Double, Integer, String> {
 
 		String response = "";
 		
 		double lat;
 		double lng;
 		
 		ProgressDialog dialog;
 		
 		public NearbyBarFetcher() {
 			dialog = new ProgressDialog(me);
 		}
 		
 		protected void onPreExecute() {
             this.dialog.setMessage("Fetching bars near " + me.text.getText());
             this.dialog.show();
         }
 		
 		@Override
 		protected String doInBackground(Double... params) {
 			lat = params[0];
 			lng = params[1];
 			
 			RestClient client = new RestClient(BarviewUtilities.getNearbyBarsURLForRunMode());
 			client.AddHeader("Latitude", String.valueOf(params[0]));
 			client.AddHeader("Longitude", String.valueOf(params[1]));
 			
 			try {
 				client.Execute(RequestMethod.GET);
 				response = client.getResponse();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			try {
 				/* Get a SAXParser from the SAXPArserFactory. */
 	            SAXParserFactory spf = SAXParserFactory.newInstance();
 	            SAXParser sp = spf.newSAXParser();
 	
 	            /* Get the XMLReader of the SAXParser we created. */
 	            XMLReader xr = sp.getXMLReader();
 	            /* Create a new ContentHandler and apply it to the XML-Reader*/
 	            NearbyBarXMLHandler myExampleHandler = new NearbyBarXMLHandler();
 	            xr.setContentHandler(myExampleHandler);
 	           
 	            /* Parse the xml-data from our URL. */
 	            xr.parse(new InputSource(new StringReader(response)));
 	            /* Parsing has finished. */
 
 	            /* Our ExampleHandler now provides the parsed data to us. */
 	            bars = myExampleHandler.getNearbyBars();
 			}
 			catch(Exception e) {
 				Log.e("doInBackground", "An error occurred: " + e.getMessage());
 			}
 			
 			return response;
 		}
 		
 		protected void onPostExecute(String result) {
 			List<Overlay> mapOverlays = mapView.getOverlays();
 			
 			for(Bar b : bars) {
 				Log.i(MapLookupActivity.class.getName(), b.getName());
 				
 				
 		        Drawable drawable = me.getResources().getDrawable(R.drawable.barview_map_annot);
 		        CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, me);
 //		        itemizedOverlay.setImageView(findViewById(R.layout.detail));
 //		        itemizedOverlay.setMap((MapView) findViewById(R.id.mapview));
 		        itemizedOverlay.setBarId(b.getBarId());
 		        
 		        // The Bar object already has lat/lng set up with GeoPoint precision - no need to multiply.
 		        GeoPoint point = new GeoPoint((int)b.getLat(), (int)b.getLng());
 		        OverlayItem overlayitem = new OverlayItem(point, b.getName(), b.getAddress());
 		        
 		        itemizedOverlay.addOverlay(overlayitem);
 		        mapOverlays.add(itemizedOverlay);
 			}
 			
 			GeoPoint p = new GeoPoint(	(int) (lat * BarviewConstants.GEOPOINT_MULT), 
 										(int) (lng * BarviewConstants.GEOPOINT_MULT));
 			mapController.animateTo(p);
 			mapController.setZoom(14);
 			
 			// Save this location as the last-searched-for point.
 			lastSearch = p;
 			
 			if (dialog.isShowing())
                 dialog.dismiss();
 		}
 	}
 }
