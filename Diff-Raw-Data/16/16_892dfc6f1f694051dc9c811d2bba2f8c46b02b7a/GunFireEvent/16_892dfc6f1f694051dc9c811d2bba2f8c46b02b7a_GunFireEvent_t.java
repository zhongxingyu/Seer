 package team.GunsPlus.API.Event.Gun;
 
import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 import team.GunsPlus.Item.Gun;
 
 public class GunFireEvent extends Event{
     private static final HandlerList handlers = new HandlerList();
     private Player player = null;
     private Gun gun = null;
     
     public GunFireEvent(Player p, Gun g) {
     	player = p;
     	gun = g;
     }
     
     public HandlerList getHandlers() {
         return handlers;
     }
      
     public static HandlerList getHandlerList() {
         return handlers;
     }
     
     public Player getPlayer() {
     	return player;
     }
     
     public Gun getGun() {
     	return gun;
     }
    
    public Block getTargetBlock() {
    	//GunUtils.get
    	return null;
    }
 }
