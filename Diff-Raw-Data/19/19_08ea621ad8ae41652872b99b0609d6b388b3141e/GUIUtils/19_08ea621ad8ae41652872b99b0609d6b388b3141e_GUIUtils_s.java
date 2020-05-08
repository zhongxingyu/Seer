/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package org.jdesktop.wonderland.client.jme.utils;
 
 import java.util.logging.Logger;
 import javax.swing.UIManager;
 
 /**
  *
  * @author nsimpson
  */
 public class GUIUtils {
 
     private static final Logger logger = Logger.getLogger(GUIUtils.class.getName());
 
     public static void initLookAndFeel() {
         try {
 
             boolean hasNimbus = false;
 
             try {
                 Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                 hasNimbus = true;
             } catch (ClassNotFoundException e) {
             }
 
             // Workaround for bug 15: Embedded Swing on Mac: SwingTest: radio button image problems
             // For now, force the cross-platform (metal) LAF to be used, or Nimbus
             // Also workaround bug 10.
             if (hasNimbus) {
                 UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
             } else {
                 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
             }
 
             if ("Mac OS X".equals(System.getProperty("os.name"))) {
                 //to workaround popup clipping on the mac we force top-level popups
                 //note: this is implemented in scenario's EmbeddedPopupFactory
                 javax.swing.UIManager.put("PopupFactory.forceHeavyWeight", Boolean.TRUE);
             }
         } catch (Exception ex) {
             logger.warning("Loading of " + UIManager.getCrossPlatformLookAndFeelClassName() + " look-and-feel failed, exception = " + ex);
         }
     }
 }
