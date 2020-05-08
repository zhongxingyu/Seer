 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.components.misc;
 
 import game.actions.SpawnWithSend;
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.ICompAnim;
 import game.components.interfaces.ILogicComponent;
 import game.entities.Entity;
 import game.entities.IEntity;
 import game.entities.groups.EntityType;
 import math.time.GameTime;
 
 public class SpawnOnDeath implements ILogicComponent {
   private IEntity owner;
   private final ICompAnim anim;
 
   public SpawnOnDeath(final ICompAnim anim) {
     this.anim = anim;
   }
 
   @Override
   public void update(final GameTime time) {
     // Do nothing
   }
 
   @Override
   public void reciveMessage(final ComponentMessage message, final Object args) {
     if (message == ComponentMessage.KILL) {
       final Entity e = new Entity(owner.getBody().getX1(),
                                   owner.getBody().getY1(),
                                   owner.getBody().getWidth(),
                                   owner.getBody().getHeight(),
                                   EntityType.ANIMATED);
       e.addRenderComponent(anim);
 
       owner.addAction(new SpawnWithSend(e, ComponentMessage.END_ANIMATION, null));
     }
   }
 
   @Override
   public ComponentType getComponentType() {
     return ComponentType.SPAWNER;
   }
 
   @Override
   public void setOwner(final IEntity owner) {
     this.owner = owner;
   }
 }
