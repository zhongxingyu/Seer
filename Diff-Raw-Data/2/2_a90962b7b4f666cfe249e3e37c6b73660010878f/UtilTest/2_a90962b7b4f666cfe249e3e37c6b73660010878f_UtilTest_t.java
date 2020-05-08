 /*
  * Copyright 2002-2005 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */	
 package test.net.sf.sojo.util;
 
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import junit.framework.TestCase;
 import net.sf.sojo.util.Util;
 
 public class UtilTest extends TestCase {
 
 	public void testString2Date() throws Exception {
 		Date lvDate = Util.string2Date("1234567");
 		assertEquals(new Date(1234567), lvDate);
 		
 		Date lvDate2 = new Date();
 		lvDate = Util.string2Date(lvDate2.toString());
 		assertEquals(lvDate2.toString(), lvDate.toString());
 		
 		try {
 			lvDate2 = Util.string2Date(null);
 			fail("Date euqals null is illegal argument");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 
 		try {
 			lvDate2 = Util.string2Date("False Date");
 			fail("Date euqals String: 'False Date' is illegal argument");
 		} catch (Exception e) {
 			assertTrue(true); 
 		}	
 	}
 	
 	public void testString2Date2() throws Exception {
 		Date lvDate = new Date();
 		Date lvDate2 = Util.string2Date(Long.toString(lvDate.getTime()));
 		assertEquals(lvDate2, lvDate);
 	}
 
 	public void testString2SqlDate() throws Exception {
 		String formatString = "yyyy-mm-dd";
 		Util.registerDateFormat(formatString);
 		try {
 			long milliseconds = 980809200000l;
 			
 			java.sql.Date lvDate = new java.sql.Date(milliseconds);
 			Date lvDate2 = Util.string2Date(lvDate.toString());
 			
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(milliseconds);
 	
 			Calendar cal2 = Calendar.getInstance();
 			cal2.setTime(lvDate2);
 			
 			assertEquals(cal.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
 			assertEquals(cal.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
 			assertEquals(cal.get(Calendar.DAY_OF_MONTH), cal2.get(Calendar.DAY_OF_MONTH));
 		} finally {
 			Util.unregisterDateFormat(formatString);
 		}
 		
 	}
 
 	public void testString2Timpstamp() throws Exception {
 	  
 	  Map<String, DateFormat> oldFormats = new HashMap<String, DateFormat>();
 	  Util.clearDateFormats(oldFormats);
 	  
 	  String formatString = "yyyy-MM-dd' 'HH:mm:ss.SSS";
     Util.registerDateFormat(formatString);
 		try {
 			long lvTime = new Date().getTime();
 			Timestamp lvTimestamp = new Timestamp(lvTime);
 			Date lvDate2 = Util.string2Date(lvTimestamp.toString());
 			
 			assertEquals(lvTimestamp.getTime(), lvDate2.getTime());
 		} finally {
 		  Util.setDateFormats(oldFormats);
 		}
 	}
 
 	public void testString2Timpstamp2() throws Exception {
 	  
 		long lvTime = 1175188856390L;
 			Timestamp lvTimestamp = new Timestamp(lvTime);
 			Date lvDate2 = Util.string2Date(lvTimestamp.toString());
 			assertEquals(lvTime, lvDate2.getTime());
 	}
 	
 	/** 
 	 * This test is just to demonstrate that timestamp formatted strings 
 	 * with 2 digits in the fractional seconds are not parsed correctly.
 	 * If this test fails, it means the bug was fixed and we can remove the
 	 * special timestamp pass (2nd pass in {@link Util#string2Date(String)}.
 	 * 
 	 * @throws Exception
 	 */
 	public void testTimestamp3() throws Exception {
 	  // none of these formats work
 	  List<SimpleDateFormat> formatList = Arrays.asList(
 	    new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.S")
       , new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SS")
       , new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS")
       , new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSS")
       , new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSS")
 	  );
 	  
	  long offset = TimeZone.getTimeZone("CET").getOffset(1175188856390L) - TimeZone.getDefault().getOffset(1175188856390L);
 	  for (SimpleDateFormat df : formatList) {
 		  // 3 digits work fine
 		  Date date = df.parse("2007-03-29 19:20:56.390");
 		  assertEquals(1175188856390L+offset, date.getTime());
 
 		  // 2 digits fail, notice the last 3 digits 039 vs. 390
 		  date = df.parse("2007-03-29 19:20:56.39");
 		  assertEquals(1175188856039L+offset, date.getTime());
 		}
 	}
 	
 	/** Tue Mar 01 20:23:55 CET 2005 - 1109705105302 */
 	public void testString2DateCET() throws Exception {
 		Date d = new Date(1109705105302l);
 		Date d2 = Util.string2Date(d.toString());
 		assertEquals(d2.toString(), d.toString());
 	}
 
 	/** Sat Oct 01 20:25:34 CEST 2005 -- 1128191134214 */
 	public void testString2DateCEST() throws Exception {
 		Date d = new Date(1128191134214l);
 		Date d2 = Util.string2Date(d.toString());
 		assertEquals(d2.toString(), d.toString());
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void testArrayType_Array() throws Exception {
 		Class lvType = Util.getArrayType(null);
 		assertEquals(lvType, Object.class);
 		lvType = Util.getArrayType(new String[0]);
 		assertEquals(lvType, String.class);
 		lvType = Util.getArrayType(new String[] {"a", "b"});
 		assertEquals(lvType, String.class);
 		lvType = Util.getArrayType(new Long[] {new Long(1), new Long(3)});
 		assertEquals(lvType, Long.class);
 		lvType = Util.getArrayType(new Object[] {new Long(1), new Long(3)});
 		assertEquals(lvType, Long.class);
 		lvType = Util.getArrayType(new Object[] {new Long(1), "c"});
 		assertEquals(lvType, Object.class);		
 		
 		List<Object> v = new ArrayList<Object>();
 		lvType = Util.getArrayType(v);
 		assertEquals(lvType, Object.class);
 		
 		v.add(new String());
 		lvType = Util.getArrayType(v);
 		assertEquals(lvType, String.class);
 		
 		Map<?,?> lvMap = new HashMap<Object, Object>();
 		lvType = Util.getArrayType(lvMap);
 		assertEquals(lvType, Object.class);
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testArrayType_Collection() throws Exception {
 		List v = new ArrayList<Object>();
 		v.add("a");
 		v.add("b");
 		Class<?> lvType = Util.getArrayType(v);
 		assertEquals(lvType, String.class);			
 		v = new ArrayList<Long>();
 		v.add(new Long (1));
 		v.add(new Long (2));
 		lvType = Util.getArrayType(v);
 		assertEquals(lvType, Long.class);			
 		v = new ArrayList();
 		v.add(new Long (1));
 		v.add("b");
 		lvType = Util.getArrayType(v);
 		assertEquals(lvType, Object.class);
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testArrayType_Map() throws Exception {
 		Map lvMap = new HashMap();
 		lvMap.put("a", "a");
 		lvMap.put("b", "b");
 		Class lvType = Util.getArrayType(new Object[] { lvMap } );
 		assertEquals(lvType, Object.class);			
 	}
 
 	
 	public void testDelLastComma() throws Exception {
 		StringBuffer sb = new StringBuffer("123,");
 		Util.delLastComma(sb);
 		assertEquals("123", sb.toString());
 		
 		sb = new StringBuffer("123");
 		Util.delLastComma(sb);
 		assertEquals("123", sb.toString());
 
 		sb = new StringBuffer("12,3");
 		Util.delLastComma(sb);
 		assertEquals("12,3", sb.toString());
 	}
 	
 	public void testIsStringInArray() throws Exception {
 		String lvArray[] = new String[] { "aa", "bb", "cc" };
 		boolean lvResult = Util.isStringInArray(lvArray, "aa");
 		assertTrue(lvResult);
 		
 		lvResult = Util.isStringInArray(lvArray, "bb");
 		assertTrue(lvResult);
 		
 		lvResult = Util.isStringInArray(lvArray, "cc");
 		assertTrue(lvResult);
 		
 		lvResult = Util.isStringInArray(lvArray, "");
 		assertFalse(lvResult);
 
 		lvResult = Util.isStringInArray(lvArray, "zz");
 		assertFalse(lvResult);
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testname() throws Exception {
 		Map lvMap = new HashMap();
 		lvMap.put("k1", "v1");
 		lvMap.put("k2", "v2");
 		lvMap.put("k3", "v3");
 		Map lvResultMap = Util.filterMapByKeys(lvMap, null);
 		assertEquals(lvMap, lvResultMap);
 		
 		lvResultMap = Util.filterMapByKeys(lvMap, new String[0]);
 		assertEquals(lvMap, lvResultMap);
 		
 		String lvArray[] = new String[] { "k1" , "k3", "k5"};
 		lvResultMap = Util.filterMapByKeys(lvMap, lvArray);
 		assertEquals(2, lvResultMap.size());
 
 		lvArray = new String[] { "k5"};
 		lvResultMap = Util.filterMapByKeys(lvMap, lvArray);
 		assertEquals(0, lvResultMap.size());
 
 		lvArray = new String[] { "k2", "k3", "k1"};
 		lvResultMap = Util.filterMapByKeys(lvMap, lvArray);
 		assertEquals(lvMap, lvResultMap);
 	}
 	
 	public void testUSformatFail() throws Exception {
 		try {
 			Date dt = Util.string2Date("Sat Oct 01 20:25:34 ABC 2005");
 			assertNotNull(dt);
 			fail("Must thrown exception by bad date format!");
 		} catch (Exception e) {
 			assertNotNull(e);
 		}
 	}
 
 	/**
 	 * For this to work properly, we have to choose a date without DST. Because it is not given that the JVM implementation supports historical DST data. 
 	 */
 	public void testUSformatOk() throws Exception {
 
 		Date lvDate = Util.string2Date("Sat Dec 01 20:25:34 PST 2005");
 		assertNotNull(lvDate);
 		
 		TimeZone tz = TimeZone.getTimeZone("PST");
     Calendar cal = Calendar.getInstance(tz);
 		
 		cal.set(2005, Calendar.DECEMBER, 1, 20, 25, 34);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		assertEquals(cal.getTime().getTime(), lvDate.getTime());
 	}
 
 	public void testKeyWordClass() throws Exception {
 		Util.setKeyWordClass(null);
 		assertEquals(Util.getKeyWordClass(), Util.DEFAULT_KEY_WORD_CLASS);
 
 		Util.setKeyWordClass("");
 		assertEquals(Util.getKeyWordClass(), Util.DEFAULT_KEY_WORD_CLASS);
 
 		Util.setKeyWordClass("yyyy");
 		assertEquals(Util.getKeyWordClass(), "yyyy");
 
 		String lvKeyWordClassNew = "myClassKeyWord"; 
 		Util.setKeyWordClass(lvKeyWordClassNew);
 		assertEquals(lvKeyWordClassNew, Util.getKeyWordClass());
 		
 		// !!! reset key word to class !!!
 		Util.resetKeyWordClass();
 	}
 }
