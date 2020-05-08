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
 package org.jdesktop.wonderland.modules.appbase.client;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import org.jdesktop.wonderland.modules.appbase.client.Window2DViewWorld;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.image.Texture2D;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.util.geom.BufferUtils;
 import java.awt.Point;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.wonderland.client.input.EventListener;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 // TODO: for debug
 
 /**
  * The generic 2D window superclass. All 2D windows in Wonderland have this root class. Instances of this 
  * class are created via App2D.createWindow.
  *
  * Windows can be arranged into a stack with other windows. Each window occupies a unique position in the 
  * stack. The lowest window is at position 0, the window immediately above that is at position 1, and so on.
  *
  * @author deronj
  */ 
 
 @ExperimentalAPI
 public abstract class Window2D extends Window {
 
     /** The XY (planar) translation of the window within the cell. */
     protected Vector2f xyTranslation;
 
     /** The width of the window (in pixels) */
     protected int width;
 
     /** The height of the window (in pixels) */
     protected int height;
 
     /** Is the window top-level (that is, does it have a Frame?) */
     protected boolean topLevel;
 
     /** The size of the window pixels */
     protected Vector2f pixelScale;
 
     /** The string to display as the window title */
     protected String title;
 
     /** The texture which contains the contents of the window */
     protected Texture texture;
 
     /** A (fairly short) list of views on this window */
     protected LinkedList<Window2DView> views = new LinkedList<Window2DView>();
 
     /** The primary world view of a window */
     protected Window2DViewWorld viewWorld;
 
     /** Listeners for key events */
     protected ArrayList<KeyListener> keyListeners = null;
 
     /** Listeners for mouse events */
     protected ArrayList<MouseListener> mouseListeners = null;
 
     /** Listeners for mouse motion events */
     protected ArrayList<MouseMotionListener> mouseMotionListeners = null;
 
     /** Listeners for mouse wheel events */
     protected ArrayList<MouseWheelListener> mouseWheelListeners = null;
 
     /** The spatial displacement specified by the user */
     protected Vector3f userDisplacement = new Vector3f();
 
     /** The angle of rotation around the local Y axis (in radians) specified by the user */
     protected float rotY;
 
     /** The local (cell relative) Z depth */
     protected float depth = 0f;
 
     /**
      * Create a Window2D instance and its "World" view.
      *
      * @param app The application to which this window belongs.
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
      * @param pixelScale The size of the window pixels.
      * @throws InstantiationException if the window cannot be created.
      */
     public Window2D (App app, int width, int height, boolean topLevel, Vector2f pixelScale) 
 	throws InstantiationException
     {
 	super(app);
 	this.width = width;
 	this.height = height;
 	this.topLevel = topLevel;
 	this.pixelScale = pixelScale;
 
 	// By default every window has a primary world view, which is 
 	// visible when the window is visible
 	float widthWorld = (float)width * (float)pixelScale.x;
 	float heightWorld = (float)height * (float)pixelScale.y;
        	viewWorld = (Window2DViewWorld) createView("World");
 	if (viewWorld == null) {
 	    throw new InstantiationException("Cannot create world view of window");
 	}
 	viewWorld.setSize(widthWorld, heightWorld);
 	viewWorld.setTopLevel(topLevel);
 	viewWorld.setVisible(true);
 
 	updateAll();
     }
 
     /**
      * {@inheritDoc}
      */
     public void cleanup () {
 	super.cleanup();
 
 	app = null;
 	viewWorld = null;
 
 	if (views != null) {
 	    for (Window2DView view : views) {
 		view.cleanup();
 	    }
 	    views.clear();
 	    views = null;
 	}
 
 	texture = null;
     }
 
     /**
      * Return the stack position of this window.
      */
     public int getStackPosition () {
 	return ((App2D)app).stack.getStackPosition(this);
     }
 
     /**
      * Specify the XY translation.
      */
     // TODO: should this apply to the other views?
     public void setXYTranslation (Vector2f trans) {
 	xyTranslation = trans;
 	viewWorld.setTranslation(new Vector3f(xyTranslation.x, xyTranslation.y, depth));
 	update(Window2DView.CHANGED_TRANSFORM);
     }
 
     /**
      * Specify the depth of the window (relative to the center z=0 plane).
      */
     public void setDepth (float depth) {
 	this.depth = depth;
 	viewWorld.setTranslation(new Vector3f(xyTranslation.x, xyTranslation.y, depth));
 	update(Window2DView.CHANGED_TRANSFORM);
     }
 
     /**
      * Returns the depth of the window.
      */
     public float getDepth () {
 	return depth;
     }
 
     /**
      * Set the translation of this window so it is positioned relative to the given window
      * at an offset of (x, y) where the top left corner of the interior of the given window
      * is (0, 0). This is also placed a slight depth epsilon above the given window.
      */
     public void positionRelativeTo (Window2D window, int x, int y) {
 	Vector2f pixelScale = window.getPixelScale();
 	Vector2f offset = new Vector2f();
	offset.x = (window.getWidth() * pixelScale.x) / 2f;
	offset.y = (window.getHeight() * pixelScale.y) / 2f;
 	offset.x += x * pixelScale.x;
 	offset.y -= y * pixelScale.y;
 	setXYTranslation(offset);
 	setDepth(window.getDepth() + viewWorld.getPopupDepthOffset());
     }
 
     /**
      * Resize the window. Note that window contents will be lost when the window is resized.
      * The visual representations of the window are updated accordingly.
      *
      * @param width The new width of the window.
      * @param height The new height of the window.
      */
     public void setSize (int width, int height) {
 	update(setSizeNoUpdate(width, height));
     }
 
     /**
      * If the new width differs from the old width, or the new height differs from the old height,
      * update the old width and height to the new values and return Window2DView.CHANGED_SIZE,
      * an update change flag which indicates that the size has been changed. Otherwise return 0.
      * The visual representations of the window are not changed.
      */
     protected int setSizeNoUpdate (int width, int height) {
 	if (this.width != width || this.height != height) {
 	    this.width = width;
 	    this.height = height;
 	    return Window2DView.CHANGED_SIZE;
 	}
 	return 0;
     }
 
     /**
      * The width of the window (excluding the decoration).
      */
     public int getWidth () {
 	return width;
     }
 
     /** 
      * The height of the window (excluding the decoration).
      */
     public int getHeight () { 
 	return height; 
     }
 
     /** 
      * Move this window in the stack so that it is above the given sibling window.
      * The visual representations of the window are updated accordingly.
      *
      * @param sibWin The window which will be directly below this window after this call.
      */
     public void setSiblingAbove (Window2D sibWin) {
 	update(setSiblingAboveNoUpdate(sibWin));
     }
 
     /**
      * If sibWin == null return 0. Otherwise move this window in the stack so that it is above the given
      * sibling window. The visual representations of the window are not changed.
      *
      * @param sibWin The sibling window.
      */
     protected int setSiblingAboveNoUpdate(Window2D sibWin) {
 	if (sibWin == null) return 0;
 
 	((App2D)app).windowRemove(this);
 	((App2D)app).windowAddSiblingAbove(this, sibWin);
 	return Window2DView.CHANGED_STACK;
     }
 
     /**
      * Set both the window size and the sibling above in the stack in the same call.
      * This is like performing the following:
      * <br><br>
      * setSize(width, height);
      * <br>
      * setSiblingAbove(sibWin);
      * <br><br>
      * The visual representations of the window are updated accordingly.
      * 
      * @param width The new width of the window.
      * @param height The new height of the window.
      * @param sibWin The window which will be directly below this window after this call.
      */
     public void configure (int width, int height, Window2D sibWin) {
        	update(configureNoUpdate(width, height, sibWin));
     }
 
     /**
      * First calls setSizeNoUpdate and then calls setSiblingAboveNoUpdate. Returns
      * the accumulation (that is, the OR) of the two return values of these methods.
      * The visual representations of the window are not changed.
      *
      * @param width The new width of the window.
      * @param height The new height of the window.
      * @param sibWin The window which will be directly below this window after this call.
      */
     protected int configureNoUpdate (int width, int height, Window2D sibWin) {
 	int chgMask = 0;
 	chgMask |= setSizeNoUpdate(width, height);
 	chgMask |= setSiblingAboveNoUpdate(sibWin);
 	return chgMask;
     }
 
     /** 
      * Returns the  The size of the window pixels 
      */
     public Vector2f getPixelScale () {
 	return pixelScale;
     }
 
     /**
      * Change the visibility of the window 
      *
      * @param visible Whether the window should be visible.
      */
     public void setVisible (boolean visible) {
 	if (this.visible == visible) return;
 	super.setVisible(visible);
 	update(Window2DView.CHANGED_VISIBILITY);
     }
     
     /**
      * Change whether this is a top-level window or not. A top-level window has an enclosing frame.
      */
     public void setTopLevel (boolean topLevel) { 
 	if (this.topLevel == topLevel) return;
 	this.topLevel = topLevel;
 	update(Window2DView.CHANGED_VISIBILITY |
 	       Window2DView.CHANGED_TOP_LEVEL);
     }
 
     /**
      * Is this a top-level window?
      */
     public boolean isTopLevel () { 
 	return topLevel;
     }
 
     /**
      * Add a new sibling window to the window stack of this window's app. The sibling will be
      * placed directly below this window.
      *
      * @param sibWin The sibling window to add to the stack.
      */
     public void addSiblingAbove (Window2D sibWin) {
 	((App2D)app).windowAddSiblingAbove(this, sibWin);
 	update(Window2DView.CHANGED_STACK);
     }
 
     /**
      * Move this window to the top of the window stack.
      * 
      */
     public void toFront () {
 	((App2D)app).windowToFront(this);
 	update(Window2DView.CHANGED_STACK);
     }
 
     /**
      * Specify the window's title.
      *
      * @param title The string to display as the window title.
      */
     public void setTitle (String title) {
 	this.title = title;
 	update(Window2DView.CHANGED_TITLE);
     }
 
     /**
      * Return the window title.
      */
     public String getTitle () {
 	return title;
     }
 
     /**
      * Return the texture containing the window contents.
      */
     public Texture getTexture () {
 	return texture;
     }
 
     /** 
      * Returns the primary world view of this window
      */
     public Window2DViewWorld getPrimaryWorldView () {
 	return viewWorld;
     }
 
     /**
      * Create a view of this window in the named space.
      *
      * @param spaceName The name of the space in which the view  will reside.
      * @return The view created. Null indicates that this window type doesn't support the given spaceName.
      */
     public Window2DView createView (String spaceName) {
 	Window2DView view = (Window2DView) guiFactory.createView(this, spaceName);
 	if (view == null) return null;
 	views.add(view);
 	return view;
     }
 
     /**
      * Destroy the given view.
      *
      * @param view The view to destroy.
      */
     public void destroyView (Window2DView view) {
 	views.remove(view);
 	view.cleanup();
     }
 
     /**
      * Destroy all views in the given space.
      *
      * @param spaceName The name of the space whose views should be destroyed.
      */
     public void destroyViews (String spaceName) {
 	for (Window2DView view : views) {
 	    if (view.getSpaceName().equals(spaceName)) {
 		destroyView(view);
 	    }
 	}
 	// TODO: >>>> Window should go away when all the views are closed
     }
 
     /**
      * Destroy all views of the window.
      */
     public void destroyViews () {
 	for (Window2DView view : views) {
 	    destroyView(view);
 	}
     }
 
     /**
      * Returns an array containing all views of this window in the named space.
      *
      * @param spaceName The name of the space. 
      * @return An array of views. Null indicates that this window doesn't have any views in this space.
      */
     public Window2DView[] getView (String spaceName) {
 	int numInSpace = 0;
 	for (Window2DView view : views) {
 	    if (view.getSpaceName().equals(spaceName)) {
 		numInSpace++;
 	    }
 	}
 	if (numInSpace == 0) return null;
 
 	Window2DView[] ary = new Window2DView[numInSpace];
 	int i = 0;
 	for (Window2DView view : views) {
 	    if (view.getSpaceName().equals(spaceName)) {
 		ary[i++] = view;
 	    }
 	}
 	return ary;
     }
 
     /**
      * Deliver the given key event to this window.
      *
      * @param event The key event.
      */
     public void deliverEvent (KeyEvent event) {
         if (keyListeners == null) {
             return;
 	}
 
         for (KeyListener listener : keyListeners) {
             switch (event.getID()) {
 	    case KeyEvent.KEY_PRESSED :
 		listener.keyPressed(event);
 		break;
 	    case KeyEvent.KEY_RELEASED :
 		listener.keyReleased(event);
 		break;
 	    case KeyEvent.KEY_TYPED :
 		listener.keyTyped(event);
 		break;
             }
         }
     }
 
     /**
      * Deliver the given mouse event to this window.
      *
      * @param event The mouse event.
      */
     public void deliverEvent (MouseEvent event) {
         if        (event instanceof MouseWheelEvent) {
             deliverEvent((MouseWheelEvent)event);
 	} else if (event.getID() == MouseEvent.MOUSE_DRAGGED ||
 		   event.getID() == MouseEvent.MOUSE_MOVED) {
             deliverMouseMotionEvent(event);
 	}
 
         if (mouseListeners == null) {
             return;
 	}
 
         for (MouseListener listener : mouseListeners) {
             switch (event.getID()) {
 	    case MouseEvent.MOUSE_CLICKED :
 		listener.mouseClicked(event);
 		break;
 	    case MouseEvent.MOUSE_PRESSED :
 		listener.mousePressed(event);
 		break;
 	    case MouseEvent.MOUSE_RELEASED :
 		listener.mouseReleased(event);
 		break;
 	    case MouseEvent.MOUSE_ENTERED :
 		listener.mouseEntered(event);
 		break;
 	    case MouseEvent.MOUSE_EXITED :
 		listener.mouseExited(event);
 		break;
             }
         }
     }   
 
     /**
      * Deliver the given mouse motion event to the window.
      *
      * @param event The mouse motion event to deliver.
      */
     private void deliverMouseMotionEvent (MouseEvent event) {
         if (mouseMotionListeners == null) {
             return;
 	}
 
         for (MouseMotionListener listener : mouseMotionListeners) {
             switch (event.getID()) {
 	    case MouseEvent.MOUSE_MOVED :
 		listener.mouseMoved(event);
 		break;
 	    case MouseEvent.MOUSE_DRAGGED :
 		listener.mouseDragged(event);
 		break;
             }
         }
     }
 
     /**
      * Deliver the given mouse wheel event to the window.
      *
      * @param event The mouse wheel event to deliver.
      */
     protected void deliverEvent (MouseWheelEvent event) {
         if (mouseWheelListeners == null) {
             return;
 	}
 
         for (MouseWheelListener listener : mouseWheelListeners) {
             listener.mouseWheelMoved(event);
         }
     }
 
     /**
      * Add a new listener for key events.
      *
      * @param listener The key listener to add.
      */
     public void addKeyListener (KeyListener listener) {
         if (keyListeners == null) {
             keyListeners = new ArrayList<KeyListener>();
 	}
         keyListeners.add(listener);
     }
 
     /**
      * Add a new listener for mouse events.
      *
      * @param listener The mouse listener to add.
      */
     public void addMouseListener(MouseListener listener) {
         if (mouseListeners == null) {
             mouseListeners = new ArrayList<MouseListener>();
 	}
         mouseListeners.add(listener);
     }
 
     /**
      * Add a new listener for mouse motion events.
      *
      * @param listener The mouse motion listener to add.
      */
     public void addMouseMotionListener(MouseMotionListener listener) {
         if (mouseMotionListeners == null) {
             mouseMotionListeners = new ArrayList<MouseMotionListener>();
 	}
         mouseMotionListeners.add(listener);
     }
 
     /**
      * Add a new listener for mouse wheel events.
      *
      * @param listener The mouse wheel listener to add.
      */
     public void addMouseWheelListener(MouseWheelListener listener) {
         if (mouseWheelListeners == null) {
             mouseWheelListeners = new ArrayList<MouseWheelListener>();
 	}
         mouseWheelListeners.add(listener);
     }
 
     /**
      * Add a listener for key events.
      *
      * @param listener The key listener to add.
      */
     public void removeKeyListener (KeyListener listener) {
         if (keyListeners == null) return;
 	keyListeners.remove(listener);
 	if (keyListeners.size() == 0) {
             keyListeners = null;
 	}
     }
 
     /**
      * Remove a listener for mouse events.
      *
      * @param listener The mouse listener to remove.
      */
     public void removeMouseListener(MouseListener listener) {
         if (mouseListeners == null) return;
 	mouseListeners.remove(listener);
 	if (mouseListeners.size() == 0) {
             mouseListeners = null;
 	}
     }
 
     /**
      * Remove a listener for mouse motion events.
      *
      * @param listener The mouse motion listener to remove.
      */
     public void removeMouseMotionListener(MouseMotionListener listener) {
         if (mouseMotionListeners == null) return;
 	mouseMotionListeners.remove(listener);
 	if (mouseMotionListeners.size() == 0) {
             mouseMotionListeners = null;
 	}
     }
 
     /**
      * Remove a listener for mouse wheel events.
      *
      * @param listener The mouse wheel listener to remove.
      */
     public void removeMouseWheelListener(MouseWheelListener listener) {
         if (mouseWheelListeners == null) return;
 	mouseWheelListeners.remove(listener);
 	if (mouseWheelListeners.size() == 0) {
             mouseWheelListeners = null;
 	}
     }
 
     /** 
      * Returns an iterator over all the views of this window.
      */
     protected Iterator<Window2DView> getViewIterator () {
 	return views.iterator();
     }
 
     /** 
      * Updates all visual representations of this window as if every attribute had been changed.
      */
     protected void updateAll () {
 	update(Window2DView.CHANGED_ALL);
     }
 
     /** 
      * Updates the visual representations of this window based a a list of changes.
      *
      * @param changeMask The change flag mask which contains a 1 bit for every attribute set which has changed.
      */
     protected void update (int changeMask) {
 	if ((changeMask & Window2DView.CHANGED_SIZE) != 0) {
 	    updateTexture();
 	}
 	updateViews(changeMask);
     }
 
     /** 
      * The window size has been updated. Recreate the texture.
      */
     protected void updateTexture () {
 
 	// TODO: someday dynamically detect graphics card support for NPOT
 	int roundedWidth = getSmallestEnclosingPowerOf2(width);
 	int roundedHeight = getSmallestEnclosingPowerOf2(height);
 
 	// Check if we already have the size we want
 	if (texture != null) {
 	    int texWidth = texture.getImage().getWidth();
 	    int texHeight = texture.getImage().getHeight();
 	    if (texWidth == roundedWidth &&
 		texHeight == roundedHeight) {
 		return;
 	    }
 	}
 
 	// Create the buffered image using dummy data to initialize it
 	// TODO: change this after by ref textures are implemented
 	ByteBuffer data = BufferUtils.createByteBuffer(roundedWidth * roundedHeight * 4);
 	Image image = new Image(Image.Format.RGB8, roundedWidth, roundedHeight, data);
 
 	// Create the texture which wraps the image 
 	texture = new Texture2D();
 	texture.setImage(image);
         texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
         texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
 	texture.setApply(Texture.ApplyMode.Replace);
 	
 	/* TODO: NOTYET: set anisotropic filtering
         // This improves texture filtering when the texture is close up from the side,
 	// viewing down the length of the window, but it actually causes more drop outs
 	// in magnified text in some cases.
 	// The anticipated Java3D code was:
 	texture.setAnisotropicFilterMode(Texture2D.ANISOTROPIC_SINGLE_VALUE);
 	texture.setAnisotropicFilterDegree(8.0f);
 	*/
     }
 
     /** 
      * Update every view based on current window state.
      *
      * @param changeMask The change flag mask which contains a 1 bit for every attribute set which has changed.
      */
     protected void updateViews (int changeMask) {
 	for (Window2DView view : views) {
 	    view.update(changeMask);
 	}
     }
 
     public void forceTextureIdAssignment () {
 	if (views.size() <= 0) {
 	    System.err.println("Trying to assign texture ID before view has been created.");
 	    System.exit(1);
 	}
 	for (Window2DView view : views) {
 	    view.forceTextureIdAssignment();
 	}	
     }
 
     /** 
      * Rounds up the given value to the nearest power of two which is larger or equal to the value.
      * 
      * @param The value to round.
      */
     int getSmallestEnclosingPowerOf2(int value) {
 
 	if (value < 1)
 	    return value;
 	
 	int powerValue = 1;
 	for (;;) {
 	    powerValue *= 2;
 	    if (value <= powerValue) {
                 return powerValue;
 	    }
 	}
     }
 
     /**
      * Called by the GUI to close the window.
      */
     public void userClose () {
 	cleanup();
     }
 
     /**
      * Called by the GUI to move the window to the front (top) of the window stack.
      */
     public void userToFront () {
 	toFront();
     }
 
     /** 
      * Returns the spatial displacement specified by the user.
      */
     public Vector3f getUserDisplacement () {
 	return userDisplacement;
     }
 
     /**
      * Returns the y rotation angle of the window (in radians) specified by the user
      */
     public float getRotateY () {
 	return rotY;
     }
 
     /**
      * Transform the given 3D point in world coordinates into the corresponding point in the pixel space of the image 
      * of the world view of the window. The given point must be on the surface of the window.
      * @param point The point to transform.
      * @return the 2D position of the pixel space the window's image, or null if the point is not within the window
      * or is not on the surface of the window.
      */
     public Point calcWorldPositionInPixelCoordinates (Vector3f point) { 
 	return viewWorld.calcPositionInPixelCoordinates(point);
     }
 
     /**
      * Add an event listener to this window's world view.
      * @param listener The listener to add.
      */
     public void addWorldEventListener (EventListener listener) {
 	viewWorld.addEventListener(listener);
     }
 
     /**
      * Remove an event listener from this window's world view.
      * @param listener The listener to remove.
      */
     public void removeEventListener (EventListener listener) {
 	viewWorld.removeEventListener(listener);
     }
 
     /**
      * Does this window's world view have the given listener attached to it?
      * @param listener The listener to check.
      */
     public boolean hasEventListener (EventListener listener) {
 	return viewWorld.hasEventListener(listener);
     }
 
     /**
      * Add an entity component to this window's world view.
      */
     public void addWorldEntityComponent (Class clazz, EntityComponent comp) {
 	viewWorld.addEntityComponent(clazz, comp);
     }
 
     /**
      * Remove an entity component from this window's world view.
      */
     public void removeWorldEntityComponent (Class clazz) {
 	viewWorld.removeEntityComponent(clazz);
     }
 
     /**
      * Return the entity component of the window's world view for the given class.
      * @param listener The listener to check.
      */
     public EntityComponent getWorldEntityComponent (Class clazz) {
 	return viewWorld.getEntityComponent(clazz);
     }
 
 }
