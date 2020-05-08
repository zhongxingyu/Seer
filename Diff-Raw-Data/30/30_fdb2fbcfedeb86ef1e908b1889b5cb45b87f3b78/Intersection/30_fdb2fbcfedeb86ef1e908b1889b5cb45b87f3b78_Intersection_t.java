 package vitry.primitive;
 
 /**
  * The & type.
  */
 public interface Intersection extends Value, CompoundPattern
     {
     }
 
 
 abstract class AbstractIntersection extends AbstractCompoundPattern implements Intersection
     {
         public boolean match(Object a) {
             for (Pattern x : this)
                 if (!x.match(a)) return false;
             return true;
         }
 
         public boolean match(Product a) {
             for (Pattern x : this)
                 if (!x.match(a)) return false;
             return true;
         }
         
         public boolean matchFor(Pattern p) {
             return p.match(this);
         }
         
         public String toString() {
            return Util.join(this, "(", " & ", ")");
         }
     }
