 package MainPackage;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.geom.*;
 import java.awt.image.BufferedImage;
 import java.text.DecimalFormat;
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
     private final int HEALTHLABEL = 0;
     private final int PAUSEMENU = 1;
     private final int PAUSETOMENU = 2;
 
     public Renderer() {
 
         loader = new MediaLoader();
         fontSizes.add(36f);
         fontPaths.add("src/resources/BlackHoleBB_ital.ttf");
 
         imagePaths.add("src/resources/healthbackground.png");
         imagePaths.add("src/resources/PauseMenu.png");
         imagePaths.add("src/resources/PauseButton_ToMenu.png");
         images = loader.loadImages(imagePaths);
 
         fpsFont = loader.loadFonts(fontPaths, fontSizes).get(0);
     }
 
     public void drawScreen(Graphics g, ArrayList<Ship> ships, double xRot, double yRot, int fps,
             ArrayList<StarChunk> stars, Camera camera, ArrayList<Shot> shots, String version, int ups, boolean paused) {
 
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
         // draw the player and enemies
         for (Ship ship : ships) {
             ship.draw(g2d, camera.getLocation());
         }
         
         // draw fps info and other stats
         g2d.setFont(new Font("Arial", Font.TRUETYPE_FONT, 12));
         g2d.setColor(Color.WHITE);
         
         //version
         g2d.drawString("Version: " + version, camera.getSize().x - 130, 10);
         //fps
         g2d.drawString("FPS: " + String.valueOf(fps), camera.getSize().x - 130, 20);
         //ups
         g2d.drawString("UPS: " + String.valueOf(ups), camera.getSize().x - 130, 30);
         //player game location
         g2d.drawString("Player Location: " + convertPointToOrderedPair(ships.get(0).getLocation()),
                 camera.getSize().x - 180, 40);
         
         g2d.drawString("Shots: " + shots.size(), camera.getSize().x - 130, 50);
         // move and draw the bullets
         for (Shot shot : shots) {
             if (shot.imagesLoaded()) {
                 shot.draw(g2d, camera.getLocation());
             }
         }
         
         //draw player health (shield and hull)
         g2d.drawImage(images.get(HEALTHLABEL), null, 0, 0);
         g2d.setFont(fpsFont.deriveFont(15f));
         g2d.setColor(Color.CYAN);
         // first we have to find the player
         for (Ship ship : ships)
         {
             if (ship instanceof Player)
             {
                 g2d.drawString("Shield Integrity: " + ship.getShieldHealth() + "%", 10, 19);
                 g2d.drawString("Hull Integrity: " + ship.getHullHealth() + "%", 10, 35);
             }
         }
 
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
         
         //draw pause menu
        if (paused)
         {
             g2d.drawImage(images.get(PAUSEMENU), null, 10, 100);
             g2d.drawImage(images.get(PAUSETOMENU),null, 20, 110);
         }
         
         g.drawImage(bufferedImage, 0, 0, null);
 
         g2d.dispose();
         g.dispose();
     }
 
     public void drawLauncher(Graphics g, BufferedImage startButtonImage, BufferedImage backgroundImage,
             BufferedImage exitButtonImage, Dimension screensize) {
         BufferedImage bufferedImage = new BufferedImage(screensize.width, screensize.height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = bufferedImage.createGraphics();
         
         g2d.drawImage(backgroundImage, screensize.width - screensize.width / 2 - backgroundImage.getWidth() / 2,
                 screensize.height - screensize.height / 2 - backgroundImage.getHeight() / 2 - 100, null);
         
         g2d.drawImage(startButtonImage, screensize.width - screensize.width / 2 - startButtonImage.getWidth() / 2,
                 screensize.height - screensize.height / 2 - startButtonImage.getWidth() / 4, null);
         
         g2d.drawImage(exitButtonImage, screensize.width - screensize.width / 2 - exitButtonImage.getWidth() * 4,
                 screensize.height - screensize.height / 2 - exitButtonImage.getHeight() / 2, null);
         
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
     
     private String convertPointToOrderedPair(Point2D.Double point)
     {
         DecimalFormat format = new DecimalFormat("0.#");
         return "(" + format.format(point.x) + ", " + format.format(point.y) + ")";
     }
 }
