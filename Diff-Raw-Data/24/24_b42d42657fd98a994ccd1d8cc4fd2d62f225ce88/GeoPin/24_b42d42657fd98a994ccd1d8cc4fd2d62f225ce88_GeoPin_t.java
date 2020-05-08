 package at.fakeroot.sepm.client;
 import at.fakeroot.sepm.shared.client.serialize.ClientGeoObject;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.Point;
 import com.google.gwt.maps.client.overlay.Icon;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 
 /**
  * The class GeoPin represents a marker on a Map
  * @author RK
  */
 public class GeoPin extends Marker implements MarkerClickHandler
 {
 	private ClientGeoObject clientGeoObject;
 	private IGeoManager geoManager;
 	private static MarkerOptions markerOptions = MarkerOptions.newInstance();
 	
 	/**
 	 * Constructor
 	 * @param geoManager an Object, that implements the Interface IGeoManager
 	 * @param clientGeoObject is needed to identify the Marker
 	 */
 	public GeoPin(IGeoManager geoManager, ClientGeoObject clientGeoObject)
 	{
 		super(clientGeoObject.getPoint(), markerOptions);
 		this.clientGeoObject = clientGeoObject;
 		this.geoManager = geoManager;
 		addMarkerClickHandler(this);
 		
		//MarkerOptions
		Icon markerIcon = Icon.newInstance(clientGeoObject.getImageUrl());
 		markerIcon.setIconAnchor(Point.newInstance(13, 40));
 		markerOptions.setIcon(markerIcon);
 		markerOptions.setTitle(clientGeoObject.getTitel());
 	}
 	
 	/**
	 * opens the DetailView, as soon as the marker is clicked
 	 */
 	public void onClick(MarkerClickEvent event) 
 	{
 		geoManager.showDetailView(clientGeoObject);
 	}
 
 	public ClientGeoObject getClientGeoObject() {
 		return (clientGeoObject);
 	}
 }
