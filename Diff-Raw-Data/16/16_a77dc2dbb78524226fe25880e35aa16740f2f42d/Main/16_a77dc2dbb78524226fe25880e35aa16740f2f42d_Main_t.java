 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mvhsbandinventory;
 
 import java.awt.Dimension;
 import javax.swing.JFrame;
 
 /**
  *
  * @author nicholson
  */
 public class Main
 {
 
     public static JFrame window;
     public static Display panel;
     public static InstrumentFileStore ifs;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
        ifs = new InstrumentFileStore("/home/jonathan/csvtest"); //linux
        //ifs = new InstrumentFileStore("C:/csvTest"); //pc
         window = new JFrame();
         panel = new Display(new InstrumentList(ifs));
 
         window.add(panel);
         window.setTitle("MVHS - Band Inventory");
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         window.setMinimumSize(new Dimension(930, 575));
         window.setVisible(true);
         panel.repaint();
     }
 }
