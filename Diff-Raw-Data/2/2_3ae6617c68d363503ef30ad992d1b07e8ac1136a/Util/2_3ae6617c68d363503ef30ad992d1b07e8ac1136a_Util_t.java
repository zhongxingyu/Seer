 package org.bisanti.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * <i>
  * Written and authored by Jason Bisanti. Free to use and reproduce, but please
  * keep my name as the original author!
  * <br><br></i>
  * @author Jason Bisanti
  */
 public final class Util
 {
     private Util(){};
     
     public static boolean containsNull(Object obj, Object... others)
     {
         if(obj == null)
         {
             return true;
         }
 
         for (Object other : others)
         {
             if (other == null)
             {
                 return true;
             }
         }        
         
         return false;
     }
     
     public static boolean equal(Object obj1, Object obj2)
     {
         return obj1 == null ? obj2 == null : obj1.equals(obj2);
     }
     
     public static boolean equalValues(Number num1, Number num2)
     {
         if(containsNull(num1, num2))
         {
             return num1 == num2;
         }
         else
         {
             return num1.doubleValue() == num2.doubleValue();
         }
     }
     
     public static boolean isNullOrEmpty(Collection collection)
     {
         return collection == null || collection.isEmpty();
     }
     
     public static boolean isNullOrEmpty(Map map)
     {
         return map == null || map.isEmpty();
     }
     
     public static boolean isNullOrEmpty(Object[] array)
     {
         return array == null || array.length == 0;
     }
     
     public static <T> List<T> asList(Iterator<T> iterator)
     {
         List<T> list = new ArrayList<T>();
         while(iterator.hasNext())
         {
             list.add(iterator.next());
         }
         return list;
     }
     
     public static <T> List<T> asList(Enumeration<T> enumeration)
     {
         List<T> list = new ArrayList<T>();
         while(enumeration.hasMoreElements())
         {
             list.add(enumeration.nextElement());
         }
         return list;
     }
     
     public static boolean equalCollections(boolean considerOrder, Collection col1, Collection col2)
     {
         if(containsNull(col1, col2))
         {
             return col1 == col2;
         }
         else if(considerOrder)
         {
             if(col1.size() == col2.size())
             {
                 Iterator it1 = col1.iterator();
                 Iterator it2 = col2.iterator();
                 while(it1.hasNext())
                 {
                     if(!it1.next().equals(it2.next()))
                     {
                         return false;
                     }
                 }
                 
                 return true;
             }
             else
             {            
                 return false;
             }
         }
         else
         {
            return col1 == col2 || (col1.size() == col2.size() && col1.containsAll(col2) && col2.containsAll(col1));
         }
     }
     
     public static boolean equalMaps(boolean considerOrder, Map map1, Map map2)
     {
         if(containsNull(map1, map2))
         {
             return map1 == map2;
         }
         else if(considerOrder)
         {
             if(map1.size() == map2.size())
             {
                 Iterator<Map.Entry> it1 = map1.entrySet().iterator();
                 Iterator<Map.Entry> it2 = map2.entrySet().iterator();
                 while(it1.hasNext())
                 {
                     Map.Entry entry1 = it1.next();
                     Map.Entry entry2 = it2.next();
                     if(!equal(entry1.getKey(), entry2.getKey()) ||
                        !equal(entry1.getValue(), entry2.getValue()))
                     {
                         return false;
                     }
                 }
                 
                 return true;
             }
             else
             {
                 return false;
             }
         }
         else
         {
             Set<Map.Entry> entries = map1.entrySet();
             for(Map.Entry entry: entries)
             {
                 if(!equal(entry.getValue(), map2.get(entry.getKey())))
                 {
                     return false;
                 }
             }
             
             return true;
         }
     }
     
 }
