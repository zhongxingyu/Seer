 package com.razh.laser.entities;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.World;
 import com.razh.laser.Geometry;
 import com.razh.laser.MeshActor;
 
 public class EntityFactory {
 	public static Entity createLaserSource(World world) {
 		MeshActor actor = new MeshActor();
 		actor.setMesh(Geometry.createRing(1.0f, 0.8f, 0, 180 * MathUtils.degreesToRadians, 32, true));
 //		actor.setMesh(Geometry.createCircle(64));
 		actor.setMode(GL20.GL_TRIANGLE_STRIP);
 
 		actor.setGeometry(Geometry.createRingHull(1.0f, 0.8f, 0, 180 * MathUtils.degreesToRadians, 4, true));
 		actor.setColor(Color.RED);
 //		actor.setPosition((float) (Gdx.graphics.getWidth() * Math.random()),
 //		                  (float) (Gdx.graphics.getHeight() * Math.random()));
 		actor.setPosition(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.2f);
 		actor.setWidth(100);
 		actor.setHeight(100);
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(actor.getX(), actor.getY());
 
 		Body body = world.createBody(bodyDef);
 
 		CircleShape circle = new CircleShape();
 		circle.setRadius(actor.getWidth());
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = circle;
 
		body.createFixture(fixtureDef);
 
 		circle.dispose();
 
 		Entity entity = new Entity();
 		entity.setActor(actor);
 		entity.setBody(body);
 		actor.setEntity(entity);
 
 		return entity;
 	}
 }
