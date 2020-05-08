 package fi.helsinki.cs.oato;
 
 import fi.helsinki.cs.oato.gui.*;
 import javax.swing.*;
 
 public class Main {
     public static OnlineCourseSource onlineCourses;
     public static final boolean DEBUG = true;
 
    private static JFrame mainGui = new MainGUI();
 
     public static void main(String args[]) {
         onlineCourses = new OnlineCourseSource();
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                Main.mainGui.setVisible(true);
             }
         });
     }
 
     public static JFrame getMainGui() {
         return mainGui;
     }
 }
