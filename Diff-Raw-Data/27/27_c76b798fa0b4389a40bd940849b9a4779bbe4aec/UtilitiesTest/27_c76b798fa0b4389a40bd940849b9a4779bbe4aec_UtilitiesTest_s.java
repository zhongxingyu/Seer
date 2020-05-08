 package ro.finsiel.eunis.search;
 
 import static junit.framework.Assert.assertEquals;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import eionet.eunis.test.DbHelper;
 import ro.finsiel.eunis.utilities.SQLUtilities;
 
 public class UtilitiesTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         DbHelper.handleSetUpOperation("seed-four-species.xml");
     }
 
     /**
      * To <em>not</em> encode the ampersand in the value is probably not a good idea.
      * "&amp;category=Mosses %26 Liverworts" or "&amp;category=Mosses%20%26%20Liverworts"
      * would be more correct.
      */
     @Test
     public void stringParameter() {
         StringBuffer urlString = Utilities.writeURLParameter("category", "Mosses & Liverworts");
         assertEquals("&amp;category=Mosses & Liverworts", urlString.toString());
     }
 
     /**
      * Check how '+' is handled. This is an old encoding for space ' ', so '+'
      * should be encoded to '%2B'.
      */
     @Test
     public void plusParameter() {
         StringBuffer urlString = Utilities.writeURLParameter("operator", "+");
         assertEquals("&amp;operator=+", urlString.toString());
     }
 
     @Test
     public void integerURLParameter() {
         StringBuffer urlString = Utilities.writeURLParameter("code", 300);
         assertEquals("&amp;code=300", urlString.toString());
     }
 
     @Test
     public void multipleStringValues() {
         String[] countries = {"AT", "BE", "BG" };
 
         StringBuffer urlString = Utilities.writeURLParameter("country", countries);
         assertEquals("&amp;country=AT&amp;country=BE&amp;country=BG", urlString.toString());
     }
 
     @Test
     public void formatSimpleDecimal() {
         String testString = "-123.3435242";
         String expected = "<span style=\"font-family: 'Courier New', courier; font-size: 95%\">XXXXX-123.34352</span>";
 
         String result = Utilities.formatArea(testString, 9, 5, "X");
         assertEquals(expected, result);
     }
 
     @Test
     public void testTrimArray() {
         String[] testArray = {" 1 ", "2 ", null, "4" };
         String[] resultArray;
         String[] expectedArray = {"1", "2", null, "4" };
 
         resultArray = Utilities.trimArray(testArray);
         for (int i = 0;  i  < resultArray.length; i++) {
             assertEquals(expectedArray[i], resultArray[i]);
         }
     }
 
     @Test
     public void testFloatAsStringToInt() {
         int result = Utilities.checkedStringToInt("1234.5", 123);
         assertEquals(123, result);
     }
 
     @Test
     public void testIntAsStringToInt() {
         int result = Utilities.checkedStringToInt("3456", 123);
         assertEquals(3456, result);
     }
 
     @Test
     public void testHighlightTerm() {
         String test = "my test for replacement. replaces patterns within text";
         test = Utilities.highlightTerm(test, "replace");
         String expected = "my test for <strong>replace</strong>ment. <strong>replace</strong>s patterns within text";
         assertEquals(expected, test);
     }
 
     @Test
     public void formatReferencesDate() {
         String result;
         Calendar cal = new GregorianCalendar(2001, 7, 20);
         Date date = cal.getTime();
         result = Utilities.formatReferencesDate(date);
         assertEquals("2001", result);
     }
 
     @Test
     public void formatReferencesYear() {
         String result;
         result = Utilities.formatReferencesYear("2001");
         assertEquals("2001", result);
 
         result = Utilities.formatReferencesYear("2001-12-31");
         assertEquals("2001", result);
 
         result = Utilities.formatReferencesYear("2001-Dec-31");
         assertEquals("2001", result);
 
         // Why is there an exception for "0001"?
         result = Utilities.formatReferencesYear("0001");
         assertEquals("", result);
     }
 
     @Test
     public void getReferencesByIdDc() {
         SQLUtilities sqlUtils = DbHelper.getSqlUtilities();
         // The created date isn't set in the seed. We set it manually.
         //sqlUtils.UpdateSQL("UPDATE DC_INDEX SET CREATED='2004' WHERE ID_DC = 2414");
 
         // Create a reference that has a source. The seed in setUpBeforeClass is not used.
         sqlUtils.UpdateSQL("INSERT INTO DC_INDEX (ID_DC, TITLE, SOURCE, CREATED) VALUES (2500, 'atitle','source','2004')");
 
         String result = Utilities.getReferencesByIdDc("2500");
         String expected = "&lt;ul&gt;&lt;li&gt;Author : source&lt;/li&gt; &lt;li&gt;Title : atitle&lt;/li&gt; &lt;li&gt;Editor : &lt;/li&gt; &lt;li&gt;Publisher : &lt;/li&gt; &lt;li&gt;Date : 2004&lt;/li&gt; &lt;li&gt;Url : &lt;/li&gt;&lt;/ul&gt;";
         assertEquals(expected, result);
     }
 
 }
