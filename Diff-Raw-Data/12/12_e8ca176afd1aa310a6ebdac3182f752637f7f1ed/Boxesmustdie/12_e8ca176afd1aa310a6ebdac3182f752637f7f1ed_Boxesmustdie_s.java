 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.lwjgl.opengl.GL11;
 
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.utils.BufferUtils;
 
 public class Boxesmustdie implements ApplicationListener {
 	
 	public static void main(String[] arg)
 	{
 		//LwjglApplication(ApplicationListener listener, java.lang.String title, int width, int height, boolean useGL2) 
 		new LwjglApplication(new Boxesmustdie(), "Boxes must die", 800, 600, false); 
 	}
 	
     // Vertex buffer.
     public FloatBuffer vertexBuffer = null;
     
     public Random random; 
 
     public List<Box> shapes; 
     
     public List<Box> destroyed;
     
     public List<Box> toInsert;
     
     public Player player; 
     
     public boolean spacebarheld, bossfight;
     
     int collisioncount, framecount, level; 
    
     
     @Override
     public void create() {
         System.out.println("Created!");
         random = new Random(); 
         shapes = new ArrayList<Box>(); 
         toInsert = new ArrayList<Box>();
         bossfight = false; 
         framecount = 0;
         level = 1; 
         destroyed = new ArrayList<Box>(); 
         
         player = new Player(10, 10, 20, shapes, this);
         shapes.add(player);
         
         
         this.vertexBuffer = BufferUtils.newFloatBuffer(8);
         
         float[] box = new float[] {0,0, 0,1,  1,0,  1,1};
         this.vertexBuffer.put(box);
         this.vertexBuffer.rewind();
         Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, this.vertexBuffer);
         
         // Enable vertex array.
         Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
         
         // Select clear color for the screen.
         Gdx.gl11.glClearColor(.3f, .3f, .3f, 1f);
         
         
     }
 
     @Override
     public void dispose() {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void pause() {
         // TODO Auto-generated method stub
     }
     
     private void display(){
         Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT);
         Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW);
         
         for (Box s : shapes)
         {
             s.display(); 
         } 
     }
     
     private void update()
     {
     	framecount += 1;
    	if (framecount%2000 == 0)
     	{
     		bossfight = true; 
     		float startpos = random.nextInt(600) + 1;
     		shapes.add(new Overlord(800, startpos, 40, this, 20));
     	}
     	int rand = random.nextInt(3) + 1; 
 		if (rand == 1 && !bossfight)
 		{
     		float startpos = random.nextInt(600) + 1;
     		shapes.add(new EnemyBox(800, startpos, 20, this, 2)); 
 		}
     	
     	for (Box s: shapes)
     	{
     		s.update(); 
     	}
     	
     	for (Box b : toInsert)
     	{
     		shapes.add(b);
     	}
     	toInsert.clear();
     	
     	for (Box b : destroyed)
     	{
     		shapes.remove(b);
     	}
     	
     }
     
     
     
     @Override
     public void render() {
         this.display();
         this.update();
     }
 
     @Override
     public void resize(int width, int height) {
         // Load the Project matrix. Next commands will be applied on that matrix.
         Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
         Gdx.gl11.glLoadIdentity();
 
         // Set up a two-dimensional orthographic viewing region.
         Gdx.glu.gluOrtho2D(Gdx.gl11, 0, width, 0, height);
 
         // Set up affine transformation of x and y from world coordinates to window coordinates
         Gdx.gl11.glViewport(0, 0, width, height);
 
         // Set the Modelview matrix back.
         Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);    
     }
 
     @Override
     public void resume() {
         // TODO Auto-generated method stub
     }
     
     public void cleanup()
     {
     	shapes = new ArrayList<Box>();
    	shapes.add(player);
     }
 }
 
