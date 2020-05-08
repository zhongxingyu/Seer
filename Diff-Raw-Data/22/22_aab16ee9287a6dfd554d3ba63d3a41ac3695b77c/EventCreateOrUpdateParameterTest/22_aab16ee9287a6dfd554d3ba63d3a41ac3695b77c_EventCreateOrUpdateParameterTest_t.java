 package org.sukrupa.event;
 
 import org.junit.Test;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.sukrupa.platform.hamcrest.CollectionMatchers.hasOnly;
 
 public class EventCreateOrUpdateParameterTest {
 
     @Test
     public void shouldReturnStudentIdsOfAttendees() {
         EventCreateOrUpdateParameter eventCreateOrUpdateParameter = new EventCreateOrUpdateParameter();
         eventCreateOrUpdateParameter.setAttendees("1,2,3");
         assertThat(eventCreateOrUpdateParameter.getStudentIdsOfAttendees(), hasOnly("1","2","3"));
     }

    @Test
    public void shouldReturnStudentIdOfAttendeesWithoutEnter() {
        EventCreateOrUpdateParameter eventCreateOrUpdateParameter = new EventCreateOrUpdateParameter();
        eventCreateOrUpdateParameter.setAttendees("34545, 83415, 64262\r\n");
        assertThat(eventCreateOrUpdateParameter.getStudentIdsOfAttendees(), hasOnly("34545", "83415", "64262"));
    }
 }
