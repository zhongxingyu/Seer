 package ca.idrc.tagin.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.Basic;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
import com.google.appengine.api.datastore.Key;

 @Entity
 public class Pattern {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Key key;
 	
 	@Basic(fetch = FetchType.EAGER)
 	@ElementCollection
 	private Map<String,Integer> beacons; // BSSID / RSSI pairs
 	
 	public Pattern() {
 		beacons = new HashMap<String,Integer>();
 	}
 	
	public Key getKey() {
		return key;
 	}
 	
 	public Map<String,Integer> getBeacons() {
 		return beacons;
 	}
 	
 	public String toString() {
 		return getClass().getName() +
			"[Key: " + getKey() + ", values: " + getBeacons().toString() + "]";
 	}
 
 }
