 package marco;
 
 import marco.lang.MarcoObject;
 
 public class MarcoTypeError extends MarcoException {
    private final Class expected;
    private final MarcoObject actualValue;

    public MarcoTypeError(Class expected, MarcoObject value) {
        super("Expected " + expected.getName() + " but got a " + value.getClass().getName());
        this.expected = expected;
        this.actualValue = value;
    }

    public Class getExpected() {
        return expected;
    }

    public MarcoObject getActual() {
        return actualValue;
     }
 }
