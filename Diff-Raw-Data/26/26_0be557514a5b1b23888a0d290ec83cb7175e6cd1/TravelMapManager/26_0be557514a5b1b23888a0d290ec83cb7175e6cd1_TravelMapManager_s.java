 package croo.szakdolgozat.client.presenter;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.user.client.ui.Frame;
 
 import croo.szakdolgozat.shared.Route;
 import croo.szakdolgozat.shared.Town;
 
public class MapManager
 {
 	private MapWidget map;
 
	public MapManager(MapWidget map)
 	{
 		this.map = map;
 	}
 
 	public void drawRoute(final Route route)
 	{
 		map.clearOverlays();
 		map.getInfoWindow().setMaximizeEnabled(true);
 		map.addOverlay(route.getRouteWayInJSO());
 		map.addOverlay(markerOfTown(route.getEndTown()));
 
 		GWT.log("Drawing the route and the destination marker.");
 	}
 
 	private Marker markerOfTown(final Town town)
 	{
 		final Marker marker = new Marker(town.getTownCoordinateInJSO());
 		final PlacesListPanel placesPanel = new PlacesListPanel(town, map.getInfoWindow());
 		marker.addMarkerClickHandler(new MarkerClickHandler() {
 			@Override
 			public void onClick(MarkerClickEvent event)
 			{
 				if (!map.getInfoWindow().isVisible())
 					showInterestingPlaces(marker, placesPanel);
 			}
 		});
 		return marker;
 	}
 
 	// TODO: Default max content is like WTF. fix that...
 	private void showInterestingPlaces(final Marker destination, final PlacesListPanel placesPanel)
 	{
 		InfoWindowContent initContent = new InfoWindowContent(placesPanel);
 		initContent.setMaxContent(new Frame("http://www.asdf.com"));// placesPanel.createMaxContent("http://www.asdf.com"));
 		initContent.setNoCloseOnClick(true);
 		map.getInfoWindow().open(destination, initContent);
 	}
 
 	public void addMapClickHandler(TravelMapPresenter travelMapPresenter)
 	{
 		map.addMapClickHandler(travelMapPresenter);
 	}
 
 	public MapWidget getMap()
 	{
 		return map;
 	}
 
 	public void initMap()
 	{
 		LatLng budapest = LatLng.newInstance(47.49841, 19.04076);
 		map.setCenter(budapest);
 		map.setZoomLevel(7);
 		map.setSize("800px", "600px");
 		map.setTitle("Utazz a MÁVval!");
 		map.setScrollWheelZoomEnabled(true);
 		map.setContinuousZoom(true);
 		map.addControl(new LargeMapControl());
 	}
 }
