 package org.hopto.energy.energydominion.api.event;
 
 import org.hopto.energy.energydominion.api.Player;
 
 public abstract class DominionCardEvent implements CardEvent {
     private Player trigger;
     private Player owner;
 
 
     protected DominionCardEvent(Player trigger) {
         this(trigger,null);
     }
 
     protected DominionCardEvent(Player trigger,Player owner) {
         this.trigger=trigger;
         this.owner=owner;
     }
 
     @Override
     public Player getCardOwner() {
         return owner;
     }
 
     @Override
     public Player getTrigger() {
         return trigger;
     }
 
     @Override
     public void setCardOwner(Player owner) {
         this.owner = owner;
 
 
     }
 
     @Override
     public void setTrigger(Player trigger) {
         this.trigger = trigger;
 
     }
 }
