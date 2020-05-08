 /*
  * Created on Sep 22, 2004
  */
 package uk.org.ponder.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import uk.org.ponder.arrayutil.ArrayEnumeration;
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.reflect.ReflectUtils;
 import uk.org.ponder.reflect.ReflectiveCache;
 
 /**
  * Utilities for converting Java multiple-valued types (arrays, collections,
  * Maps) into readable (Enumeration) and writeable (Denumeration) iterators over
  * their contents.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class EnumerationConverter {
   public static boolean isObjectArray(Class c) {
     if (!c.isArray())
       return false;
     return !c.getComponentType().isPrimitive();
   }
 
   public static boolean isEnumerable(Class c) {
     return Enumeration.class.isAssignableFrom(c)
         || Collection.class.isAssignableFrom(c)
         || Map.class.isAssignableFrom(c) || Iterator.class.isAssignableFrom(c)
         || isObjectArray(c);
   }
 
   // Maps are not currently denumerable! Pending some scheme for
   // getting at their keys.
   public static boolean isDenumerable(Class c) {
     return /* c != Collection.class && */Collection.class.isAssignableFrom(c)
         && !isMappable(c) || c.isArray();
   }
 
   public static boolean isMappable(Class c) {
     return Map.class.isAssignableFrom(c)
         || BeanLocator.class.isAssignableFrom(c);
   }
 
   public static Map getMap(Object o) {
     if (o instanceof Map) {
       return (Map) o;
     }
     throw new AssertionException("getMap called for unmappable type "
         + o.getClass());
   }
 
   public static int getEnumerableSize(Object o) {
     if (o instanceof Collection) {
       return ((Collection) o).size();
     }
     else if (o.getClass().isArray()) {
       return ((Object[]) o).length;
     }
    return 1;
   }
 
   /**
    * Return an enumeration of the contents of the argument o, which may itself
    * be already either an Enumeration or Iterator, Collection or Array type. If
    * a Map, return an enumeration of its values, and in the default case, return
    * an enumeration consisting of just the single argument.
    */
   public static Enumeration getEnumeration(final Object o) {
     if (o instanceof Enumeration) {
       return (Enumeration) o;
     }
     else if (o instanceof Iterator) {
       return new Enumeration() {
         public boolean hasMoreElements() {
           return ((Iterator) o).hasNext();
         }
 
         public Object nextElement() {
           return ((Iterator) o).next();
         }
       };
     }
     else if (o instanceof Collection) {
       return Collections.enumeration((Collection) o);
     }
     else if (o instanceof Map) {
       return Collections.enumeration(((Map) o).values());
     }
     else if (o.getClass().isArray()) {
       return new ArrayEnumeration((Object[]) o);
     }
     else
       return new SingleEnumeration(o);
   }
 
   public static Denumeration getDenumeration(final Object collo,
       final ReflectiveCache cache) {
     if (collo instanceof Collection) {
       return new Denumeration() {
         public void add(Object o) {
           ((Collection) collo).add(o);
         }
 
         public boolean remove(Object o) {
           return ((Collection) collo).remove(o);
         }
       };
     }
 
     else if (collo.getClass().isArray()) {
       final Object[] coll = (Object[]) collo;
       if (coll.length == 0) {
         final List buildup = new ArrayList();
         return new CompletableDenumeration() {
           public Object complete() {
             return buildup.toArray((Object[]) ReflectUtils
                 .instantiateContainer(coll.getClass(), buildup.size(), cache));
           }
 
           public void add(Object o) {
             buildup.add(o);
           }
 
           public boolean remove(Object o) {
             return buildup.remove(o);
           }
 
         };
       }
       else {
         // TODO: use CompletableDenumeration here to allow "extensible" arrays.
         return new Denumeration() {
           int index = 0;
 
           public void add(Object o) {
             coll[index++] = o;
           }
 
           public boolean remove(Object o) {
             throw new UniversalRuntimeException(
                 "Removal not supported from Array denumerable");
           }
 
         };
       }
     }
     return null;
   }
 
 }
