 package com.nickardson.bukkitlua;
 
 import java.io.File;
 import java.io.IOException;
import java.lang.reflect.Array;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 public class BukkitLua extends org.bukkit.plugin.java.JavaPlugin {
 	public static final String binPath = "lua-bin";
 	
 	FileReader classPathLoader;
 	public BukkitLua() {
 		if (!new File(binPath).exists()) {
 			new File(binPath).mkdir();
 		}
 		
 		classPathLoader = new FileReader();
 		
 		if (!new File(binPath + "/core.lua").exists()) {
 			classPathLoader.saveLocalFileTo("/core.lua", binPath + "/core.lua");
 		}
 		
 		if (new File(binPath + "/core.lua").exists()) {
 			try {
 				LuaRunner.core = FileReader.readAll(binPath + "/core.lua");
 			} catch (IOException e) {
 				this.getLogger().info("Unable to read " + binPath + "/core.lua");
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("Lua") && args.length >= 1) {
 			if (args[0].equalsIgnoreCase("Run")) {
 				if (sender.hasPermission("lua.runfile")) {
 					try {
 						String filename = "";
 						
 						if (args.length > 1) {
 							filename = args[1];
 						}
 						
 						File luaFile = new File(binPath + "/" + filename);
 						
 						// If we can't find the file, and there's no '.' in the filename, append .lua
 						if (!luaFile.exists() && args[1].indexOf('.') == -1) {
 							filename += ".lua";
 							luaFile = new File(binPath + "/" + filename);
 						}
 						
 						// Gets all of the args except the first one.
 						String[] scriptArgs = new String[args.length - 1];
 					    for (int i = 1; i < args.length; i++) {
 					    	scriptArgs[i-1] = args[i];
 					    }
 					    
 						if (luaFile.exists()) {
 							LuaRunner.runFile(luaFile.getPath(), sender, scriptArgs);
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.RED + "Error: File does not exist.");
 							return true;
 						}
 					}
 					catch (IOException err){
 						sender.sendMessage(ChatColor.RED + "Error: Unable to run file due to IO error.");
 						sender.sendMessage(ChatColor.RED + err.getLocalizedMessage());
 						return true;
 					}
 				}
 			}
 			else if (args[0].equalsIgnoreCase("Script") || args[0].equalsIgnoreCase("Code") || args[0].equalsIgnoreCase("Cmd")) {
 				if (sender.hasPermission("lua.runfile")) {
 					String msg = "";
 					
 				    for(int i = 1; i < args.length; i++) {
 				        msg += args[i] + " ";
 				    }
 				    
 				    /*String[] scriptArgs = new String[args.length - 1];
 				    for (int i = 1; i < args.length; i++) {
 				    	scriptArgs[i-1] = args[i];
 				    }*/
 				    
 				    String[] scriptArgs = new String[0];
 				    
 				    try {
 						LuaRunner.runString(msg, sender, scriptArgs);
 						return true;
 					} catch (IOException err) {
 						sender.sendMessage(ChatColor.RED + "Error: Unable to run file.");
 						sender.sendMessage(ChatColor.RED + err.getLocalizedMessage());
 						return true;
 					}
 				}
 			}
 		}
		else if (args[0].equalsIgnoreCase("List")) {
 			BukkitLua.printHelp(sender);
 			return true;
 		}
		else if (args.length == 0) {
 			BukkitLua.printHelp(sender);
 			return true;
 		}
 		
 		return false;
 	}
 
 	public static void printHelp(CommandSender sender) {
 		sender.sendMessage(ChatColor.AQUA + "--- BukkitLua ---" + ChatColor.RESET);
 		sender.sendMessage(ChatColor.BOLD + "/lua run path/file [arg1, arg2, ...]" + ChatColor.RESET);
 		sender.sendMessage(ChatColor.ITALIC + "    Runs a file with the given arguments." + ChatColor.RESET);
 		
 		sender.sendMessage(ChatColor.BOLD + "/lua script print('Sample')" + ChatColor.RESET);
 		sender.sendMessage(ChatColor.BOLD + "/lua code print('Sample')" + ChatColor.RESET);
 		sender.sendMessage(ChatColor.ITALIC + "    Runs a script." + ChatColor.RESET);
 	}
 	
 	@Override
 	public void onDisable() {
 
 	}
 
 	@Override
 	public void onEnable() {
 		File startupFile = new File(binPath + "/startup.lua");
 		if (!startupFile.exists()) {
 			try {
 				startupFile.createNewFile();
 			} catch (IOException e) {
 				this.getLogger().info("Unable to create startup file in " + binPath + "/startup.lua.");
 			}
 		}
 		else {
 			String script = binPath + "/startup.lua";
 			
 			try {
 				LuaRunner.runFile(script, null, null);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 	}
 
 	@Override
 	public void onLoad() {
 	}
 }
