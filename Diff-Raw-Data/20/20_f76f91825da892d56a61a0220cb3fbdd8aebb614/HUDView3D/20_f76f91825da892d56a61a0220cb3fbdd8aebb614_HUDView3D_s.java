 /*
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
 package org.jdesktop.wonderland.modules.hud.client;
 
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault.App2DCellRendererJME;
 import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
 
 /**
  * A 3D view for HUD component windows
  * @author nsimpson
  */
 public class HUDView3D extends View2DEntity {
 
     private static final Logger logger = Logger.getLogger(HUDView2D.class.getName());
     private View2DDisplayer displayer;
     private Cell cell;
 
     /**
      * Create an instance of HUDView3D with default geometry node.
      * @param displayer the entity in which the view is displayed.
      * @param window the window displayed in this view.
      */
     public HUDView3D(HUDView3DDisplayer displayer, Window2D window, Cell cell) {
         this(displayer, window, cell, null);
     }
 
     /**
      * Create an instance of HUDView3D with a specified geometry node.
      * @param window The window displayed in this view.
      * @param geometryNode The geometry node on which to display the view.
      */
     public HUDView3D(HUDView3DDisplayer displayer, Window2D window, Cell cell, GeometryNode geometryNode) {
         super(window, geometryNode);
         this.displayer = displayer;
         this.cell = cell;
 
         changeMask = CHANGED_ALL;
         update();
     }
 
     /**
      * {@inheritDoc}
      */
     public View2DDisplayer getDisplayer() {
         return displayer;
     }
 
     /**
      * {@inheritDoc}
      */
     protected Entity getParentEntity() {
         Entity cellEntity = ((App2DCellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();
         switch (type) {
 
             case UNKNOWN:
             case PRIMARY:
                 // Attach primaries directly to cell entity
                 return cellEntity;

             default:
                 // Attach non-primaries to the entity of their parent, if possible
                 if (parent == null) {
                     logger.warning("Attempt to attach a non-primary view without a parent");
                     logger.warning("cell = " + cell);
                     logger.warning("view = " + this);
                     logger.warning("view type = " + type);
                     // This is the best we can do
                     return cellEntity;
                 } else {
                     return parent.getEntity();
                 }
         }
     }
 
     @Override
     /**
      * From App2DCell.updatePrimaryTransform
      */
     protected void updatePrimaryTransform(CellTransform userDeltaTransform) {
         Vector3f translation = getTranslationUserCurrent();
 
         if (type == Type.PRIMARY && isOrtho()) {
             Vector2f locOrtho = getLocationOrtho();
             translation.addLocal(new Vector3f(locOrtho.x, locOrtho.y, 0f));
         }
         sgChangeTransformUserSet(viewNode, new CellTransform(null, translation, null));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean hasFrame() {
         return false;
     }
 
     /**
      * {@inheritDoc}
 
      */
     @Override
     protected void attachFrame() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void detachFrame() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void frameUpdateTitle() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void frameUpdate() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public synchronized void cleanup() {
         super.cleanup();
         displayer = null;
     }
 
     @Override
     public String toString() {
         String string = "view: " + getName() +
                 ", size: " + getSizeApp() +
                 ", ortho: " + isOrtho();
 
         if (isOrtho()) {
             string += ", ortho location: " + this.getLocationOrtho() +
                     ", translation user ortho: " + this.getTranslationUserOrtho();
         }
         return string;
     }
 }
