 package merkurius.ld27.system;
 
 import java.util.Map;
 
 import merkurius.ld27.component.Shooter;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.systems.EntityProcessingSystem;
 import com.badlogic.gdx.math.Vector2;
 
 import fr.kohen.alexandre.framework.base.C;
 import fr.kohen.alexandre.framework.components.Transform;
 import fr.kohen.alexandre.framework.components.Velocity;
 import fr.kohen.alexandre.framework.components.VisualComponent;
 import fr.kohen.alexandre.framework.model.Visual;
 import fr.kohen.alexandre.framework.systems.interfaces.AnimationSystem;
 
 /**
  * Extend this class to modify how the animations are updated
  * @author Alexandre
  *
  */
 public class LD27AnimationSystem extends EntityProcessingSystem implements AnimationSystem {
 	protected ComponentMapper<VisualComponent> 	visualMapper;
 	protected ComponentMapper<Transform> 		transformMapper;
 	protected ComponentMapper<Velocity> 		velocityMapper;
 	protected ComponentMapper<Shooter>      	shooterMapper;
 	protected Map<Entity,Visual>				visuals;
 	
 	
 	@SuppressWarnings("unchecked")
 	public LD27AnimationSystem() {
 		super( Aspect.getAspectForAll(VisualComponent.class, Shooter.class, Velocity.class) );
 	}
 	
 	@Override
 	public void initialize() {
 		visualMapper 	= ComponentMapper.getFor(VisualComponent.class, world);
 		velocityMapper 	= ComponentMapper.getFor(Velocity.class, world);
 		shooterMapper 	= ComponentMapper.getFor(Shooter.class, world);
 	}
 
 	@Override
 	protected void process(Entity e) {
 		visualMapper.get(e).stateTime += world.getDelta();
 		if( velocityMapper.getSafe(e) != null ) {
 			setCurrentAnim( visualMapper.get(e), shooterMapper.get(e).getShootingVector(), velocityMapper.get(e).getSpeed() );
 		}
 	}
 	
 	protected void setCurrentAnim(VisualComponent visual, Vector2 direction, Vector2 speed) {
 		visual.currentAnimationName = getAnim(visual, direction) ;
 		if( speed.len() < 50 ) {
 			visual.currentAnimationName = idleAnim(visual);
 		}
 	}
 	
 	
 	protected String getAnim(VisualComponent visual, Vector2 direction) {
 		String currentAnim = visual.currentAnimationName;
 		
 		if( direction.angle() > 337.5 ) {
			currentAnim = C.WALK_UP;
 		} else if( direction.angle() > 292.5 ) {
			currentAnim = C.WALK_UP_LEFT;
 		} else if( direction.angle() > 247.5 ) {
			currentAnim = C.WALK_LEFT;
 		} else if( direction.angle() > 202.5 ) {
 			currentAnim = C.WALK_DOWN_LEFT;
 		} else if( direction.angle() > 157.5 ) {
			currentAnim = C.WALK_DOWN;
 		} else if( direction.angle() > 112.5 ) {
			currentAnim = C.WALK_DOWN_RIGHT;
 		} else if( direction.angle() > 67.5 ) {
			currentAnim = C.WALK_RIGHT;
 		} else if( direction.angle() > 22.5 ) {
 			currentAnim = C.WALK_UP_RIGHT;
 		} else{
			currentAnim = C.WALK_UP;
 		}
 		/*if( speed.len() < 1 )
 			return idleAnim(visual);
 			
 		if( speed.x < 0.1f ) {
 			if( speed.y < 0.1f )
 				currentAnim = C.WALK_DOWN_LEFT;
 			else if( speed.y > 0.1f )
 				currentAnim = C.WALK_UP_LEFT;
 			else 
 				currentAnim = C.WALK_LEFT;
 		} else if( speed.x > 0.1f ) {
 			if( speed.y < 0.1f )
 				currentAnim = C.WALK_DOWN_RIGHT;
 			else if( speed.y > 0.1f )
 				currentAnim = C.WALK_UP_RIGHT;
 			else 
 				currentAnim = C.WALK_RIGHT;
 		} else {
 			if( speed.y > 0.1f )
 				currentAnim = C.WALK_UP;
 			else 
 				currentAnim = C.WALK_DOWN;
 		}	*/	
 		
 		return currentAnim;
 	}
 	
 	
 	/**
 	 * Sets the current animation to the idle animation
 	 */
 	protected String idleAnim(VisualComponent visual) {
 		String currentAnim = visual.currentAnimationName;
 		
 		if (currentAnim.equalsIgnoreCase(C.WALK_LEFT)) {
 			currentAnim = C.STAND_LEFT;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_RIGHT)) {
 			currentAnim = C.STAND_RIGHT;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_UP)) {
 			currentAnim = C.STAND_UP;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_DOWN)) {
 			currentAnim = C.STAND_DOWN;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_DOWN_LEFT)) {
 			currentAnim = C.STAND_DOWN_LEFT;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_DOWN_RIGHT)) {
 			currentAnim = C.STAND_DOWN_RIGHT;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_UP_LEFT)) {
 			currentAnim = C.STAND_UP_LEFT;
 		} else if (currentAnim.equalsIgnoreCase(C.WALK_UP_RIGHT)) {
 			currentAnim = C.STAND_UP_RIGHT;
 		}
 		
 		return currentAnim;
 	}
 	
 }
