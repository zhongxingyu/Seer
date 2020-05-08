 package com.twu.thoughtconf.repositories;
 
 import com.twu.thoughtconf.dao.SessionAttendeeMapper;
 import com.twu.thoughtconf.domain.SessionAttendee;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class SessionAttendeeRepositoryIntegrationTest {
 
    @Autowired
    private SessionAttendeeRepository sessionAttendeeRepository;
 
     @Test
     public void shouldSaveSessionAttendee() throws Exception {
 
        SessionAttendee sessionAttendee = new SessionAttendee("eseleme", 1);
        SessionAttendee foundSessionAttendee = sessionAttendeeRepository.findByEmail(sessionAttendee.getAttendeeEmail());
 
        assertThat(foundSessionAttendee.getAttendeeEmail(), is(sessionAttendee.getAttendeeEmail()));
        assertThat(sessionAttendee.getAttendeeEmail(), is("eseleme"));
     }
 
 }
