 package Staartvin.ArmorControl;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 
 /**
  * @author Staartvin
  *
  */
 public class Listeners implements Listener {
 
 	ArmorControl plugin;
 	
 	public Listeners (ArmorControl plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event) {
 		if (!plugin.getConfig().getBoolean("UseArmorControl")) return;
 		// Player is not online for some reason
 		if (plugin.getServer().getPlayer(event.getPlayer().getName()) == null) return;
 	    Player player = plugin.getServer().getPlayer(event.getPlayer().getName());
 	 // Player hasn't got the correct permission
 	    if (player.hasPermission("armorcontrol.exempt")) return;
 	    
 	    plugin.inv = plugin.getServer().getPlayer(event.getPlayer().getName()).getInventory();
 	    plugin.methods.checkInventoryforArmor(player); 
 	}
 	
 	@EventHandler
 	public void onBowUse(EntityShootBowEvent event) {
 		if (!plugin.getConfig().getBoolean("UseWeaponControl")) return;
 		// Entity is not a player
 		if (!(event.getEntity() instanceof Player)) return;
 	    Player player = (Player) event.getEntity();
 	 // Player hasn't got the correct permission
 	    if (player.hasPermission("weaponcontrol.exempt")) return;
 	    
 	    if (player.getLevel() < plugin.bowLevel) {
 	     player.sendMessage(ChatColor.RED + "You cannot use a bow! You must be at least level: " + plugin.bowLevel);
 	     event.setCancelled(true);
 	    }
 	}
 	
 	@EventHandler
 	public void onWeaponUse(EntityDamageByEntityEvent event) {
 		if (!plugin.getConfig().getBoolean("UseWeaponControl")) return;
 		// Damager is not a player for some reason
 		if (!(event.getDamager() instanceof Player)) return;
 	    Player player = (Player) event.getDamager();
 	 // Player has got the correct exempt permission
 	    if (player.hasPermission("weaponcontrol.exempt")) return;
 //	    if (player.getItemInHand() == null) return;
 	    int IDinHand = player.getItemInHand().getTypeId();
 	    // Nothing in hand
 	    if (IDinHand == 0) return;
 	    if (IDinHand == 267 || IDinHand == 268 || IDinHand == 272 || IDinHand == 276 || IDinHand == 283 || IDinHand == 261) {
 		    for (int ID : plugin.weaponIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, plugin.methods.checkWeaponType(ID), "weapon")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this weapon! You must be at least level: " + plugin.methods.findLevel(plugin.methods.checkWeaponType(ID), "weapon"));
 		    		}
 		    	}
 		    }
 	    }
 	}
 	
 	@EventHandler
 	public void onToolUse(BlockBreakEvent event) {
 		if (!plugin.getConfig().getBoolean("UseToolControl")) return;
 		
 		// Player is not online for some reason
 		if (event.getPlayer() == null) return;
 	    Player player = event.getPlayer();
 	    // Player has got the correct exempt permission
 	    if (player.hasPermission("toolcontrol.exempt")) return;
 	    int IDinHand = player.getItemInHand().getTypeId();
 	    if (IDinHand == 0) return;
 	    // Tool in hand is a weapon
 	    if (IDinHand == 267 || IDinHand == 268 || IDinHand == 272 || IDinHand == 276 || IDinHand == 283 || IDinHand == 261) return;
 	    
 	    // Wood tools
 		    for (int ID : plugin.woodToolsIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, "wood", "tool")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this tool! You must be at least level: " + plugin.methods.findLevel("wood", "tool"));
 		    		}
 		    	}
 		    }
 		    
 		 // Stone tools
 		    for (int ID : plugin.stoneToolsIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, "stone", "tool")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this tool! You must be at least level: " + plugin.methods.findLevel("stone", "tool"));
 		    		}
 		    	}
 		    }
 		    
 		 // Iron tools
 		    for (int ID : plugin.ironToolsIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, "iron", "tool")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this tool! You must be at least level: " + plugin.methods.findLevel("iron", "tool"));
 		    		}
 		    	}
 		    }
 		    
 		 // Gold tools
 		    for (int ID : plugin.goldToolsIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, "gold", "tool")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this tool! You must be at least level: " + plugin.methods.findLevel("gold", "tool"));
 		    		}
 		    	}
 		    }
 		    
 		 // Diamond tools
 		    for (int ID : plugin.diamondToolsIDs) {
 		    	if (IDinHand == ID) {
 		    		if (!plugin.methods.checkLevel(player, "diamond", "tool")) {
 		    			event.setCancelled(true);
 		    			player.sendMessage(ChatColor.RED + "You cannot use this tool! You must be at least level: " + plugin.methods.findLevel("diamond", "tool"));
 		    		}
 		    	}
 		    }
 	    }	
 }
