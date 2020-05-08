 package me.wizzledonker.plugins.oblicomcore.world;
 
 import java.util.Date;
 import me.wizzledonker.plugins.oblicomcore.OblicomCore;
 import org.bukkit.configuration.ConfigurationSection;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import me.wizzledonker.plugins.oblicomcore.entity.Citizen;
 
 import me.wizzledonker.plugins.oblicomcore.exceptions.OblicomWorldException;
 
 /**
  *
  * @author nagib.kanaan
  */
 public class Jail {
     private OblicomWorld world;
     private ConfigurationSection data;
     private ConfigurationSection location;
     
     public Jail () {
        this.world = OblicomWorld.getWorld();;
         this.data = world.getData().getConfigurationSection("police.jail.prisoners");
         this.location = world.getData().getConfigurationSection("police.jail.location");
         
         if (data == null) {
             data = world.getData().createSection("police.jail.prisoners");
         }
     }
     
     /**
      * Add Citizen to the list of arrested.
      * 
      * @param citizen
      * @param reason 
      * @param time
      */
     public void arrestCitizen(Citizen citizen, String reason, int time) {
         String player = citizen.getPlayer().getName();
         
         data.set(player + ".reason", reason);
         data.set(player + ".time", time); // TODO ADD TIME
     };
     
     /**
      * Remove Citizen from the list of arrested.
      * 
      * @param citizen
      */    
     public void releaseCitizen(Citizen citizen) {
         String player = citizen.getPlayer().getName();
         
         data.set(player, null);
     };
 
     /**
      * Verify if the citizen is in jail
      * 
      * @param citizen
      * @return citizen is arrested?
      */    
     public boolean isInList(Citizen citizen) {
         return data.contains(citizen.getPlayer().getName());
 
     }
     /**
      * Verify if the citizen is in jail by name
      * 
      * @param name of player
      * @return player is arrested?
      */    
     public boolean isInList(String name) {
         return data.contains(name);
     }
     
     /**
      * Set the location of the Jail.
      * 
      * @param player
      * @return jail location saved?
      */    
     public boolean setLocation(Player player) {
         Location playerLocation = player.getLocation();
         
         location.set("x", playerLocation.getX());
         location.set("y", playerLocation.getY());
         location.set("z", playerLocation.getZ());
         location.set("world", playerLocation.getWorld().getName());
         
         try {
             world.save();
             return true;
         } catch (OblicomWorldException error) {
             OblicomCore.log("Erro to save the jail location: " + error.getMessage());
             return false;
         }
     }
     
     /**
      * Get the location of the Jail.
      * 
      * @return location
      */    
     public Location getLocation() {
         World map = world.getPlugin().getServer().getWorld(location.getString("police.jail.location.world"));
         int x = location.getInt("police.jail.location.x");
         int y = location.getInt("police.jail.location.y");
         int z = location.getInt("police.jail.location.z");
         
         return new Location(map, x, y, z);
     }
 }
