 package me.asofold.bpl.rsp.core;
 
 import me.asofold.bpl.rsp.RSP;
 import me.asofold.bpl.rsp.config.WorldSettings;
 import me.asofold.bpl.rsp.plshared.Players;
 import me.asofold.bpl.rsp.tasks.DelayedTeleport;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 
 /**
  * Static methods for confinement and distances.
  * @author mc_dev
  *
  */
 public class Confinement {
 	
 	/**
 	 * Euklidian distance in x-z plane to center of settings.
 	 * @param settings
 	 * @param loc
 	 * @return
 	 */
 	public static final double distance(final WorldSettings settings, final Location loc){
 		final double dx = settings.cX - loc.getX();
 		final double dz = settings.cZ - loc.getZ();
 		return Math.sqrt(dx*dx+dz*dz);
 	}
 	
 
 	/**
 	 * Check if the player is still inside of bounds for the given world.
 	 * @param settings not null
 	 * @param data
 	 * @param loc
 	 * @return If inside  confinements or not teleported  (might be outside, but offline!)<br> So a return value of false means that an attempt has been or will be made to teleport the player.
 	 */
 	public static final boolean checkConfinement(final WorldSettings settings, final PlayerData data, final Location loc){
 		// TODO: reorganize to use variables for distances rather than calling methods all the time.
 		final boolean outside;
 		if (settings.circular) outside = distance(settings, loc)>settings.cR; // TODO: use squared dist (WorldSettings.cRsq)
 		else outside = (Math.abs(settings.cX-loc.getX()) > settings.cR) || (Math.abs(settings.cZ-loc.getZ()) > settings.cR);
 		if ( !outside){
 			data.lastValidLoc = loc.clone();
 			return true;
 		}
 		else{
 			restoreConfinement(settings, data, loc);
 			return false;
 		}
 	}
 
 	/**
 	 * Teleport player back into the confinement.
 	 * @param settings
 	 * @param data
 	 * @param loc
 	 * @return
 	 */
 	public static final boolean restoreConfinement(final WorldSettings settings, final PlayerData data, final Location loc){
 		final Player player = Players.getPlayerExact(data.playerName);
 		if ( player == null) return true; // TODO: special case.
 		Location tpLoc = null;
 		if ( settings.useLastLocation){
 			if (data.lastValidLoc != null){
 				// Could be in another world !
 				if (isWithinBounds(getSettings(data.lastValidLoc.getWorld().getName()), data.lastValidLoc)){
 					tpLoc = data.lastValidLoc;
 				}
 				else data.lastValidLoc = null;
 			}
 		}
 		if ( tpLoc == null){
 			org.bukkit.util.Vector lv = loc.toVector();
 			final World world = loc.getWorld();
 			
 			if ( settings.circular){
 				final org.bukkit.util.Vector center = new org.bukkit.util.Vector( settings.cX, loc.getY(), settings.cZ);
 				org.bukkit.util.Vector tpDir = center.subtract(loc.toVector()).normalize().multiply(center.distance(lv)-settings.cR+1.0);
 				lv = lv.add(tpDir);
 			} else{ // rectangular
 				double dX = lv.getX() - settings.cX;
 				if ( dX > settings.cR ) lv.setX(settings.cX+settings.cR - 1.0);
 				else if (dX < -settings.cR) lv.setX(settings.cX-settings.cR + 1.0);
 				double dZ = lv.getZ() - settings.cZ;
 				if ( dZ > settings.cR ) lv.setZ(settings.cZ+settings.cR - 1.0);
 				else if (dZ < -settings.cR) lv.setZ(settings.cZ-settings.cR + 1.0);
 			}
 			int bX = lv.getBlockX();
 			int bZ = lv.getBlockZ();
			Chunk chunk = world.getChunkAt(bX / 16,bZ / 16);
 			if ( !chunk.isLoaded()) chunk.load();
 			// TODO: maybe calculate distance to old loc, see if air at same level etc!
 			// TODO: some nether hook ? or highest block setting ?
 			lv.setY(world.getHighestBlockYAt(bX, bZ)+1);
 			tpLoc = new Location(world, lv.getX(), lv.getY(), lv.getZ());
 		} 
 		tpLoc.setYaw(loc.getYaw());
 		tpLoc.setPitch(loc.getPitch());
 		if ( Bukkit.getScheduler().scheduleSyncDelayedTask(RSP.getPluginInstance(), new DelayedTeleport(data.playerName, tpLoc)) == -1) player.teleport(tpLoc);
 		player.sendMessage(settings.cMessage);
 		return false;
 	}
 
 	/**
 	 * Expect same world !
 	 * @param s not null
 	 * @param loc
 	 * @return
 	 */
 	public static final boolean isWithinBounds(final WorldSettings s, final Location loc) {
 		if ( !s.confine) return true;
 		if ( s.circular) return distance(s, loc) <= s.cR;
 		else return (Math.abs(s.cX-loc.getX()) <= s.cR) && (Math.abs(s.cZ-loc.getZ()) <= s.cR);
 	}
 	
 	/**
 	 * Used for set back postition.
 	 * @param worldName
 	 * @return
 	 */
 	public static final WorldSettings getSettings(String worldName){
 		return ((RSPCore) RSP.getRSPCore()).getSettings(worldName);
 	}
 
 }
