 package gamed.client.SpeedRisk;
 
 import gamed.client.MediaRequestor;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 public class Country implements MediaRequestor
 {
     public Image overlay;
     public final int id;
     protected BufferedImage img;
     protected RiskPlayer owner;
     protected int armies;
     protected int x;
     protected int y;
     protected int lx;
     protected int ly;
     protected boolean isSelected;
     protected Rectangle bounds;
     protected Rectangle iconBounds;
     public final String imageFile;
 
     public Country(int id, String imageFile, int img_x, int img_y, int label_x, int label_y)
     {
         this.id = id;
         this.imageFile = imageFile;
         isSelected = false;
         x = img_x;
         y = img_y;
         lx = label_x;
         ly = label_y;
     }
 
     public void setOwner(RiskPlayer o)
     {
         owner = o;
         setSelected(isSelected);
     }
 
     public void set(RiskPlayer owner, int armies)
     {
         this.owner = owner;
         this.armies = armies;
         setSelected(isSelected);
     }
 
     public void set(RiskPlayer owner, int armies, boolean selected)
     {
         this.owner = owner;
         this.armies = armies;
         setSelected(selected);
     }
 
     public void setSelected(boolean selected)
     {
         isSelected = selected;
         if (img == null)
             return;
 
         if (isSelected)
         {
             colorSelectedImage(img);
         }
         else
         {
            if (owner != null)
                owner.renderer.renderCountry(img, x, y);
         }
     }
 
     public boolean contains(java.awt.Point p)
     {
         if (iconBounds == null || bounds == null)
             return false;
 
         if (iconBounds.contains(p))
             return true;
 
         if (bounds.contains(p))
         {
             int ix = p.x - x;
             int iy = p.y - y;
             int color = img.getRGB(ix, iy);
             if ((color & 0xff000000) != 0)
             {
                 return true;
             }
         }
         return false;
     }
 
     public void paint(Graphics g)
     {
         if (img != null)
             g.drawImage(img, x, y, null);
     }
 
     public void paintIcon(Graphics g)
     {
         if (owner != null)
             owner.renderer.renderIcon(g, lx, ly, armies);
     }
 
     protected void colorSelectedImage(BufferedImage image)
     {
         int w = image.getWidth();
         int h = image.getHeight();
         int v;
         for (int i = 0; i < w; i++)
         {
             for (int j = 0; j < h; j++)
             {
                 v = image.getRGB(i, j);
                 image.setRGB(i, j, v & 0xff000000);
             }
         }
     }
 
     protected BufferedImage makeBufferedImage(Image o)
     {
         int w = o.getWidth(null);
         if (w == -1)
         {
             return null;
         }
         BufferedImage i = new BufferedImage(w, o.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g = i.createGraphics();
         g.drawImage(o, 0, 0, null);
         return i;
     }
 
     public Iterable<String> getMediaRequests()
     {
         ArrayList list = new ArrayList(1);
         list.add(imageFile);
         return list;
     }
 
     public void mediaCompleted(String request, Image img)
     {
         this.overlay = img;
         this.img = makeBufferedImage(overlay);
         this.bounds = new Rectangle(x, y, this.img.getWidth(), this.img.getHeight());
         this.iconBounds = new Rectangle(lx, ly, 18, 15);
     }
 }
