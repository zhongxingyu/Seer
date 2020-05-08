 package nl.sense.demo;
 
 import java.util.List;
 
 import org.osmdroid.ResourceProxy;
 import org.osmdroid.tileprovider.MapTileProviderBasic;
 import org.osmdroid.tileprovider.tilesource.ITileSource;
 import org.osmdroid.tileprovider.tilesource.XYTileSource;
 import org.osmdroid.tileprovider.util.CloudmadeUtil;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MyLocationOverlay;
 import org.osmdroid.views.overlay.Overlay;
 import org.osmdroid.views.overlay.TilesOverlay;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 /**
  *
  * @author Alex van der Linden
  *
  */
 public class MapActivity extends Activity {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	private MyLocationOverlay myLocationoverlay;
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Setup base map
 		final RelativeLayout rl = new RelativeLayout(this);
 
 		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());
 
 		final MapView osmv = new MapView(this, 256);
 		rl.addView(osmv, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.FILL_PARENT));
 		osmv.setBuiltInZoomControls(true);
 		osmv.setMultiTouchControls(true);
 		
 	    myLocationoverlay = new MyLocationOverlay(this, osmv);
 	    myLocationoverlay.enableMyLocation(); // not on by default
 	    myLocationoverlay.disableCompass();
 	    myLocationoverlay.enableFollowLocation();
 	    myLocationoverlay.setDrawAccuracyEnabled(true);
 	    myLocationoverlay.runOnFirstFix(new Runnable() {
 	    public void run() {
 			if (myLocationoverlay != null){	
 				// zoom to Rotterdam
 				osmv.getController().setCenter(new GeoPoint(51921700, 4481100));	
 			}
 			else {
 			}
 	            osmv.getController().animateTo(myLocationoverlay
 	                    .getMyLocation());
 	        }
 	    });
 		
 		 
 		// zoom to 16
 		osmv.getController().setZoom(16);
 
 		//osmv.getController().setCenter(new GeoPoint(51921700, 4481100));
 
 		// Add tiles layer with custom tile source
 		final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
 		final ITileSource tileSource = new XYTileSource("NoiseTiles2012", null, 1, 16, 256, ".png",
 				"http://a.tiles.mapbox.com/v3/merglind.NoiseTiles2012/");		
 		tileProvider.setTileSource(tileSource);
 		final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
 		tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
 		osmv.getOverlays().add(tilesOverlay);
 	    osmv.getOverlays().add(myLocationoverlay);	
 
 
 
 
 		
 		this.setContentView(rl);
 	}
  	
 
 }
 
     
 
        
