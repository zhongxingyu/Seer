 package gui;
 
 import character.Input;
 import character.Player;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/4/13
  * Time: 2:53 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Window extends JFrame {
     int[][] map;
     ImagePanel image;
     Stack<Integer> keyStack;
 
     public Window() {
         initComponents();
         createEntities();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
     }
 
     public void initComponents() {
         int[] RGB;
 
         map = new int[][]{
                 {0,0,0,1,0,0,1,1,1,0,0,0,0},
                 {0,1,0,0,0,1,0,0,0,0,1,1,0},
                 {1,1,1,1,0,1,0,1,0,1,0,1,1},
                 {0,0,0,1,0,0,0,1,0,1,0,0,0},
                 {0,1,0,1,1,1,1,0,0,0,1,1,0},
                 {0,1,0,0,0,0,0,0,1,0,0,0,0},
                 {0,1,1,1,1,1,1,1,1,1,1,1,1},
                 {0,0,0,0,0,0,0,0,0,0,0,0,0}
         };
 
         keyStack = new Stack<Integer>();
 
         BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
 
         RGB = new int[img.getHeight() * img.getWidth()];
 
         for (int y = 0; y < img.getHeight(); y++) {
             for (int x = 0; x < img.getWidth(); x++) {
                 double yRatio = (double) y / img.getHeight();
                 double xRatio = (double) x / img.getWidth();
 
                 RGB[y * img.getWidth() + x] = getColour(map[(int) (map.length * yRatio)][(int) (map[0].length * xRatio)]);
             }
         }
 
         img.setRGB(0, 0, img.getWidth(), img.getHeight(), RGB, 0, img.getWidth());
 
         image = new ImagePanel(img, 500 / map[0].length, 500 / map.length);
 
         add(image);
 
         addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 keyStack.push(e.getKeyCode());
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
         pack();
     }
 
     //TODO: clean this up a lot
     public void createEntities() {
         BufferedImage img;
         try {
            img = ImageIO.read(new File("/home/brad/GSProject/src/images/abc.jpeg"));
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return;
         }
         Player player = new Player(img, new Input() {
             @Override
             public int getMovement(int x, int y) {
                 repaint();
                 if (!keyStack.empty()) {
                     switch (keyStack.pop()) {
                         case KeyEvent.VK_LEFT:
                         case KeyEvent.VK_A:
                             if(map[y][x-1] != 0)
                                 break;
                             return 1;
                         case KeyEvent.VK_UP:
                         case KeyEvent.VK_W:
                             if(map[y-1][x] != 0)
                                 break;
                             return 2;
                         case KeyEvent.VK_RIGHT:
                         case KeyEvent.VK_D:
                             if(map[y][x+1] != 0)
                                 break;
                             return 3;
                         case KeyEvent.VK_DOWN:
                         case KeyEvent.VK_S:
                             if(map[y+1][x] != 0)
                                 break;
                             return 4;
                     }
                 }
                 return 0;
             }
         });
 
         image.addEntity(player);
 
         Thread t = new Thread(player);
 
         t.start();
     }
 
     public int getColour(int index) {
         switch (index) {
             // White
             case 0:
                 return 0xFFFFFF;
             // Black
             case 1:
                 return 0;
             // Red
             case 2:
                 return 0xFF0000;
             // Green
             case 3:
                 return 0x00FF00;
             // Blue
             case 4:
                 return 0x0000FF;
             // Stupid colour
             case 5:
                 return 0x5EDA9E;
             default:
                 return 0;
         }
     }
 }
