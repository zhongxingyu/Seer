 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.shared;
 
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.Pair;
 import com.flexive.shared.TimestampRecorder;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Unit tests for com.flexive.shared.FxSharedUtils.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @see com.flexive.shared.FxSharedUtils
  */
 @Test(groups = {"shared"})
 public class FxSharedUtilTest {
 
     /**
      * Unit test for com.flexive.shared.FxSharedUtils.formatResource.
      */
     @Test
     public void formatResource() {
         Assert.assertTrue("ABC".equals(FxFormatUtils.formatResource("{0}", -1, "ABC")), "Did not replace single argument correctly");
         Assert.assertTrue("Test:-ABC-".equals(FxFormatUtils.formatResource("Test:-{0}-", -1, "ABC")), "Did not replace single argument correctly");
         Assert.assertTrue("Test:-ABC-".equals(FxFormatUtils.formatResource("Test:-{0}{1}C{2}", -1, "A", "B", "-")), "Did not replace multiple arguments correctly");
         Map<Long, String> translations = new HashMap<Long, String>();
         translations.put(1L, "translation1");
         translations.put(2L, "translation2");
         FxString translatedString = new FxString(translations);
         Assert.assertTrue("Test: translation1".equals(FxFormatUtils.formatResource("Test: {0}", 1, translatedString)), "Did not replace FxString argument correctly");
         Assert.assertTrue("Test: translation2".equals(FxFormatUtils.formatResource("Test: {0}", 2, translatedString)), "Did not replace FxString argument correctly");
         Assert.assertEquals("Test: translation2==translation2", FxFormatUtils.formatResource("Test: {0}=={1}", 2,
                 translatedString, translatedString), "Did not replace multiple FxString arguments correctly");
         Assert.assertTrue("".equals(FxFormatUtils.formatResource("", -1)), "Did not treat empty string value correctly.");
         Assert.assertTrue("".equals(FxFormatUtils.formatResource(null, -1)), "Did not treat null string value correctly.");
         Assert.assertTrue("\t \t \n".equals(FxFormatUtils.formatResource("\t{0}\t \n", -1, " ")), "Did not preserve whitespace correctly.");
     }
 
     /**
      * Tests for the filter method
      */
     @Test
     public void filter() {
         String newline = "abc\ndef";
         String htmlFilter = "1 > 2 < 3";
         Assert.assertEquals(FxFormatUtils.escapeForJavaScript(newline, false), newline.replace('\n', ' '), "Did not preserve newlines correctly.");
         Assert.assertEquals(FxFormatUtils.escapeForJavaScript(newline, true), StringUtils.replace(newline, "\n", "<br/>"), "Did not replaces newlines correctly.");
         Assert.assertEquals(FxFormatUtils.escapeForJavaScript(htmlFilter, false, true), StringUtils.replace(StringUtils.replace(htmlFilter, "<", "&lt;"), ">", "&gt;"), "Did not escape HTML code correctly.");
         Assert.assertEquals(FxFormatUtils.escapeForJavaScript(null), "", "Did not treat null value correctly.");
         Assert.assertEquals(FxFormatUtils.escapeForJavaScript(""), "", "Did not treat empty string correctly.");
     }
 
     @Test
     public void splitLiterals() {
         Assert.assertTrue(FxSharedUtils.splitLiterals(null).length == 0, "Null parameters should return an empty array.");
         assertSplitResult(FxSharedUtils.splitLiterals("literal value"), new String[]{"literal value"});
         assertSplitResult(FxSharedUtils.splitLiterals("value 1, value 2"), new String[]{"value 1", "value 2"});
         assertSplitResult(FxSharedUtils.splitLiterals(","), new String[]{"", ""});
         assertSplitResult(FxSharedUtils.splitLiterals(",,"), new String[]{"", "", ""});
         assertSplitResult(FxSharedUtils.splitLiterals("some,value,list"), new String[]{"some", "value", "list"});
         assertSplitResult(FxSharedUtils.splitLiterals("'some,value,list'"), new String[]{"some,value,list"});
         assertSplitResult(FxSharedUtils.splitLiterals("'something','has to','change'"), new String[]{"something", "has to", "change"});
         assertSplitResult(FxSharedUtils.splitLiterals("'',''"), new String[]{"", ""});
         assertSplitResult(FxSharedUtils.splitLiterals("'\"quoted,string\"',\"'another quoted,string'\""),
                 new String[]{"\"quoted,string\"", "'another quoted,string'"});
     }
 
     @Test
     public void getColumnIndex() {
         final String[] columns = {"co.@pk", "co.name", "co.list.@value"};
         Assert.assertEquals(FxSharedUtils.getColumnIndex(columns, "@pk"), 1);
         Assert.assertEquals(FxSharedUtils.getColumnIndex(columns, "co.@pk"), 1);
         Assert.assertEquals(FxSharedUtils.getColumnIndex(columns, "pk"), -1);
         Assert.assertEquals(FxSharedUtils.getColumnIndex(columns, "@value"), 3);
         Assert.assertEquals(FxSharedUtils.getColumnIndex(columns, "co.list.@value"), 3);
     }
 
     @Test
     public void timestampRecorder() throws InterruptedException {
         final TimestampRecorder tsr = new TimestampRecorder();
         tsr.begin();
         Thread.sleep(50);
         tsr.timestamp("test");
         final Pair<String, Long> timestamp = tsr.getTimestamps().get(0);
         Assert.assertEquals("test", timestamp.getFirst());
         final double millis = timestamp.getSecond() / 1000000.0;
        Assert.assertTrue(Math.abs(50.0 - millis) < 5.0, "Timestamp does not match expected 50ms: " + millis);
     }
 
     private void assertSplitResult(String[] result, String[] expected) {
         Assert.assertEquals(Arrays.asList(result), Arrays.asList(expected),
                 "Expected split result: " + Arrays.asList(expected) + ", got: " + Arrays.asList(result));
     }
 }
