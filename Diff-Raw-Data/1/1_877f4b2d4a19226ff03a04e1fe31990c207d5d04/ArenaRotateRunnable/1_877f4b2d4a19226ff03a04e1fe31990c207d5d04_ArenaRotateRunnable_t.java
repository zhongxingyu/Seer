 package net.slipcor.pvparena.modules.arenarotate;
 
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Debug;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaRegion;
 import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
 import net.slipcor.pvparena.managers.ArenaManager;
 import net.slipcor.pvparena.runnables.ArenaRunnable;
 
 public class ArenaRotateRunnable extends ArenaRunnable {
 	private Debug debug = new Debug(68);
 	public ArenaRotateRunnable(Arena a, int i) {
 		super(MSG.ARENA_STARTING_IN.getNode(), i, null, null, true);
 		debug.i("ArenaRotateRunnable constructor");
 	}
 
 	protected void commit() {
 		debug.i("ArenaRotateRunnable commiting");
 		ArenaRotate.commit();
 	}
 
 	@Override
 	protected void warn() {
 		PVPArena.instance.getLogger().warning("ArenaRotateRunnable not scheduled yet!");
 	}
 	
 	@Override
 	public void spam() {
 		if ((super.message == null) || (MESSAGES.get(seconds) == null)) {
 			return;
 		}
 		MSG msg = MSG.getByNode(this.message);
 		if (msg == null) {
 			PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
 			return;
 		}
 		String message = seconds > 5 ? Language.parse(msg, MESSAGES.get(seconds), ArenaManager.getNames()) : MESSAGES.get(seconds);
 		if (global) {
 			Player[] players = Bukkit.getOnlinePlayers();
 			
 			playerssss: for (Player p : players) {
 				for (Arena aaa : ArenaManager.getArenas()) {
 					if (!aaa.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_ONLYSPAMTOJOIN)) {
 						Arena.pmsg(p, message);
 						continue playerssss;
 					}
 					for (ArenaRegion region : aaa.getRegionsByType(RegionType.JOIN)) {
 						if (region.getShape().contains(new PABlockLocation(p.getLocation()))) {
 							Arena.pmsg(p, message);
 							continue playerssss;
 						}
 					}
 				}
 			}
 			
 			return;
 		}
 		if (arena != null) {
 			Set<ArenaPlayer> players = arena.getFighters();
 			for (ArenaPlayer ap : players) {
 				if (sPlayer != null) {
 					if (ap.getName().equals(sPlayer)) {
 						continue;
 					}
 				}
 				if (ap.get() != null) {
 					arena.msg(ap.get(), message);
 				}
 			}
 			return;
 		}
 		if (Bukkit.getPlayer(sPlayer) != null) {
 			Arena.pmsg(Bukkit.getPlayer(sPlayer), message);
 			return;
 		}
 	}
 }
