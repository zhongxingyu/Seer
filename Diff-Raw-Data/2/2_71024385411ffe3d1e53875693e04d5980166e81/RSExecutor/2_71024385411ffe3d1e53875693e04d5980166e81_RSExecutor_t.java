 package com.archmageinc.RealStore;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.material.MaterialData;
 
 public class RSExecutor implements CommandExecutor {
 
 	private RealStore plugin;
 	
 	public RSExecutor(RealStore instance){
 		plugin	=	instance;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,String[] args){
 		if(cmd.getName().equalsIgnoreCase("rs") || cmd.getName().equalsIgnoreCase("RealStore")){
 			if(!(sender instanceof Player)){
 				plugin.logMessage("RealStore interactions may only be used by Players!");
 				return true;
 			}
 			
 			Player player	=	(Player) sender;
 			
			if(!player.hasPermission("RealStore.*")){
 				plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"You do not have permission to do that.");
 				return true;
 			}
 			
 			if(args.length<1){
 				plugin.sendPlayerMessage(player,ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"No RealStore command found! Please use '/rs help' for proper RealStore usage. ");
 				return true;
 			}
 			
 			String rsCommand	=	args[0].toLowerCase();
 			
 /************************************************************
  * Getting Help
  ************************************************************/
 			if(rsCommand.equals("help")){
 				plugin.sendHelpInfo(player, (args.length<2 ? null : args[1]) );
 				return true;
 			}
 			
 			
 /**********************************************************
  * Setting Stores
  **********************************************************/
 			if(rsCommand.equals("store")){
 				if(args.length<2){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Do you wish to add or remove a store? Use '/rs help store' for more information.");
 					return true;
 				}
 				String storeCommand	=	args[1].toLowerCase();
 				/****************************
 				 * Adding a store
 				 ****************************/
 				if(storeCommand.equals("add")){
 					if(!plugin.hasCoffer(player)){
 						plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" You have no coffers! Use '/rs help coffer' for more information.");
 						return true;
 					}
 					
 					plugin.sendPlayerMessage(player, "Click on a chest to create a store.");
 					plugin.getServer().getPluginManager().registerEvents(new StoreManagementListener(plugin, player),plugin);
 					return true;
 				}
 				/*****************************
 				 * Removing a store
 				 *****************************/
 				if(storeCommand.equals("remove")){
 					plugin.getServer().getPluginManager().registerEvents(new StoreManagementListener(plugin,player,true), plugin);
 					plugin.sendPlayerMessage(player, "Click on the chest store to remove the store.");
 					return true;
 				}
 				
 				plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Unknown store command! Use '/rs help store' for more information.");
 				return true;
 			}
 			
 /**************************************************************
  * Setting Prices
  **************************************************************/
 			if(rsCommand.equals("price")){
 				if(args.length<2){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" No price found. Use '/rs help price' for proper RealStore price setting usage.");
 					return true;
 				}
 				
 				if(!plugin.hasCoffer(player)){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" You do not have any coffers. Create a coffer first. Use '/rs help coffer' for more.");
 					return true;
 				}
 				
 				if(!plugin.hasStore(player)){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" You do not have any stores on which to set prices. Create a store first. Use '/rs help store' for more.");
 					return true;
 				}
 				
 				try{
 					Integer price	=	Integer.parseInt(args[1]);
 					if(price<=0){
 						plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" The price must be greater than zero!");
 						return true;
 					}
 					
 					/********************************
 					 * Command setting price
 					 ********************************/
 					if(args.length>2){
 						String type		=	args[2];
 						try{
 							Byte data				=	args.length>3 ? (byte) Integer.parseInt(args[3]) : (byte) 0;
 							
 							if(Material.matchMaterial(type)==null){
 								if(type.equalsIgnoreCase("default")){
 									/******************************
 									 * Default setting price
 									 ******************************/
 									plugin.sendPlayerMessage(player, "Click on the chest store to set the default price to "+price+" gold nuggets");
 									plugin.getServer().getPluginManager().registerEvents(new PriceSetListener(plugin,player,price,true), plugin);
 									return true;
 								}else{
 									plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Unknown material used to set the price. Use '/rs help material' to list material names.");
 									return true;
 								}
 							}
 							
 							MaterialData material	=	new MaterialData(Material.matchMaterial(type),data);
 							plugin.sendPlayerMessage(player, "Click on the chest store to set the price of "+material.toString()+" to a price of "+price+" gold nuggets");
 							plugin.getServer().getPluginManager().registerEvents(new PriceSetListener(plugin,player,price,material), plugin);
 							return true;
 							
 						}catch(NumberFormatException e){
 							plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Invalid material data. Use '/rs help material' to list material names and data values.");
 							return true;
 						}
 					}
 					
 					/***************************
 					 * Tap setting price
 					 ***************************/
 					plugin.getServer().getPluginManager().registerEvents(new PriceSetListener(plugin,player,price), plugin);
 					plugin.sendPlayerMessage(player, "Click on the chest store with an item to set the price of that item to "+price+" gold nuggets.");
 					return true;
 					
 				}catch(NumberFormatException e){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Improper price. Use '/rs help price' for proper RealStore price setting usage.");
 					return true;
 				}
 			}
 			
 /********************************************************
  * Setting Coffers
  *******************************************************/
 			if(rsCommand.equals("coffer")){
 				if(args.length<2){
 					plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Do you wish to add or remove a coffer? Use '/rs help coffer' for more information.");
 					return true;
 				}
 				String cofferCommand	=	args[1].toLowerCase();
 				/****************************
 				 * Adding a coffer
 				 ****************************/
 				if(cofferCommand.equals("add")){
 					plugin.getServer().getPluginManager().registerEvents(new CofferManagementListener(plugin,player,false), plugin);
 					plugin.sendPlayerMessage(player, "Click on a chest to add it to your coffers.");
 					return true;
 				}
 				/*****************************
 				 * Removing a coffer
 				 *****************************/
 				if(cofferCommand.equals("remove")){
 					plugin.getServer().getPluginManager().registerEvents(new CofferManagementListener(plugin,player,true), plugin);
 					plugin.sendPlayerMessage(player, "Click on a chest to remove it from your coffers.");
 					return true;
 				}
 				
 				plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Unknown coffer command! Use '/rs help coffer' for more information.");
 				return true;
 				
 			}
 			/**
 			 * TODO: Add a method for store owners to check prices
 			 */
 			
 			
 /**********************************************************
  * Unknown RealStore Command
  **********************************************************/
 			plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+" Unknown RealStore command! Use '/rs help' for more information.");
 			return true;
 		}
 		
 		return false;
 	}
 
 }
