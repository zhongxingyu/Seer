 package values;
 
 import basic.Layout;
 
 import javax.swing.ImageIcon;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.PixelGrabber;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * Class generates and stores color for brick.
  * 
  * @author X-Stranger
  */
 public class BrickColor {
     
     /** Brick colors object. */
     private Map<Orientation, ImageIcon> colors;
     /** Brick colors index. */
     private int index = -1;
 
     /** Brick colors. */
     private static final int GRAY_COLOR = 0x828282;
     private static final int[] RGB_COLORS = {0x0000ff, 0xee0000, 0xffba00, 0xfff326, 0x00bb00,
                                             0x00eeee, 0xe400ff, 0xffb3d0, 0xffffff, 0xffb302, GRAY_COLOR}; 
 
     /** Special bricks total count. */
     public static final int SPECIAL_TOTAL = 5;
     /** "Universal color" brick id. */
     public static final int SPECIAL_UNIVERSAL = Layout.FIELD;
     /** "Bomb" brick id. */
     public static final int SPECIAL_BOMB      = Layout.FIELD + 1;
     /** "Lightning" brick id. */
     public static final int SPECIAL_LIGHTNING = Layout.FIELD + 2;
     /** "Arrow changer" brick id. */
     public static final int SPECIAL_ARROWS    = Layout.FIELD + 3;
     /** "Color changer" brick id. */
     public static final int SPECIAL_COLORS    = Layout.FIELD + 4;
 
     private static final ImageIcon BLACK_IMAGE = new ImageIcon(ClassLoader.getSystemResource("images/black.png"));
     private static final ImageIcon GRAY_IMAGE  = new ImageIcon(ClassLoader.getSystemResource("images/gray.png"));
 
     /** Static value. */
     public static final BrickColor BLACK = new BrickColor() {
 
         public ImageIcon getColor(Orientation orientation) {
             return BLACK_IMAGE;
         }
 
         public ImageIcon getColor() {
             return BLACK_IMAGE;
         }
     }; 
 
     /** Static value. */
     public static final BrickColor GRAY = new BrickColor() {
 
         public ImageIcon getColor(Orientation orientation) {
             return GRAY_IMAGE;
         }
 
         public ImageIcon getColor() {
             return GRAY_IMAGE;
         }
     }; 
 
     /** Possible colors array. */
     private static final List<Map<Orientation, ImageIcon>> COLORS = new ArrayList<Map<Orientation, ImageIcon>>();
 
     /**
      * Initializes Brick Colors.
      *
      * @param index - bricks theme index value
      */
     public static void init(Integer index) {
         COLORS.clear();
         ImageIcon mask = new ImageIcon(ClassLoader.getSystemResource("images/mask" + index + ".png"));
         ImageIcon upImage = new ImageIcon(ClassLoader.getSystemResource("images/d_up" + index + ".png"));
         ImageIcon downImage = new ImageIcon(ClassLoader.getSystemResource("images/d_down" + index + ".png"));
         ImageIcon leftImage = new ImageIcon(ClassLoader.getSystemResource("images/d_left" + index + ".png"));
         ImageIcon rightImage = new ImageIcon(ClassLoader.getSystemResource("images/d_right" + index + ".png"));
 
         GRAY_IMAGE.setImage(new ImageIcon(createBasicImage(Layout.FIELD, mask)).getImage());
 
         ImageIcon icon;
         BufferedImage image;
         Map<Orientation, ImageIcon> map;
 
         for (int i = 0; i < Layout.FIELD; i++) {
             map = new HashMap<Orientation, ImageIcon>();
 
             icon = new ImageIcon(createBasicImage(i, mask));
             map.put(Orientation.NONE, icon);
 
             image = createBasicImage(i, mask);
             image.getGraphics().drawImage(upImage.getImage(), 0, 0, null);
             map.put(Orientation.TOP, new ImageIcon(image));
 
             image = createBasicImage(i, mask);
             image.getGraphics().drawImage(rightImage.getImage(), 0, 0, null);
             map.put(Orientation.RIGHT, new ImageIcon(image));
 
             image = createBasicImage(i, mask);
             image.getGraphics().drawImage(downImage.getImage(), 0, 0, null);
             map.put(Orientation.BOTTOM, new ImageIcon(image));
 
             image = createBasicImage(i, mask);
             image.getGraphics().drawImage(leftImage.getImage(), 0, 0, null);
             map.put(Orientation.LEFT, new ImageIcon(image));
 
             COLORS.add(map);
         }
 
         for (int i = 0; i < SPECIAL_TOTAL; i++) {
             map = new HashMap<Orientation, ImageIcon>();
 
             icon = new ImageIcon(ClassLoader.getSystemResource("images/" + i + ".png"));
             map.put(Orientation.NONE, icon);
 
             image = toBufferedImage(icon.getImage());
             image.getGraphics().drawImage(upImage.getImage(), 0, 0, null);
             map.put(Orientation.TOP, new ImageIcon(image));
 
             image = toBufferedImage(icon.getImage());
             image.getGraphics().drawImage(rightImage.getImage(), 0, 0, null);
             map.put(Orientation.RIGHT, new ImageIcon(image));
 
             image = toBufferedImage(icon.getImage());
             image.getGraphics().drawImage(downImage.getImage(), 0, 0, null);
             map.put(Orientation.BOTTOM, new ImageIcon(image));
 
             image = toBufferedImage(icon.getImage());
             image.getGraphics().drawImage(leftImage.getImage(), 0, 0, null);
             map.put(Orientation.LEFT, new ImageIcon(image));
 
             COLORS.add(map);
         }
     }
 
     /**
      * Method creates new brick image with specified color.
      *
      * @param i - color index
      * @param mask - IconImage containing transparent mask
      * @return BufferedImage object
      */
     private static BufferedImage createBasicImage(int i, ImageIcon mask) {
         int w = BLACK_IMAGE.getIconWidth();
         int h = BLACK_IMAGE.getIconHeight();
         BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
         Graphics gr = image.getGraphics();
         gr.setColor(new Color(RGB_COLORS[i]));
         gr.fillRect(0, 0, w, h);
         gr.drawImage(mask.getImage(), 0, 0, null);
         return image;
     }
 
     /** Random number generator to select color value. */
     private static Random generator = new Random(System.currentTimeMillis());
 
     /**
      * Private constructor.
      */
     private BrickColor() {
         // do nothing
     }
 
     /**
      * Default constructor.
      * 
      * @param index - colors map index to initialize
      */
     public BrickColor(int index) {
         this.colors = COLORS.get(index);
         this.index = index;
     }
 
     /**
      * Constructor that creates block with next color than passed, but less that level value.
      *
      * @param color - BrickColor object
      * @param level - game level
      */
     public BrickColor(BrickColor color, int level) {
         int index = color.getIndex() + 1;
         if (index == level) { index = 0; }
         this.colors = COLORS.get(index);
         this.index = index;
     }
 
     /**
      * Returns color according to orientation.
      * 
      * @param orientation - brick orientation
      * @return brick color (ImageIcon object)
      */
     public ImageIcon getColor(Orientation orientation) {
         return colors.get(orientation);
     }
     
     /**
      * Returns color according to orientation.
      * 
      * @return brick color (ImageIcon object)
      */
     public ImageIcon getColor() {
         return colors.get(Orientation.NONE);
     }
     
     /**
      * Returns color index.
      * 
      * @return int index value or -1 if none
      */
     public int getIndex() {
         return index;
     }
     
     /**
      * Reinitializes brick colors.
      */
     public void reInit() {
         if (index >= 0) {
             colors = COLORS.get(index);
         }
     }
 
     /**
      * Static method which creates a new BrickColor instance.
      * 
      * @param level - max color index
      * @param arcade - boolean game type flag
      * @return new color instance
      */
     public static BrickColor generate(int level, boolean arcade) {
         if (arcade && (generator.nextInt(Layout.FIELD + BrickColor.SPECIAL_TOTAL) == 7)) {
             return new BrickColor(Layout.FIELD + generator.nextInt(BrickColor.SPECIAL_TOTAL));
         }
         return new BrickColor(generator.nextInt(level));
     }
 
     /**
      * Compares object to another object of the same type.
      * 
      * @param brickColor - object to compare to
      * @return boolean value
      */
     public boolean compareTo(BrickColor brickColor) {
         return this.getColor().equals(brickColor.getColor());
     }
 
     /**
      * This method returns true if the specified image has transparent pixels.
      * Source: http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
      *
      * @param image - the Image object to check
      * @return true if image has Alpha-channel
      */
     private static boolean hasAlpha(Image image) {
         // If buffered image, the color model is readily available
         if (image instanceof BufferedImage) {
             BufferedImage bimage = (BufferedImage) image;
             return bimage.getColorModel().hasAlpha();
         }
 
         // Use a pixel grabber to retrieve the image's color model;
         // grabbing a single pixel is usually sufficient
         PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
         try {
             pg.grabPixels();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         // Get the image's color model
         ColorModel cm = pg.getColorModel();
         return cm.hasAlpha();
     }
 
     /**
      * This method returns a buffered image with the contents of an image.
      * Source: http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
      *
      * @param image - the Image object to convert
      * @return new BufferedImage object created from image
      */
     private static BufferedImage toBufferedImage(Image image) {
         if (image instanceof BufferedImage) {
             return (BufferedImage) image;
         }
 
         // This code ensures that all the pixels in the image are loaded
         image = new ImageIcon(image).getImage();
 
         // Determine if the image has transparent pixels; for this method's
         // implementation, see e661 Determining If an Image Has Transparent Pixels
         boolean hasAlpha = hasAlpha(image);
 
         // Create a buffered image with a format that's compatible with the screen
         BufferedImage bimage = null;
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         try {
             // Determine the type of transparency of the new buffered image
             int transparency = Transparency.OPAQUE;
             if (hasAlpha) {
                 transparency = Transparency.BITMASK;
             }
 
             // Create the buffered image
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             GraphicsConfiguration gc = gs.getDefaultConfiguration();
             bimage = gc.createCompatibleImage(
                 image.getWidth(null), image.getHeight(null), transparency);
         } catch (HeadlessException e) {
             e.printStackTrace();
         }
 
         if (bimage == null) {
             // Create a buffered image using the default color model
             int type = BufferedImage.TYPE_INT_RGB;
             if (hasAlpha) {
                 type = BufferedImage.TYPE_INT_ARGB;
             }
             bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
         }
         
         // Copy image to buffered image
         Graphics g = bimage.createGraphics();
 
         // Paint the image onto the buffered image
         g.drawImage(image, 0, 0, null);
         g.dispose();
 
         return bimage;
     }
 }
