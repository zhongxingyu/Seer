 package com.HuskySoft.metrobike.algorithm;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import com.HuskySoft.metrobike.backend.DirectionsRequest.RequestParameters;
 import com.HuskySoft.metrobike.backend.DirectionsStatus;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.TravelMode;
 import com.HuskySoft.metrobike.backend.Utility;
 
 /**
  * The simplest algorithm, this class simply asks Google for multiple Routes and
  * returns all of them.
  * 
  * @author dutchscout
  */
 public final class BicycleOnlyAlgorithm extends AlgorithmWorker {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public DirectionsStatus findRoutes(final RequestParameters toProcess) {
 		clearErrors();
 
 		try {
 			// Get only bicycle routes, no matter what.
			if (toProcess.getTravelMode() == TravelMode.BICYCLING
					|| toProcess.getTravelMode() == TravelMode.TRANSIT
					|| toProcess.getTravelMode() == TravelMode.MIXED) {
 				addBicycleResults(toProcess);
 			} else {
 				return addError(DirectionsStatus.UNSUPPORTED_TRAVEL_MODE_ERROR, ": "
 						+ toProcess.getTravelMode().toString());
 			}
 		} catch (UnsupportedEncodingException e) {
 			return addError(DirectionsStatus.UNSUPPORTED_CHARSET);
 		}
 
 		// If we got no results, return the appropriate status code
 		if (getResults() == null || getResults().size() == 0) {
 			if (!hasErrors()) {
 				// If we didn't notice not getting results somehow, add this
 				// error manually.
 				addError(DirectionsStatus.NO_RESULTS_FOUND);
 			}
 			return getMostRecentStatus();
 		}
 
 		return markSuccessful();
 	}
 
 	/**
 	 * Retrieves bicycle directions and adds these to the overall algorithm
 	 * results.
 	 * 
 	 * @param toProcess
 	 *            the RequestParameters object describing the search to make
 	 * @throws UnsupportedEncodingException
 	 *             if there is a problem with the default charset
 	 */
 	private void addBicycleResults(final RequestParameters toProcess)
 			throws UnsupportedEncodingException {
 		// Build the query string
 		String queryString;
 		queryString = Utility.buildBicycleQueryString(
 				toProcess.getStartAddress(), toProcess.getEndAddress(), true);
 
 		// Fetch the query results
 		String jsonResult = doQueryWithHandling(queryString);
 
 		if (jsonResult != null) {
 			// Parse the results
 			List<Route> result = buildRouteListFromJSONString(jsonResult);
 			if (result != null) {
 				// Add the results
 				addResults(result);
 			}
 		}
 	}
 }
