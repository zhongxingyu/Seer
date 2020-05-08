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
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 
 /**
  * A layout manager which lays out HUD components according to compass point
  * positions.
  * @author nsimpson
  */
 public class HUDCompassLayoutManager extends HUDAbsoluteLayoutManager {
 
     private static final Logger logger = Logger.getLogger(HUDCompassLayoutManager.class.getName());
 
     public HUDCompassLayoutManager(int hudWidth, int hudHeight) {
         super(hudWidth, hudHeight);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Vector2f getLocation(HUDComponent component) {
         Vector2f location = new Vector2f();
 
         if (component == null) {
             return location;
         }
 
         HUDView2D view2d = (HUDView2D) hudViewMap.get(component);
 
         if (view2d == null) {
             return location;
         }
 
         // get HUD component's view width
         float compWidth = view2d.getDisplayerLocalWidth();
         float compHeight = view2d.getDisplayerLocalHeight();
 
         // get the center of the HUD
         float hudCenterX = hudWidth / 2f;
         float hudCenterY = hudHeight / 2f;
 
         if (component.getPreferredLocation() != Layout.NONE) {
             switch (component.getPreferredLocation()) {
                 case NORTH:
                    location.set(hudCenterX, hudHeight - 20 - compHeight / 2f);
                     break;
                 case SOUTH:
                     location.set(hudCenterX, 20);
                     break;
                 case WEST:
                     location.set(20 + compWidth / 2f, hudCenterY - compHeight / 2f);
                     break;
                 case EAST:
                     location.set(hudWidth - 20 - compWidth / 2f, hudCenterY - compHeight / 2f);
                     break;
                 case CENTER:
                     location.set(hudCenterX, hudCenterY);
                     break;
                 case NORTHWEST:
                     location.set(20 + compWidth / 2f, hudHeight - 20 - compHeight / 2f);
                     break;
                 case NORTHEAST:
                     location.set(hudWidth - 20 - compWidth / 2f, hudHeight - 20 - compHeight / 2f);
                     break;
                 case SOUTHWEST:
                     location.set(20 + compWidth / 2f, 20 + compHeight / 2f);
                     break;
                 case SOUTHEAST:
                     location.set(hudWidth - 20 - compWidth / 2, 20 + compHeight / 2f);
                     break;
                 default:
                     logger.warning("unhandled layout type: " + component.getPreferredLocation());
                     break;
             }
         } else {
             location.set(component.getX() + compWidth / 2f, component.getY() + compHeight / 2f);
         }
         return location;
     }
 }
