 package org.onebusaway.transit_data_federation.impl.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.onebusaway.gtfs.model.AgencyAndId;
 import org.onebusaway.realtime.api.EVehiclePhase;
 import org.onebusaway.transit_data.model.ListBean;
 import org.onebusaway.transit_data.model.StopBean;
 import org.onebusaway.transit_data.model.TripStopTimesBean;
 import org.onebusaway.transit_data.model.schedule.FrequencyBean;
 import org.onebusaway.transit_data.model.service_alerts.SituationBean;
 import org.onebusaway.transit_data.model.trips.TripBean;
 import org.onebusaway.transit_data.model.trips.TripDetailsBean;
 import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
 import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
 import org.onebusaway.transit_data.model.trips.TripStatusBean;
 import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
 import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
 import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
 import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
 import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
 import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
 import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
 import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
 import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
 import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
 import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
 import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
 import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
 import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
 import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
 import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class TripStatusBeanServiceImpl implements TripDetailsBeanService {
 
   private static Logger _log = LoggerFactory.getLogger(TripStatusBeanServiceImpl.class);
 
   private TransitGraphDao _transitGraphDao;
 
   private BlockCalendarService _blockCalendarService;
 
   private BlockStatusService _blockStatusService;
 
   private TripBeanService _tripBeanService;
 
   private TripStopTimesBeanService _tripStopTimesBeanService;
 
   private StopBeanService _stopBeanService;
 
   private ServiceAlertsBeanService _serviceAlertBeanService;
 
   @Autowired
   public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
     _transitGraphDao = transitGraphDao;
   }
 
   @Autowired
   public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
     _blockCalendarService = blockCalendarService;
   }
 
   @Autowired
   public void setBlockStatusService(BlockStatusService blockStatusService) {
     _blockStatusService = blockStatusService;
   }
 
   @Autowired
   public void setTripBeanService(TripBeanService tripBeanService) {
     _tripBeanService = tripBeanService;
   }
 
   @Autowired
   public void setTripStopTimesBeanService(
       TripStopTimesBeanService tripStopTimesBeanService) {
     _tripStopTimesBeanService = tripStopTimesBeanService;
   }
 
   @Autowired
   public void setStopBeanService(StopBeanService stopBeanService) {
     _stopBeanService = stopBeanService;
   }
 
   @Autowired
   public void setServiceAlertBeanService(
       ServiceAlertsBeanService serviceAlertBeanService) {
     _serviceAlertBeanService = serviceAlertBeanService;
   }
 
   /****
    * {@link TripStatusBeanService} Interface
    ****/
 
   @Override
   public TripDetailsBean getTripForId(TripDetailsQueryBean query) {
 
     ListBean<TripDetailsBean> listBean = getTripsForId(query);
     List<TripDetailsBean> trips = listBean.getList();
 
    if (trips.isEmpty()) {
       return null;
     } else if (trips.size() == 1) {
       return trips.get(0);
     } else {
       // Be smarter here?
       return trips.get(0);
     }
   }
 
   @Override
   public ListBean<TripDetailsBean> getTripsForId(TripDetailsQueryBean query) {
 
     AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
     long serviceDate = query.getServiceDate();
     AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(query.getVehicleId());
     long time = query.getTime();
 
     TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
     if (tripEntry == null)
       return new ListBean<TripDetailsBean>();
 
     Map<BlockInstance, List<BlockLocation>> locationsByInstance = _blockStatusService.getBlocks(
         tripEntry.getBlock().getId(), serviceDate, vehicleId, time);
 
     List<TripDetailsBean> tripDetails = new ArrayList<TripDetailsBean>();
 
     for (Map.Entry<BlockInstance, List<BlockLocation>> entry : locationsByInstance.entrySet()) {
 
       BlockInstance blockInstance = entry.getKey();
       List<BlockLocation> locations = entry.getValue();
 
       BlockTripEntry targeBlockTripEntry = _blockCalendarService.getTargetBlockTrip(
           blockInstance, tripEntry);
 
       if (targeBlockTripEntry == null)
         throw new IllegalStateException("expected blockTrip for trip="
             + tripEntry + " and block=" + blockInstance);
 
       /**
        * If we have no locations for the specified block instance, it means the
        * block is not currently active. But we can still attempt to construct a
        * trip details
        */
       if (locations.isEmpty()) {
         TripDetailsBean details = getTripEntryAndBlockLocationAsTripDetails(
             targeBlockTripEntry, blockInstance, null, query.getInclusion(),
             time);
         tripDetails.add(details);
       } else {
         for (BlockLocation location : locations) {
           TripDetailsBean details = getBlockLocationAsTripDetails(
               targeBlockTripEntry, location, query.getInclusion(), time);
           tripDetails.add(details);
         }
       }
     }
     return new ListBean<TripDetailsBean>(tripDetails, false);
   }
 
   @Override
   public TripDetailsBean getTripForVehicle(AgencyAndId vehicleId, long time,
       TripDetailsInclusionBean inclusion) {
 
     BlockLocation blockLocation = _blockStatusService.getBlockForVehicle(
         vehicleId, time);
     if (blockLocation == null)
       return null;
     return getBlockLocationAsTripDetails(blockLocation.getActiveTrip(),
         blockLocation, inclusion, time);
   }
 
   @Override
   public ListBean<TripDetailsBean> getTripsForBounds(
       TripsForBoundsQueryBean query) {
     List<BlockLocation> locations = _blockStatusService.getBlocksForBounds(
         query.getBounds(), query.getTime());
     return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
         query.getTime());
   }
 
   @Override
   public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
     AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(query.getRouteId());
     List<BlockLocation> locations = _blockStatusService.getBlocksForRoute(
         routeId, query.getTime());
     return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
         query.getTime());
   }
 
   @Override
   public ListBean<TripDetailsBean> getTripsForAgency(
       TripsForAgencyQueryBean query) {
     List<BlockLocation> locations = _blockStatusService.getBlocksForAgency(
         query.getAgencyId(), query.getTime());
     return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
         query.getTime());
   }
 
   @Override
   public TripStatusBean getBlockLocationAsStatusBean(
       BlockLocation blockLocation, long time) {
 
     TripStatusBean bean = new TripStatusBean();
     bean.setStatus("default");
 
     BlockInstance blockInstance = blockLocation.getBlockInstance();
     long serviceDate = blockInstance.getServiceDate();
 
     bean.setServiceDate(serviceDate);
 
     FrequencyEntry frequency = blockInstance.getFrequency();
 
     if (frequency != null) {
       FrequencyBean fb = FrequencyBeanLibrary.getBeanForFrequency(serviceDate,
           frequency);
       bean.setFrequency(fb);
     }
 
     bean.setLastUpdateTime(blockLocation.getLastUpdateTime());
     bean.setLastLocationUpdateTime(blockLocation.getLastLocationUpdateTime());
 
     bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
     bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());
 
     bean.setLocation(blockLocation.getLocation());
     bean.setOrientation(blockLocation.getOrientation());
 
     bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
     if (blockLocation.isLastKnownOrientationSet())
       bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());
 
     bean.setScheduleDeviation(blockLocation.getScheduleDeviation());
 
     BlockTripEntry activeBlockTrip = blockLocation.getActiveTrip();
 
     if (activeBlockTrip != null) {
       bean.setScheduledDistanceAlongTrip(blockLocation.getScheduledDistanceAlongBlock()
           - activeBlockTrip.getDistanceAlongBlock());
       bean.setDistanceAlongTrip(blockLocation.getDistanceAlongBlock()
           - activeBlockTrip.getDistanceAlongBlock());
       TripEntry activeTrip = activeBlockTrip.getTrip();
       bean.setTotalDistanceAlongTrip(activeTrip.getTotalTripDistance());
 
       TripBean activeTripBean = _tripBeanService.getTripForId(activeTrip.getId());
       bean.setActiveTrip(activeTripBean);
       bean.setBlockTripSequence(activeBlockTrip.getSequence());
 
       if (blockLocation.isLastKnownDistanceAlongBlockSet()) {
         bean.setLastKnownDistanceAlongTrip(blockLocation.getLastKnownDistanceAlongBlock()
             - activeBlockTrip.getDistanceAlongBlock());
       }
 
     } else {
       _log.warn("no active block trip for block location: blockInstance="
           + blockLocation.getBlockInstance() + " time=" + time);
     }
 
     BlockStopTimeEntry closestStop = blockLocation.getClosestStop();
     if (closestStop != null) {
       StopTimeEntry stopTime = closestStop.getStopTime();
       StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId());
       bean.setClosestStop(stopBean);
       bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
     }
 
     BlockStopTimeEntry nextStop = blockLocation.getNextStop();
     if (nextStop != null) {
       StopTimeEntry stopTime = nextStop.getStopTime();
       StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId());
       bean.setNextStop(stopBean);
       bean.setNextStopTimeOffset(blockLocation.getNextStopTimeOffset());
       bean.setNextStopDistanceFromVehicle(blockLocation.getNextStop().getDistanceAlongBlock()
           - blockLocation.getDistanceAlongBlock());
     }
 
     EVehiclePhase phase = blockLocation.getPhase();
     if (phase != null)
       bean.setPhase(phase.toLabel());
 
     String status = blockLocation.getStatus();
     if (status != null)
       bean.setStatus(status);
 
     bean.setPredicted(blockLocation.isPredicted());
 
     AgencyAndId vid = blockLocation.getVehicleId();
     if (vid != null)
       bean.setVehicleId(ApplicationBeanLibrary.getId(vid));
 
     if (activeBlockTrip != null) {
       List<SituationBean> situations = _serviceAlertBeanService.getSituationsForVehicleJourney(
           time, blockInstance, activeBlockTrip, blockLocation.getVehicleId());
       if (!situations.isEmpty())
         bean.setSituations(situations);
     }
 
     return bean;
   }
 
   /****
    * Private Methods
    ****/
 
   private ListBean<TripDetailsBean> getBlockLocationsAsTripDetails(
       List<BlockLocation> locations, TripDetailsInclusionBean inclusion,
       long time) {
     List<TripDetailsBean> tripDetails = new ArrayList<TripDetailsBean>();
     for (BlockLocation location : locations) {
       TripDetailsBean details = getBlockLocationAsTripDetails(
           location.getActiveTrip(), location, inclusion, time);
       tripDetails.add(details);
     }
     return new ListBean<TripDetailsBean>(tripDetails, false);
   }
 
   private TripDetailsBean getBlockLocationAsTripDetails(
       BlockTripEntry targetBlockTrip, BlockLocation blockLocation,
       TripDetailsInclusionBean inclusion, long time) {
 
     if (targetBlockTrip == null || blockLocation == null)
       return null;
 
     return getTripEntryAndBlockLocationAsTripDetails(targetBlockTrip,
         blockLocation.getBlockInstance(), blockLocation, inclusion, time);
   }
 
   private TripDetailsBean getTripEntryAndBlockLocationAsTripDetails(
       BlockTripEntry blockTripEntry, BlockInstance blockInstance,
       BlockLocation blockLocation, TripDetailsInclusionBean inclusion, long time) {
 
     TripBean trip = null;
     long serviceDate = blockInstance.getServiceDate();
     FrequencyBean frequency = null;
     TripStopTimesBean stopTimes = null;
     TripStatusBean status = null;
     AgencyAndId vehicleId = null;
 
     boolean missing = false;
 
     if (blockInstance.getFrequency() != null)
       frequency = FrequencyBeanLibrary.getBeanForFrequency(serviceDate,
           blockInstance.getFrequency());
 
     TripEntry tripEntry = blockTripEntry.getTrip();
 
     if (inclusion.isIncludeTripBean()) {
       trip = _tripBeanService.getTripForId(tripEntry.getId());
       if (trip == null)
         missing = true;
     }
 
     if (inclusion.isIncludeTripSchedule()) {
 
       stopTimes = _tripStopTimesBeanService.getStopTimesForBlockTrip(
           blockInstance, blockTripEntry);
 
       if (stopTimes == null)
         missing = true;
     }
 
     if (inclusion.isIncludeTripStatus() && blockLocation != null) {
       status = getBlockLocationAsStatusBean(blockLocation, time);
       if (status == null)
         missing = true;
       else
         vehicleId = AgencyAndIdLibrary.convertFromString(status.getVehicleId());
     }
 
     List<SituationBean> situations = _serviceAlertBeanService.getSituationsForVehicleJourney(
         time, blockInstance, blockTripEntry, vehicleId);
 
     if (missing)
       return null;
 
     String tripId = AgencyAndIdLibrary.convertToString(tripEntry.getId());
 
     TripDetailsBean bean = new TripDetailsBean();
     bean.setTripId(tripId);
     bean.setServiceDate(serviceDate);
     bean.setFrequency(frequency);
     bean.setTrip(trip);
     bean.setSchedule(stopTimes);
     bean.setStatus(status);
     bean.setSituations(situations);
     return bean;
   }
 }
