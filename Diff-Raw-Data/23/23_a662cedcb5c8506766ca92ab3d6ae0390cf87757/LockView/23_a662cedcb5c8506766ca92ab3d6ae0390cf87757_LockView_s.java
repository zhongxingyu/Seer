 package com.intelix.digihdmi.app.views;
 
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * @author Michael Caron <michael.r.caron@gmail.com>
  */
 public class LockView extends JPanel {
 
     JButton btnUnlock;
 
     public LockView() {
         initComponents();
     }
 
     private void initComponents() {
 
        setLayout(new MigLayout("", "[center,fill,grow]", "[grow]"));
         
         btnUnlock = new JButton("Unlock");
         
         this.add(btnUnlock);
         this.add(new JLabel("Device is now locked."));
     }
 
     public void setUnlockAction(Action a)
     {
         btnUnlock.setAction(a);
     }
 }
