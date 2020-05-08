 package edu.northwestern.bioinformatics.studycalendar.dao.reporting;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
 import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
 import edu.nwu.bioinformatics.commons.DateUtils;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author John Dzak
  */
 public class ScheduledActivitiesReportRowDaoTest extends
         ReportDaoTestCase<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow, ScheduledActivitiesReportRowDao> {
     private static final long NEG_16 = -16;
     private static final long NEG_17 = -17;
     private static final long NEG_18 = -18;
     private static final long NEG_19 = -19;
 
     protected ScheduledActivitiesReportFilters createFilters() {
         return new ScheduledActivitiesReportFilters();
     }
 
     public void testSearchWithStudyFilter_Pos() {
         filters.setStudyAssignedIdentifier("Foo");
         ScheduledActivitiesReportRow row = assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16).get(2);
 
         assertNotNull("ID should not be null", row.getId());
 
         ScheduledActivity schd = row.getScheduledActivity();
         assertNotNull(schd.getDetails());
         assertNotNull("Scheduled Activity id should not be null", schd.getId());
         assertNotNull("Scheduled Activity actual date should not be null", schd.getCurrentState());
         assertNotNull("Scheduled Activity ideal date should not be null", schd.getIdealDate());
 
         Activity acivity = schd.getActivity();
         assertNotNull("Activity id should not be null", acivity.getId());
         assertNotNull("Activity name should not be null", acivity.getName());
         assertNotNull("Actiivty type should not be null", acivity.getType());
 
         assertNotNull("Subject should not be null", row.getSubject());
 
         assertNotNull("Study should not be null", row.getStudy());
 
         assertNotNull("Site should not be null", row.getSite());
         assertNotNull("Site coordinator name should not be null", row.getSubjectCoordinatorName());
     }
 
     public void testSearchWithStudyFilter_Neg() {
         filters.setStudyAssignedIdentifier("Fla");
         assertSearchWithResults();
     }
 
     public void testSearchWithScheduledActivityMode_Scheduled() {
         filters.setCurrentStateMode(ScheduledActivityMode.SCHEDULED);
         assertSearchWithResults(NEG_16);
     }
 
     public void testSearchWithScheduledActivityMode_Canceled() {
         filters.setCurrentStateMode(ScheduledActivityMode.CANCELED);
         assertSearchWithResults(NEG_17);
     }
 
     public void testSearchWithScheduledActivityMode_Occurred() {
         filters.setCurrentStateMode(ScheduledActivityMode.OCCURRED);
         assertSearchWithResults();
     }
 
     public void testSearchWithSiteFilter_Pos() {
         filters.setSiteName("DC");
         assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
     }
 
     public void testSearchWithLabelFilter() {
         filters.setLabel("LABELB");
         assertSearchWithResults(NEG_17, NEG_16);
     }
 
     public void testSearchWithSiteFilter_Neg() {
         filters.setSiteName("Bedrock");
         assertSearchWithResults();
     }
 
     public void testSearchWithStartDateFilter_Pos() {
         MutableRange<Date> range = new MutableRange<Date>();
         range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
         filters.setActualActivityDate(range);
         assertSearchWithResults(NEG_19, NEG_18, NEG_17);
     }
 
     public void testSearchWithStartDateFilter_Neg() {
         MutableRange<Date> range = new MutableRange<Date>();
         range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 15));
         filters.setActualActivityDate(range);
         assertSearchWithResults(NEG_19, NEG_18);
     }
 
     public void testSearchWithStopDateFilter_Pos() {
         MutableRange<Date> range = new MutableRange<Date>();
         range.setStop(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
         filters.setActualActivityDate(range);
         assertSearchWithResults(NEG_16);
     }
 
     public void testSearchWithStopDateFilter_Neg() {
         MutableRange<Date> range = new MutableRange<Date>();
         range.setStop(DateUtils.createDate(2006, Calendar.OCTOBER, 25));
         filters.setActualActivityDate(range);
         assertSearchWithResults();
     }
 
     public void testSearchWithActivityTypeFilter_Pos() {
         ActivityType activityType = createNamedInstance("INTERVENTION", ActivityType.class);
         activityType.setId(2);
         filters.setActivityType(activityType);
         assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
     }
 
     public void testSearchWithActivityTypeFilter_Neg() {
         ActivityType activityType = createNamedInstance("DISEASE_MEASURE", ActivityType.class);
         activityType.setId(3);
         filters.setActivityType(activityType);
         assertSearchWithResults();
     }
 
     public void testSearchWithSubjectCoordinatorFilter_Pos() {
         filters.setSubjectCoordinator(edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId(-200, new User()));
         assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
     }
 
     public void testSearchWithSubjectCoordinatorFilter_Neg() {
         filters.setSubjectCoordinator(Fixtures.setId(-100, new User()));
         assertSearchWithResults();
     }
 
     public void testSearchWithNoFiltersIsEmpty() {
         assertSearchWithResults();
     }
 
     public void testRowWithPersonIdFilter() {
         filters.setPersonId("UNIVERSAL");
         List<ScheduledActivitiesReportRow> rows = assertSearchWithResults(NEG_18, NEG_17, NEG_16);
         assertEquals("Returns different number of rows ", 3, rows.size());
     }
 
     public void testRowWithLabelFilter() {
         filters.setLabel("LABELB");
         List<ScheduledActivitiesReportRow> rows = doSearch();
         for (ScheduledActivitiesReportRow row : rows ) {
             assertTrue("labelB is not in the row", row.getScheduledActivity().getLabels().contains("labelB"));
         }
     }
 
 }
