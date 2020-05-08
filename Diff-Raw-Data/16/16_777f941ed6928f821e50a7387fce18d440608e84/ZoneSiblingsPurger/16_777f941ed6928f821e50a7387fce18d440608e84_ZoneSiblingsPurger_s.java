 package pl.edu.mimuw.cloudatlas.agent;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import pl.edu.mimuw.cloudatlas.attributes.TimeValue;
 import pl.edu.mimuw.cloudatlas.zones.Zone;
 import pl.edu.mimuw.cloudatlas.zones.ZoneNames;
 
 public class ZoneSiblingsPurger {
 	
 	private static Logger log = LogManager.getFormatterLogger(ZoneSiblingsPurger.class);
 
 	private final String myZoneName;
 	private final long maxZoneAge;
 	
 	public ZoneSiblingsPurger(String myZoneName, long maxZoneAge) {
 		this.myZoneName = myZoneName;
 		this.maxZoneAge = maxZoneAge;
 	}
 	
 	public void purgeSiblings(Zone rootZone) {
 		long timestampThreshold = new Date().getTime() - maxZoneAge;
 		
 		for (Zone zone = rootZone.findZone(myZoneName).getParent(); zone != null; zone = zone.getParent()) {
 			List<String> childNames = new ArrayList<String>();
 			childNames.addAll(zone.getChildNames());
 			
 			for (String childName : childNames) {
 				Zone childZone = zone.getChild(childName);
 				if (
 						!ZoneNames.isAncestorOrSelf(myZoneName, childZone.getGlobalName()) &&
 						getTimestamp(childZone) < timestampThreshold
 				) {
					log.debug("Purging old zone %s", childZone.getGlobalName());
 					zone.removeChild(childName);
 				}
 			}
 		}
 	}
 	
 	private long getTimestamp(Zone zone) {
 		return ((TimeValue) zone.getZMI().getAttributeValue("timestamp")).getTimestamp();
 	}
 }
