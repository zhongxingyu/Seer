 package com.shloms.serubin.hatme;
 
 //import java.io.File;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 //import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HatMe extends JavaPlugin{
 
 	public static HatMe plugin;
 	public static Logger log = Logger.getLogger("Minecraft");
 	FileConfiguration config;
	public static String version = 0.4;
 	public static String name = "HatMe";
 	
 	  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		  Player player = (Player) sender;
 		  ItemStack itemHand = player.getItemInHand();
 		  if (commandLabel.equalsIgnoreCase("hat") || commandLabel.equalsIgnoreCase("hatme") || commandLabel.equalsIgnoreCase("hm")){
 			if (checkPermissionBasic(player)) {
 				 if (player.getItemInHand().getTypeId() == 0) {
 			         player.sendMessage(ChatColor.RED + "Please pick a valid item!");
 			         return true;
 				  } else {
 			             PlayerInventory inventory = player.getInventory();
 						 //Gets item from head
 			             ItemStack itemHead = inventory.getHelmet();
 						 //Removes item from hand
 			             inventory.removeItem(itemHand);
 						 //Sets item from hand to head
 			             inventory.setHelmet(itemHand);
 						 //Checks for item in head
 			             if (itemHead.getTypeId() != 0) {
 						 //sets item in head to hand
 			                 inventory.setItemInHand(itemHead);
 			             player.sendMessage(ChatColor.YELLOW + "You now have a hat!");
 							return true;
 						}
 					  } 
 						}
			 return false;
 					  }
 					 
 					  
 		  if (commandLabel.equalsIgnoreCase("unhat") || commandLabel.equalsIgnoreCase("unhatme") || commandLabel.equalsIgnoreCase("unhm")){
 			  if(checkPermissionBasic(player)){
 				  if (player.getInventory().getHelmet().getTypeId() == 0) {                          //If helmet is empty do nothing
 	                  player.sendMessage(ChatColor.RED + "You have no hat to take off!");
 	                  return true;
 				  }else{
 					//ItemStack itemHand = player.getItemInHand();
                     PlayerInventory inventory = player.getInventory();
                     int empty = inventory.firstEmpty();
                    ItemStack none = inventory.getItem(empty);
                     ItemStack itemHead = inventory.getHelmet();                        //Get item in helmet
                     inventory.setHelmet(null);                     // removes item from helmet
                     inventory.setItem(empty, itemHead);              //Sets item from helmet to first open slot
                     return true;
 				  	}  
 		  	}
 		  }
		return false;
 	  }
 	 
 	  
 	 private boolean checkPermissionBasic(Player player) {
 	     if(player.hasPermission("hatme.basic") || player.hasPermission("hatme.*") || player.isOp()) return true;
 	     return false;
 	    }
 	// private boolean checkPermissionGive(Player player) {
 	  //   if(player.hasPermission("hatme.give") || player.hasPermission("hatme.*") || player.isOp()) return true;
 	    // return false;
 	// }
 	    
 	 
 	@Override
 	public void onDisable() {
 		//PluginDescriptionFile pdfFile =this.getDescription();
 		//this.logger.info(pdfFile.getName() + " is now disabled ");
 		log.info(name + "has been disabled");
 	}
 	@Override
 	public void onEnable(){
 		log.info(name + "version" + version + "has been enabled");
 		PluginManager pm = getServer().getPluginManager();
 		
 
 		//this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " is enabled ");
 		}
 	public boolean hatOn(CommandSender sender){
 		Player player = (Player) sender;
 	 if (player.getItemInHand().getTypeId() == 0) {
          player.sendMessage(ChatColor.RED + "Please pick a valid item!");
          return true;
 	  } else {
              ItemStack itemHand = player.getItemInHand();
              PlayerInventory inventory = player.getInventory();
              ItemStack itemHead = inventory.getHelmet();
              inventory.removeItem(itemHand);
              inventory.setHelmet(itemHand);
              if (itemHead.getTypeId() != 0) {
                  inventory.setItemInHand(itemHead);
              player.sendMessage(ChatColor.YELLOW + "You now have a hat!");
              return true;
              }
 	  	}
 	 return false;
 	}
 	
 }
 
