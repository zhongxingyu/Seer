 package me.tehbeard.BeardStat.listeners;
 
 import java.util.Date;
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Calls the stat manager to trigger events
  * @author James
  *
  */
 public class StatPlayerListener implements Listener {
 
 	List<String> worlds;
 	private PlayerStatManager playerStatManager;
 
 	public StatPlayerListener(List<String> worlds,PlayerStatManager playerStatManager){
 		this.worlds = worlds;
 		this.playerStatManager = playerStatManager;
 		}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerAnimation(PlayerAnimationEvent event) {
 		if(event.getAnimationType()==PlayerAnimationType.ARM_SWING){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","armswing").incrementStat(1);
 		}
 
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		playerStatManager.getPlayerBlob(event.getPlayer().getName());
 		playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","login").incrementStat(1);
 		playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","lastlogin").setValue( (int)(System.currentTimeMillis()/1000L));
 		if(!playerStatManager.getPlayerBlob(event.getPlayer().getName()).hasStat("stats", "firstlogin")){
		    playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","firstlogin").setValue((int)(event.getPlayer().getFirstPlayed()/1000L));    
 		}
 		
 		BeardStat.self().setLoginTime(event.getPlayer().getName(), (new Date()).getTime());
 
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerChat(PlayerChatEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","chatletters").incrementStat(event.getMessage().length());
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","chat").incrementStat(1);
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(
 					event.getPlayer().getName()).getStat("itemdrop",event.getItemDrop().getItemStack().getType().toString().toLowerCase().replace("_","")).incrementStat(event.getItemDrop().getItemStack().getAmount()
 							);
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerFish(PlayerFishEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","fishcaught").incrementStat(1);
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerKick(PlayerKickEvent event){
 		if(event.isCancelled()==false){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","kicks").incrementStat(1);
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","lastlogout").setValue( (int)((new Date()).getTime()/1000L));
 			calc_timeonline(event.getPlayer().getName());
 		}
 
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 
 		playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","lastlogout").setValue( (int)((new Date()).getTime()/1000L));
 		calc_timeonline(event.getPlayer().getName());
 
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if(event.isCancelled()==false &&
 				(event.getTo().getBlockX() != event.getFrom().getBlockX() || 
 				event.getTo().getBlockY() != event.getFrom().getBlockY() || 
 				event.getTo().getBlockZ() != event.getFrom().getBlockZ() )&& 
 				!worlds.contains(event.getPlayer().getWorld().getName())){
 
 			Location from,to;
 			Player player = event.getPlayer();
 			from = event.getFrom();
 			to = event.getTo();
 			if(from.getWorld().equals(to.getWorld())){
 				if(from.distance(to) < 8){
 					playerStatManager.getPlayerBlob(player.getName()).getStat("stats","move").incrementStat((int)Math.ceil(from.distance(to)));
 				}
 			}
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("itempickup",event.getItem().getItemStack().getType().toString().toLowerCase().replace("_","")).incrementStat(event.getItem().getItemStack().getAmount());
 
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerPortal(PlayerPortalEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","portal").incrementStat(1);
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			TeleportCause teleportCause = event.getCause();
 			Player player = event.getPlayer();
 			if(teleportCause == TeleportCause.ENDER_PEARL){
 				playerStatManager.getPlayerBlob(player.getName()).getStat("itemuse","enderpearl").incrementStat(1);
 			}
 			else
 			{
 				playerStatManager.getPlayerBlob(player.getName()).getStat("stats","teleport").incrementStat(1);
 			}
 
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerBucketFill(PlayerBucketFillEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","fill"+ event.getBucket().toString().toLowerCase().replace("_","")).incrementStat(1);
 
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
 		if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 			playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","empty"+ event.getBucket().toString().toLowerCase().replace("_","")).incrementStat(1);
 
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerInteract(PlayerInteractEvent event){
 
 		if(event.getClickedBlock()!=null){
 			if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
 				Player player = event.getPlayer();
 				Action action = event.getAction();
 				ItemStack item = event.getItem();
 				Block clickedBlock = event.getClickedBlock();
 				Result result = event.useItemInHand();
 				if(item !=null &&
 						action!=null &&
 						clickedBlock!=null){
 
 						if(result.equals(Result.DENY)==false){
 							/*lighter
 							  sign
 							  tnt
 							  bucket
 							  waterbucket
 							  lavabucket
 							  cakeblock*/
 							if(item.getType()==Material.FLINT_AND_STEEL ||
 									item.getType()==Material.FLINT_AND_STEEL ||
 									item.getType()==Material.SIGN 
 									){
 								playerStatManager.getPlayerBlob(player.getName()).getStat("itemuse",item.getType().toString().toLowerCase().replace("_","")).incrementStat(1);
 							}
 						}
 						if(clickedBlock.getType() == Material.CAKE_BLOCK||
 								(clickedBlock.getType() == Material.TNT && item.getType()==Material.FLINT_AND_STEEL)){
 							playerStatManager.getPlayerBlob(player.getName()).getStat("itemuse",clickedBlock.getType().toString().toLowerCase().replace("_","")).incrementStat(1);
 						}
 						if(clickedBlock.getType().equals(Material.CHEST)){
 							playerStatManager.getPlayerBlob(player.getName()).getStat("stats","openchest").incrementStat(1);
 						}
 
 
 				}
 
 
 
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerExp(PlayerExpChangeEvent event){
 		if(!worlds.contains(event.getPlayer().getWorld().getName())){
 			Player player = event.getPlayer();
 			playerStatManager.getPlayerBlob(player.getName()).getStat("exp","lifetimexp").incrementStat(event.getAmount());
 			playerStatManager.getPlayerBlob(player.getName()).getStat("exp","currentexp").setValue(player.getTotalExperience() + event.getAmount());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerExpLevel(PlayerLevelChangeEvent event){
 		if(!worlds.contains(event.getPlayer().getWorld().getName())){
 			Player player = event.getPlayer();
 			playerStatManager.getPlayerBlob(player.getName()).getStat("exp","currentlvl").setValue(event.getNewLevel());
 		}
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onEnchant(EnchantItemEvent event){
 		
 		Player player = event.getEnchanter();
 		
 		if(event.isCancelled()==false && !worlds.contains(player.getWorld().getName())){
 			playerStatManager.getPlayerBlob(player.getName()).getStat("enchant","total").incrementStat(1);
 			playerStatManager.getPlayerBlob(player.getName()).getStat("enchant","totallvlspent").incrementStat(event.getExpLevelCost());
 		}
 	}
 	
 	private void calc_timeonline(String player){
 		
 			long seconds = (System.currentTimeMillis() - BeardStat.self().getLoginTime(player))/1000L;
 			playerStatManager.getPlayerBlob(player).getStat("stats","playedfor").incrementStat(Integer.parseInt(""+seconds));
 
 			BeardStat.self().wipeLoginTime(player);		
 			
 	}
 
 
 
 
 
 }
