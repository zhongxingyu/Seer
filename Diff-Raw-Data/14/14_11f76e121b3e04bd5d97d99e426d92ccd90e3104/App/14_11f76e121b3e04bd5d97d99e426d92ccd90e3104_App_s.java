 package com.cefn.flunstellas;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.swing.*;
 
 import org.apache.commons.jxpath.JXPathContext;
 import org.apache.commons.lang3.ArrayUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig.Feature;
 import org.codehaus.jackson.type.TypeReference;
 
 import codeanticode.gsvideo.GSCapture;
 
 import com.cefn.flunstellas.impl.BasicAuthor;
 import com.cefn.flunstellas.impl.BasicGraph;
 import com.cefn.flunstellas.impl.ModelMediaBalloon;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 import chrriis.common.UIUtils;
 import chrriis.common.WebServer;
 import chrriis.dj.nativeswing.NativeSwing;
 import chrriis.dj.nativeswing.swtimpl.NativeInterface;
 import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;
 import chrriis.dj.nativeswing.swtimpl.components.JFileDialog.DialogType;
 import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
 import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
 import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
  
 /* Based on java swing example 'TopLevelDemo' */
 public class App {
 	
 	public static final Map<String,GSCapture> cameraMap = new HashMap<String,GSCapture>(); //lazy map containing named, live cameras
 
 	static final String NULL_CAMERA = "null";
 	static final String DEFAULT_CAMERA = "default";
 	static final String[] CAMERA_NAMES; //names of known valid cameras	
 	static{
     	String[] macroNames = new String[]{NULL_CAMERA,DEFAULT_CAMERA};
    		String os = System.getProperty("os.name");		
  	    if(os.startsWith("Mac")){
 	        //GSVideo.localGStreamerPath = "native_lib/macosx";
  	    	CAMERA_NAMES = ArrayUtils.addAll(macroNames, new String[]{
  	    			//not known what Mac cameras are called
  	    	});
 	    }
  	    else if(os.startsWith("Windows")){
  	    	CAMERA_NAMES = ArrayUtils.addAll(macroNames, GSCapture.list());
 	    }
  	    else if(os.startsWith("Linux")){
  	    	CAMERA_NAMES = ArrayUtils.addAll(macroNames, new String[]{
  	 	    		"/dev/video0",
  	 	    		"/dev/video1"	
  	    	});
  	    }
  	    else{
  	    	CAMERA_NAMES = new String[]{};
  	    }
 	}
 	static final String DEFAULT_CAMERA_NAME = (CAMERA_NAMES.length > 0 ? CAMERA_NAMES[0] : null); //null is no camera
 
 	final static String PATH = "";
 	final static String OBJECTS_PATH = PATH + "";
 	final static String MEDIA_PATH = PATH + "";
 
 	final Dimension frameSize;
 	final Dimension menuSize; 	
 	final Dimension viewerPreferredSize;
 	final Dimension editorPreferredSize;
 
 	final static float viewerShare = 0.66f;
 	final static float editorShare = 0.33f;
 	
 	final float cylinderHeightRatio = 0.25f; //the height ratio of the balloons to the target width
 	
 	final JFrame frame;
 	final JMenuBar menuBar;
 	
 	final ProcessingViewer viewer;
 	final Editor editor;
 	final EditorListener editorListener;
 
 	Graph graph;
 	
 	public App(String... args){
 		
 		//work out various dimensions
 		frameSize = new Dimension(1024, 768);
 		menuSize = new Dimension(frameSize.width, 20); 
         int contentHeight = frameSize.height - menuSize.height ;
         viewerPreferredSize = new Dimension((int)(frameSize.width * viewerShare), contentHeight);
         editorPreferredSize = new Dimension((int)(frameSize.width * editorShare), contentHeight);
 		
         //Create and set up the window, menu and content panels.
         //create a browser panel for interactive editing of properties
 		//create a Processing panel for viewing the 3d world
         frame = new JFrame("Constellation Editor");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         //create a menu with basic save and load options        
         menuBar = new JMenuBar();
         JMenu menu = new JMenu("File");
         menuBar.add(menu);
         JMenuItem open = new JMenuItem("Open");
         open.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileDialog fileDialog = new JFileDialog();
 				fileDialog.setDialogType(DialogType.OPEN_DIALOG_TYPE);
 				fileDialog.show(frame);
 				String path = fileDialog.getParentDirectory();
 				String name = fileDialog.getSelectedFileName();
 				if(path != null && name != null){
 					Graph loadedGraph = loadGraph(new File(path, name));
 					setGraph(loadedGraph);
 				}
 			}
 		});
         menu.add(open);
         JMenuItem save = new JMenuItem("Save");
         save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileDialog fileDialog = new JFileDialog();
 				fileDialog.setDialogType(DialogType.SAVE_DIALOG_TYPE);
 				fileDialog.show(frame);
 				String path = fileDialog.getParentDirectory();
 				String name = fileDialog.getSelectedFileName();
 				if(path != null && name != null){
 					saveGraph(new File(path, name));					
 				}
 			}
 		});
         menu.add(save);
         frame.setJMenuBar(menuBar);
         
         editor = new Editor();
 		viewer = new ProcessingViewer(viewerPreferredSize);
 		
         //wire up to receive notifications of changes to properties
 		editorListener = new EditorListener();
 		editor.addWebBrowserListener(editorListener);
 		
 	}
 	
 	public ProcessingViewer getViewer() {
 		return viewer;
 	}
 
 	public boolean saveGraph(File saveFile){
 		try{
 			ObjectMapper mapper = new ObjectMapper();
 			mapper.configure(Feature.INDENT_OUTPUT, true);
 			mapper.writeValue(saveFile, graph);			
 			return true;
 		}
 		catch(Exception e){
 			try{
 				throw (RuntimeException) e;
 			}
 			catch(ClassCastException cce){
 				System.out.println("Problem: saving to file" + saveFile + " threw " + e);
 				return false;
 			}
 		}
 	}
 	
 	public Graph loadGraph(File loadFile){
 		try{
 			ObjectMapper mapper = new ObjectMapper();
 			return mapper.readValue(loadFile, BasicGraph.class);
 		}
 		catch(Exception e){
 			try{
 				throw (RuntimeException) e;
 			}
 			catch(ClassCastException cce){
 				System.out.println("Problem: loading from file" + loadFile + " threw " + e);
 				return null;
 			}
 		}
 	}
 
 	public void resetGraphView(Graph graph){
 				
 		//paths to graph properties
 		String[] graphPaths = new String[]{
 			"title",
 			"description",
 			"author.title",
 			"heightLabel",
 			"radiusLabel",
 			"angleLabel"
 		};		
 		//paths to viewer properties
 		String[] viewerPaths = new String[]{
 			"XAngle","YOffset","ZOffset"
 		};
 		
 		//reset viewer
 		runInBrowser("blankGraph()");
 				
 		//send all properties from graph
 		for(String graphPath:graphPaths){
 			graphPath = "graph." + graphPath;
 			//get value
 			Object value = queryModelByDotNotation(App.this,graphPath);
 			//pass over to browser control
 			populateControlByDotNotation(graphPath , value != null ? value.toString(): null);
 		}
 		
 		//send all properties from viewer
 		for(String viewerPath:viewerPaths){
 			viewerPath = "viewer." + viewerPath;
 			Object value = queryModelByDotNotation(App.this,viewerPath);
 			populateControlByDotNotation(viewerPath , value != null ? value.toString(): null );
 		}
 
 		//send information from each balloon
 		for(int balloonIndex = 0; balloonIndex < graph.getBalloons().size(); balloonIndex++){
 			runInBrowser("addBalloon()");
 			sendBalloonFields(balloonIndex);
 		}
 		
 		runInBrowser("resetLayout()");
 		
 	}
 	
 	public void addBalloon(){
 		Balloon balloon = new ModelMediaBalloon();
 		int balloonPosition = graph.getBalloons().size();
 		graph.getBalloons().add(balloon);
 		//runInBrowserAndWait("addBalloon()");
 		runInBrowser("addBalloon()");
 		sendBalloonFields(balloonPosition);
 		//runInBrowserAndWait("resetLayout()");
 		runInBrowser("resetLayout()");
 	}
 	
 	public String calculateBalloonPrefix(int balloonIndex){
 		return "graph.balloons[" + (balloonIndex + 1) + "]."; //numbered like xpath
 	}
 	
 	public void sendBalloonFields(int balloonIndex){
 		String[] balloonPaths = new String[]{
 				"title",
 				"description",
 				"center.radius", "center.height", "center.angle",
 				"rotation.x", "rotation.y", "rotation.z",
 				"scale"
 		};		
 		String balloonPrefix = calculateBalloonPrefix(balloonIndex);
 		for(String balloonPath:balloonPaths){
 			balloonPath = balloonPrefix + balloonPath;
 			Object value = queryModelByDotNotation(App.this, balloonPath);
 			populateControlByDotNotation(balloonPath , value != null ? value.toString(): null );
 		}
 	}
 	
 	public void chooseFileForDotNotation(String notation, String title){
 		JFileDialog fileDialog = new JFileDialog();
 		fileDialog.setDialogType(DialogType.OPEN_DIALOG_TYPE);
 		fileDialog.setTitle(title);
 		fileDialog.show(frame);
 		String path = fileDialog.getParentDirectory();
 		String name = fileDialog.getSelectedFileName();
 		if(path != null && name != null){
 			String value = null;
 			try{
 				value = new File(path, name).getCanonicalPath();
 			}
 			catch(IOException ioe){
 			}
 			populateModelByDotNotation(App.this, notation, value);
 			populateControlByDotNotation(notation, value);
 		}		
 
 	}	
 	
 	public void runInBrowser(final String javascript){
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run() {
 				editor.executeJavascript(javascript);				
 			}
 		});
 	}
 
 	public Object runInBrowserAndWait(final String javascript) {
 		final AtomicReference<Object> resultRef = new AtomicReference<Object>();
 		Runnable triggerScript = new Runnable(){
 			public void run() {
 				resultRef.set(editor.executeJavascriptWithResult(javascript));				
 			}
 		};
 		if(SwingUtilities.isEventDispatchThread()){
 			triggerScript.run();
 		}
 		else{
 			try {
 				SwingUtilities.invokeAndWait(triggerScript);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			}			
 		}
 		return resultRef.get();
 	}
 
 	public void populateControlByDotNotation(String path, String value){
 		if(value == null) value = "";
 		//runInBrowserAndWait("setPath('" + path + "', '" + escapeForJavascript(value) +"')");	
 		runInBrowser("setPath('" + path + "', '" + escapeForJavascript(value) +"')");	
 	}
 	
     public void populateModelByDotNotation(Object root, String member, String value){
 		JXPathContext pathContext = JXPathContext.newContext(root);
     	member = member.replaceAll("\\.", "/"); //switch to slash-separated JXPath from dot notation
     	pathContext.setValue(member, value);	
     }
 
     public Object queryModelByDotNotation(Object root, String member){
 		JXPathContext pathContext = JXPathContext.newContext(root);
     	member = member.replaceAll("\\.", "/"); //handly switch to JXPath
     	return pathContext.getValue(member);	
     }
 
 	public Graph getGraph(){
 		return graph;
 	}
 	
 	public void setGraph(Graph graph){
 		this.graph = graph;
 		resetGraphView(graph);
 	}
 			
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
     void run(){
     	        
 		//set sizes for things
         menuBar.setPreferredSize(menuSize);
         viewer.setPreferredSize(new Dimension(viewerPreferredSize.width, viewerPreferredSize.height));
         editor.setPreferredSize(new Dimension(editorPreferredSize.width, editorPreferredSize.height));
         
         //create window heirarchy
         frame.setJMenuBar(menuBar);
         frame.getContentPane().add(viewer, BorderLayout.CENTER);
         frame.getContentPane().add(editor, BorderLayout.EAST);
 
         //Layout and display the window.
     	frame.setVisible(true);
         frame.pack();
                 
         Runnable initTasks = new Runnable(){
         	public void run() {
                 //load the 3d viewer in the Processing pane
                 viewer.init();
                 //establish the URL for serving html and visit the editor
                 WebServer server = WebServer.getDefaultWebServer();
                 String editorUrl = server.getClassPathResourceURL(App.class.getName(), "html/editor.html");
                 editor.navigate(editorUrl);
         	}
         };
 
         Runnable processingRedraw = new Runnable(){
         	public void run() {
         		viewer.redraw();
         		SwingUtilities.invokeLater(this);
         	}
         };
 
         //kick off the events within threaded windowing toolkits
         SwingUtilities.invokeLater(initTasks);
         SwingUtilities.invokeLater(processingRedraw);
         
         NativeInterface.runEventPump(); //this possibly blocks?
         
     }
     
     public void start(){
     	
     	if(graph == null){
         	Author author = new BasicAuthor();
         	Graph graph = new BasicGraph(author, "", "");
         	setGraph(graph);    		
     	}
     	else{
     		resetGraphView(graph);
     	}
     	
     	for(String cameraName:CAMERA_NAMES){
         	runInBrowser("addCameraName('" + escapeForJavascript(cameraName) + "')");	
     	}
 
     }
     
     public String escapeForJavascript(String value){
     	return value.replaceAll("'", "\\'");
     }
 
     public class Editor extends JWebBrowser{
     	
     	protected Editor() {
     		super();
             this.setBarsVisible(false);	            
 		}
     	
     }
     
     public class EditorListener extends WebBrowserAdapter{
     	    	
     	public Map<String,String> unmarshallData(final WebBrowserCommandEvent e){
     		if(e.getParameters() != null && e.getParameters().length > 0){ 			
     			String data = (String)e.getParameters()[0];
     			if(data != null){
             		try{
         				ObjectMapper mapper = new ObjectMapper();
         				return mapper.readValue(data,new TypeReference<HashMap<String,String>>(){});
         			}
         			catch(Exception jpe){
         				try{
         					throw (RuntimeException)jpe;
         				}
         				catch(ClassCastException cce){
         					System.out.println("Could not parse:" + data + "\n" + jpe);
         				}
         			}    				
     			}
     		}
 			return null;
     	}
     	    	
 		@Override
 		public void commandReceived(final WebBrowserCommandEvent e) {
 			if(e.getCommand().equals("onload")){
 				start();
 			}
 			else if(e.getCommand().equals("addBalloon")){
 				addBalloon();
 			}
 			else{
 				Map<String,String> dataMap = unmarshallData(e);
 				if(dataMap != null){
 					String path = dataMap.get("path");			
 					if(e.getCommand().equals("fieldChanged")){
 						String value = dataMap.get("value");
 						if(path != null){
 							populateModelByDotNotation(App.this,path,value);
 							//update controls in editor
 							populateControlByDotNotation(path, value);
 						}
 					}
 					else if(e.getCommand().equals("chooseFile")){
 						String dialog = dataMap.get("dialog");
 						chooseFileForDotNotation(path, dialog);
 					}						
 				}				
 			}
 		}
     }
         
     public class ProcessingViewer extends PApplet implements Viewer{
     	
     	public float xAngle = -15; //the cylinder is angled forward this amount
     	public float yAngle = -90; //the floor is rotated by this angle
     	public float yOffset = 0f; //the cylinder is lifted by this amount
     	public float zOffset = 0f; //the cylinder is pushed back by this amount
 
     	float rotX, rotY;
     	private Dimension size;
     	String cameraName;
     	GSCapture camera;
     	boolean cameraFlipped;
     	
     	PFont labelFont = null;
     	
     	protected ProcessingViewer(Dimension size){
     		super();
     		this.size = size;
     	}
     	
 		public float getXAngle() {
 			return xAngle;
 		}
 
 		public float getYAngle() {
 			return yAngle;
 		}
 
 		public float getYOffset() {
 			return yOffset;
 		}
 
 		public float getZOffset() {
 			return zOffset;
 		}
 
 		public void setXAngle(float value) {
 			xAngle = value;
 		}
 
 		public void setYAngle(float value) {
 			yAngle = value;
 		}
 
 		public void setYOffset(float value) {
 			yOffset = value;
 		}
 
 		public void setZOffset(float value) {
 			zOffset = value;
 		}
 
 		
 		
     	@Override
     	public void setup() {
       	  size(size.width, size.height, P3D);
 
   		  noStroke();
   		  noLoop();
   		  
   		  //trigger load of default camera configuration
   		  setCameraName(DEFAULT_CAMERA_NAME); 
   		    		  
     	}
     	
     	public void setCameraName(String newName){
     		if(newName == null || newName.equals(NULL_CAMERA)){
     			newName = NULL_CAMERA;
     		}
 			if(newName != null && newName.equals("")){
 				newName = DEFAULT_CAMERA;
 			}
     		this.cameraName = newName;
     		if(camera != null){ //stop a camera if it's running
     			camera.stop();
     		}
     		if(newName.equals(NULL_CAMERA)){ //null value - no camera
     			cameraName = NULL_CAMERA;
     			camera = null;
     		}
     		else{
     			//lazy create named camera
     			camera = cameraMap.get(newName);
    			if(camera != null){  //resume if was paused
    			//	camera.start();
    			}
    			else{//load if it doesn't exist already
     				if(cameraName.equals(DEFAULT_CAMERA)){
    	        		camera = new GSCapture(this, 320, 240, 15);
     				}
     				else{
    	        		camera = new GSCapture(this, 320, 240, newName, 15);    			    					
     				}
     				cameraMap.put(newName, camera);
     			}
     		}
     	}
     	
     	public void captureEvent(GSCapture c) {
     		c.read();
     		cameraFlipped = false;
     	}
     	
     	public void flipVideo(){
     		if(!cameraFlipped){
         		camera.loadPixels();
         		int halfWidth = camera.width / 2;
         		for(int y = 0; y < camera.height; y++){
         			int leftboundary = y*camera.width;
         			int rightboundary = leftboundary+camera.width-1;
         			for(int x = 0; x < halfWidth; x++){
        				 	int left = leftboundary+x;
        				 	int right = rightboundary-x;
         				int tmpPixel = camera.pixels[left];
         				camera.pixels[left]=camera.pixels[right];
         				camera.pixels[right]=tmpPixel;
         			}
         		}
         		camera.updatePixels();
         		cameraFlipped = true;
     		}
     	}
     	
     	@Override
     	public void draw() {
     		
     		//size of current viewport
     		Dimension viewerActualSize = viewer.getSize();
 
     		//smallest preferred dimension of viewer
     		float minPreferredDim = Math.min(viewerPreferredSize.width,viewerPreferredSize.height);
     		//smallest actual dimension of viewer
     		float minActualDim = Math.min(viewerActualSize.width,viewerActualSize.height);
     		
     		//force rendering with a vanishing point and configure view
     		perspective();
     		background(0);  
     		lights();
 
     		//paint the video background relative to absolute coordinate system
     		if(camera != null){
         		pushMatrix();
         		//center on middle of screen
         		translate(viewerActualSize.width * 0.5f,viewerActualSize.height * 0.5f);
         		float videoScale = max(
         				((float)viewerActualSize.height)/((float)camera.height),
         				((float)viewerActualSize.width)/((float)camera.width)
         		);
         		translate(0, 0, -minPreferredDim); //push to the back of the screen
         		scale(videoScale * 2.0f); //scale video to fill screen
         		translate(-camera.width * 0.5f,-camera.height * 0.5f); //center video paint operation
         		//mirror the image then draw it
         		flipVideo();
         		image(camera,0,0);
         		popMatrix();    			
     		}
     		
     		//paint the graph relative to perspective, transformed, scaled and rotated coordinate system    		 
     		pushMatrix();
     		//shift center of world to center of current viewer
     		translate(viewerActualSize.width * 0.5f, viewerActualSize.height * 0.5f);
     		//scale graph according to preferred dimensions
 			scale(minPreferredDim);
 
     		//zoom to fit depending on actual versus preferred dimensions
     		float horizontalTargetScale = ((float)viewerActualSize.width) / ((float)viewerPreferredSize.width);
     		float verticalTargetScale = ((float)viewerActualSize.width) / ((float)viewerPreferredSize.width);
     		float zoomScale = Math.min(horizontalTargetScale,verticalTargetScale);
     		scale(zoomScale);
 
     		//move backwards and upwards
     		translate(0,yOffset,-zOffset);
     		//angle floor forward
     		rotateX(PApplet.PI / 180f * xAngle);
     		//rotate floor 
     		rotateY(PApplet.PI / 180f * yAngle);
     		
     		if(graph != null){
     			drawGraph(graph, this);
     		}
     		
     		popMatrix();
     		  
     		  
     	}
     	
     	public void drawGraph(Graph graph, PApplet applet) {
 			
     		//draw circles
     		applet.pushMatrix();
     		applet.rotateX(PApplet.PI * 0.5f);
     		applet.stroke(255);
     		applet.noFill();
     		for(float i = 10; i --> 0;){
     			applet.ellipse(0, 0, i/10, i/10);
     		}
     		applet.noStroke();
     		applet.popMatrix();
     		
     		//populate view
     		Balloon[] balloonsCopy = graph.getBalloons().toArray(new Balloon[0]);
     		for(Balloon balloon: balloonsCopy){
     			Polar center = balloon.getCenter();
     			applet.pushMatrix();
     			applet.rotateY(center.getAngle() * PApplet.PI / 180f);
     			applet.translate(-center.getRadius(), -center.getHeight(), 0);
     			
     			//draw string
     			applet.stroke(applet.color(0,0,255));
     			applet.line(0, 0, 0, 0, center.getHeight(),0);
     			applet.noStroke();
 
     			//draw balloon
     			applet.pushMatrix();
     			applet.rotateX(PApplet.PI / 180 * balloon.getRotation().getX());
     			applet.rotateY(PApplet.PI / 180 * balloon.getRotation().getY());
     			applet.rotateZ(PApplet.PI / 180 * balloon.getRotation().getZ());
     			applet.scale(balloon.getScale());
     			balloon.draw(applet);
     			applet.popMatrix();
     			
     			//draw label relative to balloon as origin
     			//compensating for viewer spin
     			//and balloon scale and spin
     			if(balloon.getShowLabel() && balloon.getTitle() != null){
     				if(labelFont == null){
     					labelFont = applet.createFont("Arial", 32);
     				}
     				applet.pushMatrix();
     				applet.translate(0, -0.03f * balloon.getScale(),0);
     				applet.textFont(labelFont);
     				applet.textAlign(PApplet.CENTER);
     				applet.rotateY(- PApplet.PI / 180 * (getYAngle() + balloon.getCenter().getAngle()));
     				applet.scale(0.001f);
     				applet.text(balloon.getTitle(), 0,0,0); //draw text above
     				applet.popMatrix();
     			}
 
     			
     			
     			applet.popMatrix();
     		}
     		
     	}
 
     	
     	public void mouseDragged(){
     		  rotX += (pmouseY - mouseY) * 0.01;
     		  rotY += (mouseX - pmouseX) * 0.01;
     	}
     	
     }
     
      
     public static void main(String[] args) {
     	
     	//initialise Native Swing support
     	UIUtils.setPreferredLookAndFeel();
     	NativeSwing.initialize();
     	NativeInterface.open();
     	
     	App app = new App(args);
     	    	    	
     	app.run();
     	
     }
     
 }
