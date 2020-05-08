 package com.gamegear.firstwing.actors;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gamegear.firstwing.WorldRenderer;
 
 public class Enemy extends Actor {
 	
 	protected int health;
 	protected int speed;
 
 	private TextureRegion idleLeft;
 	
 	public Enemy(Vector2 position, World world){
		this.SIZE = 0.5f;
 		this.loadTextures();
		this.bodyDef.position.set(position);
		this.bodyDef.type = BodyType.DynamicBody;
		this.body = world.createBody(bodyDef);
 		
		PolygonShape rect = new PolygonShape();
		rect.setAsBox(SIZE / 2, SIZE / 2);
		this.body.createFixture(rect, 0f);
		this.body.setUserData(this);
 		rect.dispose();
 	}
 	
 	@Override
 	protected void loadTextures() {
 		this.idleLeft = WorldRenderer.atlas.findRegion("bob-01");
 	}
 
 	@Override
 	protected void draw() {
 		this.texture = this.idleLeft;
 	}
 }
