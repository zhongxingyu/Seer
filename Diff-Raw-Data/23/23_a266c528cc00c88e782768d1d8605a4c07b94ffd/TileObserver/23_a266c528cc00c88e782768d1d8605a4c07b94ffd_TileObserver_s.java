 package org.opentravelmate.widget.map;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import android.view.View;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.model.CameraPosition;
 
 /**
  * Observe the tile life-cycles (displayed, released).
  *
  * @author marc.plouhinec@gmail.com (Marc Plouhinec)
  */
 public class TileObserver {
 	
 	private final GoogleMap googleMap;
 	private final View mapView;
 	private TileCoordinates currentCenteredTileCoordinates;
 	private Map<String, TileCoordinates> currentlyDisplayedTileCoordinateById;
 	private List<TilesListener> tileDisplayedListeners = new LinkedList<TilesListener>();
 	private List<TilesListener> tileReleasedListeners = new LinkedList<TilesListener>();
 	
 	/**
 	 * Create a new TileObserver.
 	 * 
 	 * @param googleMap
 	 */
 	public TileObserver(GoogleMap googleMap, View mapView) {
 		this.googleMap = googleMap;
 		this.mapView = mapView;
 		
 		this.currentCenteredTileCoordinates = this.getCenteredTileCoordinates();
 		this.currentlyDisplayedTileCoordinateById = this.getDisplayedTileCoordinateById();
 		
 		googleMap.setOnCameraChangeListener(new CustomCameraChangeListener());
 	}
 	
 	/**
 	 * Register a listener for the TILES_DISPLAYED event.
 	 * 
 	 * @param listener
 	 */
 	public void onTilesDisplayed(TilesListener listener) {
 		this.tileDisplayedListeners.add(listener);
 	}
 	
 	/**
 	 * Register a listener for the TILES_RELEASED event.
 	 * 
 	 * @param listener
 	 */
 	public void onTilesReleased(TilesListener listener) {
 		this.tileReleasedListeners.add(listener);
 	}
 	
 	/**
 	 * Get the displayed tiles coordinates.
 	 * 
 	 * @return displayed tiles coordinates
 	 */
 	public List<TileCoordinates> getDisplayedTileCoordinates() {
 		return new ArrayList<TileCoordinates>(this.currentlyDisplayedTileCoordinateById.values());
 	}
 	
 	/**
 	 * Get the coordinates of the tile in the center.
 	 * 
 	 * @return coordinates of the tile in the center
 	 */
 	private TileCoordinates getCenteredTileCoordinates() {
 		CameraPosition cameraPosition = this.googleMap.getCameraPosition();
 		return new TileCoordinates(
 				(int)Math.floor(cameraPosition.zoom),
 				(int)Math.floor(ProjectionUtils.lngToTileX(cameraPosition.zoom, cameraPosition.target.longitude)),
 				(int)Math.floor(ProjectionUtils.latToTileY(cameraPosition.zoom, cameraPosition.target.latitude)));
 	}
 	
 	/**
 	 * Compute all the displayed tile coordinates.
 	 * 
 	 * @return displayed tile coordinates
 	 */
 	private Map<String, TileCoordinates> getDisplayedTileCoordinateById() {
 		CameraPosition cameraPosition = this.googleMap.getCameraPosition();
 		float zoom = cameraPosition.zoom;
		int zoomAsInt = (int)Math.floor(zoom);
 		Point xyCenter = new Point(
				ProjectionUtils.lngToTileX(zoom, cameraPosition.target.longitude),
				ProjectionUtils.latToTileY(zoom, cameraPosition.target.latitude));
 		int mapCanvasWidth = this.mapView.getWidth();
 		int mapCanvasHeight = this.mapView.getHeight();
 		
 		Point xyNorthEast = new Point(xyCenter.x - (mapCanvasWidth / 2) / 256, xyCenter.y - (mapCanvasHeight / 2) / 256);
 		Point xySouthWest = new Point(xyCenter.x + (mapCanvasWidth / 2) / 256, xyCenter.y + (mapCanvasHeight / 2) / 256);
		TileCoordinates tileNorthEast = new TileCoordinates(zoomAsInt, (int)Math.floor(xyNorthEast.x), (int)Math.floor(xyNorthEast.y));
		TileCoordinates tileSouthWest = new TileCoordinates(zoomAsInt, (int)Math.floor(xySouthWest.x), (int)Math.floor(xySouthWest.y));
 		
 		// Take all the tiles from the north-east to the south-east and include the adjacents ones outside of the view-port.
 		Map<String, TileCoordinates> displayedTileCoordinateById = new HashMap<String, TileCoordinates>();
 		for (int y = tileNorthEast.y - 1; y <= tileSouthWest.y + 1; y++) {
             for (int x = tileNorthEast.x - 1; x <= tileSouthWest.x + 1; x++) {
                 displayedTileCoordinateById.put(zoom + "_" + x + "_" + y, new TileCoordinates(zoomAsInt, x, y));
             }
         }
 		return displayedTileCoordinateById;
 	}
 	
 	/**
 	 * Listen to the center and zoom changes.
 	 */
 	private class CustomCameraChangeListener implements OnCameraChangeListener {
 		@Override
 		public void onCameraChange(CameraPosition cameraPosition) {
 			// Check if the centered tile has changed
 			TileCoordinates centeredTileCoordinates = getCenteredTileCoordinates();
 			boolean hasChanged = 
 					currentCenteredTileCoordinates.zoom != centeredTileCoordinates.zoom ||
 		            currentCenteredTileCoordinates.x != centeredTileCoordinates.x ||
 		            currentCenteredTileCoordinates.y != centeredTileCoordinates.y;
 			
 			if (hasChanged) {
 				currentCenteredTileCoordinates = centeredTileCoordinates;
 				
 				// Compare the currently displayed tiles with the previous ones
 				Map<String, TileCoordinates> displayedTileCoordinateById = getDisplayedTileCoordinateById();
 				List<TileCoordinates> newTileCoordinates = new LinkedList<TileCoordinates>();
 				for (Map.Entry<String, TileCoordinates> entry : displayedTileCoordinateById.entrySet()) {
 					if (!currentlyDisplayedTileCoordinateById.containsKey(entry.getKey())) {
 						newTileCoordinates.add(entry.getValue());
 					}
 				}
 				List<TileCoordinates> removedTileCoordinates = new LinkedList<TileCoordinates>();
 				for (Map.Entry<String, TileCoordinates> entry : currentlyDisplayedTileCoordinateById.entrySet()) {
 					if (!displayedTileCoordinateById.containsKey(entry.getKey())) {
 						removedTileCoordinates.add(entry.getValue());
 					}
 				}
 				currentlyDisplayedTileCoordinateById = displayedTileCoordinateById;
 				
 				// Call the listeners
 				if (!newTileCoordinates.isEmpty()) {
 					for (TilesListener listener : tileDisplayedListeners) {
 						listener.on(newTileCoordinates);
 					}
 				}
 				if (!removedTileCoordinates.isEmpty()) {
 					for (TilesListener listener : tileReleasedListeners) {
 						listener.on(removedTileCoordinates);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Tile event listener.
 	 */
 	public static interface TilesListener {
 		/**
 		 * Function called when tiles are displayed or released.
 		 * 
 		 * @param tileCoordinates
 		 */
 		public void on(List<TileCoordinates> tileCoordinates);
 	}
 }
