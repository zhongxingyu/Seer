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
 package org.jdesktop.wonderland.client.jme;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.CameraComponent;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassFocusListener;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.input.InputManager3D;
 import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
 import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
 
 /**
  *
  */
 public class JmeClientMain {
     
     // properties
     private Properties props;
     
     // standard properties
     private static final String SERVER_NAME_PROP = "sgs.server";
     private static final String SERVER_PORT_PROP = "sgs.port";
     private static final String USER_NAME_PROP   = "cellboundsviewer.username";
     
     // default values
     private static final String SERVER_NAME_DEFAULT = "localhost";
     private static final String SERVER_PORT_DEFAULT = "1139";
     private static final String USER_NAME_DEFAULT   = "jmetest";
    
     /**
      * The desired frame rate
      */
    private int desiredFrameRate = 1;
     
     /**
      * The width and height of our 3D window
      */
     private int width = 800;
     private int height = 600;
     
     public JmeClientMain(String[] args) {
         props = loadProperties("run-client.properties");
    
         String serverName = props.getProperty(SERVER_NAME_PROP,
                                               SERVER_NAME_DEFAULT);
         String serverPort = props.getProperty(SERVER_PORT_PROP,
                                               SERVER_PORT_DEFAULT);
         String userName   = props.getProperty(USER_NAME_PROP,
                                               USER_NAME_DEFAULT);
         
         
         processArgs(args);
 
         WorldManager worldManager = ClientContextJME.getWorldManager();
 
         worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);
         
        createUI(worldManager);

        // Dont start the client manager until JME has been initialized, many JME components
        // expect the renderer to be ready during init.
        ClientManager clientManager = new ClientManager(serverName, Integer.parseInt(serverPort), userName);
        
        // Low level Federation testing
//        ClientManager clientManager2 = new ClientManager(serverName, Integer.parseInt(serverPort), userName+"2");
        
     }
 
     /**
      * @deprecated 
      * @return
      */
     static WorldManager getWorldManager() {
         return ClientContextJME.getWorldManager();
     }
     
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         JmeClientMain worldTest = new JmeClientMain(args);
         
     }
     
     /**
      * Process any command line args
      */
     private void processArgs(String[] args) {
         for (int i=0; i<args.length;i++) {
             if (args[i].equals("-fps")) {
                 desiredFrameRate = Integer.parseInt(args[i+1]);
                 System.out.println("DesiredFrameRate: " + desiredFrameRate);
                 i++;
             }
         }
     }
     
     /**
      * Create all of the Swing windows - and the 3D window
      */
     private void createUI(WorldManager wm) {             
         MainFrame frame = new MainFrame(wm, width, height);
         // center the frame
         frame.setLocationRelativeTo(null);
 
         // show frame
         frame.setVisible(true);
 
 	// Initialize the input manager.
 	// Note: this also creates the view manager.
 	// TODO: low bug: we would like to initialize the input manager BEFORE frame.setVisible.
 	// But if we create the camera before frame.setVisible the client window never appears.
 	CameraComponent cameraComp = ViewManager.getViewManager().getCameraComponent();
 	InputManager inputManager = ClientContext.getInputManager();
 	inputManager.initialize(frame.getCanvas(), cameraComp);
 
 	// Default Policy: Enable global key and mouse focus everywhere 
 	// Note: the app base will impose its own (different) policy later
 	inputManager.addKeyMouseFocus(inputManager.getGlobalFocusEntity());
 
 	//TODO: temporary: example global key event listener for Paul */
 	InputManager3D.getInputManager().addGlobalEventListener(
 	    new EventClassFocusListener () {
 		private final Logger logger = Logger.getLogger("My Logger");
 		public Class[] eventClassesToConsume () {
 		    return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
 		}
 		public void commitEvent (Event event) {
 		    // NOTE: to test, change the two logger.fine calls below to logger.warning
 		    if (event instanceof KeyEvent3D) {
 			if (((KeyEvent3D)event).isPressed()) {
 			    logger.fine("Global listener: received key event, event = " + event );
 			}
 		    } else {
 			logger.fine("Global listener: received mouse event, event = " + event);
 		    }
 		}
     	    });
     }
     
     private static Properties loadProperties(String fileName) {
         // start with the system properties
         Properties props = new Properties(System.getProperties());
     
         // load the given file
         if (fileName != null) {
             try {
                 props.load(new FileInputStream(fileName));
             } catch (IOException ioe) {
                 Logger.getLogger(JmeClientMain.class.getName()).log(Level.WARNING, "Error reading properties from " +
                            fileName, ioe);
             }
         }
         
         return props;
     }
     
 //    class NodeMoveListener implements GeometricUpdateListener {
 //
 //        private ClientManager clientManager;
 //        
 //        public NodeMoveListener(ClientManager clientManager) {
 //            this.clientManager = clientManager;
 //        }
 //        
 //        public void geometricDataChanged(Spatial arg0) {
 //            clientManager.nodeMoved(arg0);
 //        }
 //        
 //    }
 }
