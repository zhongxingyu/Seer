 package net.kiwz.ThePlugin.utils;
 
 import java.io.File;
 
 import net.minecraft.server.v1_6_R2.EntityPlayer;
 import net.minecraft.server.v1_6_R2.MinecraftServer;
 import net.minecraft.server.v1_6_R2.PlayerInteractManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
 import org.bukkit.entity.Player;
 
 public class OfflinePlayer {
 	/**
 	 * Gets a player object by the given username
 	 * <p />
 	 * This method may not return objects for players that never played on this server before
 	 * 
 	 * @param name Name to look up
 	 * 
 	 * @return Player as long as the player has played here before, otherwise null
 	 */
     public Player getOfflinePlayer(String name) {
     	File playerfolder = null;
         Player player = null;
         Player onlinePlayer = Bukkit.getServer().getPlayer(name);
         if (onlinePlayer != null) {
         	player = onlinePlayer;
             return player;
         }
         try {
             playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
         } catch (Exception e) {
         }
         for (File playerfile : playerfolder.listFiles()) {
             String filename = playerfile.getName();
             String playername = filename.substring(0, filename.length() - 4);
            if (playername.trim().equalsIgnoreCase(name)) {
                 final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
                 final EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new PlayerInteractManager(server.getWorldServer(0)));
                 player = (entity == null) ? null : (Player) entity.getBukkitEntity();
                 if (player != null) {
                 	player.loadData();
                 }
             }
         }
         return player;
     }
 }
