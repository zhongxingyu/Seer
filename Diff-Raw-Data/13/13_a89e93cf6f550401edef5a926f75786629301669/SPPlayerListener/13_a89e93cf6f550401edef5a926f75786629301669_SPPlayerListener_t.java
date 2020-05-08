 package aor.SimplePlugin;
 
import java.util.ArrayList;

 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 import org.bukkit.ChatColor;
import org.bukkit.Location;
 import org.bukkit.Material;
 import aor.SimplePlugin.SpellBook;
 
 public class SPPlayerListener extends PlayerListener {
 	public static SimplePlugin plugin;
 
 	public SPPlayerListener(SimplePlugin instance) {
 		plugin = instance;
 	}
 
 	public void onInventoryOpen(PlayerInventoryEvent event){
 		for(int i=0;i<plugin.spellOnInventoryOpenList.size();i++){
 			plugin.spellList.get(plugin.spellOnInventoryOpenList.get(i)).onInventoryOpen(event);
 		}
 	}
 	public void onItemHeldChange(PlayerItemHeldEvent event){
 		if (event.getPlayer().getItemInHand().getType() == Material.GOLD_HOE){
 			if (plugin.spellList.get(SimplePlugin.playerBooks.get(event.getPlayer().getName()).getCurrentSpell()).checkInventoryRequirements(event.getPlayer().getInventory())) // If they have the right items...
 			{
 				event.getPlayer().sendMessage("Selected spell: " + ChatColor.DARK_GREEN + plugin.spellList.get(SimplePlugin.playerBooks.get(event.getPlayer().getName()).getCurrentSpell()).getName()); // Print spell with green.
 			}
 			else // If they don't.
 			{
 				event.getPlayer().sendMessage("Selected spell: " + ChatColor.DARK_RED + plugin.spellList.get(SimplePlugin.playerBooks.get(event.getPlayer().getName()).getCurrentSpell()).getName()); // Print spell with red.
 			}
 		}
 		for(int i=0;i<plugin.spellOnItemHeldChangeList.size();i++){
 			plugin.spellList.get(plugin.spellOnItemHeldChangeList.get(i)).onItemHeldChange(event);
 		}
 	}
 	public void onPlayerAnimation(PlayerAnimationEvent event){
 		for(int i=0;i<plugin.spellOnPlayerAnimationList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerAnimationList.get(i)).onPlayerAnimation(event);
 		}
 	}
 	public void onPlayerBedEnter(PlayerBedEnterEvent event){
 		for(int i=0;i<plugin.spellOnPlayerBedEnterList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerBedEnterList.get(i)).onPlayerBedEnter(event);
 		}
 	}
 	public void onPlayerBedLeave(PlayerBedLeaveEvent event){
 		for(int i=0;i<plugin.spellOnPlayerBedLeaveList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerBedLeaveList.get(i)).onPlayerBedLeave(event);
 		}
 	}
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
 		for(int i=0;i<plugin.spellOnPlayerBucketEmptyList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerBucketEmptyList.get(i)).onPlayerBucketEmpty(event);
 		}
 	}
 	public void onPlayerBucketFill(PlayerBucketFillEvent event){
 		for(int i=0;i<plugin.spellOnPlayerBucketFillList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerBucketFillList.get(i)).onPlayerBucketFill(event);
 		}
 	}
 	public void onPlayerChat(PlayerChatEvent event){
 		for(int i=0;i<plugin.spellOnPlayerChatList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerChatList.get(i)).onPlayerChat(event);
 		}
 	}
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		for(int i=0;i<plugin.spellOnPlayerDropItemList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerDropItemList.get(i)).onPlayerDropItem(event);
 		}
 	}
 	public void onPlayerEggThrow(PlayerEggThrowEvent event){
 		for(int i=0;i<plugin.spellOnPlayerEggThrowList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerEggThrowList.get(i)).onPlayerEggThrow(event);
 		}
 	}
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
 		for(int i=0;i<plugin.spellOnPlayerInteractEntityList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerInteractEntityList.get(i)).onPlayerInteractEntity(event);
 		}
 	}
 	public void onPlayerKick(PlayerKickEvent event){
 		for(int i=0;i<plugin.spellOnPlayerKickList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerKickList.get(i)).onPlayerKick(event);
 		}
 	}
 	public void onPlayerLogin(PlayerLoginEvent event){
 		for(int i=0;i<plugin.spellOnPlayerLoginList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerLoginList.get(i)).onPlayerLogin(event);
 		}
 	}
 	public void onPlayerMove(PlayerMoveEvent event){
 		for(int i=0;i<plugin.spellOnPlayerMoveList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerMoveList.get(i)).onPlayerMove(event);
 		}
 	}
 	public void onPlayerPickupItem(PlayerPickupItemEvent event){
 		for(int i=0;i<plugin.spellOnPlayerPickupItemList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerPickupItemList.get(i)).onPlayerPickupItem(event);
 		}
 	}
 	public void onPlayerPortal(PlayerPortalEvent event){
 		for(int i=0;i<plugin.spellOnPlayerPortalList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerPortalList.get(i)).onPlayerPortal(event);
 		}
 	}
 	public void onPlayerPreLogin(PlayerPreLoginEvent event){
 		for(int i=0;i<plugin.spellOnPlayerPreLoginList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerPreLoginList.get(i)).onPlayerPreLogin(event);
 		}
 	}
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		for(int i=0;i<plugin.spellOnPlayerRespawnList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerRespawnList.get(i)).onPlayerRespawn(event);
 		}
 	}
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		for(int i=0;i<plugin.spellOnPlayerTeleportList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerTeleportList.get(i)).onPlayerTeleport(event);
 		}
 	}
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
 		for(int i=0;i<plugin.spellOnPlayerToggleSneakList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerToggleSneakList.get(i)).onPlayerToggleSneak(event);
 		}
 	}
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		Player player = event.getPlayer(); // Set the player object.
 		SimplePlugin.playerBooks.put(player.getName(), new SpellBook(player, plugin)); // Add a new spellbook for the player to the hashmap.
 		for(int i=0;i<plugin.spellOnPlayerJoinList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerJoinList.get(i)).onPlayerJoin(event);
 		}
 	}
 	
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		Player player = event.getPlayer(); // Set the player object.
 		SimplePlugin.playerBooks.remove(player.getName()); // Remove the player's spellbook from the hashmap.
 		for(int i=0;i<plugin.spellOnPlayerQuitList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerQuitList.get(i)).onPlayerQuit(event);
 		}
 	}
 	
 	
 	public void onPlayerInteract(PlayerInteractEvent event) {
 
 		Player player = event.getPlayer(); // Set the player object.		
 		
 		if (player.getItemInHand().getType() == Material.GOLD_HOE){
 			event.setCancelled(true);	//Cancel the event. This only overrides the default gold hoe behavior, not calls to plugins.
 		}
 		
 		// Left clicking air or a block event:
 		if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && player.getItemInHand().getType() == Material.GOLD_HOE) // If they right clicked with the gold hoe...
 		{
 			SpellBook spellBook = SimplePlugin.playerBooks.get(player.getName());
 			plugin.spellList.get(spellBook.getCurrentSpell()).castSpell(player);
 		}
 		
 		// Right clicking air or a block event:
 		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getType() == Material.GOLD_HOE) // If they left clicked with the gold hoe.
 		{
 			SpellBook spellBook = SimplePlugin.playerBooks.get(player.getName());
 			spellBook.nextSpell(player); // Scroll through spells.
 		}
 		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getType() == Material.GOLD_HOE){
 			if(plugin.spellList.get(SimplePlugin.playerBooks.get(player.getName()).getCurrentSpell()).playerSelect){
 				ArrayList<Double> playerDistances=new ArrayList<Double>();
 				ArrayList<String> playerNames=new ArrayList<String>();
 				for(int i=0;i<player.getWorld().getPlayers().size();i++){
 					playerDistances.add(distance(player.getWorld().getPlayers().get(i).getLocation(),player.getTargetBlock(null, 256).getLocation()));
 					playerNames.add(player.getWorld().getPlayers().get(i).getDisplayName());
 				}
 				double shortest=-1;
 				String name="";
 				while(playerDistances.size()>0){
 					if(playerDistances.get(0)>shortest){
 						shortest=playerDistances.get(0);
 						name=playerNames.get(0);
 					}
 					playerDistances.remove(0);
 					playerNames.remove(0);
 				}
 				plugin.spellList.get(SimplePlugin.playerBooks.get(player.getName()).getCurrentSpell()).selectedPlayerNames.put(player.getName(), name);
 				player.sendMessage("You have selected "+name+".");
 			}
 		}
 		for(int i=0;i<plugin.spellOnPlayerInteractList.size();i++){
 			plugin.spellList.get(plugin.spellOnPlayerInteractList.get(i)).onPlayerInteract(event);
 		}
 	}
 	public double distance(Location pos1,Location pos2){
 		return Math.hypot(pos1.getY()-pos2.getY(), Math.hypot(pos1.getX()-pos2.getX(),pos1.getZ()-pos1.getZ()));
 	}
 }
