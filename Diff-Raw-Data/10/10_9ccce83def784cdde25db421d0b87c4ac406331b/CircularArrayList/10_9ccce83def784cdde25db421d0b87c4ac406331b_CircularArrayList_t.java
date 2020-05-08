 import java.util.ArrayList;
import java.util.Collection;
 
 public class CircularArrayList<E> extends ArrayList<E> {
    public CircularArrayList(Collection<? extends E> other) {
         super(other);
     }
 
    public CircularArrayList() {
        super();
    }

     public E get(int i) {
         return super.get(i % size());
     }
 }
