 package org.liveSense.server.i18n.constants;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.liveSense.server.i18n.I18N;
 
 import junit.framework.TestCase;
 
 
 public class TestConstants extends TestCase {
 
 
 	@Test
     public void testConstants() throws Exception {
         FooConstants msg = I18N.create(FooConstants.class);
         assertEquals("1", msg.test_String());
         assertEquals(1, msg.test_int());
         assertEquals(new Integer(1), msg.test_Integer());
        assertEquals(1.0f, msg.test_float(), 0.01f);
         assertEquals(new Float(1), msg.test_Float());
        assertEquals(1.0d, msg.test_double(), 0.01f);
         assertEquals(new Double(1.0), msg.test_Double());
         assertEquals(true, msg.test_boolean());
         assertEquals(Boolean.TRUE, msg.test_Boolean());
         assertEquals(new String[]{"1","2","3"}, msg.test_StringArray());
         {
             Map<String,String> expected = new HashMap<String,String>();
             expected.put("a", "1");
             expected.put("b", "2");
             expected.put("c", "3");
             assertEquals(expected, msg.test_MapStringString());
         }
     }
     
 	@Test
     public void testConstantsWithKeyAnnotations() throws Exception {
         FooConstantsWithKeyAnnotations msg = I18N.create(FooConstantsWithKeyAnnotations.class);
         assertEquals("1", msg.method_test_String());
         assertEquals(1, msg.method_test_int());
         assertEquals(new Integer(1), msg.method_test_Integer());
        assertEquals(1.0f, msg.method_test_float(), 0.01f);
         assertEquals(new Float(1), msg.method_test_Float());
        assertEquals(1.0d, msg.method_test_double(), 0.01f);
         assertEquals(new Double(1.0), msg.method_test_Double());
         assertEquals(true, msg.method_test_boolean());
         assertEquals(Boolean.TRUE, msg.method_test_Boolean());
         assertEquals(new String[]{"1","2","3"}, msg.method_test_StringArray());
         {
             Map<String,String> expected = new HashMap<String,String>();
             expected.put("a", "1");
             expected.put("b", "2");
             expected.put("c", "3");
             assertEquals(expected, msg.method_test_MapStringString());
         }
     }
 
 	@Test
     public void testFooConstantsWithKeyAndDefaultAnnotations() throws Exception {
         FooConstantsWithKeyAndDefaultAnnotations msg = I18N.create(FooConstantsWithKeyAndDefaultAnnotations.class);
         assertEquals("1", msg.method_test_String());
         assertEquals(1, msg.method_test_int());
         assertEquals(new Integer(1), msg.method_test_Integer());
        assertEquals(1.0f, msg.method_test_float(), 0.01f);
         assertEquals(new Float(1), msg.method_test_Float());
        assertEquals(1.0d, msg.method_test_double(), 0.01f);
         assertEquals(new Double(1.0), msg.method_test_Double());
         assertEquals(true, msg.method_test_boolean());
         assertEquals(Boolean.TRUE, msg.method_test_Boolean());
         assertEquals(new String[]{"1","2","3"}, msg.method_test_StringArray());
         {
             Map<String,String> expected = new HashMap<String,String>();
             expected.put("a", "1");
             expected.put("b", "2");
             expected.put("c", "3");
             assertEquals(expected, msg.method_test_MapStringString());
         }
     }
 	
 	@Test
     public void testConstantsWithLookup() throws Exception {
         FooConstantsWithLookup msg = I18N.create(FooConstantsWithLookup.class);
         assertEquals("1", msg.getString("test_String"));
         assertEquals(1, msg.getInt("test_int"));
         assertEquals(1, msg.getInt("test_Integer"));
        assertEquals(1.0f, msg.getFloat("test_float"), 0.01f);
        assertEquals(1.0f, msg.getFloat("test_Float"), 0.01f);
        assertEquals(1.0d, msg.getDouble("test_double"), 0.01f);
        assertEquals(1.0d, msg.getDouble("test_Double"), 0.01f);
         assertEquals(true, msg.getBoolean("test_boolean"));
         assertEquals(true, msg.getBoolean("test_Boolean"));
         assertEquals(new String[]{"1","2","3"}, msg.getStringArray("test_StringArray"));
         {
             Map<String,String> expected = new HashMap<String,String>();
             expected.put("a", "1");
             expected.put("b", "2");
             expected.put("c", "3");
             assertEquals(expected, msg.getMap("test_MapStringString"));
         }
     }
     
 	@Test
     private void assertEquals(String[] expected, String[] actual) {
         assertEquals(expected.length, actual.length);
         for (int i = 0; i < actual.length; i++) {
             assertEquals(expected[i], actual[i]);
         }
     }
 	
 	@Test
     private void assertEquals(Map<String,String> expected, Map<String,String> actual) {
         assertNotNull(actual);
         assertEquals(expected.size(), actual.size());
         for (Entry<String,String> e : expected.entrySet()) {
             assertEquals(e.getValue(), actual.get(e.getKey()));
         }
         for (Entry<String,String> e : actual.entrySet()) {
             assertEquals(e.getValue(), expected.get(e.getKey()));
         }
     }
 }
