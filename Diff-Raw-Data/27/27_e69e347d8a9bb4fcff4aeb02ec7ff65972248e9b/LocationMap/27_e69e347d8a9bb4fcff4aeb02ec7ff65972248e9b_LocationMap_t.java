 package tw.edu.ntu.fortour;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
import android.widget.TextView;
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
 	private Button mButtonLMOk, mButtonLMLocation, mButtonLMCancel, mButtonLMBack;
	private TextView mTextViewLMLocation;
 	private MapView mMapView;
 	private MapController mMapController;
 	private GeoPoint mGeoPoint, mManualGeoPoint;
 	private MyLocationOverlay mMyLocationOverlay;
 	private String locLongitude, locLatitude, locName;
 	
 	private List<Overlay> mMapOverlays;
 	
 	private boolean hasLocation = false;
 	private boolean updateMode  = false;
 	private boolean manualMode  = false;
 	
 	protected static String KEY_LATITUDE  = "KEY_LATITUDE";
 	protected static String KEY_LONGITUDE = "KEY_LONGITUDE";
 	protected static String KEY_LOCNAME   = "KEY_LOCNAME";
 	protected static String KEY_UPDMODE   = "KEY_UPDMODE";
 	
	private final int ADDRESS_LIMIT = 5;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.location_map);
 
         /* check Internet first */
         if( !Util.isOnline( getSystemService( Context.CONNECTIVITY_SERVICE ) ) ) {
         	Toast.makeText( LocationMap.this, getString( R.string.stringNoInternetConnection ), Toast.LENGTH_LONG ).show();
         }
         
         Bundle b = this.getIntent().getExtras();
         
         if( b != null ) {
         	locLatitude  = b.getString( KEY_LATITUDE );
 	        locLongitude = b.getString( KEY_LONGITUDE );
 	        locName      = b.getString( KEY_LOCNAME );
 	        updateMode   = b.getString( KEY_UPDMODE ) != null ? true : false;
 
 	        if( locLatitude != null && locLongitude != null ) hasLocation = true;
         }
         
         mButtonLMOk        = (Button) findViewById( R.id.buttonLMOk );
         mButtonLMLocation  = (Button) findViewById( R.id.buttonLMDetermine );
         mButtonLMCancel    = (Button) findViewById( R.id.buttonLMCancel );
         mButtonLMBack      = (Button) findViewById( R.id.buttonLMBack );
        mTextViewLMLocation= (TextView) findViewById( R.id.textViewLMLocation );
         mMapView		   = (MapView) findViewById( R.id.mapView );
         
         mProgressDialog = new ProgressDialog( LocationMap.this );
         
         mProgressDialog.setTitle( getString( R.string.stringLoading ) );
         mProgressDialog.setMessage( getString( R.string.stringPleaseWait ) );
         mProgressDialog.setCancelable( true );
         mProgressDialog.setOnCancelListener( new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface arg0) {
 				Toast.makeText( LocationMap.this, getString( R.string.stringUnableToRetrieveLocationNow ), Toast.LENGTH_LONG ).show();
 			}
 		} );
         
         mProgressDialog.show();
         
         /* NOTE: DO NOT USE 'setStreetView( true )' or it may be a strange layout. */
         mMapView.setClickable( true );
         mMapView.setBuiltInZoomControls( true );
         mMapView.displayZoomControls( true );
         
         mMapOverlays = mMapView.getOverlays();
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
         	if( !( hasLocation && updateMode ) ) {
 	        	mButtonLMOk.setVisibility( View.GONE );
 	        	mButtonLMLocation.setVisibility( View.GONE );
 	        	mButtonLMCancel.setVisibility( View.GONE );
 	        	mButtonLMBack.setVisibility( View.VISIBLE );
	        	
	        	if( !"".equals( locName ) ) {
	        		mTextViewLMLocation.setText( locName );
	        		mTextViewLMLocation.setVisibility( View.VISIBLE );
	        	}
         	}
         	determinLocation.run();
         }
         mMapOverlays.add( mMyLocationOverlay );
         
         /* should after all variable initial */
         setButtonListener();
 	}
 
     Runnable addMarker = new Runnable() {
 		@Override
 		public void run() {
         	markerOverlay mLMOverlay = new markerOverlay( getResources().getDrawable( R.drawable.locate ) );
         	
         	OverlayItem mOverlayItem = new OverlayItem( mGeoPoint, getString( R.string.stringLocation ), locName );
         	mLMOverlay.addMarker( mOverlayItem );
         	
         	mMapOverlays.add( mLMOverlay );
 		}
 	};
     
     Runnable determinLocation = new Runnable() {
 		@Override
 		public void run() {
 			if( !hasLocation ) mGeoPoint = mMyLocationOverlay.getMyLocation();
 			else mGeoPoint = new GeoPoint( Integer.valueOf( locLatitude ) , Integer.valueOf( locLongitude ) );
 			
 			mMapController.animateTo( mGeoPoint, addMarker );
 
 			if( mProgressDialog != null ) mProgressDialog.dismiss();
 			
 			// Translate location to location name
 			if( !hasLocation || ( hasLocation && updateMode ) ) runOnUiThread( getAddressList );
 		}
 	}; 
 
 	Runnable getAddressList = new Runnable() {
 		@Override
 		public void run() {
 	        Geocoder mGeocoder = new Geocoder( LocationMap.this, Locale.getDefault() );
 	        
 			Spinner mSpinner = (Spinner) findViewById( R.id.spinnerLMList );
 			mSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 				@Override
 				public void onItemSelected(AdapterView<?> view, View arg1, int arg2, long arg3) {
 					locName = view.getSelectedItem().toString();
 				}
 				
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) { }
 			});
 			
 			try {
 				List<Address> addressesList = null;
 				
 				if( !manualMode ) addressesList = mGeocoder.getFromLocation( mGeoPoint.getLatitudeE6()/1E6, mGeoPoint.getLongitudeE6()/1E6, ADDRESS_LIMIT );
 				else addressesList = mGeocoder.getFromLocation( mManualGeoPoint.getLatitudeE6()/1E6, mManualGeoPoint.getLongitudeE6()/1E6, ADDRESS_LIMIT );
 				
 				if( addressesList != null ) {
 					ArrayList<String> mArrayList = new ArrayList<String>();
 					for( Address addr : addressesList ) {
 						mArrayList.add( addr.getAddressLine(0) );
 					}
 					
 					ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>( LocationMap.this, android.R.layout.simple_spinner_item, mArrayList );
 					mArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
 					mSpinner.setAdapter( mArrayAdapter );
 					
 					mSpinner.setVisibility( View.VISIBLE );
 				}
 			}
 			catch( Exception e ) { 
				Toast.makeText( LocationMap.this, getString( R.string.stringLocationList ) + ": " + e.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
 			} 
 		}
 	};
 	
 	private void setButtonListener() {
 		mButtonLMLocation.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				manualMode = false;
 				
 				mProgressDialog.show();
 
				mMapOverlays.clear();
 				
				if( !hasLocation ) mMyLocationOverlay.runOnFirstFix( determinLocation );
				else determinLocation.run();
 				
				mMapOverlays.add( mMyLocationOverlay );
 			}
 		} );
 		
 		mButtonLMOk.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {				
 				mGeoPoint = mMyLocationOverlay.getMyLocation();
 
 				Intent i = new Intent();
 				Bundle b = new Bundle();
 				
 				if( manualMode && mManualGeoPoint != null ) mGeoPoint = mManualGeoPoint;
 				
 				if( mGeoPoint != null ) {
 					b.putString( KEY_LATITUDE, Integer.toString( mGeoPoint.getLatitudeE6() ) );
 					b.putString( KEY_LONGITUDE, Integer.toString( mGeoPoint.getLongitudeE6() ) );
 					b.putString( KEY_LOCNAME, locName );
 					
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
 	
 	class markerOverlay extends ItemizedOverlay<OverlayItem> {
 		private ArrayList<OverlayItem> mOverlayList = new ArrayList<OverlayItem>();
 			
 		private OverlayItem mOverlayitemInDrag = null;
 		private Drawable mDrawableMarker       = null;
 		private ImageView mImageViewDragImage  = null;
 		
 	    private int xDragImageOffset = 0;
 	    private int yDragImageOffset = 0;
 	    private int xDragTouchOffset = 0;
 	    private int yDragTouchOffset = 0;
 	    
 		public markerOverlay( Drawable defaultMarker ) {
 			super( boundCenterBottom( defaultMarker ) );
 			
 			mDrawableMarker     = defaultMarker;
 			mImageViewDragImage = (ImageView) findViewById( R.id.imageViewLMDrag );
 			
 			xDragImageOffset = mImageViewDragImage.getDrawable().getIntrinsicWidth() / 2;
 		    yDragImageOffset = mImageViewDragImage.getDrawable().getIntrinsicHeight();
 		}
 		
 		private void addMarker( OverlayItem item ) {
 			mOverlayList.add( item );
 			populate();
 		}
 
 		@Override
 		protected OverlayItem createItem( int i ) {
 			return mOverlayList.get( i );
 		}
 
 		@Override
 		public int size() {
 			return mOverlayList.size();
 		}
 		
 	    @Override
 	    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
 			final int x    = (int) event.getX();
 			final int y    = (int) event.getY();
 			
 			boolean result = false;
 			
 			switch( event.getAction() ) {
 				case MotionEvent.ACTION_DOWN:
 					for( OverlayItem item : mOverlayList ) {
 						Point p = new Point(0,0);
 						
 						mMapView.getProjection().toPixels( item.getPoint(), p );
 
 						if( ( !hasLocation || updateMode ) && hitTest( item, mDrawableMarker, x - p.x , y - p.y ) ) {
 							manualMode = true;
 							
 							result = true;
 							mOverlayitemInDrag = item;
 							mOverlayList.remove( mOverlayitemInDrag );
 							populate();
 	
 							xDragTouchOffset = 0;
 							yDragTouchOffset = 0;
 							    
 							setDragImagePosition( p.x, p.y );
 							mImageViewDragImage.setVisibility( View.VISIBLE );
 	
 							xDragTouchOffset = x - p.x;
 							yDragTouchOffset = y - p.y;
 							    
 							break;
 						}
 					}
 					break;
 
 				case MotionEvent.ACTION_MOVE:
 					if( mOverlayitemInDrag != null ) {
 						setDragImagePosition( x, y );
 						result = true;
 					}
 					break;
 				
 				case MotionEvent.ACTION_UP:
 					if( mOverlayitemInDrag != null ) {
 						mImageViewDragImage.setVisibility( View.GONE );
 						
 						mManualGeoPoint = mMapView.getProjection().fromPixels( x - xDragTouchOffset, y - yDragTouchOffset );
 						
 						mOverlayList.add( new OverlayItem(	mManualGeoPoint,
 															mOverlayitemInDrag.getTitle(),
 															mOverlayitemInDrag.getSnippet() )
 						);
 						populate();
 						    
 						mOverlayitemInDrag = null;
 						
 						result = true;
 						    
 						mMapController.animateTo( mManualGeoPoint, getAddressList );
 					}
 					break;
 					
 				default:
 					break;
 			}
 			
 			return ( result || super.onTouchEvent( event, mapView ) );
 		}
 	    
 		private void setDragImagePosition( int x, int y ) {
 			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImageViewDragImage.getLayoutParams();
 			lp.setMargins( x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0 );
 			mImageViewDragImage.setLayoutParams( lp );
 		}
 	}
 }
