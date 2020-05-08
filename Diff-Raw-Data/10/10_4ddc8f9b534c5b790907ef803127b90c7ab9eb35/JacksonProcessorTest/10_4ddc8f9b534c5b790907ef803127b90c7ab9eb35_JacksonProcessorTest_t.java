 package org.nohope.jongo;
 
 import org.jongo.marshall.MarshallingException;
 import org.junit.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Date: 04.08.12
  * Time: 16:59
  */
 public class JacksonProcessorTest {
     @Test
     public void testMapKeyEscaping() {
         final Map<String, Object> map1 = generateMap();
 
         final JacksonProcessor proc = new JacksonProcessor();
         final String marshalled = proc.marshall(map1);
         final Object restored = proc.unmarshall(marshalled, Object.class);
 
         assertEquals(map1, restored);
     }
 
     @Test
     public void testIntegerMapEscaping() {
         final Map<Integer, Object> map1 = new HashMap<>();
         map1.put(1, "value.#pizda");
 
         final JacksonProcessor proc = new JacksonProcessor();
         final String marshalled = proc.marshall(map1);
         final Object restored = proc.unmarshall(marshalled, Object.class);
 
         assertEquals(map1, restored);
     }
 
     @Test
     public void testComplexMapEscaping() {
         final Map<Key, Object> map1 = new HashMap<>();
         map1.put(new Key("x"), "value.#pizda");
         map1.put(new Key("y"), "value.#pizda");
         map1.put(new Key("z"), "value.#pizda");
 
         final JacksonProcessor proc = new JacksonProcessor();
         final String marshalled = proc.marshall(map1);
         final Object restored = proc.unmarshall(marshalled, Object.class);
 
         assertEquals(map1, restored);
     }
 
     @Test(expected = MarshallingException.class)
     public void incorrectUnmarshalling() {
         final JacksonProcessor proc = new JacksonProcessor();
         proc.unmarshall("", Object.class);
     }
 
     @Test(expected = MarshallingException.class)
     public void incorrectMarshalling() {
        new JacksonProcessor().marshall(new Bean());
     }
 
     @Test
     @SuppressWarnings("ConstantConditions")
     public void nullChecks() {
         try {
             new JacksonProcessor().unmarshall("x", null);
             fail();
         } catch (final IllegalArgumentException e) {
         }
 
         try {
             new JacksonProcessor().unmarshall(null, Object.class);
             fail();
         } catch (final IllegalArgumentException e) {
         }
 
         try {
             new JacksonProcessor(null);
             fail();
         } catch (final IllegalArgumentException e) {
         }
     }
 
     @Test
     public void testEscaping() {
         final String source = "text hash1=# hash2=\\# hash3=\\\\# dot=. at=@ underscore=_ underscore2=\\_";
         final String escaped = JacksonProcessor.escape(source);
         assertEquals("text hash1=\\# hash2=\\\\# hash3=\\\\\\# dot=_ at=\\@ underscore=\\_ underscore2=\\\\_", escaped);
 
         final String restored = JacksonProcessor.unescape(escaped);
         assertEquals(source, restored);
     }
 
     static Map<String, Object> generateMap() {
         final Map<String, Object> map1 = new HashMap<>();
 
         map1.put("hui.pizda.dzhigurda", "value.#pizda");
         map1.put("hui#pizda#dzhigurda1", "value.#pizda1");
 
         final Map<String, Object> map2 = new HashMap<>();
         map2.put("level2.hui.pizda.dzhigurda", "level2.value.#pizda");
         map2.put("level2.hui#pizda#dzhigurda1", "level2.value.#pizda1");
 
         map1.put("level2.map#one", map2);
 
         final Map<String, Object> map3 = new HashMap<>();
         map3.put("level3.hui.pizda.dzhigurda", "level3.value.#pizda");
         map3.put("level3.hui#pizda#dzhigurda1", "level3.value.#pizda1");
         map3.put("TEST_METADATA_KEY1", "TEST_VALUE1");
         map1.put("level3.map#two", map3);
         map1.put("TEST_METADATA_KEY", "TEST_VALUE");
         return map1;
     }
 
     private static class Key {
         private final String val;
 
         @SuppressWarnings("unused") // for deserialization purpose
         Key() {
             this.val = null;
         }
 
         Key(final String val) {
             this.val = val;
         }
 
         @Override
         public boolean equals(final Object o) {
             if (this == o) {
                 return true;
             }
             if (o == null || getClass() != o.getClass()) {
                 return false;
             }
 
             final Key key = (Key) o;
             return !(val != null ? !val.equals(key.val) : key.val != null);
         }
 
         @Override
         public int hashCode() {
             return val != null ? val.hashCode() : 0;
         }
     }

    static class Bean {
        Bean bean;
        Bean() {
            bean = this;
        }
    }
 }
