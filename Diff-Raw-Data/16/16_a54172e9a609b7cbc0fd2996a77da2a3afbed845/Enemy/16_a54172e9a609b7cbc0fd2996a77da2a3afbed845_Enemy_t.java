 package com.gamegear.firstwing.actors;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gamegear.firstwing.WorldRenderer;
 
 public class Enemy extends Actor {
 	
 	protected int health;
 	protected int speed;
 
 	private TextureRegion idleLeft;
 	
 	public Enemy(Vector2 position, World world){
		super(4f, 1, 0.5f, 0.5f, BodyType.DynamicBody, position);
 		this.loadTextures();
 		
		CircleShape rect = new CircleShape();
		rect.setRadius(0.5f / 2);
		super.setShape(world, rect);
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
