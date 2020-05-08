 package me.deal.client.view.main;
 
 import java.util.ArrayList;
 
 import me.deal.client.events.DealsEvent;
 import me.deal.client.events.DealsEventHandler;
 import me.deal.client.model.Deals;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.shared.Deal;
 import me.deal.shared.LatLngCoor;
 import me.deal.shared.Location;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.event.MapDragEndHandler;
 import com.google.gwt.maps.client.event.MapZoomEndHandler;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.geom.LatLngBounds;
 import com.google.gwt.maps.client.geom.Point;
 import com.google.gwt.maps.client.overlay.Icon;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import static java.lang.Math.*;
 
 public class GoogleMapWidget extends Composite {
 
     private static GoogleMapWidgetUiBinder uiBinder = GWT
             .create(GoogleMapWidgetUiBinder.class);
 
     interface GoogleMapWidgetUiBinder extends UiBinder<Widget, GoogleMapWidget> {
     }
 
     private final DealServiceAsync dealService;
     private final HandlerManager eventBus;
     private boolean largeMap;
     
     private ListItemWidget currentListItemWidget;
     private ArrayList <Marker> currentMarks;
     public boolean dragged = false;
 
     @UiField
     MapWidget mapWidget;
     
     public @UiConstructor GoogleMapWidget(final DealServiceAsync dealService,
             final HandlerManager eventBus, boolean largeMap) {
         initWidget(uiBinder.createAndBindUi(this));
         this.dealService = dealService;
         this.eventBus = eventBus;
         this.largeMap = largeMap;
         
         this.currentMarks = new ArrayList<Marker>();
         this.currentListItemWidget = new ListItemWidget(eventBus);
         initialize();
     }
     
     /* Initializes the map. If the website is in List View, create a small map. If in large view, create a full screen map.*/
     private void initialize() {
         
         if (largeMap)
             mapWidget.setSize("100%", "100%");
         else
         	mapWidget.setSize("350px", "350px");
 
         mapWidget.addControl(new LargeMapControl());
         
         /* The following handler responds when new deals are loaded or the user location is changed so that the
          * map can update with the new information. In all cases, the an array of Markers corresponding to the deals 
          * is created and then placed on the map. If in List View, certain conditions will resize the map to ensure 
          * that all deals are visible.     
          */
         eventBus.addHandler(DealsEvent.TYPE,
             new DealsEventHandler(){
                 @Override
                 public void onDeals(DealsEvent event) {
                 	
                 	Integer dealIndex = Deals.getInstance().getDealIndex();
                 	Integer dealRadius = Deals.getInstance().DEFAULT_NUM_DEALS/2;
                 	Integer totalNumDeals = Deals.getInstance().getDeals().size();
                 	
                 	Integer minDealIndex = (dealIndex - dealRadius < 0) ? 0 : (dealIndex - dealRadius);
                 	Integer maxDealIndex = (dealIndex + dealRadius > totalNumDeals) ? totalNumDeals - 1 : (dealIndex + dealRadius - 1);
                 	
                 	LatLngBounds bounds = getLatLngBounds(minDealIndex, maxDealIndex);
                     
                 	mapWidget.setCenter(Deals.getInstance().getLocation().getLatLng().convert());
                 	mapWidget.setZoomLevel(mapWidget.getBoundsZoomLevel(bounds));
                	// System.out.println("zoomLevel = " + mapWidget.getBoundsZoomLevel(bounds));
                	// System.out.println("Bounds Min = " + bounds.getSouthWest().getLatitude() + ", " + bounds.getSouthWest().getLongitude() + "\nBounds Max = " + bounds.getNorthEast().getLatitude() + ", " + bounds.getNorthEast().getLongitude());
                 	Deals.getInstance().setRadius(boundsToRadius(bounds));
                 	
                     currentMarks.clear();
                     dealsToMarkers(minDealIndex, maxDealIndex);
                     updateMarkers();
                     
                     Deals.getInstance().setResize(true);
                     dragged = false;
               }  
         });
         
         /* The following handler responds when the user manually drags the map's location, which results in reloading the 
          * deals and firing an event alerting the other widgets. Local variable "dragged" is set to true to indicate that
          * the map should not be resized.
          */
         mapWidget.addMapDragEndHandler(new MapDragEndHandler(){
         public void onDragEnd(MapDragEndEvent e) {
                 Deals deals = Deals.getInstance();
                 Location loc = deals.getLocation();
                 loc.setLatLng(new LatLngCoor(mapWidget.getCenter().getLatitude(), mapWidget.getCenter().getLongitude()));
                 deals.setLocation(loc);
                 deals.setOffset(0);
                 dealService.getYipitDeals(deals.getLocation().getLatLng(),
                     deals.getRadius(),
                     deals.DEFAULT_NUM_DEALS,
                     deals.getOffset(),
                     deals.getTags(),
                     new AsyncCallback<ArrayList<Deal>>() {
                         @Override
                         public void onFailure(Throwable caught) {
                             Window.alert("Failed to load deals.");
                         }
     
                         @Override
                         public void onSuccess(ArrayList<Deal> result) {
                             Deals deals = Deals.getInstance();
                             deals.setOffset(result.size());
                             deals.setDeals(result);
                             dragged = true;
                             eventBus.fireEvent(new DealsEvent());
                         }
                 });
             }
         });
         
         /* When the user zooms in or out on the map, get the new radius of the map.
          */
         mapWidget.addMapZoomEndHandler(new MapZoomEndHandler(){
             public void onZoomEnd(MapZoomEndEvent e)
             {
                 Deals.getInstance().setRadius(boundsToRadius(mapWidget.getBounds()));
             }
         });
         
 
     }
     
     //Takes the current deals and maps them to markers.
     private void updateMarkers() {
         mapWidget.clearOverlays(); 
         	
         for(Marker curr : currentMarks) {
             mapWidget.addOverlay(curr);
         }
     }
     
     private void dealsToMarkers(Integer minDealIndex, Integer maxDealIndex)
     {
     	ArrayList<Deal> deals = Deals.getInstance().getDeals();
         for(int i = minDealIndex; i < maxDealIndex; i++)
         {
             Marker temp = createMarker(deals.get(i));
             currentMarks.add(temp);
         }
     }
     
     /* Given a deal, creates the appropriate marker with the correct icon and behavior
      */
     private Marker createMarker(final Deal current)
     {
         Icon icon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=" + current.getIDUrl().toString() + "|" + current.getColor());
         icon.setInfoWindowAnchor(Point.newInstance(10, 10));
         icon.setShadowURL("http://www.google.com/mapfiles/shadow50.png");
         MarkerOptions ops = MarkerOptions.newInstance();
         ops.setClickable(true);
         ops.setIcon(icon);
         final Marker temp = new Marker(current.getBusinessAddress().getLatLng().convert(), ops);
         
         /* If the user clicks on the marker, the marker will open an InfoWindow revealing more information. If in Map
          * View, the marker will display a full ListItemWidget. If not, the InfoWindow will merely contain the name 
          * and phone number of the location.
          */
         temp.addMarkerClickHandler(new MarkerClickHandler() {
             public void onClick(MarkerClickEvent e)
             {
                 InfoWindowContent window;
                 if(!largeMap)
                 {
                     try
                     {
                         window  = new InfoWindowContent(current.getDealBusinessInfo().getName() + "<br>" + current.getBusinessPhoneNumber() + "</br>");                 
                     } 
                     catch(NullPointerException n) 
                     {
                         window  = new InfoWindowContent(current.getTitle() + "<br>" + current.getBusinessPhoneNumber() + "</br>");                 
                     }
                     window.setMaxWidth(25);
                 }
                 else
                 {
                 	currentListItemWidget.setDeal(current);
                     window = new InfoWindowContent(currentListItemWidget);
                 }
                 mapWidget.getInfoWindow().open(temp, window);
             }
         });
 
         return temp;
     }
     
     public void centerMarker(Deal current)
     {
         mapWidget.setCenter(current.getBusinessAddress().getLatLng().convert());
     }
     
     public void setMapSize(boolean mapView)
     {
         this.largeMap = mapView;
     }
     
     // Uses Haversine formula to calculate the radius represented by a coordinate distance.
     private double boundsToRadius(LatLngBounds coords)
     {
         LatLng NE = coords.getNorthEast();
         LatLng center = coords.getCenter();
         double lon = NE.getLongitude() - center.getLongitude();
         double lat = NE.getLatitude() - center.getLatitude();
         double a = pow(sin(lat/2), 2) + cos(center.getLatitude())*cos(NE.getLatitude())*pow(sin(lon/2), 2);
         double c = 2*atan2(sqrt(a), sqrt(1-a))*0.0174532925;
         return 3961.3 * c;
     }
     
     // Figures out the bounds of the deal based on all the deals that will be displayed on the map
     private LatLngBounds getLatLngBounds(Integer minDealIndex, Integer maxDealIndex) {
     	Double minLat = 90.0;
     	Double maxLat = -90.0;
     	Double minLng = 180.0;
     	Double maxLng = -180.0;
     	// System.out.println("maxDealIndex = " + maxDealIndex);
     	// System.out.println("minDealIndex = " + minDealIndex);
     	ArrayList<Deal> deals = Deals.getInstance().getDeals();
     	for(int i = minDealIndex; i <= maxDealIndex; i++) {
     		LatLngCoor dealLatLng = deals.get(i).getBusinessAddress().getLatLng();
     		minLat = ((minLat > dealLatLng.getLatitude()) ? dealLatLng.getLatitude() : minLat);
     		maxLat = ((maxLat < dealLatLng.getLatitude()) ? dealLatLng.getLatitude() : maxLat);
     		minLng = ((minLng > dealLatLng.getLongitude()) ? dealLatLng.getLongitude() : minLng);
     		maxLng = ((maxLng < dealLatLng.getLongitude()) ? dealLatLng.getLongitude() : maxLng);
     	}
     	
     	/*
     	for(int i = minDealIndex; i <= maxDealIndex; i++) {
     		LatLngCoor dealLatLng = deals.get(i).getBusinessAddress().getLatLng();
     		if(minLat == dealLatLng.getLatitude())
     			System.out.println("minLat = " + deals.get(i).getIDUrl());
     		if(minLng == dealLatLng.getLongitude())
     			System.out.println("minLng = " + deals.get(i).getIDUrl());
     		if(maxLat == dealLatLng.getLongitude())
     			System.out.println("maxLat = " + deals.get(i).getIDUrl());
     		if(maxLng == dealLatLng.getLatitude())
     			System.out.println("maxLng = " + deals.get(i).getIDUrl());
     	} */
 
 		return LatLngBounds.newInstance(LatLng.newInstance(minLat-.02, minLng-.02), LatLng.newInstance(maxLat+.02, maxLng+.02));
     }
 }
