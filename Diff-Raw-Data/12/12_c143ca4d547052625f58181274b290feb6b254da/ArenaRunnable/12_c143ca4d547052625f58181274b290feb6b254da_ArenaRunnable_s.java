 package net.slipcor.pvparena.runnables;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * <pre>Arena Runnable class</pre>
  * 
  * The interface for arena timers
  * 
  * @author slipcor
  * 
  * @version v0.10.0.1
  */
 
 public abstract class ArenaRunnable extends BukkitRunnable {
 
 	protected static HashMap<Integer, String> messages = new HashMap<Integer, String>();
 	static {
 		String s = Language.parse(MSG.TIME_SECONDS);
 		String m = Language.parse(MSG.TIME_MINUTES);
 		messages.put(1, "1..");
 		messages.put(2, "2..");
 		messages.put(3, "3..");
 		messages.put(4, "4..");
 		messages.put(5, "5..");
 		messages.put(10, "10 " + s);
 		messages.put(20, "20 " + s);
 		messages.put(30, "30 " + s);
 		messages.put(60, "60 " + s);
 		messages.put(120, "2 " + m);
 		messages.put(180, "3 " + m);
 		messages.put(240, "4 " + m);
 		messages.put(300, "5 " + m);
 		messages.put(600, "10 " + m);
 		messages.put(1200, "20 " + m);
 		messages.put(1800, "30 " + m);
 		messages.put(2400, "40 " + m);
 		messages.put(3000, "50 " + m);
 		messages.put(3600, "60 " + m);
 	}
	String message;
	Integer seconds;
	String sPlayer;
	Arena arena;
	Boolean global;
 	
 	/**
 	 * Spam the message of the remaining time to... someone, probably:
 	 * @param s the Language.parse("**") String to wrap
 	 * @param arena the arena to spam to (!global) or to exclude (global)
 	 * @param player the player to spam to (!global && !arena) or to exclude (global || arena)
 	 * @param i the seconds remaining
 	 * @param global the trigger to generally spam to everyone or to specific arenas/players
 	 */
 	public ArenaRunnable(String s, Integer i, Player player, Arena arena, Boolean global) {
 		this.message = s;
 		this.seconds = i;
 		this.sPlayer = player == null ? null : player.getName();
 		this.arena = arena;
 		this.global = global;
 		
 		if (this instanceof EndRunnable) {
 			runTaskTimer(PVPArena.instance, 20L, 20L);
 		} else {
 			runTaskTimerAsynchronously(PVPArena.instance, 20L, 20L);
 		}
 	}
 	public void spam() {
 		if ((message == null) || (messages.get(seconds) == null)) {
 			return;
 		}
 		MSG msg = MSG.getByNode(this.message);
 		if (msg == null) {
 			PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
 			return;
 		}
 		String message = seconds > 5 ? Language.parse(msg, messages.get(seconds)) : messages.get(seconds);
 		if (global) {
 			Player[] players = Bukkit.getOnlinePlayers();
 			
 			for (Player p : players) {
 				try {
 					if (arena != null) {
 						if (arena.hasPlayer(p)) {
 							continue;
 						}
 					}
 					if (sPlayer != null) {
 						if (sPlayer.equals(p.getName())) {
 							continue;
 						}
 					}
 					Arena.pmsg(p, message);
 				} catch (Exception e) {}
 			}
 			
 			return;
 		}
 		if (arena != null) {
 			HashSet<ArenaPlayer> players = arena.getFighters();
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
 	
 	@Override
 	public void run() {
 		spam();
 		if (seconds <= 0) {
 			commit();
 			try {
 				cancel();
 			} catch (IllegalStateException e) {
 				warn();
 			}
 		}
 		seconds--;
 	}
 	
 	protected abstract void warn();
 	protected abstract void commit();
 }
