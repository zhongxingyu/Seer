 package org.onebusaway.nyc.presentation.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.onebusaway.geocoder.model.GeocoderResult;
 import org.onebusaway.geocoder.services.GeocoderService;
 import org.onebusaway.geospatial.model.CoordinateBounds;
 import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
 import org.onebusaway.nyc.presentation.model.search.RouteSearchResult;
 import org.onebusaway.nyc.presentation.model.search.SearchResult;
 import org.onebusaway.nyc.presentation.model.search.StopSearchResult;
 import org.onebusaway.nyc.presentation.service.NycSearchService;
 import org.onebusaway.nyc.presentation.model.FormattingContext;
 import org.onebusaway.nyc.presentation.model.RouteItem;
 import org.onebusaway.nyc.presentation.model.DistanceAway;
 import org.onebusaway.nyc.presentation.model.Mode;
 import org.onebusaway.nyc.presentation.model.StopItem;
 import org.onebusaway.nyc.presentation.service.ConfigurationBean;
 import org.onebusaway.nyc.presentation.service.NycConfigurationService;
 import org.onebusaway.presentation.services.ServiceAreaService;
 import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
 import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
 import org.onebusaway.transit_data.model.ListBean;
 import org.onebusaway.transit_data.model.NameBean;
 import org.onebusaway.transit_data.model.RouteBean;
 import org.onebusaway.transit_data.model.RoutesBean;
 import org.onebusaway.transit_data.model.SearchQueryBean;
 import org.onebusaway.transit_data.model.StopBean;
 import org.onebusaway.transit_data.model.StopGroupBean;
 import org.onebusaway.transit_data.model.StopGroupingBean;
 import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
 import org.onebusaway.transit_data.model.StopsBean;
 import org.onebusaway.transit_data.model.StopsForRouteBean;
 import org.onebusaway.transit_data.model.TripStopTimeBean;
 import org.onebusaway.transit_data.model.TripStopTimesBean;
 import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
 import org.onebusaway.transit_data.model.service_alerts.SituationBean;
 import org.onebusaway.transit_data.model.trips.TripBean;
 import org.onebusaway.transit_data.model.trips.TripDetailsBean;
 import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
 import org.onebusaway.transit_data.model.trips.TripStatusBean;
 import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
 import org.onebusaway.transit_data.services.TransitDataService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NycSearchServiceImpl implements NycSearchService {
   private final static Pattern routePattern = Pattern.compile(
       "(?:[BMQS]|BX)[0-9]+", Pattern.CASE_INSENSITIVE);
 
   // when querying for stops from a lat/lng, use this distance in meters
   private double distanceToStops = 100;
   
   private WebappIdParser idParser = new WebappIdParser();
 
   @Autowired
   private TransitDataService transitService;
 
   @Autowired
   private GeocoderService geocoderService;
 
   @Autowired
   private NycConfigurationService configurationService;
   
   @Autowired
   private ServiceAreaService serviceArea;
 
   private static final SearchResultComparator searchResultComparator = new SearchResultComparator();
 
   @Override
   public List<SearchResult> search(String q, Mode m) {
     List<SearchResult> results = new ArrayList<SearchResult>();
 
     if (q == null)
       return results;
 
     q = q.trim();
 
     if (q.isEmpty())
       return results;
 
     String[] fields = q.split(" ");
     // we are probably a query for a route or a stop ID
     if (fields.length == 1) {
       if (isStop(q)) {
         SearchQueryBean queryBean = makeSearchQuery(q);
         StopsBean stops = transitService.getStops(queryBean);
 
         for (StopBean stopBean : stops.getStops()) {
           StopSearchResult stopSearchResult = makeStopSearchResult(stopBean, m);
           results.add(stopSearchResult);
         }
 
       } else if (isRoute(q)) {
         SearchQueryBean queryBean = makeSearchQuery(q);
         RoutesBean routes = transitService.getRoutes(queryBean);
 
         for (RouteBean routeBean : routes.getRoutes()) {
           List<RouteSearchResult> routeSearchResult = makeRouteSearchResult(
               routeBean, m);
           results.addAll(routeSearchResult);
         }
       }
 
     // we should be either an intersection and route or an intersection
     } else {
       // if we're a route, set route and the geocoder query appropriately
       String route = null;
       String queryNoRoute = null;
 
       if (isRoute(fields[0])) {
         route = fields[0];
         StringBuilder sb = new StringBuilder(32);
 
         for (int i = 1; i < fields.length; i++) {
           sb.append(fields[i]);
           sb.append(" ");
         }
 
         queryNoRoute = sb.toString().trim();
 
       } else if (isRoute(fields[fields.length - 1])) {
         route = fields[fields.length - 1];
         StringBuilder sb = new StringBuilder(32);
 
         for (int i = 0; i < fields.length - 1; i++) {
           sb.append(fields[i]);
           sb.append(" ");
         }
 
         queryNoRoute = sb.toString().trim();
       }
 
       // we are a route and intersection
       if (route != null) {
         List<StopsBean> stopsList = fetchStopsFromGeocoder(queryNoRoute);
 
         for (StopsBean stopsBean : stopsList) {
           for (StopBean stopBean : stopsBean.getStops()) {
             boolean useStop = false;
 
             List<RouteBean> routes = stopBean.getRoutes();
             for (RouteBean routeBean : routes) {
               String idNoAgency = idParser.parseIdWithoutAgency(routeBean.getId());
 
               if (idNoAgency.equalsIgnoreCase(route)) {
                 useStop = true;
                 break;
               }
             }
 
             if (useStop) {
               StopSearchResult stopSearchResult = makeStopSearchResult(
                   stopBean, m);
               results.add(stopSearchResult);
             }
           }
         }
       } else {
         // try an intersection search
         List<StopsBean> stopsBeanList = fetchStopsFromGeocoder(q);
         for (StopsBean stopsBean : stopsBeanList) {
           List<StopBean> stopsList = stopsBean.getStops();
 
           for (StopBean stopBean : stopsList) {
             StopSearchResult stopSearchResult = makeStopSearchResult(stopBean,
                 m);
             results.add(stopSearchResult);
           }
         }
       }
     }
 
     return sortSearchResults(results);
   }
 
   private Map<String, List<StopBean>> createDirectionToStopBeansMap(String routeId) {
     Map<String, List<StopBean>> directionIdToStopBeans = new HashMap<String, List<StopBean>>();
 
     StopsForRouteBean stopsForRoute = transitService.getStopsForRoute(routeId);
     List<StopBean> stopBeansList = stopsForRoute.getStops();
 
     List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
     for (StopGroupingBean stopGroupingBean : stopGroupings) {
       List<StopGroupBean> stopGroups = stopGroupingBean.getStopGroups();
       for (StopGroupBean stopGroupBean : stopGroups) {
         NameBean name = stopGroupBean.getName();
         String type = name.getType();
 
         if (!type.equals("destination"))
           continue;
 
         List<StopBean> stopsForDirection = new ArrayList<StopBean>();
         String directionId = stopGroupBean.getId();
         Set<String> stopIds = new HashSet<String>(stopGroupBean.getStopIds());
 
         for (StopBean stopBean : stopBeansList) {
           String stopBeanId = stopBean.getId();
           if (stopIds.contains(stopBeanId))
             stopsForDirection.add(stopBean);
         }
 
         directionIdToStopBeans.put(directionId, stopsForDirection);
       }
     }
     return directionIdToStopBeans;
   }
 
   private TripsForRouteQueryBean makeTripsForRouteQueryBean(String routeId) {
     TripsForRouteQueryBean tripRouteQueryBean = new TripsForRouteQueryBean();
     tripRouteQueryBean.setRouteId(routeId);
     tripRouteQueryBean.setMaxCount(100);
     tripRouteQueryBean.setTime(new Date().getTime());
     TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
     inclusionBean.setIncludeTripBean(true);
     inclusionBean.setIncludeTripSchedule(true);
     inclusionBean.setIncludeTripStatus(true);
     tripRouteQueryBean.setInclusion(inclusionBean);
     return tripRouteQueryBean;
   }
 
   public boolean isStop(String s) {
     // stops are 6 digits
     int n = s.length();
     if (n != 6)
       return false;
     for (int i = 0; i < n; i++) {
       if (!Character.isDigit(s.charAt(i))) {
         return false;
       }
     }
     return true;
   }
 
   public boolean isRoute(String s) {
     Matcher matcher = routePattern.matcher(s);
     return matcher.matches();
   }
 
   private List<StopsBean> fetchStopsFromGeocoder(String q) {
     List<StopsBean> result = new ArrayList<StopsBean>();
 
     // FIXME HACK: append brooklyn to addresses to prevent manhattan adresses from being
     // returned instead--use google viewport biasing instead?
     if(q != null && q.isEmpty() == false) {
     	q = q + " brooklyn, ny";
     }
     
     List<GeocoderResult> geocoderResults = geocoderService.geocode(q).getResults();
     for (GeocoderResult geocoderResult : geocoderResults) {
       double lat = geocoderResult.getLatitude();
       double lng = geocoderResult.getLongitude();
       CoordinateBounds bounds = SphericalGeometryLibrary.bounds(lat, lng,
           distanceToStops);
 
       // and add any stops for it
       SearchQueryBean searchQueryBean = makeSearchQuery(bounds);
       searchQueryBean.setMaxCount(100);
       StopsBean stops = transitService.getStops(searchQueryBean);
       result.add(stops);
     }
     return result;
   }
 
   private List<RouteSearchResult> makeRouteSearchResult(RouteBean routeBean, Mode m) {
     ConfigurationBean config = configurationService.getConfiguration();  
 
     List<RouteSearchResult> results = new ArrayList<RouteSearchResult>();
 
     String routeId = routeBean.getId();
     String routeShortName = routeBean.getShortName();
     String routeLongName = routeBean.getLongName();
 
     // create lookups keyed by directionId to the different bits of
     // information we need for the response
     Map<String, List<StopBean>> directionIdToStopBeans = createDirectionToStopBeansMap(routeId);
     Map<String, String> directionIdToHeadsign = new HashMap<String, String>();
     Map<String, String> directionIdToTripId = new HashMap<String, String>();
     Map<String, Map<String, NaturalLanguageStringBean>> directionIdToServiceAlerts = 
     	new HashMap<String, Map<String, NaturalLanguageStringBean>>();
         
     Map<String, Map<String, Double>> tripIdToStopDistancesMap = new HashMap<String, Map<String, Double>>();
     Map<String, List<DistanceAway>> stopIdToDistanceAways = new HashMap<String, List<DistanceAway>>();
 
     TripsForRouteQueryBean tripQueryBean = makeTripsForRouteQueryBean(routeId);
     ListBean<TripDetailsBean> tripsForRoute = transitService.getTripsForRoute(tripQueryBean);
     List<TripDetailsBean> tripsList = tripsForRoute.getList();
     
     // create a map of trip ids->(stop ids->distances along trip values) to
     // figure out which stop the vehicle is between. This is all with schedule data only so far...
     for (TripDetailsBean tripDetailsBean : tripsList) {
       TripBean trip = tripDetailsBean.getTrip();
       String tripId = trip.getId();
       String tripDirectionId = trip.getDirectionId();
       String tripHeadsign = trip.getTripHeadsign();
 
       // FIXME: most common headsign? 
       directionIdToHeadsign.put(tripDirectionId, tripHeadsign);
       directionIdToTripId.put(tripDirectionId, tripId);
       
       if (!tripIdToStopDistancesMap.containsKey(tripId)) {
         TripStopTimesBean schedule = tripDetailsBean.getSchedule();
         List<TripStopTimeBean> stopTimes = schedule.getStopTimes();
 
         for (TripStopTimeBean tripStopTimeBean : stopTimes) {
           StopBean stopBean = tripStopTimeBean.getStop();
           String stopId = stopBean.getId();
           double distanceAlongTrip = tripStopTimeBean.getDistanceAlongTrip();
 
           Map<String, Double> stopIdToDistance = tripIdToStopDistancesMap.get(tripId);
 
           if (stopIdToDistance == null) {
             stopIdToDistance = new HashMap<String, Double>();
             tripIdToStopDistancesMap.put(tripId, stopIdToDistance);
           }
 
           stopIdToDistance.put(stopId, distanceAlongTrip);
         }
       }
 
       TripStatusBean tripStatusBean = tripDetailsBean.getStatus();
 
       // compile a list of service alerts that affect this route+direction
       Map<String, NaturalLanguageStringBean> serviceAlertIdsToNaturalLanguageStringBeans = directionIdToServiceAlerts.get(tripDirectionId);
 
       if (serviceAlertIdsToNaturalLanguageStringBeans == null) {
     	serviceAlertIdsToNaturalLanguageStringBeans = new HashMap<String, NaturalLanguageStringBean>();
       	directionIdToServiceAlerts.put(tripDirectionId, serviceAlertIdsToNaturalLanguageStringBeans);
       }
 
       if(tripStatusBean.getSituations() != null) {
     	  for(SituationBean situationBean : tripStatusBean.getSituations()) {
     		  NaturalLanguageStringBean serviceAlert = serviceAlertIdsToNaturalLanguageStringBeans.get(situationBean.getId());
     	  
     		  if (serviceAlert == null) {
     			  serviceAlertIdsToNaturalLanguageStringBeans.put(situationBean.getId(), situationBean.getDescription());
     		  }
     	  }     
       }
       
       // should we display this vehicle on the UI specified by "m"?
       if (tripStatusBean == null || !shouldDisplayTripForUIMode(tripStatusBean, m))
         continue;
       
       /*
        * Now that we have a structure of stops and each stop's distance along
        * the route, we can calculate how far the vehicle (for which we only have
        * distance along *route*) is away from its next stop in meters (which we
        * also have a distance along route for).
        */
       StopBean closestStop = tripStatusBean.getNextStop();
 
       if (closestStop != null) {
         String closestStopId = closestStop.getId();
         double distanceAwayFromClosestStopInMeters = tripStatusBean.getNextStopDistanceFromVehicle();
         int distanceAwayFromClosestStopInFeet = (int) this.metersToFeet(distanceAwayFromClosestStopInMeters);
 
         List<DistanceAway> stopDistanceAways = stopIdToDistanceAways.get(closestStopId);
 
         if (stopDistanceAways == null) {
           stopDistanceAways = new ArrayList<DistanceAway>();
           stopIdToDistanceAways.put(closestStopId, stopDistanceAways);
         }
 
         // (we're always 0 stops away from our next stop, by definition)
         DistanceAway distanceAway = new DistanceAway(0,
             distanceAwayFromClosestStopInFeet, 
             new Date(tripStatusBean.getLastUpdateTime()), 
             m, 
             config.getStaleDataTimeout(),
             FormattingContext.ROUTE,
             tripStatusBean);
 
         stopDistanceAways.add(distanceAway);
       }
     }
 
     // for each route direction for the route we are creating a result for...
     for (Map.Entry<String, List<StopBean>> directionStopBeansEntry : directionIdToStopBeans.entrySet()) {
       String directionId = directionStopBeansEntry.getKey();
       String tripId = directionIdToTripId.get(directionId);
       String tripHeadsign = directionIdToHeadsign.get(directionId);
 
       // on the off chance that this happens degrade more gracefully
       if (tripHeadsign == null)
     	 continue;
 
       // get list of stops for this route *direction*
       List<StopBean> stopBeansList = directionStopBeansEntry.getValue();
       Map<String, Double> stopIdToDistances = tripIdToStopDistancesMap.get(tripId);
 
       // (sort by order in route)
       if (stopIdToDistances != null) {
         Comparator<StopBean> stopBeanComparator = 
         	new NycSearchServiceImpl.StopBeanComparator(stopIdToDistances);
         Collections.sort(stopBeansList, stopBeanComparator);
       }
 
       // for each stop on the route, by order bus will stop there...
       List<StopItem> stopItemsList = new ArrayList<StopItem>();
 
       for (StopBean stopBean : stopBeansList) {
         String stopId = stopBean.getId();
         List<DistanceAway> distances = stopIdToDistanceAways.get(stopId);
 
         // if there is more than one vehicle stopping at this stop, sort the
         // vehicles by distance away from this stop
        if (distances != null && distances.size() > 0) {
          Collections.sort(distances);
        }
 
         StopItem stopItem = new StopItem(stopBean, distances);
         stopItemsList.add(stopItem);
       }
 
       // get a list of unique service alerts that affect this route direction
       List<NaturalLanguageStringBean> serviceAlerts = null;
       Map<String, NaturalLanguageStringBean> serviceAlertIdsToServiceAlerts = directionIdToServiceAlerts.get(directionId);
       if(serviceAlertIdsToServiceAlerts == null) {
     	  serviceAlerts = Collections.emptyList();
       } else {
     	  serviceAlerts = new ArrayList<NaturalLanguageStringBean>(serviceAlertIdsToServiceAlerts.values());
       }
       	
       RouteSearchResult routeSearchResult = new RouteSearchResult(routeId,
           routeShortName, routeLongName, tripHeadsign, directionId,
           stopItemsList, serviceAlerts);
 
       results.add(routeSearchResult);
     }
 
     return results;
   }
 
   private boolean shouldDisplayTripForUIMode(TripStatusBean statusBean, Mode m) {
     // UI states here:
     // https://spreadsheets.google.com/ccc?key=0Al2nqv1nCD71dGt5SkpHajRQZmdLaVZScnhoYVhiZWc&hl=en#gid=0
 
     // don't show non-realtime trips (row 8)
     if (statusBean == null 
     	|| statusBean.isPredicted() == false
        || Double.isNaN(statusBean.getDistanceAlongTrip()) || statusBean.getDistanceAlongTrip() == 0) {    
     	return false;
     }
     
     String status = statusBean.getStatus();
     String phase = statusBean.getPhase();
     
     // hide disabled vehicles (row 7)
     if(status != null && status.toLowerCase().compareTo("disabled") == 0) {
     	return false;
     }
 
 	// hide deadheading vehicles (except within a block) (row 3)
 	// hide vehicles at the depot (row 1)
     if (phase != null && phase.toLowerCase().compareTo("in_progress") != 0
     		&& phase.toLowerCase().compareTo("layover_before") != 0 
     		&& phase.toLowerCase().compareTo("layover_during") != 0) {
     	return false;
     }
 
     // hide deviated vehicles from mobile web + sms interfaces (row 4)
     if (m == Mode.MOBILE_WEB || m == Mode.SMS) {
       boolean routeIsOnDetour = false;
       
       List<SituationBean> situations = statusBean.getSituations();
       if (situations != null) {
         for (SituationBean situationBean : situations) {
           String miscelleanousReason = situationBean.getMiscellaneousReason();
     		  
           if (miscelleanousReason != null 
         	  && miscelleanousReason.compareTo("detour") == 0) {
             routeIsOnDetour = true;
             break;
           }
         }
       }
       
       if((status != null 
     		  && status.toLowerCase().compareTo("deviated") == 0) && ! routeIsOnDetour) {
         return false;
       }
     }
 
     // hide data >= (hide timeout) minutes old (row 5)
     ConfigurationBean config = configurationService.getConfiguration();
     
     if (new Date().getTime() - statusBean.getLastUpdateTime() >= 1000 * config.getHideTimeout()) {
        	return false;
     }
     
     return true;
   }
 
   private StopSearchResult makeStopSearchResult(StopBean stopBean, Mode m) {
     ConfigurationBean config = configurationService.getConfiguration();
 
 	String stopId = stopBean.getId();
     String stopName = stopBean.getName();
     String stopDirection = stopBean.getDirection();
     List<Double> latLng = Arrays.asList(new Double[] {stopBean.getLat(), stopBean.getLon()});
 
     Map<String, List<DistanceAway>> routeIdToDistanceAways = new HashMap<String, List<DistanceAway>>();
     Map<String, String> headsignToDirectionId = new HashMap<String, String>();
     Map<String, String> routeIdToHeadsign = new HashMap<String, String>();
     List<RouteItem> availableRoutes = new ArrayList<RouteItem>();
     Map<String, NaturalLanguageStringBean> serviceAlertIdsToServiceAlerts = new HashMap<String, NaturalLanguageStringBean>();
 
     ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
     query.setTime(System.currentTimeMillis());
     query.setMinutesBefore(60);
     query.setMinutesAfter(90);
     
     StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = 
     	transitService.getStopWithArrivalsAndDepartures(stopId, query);
     List<ArrivalAndDepartureBean> arrivalsAndDepartures = stopWithArrivalsAndDepartures.getArrivalsAndDepartures();
 
     for (ArrivalAndDepartureBean arrivalAndDepartureBean : arrivalsAndDepartures) {
       TripBean tripBean = arrivalAndDepartureBean.getTrip();
       TripStatusBean tripStatusBean = arrivalAndDepartureBean.getTripStatus();      
       String headsign = tripBean.getTripHeadsign();
       String routeId = tripBean.getRoute().getId();
       String directionId = tripBean.getDirectionId();
       
       if(routeId == null || headsign == null || directionId == null) {
     	  continue;
       }
       
       // FIXME: most common headsign?
       routeIdToHeadsign.put(routeId, headsign);
       headsignToDirectionId.put(headsign, directionId);
       
       // add service alerts to our list of service alerts for all routes at this stop
       if(arrivalAndDepartureBean.getSituations() != null) {
     	  for(SituationBean situationBean : arrivalAndDepartureBean.getSituations()) {
     		  NaturalLanguageStringBean serviceAlert = serviceAlertIdsToServiceAlerts.get(situationBean.getId());
       	
     		  if(serviceAlert == null)
     			  serviceAlertIdsToServiceAlerts.put(situationBean.getId(), situationBean.getDescription());
     	  }
       }
 
       // hide non-realtime arrivals and departures
       if(tripStatusBean == null || tripStatusBean.isPredicted() == false 
     		  || tripStatusBean.getVehicleId() == null || tripStatusBean.getVehicleId().equals("")) {
     	  continue;
       }
       
       System.out.println("A-D FOR STOP: VID=" + tripStatusBean.getVehicleId());
       
       // hide buses that left the stop recently
       if (arrivalAndDepartureBean.getDistanceFromStop() < 0) {
     	  System.out.println("   --- HIDING BECAUSE OF DIST. FROM STOP (" + arrivalAndDepartureBean.getDistanceFromStop() + ")");
     	  continue;
       }
       
       // hide arrivals are not the vehicle's current trip yet, except when in layover before or during state.
       if(tripBean != null && tripStatusBean != null) {
 		  TripBean currentTrip = tripStatusBean.getActiveTrip();
     	  
     	  if(currentTrip != null && !currentTrip.getId().equals(tripBean.getId())) {
     		  String phase = tripStatusBean.getPhase();
     		  
     		  if(phase != null && 
     				  !phase.toLowerCase().equals("layover_before") &&
     				  !phase.toLowerCase().equals("layover_during")) {
     			  
     			  System.out.println("   --- HIDING BECAUSE OF PHASE (" + phase + ")");
     			  continue;
     		  }
     	  }
       }
       
       // should we display this vehicle on the UI specified by "m"?
       if(! shouldDisplayTripForUIMode(arrivalAndDepartureBean.getTripStatus(), m)) {
     	  System.out.println("   --- HIDING BECAUSE OF FILTER FUNCTION");
     	  continue;
       }
       
       double distanceFromStopInMeters = arrivalAndDepartureBean.getDistanceFromStop();
       int distanceFromStopInFeet = (int) this.metersToFeet(distanceFromStopInMeters);
       int numberOfStopsAway = arrivalAndDepartureBean.getNumberOfStopsAway();
         
       System.out.println("   +++ ADDING TO ARRIVAL LIST");      
 
       DistanceAway distanceAway = new DistanceAway(numberOfStopsAway,
           distanceFromStopInFeet, 
           new Date(arrivalAndDepartureBean.getTripStatus().getLastUpdateTime()), 
           m, 
           config.getStaleDataTimeout(),
           FormattingContext.STOP,
           tripStatusBean);
 
       List<DistanceAway> distanceAways = routeIdToDistanceAways.get(routeId);
 
       if (distanceAways == null) {
         distanceAways = new ArrayList<DistanceAway>();
         routeIdToDistanceAways.put(routeId, distanceAways);
       }
 
       distanceAways.add(distanceAway);
     } // for arrivalanddeparture beans
 
     /*
      * Create AvailableRoute objects for each route at the given stop. Bring in
      * DistanceAway objects, if available, from above.
      */
     List<RouteBean> routes = stopBean.getRoutes();
 
     for (RouteBean routeBean : routes) {
       String routeId = routeBean.getId();
       String shortName = routeBean.getShortName();
       String longName = routeBean.getLongName();
       String headsign = routeIdToHeadsign.get(routeId);
 
       if (headsign == null)
         continue;
 
       List<DistanceAway> distanceAways = routeIdToDistanceAways.get(routeId);
 
       if (distanceAways == null)
         distanceAways = Collections.emptyList();
 
       Collections.sort(distanceAways);
 
       RouteItem availableRoute = new RouteItem(shortName, longName,
           headsign, headsignToDirectionId.get(headsign), distanceAways);
       availableRoutes.add(availableRoute);
     }
 
     // Make list of service alerts from our list of unique alerts that apply to routes at this stop
     List<NaturalLanguageStringBean> serviceAlerts = null;
     if(serviceAlertIdsToServiceAlerts.isEmpty()) {
   	  serviceAlerts = Collections.emptyList();
     } else {
   	  serviceAlerts = new ArrayList<NaturalLanguageStringBean>(serviceAlertIdsToServiceAlerts.values());
     }
     	
     StopSearchResult stopSearchResult = new StopSearchResult(stopId, stopName,
         latLng, stopDirection, availableRoutes, serviceAlerts);
 
     return stopSearchResult;
   }
 
   private double metersToFeet(double meters) {
     double feetInMeters = 3.28083989501312;
     return meters * feetInMeters;
   }
 
   private SearchQueryBean makeSearchQuery(String q) {
     return makeSearchQuery(q, serviceArea.getServiceArea());
   }
 
   private SearchQueryBean makeSearchQuery(CoordinateBounds bounds) {
     return makeSearchQuery(null, bounds);
   }
 
   private SearchQueryBean makeSearchQuery(String q, CoordinateBounds bounds) {
     SearchQueryBean queryBean = new SearchQueryBean();
     queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
     queryBean.setBounds(bounds);
     queryBean.setMaxCount(5);
     if (q != null)
       queryBean.setQuery(q);
     return queryBean;
   }
 
   private List<SearchResult> sortSearchResults(List<SearchResult> searchResults) {
     Collections.sort(searchResults, NycSearchServiceImpl.searchResultComparator);
     return searchResults;
   }
 
   public static class StopBeanComparator implements Comparator<StopBean> {
 
     private final Map<String, Double> stopIdToDistances;
 
     public StopBeanComparator(Map<String, Double> stopIdToDistances) {
       this.stopIdToDistances = stopIdToDistances;
     }
 
     @Override
     public int compare(StopBean o1, StopBean o2) {
       Double d1 = stopIdToDistances.get(o1.getId());
       Double d2 = stopIdToDistances.get(o2.getId());
       if (d1 == null)
         d1 = Double.valueOf(0);
       if (d2 == null)
         d2 = Double.valueOf(0);
       return d1.compareTo(d2);
     }
 
   }
 
   public static class SearchResultComparator implements
       Comparator<SearchResult> {
 
     @Override
     public int compare(SearchResult o1, SearchResult o2) {
       if ((o1 instanceof RouteSearchResult) && (o2 instanceof StopSearchResult))
         return -1;
       else if ((o1 instanceof StopSearchResult)
           && (o2 instanceof RouteSearchResult))
         return 1;
       else if ((o1 instanceof RouteSearchResult)
           && (o2 instanceof RouteSearchResult))
         return ((RouteSearchResult) o1).getName().compareTo(
             ((RouteSearchResult) o2).getName());
       else if ((o1 instanceof StopSearchResult)
           && (o2 instanceof StopSearchResult))
         return ((StopSearchResult) o1).getName().compareTo(
             ((StopSearchResult) o2).getName());
       else
         throw new IllegalStateException("Unknown search result types");
     }
   }
 }
