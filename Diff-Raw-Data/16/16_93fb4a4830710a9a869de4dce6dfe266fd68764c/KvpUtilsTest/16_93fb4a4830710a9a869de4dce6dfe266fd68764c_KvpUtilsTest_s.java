 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows.kvp;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.geoserver.ows.util.KvpUtils;
 
 import junit.framework.TestCase;
 
 public class KvpUtilsTest extends TestCase {
     public void testEmptyString() {
         assertEquals(0, KvpUtils.readFlat("").size());
     }
     
     public void testTrailingEmtpyStrings() {
         assertEquals(Arrays.asList(new String[] {"x", "", "x", "", ""}), KvpUtils.readFlat("x,,x,,"));
     }
     
     public void testEmtpyNestedString() {
     	List result = KvpUtils.readNested("");
     	assertEquals(1, result.size());
     	assertEquals(0, ((List) result.get(0)).size());
     }
     
     public void testStarNestedString() {
     	List result = KvpUtils.readNested("*");
     	assertEquals(1, result.size());
     	assertEquals(0, ((List) result.get(0)).size());
     }
     
     public void testWellKnownTokenizers(){
         String[] expected;
         List actual;
         
         expected = new String[]{"1", "2", "3", ""};
         actual = KvpUtils.readFlat("1,2,3,", KvpUtils.INNER_DELIMETER);
         assertKvp(expected, actual);
 
         expected = new String[]{"abc", "def", ""};
         actual = KvpUtils.readFlat("(abc)(def)()", KvpUtils.OUTER_DELIMETER);
         assertKvp(expected, actual);
 
         expected = new String[]{"", "A=1", "B=2", ""};
         actual = KvpUtils.readFlat(";A=1;B=2;", KvpUtils.CQL_DELIMITER);
         assertKvp(expected, actual);
 
         expected = new String[]{"ab", "cd", "ef", ""};
         actual = KvpUtils.readFlat("ab&cd&ef&", KvpUtils.KEYWORD_DELIMITER);
         assertKvp(expected, actual);
 
         expected = new String[]{"A", "1 "};
         actual = KvpUtils.readFlat("A=1 ", KvpUtils.VALUE_DELIMITER);
         assertKvp(expected, actual);
     }
     
     public void testRadFlatUnkownDelimiter(){
         List actual;
         
         final String[] expected = new String[]{"1", "2", "3", ""};
         actual = KvpUtils.readFlat("1^2^3^", "\\^");
         assertKvp(expected, actual);
 
         actual = KvpUtils.readFlat("1-2-3-", "-");
         assertKvp(expected, actual);
     }
     
     private void assertKvp(String[] expected, List actual){
         List expectedList = Arrays.asList(expected);
         assertEquals(expectedList.size(), actual.size());
         assertEquals(expectedList, actual);
     }
     
 }
