 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.appbase.client.cell.gui.guidefault;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.math.Matrix4f;
 import com.jme.math.Quaternion;
 import com.jme.math.Ray;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import java.awt.Button;
 import java.awt.Rectangle;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.modules.appbase.client.gui.Window2DView;
 import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.cell.AppCell;
 import org.jdesktop.wonderland.modules.appbase.client.gui.Window2DViewWorld;
 import org.jdesktop.wonderland.client.jme.utils.graphics.TexturedQuad;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import com.jme.scene.state.TextureState;
 import java.nio.FloatBuffer;
 import com.jme.util.geom.BufferUtils;
 import com.jme.scene.TexCoords;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.LinkedList;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.mtgame.RenderUpdater;
 import org.jdesktop.wonderland.client.input.EventListener;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.input.InputManager3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
 
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
     /** The current width of the view (excluding the frame). */
     protected float width;
     /** The current height of the view (excluding the frame). */
     protected float height;
     /** The textured 3D object in which displays the window contents */
     protected ViewGeometryObject geometryObj;
     /** Whether the view's entity tree is connected to its cell */
     protected boolean connectedToCell;
     /** The current cell to which this view is attached. */
     protected AppCell currentCell;
     /** Whether the view's entity tree is attached to the HUD */
     protected boolean attachedToHud;
     /** 
      * The root of the view subgraph. This contains all geometry and is 
      * connected to the local scene graph of the cell.
      */
     protected Node baseNode;
     /**
      * The view's entity.
      */
     private HudableEntity entity;
     /** The control arbitrator of the window */
     protected ControlArb controlArb;
     /** The frame displayed for this view when it is in world mode. */
     protected FrameWorldDefault worldFrame;
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
 
         private EntityComponentEntry(Class clazz, EntityComponent comp) {
             this.clazz = clazz;
             this.comp = comp;
         }
     }
     /** The entity components which should be attached to this view while the view is attached to its cell. */
     private LinkedList<EntityComponentEntry> entityComponents = new LinkedList<EntityComponentEntry>();
 
     /** Whether the view is in the HUD. */
     private boolean onHud;
 
     /** The location of the view when it is in the HUD. */
     private Point hudLocation;
 
     /** The caller-specified size of the view when it is in the HUD. */
     private Dimension hudSize;
 
     /** TODO: temp */
     private int hudZOrder;
 
     /**
      * Create a new instance of ViewWorldDefault.
      *
      * @param window The window displayed by the view.
      */
     public ViewWorldDefault(Window2D window) {
         super(window, "World");
         gui = new Gui2DInterior(this);
 
         controlArb = window.getApp().getControlArb();
 
         entity = new HudableEntity("View Entity");
         baseNode = new Node("View Base Node");
         RenderComponent rc =
                 ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(baseNode);
         entity.addComponent(RenderComponent.class, rc);
 
         hudLocation = new Point(0, 0);
     }
 
     /**
      * Clean up resources.
      */
     @Override
     public void cleanup() {
         super.cleanup();
         setVisible(false);
         if (geometryObj != null) {
             geometryObj.cleanup();
             geometryObj = null;
         }
         if (controlArb != null) {
             controlArb = null;
         }
         if (worldFrame != null) {
             worldFrame.cleanup();
             worldFrame = null;
         }
         detachEventListeners(entity);
         if (gui != null) {
             gui.cleanup();
             gui = null;
         }
         if (entity != null) {
             if (onHud) {
                 moveToWorld();
             }
             if (connectedToCell) {
                 detachFromCell((AppCell) getCell());
                 connectedToCell = false;
                 currentCell = null;
             }
             Entity parentEntity = entity.getParent();
             if (parentEntity != null) {
                 parentEntity.removeEntity(entity);
             }
             entity.removeComponent(RenderComponent.class);
             
             // Make sure that entity listeners and components are really gone
             // There is a case with movement into or out of the HUD where these
             // might get stuck on the entity
             for (EventListener listener : eventListeners) {
                 listener.removeFromEntity(entity);
             }
             for (EntityComponentEntry entry : entityComponents) {
                 detachEntityComponent(entry.clazz);
             }
             
             baseNode = null;
             entity = null;
         }
     }
 
     /**
      * Returns the depth offset above a base window that popup windows should be positioned.
      */
     public float getPopupDepthOffset() {
         return 0.01f;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setVisible(boolean visible) {
         viewVisible = visible;
         // TODO: TEMPORARY HACK: also call setVisible on the underlying window.
         // disable for dev3 (nsimpson)
         //window.setVisible(visible);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean getVisible() {
         return viewVisible;
     }
 
     /**
      * Returns whether the window is actually visible, that is,
      * the view visibility combined with the window visibility.
      */
     boolean getActuallyVisible() {
         return visible;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setTopLevel(boolean topLevel) {
         if (this.topLevel == topLevel) {
             return;
         }
         this.topLevel = topLevel;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean getTopLevel() {
         return topLevel;
     }
 
     /**
      * Sets the translation of the view. Don't forget to also call update(CHANGED_TRANSFORM) afterward.
      *
      * @param translation The new translation of the window relative to the center of the cell.
      */
     public void setTranslation(Vector3f translation) {
         this.translation.set(translation);
     }
 
     /**
      * Returns the translation of the view.
      */
     public Vector3f getTranslation() {
         return new Vector3f(translation);
     }
 
     /**
      * {@inheritDoc}
      */
     public float getWidth() {
         return width;
     }
 
     /**
      * {@inheritDoc}
      */
     public float getHeight() {
         return height;
     }
 
     /**
      * Returns the frame of view. 
      */
     FrameWorldDefault getFrame() {
         update(CHANGED_TOP_LEVEL);
         return worldFrame;
     }
 
     /**
      * {@inheritDoc}"
      */
     @Override
     public void update(int changeMask) {
 
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
                 if (worldFrame == null) {
                     worldFrame = (FrameWorldDefault) window.getApp().getDisplayer().getGui2DFactory().createFrame(this);
                 }
             } else {
                 if (worldFrame != null) {
                     worldFrame.cleanup();
                     worldFrame = null;
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
             if (worldFrame != null) {
                 worldFrame.setTitle(((Window2D) window).getTitle());
             }
         }
     }
 
     /** Update the view's geometry (for a size change) */
     protected void updateGeometrySize() throws InstantiationException {
 
         // Calculate view size appropriate for the current Hud mode
         if (onHud) {
             width = (float) getHUDWidth();
             height = (float) getHUDHeight();
         } else {
             Window2D window2D = (Window2D) window;
             Vector2f pixelScale = window2D.getPixelScale();
             width = pixelScale.x * (float) window2D.getWidth();
             height = pixelScale.y * (float) window2D.getHeight();
         }
 
         if (geometryObj == null) {
 
             // Geometry first time creation
             setGeometry(new ViewGeometryObjectDefault(this));
 
         } else {
 
             // Geometry resize
             geometryObj.updateSize();
         }
 
         if (worldFrame != null) {
             worldFrame.update();
         }
     }
 
     /** The window's texture may have changed */
     protected void updateTexture() {
         if (geometryObj != null) {
             geometryObj.updateTexture();
         }
     }
 
     /** Update the view's transform */
     protected void updateTransform() {
         if (onHud) {
             // Get rid of any non-zero rotation or non-unity scale that the node may have
             baseNode.setLocalRotation(new Quaternion());
             baseNode.setLocalScale(1.0f);
             baseNode.setLocalTranslation(new Vector3f((float)hudLocation.getX(),
                                                       (float)hudLocation.getY(),
                                                       0f));
         } else {
             // TODO: this is all we have for world mode now (no rotation)
             baseNode.setLocalTranslation(translation);
         }
     }
 
     /**
      * Returns the texture width.
      */
     public int getTextureWidth() {
         if (geometryObj != null) {
             return geometryObj.getTextureWidth();
         } else {
             return 0;
         }
     }
 
     /**
      * Returns the texture height.
      */
     public int getTextureHeight() {
         if (geometryObj != null) {
             return geometryObj.getTextureHeight();
         } else {
             return 0;
         }
     }
 
     /** Update the view's stacking relationships */
     protected void updateStack() {
         // TODO
     }
 
     /** Update the view's visibility */
     void updateVisibility() {
         AppCell cell = getCell();
 
         if (cell == null) {
             return;
         }
         baseNode.setName("ViewWorldDefault Node for cell " + getCell().getCellID().toString());
 
         visible = viewVisible && window.isVisible();
 
         if (onHud) {
             if (visible && !attachedToHud) {
                 attachToHud();
             } else if (!visible && attachedToHud) {
                 detachFromHud();
             }
         } else {
             if (visible && !connectedToCell) {
                 attachToCell(cell);
             } else if (!visible && connectedToCell) {
                 detachFromCell(cell);
             }
         }
     }
 
     /** Get the cell of the view */
     protected AppCell getCell() {
         return (AppCell) window.getApp().getDisplayer();
     }
 
     /**
      * If you want to customize the geometry of a view implement
      * this interface and pass an instance to view.setGeometry.
      */
     public abstract class ViewGeometryObject extends Node {
 
         private Point lastPosition = null;
         /** The view for which the geometry object was created. */
         protected ViewWorldDefault view;
 
         /**
          * Create a new instance of ViewGeometryObject. Attach your geometry as
          * children of this Node.
          *
          * @param view The view object the geometry is to display.
          */
         public ViewGeometryObject(ViewWorldDefault view) {
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
         public void cleanup() {
             view = null;
         }
 
         /**
          * The size of the view (and corresponding window texture) has
          * changed. Make the corresponding change to your geometry in
          * this method.
          */
         public abstract void updateSize();
 
         /**
          * The view's texture may have changed. You should get the view's
          * texture and make your geometry display it.
          *
          */
         public abstract void updateTexture();
 
         /**
          * Transform the given 3D point in world coordinates into the corresponding point
          * in the texel space of the geometry. The given point must be in the plane of the window.
          * <br><br>
          * Note: works when called with both a vector or a point.
          * @param point The point to transform.
          * @param clamp If true return the last position if the argument point is null or the resulting
          * position is outside of the geometry's rectangle. Otherwise, return null if these conditions hold.
          * @return the 2D position of the pixel space the window's image.
          */
         public Point calcWorldPositionInPixelCoordinates(Vector3f point, boolean clamp) {
             if (point == null) {
                 if (clamp) {
                     return lastPosition;
                 } else {
                     lastPosition = null;
                     return null;
                 }
             }
             logger.fine("point = " + point);
 
             // First calculate the actual coordinates of the corners of the view in world coords.
             Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
             Vector3f topRightLocal = new Vector3f(width / 2f, height / 2f, 0f);
             Vector3f bottomLeftLocal = new Vector3f(-width / 2f, -height / 2f, 0f);
             Vector3f bottomRightLocal = new Vector3f(width / 2f, -height / 2f, 0f);
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
             if (clamp) {
                 if (x < 0 || x >= 1 || y < 0 || y >= 1) {
                     logger.fine("Outside!");
                     return lastPosition;
                 }
             }
 
             int winWidth = ((Window2D) window).getWidth();
             int winHeight = ((Window2D) window).getHeight();
             logger.fine("winWidth = " + winWidth);
             logger.fine("winHeight = " + winHeight);
 
             logger.fine("Final xy " + (int) (x * winWidth) + ", " + (int) (y * winHeight));
             lastPosition = new Point((int) (x * winWidth), (int) (y * winHeight));
             return lastPosition;
         }
 
         /**
          * Returns the texture width.
          */
         public abstract int getTextureWidth();
 
         /**
          * Returns the texture height.
          */
         public abstract int getTextureHeight();
 
         /**
          * Returns the texture state.
          */
         public abstract TextureState getTextureState();
 
         /**
          * Given a point in the pixel space of the Wonderland canvas calculates
          * the texel coordinates of the point on the geometry where a
          * ray starting from the current eye position intersects the geometry.
          *
          * Note on subclassing:
          *
          * If the geometry is nonplanar it is recommended that the subclass
          * implement this method by performing a pick. If this pick misses the
          * subclass will need to decide how to handle the miss. One possible way to
          * handle this is to assume that there is a planar "halo" surrounding the
          * the window with which the ray can be intersected.
          */
         protected Point calcIntersectionPixelOfEyeRay(int x, int y) {
 
             // Calculate the ray
             Ray rayWorld = InputManager3D.getInputManager().pickRayWorld(x, y);
 
             // Calculate an arbitrary point on the plane (in this case, the top left corner)
             Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
             Matrix4f local2World = getLocalToWorldMatrix(null);
             Vector3f topLeftWorld = local2World.mult(topLeftLocal, new Vector3f());
 
             // Calculate the plane normal
             Vector3f planeNormalWorld = getPlaneNormalWorld();
 
             // Now find the intersection of the ray with the plane
             Vector3f intPointWorld = calcPlanarIntersection(rayWorld, topLeftWorld, planeNormalWorld);
             if (intPointWorld == null) {
                 return null;
             }
 
             // TODO: opt: we can optimize the following by reusing some of the intermediate
             // results from the previous steps
             Point pt = calcWorldPositionInPixelCoordinates(intPointWorld, false);
             return pt;
         }
 
         /**
          * Returns the plane normal of the window in world coordinates.
          */
         protected Vector3f getPlaneNormalWorld() {
             // Find two vectors on the plane and take the cross product and then normalize
 
             Vector3f topLeftLocal = new Vector3f(-width / 2f, height / 2f, 0f);
             Vector3f topRightLocal = new Vector3f(width / 2f, height / 2f, 0f);
             Vector3f bottomLeftLocal = new Vector3f(-width / 2f, -height / 2f, 0f);
             Vector3f bottomRightLocal = new Vector3f(width / 2f, -height / 2f, 0f);
             logger.fine("topLeftLocal = " + topLeftLocal);
             logger.fine("topRightLocal = " + topRightLocal);
             logger.fine("bottomLeftLocal = " + bottomLeftLocal);
             logger.fine("bottomRightLocal = " + bottomRightLocal);
             Matrix4f local2World = getLocalToWorldMatrix(null); // TODO: prealloc
             Vector3f topLeftWorld = local2World.mult(topLeftLocal, new Vector3f()); // TODO:prealloc
             Vector3f topRightWorld = local2World.mult(topRightLocal, new Vector3f()); // TODO:prealloc
             Vector3f bottomLeftWorld = local2World.mult(bottomLeftLocal, new Vector3f()); // TODO:prealloc
             Vector3f bottomRightWorld = local2World.mult(bottomRightLocal, new Vector3f()); // TODO:prealloc
 
             Vector3f leftVec = bottomLeftWorld.subtract(topLeftWorld);
             Vector3f topVec = topRightWorld.subtract(topLeftWorld);
             return leftVec.cross(topVec).normalize();
         }
 
         /**
          * Calculates the point in world coordinates where the given ray
          * intersects the "world plane" of this geometry. All inputs are
          * in world coordinates. Returns null if the ray doesn't intersect the plane.
          *
          * @param ray The ray.
          * @param planePoint A point on the plane.
          * @param planeNormal The plane normal vector.
          */
         protected Vector3f calcPlanarIntersection(Ray ray,
                 Vector3f planePoint, Vector3f planeNormal) {
 
             // Uses the following formula:
             //
             // t = (planeNormal dot (planePointWorld – rayStart)) / (planeNormal dot rayVectorWorld)
             //
             // Then use the parameter t in the line equation P = P0 + t * rayDirection to calculate
             // the intersection point P.
             //
             // Source: http://www.thepolygoners.com/tutorials/lineplane/lineplane.html
 
             // First calculate planeNormal dot rayDirection
             float denominator = planeNormal.dot(ray.getDirection());
             if (denominator == 0f) {
                 // No intersection
                 return null;
             }
 
             // Now calculate the numerator
             Vector3f vecTmp = planePoint.subtract(ray.getOrigin(), new Vector3f());
             float numerator = planeNormal.dot(vecTmp);
 
             // Now calculate t
             float t = numerator / denominator;
 
             // Now plug t into the ray equation P = P0 + t * rayDirection
             Vector3f p = ray.getDirection().mult(t).add(ray.getOrigin());
             return p;
         }
 
         /**
          *  Set the HUD Z order for the geometry.
          */
          public abstract void setHudZOrder (int zOrder);
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
         private ViewGeometryObjectDefault(ViewWorldDefault view) {
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
         @Override
         public void cleanup() {
             super.cleanup();
             if (quad != null) {
                 detachChild(quad);
                 quad = null;
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void updateSize() {
             float width = view.getWidth();
             float height = view.getHeight();
             quad.initialize(width, height);
             updateTexture();
         }
 
         /**
          * {@inheritDoc}
          */
         public void updateTexture() {
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
         public int getTextureWidth() {
             return quad.getTexture().getImage().getWidth();
         }
 
         /**
          * Returns the texture height.
          */
         public int getTextureHeight() {
             return quad.getTexture().getImage().getHeight();
         }
 
         /**
          * Returns the texture state.
          */
         public TextureState getTextureState() {
             return quad.getTextureState();
         }
 
         /**
          *  {inheritDoc}
          */
          public void setHudZOrder (int zOrder) {
              if (quad != null) {
                  quad.setZOrder(zOrder);
              }
          }
 
     }
 
     /**
      * Replace this view's geometry with different geometry.
      * The updateSize method of the geometry object will be called 
      * to provide geometry object with the view's current size.
      *
      * @param newGeometryObj The new geometry object for this window.
      */
     public void setGeometry(ViewGeometryObject newGeometryObj) {
 
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
     public Point calcPositionInPixelCoordinates(Vector3f point, boolean clamp) {
         if (geometryObj == null) {
             return null;
         }
         return geometryObj.calcWorldPositionInPixelCoordinates(point, clamp);
     }
 
     /**
      * {@inheritDoc}
      */
     public Point calcIntersectionPixelOfEyeRay(int x, int y) {
         if (geometryObj == null) {
             return null;
         }
         return geometryObj.calcIntersectionPixelOfEyeRay(x, y);
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
     public static float pointLineDistance(final Vector3f lineStart,
             final Vector3f lineEnd,
             final Vector3f point) {
         Vector3f a = new Vector3f(lineEnd);
         a.subtract(lineStart);
 
         Vector3f b = new Vector3f(lineStart);
         b.subtract(point);
 
         Vector3f cross = new Vector3f();
         cross.cross(a, b);
 
         return cross.length() / a.length();
     }
 
     /**
      * {@inheritDoc}
      */
     public void deliverEvent(Window2D window, MouseEvent3D me3d) {
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
         if (geometryObj == null) {
             return;
         }
 
         // Convert mouse event intersection point to 2D. For most events this is the intersection
         // point based on the destination pick details calculated by the input system, but for drag
         // events this needs to be derived from the actual hit pick details (because for drag events
         // the destination pick details might be overridden by a grab).
         Point point;
         if (me3d.getID() == MouseEvent.MOUSE_DRAGGED) {
             MouseDraggedEvent3D de3d = (MouseDraggedEvent3D) me3d;
             point = geometryObj.calcWorldPositionInPixelCoordinates(de3d.getHitIntersectionPointWorld(), true);
         } else {
             point = geometryObj.calcWorldPositionInPixelCoordinates(me3d.getIntersectionPointWorld(), false);
         }
         if (point == null) {
             // Event was outside our panel so do nothing
             // This can happen for drag events
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
 
     @Override
     public void forceTextureIdAssignment() {
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
         Texture tex = ((Window2D) window).getTexture();
         int texid = tex.getTextureId();
         logger.warning("ViewWorldDefault: allocated texture id " + texid);
         if (texid == 0) {
             logger.severe("Texture Id is still 0!!!");
         }
     }
 
     /**
      * Attach the event listeners of this view (and any associated frame) to the given entity.
      */
     private void attachEventListeners(Entity entity) {
         if (entity == null) {
             return;
         }
         if (gui != null) {
             ((Gui2D) gui).attachEventListeners(entity);
         }
         if (worldFrame != null) {
             worldFrame.attachEventListeners(entity);
         }
     }
 
     /**
      * Detach the event listeners of this view (and any associated frame) from the given entity.
      */
     private void detachEventListeners(Entity entity) {
         if (entity == null) {
             return;
         }
         if (gui != null) {
             ((Gui2D) gui).detachEventListeners(entity);
         }
         if (worldFrame != null) {
             worldFrame.detachEventListeners(entity);
         }
     }
 
     /**
      * Connect this view to the given cell.
      */
     private void attachToCell(AppCell cell) {
 
         cell.attachView(this, RendererType.RENDERER_JME);
         currentCell = cell;
         attachEventListeners(getEntity());
 
         // For debug
         //logger.severe("SCENE GRAPH AT ATTACH TO CELL:");
         //cell.logSceneGraph(RendererType.RENDERER_JME);
 
         // Attach this view's event listeners
         for (EventListener listener : eventListeners) {
             listener.addToEntity(entity);
         }
 
         // Attach this view's entity components to the view entity
         // TODO: are these still used?
         for (EntityComponentEntry entry : entityComponents) {
             attachEntityComponent(entry.clazz, entry.comp);
         }
 
         connectedToCell = true;
     }
 
     /**
      * Disconnect this view from the given cell.
      */
     private void detachFromCell(AppCell cell) {
 
         cell.detachView(this, RendererType.RENDERER_JME);
         currentCell = null;
         detachEventListeners(getEntity());
 
         // Detach this view's event listeners
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
     public synchronized void addEventListener(EventListener listener) {
         if (hasEventListener(listener)) {
             return;
         }
         eventListeners.add(listener);
         listener.addToEntity(entity);
     }
 
     /**
      * Remove an event listener from this view.
      * @param listener The listener to remove.
      */
     public synchronized void removeEventListener(EventListener listener) {
         if (!hasEventListener(listener)) {
             return;
         }
         eventListeners.remove(listener);
         listener.removeFromEntity(entity);
     }
 
     /**
      * Does this view have the given listener attached to it?
      * @param listener The listener to check.
      */
     public synchronized boolean hasEventListener(EventListener listener) {
         return eventListeners.contains(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public synchronized void addEntityComponent(Class clazz, EntityComponent comp) {
 
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
     public synchronized void removeEntityComponent(Class clazz) {
 
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
     public void attachEntityComponent(Class clazz, EntityComponent comp) {
         entity.addComponent(clazz, comp);
         comp.setEntity(entity);
     }
 
     /**
      * Detach the given entity component class from the view's entity.
      */
     public void detachEntityComponent(Class clazz) {
         entity.removeComponent(clazz);
     }
 
     /**
      * Returns the given entity component entry in the entity components list.
      */
     private synchronized EntityComponentEntry getEntityComponentEntry(Class clazz) {
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
     public synchronized EntityComponent getEntityComponent(Class clazz) {
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
     private Entity getCellRendererEntity() {
         AppCell cell = (AppCell) getCell();
         if (cell == null) {
             return null;
         }
         AppCellRendererJME cellRenderer = (AppCellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
         if (cellRenderer == null) {
             return null;
         }
         return cellRenderer.getEntity();
     }
 
     /**
      * Return this view's entity. 
      */
     Entity getEntity() {
         return entity;
     }
 
     public void setParentEntity(Entity parentEntity) {
         if (entity == null) {
             return;
         }
 
         // Detach from previous parent entity
         Entity prevParentEntity = entity.getParent();
         if (prevParentEntity != null) {
             prevParentEntity.removeEntity(entity);
             RenderComponent rcEntity = (RenderComponent) entity.getComponent(RenderComponent.class);
             if (rcEntity != null) {
                 rcEntity.setAttachPoint(null);
             }
         }
 
         // Attach to new parent entity
         if (parentEntity != null) {
             parentEntity.addEntity(entity);
             RenderComponent rcParentEntity =
                     (RenderComponent) parentEntity.getComponent(RenderComponent.class);
             RenderComponent rcEntity = (RenderComponent) entity.getComponent(RenderComponent.class);
             if (rcParentEntity != null && rcParentEntity.getSceneRoot() != null && rcEntity != null) {
                 rcEntity.setAttachPoint(rcParentEntity.getSceneRoot());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setOnHUD (boolean onHUD) {
         if (this.onHud == onHUD) return;
 
         this.onHud = onHUD;
         if (onHud) {
             moveToHud();
         } else {
             moveToWorld();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isOnHUD () {
         return onHud;
     }
     
     /**
      * {@inheritDoc}
      */
     public void setHUDLocation (int x, int y) {
         hudLocation = new Point(x, y);
         if (onHud) {
             update(CHANGED_SIZE);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public int getHUDX () {
         return hudLocation.x;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getHUDY () {
         return hudLocation.y;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setHUDSize(int width, int height) {
         hudSize = new Dimension(width, height);
         if (onHud) {
             update(CHANGED_SIZE);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public int getHUDWidth () {
         // If the caller hasn't specified a width, use the current window width 
         if (hudSize != null) {
             return hudSize.width;
         } else {
             return ((Window2D)window).getWidth();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public int getHUDHeight () {
         // If the caller hasn't specified a height, use the current window height 
         if (hudSize != null) {
             return hudSize.height;
         } else {
             return ((Window2D)window).getHeight();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setHUDBounds (Rectangle bounds) {
         hudLocation = new Point(bounds.x, bounds.y);
         hudSize = new Dimension(bounds.width, bounds.height);
         if (onHud) {
             update(CHANGED_TRANSFORM | CHANGED_SIZE);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void setHUDZOrder (int zOrder) {
         hudZOrder = zOrder;
         if (onHud) {
             updateHudState();
         }
     }
 
     /**
      * Moves this view into the HUD.
      */
     private void moveToHud () {
 
         // Detach entity from cell (without removing listeners or components!) and attach it to the HUD
         if (connectedToCell) {
             currentCell.detachView(this, RendererType.RENDERER_JME);
             connectedToCell = false;
         }
         if (visible) {
             attachToHud();
         }
         
         // Update HUD state for all entities
         updateHudState();
 
         // Finally, the view's render component must be in ortho mode
         entity.getComponent(RenderComponent.class).setOrtho(true);
     }
 
     /**
      * Moves this view back into the world.
      */
     private void moveToWorld () {
 
         // Detach entity from HUD and reattach entity to the cell
         if (visible) {
             detachFromHud();
             if (!connectedToCell) {
                 currentCell.attachView(this, RendererType.RENDERER_JME);
                 connectedToCell = true;
             }
         }
 
         // Update HUD state for all entities
         updateHudState();
 
         // Finally, the view's render component must be in perspective mode
        entity.getComponent(RenderComponent.class).setOrtho(true);
     }
 
     /**
      * Update HUD state for all entities of this view.
      */
     private void updateHudState () {
         updateHudStateForEntityAndChildren(entity);
     }
 
     /**
      * Updates the HUD state of the given entity and its children.
      * (This is done safely, within the render loop.
      */
     private void updateHudStateForEntityAndChildren (HudableEntity entity) {
         ClientContextJME.getWorldManager().addRenderUpdater(entity, this);
         for (int i=0; i<entity.numEntities(); i++) {
             updateHudStateForEntityAndChildren((HudableEntity)entity.getEntity(i));
         }
     }
 
     /** 
      * Attach the view's entity to the HUD entity.
      */
     private void attachToHud () {
         if (attachedToHud) return;
         // TODO: currently we don't have an entity from the HUD, so for now just add it to the world
         ClientContextJME.getWorldManager().addEntity(entity);
         attachedToHud = true;
     }
 
     /** 
      * Detach the view's entity to the HUD entity.
      */
     private void detachFromHud () {
         if (!attachedToHud) return;
         // TODO: currently we don't have an entity from the HUD, so for now just add it to the world
         ClientContextJME.getWorldManager().removeEntity(entity);
         attachedToHud = false;
     }
 
     /**
      * An entity which we can move in and out of the HUD.
      */
     private class HudableEntity extends Entity implements RenderUpdater {
 
         public HudableEntity (String name) {
             super(name);
         }
 
         public void update(Object o) {
 
             // Position and size the view appropriately
             ((ViewWorldDefault)o).update(CHANGED_TRANSFORM | CHANGED_SIZE);
 
             if (onHud) {
                 geometryObj.setZOrder(hudZOrder);
                 baseNode.setCullHint(Spatial.CullHint.Never);
             } else {
                 baseNode.setCullHint(Spatial.CullHint.Inherit);
             }
         }
     }
 }
