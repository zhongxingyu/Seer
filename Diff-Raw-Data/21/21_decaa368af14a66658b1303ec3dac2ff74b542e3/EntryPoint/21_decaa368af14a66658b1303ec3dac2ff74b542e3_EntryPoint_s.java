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
=======
		System.out.println("Hello World!");
		Matrix m = Matrix.mat(new float[][] { new float[] { 1, 2, 3 }, new float[] { 4, 5, 6 } });
		Matrix m1 = Matrix.mat(new float[][] { new float[] { 1, 2 }, new float[] { 4, 5 },
				new float[] { 1, 5 } });
		Matrix m2 = m.multiply(m1);
		Matrix m3 = Matrix.mat(new float[][] { new float[] { 1, 4, 3 }, new float[] { 4, 50, 6 },
				new float[] { 1, 2, 3 } });
		float det = m3.determinant();
		Vector v = Matrix.vec3(1, 2, 3);
		Vector3 v1 = (Vector3) m2.multiply(v);
		System.out.println(m);
		System.out.println(m1);
		System.out.println(m2);
		System.out.println(m3);
		System.out.println(v1);
		System.out.println(v1.getY());
		System.out.println(det);
>>>>>>> branch 'master' of https://github.com/cookiehunter/Engine.git
 	}
 }
