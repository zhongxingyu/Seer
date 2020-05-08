 package uk.org.lidalia.lang;
 
import java.lang.reflect.Constructor;

 /**
  * A collection of methods that could conveniently exist on {@link Class} but don't.
  */
 public final class Classes {
 
     /**
      * @return the generified compile time type of the object
      */
     @SuppressWarnings("unchecked")
     public static <CompileTimeType> Class<? extends CompileTimeType> getClass(final CompileTimeType object) {
         return (Class<? extends CompileTimeType>) object.getClass();
     }
 
     /**
      * @return true if one and other are the same type, or one is a subtype of other, or other is a subtype of one
      */
     public static boolean inSameClassHierarchy(final Class<?> one, final Class<?> other) {
         return one.isAssignableFrom(other) || other.isAssignableFrom(one);
     }
 
     /**
      * @param type the class to check for the existence of the constructor
      * @param parameterTypes the types of the parameters of the constructor
      * @return true if a constructor with those types exists on the class, false otherwise
      */
     public static boolean hasConstructor(final Class<?> type, final Class<?>... parameterTypes) {
         try {
             type.getConstructor(parameterTypes);
             return true;
         } catch (NoSuchMethodException e) {
             return false;
         }
 
     }
 
     private Classes() {
         throw new UnsupportedOperationException("Not instantiable");
     }
 }
