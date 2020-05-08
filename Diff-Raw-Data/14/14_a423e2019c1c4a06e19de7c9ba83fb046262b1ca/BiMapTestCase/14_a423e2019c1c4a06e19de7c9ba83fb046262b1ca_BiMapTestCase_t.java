 package org.openblend.guava101.test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.openblend.guava101.support.Constants;
 import org.openblend.guava101.support.Person;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 @SuppressWarnings("deprecation")
 public class BiMapTestCase {
     @Test
     public void testBefore() throws Exception {
         Map<String, Person> map = new HashMap<String, Person>();
        put(map, Constants.VRHNIKA, Constants.ALES);
        put(map, Constants.LJUBLJANA, Constants.ANDY);
         Map<Person, String> inverse = new HashMap<Person, String>();
         for (Map.Entry<String, Person> entry : map.entrySet()) {
             inverse.put(entry.getValue(), entry.getKey());
         }
         Assert.assertEquals(Constants.VRHNIKA, inverse.get(Constants.ALES));
     }
 
    protected static void put(Map<String, Person> map, String key, Person value) {
        if (map.containsValue(value))
            throw new IllegalArgumentException("Dup value: " + value);
        map.put(key, value);
    }

     @Test
     public void testAfter() throws Exception {
         BiMap<String, Person> map = HashBiMap.create();
         map.put(Constants.VRHNIKA, Constants.ALES);
         map.put(Constants.LJUBLJANA, Constants.ANDY);
         Assert.assertEquals(Constants.VRHNIKA, map.inverse().get(Constants.ALES));
     }
 }
