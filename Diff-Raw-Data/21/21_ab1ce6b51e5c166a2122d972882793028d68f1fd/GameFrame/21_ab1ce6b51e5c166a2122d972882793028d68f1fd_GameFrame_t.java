 package com.zabador.graphics_engine;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.*;
 import javax.swing.*;
 
 import com.zabador.entities.Hero;
 import com.zabador.entities.ImageEntity;
 
 import java.util.*;
 
 @SuppressWarnings("serial")
 public class GameFrame extends JFrame implements Runnable, KeyListener {
     int screenWidth = 800;
     int screenHeight = 600;
     int frameCount = 0, frameRate = 0;
     Long startTime;
 
     static int SPRITE_NORMAL = 0;
     static int SPRITE_COLLIDED = 1;
 
     //double buffer objects
     BufferedImage backbuffer;
     Graphics2D g2d;
 
     Hero hero;
     ImageEntity background;
     Thread gameloop;
     Random rand = new Random();
 
     boolean keyUP,keyDOWN,keyLEFT,keyRIGHT;
 
     public GameFrame() {
         super("Our game");
         setSize(screenWidth, screenHeight);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         //create the back buffer for smooth graphics
         backbuffer = new BufferedImage(screenWidth, screenHeight,
                 BufferedImage.TYPE_INT_RGB);
         g2d = backbuffer.createGraphics();
 
         addKeyListener(this);
 
         //load the background
         background = new ImageEntity(this);
         background.load("/images/bluespace.png");
 
         //load the asteroid sprite
         hero = new Hero(this, g2d);
         hero.load("/images/player.png");
         Point point = new Point(100, 100);
         hero.setPosition(point);
         startTime = System.currentTimeMillis();
 
         gameloop = new Thread(this);
         gameloop.start();
     }
 
     public void run() {
         Thread t = Thread.currentThread();
         while (t == gameloop) {
             try {
                 Thread.sleep(15);
                 gameUpdate();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
             //draw the background
             g2d.drawImage(background.getImage(), 0, 0, screenWidth - 1,
                     screenHeight - 1, this);
             g2d.drawString("FPS: " + frameRate, 5,10);
            g2d.drawString("x: " + hero.position().x, 5,20);
            g2d.drawString("y: " + hero.position().y, 5,30);
 
             hero.transform();
             hero.draw();
 
             repaint();
         }
     }
 
     private void gameUpdate() {
         frameCount++;
         if(System.currentTimeMillis() > startTime + 1000) {
             startTime = System.currentTimeMillis();
             frameRate = frameCount;
             frameCount = 0;
         }
         checkInput();
         updateHero();
     }
 
     private void updateHero() {
        Point center = hero.center();
        int x = center.x;
        int y = center.y;
        int width = hero.imageWidth();
        int height = hero.imageHeight();

        if(x - width/2 < 0)
            hero.setVelocity(new Point(width/2,hero.position().y));
        else if(x + width > screenWidth)
            hero.setPosition(new Point(screenWidth - width +2, hero.position().y));
        else if(y - height/2 < 0)
            hero.setPosition(new Point(hero.position().x, height/2));
        else if(y + height/2 > screenHeight) 
            hero.setPosition(new Point(hero.position().x, screenHeight - height/2));
        else
            hero.updatePosition();

         hero.setState(SPRITE_NORMAL);
     }
 
     public void paint(Graphics g) {
         //draw the back buffer to the screen
         g.drawImage(backbuffer, 0, 0, this);
     }
 
     public void checkInput() {
         if (keyUP) {
             hero.setVelocity(new Point(0,-2));
         }
         else if (keyDOWN) {
             hero.setVelocity(new Point(0,2));
         }
         else if (keyLEFT) {
             hero.setVelocity(new Point(-2,0));
         }
         else if (keyRIGHT) {
             hero.setVelocity(new Point(2,0));
         }
         else hero.setVelocity(new Point(0,0));
     }
 
     public void keyPressed(KeyEvent e) {
         System.out.println("Keypressed");
         switch(e.getKeyCode()) {
             case KeyEvent.VK_UP:
             case KeyEvent.VK_W:
                 keyUP = true;
                 break;
             case KeyEvent.VK_DOWN:
             case KeyEvent.VK_S:
                 keyDOWN = true;
                 break;
             case KeyEvent.VK_LEFT:
             case KeyEvent.VK_A:
                 keyLEFT = true;
                 break;
             case KeyEvent.VK_RIGHT:
             case KeyEvent.VK_D:
                 keyRIGHT = true;
                 break;
         }
 
     }
 
     public void keyReleased(KeyEvent e) {
         switch(e.getKeyCode()) {
             case KeyEvent.VK_UP:
             case KeyEvent.VK_W:
                 keyUP = false;
                 break;
             case KeyEvent.VK_DOWN:
             case KeyEvent.VK_S:
                 keyDOWN = false;
                 break;
             case KeyEvent.VK_LEFT:
             case KeyEvent.VK_A:
                 keyLEFT = false;
                 break;
             case KeyEvent.VK_RIGHT:
             case KeyEvent.VK_D:
                 keyRIGHT = false;
                 break;
         }
 
 
     }
 
     public void keyTyped(KeyEvent e) {
     }
 }
