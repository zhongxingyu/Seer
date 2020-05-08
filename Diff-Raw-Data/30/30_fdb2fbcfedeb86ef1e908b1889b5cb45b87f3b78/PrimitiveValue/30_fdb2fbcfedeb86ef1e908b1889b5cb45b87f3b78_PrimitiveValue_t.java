 package vitry.primitive;
 
 /**
 * Wrapper for host objects that need to implement Pattern.
  */
 public class PrimitiveValue extends Atom
     {
         private Object obj;
 
         private PrimitiveValue(Object obj) {
             this.obj = obj;
         }
 
         public boolean equals(Object o) {
             if (o instanceof PrimitiveValue)
                 return obj.equals(((PrimitiveValue) o).obj);
             return obj.equals(o);
         }
 
         public int hashCode() {
             return obj.hashCode();
         }
 
         public String toString() {
             return obj.toString();
         }
 
        public static Pattern wrap(Object o) {
            if (o instanceof Pattern) 
                return (Pattern) o;
             else
                 return new PrimitiveValue(o);
         }
 
        public static Pattern[] wrap(Object... values) {
            Pattern[] values2 = new Pattern[values.length];
             for (int i = 0; i < values.length; ++i)
                 values2[i] = wrap(values[i]);
             return values2;
         }
     }
