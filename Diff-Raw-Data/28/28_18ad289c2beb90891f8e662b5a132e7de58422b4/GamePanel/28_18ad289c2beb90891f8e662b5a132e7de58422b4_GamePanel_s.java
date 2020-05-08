 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Main;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Rune
  */
 public class GamePanel extends JPanel implements Runnable {
 
    private static int WIDTH = 240;
    private static int HEIGHT = 240;
     private boolean running;
     private BufferedImage image;
    private int FPS = 60;
    private double averageFPS;
     int frameCount = 1;
    private int maxFrameCount = 60;
     private Thread thread;
     private Graphics2D g;
 
     public GamePanel() {
         super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
         setFocusable(true);
         requestFocus();
     }
 
     public void addNotify() {
         super.addNotify();
         if (thread == null) {
             thread = new Thread(this);
             thread.start();
             running = true;
         }
     }
 
     public void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
         g = (Graphics2D) image.getGraphics();
 
     }
 
     public void run() {
         init();
         long start, end, loopTime, wait;
        long targetTime = 1000 / FPS;
         long totalTime = 0;
 
         //gameloop
         while (running) {
             start = System.nanoTime();
 
             update();
             render();
             draw();
             
             loopTime = (System.nanoTime() - start) / 1000000;
             
             wait = targetTime - loopTime;
             if(wait < 0){
                 wait = 0;
             }
             try {
                 thread.sleep(wait);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             totalTime += System.nanoTime() - start;
             frameCount++;
 
            if (frameCount == maxFrameCount) {
                averageFPS = 1000D / ((totalTime / frameCount) / 1000000);
                 totalTime = 0;
                 frameCount = 0;
             }
         }
     }
 
     public void update() {
     }
 
     public void render() {
     }
 
     public void draw() {
         Graphics g2 = this.getGraphics();
         
         g2.setColor(new Color(0, 100, 255));
         g2.fillRect(0, 0, WIDTH, HEIGHT);        
         g2.drawImage(image, 0, 0, null);
         
         g2.setColor(Color.BLACK);
        g2.drawString("FPS:" + averageFPS, HEIGHT / 2, WIDTH / 2);
         g2.dispose();
     }
 }
