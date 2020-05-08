 package org.pillarone.riskanalytics.domain.utils.datetime;
 
 import org.joda.time.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.pillarone.riskanalytics.core.simulation.NotInProjectionHorizon;
 import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;
 
 /**
  * Utility class for date, time and period calculations that are either
  * domain-specific, or generic but not already provided by org.joda.time classes.
  * <p/>
  * ben (dot) ginsberg (at) intuitive-collaboration (dot) com
  */
 public class DateTimeUtilities {
 
     /**
      * A readily available formatter in a form we often want when providing output to the user.
      */
     public static final DateTimeFormatter formatDate = DateTimeFormat.forPattern("dd-MMMM-yyyy");
 
     /**
      * Converts a string of the form YYYY-MM-DD (an ISO-2014 date or ISO-8601 calendar date) to a Joda DateTime
      *
      * @param date - date
      * @return - joda time
      */
     public static DateTime convertToDateTime(String date) {
         try {
             if (date.length() == 0) return null;
             return new DateTime((new SimpleDateFormat("yyyy-MM-dd")).parse(date));
         }
         catch (ParseException ex) {
             throw new RuntimeException(ex.getMessage(), ex);
         }
     }
 
     /**
      * Maps a date $t$ to a period $p \in \mathbf{Z}$ relative to a start
      * date $t_0$ and an interval length $d$ (for now, always one year).
      * Conceptually, period $p = \text{floor}(\frac{t-t_0}{d})$,
      * where $t, t_0 \& d$ have continuous time units.
      * However, although periods have equal calendrical duration, the linear time
      * duration of periods vary due to, e.g., leap years or variable month lengths.
      * <p/>
      * Periods may start anywhere within the calendar year, but they <b>must</b> last one year
      * (i.e. up to the same date on each following year, or each preceding year for negative periods).
      * <p/>
      * Usage://todo(bgi)
      * DateTime startOfPrevPeriod = new DateTime(2009,3,1,0,0,0,0);
      * DateTime startOfThisPeriod = new DateTime(2010,3,1,0,0,0,0);
      * DateTime startOfNextPeriod = new DateTime(2011,3,1,0,0,0,0);
      * DateTime lastPointInPrevPeriod = new DateTime(2010,2,28,23,59,59,999);
      * DateTime lastPointInThisPeriod = new DateTime(2011,2,28,23,59,59,999);
      * DateTime lastPointInNextPeriod = new DateTime(2012,2,29,23,59,59,999);
      * int
      * period = DateTimeUtilities.mapDateToPeriod(lastPointInNextPeriod, startOfThisPeriod) // 1
      * period = DateTimeUtilities.mapDateToPeriod(lastPointInPrevPeriod, startOfThisPeriod) // -1
      * <p/>
      * // todo(bgi): treat non-year periods, e.g. quarterly, monthly (rename this method to mapDateToCalendarYear)
      *
      * @param date
      * @param startDate
      * @return
      */
     public static int mapDateToPeriod(DateTime date, DateTime startDate) {
         Years years = Years.yearsBetween(startDate, date); // (today, yesterday) -> 'P0Y' (rounded integer!)
         int signedYearDiff = years.getYears();
         /**
          *  Count fractional years before startDate as whole years;
          *  i.e., compensate yearsBetween's unsigned time-difference rounding.
          *
          *  The implementation below is analagous to trying to compute the function floor() when we only have the
          *  (even/symmetric) function int(); however, the analogy overlooks the nonlinearity of calendrical periods
          */
         int startYear = startDate.getYear();
         if (date.isBefore(startDate)) {
             if (startYear > date.getYear()) {
                 /**
                  * Copy the date-to-map ('date') to the same calendar year as 'startDate' (i.e. within one period), to see
                  * whether it lies within, or before, the implied time period (the year-long interval starting at startDate)
                  */
                 MutableDateTime movedDate = new MutableDateTime(date);
                 movedDate.setYear(startYear);
                 if (movedDate.isAfter(startDate)) signedYearDiff--;
             } else {
                 /**
                  * Both dates are in the same calendar year, and we already know that date < startDate,
                  * so we know we need to apply the compensation
                  */
                 signedYearDiff--;
             }
         }
         return signedYearDiff;
     }
 
     /**
      * Maps a date $t$ to a fraction of period $f \in [0,1)$ representing elapsed time since start of period.
      * Currently only supports periods beginning at the beginning of a given year.
      *
      * @param date
      * @return
      */
     public static double mapDateToFractionOfPeriod(DateTime date) {
         return ((date.dayOfYear().get() - 1) / (date.year().isLeap() ? 366d : 365d));
     }
 
     public static Period simulationPeriodLength(PeriodScope periodScope) {
         return simulationPeriodLength(periodScope.getCurrentPeriodStartDate(), periodScope.getNextPeriodStartDate());
     }
 
     public static Period simulationPeriodLength(DateTime beginOfPeriod, DateTime endOfPeriod) {
         return new Period(beginOfPeriod, endOfPeriod);
     }
 
     public static int simulationPeriod(DateTime simulationStart, DateTime endOfFirstPeriod, DateTime date) {
         Period periodLength = simulationPeriodLength(simulationStart, endOfFirstPeriod);
         return simulationPeriod(simulationStart, periodLength, date);
     }
 
     public static int simulationPeriod(DateTime simulationStart, Period periodLength, DateTime date) {
         if (periodLength.getMonths() > 0) {
             double months = new Period(simulationStart, date, PeriodType.months()).getMonths() / (double) periodLength.getMonths();
             return months < 0 ? (int) Math.floor(months) : (int) months;
         } else if (periodLength.getMonths() == 0 && periodLength.getYears() > 0) {
             return new Period(simulationStart, date).getYears();
         }
         throw new IllegalArgumentException("['DateTimeUtilities.notImplemented','"
                         +simulationStart+"','"+periodLength+"','"+date+"']");
     }
 
     public static double dateAsDouble(DateTime simulationStart, DateTime endOfFirstPeriod, DateTime date) {
         Period periodLength = simulationPeriodLength(simulationStart, endOfFirstPeriod);
         int period = simulationPeriod(simulationStart, periodLength, date);
         DateTime beginOfPeriodForDate = new DateTime(simulationStart);
         for (int i = 0; i < period; i++) {
             beginOfPeriodForDate = beginOfPeriodForDate.plus(periodLength);
         }
         double periodLengthInDays = Days.daysBetween(simulationStart, endOfFirstPeriod).getDays();
         return periodLengthInDays == 0 ? 0 : period + Days.daysBetween(beginOfPeriodForDate, date).getDays() / periodLengthInDays;
     }
 
     public static DateTime getDate(DateTime beginOfPeriod, DateTime endOfPeriod, int periodOffset, double fractionOfPeriod) {
         Period periodLength = simulationPeriodLength(beginOfPeriod, endOfPeriod);
         DateTime shiftedDate = new DateTime(beginOfPeriod).plusDays((int) (fractionOfPeriod * Days.daysBetween(beginOfPeriod, endOfPeriod).getDays()));
         int sign = periodOffset >= 0 ? 1 : -1;
         periodOffset = sign * periodOffset;
         Period shift = new Period();
         while (periodOffset > 0) {
             shift = shift.plus(periodLength);
             periodOffset--;
         }
         if (sign == 1) {
             shiftedDate = shiftedDate.plus(shift);
         } else {
             shiftedDate = shiftedDate.minus(shift);
         }
         return shiftedDate;
     }
 
     public static DateTime getDate(DateTime startDate, Period periodLength, double periodOffset) {
         DateTime endOfFirstPeriod = startDate.plus(periodLength);
         return getDate(startDate, endOfFirstPeriod, (int) Math.floor(periodOffset), periodOffset - Math.floor(periodOffset));
     }
 
     public static DateTime getDate(PeriodScope periodScope, double fractionOfPeriod) {
         return getDate(periodScope.getCurrentPeriodStartDate(), periodScope.getNextPeriodStartDate(), 0, fractionOfPeriod);
     }
 
     public static DateTime getDate(PeriodScope periodScope, int period, double fractionOfPeriod) {
         try {
             DateTime beginOfPeriod = periodScope.getPeriodCounter().startOfPeriod(period);
             DateTime endOfPeriod = periodScope.getPeriodCounter().endOfPeriod(period);
             return getDate(beginOfPeriod, endOfPeriod, 0, fractionOfPeriod);
         }
         catch (NotInProjectionHorizon ex) {
             throw new IllegalArgumentException("Period " + period + " is not within projection horizon.");
         }
     }
 
     /**
      *  This function calculates the months between two dates including a fraction. The fraction is calculated in the last month.
      * @param startDate - start date
      * @param endDate - end date
      * @return - fraction of months.
      */
     public static double deriveNumberOfMonths(DateTime startDate, DateTime endDate) {
         int numberOfCompleteMonths = Months.monthsBetween(startDate, endDate).getMonths();
         double fractionInLastMonth = 0d;
         if (startDate.plusMonths(numberOfCompleteMonths).isBefore(endDate)) {
             fractionInLastMonth = Days.daysBetween(startDate.plusMonths(numberOfCompleteMonths), endDate).getDays() /
                     (double) Days.daysBetween(startDate.plusMonths(numberOfCompleteMonths), startDate.plusMonths(numberOfCompleteMonths + 1)).getDays();
         }
         return numberOfCompleteMonths + fractionInLastMonth;
     }
 
     public static double getInterestRateForTimeInterval(double yearlyRate, DateTime startDate, DateTime endDate){
          /**
          * Basis of interval interest rate is the fiscal (calendar) year.
          * Convention for intervals: interval = [startDate,endDate), that is, the left boundary of interval
          * is included, the right boundary excluded.
          * */
         double intervalInterestRate = 0;
         int startYear = startDate.getYear();
         int endYear = endDate.getYear();
         int numberOfCoveredYears = endYear - startYear;
 
         double numberOfDaysInYear = startDate.year().isLeap() ? 366 : 365;
         double runTime = Math.min(Days.daysBetween(startDate, endDate).getDays(),
                 Days.daysBetween(startDate, new DateTime(startYear + 1, 1, 1, 0, 0, 0, 0)).getDays()) / numberOfDaysInYear;
 
         double interestValue = runTime == 1 ? 1 + yearlyRate : Math.pow(1 + yearlyRate, runTime);
 
         for (int i = 1; i <= numberOfCoveredYears; i++) {
             numberOfDaysInYear = Days.daysBetween(new DateTime(startYear + i, 1, 1, 0, 0, 0, 0),
                     new DateTime(startYear + i + 1, 1, 1, 0, 0, 0, 0)).getDays();
             runTime = Math.min(Days.daysBetween(new DateTime(startYear + i, 1, 1, 0, 0, 0, 0),
                     new DateTime(startYear + i + 1, 1, 1, 0, 0, 0, 0)).getDays(),
                     Days.daysBetween(new DateTime(startYear + i, 1, 1, 0, 0, 0, 0), endDate).getDays()) / numberOfDaysInYear;
 
             if (runTime == 1) {
                 interestValue *= (1.0 + yearlyRate);
             }
             else {
                 interestValue *= Math.pow(1.0 + yearlyRate, runTime);
             }
         }
         intervalInterestRate = interestValue - 1.0;
         return intervalInterestRate;
     }
 
     /**
      * Utility function for use mainly in interest calculation and interpolations.
      *
      * @param startDate - start date
      * @param endDate - end date
      * @return integer number of days between dates assuming every month has 30 days.
      */
     public static int days360(DateTime startDate, DateTime endDate) {
 
         int startDayOfMonth = (startDate.getDayOfMonth() == 31) ? 30 : startDate.getDayOfMonth();
         int startDay = startDate.getMonthOfYear() * 30 + startDayOfMonth;
        int endDayOfMonth = endDate.getDayOfMonth() == 31 ? 30 : endDate.getDayOfMonth();
        int endDay = (endDate.getYear() - startDate.getYear()) * 360 + endDate.getMonthOfYear() * 30 + endDayOfMonth;
 
         return endDay - startDay;
     }
 
    private DateTime getEndingDateAdjustedForStartDate(DateTime startDate, DateTime endDate) {

//        if(endDate.dayOfMonth().withMaximumValue().equals(endDate) ) {
//            if(startDate.getDayOfMonth() < 30 ) {
//                return endDate.plusMonths(1).dayOfMonth().withMinimumValue();
//            }
//        }

        return new DateTime();

    }

//private Calendar getEndingDateAccordingToStartingDate(double date, Calendar startingDate) {
//        +        Calendar endingDate = getDate(date);
//        +        endingDate.setTime(DateUtil.getJavaDate(date, false));
//        +        if (isLastDayOfMonth(endingDate)) {
//            +            if (startingDate.get(Calendar.DATE) < 30) {
//                +                endingDate = getFirstDayOfNextMonth(endingDate);
//                +            }
//            +        }
//        +        return endingDate;
//        +    }

     /**
      * @param startDate start date
      * @param endDate end date
      * @return fraction of number of months between dates assuming every month has 30 days
      */
     public static double months360(DateTime startDate, DateTime endDate) {
         return days360(startDate, endDate) / 30d;
     }
 
     /**
      *
      * Returns the proportion of a period (periodStart -> periodEnd ) up to the 'toDate'
      *
      * @param periodStart - start date
      * @param periodEnd - end date
      * @param toDate - to this date as a proportion of period
      * @return proportion of the period (periodStart -> periodEnd ) from periodStart to 'toDate'
      * @exception IllegalArgumentException if toDate not contained between periodStart and periodEnd.
      */
     public static double days360ProportionOfPeriod(DateTime periodStart, DateTime periodEnd, DateTime toDate  ) {
 
 //        Check that the 'toDate' actually falls in the period.
         if(! (
                 (periodStart.isBefore(toDate) || periodStart.isEqual(toDate))
                     &&
                 ( periodEnd.isAfter(toDate) || periodEnd.isEqual(toDate))
             )
         ) {
             throw new IllegalArgumentException(" Attempted to find the proportion of a period where the specified " +
                     "period does not include the date of interest. Please contact development ");
         }
         double daysInPeriod = days360(periodStart, periodEnd);
         double daysToUpdate = days360(periodStart, toDate);
         return  daysToUpdate / daysInPeriod;
     }
 
     /**
      * Checks that the 'check date' is strictly between the start and end date.
      *
      * @param startDate - start date
      * @param endDate - end date
      * @param checkDate - check date is between
      * @return True if check date is strictly between (not equal to) start and end date. Otherwise false.
      */
     public static boolean isBetween(DateTime startDate, DateTime endDate, DateTime checkDate) {
         assert startDate != null;
         assert endDate != null;
         assert checkDate != null;
         return checkDate.isBefore(endDate) && (checkDate.isAfter(startDate));
     }
 
     /**
      *
      *
      * @param map - DateTime, Double map, for instance premiums indexed by date with their values
      * @param startDate - start of the period to sum
      * @param endDate - end of the period to sum
      * @return Sum of the values in the map inside the date range.
      */
     public static double sumDateTimeDoubleMapByDateRange(Map<DateTime, Double> map, DateTime startDate, DateTime endDate ){
         assert startDate != null;
         assert endDate != null;
 
         double returnValue = 0;
         for (Map.Entry<DateTime, Double> entry : map.entrySet()) {
             if( isBetween(startDate, endDate, entry.getKey())) {
                 returnValue += entry.getValue();
             }
         }
         return returnValue;
     }
 
 }
