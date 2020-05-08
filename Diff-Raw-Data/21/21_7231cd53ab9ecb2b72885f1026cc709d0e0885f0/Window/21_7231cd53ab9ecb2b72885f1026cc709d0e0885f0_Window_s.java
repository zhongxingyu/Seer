 package com.cozycoding.crea1.tetris.ui;
 
 import javax.swing.JFrame;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 
 /**
  * @author Marius Kristensen
  */
 public class Window extends JFrame {
 
     public Window(String title, Dimension dimension) throws HeadlessException {
         setTitle(title);
        setSize(dimension);
         setResizable(false);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setVisible(true);
     }
 
 }
