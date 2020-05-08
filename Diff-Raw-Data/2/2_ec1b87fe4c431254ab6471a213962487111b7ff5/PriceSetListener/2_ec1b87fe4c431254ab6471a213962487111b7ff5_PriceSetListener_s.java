 package com.archmageinc.RealStore;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.MaterialData;
 
 public class PriceSetListener implements Listener {
 
 	private RealStore plugin;
 	private Player owner;
 	private Integer cost;
 	private MaterialData type;
 	private boolean setDefault	=	false;
 	
 	/**
 	 * Constructor of event listener if the MaterialData is already known
 	 * 
 	 * @param instance The main plugin
 	 * @param player The player we are listening for
 	 * @param price The price to set
 	 * @param material The material to set the price on
 	 */
 	public PriceSetListener(RealStore instance,Player player,Integer price,MaterialData material){
 		plugin		=	instance;
 		owner		=	player;
 		cost		=	price;
 		type		=	material;
 		plugin.addSetting(player);
 	}
 	
 	/**
 	 * Constructor of event listener if an item in hand will be used to set the price
 	 * 
 	 * @param instance The main plugin
 	 * @param player The player we are listening for
 	 * @param price The price to set
 	 */
 	public PriceSetListener(RealStore instance,Player player,Integer price){
 		plugin		=	instance;
 		owner		=	player;
 		cost		=	price;
 		plugin.addSetting(player);
 	}
 	
 	/**
 	 * Constructor of event listener if we are setting the default price
 	 * 
 	 * @param instance The main plugin
 	 * @param player The player we are listening for
 	 * @param price The price to set default
 	 * @param def True to set the default price, false to set price with item in hand
 	 */
 	public PriceSetListener(RealStore instance,Player player,Integer price,boolean def){
 		plugin		=	instance;
 		owner		=	player;
 		cost		=	price;
 		setDefault	=	def;
 		plugin.addSetting(player);
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		//We don't care about players other than the owner
 		if(!event.getPlayer().equals(owner))
 			return;
 		//We don't care about interactions with non-chests
 		if(!(event.getClickedBlock().getState() instanceof Chest))
 			return;
 		
 		Chest chest	=	(Chest) event.getClickedBlock().getState();
 		
 		//DoubleChests are very broken right now
 		if(!(chest.getInventory().getHolder() instanceof Chest)){
 			plugin.sendPlayerMessage(owner, ChatColor.DARK_BLUE+"Warning: "+ChatColor.WHITE+"Double chests cannot be used currently.");
 			return;
 		}
 		
 		//We must set the price on a store
 		if(!plugin.isStore(chest)){
 			plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"That is not a store!");
 			return;			
 		}
 		
 		//We must set the price on a store we own
 		if(!plugin.getStoreOwner(chest).equals(owner)){
 			plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"That is not your store!");
 			return;	
 		}
 		
 		/**
 		 * Setting Default Price
 		 */
 		if(type==null && setDefault){
 			if(plugin.setDefaultPrice(owner, chest, cost))
 				plugin.sendPlayerMessage(owner, ChatColor.GREEN+"Setting the default price in this store to "+cost+" gold nuggets.");
 			else
 				plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"Unable to set the default price to "+cost+" for that chest. Try again.");
 			
 			plugin.removeSetting(owner);
 			event.getHandlers().unregister(this);
 			return;
 		}
 		
 		//If we don't already have an item type we must have the item in our hand
		if(type==null && event.getItem().getType()==null){
 			plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"You must have the item whos price you wish to set in your hand!");
 			return;	
 		}else{
 			type	=	event.getItem().getData();
 		}
 		
 		
 		/**
 		 * Setting Item Price
 		 */
 		try{
 			if(plugin.setPrice(owner, chest, type, cost))
 				plugin.sendPlayerMessage(owner, ChatColor.GREEN+"Setting the price of "+ChatColor.WHITE+type.toString()+ChatColor.GREEN+" to "+ChatColor.WHITE+cost+ChatColor.GREEN+" gold nuggets for that store.");
 			else
 				plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Error: "+ChatColor.WHITE+"Unable to set the price of "+type.toString()+" to "+cost+" for that chest. Try again.");
 		}catch(NullPointerException e){
 			plugin.sendPlayerMessage(owner, ChatColor.GREEN+"Setting the price of "+ChatColor.WHITE+event.getItem().getType().toString()+ChatColor.GREEN+" to "+ChatColor.WHITE+cost+ChatColor.GREEN+" gold nuggets for that store.");
 		}
 		plugin.removeSetting(owner);
 		event.getHandlers().unregister(this);
 	}
 }
