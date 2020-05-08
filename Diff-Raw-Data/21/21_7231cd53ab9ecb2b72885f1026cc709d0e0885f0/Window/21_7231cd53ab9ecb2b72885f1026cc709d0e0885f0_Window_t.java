 package com.cozycoding.crea1.tetris.ui;
 
 import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 
 /**
  * @author Marius Kristensen
  */
 public class Window extends JFrame {
 
     public Window(String title, Dimension dimension) throws HeadlessException {
         setTitle(title);
         setResizable(false);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setVisible(true);
     }
 
    public void addGamePanelAndSetVisible(GamePanel gamePanel) {
        getContentPane().add(gamePanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }


 }
