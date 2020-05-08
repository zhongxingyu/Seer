 package com.siegedog.hava.derpygame;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.siegedog.hava.engine.BoxNode;
 import com.siegedog.hava.engine.Node;
 
 public class Crate extends Node {
 
 	BoxNode physics;
 	
 	public Crate(float x, float y, float w, float h) {
 		super("crate");
 		
 		FixtureDef fd = new FixtureDef();
		fd.density = 40f;
 		fd.friction = 0.2f;
 		
 		PolygonShape shape = new PolygonShape();
 		shape.setAsBox(w / ( 2 * PIXELS_PER_METER), h / ( 2 * PIXELS_PER_METER ));
 		fd.shape = shape;
 		
 		BodyDef bd = new BodyDef();
 		bd.position.set(x, y);
 		bd.type = BodyType.DynamicBody;
 		
 		this.addNode(physics = new BoxNode(bd, fd));		
 	}
 }
