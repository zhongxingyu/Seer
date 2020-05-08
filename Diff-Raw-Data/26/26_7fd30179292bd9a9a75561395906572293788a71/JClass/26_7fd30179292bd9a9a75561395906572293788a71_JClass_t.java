 package org.unbunt.ella.lang;
 
 import org.unbunt.ella.engine.context.Context;
 import org.unbunt.ella.engine.corelang.*;
 import org.unbunt.ella.exception.ClosureTerminatedException;
 import org.unbunt.ella.exception.EllaRuntimeException;
 
 import java.lang.reflect.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Represents an EllaScript object wrapping a Java class.
  */
 public class JClass extends AbstractObj implements NativeObj {
     /**
      * The wrapped class.
      */
     public final Class<?> clazz;
 
     protected static final Call NATIVE_CONSTRUCTOR = new NativeCall() {
         public Obj call(Engine engine, Obj context, Obj[] args) throws ClosureTerminatedException {
            Object result;
             Class<?> cls = ((JClass) context).clazz;
 
             if (args.length == 0) {
                 try {
                     return new JObject(cls.getConstructor().newInstance());
                 } catch (NoSuchMethodException e) {
                     throw new EllaRuntimeException(e);
                 } catch (InvocationTargetException e) {
                     throw ReflectionUtils.wrapInvocationTargetException(e);
                 } catch (IllegalAccessException e) {
                     throw new EllaRuntimeException(e);
                 } catch (InstantiationException e) {
                     throw new EllaRuntimeException(e);
                 }
             }
 
             Object[] jargs = NativeWrapper.unwrap(args);
 
             Constructor ctor =
                     ReflectionUtils.findMatchingConstructor(cls.getConstructors(),
                                                             ReflectionUtils.getArgTypes(jargs, args),
                                                             jargs);
 
             if (ctor == null) {
                 throw new EllaRuntimeException("No such constructor");
             }
 
             try {
                 result = ctor.newInstance(NativeWrapper.unwrap(args));
             } catch (InstantiationException e) {
                 throw new EllaRuntimeException(e);
             } catch (IllegalAccessException e) {
                 throw new EllaRuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw ReflectionUtils.wrapInvocationTargetException(e);
             }
 
            return result instanceof Obj ? (Obj) result : new JObject(result);
         }
     };
 
     /**
      * Creates a new JClass wrapping the specified class.
      *
      * @param clazz the class to wrap.
      */
     public JClass(Class<?> clazz) {
         this.clazz = clazz;
     }
 
     protected static final int OBJECT_ID = ProtoRegistry.generateObjectID();
 
     public int getObjectID() {
         return OBJECT_ID;
     }
 
     /**
      * Registers this EllaScript object within the given execution context.
      *
      * @param ctx the execution context to register this object in.
      */
     public static void registerInContext(Context ctx) {
         JClassProto.registerInContext(ctx);
         ctx.registerProto(OBJECT_ID, JClassProto.OBJECT_ID);
     }
 
     public Call getNativeConstructor() {
         return NATIVE_CONSTRUCTOR;
     }
 
     @Override
     public Obj getSlot(Context ctx, Obj key) {
         Obj value = slots.get(key);
         if (value != null) {
             return value;
         }
 
         String keyStr = key.toString();
 
         // try to load method named exactly like the slot
         List<Method> directMatches = ReflectionUtils.getStaticMethods(clazz, keyStr);
         if (!directMatches.isEmpty()) {
             JMethod method = new JMethod(directMatches.toArray(new Method[directMatches.size()]));
             slots.put(key, method);
             return method;
         }
 
         // interpret given slot name as property name and try to find and invoke corresponding getter method
         String propStr = ReflectionUtils.getterFromProperty(keyStr);
         Method getter = null;
         try {
             getter = clazz.getMethod(propStr, (Class[])null);
             if (!Modifier.isStatic(getter.getModifiers())) {
                 getter = null;
             }
         } catch (NoSuchMethodException ignored) {
         }
         if (getter != null) {
             try {
                 return ReflectionUtils.invokeMethod(ctx, getter, null, (Object[])null);
             } catch (IllegalAccessException ignored) {
                 // access to getter denied by vm -> act as if no getter method was found;
             } catch (InvocationTargetException e) {
                 // exception thrown by getter -> re-throw as script exception
                 throw ReflectionUtils.wrapInvocationTargetException(e);
             }
         }
 
         // try to find property named like the slot
         try {
             Field field = clazz.getField(keyStr);
             if (Modifier.isStatic(field.getModifiers())) {
                 Object fieldValue = field.get(null);
                 return NativeWrapper.wrap(ctx, fieldValue);
             }
         } catch (NoSuchFieldException ignored) {
         } catch (IllegalAccessException ignored) {
         }
 
         return null;
     }
 
     @Override
     public Obj setSlot(Context ctx, Obj key, Obj val) {
         if (slots.containsKey(key)) {
             slots.put(key, val);
             return this;
         }
 
         String keyStr = key.toString();
         Object jvalue = val.toJavaObject();
 
         // interpret given slot name as property name and try to find and invoke corresponding setter method
         String propStr = ReflectionUtils.setterFromProperty(keyStr);
         Method[] methods = clazz.getMethods();
         Method[] staticMethods = new Method[methods.length];
         for (int i = 0; i < staticMethods.length; i++) {
             Method m = staticMethods[i];
             int flags = m.getModifiers();
             if (Modifier.isStatic(flags)) {
                 staticMethods[i] = m;
             }
         }
         Method setter = ReflectionUtils.findMatchingMethod(staticMethods, propStr,
                                                            new Class[] {
                                                                    jvalue == null ? null : jvalue.getClass()
                                                            },
                                                            new Object[] { jvalue });
         if (setter != null) {
             try {
                 ReflectionUtils.invokeMethod(ctx, setter, null, jvalue);
             } catch (IllegalAccessException ignored) {
                 // access to setter denied by vm -> act as if no setter method was found;
             } catch (InvocationTargetException e) {
                 // exception thrown by setter -> re-throw as script exception
                 throw ReflectionUtils.wrapInvocationTargetException(e);
             }
         }
 
         // try to find property named like the slot and set it's value
         try {
             Field field = clazz.getField(keyStr);
             int flags = field.getModifiers();
             if (Modifier.isStatic(flags)) {
                 field.set(null, val);
                 return this;
             }
         } catch (NoSuchFieldException ignored) {
         } catch (IllegalAccessException ignored) {
         }
 
         // fall back to using normal script object property (slot)
         slots.put(key, val);
 
         return this;
     }
 
     // TODO: Merge with JObject.getSlots()
     // TODO: filter out non-static methods
     @Override
     public Map<Obj, Obj> getSlots() {
         Map<Obj, Obj> result = new HashMap<Obj, Obj>();
         Map<String, List<Method>> methodsByName = new HashMap<String, List<Method>>();
         Method[] methods = clazz.getMethods();
         for (Method method : methods) {
             String name = method.getName();
             List<Method> namedMethods = methodsByName.get(name);
             if (namedMethods == null) {
                 namedMethods = new ArrayList<Method>();
                 methodsByName.put(name, namedMethods);
             }
             namedMethods.add(method);
         }
         for (Map.Entry<String, List<Method>> entry : methodsByName.entrySet()) {
             List<Method> namedMethods = entry.getValue();
             String name = entry.getKey();
             result.put(
                     new Str(name),
                     new JMethod(namedMethods.toArray(new Method[namedMethods.size()]))
             );
         }
         result.putAll(slots);
         return result;
     }
 
     @Override
     public Class toJavaObject() {
         return clazz;
     }
 
     public String toString() {
         return "[JClass " + clazz.toString() + "]";
     }
 
     /**
      * Represents the implicit parent object for JClass objects.
      */
     public static class JClassProto extends AbstractObj implements NativeObj {
         protected static final Call NATIVE_CONSTRUCTOR = new NativeCall() {
             public Obj call(Engine engine, Obj context, Obj[] args) throws ClosureTerminatedException {
                 ClassLoader loader = engine.getClass().getClassLoader();
                 try {
                     Class cls = loader.loadClass(args[0].toString());
                     return new JClass(cls);
                 } catch (ClassNotFoundException e) {
                     throw new EllaRuntimeException(e);
                 }
             }
         };
 
         private JClassProto() {
         }
 
         public static final int OBJECT_ID = ProtoRegistry.generateObjectID();
 
         public int getObjectID() {
             return OBJECT_ID;
         }
 
         /**
          * Registers this EllaScript object within the given execution context.
          *
          * @param ctx the execution context to register this object in.
          */
         public static void registerInContext(Context ctx) {
             Base.registerInContext(ctx);
             ctx.registerProto(OBJECT_ID, Base.OBJECT_ID);
             if (!ctx.hasObject(OBJECT_ID)) {
                 ctx.registerObject(new JClassProto());
             }
         }
 
         public Call getNativeConstructor() {
             return NATIVE_CONSTRUCTOR;
         }
     }
 }
