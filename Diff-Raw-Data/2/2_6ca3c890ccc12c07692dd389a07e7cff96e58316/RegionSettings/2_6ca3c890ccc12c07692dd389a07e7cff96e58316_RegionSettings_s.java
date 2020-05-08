 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania.configuration;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.GlobalRegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.worldguard.RegionAccess;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static net.invisioncraft.plugins.salesmania.commands.auction.AuctionCommandExecutor.AuctionCommand;
 
 public class RegionSettings implements ConfigurationHandler {
     private FileConfiguration config;
     private Settings settings;
     private boolean isEnabled;
     private Salesmania plugin;
 
     private boolean wgEnabled;
     private GlobalRegionManager regionManager;
 
     private HashMap<String, RegionAccess> accessMap;
     private static final String DEFAULT_MAP_KEY = "default";
 
     public RegionSettings(Settings settings) {
         this.settings = settings;
         plugin = settings.getPlugin();
         accessMap = new HashMap<>();
 
         WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
         if(worldGuard != null && worldGuard.isEnabled()) {
             wgEnabled = true;
             regionManager = worldGuard.getGlobalRegionManager();
         }
 
         update();
     }
 
     public boolean isAllowed(Player player, AuctionCommand command) {
         if(!isEnabled) return true;
         if(player.hasPermission("salesmania.auction.region-override")) return true;
 
         if(isEnabled) {
             // Get players region
             ApplicableRegionSet regionSet = regionManager.get(player.getWorld()).getApplicableRegions(player.getLocation());
 
             boolean allowed = false;
             if(regionSet.size() == 0) { // Default
                 RegionAccess access = getRegionAccess(DEFAULT_MAP_KEY);
                 allowed = isAllowed(access, command);
             }
             else for(ProtectedRegion region : regionSet) {
                     RegionAccess access = getRegionAccess(region.getId());
                     allowed = isAllowed(access, command);
             }
 
             return allowed;
         }
 
         return false;
     }
 
     public boolean shouldStash(Player player) {
         if(!isEnabled) return false;
        if(player.hasPermission("salesmania.auction.region-override")) return true;
 
         if(isEnabled) {
             ApplicableRegionSet regionSet = regionManager.get(player.getWorld()).getApplicableRegions(player.getLocation());
 
             boolean shouldStash = false;
             if(regionSet.size() == 0) { // Default
                 RegionAccess access = getRegionAccess(DEFAULT_MAP_KEY);
                 shouldStash = access.itemsToStash();
             }
             else for(ProtectedRegion region : regionSet) {
                 RegionAccess access = getRegionAccess(region.getId());
                 shouldStash = access.itemsToStash();
             }
             return shouldStash;
         }
         return false;
     }
 
     public boolean isAllowed(RegionAccess access, AuctionCommand command) {
         // Sometimes i like to use funny syntax like this for jokes. don't mind me.
         return !access.isDenied(command) &&
                !access.isDenied(AuctionCommand.ALL);
     }
 
     public RegionAccess getRegionAccess(String region) {
         if(accessMap.containsKey(region)) return accessMap.get(region);
         else return accessMap.get(DEFAULT_MAP_KEY);
     }
 
     @SuppressWarnings("unchecked")
     public void parseRegions() {
         accessMap.clear();
 
         // Parse defaults
         try {
             RegionAccess defaultAccess = new RegionAccess();
             defaultAccess.getDenied().addAll(parseCommandList(config.getStringList("Auction.WorldGuardRegions.defaultDeny")));
             defaultAccess.setItemsToStash(config.getBoolean("Auction.WorldGuardRegions.defaultToStash"));
             accessMap.put(DEFAULT_MAP_KEY, defaultAccess);
 
         } catch (IllegalArgumentException ex) {
             plugin.getLogger().warning("Bad command '" + ex.getMessage() +  "' in world guard region default deny list");
         }
 
 
         // Parse regions
         for(Map<?,?> map : config.getMapList("Auction.WorldGuardRegions.regions")) {
             try {
                 if(!(Boolean)map.get("enabled")) continue;
                 RegionAccess regionAccess = new RegionAccess();
                 regionAccess.getDenied().addAll(parseCommandList((List<String>)map.get("deny")));
                 regionAccess.setItemsToStash((Boolean)map.get("toStash"));
 
                if(map.get("regionName") instanceof ArrayList) {
                     for (String regionName : (ArrayList<String>)map.get("regionName")) {
                         accessMap.put(regionName, regionAccess);
                     }
                } else accessMap.put((String)map.get("regionName"), regionAccess);
 
             } catch (ClassCastException | IllegalArgumentException ex) {
                 plugin.getLogger().warning("Configuration for world guard region '" + map.get("regionName") + "' seems invalid.");
                 if(ex instanceof IllegalArgumentException) {
                     plugin.getLogger().warning("Bad command in deny list '" + ex.getMessage() + "'");
                 }
             }
         }
     }
 
     private ArrayList<AuctionCommand> parseCommandList(List<String> cmdlist) throws IllegalArgumentException {
         ArrayList<AuctionCommand> commandList = new ArrayList<>();
         for (String cmdString : cmdlist) {
             try {
                 commandList.add(AuctionCommand.valueOf(cmdString.toUpperCase()));
             } catch (IllegalArgumentException ex) {
                 throw new IllegalArgumentException(cmdString); // Rethrow it with the command that failed in the message
             }
         }
         return commandList;
     }
 
 
 
     @Override
     public void update() {
         config = settings.getConfig();
         isEnabled = config.getBoolean("Auction.WorldGuardRegions.enabled") && wgEnabled;
         if(isEnabled) parseRegions();
     }
 }
