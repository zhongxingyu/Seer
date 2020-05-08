 package com.jantox.dungmast.entities;
 
 import java.util.ArrayList;
 
 import com.jantox.dungmast.MasterSpawner;
 import com.jantox.dungmast.Renderer;
 import com.jantox.dungmast.colsys.Circle;
 import com.jantox.dungmast.colsys.CollisionSystem;
 import com.jantox.dungmast.math.Vector2D;
 import com.jantox.dungmast.scripts.Assets;
 
 public class Skeleton extends Living {
 	
 	Vector2D vel;
 	private Vector2D gotoline;
 	private boolean gotodone = false;
 	
 	int breakTime = 0;
 	
 	ControlPoint p;
 	
 	long timedone = 0;
 	
 	public Skeleton(Vector2D pos, Vector2D gotoline) {
 		super(pos);
 		this.gotoline = gotoline;
 		
 		this.colmask = new Circle(this.pos, 11);
 		this.health = 300;
 		
 		this.sprite = Assets.loadSprite("skeleton.png");
 		
 		MasterSpawner.CURRENT_MONSTERS ++;
 		p = map.getRandomControlPoint();
 		
 		this.requestCollisions(entity_type.PLAYER, entity_type.GATE, entity_type.CONTROL_POINT, entity_type.BARRICADE);
 	}
 
 	@Override
 	public void handleCollision(Entity e) {
 		if(e instanceof Player) {
 			Player p = (Player) e;
 			if(breakTime == 0) {
 				breakTime = 20;
 				p.damage(3);
 			}
 			pos.subtract(vel);
 		} else if(e instanceof Gate) {
 			if(!((Gate)e).isBroken()) {
 				if (breakTime == 0) {
 					((Gate) e).damage(5);
 					breakTime = 1;
 					pos.subtract(vel);
 				}
 			}
 		} else if(e instanceof Barricade) {
 			((Living)e).damage(5);
 		} 
 		colmask.update(pos);
 	}
 
 	@Override
 	public void update() {
 		super.update();
 		
 		timedone++;
 		
 		if(timedone > 60 * 45) {
 			this.expired = true;
 		}
 		
 		if(breakTime > 0)
 			breakTime --;
 		
 		if(gotodone) {
 			if(p != null) {
 				if(p.distanceSquared(this) >= 20) {
 					vel = new Vector2D(p.getPosition().getX() - pos.getX(), p
 							.getPosition().getY() - pos.getY());
 					vel.normalize();
 				}
 			}
 		} else {
 			vel = new Vector2D(gotoline.x - pos.getX(), gotoline.y - pos.getY());
 			vel.normalize();
 			
 			if(gotoline.distanceSquared(pos) <= 20 * 20) {
 				gotodone = true;
 			}
 		}
 		
 		ArrayList<Entity> ale = map.getEntities(entity_type.ALL);
 		for(Entity e : ale) {
			if(e != this && !(e instanceof ControlPoint) && !(e instanceof Gate)) {
 				if(CollisionSystem.collides(e.getMask(), this.getMask())) {
 					Vector2D toa = new Vector2D(pos.x - e.pos.x, pos.y - e.pos.y);
 					
 					Vector2D nd = toa.copy();
 					nd.normalize();
 					nd.multiply(4);
 					
 					vel = nd;
 				}
 			}
 		}
 		vel.x /= 3;
 		vel.y /= 3;
 
 		pos.add(vel);
 		colmask.update(pos);
 	}
 
 	@Override
 	public void render(Renderer renderer) {
 		renderer.drawSprite(sprite, new Vector2D(pos.x - 15, pos.y - 49), true);
 	}
 
 	@Override
 	public void drop(int chance) {
 		map.spawn(new Gem(new Vector2D(pos.x + rand.nextGaussian() * 10, pos.y + rand.nextGaussian() * 10)));
 	}
 
 }
