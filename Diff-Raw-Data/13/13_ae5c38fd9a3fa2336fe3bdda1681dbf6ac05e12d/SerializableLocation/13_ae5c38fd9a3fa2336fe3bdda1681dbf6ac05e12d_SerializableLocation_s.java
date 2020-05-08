 package com.imjake9.simplejail.cells.data;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 @SerializableAs("BukkitLocation")
 public class SerializableLocation extends Location implements ConfigurationSerializable {
     
     public SerializableLocation(World w, double x, double y, double z, float yaw, float pitch) {
         super(w, x, y, z, yaw, pitch);
     }
     
     public SerializableLocation(Location loc) {
         super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
     }
     
     public static SerializableLocation deserialize(Map<String, Object> data) {
         return new SerializableLocation(
                 Bukkit.getWorld(UUID.fromString((String)data.get("world"))),
                 (Double)data.get("x"), (Double)data.get("y"), (Double)data.get("z"),
                (Float)data.get("yaw"), (Float)data.get("pitch")
                 );
     }
     
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> loc = new HashMap<String, Object>();
         loc.put("world", getWorld().getUID().toString());
         loc.put("x", getX());
         loc.put("y", getY());
         loc.put("z", getZ());
         loc.put("yaw", getYaw());
         loc.put("pitch", getPitch());
         return loc;
     }
 
 }
