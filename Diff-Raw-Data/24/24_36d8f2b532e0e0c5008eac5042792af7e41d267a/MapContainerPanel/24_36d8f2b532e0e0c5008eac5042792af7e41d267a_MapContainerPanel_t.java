 package cpsc310.client;
 
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 
 
 /**
  * Split panel that contains Google map and street view
  */
 public class MapContainerPanel extends SplitLayoutPanel {
	private static int defaultMapSize = 500;
 	private PropertyMap map = null;
 	
 	private Timer streetViewResizeTimer = new Timer() {
         @Override
         public void run() {
         	map.getStreetViewMap().checkResize();		
         }
     };
     private Timer mapResizeTimer = new Timer() {
     	@Override
     	public void run() {
     		map.getMap().checkResize();		
     	}
     };
 
 	/**
 	 * Constructor
 	 * @param map - Property Map instance to ensure same map instance
 	 * is used as the caller class.
 	 */
 	public MapContainerPanel(PropertyMap map) {
 		this.map = map;
 		
 		// Add Google map and street view
 		this.addWest(map.getMap(), defaultMapSize);
 		this.add(map.getStreetViewMap());
 		
 		// Set style name
 		this.setStyleName("mapContainerPanel");
 	}
 
 	/**
 	 * Override SplitLayoutPanel's default method to
 	 * ensure that map and the street view gets properly resized
 	 * whenever resizing event is fired.
 	 */
 	@Override
 	public void onResize() {
 		streetViewResizeTimer.schedule(400);
 		mapResizeTimer.schedule(400);		
 	}	
 }
