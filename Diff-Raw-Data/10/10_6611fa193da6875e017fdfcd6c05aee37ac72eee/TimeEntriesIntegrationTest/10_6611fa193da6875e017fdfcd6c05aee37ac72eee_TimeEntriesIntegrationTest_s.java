 package org.synyx.jmite;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.joda.time.DateMidnight;
import org.junit.Ignore;
 import org.junit.Test;
 import org.synyx.jmite.domain.Customer;
 import org.synyx.jmite.domain.Project;
 import org.synyx.jmite.domain.Service;
 import org.synyx.jmite.domain.TimeEntries;
 import org.synyx.jmite.domain.TimeEntry;
 import org.synyx.jmite.domain.TimeEntryQuery;
 
 
 /**
  * Integration test for {@link TimeEntries} resource.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class TimeEntriesIntegrationTest extends
         AbstractReadingMiteIntegrationTests<TimeEntry, TimeEntries> {
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.jmite.AbstractMiteIntegrationTests#getCollectionResource()
      */
     @Override
     protected TimeEntries getCollectionResource() {
 
         return mite.timeEntries();
     }
 
 
     @Test
     public void queriesEntriesForEntryCollaborators() throws Exception {
 
         TimeEntry entry = get();
 
         assertTrue(query().withProject(entry).execute().contains(entry));
         assertTrue(query().withCustomer(entry).execute().contains(entry));
         assertTrue(query().withService(entry).execute().contains(entry));
 
     }
 
 
     @Test
     public void queriesEntriesForProject() throws Exception {
 
         TimeEntry entry = get();
 
         Project project = entry.getProject(mite);
         assertTrue(query().with(project).execute().contains(entry));
     }
 
 
     @Test
     public void queriesEntriesForService() throws Exception {
 
         TimeEntry entry = get();
 
         Service service = entry.getService(mite);
         assertTrue(query().with(service).execute().contains(entry));
     }
 
 
     @Test
    @Ignore
     public void queriesEntriesForCustomer() throws Exception {
 
         TimeEntry entry = get();
 
         Customer customer = entry.getCustomer(mite);
         assertTrue(query().with(customer).execute().contains(entry));
     }
 
 
     @Test
     public void testThisMonth() {
 
         DateMidnight day = new DateMidnight().withMonthOfYear(4);
 
         DateMidnight start = day.withDayOfMonth(1);
         DateMidnight end = start.plusMonths(1);
 
         List<TimeEntry> result = query().within(start, end).execute();
         assertFalse(result.isEmpty());
 
         DateMidnight temp = start;
         List<TimeEntry> reference = new ArrayList<TimeEntry>();
 
         // Sum up all single day's entries
         while (temp.isBefore(end)) {
 
             reference.addAll(query().at(temp).execute());
             temp = temp.plusDays(1);
         }
 
         Collections.sort(result);
         Collections.sort(reference);
 
         assertEquals(result, reference);
     }
 
 
     private TimeEntryQuery query() {
 
         return getCollectionResource().query();
     }
 }
