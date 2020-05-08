 package modules;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import pipeline.Pipeline;
 import riverObjects.ImageShape;
 
 /**
  * This module detects and saves the ImageShapes
  * of all the non-white blobs in a picture. A 
  * shape is defined here as a group of pixels with
  * exactly the same RGB values.
  */
 public class BlobDetection implements Module {
     private int white = Color.white.getRGB(); // Int representation of white
     private BufferedImage original;
     private List<ImageShape> imageShapes = new ArrayList<ImageShape>();
     
     public BlobDetection(BufferedImage img) {
         original = img;
     }
     
     /**
      * Finds and returns a sorted list (by edge
      * size) of all non-white ImageShapes within the picture.
      * 
      * @return list of image shapes
      */
     public List<ImageShape> findImageShapes() {
         // If image shapes have already been found, no need to re-detect
         if (!imageShapes.isEmpty()) {
             return imageShapes;
         }
         // Else, detect shapes
         else {
             int width = original.getWidth();
             int height = original.getHeight();
 
             // Create a boolean 2D array for storing visited places
             boolean[][] visited = new boolean[height][width];
             Point current = new Point();
            // Store the entry point for backtracking in Moore neighborhood algorithm
             Point previous = new Point(0, height);
             /* From bottom to top and left to right scan the pixels until a non-white,
              * not previously visited pixel is found */
             for (int x = 0; x < width; x++) {
                 for (int y = height - 1; y >= 0; y--) {
                     if (!visited[y][x] && isEdge(x, y, original)) {
                         // Add image shape to list
                         current.x = x;
                         current.y = y;
                         imageShapes.add(mooreNeighborhood(current, previous, visited, original));
                     }
                     
                     // Remember previously visited point
                     previous.x = x;
                     previous.y = y;
                 }
                 
                 // Adjust previous if we are going to a new column
                 previous.x++;
                 previous.y = height;
             }
             sortImageShapes(imageShapes);
             return imageShapes;
         }
     }
     
     /**
      * Sorts the image list in decreasing order,
      * based on the size of image shape boundaries.
      * 
      * @param list - the list of image shapes to be sorted
      */
     private void sortImageShapes(List<ImageShape> list) {
         
         // Create comparator for sorting, based on the perimeter of image shapes
         Comparator<ImageShape> comp = new Comparator<ImageShape>() {
             public int compare( ImageShape o1, ImageShape o2 ) {
                 return Math.round( o2.getPerimeter() - o1.getPerimeter());
             }
         };
         
         // Use comparator to sort list
         Collections.sort( list, comp );
     }
     
     /**
      * Returns a list with a single item: the largest
      * image shape found for the picture.
      * 
      * @return list with largest image shape
      */
     public List<ImageShape> findLargestShape() {
         List<ImageShape> result = new ArrayList<ImageShape>();
         
         // If image shapes have not been found, detect them first
         if (imageShapes.isEmpty())
             findImageShapes();
         
         result.add( imageShapes.get( 0 ) );
         return result;
         
     }
     
     /**
      * Checks whether a pixel is in an edge of a
      * group of in a picture.
      * 
      * @param x - pixel's x coordinate
      * @param y - pixel's y coordinate
      * @param img - source image
      * @return true if pixel is in edge, false otherwise
      */
     private boolean isEdge(int x, int y, BufferedImage img) {
         int pixel = img.getRGB(x, y);
         
         // Pixel is only edge if it is not white
         if (pixel != white) {
             // if current 
             if (y + 1 == img.getHeight())
                 return true;
             
             int previous = img.getRGB( x, y + 1 );
             // If pixel is a different color than the previous, it is an edge
             if (pixel != previous)
                 return true;
         }
         return false;
     }
     
     /**
      * This method uses the Moore neighborhood contour-finding algorithm 
      * to determine the boundaries of a group of pixels. It returns an
      * ImageShape which contains the group's color and boundaries. Note
      * that this algorithm may fail to find the complete contour of an
      * 8-connected group of pixels which is not 4-connected (i.e. a group
      * of pixels only connected by diagonals).
      * 
      * @param start - A starting point on the edge of the pixel group
      * @param entry - The point through which the starting point was found
      * @param visited - Array of visited pixels
      * @param img - Image being examined
      * @return an ImageShape containing group color and boundaries
      */
     public ImageShape mooreNeighborhood(final Point start, Point entry, boolean[][] visited, BufferedImage img) {
         int width = img.getWidth();
         int height = img.getHeight();
         
         // Pixel color
         int color = img.getRGB( start.x, start.y );
         // Create resulting image shape
         Polygon p = new Polygon();
         ImageShape result = new ImageShape(p, new Color(color));
         
         // Set the current boundary point to be the starting point
         Point current = new Point(start.x, start.y);
         
         // Add first pixel to polygon
         p.addPoint( start.x, start.y );
         visited[start.y][start.x] = true;
         
         // If the shape is a single isolated pixel, quit now
         if (isIsolatedPixel(start, img))
             return result;
         
         // Remember which pixel to backtrack to
         Point backtrack = new Point(entry.x, entry.y);
         
         // Set clockwise to be the next clockwise pixel (from backtrack, with current as the center)
         Point clockwise = findNextClockwisePixel(backtrack, current);
         // While c not equal to s do
         while (!start.equals( clockwise )) {
             // If clockwise pixel is within bounds and is of the right color
             if (clockwise.x >= 0 && clockwise.x < width && clockwise.y >= 0 && clockwise.y < height &&
                     img.getRGB( clockwise.x, clockwise.y ) == color) {
                 // Add it to polygon
                 p.addPoint( clockwise.x, clockwise.y );
                 visited[clockwise.y][clockwise.x] = true;
                 // Step to new boundary point
                 current.x = clockwise.x;
                 current.y = clockwise.y;
                 // Set clockwise to be the next clockwise pixel (from backtrack, with current as the center)
                 clockwise = findNextClockwisePixel(backtrack, current);
             }
             else {
                 // Not in bounds or not the right color, so move on to the next pixel
                 backtrack.x = clockwise.x;
                 backtrack.y = clockwise.y;
                 // Set clockwise to be the next clockwise pixel (from backtrack, with current as the center)
                 clockwise = findNextClockwisePixel(backtrack, current);
             }
        }       
         return result;
     }
     
     /**
      * Finds the next clockwise pixel in the image, going around the
      * center point, starting after the from point. Note that this method
      * will skip pixels which are out of the image boundaries.
      * 
      * @param from - point from which to start the clockwise search
      * @param center - reference point which will be looked around
      * @param img - the source image
      * @return the next clockwise point within the boundaries of the image
      */
     private Point findNextClockwisePixel(Point from, Point center) {
         Point current = new Point(from.x, from.y);
         
         // Determine where start is in relation to center
         Point offset = new Point (current.x - center.x, current.y - center.y);
         
         // If in same column
         if (offset.x == 0) {
             // If one row above
             if (offset.y == -1)
                 // We are above center, so move to upper right corner
                 current.x++;
             // Else it will be one row below
             else
                 // We are below center, so move to lower left corner
                 current.x--;
         }
         // Else, if one column to the left
         else if (offset.x == -1) {
             // If one row above
             if (offset.y == -1)
                 // We are in upper left corner, so move above center
                 current.x++;
             // Else, if in same row
             else if (offset.y == 0)
                 // We are to the left of center, so move to upper left corner
                 current.y--;
             // Else it will be one row below
             else
                 // We are in lower left corner, so move to the left of center
                 current.y--;
         }
         // Else it will be one column to the right
         else {
             // If one row above
             if (offset.y == -1)
                 // We are in upper right corner, so move to the right of center
                 current.y++;
             // Else, if in same row
             else if (offset.y == 0)
                 // We are to the right of center, so move to lower right corner
                 current.y++;
             // Else it will be one row below
             else
                 // We are in lower right corner, so move below center
                 current.x--;
         }
         return current;
     }
     
     /**
      * Checks if a pixel in the image is isolated (that is, does
      * not have any neighbor pixels of the same color).
      * 
      * @param p - pixel to be checked
      * @param img - source image
      * @return true if isolated, false otherwise
      */
     private boolean isIsolatedPixel(Point p, BufferedImage img) {
         int pixel = img.getRGB( p.x, p.y );
         int width = img.getWidth();
         int height = img.getHeight();
         
         // Check right
         if (p.x + 1 < width) {
             if (pixel == img.getRGB( p.x + 1, p.y ))
                 return false;
         }
             
         // Check left
         if (p.x - 1 >= 0) {
             if (pixel == img.getRGB( p.x - 1, p.y ))
                 return false;
         }
         
         // Check top
         if (p.y - 1 >= 0) {
             if (pixel == img.getRGB( p.x, p.y - 1 ))
                 return false;
         }
         
         // Check bottom
         if (p.y + 1 < height) {
             if (pixel == img.getRGB( p.x, p.y + 1 ))
                 return false;
         }
         
         // Check top right diagonal
         if (p.x + 1 < width && p.y - 1 >= 0) {
             if (pixel == img.getRGB( p.x + 1, p.y - 1 ))
                 return false;
         }
         
         // Check bottom right diagonal
         if (p.x + 1 < width && p.y + 1 < height) {
             if (pixel == img.getRGB( p.x + 1, p.y + 1 ))
                 return false;
         }
         
         // Check top left diagonal
         if (p.x - 1 >= 0 && p.y - 1 >= 0) {
             if (pixel == img.getRGB( p.x - 1, p.y - 1 ))
                 return false;
         }
         
         // Check bottom left diagonal
         if (p.x - 1 >= 0 && p.y + 1 < height) {
             if (pixel == img.getRGB( p.x - 1, p.y + 1 ))
                 return false;
         }
         
         return true;
     }
 
     /**
      * Returns a copy of the original image
      * provided to the module.
      */
     public BufferedImage getImage() {
         return Pipeline.deepCopy( original );
     }
 
 }
