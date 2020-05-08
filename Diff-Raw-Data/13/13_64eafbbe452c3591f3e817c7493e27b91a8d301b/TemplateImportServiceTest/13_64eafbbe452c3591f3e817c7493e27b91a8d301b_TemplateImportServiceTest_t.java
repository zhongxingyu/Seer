 package edu.northwestern.bioinformatics.studycalendar.service.importer;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.DaoTools;
 import edu.northwestern.bioinformatics.studycalendar.dao.DynamicMockDaoFinder;
 import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.LocalGridIdentifierCreator;
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.Child;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
 import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Jalpa Patel
  */
 public class TemplateImportServiceTest extends StudyCalendarTestCase {
     private StudyXmlSerializer studyXmlSerializer;
     private StudyDao studyDao;
     private TemplateService templateService;
     private TemplateDevelopmentService templateDevelopmentService;
     private DeltaService deltaService;
     private GridIdentifierResolver gridIdentifierResolver;
     private LocalGridIdentifierCreator localGridIdentifierCreator;
     private ActivityDao activityDao;
     private ActivityService activityService;
     private StudyService studyService;
     private AmendmentService amendmentService;
     private DynamicMockDaoFinder daoFinder;
     private TemplateImportService service;
     private DaoTools daoTools;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         studyXmlSerializer = registerMockFor(StudyXmlSerializer.class);
         templateService = registerMockFor(TemplateService.class);
         templateDevelopmentService = registerMockFor(TemplateDevelopmentService.class);
         deltaService = registerMockFor(DeltaService.class);
         activityService = registerMockFor(ActivityService.class);
         gridIdentifierResolver = registerMockFor(GridIdentifierResolver.class);
         localGridIdentifierCreator = registerMockFor(LocalGridIdentifierCreator.class);
         studyService = registerMockFor(StudyService.class);
         amendmentService = registerMockFor(AmendmentService.class);
         daoFinder = new DynamicMockDaoFinder();
         activityDao = registerDaoMockFor(ActivityDao.class);
         studyDao = registerDaoMockFor(StudyDao.class);
         daoTools =  registerMockFor(DaoTools.class);
 
         service = new TemplateImportService();
         service.setActivityDao(activityDao);
         service.setActivityService(activityService);
         service.setAmendmentService(amendmentService);
         service.setDaoFinder(daoFinder);
         service.setDeltaService(deltaService);
         service.setGridIdentifierResolver(gridIdentifierResolver);
         service.setLocalGridIdentifierCreator(localGridIdentifierCreator);
         service.setStudyDao(studyDao);
         service.setStudyService(studyService);
         service.setStudyXmlSerializer(studyXmlSerializer);
         service.setTemplateDevelopmentService(templateDevelopmentService);
         service.setTemplateService(templateService);
         service.setDaoTools(daoTools);
     }
 
     @Override
     protected void replayMocks() {
         super.replayMocks();
         daoFinder.replayAll();
     }
 
     @Override
     protected void verifyMocks() {
         daoFinder.verifyAll();
         super.verifyMocks();
     }
 
     public void testReadAndSaveTemplateForNewStudy() throws Exception {
         Study study = createStudy();
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study, target);
         expectExistingStudyFound(null, study.getAssignedIdentifier());
         Change change = study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         expectDeltaAndChangesForTemplateIndex(((Add)change));
         expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
         expectNoGridIdConflicts(study);
         expectDaoForPlannedCalendar(study);
         EpochDao epochDao = daoFinder.expectDaoFor(Epoch.class, EpochDao.class);
         expect(epochDao.getByGridId(((Add)change).getChild().getGridId())).andReturn((Epoch)((Add)change).getChild());
         studyDao.save(study);
         daoTools.forceFlush();
         studyService.save(study);
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
     }
 
     public void testReadAndSaveWhenNewSudyHasLessAmendmentsThanExistingStudy() throws Exception {
         Study study =  createStudy();
         study.setAmendment(study.getDevelopmentAmendment());
         Study newStudy = createStudy();
         Amendment oldAmendment = Fixtures.createAmendment("OldAmendment", DateTools.createDate(2007, Calendar.APRIL, 6));
         Delta<Epoch> delta1 = Delta.createDeltaFor(new Epoch());
         delta1.setGridId("delta1");
         delta1.addChanges(Add.create(new StudySegment()));
         oldAmendment.addDelta(delta1);
         study.pushAmendment(oldAmendment);
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(newStudy, target);
         expectExistingStudyFound(study,  newStudy.getAssignedIdentifier());
         expectExistingStudy(study);
         replayMocks();
         try {
             service.readAndSaveTemplate(target);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             assertEquals("Imported study doesn't have all released amendment as of existing study", scve.getMessage());
         }
     }
 
     public void testReadAndSaveWhenExistingAmendmentHasDifferences() throws Exception {
         Study study =  createStudy();
         study.setAmendment(study.getDevelopmentAmendment());
         Study existingStudy = createStudy();
         existingStudy.getDevelopmentAmendment().setName("Amendment1");
         existingStudy.setAmendment(existingStudy.getDevelopmentAmendment());
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study, target);
         expectExistingStudyFound(existingStudy, study.getAssignedIdentifier());
         expectExistingStudy(existingStudy);
         replayMocks();
         try {
             service.readAndSaveTemplate(target);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
            assertTrue("Wrong message: " + scve.getMessage(),
                scve.getMessage().
                    startsWith("Existing released amendment 04/06/2007 (Amendment1) differs from released amendment 04/06/2007 (Amendment) in imported template:"));
         }
     }
 
     public void testReadAndSaveWhenGridIdConflictsForNewStudy() throws Exception {
         Study study = createStudy();
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study,target);
         expectExistingStudyFound(null, study.getAssignedIdentifier());
         Change change = study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         expectDeltaAndChangesForTemplateIndex(((Add)change));
         expectGridIdConflictsForChildNode(study);
         expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
         expect(localGridIdentifierCreator.getGridIdentifier()).andReturn("new-epoch");
         expectDaoForPlannedCalendar(study);
         EpochDao epochDao = daoFinder.expectDaoFor(Epoch.class, EpochDao.class);
         expect(epochDao.getByGridId("new-epoch")).andReturn((Epoch)((Add)change).getChild());
         studyDao.save(study);
         daoTools.forceFlush();
         studyService.save(study);
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
         assertEquals("Grid Id is not changed", "new-epoch", ((Add)change).getChild().getGridId());
     }
 
     public void testReadAndSaveWhenStudyHasNewAmendment() throws Exception {
         Study newStudy = createReleasesStudy();
         Study existingStudy = createReleasesStudy();
         Amendment newAmendment = Fixtures.createAmendment("NewAmendment", DateTools.createDate(2007, Calendar.APRIL, 6));
         Epoch epoch = new Epoch();
         epoch.setGridId("epoch");
         Delta<Epoch> newDelta = Delta.createDeltaFor(epoch);
         newDelta.setGridId("delta1");
         StudySegment studySegment =  new StudySegment();
         studySegment.setName("NewA");
         studySegment.setGridId("segment1");
         Add add = Add.create(studySegment);
         add.setGridId("add-new");
         newDelta.addChanges(add);
         newAmendment.addDelta(newDelta);
         newStudy.pushAmendment(newAmendment);
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(newStudy, target);
         expectExistingStudyFound(existingStudy, newStudy.getAssignedIdentifier());
         expectExistingStudy(existingStudy);
         for (Change change : existingStudy.getAmendment().getDeltas().get(0).getChanges()) {
             expectDeltaAndChangesForTemplateIndex((Add)change);
 
         }
 
         for (Amendment amendment : newStudy.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     expectDeltaAndChangesForTemplateIndex((Add)change);
                     expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
                 }
             }
         }
         expectGridIdConflictsCheck(studySegment, false);
         expectGridIdConflictsCheck(epoch, false);
         expectGridIdConflictsCheck(add, false);
         expectGridIdConflictsCheck(newDelta, false);
         studyDao.save(existingStudy);
         daoTools.forceFlush();
         expect(daoFinder.expectDaoFor(Epoch.class, EpochDao.class).getByGridId("epoch")).andReturn(epoch);
         expect(daoFinder.expectDaoFor(StudySegment.class, StudySegmentDao.class).getByGridId("segment1")).andReturn(studySegment);
         amendmentService.amend(existingStudy);
         studyService.save(existingStudy);
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
     }
 
     public void testReadAndSaveWhenExistingStudyHasGridIdConflictsForReleasedAmendment() throws Exception {
         Study newStudy = createReleasesStudy();
         Study existingStudy = createReleasesStudy();
         Add add = (Add)newStudy.getAmendment().getDeltas().get(0).getChanges().get(0);
        add.setGridId("foom");
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(newStudy, target);
         expectExistingStudyFound(existingStudy, newStudy.getAssignedIdentifier());
         expectExistingStudy(existingStudy);
         for (Amendment amendment : existingStudy.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     expectDeltaAndChangesForTemplateIndex((Add)change);
                 }
             }
         }
         for (Amendment amendment : newStudy.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     expectDeltaAndChangesForTemplateIndex((Add)change);
                     expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
                 }
             }
         }
         replayMocks();
         try {
             service.readAndSaveTemplate(target);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             String expectedMessage = "Imported study and existing study has different grid ids for released amendments";
             assertEquals(expectedMessage, scve.getMessage());
         }
     }
 
     public void testReadAndSaveWhenExistingStudyHasGridIdConflictsForNewAmendment() throws Exception {
         Study newStudy = createReleasesStudy();
         Study existingStudy = createReleasesStudy();
         Amendment newAmendment = Fixtures.createAmendment("NewAmendment", DateTools.createDate(2007, Calendar.APRIL, 6));
         Epoch epoch = new Epoch();
         epoch.setGridId("epoch");
         Delta<Epoch> newDelta = Delta.createDeltaFor(epoch);
         newDelta.setGridId("delta1");
         StudySegment studySegment =  new StudySegment();
         studySegment.setName("NewA");
         studySegment.setGridId("segment1");
         Add add = Add.create(studySegment);
         add.setGridId("add-new");
         newDelta.addChanges(add);
         newAmendment.addDelta(newDelta);
         newStudy.pushAmendment(newAmendment);
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(newStudy, target);
         expectExistingStudyFound(existingStudy, newStudy.getAssignedIdentifier());
         expectExistingStudy(existingStudy);
 
         expectGridIdConflictsCheck(studySegment, false);
         expectGridIdConflictsCheck(epoch, false);
         expectGridIdConflictsCheck(add, false);
         expectGridIdConflictsCheck(newDelta, true);
         for (Change change : existingStudy.getAmendment().getDeltas().get(0).getChanges()) {
             expectDeltaAndChangesForTemplateIndex((Add)change);
         }
 
         for (Amendment amendment : newStudy.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     expectDeltaAndChangesForTemplateIndex((Add)change);
                     expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
                 }
             }
         }
         replayMocks();
         try {
             service.readAndSaveTemplate(target);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             String expectedMessage = "Existing study has new amendments with [ grid id delta1 ] already exists in system";
             assertEquals(expectedMessage, scve.getMessage());
         }
     }
 
     public void testReadAndSaveWhenDeltaHasUnknownNode() throws Exception {
         Study study = createStudy();
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study, target);
         expectExistingStudyFound(null, study.getAssignedIdentifier());
         Change change = study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         expectDeltaAndChangesForTemplateIndex(((Add)change));
         expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
         expectNoGridIdConflicts(study);
         expect(daoFinder.expectDaoFor(PlannedCalendar.class, PlannedCalendarDao.class).
               getByGridId(study.getPlannedCalendar().getGridId())).andReturn(null);
         studyDao.save(study);
         daoTools.forceFlush();
         replayMocks();
         try {
             service.readAndSaveTemplate(target);
             fail("Exception not thrown");
         } catch (StudyCalendarValidationException scve) {
             String expectedMessage = "Delta with id pc-delta references unknown node with id cal. Please check the node id.";
             assertEquals(expectedMessage, scve.getMessage());
         }
     }
 
     public void testReadAndSaveWhenNewActivityWithNewSourceAndNewType() throws Exception {
         Source source = createSource("Source");
         ActivityType type = createActivityType("DISEASE_MEASURE");
         Activity activity  = createActivity("Activity");
         activity.setSource(source);
         activity.setType(type);
         PlannedActivity pa = createPlannedActivity(activity,  3);
         List<PlannedActivity> plannedActivities = new ArrayList<PlannedActivity>();
         plannedActivities.add(pa);
         Study study = createStudy();
 
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study, target);
         expectExistingStudyFound(null, study.getAssignedIdentifier());
         Change change = study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         expectDeltaAndChangesForTemplateIndex(((Add)change));
         expectForNewActivities(((Add)change), plannedActivities);
         expectNoGridIdConflicts(study);
 
         expect(activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName())).andReturn(null);
         activityService.saveActivity(activity);
         studyDao.save(study);
         daoTools.forceFlush();
         studyService.save(study);
         expectDaoForPlannedCalendar(study);
         expect(daoFinder.expectDaoFor(Epoch.class, EpochDao.class).
                 getByGridId(((Add)change).getChild().getGridId())).andReturn((Epoch)((Add)change).getChild());
 
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
     }
 
     public void testreadAndSaveStudyAttributeForNewStudy() throws Exception {
         Study study = createStudy();
         String assignedIdentifier = "AssignedIdentifier";
         study.setAssignedIdentifier(assignedIdentifier);
         String longTitle = "LongTitle";
         study.setLongTitle(longTitle);
         StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "Type1", "ident1");
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(study, target);
         expectExistingStudyFound(null, study.getAssignedIdentifier());
         Change change = study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         expectDeltaAndChangesForTemplateIndex(((Add)change));
         expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
         expectNoGridIdConflicts(study);
         expectDaoForPlannedCalendar(study);
         EpochDao epochDao = daoFinder.expectDaoFor(Epoch.class, EpochDao.class);
         expect(epochDao.getByGridId(((Add)change).getChild().getGridId())).andReturn((Epoch)((Add)change).getChild());
         studyDao.save(study);
         daoTools.forceFlush();
         studyService.save(study);
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
 
         assertEquals("New study doesn't have identifier", assignedIdentifier, study.getAssignedIdentifier());
         assertNotNull(study.getLongTitle());
         assertEquals("New study doesn't have long title", longTitle, study.getLongTitle());
         assertEquals("New study doesn't have secondary identifiers", 1, study.getSecondaryIdentifiers().size());
         assertEquals("New study doesn't have correct secondary identifiers",
                 identifier.getValue(), study.getSecondaryIdentifierValue("Type1"));
     }
 
     public void testReadAndSaveUpdateStudyAttributeWhenExistingStudy() throws Exception {
         Study newStudy = createReleasesStudy();
         String newIdentifier = "UpdatedAssignedIdentifier";
         newStudy.setAssignedIdentifier(newIdentifier);
         String longTitle = "LongTitle";
         newStudy.setLongTitle(longTitle);
         StudySecondaryIdentifier identifier = addSecondaryIdentifier(newStudy, "Type1", "ident1");
         Study existingStudy = createReleasesStudy();
         InputStream target = registerMockFor(InputStream.class);
         expectStudyLoadedFromXml(newStudy, target);
         expectExistingStudyFound(existingStudy, newStudy.getAssignedIdentifier());
         expectExistingStudy(existingStudy);
         for (Change change : existingStudy.getAmendment().getDeltas().get(0).getChanges()) {
             expectDeltaAndChangesForTemplateIndex((Add)change);
 
         }
         for (Amendment amendment : newStudy.getAmendmentsList()) {
             for (Delta<?> delta : amendment.getDeltas()) {
                 for (Change change : delta.getChanges()) {
                     expectDeltaAndChangesForTemplateIndex((Add)change);
                     expectForNewActivities(((Add)change), new ArrayList<PlannedActivity>());
                 }
             }
         }
         studyDao.save(existingStudy);
         daoTools.forceFlush();
         studyService.save(existingStudy);
         assertEquals("Existing study has different identifier", "study", existingStudy.getAssignedIdentifier());
         assertNull(existingStudy.getLongTitle());
         assertEquals("Existing study has secondary identifiers", 0, existingStudy.getSecondaryIdentifiers().size());
         replayMocks();
         service.readAndSaveTemplate(target);
         verifyMocks();
         assertEquals("Existing study has old identifier", newIdentifier, existingStudy.getAssignedIdentifier());
         assertNotNull(existingStudy.getLongTitle());
         assertEquals("Existing study doesn't have long title", longTitle, existingStudy.getLongTitle());
         assertEquals("Existing study doesn't have secondary identifiers", 1, existingStudy.getSecondaryIdentifiers().size());
         assertEquals("Existing study doesn't have correct secondary identifier",
                 identifier.getValue(), existingStudy.getSecondaryIdentifierValue("Type1"));
     }
 
     private void expectStudyLoadedFromXml(Study newStudy,InputStream target) {
         expect(studyXmlSerializer.readDocument(target)).andReturn(newStudy);
     }
 
     private void expectExistingStudyFound(Study existingStudy, String identifier) {
         expect(studyDao.getByAssignedIdentifier(identifier)).andReturn(existingStudy);
     }
 
     private void expectExistingStudy(Study existingStudy) {
         expect(studyService.getCompleteTemplateHistory(existingStudy)).andReturn(existingStudy);
         templateDevelopmentService.deleteDevelopmentAmendmentOnly(existingStudy);
     }
 
     private void expectDeltaAndChangesForTemplateIndex(Add add) {
         expect(deltaService.findChangeChild(add)).andReturn(add.getChild());
         expect(templateService.findChildren((Parent)add.getChild(), Period.class)).andReturn(new ArrayList<Period>());
         expect(templateService.findChildren((Parent)add.getChild(), StudySegment.class)).andReturn(new ArrayList<StudySegment>());
         expect(templateService.findChildren((Parent)add.getChild(), PlannedActivity.class)).andReturn(new ArrayList<PlannedActivity>());
     }
 
     private void expectForNewActivities(Add add, List<PlannedActivity> plannedActivities) {
         expect(deltaService.findChangeChild(add)).andReturn(add.getChild());
         expect(templateService.findChildren((Parent)add.getChild(), PlannedActivity.class)).andReturn(plannedActivities);
     }
 
     private void expectDaoForPlannedCalendar(Study study) {
         expect(daoFinder.expectDaoFor(PlannedCalendar.class, PlannedCalendarDao.class).
               getByGridId(study.getPlannedCalendar().getGridId())).andReturn(study.getPlannedCalendar());
     }
 
     private void expectNoGridIdConflicts(Study study) {
         checkGridIdConflictsForChildNode(study, false);
     }
 
     private void expectGridIdConflictsForChildNode(Study study) {
         checkGridIdConflictsForChildNode(study, true);
     }
 
     private void checkGridIdConflictsForChildNode(Study study, Boolean value) {
         expect(gridIdentifierResolver.resolveGridId(PlannedCalendar.class, study.getPlannedCalendar().getGridId())).andReturn(false);
         List<Amendment> amendments  = new ArrayList<Amendment>(study.getAmendmentsList());
         if (study.getDevelopmentAmendment() != null) {
            amendments.add(study.getDevelopmentAmendment());
         }  
         for (Amendment amendment : amendments) {
             for (Delta<?> delta : amendment.getDeltas()) {
                expect(gridIdentifierResolver.resolveGridId(delta.getClass(), delta.getGridId())).andReturn(false);
                 for (Change change : delta.getChanges()) {
                     expect(gridIdentifierResolver.resolveGridId(change.getClass(), change.getGridId())).andReturn(false);
                     Child child = ((Add)change).getChild();
                     expect(gridIdentifierResolver.resolveGridId(child.getClass(), child.getGridId())).andReturn(value);
                 }
             }
         }
     }
 
     private void expectGridIdConflictsCheck(MutableDomainObject node, Boolean value) {
         expect(gridIdentifierResolver.resolveGridId(node.getClass(), node.getGridId())).andReturn(value);
     }
 
     private Study createStudy() {
         Study study = createNamedInstance("study", Study.class);
         PlannedCalendar cal = new PlannedCalendar();
         cal.setGridId("cal");
         study.setPlannedCalendar(cal);
         Amendment amendment = Fixtures.createInDevelopmentAmendment("Amendment", DateTools.createDate(2007, Calendar.APRIL, 6), true);
         Delta<PlannedCalendar> delta = Delta.createDeltaFor(cal);
         delta.setGridId("pc-delta");
         Epoch epoch = Epoch.create("Treatment", "A", "B");
        assignIds(epoch, 3);
         Add add = Add.create(epoch, 0);
         add.setGridId("add");
         add.setChild(epoch);
         delta.addChange(add);
         amendment.addDelta(delta);
         study.setDevelopmentAmendment(amendment);
         return study;
     }
 
     private Study createReleasesStudy() {
         Study study = createStudy();
         study.setAmendment(study.getDevelopmentAmendment());
         study.setDevelopmentAmendment(null);
         for (Change change : study.getAmendment().getDeltas().get(0).getChanges()) {
             study.getPlannedCalendar().addEpoch(
                 (Epoch) ((Add) change).getChild()
             );
         }
         return study;
     }
 }
