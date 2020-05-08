 package test.mock;
 
 import java.lang.reflect.Field;
 
 import static test.mock.Phase.*;
 
 /**
  * Static methods for creating and interacting with test mocks.
  */
 public final class Mocks {
 
     /**
      * For making mocks.
      */
     private static MockFactory factory = new MockFactory();
 
     /**
      * Initialize the instance variables in the given test.
      * Null interface fields will be assigned values.
      * Generally, this method will be used in a test as follows:
      * <pre>
              @Before
              public void init() {
                  Mocks.init(this);
              }
      * </pre>
      */
     public static void init(Object test) {
         factory = new MockFactory();
         try {
             _init(test);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
         current = invoke;
     }
 
     private static void _init(Object test) throws IllegalAccessException {
         for (Field field : test.getClass().getDeclaredFields()) {
             field.setAccessible(true);
             Object value = field.get(test);
             Class type = field.getType();
             if (value==null && type.isInterface()) {
                 String name = field.getName();
                 Object mock = mock(name,type);
                 field.set(test,mock);
             }
         }
     }
 
     /**
      * Return a mock of the given description.
      * This method is used internally by init.
      */
     static <T> T mock(String name, Class<T> face) {
         return factory.mock(face, name);
     }
 
     /**
     * Specify that the following method will return the given value.
      */
     public static <T> void returns(T value) {
         factory.returns(value);
     }
 
     /**
     * Specify that the following method will return no value.
      */
     public static <T> void _() {
         factory.returns(null);
     }
 
     /**
     * Specify that the following method will return the given value.
      */
     public static <T> void _(T value) {
         factory.returns(value);
     }
 
     /**
     * Specify that the following method will return no value.
      */
     public static <T> void returns() {
         factory.returns(null);
     }
 
     /**
      * Start verifying invocations.
      */
     public static void verify() {
         current = verify;
     }
 
     /**
      * Start verifying things didn't happen.
      */
     public static void no() {
         current = no;
     }
 }
