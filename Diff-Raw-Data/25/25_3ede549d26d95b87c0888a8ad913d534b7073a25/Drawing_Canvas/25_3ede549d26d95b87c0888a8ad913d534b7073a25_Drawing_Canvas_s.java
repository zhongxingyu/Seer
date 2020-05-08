 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fibers;
 
 import java.util.Vector;
 import java.awt.*;
 import javax.swing.*;
 
 /**
  *
  * @author human
  */
 public class Drawing_Canvas extends javax.swing.JPanel implements Runnable {
 
   Thread mythread;
   boolean keeprunning = true;
   Things.Layers lay = null;
   /* *************************************************************************************************** */
 
   public Drawing_Canvas() {
     this.setBackground(Color.red);
     lay = new Things.Layers();
     lay.Make_Layers(3);
   }
 
   /* *************************************************************************************************** */
   @Override
   public void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2 = (Graphics2D) g;
     g2.setBackground(Color.yellow);
     g2.clearRect(0, 0, this.getWidth(), this.getHeight());
    Things.TransForm tr= new Things.TransForm();
    lay.Draw_Me(tr,g2);
   }
   /* *************************************************************************************************** */
 
   public void start() {
     mythread = new Thread(this);
     keeprunning = true;
     mythread.start();
   }
   /* *************************************************************************************************** */
 
   public void stop() {
     keeprunning = false;
     mythread = null;
   }
   /* *************************************************************************************************** */
 
   @Override
   public void run() {
     while (keeprunning) {
       boolean nop = true;
       System.out.println(nop);
     }
  } 
 }
