 package net.slipcor.pvparena.modules.battlefieldguard;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import net.slipcor.pvparena.api.PVPArenaAPI;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.managers.Arenas;
 
 public class BattleRunnable implements Runnable {
 	private Debug db = new Debug(42);
 
 	/**
 	 * construct a powerup spawn runnable
 	 * 
 	 * @param a
 	 *            the arena it's running in
 	 */
 	public BattleRunnable() {
 		db.i("BattleRunnable constructor");
 	}
 
 	/**
 	 * the run method, spawn a powerup
 	 */
 	@Override
 	public void run() {
 		db.i("BattleRunnable commiting");
 		try {
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 				ArenaPlayer ap = ArenaPlayer.parsePlayer(p);
 				
 				String name = PVPArenaAPI.getArenaNameByLocation(p.getLocation());
 				
 				db.i("arena: " + String.valueOf(name));
 				
 				if (name == null || name.equals("")) {
 					continue; // not physically in an arena
 				}
 				
 				Arena a = Arenas.getArenaByName(name);
 				
 				if (ap.getArena() == null || !ap.getArena().equals(a)) {
 					if (ap.getArena() != null) {
 						ap.getArena().playerLeave(p, "exit");
 						continue;
 					}
 					
 					Arenas.getArenaByName(name).tpPlayerToCoordName(p, "exit");
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
