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
 
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.util.Locale;
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
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 /**
  * The Main JFrame for the wonderland jme client
  * 
  * @author  paulby
  */
 public class MainFrameImpl extends JFrame implements MainFrame {
 
     private static final Logger logger = Logger.getLogger(MainFrameImpl.class.getName());
 
     private static final ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle", Locale.getDefault());
 
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
//        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//        } catch (Exception ex) {
//            logger.warning("Loading of Metal look-and-feel failed, exception = " + ex);
//        }
 
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 
         initComponents();
         
         // Add the help menu to the main menu bar
         HelpSystem helpSystem = new HelpSystem();
         JMenu helpMenu = helpSystem.getHelpJMenu();
         mainMenuBar.add(helpMenu);
         
         wm.getRenderManager().setFrameRateListener(new FrameRateListener() {
             public void currentFramerate(float framerate) {
                 fpsLabel.setText("FPS: "+framerate);
             }
         }, 100);
 
         setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/resources/bundle").getString("Wonderland"));
         centerPanel.setMinimumSize(new Dimension(width, height));
         centerPanel.setPreferredSize(new Dimension(width, height));
 
         serverField.getDocument().addDocumentListener(new DocumentListener() {
             public void insertUpdate(DocumentEvent e) {
                 checkButtons();
             }
 
             public void removeUpdate(DocumentEvent e) {
                 checkButtons();
             }
 
             public void changedUpdate(DocumentEvent e) {
                 checkButtons();
             }
 
             public void checkButtons() {
                 String cur = serverField.getText();
                 if (cur != null && cur.length() > 0 &&
                         !cur.equals(serverURL))
                 {
                     goButton.setEnabled(true);
                 } else {
                     goButton.setEnabled(false);
                 }
             }
         });
 
 
         pack();
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
     public Canvas getCanvas () {
         return ViewManager.getViewManager().getCanvas();
     }
 
     /**
      * Returns the panel of the frame in which the 3D canvas resides.
      */
     public JPanel getCanvas3DPanel () {
         return centerPanel;
     }
 
     /**
      * Add the specified menu item to the tool menu.
      * 
      * TODO - design a better way to manage the menus and toolsbars
      * 
      * @param menuItem
      */
     public void addToToolMenu(JMenuItem menuItem) {
         toolsMenu.add(menuItem);
     }
 
     /**
      * Add the specified menu item to the edit menu.
      *
      * TODO - design a better way to manage the menus and toolsbars
      *
      * @param menuItem
      */
     public void addToEditMenu(JMenuItem menuItem) {
         editMenu.add(menuItem);
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
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         cameraButtonGroup = new javax.swing.ButtonGroup();
         serverPanel = new javax.swing.JPanel();
         serverLabel = new javax.swing.JLabel();
         serverField = new javax.swing.JTextField();
         goButton = new javax.swing.JButton();
         centerPanel = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         fpsLabel = new javax.swing.JLabel();
         mainMenuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         logoutMI = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         exitMI = new javax.swing.JMenuItem();
         viewMenu = new javax.swing.JMenu();
         firstPersonRB = new javax.swing.JRadioButtonMenuItem();
         thirdPersonRB = new javax.swing.JRadioButtonMenuItem();
         frontPersonRB = new javax.swing.JRadioButtonMenuItem();
         editMenu = new javax.swing.JMenu();
         toolsMenu = new javax.swing.JMenu();
 
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
 
         fpsLabel.setText(bundle.getString("FPS_:")); // NOI18N
         jPanel1.add(fpsLabel);
 
         getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);
 
         fileMenu.setText(bundle.getString("File")); // NOI18N
 
         logoutMI.setText(bundle.getString("Log_out")); // NOI18N
         logoutMI.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logoutMIActionPerformed(evt);
             }
         });
         fileMenu.add(logoutMI);
         fileMenu.add(jSeparator1);
 
         exitMI.setText(bundle.getString("Exit")); // NOI18N
         exitMI.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitMIActionPerformed(evt);
             }
         });
         fileMenu.add(exitMI);
 
         mainMenuBar.add(fileMenu);
 
         viewMenu.setText(bundle.getString("View")); // NOI18N
 
         cameraButtonGroup.add(firstPersonRB);
         firstPersonRB.setText(bundle.getString("First_Person_Camera")); // NOI18N
         firstPersonRB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         viewMenu.add(firstPersonRB);
 
         cameraButtonGroup.add(thirdPersonRB);
         thirdPersonRB.setSelected(true);
         thirdPersonRB.setText(bundle.getString("Third_Person_Camera")); // NOI18N
         thirdPersonRB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         viewMenu.add(thirdPersonRB);
 
         cameraButtonGroup.add(frontPersonRB);
         frontPersonRB.setText("Front Camera");
         frontPersonRB.setToolTipText("A Camera looking at the front of the avatar (for testing only)");
         frontPersonRB.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cameraChangedActionPerformed(evt);
             }
         });
         viewMenu.add(frontPersonRB);
 
         mainMenuBar.add(viewMenu);
 
         editMenu.setText(bundle.getString("Edit")); // NOI18N
         mainMenuBar.add(editMenu);
 
         toolsMenu.setText(bundle.getString("Tools")); // NOI18N
         mainMenuBar.add(toolsMenu);
 
         setJMenuBar(mainMenuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void exitMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMIActionPerformed
 // TODO add your handling code here:
     System.exit(0);
 }//GEN-LAST:event_exitMIActionPerformed
 
 private void serverFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverFieldActionPerformed
     // TODO add your handling code here:
 }//GEN-LAST:event_serverFieldActionPerformed
 
 private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
     if (serverListener != null) {
         serverListener.serverURLChanged(serverField.getText());
     }
 }//GEN-LAST:event_goButtonActionPerformed
 
 private void logoutMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutMIActionPerformed
     if (serverListener != null) {
         serverListener.logout();
     }
 }//GEN-LAST:event_logoutMIActionPerformed
 
 private void cameraChangedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraChangedActionPerformed
     if (evt.getSource()==firstPersonRB) {
         ClientContextJME.getViewManager().setCameraProcessor(new FirstPersonCameraProcessor());
     } else if (evt.getSource()==thirdPersonRB) {
         ClientContextJME.getViewManager().setCameraProcessor(new ThirdPersonCameraProcessor());
     } else if (evt.getSource()==frontPersonRB) {
         ClientContextJME.getViewManager().setCameraProcessor(new FrontHackPersonCameraProcessor());
     }
 
 }//GEN-LAST:event_cameraChangedActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup cameraButtonGroup;
     private javax.swing.JPanel centerPanel;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenuItem exitMI;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JRadioButtonMenuItem firstPersonRB;
     private javax.swing.JLabel fpsLabel;
     private javax.swing.JRadioButtonMenuItem frontPersonRB;
     private javax.swing.JButton goButton;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JMenuItem logoutMI;
     private javax.swing.JMenuBar mainMenuBar;
     private javax.swing.JTextField serverField;
     private javax.swing.JLabel serverLabel;
     private javax.swing.JPanel serverPanel;
     private javax.swing.JRadioButtonMenuItem thirdPersonRB;
     private javax.swing.JMenu toolsMenu;
     private javax.swing.JMenu viewMenu;
     // End of variables declaration//GEN-END:variables
 
 }
