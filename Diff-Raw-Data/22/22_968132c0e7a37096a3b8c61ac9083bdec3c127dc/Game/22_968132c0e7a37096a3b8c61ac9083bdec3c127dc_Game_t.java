 package cx.it.hyperbadger.spacezombies;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import cx.it.hyperbadger.spacezombies.explorer.Planet;
 import cx.it.hyperbadger.spacezombies.explorer.Ship;
 import cx.it.hyperbadger.spacezombies.explorer.ShipControl;
 import cx.it.hyperbadger.spacezombies.explorer.SolarSystem;
 
 import static org.lwjgl.opengl.GL11.*;
 @SuppressWarnings("unused")
 public class Game {
 	private int fps, lastFPS;
 	private Planet planetEarth, theSun, theMoon;
 	public static final double G = 100;
 	private static long time, lastFrame;
 	private SolarSystem sol = null;
 	private Ship ship = null;
 	public static long delta = 160;
 	private ShipControl shipControl = null;
	private int screenWidth = 1500, screenHeight = 1000;
 	public Game(){
 		try {
 			Display.setDisplayMode(new DisplayMode(screenWidth,screenHeight));
 			Display.setTitle("Space Zombies");
 			Display.create();
 		} catch (LWJGLException e) {
 			System.err.println("Could not create display");
 			System.exit(0);
 		}
 		//start opengl
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, screenWidth, screenHeight, 0, 1, -1);
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_TEXTURE_2D);
 		glShadeModel(GL11.GL_SMOOTH);                            // Enable Smooth Shading
         glClearColor(0.0f, 0.0f, 0.0f, 0.5f);               // Black Background
         glClearDepth(1.0f);
        glEnable(GL_BLEND); 
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		//load planets
 		ArrayList<Planet> solPlanets = new ArrayList<Planet>();
 		sol = new SolarSystem("Sole",solPlanets);
 		solPlanets.add(new Planet(800,500,100,10000,"Sun","sun.png",null));
 		solPlanets.add(new Planet(1000,500,20,10,"Earth","earth.png",sol.findPlanet("Sun")));
 		solPlanets.add(new Planet(1025,500,5,1,"Moon","moon.png",sol.findPlanet("Earth")));
 		ship = new Ship(100,1000,800,"spaceship.png");
 		shipControl = new ShipControl(ship);
 		//initialize loop
 		Display.update();
 		Display.sync(60);
 		Game.delta = getDelta();
 		while(!Display.isCloseRequested()){
 			loop();
 		}
 		//loop end destroy display
 		Display.destroy();
 	}
 	public void loop(){
 		//clear
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	
 		//draw
 		sol.move();
 		sol.draw();
 		ship.draw();
 		ship.move(sol.getMasses());
 		shipControl.update();
 		//update
 		Display.update();
 		Display.sync(60);
 		Display.setTitle("Space Zombies - "+ship.getVelocity()+"m/s");
 		Game.delta = getDelta();
 	}
 	public int getDelta() {
 		long time = getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 		return delta;
 	}
 	public long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 }
