 package com.gamegear.firstwing.actors;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.badlogic.gdx.physics.box2d.World;
 import com.gamegear.firstwing.actors.json.ActorProperties;
 import com.gamegear.firstwing.actors.json.Polygon;
 
 public abstract class Actor {
 	public enum ActorState{
 		ALIVE,
 		DYING,
 		DEAD
 	}
 	
 	protected float stateTime = 0;
 	
 	private Body body;
 	private BodyDef bodyDef;
 	private TextureRegion texture;
 	private Filter filter;
 	
 	private float scale;
 	private float width;
 	private float height;
 	
 	private World world;
 	
 	private ActorProperties actor;
 	private ActorState state;
 	
 	/** @param Scale
 	 * @param Width
 	 * @param Height
 	 * @param BodyType
 	 * @param Position */
 	public Actor(ActorProperties actor, BodyType bodyType, Vector2 position, World world, Filter filter)
 	{
 		this.actor = actor;
 		this.scale = actor.Scale;
 		this.width = actor.Width;
 		this.height = actor.Height;
 		this.world = world;
 		this.bodyDef = new BodyDef();
 		this.bodyDef.type = bodyType;
 		this.bodyDef.position.set(position);
 		this.filter = filter;
 	}
 	
 	protected void setShape(Shape shape, float density)
 	{
 		this.body = this.world.createBody(bodyDef);
 		this.body.createFixture(shape, density);
 		this.body.getFixtureList().get(0).setFilterData(filter);
 		this.body.setUserData(this);
 		shape.dispose();
 	}
 	
 	protected Shape createShape(ActorProperties actor, Boolean rectangle){
 		Shape shape;
 		if(actor.Polygons != null && actor.Polygons.size() > 2){
 			shape = new PolygonShape();
 			ArrayList<Vector2> polygons = new ArrayList<Vector2>();
 			for(Polygon poly : actor.Polygons){
 				polygons.add(new Vector2(poly.X, poly.Y));				
 			}
 			Vector2[] vectrices = new Vector2[polygons.size()];
 			((PolygonShape) shape).set(polygons.toArray(vectrices));
 		} else {
 			if(rectangle){
 				shape = new PolygonShape();
 				((PolygonShape) shape).setAsBox((actor.Width * actor.Scale) / 2, (actor.Height * actor.Scale) /2);
 			} else {
 				shape = new CircleShape();
 				shape.setRadius((actor.Width * actor.Scale) / 2);
 			}
 		}
 		
 		return shape;
 	}
 	
 	public void update(float delta){
 		stateTime += delta;
 	}
 	
 	public Body getBody(){
 		return this.body;
 	}
 	
 	public BodyDef getBodyDef(){
 		return this.bodyDef;
 	}
 	
 	public Vector2 getPosition() {
 		return this.body.getPosition();
 	}
 	
 	public float getScale() {
 		return this.scale;
 	}
 	
 	public float getWidth() {
 		return this.width;
 	}
 
 	public float getHeight() {
 		return this.height;
 	}
 	
 	public TextureRegion getTexture(){
 		this.draw();
 		return this.texture;
 	}
 	
 	public ActorProperties getProperties(){
 		return this.actor;
 	}
 	
 	public void setState(ActorState state, boolean resetStateTime){
 		if(resetStateTime){
 			this.stateTime = 0;
 		}
 		
 		this.state = state;
 	}
 	
 	public ActorState getState(){
 		return this.state;
 	}
 	
 	protected void setTexture(TextureRegion texture){
 		this.texture = texture;
 	}
 	
 	protected abstract void loadTextures();
 	
 	protected abstract void draw();
 	
 }
