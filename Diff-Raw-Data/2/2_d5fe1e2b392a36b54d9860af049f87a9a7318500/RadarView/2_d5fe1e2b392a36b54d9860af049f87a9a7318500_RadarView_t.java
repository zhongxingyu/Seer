 /*
  * Copyright (C) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.code.geobeagle.mainactivity;
 
 /*
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.google.code.geobeagle.R;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.BitmapDrawable;
 import android.hardware.SensorListener;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 @SuppressWarnings("deprecation")
 public class RadarView extends View implements SensorListener, LocationListener {
 
     private static final long RETAIN_GPS_MILLIS = 10000L;
     private Paint mGridPaint;
     private Paint mErasePaint;
     private float mOrientation;
     private double mTargetLat;
     private double mTargetLon;
     private double mMyLocationLat;
     private double mMyLocationLon;
     private int mLastScale = -1;
     private String[] mDistanceScale = new String[4];
 
     private static float KM_PER_METERS = 0.001f;
     private static float METERS_PER_KM = 1000f;
 
     /**
      * These are the list of choices for the radius of the outer circle on the
      * screen when using metric units. All items are in kilometers. This array
      * is used to choose the scale of the radar display.
      */
     private static double mMetricScaleChoices[] = {
             100 * KM_PER_METERS, 200 * KM_PER_METERS, 400 * KM_PER_METERS, 1, 2, 4, 8, 20, 40, 100,
             200, 400, 1000, 2000, 4000, 10000, 20000, 40000, 80000
     };
 
     /**
      * Once the scale is chosen, this array is used to convert the number of
      * kilometers on the screen to an integer. (Note that for short distances we
      * use meters, so we multiply the distance by {@link #METERS_PER_KM}. (This
      * array is for metric measurements.)
      */
     private static float mMetricDisplayUnitsPerKm[] = {
             METERS_PER_KM, METERS_PER_KM, METERS_PER_KM, METERS_PER_KM, METERS_PER_KM, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f
     };
 
     /**
      * This array holds the formatting string used to display the distance to
      * the target. (This array is for metric measurements.)
      */
     private static String mMetricDisplayFormats[] = {
             "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.1fkm", "%.1fkm", "%.0fkm", "%.0fkm",
             "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm",
             "%.0fkm", "%.0fkm"
     };
 
     /**
      * This array holds the formatting string used to display the distance on
      * each ring of the radar screen. (This array is for metric measurements.)
      */
     private static String mMetricScaleFormats[] = {
             "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm",
             "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm", "%.0fkm",
             "%.0fkm", "%.0fkm", "%.0fkm"
     };
 
     private static float KM_PER_YARDS = 0.0009144f;
     private static float KM_PER_MILES = 1.609344f;
     private static float YARDS_PER_KM = 1093.6133f;
     private static float MILES_PER_KM = 0.621371192f;
 
     /**
      * These are the list of choices for the radius of the outer circle on the
      * screen when using standard units. All items are in kilometers. This array
      * is used to choose the scale of the radar display.
      */
     private static double mEnglishScaleChoices[] = {
             100 * KM_PER_YARDS, 200 * KM_PER_YARDS, 400 * KM_PER_YARDS, 1000 * KM_PER_YARDS,
             1 * KM_PER_MILES, 2 * KM_PER_MILES, 4 * KM_PER_MILES, 8 * KM_PER_MILES,
             20 * KM_PER_MILES, 40 * KM_PER_MILES, 100 * KM_PER_MILES, 200 * KM_PER_MILES,
             400 * KM_PER_MILES, 1000 * KM_PER_MILES, 2000 * KM_PER_MILES, 4000 * KM_PER_MILES,
             10000 * KM_PER_MILES, 20000 * KM_PER_MILES, 40000 * KM_PER_MILES, 80000 * KM_PER_MILES
     };
 
     /**
      * Once the scale is chosen, this array is used to convert the number of
      * kilometers on the screen to an integer. (Note that for short distances we
      * use meters, so we multiply the distance by {@link #YARDS_PER_KM}. (This
      * array is for standard measurements.)
      */
     private static float mEnglishDisplayUnitsPerKm[] = {
             YARDS_PER_KM, YARDS_PER_KM, YARDS_PER_KM, YARDS_PER_KM, MILES_PER_KM, MILES_PER_KM,
             MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM,
             MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM, MILES_PER_KM,
             MILES_PER_KM, MILES_PER_KM
     };
 
     /**
      * This array holds the formatting string used to display the distance to
      * the target. (This array is for standard measurements.)
      */
     private static String mEnglishDisplayFormats[] = {
             "%.0fyd", "%.0fyd", "%.0fyd", "%.0fyd", "%.1fmi", "%.1fmi", "%.1fmi", "%.1fmi",
             "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi",
             "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi"
     };
 
     /**
      * This array holds the formatting string used to display the distance on
      * each ring of the radar screen. (This array is for standard measurements.)
      */
     private static String mEnglishScaleFormats[] = {
             "%.0fyd", "%.0fyd", "%.0fyd", "%.0fyd", "%.2fmi", "%.1fmi", "%.0fmi", "%.0fmi",
             "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi",
             "%.0fmi", "%.0fmi", "%.0fmi", "%.0fmi"
     };
 
     // True when we have know our own location
     private boolean mHaveLocation = false;
 
     // The view that will display the distance text
     private TextView mDistanceView;
 
     // Distance to target, in KM
     private double mDistance;
 
     // Bearing to target, in degrees
     private double mBearing;
 
     // Ratio of the distance to the target to the radius of the outermost ring
     // on the radar screen
     private float mDistanceRatio;
 
     // The bitmap used to draw the target
     private Bitmap mBlip;
 
     // Used to draw the animated ring that sweeps out from the center
     private Paint mSweepPaint0;
 
     // Used to draw the animated ring that sweeps out from the center
     private Paint mSweepPaint1;
 
     // Used to draw the animated ring that sweeps out from the center
     private Paint mSweepPaint2;
 
     // Time in millis when the most recent sweep began
     private long mSweepTime;
 
     // True if the sweep has not yet intersected the blip
     private boolean mSweepBefore;
 
     // Time in millis when the sweep last crossed the blip
     private long mBlipTime;
 
     // True if the display should use metric units; false if the display should
     // use standard units
     private boolean mUseMetric;
 
     // Time in millis for the last time GPS reported a location
     private long mLastGpsFixTime = 0L;
 
     // The last location reported by the network provider. Use this if we can't
     // get a location from GPS
     private Location mNetworkLocation;
 
     // True if GPS is reporting a location
     private boolean mGpsAvailable;
 
     // True if the network provider is reporting a location
     private boolean mNetworkAvailable;
 
     private TextView mBearingView;
     private float mMyLocationAccuracy;
     private TextView mAccuracyView;
 
     public RadarView(Context context) {
         this(context, null);
     }
 
     public RadarView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public RadarView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         // Paint used for the rings and ring text
         mGridPaint = new Paint();
         mGridPaint.setColor(0xFF00FF00);
         mGridPaint.setAntiAlias(true);
         mGridPaint.setStyle(Style.STROKE);
         mGridPaint.setStrokeWidth(1.0f);
         mGridPaint.setTextSize(10.0f);
         mGridPaint.setTextAlign(Align.CENTER);
 
         // Paint used to erase the rectangle behind the ring text
         mErasePaint = new Paint();
         mErasePaint.setColor(0xFF191919);
         mErasePaint.setStyle(Style.FILL);
 
         // Outer ring of the sweep
         mSweepPaint0 = new Paint();
         mSweepPaint0.setColor(0xFF33FF33);
         mSweepPaint0.setAntiAlias(true);
         mSweepPaint0.setStyle(Style.STROKE);
         mSweepPaint0.setStrokeWidth(2f);
         mSweepPaint0.setAlpha(100);
 
         // Middle ring of the sweep
         mSweepPaint1 = new Paint();
         mSweepPaint1.setColor(0x7733FF33);
         mSweepPaint1.setAntiAlias(true);
         mSweepPaint1.setStyle(Style.STROKE);
         mSweepPaint1.setStrokeWidth(2f);
         mSweepPaint1.setAlpha(100);
 
         // Inner ring of the sweep
         mSweepPaint2 = new Paint();
         mSweepPaint2.setColor(0x3333FF33);
         mSweepPaint2.setAntiAlias(true);
         mSweepPaint2.setStyle(Style.STROKE);
         mSweepPaint2.setStrokeWidth(2f);
         mSweepPaint2.setAlpha(100);
 
         mBlip = ((BitmapDrawable)getResources().getDrawable(R.drawable.blip)).getBitmap();
     }
 
     /**
      * Sets the target to track on the radar
      * 
      * @param latE6 Latitude of the target, multiplied by 1,000,000
      * @param lonE6 Longitude of the target, multiplied by 1,000,000
      */
     public void setTarget(int latE6, int lonE6) {
         mTargetLat = latE6 / (double)GeoUtils.MILLION;
         mTargetLon = lonE6 / (double)GeoUtils.MILLION;
     }
 
     /**
      * Sets the view that we will use to report distance
      * 
      * @param t The text view used to report distance
      */
     public void setDistanceView(TextView d, TextView b, TextView a) {
         mDistanceView = d;
         mBearingView = b;
         mAccuracyView = a;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         int center = Math.min(getHeight(), getWidth()) / 2;
         int radius = center - 8;
 
         // Draw the rings
         final Paint gridPaint = mGridPaint;
         gridPaint.setAlpha(100);
 
         canvas.drawCircle(center, center, radius, gridPaint);
         canvas.drawCircle(center, center, radius * 3 / 4, gridPaint);
         canvas.drawCircle(center, center, radius >> 1, gridPaint);
         canvas.drawCircle(center, center, radius >> 2, gridPaint);
 
         int blipRadius = (int)(mDistanceRatio * radius);
 
         final long now = SystemClock.uptimeMillis();
         if (mSweepTime > 0 && mHaveLocation) {
             // Draw the sweep. Radius is determined by how long ago it started
             long sweepDifference = now - mSweepTime;
             if (sweepDifference < 512L) {
                 int sweepRadius = (int)(((radius + 6) * sweepDifference) >> 9);
                 canvas.drawCircle(center, center, sweepRadius, mSweepPaint0);
                 canvas.drawCircle(center, center, sweepRadius - 2, mSweepPaint1);
                 canvas.drawCircle(center, center, sweepRadius - 4, mSweepPaint2);
 
                 // Note when the sweep has passed the blip
                 boolean before = sweepRadius < blipRadius;
                 if (!before && mSweepBefore) {
                     mSweepBefore = false;
                     mBlipTime = now;
                 }
             } else {
                 mSweepTime = now + 1000;
                 mSweepBefore = true;
             }
             postInvalidate();
         }
 
         // Draw horizontal and vertical lines
         canvas.drawLine(center, center - (radius >> 2) + 6, center, center - radius - 6, gridPaint);
         canvas.drawLine(center, center + (radius >> 2) - 6, center, center + radius + 6, gridPaint);
         canvas.drawLine(center - (radius >> 2) + 6, center, center - radius - 6, center, gridPaint);
         canvas.drawLine(center + (radius >> 2) - 6, center, center + radius + 6, center, gridPaint);
 
         // Draw X in the center of the screen
         canvas.drawLine(center - 4, center - 4, center + 4, center + 4, gridPaint);
         canvas.drawLine(center - 4, center + 4, center + 4, center - 4, gridPaint);
 
         if (mHaveLocation) {
             double northAngle = Math.toRadians(-mOrientation) - (Math.PI / 2);
             float northX = (float)Math.cos(northAngle);
             float northY = (float)Math.sin(northAngle);
             float tipX = northX * (radius - 12), tipY = northY * (radius - 12);
             float baseX = northY * 8, baseY = -northX * 8;
 
             // northern half
             gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
             final int saveColor = mGridPaint.getColor();
             gridPaint.setColor(Color.RED);
             gridPaint.setAlpha(180);
             Path path = new Path();
             path.moveTo(center + baseX, center + baseY);
             path.lineTo(center + tipX, center + tipY);
             path.lineTo(center - baseX, center - baseY);
             path.close();
             canvas.drawPath(path, gridPaint);
 
             // southern half
             gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
             gridPaint.setColor(Color.GRAY);
             gridPaint.setAlpha(180);
             path.reset();
             path.moveTo(center + baseX, center + baseY);
             path.lineTo(center - tipX, center - tipY);
             path.lineTo(center - baseX, center - baseY);
             path.close();
             canvas.drawPath(path, mGridPaint);
             gridPaint.setStyle(Paint.Style.STROKE);
 
             mGridPaint.setColor(saveColor);
             mGridPaint.setAlpha(255);
 
             double bearingToTarget = mBearing - mOrientation;
             double drawingAngle = Math.toRadians(bearingToTarget) - (Math.PI / 2);
 
             float cos = (float)Math.cos(drawingAngle);
             float sin = (float)Math.sin(drawingAngle);
 
             // Draw the blip. Alpha is based on how long ago the sweep crossed
             // the blip.
             long blipDifference = now - mBlipTime;
             gridPaint.setAlpha(255 - (int)((128 * blipDifference) >> 10));
             canvas.drawBitmap(mBlip, center + (cos * blipRadius) - 8, center + (sin * blipRadius)
                     - 8, gridPaint);
             gridPaint.setAlpha(255);
         }
     }
 
     public void onAccuracyChanged(int sensor, int accuracy) {
     }
 
     /**
      * Called when we get a new value from the compass
      * 
      * @see android.hardware.SensorListener#onSensorChanged(int, float[])
      */
     public void onSensorChanged(int sensor, float[] values) {
         mOrientation = values[0];
         double bearingToTarget = mBearing - mOrientation;
         updateBearing(bearingToTarget);
         postInvalidate();
     }
 
     /**
      * Called when a location provider has a new location to report
      * 
      * @see android.location.LocationListener#onLocationChanged(android.location.Location)
      */
     public void onLocationChanged(Location location) {
         Log.v("GeoBeagle", "radarview::onLocationChanged");
         if (!mHaveLocation) {
             mHaveLocation = true;
         }
 
         final long now = SystemClock.uptimeMillis();
         boolean useLocation = false;
         final String provider = location.getProvider();
         if (LocationManager.GPS_PROVIDER.equals(provider)) {
             // Use GPS if available
             mLastGpsFixTime = SystemClock.uptimeMillis();
             useLocation = true;
         } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
             // Use network provider if GPS is getting stale
             useLocation = now - mLastGpsFixTime > RETAIN_GPS_MILLIS;
             if (mNetworkLocation == null) {
                 mNetworkLocation = new Location(location);
             } else {
                 mNetworkLocation.set(location);
             }
 
             mLastGpsFixTime = 0L;
         }
         if (useLocation) {
             mMyLocationLat = location.getLatitude();
             mMyLocationLon = location.getLongitude();
             mMyLocationAccuracy = location.getAccuracy();
 
             mDistance = GeoUtils.distanceKm(mMyLocationLat, mMyLocationLon, mTargetLat, mTargetLon);
             mBearing = GeoUtils.bearing(mMyLocationLat, mMyLocationLon, mTargetLat, mTargetLon);
 
             updateDistance(mDistance);
             double bearingToTarget = mBearing - mOrientation;
             updateBearing(bearingToTarget);
         }
     }
 
     public void onProviderDisabled(String provider) {
     }
 
     public void onProviderEnabled(String provider) {
     }
 
     /**
      * Called when a location provider has changed its availability.
      * 
      * @see android.location.LocationListener#onStatusChanged(java.lang.String,
      *      int, android.os.Bundle)
      */
     public void onStatusChanged(String provider, int status, Bundle extras) {
         Log.v("GeoBeagle", "onStatusChanged " + provider + ", " + status);
         if (LocationManager.GPS_PROVIDER.equals(provider)) {
             switch (status) {
                 case LocationProvider.AVAILABLE:
                     mGpsAvailable = true;
                     startSweep();
                     break;
                 case LocationProvider.OUT_OF_SERVICE:
                     stopSweep();
                 case LocationProvider.TEMPORARILY_UNAVAILABLE:
                     mGpsAvailable = false;
 
                     if (mNetworkLocation != null && mNetworkAvailable) {
                         // Fallback to network location
                         mLastGpsFixTime = 0L;
                         onLocationChanged(mNetworkLocation);
                     } else {
                         handleUnknownLocation();
                     }
 
                     break;
             }
 
         } else if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
             switch (status) {
                 case LocationProvider.AVAILABLE:
                     mNetworkAvailable = true;
                     startSweep();
                     break;
                 case LocationProvider.OUT_OF_SERVICE:
                     stopSweep();
                 case LocationProvider.TEMPORARILY_UNAVAILABLE:
                     mNetworkAvailable = false;
 
                     if (!mGpsAvailable) {
                         handleUnknownLocation();
                     }
                     break;
             }
         }
     }
 
     /**
      * Called when we no longer have a valid location.
      */
     public void handleUnknownLocation() {
         Log.v("GeoBeagle", "!!!!!!unknown location");
         mHaveLocation = false;
         mDistanceView.setText("");
         mAccuracyView.setText("");
         mBearingView.setText("");
     }
 
     /**
      * Update state to reflect whether we are using metric or standard units.
      * 
      * @param useMetric True if the display should use metric units
      */
     public void setUseMetric(boolean useMetric) {
         mUseMetric = useMetric;
         mLastScale = -1;
         if (mHaveLocation) {
             updateDistance(mDistance);
         }
         invalidate();
     }
 
     private void updateBearing(double bearing) {
         if (mHaveLocation)
            mBearingView.setText(((int)bearing / 5) * 5 + "");
     }
 
     /**
      * Update our state to reflect a new distance to the target. This may
      * require choosing a new scale for the radar rings.
      * 
      * @param distanceKm The new distance to the target
      * @param bearing
      */
     private void updateDistance(double distanceKm) {
         final double[] scaleChoices;
         final float[] displayUnitsPerKm;
         final String[] displayFormats;
         final String[] scaleFormats;
         String distanceStr = null;
         String accuracyStr = null;
 
         if (mUseMetric) {
             scaleChoices = mMetricScaleChoices;
             displayUnitsPerKm = mMetricDisplayUnitsPerKm;
             displayFormats = mMetricDisplayFormats;
             scaleFormats = mMetricScaleFormats;
         } else {
             scaleChoices = mEnglishScaleChoices;
             displayUnitsPerKm = mEnglishDisplayUnitsPerKm;
             displayFormats = mEnglishDisplayFormats;
             scaleFormats = mEnglishScaleFormats;
         }
 
         final int count = scaleChoices.length;
         for (int i = 0; i < count; i++) {
             if (distanceKm < scaleChoices[i] || i == (count - 1)) {
                 String format = displayFormats[i];
                 double distanceDisplay = distanceKm * displayUnitsPerKm[i];
                 if (mLastScale != i) {
                     mLastScale = i;
                     String scaleFormat = scaleFormats[i];
                     float scaleDistance = (float)(scaleChoices[i] * displayUnitsPerKm[i]);
                     mDistanceScale[0] = String.format(scaleFormat, (scaleDistance / 4));
                     mDistanceScale[1] = String.format(scaleFormat, (scaleDistance / 2));
                     mDistanceScale[2] = String.format(scaleFormat, (scaleDistance * 3 / 4));
                     mDistanceScale[3] = String.format(scaleFormat, scaleDistance);
                 }
 
                 mDistanceRatio = (float)(mDistance / scaleChoices[mLastScale]);
                 distanceStr = String.format(format, distanceDisplay);
                 break;
             }
         }
 
         if (mMyLocationAccuracy != 0.0)
             for (int i = 0; i < count; i++) {
                 String format = displayFormats[i];
                 if (mMyLocationAccuracy / 1000 < scaleChoices[i] || i == (count - 1)) {
                     accuracyStr = String.format(format, mMyLocationAccuracy / 1000
                             * displayUnitsPerKm[i]);
                     break;
                 }
             }
 
         mDistanceView.setText(distanceStr);
         mAccuracyView.setText(accuracyStr);
     }
 
     /**
      * Turn on the sweep animation starting with the next draw
      */
     public void startSweep() {
         mSweepTime = SystemClock.uptimeMillis();
         mSweepBefore = true;
     }
 
     /**
      * Turn off the sweep animation
      */
     public void stopSweep() {
         mSweepTime = 0L;
     }
 }
