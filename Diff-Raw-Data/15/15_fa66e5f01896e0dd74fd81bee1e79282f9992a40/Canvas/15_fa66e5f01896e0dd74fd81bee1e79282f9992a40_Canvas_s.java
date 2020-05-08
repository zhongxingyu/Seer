 package shapes;
 
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.*;
 
 public class Canvas extends JPanel implements Runnable {
 
   private Thread animator;
   private Game game;
 
   private final int DELAY = 50; // TODO: FPS should be a global constant
   public final int HEIGHT = 500;
   public final int WIDTH = 500;
 
  public Canvas(Game game) {
     super();
 
     this.game = game;
 
     setDoubleBuffered(true);
     setPreferredSize(new Dimension(WIDTH, HEIGHT));
     setSize(WIDTH, HEIGHT);
   }
 
   public void paint(Graphics g0) {
     super.paint(g0);
 
     Graphics2D g = (Graphics2D)g0;
     for (Shape s : game.getAllShapes()) {
       s.render(g);
     }
 
     Toolkit.getDefaultToolkit().sync();
     g0.dispose();
   }
 
   public void addNotify() {
     super.addNotify();
     animator = new Thread(this);
     animator.start();
   }
 
   public void run() {
     long beforeTime, timeDiff, sleep;
 
     beforeTime = System.currentTimeMillis();
 
     while (true) {
 
       game.autoUpdate();
       game.update();
       repaint();
 
       timeDiff = System.currentTimeMillis() - beforeTime;
       sleep = DELAY - timeDiff;
 
       if (sleep < 0)
         sleep = 2;
       try {
         Thread.sleep(sleep);
       } catch (InterruptedException e) {
         System.out.println("interrupted");
       }
 
       beforeTime = System.currentTimeMillis();
     }
   }
 
   public Point[] getCorners() {
     return new Point[] {
       new Point(0, HEIGHT),
       new Point(WIDTH, HEIGHT),
       new Point(WIDTH, 0),
       new Point(0, 0)
     };
   }
 
   Segment[] getBorders() {
     return new Segment[] {
       new Segment(new Point(0, HEIGHT), new Point(WIDTH, HEIGHT)),
       new Segment(new Point(WIDTH, HEIGHT), new Point(WIDTH, 0)),
       new Segment(new Point(WIDTH, 0), new Point(0, 0)),
       new Segment(new Point(0, 0), new Point(0, HEIGHT))
     };
   }
 }
