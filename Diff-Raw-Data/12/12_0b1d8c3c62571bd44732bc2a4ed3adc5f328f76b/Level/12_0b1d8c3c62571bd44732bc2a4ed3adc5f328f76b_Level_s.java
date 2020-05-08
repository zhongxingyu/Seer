 package com.secondhand.model;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.secondhand.opengl.Circle;
 
 public class Level {
 
 	private List<Entity> entityList;
 	private int maxSize;
 	private PhysicsWorld pW;
 	private PhysicsHandler playerHandler;
 	private Player player;
 
 	// many constructors necessary?
 	// default maxsize?
 	public Level() {
 		this(100);
 	}
 
 	public Level(int maxSize) {
 		this(maxSize, new PhysicsWorld(new Vector2(), true), new Player(
 				new Vector2(), 10));
 	}
 
 	public Level(int maxSize, PhysicsWorld pW, Player p) {
 		this.maxSize = maxSize;
 		this.pW = pW;
 		player = p;
 	}
 
 	public void addEntity(Entity entity) {
 		entityList.add(entity);
 	}
 
 	public void removeEntity(Entity entity) {
 		entityList.remove(entity);
 	}
 
 	public void setEntetyList(List<Entity> list) {
 		entityList = list;
 	}
 
 	public List<Entity> getEntityList() {
 		return entityList;
 	}
 
 	public PhysicsWorld getPhysics() {
 		return pW;
 	}
 
 	public void registerEntities() {
 		registerPlayer();
 		for (Entity e : entityList) {
 			registerEntity(e);
 		}
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void registerEntity(Entity entity) {
 
 		IShape sh = new Circle(0, 0, entity.getRadius());
 
 		PhysicsHandler pH = new PhysicsHandler(sh);
 
 		sh.registerUpdateHandler(pH);
 
 		Body body = PhysicsFactory.createCircleBody(pW, sh,
 				BodyType.DynamicBody,
 				PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));
 		// a connection between a body and an entity
 		body.setUserData(entity);
 
 		pW.registerPhysicsConnector(new PhysicsConnector(sh, body, true, true));
 
 	}
 
 	// i separate player so that its easier to to reach it
 	public void registerPlayer() {
 
 		IShape sh = new Circle(0, 0, player.getRadius());
 
 		playerHandler = new PhysicsHandler(sh);
 
 		sh.registerUpdateHandler(playerHandler);
 
 		Body body = PhysicsFactory.createCircleBody(pW, sh,
 				BodyType.DynamicBody,
 				PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));
 
 		// a connection between a body and an entity
 		body.setUserData(player);

 		pW.registerPhysicsConnector(new PhysicsConnector(sh, body, true, true));
 	}
 
 	// I wonder if all this is needed
 	// Do we even use the vectors in entity?
 	// to me it seems that box2d works that out for us
 	public void moveEntitys(Vector2 v) {
 		if (v.x + v.y == 0) {
 			playerHandler.accelerate(v.x, v.y);
 		}
 
 		Iterator<Body> bit = pW.getBodies();
 		Body tmp;
 		Entity e;
 		while (bit.hasNext()) {
 			tmp = bit.next();
 			e = (Entity) tmp.getUserData();
 			e.setVector(tmp.getPosition());
 		}
 
 	}
 
 	public boolean checkPlayerSize() {
 		return player.getRadius() >= maxSize;
 
 	}
 
 }
