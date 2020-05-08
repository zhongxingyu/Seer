 package gamed.client.SpeedRisk;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 public class Kamchat extends Country
 {
     private int x2;
     private int y2;
     public Image overlay2;
     private BufferedImage img2;
     private Rectangle bounds2;
 
     public Kamchat(int img_x, int img_y, int label_x, int label_y)
     {
         super(36, null, img_x, img_y, label_x, label_y);
         x2 = 0;
         y2 = 57;
     }
 
     @Override
     public void paint(Graphics g)
     {
         g.drawImage(img, x, y, null);
         g.drawImage(img2, x2, y2, null);
     }
 
     @Override
     public void setSelected(boolean selected)
     {
         isSelected = selected;
         if (img == null)
             return;
 
         if (isSelected)
         {
             colorSelectedImage(img);
             colorSelectedImage(img2);
         }
         else
         {
            owner.renderer.renderCountry(img, x, y);
            owner.renderer.renderCountry(img2, x2, y2);
         }
     }
 
     @Override
     public boolean contains(java.awt.Point p)
     {
         if (super.contains(p))
             return true;
         if (bounds2 != null && bounds2.contains(p))
         {
             int ix = p.x - x2;
             int iy = p.y - y2;
             int color = img2.getRGB(ix, iy);
             if ((color & 0xff000000) != 0)
             {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public Iterable<String> getMediaRequests()
     {
         ArrayList images = new ArrayList(2);
         images.add("resources/ClassicRisk/c36.png");
         images.add("resources/ClassicRisk/c36b.png");
         return images;
     }
 
     @Override
     public void mediaCompleted(String request, Image img)
     {
         if (request.contains("c36b"))
         {
             overlay2 = img;
             img2 = makeBufferedImage(overlay2);
             bounds2 = new Rectangle(x2, y2, img2.getWidth(), img2.getHeight());
         }
         else
         {
             super.mediaCompleted(request, img);
         }
     }
 }
