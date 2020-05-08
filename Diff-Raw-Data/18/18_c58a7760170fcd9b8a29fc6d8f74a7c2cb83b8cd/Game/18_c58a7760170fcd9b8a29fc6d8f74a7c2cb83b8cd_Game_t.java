 package com.game.src;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import com.game.src.engine.Controller;
 import com.game.src.engine.InputHandler;
 import com.game.src.graphics.Render;
 
 public class Game implements Runnable {
 
 	public static int width, height;
 	public boolean running = false;
 	private Thread thread;
 	private Render render;
 	private Model m;
 	InputHandler input = new InputHandler();
 	Controller controller = new Controller();
 
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
 
 		try {
 			m = Model.getModel("res/teste.obj");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(30f, (float) (640 / 480), 0.3f, 100f);
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
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

		if (controller.location == null) System.out.println("	A");
 		glRotatef(controller.rotation.x, 1f, 0f, 0f);
 		glRotatef(controller.rotation.y, 0f, 1f, 0f);
 		glRotatef(controller.rotation.z, 0f, 0f, 1f);
 		glTranslatef(controller.location.x, controller.location.y, controller.location.y);
 		m.render();
 		glPopMatrix();
 		Display.update();
 	}
 
 }
