 package com.ervilsoft.csleveleditor;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 
 public class Art {
 
     public static BufferedImage gui = load("/gui/menu.png", true);
     public static BufferedImage[][] buttons = cut("/gui/menu.png", 128, 24, 0, 47, true);
     
    public static BufferedImage[][] floorTiles = cut("map/floortiles.png", 32, 32);
    public static BufferedImage[][] wallTiles = cut("map/floortiles.png", 32, 56, 0, 104);
    public static BufferedImage[][] treasureTiles = cut("map/treasure.png", 32, 56);
    public static BufferedImage[][] railsTiles = cut("map/rails.png", 32, 38);
    public static BufferedImage shadow = load("map/shadow.png");
     
 
     public static BufferedImage load(String string) {
         return load(string, false);
     }
 
     public static BufferedImage load(String string, boolean isResource) {
         try {
             return getResource(string, isResource);
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Missing resource: " + string, "Error...", JOptionPane.ERROR_MESSAGE);
             System.exit(1);
         }
         return null;
     }
 
     public static BufferedImage[][] cut(String string, int w, int h) {
         return cut(string, w, h, 0, 0, false);
     }
     
     public static BufferedImage[][] cut(String string, int w, int h, boolean isResource) {
         return cut(string, w, h, 0, 0, isResource);
     }
 
     public static BufferedImage[][] cut(String string, int w, int h, int bx, int by) {
         return cut(string, w, h, bx, by, false);
     }
     
     public static BufferedImage[][] cut(String string, int w, int h, int bx, int by, boolean isResource) {
         try {
             
             BufferedImage image = getResource(string, isResource);
 
             int xTiles = (image.getWidth() - bx) / w;
             int yTiles = (image.getHeight() - by) / h;
 
             BufferedImage[][] result = new BufferedImage[xTiles][yTiles];
 
             for (int x = 0; x < xTiles; x++) {
                 for (int y = 0; y < yTiles; y++) {
                     result[x][y] = new BufferedImage(w, h, image.getType());
 
                     Graphics2D g = result[x][y].createGraphics();
                     g.drawImage(image,
                             0, 0,
                             w, h,
                             x * w + bx,
                             y * h + by,
                             x * w + w + bx,
                             y * h + h + by, null);
                     g.dispose();
                 }
             }
 
             return result;
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Missing resource: " + string, "Error...", JOptionPane.ERROR_MESSAGE);
             System.exit(1);
         }
         return null;
     }
     
     public static BufferedImage getResource(String string, boolean isResource) throws IOException {
         if (isResource) {
             return ImageIO.read(LevelEditor.class.getResource(string));
         } else {
            return ImageIO.read(ClassLoader.getSystemResource(string));
         }
     }
 }
