 package org.fourdnest.androidclient.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.tools.MapTools;
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.ResourceProxy;
 import org.osmdroid.ResourceProxy.bitmap;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.ItemizedOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 import org.osmdroid.views.overlay.PathOverlay;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MapViewActivity extends Activity {
 
 	public static final String EGG_ID = "EggID";
 	private static final int DEFAULT_ZOOM = 15;
 	/** Called when the activity is first created. */
 	private MapController mapController;
 	private MapView mapView;
 	private FourDNestApplication application;
 	private int eggID;
 	private ItemizedOverlay<OverlayItem> overlay;
 	private PathOverlay pathOverlay;
 	private ResourceProxy resourceProxy;
 	private static final String TAG = "Mapview";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		this.application = (FourDNestApplication) getApplication();
 
 		Bundle startingExtras = getIntent().getExtras();
 		this.eggID = (Integer) startingExtras.get(MapViewActivity.EGG_ID);
 
 		setContentView(R.layout.egg_view);
 
 		Egg egg;
 		if (startingExtras.getBoolean(NewEggActivity.NEW_EGG)) {
 			egg = this.application.getDraftEggManager().getEgg(eggID);
 		}else {
 			egg = this.application.getStreamEggManager().getEgg(eggID);
 		}
 
 		super.onCreate(savedInstanceState);
 
 		resourceProxy = new DefaultResourceProxyImpl(application);
 
 		setContentView(R.layout.map_view);
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setTileSource(TileSourceFactory.MAPNIK);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setMultiTouchControls(true);
 		mapController = mapView.getController();
 		mapController.setZoom(DEFAULT_ZOOM);
 
 		GeoPoint firstPoint;
 		GeoPoint lastPoint = null;
 		if (egg.getMimeType() == Egg.fileType.ROUTE) {
 			
 			this.pathOverlay = new PathOverlay(Color.RED, resourceProxy);
 
 			List<String> list = new ArrayList<String>();
 			try {
 				list = MapTools.getLocationListFromEgg(egg);
 			} catch (NumberFormatException e) {
 				Log.d(TAG, "Failed to produce location list from location file");
 				e.printStackTrace();
 			} catch (IOException e) {
 				Log.d(TAG, "Failed to produce location list from location file");
 			}
 
 			float lat, lon;
 			String[] temp;
 			for (String locString : list) {
 				temp = locString.split(",");
 				lat = Float.valueOf(temp[1]);
 				lon = Float.valueOf(temp[0]);
 				GeoPoint point = new GeoPoint(lat, lon);
 				pathOverlay.addPoint(point);
 
 			}
 			this.mapView.getOverlays().add(pathOverlay);
 			temp = list.get(0).split(",");
 			lat = Float.valueOf(temp[1]);
 			lon = Float.valueOf(temp[0]);
 			firstPoint = new GeoPoint(lat, lon);
 			mapController.setCenter(firstPoint);
 			
 			if (list.size() > 1) {
 				temp = list.get(list.size()-1).split(",");
 				lat = Float.valueOf(temp[1]);
 				lon = Float.valueOf(temp[0]);
 				lastPoint = new GeoPoint(lat, lon);
 			}
 
 		} else {
 
 			firstPoint = new GeoPoint(egg.getLatitude(), egg.getLongitude());
 			
 		}
 		mapController.setCenter(firstPoint);
 		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
 		items.add(new OverlayItem("", "", firstPoint));
 		if (lastPoint != null) {
 			OverlayItem item = new OverlayItem("", "", lastPoint);
 			Drawable marker = resourceProxy.getDrawable(bitmap.person);
 			item.setMarker(marker);
 			items.add(item);
 		}
 
 		this.overlay = new ItemizedIconOverlay<OverlayItem>(items,
 				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
 
 					public boolean onItemLongPress(int arg0, OverlayItem arg1) {
 						// TODO Auto-generated method stub
 						return true;
 					}
 
 					public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
 						// TODO Auto-generated method stub
 						return true;
 					}
 				}, resourceProxy);
 
 		this.mapView.getOverlays().add(this.overlay);
 		mapView.invalidate();
 
 	}
 
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
