 package se2.cleaningRobot.simulation;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.FlowLayout;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import java.lang.*;
 
 /**
  * @author SE2 Gruppe 8
  */
 public class SimView extends JFrame {
 
   private SimModel model;
 
   public SimView(SimModel model) {
     super("CleaningRobot Simulation");
 
     this.model = model;
 
     // add all elements to the JFrame container
     add(this.model.getScene());
 
     // set up properties of the JFrame
    Dimension dim = new Dimension(500, 500);
    setMinimumSize(dim);
    setPreferredSize(dim);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //setUndecorated(true); // title bars == hate
     pack();
     setLocationRelativeTo(null);
     setVisible(true);
   }
 
   public void addListeners(KeyListener kl, MouseListener ml) {
     this.model.getScene().addKeyListener(kl);
     this.model.getScene().addMouseListener(ml);
   }
 }
