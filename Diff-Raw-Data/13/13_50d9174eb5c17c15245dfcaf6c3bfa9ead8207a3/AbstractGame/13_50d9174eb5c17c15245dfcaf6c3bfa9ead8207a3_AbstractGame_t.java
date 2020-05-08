 package engine;
 
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_PROJECTION;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glClearColor;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glOrtho;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 
 import util.Graphics;
 import util.Timer;
 
 public abstract class AbstractGame {
 	/** screen parameters */
 	private int screenWidth;
 	private int screenHeight;
 	private boolean fullscreen;
 
 	/** time at last frame */
 	private long lastFrame;
 
 	/** frames per second */
 	private int fps;
 
 	/** last fps time */
 	private long lastFPS;
 
 	/** window title */
 	private String title;
 
 	public AbstractGame(String title, int screenWidth, int screenHeight,
 			boolean fullscreen) {
 		Display.setTitle(title);
 
 		this.title = title;
 		this.screenWidth = screenWidth;
 		this.screenHeight = screenHeight;
 		this.fullscreen = fullscreen;
 	}
 
 	/**
 	 * Main entry point of the game.
 	 */
 	public void start() {
 		try {
 			Graphics.setDisplayMode(screenWidth, screenHeight, fullscreen);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		initGL();
 
 		init();
 
 		lastFPS = Timer.getTime();
 
 		while (!Display.isCloseRequested()) {
			int delta = getDelta();
			if(delta > 0) {
				update(delta);
			}
 			updateFPS();
 			Display.update();
 			Display.sync(60);
 		}
 
 		shutdown();
 
 		Display.destroy();
 	}
 
 	protected abstract void init();
 
 	protected abstract void update(int delta);
 
 	protected abstract void shutdown();
 
 	/**
 	 * Initialize openGL.
 	 */
 	private void initGL() {
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, screenWidth, 0, screenHeight, 1, -1);
 		glMatrixMode(GL_MODELVIEW);
 		
 		glClearColor(0.1f, 0.2f, 0.5f, 1.0f);
 		glEnable(GL_TEXTURE_2D);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 
 	/**
 	 * Calculate the FPS and display it in the title bar.
 	 */
 	private void updateFPS() {
 		if (Timer.getTime() - lastFPS > 1000) {
 			Display.setTitle(title + "\tFPS: " + fps);
 			fps = 0;
 			lastFPS += 1000;
 		}
 		fps++;
 	}
 
 	/**
 	 * Calculate how many milliseconds have passed since last frame.
 	 * 
 	 * @return milliseconds passed since last frame
 	 */
 	public int getDelta() {
 		long time = Timer.getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 
 		return delta;
 	}
 
 	/**
 	 * Accessor for the screen width.
 	 * @return screen width
 	 */
 	public int getScreenWidth() {
 		return screenWidth;
 	}
 
 	/**
 	 * Accessor for the screen height.
 	 * @return screen height
 	 */
 	public int getScreenHeight() {
 		return screenHeight;
 	}
 }
