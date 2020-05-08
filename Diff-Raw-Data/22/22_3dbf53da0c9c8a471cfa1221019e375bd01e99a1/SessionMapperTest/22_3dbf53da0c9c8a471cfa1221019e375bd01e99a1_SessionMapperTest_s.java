 package com.twu.thoughtconf.dao;
 
 import com.twu.thoughtconf.domain.ConferenceSession;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.sql.Timestamp;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(value = {"/mapper-context.xml"})
 public class SessionMapperTest {
 
     @Autowired
     SessionMapper sessionMapper;
 
     @Test
     public void shouldGetSessionDetailByGivenSessionId() {
         ConferenceSession conferenceSession = sessionMapper.findSessionByID("1");
 
         // TODO: DJ and Akriti, implement equals on ConferenceSession
 
         assertThat(conferenceSession.getSessionId(), is(1));
         assertThat(conferenceSession.getName(), is("dev bootcamp"));
         assertThat(conferenceSession.getLocation(), is("Ajanta"));
         assertThat(conferenceSession.getType(), is("Hands on"));
         assertThat(conferenceSession.getAbstract(), is("Attendees should bring laptops so that they can code, code and code"));
         assertThat(conferenceSession.getSpeaker(), is("Meng Wang"));
         assertThat(conferenceSession.getSpeakerIntro(), is("Guru in java and many other languages"));
         assertThat(conferenceSession.getSessionId(), is(1));
         assertThat(conferenceSession.getConferenceName(), is("conf1"));
 
 
         DateTime expectedSessionStartTime = new DateTime(2012, 12, 11, 9, 30, 0);
         assertThat(conferenceSession.getStartTime(), is(expectedSessionStartTime));
 
     }
 
 
     @Test
     public void shouldSaveSession() {
         DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
         DateTime startTime = formatter.parseDateTime("2012-10-20 08:30:00");
         DateTime endTime = formatter.parseDateTime("2012-10-20 09:30:00");
 
         ConferenceSession conferenceSession = new ConferenceSession("anything", "somewhere", startTime, endTime, "session abstract", "presenter", "about presenter");
         sessionMapper.save(conferenceSession);
 
         ConferenceSession expectedConferenceSession = sessionMapper.getSessionByName("anything");
         System.out.println(expectedConferenceSession.getEndTime());
         assertThat(conferenceSession.getName(), is(expectedConferenceSession.getName()));
         assertThat(conferenceSession.getEndTime(), is(expectedConferenceSession.getEndTime()));
         sessionMapper.delete(conferenceSession);
     }
     @Test
     public void shouldSaveSessionWithCorrectTime() {
         DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
         DateTime startTime = formatter.parseDateTime("2012-10-20 08:30:00");
         DateTime endTime = formatter.parseDateTime("2012-10-20 09:30:00");
 

         ConferenceSession conferenceSession = new ConferenceSession("anything", "somewhere", startTime, endTime, "session abstract", "presenter", "about presenter");
         sessionMapper.save(conferenceSession);
 
         ConferenceSession expectedConferenceSession = sessionMapper.getSessionByName("anything");
         System.out.println(expectedConferenceSession.getEndTime());
         assertThat(conferenceSession.getName(), is(expectedConferenceSession.getName()));
         assertThat(conferenceSession.getEndTime(), is(expectedConferenceSession.getEndTime()));
         sessionMapper.delete(conferenceSession);
     }
 
 
 }
