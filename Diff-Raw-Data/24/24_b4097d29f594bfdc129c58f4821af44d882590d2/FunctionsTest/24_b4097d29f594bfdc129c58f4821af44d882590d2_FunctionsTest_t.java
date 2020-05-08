 package org.agmip.common;
 
import java.io.BufferedReader;
 import java.io.File;
import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import static org.agmip.common.Functions.*;
 import org.agmip.common.Functions.CompareMode;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class FunctionsTest {
 
     @Test
     public void numericStringNonRoundedTest() {
         int test = 1234;
         assertEquals("Numeric conversion failed", test, numericStringToBigInteger("1234.56", false).intValue());
     }
 
     @Test
     public void numericStringRoundedTest() {
         int test = 1235;
         assertEquals("Numeric conversion failed", test, numericStringToBigInteger("1234.56").intValue());
     }
 
     @Test
     public void toDateTest() {
         Date test = new Date(2012 - 1900, 0, 1);
         Date d = convertFromAgmipDateString("20120101");
         assertEquals("Dates not the same", test, d);
     }
 
     @Test
     public void toStringTest() {
         String test = "20120101";
         String d = convertToAgmipDateString(new Date(2012 - 1900, 0, 1));
         assertEquals("Dates not the same", test, d);
     }
 
     @Test
     public void failToDateTest() {
         Date d = convertFromAgmipDateString("1");
         assertNull("Converted invalid date", d);
     }
 
     @Test
     public void dateOffsetTest() {
         String test = "20120219";
         String initial = "20120212";
         String offset = "7";
 
         assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
     }
 
     @Test
     public void dateOffsetMonthBoundary() {
         String test = "20120703";
         String initial = "20120628";
         String offset = "5";
 
         assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
     }
 
     @Test
     public void dateOffsetYearBoundary() {
         String test = "20120101";
         String initial = "20111225";
         String offset = "7";
 
         assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
     }
 
     @Test
     public void dateOffsetReverseOffset() {
         String test = "20120101";
         String initial = "20120105";
         String offset = "-4";
 
         assertEquals("Date offset incorrect", test, dateOffset(initial, offset));
     }
 
     @Test
     public void failDateOffsetBadDate() {
         assertNull("Offset invalid date", dateOffset("1232", "1"));
     }
 
     @Test
     public void failDateOffsetBadOffset() {
         assertNull("Offset invalid offset", dateOffset("20120101", "abc"));
     }
 
     @Test
     public void failDateOffsetBadNumericOffsetTest() {
         assertNull("Offset invalid offset", dateOffset("20120101", "1.0"));
     }
 
     @Test
     public void failDateOffsetBadNumericOffset2Test() {
         assertNull("Offset invalid offset", dateOffset("20120101", "1.2"));
     }
 
     @Test
     public void numericOffsetTest() {
         String test = "12.34";
         String initial = "11.22";
         String offset = "1.12";
 
         assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
     }
 
     @Test
     public void integerOffsetTest() {
         String test = "12";
         String initial = "11";
         String offset = "1";
 
         assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
     }
 
     @Test
     public void mixedNumericOffsetTest() {
         String test = "12.34";
         String initial = "11";
         String offset = "1.34";
 
         assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
     }
 
     @Test
     public void failedNumericOffsetBadInitialTest() {
         assertNull("Offset invalid initial", numericOffset("abc", "12.34"));
     }
 
     @Test
     public void failedNumericOffsetBadOffsetTest() {
         assertNull("Offset invalid offset", numericOffset("12.34", "abc"));
     }
 
     @Test
     public void numericNegativeOffsetTest() {
         String test = "12.34";
         String initial = "23.45";
         String offset = "-11.11";
 
         assertEquals("Numeric offset incorrect", test, numericOffset(initial, offset));
     }
 
     @Test
     public void multiplySimple() {
         String test = "12.34";
         String f1 = "1234";
         String f2 = ".01";
 
         assertEquals("Multiply incorrect", test, multiply(f1, f2));
     }
 
     @Test
     public void mutliplyIntentionalFailure() {
         String f1 = "Hi";
         String f2 = "12";
 
         assertNull("This shouldn't work", multiply(f1, f2));
     }
 
     @Test
     public void sumSingle() {
         String test = "1234";
         String f1 = "1234";
 
         assertEquals("Sum incorrect", test, sum(f1));
     }
 
     @Test
     public void sumMultiple() {
         String test = "1234.01";
         String f1 = "1234";
         String f2 = ".01";
 
         assertEquals("Sum incorrect", test, sum(f1, f2));
     }
 
     @Test
     public void sumIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", sum(f1, f2));
     }
 
     @Test
     public void substractNull() {
         String test = "1234";
         String f1 = "1234";
 
         assertEquals("Substract incorrect", test, substract(f1));
     }
 
     @Test
     public void substractSingle() {
         String test = "1012";
         String f1 = "1234";
         String f2 = "222";
 
         assertEquals("Substract incorrect", test, substract(f1, f2));
     }
 
     @Test
     public void substractMultiple() {
         String test = "499.77";
         String f1 = "1234";
         String f2 = "234";
         String f3 = "500.23";
 
         assertEquals("Substract incorrect", test, substract(f1, f2, f3));
     }
 
     @Test
     public void substractIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", substract(f1, f2));
     }
 
     @Test
     public void productSingle() {
         String test = "1234";
         String f1 = "1234";
 
         assertEquals("Product incorrect", test, product(f1));
     }
 
     @Test
     public void productMultiple() {
         String test = "12.34";
         String f1 = "1234";
         String f2 = ".01";
 
         assertEquals("Product incorrect", test, product(f1, f2));
     }
 
     @Test
     public void productIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", product(f1, f2));
     }
 
     @Test
     public void divideSimpleDivisible() {
         String test = "2.55";
         String f1 = "10.2";
         String f2 = "4";
 
         assertEquals("Divide incorrect", test, divide(f1, f2));
     }
 
     @Test
     public void divideSimpleIndivisible() {
         String test = "10.2352";
         String f1 = "23.541";
         String f2 = "2.3";
 
         assertEquals("Divide incorrect", test, divide(f1, f2));
     }
 
     @Test
     public void divideWithScaleDivisible() {
         String test = "2.6";
         String f1 = "10.2";
         String f2 = "4";
         int scale = 1;
 
         assertEquals("Divide incorrect", test, divide(f1, f2, scale));
     }
 
     @Test
     public void divideWithScaleIndivisible() {
         String test = "10.2";
         String f1 = "23.541";
         String f2 = "2.3";
         int scale = 1;
 
         assertEquals("Divide incorrect", test, divide(f1, f2, scale));
     }
 
     @Test
     public void divideIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", divide(f1, f2));
     }
 
     @Test
     public void averageSimpleDivisible() {
         String test = "7.1";
         String f1 = "10.2";
         String f2 = "4";
 
         assertEquals("Average incorrect", test, average(f1, f2));
     }
 
     @Test
     public void averageSimpleIndivisible() {
         String test = "9.38553";
         String f1 = "23.541";
         String f2 = "2.3";
         String f3 = "2.3156";
 
         assertEquals("Average incorrect", test, average(f1, f2, f3));
     }
 
     @Test
     public void averageWithScaleDivisible() {
         String test = "7.10";
         String f1 = "10.2";
         String f2 = "4";
         int scale = 2;
 
         assertEquals("Average incorrect", test, average(scale, f1, f2));
     }
 
     @Test
     public void averageWithScaleIndivisible() {
         String test = "9.39";
         String f1 = "23.541";
         String f2 = "2.3";
         String f3 = "2.3156";
         int scale = 2;
 
         assertEquals("Average incorrect", test, average(scale, f1, f2, f3));
     }
 
     @Test
     public void averageIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", average(f1, f2));
     }
 
     @Test
     public void logWithScaleIndivisible() {
         String test = Math.log(12) + "";
         String f1 = "12";
 
         assertEquals("Log incorrect", test, log(f1));
     }
 
     @Test
     public void logIntentionalFailure() {
         String f1 = "H1";
 
         assertNull("This shouldn't work", log(f1));
     }
 
     @Test
     public void expWithScaleIndivisible() {
         String test = Math.exp(12) + "";
         String f1 = "12";
 
         assertEquals("Exp incorrect", test, exp(f1));
     }
 
     @Test
     public void expIntentionalFailure() {
         String f1 = "H1";
 
         assertNull("This shouldn't work", exp(f1));
     }
 
     @Test
     public void minSingle() {
         String test = "1234";
         String f1 = "1234";
 
         assertEquals("Min incorrect", test, min(f1));
     }
 
     @Test
     public void minMultiple() {
         String test = "0.01";
         String f1 = "1234";
         String f2 = ".01";
         String f3 = "3.01";
 
         assertEquals("Min incorrect", test, min(f1, f2, f3));
     }
 
     @Test
     public void minIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", min(f1, f2));
     }
 
     @Test
     public void maxSingle() {
         String test = "1234";
         String f1 = "1234";
 
         assertEquals("Max incorrect", test, max(f1));
     }
 
     @Test
     public void maxMultiple() {
         String test = "1234";
         String f1 = "1234";
         String f2 = ".01";
         String f3 = "3.01";
 
         assertEquals("Max incorrect", test, max(f1, f2, f3));
     }
 
     @Test
     public void maxIntentionalFailure() {
         String f1 = null;
         String f2 = "Hi";
 
         assertNull("This shouldn't work", max(f1, f2));
     }
 
     @Test
     public void roundUp() {
         String test = "0.13";
         String f1 = "0.1251";
         int scale = 2;
 
         assertEquals("Round incorrect", test, round(f1, scale));
     }
 
     @Test
     public void roundDown() {
         String test = "0.12";
         String f1 = "0.1249";
         int scale = 2;
 
         assertEquals("Round incorrect", test, round(f1, scale));
     }
 
     @Test
     public void roundIntentionalFailure() {
         String f1 = "Hi";
         int scale = 2;
 
         assertNull("This shouldn't work", round(f1, scale));
     }
 
     @Test
     public void compareGreaterMode() {
         String small = "1.1";
         String big = "2.1";
         CompareMode mode = CompareMode.GREATER;
 
         assertFalse("Compare incorrect", compare(small, big, mode));
         assertFalse("Compare incorrect", compare(big, big, mode));
         assertTrue("Compare incorrect", compare(big, small, mode));
     }
 
     @Test
     public void compareNotGreaterMode() {
         String small = "1.1";
         String big = "2.1";
         CompareMode mode = CompareMode.NOTGREATER;
 
         assertTrue("Compare incorrect", compare(small, big, mode));
         assertTrue("Compare incorrect", compare(big, big, mode));
         assertFalse("Compare incorrect", compare(big, small, mode));
     }
 
     @Test
     public void compareLessMode() {
         String small = "1.1";
         String big = "2.1";
         CompareMode mode = CompareMode.LESS;
 
         assertTrue("Compare incorrect", compare(small, big, mode));
         assertFalse("Compare incorrect", compare(big, big, mode));
         assertFalse("Compare incorrect", compare(big, small, mode));
     }
 
     @Test
     public void compareNotLessMode() {
         String small = "1.1";
         String big = "2.1";
         CompareMode mode = CompareMode.NOTLESS;
 
         assertFalse("Compare incorrect", compare(small, big, mode));
         assertTrue("Compare incorrect", compare(big, big, mode));
         assertTrue("Compare incorrect", compare(big, small, mode));
     }
 
     @Test
     public void compareEqualMode() {
         String small = "1.1";
         String big = "2.1";
         CompareMode mode = CompareMode.EQUAL;
 
         assertFalse("Compare incorrect", compare(small, big, mode));
         assertTrue("Compare incorrect", compare(big, big, mode));
         assertFalse("Compare incorrect", compare(big, small, mode));
     }
 
     @Test
     public void compareIntentionalFailure() {
         String f1 = "H1.1";
         String f2 = "2.1";
         CompareMode mode = CompareMode.LESS;
 
         assertFalse("This shouldn't work", compare(f1, f2, mode));
     }
 
     @Test
     public void removeNullNoNull() {
         String[] test = {"1234", "123"};
         String[] in = {"1234", "123"};
 
         in = removeNull(in);
         for (int i = 0; i < test.length; i++) {
             assertEquals("Should change anything", test[i], in[i]);
         }
         assertEquals("Should change anything", test.length, in.length);
     }
 
     @Test
     public void removeNullHasNull() {
         String[] test = {"1234", "123"};
         String[] in = {"1234", null, "123"};
 
         in = removeNull(in);
         for (int i = 0; i < test.length; i++) {
             assertEquals("Should change anything", test[i], in[i]);
         }
         assertEquals("Should remove the null element", test.length, in.length);
     }
 
     @Test
     public void removeNullNullArray() {
         String[] test = {};
         String[] in = null;
 
         in = removeNull(in);
         for (int i = 0; i < test.length; i++) {
             assertEquals("Should change anything", test[i], in[i]);
         }
         assertEquals("Should return a empty array", test.length, in.length);
     }
 
     @Test
     public void calcDAPNormal() {
         String date = "19810502";
         String pdate = "19810318";
         String expected = "45";
         String actual;
 
         actual = calcDAP(date, pdate);
         assertEquals("Should return differences of days", expected, actual);
     }
 
     @Test
     public void calcDAPInvalidDate() {
         String date = "";
         String pdate = "19810318";
         String expected = "";
         String actual;
 
         actual = calcDAP(date, pdate);
         assertEquals("Should return blank string", expected, actual);
     }
 
     @Test
     public void convertMsToDayNormal() {
         long ms = 3884400000l;
         String expected = "45";
         String actual;
 
         actual = convertMsToDay(ms);
         assertEquals("Should return correct number of days", expected, actual);
     }
     
     @Test
     public void clearDirectoryNormalDir() throws IOException {
         File f = new File("\\test");
         f.mkdir();
         assertTrue(clearDirectory(f));
     }
     
     @Test
     public void clearDirectoryNormalCombine() throws IOException {
         File f = new File("\\test");
         File f2 = new File("\\test\\a\\b.txt");
         f2.getParentFile().mkdirs();
         f2.createNewFile();
         assertTrue(clearDirectory(f));
     }
     
     @Test
     public void clearDirectoryNormalFile() throws IOException {
         File f = new File("test.txt");
         f.createNewFile();
         assertTrue(clearDirectory(f));
     }
     
     @Test
     public void clearDirectoryFailure() throws IOException {
         File f = new File("\\test");
        File f2 = new File("\\test\\a\\b.txt");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        FileWriter fw = new FileWriter(f2);
         assertTrue(!clearDirectory(f));
        fw.close();
        assertTrue(clearDirectory(f));
    }
    
    @Test
    public void clearDirectoryNotExist() throws IOException {
        File f = new File("\\test");
        assertTrue(clearDirectory(f));
     }
 }
