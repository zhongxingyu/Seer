 package fr.itinerennes;
 
 /**
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public final class ItineRennesConstants {
 
     /**
      * Private constructor to avoid instantiation.
      */
     private ItineRennesConstants() {
 
     }
 
     /*
      * Default config values.
      */
 
     /** Default zoom level. */
     public static final int CONFIG_DEFAULT_ZOOM = 16;
 
     /** Latitude of city Rennes. */
     public static final int CONFIG_RENNES_LAT = 48109600;
 
     /** Longitude of city Rennes. */
     public static final int CONFIG_RENNES_LON = -1679200;
 
     /** Nominatim search bounding box offset. */
     public static final int CONFIG_NOMINATIM_SEARCH_OFFSET = 150000;
 
     /** URL to get XML file describing Itinerennes versions. */
     public static final String ITINERENNES_VERSION_URL = "http://api.itinerennes.fr/android/version/";
 
     /**
      * Minimum zoom level displaying detailed overlay items. On higher zoom, a simple circle will be
      * displayed.
      */
     public static final int CONFIG_MINIMUM_ZOOM_ITEMS = 17;
 
     /**
      * Zoom level for zooming on a precise marker or location.
      */
     public static final int CONFIG_ZOOM_ON_LOCATION = 17;
 
     /*
      * Keolis constants.
      */
 
     /** Keolis API URL. */
     public static final String KEOLIS_API_URL = "http://data.keolis-rennes.com/json/";
 
     /** Key for Keolis API. */
     public static final String KEOLIS_API_KEY = "E6S9CADHA5XK4T4";
 
     /*
      * OneBusAway constants.
      */
     /** OneBusAway API URL. */
     public static final String OBA_API_URL = "http://api.itinerennes.fr/onebusaway-api-webapp/api/";
 
     /** OneBusAway API version. */
     public static final String OBA_API_KEY = "web";
 
     /*
      * Database constants.
      */
     /** The database schema version. */
    public static final int DATABASE_VERSION = 28;
 
     /** Duration of toast messages. */
     public static final int TOAST_DURATION = 5000;
 
     /*
      * Misc constants.
      */
     /** The APK download page URL. */
     public static final String DOWNLOAD_PAGE_URL = "https://bugtracker.dudie.fr/projects/itr-android/files";
 }
