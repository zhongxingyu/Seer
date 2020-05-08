 package main.java;
 
 /**
 * Created with IntelliJ IDEA.
  * User: Alan Derrick
  * Date: 10/13/12
  * Time: 5:10 PM
  */
// file: EmptyFrame.java
// Adapted from Core Java, vol.1, by Horstmann & Cornell
 
 import javax.swing.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 class GetQuote extends JFrame {
 
     // Constructor:
     public GetQuote() {
         setTitle("Stock Quote v 1.0");
         setSize(300,200); // default size is 0,0
         setLocation(10,200); // default is 0,0 (top left corner)
 
         /**
          * Window Listeners
          */
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             } //windowClosing
         } );
     }
 
     public static void main(String[] args) {
         JFrame f = new GetQuote();
         f.setVisible(true);
     } //main
 } //class GetQuote
 
 
 /* NOTES:
	WindowAdapter() is class that implements WindowListers
 	with null methods for all the 7 methods of WindowListeners!
 	It is found in java.awt.event.*.
  */
