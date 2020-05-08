 package com.craigknott.setLocker;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class CommandManager implements CommandExecutor {
 
 	private SetLocker plugin;
 	
 	public CommandManager(SetLocker plugin){
 			this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("getCurrentSelection")){
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Player only command");
 			} else {
 				WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
 				Selection selection = worldEdit.getSelection((Player) sender);
 				sender.sendMessage("It works");
 				return true;
 			}
 		}
 			
 		return false;
 	}
 	
 }
