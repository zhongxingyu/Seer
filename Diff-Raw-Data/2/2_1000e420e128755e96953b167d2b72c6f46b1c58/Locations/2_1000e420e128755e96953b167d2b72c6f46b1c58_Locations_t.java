 package tk.kirlian.util;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 
 /**
  * Functions to convert Locations to Strings and vice versa.
  */
 public class Locations {
     private static final String integerRegex = "([+-]?\\d+)";
     private static final Pattern locationRegex = Pattern.compile(
        "\\s*" + "((.+?)\\s*:)?" +
         "\\s*" + integerRegex + "\\s*" + "," +
         "\\s*" + integerRegex + "\\s*" + "," +
         "\\s*" + integerRegex + "\\s*");
 
     private Locations() {}
 
     /**
      * Try to parse a {@link Location} object in the form "WorldName:X,Y,Z".
      *
      * @throws IllegalArgumentException if the location string is invalid.
      */
     public static Location parseLocation(final Server server, final String locationString) {
         Matcher matcher = locationRegex.matcher(locationString);
         if(matcher.matches()) {
             World world = null;
             if(matcher.group(1) != null) {
                 world = server.getWorld(matcher.group(2));
             }
             if(world == null) {
                 throw new IllegalArgumentException("Invalid world name");
             }
             int x = Integer.parseInt(matcher.group(3));
             int y = Integer.parseInt(matcher.group(4));
             int z = Integer.parseInt(matcher.group(5));
             return new Location(world, x, y, z);
         } else {
             throw new IllegalArgumentException("Invalid location");
         }
     }
 
     /**
      * Convert a {@link Location} object to a string that can be parsed
      * by {@link #parseLocation(Server, String)}.
      */
     public static String toString(Location location) {
         StringBuilder s = new StringBuilder(32);
         if(location.getWorld() != null) {
             s.append(location.getWorld().getName());
             s.append(":");
         }
         s.append(Integer.toString(location.getBlockX()));
         s.append(",");
         s.append(Integer.toString(location.getBlockY()));
         s.append(",");
         s.append(Integer.toString(location.getBlockZ()));
         return s.toString();
     }
 }
