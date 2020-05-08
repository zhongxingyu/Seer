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
 package org.jdesktop.wonderland.client.jme;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.sun.scenario.animation.Clip;
 import com.sun.scenario.animation.Interpolators;
 import com.sun.scenario.animation.TimingTarget;
 import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
 import org.jdesktop.wonderland.client.jme.login.JmeLoginUI;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.CameraComponent;
 import org.jdesktop.mtgame.CollisionManager;
 import org.jdesktop.mtgame.CollisionSystem;
 import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
 import org.jdesktop.mtgame.JBulletPhysicsSystem;
 import org.jdesktop.mtgame.JMECollisionSystem;
 import org.jdesktop.mtgame.PhysicsManager;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.view.AvatarCell;
 import org.jdesktop.wonderland.client.cell.view.ViewCell;
 import org.jdesktop.wonderland.common.ThreadManager;
 import org.jdesktop.wonderland.client.comms.LoginFailureException;
 import org.jdesktop.wonderland.client.comms.SessionStatusListener;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.MainFrame.ServerURLListener;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.login.LoginManager;
 /* For Testing FocusEvent3D
 import org.jdesktop.wonderland.client.jme.input.FocusEvent3D;
 import org.jdesktop.wonderland.client.jme.input.InputManager3D;
  */
 
 /**
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 public class JmeClientMain {
 
     public static final String SERVER_URL_PROP = "sgs.server";
     private static final Logger LOGGER =
             Logger.getLogger(JmeClientMain.class.getName());
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/client/jme/resources/bundle");
     /** The frame of the Wonderland client window. */
     private static MainFrame frame;
     // standard properties
     private static final String PROPS_URL_PROP = "run.properties.file";
     private static final String CONFIG_DIR_PROP =
             "wonderland.client.config.dir";
     private static final String DESIRED_FPS_PROP = "wonderland.client.fps";
     private static final String WINDOW_SIZE_PROP =
             "wonderland.client.windowSize";
     // default values
     private static final String SERVER_URL_DEFAULT = "http://localhost:8080";
     private static final String DESIRED_FPS_DEFAULT = "30";
     private static final String WINDOW_SIZE_DEFAULT = "800x600";
     private int desiredFrameRate = Integer.parseInt(DESIRED_FPS_DEFAULT);
     /**
      * The width and height of our 3D window
      */
     private int width = 800;
     private int height = 600;
     // the current Wonderland login and session
     private JmeLoginUI login;
     private JmeClientSession curSession;
     // keep tack of whether we are currently logging out
     private boolean loggingOut;
 
     /**
      * creates a new JmeClientMain
      * @param args the command line arguments
      */
     public JmeClientMain(String[] args) {
         checkVmVersion();
 
         // process command line arguments
         processArgs(args);
 
         // load properties in a properties file
         URL propsURL = getPropsURL();
         loadProperties(propsURL);
 
         // set up the context
         ClientContextJME.setClientMain(this);
 
         String windowSize = System.getProperty(
                 WINDOW_SIZE_PROP, WINDOW_SIZE_DEFAULT);
         try {
             if (windowSize.equalsIgnoreCase("fullscreen")) {
                 GraphicsEnvironment ge =
                         GraphicsEnvironment.getLocalGraphicsEnvironment();
                 GraphicsDevice[] gs = ge.getScreenDevices();
                 if (gs.length > 1) {
                     LOGGER.warning("Fullscreen using size of first screen");
                 }
                 GraphicsConfiguration gc = gs[0].getDefaultConfiguration();
                 Rectangle size = gc.getBounds();
                 width = size.width;
                 height = size.height; // -50 hack for current swing decorations
             } else {
                 String sizeWidth =
                         windowSize.substring(0, windowSize.indexOf('x'));
                 String sizeHeight =
                         windowSize.substring(windowSize.indexOf('x') + 1);
                 width = Integer.parseInt(sizeWidth);
                 height = Integer.parseInt(sizeHeight);
             }
         } catch (Exception e) {
             LOGGER.warning(WINDOW_SIZE_PROP +
                     " error, should be of the form 640x480 (or fullscreen), " +
                     "instead of the current " + windowSize);
         }
 
         // make sure the server URL is set
         String serverURL = System.getProperty(SERVER_URL_PROP);
         if (serverURL == null) {
             serverURL = SERVER_URL_DEFAULT;
             System.setProperty(SERVER_URL_PROP, serverURL);
         }
 
         // HUGE HACK ! Force scenario to initialize so menus work correctly
         // (work around for scenario bug)
         Clip clip2 = Clip.create(1000, new TimingTarget() {
 
             public void timingEvent(float arg0, long arg1) {
             }
 
             public void begin() {
             }
 
             public void end() {
             }
 
             public void pause() {
             }
 
             public void resume() {
             }
         });
         clip2.setInterpolator(Interpolators.getEasingInstance(0.4f, 0.4f));
         clip2.start();
         // End HUGE HACK.
 
 
         WorldManager worldManager = ClientContextJME.getWorldManager();
 
         String requestedFPS = System.getProperty(
                 DESIRED_FPS_PROP, DESIRED_FPS_DEFAULT);
         if (requestedFPS != null) {
             try {
                 desiredFrameRate = Integer.parseInt(requestedFPS);
             } catch (NumberFormatException e) {
                 // No action required, the default has already been set.
                 LOGGER.warning(DESIRED_FPS_PROP +
                         " property format error for '" + requestedFPS +
                         "', using default");
             }
         }
         worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);
 
         createUI(worldManager);
 
         // Register our loginUI for login requests
         login = new JmeLoginUI(frame);
         LoginManager.setLoginUI(login);
 
         // add a listener that will be notified when the user selects a new
         // server
         frame.addServerURLListener(new ServerURLListener() {
 
             public void serverURLChanged(final String serverURL) {
                 // run in a new thread so we don't block the AWT thread
                 new Thread(ThreadManager.getThreadGroup(), new Runnable() {
 
                     public void run() {
                         try {
                             loadServer(serverURL);
                         } catch (IOException ioe) {
                             LOGGER.log(Level.WARNING, "Error connecting to " +
                                     serverURL, ioe);
                         }
                     }
                 }).start();
             }
 
             public void logout() {
                 new Thread(ThreadManager.getThreadGroup(), new Runnable() {
 
                     public void run() {
                         JmeClientMain.this.logout();
                     }
                 }).start();
             }
         });
 
 //        JMenuItem physicsMI = new JCheckBoxMenuItem(
 //                BUNDLE.getString("Physics Enabled"));
 //        physicsMI.setEnabled(false);
 //        physicsMI.setSelected(false);
 //        physicsMI.addActionListener(new ActionListener() {
 //
 //            public void actionPerformed(ActionEvent e) {
 //                PhysicsSystem phySystem = ClientContextJME.getPhysicsSystem(
 //                        curSession.getSessionManager(), "Default");
 //                if (phySystem instanceof JBulletPhysicsSystem) {
 //                    ((JBulletPhysicsSystem) phySystem).setStarted(
 //                            ((JCheckBoxMenuItem) e.getSource()).isSelected());
 //                } else {
 //                    LOGGER.severe("Unsupported physics system " + phySystem);
 //                }
 //            }
 //        });
 //        frame.addToEditMenu(physicsMI, 3);
 
         // connect to the default server
         try {
             loadServer(serverURL);
         } catch (IOException ioe) {
             LOGGER.log(Level.WARNING, "Error connecting to default server " +
                     serverURL, ioe);
         }
 
     }
 
     /**
      * Move the client to the given location
      * @param serverURL the url of the server to go to, or null to stay
      * on the current server
      * @param translation the translation
      * @param look the direction to look in, or null to look in the default
      * direction
      * @throws IOException if there is an error going to the new location
      */
     public void gotoLocation(String serverURL, Vector3f translation,
             Quaternion look)
             throws IOException {
         if (serverURL == null) {
             // get the server from the current session
             if (curSession == null) {
                 throw new IllegalStateException("No server");
             }
 
             serverURL = curSession.getSessionManager().getServerURL();
         }
 
         // see if we need to change servers
         if (curSession != null &&
                 serverURL.equals(
                 curSession.getSessionManager().getServerURL())) {
             // no need to change - make a local move request
             ViewCell vc = curSession.getLocalAvatar().getViewCell();
             if (vc instanceof AvatarCell) {
                 ((AvatarCell) vc).triggerGoto(translation, look);
             }
 
         } else {
             loadServer(serverURL, translation, look);
         }
     }
 
     protected void loadServer(String serverURL) throws IOException {
         loadServer(serverURL, null, null);
     }
 
     protected void loadServer(String serverURL, Vector3f translation,
             Quaternion look)
             throws IOException {
         LOGGER.info("[JmeClientMain] loadServer " + serverURL);
 
         logout();
 
         // get the login manager for the given server
         ServerSessionManager lm = LoginManager.getSessionManager(serverURL);
 
         // Register physics and phyiscs collision systems for this session
         WorldManager worldManager = ClientContextJME.getWorldManager();
         CollisionManager collisionManager = worldManager.getCollisionManager();
         CollisionSystem collisionSystem = collisionManager.loadCollisionSystem(
                 JBulletDynamicCollisionSystem.class);
         JBulletDynamicCollisionSystem jBulletCollisionSystem =
                 (JBulletDynamicCollisionSystem) collisionSystem;
         PhysicsManager physicsManager = worldManager.getPhysicsManager();
         JBulletPhysicsSystem jBulletPhysicsSystem =
                 (JBulletPhysicsSystem) physicsManager.loadPhysicsSystem(
                 JBulletPhysicsSystem.class, jBulletCollisionSystem);
         ClientContextJME.addCollisionSystem(
                 lm, "Physics", jBulletCollisionSystem);
         ClientContextJME.addPhysicsSystem(lm, "Physics", jBulletPhysicsSystem);
 
         // Register default collision system for this session
         JMECollisionSystem jmeCollisionSystem =
                 (JMECollisionSystem) collisionManager.loadCollisionSystem(
                 JMECollisionSystem.class);
         ClientContextJME.addCollisionSystem(lm, "Default", jmeCollisionSystem);
 
         // set the initial position, which will bne sent with the initial
         // connection properties of the cell cache connection
         login.setInitialPosition(translation, look);
 
         // create a new session
         try {
             curSession = lm.createSession(login);
         } catch (LoginFailureException lfe) {
             IOException ioe = new IOException("Error connecting to " +
                     serverURL);
             ioe.initCause(lfe);
             throw ioe;
         }
 
         // make sure we logged in successfully
         if (curSession == null) {
             LOGGER.log(Level.WARNING, "Unable to connect to session");
             return;
         }
 
         frame.connected(true);
 
         // Listen for session disconnected and remove session physics and
         // collision systems
         curSession.addSessionStatusListener(new SessionStatusListener() {
 
             public void sessionStatusChanged(
                     WonderlandSession session, Status status) {
                 if (status == Status.DISCONNECTED) {
                     ServerSessionManager serverSessionManager =
                             session.getSessionManager();
                     ClientContextJME.removeAllPhysicsSystems(
                             serverSessionManager);
                     ClientContextJME.removeAllCollisionSystems(
                             serverSessionManager);
 
                     // update the UI for logout
                     boolean inLogout;
                     synchronized (JmeClientMain.this) {
                         inLogout = loggingOut;
                     }
 
                     if (!inLogout) {
                         // if we didn't initiate the logout through the
                         // logout() method, then this is an unexpected
                         // logout.  Clean up by calling the logout() method,
                         // then attempt to reconnect
                         // reconnect dialog
                         final ServerSessionManager mgr =
                                 curSession.getSessionManager();
 
                         LOGGER.warning("[JmeClientMain] unexpected logout!");
 
                         logout();
 
                         SwingUtilities.invokeLater(new Runnable() {
 
                             public void run() {
                                 ReconnectDialog rf = new ReconnectDialog(
                                         JmeClientMain.this, mgr);
                                 rf.setVisible(true);
                             }
                         });
 
                     } else {
                         synchronized (JmeClientMain.this) {
                             loggingOut = false;
                         }
                     }
                 }
             }
         });
 
         // set the primary login manager and session
         LoginManager.setPrimary(lm);
         lm.setPrimarySession(curSession);
         frame.setServerURL(serverURL);
     }
 
     /**
      * logs out
      */
     protected void logout() {
         LOGGER.warning("[JMEClientMain] log out");
 
         // disconnect from the current session
         if (curSession != null) {
             if (curSession.getStatus() == Status.CONNECTED) {
                 synchronized (this) {
                     loggingOut = true;
                 }
             }
 
             curSession.getCellCache().unloadAll();
 
             curSession.logout();
             curSession = null;
             frame.connected(false);
 
             // notify listeners that there is no longer a primary server
             LoginManager.setPrimary(null);
         }
     }
 
     /**
      * returns the properties URL
      * @return the properties URL
      */
     protected URL getPropsURL() {
         String propURLStr = System.getProperty(PROPS_URL_PROP);
         try {
             URL propsURL;
 
             if (propURLStr == null) {
                 String configDir = System.getProperty(CONFIG_DIR_PROP);
                 if (configDir == null) {
                     File userDir = new File(System.getProperty("user.dir"));
                     configDir = userDir.toURI().toURL().toString();
                 }
 
                 // use the default
                 URL configDirURL = new URL(configDir);
                 propsURL = new URL(configDirURL, "run-client.properties");
             } else {
                 propsURL = new URL(propURLStr);
             }
 
             return propsURL;
         } catch (IOException ioe) {
             LOGGER.log(Level.WARNING, "Unable to load properties", ioe);
             return null;
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         if (Webstart.isWebstart()) {
             Webstart.webstartSetup();
         }
 
         JmeClientMain worldTest = new JmeClientMain(args);
 
     }
 
     /**
      * Process any command line args
      */
     private void processArgs(String[] args) {
         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("-fps")) {
                 desiredFrameRate = Integer.parseInt(args[i + 1]);
                 System.out.println("DesiredFrameRate: " + desiredFrameRate);
                 i++;
             }
 
             if (args[i].equals("-p")) {
                 System.setProperty(PROPS_URL_PROP, "file:" + args[i + 1]);
                 i++;
             }
         }
     }
 
     /**
      * Create all of the Swing windows - and the 3D window
      */
     private void createUI(WorldManager wm) {
 
         frame = new MainFrameImpl(wm, width, height);
         // center the frame
         frame.getFrame().setLocationRelativeTo(null);
 
         // show frame
         frame.getFrame().setVisible(true);
 
         JPanel canvas3D = frame.getCanvas3DPanel();
         // Initialize an onscreen view
         ViewManager.initialize(canvas3D.getWidth(), canvas3D.getHeight());
 
         // This call will block until the render buffer is ready, for it to
         // become ready the canvas3D must be visible
         ViewManager viewManager = ViewManager.getViewManager();
         viewManager.attachViewCanvas(canvas3D);
 
         // Initialize the input manager.
         // Note: this also creates the view manager.
         // TODO: low bug: we would like to initialize the input manager BEFORE
         // frame.setVisible. But if we create the camera before frame.setVisible
         // the client window never appears.
         CameraComponent cameraComp = viewManager.getCameraComponent();
         InputManager inputManager = ClientContext.getInputManager();
         inputManager.initialize(frame.getCanvas(), cameraComp);
 
         // Default Policy: Enable global key and mouse focus everywhere
         // Note: the app base will impose its own (different) policy later
         inputManager.addKeyMouseFocus(inputManager.getGlobalFocusEntity());
 
         /* For Testing FocusEvent3D
         InputManager3D.getInputManager().addGlobalEventListener(
         new EventClassListener () {
         private final Logger logger = Logger.getLogger("My Logger");
         public Class[] eventClassesToConsume () {
         return new Class[] { FocusEvent3D.class };
         }
         public void commitEvent (Event event) {
         logger.severe("Global listener: received mouse event, event = " + event);
         }
         });
          */
 
         /* Note: Example of global key and mouse event listener
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
         MouseEvent3D mouseEvent = (MouseEvent3D) event;
         System.err.println("Event pickDetails = " + mouseEvent.getPickDetails());
         System.err.println("Event entity = " + mouseEvent.getEntity());
         }
         }
         });
          */
 
         frame.setDesiredFrameRate(desiredFrameRate);
     }
 
     /**
      * Returns the frame of the Wonderland client window.
      * @return the frame of the Wonderland client window.
      */
     public static MainFrame getFrame() {
         return frame;
     }
 
     /**
      * Set the main frame
      * @param frame the new main frame
      */
     public static void setFrame(MainFrame frame) {
         JmeClientMain.frame = frame;
     }
 
     /**
      * Load system properties and properties from the named file
      * @param propsURL the URL of the properties file to load
      */
     protected void loadProperties(URL propsURL) {
         // load the given file
         if (propsURL != null) {
             try {
                 System.getProperties().load(propsURL.openStream());
             } catch (IOException ioe) {
                 LOGGER.log(Level.WARNING, "Error reading properties from " +
                         propsURL, ioe);
             }
         }
     }
 
     /**
      * Check we are running in a supported VM.
      */
     private void checkVmVersion() {
         try {
            Class clazz = Class.forName("javax.lang.model.SourceVersion");
         } catch (ClassNotFoundException ex) {
             Logger.getAnonymousLogger().severe("Java Version is older than 6");
             JOptionPane.showMessageDialog(null, 
                     BUNDLE.getString("JAVA_VERSION_1") + "\n\n" +
                     BUNDLE.getString("JAVA_VERSION_2") + "\n" +
                     BUNDLE.getString("JAVA_VERSION_3") + "\n\n" +
                     BUNDLE.getString("JAVA_VERSION_4") + "\n" +
                     BUNDLE.getString("JAVA_VERSION_5"),
                     BUNDLE.getString("JAVA_VERSION"), JOptionPane.ERROR_MESSAGE);
             System.exit(1);
         }
     }
 }
