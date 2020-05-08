 package gui;
 
 import character.Entity;
 
 import javax.swing.JPanel;
 import java.awt.Graphics;
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/4/13
  * Time: 3:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ImagePanel extends JPanel {
     private BufferedImage image;
    private double xBlockSize, yBlockSize;
     private List<Entity> entityList;
 
    public ImagePanel(BufferedImage img, double xBlkSize, double yBlkSize) {
         image = img;
         xBlockSize = xBlkSize;
         yBlockSize = yBlkSize;
 
         setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
         entityList = new ArrayList<Entity>();
     }
 
     // Resizes sprite to block size and adds entity to list
     public void addEntity(Entity e) {
        e.sprite = Helper.resizeImage(e.sprite, (int)xBlockSize, (int)yBlockSize);
         entityList.add(e);
     }
 
     @Override
     public void paint(Graphics g) {
         g.drawImage(image, 0, 0, null);
         for(Entity e : entityList) {
            g.drawImage(e.sprite, (int)(e.getX() * xBlockSize), (int)(e.getY() * yBlockSize), null);
         }
     }
 }
