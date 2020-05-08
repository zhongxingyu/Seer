 package me.ellbristow.simplespawnlitecore.events;
 
 import me.ellbristow.simplespawnlitecore.LocationType;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 
 public class SimpleSpawnTeleportEvent extends Event implements Cancellable {
 
     private static final HandlerList handlers = new HandlerList();
     private LocationType type;
     private Location fromLoc;
     private Location toLoc;
     private boolean cancelled;
     private Player player;
     
     public SimpleSpawnTeleportEvent(Player player, LocationType locationType, Location fromLoc, Location toLoc) {
        this.player = player;
         this.type = locationType;
         this.fromLoc = fromLoc;
         this.toLoc = toLoc;
     }
     
     public Player getPlayer() {
         return player;
     }
     
     public Location getFromLoc() {
         return fromLoc;
     }
 
     public void setFromLoc(Location fromLoc) {
         this.fromLoc = fromLoc;
     }
 
     public Location getToLoc() {
         return toLoc;
     }
 
     public void setToLoc(Location toLoc) {
         this.toLoc = toLoc;
     }
     
     public LocationType getType() {
         return type;
     }
 
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
     
     public static HandlerList getHandlerList() {
         return handlers;
     }
 
     @Override
     public boolean isCancelled() {
         return cancelled;
     }
 
     @Override
     public void setCancelled(boolean bln) {
         cancelled = bln;
     }
     
     
 
 }
