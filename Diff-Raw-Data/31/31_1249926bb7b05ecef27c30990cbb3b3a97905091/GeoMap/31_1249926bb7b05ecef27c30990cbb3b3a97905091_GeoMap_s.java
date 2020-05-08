 package at.fakeroot.sepm.client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.client.serialize.ClientGeoObject;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.maps.client.MapType;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.Control;
 import com.google.gwt.maps.client.control.ControlAnchor;
 import com.google.gwt.maps.client.control.ControlPosition;
 import com.google.gwt.maps.client.control.HierarchicalMapTypeControl;
 import com.google.gwt.maps.client.control.LargeMapControl3D;
 import com.google.gwt.maps.client.event.MapMoveEndHandler;
 import com.google.gwt.maps.client.geocode.GeoAddressAccuracy;
 import com.google.gwt.maps.client.geocode.Geocoder;
 import com.google.gwt.maps.client.geocode.LocationCallback;
 import com.google.gwt.maps.client.geocode.Placemark;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class GeoMap extends Composite implements MapMoveEndHandler
 {
 	private class LocationSelector extends DialogBox implements ClickHandler {
 		private JsArray<Placemark> locations;
 		
 		public LocationSelector(JsArray<Placemark> locations) {
 			super(false, true);
 			VerticalPanel panel = new VerticalPanel();
 			this.locations = locations;
 			for (int i = 0; i < locations.length(); i++) {
 				Placemark p = locations.get(i);
 				Button btn = new Button(p.getAddress());
 				btn.setTabIndex(i);
 				btn.addClickHandler(this);
 				btn.setWidth("100%");
 				btn.setStyleName("locationBtn");
 				panel.add(btn);
 			}
 			Button cancelBtn = new Button("Cancel");
 			cancelBtn.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					hide();
 				}
 			});
 			panel.add(cancelBtn);
 
 			setWidget(panel);
 			setText("Please choose");
 		}
 
 		@Override
 		public void onClick(ClickEvent event) {
 			Button src = (Button) event.getSource();
 			setMapPosition(locations.get(src.getTabIndex()));
 			hide();
 		}
 	}
 	
 	private IGeoManager geoManager;
 	private MapWidget geoMap;
 	private Geocoder geoCoder;
 	private ArrayList<GeoPin> geoPins;
 	private boolean blockEventHandler;
 
 	
 	/**
 	 * Constructor.
 	 * @param geoManger An object which implements the IGeoManager interface.
 	 */
 	public GeoMap(IGeoManager geoManager)
 	{
 		//Set the geoManager.
 		this.geoManager = geoManager;
 		
 		//Create a panel which holds the geoMap.
 		FlowPanel fPanel = new FlowPanel();
 		fPanel.setSize("100%", "100%");
 		
 		//Set the position of the geoMap so that it displays Austria.
 		geoMap = new MapWidget(LatLng.newInstance(47.569114, 13.337402), 7);
 		geoMap.setSize("100%", "100%");
 		
 		//Add the navigation control to the map.
 		geoMap.addControl(new LargeMapControl3D(), new ControlPosition(ControlAnchor.TOP_RIGHT, 5, 35));
 		
 		//Add the supported map types to the map.
 		geoMap.addMapType(MapType.getNormalMap());
 		geoMap.addMapType(MapType.getSatelliteMap());
 		geoMap.addMapType(MapType.getPhysicalMap());
 		geoMap.addControl(new HierarchicalMapTypeControl());
 		
 		//Set map behaviour.
 		geoMap.setDoubleClickZoom(true);
 		geoMap.setScrollWheelZoomEnabled(true);
 		
 		//Add event handlers.
 		geoMap.addMapMoveEndHandler(this);
 		
 		//Add the map to the panel.
 		fPanel.add(geoMap);
 		
 		//Put the panel into a widget (required by GWT).
 		initWidget(fPanel);
 		
 		//Create the geoCoder which is required to search for locations.
 		geoCoder = new Geocoder();
 		
 		//Create the geoPin array.
 		geoPins = new ArrayList<GeoPin>();
 		
 		//Set the block variable to false.
 		blockEventHandler = false;
 	}
 	
 	/**
 	 * Center the map at the given coordinates.
 	 * @param x
 	 * @param y
 	 */
 	public void setCenter(double x, double y)
 	{
 		geoMap.setCenter(LatLng.newInstance(y, x));
 	}
 	
 	/**
 	 * Returns the Center of the Map
 	 * @return Center
 	 */
 	public LatLng getCenter(){
 		return geoMap.getCenter();
 	}
 	
 	/**
 	 * Searches for a given location and moves the displayed map region to the search result (if there
 	 * is one).
 	 * @param where specifies the location where to search.
 	 */
 	public void search(String where)
 	{
 		geoCoder.getLocations(where, new LocationCallback()
 		{
 			public void onSuccess(JsArray<Placemark> locations)
 			{
 				if (locations.length() > 1) {
 					showLocationSelector(locations);
 				}
 				else if (locations.length() == 1) setMapPosition(locations.get(0));
 				else {
 					geoManager.showRegionError();
 				}
 			}
 			
 			public void onFailure(int statusCode)
 			{
				//Perform the search query now *after* the geo-coding has finished.
				geoManager.showErrorMessage("Region request error", "Couldn't request the search region! Code "+statusCode);
 			}
 		});
 	}
 	
 	/**
 	 * set the visible map area
 	 * @param p Placemark to center the map at
 	 */
 	public void setMapPosition(Placemark p) {
 		blockEventHandler = true;
 		geoMap.setCenter(p.getPoint());
 		int resultType = p.getAccuracy();
 		if (resultType == GeoAddressAccuracy.COUNTRY)
 			geoMap.setZoomLevel(6);
 		else if (resultType == GeoAddressAccuracy.REGION)
 			geoMap.setZoomLevel(8);
 		else if (resultType == GeoAddressAccuracy.SUB_REGION)
 			geoMap.setZoomLevel(9);
 		else if (resultType == GeoAddressAccuracy.TOWN)
 			geoMap.setZoomLevel(11);
 		else if (resultType == GeoAddressAccuracy.POSTAL_CODE)
 			geoMap.setZoomLevel(12);
 		else if (resultType == GeoAddressAccuracy.STREET)
 			geoMap.setZoomLevel(13);
 		else if (resultType == GeoAddressAccuracy.INTERSECTION)
 			geoMap.setZoomLevel(15);
 		else if (resultType == GeoAddressAccuracy.ADDRESS)
 			geoMap.setZoomLevel(16);
 		blockEventHandler = false;
 		
 		// inform GeoManager that the visible area has changed
 		geoManager.setBoundingBox(getBoundingBox());
 	}
 	
 	private void showLocationSelector(JsArray<Placemark> locations) {
 		LocationSelector popup = new LocationSelector(locations);
 		popup.center();
 	}
 	
 	/**
 	 * Add a pin to the map.
 	 * @param obj The ClientGeoObject which should be added.
 	 */
 	public void addPin(ClientGeoObject obj)
 	{
 		GeoPin newPin = new GeoPin(geoManager, obj);
 		geoMap.addOverlay(newPin);
 		geoPins.add(newPin);
 	}
 	
 	/**
 	 * Add multiple pins to the map.
 	 * @param objList The list of ClientGeoObjects which should be added.
 	 */
 	public void addPins(List<ClientGeoObject> objList)
 	{
 		Iterator<ClientGeoObject> iterator = objList.iterator();
 		while (iterator.hasNext())
 			addPin(iterator.next());
 	}
 	
 	/**
 	 * Set multiple pins on the map. Already existing pins are deleted.
 	 * @param objList The list of ClientGeoObjects which should be added.
 	 */
 	public void setPins(List<ClientGeoObject> objList)
 	{
 		clearPins();
 		addPins(objList);
 	}
 	
 	/**
 	 * Clear existing pins on the map.
 	 */
 	public void clearPins()
 	{
 		Iterator<GeoPin> iter = geoPins.iterator();
 		while (iter.hasNext())
 			geoMap.removeOverlay(iter.next());
 		geoPins.clear();
 	}
 
 	/**
 	 * This function is called when a map move event ends. The bounding box of the GeoManager has
 	 * to be updated.
 	 * @param event The MapDragEndEvent.
 	 */
 	public void onMoveEnd(MapMoveEndEvent event)
 	{
 		if (blockEventHandler)
 			return;
 		geoManager.setBoundingBox(getBoundingBox());
 		//Clear the "Where" string within the SearchBox since we changed the currently displayed map region.
 		geoManager.clearWhereString();
 	}
 	
 	/**
 	 * This function returns the bounding box which represents the currently displayed region within the map.
 	 * @return The bounding box, containing the coordinates.
 	 */
 	private BoundingBox getBoundingBox()
 	{
 		return(new BoundingBox(
 			geoMap.getBounds().getSouthWest().getLongitude(),
 			geoMap.getBounds().getSouthWest().getLatitude(),
 			geoMap.getBounds().getNorthEast().getLongitude(),
 			geoMap.getBounds().getNorthEast().getLatitude()));
 	}
 	
 	/**
 	 * This function creates the detail view which is displayed when a pin is clicked.
 	 * @param pin The pin which has been clicked.
 	 * @return The created detail view.
 	 */
 	public DetailView createDetailView(ClientGeoObject obj)
 	{
 		DetailView detailView=new DetailView(obj, geoManager);
 		geoMap.getInfoWindow().open(obj.getPoint(), detailView);
 		return detailView;
 	}
 	
 	public void addControl(Control control) {
 		geoMap.addControl(control);
 	}
 	
 	public void addControl(Control control, ControlPosition position) {
 		geoMap.addControl(control, position);
 	}
 }
