 package MainPackage;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.BufferedImage;
 import java.util.*;
 
 /**
  * @author Michael Kieburtz
  * @author Davis Freeman
  */
 public class Renderer {
 
     private ArrayList<String> fontPaths = new ArrayList<String>();
     private ArrayList<Float> fontSizes = new ArrayList<Float>();
     private ArrayList<String> imagePaths = new ArrayList<String>();
     private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
     private Font fpsFont;
     private MediaLoader loader;
     private final int FPSLABEL = 0;
 
     public Renderer() {
 
         loader = new MediaLoader();
         fontSizes.add(36f);
         fontPaths.add("src/resources/BlackHoleBB_ital.ttf");
 
         imagePaths.add("src/resources/FPSbackground.png");
         images = loader.loadImages(imagePaths);
 
         fpsFont = loader.loadFonts(fontPaths, fontSizes).get(0);
     }
 
     public void drawScreen(Graphics g, ArrayList<Ship> ships, double xRot, double yRot, int fps,
             ArrayList<StarChunk> stars, Camera camera, ArrayList<Shot> shots, String version) {
 
         BufferedImage bufferedImage = new BufferedImage(camera.getSize().x, camera.getSize().y, BufferedImage.TYPE_INT_ARGB);
 
         Graphics2D g2d = bufferedImage.createGraphics(); // turns it into 2d graphics
 
         // draw backround rectangle
         g2d.setColor(Color.BLACK);
 
         g2d.fillRect(0, 0, camera.getSize().x, camera.getSize().y);
 
         // enable anti-aliasing
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 
         // draw stars
         for (StarChunk starChunk : stars) {
             if (camera.insideView(starChunk.getBoundingRect())) {
                 starChunk.draw(g2d, camera.getLocation());
             }
         }
         
         // draw fps info
         g2d.drawImage(images.get(FPSLABEL), null, 0, 0);
         g2d.setFont(fpsFont.deriveFont(32f));
         g2d.setColor(Color.CYAN);
 
         g2d.drawString(String.valueOf(fps), 155, 33);
 
         g2d.setFont(fpsFont.deriveFont(60f));
 
         g2d.drawString("FPS:", 10, 50);
 
         // draw version info'
         g2d.setFont(new Font("Arial", Font.TRUETYPE_FONT, 12));
         g2d.setColor(Color.WHITE);
         g2d.drawString("Version: " + version, camera.getSize().x - 130, 10);
 
         // move and draw the bullets
         for (Shot shot : shots) {
             if (shot.imagesLoaded()) {
                 shot.draw(g2d, camera.getLocation());
             }
         }
         
         //draw player health
        g2d.drawString(String.valueOf(Player.returnHealth()), 200, 200);
 
         // draw the minimap
         g2d.setColor(Color.BLACK);
         g2d.fillRect(camera.getSize().x - 201, camera.getSize().y - 225, 200, 200);
 
         g2d.setColor(new Color(0, 255, 0, 50));
         g2d.fillRect(camera.getSize().x - 201, camera.getSize().y - 225, 200, 200);
 
         g2d.setColor(Color.GREEN);
         g2d.drawRect(camera.getSize().x - 201, camera.getSize().y - 225, 200, 200);
 
         for (Ship ship : ships) {
 
             if (ship instanceof Player) {
                 g2d.setColor(Color.CYAN);
             } else if (ship instanceof EnemyShip) {
                 g2d.setColor(Color.RED);
             } else {
                 g2d.setColor(Color.YELLOW);
             }
 
             Ellipse2D.Double minimapShip = new Ellipse2D.Double(camera.getSize().x - 201 + 100 + ship.getLocation().x / 100,
                     camera.getSize().y - 225 + 100 + ship.getLocation().y / 100, 1, 1);
             g2d.draw(minimapShip);
         }
 
         // draw the player and enemies
         for (Ship ship : ships) {
             ship.draw(g2d, camera.getLocation());
         }
 
         g.drawImage(bufferedImage, 0, 0, null);
 
         g2d.dispose();
         g.dispose();
     }
 
     public void drawLauncher(Graphics g, BufferedImage startButtonImage, BufferedImage backgroundImage) {
         BufferedImage bufferedImage = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = bufferedImage.createGraphics();
 
         g2d.drawImage(backgroundImage, 110, 0, null);
         g2d.drawImage(startButtonImage, 450, 450, null);
         g.drawImage(bufferedImage, 0, 0, null);
         g2d.dispose();
         g.dispose();
     }
 
     public void drawLoadingScreen(Graphics g, int percentDone, int width, int height) {
 
         BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 
         Graphics2D g2d = bufferedImage.createGraphics();
 
         // enable anti-aliasing
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         g2d.setColor(Color.BLACK);
         g2d.fillRect(0, 0, width, height);
 
         g2d.setColor(Color.BLUE);
         g2d.drawRect((width / 2) - 200, (height / 2) - 50, 400, 10);
         g2d.setColor(Color.CYAN);
         g2d.fillRect((width / 2) - 200, (height / 2) - 50, percentDone * 4, 10);
 
         g2d.setFont(fpsFont);
         g2d.setColor(new Color(0x00CECE)); // hex codes rock
         g2d.drawString("Loading...", width / 2 - 75, height / 2 - 75);
 
         g.drawImage(bufferedImage, 0, 0, null);
 
         g2d.dispose();
         g.dispose();
     }
 }
