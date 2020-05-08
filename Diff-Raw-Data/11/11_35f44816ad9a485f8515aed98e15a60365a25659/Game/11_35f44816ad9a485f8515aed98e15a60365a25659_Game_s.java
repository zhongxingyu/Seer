 package com.vloxlands.game;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.Vloxlands;
 import com.vloxlands.game.util.Camera;
 import com.vloxlands.game.util.ViewFrustum;
 import com.vloxlands.game.voxel.Voxel;
 import com.vloxlands.game.world.Map;
 import com.vloxlands.gen.MapGenerator;
 import com.vloxlands.render.ChunkRenderer;
 import com.vloxlands.render.model.Model;
 import com.vloxlands.render.util.ModelLoader;
 import com.vloxlands.render.util.ShaderLoader;
 import com.vloxlands.scene.Scene;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.settings.Settings;
 import com.vloxlands.util.FontAssistant;
 import com.vloxlands.util.MathHelper;
 import com.vloxlands.util.RenderAssistant;
 
 import de.dakror.universion.UniVersion;
 
 public class Game
 {
 	public static Game currentGame;
 	public static Map currentMap;
 	public ViewFrustum viewFrustum = new ViewFrustum();
 	
	public float zNear = 0.001f, zFar = 10000;
 	Vector3f up = new Vector3f(0, 1, 0);
 	
 	public static MapGenerator mapGenerator;
 	
 	public Camera camera = new Camera();
 	
 	long start = 0, last;
 	public int frames = 21;
 	boolean showFPS = false;
 	
 	Model m = ModelLoader.loadModel("/graphics/models/crystal.obj");
 	
 	Scene scene;
 	
 	public float cameraSpeed = 0.3f;
 	public int cameraRotationSpeed = 180;
 	private Vector3f lightPos = new Vector3f();
 	private Vector3f directionalArrowsPos = new Vector3f();
 	
 	public void gameLoop()
 	{
 		if (start == 0) start = System.currentTimeMillis();
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_CULL_FACE);
 		ShaderLoader.useProgram("/graphics/shaders/", "default");
 		if (CFG.LIGHTING) RenderAssistant.enable(GL_LIGHTING);
 		else RenderAssistant.disable(GL_LIGHTING);
 		
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		moveCamera();
 		gluPerspective(CFG.FOV, Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
 		viewFrustum.calculateViewFrustum(camera.getPosition(), camera.getRotation(), up, zNear, zFar);
 		
 		// glRotated(camera.getRotation().x, 1f, 0f, 0f);
 		// glRotated(camera.getRotation().y, 0f, 1f, 0f);
 		// glRotated(camera.getRotation().z, 0f, 0f, 1f);
 		//
 		// glTranslated(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
 		
 		Vector3f u = camera.getPosition();
 		Vector3f v = MathHelper.getNormalizedRotationVector(camera.getRotation());
 		Vector3f w = camera.getPosition().translate(v.x, v.y, v.z);
 		
 		// CFG.p("u: " + u);
 		
 		gluLookAt(u.x, u.y, u.z, w.x, w.y, w.z, 0, 1, 0);
 		
 		
 		if (CFG.SHOW_DIRECTIONS) renderDirectionalArrows();
 		
 		if (currentMap != null)
 		{
 			glPushMatrix();
 			{
 				currentMap.render();
 				
 				glPointSize(10);
 				glBegin(GL_POINTS);
 				{
 					glVertex3f(lightPos.x, lightPos.y, lightPos.z);
 				}
 				glEnd();
 			}
 			glPopMatrix();
 		}
 		
 		RenderAssistant.set2DRenderMode(true);
 		
 		glPushMatrix();
 		{
 			if (scene != null) scene.update();
 		}
 		glPopMatrix();
 		while (Keyboard.next())
 		{
 			if (Keyboard.getEventKey() == Keyboard.KEY_F11 && !Keyboard.getEventKeyState())
 			{
 				CFG.FULLSCREEN = !CFG.FULLSCREEN;
 				Vloxlands.setFullscreen();
 				updateViewport();
 				Settings.saveSettings();
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_F4 && !Keyboard.getEventKeyState()) showFPS = !showFPS;
 			if (Keyboard.getEventKey() == Keyboard.KEY_L && !Keyboard.getEventKeyState()) CFG.LIGHTING = !CFG.LIGHTING;
 			if (Keyboard.getEventKey() == Keyboard.KEY_B && !Keyboard.getEventKeyState()) CFG.SHOW_CHUNK_BOUNDRIES = !CFG.SHOW_CHUNK_BOUNDRIES;
 			if (Keyboard.getEventKey() == Keyboard.KEY_T && Keyboard.getEventKeyState()) ChunkRenderer.renderChunks(currentMap.islands.get(0));
 			if (Keyboard.getEventKey() == Keyboard.KEY_Z && Keyboard.getEventKeyState())
 			{
 				CFG.SHOW_WIREFRAME = !CFG.SHOW_WIREFRAME;
 				ChunkRenderer.renderChunks(currentMap.islands.get(0));
 			}
 			if (Keyboard.getEventKey() == Keyboard.KEY_V && !Keyboard.getEventKeyState())
 			{
 				CFG.SHOW_DIRECTIONS = !CFG.SHOW_DIRECTIONS;
 				directionalArrowsPos = new Vector3f(camera.getPosition());
 			}
 			if (scene != null) scene.handleKeyboard(Keyboard.getEventKey(), Keyboard.getEventKeyState());
 		}
 		
 		glColor4f(1, 1, 1, 1);
 		if (showFPS) RenderAssistant.renderText(0, 0, getFPS() + "", FontAssistant.GAMEFONT.deriveFont(30f));
 		
 		glColor4f(0.8f, 0.8f, 0.8f, 1);
 		RenderAssistant.renderText(Display.getWidth() - 250, 0, UniVersion.prettyVersion(), FontAssistant.GAMEFONT.deriveFont(20f));
 		glColor4f(1, 1, 1, 1);
 		
 		RenderAssistant.set2DRenderMode(false);
 		
 		if (Keyboard.isKeyDown(Keyboard.KEY_R))
 		{
 			lightPos.x = camera.position.x;
 			lightPos.y = camera.position.y;
 			lightPos.z = camera.position.z;
 			RenderAssistant.setUniform3f("lightPosition", lightPos);
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
 		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) cameraSpeed = 0.5f;
 		else cameraSpeed = 0.3f;
 		
 		
 		if (currentMap != null) currentMap.onTick();
 		
 		Display.update();
 		if (Display.wasResized()) updateViewport();
 		
 		if (CFG.FPS <= 120) Display.sync(CFG.FPS);
 		
 		frames++;
 		
 		if (mapGenerator != null && mapGenerator.isDone())
 		{
 			mapGenerator.map.initMap();
 			currentMap = mapGenerator.map;
 			mapGenerator = null;
 		}
 		
 		last = System.currentTimeMillis();
 		
 	}
 	
 	public void updateViewport()
 	{
 		scene.content.clear();
 		scene.init();
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 	}
 	
 	public static void initGame()
 	{
 		Voxel.loadVoxels();
 		RenderAssistant.storeTextureAtlas("graphics/textures/voxelTextures.png", 16, 16);
 		currentGame = new Game();
 		currentGame.resetCamera();
 	}
 	
 	public void resetCamera()
 	{
 		camera.setPosition(128.5f, 130, 128.5f);
 		camera.setRotation(30, 135, 0);
 	}
 	
 	public void setScene(Scene s)
 	{
 		scene = s;
 		scene.init();
 	}
 	
 	public int getFPS()
 	{
 		return Math.round(frames / ((System.currentTimeMillis() - start) / 1000f));
 	}
 	
 	public static void initGLSettings()
 	{
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
		glEnable(GL_ALPHA_TEST);
 		
 		glShadeModel(GL_SMOOTH);
 		glEnable(GL_POINT_SMOOTH);
 		glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
 		glEnable(GL_LINE_SMOOTH);
 		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
 		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
 		
 		glEnable(GL_LIGHTING);
 		glEnable(GL_LIGHT0);
 		glLightModel(GL_LIGHT_MODEL_AMBIENT, MathHelper.asFloatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1f }));
 		glLight(GL_LIGHT0, GL_DIFFUSE, MathHelper.asFloatBuffer(new float[] { 1.5f, 1.5f, 1.5f, 1 }));
 		glEnable(GL_COLOR_MATERIAL);
 		glColorMaterial(GL_FRONT, GL_DIFFUSE);
 		glMaterialf(GL_FRONT, GL_SHININESS, 100f);
 		
 		glMaterial(GL_FRONT, GL_DIFFUSE, MathHelper.asFloatBuffer(new float[] { 1, 0, 0, 1 }));
 		glMaterial(GL_FRONT, GL_SPECULAR, MathHelper.asFloatBuffer(new float[] { 1, 1, 1, 1 }));
 		glMaterial(GL_FRONT, GL_AMBIENT, MathHelper.asFloatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1 }));
 		
 		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
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
 	
 	public void rotateCamera()
 	{
 		float x = (Mouse.getY() - Display.getHeight() / 2) / (float) Display.getHeight() * cameraRotationSpeed;
 		float y = (Mouse.getX() - Display.getWidth() / 2) / (float) Display.getWidth() * cameraRotationSpeed;
 		
 		if (Math.abs(camera.rotation.x - x) >= 90) x = 0;
 		
 		camera.rotate(-x, y, 0);
 		
 		Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 	}
 	
 	
 	public static Vector3f getNormalizedRotationVectorForSidewardMovement(Vector3f v)
 	{
 		double x = Math.sin(Math.toRadians(v.y));
 		double y = 0;
 		double z = Math.cos(Math.toRadians(v.y));
 		
 		return new Vector3f((float) -x, (float) -y, (float) z);
 	}
 	
 	public void renderDirectionalArrows()
 	{
 		glPushMatrix();
 		{
 			glTranslatef(directionalArrowsPos.x, directionalArrowsPos.y, directionalArrowsPos.z);
 			glLineWidth(10);
 			glColor3d(1, 0, 0);
 			glBegin(GL_LINES);
 			{
 				glVertex3d(0, 0, 0);
 				glVertex3d(2, 0, 0);
 			}
 			glEnd();
 			
 			glColor3d(0, 1, 0);
 			
 			glBegin(GL_LINES);
 			{
 				glVertex3d(0, 0, 0);
 				glVertex3d(0, 2, 0);
 			}
 			glEnd();
 			
 			glColor3d(0, 0, 1);
 			
 			glBegin(GL_LINES);
 			{
 				glVertex3d(0, 0, 0);
 				glVertex3d(0, 0, 2);
 			}
 			glEnd();
 			
 			glColor3d(1, 1, 1);
 			glLineWidth(1);
 		}
 		glPopMatrix();
 	}
 }
