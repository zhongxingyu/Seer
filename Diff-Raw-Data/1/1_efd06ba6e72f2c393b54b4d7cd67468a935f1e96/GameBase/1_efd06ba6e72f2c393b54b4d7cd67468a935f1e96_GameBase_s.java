 package grige;
 
 import com.jogamp.newt.opengl.GLWindow;
 
 import com.jogamp.newt.event.WindowEvent;
 import com.jogamp.newt.event.WindowListener;
 import com.jogamp.newt.event.WindowUpdateEvent;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLAutoDrawable;
 
 import java.util.ArrayList;
 
 public abstract class GameBase implements GLEventListener, WindowListener{
 	
 	static GameBase instance;
 	
 	//Game State Data
 	private boolean running;
 	private ArrayList<GameObject> worldObjects;
 	private ArrayList<Light> worldLights;
 	private float currentFPS;
 	
 	//Game Managers
 	protected Camera camera;
 	protected Audio audio;
 	
 	//OpenGL Data
 	private GLProfile glProfile;
 	private GLCapabilities glCapabilities;
 	private GLWindow gameWindow;
 	
 	protected abstract void initialize();
 	protected abstract void update(float deltaTime);
 	protected abstract void display();
 	
 	public GameBase()
 	{
 		GameBase.instance = this;
 	}
 	
 	public final void start()
 	{
 		internalSetup();
 		gameWindow.display(); //Draw once before looping to initalize the screen/opengl
 		
 		running = true;
 		long lastFrameTime = System.nanoTime();
 		
 		while(running)
 		{	
 			long currentTime = System.nanoTime();
 			float deltaTime = (currentTime - lastFrameTime)/1000000000f;
 			lastFrameTime = currentTime;
 			currentFPS = 1f/deltaTime;
 			
 			internalUpdate(deltaTime);
 			gameWindow.display();
 		}
 		cleanup();
 	}
 	
 	protected void internalSetup()
 	{
 		//Initialize the OpenGL profile that the game will use
 		glProfile = GLProfile.getDefault();
 		glCapabilities = new GLCapabilities(glProfile);
 		
 		//Create the game window
 		gameWindow = GLWindow.create(glCapabilities);
 		gameWindow.setSize(320, 320);
 		gameWindow.setVisible(true);
 		gameWindow.setTitle("GrIGE");
 		
 		//Create the various managers for the game
 		camera = new Camera(gameWindow.getWidth(),gameWindow.getHeight(),10000);
 		
 		//Add the required event listeners
 		gameWindow.addWindowListener(this);
 		gameWindow.addGLEventListener(this);
 		
 		//Instantiate other structures
 		worldObjects = new ArrayList<GameObject>();
 		worldLights = new ArrayList<Light>();
 	}
 	
 	public float getFPS()
 	{
 		return currentFPS;
 	}
 	
 	public void addObject(GameObject obj)
 	{
 		worldObjects.add(obj);
 	}
 	
 	public void addLight(Light l)
 	{
 		worldLights.add(l);
 	}
 	
 	public Drawable[] getObjectsAtLocation(Vector2 loc)
 	{
 		ArrayList<Drawable> objList = new ArrayList<Drawable>();
 		
 		for(Drawable d : worldObjects)
 		{
 			AABB bounds = d.getAABB();
 			if(bounds.contains(loc))
 				objList.add(d);
 		}
 		
 		return objList.toArray(new Drawable[objList.size()]);
 	}
 	
 	public void destroy(GameObject obj)
 	{
 		obj.markedForDeath = true;
 	}
 	
 	private void internalUpdate(float deltaTime)
 	{
 		ArrayList<GameObject> deathList = new ArrayList<GameObject>();
 		
 		//Update input data
 		Input.update();
 		
 		//Run an update on all objects
 		for(GameObject obj : worldObjects)
 		{
 			if(obj.markedForDeath)
 				deathList.add(obj);
 			else
 				obj.update(deltaTime);
 		}
 		
 		//Remove all objects that are marked for death
 		for(GameObject obj : deathList)
 			worldObjects.remove(obj);
 		
 		//Call the user-defined game update
 		update(deltaTime);
 	}
 	
 	protected void cleanup()
 	{
 		Audio.cleanup();
 		
 		gameWindow.destroy();
 		GLProfile.shutdown();
 	}
 	
 	GL getGL()
 	{
 		return gameWindow.getGL();
 	}
 	
 	//Window utility functions
 	public String getWindowTitle() { return gameWindow.getTitle(); }
 	public boolean isFullscreen() { return gameWindow.isFullscreen(); }
 	public Vector2I getWindowSize() { return new Vector2I(gameWindow.getWidth(), gameWindow.getHeight()); }
 	public int getWindowWidth() { return gameWindow.getWidth(); }
 	public int getWindowHeight() { return gameWindow.getHeight(); }
 	
 	public void setWindowTitle(String title)
 	{
 		gameWindow.setTitle(title);
 	}
 	
 	public void setWindowSize(Vector2I size)
 	{
 		setWindowSize(size.x, size.y);
 	}
 	
 	public void setWindowSize(int width, int height)
 	{
 		gameWindow.setSize(width, height);
 	}
 	
 	public void setFullscreen(boolean fullscreen)
 	{
 		gameWindow.setFullscreen(fullscreen);
 	}
 	
 	//GLEvent listener methods
 	public final void init(GLAutoDrawable glad)
 	{
 		//Initialize internal components
 		camera.initialize(glad.getGL());
 		Audio.initialize();
 		Input.initialize(gameWindow.getHeight());
 		
 		//Add input listeners
 		gameWindow.addKeyListener(Input.getInstance());
 		gameWindow.addMouseListener(Input.getInstance());
 		
 		//Run child class initialization
 		initialize();
 	}
 	
 	public final void display(GLAutoDrawable glad)
 	{
 		GL gl = glad.getGL();
 		
 		//Reset the camera for this draw call
 		camera.refresh(gl);
 		
 		camera.drawLightingStart();
 		//Draw all our objects into the depth buffer so that our shadows can get depth-tested correctly against objects at the same depth
 		for(GameObject obj : worldObjects)
 			camera.drawObjectDepthToLighting(obj);
 		
 		//Draw *all* the lights
 		gl.glEnable(GL.GL_STENCIL_TEST); //We need to stencil out bits of light, so enable stencil test while we're drawing lights
 		for(Light l : worldLights)
 		{
 			ArrayList<float[]> vertexArrays = new ArrayList<float[]>();
 			for(GameObject obj : worldObjects)
 			{
 				//Compute/store the vertices of the shadow of this objected, as a result of the current light
 				float[] vertices = camera.generateShadowVertices(l, obj);
 				vertexArrays.add(vertices);
 			}
 			
 			camera.drawShadowsToStencil(vertexArrays);
 			
 			//Draw lighting (where the stencil is empty)
 			camera.drawLight(l);
 			camera.clearShadowStencil();
 		}
 		gl.glDisable(GL.GL_STENCIL_TEST); //We only use stencil test for rendering lights
 		camera.drawLightingEnd();
 		
 		camera.drawGeometryStart();
 		//Draw all the objects now that we've finalized our lighting
 		for(GameObject obj : worldObjects)
 			camera.drawObject(obj);
 		camera.drawGeometryEnd();
 		
 		//Let the child game class draw any required UI
 		camera.drawInterfaceStart();
 		display();
 		camera.drawInterfaceEnd();
 		
 		//Commit all drawing thats happened, combining them via their respective framebuffers as needed
 		camera.commitDraw();
 	}
 	
 	public void reshape(GLAutoDrawable glad, int x, int y, int width, int height)
 	{
 		camera.setSize(width, height, camera.getDepth());
 	}
 
 	public void dispose(GLAutoDrawable glad){}
 	
 	//Window listener methods
 	public void windowDestroyNotify(WindowEvent we)
 	{
 		running = false;
 	}
 	
 	public void windowDestroyed(WindowEvent we){}
 	public void windowGainedFocus(WindowEvent we){}
 	public void windowLostFocus(WindowEvent we){}
 	public void windowMoved(WindowEvent we){}
 	public void windowResized(WindowEvent we){}
 	public void windowRepaint(WindowUpdateEvent wue){}
 	
 	public static void printOpenGLError(GL gl, boolean displayNoError)
 	{
 		int error = gl.glGetError();
 		switch(error)
 		{
 		case(GL.GL_NO_ERROR):
 			if(displayNoError)
 				System.out.println("No Error");
 			break;
 		
 		case(GL.GL_INVALID_ENUM):
 			System.out.println("Invalid Enum");
 			break;
 		
 		case(GL.GL_INVALID_VALUE):
 			System.out.println("Invalid Value");
 			break;
 			
 		case(GL.GL_INVALID_OPERATION):
 			System.out.println("Invalid Operation");
 			break;
 			
 		case(GL.GL_INVALID_FRAMEBUFFER_OPERATION):
 			System.out.println("Invalid Framebuffer Operation");
 			break;
 			
 		case(GL.GL_OUT_OF_MEMORY):
 			System.out.println("Out of Memory");
 			break;
 			
 		default:
 			System.out.println("UNKNOWN OPENGL ERROR: "+error);
 		}
 	}
 }
