 package se.exuvo.planets.systems;
 
 import se.exuvo.planets.components.Position;
 import se.exuvo.planets.components.Velocity;
 import se.exuvo.settings.Settings;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.IntervalEntityProcessingSystem;
 import com.badlogic.gdx.Gdx;
 
 public class VelocitySystem extends IntervalEntityProcessingSystem {
 	@Mapper	ComponentMapper<Velocity> vm;
 	@Mapper	ComponentMapper<Position> pm;
 
 	private float maxX, maxY, minX, minY;
 	private boolean paused;
 	
 	public VelocitySystem() {
 		super(Aspect.getAspectForAll(Velocity.class, Position.class), Settings.getFloat("PhysicsStep"));
 		maxX = Gdx.graphics.getWidth()/2;
 		maxY = Gdx.graphics.getHeight()/2;
 		minX = -maxX;
 		minY = -maxY;
 	}
 
 	@Override
 	protected void process(Entity e) {
 		Position p = pm.get(e);
 		Velocity v = vm.get(e);
 		
 		// apply speed to position
 		p.vec.add(v.vec);
 		
 		if(p.vec.x < minX) p.vec.x = minX;
 		if(p.vec.y < minY) p.vec.y = minY;
 		if(p.vec.x > maxX) p.vec.x = maxX;
		if(p.vec.y > maxY) p.vec.y = maxY;
 	}
 	
 	@Override
 	protected boolean checkProcessing() {
 		if(paused){
 			return false;
 		}else{
 			return super.checkProcessing();
 		}
 	}
 	
 	public void setPaused(boolean newState){
 		paused = newState;
 	}
 
 }
