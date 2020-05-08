 /*
  *	This is the thing that draws thing.  There's not much more to it.
  * 
  *	//TODO:  Add functions for modifying how light is produced/drawn
  */
 
 package render;
 
 import java.awt.Canvas;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.util.glu.GLU;
 
 import entity.Camera;
 import entity.EntityList;
 
 import window.Window;
 import window.tree.Model;
 
 public class Renderer {
 	private Window window;
 	private EntityList objectList;
     private Camera camera;
     private boolean isInitialized=false;
     private boolean supportsVBO=false;
 	//private float x=0,y=0,z=0;
 	
     public static float nearClipping = 0.01f;
 	public static float farClipping = 50.0f;
 	private float zoom = 1f;  //The closer this value is to 0, the farther you are zoomed in.
 	
 	//Default light (needs turning into an entity
     private float lightAmbient[]={ 0.2f, 0.2f, 0.2f, 1.0f };    // Ambient Light Values ( NEW )
     private float lightDiffuse[]={ 1f, 1f, 1f, 1f }; // { 0.8f, 0.8f, 0.8f, 1.0f };    // Diffuse Light Values ( NEW )
     private float lightSpecular[]={ 1f, 1f, 1f, 1.0f };
     private float lightPosition[]={ 0.0f, 15.0f, 0.0f, 1.0f };   // Light Position ( NEW )
 	private Canvas display_parent;
 
 	public Renderer(EntityList objectList, Canvas display_parent){
 		this.objectList = objectList;
 		this.display_parent = display_parent;
 	}
 
 	public Renderer(EntityList objectList, Model m) {
 		this.objectList = objectList;
 	}
 
 	public void draw() {
 		GLContext.getCapabilities().toString();
 		//Move the camera with its focus
 		camera.updatePosition();
 		
 		//Get its new position
 		Vector3f camPos = camera.getPosition();
 		Vector3f focusPos = camera.getFocusPosition();
 		Vector3f up = camera.getUp();
 
 		// Clear The Screen And The Depth Buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
 		GL11.glLoadIdentity();
 		
 		//Look at the camera's focus
 		GLU.gluLookAt(
 				camPos.x, camPos.y, camPos.z, 	//Camera Location
 				focusPos.x, focusPos.y, focusPos.z, 		//Focus On Location
 				up.x, up.y, up.z			//Up Vector
 		);
 
 		//Draw the 3d stuff
 		objectList.drawList();
 		
 		//Draw the window manager stuff
 		window.draw();
 		
 		GL11.glFlush();
 		Display.update();
 
 		// Reduce input lag
 		//Display.processMessages(); // process new native messages since
 	}
 	
 	public void initGL() {		
 		//Setup Display
 		try {
 			Display.setParent(display_parent);
 			Display.create();
 			Display.setTitle("JGE3d");
 			//TODO:  Make Configurable by User
 			Display.setVSyncEnabled(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		window = new Window(objectList);
 
 		while(objectList.getItem(Camera.CAMERA_NAME)==null) {
 			
 		}
 		camera = (Camera) objectList.getItem(Camera.CAMERA_NAME);
 
 		window.setCamera(camera);
 		
 		setPerspective();
 
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		GL11.glClearDepth(1.0f);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 		
 		GL11.glEnable(GL11.GL_LIGHTING);
 		
 		//Initialize default settings
         ByteBuffer temp = ByteBuffer.allocateDirect(16);
         temp.order(ByteOrder.nativeOrder());
         
         // Setup The Ambient Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(lightAmbient).flip());              
         
         // Setup The Diffuse Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(lightDiffuse).flip());              
         
         // Setup The Specular Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, (FloatBuffer)temp.asFloatBuffer().put(lightSpecular).flip());        
         
         // Position The Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(lightPosition).flip());
         
         GL11.glEnable(GL11.GL_LIGHT1);
         
        if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object)
         	supportsVBO=true;
        else
         	supportsVBO=false;
 		isInitialized=true;
 	}
 
 	public void setPerspective(){setPerspective(nearClipping,farClipping,zoom);}
 	public void setPerspective(float near, float far, float zoomVal) {
 		nearClipping = near;
 		farClipping = far;
 		if(zoomVal <= 1.0 && zoomVal > 0){
 			zoom = zoomVal;
 		}else if(zoomVal > 1000.0){
 			zoom = 1.0f;
 		}else{
 			zoom = 0.1f; //TODO:  I guess this is the smallest zoom we'd want?
 		}
 		
 		//Calculate the shape of the screen and notify OpenGL
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GLU.gluPerspective(
 			45.0f/zoom, 
 			(float) window.getWidth() / window.getHeight(), 
 			nearClipping, 
 			farClipping
 		);
 
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 	}
 	
 	public boolean supportsVBO() {
 		return supportsVBO;
 	}
 	
 	public void destroy() {window.destroy();}
 	
 	public boolean isInitialized() {return isInitialized;}
 }
