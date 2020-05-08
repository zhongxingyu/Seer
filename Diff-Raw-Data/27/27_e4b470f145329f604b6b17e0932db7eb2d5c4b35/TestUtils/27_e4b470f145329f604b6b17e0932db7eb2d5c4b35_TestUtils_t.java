 package org.app.test;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ObjectUtils;
 
 public abstract class TestUtils {
 
     @SuppressWarnings("unchecked")
     public static void assertObjectEqual(Object expected, Object actural) {
         try {
             Map<String, Object> srcMap = BeanUtils.describe(actural);
             Set<String> srckeys = srcMap.keySet();
             Map<String, Object> dstMap = BeanUtils.describe(expected);
             Set<String> dstkeys = dstMap.keySet();
             if (srckeys.size() != dstkeys.size()) {
                throw new AssertionError("actural class is not the sample class type as exptected class.");
             }
             for (String dstkey : dstkeys) {
                 Object srcObj = srcMap.get(dstkey);
                 Object dstObj = dstMap.get(dstkey);
                if(ObjectUtils.notEqual(srcObj, dstObj)){
                    throw new AssertionError("some property are not equal.");
                }
             }
 
         } catch (Exception e) {
            throw new AssertionError("unsupport operation.");
         }
     }
 }
