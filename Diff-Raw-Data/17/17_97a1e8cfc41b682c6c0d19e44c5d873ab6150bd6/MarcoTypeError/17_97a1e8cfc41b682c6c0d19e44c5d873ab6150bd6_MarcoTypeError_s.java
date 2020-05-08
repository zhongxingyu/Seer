 package marco;
 
 import marco.lang.MarcoObject;
 
 public class MarcoTypeError extends MarcoException {
    public MarcoTypeError(Class klass, MarcoObject value) {
        super("Expected " + klass.getName() + " but got a " + value.getClass().getName());
     }
 }
