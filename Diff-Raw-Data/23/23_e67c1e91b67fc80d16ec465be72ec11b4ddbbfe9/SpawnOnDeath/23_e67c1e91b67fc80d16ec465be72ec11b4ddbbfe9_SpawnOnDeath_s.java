 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.components.misc;
 
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.ICompAnim;
 import game.components.interfaces.ILogicComponent;
 import game.entities.IEntity;
 import math.time.GameTime;
 
 public class SpawnOnDeath implements ILogicComponent {
   private IEntity owner;
   private final ICompAnim anim;
   
   public SpawnOnDeath(final ICompAnim anim) {
     this.anim = anim;
   }
 
   @Override
   public void update(GameTime time) {
     // Do nothing
   }
 
   @Override
   public void reciveMessage(ComponentMessage message, Object args) {
     if (message == ComponentMessage.KILL) {
      owner.addAction(new SpawnRunToEndAnim(
        owner.getBody().getX1(), owner.getBody().getY1(),
        anim.getTileWidth(), anim.getTileHeight(),
        anim));
       owner.addAction(new RemoveEntity(owner));
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
