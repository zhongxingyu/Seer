 package net.bodz.bas.c.type;
 
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 @Deprecated
 public class TypePoSetTest
         extends Assert {
 
     static String join(String delim, Set<?> set) {
         StringBuilder buf = null;
         for (Object o : set) {
             if (buf == null)
                 buf = new StringBuilder(set.size() * 30);
             else
                 buf.append(delim);
             buf.append(o);
         }
        if (buf == null)
            return "";
        else
            return buf.toString();
     }
 
     @Test
     public void test1() {
         TypePoSet set = new TypePoSet();
         set.add(Number.class);
         set.add(Object.class);
         set.add(String.class);
         System.out.println(join(", ", set));
         assertEquals(Number.class, set.floor(Float.class));
         assertEquals(Object.class, set.floor(List.class));
 
         set.add(Double.class);
         set.add(Integer.class);
         System.out.println(join(", ", set));
         assertEquals(Number.class, set.floor(Float.class));
         assertEquals(Object.class, set.floor(List.class));
     }
 
 }
