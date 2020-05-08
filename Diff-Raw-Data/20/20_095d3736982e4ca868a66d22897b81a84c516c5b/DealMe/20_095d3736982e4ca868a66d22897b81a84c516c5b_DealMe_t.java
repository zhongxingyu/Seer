 package me.deal.client;
 
 import me.deal.client.events.DealsLocationEvent;
 import me.deal.client.model.DealsLocation;
 import me.deal.client.servlets.DealService;
 import me.deal.client.servlets.DealServiceAsync;
 import me.deal.client.servlets.DirectionsService;
 import me.deal.client.servlets.DirectionsServiceAsync;
 import me.deal.client.servlets.GeocodingService;
 import me.deal.client.servlets.GeocodingServiceAsync;
 import me.deal.client.view.main.GoogleMapWidget;
 import me.deal.client.view.main.ListWidget;
 import me.deal.client.view.menubar.LocationWidget;
 import me.deal.client.view.menubar.MenuWidget;
 import me.deal.shared.LatLngCoor;
 import me.deal.shared.Location;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.geolocation.client.Geolocation;
 import com.google.gwt.geolocation.client.Position;
 import com.google.gwt.geolocation.client.Position.Coordinates;
 import com.google.gwt.geolocation.client.PositionError;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class DealMe implements EntryPoint {
 	
     interface MyUiBinder extends UiBinder<Widget,DealMe> {
         MyUiBinder INSTANCE = GWT.create(MyUiBinder.class);
 }
 
 
 	private final DealServiceAsync dealService =  GWT.create(DealService.class);
 	private final DirectionsServiceAsync directionsService = GWT.create(DirectionsService.class);
 	private final GeocodingServiceAsync geocodingService = GWT.create(GeocodingService.class);
 	
 	private final HandlerManager eventBus = new HandlerManager(null);
 	
 //	@UiField
 //	HeaderWidget headerWidget;
 	
 //	@UiField
 //	FilterWidget filterWidget;	
 	
 	@UiField (provided=true)
 	MenuWidget menuWidget;
 	
 	@UiField (provided=true)
 	LocationWidget locationWidget;
 	
 	@UiField (provided=true)
 	ListWidget listWidget;
 	
 	@UiField (provided=true)
 	GoogleMapWidget googleMapWidget;
 	
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 //		headerWidget = new HeaderWidget();
 //		filterWidget = new FilterWidget(dealService, eventBus);
 		menuWidget = new MenuWidget(eventBus);
 		locationWidget = new LocationWidget(geocodingService, eventBus);
 		listWidget = new ListWidget(dealService, directionsService, eventBus);
 		googleMapWidget = new GoogleMapWidget(dealService, eventBus);
 		
 		getUserLocation();
 		
 		Widget w = MyUiBinder.INSTANCE.createAndBindUi(this);
         RootLayoutPanel.get().add(w);
 	}
 	
 	private void getUserLocation() {
 		Geolocation geo;
 		if((geo = Geolocation.getIfSupported()) != null) {
 			geo.getCurrentPosition(
 			new Callback<Position, PositionError>() {
 					@Override
 					public void onFailure(PositionError reason) {
 						Window.alert(reason.toString());
 					}
 
 					@Override
 					public void onSuccess(Position result) {
 						Coordinates userCoor = result.getCoordinates();
 						LatLngCoor userLatLng = new LatLngCoor(userCoor.getLatitude(), userCoor.getLongitude());
 						geocodingService.convertLatLngToAddress(userLatLng, new AsyncCallback<Location>() {
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Failed to geocode!");
 							}
 
 							@Override
 							public void onSuccess(final Location result) {
 								// Window.alert(result.getAddress() + "\n" + result.getCity() + ", " + result.getState() + " " + result.getZipCode());
 								DealsLocation.getInstance().setDealsLocation(result);
 								eventBus.fireEvent(new DealsLocationEvent());
 							}
 						});
 					}
 			});
 		}
 	}
 }
