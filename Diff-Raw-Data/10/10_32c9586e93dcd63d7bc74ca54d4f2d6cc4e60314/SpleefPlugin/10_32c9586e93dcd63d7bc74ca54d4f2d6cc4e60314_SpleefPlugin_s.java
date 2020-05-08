 package org.noip.evan1026;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class SpleefPlugin extends JavaPlugin implements Listener {
 	Logger log;
 	WorldEditPlugin we;
 	HashMap<String, ArrayList<Location>> arenaBlockLocations = new HashMap<String, ArrayList<Location>>();
 	HashMap<Location, BlockState> blocks = new HashMap<Location, BlockState>();
 
 	public void onEnable(){
 		we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
 		log = getLogger();
 		log.info("Spleef is enabled WOO!!");
 		getServer().getPluginManager().registerEvents(this, this);
 		loadFiles();
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (IOException e) {
 		    e.printStackTrace();
 		}
 	}
 
 	public void onDisable(){
 		log.info("Y U NO LOVE SPLEEF?!");
 		saveFiles();
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if (cmd.getName().equalsIgnoreCase("spleefAddBlocks")){
 			if (!(sender instanceof Player)){
 				sender.sendMessage("This command can only be used in-game.");
 				return true;
 			}
 			Selection selection = we.getSelection((Player)sender);
 			if (selection == null){
 				sender.sendMessage(ChatColor.RED + "Make a WorldEdit selection first.");
 				return true;
 			}
 			if (args.length != 1){
 				return false;
 			}
 			if (!arenaBlockLocations.containsKey(args[0])){
 				sender.sendMessage(ChatColor.RED + "Arena " + args[0] + " does not exist. You can add it by typing /spleefAddArena " + args[0] + ".");
 				return true;
 			}
 			for(int i = selection.getMinimumPoint().getBlockX(); i <= selection.getMaximumPoint().getBlockX(); i++){
 				for(int j = selection.getMinimumPoint().getBlockY(); j <= selection.getMaximumPoint().getBlockY(); j++){
 					for(int k = selection.getMinimumPoint().getBlockZ(); k <= selection.getMaximumPoint().getBlockZ(); k++){
 						Location here = new Location(selection.getWorld(), i, j, k);
						if (!arenaBlockLocations.containsValue(here)) arenaBlockLocations.get(args[0]).add(here);
 						if (!blocks.containsKey(here)){
 							blocks.put(here, selection.getWorld().getBlockAt(here).getState());
 						}
						else if(!(blocks.get(here).getType().equals(selection.getWorld().getBlockAt(here).getState().getType()) && blocks.get(here).getData().equals(selection.getWorld().getBlockAt(here).getState().getData()))){
 							blocks.remove(here);
 							blocks.put(here, selection.getWorld().getBlockAt(here).getState());
 						}
 					}
 				}
 			}
 			sender.sendMessage(ChatColor.AQUA + "Blocks successfully added.");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spleefRemoveBlocks")){
 			if (!(sender instanceof Player)){
 				sender.sendMessage("This command can only be used in-game.");
 				return true;
 			}
 			Selection selection = we.getSelection((Player)sender);
 			if (selection == null){
 				sender.sendMessage(ChatColor.RED + "Make a WorldEdit selection first.");
 				return true;
 			}
 			if (args.length != 1){
 				return false;
 			}
 			if (!arenaBlockLocations.containsKey(args[0])){
 				sender.sendMessage(ChatColor.RED + "Arena " + args[0] + " does not exist. You can add it by typing /spleefAddArena " + args[0] + ".");
 				return true;
 			}
 			for(int i = selection.getMinimumPoint().getBlockX(); i <= selection.getMaximumPoint().getBlockX(); i++){
 				for(int j = selection.getMinimumPoint().getBlockY(); j <= selection.getMaximumPoint().getBlockY(); j++){
 					for(int k = selection.getMinimumPoint().getBlockZ(); k <= selection.getMaximumPoint().getBlockZ(); k++){
 						Location here = new Location(selection.getWorld(), i, j, k);
						if (arenaBlockLocations.containsValue(here)) arenaBlockLocations.get(args[0]).remove(here);
 						if (blocks.containsKey(here)) blocks.remove(here);
 					}
 				}
 			}
 			sender.sendMessage(ChatColor.AQUA + "Blocks successfully removed.");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spleefReset")){
 			if (args.length != 1){
 				return false;
 			}
 			if (!arenaBlockLocations.containsKey(args[0])){
 				sender.sendMessage(ChatColor.RED + "Arena " + args[0] + " does not exist. You can add it by typing /spleefAddArena " + args[0] + ".");
 				return true;
 			}
 			for (Location loc : arenaBlockLocations.get(args[0])){
 				loc.getBlock().setType(blocks.get(loc).getType());
 				loc.getBlock().setData(blocks.get(loc).getRawData());
 			}
 			sender.sendMessage(ChatColor.AQUA + "Arena " + args[0] + " successfully reset.");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spleefAddArena")){
 			if (args.length != 1){
 				return false;
 			}
 			if (arenaBlockLocations.containsKey(args[0])){
 				sender.sendMessage(ChatColor.RED + "Arena " + args[0] + " already exists. You can add blocks to it by typing /spleefAddBlocks " + args[0] + ".");
 				return true;
 			}
 			arenaBlockLocations.put(args[0], new ArrayList<Location>());
 			sender.sendMessage(ChatColor.AQUA + "Arena " + args[0] + " successfully added.");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spleefRemoveArena")){
 			if (args.length != 1){
 				return false;
 			}
 			if (!arenaBlockLocations.containsKey(args[0])){
 				sender.sendMessage(ChatColor.RED + "Arena " + args[0] + " doesn't exist. You don't need to remove it.");
 				return true;
 			}
 			arenaBlockLocations.remove(args[0]);
 			sender.sendMessage(ChatColor.AQUA + "Arena " + args[0] + " successfully removed.");
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("spleefList")){
 			String output = ChatColor.AQUA + "";
 			for(String x: arenaBlockLocations.keySet()){
 				output += x + ", ";
 			}
 			sender.sendMessage(output.substring(0,output.length() - 2));
 			return true;
 		}
 		return false;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && blocks.containsKey(event.getClickedBlock().getLocation()) && event.getPlayer().hasPermission("evan1026.spleef.instabreak")){
 			event.getClickedBlock().setTypeId(0);
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		if (!blocks.isEmpty() && blocks.containsValue(event.getBlock().getLocation()) && !event.getPlayer().hasPermission("evan1026.spleef.break")){
 			event.setCancelled(true);
 		}
 	}
 	
 	private void loadFiles(){
 		List<String> file;
 		try {
 			file = Files.readAllLines(Paths.get("plugins/SpleefPlugin/arenaBlocks.txt"), Charset.defaultCharset());
 		} catch (IOException e) {
 			log.info("Save file not found. It will be created next time this plugin is disabled.");
 			return;
 		}
 		for(String x : file){
 			String arena = x.substring(0,x.indexOf(":"));
 			x = x.substring(x.indexOf(":") + 1);
 			arenaBlockLocations.put(arena, new ArrayList<Location>());
 			for(String y : x.split(";")){
 				String world = y.substring(0, y.indexOf(","));
 				y = y.substring(y.indexOf(",") + 1);
 				double locX = Double.parseDouble(y.substring(0, y.indexOf(",")));
 				y = y.substring(y.indexOf(",") + 1);
 				double locY = Double.parseDouble(y.substring(0, y.indexOf(",")));
 				y = y.substring(y.indexOf(",") + 1);
 				double locZ = Double.parseDouble(y.substring(0, y.indexOf(",")));
 				y = y.substring(y.indexOf(",") + 1);
 				int blockID = Integer.parseInt(y.substring(0, y.indexOf(",")));
 				y = y.substring(y.indexOf(",") + 1);
 				byte data = Byte.parseByte(y);
 				Location loc = new Location(getServer().getWorld(world), locX, locY, locZ);
 				arenaBlockLocations.get(arena).add(loc);
 				Block block = loc.getBlock();
 				BlockState tempState = block.getState();
 				block.setTypeId(blockID);
 				block.setData(data);
 				blocks.put(loc, block.getState());
 				block.setTypeId(tempState.getTypeId());
 				block.setData(tempState.getRawData());
 			}
 		}
 	}
 	
 	private void saveFiles(){
 		List<String> file = new ArrayList<String>();
 		File path = new File("plugins/SpleefPlugin");
 		path.mkdirs();
 		path = new File("plugins/SpleefPlugin/arenaBlocks.txt");
 		try {
 			path.createNewFile();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		for(String x : arenaBlockLocations.keySet()){
 			String tempString = x + ":";
 			for(Location loc : arenaBlockLocations.get(x)){
 				tempString += loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + blocks.get(loc).getTypeId() + "," + blocks.get(loc).getRawData() + ";";
 			}
 			file.add(tempString.substring(0,tempString.length() - 1));
 		}
 		try {
 			Files.write(Paths.get("plugins/SpleefPlugin/arenaBlocks.txt"), file, Charset.defaultCharset());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
