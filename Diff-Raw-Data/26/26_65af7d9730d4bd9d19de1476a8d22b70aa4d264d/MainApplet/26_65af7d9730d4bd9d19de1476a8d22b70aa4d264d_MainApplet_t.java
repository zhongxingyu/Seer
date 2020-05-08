 package mylittleterrorist;
 
 import java.applet.Applet;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class MainApplet extends Applet {
     private static final long serialVersionUID = 1L;
 
     // Milliseconds between each update
     public static final long UPDATE_PERIOD = 32;
 
     protected Timer timer;
     
     protected Image buffer;
     protected Rectangle viewBounds;
 
     protected Game game;
    
     @Override
     public void destroy() {
         super.destroy();
     }
 
     @Override
     public void init() {
         super.init();
         resize(624, 468);
         
         game = new Game();
         
         this.addMouseListener(new MouseAdapter() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (viewBounds != null && viewBounds.contains(e.getPoint())) {
                     Dimension size = game.getMapSize();
                     int x = (int)(e.getX() - viewBounds.getMinX()),
                         y = (int)(e.getY() - viewBounds.getMinY());
                     x = (int)((x/viewBounds.getWidth()) * size.width);
                     y = (int)((y/viewBounds.getHeight()) * size.height);
                     game.inputEvent(x,y,e);
                 }
             }
             
         });
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         
         Dimension size = game.getPixelSize();
         if (buffer == null || buffer.getWidth(this) != size.width ||
                 buffer.getHeight(this) != size.height) {
             buffer = createImage(size.width, size.height);
         }
         
         game.render((Graphics2D)buffer.getGraphics());
         
         double scale = Math.min(
                 (getWidth()+0.0)/size.width,
                 (getHeight()+0.0)/size.height);
         int scaledW = (int)(size.width * scale),
             scaledH = (int)(size.height * scale);
         int x = (getWidth()-scaledW)/2,
             y = (getHeight()-scaledH)/2;
         
         if (viewBounds == null) viewBounds = new Rectangle();
         viewBounds.setBounds(x, y, scaledW, scaledH);
         
         g.drawImage(buffer, x, y, scaledW, scaledH, this);
     }
 
     @Override
     public void update(Graphics g) {
         paint(g);
     }
 
     protected void updateGame() {
         game.tick();
         repaint();
     }
 
     @Override
     public void start() {
         super.start();
        timer = new Timer(true);
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                updateGame();
            }
        };
         timer.schedule(updateTask, 0, UPDATE_PERIOD);
     }
 
     @Override
     public void stop() {
         timer.cancel();
         timer.purge();
        timer = null;
         super.stop();
     }
 
 }
