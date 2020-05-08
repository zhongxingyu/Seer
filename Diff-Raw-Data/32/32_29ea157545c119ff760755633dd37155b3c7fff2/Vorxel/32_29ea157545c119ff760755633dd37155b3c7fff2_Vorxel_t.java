 package misson20000.api.vorxel;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import pitzik4.geode.Geode;
 
 public class Vorxel {
 	private static Geode game;
 
 	public static void init(Settings set, Geode gamep) {
 		game = gamep;
 		try {
 			Display.setDisplayMode(new DisplayMode(set.width,set.height));
 			new ThreadTick().start();
 			Display.create();
 		} catch (LWJGLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, 800, 0, 600, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 	}
 	
 	public static void main(String[] args) {
 		init(new Settings(), null);
 	}
 
 	public static void tick() {
 		// TODO Auto-generated method stub
 		if(game != null) {
 			game.tick();
 		}
 		Display.update();
 		if(Display.isCloseRequested()) {
			game.close();
 			Display.destroy();
 			System.exit(0);
 		}
 		render();
 	}
 
 	private static void render() {
 		System.out.println("Render");
 		// TODO Auto-generated method stub
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
 		// set the color of the quad (R,G,B,A)
 		GL11.glColor3f(0.5f,0.5f,1.0f);
 		 
 		// draw quad
 		GL11.glBegin(GL11.GL_QUADS);
 		GL11.glVertex2f(100,100);
 		GL11.glVertex2f(100+200,100);
 		GL11.glVertex2f(100+200,100+200);
 		GL11.glVertex2f(100,100+200);
 		GL11.glEnd();
 	}
 }
