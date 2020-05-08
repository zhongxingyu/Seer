 /*******************************************************************************
  * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Luis Silva - ACGHSync
  *     João Antunes - initial API and implementation
  ******************************************************************************/
 package pt.ist.maidSyncher.domain.github;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import jvstm.cps.ConsistencyPredicate;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.egit.github.core.Issue;
 import org.eclipse.egit.github.core.Label;
 import org.eclipse.egit.github.core.Milestone;
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.User;
 import org.eclipse.egit.github.core.service.IssueService;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
 import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
 import pt.ist.maidSyncher.api.activeCollab.ACTask;
 import pt.ist.maidSyncher.domain.MaidRoot;
 import pt.ist.maidSyncher.domain.SynchableObject;
 import pt.ist.maidSyncher.domain.activeCollab.ACProject;
 import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
 import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
 import pt.ist.maidSyncher.domain.dsi.DSIIssue;
 import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
 import pt.ist.maidSyncher.domain.dsi.DSIObject;
 import pt.ist.maidSyncher.domain.dsi.DSIProject;
 import pt.ist.maidSyncher.domain.dsi.DSIRepository;
 import pt.ist.maidSyncher.domain.dsi.DSISubTask;
 import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
 import pt.ist.maidSyncher.domain.exceptions.SyncEventIncogruenceBetweenOriginAndDestination;
 import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncEvent;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 
 public class GHIssue extends GHIssue_Base {
     private static final Logger LOGGER = LoggerFactory.getLogger(GHIssue.class);
 
     public final static String STATE_CLOSED = "closed";
     public final static String STATE_OPEN = "open";
 
     //The PropertyDescriptor s of this issue
     public final static String DSC_STATE = "state";
     public final static String DSC_LABELS = "labels";
     public final static String DSC_NUMBER = "number";
     public final static String DSC_BODY = "body";
     public final static String DSC_TITLE = "title";
     public final static String DSC_UPDATED_AT = "updatedAt";
     public final static String DSC_HTML_URL = "htmlUrl";
     public final static String DSC_MILESTONE = "milestone";
 
     public final static String MOVED_TO_PREFIX = "Moved to ";
 
     public GHIssue() {
         super();
         MaidRoot.getInstance().addGhIssues(this);
     }
 
     static GHIssue process(Issue issue) {
         return process(issue, false);
     }
 
     static GHIssue process(Issue issue, boolean skipSync) {
         checkNotNull(issue);
         MaidRoot maidRoot = MaidRoot.getInstance();
         return (GHIssue) findOrCreateAndProccess(issue, GHIssue.class, maidRoot.getGhIssuesSet(), skipSync);
     }
 
     public static GHIssue process(Issue issue, Repository repository) {
         return process(issue, repository, false);
 
     }
 
     public static GHIssue process(Issue issue, Repository repository, boolean skipSync) {
         checkNotNull(repository);
         //let's first take care of the issue, and then assign it the repository
         GHIssue ghIssue = process(issue, skipSync);
         GHRepository ghRepository = GHRepository.process(repository, skipSync);
 
         ghIssue.setRepository(ghRepository);
 
         return ghIssue;
 
     }
 
     public static GHIssue process(Issue issue, GHRepository ghRepository, boolean skipSync) {
         checkNotNull(ghRepository);
         //let's first take care of the issue, and then assign it the repository
         GHIssue ghIssue = process(issue, skipSync);
 
         ghIssue.setRepository(ghRepository);
 
         return ghIssue;
 
     }
 
     protected static List<Label> addLabelsToUse(List<Label> currentlyUsedLabels, Label... labelsToAdd) {
         List<Label> labelsToReturn = null;
         if (currentlyUsedLabels == null) {
             labelsToReturn = new ArrayList<Label>();
         } else {
             labelsToReturn = currentlyUsedLabels;
         }
         for (Label labelToAdd : labelsToAdd) {
             labelsToReturn.add(labelToAdd);
         }
         return labelsToReturn;
     }
 
     @Override
     public void copyPropertiesTo(Object dest) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
     TaskNotVisibleException {
         super.copyPropertiesTo(dest);
 
         //let's take care of the non simple properties i.e., Milestone and Labels
         Issue issue = (Issue) dest;
 
         if (getMilestone() != null) {
             //let's get it to the issue
             GHMilestone ghMilestone = getMilestone();
             Milestone newMilestone = new Milestone();
             //we only need the number in reality
             newMilestone.setNumber(ghMilestone.getNumber());
             issue.setMilestone(newMilestone);
         }
 
         List<Label> labelsToBeCopied = new ArrayList<>();
         for (GHLabel ghLabel : getLabelsSet()) {
             Label newLabel = new Label();
             ghLabel.copyPropertiesTo(newLabel);
             labelsToBeCopied.add(newLabel);
         }
 
         if (labelsToBeCopied.isEmpty() == false)
             //because there is a different behaviour
             //if the issue.getLabels returns null
             //or an empty list
             issue.setLabels(labelsToBeCopied);
 
     }
 
     @Override
     public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
     NoSuchMethodException {
         checkNotNull(orig);
         checkArgument(orig instanceof Issue, "provided object must be an instance of " + Issue.class.getName());
 
         Set<String> changedPropertyDescriptors = new HashSet<>(super.copyPropertiesFrom(orig));
 
         Issue issue = (Issue) orig;
         //now let's take care of the relations with the other objects
         //like Milestone and Label
         Milestone milestone = issue.getMilestone();
         if (milestone != null) {
             GHMilestone ghMilestone = GHMilestone.process(milestone);
             if (!ObjectUtils.equals(getMilestone(), ghMilestone))
                 changedPropertyDescriptors.add(getPropertyDescriptorNameAndCheckItExists(issue, "milestone"));
             setMilestone(ghMilestone);
         }
 
         Set<GHLabel> ghOldLabels = new HashSet<GHLabel>(getLabelsSet());
         Set<GHLabel> newGHLabels = new HashSet<GHLabel>();
         if (issue.getLabels() != null) {
             for (Label label : issue.getLabels()) {
                 GHLabel ghLabel = GHLabel.process(label);
                 newGHLabels.add(ghLabel);
             }
             if (!ObjectUtils.equals(ghOldLabels, newGHLabels))
                 changedPropertyDescriptors.add(getPropertyDescriptorNameAndCheckItExists(issue, "labels"));
             for (GHLabel ghLabel : getLabelsSet()) {
                 removeLabels(ghLabel);
             }
             for (GHLabel ghLabel : newGHLabels) {
                 addLabels(ghLabel);
             }
         }
 
         User assignee = issue.getAssignee();
         GHUser ghNewAssignee = null;
         if (assignee != null)
             ghNewAssignee = GHUser.process(assignee);
         GHUser ghOldAssignee = getAssignee();
         if (!ObjectUtils.equals(ghNewAssignee, ghOldAssignee))
             changedPropertyDescriptors.add(getPropertyDescriptorNameAndCheckItExists(issue, "assignee"));
         setAssignee(ghNewAssignee);
         return changedPropertyDescriptors;
     }
 
     @Override
     public DateTime getUpdatedAtDate() {
         return getUpdatedAt() == null ? getCreatedAt() : getUpdatedAt();
     }
 
     @ConsistencyPredicate
     private boolean checkDSIObjectMultiplicity() {
         //we cannot be both an issue and a subtask
         if (getDsiObjectIssue() != null && getDsiObjectSubTask() != null)
 
             return false;
         return true;
     }
 
     @Override
     public DSIObject getDSIObject() {
         if (getDsiObjectIssue() != null)
             return getDsiObjectIssue();
         else
             return getDsiObjectSubTask();
     }
 
     @Override
     public DSIObject findOrCreateDSIObject() {
         //let's either make a DSISubTask or a DSIIssue
         //depending if we have a @subtask on the description or not
         DSIObject dsiObjectToReturn = getDSIObject();
         if (dsiObjectToReturn == null) {
             //we cannot create ACSubTasks through GH without using a comment on an
             //already existing issue, so, we assume it's a Issue
             dsiObjectToReturn = new DSIIssue();
             setDsiObjectIssue((DSIIssue) dsiObjectToReturn);
         }
         return dsiObjectToReturn;
     }
 
     @Override
     public SyncActionWrapper sync(SyncEvent syncEvent) {
         SyncActionWrapper syncActionWrapperToReturn = null;
         if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
             //then we should need to create a GHIssue, let's just make sure that's correct
             if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.GITHUB))
                 throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());
 
             //if we are creating an artefact on AC side, it mustn't be a SubTask, it's a Task
             ACTask newAcTask = new ACTask();
             syncActionWrapperToReturn = syncCreateEvent(newAcTask, syncEvent);
 
         } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
             //let's retrieve the existing ACTask or ACSubTask and use it to prefill the api object
             DSIObject dsiIssueOrSubTask = getDSIObject();
             if (dsiIssueOrSubTask instanceof DSIIssue) {
                 DSIIssue dsiIssue = (DSIIssue) dsiIssueOrSubTask;
                 pt.ist.maidSyncher.domain.activeCollab.ACTask acDomainTask = dsiIssue.getAcTask();
                 ACTask newAcTask = new ACTask();
                 try {
                     acDomainTask.copyPropertiesTo(newAcTask);
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                     throw new SyncActionError("Error trying to prefill the properties from the ACTask to the bean.", syncEvent, e);
                 }
 
                 syncActionWrapperToReturn = syncUpdateTaskEvent(newAcTask, dsiIssue, syncEvent);
 
             } else if (dsiIssueOrSubTask instanceof DSISubTask) {
                 DSISubTask dsiSubTask = (DSISubTask) dsiIssueOrSubTask;
                 pt.ist.maidSyncher.domain.activeCollab.ACSubTask acDomainSubTask = dsiSubTask.getAcSubTask();
                 ACSubTask newAcSubTask = new ACSubTask();
                 try {
                     acDomainSubTask.copyPropertiesTo(newAcSubTask);
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                     throw new SyncActionError("Error trying to prefill the properties from the ACSubTask to the bean.",
                             syncEvent, e);
                 }
 
                 syncActionWrapperToReturn = syncUpdateSubTaskEvent(newAcSubTask, dsiSubTask, syncEvent);
 
             }
         }
 
         else {
             LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
             syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
         }
 
         return syncActionWrapperToReturn;
     }
 
     /**
      * Enforces only one Label that corresponds to an ACProject.
      * <strong>Makes changes to the GH repository</strong>
      * 
      * @throws IOException
      * @{@link Deprecated} - not the behavior used anymore
      */
     @Deprecated
     private void validateAndCorrectLabels() throws IOException {
         GHLabel labelFound = null;
         Set<GHLabel> labelsToRemove = new HashSet<>();
         for (GHLabel ghLabel : getLabelsSet()) {
             if (ghLabel.getDSIObject() != null) {
                 if (labelFound != null) {
                     labelsToRemove.add(ghLabel);
                 } else {
                     labelFound = ghLabel;
                 }
             }
         }
 
         //let us enforce the labels then
         IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
         Issue issueToRemoveLabelsFrom = issueService.getIssue(getRepository(), getNumber());
         for (Label currentLabel : issueToRemoveLabelsFrom.getLabels()) {
             final String currentLabelName = currentLabel.getName();
             boolean removeThisOne = Iterables.any(labelsToRemove, new Predicate<GHLabel>() {
                 @Override
                 public boolean apply(GHLabel input) {
                     if (input == null)
                         return false;
                     return input.getName().equalsIgnoreCase(currentLabelName);
 
                 }
 
             });
             if (removeThisOne) {
                 issueToRemoveLabelsFrom.getLabels().remove(currentLabel);
             }
         }
 
         //let us communicate the changes
         Issue newlyEditedIssue = issueService.editIssue(getRepository(), issueToRemoveLabelsFrom);
         try {
             copyPropertiesFrom(newlyEditedIssue);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
             throw new Error(e);
         }
 
     }
 
     private SyncActionWrapper syncUpdateSubTaskEvent(ACSubTask newAcSubTask, DSISubTask dsiSubTask, final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         boolean auxChangedLabels = false;
         boolean auxChangedState = false;
         boolean auxChangedBody = false;
         boolean auxChangedTitle = false;
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_ID:
             case DSC_URL:
             case DSC_HTML_URL:
             case DSC_CREATED_AT:
             case DSC_UPDATED_AT:
             case DSC_NUMBER:
             case DSC_MILESTONE:
                 //changes to this milestone don't reflect
                 //on the other side, because we are only a subtask
                 break; //the ones above, there'se no sense in changing anything
             case DSC_LABELS:
                 //seen that we were a subtask, if we edited the project label,
                 //we need to create an ACTask on another ACProject
                 auxChangedLabels = true;
                 break;
             case DSC_STATE:
                 //we might have to close/open the Task/SubTask on the other side
                 auxChangedState = true;
                 break;
             case DSC_BODY:
                 //let us change the body on the other side as well
                 auxChangedBody = true;
                 break;
             case DSC_TITLE:
                 //change the name on the other side
                 auxChangedTitle = true;
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
 
             }
         }
 
         final boolean changedLabels = auxChangedLabels;
         final boolean changedState = auxChangedState;
         final boolean changedBody = auxChangedBody;
         final boolean changedTitle = auxChangedTitle;
 
         return new SyncActionWrapper<SynchableObject>() {
 
             @Override
             public Collection<SynchableObject> sync() throws IOException {
                 Issue newIssue = null;
                 ACSubTask acSubTaskToEdit = null;
 
                 Set<SynchableObject> synchableObjectsToReturn = new HashSet<>();
                 if (changedLabels) {
                     //we do nothing here, we only sync if it's the parent that changed
                 }
                 if (changedState) {
                     //then we need to apply the state to the corresponding ACSubTask
                     acSubTaskToEdit = getNewPrefilledACSubTask(acSubTaskToEdit);
                     switch (getState()) {
                     case STATE_CLOSED:
                         acSubTaskToEdit.setComplete(true);
                         break;
                     case STATE_OPEN:
                         acSubTaskToEdit.setComplete(false);
                         break;
                     }
 
                 }
                 if (changedBody) {
                     //we are a subtask, we must ensure that we have the relation to the task in the beginning
                     if (StringUtils.startsWith(getBody(), getSubTaskBodyPrefix()) == false) {
                         //then we need to correct that!!
                         String newBody = applySubTaskBodyPrefix(getBody());
                         newIssue = getNewPrefilledIssue(newIssue);
                         newIssue.setBody(newBody);
                     }
 
                 }
                 if (changedTitle) {
                     //let's update the title on the other end
                     acSubTaskToEdit = getNewPrefilledACSubTask(acSubTaskToEdit);
                     acSubTaskToEdit.setName(getTitle());
                 }
 
                 if (newIssue != null) {
                     //we should apply the changes
                     IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                     Issue editedIssue = issueService.editIssue(getRepository(), newIssue);
                     GHIssue.process(editedIssue, true);
 
                 }
 
                 if (acSubTaskToEdit != null) {
                     //we should apply the changes
                     String url = getDsiObjectSubTask().getAcSubTask().getUrl();
                     ACSubTask newlyUpdatedSubTask = acSubTaskToEdit.update(url);
                     try {
                         getDsiObjectSubTask().getAcSubTask().copyPropertiesFrom(newlyUpdatedSubTask);
                     } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                         throw new Error(e);
                     }
                 }
 
                 return synchableObjectsToReturn;
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 return syncEvent;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 return Collections.singleton(getDSIObject());
             }
 
             @Override
             public Set<Class> getSyncDependedTypesOfDSIObjects() {
                 Set<Class> classesDependedOn = new HashSet<>();
                 classesDependedOn.add(GHLabel.class);
                 classesDependedOn.add(pt.ist.maidSyncher.domain.activeCollab.ACSubTask.class);
                 return classesDependedOn;
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             }
         };
     }
 
     public final static String SUB_TASK_BODY_PREFIX = "Subtask of #";
 
     private String getSubTaskBodyPrefix() {
         DSISubTask dsiSubTask = (DSISubTask) getDSIObject();
         return SUB_TASK_BODY_PREFIX + dsiSubTask.getParentIssue().getGhIssue().getNumber();
     }
 
     private static String getSubTaskBodyPrefix(GHIssue ghParentIssue) {
         return SUB_TASK_BODY_PREFIX + ghParentIssue.getNumber();
     }
 
     /**
      * 
      * @param body
      * @return A string with the getSubTaskBodyPrefix in the beginning, and the rest of the body there.
      *         If we had the subTaskBodyPrefix in any other place of the body, it moves it to the beginning
      */
     public String applySubTaskBodyPrefix(String body) {
         String subTaskBodyPrefixString = getSubTaskBodyPrefix();
         String newBody = body;
 
         //let's try to find it
         if (StringUtils.contains(body, subTaskBodyPrefixString)) {
             //let's remove it.
             newBody = StringUtils.remove(body, subTaskBodyPrefixString);
         }
 
         newBody = subTaskBodyPrefixString + " " + newBody;
 
         return newBody;
     }
 
     /**
      * 
      * @param body
      * @return A string with the getSubTaskBodyPrefix in the beginning, and the rest of the body there.
      *         If we had the subTaskBodyPrefix in any other place of the body, it moves it to the beginning
      */
     public static String applySubTaskBodyPrefix(String body, GHIssue parentGHIssue) {
         String subTaskBodyPrefixString = getSubTaskBodyPrefix(parentGHIssue);
         String newBody = body;
 
         //let's try to find it
         if (StringUtils.contains(body, subTaskBodyPrefixString)) {
             //let's remove it.
             newBody = StringUtils.remove(body, subTaskBodyPrefixString);
         }
 
         newBody = subTaskBodyPrefixString + " " + newBody;
 
         return newBody;
     }
 
     private Issue getNewPrefilledIssue(Issue newIssueToReuse) {
         if (newIssueToReuse != null)
             return newIssueToReuse;
         Issue newIssue = new Issue();
         try {
             copyPropertiesTo(newIssue);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
             throw new Error("Error trying to prefill an Issue.", e);
         }
         return newIssue;
     }
 
     /**
      * 
      * @param newACTaskToReuse
      * @return the newACTaskToReuse if it isn't null, or a newly prefilled one from the dsiIssue.getAcTask()
      */
     private ACTask getNewPrefilledACTask(ACTask newACTaskToReuse) {
         if (newACTaskToReuse != null)
             return newACTaskToReuse;
         checkNotNull(getDSIObject());
         DSIIssue dsiIssue = (DSIIssue) getDSIObject();
         checkNotNull(dsiIssue);
         pt.ist.maidSyncher.domain.activeCollab.ACTask domainAcTask = dsiIssue.getAcTask();
         checkNotNull(domainAcTask);
         ACTask newAcTask = new ACTask();
         try {
             domainAcTask.copyPropertiesTo(newAcTask);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
             throw new Error("Error trying to prefill an Issue.", e);
         }
         return newAcTask;
 
     }
 
     private ACSubTask getNewPrefilledACSubTask(ACSubTask newACSubTaskToReuse) {
         if (newACSubTaskToReuse != null)
             return newACSubTaskToReuse;
         checkNotNull(getDSIObject());
         DSISubTask dsiSubTask = (DSISubTask) getDSIObject();
         checkNotNull(dsiSubTask);
         pt.ist.maidSyncher.domain.activeCollab.ACSubTask acSubTask = dsiSubTask.getAcSubTask();
         checkNotNull(acSubTask);
         ACSubTask newACSubTask = new ACSubTask();
         try {
             acSubTask.copyPropertiesTo(newACSubTask);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
             throw new Error("Error trying to prefill an Issue.", e);
         }
         return newACSubTask;
     }
 
     //Not used, TODO remove or use it
 //    private void updateSubTaskLabels(Set<SynchableObject> synchableObjectsToReturn) {
 //        //this will take care of incorrect uses of the labels
 //        validateAndCorrectLabels();
 //        //now let's assert if it changed the project label or not
 //        Optional<GHLabel> optionalGHLabel = Iterables.tryFind(getLabels(), new Predicate<GHLabel>() {
 //            @Override
 //            public boolean apply(GHLabel input) {
 //                if (input == null)
 //                    return false;
 //                DSIProject dsiProject = (DSIProject) input.getDSIObject();
 //                if (dsiProject != null && dsiProject.getAcProject() != null)
 //                    return true;
 //                return false;
 //            }
 //        });
 //        //let us check if we changed projects or not
 //
 //        ACProject projectLabelProject = ((DSIProject) projectLabel.getDSIObject()).getAcProject();
 //
 //    }
 
 //    private boolean projectChanged(Optional<GHLabel> optionalGHLabelWithAssociatedProject, ACProject currentProject) {
 //        if (optionalGHLabelWithAssociatedProject.isPresent() == false) {
 //            return ObjectUtils.equals(null, currentProject) == false;
 //        } else {
 //            ACProject acProject = ((DSIProject) optionalGHLabelWithAssociatedProject.get().getDSIObject()).getAcProject();
 //        }
 //
 //    }
 
     private SyncActionWrapper syncUpdateTaskEvent(ACTask newAcTask, DSIIssue dsiIssue, final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         boolean auxChangedMilestones = false;
         boolean auxChangedLabels = false;
         boolean auxChangedState = false;
         boolean auxChangedBody = false;
         boolean auxChangedTitle = false;
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_ID:
             case DSC_URL:
             case DSC_HTML_URL:
             case DSC_CREATED_AT:
             case DSC_UPDATED_AT:
             case DSC_NUMBER:
                 break; //the ones above, there'se no sense in changing anything
 
             case DSC_MILESTONE:
                 //we need to create/change that milestone on the AC side
                 auxChangedMilestones = true;
 
                 break;
             case DSC_LABELS:
                 //seen that we were a subtask, if we edited the project label,
                 //we need to create an ACTask on another ACProject
                 auxChangedLabels = true;
                 break;
             case DSC_STATE:
                 //we might have to close/open the Task/SubTask on the other side
                 auxChangedState = true;
                 break;
             case DSC_BODY:
                 //let us change the body on the other side as well
                 auxChangedBody = true;
                 break;
             case DSC_TITLE:
                 //change the name on the other side
                 auxChangedTitle = true;
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
 
             }
         }
         final boolean changedMilestones = auxChangedMilestones;
         final boolean changedLabels = auxChangedLabels;
         final boolean changedState = auxChangedState;
         final boolean changedBody = auxChangedBody;
         final boolean changedTitle = auxChangedTitle;
         return new SyncActionWrapper<SynchableObject>() {
 
             @Override
             public Collection<SynchableObject> sync() throws IOException {
                 Set<SynchableObject> synchableObjectsToReturn = new HashSet<>();
                 ACTask acTaskToEdit = null;
                 DSIIssue dsiIssue = (DSIIssue) getDSIObject();
                 if (changedLabels) {
                     //let's assert the project label - if none is found, or more than one is found, let's assume the default project is to be used
                     ACProject acProject = getProjectToUsedBasedOnCurrentLabels();
                     ACProject currentACProject = dsiIssue.getAcTask().getProject();
                     if (ObjectUtils.notEqual(currentACProject, acProject)) {
                         //let's use the /move-to-project on the AC side
                         ACTask.moveTo(dsiIssue.getAcTask().getId(), currentACProject.getId(), acProject.getId());
                         //assuming all went well, we need to update this ACTask's projectId
                         dsiIssue.getAcTask().setProject(acProject);
                         DSIProject dsiProject = acProject.getDsiObjectProject();
                         dsiIssue.setProject(dsiProject);
 
                     }
 
                 }
                 if (changedMilestones) {
                     if (getMilestone() != null) {
 
                         //let us change the milestone on the other side.
                         //if the corresponding ACMilestone doesn't exist, reuse/create it
 
                         //let's try to find one with the name to use
                         ACProject acProject = getProjectToUsedBasedOnCurrentLabels();
                         pt.ist.maidSyncher.domain.activeCollab.ACMilestone milestoneToUse =
                                 pt.ist.maidSyncher.domain.activeCollab.ACMilestone.findMilestone(acProject, getMilestone()
                                         .getTitle());
                         if (milestoneToUse == null) {
                             //we have to create it
                             ACMilestone acMilestoneToCreate = new ACMilestone();
                             acMilestoneToCreate.setName(getMilestone().getTitle());
                             acMilestoneToCreate.setBody(getMilestone().getDescription());
                             acMilestoneToCreate.setDueOn(getMilestone().getDueOn().toDate());
                             acMilestoneToCreate.setProjectId(acProject.getId());
                             ACMilestone newlyCreatedMilestone = ACMilestone.create(acMilestoneToCreate);
                             milestoneToUse =
                                     pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(newlyCreatedMilestone, true);
                         }
 
                         acTaskToEdit = getNewPrefilledACTask(acTaskToEdit);
                         acTaskToEdit.setMilestoneId((int) milestoneToUse.getId());
 
                     }
 
                 }
                 if (changedState) {
                     //update the state on the other side
                     acTaskToEdit = getNewPrefilledACTask(acTaskToEdit);
                     acTaskToEdit.setComplete(getState().equalsIgnoreCase(STATE_CLOSED));
 
                 }
                 if (changedBody) {
                     //update the body on the other side
                     acTaskToEdit = getNewPrefilledACTask(acTaskToEdit);
                     acTaskToEdit.setBody(getBodyHtml());
 
                 }
                 if (changedTitle) {
                     //update the title on the other side
                     acTaskToEdit = getNewPrefilledACTask(acTaskToEdit);
                     acTaskToEdit.setName(getTitle());
 
                 }
 
                 if (acTaskToEdit != null) {
                     //let's edit it
 
                     //get the base url
                     pt.ist.maidSyncher.domain.activeCollab.ACTask acTask = dsiIssue.getAcTask();
                     ACTask updatedAcTask = acTaskToEdit.update(acTask.getUrl());
                     pt.ist.maidSyncher.domain.activeCollab.ACTask.process(updatedAcTask, acTask.getProject().getId(), true);
 
                 }
 
                 return synchableObjectsToReturn;
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 return syncEvent;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 return Collections.emptyList();
             }
 
             @Override
             public Set<Class> getSyncDependedTypesOfDSIObjects() {
                 Set<Class> classesDependedOn = new HashSet();
                 classesDependedOn.add(ACProject.class);
                 classesDependedOn.add(ACMilestone.class);
                 classesDependedOn.add(GHMilestone.class);
                 classesDependedOn.add(GHLabel.class);
                 return Collections.singleton((Class) ACProject.class);
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             }
         };
     }
 
     private ACProject getProjectToUsedBasedOnCurrentLabels() {
 
         Collection<GHLabel> appliableLabels = Collections2.filter(getLabelsSet(), new Predicate<GHLabel>() {
             @Override
             public boolean apply(GHLabel input) {
                 if (input == null)
                     return false;
                 DSIProject dsiProject = (DSIProject) input.getDSIObject();
                 if (dsiProject == null)
                     return false;
                 if (dsiProject.getAcProject() != null)
                     return true;
                 return false;
             }
         });
         //let's try to search on the current labels
         if (appliableLabels.size() != 1) {
             return ((DSIRepository) getRepository().getDSIObject()).getDefaultProject();
         } else
             return ((DSIProject) appliableLabels.iterator().next().getDSIObject()).getAcProject();
     }
 
     private SyncActionWrapper syncCreateEvent(final ACTask newAcTask, final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_ID:
             case DSC_URL:
             case DSC_HTML_URL:
             case DSC_CREATED_AT:
             case DSC_UPDATED_AT:
             case DSC_NUMBER:
 
             case DSC_MILESTONE:
             case DSC_LABELS:
             case DSC_STATE:
             case DSC_BODY:
             case DSC_TITLE:
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
 
             }
         }
 
         //if we have a GHLabel of 'deleted' let's not do anything
         if (GHLabel.containsDeletedLabel(getLabelsSet())) {
             return new EmptySyncActionWrapper(syncEvent);
         } else {
 
             SyncActionWrapper toReturnActionWrapper = new SyncActionWrapper() {
 
                 @Override
                 public Collection<SynchableObject> sync() throws IOException {
 
                     //let's fill appropriately the newTask
                     newAcTask.setName(getTitle());
                     newAcTask.setBody(getBodyHtml());
                     newAcTask.setVisibility(true);
 
                     if (getState().equals(STATE_CLOSED)) {
                         newAcTask.setComplete(true);
                     } else if (getState().equals(STATE_OPEN)) {
                         newAcTask.setComplete(false);
                     }
 
                     DSIRepository dsiRepository = (DSIRepository) getRepository().getDSIObject(); //depended on
                     //we will also need the project on the AC side
                     DSIIssue dsiIssue = (DSIIssue) getDSIObject(); /*seen that we are creating based on something
                                                                    from GH, we are creating an issue, thus the cast */
                     //let's find out which project we should be creating the task on
                     //in this version, let's use the default one always.
                     //TODO use the labels to create the task in various projects GHIssue #10
 
                     final ACProject acProject = dsiRepository.getDefaultProject();
                     //now, the harder stuff, Milestones, and Category [aka repository]
 
                     //let's see if the Milestone exists
                     if (getMilestone() != null) {
 
                         DSIMilestone dsiMilestone = (DSIMilestone) getMilestone().getDSIObject(); //depended on
                         if (dsiMilestone == null || dsiMilestone.getAcMilestone(acProject) == null) {
                             ACMilestone acMilestoneToCreate =
                                     getMilestone().getACCorrespondingPreliminarObject(acProject.getId());
                             pt.ist.maidSyncher.domain.activeCollab.ACMilestone newMilestone =
                                     pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(
                                             ACMilestone.create(acMilestoneToCreate), true);
                             if (dsiMilestone == null) {
                                 dsiMilestone = (DSIMilestone) getMilestone().findOrCreateDSIObject();
                             }
                             dsiMilestone.addAcMilestones(newMilestone);
 
                         } else {
                             pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestone =
                                     dsiMilestone.getAcMilestone(acProject);
                             //TODO BUG? shouldn't we check if acMilestone == null?!
                             if (ObjectUtils.equals(acMilestone.getProject(), acProject) == false) {
                                 //then this milestone should be moved,or copied to the new project
                                 //let's see which one we should do
                                 boolean hasOtherTasks = acMilestone.getTasksSet().isEmpty() == false;
 
                                 if (hasOtherTasks) {
                                     //let's copy it
                                     pt.ist.maidSyncher.domain.activeCollab.ACMilestone newMilestone =
                                             pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(ACMilestone.copyTo(
                                                     acMilestone.getId(), acMilestone.getProject().getId(), acProject.getId()),
                                                     true);
                                     dsiMilestone.addAcMilestones(newMilestone);
                                 } else {
                                     //let's move it
                                     ACMilestone.moveTo(acMilestone.getId(), acMilestone.getProject().getId(), acProject.getId());
                                     //let's make the move internally without fuss
                                     acMilestone.setProject(acProject);
                                 }
 
                             }
                         }
                         pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestone = dsiMilestone.getAcMilestone(acProject);
                         long id = acMilestone.getId();
                         newAcTask.setMilestoneId((int) id);
                     }
 
                     ACTaskCategory acTaskCategory = dsiRepository.getACTaskCategoryFor(acProject);
 
                     newAcTask.setCategoryId((int) acTaskCategory.getId());
 
                     ACTask newlyCreatedTask = ACTask.createTask(newAcTask, acProject.getId());
                     pt.ist.maidSyncher.domain.activeCollab.ACTask newlyCreatedDomainAcTask =
                             pt.ist.maidSyncher.domain.activeCollab.ACTask.process(newlyCreatedTask, acProject.getId(), true);
 
                     dsiIssue.setAcTask(newlyCreatedDomainAcTask);
                     return Collections.singleton((SynchableObject) newlyCreatedDomainAcTask);
 
                 }
 
                 @Override
                 public Collection<DSIObject> getSyncDependedDSIObjects() {
                     Set<DSIObject> dsiObjectsDependedOn = new HashSet<>();
 //                dsiObjectsDependedOn.add(getMilestone().getDSIObject());
                     dsiObjectsDependedOn.add(getRepository().getDSIObject());
                     dsiObjectsDependedOn.add(((DSIRepository) getRepository().getDSIObject()).getDefaultProject()
                             .getDsiObjectProject());
                     dsiObjectsDependedOn.add(null); //null - means all of the GHLabel's should be synched
                     return dsiObjectsDependedOn;
                 }
 
                 @Override
                 public SyncEvent getOriginatingSyncEvent() {
                     return syncEvent;
                 }
 
                 @Override
                 public Set<Class> getSyncDependedTypesOfDSIObjects() {
                     Set<Class> dependedOnDSIClasses = new HashSet<>();
                     dependedOnDSIClasses.add(DSIMilestone.class);
                     dependedOnDSIClasses.add(DSIProject.class);
                     dependedOnDSIClasses.add(DSIRepository.class);
                     return dependedOnDSIClasses;
                 }
 
                 @Override
                 public Collection getPropertyDescriptorNamesTicked() {
                     return tickedDescriptors;
                 }
             };
             return toReturnActionWrapper;
         }
 
     }
 
 }
