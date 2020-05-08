 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.math.Matrix4f;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import java.awt.Button;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
 import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.AppCell;
 import org.jdesktop.wonderland.modules.appbase.client.Window2DViewWorld;
 import org.jdesktop.wonderland.client.jme.utils.graphics.TexturedQuad;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import com.jme.scene.state.TextureState;
 import java.nio.FloatBuffer;
 import com.jme.util.geom.BufferUtils;
 import com.jme.scene.TexCoords;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.LinkedList;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.mtgame.PickDetails;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.wonderland.client.input.EventListener;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 /**
  * A view onto a window which exists in the 3D world.
  *
  * @author deronj
  */ 
 
 @ExperimentalAPI
 public class ViewWorldDefault extends Window2DView implements Window2DViewWorld {
 
     private static final Logger logger = Logger.getLogger(ViewWorldDefault.class.getName());
 
     /** The vector offset from the center of the cell to the center of the window */
     protected Vector3f translation = new Vector3f();
 
     /** The width of the view (excluding the frame) */
     protected float width;
 
     /** The height of the view (excluding the frame) */
     protected float height;
 
     /** The textured 3D object in which displays the window contents */
     protected ViewGeometryObject geometryObj;
 
     /** The view's geometry is connected to its cell */
     protected boolean connectedToCell;
     
     /** 
      * The root of the view subgraph. This contains all geometry and is 
      * connected to the local scene graph of the cell.
      */
     protected Node baseNode;
 
     /**
      * The view's entity.
      */
     protected Entity entity;
 
     /** The control arbitrator of the window */
     protected ControlArb controlArb;
 
     /** The frame of the view's window (if it is top-level) */
     protected FrameWorldDefault frame;
     
     /** The visibility of the view itself */
     private boolean viewVisible;
 
     /** 
      * The combined window/view visibility. The view is actually visible
      * only if both are true.
      */
     private boolean visible;
 
     /** Whether the view's window is top-level */
     private boolean topLevel;
 
     /** A dummy AWT component used by deliverEvent(Window2D,MouseEvent3D) */
     private static Button dummyButton = new Button();
 
     /*
     ** TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
     ** TODO: >>>>>>>> Is this obsolete in 0.5?
     ** 
     ** We cannot rely on the x and y values in the intersection info coming from LG
     ** for mouse release events.
     ** The problem is both the same for the X11 and AWT pickers. The LG pickers are 
     ** currently defined to set the node info of all events interior to and terminating
     ** a grab to the node info of the button press which started the grab. Not only is
     ** the destination node set (as is proper) but also the x and y intersection info
     ** (which is dubious and, I believe, improper). Note that there is a hack in both 
     ** pickers to work around this problem for drag events, which was all LG cared about
     ** at the time. I don't want to perturb the semantics of the LG pickers at this time,
     ** but in the future the intersection info must be dealt with correctly for all
     ** events interior to and terminating a grab. Note that this problem doesn't happen
     ** with button presses, because these start grabs.
     **
     ** For now I'm simply going to treat the intersection info in button release events
     ** as garbage and supply the proper values by tracking the pointer position myself.
     */
     private boolean pointerMoveSeen = false;
     private int pointerLastX;
     private int pointerLastY;
 
     /** The event listeners which are attached to this view while the view is attached to its cell */
     private LinkedList<EventListener> eventListeners = new LinkedList<EventListener>();
 
     /** A type for entries in the entityComponents list. */
     private class EntityComponentEntry {
 	private Class clazz;
 	private EntityComponent comp;
 	private EntityComponentEntry (Class clazz, EntityComponent comp) {
 	    this.clazz = clazz;
 	    this.comp = comp;
 	}
     }
 
     /** The entity components which should be attached to this view while the view is attached to its cell. */
     private LinkedList<EntityComponentEntry> entityComponents = new LinkedList<EntityComponentEntry>();
 
     /**
      * Create a new instance of ViewWorldDefault.
      *
      * @param window The window displayed by the view.
      */
     public ViewWorldDefault (Window2D window) {
 	super(window, "World");
 	gui = new Gui2DInterior(this);
 
 	controlArb = window.getApp().getControlArb();
 
 	entity = new Entity("View Entity");
 	baseNode = new Node("View Base Node"); 
 	RenderComponent rc = 
 	    ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(baseNode);
 	entity.addComponent(RenderComponent.class, rc);
     }
 
     /**
      * Clean up resources.
      */
     public void cleanup () {
 	super.cleanup();
 	setVisible(false);
 	if (geometryObj != null) {
 	    geometryObj.cleanup();
 	    geometryObj = null;
 	}
 	if (controlArb != null) {
 	    controlArb = null;
 	}
 	if (frame != null) {
 	    frame.cleanup();
 	    frame = null;
         }
 	detachEventListeners(entity);
 	if (gui != null) {
 	    gui.cleanup();
 	    gui = null;
 	}
 	if (entity != null) {
 	    if (connectedToCell) {
 		detachFromCell((AppCell)getCell());
 	    }
 	    Entity parentEntity = entity.getParent();
 	    if (parentEntity != null) {
 		parentEntity.removeEntity(entity);
 	    }
 	    entity.removeComponent(RenderComponent.class);
 	    baseNode = null;
 	    entity = null;
 	}
     }
 
     /**
      * Returns the depth offset above a base window that popup windows should be positioned.
      */
     public float getPopupDepthOffset () {
	return 0.01f;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setVisible (boolean visible) {
 	viewVisible = visible;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean getVisible () {
 	return viewVisible;
     }
 
     /**
      * Returns whether the window is actually visible, that is,
      * the view visibility combined with the window visibility.
      */
     boolean getActuallyVisible () {
 	return visible;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setTopLevel (boolean topLevel) {
 	if (this.topLevel == topLevel) return;
 	this.topLevel = topLevel;
     }
 	
     /**
      * {@inheritDoc}
      */
     public boolean getTopLevel () {
 	return topLevel;
     }
 
     /**
      * Sets the translation of the view. Don't forget to also call update(CHANGED_TRANSFORM) afterward.
      *
      * @param translation The new translation of the window relative to the center of the cell.
      */
     public void setTranslation (Vector3f translation) { 
 	this.translation.set(translation);
     }
 
     /**
      * Returns the translation of the view.
      */
     public Vector3f getTranslation () { 
 	return new Vector3f(translation);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSize (float width, float height) {
 	this.width = width;
 	this.height = height;
     }
 
     /**
      * {@inheritDoc}
      */
     public float getWidth () { 
 	return width; 
     }
 
     /**
      * {@inheritDoc}
      */
     public float getHeight () { 
 	return height; 
     }
 
     /**
      * Returns the frame of view. 
      */
     FrameWorldDefault getFrame () {
 	update(CHANGED_TOP_LEVEL);
 	return frame;
     }
 
     /**
      * {@inheritDoc}"
      */
     @Override
     public void update (int changeMask) {
 	
 	// It's necessary to do these in the following order
 
 	if ((changeMask & CHANGED_VISIBILITY) != 0) {
 	    updateVisibility();
 
 	    // When the view has been made visible for the first time
 	    // we need to create the geometry
 	    if (visible && geometryObj == null) {
 		changeMask |= CHANGED_SIZE;
 	    }
 	}
 
 	if ((changeMask & CHANGED_TOP_LEVEL) != 0) {
 	    if (topLevel) {
 		if (frame == null) {
 		    frame = (FrameWorldDefault) window.getApp().getAppType().getGuiFactory().createFrame(this);
 		}
 	    } else {
 		if (frame != null) {
 		    frame.cleanup();
 		    frame = null;
 		}
 	    }
 	}
 
 	if ((changeMask & CHANGED_SIZE) != 0) {
 	    try {
                 updateGeometrySize();
                 updateTexture();
             } catch (InstantiationException ex) {
                 logger.warning("Instantiation exception while updating view size");
 		ex.printStackTrace();
                 return;
             }
 	}
 	if ((changeMask & CHANGED_STACK) != 0) {
 	    updateStack();
 	}
 	if ((changeMask & (CHANGED_TRANSFORM | CHANGED_STACK)) != 0) {
 	    updateTransform();
 	}
 
 	if ((changeMask & CHANGED_TITLE) != 0) {
 	    if (frame != null) {
 		frame.setTitle(((Window2D)window).getTitle());
 	    }
 	}
     }
 
     /** Update the view's geometry (for a size change) */
     protected void updateGeometrySize () throws InstantiationException {
 
         Window2D window2D = (Window2D) window;
         Vector2f pixelScale = window2D.getPixelScale();
 	width = pixelScale.x * (float)window2D.getWidth();
 	height = pixelScale.y * (float)window2D.getHeight();
 
 	if (geometryObj == null) {
 
 	    // Geometry first time creation
 
 	    setGeometry(new ViewGeometryObjectDefault(this));
 
 	} else {
 
 	    // Geometry resize
 	    geometryObj.updateSize();
 	}
 
 	if (frame != null) {
 	    frame.update();
 	}
     }
 
     /** The window's texture may have changed */
     protected void updateTexture () {
 	if (geometryObj != null) {
 	    geometryObj.updateTexture();
 	}
     }
 
     /** Update the view's transform */
     protected void updateTransform () {
 	baseNode.setLocalTranslation(translation);
     }
 
     /**
      * Returns the texture width.
      */
     public int getTextureWidth () {
 	if (geometryObj != null) {
 	    return geometryObj.getTextureWidth();
 	} else {
 	    return 0;
 	}
     }
 
     /**
      * Returns the texture height.
      */
     public int getTextureHeight () {
 	if (geometryObj != null) {
 	    return geometryObj.getTextureHeight();
 	} else {
 	    return 0;
 	}
     }
 
     /** Update the view's stacking relationships */
     protected void updateStack () {
 	// TODO
     }
 
     /** Update the view's visibility */
     void updateVisibility() {
 	AppCell cell = getCell();
 
 	if (cell == null) return;
 	baseNode.setName("ViewWorldDefault Node for cell " + getCell().getCellID().toString());
 
 	visible = viewVisible && window.isVisible();
 	
 	if (visible && !connectedToCell) {
 	    attachToCell(cell);
 	} else if (!visible && connectedToCell) {
 	    detachFromCell(cell);
 	}
     }
 
     /** Get the cell of the view */
     protected AppCell getCell () {
 	return window.getCell();
     }
   
     /**
      * If you want to customize the geometry of a view implement
      * this interface and pass an instance to view.setGeometry.
      */
     public abstract class ViewGeometryObject extends Node {
 
 	/** The view for which the geometry object was created. */
 	protected ViewWorldDefault view;
 
 	/** 
 	 * Create a new instance of ViewGeometryObject. Attach your geometry as 
 	 * children of this Node.
 	 *
 	 * @param view The view object the geometry is to display.
 	 */
        	public ViewGeometryObject (ViewWorldDefault view) {
 	    super("ViewGeometryObject");
 	    this.view = view;
 	}
 
       	/** 
 	 * Clean up resources. Any resources you allocate for the 
 	 * object should be detached in here. 
 	 *
 	 * NOTE: It is normal for one ViewObjectGeometry to get stuck in 
 	 * the Java heap (because it is reference by Wonderland picker last 
 	 * picked node). So make sure there aren't large objects, such as a 
 	 * texture, attached to it.
 	 *
 	 * NOTE: for cleanliness, call super.cleanup() in your constructor.
 	 * (This is optional).
 	 */
 	public void cleanup () {
 	    view = null;
 	}
 
 	/**
 	 * The size of the view (and corresponding window texture) has
 	 * changed. Make the corresponding change to your geometry in
 	 * this method.
 	 */
 	public abstract void updateSize ();
 
 	/** 
 	 * The view's texture may have changed. You should get the view's
 	 * texture and make your geometry display it.
 	 */
 	public abstract void updateTexture ();
 
 	public Point calcPositionInPixelCoordinates (Vector3f point) {
 	    if (point == null) return null;
 	    logger.fine("point = " + point);
 
 	    // First calculate the actual coordinates of the corners of the view in world coords.
 	    Vector3f topLeftLocal = new Vector3f( -width/2f, height/2f, 0f);
 	    Vector3f topRightLocal = new Vector3f( width/2f, height/2f, 0f);
 	    Vector3f bottomLeftLocal = new Vector3f( -width/2f, -height/2f, 0f);
 	    Vector3f bottomRightLocal = new Vector3f( width/2f, -height/2f, 0f);
 	    logger.fine("topLeftLocal = " + topLeftLocal);
 	    logger.fine("topRightLocal = " + topRightLocal);
 	    logger.fine("bottomLeftLocal = " + bottomLeftLocal);
 	    logger.fine("bottomRightLocal = " + bottomRightLocal);
 	    Matrix4f local2World = getLocalToWorldMatrix(null); // TODO: prealloc
 	    Vector3f topLeft = local2World.mult(topLeftLocal, new Vector3f()); // TODO:prealloc
 	    Vector3f topRight = local2World.mult(topRightLocal, new Vector3f()); // TODO:prealloc
 	    Vector3f bottomLeft = local2World.mult(bottomLeftLocal, new Vector3f()); // TODO:prealloc
 	    Vector3f bottomRight = local2World.mult(bottomRightLocal, new Vector3f()); // TODO:prealloc
 	    logger.fine("topLeft = " + topLeft);
 	    logger.fine("topRight = " + topRight);
 	    logger.fine("bottomLeft = " + bottomLeft);
 	    logger.fine("bottomRight = " + bottomRight);
 
 	    // Now calculate the x and y coords relative to the view
 
 	    float widthWorld = topRight.x - topLeft.x;
 	    float heightWorld = topLeft.y - bottomLeft.y;
 	    logger.fine("widthWorld = " + widthWorld);
 	    logger.fine("heightWorld = " + heightWorld);
 
 	    // TODO: doc: point must be on window
 	    float x = point.x - topLeft.x;
 	    float y = (topLeft.y - point.y);
 	    logger.fine("x = " + x);
 	    logger.fine("y = " + y);
 
 	    x /= widthWorld;
 	    y /= heightWorld;
 	    logger.fine("x pct = " + x);
 	    logger.fine("y pct = " + y);
 
 	    // Assumes window is never scaled
 	    if (x < 0 || x >= 1 || y < 0 || y >= 1) {
 		logger.fine("Outside!");
 		return null;
 	    }
         
 	    int winWidth = ((Window2D)window).getWidth();
 	    int winHeight = ((Window2D)window).getHeight();
 	    logger.fine("winWidth = " + winWidth);
 	    logger.fine("winHeight = " + winHeight);
 
 	    logger.fine("Final xy " + (int)(x * winWidth) + ", "+ (int)(y * winHeight));
 	    return new Point((int)(x * winWidth), (int)(y * winHeight));
 	}
 
 	/**
 	 * Returns the texture width.
 	 */
 	public abstract int getTextureWidth ();
 
 	/**
 	 * Returns the texture height.
 	 */
 	public abstract int getTextureHeight ();
 
 	/**
 	 * Returns the texture state.
 	 */
 	public abstract TextureState getTextureState ();
     }
 
     /** 
      * The geometry node used by the view.
      */
     protected class ViewGeometryObjectDefault extends ViewGeometryObject {
                
 	/** The actual geometry */
         private TexturedQuad quad;
 
 	/** 
 	 * Create a new instance of textured geometry. The dimensions
 	 * are derived from the view dimensions.
 	 *
 	 * @param view The view object the geometry is to display.
 	 */
         private ViewGeometryObjectDefault (ViewWorldDefault view) {
 	    super(view);
 
 	    quad = new TexturedQuad(null, "ViewWorldDefault-TexturedQuad");
 	    quad.setModelBound(new BoundingBox());
 	    attachChild(quad);
 
 	    updateSize();
 	    quad.updateModelBound();
 
 	    /* TODO: debug
 	    quad.printRenderState();
 	    quad.printGeometry();
             */
         }
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	public void cleanup () {
 	    super.cleanup();
 	    if (quad != null) {
 		detachChild(quad);
 		quad = null;
 	    }
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void updateSize () {
 	    float width = view.getWidth();
 	    float height = view.getHeight();
 	    quad.initialize(width, height);
 	    updateTexture();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void updateTexture () {
 	    Window2D window2D = (Window2D) view.getWindow();
 	    Texture texture = window2D.getTexture();
 
 	    /* For debug
 	    java.awt.Image bi = Toolkit.getDefaultToolkit().getImage("/home/dj/wl/images/Monkey.png");
 	    Image image = TextureManager.loadImage(bi, false);
 	    Texture texture = new Texture2D();
 	    texture.setImage(image);
 	    texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
 	    texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
 	    texture.setApply(Texture.ApplyMode.Replace);
 	    */
 
 	    if (texture == null) {
 		// Texture hasn't been created yet. (This method will
 		// be called again when it is created).
 		return;
 	    }
 
 	    quad.setTexture(texture);
 
 	    float winWidth = (float) window2D.getWidth();
 	    float winHeight = (float) window2D.getHeight();
 
 	    Image image = texture.getImage();
             float texCoordW = winWidth / image.getWidth();
             float texCoordH = winHeight / image.getHeight();
             
             //logger.warning("TexCoords "+texCoordW+", "+texCoordH);
 
 	    FloatBuffer tbuf = BufferUtils.createVector2Buffer(4);
 	    quad.setTextureCoords(new TexCoords(tbuf));
 	    tbuf.put(0f).put(0);
 	    tbuf.put(0f).put(texCoordH);
 	    tbuf.put(texCoordW).put(texCoordH);
 	    tbuf.put(texCoordW).put(0);
         } 
 
 	/**
 	 * Returns the texture width.
 	 */
 	public int getTextureWidth () {
 	    return quad.getTexture().getImage().getWidth();
 	}
 
 	/**
 	 * Returns the texture height.
 	 */
 	public int getTextureHeight () {
 	    return quad.getTexture().getImage().getHeight();
 	}
 
 	/**
 	 * Returns the texture state.
 	 */
 	public TextureState getTextureState () {
 	    return quad.getTextureState();
 	}
     }
 
     /**
      * Replace this view's geometry with different geometry.
      * The updateSize method of the geometry object will be called 
      * to provide geometry object with the view's current size.
      *
      * @param newGeometryObj The new geometry object for this window.
      */
     public void setGeometry (ViewGeometryObject newGeometryObj) {
 
 	// Detach any previous geometry object
 	if (geometryObj != null) {
 	    geometryObj.cleanup();
 	    baseNode.detachChild(geometryObj);
 	}
 
 	geometryObj = newGeometryObj;
 	geometryObj.updateSize();
 	geometryObj.updateTexture();
 
 	baseNode.attachChild(newGeometryObj);
     }
 
     /**
      * {@inheritDoc}
      */
     public Point calcPositionInPixelCoordinates (Vector3f point) {
 	if (geometryObj == null) return null;
 	return geometryObj.calcPositionInPixelCoordinates(point);
     }
 
     /**
      * Calculates the distance of a point from a line.
      * <p><code>
      *    x1----------------------------x2 <br>
      *                  |               <br>
      *                  | distance      <br>
      *                  |               <br>
      *                 point            <br>
      * </code>
      * <p>
      * The formula is <br>
      * <code>
      *      d = |(x2-x1) x (x1-p)| <br>
      *          ------------------ <br>
      *              |x2-x1|        <br>
      * </code>
      *
      * Where p=point, lineStart=x1, lineEnd=x2
      *
      */
     public static float pointLineDistance( final Vector3f lineStart, 
 					   final Vector3f lineEnd, 
 					   final Vector3f point ) {
 	Vector3f a = new Vector3f(lineEnd);
 	a.subtract(lineStart);
         
 	Vector3f b = new Vector3f(lineStart);
 	b.subtract(point);
         
 	Vector3f cross = new Vector3f();
 	cross.cross(a,b);
         
 	return cross.length()/a.length();
     }
 
     /**
      * {@inheritDoc}
      */
     public void deliverEvent (Window2D window, MouseEvent3D me3d) {
 	/*
 	System.err.println("********** me3d = " + me3d);
 	System.err.println("********** awt event = " + me3d.getAwtEvent());
 	PickDetails pickDetails = me3d.getPickDetails();
 	System.err.println("********** pt = " + pickDetails.getPosition());
 	*/
 
 	// No special processing is needed for wheel events. Just
 	// send the 2D wheel event which is contained in the 3D event.
 	if (me3d instanceof MouseWheelEvent3D) {
 	    controlArb.deliverEvent(window, (MouseEvent) me3d.getAwtEvent());
 	    return;
 	}
 
 	// Can't convert if there is no geometry
 	if (geometryObj == null) return;
 
 	// Convert mouse event intersection point to 2D. For most events this is the intersection
 	// point based on the destination pick details calculated by the input system, but for drag
 	// events this needs to be derived from the actual hit pick details (because for drag events
 	// the destination pick details might be overridden by a grab).
 	Point point;
 	if (me3d.getID() == MouseEvent.MOUSE_DRAGGED) {
 	    MouseDraggedEvent3D de3d = (MouseDraggedEvent3D) me3d;
 	    point = geometryObj.calcPositionInPixelCoordinates(de3d.getHitIntersectionPointWorld());
 	} else {
 	    point = geometryObj.calcPositionInPixelCoordinates(me3d.getIntersectionPointWorld());
 	}
 	if (point == null) {
             // Event was outside our panel so do nothing
             // This can happen for drag events
             // TODO fix so that drags continue in the plane of the panel
 	    return;
 	}
 
 	// Construct a corresponding 2D event
 	MouseEvent me = (MouseEvent) me3d.getAwtEvent();
 	int id = me.getID();
 	long when = me.getWhen();
 	int modifiers = me.getModifiers();
 	int button = me.getButton();
 
 	// TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
 	// See comment for pointerMoveSeen above
 	if (id == MouseEvent.MOUSE_RELEASED && pointerMoveSeen) {
 	    point.x = pointerLastX;
 	    point.y = pointerLastY;
 	}
 
 	me = new MouseEvent(dummyButton, id, when, modifiers, point.x, point.y, 
 			    0, false, button);
 
 	// Send event to the window's control arbiter
 	controlArb.deliverEvent(window, me);
 
 	// TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
 	// See comment for pointerMoveSeen above
 	if (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_DRAGGED) {
 	    pointerMoveSeen = true;
 	    pointerLastX = point.x;
 	    pointerLastY = point.y;
 	}
     }
 
     public void forceTextureIdAssignment () {
 	if (geometryObj == null) {
 	    setGeometry(new ViewGeometryObjectDefault(this));
 	    if (geometryObj == null) {
 		logger.severe("***** Cannot allocate geometry!!");
 		return;
 	    }
 	}
 	TextureState ts = geometryObj.getTextureState();
 	if (ts == null) {
 	    logger.warning("Trying to force texture id assignment while view texture state is null");
 	    return;
 	}
 	// The JME magic - must be called from within the render loop
 	ts.load();
 	
 	// Verify
 	Texture texture = ((Window2D)window).getTexture();
 	int texid = texture.getTextureId();
 	logger.warning("ViewWorldDefault: allocated texture id " + texid);
 	if (texid == 0) {
 	    logger.severe("Texture Id is still 0!!!");
 	}
     }
 
     /**
      * Attach the event listeners of this view (and any associated frame) to the given entity.
      */
     private void attachEventListeners (Entity entity) {
 	if (entity == null) return;
 	if (gui != null) {
 	    ((Gui2D)gui).attachEventListeners(entity);
 	}
 	if (frame != null) {
 	    frame.attachEventListeners(entity);
 	}
     }
 
     /**
      * Detach the event listeners of this view (and any associated frame) from the given entity.
      */
     private void detachEventListeners (Entity entity) {
 	if (entity == null) return;
 	if (gui != null) {
 	    ((Gui2D)gui).detachEventListeners(entity);
 	}
 	if (frame != null) {
 	    frame.detachEventListeners(entity);
 	}
     }
 
     /**
      * Connect this view to the given cell.
      */
     private void attachToCell (AppCell cell) {
 
 	cell.attachView(this, RendererType.RENDERER_JME);
 	attachEventListeners(getEntity());
 
 	// For debug
 	//logger.severe("SCENE GRAPH AT ATTACH TO CELL:");
 	//cell.logSceneGraph(RendererType.RENDERER_JME);
 
 	// Attach this view's event listeners
 	AppCellRendererJME cellRenderer = (AppCellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
 	for (EventListener listener : eventListeners) {
 	    listener.addToEntity(entity);
 	}
 
 	// Attach this view's entity components to the view entity
 	for (EntityComponentEntry entry : entityComponents) {
 	    attachEntityComponent(entry.clazz, entry.comp);
 	}
 
 	connectedToCell = true;
     }
 
     /**
      * Disconnect this view from the given cell.
      */
     private void detachFromCell (AppCell cell) {
 
 	cell.detachView(this, RendererType.RENDERER_JME);
 	detachEventListeners(getEntity());
 
 	// Detach this view's event listeners
 	AppCellRendererJME cellRenderer = (AppCellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
 	for (EventListener listener : eventListeners) {
 	    listener.removeFromEntity(entity);
 	}
 
 	// Detach this view's entity components
 	for (EntityComponentEntry entry : entityComponents) {
 	    detachEntityComponent(entry.clazz);
 	}
 
 	connectedToCell = false;
     }
 
     /**
      * Add an event listener to this view.
      * @param listener The listener to add.
      */
     public synchronized void addEventListener (EventListener listener) {
 	if (hasEventListener(listener)) return;
 	eventListeners.add(listener);
 	listener.addToEntity(entity);
     }
 
     /**
      * Remove an event listener from this view.
      * @param listener The listener to remove.
      */
     public synchronized void removeEventListener (EventListener listener) {
 	if (!hasEventListener(listener)) return;
 	eventListeners.remove(listener);
 	listener.removeFromEntity(entity);
     }
 
     /**
      * Does this view have the given listener attached to it?
      * @param listener The listener to check.
      */
     public synchronized boolean hasEventListener (EventListener listener) {
 	return eventListeners.contains(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public synchronized void addEntityComponent (Class clazz, EntityComponent comp) {
 
 	// Is this class already in the list?
 	EntityComponentEntry entry = getEntityComponentEntry(clazz);
 	if (entry != null) {
 	    // If it already has the same component do nothing.
 	    if (entry.comp != comp) {
 
 		// Class is in the list but component has changed
 		detachEntityComponent(clazz);
 		entry.comp = comp;
 		attachEntityComponent(clazz, entry.comp);
 	    }
 
 	} else {
 
 	    entityComponents.add(new EntityComponentEntry(clazz, comp));
 	    attachEntityComponent(clazz, comp);
 	}
     }
 
     /**
      * {@inheritDoc}
      */
     public synchronized void removeEntityComponent (Class clazz) {
 
 	// Is this class already not in the list?
 	EntityComponentEntry entry = getEntityComponentEntry(clazz);
 	if (entry != null) {
 	    entityComponents.remove(entry);
 	    detachEntityComponent(clazz);
 	}
     }
 
     /**
      * Attach the given entity component to the view's entity.
      */
     public void attachEntityComponent (Class clazz, EntityComponent comp) {
 	entity.addComponent(clazz, comp);
 	comp.setEntity(entity);
     }
 
     /**
      * Detach the given entity component class from the view's entity.
      */
     public void detachEntityComponent (Class clazz) {
 	entity.removeComponent(clazz);
     }
 
     /**
      * Returns the given entity component entry in the entity components list.
      */
     private synchronized EntityComponentEntry getEntityComponentEntry (Class clazz) {
 	for (EntityComponentEntry entry : entityComponents) {
 	    if (entry.clazz == clazz) {
 		return entry;
 	    }
 	}
 	return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public synchronized EntityComponent getEntityComponent (Class clazz) {
 	EntityComponentEntry entry = getEntityComponentEntry(clazz);
 	if (entry.comp == null) {
 	    return null;
 	} else {
 	    return entry.comp;
 	}
     }
 
     /**
      * Return this view's cell renderer entity. 
      */
     private Entity getCellRendererEntity () {
 	AppCell cell = (AppCell) getCell();
 	if (cell == null) return null;
 	AppCellRendererJME cellRenderer = (AppCellRendererJME)cell.getCellRenderer(RendererType.RENDERER_JME);
 	if (cellRenderer == null) return null;
 	return cellRenderer.getEntity();
     }
 
     /**
      * Return this view's entity. 
      */
     Entity getEntity () {
 	return entity;
     }
 
     public void setParentEntity (Entity parentEntity) {
 	if (entity == null) return;
 
 	// Detach from previous parent entity
 	Entity prevParentEntity = entity.getParent();
 	if (prevParentEntity != null) {
 	    prevParentEntity.removeEntity(entity);
 	    RenderComponent rcEntity = (RenderComponent)entity.getComponent(RenderComponent.class);
 	    if (rcEntity != null) {
 		rcEntity.setAttachPoint(null);
 	    }
 	}
 	
 	// Attach to new parent entity
 	if (parentEntity != null) {
 	    parentEntity.addEntity(entity);
 	    RenderComponent rcParentEntity = 
 		(RenderComponent) parentEntity.getComponent(RenderComponent.class);
 	    RenderComponent rcEntity = (RenderComponent)entity.getComponent(RenderComponent.class);
 	    if (rcParentEntity != null && rcParentEntity.getSceneRoot() != null && rcEntity != null) {
 		rcEntity.setAttachPoint(rcParentEntity.getSceneRoot());
 	    }
 	}
     }
 }
 
