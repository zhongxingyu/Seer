 package me.arno.blocklog.commands;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.logs.LoggedBlock;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class CommandWand extends BlockLogCommand {
 	public CommandWand(BlockLog plugin) {
 		super(plugin, "blocklog.wand");
 	}
 	
 	public boolean execute(Player player, Command cmd, String[] args) {
 		if(args.length > 1) {
 			player.sendMessage(ChatColor.WHITE + "/bl wand [target]");
 			return true;
 		}
 		
 		if(!hasPermission(player)) {
 			player.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		if(args.length == 1) {
 			if(args[0].equalsIgnoreCase("target")) {
 				getBlockEdits(player, player.getTargetBlock(null, 0));
 				return true;
 			}
 		}
 		
		HashMap<String, ItemStack> playerItemStack = plugin.playerItemStack;
		HashMap<String, Integer> playerItemSlot = plugin.playerItemSlot;
		
 		Material wand = Material.getMaterial(getConfig().getInt("blocklog.wand"));
 		
 		if(player.getInventory().contains(wand) && !playerItemStack.containsKey(player.getName())) {
			log.info("First");
 			if(plugin.users.isEmpty()) {
 				plugin.users.add(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand enabled!");
 			} else if(plugin.users.contains(player.getName())) {
 				plugin.users.remove(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand disabled!");
 			} else {
 				plugin.users.add(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand enabled!");
 			}
 		} else if(!player.getInventory().contains(wand) && plugin.users.contains(player.getName())) {
			log.info("Second");
 			plugin.users.remove(player.getName());
 			player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand disabled!");
 		} else {
			log.info("Third");
 			if(plugin.users.isEmpty()) {
				log.info("First");
 				playerItemStack.put(player.getName(), player.getItemInHand());
 				playerItemSlot.put(player.getName(), player.getInventory().getHeldItemSlot());
 				
 				player.setItemInHand(new ItemStack(wand, 1));
 				
 				plugin.users.add(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand enabled!");
 			} else if(plugin.users.contains(player.getName())) {
				log.info("Second");
 				ItemStack itemStack = playerItemStack.get(player.getName());
 				Material itemInHand = player.getItemInHand().getType();
 				int invSlot = playerItemSlot.get(player.getName());
 				int itemInHandSlot = player.getInventory().getHeldItemSlot();
 				
 				if(itemInHandSlot == invSlot || (itemInHand == wand && itemInHandSlot != invSlot))
 					player.setItemInHand(itemStack);
 				else
 					player.getInventory().setItem(invSlot, itemStack);
 				
 				plugin.users.remove(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand disabled!");
 			} else {
				log.info("Third");
 				playerItemStack.put(player.getName(), player.getItemInHand());
 				playerItemSlot.put(player.getName(), player.getInventory().getHeldItemSlot());
 				
 				player.setItemInHand(new ItemStack(wand, 1));
 				
 				plugin.users.add(player.getName());
 				player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand enabled!");
 			}
 		}
 		return true;
 	}
 	
 	public void getBlockEdits(Player player, Block block) {
 		try {
 			player.sendMessage(ChatColor.DARK_RED + "BlockLog History (" + getConfig().getString("blocklog.results") + " Last Edits)");
 			Integer BlockNumber = 0;
 			Integer BlockCount = 0;
 			Integer BlockSize = plugin.getBlocks().size();
 			Location BlockLocation = block.getLocation();
 			
 			while(BlockSize > BlockNumber)
 			{
 				LoggedBlock LBlock = plugin.getBlocks().get(BlockNumber); 
 				if(LBlock.getX() == BlockLocation.getX() && LBlock.getY() == BlockLocation.getY() && LBlock.getZ() == BlockLocation.getZ() && LBlock.getWorld() == BlockLocation.getWorld()) {
 					if(BlockCount == getConfig().getInt("blocklog.results"))
 						break;
 					
 					Calendar calendar = GregorianCalendar.getInstance();
 					calendar.setTimeInMillis(LBlock.getDate() * 1000);
 					
 					String date =  calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
 					
 					String name = Material.getMaterial(LBlock.getBlockId()).toString();
 					int type = LBlock.getTypeId();
 					
 					if(type == 0)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " broke a " + ChatColor.GOLD + name);
 					else if(type == 1)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " placed a " + ChatColor.GOLD + name);
 					else if(type == 2)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " burned a " + ChatColor.GOLD + name);
 					else if(type == 3 || type == 10 || type == 11 || type == 12)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " blew a " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " up");
 					else if(type == 4)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.DARK_GREEN + "A " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " decayed");
 					else if(type == 5)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " grew a " + ChatColor.GOLD + name);
 					else if(type == 6 || type == 7 || type == 8)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.GOLD + LBlock.getPlayerName() + " (" + LBlock.getEntityName() + ")" + ChatColor.DARK_GREEN + " created a " + ChatColor.GOLD + name);
 					else if(type == 9)
 						player.sendMessage(ChatColor.BLUE + "[" + date + "] " + ChatColor.DARK_GREEN + "A " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " faded");
 					BlockCount++;
 				}
 				BlockNumber++;
 			}
 			
 			if(BlockCount < getConfig().getInt("blocklog.results")) {
 				Connection conn = plugin.conn;
 				Statement stmt = conn.createStatement();
 				
 				Integer x = block.getX();
 				Integer y = block.getY();
 				Integer z = block.getZ();
 				
 				ResultSet rs = stmt.executeQuery("SELECT entity, trigered, block_id, type, FROM_UNIXTIME(date, '%d-%m-%Y %H:%i:%s') AS date FROM blocklog_blocks WHERE x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "' AND world = '" + block.getWorld().getName() + "' ORDER BY date DESC LIMIT " + (getConfig().getInt("blocklog.results") - BlockCount));
 				
 				while(rs.next()) {
 					String name = Material.getMaterial(rs.getInt("block_id")).toString();
 					int type = rs.getInt("type");
 					
 					if(type == 0)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " broke a " + ChatColor.GOLD + name);
 					else if(type == 1)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " placed a " + ChatColor.GOLD + name);
 					else if(type == 2)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " burned a " + ChatColor.GOLD + name);
 					else if(type == 3 || type == 10 || type == 11 || type == 12)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " blew a " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " up");
 					else if(type == 4)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.DARK_GREEN + "A " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " decayed");
 					else if(type == 5)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " grew a " + ChatColor.GOLD + name);
 					else if(type == 6 || type == 7 || type == 8)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.GOLD + rs.getString("trigered") + " (" + rs.getString("entity") + ")" + ChatColor.DARK_GREEN + " created a " + ChatColor.GOLD + name);
 					else if(type == 9)
 						player.sendMessage(ChatColor.BLUE + "[" + rs.getString("date") + "] " + ChatColor.DARK_GREEN + "A " + ChatColor.GOLD + name + ChatColor.DARK_GREEN + " faded");
 				}
 			}
 		} catch(SQLException e) {
 			e.getStackTrace();
 		}
 	}
 }
