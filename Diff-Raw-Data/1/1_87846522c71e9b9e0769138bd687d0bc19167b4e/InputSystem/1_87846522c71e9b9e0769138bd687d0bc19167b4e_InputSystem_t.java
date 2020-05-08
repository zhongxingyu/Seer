 package com.punchline.javalib.entities.systems;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.punchline.javalib.entities.Entity;
 
 /**
  * Base class for any {@link EntitySystem} that can take user input.
  * @author Natman64
  * @created Jul 24, 2013
  */
 public abstract class InputSystem extends EntitySystem implements InputProcessor {
 
 	//region Fields
 	
 	/**
 	 * The game's {@link InputMultiplexer}.
 	 */
 	protected InputMultiplexer input;
 	
 	/**
 	 * Whether this InputSystem processes accelerometer input. By default, this is automatically enabled if an accelerometer is available.
 	 * Performance will be improved if subclasses set this to false.
 	 */
 	protected boolean tiltEnabled;
 	
 	/**
 	 * The accelerometer's x value, if it surpasses {@link #tiltThresholdX}.
 	 */
 	protected float tiltX;
 	
 	/**
 	 * The accelerometer's y value, if it surpasses {@link #tiltThresholdY}.
 	 */
 	protected float tiltY;
 	
 	/**
 	 * The accelerometer's z value, if it surpasses {@link #tiltThresholdZ}.
 	 */
 	protected float tiltZ;
 	
 	//TODO tweak for best thresholds.
 	
 	/**
 	 * The minimum accelerometer x value that will trigger a tilt event.
 	 */
 	protected float tiltThresholdX = 1.5f;
 	
 	/**
 	 * The minimum accelerometer y value that will trigger a tilt event.
 	 */
 	protected float tiltThresholdY = 3f;
 	
 	/**
 	 * The minimum accelerometer z value that will trigger a tilt event.
 	 */
 	protected float tiltThresholdZ = 3f;
 	
 	//endregion
 	
 	//region Initialization/Disposal
 	
 	/**
 	 * Constructs an InputSystem.
 	 * @param input The game's {@link InputMultiplexer}.
 	 */
 	public InputSystem(InputMultiplexer input) {
 		this.input = input;
 		
 		tiltEnabled = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer);
 	}
 	
 	/**
 	 * Tells the game's {@link InputMultiplexer} to stop processing this
 	 * system's input.
 	 */
 	@Override
 	public void dispose() {
 		input.removeProcessor(this);
 	}
 	
 	//endregion
 	
 	//region Processing
 	
 	@Override
 	protected void process(Entity e) { }
 
 	@Override
 	public boolean canProcess(Entity e) {
 		return false; //By default, InputSystems don't process entities.
 	}
 	
 	@Override
 	public void processEntities() {
 		
 		//Process Accelerometer
 		if (tiltEnabled) {
 			
 			//Get accelerometer info
 			float x = Gdx.input.getAccelerometerX();
 			float y = Gdx.input.getAccelerometerY();
 			float z =  Gdx.input.getAccelerometerZ();
 			
 			//Account for input thresholds
 			if (Math.abs(x) < tiltThresholdX) x = 0;
 			if (Math.abs(y) < tiltThresholdY) y = 0;
 			if (Math.abs(z) < tiltThresholdZ) z = 0;
 			
 			//Trigger tilt events
 			if (x != tiltX) {
 				tiltX = x;
 				onTiltX(x);
 			}
 			
 			if (y != tiltY) {
 				tiltY = y;
 				onTiltY(y);
 			}
 			
 			if (z != tiltZ) {
 				tiltZ = z;
 				onTiltZ(z);
 			}
 			
 		}
 		
 		super.processEntities();
 		
 	}
 	
 	//endregion
 	
 	//endregion
 	
 	//region Events
 
 	@Override
 	public void pause() {
 		input.removeProcessor(this);
 		
 		//Stop tilt events
 		tiltX = 0f;
 		tiltY = 0f;
 		tiltZ = 0f;
 	}
 
 	@Override
 	public void resume() {
 		input.addProcessor(this);
 	}
 	
 	//endregion
 	
 	//region Key Events
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	//endregion
 	
 	//region Touch Events
 	
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
 
 	//endregion
 	
 	//region Mouse Events
 	
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 	
 	//endregion
 	
 	//region Tilt Events
 	
 	/**
 	 * Event called when the device's accelerometer x value changes.
 	 * @param x The new accelerometer x value, or 0 if the real value falls below {@link #tiltThresholdX}.
 	 */
 	protected void onTiltX(float x) { }
 	
 	/**
 	 * Event called when the device's accelerometer y value changes.
 	 * @param x The new accelerometer y value, or 0 if the real value falls below {@link #tiltThresholdY}.
 	 */
 	protected void onTiltY(float y) { }
 	
 	/**
 	 * Event called when the device's accelerometer z value changes.
 	 * @param x The new accelerometer z value, or 0 if the real value falls below {@link #tiltThresholdZ}.
 	 */
 	protected void onTiltZ(float z) { }
 	
 	//endregion
 	
 }
