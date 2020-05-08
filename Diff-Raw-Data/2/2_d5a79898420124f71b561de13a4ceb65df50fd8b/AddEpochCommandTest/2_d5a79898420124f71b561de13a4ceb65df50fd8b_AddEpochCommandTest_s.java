 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createEpoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class AddEpochCommandTest extends StudyCalendarTestCase {
     private AddEpochCommand command;
     private StudyDao studyDao;
 
     protected void setUp() throws Exception {
         super.setUp();
         studyDao = registerDaoMockFor(StudyDao.class);
         command = new AddEpochCommand(studyDao);
     }
 
     public void testApply() throws Exception {
         Study study = new Study();
         study.setPlannedCalendar(new PlannedCalendar());
         command.setStudy(study);
 
         studyDao.save(study);
         replayMocks();
 
         command.apply();
         verifyMocks();
 
         assertEquals(1, study.getPlannedCalendar().getEpochs().size());
         Epoch actualEpoch = study.getPlannedCalendar().getEpochs().get(0);
         assertEquals("Wrong name on new epoch", "New Epoch", actualEpoch.getName());
         assertEquals("Epoch missing single arm", 1, actualEpoch.getArms().size());
     }
 
     public void testModel() throws Exception {
         Study study = new Study();
         study.setPlannedCalendar(new PlannedCalendar());
         Epoch e1 = createEpoch("E1");
         Epoch e2 = createEpoch("E2");
         Epoch e3 = createEpoch("New Epoch");
         study.getPlannedCalendar().addEpoch(e1);
         study.getPlannedCalendar().addEpoch(e2);
         study.getPlannedCalendar().addEpoch(e3);
         command.setStudy(study);
 
         replayMocks();
         Map<String, Object> model = command.getModel();
         verifyMocks();
 
        assertEquals(2, model.size());
         assertContainsPair("Missing epoch", model, "epoch", e3);
     }
 }
