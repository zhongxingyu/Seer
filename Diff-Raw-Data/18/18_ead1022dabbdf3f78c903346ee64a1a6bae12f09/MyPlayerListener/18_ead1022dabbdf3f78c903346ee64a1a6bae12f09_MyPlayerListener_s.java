 package me.krconv.NameChanger;
 
import java.util.Map;

 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.EntityTracker;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class MyPlayerListener implements Listener {
 
 
 	NameChanger plugin;
 
 	MyPlayerListener(NameChanger instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void PlayerJoinAltChanger(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
		player.sendMessage("hi");
 		//if (plugin.RealToAltList.containsKey(player)) {
 		String alt = plugin.autoAltManager.getAltNameForPlayer(player.getName());
 		//if (plugin.autoAltManager.doesPlayerHaveAlt(player.getName())) {
 		if (alt != null) {
 			//String target = plugin.RealToAltList.get(player);
			player.sendMessage("hi");
 			if (player.getServer().getPlayer(alt) == null) {
				player.sendMessage("hi");
 				event.setJoinMessage(ChatColor.YELLOW + alt + " has joined the game.");
 				System.out.print("[NameChanger] " + player.getName()
 						+ " automatically logged in as "
 						+ alt + "!");

 				player.saveData();
 				player.setPlayerListName(alt);
 				player.setDisplayName(alt);
 				EntityPlayer playerSelf = ((CraftPlayer) player).getHandle();
 				setPlayerNameSelf(playerSelf, alt);
 				player.loadData();
 				player.teleport(player.getLocation());
				player.setGameMode(player.getServer().getDefaultGameMode());
				player.sendMessage(ChatColor.GREEN + "Your name is now "
 						+ player.getName() + ".");
				player.sendMessage("hi");
 			}
 		}
 	}
 
 	public void setPlayerNameSelf(EntityPlayer playerSelf, String newName) {
 		WorldServer world = (WorldServer) playerSelf.world;
 		EntityTracker tracker = world.tracker;
 		tracker.untrackEntity(playerSelf);
 		playerSelf.name = newName;
 		tracker.track(playerSelf);
 
 	}
 }
