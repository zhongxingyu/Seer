 package cpsc310.client;
 
 import java.util.Stack;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.MetaElement;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapOptions;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.InfoWindowContent.InfoWindowTab;
 import com.google.gwt.maps.client.control.LargeMapControl3D;
 import com.google.gwt.maps.client.event.MapClickHandler;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geocode.Geocoder;
 import com.google.gwt.maps.client.geocode.LatLngCallback;
 import com.google.gwt.maps.client.geocode.LocationCallback;
 import com.google.gwt.maps.client.geocode.Placemark;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.geom.Point;
 import com.google.gwt.maps.client.geom.Size;
 import com.google.gwt.maps.client.overlay.Icon;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.maps.client.overlay.PolyEditingOptions;
 import com.google.gwt.maps.client.overlay.PolyStyleOptions;
 import com.google.gwt.maps.client.overlay.Polygon;
 import com.google.gwt.maps.client.streetview.LatLngStreetviewCallback;
 import com.google.gwt.maps.client.streetview.Pov;
 import com.google.gwt.maps.client.streetview.StreetviewClient;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaOptions;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaWidget;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.reveregroup.gwt.facebook4gwt.Facebook;
 import com.reveregroup.gwt.facebook4gwt.ShareButton;
 
 public class PropertyMap {
 	private MapWidget map;
 	private MapOptions mOptions;
 	private StreetviewPanoramaWidget panorama;
 	private StreetviewClient svClient;
 	// keep a stack of all the markers
 	private Stack<Marker> markers = new Stack();
 	private Geocoder geocoder;
 
 	private LoginServiceAsync loginService = GWT.create(LoginService.class);
 
 	// polygon settings
 	private String color = "#FF0000";
 	private double opacity = 0.1;
 	private int weight = 1;
 	private boolean fillFlag = true;
 	private Polygon lastPolygon = null;
 	private boolean specifyingRegion = false;
 
 	/**
 	 * Constructor Instantiates two instances of a map - standard and streetview
 	 * 
 	 * @param location
 	 *            - location you want the map centered on
 	 * 
 	 */
 
 	public PropertyMap(LatLng location) {
 		buildStreetViewMap(location);
 		buildStandardMap(location);
 
 		map.addMapClickHandler(new MapClickHandler() {
 			public void onClick(MapClickEvent e) {
 				LatLng point = e.getLatLng();
 				if (specifyingRegion) {
 					drawSquare(point);
 					// allow only one square to be drawn at a time
 					setSpecifyingRegion(false);
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * Sets up the streetview map centered on provided location
 	 * 
 	 * @param location
 	 *            - the location to center the map on
 	 */
 	public void buildStreetViewMap(LatLng location) {
 		StreetviewPanoramaOptions options = StreetviewPanoramaOptions
 				.newInstance();
 		options.setLatLng(location);
 		svClient = new StreetviewClient();
 		panorama = new StreetviewPanoramaWidget(options);
 		panorama.setSize("100%", "100%");
 	}
 
 	/**
 	 * Sets up the standard map centered on provided location
 	 * 
 	 * @param location
 	 *            - the location to center the map on
 	 */
 	public void buildStandardMap(LatLng location) {
 		mOptions = MapOptions.newInstance();
 		map = new MapWidget();
 		map.setCenter(location, 10);
 		map.setSize("100%", "100%");
 		// Add some controls for the zoom level
 		map.addControl(new LargeMapControl3D());
 	}
 
 	/**
 	 * Finds the location and plots it on the map changes the street-view too
 	 * 
 	 * @param location
 	 *            - string representation of the address
 	 * @param placeMarker
 	 *            - true if you want to place a marker on map and update
 	 *            streetview, false otherwise.
 	 * @return returns LatLng point
 	 * 
 	 */
 	public LatLng findLocation(final HouseData house, final boolean placeMarker) {
 		final LatLngWrapper llWrap = new LatLngWrapper();
 
 		LatLngCallback callback = new LatLngCallback() {
 
 			public void onFailure() {
 				Window.alert("Location not found");
 			}
 
 			public void onSuccess(LatLng point) {
 				// add the location onto the map
 				// check if it's on sale, true for third param if so.
 				if (placeMarker) {
 					// addSpecialMarker(point, house, house.getIsSelling());
 					addSpecialMarker(point, house);
 					// refreshStreetView(point);
 				}
 				llWrap.setResponse(point);
 			}
 		};
 		geocoder = new Geocoder();
 		geocoder.getLatLng(house.getAddress() + " VANCOUVER, BC", callback);
 
 		return llWrap.getLL();
 	}
 
 	/**
 	 * get the lat and long in an array
 	 * 
 	 * @param house
 	 *            houseData object
 	 * @return a size 2 double array of lat and long given the house address -
 	 *         array at index 0 is latitude, array at index 1 is longitude
 	 *         Returns null if the lat and long was not found.
 	 * 
 	 */
 
 	public Double[] getLL(final HouseData house) {
 		Double[] latLong = new Double[2];
 		LatLng point = findLocation(house, false);
 		if (point != null) {
 			latLong[0] = point.getLatitude();
 			latLong[1] = point.getLongitude();
 		} else
 			return null;
 		return latLong;
 	}
 
 	/**
 	 * Reverse geocoding to get the postal Code
 	 * 
 	 * @param address
 	 *            Address of the postal code to be found
 	 * @return postal code
 	 * 
 	 */
 
 	public String getPostalCodeInVancouver(String address) {
 		final PCWrapper pcWrap = new PCWrapper();
 
 		LocationCallback callback = new LocationCallback() {
 
 			public void onFailure(int statusCode) {
 				Window.alert("Failed to geocode position ");
 			}
 
 			public void onSuccess(JsArray<Placemark> locations) {
 				if (locations.length() > 1) {
 					// Window.alert("more than one postal code found");
 				}
 				for (int i = 0; i < locations.length(); ++i) {
 					Placemark location = locations.get(i);
 					String pc = location.getPostalCode();
 					// Window.alert(pc);
 					pcWrap.setPC(pc);
 				}
 			}
 		};
 
 		geocoder = new Geocoder();
 		geocoder.getLocations(address + " VANCOUVER, BC", callback);
 		return pcWrap.getPC();
 	}
 
 	/**
 	 * Adds a marker to the map
 	 * 
 	 * @param point
 	 *            latitude and longitude
 	 * @param location
 	 *            string representation of the location to be displayed in
 	 *            overlay
 	 */
 	private void addMarker(final LatLng point, final String location) {
 		final Marker marker = new Marker(point);
 		map.addOverlay(marker);
 		map.setCenter(point);
 		map.getInfoWindow().open(marker,
 				new InfoWindowContent(location.toLowerCase()));
 		refreshStreetView(point);
 
 		marker.addMarkerClickHandler(new MarkerClickHandler() {
 			public void onClick(MarkerClickEvent event) {
 				try {
 					map.getInfoWindow().open(marker,
 							new InfoWindowContent(location.toLowerCase()));
 					refreshStreetView(point);
 				} catch (Exception e) {
 					Window.alert(e.getMessage());
 				}
 
 			}
 		});
 
 		markers.push(marker);
 	}
 
 	/**
 	 * Adds a marker to the map, if the property is on sale, it will be a green
 	 * marker
 	 * 
 	 * @param point
 	 *            latitude and longitude
 	 * @param location
 	 *            string representation of the location to be displayed in
 	 *            overlay
 	 * @param onSale
 	 *            true if the property is on sale
 	 * 
 	 */
 	private void addSpecialMarker(final LatLng point, final HouseData house) {
 
 		// Set up the marker and Icon
 		Icon icon = setIcon(house.getIsSelling());
 		MarkerOptions options = MarkerOptions.newInstance();
 		options.setIcon(icon);
 
 		final Marker marker = new Marker(point, options);
 		map.addOverlay(marker);
 		map.setCenter(point);
 
 		// Assemble the info window
 		final InfoWindowContent content = buildInfoWindow(house);
 		map.getInfoWindow().open(marker, content);
 
 		refreshStreetView(point);
 
 		// Click handler for each marker
 		marker.addMarkerClickHandler(new MarkerClickHandler() {
 			public void onClick(MarkerClickEvent event) {
 				try {
 					map.getInfoWindow().open(marker, content);
 					refreshStreetView(point);
 				} catch (Exception e) {
 					Window.alert(e.getMessage());
 				}
 
 			}
 		});
 
 		markers.push(marker);
 	}
 
 	/**
 	 * Assembles the info window for a marker given the house point
 	 * 
 	 * @param house
 	 *            HouseData point
 	 * @return InfoWindowContent either with one or two tabs
 	 */
 
 	private InfoWindowContent buildInfoWindow(HouseData house) {
 		InfoWindowContent iw;
 		VerticalPanel firstTab = getHouseInfoMarkerPanel(house);
 		// Show additional information if the house is being sold
 		if (house.getIsSelling()) {
 			VerticalPanel secondTab = getContactInfoMarkerPanel(house);
 			iw = getInfoWindowTabs(firstTab, secondTab);
 		} else
 			iw = new InfoWindowContent(firstTab);
 
 		return iw;
 	}
 
 	/**
 	 * if a property is on sale, the icon is set to real estate icon otherwise
 	 * the icon is set to a normal red marker
 	 * 
 	 * 
 	 * @param onSale
 	 * @return Icon
 	 */
 	private Icon setIcon(boolean onSale) {
 		// Set the icon
 		Icon icon;
 		String onSaleIconImgLink = "http://maps.google.com/mapfiles/ms/micons/realestate.png";
 		String onSaleIconShadowLink = "http://maps.google.com/mapfiles/ms/micons/realestate.shadow.png";
 		String normalIconImgLink = "http://maps.google.com/mapfiles/ms/micons/red-dot.png";
 		String normalIconShadowLink = "http://maps.google.com/mapfiles/ms/micons/msmarker.shadow.png";
 
 		// marker is a for sale sign if it's on sale, red otherwise
 		if (onSale) {
 			icon = Icon.newInstance(onSaleIconImgLink);
 			icon.setShadowURL(onSaleIconShadowLink);
 		} else {
 			icon = Icon.newInstance(normalIconImgLink);
 			icon.setShadowURL(normalIconShadowLink);
 		}
 		icon.setIconAnchor(Point.newInstance(6, 20));
 		icon.setInfoWindowAnchor(Point.newInstance(5, 1));
 
 		return icon;
 	}
 
 	/**
 	 * 
 	 * Takes in two vertical panels and puts them together
 	 * 
 	 * @param p1
 	 *            first tab content
 	 * @param p2
 	 *            second tab content
 	 * 
 	 */
 
 	private InfoWindowContent getInfoWindowTabs(VerticalPanel p1,
 			VerticalPanel p2) {
 
 		InfoWindowTab tabs[] = new InfoWindowTab[2];
 
 		tabs[0] = new InfoWindowTab("Info", p1);
 		tabs[1] = new InfoWindowTab("Contact", p2);
 		final InfoWindowContent content = new InfoWindowContent(tabs, 0);
 		return content;
 	}
 
 	/**
 	 * 
 	 * Takes in house data object and adds house info into panel
 	 * 
 	 * @param house
 	 *            the house object
 	 * 
 	 */
 
 	private VerticalPanel getHouseInfoMarkerPanel(HouseData house) {
 		VerticalPanel markerInfoWindow = new VerticalPanel();
 		HTML htmlWidget;
 		// If the house is on sale, provide extra field for sale price and
 		// realtor information
 		if (house.getIsSelling()) {
 			// TODO: isSelling field for a house is not set when realtor edits
 			// on table
 			// need to add event handler and modify houseData point
 			htmlWidget = new HTML("<p><b><u>Property Information</u></b></br> "
 					+ "<b>Address: </b>" + house.getAddress().toLowerCase()
 					+ "</br>" + "<b>Current Land Value: </b>"
 					+ house.getCurrentLandValue() + "</br>"
 					+ "<b>Year built: </b>" + house.getYearBuilt() + "</br>"
 					+ "<b>Selling Price: </b>" + house.getPrice() + "</br>"
 					+ "</p>");
 		} else {
 			htmlWidget = new HTML("<p><b><u>Property Information</u></b></br> "
 					+ "<b>Address: </b>" + house.getAddress().toLowerCase()
 					+ "</br>" + "<b>Current Land Value: </b>"
 					+ house.getCurrentLandValue() + "</br>"
 					+ "<b>Year built: </b>" + house.getYearBuilt() + "</p>");
 		}
 
 		markerInfoWindow.add(htmlWidget);
 
 		String shareURL = generateShareURL(house);
 		ShareButton shareBtn = new ShareButton(shareURL, "");
 		markerInfoWindow.add(shareBtn);
 
 		return markerInfoWindow;
 	}
 
 	/**
 	 * 
 	 * Get Realtor information. Assumes the house is being sold and realtor
 	 * information is available
 	 * 
 	 * @param house
 	 *            the house object
 	 * @return VerticalPanel containing the marker InfoWindow
 	 * 
 	 */
 
 	private VerticalPanel getContactInfoMarkerPanel(HouseData house) {
 		VerticalPanel markerInfoWindow = new VerticalPanel();
 		HTML htmlWidget;
 		String email = house.getOwner();
 		LoginInfo user = getUser(email);
 		if (user != null) {
 			String realtor = user.getNickname();
 			int phoneNumber = user.getphoneNumber();
 			String website = user.getWebsite();
 			String description = user.getDescription();
 
 			htmlWidget = new HTML("<p><b><u>Contact Information</u></b></br> "
 					+ "<b>Realtor: </b>" + realtor + "</br>" + "<b>Email: </b>"
 					+ email + "</br>" + "<b>Phone: </b>" + phoneNumber
 					+ "</br>" + "<b>Website: </b>" + website + "</br>"
 					+ "<b>About: </b>" + description + "</br>" + "</p>");
 		} else {
 			String realtor = "Temporary Name";
 			String phoneNumber = "6042534432";
 			String website = "www.google.com";
 			String description = "Hello, please contact me for real estates" +
 					"fjdsaljfldsajfdsfdlj long string here fjlksjfklsajfdsklfjdfds";
 			
 
 			htmlWidget = new HTML("<p><b><u>Contact Information</u></b></br> "
 					+ "<b>Realtor: </b>" + realtor + "</br>" + "<b>Email: </b>"
 					+ email + "</br>" + "<b>Phone: </b>" + phoneNumber
 					+ "</br>" + "<b>Website: </b>" + website + "</br>"
 					+ "<b>About: </b>" + description + "</br>" + "</p>");
 		}
 
 		markerInfoWindow.add(htmlWidget);
 		markerInfoWindow.setWidth("150px");
 		return markerInfoWindow;
 	}
 
 	public LoginInfo getUser(String userEmail) {
 		final LoginWrapper loginWrap = new LoginWrapper();
 		// add the user if not already in db
 		AsyncCallback<LoginInfo> userCallback = new AsyncCallback<LoginInfo>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}

 			public void onSuccess(LoginInfo user) {
 				loginWrap.setLogin(user);
 			}
 		};
 		loginService.getUser(userEmail, userCallback);
 
 		return loginWrap.getLogin();
 	}
 
 	/**
 	 * Changes streetview location given location coordinates
 	 * 
 	 * @param location
 	 *            latitude and longitude
 	 */
 
 	private void refreshStreetView(LatLng location) {
 		svClient.getNearestPanoramaLatLng(location,
 				new LatLngStreetviewCallback() {
 					@Override
 					public void onFailure() {
 						// streetview is not available
 					}
 
 					@Override
 					public void onSuccess(LatLng point) {
 						panorama.setLocationAndPov(point, Pov.newInstance());
 					}
 				});
 	}
 
 	/**
 	 * Changes streetview location given location coordinates
 	 * 
 	 * @param location
 	 *            latitude and longitude
 	 */
 
 	public void refreshStreetView(String location) {
 		LatLngCallback callback = new LatLngCallback() {
 
 			public void onFailure() {
 				// Window.alert("Location not found");
 			}
 
 			public void onSuccess(LatLng point) {
 				// refresh streetview with this location
 				svClient.getNearestPanoramaLatLng(point,
 						new LatLngStreetviewCallback() {
 							@Override
 							public void onFailure() {
 								// streetview is not available
 							}
 
 							@Override
 							public void onSuccess(LatLng point) {
 								panorama.setLocationAndPov(point, Pov
 										.newInstance());
 							}
 						});
 			}
 		};
 		geocoder = new Geocoder();
 		geocoder.getLatLng(location + " VANCOUVER, BC", callback);
 	}
 
 	/**
 	 * Clears all overlays from the map
 	 */
 	public void clearMap() {
 		map.clearOverlays();
 		if (lastPolygon != null)
 			lastPolygon = null;
 	}
 
 	/**
 	 * Clears all of the markers from the map
 	 */
 	public void clearMarkers() {
 
 		while (!markers.empty()) {
 			map.removeOverlay(markers.pop());
 		}
 	}
 
 	/**
 	 * Returns reference to the standard map
 	 */
 	public MapWidget getMap() {
 		return this.map;
 	}
 
 	/**
 	 * Returns reference to the streetView map
 	 */
 	public StreetviewPanoramaWidget getStreetViewMap() {
 		return this.panorama;
 	}
 
 	/**
 	 * 
 	 * Allows the user to edit the last drawn polygon
 	 * 
 	 */
 	public void editPolygon() {
 		if (lastPolygon == null) {
 			return;
 		}
 		lastPolygon.setEditingEnabled(PolyEditingOptions.newInstance(4));
 	}
 
 	/**
 	 * deletes the specified region on the map
 	 */
 	public void clearSpecifiedRegion() {
 		if (lastPolygon != null) {
 			map.removeOverlay(lastPolygon);
 			lastPolygon = null;
 		}
 	}
 
 	/**
 	 * Allows the user to draw a region on the map
 	 * 
 	 */
 	public void createPolygon() {
 		PolyStyleOptions style = PolyStyleOptions.newInstance(color, weight,
 				opacity);
 
 		final Polygon poly = new Polygon(new LatLng[0], color, weight, opacity,
 				color, fillFlag ? .7 : 0.0);
 		lastPolygon = poly;
 		map.addOverlay(poly);
 		poly.setDrawingEnabled();
 		poly.setStrokeStyle(style);
 	}
 
 	/**
 	 * 
 	 * Algorithm that calculates whether or not the point is in the polygon
 	 * 
 	 * @param point
 	 *            the point to check if it is in the polygon
 	 * 
 	 */
 	boolean isPointInPolygon(LatLng point) {
 
 		if (lastPolygon == null) {
 			Window.alert("No region specified");
 			return false;
 		}
 
 		int j = 0;
 		boolean oddNodes = false;
 		double y = point.getLatitude();
 		double x = point.getLongitude();
 		int numVertexes = lastPolygon.getVertexCount();
 
 		for (int i = 0; i < numVertexes; i++) {
 			j++;
 			if (j == numVertexes) {
 				j = 0;
 			}
 			if (((lastPolygon.getVertex(i).getLatitude() < y) && (lastPolygon
 					.getVertex(j).getLatitude() >= y))
 					|| ((lastPolygon.getVertex(j).getLatitude() < y) && (lastPolygon
 							.getVertex(i).getLatitude() >= y))) {
 				if (lastPolygon.getVertex(i).getLongitude()
 						+ (y - lastPolygon.getVertex(i).getLatitude())
 						/ (lastPolygon.getVertex(j).getLatitude() - lastPolygon
 								.getVertex(i).getLatitude())
 						* (lastPolygon.getVertex(j).getLongitude() - lastPolygon
 								.getVertex(i).getLongitude()) < x) {
 					oddNodes = !oddNodes;
 				}
 			}
 		}
 
 		if (oddNodes)
 			Window.alert("point is in the polygon");
 		else
 			Window.alert("point is not in the polygon");
 		return oddNodes;
 	}
 
 	/**
 	 * 
 	 * Draws a rectangle on the map given a corner point
 	 * 
 	 * @param point
 	 *            location of the corner of the rectangle
 	 * 
 	 */
 	public void drawSquare(LatLng point) {
 		PolyStyleOptions style = PolyStyleOptions.newInstance(color, weight,
 				opacity);
 		// the other four points of the triangle
 		LatLng point1 = LatLng.newInstance(point.getLatitude(), point
 				.getLongitude() + 0.1);
 		LatLng point2 = LatLng.newInstance(point.getLatitude() - 0.02, point
 				.getLongitude() + 0.1);
 		LatLng point3 = LatLng.newInstance(point.getLatitude() - 0.02, point
 				.getLongitude());
 
 		LatLng[] polygonPoints = new LatLng[5];
 		polygonPoints[0] = point;
 		polygonPoints[1] = point1;
 		polygonPoints[2] = point2;
 		polygonPoints[3] = point3;
 		polygonPoints[4] = point;
 
 		final Polygon poly = new Polygon(polygonPoints, color, weight, opacity,
 				color, fillFlag ? .7 : 0.0);
 		lastPolygon = poly;
 		map.addOverlay(poly);
 		poly.setStrokeStyle(style);
 		lastPolygon.setEditingEnabled(PolyEditingOptions.newInstance(5));
 
 	}
 
 	/**
 	 * 
 	 * Get the longitude values of the specified polygon
 	 * 
 	 * @pre there is a polygon defined on the map
 	 * 
 	 * @return an array of latitude values, null if no polygon was defined
 	 * 
 	 * 
 	 */
 	private Double[] getPolyLat() {
 		if (lastPolygon == null)
 			return null;
 		int numVertexes = lastPolygon.getVertexCount();
 		Double[] polyPoints = new Double[numVertexes];
 		LatLng point;
 
 		for (int i = 0; i < lastPolygon.getVertexCount(); i++) {
 			point = lastPolygon.getVertex(i);
 			polyPoints[i] = point.getLatitude();
 		}
 
 		return polyPoints;
 	}
 
 	/**
 	 * 
 	 * get the longitude values of the specified polygon
 	 * 
 	 * @pre there is a polygon defined on the map
 	 * @return an array of longitude values
 	 * 
 	 * 
 	 */
 	private Double[] getPolyLng() {
 		if (lastPolygon == null)
 			return null;
 		int numVertexes = lastPolygon.getVertexCount();
 		Double[] polyPoints = new Double[numVertexes];
 		LatLng point;
 
 		for (int i = 0; i < lastPolygon.getVertexCount(); i++) {
 			point = lastPolygon.getVertex(i);
 			polyPoints[i] = point.getLongitude();
 		}
 		return polyPoints;
 	}
 
 	/**
 	 * 
 	 * setter method for map click handler when drawing rectangle
 	 * 
 	 * @param specifyingRegion
 	 *            if the user is specifying a region, it's true
 	 * 
 	 */
 	public void setSpecifyingRegion(boolean specifyingRegion) {
 		this.specifyingRegion = specifyingRegion;
 	}
 
 	/**
 	 * 
 	 * access to the private boolean value specifyingRegion
 	 * 
 	 * @param specifyingRegion
 	 *            if the user is specifying a region, it's true
 	 * 
 	 */
 	public boolean isSpecifyingRegion() {
 		return specifyingRegion;
 	}
 
 	/**
 	 * Method to paratermize the base URL with a given house's civic number and
 	 * street name. Civic number stored in variable cn and street name is stored
 	 * in variable sn.
 	 * 
 	 * @pre house != null;
 	 * @post true;
 	 * @param house
 	 *            - the HouseData object to get the civic number and street name
 	 *            from.
 	 * @return the string containing the base URL with the civic number and
 	 *         street name of the given house as parameters.
 	 */
 	private String generateShareURL(HouseData house) {
 		String baseURL = GWT.getHostPageBaseURL();
 		String civicNumber = String.valueOf(house.getCivicNumber());
 		String streetName = house.getStreetName().toUpperCase();
 
 		// parameterize URL and add house parameters to the URL
 		String shareURL = baseURL + "?cn=" + civicNumber + "&sn=" + streetName;
 
 		return shareURL;
 	}
 
 	/**
 	 * 
 	 * Wrapper classes so we can grab data from async calls
 	 * 
 	 */
 
 	// Wrapper class for latlng
 	class LatLngWrapper {
 		LatLng theLatLng;
 
 		void setResponse(LatLng ll) {
 			this.theLatLng = ll;
 		}
 
 		LatLng getLL() {
 			return theLatLng;
 		}
 	}
 
 	// Wrapper class for postalCode
 	class PCWrapper {
 		String thePC;
 
 		void setPC(String pc) {
 			this.thePC = pc;
 		}
 
 		String getPC() {
 			return thePC;
 		}
 	}
 
 	// Wrapper class for LoginInfo
 	class LoginWrapper {
 		LoginInfo theLogin;
 
 		void setLogin(LoginInfo login) {
 			this.theLogin = login;
 		}
 
 		LoginInfo getLogin() {
 			return theLogin;
 		}
 	}
 }
