 package com.seitenbau.testing.asserts.fest.impl;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.hamcrest.Description;
 import org.hamcrest.TypeSafeMatcher;
 import org.junit.ComparisonFailure;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import com.seitenbau.testing.asserts.fest.Assertions;
 import com.seitenbau.testing.util.DateUtil;
 
 public class DateAssertTest
 {
   Date datum = new Date();
 
   Date upperBorder = new Date(datum.getTime() + (15 * 1000));
 
   Date upperLimit = new Date(datum.getTime() + (15 * 1000) + 1);
 
   Date lowerBorder = new Date(datum.getTime() - (15 * 1000));
 
   Date lowerLimit = new Date(datum.getTime() - (15 * 1000) + 1);
 
   @Rule
   public ExpectedException exception = ExpectedException.none();
 
   /*
    * minutes miliSeconds isExactly isNow
    */
   @Test
   public void testIsValid()
   {
     new DateAssert(datum).is(datum);
   }
 
   @Test
   public void testIsNow()
   {
     new DateAssert(datum).isNow();
   }
 
   @Test
   public void testIsNull()
   {
     new DateAssert((Date) null).isNull();
   }
 
   @Test
   public void testIsNow1() throws InterruptedException
   {
     exception.expect(AssertionError.class);
     Thread.sleep(1);
     new DateAssert(datum).isNow(0);
   }
 
   @Test
   public void testIsValidExcatly()
   {
     new DateAssert(datum).isExactly(datum);
   }
 
   @Test
   public void testIsValidExcatlyString()
   {
     Date ref = DateUtil.asDate("2010-01-23 10:00");
     new DateAssert(ref).isExactly("23.01.2010 10:00");
   }
   
   @Test(expected=ComparisonFailure.class)
   public void testIsValidExcatlyString2()
   {
     Date ref = DateUtil.asDate("2010-01-23 10:00");
     new DateAssert(ref).isExactly("23.01.2010 10:01");
   }
 
   @Test
   public void testIsBorder()
   {
     new DateAssert(datum).is(upperBorder);
     new DateAssert(upperBorder).is(datum);
     new DateAssert(datum).is(lowerBorder);
     new DateAssert(lowerBorder).is(datum);
   }
 
   @Test(expected = AssertionError.class)
   public void testIsover()
   {
     new DateAssert(datum).is(upperLimit);
   }
 
   @Test
   public void testBuilder()
   {
     new DateAssert(datum).isIn(10).seconds();
   }
 
   @Test
   public void testBuilder_Seconds()
   {
     createSut(0).isIn(15).seconds();
   }
 
   @Test(expected = AssertionError.class)
   public void testBuilder_SecondsFail()
   {
     createSut(-1).isIn(15).seconds();
   }
 
   @Test
   public void testExactly()
   {
     final Date ref = new Date();
     Date past = new Date(ref.getTime());
     DateAssert sut = createSut(ref, past);
     sut.isExactly(ref);
   }
 
   @Test(expected = AssertionError.class)
   public void testExactlyFails()
   {
     final Date ref = new Date();
     Date past = new Date(ref.getTime() + 1);
     DateAssert sut = createSut(ref, ref);
     sut.isExactly(past);
   }
   
   @Test
   public void testExactlyFailsWithAs()
   {
      final Date ref = DateUtil.asDate("2012-12-01 10:03:02");
      Date past = new Date(ref.getTime() + 1);
       DateAssert sut = createSut(ref, ref);
      exception.expectMessage("[HI] expected Sat Dec 01 10:03:02 CET 2012 but was Sat Dec 01 10:03:02 CET 2012 diff=-1ms");
       sut.as("HI").isExactly(past);
   }
 
   @Test
   public void testDateCheckOnly()
   {
     createSut(0).onDate().isExactly(new Date());
   }
 
   @Test
   public void testDateCheckOnlyNoTime()
   {
     createSut("2010-02-01 10:00").onDate().isExactly("2010-02-01 15:01");
   }
 
   @Test
   public void testDateCheckOnlyYear()
   {
     expectComparisonException("[Year] expected:<201[1]> but was:<201[0]>");
     createSut("2010-02-01 10:00").onDate().isExactly("2011-02-01 10:01");
   }
 
   @Test
   public void testDateCheckOnlyMonth()
   {
     expectComparisonException("[Month] expected:<[3]> but was:<[2]>");
     createSut("2010-02-01 10:00").onDate().isExactly("2010-03-01 10:01");
   }
 
   @Test
   public void testDateCheckOnlyDay()
   {
     expectComparisonException("[Day] expected:<[3]> but was:<[1]>");
     createSut("2010-02-01 10:00").onDate().isExactly("2010-02-03 10:01");
   }
 
   @Test
   public void testTimeCheckOnlyNoDate()
   {
     createSut("2010-02-01 10:00").onTime().isExactly("2011-02-01 10:00");
   }
 
   @Test
   public void testtimeCheckOnlyHour()
   {
     expectComparisonException("[Hour] expected:<[9]> but was:<[10]>");
     createSut("2010-02-01 10:00").onTime().isExactly("2010-02-01 9:00");
   }
 
   @Test
   public void testtimeCheckOnlyMinute()
   {
     expectComparisonException("[Minute] expected:<[15]> but was:<[0]>");
     createSut("2010-02-01 10:00").onTime().isExactly("2010-02-01 10:15");
   }
 
   @Test
   public void testtimeCheckOnlySeconds()
   {
     expectComparisonException("[Second] expected:<[16]> but was:<[0]>");
     createSut("2010-02-01 10:00").onTime().isExactly("2010-02-01 10:00:16");
   }
   
   @Test
   public void testtimeCheckOnlyMiliSeconds()
   {
     expectComparisonException("[Milisecond] expected:<[92]> but was:<[0]>");
     createSut("2010-02-01 10:00").onTime().isExactly("2010-02-01 10:00:0.092");
   }
 
   private void expectComparisonException(final String expected)
   {
     exception.expect(new TypeSafeMatcher<ComparisonFailure>(ComparisonFailure.class)
     {
       public void describeTo(Description description)
       {
       }
 
       @Override
       protected boolean matchesSafely(ComparisonFailure item)
       {
         Assertions.assertThat(item.getMessage()).isEqualTo(expected);
         return true;
       }
     });
   }
 
   private DateAssert createSut(int offsetInMS)
   {
     return createSut(datum, offsetInMS);
   }
 
   private DateAssert createSut(Date ref, int offsetInMS)
   {
     Date past = new Date(ref.getTime() + offsetInMS);
     DateAssert sut = createSut(ref, past);
     return sut;
   }
 
   private DateAssert createSut(String actual)
   {
     DateAssert sut = createSut(datum, DateUtil.asDate(actual));
     return sut;
   }
 
   private DateAssert createSut(final Date refNow, Date actual)
   {
     DateAssert sut = new DateAssert(actual)
     {
       @Override
       Date getNow()
       {
         return refNow;
       }
     };
     return sut;
   }
 
 }
