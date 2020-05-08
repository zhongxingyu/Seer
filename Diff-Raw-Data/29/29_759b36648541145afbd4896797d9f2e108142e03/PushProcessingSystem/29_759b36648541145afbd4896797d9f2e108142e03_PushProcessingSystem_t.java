 package com.soc.game.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.soc.game.components.Position;
 import com.soc.game.components.Push;
 import com.soc.game.components.Velocity;
 
 public class PushProcessingSystem extends EntityProcessingSystem{
 
 	@Mapper ComponentMapper<Push> pm;
 	@Mapper ComponentMapper<Position> pom;
 	@Mapper ComponentMapper<Velocity> vm;
 
 	
	@SuppressWarnings("unchecked")
 	public PushProcessingSystem() {
 		super(Aspect.getAspectForAll(Push.class));
 	}
 
 	@Override
 	protected void process(Entity e) {
 		  Push pus = pm.get(e);
 		  Position pos = pom.get(e);
 		  Velocity vel = vm.get(e);
 		  
 		  vel.vx = pus.speed*Math.signum(pos.direction.x);
 		  vel.vy = pus.speed*Math.signum(pos.direction.y);
 		  
 		  pus.distance -= Math.abs(vel.vx*world.delta)+Math.abs(vel.vy*world.delta);
 		  if(pus.distance < 0){
 			  e.removeComponent(pus);
 			  e.changedInWorld();
 		  }
 	}
 }
