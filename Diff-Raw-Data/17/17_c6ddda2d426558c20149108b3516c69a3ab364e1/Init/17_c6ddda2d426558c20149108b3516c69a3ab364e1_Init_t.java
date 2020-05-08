 package fls.engine.main;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import fls.engine.main.art.Sprites;
 import fls.engine.main.input.Input;
 
 @SuppressWarnings("serial")
 public class Init extends Canvas implements Runnable {
 
     public static int width = 400;
     public static int height = width / 12 * 9;
     public Input input;
     private boolean running = false;
     private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
     public BufferedImage icon;
     public Thread thread;
     public String title;
     private boolean started = false;
     private boolean showFPS = false;
     private int ticks = 0;
     public int exframes;
    public final String version = "0.3";
 
     public Init() {
         setTitle("Use setTitle('name')");
         setSize(width, height);
         setVisible(true);
         addKeyListener(input);
     }
 
     public void run() {
         requestFocus();
         long lastTime = System.nanoTime();
         double nsPerTick = 1000000000D / 60D;
 
         int ticks = 0;
         int frames = 0;
         long lastTimer = System.currentTimeMillis();
         double delta = 0;
 
         System.out.println("Made with [2D Engine]");
 
         while (running) {
             long now = System.nanoTime();
             delta += (now - lastTime) / nsPerTick;
             lastTime = now;
             boolean shouldRender = true;
 
             while (delta >= 1) {
                 ticks++;
                 initTick();
                 delta -= 1;
                 shouldRender = true;
             }
 
             try {
                 Thread.sleep(2);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             if (shouldRender) {
                 frames++;
                 initRender();
             }
             if (System.currentTimeMillis() - lastTimer >= 1000) {
                 lastTimer += 1000;
                 exframes = frames;
                 if (showFPS) {
                     System.out.println("Frames : " + frames + " Ticks : " + ticks);
                 }
                 frames = 0;
                 ticks = 0;
             }
         }
         stop();
     }
 
     private void initTick() {
         tick();
         if (!started)
             ticks++;
     }
 
     public void tick() {
 
     }
 
     private void initRender() {
         BufferStrategy bs = getBufferStrategy();
         if (bs == null) {
             createBufferStrategy(3);
             return;
         }
         Graphics g = bs.getDrawGraphics();
         if (started)
             render(g);
         else
             splash(g);
         g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
         g.dispose();
         bs.show();
     }
 
     private void splash(Graphics g) {
         BufferedImage img = null;
         try {
             img = ImageIO.read(Init.class.getResource("/Splash.png"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
         String msg = "Version :" + version;
         Sprites.drawWString(msg, g, (getWidth() / 2) - msg.length() * 2, getHeight() - 75);
         if (ticks > 60 * 4) {
             g.clearRect(0, 0, getWidth(), getHeight());
             started = true;
             ticks = 0;
         }
     }
 
     public void render(Graphics g) {
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, getWidth(), getHeight());
         String msg = "You haven't used render yet";
         Sprites.drawWString(msg, g, width / 2 - msg.length() - 40, height / 2 + 3);
     }
 
     public void start() {
         if (running)
             return;
         thread = new Thread(this, this.title);
         thread.start();
         running = true;
     }
 
     public void stop() {
         if (!running)
             return;
         running = false;
         try {
             thread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         System.out.println("[2D Engine] created by Elliot Lee-Cerrino");
         System.exit(3);
     }
 
     /**
      * 
      * Used to set the title of the frame
      * 
      * @param title
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Used to set the width of the frame the height is also used off this e.g.<br>
      * height = width / 9 * 12
      * 
      * @param width
      */
     public void setWidth(int width) {
         Init.width = width;
     }
 
     public void alterSize(int width, int height) {
            setSize(width, height);
     }
 
     public void alterSize(int width) {
         this.setSize(width, width / 12 * 9);
     }
 
     /**
      * Sets the icon of the frame
      * 
      * @param img
      */
     public void setIcon(BufferedImage img) {
         this.icon = img;
     }
 
     /**
      * 
      * use if you want the FPS to be outputed to the System.out stream
      * 
      * @param show
      */
     public void showFPS(boolean show) {
         showFPS = show;
     }
 
     /**
      * can be used to set the current FPS to a variable
      * 
      * @return
      */
     public int getFPS() {
         return exframes;
     }
 
     public static void main(String[] args) {
         Init game = new Init();
         JFrame frame = new JFrame(game.title);
         frame.add(game);
         frame.pack();
         frame.setVisible(true);
         frame.setDefaultCloseOperation(3);
         frame.setLocationRelativeTo(null);
         frame.setIconImage(game.image);
         game.start();
     }
 }
