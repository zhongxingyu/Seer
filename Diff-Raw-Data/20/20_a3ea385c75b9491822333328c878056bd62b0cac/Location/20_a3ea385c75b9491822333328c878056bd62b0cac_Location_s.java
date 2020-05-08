 package net.es.topology.common.records.ts;
 
 import net.es.topology.common.records.ts.keys.ReservedKeys;
 
 /**
  * A Location is a reference to a geographical location or area.
  * A Location object can be related to other Network Objects to describe that these are located there.
  *
  * @author <a href="mailto:a.hassany@gmail.com">Ahmed El-Hassany</a>
  */
 public class Location {
     NetworkObject networkObject = null;
 
     public Location(NetworkObject networkObject) {
         this.networkObject = networkObject;
     }
 
     /**
      * Get the NetworkObject that this location object is wrapped around
      *
      * @return
      */
     public NetworkObject getNetworkObject() {
         return this.networkObject;
     }
 
     /**
      * Set the NetworkObject that this location object is wrapped around
      *
      * @param networkObject
      */
     public void setNetworkObject(NetworkObject networkObject) {
         this.networkObject = networkObject;
     }
 
     /**
      * A human readable string
      *
      * @return
      */
     public String getName() {
         return this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_NAME));
     }
 
     /**
      * A human readable string
      *
      * @param name
      */
     public void setName(String name) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_NAME, name);
     }
 
     /**
      * Longitude in WGS84 coordinate system (in decimal degrees)
      *
      * @return
      */
     public Float getLongitude() {
        return Float.valueOf(this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_LONGITUDE)));
     }
 
     /**
      * Longitude in WGS84 coordinate system (in decimal degrees)
      *
      * @param longitude
      */
     public void setLongitude(Float longitude) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_LONGITUDE, longitude.toString());
     }
 
     /**
      * Latitude in WGS84 coordinate system (in decimal degrees)
      *
      * @return
      */
     public Float getLatitude() {
        return Float.valueOf(this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_LATITUDE)));
     }
 
     /**
      * Latitude in WGS84 coordinate system (in decimal degrees)
      *
      * @param latitude
      */
     public void setLatitude(Float latitude) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_LATITUDE, latitude.toString());
     }
 
     /**
      * Altitude in WGS84 coordinate system (in decimal meters)
      *
      * @return
      */
     public Float getAltitude() {
        return Float.valueOf(this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_ALTITUDE)));
     }
 
     /**
      * Altitude in WGS84 coordinate system (in decimal meters)
      *
      * @param altitude
      */
     public void setAltitude(Float altitude) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_ALTITUDE, altitude.toString());
     }
 
     /**
      * the UN/LOCODE location identifier [UNLOCODE]
      *
      * @return
      */
     public String getUnlocode() {
         return this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_UNLOCODE));
     }
 
     /**
      * the UN/LOCODE location identifier [UNLOCODE]
      *
      * @param unlocode
      */
     public void setUnlocode(String unlocode) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_UNLOCODE, unlocode);
     }
 
     /**
      * A persistent globally unique URI for the location
      */
     public String getId() {
         return this.networkObject.arrayToString(this.networkObject.getValue(ReservedKeys.RECORD_LOCATION_ID));
     }
 
     /**
      * A persistent globally unique URI for the location
      *
      * @param id URI
      */
     public void setId(String id) {
         this.networkObject.add(ReservedKeys.RECORD_LOCATION_ID, id);
     }
 
     // TODO (AH): parse address! which is completely generic!
 }
