 import java.util.ArrayList;
import java.util.List;
 
 public class CircularArrayList<E> extends ArrayList<E> {
    public CircularArrayList(List<E> other) {
         super(other);
     }
 
     public E get(int i) {
         return super.get(i % size());
     }
 }
