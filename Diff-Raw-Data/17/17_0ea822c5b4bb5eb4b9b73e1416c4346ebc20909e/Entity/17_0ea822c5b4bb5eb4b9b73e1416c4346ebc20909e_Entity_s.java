 
 import java.awt.Rectangle;
 import javax.swing.ImageIcon;
 
 
public abstract class Entity{
    
     ImageIcon image;
     int x, y, width, height;
     
    public Entity(String path, int xc, int yc)
     {
        image = new ImageIcon(path);
         x = xc;
         y = yc;
         init();
     }
     
     public boolean isTouching(int xc, int yc, int w, int h)
     {
         return(x + width >= xc && x < xc + w 
                 && y + height > yc && y < yc + h);
     }
     
     public boolean isTouching(Rectangle rect)
     {
         return(x + width >= rect.x && x < rect.x + rect.width 
                 && y + height > rect.y && y < rect.y + rect.height);
     }
     
     public abstract void init();
     public abstract void update();
 }
