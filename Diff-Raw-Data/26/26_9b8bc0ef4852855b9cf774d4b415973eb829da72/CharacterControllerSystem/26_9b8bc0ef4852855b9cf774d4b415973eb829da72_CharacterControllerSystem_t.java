 package entitySystems;
 
 
 import ability.Ability;
 import math.Vector2;
 import components.*;
import content.ContentManager;
 import entityFramework.*;
 
 /**
  * 
  * @author Joakim Johansson
  *
  */
 public class CharacterControllerSystem extends EntitySingleProcessingSystem{
 
 	public CharacterControllerSystem(IEntityWorld world) {
 		super(world, InputComp.class, 
 					 PhysicsComp.class,
 					 AbilityPointsComp.class,
 					 TransformationComp.class,
 					 WeaponComp.class);
 	}
 	
 	@Override
 	public void initialize() {
 	}
 
 	@Override
 	protected void processEntity(IEntity entity) {
 		PhysicsComp	  physComp   = entity.getComponent(PhysicsComp.class);
 		InputComp 		  inpComp    = entity.getComponent(InputComp.class);
 		TransformationComp    posComp    = entity.getComponent(TransformationComp.class);
 		AbilityPointsComp apComp     = entity.getComponent(AbilityPointsComp.class);
 		WeaponComp 	  weaponComp = entity.getComponent(WeaponComp.class);
  		
 		if(inpComp.isLeftMouseButtonDown()){
 			Ability ability = weaponComp.getPrimaryAbility();
 			ability.useAbility(entity, inpComp.getMousePosition(), this.getWorld().getEntityManager());
 		}
 
 		int speedFactor = 2;
		if(inpComp.isGallopButtonPressed()){
 			speedFactor=4;
			IEntity cloud = this.getEntityManager().createEntity(ContentManager.loadArchetype("Cloud.archetype"));
			cloud.getComponent(TransformationComp.class).setPosition(new Vector2(posComp.getPosition().X,posComp.getPosition().Y + posComp.getOrigin().Y));
			cloud.getComponent(TransformationComp.class).setScale(0.4f, 0.4f);
			cloud.refresh();
		}
 
 		Vector2 velocity = new Vector2(0,0);
 		if (inpComp.isBackButtonPressed()){
 			velocity=Vector2.add(velocity, new Vector2(0,1));
 		}
 		if (inpComp.isForwardButtonPressed()){
 			velocity=Vector2.add(velocity, new Vector2(0,-1));
 		}
 		if (inpComp.isLeftButtonPressed()){
 			velocity=Vector2.add(velocity, new Vector2(-1,0));
 			posComp.setMirror(false);
 		}
 		if (inpComp.isRightButtonPressed()){
 			velocity=Vector2.add(velocity, new Vector2(1,0));
 			posComp.setMirror(true);
 		}
 
 		if(velocity.length()!=0)
 			velocity=Vector2.norm(velocity);
 
 		velocity = Vector2.mul(speedFactor, velocity);
 		physComp.setVelocity(velocity);
 
 
 	}
 
 }
