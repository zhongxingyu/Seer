 package net.robbytu.bukkit.NoTalk;
 
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class NoTalk extends JavaPlugin {
 	private final chatHandler chatListener = new chatHandler(this);
 	
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(chatListener, this);
 		
 		getServer().getLogger().info("NoTalk has been happily enabled.");
 	}
 	
 	public void onDisable() {
 		getServer().getLogger().info("NoTalk has been disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if(!(sender instanceof Player)){
 			sender.sendMessage("This command must be run by a player");
 			return true;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("notalk")) {
 			if(args.length == 0) {
 				sender.sendMessage("NoTalk version 1.0");
 				sender.sendMessage("/notalk <define|remove|list|info>");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("info")) {
 				sender.sendMessage("NoTalk 1.0 by RobbytuProjects");
 				sender.sendMessage(ChatColor.GRAY + "You are able to:");
 
 				if(!sender.hasPermission("notalk.define")) {
 					sender.sendMessage(ChatColor.GRAY + "Define new NoTalk regions - " + ChatColor.RED + "NO");
 				}
 				else {
 					sender.sendMessage(ChatColor.GRAY + "Define new NoTalk regions - " + ChatColor.GREEN + "YES");
 				}
 
 
 				if(!sender.hasPermission("notalk.list")) {
 					sender.sendMessage(ChatColor.GRAY + "List all NoTalk regions - " + ChatColor.RED + "NO");
 				}
 				else {
 					sender.sendMessage(ChatColor.GRAY + "List all NoTalk regions - " + ChatColor.GREEN + "YES");
 				}
 
 
 				if(!sender.hasPermission("notalk.remove")) {
 					sender.sendMessage(ChatColor.GRAY + "Remove NoTalk regions - " + ChatColor.RED + "NO");
 				}
 				else {
 					sender.sendMessage(ChatColor.GRAY + "Remove NoTalk regions - " + ChatColor.GREEN + "YES");
 				}
 
 
 				if(!sender.hasPermission("notalk.bypass")) {
 					sender.sendMessage(ChatColor.GRAY + "Bypass NoTalk regions - " + ChatColor.RED + "NO");
 				}
 				else {
 					sender.sendMessage(ChatColor.GRAY + "Bypass NoTalk regions - " + ChatColor.GREEN + "YES");
 				}
 			}
 			else if(args[0].equalsIgnoreCase("remove")) {
 				// Check permissions
 				if(!sender.hasPermission("notalk.remove")) {
 					sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
 					return true;
 				}
 				
 				// Check arguments
 				if(args.length != 3) {
 					sender.sendMessage(ChatColor.RED + "Please provide the name of the world and region to delete.");
 					sender.sendMessage(ChatColor.GRAY + "/notalk remove <world> <region>");
 					return true;
 				}
 				
 				// Check for existence (region.world.region)
 				if(!this.getConfig().contains("region." + args[1] + "." + args[2])) {
 					sender.sendMessage(ChatColor.RED + "This region does not exist.");
 					return true;
 				}
 				
 				// Delete it
 				this.getConfig().set("region." + args[1] + "." + args[2], null);
 				this.saveConfig();
 				
 				sender.sendMessage(ChatColor.GREEN + "The region has been deleted.");
 				
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("list")) {
 				// Check permissions
 				if(!sender.hasPermission("notalk.list")) {
 					sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
 					return true;
 				}
 				
 				// Get all worlds
				Set<String> worlds;
				try {
					/*
					 *  	Fixes issue #1
					 *  
					 *  	Dirty workaround, so that, when there is no configuration file and an Exception is
					 *  	thrown, it will be catched and the user will see that there are no regions defines.
					 *  
					 *  	It's a bit dirty, but it works :-)
					 *  
					 */
					
					worlds = ((MemorySection)this.getConfig().get("region")).getKeys(false);
					if(worlds.size() == 0) {
						sender.sendMessage(ChatColor.RED + "No NoTalk regions have been defined.");
						return true;
					}
				}
				catch(Exception ex) {
 					sender.sendMessage(ChatColor.RED + "No NoTalk regions have been defined.");
 					return true;
 				}
 				
 				// Display regions per world
 				sender.sendMessage("The following NoTalk regions have been defined:");
 				for(String world : worlds) {
 					String regions = " ";
 					Set<String> _regions = ((MemorySection)this.getConfig().get("region." + world)).getKeys(false);
 					for(String region : _regions) {
 						regions += " " + region + ",";
 					}
 				
 					sender.sendMessage(ChatColor.AQUA + world + ChatColor.WHITE + ":" + regions.substring(0, regions.length()-1));
 				}
 				
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("define")) {
 				// Check permissions
 				if(!sender.hasPermission("notalk.define")) {
 					sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
 					return true;
 				}
 				
 				// Check for a name
 				if(args.length != 2) {
 					sender.sendMessage(ChatColor.RED + "You must provide a name for the NoTalk region you are about to define.");
 					sender.sendMessage(ChatColor.GRAY + "/notalk define <name>");
 					return true;
 				}
 				
 				// Check for WorldEdit
 				WorldEditPlugin WE = this.getWorldEdit();
 				if(WE == null) {
 					sender.sendMessage(ChatColor.RED + "You must have WorldEdit installed in order to define NoTalk regions.");
 					return true;
 				}
 				
 				// Check for selected region
 				Selection sel = WE.getSelection((Player)sender);
 				if(sel == null) {
 					sender.sendMessage(ChatColor.RED + "Please select a region with WorldEdit first.");
 					return true;
 				}
 
 				// Check for existence (region.world.name)
 				if(this.getConfig().contains("region." + sel.getWorld().getName() + "." + args[1])) {
 					sender.sendMessage(ChatColor.RED + "A NoTalk region named " + args[1] + " is already defined.");
 					return true;
 				}
 				
 				// Create the region itself
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".min.x", sel.getMinimumPoint().getX());
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".min.y", sel.getMinimumPoint().getY());
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".min.z", sel.getMinimumPoint().getZ());
 
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".max.x", sel.getMaximumPoint().getX());
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".max.y", sel.getMaximumPoint().getY());
 				this.getConfig().set("region." + sel.getWorld().getName() + "." + args[1] + ".max.z", sel.getMaximumPoint().getZ());
 				
 				this.saveConfig();
 				
 				sender.sendMessage(ChatColor.GREEN + "Successfully created region!");
 			}
 			return true;
 		}
 		return true; 
 	}
 	
 	private WorldEditPlugin getWorldEdit() {
 	    if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
             return (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
         }
 	    return null;
 	}
 }
