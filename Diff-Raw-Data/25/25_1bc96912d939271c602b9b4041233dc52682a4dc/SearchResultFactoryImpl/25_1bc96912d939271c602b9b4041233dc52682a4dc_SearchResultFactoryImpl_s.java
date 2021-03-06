 /**
  * Copyright (c) 2011 Metropolitan Transportation Authority
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.onebusaway.nyc.webapp.actions.m;
 
 import org.onebusaway.nyc.geocoder.service.NycGeocoderResult;
 import org.onebusaway.nyc.presentation.model.SearchResult;
 import org.onebusaway.nyc.presentation.service.realtime.RealtimeService;
 import org.onebusaway.nyc.presentation.service.search.SearchResultFactory;
 import org.onebusaway.nyc.transit_data.services.NycTransitDataService;
 import org.onebusaway.nyc.transit_data_federation.siri.SiriDistanceExtension;
 import org.onebusaway.nyc.transit_data_federation.siri.SiriExtensionWrapper;
 import org.onebusaway.nyc.util.configuration.ConfigurationService;
 import org.onebusaway.nyc.webapp.actions.m.model.GeocodeResult;
 import org.onebusaway.nyc.webapp.actions.m.model.RouteAtStop;
 import org.onebusaway.nyc.webapp.actions.m.model.RouteDirection;
 import org.onebusaway.nyc.webapp.actions.m.model.RouteInRegionResult;
 import org.onebusaway.nyc.webapp.actions.m.model.RouteResult;
 import org.onebusaway.nyc.webapp.actions.m.model.StopOnRoute;
 import org.onebusaway.nyc.webapp.actions.m.model.StopResult;
 import org.onebusaway.transit_data.model.NameBean;
 import org.onebusaway.transit_data.model.RouteBean;
 import org.onebusaway.transit_data.model.StopBean;
 import org.onebusaway.transit_data.model.StopGroupBean;
 import org.onebusaway.transit_data.model.StopGroupingBean;
 import org.onebusaway.transit_data.model.StopsForRouteBean;
 import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
 import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
 
 import uk.org.siri.siri.MonitoredCallStructure;
 import uk.org.siri.siri.MonitoredStopVisitStructure;
 import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
 import uk.org.siri.siri.NaturalLanguageStringStructure;
 import uk.org.siri.siri.VehicleActivityStructure;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class SearchResultFactoryImpl implements SearchResultFactory {
 
   private ConfigurationService _configurationService;
 
   private RealtimeService _realtimeService;
 
   private NycTransitDataService _nycTransitDataService;
 
   public SearchResultFactoryImpl(NycTransitDataService nycTransitDataService, 
       RealtimeService realtimeService, ConfigurationService configurationService) {
     _nycTransitDataService = nycTransitDataService;
     _realtimeService = realtimeService;
     _configurationService = configurationService;
   }
 
   @Override
   public SearchResult getRouteResultForRegion(RouteBean routeBean) { 
     return new RouteInRegionResult(routeBean);
   }
   
   @Override
   public SearchResult getRouteResult(RouteBean routeBean) {    
     List<RouteDirection> directions = new ArrayList<RouteDirection>();
     
     StopsForRouteBean stopsForRoute = _nycTransitDataService.getStopsForRoute(routeBean.getId());
 
     // create stop ID->stop bean map
     Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
     for(StopBean stopBean : stopsForRoute.getStops()) {
       stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
     }  
 
     // add stops in both directions
     Map<String, List<String>> stopIdToDistanceAwayStringMap = getStopIdToDistanceAwayStringsListMapForRoute(routeBean);
 
     List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
     for (StopGroupingBean stopGroupingBean : stopGroupings) {
       for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
         NameBean name = stopGroupBean.getName();
         String type = name.getType();
 
         if (!type.equals("destination"))
           continue;
         
         // service in this direction
         Boolean hasUpcomingScheduledService = 
             _nycTransitDataService.routeHasUpcomingScheduledService(new Date(), routeBean.getId(), stopGroupBean.getId());
      
         // stops in this direction
         List<StopOnRoute> stopsOnRoute = null;
         if(!stopGroupBean.getStopIds().isEmpty()) {
           stopsOnRoute = new ArrayList<StopOnRoute>();
 
           for(String stopId : stopGroupBean.getStopIds()) {
             stopsOnRoute.add(new StopOnRoute(stopIdToStopBeanMap.get(stopId), stopIdToDistanceAwayStringMap.get(stopId)));
           }
         }
 
         directions.add(new RouteDirection(stopGroupBean, stopsOnRoute, hasUpcomingScheduledService, null));
       }
     }
 
     // service alerts in this direction
     Set<String> serviceAlertDescriptions = new HashSet<String>();
 
     List<ServiceAlertBean> serviceAlertBeans = _realtimeService.getServiceAlertsForRoute(routeBean.getId());
     for(ServiceAlertBean serviceAlertBean : serviceAlertBeans) {
       for(NaturalLanguageStringBean description : serviceAlertBean.getDescriptions()) {
         if(description.getValue() != null) {
           serviceAlertDescriptions.add(description.getValue().replace("\n", "<br/>"));
         }
       }
     }
     
     return new RouteResult(routeBean, directions, serviceAlertDescriptions);
   }
 
   @Override
   public SearchResult getStopResult(StopBean stopBean, Set<String> routeIdFilter) {
     List<RouteAtStop> routesWithArrivals = new ArrayList<RouteAtStop>();
     List<RouteAtStop> routesWithNoVehiclesEnRoute = new ArrayList<RouteAtStop>();
     List<RouteAtStop> routesWithNoScheduledService = new ArrayList<RouteAtStop>();
     
     Set<String> serviceAlertDescriptions = new HashSet<String>();
 
     for(RouteBean routeBean : stopBean.getRoutes()) {
       if(routeIdFilter != null && !routeIdFilter.isEmpty() && !routeIdFilter.contains(routeBean.getId())) {
           continue;
       }
       
       StopsForRouteBean stopsForRoute = _nycTransitDataService.getStopsForRoute(routeBean.getId());
       
       List<RouteDirection> directions = new ArrayList<RouteDirection>();
       List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
       for (StopGroupingBean stopGroupingBean : stopGroupings) {
         for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
           NameBean name = stopGroupBean.getName();
           String type = name.getType();
 
           if (!type.equals("destination"))
             continue;
         
           // filter out route directions that don't stop at this stop
           if(!stopGroupBean.getStopIds().contains(stopBean.getId()))
             continue;
 
           // arrivals in this direction
           List<String> arrivalsForRouteAndDirection = getDistanceAwayStringsForStopAndRouteAndDirection(stopBean, routeBean, stopGroupBean);
 
           // service alerts for this route + direction
           List<ServiceAlertBean> serviceAlertBeans = _realtimeService.getServiceAlertsForRouteAndDirection(routeBean.getId(), stopGroupBean.getId());
           for(ServiceAlertBean serviceAlertBean : serviceAlertBeans) {
             for(NaturalLanguageStringBean description : serviceAlertBean.getDescriptions()) {
               if(description.getValue() != null) {
                 serviceAlertDescriptions.add(description.getValue().replace("\n", "<br/>"));
               }
             }
           }
           
           // service in this direction
           Boolean hasUpcomingScheduledService = 
               _nycTransitDataService.stopHasUpcomingScheduledService(new Date(), stopBean.getId(), routeBean.getId(), stopGroupBean.getId());
 
           directions.add(new RouteDirection(stopGroupBean, null, hasUpcomingScheduledService, arrivalsForRouteAndDirection));
         }
       }
       
       RouteAtStop routeAtStop = new RouteAtStop(routeBean, directions, serviceAlertDescriptions);
 
       for(RouteDirection direction : routeAtStop.getDirections()) {
        if(direction.getHasUpcomingScheduledService() == false) {
           routesWithNoScheduledService.add(routeAtStop);
           break;
         } else {
          if(direction.getDistanceAways().size() > 0) {
             routesWithArrivals.add(routeAtStop);
             break;
           } else {
             routesWithNoVehiclesEnRoute.add(routeAtStop);
             break;
           }
         }
       }
     }
 
     return new StopResult(stopBean, routesWithArrivals, routesWithNoVehiclesEnRoute, routesWithNoScheduledService);
   }
 
   @Override
   public SearchResult getGeocoderResult(NycGeocoderResult geocodeResult, Set<String> routeShortNameFilter) {
     return new GeocodeResult(geocodeResult);   
   }
 
   /*** 
    * PRIVATE METHODS
    */
   private List<String> getDistanceAwayStringsForStopAndRouteAndDirection(StopBean stopBean, RouteBean routeBean, StopGroupBean stopGroupBean) {
     List<String> result = new ArrayList<String>();
 
     // stop visits
     List<MonitoredStopVisitStructure> visitList = 
       _realtimeService.getMonitoredStopVisitsForStop(stopBean.getId(), 0);  
     
     for(MonitoredStopVisitStructure visit : visitList) {
       String routeId = visit.getMonitoredVehicleJourney().getLineRef().getValue();
       if(!routeBean.getId().equals(routeId))
         continue;
 
       String directionId = visit.getMonitoredVehicleJourney().getDirectionRef().getValue();
       if(!stopGroupBean.getId().equals(directionId))
         continue;
 
       // on detour?
       MonitoredCallStructure monitoredCall = visit.getMonitoredVehicleJourney().getMonitoredCall();
       if(monitoredCall == null) {
         continue;
       }
 
       if(result.size() < 3) {
         result.add(getPresentableDistance(visit.getMonitoredVehicleJourney(), visit.getRecordedAtTime().getTime(), true));
       }
     }
     
     return result;
   }
   
   private Map<String, List<String>> getStopIdToDistanceAwayStringsListMapForRoute(RouteBean routeBean) {
     Map<String, List<String>> result = new HashMap<String, List<String>>();      
 
     // stop visits
     List<VehicleActivityStructure> journeyList = 
       _realtimeService.getVehicleActivityForRoute(routeBean.getId(), null, 0);
   
     // build map of stop IDs to list of distance strings
     for(VehicleActivityStructure journey : journeyList) {
       // on detour?
       MonitoredCallStructure monitoredCall = journey.getMonitoredVehicleJourney().getMonitoredCall();
       if(monitoredCall == null) {
         continue;
       }
 
       String stopId = monitoredCall.getStopPointRef().getValue();      
 
       List<String> distanceStrings = result.get(stopId);
       if(distanceStrings == null) {
         distanceStrings = new ArrayList<String>();
       }
       
       distanceStrings.add(getPresentableDistance(journey.getMonitoredVehicleJourney(), journey.getRecordedAtTime().getTime(), false));
       result.put(stopId,  distanceStrings);
     }
 
     return result;
   }
   
   private String getPresentableDistance(MonitoredVehicleJourneyStructure journey, long updateTime, boolean isStopContext) {
     MonitoredCallStructure monitoredCall = journey.getMonitoredCall();
     SiriExtensionWrapper wrapper = (SiriExtensionWrapper)monitoredCall.getExtensions().getAny();
     SiriDistanceExtension distanceExtension = wrapper.getDistances();    
     
     String message = "";
     String distance = distanceExtension.getPresentableDistance();
 
     // at terminal label only appears in stop results
     NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();
     if(isStopContext && progressStatus != null && progressStatus.getValue().equals("layover")) {
       message += "at terminal";
     }
     
     int staleTimeout = _configurationService.getConfigurationValueAsInteger("display.staleTimeout", 120);    
     long age = (System.currentTimeMillis() - updateTime) / 1000;
 
     if(age > staleTimeout) {
       if(message.length() > 0) {
         message += ", ";
       }
       
       message += "old data";
     }
 
     if(message.length() > 0)
       return distance + " (" + message + ")";
     else
       return distance;
   }
 }
