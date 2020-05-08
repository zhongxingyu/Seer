 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.entities;
 
 import game.actions.IAction;
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.ILogicComponent;
 import game.components.interfaces.IRenderComponent;
 import game.entities.groups.EntityType;
 import game.world.World;
 
 import java.util.ArrayList;
 
 import math.Rectangle;
 import math.time.GameTime;
 
 import org.newdawn.slick.Graphics;
 
 public class Entity implements IEntity {
   protected final Rectangle body;
   private final ArrayList<IAction> actions;
 
   private final ArrayList<IRenderComponent> renders;
 
   private final EntityType type;
 
   private final ArrayList<ILogicComponent> updates;
   private World world;
 
   public Entity(float x, float y, float w, float h, EntityType type) {
     body = new Rectangle(x, y, w, h);
 
     actions = new ArrayList<IAction>();
 
     updates = new ArrayList<ILogicComponent>();
     renders = new ArrayList<IRenderComponent>();
 
     this.type = type;
   }
 
   @Override
   public void addAction(IAction action) {
     actions.add(action);
   }
 
   @Override
   public void addLogicComponent(ILogicComponent comp) {
     comp.setOwner(this);
     updates.add(comp);
   }
 
   @Override
   public void addRenderComponent(IRenderComponent comp) {
     comp.setOwner(this);
     renders.add(comp);
   }
 
   @Override
   public Rectangle getBody() {
     return body;
   }
 
   @Override
   public ILogicComponent getComponent(ComponentType componentType) {
    for (ILogicComponent comp : updates) {
       if (comp.getComponentType().equals(componentType)) {
         return comp;
       }
     }
 
     for (ILogicComponent comp : renders) {
       if (comp.getComponentType().equals(componentType)) {
         return comp;
       }
     }
 
     return null;
   }
 
   @Override
   public EntityType getType() {
     return type;
   }
 
   @Override
   public World getWorld() {
     return world;
   }
 
   @Override
   public void render(Graphics g) {
     g.pushTransform();
     g.translate(body.getX1(), body.getY1());
 
     for (IRenderComponent comp : renders) {
       comp.render(g);
     }
 
     g.popTransform();
   }
 
   @Override
   public void sendMessage(ComponentMessage message, Object args) {
     for (ILogicComponent comp : updates) {
       comp.reciveMessage(message, args);
     }
 
     for (IRenderComponent comp : renders) {
       comp.reciveMessage(message, args);
     }
   }
 
   @Override
   public void setWorld(World world) {
     this.world = world;
   }
 
   @Override
   public void update(GameTime time) {
     for (ILogicComponent comp : updates) {
       comp.update(time);
     }
 
     for (IRenderComponent comp : renders) {
       comp.update(time);
     }
 
     for (IAction action : actions) {
       action.execute(time, world);
     }
     actions.clear();
   }
 
   @Override
   public boolean equals(IEntity other) {
     return this == other;
   }
 }
