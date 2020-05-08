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
 
     public boolean equals(Object o) {
        return this == o;
     }
 }
