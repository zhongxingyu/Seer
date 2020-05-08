 package alshain01.Flags;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
 import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation;
 
 import com.bekvon.bukkit.residence.Residence;
 import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
 import com.massivecraft.factions.entity.BoardColls;
 import com.massivecraft.factions.event.FactionsEventDisband;
 import com.massivecraft.mcore.ps.PS;
 import com.sk89q.worldguard.bukkit.WGBukkit;
 import com.worldcretornica.plotme.PlotManager;
 
 import alshain01.Flags.area.Area;
 import alshain01.Flags.area.FactionsTerritory;
 import alshain01.Flags.area.GriefPreventionClaim78;
 import alshain01.Flags.area.GriefPreventionClaim;
 import alshain01.Flags.area.InfinitePlotsPlot;
 import alshain01.Flags.area.PlotMePlot;
 import alshain01.Flags.area.ResidenceClaimedResidence;
 import alshain01.Flags.area.World;
 import alshain01.Flags.area.WorldGuardRegion;
 
 /**
  * Class for retrieving area system specific information.
  * 
  * @author Alshain01
  */
 public final class Director {
 	private Director(){}
 	
 	public enum LandSystem {
 		NONE(null, null), 
 		GRIEF_PREVENTION("GriefPrevention", "Grief Prevention"),
 		WORLDGUARD("WorldGuard", "WorldGuard"),
 		RESIDENCE("Residence", "Residence"),
 		INFINITEPLOTS("InfinitePlots","InfinitePlots"),
 		FACTIONS("Factions", "Factions"),
 		PLOTME("PlotMe", "PlotMe");
 		
 		private String pluginName = null;
 		private String displayName = null;
 		
 		private LandSystem(String name, String displayName) {
 			this.pluginName = name;
 			this.displayName = displayName;
 		}
 		
 		/**
 		 * Gets the plug-in name as indicated in it's plugin.yml
 		 * 
 		 * @return The case sensitive plugin.yml name for the enumerated value
 		 */
 		@Override
 		public String toString() {
 			return this.pluginName;
 		}
 		
 		/**
 		 * Gets a user friendly string, including spaces, for the plug-in.
 		 * 
 		 * @return The user friendly name of the plugin
 		 */
 		public String getDisplayName() {
 			return this.displayName;
 		}
 		
 		/**
 		 * Gets the enumeration that matches the case sensitive plugin.yml name.
 		 * 
 		 * @return The enumeration. LandSystem.NONE if no matches found.
 		 */
 		public static LandSystem getByName(String name) {
 			for(LandSystem p : LandSystem.values()) {
 				if(name.equals(p.pluginName)) { return p; }
 			}
 			return LandSystem.NONE;
 		}
 	}
 	
 	/**
 	 * Gets the current land system in use by Flags.
 	 * 
 	 * @return The current Land Management System in use by Flags.
 	 */
 	public static LandSystem getSystem() {
 		return Flags.currentSystem;
 	}
 
 	/**
 	 * Gets an area from the data store at a specific location.
 	 *  
 	 * @param location The location to request an area.
 	 * @return An Area from the configured system or the world if no area is defined.
 	 */
 	public static Area getAreaAt(Location location) {
 		// hasArea() and area.isArea() may not necessarily be the same for all systems,
 		// however hasArea() is faster than constructing an area object, and calling both has minimal impact.
 		// This is done purely for efficiency.
 		if(!hasArea(location)) { return new World(location); }
 		Area area = getArea(location);
 		return (area.isArea()) ? area : new World(location);
 	}
 	
 	/**
 	 * Gets an area by system specific name.  The name is formatted based on the system.
 	 * 
 	 * GriefPrevention = ID number
 	 * WorldGuard = worldname.regionname
 	 * Residence = Residence name OR ResidenceName.SubzoneName
	 * InifitePlots = worldname.PlotLoc (X;Z)
 	 * Factions = worldname.FactionID
 	 * PlotMe = worldname.PlotID
 	 * 
 	 * @param name The system specific name of the area or world name
 	 * @return The Area requested, may be null in cases of invalid system selection.
 	 */
 	public static Area getArea(String name) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			Plugin plugin = Flags.getInstance().getServer().getPluginManager().getPlugin("GriefPrevention");
 			if(Float.valueOf(plugin.getDescription().getVersion()) >= 7.8) {
 				Long ID = Long.parseLong(name);
 				return new GriefPreventionClaim78(ID);
 			} else if(Float.valueOf(plugin.getDescription().getVersion()) == 7.7){
 				Long ID = Long.parseLong(name);
 				return new GriefPreventionClaim(ID);
 			} else {
 				Flags.getInstance().getLogger().warning("Unsupported Grief Prevention version detected. Shutting down integrated support. Only world flags will be available.");
 				Flags.currentSystem = LandSystem.NONE;
 			}
 		} else if(getSystem() == LandSystem.RESIDENCE) { 
 			return new ResidenceClaimedResidence(name);
 		} else if(getSystem() == LandSystem.WORLDGUARD) { 
 			String[] path = name.split("\\.");
 			return new WorldGuardRegion(path[0], path[1]);
 		} else if(getSystem() == LandSystem.INFINITEPLOTS) {
 			String[] path = name.split("\\.");
 			return new InfinitePlotsPlot(path[0], path[1]);
 		} else if(getSystem() == LandSystem.FACTIONS) { 
 			String[] path = name.split("\\.");
 			return new FactionsTerritory(path[0], path[1]);
 		} else if(getSystem() == LandSystem.PLOTME) {
 			String[] path = name.split("\\.");
 			return new PlotMePlot(path[0], path[1]);
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets a set of system specific area names stored in the database
 	 * 
 	 * @return A list containing all the area names.
 	 */
 	public static Set<String> getAreaNames() {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) { return Flags.getDataStore().readKeys("GriefPreventionData"); }
 		if(getSystem() == LandSystem.RESIDENCE) { return Flags.getDataStore().readKeys("ResidenceData"); }
 		
 		if(getSystem() == LandSystem.WORLDGUARD) {
 			Set<String> worlds = Flags.getDataStore().readKeys("WorldGuardData");
 			Set<String> wgAreas = new HashSet<String>();
 			Set<String> localAreas;
 			
 			for(String world : worlds) {
 				localAreas = Flags.getDataStore().readKeys("WorldGuardData." + world);
 				for(String area : localAreas) {
 					wgAreas.add(world + "." + area);
 				}
 			}
 			return wgAreas;
 		}
 		
 		if(getSystem() == LandSystem.INFINITEPLOTS) {
 			Set<String> worlds = Flags.getDataStore().readKeys("InfinitePlotsData");
 			Set<String> areas = new HashSet<String>();
 			for(String world : worlds) {
 				Set<String>localAreas = Flags.getDataStore().readKeys("InfinitePlotsData." + world);
 				for(String localArea : localAreas) {
 					areas.add(world + "." + localArea);
 				}
 			}
 			
 			return areas;
 		}
 		
 		if(getSystem() == LandSystem.FACTIONS) {
 			Set<String> worlds = Flags.getDataStore().readKeys("FactionsData");
 			Set<String> areas = new HashSet<String>();
 			for(String world : worlds) {
 				Set<String>localAreas = Flags.getDataStore().readKeys("FactionsData." + world);
 				for(String localArea : localAreas) {
 					if(!areas.contains(localArea)) {
 						areas.add(world + "." + localArea);
 					}
 				}
 			}
 			
 			return areas;
 		}
 		
 		if(getSystem() == LandSystem.PLOTME) {
 			Set<String> worlds = Flags.getDataStore().readKeys("PlotMeData");
 			Set<String> areas = new HashSet<String>();
 			for(String world : worlds) {
 				Set<String>localAreas = Flags.getDataStore().readKeys("PlotMeData." + world);
 				for(String localArea : localAreas) {
 					areas.add(world + "." + localArea);
 				}
 			}
 			
 			return areas;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets a user friendly name of the area type of the configured system, capitalized.
 	 * For use when the name is required even though an area does not exist (such as error messages).
 	 * If you have an area instance, use Area.getAreaType() instead.
 	 * 
 	 * @return The user friendly name.
 	 */
 	public static String getSystemAreaType() {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) { return Message.GriefPrevention.get(); }
 		if(getSystem() == LandSystem.WORLDGUARD) { return Message.WorldGuard.get(); }
 		if(getSystem() == LandSystem.RESIDENCE) { return Message.Residence.get(); }
 		if(getSystem() == LandSystem.INFINITEPLOTS) { return Message.InfinitePlots.get(); }
 		if(getSystem() == LandSystem.FACTIONS) { return Message.Factions.get(); }
 		if(getSystem() == LandSystem.PLOTME) { return Message.PlotMe.get(); }
 		// Should never make it here.
 		return Message.World.get();
 	}
 	
 	/**
 	 * Checks if a player is in Pvp combat that is being monitored by the system
 	 * 
 	 * @param player The player to request information for
 	 * @return True if the player is in pvp combat, false is not or if system is unsupported.
 	 */
 	public static boolean inPvpCombat(Player player) {
 		return (getSystem() != LandSystem.GRIEF_PREVENTION) ? false : 
 			GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
 	}
 	
 	/*
 	 * Gets the area at a specific location if one exists, otherwise null
 	 */
 	private static Area getArea(Location location) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			Plugin plugin = Flags.getInstance().getServer().getPluginManager().getPlugin("GriefPrevention");
 			if(Float.valueOf(plugin.getDescription().getVersion().substring(0, 3)) >= 7.8) {
 				return new GriefPreventionClaim78(location);
 			} else if(Float.valueOf(plugin.getDescription().getVersion().substring(0, 3)) == 7.7) {
 				return new GriefPreventionClaim(location);
 			} else {
 				Flags.getInstance().getLogger().warning("Unsupported Grief Prevention version detected. Shutting down integrated support. Only world flags will be available.");
 				Flags.currentSystem = LandSystem.NONE;
 				return null;
 			}
 		}
 		else if(getSystem() == LandSystem.WORLDGUARD) { return new WorldGuardRegion(location); }
 		else if(getSystem() == LandSystem.RESIDENCE) { return new ResidenceClaimedResidence(location); }
 		else if(getSystem() == LandSystem.INFINITEPLOTS) { return new InfinitePlotsPlot(location); }
 		else if(getSystem() == LandSystem.FACTIONS) { return new FactionsTerritory(location); }
 		else if(getSystem() == LandSystem.PLOTME) { return new PlotMePlot(location); }
 		return null;
 	}
 	
 	/*
 	 * Performs a fast check to see if an area is defined at a location
 	 */
 	private static boolean hasArea(Location location) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) { return GriefPrevention.instance.dataStore.getClaimAt(location, false) != null; }
 		if(getSystem() == LandSystem.WORLDGUARD) { return WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location).size() != 0; }
 		if(getSystem() == LandSystem.RESIDENCE) { return Residence.getResidenceManager().getByLoc(location) != null; }
 		if(getSystem() == LandSystem.INFINITEPLOTS) { return InfinitePlots.getInstance().getPlotManager().getPlotAt(PlotLocation.fromWorldLocation(location)) != null; }
 		if(getSystem() == LandSystem.FACTIONS) { return BoardColls.get().getFactionAt(PS.valueOf(location)) != null; }
 		if(getSystem() == LandSystem.PLOTME) { return PlotManager.getPlotById(location) != null; }
 		return false;
 	}
 	
 	/*
 	 * Database cleanup monitors
 	 */
 	protected static void enableMrClean(PluginManager pm) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			if(Float.valueOf(pm.getPlugin(getSystem().toString())
 					.getDescription().getVersion().substring(0,3)) >= 7.8) {
 				
 				pm.registerEvents(new GriefPreventionCleaner(), Flags.getInstance());
 			}
 		} else if(getSystem() == LandSystem.RESIDENCE) {
 			pm.registerEvents(new ResidenceCleaner(), Flags.getInstance());
 		} else if(getSystem() == LandSystem.FACTIONS) {
 			pm.registerEvents(new FactionsCleaner(), Flags.getInstance());
 		}
 	}
 	
 	private static class GriefPreventionCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onClaimDeleted(ClaimDeletedEvent e) {
 			// Cleanup the database, keep the file from growing too large.
 			new GriefPreventionClaim78(e.getClaim().getID()).remove();
 		}
 	}
 	
 	private static class ResidenceCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onResidenceDelete(ResidenceDeleteEvent e) {
 			// Cleanup the database, keep the file from growing too large.
 			new ResidenceClaimedResidence(e.getResidence().getName()).remove();
 		}
 	}
 	
 	private static class FactionsCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onFactionDisband(FactionsEventDisband e) {
 			for(org.bukkit.World world : Bukkit.getWorlds()) {
 				new FactionsTerritory(world.getName(), e.getFaction().getId()).remove();
 			}
 		}
 	}
 }
