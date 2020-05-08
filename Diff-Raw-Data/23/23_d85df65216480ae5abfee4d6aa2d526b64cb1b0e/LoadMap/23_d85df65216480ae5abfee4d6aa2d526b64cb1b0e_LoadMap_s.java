 package tribes2d.map;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 /**
  *
  *
  */
 public class LoadMap {
 
     public LoadMap(Graphics g, File mapFile, JPanel parent) throws IOException {
        BufferedImage level = new BufferedImage(128*16, 128*16, BufferedImage.TYPE_INT_ARGB_PRE);
         BufferedImage levelElement = null;
         //Temporary for now
         int[][] tempTestMap = generateTestMap();
         levelElement = ImageIO.read(getClass().getClassLoader().getResource("LevelDesign/grass001.png"));
 
         System.out.println("begin loading...");
         for (int i = 0; i < 128; i++) {
             System.out.println(Math.round(i/10)+"%");
             for (int ii = 0; ii < 128; ii++) {
 
                 switch (tempTestMap[i][ii]) {
                     case -1:
                         level.createGraphics().drawImage(levelElement, null, i*16, ii*16);
                         break;
                     default:
                         break;
                 }
 
             }
         }
         System.out.println("loading successfull...");
         paintComponent(g, level);
         
     }
 
    public void paintComponent(Graphics grafica, BufferedImage image) {
         grafica.drawImage(image, 0, 0, null); //no need for ImageObserver here
     }

     private int[][] generateTestMap() {
         int[][] testMap = new int[128][128];
         
         for (int i = 0; i < 128; i++) {
             for (int ii = 0; ii < 128; ii++) {
                 if (i == 0 || ii == 0 || i==128 || ii==128) {
                     testMap[i][ii] = -1;
                 } else {
                     testMap[i][ii] = 0;
                 }
             }
         }
         return testMap;
     }
 }
