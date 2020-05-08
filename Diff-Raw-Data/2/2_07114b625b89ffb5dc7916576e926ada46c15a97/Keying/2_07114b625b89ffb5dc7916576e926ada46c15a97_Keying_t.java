 package com.Bench3.myGame;
 
 import com.Bench3.myGame.levels.Level;
 import com.Bench3.myGame.levels.Level1;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class Keying extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     private long frameRate = 400;
     private long timeBetweenFrames = 1000/frameRate;
     Level currentLevel = null;
 
 
     public Keying(Display f, Images i) {
         currentLevel = new Level1(f, i);
     }
 
         //test commit
     @SuppressWarnings("unused")
     public void paintComponent(Graphics g) {
 
             super.paintComponent(g);
             currentLevel.paintComponent(g);
         try {
             Thread.sleep(timeBetweenFrames);
         } catch (InterruptedException e){
             //who cares
         }
         repaint();
     }
 
     private void battle(Graphics g) {
         currentLevel.battle(g);
     }
 
     private void moveCharacter() {
         currentLevel.moveCharacter();
     }
 
     public class jumpThread implements Runnable {
 
         @Override
         public void run() {
             try {
                 Thread.sleep(10);
             } catch (Exception e) {
                 e.printStackTrace();
                 new Thread(this).start();
                 System.exit(0);
             }
         }
     }
 }
