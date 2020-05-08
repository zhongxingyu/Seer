 package catt.kedavra;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import catt.kedavra.components.Updatable;
 import catt.kedavra.entities.Collidable;
 
 /**
  * The Great and Powerful Collidinator, as told in legend, enacts judgment on all the Entities in the game world, deciding
  * whether or not they collide at any given moment.  The methods through which this occurs are poorly understood, but physicists
  * and philosophers alike are scrambling to better understand its mysterious ways.
  * @author Catt
  *
  */
 public class Collidinator implements Updatable {
 	
 	private ArrayList<Collidable> collisionRoll = new ArrayList<Collidable>();
 	
 	/**
 	 * Adds the specified Collidable Entity to the collision roll.
 	 * @param collidee The specified Collidable Entity.
 	 */
 	public void add(Collidable collidee){
 		collisionRoll.add(collidee);
 	}
 	
 	/**
 	 * Removes the specified Collidable Entity from the collision roll.
 	 * @param collidee The specified Collidable Entity.
 	 */
 	public void remove(Collidable collidee){
 		collisionRoll.remove(collidee);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) {
 		//c//Check each Entity for collisions in the slowest, most straight-forward way. (Optimizations to come.)
 		//System.out.println("STEP");
 		for(int i = 0; i < collisionRoll.size(); ++i){
 			Collidable a = collisionRoll.get(i);
 			if (a.getCollisionType() != Collidable.CT_NONE)
 				for(int j = i+1; j < collisionRoll.size(); ++j){
 					Collidable b = collisionRoll.get(j);
 					if (b.getCollisionType() != Collidable.CT_NONE){
 						Collidinator.collide(sbg, a, b);
 						//System.out.print(a.toString());System.out.print(" , ");System.out.println(b.toString());
 					}
 				}	
 		}	
 	}
 	
 	/**
 	 * Enacts judgment on two Collidable objects, a and b.  If the objects collide, their collision methods will be called.
 	 * @param a A Collidable object
 	 * @param b Another Collidable object
 	 */
 	public static void collide(StateBasedGame sbg, Collidable a, Collidable b){
 		
 		//c//If either Collidable has collisions disabled, do nothing.
 		if (a.getCollisionType() == Collidable.CT_NONE || b.getCollisionType() == Collidable.CT_NONE)
 			return;
 		
 		//c//If both Collidables have bounding circles, use circular radii to find overlap.
 		if (a.getCollisionType() == Collidable.CT_CIRCLE && b.getCollisionType() == Collidable.CT_CIRCLE){
 			int radA = a.getCollisionRadii()[0];
 			int radB = b.getCollisionRadii()[0];
 			float distance = Math.abs(a.getPosition().distance(b.getPosition()));
 			float offset = distance - (radA + radB);
 			if(offset < 0){
 				//c//Find the offset vector.
 				Vector2f vecA = new Vector2f();
 				Vector2f vecB = new Vector2f();
 				vecA.set(Math.abs(offset), 0);
 				vecA.setTheta(Math.toDegrees(Math.atan2((a.getPosition().y-b.getPosition().y),(a.getPosition().x-b.getPosition().x))));
 				vecB.set(Math.abs(offset), 0);
 				vecB.setTheta(Math.toDegrees(Math.atan2((b.getPosition().y-a.getPosition().y),(b.getPosition().x-a.getPosition().x))));
 				//c//Collide!
 				a.collision(sbg, b, vecA);
 				b.collision(sbg, a, vecB);
				return;
 			}
			
 		}
 		//c//If both Collidables have bounding rectangles, use rectangular radii to find overlap.
 		if (a.getCollisionType() == Collidable.CT_RECTANGLE && b.getCollisionType() == Collidable.CT_RECTANGLE){
 			//Get the distance components between the two entities.
 			float distX = a.getPosition().getX() - b.getPosition().getX();
 			float distY = a.getPosition().getY() - b.getPosition().getY();
 			float hws = a.getCollisionRadii()[0]+b.getCollisionRadii()[0];
 			float hhs = a.getCollisionRadii()[1]+b.getCollisionRadii()[1];
 			//If the entities are intersecting on the Y axis...
 			if (hhs >= Math.abs(distY)){
 				//And the Y axis...
 				if (hws > Math.abs(distX)){
 					//Get the overlap between the two entities.
 					float overlapX = hws - Math.abs(distX);
 					float overlapY = hhs - Math.abs(distY);
 					
 					if(Math.abs(overlapX)<Math.abs(overlapY)){
 						if(distX < 0){
 							//Collision to the right.
 							a.collision(sbg, b, new Vector2f(-overlapX,0));
 							b.collision(sbg, a, new Vector2f(overlapX,0));
							return;
 						}
 						else{
 							//Collision to the left.
 							a.collision(sbg, b, new Vector2f(overlapX,0));
 							b.collision(sbg, a, new Vector2f(-overlapX,0));
							return;
 						}	
 					}
 					else{
 						if(distY < 0){
 							//Collision below.
 							a.collision(sbg, b, new Vector2f(0,-overlapY));
 							b.collision(sbg, a, new Vector2f(0,overlapY));
							return;
 						}
 						else{
 							//Collision above.
 							a.collision(sbg, b, new Vector2f(0,overlapY));
 							b.collision(sbg, a, new Vector2f(0,-overlapY));
							return;
 						}
 					}
 				}
 			}
 		}
 		
 		//c//If the Collidables have different bounding types, do some magic.
 		float distX, distY, hws, hhs;
 		if(a.getCollisionType() == Collidable.CT_RECTANGLE){
 			distX = a.getPosition().getX() - b.getPosition().getX();
 			distY = a.getPosition().getY() - b.getPosition().getY();
 			hws = a.getCollisionRadii()[0]+b.getCollisionRadii()[0];
 			hhs = a.getCollisionRadii()[1]+b.getCollisionRadii()[0];
 		}
 		else{
 			distX = a.getPosition().getX() - b.getPosition().getX();
 			distY = a.getPosition().getY() - b.getPosition().getY();
 			hws = a.getCollisionRadii()[0]+b.getCollisionRadii()[0];
 			hhs = a.getCollisionRadii()[0]+b.getCollisionRadii()[1];
 		}
 		//If the entities are intersecting on the Y axis...
 		if (hhs >= Math.abs(distY)){
 			//And the Y axis...
 			if (hws > Math.abs(distX)){
 				//Get the overlap between the two entities.
 				float overlapX = hws - Math.abs(distX);
 				float overlapY = hhs - Math.abs(distY);
 				
 				if(Math.abs(overlapX)<Math.abs(overlapY)){
 					if(distX < 0){
 						//Collision to the right.
 						a.collision(sbg, b, new Vector2f(-overlapX,0));
 						b.collision(sbg, a, new Vector2f(overlapX,0));
						return;
 					}
 					else{
 						//Collision to the left.
 						a.collision(sbg, b, new Vector2f(overlapX,0));
 						b.collision(sbg, a, new Vector2f(-overlapX,0));
						return;
 					}	
 				}
 				else{
 					if(distY < 0){
 						//Collision below.
 						a.collision(sbg, b, new Vector2f(0,-overlapY));
 						b.collision(sbg, a, new Vector2f(0,overlapY));
						return;
 					}
 					else{
 						//Collision above.
 						a.collision(sbg, b, new Vector2f(0,overlapY));
 						b.collision(sbg, a, new Vector2f(0,-overlapY));
						return;
 					}
 				}
 			}
 		}
 	}
 
 }
