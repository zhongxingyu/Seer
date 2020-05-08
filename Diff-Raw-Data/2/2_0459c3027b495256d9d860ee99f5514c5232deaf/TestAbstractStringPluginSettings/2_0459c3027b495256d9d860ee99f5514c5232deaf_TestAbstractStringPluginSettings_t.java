 package com.atlassian.sal.core.pluginsettings;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 public class TestAbstractStringPluginSettings
 {
     private PluginSettingsAcceptor acceptor;
     private static final String KEY = "some key";
 
     @Before
     public void setUp()
     {
         acceptor = new PluginSettingsAcceptor();
     }
 
     @Test
     public void testString()
     {
         String value = "this is the value";
         
         acceptor.put(KEY, value);
         assertEquals("Values should be equal.", value, acceptor.get(KEY));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetWithNullKey()
     {
         acceptor.get(null);
     }
 
     @Test
     public void getWithLongKeyDoesNotCauseError()
     {
         assertNull(acceptor.get(StringUtils.repeat("a", 101)));
     }
 
     @Test
     public void getWithVeryLongKeyDoesNotCauseError()
     {
         assertNull(acceptor.get(StringUtils.repeat("a", 256)));
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testPutWithNullKey()
     {
         acceptor.put(null, "foo");
     }
 
     @Test
     public void putWith100CharacterKeyIsAlwaysAccepted()
     {
         acceptor.put(StringUtils.repeat("a", 100), "foo");
     }
     
     /**
      * The SAL layer accepts this key. The underlying storage layer
      * may fail in a real application.
      */
     @Test
     public void putWithVeryLongKeyIsAcceptedBySalLayer()
     {
         String previousValue = System.setProperty("atlassian.dev.mode", "false");
         try
         {
             // A new instance to pick up the system property
             acceptor = new PluginSettingsAcceptor();
             acceptor.put(StringUtils.repeat("a", 255), "foo");
         }
         finally
         {
             System.setProperty("atlassian.dev.mode", StringUtils.defaultString(previousValue));
         }
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void putWithOverlyLongKeyFails()
     {
         acceptor.put(StringUtils.repeat("a", 256), "foo");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void putWithLongKeyFailsInDevMode()
     {
         String previousValue = System.setProperty("atlassian.dev.mode", "true");
         try
         {
             // A new instance to pick up the system property
             acceptor = new PluginSettingsAcceptor();
             acceptor.put(StringUtils.repeat("a", 101), "foo");
         }
         finally
         {
             System.setProperty("atlassian.dev.mode", StringUtils.defaultString(previousValue));
         }
     }
     
     @Test
     public void testSpecialCharsString()
     {
         String value = "this\tis\bthe\nvalue\rand\ffun";
 
         acceptor.put(KEY, value);
         assertEquals("Values should be equal.", value, acceptor.get(KEY));
     }
 
     @Test
     public void testList()
     {
         final List<String> value = new ArrayList<String>();
         final String first = "Beanz Meanz Heinz";
         final String second = "Sic 'em Rex!";
         
         value.add(first);
         value.add(second);
         
         acceptor.put(KEY, value);
         
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof List);
         
         final List<?> real = (List<?>) actual;
         
         assertEquals("List size should be the same", 2, real.size());
         assertEquals("List should still be in order", first, real.get(0));
         assertEquals("List should still be in order", second, real.get(1));
     }
 
     @Test
     public void testSpecialCharsInList()
     {
         final List<String> value = new ArrayList<String>();
 
         final String first = "At\tfirst\bwhen\nI\rsee\fyou cry";
         final String second = "\tit\bmakes\nme\rsmile\f";
 
         value.add(first);
         value.add(second);
         acceptor.put(KEY, value);
 
         final Object actual = acceptor.get(KEY);
 
         assertTrue(actual instanceof List);
 
         final List<?> real = (List<?>) actual;
 
         assertEquals("List size should be the same", 2, real.size());
         assertEquals("The content should match the original data", first, real.get(0));
         assertEquals("The content should match the original data", second, real.get(1));
     }
 
     @Test
     public void testMap()
     {
         final Map<String, String> value = new HashMap<String, String>();
         final String[] first = {"antzpantz", "Sic 'em Rex!"}; 
         final String[] second = {"homestar", "Lookin' at a thing in a bag"}; 
         final String[] third = {"he-man", "By the power of Greyskull!!!"};
         
         mapPut(value, first);
         mapPut(value, second);
         mapPut(value, third);
         
         acceptor.put(KEY, value);
 
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof Map);
         
         final Map<?, ?> real = (Map<?, ?>) actual;
         
         assertEquals("The size should be the same as what was entered.", 3, real.entrySet().size());
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, first);
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, second);
         assertMapEntryEquals("Map should retrieve the correct value for each key", real, third);
     }
 
     @Test
     public void testSpecialCharsInMap()
     {
         final Map<String, String> value = new HashMap<String, String>();
         final String[] first = {"one", "one\tthing\bI\ndon't\rknow\fwhy"};
         final String[] second = {"two", "I\tdoesn't\beven\nmatter\rhow\fhard you try"};
 
         mapPut(value, first);
         mapPut(value, second);
 
         acceptor.put(KEY, value);
 
         final Object actual = acceptor.get(KEY);
 
         assertTrue(actual instanceof Map);
 
         final Map<?, ?> real = (Map<?, ?>) actual;
 
         assertEquals("The size should be the same as what was entered.", 2, real.entrySet().size());
         assertMapEntryEquals("The content should match the original data", real, first);
         assertMapEntryEquals("The content should match the original data", real, second);
     }
 
     @Test
     public void testEmptyMap()
     {
         final Map<String, String> value = new HashMap<String, String>();
         acceptor.put(KEY, value);
         final Object actual = acceptor.get(KEY);
         assertTrue(actual instanceof Map);
         assertTrue(((Map<?, ?>) actual).isEmpty());
     }
 
     @Test
     public void testProperties()
     {
         final Properties value = new Properties();
         final String[] first = {"antzpantz", "Sic 'em Rex!"}; 
         final String[] second = {"homestar", "Lookin' at a thing in a bag"}; 
         final String[] third = {"he-man", "By the power of Greyskull!!!"};
      
         propertiesPut(value, first);        
         propertiesPut(value, second);        
         propertiesPut(value, third);
         
         acceptor.put(KEY, value);
         
         final Object actual = acceptor.get(KEY);
         
         assertTrue(actual instanceof Properties);
         
         final Properties real = (Properties) actual;
         
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, first);
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, second);
         assertPropertiesEntryEquals("Propertis should contain the same value for each key", real, third);
     }
 
     @Test
     public void testSpecialCharsInKeys()
     {
         String key1 = "this\tis\bthe\nkey\rsample\fhohoho1";
         String value1 = "value1";
 
         String key2 = "this\tis\bthe\nkey\rsample\fhohoho2";
         List<String> value2 = Arrays.asList("value2");
 
         String key3 = "this\tis\bthe\nkey\rsample\fhohoho3";
         Map<String, String> value3 = new HashMap<String, String>();
         value3.put(key3 + "inner1", "value3inner1");
         value3.put(key3 + "inner2", "value3inner2");
 
         String key4 = "this\tis\bthe\nkey\rsample\fhohoho4";
         Properties value4 = new Properties();
         value4.setProperty(key4 + "inner1", "value4inner1");
         value4.setProperty(key4 + "inner2", "value4inner2");
 
         acceptor.put(key1, value1);
         acceptor.put(key2, value2);
         acceptor.put(key3, value3);
         acceptor.put(key4, value4);
 
         assertEquals("Values should be equal.", value1, acceptor.get(key1));
         assertEquals("Values should be equal.", value2, acceptor.get(key2));
         assertEquals("Values should be equal.", value3, acceptor.get(key3));
         assertEquals("Values should be equal.", value4, acceptor.get(key4));
     }
 
     @Test
     public void testPutReturnValueNull()
     {
         List<String> value = Arrays.asList("one", "two", "three");
         assertNull(acceptor.put(KEY, value));
     }
 
     @Test
     public void testPutReturnValueOldValue()
     {
         List<String> oldValue = Arrays.asList("two", "three", "four");
         acceptor.put(KEY, oldValue);
         List<String> value = Arrays.asList("one", "two", "three");
         assertEquals(oldValue, acceptor.put(KEY, value));
     }
 
     @Test
     public void testRemoveReturnValueNull()
     {
         assertNull(acceptor.remove(KEY));
     }
 
     @Test
     public void testRemoveReturnValueOldValue()
     {
         List<String> oldValue = Arrays.asList("two", "three", "four");
         acceptor.put(KEY, oldValue);
         assertEquals(oldValue, acceptor.remove(KEY));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testRemoveWithNullKey()
     {
         acceptor.remove(null);
     }
 
     @Test
     public void removeWithLongKeyDoesNotFail()
     {
         assertNull(acceptor.remove(StringUtils.repeat("a", 101)));
     }
 
     @Test
    public void removeWithVeryLongKeyDoesNotFail()
     {
         assertNull(acceptor.remove(StringUtils.repeat("a", 256)));
     }
     
     private void assertPropertiesEntryEquals(String errMsg, Properties real, String[] kvPair)
     {
         assertEquals(errMsg, kvPair[1], real.getProperty(kvPair[0]));
     }
 
     private static void propertiesPut(Properties properties, String[] values)
     {
         properties.setProperty(values[0], values[1]);
     }
 
     private static void assertMapEntryEquals(String errMsg, Map<?, ?> real, String[] kvPair)
     {
         assertEquals(errMsg, kvPair[1], real.get(kvPair[0]));
     }
 
     private static void mapPut(Map<String, String> map, String[] values)
     {
         map.put(values[0], values[1]);
     }
 
 
     private static final class PluginSettingsAcceptor extends AbstractStringPluginSettings
     {
         private final Map<String,String> backingStore = new HashMap<String, String>();
         
         protected void putActual(String key, String val)
         {
             backingStore.put(key, val);
         }
 
         protected String getActual(String key)
         {
             return backingStore.get(key);                        
         }
 
         protected void removeActual(String key)
         {
             backingStore.remove(key);
         }
     }
 }
