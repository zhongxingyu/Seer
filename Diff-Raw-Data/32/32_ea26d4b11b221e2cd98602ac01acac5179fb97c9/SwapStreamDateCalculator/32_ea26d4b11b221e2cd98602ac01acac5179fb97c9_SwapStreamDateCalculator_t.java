 package com.twock.swappricer.fpml;
 
 import java.util.*;
 
 import com.twock.swappricer.DateUtil;
 import com.twock.swappricer.HolidayCalendarContainer;
 import com.twock.swappricer.PricerException;
 import com.twock.swappricer.fpml.model.*;
 import com.twock.swappricer.fpml.model.enumeration.*;
 
 /**
  * @author Chris Pearson (chris@twock.com)
  */
 public class SwapStreamDateCalculator {
 
   private static final EnumSet<PeriodEnum> MONTH_OR_YEAR = EnumSet.of(PeriodEnum.M, PeriodEnum.Y);
 
   /**
    * Work out from the period dates whether the first period is a stub or not.
    *
    * @param date1 unadjusted period start date
    * @param date2 unadjusted period end date
    * @param calculationPeriodFrequency swap stream's period frequency
    * @return true if there is an initial stub, false otherwise
    */
   public boolean hasInitialStub(DateWithDayCount date1, DateWithDayCount date2, CalculationPeriodFrequency calculationPeriodFrequency) {
     if(!matchesRollConvention(date1, calculationPeriodFrequency.getRollConvention())) {
       return true;
     }
     DateWithDayCount dateCopy = new DateWithDayCount(date1);
     addPeriod(dateCopy, calculationPeriodFrequency.getPeriod(), calculationPeriodFrequency.getPeriodMultiplier(), calculationPeriodFrequency.getRollConvention());
     return date2.compareTo(dateCopy) == 0;
   }
 
   /**
    * Find whether the given date complies with the supplied roll convention.
    *
    * @param date the date to check
    * @param rollConvention the roll convention to check the date against
    * @return true if the date matches the roll convention
    */
   public boolean matchesRollConvention(DateWithDayCount date, RollConventionEnum rollConvention) {
     switch(rollConvention) {
       case EOM:
         short[] nextDate = DateUtil.dayCountToDate(date.getDayCount() + 1);
         return nextDate[1] != date.getDate()[1];
       case IMM:
         // third wednesday of the month
         if(date.getDayOfWeek() != Calendar.WEDNESDAY) {
           return false;
         }
         // 1 sunday, 2 mon, 3 tue, 4 wed,..., 7 sat
         // if dow=1, add 17     11-dow=3
         // if dow=2, add 16     11-dow=2
         // if dow=3, add 15     11-dow=1
         // if dow=4, add 14     11-dow=0
         // if dow=5, add 20     11-dow=6
         // if dow=6, add 19     11-dow=5
         // if dow=7, add 18     11-dow=4
         // so add 14+(11-dow)%7
         DateWithDayCount firstOfMonth = new DateWithDayCount(date.getYear(), date.getMonthOfYear(), 1);
         int dayOfWeek = firstOfMonth.getDayOfWeek();
         int immDayCount = firstOfMonth.getDayCount() + 14 + ((11 - dayOfWeek) % 7);
         return immDayCount == date.getDayCount();
 
       default:
         if(!rollConvention.value().matches("\\d+")) {
           throw new PricerException("Unhandled roll convention: " + rollConvention);
         }
         int roll = Integer.parseInt(rollConvention.value());
         if(roll == date.getDate()[2]) {
           return true;
         }
         // roll conventions greater than number of days in the month must match too
         if(roll >= 29) {
           nextDate = DateUtil.dayCountToDate(date.getDayCount() + 1);
           return nextDate[1] != date.getDate()[1];
         } else {
           return false;
         }
     }
   }
 
   /**
    * Adjust the given date according to the business day convention and calendars provided.
    *
    * @param date date to adjust - this date will be altered
    * @param businessDayConvention business day convention to apply
    * @param calendars holiday calendars in use, can be null
    * @return startDate, for convenience/method chaining
    */
   public DateWithDayCount adjustDate(DateWithDayCount date, BusinessDayConventionEnum businessDayConvention, HolidayCalendarContainer calendars) {
     if(businessDayConvention == BusinessDayConventionEnum.NO_ADJUST) {
       return date;
     }
     short startMonth = businessDayConvention == BusinessDayConventionEnum.MODFOLLOWING || businessDayConvention == BusinessDayConventionEnum.MODPRECEDING ? date.getMonthOfYear() : 0;
     int startDate = date.getDayCount();
     while(calendars.isWeekendOrPublicHoliday(date)) {
       int dayOfWeek = date.getDayOfWeek();
       switch(businessDayConvention) {
         case FOLLOWING:
           date.addDays(dayOfWeek == Calendar.SATURDAY ? 2 : 1);
           break;
         case MODFOLLOWING:
           date.addDays(1);
           if(date.getMonthOfYear() != startMonth) {
             // if we've strayed to the next month, rewind to the starting date, and go backwards instead
             date.setDayCount(startDate);
             return adjustDate(date, BusinessDayConventionEnum.PRECEDING, calendars);
           }
           break;
         case PRECEDING:
           date.addDays(dayOfWeek == Calendar.SUNDAY ? -2 : -1);
           break;
         case MODPRECEDING:
           date.addDays(-1);
           if(date.getMonthOfYear() != startMonth) {
             // if we've strayed to the previous month, fast forward to the starting date, and go forwards instead
             date.setDayCount(startDate);
             return adjustDate(date, BusinessDayConventionEnum.FOLLOWING, calendars);
           }
           break;
         case NEAREST:
           if(dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY) {
             return adjustDate(date, BusinessDayConventionEnum.FOLLOWING, calendars);
           } else {
             return adjustDate(date, BusinessDayConventionEnum.PRECEDING, calendars);
           }
         default:
           throw new PricerException("Unhandled business day convention " + businessDayConvention);
       }
     }
     return date;
   }
 
   /**
    * Given the necessary information to calculate the schedule of unadjusted period dates, calculate them and return as
    * a list.
    *
    * @param effectiveDate swap stream effective date
    * @param firstRegularPeriodStartDate beginning of the first regular period if specified in the FpML, null otherwise
    * @param lastRegularPeriodEndDate end of the last regular period if specified in the FpML, null otherwise
    * @param terminationDate swap stream end date
    * @param calculationPeriodFrequency period frequency and roll convention
    * @return the unadjusted period dates in order
    */
   public List<DateWithDayCount> calculateUnadjustedPeriodDates(DateWithDayCount effectiveDate, DateWithDayCount firstRegularPeriodStartDate, DateWithDayCount lastRegularPeriodEndDate, DateWithDayCount terminationDate, CalculationPeriodFrequency calculationPeriodFrequency) {
     List<DateWithDayCount> result = new ArrayList<DateWithDayCount>();
     result.add(effectiveDate);
 
     if(calculationPeriodFrequency.getPeriod() != PeriodEnum.T) {
       DateWithDayCount start = effectiveDate;
       if(firstRegularPeriodStartDate != null && firstRegularPeriodStartDate.compareTo(terminationDate) < 0) {
         result.add(firstRegularPeriodStartDate);
         start = firstRegularPeriodStartDate;
       }
       DateWithDayCount end = lastRegularPeriodEndDate == null ? terminationDate : lastRegularPeriodEndDate;
 
       DateWithDayCount current = new DateWithDayCount(start);
       while(current.compareTo(end) < 0) {
         addPeriod(current, calculationPeriodFrequency.getPeriod(), calculationPeriodFrequency.getPeriodMultiplier(), calculationPeriodFrequency.getRollConvention());
         if(current.compareTo(end) < 0) {
           result.add(new DateWithDayCount(current));
         }
       }
 
       if(lastRegularPeriodEndDate != null && lastRegularPeriodEndDate.compareTo(effectiveDate) > 0) {
         result.add(lastRegularPeriodEndDate);
       }
     }
     result.add(terminationDate);
     return result;
   }
 
   /**
    * Take a given date and modify it by adding the given calculation period to it, and applying the roll convention.
    *
    * @param current the date to modify
    * @param period unit of periods
    * @param periodMultiplier number of units per period
    * @param rollConvention roll convention for monthly/yearly rolls
    */
   private static void addPeriod(DateWithDayCount current, PeriodEnum period, Integer periodMultiplier, RollConventionEnum rollConvention) {
     switch(period) {
       case D:
         current.addDays(periodMultiplier);
         break;
       case W:
         current.addDays(7 * periodMultiplier);
         break;
       case M:
         current.addMonths(periodMultiplier, rollConvention);
         break;
       case Y:
         current.addMonths(periodMultiplier * 12, rollConvention);
         break;
     }
   }
 
   /**
    * When given a list of unadjusted period dates, and the necessary business day adjustments and holiday calendars,
    * calculate and return the list of adjusted calculation period dates.  Where possible tries to use the same date
    * objects in the returned list, so beware side effects when altering dates in the returned list.  Will leave dates
    * in unadjustedDates unmodified.
    *
    * @param unadjustedDates the pre-calculated list of unadjusted period dates
    * @param effectiveDateAdjustments adjustments to apply to the first date only
    * @param normalAdjustments adjustments to apply to every date except first and last
    * @param terminationDateAdjustments adjustments to apply to the last date only
    * @param allCalendars all holiday calendars in existence, appropriate calendars will be extracted
    * @return the adjusted period dates, the list will be the same length as the unadjustedDates parameter
    */
   public List<DateWithDayCount> calculateAdjustedPeriodDates(List<DateWithDayCount> unadjustedDates, BusinessDayAdjustments effectiveDateAdjustments, BusinessDayAdjustments normalAdjustments, BusinessDayAdjustments terminationDateAdjustments, HolidayCalendarContainer allCalendars) {
     HolidayCalendarContainer[] calendars = {
       new HolidayCalendarContainer(allCalendars, effectiveDateAdjustments.getBusinessCenters()),
       new HolidayCalendarContainer(allCalendars, normalAdjustments.getBusinessCenters()),
       new HolidayCalendarContainer(allCalendars, terminationDateAdjustments.getBusinessCenters())
     };
     List<DateWithDayCount> adjustedDates = new ArrayList<DateWithDayCount>(unadjustedDates.size());
     DateWithDayCount temp = new DateWithDayCount(0);
     for(int index = 0, last = unadjustedDates.size() - 1; index <= last; index++) {
       DateWithDayCount toAdjust = unadjustedDates.get(index);
       temp.setDayCount(toAdjust.getDayCount());
       adjustDate(temp,
         index == 0 ? effectiveDateAdjustments.getBusinessDayConvention() : (index == last ? terminationDateAdjustments.getBusinessDayConvention() : normalAdjustments.getBusinessDayConvention()),
         calendars[index == 0 ? 0 : (index == last ? 2 : 1)]);
       if(temp.compareTo(toAdjust) == 0) {
         adjustedDates.add(toAdjust);
       } else {
         adjustedDates.add(temp);
         temp = new DateWithDayCount(0);
       }
     }
     return adjustedDates;
   }
 
   /**
    * Take a date and shift it according to the parameters.  Used for fixing lags, payment lags, etc.  This method
    * modifies the provided date, and does not create a new DateWithDayCount object.
    *
    * @param date the date to adjust, this will be modified by the message
    * @param periodMultiplier number of periods to shift by, can be negative
    * @param period period type, the only acceptable period is days
    * @param dayType type applied to days
    * @param businessDayConvention business day convention to apply
    * @param holidayCalendarContainer all holiday calendars, relevant ones will be extracted for use
    * @return for convenience, returns the modified date paramter for method chaining
    */
   public DateWithDayCount shift(DateWithDayCount date, int periodMultiplier, PeriodEnum period, DayTypeEnum dayType, BusinessDayConventionEnum businessDayConvention, HolidayCalendarContainer holidayCalendarContainer) {
     if(period != PeriodEnum.D) {
       throw new PricerException("Unhandled period " + period + ", expected D");
     }
     switch(dayType) {
       case BUSINESS:
         int signum = Integer.signum(periodMultiplier);
         int abs = Math.abs(periodMultiplier);
         for(int i = 0; i < abs; i++) {
           date.addDays(signum);
           adjustDate(date, signum == -1 ? BusinessDayConventionEnum.PRECEDING : BusinessDayConventionEnum.FOLLOWING, holidayCalendarContainer);
         }
         break;
       case CALENDAR:
         date.addDays(periodMultiplier);
         adjustDate(date, businessDayConvention, holidayCalendarContainer);
         break;
       default:
         throw new PricerException("Unhandled dayType " + dayType + ", expected BUSINESS or CALENDAR");
     }
     return date;
   }
 
   /**
    * When given a list of unadjusted period dates and the necessary paymentDates schedule, will calculate the payment
    * dates to accompany each calculation period.  Where possible this method will the date instances from the supplied
    * unadjustedDates parameter rather than creating new ones.
    *
    * @param unadjustedDates pre-calculated unadjusted period dates
    * @param paymentDates rules for payment date calculation
    * @param allCalendars all holiday calendars, relevant ones will be extracted for use
    * @return a list of payment dates, one per period - so length will be one less than the number of unadjustedDates
    */
   public List<DateWithDayCount> calculatePaymentDates(List<DateWithDayCount> unadjustedDates, PaymentDates paymentDates, HolidayCalendarContainer allCalendars) {
     // todo compounded payment dates
     // calculate unadjusted dates for payment schedule from effective/first regular payment/last regular payment/termination
     // to match up to period need to know multiplier, i.e. 1m period 1y payment = 12x periods per payment
     HolidayCalendarContainer paymentCalendars = new HolidayCalendarContainer(allCalendars, paymentDates.getPaymentDatesAdjustments().getBusinessCenters());
     List<DateWithDayCount> result = new ArrayList<DateWithDayCount>(unadjustedDates.size() - 1);
     boolean payInArrears = paymentDates.getPayRelativeTo() == PayRelativeToEnum.CALCULATION_PERIOD_END_DATE;
     DateWithDayCount temp = new DateWithDayCount(0);
     for(int i = payInArrears ? 1 : 0, last = unadjustedDates.size() - (payInArrears ? 1 : 2); i <= last; i++) {
       DateWithDayCount unadjusted = unadjustedDates.get(i);
       temp.setDayCount(unadjusted.getDayCount());
       adjustDate(temp, paymentDates.getPaymentDatesAdjustments().getBusinessDayConvention(), paymentCalendars);
       if(temp.compareTo(unadjusted) == 0) {
         result.add(unadjusted);
       } else {
         result.add(temp);
         temp = new DateWithDayCount(0);
       }
     }
     return result;
   }
 
   /**
    * Given a set of dates and a day count fraction, calculate the number of days in each period for calculation
    * purposes.
    *
    * @param adjustedDates a list of period dates
    * @param dayCountFraction the day count fraction to use for calculation
    * @param regularCalculationPeriod frequency information for regular calculation periods
    * @param hasInitialStub whether the initial period is irregular
    * @param hasFinalStub whether the final period is irregular
    * @return number of days in each period, so array will be one smaller than adjustedDates parameter
    */
   public double[] getDayCountFractions(List<DateWithDayCount> adjustedDates, DayCountFractionEnum dayCountFraction, CalculationPeriodFrequency regularCalculationPeriod, boolean hasInitialStub, boolean hasFinalStub) {
     if(dayCountFraction == DayCountFractionEnum.ACT_ACT_ICMA || dayCountFraction == DayCountFractionEnum.ACT_ACT_ISMA) {
       if(regularCalculationPeriod == null) {
         throw new PricerException("Regular calculation period required for " + dayCountFraction.value());
       } else if(!MONTH_OR_YEAR.contains(regularCalculationPeriod.getPeriod())) {
         throw new PricerException("Can only handle yearly or monthly regular calculation periods for " + dayCountFraction.value());
       }
     }
     double[] result = new double[adjustedDates.size() - 1];
     for(int i = 0, last = adjustedDates.size() - 2; i <= last; i++) {
       DateWithDayCount startDate = adjustedDates.get(i);
       DateWithDayCount endDate = adjustedDates.get(i + 1);
       switch(dayCountFraction) {
         case SINGLE:
           result[i] = 1;
           break;
         case ACT_ACT_ISDA:
           short startYear = startDate.getYear();
           short endYear = endDate.getYear();
           if(startYear == endYear) {
             result[i] = (double)(endDate.getDayCount() - startDate.getDayCount()) / DateUtil.daysInYear(startYear);
           } else {
             result[i] = (double)(DateUtil.dateToDayCount(new short[]{(short)(startYear + 1), 1, 1}) - startDate.getDayCount()) / DateUtil.daysInYear(startYear)
               + endYear - startYear - 1
               + (double)(endDate.getDayCount() - DateUtil.dateToDayCount(new short[]{endYear, 1, 1})) / DateUtil.daysInYear(endYear);
           }
           break;
         case ACT_ACT_ICMA:
         case ACT_ACT_ISMA:
           double periodsPerYear = 12.0 / (regularCalculationPeriod.getPeriod() == PeriodEnum.Y ? 12 * regularCalculationPeriod.getPeriodMultiplier() : regularCalculationPeriod.getPeriodMultiplier());
           result[i] = 0;
           if(hasInitialStub && i == 0) {
             DateWithDayCount tempEndDate = new DateWithDayCount(endDate);
             DateWithDayCount tempStartDate = new DateWithDayCount(0);
             while(tempEndDate.compareTo(startDate) > 0) {
               tempStartDate.setDayCount(tempEndDate.getDayCount());
               addPeriod(tempStartDate, regularCalculationPeriod.getPeriod(), -regularCalculationPeriod.getPeriodMultiplier(), regularCalculationPeriod.getRollConvention());
               result[i] += (double)(tempEndDate.getDayCount() - Math.max(tempStartDate.getDayCount(), startDate.getDayCount())) / (periodsPerYear * (tempEndDate.getDayCount() - tempStartDate.getDayCount()));
               tempEndDate.setDayCount(tempStartDate.getDayCount());
             }
           } else if(hasFinalStub && i == last) {
             DateWithDayCount tempStartDate = new DateWithDayCount(startDate);
             DateWithDayCount tempEndDate = new DateWithDayCount(0);
             while(tempStartDate.compareTo(endDate) < 0) {
               tempEndDate.setDayCount(tempStartDate.getDayCount());
               addPeriod(tempEndDate, regularCalculationPeriod.getPeriod(), regularCalculationPeriod.getPeriodMultiplier(), regularCalculationPeriod.getRollConvention());
               result[i] += (double)(Math.min(tempEndDate.getDayCount(), endDate.getDayCount()) - tempStartDate.getDayCount()) / (periodsPerYear * (tempEndDate.getDayCount() - tempStartDate.getDayCount()));
               tempStartDate.setDayCount(tempEndDate.getDayCount());
             }
           } else {
             result[i] = 1.0 / periodsPerYear;
           }
           break;
        case ACT_365_FIXED:
          result[i] = (double)(endDate.getDayCount() - startDate.getDayCount()) / 365;
          break;
        case ACT_360:
          result[i] = (double)(endDate.getDayCount() - startDate.getDayCount()) / 360;
          break;
         default:
           throw new PricerException("Unhandled day count fraction " + dayCountFraction);
       }
     }
     return result;
   }
 }
