 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.dao.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
 import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Hibernate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.*;
 
 @Transactional
 public class StudyService {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private ActivityDao activityDao;
     private StudyDao studyDao;
     private DeltaService deltaService;
     private TemplateService templateService;
     private PlannedCalendarDao plannedCalendarDao;
     private NowFactory nowFactory;
     private ScheduledActivityDao scheduledActivityDao;
     private NotificationService notificationService;
     private DaoFinder daoFinder;
 
     private static final String COPY = "copy";
 
     public void scheduleReconsent(final Study study, final Date startDate, final String details) throws Exception {
         List<StudySubjectAssignment> subjectAssignments = studyDao.getAssignmentsForStudy(study.getId());
         Activity reconsent = activityDao.getByName("Reconsent");
 
         for (StudySubjectAssignment assignment : subjectAssignments) {
             if (!assignment.isExpired()) {
                 ScheduledActivity upcomingScheduledActivity = getNextScheduledActivity(assignment
                     .getScheduledCalendar(), startDate);
                 if (upcomingScheduledActivity != null) {
                     ScheduledActivity reconsentEvent = new ScheduledActivity();
                     reconsentEvent.setIdealDate(upcomingScheduledActivity.getActualDate());
                     reconsentEvent.changeState(new Scheduled("Created From Reconsent", upcomingScheduledActivity
                         .getActualDate()));
                     reconsentEvent.setDetails(details);
                     reconsentEvent.setActivity(reconsent);
                     reconsentEvent.setSourceAmendment(study.getAmendment());
                     upcomingScheduledActivity.getScheduledStudySegment().addEvent(reconsentEvent);
                     scheduledActivityDao.save(reconsentEvent);
 
                     Notification notification = new Notification(reconsentEvent);
                     //FIXME:SAURABH this will send same email message multiple times to same subject coordinator.
                     // Update the logic here once the email message content is finalized.
                     assignment.addNotification(notification);
                     notificationService.notifyUsersForNewScheduleNotifications(notification);
 
                 }
             }
 
         }
         studyDao.save(study);
     }
 
     private ScheduledActivity getNextScheduledActivity(final ScheduledCalendar calendar, final Date startDate) {
         for (ScheduledStudySegment studySegment : calendar.getScheduledStudySegments()) {
             if (!studySegment.isComplete()) {
                 Map<Date, List<ScheduledActivity>> eventsByDate = studySegment.getActivitiesByDate();
                 for (Date date : eventsByDate.keySet()) {
                     List<ScheduledActivity> events = eventsByDate.get(date);
                     for (ScheduledActivity event : events) {
                         if ((event.getActualDate().after(startDate) || event.getActualDate().equals(startDate))
                             && ScheduledActivityMode.SCHEDULED == event.getCurrentState().getMode()) {
                             return event;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     // TODO: need replace all business uses of StudyDao#save with this method
     public void save(final Study study) {
         studyDao.save(study);
         if (study.getAmendment() != null) {
             for (Amendment amendment : study.getAmendmentsList()) {
                 deltaService.saveRevision(amendment);
             }
         }
         if (study.getDevelopmentAmendment() != null) {
             deltaService.saveRevision(study.getDevelopmentAmendment());
         }
     }
 
     public Study copy(final Study study, final Integer selectedAmendmentId) {
         if (study != null) {
             Amendment amendment = null;
             Study revisedStudy = study;
             if (selectedAmendmentId == null) {
                 amendment = study.getAmendment();
             } else if (study.getDevelopmentAmendment() != null && selectedAmendmentId.equals(study.getDevelopmentAmendment().getId())) {
                 amendment = study.getDevelopmentAmendment();
                 revisedStudy = deltaService.revise(study, amendment);
             }
             if (amendment == null) {
                 throw new StudyCalendarValidationException("Can not find amendment for given amendment id:" + selectedAmendmentId);
             }
             String newStudyName = this.getNewStudyNameForCopyingStudy(revisedStudy.getName());
             Map<Study, Set<Population>> newStudy = revisedStudy.copy(newStudyName);
             Study copiedStudy = newStudy.keySet().iterator().next();
             Set<Population> populationSet = newStudy.values().iterator().next();
             studyDao.save(copiedStudy);
             Set<Population> populations = new TreeSet<Population>();
             for(Population population:populationSet) {
                 Population copiedPopulation = new Population();
                 copiedPopulation.setAbbreviation(population.getAbbreviation());
                 copiedPopulation.setName(population.getName());
                 copiedPopulation.setStudy(null);
                 Change change = Add.create(copiedPopulation);
                 if (copiedStudy.getDevelopmentAmendment() != null) {
                     deltaService.updateRevisionForStudy(copiedStudy.getDevelopmentAmendment(),copiedStudy,change);
                     deltaService.saveRevision(copiedStudy.getDevelopmentAmendment());
                 }
                 copiedPopulation = (Population)((ChildrenChange)change).getChild();
                 populations.add(copiedPopulation);
 
             }
 
             List<Epoch> epochs  = new ArrayList<Epoch>();
             for (Delta delta:copiedStudy.getDevelopmentAmendment().getDeltas()) {
                 if (delta.getNode() instanceof PlannedCalendar) {
                     List<ChildrenChange> changes = delta.getChanges();
                     for (ChildrenChange change:changes  ) {
                         if ((ChangeAction.ADD).equals(change.getAction())) {
                                epochs.add((Epoch)change.getChild());
                         }
                     }
                 }
             }
 
             for (Epoch epoch:epochs) {
                 List<StudySegment> studySegments = epoch.getChildren();
                 for (StudySegment studySegment : studySegments) {
                     SortedSet<Period> periods = studySegment.getPeriods();
                     for (Period period : periods) {
                         List<PlannedActivity> plannedActivities = period.getChildren();
                         for (PlannedActivity plannedActivity : plannedActivities) {
                             plannedActivity.setPopulation(Population.findMatchingPopulationByAbbreviation(populations, plannedActivity.getPopulation()));
                         }
                     }
                 }
             }
             if (copiedStudy.getDevelopmentAmendment() != null) {
                deltaService.saveRevision(copiedStudy.getDevelopmentAmendment());
             }
             return copiedStudy;
         } else {
             throw new StudyCalendarValidationException("Can not find study");
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     public String getNewStudyNameForCopyingStudy(String studyName) {
         String templateName = studyName;
         templateName = templateName + " copy";
 
         final String searchString = templateName + "%";
         List<Study> studies = getStudyDao().searchStudiesByAssignedIdentifier(searchString);
         if (studies.size() == 0) {
             return templateName;
         }
 
         Collections.sort(studies, new CopiedStudyTemporaryNameComparator());
         Study study = studies.get(0);
         String name = study.getName();
         String numericPartSupposedly = name.substring(name.indexOf(COPY) + 4, name.length());
         int newNumber = 0;
         if (!StringUtils.isBlank(numericPartSupposedly)) {
             try {
                 newNumber = Integer.valueOf(numericPartSupposedly.trim()) + 1;
             } catch (NumberFormatException e) {
                 log.debug("Can't convert study's numeric string " + newNumber + " into int");
             }
         } else {
             newNumber = 2;
         }
         templateName = templateName + " " + newNumber;
 
         if (studyDao.getByAssignedIdentifier(templateName) != null) {
             return getNewStudyNameForCopyingStudy(templateName);
         }
         return templateName;
     }
 
     @SuppressWarnings({ "unchecked" })
     public String getNewStudyName() {
         String templateName = "[ABC 1000]";
 
         List<Study> studies = getStudyDao().searchStudiesByAssignedIdentifier("[ABC %]");
         List<Study> filteredStudies = new ArrayList<Study>();
         for(Study study: studies) {
             if (study.getAssignedIdentifier().matches("\\[ABC \\d\\d\\d\\d\\]")) {
                 filteredStudies.add(study);
             }
         }
         Collections.sort(filteredStudies, new StudyTemporaryNameComparator());
         if (filteredStudies.size() == 0) {
             return templateName;
         }
         Study study = filteredStudies.get(0);
         String studyName = study.getName();
         String numericPartSupposedly = studyName.substring(studyName.indexOf(" ") + 1, studyName.lastIndexOf("]"));
         int newNumber = 1000;
         try {
             newNumber = new Integer(numericPartSupposedly) + 1;
         } catch (NumberFormatException e) {
             log.debug("Can't convert study's numeric string " + newNumber + " into int");
         }
         templateName = "[ABC " + newNumber + "]";
         return templateName;
     }
 
     public Study saveStudyFor(PlanTreeNode<?> node) {
         node = templateService.findCurrentNode(node);
         Study study = templateService.findStudy(node);
         save(study);
         return study;
     }
 
     public Study getStudyByAssignedIdentifier(final String assignedIdentifier) {
         Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
         if (study == null) {
             return null;
         }
         Hibernate.initialize(study);
         List<StudySite> sites = study.getStudySites();
         Hibernate.initialize(sites);
         for (StudySite studySite : sites) {
             Hibernate.initialize(studySite);
             Hibernate.initialize(studySite.getCurrentApprovedAmendment());
         }
 
         PlannedCalendar plannedCalendar = study.getPlannedCalendar();
         if (plannedCalendar != null) {
             plannedCalendarDao.initialize(plannedCalendar);
         }
 
         List<Amendment> amendmentList = study.getAmendmentsList();
         for (Amendment amendment : amendmentList) {
             initializeAmendment(amendment);
         }
 
         Amendment developmentAmendment = study.getDevelopmentAmendment();
         initializeAmendment(developmentAmendment);
         return study;
     }
 
     @SuppressWarnings({ "unchecked" })
     private void initializeAmendment(Amendment amendment) {
         Hibernate.initialize(amendment);
         if (amendment != null) {
             List<Delta<?>> deltas = amendment.getDeltas();
             Hibernate.initialize(deltas);
             for (Delta delta : deltas) {
                 Hibernate.initialize(delta.getChanges());
                 List<Change> changes = delta.getChanges();
                 for (Change change : changes) {
                     Hibernate.initialize(change);
                     if (change instanceof ChildrenChange) {
                         ChildrenChange childrenChange = (ChildrenChange) change;
                         childrenChange.setChild((Child) getChild(childrenChange, ((Parent) delta.getNode()).childClass()));
                     }
                 }
             }
         }
     }
 
     // Methods to get child from child id
     private DomainObject getChild(ChildrenChange change, Class<? extends Changeable> childClass) {
         if (change.getChild() != null) {
             return change.getChild();
         } else {
             DomainObjectDao<?> dao = getDaoFinder().findDao(childClass);
             return dao.getById(change.getChildId());
         }
     }
 
     /**
      * Mutates the provided example study into a saveable form and then saves it.
      * Specifically, it takes the plan tree embodied in the plannedCalendar of the
      * example study and translates it into a development amendment which, when
      * released, will have the same structure as the example.
      *
      * @param example
      */
     public void createInDesignStudyFromExamplePlanTree(Study example) {
         example.setAmendment(null);
         Amendment newDev = new Amendment();
         newDev.setDate(nowFactory.getNow());
         newDev.setName(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
 
         PlannedCalendarDelta delta = new PlannedCalendarDelta(example.getPlannedCalendar());
         List<Epoch> epochs = new ArrayList<Epoch>(example.getPlannedCalendar().getEpochs());
         example.getPlannedCalendar().getEpochs().clear();
         for (Epoch epoch : epochs) {
             Add.create(epoch).mergeInto(delta, nowFactory.getNow());
         }
         newDev.addDelta(delta);
 
         example.setDevelopmentAmendment(newDev);
         save(example);
     }
 
     /**
      * Returns a transient version of <tt>source</tt> that has every {@link Add} in every
      * amendment resolved to the version of the added node as it existed when it was added.
      *
      * @param source
      * @return a new, transient {@link Study} instance
      */
     public Study getCompleteTemplateHistory(Study source) {
         List<Study> snapshots = new LinkedList<Study>();
         snapshots.add(source.transientClone());
         while (last(snapshots).getAmendment() != null && last(snapshots).getAmendment().getPreviousAmendment() != null) {
             snapshots.add(deltaService.amendToPreviousVersion(last(snapshots).transientClone()));
             resolveCurrentAdds(last(snapshots));
         }
         // The current amendment for each snapshot is now the version we want
         // to include with the complete history
         Study complete = source.transientClone();
         Amendment current = complete.getAmendment();
         for (int i = 1; i < snapshots.size(); i++) {
            current.setPreviousAmendment(snapshots.get(i).getAmendment());
         }
         return complete;
     }
 
     private void resolveCurrentAdds(Study study) {
         for (Delta<?> delta : study.getAmendment().getDeltas()) {
             for (Change change : delta.getChanges()) {
                 if (change.getAction() == ChangeAction.ADD) {
                     Add add = (Add) change;
                     Child childTemplate;
                     try {
                         childTemplate = (Child) ((Parent) delta.getNode()).childClass().newInstance();
                         childTemplate.setId(add.getChildId());
                     } catch (InstantiationException e) {
                         throw new StudyCalendarError("Uninstantiable child class", e);
                     } catch (IllegalAccessException e) {
                         throw new StudyCalendarError("Inaccessible child class", e);
                     }
                     add.setChild(templateService.findEquivalentChild(study, childTemplate));
                 }
             }
         }
     }
 
     private <T> T last(List<T> list) {
         if (list.isEmpty()) return null;
         else return list.get(list.size() - 1);
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setActivityDao(final ActivityDao activityDao) {
         this.activityDao = activityDao;
     }
 
     @Required
     public void setStudyDao(final StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
     public StudyDao getStudyDao() {
         return studyDao;
     }
 
     @Required
     public void setDeltaService(final DeltaService deltaService) {
         this.deltaService = deltaService;
     }
 
     @Required
     public void setTemplateService(final TemplateService templateService) {
         this.templateService = templateService;
     }
 
     @Required
     public void setPlannedCalendarDao(final PlannedCalendarDao plannedCalendarDao) {
         this.plannedCalendarDao = plannedCalendarDao;
     }
 
     @Required
     public void setNowFactory(NowFactory nowFactory) {
         this.nowFactory = nowFactory;
     }
 
     @Required
     public void setScheduledActivityDao(final ScheduledActivityDao scheduledActivityDao) {
         this.scheduledActivityDao = scheduledActivityDao;
     }
 
     @Required
     public void setNotificationService(final NotificationService notificationService) {
         this.notificationService = notificationService;
     }
 
     public DaoFinder getDaoFinder() {
         return daoFinder;
     }
 
     @Required
     public void setDaoFinder(DaoFinder daoFinder) {
         this.daoFinder = daoFinder;
     }
 
     ////// INNER CLASSES
 
     private class CopiedStudyTemporaryNameComparator implements Comparator<Study> {
         private final Logger logger = LoggerFactory.getLogger(getClass());
 
         /**
          * Compares the study name. Compares only  studies having name matches with copy * .
          *
          * @param study
          * @param anotherStudy
          * @return
          */
         public int compare(final Study study, final Study anotherStudy) {
             // String numericPartSupposedly = "";
             String name = study.getName();
             String anotherStudyName = anotherStudy.getName();
             if (name.indexOf(COPY) <= 0 || anotherStudyName.indexOf(COPY) <= 0) {
                 return 0;
             }
 
             String numericPartSupposedly = name.substring(name.indexOf(COPY) + 4, name.length());
             String anotherNumericPartSupposedly = anotherStudyName.substring(anotherStudyName.indexOf(COPY) + 4, anotherStudyName.length());
             Integer number = 0;
             Integer anotherNumber = 0;
             try {
 
                 number = !StringUtils.isBlank(numericPartSupposedly) ? Integer.valueOf(numericPartSupposedly.trim()) : 0;
             } catch (NumberFormatException e) {
                 logger.debug("error while comparing two stduies. first study name:" + study.getName() + " another study name:" + anotherStudy.getName() + ". error message:" + e.getMessage());
             }
             try {
 
                 anotherNumber = !StringUtils.isBlank(anotherNumericPartSupposedly) ? Integer.valueOf(anotherNumericPartSupposedly.trim()) : 0;
             } catch (NumberFormatException e) {
                 logger.debug("error while comparing two stduies. first study name:" + study.getName() + " another study name:" + anotherStudy.getName() + ". error message:" + e.getMessage());
             }
             return anotherNumber.compareTo(number);
         }
     }
 
     private static class StudyTemporaryNameComparator implements Comparator<Study> {
         private final Logger logger = LoggerFactory.getLogger(getClass());
 
         /**
          * Compares the study name. Compares only  studies having name matches with [ABC *] .
          *
          * @param study
          * @param anotherStudy
          * @return
          */
         public int compare(final Study study, final Study anotherStudy) {
             // String numericPartSupposedly = "";
             String name = study.getName();
             String anotherStudyName = anotherStudy.getName();
             if (name.indexOf("ABC") <= 0 || anotherStudyName.indexOf("ABC") <= 0) {
                 return 1;
             } else if (name.indexOf("]") <= 0 || anotherStudyName.indexOf("]") <= 0) {
                 return 1;
             }
             if (name.indexOf("[") < 0 || anotherStudyName.indexOf("[") < 0) {
                 return 1;
             }
 
             try {
                 String numericPartSupposedly = name.substring(name.indexOf(" ") + 1, name.lastIndexOf("]"));
                 String anotherNumericPartSupposedly = anotherStudyName.substring(anotherStudyName.indexOf(" ") + 1, anotherStudyName.lastIndexOf("]"));
                 Integer number = new Integer(numericPartSupposedly);
                 Integer anotherNumber = new Integer(anotherNumericPartSupposedly);
                 return anotherNumber.compareTo(number);
 
             } catch (NumberFormatException e) {
                 logger.debug("error while comparing two stduies. first study name:" + study.getName() + " another study name:" + anotherStudy.getName());
             }
 
             return 1;
         }
     }
 }
