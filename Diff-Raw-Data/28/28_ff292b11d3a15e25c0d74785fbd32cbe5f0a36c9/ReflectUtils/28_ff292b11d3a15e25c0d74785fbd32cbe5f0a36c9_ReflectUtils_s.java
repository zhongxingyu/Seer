 /*
  * Created on Nov 26, 2004
  */
 package uk.org.ponder.reflect;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import uk.org.ponder.conversion.StaticLeafParser;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Logger;
 
 /**
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ReflectUtils {
 
   public static boolean isPublicStatic(int modifiers) {
     return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
   }
 
   /**
    * Determine if the given type is assignable from the given value, assuming
    * setting by reflection. Considers primitive wrapper classes as assignable to
    * the corresponding primitive types. <p/>For example used in a bean factory's
    * constructor resolution. <p/>Taken from Spring Framework "BeanUtils.java"
    * release version 2.0.5
    * 
    * @author Rod Johnson
    * @author Juergen Hoeller
    * @author Rob Harrop
    * @param type the target type
    * @param value the value that should be assigned to the type
    * @return if the type is assignable from the value
    */
   public static boolean isAssignable(Class type, Object value) {
     return (value != null ? isAssignable(type, value.getClass())
         : !type.isPrimitive());
   }
 
   /**
    * Determine if the given target type is assignable from the given value type,
    * assuming setting by reflection. Considers primitive wrapper classes as
    * assignable to the corresponding primitive types. 
    * 
    * @param declaredType the target type
    * @param objectType the value type that should be assigned to the target type
    * @return if the target type is assignable from the value type
    */
   public static boolean isAssignable(Class declaredType, Class objectType) {
     return (declaredType.isAssignableFrom(objectType) || StaticLeafParser
         .wrapClass(declaredType) == objectType);
   }
 
   /**
    * Determine a weight that represents the class hierarchy difference between
    * types and arguments. A direct match, i.e. type Integer -> arg of class
    * Integer, does not increase the result - all direct matches means weight 0.
    * A match between type Object and arg of class Integer would increase the
    * weight by 2, due to the superclass 2 steps up in the hierarchy (i.e.
    * Object) being the last one that still matches the required type Object.
    * Type Number and class Integer would increase the weight by 1 accordingly,
    * due to the superclass 1 step up the hierarchy (i.e. Number) still matching
    * the required type Number. Therefore, with an arg of type Integer, a
    * constructor (Integer) would be preferred to a constructor (Number) which
    * would in turn be preferred to a constructor (Object). All argument weights
    * get accumulated.
    * </p> This method will assign a penalty of 1024 to an invocation that would
    * require a type conversion (currently only default leaf conversions from String 
    * are considered)
    * </p>
    * This method adapted from Spring framework AutowireUtils.java release version
    * 2.0.5
    * 
    * @author Juergen Hoeller
    * @param argTypes the argument types to match
    * @param args the arguments to match
    * @return the accumulated weight for all arguments
    */
   public static int getTypeDifferenceWeight(Class[] argTypes, Object[] args) {
     int result = 0;
     for (int i = 0; i < argTypes.length; i++) {
       Object arg = args[i];
       if (!isAssignable(argTypes[i], arg)) {
         if (arg instanceof String && StaticLeafParser.instance().isLeafType(argTypes[i])) {
           result += 1024;
         }
         else return Integer.MAX_VALUE;
       }
       if (args[i] != null) {
         Class superClass = arg.getClass().getSuperclass();
         while (superClass != null) {
           if (isAssignable(argTypes[i], superClass)) {
             result++;
             superClass = superClass.getSuperclass();
           }
           else {
             superClass = null;
           }
         }
       }
     }
     return result;
   }
 
   public static Method[] getMatchingMethods(Class clazz, String methodname, Object[] args) {
     Method[] allMethods = clazz.getMethods();
     int bestMatch = Integer.MAX_VALUE;
     List togo = new ArrayList(); 
     for (int i = 0; i < allMethods.length; ++ i) {
       Method method = allMethods[i];
       if (!method.getName().equals(methodname) || method.getParameterTypes().length != args.length) continue;
       int diff = getTypeDifferenceWeight(method.getParameterTypes(), args);
       if (diff < bestMatch) {
         togo.clear();
         bestMatch = diff;
       }
       if (diff <= bestMatch) {
         togo.add(method);
       }
     }
     return (Method[]) togo.toArray(new Method[togo.size()]);
   }
   
   /** Determines whether an object is capable of supporting a method invocation
    * with a given name. Used primarily to resolve potential ambiguity in EL
    * expressions that are being interpreted as method bindings.
    */
   public static boolean hasMethod(Object obj, String methodname) {
     if (obj instanceof MethodInvokingProxy) return true;
     Method[] allMethods = obj.getClass().getMethods();
     for (int i = 0; i < allMethods.length; ++ i) {
       Method method = allMethods[i];
       if (method.getName().equals(methodname)) return true;
     }
     return false;
   }
   
   /**
    * Returns a list of all superclasses and implemented interfaces by the
    * supplied class, recursively to the base, up to but excluding Object.class.
    * These will be listed in order from the supplied class, all concrete
    * superclasses in ascending order, and then finally all interfaces in
    * recursive ascending order.
    */
 
   public static List getSuperclasses(Class clazz) {
     List togo = new ArrayList();
     while (clazz != Object.class) {
       togo.add(clazz);
       clazz = clazz.getSuperclass();
     }
     int supers = togo.size();
     for (int i = 0; i < supers; ++i) {
       appendSuperclasses((Class) togo.get(i), togo);
     }
     return togo;
   }
 
   private static void appendSuperclasses(Class clazz, List accrete) {
     Class[] interfaces = clazz.getInterfaces();
     for (int i = 0; i < interfaces.length; ++i) {
      accrete.add(interfaces[i]);
     }
     for (int i = 0; i < interfaces.length; ++i) {
       appendSuperclasses(interfaces[i], accrete);
     }
   }
 
   public static final int UNKNOWN_SIZE = -1;
 
   /**
    * Instantiates a "default" type of container conforming to a given interface
    * and of a given size. If asked to create an array of unknown size, will
    * return an zero-element array instead, signalling a "placeholder".
    */
   public static Object instantiateContainer(Class declaredtype, int size,
       ReflectiveCache cache) {
     // only missing case is if someone madly declares an argument of type
     // "AbstractList" or similar...
     if (declaredtype == List.class || declaredtype == Collection.class) {
       return size == UNKNOWN_SIZE ? new ArrayList()
           : new ArrayList(size);
     }
     else if (declaredtype == Set.class) {
       return size == UNKNOWN_SIZE ? new HashSet()
           : new HashSet(size);
     }
     else if (declaredtype == Map.class) {
       return size == UNKNOWN_SIZE ? new HashMap()
           : new HashMap(size);
     }
     else if (declaredtype.isArray()) {
       if (size == UNKNOWN_SIZE)
         size = 0;
       // erm, this is a native method! How long does it take exactly!
       Class component = declaredtype.getComponentType();
       if (component == Object.class) {
         // the most common case.
         return new Object[size];
       }
       else {
         return Array.newInstance(component, size);
       }
     }
     // else it had better be something default-constructible.
     else
       return cache.construct(declaredtype);
   }
 
   public static final boolean PREFIX = true;
   public static final boolean SUFFIX = false;
 
   public static void addStaticStrings(Object provider, String xfix,
       boolean prefix, StringList strings) {
     Class provclass = provider instanceof Class ? (Class) provider
         : provider.getClass();
     Field[] fields = provclass.getFields();
     for (int j = 0; j < fields.length; ++j) {
       Field field = fields[j];
       try {
         int modifiers = field.getModifiers();
         if (!isPublicStatic(modifiers))
           continue;
         String fieldname = field.getName();
         if ((prefix ? fieldname.startsWith(xfix)
             : fieldname.endsWith(xfix)) && field.getType() == String.class) {
           strings.add(field.get(provider));
         }
       }
       catch (Throwable t) {
         Logger.log.fatal("Error reflecting for static names ", t);
       }
     }
   }
 }
