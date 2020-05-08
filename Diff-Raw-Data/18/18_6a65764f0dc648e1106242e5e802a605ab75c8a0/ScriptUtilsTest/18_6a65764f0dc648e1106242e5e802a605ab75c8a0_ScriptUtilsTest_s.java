 /**
  *
  */
 package com.github.jtse.puzzle.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 
 /**
  * @author jtse
  */
 public class ScriptUtilsTest {
   @Test
   public void testRead() {
     List<ImmutableMap<String, String>> expected = ImmutableList.of(
         ImmutableMap.of("confKey1", "confVal1", "confKey2", "confVal2"),
        ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3"),
         ImmutableMap.of("key1", "val1b", "key2", "val2b", "key3", "val3b"),
         ImmutableMap.of("key1", "val1c", "key2", "val2c", "key3", "val3c"));
 
     String input = "confKey1=confVal1\n"
        + "key1=val1\n key2=val2\nkey3=val3\n"
         + "key1=val1b\n key2=val2b\nkey3=val3b\n"
         + "key1=val1c\n key2=val2c\nkey3=val3c\n"
         + "confKey2=confVal2\n";
 
     InputStream in = new ByteArrayInputStream(input.getBytes());
 
     List<Map<String, String>> actual = ScriptUtils.read(in,
         ImmutableSet.of("key1", "key2", "key3"));
 
     Assert.assertEquals(expected, actual);
   }
 
   @Test
   public void testParseColor() {
     float[] expected = { 1.0f, 0.5f, 0.9f, 1.0f };
     assertArrayEquals(expected, ScriptUtils.parseColor("1.0, 0.5 0.9"));
   }
 
   @Test
   public void testParseColorWith4Values() {
     float[] expected = { 1.0f, 0.5f, 0.9f, 0.5f };
     assertArrayEquals(expected, ScriptUtils.parseColor("1.0, 0.5 0.9 	0.5"));
   }
 
   private static final void assertArrayEquals(float[] expected, float[] actual) {
     Assert.assertEquals(expected.length, actual.length);
 
     for (int i = 0; i < expected.length; i++) {
       Assert.assertEquals(expected[i], actual[i], 0.000001);
     }
   }
 }
