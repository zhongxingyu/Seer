 
 import java.util.LinkedList;
 import java.util.List;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;
 
 public class Main {
 
     private static final int DISPLAY_WIDTH = 800;
     private static final int DISPLAY_HEIGHT = 600;
 
     public static void main(String[] args) {
         initDisplay();
         GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         ShaderManager.initShaders();
         
         List<Drawable> drawables = new LinkedList<Drawable>();
         
         float[] vertices1 = {
             0.1f, 0.5f, 0.0f,
             0.5f, 0.1f, 0.0f,
             0.1f, 0.1f, 0.0f};
         float[] colors1 = {
             0.0f, 0.0f, 0.9f,
             0.0f, 0.0f, 0.8f,
             0.0f, 0.0f, 0.7f};
         drawables.add(new Triangle(vertices1, colors1));
         
         float[] vertices2 = {
             -0.1f, -0.5f, 0.0f,
             -0.5f, 0.0f, 0.0f,
             -0.1f, 0.5f, 0.0f};
         float[] colors2 = {
             0.7f, 0.0f, 0.0f,
             1.0f, 0.0f, 0.0f,
             0.3f, 0.0f, 0.0f};
         drawables.add(new Triangle(vertices2, colors2));
 
         while (!Display.isCloseRequested()) {
             GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
             for (Drawable drawable : drawables) {
                 drawable.draw();
             }
             Display.update();
            
            try {
                Util.checkGLError();
            } catch (OpenGLException ex) {
                System.out.println(ex);
            }
         }
 
         Display.destroy();
 
     }
 
     private static void initDisplay() {
         try {
             Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
             Display.setTitle("Asteroids");
             Display.setVSyncEnabled(true);
             Display.create();
         } catch (LWJGLException ex) {
            System.out.println("Could not init display!");
             ex.printStackTrace();
            System.exit(1);
         }
     }
 }
