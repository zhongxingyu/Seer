 package net.spoothie.comfort;
 
 import java.io.File;
 import java.util.HashMap;
 
 import net.minecraft.server.Packet40EntityMetadata;
 
 import net.spoothie.comfort.ComfortDataWatcher;
 import net.spoothie.comfort.EventListener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Comfort extends JavaPlugin {
 		
 		// To do:
 		// Laying down
 		// Healing while sitting/laying
 		// Piston moving?
 		// NPC sitting/laying?
 	
 		public HashMap<Player, Block> comfortPlayers = new HashMap<Player, Block>();
 		public HashMap<String, Double> comfortBlocks = new HashMap<String, Double>();
		public boolean sneaking;
 		public double distance;
 		
 		private File pluginFolder;
 		private File configFile;
 		
 		@Override
 		public void onEnable() {
 			pluginFolder = getDataFolder();
 			configFile = new File(pluginFolder, "config.yml");
 			createConfig();
 	        saveConfig();
 	        loadConfig();      
 			EventListener eventListener = new EventListener(this);
 			getServer().getPluginManager().registerEvents(eventListener, this);
 		}
 		
 		@Override
 		public void onDisable() {
 			
 		}
 		
 		private void createConfig() {
 			if(!pluginFolder.exists()) {
 				try {
 					pluginFolder.mkdir();
 				}
 				catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			if(!configFile.exists()) {
 				try {
 					configFile.createNewFile();
 					getConfig().options().copyDefaults(true);
 				}
 				catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		private void loadConfig() {
 			sneaking = getConfig().getBoolean("sneaking");
 			distance = getConfig().getDouble("distance");
 
			for(String type : getConfig().getConfigurationSection("blocks").getKeys(true))
 				comfortBlocks.put(type, getConfig().getDouble("blocks." + type));
 		}
 
 		@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 			if (command.getName().equalsIgnoreCase("comfort")) {
 	        	if(sender instanceof Player && !((Player)sender).hasPermission("comfort.reload"))
 	        		return true;
 	        	
 	        	if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 	        		reloadConfig();
 	        		loadConfig();
 	        		sender.sendMessage(ChatColor.YELLOW + "Comfort configuration file reloaded.");
 	        	}
 	        	else
 	        		sender.sendMessage(ChatColor.YELLOW + "Use '/comfort reload' to reload the configuration file.");
 	        }
 	        
 	        return true;
 	    }
 		
 		public void sitDown(Player player, Block block) {
 			player.setAllowFlight(true);
 			player.setFlying(true);
			player.sendMessage(String.valueOf(comfortBlocks.get(getTypeString(block))));
 			player.teleport(block.getLocation().add(0.5, comfortBlocks.get(getTypeString(block)) - 0.5, 0.5));		
 			Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getEntityId(), new ComfortDataWatcher((byte) 0x04));
 			
 			for(Player p : getServer().getOnlinePlayers())
 				((CraftPlayer)p).getHandle().netServerHandler.sendPacket(packet);
 			
 			comfortPlayers.put(player, block);
 		}
 		
 		/*
 		public void layDown(Player player, Block block) {
 			player.setAllowFlight(true);
 			player.setFlying(true);
 			player.teleport(block.getLocation().add(0.5, 0.5, 0.5));
 			
 			EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
 			Packet17EntityLocationAction packet = new Packet17EntityLocationAction(entityPlayer, 0, block.getX(), block.getY(), block.getZ());		
 			
 			for(Player p : getServer().getOnlinePlayers())
 				((CraftPlayer)p).getHandle().netServerHandler.sendPacket(packet);
 			
 			//Packet70Bed packet = new Packet70Bed(player.getEntityId(), 78);
 			//
 			//for(Player p : getServer().getOnlinePlayers())
 			//	((CraftPlayer)p).getHandle().netServerHandler.sendPacket(packet);
 			
 			comfortPlayers.put(player, block);
 		}
 		*/
 		
 		public void standUp(Player player) {
 			player.setFlying(false);
 			player.setAllowFlight(false);
 			Packet40EntityMetadata packet = new Packet40EntityMetadata(player.getEntityId(), new ComfortDataWatcher((byte) 0x00));
 			
 			for(Player p : getServer().getOnlinePlayers())
 				((CraftPlayer)p).getHandle().netServerHandler.sendPacket(packet);
 
 			comfortPlayers.remove(player);
 			player.setSneaking(false);
 		}
 		
 		public String getTypeString(Block block) {
			if(block.getData() != 0)
 				return (block.getTypeId() + ":" + block.getData());
			
			return (String.valueOf(block.getTypeId())); 
 		}
 }
