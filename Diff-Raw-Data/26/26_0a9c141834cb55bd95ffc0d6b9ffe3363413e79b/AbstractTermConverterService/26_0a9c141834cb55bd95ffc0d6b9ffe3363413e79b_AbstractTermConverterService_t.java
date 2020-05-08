 package uk.ac.ox.oucs.termdates;
 
 import com.google.common.collect.BiMap;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import uk.ac.ox.oucs.termdates.exception.WeekNameNotFoundException;
 
 import java.util.Date;
 
 /**
  * @author Colin Hebert
  */
 public abstract class AbstractTermConverterService implements TermConverterService {
 
     /**
      * Number of weeks in the year
      * <p>
      * Wrong because there is not always 53 weeks in a year, that's why sometimes the {@link #getReferential()} method
      * must change.
      * </p>
      * <p>
      * It's NOT possible to know for sure when the academic year will be only on 52 weeks (or less!).
      * TODO: Extract this data from {@link #getWeekNames()}? This way 52 weeks calendars can be easily made.
     * </p>
      */
     public static final int WEEKS_IN_YEAR = 53;
 
     @Override
     public String getWeekName() {
         return getWeekName(DateTime.now().toDate());
     }
 
     @Override
     public int getTermWeekNumber() {
         return getTermWeekNumber(DateTime.now().toDate());
     }
 
     @Override
     public String getWeekName(Date date) {
         return getWeekName(getTermWeekNumber(date));
     }
 
     @Override
     public int getTermWeekNumber(Date date) {
         return getPositiveModulo(Weeks.weeksBetween(getReferential(), new DateTime(date)).getWeeks(), WEEKS_IN_YEAR);
     }
 
     @Override
     public String getWeekName(int termWeekNumber) {
         if (termWeekNumber < 0 || termWeekNumber > WEEKS_IN_YEAR - 1)
             throw new IllegalArgumentException("A week number has to be between 0 and 52");
         return getWeekNames().get(termWeekNumber);
     }
 
     @Override
     public int getTermWeekNumber(String weekName) {
 
         Integer weekNumber = getWeekNames().inverse().get(weekName);
         if (weekNumber == null)
             throw new WeekNameNotFoundException();
         return weekNumber;
     }
 
     @Override
     public Date getWeekStartDate(String weekName) {
         return getWeekStartDate(weekName, DateTime.now().toDate());
     }
 
     @Override
     public Date getWeekEndDate(String weekName) {
         return getWeekEndDate(weekName, DateTime.now().toDate());
     }
 
     @Override
     public Date getWeekStartDate(int termWeekNumber) {
         return getWeekStartDate(termWeekNumber, DateTime.now().toDate());
     }
 
     @Override
     public Date getWeekEndDate(int termWeekNumber) {
         return getWeekEndDate(termWeekNumber, DateTime.now().toDate());
     }
 
     @Override
     public Date getWeekStartDate(String weekName, Date referentialDate) {
         return getWeekStartDate(getTermWeekNumber(weekName), referentialDate);
     }
 
     @Override
     public Date getWeekEndDate(String weekName, Date referentialDate) {
         return getWeekEndDate(getTermWeekNumber(weekName), referentialDate);
     }
 
     @Override
     public Date getWeekStartDate(int termWeekNumber, Date referentialDate) {
         int weeksSinceReferential = getWeeksFromReferentialToStartOfYear(new DateTime(referentialDate)) + termWeekNumber;
         DateTime firstDayOfWeek = getReferential().plusWeeks(weeksSinceReferential).withDayOfWeek(getFirstDayOfWeek());
         return firstDayOfWeek.toDate();
     }
 
     @Override
     public Date getWeekEndDate(int termWeekNumber, Date referentialDate) {
         DateTime lastDayOfWeek = new DateTime(getWeekStartDate(termWeekNumber, referentialDate)).plusDays(6);
         return lastDayOfWeek.toDate();
     }
 
     /**
      * First day of the week (guessed with the referential)
      *
      * @return Constant giving the first day of the week for a term system
      */
     private int getFirstDayOfWeek() {
         return getReferential().getDayOfWeek();
     }
 
     /**
      * Get the number of weeks elapsed between the referential and the first day of an academic year
      *
      * @param dayInCurrentYear Day allowing to identify an academic year
      * @return Number of weeks
      */
     private int getWeeksFromReferentialToStartOfYear(DateTime dayInCurrentYear) {
         return (Weeks.weeksBetween(getReferential(), dayInCurrentYear).getWeeks() / WEEKS_IN_YEAR) * WEEKS_IN_YEAR;
     }
 
     /**
      * Date used to identify the first day of an academic year and to guess the following years
      *
      * @return first day of an academic year
      */
     public abstract DateTime getReferential();
 
     /**
      * {@inheritDoc}
     *
      * @return {@inheritDoc} in a BiMap to fetch both names and number
      */
     public abstract BiMap<Integer, String> getWeekNames();
 
     /**
      * Get the smallest positive congruent number of two numbers
     *
     * @param number  number on which the modulo should be applied
      * @param modulus
      * @return smallest positive integer congruent to number % modulus
      */
     private static int getPositiveModulo(int number, int modulus) {
         int result = number % modulus;
         return (result >= 0) ? result : result + modulus;
     }
 }
