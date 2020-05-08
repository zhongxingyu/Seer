 package com.business.utilities;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
import org.apache.commons.lang3.time.FastDateFormat;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CommonTest {
     
     private static final Logger LOG = LoggerFactory.getLogger(CommonTest.class);
 
     @Test
     public void testGetStateTerritoryAbbrevs() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetStateAbbrevs() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetFullNameStateTerritoryList() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetFullNameStateList() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetAbbrevStateTerritoryMap() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetAbbrevStateMap() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testNullEqual() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetStackTrace() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testStripNonNumericString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testStripNonNumericStringBoolean() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testStripNonNumericStringBooleanBoolean() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatDouble() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyDropSign() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyNaForZero() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testReformatNegs() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyBigDecimal() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyDouble() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatCurrencyObject() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testUnformatCurrency() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertStringCheckToNumString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertLongToBoolean() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertCheckToNumString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatSsn() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsOptionsCollection() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsOptionsCollectionUpperCaseValue() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetRowMap() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetListInString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsStringObject() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsStringObjectString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsSqlDate() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsTimestampObjectString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testAsTimestampString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatted() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testString2Date() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertStringToDate() {
        List<String> strs = Arrays.asList("10/31/2013 3:58 PM, EDT", "11/07/13", "2013-11-07 16:22:17.36", "2013-11-07 16:22:17.999", "2013-11-07 16:22:17.6", "2013-11-07 16:22:17.099");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd'T'HHmmss.S Z");
         for(String inputDateStr : strs){
             System.out.println(Pattern.matches("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{2} (AM|PM|am|pm), ([a-zA-Z]){3}", inputDateStr));
             Date d = Common.convertStringToDate(inputDateStr);
             LOG.debug(inputDateStr + " converted to date: " + d );
            System.out.println(inputDateStr + " converted to date: " + d + " [formatted: " + fdf.format(d) + "]") ;
         }
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFindFirstDateInString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertStringToSQLDate() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testISO8601ToDate() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testRoundToNumPlaces() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testSleep() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testCheckDateStringInt() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testNormalizeWhiteSpace() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testSplitAndTrim() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetMonths() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetYearsIntInt() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetYearsStringString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testIsHexadecimal() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testCreateBigDecimal() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testIsLong() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConvertArraysToLists() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testTrimArray() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testIsValidEmail() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testIsZeroString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testIsTBDAddress() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testDbListToList() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testContainsIgnoreCase() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testClean() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testBlankStringInMap() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testStringEqualsInMap() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatDateToStringObject() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testFormatDateToStringObjectString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testMapGet() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testCheckDateStringString() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testBuildOptions() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testConcatDescriptions() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetCallingMethod() {
         //fail("Not yet implemented");
     }
 
     @Test
     public void testGetFullNameCountryList() {
         //fail("Not yet implemented");
     }
 
 }
