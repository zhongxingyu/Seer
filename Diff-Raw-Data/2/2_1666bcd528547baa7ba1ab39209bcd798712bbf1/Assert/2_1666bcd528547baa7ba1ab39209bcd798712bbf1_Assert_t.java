 package http.util;
 
 /**
  * @author Karl Bennett
  */
 public final class Assert {
 
     /**
      * The {@code Assert} constructor is private because it is a utility class so does not contain any state and should
     * never be instantiated.
      */
     private Assert() {
     }
 
     /**
      * Assert that the supplied value is not null and throw an {@link IllegalArgumentException} if it is. This should be
      * used to check the arguments of constructors and methods.
      *
      * @param name the name of the variable that is being checked. It will be used in the exception message.
      * @param value the value of the variable that is being checked for null.
      * @throws IllegalArgumentException if the value is null.
      */
     public static void assertNotNull(String name, Object value) {
 
         if (null == name) {
 
             throw new IllegalArgumentException(Assert.class.getName() +
                     ".assertNotNull(String,String) cannot have a null name argument.");
         }
 
         if (null == value) {
 
             throw new IllegalArgumentException("The (" + name + ") variable should not be null.");
         }
     }
 }
