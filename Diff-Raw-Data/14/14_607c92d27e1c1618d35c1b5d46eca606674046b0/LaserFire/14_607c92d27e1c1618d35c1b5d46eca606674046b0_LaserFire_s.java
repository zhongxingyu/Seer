 package com.spotlightcoding.components;
 
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.spotlightcoding.Component;
 import com.spotlightcoding.Entity;
 import com.spotlightcoding.World;
 
 public class LaserFire extends Component{
 	//------------------------------------------------CONSTANTS
 	private final float LEFT = -0.5f;
 	private final float RIGHT = 0.5f;
	public final int TOP_HOLE = 40;
	public final int BOTTOM_HOLE = 65;
 	private final int BOTROB_DISTANCE = 200;
 	private final int BOTLAS_DISTANCE = 400;
 	
 	//------------------------------------------------PROPERTIES
 	private float robBotDistance;
 	private float laserBotDistance;
 	private String direction;
 	private Entity rob;
 	private Entity laser;
 	private Random random;
 	
 	//------------------------------------------------CONSTRUCTOR
 	public LaserFire(Entity myRob,Entity myLaser){
 		this.rob = myRob;
 		this.laser = myLaser;
 		random = new Random();
 	}
 	//------------------------------------------------PUBLIC METHODS
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		if(rob.getState() != Entity.DEAD){
 			Vector2f robPosition = rob.getPosition();
 			Vector2f laserPosition = laser.getPosition();
 			Vector2f position = owner.getPosition();
 			
 			robBotDistance = position.x - robPosition.x;
 			laserBotDistance = position.x - laserPosition.x;
 			
 			if(robBotDistance >= 0){
 				direction = "Left";
 			}else{
 				direction = "Right";
 			}
 			
 			if(robBotDistance < 200){
 				if(laserBotDistance > 400){
 					owner.setBotActive(false);
 				}else{
 					owner.setBotActive(true);
 				}
 			}else{
 				if(laserBotDistance > 400){
 					owner.setBotActive(false);
 				}
 			}
 			
 			if(owner.isBotActive()){
 				if(direction == "Left"){
 					laser.setPosition(new Vector2f(laserPosition.x += LEFT * delta,laserPosition.y));
 				}else{
 					owner.setBotActive(false);
 					//laser.setPosition(new Vector2f(laserPosition.x += RIGHT * delta,laserPosition.y));
 				}
 			}else{
 				boolean x = random.nextBoolean();
 				int y = 0;
 				if(x){
 					 y = TOP_HOLE;
 				}else{
 					 y = BOTTOM_HOLE;
 				}
 				laser.setPosition(new Vector2f(owner.getPosition().x,(int)(World.GROUND_LEVEL - owner.getSize().getHeight()) +y));
 			}
 
 			
 			if((Math.round(laser.getPosition().x) >= Math.round((rob.getPosition().x))) && 
 					(Math.round(laser.getPosition().x) <= Math.round((rob.getPosition().x + rob.getSize().width)))){
 				//have to check for y
 				
				System.out.println(Math.round(laser.getPosition().x));
 			}
 			
 			if(Math.round(laser.getPosition().x) == 440){
 				
 			}
 			
 			
 			
 		}
 	}
 
 }
