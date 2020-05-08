 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import static org.easymock.classextension.EasyMock.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class EditPeriodCommandTest extends StudyCalendarTestCase {
     private static final Duration.Unit DURATION_UNIT = Duration.Unit.day;
     private static final Integer DURATION_QUANTITY = 71;
     private static final Integer START_DAY = 9;
     private static final String NAME = "Unethical";
 
     private EditPeriodCommand command;
     private Period period;
     private StudySegment studySegment;
     private Study study;
     private AmendmentService amendmentService;
     private TemplateService templateService;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         period = new Period();
         period.setName(NAME);
         period.setStartDay(START_DAY);
         period.getDuration().setQuantity(DURATION_QUANTITY);
         period.getDuration().setUnit(DURATION_UNIT);
         period.setId(88);
 
         study = Fixtures.createBasicTemplate();
         studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
         studySegment.addPeriod(period);
         
         amendmentService = registerMockFor(AmendmentService.class);
         templateService = registerMockFor(TemplateService.class);
         initCommand();
     }
 
     private void initCommand() {
         command = new EditPeriodCommand(period, amendmentService, templateService);
     }
 
     public void testOriginalPeriodClonedIntoCommand() throws Exception {
         assertEquals(NAME, command.getPeriod().getName());
         assertEquals(START_DAY, command.getPeriod().getStartDay());
         assertEquals(DURATION_QUANTITY, command.getPeriod().getDuration().getQuantity());
         assertEquals(DURATION_UNIT, command.getPeriod().getDuration().getUnit());
 
         command.getPeriod().setName("Alternate");
         assertEquals("Original period changed on set", NAME, period.getName());
         assertEquals("Command period not changed on set", "Alternate", command.getPeriod().getName());
     }
 
     public void testApplyNameChange() throws Exception {
         command.getPeriod().setName("Ethical");
 
         expectSinglePropertyUpdate("name", NAME, "Ethical");
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 
     public void testApplyStartDayChange() throws Exception {
         command.getPeriod().setStartDay(42);
 
         expectSinglePropertyUpdate("startDay", START_DAY.toString(), Integer.toString(42));
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 
     public void testApplyDurationChange() throws Exception {
         command.getPeriod().getDuration().setUnit(Duration.Unit.week);
         command.getPeriod().getDuration().setQuantity(10);
 
         amendmentService.updateDevelopmentAmendment(period,
             PropertyChange.create("duration.quantity", "71", "10"),
             PropertyChange.create("duration.unit", "day", "week")
         );
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 
     public void testApplyRepetitionsChange() throws Exception {
         command.getPeriod().setRepetitions(1000);
 
         expectSinglePropertyUpdate("repetitions", "1", "1000");
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 
    public void testPurgeOldPlannedActivitiesFirst() throws Exception {
         PlannedActivity chem = Fixtures.createPlannedActivity("Chem-7", 3);
         PlannedActivity cbc = Fixtures.createPlannedActivity("CBC", 70);
         period.addPlannedActivity(chem);
         period.addPlannedActivity(cbc);
         command.getPeriod().getDuration().setQuantity(60);
 
         amendmentService.updateDevelopmentAmendment(period,
            Remove.create(cbc), PropertyChange.create("duration.quantity", "71", "60"));
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 
     public void testGetStudySegmentForNewlyAddedPeriod() throws Exception {
         period.setStudySegment(null);
         study.setDevelopmentAmendment(new Amendment("dev"));
         study.getDevelopmentAmendment().addDelta(Delta.createDeltaFor(studySegment, Add.create(period)));
         initCommand();
 
         expect(templateService.findParent(eqByClassAndId(period))).andReturn(studySegment).atLeastOnce();
 
         replayMocks();
         assertSame(studySegment, command.getStudySegment());
         assertNotNull(command.getStudySegment());
         verifyMocks();
     }
 
     private static <T extends DomainObject> T eqByClassAndId(final T t) {
         EasyMock.reportMatcher(new IArgumentMatcher() {
             public boolean matches(Object argument) {
                 DomainObject other = (DomainObject) argument;
                 return other != null && other.getId().equals(t.getId())
                     && other.getClass().equals(t.getClass());
             }
 
             public void appendTo(StringBuffer buffer) {
                 buffer.append(t.getClass().getName()).append(" with id ").append(t.getId());
             }
         });
         return null;
     }
 
     private void expectSinglePropertyUpdate(String property, String oldV, String newV) {
         amendmentService.updateDevelopmentAmendment(
             same(period), eq(PropertyChange.create(property, oldV, newV)));
     }
 }
