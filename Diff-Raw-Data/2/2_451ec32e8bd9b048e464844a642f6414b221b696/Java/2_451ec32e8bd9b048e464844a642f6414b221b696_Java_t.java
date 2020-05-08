 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import static java.lang.reflect.Modifier.isPublic;
 import static java.lang.reflect.Modifier.isStatic;
 
 import java.lang.reflect.AnnotatedElement;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 
 import org.joe_e.Powerless;
 import org.joe_e.Struct;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.reflect.Reflection;
 
 /**
  * Java &lt;=&gt; JSON naming conventions.
  */
 public final class
 Java {
 
     private
     Java() {}
     
     /**
      * Gets the corresponding property name.
      * <p>
      * This method implements the standard Java beans naming conventions.
      * </p>
      * @param method    candidate method
      * @return name, or null if the method is not a property accessor
      */
     static public String
     property(final Method method) {
         final String name = method.getName();
         String r =
             name.startsWith("get") &&
             (name.length() == "get".length() ||
              Character.isUpperCase(name.charAt("get".length()))) &&
             method.getParameterTypes().length == 0
                 ? name.substring("get".length())
             : (name.startsWith("is") &&
                (name.length() != "is".length() ||
                 Character.isUpperCase(name.charAt("is".length()))) &&
                method.getParameterTypes().length == 0
                 ? name.substring("is".length())
             : null);
         if (null != r && 0 != r.length() &&
                 (1 == r.length() || !Character.isUpperCase(r.charAt(1)))) {
             r = Character.toLowerCase(r.charAt(0)) + r.substring(1);
         }
         return r;
     }
     
     /**
      * synthetic modifier
      */
     static private final int synthetic = 0x1000;
     
     /**
      * Is the synthetic flag set?
      * @param flags Java modifiers
      * @return <code>true</code> if synthetic, else <code>false</code>
      */
     static protected boolean
     isSynthetic(final int flags) { return 0 != (flags & synthetic); }
 
     /**
      * Finds a named instance member.
      * @param type  class to search
      * @param name  member name
      * @return corresponding member, or <code>null</code> if not found
      */
     static public Method
     dispatch(final Class<?> type, final String name) {
     	Method r = null;
         for (final Method m : Reflection.methods(type)) {
             final int flags = m.getModifiers();
             if (!isStatic(flags) && !isSynthetic(flags)) {
                 String mn = property(m);
                 if (null == mn) {
                     mn = m.getName();
                 }
                 if (name.equals(mn)) {
                     if (null != r) { return null; }
                     r = m;
                 }
             }
         }
         return r;
     }
 
     /**
      * Finds the first invocable declaration of a public method.
      */
     static public Method
     bubble(final Method method) {
         final Class<?> declarer = method.getDeclaringClass();
         if (isPublic(declarer.getModifiers())) { return method; }
         final String name = method.getName();
         final Class<?>[] param = method.getParameterTypes();
         for (final Class<?> i : declarer.getInterfaces()) {
             try {
                 final Method r = bubble(Reflection.method(i, name, param));
                 if (null != r) { return r; }
             } catch (final NoSuchMethodException e) {}
         }
         final Class<?> parent = declarer.getSuperclass();
         if (null != parent) {
             try {
                 final Method r = bubble(Reflection.method(parent, name, param));
                 if (null != r) { return r; }
             } catch (final NoSuchMethodException e) {}
         }
         return null;
     }
     
     /**
      * Is the given type a pass-by-construction type?
      * @param type  candidate type
      * @return <code>true</code> if pass-by-construction,
      *         else <code>false</code>
      */
     static public boolean
     isPBC(final Class<?> type) {
         return String.class == type ||
             Void.class == type ||
             Integer.class == type ||
             Long.class == type ||
             Boolean.class == type ||
             java.math.BigInteger.class == type ||
             Byte.class == type ||
             Short.class == type ||
             Character.class == type ||
             Double.class == type ||
             Float.class == type ||
             java.math.BigDecimal.class == type ||
             org.web_send.Entity.class == type ||
             org.ref_send.Record.class.isAssignableFrom(type) ||
             Throwable.class.isAssignableFrom(type) ||
             org.joe_e.array.ConstArray.class.isAssignableFrom(type) ||
             org.ref_send.promise.Volatile.class.isAssignableFrom(type) ||
             Type.class.isAssignableFrom(type) ||
             AnnotatedElement.class.isAssignableFrom(type);
     }
     
     static private final class
     Alias extends Struct implements Powerless {
         final Class<?> type;
         final String name;
         
         Alias(final Class<?> type, final String name) {
             this.type = type;
             this.name = name;
         }
     }
     
     /**
      * custom typenames
      */
     static private final PowerlessArray<Alias> custom = PowerlessArray.array(
         new Alias(Object.class, "object"),
         new Alias(String.class, "string"),
         new Alias(Number.class, "number"),
         new Alias(RuntimeException.class, "Error"),
         new Alias(Method.class, "function"),
         new Alias(Type.class, "class"),
         new Alias(ClassCastException.class, "org.ref_send.Forgery"),
         new Alias(NullPointerException.class,
                   "org.ref_send.promise.Indeterminate"),
        new Alias(ArithmeticException.class, "BadArithmetic"),
         new Alias(Runnable.class, "org.ref_send.Action"),
         new Alias(org.joe_e.array.ConstArray.class, "array")
     );
     
     static protected String
     name(final Class<?> type) throws IllegalArgumentException {
         for (final Alias a : custom) {
             if (type == a.type) { return a.name; }
         }
         return Reflection.getName(type).replace('$', '-');
     }
     
     static protected Class<?>
     load(final ClassLoader code,
          final String name) throws ClassNotFoundException {
         for (final Alias a : custom) {
             if (a.name.equals(name)) { return a.type; }
         }
         return "boolean".equals(name)
             ? boolean.class
         : "byte".equals(name)
             ? byte.class
         : "char".equals(name)
             ? char.class
         : "double".equals(name)
             ? double.class
         : "float".equals(name)
             ? float.class
         : "int".equals(name)
             ? int.class
         : "long".equals(name)
             ? long.class
         : "short".equals(name)
             ? short.class
         : "void".equals(name)
             ? void.class
         : code.loadClass(name.replace('-', '$'));
     }
 }
