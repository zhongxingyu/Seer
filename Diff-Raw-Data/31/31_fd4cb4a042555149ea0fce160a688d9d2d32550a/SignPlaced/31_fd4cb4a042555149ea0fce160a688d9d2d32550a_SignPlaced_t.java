 package net.lotrcraft.strategycraft.listeners;
 
 import net.lotrcraft.strategycraft.Config;
 import net.lotrcraft.strategycraft.buildings.Building;
 import net.lotrcraft.strategycraft.buildings.BuildingManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.material.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignPlaced extends BlockListener {
 
 	public void onSignChange(SignChangeEvent event) {
 
 		String[] lines = event.getLines();
 		Block block = event.getBlock();
 		Sign sign = (Sign) event.getBlock().getState().getData();
 		Chunk chunk = block.getChunk();
 		Block baseBlock = block.getRelative(sign.getAttachedFace());
 		Location blockLoc = baseBlock.getLocation();
 		Player player = event.getPlayer();
 		Building building = null;
 
 		int x = baseBlock.getX();
 		int y = baseBlock.getY();
 		int z = baseBlock.getZ();
 		
 		if (!lines[0].equals("StrategyCraft")){
 			return;
 		}
 		
 		if (sign.isWallSign() && baseBlock.getTypeId() == Config.coreBlock) {
 			if (lines[1].equalsIgnoreCase("Castle")){
 				if (BuildingManager.getCastle(player.getName()) != null) {
 					event.setLine(0, ChatColor.RED + lines[0]);
 					player.sendMessage(ChatColor.DARK_RED + "You already own a Castle!");
 				} else {
 					BuildingManager.createNewCastle(player.getName(), blockLoc);
 					event.setLine(0, ChatColor.GREEN + lines[0]);
 					player.sendMessage(ChatColor.GOLD + "Castle created!");
 				}
 			}
 			else {
 				building = (BuildingManager.getBuildingClass(lines[1])) == null ? null : (BuildingManager.getNewBuilding(lines[1]));
 				
 				if (building == null){
 					event.setLine(0, ChatColor.RED + lines[0]);
 					player.sendMessage(ChatColor.DARK_RED + "Building doesn't exist!");
 				} else{
 					if (BuildingManager.getCastle(player.getName()).addBuilding(building, blockLoc, sign.getAttachedFace())){
 						event.setLine(0, ChatColor.DARK_GREEN + lines[0]);
						player.sendMessage(ChatColor.DARK_GREEN + "Building created!");
 					}
 				}
 			}
 
 		}
 	}
 }
