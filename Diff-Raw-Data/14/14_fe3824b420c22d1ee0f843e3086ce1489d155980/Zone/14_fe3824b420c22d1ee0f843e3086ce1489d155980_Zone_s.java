 /*
  * Simple Multi-Touch Library Copyright 2011 Erik Paluka, Christopher Collins -
  * University of Ontario Institute of Technology Mark Hancock - University of
  * Waterloo
  * 
  * Parts of this library are based on: TUIOZones http://jlyst.com/tz/
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
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.*;
 import org.jbox2d.dynamics.joints.*;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 import processing.core.*;
 import processing.event.KeyEvent;
 import processing.opengl.PGraphics3D;
 import TUIO.TuioTime;
 
 //local imports
 import vialab.SMT.renderer.*;
 
 /**
  * This is the main zone class which all other Zones extend. It holds the zone's
  * coordinates, size, matrices, etc.. Zones are used to draw and interact with
  * the sketch/application.
  * 
  * @author Erik Paluka, Zach Cook
  * @version 1.0
  */
 public class Zone extends PGraphics3DDelegate implements PConstants, KeyListener {
 	
 	//parent applet
 	protected PApplet applet;
 	//parent zone
 	protected Zone parent = null;
 	
 	//properties
 	protected String name = null;
 	/** @deprecated use Zone.(get|set)(X|Y) instead */
 	@Deprecated
 	public int x, y;
 	/** @deprecated use Zone.(get|set)(Width|Height) instead */
 	@Deprecated
 	public int height, width;
 	/** The dimensions of the zone */
 	protected Dimension dimension;
 	/** The half-dimensions of the zone */
 	protected Dimension halfDimension;
 	
 	//The zone's transformation matrix
 	protected PMatrix3D matrix = new PMatrix3D();
 	protected PMatrix3D backupMatrix = null;
 	//The zone's inverse transformation matrix
 	protected PMatrix3D inverse = new PMatrix3D();
 
 	//reflection methods
 	protected Method method_draw = null;
 	protected Method method_pickDraw = null;
 	protected Method method_touch = null;
 	protected Method method_keyPressed = null;
 	protected Method method_keyReleased = null;
 	protected Method method_keyTyped = null;
 	protected Method method_touchUp = null;
 	protected Method method_touchDown = null;
 	protected Method method_touchMoved = null;
 	protected Method method_press = null;
 	//override detection flags
 	private boolean draw_overridden = false;
 	private boolean drawImpl_overridden = false;
 	private boolean pickDraw_overridden = false;
 	private boolean pickDrawImpl_overridden = false;
 	private boolean touchImpl_overridden = false;
 	private boolean touchUDM_overridden = false;
 	private boolean pressImpl_overridden = false;
 
 	//picking variables
 	private boolean picking_on = false;
 	protected Color pickColor = null;
 
 	// A LinkedHashMap will allow insertion order to be maintained.  A synchronised one will prevent concurrent modification (which can happen pretty easily with the draw loop + touch event handling).
 	/** All of the currently active touches for this zone */
 	private Map<Long, Touch> activeTouches = Collections.synchronizedMap(
 		new LinkedHashMap<Long, Touch>());
 	protected CopyOnWriteArrayList<Zone> children = new CopyOnWriteArrayList<Zone>();
 	protected TuioTime lastUpdate = TuioTime.getSessionTime();
 	protected float rntRadius;
 	protected String renderer_name = null;
 
 	/** @deprecated use setDirect( boolean) instead */
 	@Deprecated
 	protected boolean direct;
 	protected boolean captureTouches = true;
 
 	protected boolean scalingLimit = false;
 	MouseJoint mJoint;
 	private MouseJointDef mJointDef;
 	private Object boundObject = null;
 	protected int maxWidth, maxHeight;
 	protected int minWidth, minHeight;
 	// changing the return of this method to true will cause indirect zones only redraw if setModified(true) is called on it.
 	protected boolean updateOnlyWhenModified(){ return false;}
 	/** @deprecated We're working on a better alternative */
 	@Deprecated
 	public boolean stealChildrensTouch = false;
 
 	//physics fields
 	/** @deprecated use Zone.setPhysicsEnabled( boolean) instead */
 	@Deprecated
 	public boolean physics = false;
 	/** A flag that describes whether physics is enabled. */
 	private boolean physics_enabled = false;
 	BodyDef zoneBodyDef = new BodyDef();
 	Body zoneBody;
 	PolygonShape zoneShape = new PolygonShape();
 	FixtureDef zoneFixtureDef = new FixtureDef();
 	Fixture zoneFixture;
 
 	public void enableScalingLimit(int maxW, int maxH, int minW, int minH) {
 		this.scalingLimit = true;
 		this.maxWidth = maxW;
 		this.maxHeight = maxH;
 		this.minWidth = minW;
 		this.minHeight = minH;
 	}
 
 	public void disableScalingLimit() {
 		this.scalingLimit = false;
 	}
 
 	private List<Touch> touchDownList = new ArrayList<Touch>();
 	private List<Touch> touchUpList = new ArrayList<Touch>();
 	private List<Touch> touchMovedList = new ArrayList<Touch>();
 	private List<Touch> pressList = new ArrayList<Touch>();
 	private float offsetX;
 	private float offsetY;
 	private HashSet<Long> dragSeenTouch = new HashSet<Long>();
 	private PGraphics3D extra_graphics;
 	private boolean firstDraw;
 
 	boolean warnDraw() {
 		return true;
 	}
 	boolean warnTouch() {
 		return true;
 	}
 	boolean warnKeys() {
 		return false;
 	}
 	boolean warnPick() {
 		return false;
 	}
 	boolean warnTouchUDM() {
 		return false;
 	}
 	boolean warnPress() {
 		return false;
 	}
 
 	/**
 	 * Check state of the direct flag.
 	 * 
 	 * The direct flag controls whether rendering directly onto
 	 * parent/screen/pickBuffer (direct), or into an image (not direct) If
 	 * drawing into an image we have assured size(cant draw outside of zone),
 	 * and background() will work for just the zone, but we lose a large amount
 	 * of performance.
 	 * 
 	 * @return whether zone is rendering directly onto screen/pickBuffer, or not
 	 */
 	public boolean isDirect() {
 		return direct;
 	}
 
 	/**
 	 * Change state of the direct flag.
 	 * 
 	 * The direct flag controls whether rendering directly onto
 	 * parent/screen/pickBuffer (direct), or into an image (not direct) If
 	 * drawing into an image we have assured size(cant draw outside of zone),
 	 * and background() will work for just the zone, but we lose a large amount
 	 * of performance.
 	 *
 	 * @param enabled Whether the direct flag should be enabled
 	 */
 	public void setDirect( boolean enabled) {
 		if( enabled){
 			this.vertices = null;
 			this.tessGeo = null;
 			this.texCache = null;
 			this.inGeo = null;
 			this.extra_graphics = null;
 		}
 		else{
 			//create offscreen graphics context
 			PGraphics extra_object = applet.createGraphics(
 				this.dimension.width,
 				this.dimension.height,
 				this.renderer_name);
 			//double-check the class
 			if( ! PGraphics3D.class.isInstance( extra_object))
 				throw new ClassCastException(
 					"Must use PGraphics3D, or a class that extends PGraphics3D as the renderer for zones.");
 			extra_graphics = (PGraphics3D) extra_object;
 			//draw the background
 			extra_graphics.beginDraw();
 			extra_graphics.background( 0, 0);
 			extra_graphics.endDraw();
 			setModified();
 		}
 		this.direct = enabled;
 	}
 	private void extraGraphicsNullCheck(){
 		if( ! direct)
 			if( extra_graphics == null)
 				setDirect( false);}
 
 	/**
 	 * Normally, zones "capture" touches. This means that normally when touches exit a zone, they remain assigned to that zone. This "capturing" behavior, however, can be disabled. This is commonly done with buttons and UI elements.
 	 *
 	 * @param enabled whether touch capturing should be enabled
 	 */
 	public void setCaptureTouchesEnabled( boolean enabled){
 		captureTouches = enabled;
 	}
 
 	/**
 	 * Normally, zones "capture" touches. This means that normally when touches exit a zone, they remain assigned to that zone. This "capturing" behavior, however, can be disabled. This is commonly done with buttons and UI elements.
 	 */
 	public boolean getCaptureTouchesEnabled(){
 		return captureTouches;
 	}
 
 	/**
 	 * Zone constructor, no name, (x,y) position is (0,0) , width and height are 1
 	 */
 	public Zone() {
 		this(null);
 	}
 
 	/**
 	 * Zone constructor, with a name, (x,y) position is (0,0) , width and height
 	 *   are 1
 	 * @param name - String: Name of the zone, used in the draw, touch ,etc methods
 	 */
 	public Zone(String name) {
 		this(name, SMT.zone_renderer);
 	}
 
 	/**
 	 * Zone constructor, with a name, (x,y) position is (0,0) , width and height
 	 *   are 1
 	 * @param name  - String: Name of the zone, used in the draw, touch ,etc methods
 	 * @param renderer - String: The PGraphics renderer that draws the Zone
 	 */
 	public Zone(String name, String renderer) {
 		this(name, 0, 0, 100, 100, renderer);
 	}
 
 	/**
 	 * @param x The x position of the zone
 	 * @param y The y position of the zone
 	 * @param width The width of the zone
 	 * @param height The height of the zone
 	 */
 	public Zone(int x, int y, int width, int height) {
 		this(x, y, width, height, SMT.zone_renderer);
 	}
 
 	/**
 	 * @param x The x position of the zone
 	 * @param y The y position of the zone
 	 * @param width The width of the zone
 	 * @param height The height of the zone
 	 * @param renderer The renderer that draws the zone
 	 */
 	public Zone( int x, int y, int width, int height, String renderer) {
 		this( null, x, y, width, height, renderer);
 	}
 
 	/**
 	 * @param name The name of the zone, used for the reflection methods
 	 *   (drawname(),touchname(),etc)
 	 * @param x The x position of the zone
 	 * @param y The y position of the zone
 	 * @param width The width of the zone
 	 * @param height The height of the zone
 	 */
 	public Zone( String name, int x, int y, int width, int height) {
 		this( name, x, y, width, height, SMT.zone_renderer);
 	}
 
 	/**
 	 * @param name The name of the zone, used for the reflection methods (drawname(),touchname(),etc)
 	 * @param x The x position of the zone
 	 * @param y The y position of the zone
 	 * @param width The width of the zone
 	 * @param height The height of the zone
 	 * @param renderer The renderer that draws the zone
 	 */
 	public Zone( String name, int x, int y, int width, int height, String renderer) {
 		super();
 		//check if smt has been initialized
 		if( SMT.applet == null)
 			System.err.println(
 				"Warning: Initialization of Zone before SMT.init()");
 
 		//get parent
 		this.applet = SMT.applet;
 		this.setParent( SMT.applet);
 		//this.setDelegate( SMT.renderer);
 
 		//initialize properties
 		this.direct = true;
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.dimension = new Dimension( width, height);
 		this.halfDimension = new Dimension( width / 2, height / 2);
 		this.renderer_name = renderer;
 		this.rntRadius = Math.min( width, height) / 4;
 
 		resetMatrix();
 		setName( name);
 
 		zoneBodyDef.type = BodyType.DYNAMIC;
 		zoneBodyDef.linearDamping = 1.0f;
 		zoneBodyDef.angularDamping = 1.0f;
 		zoneShape.setAsBox( SMT.box2dScale * width / 2, SMT.box2dScale * height / 2);
 		zoneFixtureDef.shape = zoneShape;
 		zoneFixtureDef.density = 1.0f;
 		zoneFixtureDef.friction = 0.3f;
 	}
 
 	/**
 	 * @param name The new name of the zone
 	 */
 	public void setName( String name) {
 		if( name != null){
 			if( name.length() < 1)
 				throw new IllegalArgumentException(
 					"Cannot give a zone an empty string for a name");
 			this.name = name;
 		}
 		else {
 			name = this.getClass().getSimpleName();
 			if( name.length() < 1)
 				//this is an anonymous class!
 				name = this.getClass().getName();
 			this.name = name;
 		}
 		loadMethods( this.name);
 	}
 
 	public boolean getPhysicsEnabled(){
 		return physics_enabled;
 	}
 	public void setPhysicsEnabled( boolean enabled){
 		physics = enabled;
 		physics_enabled = enabled;
 	}
 
 	public void invokeDraw(){
 		//setup
 		this.setDelegate( SMT.renderer);
 		if( ! this.isDirect()){
 			extraGraphicsNullCheck();
 			SMT.renderer.pushDelegate( extra_graphics);
 			extra_graphics.beginDraw();
 			extra_graphics.clear();
 		}
 		beginDraw();
 
 		//push transformations
 		if( this.isDirect()){
 			pushMatrix();
 			applyMatrix( matrix);
 		}
 
 		//drawing setup
 		pushStyle();
 
 		//invoke proper draw method
 		if( direct){
 			if( method_draw != null)
 				SMTUtilities.invoke( method_draw, applet, this);
 			else if( drawImpl_overridden)
 				drawImpl();
 			else
 				draw();
 		}
 
 		//drawing cleanup
 		popStyle();
 
 		//draw children
 		for( Zone child : children)
 			child.invokeDraw();
 
 		//pop transformations
 		if( this.isDirect())
 			popMatrix();
 
 		//if we're indirect, we can now draw for realz
 		if( ! this.isDirect()){
 			extra_graphics.endDraw();
 			SMT.renderer.popDelegate();
 			//push transformations
 			pushMatrix();
 			applyMatrix( matrix);
 			//draw extra_graphics
 			pushStyle();
 			noTint();
 			image( extra_graphics,
 				0, 0, dimension.width, dimension.height);
 			noFill();
 			stroke( 220, 220, 220, 180);
 			rect( 0, 0, dimension.width, dimension.height);
 			popStyle();
 			popMatrix();
 		}
 		
 		//cleanup
 		endDraw();
 		this.setDelegate( null);
 	}
 
 	public void invokePickDraw(){
 		//setup
 		//all calls need to be redirected through this so that color calls can be discarded and background calls changed
 		SMT.renderer.pushDelegate( this);
 		if( this.isDirect())
 			this.setDelegate(
 				parent != null ?
 					parent : SMT.picker.picking_context);
 		else {
 			extraGraphicsNullCheck();
 			this.setDelegate( extra_graphics);
 			extra_graphics.beginDraw();
 			extra_graphics.clear();
 		}
 		beginPickDraw();
 
 		//push transformations
 		if( this.isDirect()){
 			pushMatrix();
 			applyMatrix( matrix);
 		}
 
 		//picking setup
 		pushStyle();
 		colorMode( RGB, 255, 255, 255, 255);
 		noStroke();
 		if( pickColor != null)
 			fill(
 				pickColor.getRed(),
 				pickColor.getGreen(),
 				pickColor.getBlue(),
 				255);
 		else
 			noFill();
 		picking_on = true;
 
 		//invoke proper draw method
 		if( method_pickDraw != null)
 			SMTUtilities.invoke( method_pickDraw, applet, this);
 		else if( pickDrawImpl_overridden)
 			pickDrawImpl();
 		else
 			pickDraw();
 
 		//picking cleanup
 		picking_on = false;
 		popStyle();
 
 		//draw children
 		for( Zone child : children)
 			child.invokePickDraw();
 
 		//pop transformations
 		if( this.isDirect())
 			popMatrix();
 
 		//if we're indirect, we can now draw for realz
 		if( ! this.isDirect()){
 			extra_graphics.endDraw();
 			this.setDelegate(
 				parent != null ?
 					parent : SMT.picker.picking_context);
 			//push transformations
 			pushMatrix();
 			applyMatrix( matrix);
 			//draw extra_graphics
 			pushStyle();
 			noTint();
 			image( extra_graphics,
 				0, 0, dimension.width, dimension.height);
 			popStyle();
 			popMatrix();
 		}
 		
 		//cleanup
 		endPickDraw();
 		SMT.renderer.popDelegate();
 		this.setDelegate( null);
 	}
 
 	public void draw(){
 		fill( 50, 50, 50, 150);
 		stroke( 255, 255, 255, 150);
 		rect( 0, 0, width, height);
 		fill( 255, 255, 255, 150);
 		text("No Draw Method", 0, 0, width, height);
 	}
 	public void pickDraw(){
 		rect( 0, 0, width, height);
 	}
 
 	public void beginDraw(){}
 	public void beginPickDraw(){}
 	public void endDraw(){}
 	public void endPickDraw(){}
 
 
 	protected void touch(){
 		if( ! touchUpList.isEmpty() || ! touchDownList.isEmpty() ||
 				! touchMovedList.isEmpty() || ! pressList.isEmpty()){
 			for( Touch touch : touchUpList)
 				touchUpInvoker( touch);
 			for( Touch touch : pressList)
 				pressInvoker( touch);
 			for( Touch touch : touchDownList)
 				touchDownInvoker( touch);
 			for( Touch touch : touchMovedList)
 				touchMovedInvoker( touch);
 			if( ! touchMovedList.isEmpty()){
 				touchImpl();
 				SMTUtilities.invoke( method_touch, applet, this);
 			}
 			
 			if( method_touch == null && ! touchImpl_overridden &&
 					method_touchUp == null && method_touchDown == null &&
 					method_touchMoved == null && ! touchUDM_overridden &&
 					! pressImpl_overridden && method_press == null)
 				drag();
 
 			touchUpList.clear();
 			touchDownList.clear();
 			touchMovedList.clear();
 			pressList.clear();
 		}
 
 		for( Zone child : children) {
 			if (child.isChildActive()) {
 				child.backupMatrix = child.matrix.get();
 				child.matrix.apply( this.matrix);
 				child.touch();
 				child.backupMatrix = null;
 
 				PMatrix3D inverse = new PMatrix3D();
 				inverse.apply( this.matrix);
 				inverse.invert();
 				child.matrix.apply(inverse);
 			}
 		}
 	}
 
 
 	/**
 	 * @param name The name of the zone to load the methods from
 	 */
 	protected void loadMethods( String name) {
 		if ( SMT.warnUnimplemented != null) {
 			if ( SMT.warnUnimplemented.booleanValue())
 				loadMethods( name, true, true, true, true, true, true);
 			else
 				loadMethods( name, false, false, false, false, false, false);
 		}
 		else 
 			loadMethods(
 				name,
 				warnDraw(), warnTouch(), warnKeys(),
 				warnPick(), warnTouchUDM(), warnPress());
 	}
 
 	/**
 	 * @param name The name of the zone to load methods from, used as a suffix
 	 * @param warnDraw Display a warning when the draw method doesn't exist
 	 * @param warnTouch Display a warning when the touch method doesn't exist
 	 * @param warnKeys Display a warning when the keyTyped/keyPressed/keyReleased methods don't exist
 	 * @param warnPick Display a warning when the pickDraw method doesn't exist
 	 * @param warnTouchUDM Display a warning when the touchUp/touchDown/touchMoved methods don't exist
 	 * @param warnPress Display a warning when the touchPress methods don't exist
 	 */
 	protected void loadMethods(
 			String name, boolean warnDraw, boolean warnTouch, boolean warnKeys,
 			boolean warnPick, boolean warnTouchUDM, boolean warnPress) {
 
 		touchUDM_overridden =
 			SMTUtilities.checkImpl( Zone.class, "touchDown", this.getClass(), Touch.class) ||
 			SMTUtilities.checkImpl( Zone.class, "touchUp", this.getClass(), Touch.class) ||
 			SMTUtilities.checkImpl( Zone.class, "touchMoved", this.getClass(), Touch.class);
 
 		//check for extended class implementations
 		pressImpl_overridden =
 			SMTUtilities.checkImpl( Zone.class, "press", this.getClass(), Touch.class);
 		touchImpl_overridden =
 			SMTUtilities.checkImpl( Zone.class, "touch", this.getClass());
 		drawImpl_overridden =
 			SMTUtilities.checkImpl( Zone.class, "draw", this.getClass());
 		pickDrawImpl_overridden =
 			SMTUtilities.checkImpl( Zone.class, "pickDraw", this.getClass());
 
 		//check draw/pick draw
 		Class<?> current_class = this.getClass();
 		try {
 			Method actual_draw = current_class.getMethod( "draw");
 			Class<?> actual_draw_class = actual_draw.getDeclaringClass();
 			draw_overridden = actual_draw_class != Zone.class;}
 		//wtf why?
 		catch( NoSuchMethodException exception){}
 		try {
 			Method actual_pickDraw = current_class.getMethod( "pickDraw");
 			Class<?> actual_pickDraw_class = actual_pickDraw.getDeclaringClass();
 			pickDraw_overridden = actual_pickDraw_class != Zone.class;}
 		//wtf why?
 		catch( NoSuchMethodException exception){}
 
 		/*/debug
 		System.out.printf( "draw_overridden: %b\n", draw_overridden);
 		System.out.printf( "pickDraw_overridden: %b\n", pickDraw_overridden);
 		System.out.printf( "drawImpl_overridden: %b\n", drawImpl_overridden);
 		System.out.printf( "pickDrawImpl_overridden: %b\n", pickDrawImpl_overridden);
 		System.out.println();*/
 
 		if (name != null) {
 			//get draw method
 			method_draw = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "draw", name, warnDraw,
 				this.getClass());
 
 			//get pick draw method
 			method_pickDraw = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "pickDraw", name, warnPick,
 				this.getClass());
 
 			//get key methods
 			method_keyPressed = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "keyPressed", name, warnKeys,
 				this.getClass(), KeyEvent.class);
 			method_keyReleased = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "keyReleased", name, warnKeys,
 				this.getClass(), KeyEvent.class);
 			method_keyTyped = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "keyTyped", name, warnKeys,
 				this.getClass(), KeyEvent.class);
 
 			//get touch methods
 			method_touchUp = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "touchUp", name, warnTouchUDM,
 				this.getClass(), Touch.class);
 			method_touchDown = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "touchDown", name, warnTouchUDM,
 				this.getClass(), Touch.class);
 			method_touchMoved = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "touchMoved", name, warnTouchUDM,
 				this.getClass(), Touch.class);
 
 			//get press method
 			method_press = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "press", name, warnPress,
 				this.getClass(), Touch.class);
 
 			//get touch method
 			if( method_touchUp != null || method_touchDown != null ||
 					method_touchMoved != null || touchUDM_overridden ||
 					pressImpl_overridden || method_press != null) 
 				warnTouch = false;
 			method_touch = SMTUtilities.getZoneMethod(
 				Zone.class, applet, "touch", name, warnTouch,
 					this.getClass());
 		}
 
 		this.setCaptureTouchesEnabled(
 			pressImpl_overridden || method_press != null);
 	}
 
 	/**
 	 * @return The name of the zone
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return A Collection<Touch> containing all touches that are active on the
 	 *         zone
 	 */
 	public Collection<Touch> getTouchCollection() {
 		return Collections.unmodifiableCollection(activeTouches.values());
 	}
 
 	/**
 	 * @return A Touch[] containing all touches that are active on the zone
 	 */
 	public Touch[] getTouches() {
 		return activeTouches.values().toArray( new Touch[0]);
 	}
 
 	/**
 	 * @return A Set<Long> containing the long id's of the zone's touches
 	 */
 	public Set<Long> getIds() {
 		return Collections.unmodifiableSet(activeTouches.keySet());
 	}
 
 	/**
 	 * @return A Map<Long, Touch> which maps each touch id to the touch for the
 	 *         zones active touches
 	 */
 	public Map<Long, Touch> getTouchMap() {
 		return Collections.unmodifiableMap(activeTouches);
 	}
 
 	/**
 	 * @return The number of touches currently on the zone
 	 */
 	public int getNumTouches() {
 		return activeTouches.size();
 	}
 
 	/**
 	 * @return Whether the zone has touches currently on it
 	 */
 	public boolean isActive() {
 		return !activeTouches.isEmpty() || !touchUpList.isEmpty() || !touchDownList.isEmpty()
 				|| !touchMovedList.isEmpty() || !pressList.isEmpty();
 	}
 
 	/**
 	 * @return Whether the zone or one of its children has touches currently on
 	 *         it
 	 */
 	public boolean isChildActive() {
 		if (isActive()) {
 			return true;
 		}
 
 		for (Zone child : children) {
 			if (child.isChildActive()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Assigns a touch to this zone, maintaining the order of touches.
 	 * 
 	 * @param touches
 	 *            A number of Touch objects, variable number of arguments
 	 */
 	public void assign(Touch... touches) {
 		assign(Arrays.asList(touches));
 	}
 
 	/**
 	 * Assigns a touch to this zone, maintaining the order of touches.
 	 * 
 	 * @param touches
 	 *            A number of Touch objects, in an Iterable object that contains
 	 *            Touch objects
 	 */
 	public void assign(Iterable<? extends Touch> touches) {
 		for (Touch touch : touches) {
 			activeTouches.put(touch.sessionID, touch);
 			touch.assignZone(this);
 		}
 	}
 
 	/**
 	 * @param touch
 	 * @return Whether the given touch is assigned to this zone
 	 */
 	public boolean isAssigned(Touch touch) {
 		return isAssigned(touch.sessionID);
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @return Whether the Touch corresponding to the given id is assigned to
 	 *         this zone
 	 */
 	public boolean isAssigned(long id) {
 		return activeTouches.containsKey(id);
 	}
 
 	/**
 	 * Unassigns the given Touch from this zone, removing it from activeTouches.
 	 * 
 	 * @param touch
 	 */
 	public void unassign(Touch touch) {
 		unassign(touch.sessionID);
 	}
 
 	/**
 	 * Unassigns all Touch objects from this zone, clearing activeTouches.
 	 */
 	public void unassignAll() {
 		long[] touchids = new long[activeTouches.keySet().size()];
 		int i = 0;
 		for (long id : activeTouches.keySet()) {
 			touchids[i] = id;
 			i++;
 		}
 
 		for (long id : touchids) {
 			unassign(id);
 		}
 	}
 
 	/**
 	 * Unassigns the Touch corresponding to the sessionID given from this zone,
 	 * removing it from activeTouches.
 	 * 
 	 * @param sessionID
 	 */
 	public void unassign(long sessionID) {
 		Touch t = activeTouches.get(sessionID);
 		// only removes if it exists in the touch mapping
 		if (t != null) {
 			activeTouches.remove(sessionID);
 			t.unassignZone(this);
 			// at unassign if we have a mJoint destroy it
 			if (mJoint != null) {
 				SMT.world.destroyJoint(mJoint);
 				mJoint = null;
 			}
 		}
 	}
 
 	/**
 	 * Call this to before matrix operations on a zone to make them apply
 	 * immediately
 	 * <P>
 	 * Needs a matching endTouch() call to be made afterwards, otherwise the
 	 * matrix stack will overflow with too many pushMatrix commands.
 	 * <P>
 	 * Override this if something needs to occur before touch commands
 	 */
 	/*public void beginTouch() {
 		pushMatrix();
 	}*/
 
 	/**
 	 * Call this to before matrix operations on a zone to make them apply
 	 * immediately
 	 * <P>
 	 * Needs a matching beginTouch() call to be made beforehand, otherwise the
 	 * matrix stack will underflow with too many popMatrix commands.
 	 * <P>
 	 * Override this if something needs to occur after touch commands
 	 */
 	/*public void endTouch() {
 		matrix.preApply((PMatrix3D) getMatrix());
 		popMatrix();
 	}*/
 
 	public Color getPickColor() {
 		return pickColor;
 	}
 
 	public void setPickColor( Color color) {
 		this.pickColor = color;
 	}
 
 	public void removePickColor() {
 		pickColor = null;
 	}
 	
 	public boolean add( Zone... zones){
 		boolean result = true;
 		for( Zone zone : zones)
 			if( ! add( zone))
 				result = false;
 		return result;
 	}
 
 	/**
 	 * @param zone
 	 *            The zone to add to this zone
 	 * @return Whether the zone was successfully added or not
 	 */
 	public boolean add( Zone zone) {
 		if( zone != null) {
 			if( zone != this && ! isAncestor( zone)){
 				zone.removeFromParent();
 				zone.parent = this;
 				
 				SMT.picker.add(zone);
 				
 				// make sure the matrix is up to date, these calls should not
 				// occur if we do not call begin/endTouch once per
 				// frame and once at Zone initialization
 				//zone.endTouch();
 				//zone.beginTouch();
 				
 				if (!children.contains(zone)) {
 					return children.add(zone);
 				}
 			}
 			else
 				try { throw new RuntimeException(
 					"Warning: Added a Zone to itself or an ancestor");}
 				catch( RuntimeException exception){
 					exception.printStackTrace();}
 		}
 		else throw new NullPointerException(
 			"Tried to add null to a zone");
 		return false;
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
 	public Zone[] addXMLZone(String xmlFilename) {
 		List<Zone> zoneList = new ArrayList<Zone>();
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			Document doc = db.parse(applet.createInput(xmlFilename));
 
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
 	
 	private void add(NodeList zones, List<Zone> zoneList) {
 		for (int i = 0; i < zones.getLength(); i++) {
 			Node zoneNode = zones.item(i);
 			if( zoneNode.getNodeType() == Node.ELEMENT_NODE
 					&& zoneNode.getNodeName().equalsIgnoreCase("zone")) {
 				add(zoneNode, zoneList);
 			}
 		}
 	}
 
 	private void add(Node node, List<Zone> zoneList) {
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
 			PImage img = applet.loadImage(imgFilename);
 
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
 	 * @param zone
 	 * @return Whether the given zone is an ancestor of this one.
 	 */
 	public boolean isAncestor(Zone zone) {
 		for(
 				Zone ancestor = zone.getParent();
 				ancestor != null;
 				ancestor = ancestor.getParent())
 			if(ancestor == zone)
 				return true;
 		return false;
 	}
 
 	/**
 	 * Removes this zone from its parent
 	 */
 	public void removeFromParent() {
 		if(parent != null && parent.children.contains(this))
 			parent.remove(this);
 		else if( SMT.debug)
 			System.err.println( "Warning: removeFromParent where parent is null or this zone is not a child of");
 	}
 	
 	public boolean remove(Zone... zones){
 		boolean r = true;
 		for (Zone z : zones)
 			if(!remove(z))
 				r = false;
 		return r;
 	}
 
 	/**
 	 * @param child
 	 *            The child of this zone to remove
 	 * @return Whether the zone was successfully removed or not
 	 */
 	public boolean remove(Zone child) {
 		if (child != null){
 			if(children.contains(child)) {
 				child.cleanUp();
 				child.parent = null;
 				return children.remove(child);
 			} else
 				System.err.println("Warning: Removed a Zone that was not a child");
 		} else
 			System.err.println("Warning: Removed a null Zone");
 		return false;
 	}
 	
 	/**
 	 * This cleans up (unassign touches, remove from picker, and remove zoneBody) the Zone and its children
 	 */
 	private void cleanUp() {
 		for (Zone child : getChildren())
 			child.cleanUp();
 		
 		SMT.picker.remove(this);
 
 		// clear the touches from the Zone, so that it doesn't get pulled in by
 		// the touches such as when putZoneOnTop() is called
 		unassignAll();
 
 		// destroy the Zones Body, so it does not collide with other Zones any
 		// more
 		if (zoneBody != null) {
 			SMT.world.destroyBody(zoneBody);
 			zoneBody = null;
 			zoneFixture = null;
 			mJoint = null;
 		}
 	}
 
 	/**
 	 * This disconnects this zone from its children and vice versa
 	 */
 	public void clearChildren() {
 		for( Zone zone : children) {
 			this.remove(zone);
 		}
 	}
 
 	/**
 	 * @param index
 	 * @return The child at the given index, or null if the index is invalid
 	 */
 	public Zone getChild( int index) {
 		try {
 			return children.get(index);
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * @return The number of children of this zone
 	 */
 	public int getChildCount() {
 		return children.size();
 	}
 
 	/**
 	 * @return An array containing this zone's children
 	 */
 	public Zone[] getChildren() {
 		return Collections.unmodifiableList(children).toArray(new Zone[getChildCount()]);
 	}
 
 	/**
 	 * @return The zone that is the parent of this zone
 	 */
 	public Zone getParent() {
 		return parent;
 	}
 
 	/**
 	 * Reset the transformation matrix of the zone
 	 */
 	@Override
 	public void resetMatrix() {
 		matrix.reset();
 		matrix.translate(x, y);
 	}
 
 	/**
 	 * Changes the coordinates and size of the zone.
 	 * 
 	 * @param x int The zone's new x-coordinate.
 	 * @param y int The zone's new y-coordinate.
 	 * @param width int The zone's new width.
 	 * @param height int The zone's new height.
 	 */
 	@Deprecated
 	public void setData(int x, int y, int width, int height) {
 		this.setSize( width, height);
 		this.setLocation( x, y);
 	}
 
 	/**
 	 * Moves the zone to a given location with a reset matrix
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void setLocation(float x, float y) {
 		this.x = Math.round( x);
 		this.y = Math.round( y);
 		if (getParent() == null)
 			resetMatrix();
 		else {
 			this.matrix.reset();
 			this.matrix.translate(x, y);
 			// If we are in our own touch (which we can tell by if backupMatrix
 			// is set), we need to apply our parents matrix too after the reset,
 			// since it was applied before and will be inverted.
 			if (backupMatrix != null)
 				this.matrix.preApply(getParent().matrix);
 		}
 	}
 	
 	@Override
 	public void setSize(int width, int height){
 		super.setSize( width, height);
 		this.width = width;
 		this.height = height;
 		this.dimension = new Dimension( width, height);
 		this.halfDimension = new Dimension( width / 2, height / 2);
 	}
 
 	public void setWidth( int width){
 		super.setSize( width, dimension.height);
 		this.width = width;
 		this.dimension.width = width;
 		this.halfDimension.width = width / 2;
 	}
 	public void setHeight( int height){
 		super.setSize( dimension.width, height);
 		this.height = height;
 		this.dimension.height = height;
 		this.halfDimension.height = height / 2;
 	}
 
 	/**
 	 * Get the zone x-coordinate. Upper left corner for rectangle.
 	 * @return x int representing the upper left x-coordinate of the zone.
 	 */
 	public int getX() {
 		return (int) this.getOrigin().x;
 	}
 
 	/**
 	 * Get the zone y-coordinate. Upper left corner for rectangle.
 	 * @return y int representing the upper left y-coordinate of the zone.
 	 */
 	public int getY() {
 		return (int) this.getOrigin().y;
 	}
 
 	/**
 	 * Get the zone's original width.
 	 * @return width int representing the width of the zone.
 	 */
 	public int getWidth() {
 		return (int) PVector.sub(
 			fromZoneVector(
 				new PVector(this.width, 0)),
 			this.getOrigin()).mag();
 	}
 
 	/**
 	 * Get the zone's original height.
 	 * @return height int representing the height of the zone.
 	 */
 	public int getHeight() {
 		return (int) PVector.sub(
 			fromZoneVector(
 				new PVector(0, this.height)),
 			this.getOrigin()).mag();
 	}
 
 	public Dimension getSize(){
 		return dimension;
 	}
 
 	public Dimension getHalfSize(){
 		return halfDimension;
 	}
 
 	/**
 	 * @return The rotation radius of the zone, which is used by at least rnt()
 	 *         for now, controlling when rotation is done
 	 */
 	public float getRntRadius() {
 		return rntRadius;
 	}
 
 	/**
 	 * @param rntRadius
 	 *            The new rotation radius of the zone, which is used by at least
 	 *            rnt() for now, controlling when rotation is done
 	 */
 	public void setRntRadius(float rntRadius) {
 		this.rntRadius = rntRadius;
 	}
 
 	/**
 	 * Tests to see if the x and y coordinates are in the zone.
 	 * 
 	 * @param x
 	 *            float - X-coordinate to test
 	 * @param y
 	 *            float - Y-coordinate to test
 	 * @return boolean True if x and y is in the zone, false otherwise.
 	 */
 	public boolean contains(float x, float y) {
 		PVector mouse = new PVector(x, y);
 		PVector world = this.toZoneVector(mouse);
 		return
 			(world.x > 0) && (world.x < this.width) &&
 			(world.y > 0) && (world.y < this.height);
 	}
 
 	//touch methods
 
 	/**
 	 * Translates the zone, its group, and its children
 	 */
 	public void drag() {
 		drag( true, true);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param dragX
 	 *            Whether to drag along the x-axis
 	 * @param dragY
 	 *            Whether to drag along the y-axis
 	 */
 	public void drag( boolean dragX, boolean dragY) {
 		drag( dragX, dragX, dragY, dragY);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param dragLeft
 	 *            Allow dragging left
 	 * @param dragRight
 	 *            Allow dragging Right
 	 * @param dragUp
 	 *            Allow dragging Up
 	 * @param dragDown
 	 *            Allow dragging Down
 	 */
 	public void drag(
 			boolean dragLeft, boolean dragRight,
 			boolean dragUp, boolean dragDown) {
 		drag(
 			dragLeft, dragRight, dragUp, dragDown,
 			Integer.MIN_VALUE, Integer.MAX_VALUE,
 			Integer.MIN_VALUE, Integer.MAX_VALUE);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param dragLeft
 	 *            Allow dragging left
 	 * @param dragRight
 	 *            Allow dragging Right
 	 * @param dragUp
 	 *            Allow dragging Up
 	 * @param dragDown
 	 *            Allow dragging Down
 	 * @param leftLimit
 	 *            Limit on how far to be able to drag left
 	 * @param rightLimit
 	 *            Limit on how far to be able to drag right
 	 * @param upLimit
 	 *            Limit on how far to be able to drag up
 	 * @param downLimit
 	 *            Limit on how far to be able to drag down
 	 */
 	public void drag(
 			boolean dragLeft, boolean dragRight, boolean dragUp, boolean dragDown,
 			int leftLimit, int rightLimit, int upLimit, int downLimit) {
 		if (!activeTouches.isEmpty()) {
 			List<TouchPair> pairs = getTouchPairs(1);
 			drag( pairs.get(0),
 				dragLeft, dragRight, dragUp, dragDown,
 				leftLimit, rightLimit, upLimit, downLimit);
 		}
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param dragX
 	 *            Allow dragging along the x-axis
 	 * @param dragY
 	 *            Allow dragging along the y-axis
 	 * @param leftLimit
 	 *            Limit on how far to be able to drag left
 	 * @param rightLimit
 	 *            Limit on how far to be able to drag right
 	 * @param upLimit
 	 *            Limit on how far to be able to drag up
 	 * @param downLimit
 	 *            Limit on how far to be able to drag down
 	 */
 	public void drag(
 			boolean dragX, boolean dragY,
 			int leftLimit, int rightLimit, int upLimit, int downLimit) {
 		drag(
 			dragX, dragX, dragY, dragY,
 			leftLimit, rightLimit, upLimit, downLimit);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param from
 	 *            The Touch to drag from
 	 * @param to
 	 *            The Touch to drag to
 	 */
 	public void drag( Touch from, Touch to) {
 		drag( from, to, true, true);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param from
 	 *            The Touch to drag from
 	 * @param to
 	 *            The Touch to drag to
 	 * @param dragX
 	 *            Whether to drag along the x-axis
 	 * @param dragY
 	 *            Whether to drag along the y-axis
 	 */
 	public void drag(Touch from, Touch to, boolean dragX, boolean dragY) {
 		drag(from, to, dragX, dragX, dragY, dragY);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param from
 	 *            The Touch to drag from
 	 * @param to
 	 *            The Touch to drag to
 	 * @param dragLeft
 	 *            Allow dragging left
 	 * @param dragRight
 	 *            Allow dragging Right
 	 * @param dragUp
 	 *            Allow dragging Up
 	 * @param dragDown
 	 *            Allow dragging Down
 	 */
 	public void drag(Touch from, Touch to, boolean dragLeft, boolean dragRight, boolean dragUp,
 			boolean dragDown) {
 		drag(new TouchPair(from, to), dragLeft, dragRight, dragUp, dragDown);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param pair
 	 *            The TouchPair to drag to/from
 	 * @param dragLeft
 	 *            Allow dragging left
 	 * @param dragRight
 	 *            Allow dragging Right
 	 * @param dragUp
 	 *            Allow dragging Up
 	 * @param dragDown
 	 *            Allow dragging Down
 	 */
 	protected void drag(TouchPair pair, boolean dragLeft, boolean dragRight, boolean dragUp,
 			boolean dragDown) {
 		drag(pair, dragLeft, dragRight, dragUp, dragDown, Integer.MIN_VALUE, Integer.MAX_VALUE,
 				Integer.MIN_VALUE, Integer.MAX_VALUE);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param pair
 	 *            The TouchPair to drag to/from
 	 * @param dragLeft
 	 *            Allow dragging left
 	 * @param dragRight
 	 *            Allow dragging Right
 	 * @param dragUp
 	 *            Allow dragging Up
 	 * @param dragDown
 	 *            Allow dragging Down
 	 * @param leftLimit
 	 *            Limit on how far to be able to drag left
 	 * @param rightLimit
 	 *            Limit on how far to be able to drag right
 	 * @param upLimit
 	 *            Limit on how far to be able to drag up
 	 * @param downLimit
 	 *            Limit on how far to be able to drag down
 	 */
 	protected void drag(
 			TouchPair pair,
 			boolean dragLeft, boolean dragRight, boolean dragUp, boolean dragDown,
 			int leftLimit, int rightLimit, int upLimit, int downLimit) {
 		if (pair.matches()) {
 			lastUpdate = maxTime(pair);
 			return;
 		}
 		float tx = pair.to.x;
 		float ty = pair.to.y;
 		if(getParent() != null){
 			tx = getParent().getLocalX( pair.to);
 			ty = getParent().getLocalY( pair.to);
 		}
 		
 		if(!dragSeenTouch.contains(pair.from.sessionID)){
 			//the first time we see a touch we use its position for the offset
 			//of the Zone from the touch
 			offsetX = pair.from.x - getLocalX();
 			offsetY = pair.from.y - getLocalY();
 			if(getParent() != null){
 				offsetX = getParent().getLocalX( pair.from) - getLocalX();
 				offsetY = getParent().getLocalY( pair.from) - getLocalY();
 			}
 			dragSeenTouch.add(pair.from.sessionID);
 		}
 		
 		if(!dragUp)
 			upLimit = (int) getLocalY();
 		if(!dragDown)
 			downLimit = (int) getLocalY();
 		if(!dragLeft)
 			leftLimit = (int) getLocalX();
 		if(!dragRight)
 			rightLimit = (int) getLocalX();
 		if(dragUp || dragDown)
 			setY( Math.max( upLimit, Math.min( downLimit - height, ty - offsetY)));
 		if(dragLeft || dragRight)
 			setX( Math.max( leftLimit, Math.min( rightLimit - width, tx - offsetX)));
 		
 		lastUpdate = maxTime(pair);
 	}
 
 	/**
 	 * * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param fromX
 	 *            The x value to drag from
 	 * @param fromY
 	 *            The y value to drag from
 	 * @param toX
 	 *            The x value to drag to
 	 * @param toY
 	 *            The y value to drag to
 	 */
 	public void drag(int fromX, int fromY, int toX, int toY) {
 		drag(fromX, fromY, toX, toY, true, true);
 	}
 
 	/**
 	 * Performs translate on the current graphics context. Should typically be
 	 * called inside a {@link Zone#beginTouch()} and {@link Zone#endTouch()}.
 	 * 
 	 * @param fromX
 	 *            The x value to drag from
 	 * @param fromY
 	 *            The y value to drag from
 	 * @param toX
 	 *            The x value to drag to
 	 * @param toY
 	 *            The y value to drag to
 	 * @param dragX
 	 *            Whether to drag along the x-axis
 	 * @param dragY
 	 *            Whether to drag along the y-axis
 	 */
 	public void drag(
 			int fromX, int fromY, int toX, int toY, boolean dragX, boolean dragY) {
 		if (dragX)
 			translate(toX - fromX, 0);
 		if (dragY)
 			translate(0, toY - fromY);
 	}
 
 	/**
 	 * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 */
 	public void rst() {
 		rst(true, true, true, true);
 	}
 
 	/**
 	 * * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param rotate
 	 *            true if rotation should happen
 	 * @param scale
 	 *            true if scale should happen
 	 * @param translate
 	 *            true if translation should happen
 	 */
 	public void rst(boolean rotate, boolean scale, boolean translate) {
 		rst(rotate, scale, translate, translate);
 	}
 
 	/**
 	 * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param rotate
 	 *            true if rotation should happen
 	 * @param scale
 	 *            true if scale should happen
 	 * @param translateX
 	 *            true if x-translation should happen
 	 * @param translateY
 	 *            true if y-translation should happen
 	 */
 	public void rst(boolean rotate, boolean scale, boolean translateX, boolean translateY) {
 		if (!activeTouches.isEmpty()) {
 			List<TouchPair> pairs = getTouchPairs(2);
 			rst(pairs.get(0), pairs.get(1), rotate, scale, translateX, translateY);
 		}
 	}
 
 	/**
 	 * * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param from1
 	 * @param from2
 	 * @param to1
 	 * @param to2
 	 */
 	public void rst(Touch from1, Touch from2, Touch to1, Touch to2) {
 		rst(from1, from2, to1, to2, true, true, true);
 	}
 
 	/**
 	 * * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param from1
 	 * @param from2
 	 * @param to1
 	 * @param to2
 	 * @param rotate
 	 * @param scale
 	 * @param translate
 	 */
 	public void rst(Touch from1, Touch from2, Touch to1, Touch to2, boolean rotate, boolean scale,
 			boolean translate) {
 		rst(from1, from2, to1, to2, rotate, scale, translate, translate);
 	}
 
 	/**
 	 * * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param from1
 	 * @param from2
 	 * @param to1
 	 * @param to2
 	 * @param rotate
 	 * @param scale
 	 * @param translateX
 	 * @param translateY
 	 */
 	public void rst(Touch from1, Touch from2, Touch to1, Touch to2, boolean rotate, boolean scale,
 			boolean translateX, boolean translateY) {
 		rst(new TouchPair(from1, to1), new TouchPair(from2, to2), rotate, scale, translateX,
 				translateY);
 	}
 
 	/**
 	 * * Performs rotate/scale/translate on the current graphics context. Should
 	 * typically be called inside a {@link Zone#beginTouch()} and
 	 * {@link Zone#endTouch()}.
 	 * 
 	 * @param first
 	 *            The first TouchPair
 	 * @param second
 	 *            The second TouchPair
 	 * @param rotate
 	 * @param scale
 	 * @param translateX
 	 * @param translateY
 	 */
 	protected void rst(
 			TouchPair first, TouchPair second,
 			boolean rotate, boolean scale,
 			boolean translateX, boolean translateY) {
 
 		if (first.matches() && second.matches()) {
 			// nothing to do
 			lastUpdate = maxTime( first, second);
 			return;
 		}
 
 		if (first.from == null) {
 			first.from = first.to;
 		}
 
 		// PMatrix3D matrix = new PMatrix3D();
		/*if( translateX || translateY){
 			if( translateX){
 				matrix.translate(first.to.x, 0);
 			}
 			if( translateY){
 				matrix.translate(0, first.to.y);
 			}
 		}
 		else{
			matrix.translate( this.x + this.width / 2, this.y + this.height / 2);
 			// TODO: even better, add a centreOfRotation parameter
 			// TODO: even more better, add a moving vs. non-moving
 			// centreOfRotation
		}*/
 
 		if (!second.isEmpty() && !second.isFirst() && (rotate || scale)) {
 			PVector fromVec = second.getFromVec();
 			fromVec.sub(first.getFromVec());
 
 			PVector toVec = second.getToVec();
 			toVec.sub(first.getToVec());
 
 			float toDist = toVec.mag();
 			float fromDist = fromVec.mag();
 			if (toDist > 0 && fromDist > 0) {
 				float angle = PVector.angleBetween(fromVec, toVec);
 				PVector cross = PVector.cross(fromVec, toVec, new PVector());
 				cross.normalize();
 
 				if (angle != 0 && cross.z != 0 && rotate) {
 					matrix.rotate(angle, cross.x, cross.y, cross.z);
 				}
 				if (scale) {
 					float ratio = toDist / fromDist;
 					if (this.scalingLimit) {
 						// Limits have to be consistent; i.e. it should be
 						// possible
 						// to maintain aspect ratio while fulfilling all
 						// requirements
 						float w = this.getWidth();
 						float h = this.getHeight();
 						if (w * ratio > this.maxWidth) {
 							ratio = this.maxWidth / w;
 						}
 						else if (w * ratio < this.minWidth) {
 							ratio = this.minWidth / w;
 						}
 						if (h * ratio > this.maxHeight) {
 							ratio = this.maxHeight / h;
 						}
 						else if (h * ratio < this.minHeight) {
 							ratio = this.minHeight / h;
 						}
 					}
 					matrix.scale(ratio);
 				}
 			}
 		}
 
		/*if (translateX || translateY) {
 			if (translateX) {
 				matrix.translate(-first.from.x, 0);
 			}
 			if (translateY) {
 				matrix.translate(0, -first.from.y);
 			}
 		}
 		else {
 			matrix.translate(
 				- (this.x + this.width / 2),
 				- (this.y + this.height / 2));
		}*/
 
 		lastUpdate = maxTime(first, second);
 	}
 
 	/**
 	 * Rotates and scales the zone. Uses two touches. Only works inside the
 	 * zone's touch method, or between calls to beginTouch() and endTouch()
 	 */
 	public void rs() {
 		rst(true, true, false);
 	}
 
 	/**
 	 * Scales the zone. Uses two touches Only works inside the zone's touch
 	 * method, or between calls to beginTouch() and endTouch()
 	 */
 	public void pinch() {
 		rst(false, true, true);
 	}
 
 	/**
 	 * Scales the zone. Uses 4 Touch objects, specifiying the from and to for
 	 * two moving touchpoints
 	 * 
 	 * @param from1
 	 * @param from2
 	 * @param to1
 	 * @param to2
 	 */
 	public void pinch(Touch from1, Touch from2, Touch to1, Touch to2) {
 		rst(from1, from2, to1, to2, false, true, true);
 	}
 
 	/**
 	 * Rotates the zone, using two touches, about the centre Only works inside
 	 * the zone's touch method, or between calls to beginTouch() and endTouch()
 	 */
 	public void rotate() {
 		rst(true, false, false);
 	}
 
 	/**
 	 * Rotates the zone. Uses 4 Touch objects, specifiying the from and to for
 	 * two moving touchpoints
 	 * 
 	 * @param from1
 	 * @param from2
 	 * @param to1
 	 * @param to2
 	 */
 	public void rotate(Touch from1, Touch from2, Touch to1, Touch to2) {
 		rst(from1, from2, to1, to2, true, false, false);
 	}
 
 	/**
 	 * Rotates the zone around the specified x- & y-coordinates
 	 * 
 	 * @param angle
 	 *            The angle to rotate specified in radians
 	 * @param x
 	 * @param y
 	 */
 	public void rotateAbout(float angle, int x, int y) {
 		matrix.translate( x, y);
 		matrix.rotate( angle);
 		matrix.translate( - x, - y);
 	}
 
 	/**
 	 * Rotates the zone around either the centre or corner
 	 * 
 	 * @param angle
 	 *            The angle to rotate specified in radians
 	 * @param mode
 	 *            CENTER or CORNER
 	 */
 	public void rotateAbout(float angle, int mode) {
 		if (mode == CORNER) {
 			rotateAbout( angle, 0, 0);
 		}
 		else if (mode == CENTER) {
 			rotateAbout( angle, width / 2, height / 2);
 		}
 	}
 
 	/**
 	 * Horizontal Swipe. Uses a single touch. Only works inside the zone's touch
 	 * method, or between calls to beginTouch() and endTouch()
 	 */
 	public void hSwipe() {
 		drag(true, false);
 	}
 
 	/**
 	 * Horizontal Swipe. Uses a single touch. Only works inside the zone's touch
 	 * method, or between calls to beginTouch() and endTouch().
 	 * <P>
 	 * Limits are absolute and are in global coordinates.
 	 * 
 	 * @param leftLimit
 	 *            Limit on how far to be able to drag left
 	 * @param rightLimit
 	 *            Limit on how far to be able to drag right
 	 */
 	public void hSwipe(int leftLimit, int rightLimit) {
 		drag(true, false, leftLimit, rightLimit, Integer.MIN_VALUE, Integer.MAX_VALUE);
 	}
 
 	/**
 	 * Vertical Swipe. Uses a single touch. Only works inside the zone's touch
 	 * method, or between calls to beginTouch() and endTouch()
 	 */
 	public void vSwipe() {
 		drag(false, true);
 	}
 
 	/**
 	 * Vertical Swipe. Uses a single touch. Only works inside the zone's touch
 	 * method, or between calls to beginTouch() and endTouch().
 	 * <P>
 	 * Limits are absolute and are in global coordinates.
 	 * 
 	 * @param upLimit
 	 *            Limit on how far to be able to drag up
 	 * @param downLimit
 	 *            Limit on how far to be able to drag down
 	 */
 	public void vSwipe(int upLimit, int downLimit) {
 		drag(false, true, Integer.MIN_VALUE, Integer.MAX_VALUE, upLimit, downLimit);
 	}
 
 	/**
 	 * Swipe which only allows moving left. Uses a single touch. Only works
 	 * inside the zone's touch method, or between calls to beginTouch() and
 	 * endTouch()
 	 */
 	public void swipeLeft() {
 		drag(true, false, false, false);
 	}
 
 	/**
 	 * Swipe which only allows moving right. Uses a single touch. Only works
 	 * inside the zone's touch method, or between calls to beginTouch() and
 	 * endTouch()
 	 */
 	public void swipeRight() {
 		drag(false, true, false, false);
 	}
 
 	/**
 	 * Swipe which only allows moving up. Uses a single touch. Only works inside
 	 * the zone's touch method, or between calls to beginTouch() and endTouch()
 	 */
 	public void swipeUp() {
 		drag(false, false, true, false);
 	}
 
 	/**
 	 * Swipe which only allows moving down. Uses a single touch. Only works
 	 * inside the zone's touch method, or between calls to beginTouch() and
 	 * endTouch()
 	 */
 	public void swipeDown() {
 		drag(false, false, false, true);
 	}
 
 	/** Draws the rotation radius of the zone */
 	public void drawRntCircle() {
 		pushStyle();
 		pushMatrix();
 		noFill();
 		strokeWeight(5);
 		stroke( 255, 127, 39, 155);
 		ellipseMode(RADIUS);
 		ellipse(width / 2, height / 2, rntRadius, rntRadius);
 		popMatrix();
 		popStyle();
 	}
 
 	/**
 	 * Rotate and translate.\n Single finger gesture, Which only translates
 	 * inside of the rotation radius, and rotates and translates outside of this
 	 * radius.\n Using a default radius of min(width, height)/4, A quarter of
 	 * the smallest side of the zone. Only works inside the zone's touch method,
 	 * or between calls to beginTouch() and endTouch().
 	 */
 	public void rnt() {
 		rnt(rntRadius);
 	}
 
 	/**
 	 * Rotate and translate. Single finger gesture, Which only translates inside
 	 * of the rotation radius, and rotates and translates outside of this
 	 * radius. Only works inside the zone's touch method, or between calls to
 	 * beginTouch() and endTouch()
 	 * 
 	 * @param centreRadius
 	 *            The rotation radius (how far out from centre before rotation
 	 *            is applied)
 	 */
 	public void rnt(float centreRadius) {
 		if (!activeTouches.isEmpty()) {
 			List<TouchPair> pairs = getTouchPairs(1);
 			rnt(pairs.get(0), centreRadius);
 		}
 	}
 
 	/**
 	 * Rotate and translate. Single finger gesture, Which only translates inside
 	 * of the rotation radius, and rotates and translates outside of this
 	 * radius. Using a default radius of min(width, height)/4, A quarter of the
 	 * smallest side of the zone. Only works inside the zone's touch method, or
 	 * between calls to beginTouch() and endTouch()
 	 * 
 	 * @param from
 	 *            The Touch to translate from
 	 * @param to
 	 *            The Touch to translate to
 	 */
 	public void rnt(Touch from, Touch to) {
 		rnt(new TouchPair(from, to), rntRadius);
 	}
 
 	/**
 	 * Rotate and translate. Single finger gesture, Which only translates inside
 	 * of the rotation radius, and rotates and translates outside of this
 	 * radius. Only works inside the zone's touch method, or between calls to
 	 * beginTouch() and endTouch()
 	 * 
 	 * @param from
 	 *            The Touch to translate from
 	 * @param to
 	 *            The Touch to translate to
 	 * @param centreRadius
 	 *            The rotation radius (how far out from centre before rotation
 	 *            is applied)
 	 */
 	public void rnt(Touch from, Touch to, float centreRadius) {
 		rnt(new TouchPair(from, to), centreRadius);
 	}
 
 	/**
 	 * Rotate and translate. Single finger gesture, Which only translates inside
 	 * of the rotation radius, and rotates and translates outside of this
 	 * radius. Only works inside the zone's touch method, or between calls to
 	 * beginTouch() and endTouch()
 	 * 
 	 * @param pair
 	 *            A TouchPair to use for the from/to points
 	 * @param centreRadius
 	 *            The rotation radius (how far out from centre before rotation
 	 *            is applied)
 	 */
 	protected void rnt(TouchPair pair, float centreRadius) {
 
 		if (pair.matches()) {
 			// nothing to do
 			lastUpdate = maxTime(pair);
 			return;
 		}
 
 		// PMatrix3D matrix = new PMatrix3D();
 		matrix.translate(pair.to.x, pair.to.y);
 
 		PVector centre = getCentre();
 
 		PVector fromVec = pair.getFromVec();
 		fromVec.sub(centre);
 
 		PVector toVec = pair.getToVec();
 		toVec.sub(centre);
 
 		float toDist = toVec.mag();
 		float fromDist = fromVec.mag();
 		if (toDist > 0 && fromDist > centreRadius) {
 			float angle = PVector.angleBetween(fromVec, toVec);
 			PVector cross = PVector.cross(fromVec, toVec, new PVector());
 			cross.normalize();
 
 			if (angle != 0 && cross.z != 0) {
 				matrix.rotate(angle, cross.x, cross.y, cross.z);
 			}
 		}
 
 		matrix.translate(-pair.from.x, -pair.from.y);
 		lastUpdate = maxTime(pair);
 	}
 
 	public void toss() {
 		// enable physics on this zone to make sure it can move from the toss
 		setPhysicsEnabled( true);
 		Touch t = getActiveTouch(0);
 		if (zoneBody != null && mJoint != null) {
 			mJoint.setTarget(new Vec2(t.x * SMT.box2dScale, (applet.height - t.y)
 					* SMT.box2dScale));
 		}
 	}
 
 	public void dragWithinParent() {
 		Zone parent = getParent();
 		if (parent != null) {
 			drag(true, true, 0, parent.width, 0, parent.height);
 		}
 	}
 
 	/**
 	 * Rotate about the centre. Single finger gesture. Only works inside the
 	 * zone's touch method, or between calls to beginTouch() and endTouch()
 	 * 
 	 */
 	public void rotateAboutCentre() {
 		if (!activeTouches.isEmpty()) {
 			List<TouchPair> pairs = getTouchPairs(1);
 			TouchPair pair = pairs.get(0);
 
 			if (pair.matches()) {
 				// nothing to do
 				lastUpdate = maxTime(pair);
 				return;
 			}
 
 			// PMatrix3D matrix = new PMatrix3D();
 
 			PVector centre = getCentre();
 
 			translate(centre.x, centre.y);
 
 			PVector fromVec = pair.getFromVec();
 			fromVec.sub(centre);
 
 			PVector toVec = pair.getToVec();
 			toVec.sub(centre);
 
 			float toDist = toVec.mag();
 			if (toDist > 0) {
 				float angle = PVector.angleBetween(fromVec, toVec);
 				PVector cross = PVector.cross(fromVec, toVec, new PVector());
 				cross.normalize();
 
 				if (angle != 0 && cross.z != 0) {
 					matrix.rotate(angle, cross.x, cross.y, cross.z);
 				}
 			}
 
 			matrix.translate(-centre.x, -centre.y);
 			lastUpdate = maxTime(pair);
 		}
 	}
 
 	/**
 	 * @return A PVector containing the centre point of the Zone
 	 */
 	public PVector getCentre() {
 		return fromZoneVector(new PVector(width / 2, height / 2));
 	}
 
 	/**
 	 * @return A PVector containing the origin of the Zone. The origin is
 	 *         defined to be at the top-left corner of the Zone
 	 */
 	public PVector getOrigin() {
 		return fromZoneVector(new PVector(0, 0));
 	}
 
 	protected TuioTime maxTime(Iterable<Touch> touches) {
 		ArrayList<TuioTime> times = new ArrayList<TuioTime>();
 		for (Touch t : touches) {
 			if (t != null) {
 				times.add(t.currentTime);
 			}
 		}
 		return Collections.max(times, SMTUtilities.tuioTimeComparator);
 	}
 
 	protected TuioTime maxTime(TouchPair... pairs) {
 		ArrayList<Touch> touches = new ArrayList<Touch>(pairs.length * 2);
 		for (TouchPair pair : pairs) {
 			touches.add(pair.from);
 			touches.add(pair.to);
 		}
 		return maxTime(touches);
 	}
 
 	/**
 	 * Gets a touch on the zone by index, 0 is first touch, 1 is second, etc
 	 * 
 	 * @param n
 	 *            Which touch to get
 	 * @return The Nth touch on the zone, or null if there is no such touch
 	 */
 	public Touch getActiveTouch(int n) {
 		int i = 0;
 		for (Touch t : activeTouches.values()) {
 			if (i == n) {
 				return t;
 			}
 			i++;
 		}
 		return null;
 	}
 
 	/**
 	 * This takes the touches currently assigned to the Zone and converts them
 	 * into TouchPairs which are pairs of Touches of the current and previous
 	 * state of each Touch
 	 * 
 	 * @return A List of TouchPairs, one TouchPair per Touch assigned to the
 	 *         Zone, each TouchPair is the Touch at its current state, and its
 	 *         previous state
 	 */
 	protected List<TouchPair> getTouchPairs() {
 		return getTouchPairs(activeTouches.size());
 	}
 
 	/**
 	 * This takes the touches currently assigned to the Zone and converts them
 	 * into TouchPairs which are pairs of Touches of the current and previous
 	 * state of each Touch
 	 * 
 	 * @param size
 	 *            The number of Touches to put into Touchpairs, if greater than
 	 *            the number of assigned Touches on the Zone, it is filled with
 	 *            empty TouchPairs (the Touches in each TouchPair are null)
 	 * @return A List of TouchPairs, one TouchPair per Touch assigned to the
 	 *         Zone, each TouchPair is the Touch at its current state, and its
 	 *         previous state
 	 */
 	protected List<TouchPair> getTouchPairs(int size) {
 		ArrayList<TouchPair> pairs = new ArrayList<TouchPair>(size);
 		for (int i = 0; i < size; i++) {
 			Touch touch = getActiveTouch(i);
 			if (touch == null) {
 				pairs.add(new TouchPair());
 			}
 			else {
 				pairs.add(new TouchPair(SMTUtilities.getLastTouchAtTime(touch, lastUpdate), touch));
 			}
 		}
 		return pairs;
 	}
 
 	/**
 	 * Clones the zone and all child zones
 	 */
 	@Override
 	public Zone clone() {
 		return clone(Integer.MAX_VALUE, null);
 	}
 
 	/**
 	 * Clones the zone and optionally any child zones up to the specified
 	 * generation of children
 	 * 
 	 * @param cloneMaxChildGenerations
 	 *            - Max limit on how many generations of children to clone (0 -
 	 *            None, 1 - First Generation children, ... , Integer.MAX_VALUE -
 	 *            All generations of children)
 	 */
 	public Zone clone(int cloneMaxChildGenerations) {
 		return clone(cloneMaxChildGenerations, null);
 	}
 
 	/**
 	 * Clones the zone and optionally any child zones up to the specified
 	 * generation of children
 	 * 
 	 * @param cloneMaxChildGenerations
 	 *            - Max limit on how many generations of children to clone (0 -
 	 *            None, 1 - First Generation children, ... , Integer.MAX_VALUE -
 	 *            All generations of children)
 	 * 
 	 * @param enclosingClass
 	 *            - The enclosingClass of the Zone (needed when cloning a Zone
 	 *            that is an inner class and refereneces its data, otherwise
 	 *            passing null is fine)
 	 */
 	public Zone clone(int cloneMaxChildGenerations, Object enclosingClass) {
 		Zone clone;
 		try {
 			// if inner class, call its constructor properly by passing its
 			// enclosing class too
 			if (this.getClass().getEnclosingClass() != null
 					&& this.getClass().getEnclosingClass() == enclosingClass.getClass()) {
 				// clone a Zone using a copy constructor
 				clone = this.getClass()
 						.getConstructor(this.getClass().getEnclosingClass(), this.getClass())
 						.newInstance(enclosingClass, this);
 			}
 			else {
 				// clone a Zone using a copy constructor
 				clone = this.getClass().getConstructor(this.getClass()).newInstance(this);
 			}
 		}
 		catch (Exception e) {
 			// if no copy constructor, use the old way of making a normal Zone
 			clone = new Zone(this.getName(), this.x, this.y, this.width, this.height);
 		}
 
 		// use the backupMatrix if the zone being clone has its matrix modified
 		// by parent, and so has backupMatrix set
 		if (this.backupMatrix != null) {
 			clone.matrix = this.backupMatrix.get();
 		}
 		else {
 			clone.matrix = this.matrix.get();
 		}
 		clone.inverse = this.inverse.get();
 
 		if (cloneMaxChildGenerations > 0) {
 			for (Zone child : this.getChildren()) {
 				clone.add(child.clone(cloneMaxChildGenerations - 1, clone));
 			}
 		}
 		return clone;
 	}
 
 	/**
 	 * This makes a PVector into one relative to the zone's matrix
 	 * 
 	 * @param global
 	 *            The PVector to put into relative coordinate space
 	 * @return A PVector relative to the zone's coordinate space
 	 */
 	public PVector toZoneVector(PVector global) {
 		PMatrix3D temp = getGlobalMatrix();
 		temp.invert();
 		return temp.mult(global, null);
 	}
 
 	/**
 	 * This makes a PVector into a global coordinates from the given local
 	 * zone's coordinate space
 	 * 
 	 * @param local
 	 *            The PVector to put into global coordinate space
 	 * @return A PVector relative to the global coordinate space
 	 */
 	public PVector fromZoneVector( PVector local) {
 		return getGlobalMatrix().mult(local, null);
 	}
 
 	/**
 	 * Returns a matrix relative to global coordinate space
 	 * 
 	 * @return A PMatrix3D relative to the global coordinate space
 	 */
 	public PMatrix3D getGlobalMatrix() {
 		PMatrix3D temp = new PMatrix3D();
 		// list ancestors in order from most distant to closest, in order to
 		// apply their matrix's in order
 		LinkedList<Zone> ancestors = new LinkedList<Zone>();
 		Zone zone = this;
 		while (zone.getParent() != null) {
 			zone = zone.getParent();
 			ancestors.addFirst(zone);
 		}
 		// apply ancestors matrix's in proper order to make sure image is
 		// correctly oriented, but not when backupMatrix is set, as then it
 		// already has its parents applied to it.
 		if (backupMatrix == null) {
 			for (Zone i : ancestors) {
 				temp.apply(i.matrix);
 			}
 		}
 		temp.apply(matrix);
 
 		return temp;
 	}
 
 	/**
 	 * Changes the ordering of the child zones, placing the given one on top
 	 * 
 	 * @param zone
 	 *            The child to place on top of the others
 	 */
 	public void putChildOnTop( Zone zone) {
 		// only remove and add if actually in the children arraylist and not
 		// already the last(top) already
 		if ( this.children.contains(zone) &&
 				(children.indexOf(zone) < children.size() - 1)) {
 			this.children.remove(zone);
 			this.children.add(zone);
 		}
 	}
 
 	/**
 	 * Converts java key pressed events into their processing counterparts,
 	 * then calls them on this zone.
 	 * @param event the key pressed event to be converted
 	 */
 	@Override
 	public void keyPressed( java.awt.event.KeyEvent event){
 		this.keyPressed(
 			new KeyEvent(
 				event,
 				event.getWhen(),
 				KeyEvent.PRESS,
 				event.getModifiers(),
 				event.getKeyChar(),
 				event.getKeyCode()));
 	}
 
 	/**
 	 * Converts java key released events into their processing counterparts,
 	 * then calls them on this zone.
 	 * @param event the key released event to be converted
 	 */
 	@Override
 	public void keyReleased( java.awt.event.KeyEvent event){
 		this.keyReleased(
 			new KeyEvent(
 				event,
 				event.getWhen(),
 				KeyEvent.RELEASE,
 				event.getModifiers(),
 				event.getKeyChar(),
 				event.getKeyCode()));
 	}
 
 	/**
 	 * Converts java key typed events into their processing counterparts,
 	 * then calls them on this zone.
 	 * @param event the key typed event to be converted
 	 */
 	@Override
 	public void keyTyped( java.awt.event.KeyEvent event){
 		this.keyTyped(
 			new KeyEvent(
 				event,
 				event.getWhen(),
 				KeyEvent.TYPE,
 				event.getModifiers(),
 				event.getKeyChar(),
 				event.getKeyCode()));
 	}
 
 	public void keyPressed( KeyEvent event) {
 		keyPressedImpl( event);
 		SMTUtilities.invoke( method_keyPressed, applet, this, event);
 	}
 
 	public void keyReleased( KeyEvent event) {
 		keyReleasedImpl( event);
 		SMTUtilities.invoke( method_keyReleased, applet, this, event);
 	}
 
 	public void keyTyped( KeyEvent event) {
 		keyTypedImpl( event);
 		SMTUtilities.invoke( method_keyTyped, applet, this, event);
 	}
 
 	protected void touchUpInvoker( Touch touch) {
 		touchUpImpl(touch);
 		SMTUtilities.invoke( method_touchUp, applet, this, touch);
 	}
 
 	protected void touchDownInvoker( Touch touch) {
 		touchDownImpl( touch);
 		SMTUtilities.invoke( method_touchDown, applet, this, touch);
 	}
 
 	protected void touchMovedInvoker( Touch touch) {
 		touchMovedImpl( touch);
 		SMTUtilities.invoke( method_touchMoved, applet, this, touch);
 	}
 
 	protected void pressInvoker( Touch touch) {
 		pressImpl( touch);
 		SMTUtilities.invoke( method_press, applet, this, touch);
 	}
 
 	/** Override to specify a default behavior for press */
 	protected void pressImpl( Touch touch) {}
 
 	/** Override to specify a default behavior for draw */
 	protected void drawImpl(){
 		draw();
 	}
 
 	/** Override to specify a default behavior for touch */
 	protected void touchImpl() {}
 
 	/** Override to specify a default behavior for pickDraw */
 	protected void pickDrawImpl() {
 		pickDraw();
 	}
 
 	/** Override to specify a default behavior for touchDown */
 	protected void touchDownImpl(Touch touch) {
 		addPhysicsMouseJoint();
 	}
 
 	/** Override to specify a default behavior for touchUp */
 	protected void touchUpImpl(Touch touch) {}
 
 	/** Override to specify a default behavior for touchMoved */
 	protected void touchMovedImpl(Touch touch) {
 		addPhysicsMouseJoint();
 	}
 
 	/** Override to specify a default behavior for keyPressed */
 	protected void keyPressedImpl(KeyEvent e) {}
 
 	/** Override to specify a default behavior for keyReleased */
 	protected void keyReleasedImpl(KeyEvent e) {}
 
 	/** Override to specify a default behavior for keyTyped */
 	protected void keyTypedImpl(KeyEvent e) {}
 
 	public void setBodyFromMatrix() {
 		// get origin position
 		PVector o = fromZoneVector(new PVector(width / 2, height / 2));
 		// height-y to account for difference in coordinates
 		zoneBody.setTransform(new Vec2(o.x * SMT.box2dScale, (applet.height - o.y)
 				* SMT.box2dScale), getRotationAngle());
 	}
 
 	public void setMatrixFromBody() {
 		// set global matrix from zoneBody, then get local matrix from global
 		// matrix
 		PMatrix3D ng = new PMatrix3D();
 		// height-y to account for difference in coordinates
 		ng.translate(zoneBody.getPosition().x / SMT.box2dScale,
 				(applet.height - zoneBody.getPosition().y / SMT.box2dScale));
 		ng.rotate(zoneBody.getAngle());
 		ng.translate(-width / 2, -height / 2);
 		// ng=PM == (P-1)*ng=M
 		PMatrix3D M = new PMatrix3D(matrix);
 		M.invert();
 		PMatrix3D P = getGlobalMatrix();
 		P.apply(M);
 		P.invert();
 		ng.apply(P);
 		matrix.set(ng);
 	}
 
 	public void addPhysicsMouseJoint() {
 		if (zoneBody != null && mJoint == null && physics) {
 			mJointDef = new MouseJointDef();
 			mJointDef.maxForce = 1000000.0f;
 			mJointDef.frequencyHz = applet.frameRate;
 			mJointDef.bodyA = SMT.groundBody;
 			mJointDef.bodyB = zoneBody;
 			mJointDef.target.set(new Vec2(zoneBody.getPosition().x, zoneBody.getPosition().y));
 			mJoint = (MouseJoint) SMT.world.createJoint(mJointDef);
 			zoneBody.setAwake(true);
 		}
 	}
 
 
 	/**
 	 * This method is for use by Processing, override it to change what occurs
 	 * when a Processing KeyEvent is passed to the Zone
 	 * @param event The Processing KeyEvent that is sent to the Zone
 	 */
 	public void keyEvent(KeyEvent event) {
 		switch (event.getAction()) {
 		case KeyEvent.RELEASE:
 			keyReleased(event);
 			break;
 		case KeyEvent.TYPE:
 			keyTyped(event);
 			break;
 		case KeyEvent.PRESS:
 			keyPressed(event);
 			break;
 		}
 	}
 
 	/**
 	 * Make sure that if a hidden sub-Zone is given a name that the outer-Zone
 	 * overrides this method to pass through the set to those Zones too,
 	 * otherwise the methods on these sub-Zones will never be called
 	 * @param obj The object to bind to the Zone
 	 * @deprecated Do not use this method - See <a href="https://github.com/vialab/SMT/issues/174">this github issue</a>
 	 */
 	@Deprecated
 	public void setBoundObject(Object obj) {
 		this.boundObject = obj;
 	}
 
 	/**
 	 * Gets the object this zone is currently bound to.
 	 * @deprecated Do not use this method - See <a href="https://github.com/vialab/SMT/issues/174">this github issue</a>
 	 */
 	@Deprecated
 	public Object getBoundObject() {
 		return boundObject;
 	}
 
 	/**
 	 * @return The angle of the Zone in global coordinates
 	 */
 	public float getRotationAngle() {
 		PMatrix3D g = getGlobalMatrix();
 		float angle = PApplet.atan2(g.m10, g.m00);
 		return angle >= 0 ? angle : angle + 2 * PI;
 	}
 
 	public void touchDownRegister(Touch touch) {
 		touchDownList.add(touch);
 	}
 
 	public void touchUpRegister(Touch touch) {
 		touchUpList.add(touch);
 	}
 
 	public void touchMovedRegister(Touch touch) {
 		touchMovedList.add(touch);
 	}
 
 	public void pressRegister(Touch touch) {
 		pressList.add(touch);
 	}
 	
 	/**
 	 * @return the x position of the touch in local coordinates
 	 */
 	public float getLocalX() {
 		return
 			getParent() == null ?
 				getOrigin().x :
 				getParent().toZoneVector( getOrigin()).x;
 	}
 
 	/**
 	 * @return the y position of zone in parent coordinates
 	 */
 	public float getLocalY() {
 		return
 			getParent() == null ?
 				getOrigin().y :
 				getParent().toZoneVector(getOrigin()).y;
 	}
 	
 	/**
 	 * @param touch
 	 * @return the x position of zone in parent coordinates
 	 */
 	public float getLocalX( Touch touch) {
 		return toZoneVector( new PVector(
 			touch.x, touch.y)).x;
 	}
 
 	/**
 	 * @param touch
 	 * @return the y position of the touch in local coordinates
 	 */
 	public float getLocalY( Touch touch) {
 		return toZoneVector( new PVector(
 			touch.x, touch.y)).y;
 	}
 	
 	/**
 	 * Sets the local x position
 	 * @param x
 	 */
 	public void setX(float x) {
 		if (getParent() == null)
 			setLocation( x, getOrigin().y);
 		else
 			setLocation( x, getParent().toZoneVector(getOrigin()).y);
 	}
 
 	/**
 	 * Sets the local y position
 	 * @param y
 	 */
 	public void setY(float y) {
 		if (getParent() == null)
 			setLocation( getOrigin().x, y);
 		else
 			setLocation( getParent().toZoneVector(getOrigin()).x, y);
 	}
 	
 	//background functions
 	@Override
 	public void background(float arg0, float arg1, float arg2, float arg3) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0, arg1, arg2, arg3);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void background(float arg0, float arg1, float arg2) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0, arg1, arg2);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void background(float arg0, float arg1) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0, arg1);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0, arg1);
 	}
 
 	@Override
 	public void background(float arg0) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0);
 	}
 
 	@Override
 	public void background(int arg0, float arg1) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0, arg1);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0, arg1);
 	}
 
 	@Override
 	public void background(int arg0) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			fill(arg0);
 			rect(0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0);
 	}
 
 	@Override
 	public void background(PImage arg0) {
 		if( picking_on) return;
 		if( direct){
 			pushStyle();
 			noStroke();
 			image(arg0,0,0,width,height);
 			popStyle();
 		}
 		else super.background(arg0);
 	}
 
 	//color functions
 	@Override
 	public void fill(float arg0, float arg1, float arg2, float arg3) {
 		if( ! picking_on)
 			super.fill(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void fill(float arg0, float arg1, float arg2) {
 		if( ! picking_on)
 			super.fill(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void fill(float arg0, float arg1) {
 		if( ! picking_on)
 			super.fill(arg0, arg1);
 	}
 
 	@Override
 	public void fill(float arg0) {
 		if( ! picking_on)
 			super.fill(arg0);
 	}
 
 	@Override
 	public void fill(int arg0, float arg1) {
 		if( ! picking_on)
 			super.fill(arg0, arg1);
 	}
 
 	@Override
 	public void fill(int arg0) {
 		if( ! picking_on)
 			super.fill(arg0);
 	}
 
 	@Override
 	public void stroke(float arg0, float arg1, float arg2, float arg3) {
 		if( ! picking_on)
 			super.stroke(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void stroke(float arg0, float arg1, float arg2) {
 		if( ! picking_on)
 			super.stroke(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void stroke(float arg0, float arg1) {
 		if( ! picking_on)
 			super.stroke(arg0, arg1);
 	}
 
 	@Override
 	public void stroke(float arg0) {
 		if( ! picking_on)
 			super.stroke(arg0);
 	}
 
 	@Override
 	public void stroke(int arg0, float arg1) {
 		if( ! picking_on)
 			super.stroke(arg0, arg1);
 	}
 
 	@Override
 	public void stroke(int arg0) {
 		if( ! picking_on)
 			super.stroke(arg0);
 	}
 
 	@Override
 	public void tint(float arg0, float arg1, float arg2, float arg3) {
 		if( ! picking_on)
 			super.tint(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void tint(float arg0, float arg1, float arg2) {
 		if( ! picking_on)
 			super.tint(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void tint(float arg0, float arg1) {
 		if( ! picking_on)
 			super.tint(arg0, arg1);
 	}
 
 	@Override
 	public void tint(float arg0) {
 		if( ! picking_on)
 			super.tint(arg0);
 	}
 
 	@Override
 	public void tint(int arg0, float arg1) {
 		if( ! picking_on)
 			super.tint(arg0, arg1);
 	}
 
 	@Override
 	public void tint(int arg0) {
 		if( ! picking_on)
 			super.tint(arg0);
 	}
 }
