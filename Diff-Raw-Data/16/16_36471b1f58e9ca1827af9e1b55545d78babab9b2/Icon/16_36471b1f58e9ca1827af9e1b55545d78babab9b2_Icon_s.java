 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 
 public class Icon extends JComponent {
     private static final Color bgColor = new Color(0x111111);
     private static final Color fgColor = new Color(0xffffff);
 
     private static final Image overlayEating   = Util.loadImage("img/overlays/eating.png");
     private static final Image overlayMating   = Util.loadImage("img/overlays/mating.png");
     private static final Image overlaySleeping = Util.loadImage("img/overlays/sleeping.png");
 
     private static final Image imageMale   = Util.loadImage("img/sprites/m.png");
     private static final Image imageFemale = Util.loadImage("img/sprites/f.png");
 
     private int fontSize;
 
     private ImageFilter filter;
 
     private Image untaintedImage;
     private Image image;
     private Image overlay;
     private Color color;
     private Font  font;
     private int   age;
 
     private final int width;
     private final int height;
     private final Dimension dimension;
 
     public Icon(int gender) {
         width  = 64;
         height = 64;
 
         fontSize = 14;
         font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
 
         age = 0;
 
         color  = Color.RED;
         filter = new ColorizeFilter(color);
 
         dimension = new Dimension(width, height);
 
         untaintedImage =
             gender == Const.MALE?   imageMale:
             gender == Const.FEMALE? imageFemale:
             null;
         image = untaintedImage;
         image = filterImage(untaintedImage);
 
         overlay = overlayEating;
     }
 
     public void happyBirthday() {
         age++;
     }
 
     public void colorize(Color newColor) {
         color  = newColor;
         filter = new ColorizeFilter(color);
         image  = filterImage(untaintedImage);
     }
 
     private Image filterImage(Image image) {
         return createImage(new FilteredImageSource(image.getSource(), filter));
     }
 
     public Dimension getPreferredSize() { return dimension; }
 
     public void paintComponent(Graphics g) {
         final int w = getWidth();
         final int h = getHeight();
 
        final int imageX = (int) (0.25 * w);
        final int imageY = (int) (0.25 * h);
        final int imageW = w - imageX;
        final int imageH = h - imageY;
 
        final int overlayW = w - imageW;
        final int overlayH = h - imageH;
         final int overlayX = 0;
         final int overlayY = h - overlayH;
 
         g.setColor(bgColor);
         g.fillRect(0, 0, w, h);
 
         g.drawImage(image,     imageX,   imageY,   imageW,   imageH, null);
         g.drawImage(overlay, overlayX, overlayY, overlayW, overlayH, null);
 
         g.setColor(fgColor);
         g.setFont(font);
         g.drawString("" + age, 0, fontSize);
     }
 }
