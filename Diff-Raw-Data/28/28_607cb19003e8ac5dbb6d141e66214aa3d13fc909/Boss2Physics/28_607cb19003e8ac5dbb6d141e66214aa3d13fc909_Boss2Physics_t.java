 package edu.csupomona.kyra.component.physics;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import edu.csupomona.kyra.component.ai.Boss2AI;
 import edu.csupomona.kyra.component.physics.objects.ForceVector;
 
 public class Boss2Physics extends PhysicsComponent {
 
 	final float GRAVITY = 0.0012f,
 		FRICTION = 0.97f,
 		MIN_X = -2.0f,
 		MAX_X = 2.0f,
		MIN_Y = -5.0f,
		MAX_Y = 2.5f;
 	
 	public Boss2Physics(String id, float height, float width, TiledMap map) {
 		super(id, height, width, map);
 		forceVector = new ForceVector();
 	}
 		
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		setPolygon();
 		Boss2AI bossAI = (Boss2AI)owner.getAIComponent();
 		
 		ForceVector gravity = new ForceVector(0, delta * GRAVITY);
 		
		if (testFloorCollision(forceVector.clone().add(gravity))) {
 			onFloor = true;
 			forceVector.setYComponent(0.0f);
			forceVector.setXComponent(0.0f);
 			ForceVector jump = bossAI.getJump();
 			if (jump != null) {
 				forceVector.add(jump);
 			}
			ForceVector aim = bossAI.getAim();
			if (aim != null)
				owner.setXDirection(bossAI.getAim().getXDirection());
 		}
 		else {
 			onFloor = false;
 			forceVector.add(gravity);
 			owner.setXDirection(forceVector.getXDirection());
 		}
 		forceVector.clampX(MIN_X, MAX_X);
 		forceVector.clampY(MIN_Y, MAX_Y);
 		if (testWallCollisions(forceVector))
 			forceVector.setXComponent(0.0f);
 		if (testCeilingCollision(forceVector))
 			forceVector.setYComponent(0.0f);
 		setLocation(forceVector.shiftPosition(owner.getPosition()));
 	}
 
 }
