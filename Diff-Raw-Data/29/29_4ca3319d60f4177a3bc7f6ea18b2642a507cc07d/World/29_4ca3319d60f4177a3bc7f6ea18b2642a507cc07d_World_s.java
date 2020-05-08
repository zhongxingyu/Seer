 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.world;
 
 import game.entities.EntityType;
 import game.entities.Groups;
 import game.entities.IEntity;
 import game.time.GameTime;
 import game.triggers.ITrigger;
 import game.triggers.Trigger;
 
 import java.util.LinkedList;
 
 import org.newdawn.slick.Graphics;
 
 public class World {
   private final WorldContainer entities;
   private final LinkedList<IEntity> toAdd, toRemove;
 
   private final LinkedList<ITrigger> triggers;
 
   public World() {
     toAdd    = new LinkedList<IEntity>();
     toRemove = new LinkedList<IEntity>();
     entities = new WorldContainer();
     triggers = new LinkedList<ITrigger>();
   }
 
   /**
    * Delayed add, happens at the end of a frame.
    * @param obj object to add
    */
   public void add(IEntity obj) {
     assert (obj != null);
 
     obj.setWorld(this);
     toAdd.add(obj);
   }
 
   public Iterable<IEntity> get(EntityType e) {
     return entities.iterate(e);
   }
 
   public Iterable<IEntity> getUnits() {
     return entities.iterate(Groups.UNITS);
   }
 
   /**
    * Delayed remove, happens at the end of a frame.
    * @param obj object to remove
    */
   public void remove(IEntity obj) {
     assert (obj != null);
 
     toRemove.add(obj);
   }
 
   public void render(Graphics g) {
     for (IEntity e : entities.iterateAll()) {
       e.render(g);
     }
   }
 
   public void update(GameTime time) {
    // Update
     for (IEntity e : entities.iterateAll()) {
       e.update(time);
     }
 
     entities.remove(toRemove);
     toRemove.clear();
 
     entities.add(toAdd);
     toAdd.clear();
   }
 
   public void addTrigger(Trigger trigger) {
     trigger.setWorld(this);
     triggers.add(trigger);
   }
 }
