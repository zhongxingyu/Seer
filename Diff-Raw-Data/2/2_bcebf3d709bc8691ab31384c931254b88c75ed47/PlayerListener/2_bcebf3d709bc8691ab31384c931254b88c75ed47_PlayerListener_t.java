 package com.minecarts.verrier.tikitoolkit.listener;
 
 import com.minecarts.verrier.tikitoolkit.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.player.*;
 import org.bukkit.entity.Player;
 
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener{
 
 	TikiToolkit plugin;
 	
 	public PlayerListener(TikiToolkit instance)
 	{
 		plugin = instance;
 	}
 
 	
 	public void onPlayerJoin(PlayerEvent event){
 		//Do we care?
 	}
 	
 	public void onPlayerAnimation(PlayerAnimationEvent event) {
 		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
 			this.doToolCmd(event.getPlayer(),"click_left");
 		}
 	}
 	
 	public void onPlayerItem(PlayerItemEvent event){
 		this.doToolCmd(event.getPlayer(),"click_right");
 	}
 	
 	public void onItemHeldChange(PlayerItemHeldEvent event){
 		Player player = event.getPlayer();
 		int slot = event.getNewSlot();
 		String name = plugin.config.getString("admins."+player.getName()+".slot_"+slot+".name");
 		String type = getToolTypeAtSlot(player, slot);
 		if(name != null){
 			//Only display the selected tool message if they have the have the correct item in hand
 			if (player.getInventory().getItem(slot).getType() == Material.getMaterial(type)){
				player.sendMessage(String.format("Tiki: %s%s%s selected",ChatColor.GOLD,name,ChatColor.WHITE));
 			}
 		}
 	}
 	
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
 		Runnable setInventory = new setInventory(player.getName());
 		//Since we don't have the actual player object that's going to respawn
 		//	lets fire off a task to do later? Is this the best way to do it?
 		if(plugin.config.getBoolean("admins."+player.getName()+".respawn_wands", false)){
 			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, setInventory,1);
 		}
 
 	}
 	
 	public class setInventory implements Runnable {
 		private String playerName;
 		public setInventory(String playerName){
 			this.playerName = playerName;
 		}
         public void run(){
         	//Give the players their admin kit on respawn
         	Player player = plugin.getServer().getPlayer(playerName);
         	player.sendMessage("The tiki gods have restored your admin tools.");
     		for(int i=0;i<9;i++){
     			String type = getToolTypeAtSlot(player,i);
     			if(type != null){
     				//Assign the item
     				player.getInventory().setItem(i, new ItemStack(Material.valueOf(type),1));
     			}
     		}
         }
     }
 
 	
 	private void doToolCmd(Player player, String clickType){		
 		int slot = player.getInventory().getHeldItemSlot();
 		String type = getToolTypeAtSlot(player, slot);
 		
 		if(type != null){
 			//Check to see if the item in the hand is their configured wand
 			if (player.getInventory().getItemInHand().getType() == Material.getMaterial(type)){
 				//Execute the command
 				String cmd = plugin.config.getString("admins."+player.getName()+".slot_"+slot+"."+clickType);
 				if(cmd!= null){
 					player.performCommand(cmd);
 				}
 			}
 		}
 	}
 	
 	private String getToolTypeAtSlot(Player player, int slot){
 		return plugin.config.getString("admins."+player.getName()+".slot_"+slot+".type");
 	}
 }
