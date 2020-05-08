 package org.routy.task;
 
 import org.routy.model.Route;
 import org.routy.model.RouteOptimizePreference;
 import org.routy.model.RouteRequest;
 import org.routy.service.RouteService;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public abstract class CalculateRouteTask extends AsyncTask<RouteRequest, Void, Route> {
 
 	private final String TAG = "CalculateRouteTask";
 
 
 	public CalculateRouteTask() {
 
 	}
 	
 	
 	@Override
 	protected Route doInBackground(RouteRequest...requests) {
 		try {
 //			List<Route> routes = getRoutes(requests[0].getOrigin(), requests[0].getDestinations(), null);
 			if (requests.length == 0) {
 				// TODO Handle this error...
 			} else {
 				RouteRequest request = requests[0];
 				
				RouteService routeService = new RouteService(request.getOrigin(), request.getDestinations(), RouteOptimizePreference.PREFER_DISTANCE, false);
 				Route bestRoute = routeService.getBestRoute();
 				
 				return bestRoute;
 			}
 			
 			
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage());
 		}
 		return null;
 	}
 	
 	
 	@Override
 	protected void onPostExecute(Route result) {
 		if (result != null) {
 //			Log.d(TAG, "Closest destination address: " + result.getAddressLine(0));
 			Log.d(TAG, "Best route calculated.");
 			onRouteCalculated(result);
 		} else {
 			Log.e(TAG, "Best route returned was null.");
 		}
 	}
 	
 	
 	public abstract void onRouteCalculated(Route route);
 
 }
