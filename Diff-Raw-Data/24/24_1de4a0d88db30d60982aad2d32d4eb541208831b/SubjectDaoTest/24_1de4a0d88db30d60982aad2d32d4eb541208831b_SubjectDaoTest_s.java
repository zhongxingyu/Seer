 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase.assertSameDay;
 
 /**
  * @author Padmaja Vedula
  * @author Rhett Sutphin
  */
 public class SubjectDaoTest extends ContextDaoTestCase<SubjectDao> {
     private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
     private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
     private AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
 
     public void testGetAll() throws Exception {
         List<Subject> actual = getDao().getAll();
         assertEquals("Wrong size", 2, actual.size());
         assertEquals("Wrong first subject", "Ng", actual.get(0).getLastName());
         assertEquals("Wrong second subject", "Scott", actual.get(1).getLastName());
     }
 
     public void testGetById() throws Exception {
         Subject subject = getDao().getById(-100);
         assertNotNull("Subject not found", subject);
         assertEquals("Wrong last name", "Scott", subject.getLastName());
     }
 
     public void testGetIncludesProperties() throws Exception {
         Subject subject = getDao().getById(-101);
         assertEquals("Wrong number of properties", 2, subject.getProperties().size());
         assertSubjectProperty("Wrong 1st prop",
             "Preferred Contact Address", "world@bridge.us", subject.getProperties().get(0));
         assertSubjectProperty("Wrong 2nd prop",
             "Hair Color", "blue", subject.getProperties().get(1));
     }
 
     private void assertSubjectProperty(String message, String expectedName, String expectedValue, SubjectProperty actual) {
         assertEquals(message + ": wrong name", expectedName, actual.getName());
         assertEquals(message + ": wrong value", expectedValue, actual.getValue());
     }
 
     public void testSaveAssignment() throws Exception {
         {
             Site site = siteDao.getById(-1001);
             StudySite studySite = site.getStudySites().get(0);
             assertEquals("Wrong study site found in test setup", -3001, (int) studySite.getId());
             Subject subject = getDao().getById(-100);
             assertEquals("Subject should already have one assignment", 1, subject.getAssignments().size());
 
             StudySubjectAssignment spa = new StudySubjectAssignment();
             spa.setSubject(subject);
             spa.setStudySite(studySite);
             spa.setStartDate(new Date());
             spa.setEndDate(DateUtils.createDate(2008, 12, 1));
             spa.setCurrentAmendment(amendmentDao.getById(-55));
 
             subject.addAssignment(spa);
 
             getDao().save(subject);
         }
 
         interruptSession();
 
         Subject loaded = getDao().getById(-100);
         assertNotNull("Subject reloading failed", loaded);
         assertEquals("Assignment not saved", 2, loaded.getAssignments().size());
         StudySubjectAssignment newAssignment = loaded.getAssignments().get(1);
         assertEquals("Wrong subject", -100, (int) newAssignment.getSubject().getId());
         assertEquals("Wrong study site", -3001, (int) newAssignment.getStudySite().getId());
         assertSameDay("Wrong start date", new Date(), newAssignment.getStartDate());
         assertSameDay("Wrong end date", DateUtils.createDate(2008, 12, 1), newAssignment.getEndDate());
     }
 
     public void testSaveNewSubject() throws Exception {
         Integer savedId;
         {
             Subject subject = new Subject();
             subject.setFirstName("Jeff");
             subject.setLastName("Someone");
             subject.setGender(Gender.MALE);
             subject.setDateOfBirth(new Date());
             subject.setPersonId("123-45-6789");
 
             getDao().save(subject);
             savedId = subject.getId();
             assertNotNull("The saved subject id", savedId);
         }
 
         interruptSession();
 
         {
             Subject loaded = getDao().getById(savedId);
             assertNotNull("Could not reload subject id " + savedId, loaded);
             assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
             assertEquals("Wrong lastname", "Someone", loaded.getLastName());
             assertEquals("Wrong gender", Gender.MALE, loaded.getGender());
         }
     }
 
     public void testSaveSubjectWithoutPersonIdWorks() throws Exception {
         Integer savedId;
         {
             Subject subject = new Subject();
             subject.setFirstName("Jeff");
             subject.setLastName("Someone");
             subject.setGender(Gender.MALE);
             subject.setDateOfBirth(new Date());
 
             getDao().save(subject);
             savedId = subject.getId();
             assertNotNull("The saved subject id", savedId);
         }
 
         interruptSession();
 
         {
             Subject loaded = getDao().getById(savedId);
             assertNotNull("Could not reload subject id " + savedId, loaded);
             assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
             assertEquals("Wrong lastname", "Someone", loaded.getLastName());
             assertEquals("Wrong gender", Gender.MALE, loaded.getGender());
         }
     }
 
     public void testSaveSubjectWithOnlyPersonIdWorks() throws Exception {
         Integer savedId;
         {
             Subject subject = new Subject();
             subject.setGender(Gender.MALE);
             subject.setPersonId("123-45-6789");
 
             getDao().save(subject);
             savedId = subject.getId();
             assertNotNull("The saved subject id", savedId);
         }
 
         interruptSession();
 
         {
             Subject loaded = getDao().getById(savedId);
             assertNotNull("Could not reload subject id " + savedId, loaded);
             assertEquals("Wrong person ID", "123-45-6789", loaded.getPersonId());
         }
     }
 
     public void testSaveExistingWithNewPropertiesWorks() throws Exception {
         {
             Subject loaded = getDao().getById(-100);
             loaded.getProperties().add(new SubjectProperty("Seat Preference", "window"));
             loaded.getProperties().add(new SubjectProperty("Meal Preference", "vegetarian"));
             getDao().save(loaded);
         }
 
         interruptSession();
 
         Subject reloaded = getDao().getById(-100);
         assertEquals("Wrong number of properties", 2, reloaded.getProperties().size());
         assertSubjectProperty("Wrong 1st prop",
             "Seat Preference", "window", reloaded.getProperties().get(0));
         assertSubjectProperty("Wrong 2nd prop",
             "Meal Preference", "vegetarian", reloaded.getProperties().get(1));
     }
 
     public void testSaveNewWithPropertiesWorks() throws Exception {
         int newId;
         {
             Subject newSubject = Fixtures.createSubject(
                 "11.5", "Fred", "Jones", DateTools.createDate(1894, Calendar.JANUARY, 13));
             newSubject.getProperties().add(new SubjectProperty("Preferred conveyance", "Buggy"));
             getDao().save(newSubject);
             newId = newSubject.getId();
         }
 
         interruptSession();
 
         Subject reloaded = getDao().getById(newId);
         assertEquals("Wrong number of properties", 1, reloaded.getProperties().size());
         assertSubjectProperty("Wrong 1st prop",
             "Preferred conveyance", "Buggy", reloaded.getProperties().get(0));
     }
 
     public void testReplacingPropertyValuesWorks() throws Exception {
         {
             Subject loaded = getDao().getById(-101);
             assertEquals("Test setup failure", 2, loaded.getProperties().size());
             loaded.getProperties().get(1).setValue("red-pink");
             getDao().save(loaded);
         }
 
         interruptSession();
 
         Subject reloaded = getDao().getById(-101);
         assertSubjectProperty("Not updated",
             "Hair Color", "red-pink", reloaded.getProperties().get(1));
     }
 
     public void testPropertyOrderPreserved() throws Exception {
         {
             Subject loaded = getDao().getById(-101);
             assertEquals("Test setup failure", 2, loaded.getProperties().size());
             loaded.getProperties().add(1, new SubjectProperty("Foreign National", "true"));
             getDao().save(loaded);
         }
 
         interruptSession();
 
         Subject reloaded = getDao().getById(-101);
         assertSubjectProperty("Wrong 1st prop",
             "Preferred Contact Address", "world@bridge.us", reloaded.getProperties().get(0));
         assertSubjectProperty("Wrong 2nd prop",
             "Foreign National", "true", reloaded.getProperties().get(1));
         assertSubjectProperty("Wrong 3rd prop",
             "Hair Color", "blue", reloaded.getProperties().get(2));
     }
 
     public void testLoadScheduledCalendar() throws Exception {
         Subject loaded = getDao().getById(-100);
         StudySubjectAssignment assignment = loaded.getAssignments().get(0);
         ScheduledCalendar actualCalendar = assignment.getScheduledCalendar();
         assertNotNull("Scheduled calendar not found", actualCalendar);
         assertEquals("Wrong scheduled calendar found", -11, (int) actualCalendar.getId());
         assertSame("Relationship not bidirectional", assignment, actualCalendar.getAssignment());
     }
 
     public void testGetAssignment() throws Exception {
         Subject subject = getDao().getById(-100);
         Study study = studyDao.getById(-2000);
         Site site = siteDao.getById(-1001);
         assertNotNull("Test setup error: no subject", subject);
         assertNotNull("Test setup error: no study", study);
         assertNotNull("Test setup error: no site", site);
 
         StudySubjectAssignment actual = getDao().getAssignment(subject, study, site);
         assertNotNull("No assignment found", actual);
         assertEquals("Wrong assignment found", -10, (int) actual.getId());
     }
     
     public void testGetByGridId() throws Exception {
         Subject subject = getDao().getByGridId("What is the");
         assertNotNull("No subject matched", subject);
         assertEquals("Wrong subject matched", -101, (int) subject.getId());
     }
 
     // Becuase of Constraint @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN}) in Subject,
     // the deletion deletes subject and it's referred children
     public void testDeleteSubjectWithSubjectAssigment() throws Exception {
         Subject subject = getDao().getById(-100);
         getDao().delete(subject);
         Subject subject1 = getDao().getById(-100);
         assertNull("Subject is null, althouth it shoudln't be ", subject1);
     }
 
     public void testDeleteSubject() throws Exception {
         Subject subject = getDao().getById(-101);
         getDao().delete(subject);
         Subject subject1 = getDao().getById(-101);
         assertNull("Subject is not null, althouth it shoudl be ", subject1);
     }
 
     public void testFindSubjectByPersonIdWhenIdIsNull() throws Exception {
         Subject subject = getDao().getByPersonId(null);
         assertNull("Subject is not null ", subject);
     }
 }
