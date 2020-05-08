 package src;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import engine.input.InputHandler;
 import engine.render.RenderEngine;
 import engine.render.Screen;
 import engine.texture.TextureEngine;
 
 public class RenderDisplay {
 	
 	public int syncFPS = -100;
 	
 	private long lastFPS;
 	private int cFPS = 0;
 	private int FPS = 0;
 	private Screen screen;
 	private boolean exit = false;
 	private long lastFrameMilliseconds;
 	
 	private static RenderDisplay instance;
 	
 	public RenderDisplay() {
 		instance = this;
 	}
 	
 	public static RenderDisplay instance() {
 		return instance;
 	}
 	
 	public void start() throws Exception {
 		init();
 		gameLoop();
 		deinit();
 	}
 	
 	private void init() throws LWJGLException {
 		createDisplay("PhysicsEngine2D");
 		RenderEngine.initOpenGL();
 	}
 	
 	private void deinit() {
 		TextureEngine.unloadAllTextures();
 		Display.destroy();
 	}
 	
 	private void createDisplay(String title) throws LWJGLException {
 		Display.setFullscreen(false);
 		//Display.setDisplayMode(Display.getDesktopDisplayMode());
 		Display.setDisplayMode(new DisplayMode(800, 600));
 		Display.setTitle(title);
 		Display.create();
 	}
 	
 	private void gameLoop() {
 		setScreen(new StartScreen());
 		lastFrameMilliseconds = getMilliseconds();
 		lastFPS = lastFrameMilliseconds;
 		while(!Display.isCloseRequested() && !exit) {
 			if(syncFPS > 0) {
 				Display.sync(syncFPS);
 			}
 			RenderEngine.reset();
 			
 			InputHandler.refreshListeners();
 			if(screen != null) {
 				int delta = getDeltaTime();
 				screen.doLogic(delta);
 				RenderEngine.enableDephTest();
 				RenderEngine.setPerspectiveMatrixMode();
 				screen.predraw3D(delta);
 				screen.drawControls3D();
 				screen.draw3D(delta);
 				RenderEngine.loadIdentity();
 				RenderEngine.disableDephTest();
 				RenderEngine.setOrthogonalMatrixMode();
 				screen.predraw2D(delta);
 				screen.drawControls2D();
 				screen.draw2D(delta);
 			}
 			
 			refreshFPS();
 			
 			Display.update();
 		}
 		deinitCurrentScreen();
 	}
 	
 	private void deinitCurrentScreen() {
 		if(this.screen != null) {
 			screen.deinitControls();
 			this.screen.deinit();
 			InputHandler.removeKeyboardListener(screen);
 			InputHandler.removeMouseListener(screen);
 		}
 	}
 	
 	private void refreshFPS() {
 		if(getMilliseconds() - lastFPS > 1000) {
 			FPS = cFPS;
 			cFPS = 0;
 			lastFPS = getMilliseconds();
 		}
 		cFPS++;
 	}
 	
 	public void setScreen(Screen screen) {
 		deinitCurrentScreen();
 		if(screen == null) {
 			exitGame();
 		} else {
 			InputHandler.addKeyboardListener(screen);
 			InputHandler.addMouseListener(screen);
 			this.screen = screen;
 			screen.init();
 			screen.initControls();
 		}
 	}
 	
 	private void exitGame() {
 		this.exit = true;
 		this.screen = null;
 	}
 	
 	public Screen getScreen() {
 		return screen;
 	}
 	
 	public long getMilliseconds() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 	
 	private int getDeltaTime() {
 		long time = getMilliseconds();
 		int delta = 0;
 		if(time < lastFrameMilliseconds) {
 			delta = (int) (Long.MAX_VALUE-lastFrameMilliseconds+time);
 		} else {
 			delta = (int) (time - lastFrameMilliseconds);
 		}
 		lastFrameMilliseconds = time;
 		
 		return delta;
 	}
 	
 	public int getFPS() {
 		return FPS;
 	}
 }
