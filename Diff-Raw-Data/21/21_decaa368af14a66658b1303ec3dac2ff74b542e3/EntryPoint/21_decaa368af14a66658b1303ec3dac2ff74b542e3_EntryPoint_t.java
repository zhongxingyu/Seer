 package main;
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 public class EntryPoint {
 
 	private boolean running = true;
 
 	public static void main(String[] args) {
 <<<<<<< HEAD
 		new EntryPoint().init();
 	}
 
 	private void init() {
 		float ratio = 16f / 9f;
 		int width = 900;
 		int height = (int) (900f / ratio);
 
 		int fps = 100;
 
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.setTitle("Engine Test");
 			Display.setResizable(false);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 
 		MainFrame mainFame = new MainFrame();
 
 		mainFame.init();
 		int delta = 0;
 		long lastFrame = 0;
 		while (!Display.isCloseRequested() && running) {
 			lastFrame = getTime();
 			if (delta > 0) {
 				Display.setTitle("ms/frame: " + String.valueOf(delta));
 			}
 			mainFame.update(delta);
 			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 			glLoadIdentity();
 			mainFame.draw();
 			Display.update();
 			delta = 1000 / fps;
 			long time = getTime();
 			long rest = delta - (time - lastFrame);
 			if (rest < 0) {
 				delta -= rest;
 				rest = 0;
 			}
 			try {
 				Thread.sleep(rest);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Display.destroy();
 	}
 
 	public static long getTime() {
		return System.nanoTime() / 1000000;
 	}
 }
