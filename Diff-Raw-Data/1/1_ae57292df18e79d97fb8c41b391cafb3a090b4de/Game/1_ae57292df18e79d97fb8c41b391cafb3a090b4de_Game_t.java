 package de.dakror.tube.game;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import de.dakror.tube.game.tube.Field;
 import de.dakror.tube.game.tube.Row;
 import de.dakror.tube.util.Camera;
 import de.dakror.tube.util.math.MathHelper;
 
 /**
  * @author Dakror
  */
 public class Game
 {
 	public static final float zNear = 0.1f;
 	public static final float zFar = 100f;
 	
 	public static Camera camera = new Camera();
 	public static Game currentGame;
 	
 	public float cameraSpeed = 0.3f;
 	public int cameraRotationSpeed = 180;
 	
 	Row[] rows;
 	
 	public Game()
 	{
 		createTube(10);
 	}
 	
 	public void createTube(int n)
 	{
 		float step = 360f / n;
 		float radius = (0.5f * Field.SIZE) / (float) Math.cos(Math.toRadians((180 - step) / 2f)) - 0.08f;
 		
 		rows = new Row[n];
 		for (int i = 0; i < n; i++)
 		{
 			float degs = step * i;
 			float rads = (float) Math.toRadians(degs);
 			rows[i] = new Row((float) Math.cos(rads) * radius, (float) Math.sin(rads) * radius, degs);
 		}
 	}
 	
 	public void gameLoop()
 	{
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_CULL_FACE);
 		glCullFace(GL_BACK);
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(30, Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
 		
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		
 		if (Mouse.isButtonDown(1))
 		{
 			Game.currentGame.rotateCamera();
 			Game.currentGame.moveCamera();
 		}
 		Mouse.setGrabbed(Mouse.isButtonDown(1));
 		
 		Vector3f u = camera.getPosition();
 		Vector3f v = MathHelper.getNormalizedRotationVector(camera.getRotation());
 		Vector3f w = camera.getPosition().translate(v.x, v.y, v.z);
 		
 		gluLookAt(u.x, u.y, u.z, w.x, w.y, w.z, 0, 1, 0);
 		
 		
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		// glTranslatef(Display.getWidth() / 2, Display.getHeight() / 2, 0);
 		for (int i = 0; i < rows.length; i++)
 		{
 			glPushMatrix();
 			rows[i].render();
 			glPopMatrix();
 		}
 		
 		Display.update();
 		Display.sync(60);
 	}
 	
 	public void rotateCamera()
 	{
 		if (!Mouse.isGrabbed()) Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 		
 		float x = (Mouse.getY() - Display.getHeight() / 2) / (float) Display.getHeight() * cameraRotationSpeed;
 		float y = (Mouse.getX() - Display.getWidth() / 2) / (float) Display.getWidth() * cameraRotationSpeed;
 		
 		if (Math.abs(camera.rotation.x - x) >= 90) x = 0;
 		
 		camera.rotate(-x, y, 0);
 		
 		Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 	}
 	
 	public void moveCamera()
 	{
 		if (Keyboard.isKeyDown(Keyboard.KEY_W))
 		{
 			camera.move((Vector3f) MathHelper.getNormalizedRotationVector(camera.getRotation()).scale(cameraSpeed));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S))
 		{
 			camera.move((Vector3f) MathHelper.getNormalizedRotationVector(camera.getRotation()).scale(cameraSpeed).negate());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D))
 		{
 			camera.move((Vector3f) getNormalizedRotationVectorForSidewardMovement(camera.getRotation().translate(0, 90, 0)).scale(cameraSpeed));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A))
 		{
 			camera.move((Vector3f) getNormalizedRotationVectorForSidewardMovement(camera.getRotation().translate(0, -90, 0)).scale(cameraSpeed));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
 		{
 			camera.move(0, 0.5f, 0);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_E))
 		{
 			camera.move(0, -0.5f, 0);
 		}
 	}
 	
 	public static Vector3f getNormalizedRotationVectorForSidewardMovement(Vector3f v)
 	{
 		double x = Math.sin(Math.toRadians(v.y));
 		double y = 0;
 		double z = Math.cos(Math.toRadians(v.y));
 		
 		return new Vector3f((float) -x, (float) -y, (float) z);
 	}
 	
 	public static void init()
 	{
 		currentGame = new Game();
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		
 		glShadeModel(GL_SMOOTH);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_POINT_SMOOTH);
 		glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
 		glEnable(GL_LINE_SMOOTH);
 		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
 		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
 		
 		// glEnable(GL_LIGHTING);
 		// glEnable(GL_LIGHT0);
 		//
 		// float amb = 0.5f;
 		//
 		// glLightModel(GL_LIGHT_MODEL_AMBIENT, MathHelper.asFloatBuffer(new float[] { amb, amb, amb, 1 }));
 		// glMaterial(GL_FRONT, GL_DIFFUSE, MathHelper.asFloatBuffer(new float[] { 1, 0, 0, 1 }));
 		// glMaterial(GL_FRONT, GL_AMBIENT, MathHelper.asFloatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1 }));
 		// glLight(GL_LIGHT0, GL_POSITION, MathHelper.asFloatBuffer(new float[] { 0, 0, 0, 1 }));
 		// glEnable(GL_COLOR_MATERIAL);
 		// glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);
 		// glMaterialf(GL_FRONT, GL_SHININESS, 1f);
 		//
 		// glEnable(GL_FOG);
 	}
 }
