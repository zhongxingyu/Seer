 package core;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluOrtho2D;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.python.util.PythonInterpreter;
 
 public class Main {
 	private static PythonInterpreter interpreter;
 	
 	public static void main(String[] args) {
 		interpreter = new PythonInterpreter();
 		createWindow();
 		initOpenGL();
 		clearScreen();
 		newFrame();
 		clearScreen();
 		TextureRenderer.init();
 		
 		interpreter.exec(InitScript.initScript);
 		interpreter.execfile("script.py");
 		idle();
 	}
 
 	private static void createWindow() {
 		try {
 			Display.setResizable(true);
 			Display.setTitle("Python Powered Pixels");
 			Display.setLocation(100, 100);
 			Display.setDisplayMode(new DisplayMode(640, 480));
 			Display.create();
 			
 			Mouse.create();
 			Keyboard.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void initOpenGL() {
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		gluOrtho2D(0, Display.getWidth(), 0, Display.getHeight());
 		glClearColor(1, 1, 1, 1);
 		glShadeModel(GL_SMOOTH);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glDisable(GL_TEXTURE_2D);
 	}
 	
 	public static void newFrame() {
 		TextureRenderer.drawScreenFrame();
 		TextureRenderer.updateTexture();
 		
 		Display.update();
 		Display.sync(60);
 
 		if(Display.isCloseRequested()) {
 			Display.destroy();
 			System.exit(0);
 		}
 		
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glViewport(0, 0, Display.getWidth(), Display.getHeight()); 
 		gluOrtho2D(0, Display.getWidth(), 0, Display.getHeight());
 		
 		Input.updateVariables(interpreter);
 		
 		glDisable(GL_TEXTURE_2D);
 	}
 	
 	public static void clearScreen() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
 	}
 
 	private static void idle() {
 		while(!Display.isCloseRequested()) {
 			TextureRenderer.drawScreenFrame();
 			
 			Display.update();
 			Display.sync(60);
 		}
 	}
 
 } 
