 package com.oldworldind.app.gui.zebralabel;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 /**
  * @since Dec 15, 2012 at 1:26:44 AM
  * @author mcolegrove
  */
 public class RouteLabelApp {
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         displayUsage();
        System.out.println("Hello World!");
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 createPickandRunLabelPanel();
             }
         });
     }
 
     private static void createPickandRunLabelPanel() {
         JFrame jf = new JFrame("Label Router");
          jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 
         LabelPrintManagerForm pmf = LabelPrintManagerForm.getInstance();
         pmf.setOpaque(true); //content panes must be opaque
         jf.setContentPane(pmf);
         jf.pack();
         jf.setVisible(true);
     }
 
     /** 
      *
      */
     private static void displayUsage() {
        System.out.println("RouteLabel requires parameters see below");
        System.out.println("java RouteLabel <input File to Route>  <device>");
     }
 }
