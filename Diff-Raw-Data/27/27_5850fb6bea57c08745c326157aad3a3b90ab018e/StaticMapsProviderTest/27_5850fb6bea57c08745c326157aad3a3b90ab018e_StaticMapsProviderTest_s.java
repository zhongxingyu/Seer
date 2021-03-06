 package cgeo.geocaching;
 
 import cgeo.geocaching.enumerations.WaypointType;
 import cgeo.geocaching.files.LocalStorage;
 import cgeo.geocaching.geopoint.Geopoint;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 public class StaticMapsProviderTest extends TestCase {
 
     public static void testDownloadStaticMaps() {
         final double lat = 52.354176d;
         final double lon = 9.745685d;
         String geocode = "GCTEST1";
 
         boolean backupStoreWP = Settings.isStoreOfflineWpMaps();
         Settings.setStoreOfflineMaps(true);
         try {
             Geopoint gp = new Geopoint(lat + 0.25d, lon + 0.25d);
             Geocache cache = new Geocache();
             cache.setGeocode(geocode);
             cache.setCoords(gp);
             cache.setCacheId(String.valueOf(1));
 
             Waypoint theFinal = new Waypoint("Final", WaypointType.FINAL, false);
             Geopoint finalGp = new Geopoint(lat + 0.25d + 1, lon + 0.25d + 1);
             theFinal.setCoords(finalGp);
             theFinal.setId(1);
             cache.addOrChangeWaypoint(theFinal, false);
 
             Waypoint trailhead = new Waypoint("Trail head", WaypointType.TRAILHEAD, false);
             Geopoint trailheadGp = new Geopoint(lat + 0.25d + 2, lon + 0.25d + 2);
             trailhead.setCoords(trailheadGp);
             trailhead.setId(2);
             cache.addOrChangeWaypoint(trailhead, false);
 
             StaticMapsProvider.downloadMaps(cache);
 
             try {
                 Thread.sleep(10000);
             } catch (InterruptedException e) {
                 fail();
             }
             assertTrue(StaticMapsProvider.hasStaticMap(cache));
             assertTrue(StaticMapsProvider.hasStaticMapForWaypoint(geocode, theFinal));
             assertTrue(StaticMapsProvider.hasStaticMapForWaypoint(geocode, trailhead));
 
             // waypoint static maps hashcode dependent
             trailhead.setCoords(new Geopoint(lat + 0.24d + 2, lon + 0.25d + 2));
             assertFalse(StaticMapsProvider.hasStaticMapForWaypoint(geocode, trailhead));
         } finally {
             Settings.setStoreOfflineWpMaps(backupStoreWP);
            File cacheDir = LocalStorage.getStorageDir(geocode);
            if (!cacheDir.delete()) {
                cacheDir.deleteOnExit();
            }
         }
     }
 
 }
