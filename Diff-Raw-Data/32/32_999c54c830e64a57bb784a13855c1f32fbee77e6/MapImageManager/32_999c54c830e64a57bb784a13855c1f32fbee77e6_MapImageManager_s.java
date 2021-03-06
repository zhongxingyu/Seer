 package com.parworks.mars.view.siteexplorer;
 
 import com.parworks.mars.R;
 import com.parworks.mars.model.db.SiteInfoTable;
 import com.parworks.mars.view.siteexplorer.ImageViewManager.ImageLoadedListener;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class MapImageManager {
 
 	private final ImageView mMapImageView;
 	private final ProgressBar mMapImageProgressBar;
 	private final Activity mActivity;
 	private final String mSiteId;
 	
 	public static final String TAG = MapImageManager.class.getName();
 	
 	public MapImageManager(String siteId, ImageView mapImageView, ProgressBar mapImageProgressBar, Activity activity) {
 		mMapImageView = mapImageView;
 		mMapImageProgressBar = mapImageProgressBar;
 		mActivity = activity;
 		mSiteId = siteId;
 	}
 	
 	private void showMapView() {
 		mMapImageView.setVisibility(View.VISIBLE);
 		mMapImageProgressBar.setVisibility(View.INVISIBLE);
 	}
 
 	public void setMapView(Cursor data) {
 		String lat = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_LAT));
 		String lon = data.getString(data.getColumnIndex(SiteInfoTable.COLUMN_LON));
 		if(lat == null || lon == null) {
 			Toast.makeText(mActivity, "This site has no latitude or longitude.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		ViewDimensionCalculator viewDimensionCalculator = new ViewDimensionCalculator(mActivity);
 		final int width = viewDimensionCalculator.getScreenWidth();
 		
		String mapUrl = StaticGoogleMaps.getMapUrl(lat, lon, width, width/2);
 		ImageViewManager imageViewManager = new ImageViewManager();		
 		
 		if(mapUrl != null) {
 			imageViewManager.setImageView(mapUrl, mMapImageView, new ImageLoadedListener() {
 				
 				@Override
 				public void onImageLoaded() {
 					showMapView();
 					
 				}
 			});
 			
 		}  else {
 			Log.e(TAG, "mapURL was null.");
 		}
 		
 	}
 	
 }
