 package ss.linearlogic.quizquest;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import ss.linearlogic.quizquest.entity.Player;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class QuizQuest {
 	
 	private static boolean running = true;
 	
 	private final int screen_width = 480;
 	private final int screen_height = 480;
 	
 	private long startTime = 0;
 	private long endTime = 0;
 	private long deltaTime = 0;
 	
 	//Constructor for game object
 	public QuizQuest() {
 		InitializeOpenGL(screen_width, screen_height);
 		MainLoop();
 		DestroyOpenGL();
 	}
 	
 	//Initialize the window along with the opengl aspects
 	public void InitializeOpenGL(int width, int height) {
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.setVSyncEnabled(true);
 			Display.create();
 		} catch (LWJGLException ex) {
 			ex.printStackTrace();
 		}
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, width, height, 0, -1, 1);
 		glMatrixMode(GL_MODELVIEW);
 		
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 	
 	//Create a mainloop to handle rendering and logic
 	public void MainLoop() {
 	
 		Map.Initialize("map.txt");
 		Map.AddTexture("Door.png", 1);
 		Map.AddTexture("Grass.png", 0);
 		Map.AddTexture("Wall.png", 2);
 		Map.AddTexture("Roof.png", 3);
 		
 		Player.Initialize(0, 0, "Door.png");
 		
 		while (running) {
 			//Close application when close is requested or escape key is pressed
			running = !Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
 				
 			startTime = getTime();
 			deltaTime = startTime - endTime;
 			
 			//Game rendering/logic area
 			glClear(GL_COLOR_BUFFER_BIT);
 			
 			Map.Render();
 			Player.Update();
 			Player.Render();
 			
 			//Update the display
 			Display.update();
 			Display.sync(60);
 			
 			endTime = getTime();
 		}
 	}
 	
 	//Destroy the opengl context
 	public void DestroyOpenGL() {
 		Display.destroy();
 	}
 	
 	//Main static method, entry point
 	public static void main(String[] args) {
 		new QuizQuest();
 	}
 
 	//Get the current time in millis from the system
 	private long getTime() {
 		return (long)(1000 * Sys.getTime() / Sys.getTimerResolution());
 	}
 }
