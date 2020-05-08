 package org.openmrs.module.emr.visit;
 
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Visit;
 
 import java.util.Calendar;
 
 import static java.util.Calendar.DAY_OF_MONTH;
 import static java.util.Calendar.HOUR_OF_DAY;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class VisitDomainWrapperTest {
 
     private VisitDomainWrapper visitDomainWrapper;
     private Visit visit;
 
     @Before
     public void setUp(){
         visit = mock(Visit.class);
         visitDomainWrapper = new VisitDomainWrapper(visit);
     }
 
 
     @Test
     public void shouldReturnDifferenceInDaysBetweenCurrentDateAndStartDate(){
         Calendar startDate = Calendar.getInstance();
         startDate.add(DAY_OF_MONTH, -5);
 
         when(visit.getStartDatetime()).thenReturn(startDate.getTime());
 
         int days = visitDomainWrapper.getDifferenceInDaysBetweenCurrentDateAndStartDate();
 
         assertThat(days, is(5));
 
     }
     @Test
     public void shouldReturnDifferenceInDaysBetweenCurrentDateAndStartDateWhenStartDateTimeIsBiggerThanCurrentDateTime(){
         Calendar startDate = Calendar.getInstance();
         startDate.add(DAY_OF_MONTH, -5);
 
         when(visit.getStartDatetime()).thenReturn(startDate.getTime());
 
         int days = visitDomainWrapper.getDifferenceInDaysBetweenCurrentDateAndStartDate();
 
         assertThat(days, is(5));
 
     }
 }
