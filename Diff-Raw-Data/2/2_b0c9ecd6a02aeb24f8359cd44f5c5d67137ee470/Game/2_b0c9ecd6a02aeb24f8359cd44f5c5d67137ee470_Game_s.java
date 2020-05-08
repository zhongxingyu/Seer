 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.sdrx.ariath;
 
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Archon
  *
  * !> Begin the explanation with basic knowledge of the game architechture
  * (classes-wise, obviously) !> Explain the Auto Debug mode thing (Tools >
  * Options > Java > Debugger)
  */
 public class Game extends Canvas implements Runnable {
 
     // #1: Set size, Thread and loop control variables
     private static final int WIDTH = 160;
     private static final int HEIGHT = 120;
     private static final int SCALE = 4;
     private Thread thread;
     private boolean running;
     // #8: create Screen object, declare it, and also the BufferedImage and get a pointer for its pixels
     private Screen screen;
     // !> Explain these next two
     private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
     private int screenPixels[] = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
     private InputHandler input;
     private Sprite warrior;
     private Sprite mage;
     private Sprite grass;
 
     // #2: initialize the Dimension, the loop boolean and set the Thread on NEW state
     // !> we're only setting the Thread to NEW state (Thread.getState() proves it), not RUNNING
     public Game() {
         Dimension size = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
         setPreferredSize(size);
 
         thread = new Thread(this);
         running = false;
 
         // #9 second pass
         // !> note that Screen must have the window's size, not the tile's size (or get ArrayIndexOutOfBounds)
         new SpriteSheet("/resources/graphics/spritesheet.png");
         screen = new Screen(WIDTH, HEIGHT);
 
         warrior = new Sprite(0, 0);
         mage = new Sprite(16, 0);
         grass = new Sprite(32, 0);
 
         input = new InputHandler();
         addKeyListener(input);
     }
 
     // #3: provide start method, that will initialize the Thread
     // !> it's synchronized so that we don't have problems with starting and "running" being wrong
     public synchronized void start() {
         if (running) {
             return;
         }
         running = true;
         thread.start();
     }
 
     // #4: provide stop method
     // !> explain Thread.join()
     public synchronized void stop() {
         if (!running) {
             return;
         }
         running = false;
         try {
             thread.join();
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     // #5: define main loop. first pass only calls update() and render()
     @Override
     public void run() {
         // !> Do FPS line by line to see the point
         int chosenFps = 60;
         long past = System.currentTimeMillis();
         long displayFps = past;
         double timeToWait = 1000 / chosenFps;
         long now = 0;
         int fps = 0;
         int skips = 0;
         while (running) {
             now = System.currentTimeMillis();
             if ((now - past) >= timeToWait) {
                 past = now;
                 now = 0;
                 fps++;
                 updateGameState();
                 renderTiles();
             }
             if ((System.currentTimeMillis() - displayFps) >= 1000) {
                 System.out.println("FPS: " + fps);
                 displayFps = System.currentTimeMillis();
                 fps = 0;
             }
         }
     }
     // testes de mouse input
     int x, y = 0;
 
     private void updateGameState() {
         if (input.keys[KeyEvent.VK_UP]) {
             y--;
         }
         if (input.keys[KeyEvent.VK_DOWN]) {
             y++;
         }
         if (input.keys[KeyEvent.VK_LEFT]) {
             x--;
         }
         if (input.keys[KeyEvent.VK_RIGHT]) {
             x++;
         }
     }
 
     // #6: renders the game screen
     // !> explicar BufferStrategy
 //    private void renderScreen() {
 //        BufferStrategy bufferStrategy = getBufferStrategy();
 //        if (bufferStrategy == null) {
 //            createBufferStrategy(3);
 //            return;
 //        }
 //
 //        Graphics g = bufferStrategy.getDrawGraphics();
 //
 //        // #10: call Screen's render and translate its rendering array to local rendering array
 //        screen.render(10, 20);
 //
 ////        for (int v = 0; v < screen.height; v++) {
 ////            for (int u = 0; u < screen.width; u++) {
 ////                screenPixels[u + v * WIDTH] = screen.pixels[u + v * screen.width]; // Color.ORANGE.getRGB();
 ////            }
 ////        }
 //
 ////        for (int y1 = 0; y1 < 16; y1++) {
 ////            for (int x1 = 0; x1 < 16; x1++) {
 ////                screenPixels[x1 + y1 * WIDTH] = SpriteSheet.pixels[x1 + y1 * SpriteSheet.SPRITESHEET_SIZE];
 ////            }
 ////        }
 //        for (int y1 = 0; y1 < screen.height; y1++) {
 //            for (int x1 = 0; x1 < screen.width; x1++) {
//                // explicao do & 15: ele sempre ir voltar pro valor inicial 0 ~ 15
 //                // screenPixels[x1 + y1 * WIDTH] = SpriteSheet.pixels[(x1 & 15) + (y1 & 15) * SpriteSheet.SPRITESHEET_SIZE];
 //                // repeatable tile: background
 ////                screenPixels[x1 + y1 * WIDTH] = grass.pixelMap[(x1 & 15) + (y1 & 15) * SpriteSheet.TILE_SIZE];
 //                screenPixels[x1 + y1 * WIDTH] = screen.pixels[x1 + y1 * WIDTH];
 //                // to draw the char and keep track of its position, we need a Player class first. Leave it for later on.
 //            }
 //        }
 //
 //        g.drawImage(img, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
 //        g.dispose();
 //        bufferStrategy.show();
 //    }
     private void renderTiles() {
         BufferStrategy bufferStrategy = getBufferStrategy();
         if (bufferStrategy == null) {
             createBufferStrategy(3);
             return;
         }
 
         Graphics g = bufferStrategy.getDrawGraphics();
 
         // #10: call Screen's render and translate its rendering array to local rendering array
         screen.render(0, 0);
         screen.drawCharacter(((screen.width - SpriteSheet.TILE_SIZE) / 2) + x, ((screen.height - SpriteSheet.TILE_SIZE) / 2) + y);
 //        screen.drawAbstract();
 
         for (int i = 0; i < WIDTH * HEIGHT; i++) {
             screenPixels[i] = screen.pixels[i];
         }
         g.drawImage(img, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
         g.dispose();
         bufferStrategy.show();
     }
 
     // #7: test the program
     public static void main(String[] args) {
         Game game = new Game();
         JFrame frame = new JFrame("Ariath");
         frame.setResizable(false);
         frame.setAlwaysOnTop(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLocationRelativeTo(null);
         frame.add(game);
         frame.pack();
         frame.setVisible(true);
 
         game.start();
         
         game.requestFocus();
     }
 }
