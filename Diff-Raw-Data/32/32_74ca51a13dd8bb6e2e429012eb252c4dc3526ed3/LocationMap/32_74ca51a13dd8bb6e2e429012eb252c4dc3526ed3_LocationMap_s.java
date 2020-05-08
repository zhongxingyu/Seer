 package tw.edu.ntu.fortour;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 public class LocationMap extends MapActivity {
 	private Button mButtonLMOk, mButtonLMDetermine, mButtonLMCancel, mButtonLMBack;
 	private MapView mMapView;
 	private MapController mMapController;
 	private GeoPoint mGeoPoint;
 	private MyLocationOverlay mMyLocationOverlay;
 	private String locLongitude, locLatitude;
 	private boolean hasLocation = false;
 	
 	protected static String KEY_LONGITUDE = "KEY_LONGITUDE";
 	protected static String KEY_LATITUDE  = "KEY_LATITUDE";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.location_map);
 
         Bundle b = this.getIntent().getExtras();
         
         if( b != null ) {
 	        locLongitude = b.getString( KEY_LONGITUDE );
 	        locLatitude  = b.getString( KEY_LATITUDE );
 
 	        if( locLatitude != null && locLongitude != null ) hasLocation = true;
         }
         
         mButtonLMOk        = (Button) findViewById( R.id.buttonLMOk );
         mButtonLMDetermine = (Button) findViewById( R.id.buttonLMDetermine );
         mButtonLMCancel    = (Button) findViewById( R.id.buttonLMCancel );
         mButtonLMBack      = (Button) findViewById( R.id.buttonLMBack );
         
         mMapView = (MapView) findViewById( R.id.mapView );
         
         mMapView.setSatellite( false );
         mMapView.setStreetView( true );
         if( !hasLocation ) {
 	        mMapView.setClickable( true );
 	        mMapView.setBuiltInZoomControls( true );
 	        mMapView.displayZoomControls( true );
         }
         
         List<Overlay> mMapOverlays = mMapView.getOverlays();
         
         mMapController = mMapView.getController();
         mMapController.setZoom( 16 );
         
         mMyLocationOverlay = new MyLocationOverlay( LocationMap.this, mMapView );
         if( !hasLocation ) {
 	        mMyLocationOverlay.enableCompass();
 	        mMyLocationOverlay.enableMyLocation();
         }
         else {
         	mButtonLMOk.setVisibility( View.GONE );
         	mButtonLMDetermine.setVisibility( View.GONE );
         	mButtonLMCancel.setVisibility( View.GONE );
         	mButtonLMBack.setVisibility( View.VISIBLE );
         }
         mMyLocationOverlay.runOnFirstFix( determinLocation );
         
         mMapOverlays.add( mMyLocationOverlay );
         
         /* should after all definition */
         setButtonListener();
         
         /* check Internet first */
         if( !Util.isOnline( getSystemService( Context.CONNECTIVITY_SERVICE ) ) ) {
         	Toast.makeText( LocationMap.this, getString( R.string.stringNoInternetConnection ), Toast.LENGTH_LONG ).show();
         }
 	}
     
     Runnable determinLocation = new Runnable() {
 		@Override
 		public void run() {
 			if( !hasLocation ) mGeoPoint = mMyLocationOverlay.getMyLocation();
 			else mGeoPoint = new GeoPoint( Integer.valueOf( locLatitude ) , Integer.valueOf( locLongitude ) );
 			
 			mMapController.animateTo( mGeoPoint );
 		}
 	}; 
     
 	private void setButtonListener() {
 		mButtonLMDetermine.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mMyLocationOverlay.runOnFirstFix( determinLocation );
 			}
 		} );
 		
 		mButtonLMOk.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mGeoPoint = mMyLocationOverlay.getMyLocation();
 
 				Intent i = new Intent();
 				Bundle b = new Bundle();
 				
				b.putString( KEY_LONGITUDE, Integer.toString( mGeoPoint.getLongitudeE6() ) );
				b.putString( KEY_LATITUDE, Integer.toString( mGeoPoint.getLatitudeE6() ) );
				
				i.putExtras( b );
 				setResult( Activity.RESULT_OK, i );
 				
 				LocationMap.this.finish();
 			}
 		} );
 		
 		mButtonLMCancel.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				LocationMap.this.finish();
 			}
 		} );
 		
 		mButtonLMBack.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				LocationMap.this.finish();
 			}
 		} );
 	}
 	
     @Override
     protected void onResume() {
     	super.onResume();
     	if( !hasLocation && !mMyLocationOverlay.isMyLocationEnabled() ) mMyLocationOverlay.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	if( !hasLocation && mMyLocationOverlay.isMyLocationEnabled() ) mMyLocationOverlay.disableMyLocation();
     }
     
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 }
