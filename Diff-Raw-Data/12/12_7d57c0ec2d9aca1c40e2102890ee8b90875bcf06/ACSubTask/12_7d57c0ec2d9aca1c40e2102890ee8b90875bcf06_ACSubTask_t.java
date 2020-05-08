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
 package pt.ist.maidSyncher.domain.activeCollab;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.eclipse.egit.github.core.Issue;
 import org.eclipse.egit.github.core.service.IssueService;
 
 import pt.ist.maidSyncher.api.activeCollab.ACTask;
 import pt.ist.maidSyncher.domain.MaidRoot;
 import pt.ist.maidSyncher.domain.SynchableObject;
 import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
 import pt.ist.maidSyncher.domain.dsi.DSIIssue;
 import pt.ist.maidSyncher.domain.dsi.DSIObject;
 import pt.ist.maidSyncher.domain.dsi.DSIRepository;
 import pt.ist.maidSyncher.domain.dsi.DSISubTask;
 import pt.ist.maidSyncher.domain.exceptions.SyncEventIncogruenceBetweenOriginAndDestination;
 import pt.ist.maidSyncher.domain.github.GHIssue;
 import pt.ist.maidSyncher.domain.github.GHRepository;
 import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncEvent;
 
 public class ACSubTask extends ACSubTask_Base {
     private final static String DSC_PARENTID = "parentId";
 
     public ACSubTask() {
         super();
     }
 
     private static ACSubTask process(pt.ist.maidSyncher.api.activeCollab.ACSubTask acSubTask) {
         checkNotNull(acSubTask);
         return (ACSubTask) findOrCreateAndProccess(acSubTask, ACSubTask.class, MaidRoot.getInstance().getAcObjectsSet());
     }
 
     @Override
     public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
     NoSuchMethodException, TaskNotVisibleException {
         HashSet<String> propertyDescriptorsToReturn = new HashSet<>();
         propertyDescriptorsToReturn.addAll(super.copyPropertiesFrom(orig));
         pt.ist.maidSyncher.api.activeCollab.ACSubTask acSubTask = (pt.ist.maidSyncher.api.activeCollab.ACSubTask) orig;
         if (acSubTask.getParentClass().equals(ACTask.CLASS_VALUE)) {
             pt.ist.maidSyncher.domain.activeCollab.ACTask acTask =
                     pt.ist.maidSyncher.domain.activeCollab.ACTask.findById(acSubTask.getParentId());
             pt.ist.maidSyncher.domain.activeCollab.ACTask oldTask = getTask();
             setTask(acTask);
             if (!ObjectUtils.equals(acTask, oldTask)) {
                 //let's add the PropertyDescriptor
                 propertyDescriptorsToReturn.add(getPropertyDescriptorNameAndCheckItExists(acSubTask, "parentId"));
 
             }
         }
         return propertyDescriptorsToReturn;
     }
 
     public static void process(Set<pt.ist.maidSyncher.api.activeCollab.ACSubTask> acSubTasks, ACTask acTask) {
         pt.ist.maidSyncher.domain.activeCollab.ACTask acDomainTask;
         try {
             acDomainTask = pt.ist.maidSyncher.domain.activeCollab.ACTask.process(acTask);
         } catch (TaskNotVisibleException e) {
             return;
         }
 
         //let's get all of the sub tasks
         Set<ACSubTask> newSubTaskSet = new HashSet<>();
 
         Set<ACSubTask> oldSubTaskSet = new HashSet<>(acDomainTask.getSubTasksSet());
         for (pt.ist.maidSyncher.api.activeCollab.ACSubTask subTask : acSubTasks) {
             ACSubTask domainSubTask = process(subTask);
             newSubTaskSet.add(domainSubTask);
         }
 
         //ok, now let's remove all of the old ones and add the new ones TODO sync if changes were made
         for (ACSubTask oldSubTask : acDomainTask.getSubTasksSet()) {
             acDomainTask.removeSubTasks(oldSubTask);
         }
 
         for (ACSubTask newSubTask : newSubTaskSet) {
             acDomainTask.addSubTasks(newSubTask);
         }
 
     }
 
     @Override
     public DSIObject getDSIObject() {
         return getDsiObjectSubTask();
     }
 
     @Override
     public DSIObject findOrCreateDSIObject() {
         DSIObject dsiObject = getDSIObject();
         if (dsiObject == null) {
             dsiObject = new DSISubTask((DSIIssue) getTask().getDSIObject());
             setDsiObjectSubTask((DSISubTask) dsiObject);
 
         }
         return dsiObject;
     }
 
     @Override
     public SyncActionWrapper sync(SyncEvent syncEvent) {
         SyncActionWrapper syncActionWrapperToReturn = null;
         if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
             //then we should need to create a GHIssue, let's just make sure that's correct
             if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.ACTIVE_COLLAB))
                 throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());
 
             //if we are creating an artefact on AC side, it mustn't be a SubTask, it's a Task
             syncActionWrapperToReturn = syncCreateEvent(syncEvent);
 
         } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
             //then we should need to create a GHIssue, let's just make sure that's correct
             if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.ACTIVE_COLLAB))
                 throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());
 
             //if we are creating an artefact on AC side, it mustn't be a SubTask, it's a Task
             syncActionWrapperToReturn = syncUpdateEvent(syncEvent);
 
         }
 
         return syncActionWrapperToReturn;
     }
 
     private SyncActionWrapper syncUpdateEvent(SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         boolean auxChangedName = false;
         boolean auxChangedCompletion = false;
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_ID:
             case DSC_PERMALINK:
             case DSC_URL:
             case DSC_PARENTID: //this one should never change
             case DSC_CREATED_ON:
             case DSC_CREATED_BY_ID:
             case DSC_UPDATED_ON:
             case DSC_UPDATED_BY_ID:
                 //the ones above surge no changes
                 break;
             case DSC_NAME:
                 auxChangedName = true;
                 break;
             case DSC_COMPLETE:
                 auxChangedCompletion = true;
                break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
             }
         }
 
         final boolean changedName = auxChangedName;
         final boolean changedCompletion = auxChangedCompletion;
 
         return new SyncActionWrapper<SynchableObject>() {
 
             private Issue getPrefilledIssue(GHIssue ghIssue, Issue issue) {
                 if (issue != null)
                     return issue;
                 Issue issueToReturn = new Issue();
                 try {
                     ghIssue.copyPropertiesTo(issueToReturn);
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                     throw new Error("Error trying to prefill an Issue.", e);
                 }
                 issueToReturn.setTitle(getName());
                 return issueToReturn;
 
             }
 
             @Override
             public Collection<SynchableObject> sync() throws IOException {
                 Issue issueToUpdate = null;
                 DSISubTask dsiSubTask = (DSISubTask) getDSIObject();
                 GHIssue ghIssue = dsiSubTask.getGhIssue();
                 if (changedName) {
 
                     issueToUpdate = getPrefilledIssue(ghIssue, issueToUpdate);
                     issueToUpdate.setTitle(getName());
 
                 }
                 if (changedCompletion) {
                     issueToUpdate = getPrefilledIssue(ghIssue, issueToUpdate);
                     issueToUpdate
                     .setState(getComplete() == null ? GHIssue.STATE_OPEN : getComplete() ? GHIssue.STATE_CLOSED : GHIssue.STATE_OPEN);
                 }
                 if (issueToUpdate != null) {
                     IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
 
                     issueService.editIssue(ghIssue.getRepository(), issueToUpdate);
                 }
 
                 return Collections.emptySet();
 
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 // TODO Auto-generated method stub
                 return null;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 // TODO Auto-generated method stub
                 return null;
             }
 
             @Override
             public Set<Class> getSyncDependedTypesOfDSIObjects() {
                 // TODO Auto-generated method stub
                 return null;
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             }
         };
     }
 
     private SyncActionWrapper syncCreateEvent(final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_PERMALINK:
             case DSC_ID:
             case DSC_URL:
             case DSC_PARENTID:
             case DSC_NAME:
             case DSC_CREATED_ON:
             case DSC_CREATED_BY_ID:
             case DSC_UPDATED_ON:
             case DSC_UPDATED_BY_ID:
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
             }
         }
 
         return new SyncActionWrapper<SynchableObject>() {
 
             @Override
             public Collection<SynchableObject> sync() throws IOException {
 
                 pt.ist.maidSyncher.domain.activeCollab.ACTask parentACTask = getTask();
                 //let's try to find out if we need to create a GHIssue (if we have an ACTaskCategory that
                 //has an DSIRepository associated, then we do)
                 ACTaskCategory acTaskCategory = parentACTask.getTaskCategory();
                 if (ACTaskCategory.hasGHSide(acTaskCategory) == false) {
                     return Collections.emptySet();
                 }
 
                 //let's create a new GHIssue on the other side
                 Issue issue = new Issue();
 
                 //let's get the parent issue
                 DSISubTask dsiSubTask = (DSISubTask) getDSIObject();
                 GHIssue parentGHIssue = dsiSubTask.getParentIssue().getGhIssue();
 
                 //the name
                 issue.setTitle(getName());
 
                 issue.setBody(GHIssue.applySubTaskBodyPrefix(null, parentGHIssue));
                 issue.setState(getComplete() == null ? GHIssue.STATE_OPEN : getComplete() ? GHIssue.STATE_CLOSED : GHIssue.STATE_OPEN);
 
                 //set the milestones of the parent
 
                 //the repository should come from the parentACTask's ACTaskCategory
                 DSIRepository dsiRepository = (DSIRepository) acTaskCategory.getDSIObject();
                 GHRepository gitHubRepository = dsiRepository.getGitHubRepository();
 
                 parentACTask.syncGHMilestoneFromACMilestone(parentACTask.getMilestone(), gitHubRepository, issue);
 
                 //taking care of the label
                 parentACTask.syncGHLabelFromACProject(parentACTask.getProject(), gitHubRepository, issue);
 
                 //let's sync it
                 IssueService issueService = new IssueService(MaidRoot.getInstance().getGitHubClient());
                 Issue createdIssue = issueService.createIssue(gitHubRepository, issue);
                 if (issue.getState().equalsIgnoreCase(GHIssue.STATE_CLOSED)) {
                     //we must now edit it and close it
                     createdIssue = issueService.editIssue(gitHubRepository, issue);
                 }
 
                 return Collections.<SynchableObject> singleton(GHIssue.process(createdIssue,
                         gitHubRepository, true));
 
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 return syncEvent;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 Set<DSIObject> dependedOnObjects = new HashSet<DSIObject>();
                 DSISubTask dsiSubTask = (DSISubTask) getDSIObject();
                 DSIIssue parentIssue = dsiSubTask.getParentIssue();
                 dependedOnObjects.add(parentIssue);
                 return dependedOnObjects;
             }
 
             @Override
             public Set<Class> getSyncDependedTypesOfDSIObjects() {
                 Set<Class> dependedOnTypesOfDSIObjects = new HashSet<>();
                 dependedOnTypesOfDSIObjects.add(DSIRepository.class);
                 return dependedOnTypesOfDSIObjects;
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             }
         };
 
     }
 }
