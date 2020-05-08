 package fr.itinerennes.business.service;
 
 import java.util.List;
 
 import org.andnav.osm.util.BoundingBoxE6;
 import org.slf4j.Logger;
 import org.slf4j.impl.ItinerennesLoggerFactory;
 
 import fr.itinerennes.business.cache.BusStationCacheEntryHandler;
 import fr.itinerennes.business.cache.CacheProvider;
 import fr.itinerennes.business.cache.CacheProvider.CacheEntry;
 import fr.itinerennes.business.cache.GeoCacheProvider;
 import fr.itinerennes.business.http.wfs.WFSService;
 import fr.itinerennes.database.DatabaseHelper;
 import fr.itinerennes.exceptions.GenericException;
 import fr.itinerennes.model.BusStation;
 
 /**
  * Service to consult informations about the bus transport service.
  * <p>
  * Every method call is cached using the {@link CacheProvider} and {@link GeoCacheProvider}.
  * 
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public class BusService extends AbstractService implements StationProvider<BusStation> {
 
     /** The event logger. */
     private static final Logger LOGGER = ItinerennesLoggerFactory.getLogger(BusService.class);
 
     /** The cache for bus stations. */
     private final CacheProvider<BusStation> busCache;
 
     /** The geo cache. */
     private final GeoCacheProvider geoCache;
 
     /** The WFS service. */
     private final WFSService wfsService;
 
     /**
      * Creates a bike service.
      * 
      * @param dbHelper
      *            the database helper
      */
     public BusService(final DatabaseHelper dbHelper) {
 
         super(dbHelper);
         wfsService = new WFSService();
         busCache = new CacheProvider<BusStation>(dbHelper,
                 new BusStationCacheEntryHandler(dbHelper));
         geoCache = GeoCacheProvider.getInstance(dbHelper);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.business.service.StationProvider#getStation(java.lang.String)
      */
     @Override
     public final BusStation getStation(final String id) throws GenericException {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getStations.start - id={}", id);
         }
         BusStation station = null;
         final CacheEntry<BusStation> cachedEntry = busCache.load(id);
         if (cachedEntry == null
                 || fr.itinerennes.utils.DateUtils.isExpired(cachedEntry.getLastUpdate(),
                         BusStation.TTL)) {
             station = wfsService.getBusStation(id);
             busCache.replace(station);
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getStations.start - stationNotNull={}", station != null);
         }
         return station;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.business.service.StationProvider#getStations(org.andnav.osm.util.BoundingBoxE6)
      */
     @Override
     public final List<BusStation> getStations(final BoundingBoxE6 bbox) throws GenericException {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getStations.start - bbox={}", bbox);
         }
 
         final List<BusStation> stations;
 
         final BoundingBoxE6 normalizedBbox = GeoCacheProvider.normalize(bbox);
         if (geoCache.isExplored(normalizedBbox, BusStation.class.getName())) {
             stations = CacheEntry.values(busCache.load(bbox));
         } else {
             stations = wfsService.getBusStationsFromBbox(normalizedBbox);
 
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("caching {} bus stations", null != stations ? stations.size() : 0);
             }
 
             busCache.replace(stations);
 
             geoCache.markExplored(normalizedBbox, BusStation.class.getName());
         }
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getStations.end - {} stations", null != stations ? stations.size() : 0);
         }
         return stations;
     }
 
 }
