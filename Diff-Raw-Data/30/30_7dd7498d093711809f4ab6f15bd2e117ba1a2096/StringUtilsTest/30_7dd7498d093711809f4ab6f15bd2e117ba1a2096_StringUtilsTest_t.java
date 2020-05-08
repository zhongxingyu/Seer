 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2005  Jose San Leandro Armendriz
                              chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabaas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendriz
  *
  * Description: Performs some unit tests on StringUtils class.
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *                  ("Name" means no concrete version has been checked out)
  *
  * $Id$
  *
  */
 package unittests.org.acmsl.commons.utils;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.utils.StringUtils;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 
 /*
  * Importing JUnit classes.
  */
 import junit.framework.TestCase;
 
 /**
  * Performs some unit tests on StringUtils class.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro Armendriz</a>
  * @version $Revision$
  * @testfamily JUnit
  * @testkind testcase
  * @testsetup Default TestCase
  * @testedclass org.acmsl.commons.utils.StringUtils
  * @see org.acmsl.commons.utils.StringUtils
  */
 public class StringUtilsTest
     extends     TestCase
     implements  org.acmsl.commons.patterns.Test
 {
     /**
      * Constructs a test case with the given name.
      * @param name the test case name.
      */
     public StringUtilsTest(final String name)
     {
         super(name);
     }
 
     /**
      * Tests the <code>StringUtils.replace(text, original, replacement)</code>
      * method.
      * @see StringUtils#replace(String,String,String)
      */
     public void testReplace()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         assertEquals(
             "com-foo-bar",
             t_StringUtils.replace("com.foo.bar", ".", "-"));
 
         assertEquals(
             "com.-oo.bar",
             t_StringUtils.replace("com.foo.bar", "f", "-"));
     }
 
     /**
      * Tests the <code>StringUtils.escapeRegexp(text)</code>
      * method.
      * @see StringUtils#escapeRegexp(String)
      */
     public void testEscapeRegexp()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         assertEquals(
             "c\\$om\\.\\(foo\\)\\*\\.bar\\^", // c\$om\.\(foo\)\*\.bar\^
         t_StringUtils.escapeRegexp("c$om.(foo)*.bar^"));
     }
 
     /**
      * Tests the StringUtils.packageToFilePath(packageName) method.
      * @see StringUtils#packageToFilePath(String)
      */
     public void testPackageToFilePath()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         assertEquals(
             t_StringUtils.packageToFilePath("com.foo.bar"),
             "com" + File.separator + "foo" + File.separator + "bar");
 
         assertEquals(
             t_StringUtils.packageToFilePath("com"),
             "com");
 
         assertEquals(
             t_StringUtils.packageToFilePath("com."),
             "com");
 
         assertEquals(
             t_StringUtils.packageToFilePath("com.."),
             "com");
 
         assertEquals(
             t_StringUtils.packageToFilePath("..com..."),
             "com");
 
         assertEquals(
             t_StringUtils.packageToFilePath("..com...foo..bar...."),
             "com" + File.separator + "foo" + File.separator + "bar");
     }
 
     /**
      * Tests the StringUtils.justify(text, margin) method.
      * @see StringUtils#justify(String,int)
      */
     public void testJustify()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strJustified =
             t_StringUtils.justify(
                 "To be justified just after the \"just\"",
                 "To be justified just ".length());
 
         assertNotNull(t_strJustified);
 
         assertEquals(
             "To be justified just\nafter the \"just\"",
             t_strJustified);
 
         t_strJustified =
             t_StringUtils.justify(
                 "To be justified just after the \"just\"",
                 "To be justified just".length());
 
         assertNotNull(t_strJustified);
 
         assertEquals(
             "To be justified just\nafter the \"just\"",
             t_strJustified);
 
         t_strJustified =
             t_StringUtils.justify(
                 "To be justified just  after the \"after\"",
                 "To be justified just  after".length());
 
         assertNotNull(t_strJustified);
 
         assertEquals(
             "To be justified just after\nthe \"after\"",
             t_strJustified);
     }
 
     /**
      * Tests the StringUtils.uncapitalize(text, separator) method.
      * @see StringUtils#uncapitalize(String,String)
      */
     public void testUncapitalize()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = "thisIsATest";
 
         String t_strUnCapitalized =
             t_StringUtils.unCapitalize(t_strInput, "-");
 
         assertEquals(
             "this-is-a-test", t_strUnCapitalized);
 
         t_strInput = "thisIsAnotherTest";
 
         t_strUnCapitalized =
             t_StringUtils.unCapitalize(t_strInput, "-");
 
         assertEquals(
             "this-is-another-test", t_strUnCapitalized);
 
         t_strInput = "this Is yetAnotherTest";
 
         t_strUnCapitalized =
             t_StringUtils.unCapitalize(t_strInput, "-");
 
         assertEquals(
             "this-is-yet-another-test", t_strUnCapitalized);
     }
 
     /**
      * Tests the StringUtils.uncapitalizeStart(text, separator) method.
      * @see StringUtils#uncapitalize(String,String)
      */
     public void testUncapitalizeStart()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = "ThisIsATest";
 
         String t_strUnCapitalized =
             t_StringUtils.unCapitalizeStart(t_strInput);
 
         assertEquals(
             "thisIsATest", t_strUnCapitalized);
 
         t_strInput = "ThisIsAnotherTest";
 
         t_strUnCapitalized =
             t_StringUtils.unCapitalizeStart(t_strInput);
 
         assertEquals(
             "thisIsAnotherTest", t_strUnCapitalized);
     }
 
     /**
      * Tests the StringUtils.applyToEachLine(text, format) method.
      * @see StringUtils#applyToEachLine(String,String)
      */
     public void testApplyToEachLine()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = " line 1   \n    and line 2";
 
         String t_strOutput =
             t_StringUtils.applyToEachLine(
                 t_strInput, "||{0}{1}//");
 
         assertEquals("||line 1//\n||   and line 2//\n", t_strOutput);
 
         t_strInput = "    line 1   \n .   and line 2";
 
         t_strOutput =
             t_StringUtils.applyToEachLine(
                 t_strInput, "||{0}{1}//");
 
         assertEquals("||line 1//\n||.   and line 2//\n", t_strOutput);
 
         t_strOutput =
             t_StringUtils.applyToEachLine( 
                   "      update customers\n" 
                 + "      set name = ?\n" 
                 + "      where customer_id in\n" 
                 + "      (\n" 
                 + "        select person_id\n" 
                 + "        from people\n" 
                 + "        where last_name like '?%'\n" 
                 + "      )\n", 
                 "{0}|{1}");
 
         assertEquals(
                   "|update customers\n" 
                 + "|set name = ?\n" 
                 + "|where customer_id in\n" 
                 + "|(\n" 
                 + "  |select person_id\n" 
                 + "  |from people\n" 
                 + "  |where last_name like '?%'\n" 
                 + "|)\n",
                 t_strOutput);
     }
 
     /**
      * Tests the StringUtils.retrieveMinimumIndentInAllLines(text) method.
      * @see StringUtils#retrieveMinimumIndentInAllLines(String)
      */
     public void testRetrieveMinimumIndentInAllLines()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = " line 1   \n    and line 2";
 
         int t_iOutput =
             t_StringUtils.retrieveMinimumIndentInAllLines(t_strInput);
 
         assertEquals(1, t_iOutput);
 
         t_strInput = "    line 1   \n .   and line 2";
 
         t_iOutput =
             t_StringUtils.retrieveMinimumIndentInAllLines(t_strInput);
 
         assertEquals(1, t_iOutput);
     }
 
     /**
      * Tests the StringUtils.removeFirstAndLastBlankLines(text) method.
      * @see StringUtils#removeFirstAndLastBlankLines(String)
      */
     public void testRemoveFirstAndLastBlankLines()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = " line 1   \n    and line 2";
 
         String t_strOutput =
             t_StringUtils.removeFirstAndLastBlankLines(t_strInput);
 
         assertEquals(t_strInput, t_strOutput);
 
         t_strInput = " \nline 1   \n    and line 2\n     ";
 
         t_strOutput =
             t_StringUtils.removeFirstAndLastBlankLines(t_strInput);
 
         assertEquals("line 1   \n    and line 2\n", t_strOutput);
 
         t_strInput =
             "insert into customers (customer_id, name) values (?, \"test\")";
 
         t_strOutput =
             t_StringUtils.removeFirstAndLastBlankLines(t_strInput);
 
         assertEquals(t_strInput, t_strOutput);
     }
 
     /**
      * Tests the <code>StringUtils.capitalize(String)</code> method.
      * @see org.acmsl.commons.utils.StringUtils#capitalize(String)
      */
     public void testCapitalize()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = "ab-cd_ef  gh+ijk;;_|l~~@m  2n=o4p";
 
         String t_strOutput = t_StringUtils.capitalize(t_strInput);
 
         assertEquals("AbCdEfGhIjkLM2nO4p", t_strOutput);
     }
 
     /**
      * Tests the <code>StringUtils.escape(String, char)</code> method.
      * @see org.acmsl.commons.utils.StringUtils#escape(String, char)
      */
     public void testEscape()
     {
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         assertNotNull(t_StringUtils);
 
         String t_strInput = "ab ()\" .cde  \" f g ^\\\"";
 
         String t_strOutput = t_StringUtils.escape(t_strInput, '\"');
 
        assertEquals(
            "Escape failed for " + t_strInput,
            "ab ()\\\" .cde  \\\" f g ^\\\\\"",
            t_strOutput);
     }
 }
