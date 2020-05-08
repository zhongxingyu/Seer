 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.SortedSet;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyServiceIntegratedTest extends DaoTestCase {
     private StudyService service;
     private StudyDao studyDao;
     private DeltaService deltaService;
     private AmendmentService amendmentService;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         service = (StudyService) getApplicationContext().getBean("studyService");
         deltaService = (DeltaService) getApplicationContext().getBean("deltaService");
         studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
         amendmentService = (AmendmentService) getApplicationContext().getBean("amendmentService");
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     private int saveBasicSkeleton() {
         int id;
         Study blank = TemplateSkeletonCreator.BASIC.create(null);
         service.save(blank);
 
         assertNotNull("Not saved", blank.getId());
         id = blank.getId();
         interruptSession();
         return id;
     }
 
     public void testCopyDevelopmentAmendmentOnly() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         assertNotNull(loaded.getDevelopmentAmendment());
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
         assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
         assertNull("must not copy  amendment ", copiedStudy.getAmendment());
     }
 
     public void testMustNotCopyStudySites() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         assertFalse("must have study sites", loaded.getStudySites().isEmpty());
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
         assertTrue("must not copy study sites", copiedStudy.getStudySites().isEmpty());
     }
 
     public void testCopyBasicProperties() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
         assertEquals("must copy the assigned identifier ", "ECOG copy", copiedStudy.getAssignedIdentifier());
         assertEquals("must copy the long title ", "Study", copiedStudy.getLongTitle());
         assertNotSame("copied study and source study must be different", loaded, copiedStudy);
         validateIds(loaded, copiedStudy);
     }
 
     public void testCopyPopulations() {
         Study loaded = studyDao.getById(-1);
         List<Change> changes = new ArrayList<Change>();
         assertNotNull(loaded);
         assertFalse("must have populations", loaded.getPopulations().isEmpty());
         assertEquals("must have 1 population", 1, loaded.getPopulations().size());
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         interruptSession();
         copiedStudy = studyDao.getById(copiedStudy.getId());
         for (Delta delta : copiedStudy.getDevelopmentAmendment().getDeltas()) {
             if(delta.getNode() instanceof Study) {
                 changes = delta.getChanges();
             }
         }
         assertNotNull(copiedStudy);
         assertFalse("must add populations under study delta ", changes.isEmpty());
         assertEquals("must add populations under study delta", 1, changes.size());
     }
 
     public void testCopyPlannedCalendar() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         PlannedCalendar plannedCalendar = loaded.getPlannedCalendar();
 
         assertNotNull("must have planned calendar", plannedCalendar);
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
 
         assertNotNull(copiedStudy);
         PlannedCalendar copiedPlannedCalendar = copiedStudy.getPlannedCalendar();
         assertNotNull("must copy planned calendar", copiedPlannedCalendar);
 
         validateNode(plannedCalendar, copiedPlannedCalendar);
         assertSame("copied planned calendar  must belong to copied study", copiedPlannedCalendar.getStudy(), copiedStudy);
     }
 
     public void testCopyEpoch() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         PlannedCalendar calendar = loaded.getPlannedCalendar();
         assertEquals("must have 1 epoch", 1, calendar.getEpochs().size());
         Epoch epoch = calendar.getEpochs().get(0);
 
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
         PlannedCalendar copiedPlannedCalendar = copiedStudy.getPlannedCalendar();
         assertNotNull("must copy planned calendar", copiedPlannedCalendar);
         assertSame(copiedStudy, copiedPlannedCalendar.getStudy());
 
         assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
         Epoch copiedEpoch = revisedStudy.getPlannedCalendar().getEpochs().get(0);
 
         validateNode(epoch, copiedEpoch);
         assertSame("copied epoch  must belong to copied study", copiedEpoch.getParent(), revisedStudy.getPlannedCalendar());
         assertEquals("must copy name", epoch.getName(), copiedEpoch.getName());
     }
 
     public void testCopyStudySegments() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         List<StudySegment> studySegments = loaded.getPlannedCalendar().getEpochs().get(0).getChildren();
         assertEquals("must have 1 study segment", 1, studySegments.size());
         StudySegment studySegment = studySegments.get(0);
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
 
         assertNotNull(copiedStudy);
 
         assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
 
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
         List<StudySegment> copiedStudySegments = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren();
         assertEquals("must have 1 study segment", 1, copiedStudySegments.size());
         StudySegment copiedStudySegment = copiedStudySegments.get(0);
 
         validateNode(studySegment, copiedStudySegment);
         assertSame("copied study segment  must belong to copied study", copiedStudySegment.getParent(), revisedStudy.getPlannedCalendar().getEpochs().get(0));
         assertEquals("must copy name", copiedStudySegment.getName(), studySegment.getName());
     }
 
     public void testCopyPeriods() {
         Study loaded = studyDao.getById(-1);
         loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
 
         assertNotNull(loaded);
         SortedSet<Period> periods = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();
         assertEquals("must have 1 periods", 1, periods.size());
         Period period = periods.iterator().next();
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
 
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
 
         SortedSet<Period> copiedPeriods = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();
 
         assertEquals("must have 1 periods", 1, copiedPeriods.size());
         Period copiedPeriod = copiedPeriods.iterator().next();
 
 
         validateNode(period, copiedPeriod);
         assertEquals("must copy name", copiedPeriod.getName(), period.getName());
         assertEquals("must copy repetitions", copiedPeriod.getRepetitions(), period.getRepetitions());
         assertEquals("must copy startDay", copiedPeriod.getStartDay(), period.getStartDay());
         assertEquals("must copy duration", copiedPeriod.getDuration(), period.getDuration());
         assertNotSame("duration must not be same reference", copiedPeriod.getDuration(), period.getDuration());
     }
 
     public void testCopyPlannedActivities() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
 
         List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
         assertEquals("must have 1 planned activities", 1, plannedActivities.size());
 
         PlannedActivity plannedActivity = plannedActivities.get(0);
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
 
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
 
         List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
 
         assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
 
         PlannedActivity copiedPlannedActivity = copiedPlannedActivities.get(0);
         validateNode(plannedActivity, copiedPlannedActivity);
         assertEquals("must copy activity", copiedPlannedActivity.getActivity(), plannedActivity.getActivity());
         assertSame("activity must be same", copiedPlannedActivity.getActivity(), plannedActivity.getActivity());
         assertEquals("must copy condition", copiedPlannedActivity.getCondition(), plannedActivity.getCondition());
         assertEquals("must copy day", copiedPlannedActivity.getDay(), plannedActivity.getDay());
         assertEquals("must copy details", copiedPlannedActivity.getDetails(), plannedActivity.getDetails());
     }
 
     public void testCopyPlannedActivitiesWithPopulations() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
 
         List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
         assertEquals("must have 1 planned activities", 1, plannedActivities.size());
         Population population = plannedActivities.get(0).getPopulation();
         assertNotNull("planned activity must have population", population);
 
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
 
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
 
         List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
 
 
         PlannedActivity copiedPlannedActivity = copiedPlannedActivities.get(0);
 
         assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
         Population copiedPopulation = copiedPlannedActivity.getPopulation();
         validatePopulation(population, copiedPopulation);
         assertSame("copied planned activity must have the population from copied study only", revisedStudy.getPopulations().iterator().next().getName(), copiedPopulation.getName());
         assertSame("copied planned activity must have the population from copied study only", revisedStudy.getPopulations().iterator().next().getAbbreviation(), copiedPopulation.getAbbreviation());
     }
 
     public void testCopyPlannedActivitiesWithLabels() {
         Study loaded = studyDao.getById(-1);
         assertNotNull(loaded);
         loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
 
         List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
         assertEquals("must have 1 planned activities", 1, plannedActivities.size());
         assertFalse("planned activity must have labels", plannedActivities.get(0).getPlannedActivityLabels().isEmpty());
         PlannedActivityLabel plannedActivityLabel = plannedActivities.get(0).getPlannedActivityLabels().iterator().next();
         Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
         assertNotNull(copiedStudy);
 
         Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
         assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
         assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
 
         List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
 
         assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
         assertFalse("planned activity must have labels", copiedPlannedActivities.get(0).getPlannedActivityLabels().isEmpty());
 
         PlannedActivityLabel copiedPlannedActivityLabel = copiedPlannedActivities.get(0).getPlannedActivityLabels().iterator().next();
         assertNotSame(" labels must not be same", plannedActivityLabel, copiedPlannedActivityLabel);
         validateIds(plannedActivityLabel, copiedPlannedActivityLabel);
     }
 
     private void validateIds(final MutableDomainObject object, final MutableDomainObject copiedObject) {
         assertNotNull(copiedObject.getId());
         assertNotNull(copiedObject.getGridId());
         assertFalse("copied object and source object must have different ids", copiedObject.getId().equals(object.getId()));
         assertFalse("copied object  and source object must have different grid ids", copiedObject.getGridId().equals(object.getGridId()));
     }
 
     private void validatePopulation(final Population population, final Population copiedPopulation) {
         assertNotNull(population);
         assertNotNull(copiedPopulation);
         System.out.println("=====population===" +population);
         System.out.println("=====copiedPopulation===" +copiedPopulation);
         assertNotSame("copied population and source population must be different", copiedPopulation, population);
         assertNotSame("copied population and source population must belong to different different studies", copiedPopulation.getStudy(), population.getStudy());
         validateIds(population, copiedPopulation);
         assertEquals("must copy the name ", copiedPopulation.getName(), population.getName());
         assertEquals("must copy the abbreviation ", copiedPopulation.getAbbreviation(), population.getAbbreviation());
     }
 
     private void validateNode(final PlanTreeNode planTreeNode, final PlanTreeNode copiedPlanTreeNode) {
         assertNotNull(planTreeNode);
         assertNotNull(copiedPlanTreeNode);
 
         assertNotSame("copied node and source node must be different", copiedPlanTreeNode, planTreeNode);
         assertNotSame("copied node and source node must have  different parent", copiedPlanTreeNode.getParent(), planTreeNode.getParent());
         validateIds(planTreeNode, copiedPlanTreeNode);
     }
 
 
     public void testSaveBasicSkeleton() throws Exception {
         int id = saveBasicSkeleton();
 
         Study reloaded = studyDao.getById(id);
         assertEquals("Amendment not saved", 1, reloaded.getDevelopmentAmendment().getDeltas().size());
         List<Change> changes = reloaded.getDevelopmentAmendment().getDeltas().get(0).getChanges();
         // The detail of the epochs are tested in more detail in TemplateSkeletonCreatorTest
         assertEquals("Wrong number of changes", 2, changes.size());
         assertNotNull("First epoch ID not saved with amendment",
                 ((Add) changes.get(0)).getChildId());
         assertNotNull("Second epoch ID not saved with amendment",
                 ((Add) changes.get(1)).getChildId());
     }
 
     public void testSaveWithChangedDeltaDoesNotUpdateAmendment() throws Exception {
         int id = saveBasicSkeleton();
 
         int originalAmendmentVersion;
         {
             Study original = studyDao.getById(id);
             originalAmendmentVersion = original.getDevelopmentAmendment().getVersion();
             log.info("Development amendment under test has id={}", original.getDevelopmentAmendment().getId());
             Delta<?> originalDelta = original.getDevelopmentAmendment().getDeltas().get(0);
             assertEquals("Test setup failure: wrong number of changes in delta", 2,
                     originalDelta.getChanges().size());
             originalDelta.addChange(Add.create(Epoch.create("N")));
             service.save(original);
         }
 
         interruptSession();
 
         Study reloaded = studyDao.getById(id);
         Delta<?> reloadedDelta = reloaded.getDevelopmentAmendment().getDeltas().get(0);
         assertEquals("New change not present in reloaded study", 3,
                 reloadedDelta.getChanges().size());
         assertTrue("New change not present in reloaded study", reloadedDelta.getChanges().get(2) instanceof Add);
 
         assertEquals("Amendment version changed even though the amendment itself didn't change",
                 originalAmendmentVersion, (int) reloaded.getDevelopmentAmendment().getVersion());
     }
 
     public void testFullTemplateHistoryIsTransient() throws Exception {
         int id = saveBasicSkeleton();
         Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
         assertMemoryOnly(fullHistory);
         assertMemoryOnly(fullHistory.getPlannedCalendar());
         for (Amendment amendment : fullHistory.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     if (change instanceof ChildrenChange) {
                         assertMemoryOnly(((ChildrenChange) change).getChild());
                     }
                 }
             }
         }
     }
 
     private void assertMemoryOnly(TransientCloneable<?> node) {
         assertTrue(node + " is not memory-only", node.isMemoryOnly());
         if (node instanceof Parent) {
             for (Object child : ((Parent) node).getChildren()) {
                 assertMemoryOnly((TransientCloneable<?>) child);
             }
         }
     }
 
     public void testFullTemplateHistoryIncludesReleasedRemovedNodes() throws Exception {
         int id = saveBasicSkeleton();
 
         {
             Study reloaded = studyDao.getById(id);
             amendmentService.amend(reloaded);
             Epoch treatment = reloaded.getPlannedCalendar().getEpochs().get(0);
             Amendment dev = Fixtures.createAmendment("Bye-bye C", DateTools.createDate(2007, Calendar.APRIL, 6));
             reloaded.setDevelopmentAmendment(dev);
             dev.addDelta(
                 Delta.createDeltaFor(treatment, Remove.create(treatment.getStudySegments().get(2)))
             );
             amendmentService.amend(reloaded);
         }
 
         interruptSession();
 
         Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
         PlannedCalendarDelta initialDelta = (PlannedCalendarDelta) fullHistory.getAmendment().getPreviousAmendment().getDeltas().get(0);
         assertEquals("Wrong number of changes", 2, initialDelta.getChanges().size());
         Add treatmentAdd = (Add) initialDelta.getChanges().get(0);
         assertTrue("Treatment Add doesn't add an Epoch: " + treatmentAdd.getChild(),
             treatmentAdd.getChild() instanceof Epoch);
         Epoch treatment = (Epoch) treatmentAdd.getChild();
         assertEquals(
             "Wrong number of study segments in treatment epoch of initial template: " + treatment.getStudySegments(),
             3, treatment.getStudySegments().size());
     }
 }
