 package transponders.transmob;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import transponders.transmob.StopTrip;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import android.graphics.Color;
 import android.os.CountDownTimer;
 import android.util.Log;
 
 public class RouteStopsLoader implements JSONRequest.NetworkListener{
 	private enum State {
 		STOPS, POLYLINE, STOPS_BY_TRIP, POLYLINE_BY_TRIP
 	}
 	private boolean isLoading; //isLoading - if currently performing async task
 	private String result;
 	private GoogleMap map;
 	private State state;
 	private ArrayList<Marker> stopMarkers;
 	private HashMap<Marker, Stop> stopMarkersMap;
 	private ArrayList<StopTrip> stopTrips;
 	//private HashMap<Stop, Date> stopTimesMap;
 	private int[] markerIcons = {R.drawable.bus_geo_border, R.drawable.train_geo_border, R.drawable.ferry_geo_border};
	private int[] roundMarkers= {R.drawable.bus_purple_circle_pin_border35x35, R.drawable.train_purple_circle_pin_border35x35, R.drawable.ferry_purple_circle_pin_border35x35}; 
 	private Route route2;
 	private Trip trip2;
 	private Polyline polyline;
 	//private LatLng userLatLng;
 	private CountDownLatch lock; //to perform unit tests
 	private ArrayList<Stop> stops;
 	private ArrayList<EstimatedBus> estimatedBuses;
 	private ArrayList<Marker> estimatedBusMarkers;
 	private HashMap<Marker, EstimatedBus> estimatedBusMarkersMap;
 	private CountDownTimer estimatedBusTimer;
 	private int serviceType;
 	private LoadingListener loadingListener;
 
 	public RouteStopsLoader(GoogleMap map, ArrayList<Marker> stopMarkers, HashMap<Marker,Stop> stopMarkersMap, Polyline polyline) {
 		isLoading = false;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		this.map = map;
 		this.stopMarkers = stopMarkers;
 		this.stopMarkersMap = stopMarkersMap;
 		route2= null;
 		this.polyline = polyline;
 		stopTrips = new ArrayList<StopTrip>();
 		//this.userLatLng = loc;
 		estimatedBuses = new ArrayList<EstimatedBus>();
 		estimatedBusMarkers = new ArrayList<Marker>();
 		estimatedBusMarkersMap = new HashMap<Marker, EstimatedBus>();
 	}
 
 	/**
      * A method to initialize the request to get the service route times
      * of each stop to the PHP web service. 
      *
      * @param stops ArrayList containing all the nearby stops.
      */
 	public void requestRouteStops(Route route) {
 		
 
 		
 		String urlString = "http://deco3801-010.uqcloud.net/routestops.php?route="
 				+ route.getCode() + "&type=" + route.getType() + "&directions=" + route.getDirection();
 
 		JSONRequest request = new JSONRequest();
 		request.setListener(this);
 		request.execute(urlString);
 		isLoading = true;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		state = State.STOPS;
 		route2=route;
 
 	}
 	
 	public void requestRouteLine(Route route) {
 		
 
 		
 		String urlString = "http://deco3801-010.uqcloud.net/routeline.php?route="
 				+ route.getCode() + "&type=" + route.getType();
 
 		JSONRequest request = new JSONRequest();
 		request.setListener(this);
 		request.execute(urlString);
 		isLoading = true;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		state = State.POLYLINE;
 
 	}
 	
 	public void requestTripStops(Trip trip) {
 		stopTrips.clear();
 		serviceType = (int) (Math.log(trip.getRoute().getType()) / Math.log(2));
 		String urlString = "http://deco3801-010.uqcloud.net/tripstops.php?tripId="+trip.getTripId();
 		JSONRequest request = new JSONRequest();
 		request.setListener(this);
 		request.execute(urlString);
 		isLoading = true;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		state = State.STOPS_BY_TRIP;
 		this.trip2 = trip;
 	}
 	
 	public void requestTripLine(Trip trip) {
 		String urlString = "http://deco3801-010.uqcloud.net/tripline.php?tripId=" + trip.getTripId();
 		JSONRequest request = new JSONRequest();
 		request.setListener(this);
 		request.execute(urlString);
 		isLoading = true;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		state = State.POLYLINE_BY_TRIP;
 	}
 
 	/**
      * A method that defines what should be done after the PHP
      * request is completed. Overrides the networkRequestCompleted
      * method of the JSONRequest.NetworkListener interface.
      *
      * @param result The String result from the PHP service request.
      */
 	@Override
 	public void networkRequestCompleted(String result) {
 		isLoading = false;
 		if (loadingListener != null) {
 			loadingListener.onStateChange(isLoading);
 		}
 		this.result = result;
 
 		if (state == State.POLYLINE || state == State.POLYLINE_BY_TRIP) {
 			//get the line from JSON and add to map
 			String line = parseJSONToLine();
 			if (line != null) {
 				addLineToMap(line);
 			}
 		} else if (state == State.STOPS){ 
 			stops = parseJSONToStops();
 			addMarkersToMap(stops);
 			requestRouteLine(route2);
 		
 		} else if (state == State.STOPS_BY_TRIP) {
 			stops = parseJSONToStops();
 			/*stopTimesMap = new HashMap<Stop, Date>();
 			for (Stop stop: stops) {
 				stopTimesMap.put(stop, stopTrips.find(stop).getTime());
 			}*/
 			
 			addMarkersToMap(stops);
 			requestTripLine(trip2);
 			setEstimatedBuses(setUpBuses());
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return boolean if RouteDataLoader is performing an asynchronous task
 	 */
 	public boolean isLoading() {
 		return isLoading;
 	}
 
 	/**
      * A method to get the departure times of each service route.
      *
      */
 	public ArrayList<Stop> parseJSONToStops() {
 		ArrayList<Stop> output = new ArrayList<Stop>();
 		/*Use JSON-simple to parse the result. The format (of parts used) is below:
 			* {Stops: [StopId, Description, Position: {Lat, Lng}, HasParentLocation,
 			* ParentLocation: {Id, Position: {Lat, Lng}}, Routes: [Code, Name]]}
 			*/
 		if(state != State.STOPS && state != State.STOPS_BY_TRIP){
 			return output;
 		}
 			Object obj = JSONValue.parse(result);
 			try {
 				Log.d("Location", "result="+result);
 				JSONArray array = (JSONArray)((JSONObject)obj).get("Stops");
 				for (int i=0; i<array.size(); i++) {
 					JSONObject obj2 = (JSONObject)array.get(i);
 					Log.d("RESULT=", obj2.toJSONString());
 					JSONObject pos = (JSONObject)obj2.get("Position");
 					Stop stop = new Stop((String)obj2.get("StopId"), (String)obj2.get("Description"), ((Long)obj2.get("ServiceType")).toString(),
 							new LatLng((Double)pos.get("Lat"), (Double)pos.get("Lng")));
 					/*if ((Boolean)obj2.get("HasParentLocation")) {
 						JSONObject parent = (JSONObject)obj2.get("ParentLocation");
 						JSONObject parentPosJSON = (JSONObject)parent.get("Position");
 						LatLng parentPos = new LatLng((Double)parentPosJSON.get("Lat"), (Double)parentPosJSON.get("Lng"));
 						stop.setParentPosition((String)parent.get("Id"), parentPos);
 					}
 					JSONArray routes = (JSONArray)((JSONObject)obj2).get("Routes");
 					for (int j=0; j<routes.size(); j++) {
 						JSONObject route = (JSONObject)routes.get(j);
 						stop.addRoute(new Route((String)route.get("Code"), (String)route.get("Name"), (Integer)route.get("Vehicle")));
 					}*/
 					if (state == State.STOPS_BY_TRIP) {
 						StopTrip stopTrip = new StopTrip(stop, trip2);
 						String time = (String)((JSONObject)obj2).get("Time");
 						Date date = new Date(Long.parseLong(time
 								.substring(6, 18) )*10);
 						Log.d("Route", "setting date to "+ date);
 						stopTrip.setTime(date);
 						stopTrips.add(stopTrip);
 						
 						
 					}
 					output.add(stop);
 				}
 				return output;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
      * A method to format the departure time and add it to the list adapter.
      *
      */
 	public void addMarkersToMap(ArrayList<Stop> stops) {
 		// Remove old stops from map
 		/*for (Marker stopMarker : stopMarkers) {
 			Log.d("Location", "removing marker");
 			stopMarker.setVisible(false);
 			stopMarker.remove();
 
 		}
 		stopMarkers.clear();*/
 		
 		// Add new stop markers to map
 		if (stops != null) 
 		{
 			Log.d("Route", "about to add "+stops.size()+" stops to map");
 			for (Stop stop : stops)
 			{
 				serviceType = stop.getServiceType();
 				Log.d("serviceType", "" + serviceType);
 				
 				// Get the time from the stopTrips
 				String snippet = "";
 				if (state == State.STOPS_BY_TRIP) {
 					for (StopTrip st: stopTrips) {
 						if (stop == st.getStop()) {
 							/*Calendar c = Calendar.getInstance();
 							long currTime = c.getTimeInMillis();
 							long min = st.getTime().getTime(); 
 							Log.d("Route", "display minutes is "+min);
 							long minutes = (min - currTime) / 60000;
 							// Rounding, if seconds > 30, add another minute
 							long remainingMilis1 = (min - currTime) %  60000;
 							if(remainingMilis1 > 30000)
 								minutes += 1;
 							long remainingMins1 = minutes % 60;
 							String minFormat1 = remainingMins1 > 1 ? " Mins" : " Min";
 							snippet = remainingMins1 + minFormat1 + " until you arrive here assuming you \n catch the next bus at the previously selected stop.";*/
 							String dateString = new SimpleDateFormat("HH:mm").format(st.getTime());
 							snippet = "Selected Trip Arrival Time: " + dateString;
 							break;
 						}
 					}
 				}
 				
 				
 				Marker m = map.addMarker(new MarkerOptions()
 						.position(stop.getPosition())
 						.title(stop.getDescription())
 						.snippet(snippet)
 						.icon(BitmapDescriptorFactory.fromResource(markerIcons[serviceType-1])));
 				stopMarkers.add(m);
 				stopMarkersMap.put(m, stop);
 			}
 			
 			map.setOnInfoWindowClickListener(null);
 			map.setOnMapClickListener(null);
 			map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
 
 				@Override
 				public boolean onMyLocationButtonClick() 
 				{
 					return false;
 				}
 				
 			});
 		} 
 	}
 	
 	public String parseJSONToLine() {
 		if (state != State.POLYLINE && state != State.POLYLINE_BY_TRIP) {
 			return null;
 		}
 		
 		Object obj = JSONValue.parse(result);
 		String output = null;
 		
 		try {
 			
 			if (state == State.POLYLINE) {
 				JSONArray array = (JSONArray)((JSONObject)obj).get("Paths");
 				//JSONObject obj2 = (JSONObject)array.get(0);
 				HashMap<Long, String> directionToPath = new HashMap<Long, String>();
 				
 				for(int i = 0; i < array.size(); i++)
 				{
 					JSONObject obj2 = (JSONObject) array.get(i);
 					Long direction = (Long) obj2.get("Direction");
 					String path = (String) obj2.get("Path");
 					
 					directionToPath.put(direction, path);
 				}
 				
 				output = directionToPath.get(route2.getDirection());
 			} else {
 				//array = (JSONArray)((JSONObject)obj).get("Path");
 				String path = (String)((JSONObject)obj).get("Path");
 				output = path;
 			}
 			
 			
 			
 		} catch (Exception e) {
 			//error loading data
 		}
 		return output;
 	}
 	
 	public void addLineToMap(String line) {
 		if (polyline != null) {
 			polyline.setVisible(false);
 		}
 		PolylineOptions polylineOptions = new PolylineOptions();
 		polylineOptions.width(5).color(Color.RED);
 		polylineOptions.addAll(PolylineDecoder.decodePoly(line));
 		polyline = map.addPolyline(polylineOptions);
 		
 		
 		//Debug
 		List<LatLng> test = PolylineDecoder.decodePoly(line);
 		Log.d("JSONRequest", "line(0)=" + test.get(0).toString());
 		Log.d("JSONRequest", "line(1)=" + test.get(1).toString());
 		Log.d("JSONRequest", "line(2)=" + test.get(2).toString());
 		if (lock != null) {
 			lock.countDown();
 		}
 	}
 	
 	public void removeLineFromMap() {
 		if (polyline != null) {
 			polyline.setVisible(false);
 			polyline.remove();
 			polyline = null;
 		}
 	}
 	
 	public void removeEstimatedServicesFromMap() {
 		if (estimatedBusMarkers != null) {
 			for(Marker m : estimatedBusMarkers)
 			{
 				m.setVisible(false);
 				m.remove();
 			}
 			
 			estimatedBusMarkers.clear();
 			estimatedBuses.clear();
 			estimatedBusMarkersMap.clear();
 		}
 	}
 	
 	public ArrayList<EstimatedBus> setUpBuses() {
 		removeEstimatedServicesFromMap();
 		
 		ArrayList<EstimatedBus> estimatedBuses = new ArrayList<EstimatedBus>();
 		for (int i =1; i < stopTrips.size(); i++) {
 			StopTrip stStart = stopTrips.get(i-1);
 			StopTrip stEnd = stopTrips.get(i);
 			EstimatedBus bus = new EstimatedBus(stStart.getStop().getPosition(), stEnd.getStop().getPosition(), stStart.getTime(), stEnd.getTime());
 			Log.d("Bus", "Created new bus at " + bus.getPosition());
 			estimatedBuses.add(bus);
 		}
 		return estimatedBuses;
 	}
 	
 	public void setEstimatedBuses(ArrayList<EstimatedBus> estimatedBuses) {
 		this.estimatedBuses = estimatedBuses;
 		estimatedBusMarkers = new ArrayList<Marker>();
 		estimatedBusMarkersMap = new HashMap<Marker, EstimatedBus>();
 		
 		String title = "Estimated ";
 		if(serviceType == 1)
 			title += "bus";
 		else if(serviceType == 2)
 			title += "train";
 		else
 			title += "ferry";
 		
 		for (EstimatedBus bus: estimatedBuses) {
 			Log.d("Bus", "adding the bus at "+bus.getPosition());
 			boolean visiboolean = bus.isActive();
 			Marker busMarker = map.addMarker(new MarkerOptions()
 				.position(bus.getPosition())
 				.title(title)
 				.visible(visiboolean)
 				.icon(BitmapDescriptorFactory
 						.fromResource(roundMarkers[serviceType-1])));
 			estimatedBusMarkers.add(busMarker);
 			estimatedBusMarkersMap.put(busMarker, bus);
 			
 		}
 		
 		updateBuses();
 		
 		if (estimatedBusTimer != null) {
 			estimatedBusTimer.cancel();
 		}
 		estimatedBusTimer = new CountDownTimer (Long.MAX_VALUE, 5000) {
 
 			@Override
 			public void onFinish() {
 				;
 				
 			}
 
 			@Override
 			public void onTick(long arg0) {
 				
 				updateBuses();
 				
 			}
 			
 		}.start();
 	}
 	
 	public void updateBuses() {
 		Calendar c = Calendar.getInstance();
 		Long currTime = c.getTimeInMillis();
 		for (EstimatedBus bus: estimatedBuses) {
 			if (polyline == null) {
 				bus.update(new Date(currTime));
 			} else {
 				bus.updateOnLine(new Date(currTime), polyline);
 			}
 			if (bus.isActive())
 			Log.d("Bus", "Bus at "+bus.getPosition()+" isActive="+bus.isActive());
 		}
 		System.out.println("There is "+ estimatedBusMarkers.size()+" markers");
 		for (Marker m: estimatedBusMarkers) {
 			EstimatedBus bus = estimatedBusMarkersMap.get(m);
 			if (bus.isActive()) {
 				m.setPosition(bus.getPosition());
 				m.setVisible(true);
 			} else {
 				m.setVisible(false);
 			}
 		}
 	}
 	
 	public void registerListener(LoadingListener listener) {
 		this.loadingListener = listener;
 	}
 	
 	/*Test methods*/
 	public void setCompletedAsyncTasksLatch(CountDownLatch lock) {
 		this.lock =lock;
 	}
 	public ArrayList<Stop> getStops() {
 		return stops;
 	}
 }
