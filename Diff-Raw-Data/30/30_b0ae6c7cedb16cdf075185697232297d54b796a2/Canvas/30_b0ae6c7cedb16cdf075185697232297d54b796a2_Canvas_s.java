 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 public class Canvas extends JPanel {
     private static final long serialVersionUID = 4907612881980276015L;
     private Drawable element;
     // pozicia
     double offX, offY;
     double zoom;
 
     public Canvas(Drawable element) {
         super();
         this.element = element;
         offX = offY = 0;
         zoom = 1.0;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         paintComponent((Graphics2D) g);
     }
 
     protected void paintComponent(Graphics2D g) {
         super.paintComponent(g);
         g.setColor(new Color(255, 255, 255));
         g.fillRect(0, 0, getWidth(), getHeight());
         g.setColor(new Color(0, 0, 0));
         g.drawRect(0, 0, getWidth() - 1, getWidth() - 1);
 
         // TODO bug report, pri starte je canvas nejak divne posunuty netusim preco
         // TODO bug report, ked sa odzmaze vrchol a spusti sa to, tak si to mysli, ze tam vrchol stale je
 
         g.setTransform(new AffineTransform(zoom, 0, 0, zoom, offX, offY));
         /*System.out.println("off " + element.toString() + " " + offX + " " + offY + " "
                 + getLocation().y);*/
 
         if (element != null) {
             element.draw(g);
         }
     }
 
     void repaintBetween(double x1, double y1, double x2, double y2) {
         if (x2 < x1) {
             double x = x1;
             x1 = x2;
             x2 = x;
         }
         if (y2 < y1) {
             double y = y1;
             y1 = y2;
             y2 = y;
         }
         repaint((int) (offX + x1 * zoom) - 1, (int) (offY + y1 * zoom) - 1,
                 (int) ((x2 - x1) * zoom) + 1, (int) ((y2 - y1) * zoom) + 1);
     }
 
     static void realDrawRect(Graphics g, double x, double y, double w, double h) {
         g.drawRect((int) (x), (int) (y), (int) (x + w) - (int) (x), (int) (y + h) - (int) (y));
     }
 
     static void realFillRect(Graphics g, double x, double y, double w, double h) {
         g.fillRect((int) (x), (int) (y), (int) (x + w) - (int) (x), (int) (y + h) - (int) (y));
     }
 
     public static Color contrastColor(Color color, Constrast type) {
         if (type == Constrast.invert) {
             return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
         }
         if (type == Constrast.textbw) {
             // nasledovne konstanty nie su nahodne, neodporucam menit :)
             int value = color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114;
             if (value >= 128000)
                 return Color.black;
             else
                 return Color.white;
         }
         if (type == Constrast.borderbw) {
             // tu sa da s konstantami trochu pohrat
             int value = color.getRed() + color.getGreen() + color.getBlue() + 2;
             if (value > 200)
                 return Color.black;
             else
                 return Color.blue;
         }
 
         return Color.black;
     }
 
     public static String shorten(Graphics g, String text, int width, Preference preference) {
         if (g.getFontMetrics().stringWidth(text) <= width)
             return text;
         if (preference == Preference.begin) {
             if (g.getFontMetrics().stringWidth("..") > width)
                 return "";
             int len = 0;
             while (g.getFontMetrics().stringWidth(text.substring(0, len + 1) + "..") <= width)
                 len++;
             return text.substring(0, len) + "..";
         }
         if (preference == Preference.end) {
             if (g.getFontMetrics().stringWidth("..") > width)
                 return "";
             int len = 0;
             while (g.getFontMetrics().stringWidth(".." + text.substring(text.length() - len)) <= width)
                 len++;
             return ".." + text.substring(text.length() - len);
         }
         return "";
     }
 
     public static String[] shortenWrap(Graphics g, String text, int width, String regex) {
         if (g.getFontMetrics().stringWidth(text) <= width) {
             String[] line = new String[1];
             line[0] = text.trim();
             return line;
         }
         String[] lines = text.split(regex);
         for (int i = 0; i < lines.length; ++i)
             lines[i] = shorten(g, lines[i].trim(), width, Preference.begin);
         return lines;
     }
 
     public static String[] multiGet(Graphics g, String text, int width) {
         ArrayList<String> words = new ArrayList<String>();
         int from = 0, to;
         while ((from = text.indexOf("{", from)) != -1) {
             from++;
             to = text.indexOf("}", from);
             if (to != -1)
                 words.add(shorten(g, text.substring(from, to), width, Preference.begin));
         }
         return (String[]) words.toArray(new String[words.size()]);
     }
 }
