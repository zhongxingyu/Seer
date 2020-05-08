 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.events
  * Created: 2013/02/09 19:16:05
  */
 package net.syamn.sakuracmd.events;
 
 import org.bukkit.World;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 /**
  * EndResettingEvent (EndResettingEvent.java)
  * @author syam(syamn)
  */
 public class EndResettingEvent extends Event implements Cancellable{
     private static final HandlerList handlers = new HandlerList();
     private boolean isCancelled = false;
     
     private World world;
     private short dragonAmount;
     private String message;
     
     public EndResettingEvent(World world, short dragonAmount, String message){
         this.world = world;
         this.dragonAmount = dragonAmount;
        this.message = message;
     }
     
     public World getWorld(){
         return this.world;
     }
     
     public short getDragonAmount(){
         return dragonAmount;
     }
     
     public void setDragonAmount(short amount){
         this.dragonAmount = amount;
     }
     
     public String getCompleteMessage(){
         return this.message;
     }
     
     public void setCompleteMessage(String message){
         this.message = message;
     }
     
     @Override
     public boolean isCancelled() {
         return this.isCancelled;
     }
 
     @Override
     public void setCancelled(boolean cancelled) {
         this.isCancelled = cancelled;
     }
 
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
 
     public static HandlerList getHandlerList() {
         return handlers;
     }
 }
