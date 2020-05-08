 /*
  * This program is free software. It comes without any warranty, to
  * the extent permitted by applicable law. You can redistribute it
  * and/or modify it under the terms of the Do What The Fuck You Want
  * To Public License, Version 2, as published by Sam Hocevar. See
  * http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.time;
 
 import org.joda.time.LocalDate;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 
 /**
  * Test cases for {@link FiscalDate#getFiscalWeekOfWeekyear()}.
  */
 @SuppressWarnings(CompilerWarnings.STATIC_METHOD)
 public class FiscalDateGetFiscalWeekOfWeekyearTest {
 
     /**
      * Ensures that the first week of a fiscal-year has the index number 1.
      */
     @Test
     public void shouldReturnFirstWeekAtStartOfFiscalYear() {
         // given
         final FiscalYearFactory fiscalYearFactory = FiscalYears.earlyFiscalYear(11);
         final FiscalDate fiscalDate = fiscalYearFactory.createFromCalendarDate(new LocalDate(2015, 11, 1));
 
         // when
         final int weekOfWeekyear = fiscalDate.getFiscalWeekOfWeekyear();
 
         // then
         Assert.assertEquals(1, weekOfWeekyear);
     }
 
     /**
      * Ensures that the last week of a fiscal-year has the index number 52.
      */
     @Test
     public void shouldReturnLastWeekAtEndOfFiscalYear() {
         // given
         final FiscalYearFactory fiscalYearFactory = FiscalYears.earlyFiscalYear(11);
        final FiscalDate fiscalDate = fiscalYearFactory.createFromCalendarDate(new LocalDate(2015, 10, 31));
 
         // when
         final int weekOfWeekyear = fiscalDate.getFiscalWeekOfWeekyear();
 
         // then
         Assert.assertEquals(52, weekOfWeekyear);
     }
 
 }
