 /* Copyright (c) 2009 Ari Pollak <aripollak@gmail.com>
 
    This file is part of Picture Map.
 
    Picture Map is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    Picture Map is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with Picture Map.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.aripollak.picturemap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore.Images;
 import android.util.Config;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 
 // TODO: add intent to share location with Maps?
 // TODO: show larger image on click
 // TODO: Attach to media scanner to redo map if card is re-inserted?
 // TODO: cache thumbnails and locations
 // TODO: let people search for stuff by date/picture
 // TODO: let people re-geotag pictures
 public class MainActivity extends MapActivity {
	private static final String TAG = "PictureMap";
 	MapView mMapView;
 	View mPopup;
 	List<Overlay> mMapOverlays;
 	ImageOverlay mImageOverlay;
 	MyLocationOverlay mMyLocationOverlay;
 	PopulateMapTask mPopulateMapTask;
 	
 	/* Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.main);
 
         /*Button button = (Button)findViewById(R.id.choosepic);
         button.setOnClickListener(mGetImageListener); */
         
         mMapView = (MapView) findViewById(R.id.map);
         mMapView.setBuiltInZoomControls(true);
         
 		// TODO: make popup look like a caption
         // Can't embed the popup in main.xml since we can't seem to access
         // the MapView.LayoutParams-specific fields from there.
         mPopup = getLayoutInflater().inflate(R.layout.popup, null); 
 		MapView.LayoutParams params = new MapView.LayoutParams(
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				new GeoPoint(0, 0), MapView.LayoutParams.BOTTOM_CENTER);
         mMapView.addView(mPopup, params);
 
 		Button openImageButton = (Button) findViewById(R.id.viewButton);
 		openImageButton.setOnClickListener(mViewImageListener);
 		
         mMapOverlays = mMapView.getOverlays();
         mMyLocationOverlay = new CustomMyLocationOverlay(
         							getApplicationContext(), mMapView);
         
     	// If we just had a configuration change, re-use the old image overlay
     	ImageOverlay oldInstance = (ImageOverlay) getLastNonConfigurationInstance();
         if (oldInstance != null) {
         	mImageOverlay = oldInstance;
         } else {
         	Drawable mDrawable = this.getResources().getDrawable(
         							android.R.drawable.ic_menu_myplaces);
        	mImageOverlay = new ImageOverlay(mDrawable, mMapView);
             Intent intent = getIntent();
             String action = intent.getAction();
             Uri uri = null;
             if (action != null && action.equals(Intent.ACTION_SEND) &&
             		intent.hasExtra(Intent.EXTRA_STREAM)) {
                 // Handle Share from the Gallery app
             	uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
             }
             mPopulateMapTask = new PopulateMapTask(this);
             mPopulateMapTask.execute(uri);
         }
         
         
     	mMapOverlays.add(mImageOverlay);
     	mMapOverlays.add(mMyLocationOverlay);
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	mMyLocationOverlay.enableMyLocation();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	mMyLocationOverlay.disableMyLocation();
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
     	if (mPopulateMapTask == null || 
     			mPopulateMapTask.getStatus().equals(AsyncTask.Status.FINISHED))
     		return mImageOverlay;
     	else
     		// Don't save instance if we haven't finished populating the map
     		// since the old thread will be in a weird state
     		return null;
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	// Don't keep running in case this activity will get restarted
     	if (mPopulateMapTask != null)
     		mPopulateMapTask.cancel(true);
     }
     
     // TODO: save currently focused map item
 /*    @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     }
 */
     
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.mainmenu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	super.onOptionsItemSelected(item);
     	switch (item.getItemId()) {
     	case R.id.my_location:
     		GeoPoint point = mMyLocationOverlay.getMyLocation();
     		if (point == null)
     			Toast.makeText(this, "Location unavailable",
     						   Toast.LENGTH_SHORT).show();
     		else
     			mMapView.getController().animateTo(point);
     		return true;
     	}
     	return false;
     }
     
     
     /** Clicked on View Picture button */
     private final OnClickListener mViewImageListener = new OnClickListener() {
     	@Override
     	public void onClick(View v) {
     		int index = mImageOverlay.getLastFocusedIndex();
     		if (index == -1) {
     			if (Config.LOGV)
     				Log.v(TAG, "Couldn't get focused image?");
     			return;
     		}
     		OverlayItem item = mImageOverlay.getItem(index);
     		Uri uri = Uri.withAppendedPath(
 						Images.Media.EXTERNAL_CONTENT_URI,
 						item.getSnippet());
     		Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setDataAndType(uri, "image/jpeg");
             startActivity(Intent.createChooser(
             		intent, getString(R.string.select_image)));
     	}
     };
 
     
     @Override
     protected boolean isRouteDisplayed() { return false; }
     
 
 	protected class ImageOverlay extends ItemizedOverlay<OverlayItem> 
 	implements com.google.android.maps.ItemizedOverlay.OnFocusChangeListener {
 
 		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		//private MapView mMapView;
 		
		public ImageOverlay(Drawable defaultMarker, MapView mapView) {
 			super(boundCenterBottom(defaultMarker));
			//mMapView = mapView;
 			setOnFocusChangeListener(this);
 			populate();
 		}
 		
 		
 		public void addOverlay(OverlayItem item) {
 			boundCenterBottom(item.getMarker(0));
 			mOverlays.add(0, item); // put older items at the beginning
 		    populate();
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 			return mOverlays.get(i);
 		}
 
 
 		@Override
 		public int size() {
 			return mOverlays.size();
 		}
 		
 	    /** pop up a balloon when clicking on an image marker;
 	     *  disable it when clicking elsewhere
 	     */
 		@Override
 		public void onFocusChanged(ItemizedOverlay overlay, OverlayItem item) {
 			// This seems to be required all the time, even when
 			// an image is already focused and we're just switching
 			mPopup.setVisibility(View.GONE);
 			if (item == null)
 				return;
 			GeoPoint newPoint = item.getPoint();
 			((MapView.LayoutParams) mPopup.getLayoutParams()).point = newPoint;
 			//mMapView.getController().animateTo(newPoint);
 			mPopup.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected int getIndexToDraw(int drawingOrder) {
 			super.getIndexToDraw(drawingOrder);
 			return drawingOrder; // show newer items on top (higher rank)
 		}
 	}
 
 	
 	class CustomMyLocationOverlay extends MyLocationOverlay {
 
 		public CustomMyLocationOverlay(Context context, MapView mapView) {
 			super(context, mapView);
 		}
 		
 		/** Override this so a tap on the My Location point has absolutely 
 		 *  no effect, thus allowing images under the point to still be selected. */
 		@Override
 		public boolean onTap(GeoPoint p, MapView map) {
 			return false;
 		}
 	}
    
 }
