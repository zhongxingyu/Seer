 package me.limebyte.battlenight.api.battle;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 @SerializableAs("Waypoint")
 public class Waypoint implements ConfigurationSerializable {
 
     private Location location;
     private static final String LOC_SEP = ", ";
 
     public Location getLocation() {
        return location.clone();
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public boolean isSet() {
         return location != null;
     }
 
     public static final String parseLocation(Location loc) {
         if (loc == null) return "unset";
         String w = loc.getWorld().getName();
         double x = loc.getBlockX() + 0.5;
         double y = loc.getBlockY();
         double z = loc.getBlockZ() + 0.5;
         float yaw = loc.getYaw();
         float pitch = loc.getPitch();
         return w + "(" + x + LOC_SEP + y + LOC_SEP + z + LOC_SEP + yaw + LOC_SEP + pitch + ")";
     }
 
     public static final Location parseLocation(String string) {
         if (string.equals("unset")) return null;
         String[] parts = string.split("\\(");
         World w = Bukkit.getServer().getWorld(parts[0]);
 
         String[] coords = parts[1].substring(0, parts[1].length() - 1).split(LOC_SEP);
         double x = Double.parseDouble(coords[0]);
         double y = Double.parseDouble(coords[1]);
         double z = Double.parseDouble(coords[2]);
         float yaw = Float.parseFloat(coords[3]);
         float pitch = Float.parseFloat(coords[4]);
 
         return new Location(w, x, y, z, yaw, pitch);
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("Location", parseLocation(location));
         return map;
     }
 
     public static Waypoint deserialize(Map<String, Object> map) {
         Waypoint waypoint = new Waypoint();
         waypoint.setLocation(parseLocation((String) map.get("Location")));
         return waypoint;
     }
 
 }
