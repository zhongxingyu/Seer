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
 package pt.ist.maidSyncher.domain;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.eclipse.egit.github.core.client.GitHubClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.ist.fenixframework.Atomic;
 import pt.ist.fenixframework.Atomic.TxMode;
 import pt.ist.fenixframework.DomainRoot;
 import pt.ist.fenixframework.FenixFramework;
 import pt.ist.maidSyncher.api.activeCollab.ACContext;
 import pt.ist.maidSyncher.domain.activeCollab.ACInstance;
 import pt.ist.maidSyncher.domain.dsi.DSIObject;
 import pt.ist.maidSyncher.domain.exceptions.SyncEventIllegalConflict;
 import pt.ist.maidSyncher.domain.github.GHOrganization;
 import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncEvent;
 import pt.ist.maidSyncher.domain.sync.SyncEvent.SyncUniverse;
 import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;
 import pt.ist.maidSyncher.domain.sync.logs.SyncActionLog;
 import pt.ist.maidSyncher.domain.sync.logs.SyncEventConflictLog;
 import pt.ist.maidSyncher.domain.sync.logs.SyncLog;
 import pt.ist.maidSyncher.domain.sync.logs.SyncWarningLog;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.gag.annotation.remark.ShoutOutTo;
 
 public class MaidRoot extends MaidRoot_Base {
 
     private static Multimap<DSIObject, SyncEvent> changesBuzz = HashMultimap.create();
 
     private static GitHubClient gitHubClient;
 
     private static Properties configurationProperties;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(MaidRoot.class);
 
     private static void initProperties() {
         configurationProperties = new Properties();
         InputStream configurationInputStream = MaidRoot.class.getResourceAsStream("/configuration.properties");
         if (configurationInputStream != null) {
             try {
                 configurationProperties.load(configurationInputStream);
             } catch (IOException e) {
                 throw new Error(e);
             }
         }
     }
 
     static {
         if (configurationProperties == null || configurationProperties.isEmpty()) {
             initProperties();
         }
         ACContext acContext = ACContext.getInstance();
 
         if (acContext.getServer() == null || StringUtils.isBlank(acContext.getServer())) {
             acContext.setServer(configurationProperties.getProperty("ac.server.host"));
             acContext.setToken(configurationProperties.getProperty("ac.server.token"));
         }
         if (getGitHubClient() == null) {
             //let's try to connect to the GH Account
 
             //let's try to authenticate and get the user and repository list
             setGitHubClient(new GitHubClient());
 
             String oauth2Token = configurationProperties.getProperty("github.oauth2.token");
             getGitHubClient().setOAuth2Token(oauth2Token);
         }
     }
 
     public static MaidRoot getInstance() {
         if (FenixFramework.getDomainRoot().getMaidRoot() == null) {
             initialize();
         }
         return FenixFramework.getDomainRoot().getMaidRoot();
     }
 
     @Atomic
     private static void initialize() {
         if (FenixFramework.getDomainRoot().getMaidRoot() == null) {
             FenixFramework.getDomainRoot().setMaidRoot(new MaidRoot(FenixFramework.getDomainRoot()));
         }
     }
 
     public MaidRoot(DomainRoot domainRoot) {
         super();
         domainRoot.setMaidRoot(this);
         checkIfIsSingleton();
         init();
     }
 
     public void init() {
         GHOrganization ghOrganization = getGhOrganization();
         if (ghOrganization == null)
             setGhOrganization(new GHOrganization());
         ACInstance acInstance = getAcInstance();
         if (acInstance == null)
             setAcInstance(new ACInstance(this));
 
     }
 
     private void checkIfIsSingleton() {
         if (FenixFramework.getDomainRoot().getMaidRoot() != null && FenixFramework.getDomainRoot().getMaidRoot() != this) {
             throw new Error("There can only be one! (instance of MyOrg [aka MaidRoot])");
         }
     }
 
     /**
      * Clears the {@link #changesBuzz}
      * 
      */
     public void resetSyncEvents() {
         this.changesBuzz = HashMultimap.create();
 
     }
 
     private void checkChangesBuzzApiObjectsAreEqual() {
         //TODO issue #6
 //        //ok, let's iterate through all of them
 //        for (DSIObject keyDSIObject  : changesBuzz.keySet())
 //        {
 //            List<ACObject> acObjects = new ArrayList<>();
 //            List<Object> ghObjects = new ArrayList<>();
 //            for (SyncEvent syncEvent : changesBuzz.get(keyDSIObject))
 //            {
 //                //let's get the events different api objects
 //                //and check afterwards if they are the same or not
 //                syncEvent.getApiObjectWrapper().getAPIObject()
 //            }
 //        }
 
     }
 
     /**
      * Adds the given {@link SyncEvent} to the list of sync events.
      * 
      * <p>
      * It also looks out for possible errors, and throws a {@link SyncEventIllegalConflict} if they are found.
      * </p>
      * 
      * <p>
      * The behavior of the method, is as follows:
      * </p>
      * <p>
      * Checks for the given {@link SyncEvent#getDsiElement()} if there are other events, if there are the behaviour is as follows:
      * </p>
      * <ul>
      * <li>If two {@link TypeOfChangeEvent} events of the type {@link TypeOfChangeEvent#CREATE} are found, with different
      * {@link SyncUniverse}s, an error is thrown;</li>
      * <li>Two {@link TypeOfChangeEvent} of type {@link TypeOfChangeEvent#CREATE}, but with different {@link SyncUniverse}
      * targets, we simply log that, and remove both events (in the end, this means that both objects are already created on both
      * universes)</li>
      * <li>A {@link TypeOfChangeEvent#READ} is neutral (although several reads of the same target, could be squashed to just one)</li>
      * <li>If we get a {@link TypeOfChangeEvent#CREATE} event, and an {@link TypeOfChangeEvent#UPDATE} one, we are conservative
      * and throw an error (as they shouldn't occur for now, because we either create a new object on the other side, or we need to
      * update it) TODO on each case that occurs, see what really happen and change this behaviour</li>
      * <li>If we get an {@value TypeOfChangeEvent#UPDATE} longside with an {@link TypeOfChangeEvent#UPDATE} or
      * {@link TypeOfChangeEvent#DELETE}, we have a conflict solved by date, i.e. the one that wins is the one with the most recent
      * {@link SyncEvent#getDateOfChange()}</li>
      * 
      * 
      * 
      * 
      * 
      * @throws SyncEventIllegalConflict if there are sync errors, see the behavior above
      * @param syncEvent
      */
     protected void addSyncEvent(SyncEvent syncEvent) throws SyncEventIllegalConflict {
         DSIObject dsiElement = syncEvent.getDsiElement();
         boolean addEvent = true;
         List<SyncEvent> syncEventsToDelete = new ArrayList<>();
         if (dsiElement == null) {
             changesBuzz.put(null, syncEvent);
         } else if (changesBuzz.containsKey(dsiElement)) {
             scanExistingEvents: for (SyncEvent syncEventAlreadyPresent : changesBuzz.get(dsiElement)) {
                 switch (syncEvent.getTypeOfChangeEvent()) {
                 case CREATE:
 
                     /* CREATE */
                     switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                     case CREATE:
                         if (syncEvent.getTargetSyncUniverse().equals(syncEventAlreadyPresent.getTargetSyncUniverse())) {
                             throw new SyncEventIllegalConflict("Two sync events of the type Create for the DSIObject: "
                                     + dsiElement.getExternalId() + " with class: " + dsiElement.getClass().getName()
                                     + " were detected");
                         } else {
                             //let's remove the one that exists and do not add this one
                             addEvent = false;
                             syncEventsToDelete.add(syncEventAlreadyPresent);
                             break scanExistingEvents;
                         }
                     case READ:
                         break;
                     case UPDATE:
                         throwSyncUpdateAndCreateConflictException(dsiElement);
                     case DELETE:
                         //let's be conservative for now and throw an exception
                         //(this case might be 'legal' and lead to the whoever has the most
                         //recent date to prevail over the other, but let's see this case by case)
                         throw new SyncEventIllegalConflict("A sync event of Create and Delete over the same DSIObject: "
                                 + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getName() + " was detected");
                     }
                     break;
                     /* end of CREATE */
 
                 case READ:
                     //the READs are pretty much neutral
                     break;
                 case UPDATE:
                     switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                     case CREATE:
                         throwSyncUpdateAndCreateConflictException(dsiElement);
                         break;
                     case READ:
                         break;
                     case UPDATE:
                     case DELETE:
                         //let's see the one that prevails, based on the date
                         addEvent = processUpdateAndDeleteOrUpdate(syncEvent, syncEventAlreadyPresent, syncEventsToDelete);
                         break scanExistingEvents;
                     }
                     break;
                 case DELETE:
                     switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                     case CREATE:
                         //for now, let's throw an error, but this might be pheasible
                         throw new SyncEventIllegalConflict("a Delete with a Create was detected. dsiElement: "
                                 + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getSimpleName());
                     case READ:
                         break;
                     case UPDATE:
                     case DELETE:
                         //let's see the one that prevails, based on the date
                         addEvent = processUpdateAndDeleteOrUpdate(syncEvent, syncEventAlreadyPresent, syncEventsToDelete);
                         break scanExistingEvents;
                     }
 
                     break;
                 }
 
             }
 
         //so, let's add if we have to, and delete the ones we should
         if (addEvent) {
             changesBuzz.put(dsiElement, syncEvent);
         }
         for (SyncEvent syncEventToDelete : syncEventsToDelete) {
             changesBuzz.remove(dsiElement, syncEventToDelete);
             syncEventToDelete.delete();
         }
 
         } else {
             changesBuzz.put(dsiElement, syncEvent);
         }
     }
 
     private boolean processUpdateAndDeleteOrUpdate(SyncEvent syncEvent, SyncEvent syncEventAlreadyPresent,
             List<SyncEvent> syncEventsToDelete) {
         SyncEventConflictLog syncEventConflictLog = new SyncEventConflictLog(syncEvent, syncEventAlreadyPresent);
         MaidRoot.getInstance().getCurrentSyncLog().addSyncConflictLogs(syncEventConflictLog);
         if (syncEvent.getDateOfChange().compareTo(syncEventAlreadyPresent.getDateOfChange()) <= 0) {
             //the already present is more recent
             syncEventConflictLog.markSecondAsWinner();
             return false;
         } else {
             //the update, the new event, is more recent
             //let's delete this already present one
             syncEventsToDelete.add(syncEventAlreadyPresent);
             syncEventConflictLog.markFirstAsWinner();
             return true;
         }
     }
 
     private void throwSyncUpdateAndCreateConflictException(DSIObject dsiElement) throws SyncEventIllegalConflict {
         throw new SyncEventIllegalConflict("A sync event of the type Create and Update for the DSIObject: "
                 + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getName() + " were detected");
 
     }
 
     public static Multimap<DSIObject, SyncEvent> getChangesBuzz() {
         return changesBuzz;
     }
 
     public static GitHubClient getGitHubClient() {
         return gitHubClient;
     }
 
 //    public void fullySync(User repository) {
 //        checkNotNull(repository, "repository must not be null");
 //        checkArgument(repository.getType().equals(User.TYPE_ORG), "You must provide a repository");
 //
 //    }
 
     public static void setGitHubClient(GitHubClient gitHubClient) {
         MaidRoot.gitHubClient = gitHubClient;
     }
 
     private static class SyncWrapper {
         final DSIObject dsiObject;
         private final Set<SyncActionWrapper<? extends SynchableObject>> actionWrappers = new HashSet<>();
 
         public SyncWrapper(DSIObject dsiObject) {
             this.dsiObject = dsiObject;
         }
 
 //        @Override
 //        public boolean equals(Object obj) {
 //            if (obj == null) {
 //                return false;
 //            }
 //            if ((obj instanceof SyncWrapper) == false)
 //                return false;
 //            SyncWrapper syncWrapperToCompareWith = (SyncWrapper) obj;
 //            return ObjectUtils.equals(dsiObject, syncWrapperToCompareWith.dsiObject);
 //        }
 //
 //        @Override
 //        public int hashCode() {
 //            return ObjectUtils.hashCode(dsiObject);
 //        }
 
         public void addSyncAction(SyncActionWrapper<? extends SynchableObject> syncActionWrapper) {
             this.getActionWrappers().add(syncActionWrapper);
         }
 
         public int getNumberSyncActions() {
             return this.getActionWrappers().size();
         }
 
         public static int getNumberSyncActions(Collection<SyncWrapper> syncWrappers) {
             int number = 0;
             for (SyncWrapper syncWrapper : syncWrappers) {
                 number += syncWrapper.getNumberSyncActions();
             }
             return number;
         }
 
         public static Set<DSIObject> getDSIObjects(Collection<SyncWrapper> syncWrappers) {
             Set<DSIObject> dsiObjects = new HashSet<>();
             for (SyncWrapper syncWrapper : syncWrappers) {
                 dsiObjects.add(syncWrapper.dsiObject);
             }
             return dsiObjects;
         }
 
         /**
          * Processes the SyncActions contained in the {@link #actionWrappers}. If all are consumed, true is returned
          * 
          * @return true if everything was processed, false otherwise
          */
         @Atomic(mode = TxMode.READ)
         public boolean process(Set<DSIObject> dsiObjectsToSync) {
             Iterator<SyncActionWrapper<? extends SynchableObject>> actionWrappersIterator = getActionWrappers().iterator();
             while (actionWrappersIterator.hasNext()) {
                 SyncActionWrapper<? extends SynchableObject> syncActionWrapper = actionWrappersIterator.next();
                 if (SyncEvent.isAbleToRunNow(syncActionWrapper, dsiObjectsToSync)) {
                     SyncActionLog syncActionLog = logSyncStart(syncActionWrapper);
                     try {
                         atomicProcessSyncAction(syncActionWrapper);
                         logSyncSuccessAndDeleteSyncEvent(syncActionLog, syncActionWrapper);
                         actionWrappersIterator.remove();
                     } catch (Exception ex) {
                         logSyncFailure(syncActionLog, ex);
 
                     }
                 }
             }
             return getActionWrappers().isEmpty();
         }
 
         @Atomic(mode = TxMode.WRITE)
         private void logSyncFailure(SyncActionLog syncActionLog, Exception ex) {
             syncActionLog.markEndOfSync(false, ExceptionUtils.getFullStackTrace(ex));
         }
 
         @Atomic(mode = TxMode.WRITE)
         private SyncActionLog logSyncStart(SyncActionWrapper<? extends SynchableObject> syncActionWrapper) {
             SyncLog currentSyncLog = MaidRoot.getInstance().getCurrentSyncLog();
             System.out.println("current " + currentSyncLog == null);
             System.out.println("syncActionWrapper " + syncActionWrapper == null);
             System.out.println("syncActionWrapper event " + syncActionWrapper.getOriginatingSyncEvent() == null);
 
             SyncActionLog syncActionLog =
                     new SyncActionLog(currentSyncLog, syncActionWrapper.getOriginatingSyncEvent(),
                             syncActionWrapper.getOriginatingSyncEvent().getDsiElement());
             syncActionLog.markStartOfSync();
             return syncActionLog;
 
         }
 
         @Atomic(mode = TxMode.WRITE)
         private void logSyncSuccessAndDeleteSyncEvent(SyncActionLog syncActionLog,
                 SyncActionWrapper<? extends SynchableObject> syncActionWrapper) {
             MaidRoot.getInstance().getSyncEventsToProcessSet().remove(syncActionWrapper.getOriginatingSyncEvent());
             syncActionLog.markEndOfSync(true);
             syncActionWrapper.getOriginatingSyncEvent().delete();
 
         }
 
         @Atomic(mode = TxMode.WRITE)
         private void atomicProcessSyncAction(SyncActionWrapper syncActionWrapper) throws IOException {
             LOGGER.info("Running SyncActionWrapper for event: " + syncActionWrapper.getOriginatingSyncEvent().toString());
             syncActionWrapper.sync();
 
         }
 
         public Set<SyncActionWrapper<? extends SynchableObject>> getActionWrappers() {
             return actionWrappers;
         }
     }
 
     @Atomic(mode = TxMode.READ)
     public void applyChangesBuzz() throws IOException {
         if (getSyncEventsToProcessSet().isEmpty() == false) {
             throw new IllegalStateException("The maidRoot still has SyncEvents to "
                     + "process, that should be processed before calling this method");
         }
         Map<DSIObject, Collection<SyncEvent>> changesMap = getChangesBuzz().asMap();
         Set<SyncWrapper> syncWrappers = createSyncWrappers(changesMap);
 
         registerNumberGeneratedSyncActions(syncWrappers);
 
         //after everything is validated, let's add the SyncEvents to the
         //to process queue of the MaidRoot
         addSyncEventsToProcessQueue(syncWrappers);
 
         processSyncWrappers(syncWrappers);
 
         LOGGER.info("Applied all SyncActions");
 
         getCurrentSyncLog().markAsSuccess();
 
     }
 
 
     private void processSyncWrappers(Set<SyncWrapper> syncWrappers) {
         while (syncWrappers.isEmpty() == false) {
             Iterator<SyncWrapper> syncWrappersIterator = syncWrappers.iterator();
             while (syncWrappersIterator.hasNext()) {
                 SyncWrapper syncWrapper = syncWrappersIterator.next();
 
                 if (syncWrapper.process(SyncWrapper.getDSIObjects(syncWrappers))) {
                     //then we can remove it
                     syncWrappersIterator.remove();
                 }
             }
         }
 
     }
 
     /**
      * Tries to process any remaining {@link SyncEvent} that might still be
      * in {@link #getSyncEventsToProcessSet()}.
      * 
      */
     @Atomic(mode = TxMode.READ)
     public void processRemainingInstances() {
         //based on the events we have, let's create the map
         Set<SyncWrapper> syncWrappers = createSyncWrappers(getMultiMapFromSyncEvents(getSyncEventsSet()).asMap());
 
         registerNumberOfGeneratedSyncActionsForUnprocessedEvents(syncWrappers);
 
         processSyncWrappers(syncWrappers);
 
         LOGGER.info("Applied all SyncActions of the unprocessed SyncEvents");
 
     }
 
     @Atomic(mode = TxMode.WRITE)
     private void registerNumberOfGeneratedSyncActionsForUnprocessedEvents(Set<SyncWrapper> syncWrappers) {
         int numberSyncActions = SyncWrapper.getNumberSyncActions(syncWrappers);
         getCurrentSyncLog().setNrGeneratedSyncActionsFromRemainingSyncEvents(numberSyncActions);
         LOGGER.info("Number of SyncActions from unprocessed Sync Events Generated: " + numberSyncActions);
 
     }
 
     private Set<SyncWrapper> createSyncWrappers(Map<DSIObject, Collection<SyncEvent>> changesMap) {
         Set<SyncWrapper> syncWrappers = new HashSet<>();
 
         for (DSIObject dsiObject : changesMap.keySet()) {
             SyncWrapper syncWrapper = new SyncWrapper(dsiObject);
             for (SyncEvent syncEvent : changesMap.get(dsiObject)) {
                 SyncActionWrapper syncActionWrapper = syncEvent.getOriginObject().sync(syncEvent);
                 if (syncActionWrapper != null) {
                     validate(syncActionWrapper, syncEvent);
                     syncWrapper.addSyncAction(syncActionWrapper);
                 }
             }
             syncWrappers.add(syncWrapper);
         }
         return syncWrappers;
     }
 
     private static Multimap<DSIObject, SyncEvent> getMultiMapFromSyncEvents(Collection<SyncEvent> syncEvents) {
         Multimap<DSIObject, SyncEvent> multimap = HashMultimap.create();
         for (SyncEvent syncEvent : syncEvents) {
             multimap.put(syncEvent.getDsiElement(), syncEvent);
         }
         return multimap;
     }
 
     @Atomic(mode = TxMode.WRITE)
     private void addSyncEventsToProcessQueue(Set<SyncWrapper> syncWrappers) {
         for (SyncWrapper syncWrapper : syncWrappers) {
             for (SyncActionWrapper<? extends SynchableObject> syncActionWrapper : syncWrapper.getActionWrappers()) {
                 getSyncEventsToProcessSet().add(syncActionWrapper.getOriginatingSyncEvent());
             }
         }
 
     }
 
     @Atomic(mode = TxMode.WRITE)
     private void registerNumberGeneratedSyncActions(Set<SyncWrapper> syncWrappers) {
         int numberSyncActions = SyncWrapper.getNumberSyncActions(syncWrappers);
         getCurrentSyncLog().setNrGeneratedSyncActions(numberSyncActions);
         LOGGER.info("Number of SyncActions Generated: " + numberSyncActions);
 
     }
 
     /**
      * 
      * @param syncActionWrapper {@link SyncActionWrapper} to validate
      * @throws Exception if there is any kind of problem with it
      *             It verifies the syncActionWrapper for an originationg sync event;
      *             That the lists are not null, that all of the property descriptors were ticked, etc;
      */
     @ShoutOutTo(value = { "the guy that thought that including a null was a good idea" })
     private void validate(SyncActionWrapper syncActionWrapper, SyncEvent syncEvent) {
         checkNotNull(syncActionWrapper.getOriginatingSyncEvent());
         checkNotNull(syncActionWrapper.getOriginatingSyncEvent().getOriginObject());
         checkNotNull(syncActionWrapper.getOriginatingSyncEvent().getDsiElement());
         checkNotNull(syncActionWrapper.getPropertyDescriptorNamesTicked());
        checkNotNull(syncActionWrapper.getSyncDependedDSIObjects());
         checkNotNull(syncActionWrapper.getSyncDependedTypesOfDSIObjects());
 
         if (syncActionWrapper.getOriginatingSyncEvent().equals(syncEvent) == false) {
             throw new IllegalArgumentException("Sync events from wrapper and original event differ");
         }
 
         Collection<String> propertyDescriptorsTicked = syncActionWrapper.getPropertyDescriptorNamesTicked();
         Collection<String> changedPropertyDescriptors =
                 syncActionWrapper.getOriginatingSyncEvent().getChangedPropertyDescriptorNames().getUnmodifiableList();
         if (!propertyDescriptorsTicked.containsAll(changedPropertyDescriptors)) {
             //let's generate a log
             registerPropertyDescriptorNotConsideredWarning(changedPropertyDescriptors, propertyDescriptorsTicked, syncEvent);
         }
 
     }
 
     @SuppressWarnings("unused")
     @Atomic(mode = TxMode.WRITE)
     private static void registerPropertyDescriptorNotConsideredWarning(Collection<String> changedPropertyDescriptors,
             Collection<String> propertyDescriptorsTicked,
             SyncEvent syncEvent) {
         StringBuilder exceptionMessageBuilder = new StringBuilder();
         exceptionMessageBuilder.append("One didn't consider a property descriptor change. Property descriptors not ticked: ");
         HashSet<String> copyOfChangedDescriptors = new HashSet<>(changedPropertyDescriptors);
         copyOfChangedDescriptors.removeAll(propertyDescriptorsTicked);
         for (String propertyDescriptor : copyOfChangedDescriptors) {
             exceptionMessageBuilder.append(propertyDescriptor + " ");
         }
         if (syncEvent != null && syncEvent.getOriginObject() != null) {
             exceptionMessageBuilder.append("Class of origin object: " + syncEvent.getOriginObject().getClass().getName());
 
         } else {
             exceptionMessageBuilder.append("Class of Origin object unknown because syncEvent is null.");
         }
 
         new SyncWarningLog(MaidRoot.getInstance().getCurrentSyncLog(), exceptionMessageBuilder.toString());
 
     }
 
 }
