 package com.game.src;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import static org.lwjgl.opengl.GL11.*;
 
 import com.game.engine.Controller;
 import com.game.engine.InputHandler;
 import com.game.src.graphics.Camera;
 import com.game.src.graphics.Render;
 import com.game.src.graphics.RenderModel;
 
 public class Game implements Runnable {
 
 	public static int width, height;
 	public boolean running = false;
 	private Thread thread;
 	@SuppressWarnings("unused")
 	private Render render;
 	InputHandler input = new InputHandler();
 	Camera cam;
 	Controller controller;
     String filename = "/teste3.obj";
    String defaultTextureMaterial = "/wood.png";
 	RenderModel model;
 
 	public Game() {
 		render = new Render();
 	}
 
 	public static void setSize(int width, int height) {
 		Game.width = width;
 		Game.height = height;
 	}
 
 	private void init() {
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.setTitle("Unnamed Game");
 			Display.create();
 			Mouse.setGrabbed(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		
 		model = new RenderModel(filename, defaultTextureMaterial);
 		
		cam = new Camera(70, (float) Display.getWidth() / (float) Display.getHeight(), 0.3f, 1000);
 		controller = new Controller(cam);
 		glLoadIdentity();
 		// gluPerspective(30f, (float) (640 / 480), 0.3f, 100f);
 	}
 
 	public void start() {
 		running = true;
 		thread = new Thread(this, "Display");
 		thread.start();
 	}
 
 	public void run() {
 		init();
 		long lastTime = System.nanoTime();
 		double ns = 1000000000.0 / 60.0;
 		double delta = 0;
 		long lastTimer = System.currentTimeMillis();
 		int frames = 0;
 		int updates = 0;
 		while (running) {
 			long now = System.nanoTime();
 			delta += (now - lastTime) / ns;
 			lastTime = now;
 			if (delta >= 1) {
 				update();
 				updates++;
 				delta--;
 			}
 			render();
 			frames++;
 			while (System.currentTimeMillis() - lastTimer > 1000) {
 				lastTimer += 1000;
 				System.out.println(updates + " ups, " + frames + " fps.");
 				updates = 0;
 				frames = 0;
 			}
 			check();
 		}
 		Display.destroy();
 	}
 
 	private void check() {
 		if (Display.isCloseRequested()) running = false;
 	}
 
 	private void update() {
 		input.update();
 		controller.moveController();
 	}
 
 	private void render() {
 		//int size = 20;
 		glPushMatrix();
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		//glEnable(GL_LIGHT0);
 		//glLight(GL_LIGHT0, GL_INTENSITY, FloatUtils.asFloatBuffer(new float[]{10f, 10f, 10f, 10.0f}));			
 		//glLight(GL_LIGHT0, GL_INTENSITY, GL_INTENSITY);
 		//render.setColor(0.0f, 1.0f, 0.0f);
 		//render.tile(px, py, size);
 		//render.setColor(1.0f, 0.0f, 0.0f);
 		//render.tile(bx, by, size);
 		cam.useView();
 		model.render();
 		glPopMatrix();
 		Display.update();
 	}
 	
 }
