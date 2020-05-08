 package nerot;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.lang.reflect.*;
 import java.util.Date;
 
 /**
  * A task that uses reflection to call an object generically and store the results in the Store.
  */
 public class GenericTask extends BaseTask {
     
     private static final Log LOG = LogFactory.getLog(GenericTask.class);
 
     private String key;
     private Object actor;
     private String method;
     private Object[] args;
 
     /**
      * Execute the generic task using reflection. Supports static and dynamic method execution.
      */
     public void execute() {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Executing GenericTask. key='" + key + "', actor='" + actor + "', method='" + method + "'. args='" + arrayToString(args) + "'");
             }
             
             Object retobj = null;
             if (actor instanceof Class) {
                 Method m = getMethodObject((Class) actor);
                 m.setAccessible(true);
                 retobj = m.invoke(m, args);
             } else {
                 Method m = getMethodObject(actor.getClass());
                 m.setAccessible(true);
                 retobj = m.invoke(actor, args);
             }
             getStore().set(key, retobj);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("stored object for key: " + key);
             }
         } catch (Throwable t) {
             LOG.error("failed to set object for key: " + key, t);
         }
     }
 
     private Method getMethodObject(Class c) throws Throwable {
         int found = 0;
         Method result = null;
         Method[] methods = c.getDeclaredMethods();
         for (int i = 0; i < methods.length; i++) {
             Method method = methods[i];
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Checking reflected method name '" + method.getName() + "' vs. supplied method name '" + getMethod() + "'");
             }
             
             if (method.getName().equals(getMethod()) && isEquivalent(toClassArray(args), method.getParameterTypes())) {
                 if (result != null) {
                     found++;
                 }
                 result = method;
                 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Matched method '" + method + "'");
                 }
             }
         }
 
         if (found > 1) {
             LOG.warn("" + found + " method signatures matched specified method!");
         }
 
         return result;
     }
 
     private Class[] toClassArray(Object[] o) {
         Class[] result = null;
         if (o != null) {
             result = new Class[o.length];
             for (int i = 0; i < o.length; i++) {
                 Object obj = o[i];
                 if (obj != null) {
                     result[i] = obj.getClass();
                 }
             }
         }
         
         if (LOG.isDebugEnabled()) {
             LOG.debug("toClassArray results: " + arrayToString(result));
         }
 
         return result;
     }
 
    private String arrayToString(Object[] o, StringBuffer sb) {
         StringBuffer sb = new StringBuffer();
         if (o != null) {
             sb.append("[");
             for (int i=0; i<o.length; i++) {
                 if (i!=0) {
                     sb.append(",");
                 }
                 sb.append(o[i]);
             }
             sb.append("]");
         }
         else {
             sb.append(o);
         }
         return sb.toString();
     }
 
     private boolean isEquivalent(Class[] c1, Class[] c2) {
         if ((c1 == null || c1.length == 0) && (c2 == null || c2.length == 0)) {
             return true;
         } else if (c1 != null && c2 != null && c1.length == c2.length) {
             for (int i = 0; i < c1.length; i++) {
                 Class c1c = toPrimitiveIfWrapperClass(c1[i]);
                 Class c2c = toPrimitiveIfWrapperClass(c2[i]);
                 if (!c1c.getName().equals(c2c.getName())) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     private Class toPrimitiveIfWrapperClass(Class c) {
         Class result = c;
         if (c == Byte.class) {
             result = byte.class;
         } else if (c == Short.class) {
             result = short.class;
         } else if (c == Integer.class) {
             result = int.class;
         } else if (c == Long.class) {
             result = long.class;
         } else if (c == Float.class) {
             result = float.class;
         } else if (c == Double.class) {
             result = double.class;
         } else if (c == Boolean.class) {
             result = boolean.class;
         } else if (c == Character.class) {
             result = char.class;
         }
         return result;
     }
 
     /**
      * Key for the value stored in Nerot's store.
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Set key for the return Object in Nerot's store.
      */
     public void setKey(String key) {
         this.key = key;
     }
 
     /**
      * Get the instance or class to call.
      */
     public Object getActor() {
         return actor;
     }
 
     /**
      * Set the instance or class to call.
      */
     public void setActor(Object actor) {
         this.actor = actor;
     }
 
     /**
      * Get the method name to call on the actor.
      */
     public String getMethod() {
         return method;
     }
 
     /**
      * Set the method name to call on the actor.
      */
     public void setMethod(String method) {
         this.method = method;
     }
 
     /**
      * Get the arguments to use when calling specified method on the actor.
      */
     public Object[] getArgs() {
         return args;
     }
 
     /**
      * Set the arguments to use when calling specified method on the actor.
      */
     public void setArgs(Object[] args) {
         this.args = args;
     }
 
     /**
      * Set the arguments to use when calling method on the actor.
      */
     public void setActor(Object[] args) {
         this.args = args;
     }
 }
