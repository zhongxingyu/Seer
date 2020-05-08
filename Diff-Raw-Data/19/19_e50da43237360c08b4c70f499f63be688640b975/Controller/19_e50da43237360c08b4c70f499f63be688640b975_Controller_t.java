 /*
  * The thing that controls everything....
  */
 package controller;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import importing.Obj_Parser;
 import importing.Parser;
 import importing.XGL_Parser;
 import importing.pieces.Model;
 
 import javax.vecmath.Vector3f;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.lwjgl.opengl.Display;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import physics.Physics;
 
 import com.bulletphysics.collision.dispatch.CollisionFlags;
 
 import com.bulletphysics.collision.shapes.BoxShape;
 import com.bulletphysics.collision.shapes.CollisionShape;
 
 import de.matthiasmann.twl.model.TreeTableNode;
 
 import entity.Camera;
 import entity.Entity;
 import entity.EntityList;
 import entity.QueueItem;
 import render.Renderer;
 import window.tree.ColoredTextString;
 
 public class Controller extends Applet{
 	private static final long serialVersionUID = 4458487765324323938L;
 
 	// the game always runs (except when it doesn't)
 	private volatile static boolean isRunning = true;
 	
 	private Renderer renderer;
 	private Physics physics;
 	
 	private long frames = 0;
 
 	private EntityList objectList;
 	
 	Runnable treeListener;
 	
 	private Canvas display_parent;
 	
 	public static void main(String[] args) throws Exception {
 		Applet app = new Controller();
 		app.init();
 	}
 
 	public void startApp() {
 		try {
 			startThreads();
 			loadLevel();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// Tell game loop to stop running, after which the LWJGL Display will 
 	// be destroyed. The main thread will wait for the Display.destroy().
 	private void stopLWJGL() {
 		isRunning = false;
 		try {
 			render_thread.join();
 			physics_thread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void start() {
 		
 	}
 
 	public void stop() {
 		
 	}
 	
 	// Applet Destroy method will remove the canvas, 
 	// before canvas is destroyed it will notify stopLWJGL()
 	// to stop the main game loop and to destroy the Display
 	public void destroy() {
 		remove(display_parent);
 		super.destroy();
 	}
 	
 	public void init(){
 		//Create a container to house the application
 		//for applets this is important otherwise the app won't embed
 		setLayout(new BorderLayout());
 		try {
 			display_parent = new Canvas() {
 				private static final long serialVersionUID = 1L;
 				public final void addNotify() {
 					super.addNotify();
 					startApp();
 				}
 				public final void removeNotify() {
 					stopLWJGL();
 					super.removeNotify();
 				}
 			};
 			display_parent.setSize(getWidth(),getHeight());
 			add(display_parent);
 			display_parent.setFocusable(true);
 			display_parent.requestFocus();
 			display_parent.setIgnoreRepaint(true);
 			setVisible(true);
 		} catch (Exception e) {
 			System.err.println(e);
 			throw new RuntimeException("Unable to create display");
 		}
 	}
 
 	public Controller(){	}
 
 	private void startThreads() throws Exception {
 		//Instantiate Physics first, as it depends on nothing
 		physics = new Physics();
 		
 		
 		//Next is the entity list, since it only depends on the physics
 		objectList = new EntityList(physics);
 		renderer = new Renderer(objectList, display_parent);
 		
 		
 		//Make a camera	
 		CollisionShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
 
 		Camera cam = new Camera(0.0, boxShape, false);
 		objectList.enqueue(cam, QueueItem.ADD);
 		
 		cam.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
 		objectList.parseQueue();
 		
 		physics_thread.start();
 		render_thread.start();
 	
 		readConfigFile();
 		
 		render_thread.interrupt();
 		
		cam.changeDefaultFocus(Config.getFullAssemblyFocus());
		Entity entitty = ((Camera)objectList.getItem(Camera.CAMERA_NAME)).getFocus();
 	}
 
 	// Create the Physics Listening thread
 	Thread physics_thread = new Thread() {
 		public void run() {
 			objectList.parseQueue();
 			while (isRunning) {
 				if(objectList != null && objectList.queueSize() > 0)
 					objectList.parseQueue();
 				else{
 					physics.clientUpdate();
 				}
 			}
 		}
 	};
 
 	// Create the vidya thread
 	Thread render_thread = new Thread() {
 		public void run() {
 			renderer.initGL();
 			try {
 				this.join();
 			} catch (InterruptedException e) {}
 			while (isRunning) {
 				if(Display.isCloseRequested())
 					isRunning=false;
 				renderer.draw();
 			}
 		}
 	};
 	
 	public long getFrames() { return frames; }
 	public void resetFrames() { frames = 0;	}
 	
 	public static void quit() { isRunning = false;	}
 	
 	public void loadLevel() throws Exception{
 		//Make a camera	
 		CollisionShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
 
 		Camera cam = new Camera(0.0, boxShape, false );
 		objectList.enqueue(cam, QueueItem.ADD);
 		
 		
 		//Load some stuff (I would only pick one of the following
 		//two methods if I were you)
 		//loadTestShapes(cam);
 		//pullModelFiles("resources/models/cathodes/minixgl");
 	}
 
 	@SuppressWarnings("unused")
 	private void loadTestShapes(Camera cam) {
 		physics.setGravity(new Vector3f(0,-10,0));
 		
 		//Legoman thing
 		Parser p = new XGL_Parser();
 		//Parser p = new Obj_Parser();
 		try{
 			//p.readFile("resources/models/misc/singlebox.obj");
 			p.readFile("resources/models/misc/singlebox.xgl");
 			//p.readFile("resources/models/misc/legoman.xgl");
 			//p.readFile("resources/models/misc/10010260.xgl");
 			//p.readFile("resources/models/misc/box2.xgl");
 			//p.readFile("resources/models/misc/cath.xgl");
 			//p.readFile("resources/models/cathodes/0335-CATHODE_ASSEMBLY.obj");
 		}catch(Exception e){
 			e.printStackTrace();
 			System.out.println("Model loading failed");
 		}
 		//p.createModel().saveXGL("resources/models/misc/singlebox.xgl");
 		BoxShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
 		Entity ent = new Entity(0.0f, boxShape, true);
 		ent.setModel(p.createModel());
 		ent.setPosition(new Vector3f(0.0f,0.0f,0.0f));
 		ent.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
 		//physics.reduceHull(ent);
 
 		objectList.enqueue(ent, QueueItem.ADD);
 		cam.setDistance(20.0f);
 		
 		cam.focusOn(ent);
 		
 		for(int i=10;i<100;i+=10) {
 			ent = new Entity(5.0f, boxShape, true);
 			ent.setModel(p.createModel());
 			ent.setPosition(new Vector3f(0.0f,(float)i,0.0f));
 			ent.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
 			//physics.reduceHull(ent);
 			//ent.setGravity(new Vector3f(0,-1,0));
 			objectList.enqueue(ent, QueueItem.ADD);
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private void pullModelFiles(String filename) throws Exception{
 		File dir = new File(filename);
 		File[] subFiles;
 		subFiles = dir.listFiles();
 		
 		for( File f: subFiles){
 			if(!f.isDirectory()){
 				//create model
 				int dotPos = f.getPath().lastIndexOf(".");
 		        String extension = f.getPath().substring(dotPos);
 		        if( extension.equals(".xgl") || extension.equals(".obj")){
 		        	Parser p;
 		        	XGL_Parser xparse = new XGL_Parser();
 		    		Obj_Parser oparse = new Obj_Parser();
 					if( extension.equals(".xgl")){
 						xparse.readFile(f.getPath());
 						p = xparse;
 					} else {
 						oparse.readFile(f.getPath());
 						p = oparse;
 					}
 					if( p != null){
 						//Make a cathode	
 						BoxShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
 						Entity ent = new Entity(1.0f, boxShape, false);	
 						ent.setModel(p.createModel());	
 						//ent.setPosition(new Vector3f(0.0f,0.0f,(float) Math.random()));
 						ent.setProperty("name", f.getPath().substring(0,dotPos-1));
 						objectList.enqueue(ent, QueueItem.ADD);
 						ent.setShouldDraw(true);
 					}
 		        }
 			}else{
 				pullModelFiles(f.getPath());
 			}
 		}
 	}
 	
 	/* Config file reading */
 	private void readConfigFile() throws Exception{
 		Document dom;
 		//URL url = new URL("resources/models/config.xml");
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		
 		//Create Dom Structure
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		dom = db.parse(this.getClass().getClassLoader().getResourceAsStream("resources/models/config_small.xml"));
 		
 		String configName;
 		window.tree.Model treeModel = new window.tree.Model();
 		HashMap<String, Vector3f> defaultPositions = new HashMap<String, Vector3f>();
 		String fullassemblyFocus = null;
 		String lineupFocus = null;
 		
 		ArrayList<Node> tagList;
 		
 		try {
 			//Now that we've got the file in DOM format, loop through elements
 			Element rootElement = dom.getDocumentElement();
 			
 			if( rootElement.getNodeName().equals("config")){
 				tagList = findChildrenByName(rootElement, "fullassembly");
 				if(tagList.size() == 1){
 					fullassemblyFocus = tagList.get(0).getTextContent();
 				}else{
 					throw new Exception("No default focus");
 				}
 				
 				tagList = findChildrenByName(rootElement, "lineup");
 				if(tagList.size() == 1){
 					lineupFocus = tagList.get(0).getTextContent();
 				}else{
 					throw new Exception("No default focus");
 				}
 				
 				tagList = findChildrenByName(rootElement, "name");
 				configName = tagList.get(0).getTextContent();
 				
 				tagList = findChildrenByName(rootElement, "item");
 				for(int i = 0; i < tagList.size(); i++){
 					//Create nodes for all of them
 					createItem((Element)tagList.get(i), treeModel, configName, defaultPositions);
 				}
 				objectList.parseQueue();
 				Config.addConfig(configName, new Vector3f(0,0,0), treeModel, defaultPositions, objectList.getItem(configName + "-" + fullassemblyFocus), objectList.getItem(configName + "-" + lineupFocus));
 			}else{
 				Exception e = new Exception();
 				e.initCause(new Throwable("Invalid config file"));
 				throw e;
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	private void createItem(Element ele, TreeTableNode parent, String configName, HashMap<String, Vector3f> defaultPositions) throws Exception {
 		String name;
 		String value;
 		ArrayList<Byte> color = null; 
 		String path;
 		boolean show;
 		Vector3f position;
 		boolean collidable;
 		String[] xyz;
 		ArrayList<Node> tagList = findChildrenByName(ele, "name");
 		name = tagList.get(0).getTextContent();
 		
 		tagList = findChildrenByName(ele, "value");
 		value = tagList.get(0).getTextContent();
 		
 		tagList = findChildrenByName(ele, "show");
 		if(tagList.size() == 0){
 			show = true;
 		}else if(tagList.get(0).getTextContent() == "true"){
 			show = true;
 		}else{
 			show = false;
 		}
 		
 		tagList = findChildrenByName(ele, "position");
 		if(tagList.size() > 0){
 			xyz = tagList.get(0).getTextContent().split(",");
 			position = new Vector3f(Float.parseFloat(xyz[0]),Float.parseFloat(xyz[1]),Float.parseFloat(xyz[2]));
 		}else{
 			position = new Vector3f(0,0,0);
 		}
 		
 		tagList = findChildrenByName(ele, "collidable");
 		if(tagList.size() > 0){
 			if(tagList.get(0).getTextContent().equals("true"))
 				collidable = true;
 			else if(tagList.get(0).getTextContent().equals("false"))
 				collidable = false;
 			else {
 				System.out.println("XML parse error: unsupported value" + tagList.get(0).getTextContent());
 				collidable=false;
 			}
 		}else{
 			collidable = true;
 		}
 		
 		tagList = findChildrenByName(ele, "path");
 		if( tagList.size() > 0){
 			path = tagList.get(0).getTextContent();
 			BoxShape boxShape = new BoxShape(new Vector3f(1, 1, 1));
 			
 			Entity ent = new Entity(1.0f, boxShape, collidable);
 			ent.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
 			if( !path.equals("") ){
 				//Make a cathode	
 				Parser p = new XGL_Parser();
 				p.readFile(path);
 				Model model = new Model();
 				model = p.createModel();
 				ent.setModel(model);
 				ent.setCollisionShape(model.createCollisionShape());
 			}
 			
 			ent.setPosition(position);
 			ent.setProperty("name", configName + "-" + name);
 			objectList.enqueue(ent, QueueItem.ADD);
 			
 			defaultPositions.put(name, position);
 			color = ent.getModel().getColor();
 			
 			ent.setProperty("resetydist", 4);
 		}
 		
 		if(show == true){
 			window.tree.Node item;
 			if( parent.getClass() == window.tree.Model.class ){
 				if(color != null){
 					item = ((window.tree.Model)parent).insert(new ColoredTextString(name, color.get(0), color.get(1), color.get(2)), "");
 				}else{
 					item = ((window.tree.Model)parent).insert(name, "");
 				}
 			}else{
 				if(color != null){
 					item = ((window.tree.Node)parent).insert(new ColoredTextString(name, color.get(0), color.get(1), color.get(2)),value);
 				}else{
 					item = ((window.tree.Node)parent).insert(name,value);
 				}
 			}
 			
 			tagList = findChildrenByName(ele, "item");
 			for(int i = 0; i < tagList.size(); i++){
 				//Create nodes for all of them
 				createItem((Element)tagList.get(i),item, configName, defaultPositions);
 			}
 		}
 	}
 
 	private ArrayList<Node> findChildrenByName(Node root, String name){
 		String[] names = new String[1];
 		names[0] = name;
 		return findChildrenByName(root,names);
 	}
 	
 	private ArrayList<Node> findChildrenByName(Node root, String[] names){
 		ArrayList<Node> list = new ArrayList<Node>();
 		for( int i = 0; i < names.length; i++){
 			Node e = root.getFirstChild();
 			while(e != null){
 				if(e.getNodeName().equals(names[i])){
 					list.add(e);
 				}
 				e = e.getNextSibling();
 			}
 		}
 		return list;
 	}
 }
