 
 package tajmi.som;
 
 import java.util.Iterator;
 import tajmi.abstracts.som.InitFunc;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import scala.Tuple2;
 
 /**
  * Implements a static 2D field for the SOM projection
  * @author badi
  */
 public class Field<F> implements Iterable<Tuple2<Position, F>> {
 
     List<List<F>> field;
     int length, width;
 
 
     public Field (int length, int width, InitFunc initf) {
 
        this.length = 2 * length;
         this.width = 2 * width;
 
         boolean zero = true;
        
         field = new ArrayList<List<F>>(this.length);
         for (int x = 0; x < this.length; x++){
//            if ( even(x) ) continue;
            
             field.add(new ArrayList<F>(this.width));
             for (int y = 0; y < this.width; y++) {
 
                 if (zero && even(y)){
                     field.get(x).add(null);
                     continue;
                 }
                 else if (!zero && odd(y)){
                     field.get(x).add(null);
                     continue;
                 }
 
                 F res = (F) initf.call();
                 field.get(x).add(res);
             }
 
             if (zero) zero = false;
             else zero = true;
         }
     }
 
     private boolean even (int x) {
         return x % 2 == 0;
     }
 
     private boolean odd (int x) {
         return ! even (x);
     }
 
     public Field (Field<F> old_field) {
         this.length = old_field.length;
         this.width = old_field.width;
 
         this.field = new ArrayList<List<F>>(length);
 
         for (int i = 0; i < length; i++) {
             this.field.add(new ArrayList(width));
             for (int j = 0; j < width; j++)
                 this.field.get(i).add(old_field.get(new Position(i, j)));
         }
     }
 
     public F get (Position pos) {
         return (F) field.get(pos.x()).get(pos.y());
     }
 
     public void set (Position pos, F datum) {
         field.get(pos.x()).set(pos.y(), datum);
     }
 
     public Field set (List<Tuple2<Position,F>> info) {
         for (Tuple2<Position, F> item : info) {
             Position pos = item._1();
             F datum = item._2();
             set( pos, datum );
         }
         return this;
     }
 
     public int size () {
         return field.size() * field.get(0).size();
     }
 
      public Tuple2<Integer, Integer> dimensions () {
        return new Tuple2<Integer, Integer>(length / 2, width / 2);
     }
 
     /**
      * @return an interator over a linked list of the elements in the field.
      */
     public Iterator<Tuple2<Position, F>> iterator() {
        // this is ugly: Java needs lazy generators
         List<Tuple2<Position, F>> l = new LinkedList<Tuple2<Position, F>>();
         for (int x = 0; x < field.size(); x++)
             for (int y = 0; y < field.get(x).size(); y++) {
                 F f = field.get(x).get(y);
                 if (f == null)
                     continue;
                 else
                     l.add(new Tuple2<Position, F> (new Position(x, y), f));
             }
         return l.iterator();
     }
 
     public String toString () {
         String me = "";
         for (int i = 0; i < length; i++){
             for (int j = 0; j < width; j++) {
                 F f = this.get(new Position(i, j));
                 me += f == null
                         ? " "
                        : f;
             }
             me += "\n";
         }
         return me;
     }
 
 }
