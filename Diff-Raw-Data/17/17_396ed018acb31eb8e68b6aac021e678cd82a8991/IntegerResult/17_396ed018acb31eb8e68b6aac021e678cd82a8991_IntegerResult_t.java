 package pl.edu.pjwstk.interpreter.qres;
 
 import edu.pjwstk.jps.datastore.IOID;
 import edu.pjwstk.jps.result.IIntegerResult;
 
 public class IntegerResult extends SimpleResult<Integer> implements IIntegerResult {
 
     public IntegerResult(IOID ioid) {
         super(ioid);
     }
 
     public IntegerResult(Integer value) {
         super(value);
     }
 
     public IntegerResult(IOID ioid, Integer value) {
         super(ioid, value);
     }
 
     public String toString() {
         return "IntegerResult{" +
                 "value=" + getValue() +
                 '}';
     }
 
     public boolean equals(Object o) {
        boolean out =  ((o == this) || ( (o instanceof IntegerResult) && (((IntegerResult) o).getValue().equals(this.getValue()))));
        return out;
     }
 }
