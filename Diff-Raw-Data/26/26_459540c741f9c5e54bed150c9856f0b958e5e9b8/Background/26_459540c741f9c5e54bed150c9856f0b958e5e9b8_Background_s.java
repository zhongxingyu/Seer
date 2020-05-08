 package com.devcamp.cadejo.actors;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Background {
 	
 	public static final float SPEED = -7.75f;
 	public static final float SIZE_W = 20f;
 	public static final float SIZE_H = 14f;
 	public static Vector2 tmp = new Vector2();
 	
 	Vector2 position = new Vector2();
 	Vector2 acceleration = new Vector2();
 	Vector2 velocity = new Vector2();
 	Rectangle bounds = new Rectangle();
 	private int id;
 	
 	public Background(Vector2 position, int id){
 		this.velocity.x = SPEED;
 		this.position = position;
 		this.bounds.height = SIZE_H;
 		this.bounds.width = SIZE_W;
 		this.id = id;
 	}
 	
 	public void update(float delta){
		tmp = velocity.cpy();
 		position.add(tmp.scl(delta));
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public Vector2 getPosition() {
 		return position;
 	}
 
 	public void setPosition(Vector2 position) {
 		this.position = position;
 	}
 
 	public Vector2 getAcceleration() {
 		return acceleration;
 	}
 
 	public void setAcceleration(Vector2 acceleration) {
 		this.acceleration = acceleration;
 	}
 
 	public Vector2 getVelocity() {
 		return velocity;
 	}
 
 	public void setVelocity(Vector2 velocity) {
 		this.velocity = velocity;
 	}
 
 	public Rectangle getBounds() {
 		return bounds;
 	}
 
 	public void setBounds(Rectangle bounds) {
 		this.bounds = bounds;
 	}
 	
 	@Override
 	public boolean equals(Object i){
 		if(((Background)i).id == this.id)
 			return true;
 		return false;
 	}
 	
 	
 
 }
