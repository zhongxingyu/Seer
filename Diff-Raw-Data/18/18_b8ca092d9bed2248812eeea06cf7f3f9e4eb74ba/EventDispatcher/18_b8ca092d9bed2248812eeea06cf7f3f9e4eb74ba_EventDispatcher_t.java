 package sofia.internal.events;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 //-------------------------------------------------------------------------
 /**
  * Represents a reflective dispatcher with an internal cache of looked-up
  * Method objects.
  *
  * @author  Tony Allevato, Stephen Edwards
  * @author  Last changed by $Author$
  * @version $Revision$, $Date$
  */
 public class EventDispatcher
 {
     //~ Fields ................................................................
 
     // The name of the method that this dispatcher calls.
     private String methodName;
 
     // A cache of matching method transformers for a particular class.
     private WeakHashMap<CacheKey,
         List<MethodTransformer>> transformerCache;
 
     private static final Map<Class<?>, Class<?>> wrapperEquivalent =
             new HashMap<Class<?>, Class<?>>();
 
     static {
         wrapperEquivalent.put(Boolean.class,   boolean.class);
         wrapperEquivalent.put(Byte.class,      byte.class);
         wrapperEquivalent.put(Character.class, char.class);
         wrapperEquivalent.put(Double.class,    double.class);
         wrapperEquivalent.put(Float.class,     float.class);
         wrapperEquivalent.put(Integer.class,   int.class);
         wrapperEquivalent.put(Short.class,     short.class);
 
         wrapperEquivalent.put(boolean.class,   Boolean.class);
         wrapperEquivalent.put(byte.class,      Byte.class);
         wrapperEquivalent.put(char.class,      Character.class);
         wrapperEquivalent.put(double.class,    Double.class);
         wrapperEquivalent.put(float.class,     Float.class);
         wrapperEquivalent.put(int.class,       Integer.class);
         wrapperEquivalent.put(short.class,     Short.class);
     }
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Creates a new event dispatcher with the specified method name.
      *
      * @param method the name of the method that this dispatcher will call
      */
     public EventDispatcher(String method)
     {
         methodName = method;
         transformerCache =
                 new WeakHashMap<CacheKey, List<MethodTransformer>>();
     }
 
 
     //~ Public methods ........................................................
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether a receiver has a method that satisfies
      * this dispatcher, given the specified arguments.
      *
      * @param receiver the receiver of the method call
      * @param args the arguments that would be passed to the method
      * @return true if the receiver has a method that satisfies this
      *     dispatcher, otherwise false
      */
     public boolean isSupportedBy(Object receiver, Object... args)
     {
         List<MethodTransformer> transformers =
                 getMethodTransformers(receiver, args);
 
         return !transformers.isEmpty();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Dispatches the event to the specified receiver, walking up the
      * containment hierarchy as needed to notify parents of the event.
      *
      * @param receiver the receiver of the method call
      * @param args the arguments that would be passed to the method
      * @return true if the event should not be dispatched further (because one
      *      of the handlers returned true), false if dispatch should continue
      */
     public boolean dispatch(Object receiver, Object... args)
     {
         List<MethodTransformer> transformers =
                 getMethodTransformers(receiver, args);
 
         if (!transformers.isEmpty())
         {
             for (MethodTransformer transformer : transformers)
             {
                 Object result = invokeTransformer(transformer, receiver, args);
 
                 if (Boolean.TRUE.equals(result))
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Transforms an argument list using the specified transformer and invokes
      * the method. This is a hook where subclasses can override the logic, if
      * necessary.
      *
      * @param transformer the transformer
      * @param receiver the receiving object
      * @param args the arguments to the method
      * @return the result of invoking the method
      */
     protected Object invokeTransformer(MethodTransformer transformer,
             Object receiver, Object... args)
     {
         return transformer.invoke(receiver, args);
     }
 
 
     // ----------------------------------------------------------
     /**
      * TODO document
      *
      * @param receiver
      * @param argTypes
      * @return
      */
     protected List<MethodTransformer> lookupTransformers(
             Object receiver, List<Class<?>> argTypes)
     {
         List<MethodTransformer> transformers =
                 new ArrayList<MethodTransformer>();
 
         Method method = lookupMethod(receiver, argTypes);
         if (method != null)
         {
             MethodTransformer identity = new MethodTransformer(
                     Arrays.asList(method.getParameterTypes()));
            identity.methodCache.put(receiver.getClass(), method);
             transformers.add(identity);
         }
 
         return transformers;
     }
 
 
     // ------------------------------------------------------
     protected Method lookupMethod(Object receiver, List<Class<?>> argTypes)
     {
         //System.out.println("Looking for "
         //		+ receiver.getClass().getCanonicalName() + "."
         //		+ methodName + "(" + argTypes.toString() + ")...");
 
         Class<?> clazz = receiver.getClass();
         Method bestMatch = null;
         int[] bestScore = new int[argTypes.size()];
         int[] nextScore = new int[argTypes.size()];
 
         while (clazz != null)
         {
             for (Method candidate : clazz.getDeclaredMethods())
             {
                 if (methodName.equals(candidate.getName()))
                 {
                     try
                     {
                         //System.out.println("   checking "
                         //    + candidate.toGenericString());
 
                         // Check this method and leave results in nextScore
                         scoreMethod(candidate, argTypes, nextScore);
 
                         if (bestMatch == null
                             || isBetter(bestScore, nextScore))
                         {
                             bestMatch = candidate;
 
                             // Rotate nextScore into the bestScore position
                             // then reuse the old bestScore array next iter.
                             int[] tmp = bestScore;
                             bestScore = nextScore;
                             nextScore = tmp;
                         }
                     }
                     catch (IllegalArgumentException e)
                     {
                         // This method isn't compatible with the
                         // given arguments, so ignore it.
                     }
                 }
             }
 
             clazz = clazz.getSuperclass();
         }
 
         if (bestMatch != null)
         {
             //System.out.println("   ...found!");
         }
 
         return bestMatch;
     }
 
 
     // ----------------------------------------------------------
     private List<Class<?>> classesForObjects(Object... objects)
     {
         List<Class<?>> types = new ArrayList<Class<?>>(objects.length);
 
         for (int i = 0; i < objects.length; i++)
         {
             types.add(objects[i].getClass());
         }
 
         return types;
     }
 
 
     // ----------------------------------------------------------
     private List<MethodTransformer> getMethodTransformers(Object receiver,
             Object... args)
     {
         List<MethodTransformer> transformers = null;
         CacheKey key = new CacheKey(receiver, args);
 
         if (!transformerCache.containsKey(key))
         {
             transformers = lookupTransformers(
                     receiver, key.getParameterTypes());
             transformerCache.put(key, transformers);
         }
         else
         {
             transformers = transformerCache.get(key);
         }
 
         return transformers;
     }
 
 
     // ----------------------------------------------------------
     private int argConversionCost(
         Class<?> actualParamType, Class<?> formalParamType)
         throws IllegalArgumentException
     {
         if (formalParamType.equals(actualParamType))
         {
             // Identical types
             return 0;
         }
 
         if (formalParamType.equals(wrapperEquivalent.get(actualParamType)))
         {
             // Treat auto-boxing/unboxing as free
             return 0;
         }
 
         if (formalParamType.isAssignableFrom(actualParamType))
         {
             // Calculate distance
             int distance = 1;
 
             outerLoop:
             while (actualParamType != null)
             {
                 if (formalParamType.equals(actualParamType.getSuperclass()))
                 {
                     return distance;
                 }
 
                 for (Class<?> iface : actualParamType.getInterfaces())
                 {
                     if (iface.equals(formalParamType))
                     {
                         return distance;
                     }
                 }
 
                 for (Class<?> iface : actualParamType.getInterfaces())
                 {
                     if (formalParamType.isAssignableFrom(iface))
                     {
                         actualParamType = iface;
                         distance++;
                         continue outerLoop;
                     }
                 }
 
                 actualParamType = actualParamType.getSuperclass();
                 distance++;
             }
 
             return distance;
         }
 
         throw new IllegalArgumentException("incompatible types");
     }
 
 
     // ----------------------------------------------------------
     private void scoreMethod(
         Method m, List<Class<?>> actualArgTypes, int[] scores)
         throws IllegalArgumentException
     {
         Class<?>[] formals = m.getParameterTypes();
         if (formals.length != actualArgTypes.size())
         {
             throw new IllegalArgumentException(
                 "incompatible number of arguments");
         }
 
         for (int i = 0; i < formals.length; i++)
         {
             scores[i] = argConversionCost(actualArgTypes.get(i), formals[i]);
         }
     }
 
 
     // ----------------------------------------------------------
     private boolean isBetter(int[] oldScore, int[] newScore)
     {
         int oldTotal = 0;
         for (int i : oldScore)
         {
             oldTotal += i;
         }
 
         int newTotal = 0;
         for (int i : newScore)
         {
             newTotal += i;
         }
 
         if (oldTotal != newTotal)
         {
             return newTotal < oldTotal;
         }
         else
         {
             for (int i = 0; i < oldScore.length; i++)
             {
                 if (oldScore[i] != newScore[i])
                 {
                     return newScore[i] < oldScore[i];
                 }
             }
             return false;
         }
     }
 
 
     //~ Inner classes .........................................................
 
     // ------------------------------------------------------
     /**
      * Subclasses of {@code EventDispatcher} should subclass this internally in
      * order to support multiple method signatures.
      */
     protected class MethodTransformer
     {
         //~ Fields ............................................................
 
        protected final List<Class<?>> argTypes;
        protected final Map<Class<?>, Method> methodCache;
 
 
         //~ Constructors ......................................................
 
         // ------------------------------------------------------
         public MethodTransformer(Class<?>... argTypes)
         {
             this(Arrays.asList(argTypes));
         }
 
 
         // ------------------------------------------------------
         public MethodTransformer(List<Class<?>> argTypes)
         {
             this.argTypes = argTypes;
            this.methodCache = new HashMap<Class<?>, Method>();
         }
 
 
         //~ Methods ...........................................................
 
         // ------------------------------------------------------
         public void addIfSupportedBy(Object receiver,
                 List<MethodTransformer> transformers)
         {
            Method method = lookupMethod(receiver, argTypes);
 
             if (method != null)
             {
                methodCache.put(receiver.getClass(), method);
                 transformers.add(this);
             }
         }
 
 
         // ------------------------------------------------------
         public boolean isCompatible(Object... args)
         {
             //System.out.println("---\nChecking compat of " + argTypes
             //		+ " and " + Arrays.toString(args));
 
             if (args.length != argTypes.size())
             {
                 //System.out.println("   Not the same number.");
                 return false;
             }
             else
             {
                 for (int i = 0; i < args.length; i++)
                 {
                     Class<?> formal = argTypes.get(i);
                     Class<?> actual = args[i].getClass();
 
                     if (!formal.isAssignableFrom(actual))
                     {
                         //System.out.println("   Not compatible.");
                         return false;
                     }
                 }
 
                 //System.out.println("   Compatible.");
                 return true;
             }
         }
 
 
         // ------------------------------------------------------
         public Object invoke(Object receiver, Object... args)
         {
             try
             {
                 //System.out.println("Invoking " + method.toGenericString()
                 //    + " with " + Arrays.toString(args));
                return methodCache.get(receiver.getClass()).invoke(
                        receiver, transform(args));
             }
             catch (InvocationTargetException e)
             {
                 Throwable cause = e.getCause();
 
                 if (cause instanceof Error)
                 {
                     throw (Error) cause;
                 }
                 else if (cause instanceof RuntimeException)
                 {
                     throw (RuntimeException) cause;
                 }
                 else
                 {
                     throw new RuntimeException(cause);
                 }
             }
             catch (IllegalAccessException e)
             {
                 throw new RuntimeException(e);
             }
         }
 
 
         // ----------------------------------------------------------
         /**
          * Transforms the specified argument list to one that will be passed to
          * the handler method. By default, it returns the same argument list.
          * Override this to transform the arguments (such as from a MotionEvent
          * to floats for x/y).
          *
          * @param args the arguments
          * @return the transformed argument list
          */
         protected Object[] transform(Object... args)
         {
             return args;
         }
     }
 
 
     // ----------------------------------------------------------
     private class CacheKey
     {
         private Class<?> receiverType;
         private List<Class<?>> argTypes;
 
 
         // ----------------------------------------------------------
         public CacheKey(Object receiver, Object... args)
         {
             receiverType = receiver.getClass();
             argTypes = classesForObjects(args);
         }
 
 
         // ----------------------------------------------------------
         @SuppressWarnings("unused")
         public Class<?> getReceiverType()
         {
             return receiverType;
         }
 
 
         // ----------------------------------------------------------
         public List<Class<?>> getParameterTypes()
         {
             return argTypes;
         }
 
 
         // ----------------------------------------------------------
         public boolean equals(Object other)
         {
             if (other instanceof CacheKey)
             {
                 CacheKey otherMethod = (CacheKey) other;
 
                 return receiverType.equals(otherMethod.receiverType) &&
                         argTypes.equals(otherMethod.argTypes);
             }
             else
             {
                 return false;
             }
         }
 
 
         // ----------------------------------------------------------
         public int hashCode()
         {
             return receiverType.hashCode() ^ (argTypes.hashCode() << 13);
         }
     }
 }
