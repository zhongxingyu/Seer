 package com.HuskySoft.metrobike.algorithm;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.HuskySoft.metrobike.backend.APIQuery;
 import com.HuskySoft.metrobike.backend.DirectionsRequest;
 import com.HuskySoft.metrobike.backend.DirectionsStatus;
 import com.HuskySoft.metrobike.backend.GoogleAPIWrapper;
 import com.HuskySoft.metrobike.backend.GoogleMapsResponseStatusCodes;
 import com.HuskySoft.metrobike.backend.Leg;
 import com.HuskySoft.metrobike.backend.Location;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.Step;
 import com.HuskySoft.metrobike.backend.TravelMode;
 import com.HuskySoft.metrobike.backend.Utility;
 import com.HuskySoft.metrobike.backend.WebRequestJSONKeys;
 import com.HuskySoft.metrobike.backend.Utility.TransitTimeMode;
 
 /**
  * An abstract class for the implementation of a metrobike route-finding
  * algorithm. This abstract class provides error message and result managing
  * facilities and defines the interface for the various algorithm
  * implementations.
  * 
  * @author dutchscout
  */
 public abstract class AlgorithmWorker {
 
     /**
      * The TAG to use in this file for Android Log messages.
      */
     private static final String TAG = "com.HuskySoft.metrobike.algorithm: "
             + "AlgorithmWorker.java: ";
 
     /**
      * The maximum number of consecutive attempts to contact the Google server.
      */
     private static final int MAX_CONNECTION_ATTEMPTS = 2;
 
     /**
      * The maximum number of consecutive attempts to contact the Google server
      * after exceeding query limit.
      */
     private static final int MAX_QUERY_LIMIT_RETRIES = 2;
 
     /**
      * The time in milliseconds to wait between retries.
      */
     private static final long CONNECTION_RETRY_DELAY_MS = 1000;
 
     /**
      * The time in milliseconds to wait between retries after exceeding the
      * query limit.
      */
     private static final long QUERY_LIMIT_RETRY_DELAY_MS = 3000;
 
     /**
      * The time in milliseconds to wait between Google Transit request queries.
      * This is to prevent going over the query limit.
      */
     protected static final long TRANSIT_QUERY_DELAY_MS = 1500;
 
     /**
      * Holds any error message(s) generated during algorithm execution.
      */
     private String errorMessages = null;
 
     /**
      * The most recent status of this request; multiple errors are ignored, only
      * the most recent is stored here.
      */
     private DirectionsStatus mostRecentStatus = DirectionsStatus.NOT_YET_COMPLETE;
 
     /**
      * Holds the routes found/built by the worker.
      */
     private List<Route> results = null;
 
     /**
      * Holds the pointer that determines whether or not to call doQuery from the
      * stub class or the actual class that talks to the Google APIs. It defaults
      * to the real connection unless the stub is explicitly set in the parameter
      * object that is passed in to doRequest.
      */
     private APIQuery queryObj = new GoogleAPIWrapper();
 
     /**
      * Holds an arbitrary route to be shared amongst algorithms in order to
      * reduce transit API queries.
      */
     private Route referencedRoute = null;
     
     /**
      * This determines the language that directions responses
      * are returned in from API calls (English by default).
      * (this is populated from the DirectionsRequest parameter)
      */
     private String queryLanguage = null;
 
     /**
      * Runs the algorithm on the RequestParameters.
      * 
      * @param toProcess
      *            the RequestParameters object describing the search to make
      * @return the final status of the findRoutes process
      */
     public abstract DirectionsStatus findRoutes(DirectionsRequest.RequestParameters toProcess);
 
     /**
      * Saves given route so multiple algorithms can reference it.
      * 
      * @param route
      *            to set
      */
     public final void setQueryLanguage(final String queryLanguageToSet) {
         System.out.println(TAG + "setQueryLanguage()->queryLanguageToSet: "
                 + queryLanguageToSet);
         queryLanguage = queryLanguageToSet;
     }
     
     /**
      * Saves given route so multiple algorithms can reference it.
      * 
      * @param route
      *            to set
      */
     public final void setReferencedRoute(final Route route) {
         System.out.println(TAG + "setReferencedRoute()->route: " + route);
         referencedRoute = route;
     }
 
     /**
      * @return getReferencedRoute
      */
     public final Route getReferencedRoute() {
         System.out.println(TAG + "getReferencedRoute()->referencedRoute: " + referencedRoute);
         return referencedRoute;
     }
 
     /**
      * Returns true if there was an error, false otherwise.
      * 
      * @return true if there was an error, false otherwise
      */
     public final boolean hasErrors() {
         return mostRecentStatus.isError();
     }
 
     /**
      * Adds a message to the error message log.
      * 
      * @param theError
      *            the message to add
      * 
      * @return the error that was passed in
      */
     protected final DirectionsStatus addError(final DirectionsStatus theError) {
         return addError(theError, null);
     }
 
     /**
      * Sets whether to use the stub class to simulate the Google APIs or to
      * actually contact Google. The default is to actually contact Google so
      * this should only be set if you plan on using the stub method.
      * 
      * @param query
      *            This should be the StubGoogleAPIWrapper if this method is
      *            called.
      */
     public final void setResource(final APIQuery query) {
         if (query != null) {
             queryObj = query;
         }
     }
 
     /**
      * Getter for the queryObj.
      * 
      * @return returns the current APIQuery Object.
      */
     public final APIQuery getResource() {
         return queryObj;
     }
 
     /**
      * Adds a message to the error message log with extra details.
      * 
      * @param theError
      *            the message to add
      * @param extraDetails
      *            any extra information
      * @return the error that was passed in
      */
     protected final DirectionsStatus addError(final DirectionsStatus theError,
             final String extraDetails) {
         String toAdd = theError.getMessage();
         if (extraDetails != null) {
             toAdd += extraDetails;
         }
         if (errorMessages == null) {
             errorMessages = toAdd;
         } else {
             errorMessages = errorMessages + "\n" + toAdd;
         }
 
         mostRecentStatus = theError;
 
         return theError;
     }
 
     /**
      * Returns a String containing a human-readable error message.
      * 
      * @return A string if there is an error. Null otherwise.
      */
     public final String getErrors() {
         System.out.println(TAG + "getErrors()->errorMessages: " + errorMessages);
         return errorMessages;
     }
 
     /**
      * Clears all error messages generated during algorithm execution.
      */
     protected final void clearErrors() {
         errorMessages = null;
         mostRecentStatus = DirectionsStatus.NOT_YET_COMPLETE;
     }
 
     /**
      * The algorithm calls this to mark the findRoutes() process successful.
      * 
      * @return DirectionsStatus.REQUEST_SUCCESSFUL
      */
     protected final DirectionsStatus markSuccessful() {
         mostRecentStatus = DirectionsStatus.REQUEST_SUCCESSFUL;
         errorMessages = null;
 
         return DirectionsStatus.REQUEST_SUCCESSFUL;
     }
 
     /**
      * Add results found by the algorithm to the results list.
      * 
      * @param theResults
      *            the results to add
      */
     protected final void addResults(final List<Route> theResults) {
         if (results == null) {
             results = new ArrayList<Route>(theResults);
         } else {
             results.addAll(theResults);
         }
     }
 
     /**
      * This method returns the list of routes the resulted from the last call to
      * findRoutes().
      * 
      * @return A list of routes that this algorithm considers to be
      *         near-optimal. Returns null if no routes were found, if
      *         findRoutes() has not yet been run, or if there is an error.
      */
     public final List<Route> getResults() {
         return results;
     }
 
     /**
      * Returns the mostRecentStatus. Meant for use by individual algorithm
      * implementations.
      * 
      * @return the mostRecentStatus
      */
     protected final DirectionsStatus getMostRecentStatus() {
         return mostRecentStatus;
     }
 
     /**
      * Completes the URL query, handling any likely exceptions.
      * 
      * @param queryURL
      *            the URL to fetch
      * @return the response from the server, or null if there is an error
      */
     protected final String doQueryWithHandling(final String queryURL) {
         String response = null;
 
         int tryNum = 0;
         int tryNumQueryLimit = 0;
 
         while (response == null && tryNum < MAX_CONNECTION_ATTEMPTS
                 && tryNumQueryLimit < MAX_QUERY_LIMIT_RETRIES) {
             System.out.println(TAG + "doQueryWithHandling()->tryNum: " + tryNum);
             System.out
                     .println(TAG + "doQueryWithHandling()->tryNumQueryLimit: " + tryNumQueryLimit);
             try {
                 response = queryObj.doQuery(queryURL);
                 JSONObject responseJSON;
                 try {
                     responseJSON = new JSONObject(response);
                     String statusString = responseJSON.getString(WebRequestJSONKeys.STATUS
                             .getLowerCase());
                     if (statusString
                             .equalsIgnoreCase(GoogleMapsResponseStatusCodes.OVER_QUERY_LIMIT
                                     .toString())) {
                         response = null;
                         tryNumQueryLimit++;
                         System.err.println(TAG + "Over query limit... retrying "
                                + (MAX_QUERY_LIMIT_RETRIES - tryNumQueryLimit) + " more times.");
                        try {
                            Thread.sleep(QUERY_LIMIT_RETRY_DELAY_MS);
                        } catch (InterruptedException e1) {
                            // This means the user is trying to cancel us!
                        	addError(DirectionsStatus.USER_CANCELLED_REQUEST);
                        	return null;
                        	//System.err
                            //        .println(TAG + "Connection retry interrupted (not a problem)");
                        }
                     }
                 } catch (JSONException e) {
                     System.err.println(TAG + "Error parsing JSON response.");
                 }
             } catch (IOException e) {
                 tryNum++;
                 System.err.println(TAG + "Bad connection... retrying "
                         + (MAX_CONNECTION_ATTEMPTS - tryNum) + " more times.");
                 try {
                     Thread.sleep(CONNECTION_RETRY_DELAY_MS);
                 } catch (InterruptedException e1) {
                     //System.err.println(TAG + "Connection retry interrupted (not a problem)");
                 	// This means the user is trying to cancel us!
                 	addError(DirectionsStatus.USER_CANCELLED_REQUEST);
                 	return null;
                 }
             }
         }
 
         if (response == null) {
             addError(DirectionsStatus.CONNECTION_ERROR);
             return null;
         }
 
         return response;
     }
 
     /**
      * A helper method to build a list of routes from a String holding Google's
      * JSON response.
      * 
      * @param srcJSON
      *            the String holding the JSON to parse
      * @return a list of Routes parsed from the JSON
      */
     protected final List<Route> buildRouteListFromJSONString(final String srcJSON) {
         System.out.println(TAG + "buildRouteListFromJSONString()->Entering this method.");
         List<Route> routesList = new ArrayList<Route>();
 
         JSONObject myJSON;
 
         try {
             myJSON = new JSONObject(srcJSON);
             String statusString = myJSON.getString(WebRequestJSONKeys.STATUS.getLowerCase());
             if (!statusString.equalsIgnoreCase(GoogleMapsResponseStatusCodes.OK.toString())) {
                 // TODO: Find a way to let the calling algorithm handle this
                 // error
                 // Could even be as simple as checking for this error
                 addError(DirectionsStatus.NO_RESULTS_FOUND);
                 System.out.println(TAG + "buildRouteListFromJSONString()->Exiting this method.");
                 return null;
             } else if (statusString.equalsIgnoreCase(GoogleMapsResponseStatusCodes.OVER_QUERY_LIMIT
                     .toString())) {
                 addError(DirectionsStatus.OVER_QUERY_LIMIT);
                 System.out.println(TAG + "buildRouteListFromJSONString()->Exiting this method.");
                 return null;
             }
         } catch (JSONException e) {
             addError(DirectionsStatus.PARSING_ERROR);
             return null;
         }
 
         JSONArray routesArray;
         try {
             routesArray = myJSON.getJSONArray(WebRequestJSONKeys.ROUTES.getLowerCase());
             for (int i = 0; i < routesArray.length(); i++) {
                 Route currentRoute = Route.buildRouteFromJSON(routesArray.getJSONObject(i));
                 routesList.add(currentRoute);
             }
             System.err.println("JSON_TEST" + "Processed " + routesArray.length() + " routes!");
         } catch (JSONException e1) {
             addError(DirectionsStatus.PARSING_ERROR);
             System.out.println(TAG + "buildRouteListFromJSONString()->Exiting this method.");
             return null;
         }
 
         System.out.println(TAG + "buildRouteListFromJSONString()->Exiting this method.");
         return routesList;
     }
 
     /**
      * Retrieves transit directions between given start and end addresses.
      * 
      * @param startAddress
      *            describing the search to make
      * @param endAddress
      *            describing the search to make
      * @param routeTime
      *            departure or arrival time for search
      * @param timeMode
      *            denoting if searching by departure or arrival time
      * @throws UnsupportedEncodingException
      *             if there is a problem with the default charset
      * @return list of retrieved Routes
      */
     protected final List<Route> getTransitResults(final String startAddress, 
             final String endAddress,
             final long routeTime, 
             final TransitTimeMode timeMode)
             throws UnsupportedEncodingException {
 
         System.out.println(TAG + "getTransitResults()->startAddress: " + startAddress);
         System.out.println(TAG + "getTransitResults()->endAddress: " + endAddress);
         System.out.println(TAG + "getTransitResults()->routeTime: " + routeTime);
 
         // For preventing exceeding query request limit
         try {
             Thread.sleep(TRANSIT_QUERY_DELAY_MS);
         } catch (InterruptedException e) {
         	// This means the user is trying to cancel us!
         	addError(DirectionsStatus.USER_CANCELLED_REQUEST);
         	return null;
         	//System.err.println("Error delaying transit query request.");
         }
 
         String queryString = Utility.buildTransitQueryString(startAddress, endAddress, routeTime,
                 timeMode, true, queryLanguage);
 
         System.out.println(TAG + "getTransitResults()->queryString: " + queryString);
         // Fetch the query results
         String jsonResult = doQueryWithHandling(queryString);
 
         if (jsonResult != null) {
             // Parse the results
             List<Route> result = buildRouteListFromJSONString(jsonResult);
             return result;
         }
         return null;
     }
 
     /**
      * Retrieves bicycle directions between given start and end addresses.
      * 
      * @param startAddress
      *            describing the search to make
      * @param endAddress
      *            describing the search to make
      * @throws UnsupportedEncodingException
      *             if there is a problem with the default charset
      * @return list of retrieved Routes
      */
     protected final List<Route> getBicycleResults(final String startAddress, 
             final String endAddress)
             throws UnsupportedEncodingException {
         System.out.println(TAG + "getBicycleResults()->startAddress: " + startAddress);
         System.out.println(TAG + "getBicycleResults()->endAddress: " + endAddress);
         // Build the query string
         String queryString;
         queryString = Utility.buildBicycleQueryString(startAddress, endAddress, true,
                 queryLanguage);
 
         System.out.println(TAG + "getBicycleResults()->queryString: " + queryString);
         // Fetch the query results
         String jsonResult = doQueryWithHandling(queryString);
 
         if (jsonResult != null) {
             // Parse the results
             List<Route> result = buildRouteListFromJSONString(jsonResult);
             return result;
         }
         return null;
     }
 
     /**
      * Replaces all walking Steps within given Route (assumed to be a transit
      * Route) with bicycling directions Steps and re-queries API for all other
      * steps so that transit departure times align.
      * 
      * NOTE: this method only works for TransitTimeMode.DEPARTURE_TIME
      * 
      * @param transitRoute
      *            the transit Route for which walking Steps will be replaced
      * @param departureTime
      *            the departure time for given Route
      * @return given Route with walking Steps replaced by bicycling Steps return
      *         null if Route could not be generated
      */
     protected final Route replaceWalkingWithBicyclingDeparture(final Route transitRoute,
             final long departureTime) {
 
         System.out.println(TAG + "replaceWalkingWithBicyclingDeparture()->transitRoute: "
                 + transitRoute);
         System.out.println(TAG + "replaceWalkingWithBicyclingDeparture()->departureTime: "
                 + departureTime);
 
         Location curStretchStartLocation = null;
         long curStretchDepartTime = departureTime;
         Step prevStep = null;
 
         Route comboRoute = new Route();
 
         Step curStep;
         List<Leg> curLegs = transitRoute.getLegList();
         for (int l = 0; l < curLegs.size(); l++) {
             List<Step> curSteps = curLegs.get(l).getStepList();
             for (int s = 0; s <= curSteps.size(); s++) {
                 if (s == curSteps.size()) {
                     curStep = prevStep;
                 } else {
                     curStep = curSteps.get(s);
                 }
 
                 if (curStretchStartLocation == null) {
                     curStretchStartLocation = curStep.getStartLocation();
                 }
 
                 if ((l == (curLegs.size() - 1) && s == curSteps.size())
                         || (prevStep != null && !prevStep.getTravelMode().equals(
                                 curStep.getTravelMode()))) {
                     // we reached the [very] last step OR are switching travel
                     // modes
 
                     List<Route> subRoutes = null;
                     // NOTE: this is needed to deal with end-of-route edge case
                     TravelMode travelModeToCheck = prevStep.getTravelMode();
                     Location curStretchEndLocation = prevStep.getEndLocation();
                     if (l == (curLegs.size() - 1) && s == curSteps.size()) {
                         travelModeToCheck = curStep.getTravelMode();
                         curStretchEndLocation = curStep.getEndLocation();
                     }
 
                     if (travelModeToCheck.equals(TravelMode.WALKING)
                             || travelModeToCheck.equals(TravelMode.BICYCLING)) {
                         // ask for bicycling directions for prev stretch
                         try {
                             subRoutes = getBicycleResults(
                                     curStretchStartLocation.getLocationAsString(),
                                     curStretchEndLocation.getLocationAsString());
                         } catch (UnsupportedEncodingException e) {
                             addError(DirectionsStatus.UNSUPPORTED_CHARSET);
                         }
 
                     } else if (travelModeToCheck.equals(TravelMode.TRANSIT)) {
                         // ask for transit directions for prev stretch
                         try {
                             subRoutes = getTransitResults(
                                     curStretchStartLocation.getLocationAsString(),
                                     curStretchEndLocation.getLocationAsString(),
                                     curStretchDepartTime, TransitTimeMode.DEPARTURE_TIME);
 
                         } catch (UnsupportedEncodingException e) {
                             addError(DirectionsStatus.UNSUPPORTED_CHARSET);
                         }
                     } else {
                         System.err.println(TAG + "Unexpected travel mode ("
                                 + travelModeToCheck.toString() + ")");
                         // addError(...)
                         return null;
                     }
 
                     if (subRoutes == null || subRoutes.size() == 0) {
                         System.err.println(TAG + "No subroutes found");
                         return null;
                     } else {
                         // find a subRoute with no walking steps (this could be
                         // more efficient
                         // if it did not run this check for bicycling queries)
                         Route subRouteWithoutWalkingSteps = getRouteWithoutWalkingSteps(subRoutes);
                         if (subRouteWithoutWalkingSteps == null) {
                             System.err.println(TAG + "All sub routes unusable"
                                     + " (all contained walking steps).");
                             return null;
                         } else {
                             for (Leg newLeg : subRouteWithoutWalkingSteps.getLegList()) {
                                 comboRoute.addLeg(newLeg);
                             }
                             curStretchDepartTime += subRouteWithoutWalkingSteps
                                     .getDurationInSeconds();
                         }
                     }
                     curStretchStartLocation = curStep.getStartLocation();
                 }
                 prevStep = curStep;
             }
         }
         System.out.println(TAG + "replaceWalkingWithBicyclingDeparture()->comboRoute: "
                 + comboRoute);
         return comboRoute;
     }
 
     /**
      * Replaces all walking Steps within given Route (assumed to be a transit
      * Route) with bicycling directions Steps and re-queries API for all other
      * steps so that transit arrival times align.
      * 
      * NOTE: this method only works for TransitTimeMode.ARRIVAL_TIME
      * 
      * @param transitRoute
      *            the transit Route for which walking Steps will be replaced
      * @param arrivalTime
      *            the arrival time for given Route
      * @return given Route with walking Steps replaced by bicycling Steps return
      *         null if Route could not be generated
      */
     protected final Route replaceWalkingWithBicyclingArrival(final Route transitRoute,
             final long arrivalTime) {
         System.out.println(TAG + "replaceWalkingWithBicyclingArrival()->transitRoute: "
                 + transitRoute);
         System.out.println(TAG + "replaceWalkingWithBicyclingArrival()->arrivalTime: "
                 + arrivalTime);
 
         Location curStretchEndLocation = null;
         long curStretchArrivalTime = arrivalTime;
         Step prevStep = null;
 
         Route comboRoute = new Route();
 
         Step curStep;
         List<Leg> curLegs = transitRoute.getLegList();
         for (int l = curLegs.size() - 1; l >= 0; l--) {
             List<Step> curSteps = curLegs.get(l).getStepList();
             for (int s = curSteps.size() - 1; s >= -1; s--) {
                 if (s == -1) {
                     curStep = prevStep;
                 } else {
                     curStep = curSteps.get(s);
                 }
 
                 if (curStretchEndLocation == null) {
                     curStretchEndLocation = curStep.getEndLocation();
                 }
 
                 if ((l == 0 && s == -1)
                         || (prevStep != null && !prevStep.getTravelMode().equals(
                                 curStep.getTravelMode()))) {
                     // we reached the [very] last step OR are switching travel
                     // modes
 
                     List<Route> subRoutes = null;
                     // NOTE: this is needed to deal with end-of-route edge case
                     TravelMode travelModeToCheck = prevStep.getTravelMode();
                     Location curStretchStartLocation = prevStep.getStartLocation();
                     if (l == 0 && s == -1) {
                         travelModeToCheck = curStep.getTravelMode();
                         curStretchStartLocation = curStep.getStartLocation();
                     }
 
                     // if (!travelModeToCheck.equals(TravelMode.TRANSIT)) {
                     if (travelModeToCheck.equals(TravelMode.WALKING)
                             || travelModeToCheck.equals(TravelMode.BICYCLING)) {
                         // ask for bicycling directions for prev stretch
                         try {
                             subRoutes = getBicycleResults(
                                     curStretchStartLocation.getLocationAsString(),
                                     curStretchEndLocation.getLocationAsString());
                         } catch (UnsupportedEncodingException e) {
                             addError(DirectionsStatus.UNSUPPORTED_CHARSET);
                         }
 
                     } else if (travelModeToCheck.equals(TravelMode.TRANSIT)) {
                         // ask for transit directions for prev stretch
                         try {
                             subRoutes = getTransitResults(
                                     curStretchStartLocation.getLocationAsString(),
                                     curStretchEndLocation.getLocationAsString(),
                                     curStretchArrivalTime, TransitTimeMode.ARRIVAL_TIME);
 
                         } catch (UnsupportedEncodingException e) {
                             addError(DirectionsStatus.UNSUPPORTED_CHARSET);
                         }
                     } else {
                         System.err.println(TAG + "Unexpected travel mode ("
                                 + travelModeToCheck.toString() + ")");
                         // addError(...)
                         return null;
                     }
 
                     if (subRoutes == null || subRoutes.size() == 0) {
                         System.err.println(TAG + "No subroutes found");
                         return null;
                     } else {
                         // find a subRoute with no walking steps (this could be
                         // more efficient
                         // if it did not run this check for bicycling queries)
                         Route subRouteWithoutWalkingSteps = getRouteWithoutWalkingSteps(subRoutes);
                         if (subRouteWithoutWalkingSteps == null) {
                             System.err.println(TAG + "All sub routes unusable"
                                     + " (all contained walking steps).");
                             return null;
                         } else {
                             for (Leg newLeg : subRouteWithoutWalkingSteps.getLegList()) {
                                 comboRoute.addLegBeginning(newLeg);
                             }
                             curStretchArrivalTime -= subRouteWithoutWalkingSteps
                                     .getDurationInSeconds();
                         }
                     }
                     curStretchEndLocation = curStep.getEndLocation();
                 }
                 prevStep = curStep;
             }
         }
         System.out.println(TAG + "replaceWalkingWithBicyclingArrival()->comboRoute: " + comboRoute);
 
         return comboRoute;
     }
 
     /**
      * Returns the first route found with given list that does not contain any
      * walking steps, return null if all contain walking steps.
      * 
      * @param subRoutes
      *            list of routes
      * @return first route without walking steps, null if none found
      */
     protected final Route getRouteWithoutWalkingSteps(final List<Route> subRoutes) {
         System.out.println(TAG + "getRouteWithoutWalkingSteps()->Entering this method.");
         for (Route subRoute : subRoutes) {
             boolean subRouteHasWalkingSteps = false;
             for (Leg curLeg : subRoute.getLegList()) {
                 for (Step curStep : curLeg.getStepList()) {
                     if (curStep.getTravelMode().equals(TravelMode.WALKING)
                             && curStep.getDistanceInMeters() > 0) {
                         subRouteHasWalkingSteps = true;
                     }
                 }
             }
             if (!subRouteHasWalkingSteps) {
                 System.out.println(TAG + "getRouteWithoutWalkingSteps()->Exiting this method.");
                 return subRoute;
             }
         }
 
         System.out.println(TAG + "getRouteWithoutWalkingSteps()->Exiting this method "
                 + "(returning null).");
         return null;
     }
 }
