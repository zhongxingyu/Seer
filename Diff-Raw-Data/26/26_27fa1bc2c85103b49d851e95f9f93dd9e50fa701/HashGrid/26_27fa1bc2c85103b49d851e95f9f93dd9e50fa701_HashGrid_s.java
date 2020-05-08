 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package halfzero;
 
 import halfzero.Grid.DimensionMismatchException;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 /**
  *
  * @author harrison
  */
 public class HashGrid <E extends Serializable> implements Grid<E> {
     
     private java.util.Map<Coordinate,E> map;
     private int w,h;
     
     public HashGrid (int _w, int _h) {
         w = _w; h = _h;
         map = new java.util.HashMap<Coordinate, E>(_w*_h);
     }
 
     @Override
     public int width() {
         return w;
     }
 
     @Override
     public int height() {
         return h;
     }
 
     @Override
     public boolean isEmpty() {
         return w==0 && h==0;
     }
 
     @Override
     public boolean contains(E o) {
         return map.containsValue(o);
     }
 
     @Override
     public E[][] toArray() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public E get(int x, int y) {
        Coordinate c = new Coordinate(x, y);
        return map.get(c);
     }
 
     @Override
     public void set(int x, int y, E _val) {
         Coordinate c = new Coordinate(x, y);
         map.put(c, _val);
     }
 
     @Override
     public Grid<E> subGrid(int x0, int x1, int y0, int y1) {
         return new SubGrid<E>(this, x0, y0, x1 - x0, y1 - y0);
     }
 
     @Override
     public void insert(Grid<E> sg, int x0, int y0) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void grow(int dx, int dy) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void grow(int dx, int dy, E o) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void adjoinH(Grid<E> n) throws DimensionMismatchException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void adjoinV(Grid<E> n) throws DimensionMismatchException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     @Override
     public Iterator<E> iterator() {
         return new Iterator<E>() {
             
             Iterator<Entry<Coordinate, E>> it = map.entrySet().iterator();
 
             @Override
             public boolean hasNext() {
                 return it.hasNext();
             }
 
             @Override
             public E next() {
                 return it.next().getValue();
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
         };
     }
     
 }
 
 class SubGrid<E extends Serializable> implements Grid<E> {
     
     private Grid<E> backing;
     private int x0, y0, w, h;
 
     public SubGrid (Grid<E> _backing, int _x0, int _y0, int _w, int _h) {
         x0 = _x0; y0 = _y0; w = _w; h = _h;
         backing = _backing;
         if (x0 < 0
          || y0 < 0
          || x0 + w >= backing.width()
          || y0 + h >= backing.height())
             throw new ArrayIndexOutOfBoundsException();
     }
     
     @Override
     public int width() {
         return w;
     }
 
     @Override
     public int height() {
         return h;
     }
 
     @Override
     public boolean isEmpty() {
         return w==0 && h==0;
     }
 
     @Override
     public boolean contains(E o) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public E[][] toArray() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public E get(int x, int y) {
         if (x < 0 || y < 0 || x >= w || y >= h)
             throw new ArrayIndexOutOfBoundsException();
         return backing.get(x0 + x, y0 + y);
     }
 
     @Override
     public void set(int x, int y, E _val) {
         if (x < 0 || y < 0 || x >= w || y >= h)
             throw new ArrayIndexOutOfBoundsException();
         backing.set(x0 + x, y0 + y, _val);
     }
 
     @Override
     public Grid<E> subGrid(int _x0, int _x1, int _y0, int _y1) {
         return backing.subGrid(x0 + _x0, x0 + _x1, y0 + _y0, y0 + _y1);
     }
 
     @Override
     public void insert(Grid<E> sg, int x0, int y0) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void grow(int dx, int dy) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void grow(int dx, int dy, E o) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void adjoinH(Grid<E> n) throws DimensionMismatchException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void adjoinV(Grid<E> n) throws DimensionMismatchException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<E> iterator() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
