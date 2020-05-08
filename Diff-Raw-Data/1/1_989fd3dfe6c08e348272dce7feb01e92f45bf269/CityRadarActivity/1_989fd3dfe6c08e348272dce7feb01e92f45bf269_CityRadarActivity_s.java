 package org.anddev.andengine.examples.app.cityradar;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.anddev.andengine.entity.layer.ILayer;
 import org.anddev.andengine.entity.primitive.Line;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.shape.modifier.LoopShapeModifier;
 import org.anddev.andengine.entity.shape.modifier.RotationModifier;
 import org.anddev.andengine.entity.shape.modifier.ease.EaseLinear;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.examples.adt.City;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.BuildableTexture;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.builder.BlackPawnTextureBuilder;
 import org.anddev.andengine.opengl.texture.builder.ITextureBuilder.TextureSourcePackingException;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.sensor.location.ILocationListener;
 import org.anddev.andengine.sensor.location.LocationProviderStatus;
 import org.anddev.andengine.sensor.location.LocationSensorOptions;
 import org.anddev.andengine.sensor.orientation.IOrientationListener;
 import org.anddev.andengine.sensor.orientation.OrientationData;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 import org.anddev.andengine.util.MathUtils;
 
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class CityRadarActivity extends BaseGameActivity implements
     IOrientationListener, ILocationListener {
   private static final boolean USE_MOCK_LOCATION = false;
   private static final boolean USE_ACTUAL_LOCATION = !USE_MOCK_LOCATION;
 
   private static final int CAMERA_WIDTH = 480;
   private static final int CAMERA_HEIGHT = 800;
 
   private static final int GRID_SIZE = 80;
 
   private static final int LAYER_COUNT = 1;
   private static final int LAYER_CITIES = 0;
 
   private Camera mCamera;
 
   private BuildableTexture mBuildableTexture;
 
   private TextureRegion mRadarPointTextureRegion;
   private TextureRegion mRadarTextureRegion;
 
   private Texture mFontTexture;
   private Font mFont;
 
   private Location mUserLocation;
 
   private final ArrayList<City> mCities = new ArrayList<City>();
   private final HashMap<City, Sprite> mCityToCitySpriteMap = new HashMap<City,
       Sprite>();
   private final HashMap<City, Text> mCityToCityNameTextMap = new HashMap<City,
       Text>();
 
   public CityRadarActivity() {
     mCities.add(new City("London", 51.509, -0.118));
     mCities.add(new City("New York", 40.713, -74.006));
     mCities.add(new City("Beijing", 39.929, 116.388));
     mCities.add(new City("Sydney", -33.850, 151.200));
     mCities.add(new City("Berlin", 51.518, 13.408));
     mCities.add(new City("Rio", -22.908, -43.196));
     mCities.add(new City("New Delhi", 28.636, 77.224));
     mCities.add(new City("Cape Town", -33.926, 18.424));
 
     mUserLocation = new Location(LocationManager.GPS_PROVIDER);
 
     if (USE_MOCK_LOCATION) {
       mUserLocation.setLatitude(51.518);
       mUserLocation.setLongitude(13.408);
     }
   }
 
   @Override
   public Engine onLoadEngine() {
     mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT,
         new FillResolutionPolicy(), mCamera));
   }
 
   @Override
   public void onLoadResources() {
     // init font
     mFontTexture = new Texture(256, 256,
         TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     mFont = new Font(mFontTexture, Typeface.DEFAULT, 12, true, Color.WHITE);
 
     mEngine.getFontManager().loadFont(mFont);
     mEngine.getTextureManager().loadTexture(mFontTexture);
 
     // init texture regions
     mBuildableTexture = new BuildableTexture(512, 256,
         TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
     mRadarTextureRegion = TextureRegionFactory.createFromAsset(
         mBuildableTexture, this, "gfx/radar.png");
     mRadarPointTextureRegion = TextureRegionFactory.createFromAsset(
         mBuildableTexture, this, "gfx/radarpoint.png");
 
     try {
       mBuildableTexture.build(new BlackPawnTextureBuilder(1));
     }
     catch (final TextureSourcePackingException e) {
       Debug.e(e);
     }
 
     mEngine.getTextureManager().loadTexture(mBuildableTexture);
   }
 
   @Override
   public Scene onLoadScene() {
     final Scene scene = new Scene(LAYER_COUNT);
 
     final HUD hud = new HUD();
     mCamera.setHUD(hud);
 
     // background
     initBackground(hud.getBottomLayer());
 
     // cities
     initCitySprites(scene.getLayer(LAYER_CITIES));
 
     return scene;
   }
 
   private void initCitySprites(final ILayer pLayer) {
     final int cityCount = mCities.size();
 
     for (int i = 0; i < cityCount; i++) {
       final City city = mCities.get(i);
 
       final Sprite citySprite = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2,
           mRadarPointTextureRegion);
       citySprite.setColor(0, 0.5f, 0, 1);
 
       final Text cityNameText = new Text(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2,
           mFont, city.getName()) {
         @Override
         protected void onManagedDraw(final GL10 pGL, final Camera pCamera) {
           // this ensures that the name of the city is always 'pointing down'
           setRotation(-mCamera.getRotation());
           super.onManagedDraw(pGL, pCamera);
         }
       };
       cityNameText.setRotationCenterY(-citySprite.getHeight() / 2);
 
       mCityToCityNameTextMap.put(city, cityNameText);
       mCityToCitySpriteMap.put(city, citySprite);
 
       pLayer.addEntity(citySprite);
       pLayer.addEntity(cityNameText);
     }
   }
 
   private void initBackground(final ILayer pLayer) {
     // vertical grid lines
     for (int i = GRID_SIZE / 2; i < CAMERA_WIDTH; i += GRID_SIZE) {
       final Line line = new Line(i, 0, i, CAMERA_HEIGHT);
       line.setColor(0, 0.5f, 0, 1);
       pLayer.addEntity(line);
     }
 
     // horizontal grid lines
     for (int i = GRID_SIZE / 2; i < CAMERA_HEIGHT; i += GRID_SIZE) {
       final Line line = new Line(0, i, CAMERA_WIDTH, i);
       line.setColor(0, 0.5f, 0, 1);
       pLayer.addEntity(line);
     }
 
     final Sprite radarSprite = new Sprite(
         CAMERA_WIDTH / 2 - mRadarTextureRegion.getWidth(),
         CAMERA_HEIGHT / 2 - mRadarTextureRegion.getHeight(),
         mRadarTextureRegion);
     radarSprite.setColor(0, 1, 0, 1);
     radarSprite.setRotationCenter(radarSprite.getWidth(),
         radarSprite.getHeight());
     radarSprite.addShapeModifier(new LoopShapeModifier(new RotationModifier(3,
         0, 360, EaseLinear.getInstance())));
 
     // title
     final Text titleText = new Text(0, 0, mFont, "-- CityRadar --");
     titleText.setPosition(CAMERA_WIDTH / 2 - titleText.getWidth() / 2,
         titleText.getHeight() + 35);
     titleText.setScale(2);
     titleText.setScaleCenterY(0);
     pLayer.addEntity(titleText);
   }
 
   @Override
   public void onLoadComplete() {
     refreshCitySprites();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
 
     enableOrientationSensor(this);
 
     final LocationSensorOptions locationSensorOptions =
         new LocationSensorOptions();
     locationSensorOptions.setAccuracy(Criteria.ACCURACY_COARSE);
     locationSensorOptions.setMinimumTriggerTime(0);
     locationSensorOptions.setMinimumTriggerDistance(0);
     enableLocationSensor(this, locationSensorOptions);
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     mEngine.disableOrientationSensor(this);
     mEngine.disableLocationSensor(this);
   }
 
   @Override
   public void onOrientationChanged(final OrientationData pOrientationData) {
     mCamera.setRotation(-pOrientationData.getYaw());
   }
 
   @Override
   public void onLocationChanged(final Location pLocation) {
     if (USE_ACTUAL_LOCATION) {
       mUserLocation = pLocation;
     }
     refreshCitySprites();
   }
 
   @Override
   public void onLocationLost() {
   }
 
   @Override
   public void onLocationProviderDisabled() {
   }
 
   @Override
   public void onLocationProviderEnabled() {
   }
 
   @Override
   public void onLocationProviderStatusChanged(
       final LocationProviderStatus pLocationProviderStatus,
       final Bundle pBundle) {
   }
 
   private void refreshCitySprites() {
     final double userLatitudeRad = MathUtils.degToRad(
         (float)mUserLocation.getLatitude());
     final double userLongitudeRad = MathUtils.degToRad(
         (float)mUserLocation.getLongitude());
 
     final int cityCount = mCities.size();
 
     double maxDistance = Double.MIN_VALUE;
 
     // calculate the distances and bearings of the cities to the location of
     // the user
     for (int i = 0; i < cityCount; i++) {
       final City city = mCities.get(i);
 
       final double cityLatitudeRad = MathUtils.degToRad(
           (float)city.getLatitude());
       final double cityLongitudeRad = MathUtils.degToRad(
           (float)city.getLongitude());
 
       city.setDistanceToUser(GeoMath.calculateDistance(userLatitudeRad,
           userLongitudeRad, cityLatitudeRad, cityLongitudeRad));
       city.setBearingToUser(GeoMath.calculateBearing(userLatitudeRad,
           userLongitudeRad, cityLatitudeRad, cityLongitudeRad));
 
       maxDistance = Math.max(maxDistance, city.getDistanceToUser());
     }
 
     // calculate a scaleRatio so that all cities are visible at all times
     final double scaleRatio = (CAMERA_WIDTH / 2) / maxDistance * 0.93f;
 
     for (int i = 0; i < cityCount; i++) {
       final City city = mCities.get(i);
 
       final Sprite citySprite = mCityToCitySpriteMap.get(city);
       final Text cityNameText = mCityToCityNameTextMap.get(city);
 
       final float bearingInRad = MathUtils.degToRad(90 -
           (float)city.getBearingToUser());
 
       final float x = (float)(CAMERA_WIDTH / 2 + city.getDistanceToUser() *
           scaleRatio * Math.cos(bearingInRad));
       final float y = (float)(CAMERA_HEIGHT / 2 + city.getDistanceToUser() *
           scaleRatio * Math.sin(bearingInRad));
 
       // not sure why citySprite is null sometime, put this condition to prevent
       // the error
       if (citySprite != null) {
         citySprite.setPosition(x - citySprite.getWidth() / 2,
             y - citySprite.getHeight() / 2);
 
         final float textX = x - cityNameText.getWidth() / 2;
         final float textY = y + citySprite.getHeight() / 2;
 
         cityNameText.setPosition(textX, textY);
       }
     }
   }
 
   private static class GeoMath {
     private static final double RADIUS_EARTH_METERS = 6371000;
     /**
      *
      * @return the distance in meters.
      */
     public static double calculateDistance(final double pLatitude1,
         final double pLongitude1, final double pLatitude2,
         final double pLongitude2) {
       return Math.acos(Math.sin(pLatitude1) * Math.sin(pLatitude2) +
           Math.cos(pLatitude1) * Math.cos(pLatitude2) *
           Math.cos(pLongitude2 - pLongitude1)) * RADIUS_EARTH_METERS;
     }
 
     /**
      *
      * @return the bearing in degrees.
      */
     public static double calculateBearing(final double pLatitude1,
         final double pLongitude1, final double pLatitude2,
         final double pLongitude2) {
       final double y = Math.sin(pLongitude2 - pLongitude1) *
           Math.cos(pLatitude2);
       final double x = Math.cos(pLatitude1) * Math.sin(pLatitude2) -
           Math.sin(pLatitude1) * Math.cos(pLatitude2) *
           Math.cos(pLongitude2 - pLongitude1);
       final float bearing = MathUtils.radToDeg((float)Math.atan2(y, x));
       return (bearing + 360) % 360;
     }
   }
 }
