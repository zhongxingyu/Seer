 package co.shoutbreak.ui;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.Paint.Style;
 import android.graphics.Path.Direction;
 import android.location.Location;
 
 import co.shoutbreak.R;
 import co.shoutbreak.core.C;
 import co.shoutbreak.core.utils.SBLog;
 import co.shoutbreak.storage.User;
 import co.shoutbreak.ui.CustomMapView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 //If this crashes DroidX, use FixedMyLocationOverlay
 //http://stackoverflow.com/questions/753793/how-can-i-use-a-custom-bitmap-for-the-you-are-here-point-in-a-mylocationoverlay
 //http://www.gitorious.net/android-maps-api/android-maps-api/blobs/42614538ffda1a6985c398933a85fcd9afc752ee/src/com/google/android/maps/MyLocationOverlay.java
 public class UserLocationOverlay extends MyLocationOverlay {
 	
 	private static final String TAG = "UserLocationOverlay";
 
 	private Shoutbreak _ui;
 	private Canvas _canvas;
 	private CustomMapView _map;
 	private Paint _circleFillPaint;
 	private Paint _circleBorderPaint;
 	private Bitmap _resizeIcon;
 	private double _density;
 
 	private boolean _baseRadiusPxIsWrong;				// is the radius calculation based on the current zoom level? or is the pixel value wrong?
 	private boolean _calibrateZoomLevelForRadiusSize;	// have we calibrated for the best zoom level yet?
 
 	private Point _resizeIconSize;		// icon dimensions in pixels
 	private Point _resizeIconLocation;	// icon location in pixels
 	private Point _userLocationPx;		// user location in pixels
 
 	private GeoPoint _lastUserLocationGeoPoint; // the last user coordinates
 	private GeoPoint _lastTopRadiusGeoPoint; 	// the coordinates of the top of the current radius
 
 	private final double SQRT2 = (double) 1.4142;	// square root of 2, for hypotenuse of 45, 45, 90 triangle, for resize icon location
 	private float _baseRadiusMeters;// radius length in meters of baseRadius (baseRadius is radius length with no user resizing)
 	private float _baseRadiusPx; 	// baseRadius in pixels
 	private float _currentRadiusPx; // currently displayed radius (baseRadius + resizeAdjustment)
 
 	private double _baseTopRadiusAbsoluteMeters;	// how many meters north of equator is the top of baseRadius
 
 	private int _resizeAdjustmentPx;// user circle resize, in pixels
 	private int _zoomLevel;			// current zoom level of map
 	private int _mapSizeConstraint; // min(map width, map height)
 
 	private int _latRadiusLatForPeopleCount;
 	private int _peopleCount;
 
 	public void handleDensityChange(double newDensity, int newLevel) {
 		handleRadiusChange(newDensity, newLevel);
 	}
 	
 	public void handleLevelUp(double newDensity, int newLevel) {
 		handleRadiusChange(newDensity, newLevel);
 	}
 	
 	private void handleRadiusChange(double newDensity, int newLevel) {
 		_density = newDensity;
 		_baseRadiusMeters = User.calculateRadius(newLevel, newDensity);
 		_calibrateZoomLevelForRadiusSize = true;
     	_baseRadiusPxIsWrong = true;
     	_resizeAdjustmentPx = 0;
     	// this calls draw() immediately rather than wait for next interval
     	//if (_canvas != null && _mapView != null) {
     		//_mapView.draw(_canvas);
     	//}	
 	}
 	
 	/* TODO: not sure what to do with this
 	public void update(Observable observable, Object data) {
 		/* TODO: not sure what to do with this
 		StateManager stateManager = (StateManager)observable;
 		StateEvent e = (StateEvent)data;
 		if (e.locationTurnedOn) {
 			// TODO: toggleLocationTracker in Service
 			enableMyLocation();
 			stateManager.setIsUserOverlayUsingGPS(true);
 		}
 		if (e.locationTurnedOff) {
 			disableMyLocation();
 			stateManager.setIsUserOverlayUsingGPS(false);
 		}		
 		if (e.densityChanged || e.levelChanged) {
 			_density = _ui.getUser().getCellDensity().density;
 			_baseRadiusMeters = User.calculateRadius(_ui.getUser().getLevel(), _density);
 			_calibrateZoomLevelForRadiusSize = true;
 			_baseRadiusPxIsWrong = true;
 			_resizeAdjustmentPx = 0;
 			// this calls draw() immediately rather than wait for next interval
 			//if (_canvas != null && _mapView != null) {
 			//_mapView.draw(_canvas);
 			//}	
 		}
 	}*/
 
 	//public void updateUserInfo(int level, CellDensity cellDensity) {
 	//	_baseRadiusPxIsWrong = true;
 	//	_baseRadiusMeters = User.calculateRadius(level, _density);
 	//	_calibrateZoomLevelForRadiusSize = true;
 	//	_resizeAdjustmentPx = 0;
 	//	if (cellDensity.isSet) {
 	//		setPopulationDensity(level, cellDensity.density);
 	//	}
 	//	// int people = _ui.getUserInfo().getLevel() * C.CONFIG_PEOPLE_PER_LEVEL;
 	//	// _baseRadiusMeters = (float) Math.sqrt(people / (_density * Math.PI));
 	//}
 
 	public UserLocationOverlay(Shoutbreak ui, MapView map) {
 		super(ui, map);
 		SBLog.i(TAG, "new UserLocationOverlay()");
 		_ui = ui;		
 		_baseRadiusPx = -1;
 		_baseRadiusMeters = 0;
 		_zoomLevel = C.DEFAULT_ZOOM_LEVEL;
 		_baseRadiusPxIsWrong = true;
 		_calibrateZoomLevelForRadiusSize = false;
 		_mapSizeConstraint = -1;
 		_resizeAdjustmentPx = 0;
 		_latRadiusLatForPeopleCount = 0;
 		_peopleCount = 0;
 		_circleFillPaint = new Paint();
 		_circleFillPaint.setARGB(50, 108, 10, 171);
 		_circleFillPaint.setAntiAlias(true);
 		_circleBorderPaint = new Paint();
 		_circleBorderPaint.setAntiAlias(true);
 		_circleBorderPaint.setARGB(120, 69, 3, 111);
 		_circleBorderPaint.setStyle(Style.STROKE);
 		_circleBorderPaint.setStrokeWidth(4);
 		Bitmap resizeBitmap = BitmapFactory.decodeResource(_ui.getResources(), R.drawable.resize_icon);
 		_resizeIconSize = new Point(resizeBitmap.getWidth(), resizeBitmap.getHeight());
 		_resizeIconLocation = new Point();
 		_resizeIcon = Bitmap.createBitmap(resizeBitmap, 0, 0, _resizeIconSize.x, _resizeIconSize.y);
 	}
 
 	@Override 
 	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
 		// called every time map is redrawn....   don't do anything heavy here
 		_lastUserLocationGeoPoint = myLocation;
 		_userLocationPx = mapView.getProjection().toPixels(_lastUserLocationGeoPoint, null);
 
 		if (_baseRadiusPxIsWrong) {
 			_baseRadiusPx = calculateRadiusPixelsAtCurrentZoomLevel();
 			_baseRadiusPxIsWrong = false;
 			calculateCurrentRadius();
 		}
 
 		// draw circle
 		Path path = new Path();
 		path.addCircle(_userLocationPx.x, _userLocationPx.y, _currentRadiusPx, Direction.CW);
 		canvas.drawCircle(_userLocationPx.x, _userLocationPx.y, _currentRadiusPx, _circleFillPaint);
 		canvas.drawPath(path, _circleBorderPaint);
 
 		// draw icon
 		double iconXYOffset = _currentRadiusPx / SQRT2;
 		_resizeIconLocation.x = (int) (_userLocationPx.x - iconXYOffset - (_resizeIconSize.x / 2));
 		_resizeIconLocation.y = (int) (_userLocationPx.y + iconXYOffset - (_resizeIconSize.y / 2));
 		canvas.drawBitmap(
 				_resizeIcon,
 				_resizeIconLocation.x,
 				_resizeIconLocation.y, 
 				null
 		);
 
 		if (_calibrateZoomLevelForRadiusSize) {
 			calibrateZoomLevelToShowCircle();
 		}
 
 		if (_canvas == null) {
 			_canvas = canvas;
 		}
 	}
 
 	// initializes pixel values based on radiusMeters
 	private float calculateRadiusPixelsAtCurrentZoomLevel() {
 		// converts lat/long & meters to pixels
 		// this is probably a little inaccurate, so only do when radiusMeters is initialized    	
 		double topRadiusLat = _lastUserLocationGeoPoint.getLatitudeE6() / 1E6;	// get latitude of user location
 		_baseTopRadiusAbsoluteMeters = topRadiusLat * C.DEGREE_LAT_IN_METERS; 		// convert that to meters
 		_baseTopRadiusAbsoluteMeters += _baseRadiusMeters; 										// add the radius to it
 		topRadiusLat = _baseTopRadiusAbsoluteMeters / C.DEGREE_LAT_IN_METERS; 			// convert back to degrees lat 
 		GeoPoint topRadiusGeoPoint = new GeoPoint((int) (topRadiusLat * 1E6), _lastUserLocationGeoPoint.getLongitudeE6());
 		Point topRadiusPx = _map.getProjection().toPixels(topRadiusGeoPoint, null);
 		return _userLocationPx.y - topRadiusPx.y; // downward y axis
 	}
 
 	// called after zoom occurs
 	public void handleZoomLevelChange() {
 		if (_lastTopRadiusGeoPoint != null) {	
 			Point topRadiusPx = _map.getProjection().toPixels(_lastTopRadiusGeoPoint, null);
 			_baseRadiusPx = _userLocationPx.y - topRadiusPx.y; // downward y axis
 			_resizeAdjustmentPx = 0;
 			calculateCurrentRadius();
 		}
 	}
 
 	// called when user drags resize icon
 	public void resize(int radiusChangePx) {
 		_resizeAdjustmentPx += radiusChangePx;
 
 		// force minimum radius size
 		if (_baseRadiusPx + _resizeAdjustmentPx < C.MIN_RADIUS_PX) {
 			_resizeAdjustmentPx = (int) (C.MIN_RADIUS_PX - _baseRadiusPx);
 		}
 		calculateCurrentRadius();
 	}
 
 	public void calculateCurrentRadius() {    	
 		_currentRadiusPx = _baseRadiusPx + _resizeAdjustmentPx;
 		_lastTopRadiusGeoPoint = _map.getProjection().fromPixels(_userLocationPx.x, (int) (_userLocationPx.y - _currentRadiusPx));
 		updatePeopleCount();
 	}
 
 	protected void updatePeopleCount() {
 		// prevents us from doing this way too often - let's only care every 50 microdegrees
 		if (Math.abs(_lastTopRadiusGeoPoint.getLatitudeE6() - _latRadiusLatForPeopleCount) > 50) {
 			_latRadiusLatForPeopleCount = _lastTopRadiusGeoPoint.getLatitudeE6();
 			Location l1 = new Location("GPS");
 			l1.setLatitude(_lastTopRadiusGeoPoint.getLatitudeE6() / 1E6);
 			l1.setLongitude(_lastTopRadiusGeoPoint.getLongitudeE6() / 1E6);
 			Location l2 = new Location("GPS");
 			l2.setLatitude(_lastUserLocationGeoPoint.getLatitudeE6() / 1E6);
 			l2.setLongitude(_lastUserLocationGeoPoint.getLongitudeE6() / 1E6);
 			float dist = l1.distanceTo(l2);
 			_peopleCount = (int)(Math.PI * dist * dist * _density);
 			// Tack an extra person on to display cuz it looks better.
 			// It's usually a little low - but we don't want people trying to shout beyond their level so leave it low.
			_ui.mapPeopleCountTv.setText(Integer.toString(_peopleCount + 1));
 		}
 	}
 
 	//public void setPopulationDensity(int level, double density) {
 	// TODO: don't hardcode level
 	// TODO: do we keep track of resize if density changes?  or just say fuck it?    	
 	//_density = density;
 	//_baseRadiusMeters = User.calculateRadius(level, _density);
 	//_calibrateZoomLevelForRadiusSize = true;
 	//_baseRadiusPxIsWrong = true;
 	// this calls draw() immediately rather than wait for next interval
 	//if (_canvas != null) {
 	//	_mapView.draw(_canvas);
 	//}
 	//}
 
 	public Point getResizeIconLocation() {
 		return _resizeIconLocation;
 	}
 
 	public void setMapView(CustomMapView mapView) {
 		_map = mapView;
 	}
 
 	// finds a zoom level that displays the circle nicely
 	// http://groups.google.com/group/google-maps-api/browse_thread/thread/6ff83431273c6adb/0f83700b2a7b4144
 	public void calibrateZoomLevelToShowCircle() {
 		if (_mapSizeConstraint == -1) {
 			_mapSizeConstraint = (_map.getWidth() >= _map.getHeight()) ? _map.getHeight() : _map.getWidth();
 		}
 		double diameter = _currentRadiusPx + _currentRadiusPx;
 		if (diameter > 0) {
 			double factor = _mapSizeConstraint / diameter;
 			// each zoom level shows half as much as the previous
 			double zoomChange = Math.log(factor) / Math.log(2); // = log2(factor)
 			int flooredZoomChange = (int) Math.floor(zoomChange);
 			_zoomLevel += flooredZoomChange;
 		} else {
 			_zoomLevel = _map.getMaxZoomLevel() - 1;
 		}
 
 		// TODO: fancy smooth zooming
 		// TODO: can we use zoom to show a given lat/long span
 
 		_map.getController().setZoom(_zoomLevel);
 		_calibrateZoomLevelForRadiusSize = false;
 		_baseRadiusPxIsWrong = true;
 	}
 	
 	public int getCurrentPower() {
 		SBLog.i(TAG, "getCurrentPower()");
 		return User.calculatePower(_peopleCount);
 	}
 
 	// distance math if we need it
 	//GeoPoint radiusEdge = _mapView.getProjection().fromPixels((int)(_userLocationPx.x + _radiusPx), _userLocationPx.y);
 	//Location location1 = new Location("gps");
 	//location1.setLatitude(radiusEdge.getLatitudeE6() / 1E6);
 	//location1.setLongitude(radiusEdge.getLongitudeE6() / 1E6);
 	//Location location2 = new Location("gps");
 	//location2.setLatitude(_lastUserLocation.getLatitudeE6() / 1E6);
 	//location2.setLongitude(_lastUserLocation.getLongitudeE6() / 1E6);
 	//_radiusMeters = location2.distanceTo(location1);
 
 }
