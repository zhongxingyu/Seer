 package net.betterverse.unclaimed.commands;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class UnclaimedCommandTeleportTask implements Runnable {
	private static Set<String> cooling = new HashSet<String>();
 
	private String player;
 
	public UnclaimedCommandTeleportTask(Player player) {
		cooling.add(player.getName());
		this.player = player.getName();
	}
 
	public static boolean isCooling(Player player) {
		return cooling.contains(player.getName());
	}
 
	@Override
	public void run() {
		cooling.remove(player);
	}
 }
