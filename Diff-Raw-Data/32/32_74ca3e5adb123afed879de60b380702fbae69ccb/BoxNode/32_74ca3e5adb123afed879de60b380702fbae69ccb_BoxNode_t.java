 package com.siegedog.hava.engine;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.siegedog.hava.derpygame.GameplayScreen;
 
 public class BoxNode extends Node {
 
 	// The basic box body
 	protected Body body;
 	
 	// Useful fixture definition field
 	protected FixtureDef fDef;
 	protected BodyDef bDef;
 	
 	protected PolygonShape polyShape;
 	
 	// Reference to the main B2D world
 	protected World world;
 	
 	protected Fixture bodyFixture;
 	
 	public BoxNode(BodyDef bDef, FixtureDef fDef) {
 		super("B2DNode");
 		world = GameplayScreen.get().getWorld();
 		if(bDef == null) {
 			bDef = new BodyDef();
 			bDef.type = BodyType.DynamicBody;
 			bDef.position.set(0, 0);
 			bDef.linearDamping = 0f;
 			bDef.fixedRotation = true;
 		}
 		
 		if(fDef == null) {
 			fDef = new FixtureDef();
 			
 			polyShape = new PolygonShape();
 			polyShape.setAsBox(32 / (2 * PIXELS_PER_METER), 32 / (2 * PIXELS_PER_METER));
 			
 			fDef.shape = polyShape;
 			fDef.density = 100f;
 			fDef.friction = 0.3f;
 			fDef.restitution = 0f;
 		}
 		
 		this.bDef = bDef;
 		this.fDef = fDef;
 		body = world.createBody(bDef);
 		bodyFixture = body.createFixture(fDef);
 		
 	}
 
 	@Override
 	protected Object handleMsg(Message message) {
 			return null;
 	}
 	
 	public Body getBody() {
 		return body;
 	}
 	
 	@Override
 	public void update(float delta) {
 		// TODO Auto-generated method stub
 		super.update(delta);
 	}
 	
 	protected void setPosition(float x, float y) {
 		body.setTransform(x, y, 0);
 	}
 	
 	protected void setDimensions(float w, float h) {
 		((PolygonShape)bodyFixture.getShape()).setAsBox(w / ( 2 * PIXELS_PER_METER), h / ( 2 * PIXELS_PER_METER));
 	}
 
 	public Node syncSprite(RenderNode2D ren) {
 		Sprite s = ren.getSprite();
 		Vector2 v = body.getPosition();
 
 		s.setPosition(v.x * PIXELS_PER_METER - s.getWidth() / 2, v.y * PIXELS_PER_METER - s.getHeight() / 2);
 		return this;
 	}
 
 }
