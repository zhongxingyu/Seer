 package vitry.primitive;
 
 /**
 * Wrapper for host objects that need to implement Value or Pattern.
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
 
        public static PrimitiveValue wrap(Object o) {
            if (o instanceof PrimitiveValue) 
                return (PrimitiveValue) o;
             else
                 return new PrimitiveValue(o);
         }
 
        public static PrimitiveValue[] wrap(Object... values) {
            PrimitiveValue[] values2 = new PrimitiveValue[values.length];
             for (int i = 0; i < values.length; ++i)
                 values2[i] = wrap(values[i]);
             return values2;
         }
     }
