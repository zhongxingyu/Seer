 // Created by plusminus on 22:01:11 - 29.09.2008
 package org.osmdroid.views.overlay;
 
 import java.util.LinkedList;
import java.util.List;
 
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.LocationListenerProxy;
 import org.osmdroid.ResourceProxy;
 import org.osmdroid.SensorEventListenerProxy;
 import org.osmdroid.api.IMyLocationOverlay;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.util.LocationUtils;
 import org.osmdroid.util.NetworkLocationIgnorer;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.MapView.Projection;
 import org.osmdroid.views.overlay.Overlay.Snappable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Picture;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 
 /**
  * 
  * @author Manuel Stahl
  * 
  */
 public class MyLocationOverlay extends Overlay implements IMyLocationOverlay, IOverlayMenuProvider,
 		SensorEventListener, LocationListener, Snappable {
 
 	private static final Logger logger = LoggerFactory.getLogger(MyLocationOverlay.class);
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	protected final Paint mPaint = new Paint();
 	protected final Paint mCirclePaint = new Paint();
 
 	protected final Bitmap PERSON_ICON;
 	protected final Bitmap DIRECTION_ARROW;
 
 	protected final MapView mMapView;
 
 	private final MapController mMapController;
 	private final LocationManager mLocationManager;
 	private final SensorManager mSensorManager;
 
 	public LocationListenerProxy mLocationListener = null;
 	public SensorEventListenerProxy mSensorListener = null;
 
 	private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
 	private final Point mMapCoords = new Point();
 
 	private Location mLocation;
 	private long mLocationUpdateMinTime = 0;
 	private float mLocationUpdateMinDistance = 0.0f;
 	protected boolean mFollow = false; // follow location updates
 	protected boolean mDrawAccuracyEnabled = true;
 	private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
 
 	private final Matrix directionRotater = new Matrix();
 
 	/** Coordinates the feet of the person are located scaled for display density. */
 	protected final PointF PERSON_HOTSPOT;
 
 	private final float DIRECTION_ARROW_CENTER_X;
 	private final float DIRECTION_ARROW_CENTER_Y;
 
 	protected final Picture mCompassFrame = new Picture();
 	protected final Picture mCompassRose = new Picture();
 	private final Matrix mCompassMatrix = new Matrix();
 
 	// actual compass value. Note: this one is only changed when an actual compass value
 	// is being read, so a check >= 0 is valid
 	private float mAzimuth = -1.0f;
 
 	private float mCompassCenterX = 35.0f;
 	private float mCompassCenterY = 35.0f;
 	private final float mCompassRadius = 20.0f;
 
 	private final float COMPASS_FRAME_CENTER_X;
 	private final float COMPASS_FRAME_CENTER_Y;
 	private final float COMPASS_ROSE_CENTER_X;
 	private final float COMPASS_ROSE_CENTER_Y;
 
 	public static final int MENU_MY_LOCATION = getSafeMenuId();
 	public static final int MENU_COMPASS = getSafeMenuId();
 
 	private final float mScale;
 
 	private boolean mOptionsMenuEnabled = true;
 
 	// to avoid allocations during onDraw
 	private final float[] mMatrixValues = new float[9];
 	private final GeoPoint mGeoPoint = new GeoPoint(0, 0);
 	private final Matrix mMatrix = new Matrix();
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public MyLocationOverlay(final Context ctx, final MapView mapView) {
 		this(ctx, mapView, new DefaultResourceProxyImpl(ctx));
 	}
 
 	public MyLocationOverlay(final Context ctx, final MapView mapView,
 			final ResourceProxy pResourceProxy) {
 		super(pResourceProxy);
 		mMapView = mapView;
 		mLocationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
 		mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
 		mMapController = mapView.getController();
 		mCirclePaint.setARGB(0, 100, 100, 255);
 		mCirclePaint.setAntiAlias(true);
 
 		PERSON_ICON = mResourceProxy.getBitmap(ResourceProxy.bitmap.person);
 		DIRECTION_ARROW = mResourceProxy.getBitmap(ResourceProxy.bitmap.direction_arrow);
 
 		DIRECTION_ARROW_CENTER_X = DIRECTION_ARROW.getWidth() / 2 - 0.5f;
 		DIRECTION_ARROW_CENTER_Y = DIRECTION_ARROW.getHeight() / 2 - 0.5f;
 
 		mScale = ctx.getResources().getDisplayMetrics().density;
 
 		// Calculate position of person icon's feet, scaled to screen density
 		PERSON_HOTSPOT = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);
 
 		createCompassFramePicture();
 		createCompassRosePicture();
 
 		COMPASS_FRAME_CENTER_X = mCompassFrame.getWidth() / 2 - 0.5f;
 		COMPASS_FRAME_CENTER_Y = mCompassFrame.getHeight() / 2 - 0.5f;
 		COMPASS_ROSE_CENTER_X = mCompassRose.getWidth() / 2 - 0.5f;
 		COMPASS_ROSE_CENTER_Y = mCompassRose.getHeight() / 2 - 0.5f;

		final List<Sensor> mOrientationSensors = mSensorManager
				.getSensorList(Sensor.TYPE_ORIENTATION);
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public long getLocationUpdateMinTime() {
 		return mLocationUpdateMinTime;
 	}
 
 	/**
 	 * Set the minimum interval for location updates. See {@link
 	 * LocationManager.requestLocationUpdates(String, long, float, LocationListener)}. Note that you
 	 * should call this before calling {@link enableMyLocation()}.
 	 * 
 	 * @param milliSeconds
 	 */
 	public void setLocationUpdateMinTime(final long milliSeconds) {
 		mLocationUpdateMinTime = milliSeconds;
 	}
 
 	public float getLocationUpdateMinDistance() {
 		return mLocationUpdateMinDistance;
 	}
 
 	/**
 	 * Set the minimum distance for location updates. See
 	 * {@link LocationManager.requestLocationUpdates}. Note that you should call this before calling
 	 * {@link enableMyLocation()}.
 	 * 
 	 * @param meters
 	 */
 	public void setLocationUpdateMinDistance(final float meters) {
 		mLocationUpdateMinDistance = meters;
 	}
 
 	public void setCompassCenter(final float x, final float y) {
 		mCompassCenterX = x;
 		mCompassCenterY = y;
 	}
 
 	/**
 	 * If enabled, an accuracy circle will be drawn around your current position.
 	 * 
 	 * @param drawAccuracyEnabled
 	 *            whether the accuracy circle will be enabled
 	 */
 	public void setDrawAccuracyEnabled(boolean drawAccuracyEnabled) {
 		mDrawAccuracyEnabled = drawAccuracyEnabled;
 	}
 
 	/**
 	 * If enabled, an accuracy circle will be drawn around your current position.
 	 * 
 	 * @return true if enabled, false otherwise
 	 */
 	public boolean isDrawAccuracyEnabled() {
 		return mDrawAccuracyEnabled;
 	}
 
 	// ===========================================================
 	// Methods from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
 
 		if (shadow) {
 			return;
 		}
 
 		// Note - all azimuths are offset by -90 when in landscape mode. This is because the
 		// hardware does not change orientation when physically flipped, but Android changes the
 		// screen coordinates therefore it will be off by 90 degrees. This assumes that Android only
 		// allows two screen rotations - 0 degrees (portrait) and 90 degrees (landscape) and does
 		// not permit 180 or 270 degrees (upside-down portrait and upside-down landscape
 		// respectively). This is probably a bad assumption, so maybe there is a better way to do
 		// this. SensorManager.remapCoordinateSystem might be able to help.
 
 		final int azimuthRotationOffset = (osmv.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? -90
 				: 0);
 
 		if (mLocationListener != null && mLocation != null) {
 			final Projection pj = osmv.getProjection();
 			mGeoPoint.setCoordsE6((int) (mLocation.getLatitude() * 1E6),
 					(int) (mLocation.getLongitude() * 1E6));
 			pj.toMapPixels(mGeoPoint, mMapCoords);
 
 			if (mDrawAccuracyEnabled) {
 				final float radius = pj.metersToEquatorPixels(this.mLocation.getAccuracy());
 
 				this.mCirclePaint.setAlpha(50);
 				this.mCirclePaint.setStyle(Style.FILL);
 				c.drawCircle(mMapCoords.x, mMapCoords.y, radius, this.mCirclePaint);
 
 				this.mCirclePaint.setAlpha(150);
 				this.mCirclePaint.setStyle(Style.STROKE);
 				c.drawCircle(mMapCoords.x, mMapCoords.y, radius, this.mCirclePaint);
 			}
 
 			c.getMatrix(mMatrix);
 			mMatrix.getValues(mMatrixValues);
 
 			if (DEBUGMODE) {
 				final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
 						/ mMatrixValues[Matrix.MSCALE_X];
 				final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
 						/ mMatrixValues[Matrix.MSCALE_Y];
 				c.drawText("Lat: " + mLocation.getLatitude(), tx, ty + 5, this.mPaint);
 				c.drawText("Lon: " + mLocation.getLongitude(), tx, ty + 20, this.mPaint);
 				c.drawText("Alt: " + mLocation.getAltitude(), tx, ty + 35, this.mPaint);
 				c.drawText("Acc: " + mLocation.getAccuracy(), tx, ty + 50, this.mPaint);
 			}
 
 			float bearing = -1.0f;
 			if (mLocation.getProvider().equals(LocationManager.GPS_PROVIDER) && mAzimuth >= 0.0f) {
 				// if GPS and compass is available, use compass value
 				bearing = mAzimuth;
 			} else if (mLocation.hasSpeed() && mLocation.getSpeed() > 1 && mLocation.hasBearing()) {
 				// use bearing if available and if we're actually moving
 				// XXX do we really need to test for speed > 1, or maybe better
 				// some number other than 1
 				bearing = this.mLocation.getBearing();
 			}
 
 			if (bearing >= 0.0f) {
 				/*
 				 * Rotate the direction-Arrow according to the bearing we are driving. And draw it
 				 * to the canvas.
 				 */
 				this.directionRotater.setRotate(-bearing + azimuthRotationOffset,
 						DIRECTION_ARROW_CENTER_X, DIRECTION_ARROW_CENTER_Y);
 
 				this.directionRotater.postTranslate(-DIRECTION_ARROW_CENTER_X,
 						-DIRECTION_ARROW_CENTER_Y);
 				this.directionRotater.postScale(1 / mMatrixValues[Matrix.MSCALE_X],
 						1 / mMatrixValues[Matrix.MSCALE_Y]);
 				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
 				c.drawBitmap(DIRECTION_ARROW, this.directionRotater, this.mPaint);
 			} else {
 				this.directionRotater.setTranslate(-PERSON_HOTSPOT.x, -PERSON_HOTSPOT.y);
 				this.directionRotater.postScale(1 / mMatrixValues[Matrix.MSCALE_X],
 						1 / mMatrixValues[Matrix.MSCALE_Y]);
 				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
 				c.drawBitmap(PERSON_ICON, this.directionRotater, this.mPaint);
 			}
 		}
 
 		if ((isCompassEnabled()) && (mAzimuth >= 0.0f)) {
 			final float centerX = mCompassCenterX * mScale;
 			final float centerY = mCompassCenterY * mScale + (c.getHeight() - mMapView.getHeight());
 
 			this.mCompassMatrix.setTranslate(-COMPASS_FRAME_CENTER_X, -COMPASS_FRAME_CENTER_Y);
 			this.mCompassMatrix.postTranslate(centerX, centerY);
 
 			c.save();
 			c.setMatrix(mCompassMatrix);
 			c.drawPicture(mCompassFrame);
 
 			this.mCompassMatrix.setRotate(-mAzimuth + azimuthRotationOffset, COMPASS_ROSE_CENTER_X,
 					COMPASS_ROSE_CENTER_Y);
 			this.mCompassMatrix.postTranslate(-COMPASS_ROSE_CENTER_X, -COMPASS_ROSE_CENTER_Y);
 			this.mCompassMatrix.postTranslate(centerX, centerY);
 
 			c.setMatrix(mCompassMatrix);
 			c.drawPicture(mCompassRose);
 			c.restore();
 		}
 	}
 
 	@Override
 	public void onLocationChanged(final Location location) {
 		if (DEBUGMODE) {
 			logger.debug("onLocationChanged(" + location + ")");
 		}
 
 		// ignore temporary non-gps fix
 		if (mIgnorer.shouldIgnore(location.getProvider(), System.currentTimeMillis())) {
 			logger.debug("Ignore temporary non-gps location");
 			return;
 		}
 
 		mLocation = location;
 		if (mFollow) {
 			mMapController.animateTo(new GeoPoint(location));
 		} else {
 			mMapView.postInvalidate(); // redraw the my location icon
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(final String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(final String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
 		if (status == LocationProvider.AVAILABLE) {
 			final Thread t = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					for (final Runnable runnable : mRunOnFirstFix) {
 						runnable.run();
 					}
 					mRunOnFirstFix.clear();
 				}
 			});
 			t.run();
 		}
 	}
 
 	@Override
 	public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
 			final MapView mapView) {
 		if (this.mLocation != null) {
 			final Projection pj = mapView.getProjection();
 			pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
 			snapPoint.x = mMapCoords.x;
 			snapPoint.y = mMapCoords.y;
 			final double xDiff = x - mMapCoords.x;
 			final double yDiff = y - mMapCoords.y;
 			final boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
 			if (DEBUGMODE) {
 				logger.debug("snap=" + snap);
 			}
 			return snap;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			disableFollowLocation();
 		}
 
 		return super.onTouchEvent(event, mapView);
 	}
 
 	@Override
 	public void onAccuracyChanged(final Sensor arg0, final int arg1) {
 		// This is not interesting for us at the moment
 	}
 
 	@Override
 	public void onSensorChanged(final SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
 			if (event.values != null) {
 				mAzimuth = event.values[0];
 				mMapView.postInvalidate();
 			}
 		}
 	}
 
 	// ===========================================================
 	// Menu handling methods
 	// ===========================================================
 
 	@Override
 	public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
 		this.mOptionsMenuEnabled = pOptionsMenuEnabled;
 	}
 
 	@Override
 	public boolean isOptionsMenuEnabled() {
 		return this.mOptionsMenuEnabled;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
 			final MapView pMapView) {
 		pMenu.add(0, MENU_MY_LOCATION + pMenuIdOffset, Menu.NONE,
 				mResourceProxy.getString(ResourceProxy.string.my_location)).setIcon(
 				mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_mylocation));
 
 		pMenu.add(0, MENU_COMPASS + pMenuIdOffset, Menu.NONE,
 				mResourceProxy.getString(ResourceProxy.string.compass)).setIcon(
 				mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_compass));
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
 			final MapView pMapView) {
 		return false;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(final int pFeatureId, final MenuItem pItem,
 			final int pMenuIdOffset, final MapView pMapView) {
 		final int menuId = pItem.getItemId() - pMenuIdOffset;
 		if (menuId == MENU_MY_LOCATION) {
 			if (this.isMyLocationEnabled()) {
 				this.disableFollowLocation();
 				this.disableMyLocation();
 			} else {
 				this.enableFollowLocation();
 				this.enableMyLocation();
 			}
 			return true;
 		} else if (menuId == MENU_COMPASS) {
 			if (this.isCompassEnabled()) {
 				this.disableCompass();
 			} else {
 				this.enableCompass();
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	/**
 	 * Return a GeoPoint of the last known location, or null if not known.
 	 */
 	public GeoPoint getMyLocation() {
 		if (mLocation == null) {
 			return null;
 		} else {
 			return new GeoPoint(mLocation);
 		}
 	}
 
 	@Override
 	public Location getLastFix() {
 		return mLocation;
 	}
 
 	/**
 	 * Enables "follow" functionality. The map will center on your current location and
 	 * automatically scroll as you move. Scrolling the map in the UI will disable.
 	 */
 	public void enableFollowLocation() {
 		mFollow = true;
 
 		// set initial location when enabled
 		if (isMyLocationEnabled()) {
 			mLocation = LocationUtils.getLastKnownLocation(mLocationManager);
 			if (mLocation != null) {
 				mMapController.animateTo(new GeoPoint(mLocation));
 			}
 		}
 
 		// Update the screen to see changes take effect
 		if (mMapView != null) {
 			mMapView.postInvalidate();
 		}
 	}
 
 	/**
 	 * Disables "follow" functionality.
 	 */
 	public void disableFollowLocation() {
 		mFollow = false;
 	}
 
 	/**
 	 * If enabled, the map will center on your current location and automatically scroll as you
 	 * move. Scrolling the map in the UI will disable.
 	 * 
 	 * @return true if enabled, false otherwise
 	 */
 	public boolean isFollowLocationEnabled() {
 		return mFollow;
 	}
 
 	/**
 	 * Enable location updates and show your current location on the map. By default this will
 	 * request location updates as frequently as possible, but you can change the frequency and/or
 	 * distance by calling {@link setLocationUpdateMinTime(long)} and/or {@link
 	 * setLocationUpdateMinDistance(float)} before calling this method. You will want to call
 	 * enableMyLocation() probably from your Activity's Activity.onResume() method, to enable the
 	 * features of this overlay. Remember to call the corresponding disableMyLocation() in your
 	 * Activity's Activity.onPause() method to turn off updates when in the background.
 	 */
 	@Override
 	public boolean enableMyLocation() {
 		if (mLocationListener == null) {
 			mLocationListener = new LocationListenerProxy(mLocationManager);
 			mLocationListener.startListening(this, mLocationUpdateMinTime,
 					mLocationUpdateMinDistance);
 		}
 
 		// set initial location when enabled
 		if (isFollowLocationEnabled()) {
 			mLocation = LocationUtils.getLastKnownLocation(mLocationManager);
 			if (mLocation != null) {
 				mMapController.animateTo(new GeoPoint(mLocation));
 			}
 		}
 
 		// Update the screen to see changes take effect
 		if (mMapView != null) {
 			mMapView.postInvalidate();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Disable location updates
 	 */
 	@Override
 	public void disableMyLocation() {
 		if (mLocationListener != null) {
 			mLocationListener.stopListening();
 		}
 
 		mLocationListener = null;
 
 		// Update the screen to see changes take effect
 		if (mMapView != null) {
 			mMapView.postInvalidate();
 		}
 	}
 
 	/**
 	 * If enabled, the map is receiving location updates and drawing your location on the map.
 	 * 
 	 * @return true if enabled, false otherwise
 	 */
 	@Override
 	public boolean isMyLocationEnabled() {
 		return mLocationListener != null;
 	}
 
 	/**
 	 * Enable orientation sensor (compass) updates and show a compass on the map. You will want to
 	 * call enableCompass() probably from your Activity's Activity.onResume() method, to enable the
 	 * features of this overlay. Remember to call the corresponding disableCompass() in your
 	 * Activity's Activity.onPause() method to turn off updates when in the background.
 	 */
 	@Override
 	public boolean enableCompass() {
 		boolean result = true;
 		if (mSensorListener == null) {
			final Sensor sensorOrientation = this.mSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);

			if (sensorOrientation == null)
				return false;

 			mSensorListener = new SensorEventListenerProxy(mSensorManager);
 			mSensorListener.startListening(this, Sensor.TYPE_ORIENTATION,
 					SensorManager.SENSOR_DELAY_UI);

			result = mSensorManager.registerListener(mSensorListener, sensorOrientation,
					SensorManager.SENSOR_DELAY_UI);
 		}
 
 		// Update the screen to see changes take effect
 		if (mMapView != null) {
 			mMapView.postInvalidate();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Disable orientation updates
 	 */
 	@Override
 	public void disableCompass() {
 		if (mSensorListener != null) {
			mSensorManager.unregisterListener(mSensorListener);
 		}
 
 		mSensorListener = null;
 		mAzimuth = -1.0f;
 
 		// Update the screen to see changes take effect
 		if (mMapView != null) {
 			mMapView.postInvalidate();
 		}
 	}
 
 	/**
 	 * If enabled, the map is receiving orientation updates and drawing your location on the map.
 	 * 
 	 * @return true if enabled, false otherwise
 	 */
 	@Override
 	public boolean isCompassEnabled() {
 		return mSensorListener != null;
 	}
 
 	@Override
 	public boolean runOnFirstFix(final Runnable runnable) {
 		if (mLocationListener != null && mLocation != null) {
 			runnable.run();
 			return true;
 		} else {
 			mRunOnFirstFix.addLast(runnable);
 			return false;
 		}
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	private Point calculatePointOnCircle(final float centerX, final float centerY,
 			final float radius, final float degrees) {
 		// for trigonometry, 0 is pointing east, so subtract 90
 		// compass degrees are the wrong way round
 		final double dblRadians = Math.toRadians(-degrees + 90);
 
 		final int intX = (int) (radius * Math.cos(dblRadians));
 		final int intY = (int) (radius * Math.sin(dblRadians));
 
 		return new Point((int) centerX + intX, (int) centerY - intY);
 	}
 
 	private void drawTriangle(final Canvas canvas, final float x, final float y,
 			final float radius, final float degrees, final Paint paint) {
 		canvas.save();
 		final Point point = this.calculatePointOnCircle(x, y, radius, degrees);
 		canvas.rotate(degrees, point.x, point.y);
 		final Path p = new Path();
 		p.moveTo(point.x - 2 * mScale, point.y);
 		p.lineTo(point.x + 2 * mScale, point.y);
 		p.lineTo(point.x, point.y - 5 * mScale);
 		p.close();
 		canvas.drawPath(p, paint);
 		canvas.restore();
 	}
 
 	private void createCompassFramePicture() {
 		// The inside of the compass is white and transparent
 		final Paint innerPaint = new Paint();
 		innerPaint.setColor(Color.WHITE);
 		innerPaint.setAntiAlias(true);
 		innerPaint.setStyle(Style.FILL);
 		innerPaint.setAlpha(200);
 
 		// The outer part (circle and little triangles) is gray and transparent
 		final Paint outerPaint = new Paint();
 		outerPaint.setColor(Color.GRAY);
 		outerPaint.setAntiAlias(true);
 		outerPaint.setStyle(Style.STROKE);
 		outerPaint.setStrokeWidth(2.0f);
 		outerPaint.setAlpha(200);
 
 		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
 		final int center = picBorderWidthAndHeight / 2;
 
 		final Canvas canvas = mCompassFrame.beginRecording(picBorderWidthAndHeight,
 				picBorderWidthAndHeight);
 
 		// draw compass inner circle and border
 		canvas.drawCircle(center, center, mCompassRadius * mScale, innerPaint);
 		canvas.drawCircle(center, center, mCompassRadius * mScale, outerPaint);
 
 		// Draw little triangles north, south, west and east (don't move)
 		// to make those move use "-bearing + 0" etc. (Note: that would mean to draw the triangles
 		// in the onDraw() method)
 		drawTriangle(canvas, center, center, mCompassRadius * mScale, 0, outerPaint);
 		drawTriangle(canvas, center, center, mCompassRadius * mScale, 90, outerPaint);
 		drawTriangle(canvas, center, center, mCompassRadius * mScale, 180, outerPaint);
 		drawTriangle(canvas, center, center, mCompassRadius * mScale, 270, outerPaint);
 
 		mCompassFrame.endRecording();
 	}
 
 	private void createCompassRosePicture() {
 		// Paint design of north triangle (it's common to paint north in red color)
 		final Paint northPaint = new Paint();
 		northPaint.setColor(0xFFA00000);
 		northPaint.setAntiAlias(true);
 		northPaint.setStyle(Style.FILL);
 		northPaint.setAlpha(220);
 
 		// Paint design of south triangle (black)
 		final Paint southPaint = new Paint();
 		southPaint.setColor(Color.BLACK);
 		southPaint.setAntiAlias(true);
 		southPaint.setStyle(Style.FILL);
 		southPaint.setAlpha(220);
 
 		// Create a little white dot in the middle of the compass rose
 		final Paint centerPaint = new Paint();
 		centerPaint.setColor(Color.WHITE);
 		centerPaint.setAntiAlias(true);
 		centerPaint.setStyle(Style.FILL);
 		centerPaint.setAlpha(220);
 
 		// final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 * mScale);
 		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
 		final int center = picBorderWidthAndHeight / 2;
 
 		final Canvas canvas = mCompassRose.beginRecording(picBorderWidthAndHeight,
 				picBorderWidthAndHeight);
 
 		// Blue triangle pointing north
 		final Path pathNorth = new Path();
 		pathNorth.moveTo(center, center - (mCompassRadius - 3) * mScale);
 		pathNorth.lineTo(center + 4 * mScale, center);
 		pathNorth.lineTo(center - 4 * mScale, center);
 		pathNorth.lineTo(center, center - (mCompassRadius - 3) * mScale);
 		pathNorth.close();
 		canvas.drawPath(pathNorth, northPaint);
 
 		// Red triangle pointing south
 		final Path pathSouth = new Path();
 		pathSouth.moveTo(center, center + (mCompassRadius - 3) * mScale);
 		pathSouth.lineTo(center + 4 * mScale, center);
 		pathSouth.lineTo(center - 4 * mScale, center);
 		pathSouth.lineTo(center, center + (mCompassRadius - 3) * mScale);
 		pathSouth.close();
 		canvas.drawPath(pathSouth, southPaint);
 
 		// Draw a little white dot in the middle
 		canvas.drawCircle(center, center, 2, centerPaint);
 
 		mCompassRose.endRecording();
 	}
 }
