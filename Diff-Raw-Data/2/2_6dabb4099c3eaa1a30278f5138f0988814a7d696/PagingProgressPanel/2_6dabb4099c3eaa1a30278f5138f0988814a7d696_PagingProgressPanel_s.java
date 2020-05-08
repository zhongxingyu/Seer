 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import java.awt.BorderLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 /**
  *
  * @author Shawn
  */
 public class PagingProgressPanel extends JPanel {
     
     private JProgressBar bar;
     private JLabel label;
     
     public PagingProgressPanel() {
         super(new BorderLayout());
         init();
     }
     
     private void init() {
         bar = new JProgressBar(0, 100);
        label = new JLabel();
         
         this.add(label, BorderLayout.NORTH);
         this.add(bar, BorderLayout.SOUTH);
     }
 
     public JProgressBar getProgressBar() {
         return bar;
     }
 
     public JLabel getLabel() {
         return label;
     }
     
 }
