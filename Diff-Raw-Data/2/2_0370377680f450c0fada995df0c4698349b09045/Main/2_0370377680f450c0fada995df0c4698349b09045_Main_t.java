 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferStrategy;
 import java.util.Random;
 
 public class Main extends JPanel implements KeyListener, WindowListener {
   public static final int NUM_STARS = 200;
   private static final double ACCEL = 0.00000001;
   private static final double MAX_SPEED = 5.0;
   private boolean running = true;
   private final JFrame frame;
   double x = 600;
   double y = 2500;
   double angle;
   double speedX, speedY;
   private int rotateLeft;
   private int rotateRight;
   private int throttle;
 
   private final Polygon[] walls = {
           new Polygon(new int[]{-200,360,360,120,-200}, new int[]{-3427,-3427,-3147,-2747,-2747}, 5),
           new Polygon(new int[]{840,360,360,840}, new int[]{-3427,-3427,-3147,-3147}, 4),
           new Polygon(new int[]{840,1280,1280,1120,1000,1000,840}, new int[]{-3427,-3427,-2627,-2627,-2747,-2947,-3147}, 7),
           new Polygon(new int[]{120,120,-200,-200}, new int[]{-2747,-2107,-2107,-2747}, 4),
           new Polygon(new int[]{1120,1280,1280,800,800,960,960,1120}, new int[]{-2347,-2347,-1747,-1747,-1987,-2147,-2267,-2347}, 8),
           new Polygon(new int[]{360,480,560,560,280,280}, new int[]{-2267,-2267,-2067,-1747,-1747,-2107}, 6),
           new Polygon(new int[]{120,120,-200,-200}, new int[]{-2107,-1667,-1667,-2107}, 4),
           new Polygon(new int[]{120,680,680,520,120}, new int[]{-1507,-1267,-1067,-1067,-1267}, 5),
           new Polygon(new int[]{680,1160,1160,680}, new int[]{-1267,-1267,-1067,-1067}, 4),
           new Polygon(new int[]{960,1200,1320,1160}, new int[]{-1587,-1427,-1507,-1587}, 4),
           new Polygon(new int[]{1280,1520,1520,1280}, new int[]{-3427,-3427,-2627,-2627}, 4),
           new Polygon(new int[]{1280,1280,1520,1520}, new int[]{-2627,-2347,-2347,-2627}, 4),
           new Polygon(new int[]{1520,1520,1280,1280}, new int[]{-2347,-1947,-1747,-2347}, 4),
           new Polygon(new int[]{1400,1400,1640,1640}, new int[]{-1267,-1067,-1067,-1267}, 4),
           new Polygon(new int[]{1400,1760,1960,1960,1640}, new int[]{-1267,-1587,-1587,-1267,-1267}, 5),
           new Polygon(new int[]{-200,-200,120,120}, new int[]{-1667,-667,-667,-1667}, 4),
           new Polygon(new int[]{1280,920,1720}, new int[]{-867,-427,-467}, 3),
           new Polygon(new int[]{800,520,120,120}, new int[]{-707,-427,-427,-667}, 4),
           new Polygon(new int[]{-200,-200,120,120}, new int[]{-667,-267,-267,-667}, 4),
           new Polygon(new int[]{-200,-200,1960,1960}, new int[]{-267,13,13,-267}, 4),
           new Polygon(new int[]{1960,2560,2560,1960}, new int[]{13,13,-267,-267}, 4),
           new Polygon(new int[]{2040,2200,2080,2200,2120,2480,2560,2560}, new int[]{-267,-547,-667,-827,-947,-1267,-1267,-267}, 8),
           new Polygon(new int[]{2480,2200,2600,2560}, new int[]{-1267,-1867,-1867,-1267}, 4),
           new Polygon(new int[]{1680,1640,1720,1840,1800}, new int[]{-2267,-2187,-2107,-2147,-2267}, 5),
           new Polygon(new int[]{2000,1920,2000,2120,2120}, new int[]{-2387,-2307,-2227,-2267,-2347}, 5),
           new Polygon(new int[]{1720,1640,1760,1800}, new int[]{-2547,-2427,-2387,-2467}, 4),
           new Polygon(new int[]{1960,1840,1920,2040}, new int[]{-2667,-2547,-2507,-2667}, 4),
           new Polygon(new int[]{2160,2320,2360,2200}, new int[]{-2467,-2347,-2427,-2547}, 4),
           new Polygon(new int[]{2200,2200,2440,2440,2600,2600}, new int[]{-1867,-1867,-2267,-2747,-2747,-1867}, 6),
          new Polygon(new int[]{1520,1840,1840,1520}, new int[]{-2627,-2867,-3267,-3427}, 4),
           new Polygon(new int[]{2440,2160,2160,2600,2600}, new int[]{-2747,-2907,-3267,-3427,-2747}, 5),
   };
   private final Polygon[] blinks = {
           new Polygon(new int[]{560,800,800,560}, new int[]{-1907,-1907,-1827,-1827}, 4),
           new Polygon(new int[]{1520,1400,1640,1760}, new int[]{-1907,-1787,-1547,-1627}, 4),
   };
   private final Polygon[] goals = {
           new Polygon(new int[]{1520,2600,2160,1840}, new int[]{-3427,-3427,-3267,-3267}, 4),
   };
 
   public static void main(String[] args) {
     new Main().run();
   }
 
   private void run() {
     boolean died = false;
     boolean win = false;
     double[] starX = new double[NUM_STARS];
     double[] starY = new double[NUM_STARS];
     Random random = new Random();
     for (int i = 0; i < NUM_STARS; i++) {
       starX[i] = random.nextDouble();
       starY[i] = random.nextDouble();
     }
     long prevTime = System.nanoTime();
     long startTime = prevTime;
 
     long totalTime = 0;
     long ticks = 0;
 
     while (running) {
       long t = System.nanoTime();
       long delta = t - prevTime;
       prevTime = t;
 
       BufferStrategy bufferStrategy = frame.getBufferStrategy();
       Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
 
       if (win) {
         angle += 3 * delta * 0.00000001;
       } else if (!died) {
         angle += (rotateLeft + rotateRight) * delta * 0.00000001;
       }
       if (!win && !died) {
         speedX += Math.sin(angle) * throttle * delta * ACCEL;
         speedY += Math.cos(angle) * throttle * delta * ACCEL;
         speedY += Math.cos(Math.PI) * delta * ACCEL / 10;
         double foo = Math.sqrt(speedX * speedX + speedY * speedY);
         if (foo > MAX_SPEED) {
             speedX = speedX * MAX_SPEED / foo;
             speedY = speedY * MAX_SPEED / foo;
         }
       }
       x += speedX;
       y += speedY;
       g.setColor(Color.BLACK);
       g.fillRect(0, 0, 800, 600);
 
       for (int i = 0; i < NUM_STARS; i++) {
         int level = (i / (NUM_STARS / 3));
         int scale = 10 + 2*level;
         int color = 60 + level * 60;
         g.setColor(new Color(color, color, color));
         int x2 = (int) (-x * scale * 0.1 + 800 * scale * starX[i]) % 800;
         int y2 = (int) (y * scale * 0.1 + 600 * scale * starY[i]) % 600;
         x2 = (800 + x2) % 800;
         y2 = (600 + y2) % 600;
         g.fillOval(x2, y2, 1 + level, 1 + level);
       }
 
       boolean blinkVisible = (System.currentTimeMillis() / 1500) % 2 == 0;
       g.translate(400, 300);
 
       g.setColor(Color.LIGHT_GRAY);
       g.translate(-x, y);
       for (Polygon p : walls) {
         g.fillPolygon(p);
       }
       g.setColor(Color.YELLOW);
       for (Polygon p : goals) {
         g.fillPolygon(p);
       }
       if (blinkVisible) {
         g.setColor(Color.RED);
       } else {
         g.setColor(Color.DARK_GRAY);
       }
       for (Polygon p : blinks) {
         g.fillPolygon(p);
       }
       g.translate(x, -y);
 
       g.setPaint(new RadialGradientPaint(0, -10, 30, new float[]{0.2f, 0.7f, 0.8f}, new Color[]{Color.BLUE, Color.CYAN, Color.WHITE}));
       if (!died) {
         g.rotate(angle);
         Polygon ship = new Polygon(new int[]{0, 15, 0, -15, 0}, new int[]{-20, 20, 10, 20, -20}, 5);
         g.fillPolygon(ship);
         if (!win && throttle > 0) {
           int color = (int) ((System.currentTimeMillis() / 50) % 3);
           Color[] throttleColors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW};
           g.setColor(throttleColors[color]);
           g.drawPolyline(new int[]{-5, 0, 5}, new int[]{15, 25, 15}, 3);
         }
         g.rotate(-angle);
       } else {
         g.setColor(Color.RED);
         for (int i = 0; i < 3; i++) {
           int offset = (int) ((System.currentTimeMillis() / 30) % 5);
           g.drawOval(-i*10 - offset, -i*10 - offset, 2*i*10 + offset, 2*i*10 + offset);
         }
       }
 
       g.setColor(Color.WHITE);
       g.translate(-400, -300);
 
       if (!died && !win) {
         totalTime = t - startTime;
         ticks = totalTime / 100000000;
       }
       g.drawString(String.format("%.1f", ticks / 10d), 40, 40);
 
       Ellipse2D.Double body = new Ellipse2D.Double(-10 + x, -10 - y, 20, 20);
       for (Polygon p : walls) {
         Area areaA = new Area(body);
         areaA.intersect(new Area(p));
         if (!areaA.isEmpty()) {
             died = true;
         }
       }
       if (blinkVisible) {
         for (Polygon p : blinks) {
           Area areaA = new Area(body);
           areaA.intersect(new Area(p));
           if (!areaA.isEmpty()) {
             died = true;
           }
         }
       }
       for (Polygon p : goals) {
         Area areaA = new Area(body);
         areaA.intersect(new Area(p));
         if (!areaA.isEmpty()) {
           win = true;
         }
       }
 
       g.dispose();
       bufferStrategy.show();
       Toolkit.getDefaultToolkit().sync();
       try {
         Thread.sleep(10);
       } catch (InterruptedException e) {
       }
     }
 
     frame.dispose();
   }
 
   public Main() throws HeadlessException {
     frame = new JFrame("Cave4K");
     frame.addWindowListener(this);
     frame.setSize(800, 600);
     frame.setVisible(true);
     frame.createBufferStrategy(2);
     frame.add(this);
     frame.addKeyListener(this);
   }
 
   @Override
   public void keyTyped(KeyEvent keyEvent) {
   }
 
   @Override
   public void keyPressed(KeyEvent keyEvent) {
     if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
       rotateLeft = -1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
       rotateRight = 1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
       throttle = 1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
       running = false;
     }
   }
 
   @Override
   public void keyReleased(KeyEvent keyEvent) {
     if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
       rotateLeft = 0;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
       rotateRight = 0;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
       throttle = 0;
     }
   }
 
   @Override
   public void windowOpened(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowClosing(WindowEvent windowEvent) {
     running = false;
   }
 
   @Override
   public void windowClosed(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowIconified(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowDeiconified(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowActivated(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowDeactivated(WindowEvent windowEvent) {
   }
 }
