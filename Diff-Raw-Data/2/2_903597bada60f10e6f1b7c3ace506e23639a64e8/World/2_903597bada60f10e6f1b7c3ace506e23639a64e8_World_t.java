 package galapagos;
 
 import java.util.*;
 
 /**
  * A torus world, a two-dimensional array containing "places" that can
  * contain objects and which are aware of their position and
  * surroundings. Also provides an iterator-based facility for going
  * through the places of the world randomly.
  */
 public class World<T> implements Iterable<World<T>.Place> {
     private ArrayList<Place> array;
     private int width;
     private int height;
 
     /**
      * Create a world of size worldWidth * worldHeight.
      */
     public World(int worldWidth, int worldHeight) {
         width = worldWidth;
         height = worldHeight;
         array = new ArrayList<Place>(width * height);
         for (int x = 0; x < width; x++)
             for (int y = 0; y < height; y++)
                 array.add(new Place(x, y));
     }
 
     /**
      * Return the width of the world in places, this value will never
      * change.
      *
      * @return The height in cells.
      */
     public int width() {
         return width;
     }
 
     /**
      * Return the height of the world in places, this value will never
      * change.
      *
      * @return The height in cells.
      */
     public int height() {
         return height;
     }
 
     /**
      * A position in the world, contains a single element. Objects of
      * this class are immutable, but may change when the setAt()
      * method of World is invoked.
      */
     public class Place {
         private T element;
         private int xPosition, yPosition;
 
         private Place(int x, int y) {
             xPosition = x;
             yPosition = y;
         }
 
         private Place(int x, int y, T object) {
             xPosition = x;
             yPosition = y;
             element = object;
         }
 
         /**
          * Get the element of the place.
          *
          * @return The element at the place, may be null, meaning that
          * the place is empty.
          */
         public T element() {
             return element;
         }
 
         /**
          * Get the x position (left to right, zero-indexed) of this
          * place in its world.
          *
          * @return The x position.
          */
         public int xPosition() {
             return xPosition;
         }
 
         /**
          * Get the y position (top to bottom, zero-indexed) of this
          * place in its world.
          *
          * @return The y position.
          */
         public int yPosition() {
            return yPosition;
         }
 
         private int wrappedX(int x) {
             if (x < 0) return width + x;
             else return x % width;
         }
 
         private int wrappedY(int y) {
             if (y < 0) return height + y;
             return y % height;
         }
 
         /**
          * Get the filled neighbors of this place as a randomly
          * arranged list.
          *
          * @return A list of non-empty neighbor places.
          */
         public List<Place> filledNeighbours() {
             List<Place> list = new ArrayList<Place>(8);
             for (int x = xPosition - 1; x <= xPosition + 1; x++)
                 for (int y = yPosition - 1; y <= yPosition + 1; y++) {
                     Place p = getAt(wrappedX(x), wrappedY(y));
                     if (p != this && p.element != null)
                         list.add(getAt(wrappedX(x), wrappedY(y)));
                 }
             Collections.shuffle(list);
             return list;
         }
         
         /**
          * Get the empty neighbors of this place as a randomly
          * arranged list.
          *
          * @return A list of empty neighbor places.
          */
         public List<Place> emptyNeighbours() {
             List<Place> list = new ArrayList<Place>(8);
             for (int x = xPosition - 1; x <= xPosition + 1; x++)
                 for (int y = yPosition - 1; y <= yPosition + 1; y++) {
                     Place p = getAt(wrappedX(x), wrappedY(y));
                     if (p != this && p.element == null)
                         list.add(getAt(wrappedX(x), wrappedY(y)));
                 }
             Collections.shuffle(list);
             return list;
         }
     }
 
     /**
      * Return the place at offset x,y.
      */
     public Place getAt(int x, int y) {
         assert(x < width);
         assert(y < height);
         return array.get(x * height + y);
     }
 
     /**
      * Set the element of the place at offset x,y.
      */
     public void setAt(int x, int y, T value) {
         getAt(x, y).element = value;
     }
 
     public Iterator<Place> iterator() {
         return array.iterator();
     }
 
     /**
      * Return an iterator that will iterate through the places of the
      * world in random order. Changes to the world after the iterator
      * has been created will not be reflected in the elements iterated
      * over.
      */
     public Iterator<Place> randomIterator() {
         List<Place> list = (List) array.clone();
         Collections.shuffle(list);
         return list.iterator();
     }
 }
