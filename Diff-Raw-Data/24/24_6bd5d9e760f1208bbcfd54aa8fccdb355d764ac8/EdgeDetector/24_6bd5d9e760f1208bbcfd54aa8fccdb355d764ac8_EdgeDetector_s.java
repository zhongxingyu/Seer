 package cs437.som.demo;
 
 import cs437.som.TrainableSelfOrganizingMap;
 import cs437.som.util.SOMBuilder;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.security.SecureRandom;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * An image edge detector using a self-organizing map.
  */
 public class EdgeDetector {
     /** The minimum distance between 2 colors to classify as a change in detail */
     private static final int MINIMUM_COLOR_DISTANCE = 60;
 
     /** Better than standard out... */
     private static final Logger log = Logger.getLogger("EdgeDetector");
     private static final int BYTEMASK = 0xFF;
     private static final int TWO_BYTE_SHIFT = 16;
     private static final int ONE_BYTE_SHIFT = 8;
     private static final int FRAME_LOCATION_OFFSET = 400;
 
     /** The ED's SOM */
     private TrainableSelfOrganizingMap som = null;
 
     /** The number of possible 3x3 matrices with each element having 3 possible
      * values.  This is the expected number of iterations for a SOM that will
      * be exhaustively trained.
      */
     public static final int threeRaiseNine = 19683;
 
     /** The number of possible colors that can be stored in 24 bits (2^25 - 1) */
     private static final int possibleColors = 33554431;
 
     /**
      * Create an empty EdgeDetector.
      */
     private EdgeDetector() {
     }
 
     /**
      * Create an edge detector.
      *
      * @param iterations The number of iterations used in training the SOM.
      */
     private EdgeDetector(int iterations) {
         SOMBuilder somb = new SOMBuilder(9, iterations);
         somb.setVisible(true);
         som = somb.getMap();
     }
 
     /**
      * Create an EdgeDetector with a given map and train it exhaustively.
      * 
      * @param map The SOM the EdgeDetector will use.  The EdgeDetector assumes
      * ownership of the SOM.
      * @return An exhaustively trained EdgeDetector.
      */
     public static EdgeDetector trainExhaustivelyFromMap(TrainableSelfOrganizingMap map) {
         EdgeDetector ed = new EdgeDetector();
         ed.som = map;
         ed.trainExhaustively();
         return ed;
     }
 
     /**
      * Create an EdgeDetector with a given map and train it with random samples
      * of possible inputs.
      *
      * @param map The SOM the EdgeDetector will use.  The EdgeDetector assumes
      * ownership of the SOM.
      * @param samples The number of input samples to train with.
      * @return An exhaustively trained EdgeDetector.
      */
     public static EdgeDetector trainRandomlyFromMap(TrainableSelfOrganizingMap map,
                                                     int samples) {
         EdgeDetector ed = new EdgeDetector();
         ed.som = map;
         ed.trainWithRandomPermutations(samples);
         return ed;
     }
 
     /**
      * Train the edge detector's self-organizing map with a random sample of
      * the possible inputs it may see.
      *
      * @param n The number of sample inputs to use for training.
      */
     private void trainWithRandomPermutations(int n) {
         int[][] matrices = generateRandomPermutations(n);
         log.info("Training.");
         for (int[] matrix : matrices) {
             som.trainWith(matrix);
         }
     }
 
     /**
      * Generate a random sampling of input matrices.
      *
      * @param n The number of samples to produce.
      * @return An array (size n) of double[9], each of which represents a 3x3
      * matrix in row major form.
      */
     private int[][] generateRandomPermutations(int n) {
         int[][] permutations = new int[n][9];
         Random r = new SecureRandom();
 
         log.info("Generating " + n + " random matrices.");
         for (int i = 0; i < permutations.length; i++) {
             permutations[i] = new int[] {
                     r.nextInt(3) - 1, r.nextInt(3) - 1, r.nextInt(3) - 1,
                     r.nextInt(3) - 1, r.nextInt(3) - 1, r.nextInt(3) - 1,
                     r.nextInt(3) - 1, r.nextInt(3) - 1, r.nextInt(3) - 1
             };
         }
 
         return permutations;
     }
 
     /**
      * Train the edge detector's self-organizing map with every possible input
      * matrix being shown to it once.
      */
     public void trainExhaustively() {
         int[][] matrices = generateAllPermutations();
         log.info("Training.");
         for (int[] matrix : matrices) {
             som.trainWith(matrix);
         }
     }
 
     /**
      * Generate all possible input matrices.
      *
      * @return An array (size 19683, or 3^9) of double[9], each of which
      * represents a 3x3 matrix in row major form.
      */
 	private int[][] generateAllPermutations() {
         int[] possibleValues  = { -1, 0, 1 };
         int rows = threeRaiseNine;
         int cols = 9;
 
 		int[][] permutations = new int[rows][cols];
 
         for (int i = 0; i < rows; i++) {
             int factor = 1;
             for (int j = 0; j < cols; j++) {
 				permutations[i][j] = possibleValues[i / factor % possibleValues.length];
 				factor *= possibleValues.length;
 			}
 		}
 
 		return permutations;
 	}
 
     /**
      * Detect the edges in an image.
      *
      * @param image The image to process.
      * @return A new image where the pixels correspond to colors assigned to
      * the individual neurons of the self-organizing map.
      */
     public BufferedImage runOnImage(BufferedImage image) {
         int height = image.getHeight();
         int width = image.getWidth();
 
         log.info("Processing " + width + 'x' + height + " image.");
 
         BufferedImage out = new BufferedImage(width, height,
                 BufferedImage.TYPE_INT_RGB);
         int colorStep = possibleColors / som.getNeuronCount();
 
         for (int y = 1; y < height; y++) {
             for (int x = 1; x < width; x++) {
                 int[] differenceMatrix = getDifferenceMatrix(image, x, y);
                 int best = som.getBestMatchingNeuron(differenceMatrix);
                 out.setRGB(x, y, colorStep * best);
             }
         }
 
         return out;
     }
 
     /**
      * Normalize an image produced in a previous processing.
      *
      * The most common color corresponds to the SOM's neuron that responds to a
      * lack of an edge.  Whatever color that neuron was assigned is converted
      * to black to serve as the background.  All other colors are converted to
      * white.
      *
      * This operation is predicated on the idea that several neurons will
      * respond to different features in the image that correspond to parts of
      * an edge, whereas another single neuron will be most closely associated
      * with a lack of an edge.
      *
      * @param image The image to normalize.
      * @return A black and white image where the white pixels indicate an edge
      * in the original image.
      */
     public BufferedImage normalizeImage(BufferedImage image) {
         log.info("Normalizing.");
         int mostCommonColor = findMostCommonColor(image);
 
         log.fine("Rewriting colors.");
         BufferedImage out = new BufferedImage(image.getWidth(),
                 image.getHeight(), image.getType());
         int black = Color.black.getRGB();
         int white = Color.white.getRGB();
         for (int y = 0; y < image.getHeight(); y++) {
             for (int x = 0; x < image.getWidth(); x++) {
                 int color = image.getRGB(x, y);
                 if (color == mostCommonColor)
                     out.setRGB(x, y, black);
                 else
                     out.setRGB(x, y, white);
             }
         }
 
         return out;
     }
 
     /**
      * Compute the most common color in an image.
      *
      * @param image The input image.
      * @return The most common color as a 24-bit RGB value;
      */
     private int findMostCommonColor(BufferedImage image) {
         Map<Integer, Integer> colorFrequency =
                 new HashMap<Integer, Integer>(som.getNeuronCount());
         
         log.fine("Computing color frequencies.");
         for (int y = 0; y < image.getHeight(); y++) {
             for (int x = 0; x < image.getWidth(); x++) {
                 int color = image.getRGB(x, y);
                 if (colorFrequency.containsKey(color))
                     colorFrequency.put(color, colorFrequency.get(color) + 1);
                 else
                     colorFrequency.put(color, 1);
             }
         }
 
         log.fine("Finding most common color.");
         int mostCommonColor = -1;
         int max = 0;
         for (Integer integer : colorFrequency.keySet()) {
             int count = colorFrequency.get(integer);
             if (count > max) {
                 max = count;
                 mostCommonColor = integer;
             }
         }
         return mostCommonColor;
     }
 
     /**
      * Create a "difference" matrix from a point in an image.  The matrix is a
      * 3x3 grid of numbers.  The central number represents the point in
      * question.  The remaining numbers in the matrix are -1 if the point in
      * the image having the same positional relationship as then number in the
      * matrix does not exist (ie., it would be off the image), or 1 if the
      * Euclidean distance of the red, green, and blue values of the color are
      * greater than {@code MINIMUM_COLOR_DISTANCE} from the reference pixel.
      *
      * @param img The image to extract data from.
      * @param x The x-axis coordinate of the point to process.
      * @param y The y-axis coordinate of the point to process.
      * @return The matrix as described above.
      */
     private int[] getDifferenceMatrix(BufferedImage img, int x, int y) {
         int[] matrix = new int[9];
 
         int center = getPixelColor(img, x, y);
 
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 int pixel = getPixelColor(img, x + j - 1, y + i - 1);
                 if (pixel == -1) {
                     matrix[i * 3 + j] = -1;
                 } else {
                     double difference = colorDistance(center, pixel);
                     if (difference < MINIMUM_COLOR_DISTANCE) {
                         matrix[i * 3 + j] = 0;
                     } else {
                         matrix[i * 3 + j] = 1;
                     }
                 }
             }
         }
 
         return matrix;
     }
 
     /**
      * Retrieve the color of a single pixel in an image.
      *
      * @param image The image in question.
      * @param x The x-axis location of the pixel.
      * @param y The y-axis location of the pixel.
      * @return The color, as a 32-bit ARGB value, at (x, y) or -1 if that
      * location would be outside of the boundaries of the image.
      */
     private int getPixelColor(BufferedImage image, int x, int y) {
         if (x < 0 || x > image.getWidth() - 1 || y < 0 || y > image.getHeight() - 1)
             return -1;
         return image.getRGB(x, y);
     }
 
     /**
      * Calculate the Euclidean difference between two colors using their RGB
      * values as elements of a 3-dimensional vector.
      *
      * @param one The first color as a 32-bit ARGB value (the alpha will be
      * ignored).
      * @param two The second color as a 32-bit ARGB value (the alpha will be
      * ignored).
      * @return The distance between one and two.
      */
     private double colorDistance(int one, int two) {
         int r1 = (one >> TWO_BYTE_SHIFT) & BYTEMASK;
         int g1 = (one >> ONE_BYTE_SHIFT) & BYTEMASK;
         int b1 = one & BYTEMASK;
         int r2 = (two >> TWO_BYTE_SHIFT) & BYTEMASK;
         int g2 = (two >> ONE_BYTE_SHIFT) & BYTEMASK;
         int b2 = two & BYTEMASK;
         int dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
         return Math.sqrt(dr * dr + dg * dg + db * db);
     }
 
     @Override
     public String toString() {
         return "EdgeDetector{som=" + som + '}';
     }
 
     public static void main(String[] args) throws IOException {
         EdgeDetector ed = new EdgeDetector(100);
         ed.trainWithRandomPermutations(100);
 
         Class<EdgeDetector> edc = EdgeDetector.class;
         BufferedImage original = ImageIO.read(edc.getResourceAsStream("image.jpg"));
         BufferedImage known = ImageIO.read(edc.getResourceAsStream("known_edges.jpg"));
         BufferedImage detected = ed.runOnImage(original);
 
         writeImage(detected, "out");
 
         BufferedImage normalized = ed.normalizeImage(detected);
         writeImage(normalized, "out_normalized");
 
         // image frame titles
         String[] titles = {"Reference Image", "Processed Image",
                 "Normalized Image"};
         // image frame images
         BufferedImage[] images = {known, detected, normalized};
 
         // Display images
         for (int i = 0; i < titles.length; i++) {
             JFrame f1 = ImageFrame.createInJFrame(titles[i], images[i]);
             f1.setLocation(i * FRAME_LOCATION_OFFSET,0);
             f1.setVisible(true);
         }
     }
 
     /**
      * Write an image to a PNG file.
      *
      * @param image The image to store.
      * @param filename The name of the file to write to (without the ".png"
      * extension).
      */
     public static void writeImage(BufferedImage image, String filename) {
         try {
             ImageIO.write(image, "png", new File(filename + ".png"));
         } catch (IOException e) {
             log.severe("Exception while writing file: " + filename + ".png");
             log.log(Level.SEVERE, "Exception: ", e);
         }
     }
 }
