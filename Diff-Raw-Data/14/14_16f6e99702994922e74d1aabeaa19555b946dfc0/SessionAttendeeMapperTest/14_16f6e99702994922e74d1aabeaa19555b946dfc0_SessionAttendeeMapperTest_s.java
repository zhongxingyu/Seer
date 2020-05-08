 package com.twu.thoughtconf.dao;
 
import com.twu.thoughtconf.dao.SessionAttendeeMapper;
 import com.twu.thoughtconf.domain.SessionAttendee;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(value = {"/mapper-context.xml"})
 public class SessionAttendeeMapperTest {
 
     @Autowired
     private SessionAttendeeMapper sessionAttendeeMapper;
 
     @Before
     public void cleanSessionAttendeeTable() {
         sessionAttendeeMapper.cleanTable();
     }
 
     @Test
     public void saveTest_should_insert_new_record_into_session_attendee_table(){
 
         sessionAttendeeMapper.save("eselemela@thoughtworks.com", "1");
         SessionAttendee sessionAttendee = sessionAttendeeMapper.getSessionAttendee("eselemela@thoughtworks.com");
 
         assertThat(sessionAttendee.getAttendeeEmail(), is("eselemela@thoughtworks.com"));
     }
 }
