 package com.tiny.weapons.shots;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.tiny.tank.Camera;
 import com.tiny.tank.Main_Gameplay;
 import com.tiny.tank.Tank;
 import com.tiny.weapons.CircularShot;
 import com.tiny.weapons.Shot;
 
 public class NormalShot extends CircularShot{
 
 	//fall rate and max fall rate
 	public final float gravity = .1f;
 	public final float terminalVelocity = 5;
 	
 	
 	/**
 	 * Creates a standard circle shot with explosion of given radius
 	 * @param pos Position (duh)
 	 * @param radiusOfEffect Radius of final explosion
 	 * @param initialRadius Radius that explosion starts at
 	 * @param graphicalRep Unused currently
 	 * @param shotName String identifying name of shot
 	 */
 	public NormalShot(Vector2f pos, int radiusOfEffect, int initialRadius, Object graphicalRep, float animationLimit, float animationStep,int damage, String shotName) {
 		super(pos, radiusOfEffect, initialRadius, graphicalRep, animationLimit, animationStep, shotName);
 		setDamage(damage);
 		// TODO Auto-generated constructor stub
 		
 		animationCounter = 0;
 	}
 
 	
 	@Override
 	public void onCollisionEffect(GameContainer container) {
 		// TODO Auto-generated method stub
 		circleExplosion((int)pos.x, (int)pos.y, radiusOfEffect, container);
 		Main_Gameplay.map.update();
 	}
 	
 	public void init(Vector2f pos, Vector2f impulse){
 		super.init(pos,impulse);
 		animationCounter = 0;
 	}
 	
 	@Override
 	public boolean update(GameContainer container) {
 		// TODO Auto-generated method stub
 		
 		//Returns true if dead and not shot. Allows for removal
 		if(!isShot || !isAlive){
 			return true;
 		}
 		
 		//if collides with terrain
 		if(pointCollision()){
 			isAnimating = true;
 			/*while(pointCollision()){
 				pos.y-=1;
 			}*/
 			areaOfEffect = new Circle(pos.x,pos.y, initialRadius);
 		}
 		
 		//when animating
 		if(isAnimating){
 			//as long as radius is still growing
 			if(initialRadius < radiusOfEffect){
 				
 				animationCounter+=animationStep; //increment counter
 				if(animationCounter > animationLimit){ //if limit time has been passed
 					initialRadius++; //increase radius
 					areaOfEffect = new Circle(pos.x,pos.y, initialRadius); //and graphical rep of it
 					for(int i=0;i<Main_Gameplay.players.size();i++) {
 						if(areaOfEffect.intersects(Main_Gameplay.players.get(i).getHitbox()) ) {
 							//do damage
 						}
 					}
 					animationCounter -= animationLimit; //decrement counter
 				}
 			}
 			else{ //its dead jim
 				onCollisionEffect(container);
 				isAlive = false;
 				isShot = false;
 				isAnimating= false;
 				finished();
 			}
 		}else if(isShot){ //if not otehr states, has to be falling so update
 			//temporary
 			impulse.y-=gravity;
 			//if(impulse.y < -terminalVelocity){
 			//	impulse.y = -terminalVelocity;
 		//	}
 			pos.x+=impulse.x;
 			pos.y-=impulse.y;
 			if(pos.y > Main_Gameplay.map.getMap().getHeight()){
 				isAnimating = true;
 				areaOfEffect = new Circle(pos.x,Main_Gameplay.map.getMap().getHeight()-1, initialRadius);
 			}
 		}
 		
 		return isAlive;
 		
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g, Camera cam) {
 		// TODO Auto-generated method stub
 	
 		if(!isAlive || !isShot){
 			return;
 		}
 		
 		if(isAnimating){
 			//areaOfEffect.setCenterX(cam.scale*(pos.x-cam.pos.x));
 			//areaOfEffect.setCenterY(cam.scale*(pos.y-cam.pos.y));
 			areaOfEffect = new Circle(cam.transformScreenToCamX(pos.x),cam.transformScreenToCamY(pos.y),initialRadius*cam.getScale());
 			ArrayList<Tank> tanks = Main_Gameplay.players;
 			boolean didHit = false;
 			for(Tank t: tanks){
 				if(areaOfEffect.intersects(t.getHitbox())){
 					//add damage every tick
 					t.getStat().addToDamage(getDamage());
 					didHit = true;
 				}
 			}
 			
 			if(tanks.get(0).isTurn() && didHit){
 				tanks.get(0).setShotHit(true);
				System.out.println("player 1 hit");
 			}else if(tanks.get(1).isTurn() && didHit){
 				tanks.get(1).setShotHit(true);
				System.out.println("player 2 hit");
 			}
 			
 			g.fill(areaOfEffect);
 			
 			return;
 		}
 		g.setColor(Color.black);
 		g.fill(new Circle(cam.transformScreenToCamX(pos.x), cam.transformScreenToCamY(pos.y), 1*cam.getScale()));
 		g.setColor(Color.white);
 		
 	}
 
 	@Override
 	public void finished(){
 	
 		
 		ArrayList<Tank> players = Main_Gameplay.players;
 		
 		for(Tank t : players){
 			t.shotDone();
 			t.setFalling(true);
 			t.setShooting(false);
 		}
 		
 		
 	}
 
 
 	@Override
 	public Shot copy() {
 		// TODO Auto-generated method stub
 		return new NormalShot(pos, radiusOfEffect, initialRadius, graphicalRep, animationLimit, animationStep, damage, shotName);
 	}
 
 
 	public int getDamage() {
 		return damage;
 	}
 
 
 	public void setDamage(int damage) {
 		this.damage = damage;
 	}
 
 
 	@Override
 	public void doDamge(Tank tank) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
