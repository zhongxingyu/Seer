 package com.liongrid.gameengine;
 
 import com.liongrid.gameengine.tools.Vector2;
 
 /**
  * @author Lastis
  *	A CollisionObject is a object is used in the CollisionHandler. A CollisionObject needs to
  *	implement a subinterface of Shape to work. A CollisionObject is basically a shape with
  *	an array of all the other shapes it collides with. 
  */
 public abstract class CollisionObject implements Shape{
 	
	public static final int DEFAULT_MAX_COLLISIONS = 10;
 	
 	public int collisionCnt;
 	public CollisionObject[] collisions;
 	public Object owner;
 	
 	private int type;
 	private Vector2 pos;
 	
 	/**
 	 * A CollisionObject is basically a shape with an array of all the other shapes 
 	 * it collides with. It is used in CollisionHandler. The CollisionObject has a max
 	 * number of objects it will store in its array.
 	 * 
 	 * @param type - CollisionObject are divided into different arrays in CollisionHandler
 	 * according to their types.
 	 * @param pos - The position of the shape.
 	 * @param owner - A pointer to the owner of the shape.
 	 * @param maxCollisions - Number of max collisions the CollisionObject will store
 	 * in its array.
 	 */
 	public CollisionObject(int type, Vector2 pos, Object owner, int maxCollisions) {
 		this.type = type;
 		this.pos = pos;
 		this.owner = owner;
 		collisions = new CollisionObject[maxCollisions];
 	}
 	
 	/**
 	 * A CollisionObject is basically a shape with an array of all the other shapes 
 	 * it collides with. It is used in CollisionHandler. The CollisionObject has a max
 	 * number of objects it will store in its array. This uses the default value for
 	 * the size of the collisions array.
 	 * 
 	 * @param type - CollisionObject are divided into different arrays in CollisionHandler
 	 * according to their types.
 	 * @param pos - The position of the shape.
 	 * @param owner - A pointer to the owner of the shape.
 	 * in its array.
 	 */
 	public CollisionObject(int type, Vector2 pos, Object owner) {
 		this.type = type;
 		this.pos = pos;
 		this.owner = owner;
 		collisions = new CollisionObject[DEFAULT_MAX_COLLISIONS];
 	}
 
 	/**
 	 * This is called by the CollisionHandler and is called once with every other 
 	 * CollisionObject in the arrays of CollisionHandler.
 	 * @param object - The shape that the CollisionShape collides with.  
 	 */
 	public void collide(CollisionObject object){
 		if(collisionCnt >= collisions.length) return;
 		if(Collision.collides(this, object)){
 			collisions[collisionCnt] = object;
 			collisionCnt++;
 		}
 	}
 	
 	/**
 	 * Erases old history and makes the shape ready for new collisions.
 	 * This is usually called before a set of .collides(shape) calls.
 	 */
 	public void clear(){
 		collisionCnt = 0;
 		for(int i = 0; i < collisions.length; i++){
 			collisions[i] = null;
 		}
 	}
 	
 	/**
 	 * @return the index of the type the object represents.
 	 */
 	public int getType(){
 		return type;
 	}
 	
 	public Vector2 getPos() {
 		return pos;
 	}
 }
