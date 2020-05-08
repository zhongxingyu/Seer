 package danmw3.games.blocker;
 
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.Color;
 
 public class Blocker {
 
 	private Vector3f position = null;
 
 	private float yaw = 0.0f;
 
 	private float pitch = 0.0f;
 
 	private boolean jumping = false;
 
 	private static final float eyeHeight = -2f;
 
 	static TrueTypeFont font;
 	
 	public Blocker(float x, float y, float z) {
 		position = new Vector3f(x, y, z);
 	}
 
 	public Blocker() {
 
 	}
 
 	public void yaw(float amount) {
 
 		yaw += amount;
 	}
 
 	public void pitch(float amount) {
 
 		pitch += amount;
 	}
 
 	public void walkForward(float distance) {
 		position.x -= distance * (float) Math.sin(Math.toRadians(yaw));
 		position.z += distance * (float) Math.cos(Math.toRadians(yaw));
 	}
 
 	public void walkBackwards(float distance) {
 		position.x += distance * (float) Math.sin(Math.toRadians(yaw));
 		position.z -= distance * (float) Math.cos(Math.toRadians(yaw));
 	}
 
 	public void strafeLeft(float distance) {
 		position.x -= distance * (float) Math.sin(Math.toRadians(yaw - 90));
 		position.z += distance * (float) Math.cos(Math.toRadians(yaw - 90));
 	}
 
 	public void strafeRight(float distance) {
 		position.x -= distance * (float) Math.sin(Math.toRadians(yaw + 90));
 		position.z += distance * (float) Math.cos(Math.toRadians(yaw + 90));
 	}
 
 	public void lookThrough() {
 		GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
 
 		GL11.glRotatef(yaw, 0.0f, 1.0f, 0.0f);
 
 		GL11.glTranslatef(position.x, position.y, position.z);
 	}
 
 	private static boolean gameRunning = true;
 	private static int targetWidth = 800;
 	private static int targetHeight = 600;
 
 	private float xrot = 0.1f;
 	private float yrot = 0.1f;
 	private float zrot = 0.1f;
 
 	public static void initDisplay(boolean fullscreen) {
 		DisplayMode chosenMode = null;
 
 		try {
 			DisplayMode[] modes = Display.getAvailableDisplayModes();
 
 			for (int i = 0; i < modes.length; i++) {
 				if ((modes[i].getWidth() == targetWidth)
 						&& (modes[i].getHeight() == targetHeight)) {
 					chosenMode = modes[i];
 					break;
 				}
 			}
 		} catch (LWJGLException e) {
 			Sys.alert("Error", "Unable to determine display modes.");
 			System.exit(0);
 		}
 
 		if (chosenMode == null) {
 			Sys.alert("Error", "Unable to find appropriate display mode.");
 			System.exit(0);
 		}
 
 		try {
 			Display.setDisplayMode(chosenMode);
 			Display.setFullscreen(fullscreen);
 			Display.setTitle("Secret Title");
 			Display.create();
 
 		} catch (LWJGLException e) {
 			Sys.alert("Error", "Unable to create display.");
 			System.exit(0);
 		}
 
 	}
 
 	public static boolean initGL() {
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 
 		GLU.gluPerspective(45.0f, ((float) targetWidth)
 				/ ((float) targetHeight), 0.1f, 100.0f);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		GL11.glClearDepth(1.0f);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 		return true;
 	}
 
 	public boolean isKeyPressed(int keyCode) {
 		switch (keyCode) {
 		case KeyEvent.VK_SPACE:
 			keyCode = Keyboard.KEY_SPACE;
 			break;
 		case KeyEvent.VK_ESCAPE:
 			keyCode = Keyboard.KEY_ESCAPE;
 			break;
 		case KeyEvent.VK_W:
 			keyCode = Keyboard.KEY_W;
 			break;
 		case KeyEvent.VK_A:
 			keyCode = Keyboard.KEY_A;
 			break;
 		case KeyEvent.VK_S:
 			keyCode = Keyboard.KEY_S;
 			break;
 		case KeyEvent.VK_D:
 			keyCode = Keyboard.KEY_D;
 			break;
 		}
 
 		return org.lwjgl.input.Keyboard.isKeyDown(keyCode);
 	}
 
 	public void run() throws FontFormatException, IOException {
 		Blocker camera = new Blocker(0, eyeHeight, 0);
 
 		float dx = 0.0f;
 		float dy = 0.0f;
 
 		float motionX = 0.0f;
 		float motionY = 0.0f;
 		float motionZ = 0.0f;
 		float gravity = 9.8f; // WE BE LIVIN' ON EARTH
 		long timeJumpStart = 0L;
 		// Longs because of timer accuracy
 		long dt = 0;
 		long lastTime = 0;
 		long time = 0;
 
 		float mouseSensitivity = 0.05f;
 		float movementSpeed = 5.0f;
 		float physicsSpeed = 300f;
 		Mouse.setGrabbed(true);
 		gameRunning = true;
 		while (gameRunning) {
 			update();
 			// Display.update goes AFTER all rendering
 			// Sync to limit FPS, and not burn all yer CPU's
 			Display.sync(120);
 			Display.processMessages();
 
 			// Re-did the timing system
 			time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
 			dt = (time - lastTime);
 			lastTime = time;
 
 			Mouse.poll();
 			dx = Mouse.getDX();
 
 			dy = Mouse.getDY();
 
 			camera.yaw(dx * mouseSensitivity);
 
 			camera.pitch(dy * -mouseSensitivity);
 
 			Keyboard.poll();
 			if (Keyboard.isKeyDown(Keyboard.KEY_W))// forward
 			{
 				camera.walkForward(movementSpeed * dt / physicsSpeed);
 
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_S))// backwards
 			{
 				camera.walkBackwards(movementSpeed * dt / physicsSpeed);
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_A))// left
 			{
 				camera.strafeLeft(movementSpeed * dt / physicsSpeed);
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_D))// right
 			{
 				camera.strafeRight(movementSpeed * dt / physicsSpeed);
 			}
 
 			// only jump once, unless you want weird flight
 			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !jumping) {
 				motionY = 15;
 				timeJumpStart = time;
 				jumping = true;
 			}
 
 			// It'll kill it in the first sec. otherwise
 			if (jumping && time != timeJumpStart) {
 				// Real-life physics yo
 				float float_sec = (time - timeJumpStart) / physicsSpeed;
 				if (float_sec != 0) {
 					float newPosY = ((1f / 2f * gravity * float_sec * float_sec)
 							- (motionY * float_sec) - (camera.position.y));
 					if (newPosY >= -eyeHeight) {
 						jumping = false;
 						timeJumpStart = 0;
 						newPosY = eyeHeight;
 					}
 					camera.position.y = newPosY;
 
 				}
 			}
			
 			/* Commenting out because spam */
 			/*
 			 * System.out.println("X: " + Math.round(camera.position.x) + " Y: "
 			 * + Math.round(-camera.position.y) + " Z: " +
 			 * Math.round(camera.position.z));
 			 */
 			// Moved render code, to help with flicker when jumping
 			render();
 			GL11.glLoadIdentity();
 			camera.lookThrough();
 			Display.update(false);
 
 			if (Display.isCloseRequested()) {
 				Mouse.setGrabbed(false);
 				gameRunning = false;
 				continue;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				Mouse.setGrabbed(false);
 				int result = JOptionPane.showConfirmDialog(null,
 						"Are you sure you want to quit?", "Close",
 						JOptionPane.OK_CANCEL_OPTION,
 						JOptionPane.QUESTION_MESSAGE);
 				if (result == JOptionPane.OK_OPTION) {
 					gameRunning = false;
 					continue;
 				}
 				try {
 					Thread.sleep(1000);
 					Mouse.setGrabbed(true);
 				} catch (InterruptedException is) {
 				}
 			}
 			
 		}
 		try {
 			Display.releaseContext();
 			Display.destroy();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void update() {
 		xrot += 0.1f;
 		yrot += 0.1f;
 		zrot += 0.1f;
 	}
 
 	private void render() {
 		font.drawString(10, 10, "HELLO", Color.yellow);
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		
 		GL11.glTranslatef(-1f, 0.0f, -70f);
 
 		int xBlocks = 10;
 		int yBlocks = 1;
 		int zBlocks = 10;
 
 		for (int x = 0; x < xBlocks; x++) {
 			for (int y = 0; y < yBlocks; y++) {
 				for (int z = 0; z < zBlocks; z++) {
 					RenderCube();
 					GL11.glTranslatef(0f, 0f, 2f);
 				}
 				GL11.glTranslatef(0f, 2f, -(zBlocks * 2));
 			}
 			GL11.glTranslatef(2f, -(yBlocks * 2), 0);
 		}
 		
 	}
 
 	public void RenderCube() {
 		// GL11.glBegin(GL11.GL_LINE_LOOP);
 		GL11.glBegin(GL11.GL_QUADS);
 		GL11.glColor3f(0.0f, 1.0f, 0.0f);
 		GL11.glVertex3f(1.0f, 1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
 		GL11.glVertex3f(1.0f, 1.0f, 1.0f);
 
 		GL11.glColor3f(1.0f, 0.5f, 0.0f);
 		GL11.glVertex3f(1.0f, -1.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
 		GL11.glVertex3f(1.0f, -1.0f, -1.0f);
 
 		GL11.glColor3f(1.0f, 0.0f, 0.0f);
 		GL11.glVertex3f(1.0f, 1.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
 		GL11.glVertex3f(1.0f, -1.0f, 1.0f);
 
 		GL11.glColor3f(1.0f, 1.0f, 0.0f);
 		GL11.glVertex3f(1.0f, -1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
 		GL11.glVertex3f(1.0f, 1.0f, -1.0f);
 
 		GL11.glColor3f(0.0f, 0.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
 		GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
 		GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
 
 		GL11.glColor3f(1.0f, 0.0f, 1.0f);
 		GL11.glVertex3f(1.0f, 1.0f, -1.0f);
 		GL11.glVertex3f(1.0f, 1.0f, 1.0f);
 		GL11.glVertex3f(1.0f, -1.0f, 1.0f);
 		GL11.glVertex3f(1.0f, -1.0f, -1.0f);
 		GL11.glEnd();
 	}
 	
 	public static void init(){
 		Font awtFont = new Font("Times New Roman", Font.PLAIN, 2);
 		font = new TrueTypeFont(awtFont, false);
 	}
 	
 	public static void main(String[] args) {
 		Blocker app = new Blocker();
 		JFrame mainMenu = new FrameTesting("Secret Title", 0, 275, 475);
 		JFrame console = new FrameTesting("Console", 1, 600, 475);
 		// initDisplay(false);
 		// app.run();
 
 	}
 }
