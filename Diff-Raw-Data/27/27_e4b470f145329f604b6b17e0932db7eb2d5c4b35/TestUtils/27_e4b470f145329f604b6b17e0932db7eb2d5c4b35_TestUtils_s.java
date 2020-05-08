 package org.app.test;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
 
 public abstract class TestUtils {
 
     @SuppressWarnings("unchecked")
     public static void assertObjectEqual(Object expected, Object actural) {
         try {
             Map<String, Object> srcMap = BeanUtils.describe(actural);
             Set<String> srckeys = srcMap.keySet();
             Map<String, Object> dstMap = BeanUtils.describe(expected);
             Set<String> dstkeys = dstMap.keySet();
             if (srckeys.size() != dstkeys.size()) {
                Assert.fail("actural class is not the sample class type as exptected class.");
             }
             for (String dstkey : dstkeys) {
                 Object srcObj = srcMap.get(dstkey);
                 Object dstObj = dstMap.get(dstkey);
                Assert.assertEquals("assert failed within " + srcObj, dstObj, srcObj);
             }
 
         } catch (Exception e) {
            Assert.fail("unsupport operation.");
         }
     }
 }
