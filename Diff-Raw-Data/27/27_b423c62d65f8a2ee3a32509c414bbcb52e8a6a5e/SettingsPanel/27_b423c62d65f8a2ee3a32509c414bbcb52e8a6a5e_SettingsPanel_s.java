 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.menu.frames.settings;
 
 import cz.mgn.collabdesktop.utils.gui.FormUtility;
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 import javax.swing.JPanel;
 
 /**
  *
  * @author indy
  */
 public abstract class SettingsPanel extends JPanel {
 
     protected JPanel form;
     protected FormUtility formUtility;
 
     public SettingsPanel() {
         form = new JPanel();
         setLayout(new BorderLayout());
         add(form, BorderLayout.NORTH);
         form.setLayout(new GridBagLayout());
         formUtility = new FormUtility();
     }
 
     public abstract void reset();
 
     public abstract void set();
 
     public abstract String getPanelName();
 }
