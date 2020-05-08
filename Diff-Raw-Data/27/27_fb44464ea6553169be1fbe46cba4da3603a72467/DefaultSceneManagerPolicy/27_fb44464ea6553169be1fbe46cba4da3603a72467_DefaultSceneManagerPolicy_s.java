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
 package org.jdesktop.wonderland.client.scenemanager;
 
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
 
 /**
  * Implements a simple selection policy based upon the JME input mechanism.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class DefaultSceneManagerPolicy implements SceneManagerPolicy {
 
     public boolean isClearedSelection(Event event) {
         // If the event carries no Entity (happens on the background), or if
         // the Entity that is clicked is not-selectable (NOT YET IMPLEMENTED)
         // then clear the existing selection.
         if (event instanceof MouseButtonEvent3D) {
             MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
             return mbe.isPressed() == true && mbe.getEntity() == null &&
                     mbe.getButton() == ButtonId.BUTTON1;
         }
         return false;
     }
 
     public boolean isSingleSelection(Event event) {
         // If the event carries an Entity that is selectable (NOT YET IMPLEMENTED)
         // and it is a left-mouse button press with no shift key
         if (event instanceof MouseButtonEvent3D) {
             MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
             return mbe.isPressed() == true && mbe.getEntity() != null &&
                     mbe.getButton() == ButtonId.BUTTON1 &&
                     mbe.getAwtEvent().isShiftDown() == false;
         }
         return false;
     }
 
     public boolean isMultiSelection(Event event) {
         // If the event carries an Entity that is selectable (NOT YET IMPLEMENTED)
         // and it is a left-mouse button press with a shift key
         if (event instanceof MouseButtonEvent3D) {
             MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
             return mbe.isPressed() == true && mbe.getEntity() != null &&
                     mbe.getButton() == ButtonId.BUTTON1 &&
                     mbe.getAwtEvent().isShiftDown() == true;
         }
         return false;        
     }
 
     public boolean isActivation(Event event) {
         // If the event carries an Entity that can be activated (N.Y.I) and
         // is a double-click with the left-mouse button
         if (event instanceof MouseButtonEvent3D) {
             MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
             return mbe.isClicked() == true && mbe.getEntity() != null &&
                     mbe.getButton() == ButtonId.BUTTON1 &&
                     mbe.getClickCount() == 2;
         }
         return false;
     }
 
     public boolean isHoverInterrupt(Event event) {
         // Any mouse event will interrupt a hover
         if (event instanceof MouseEvent3D) {
             return true;
         }
         return false;        
     }
 
     public long getHoverDelay() {
         return 2000;
     }
     
     public boolean isContext(Event event) {
         // If the event is a press of the right-mouse button and there is an
        // Entity.
         if (event instanceof MouseButtonEvent3D) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            return mbe.isPressed() == true && mbe.getButton() == ButtonId.BUTTON3 &&
                    mbe.getAwtEvent().isShiftDown() == false;
         }
         return false;
     }
 
     public boolean isEnter(Event event) {
         // If the event carries an Entity and is a mouse enter event and the
         // Entity can be selected (N.Y.I)
         if (event instanceof MouseEnterExitEvent3D) {
             MouseEnterExitEvent3D mee = (MouseEnterExitEvent3D)event;
             return mee.isEnter() == true;
         }
         return false;
     }
 
     public boolean isExit(Event event) {
         // If the event carries an Entity and is a mouse exit event and the
         // Entity can be selected (N.Y.I)
         if (event instanceof MouseEnterExitEvent3D) {
             MouseEnterExitEvent3D mee = (MouseEnterExitEvent3D)event;
             return mee.isEnter() == false;
         }
         return false;  
     }
 }
