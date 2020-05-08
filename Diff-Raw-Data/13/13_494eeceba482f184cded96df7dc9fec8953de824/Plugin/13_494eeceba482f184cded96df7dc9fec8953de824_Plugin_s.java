 package cbccore.plugins;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 public class Plugin<T> {
     public Method getNeeded() {
         Method[] methods = ((T)(this)).getClass().getDeclaredMethods();
         ArrayList<Method> newMethods = new ArrayList<Method>();
         for(Method m : methods) {
             Annotation[] annotations = m.getAnnotations();
             boolean found = false;
             for(Annotation a : annotations) {
                 if(a.annotationType().equals(obj)) {
                     found = true;
                     break;
                 }
             }
            m.
             if(found) newMethods.add(m);
         }
         return null;
     }
 }
