 package org.unbunt.ella.lang;
 
 import org.unbunt.ella.engine.context.Context;
 import org.unbunt.ella.engine.corelang.Obj;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
import java.lang.reflect.Method;
 
 /**
  * Helper class providing methods for wrapping Java objects in EllaScript objects an vice versa.
  */
 public class NativeWrapper {
     public static Obj wrap(int i) {
         return new NNum(i);
     }
 
     public static Obj wrap(long l) {
         return new NNum(l);
     }
 
     /**
      * NOTE: We *have* to find a way to retrieve the context's singleton objects (like Null) without having callers
      *       pass the context object explicitly.
     *
     * @param ctx the execution context
     * @param o the Java object to wrap
     * @return an Ella object wrapping the given Java object
      */
     public static Obj wrap(Context ctx, Object o) {
         if (o == null) {
             return ctx.getObjNull();
         }
         else if (o instanceof Boolean) {
             return (Boolean) o ? ctx.getObjTrue() : ctx.getObjFalse();
         }
         else if (o instanceof Number) {
             if (o instanceof Integer || o instanceof Long || o instanceof Byte || o instanceof Short) {
                 return new NNum(((Number) o).longValue());
             }
             else if (o instanceof Double || o instanceof Float) {
                 return new NReal(((Number) o).doubleValue());
             }
             else if (o instanceof BigDecimal) {
                 return new NBigReal((BigDecimal) o);
             }
             else if (o instanceof BigInteger) {
                 return new NBigNum((BigInteger) o);
             }
         }
         else if (o instanceof String) {
             return new Str((String) o);
         }
         else if (o instanceof Character || o instanceof CharSequence) {
             return new Str(o.toString());
         }
        else if (o instanceof Object[]) {
            return new JArray(o, ((Object[]) o).length);
        }
        else if (o instanceof Class) {
            return new JClass((Class) o);
        }
        else if (o instanceof Method) {
            return new JMethod(new Method[] { (Method) o });
        }
         return new JObject(o);
     }
 
     public static Object[] unwrap(Obj[] objects) {
         Object[] jobjects = new Object[objects.length];
         for (int i = 0; i < objects.length; i++) {
             jobjects[i] = objects[i].toJavaObject();
         }
         return jobjects;
     }
 }
