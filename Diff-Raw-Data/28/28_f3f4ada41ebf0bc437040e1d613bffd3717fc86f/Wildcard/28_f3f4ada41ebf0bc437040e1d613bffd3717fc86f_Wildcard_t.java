 package bv;
 
 import java.util.Set;
 
 public class Wildcard extends Expression {
     public Wildcard (int size) {
         super(true, false, true, size);
     }
 
     @Override
     public long eval () {
        throw new Error("Wildcard.eval()");
     }
 
     @Override
     public long weight () {
         return all.SolverMeta.myGenProgs.genProgs(size).size();
     }
 
     @Override
     public Expression filter (long output) {
         if (eval() == output)
             return this;
         else
             return null;
     }
 
     public Set<Long> allValues () {
         return all.SolverMeta.myGenVals.genValues(size);
     }
 
     @Override
     public Expression any () {
         return null;
     }
 
     @Override
     public String toString () {
         return "(*" + size + ")";
     }
 }
