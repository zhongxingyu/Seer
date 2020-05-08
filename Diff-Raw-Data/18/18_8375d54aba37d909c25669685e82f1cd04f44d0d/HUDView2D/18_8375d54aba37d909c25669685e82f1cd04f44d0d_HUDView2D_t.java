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
 
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderComponent;
 import org.jdesktop.wonderland.client.hud.HUDView;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
 
 /**
  * A 2D view for HUD component windows.
  *
  * @author nsimpson
  */
 public class HUDView2D extends View2DEntity implements HUDView {
 
     private static final Logger logger = Logger.getLogger(HUDView2D.class.getName());
     private View2DDisplayer displayer;
 
     /**
      * Create an instance of HUDView2D with default geometry node.
      * @param displayer the entity in which the view is displayed.
      * @param window the window displayed in this view.
      */
     public HUDView2D(View2DDisplayer displayer, Window2D window) {
         this(displayer, window, null);
        name = "HUDView2D for " + window.getName();
     }
 
     /**
      * Create an instance of HUDView2D with a specified geometry node.
      * @param displayer the entity in which the view is displayed.
      * @param window The window displayed in this view.
      * @param geometryNode The geometry node on which to display the view.
      */
     public HUDView2D(View2DDisplayer displayer, Window2D window, GeometryNode geometryNode) {
         super(window, geometryNode);
         this.displayer = displayer;
         changeMask = CHANGED_ALL;
         update();
        name = "HUDView2D for " + window.getName();
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
        View2DEntity parentView = (View2DEntity) getParent();
        if (parentView == null) {
            return null;
        }
        return parentView.getEntity();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean hasFrame() {
         return false;
     }
 
     public HUDView2D getFrameView() {
         return null;
     }
 
     public Window2D getFrameWindow() {
         return null;
     }
 
     public void attachView(HUDView2D view) {
         logger.fine("attach view: " + view + "to: " + this);
         Entity e = view.getEntity();
         RenderComponent rcFrame = (RenderComponent) e.getComponent(RenderComponent.class);
         rcFrame.setAttachPoint(this.getGeometryNode());
     }
 
     public void detachView(HUDView2D view) {
         logger.fine("detach view: " + view + "from: " + this);
         Entity viewEntity = view.getEntity();
         if (viewEntity == null) {
             return;
         }
         entity.removeEntity(viewEntity);
         RenderComponent rcFrame = (RenderComponent) viewEntity.getComponent(RenderComponent.class);
         if (rcFrame != null) {
             rcFrame.setAttachPoint(null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void reattachFrame() {
         logger.fine("reattach frame");
         detachFrame();
         attachFrame();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void attachFrame() {
         logger.fine("attach frame");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void detachFrame() {
         logger.fine("detach frame");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void frameUpdateTitle() {
         logger.fine("update frame title");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void frameUpdate() {
         logger.fine("update frame");
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
             string += ", ortho location: " + this.getLocationOrtho();
         }
         return string;
     }
 }
