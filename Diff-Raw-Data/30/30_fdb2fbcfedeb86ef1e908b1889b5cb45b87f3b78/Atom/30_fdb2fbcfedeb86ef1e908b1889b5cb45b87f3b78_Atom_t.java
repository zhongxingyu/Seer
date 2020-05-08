 package vitry.primitive;
 
 /**
  * Implement:
  *   - equals (adjust for Refs!)
  */
 abstract public class Atom implements Value, Pattern
     {
         public Pattern head() {
             return this;
         }
 
         public Seq<Pattern> tail() {
             return null;
         }
 
         public boolean match(Object o) {
            return this.equals(o);
//            return o.equals(this) || this.equals(o);
         }
 
         public boolean match(Product p) {
             return false;
         }
 
         public boolean match(Union p) {
             return false;
         }
 
         public boolean match(Set p) {
             return false;
         }
 
         public boolean match(Intersection p) {
             return false;
         }
 
         public boolean match(Type p) {
             return false;
         }
 
         public boolean match(FunctionType p) {
             return false;
         }
 
         public boolean matchFor(Pattern p) {
             return p.match(this);
         }
     }
