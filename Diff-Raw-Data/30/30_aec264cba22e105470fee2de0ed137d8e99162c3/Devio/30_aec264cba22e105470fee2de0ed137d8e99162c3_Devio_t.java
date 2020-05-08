 package com.github.desmaster.Devio;
 
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_PROJECTION;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glOrtho;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.gfx.Screen;
 import com.github.desmaster.Devio.util.*;
 
 
 public class Devio {
 
 	private DisplayMode dm;
 
 	public static int WIDTH;
 	public static int HEIGHT;
 	public static final String TITLE = "Devio";
 	private boolean fullscreen = false;
 	
 	int fps;
 	long lastFrame;
 	long lastFPS;
 
 	private boolean running = false;
 
 	private Screen screen;
 	private Counter counter;
 	private InputHandler input = new InputHandler(this);
 	private iFile file;
 	public static Position spawnPosition;
 	//private Player player;
 	
 
 	public Devio() {
 		file = new iFile();
 		file.loadConfig();
 		dm = new DisplayMode(WIDTH, HEIGHT);
 	}
 
 	private void init() {
 		initScreen();
 		launch();
 		initObjects();
 		initGL();
 	}
 
 	private void initScreen() {
 		try {
 			Display.setTitle(TITLE);
 			Display.setDisplayMode(dm);
 			Display.setFullscreen(fullscreen);
 			Display.setVSyncEnabled(true);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void launch() {
 
 	}
 
 	private void initObjects() {
		spawnPosition = new Position(0,0);
		//spawnPosition = new Position((Display.getWidth() / 2) - 16, (Display.getHeight() / 2) - 16);
 		screen = new Screen(input);
 		counter = new Counter();
 		counter.start();
 	}
 
 	private void initGL() {
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_TEXTURE_2D); // Enable 2D Texture Rendering
 		glEnable(GL_BLEND); // Enable GL Blending
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // You need to enable blending to allow transparent bits on an image when using OpenGL.
 	}
 
 	public void run() {
 		while (running) {
 			int delta = getDelta();
 			Display.update();
 			if (Display.isCloseRequested()) {
 				stop();
 				
 			} else {
 				tick(delta);
 				scanInput();
 				render(); 
 			}
 		}
 	}
 
 	public void start() {
 		init();
 		running = true;
 		run();
 	}
 
 	public void stop() {
 		running = false;
 		Console.log("Stopped the game loop");
 		file.saveConfig();
 		Display.destroy();
 		Console.log("Destroyed the screen");
 		System.exit(0);
 	}
 	
 	public long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 
 	public int getDelta() {
 		long time = getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 		return delta;
 	}
 	
 	public void tick(int delta) {
 		input.tick();
 		Screen.getPlayer().tick(delta);
 		screen.tick();
 		counter.tick();
 	}
 	
 	public void scanInput() {
 		if (input.exit.clicked) stop();
 	}
 
 	public void render() {
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 		screen.render();
 		Display.sync(60);
 	}
 
 	public static void main(String[] args) {
 		Devio game = new Devio();
 		game.start();
 	}
 
 }
