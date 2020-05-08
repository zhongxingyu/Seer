 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RefScraper.data;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author al
  */
 public class PositionMap {
 
     private Map<String, Position> theMap;
 
     PositionMap() {
         theMap = new HashMap<String, Position>();
        theMap.put("BattleofGlasgow(1553)", new Position("55°51′29″N", "4°15′32″W"));
         theMap.put("BattleofGlasgow(1560)", new Position("55°51′29″N", "4°15′32″W"));
         theMap.put("BattleofCravant", new Position("47°41′02″N", "3°41′30″E"));
         theMap.put("BattleofFaughart", new Position("53°50′N", "6°30′W"));
         theMap.put("BattleofLinlithgowBridge", new Position("55°58′45″N", "3°36′38″W"));
         theMap.put("BattleofVerneuil", new Position("48°44′22″N", "0°55′43″E"));
     }
 
     synchronized String getLongitude(String key) {
         Position thePos = theMap.get(key);
 
         if (thePos != null) {
             return thePos.getLongitude();
         } else {
             return null;
         }
     }
 
     synchronized String getLatitude(String key) {
         Position thePos = theMap.get(key);
 
         if (thePos != null) {
             return thePos.getLatitude();
         } else {
             return null;
         }
     }
 }
