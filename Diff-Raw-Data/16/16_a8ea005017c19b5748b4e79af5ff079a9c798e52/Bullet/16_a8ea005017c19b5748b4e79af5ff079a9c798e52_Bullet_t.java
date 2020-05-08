 package com.jumpandrun;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Fixture;
 
 public class Bullet {
 	public Body body;
 	public int age;
 	public boolean kill;
 	public int damage;
 	//public Fixture bulletPhysicsFixture;
 	
 	public Vector2 position = new Vector2();
 
 	public Bullet(float x, float y) {
 		position.x = x;
 		position.y = y;
 		age = 0;
 		kill = false;
 		damage = 50;
 	}
 	
	public void update(float delta) {
 		position.x = body.getPosition().x;
 		position.y = body.getPosition().y;
		age+= delta;
		body.setLinearVelocity(body.getLinearVelocity().x, 0);
 	}
 }
