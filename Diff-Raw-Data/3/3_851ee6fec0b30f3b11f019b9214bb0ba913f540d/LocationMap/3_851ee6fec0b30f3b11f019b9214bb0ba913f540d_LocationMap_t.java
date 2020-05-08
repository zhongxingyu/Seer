 package tw.edu.ntu.fortour;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class LocationMap extends MapActivity {
 	private ProgressDialog mProgressDialog;
 	private Button mButtonLMOk, mButtonLMDetermine, mButtonLMCancel, mButtonLMBack;
 	private MapView mMapView;
 	private MapController mMapController;
 	private GeoPoint mGeoPoint;
 	private MyLocationOverlay mMyLocationOverlay;
 	private String locLongitude, locLatitude;
 	private boolean hasLocation = false;
 	
 	protected static String KEY_LATITUDE  = "KEY_LATITUDE";
 	protected static String KEY_LONGITUDE = "KEY_LONGITUDE";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.location_map);
 
         mProgressDialog = ProgressDialog.show( LocationMap.this, getString( R.string.stringLoading ), getString( R.string.stringPleaseWait ) );
         mProgressDialog.setCancelable( true );
         mProgressDialog.setOnCancelListener( new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface arg0) {
 				Toast.makeText( LocationMap.this, getString( R.string.stringUnableToRetrieveLocationNow ), Toast.LENGTH_LONG ).show();
 			}
 		} );
         
         Bundle b = this.getIntent().getExtras();
         
         if( b != null ) {
         	locLatitude  = b.getString( KEY_LATITUDE );
 	        locLongitude = b.getString( KEY_LONGITUDE );
 
 	        if( locLatitude != null && locLongitude != null ) hasLocation = true;
         }
         
         mButtonLMOk        = (Button) findViewById( R.id.buttonLMOk );
         mButtonLMDetermine = (Button) findViewById( R.id.buttonLMDetermine );
         mButtonLMCancel    = (Button) findViewById( R.id.buttonLMCancel );
         mButtonLMBack      = (Button) findViewById( R.id.buttonLMBack );
         
         mMapView = (MapView) findViewById( R.id.mapView );
         
        /* NOTE: DO NOT USE 'setStreetView( true )' or it may be a strange layout. */
         mMapView.setClickable( true );
         mMapView.setBuiltInZoomControls( true );
         mMapView.displayZoomControls( true );
         
         List<Overlay> mMapOverlays = mMapView.getOverlays();
         mMapOverlays.clear();
         
         mMapController = mMapView.getController();
         mMapController.setZoom( 17 );
         
         mMyLocationOverlay = new MyLocationOverlay( LocationMap.this, mMapView );
         if( !hasLocation ) {
 	        mMyLocationOverlay.enableCompass();
 	        mMyLocationOverlay.enableMyLocation();
 	        mMyLocationOverlay.runOnFirstFix( determinLocation );
         }
         else {
         	mButtonLMOk.setVisibility( View.GONE );
         	mButtonLMDetermine.setVisibility( View.GONE );
         	mButtonLMCancel.setVisibility( View.GONE );
         	mButtonLMBack.setVisibility( View.VISIBLE );
         	determinLocation.run();
         	
         	/* add landmark */
         	landmarkOverlay mLMOverlay = new landmarkOverlay( getResources().getDrawable( R.drawable.btn_loc ) );
         	OverlayItem mOverlayItem = new OverlayItem( mGeoPoint, "", "" );
         	mLMOverlay.addLandmark( mOverlayItem );
         	mMapOverlays.add( mLMOverlay );
         }
         
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
 			
 			if( mProgressDialog != null ) mProgressDialog.dismiss();
 		}
 	}; 
     
 	private void setButtonListener() {
 		mButtonLMDetermine.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mProgressDialog.show();
 				mMyLocationOverlay.runOnFirstFix( determinLocation );
 			}
 		} );
 		
 		mButtonLMOk.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mGeoPoint = mMyLocationOverlay.getMyLocation();
 
 				Intent i = new Intent();
 				Bundle b = new Bundle();
 				
 				if( mGeoPoint != null ) {
 					b.putString( KEY_LATITUDE, Integer.toString( mGeoPoint.getLatitudeE6() ) );
 					b.putString( KEY_LONGITUDE, Integer.toString( mGeoPoint.getLongitudeE6() ) );
 					
 					i.putExtras( b );
 				}
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
 	
 	class landmarkOverlay extends ItemizedOverlay<OverlayItem> {
 		private ArrayList<OverlayItem> mOverlayList = new ArrayList<OverlayItem>();
 		
 		public landmarkOverlay( Drawable defaultMarker ) {
 			super( boundCenterBottom(defaultMarker) );
 		}
 		
 		private void addLandmark( OverlayItem item ) {
 			mOverlayList.add( item );
 			populate();
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			return mOverlayList.get( i );
 		}
 
 		@Override
 		public int size() {
 			return mOverlayList.size();
 		}
 		
 	}
 }
