 package com.deeter;
 
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import com.deeter.obj.builder.BuildHelper;
 import com.deeter.utility.Scene;
 
 public class ObjectLoaderTest {
 
     public final String WINDOW_TITLE = "Test OBJ loader";
     /** Desired frame time */
     private final int FRAMERATE = 60;
     private boolean finished;
    private String filename = "res/goblin.obj";
     private String defaultTextureMaterial = "";
     boolean fullscreen = false;
     private PatCamera camera;
     private Scene goblinScene;
     private Scene bunnyScene;
     
     public ObjectLoaderTest() {
         try {
         	this.setupOpenGL(fullscreen);
             this.setupCamera();
             this.setupScene();
             this.gameLoop();
         } catch (Exception e) {
             e.printStackTrace(System.err);
             Sys.alert(WINDOW_TITLE, "An error occured and the program will exit.");
         } finally {
             this.cleanup();
         }
         System.exit(0);
     }
 
     private void setupOpenGL(boolean fullscreen) throws Exception {
         Display.setTitle(WINDOW_TITLE);
         Display.setFullscreen(fullscreen);
         Display.setVSyncEnabled(true);
         Display.create();
 
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
         GL11.glLoadIdentity();
         GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
     }
     
     private void setupCamera() {
 		camera = new PatCamera.Builder()
 			.setAspectRatio((float) Display.getDisplayMode().getWidth() / (float) Display.getDisplayMode().getHeight())
 			.setFieldOfView(60)
 			.build();
 		camera.applyPerspectiveMatrix();
 		camera.applyOptimalStates();
 		camera.setPosition(500f, 50f, 200f);
 		Mouse.setGrabbed(true);
 	}
     
     private void setupScene() {
    	goblinScene = BuildHelper.setupScene(filename, defaultTextureMaterial, 0, 0, 0);
    	bunnyScene = BuildHelper.setupScene("res/bunny.obj", defaultTextureMaterial, 0, 0, 0);
     }
 
     private void gameLoop() {
         while (!finished) {
             GL11.glLoadIdentity();
             
             if (Display.isCloseRequested()) {
                 finished = true;
             } // The window is in the foreground, so render!
             else if (Display.isActive()) {
             	this.logic();
                 this.render();
                 Display.sync(FRAMERATE);
             } // The window is not in the foreground, so we can allow other stuff to run and infrequently update
             else {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                 }
                 this.logic();
 
                 // Only bother rendering if the window is visible or dirty
                 if (Display.isVisible() || Display.isDirty()) {
                     System.err.print(".");
                     this.render();
                 }
             }
             
             Display.update();
         }
     }
 
     private void cleanup() {
         Display.destroy();
     }
 
     private void logic() {
     	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
     		finished = true;
     	}
     	camera.applyTranslations();
         if (Mouse.isGrabbed()) {
 			camera.processMouse();
 			camera.processKeyboard(16, 50.0f);
 		}
     }
     
     private void render() {
     	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
         GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
         
         GL11.glColor3f(0.49f, 0.2f, 0.043f);
         for (int i = 0; i < 10; i++) {
         	goblinScene.render(null);
             GL11.glTranslatef(100.0f, 0, 0);
         }
 
         GL11.glTranslatef(0, 50f, 0);
         for (int i = 0; i < 10; i++) {
         	bunnyScene.render(null);
         	GL11.glTranslatef(100.0f, 0, 0);
         }
     }
     
     public static void main(String[] args) {
     	new ObjectLoaderTest();
     }
 }
