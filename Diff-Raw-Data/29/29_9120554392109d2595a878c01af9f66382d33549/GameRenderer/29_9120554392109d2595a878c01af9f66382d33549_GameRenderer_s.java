 /* 
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.galactogolf.views;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.galactogolf.Game;
 import com.galactogolf.UserPreferences;
 import com.galactogolf.genericobjectmodel.GameWorld;
 import com.galactogolf.genericobjectmodel.NonPlayerEntity;
 import com.galactogolf.genericobjectmodel.Vector2D;
 import com.galactogolf.specificobjectmodel.ArrowSprite;
 import com.galactogolf.specificobjectmodel.BarrierEntity;
 import com.galactogolf.specificobjectmodel.BounceBarrierEntity;
 import com.galactogolf.specificobjectmodel.Debris1Entity;
 import com.galactogolf.specificobjectmodel.Debris2Entity;
 import com.galactogolf.specificobjectmodel.Debris3Entity;
 import com.galactogolf.specificobjectmodel.DebrisEntity;
 import com.galactogolf.specificobjectmodel.GalactoGolfPlayerEntity;
 import com.galactogolf.specificobjectmodel.GalactoGolfWorld;
 import com.galactogolf.specificobjectmodel.GameConstants;
 import com.galactogolf.specificobjectmodel.HazeSprite;
 import com.galactogolf.specificobjectmodel.InfoBarSprite;
 import com.galactogolf.specificobjectmodel.MoonEntity;
 import com.galactogolf.specificobjectmodel.PlanetEntity;
 import com.galactogolf.specificobjectmodel.PowerHandleSprite;
 import com.galactogolf.specificobjectmodel.ResetButtonSprite;
 import com.galactogolf.specificobjectmodel.SaturnEntity;
 import com.galactogolf.specificobjectmodel.BonusStarEntity;
 import com.galactogolf.specificobjectmodel.StarSprite;
 import com.galactogolf.specificobjectmodel.StarSprite2;
 import com.galactogolf.specificobjectmodel.SunEntity;
 import com.galactogolf.specificobjectmodel.WormholeEntity;
 import com.galactogolf.views.textprinter.TextPrinter;
 
 import android.content.Context;
 import android.opengl.GLU;
 import android.opengl.GLSurfaceView.Renderer;
 import android.util.Log;
 
 /***
  * Renderer called by view thread.
  * 
  */
 public class GameRenderer implements Renderer {
 
 	private GameWorld _world;
 	protected Context _context;
 	private TextPrinter _textRenderer;
 	private RenderInstructionBuffer _openBuffer;
 	private RenderInstructionBuffer _closedBuffer;
 
 	int _screenWidth;
 	int _screenHeight;
 	float _fps = 0.0f;
 	protected HashMap<String, GameEntityRenderable> _renderables = new HashMap<String, GameEntityRenderable>();
 	private Object _drawLock = new Object();
 	private static final int INSTRUCTION_BUFFER_SIZE = 1000;
 
 	public RenderInstructionBuffer GetOpenRenderInstructionBuffer() {
 		return _openBuffer;
 	}
 
 	public synchronized void SwapRenderInstructionPool() {
 		RenderInstructionBuffer temp = _openBuffer;
 		_openBuffer = _closedBuffer;
 		_closedBuffer = temp;
 		try {
 			_openBuffer.open();
 			_closedBuffer.close();
 		} catch (RenderException e) {
 			Log.e("Exception", e.getMessage());
 		}
 
 		synchronized (_drawLock) {
 			_drawQueueChanged = true;
 			_drawLock.notify();
 		}
 	}
 
 	public GameRenderer(Context context, GameWorld world) {
 		_showFPS = UserPreferences.ShowFramesPerSecond();
 	}
 
 	public GameRenderer(Context context, Game game, int gameWidth,
 			int gameHeight) {
 		_context = context;
 		_world = game.getWorld();
 		_world.SetRenderer(this);
 		_renderables.put(MoonEntity.class.getName(), new GameEntityRenderable(
 				GameConstants.FRAMES_MOON));
 		_renderables.put(GalactoGolfPlayerEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_ROCKET));
 		_renderables.put(PlanetEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_PLANET));
 		_renderables.put(SunEntity.class.getName(), new GameEntityRenderable(
 				GameConstants.FRAMES_SUN));
 		_renderables.put(SaturnEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_SATURN));
 		_renderables.put(BonusStarEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_BONUS));
 		_renderables.put(WormholeEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAME_WORMHOLE));
 		_renderables.put(BounceBarrierEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_BOUNCE_BARRIER));
 		_renderables.put(BarrierEntity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_BARRIER));
 		_renderables.put(StarSprite.class.getName(), new GameEntityRenderable(
 				GameConstants.FRAMES_SMALL_STAR));
 		_renderables.put(StarSprite2.class.getName(), new GameEntityRenderable(
 				GameConstants.FRAMES_SMALL_STAR2));
 		_renderables.put(HazeSprite.class.getName(), new GameEntityRenderable(
 				GameConstants.HAZE_SPRITE));
 		_renderables.put(ArrowSprite.class.getName(), new GameEntityRenderable(
 				GameConstants.FRAMES_ARROW));
 		_renderables.put(PowerHandleSprite.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_POWER_HANDLE));
 		_renderables.put(ResetButtonSprite.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_RESET_BUTTON));
 		_renderables.put(InfoBarSprite.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_INFOBAR));
 		_renderables.put(Debris1Entity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_DEBRIS1));
 		_renderables.put(Debris2Entity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_DEBRIS2));
 		_renderables.put(Debris3Entity.class.getName(),
 				new GameEntityRenderable(GameConstants.FRAMES_DEBRIS3));
 		lastTimeMillis = System.currentTimeMillis();
 		_openBuffer = new RenderInstructionBuffer(INSTRUCTION_BUFFER_SIZE, true);
 		_closedBuffer = new RenderInstructionBuffer(INSTRUCTION_BUFFER_SIZE,
 				false);
 		
 		_showFPS = UserPreferences.ShowFramesPerSecond();
 
 	}
 
 	private long lastTimeMillis;
 
 	public HashMap<String, GameEntityRenderable> GetRenderables() {
 		return _renderables;
 	}
 
 	private float _currentScaleFactor = 1.0f;
 	private boolean _drawQueueChanged;
 	private GL10 _gl; // current gl context, set at the start of
 						// on{Draw,Changed,Created} etc.
 	private boolean _isSetupComplete = false;
 	private int prevScore = -999;
 	private int prevLevel = -999;
 	private int prevPar = -999;
 	private float prevFPS = -999.0f;
 
 	private StringBuilder scoreMessage = new StringBuilder("Strokes:");
 	private int scoreMessageLength = scoreMessage.length();
 	private StringBuilder parMessage = new StringBuilder("Par:");
 	private int parMessageLength = parMessage.length();
 	private StringBuilder levelMessage = new StringBuilder("Hole:");
 	private int levelMessageLength = levelMessage.length();
 	private StringBuilder fpsMessage = new StringBuilder("FPS:");
 	private int fpsMessageLength = fpsMessage.length();
 	private boolean _showFPS;
 
 	public void onDrawFrame(GL10 gl) {
 		_gl = gl;
 		long timeNow = System.currentTimeMillis();
 		long timeSinceLastFrame = timeNow - lastTimeMillis;
 		lastTimeMillis = timeNow;
 
 		synchronized (_drawLock) {
 			if (!_drawQueueChanged) {
 				while (!_drawQueueChanged) {
 					try {
 						_drawLock.wait();
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 			_drawQueueChanged = false;
 		}
 
 		synchronized (this) {
 
 			gl.glEnable(GL10.GL_BLEND);
 			gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
 
 			gl.glClearColor(0.08f, 0.11f, 0.30f, 1.0f);
 			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | // OpenGL docs.
 					GL10.GL_DEPTH_BUFFER_BIT);
 			gl.glLoadIdentity();
 			gl.glEnable(GL10.GL_TEXTURE_2D);
 			gl.glDisable(GL10.GL_DEPTH_TEST);
 			
 			
 		
 			if (((GalactoGolfWorld) _world).runningPhysics()) { // how far the
 				// player is away from the cam origin
 				float playerDistFromCamOrigin = Vector2D.Sub(
 						_world.GetPlayer().getPosition(),
 						new Vector2D(_world.getCameraLocation().x, _world
 								.getCameraLocation().y)).magnitude(); // how
 																		// much
 																		// we
 																		// would
 																		// have
 																		// to
 																		// scale
 																		// to
 																		// easily
 																		// fit
 																		// the
 																		// player
 																		// in
 				float autoScaleFactor = (GetScreenHeight() / 2)
 						/ (playerDistFromCamOrigin + 50);
 
 				if (autoScaleFactor < _world.getCameraLocation().z) {
 					_currentScaleFactor += (autoScaleFactor - _currentScaleFactor) * 0.2;
 				} else {
 					_currentScaleFactor = _world.getCameraLocation().z;
 				}
 			} else {
 				_currentScaleFactor = _world.getCameraLocation().z;
 			}
 			
 			if (_currentScaleFactor < 0.5) {
 					_currentScaleFactor = 0.5f;
 				
 
 			}
 			
 			// check to see if the spaceship is not visible even if we have zoomed
 			if(_world.GetPlayer().getPosition().x<_world.getCameraLocation().x-_world.GetScreenWidth()/_currentScaleFactor*0.8/2){
 				_world.getCameraLocation().x=(float) (_world.GetPlayer().getPosition().x+_world.GetScreenWidth()/_currentScaleFactor*0.8/2);
 			}
 			// check to see if the spaceship is not visible even if we have zoomed
 			if(_world.GetPlayer().getPosition().x>_world.getCameraLocation().x+_world.GetScreenWidth()/_currentScaleFactor*0.8/2){
 				_world.getCameraLocation().x=(float) (_world.GetPlayer().getPosition().x-_world.GetScreenWidth()/_currentScaleFactor*0.8/2);
 			}
 			// check to see if the spaceship is not visible even if we have zoomed
 			if(_world.GetPlayer().getPosition().y<_world.getCameraLocation().y-_world.GetScreenHeight()/_currentScaleFactor*0.8/2){
 				_world.getCameraLocation().y=(float) (_world.GetPlayer().getPosition().y+_world.GetScreenHeight()/_currentScaleFactor*0.8/2);
 			}
 			// check to see if the spaceship is not visible even if we have zoomed
			if(_world.GetPlayer().getPosition().y>_world.getCameraLocation().y+_world.GetScreenHeight()/_currentScaleFactor*0.8/2){
				_world.getCameraLocation().y=(float) (_world.GetPlayer().getPosition().y-_world.GetScreenHeight()/_currentScaleFactor*0.8/2);
 			}
 
 
 		
 			gl.glScalef(_currentScaleFactor, _currentScaleFactor,
 					_currentScaleFactor);
 			gl.glTranslatef(
 					-(_world.getCameraLocation().x - (GetScreenWidth() / 2)
 							/ _currentScaleFactor),
 					-(_world.getCameraLocation().y - (GetScreenHeight() / 2)
 							/ _currentScaleFactor), 0.0f);
 
 			Iterator<RenderInstruction> iter = _closedBuffer.iterator();
 			while (iter.hasNext()) {
 				RenderInstruction instruction = iter.next();
 				gl.glPushMatrix();
 				instruction.render(this);
 				gl.glPopMatrix();
 			}
 
 			if (prevScore != ((GalactoGolfWorld) _world).GetScore()) {
 				scoreMessage.delete(scoreMessageLength, scoreMessage.length());
 				scoreMessage.append(((GalactoGolfWorld) _world).GetScore());
 				prevScore = ((GalactoGolfWorld) _world).GetScore();
 			}
 			if (prevPar != ((GalactoGolfWorld) _world).getPar()) {
 				parMessage.delete(parMessageLength, parMessage.length());
 				parMessage.append(((GalactoGolfWorld) _world).getPar());
 				prevPar = ((GalactoGolfWorld) _world).getPar();
 			}
 			if (prevLevel != ((GalactoGolfWorld) _world)
 					.getCurrentLevelNumber()) {
 				levelMessage.delete(levelMessageLength, levelMessage.length());
 				levelMessage.append(((GalactoGolfWorld) _world)
 						.getCurrentLevelNumber());
 				prevLevel = ((GalactoGolfWorld) _world).getCurrentLevelNumber();
 			}
 
 			_textRenderer.drawTextInViewSpace(gl, 0, _screenHeight-40, levelMessage);
 			_textRenderer.drawTextInViewSpace(gl, 105, _screenHeight-40, parMessage);
 			_textRenderer.drawTextInViewSpace(gl, 195, _screenHeight-40, scoreMessage);
 			if (_showFPS) {
 				// calc moving average of FPS
 				_fps = Math
 						.round((_fps * 9 + 1000.0f / timeSinceLastFrame) / 10);
 				if (prevFPS != _fps) {
 					fpsMessage.delete(fpsMessageLength, fpsMessage.length());
 					fpsMessage.append(_fps);
 					prevFPS = _fps;
 				}
 
 				_textRenderer.drawTextInViewSpace(gl, 0,
 						0, fpsMessage);
 			}
 		}
 
 	}
 
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		_gl = gl;
 		_screenWidth = width;
 		_screenHeight = height;
 		gl.glViewport(0, 0, width, height);
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		gl.glLoadIdentity();
 		GLU.gluOrtho2D(gl, 0, width, height, 0);
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 		gl.glLoadIdentity();
 
 		_world.SetScreenWidth(width);
 		_world.SetScreenHeight(height);
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		_gl = gl;
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
 				GL10.GL_LINEAR);
 		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
 				GL10.GL_LINEAR);
 
 		this.loadRenderables(gl);
 		_isSetupComplete = true;
 		_textRenderer = new TextPrinter(gl, _context);
 	}
 
 	private void loadRenderables(GL10 gl) {
 		Iterator<GameEntityRenderable> iter = _renderables.values().iterator();
 		while (iter.hasNext()) {
 			iter.next().initGL(gl, _context);
 		}
 
 	}
 
 	public GameEntityRenderable GetRenderable(
 			Class<? extends NonPlayerEntity> renderableClass) {
 		return _renderables.get(renderableClass.getName());
 	}
 
 	public float GetScreenWidth() {
 		return _screenWidth;
 	}
 
 	public float GetScreenHeight() {
 		return _screenHeight;
 	}
 
 	public synchronized void waitTillDrawingComplete() {
 
 	}
 
 	public GL10 GetGL() {
 		return _gl;
 	}
 
 	public TextPrinter GetTextRenderer() {
 		return _textRenderer;
 	}
 
 	public boolean isSetupComplete() {
 		return _isSetupComplete;
 	}
 }
