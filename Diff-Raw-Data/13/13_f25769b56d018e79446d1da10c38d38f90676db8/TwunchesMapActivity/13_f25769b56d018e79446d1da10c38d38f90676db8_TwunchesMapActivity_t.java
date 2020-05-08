 /**
  *	Copyright 2010-2012 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.ui;
 
 import java.util.List;
 
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import be.norio.twunch.android.BuildProperties;
 import be.norio.twunch.android.R;
 import be.norio.twunch.android.provider.TwunchContract.Twunches;
 import be.norio.twunch.android.util.TwunchItemizedOverlay;
 import be.norio.twunch.android.util.TwunchOverlayItem;
 
 import com.actionbarsherlock.app.SherlockMapActivity;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 
 public class TwunchesMapActivity extends SherlockMapActivity {
 
 	Cursor mCursor;
 
 	MapView mMapView;
 	TwunchItemizedOverlay mItemizedOverlay;
 	MyLocationOverlay mMyLocationOverlay;
 
 	private Drawable mDrawable;
 
	private ContentObserver mObserver;

 	private interface TwunchesQuery {
 		int _TOKEN = 0x1;
 
 		String[] PROJECTION = { BaseColumns._ID, Twunches.LATITUDE, Twunches.LONGITUDE };
 
 		int _ID = 0;
 		int LATITUDE = 1;
 		int LONGITUDE = 2;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mMapView = new MapView(this, BuildProperties.MAPS_KEY);
 		mMapView.setClickable(true);
 		mMapView.setBuiltInZoomControls(true);
 
 		setContentView(mMapView);
 
 		mDrawable = getResources().getDrawable(R.drawable.marker);
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 
		mObserver = new ContentObserver(new Handler()) {
 			public void onChange(boolean selfChange) {
 				mCursor.requery();
 				showOverlays(false);
 			};
		};
 		mCursor = getContentResolver().query(Twunches.buildFutureTwunchesUri(), TwunchesQuery.PROJECTION, null, null, null);
 		startManagingCursor(mCursor);
 		showOverlays(true);
 	}
 
 	protected void showOverlays(boolean first) {
 		List<Overlay> mapOverlays = mMapView.getOverlays();
 		mapOverlays.clear();
 		mItemizedOverlay = new TwunchItemizedOverlay(mDrawable, this);
 		while (mCursor.moveToNext()) {
 			if (mCursor.getFloat(TwunchesQuery.LATITUDE) != 0 && mCursor.getFloat(TwunchesQuery.LONGITUDE) != 0) {
 				GeoPoint point = new GeoPoint(new Double(mCursor.getFloat(TwunchesQuery.LATITUDE) * 1E6).intValue(), new Double(
 						mCursor.getFloat(TwunchesQuery.LONGITUDE) * 1E6).intValue());
 				TwunchOverlayItem overlayitem = new TwunchOverlayItem(point, mCursor.getInt(0));
 				mItemizedOverlay.addOverlay(overlayitem);
 			}
 		}
 		mapOverlays.add(mItemizedOverlay);
 		mapOverlays.add(mMyLocationOverlay);
 		if (first) {
 			mMapView.getController().zoomToSpan(mItemizedOverlay.getLatSpanE6(), mItemizedOverlay.getLonSpanE6());
 			mMapView.getController().animateTo(mItemizedOverlay.getCenter());
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mMyLocationOverlay.disableMyLocation();
		getContentResolver().unregisterContentObserver(mObserver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mMyLocationOverlay.enableMyLocation();
		getContentResolver().registerContentObserver(Twunches.buildFutureTwunchesUri(), true, mObserver);
 	}
 
 }
