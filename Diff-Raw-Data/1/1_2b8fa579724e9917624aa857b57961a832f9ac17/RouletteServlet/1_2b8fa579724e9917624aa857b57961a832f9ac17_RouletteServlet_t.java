 package com.pixeltron.maproulette.servlets;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 import com.google.code.geocoder.Geocoder;
 import com.google.code.geocoder.GeocoderRequestBuilder;
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.GeocoderStatus;
 import com.google.code.geocoder.model.LatLng;
 import com.google.common.collect.Lists;
 import com.google.common.primitives.Ints;
 import com.google.gson.Gson;
 import com.pixeltron.maproulette.models.FoursquareApiRequestResponse;
 import com.pixeltron.maproulette.responses.WaypointResponse;
 
 import fi.foyt.foursquare.api.JSONFieldParser;
 import fi.foyt.foursquare.api.ResultMeta;
 import fi.foyt.foursquare.api.entities.CompactVenue;
 import fi.foyt.foursquare.api.entities.Recommendation;
 import fi.foyt.foursquare.api.entities.RecommendationGroup;
 import fi.foyt.foursquare.api.io.Response;
 
 @SuppressWarnings("serial")
 public class RouletteServlet extends HttpServlet {
 	
 	public static final String FOURSQUARE_API_KEY_DEV = "GWCCYYFINDKJ1A3JUY0KMUAEXX5UQ0EGHTQPPGUGLTVAKNUK";
 	public static final String FOURSQUARE_API_SECRET_DEV = "JYUTNCPVW4K0JLGFYS3ROLHHDEFPZOJSPP2R0RJHZBTOCQJO";
 	public static final String FOURSQUARE_API_KEY_PROD = "UMGTNRDSNZV2WY1TE5WWLSLMS1UAMH4YCYJFXHEPSKKXVHYA";
 	public static final String FOURSQUARE_API_SECRET_PROD = "FYO552JTH34WSCYK0OZUMVMZUHTNCTOB02CVCWRPYPADP1CC";
 	
 	private static int CONV_MI_LL = 69;              		// 69 miles = 1 latitude/longitude (average)
 	//private static double CONV_LL_MI = 0.000621371192;  	// conversion factor for lat/long to miles
 	private static int CONV_MI_M = 1760;             		// rough miles to meters
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {		
 		String start = req.getParameter("start");
 		String end = req.getParameter("end");
 		String categories = req.getParameter("categories");
 		String search = req.getParameter("search");
 		String checkNew = req.getParameter("new");
 		String checkOld = req.getParameter("old");
 		String oauth_token = req.getParameter("oauth_token");
 		String responseBody = "";
 		
 		Gson gson = new Gson();
 		WaypointResponse wayResp = new WaypointResponse();
 		
 		if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(end)) {
 			LatLng startLL = null;
 			LatLng endLL = null;
 			
 			try {
 				final Geocoder geocoder = new Geocoder();
 				GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(start).setLanguage("en").getGeocoderRequest();
 				GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
 				startLL = (geocoderResponse.getStatus().equals(GeocoderStatus.OK) ? geocoderResponse.getResults().get(0).getGeometry().getLocation() : null);
 				geocoderRequest = new GeocoderRequestBuilder().setAddress(end).setLanguage("en").getGeocoderRequest();
 				geocoderResponse = geocoder.geocode(geocoderRequest);
 				endLL = (geocoderResponse.getStatus().equals(GeocoderStatus.OK) ? geocoderResponse.getResults().get(0).getGeometry().getLocation() : null);
 			} catch (Exception e) {
 				e.printStackTrace();
 				wayResp.addError("Exception thrown during geocoding.");
 			}
 			
 			if (startLL != null && endLL != null) {
 				// Set up math variables
 				Random rand = new Random();
 				int numWaypoints = rand.nextInt(6) + 1;
 				
                 double rise = startLL.getLng().doubleValue() - endLL.getLng().doubleValue();
                 double run = startLL.getLat().doubleValue() - endLL.getLat().doubleValue();
                 double risestep = rise / numWaypoints;
                 double runstep = run / numWaypoints;
                 double distance = Math.sqrt(rise * rise + run * run) * CONV_MI_LL;
                 double wpDist = distance / numWaypoints;
                 if (numWaypoints == 0) wpDist = distance;
                 int rad = Ints.checkedCast(Math.round(wpDist * CONV_MI_M) / 2);
                 if (rad > 35000) rad = 35000;
                 
                 // Build waypoints
                 LatLng nextWP = new LatLng();
                 nextWP.setLat(BigDecimal.valueOf(startLL.getLat().doubleValue() - runstep));
                 nextWP.setLng(BigDecimal.valueOf(startLL.getLng().doubleValue() - risestep));
                 List<LatLng> waypoints = Lists.newArrayList();
                 for (int i=0;i<numWaypoints;i++) {
                     double lat = nextWP.getLat().doubleValue();
                     double lng = nextWP.getLng().doubleValue();
                     nextWP = new LatLng();
                     nextWP.setLat(BigDecimal.valueOf(lat - runstep));
                     nextWP.setLng(BigDecimal.valueOf(lng - risestep));
 
                     // Randomize lat/long
                     if (rand.nextDouble() > 0.5) {
                         lat = lat + (rand.nextDouble() * (runstep / 4));
                     } else {
                         lat = lat - (rand.nextDouble() * (runstep / 4));
                     }
                     if (Math.random() > 0.5) {
                         lng = lng + (rand.nextDouble() * (risestep / 4));
                     } else {
                         lng = lng - (rand.nextDouble() * (risestep / 4));
                     }
                     
                     LatLng curWP = new LatLng();
                     curWP.setLat(BigDecimal.valueOf(lat));
                     curWP.setLng(BigDecimal.valueOf(lng));
                     waypoints.add(curWP);
                 }
                 
                 List<HTTPResponse> responses = Lists.newArrayList();
                 for (LatLng waypoint : waypoints) {
                 	URLFetchService fetch = URLFetchServiceFactory.getURLFetchService();
                 	StringBuilder urlBuilder = new StringBuilder("https://api.foursquare.com/v2/venues/explore?ll=");
                 	urlBuilder.append(waypoint.toUrlValue());
                 	urlBuilder.append("&limit=6&client_id=GWCCYYFINDKJ1A3JUY0KMUAEXX5UQ0EGHTQPPGUGLTVAKNUK&client_secret=JYUTNCPVW4K0JLGFYS3ROLHHDEFPZOJSPP2R0RJHZBTOCQJO&v=20131013");
                 	if (StringUtils.isNotBlank(categories)) {
                 		urlBuilder.append("&section=");
                 		urlBuilder.append(categories);
                 	}
                 	if (StringUtils.isNotBlank(search)) {
                 		urlBuilder.append("&query=");
                 		urlBuilder.append(URLEncoder.encode(search, "UTF-8"));
                 	}
                 	urlBuilder.append("&radius=");
                 	urlBuilder.append(rad);
                 	if (StringUtils.isNotBlank(oauth_token)) {
                 		urlBuilder.append("&oauth_token=");
                 		urlBuilder.append(URLEncoder.encode(oauth_token, "UTF-8"));
                 		urlBuilder.append("&novelty=");
                 		if (StringUtils.isNotBlank(checkNew)) {
                 			if (StringUtils.isNotBlank(checkOld)) {
                     			urlBuilder.append("both");
                     		} else {
                     			urlBuilder.append(checkNew);
                     		}
                 		} else {
                 			urlBuilder.append(checkOld);
                 		}
                 		
                 	}
                 	URL reqUrl = new URL(urlBuilder.toString());
                 	HTTPRequest fsqreq = new HTTPRequest(reqUrl);
                 	try {
 						responses.add(fetch.fetchAsync(fsqreq).get());
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 					}
                 }
                 
                 List<RecommendationGroup> foursquareResults = Lists.newArrayList();
                 for (HTTPResponse fsqresp : responses) {
                 	FoursquareApiRequestResponse response = handleApiResponse(
 		                			new Response(new String(fsqresp.getContent(), "UTF-8"), 
 		                			fsqresp.getResponseCode(), 
 		                			null));
              	
                     if (response.getMeta().getCode() == 200) {
                     	try {
 							RecommendationGroup[] groups = (RecommendationGroup[]) JSONFieldParser.parseEntities(
 											RecommendationGroup.class, 
 											response.getResponse().getJSONArray("groups"), 
 											true);
 							if (groups.length > 0) {
 								if (groups[0].getItems().length > 0)
 									foursquareResults.add(groups[0]);
 							}
                     	} catch (Exception e) {
                     		e.printStackTrace();
                     	}
                     }
                 }
                 
                 List<CompactVenue> venueResults = Lists.newArrayList();
                 for (RecommendationGroup result : foursquareResults) {
                 		Recommendation[] venues = result.getItems();
                 		List<CompactVenue> venueData = Lists.newArrayList();
                 		for (Recommendation venue : venues) {
                 			venueData.add(venue.getVenue());
                 		}
                 		int random = rand.nextInt(venues.length);
                 		CompactVenue currentVenue = venueData.get(random);
                 		venueData.remove(random);
                 		while (venueResults.contains(currentVenue) && !venueData.isEmpty()) {
                 			random = rand.nextInt(venueData.size());
                 			currentVenue = venueData.get(random);
                 			venueData.remove(random);
                 		}
                 		venueResults.add(currentVenue);
                 }
                 
                 if (venueResults.size() > 0) {
                 	wayResp.setData(venueResults);
                 } else {
                 	wayResp.addError("Venue results was size 0");
                 }
 			} else {
 				wayResp.addError("Did not get valid start and end lat/lngs");
 			}
 		}
 		
 		wayResp.prepareForTransport();
 		responseBody = gson.toJson(wayResp);
		resp.setCharacterEncoding("UTF-8");
 		resp.getOutputStream().println(responseBody);
 	}
 
 	/**
 	 * Handles normal API request response
 	 * 
 	 * @param response raw response
 	 * @return ApiRequestResponse
 	 * @throws JSONException when JSON parsing error occurs
 	 */
 	private FoursquareApiRequestResponse handleApiResponse(Response response) throws JSONException {
 		JSONObject responseJson = null;
 		JSONArray notificationsJson = null;
 		String errorDetail = null;
 		if (response.getResponseCode() == 200) {
 			JSONObject responseObject = new JSONObject(response.getResponseContent());
 			responseJson = responseObject.getJSONObject("response");
 			notificationsJson = responseObject.optJSONArray("notifications");
 		} else {
 		  errorDetail = response.getMessage();
 		}
 	
 		return new FoursquareApiRequestResponse(new ResultMeta(response.getResponseCode(), "", errorDetail), responseJson, notificationsJson);
 	}
 }
 
