 package net.bodz.bas.c.type;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import net.bodz.bas.t.iterator.Iterables;
 import net.bodz.bas.t.preorder.testtype.*;
 
 public class TypeVectorPoMapTest
         extends Assert {
 
     static Class<?>[] r(Class<?>... classes) {
         return classes;
     }
 
     Map<Class<?>[], Object> orig = new HashMap<Class<?>[], Object>();
     {
         orig.put(r(Cat.class, Dog.class), "Cat, Dog");
         orig.put(r(C.class, Dog.class), "C, Dog");
         orig.put(r(Cat.class, D.class), "Cat, D");
         orig.put(r(C.class, D.class), "C, D");
         orig.put(r(CI.class, Dx.class), "CI, Dx");
         orig.put(r(CI.class, DxKI.class), "CI, DxKI");
         orig.put(r(CJz.class, DxKI.class), "CJz, DxKI");
         orig.put(r(CJz.class, DIJy.class), "CJz, DIJy");
         orig.put(r(CJzKI.class, DIJ.class), "CJzKI, DIJ");
     }
 
     static String joinSimpleName(Class<?>[] classes) {
         StringBuilder buf = null;
         for (Class<?> c : classes) {
             if (buf == null)
                 buf = new StringBuilder(classes.length * 30);
             else
                 buf.append(", ");
             buf.append(c.getSimpleName());
         }
        return buf.toString();
     }
 
     void dumpOrig() {
         System.out.println("Orig entry order: ");
         for (Entry<Class<?>[], Object> e : orig.entrySet()) {
             Class<?>[] key = e.getKey();
             System.out.println(joinSimpleName(key));
         }
         System.out.println();
     }
 
     @Test
     public void test1() {
         TypeVectorPoMap<Object> map = new TypeVectorPoMap<Object>();
         map.putAll(orig);
 
         // dump entry order
         for (Entry<Class<?>[], Object> e : map.entrySet()) {
             Class<?>[] key = e.getKey();
             System.out.println(joinSimpleName(key));
         }
 
         List<Object> children;
 
         assertEquals("CI, DxKI", map.meet(r(CI.class, DxKIx.class)));
 
         children = Iterables.toList(map.join(r(CI.class, D.class)));
         assertEquals(2, children.size());
         assertEquals("CI, Dx", children.get(0));
         assertEquals("CI, DxKI", children.get(1));
 
         children = Iterables.toList(map.join(r(CJ.class, I.class)));
         assertEquals(3, children.size());
         // DIJy extends DIJ, but CJz order take precedence.
         assertEquals("CJz, DIJy", children.get(0)); // DIJy
         assertEquals("CJz, DxKI", children.get(1));
         assertEquals("CJzKI, DIJ", children.get(2)); // DIJ
 
         assertEquals(null, map.meet(r(CJzKI.class, Dx.class)));
     }
 
 }
