 package com.punchline.javalib.entities;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Disposable;
 
 /**
  * Wrapper class of Box2D {@link World}. Manages the world's time step using the method described 
  * <a href="http://gafferongames.com/game-physics/fix-your-timestep/">here</a>.
  * @author Natman64
  *
  */
 public final class PhysicsWorld implements Disposable {
 
 	//region Constants
 	
 	private static final float TIME_STEP = 1.0f / 60.0f;
 	private static final int MAX_STEPS = 5;
 	
 	//endregion
 	
 	//region Fields
 	
 	private World world;
 	private int velocityIterations;
 	private int positionIterations;
 	
 	private float elapsedTime;
 	private float elapsedRatio;
 	
 	//endregion
 	
 	//region Initialization
 	
 	/**
 	 * Creates a PhysicsWorld.
 	 * @param gravity The world's gravity vector.
 	 * @param velocityIterations The world's number of velocity iterations per 
 	 * time step.
 	 * @param positionIterations The world's number of position iterations per 
 	 * time step.
 	 */
 	public PhysicsWorld(Vector2 gravity, int velocityIterations, int positionIterations) {
 		world = new World(gravity, true);
 		world.setAutoClearForces(false);
 		
 		this.velocityIterations = velocityIterations;
 		this.positionIterations = positionIterations;
 	}
 	
 	/**
 	 * Creates a PhysicsWorld.
 	 * @param gravity The world's gravity vector.
 	 */
 	public PhysicsWorld(Vector2 gravity) {
 		this(gravity, 6, 2);
 	}
 	
 	/**
 	 * Creates a PhysicsWorld with normal gravity.
 	 */
 	public PhysicsWorld() {
 		this(new Vector2(0, -9.8f));
 	}
 	
 	//endregion
 	
 	//region Disposal
 	
 	@Override
 	public void dispose() {
 		world.dispose();
 	}
 	
 	//endregion
 	
 	//region Accessors
 	
 	/**
 	 * @return The Box2D {@link World} wrapped in this instance.
 	 */
 	public World getWorld() {
 		return world;
 	}
 	
 	/**
 	 * @return The remainder of the previous delta time that has not yet
 	 * been processed by the physics simulation.
 	 */
 	public float getElapsedRatio() {
 		return elapsedRatio;
 	}
 	
 	//endregion
 	
 	//region Processing
 	
 	/**
 	 * Runs the {@link World}'s time step the appropriate number of times in order
 	 * to accommodate the current frame rate.
 	 * @param deltaSeconds The delta time, in seconds.
 	 */
 	public void process(float deltaSeconds) {
 		
 		elapsedTime += deltaSeconds; //Calculate how many steps are needed
 		int steps = (int) (elapsedTime / TIME_STEP); //to simulate the last game loop
 		
 		if (steps > 0) {
 			elapsedTime -= steps * TIME_STEP; 
 			//elapsedTime now equals the amount of time that hasn't been simulated
 		}
 		
 		elapsedRatio = elapsedTime / TIME_STEP;
 		//elapsedRatio now equals the fraction of a time step that hasn't been simulated
 		
 		steps = Math.min(steps, MAX_STEPS); //Clamp steps to avoid the spiral of doom
 		
 		for (int i = 0; i < steps; i++) {
 			singleStep();
 		}
 		
		//world.step(elapsedTime, velocityIterations, positionIterations);
 		
 		world.clearForces();
 		
 	}
 	
 	//endregion
 	
 	//region Helpers
 	
 	private void singleStep() {
 		world.step(TIME_STEP, velocityIterations, positionIterations);
 	}
 	
 	//endregion
 	
 }
