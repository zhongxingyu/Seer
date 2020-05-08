 package net.betterverse.unclaimed.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.entity.Player;
 
 public class UnclaimedCommandTeleportTask implements Runnable {
 	private static Map<String, Long> cooling = new HashMap<String, Long>();
 
 	private String player;
 
 	public UnclaimedCommandTeleportTask(Player player, long endTime) {
 		cooling.put(player.getName(), endTime);
 		this.player = player.getName();
 	}
 
 	public static long getRemainingTime(Player player) {
 		Long coolTime = cooling.get(player.getName());
 		if (coolTime == null) {
 			return 0;
 		}
		long diff = System.currentTimeMillis() - coolTime;
 		if (diff < 0) {
 			cooling.remove(player.getName()); // Just in case the server is lagging
 			return 0;
 		}
 		return diff;
 	}
 
 	public static void reset(Player player) {
 		cooling.remove(player.getName());
 	}
 
 	@Override
 	public void run() {
 		cooling.remove(player);
 	}
 }
