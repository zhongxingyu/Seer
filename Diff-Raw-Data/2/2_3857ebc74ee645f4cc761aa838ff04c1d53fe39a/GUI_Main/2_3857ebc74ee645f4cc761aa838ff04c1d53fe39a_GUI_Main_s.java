 package gui;
 
 import javax.swing.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: anand
  * Date: 9/7/13
  * Time: 11:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class GUI_Main {
     static ArrayList<Long> intervalList;
 
     public static ArrayList<Long> getIntervalList() {
         return intervalList;
     }
 
     public static void setIntervalList(ArrayList<Long> intervalList) {
         GUI_Main.intervalList = intervalList;
     }
 
     static boolean clickOver = false;
 
     public static void main(String[] args) {
         // TODO Auto-generated method stub
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
 
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 // TODO Auto-generated method stub
                while(!clickOver)
                     createAndShowGUI();
 
 
 
 
             }
         });
 
     }
 
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread.
      */
 
     private static void createAndShowGUI(){
         //create and show window
         JFrame frame = new JFrame("Window");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //create and setup the new content pane
         Beep newContentPane = new Beep();
         newContentPane.setOpaque(true);//content panes must be opaque
         frame.setContentPane(newContentPane);
         frame.setLocation(100, 100);
         frame.addWindowListener(newContentPane);
         //display the pack
         frame.pack(); //causes window size to be made fit according to the size of its sub components
         frame.setVisible(true);
 
         if(clickOver)
             intervalList  = new ArrayList<Long>(newContentPane.getInterv());
 
     }
 }
