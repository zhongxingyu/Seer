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
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 
 import com.jme.math.Vector3f;
 import imi.character.CharacterSteeringHelm;
 import imi.character.statemachine.GameContext;
 import imi.character.steering.GoTo;
 import imi.loaders.repository.Repository;
 import imi.utils.instruments.DefaultInstrumentation;
 import imi.utils.instruments.Instrumentation;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.ref.WeakReference;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.SwingUtilities;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.BaseClientPlugin;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellRenderer;
 import org.jdesktop.wonderland.client.cell.view.AvatarCell;
 import org.jdesktop.wonderland.client.cell.view.ViewCell;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
 import org.jdesktop.wonderland.modules.avatarbase.client.AvatarConfigManager;
 
 /**
  *
  * @author paulby
  */
 public class AvatarPlugin extends BaseClientPlugin
         implements ViewManagerListener
 {
     private ServerSessionManager loginManager;
     private String baseURL;
     private WeakReference<AvatarTestPanel> testPanelRef = null;
     private Instrumentation instrumentation;
 
     private JMenuItem avatarControlsMI;
     private JMenuItem avatarMI;
     private JMenuItem avatarSettingsMI;
     private JMenuItem startingLocationMI;
 
     private AvatarImiJME curAvatar;
     private boolean menusAdded = false;
     private final SessionLifecycleListener lifecycleListener;
 
     public AvatarPlugin() {
         this.lifecycleListener = new SessionLifecycleListener() {
 
             public void sessionCreated(WonderlandSession session) {
             }
 
             public void primarySession(WonderlandSession session) {
                 // We need a primary session, so wait for it
                 AvatarConfigManager.getAvatarConfigManager().addServer(AvatarPlugin.this.loginManager);
             }
         };
     }
 
     @Override
     public void initialize(ServerSessionManager loginManager) {
         this.loginManager = loginManager;
 
         // Set the base URL
         String serverHostAndPort = loginManager.getServerNameAndPort();
         this.baseURL = "wla://avatarbaseart@"+serverHostAndPort+"/";
 
         // XXX TODO: this shouldn't be done here -- it should be done in
         // activate or should be registered per session not globally 
         // XXX
         WorldManager worldManager = ClientContextJME.getWorldManager();
         worldManager.addUserData(Repository.class, new Repository(worldManager, baseURL, ClientContext.getUserDirectory("AvatarCache")));
 
         // Workaround to guarntee that the webdav module has been initialized
         loginManager.addLifecycleListener(lifecycleListener);
 
         avatarControlsMI = new JMenuItem("Avatar Controls");
         avatarControlsMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (testPanelRef == null || testPanelRef.get() == null) {
                     AvatarTestPanel test = new AvatarTestPanel();
                     JFrame f = new JFrame("Test Controls");
                     f.getContentPane().add(test);
                     f.pack();
                     f.setVisible(true);
                     f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                     test.setAvatarCharactar(curAvatar.getAvatarCharacter());
                     testPanelRef = new WeakReference(test);
                 } else {
                     SwingUtilities.getRoot(testPanelRef.get().getParent()).setVisible(true);
                 }
             }
         });
 
         avatarMI = new JMenuItem("Avatar...");
         avatarMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ViewCell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
                 if (cell instanceof AvatarCell) {
                     AvatarImiJME rend = (AvatarImiJME) ((AvatarCell) cell).getCellRenderer(ClientContext.getRendererType());
                     AvatarConfigFrame f = new AvatarConfigFrame(rend);
                     f.setVisible(true);
                 }
             }
         });
 
        avatarSettingsMI = new JMenuItem("Avatar Setings...");
         avatarSettingsMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 AvatarInstrumentation in = new AvatarInstrumentation(instrumentation);
                 in.setVisible(true);
             }
         });
 
         startingLocationMI = new JMenuItem("Starting Location");
         startingLocationMI.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 GameContext context = curAvatar.getAvatarCharacter().getContext();
                 CharacterSteeringHelm helm = curAvatar.getAvatarCharacter().getContext().getSteering();
                 helm.addTaskToTop(new GoTo(new Vector3f(0, 0, 0), context));
                 helm.setEnable(true);
             }
         });
 
         instrumentation = new DefaultInstrumentation(ClientContextJME.getWorldManager());
 
         // register the renderer for this session
         ClientContextJME.getAvatarRenderManager().registerRenderer(loginManager, AvatarImiJME.class);
 
         super.initialize(loginManager);
     }
 
     @Override
     public void cleanup() {
         // XXX should be done in deactivate XXX
         WorldManager worldManager = ClientContextJME.getWorldManager();
         worldManager.removeUserData(Repository.class);
 
         ClientContextJME.getAvatarRenderManager().unregisterRenderer(loginManager);
         AvatarConfigManager.getAvatarConfigManager().removeServer(loginManager);
         loginManager.removeLifecycleListener(lifecycleListener);
         super.cleanup();
     }
 
     @Override
     protected void activate() {        
         // add view listener -- menus will be added once the primary
         // view cell has changed
         ClientContextJME.getViewManager().addViewManagerListener(this);
         if (ClientContextJME.getViewManager().getPrimaryViewCell() != null) {
             // fake a view cell changed event
             primaryViewCellChanged(null, ClientContextJME.getViewManager().getPrimaryViewCell());
         }
     }
     
     @Override
     protected void deactivate() {
         // remove menus
         if (menusAdded) {
             JmeClientMain.getFrame().removeFromWindowMenu(avatarControlsMI);
             JmeClientMain.getFrame().removeFromEditMenu(avatarMI);
             JmeClientMain.getFrame().removeFromEditMenu(avatarSettingsMI);
             JmeClientMain.getFrame().removeFromPlacemarksMenu(startingLocationMI);
 
             menusAdded = false;
         }
         
         // remove view listener
         ClientContextJME.getViewManager().removeViewManagerListener(this);
     }
 
     public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
         CellRenderer rend = newViewCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
         if (!(rend instanceof AvatarImiJME)) {
             return;
         }
 
         // set the current avatar
         curAvatar = (AvatarImiJME) rend;
         if (testPanelRef == null || testPanelRef.get() == null) {
             // Do nothing
         } else {
             testPanelRef.get().setAvatarCharactar(curAvatar.getAvatarCharacter());
         }
 
         // notify the avatar config manager that the view cell has changed
         AvatarConfigManager.getAvatarConfigManager().setViewCell(newViewCell);
 
         // add menus
         if (!menusAdded) {
             JmeClientMain.getFrame().addToWindowMenu(avatarControlsMI, 0);
             JmeClientMain.getFrame().addToEditMenu(avatarMI, 0);
             JmeClientMain.getFrame().addToEditMenu(avatarSettingsMI, 1);
             JmeClientMain.getFrame().addToPlacemarksMenu(startingLocationMI, 0);
 
             menusAdded = true;
         }
     }
 }
