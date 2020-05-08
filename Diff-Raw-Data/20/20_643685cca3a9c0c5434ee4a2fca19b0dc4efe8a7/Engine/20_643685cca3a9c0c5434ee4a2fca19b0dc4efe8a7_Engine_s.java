 /*
  * Copyright (c) 2011 James Johnson
  * Permission is hereby granted, free of charge, to any person obtaining a copy 
  * of this software and associated documentation files (the "Software"), to 
  * deal in the Software without restriction, including without limitation the 
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
  * sell copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
  * IN THE SOFTWARE.
  */
 
 package org.robobrain.sdk.game;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.robobrain.sdk.GLRenderer;
 import org.robobrain.sdk.audio.MusicManager;
 import org.robobrain.sdk.audio.SoundManager;
 import org.robobrain.sdk.graphics.Renderable;
 import org.robobrain.sdk.graphics.TextureManager;
 import org.robobrain.sdk.input.Multitouch;
 
 import android.util.Log;
 
 /**
  * The base Engine class encapsulates a single game. It's basically the main loop.
  * Derive your game class from this.
  * @author James Johnson
  *
  */
 public class Engine {
 	/**
 	 * The World manages all the game Entities.
 	 */
 	protected World mWorld;
 	
 	private boolean mInitialized;
 	private boolean mPaused;
 	
 	/**
 	 * Initializes the Engine.
 	 */
 	public Engine() {
 		mWorld = null;
 		mInitialized = false;
 		mPaused = false;
 	}
 	
 	/**
 	 * Initializes the Engine. Do all of your asset loading and setup here.
 	 */
 	public void init() {
 		mInitialized = true;
 	}
 	
 	/**
 	 * Updates the engine one step. Called at least once per frame. This also updates
 	 * the World. So make sure you call super.update() if you override this method.
 	 * Do anything here that needs to be calculated on a per frame basis. AI, 
 	 * rendering, sound, etc. 
 	 * @param time
 	 * The amount of time in milliseconds since this function was last called.
 	 */
 	public void update(long time) {
 		if (mPaused) {
 			return;
 		}
 		if (mWorld == null) {
 			return;
 		}
 		mWorld.update(time);
		Multitouch.clear();
 	}
 	
 	/**
 	 * Draws all sprites.
 	 * @param gl   A valid OpenGL ES 1.0 object.
 	 */
 	public void render(GL10 gl) {
 		if (mPaused) {
 			return;
 		}
 		if (mWorld == null) {
 			return;
 		}
 		mWorld.render(gl);
 	}
 	
 	/**
 	 * Pauses the game.
 	 */
 	public void pause() {
 		mPaused = true;
 	}
 	
 	/**
 	 * Resumes playing the game.
 	 */
 	public void play() {
 		mPaused = false;
 	}
 	
 	/**
 	 * Called when OpenGL is being reset. Forces World to refresh sprite textures.
 	 * Call this function after OpenGL has been reset by GLView and all resources 
 	 * need to be rebuilt.
 	 */
 	public void glReset() {
 	    if (mWorld != null) {
 	        mWorld.onGlReset();
 	    }
 	}
 	
 	/**
 	 * Shuts down the engine and releases any resources it is using. You should
 	 * release resources you are using here. Called when the game Activity is
 	 * being closed. 
 	 */
 	public void shutdown() {
 		SoundManager.release();
 		MusicManager.release();
 		TextureManager.unloadAll();
 	}
 	
 	/**
 	 * Returns an array of Entities that need to be displayed on screen.
 	 * @return
 	 * An array of Entities.
 	 */
 	public Entity[] getDisplayList() {
 		if (mWorld == null) {
 			return null;
 		}
 		return mWorld.entitiesToArray();
 	}
 	
 	/**
 	 * Registers the World with the game Engine.
 	 * @param world
 	 */
 	public void registerWorld(World world) {
 		if (world == null) {
 			Log.e("Game Engine", "Null World passed to registerWorld().");
 			return;
 		}
 		mWorld = world;
 	}
 	
 	/**
 	 * Returns true if the Engine has been initialized and is ready to play.
 	 *
 	 * @return
 	 */
 	public boolean getInitialized() { return mInitialized; }
 	
 	/**
 	 * Gets the game's frame rate.
 	 * @return
 	 * The game's frame rate, measured in Frames Per Second.
 	 */
 	public float getFPS() { return GLRenderer.getFPS(); }
 }
