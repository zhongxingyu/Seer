 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyDaoTest extends DaoTestCase {
     private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");
 
     public void testGetById() throws Exception {
         Study study = dao.getById(100);
         assertNotNull("Study 1 not found", study);
         assertEquals("Wrong name", "First Study", study.getName());
     }
 
     public void testLoadingArms() throws Exception {
         Study study = dao.getById(100);
         assertNotNull("Study 1 not found", study);
 
         assertEquals("Wrong number of arms", 2, study.getArms().size());
         assertArm("Wrong arm 0", 201, 1, "Sinister", study.getArms().get(0));
         assertArm("Wrong arm 1", 200, 2, "Dexter", study.getArms().get(1));
 
         assertSame("Arm <=> Study relationship not bidirectional on load", study, study.getArms().get(0).getStudy());
     }
 
     public void testSaveNewStudy() throws Exception {
         Integer savedId;
         {
             Study study = new Study();
             study.setName("New study");
             study.addArm(new Arm());
             study.getArms().get(0).setName("First Arm");
             dao.save(study);
             savedId = study.getId();
             assertNotNull("The saved study didn't get an id", savedId);
         }
 
         interruptSession();
 
         {
             Study loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId);
             assertEquals("Wrong name", "New study", loaded.getName());
             // TODO: cascade saving arms
             // assertEquals("Wrong number of arms", 1, loaded.getArms().size());
             // assertEquals("Wrong name for arm 0", "First arm", loaded.getArms().get(0).getName());
             // assertEquals("Wrong number for arm 0", (Integer) 1, loaded.getArms().get(0).getNumber());
         }
     }
 
     private static void assertArm(
         String message, Integer expectedId, Integer expectedNumber, String expectedName, Arm actualArm
     ) {
         assertEquals(message + ": wrong id", expectedId, actualArm.getId());
         assertEquals(message + ": wrong number", expectedNumber, actualArm.getNumber());
         assertEquals(message + ": wrong name", expectedName, actualArm.getName());
     }
 }
