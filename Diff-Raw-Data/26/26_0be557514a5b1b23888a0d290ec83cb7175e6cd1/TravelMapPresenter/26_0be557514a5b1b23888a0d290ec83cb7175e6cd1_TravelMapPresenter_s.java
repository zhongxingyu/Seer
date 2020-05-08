 package croo.szakdolgozat.client.presenter;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.Maps;
 import com.google.gwt.maps.client.event.MapClickHandler;
 import com.google.web.bindery.event.shared.EventBus;
 
 import croo.szakdolgozat.client.display.TravelMapDisplay;
 import croo.szakdolgozat.client.events.SendEvent;
 import croo.szakdolgozat.client.stubs.MapServiceAsync;
 import croo.szakdolgozat.client.stubs.callbacks.ErrorHandlingAsyncCallback;
 import croo.szakdolgozat.shared.Route;
 
 public class TravelMapPresenter implements MapClickHandler
 {
 	private static final String MY_GOOGLEAPI_AUTH_KEY = "AIzaSyD--gmXsvTyag6v_Li5-wsYfdlXTMyauCU";
 	private EventBus eventBus;
 	private TravelMapDisplay display;
	private MapManager mapManager;
 
 	private MapServiceAsync mapService;
 
 	public TravelMapPresenter(EventBus eventBus, TravelMapDisplay display, MapServiceAsync mapService)
 	{
 		this.eventBus = eventBus;
 		this.display = display;
 		this.mapService = mapService;
 	}
 
 	public void verifyTownInput(final String location)
 	{
 		GWT.log("Validating of input " + location + ".");
 		mapService.verifyLocation(location, new ErrorHandlingAsyncCallback<Boolean>() {
 			@Override
 			public void onSuccess(Boolean locationIsValid)
 			{
 				if (locationIsValid)
 					display.setErrorLabel("");
 				else
 					display.setErrorLabel(location + " nevű város nincs az adatbázisban.");
 			}
 		});
 	}
 
 	public void onSendButtonClicked(String startTown, String destinationTown)
 	{
 		display.setErrorLabel("");
 
 		GWT.log("Getting route information...");
 		mapService.getRoute(startTown, destinationTown, new ErrorHandlingAsyncCallback<Route>() {
 			@Override
 			public void onSuccess(Route route)
 			{
 				if (route != null) {
 					mapManager.drawRoute(route);
 					eventBus.fireEvent(new SendEvent());
 				} else {
 					display.setErrorLabel("Rossz városnevet adtál meg.");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onClick(MapClickEvent event)
 	{
 		GWT.log("You have clicked on the map.");
 	}
 
 	public void initializeMap()
 	{
 		Maps.loadMapsApi(MY_GOOGLEAPI_AUTH_KEY, "2", false, getMapInitThread());
 	}
 
 	private Runnable getMapInitThread()
 	{
 		return new Runnable() {
 			public void run()
 			{
				mapManager = new MapManager(new MapWidget());
 				mapManager.initMap();
 				mapManager.addMapClickHandler(TravelMapPresenter.this);
 				display.setTravelMap(mapManager.getMap());
 			}
 		};
 	}
 }
