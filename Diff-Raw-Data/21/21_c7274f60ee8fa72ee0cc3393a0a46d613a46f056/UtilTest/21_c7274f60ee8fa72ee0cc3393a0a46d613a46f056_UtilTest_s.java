 package com.github.wolfie.bob;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class UtilTest {
   
   @Test(expected = NullPointerException.class)
   public void testImplodeStringObjectArrayNullArgs1() {
     Util.implode(null, (Object) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testImplodeStringObjectArrayNullArgs2() {
     Util.implode(null, (Object[]) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testImplodeStringObjectArrayNullObjectBits() {
     Util.implode(",", (Object) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testImplodeStringObjectArrayNullArrayBits() {
     Util.implode(",", (Object[]) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testImplodeStringObjectArrayNullGlue() {
     Util.implode(null, new Object[] { new Object() });
   }
   
   @Test
   public void testImplodeStringObjectArray() {
     final String actual = Util.implode(",",
         new Object[] { "foo", "bar", "baz" });
     assertEquals("foo,bar,baz", actual);
   }
   
   @Test
   public void testImplodeStringCollectionOfQ() {
     final String actual = Util.implode(",", Arrays.asList("foo", "bar", "baz"));
     assertEquals("foo,bar,baz", actual);
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithNull() {
     Util.checkNulls((Object[]) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithOneNull() {
     Util.checkNulls((Object) null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithTwoNulls() {
     Util.checkNulls(null, null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithThreeNulls() {
     Util.checkNulls(null, null, null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithNullAndObject() {
     Util.checkNulls(null, new Object());
   }
   
   @Test(expected = NullPointerException.class)
   public void testCheckNullsWithObjectAndNull() {
     Util.checkNulls(new Object(), null);
   }
   
   @Test
   public void testCheckNullsWithObject() {
     Util.checkNulls(new Object());
   }
   
   @Test
   public void testCheckNullsWithTwoObjects() {
     Util.checkNulls(new Object(), new Object());
   }
   
   @Test
   public void testCheckNullsWithThreeObjects() {
     Util.checkNulls(new Object(), new Object(), new Object());
   }
   
   @Test(expected = NullPointerException.class)
   public void testPutIfNotExistsNulls() {
     Util.putIfNotExists(null, null, null);
   }
   
   @Test(expected = NullPointerException.class)
   public void testPutIfNotExistsNullMap() {
     Util.putIfNotExists(1, "foo", null);
   }
   
   @Test
   public void testPutIfNotExistsWithEmptyMap() {
     final Map<Integer, String> hashMap = new HashMap<Integer, String>();
     final Integer key = 1;
     final String value = "foo";
     
     Util.putIfNotExists(key, value, hashMap);
     
     assertSame(1, hashMap.size());
     assertEquals(value, hashMap.get(key));
     assertTrue(hashMap.containsKey(key));
     assertTrue(hashMap.containsValue(value));
   }
   
   @Test
   public void testPutIfNotExistsWithFilledMapUnderwrite() {
     final Map<Integer, String> hashMap = new HashMap<Integer, String>();
     final Integer key = 1;
     final String value1 = "foo";
     final String value2 = "bar";
     hashMap.put(key, value1);
     
     Util.putIfNotExists(key, value2, hashMap);
     
     assertSame(1, hashMap.size());
     assertEquals(value1, hashMap.get(key));
     assertTrue(hashMap.containsKey(key));
     assertTrue(hashMap.containsValue(value1));
   }
   
   @Test
   public void testWordWrapStringIntWithoutIndenting() {
     final String actual = Util.wordWrap("foo bar baz", 3);
     assertEquals("foo\nbar\nbaz", actual);
   }
   
   @Test
   public void testWordWrapStringIntWithIndenting() {
     final String actual = Util.wordWrap(" foo bar baz", 4);
     assertEquals(" foo\n bar\n baz", actual);
   }
   
   @Test
   public void testWordWrapStringIntWithShorterLineThanWords() {
     final String actual = Util.wordWrap("foo bar baz", 2);
     assertEquals("foo\nbar\nbaz", actual);
   }
 }
