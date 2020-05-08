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
 
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.jdesktop.mtgame.FrameRateListener;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.help.HelpSystem;
 import org.jdesktop.wonderland.common.LogControl;
 import java.util.logging.Logger;
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.UIManager;
 import org.jdesktop.wonderland.client.jme.dnd.DragAndDropManager;
 
 /**
  * The Main JFrame for the wonderland jme client
  * 
  * @author  paulby
  */
 public class MainFrameImpl extends JFrame implements MainFrame {
 
     private static final Logger logger = Logger.getLogger(MainFrameImpl.class.getName());
     private static final ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle", Locale.getDefault());
     private JMenuItem logoutMI;
     private JMenuItem exitMI;
     private ButtonGroup cameraButtonGroup = new ButtonGroup();
     private JRadioButtonMenuItem firstPersonRB;
     private JRadioButtonMenuItem thirdPersonRB;
     private JRadioButtonMenuItem frontPersonRB;
 
     private final Map<JMenuItem, Integer> menuWeights =
                                               new HashMap<JMenuItem, Integer>();
 
     static {
         new LogControl(MainFrameImpl.class, "/org/jdesktop/wonderland/client/jme/resources/logging.properties");
     }
 
     // variables for the location field
     private String serverURL;
     private ServerURLListener serverListener;
 
     /** Creates new form MainFrame */
     public MainFrameImpl(WorldManager wm, int width, int height) {
 
         // Workaround for bug 15: Embedded Swing on Mac: SwingTest: radio button image problems
         // For now, force the cross-platform (metal) LAF to be used
         // Also workaround bug 10.
         try {
             UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
             if ("Mac OS X".equals(System.getProperty("os.name"))) {
                 //to workaround popup clipping on the mac we force top-level popups
                 //note: this is implemented in scenario's EmbeddedPopupFactory
                 javax.swing.UIManager.put("PopupFactory.forceHeavyWeight", Boolean.TRUE);
             }
         } catch (Exception ex) {
             logger.warning("Loading of " + UIManager.getCrossPlatformLookAndFeelClassName() + " look-and-feel failed, exception = " + ex);
         }
 
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 
         initComponents();
         initMenus();
 
         wm.getRenderManager().setFrameRateListener(new FrameRateListener() {
 
             public void currentFramerate(float framerate) {
                 fpsLabel.setText("FPS: " + framerate);
             }
         }, 100);
 
         setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle").getString("Wonderland"));
         centerPanel.setMinimumSize(new Dimension(width, height));
         centerPanel.setPreferredSize(new Dimension(width, height));
 
         // Register the main panel with the drag-and-drop manager
         DragAndDropManager.getDragAndDropManager().setDropTarget(centerPanel);
 
         serverField.getDocument().addDocumentListener(new DocumentListener() {
 
             public void insertUpdate(DocumentEvent e) {
                 updateGoButton();
             }
 
             public void removeUpdate(DocumentEvent e) {
                 updateGoButton();
             }
 
             public void changedUpdate(DocumentEvent e) {
                 updateGoButton();
             }
         });
 
         // Listen for the "Enter" key on the server text field and activate
         // the go button
         serverField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String serverText = serverField.getText();
                 if (serverText != null && serverText.equals("") == false) {
                     goButton.doClick();
                 }
             }
         });
 
         pack();
     }
 
     private void initMenus() {
         // File menu
         // Log out
         logoutMI = new JMenuItem(bundle.getString("Log out"));
         logoutMI.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent evt) {
                 logoutMIActionPerformed(evt);
             }
         });
         addToFileMenu(logoutMI, 2);
 
         // Exit
         exitMI = new JMenuItem(bundle.getString("Exit"));
         exitMI.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent evt) {
                 exitMIActionPerformed(evt);
             }
         });
         addToFileMenu(exitMI, 3);
 
         // View menu
         firstPersonRB = new JRadioButtonMenuItem(bundle.getString("First Person Camera"));
         firstPersonRB.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         addToViewMenu(firstPersonRB, 0);
         cameraButtonGroup.add(firstPersonRB);
 
         thirdPersonRB = new JRadioButtonMenuItem(bundle.getString("Third Person Camera"));
         thirdPersonRB.setSelected(true);
         thirdPersonRB.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         addToViewMenu(thirdPersonRB, 1);
         cameraButtonGroup.add(thirdPersonRB);
 
         frontPersonRB = new JRadioButtonMenuItem(bundle.getString("Front Camera"));
         frontPersonRB.setToolTipText("A camera looking at the front of the avatar (for testing only)");
         frontPersonRB.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         addToViewMenu(frontPersonRB, 2);
         cameraButtonGroup.add(frontPersonRB);
 
         // Help menu
         HelpSystem helpSystem = new HelpSystem();
         JMenu helpMenu = helpSystem.getHelpJMenu();
         mainMenuBar.add(helpMenu);
     }
 
     private void logoutMIActionPerformed(ActionEvent evt) {
         if (serverListener != null) {
             serverListener.logout();
         }
 
         serverURL = null;
         updateGoButton();
     }
 
     private void exitMIActionPerformed(ActionEvent evt) {
         System.exit(0);
     }
 
     private void cameraChangedActionPerformed(java.awt.event.ActionEvent evt) {
         if (evt.getSource() == firstPersonRB) {
             ClientContextJME.getViewManager().setCameraProcessor(new FirstPersonCameraProcessor());
         } else if (evt.getSource() == thirdPersonRB) {
             ClientContextJME.getViewManager().setCameraProcessor(new ThirdPersonCameraProcessor());
         } else if (evt.getSource() == frontPersonRB) {
             ClientContextJME.getViewManager().setCameraProcessor(new FrontHackPersonCameraProcessor());
         }
 
     }
 
     public void updateGoButton() {
         String cur = serverField.getText();
         if (cur != null && cur.length() > 0 && !cur.equals(serverURL)) {
             goButton.setEnabled(true);
         } else {
             goButton.setEnabled(false);
         }
     }
 
     /**
      * Return the JME frame
      * @return the frame
      */
     public JFrame getFrame() {
         return this;
     }
 
     /**
      * Returns the canvas of the frame.
      */
     public Canvas getCanvas() {
         return ViewManager.getViewManager().getCanvas();
     }
 
     /**
      * Returns the panel of the frame in which the 3D canvas resides.
      */
     public JPanel getCanvas3DPanel() {
         return centerPanel;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToMenu(JMenu menu, JMenuItem menuItem, int weight) {
         if (weight < 0) {
             weight = Integer.MAX_VALUE;
         }
 
         logger.fine(menu.getText() + " menu: inserting [" + menuItem.getText() + 
                     "] with weight: " + weight);
 
         // find the index of the first menu item with a higher weight or
         // the same weight and later in the alphabet
         int index = 0;
         for (index = 0; index < menu.getItemCount(); index++) {
             JMenuItem curItem = menu.getItem(index);
             int curWeight = menuWeights.get(curItem);
             if (curWeight > weight) {
                 break;
             } else if (curWeight == weight) {
                 if (curItem.getName() == null) {
                     break;
                 }
 
                 if (menuItem.getName() != null &&
                     menuItem.getName().compareTo(curItem.getName()) > 0)
                 {
                     break;
                 }
             }
         }
 
         // add the item at the right place
         menu.insert(menuItem, index);
 
         // remember the menu's weight
         menuWeights.put(menuItem, weight);
     }
 
     /**
      * Remove the given menu item from a menu
      * @param menu the menu to remove from
      * @param item the item to remove
      */
     public void removeFromMenu(JMenu menu, JMenuItem item) {
         menu.remove(item);
         menuWeights.remove(item);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToFileMenu(JMenuItem menuItem) {
         addToMenu(fileMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToFileMenu(JMenuItem menuItem, int index) {
         addToMenu(fileMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromFileMenu(JMenuItem menuItem) {
         removeFromMenu(fileMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToEditMenu(JMenuItem menuItem) {
         addToMenu(editMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToEditMenu(JMenuItem menuItem, int index) {
         addToMenu(editMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromEditMenu(JMenuItem menuItem) {
         removeFromMenu(editMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToViewMenu(JMenuItem menuItem) {
         addToMenu(viewMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToViewMenu(JMenuItem menuItem, int index) {
         addToMenu(viewMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromViewMenu(JMenuItem menuItem) {
         removeFromMenu(viewMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToToolsMenu(JMenuItem menuItem) {
         addToMenu(toolsMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToToolsMenu(JMenuItem menuItem, int index) {
         addToMenu(toolsMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromToolsMenu(JMenuItem menuItem) {
         removeFromMenu(toolsMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToPlacemarksMenu(JMenuItem menuItem) {
         addToMenu(placemarksMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToPlacemarksMenu(JMenuItem menuItem, int index) {
         addToMenu(placemarksMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromPlacemarksMenu(JMenuItem menuItem) {
         removeFromMenu(placemarksMenu, menuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToWindowMenu(JMenuItem menuItem) {
         addToMenu(windowMenu, menuItem, -1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addToWindowMenu(JMenuItem menuItem, int index) {
         addToMenu(windowMenu, menuItem, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeFromWindowMenu(JMenuItem menuItem) {
         removeFromMenu(windowMenu, menuItem);
     }
 
     /**
      * Set the server URL in the location field
      * @param serverURL the server URL to set
      */
     public void setServerURL(String serverURL) {
         this.serverURL = serverURL;
         serverField.setText(serverURL);
     }
 
     public void addServerURLListener(ServerURLListener listener) {
         serverListener = listener;
     }
 
     public void setMessageLabel(String msg) {
         messageLabel.setText(msg);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         serverPanel = new javax.swing.JPanel();
         serverLabel = new javax.swing.JLabel();
         serverField = new javax.swing.JTextField();
         goButton = new javax.swing.JButton();
         centerPanel = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         messageLabel = new javax.swing.JTextField();
         jPanel3 = new javax.swing.JPanel();
         fpsLabel = new javax.swing.JLabel();
         mainMenuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         editMenu = new javax.swing.JMenu();
         viewMenu = new javax.swing.JMenu();
         placemarksMenu = new javax.swing.JMenu();
         toolsMenu = new javax.swing.JMenu();
         windowMenu = new javax.swing.JMenu();
 
         jLabel1.setText("jLabel1");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         serverPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         serverPanel.setLayout(new java.awt.BorderLayout());
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle"); // NOI18N
         serverLabel.setText(bundle.getString("Location:")); // NOI18N
         serverPanel.add(serverLabel, java.awt.BorderLayout.WEST);
 
         serverField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 serverFieldActionPerformed(evt);
             }
         });
         serverPanel.add(serverField, java.awt.BorderLayout.CENTER);
 
         goButton.setText(bundle.getString("Go!")); // NOI18N
         goButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 goButtonActionPerformed(evt);
             }
         });
         serverPanel.add(goButton, java.awt.BorderLayout.EAST);
 
         getContentPane().add(serverPanel, java.awt.BorderLayout.NORTH);
         getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);
 
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         messageLabel.setColumns(20);
         messageLabel.setEditable(false);
         messageLabel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 messageLabelActionPerformed(evt);
             }
         });
         jPanel2.add(messageLabel);
 
         jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);
 
         fpsLabel.setText(bundle.getString("FPS_:")); // NOI18N
         jPanel3.add(fpsLabel);
 
         jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);
 
         fileMenu.setText(bundle.getString("File")); // NOI18N
         mainMenuBar.add(fileMenu);
 
         editMenu.setText(bundle.getString("Edit")); // NOI18N
         mainMenuBar.add(editMenu);
 
         viewMenu.setText(bundle.getString("View")); // NOI18N
         mainMenuBar.add(viewMenu);
 
         placemarksMenu.setText("Placemarks");
         mainMenuBar.add(placemarksMenu);
 
         toolsMenu.setText(bundle.getString("Tools")); // NOI18N
         mainMenuBar.add(toolsMenu);
 
         windowMenu.setText("Window");
         mainMenuBar.add(windowMenu);
 
         setJMenuBar(mainMenuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void serverFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverFieldActionPerformed
    goButtonActionPerformed(evt);
 }//GEN-LAST:event_serverFieldActionPerformed
 
 private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
     logger.info("[MainFrameImp] GO! " + serverField.getText());
 
     if (serverListener != null) {
         serverListener.serverURLChanged(serverField.getText());
     }
 }//GEN-LAST:event_goButtonActionPerformed
 
 private void messageLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageLabelActionPerformed
     // TODO add your handling code here:
 }//GEN-LAST:event_messageLabelActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel centerPanel;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JLabel fpsLabel;
     private javax.swing.JButton goButton;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JMenuBar mainMenuBar;
     private javax.swing.JTextField messageLabel;
     private javax.swing.JMenu placemarksMenu;
     private javax.swing.JTextField serverField;
     private javax.swing.JLabel serverLabel;
     private javax.swing.JPanel serverPanel;
     private javax.swing.JMenu toolsMenu;
     private javax.swing.JMenu viewMenu;
     private javax.swing.JMenu windowMenu;
     // End of variables declaration//GEN-END:variables
 
 }
