 package com.secondhand.view.entities;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.secondhand.model.PolygonEntity;
 import com.secondhand.view.opengl.Polygon;
 import com.secondhand.view.physics.MyPhysicsFactory;
 
 public class PolygonView extends EntityView {

 	public PolygonView(final PhysicsWorld physicsWorld, final PolygonEntity polygonEntity , final Polygon polygon,
 			final FixtureDef fixtureDef) {

 		super(physicsWorld, polygonEntity, polygon,
 				MyPhysicsFactory.createPolygonBody(physicsWorld, polygon, BodyType.DynamicBody, fixtureDef));
		polygon.setBody(body);

 	}
 
 }
