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
            	Object obj = new Object(); //broken, just getting to compile
                 if(a.annotationType().equals(obj)) {
                     found = true;
                     break;
                 }
             }
            //m. //This code is broken, just making it so that it can compile
                 //for the 10.4 release ~PiPeep
             if(found) newMethods.add(m);
         }
         return null;
     }
 }
