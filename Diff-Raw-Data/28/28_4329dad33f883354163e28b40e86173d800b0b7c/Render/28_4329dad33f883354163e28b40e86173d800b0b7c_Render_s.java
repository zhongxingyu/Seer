 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package nordland.render;
 
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.opengl.ARBVertexBufferObject;
 
 import org.lwjgl.LWJGLException;
 
 
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 
 
 import nordland.render.Voxel;
 import nordland.render.VBO;
 
 import nordland.util.math.Vector3;
 import nordland.world.map.Tile;
 import nordland.world.map.Chunk;
 import nordland.world.World;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.ByteOrder;
 
 import org.lwjgl.input.Mouse;
 
 
 /**
  *
  * @author red
  */
 public class Render {
     private static final Render INSTANCE = new Render();
 
     public static final VBO vbo = new VBO();
 
     public static final int DISPLAY_HEIGHT = 480;
     public static final int DISPLAY_WIDTH = 640;
 
     public Vector3f cursor_position = new Vector3f(0.0f, 0.0f, 0.0f);
     public Vector3f mouse_position = new Vector3f(0.0f, 0.0f, 0.0f);
 
     public Voxel voxel_render ;
 
     private Render() {
 
     }
 
     public static Render getInstance() {
         return INSTANCE;
     }
 
     public void create() throws LWJGLException {
         Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH,DISPLAY_HEIGHT));
         Display.setFullscreen(false);
         Display.setTitle("JSKiller");
         Display.create();
         
         if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object == false) {
             throw new RuntimeException("OpenGL Vertex Buffer Objects are not supported by Graphics Card. Unable to run program.");
         }
         initGL();
 
         vbo.init();
         vbo.rebuild();
 
         voxel_render = new Voxel(0.0f, 0.0f, 0.0f);
         voxel_render.init();
     }
 
     public void initGL(){
 
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         GL11.glLoadIdentity();
         //GL11.glFrustum(-1, 1, -1, 1, -1, 1000);
 //        Calculate the aspect ratio of the window
         GLU.gluPerspective(45.0f,((float)DISPLAY_WIDTH)/((float)DISPLAY_HEIGHT),0.1f,1000.0f);
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
         GL11.glLoadIdentity();
         GL11.glEnable(GL11.GL_TEXTURE_2D);                                    // Enable Texture Mapping ( NEW )
         GL11.glShadeModel(GL11.GL_SMOOTH);
         GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         GL11.glClearDepth(1.0f);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         GL11.glDepthFunc(GL11.GL_LEQUAL);
         GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);
 
     }
 
     //==========================================================================
     //public static Tile[][][] tiles = new Tile[200][200][200];
 
     public void rebuild_vbo() {
         
          vbo.rebuild();
     }
 
 
     private float lightAmbient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
     private float lightDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f }; 
     private float lightPosition[] = { 1.0f, 1.0f, 0.0f, 0.1f };
     float lightSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
 
     public void render_all() {
         
     GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
     GL11.glTranslatef(0.0f,-2.0f,0.0f);                              // Move Into The Screen 5 Units
 
     /*ByteBuffer temp = ByteBuffer.allocateDirect(16);
     temp.order(ByteOrder.nativeOrder());
     GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(lightAmbient).flip());
     GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(lightDiffuse).flip());
     GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(lightPosition).flip());
     GL11.glEnable(GL11.GL_LIGHT1);
     GL11.glEnable(GL11.GL_LIGHTING);
 
     GL11.glEnable(GL11.GL_COLOR_MATERIAL);
     GL11.glColorMaterial ( GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE ) ;*/
 
         vbo.render();
 
        //Mouse.getX(),Mouse.getY()
         FloatBuffer World_Ray = Raycast.getMousePosition(320,200);
             int wx = (int)World_Ray.get(0);
             int wy = (int)World_Ray.get(1);
             int wz = (int)World_Ray.get(2);
 
         Display.setTitle("FPS: 0"
             + " WX: " + Integer.toString(wx)
             + " WY: " + Integer.toString(wy)
             + " WZ: " + Integer.toString(wz)
         );
 
         //cursor_position.set(wx,wy,wz);
 
         //render aim cursor
        voxel_render.tile_id = 8;
         voxel_render.set_origin(wx , wy, wz);
         voxel_render.render();
 
     }
 }
