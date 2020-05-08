 package core;
 
 import Screens.ScreenManager;
 import helpers.Delegate;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 
 import java.awt.*;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/22/13
  * Time: 11:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     private static boolean isChanged;
     private static ScreenManager SM;
 
     public static void init() {
         Delegate d = new Delegate() {
             @Override
             public void change(int val) {
                 isChanged = val != 0;
             }
         };
 
         SM = new ScreenManager(d);
 
     }
 
     public static void main(String[] args) {
         try {
             LibraryLoader.loadNativeLibraries();
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Sorry :-(");
             System.exit(0);
         }
 
         try {
             Display.setDisplayMode(new DisplayMode(800, 600));
             Display.create();
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
 
         Display.setTitle("Project");
 
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         glOrtho(0, 800, 600, 0, -1, 1);
         glMatrixMode(GL_MODELVIEW);
 
         init();
 
         isChanged = true;
 
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
         while (!Display.isCloseRequested()) {
             if (isChanged) {
                 SM.Initialize();
                 isChanged = false;
             }
             SM.Render();
             SM.Update();
             Display.update();
             Display.sync(80);
         }
 
         Display.destroy();
 
         System.exit(0);
     }
 
 }
