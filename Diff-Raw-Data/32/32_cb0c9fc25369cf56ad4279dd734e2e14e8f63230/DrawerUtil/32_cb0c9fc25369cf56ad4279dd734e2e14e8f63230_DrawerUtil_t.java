 package net.stuffrepos.tactics16.util.image;
 
import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class DrawerUtil {
 
    /**
     * Copied from java.awt.Graphics2D.fill3dRect.
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     * @param raised 
     */
    public static void fill3dRect(Graphics g, int x, int y, int width, int height, boolean raised) {
        Color c = g.getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();

        if (!raised) {
            g.setColor(darker);
        }

        g.fillRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(raised ? brighter : darker);
        g.fillRect(x, y, 1, height);
        g.fillRect(x + 1, y, width - 2, 1);
        g.setColor(raised ? darker : brighter);
        g.fillRect(x + 1, y + height - 1, width - 1, 1);
        g.fillRect(x + width - 1, y, 1, height - 1);

        g.setColor(c);
     }
     
 }
