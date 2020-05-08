 package demo.client.local.game.tools;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import demo.client.local.game.gui.BoardCanvas;
 import demo.client.shared.game.model.BoardModel;
 
 /**
  * An object containing dimensions of components in Block Drop, including {@link Square squares},
  * {@link BoardCanvas board canvases}, and {@link BoardModel board models}.
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 public class Size {
 
   /**
    * The enumerated size categories, used to retrieve an appropriate {@link Size size} instance from
    * {@link Size#getSize(SizeCategory) getSize}.
    * 
    * @author mbarkley <mbarkley@redhat.com>
    * 
    */
   public enum SizeCategory {
     /**
      * The main canvases size category.
      */
     MAIN,
     /**
      * The upcoming piece display canvases size category.
      */
     NEXT,
     /**
      * The opponent canvas size category.
      */
     OPPONENT
   }
 
   /** The height of the board in squares. */
   public static final int HEIGHT = 15;
   /** The width of the board in squares. */
   public static final int WIDTH = 10;
   // Height / Width
   public static final double RATIO = 1.5;
 
   /** Canvas coordinate-space height (in pixels). */
   private static int MAIN_COORD_HEIGHT = 780;
   /** Canvas coordinate-space width (in pixels). */
   private static int MAIN_COORD_WIDTH = 520;
   /** The width and height of each square in this block (in pixels). */
   private static double MAIN_BLOCK_SIZE = MAIN_COORD_HEIGHT / HEIGHT;
   /** The height of the opponent canvas (in pixels). */
   private static int OPP_COORD_HEIGHT = 390;
   /** The width of the opponent canvas (in pixels). */
   private static int OPP_COORD_WIDTH = 260;
   /** The height of the upcoming piece canvas (in pixels). */
   private static int NEXT_COORD_HEIGHT = (int) (MAIN_BLOCK_SIZE * 5);
   /** The width of the upcoming piece canvas (in pixels). */
   private static int NEXT_COORD_WIDTH = (int) (MAIN_BLOCK_SIZE * 5);
   private static int NEXT_BLOCK_SIZE = (int) MAIN_BLOCK_SIZE;
 
   private static Map<SizeCategory, Size> cachedSizes = new HashMap<SizeCategory, Size>();
 
   // Perform sanity checks to make sure size values have been set reasonably.
   @SuppressWarnings(value = { "all" })
   private static void sanityCheck() {
     // Dimensions in tiles divide dimensions in pixels.
     assert MAIN_COORD_HEIGHT % HEIGHT == 0 && MAIN_COORD_WIDTH % WIDTH == 0;
     // Block size should divide height and width HEIGHT and WIDTH times, respectively
     assert MAIN_COORD_HEIGHT / HEIGHT == MAIN_BLOCK_SIZE && MAIN_COORD_WIDTH / WIDTH == MAIN_BLOCK_SIZE;
   }
 
   /**
    * Get a Size instance with constant size values of the appropriate values for the specified
    * category.
    * 
    * @param category
    *          Used to determine the constant values assigned to the returned Size instance.
    * @return A Size instance.
    */
   public static Size getSize(SizeCategory category) {
     if (!cachedSizes.containsKey(category)) {
       cachedSizes.put(category, new Size(category));
     }
     return cachedSizes.get(category);
   }
 
   public static void reset(Map<SizeCategory, Integer[]> dimensionMap) {
     for (Entry<SizeCategory, Integer[]> entry : dimensionMap.entrySet()) {
       Size size = getSize(entry.getKey());
       size.coordHeight = entry.getValue()[0];
       size.coordWidth = entry.getValue()[1];
       if (entry.getKey().equals(SizeCategory.NEXT)) {
         size.blockSize = size.coordWidth / 4;
       }
       else {
        size.blockSize = size.coordHeight / HEIGHT;
       }
     }
   }
 
   /**
    * The height of a canvas of this instance's {@link Size.SizeCategory size category}.
    */
   private int coordHeight;
   /**
    * The width of a canvas of this instance's {@link Size.SizeCategory size category}.
    */
   private int coordWidth;
   /**
    * The width of a block of this instance's {@link Size.SizeCategory size category}.
    */
   private double blockSize;
 
   private Size(SizeCategory category) {
     switch (category) {
     case MAIN:
       coordHeight = MAIN_COORD_HEIGHT;
       coordWidth = MAIN_COORD_WIDTH;
       blockSize = MAIN_BLOCK_SIZE;
       break;
     case NEXT:
       coordHeight = NEXT_COORD_HEIGHT;
       coordWidth = NEXT_COORD_WIDTH;
       blockSize = NEXT_BLOCK_SIZE;
       break;
     case OPPONENT:
       coordHeight = OPP_COORD_HEIGHT;
       coordWidth = OPP_COORD_WIDTH;
       blockSize = OPP_COORD_HEIGHT / HEIGHT;
       break;
     default:
       coordHeight = 0;
       coordWidth = 0;
       blockSize = 0;
       break;
     }
   }
 
   /**
    * Get the coordinate height of a canvas of this instance's {@link Size.SizeCategory size
    * category}.
    * 
    * @return The coordinate height of a canvas of this instance's {@link Size.SizeCategory size
    *         category}.
    */
   public int getCoordHeight() {
     return coordHeight;
   }
 
   /**
    * Get the coordinate width of a canvas of this instance's {@link Size.SizeCategory size category}
    * .
    * 
    * @return The coordinate width of a canvas of this instance's {@link Size.SizeCategory size
    *         category}.
    */
   public int getCoordWidth() {
     return coordWidth;
   }
 
   /**
    * Get the width of a block of this instance's {@link Size.SizeCategory size category}.
    * 
    * @return The width of a block of this instance's {@link Size.SizeCategory size category}.
    */
   public double getBlockSize() {
     return blockSize;
   }
 
 }
