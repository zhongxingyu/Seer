 package com.skyost.jail.tasks;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.skyost.jail.BukkitJail;
 import com.skyost.jail.util.Utils;
 
 public class ReleasePlayer implements Runnable {
 	
 	String playername;
 	
 	public ReleasePlayer(String playername) {
 		this.playername = playername;
 	}
 
 	@Override
 	public void run() {
 		Player player = Bukkit.getPlayer(playername);
		if(BukkitJail.isJailed(player)) {
			try {
				BukkitJail.getBukkitJailConfig().JailedPlayers.remove(playername);
				BukkitJail.getBukkitJailConfig().save();
				if(player != null && player.isOnline()) {
					player.teleport(Utils.getMainWorld().getSpawnLocation());
					player.sendMessage(BukkitJail.getBukkitJailConfig().JailedMessages_5);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
 			}
 		}
 	}
 
 }
