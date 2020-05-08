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
 package org.jdesktop.wonderland.modules.avatarbase.client;
 
 import com.jme.math.Vector3f;
 import imi.camera.CameraModels;
 import imi.camera.ChaseCamModel;
 import imi.camera.ChaseCamState;
 //import imi.character.AvatarSystem;
 import imi.character.AvatarSystem;
 import imi.character.avatar.Avatar;
 import imi.character.behavior.CharacterBehaviorManager;
 import imi.character.behavior.GoTo;
 import imi.character.statemachine.GameContext;
 import imi.repository.Repository;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.instrument.Instrumentation;
 import java.lang.ref.WeakReference;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import org.jdesktop.mtgame.RenderManager;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.BaseClientPlugin;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.client.cell.CellRenderer;
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.client.cell.view.ViewCell;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.jme.MainFrame;
 import org.jdesktop.wonderland.client.jme.MainFrameImpl;
 import org.jdesktop.wonderland.client.jme.ViewManager;
 import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.common.annotation.Plugin;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.modules.avatarbase.client.AvatarSessionLoader.AvatarLoaderStateListener;
 import org.jdesktop.wonderland.modules.avatarbase.client.AvatarSessionLoader.State;
 import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.FlexibleCameraAdapter;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarCollisionChangeRequestEvent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarControls;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME.AvatarChangedListener;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarTestPanel;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.GestureHUD;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WonderlandAvatarCache;
 import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
 import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry.AvatarInUseListener;
 import org.jdesktop.wonderland.modules.avatarbase.client.registry.spi.AvatarSPI;
 import org.jdesktop.wonderland.modules.avatarbase.client.ui.AvatarConfigFrame;
 import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
 
 /**
  * A client-side plugin to initialize the avatar system
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @Plugin
 public class AvatarClientPlugin extends BaseClientPlugin 
         implements AvatarLoaderStateListener, ViewManagerListener {
 
     private static Logger logger = Logger.getLogger(AvatarClientPlugin.class.getName());
     private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/avatarbase/client/resources/Bundle");
 
     // A map of a session and the loader for that session
     private Map<ServerSessionManager, AvatarSessionLoader> loaderMap = null;
 
     // Listener for when a new avatar becomes in-use
     private AvatarInUseListener inUseListener = null;
 
     // Listen for when the avatar character changes to update the state of the
     // chase camera.
     private AvatarChangedListener avatarChangedListener = null;
 
     // The current avatar cell renderer
     private AvatarImiJME avatarCellRenderer = null;
 
     // Chase camera state and model and menu item
     private ChaseCamState camState = null;
     private ChaseCamModel camModel = null;
     private JRadioButtonMenuItem chaseCameraMI = null;
 
 
     // Some test control panels for the avatar
     private WeakReference<AvatarTestPanel> testPanelRef = null;
     private JMenuItem avatarControlsMI = null;
     private JMenuItem avatarSettingsMI = null;
     private Instrumentation instrumentation = null;
 
     // The gesture HUD panel and menu item
     private WeakReference<GestureHUD> gestureHUDRef = null;
     private JMenuItem gestureMI = null;
     private boolean gestureHUDEnabled = false;
 
     // True if the menus have been added to the main menu, false if not
     private boolean menusAdded = false;
 
     // Menu items for the collision & gravity check boxes
     private JCheckBoxMenuItem collisionEnabledMI = null;
     private JCheckBoxMenuItem gravityEnabledMI = null;
 
     // The avatar configuration menu item
     private JMenuItem avatarConfigMI = null;
 
     /**
      * {@inheritDoc]
      */
     @Override
     public void initialize(ServerSessionManager manager) {
         loaderMap = new HashMap();
 
         // A listener for changes to the primary view cell renderer. (This
         // rarely happens in practice). When the avatar cell renderer changes,
         // reset the chase camera state.
         avatarChangedListener = new AvatarChangedListener() {
             public void avatarChanged(Avatar newAvatar) {
                 if (camState != null) {
                     // stop listener for changes from the old avatar cell
                     // renderer.
                     avatarCellRenderer.removeAvatarChangedListener(avatarChangedListener);
 
                     if (newAvatar.getContext() != null) {
                         camState.setTargetCharacter(newAvatar);
                     }
                     else {
                         camState.setTargetCharacter(null);
                     }
                     
                     // force an update
                     CellTransform transform = avatarCellRenderer.getCell().getLocalTransform();
                     camState.setCameraPosition(transform.getTranslation(null));
                 }
             }
         };
 
         // A menu item for the chase camera
         chaseCameraMI = new JRadioButtonMenuItem(bundle.getString("Chase_Camera"));
         chaseCameraMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                 Vector3f offsetVec = new Vector3f(0.0f, 4.0f, -10.0f);
                 // change camera hook
                 if (camState == null) {
                     camModel = (ChaseCamModel) CameraModels.getCameraModel(ChaseCamModel.class);
                     camState = new ChaseCamState(offsetVec, new Vector3f(0.0f, 1.8f, 0.0f));
                     camState.setDamping(1.7f);
                     camState.setLookAtDamping(1.7f);
                 }
                 camState.setCameraPosition(avatarCellRenderer.getCell().getLocalTransform().getTranslation(null).add(offsetVec));
                 camState.setTargetCharacter(avatarCellRenderer.getAvatarCharacter());
                 ClientContextJME.getViewManager().setCameraController(new FlexibleCameraAdapter(camModel, camState));
             }
         });
 
         // A menu item for a test control panel for the avatar.
 //        avatarControlsMI = new JMenuItem(bundle.getString("Avatar_Controls"));
 //        avatarControlsMI.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent e) {
 //                if (testPanelRef == null || testPanelRef.get() == null) {
 //                    AvatarTestPanel test = new AvatarTestPanel();
 //                    JFrame f = new JFrame(bundle.getString("Avatar_Controls"));
 //                    f.getContentPane().add(test);
 //                    f.pack();
 //                    f.setVisible(true);
 //                    f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 //                    test.setAvatarCharacter(avatarCellRenderer.getAvatarCharacter());
 //                    testPanelRef = new WeakReference(test);
 //                } else {
 //                    SwingUtilities.getRoot(testPanelRef.get().getParent()).setVisible(true);
 //                }
 //            }
 //        });
 
         // Avatar Instrumentation is a dev tool
 //        avatarSettingsMI = new JMenuItem(bundle.getString("Avatar_Settings..."));
 //        avatarSettingsMI.addActionListener(new ActionListener() {
 //            public void actionPerformed(ActionEvent e) {
 //                AvatarInstrumentation in = new AvatarInstrumentation(instrumentation);
 //                in.setVisible(true);
 //            }
 //        });
 //        instrumentation = new DefaultInstrumentation(ClientContextJME.getWorldManager());
 
         // The menu item for the Gesture (HUD)
         gestureMI = new JCheckBoxMenuItem(bundle.getString("Gesture_UI"));
         gestureMI.setSelected(false);
         gestureMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (gestureHUDRef == null || gestureHUDRef.get() == null) {
                     GestureHUD hud = new GestureHUD();
                     hud.setAvatarCharacter(avatarCellRenderer.getAvatarCharacter());
                     gestureHUDRef = new WeakReference(hud);
                 }
                 gestureHUDEnabled = !gestureHUDEnabled;
                 gestureMI.setSelected(gestureHUDEnabled);
                 ((GestureHUD)gestureHUDRef.get()).setVisible(gestureHUDEnabled);
             }
         });
 
         // The menu item for the avatar configuration
         avatarConfigMI = new JMenuItem(bundle.getString("Avatar_Appearance..."));
         avatarConfigMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 // First check to see whether OpenGL 2.0 is supported on the
                 // system. If not, then display a dialog saying you cannot
                 // configure your avatar.
                 RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
                String shaderCheck = System.getProperty("avatar.shaderCheck");
                boolean shaderPass = true;

                if (shaderCheck != null && shaderCheck.equals("true")) {
                    shaderPass = rm.getContextCaps().GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB >= 512;
                }
                if (rm.supportsOpenGL20() == false || !shaderPass) {
                     String msg = "Unfortunately your system graphics does not" +
                             " support the shaders which are required to configure" +
                             " the avatar system.";
                     String title = "Advanced Shaders Required";
                     JFrame frame = JmeClientMain.getFrame().getFrame();
                     JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 AvatarConfigFrame f = new AvatarConfigFrame();
                 f.setVisible(true);
             }
         });
 
         // Check box to set collision enabled
         collisionEnabledMI = new JCheckBoxMenuItem(bundle.getString("Avatar_Collision_Enabled"));
         collisionEnabledMI.setSelected(false); // TODO should be set by server
         collisionEnabledMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 boolean isCollision = collisionEnabledMI.isSelected();
                 boolean isGravity = gravityEnabledMI.isSelected();
                 ClientContext.getInputManager().postEvent(
                         new AvatarCollisionChangeRequestEvent(isCollision, isGravity));
             }
         });
 
         // Check box to set gravity (floor following) enabled
         gravityEnabledMI = new JCheckBoxMenuItem(bundle.getString("Avatar_Gravity_Enabled"));
         gravityEnabledMI.setSelected(true); // TODO should be set by server
         gravityEnabledMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 boolean isCollision = collisionEnabledMI.isSelected();
                 boolean isGravity = gravityEnabledMI.isSelected();
                 ClientContext.getInputManager().postEvent(
                         new AvatarCollisionChangeRequestEvent(isCollision, isGravity));
             }
         });
 
         // register the renderer for this session
         ClientContextJME.getAvatarRenderManager().registerRenderer(manager,
                 AvatarImiJME.class, AvatarControls.class);
 
         // XXX TODO: this shouldn't be done here -- it should be done in
         // activate or should be registered per session not globally
         // XXX
         try {
             String serverHostAndPort = manager.getServerNameAndPort();
             String baseURL = "wla://avatarbaseart/";
             URL url = AssetUtils.getAssetURL(baseURL, serverHostAndPort);
             WorldManager worldManager = ClientContextJME.getWorldManager();
             worldManager.addUserData(Repository.class, new Repository(worldManager,
                     new WonderlandAvatarCache(url.toExternalForm(),
                     ClientContext.getUserDirectory(bundle.getString("AvatarCache")))));
         } catch (MalformedURLException excp) {
             logger.log(Level.WARNING, "Unable to form avatar base URL", excp);
         }
 
         // Initialize the AvatarSystem after we set up caching
         AvatarSystem.initialize(ClientContextJME.getWorldManager());
         
         super.initialize(manager);
     }
 
     /**
      * {@inheritDoc]
      */
     @Override
     public void cleanup() {
         // XXX should be done in deactivate XXX
         WorldManager worldManager = ClientContextJME.getWorldManager();
         worldManager.removeUserData(Repository.class);
 
         ServerSessionManager manager = getSessionManager();
         ClientContextJME.getAvatarRenderManager().unregisterRenderer(manager);
         
         super.cleanup();
     }
 
     /**
      * {@inheritDoc]
      */
     @Override
     protected void activate() {
         // Upon a new session, load the session and put it in the map. Wait
         // for it to finish loading. When done then set up the view Cell or
         // wait for it to finish.
         ServerSessionManager manager = getSessionManager();
         AvatarSessionLoader loader = new AvatarSessionLoader(manager);
         loaderMap.put(manager, loader);
         loader.addAvatarLoaderStateListener(this);
         loader.load();
     }
 
     /**
      * {@inheritDoc]
      */
     @Override
     protected void deactivate() {
         // Fetch the loader from the currently known list and unload it
         AvatarSessionLoader loader = loaderMap.get(getSessionManager());
         if (loader != null) {
             loader.removeAvatarLoaderStateListener(this);
             loader.unload();
             loaderMap.remove(getSessionManager());
         }
         
         ViewManager.getViewManager().removeViewManagerListener(this);
 
         // Remove the listener for changes in avatar in use
         if (inUseListener != null) {
             AvatarRegistry.getAvatarRegistry().removeAvatarInUseListener(inUseListener);
             inUseListener = null;
         }
 
         // remove menus
         if (menusAdded == true) {
             MainFrame frame = JmeClientMain.getFrame();
             frame.removeFromWindowMenu(gestureMI);
             frame.removeFromToolsMenu(collisionEnabledMI);
             frame.removeFromToolsMenu(gravityEnabledMI);
             frame.removeFromEditMenu(avatarConfigMI);
             
             if (frame instanceof MainFrameImpl) { // Until MainFrame gets this method added
                 ((MainFrameImpl) frame).removeFromCameraChoices(chaseCameraMI);
             }
             else {
                 frame.removeFromViewMenu(chaseCameraMI);
             }
 
             // Remove the avatar controls (test) if it exists
             if (avatarControlsMI != null) {
                 frame.removeFromWindowMenu(avatarControlsMI);
             }
 
             // Add the avatar instrumentions settings if it exists
             if (avatarSettingsMI != null) {
                 frame.removeFromEditMenu(avatarSettingsMI);
             }
             menusAdded = false;
         }
     }
 
     /**
      * {@inheritDoc]
      */
     public void stateChanged(State state) {
         if (state == State.READY) {
             // If the state is ready, then set-up the primary view Cell if it is
             // ready or wait for it to become ready.
             ViewManager manager = ViewManager.getViewManager();
             manager.addViewManagerListener(this);
             if (manager.getPrimaryViewCell() != null) {
                 // fake a view cell changed event
                 primaryViewCellChanged(null, manager.getPrimaryViewCell());
             }
         }
     }
 
     /**
      * {@inheritDoc]
      */
     public void primaryViewCellChanged(ViewCell oldViewCell, final ViewCell newViewCell) {
 
         logger.warning("Primary view Cell Changes to " + newViewCell.getName());
 
         // Fetch the cell renderer for the new primary view Cell. It should
         // be of type AvatarImiJME. If not, log a warning and return
         CellRenderer rend = newViewCell.getCellRenderer(RendererType.RENDERER_JME);
         if (!(rend instanceof AvatarImiJME)) {
             logger.warning("Cell renderer for view " + newViewCell.getName() +
                     " is not of type AvatarImiJME.");
             return;
         }
 
         // If there is an old avatar, then remove the listener (although in
         // practice primary view cells do not change.
         if (avatarCellRenderer != null) {
             avatarCellRenderer.removeAvatarChangedListener(avatarChangedListener);
         }
 
         // set the current avatar
         avatarCellRenderer = (AvatarImiJME) rend;
 
         // start listener for new changes. This is used for the chase camera.
         avatarCellRenderer.addAvatarChangedListener(avatarChangedListener);
 
         // Set the state of the chase camera from the current avatar in the
         // cell renderer.
         if (camState != null) {
             camState.setTargetCharacter(avatarCellRenderer.getAvatarCharacter());
             camModel.reset(camState);
         }
 
         // Initialize the avatar control panel (test) with the current avatar
         // character.
 //        if (testPanelRef != null && testPanelRef.get() != null) {
 //            testPanelRef.get().setAvatarCharacter(avatarCellRenderer.getAvatarCharacter());
 //        }
 
         // Initialize the gesture HUD panel with the current avatar character.
         if (gestureHUDRef != null && gestureHUDRef.get() != null) {
             gestureHUDRef.get().setAvatarCharacter(avatarCellRenderer.getAvatarCharacter());
         }
 
         // We also want to listen (if we aren't doing so already) for when the
         // avatar in-use has changed.
         if (inUseListener == null) {
             inUseListener = new AvatarInUseListener() {
                 public void avatarInUse(AvatarSPI avatar, boolean isLocal) {
                     refreshAvatarInUse(newViewCell, isLocal);
                 }
             };
             AvatarRegistry.getAvatarRegistry().addAvatarInUseListener(inUseListener);
         }
 
         // Once the avatar loader is ready and the primary view has been set,
         // we tell the avatar cell component to set it's avatar in use.
         refreshAvatarInUse(newViewCell, false);
 
         // Finally, enable the menu items to allow avatar configuration. We
         // do this after the view cell is set, so we know we have an avatar
         // in the world.
         if (menusAdded == false) {
             MainFrame frame = JmeClientMain.getFrame();
             frame.addToWindowMenu(gestureMI, 0);
             frame.addToToolsMenu(gravityEnabledMI, -1);
             frame.addToToolsMenu(collisionEnabledMI, -1);
             frame.addToEditMenu(avatarConfigMI, 0);
 
             if (frame instanceof MainFrameImpl) { // Only until the MainFrame interface gets this method
                 ((MainFrameImpl) frame).addToCameraChoices(chaseCameraMI, 3);
             }
             else {
                 frame.addToViewMenu(chaseCameraMI, 3);
             }
 
             // Add the avatar control (test) if it exists
             if (avatarControlsMI != null) {
                 frame.addToWindowMenu(avatarControlsMI, 0);
             }
             
             // Add the avatar instrumentation settings if it exists
             if (avatarSettingsMI != null) {
                 frame.addToEditMenu(avatarSettingsMI, 1);
             }
             menusAdded = true;
         }
     }
 
     /**
      * Refreshes the primary view cell with the current avatar in use given the
      * current primary view cell.
      */
     public synchronized void refreshAvatarInUse(ViewCell viewCell, boolean isLocal) {
         // Once the avatar loader is ready and the primary view has been set,
         // we tell the avatar cell component to set it's avatar in use.
         AvatarConfigComponent configComponent = viewCell.getComponent(AvatarConfigComponent.class);
         AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
         AvatarSPI avatar = registry.getAvatarInUse();
         if (avatar != null) {
             ServerSessionManager session = viewCell.getCellCache().getSession().getSessionManager();
             AvatarConfigInfo configInfo = avatar.getAvatarConfigInfo(session);
             configComponent.requestAvatarConfigInfo(configInfo, isLocal);
         }
     }
 }
