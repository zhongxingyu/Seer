 package com.minecraftdimensions.bungeesuitehomes.listeners;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.messaging.PluginMessageListener;
 
 import com.minecraftdimensions.bungeesuitehomes.managers.HomesManager;
 
 public class HomesMessageListener implements PluginMessageListener {
 
 	@Override
 	public void onPluginMessageReceived(String channel, Player player,
 			byte[] message) {
 		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
 				message));
 		String task = null;
 
 		try {
 			task = in.readUTF();
 
			if (task.equals("TeleportToLocation")) {
 				HomesManager
 						.teleportPlayerToLocation(
 								in.readUTF(),
 								new Location(Bukkit.getWorld(in.readUTF()), in
 										.readDouble(), in.readDouble(), in
 										.readDouble(), in.readFloat(), in
 										.readFloat()));
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
