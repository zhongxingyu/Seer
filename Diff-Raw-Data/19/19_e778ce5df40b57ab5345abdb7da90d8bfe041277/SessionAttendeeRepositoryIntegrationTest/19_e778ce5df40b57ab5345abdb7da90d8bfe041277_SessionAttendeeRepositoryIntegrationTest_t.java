 package com.twu.thoughtconf.repositories;
 
 import com.twu.thoughtconf.dao.SessionAttendeeMapper;
 import com.twu.thoughtconf.domain.SessionAttendee;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class SessionAttendeeRepositoryIntegrationTest {
 
    public static final String attendeeEmail = "eseleme";
 
     @Test
     public void shouldSaveSessionAttendee() throws Exception {
        SessionAttendeeMapper sessionAttendeeMapper = mock(SessionAttendeeMapper.class);
        SessionAttendee expectedSessionAttendee = new SessionAttendee(attendeeEmail, 1);
        when(sessionAttendeeMapper.getSessionAttendee(attendeeEmail)).thenReturn(expectedSessionAttendee);
 
        SessionAttendeeRepository sessionAttendeeRepository = new SessionAttendeeRepository(sessionAttendeeMapper);
 
        SessionAttendee foundSessionAttendee = sessionAttendeeRepository.findByEmail(attendeeEmail);

        assertThat(expectedSessionAttendee, sameInstance(foundSessionAttendee));
     }
 
 }
