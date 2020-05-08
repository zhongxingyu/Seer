 /*****************************************************************************
  * 
  * Copyright 2012 See AUTHORS file.
  * 
  * This file is part of Escape-IR.
  * 
  * Escape-IR is free software: you can redistribute it and/or modify
  * it under the terms of the zlib license. See the COPYING file.
  * 
  *****************************************************************************/
 
 package fr.escape.game.entity.ships;
 
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 import android.graphics.Rect;
 
 import fr.escape.Objects;
 import fr.escape.app.Engine;
 import fr.escape.app.Graphics;
 import fr.escape.game.User;
 import fr.escape.game.entity.CollisionBehavior;
 import fr.escape.game.entity.Entity;
 import fr.escape.game.entity.EntityContainer;
 import fr.escape.game.entity.weapons.Weapon;
 import fr.escape.game.entity.weapons.shot.Shot.ShotContext;
 import fr.escape.graphics.AnimationTexture;
 import fr.escape.graphics.Texture;
 
 /**
  * This class provide a skeletal implementation of any {@link Ship} in the game.
  */
 public abstract class AbstractShip implements Ship {
 	
 	//private static final String TAG = AbstractShip.class.getSimpleName();
 	private final Engine engine;
 	
 	private final BodyDef bodyDef;
 	private final FixtureDef fixture;
 	private final List<Weapon> weapons;
 	private final EntityContainer econtainer;
 	private final AnimationTexture shipDrawable;
 	private final CollisionBehavior collisionBehavior;
 	private final int initialLife;
 
 	private int activeWeapon;
 	private boolean isWeaponLoaded;
 	
 	private Body body;
 	private int angle;
 	private int life;
 	
 	/**
 	 * AbstractShip Constructor
 	 * 
 	 * @param bodyDef : The body definition for the JBox2D Object.
 	 * @param fixture : Fixture linked to the JBox2D Body.
 	 * @param weapons : List of {@link Weapon} usable by an {@link AbstractShip}.
 	 * @param life : Initial life of the {@link AbstractShip}.
 	 * @param container : {@link EntityContainer} in witch the {@link AbstractShip} is contained.
 	 * @param textures : {@link Texture} use for the {@link AbstractShip}.
 	 * @param collisionBehavior : Behavior used by this {@link AbstractShip} to manage JBox2D Collisions.
 	 */
 	public AbstractShip(Engine engine, BodyDef bodyDef, FixtureDef fixture, List<Weapon> weapons, int life, EntityContainer container, AnimationTexture textures, CollisionBehavior collisionBehavior) {
 		
 		this.engine = engine;
 		this.bodyDef = Objects.requireNonNull(bodyDef);
 		this.fixture = Objects.requireNonNull(fixture);
 		this.weapons = Objects.requireNonNull(weapons);
 		this.econtainer = Objects.requireNonNull(container);
 		this.shipDrawable = Objects.requireNonNull(textures);
 		this.collisionBehavior = Objects.requireNonNull(collisionBehavior);
 		
 		this.activeWeapon = 0;
 		this.isWeaponLoaded = false;
 		this.life = life;
 		this.initialLife = life;
 		
 	}
 	
 	@Override
 	public boolean damage(int value) {
 
 		if(value < 0) {
 			throw new IllegalArgumentException();
 		}
 		
 		life -= value;
 		
 		if(life <= 0) {
 			//Foundation.ACTIVITY.debug(TAG, "A Ship has been destroy.");
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public Weapon getActiveWeapon() {
 		return weapons.get(activeWeapon);
 	}
 	
 	@Override
 	public List<Weapon> getAllWeapons() {
 		return weapons;
 	}
 	
 	@Override
 	public void setActiveWeapon(int which) {
 		
 		if(which < 0 || which >= weapons.size()) {
 			throw new IndexOutOfBoundsException();
 		}
 		
 		getActiveWeapon().unload();
 		
 		this.isWeaponLoaded = false;
 		this.activeWeapon = which;
 	}
 	
 	@Override
 	public BodyDef getBodyDef() {
 		return bodyDef;
 	}
 	
 	@Override
 	public Body getBody() {
 		return body;
 	}
 	
 	@Override
 	public void setBody(Body body) {
 		this.body = body;
 	}
 	
 	@Override
 	public void createBody(World world) {
 		Objects.requireNonNull(world);
 		if(body == null) {
 			body = world.createBody(bodyDef);
 			body.createFixture(fixture);
 			body.setUserData(this);
 		}
 	}
 	
 	@Override
 	public float getX() {
 		return body.getPosition().x;
 	}
 	
 	@Override
 	public float getY() {
 		return body.getPosition().y;
 	}
 	
 	@Override
 	public void draw(Graphics graphics) {
 		Objects.requireNonNull(graphics);
 		
 		int x = engine.getConverter().toPixelX(getX()) - (shipDrawable.getWidth() / 2);
 		int y = engine.getConverter().toPixelY(getY()) - (shipDrawable.getHeight() / 2);
 			
 		graphics.draw(shipDrawable, x, y, x + shipDrawable.getWidth(), y + shipDrawable.getHeight(), angle);
 		
 	}
 	
 	@Override
 	public void update(Graphics graphics, long delta) {
 		Objects.requireNonNull(graphics);
 		
 		draw(graphics);
 		getActiveWeapon().update(graphics, delta);
 		
 		if(!econtainer.isInside(getEdge())) {
 			econtainer.edgeReached(this);
 		}
 	}
 	
 	@Override
 	public void rotateBy(int angle) {
 		this.setRotation(this.angle + angle);
 	}
 	
 	@Override
 	public void setRotation(int angle) {
 		this.angle = angle % 360;
 	}
 		
 	@Override
 	public boolean isWeaponLoaded() {
 		return isWeaponLoaded;
 	}
 	
 	@Override
 	public boolean loadWeapon() {
 		
 		Weapon activeWeapon = getActiveWeapon();
 		
 		if(activeWeapon.load(getX(), getY() - engine.getConverter().toMeterY(shipDrawable.getHeight()), 
 				new ShotContext(isPlayer(), shipDrawable.getWidth(), shipDrawable.getHeight()))) {
 			
 			isWeaponLoaded = true;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean reloadWeapon(int which, int number) {
 		if(which < 0 || which >= weapons.size()) {
 			throw new IndexOutOfBoundsException();
 		}
 		
 		return Objects.requireNonNull(weapons.get(which)).reload(number);
 	}
 	
 	@Override
 	public boolean fireWeapon() {
 		return loadWeapon() && fireWeapon(new float[]{0.0f, 0.0f, 5.0f});
 	}
 	
 	@Override
 	public boolean fireWeapon(float[] velocity) {
 		
 		Weapon activeWeapon = getActiveWeapon();
 		
 		if(activeWeapon.fire(velocity, new ShotContext(isPlayer(), shipDrawable.getWidth(), shipDrawable.getHeight()))) {
 			isWeaponLoaded = false;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void moveTo(float x, float y) {
 		float distanceX = x - getX();
 		float distanceY = y - getY();
 		
 		float max = Math.max(Math.abs(distanceX), Math.abs(distanceY));
		float coeff = 5.0f / max;
		
 		getBody().setLinearDamping((coeff));
 		getBody().setLinearVelocity(new Vec2(distanceX * coeff, distanceY * coeff));
 	}
 	
 	@Override
 	public void moveBy(float[] velocity) {
 		throw new UnsupportedOperationException();
 	}
 	
 	@Override
 	public Rect getEdge() {
 		
 		int x = engine.getConverter().toPixelX(getX());
 		int y = engine.getConverter().toPixelY(getY());
 		
 		return new Rect(x - (shipDrawable.getWidth() / 2), y + (shipDrawable.getHeight() / 2), x + (shipDrawable.getWidth() / 2), y - (shipDrawable.getHeight() / 2));
 	}
 	
 	@Override
 	public void collision(final User user, final Entity e, final int whois) {
 		Objects.requireNonNull(user);
 		Objects.requireNonNull(e);
 		
 		engine.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				getCollisionBehavior().applyCollision(user, AbstractShip.this, e, whois);
 			}
 			
 		});
 	}
 	
 	/**
 	 * @return Return the {@link CollisionBehavior} of the {@link Ship}.
 	 */
 	final CollisionBehavior getCollisionBehavior() {
 		return collisionBehavior;
 	}
 	
 	/**
 	 * @return Return the {@link EntityContainer} in which the {@link Ship} is contained.
 	 */
 	final protected EntityContainer getEntityContainer() {
 		return econtainer;
 	}
 
 	/**
 	 * @return Return the {@link AnimationTexture} of the {@link Ship}.
 	 */
 	final protected AnimationTexture getShipDrawable() {
 		return shipDrawable;
 	}
 
 	@Override
 	public boolean reset(float x, float y) {
 		
 		life = initialLife;
 		
 		// Unload current Weapon
 		getActiveWeapon().unload();
 		
 		// Reset Body
 		getBody().setLinearVelocity(new Vec2(0.0f,0.0f));
 		getBody().setTransform(new Vec2(x, y), getBody().getAngle());
 		
 		// Reset All Weapons
 		for(Weapon w : getAllWeapons()) {
 			if(!w.reset()) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public int getCurrentLife() {
 		return life;
 	}
 	
 	@Override
 	public int getInitialLife() {
 		return initialLife;
 	}
 	
 	protected Engine getEngine() {
 		return engine;
 	}
 	
 }
