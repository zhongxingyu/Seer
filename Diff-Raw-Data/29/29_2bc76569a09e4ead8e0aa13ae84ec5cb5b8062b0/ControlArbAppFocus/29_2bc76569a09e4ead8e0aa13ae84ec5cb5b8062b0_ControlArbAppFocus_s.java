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
 
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.input.InputManager3D;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * A control arb which maintains app input focus. When an control is taken for an app,
  * its focus entity is focussed for key and mouse events. When control is released
  * for an app, its focus entity is defocussed for key and mouse events. Finally, whenever
  * one or more app is controlled the global focus entity is defocussed. 
  *
  * @author deronj
  */
 @ExperimentalAPI
 public abstract class ControlArbAppFocus extends ControlArb {
 
     /** The input manager. */
     private InputManager inputManager;
 
     /** The number of apps which have control. */
     private static int numControlledApps;
 
     /**
      * Create an instance of ControlArbAppFocus.
      */
     public ControlArbAppFocus () {
         inputManager = InputManager3D.getInputManager();
     }
 
     /** {@inheritDoc} */
     @Override
     public void cleanup() {
         super.cleanup();
         numControlledApps = 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void takeControl() {
         if (hasControl()) return;
         super.takeControl();
         if (!hasControl()) return;
 
         // Assign focus to the app
         inputManager.addKeyMouseFocus(new Entity[] { app.getFocusEntity() });
 
         numControlledApps++;
         if (numControlledApps == 1) {
             // At least one app has keyboard/mouse control. Disable global (world) listeners.
             inputManager.removeKeyMouseFocus(inputManager.getGlobalFocusEntity());            
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void releaseControl() {
         if (!hasControl()) return;
         super.releaseControl();
         if (hasControl()) return;
 
         // Remove focus from the app
         inputManager.removeKeyMouseFocus(new Entity[] { app.getFocusEntity() });
 
         numControlledApps--;
         if (numControlledApps <= 0) {
             // No more apps have control. Reenable global (world) listeners.
             inputManager.addKeyMouseFocus(inputManager.getGlobalFocusEntity());            
         }
     }
 }
