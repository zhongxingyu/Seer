 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.events.impl;
 
 import game.components.ComponentMessage;
 import game.entities.EntityType;
 import game.entities.IEntity;
 import game.events.IEvent;
 
 public class DamagePlayerEvent implements IEvent<ObjectEventArgs> {
   @Override
   public void execute(IEntity sender, ObjectEventArgs args) {
     if (args.getObject().getType() == EntityType.CREEP) {
      float dmg = 10; // FIXME: Magic number

       for (IEntity e : args.getWorld().get(EntityType.PLAYER)) {
        e.sendMessage(ComponentMessage.DAMAGE, Float.valueOf(dmg));
       }
     }
   }
 }
