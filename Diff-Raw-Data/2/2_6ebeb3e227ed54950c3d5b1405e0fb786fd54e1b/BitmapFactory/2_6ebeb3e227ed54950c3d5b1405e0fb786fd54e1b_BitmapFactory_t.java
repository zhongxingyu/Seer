 package medievalhero;
 
 import java.awt.image.BufferedImage;
 
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 
 import java.awt.Graphics2D;
 import java.awt.Transparency;
 import java.io.IOException;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import java.util.Hashtable;
 
 public class BitmapFactory {
     
     private String IMAGE_DIR = "data/images/";
     private Board game;
     
     private int tileWidth = 32, tileHeight = 32;
     
     private Hashtable<String, BufferedImage> menu = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> inGameMenu = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> credits = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> tiles = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> hero = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> mobs = new Hashtable<String, BufferedImage>();
     private Hashtable<String, BufferedImage> items = new Hashtable<String, BufferedImage>();
     
     public BitmapFactory(Board board) {
         game = board;
     }
     
     public void loadTestImages() {
         try {
             URL url = BitmapFactory.class.getResource(IMAGE_DIR + "tiles/tiletest.png");
             BufferedImage tmp = ImageIO.read(url);
             BufferedImage tmp2 = null;
             
             tmp2 = clip(tmp, 0, 0, 32, 32);
             tiles.put("testimg", tmp2);
             
             tmp2 = transparant(clip(tmp, 96, 96, 32, 32), 0);
             tiles.put("mapperCursor", tmp2);
             
         } catch (IOException e) {
             System.out.println("Exception loading graphicsfile: " + e);
         }
         
     }
     
     public void loadBasicImages() {
         
     }
     
     public void loadTiles(String src) {
         try {
             URL url = BitmapFactory.class.getResource(IMAGE_DIR + "tiles/" + src);
             BufferedImage tmp = ImageIO.read(url);
             BufferedImage tmp2 = null;
             
             int counterWidth = tmp.getWidth() / 32;
             int counterHeight = tmp.getHeight() / 32;
             
             for (int i = 0; i < counterHeight; i++) {
                 for (int j = 0; j < counterWidth; j++) {
                     tmp2 = clip(tmp, j * 32, i * 32, 32, 32);
                     tiles.put(src.substring(0, src.length() - 4) + "_" + j + "_" + i, tmp2);
                 }
             }
         } catch (IOException e) {
             System.out.println("Exception loading TILES | " + e);
         }
     }
     
     public void loadItems(String src, int width, int height) {
         if(width == 0 || width < 0) width = 32;
         if(height == 0 || height < 0) height = 32;
         
         try {
             URL url = BitmapFactory.class.getResource(IMAGE_DIR + "items/" + src);
             BufferedImage tmp = ImageIO.read(url);
             BufferedImage tmp2 = null;
             
             int counterWidth = tmp.getWidth() / width;
             int counterHeight = tmp.getHeight() / height;
             
             for (int i = 0; i < counterHeight; i++) {
                 for (int j = 0; j < counterWidth; j ++) {
                     tmp2 = transparant(clip(tmp, j * width, i * height, width, height), 0);
                    items.put(src.substring(0, src.length() - 4) + "_" + j + "_" + i, tmp2);
                 }
             }
         } catch (IOException e) {
             System.out.println("Exception loading ITEMS | " + e);
         }
     }
     
     public BufferedImage getMenuImg(String image) {
         return menu.get(image);
     }
     
     public BufferedImage getInGameMenuImg(String image) {
         return inGameMenu.get(image);
     }
     
     public BufferedImage getCreditsImg(String image) {
         return credits.get(image);
     }
     
     public BufferedImage getTileImg(String image) {
         return tiles.get(image);
     }
     
     public BufferedImage getHeroImg(String image) {
         return hero.get(image);
     }
     
     public BufferedImage getMobsImg(String image) {
         return mobs.get(image);
     }
     
     public BufferedImage getItemImg(String image) {
         return items.get(image);
     }
     
     private static BufferedImage transparant(BufferedImage src, int whatColor) {
         BufferedImage work = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
         
         for (int i = 0; i < src.getWidth(); i++) {
             for (int j = 0; j <src.getHeight(); j++) {
                 if (src.getRGB(i, j) == -1) {
                     int argb = work.getRGB(i, j);
                     int oldAlpha = (argb >>> 24);
                     if (oldAlpha == 100) {
                         argb = (80 << 24) | (argb & 0xffffff);
                         work.setRGB(i, j, argb);
                     }
                 } else {
                     work.setRGB(i, j, src.getRGB(i, j));
                 }
             }
         }
         
         return work;
     }
     
     private static BufferedImage clip(BufferedImage src, int x, int y, int w, int h) {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         BufferedImage work = null;
         
         try {
             GraphicsDevice screen = ge.getDefaultScreenDevice();
             GraphicsConfiguration gc = screen.getDefaultConfiguration();
             work = gc.createCompatibleImage(w, h, Transparency.BITMASK);
         } catch (Exception e) {
             System.out.println("Exception in BitmapFactory_Clip = " + e);
         }
         
         if (work == null) {
             work = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         }
         
         int[] pixels = new int[w * h];
         src.getRGB(x, y, w, h, pixels, 0, w);
         work.setRGB(0, 0, w, h, pixels, 0, w);
         
         return work;
     }
     
 }
