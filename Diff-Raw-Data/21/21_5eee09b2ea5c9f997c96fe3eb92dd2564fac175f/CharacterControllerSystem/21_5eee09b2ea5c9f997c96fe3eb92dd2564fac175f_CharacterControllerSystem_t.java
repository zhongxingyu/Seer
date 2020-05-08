 package entitySystems;
 
 
 import com.google.common.collect.ImmutableList;
 
 import utils.Circle;
 import utils.ContentManager;
 import math.Vector2;
 import components.*;
 import entityFramework.*;
 import graphics.Color;
 import graphics.Texture2D;
 
 /**
  * 
  * @author Joakim Johansson
  *
  */
 public class CharacterControllerSystem extends EntitySingleProcessingSystem{
 
 	public CharacterControllerSystem(IEntityWorld world) {
		super(world, InputComponent.class, PhysicsComponent.class, TransformationComp.class);
 	}
 	private ComponentMapper<PhysicsComponent> physCM;
 	private ComponentMapper<InputComponent> inpCM;
 	private ComponentMapper<TransformationComp> transCM;
 	
 	private Texture2D attackTexture;
 	@Override
 	public void initialize() {
 		physCM = ComponentMapper.create(this.getWorld().getDatabase(), PhysicsComponent.class);
 		inpCM =  ComponentMapper.create(this.getWorld().getDatabase(), InputComponent.class);
 		transCM =  ComponentMapper.create(this.getWorld().getDatabase(), TransformationComp.class);
 		attackTexture = ContentManager.loadTexture("pinksplosion rocket.png");
 	}
 
 	@Override
 	protected void processEntity(IEntity entity) {
 		PhysicsComponent	physComp = physCM.getComponent(entity);
 		InputComponent 		inpComp = inpCM.getComponent(entity);
 		TransformationComp	posComp = transCM.getComponent(entity);
 		
 		
 		if(inpComp.isLeftMouseButtonDown()){
 			IEntity attack = this.getWorld().getEntityManager().createEmptyEntity();			
 			AttackComponent attackComp = new AttackComponent(new Circle(Vector2.Zero,20f),20, ImmutableList.of("Enemies"));
 			
 			Vector2 attackSpeed = Vector2.subtract(inpComp.getMousePosition(), posComp.getPosition());
 			attackSpeed = Vector2.norm(attackSpeed);
 			
 			PhysicsComponent attackPhysComp = new PhysicsComponent(attackSpeed);
 			
 			TransformationComp attackPosComp = new TransformationComp();
 			
 			SpatialComponent attackSpatComp = new SpatialComponent(new Circle(Vector2.Zero,30f)); 
 			
 			RenderingComponent attackRendComp = new RenderingComponent();
 			attackRendComp.setColor(new Color(255,255,255, 255));
 			attackRendComp.setTexture(attackTexture);
 			
 			attackPosComp.setRotation(attackSpeed.angle());
 			attackPosComp.setPosition(Vector2.add(posComp.getPosition(), Vector2.mul(60, attackSpeed)));
 			attackPosComp.setOrigin(new Vector2(attackTexture.Width/2,attackTexture.Height/2));
 			
 			attack.addComponent(attackPosComp);
 			attack.addComponent(attackPhysComp);
 			attack.addComponent(attackComp);
 			attack.addComponent(attackRendComp);
 			attack.addComponent(attackSpatComp);
 			
 			attack.refresh();
 		}
 		
 		int speedFactor = 2;
 		if(inpComp.isGallopButtonPressed())
 			speedFactor=4;
 		
 		Vector2 velocity = new Vector2(0,0);
 		if (inpComp.isBackButtonPressed())
 			velocity=Vector2.add(velocity, new Vector2(0,1));
 		if (inpComp.isForwardButtonPressed())
 			velocity=Vector2.add(velocity, new Vector2(0,-1));
 		if (inpComp.isLeftButtonPressed())
 			velocity=Vector2.add(velocity, new Vector2(-1,0));
 		if (inpComp.isRightButtonPressed())
 			velocity=Vector2.add(velocity, new Vector2(1,0));
 		
 		if(velocity.length()!=0)
 			velocity=Vector2.norm(velocity);
 			
 		velocity = Vector2.mul(speedFactor, velocity);
 		physComp.setVelocity(velocity);
 		
 		
 	}
 
 }
