 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.intelix.digihdmi.app.views;
 
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import net.miginfocom.swing.MigLayout;
 
 /**
  *
  * @author mcaron
  */
 public class PasswordChangePanel extends ApplicationView {
 
     private JButton btnSetAdminPass;
     private JButton btnSetLockPass;
 
     public void setBtnAdminPsswdAction(Action action){ btnSetAdminPass.setAction(action); }
     public void setBtnUnlockPsswdAction(Action action){ btnSetLockPass.setAction(action); }
 
     @Override
     protected JComponent createRightComponent() {
         JPanel p = new JPanel();
 
         btnSetAdminPass = new JButton("Set Admin Password");
         btnSetAdminPass.setOpaque(false);
         btnSetLockPass = new JButton("Set Unlock Password");
         btnSetLockPass.setOpaque(false);
 
         p.setLayout(new MigLayout(
                 (System.getProperty("DEBUG_UI") == null ? "" : "debug,") +
                 "al 50% 50%, gapy 10"));
 
        p.add(btnSetAdminPass,   "align center, growx, wrap");
         p.add(btnSetLockPass,  "align center, growx, wrap");
 
         return p;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 JFrame f = new JFrame("Password Change Panel");
                 System.setProperty("DEBUG_UI", "true");
                 PasswordChangePanel lv = new PasswordChangePanel();
                 f.getContentPane().add(lv);
                 f.setSize(700,400);
                 f.setVisible(true);
             }
         });
     }
 
 }
