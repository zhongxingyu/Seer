 package objects;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 import static org.lwjgl.opengl.GL11.*;
 import window.BaseWindow;
 
 public class Refactored extends BaseWindow 
 {
 
  float scale = 1,angle=0, lx=0,lz=0, fraction=0.1f, x=100,y=100,z=100,ly=0,angley=0;
   public static Vector3f rotation = new Vector3f(0, 0, 0);
   public static float mouseSpeed = 0.01f;
   public static final int maxLookUp = 85;
   public static final int maxLookDown = -85;
   int xOrigin = -1;
   
   Terrain t_bg;
   Cube c_begin, c_end;
 	/**
 	 * Initial setup of projection of the scene onto screen, lights etc.
 	 */
   protected void setupView()
 	{    
     initializeModels();
     
     // enable depth buffer (off by default)
     glEnable(GL_DEPTH_TEST); 
     // enable culling of back sides of polygons
     glEnable(GL_CULL_FACE);
 
     // mapping from normalized to window coordinates
     glViewport(0, 0, 1024, 768);
 
     // setup projection matrix stack
     glMatrixMode(GL_PROJECTION);
     glLoadIdentity();
     GLU.gluPerspective(45, 1024 / (float)768, 1.0f, 1000.0f);
     
     setCameraMatrix();    
 	}
     
   protected void setCameraMatrix()
   {
     // model view stack 
     glMatrixMode(GL_MODELVIEW);
     glLoadIdentity();
     // setup view space; 
     // translate to 0,2,4 and rotate 30 degrees along x 
 //    glTranslatef(-WIDTH/2, -WIDTH/4, -WIDTH);
     //glRotatef(15.0f, 1.0f, 0.0f, 0.0f);   
    GLU.gluLookAt(WIDTH/2, WIDTH/4, -WIDTH, 0.0f, 0.0f, 0.0f,           0.0f, 1.0f, 0.0f);
   }
 
   /** 
    * can be used for 3D model initialization
    */
   protected void initializeModels()
   {
     t_bg = new Terrain(WIDTH);
     c_begin= new Cube(WIDTH);
     c_end = new Cube(WIDTH);
     
    
   }
   /**
    * Resets the view of current frame
    */
   protected void resetView()
   {
     // clear color and depth buffer
     glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
     //renderFramefirst();
   }
   
   /**
    * Renders current frame
    */
     
   protected void renderFrame()
   {
 	  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		// Reset transformations
 	  glLoadIdentity();
 		// Set the camera
 //      glTranslatef(position.x, position.y, position.z);
 		GLU.gluLookAt(
 				x, y,  z,
 				x+lx, y+ly,  z+lz,
 				0.0f, 1.0f,  0.0f);
 
 	//draw terrain
   t_bg.render3D();
   
   //draw beginning and anding of bubbles
   c_begin.m_nX+=0.01; 
   glDisable(GL_CULL_FACE);
   c_begin.render3D();
   glEnable(GL_CULL_FACE);
 	 
   }
   
   /**
    * Processes Keyboard and Mouse input and spawns actions
    */  
   protected void processInput()
   {
     if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
     }
     if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
 		angle += 0.01f;
     }
     if (Keyboard.isKeyDown(Keyboard.KEY_UP)){
    	x += lx * fraction;
		z += lz * fraction;
		y += ly * fraction;
     }
     if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
    	x -= lx * fraction;
    	z -= lz * fraction;
		y -= ly * fraction;
     }
     if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
     	y += 0.1;
     }    
     if (Mouse.isGrabbed()) {
         float mouseDX = Mouse.getDX()* mouseSpeed * 0.16f;;
         float mouseDY = Mouse.getDY()* mouseSpeed * 0.16f;;
         
         if(mouseDX>0){
     		angle += mouseDX;
         }
         if(mouseDX<0){
     		angle += mouseDX;
         }
         if(mouseDY>0){
         	angley+=mouseDY;
         }
         if(mouseDY<0){
         	angley+=mouseDY;
         }
         System.out.println(mouseDX+"   "+mouseDY);
         
     }
     while (Mouse.next()) {
         if (Mouse.isButtonDown(0)) {
             Mouse.setGrabbed(true);
         }
         if (Mouse.isButtonDown(1)) {
             Mouse.setGrabbed(false);
         }
 
     }
 	lx = (float) Math.sin(angle);
 	ly = (float) Math.tan(angley);
	lz = (float) - Math.cos(angle);
             
     super.processInput();
   }
   
   public static void main(String[] args) {
     (new Refactored()).execute();
   }  
 }
