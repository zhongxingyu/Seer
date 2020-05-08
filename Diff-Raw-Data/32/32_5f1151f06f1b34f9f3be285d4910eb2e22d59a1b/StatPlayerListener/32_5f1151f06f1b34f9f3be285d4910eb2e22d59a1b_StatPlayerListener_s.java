 package me.tehbeard.BeardStat.listeners;
 
 import java.util.Date;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.StatCollectors.*;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.Material;
 
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 /**
  * Calls the stat manager to trigger events
  * @author James
  *
  */
 public class StatPlayerListener extends PlayerListener {
 
 
 
 	@Override
 	public void onPlayerAnimation(PlayerAnimationEvent event) {
 		// TODO Auto-generated method stub
 		for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 			((PlayerStatCollector)sc).onPlayerAnimation(event.getPlayer(),event.getAnimationType());
 		}
 	}
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// TODO Auto-generated method stub
 		for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 			((PlayerStatCollector)sc).onPlayerJoin(event.getPlayer());
 		}
 	}
 
 	public void onPlayerPreLogin(PlayerPreLoginEvent event){
 		if(event.getResult()==PlayerPreLoginEvent.Result.ALLOWED){
 		}
 	}
 	public void onPlayerChat(PlayerChatEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerChat(event.getPlayer(), event.getMessage());
 			}
 		}
 	}
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerDropItem(event.getPlayer(),event.getItemDrop());
 			}}
 	}
 	public void onPlayerFish(PlayerFishEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerFish(event.getPlayer(),event.getState());
 			}}
 	}
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerInteractEntity(event.getPlayer(),event.getRightClicked());
 			}
 		}
 
 	}
 
 	public void onPlayerKick(PlayerKickEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerKick(event.getPlayer(),event.getLeaveMessage());
 			}
 			calc_timeonline(event.getPlayer().getName());
 		}
 
 	}
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		if(event.getResult() == Result.ALLOWED){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerLogin(event.getPlayer());
 				PlayerStatManager.getPlayerBlob(event.getPlayer().getName());
 				BeardStat.loginTimes.put(event.getPlayer().getName(), (new Date()).getTime());
 
 			}}
 	}
 	public void onPlayerQuit(PlayerQuitEvent event) {
 
 		for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 			((PlayerStatCollector)sc).onPlayerLogout(event.getPlayer());
 		}
 		calc_timeonline(event.getPlayer().getName());
 
 	}
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if(event.isCancelled()==false &&
 				event.getTo().getBlockX() != event.getFrom().getBlockX() && 
 				event.getTo().getBlockY() != event.getFrom().getBlockY() && 
 				event.getTo().getBlockZ() != event.getFrom().getBlockZ()){
 
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerMove(event.getPlayer(),event.getFrom(),event.getTo());
 			}
 		}
 	}
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerPickup(event.getPlayer(),event.getItem());
 			}
 		}
 	}
 	public void onPlayerPortal(PlayerPortalEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerPortal(event.getPlayer(),event.getTo(),event.getFrom());
 			}
 		}
 	}
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){
 				((PlayerStatCollector)sc).onPlayerTeleport(event.getPlayer(),event.getTo(),event.getFrom());
 			}
 		}
 	}
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){				
 				((PlayerStatCollector)sc).onPlayerToggleSneak(event.getPlayer(),event.isSneaking());
 			}
 		}
 	}
 
 	public void onPlayerBucketFill(PlayerBucketFillEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){				
 				((BlockStatCollector)sc).onPlayerBucketFill(event.getPlayer(),event.getBucket());
 			}
 		}
 	}
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
 		if(event.isCancelled()==false){
 			for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){				
 				((BlockStatCollector)sc).onPlayerBucketEmpty(event.getPlayer(),event.getBucket());
 			}
 		}
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getClickedBlock()!=null){
 			if(event.getClickedBlock().getType().equals(Material.CHEST)){
 				PlayerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","openchest").incrementStat(1);
 			}
 			if(event.isCancelled()==false){
 
 				for( IStatCollector sc : StatCollectorManager.getCollectors(event.getType())){				
 					((BlockStatCollector)sc).onPlayerBlockInteract(event.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace(),event.useItemInHand());
 				}
 			}
 		}
 	}
 
 	private void calc_timeonline(String player){
 		if( BeardStat.loginTimes.containsKey(player)){
		long seconds = ((new Date()).getTime() - BeardStat.loginTimes.get(player))/1000L;
		PlayerStatManager.getPlayerBlob(player).getStat("stats","playedfor").incrementStat(Integer.parseInt(""+seconds));
		BeardStat.loginTimes.remove(player);		
 		}
 		else
 		{
 			BeardStat.printDebugCon("Attempted to calculate time for a player who doesn't have a record!");
 		}
 	}
 
 
 
 }
