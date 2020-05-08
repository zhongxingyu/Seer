 package org.hopto.energy.energydominion.api.event;
 
 import org.hopto.energy.energydominion.api.Player;
 
public class PlayCardEvent extends PlayEvent {
 
     @Override
     public Player getCardOwner() {
         return null;
     }
 
     @Override
     public Player getTrigger() {
         return null;
     }
 }
