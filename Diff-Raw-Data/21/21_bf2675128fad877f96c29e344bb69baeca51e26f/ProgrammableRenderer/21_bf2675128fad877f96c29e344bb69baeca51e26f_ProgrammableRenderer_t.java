 /*
  *	This is the thing that draws thing.  There's not much more to it.
  * 
  *	//TODO:  Add functions for modifying how light is produced/drawn
  */
 
 package engine.render;
 
 import java.awt.Canvas;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.opengl.PixelFormat;
 
 import engine.entity.Camera;
 import engine.entity.Entity;
 import engine.entity.EntityList;
 import engine.entity.QueueItem;
 import engine.window.WindowManager;
 
 public class ProgrammableRenderer extends RendererInterface {
 	private WindowManager window_manager;
 	private EntityList object_list;
 	private Camera camera;
 	private static boolean supports_vbo = false;
 	// private float x=0,y=0,z=0;
 
 	public static float near_clipping = 1f;
 	public static float far_clipping = 10000.0f;
 	private float zoom = 1f; // The closer this value is to 0, the farther you
 
 	private Canvas display_parent;
 	
 	private ConcurrentLinkedQueue<QueueItem> render_queue;
 
 	public ProgrammableRenderer(EntityList objectList) {
 		this(objectList, null);
 		render_queue = new ConcurrentLinkedQueue<QueueItem>();
 	}
 
 	public ProgrammableRenderer(EntityList objectList, Canvas display_parent) {
 		this.object_list = objectList;
 		this.display_parent = display_parent;
 	}
 
 	public void draw() {
 		// Clear The Screen And The Depth Buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
 		if (camera != null) {
 			camera.updatePosition();
 		} else {
 			camera = (Camera) object_list.getItem(Camera.CAMERA_NAME);
 			if( camera != null ){
 				camera.updatePosition();
 			}else{
 				System.out.println("WARNING: Tried to draw without camera set...");
 				return;
 			}
 		}
 		
 		// Draw the 3d stuff
 		for (Entity ent : object_list.getEntitiesAndSubEntities()){
 			Boolean should_draw = (Boolean)ent.getProperty(Entity.SHOULD_DRAW);
 			if(should_draw == null)
 				should_draw = false;
 			if(should_draw)
 				ent.drawProgrammablePipe();
 		}
 
 		// Draw the window manager stuff
 		if (window_manager != null) window_manager.draw();
 
 		Display.update();
 	}
 
 	public void initGL() {
 		// Setup Display
 		try {
 			if (display_parent != null) {
 				Display.setParent(display_parent);
 			}
			Display.setDisplayMode(new DisplayMode(1000,1000));
 			Display.create(new PixelFormat(24,8,24,0,0));
 			Display.setTitle("JGE3d");
 
 			// Create a fullscreen window with 1:1 orthographic 2D projection
 			// (default)
 			Display.setFullscreen(false);
 
 			// Enable vsync if we can (due to how OpenGL works, it cannot be
 			// guarenteed to always work)
 			// TODO: Make Configurable by User
 			Display.setVSyncEnabled(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		window_manager = new WindowManager();
 
 		// camera = (Camera) objectList.getItem(Camera.CAMERA_NAME);
 
 		setPerspective();
 
 		// Set default openGL for drawing
 		//GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		GL11.glClearDepth(1.0f);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 
 		// Initialize default settings
 		ByteBuffer temp = ByteBuffer.allocateDirect(16);
 		temp.order(ByteOrder.nativeOrder());
 
 		if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
 			supports_vbo = true;
 		} else {
 			supports_vbo = false;
 		}
 
 		// Blending functions so we can have transparency
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glEnable(GL11.GL_BLEND);
 	}
 
 	public void setPerspective() {
 		setPerspective(near_clipping, far_clipping, zoom);
 	}
 
 	public void setPerspective(float near, float far, float zoomVal) {
 		if(camera != null) {
 			near_clipping = camera.getNear();
 			far_clipping = camera.getFar();
 			if (zoomVal <= 1.0 && zoomVal > 0) {
 				zoom = zoomVal;
 			} else if (zoomVal > 1000.0) {
 				zoom = 1.0f;
 			} else {
 				zoom = 0.1f; // TODO: I guess this is the smallest zoom we'd want?
 			}
 		}
 	}
 
 	public static boolean supportsVBO() {
 		return supports_vbo;
 	}
 	
 	public WindowManager getWindowManager() {
 		return window_manager;
 	}
 
 	public void destroy() {
 		window_manager.destroy();
 	}
 
 	public void setCamera(Camera camera) {
 		this.camera = camera;
 		setPerspective();
 	}
 	
 	public void parseRenderQueue() {
 		if(render_queue.size() > 0){
 			Object[] itemArray = render_queue.toArray();
 			for (Object item : itemArray) {
 				if (QueueItem.ADD == ((QueueItem) item).getAction()) {
 					addRenderItem(((QueueItem) item).getEnt());
 				} else if (QueueItem.REMOVE == ((QueueItem) item).getAction()) {
 					removeRenderItem(((QueueItem) item).getEnt());
 				}
 	
 				render_queue.remove(item);
 			}
 		}
 	}
 
 	// Add an item to the entity List
 	private void addRenderItem(Entity e) {
 		if (e.keyExists("name")) {
 			if( e.keyExists("model")){
 				//TODO: Check to make sure actually is a Model class
 				Model ent_model = (Model)e.getProperty("model");
 				
 				if (ent_model != null) {
 					ent_model.verify();
 					ent_model.createVBO();
 					ent_model.reduceHull();
 					
 					e.setCollisionShape(ent_model.getCollisionShape());
 				} else {
 					System.out.println("Trying to add/update render object of NULL model");
 				}
 			}
 		} else {
 			System.out.println("Trying to add/update render object of unnamed entity");
 		}
 	}
 	
 	private void removeRenderItem(Entity e) {
 		if (e.keyExists("name")) {
 			//TODO: Check to make sure actually is a Model class
 			Model ent_model = (Model)e.getProperty("model");
 			
 			if (ent_model != null) {
 				ent_model.destroyVBO();
 			} else {
 				System.out.println("Trying to delete render object of NULL model");
 			}
 		} else {
 			System.out.println("Trying to delete render object of unnamed entity");
 		}
 	}
 		
 	public void entityAdded(Entity ent) {
 		render_queue.add(new QueueItem(ent, QueueItem.ADD));
 		ent.addListener(this);
 	}
 
 	public void entityRemoved(Entity ent) {
 		render_queue.add(new QueueItem(ent, QueueItem.REMOVE));
 		ent.removeListener(this);
 	}
 
 	@Override
 	public void entityPropertyChanged(String property, Entity entity, Object old_value) {
 		if(property == "model"){
 			render_queue.add(new QueueItem(entity, QueueItem.ADD));
 		}
 	}
 }
