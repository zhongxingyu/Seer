 package game;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import javax.swing.UIManager;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.util.vector.Vector3f;
 
 
 import physics.collisionObjects.CollisionBox;
 import physics.collisionObjects.CollisionObject;
 import render.Model;
 import render.util.OBJLoader;
 import render.util.ShaderLoader;
 import util.math.MathHelper;
 import editor.Editor;
 
 public class Main
 {
 	public static Model m;
 
 	static int i = 0;
 	static Camera camera = new Camera();
 
 	public static int cameraSpeed;
 	public static boolean fullscreen;
 	public static int resX = 0;
 	public static int resY = 0;
 	public static CollisionBox c;
 	private static DisplayMode[] fullscreenmodes;
 	public static final LogManager logmanager = LogManager.getLogManager();
 
 	public static Vector3f lightPos = new Vector3f();
 
 	public static final Logger log = Logger.getLogger("BOLT");
 
 	public static void main(String[] args)
 	{
 		Game.currentGame = new TestGame();
 		Game.currentGame.prepareGame();
 
 		try
 		{
 			File logFile = new File("./nonsync/logging.properties");
 			if (!logFile.exists()) logFile.createNewFile();
 			logmanager.readConfiguration(new FileInputStream(logFile));
 			
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e2)
 		{
 			e2.printStackTrace();
 		}
 		log.setLevel(Level.ALL);
 
 		if (args.length > 0)
 		{
 			if (args[0].toLowerCase().equals("-editor"))
 			{
 				new Editor();
 				return;
 			}
 
 		}
 
 		loadOptions();
 		System.out.printf("fullscreen: %b\nresolution: %dx%d\ncameraspeed: %d\n", fullscreen, resX, resY, cameraSpeed);
 		try
 		{
 			fullscreenmodes = Display.getAvailableDisplayModes();
 			System.out.printf("available fullscreen-modes:\n");
 			for (DisplayMode akt : fullscreenmodes)
 			{
 				System.out.printf("%dx%d,%dbpp,%dHz\n", akt.getWidth(), akt.getHeight(), akt.getBitsPerPixel(), akt.getFrequency());
 			}
 			if (fullscreen)
 			{
 				enterFullscreen();
 			}
 			else
 			{
 				leaveFullscreen();
 			}
 			Display.create();
 		}
 		catch (LWJGLException e1)
 		{
 			e1.printStackTrace();
 		}
 
 		try
 		{
 			m = OBJLoader.loadModel("test/crystal.obj");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		initGLSettings();
 		c = CollisionBox.create(m.getVerteciesAsArray());
 		log.log(Level.INFO, c.toString());
 
 		Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 		Mouse.setGrabbed(true);
 
 		Game.currentGame.initializeGame();
 
 		while (!Display.isCloseRequested())
 			gameLoop();
 
 		Display.destroy();
 	}
 
 	public static void enterFullscreen() throws LWJGLException
 	{
 		// Display.setFullscreen(true);
 		boolean found = false;
 		for (DisplayMode akt : fullscreenmodes)
 		{
 			if (akt.getWidth() == resX && akt.getHeight() == resY)
 			{
 				Display.setDisplayModeAndFullscreen(akt);
 				found = true;
 			}
 		}
 		if (!found)
 		{
 			System.out.printf("can not find matching resolution - falling back to desktop resolution\n");
 			Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
 		}
 	}
 
 	public static void leaveFullscreen() throws LWJGLException
 	{
 		Display.setDisplayMode(new DisplayMode(resX, resY));
 		Display.setFullscreen(false);
 	}
 
 	public static void toggleFullscreen()
 	{
 		try
 		{
 			if (Display.isFullscreen())
 			{
 				leaveFullscreen();
 			}
 			else
 			{
 				enterFullscreen();
 			}
 		}
 		catch (LWJGLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void gameLoop()
 	{
 		i++;
 
 		camera.rotation.y += ((Mouse.getX() - (Display.getWidth() / 2)) / (float) Display.getWidth()) * cameraSpeed;
 		camera.rotation.x -= ((Mouse.getY() - (Display.getHeight() / 2)) / (float) Display.getHeight()) * cameraSpeed;
 
 		camera.rotation.x = MathHelper.clamp(camera.rotation.x, -90, 90);
 
 		Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 
 		double x = Math.sin(Math.toRadians(camera.rotation.y)) * GameRules.cameraSpeed;
 		double y = -Math.sin(Math.toRadians(camera.rotation.x)) * GameRules.cameraSpeed;
 		double z = -Math.cos(Math.toRadians(camera.rotation.y)) * GameRules.cameraSpeed;
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_W))
 		{
 			camera.position.x += x * Math.cos(Math.toRadians(camera.rotation.x));
 			camera.position.y += y;
 			camera.position.z += z * Math.cos(Math.toRadians(camera.rotation.x));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S))
 		{
 			camera.position.x -= x * Math.cos(Math.toRadians(camera.rotation.x));
 			camera.position.y -= y;
 			camera.position.z -= z * Math.cos(Math.toRadians(camera.rotation.x));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A))
 		{
 			camera.position.x += Math.sin(Math.toRadians(camera.rotation.y - 90)) * GameRules.cameraSpeed;
 			camera.position.z -= Math.cos(Math.toRadians(camera.rotation.y - 90)) * GameRules.cameraSpeed;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D))
 		{
 			camera.position.x += Math.sin(Math.toRadians(camera.rotation.y + 90)) * GameRules.cameraSpeed;
 			camera.position.z -= Math.cos(Math.toRadians(camera.rotation.y + 90)) * GameRules.cameraSpeed;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_E))
 		{
 			lightPos.x = camera.position.x;
 			lightPos.y = camera.position.y;
 			lightPos.z = camera.position.z;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
 		{
 			lightPos.z++;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
 		{
 			lightPos.z--;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
 		{
 			lightPos.x++;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
 		{
 			lightPos.x--;
 		}
 		glPushMatrix();
 
 		glRotated(camera.rotation.x, 1f, 0f, 0f);
 		glRotated(camera.rotation.y, 0f, 1f, 0f);
 		glRotated(camera.rotation.z, 0f, 0f, 1f);
 
 		glTranslatef(-camera.position.x, -camera.position.y, -camera.position.z);
 
 		glLight(GL_LIGHT0, GL_POSITION, MathHelper.asFloatBuffer(new float[] { lightPos.x, lightPos.y, lightPos.z, 1 }));
 
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 		Game.currentGame.gameLoop();
 
 		glColor4d(1, 1, 1, 1);
 		glPointSize(10);
 		for (Vector3f v : c.points)
 		{
 			glBegin(GL_POINTS);
 			glVertex3f(v.x, v.y, v.z);
 			glEnd();
 			// System.out.println(v);
 		}
 
 		Display.update();
 		Display.sync(50);
 
 		glPopMatrix();
 	}
 
 	public static void initGLSettings()
 	{
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(100, (float) Display.getWidth() / Display.getHeight(), 0.01f, 1000);
 		glMatrixMode(GL_MODELVIEW);
 
 		glShadeModel(GL_SMOOTH);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_CULL_FACE);
 
 		glEnable(GL_LIGHTING);
 		glEnable(GL_LIGHT0);
 		glLightModel(GL_LIGHT_MODEL_AMBIENT, MathHelper.asFloatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1f }));
 		glLight(GL_LIGHT0, GL_DIFFUSE, MathHelper.asFloatBuffer(new float[] { 1.5f, 1.5f, 1.5f, 1 }));
 		glEnable(GL_COLOR_MATERIAL);
 		glColorMaterial(GL_FRONT, GL_DIFFUSE);
 		glMaterialf(GL_FRONT, GL_SHININESS, 1000f);
 
 		glLightModel(GL_LIGHT_MODEL_AMBIENT, MathHelper.asFloatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1 }));
 
 		ShaderLoader.useProgram("test/", "shader");
 	}
 
 	public static void loadOptions()
 	{
 		File OBJFile = new File("nonsync/options.txt");
 		String line;
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(OBJFile));
 			while ((line = reader.readLine()) != null)
 			{
 				if (line.startsWith("cameraSpeed")) cameraSpeed = Integer.valueOf(line.split("=")[1]);
 				if (line.startsWith("fullscreen")) fullscreen = Boolean.valueOf(line.split("=")[1]);
 				if (line.startsWith("resX")) resX = Integer.valueOf(line.split("=")[1]);
 				if (line.startsWith("resY")) resY = Integer.valueOf(line.split("=")[1]);
 			}
 			reader.close();
 		}
 		catch (FileNotFoundException e1)
 		{
 			e1.printStackTrace();
 		}
 		catch (NumberFormatException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
