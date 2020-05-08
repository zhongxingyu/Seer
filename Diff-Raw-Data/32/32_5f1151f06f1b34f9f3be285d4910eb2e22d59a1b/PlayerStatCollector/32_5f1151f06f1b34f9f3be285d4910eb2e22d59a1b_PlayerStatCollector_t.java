 package me.tehbeard.BeardStat.StatCollectors;
 
 import java.util.Date;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerFishEvent.State;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * The base stat collector for player events
  * @author James
  *
  */
 public class PlayerStatCollector implements IStatCollector {
 
 	public void onPlayerJoin(Player player){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","login").incrementStat(1);
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","lastlogin").setValue( (int)((new Date()).getTime()/1000L));
 	}
 	public void onPlayerLogin(Player player){
 
 	}
 
 	public void onPlayerLogout(Player player){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","lastlogout").setValue( (int)((new Date()).getTime()/1000L));
 
 	}
 
 	public void onPlayerChat(Player player,String message){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","chatletters").incrementStat(message.length());
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","chat").incrementStat(1);
 	}
 
 	public void onPlayerDropItem(Player player,Item drop){
 		
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("itemdrop",drop.getItemStack().getType().toString().toLowerCase().replace("_","")).incrementStat(drop.getItemStack().getAmount());
 	}
 
 	public void onPlayerInteractEntity(Player player,Entity entity){
 
 	}
 
 	public void onPlayerKick(Player player,String reason){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","kicks").incrementStat(1);
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","lastlogout").setValue( (int)((new Date()).getTime()/1000L));
 	}
 
 	public void onPlayerMove(Player player, Location from, Location to){
 		if(from.distance(to) < 5){
 			PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","move").incrementStat((int)from.distance(to));
 		}
 	}
 
 	public void onPlayerPickup(Player player,Item pickup){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("itempickup",pickup.getItemStack().getType().toString().toLowerCase().replace("_","")).incrementStat(pickup.getItemStack().getAmount());
 	}
 
 	public void onPlayerPortal(Player player,Location to,Location  From){
		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","portal").incrementStat(1);
 	}
 
 	public void onPlayerTeleport(Player player,Location to,Location  From){
 		PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","teleport").incrementStat(1);
 	}
 
 	public void onPlayerToggleSneak(Player player,boolean isSneaking){
 
 	}
 	public void onPlayerFish(Player player, PlayerFishEvent.State state){
 		if(state==State.CAUGHT_FISH){
 			PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","fishcaught").incrementStat(1);	
 		}
 
 	}
 	public void onPlayerAnimation(Player player,
 			PlayerAnimationType animationType) {
 		if(animationType==PlayerAnimationType.ARM_SWING){
 			PlayerStatManager.getPlayerBlob(player.getName()).getStat("stats","armswing").incrementStat(1);
 		}
 
 	}
 
 
 
 
 
 
 }
