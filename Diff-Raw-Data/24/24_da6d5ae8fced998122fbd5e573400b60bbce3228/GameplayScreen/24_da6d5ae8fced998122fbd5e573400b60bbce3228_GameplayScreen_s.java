 package Screens;
 
 import core.Camera;
 import helpers.Delegate;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 
 import java.util.ArrayList;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hp
  * Date: 23/04/13
  * Time: 3:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameplayScreen extends Screen
 {
     //Camera for single player
     Camera cam;
 
     public GameplayScreen(Delegate d) {
         super(d);
         cam = new Camera(new Vector3f(0, 0, 0));
     }
 
     public void Initialize(){

     }
 
     public void Render(){
 
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glColor3f(0.0f, 1.0f, 1.0f);
 
         glRotatef(0.0f, 0.0f, 0.0f, 1.0f);
         glDisable(GL_DEPTH_TEST);
 
         glBegin(GL_QUADS);
 
         glColor3d(1, 0, 0);
         glVertex3i(50, 50, 50);
         glVertex3i(50, -50, 50);
         glVertex3i(-50, -50, 50);
         glVertex3i(-50, 50, 50);
 
 
         glColor3d(0, 1, 0);
         glVertex3i(50, 50, -50);
         glVertex3i(50, -50, -50);
         glVertex3i(-50, -50, -50);
         glVertex3i(-50, 50, -50);
 
 
         glColor3d(0, 0, 1);
         glVertex3i(50, 50, 50);
         glVertex3i(50, -50, 50);
         glVertex3i(50, -50, -50);
         glVertex3i(50, 50, -50);
 
 
         glColor3d(0, 1, 1);
         glVertex3i(-50, 50, 50);
         glVertex3i(-50, -50, 50);
         glVertex3i(-50, -50, -50);
         glVertex3i(-50, 50, -50);
 
 
         glColor3d(1, 0, 0);
         glVertex3i(-50, 50, -50);
         glVertex3i(-50, 50, 50);
         glVertex3i(50, 50, 50);
         glVertex3i(50, 50, -50);
 
 
         glColor3d(1, 1, 0);
         glVertex3i(-50, -50, -50);
         glVertex3i(-50, -50, 50);
         glVertex3i(50, -50, 50);
         glVertex3i(50, -50, -50);
 
         glEnd();
         cam.setCameraView();
 
     }
 
     public void Update(){
         if(Keyboard.isKeyDown(Keyboard.KEY_UP))
         {
             cam.walk(1);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
         {
             cam.rwalk(1);
         }
     }
 }
