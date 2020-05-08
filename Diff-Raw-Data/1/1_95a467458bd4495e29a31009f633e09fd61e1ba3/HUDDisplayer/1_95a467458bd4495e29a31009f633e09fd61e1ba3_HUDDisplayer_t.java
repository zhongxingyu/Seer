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
 package org.jdesktop.wonderland.modules.appbase.client;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDEvent;
 import org.jdesktop.wonderland.client.hud.HUDEventListener;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
 import org.jdesktop.wonderland.modules.appbase.client.view.WindowSwingHeader;
 
 @ExperimentalAPI
 public class HUDDisplayer implements View2DDisplayer {
 
     /** The app displayed by this displayer. */
     private App2D app;
     /** The HUD. */
     private HUD mainHUD;
     /** HUD components for windows shown in the HUD. */
     private LinkedList<HUDComponent> hudComponents;
 
     public HUDDisplayer (App2D app) {
         this.app = app;
         mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
         hudComponents = new LinkedList<HUDComponent>();
     }
 
     public void cleanup () {
         if (hudComponents != null) {
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             for (HUDComponent component : hudComponents) {
                 component.setVisible(false);
                 mainHUD.removeComponent(component);
             }
             hudComponents.clear();
             hudComponents = null;
         }
         mainHUD = null;
         app = null;
     }
 
     public View2D createView (Window2D window) {
 
         // Don't ever show frame headers in the HUD
         if (window instanceof WindowSwingHeader) return null;
 
         HUDComponent component = mainHUD.createComponent(window);
        component.setName(app.getName());
         component.setPreferredLocation(Layout.CENTER);
         hudComponents.add(component);
 
         component.addEventListener(new HUDEventListener() {
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEvent.HUDEventType.CLOSED)) {
                     // TODO: currently we take the entire app off the HUD when
                     // any HUD view of any app window is quit
                     app.setShowInHUD(false);
                 }
             }
         });
 
         mainHUD.addComponent(component);
         component.setVisible(true);
 
         // TODO: get the view from the HUD component and return it?
         return null;
     }
 
     public void destroyView (View2D view) {
     }
 
     public void destroyAllViews () {
     }
 
     public Iterator<? extends View2D> getViews () {
         return null;
     }
 
 }
