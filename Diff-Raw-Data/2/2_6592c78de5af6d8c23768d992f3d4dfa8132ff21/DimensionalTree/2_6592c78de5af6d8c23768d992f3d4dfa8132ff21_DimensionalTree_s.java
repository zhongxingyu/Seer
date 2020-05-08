 package dk.itu.grp11.contrib;
 
import dk.itu.grp11.exceptions.IllegalIntervalException;

 public class DimensionalTree<Key extends Comparable<Key>, Key2 extends Comparable<Key2>, Value> {
   private Node root;
   private int count;
   private Class<Value[]> valueClass;
   DynArray<Value> found;
 
   // helper node data type
   private class Node {
       Key d1, d2;                                               // First two comparable dimensions (Values 0/1)
       Key2 d3;                                                  // Third comparable dimension (Values 0/1/2)
       Node D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12;   // Tvelve dimensions
       Value value;                                              // associated data
 
       Node(Key x, Key y, Key2 type, Value value) {
           this.d1 = x;
           this.d2 = y;
           this.d3 = type;
           this.value = value;
       }
   }
   
   public DimensionalTree(Class<Value[]> valueClass) {
     this.valueClass = valueClass;
   }
   
   /***********************************************************************
    *  Insert (x, y) into appropriate quadrant
    ***********************************************************************/
    public void insert(Key x, Key y, Key2 type, Value value) {
        root = insert(root, x, y, type, value);
        count++;
    }
 
    private Node insert(Node h, Key d1, Key d2, Key2 d3, Value value) {
        if (h == null) return new Node(d1, d2, d3, value);
        //// if (eq(x, h.x) && eq(y, h.y)) h.value = value;  // duplicate
        else if ( less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) == -1) h.D1  = insert(h.D1 , d1, d2, d3, value);
        else if ( less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) ==  0) h.D2  = insert(h.D2 , d1, d2, d3, value);
        else if ( less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) ==  1) h.D3  = insert(h.D3 , d1, d2, d3, value);
        else if ( less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) == -1) h.D4  = insert(h.D4 , d1, d2, d3, value);
        else if ( less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) ==  0) h.D5  = insert(h.D5 , d1, d2, d3, value);
        else if ( less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) ==  1) h.D6  = insert(h.D6 , d1, d2, d3, value);
        else if (!less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) == -1) h.D7  = insert(h.D7 , d1, d2, d3, value);
        else if (!less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) ==  0) h.D8  = insert(h.D8 , d1, d2, d3, value);
        else if (!less(d1, h.d1) &&  less(d2, h.d2) && compare(d3, h.d3) ==  1) h.D9  = insert(h.D9 , d1, d2, d3, value);
        else if (!less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) == -1) h.D10 = insert(h.D10, d1, d2, d3, value);
        else if (!less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) ==  0) h.D11 = insert(h.D11, d1, d2, d3, value);
        else if (!less(d1, h.d1) && !less(d2, h.d2) && compare(d3, h.d3) ==  1) h.D12 = insert(h.D12, d1, d2, d3, value);
        return h;
    }
    
    /***********************************************************************
     *  Range search.
     ***********************************************************************/
 
     public Value[] query2D(Interval2D<Key, Key2> rect) {
       found = new DynArray<Value>(valueClass);
       return query2D(root, rect);
     }
 
     private Value[] query2D(Node h, Interval2D<Key, Key2> rect) {
         
         if (h == null) return null;
         if (rect.getIntervalX().getD3() != rect.getIntervalY().getD3()) throw new IllegalArgumentException();
         Key d1min = rect.getIntervalX().getLow();
         Key d2min = rect.getIntervalY().getLow();
         Key d1max = rect.getIntervalX().getHigh();
         Key d2max = rect.getIntervalY().getHigh();
         Key2 d3 = rect.getIntervalX().getD3();
         if (rect.contains(h.d1, h.d2, h.d3))
             found.add(h.value);
         if ( less(d1min, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) == -1) query2D(h.D1 , rect);
         if ( less(d1min, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) ==  0) query2D(h.D2 , rect);
         if ( less(d1min, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) ==  1) query2D(h.D3 , rect);
         if ( less(d1min, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) == -1) query2D(h.D4 , rect);
         if ( less(d1min, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) ==  0) query2D(h.D5 , rect);
         if ( less(d1min, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) ==  1) query2D(h.D6 , rect);
         if (!less(d1max, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) == -1) query2D(h.D7 , rect);
         if (!less(d1max, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) ==  0) query2D(h.D8 , rect);
         if (!less(d1max, h.d1) &&  less(d2min, h.d2) && compare(d3, h.d3) ==  1) query2D(h.D9 , rect);
         if (!less(d1max, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) == -1) query2D(h.D10, rect);
         if (!less(d1max, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) ==  0) query2D(h.D11, rect);
         if (!less(d1max, h.d1) && !less(d2max, h.d2) && compare(d3, h.d3) ==  1) query2D(h.D12, rect);
         return found.toArray();
     }
    
    /*************************************************************************
     *  helper comparison functions
     *************************************************************************/
 
     private boolean less(Key k1, Key k2) { return k1.compareTo(k2) <  0; }
     private int compare(Key2 k1, Key2 k2) { 
       if (k1.compareTo(k2) < 0) return -1;
       else if (k1.compareTo(k2) > 0) return 1;
       else return 0;
     }
     
     public int count() {
       return count;
     }
 }
