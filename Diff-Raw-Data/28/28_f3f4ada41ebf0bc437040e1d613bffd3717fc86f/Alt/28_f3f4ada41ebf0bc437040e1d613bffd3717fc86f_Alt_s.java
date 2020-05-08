 package bv;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 
 public class Alt extends Expression {
     public ArrayList<Expression> alts;
 
     public Alt (ArrayList<Expression> alts) {
         super(true, false, true, alts.get(0).size);
         this.alts = alts;
     }
 
     @Override
     public long eval () {
        return 0L;
     }
 
     @Override
     public long weight () {
         long res = 0;
         for (Expression e : alts) {
             res += e.weight();
         }
         return res;
     }
 
     
     @Override
     public Expression filter (long output) {
         if (eval() == output)
             return this;
         else
             return null;
     }
 
     @Override
     public ArrayList<Expression> all () {
         return alts;
     }
 
     public Set<Long> allValues () {
         Set<Long> values = new HashSet<Long>();
         for (Expression e : alts) {
             values.addAll(e.allValues());
         }
         return values;
     }
 
     @Override
     public Expression any () {
         return alts.get(0).any();
     }
 
     @Override
     public String toString () {
         return "(ALT size: " + alts.size() + ")";
     }
 }
