 package com.crimsonrpg.personas.personas.flag;
 
 import com.crimsonrpg.flaggables.api.FlagId;
 import com.crimsonrpg.flaggables.api.GenericFlag;
 import com.crimsonrpg.personas.personasapi.npc.NPC;
 import org.bukkit.Location;
 import org.bukkit.configuration.ConfigurationSection;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 
 /**
  * The core trait. Developers don't need to access this.
  * All NPCs should have this.
  */
 @FlagId("npc-core")
 public class FlagNPCCore extends GenericFlag<NPC> {
 
     private Location location;
     private int health;
 
     public void load(ConfigurationSection cs) {
         World world = Bukkit.getWorld(cs.getString("loc.world"));
         if (world != null) {
             location = new Location(
                     Bukkit.getWorld(cs.getString("loc.world")),
                     cs.getDouble("loc.x"),
                     cs.getDouble("loc.y"),
                     cs.getDouble("loc.z"),
                     (float) cs.getDouble("loc.pitch"),
                     (float) cs.getDouble("loc.yaw"));
         } else {
             location = null;
         }
 
         health = cs.getInt("health");
     }
 
     public void save(ConfigurationSection cs) {
         String worldName = "";
         double x = 0;
         double y = 0;
         double z = 0;
         float pitch = 0;
         float yaw = 0;
 
         if (location != null) {
             worldName = location.getWorld().getName();
             x = location.getX();
             y = location.getY();
             z = location.getZ();
             pitch = location.getPitch();
             yaw = location.getYaw();
         }
 
         cs.set("loc.world", worldName);
         cs.set("loc.x", x);
         cs.set("loc.y", y);
         cs.set("loc.z", z);
         cs.set("loc.pitch", pitch);
         cs.set("loc.yaw", yaw);
 
 
         cs.set("health", health);
     }
 
     public void reset() {
         location = null;
        health = 0;
     }
 
     public int getHealth() {
         return health;
     }
 
     public FlagNPCCore setHealth(int health) {
         this.health = health;
         return this;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public FlagNPCCore setLocation(Location location) {
         this.location = location;
         return this;
     }
 }
