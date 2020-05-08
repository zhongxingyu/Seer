 package com.tknilsson.habitboss.model;
 
 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.DateTimeUtils;
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 
 
 import java.lang.RuntimeException;
 
 import static org.junit.Assert.*;
 
 public class HabitTest  {
 
     @Before
     public void setup(){
         DateTimeUtils.setCurrentMillisSystem();
     }
 
     @After
     public void teardown(){
         DateTimeUtils.setCurrentMillisSystem();
     }
 
     @Test
     public void testInstantiation() {
        Habit habit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "Walk the dog");
        Assert.assertNotNull(habit);
       Assert.assertEquals("Walk the dog", habit.getDescription());
     }
 
     @Test
     public void testNewlyCreatedHabitsShouldNotBeOverdue() {
         Habit dailyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
         Assert.assertFalse(dailyHabit.isOverdue());
 
         Habit weeklyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.WEEKLY, "");
         Assert.assertFalse(weeklyHabit.isOverdue());
 
         Habit monthlyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.MONTHLY, "");
         Assert.assertFalse(monthlyHabit.isOverdue());
     }
 
     @Test
     public void testAllHabitsOverdueOncePastLengthofTimeWindow() {
         Habit dailyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
         dailyHabit.setLastTicked(DateTime.now().minusHours(2));
         Assert.assertFalse(dailyHabit.isOverdue());
         dailyHabit.setLastTicked(DateTime.now().minusDays(2));
         Assert.assertTrue(dailyHabit.isOverdue());
         dailyHabit.setLastTicked(DateTime.now().minusMonths(2));
         Assert.assertTrue(dailyHabit.isOverdue());
 
         Habit weeklyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.WEEKLY, "");
         weeklyHabit.setLastTicked(DateTime.now().minusHours(2));
         Assert.assertFalse(weeklyHabit.isOverdue());
         weeklyHabit.setLastTicked(DateTime.now().minusDays(2));
         Assert.assertFalse(weeklyHabit.isOverdue());
         weeklyHabit.setLastTicked(DateTime.now().minusMonths(2));
         Assert.assertTrue(weeklyHabit.isOverdue());
 
         Habit monthlyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.MONTHLY, "");
         monthlyHabit.setLastTicked(DateTime.now().minusHours(2));
         Assert.assertFalse(monthlyHabit.isOverdue());
         monthlyHabit.setLastTicked(DateTime.now().minusDays(2));
         Assert.assertFalse(monthlyHabit.isOverdue());
         monthlyHabit.setLastTicked(DateTime.now().minusMonths(2));
         Assert.assertTrue(monthlyHabit.isOverdue());
     }
 
 
     @Test
     public void testDailyDueSoonOnceWithinAnHourOfDueTime() {
         Habit habit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
 
         habit.setLastTicked(DateTime.now().minusMinutes(30));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusHours(10));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusHours(23).minusMinutes(30));
         Assert.assertTrue(habit.isSoonDue());
     }
 
 
     @Test
     public void testWeeklyDueSoonWhenWithinADayOfDueTime() {
         Habit habit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.WEEKLY, "");
 
         habit.setLastTicked(DateTime.now().minusDays(1));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusDays(5));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusDays(6).minusHours(12));
         Assert.assertTrue(habit.isSoonDue());
     }
 
     @Test
     public void testMonthlyDueSoonWhenWithinAThreeDaysOfDueTime() {
         Habit habit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.MONTHLY, "");
 
         habit.setLastTicked(DateTime.now().minusDays(25));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusDays(10));
         Assert.assertFalse(habit.isSoonDue());
 
         habit.setLastTicked(DateTime.now().minusDays(29));
         Assert.assertTrue(habit.isSoonDue());
     }
 
     @Test
     public void testSetHabitDone() {
         Habit dailyHabit = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
         dailyHabit.setLastTicked(DateTime.now().minusMonths(2));
         Assert.assertTrue(dailyHabit.isOverdue());
         dailyHabit.markAsDone();
         Assert.assertFalse(dailyHabit.isOverdue());
         Assert.assertFalse(dailyHabit.isSoonDue());
     }
 
     @Test
     public void testWhenIsDailyMarkableAgain() {
         DateTime midnightToday = DateTime.now().withTimeAtStartOfDay();
         DateTime twohoursBeforeMidnight = midnightToday.minusHours(2);
         DateTime hourBeforeMidnight = midnightToday.minusHours(1);
         DateTime hourAfterMidnight =  midnightToday.plusHours(1);
 
         Habit markable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
         markable.setLastTicked(twohoursBeforeMidnight);
         DateTimeUtils.setCurrentMillisFixed(hourAfterMidnight.getMillis());
         Assert.assertTrue(markable.canBeMarkedAsDoneAgain());
 
         Habit nonMarkable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.DAILY, "");
         nonMarkable.setLastTicked(twohoursBeforeMidnight);
         DateTimeUtils.setCurrentMillisFixed(hourBeforeMidnight.getMillis());
         Assert.assertFalse(nonMarkable.canBeMarkedAsDoneAgain());
     }
 
     @Test
     public void testWhenIsWeeklyMarkableAgain() {
         DateTime sundayThisWeek = DateTime.now().withDayOfWeek(DateTimeConstants.SUNDAY);
         DateTime twoDaysBeforeSunday = sundayThisWeek.minusDays(2);
         DateTime oneDayBeforeSunday = sundayThisWeek.minusDays(1);
         DateTime oneDayAfterSunday =  sundayThisWeek.plusDays(1);
 
         Habit markable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.WEEKLY, "");
         markable.setLastTicked(twoDaysBeforeSunday);
         DateTimeUtils.setCurrentMillisFixed(oneDayAfterSunday.getMillis());
         Assert.assertTrue(markable.canBeMarkedAsDoneAgain());
 
         Habit nonMarkable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.WEEKLY, "");
         nonMarkable.setLastTicked(twoDaysBeforeSunday);
         DateTimeUtils.setCurrentMillisFixed(oneDayBeforeSunday.getMillis());
         Assert.assertFalse(nonMarkable.canBeMarkedAsDoneAgain());
     }
 
 
 
     @Test
     public void testWhenIsMonthlyMarkableAgain() {
         DateTime endOfLastMonth = new DateTime().minusMonths(1).dayOfMonth().withMaximumValue();
         DateTime twoDaysBeforeMonthEnd = endOfLastMonth.minusDays(2);
         DateTime oneDayBeforeMonthEnd = endOfLastMonth.minusDays(1);
         DateTime oneDayAfterMonthEnd =  endOfLastMonth.plusDays(1);
 
         Habit markable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.MONTHLY, "");
         markable.setLastTicked(twoDaysBeforeMonthEnd);
         DateTimeUtils.setCurrentMillisFixed(oneDayAfterMonthEnd.getMillis());
         Assert.assertTrue(markable.canBeMarkedAsDoneAgain());
 
         Habit nonMarkable = new Habit(Habit.Kind.GOOD, Habit.TimeWindow.MONTHLY, "");
         nonMarkable.setLastTicked(twoDaysBeforeMonthEnd);
         DateTimeUtils.setCurrentMillisFixed(oneDayBeforeMonthEnd.getMillis());
         Assert.assertFalse(nonMarkable.canBeMarkedAsDoneAgain());
     }
 
 }
