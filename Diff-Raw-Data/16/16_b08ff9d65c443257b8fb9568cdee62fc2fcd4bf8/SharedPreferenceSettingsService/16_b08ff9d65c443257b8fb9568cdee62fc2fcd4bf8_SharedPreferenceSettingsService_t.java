 package fi.testbed2.service.impl;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Rect;
 import android.location.LocationManager;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import fi.testbed2.R;
 import fi.testbed2.android.app.Logger;
 import fi.testbed2.android.app.MainApplication;
 import fi.testbed2.android.ui.view.MapScaleInfo;
 import fi.testbed2.domain.MapLocationGPS;
 import fi.testbed2.domain.Municipality;
 import fi.testbed2.service.LocationService;
 import fi.testbed2.service.MunicipalityService;
 import fi.testbed2.service.SettingsService;
 import net.margaritov.preference.colorpicker.ColorPickerPreference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Singleton
 public class SharedPreferenceSettingsService implements SettingsService {
 
    public static final String SHARED_PREFERENCE_FILE_NAME = "fi.testbed2_preferences";

     @Inject
     MunicipalityService municipalityService;
 
     @Inject
     LocationService locationService;
 
     @Inject
     SharedPreferences sharedPreferences;
 
     @Inject
     Context context;
 
     public SharedPreferenceSettingsService() {
         Logger.debug("SharedPreferenceSettingsService instantiated");
     }
 
     /**
      * Saves the bounds of the map user has previously viewed to persistent storage.
      */
     @Override
     public void saveMapBoundsAndScaleFactor(Rect bounds, MapScaleInfo scaleInfo, int orientation) {
 
         SharedPreferences.Editor editor = sharedPreferences.edit();
 
         if (editor!=null) {
             if (bounds!=null) {
                 // bounds String format is left:top:right:bottom
                 editor.putString(getMapBoundsPreferenceKey(orientation),
                         "" + bounds.left + ":" + bounds.top + ":" + bounds.right + ":" + bounds.bottom);
             }
             editor.putFloat(getMapScalePreferenceKey(orientation), scaleInfo.getScaleFactor());
             editor.putFloat(getMapScalePivotXPreferenceKey(orientation), scaleInfo.getPivotX());
             editor.putFloat(getMapScalePivotYPreferenceKey(orientation), scaleInfo.getPivotY());
             editor.commit();
         }
 
     }
 
     @Override
     public Rect getSavedMapBounds(int orientation) {
 
         // left:top:right:bottom
         String frameBoundsPref = sharedPreferences.getString(getMapBoundsPreferenceKey(orientation), null);
 
         if (frameBoundsPref==null) {
             return null;
         }
 
         String[] parts = frameBoundsPref.split(":");
         final Rect bounds = new Rect(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 
         return bounds;
 
     }
 
 
     @Override
     public MapScaleInfo getSavedScaleInfo(int orientation) {
 
         float scaleFactor = sharedPreferences.getFloat(getMapScalePreferenceKey(orientation), 1.0f);
         float pivotX = sharedPreferences.getFloat(getMapScalePivotXPreferenceKey(orientation), 0.0f);
         float pivotY = sharedPreferences.getFloat(getMapScalePivotYPreferenceKey(orientation), 0.0f);
 
         return new MapScaleInfo(scaleFactor, pivotX, pivotY);
     }
 
     @Override
     public List<Municipality> getSavedMunicipalities() {
 
         List<Municipality> municipalities = new ArrayList<Municipality>();
 
         String municipalitiesString = sharedPreferences.getString(PREF_LOCATION_SHOW_MUNICIPALITIES, "");
 
         String[] municipalityArray = municipalitiesString.split(
                 SettingsService.PREF_LOCATION_SHOW_MUNICIPALITIES_SPLIT);
 
         if (municipalityArray.length<1 || municipalityArray[0].length()==0) {
             return municipalities;
         }
 
         for (String municipalityName : municipalityArray) {
             Municipality municipality = municipalityService.getMunicipality(municipalityName);
             if (municipality!=null) {
                 municipalities.add(municipality);
             }
         }
 
         return municipalities;
 
     }
 
     @Override
     public void saveWhatsNewDialogShownForCurrentVersion() {
         Logger.debug("Saving that what's new dialog is shown for version: " + MainApplication.getVersionName());
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(SettingsService.PREF_WHATS_NEW_DIALOG_SHOWN_FOR_VERSION,
                 MainApplication.getVersionName());
         editor.commit();
     }
 
     @Override
     public void saveHardwareAccelerationDialogShown() {
         Logger.debug("Saving hardware acceleration dialog is shown");
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putBoolean(SettingsService.PREF_HW_ACCEL_DIALOG_SHOWN, true);
         editor.commit();
     }
 
     @Override
     public int getSavedFrameDelay() {
         return Integer.parseInt(sharedPreferences.getString(SettingsService.PREF_ANIM_FRAME_DELAY, "1000"));
     }
 
     @Override
     public boolean isStartAnimationAutomatically() {
         return sharedPreferences.getBoolean(SettingsService.PREF_ANIM_AUTOSTART, true);
     }
 
     @Override
     public boolean isShowWhatsNewDialog() {
 
         String dialogShownForVersion = sharedPreferences.
                 getString(SettingsService.PREF_WHATS_NEW_DIALOG_SHOWN_FOR_VERSION, "");
 
         Logger.debug("What's new dialog is shown for version: " + dialogShownForVersion);
 
         if (!dialogShownForVersion.equals(MainApplication.getVersionName())) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public boolean isShowHardwareAccelerationDialog() {
         return !sharedPreferences.getBoolean(
                 SettingsService.PREF_HW_ACCEL_DIALOG_SHOWN, false);
     }
 
     public boolean showUserLocation() {
         return sharedPreferences.getBoolean(SettingsService.PREF_LOCATION_SHOW_USER_LOCATION, true);
     }
 
     public String getTestbedPageURL() {
 
         String mapType = sharedPreferences.getString(PREF_MAP_TYPE, "radar");
         String mapTimeStep = sharedPreferences.getString(PREF_MAP_TIME_STEP, "5");
         String mapNumberOfImages = sharedPreferences.getString(PREF_MAP_NUMBER_OF_IMAGES, "10");
         return context.getString(R.string.testbed_base_url, mapType, mapTimeStep, mapNumberOfImages);
 
     }
 
     @Override
     public String getMapMarkerColor() {
         int color = sharedPreferences.getInt(PREF_LOCATION_MAP_MARKER_COLOR,
                 ColorPickerPreference.convertToColorInt(context.getString(R.string.preference_map_marker_color_default)));
         return ColorPickerPreference.convertToARGB(color);
     }
 
     @Override
     public String getMapPointColor() {
         int color = sharedPreferences.getInt(PREF_LOCATION_MAP_POINT_COLOR,
                 ColorPickerPreference.convertToColorInt(context.getString(R.string.preference_map_point_color_default)));
         return ColorPickerPreference.convertToARGB(color);
     }
 
     private static String getMapBoundsPreferenceKey(int orientation) {
         return PREF_BOUNDS_PREFERENCE_KEY_PREFIX + orientation;
     }
 
     private static String getMapScalePreferenceKey(int orientation) {
         return PREF_SCALE_PREFERENCE_KEY_PREFIX + orientation;
     }
 
     private static String getMapScalePivotXPreferenceKey(int orientation) {
         return PREF_SCALE_PIVOT_X_PREFERENCE_KEY_PREFIX + orientation;
     }
 
     private static String getMapScalePivotYPreferenceKey(int orientation) {
         return PREF_SCALE_PIVOT_Y_PREFERENCE_KEY_PREFIX + orientation;
     }
 
     @Override
     public int getMapMarkerSize() {
         return Integer.valueOf(sharedPreferences.getString(PREF_LOCATION_MAP_MARKER_SIZE, "40"));
     }
 
     @Override
     public int getMapPointSize() {
         return Integer.valueOf(sharedPreferences.getString(PREF_LOCATION_MAP_POINT_SIZE, "10"));
     }
 
     @Override
     public String getLocationProvider() {
         return sharedPreferences.getString(PREF_LOCATION_PROVIDER, LocationManager.NETWORK_PROVIDER);
     }
 
     public void setLocationProvider(String provider) {
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(SettingsService.PREF_LOCATION_PROVIDER, provider);
         editor.commit();
     }
 
     public MapLocationGPS getSavedFixedLocation() {
 
         double lat = Double.parseDouble(sharedPreferences.getString(PREF_LOCATION_FIXED_LAT, "-1"));
         double lon = Double.parseDouble(sharedPreferences.getString(PREF_LOCATION_FIXED_LON, "-1"));
 
         if (lat<0 || lon<0) {
             return null;
         }
 
         return new MapLocationGPS(lat, lon);
 
     }
 
     /**
      * Saves the last known location as fixed location to the preferences.
      * If the location is not available, does not save it to the preferences.
      *
      * @return Returns the last known location or null if no location was available
      */
     public MapLocationGPS saveCurrentLocationAsFixedLocation() {
 
         MapLocationGPS lastKnownLocation = locationService.getUserLastLocation();
 
         if (lastKnownLocation!=null) {
             Logger.debug("Saving last known location to preferences: " + lastKnownLocation);
             SharedPreferences.Editor editor = sharedPreferences.edit();
             editor.putString(SettingsService.PREF_LOCATION_FIXED_LAT,""+lastKnownLocation.getLatitude());
             editor.putString(SettingsService.PREF_LOCATION_FIXED_LON,""+lastKnownLocation.getLongitude());
             editor.commit();
         }
 
         return lastKnownLocation;
 
     }
 
     public boolean showAds() {
         return sharedPreferences.getBoolean(SettingsService.PREF_SHOW_ADS, true);
     }
 
 }
