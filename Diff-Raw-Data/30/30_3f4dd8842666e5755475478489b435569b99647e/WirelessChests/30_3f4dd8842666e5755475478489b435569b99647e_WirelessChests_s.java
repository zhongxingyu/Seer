 package com.resbah.WirelessChests;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WirelessChests extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public void onEnable(){
 		log.info("WirelessChests has been Enabled");
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	 
 	public void onDisable(){ 
 		this.saveConfig();
 		log.info("WirelessChests has been Disabled");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (cmd.getName().equalsIgnoreCase("wcrem")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 				if(player.hasPermission("wirelesschests.remove")){
 	    		if(args.length == 2){
 	    			String chest = args[0];
 	    			String group = args[1];
 	    			this.getConfig().set("group."+group+"."+chest, null);
 	    			player.sendMessage("Chest removed successfully!");
 	    			if(this.getConfig().getString("defaults."+group) == chest){
 	    				this.getConfig().set("defaults."+group,null);
 	    				player.sendMessage("Chest was a Main chest. Removed from Main Chest List Successfully!");
 	    			}
 	    			this.saveConfig();
 	    			this.reloadConfig();
 	    		}
 			}else{
 				player.sendMessage("You do not have permission to preform this command!");
 			}
 			}
 			return true;
 		}
 			
 		if (cmd.getName().equalsIgnoreCase("wcremgroup")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 				if(player.hasPermission("wirelesschests.removegroup")){
 	    		if(args.length == 1){
 	    			String group = args[1];
 	    			this.getConfig().set("group."+group, null);
 	    			this.getConfig().set("defaults."+group,null);
 	    			player.sendMessage("Group removed successfully!");
 	    		}
 	    			this.saveConfig();
 	    			this.reloadConfig();
 	    		}else{
 					player.sendMessage("You do not have permission to preform this command!");
 				}
 			}
 			return true;
 			}
 			
 		if (cmd.getName().equalsIgnoreCase("wcabout")){
 			if(player.hasPermission("wirelesschests.about")){
				sender.sendMessage("WirelessChests by resba");
				sender.sendMessage("Version 0.0.3-ALPHA");
 				sender.sendMessage("https://www.github.com/resba/WirelessChests");
 			}else{
 				player.sendMessage("You do not have permission to preform this command!");
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("wcsetmain")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 				if(player.hasPermission("wirelesschests.setmain")){
 	    		if(args.length == 2){
 	    			String group = args[1];
 	    			String chest = args[0];
 	    			if(this.getConfig().get("group."+group) != null){
 	    				if(this.getConfig().get("group."+group+"."+chest) != null){
 	    					this.getConfig().set("defaults."+group, chest);
 	    					player.sendMessage("Done!");
 	    					this.saveConfig();
 	    					this.reloadConfig();
 	    				}else{
 	    					player.sendMessage("Chest does not exist!");
 	    				}
 	    			}else{
 	    				player.sendMessage("Group does not exist!");
 	    			}
 	    			
 	    		}
 				}else{
 					player.sendMessage("You do not have permission to preform this command!");
 				}
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("wcsync")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 				if(player.hasPermission("wirelesschests.sync")){
 			if(args.length == 1){
 				String group = args[0];
 				if(this.getConfig().getString("defaults."+group) != null){
 				String dc = this.getConfig().getString("defaults."+group);
 				int dx = this.getConfig().getInt("group."+group+"."+dc+".x");
 				int dy = this.getConfig().getInt("group."+group+"."+dc+".y");
 				int dz = this.getConfig().getInt("group."+group+"."+dc+".z");
 				World dw = player.getWorld();
 				Block db = dw.getBlockAt(dx, dy, dz);
 				Chest dchst = (Chest)db.getState();
 				Inventory maininv = dchst.getInventory();
 				ItemStack[] mainstack = maininv.getContents();
 				
 				
 					Set<String> keys = this.getConfig().getConfigurationSection("group."+group).getKeys(false);
 					Iterator<String> iter = keys.iterator();
 						while (iter.hasNext()) {
 							String cnames = iter.next();
 							int x = this.getConfig().getInt("group."+group+"."+cnames+".x");
 							int y = this.getConfig().getInt("group."+group+"."+cnames+".y");
 							int z = this.getConfig().getInt("group."+group+"."+cnames+".z");
 							World w = player.getWorld();
 							Block b = w.getBlockAt(x, y, z);
 							Chest chst = (Chest)b.getState();
 							Inventory tchst = chst.getInventory();
 							tchst.setContents(mainstack);
 							player.sendMessage(cnames+" Synced with Main Chest "+dc+".");
 					}
 				}
 				
				
 				}else{
					player.sendMessage("Error! You haven't set a Main chest yet. Do so with /wcsync [chest name] [group name]");
 				}
 				
 			}else{
 				player.sendMessage("You do not have permission to preform this command!");
 			}
 			}
 				
 			return true;
 			}
 
 		if(cmd.getName().equalsIgnoreCase("wcaddchest")){
 			if(player == null) {
 				sender.sendMessage("This command can only be run by a player.");
 				return false;
 			}else{
 				if(player.hasPermission("wirelesschests.add")){
 			if(args.length == 2){
 			String chestname = args[0];
 			String groupname = args[1];
 			Location loc = player.getLocation();
 			World w = loc.getWorld();
 			loc.setY(loc.getY() - 1);
 			Block b = w.getBlockAt(loc);
 			int bid = b.getTypeId();
 			if(bid!=54){
 			b.setType(Material.CHEST);
 			}
 			player.sendMessage("Created chest "+chestname+" inside group "+groupname+"!");
 			this.getConfig().set("chest." + chestname, chestname);
 			if (this.getConfig().get("group." + groupname) == null){
 			this.getConfig().set("group." + groupname, true);
 			}
 			this.getConfig().set("group." + groupname +"."+chestname+".chestname", chestname);
 			this.getConfig().set("group." + groupname +"."+chestname+".x", loc.getBlockX());
 			this.getConfig().set("group." + groupname +"."+chestname+".y", loc.getBlockY());
 			this.getConfig().set("group." + groupname +"."+chestname+".z", loc.getBlockZ());
 			this.saveConfig();
 			this.reloadConfig();
 			}else{
 				player.sendMessage("Sorry! Your formatting is invalid. Please try again.");
 				return false;
 			}
 		}else{
 				player.sendMessage("You do not have permission to preform this command!");
 			}
 			}
 			return true;
 		}
 		return false;
 	}
 	private final WirelessChestsBlockListener blockListener = new WirelessChestsBlockListener(this);
 }
