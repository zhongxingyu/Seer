 /*
  * Modified version of the TUIO processing library - part of the reacTIVision
  * project http://reactivision.sourceforge.net/
  * 
  * Copyright (c) 2005-2009 Martin Kaltenbrunner <mkalten@iua.upf.edu>
  * 
  * This version Copyright (c) 2011 Erik Paluka, Christopher Collins - University
  * of Ontario Institute of Technology Mark Hancock - University of Waterloo
  * contact: christopher.collins@uoit.ca
  * 
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License Version 3 as published by the
  * Free Software Foundation.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package vialab.SMT;
 
 import java.awt.Point;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PImage;
 
 //import android.view.*;
 
 import TUIO.*;
 
 /**
  * The Core class of the SMT library, it provides the TUIO/Touch Processing client
  * implementation, specifies the back-end source of Touches via TouchSource, and
  * the SMT library cannot be used without it. Use this class to
  * add/remove zones from the sketch/application.
  * 
  * @author Erik Paluka
  * @author Zach Cook
  * @version 1.0
  */
 public class SMT {
 	
 	private static class MainZone extends Zone{
 		MainZone(int x, int y, int w, int h){
 			super(x, y, w, h);
 		}
 		
 		protected void drawImpl() {
 		}
 
 		protected void touchImpl() {
 		}
 	}
 	
 	static Zone sketch;
 
 	static float box2dScale = 0.1f;
 
 	static World world;
 
 	private static int MAX_PATH_LENGTH = 50;
 
 	protected static LinkedList<Process> tuioServerList = new LinkedList<Process>();
 
 	/** Processing PApplet */
 	protected static PApplet parent;
 
 	/** Gesture Handler */
 	// private static GestureHandler handler;
 
 	/** Tuio Client that listens for Tuio Messages via port 3333 UDP */
 	protected static LinkedList<TuioClient> tuioClientList = new LinkedList<TuioClient>();
 
 	/** Flag for drawing touch points */
 	private static TouchDraw drawTouchPoints = TouchDraw.SMOOTH;
 
 	/** Matrix used to test if the zone has gone off the screen */
 	// private PMatrix3D mTest = new PMatrix3D();
 
 	protected static SMTZonePicker picker;
 
 	protected static SMTTuioListener listener;
 
 	protected static SMTTouchManager manager;
 
 	protected static Method touch;
 
 	protected static Boolean warnUnimplemented;
 
 	/**
 	 * This controls whether we give a warning for uncalled methods which use
 	 * one of the reserved method prefixes (draw,pickDraw,touch,etc and any that
 	 * have been added by calls to SMTUtilities.getZoneMethod() with a unique
 	 * prefix).
 	 * <P>
 	 * Default state is on.
 	 */
 	public static boolean warnUncalledMethods = true;
 
 	/**
 	 * The renderer to use as a default for zones, can be P3D, P2D, OPENGL, etc,
 	 * upon SMT initialization this is set to the same as the PApplet's
 	 * renderer
 	 */
 	public static String defaultRenderer;
 
 	/** TUIO adapter depending on which TouchSource is used */
 	static AndroidToTUIO att = null;
 	static MouseToTUIO mtt = null;
 
 	protected static LinkedList<BufferedReader> tuioServerErrList = new LinkedList<BufferedReader>();
 
 	protected static LinkedList<BufferedReader> tuioServerOutList = new LinkedList<BufferedReader>();
 
 	protected static LinkedList<BufferedWriter> tuioServerInList = new LinkedList<BufferedWriter>();
 
 	static Body groundBody;
 
 	static boolean fastPicking = true;
 
 	static Map<Integer, TouchSource> deviceMap = Collections
 			.synchronizedMap(new LinkedHashMap<Integer, TouchSource>());
 
 	static int mainListenerPort;
 
 	protected static boolean inShutdown = false;
 	
 	public static boolean debug = false;
 
 	/**
 	 * Prevent SMT instantiation with protected constructor
 	 */
 	protected SMT() {
 	}
 
 	public static void setFastPicking(boolean fastPicking) {
 		SMT.fastPicking = fastPicking;
 	}
 
 	/**
 	 * Calling this function overrides the default behavior of showing select
 	 * unimplemented based on defaults for each zone specified in loadMethods()
 	 * 
 	 * @param warn
 	 *            whether to warn when there are unimplemented methods for zones
 	 */
 	public static void setWarnUnimplemented(boolean warn) {
 		SMT.warnUnimplemented = warn;
 	}
 
 	/*
 	 * Initializes the SMT, begins listening to a TUIO source on the
 	 * default port of 3333
 	 */
 
 	/**
 	 * 
 	 * Allows you to select the TouchSource backend from which to get
 	 * multi-touch events from
 	 * 
 	 * @param parent
 	 *            - PApplet: The Processing PApplet, usually just 'this' when
 	 *            using the Processing IDE
 	 * 
 	 * @param port
 	 *            - int: The port to listen on
 	 * 
 	 * @param source
 	 *            -int: The source of touch events to listen to. One of:
 	 *            TouchSource.MOUSE, TouchSource.TUIO_DEVICE,
 	 *            TouchSource.ANDROID, TouchSource.WM_TOUCH, TouchSource.SMART
 	 */
 	public static void init(PApplet parent) {
 		init(parent, 3333);
 	}
 
 	/**
 	 * Allows you to select the TouchSource backend from which to get
 	 * multi-touch events from
 	 * 
 	 * @param parent
 	 *            PApplet - The Processing PApplet, usually just 'this' when
 	 *            using the Processing IDE
 	 * @param port
 	 *            int - The port to listen on
 	 */
 	public static void init(PApplet parent, int port) {
 		init(parent, port, TouchSource.TUIO_DEVICE);
 	}
 
 	/**
 	 * Allows you to select the TouchSource backend from which to get
 	 * multi-touch events from
 	 * 
 	 * @param parent
 	 *            PApplet - The Processing PApplet, usually just 'this' when
 	 *            using the Processing IDE
 	 * @param source
 	 *            enum TouchSource - The source of touch events to listen to.
 	 *            One of: TouchSource.MOUSE, TouchSource.TUIO_DEVICE,
 	 *            TouchSource.ANDROID, TouchSource.WM_TOUCH, TouchSource.SMART
 	 */
 	public static void init(PApplet parent, TouchSource source) {
 		init(parent, 3333, source);
 	}
 
 	/**
 	 * Allows you to select the TouchSource backend from which to get
 	 * multi-touch events from
 	 * 
 	 * @param parent
 	 *            PApplet - The Processing PApplet, usually just 'this' when
 	 *            using the Processing IDE
 	 * @param source
 	 *            enum TouchSource - The source of touch events to listen to.
 	 *            One of: TouchSource.MOUSE, TouchSource.TUIO_DEVICE,
 	 *            TouchSource.ANDROID, TouchSource.WM_TOUCH, TouchSource.SMART
 	 * @param extraClasses
 	 *            Class<?> - Classes that should be checked for method
 	 *            definitions similar to PApplet for drawZoneName(), etc, but
 	 *            PApplet takes precedence.
 	 */
 	public static void init(PApplet parent, TouchSource source, Class<?>... extraClasses) {
 		init(parent, 3333, source, extraClasses);
 	}
 
 	/**
 	 * Allows you to select the TouchSource backend from which to get
 	 * multi-touch events from
 	 * 
 	 * @param parent
 	 *            PApplet - The Processing PApplet, usually just 'this' when
 	 *            using the Processing IDE
 	 * 
 	 * @param port
 	 *            int - The port to listen on
 	 * 
 	 * @param source
 	 *            int - The source of touch events to listen to. One of:
 	 *            TouchSource.MOUSE, TouchSource.TUIO_DEVICE,
 	 *            TouchSource.ANDROID, TouchSource.WM_TOUCH, TouchSource.SMART
 	 */
 	private static void init(final PApplet parent, int port, TouchSource source,
 			Class<?>... extraClasses) {
 		if (parent == null) {
 			System.err
 					.println("Null parent PApplet, pass 'this' to SMT.init() instead of null");
 			return;
 		}
 		
 		SMT.parent = parent;
 		
 		SMTUtilities.loadMethods(parent.getClass());
 
 		// As of now the toolkit only supports OpenGL
		if (!parent.g.is3D()) {
 			System.out
					.println("SMT only supports using OpenGL 3D renderers, please use either OPENGL, P3D, in the size function e.g  size(displayWidth, displayHeight, P3D);");
 		}
 
 		if (System.getProperty("os.name").equals("Mac OS X")) {
 			SMT.fastPicking = false;
 			System.out
 					.println("Defaulting to slow picking on OS X, if calling SMT.setFastPicking(true); after SMT.init() increases performance and doesn't cause bugs, please contact SMT's developers.");
 		}
 
 		touch = SMTUtilities.getPMethod(parent, "touch");
 		
 		SMT.sketch = new MainZone(0,0,parent.width,parent.height);
 
 		// set Zone.applet so that it is consistently set at this time, not
 		// after a zone is constructed
 		Zone.applet = parent;
 
 		// parent.registerMethod("dispose", this);
 		parent.registerMethod("draw", new SMT());
 		parent.registerMethod("pre", new SMT());
 		// handler = new GestureHandler();
 
 		picker = new SMTZonePicker();
 
 		defaultRenderer = parent.g.getClass().getName();
 
 		listener = new SMTTuioListener();
 		manager = new SMTTouchManager(listener, picker);
 
 		TuioClient c = new TuioClient(port);
 		c.connect();
 		while (!c.isConnected()) {
 			c = new TuioClient(++port);
 			c.connect();
 		}
 		c.addTuioListener(listener);
 		tuioClientList.add(c);
 
 		if(debug){
 			System.out.println("TuioClient listening on port: " + port);
 		}
 		mainListenerPort = port;
 
 		switch (source) {
 		case ANDROID:
 			// this still uses the old method, should be re-implemented without
 			// the socket
 			att = new AndroidToTUIO(parent.width, parent.height, port);
 			deviceMap.put(port, source);
 			// parent.registerMethod("touchEvent", att); //when Processing
 			// supports this
 			break;
 		case MOUSE:
 			// this still uses the old method, should be re-implemented without
 			// the socket
 			mtt = new MouseToTUIO(parent.width, parent.height, port);
 			deviceMap.put(port, source);
 			parent.registerMethod("mouseEvent", mtt);
 			break;
 		case TUIO_DEVICE:
 			break;
 		case WM_TOUCH:
 			if (System.getProperty("os.arch").equals("x86")) {
 				SMT.runWinTouchTuioServer(false, "127.0.0.1", port);
 			}
 			else {
 				SMT.runWinTouchTuioServer(true, "127.0.0.1", port);
 			}
 			deviceMap.put(port, source);
 			break;
 		case SMART:
 			SMT.runSmart2TuioServer();
 			deviceMap.put(port, source);
 			break;
 		case LEAP:
 			SMT.runLeapTuioServer(port);
 			deviceMap.put(port, source);
 			break;
 		case MULTIPLE:
 			// ANDROID
 			if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) {
 				att = new AndroidToTUIO(parent.width, parent.height, port);
 				deviceMap.put(port, TouchSource.ANDROID);
 				// parent.registerMethod("touchEvent", att); //when Processing
 				// supports this
 				break;
 			}
 			else {
 				// TUIO_DEVICE:
 				// covered already by c
 				deviceMap.put(port, TouchSource.TUIO_DEVICE);
 
 				// WM_TOUCH:
 				TuioClient c2 = new TuioClient(++port);
 				c2.connect();
 				while (!c2.isConnected()) {
 					c2 = new TuioClient(++port);
 					c2.connect();
 				}
 				c2.addTuioListener(new SMTProxyTuioListener(port, listener));
 				if(debug){
 					System.out.println("TuioClient listening on port: " + port);
 				}
 				if (System.getProperty("os.arch").equals("x86")) {
 					SMT.runWinTouchTuioServer(false, "127.0.0.1", port);
 					deviceMap.put(port, TouchSource.WM_TOUCH);
 				}
 				else {
 					SMT.runWinTouchTuioServer(true, "127.0.0.1", port);
 					deviceMap.put(port, TouchSource.WM_TOUCH);
 				}
 
 				// LEAP:
 				TuioClient c3 = new TuioClient(++port);
 				c3.connect();
 				while (!c3.isConnected()) {
 					c3 = new TuioClient(++port);
 					c3.connect();
 				}
 				c3.addTuioListener(new SMTProxyTuioListener(port, listener));
 				if(debug){
 					System.out.println("TuioClient listening on port: " + port);
 				}
 				SMT.runLeapTuioServer(port);
 				deviceMap.put(port, TouchSource.LEAP);
 
 				// MOUSE:
 				TuioClient c4 = new TuioClient(++port);
 				c4.connect();
 				while (!c4.isConnected()) {
 					c4 = new TuioClient(++port);
 					c4.connect();
 				}
 				c4.addTuioListener(new SMTProxyTuioListener(port, listener));
 				if(debug){
 					System.out.println("TuioClient listening on port: " + port);
 				}
 
 				// this still uses the old method, should be re-implemented
 				// without
 				// the socket
 				mtt = new MouseToTUIO(parent.width, parent.height, port);
 				deviceMap.put(port, TouchSource.MOUSE);
 				parent.registerMethod("mouseEvent", mtt);
 
 				tuioClientList.add(c2);
 				tuioClientList.add(c3);
 				tuioClientList.add(c4);
 			}
 			break;
 		default:
 			break;
 		}
 
 		parent.hint(PConstants.DISABLE_OPTIMIZED_STROKE);
 
 		/**
 		 * Disconnects the TuioClient when the PApplet is stopped. Shuts down
 		 * any threads, disconnect from the net, unload memory, etc.
 		 */
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 			public void run() {
 				SMT.inShutdown  = true;
 				if (warnUncalledMethods) {
 					SMTUtilities.warnUncalledMethods(parent);
 				}
 				for (TuioClient t : tuioClientList) {
 					if (t.isConnected()) {
 						t.disconnect();
 					}
 				}
 				for (BufferedWriter w : tuioServerInList) {
 					try {
 						w.newLine();
 						w.flush();
 					}
 					catch (IOException e) {}
 				}
 				
 				try {
 					Thread.sleep(500);
 				}
 				catch (InterruptedException e) {}
 
 				for (Process tuioServer : tuioServerList) {
 					if (tuioServer != null) {
 						tuioServer.destroy();
 					}
 				}
 			}
 		}));
 
 		world = new World(new Vec2(0.0f, 0.0f), true);
 		// top
 		groundBody = createStaticBox(0, -10.0f, parent.width, 10.f);
 		// bottom
 		createStaticBox(0, parent.height + 10.0f, parent.width, 10.f);
 		// left
 		createStaticBox(-10.0f, 0, 10.0f, parent.height);
 		// right
 		createStaticBox(parent.width + 10.0f, 0, 10.0f, parent.height);
 	}
 
 	/**
 	 * This creates a static box to interact with the physics engine, collisions
 	 * will occur with Zones that have physics == true, keeping them on the
 	 * screen
 	 * 
 	 * @param x
 	 *            X-position
 	 * @param y
 	 *            Y-position
 	 * @param w
 	 *            Width
 	 * @param h
 	 *            Height
 	 */
 	public static Body createStaticBox(float x, float y, float w, float h) {
 		BodyDef boxDef = new BodyDef();
 		boxDef.position.set(SMT.box2dScale * (x + w / 2), SMT.box2dScale
 				* (parent.height - (y + h / 2)));
 		Body box = world.createBody(boxDef);
 		PolygonShape boxShape = new PolygonShape();
 		boxShape.setAsBox(SMT.box2dScale * w / 2, SMT.box2dScale * h / 2);
 		box.createFixture(boxShape, 0.0f);
 		return box;
 	}
 
 	/**
 	 * Redirects the MotionEvent object to AndroidToTUIO, used because current
 	 * version of Processing (2.x) does not support registerMethod("touchEvent",
 	 * target).
 	 * 
 	 * @param me
 	 *            MotionEvent - the motion event triggered in Android
 	 * @return Should the event get consumed elsewhere or not
 	 */
 	public static boolean passAndroidTouchEvent(Object me) {
 		return att.onTouchEvent(me);
 	}
 
 	/**
 	 * Returns the list of zones.
 	 * 
 	 * @return zoneList
 	 */
 	public static Zone[] getZones() {
 		return getDescendents(sketch).toArray(new Zone[0]);
 	}
 
 	private static List<Zone> getDescendents(Zone parent) {
 		ArrayList<Zone> descendents = new ArrayList<Zone>();
 		for(Zone child : parent.getChildren()){
 			descendents.add(child);
 			descendents.addAll(getDescendents(child));
 		}
 		return descendents;
 	}
 
 	/**
 	 * Sets the flag for drawing touch points in the PApplet. Draws the touch
 	 * points if flag is set to true.
 	 * 
 	 * @param drawTouchPoints
 	 *            boolean - flag
 	 * @deprecated
 	 */
 	public static void setDrawTouchPoints(TouchDraw drawTouchPoints) {
 		setTouchDraw(drawTouchPoints);
 	}
 
 	/**
 	 * Sets the flag for drawing touch points in the PApplet. Draws the touch
 	 * points if flag is set to true. Sets the maximum path length to draw to be
 	 * max_path_length
 	 * 
 	 * @param drawTouchPoints
 	 *            boolean - flag
 	 * 
 	 * @param max_path_length
 	 *            int - sets maximum path length to draw
 	 * @deprecated
 	 */
 	public static void setDrawTouchPoints(TouchDraw drawTouchPoints, int max_path_length) {
 		setTouchDraw(drawTouchPoints, max_path_length);
 	}
 
 	/**
 	 * Sets the flag for drawing touch points in the PApplet. Draws the touch
 	 * points if flag is set to true.
 	 * 
 	 * @param drawTouchPoints
 	 *            boolean - flag
 	 */
 	public static void setTouchDraw(TouchDraw drawTouchPoints) {
 		SMT.drawTouchPoints = drawTouchPoints;
 	}
 
 	/**
 	 * Sets the flag for drawing touch points in the PApplet. Draws the touch
 	 * points if flag is set to true. Sets the maximum path length to draw to be
 	 * max_path_length
 	 * 
 	 * @param drawTouchPoints
 	 *            boolean - flag
 	 * 
 	 * @param max_path_length
 	 *            int - sets maximum path length to draw
 	 */
 	public static void setTouchDraw(TouchDraw drawTouchPoints, int max_path_length) {
 		SMT.drawTouchPoints = drawTouchPoints;
 		SMT.MAX_PATH_LENGTH = max_path_length;
 	}
 
 	/**
 	 * Draws the touch points in the PApplet if flag is set to true.
 	 */
 	public static void drawSmoothTouchPoints() {
 		parent.pushStyle();
 		for (Touch t : SMTTouchManager.currentTouchState) {
 			parent.strokeWeight(3);
 
 			long time = System.currentTimeMillis() - t.startTimeMillis;
 
 			if (t.isAssigned() && time < 250) {
 				parent.noFill();
 				parent.stroke(155, 255, 155, 100);
 				parent.ellipse(t.x, t.y, 75 - time / 10, 75 - time / 10);
 			}
 			else if (!t.isAssigned() && time < 500) {
 				{
 					parent.noFill();
 					parent.stroke(255, 155, 155, 100);
 					parent.ellipse(t.x, t.y, time / 10 + 50, time / 10 + 50);
 				}
 			}
 			parent.noStroke();
 			parent.fill(0, 0, 0, 50);
 			parent.ellipse(t.x, t.y, 40, 40);
 			parent.fill(255, 255, 255, 50);
 			if (t.isJointCursor) {
 				parent.fill(0, 255, 0, 50);
 			}
 			parent.ellipse(t.x, t.y, 30, 30);
 			parent.noFill();
 			parent.stroke(200, 240, 255, 50);
 			Point[] path = t.getPathPoints();
 			if (path.length > 3) {
 				for (int j = 3 + Math.max(0, path.length - (SMT.MAX_PATH_LENGTH + 2)); j < path.length; j++) {
 					float weight = 10 - (path.length - j) / 5;
 					if (weight >= 1) {
 						parent.strokeWeight(weight);
 						parent.bezier(path[j].x, path[j].y, path[j - 1].x, path[j - 1].y,
 								path[j - 2].x, path[j - 2].y, path[j - 3].x, path[j - 3].y);
 					}
 				}
 			}
 		}
 		parent.popStyle();
 	}
 
 	/**
 	 * Draws all Touch objects and their path history, with some data to try to
 	 * help with debugging
 	 */
 	public static void drawDebugTouchPoints() {
 		parent.pushStyle();
 		for (Touch t : SMTTouchManager.currentTouchState) {
 			parent.fill(255);
 			if (t.isJointCursor) {
 				parent.fill(0, 255, 0);
 			}
 			parent.stroke(0);
 			parent.ellipse(t.x, t.y, 30, 30);
 			parent.line(t.x, t.y - 15, t.x, t.y + 15);
 			parent.line(t.x - 15, t.y, t.x + 15, t.y);
 			TuioPoint[] path = t.path.toArray(new TuioPoint[t.path.size()]);
 			TuioPoint lastText = null;
 			if (path.length > 3) {
 				for (int j = 1 + Math.max(0, path.length - (SMT.MAX_PATH_LENGTH + 2)); j < path.length; j++) {
 					String pointText = "#" + j + " x:" + path[j].getScreenX(parent.width) + " y:"
 							+ path[j].getScreenY(parent.height) + " t:"
 							+ path[j].getTuioTime().getTotalMilliseconds() + "ms";
 					parent.fill(255);
 					parent.line(path[j].getScreenX(parent.width),
 							path[j].getScreenY(parent.height),
 							path[j - 1].getScreenX(parent.width),
 							path[j - 1].getScreenY(parent.height));
 					parent.ellipse(path[j].getScreenX(parent.width),
 							path[j].getScreenY(parent.height), 5, 5);
 					if (lastText == null
 							|| Math.abs(path[j].getScreenY(parent.height)
 									- lastText.getScreenY(parent.height)) > 13
 							|| Math.abs(path[j].getScreenX(parent.width)
 									- lastText.getScreenX(parent.width)) > parent
 									.textWidth(pointText) * 1.5) {
 						parent.fill(0);
 						parent.textSize(12);
 						parent.text(pointText, path[j].getScreenX(parent.width) + 5,
 								path[j].getScreenY(parent.height));
 						lastText = path[j];
 						parent.fill(255, 0, 0);
 						parent.ellipse(path[j].getScreenX(parent.width),
 								path[j].getScreenY(parent.height), 5, 5);
 					}
 				}
 			}
 		}
 		parent.popStyle();
 	}
 
 	/**
 	 * Adds a zone(s) to the sketch/application.
 	 * 
 	 * @param zones
 	 *            - Zone: The zones to add to the sketch/application
 	 */
 	public static void add(Zone... zones) {
 		for (Zone zone : zones) {
 			//no-op if the zone is already a child of the root Zone, or if the
 			//zone is the root zone (to prevent parenting to itself)
 			if(!SMT.sketch.children.contains(zone) && SMT.sketch != zone){
 				if (zone != null) {
 					if(zone.parent == null){
 						zone.parent = SMT.sketch;
 						addToZoneList(zone);
 					}
 					
 					picker.add(zone);
 	
 					// make sure the matrix is up to date, these calls should not
 					// occur if we do not call begin/endTouch once per
 					// frame and once at Zone initialization
 					zone.endTouch();
 					zone.beginTouch();
 				}
 				else {
 					System.err.println("Error: Added a null Zone");
 				}
 			}
 		}
 	}
 
 	private static void addToZoneList(Zone zone) {
 		if (!sketch.children.contains(zone)) {
 			sketch.children.add(zone);
 		}
 	}
 
 	/**
 	 * This adds zones by creating them from XML specs, the XML needs "zone"
 	 * tags, and currently supports the following variables: name, x, y, width,
 	 * height, img
 	 * 
 	 * @param xmlFilename
 	 *            The XML file to read in for zone configuration
 	 * @return The array of zones created from the XML File
 	 */
 	public static Zone[] add(String xmlFilename) {
 		List<Zone> zoneList = new ArrayList<Zone>();
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(parent.createInput(xmlFilename));
 
 			NodeList zones = doc.getElementsByTagName("zone");
 			add(zones, zoneList);
 		}
 		catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		catch (SAXException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		return zoneList.toArray(new Zone[zoneList.size()]);
 	}
 
 	/**
 	 * This adds a set of zones to a parent Zone
 	 * 
 	 * @param parent
 	 *            The Zone to add the given zones to
 	 * @param zones
 	 *            The zones to add to the parent as children
 	 */
 	public static void addChild(Zone parent, Zone... zones) {
 		for (Zone z : zones) {
 			if (parent != null) {
 				parent.add(z);
 			}
 		}
 	}
 
 	/**
 	 * This adds a set of zones to a parent Zone
 	 * 
 	 * @param parentName
 	 *            The name of the Zone to add the given zones to
 	 * @param zones
 	 *            The zones to add to the parent as children
 	 */
 	public static void addChild(String parentName, Zone... zones) {
 		for (Zone z : zones) {
 			if (get(parentName) != null) {
 				get(parentName).add(z);
 			}
 		}
 	}
 
 	/**
 	 * This removes a set of zones to a parent Zone
 	 * 
 	 * @param parent
 	 *            The Zone to add the given zones to
 	 * @param zones
 	 *            The zones to add to the parent as children
 	 */
 	public static void removeChild(Zone parent, Zone... zones) {
 		for (Zone z : zones) {
 			if (parent != null) {
 				parent.remove(z);
 			}
 		}
 	}
 
 	/**
 	 * This removes a set of zones to a parent Zone
 	 * 
 	 * @param parentName
 	 *            The name of the Zone to add the given zones to
 	 * @param zones
 	 *            The zones to add to the parent as children
 	 */
 	public static void removeChild(String parentName, Zone... zones) {
 		for (Zone z : zones) {
 			if (get(parentName) != null) {
 				get(parentName).remove(z);
 			}
 		}
 	}
 
 	/**
 	 * This organizes the given zones into a grid configuration with the given
 	 * x, y, width, height, and spacing
 	 * 
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param xSpace
 	 * @param ySpace
 	 * @param zones
 	 */
 	public static void grid(int x, int y, int width, int xSpace, int ySpace, Zone... zones) {
 		int currentX = x;
 		int currentY = y;
 
 		int rowHeight = 0;
 
 		for (Zone zone : zones) {
 			if (currentX + zone.width > x + width) {
 				currentX = x;
 				currentY += rowHeight + ySpace;
 				rowHeight = 0;
 			}
 
 			zone.setLocation(currentX, currentY);
 
 			currentX += zone.width + xSpace;
 			rowHeight = Math.max(rowHeight, zone.height);
 		}
 	}
 
 	private static void add(NodeList zones, List<Zone> zoneList) {
 		for (int i = 0; i < zones.getLength(); i++) {
 			Node zoneNode = zones.item(i);
 			if (zoneNode.getNodeType() == Node.ELEMENT_NODE
 					&& zoneNode.getNodeName().equalsIgnoreCase("zone")) {
 				add(zoneNode, zoneList);
 			}
 		}
 	}
 
 	private static void add(Node node, List<Zone> zoneList) {
 		NamedNodeMap map = node.getAttributes();
 		Node nameNode = map.getNamedItem("name");
 		Node xNode = map.getNamedItem("x");
 		Node yNode = map.getNamedItem("y");
 		Node widthNode = map.getNamedItem("width");
 		Node heightNode = map.getNamedItem("height");
 
 		Node imgNode = map.getNamedItem("img");
 
 		Zone zone;
 
 		String name = null;
 		int x, y, width, height;
 		if (nameNode != null) {
 			name = nameNode.getNodeValue();
 		}
 
 		if (imgNode != null) {
 			String imgFilename = imgNode.getNodeValue();
 			PImage img = parent.loadImage(imgFilename);
 
 			if (xNode != null && yNode != null) {
 				x = Integer.parseInt(xNode.getNodeValue());
 				y = Integer.parseInt(yNode.getNodeValue());
 				if (widthNode != null && heightNode != null) {
 					width = Integer.parseInt(widthNode.getNodeValue());
 					height = Integer.parseInt(heightNode.getNodeValue());
 					zone = new ImageZone(name, img, x, y, width, height);
 				}
 				else {
 					zone = new ImageZone(name, img, x, y);
 				}
 			}
 			else {
 				zone = new ImageZone(name, img);
 			}
 		}
 		else {
 			if (xNode != null && yNode != null && widthNode != null && heightNode != null) {
 				x = Integer.parseInt(xNode.getNodeValue());
 				y = Integer.parseInt(yNode.getNodeValue());
 				width = Integer.parseInt(widthNode.getNodeValue());
 				height = Integer.parseInt(heightNode.getNodeValue());
 				zone = new Zone(name, x, y, width, height);
 			}
 			else {
 				zone = new Zone(name);
 			}
 		}
 
 		zoneList.add(zone);
 		add(zone);
 		add(node.getChildNodes(), zoneList);
 	}
 
 	/**
 	 * This removes the zone given from the SMT, meaning it will not be
 	 * drawn anymore or be assigned touches, but can be added back with a call
 	 * to add(zone);
 	 * 
 	 * @param name
 	 *            The name of the zone to remove
 	 * @return Whether all of the zones were removed successfully
 	 */
 	public static boolean remove(String name) {
 		return remove(get(name));
 	}
 
 	/**
 	 * This removes the zone given from the SMT, meaning it will not be
 	 * drawn anymore or be assigned touches, but can be added back with a call
 	 * to add(zone);
 	 * 
 	 * @param zone
 	 *            The zone to remove
 	 * @return Whether all of the zones were removed successfully
 	 */
 	public static boolean remove(Zone... zones) {
 		boolean r = true;
 		for (Zone zone : zones) {
 			//no-op if the zone is not a child of the root Zone
 			if(SMT.sketch.children.contains(zone)){
 				if (zone != null) {
 					zone.parent=null;
 					picker.remove(zone);
 					if (!removeFromZoneList(zone)) {
 						r = false;
 					}
 				}
 				else {
 					r = false;
 					System.err.println("Error: Removed a null Zone");
 				}
 			}
 		}
 		return r;
 	}
 
 	private static boolean removeFromZoneList(Zone zone) {
 		for (Zone child : zone.children) {
 			removeFromZoneList(child);
 		}
 
 		// clear the touches from the Zone, so that it doesn't get pulled in by
 		// the touches such as when putZoneOnTop() is called
 		zone.unassignAll();
 
 		// destroy the Zones Body, so it does not collide with other Zones any
 		// more
 		if (zone.zoneBody != null) {
 			world.destroyBody(zone.zoneBody);
 			zone.zoneBody = null;
 			zone.zoneFixture = null;
 			zone.mJoint = null;
 		}
 		return sketch.children.remove(zone);
 	}
 
 	/**
 	 * Performs the drawing of the zones in order. Zone on top-most layer gets
 	 * drawn last. Goes through the list on zones, pushes the current
 	 * transformation matrix, applies the zone's matrix, draws the zone, pops
 	 * the matrix, and when at the end of the list, it draws the touch points.
 	 */
 	public static void draw() {
 		sketch.draw();
 		
 		switch (drawTouchPoints) {
 		case DEBUG:
 			drawDebugTouchPoints();
 			break;
 		case SMOOTH:
 			drawSmoothTouchPoints();
 			break;
 		case NONE:
 			break;
 		}
 	}
 
 	/**
 	 * Draws a texture of the pickBuffer at the given x,y position with given
 	 * width and height
 	 * 
 	 * @param x
 	 *            - the x position to draw the pickBuffer image at
 	 * @param y
 	 *            - the y position to draw the pickBuffer image at
 	 * @param w
 	 *            - the width of the pickBuffer image to draw
 	 * @param h
 	 *            - the height of the pickBuffer image to draw
 	 * @deprecated
 	 */
 	public static void drawPickBuffer(int x, int y, int w, int h) {
 		// parent.g.image(picker.image, x, y, w, h);
 	}
 
 	/**
 	 * Returns a vector containing all the current TuioObjects.
 	 * 
 	 * @return Vector<TuioObject>
 	 */
 	public static Vector<TuioObject> getTuioObjects() {
 		return new Vector<TuioObject>(listener.getTuioObjects());
 	}
 
 	/**
 	 * @return An array containing all touches that are currently NOT assigned
 	 *         to zones
 	 */
 	public static Touch[] getUnassignedTouches() {
 		List<Touch> touches = new ArrayList<Touch>();
 		for (Touch touch : getTouches()) {
 			if(!touch.isAssigned()){
 				touches.add(touch);
 			}
 		}
 		return touches.toArray(new Touch[touches.size()]);
 	}
 
 	/**
 	 * @return An array containing all touches that are currently assigned to
 	 *         zones
 	 */
 	public static Touch[] getAssignedTouches() {
 		List<Touch> touches = new ArrayList<Touch>();
 		for (Zone zone : getZones()) {
 			for (Touch touch : zone.getTouches()) {
 				touches.add(touch);
 			}
 		}
 		return touches.toArray(new Touch[touches.size()]);
 	}
 
 	/**
 	 * @param zone
 	 *            The zone to assign touches to
 	 * @param touches
 	 *            The touches to assign to the zone, variable number of
 	 *            arguments
 	 */
 	public static void assignTouches(Zone zone, Touch... touches) {
 		zone.assign(touches);
 	}
 
 	/**
 	 * Returns a collection containing all the current Touches(TuioCursors).
 	 * 
 	 * @return Collection<Touch>
 	 */
 	public static Collection<Touch> getTouchCollection() {
 		return getTouchMap().values();
 	}
 
 	/**
 	 * Returns a collection containing all the current Touches(TuioCursors).
 	 * 
 	 * @return Touch[] containing all touches that are currently mapped
 	 */
 	public static Touch[] getTouches() {
 		return getTouchMap().values().toArray(new Touch[getTouchMap().values().size()]);
 	}
 
 	/**
 	 * @return A Map<Long,Touch> indexing all touches by their session_id's
 	 */
 	public static Map<Long, Touch> getTouchMap() {
 		return SMTTouchManager.currentTouchState.idToTouches;
 	}
 
 	// /**
 	// * This method returns all the current touches that are not actively
 	// * affecting any Zone.
 	// *
 	// * @return the array
 	// */
 	// public static Touch[] getUnassignedTouches() {
 	// Vector<TuioCursor> cursors = tuioClient.getTuioCursors();
 	// ArrayList<Touch> touches = new ArrayList<>();
 	// for (TuioCursor c : cursors) {
 	// Zone z = picker.getZoneFromId(c.getSessionID());
 	// if (z == null) {
 	// touches.add(new Touch(c));
 	// }
 	// }
 	// return touches.toArray(new Touch[touches.size()]);
 	// }
 
 	/**
 	 * @return An array of all zones that currently have touches/are active.
 	 */
 	public static Zone[] getActiveZones() {
 		ArrayList<Zone> zones = new ArrayList<Zone>();
 		for (Zone zone : getZones()) {
 			if (zone.isActive()) {
 				zones.add(zone);
 			}
 		}
 		return zones.toArray(new Zone[zones.size()]);
 	}
 
 	/**
 	 * @param zone
 	 *            The zone to get the touches of
 	 * @return A Collection<Touch> containing all touches from the given zone
 	 */
 	public static Collection<Touch> getTouchCollectionFromZone(Zone zone) {
 		return zone.getTouchCollection();
 	}
 
 	/**
 	 * @param zone
 	 *            The zone to get the touches of
 	 * @return A Touch[] containing all touches from the given zone
 	 */
 	public static Touch[] getTouchesFromZone(Zone zone) {
 		return zone.getTouches();
 	}
 
 	/**
 	 * Returns a the TuioObject associated with the session ID
 	 * 
 	 * @param s_id
 	 *            long - Session ID of the TuioObject
 	 * @return TuioObject
 	 */
 	public static TuioObject getTuioObject(long s_id) {
 		return listener.getTuioObject(s_id);
 	}
 
 	/**
 	 * Returns the Touch(TuioCursor) associated with the session ID
 	 * 
 	 * @param s_id
 	 *            long - Session ID of the Touch(TuioCursor)
 	 * @return TuioCursor
 	 */
 	public static Touch getTouch(long s_id) {
 		return SMTTouchManager.currentTouchState.getById(s_id);
 	}
 
 	/**
 	 * @param s_id
 	 *            The session_id of the Touch to get the path start of
 	 * @return A new Touch containing the state of the touch at path start
 	 */
 	public static Touch getPathStartTouch(long s_id) {
 		TuioCursor c = listener.getTuioCursor(s_id);
 		Vector<TuioPoint> path = new Vector<TuioPoint>(c.getPath());
 
 		TuioPoint start = path.firstElement();
 		return new Touch(start.getTuioTime(), c.getSessionID(), c.getCursorID(), start.getX(),
 				start.getY());
 	}
 
 	/**
 	 * This creates a new Touch object from the last touch of a given Touch
 	 * object, the new Touch will not update.
 	 * <P>
 	 * It is easier to just create a new Touch with the given Touch object as
 	 * its parameter "new Touch(current)" where current is the Touch we want the
 	 * last touch of, so this is deprecated.
 	 * 
 	 * @param current
 	 *            The Touch to get the last touch from
 	 * @return A Touch containing the last touch, will not update, stays
 	 *         consistent
 	 * @deprecated
 	 */
 	public static Touch getLastTouch(Touch current) {
 		Vector<TuioPoint> path = current.path;
 		if (path.size() > 1) {
 			TuioPoint last = path.get(path.size() - 2);
 			return new Touch(last.getTuioTime(), current.getSessionID(), current.getCursorID(),
 					last.getX(), last.getY());
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the number of current Touches (TuioCursors)
 	 * 
 	 * @return number of current touches
 	 */
 	public static int getTouchCount() {
 		return listener.getTuioCursors().size();
 	}
 
 	/**
 	 * Manipulates the zone's position if it is throw-able after it has been
 	 * released by a finger (cursor) Done before each call to draw. Uses the
 	 * zone's x and y friction values.
 	 */
 	public static void pre() {
 		// TODO: provide some default assignment of touches
 		manager.handleTouches();
 
 		if (mtt != null) {
 			// update touches from mouseToTUIO joint cursors as they are a
 			// special case and need to be shown to user
 			for (Touch t : SMTTouchManager.currentTouchState) {
 				t.isJointCursor = false;
 			}
 			for (Integer i : mtt.getJointCursors()) {
 				SMTTouchManager.currentTouchState.getById(i).isJointCursor = true;
 			}
 		}
 
 		parent.g.flush();
 		if (getTouches().length > 0) {
 			SMTUtilities.invoke(touch, parent, null);
 		}
 		
 		sketch.touch();
 
 		updateStep();
 
 		// clear the background to get rid of anything from pre-draw
 		parent.g.background(255);
 	}
 
 	/**
 	 * perform a step of physics using jbox2d, update bodies before and matrix
 	 * after of each Zone to keep the matrix and bodies synchronized
 	 */
 	private static void updateStep() {
 		for (Zone z : getZones()) {
 			if (z.physics) {
 				// generate body and fixture for zone if they do not exist
 				if (z.zoneBody == null && z.zoneFixture == null) {
 					z.zoneBody = world.createBody(z.zoneBodyDef);
 					z.zoneFixture = z.zoneBody.createFixture(z.zoneFixtureDef);
 				}
 				z.setBodyFromMatrix();
 			}
 			else {
 				// make sure to destroy body for Zones that do not have physics
 				// on, as they should not collide with others
 				if (z.zoneBody != null) {
 					world.destroyBody(z.zoneBody);
 					z.zoneBody = null;
 					z.zoneFixture = null;
 					z.mJoint = null;
 				}
 			}
 		}
 
 		world.step(1f / parent.frameRate, 8, 3);
 
 		for (Zone z : sketch.children) {
 			if (z.physics) {
 				z.setMatrixFromBody();
 			}
 		}
 	}
 	
 	/**
 	 * @return A temp file directory with a unique name
 	 * @throws IOException
 	 */
 	private static File tempDir() throws IOException{
 		File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
 	
 		if (!(temp.delete())) {
 			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
 		}
 	
 		if (!(temp.mkdir())) {
 			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
 		}
 		
 		return temp;
 	}
 	
 	/**
 	 * @param dir The directory to load the resource into
 	 * @param resource A string containing the name a program in SMT's resources folder
 	 * @return A file written into dir
 	 * @throws IOException
 	 */
 	private static File loadFile(File dir, String resource) throws IOException{
 		BufferedInputStream src = new BufferedInputStream(
 				SMT.class.getResourceAsStream("/resources/"+resource));
 		final File exeTempFile = new File(dir.getAbsolutePath() + "\\"+resource);
 		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(exeTempFile));
 		byte[] tempexe = new byte[4 * 1024];
 		int rc;
 		while ((rc = src.read(tempexe)) > 0) {
 			out.write(tempexe, 0, rc);
 		}
 		src.close();
 		out.close();
 		exeTempFile.deleteOnExit();
 		return exeTempFile;
 	}
 	
 	/**
 	 * This class encapsulates all the logic for running a seperate process in a thread
 	 * It is used by runWinTouchTuioServer(), runSMart2TuioServer(), and runLeapTuioServer()
 	 * To avoid duplicated code. It puts labels on output and allows specifying a error message
 	 * for premature shutdown condition.
 	 */
 	private static class TouchSourceThread extends Thread{
 		String label;
 		String execArg;
 		String prematureShutdownError;
 		
 		TouchSourceThread(String label, String execArg, String prematureShutdownError){
 			this.label=label;
 			this.execArg=execArg;
 			this.prematureShutdownError=prematureShutdownError;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				Process tuioServer = Runtime.getRuntime().exec(execArg);
 				BufferedReader tuioServerErr = new BufferedReader(new InputStreamReader(
 						tuioServer.getErrorStream()));
 				BufferedReader tuioServerOut = new BufferedReader(new InputStreamReader(
 						tuioServer.getInputStream()));
 				BufferedWriter tuioServerIn = new BufferedWriter(new OutputStreamWriter(
 						tuioServer.getOutputStream()));
 
 				tuioServerList.add(tuioServer);
 				tuioServerErrList.add(tuioServerErr);
 				tuioServerOutList.add(tuioServerOut);
 				tuioServerInList.add(tuioServerIn);
 
 				while (true) {
 					if (tuioServerErr.ready() && debug) {
 						System.err.println(label + ": " + tuioServerErr.readLine());
 					}
 					if (tuioServerOut.ready() && debug) {
 						System.out.println(label + ": " + tuioServerOut.readLine());
 					}
 
 					try {
 						tuioServer.exitValue();
 						if(!SMT.inShutdown){
 							System.err
 									.println(prematureShutdownError);
 							break;
 						}
 					}
 					catch (IllegalThreadStateException e) {
 						// still running... sleep time
 						Thread.sleep(100);
 					}
 				}
 			}
 			catch (IOException e) {
 				System.err.println(e.getMessage());
 			}
 			catch (Exception e) {
 				System.err.println(label + " TUIO Server stopped!");
 			}
 		}
 	}
 
 	/**
 	 * Runs a server that sends TUIO events using Windows 7 Touch events
 	 * 
 	 * @param is64Bit
 	 *            Whether to use the 64-bit version of the exe
 	 */
 	private static void runWinTouchTuioServer(boolean is64Bit, final String address, final int port) {
 		try {
 			File temp = tempDir();
 			
 			loadFile(temp, is64Bit ? "TouchHook_x64.dll" : "TouchHook.dll");
 
 			new TouchSourceThread("WM_TOUCH", loadFile(temp, is64Bit ? "Touch2Tuio_x64.exe" : "Touch2Tuio.exe").getAbsolutePath() + " " + parent.frame.getTitle() + " "
 										+ address + " " + port, "WM_TOUCH Process died early, make sure Visual C++ Redistributable for Visual Studio 2012 is installed (http://www.microsoft.com/en-us/download/details.aspx?id=30679)").start();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void runSmart2TuioServer() {
 		try {
 			File temp = tempDir();
 			
 			loadFile(temp, "SMARTTableSDK.Core.dll");
 			loadFile(temp, "libTUIO.dll");
 
 			new TouchSourceThread("SMART", loadFile(temp, "SMARTtoTUIO2.exe").getAbsolutePath(), "SMART Process died").start();								
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Runs a server that sends TUIO events using Windows 7 Touch events
 	 * 
 	 * @param is64Bit
 	 *            Whether to use the 64-bit version of the exe
 	 */
 	private static void runLeapTuioServer(final int port) {
 		try {
 			File temp = tempDir();
 			
 			loadFile(temp, "Leap.dll");
 
 			new TouchSourceThread("LEAP", loadFile(temp, "motionLess.exe").getAbsolutePath() + " " + port, "LEAP Process died early, make sure Visual C++ 2010 Redistributable (x86) is installed (http://www.microsoft.com/en-ca/download/details.aspx?id=5555)").start();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Runs an exe from a path, presumably for translating native events to tuio
 	 * events
 	 */
 	public static void runExe(final String path) {
 		Thread serverThread = new Thread() {
 			@Override
 			public void run() {
 				int max_failures = 3;
 				for (int i = 0; i < max_failures; i++) {
 					try {
 						Process tuioServer = Runtime.getRuntime().exec(path);
 						BufferedReader tuioServerErr = new BufferedReader(new InputStreamReader(
 								tuioServer.getErrorStream()));
 						BufferedReader tuioServerOut = new BufferedReader(new InputStreamReader(
 								tuioServer.getInputStream()));
 						BufferedWriter tuioServerIn = new BufferedWriter(new OutputStreamWriter(
 								tuioServer.getOutputStream()));
 
 						tuioServerList.add(tuioServer);
 						tuioServerErrList.add(tuioServerErr);
 						tuioServerOutList.add(tuioServerOut);
 						tuioServerInList.add(tuioServerIn);
 
 						while (true) {
 							if (tuioServerErr.ready() && debug) {
 								System.err.println(path + ": " + tuioServerErr.readLine());
 							}
 							if (tuioServerOut.ready() && debug) {
 								System.out.println(path + ": " + tuioServerOut.readLine());
 							}
 							Thread.sleep(1000);
 						}
 					}
 					catch (IOException e) {
 						System.err.println(e.getMessage());
 						break;
 					}
 					catch (Exception e) {
 						System.err.println("Exe stopped!, restarting.");
 					}
 				}
 				System.out.println("Stopped trying to run exe.");
 			}
 		};
 		serverThread.start();
 	}
 
 	/**
 	 * @param zone
 	 *            The zone to place on top of the others
 	 */
 	public static void putZoneOnTop(Zone zone) {
 		sketch.putChildOnTop(zone);
 	}
 
 	/**
 	 * This finds a zone by its name, returning the first zone with the given
 	 * name or null.
 	 * <P>
 	 * This will throw ClassCastException if the Zone is not an instance of the
 	 * given class , and non-applicable type compile errors when the given class
 	 * does not extend Zone.
 	 * 
 	 * @param name
 	 *            The name of the zone to find
 	 * @param type
 	 *            a class type to cast the Zone to (e.g. Zone.class)
 	 * @return a Zone with the given name or null if it cannot be found
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends Zone> T get(String name, Class<T> type) {
 		for (Zone z : getZones()) {
 			if (z.name != null && z.name.equals(name)) {
 				return (T) z;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This finds a zone by its name, returning the first zone with the given
 	 * name or null
 	 * 
 	 * @param name
 	 *            The name of the zone to find
 	 * @return a Zone with the given name or null if it cannot be found
 	 */
 	public static Zone get(String name) {
 		return get(name, Zone.class);
 	}
 
 	/**
 	 * This adds objects to check for drawZoneName, touchZoneName, etc methods
 	 * in, similar to PApplet
 	 * 
 	 * @param classes
 	 *            The additional objects to check in for methods
 	 */
 	public static void addMethodClasses(Class<?>... classes) {
 		for (Class<?> c : classes) {
 			SMTUtilities.loadMethods(c);
 		}
 	}
 }
