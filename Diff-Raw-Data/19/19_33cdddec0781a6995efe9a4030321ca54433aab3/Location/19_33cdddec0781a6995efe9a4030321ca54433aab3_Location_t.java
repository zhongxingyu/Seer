 import java.util.HashMap;
 /**
  * User: Gifflen
  */
 public class Location {
     private String locationName;
 
     public String getColor() {
         return color;
     }
 
     public void setColor(String color) {
         this.color = color;
     }
 
     private String color;
     private HashMap<String,Location> connections;
 
     public void addConnection(Location connection){
         if (!connections.containsKey(connection.getLocationName())){
             connections.put(connection.getLocationName(),connection);
         }
     }
 
     public Location(String locationName) {
        this.locationName = locationName.trim();
         this.connections = new HashMap<String,Location>();
     }
 
     public String getLocationName() {
         return locationName;
     }
 
     public HashMap<String,Location> getConnections() {
         return connections;
     }
 
     public void setLocationName(String locationName) {
         this.locationName = locationName;
     }
 
    public String listLocations(){
        String out = "";
        for(String location: connections.keySet()){
            out+= location + ";";
        }
        return out;
    }

    public static String cleanString(String incLocationName){
        incLocationName = Character.toUpperCase(incLocationName.charAt(0)) + incLocationName.substring(1).toLowerCase();
        incLocationName= incLocationName.trim();
        return incLocationName;
    }
     @Override
     public String toString() {
         return "Location{" +
                 "locationName='" + locationName + '\'' +
                ", connections=" + this.listLocations() +
                 '}';
     }
 }
