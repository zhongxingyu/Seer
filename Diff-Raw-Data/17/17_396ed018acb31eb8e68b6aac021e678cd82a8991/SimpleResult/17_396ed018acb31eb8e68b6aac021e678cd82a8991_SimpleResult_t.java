 package pl.edu.pjwstk.interpreter.qres;
 
 import edu.pjwstk.jps.datastore.IOID;
 import edu.pjwstk.jps.result.ISimpleResult;
 
 public class SimpleResult<T> extends SingleResult implements ISimpleResult<T> {
 
     private T value;
 
     public SimpleResult(IOID ioid) {
         super(ioid);
     }
 
     public SimpleResult(T value) {
         this.value = value;
     }
 
     public SimpleResult(IOID ioid, T value) {
         super(ioid);
         this.value = value;
     }
 
     @Override
     public T getValue() {
         return value;
     }
 
     @Override
     public String toString() {
         return "" + value;
     }
 

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

     public boolean equals(Object o) {
        return o instanceof SimpleResult && this.value.equals(((SimpleResult)o).getValue());
     }
 }
