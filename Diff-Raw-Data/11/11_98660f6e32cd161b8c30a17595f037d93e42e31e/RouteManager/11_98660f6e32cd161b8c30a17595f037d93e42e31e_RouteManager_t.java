 // Cycle Root Planner
 // Copyright (C) 2012  Benjamin Gibbs
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>
 
 package cyclerouteplanner.client;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.maps.gwt.client.DirectionsLeg;
 import com.google.maps.gwt.client.DirectionsRequest;
 import com.google.maps.gwt.client.DirectionsResult;
 import com.google.maps.gwt.client.DirectionsRoute;
 import com.google.maps.gwt.client.DirectionsService;
 import com.google.maps.gwt.client.DirectionsStatus;
 import com.google.maps.gwt.client.DirectionsStep;
 import com.google.maps.gwt.client.GoogleMap.ClickHandler;
 import com.google.maps.gwt.client.LatLng;
 import com.google.maps.gwt.client.MouseEvent;
 import com.google.maps.gwt.client.TravelMode;
 import com.google.maps.gwt.client.UnitSystem;
 
 import cyclerouteplanner.client.Events.ClearRouteEvent;
 import cyclerouteplanner.client.Events.ClearRouteListener;
 import cyclerouteplanner.client.Events.EventGenerator;
 import cyclerouteplanner.client.Events.RemoveLastPointEvent;
 import cyclerouteplanner.client.Events.RemoveLastPointListener;
 import cyclerouteplanner.client.Events.RouteUpdatedEvent;
 import cyclerouteplanner.client.Events.RouteUpdatedListener;
 
 public class RouteManager implements ClickHandler  {
 
 	private LinkedList<LatLng> clicks = new LinkedList<LatLng>();
 	private LinkedList<List<DirectionsStep>> route = new LinkedList<List<DirectionsStep>>();
 	private EventGenerator<RouteUpdatedEvent> routeUpdatedEventor = new EventGenerator<RouteUpdatedEvent>(); 
 	private double distance = 0.0;
 
 	private DirectionsService dirSvc = DirectionsService.create();
 	
 	public void addRouteUpdatedListener(RouteUpdatedListener listener){
 		routeUpdatedEventor.addListener(listener);
 	}
 	
 	@Override public void handle(MouseEvent event) {
 	
 		GWT.log("Mouse clicked: " + event);
 		LatLng latLng = event.getLatLng();
 		clicks.add(latLng);
 
 		if (clicks.size() < 2) {
 			notifyRouteChange();
 			return;
 		}
 
 		LatLng start = clicks.get(clicks.size() - 2);
 		LatLng end = clicks.getLast();
 
 		DirectionsRequest request = DirectionsRequest.create();
 		request.setOrigin(start);
 		request.setDestination(end);
		request.setTravelMode(TravelMode.DRIVING);
 		request.setUnitSystem(UnitSystem.METRIC);
		
		
 		request.setProvideRouteAlternatives(false);
 
 //		if (!GWT.isScript()) {
 //			removeGwtObjectId(request.getJso());
 //		}
 
 		GWT.log("Requesting route");
 		dirSvc.route(request, new DirectionsService.Callback() {
 			@Override public void handle(DirectionsResult response, DirectionsStatus status) {
 				routeUpdated(response, status);
 			}
 		});
 	}
 	
 	private void routeUpdated(DirectionsResult response, DirectionsStatus status){
 		GWT.log("RouteUpdated: " + response);
		if (DirectionsStatus.OK == status) {
 			DirectionsRoute newRoute = response.getRoutes().get(0);
 			JsArray<DirectionsLeg> newLegs = newRoute.getLegs();
 			List<DirectionsStep> newSteps = new ArrayList<DirectionsStep>();
 			for(int j =0; j < newLegs.length(); ++j){
 				DirectionsLeg leg = newLegs.get(j);
 				distance += leg.getDistance().getValue();
 				JsArray<DirectionsStep> steps = leg.getSteps();
 				for (int i = 0; i < steps.length(); i++) {
 					newSteps.add(steps.get(i));
 				}
 			}
 			route.add(newSteps);
 			notifyRouteChange();
 		} else {
			GWT.log("Failed to get route. Status: " + status.getValue());
 		}
 	}
 	
 	public List<LatLng> getRoute()
 	{
 		List<LatLng> points = new ArrayList<LatLng>();
 		for(List<DirectionsStep> part : route){
 			for(DirectionsStep step : part){
 				JsArray<LatLng> path = step.getPath();
 				for(int i = 0; i < path.length(); i++){
 					points.add(path.get(i));	
 				}
 			}
 		}
 		return points;
 	}
 	
 
 	// Workaround
 	private native void removeGwtObjectId(JavaScriptObject jso) /*-{
 		delete jso['__gwt_ObjectId'];
 	}-*/;
 	
 	ClearRouteListener getClearRouteListener(){
 		return new ClearRouteListener() {
 			@Override public void onEvent(ClearRouteEvent event) {
 				clicks.clear();
 				route.clear();
 				distance = 0.0;
 				notifyRouteChange();
 			}
 		};
 	}
 	
 	RemoveLastPointListener getRemoveLastPointListener(){
 		return new RemoveLastPointListener() {
 			@Override public void onEvent(RemoveLastPointEvent event) {
 				
 				if(clicks.size() == 0)
 					return;
 				
 				clicks.removeLast();
 				
 				if(route.size() == 0) {
 					notifyRouteChange();
 					return;
 				}
 				
 				List<DirectionsStep> lastLeg =route.removeLast();
 				for(DirectionsStep step : lastLeg){
 					distance -= step.getDistance().getValue();
 				}
 				
 				notifyRouteChange();
 			}
 
 		};
 	}
 	private void notifyRouteChange() {
 		LatLng start = (clicks.size() > 0) ? clicks.get(0) : null;
 		routeUpdatedEventor.onEvent(new RouteUpdatedEvent(start,route,distance));
 	}
 
 
 }
