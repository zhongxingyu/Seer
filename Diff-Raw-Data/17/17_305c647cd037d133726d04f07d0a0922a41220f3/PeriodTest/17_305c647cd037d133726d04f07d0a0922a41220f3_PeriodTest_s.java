 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
 
 import java.util.List;
 
 /**
  * @author Moses Hohman
  * @author Rhett Sutphin
  */
 public class PeriodTest extends StudyCalendarTestCase {
     private Period period;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         period = createPeriod("Test", 7, 28, 4);
     }
 
     public void testDisplayNameWithName() throws Exception {
         period.setName("Name-o");
         assertEquals("Name-o", period.getDisplayName());
     }
 
     public void testDisplayNameWithNoName() throws Exception {
         period.setName(null);
         assertEquals("[period]", period.getDisplayName());
     }
 
     public void testDurationNeverNull() {
         Period p = new Period();
         p.setDuration(null);
         assertNotNull(p.getDuration());
     }
 
     public void testDefaults() throws Exception {
         period = new Period();
         assertEquals("Default start day is 1", 1, (int) period.getStartDay());
         assertEquals("Default duration is 1 day", 1, (int) period.getDuration().getQuantity());
         assertEquals("Default duration is 1 day", Duration.Unit.day, period.getDuration().getUnit());
         assertEquals("Default reps is 1", 1, period.getRepetitions());
     }
 
     public void testDayRange() {
         period.setStartDay(1);
         period.setRepetitions(2);
         period.setDuration(new Duration(2, Duration.Unit.week));
         assertDayRange(1, 28, period.getTotalDayRange());
     }
 
     public void testDayRangeWithNegativeStart() throws Exception {
         period.setStartDay(-7);
         period.setDuration(new Duration(4, Duration.Unit.day));
         period.setRepetitions(1);
 
         assertDayRange(-7, -4, period.getTotalDayRange());
         period.setRepetitions(2);
         assertDayRange(-7, 0, period.getTotalDayRange());
         period.setRepetitions(3);
         assertDayRange(-7, 4, period.getTotalDayRange());
     }
 
     public void testGetDayRanges() throws Exception {
         period.setStartDay(-4);
         period.setDuration(new Duration(3, Duration.Unit.day));
         period.setRepetitions(4);
 
         List<DayRange> actual = period.getDayRanges();
         assertEquals("Wrong number of ranges", 4, actual.size());
         assertDayRange(-4, -2, actual.get(0));
         assertDayRange(-1, 1, actual.get(1));
         assertDayRange(2, 4, actual.get(2));
         assertDayRange(5, 7, actual.get(3));
     }
 
     public void testGetDayRangesForMonth() throws Exception {
         period.setStartDay(-4);
         period.setDuration(new Duration(3, Duration.Unit.month));
         period.setRepetitions(4);
 
         List<DayRange> actual = period.getDayRanges();
         assertEquals("Wrong number of ranges", 4, actual.size());
         assertDayRange(-4, 52, actual.get(0));
         assertDayRange(80, 136, actual.get(1));
         assertDayRange(164, 220, actual.get(2));
         assertDayRange(248, 304, actual.get(3));
     }
 
     public void testGetDayRangesForWeek() throws Exception {
         period.setStartDay(1);
         period.setDuration(new Duration(3, Duration.Unit.week));
         period.setRepetitions(4);
 
         List<DayRange> actual = period.getDayRanges();
         assertEquals("Wrong number of ranges", 4, actual.size());
         assertDayRange(1, 15, actual.get(0));
         assertDayRange(22, 36, actual.get(1));
         assertDayRange(43, 57, actual.get(2));
         assertDayRange(64, 78, actual.get(3));
     }
 
     public void testGetDayRangesForQuarter() throws Exception {
         period.setStartDay(-4);
         period.setDuration(new Duration(2, Duration.Unit.quarter));
         period.setRepetitions(4);
 
         List<DayRange> actual = period.getDayRanges();
         assertEquals("Wrong number of ranges", 4, actual.size());
         assertDayRange(-4, 87, actual.get(0));
         assertDayRange(178, 269, actual.get(1));
         assertDayRange(360, 451, actual.get(2));
         assertDayRange(542, 633, actual.get(3));
     }
 
     public void testGetDayRangesForFortnight() throws Exception {
         period.setStartDay(2);
         period.setDuration(new Duration(2, Duration.Unit.fortnight));
         period.setRepetitions(4);
 
         List<DayRange> actual = period.getDayRanges();
         assertEquals("Wrong number of ranges", 4, actual.size());
         assertDayRange(2, 16, actual.get(0));
         assertDayRange(30, 44, actual.get(1));
         assertDayRange(58, 72, actual.get(2));
         assertDayRange(86, 100, actual.get(3));
     }
 
     public void testAddPlannedActivityMaintainsBidirectionality() throws Exception {
         PlannedActivity e = createPlannedActivity("Any", 5);
         assertNull(e.getPeriod());
         period.addPlannedActivity(e);
         assertSame(period, e.getPeriod());
     }
 
     public void testAddPlannedActivitiesAdds() throws Exception {
         assertEquals(0, period.getPlannedActivities().size());
         PlannedActivity e = createPlannedActivity("Any", 5);
         period.addPlannedActivity(e);
 
         assertEquals(1, period.getPlannedActivities().size());
         assertSame(e, period.getPlannedActivities().iterator().next());
     }
 
     public void testAddPlannedActivityWithNoDay() throws Exception {
         PlannedActivity pa = new PlannedActivity();
         try {
             period.addChild(pa);
             fail("Exception not thrown");
         } catch (IllegalArgumentException e) {
             assertEquals("Cannot add a planned activity without a day", e.getMessage());
         }
     }
 
     public void testAddPlannedActivityOutOfDayRangeHighFails() throws Exception {
         PlannedActivity pa = createPlannedActivity("hi", 29);
         try {
             period.addChild(pa);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             assertEquals(
                "Cannot add a planned activity for day 29 to this period.  Planned activity days always start with 1.  The maximum for this period is 28.",
                 scve.getMessage());
         }
     }
 
     public void testAddPlannedActivityOutOfDayRangeLowFails() throws Exception {
         period.getDuration().setQuantity(51);
         PlannedActivity pa = createPlannedActivity("lo", -4);
         try {
             period.addChild(pa);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             assertEquals(
                "Cannot add a planned activity for day -4 to this period.  Planned activity days always start with 1.  The maximum for this period is 51.",
                 scve.getMessage());
         }
     }
 
     public void testAddPlannedActivityAtDayZeroFails() throws Exception {
         period.getDuration().setUnit(Duration.Unit.week);
         PlannedActivity pa = createPlannedActivity("Darryl", 0);
         try {
             period.addChild(pa);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             assertEquals(
                "Cannot add a planned activity for day 0 to this period.  Planned activity days always start with 1.  The maximum for this period is 196.",
                 scve.getMessage());
         }
     }
     
     public void testAddPlannedActivityWithNoDurationFails() throws Exception {
         period.getDuration().setQuantity(null);
         PlannedActivity pa = createPlannedActivity("F", 8);
         try {
             period.addChild(pa);
             fail("Exception not thrown");
         } catch (IllegalStateException ise) {
             assertEquals("Cannot add a planned activity unless the period has a duration",
                 ise.getMessage());
         }
     }
 
     public void testSortAscendingByStartDayFirst() throws Exception {
         Period p1 = createPeriod("DC1", 1, 7, 1);
         Period p2 = createPeriod("DC2", 2, 6, 1);
         assertNegative(p1.compareTo(p2));
         assertPositive(p2.compareTo(p1));
     }
 
     public void testSortAscendingByTotalDaysSecond() throws Exception {
         Period p1 = createPeriod("DC1", 2, 7, 1);
         Period p2 = createPeriod("DC2", 2, 3, 2);
         assertNegative(p2.compareTo(p1));
         assertPositive(p1.compareTo(p2));
     }
 
     public void testSortDescendingByRepetitionsThird() throws Exception {
         Period p1 = createPeriod("DC1", 1, 21, 1);
         Period p2 = createPeriod("DC2", 1, 7, 3);
         assertNegative(p2.compareTo(p1));
         assertPositive(p1.compareTo(p2));
     }
 
     public void testSortByNameLast() throws Exception {
         Period p1 = createPeriod("B", 1, 7, 1);
         Period p2 = createPeriod("A", 1, 7, 1);
         assertNegative(p2.compareTo(p1));
         assertPositive(p1.compareTo(p2));
     }
 
     public void testIsFirstDayOfRep() throws Exception {
         Period p1 = createPeriod("A", 1, 7, 8);
         assertTrue(p1.isFirstDayOfRepetition(1));
         assertTrue(p1.isFirstDayOfRepetition(8));
         assertTrue(p1.isFirstDayOfRepetition(15));
         assertTrue(p1.isFirstDayOfRepetition(50));
         assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(57));
         assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(2));
         assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(9));
     }
 
     public void testIsFirstDayOfRepNonOnePeriodStart() throws Exception {
         Period p1 = createPeriod("B", 4, 5, 5);
         assertTrue(p1.isFirstDayOfRepetition(4));
         assertTrue(p1.isFirstDayOfRepetition(9));
         assertTrue(p1.isFirstDayOfRepetition(14));
         assertTrue(p1.isFirstDayOfRepetition(19));
         assertTrue(p1.isFirstDayOfRepetition(24));
         assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(29));
         assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(5));
         assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(10));
     }
 
     public void testIsLastDayOfRep() throws Exception {
         Period p1 = createPeriod("A", 1, 7, 8);
         assertTrue(p1.isLastDayOfRepetition(7));
         assertTrue(p1.isLastDayOfRepetition(14));
         assertTrue(p1.isLastDayOfRepetition(21));
         assertTrue(p1.isLastDayOfRepetition(56));
         assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(63));
         assertFalse("Second day considered last day", p1.isLastDayOfRepetition(2));
         assertFalse("Second day considered last day", p1.isLastDayOfRepetition(9));
     }
 
     public void testIsLastDayOfRepNonOnePeriodStart() throws Exception {
         Period p1 = createPeriod("B", 4, 5, 5);
         assertTrue(p1.isLastDayOfRepetition(8));
         assertTrue(p1.isLastDayOfRepetition(13));
         assertTrue(p1.isLastDayOfRepetition(18));
         assertTrue(p1.isLastDayOfRepetition(23));
         assertTrue(p1.isLastDayOfRepetition(28));
         assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(33));
         assertFalse("Second day considered last day", p1.isLastDayOfRepetition(5));
         assertFalse("Second day considered last day", p1.isLastDayOfRepetition(10));
     }
 
     public void testIsFirstDayOfRepWithNegDays() throws Exception {
         Period p1 = createPeriod("A", -6, 5, 2);
         assertTrue(p1.isFirstDayOfRepetition(-6));
         assertTrue(p1.isFirstDayOfRepetition(-1));
         assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(5));
         assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(-5));
     }
 
     public void testIsLastDayOfRepWithNegDays() throws Exception {
         Period p1 = createPeriod("A", -6, 5, 2);
         assertTrue(p1.isLastDayOfRepetition(-2));
         assertTrue(p1.isLastDayOfRepetition(3));
         assertFalse("Out of range considered last day", p1.isFirstDayOfRepetition(5));
         assertFalse("Second day considered last day", p1.isFirstDayOfRepetition(-5));
     }
 
     public void testCloneClonesDuration() throws Exception {
         Period p1 = new Period();
         p1.getDuration().setQuantity(17);
         Period p1c = p1.clone();
         p1c.getDuration().setQuantity(42);
         assertEquals("Update wrote through from clone to original", 17, (int) p1.getDuration().getQuantity());
         assertEquals("Update of clone didn't take", 42, (int) p1c.getDuration().getQuantity());
         assertNotSame(p1.getDuration(), p1c.getDuration());
     }
 
     public void testCompareTo() {
         period = createPeriod("name", 3, Duration.Unit.day, 15, 3);
         Period anotherPeriod = createPeriod("name", 3, Duration.Unit.day, 15, 3);
         assertTrue("both periods are not equal", anotherPeriod.compareTo(period) == 0);
         period.setId(1);
         assertTrue("both periods are equal", anotherPeriod.compareTo(period) > 0);
     }
 
     public void testFindMatchingPlannedActivityWhenPresent() throws Exception {
         PlannedActivity pa1 = setGridId("Inf", createPlannedActivity("Aleph", 1));
         PlannedActivity pa2 = setGridId("Zer0", createPlannedActivity("Null", 8));
         period.addPlannedActivity(pa1);
         period.addPlannedActivity(pa2);
         assertSame("Not found", pa1, period.findNaturallyMatchingChild("Inf"));
     }
 
     public void testFindMatchingPlannedActivityWhenNotPresent() throws Exception {
         PlannedActivity pa1 = setGridId("Inf", createPlannedActivity("Aleph", 1));
         PlannedActivity pa2 = setGridId("Zer0", createPlannedActivity("Null", 8));
         period.addPlannedActivity(pa1);
         period.addPlannedActivity(pa2);
         assertNull(period.findNaturallyMatchingChild("Zip"));
     }
 }
