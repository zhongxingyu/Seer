 package net.slipcor.pvparena.modules.eventactions;
 
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.classes.PABlockLocation;
 import net.slipcor.pvparena.commands.PAA_Edit;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.events.*;
 import net.slipcor.pvparena.managers.SpawnManager;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PAListener implements Listener {
 	private final EventActions ea;
 	
 	public PAListener(EventActions ea) {
 		this.ea = ea;
 	}
 	
 	@EventHandler
 	public void onDeath(PADeathEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("death", p, a);
 	}
 
 	@EventHandler
 	public void onEnd(PAEndEvent event) {
 		Arena a = event.getArena();
 		ea.catchEvent("end", null, a);
 	}
 
 	@EventHandler
 	public void onExit(PAExitEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("exit", p, a);
 	}
 
 	@EventHandler
 	public void onJoin(PAJoinEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		
 		if (event.isSpectator()) {
 			ea.catchEvent("spectate", p, a);
 		} else {
 			ea.catchEvent("join", p, a);
 		}
 	}
 
 	@EventHandler
 	public void onKill(PAKillEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("kill", p, a);
 	}
 
 	@EventHandler
 	public void onLeave(PALeaveEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("leave", p, a);
 	}
 
 	@EventHandler
 	public void onLose(PALoseEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("lose", p, a);
 	}
 
 	@EventHandler
 	public void onStart(PAStartEvent event) {
 		Arena a = event.getArena();
 		ea.catchEvent("start", null, a);
 	}
 
 	@EventHandler
 	public void onWin(PAWinEvent event) {
 		Arena a = event.getArena();
 		Player p = event.getPlayer();
 		ea.catchEvent("win", p, a);
 	}
 	
 	
 	/**
 	 * --------------------------------------------
 	 */
 
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public boolean playerInteract(PlayerInteractEvent event) {
 		if (!event.hasBlock()) {
 			return false;
 		}
 //		debug.i("interact eventactions", event.getPlayer());
 		Arena a = PAA_Edit.activeEdits.get(event.getPlayer().getName()+"_power");
 		
 		if (a != null) {
 //			debug.i("found edit arena", event.getPlayer());
 			Location loc = event.getClickedBlock().getLocation();
 			
 			String s = "power";
 			int i = 0;
 			for (String node : a.getArenaConfig().getKeys("spawns")) {
 				if (node.startsWith(s) && !node.contains("powerup")) {
 					node = node.replace(s, "");
 					if (Integer.parseInt(node) >= i) {
 						i = Integer.parseInt(node)+1;
 					}
 				}
 			}
 			
 			SpawnManager.setBlock(a, new PABlockLocation(loc), s+i);
 			Arena.pmsg(event.getPlayer(), Language.parse(MSG.SPAWN_SET, s+i));
 			return true;
 		}
 		
 		return false;
 	}
 }
