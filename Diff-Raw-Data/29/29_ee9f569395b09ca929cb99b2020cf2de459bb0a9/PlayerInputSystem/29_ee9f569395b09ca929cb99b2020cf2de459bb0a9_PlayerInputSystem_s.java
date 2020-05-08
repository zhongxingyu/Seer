 package com.opg.javagamexyz.artemis.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector3;
 import com.opg.javagamexyz.artemis.EntityFactory;
 import com.opg.javagamexyz.artemis.components.Player;
 import com.opg.javagamexyz.artemis.components.Position;
 import com.opg.javagamexyz.artemis.components.Velocity;
 
 public class PlayerInputSystem extends EntityProcessingSystem implements InputProcessor {
 	@Mapper protected ComponentMapper<Velocity> vm;
 	@Mapper protected ComponentMapper<Position> pm;
 	
 	protected OrthographicCamera camera;
 	protected Vector3 mouseVector;
 	
 	// TODO use constants
 	protected int ax, ay;
 	protected int thruster = 400;
 	protected float drag = 0.4f;
 	
 	protected boolean shoot = false;
 	
 	protected float timeToFire;
 	protected float fireRate = 0.1f;
 	
 	@SuppressWarnings("unchecked")
 	public PlayerInputSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Velocity.class, Player.class, Position.class));
 		
 		this.camera = camera;
 		mouseVector = new Vector3();
 	}
 	
 	@Override
 	protected void initialize() {
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	@Override
 	protected void process(Entity e) {
 		mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 		camera.unproject(mouseVector);
 		
 		Velocity velocity = vm.get(e);
 		Position position = pm.get(e);
 		
 		velocity.vx += (ax - drag * velocity.vx) * world.delta;
 		velocity.vy += (ay - drag * velocity.vy) * world.delta;
 		
 		if (shoot) {
 			if (timeToFire <= 0) {
 				EntityFactory.createBullet(world, position.x - 27, position.y + 7).addToWorld();
 				EntityFactory.createBullet(world, position.x + 27, position.y + 7).addToWorld();
 				timeToFire = fireRate;
 			}
 		}
 		if (timeToFire > 0) {
 			timeToFire -= world.delta;
			// TODO necessary?
			if (timeToFire < 0) {
 				timeToFire = 0;
			}
 		}
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		switch (keycode) {
 		case Keys.UP:
 			ay = thruster;
 			return true;
 			
 		case Keys.DOWN:
 			ay = -thruster;
 			return true;
 			
 		case Keys.RIGHT:
 			ax = thruster;
 			return true;
 			
 		case Keys.LEFT:
 			ax = -thruster;
 			return true;
 			
 		case Keys.SPACE:
 			shoot = true;
 			return true;
 			
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		switch (keycode) {
 		case Keys.UP:
 			ay = 0;
 			return true;
 			
 		case Keys.DOWN:
 			ay = 0;
 			return true;
 			
 		case Keys.RIGHT:
 			ax = 0;
 			return true;
 			
 		case Keys.LEFT:
 			ax = 0;
 			return true;
 			
 		case Keys.SPACE:
 			shoot = false;
 			return true;
 			
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 }
