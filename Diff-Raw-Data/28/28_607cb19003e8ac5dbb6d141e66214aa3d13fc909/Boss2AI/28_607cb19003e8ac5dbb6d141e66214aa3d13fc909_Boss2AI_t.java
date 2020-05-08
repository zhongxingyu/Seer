 package edu.csupomona.kyra.component.ai;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.geom.Line;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import edu.csupomona.kyra.component.physics.objects.ForceVector;
 import edu.csupomona.kyra.entity.Entity;
 
 public class Boss2AI extends AIComponent {
 	
 	ForceVector aim, jump;
 	Timer jumpCooldown;
 	boolean canJump;
 	final float SIGHT_RANGE = 520.0f,
 		BULLET_SPEED = 0.85f,
		MOVE_SPEED = 0.5f,
		JUMP_SPEED = -2.0f;
	final int JUMP_COOLDOWN = 1500;
 	
 	public Boss2AI(String id, Entity player1, Entity player2, TiledMap map) {
 		super(id, player1, player2, map);
 		canJump = true;
 		jumpCooldown = new Timer();
 	}
 	
 	public ForceVector getAim() {
 		return aim;
 	}
 	
 	public ForceVector getJump() {
 		if (canJump && owner.getPhysicsComponent().onFloor) {
 			canJump = false;
 			TimerTask coolDown = new TimerTask() {
 				
 				@Override
 				public void run() {
 					canJump = true;
 				}
 			};
 			jumpCooldown.schedule(coolDown, JUMP_COOLDOWN);
 			return jump;
 		}
 		return null;
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		Line attackPath = getLineToTarget();
 		if ((attackPath != null) && (attackPath.length() < SIGHT_RANGE)) {
 			aim = new ForceVector(attackPath, BULLET_SPEED, delta);
			ForceVector move = new ForceVector(attackPath, MOVE_SPEED, delta);
			jump = move.add(new ForceVector(0, delta * JUMP_SPEED ));
 		}
 		else {
 			aim = null;
 			jump = null;
 		}
 	}
 
 }
