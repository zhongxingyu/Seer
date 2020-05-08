 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 public class ParticipantDaoTest extends ContextDaoTestCase<ParticipantDao> {
     private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
     private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
 
     public void testGetAll() throws Exception {
         List<Participant> actual = getDao().getAll();
         assertEquals("Wrong size", 2, actual.size());
         assertEquals("Wrong first participant", "Ng", actual.get(0).getLastName());
         assertEquals("Wrong second participant", "Scott", actual.get(1).getLastName());
     }
 
     public void testGetById() throws Exception {
         Participant participant = getDao().getById(-100);
         assertNotNull("Participant not found", participant);
         assertEquals("Wrong last name", "Scott", participant.getLastName());
     }
 
     public void testSaveAssignment() throws Exception {
         {
             Site site = siteDao.getById(-1001);
             StudySite studySite = site.getStudySites().get(0);
             assertEquals("Wrong study site found in test setup", -3001, (int) studySite.getId());
             Participant participant = getDao().getById(-100);
             assertEquals("Participant should already have one assignment", 1, participant.getAssignments().size());
 
             StudyParticipantAssignment spa = new StudyParticipantAssignment();
             spa.setParticipant(participant);
             spa.setStudySite(studySite);
             spa.setStartDateEpoch(new Date());
 
             participant.addAssignment(spa);
 
             getDao().save(participant);
         }
 
         interruptSession();
 
         Participant loaded = getDao().getById(-100);
         assertNotNull("Participant reloading failed", loaded);
         assertEquals("Assignment not saved", 2, loaded.getAssignments().size());
         StudyParticipantAssignment newAssignment = loaded.getAssignments().get(1);
         assertEquals("Wrong participant", -100, (int) newAssignment.getParticipant().getId());
         assertEquals("Wrong study site", -3001, (int) newAssignment.getStudySite().getId());
         assertSameDay("Wrong start date", new Date(), newAssignment.getStartDateEpoch());
     }
 
     public void testSaveNewParticipant() throws Exception {
         Integer savedId;
         {
             Participant participant = new Participant();
             participant.setFirstName("Jeff");
             participant.setLastName("Someone");
             participant.setGender("Male");
             participant.setDateOfBirth(new Date());
             participant.setPersonId("123-45-6789");
 
             getDao().save(participant);
             savedId = participant.getId();
             assertNotNull("The saved participant id", savedId);
         }
 
         interruptSession();
 
         {
             Participant loaded = getDao().getById(savedId);
             assertNotNull("Could not reload participant id " + savedId, loaded);
             assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
             assertEquals("Wrong lastname", "Someone", loaded.getLastName());
             assertEquals("Wrong gender", "Male", loaded.getGender());
         }
     }
 
     public void testLoadScheduledCalendar() throws Exception {
         Participant loaded = getDao().getById(-100);
         StudyParticipantAssignment assignment = loaded.getAssignments().get(0);
         ScheduledCalendar actualCalendar = assignment.getScheduledCalendar();
         assertNotNull("Scheduled calendar not found", actualCalendar);
         assertEquals("Wrong scheduled calendar found", -11, (int) actualCalendar.getId());
         assertSame("Relationship not bidirectional", assignment, actualCalendar.getAssignment());
     }
 
     public void testGetAssignment() throws Exception {
         Participant participant = getDao().getById(-100);
         Study study = studyDao.getById(-2000);
         Site site = siteDao.getById(-1001);
         assertNotNull("Test setup error: no participant", participant);
         assertNotNull("Test setup error: no study", study);
         assertNotNull("Test setup error: no site", site);
 
         StudyParticipantAssignment actual = getDao().getAssignment(participant, study, site);
         assertNotNull("No assignment found", actual);
         assertEquals("Wrong assignment found", -10, (int) actual.getId());
     }
 }
