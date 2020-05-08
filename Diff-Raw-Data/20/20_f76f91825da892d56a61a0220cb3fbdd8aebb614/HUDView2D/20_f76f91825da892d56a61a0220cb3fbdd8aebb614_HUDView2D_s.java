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
 
 import org.jdesktop.mtgame.Entity;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
import com.jme.scene.Node;
 import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.view.GeometryNode;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
 
 /**
  * A 2D view for HUD component windows
  * @author nsimpson
  */
 public class HUDView2D extends View2DEntity {
 
     private static final Logger logger = Logger.getLogger(HUDView2D.class.getName());
     private View2DDisplayer displayer;
 
     /**
      * Create an instance of HUDView2D with default geometry node.
      * @param displayer the entity in which the view is displayed.
      * @param window the window displayed in this view.
      */
     public HUDView2D(View2DDisplayer displayer, Window2D window) {
         this(displayer, window, null);
     }
 
     /**
      * Create an instance of HUDView2D with a specified geometry node.
      * @param window The window displayed in this view.
      * @param geometryNode The geometry node on which to display the view.
      */
     public HUDView2D(View2DDisplayer displayer, Window2D window, GeometryNode geometryNode) {
         super(window, geometryNode);
         this.displayer = displayer;

        // Create entity and node
        entity = new Entity("Entity for " + name);
        viewNode = new Node("Node for " + name);
        RenderComponent rc =
                ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(viewNode);
        entity.addComponent(RenderComponent.class, rc);
        entityMakePickable(entity);

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
         logger.warning("HUDView2D.getParentEntity returning null");
         return null;
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
