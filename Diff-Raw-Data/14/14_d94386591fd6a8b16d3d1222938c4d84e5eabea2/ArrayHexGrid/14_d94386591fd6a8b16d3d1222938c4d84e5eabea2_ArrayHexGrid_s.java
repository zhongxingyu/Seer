 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.grid;
 
 import java.lang.reflect.Array;
 import java.util.Iterator;
 import static student.grid.HexGrid.HexDir.*;
 
 
 public class ArrayHexGrid<E> implements HexGrid<E> {
     private int rs, cs;
     private Ref data[][];
     
     public ArrayHexGrid(int _rs, int _cs) {
         rs = _rs; cs = _cs;
         data = (Ref[][]) Array.newInstance(Ref[].class, rs);
         for(int r = 0; r < rs; r++) {
             data[r] = (Ref[]) Array.newInstance(Ref.class, cs);
             for(int c = 0; c < cs; c++)
                 data[r][c] = new Ref(r,c);
         }
     }
 
     @Override
     public E get(int c, int r) throws HexIndexOutOfBoundsException {
         Reference<E> ref = ref(c,r);
         if(ref == null)
             throw new HexIndexOutOfBoundsException(r, c);
         return ref.contents();
     }
 
     @Override
     public void set(int c, int r, E e) throws HexIndexOutOfBoundsException {
         Reference<E> ref = ref(c,r);
         if(ref == null)
             throw new HexIndexOutOfBoundsException(r, c);
         ref.setContents(e);
     }
 
     @Override
     public int nRows() {
         return rs;
     }
 
     @Override
     public int nCols() {
         return cs;
     }
 
     @Override
     public Reference<E> ref(int c, int r) {
         if(r < 0 || c < 0 || r >= rs || c >= cs)
             return null;//throw new HexIndexOutOfBoundsException(r, c);
         return data[r][c];
     }
 
     @Override
     public Iterator<Reference<E>> iterator() {
         return new Itr();
     }
 
     private class Itr implements Iterator<Reference<E>> {
         int row = -1, col = -1;
         
         @Override
         public boolean hasNext() {
             return row < data.length - 1
                 || col < data[row].length - 1;
         }
 
         @Override
         public Reference<E> next() {
             col++;
             if(row < 0 || col >= data[row].length) {
                 row ++;
                 col = 0;
             }
             return data[row][col];
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Not supported yet.^H^H^H^Hever.");
         }
         
     }
    
     private class Ref implements Reference<E> {
         int r, c;
         E e;
         
         public Ref(int _r, int _c){
             r = _r; c = _c;
         }
         
         @Override
         public E contents() {
             return e;
         }
 
         @Override
         public void setContents(E _e) {
             e = _e;
         }
 
         @Override
         public int row() {
             return r;
         }
 
         @Override
         public int col() {
             return c;
         }
 
         @Override
         public int slice() {
             return c - r;
         }
 
         @Override
         public Reference<E> adj(HexDir dir) {
             int er = r, ec = c;
             if(c%2==0) {
                 switch(dir) {
                     case N:
                         er -= 1; break;
                     case S:
                         er += 1; break;
                     case NE:
                         ec += 1; break;
                     case NW:
                         ec -= 1; break;
                     case SE:
                         ec += 1;
                         er += 1; break;
                     case SW:
                         ec -= 1;
                         er += 1; break;
                 }
             } else {
                 switch(dir) {
                     case N:
                         er -= 1; break;
                     case S:
                         er += 1; break;
                     case NE:
                         ec += 1;
                         er -= 1; break;
                     case NW:
                         ec -= 1;
                         er -= 1; break;
                     case SW:
                         ec -= 1; break;
                     case SE:
                         ec += 1; break;
                 }
             }
             System.out.println("("+er+","+ec+")");
             if(er < 0 || er >= data.length || ec < 0 || ec >= data[er].length)
                 return null;
             return data[er]
                        [ec];
         }
 
         @Override
         public Reference<E> lin(int dist,HexDir dir) {
             if(dist == 0)
                 return this;
             else if(dist < 0)
                 return lin(-dist, dir == N  ?
                              S  : dir == S  ?
                              N  : dir == NE ?
                              SW : dir == SW ?
                              NE : dir == NW ?
                             SE : NW); 
                 return adj(dir).lin(dist-1,dir);
         }
     }
 }
