 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.triggers;
 
 import game.time.GameTime;
 import game.world.World;
 
 import java.util.LinkedList;
 
 public class Trigger implements ITrigger {
   private final LinkedList<ICondition> conditions;
   private final LinkedList<IEffect> effects;
 
   private final boolean loop;
   private boolean runAgain;
 
   private World world;
 
   public Trigger(boolean loop) {
     this.loop = loop;
     runAgain = true;
 
     conditions = new LinkedList<ICondition>();
     effects = new LinkedList<IEffect>();
   }
 
   @Override
   public void setWorld(World world) {
     this.world = world;
   }
 
   @Override
   public boolean runAgain() {
     return runAgain;
   }
 
   @Override
   public void update(GameTime time) {
     if (runAgain) {
       if (checkConditions(time)) {
         doActions(time);
      }
 
      if (!loop) {
        runAgain = false;
       }
     }
   }
 
   private boolean checkConditions(GameTime time) {
     for (ICondition condition : conditions) {
       if (!condition.evaluate(time, world)) {
         return false;
       }
     }
 
     return true;
   }
 
   private void doActions(GameTime time) {
     for (IEffect effect : effects) {
       effect.execute(time, world);
     }
   }
 
   @Override
   public void addCondition(ICondition condition) {
     conditions.add(condition);
   }
 
   @Override
   public void addEffect(IEffect effect) {
     effects.add(effect);
   }
 }
