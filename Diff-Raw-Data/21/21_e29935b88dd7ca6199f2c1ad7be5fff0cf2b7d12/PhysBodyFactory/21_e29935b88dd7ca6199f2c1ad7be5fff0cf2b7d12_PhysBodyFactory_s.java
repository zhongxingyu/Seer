 package com.dat255_group3.utils;
 
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.dat255_group3.model.Character;
 /**
  * A class containing methods to create physical bodies in an physics-world
  * @author Group 3
  *
  */
 public class PhysBodyFactory {
 
 	/**
 	 * Creates a body for a movable character in the physical world. 
 	 * @param physWorld , The physical world in which the character should exist and be created
 	 * @param pos , the center position of the character body (pixels)
 	 * @param size , with and height the body (pixels)
 	 * @return The physical body of the character that exists in the physWorld with a set density, friction and restitution 
 	 */
	public static Body createCharacter(World physWorld, Vector2 pos, Vector2 size, float speedM) {
 		PolygonShape shape;
 		FixtureDef fixtureDef = new FixtureDef();
 		BodyDef bodyDef = new BodyDef();
 		Body body = null;
 
 
 		shape = new PolygonShape();
 		size = CoordinateConverter.pixelToMeter(size); //convert size to meters
 		shape.setAsBox(size.x/2, size.y/2);
 
 		fixtureDef.shape = shape;
 		fixtureDef.density = 1f;
 		fixtureDef.friction = 0.5f;
 		fixtureDef.restitution = 0f;
 
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.fixedRotation = true;
 		bodyDef.position.set(CoordinateConverter.pixelToMeter(pos)); //set the position in meters
 
 		body = physWorld.createBody(bodyDef);
 		body.createFixture(fixtureDef);
 		
 
 		return body;
 	}
 	
 	//TODO javadoc
 	public static Body createRoundCharacter(Character character, World world){
 		
 		FixtureDef fixtureDef = new FixtureDef();
 		BodyDef bodyDef = new BodyDef();
 		Body body = null;
 		
 		CircleShape shape = new CircleShape();
 		shape.setRadius(CoordinateConverter.pixelToMeter(25)); //TODO get radius from characterModel
 		
 		
 		fixtureDef.shape = shape;
 		fixtureDef.density = 1f;
 		fixtureDef.friction = 0.8f;
 		fixtureDef.restitution = 0f;
 
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.fixedRotation = true;
 		bodyDef.position.set(Character.getPosition());
 
 		body = world.createBody(bodyDef);
 		body.createFixture(fixtureDef);
 		
 		return body;
 
 	}
 
 	/**
 	 * Creates a solid ground that is not affected by gravity or other forces
 	 * @param pos , the center position of the ground (pixels)
 	 * @param size , with and height of the ground (pixels)
 	 * @param friction , the friction (0f-1f) of the ground surface
 	 * @param restitution , restitution of the ground surface
 	 * @param physWorld , the physical world in which the solid ground is created and exists
 	 * @return The body whit the physical properties sent in by the parameters
 	 */
 	public static Body addSolidGround(final Vector2 pos, Vector2 size, final float friction, 
 			final float restitution, World physWorld) {
 		PolygonShape polygonShape = new PolygonShape();
 		size = CoordinateConverter.pixelToMeter(size); //convert size to meters
 		polygonShape.setAsBox(size.x, size.y);
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = polygonShape;
 		fixtureDef.friction = friction;
 		fixtureDef.density = 1f;
 		fixtureDef.restitution = restitution;
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(CoordinateConverter.pixelToMeter(pos)); //set the position in meters
 		bodyDef.type = BodyType.StaticBody;
 		bodyDef.fixedRotation = true;
 
 		Body body = physWorld.createBody(bodyDef);
 		body.createFixture(fixtureDef);
 		return body;
 	}
 	
 	
 	/*
 	 * adds an obstacle
 	 * ... ja jag vet att det r ful dubblering av kod...
 	 */
 	public static Body addObstacle(final Vector2 pos, Vector2 size, final float friction, 
 			final float restitution, World physWorld) {
 		PolygonShape polygonShape = new PolygonShape();
 		size = CoordinateConverter.pixelToMeter(size); //convert size to meters
 		polygonShape.setAsBox(size.x, size.y);
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = polygonShape;
 		fixtureDef.friction = friction;
 		fixtureDef.density = 1f;
 		fixtureDef.restitution = restitution;
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(CoordinateConverter.pixelToMeter(pos)); //set the position in meters
 		bodyDef.type = BodyType.KinematicBody;
 		bodyDef.fixedRotation = true;
 
 		Body body = physWorld.createBody(bodyDef);
 		body.createFixture(fixtureDef);
 		return body;
 	}
 
 
 }
