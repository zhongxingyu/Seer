 /*
  */
 package com.github.omwah.SDFEconomy.location;
 
 import com.google.common.base.Joiner;
 import com.onarandombox.multiverseinventories.MultiverseInventories;
 import com.onarandombox.multiverseinventories.api.GroupManager;
 import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Returns the location of a player based on MultiverseInventories world
  * groupings
  */
 public class MultiverseInvLocationTranslator extends SetDestinationLocationTranslator {
     private MultiverseInventories multiInv = null;
 
     public MultiverseInvLocationTranslator(Plugin plugin) {
         super(plugin.getServer());
 
         // Try and get MultiverseInventories plugin, in case loaded before
         // SDFEconomy
         loadMultiverseInventories();
 
         // Launch a listener to check for the loading and uloading of MultiverseInventories
         if (this.multiInv == null) {
             server.getPluginManager().registerEvents(new MVILoadListener(this), plugin);
         }
     }
 
     /*
      * Add listener to wait for MultiverseInventories to be loaded
      */
     public class MVILoadListener implements Listener {
         MultiverseInvLocationTranslator translator = null;
 
         public MVILoadListener(MultiverseInvLocationTranslator translator) {
             this.translator = translator;
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onPluginEnable(PluginEnableEvent event) {
             if (event.getPlugin().getDescription().getName().equals("Multiverse-Inventories")) {
                 translator.loadMultiverseInventories();
             }
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onPluginDisable(PluginDisableEvent event) {
             if (event.getPlugin().getDescription().getName().equals("Multiverse-Inventories")) {
                 translator.unloadMultiverseInventories();
             }
         }
     }
  
     private void loadMultiverseInventories() {
         Plugin plugin = this.server.getPluginManager().getPlugin("Multiverse-Inventories");
 
         if (plugin != null && plugin instanceof MultiverseInventories) {
             this.multiInv = (MultiverseInventories) plugin;
         }
     }
 
     private void unloadMultiverseInventories() {
         this.multiInv = null;
     }
 
     /*
      * Get location name based on which groups a World belongs to 
      */
     private String getLocationName(World world) {
         String locationName = null;
         
         // Try and retrieve a name based on MultiverseInvetories groupings of worlds
         if (multiInv != null) {
             GroupManager groupManager = multiInv.getGroupManager();
             if (groupManager != null) {
                 ArrayList<String> worldGroupNames = new ArrayList<String>();
                 List<WorldGroupProfile> worldGroupProfiles = groupManager.getGroupsForWorld(world.getName());
                 if (worldGroupProfiles != null) {
                     for (WorldGroupProfile i : worldGroupProfiles) {
                         worldGroupNames.add(i.getName());
                     }
                 }
                 // Create a location name that is join of all world group names
                 Joiner joiner = Joiner.on("-");
                 locationName = joiner.join(worldGroupNames);
             }
         }
         
         // If all else fails fall back to direct world name
        if (locationName == null) {
             locationName = world.getName();
         }
         return locationName;
     }
 
     public String getLocationName(Location location) {
         if (location != null) {
             return getLocationName(location.getWorld());
         } else {
             return null;
         }
     }
 
     public boolean validLocationName(String locationName) {
         // Go through each world and determine the location names
         // for that World and check if it matches the supplied world name
         List<World> worlds = server.getWorlds();
         for(World curr_world : server.getWorlds()) {
             String worldLocName = getLocationName(curr_world);
             if (worldLocName.equalsIgnoreCase(locationName)) {
                 return true;
             }
         }
         return false;
     }
 }
