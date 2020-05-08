 package carnero.netmap.fragment;
 
 import java.util.*;
 
 import android.content.Context;
 import android.location.Location;
 import android.os.Bundle;
 import android.telephony.CellInfo;
 import android.telephony.CellLocation;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.telephony.cdma.CdmaCellLocation;
 import android.telephony.gsm.GsmCellLocation;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import carnero.netmap.App;
 import carnero.netmap.R;
 import carnero.netmap.activity.MainActivity;
 import carnero.netmap.common.*;
 import carnero.netmap.iface.IBackHandler;
 import carnero.netmap.listener.OnBtsCacheChangedListener;
 import carnero.netmap.listener.OnSectorCacheChangedListener;
 import carnero.netmap.model.*;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.*;
 
 public class NetMapFragment extends MapFragment implements SimpleGeoReceiver, OnBtsCacheChangedListener, OnSectorCacheChangedListener, IBackHandler {
 
 	private Geo mGeo;
 	private GoogleMap mMap;
 	private boolean mCentering = true;
 	private boolean mCentered = false;
 	private TelephonyManager mTelephony;
 	private LatLng mLastLocation;
 	private Bts mLastBts;
 	private Marker mMyMarker;
 	private Polyline mConnectionCurrent;
 	private int[] mFillColors = new int[5];
 	private int[] mStrokeColors = new int[5];
 	private XY mTouched;
 	private HashMap<String, Marker> mBtsMarkers = new HashMap<String, Marker>();
 	private HashMap<XY, Polygon> mCoveragePolygons = new HashMap<XY, Polygon>();
 	private Polygon mCoverageTouch;
 	private float mZoomDefault = 14f;
 	//
 	final private StatusListener mListener = new StatusListener();
 
 	@Override
 	public void onCreate(Bundle state) {
 		super.onCreate(state);
 
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle state) {
 		super.onActivityCreated(state);
 
 		mTelephony = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
 
 		mFillColors[0] = getResources().getColor(R.color.cell_l1);
 		mFillColors[1] = getResources().getColor(R.color.cell_l2);
 		mFillColors[2] = getResources().getColor(R.color.cell_l3);
 		mFillColors[3] = getResources().getColor(R.color.cell_l4);
 		mFillColors[4] = getResources().getColor(R.color.cell_l5);
 		mStrokeColors[0] = getResources().getColor(R.color.stroke_l1);
 		mStrokeColors[1] = getResources().getColor(R.color.stroke_l2);
 		mStrokeColors[2] = getResources().getColor(R.color.stroke_l3);
 		mStrokeColors[3] = getResources().getColor(R.color.stroke_l4);
 		mStrokeColors[4] = getResources().getColor(R.color.stroke_l5);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
 		final View mapView = super.onCreateView(inflater, container, state);
 		setMapTransparent((ViewGroup)mapView);
 
 		return mapView;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		mGeo = App.getGeolocation();
 		mLastLocation = mGeo.getLastLoc();
 		mCentered = false;
 
 		initializeMap();
 		setMyMarker();
 
 		mGeo.addReceiver(this);
 		SectorCache.addListener(this);
 		BtsCache.addListener(this);
 
 		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);
 	}
 
 	@Override
 	public void onPause() {
 		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);
 
 		mGeo.removeReceiver(this);
 
 		BtsCache.removeListener(this);
 		SectorCache.removeListener(this);
 
 		if (mMyMarker != null) {
 			mMyMarker.remove();
 			mMyMarker = null;
 		}
 
 		mMap.clear();
 		mMap = null;
 
 		super.onPause();
 	}
 
 	@Override
 	public boolean onBackPressed() {
 		MainActivity activity = (MainActivity)getActivity();
 		activity.hideInfo();
 
 		if (mCoverageTouch != null) {
 			mCoverageTouch.remove();
 			mCoverageTouch = null;
 
 			resetTouched();
 
 			mTouched = null;
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public void onBtsCacheChanged(Bts bts) {
 		addBts(bts);
 	}
 
 	public void onSectorCacheChanged(Sector sector) {
 		addSector(sector);
 	}
 
 	public void initializeMap() {
 		if (mMap == null) {
 			mMap = getMap();
 
 			if (mMap == null) {
 				return; // map is not yet available
 			}
 
 			mMap.setPadding(0, 0, 0, 0);
 			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			mMap.setMyLocationEnabled(false);
 			mMap.setOnCameraChangeListener(new MapMoveListener());
 			mMap.setOnMapClickListener(new CoverageClickListener());
 
 			if (mMap.getMaxZoomLevel() < mZoomDefault) {
 				mZoomDefault = mMap.getMaxZoomLevel();
 			}
 
 			UiSettings settings = mMap.getUiSettings();
 			settings.setCompassEnabled(true);
 			settings.setZoomControlsEnabled(false);
 			settings.setMyLocationButtonEnabled(false);
 		}
 
 		final List<Sector> sectors = SectorCache.getAll();
 		Log.d(Constants.TAG, "Loaded sector count: " + sectors.size());
 		for (Sector sector : sectors) {
 			addSector(sector);
 		}
 
 		if (Preferences.isSetMarkers(getActivity())) {
 			final List<Bts> btses = BtsCache.getAll();
 			Log.d(Constants.TAG, "Loaded BTS count: " + btses.size());
 			for (Bts bts : btses) {
 				addBts(bts);
 			}
 		}
 
 		setMyMarker();
 	}
 
 	public void onLocationChanged(Location location) {
 		if (location == null) {
 			return;
 		}
 
 		mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
 
 		setMyMarker();
 		setConnection();
 	}
 
 	public void setMyMarker() {
 		if (mLastLocation == null) {
 			return;
 		}
 
 		// my current position
 		if (mMyMarker == null) {
 			final MarkerOptions markerOpts = new MarkerOptions();
 			markerOpts.position(mLastLocation);
 			markerOpts.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_my));
 			markerOpts.anchor(0.5f, 0.5f);
 
 			mMyMarker = mMap.addMarker(markerOpts);
 		} else {
 			mMyMarker.setPosition(mLastLocation);
 		}
 
 		if (mCentering) {
 			if (mCentered) {
 				mMap.animateCamera(CameraUpdateFactory.newLatLng(mLastLocation));
 			} else {
 				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, mZoomDefault));
 				mCentered = true;
 			}
 		}
 	}
 
 	private void setMapTransparent(ViewGroup group) {
 		int cnt = group.getChildCount();
 
 		for (int i = 0; i < cnt; i++) {
 			View child = group.getChildAt(i);
 
 			if (child instanceof ViewGroup) {
 				setMapTransparent((ViewGroup)child);
 			} else {
 				child.setBackgroundColor(0x00000000);
 			}
 		}
 	}
 
 	public void checkMarkers() {
 		synchronized (mBtsMarkers) {
 			if (Preferences.isSetMarkers(getActivity())) {
 				final List<Bts> btses = BtsCache.getAll();
 				Log.d(Constants.TAG, "Loaded BTS count: " + btses.size());
 				for (Bts bts : btses) {
 					addBts(bts);
 				}
 			} else {
 				final Set<Map.Entry<String, Marker>> keys = mBtsMarkers.entrySet();
 				for (Map.Entry entry : keys) {
 					((Marker)entry.getValue()).remove();
 				}
 
 				mBtsMarkers.clear();
 			}
 		}
 
 		setConnection();
 	}
 
 	private void addBts(Bts bts) {
 		if (!Preferences.isSetMarkers(getActivity()) || bts.location == null) {
 			return;
 		}
 
 		final String id = Bts.getId(bts);
 		final int level = Util.getNetworkLevel(bts.network);
 
 		int pinResource;
 		switch (level) {
 			case Constants.NET_LEVEL_2:
 				pinResource = R.drawable.pin_level_2;
 				break;
 			case Constants.NET_LEVEL_3:
 				pinResource = R.drawable.pin_level_3;
 				break;
 			case Constants.NET_LEVEL_4:
 				pinResource = R.drawable.pin_level_4;
 				break;
 			case Constants.NET_LEVEL_5:
 				pinResource = R.drawable.pin_level_5;
 				break;
 			default:
 				pinResource = R.drawable.pin_level_1;
 		}
 
 		if (mBtsMarkers.containsKey(id)) {
 			mBtsMarkers.get(id).remove();
 		}
 
 		final MarkerOptions markerOpts = new MarkerOptions();
 		markerOpts.position(bts.location);
 		markerOpts.icon(BitmapDescriptorFactory.fromResource(pinResource));
 		markerOpts.anchor(0.5f, 1.0f);
 
 		mBtsMarkers.put(id, mMap.addMarker(markerOpts));
 
 		setConnection();
 	}
 
 	private void addSector(Sector sector) {
 		final int level = Util.getNetworkLevel(sector.network);
		final int fill;
		final int stroke;
		if (level < 0) {
			fill = mFillColors[0];
			stroke = mStrokeColors[0];
		} else {
			fill = mFillColors[level];
			stroke = mStrokeColors[level];
		}
 
 		if (mCoveragePolygons.containsKey(sector.index)) {
 			mCoveragePolygons.get(sector.index).remove();
 		}
 
 		final PolygonOptions polygonOpts = new PolygonOptions();
 		polygonOpts.zIndex(Util.getNetworkLevel(sector.network));
 		polygonOpts.strokeWidth(getResources().getDimension(R.dimen.sector_margin));
 		polygonOpts.strokeColor(stroke);
 		polygonOpts.fillColor(fill);
 		polygonOpts.addAll(sector.getCorners());
 
 		mCoveragePolygons.put(sector.index, mMap.addPolygon(polygonOpts));
 	}
 
 	public void setConnection() {
 		if (mMap == null) {
 			return;
 		}
 		if (!Preferences.isSetMarkers(getActivity()) || mLastBts == null || mLastBts.location == null || mLastLocation == null) {
 			removeConnection();
 			return;
 		}
 
 		// connection
 		if (mConnectionCurrent == null) {
 			final PolylineOptions polylineOpts = new PolylineOptions();
 			polylineOpts.zIndex(1010);
 			polylineOpts.width(getResources().getDimension(R.dimen.connection_width));
 			polylineOpts.color(getResources().getColor(R.color.connection_current));
 			polylineOpts.add(mLastBts.location);
 			polylineOpts.add(mLastLocation);
 
 			mConnectionCurrent = mMap.addPolyline(polylineOpts);
 		} else {
 			final List<LatLng> points = new ArrayList<LatLng>();
 			points.add(mLastBts.location);
 			points.add(mLastLocation);
 
 			mConnectionCurrent.setPoints(points);
 		}
 	}
 
 	public void removeConnection() {
 		if (mConnectionCurrent != null) {
 			mConnectionCurrent.remove();
 			mConnectionCurrent = null;
 		}
 	}
 
 	public void getCurrentCellInfo() {
 		getCurrentCellInfo(null);
 	}
 
 	public void getCurrentCellInfo(CellLocation cell) {
 		final String operator = mTelephony.getNetworkOperator();
 		final int type = mTelephony.getNetworkType();
 
 		if (cell == null) {
 			cell = mTelephony.getCellLocation();
 		}
 
 		if (cell instanceof GsmCellLocation) {
 			final GsmCellLocation cellGsm = (GsmCellLocation)cell;
 
 			mLastBts = BtsCache.update(operator, cellGsm.getLac(), cellGsm.getCid(), type);
 
 			if (mLastBts != null && !mBtsMarkers.containsKey(Bts.getId(mLastBts))) {
 				addBts(mLastBts);
 			}
 		} else if (cell instanceof CdmaCellLocation) {
 			Log.w(Constants.TAG, "CDMA location not implemented");
 		}
 
 		setConnection();
 	}
 
 	protected void resetTouched() {
 		if (mTouched == null) {
 			return;
 		}
 
 		resetCell(mTouched, -1, 0);
 		resetCell(mTouched, +1, 0);
 		resetCell(mTouched, 0, -1);
 		resetCell(mTouched, 0, +1);
 		if ((mTouched.y % 2) == 0) {
 			resetCell(mTouched, +1, -1);
 			resetCell(mTouched, +1, +1);
 		} else {
 			resetCell(mTouched, -1, -1);
 			resetCell(mTouched, -1, +1);
 		}
 	}
 
 	protected void darkenCell(XY xy, int relativeX, int relativeY) {
 		XY sector = new XY(xy.x + relativeX, xy.y + relativeY);
 		Polygon polygon = mCoveragePolygons.get(sector);
 		if (polygon != null) {
 			int getCurrentColor = polygon.getFillColor();
 			if (getCurrentColor == mFillColors[0]) {
 				polygon.setFillColor(mStrokeColors[0]);
 			} else if (getCurrentColor == mFillColors[1]) {
 				polygon.setFillColor(mStrokeColors[1]);
 			} else if (getCurrentColor == mFillColors[2]) {
 				polygon.setFillColor(mStrokeColors[2]);
 			} else if (getCurrentColor == mFillColors[3]) {
 				polygon.setFillColor(mStrokeColors[3]);
 			} else if (getCurrentColor == mFillColors[4]) {
 				polygon.setFillColor(mStrokeColors[4]);
 			}
 		}
 	}
 
 	protected void resetCell(XY xy, int relativeX, int relativeY) {
 		XY sector = new XY(xy.x + relativeX, xy.y + relativeY);
 		Polygon polygon = mCoveragePolygons.get(sector);
 		if (polygon != null) {
 			int getCurrentColor = polygon.getFillColor();
 			if (getCurrentColor == mStrokeColors[0]) {
 				polygon.setFillColor(mFillColors[0]);
 			} else if (getCurrentColor == mStrokeColors[1]) {
 				polygon.setFillColor(mFillColors[1]);
 			} else if (getCurrentColor == mStrokeColors[2]) {
 				polygon.setFillColor(mFillColors[2]);
 			} else if (getCurrentColor == mStrokeColors[3]) {
 				polygon.setFillColor(mFillColors[3]);
 			} else if (getCurrentColor == mStrokeColors[4]) {
 				polygon.setFillColor(mFillColors[4]);
 			}
 		}
 	}
 
 	// classes
 
 	public class CoverageClickListener implements GoogleMap.OnMapClickListener {
 
 		@Override
 		public void onMapClick(LatLng latLng) {
 			resetTouched();
 
 			final XY xy = LocationUtil.getSectorXY(latLng);
 
 			Sector sector = SectorCache.get(xy);
 			if (sector == null) {
 				sector = new Sector();
 				sector.index = xy;
 				sector.center = LocationUtil.getSectorCenter(xy);
 				sector.corners = LocationUtil.getSectorHexagon(sector.center);
 			}
 
 			final PolygonOptions polygonOpts = new PolygonOptions();
 			polygonOpts.strokeWidth(getResources().getDimension(R.dimen.touched_margin));
 			polygonOpts.strokeColor(getResources().getColor(R.color.white));
 			polygonOpts.fillColor(getResources().getColor(R.color.none));
 			polygonOpts.zIndex(1000);
 			polygonOpts.addAll(sector.getCorners());
 
 			if (mCoverageTouch != null) {
 				mCoverageTouch.remove();
 			}
 			mCoverageTouch = mMap.addPolygon(polygonOpts);
 
 			darkenCell(xy, -1, 0);
 			darkenCell(xy, +1, 0);
 			darkenCell(xy, 0, -1);
 			darkenCell(xy, 0, +1);
 			if ((xy.y % 2) == 0) {
 				darkenCell(xy, +1, -1);
 				darkenCell(xy, +1, +1);
 			} else {
 				darkenCell(xy, -1, -1);
 				darkenCell(xy, -1, +1);
 			}
 
 			mTouched = xy;
 
 			MainActivity activity = (MainActivity)getActivity();
 			activity.displayInfo(sector);
 		}
 	}
 
 	public class MapMoveListener implements GoogleMap.OnCameraChangeListener {
 
 		public void onCameraChange(CameraPosition position) {
 			if (mLastLocation == null) {
 				return;
 			}
 
 			final double distance = LocationUtil.getDistance(mLastLocation, position.target);
 			if (distance > 20) {
 				mCentering = false;
 			} else {
 				mCentering = true;
 			}
 		}
 	}
 
 	public class StatusListener extends PhoneStateListener {
 
 		@Override
 		public void onCellLocationChanged(CellLocation cell) {
 			getCurrentCellInfo(cell);
 		}
 
 		@Override
 		public void onDataActivity(int direction) {
 			getCurrentCellInfo();
 		}
 
 		@Override
 		public void onCellInfoChanged(List<CellInfo> info) {
 			getCurrentCellInfo();
 		}
 	}
 }
