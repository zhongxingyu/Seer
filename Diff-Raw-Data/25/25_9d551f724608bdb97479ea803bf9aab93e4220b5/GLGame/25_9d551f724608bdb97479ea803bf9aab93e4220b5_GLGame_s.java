 package org.racenet.framework;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import org.racenet.framework.interfaces.Audio;
 import org.racenet.framework.interfaces.FileIO;
 import org.racenet.framework.interfaces.Game;
 import org.racenet.framework.interfaces.Graphics;
 import org.racenet.framework.interfaces.Input;
 import org.racenet.framework.interfaces.Screen;
 
 import android.app.Activity;
 import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLSurfaceView.Renderer;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.Window;
 import android.view.WindowManager;
 
 public abstract class GLGame extends Activity implements Game, Renderer {
 
 	enum GLGameState {
 		
 		Initialized,
 		Running,
 		Paused,
 		Finished,
 		Idle
 	}
 	
 	protected GLSurfaceView glView;
 	private GLGraphics glGraphics;
 	private Audio audio;
 	private Input input;
 	private FileIO fileIO;
 	private Screen screen;
 	private GLGameState state = GLGameState.Initialized;
 	private Object stateChanged = new Object();
 	private long startTime = System.nanoTime();
 	private WakeLock wakeLock;
 	private int screenWidth;
 	private int screenHeight;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
     	screenHeight = getWindowManager().getDefaultDisplay().getHeight();
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		glView = new GLSurfaceView(this);
 		glView.setRenderer(this);
 		setContentView(glView);
 		
 		glGraphics = new GLGraphics(glView);
 		audio = new AndroidAudio(this);
 		input = new AndroidInput(this, glView, 1, 1);
 		fileIO = new AndroidFileIO(getAssets());
 		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
 		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
 	}
 	
 	public void onPause() {
 		
 		synchronized (stateChanged) {
 			
 			if (isFinishing()) {
 				
 				state = GLGameState.Finished;
 			
 			} else {
 				
 				state = GLGameState.Paused;
 			}
 			
 			while (true) {
 				
 				try {
 					
 					stateChanged.wait();
 					break;
 					
 				} catch (InterruptedException e) {
 					
 				}
 			}
 		}
 		
 		wakeLock.release();
 		glView.onPause();
 		super.onPause();
 	}
 	
 	public void onResume() {
 		
 		super.onResume();
 		glView.onResume();
 		wakeLock.acquire();
 	}
 	
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		
 		glGraphics.setGL(gl);
 		
 		synchronized (stateChanged) {
 			
 			if (state == GLGameState.Initialized) {
 				
 				this.setScreen(this.getStartScreen());
 			}
 			
 			state = GLGameState.Running;
 			startTime = System.nanoTime();
 		}
 	}
 	
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		
 	}
 	
 	public void onDrawFrame(GL10  gl) {
 		
 		synchronized (stateChanged) {
 		
 			state = this.state;
 		}
 		
 		if (state == GLGameState.Running) {
 			
 			long nanoTime = System.nanoTime();
 			float deltaTime = (nanoTime - startTime) / 1000000000.0f;
 			startTime = nanoTime;
 			
 			screen.update(deltaTime);
 			screen.present(deltaTime);
 		}
 		
 		if (state == GLGameState.Paused) {
 			
 			screen.pause();
 			synchronized (stateChanged) {
 			
 				this.state = GLGameState.Idle;
 				stateChanged.notifyAll();
 			}
 		}
 		
 		if (state == GLGameState.Finished) {
 			
 			screen.pause();
 			screen.dispose();
 			synchronized (stateChanged) {
 				
 				this.state = GLGameState.Idle;
 				stateChanged.notifyAll();
 			}
 		}
 	}
 	
 	public Input getInput() {
 		
 		return input;
 	}
 	
 	public FileIO getFileIO() {
 		
 		return fileIO;
 	}
 	
 	public Graphics getGraphics() {
 		
 		throw new RuntimeException("use getGLGraphics()");
 	}
 	
 	public GLGraphics getGLGraphics() {
 		
 		return glGraphics;
 	}
 	
 	public Audio getAudio() {
 		
 		return audio;
 	}
 	
 	public void setScreen(Screen screen) {
 		
 		if (screen == null) {
 			
 			throw new IllegalArgumentException("Screen must not be null");
 		}
 		
 		if (this.screen != null) {
 		
 			this.screen.pause();
 			this.screen.dispose();
 		}
 		
 		screen.resume();
 		screen.update(0);
 		this.screen = screen;
 	}
 	
 	public Screen getCurrentScreen() {
 		
 		return screen;
 	}
 	
     public int getScreenWidth() {
     	
     	return screenWidth;
     }
     
     public int getScreenHeight() {
     	
     	return screenHeight;
     }
 }
