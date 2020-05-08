 package com.cyprias.Lifestones;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.cyprias.Lifestones.Attunements.Attunement;
 import com.cyprias.Lifestones.Config.lifestoneStructure;
 import com.cyprias.Lifestones.Events.attuneTask;
 import com.cyprias.Lifestones.Lifestones.lifestoneLoc;
 
 public class Commands implements CommandExecutor {
 	private Lifestones plugin;
 
 	public Commands(Lifestones plugin) {
 		this.plugin = plugin;
 	}
 
 	static public String L(String key) {
 		return Lifestones.L(key);
 	}
 	static public String F(String key, Object... args) {
 		return Lifestones.F(key, args);
 	}
 	
 	String GREEN = ChatColor.GREEN.toString();
 	String RESET = ChatColor.RESET.toString();
 	String GRAY = ChatColor.GRAY.toString();
 	String YELLOW = ChatColor.YELLOW.toString();
 	
 	public class recallTask implements Runnable {
 		Player player;
 
 		int pX, pY, pZ;
 
 		public recallTask(Player player) {
 			this.player = player;
 
 			pX = player.getLocation().getBlockX();
 			pY = player.getLocation().getBlockY();
 			pZ = player.getLocation().getBlockZ();
 
 			plugin.sendMessage(player,GRAY+F("recallingToLifestone", GREEN + Config.recallDelay + GRAY));
 		}
 
 		public void run() {
 			if (player.getLocation().getBlockX() != pX || player.getLocation().getBlockY() != pY || player.getLocation().getBlockZ() != pZ) {
 				plugin.sendMessage(player,GRAY+F("recallingToLifestone", GREEN + Config.recallDelay + GRAY));
 				//plugin.sendMessage(player, GRAY+L("movedTooFarAttunementFailed"));
 				return;
 			}
 
 			Attunement attunement = Attunements.get(player);
 
 			player.teleport(attunement.loc);
 			plugin.sendMessage(player, GRAY+L("recalledToLifestone"));
 			
 			
 			plugin.playerProtections.put(player.getName(), plugin.getUnixTime() + Config.protectPlayerAfterRecallDuration);
 			//protectPlayerAfterRecallDuration
 		}
 	}
 
 	
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// final String message = getFinalArg(args, 0);
 		// plugin.info(sender.getName() + ": /" + cmd.getName() + " " +
 		// message);
 
 		if (commandLabel.equals("lifestone")) {
 			if (args.length > 0) {
 				return onCommand(sender, cmd, "lifestones", args);
 			}
 			if (!hasCommandPermission(sender, "lifestones.recall")) {
 				return true;
 			}
 			
 			
 			
 			if (!(Attunements.containsKey(sender.getName()))) {
 				plugin.sendMessage(sender, GRAY+L("notAttunedYet"));
 				return true;
 			}
 			Player player = (Player) sender;
 
 			recallTask task = new recallTask(player);
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, Config.recallDelay*20L);
 			return true;
 
 		} else if (commandLabel.equals("lifestones")) {
 			
 			if (args.length > 0) {
 				if (args[0].equalsIgnoreCase("create")) {
 					if (!hasCommandPermission(sender, "lifestones.create")) {
 						return true;
 					}
 					
 					Player player = (Player) sender;
 
 					Block pBlock = player.getLocation().getBlock();
 					if (plugin.isProtected(pBlock)) {
 						plugin.sendMessage(sender, GRAY+L("tooCloseToAnotherLifestone"));
 						return true;
 					}
 
 					Block rBlock;
 
 					lifestoneStructure lsStructure;
 					BlockPlaceEvent e;
 
 					if (Config.callBlockPlaceEvent == true){
 						for (int i = 0; i < Config.structureBlocks.size(); i++) {
 							lsStructure = Config.structureBlocks.get(i);
 							rBlock = pBlock.getRelative(lsStructure.rX, lsStructure.rY, lsStructure.rZ);
 							//don't change the block type, try placing the blocks down again to check if user has permission in the area to build. 
 							e = new BlockPlaceEvent(rBlock, rBlock.getState(), pBlock, player.getItemInHand(), player, false);
 							player.getServer().getPluginManager().callEvent(e);
 							
 							
 							if (e.isCancelled()){
 								plugin.sendMessage(sender, GRAY+L("anotherPluginBlockingCreation"));
 								return true;
 							}
 						}
 					}
 					for (int i = 0; i < Config.structureBlocks.size(); i++) {
 						lsStructure = Config.structureBlocks.get(i);
 						rBlock = pBlock.getRelative(lsStructure.rX, lsStructure.rY, lsStructure.rZ);
 
 						
 						if (Config.callBlockPlaceEvent == true){
 							e = new BlockPlaceEvent(rBlock, rBlock.getState(), pBlock, player.getItemInHand(), player, false);
 							e.getBlock().setTypeId(lsStructure.bID);
 							e.getBlock().setData(lsStructure.bData);
 
 							player.getServer().getPluginManager().callEvent(e);
 						}else{
 							rBlock.setTypeId(lsStructure.bID);
 							rBlock.setData(lsStructure.bData);
 						}
 					}
 					
 					
 					//TP player above the device
 					for (int y=1; y < (256-pBlock.getY()); y++){
 						rBlock = pBlock.getRelative(0, y, 0);
 						if (rBlock.getTypeId() == 0){
 							player.teleport(new Location(player.getWorld(), rBlock.getX() + .5, rBlock.getY() + 1, rBlock.getZ() + .5));
 							break;
 						}
 					}
 					
 					plugin.regsterLifestone(new lifestoneLoc(pBlock.getWorld().getName(), pBlock.getX(), pBlock.getY(), pBlock.getZ()));
 					Database.saveLifestone(pBlock.getWorld().getName(), pBlock.getX(), pBlock.getY(), pBlock.getZ(), Config.preferAsyncDBCalls);
 
 					plugin.sendMessage(sender, GRAY+L("lifestoneCreated"));
 					
 					return true;
 				}	else if (args[0].equalsIgnoreCase("reload")) {
 					if (!hasCommandPermission(sender, "lifestones.reload")) {
 						return true;
 					}
 					
 					plugin.getPluginLoader().disablePlugin(plugin);
 					plugin.getPluginLoader().enablePlugin(plugin);
 
 					plugin.sendMessage(sender, GRAY+L("pluginReloaded"));
 					return true;
 				} else if (args[0].equalsIgnoreCase("randomtp")) {
 					if (!hasCommandPermission(sender, "lifestones.randomtp")) {
 						return true;
 					}
 					Player player = (Player) sender;
 					
 					Location tpLoc = plugin.getRandomLocation(player.getWorld());
 					
 					if (tpLoc != null){
 						player.teleport(tpLoc);
 						plugin.sendMessage(sender, GRAY+F("teleportingToCoordinates", GREEN + tpLoc.getBlockX() + GRAY, GREEN + tpLoc.getBlockY() + GRAY, GREEN + tpLoc.getBlockZ() + GRAY));
 						
 						return true;
 					}else{
 						plugin.sendMessage(player, GRAY + L("cantFindSafeBlock"));
 						
 						return true;
 					}
 				} else if (args[0].equalsIgnoreCase("near")) {
 					if (!hasCommandPermission(sender, "lifestones.near")) {
 						return true;
 					}
 					
 					List<lifestoneDistance> lifestones = new ArrayList<lifestoneDistance>();
 					
 					
 					//CompareWarps comparator = new CompareWarps();
 					//Collections.sort(warps, comparator);
 					
 					Player player = (Player) sender;
 					lifestoneLoc ls;
 					double dist;
 					for (int i = 0; i < plugin.lifestoneLocations.size(); i++) {
 						ls = plugin.lifestoneLocations.get(i);
 						if (player.getWorld().getName().equals(ls.world)){
 							dist = player.getLocation().distance(new Location(player.getWorld(), ls.X, ls.Y, ls.Z));
 							lifestones.add(new lifestoneDistance(ls.world, ls.X, ls.Y, ls.Z, dist));
 						}
 					}
 					if (lifestones.size() > 0){
 					
 						compareLifestones comparator = new compareLifestones();
 						Collections.sort(lifestones, comparator);
 					
 						double pX = player.getLocation().getX();
 						double pZ = player.getLocation().getZ();
 						
 						String sDir = MathUtil.DegToDirection(MathUtil.AngleCoordsToCoords(pX, pZ, lifestones.get(0).X, lifestones.get(0).Z));
 
 						//plugin.sendMessage(sender, "Nearest lifestone is at " + lifestones.get(0).X + " " + lifestones.get(0).Y + " " + lifestones.get(0).Z + ", " + Math.round(lifestones.get(0).distance) + " blocks " + sDir + ".");
 						plugin.sendMessage(player, GRAY + F("nearestLifestoneAt", GREEN + lifestones.get(0).X + GRAY, GREEN + lifestones.get(0).Y + GRAY, GREEN + lifestones.get(0).Z + GRAY, GREEN + Math.round(lifestones.get(0).distance) + GRAY, GREEN + sDir + GRAY));
 						
 						
 						if (Config.lookAtNearestLS == true){
 							Location pLoc = player.getLocation();
 							Location lsLoc = new Location(player.getWorld(), lifestones.get(0).X + 0.5,lifestones.get(0).Y,lifestones.get(0).Z + 0.5);
 	
 							float yaw = MathUtil.getLookAtYaw(pLoc, lsLoc) + 90;
 							pLoc.setYaw(yaw);
 							
 							double motX = (lifestones.get(0).X + 0.5) - player.getLocation().getX();
 							double motY = (lifestones.get(0).Y) - player.getLocation().getY();
 							double motZ = (lifestones.get(0).Z + 0.5) - player.getLocation().getZ();
 							
 							float pitch = MathUtil.getLookAtPitch(motX, motY, motZ);
 						
 							pLoc.setPitch(pitch);
 							
 							player.teleport(pLoc);
 						}
 						
 					}else{
 						plugin.sendMessage(sender, GRAY + L("noLifestoneNear"));
 					}
 					return true;
 				} else if (args[0].equalsIgnoreCase("list")) {
 					if (!hasCommandPermission(sender, "lifestones.list")) {
 						return true;
 					}
 					
 					int page = 1;
 					if (args.length > 1) {// && args[1].equalsIgnoreCase("compact"))
 						if (plugin.isInt(args[1])) {
 							page = Math.abs(Integer.parseInt(args[1]));
 						} else {
 							plugin.sendMessage(sender, GRAY + F("invalidPageNumber", args[1]));
 							return true;
 						}
 					}
 					
 					
 					
 					int rows = plugin.lifestoneLocations.size();
 					
 					if (rows == 0 ){
 						plugin.sendMessage(sender, GRAY + L("noRegisteredLifestones"));
 						return true;
 					}
 					
 					int maxPages = (int) Math.ceil((float) rows / (float) Config.rowsPerPage);
 
 					if (rows > Config.rowsPerPage){
 						//plugin.sendMessage(sender, "Page " + (page) + "/" + (maxPages));
 						plugin.sendMessage(sender, GRAY+F("page", GREEN+page+GRAY, GREEN+maxPages+GRAY));
 					}
 					
 					int start = ((page-1) * Config.rowsPerPage);
 					int end = start + Config.rowsPerPage;
 					if (end > rows)
 						end = rows;
 					
 					lifestoneLoc ls; 
 					for (int i = start; i < end; i++) {
 						ls = plugin.lifestoneLocations.get(i);
						plugin.sendMessage(sender, GRAY+F("lifestoneIndex", GREEN+(i+1)+GRAY, GREEN+ls.world+GRAY, GREEN+ls.X+GRAY, GREEN+ls.Y+GRAY, GREEN+ls.Z+GRAY), false);
 
 					}
 					return true;
 				} else if (args[0].equalsIgnoreCase("tp")) {
 					if (!hasCommandPermission(sender, "lifestones.tp")) {
 						return true;
 					}
 					int lsID;
 					if (args.length > 1) {// && args[1].equalsIgnoreCase("compact"))
 						if (plugin.isInt(args[1])) {
 							lsID = Math.abs(Integer.parseInt(args[1]));
 						} else {
 							plugin.sendMessage(sender, GRAY+F("invalidID",args[1]));
 							return true;
 						}
 					}else{
 						plugin.sendMessage(sender, GRAY+L("includeIndexNum"));
 						return true;
 					}
 						
 					lsID-=1;
 					
 					if (lsID > plugin.lifestoneLocations.size()){
 						plugin.sendMessage(sender, GRAY+F("indexTooHigh",GREEN+lsID+GRAY));
 						return true;
 					}
 					
 					lifestoneLoc lsLoc = plugin.lifestoneLocations.get(lsID);
 					
 					Block lsBlock = plugin.getServer().getWorld(lsLoc.world).getBlockAt(lsLoc.X, lsLoc.Y, lsLoc.Z);
 					Block rBlock;
 					Player player = (Player) sender;
 					for (int y=1; y < (256-lsBlock.getY()); y++){
 						rBlock = lsBlock.getRelative(0, y, 0);
 						if (rBlock.getTypeId() == 0){
 							
 							player.teleport(new Location(player.getWorld(), rBlock.getX() + .5, rBlock.getY() + 1, rBlock.getZ() + .5));
 							//plugin.sendMessage(player, ChatColor.GRAY+"Teleporting to " + ChatColor.GREEN + rBlock.getX() + ChatColor.GRAY+"x" +ChatColor.GREEN + rBlock.getZ() + ChatColor.GRAY+ ".");
 
 							plugin.sendMessage(sender, GRAY+F("teleportingToCoordinates", GREEN + rBlock.getX() + GRAY, GREEN + rBlock.getY() + GRAY, GREEN + rBlock.getZ() + GRAY));
 							
 							
 							return true;
 						}
 					}
 						
 					
 					
 				}
 			}
 			
 			plugin.sendMessage(sender, F("nameAndVersion", GREEN+plugin.pluginName +GRAY, GREEN+plugin.getDescription().getVersion()+GRAY));
 
 			if (plugin.hasPermission(sender, "lifestones.recall") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone" + GRAY+" - " + L("lifestoneDesc"), true, false);
 			
 			
 			
 			if (plugin.hasPermission(sender, "lifestones.create") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone create" + GRAY+" - " + L("createALifestone"), true, false);
 			
 			if (plugin.hasPermission(sender, "lifestones.list") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone list" + GRAY+" - " + L("lifeAllLifestones"), true, false);
 			
 			if (plugin.hasPermission(sender, "lifestones.tp") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone tp [#]" + GRAY+" - " + L("tpToLifestone"), true, false);
 			
 			if (plugin.hasPermission(sender, "lifestones.near") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone near" + GRAY+" - " + L("showNearestLifestone"), true, false);
 			
 			if (plugin.hasPermission(sender, "lifestones.reload") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone reload" + GRAY+" - " + L("reloadThePlugin"), true, false);
 			
 			if (plugin.hasPermission(sender, "lifestones.randomtp") && (sender instanceof Player))
 				plugin.sendMessage(sender, GREEN+"/lifestone randomtp" + GRAY+" - " + L("tpToRandomLoc"), true, false);
 			
 			return true;
 		}
 
 		return false;
 	}
 	
 
 	
 	public boolean hasCommandPermission(CommandSender player, String permission) {
 		if (plugin.hasPermission(player, permission)) {
 			return true;
 		}
 		plugin.sendMessage(player, ChatColor.GRAY +F("noPermission", YELLOW+permission+GRAY));
 		return false;
 	}
 
 	public class lifestoneDistance extends lifestoneLoc {
 		double distance;
 		public lifestoneDistance(String world, int X, int Y, int Z, double distance) {
 			super(world, X, Y, Z);
 			// TODO Auto-generated constructor stub
 			this.distance = distance;
 		}
 
 	}
 	
 	/**/
 	public class compareLifestones implements Comparator<lifestoneDistance> {
 
 		@Override
 		public int compare(lifestoneDistance o1, lifestoneDistance o2) {
 			if (o1.distance > o2.distance) {
 				return +1;
 			} else if (o1.distance < o2.distance) {
 				return -1;
 			} else {
 				return 0;
 			}
 		}
 
 	}
 	
 
 	
 }
