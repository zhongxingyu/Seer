 package entitySystems;
 
 import java.io.IOException;
 
 import com.google.common.collect.ImmutableList;
 
 import tests.TextureTest;
 import utils.Circle;
 import math.Vector2;
 import components.AttackComponent;
 import components.InputComponent;
 import components.PhysicsComponent;
 import components.PositionComponent;
 import components.RenderingComponent;
 import components.SpatialComponent;
 import entityFramework.ComponentMapper;
 import entityFramework.EntitySingleProcessingSystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 import graphics.Color;
 import graphics.TextureLoader;
 
 /**
  * 
  * @author Joakim Johansson
  *
  */
 public class CharacterControllerSystem extends EntitySingleProcessingSystem{
 
 	public CharacterControllerSystem(IEntityWorld world) {
 		super(world, InputComponent.class, PhysicsComponent.class, PositionComponent.class);
 	}
 	private ComponentMapper<PhysicsComponent> physCM;
 	private ComponentMapper<InputComponent> inpCM;
 	private ComponentMapper<PositionComponent> posCM;
 	@Override
 	public void initialize() {
 		physCM = ComponentMapper.create(this.getWorld().getDatabase(), PhysicsComponent.class);
 		inpCM =  ComponentMapper.create(this.getWorld().getDatabase(), InputComponent.class);
 		posCM =  ComponentMapper.create(this.getWorld().getDatabase(), PositionComponent.class);
 	}
 
 	@Override
 	protected void processEntity(IEntity entity) {
 		PhysicsComponent	physComp = physCM.getComponent(entity);
 		InputComponent 		inpComp = inpCM.getComponent(entity);
 		PositionComponent	posComp = posCM.getComponent(entity);
 		
 		
 		if(inpComp.isLeftMouseButtonDown()){
 			IEntity attack = this.getWorld().getEntityManager().createEmptyEntity();
 			AttackComponent attackComp = new AttackComponent(new Circle(new Vector2(0,0),20f),20, ImmutableList.of("Enemies"));
 			Vector2 attackSpeed = Vector2.subtract(inpComp.getMousePosition(), posComp.getPosition());
 			attackSpeed = Vector2.norm(attackSpeed);
 			PhysicsComponent attackPhysComp = new PhysicsComponent(attackSpeed);
			PositionComponent attackPosComp = new PositionComponent(posComp.getPosition(),attackSpeed.angle());
 			attackPosComp.setPosition(Vector2.add(attackPosComp.getPosition(), Vector2.mul(60, attackSpeed)));
 			RenderingComponent attackRendComp = new RenderingComponent();
 			SpatialComponent attackSpatComp = new SpatialComponent(new Circle(((PositionComponent) posComp.clone()).getPosition(),30f)); 
 			
 			attackRendComp.setColor(new Color(255,255,255, 255));
 			try {
 				attackRendComp.setTexture(TextureLoader.loadTexture(TextureTest.class.getResourceAsStream("pinksplosion rocket.png")));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
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
