 package rtb;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 public final class IObject {
 
    /**
     * Check the equality of two objects based upon their interfaces and getter methods.
     *
     * @param left Object to compare.
     * @param right Object to compare.
     * @return true iff the values of each object's getter methods return equal values and the objects share at least
     *         one common interface and getter value.
     */
    public static <T> boolean equals(T left, T right) {
       Class<? extends Object> lClass = left.getClass();
       Class<?>[] lInterfaces = lClass.getInterfaces();
       if (lInterfaces.length == 0)
          return false;
 
       Class<? extends Object> rClass = right.getClass();
       Class<?>[] rInterfaces = rClass.getInterfaces();
       if (rInterfaces.length == 0)
          return false;
 
       List<Class<?>> commonInterfaces = new ArrayList<>();
       for (Class<?> face : lInterfaces)
          for (Class<?> other : rInterfaces)
             if (other.equals(face))
                commonInterfaces.add(face);
 
       if (commonInterfaces.isEmpty())
          return false;
 
       boolean checked = false;
       for (Class<?> face : commonInterfaces) {
          Method[] methods = face.getMethods();
          for (Method method : methods)
              // TODO: Check #is... methods?
             if (method.getName().startsWith("get") && method.getParameterTypes().length == 0)
                try {
                   Object lValue = method.invoke(left);
                   Object rValue = method.invoke(right);
                   if (!lValue.equals(rValue))
                      return false;
                   else
                      checked = true;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                   // TODO: With partially implemented interfaced, may be OK to catch and continue.
                   return false;
                }
       }
 
       return checked;
    }
 
    private IObject() { }
 }
