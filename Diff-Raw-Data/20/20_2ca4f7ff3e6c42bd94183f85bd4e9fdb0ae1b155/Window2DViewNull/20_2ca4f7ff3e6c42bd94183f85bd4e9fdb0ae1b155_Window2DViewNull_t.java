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
 package org.jdesktop.wonderland.modules.appbase.client.gui.guinull;
 
 import com.jme.math.Vector3f;
 import java.awt.Point;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.wonderland.client.input.EventListener;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.AppCell;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
 import org.jdesktop.wonderland.modules.appbase.client.Window2DViewWorld;
 
 /**
  * A view onto a window which exists in the 3D world.
  *
  * @author deronj
  */ 
 
 @ExperimentalAPI
 public class Window2DViewNull extends Window2DView implements Window2DViewWorld {
 
     /**
      * Create a new instance of Window2DViewNull.
      *
      * @param window The window displayed by the view.
      */
     public Window2DViewNull (Window2D window) {
 	super(window, "Null");
     }
 
     /**
      * Clean up resources.
      */
     public void cleanup () {}
 
     public float getPopupDepthOffset () { return 0f; }
 
     public void setTranslation (Vector3f translation) {}
 
     public Vector3f getTranslation () { return new Vector3f(); }
 
     /**
      * Sets the visibility of the view (independent of the window).
      * Don't forget to call update(CHANGED_VISIBILITY) afterward.
      *
      * @param visible True if the window should be made visible.
      */
     public void setVisible (boolean visible) {}
 
     /**
      * Returns the visibility of the view (independent of the window)
      */
     public boolean getVisible () {
 	return false;
     }
 
     /**
      * Returns whether the window is actually visible, that is,
      * the view visibility combined with the window visibility.
      */
     boolean getActuallyVisible () {
 	return false;
     }
 
     /**
      * Specify whether the view is top-level. Don't forget to also
      * call update(CHANGED_TOP_LEVEL) afterward.  
      *
      * @param topLevel True if the window should be a topLevel window.
      */
     public void setTopLevel (boolean topLevel) {}
 	
     /**
      * Returns whether the window of the view is top-level.
      */
     public boolean getTopLevel () {
 	return true;
     }
 
     /**
      * Sets the position of the view. Don't forget to also call
      * update(CHANGED_POSITION) afterward.
      *
      * @param position The new position of the window (in cell local coordinates).
      */
     public void setPosition (Vector3f position) {}
 
     /**
      * Returns the position of the window.
      */
     public Vector3f getPosition () { 
 	return new Vector3f(0f, 0f, 0f);
     }
 
     /**
      * Sets the dimensions of the position of the view. Don't forget to 
      * call update(CHANGED_SIZE) afterward.
      *
      * @param width The new width of the window.
      * @param height The new height of the window.
      */
     public void setSize (float width, float height) {}
 
     /**
      * Returns the width of the window.
      */
     public float getWidth () { 
 	return 0f; 
     }
 
     /**
      * Returns the height of the window.
      */
     public float getHeight () { 
 	return 0f; 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void update (int changeMask) {}
 
     /** Update the view's geometry (for a size change) */
     protected void updateGeometrySize () throws InstantiationException {}
 
     /** The window's texture may have changed */
     protected void updateTexture () {}
 
     /** Update the view's transform */
     protected void updateTransform () {}
 
     /** Update the view's stacking relationships */
     protected void updateStack () {}
 
     /** Update the view's visibility */
     void updateVisibility() {}
 
     /** Get the cell of the view */
     protected AppCell getCell () {
 	return window.getCell();
     }
   
     /**
      * {@inheritDoc}
      */
     public void deliverEvent (Window2D window, MouseEvent3D me3d) {}
 
     /**
      * {@inheritDoc}
      */
    public Point calcPositionInPixelCoordinates (Vector3f point, boolean clamp) {
 	return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addEventListener (EventListener listener) {}
 
     /**
      * {@inheritDoc}
      */
     public void removeEventListener (EventListener listener) {}
 
     /**
      * {@inheritDoc}
      */
     public boolean hasEventListener (EventListener listener) {
 	return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addEntityComponent (Class clazz, EntityComponent comp) {}
 
     /**
      * {@inheritDoc}
      */
     public void removeEntityComponent (Class clazz) {}
 
     /**
      * {@inheritDoc}
      */
     public EntityComponent getEntityComponent (Class clazz) {
 	return null;
     }
 }
 
