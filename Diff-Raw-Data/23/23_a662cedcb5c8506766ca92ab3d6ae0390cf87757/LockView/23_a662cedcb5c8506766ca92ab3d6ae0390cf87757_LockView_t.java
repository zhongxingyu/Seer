 package com.intelix.digihdmi.app.views;
 
 import javax.swing.Action;
 import javax.swing.JButton;
import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
 
        setLayout(new MigLayout("wrap 1,fill","[align center]"));
         
         btnUnlock = new JButton("Unlock");
         
         this.add(btnUnlock);
         this.add(new JLabel("Device is now locked."));
     }
 
     public void setUnlockAction(Action a)
     {
         btnUnlock.setAction(a);
     }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame("Lock View");
                LockView lv = new LockView();
                f.getContentPane().add(lv);
                f.setSize(600,400);
                f.setVisible(true);
            }
        });
    }
 }
