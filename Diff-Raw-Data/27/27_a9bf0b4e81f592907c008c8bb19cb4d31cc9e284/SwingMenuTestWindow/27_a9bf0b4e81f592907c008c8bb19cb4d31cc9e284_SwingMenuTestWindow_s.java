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
 package org.jdesktop.wonderland.modules.swingmenutest.client;
 
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.modules.appbase.client.App;
 import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
 import com.jme.math.Vector2f;
 import javax.swing.JPanel;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  *
  * The window for the Swing menu test.
  *
  * @author deronj
  */
 
 @ExperimentalAPI
 public class SwingMenuTestWindow extends WindowSwing  {
 
     /** The logger used by this class. */
     private static final Logger logger = Logger.getLogger(SwingMenuTestWindow.class.getName());
 
     /**
      * Create a new instance of SwingMenuTestWindow.
      *
      * @param app The app which owns the window.
      * @param width The width of the window (in pixels).
      * @param height The height of the window (in pixels).
      * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
      * @param pixelScale The size of the window pixels.
      */
     public SwingMenuTestWindow (final App app, int width, int height, boolean topLevel, Vector2f pixelScale)
         throws InstantiationException
     {
 	super(app, width, height, topLevel, pixelScale);
 	setSize(width, height);
 	initializeSurface();
 
 	setTitle("Swing Menu Test");
 	
 	MenuPanel menuPanel = new MenuPanel();
 	// Note: this seems to only be required for the swing set, but do it here for safety
 	// TODO: test without
        	JmeClientMain.getFrame().getCanvas3DPanel().add(menuPanel);
 	setComponent(menuPanel);
     }
 }
