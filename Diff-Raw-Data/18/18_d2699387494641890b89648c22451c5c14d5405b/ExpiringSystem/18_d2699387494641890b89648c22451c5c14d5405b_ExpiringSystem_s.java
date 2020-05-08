 package com.soc.game.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.soc.game.components.Expires;
 import com.soc.game.components.State;
 
 public class ExpiringSystem extends EntityProcessingSystem{
 	 @Mapper ComponentMapper<Expires> em;
      
      @SuppressWarnings("unchecked")
      public ExpiringSystem() {
              super(Aspect.getAspectForAll(Expires.class));
      }
      protected void process(Entity e) {
     	 System.out.println("lo ahces nene");
          Expires exp = em.get(e);
          if(exp.isExpiring){
          exp.delay -= world.getDelta();
          if (exp.delay <= 0) {
            e.getComponent(State.class).state=State.DEATH;
             e.deleteFromWorld();
          }
          }
      }
 }
