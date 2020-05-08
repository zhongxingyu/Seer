 package es.ld23;
 
import static org.lwjgl.opengl.GL11.*;

 public class Game {
 
 	private static final boolean debug = true;
	private int width;
	private int height;
 
 	static void debug(String string) {
 		if (debug) {
 			System.out.println(string);
 		}
 	}
 
 	public Game(int w, int h) {
		width = w;
		height = h;
 	}
 
 	public void initGL() {
		glViewport(0, 0, width, height);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
 	}
 
 	public void loadResources() {
 	}
 
 	public void render() {
 	}
 
 	public void tick() {
 	}
 }
