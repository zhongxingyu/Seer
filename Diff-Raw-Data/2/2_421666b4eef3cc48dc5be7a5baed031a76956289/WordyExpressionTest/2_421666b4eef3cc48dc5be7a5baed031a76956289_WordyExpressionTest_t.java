 /**
  *
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.springframework.scheduling.wordy;
 
 import org.junit.Test;
 import org.quartz.CronExpression;
 import org.springframework.scheduling.support.CronSequenceGenerator;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import static junit.framework.Assert.*;
 
 public class WordyExpressionTest {
 
     @Test
     public void shouldHandleACombinationOfExpressions_on_at() {
         String[] expressions = {
                 "on MON-FRI at 10 pm",
                 "on MON thru FRI at 10 pm",
                 "at 10 pm on MON thru FRI",
                 "at 10 pm on MON-FRI",
         };
 
         for (String expression : expressions) {
             assertEquals("0 0 22 ? * MON-FRI", wordyToCron(expression));
         }
     }
 
     @Test
     public void shouldHandleACombinationOfExpressions_on_every_between() {
         String[] expressions = {
                 "on MON-FRI between 12 am and 10 pm every 10 minutes",
                 "on MON thru FRI between 12 am and 10 pm every 10 minutes",
                 "on MON-FRI every 10 minutes between 12 am and 10 pm ",
                 "on MON thru FRI every 10 minutes between 12 am and 10 pm ",
                 "between 12 am and 10 pm on MON-FRI every 10 minutes",
                 "between 12 am and 10 pm on MON thru FRI every 10 minutes",
                 "between 12 am and 10 pm every 10 minutes on MON-FRI ",
                 "between 12 am and 10 pm every 10 minutes on MON thru FRI ",
                 "every 10 minutes between 12 am and 10 pm on MON-FRI",
                 "every 10 minutes between 12 am and 10 pm on MON thru FRI",
                 "every 10 minutes on MON thru FRI between 12 am and 10 pm",
                 "every 10 minutes on MON - FRI between 12 am and 10 pm"
         };
 
         for (String expression : expressions) {
             assertEquals("0 0/10 0-22 ? * MON-FRI", wordyToCron(expression));
         }
     }
 
     @Test
     public void shouldHandleACombinationOfExpressions_on_every_between_withListOfDays() {
         String[] expressions = {
                 "on MON, TUE, FRI between 12 am and 10 pm every 10 minutes",
                 "on MON, TUE, FRI every 10 minutes between 12 am and 10 pm ",
                 "between 12 am and 10 pm on MON, TUE, FRI every 10 minutes",
                 "between 12 am and 10 pm every 10 minutes on MON, TUE, FRI",
                 "every 10 minutes between 12 am and 10 pm on MON, TUE, FRI",
                 "every 10 minutes on MON, TUE, FRI between 12 am and 10 pm",
         };
 
         for (String expression : expressions) {
             assertEquals("0 0/10 0-22 ? * MON,TUE,FRI", wordyToCron(expression));
         }
     }
 
     @Test
     public void onExpression_shouldHandleWhiteSpaceInExpression() {
         assertEquals("0 0 22 ? * MON", wordyToCron("on   MON   at 10 pm"));
     }
 
     @Test
     public void onExpression_shouldHandleWhiteSpaceOnTheEnds() {
         assertEquals("0 0 22 ? * MON", wordyToCron("  on MON at 10 pm  "));
     }
 
     @Test
     public void onExpression_shouldNotCareAboutCase() {
         assertEquals("0 0 22 ? * MON", wordyToCron("ON mon AT 10 pm"));
     }
 
     @Test
     public void onExpression_shouldAllowASingleDayAsAValue() {
         assertEquals("0 0 22 ? * MON", wordyToCron("on MON at 10 pm"));
     }
 
     @Test
     public void onExpression_shouldAllow_at_ToPrecedeTheOnExpression() {
         assertEquals("0 0 22 ? * MON-FRI", wordyToCron("at 10 pm on MON-FRI"));
     }
 
     @Test
     public void onExpression_shouldAllow_every_ToPrecedeTheOnExpression() {
         assertEquals("0 0/10 * ? * MON-FRI", wordyToCron("every 10 minutes on MON-FRI"));
     }
 
     @Test
     public void onExpression_shouldAllowTheWord_thru_asADividerOfDays() {
         assertEquals("0 0/10 * ? * MON-FRI", wordyToCron("on MON thru FRI every 10 minutes"));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void onExpression_shouldBlowUpIfNo_at_or_every_definitionIsProvided() {
         wordyToCron("on MON-FRI");
     }
 
     @Test
     public void onExpression_shouldAllowWhiteSpaceBetweenDayListElements() {
         assertEquals("0 0/10 * ? * MON,WED,FRI", wordyToCron("on MON, WED, FRI every 10 minutes"));
     }
 
     @Test
     public void onExpression_shouldAllowTheQuartzDefaultsForDaysOfTheWeekListInAliasFormat() {
         assertEquals("0 0/10 * ? * MON,WED,FRI", wordyToCron("on MON,WED,FRI every 10 minutes"));
     }
 
     @Test
     public void onExpression_shouldAllowTheQuartzDefaultsForDaysOfTheWeekRangeInAliasFormat() {
         assertEquals("0 0/10 * ? * MON-FRI", wordyToCron("on MON-FRI every 10 minutes"));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void betweenExpression_shouldBlowUpIfTheFirstHourHasTheSideOfDayButTheSecondHourDoesNot() {
         wordyToCron("between 1 am and 10 every 1 minute");
     }
 
     @Test
     public void betweenExpression_shouldNotCareWhatSideTheEveryExpressionIsOn() {
         assertEquals("0 0/1 12-22 * * ?", wordyToCron("every 1 minute between 12 pm and 10 pm"));
     }
 
     @Test
     public void betweenExpression_shouldSupportNightHourRanges() {
         assertEquals("0 0/1 12-22 * * ?", wordyToCron("between 12 pm and 10 pm every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldSupportMorningHourRanges() {
         assertEquals("0 0/1 1-10 * * ?", wordyToCron("between 1 am and 10 am every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldNotForceTheWhitespaceBetweenTheHourAndTheSideOfDay() {
         assertEquals("0 0/1 1-22 * * ?", wordyToCron("between 1am and 10pm every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldSupportTheTimeOfDayAsPartOfTheRangeForEachHour() {
         assertEquals("0 0/1 1-22 * * ?", wordyToCron("between 1 am and 10 pm every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldSupportTheTimeOfDayAsPartOfTheRange() {
         assertEquals("0 0/1 13-22 * * ?", wordyToCron("between 1-10 pm every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldHandleHourIntervals() {
         assertEquals("0 0 0-10/2 * * ?", wordyToCron("between 0-10 every 2 hours"));
     }
 
     @Test
     public void betweenExpression_shouldBeCaseInsensitive() {
         assertEquals("0 0/15 0-10 * * ?", wordyToCron("between 0 and 10 every 15 minutes".toUpperCase()));
     }
 
     @Test
     public void betweenExpression_shouldHandleA_and_insteadOfADash() {
         assertEquals("0 0/15 0-10 * * ?", wordyToCron("between 0 and 10 every 15 minutes"));
     }
 
     @Test
     public void betweenExpression_shouldHandleSecondsIntervals() {
         assertEquals("0/10 * 0-10 * * ?", wordyToCron("between 0-10 every 10 seconds"));
     }
 
     @Test
     public void betweenExpression_shouldHandleNotPluralFormOfMinutes() {
         assertEquals("0 0/1 0-10 * * ?", wordyToCron("between 0-10 every 1 minute"));
     }
 
     @Test
     public void betweenExpression_shouldHandleMilitaryHourRanges() {
         assertEquals("0 0/15 0-10 * * ?", wordyToCron("between 0-10 every 15 minutes"));
     }
 
     @Test
     public void betweenExpression_shouldSupportBothDoubleDigitHours() {
         assertEquals("0 0/15 10-12 * * ?", wordyToCron("between 10-12 every 15 minutes"));
     }
 
     @Test
     public void betweenExpression_shouldSupportBothSingleDigitHours() {
         assertEquals("0 0/15 0-2 * * ?", wordyToCron("between 0-2 every 15 minutes"));
     }
 
     @Test
     public void betweenExpression_shouldSupportSingleDigitMinutes() {
         assertEquals("0 0/1 0-2 * * ?", wordyToCron("between 0-2 every 1 minutes"));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void betweenExpression_shouldBlowUpIfNoEveryConditionIsGiven() {
         wordyToCron("between 0-10");
     }
 
     @Test
     public void everyExpression_shouldHandleHour() {
         assertEquals("0 0 0/1 * * ?", wordyToCron("every 1 hour"));
     }
 
     @Test
     public void everyExpression_shouldHandleHour_plural() {
         assertEquals("0 0 0/3 * * ?", wordyToCron("every 3 hours"));
     }
 
     @Test
     public void everyExpression_shouldHandleMinute() {
         assertEquals("0 0/1 * * * ?", wordyToCron("every 1 minute"));
     }
 
     @Test
     public void everyExpression_shouldHandleMinute_plural() {
         assertEquals("0 0/2 * * * ?", wordyToCron("every 2 minutes"));
     }
 
     @Test
     public void everyExpression_shouldHandleSecond() {
         assertEquals("0/1 * * * * ?", wordyToCron("every 1 second"));
     }
 
     @Test
     public void everyExpression_shouldHandleSecond_plural() {
         assertEquals("0/1 * * * * ?", wordyToCron("every 1 seconds"));
     }
 
     @Test
     public void everyExpression_shouldMultipleDigitUnitSize() {
         assertEquals("0/100 * * * * ?", wordyToCron("every 100 second"));
     }
 
     @Test
     public void everyExpression_shouldIgnoreCase() {
         assertEquals("0/1 * * * * ?", wordyToCron("EVERY 1 Second"));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void everyExpression_shouldBlowUpIfABadExpressionIsGiven() {
         wordyToCron("every xx hour");
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void atExpression_shouldBlowUpIfAnEveryExpressionIsGiven() {
         wordyToCron("at 1 am every 1 minute");
     }
 
     @Test
     public void atExpression_shouldHandleGivenTheHourAndTheSideOfTheDay() {
         assertEquals("0 0 1 * * ?", wordyToCron("at 1 am"));
     }
 
     @Test
     public void atExpression_shouldHandleGivenTheHourAndTheSideOfTheDay_pm() {
         assertEquals("0 0 13 * * ?", wordyToCron("at 1 pm"));
 
     }
 
     @Test
     public void atExpression_shouldHandleMultipleDigitHours() {
         assertEquals("0 0 23 * * ?", wordyToCron("at 11 pm"));
     }
 
     @Test
     public void atExpression_shouldHandleHourAndMinutesProvidedWithPartOfDay() {
         assertEquals("0 22 23 * * ?", wordyToCron("at 11:22 pm"));
     }
 
     @Test
     public void atExpression_shouldHandleMidnightProperly() {
         assertEquals("0 0 0 * * ?", wordyToCron("at 12:00 am"));
     }
 
     @Test
     public void atExpression_shouldHandleNoonProperly() {
         assertEquals("0 0 12 * * ?", wordyToCron("at 12:00 pm"));
     }
 
     @Test
     public void atExpression_shouldNotRequireTheAmOrPm() {
         assertEquals("0 22 23 * * ?", wordyToCron("at 23:22"));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void atExpression_shouldBlowUpWhenAmAndPmIsGivenForMilitaryTime() {
         wordyToCron("at 23:22 pm");
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void atExpression_shouldBlowUpWhenGivenA3DigitMinuteValue() {
         wordyToCron("at 11:221 pm");
     }
 
     @Test
     public void shouldContainTheReadMeFileContentsWhenAnExceptionHappens() {
         try {
             wordyToCron("at 11:221 pm");
             fail();
         } catch (BadWordyExpressionException e) {
            assertTrue(e.getMessage().contains("Wordy to Cron comparison"));
         }
     }
 
     @Test
     public void atExpression_shouldIgnoreCase() {
         assertEquals("0 0 13 * * ?", wordyToCron("AT 1 pM"));
     }
 
     @Test
     public void atExpression_shouldNotCareAboutWhiteSpace_NoMinutes() {
         assertEquals("0 0 13 * * ?", wordyToCron("  at   1   pm  "));
     }
 
     @Test
     public void atExpression_shouldNotCareAboutWhiteSpace_WithMinutes() {
         assertEquals("0 10 13 * * ?", wordyToCron("  at   1:10   pm  "));
     }
 
     @Test(expected = BadWordyExpressionException.class)
     public void atExpression_shouldBlowUpIfA3DigitHourIsGiven() {
         wordyToCron("at 111 pm");
     }
 
     private String wordyToCron(String expression) {
         String cronExpression = new WordyExpression(expression).toCron();
         try {
             Date now = new Date();
             CronSequenceGenerator springCronGenerator = new CronSequenceGenerator(cronExpression, TimeZone.getDefault());
             CronExpression quartzCronExpression = new CronExpression(cronExpression);
             assertDatesAreSimilar(now, springCronGenerator, quartzCronExpression);
             return quartzCronExpression.toString();
         } catch (ParseException e) {
             throw new RuntimeException("Given cron was: [" + cronExpression + "]", e);
         }
     }
 
     private void assertDatesAreSimilar(Date now, CronSequenceGenerator springCronGenerator, CronExpression quartzCronExpression) {
         Calendar springCalendar = Calendar.getInstance();
         springCalendar.setTime(springCronGenerator.next(now));
         Calendar quartzCalendar = Calendar.getInstance();
         quartzCalendar.setTime(quartzCronExpression.getNextValidTimeAfter(now));
 
         int[] itemsToCheck = {
                 Calendar.YEAR,
                 Calendar.MONTH,
                 Calendar.DAY_OF_MONTH,
                 Calendar.HOUR_OF_DAY,
                 Calendar.MINUTE,
 //                Calendar.SECOND -- this is not checked because Spring's implementation of cron rounds the seconds
         };
 
         for (int i = 0; i < itemsToCheck.length; i++) {
             int fieldValue = itemsToCheck[i];
             assertEquals(quartzCalendar.get(fieldValue), springCalendar.get(fieldValue));
         }
     }
 
 }
