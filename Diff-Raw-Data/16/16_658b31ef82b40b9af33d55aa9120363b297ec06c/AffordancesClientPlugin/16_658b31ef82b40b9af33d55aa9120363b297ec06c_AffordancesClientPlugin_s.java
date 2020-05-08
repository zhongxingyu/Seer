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
 
 package org.jdesktop.wonderland.modules.affordances.client;
 
 
 import java.awt.event.KeyEvent;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.ClientPlugin;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenu;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuEvent;
 import org.jdesktop.wonderland.client.contextmenu.ContextMenuListener;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.scenemanager.SceneManager;
 import org.jdesktop.wonderland.modules.affordances.client.jme.TranslateAffordance;
 
 /**
  * Client-size plugin for the cell manipulator affordances.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class AffordancesClientPlugin implements ClientPlugin {
     
     public void initialize(ServerSessionManager loginInfo) {
         ContextMenu contextMenu = ContextMenu.getContextMenu();
         contextMenu.addContextMenuItem("Move", new MoveContextListener());
     }
 
     /**
      * Inner class to handle when the "Move" item has been selected from the
      * context menu
      */
     class MoveContextListener implements ContextMenuListener {
 
         public void entityContextPerformed(ContextMenuEvent event) {
             // Fetch the Entity associated with the event. If there is none,
             // then ignore the event quietly.
            Entity entity = event.getEntityList().get(0);
            if (entity == null) {
                 return;
             }
 
             // Fetch the cell associated with the entity. If null, then ignore
             // quietly.
             Cell cell = SceneManager.getCellForEntity(entity);
             if (cell == null) {
                 return;
             }
 
            // Otherwise, attach a translate affordance to the cell
             TranslateAffordance affordance = TranslateAffordance.addToCell(cell);

            // Register a listener for the Esc key to dismiss the affordance
            InputManager.inputManager().addGlobalEventListener(new KeyListener(affordance));
         }
     }
 
     /**
      * Listens for key presses to display the affordance
      */
     class KeyListener extends EventClassListener {
         private TranslateAffordance affordance;
 
         public KeyListener(TranslateAffordance affordance) {
             this.affordance = affordance;
         }
 
         @Override
         public void commitEvent(Event event) {
             if (event instanceof KeyEvent3D) {
                 KeyEvent3D ke = (KeyEvent3D)event;
                 if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                     affordance.remove();
                     InputManager.inputManager().removeGlobalEventListener(this);
                 }
             }
         }
 
         @Override
         public Class[] eventClassesToConsume() {
             return new Class[] { KeyEvent3D.class };
         }
     }
 }
