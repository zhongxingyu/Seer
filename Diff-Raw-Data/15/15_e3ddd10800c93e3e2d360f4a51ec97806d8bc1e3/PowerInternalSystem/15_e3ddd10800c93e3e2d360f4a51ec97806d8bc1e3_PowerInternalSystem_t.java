 package jpower.core.internal;
 
 import java.lang.instrument.Instrumentation;
 import java.lang.reflect.Field;
 import java.util.Vector;
 
 public class PowerInternalSystem {
     private static Instrumentation inst;
 
     public static void premain(String args, Instrumentation inst) {
         PowerInternalSystem.inst = inst;
     }
 
    @SuppressWarnings({"unchecked", "UseOfObsoleteCollectionType"})
     public static Class[] getLoadedClasses(ClassLoader loader) {
         try {
             Field f = ClassLoader.class.getDeclaredField("classes");
             f.setAccessible(true);
             Vector<Class> classList = (Vector<Class>) f.get(loader);
             return classList.toArray(new Class[classList.size()]);
         } catch (NoSuchFieldException | IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static Instrumentation getInstrumentation() {
         return inst;
     }
 }
