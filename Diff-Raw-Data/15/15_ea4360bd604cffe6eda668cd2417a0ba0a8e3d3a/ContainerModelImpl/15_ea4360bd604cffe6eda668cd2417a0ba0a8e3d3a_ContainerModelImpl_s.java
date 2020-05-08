 /*
  * Created On: Jun 27 06 12:13:12 PM
  */
 package com.thinkparity.ophelia.model.container;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.channels.FileChannel;
 import java.util.*;
 import java.util.Map.Entry;
 
 import javax.xml.transform.TransformerException;
 
 import com.thinkparity.codebase.FileSystem;
 import com.thinkparity.codebase.Pair;
 import com.thinkparity.codebase.ResourceUtil;
 import com.thinkparity.codebase.ZipUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.filter.Filter;
 import com.thinkparity.codebase.filter.FilterChain;
 import com.thinkparity.codebase.filter.FilterManager;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.ThinkParityException;
 import com.thinkparity.codebase.model.UploadMonitor;
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.ArtifactState;
 import com.thinkparity.codebase.model.artifact.ArtifactType;
 import com.thinkparity.codebase.model.artifact.ArtifactVersion;
 import com.thinkparity.codebase.model.artifact.PublishedToEMail;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.contact.OutgoingEMailInvitation;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerConstraints;
 import com.thinkparity.codebase.model.container.ContainerDraftDocument;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta;
 import com.thinkparity.codebase.model.container.ContainerVersionDelta;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentDraft;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.stream.StreamOpener;
 import com.thinkparity.codebase.model.stream.StreamSession;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.xmpp.event.ArtifactDraftDeletedEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.ArtifactReceivedEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.container.PublishedEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.container.PublishedNotificationEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.container.VersionPublishedEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.container.VersionPublishedNotificationEvent;
 
 import com.thinkparity.ophelia.model.Delegate;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.artifact.InternalArtifactModel;
 import com.thinkparity.ophelia.model.container.delegate.*;
 import com.thinkparity.ophelia.model.container.export.PDFWriter;
 import com.thinkparity.ophelia.model.container.monitor.PublishStep;
 import com.thinkparity.ophelia.model.document.CannotLockException;
 import com.thinkparity.ophelia.model.document.DocumentFileLock;
 import com.thinkparity.ophelia.model.document.DocumentNameGenerator;
 import com.thinkparity.ophelia.model.document.InternalDocumentModel;
 import com.thinkparity.ophelia.model.events.ContainerDraftListener;
 import com.thinkparity.ophelia.model.events.ContainerListener;
 import com.thinkparity.ophelia.model.events.ContainerEvent.Source;
 import com.thinkparity.ophelia.model.index.InternalIndexModel;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.handler.ArtifactIOHandler;
 import com.thinkparity.ophelia.model.io.handler.ContainerIOHandler;
 import com.thinkparity.ophelia.model.io.handler.DocumentIOHandler;
 import com.thinkparity.ophelia.model.session.InternalSessionModel;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.util.UUIDGenerator;
 import com.thinkparity.ophelia.model.util.filter.UserFilterManager;
 import com.thinkparity.ophelia.model.util.sort.ComparatorBuilder;
 import com.thinkparity.ophelia.model.util.sort.ModelSorter;
 import com.thinkparity.ophelia.model.util.sort.user.UserComparatorFactory;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 /**
  * <b>Title:</b>thinkParity Container Model Implementation</br>
  * <b>Description:</b>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.85
  */
 public final class ContainerModelImpl extends
         Model<ContainerListener> implements ContainerModel,
         InternalContainerModel {
 
     /**
      * Used by the progress monitor to determine the number of steps based on
      * file size.
      */
     private static final Integer STEP_SIZE;
 
     static {
         STEP_SIZE = 1024;
     }
 
     /**
      * Count the steps required to publish.
      * 
      * @param documentVersions
      *            A <code>List</code> of <code>DocumentVersion</code>s.
      */
     static Integer countSteps(final List<DocumentVersion> documentVersions) {
         long totalSize = 0;
         for (final DocumentVersion documentVersion : documentVersions) {
             totalSize += documentVersion.getSize();
         }
         // a step per k of size
         long steps = totalSize / STEP_SIZE;
         // add 5% for the last step
         steps = (long) (steps * 1.05);
         return Integer.valueOf((int) steps);
     }
 
     /** The artifact io layer. */
     private ArtifactIOHandler artifactIO;
 
     /** The container constraints. */
     private final ContainerConstraints containerConstraints;
 
     /** The container io layer. */
     private ContainerIOHandler containerIO;
 
     /** A default container comparator. */
     private final Comparator<Artifact> defaultComparator;
 
     /** The default document comparator. */
     private final Comparator<Artifact> defaultDocumentComparator;
 
     /** The default document filter. */
     private final Filter<? super Artifact> defaultDocumentFilter;
 
     /** A default document version comparator. */
     private final Comparator<ArtifactVersion> defaultDocumentVersionComparator;
 
     /** A default container filter. */
     private final Filter<? super Artifact> defaultFilter;
 
     /** A default artifact receipt comparator. */
     private final Comparator<ArtifactReceipt> defaultReceiptComparator;
 
     /** A default artifact receipt filter. */
     private final Filter<? super ArtifactReceipt> defaultReceiptFilter;
 
     /** The default container version comparator. */
     private final Comparator<ArtifactVersion> defaultVersionComparator;
 
     /** The default container version filter. */
     private final Filter<? super ArtifactVersion> defaultVersionFilter;
 
     /** The document io layer. */
     private DocumentIOHandler documentIO;
 
     /** A local event generator. */
     private final ContainerEventGenerator localEventGenerator;
 
     /** A remote event generator. */
     private final ContainerEventGenerator remoteEventGenerator;
 
     /**
      * Create ContainerModelImpl.
      *
      * @param workspace
      *      The thinkParity workspace.
      */
     public ContainerModelImpl() {
         super();
         this.containerConstraints = ContainerConstraints.getInstance();
         this.defaultComparator = new ComparatorBuilder().createByName(Boolean.TRUE);
         this.defaultDocumentComparator = new ComparatorBuilder().createByName(Boolean.TRUE);
         this.defaultDocumentFilter = FilterManager.createDefault();
         this.defaultDocumentVersionComparator = new ComparatorBuilder().createVersionByName();
         this.defaultFilter = FilterManager.createDefault();
         this.defaultReceiptComparator = new ComparatorBuilder().createArtifactReceiptByReceivedOnAscending();
         this.defaultReceiptFilter = FilterManager.createDefault();
         this.defaultVersionComparator = new ComparatorBuilder().createVersionById(Boolean.FALSE);
         this.defaultVersionFilter = FilterManager.createDefault();
         this.localEventGenerator = new ContainerEventGenerator(Source.LOCAL);
         this.remoteEventGenerator = new ContainerEventGenerator(Source.REMOTE);
     }
 
     /**
      * Apply a bookmark to a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      */
     public void addBookmark(final Long containerId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         try {
             getArtifactModel().applyFlagBookmark(containerId);
             notifyContainerFlagged(read(containerId), localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Add a document to a container.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      */
     public void addDocument(final Long containerId, final Long documentId) {
         try {
             assertContainerDraftExists(containerId,
                     "Draft for {0} does not exist.", containerId);
             // create draft artifact relationship
             containerIO.createDraftArtifactRel(containerId, documentId,
                     ContainerDraft.ArtifactState.ADDED);
             // create draft document
             createDraftDocument(containerId, documentId);
             // index
             getIndexModel().indexDocument(containerId, documentId);
             // fire event
             final Container eventContainer = read(containerId);        
             final ContainerDraft eventDraft = readDraft(containerId);
             final Document eventDocument = getDocumentModel().read(documentId);
             notifyDocumentAdded(eventContainer, eventDraft, eventDocument,
                     localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Add a container listener to the model.
      * 
      * @param listener
      *            A <code>ContainerListener</code>.
      */
     @Override
     public void addListener(final ContainerListener listener) {
         super.addListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#archive(java.lang.Long)
      * 
      */
     public void archive(final Long containerId) {
         try {
             assertIsDistributed("Container has not been distributed.", containerId);
 
             if (doesExistDraft(containerId))
                 deleteDraft(containerId);
             getArtifactModel().applyFlagArchived(containerId);
             getBackupModel().archive(containerId);
             getSessionModel().removeTeamMember(
                     getArtifactModel().readUniqueId(containerId),
                     getArtifactModel().readTeamIds(containerId), localUserId());
 
             notifyContainerArchived(read(containerId), localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Create a container.
      * 
      * @param name
      *            The container name.
      * @return The new container.
      */
     public Container create(final String name) {
         logger.logApiId();
         logger.logVariable("name", name);
         try {
             final Container container = new Container();
             container.setCreatedBy(localUserId());
             container.setCreatedOn(currentDateTime());
             container.setName(name);
             container.setState(ArtifactState.ACTIVE);
             container.setType(ArtifactType.CONTAINER);
             container.setUniqueId(UUIDGenerator.nextUUID());
             container.setUpdatedBy(container.getCreatedBy());
             container.setUpdatedOn(container.getCreatedOn());
             // local create
             containerIO.create(container);
     
             // local key
             final InternalArtifactModel artifactModel = getArtifactModel();
             artifactModel.applyFlagKey(container.getId());
             artifactModel.applyFlagLatest(container.getId());
     
             // index
             getIndexModel().indexContainer(container.getId());
     
             // create team
             final TeamMember teamMember = getArtifactModel().createTeam(
                     container.getId()).get(0);
     
             // create first draft
             createFirstDraft(container.getId(), teamMember);
     
             // audit\fire event
             final Container postCreation = read(container.getId());
             notifyContainerCreated(postCreation, localEventGenerator);
             return postCreation;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#createDraft(java.lang.Long)
      * 
      */
     public ContainerDraft createDraft(final Long containerId) {
         try {
             assertContainerDraftDoesNotExist(containerId,
                     "Draft for {0} already exists.", containerId);
             if (isFirstDraft(containerId)) {
                 createFirstDraft(containerId, localTeamMember(containerId));
             } else {
                 ensureOnline();
 
                 final Calendar createdOn = getSessionModel().readDateTime();
                 final InternalArtifactModel artifactModel = getArtifactModel();
 
                 final Container container = read(containerId);
                 if (!isDistributed(container.getId())) {
                     createDistributed(container, createdOn);
                 }
                 final ContainerVersion latestVersion =
                         readLatestVersion(container.getId());
                 final List<Document> documents = readDocuments(
                         latestVersion.getArtifactId(), latestVersion.getVersionId());
                 final Map<Document, DocumentFileLock> documentLocks = lockDocuments(documents);
                 try {
                     // create
                     final ContainerDraft draft = new ContainerDraft();
                     draft.setContainerId(containerId);
                     draft.setLocal(Boolean.TRUE);
                     draft.setOwner(localTeamMember(containerId));
 
                     final InternalDocumentModel documentModel = getDocumentModel();
                     Long versionId;
                     for (final Document document : documents) {
                         draft.addDocument(document);
                         draft.putState(document, ContainerDraft.ArtifactState.NONE);
                     }
                     containerIO.createDraft(draft);
                     DocumentDraft documentDraft;
                     for (final Document document : documents) {
                         documentDraft = documentModel.createDraft(
                                 documentLocks.get(document), document.getId());
                         versionId = artifactModel.readLatestVersionId(document.getId());
                         final ContainerDraftDocument draftDocument = new ContainerDraftDocument();
                         draftDocument.setChecksum(documentDraft.getChecksum());
                         draftDocument.setChecksumAlgorithm(documentDraft.getChecksumAlgorithm());
                         draftDocument.setContainerDraftId(containerId);
                         draftDocument.setDocumentId(document.getId());
                         draftDocument.setSize(documentDraft.getSize());
                         documentModel.openVersion(document.getId(), versionId, new StreamOpener() {
                             public void open(final InputStream stream) throws IOException {
                                 try {
                                     containerIO.createDraftDocument(
                                             draftDocument, stream, getBufferSize());
                                 } finally {
                                     stream.close();
                                 }
                             }
                         });
                     }
                     artifactModel.applyFlagKey(container.getId());
                     // remote create
                     final List<JabberId> team = artifactModel.readTeamIds(containerId);
                     team.remove(localUserId());
                     getSessionModel().createDraft(team, container.getUniqueId(),
                             createdOn);
                 } finally {
                     releaseLocks(documentLocks.values());
                 }
             }
             // fire event
             final Container postCreation = read(containerId);
             final ContainerDraft postCreationDraft = readDraft(containerId);
             notifyDraftCreated(postCreation, postCreationDraft, localEventGenerator);
             return postCreationDraft;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#delete(java.lang.Long)
      * 
      */
     public void delete(final Long containerId) throws CannotLockException {
         try {
             final User localUser = localUser();
             final Container container = read(containerId);
             final List<Document> allDocuments = readAllDocuments(containerId);
             final Map<Document, DocumentFileLock> allDocumentsLocks = lockDocuments(allDocuments);
             final Map<DocumentVersion, DocumentFileLock> allDocumentVersionsLocks = lockDocumentVersions(allDocuments);
             if (isDistributed(container.getId())) {
                 final InternalSessionModel sessionModel = getSessionModel();
                 final Calendar deletedOn = sessionModel.readDateTime();
                 // delete the draft
                 if (containerIO.doesExistLocalDraft(containerId, localUser.getLocalId())) {
                     sessionModel.deleteDraft(container.getUniqueId(), deletedOn);
                 }
                 // the archive user is not part of the team
                 if (isLocalTeamMember(container.getId())) {
                     final TeamMember localTeamMember = localTeamMember(container.getId());
                     final List<JabberId> team = getArtifactModel().readTeamIds(container.getId());
                     team.remove(localUserId());
                     sessionModel.removeTeamMember(
                             container.getUniqueId(), team, localTeamMember.getId());
                 }
                 // delete from the backup
                 getBackupModel().delete(containerId);
                 deleteLocal(container.getId(), allDocuments, allDocumentsLocks, allDocumentVersionsLocks);
             } else {
                 deleteLocal(container.getId(), allDocuments, allDocumentsLocks, allDocumentVersionsLocks);
             }
             // fire event
             notifyContainerDeleted(container, localEventGenerator);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Delete a draft.
      * 
      * @param containerId
      *            A container id.
      */
     public void deleteDraft(final Long containerId) throws CannotLockException {
         try {
             assertDoesExistDraft(containerId, "Draft does not exist.");
             final InternalDocumentModel documentModel = getDocumentModel();
             final Container container = read(containerId);
             final ContainerDraft draft = readDraft(containerId);
             final Boolean doesExistLocalDraft = doesExistLocalDraft(containerId);
             final List<Document> draftDocuments = draft.getDocuments();
             final Map<Document, DocumentFileLock> locks = lockDocuments(draftDocuments);
             try {
                 if (doesExistLocalDraft) {
                     if (!isFirstDraft(container.getId())) {
                         ensureOnline();
                         assertIsDistributed("Draft has not been distributed.", containerId);
                         final InternalSessionModel sessionModel = getSessionModel();
                         sessionModel.deleteDraft(container.getUniqueId(),
                                 sessionModel.readDateTime());
                     }
                 }
                 // delete local data
                 containerIO.deleteDraftDocuments(containerId);
                 for (final Document draftDocument : draftDocuments) {
                     documentModel.deleteDraft(locks.get(draftDocument), draftDocument.getId());
                     containerIO.deleteDraftArtifactRel(containerId, draftDocument.getId());
                     /* the document doesn't exist as in relation to the package
                      * so we delete it */
                     if (!doesExistArtifact(containerId, draftDocument.getId())) {
                         documentModel.delete(draftDocument.getId());
                     }
                 }
                 containerIO.deleteDraft(containerId);
                 notifyDraftDeleted(container, draft, localEventGenerator);
             } finally {
                 releaseLocks(locks.values());
             }
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#deletePublishedTo(com.thinkparity.codebase.email.EMail)
      * 
      */
     public void deletePublishedTo(final EMail publishedTo) {
         try {
             final List<Container> containers =
                 containerIO.readForPublishedToEMail(publishedTo);
             containerIO.deletePublishedTo(publishedTo);
             // fire event
             for (final Container container : containers) {
                 notifyContainerReceived(container, remoteEventGenerator);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#doesExistDraft(java.lang.Long)
      * 
      */
     public Boolean doesExistDraft(final Long containerId) {
         try {
             return containerIO.doesExistDraft(containerId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#doesExistLocalDraft(java.lang.Long)
      *
      */
     public Boolean doesExistLocalDraft(Long containerId) {
         try {
             final User localUser = localUser();
             return containerIO.doesExistLocalDraft(containerId,
                     localUser.getLocalId());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Export a container.
      * 
      * @param exportDirectory
      *            The directory <code>File</code> to export to.
      * @param containerId
      *            The container id <code>Long</code>.
      */
     public void export(final OutputStream exportStream, final Long containerId) {
         try {
             final Container container = read(containerId);
             final List<ContainerVersion> versions = readVersions(containerId);
             export(exportStream, container, versions);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Obtain a draft monitor. The monitor will be notified if and when a
      * document's state is changed.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param listener
      *            A <code>ContainerDraftListener</code>.
      * @return A <code>ContainerDraftMonitor</code>.
      */
     public ContainerDraftMonitor getDraftMonitor(final Long containerId,
             final ContainerDraftListener listener) {
         try {
             assertContainerDraftExists(containerId,
                     "Draft for {0} does not exist.", containerId);
             return new ContainerDraftMonitor(modelFactory,
                     readDraft(containerId), localEventGenerator, listener);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleDraftCreated(java.lang.Long,
      *      com.thinkparity.codebase.jabber.JabberId, java.util.Calendar)
      * 
      */
     public void handleDraftCreated(final Long containerId,
             final JabberId createdBy, final Calendar createdOn) {
         try {
             final ContainerDraft draft = new ContainerDraft();
             draft.setContainerId(containerId);
             draft.setLocal(Boolean.FALSE);
             final List<TeamMember> team = readTeam(containerId);
             draft.setOwner(team.get(indexOf(team, createdBy)));
             containerIO.createDraft(draft);
             // fire event
             notifyDraftCreated(read(containerId), readDraft(containerId),
                     remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Handle the remote draft deleted event.
      * 
      * @param containerId
      *            A container id.
      * @param deletedBy
      *            Who deleted the draft.
      * @param deletedOn
      *            When the draft was deleted.
      */
     public void handleDraftDeleted(final ArtifactDraftDeletedEvent event) {
         logger.logApiId();
         logger.logVariable("event", event);
         try {
             if (localUserId().equals(event.getDeletedBy())) {
                 logger.logInfo("Receiving {0} event for local user:  {1}.",
                         event.getClass().getSimpleName(), event);
             } else {
                 final Long containerId = getArtifactModel().readId(event.getUniqueId());
                 final ContainerDraft draft = readDraft(containerId);
                 for (final Artifact artifact : draft.getArtifacts()) {
                     containerIO.deleteDraftArtifactRel(containerId, artifact.getId());
                 }
                 containerIO.deleteDraft(containerId);
                 // fire event
                 notifyDraftDeleted(read(containerId), draft, remoteEventGenerator);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.container.PublishedEvent)
      * 
      */
     public void handleEvent(final PublishedEvent event) {
         try {
             // handle publish
             final HandlePublished delegate = createDelegate(
                     HandlePublished.class);
             delegate.setEvent(event);
             delegate.handlePublished();
             // fire events
             final Long containerId = getArtifactModel().readId(
                     event.getVersion().getArtifactUniqueId());
             final Container container = read(containerId);
             final ContainerVersion version = readVersion(containerId,
                     event.getVersion().getVersionId());
             final ContainerVersion previousVersion = readPreviousVersion(
                     containerId, version.getVersionId());
             final ContainerVersion nextVersion = readPreviousVersion(
                     containerId, version.getVersionId());
             if (delegate.didRestore().booleanValue()) {
                 notifyContainerRestored(container, remoteEventGenerator);
             }
             notifyContainerPublished(container, delegate.getDraft(),
                     previousVersion, version, nextVersion,
                     delegate.getPublishedBy(), remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.container.PublishedNotificationEvent)
      * 
      */
     public void handleEvent(final PublishedNotificationEvent event) {
         try {
             final HandlePublishedNotification delegate =
                 createDelegate(HandlePublishedNotification.class);
             delegate.setEvent(event);
             delegate.handlePublishedNotification();
             // fire event
             final Long containerId = getArtifactModel().readId(
                     event.getVersion().getArtifactUniqueId());
             final Container container = read(containerId);
             notifyContainerFlagged(container, remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.container.VersionPublishedEvent)
      *
      */
     public void handleEvent(final VersionPublishedEvent event) {
         try {
             final HandleVersionPublished delegate = createDelegate(HandleVersionPublished.class);
             delegate.setEvent(event);
             delegate.handleVersionPublished();
             // fire event
             final Long containerId = getArtifactModel().readId(
                     event.getVersion().getArtifactUniqueId());
             final Container container = read(containerId);
             final ContainerVersion version = readVersion(containerId,
                     event.getVersion().getVersionId());
             final ContainerVersion previousVersion = readPreviousVersion(
                     containerId, version.getVersionId());
             final ContainerVersion nextVersion = readPreviousVersion(
                     containerId, version.getVersionId());
             notifyContainerPublished(container, null, previousVersion, version,
                     nextVersion, delegate.getPublishedBy(),
                     remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.container.VersionPublishedNotificationEvent)
      *
      */
     public void handleEvent(final VersionPublishedNotificationEvent event) {
         try {
             final HandleVersionPublishedNotification delegate =
                 createDelegate(HandleVersionPublishedNotification.class);
             delegate.setEvent(event);
             delegate.handleVersionPublishedNotification();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#handleReceived(com.thinkparity.codebase.model.util.xmpp.event.ArtifactReceivedEvent)
      * 
      */
     public void handleReceived(final ArtifactReceivedEvent event) {
         try {
             final HandleReceived delegate = createDelegate(HandleReceived.class);
             delegate.setEvent(event);
             delegate.handleReceived();
             // fire event
             notifyContainerReceived(delegate.getContainer(), remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Determine if the container has been distributed. If a container is
      * distributed it will have 1 or more versions. If it has not been
      * distributed it will have no versions.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the container has been distributed; false otherwise.
      */
     public Boolean isDistributed(final Long containerId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         try {
             return getArtifactModel().doesVersionExist(containerId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Determine whether or not the local draft is different from the previous
      * version. If an artifact has a state other than none, the artifact is
      * modified.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the local draft is modified.
      */
     public Boolean isLocalDraftModified(final Long containerId) {
         final ContainerDraft draft = containerIO.readDraft(containerId);
         boolean isLocalDraftModified = false;
         for (final Document document : draft.getDocuments()) {
             // if the draft document is added or removed the state will be
             // recorded and the draft is modified
             switch (draft.getState(document)) {
             case ADDED:
             case REMOVED:
             case MODIFIED:
                 isLocalDraftModified = true;
                 break;
             case NONE:
                 isLocalDraftModified = getDocumentModel().isDraftModified(
                         document.getId());
                 break;
             default:
                 Assert.assertUnreachable("Unknown draft document state.");
             }
             if (isLocalDraftModified)
                 return Boolean.TRUE;
         }
         return Boolean.valueOf(isLocalDraftModified);
     }
 
     /**
      * Print a container draft.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param printer
      *            An <code>Printer</code>.
      */
     public void printDraft(final Long containerId,
             final ContainerDraftPrinter printer) {
         try {
             final ContainerDraft draft = readDraft(containerId);
             final InternalDocumentModel documentModel = getDocumentModel();
             for (final Document document : draft.getDocuments()) {
                 if (ContainerDraft.ArtifactState.REMOVED != draft.getState(document)) {
                     printer.print(document, documentModel.openDraft(document.getId()));
                 }
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Print a container version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @param printer
      *            An <code>Printer</code>.
      */
     public void printVersion(final Long containerId, final Long versionId,
             final ContainerVersionPrinter printer) {
         try {
             final InternalDocumentModel documentModel = getDocumentModel();
             final List<DocumentVersion> documentVersions =
                 containerIO.readDocumentVersions(containerId, versionId);
             final ContainerVersion previousVersion =
                 readPreviousVersion(containerId, versionId);
             final Map<DocumentVersion, Delta> versionDeltas;
             if (null == previousVersion) {
                 versionDeltas = readDocumentVersionDeltas(containerId, versionId);
             } else {
                 versionDeltas = readDocumentVersionDeltas(containerId, versionId,
                         previousVersion.getVersionId());
             }
             for (final DocumentVersion documentVersion : documentVersions) {
                 if (Delta.REMOVED != versionDeltas.get(documentVersion)) {
                     documentModel.openVersion(documentVersion.getArtifactId(),
                             documentVersion.getVersionId(), new StreamOpener() {
                         public void open(final InputStream stream)
                                         throws IOException {
                             printer.print(documentVersion, stream);
                         }
                     });
                 }
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#publish(com.thinkparity.ophelia.model.util.ProcessMonitor,
      *      java.lang.Long, java.util.List, java.util.List, java.util.List)
      * 
      */
     public void publish(final ProcessMonitor monitor, final Long containerId,
             final String versionName, final List<EMail> emails,
             final List<Contact> contacts, final List<TeamMember> teamMembers)
             throws CannotLockException {
         try {
             final ContainerDraft draft = readDraft(containerId);
             // publish
             final Publish delegate = createDelegate(Publish.class);
             delegate.setContacts(contacts);
             delegate.setContainerId(containerId);
             delegate.setEmails(emails);
             delegate.setMonitor(monitor);
             delegate.setVersionName(versionName);
             delegate.setTeamMembers(teamMembers);
             delegate.publish();
             // fire event
             final Container container = read(containerId);
             final ContainerVersion version = readLatestVersion(containerId);
             final ContainerVersion previousVersion = readPreviousVersion(
                     containerId, version.getVersionId());
             notifyContainerPublished(container, draft, previousVersion,
                     version, null, localTeamMember(container.getId()),
                     delegate.getInvitations(), localEventGenerator);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         } finally {
             notifyProcessEnd(monitor);
         }
     }
 
     /**
      * Publish the container version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @param contacts
      *            A contact <code>List</code>.
      */
     public void publishVersion(final ProcessMonitor monitor,
             final Long containerId, final Long versionId,
             final List<EMail> emails, final List<Contact> contacts,
             final List<TeamMember> teamMembers) {
         try {
             // publish version
             final PublishVersion delegate = createDelegate(PublishVersion.class);
             delegate.setContacts(contacts);
             delegate.setContainerId(containerId);
             delegate.setEmails(emails);
             delegate.setMonitor(monitor);
             delegate.setTeamMembers(teamMembers);
             delegate.setVersionId(versionId);
             delegate.publishVersion();
             // fire event
             final Container container = read(containerId);
             final ContainerVersion version = readVersion(containerId, versionId);
             final ContainerVersion previousVersion = readPreviousVersion(containerId, versionId);
             final ContainerVersion nextVersion = readNextVersion(containerId, versionId);
             final TeamMember localTeamMember = localTeamMember(containerId);
             notifyContainerPublished(container, null, version, previousVersion,
                     nextVersion, localTeamMember, localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         } finally {
             notifyProcessEnd(monitor);
         }
     }
 
     /**
      * Read the containers.
      * 
      * @return A list of containers.
      */
     public List<Container> read() {
         logger.logApiId();
         return read(defaultComparator);
     }
 
     /**
      * Read the containers.
      * 
      * @param comparator
      *            A sort ordering to user.
      * @return A list of containers.
      */
     public List<Container> read(final Comparator<Artifact> comparator) {
         logger.logApiId();
         logger.logVariable("comparator", comparator);
         return read(comparator, defaultFilter);
     }
 
     /**
      * Read the containers.
      * 
      * @param comparator
      *            A sort ordering to user.
      * @param filter
      *            A filter to apply.
      * @return A list of containers.
      */
     public List<Container> read(final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         try {
             final List<Container> containers = containerIO.read();
             FilterManager.filter(containers, filter);
             ModelSorter.sortContainers(containers, comparator);
             return containers;        
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the containers.
      * 
      * @param filter
      *            A filter to apply.
      * @return A list of containers.
      */
     public List<Container> read(final Filter<? super Artifact> filter) {
         logger.logApiId();
         logger.logVariable("filter", filter);
         return read(defaultComparator, filter);
     }
 
     /**
      * Read a container.
      * 
      * @param containerId
      *            A container id.
      * @return A container.
      */
     public Container read(final Long containerId) {
         try {
             return containerIO.read(containerId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @return A list of documents.
      */
     public List<Document> readDocuments(final Long containerId, final Long versionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         return readDocuments(containerId, versionId, defaultDocumentComparator,
                 defaultDocumentFilter);
     }
 
     /**
      * Read the delta for a version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param compareVersionId
      *            A container compare version id <code>Long</code>.
      * @return A <code>ContainerVersionDelta</code>.
      */
     public Map<DocumentVersion, Delta> readDocumentVersionDeltas(
             final Long containerId, final Long compareVersionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("compareVersionId", compareVersionId);
         try {
             assertDoesExistVersion("Compare version does not exist.", containerId, compareVersionId);
             final Map<DocumentVersion, Delta> deltas = new TreeMap<DocumentVersion, Delta>(
                     new ComparatorBuilder().createVersionByName());
             // only one version exists therefore all addeds
             final List<DocumentVersion> versions = readDocumentVersions(containerId, compareVersionId);
             for (final DocumentVersion version : versions) {
                 deltas.put(version, Delta.ADDED);
             }
             return deltas;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the delta between two versions.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param compareVersionId
      *            A container compare version id <code>Long</code>.
      * @param compareToVersionId
      *            A container compare to version id <code>Long</code>.
      * @return A <code>ContainerVersionDelta</code>.
      */
     public Map<DocumentVersion, Delta> readDocumentVersionDeltas(
             final Long containerId, final Long compareVersionId,
             final Long compareToVersionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("compareVersionId", compareVersionId);
         logger.logVariable("compareToVersionId", compareToVersionId);
         try {
             assertDoesExistVersion("Compare version does not exist.", containerId, compareVersionId);
             assertDoesExistVersion("Compare to version does not exist.", containerId, compareVersionId);
             final Map<DocumentVersion, Delta> deltas = new TreeMap<DocumentVersion, Delta>(
                     new ComparatorBuilder().createVersionByName());
             // the two versions exist
             final ContainerVersionDelta delta = containerIO.readDelta(
                     containerId, compareVersionId, compareToVersionId);
             for (final ContainerVersionArtifactVersionDelta artifactDelta :
                 delta.getDeltas()) {
                 deltas.put(documentIO.getVersion(artifactDelta.getArtifactId(),
                         artifactDelta.getArtifactVersionId()),
                         artifactDelta.getDelta());
             }
             return deltas;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the document versions for a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @return A list of documents.
      */
     public List<DocumentVersion> readDocumentVersions(final Long containerId,
             final Long versionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         return readDocumentVersions(containerId, versionId,
                 defaultDocumentVersionComparator);
     }
 
     /**
      * Read the document versions for a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param comparator
      *            A document comparator.
      * @return A list of documents.
      */
     public List<DocumentVersion> readDocumentVersions(final Long containerId,
             final Long versionId, final Comparator<ArtifactVersion> comparator) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         logger.logVariable("comparator", comparator);
         return readDocumentVersions(containerId, versionId, comparator,
                 FilterManager.createDefault());
     }
 
     /**
      * Read the document versions for a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param comparator
      *            A document comparator.
      * @param filter
      *            A document filter.
      * @return A list of documents.
      */
     public List<DocumentVersion> readDocumentVersions(final Long containerId,
             final Long versionId, final Comparator<ArtifactVersion> comparator,
             final Filter<? super ArtifactVersion> filter) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         logger.logVariable("comparator", comparator);
         logger.logVariable("filter", filter);
         try {
             final List<DocumentVersion> documentVersions =
                 containerIO.readDocumentVersions(containerId, versionId);
             FilterManager.filter(documentVersions, filter);
             ModelSorter.sortDocumentVersions(documentVersions, comparator);
             return Collections.unmodifiableList(documentVersions);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the document versions for a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param filter
      *            A document filter.
      * @return A list of document versions.
      */
     public List<DocumentVersion> readDocumentVersions(final Long containerId,
             final Long versionId, final Filter<? super ArtifactVersion> filter) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         logger.logVariable("filter", filter);
         return readDocumentVersions(containerId, versionId,
                 defaultDocumentVersionComparator, filter);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readDraft(java.lang.Long)
      *
      */
     public ContainerDraft readDraft(Long containerId) {
         return readDraft(containerId, defaultComparator, defaultFilter);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readDraft(java.lang.Long, java.util.Comparator)
      *
      */
     public ContainerDraft readDraft(final Long containerId,
             final Comparator<Artifact> comparator) {
         return readDraft(containerId, comparator, defaultFilter);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readDraft(java.lang.Long)
      * 
      */
     public ContainerDraft readDraft(final Long containerId,
             final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         try {
             final InternalDocumentModel documentModel = getDocumentModel();
             final ContainerDraft draft = containerIO.readDraft(containerId);
     
             if (null != draft) {
                 final List<Document> documents = new ArrayList<Document>();
                 documents.addAll(draft.getDocuments());
                 FilterManager.filter(documents, filter);
                 ModelSorter.sortDocuments(documents, comparator);
                 draft.setDocuments(documents);
             }
     
             if (null != draft) {
                 for (final Document document : draft.getDocuments()) {
                     if (ContainerDraft.ArtifactState.NONE == draft.getState(document)) {
                         if (documentModel.isDraftModified(document.getId())) {
                             draft.putState(document, ContainerDraft.ArtifactState.MODIFIED);
                         }
                     }
                 }
             }
             return draft;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readDraft(java.lang.Long, com.thinkparity.codebase.filter.Filter)
      *
      */
     public ContainerDraft readDraft(final Long containerId,
             final Filter<? super Artifact> filter) {
         return readDraft(containerId, defaultComparator, defaultFilter);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#readForDocument(java.lang.Long)
      *
      */
     public List<Container> readForDocument(final Long documentId) {
         try {
             final List<ContainerVersion> versions = containerIO.readVersionsForDocument(documentId);
             final List<Container> containers = new ArrayList<Container>(versions.size());
             // add unique containers only
             for (final ContainerVersion version : versions)
                 if (!ARTIFACT_UTIL.contains(containers, version))
                     containers.add(read(version.getArtifactId()));
             return containers;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#readForTeamMember(java.lang.Long)
      *
      */
     public List<Container> readForTeamMember(final Long teamMemberId) {
         try {
             return containerIO.readForTeamMember(teamMemberId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the latest container version.
      * 
      * @param containerId
      *            A container id.
      * @return A container version.
      */
     public ContainerVersion readLatestVersion(final Long containerId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         if (doesExistLatestVersion(containerId)) {
             return containerIO.readLatestVersion(containerId);
         } else {
             return null;
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readNextVersion(java.lang.Long,
      *      java.lang.Long)
      * 
      */
     public ContainerVersion readNextVersion(final Long containerId,
             final Long versionId) {
         try {
             final Long nextVersionId =
                 artifactIO.readNextVersionId(containerId, versionId);
             if (null != nextVersionId
                     && doesExistVersion(containerId, nextVersionId)) {
                 return containerIO.readVersion(containerId, nextVersionId);
             } else {
                 return null;
             }     
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the next container version sequentially.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>ContainerVersion</code>.
      */
     public ContainerVersion readPreviousVersion(final Long containerId,
             final Long versionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         try {
             final Long previousVersionId =
                 artifactIO.readPreviousVersionId(containerId, versionId);
             if (null != previousVersionId
                     && doesExistVersion(containerId, previousVersionId)) {
                 return containerIO.readVersion(containerId, previousVersionId);
             } else {
                 return null;
             }     
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishedTo(java.lang.Long,
      *      java.lang.Long)
      * 
      */
     public List<ArtifactReceipt> readPublishedTo(final Long containerId,
             final Long versionId) {
         try {
             return readPublishedTo(containerId, versionId,
                     defaultReceiptComparator, defaultReceiptFilter);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishedTo(java.lang.Long,
      *      java.lang.Long, java.util.Comparator)
      * 
      */
     public List<ArtifactReceipt> readPublishedTo(final Long containerId,
             final Long versionId, final Comparator<ArtifactReceipt> comparator) {
         try {
             return readPublishedTo(containerId, versionId, comparator,
                     defaultReceiptFilter);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishedTo(java.lang.Long,
      *      java.lang.Long, java.util.Comparator,
      *      com.thinkparity.codebase.filter.Filter)
      * 
      */
     public List<ArtifactReceipt> readPublishedTo(final Long containerId,
             final Long versionId, final Comparator<ArtifactReceipt> comparator,
             final Filter<? super ArtifactReceipt> filter) {
         try {
             final List<ArtifactReceipt> publishedTo =
                 containerIO.readPublishedToReceipts(containerId, versionId);
             // sort by user
             ModelSorter.sortReceipts(publishedTo, new Comparator<ArtifactReceipt>() {
                 public int compare(final ArtifactReceipt o1, final ArtifactReceipt o2) {
                     return o1.getUser().getLocalId().compareTo(o2.getUser().getLocalId());
                 }
             });
             // remote duplicate users
             ArtifactReceipt previousReceipt = null, receipt = null;
             for (final Iterator<ArtifactReceipt> iPublishedTo =
                 publishedTo.iterator(); iPublishedTo.hasNext();) {
                 receipt = iPublishedTo.next();
                 if (null != previousReceipt
                         && previousReceipt.getUser().equals(receipt.getUser())) {
                     iPublishedTo.remove();
                 }
                 previousReceipt = receipt;
             }
             FilterManager.filter(publishedTo, filter);
             ModelSorter.sortReceipts(publishedTo, comparator);
             return publishedTo;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishedTo(java.lang.Long,
      *      java.lang.Long, com.thinkparity.codebase.filter.Filter)
      * 
      */
     public List<ArtifactReceipt> readPublishedTo(final Long containerId,
             final Long versionId, final Filter<? super ArtifactReceipt> filter) {
         try {
             return readPublishedTo(containerId, versionId,
                     defaultReceiptComparator, filter); 
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishedToEMails(java.lang.Long, java.lang.Long)
      *
      */
     public List<PublishedToEMail> readPublishedToEMails(
             final Long containerId, final Long versionId) {
         try {
             return containerIO.readPublishedToEMails(containerId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#readPublishedToUsers(java.lang.Long, java.lang.Long)
      *
      */
     public List<User> readPublishedToUsers(final Long containerId,
             final Long versionId) {
         try {
             return containerIO.readPublishedTo(containerId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readPublishToTeam(java.lang.Long)
      *
      */
     public List<TeamMember> readPublishToTeam(final Long containerId) {
         try {
             /* filter out the team members flagged with resitricted publish and
              * the local user */
             final FilterChain<User> filter = new FilterChain<User>();
             filter.addFilter(UserFilterManager.createLocalUser(localUser()));
             filter.addFilter(UserFilterManager.createContainerPublishTo());
             return getArtifactModel().readTeam(containerId,
                     UserComparatorFactory.createName(Boolean.TRUE), filter);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#readTeam(java.lang.Long)
      * 
      */
     public List<TeamMember> readTeam(final Long containerId) {
         return getArtifactModel().readTeam(containerId,
                 UserComparatorFactory.createName(Boolean.TRUE),
                 FilterManager.createDefault());
     }
 
     /**
      * Read a container version.
      * 
      * @param containerId
      *            The container id.
      * @param versionId
      *            The version id.
      * @return A container version.
      */
     public ContainerVersion readVersion(final Long containerId, final Long versionId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         return containerIO.readVersion(containerId, versionId);
     }
 
     /**
      * Read the container versions.
      * 
      * @param containerId
      *            The container id.
      * @return A list of container versions.
      */
     public List<ContainerVersion> readVersions(final Long containerId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         return readVersions(containerId, defaultVersionComparator);
     }
 
     /**
      * Read a list of versions for the container.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A container version comparator.
      * @return A list of versions.
      */
     public List<ContainerVersion> readVersions(final Long containerId,
             final Comparator<ArtifactVersion> comparator) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("comparator", comparator);
         return readVersions(containerId, comparator, defaultVersionFilter);
     }
 
     /**
      * Read a list of versions for the container.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A container version comparator.
      * @param filter
      *            A container version filter.
      * @return A list of versions.
      */
     public List<ContainerVersion> readVersions(final Long containerId,
             final Comparator<ArtifactVersion> comparator,
             final Filter<? super ArtifactVersion> filter) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("comparator", comparator);
         logger.logVariable("filter", filter);
         final List<ContainerVersion> versions = containerIO.readVersions(containerId);
         FilterManager.filter(versions, filter);
         ModelSorter.sortContainerVersions(versions, comparator);
         return versions;
     }
 
     /**
      * Read a list of versions for the container.
      * 
      * @param containerId
      *            A container id.
      * @param filter
      *            A container version filter.
      * 
      * @return A list of versions.
      */
     public List<ContainerVersion> readVersions(final Long containerId,
             final Filter<? super ArtifactVersion> filter) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("filter", filter);
         return readVersions(containerId, defaultVersionComparator, filter);
     }
 
     /**
      * Remove a bookmark from a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      */
     public void removeBookmark(final Long containerId) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         try {
             getArtifactModel().removeFlagBookmark(containerId);
             notifyContainerFlagged(read(containerId), localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#removeDocument(java.lang.Long,
      *      java.lang.Long)
      * 
      */
     public void removeDocument(final Long containerId, final Long documentId)
             throws CannotLockException {
         try {
             assertContainerDraftExists(containerId,
                     "Draft for {0} does not exist.", containerId);
             assertDraftArtifactStateTransition("INVALID DRAFT DOCUMENT STATE",
                     containerId, documentId, ContainerDraft.ArtifactState.REMOVED);
             final ContainerDraft draft = readDraft(containerId);
             final Document document = draft.getDocument(documentId);
             final DocumentFileLock lock = lockDocument(document);
             final Map<DocumentVersion, DocumentFileLock> versionLocks = lockDocumentVersions(document);
             try {
                 containerIO.deleteDraftArtifactRel(containerId, document.getId());
                 switch (draft.getState(document.getId())) {
                 case ADDED:     // delete the document
                     containerIO.deleteDraftDocument(containerId, document.getId());
                     getDocumentModel().delete(lock, versionLocks, document.getId());
                     break;
                 case MODIFIED:  // fall through
                 case NONE:
                     containerIO.createDraftArtifactRel(
                             containerId, document.getId(), ContainerDraft.ArtifactState.REMOVED);
                     containerIO.deleteDraftDocument(containerId, document.getId());
                     getDocumentModel().remove(lock, document.getId());
                     break;
                 case REMOVED:   // fall through
                 default:
                     Assert.assertUnreachable("UNKNOWN ARTIFACT STATE");
                 }
                 // fire event
                 final Container postAdditionContainer = read(containerId);        
                 final ContainerDraft postAdditionDraft = readDraft(containerId);
                 notifyDocumentRemoved(postAdditionContainer, postAdditionDraft, document, localEventGenerator);
             } finally {
                 releaseLock(lock);
                 releaseLocks(versionLocks.values());
             }
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Remove a container listener.
      * 
      * @param listener
      *            A <code>ContainerListener</code>.
      */
     @Override
     public void removeListener(final ContainerListener listener) {
         super.removeListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#rename(java.lang.Long,
      *      java.lang.String)
      * 
      */
     public void rename(final Long containerId, final String name) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("name", name);
         try {
             assertIsNotDistributed("Container has already been distributed.",
                     containerId);
             containerIO.updateName(containerId, name);
             // index
             getIndexModel().indexContainer(containerId);
             // fire event
             notifyContainerRenamed(read(containerId), localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#renameDocument(java.lang.Long,
      *      java.lang.Long, java.lang.String)
      * 
      */
     public void renameDocument(final Long containerId, final Long documentId,
             final String name) throws CannotLockException {
         try {
             getDocumentModel().rename(documentId, name);
             // index
             getIndexModel().indexDocument(containerId, documentId);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#restore(java.lang.Long)
      *
      */
     public void restore(final Long containerId) {
         try {
             assertIsDistributed("Container has not been distributed.", containerId);
 
             final InternalArtifactModel artifactModel = getArtifactModel();
             artifactModel.removeFlagArchived(containerId);
             // restore the draft if one existed
             final InternalSessionModel sessionModel = getSessionModel();
             final JabberId draftOwner = sessionModel.readKeyHolder(
                     artifactModel.readUniqueId(containerId));
             if (draftOwner.equals(User.THINKPARITY.getId())) {
                 logger.logInfo("No remote draft exists for {0}.", containerId);
             } else {
                 final List<TeamMember> team = readTeam(containerId);
                 final ContainerDraft draft = new ContainerDraft();
                 draft.setLocal(Boolean.FALSE);
                 draft.setContainerId(containerId);
                 draft.setOwner(team.get(indexOf(team, draftOwner)));
                 containerIO.createDraft(draft);
             }
             getBackupModel().restore(containerId);
             sessionModel.addTeamMember(
                     artifactModel.readUniqueId(containerId),
                     artifactModel.readTeamIds(containerId), localUserId());
             // note the order of this add; it is important that it happen after
             // the remote add
             artifactModel.addTeamMember(containerId, localUserId());
 
             notifyContainerRestored(read(containerId), localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.InternalContainerModel#restoreBackup(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void restoreBackup(final ProcessMonitor monitor) {
         try {
             final RestoreBackup delegate = createDelegate(RestoreBackup.class);
             delegate.setMonitor(monitor);
             delegate.restoreBackup();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#restoreDraft(java.lang.Long)
      *
      */
     public void restoreDraft(final Long containerId) throws CannotLockException {
         try {
             assertDoesExistLocalDraft("A local draft does not exist.", containerId);
             Assert.assertTrue(isLocalDraftSaved(containerId), "The local draft has not been saved.");
             final InternalDocumentModel documentModel = getDocumentModel();
             final ContainerDraft draft = readDraft(containerId);
             final Map<Document, DocumentFileLock> locks = lockDocuments(draft.getDocuments());
             try {
                 for (final Document document : draft.getDocuments()) {
                     containerIO.openDraftDocument(containerId, document.getId(), new StreamOpener() {
                         public void open(InputStream stream) throws IOException {
                             try {
                                 documentModel.updateDraft(locks.get(document),
                                         document.getId(), stream);
                             } finally {
                                 stream.close();
                             }
                         }
                     });
                 }
             } finally {
                 releaseLocks(locks.values());
             }
         } catch (final CannotLockException clx) { 
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
 
     }
 
     /**
      * Revert a document to it's pre-draft state. A document must either be
      * modified or removed in order to be reverted. If a document has been
      * modified we simply overwrite the changes made with the content from the
      * latest version, if it has been removed, we restore it.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @throws CannotLockException
      *             if the document cannot be locked
      */
     public void revertDocument(final Long containerId, final Long documentId)
             throws CannotLockException {
         try {
             assertContainerDraftExists(containerId,
                     "Draft for {0} does not exist.", containerId);
             assertDraftArtifactStateTransition("Invalid document state.",
                     containerId, documentId, ContainerDraft.ArtifactState.NONE);
             assertDoesExistLatestVersion("Latest version does not exist.", containerId);
             final ContainerDraft draft = readDraft(containerId);
             switch (draft.getState(documentId)) {
             case MODIFIED:
                 revertModifiedDocument(draft, containerId, documentId);
                 break;
             case REMOVED:
                 revertRemovedDocument(draft, containerId, documentId);
                 break;
             default:
                 Assert.assertUnreachable(
                         "Cannot revert a document once it has been {0}.",
                         draft.getState(documentId));
             }
             // fire event
             final Container postRevertContainer = read(containerId);        
             final ContainerDraft postRevertDraft = readDraft(containerId);
             notifyDocumentReverted(postRevertContainer, postRevertDraft,
                     draft.getDocument(documentId), localEventGenerator);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#saveDraft(java.lang.Long)
      * 
      */
     public void saveDraft(final Long containerId) throws CannotLockException {
         try {
             assertDoesExistLocalDraft("A local draft does not exist.", containerId);
             final InternalDocumentModel documentModel = getDocumentModel();
             final ContainerDraft draft = readDraft(containerId);
             final Map<Document, DocumentFileLock> locks = lockDocuments(draft.getDocuments());
             try {
                 ContainerDraftDocument draftDocument;
                 DocumentDraft documentDraft;
                 FileChannel channel;
                 File tempFile;
                 InputStream stream;
                 for (final Document document : draft.getDocuments()) {
                     if (documentModel.isDraftModified(locks.get(document), document.getId())) {
                         documentDraft = documentModel.readDraft(locks.get(document), document.getId());
                         draftDocument = containerIO.readDraftDocument(
                                 containerId, document.getId());
                         draftDocument.setChecksum(documentDraft.getChecksum());
                         draftDocument.setSize(documentDraft.getSize());
                         channel = locks.get(document).getFileChannel(0L);
                         tempFile = workspace.createTempFile();
                         channelToFile(channel, tempFile);
                         try {
                             stream = new FileInputStream(tempFile);
                             try {
                                 containerIO.updateDraftDocument(draftDocument,
                                         stream, getBufferSize());
                             } finally {
                                 stream.close();
                             }
                         } finally {
                             Assert.assertTrue(tempFile.delete(),
                                     "Could not delete temporary file {0}.",
                                     tempFile);
                         }
                     }
                 }
             } finally {
                 releaseLocks(locks.values());
             }
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Search for containers.
      * 
      * @param expression
      *            A search expression <code>String</code>.
      * @return A <code>List&lt;Long&gt;</code>.
      */
     public List<Long> search(final String expression) {
         logger.logApiId();
         logger.logVariable("expression", expression);
         try {
             final InternalIndexModel indexModel = getIndexModel();
             final List<Long> containerIds = indexModel.searchContainers(expression);
             final List<Pair<Long, Long>> compositeIds = indexModel.searchContainerVersions(expression);
             for (final Pair<Long, Long> compositeId : compositeIds) {
                 containerIds.add(compositeId.getOne());
             }
             containerIds.addAll(indexModel.searchDocuments(expression));
             return containerIds;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.container.ContainerModel#updateDraftComment(java.lang.Long, java.lang.String)
      *
      */
     public void updateDraftComment(final Long containerId, final String comment) {
         try {
             final UpdateDraftComment delegate = createDelegate(UpdateDraftComment.class);
             delegate.setComment(comment);
             delegate.setContainerId(containerId);
             delegate.updateDraftComment();
             // fire event
             notifyDraftUpdated(read(containerId), readDraft(containerId),
                     localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#initializeModel(com.thinkparity.codebase.model.session.Environment, com.thinkparity.ophelia.model.workspace.Workspace)
      *
      */
     @Override
     protected void initializeModel(final Environment environment,
             final Workspace workspace) {
         this.artifactIO = IOFactory.getDefault(workspace).createArtifactHandler();
         this.containerIO = IOFactory.getDefault(workspace).createContainerHandler();
         this.documentIO = IOFactory.getDefault(workspace).createDocumentHandler();
     }
 
     /**
      * Calculate a delta between a version and its previous version.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param compare
      *            A <code>ContainerVersion</code>.
      * @param compareTo
      *            Another <code>ContainerVersion</code>.
      * @return A <code>ContainerVersionDelta</code>.
      */
     ContainerVersionDelta calculateDelta(final Container container,
             final ContainerVersion compare, final ContainerVersion compareTo) {
         final ContainerVersionDelta versionDelta = new ContainerVersionDelta();
         versionDelta.setCompareVersionId(compare.getVersionId());
         versionDelta.setCompareToVersionId(compareTo.getVersionId());
         versionDelta.setContainerId(container.getId());
 
         final Delta didNotHit, notContains;
         if (compare.getVersionId() > compareTo.getVersionId()) {
             didNotHit = Delta.REMOVED;
             notContains = Delta.ADDED;
         } else if (compare.getVersionId() < compareTo.getVersionId()) {
             didNotHit = Delta.ADDED;
             notContains = Delta.REMOVED;
         } else {
             throw Assert.createUnreachable("Unexpected equality.");
         }
 
         final List<DocumentVersion> compareDocuments =
             containerIO.readDocumentVersions(
                     versionDelta.getContainerId(),
                     versionDelta.getCompareVersionId());
         final List<DocumentVersion> compareToDocuments =
             containerIO.readDocumentVersions(
                     versionDelta.getContainerId(),
                     versionDelta.getCompareToVersionId());
         ContainerVersionArtifactVersionDelta artifactVersionDelta;
         boolean didHit;
 
         // walk through compare to version's documents
         for (final ArtifactVersion compareToDocument : compareToDocuments) {
             artifactVersionDelta = new ContainerVersionArtifactVersionDelta();
             artifactVersionDelta.setArtifactId(compareToDocument.getArtifactId());
             // walk through compare version's documents
             didHit = false;
             for (final ArtifactVersion versionDocument : compareDocuments) {
                 // the artifact exists in this version; and in the compare version
                 // therefore it must be the same or modified
                 if (versionDocument.getArtifactId().equals(compareToDocument.getArtifactId())) {
                     artifactVersionDelta.setArtifactVersionId(versionDocument.getVersionId());
                     // the version numbers are the same; no change
                     if (versionDocument.getVersionId().equals(compareToDocument.getVersionId())) {
                         artifactVersionDelta.setDelta(Delta.NONE);
                     } else {
                         artifactVersionDelta.setDelta(Delta.MODIFIED);
                     }
                     didHit = true;
                     break;
                 }
             }
             // the document is not in compare's version
             if (!didHit) {
                 artifactVersionDelta.setArtifactVersionId(compareToDocument.getVersionId());
                 artifactVersionDelta.setDelta(didNotHit);
             }
             versionDelta.addDelta(artifactVersionDelta);
         }
         // walk through next version's documents
         for (final ArtifactVersion compareDocument : compareDocuments) {
             if (!versionDelta.containsDelta(compareDocument)) {
                 artifactVersionDelta = new ContainerVersionArtifactVersionDelta();
                 artifactVersionDelta.setArtifactId(compareDocument.getArtifactId());
                 artifactVersionDelta.setArtifactVersionId(compareDocument.getVersionId());
                 artifactVersionDelta.setDelta(notContains);
                 versionDelta.addDelta(artifactVersionDelta);
             }
         }
         return versionDelta;
     }
 
     /**
      * Create the container in the distributed network.
      * 
      * @param container
      *            A <code>Container</code>.
      */
     void createDistributed(final Container container, final Calendar createdOn) {
         final InternalSessionModel sessionModel = getSessionModel();
         sessionModel.createArtifact(localUserId(), container.getUniqueId(),
                 createdOn);
         // TODO update the container's created on date
         // TODO update all documents' created on dates
     }
 
     /**
      * Create a new container version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @param name
      *            A name <code>String</code>.
      * @param comment
      *            A comment <code>String</code>.
      * @param createdBy
      *            The created by user id <code>JabberId</code>.
      * @param createdOn
      *            The created on <code>Calendar</code>.
      * @return The new <code>ContainerVersion</code>.
      */
     ContainerVersion createVersion(final Long containerId,
             final Long versionId, final String name, final String comment,
             final JabberId createdBy, final Calendar createdOn) {
         final Container container = read(containerId);
 
         final ContainerVersion version = new ContainerVersion();
         version.setArtifactId(container.getId());
         version.setArtifactName(container.getName());
         version.setArtifactType(container.getType());
         version.setArtifactUniqueId(container.getUniqueId());
         version.setComment(comment);
         version.setCreatedBy(createdBy);
         version.setCreatedOn(createdOn);
         version.setName(name);
         version.setUpdatedBy(version.getCreatedBy());
         version.setUpdatedOn(version.getCreatedOn());
         version.setVersionId(versionId);
         containerIO.createVersion(version);
         // index
         getIndexModel().indexContainerVersion(new Pair<Long, Long>(containerId,
                 versionId));
         return containerIO.readVersion(version.getArtifactId(),
                 version.getVersionId());
     }
 
     /**
      * Delete the local info for this container.
      * 
      * @param containerId
      *            The container id.
      */
     void deleteLocal(final Long containerId, final List<Document> documents,
             final Map<Document, DocumentFileLock> documentLocks,
             final Map<DocumentVersion, DocumentFileLock> documentVersionLocks) {
         // delete the draft
         final ContainerDraft draft = readDraft(containerId);
         if (null != draft) {
             for(final Document document : draft.getDocuments()) {
                 containerIO.deleteDraftArtifactRel(containerId, document.getId());
             }
             containerIO.deleteDraftDocuments(containerId);
             containerIO.deleteDraft(containerId);
         }
         // delete the team
         final InternalArtifactModel artifactModel = getArtifactModel();
         artifactModel.deleteTeam(containerId);
         // delete versions
         final InternalDocumentModel documentModel = getDocumentModel();
         final List<ContainerVersion> versions = readVersions(containerId);
         for (final ContainerVersion version : versions) {
             // remove the version's artifact versions
             containerIO.removeVersions(containerId, version.getVersionId());
             // delete the version's deltas
             containerIO.deleteDeltas(containerId, version.getVersionId());
             // delete the version
             containerIO.deleteVersion(containerId, version.getVersionId());
         }
         // delete documents
         for (final Document document : documents) {
             documentModel.delete(documentLocks.get(document),
                     documentVersionLocks, document.getId());
         }
         // delete the index
         getIndexModel().deleteContainer(containerId);
         // delete the container
         containerIO.delete(containerId);
     }
 
     /**
      * Obtain the artfiact persitence io.
      * 
      * @return An instance of <code>ArtifactIOHandler</code>.
      */
     ArtifactIOHandler getArtifactIO() {
         return artifactIO;
     }
 
     /**
      * Obtain containerConstraints.
      *
      * @return A ContainerConstraints.
      */
     ContainerConstraints getContainerConstraints() {
         return containerConstraints;
     }
 
     /**
      * Obtain the container persitence io.
      * 
      * @return An instance of <code>ContainerIOHandler</code>.
      */
     ContainerIOHandler getContainerIO() {
         return containerIO;
     }
 
     /**
      * Obtain the document persitence io.
      * 
      * @return An instance of <code>DocumentIOHandler</code>.
      */
     DocumentIOHandler getDocumentIO() {
         return documentIO;
     }
 
     /**
      * Create the requisite document versions if required.
      * 
      * @param containerVersion
      *            A <code>ContainerVersion</code>.
      * @param versions
      *            A <code>Map</code> of <code>DocumentVersion</code>s to
      *            their stream id <code>String</code>s.
      * @param publishedBy
      *            A published by user id <code>JabberId</code>.
      * @param publishedOn
      *            A published on <code>Calendar</code>.
      */
     void handleDocumentVersionsResolution(
             final ContainerVersion containerVersion,
             final Map<DocumentVersion, String> versions,
             final JabberId publishedBy, final Calendar publishedOn) {
         for (final Entry<DocumentVersion, String> entry : versions.entrySet()) {
             final ArtifactVersion artifactVersion =
                 modelFactory.getDocumentModel().handleDocumentPublished(
                         containerVersion.getArtifactId(), entry.getKey(),
                         entry.getValue(), publishedBy, publishedOn);
             final Long artifactId = modelFactory.getArtifactModel().readId(
                     entry.getKey().getArtifactUniqueId());
             logger.logVariable("artifactId", artifactId);
             if (!containerIO.doesExistVersion(containerVersion.getArtifactId(),
                     containerVersion.getVersionId(), artifactId,
                     entry.getKey().getVersionId()).booleanValue()) {
                 containerIO.addVersion(containerVersion.getArtifactId(),
                         containerVersion.getVersionId(),
                         artifactVersion.getArtifactId(),
                         artifactVersion.getVersionId(),
                         artifactVersion.getArtifactType());
             }
         }
     }
 
     /**
      * Handle the resolution of a container. If the container does not exist it
      * will be created.
      * 
      * @param event
      *            A <code>ContainerPublishedEvent</code>.
      * @return A <code>Container</code>.
      */
     Container handleResolution(final UUID uniqueId, final JabberId publishedBy,
             final Calendar publishedOn, final String name) {
         // determine the existance of the container and the version.
         final InternalArtifactModel artifactModel = modelFactory.getArtifactModel();
         final boolean doesExist = artifactModel.doesExist(uniqueId).booleanValue();
         final Container container;
         if (doesExist) {
             container = read(artifactModel.readId(uniqueId));
         } else {
             // ensure the published by user exists locally
             modelFactory.getUserModel().readLazyCreate(publishedBy);
     
             container = new Container();
             container.setCreatedBy(publishedBy);
             container.setCreatedOn(publishedOn);
             container.setName(name);
             container.setState(ArtifactState.ACTIVE);
             container.setType(ArtifactType.CONTAINER);
             container.setUniqueId(uniqueId);
             container.setUpdatedBy(container.getCreatedBy());
             container.setUpdatedOn(container.getCreatedOn());
             // create
             containerIO.create(container);
     
             // index
             modelFactory.getIndexModel().indexContainer(container.getId());
         }
         return container;
     }
 
     /**
      * Determine whether or not the local draft has been saved.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the draft has beeen saved.
      */
     Boolean isLocalDraftSaved(final Long containerId) {
         boolean saved = true;
         final InternalDocumentModel documentModel = modelFactory.getDocumentModel();
         final ContainerDraft draft = readDraft(containerId);
         DocumentDraft documentDraft;
         ContainerDraftDocument draftDocument;
         for (final Document document : draft.getDocuments()) {
             documentDraft = documentModel.readDraft(document.getId());
             if (null == documentDraft) {
                 saved &= true;
             } else {
                 draftDocument = containerIO.readDraftDocument(containerId, document.getId());
                 if (null == draftDocument) {
                     saved &= true;
                 } else {
                     saved &= documentDraft.getChecksum().equals(
                             draftDocument.getChecksum());
                 }
             }
         }
         return Boolean.valueOf(saved);
     }
 
     /**
      * Lock a document.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A <code>DocumentLock</code>.
      */
     DocumentFileLock lockDocument(final Document document)
             throws CannotLockException {
         return getDocumentModel().lock(document);
     }
 
     /**
      * Lock a list of documents.
      * 
      * @param documents
      *            A <code>List</code> of <code>Document</code>s.
      * @return A <code>Map</code> of <code>Document</code>s to their
      *         <code>DocumentLock</code>s.
      */
     Map<Document, DocumentFileLock> lockDocuments(final List<Document> documents)
             throws CannotLockException {
         final Map<Document, DocumentFileLock> locks = new HashMap<Document, DocumentFileLock>();
         for (final Document document : documents) {
             try {
             	locks.put(document, lockDocument(document));
             } catch (final CannotLockException clx) {
             	releaseLocks(locks.values());
             	throw clx;
             }
         }
         return locks;
     }
 
     /**
      * Lock a document's versions.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A <code>Map</code> of <code>DocumentVersion</code>s to their
      *         <code>DocumentVersionLock</code>s.
      */
     Map<DocumentVersion, DocumentFileLock> lockDocumentVersions(
             final Document document) throws CannotLockException {
         final List<DocumentVersion> versions = getDocumentModel().readVersions(document.getId());
         final Map<DocumentVersion, DocumentFileLock> locks = new HashMap<DocumentVersion, DocumentFileLock>(versions.size(), 1.0F);
         for (final DocumentVersion version : versions) {
             locks.put(version, getDocumentModel().lockVersion(version));
         }
         return locks;
     }
 
     /**
      * Read a flat list of all documents for a container. This will look in the
      * draft as well as all versions.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>List</code> of <code>Document</code>s.
      */
     List<Document> readAllDocuments(final Long containerId) {
         final List<Document> allDocuments = new ArrayList<Document>();
         final ContainerDraft draft = readDraft(containerId);
         if (null != draft) {
             for(final Document document : draft.getDocuments()) {
                 if (!allDocuments.contains(document))
                     allDocuments.add(document);
             }
         }
         final List<ContainerVersion> versions = readVersions(containerId);
         for (final ContainerVersion version : versions)
             for (final Document document :
                 readDocuments(version.getArtifactId(), version.getVersionId()))
                 if (!allDocuments.contains(document))
                     allDocuments.add(document);
         return allDocuments;
     }
 
     /**
      * Release the exclusive lock.
      * 
      * @param lock
      *            A <code>DocumentLock</code>.
      */
     void releaseLock(final DocumentFileLock lock) {
         try {
             lock.release();
         } catch (final IOException iox) {
             logger.logError("Could not release lock.", iox);
         }
     }
 
     /**
      * Release the exclusive locks.
      * 
      * @param locks
      *            A <code>Iterable</code> set of <code>DocumentLock</code>s.
      */
     void releaseLocks(final Iterable<DocumentFileLock> locks) {
         for (final DocumentFileLock lock : locks) {
             releaseLock(lock);
         }
     }
 
     /**
      * Upload a list of document versions to the streaming server.
      * 
      * @param monitor
      *            A <code>ProcessMonitor</code>.
      * @param versions
      *            A <code>List</code> of <code>DocumentVersion</code>s.
      * @return A <code>Map</code> of <code>DocumentVersion</code>s and
      *         their stream id <code>String</code>.s
      */
     Map<DocumentVersion, String> uploadDocumentVersions(
             final ProcessMonitor monitor, final List<DocumentVersion> versions) {
         final Map<DocumentVersion, String> versionStreams =
                 new HashMap<DocumentVersion, String>(versions.size(), 1.0F);
         // set fixed progress determination
         notifyDetermine(monitor, countSteps(versions));
         // upload versions
         final InternalSessionModel sessionModel = modelFactory.getSessionModel();
         final InternalDocumentModel documentModel = modelFactory.getDocumentModel();
         final StreamSession streamSession = sessionModel.createStreamSession();
         for (final DocumentVersion version : versions) {
             notifyStepBegin(monitor, PublishStep.UPLOAD_STREAM, version.getArtifactName());
             final String streamId = sessionModel.createStream(streamSession);
             documentModel.uploadVersion(version.getArtifactId(),
                     version.getVersionId(), new StreamUploader(this, monitor,
                             streamId, streamSession, version.getSize()));
             versionStreams.put(version, streamId);
             notifyStepEnd(monitor, PublishStep.UPLOAD_STREAM);
         }
         sessionModel.deleteStreamSession(streamSession);
         return versionStreams;
     }
 
     /**
      * Add an export resource to the resource map. The name will be used to
      * lookup the resource within the classpath as well as the name of the
      * resource within the map.
      * 
      * @param fileSystem
      *            The export <code>FileSystem</code>.
      * @param resources
      *            The existing export <code>Map</code> of resource name
      *            <code>String</code> to the <code>File</code>.
      * @param name
      *            The resource name <code>String</code>.
      * @param path
      *            The resource path <code>String</code>.
      * @throws IOException
      */
     private void addExportResource(final FileSystem fileSystem,
             final Map<String, File> resources, final String name,
             final String path) throws IOException {
         final File file;
         if (null != fileSystem.find(path)) {
             final File existingFile = fileSystem.find(path);
             Assert.assertTrue(existingFile.delete(),
                     "Cannot delete existing export resource.", existingFile);
         }
         file = fileSystem.createFile(path);
         final InputStream stream = ResourceUtil.getInputStream(path);
         try {
             streamToFile(stream, file);
             resources.put(name, file);
         } finally {
             stream.close();
         }
     }
 
     /**
      * Assert that a draft exists.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param assertMessage
      *            An assertion message <code>String</code>.
      * @param assertArguments
      *            Optional assertion message argument <code>Object[]</code>.
      */
     private void assertDoesExistDraft(final Long containerId,
             final String assertMessage, final Object... assertArguments) {
         Assert.assertTrue(doesExistDraft(containerId), assertMessage,
                 assertArguments);
     }
 
     /**
      * Assert that a local draft exists.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      */
     private void assertDoesExistLocalDraft(final Object assertion,
             final Long containerId) {
         Assert.assertTrue(assertion, doesExistLocalDraft(containerId));
     }
 
     /**
      * Assert the state transition for a draft artifact is valid.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      * @param artifactId
      *            An artifact id.
      * @param targetState
      *            The target state.
      */
     private void assertDraftArtifactStateTransition(final Object assertion,
             final Long containerId, final Long artifactId,
             final ContainerDraft.ArtifactState targetState) {
         final ContainerDraft draft = readDraft(containerId);
         final Artifact artifact = draft.getArtifact(artifactId);
         switch (draft.getState(artifact)) {
         case ADDED:
             switch (targetState) {
             case ADDED:
                 Assert.assertUnreachable(assertion);
                 break;
             case MODIFIED:
                 Assert.assertUnreachable(assertion);
                 break;
             case REMOVED:   // valid state
                 break;
             case NONE:
                 Assert.assertUnreachable(assertion);
                 break;
             default:
                 Assert.assertUnreachable("UNKNOWN STATE");
             }
             break;
         case MODIFIED:
             switch (targetState) {
             case ADDED:
                 Assert.assertUnreachable(assertion);
                 break;
             case MODIFIED:
                 Assert.assertUnreachable(assertion);
                 break;
             case REMOVED:   // valid state
                 break;
             case NONE:      // valid state
                 break;
             default:
                 Assert.assertUnreachable("UNKNOWN STATE");
             }
             break;
         case REMOVED:
             switch (targetState) {
             case ADDED:     // valid state
                 break;
             case MODIFIED:
                 Assert.assertUnreachable(assertion);
                 break;
             case REMOVED:
                 Assert.assertUnreachable(assertion);
                 break;
             case NONE:      // valid state
                 break;
             default:
                 Assert.assertUnreachable("UNKNOWN STATE");
             }
             break;
         case NONE:
             switch (targetState) {
             case ADDED:
                 Assert.assertUnreachable(assertion);
                 break;
             case MODIFIED:  // valid state
                 break;
             case REMOVED:   // valid state
                 break;
             case NONE:
                 Assert.assertUnreachable(assertion);
                 break;
             default:
                 Assert.assertUnreachable("UNKNOWN STATE");
             }
             break;
         default:
             Assert.assertUnreachable("UNKNOWN STATE");
         }
     }
 
     /**
      * Assert that the container has been distributed.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      */
     private void assertIsDistributed(final Object assertion,
             final Long containerId) {
         Assert.assertTrue(assertion, isDistributed(containerId));
     }
 
     /**
      * Assert that the container has been distributed.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      */
     private void assertIsNotDistributed(final Object assertion,
             final Long containerId) {
         Assert.assertNotTrue(assertion, isDistributed(containerId));
     }
 
     /**
      * Create an instance of a delegate.
      * 
      * @param <D>
      *            A delegate type.
      * @param type
      *            The delegate type <code>Class</code>.
      * @return An instance of <code>D</code>.
      */
     private <D extends Delegate<ContainerModelImpl>> D createDelegate(
             final Class<D> type) {
         try {
             final D instance = type.newInstance();
             instance.initialize(this);
             return instance;
         } catch (final IllegalAccessException iax) {
             throw new ThinkParityException("Could not create delegate.", iax);
         } catch (final InstantiationException ix) {
             throw new ThinkParityException("Could not create delegate.", ix);
         }
     }
 
     /**
      * Create a container draft.
      * 
      * @param containerDraftId
      *            A container draft id <code>Long</code>.
      * @param documentId
      *            A document id <code>Long</code>.
      */
     private void createDraftDocument(final Long containerDraftId,
             final Long documentId) throws IOException {
         final DocumentDraft documentDraft = getDocumentModel().readDraft(documentId);
         final ContainerDraftDocument draftDocument = new ContainerDraftDocument();
         draftDocument.setChecksum(documentDraft.getChecksum());
         draftDocument.setChecksumAlgorithm(documentDraft.getChecksumAlgorithm());
         draftDocument.setContainerDraftId(containerDraftId);
         draftDocument.setDocumentId(documentDraft.getDocumentId());
         draftDocument.setSize(documentDraft.getSize());
         final InputStream stream = getDocumentModel().openDraft(documentId);
         try {
             containerIO.createDraftDocument(draftDocument, stream, getBufferSize());
         } finally {
             stream.close();
         }
     }
 
     /**
      * Create the first draft for a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param owner
      *            The draft owner <code>TeamMember</code>.
      * @return A new <code>ContainerDraft</code>.
      * 
      * @see #createDraft(Long)
      * @see #read(Long)
      * @see #notifyDraftCreated(Container, ContainerDraft,
      *      ContainerEventGenerator)
      */
     private ContainerDraft createFirstDraft(final Long containerId,
             final TeamMember owner) {
         final ContainerDraft draft = new ContainerDraft();
         draft.setContainerId(containerId);
         draft.setLocal(Boolean.TRUE);
         draft.setOwner(owner);
         containerIO.createDraft(draft);
         // fire draft event
         final Container postCreation = read(containerId);
         final ContainerDraft postCreationDraft = readDraft(containerId);
         notifyDraftCreated(postCreation, postCreationDraft, localEventGenerator);
         return postCreationDraft;
     }
 
     /**
      * Determine if an artifact still exists as a reference to a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param artifactId
      *            An artifact id <code>Long</code>.
      * @return True if the artifact still exists.
      */
     private boolean doesExistArtifact(final Long containerId,
             final Long artifactId) {
         return containerIO.doesExistArtifact(containerId, artifactId).booleanValue();
     }
 
     /**
      * Export a container and a list of versions.
      * 
      * @param exportDirectory
      *            An export directory <code>File</code>.
      * @param container
      *            A <code>Container</code>.
      * @param versions
      *            A <code>List&lt;ContainerVersion&gt;</code>.
      * @throws IOException
      * @throws TransformerException
      */
     private void export(final OutputStream exportStream,
             final Container container, final List<ContainerVersion> versions)
             throws IOException, TransformerException {
         final ContainerNameGenerator nameGenerator = getNameGenerator();
         final FileSystem exportFileSystem = new FileSystem(
                 workspace.createTempDirectory(
                         nameGenerator.exportDirectoryName(container)));
 
         // prepare the data model
         try {
             final InternalDocumentModel documentModel = getDocumentModel();
             final DocumentNameGenerator documentNameGenerator = documentModel.getNameGenerator();
             final Map<ContainerVersion, User> versionsPublishedBy =
                 new HashMap<ContainerVersion, User>(versions.size(), 1.0F);
             final Map<ContainerVersion, List<DocumentVersion>> documents =
                 new HashMap<ContainerVersion, List<DocumentVersion>>(versions.size(), 1.0F);
             final Map<DocumentVersion, Long> documentsSize = new HashMap<DocumentVersion, Long>();
             final Map<ContainerVersion, List<ArtifactReceipt>> publishedTo =
                 new HashMap<ContainerVersion, List<ArtifactReceipt>>(versions.size(), 1.0F);
             final Map<ContainerVersion, Map<DocumentVersion, Delta>> deltas =
                 new HashMap<ContainerVersion, Map<DocumentVersion, Delta>>(versions.size(), 1.0F);
             File directory;
             for (final ContainerVersion version : versions) {
                 versionsPublishedBy.put(version, readUser(version.getUpdatedBy()));
                 publishedTo.put(version, readPublishedTo(
                         version.getArtifactId(), version.getVersionId()));
                 documents.put(version, readDocumentVersions(
                         version.getArtifactId(), version.getVersionId()));
 
                 final Map<DocumentVersion, Delta> versionDeltas;
                 final ContainerVersion previousVersion =
                     readPreviousVersion(version.getArtifactId(), version.getVersionId());
                 if (null == previousVersion) {
                     versionDeltas = readDocumentVersionDeltas(version.getArtifactId(),
                             version.getVersionId());
                 } else {
                     versionDeltas = readDocumentVersionDeltas(version.getArtifactId(),
                             version.getVersionId(), previousVersion.getVersionId());
                 }
                 deltas.put(version, versionDeltas);
 
                 directory = exportFileSystem.createDirectory(
                         nameGenerator.exportDirectoryName(version));
                 for (final DocumentVersion documentVersion : documents.get(version)) {
                     documentsSize.put(documentVersion, documentVersion.getSize());
                     final File file = new File(directory,
                             documentNameGenerator.exportFileName(documentVersion));
                     documentModel.openVersion(documentVersion.getArtifactId(),
                             documentVersion.getVersionId(), new StreamOpener() {
                                 public void open(final InputStream stream)
                                         throws IOException {
                                     streamToFile(stream, file);
                                 }
                             });
                 }
             }
 
             // copy resources into the export file system
             final Map<String, File> resources = new HashMap<String, File>();
             addExportResource(exportFileSystem, resources, "header-image",
                     "images/PDFHeader.jpg");
             addExportResource(exportFileSystem, resources, "footer-image",
                     "images/PDFFooter.jpg");
 
             // generate a pdf
             final PDFWriter pdfWriter = new PDFWriter(exportFileSystem);
             pdfWriter.write(nameGenerator.pdfFileName(container), resources,
                     container, readUser(container.getCreatedBy()),
                     readLatestVersion(container.getId()), versions,
                     versionsPublishedBy, documents, documentsSize, publishedTo,
                     deltas, readTeam(container.getId()));
 
             // create an archive
             final File zipFile = new File(exportFileSystem.getRoot(), container.getName());
             synchronized (workspace.getBufferLock()) {
                 ZipUtil.createZipFile(zipFile, exportFileSystem.getRoot(),
                         workspace.getBufferArray());
             }
 
             // copy it
             fileToStream(zipFile, exportStream);
         } finally {
             exportFileSystem.deleteTree();
         }
     }
 
     /**
      * Obtain a container name generator.
      * 
      * @return A <code>ContainerNameGenerator</code>.
      */
     private ContainerNameGenerator getNameGenerator() {
         logger.logApiId();
         try {
             return new ContainerNameGenerator();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Determine whether or not the draft is the first draft.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the draft is the first draft.
      */
     private Boolean isFirstDraft(final Long containerId) {
         return 0 == readVersions(containerId).size();
     }
 
     /**
      * Determine if the local user ia a team member. The only scenaio where this
      * will not be the case is for archive users.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the local user is a team member.
      */
     private boolean isLocalTeamMember(final Long containerId) {
         return contains(getArtifactModel().readTeam2(containerId), localUser());
     }
     
     /**
      * Lock a list of documents' versions.
      * 
      * @param documents
      *            A <code>List</code> of <code>Document</code>s.
      * @return A <code>Map</code> of <code>Document</code>s to their
      *         <code>DocumentLock</code>s.
      */
     private Map<DocumentVersion, DocumentFileLock> lockDocumentVersions(
             final List<Document> documents) throws CannotLockException {
         final Map<DocumentVersion, DocumentFileLock> locks = new HashMap<DocumentVersion, DocumentFileLock>();
         final List<DocumentVersion> versions = new ArrayList<DocumentVersion>();
         for (final Document document : documents) {
             versions.clear();
             versions.addAll(getDocumentModel().readVersions(document.getId()));
             for (final DocumentVersion version : versions) {
                 locks.put(version, getDocumentModel().lockVersion(version));
             }
         }
         return locks;
     }
 
     /**
      * Fire a container archived notification.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerArchived(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerArchived(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a container created notification.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyContainerCreated(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerCreated(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a container deleted notification.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyContainerDeleted(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerDeleted(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Notify that a container has been flagged.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerFlagged(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerFlagged(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a container published event.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @param version
      *            A <code>ContainerVersion</code>.
      * @param eventGenerator
      *            A <code>ContainerEventGenerator</code>.
      */
     private void notifyContainerPublished(final Container container,
             final ContainerDraft draft, final ContainerVersion previousVersion,
             final ContainerVersion version, final ContainerVersion nextVersion,
             final TeamMember teamMember,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerPublished(eventGenerator.generate(container,
                         draft, previousVersion, version, nextVersion, teamMember));
             }
         });
     }
 
     /**
      * Fire a container published event.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @param version
      *            A <code>ContainerVersion</code>.
      * @param eventGenerator
      *            A <code>ContainerEventGenerator</code>.
      */
     private void notifyContainerPublished(final Container container,
             final ContainerDraft draft, final ContainerVersion previousVersion,
             final ContainerVersion version, final ContainerVersion nextVersion,
             final TeamMember teamMember,
             final List<OutgoingEMailInvitation> outgoingEMailInvitations,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerPublished(eventGenerator.generate(container,
                         draft, previousVersion, version, nextVersion, teamMember,
                         outgoingEMailInvitations));
             }
         });
     }
 
     /**
      * Notify that the container has been received.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param eventGenerator
      *            A <code>ContainerEventGenerator</code>.
      */
     private void notifyContainerReceived(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerReceived(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a container renamed event.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param eventGenerator
      *            A <code>ContainerEventGenerator</code>.
      */
     private void notifyContainerRenamed(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerRenamed(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a container restored notification.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerRestored(final Container container,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.containerRestored(eventGenerator.generate(container));
             }
         });
     }
 
     /**
      * Fire a document added notification.
      * 
      * @param container
      *            A container.
      * @param draft
      *            A draft.
      * @param document
      *            A document.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyDocumentAdded(final Container container,
             final ContainerDraft draft, final Document document,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.documentAdded(eventGenerator.generate(container,
                         draft, document));
             }
         });
     }
 
     /**
      * Fire a document removed notification.
      * 
      * @param container
      *            A container.
      * @param draft
      *            A draft.
      * @param document
      *            A document.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyDocumentRemoved(final Container container,
             final ContainerDraft draft, final Document document,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.documentRemoved(eventGenerator.generate(container,
                         draft, document));
             }
         });
     }
 
     /**
      * Fire a document reverted notification.
      * 
      * @param container
      *            A container.
      * @param draft
      *            A draft.
      * @param document
      *            A document.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyDocumentReverted(final Container container,
             final ContainerDraft draft, final Document document,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.documentReverted(eventGenerator.generate(container,
                         draft, document));
             }
         });
     }
 
     /**
      * Notify that a container draft was created.
      * 
      * @param draft
      *            The draft.
      * @param eventGenerator
      *            The container event generator.
      */
     private void notifyDraftCreated(final Container container,
             final ContainerDraft draft,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.draftCreated(eventGenerator.generate(container, draft));
             }
         });
     }
 
     /**
      * Notify that a container draft was deleted.
      * 
      * @param container
      *            The container.
      * @param draft
      *            The draft.
      * @param eventGenerator
      *            The container event generator.
      */
     private void notifyDraftDeleted(final Container container,
             final ContainerDraft draft,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.draftDeleted(eventGenerator.generate(container, draft));
             }
         });
     }
 
     /**
      * Notify that a container draft was updated.
      * 
      * @param container
      *            The <code>Container</code>.
      * @param draft
      *            The <code>ContainerDraft</code>.
      * @param eventGenerator
      *            The container event generator.
      */
     private void notifyDraftUpdated(final Container container,
             final ContainerDraft draft,
             final ContainerEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ContainerListener>() {
             public void notifyListener(final ContainerListener listener) {
                 listener.draftUpdated(eventGenerator.generate(container, draft));
             }
         });
     }
 
     /**
      * Read the documents for the container version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param comparator
      *            A document comparator <code>Comparator&lt;Artifact&gt;</code>.
      * @param filter
      *            A document filter <code>Filter&lt;? super Artifact&gt;</code>.
      * @return A <code>List&gt;Document&gt;</code>.
      * 
      * @see FilterManager#filter(List, Filter)
      * @see ModelSorter#sortDocuments(List, Comparator)
      */
     private List<Document> readDocuments(final Long containerId, final Long versionId,
             final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         logger.logApiId();
         logger.logVariable("containerId", containerId);
         logger.logVariable("versionId", versionId);
         logger.logVariable("comparator", comparator);
         logger.logVariable("filter", filter);
         final List<Document> documents =
                 containerIO.readDocuments(containerId, versionId);
         FilterManager.filter(documents, filter);
         ModelSorter.sortDocuments(documents, comparator);
         return documents;
     }
 
     /**
      * Read a user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return A <code>User</code>.
      */
     private User readUser(final JabberId userId) {
         return getUserModel().read(userId);
     }
 
     /**
      * Revert a modified document. The document content is over-written.
      * 
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @param containerId
      *            A container id <code>Long</code>.
      * @param documentId
      *            A document id <code>Long</code>.
      * @throws CannotLockException
      * @throws IOException
      */
     private void revertModifiedDocument(final ContainerDraft draft,
             final Long containerId, final Long documentId)
             throws CannotLockException, IOException {
         final Document document = draft.getDocument(documentId);
         final DocumentFileLock lock = lockDocument(document);
         try {
             getDocumentModel().revertDraft(lock, documentId);
         } finally {
             releaseLock(lock);
         }
     }
 
     /**
      * Revert a removed document. The draft/document relationship is
      * re-established and the draft document is created.
      * 
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @param containerId
      *            A container id <code>Long</code>.
      * @param documentId
      *            A document id <code>Long</code>.
      * @throws CannotLockException
      * @throws IOException
      */
     private void revertRemovedDocument(final ContainerDraft draft,
             final Long containerId, final Long documentId)
             throws CannotLockException, IOException {
         final Document document = draft.getDocument(documentId);
         final DocumentFileLock lock = lockDocument(document);
         try {
             containerIO.deleteDraftArtifactRel(containerId, document.getId());
             containerIO.createDraftArtifactRel(containerId, document.getId(),
                     ContainerDraft.ArtifactState.NONE);
             getDocumentModel().revertDraft(lock, documentId);
         } finally {
             releaseLock(lock);
         }
         // create draft document
         createDraftDocument(containerId, documentId);
     }
 
     /**
      * <b>Title:</b>Publishing Delegate Stream Uploader<br>
      * <b>Description:</b><br>
      */
     private static final class StreamUploader implements
             com.thinkparity.codebase.model.stream.StreamUploader {
 
         /** A reference to <code>PublishingDelegate</code>. */
         private final ContainerModelImpl impl;
 
         /** A <code>ProcessMonitor</code>. */
         private final ProcessMonitor monitor;
 
         /** A stream id <code>String</code>. */
         private final String streamId;
 
         /** A <code>StreamSession</code>. */
         private final StreamSession streamSession;
 
         /** The stream size <code>Long</code>. */
         private final Long streamSize;
 
         /**
          * Create StreamUploader.
          * 
          * @param publishingDelegate
          *            A reference to <code>PublishingDelegate</code>.
          * @param streamId
          *            A stream id <code>String</code>.
          * @param streamSession
          *            A <code>StreamSession</code>.
          * @param streamSize
          *            The stream size <code>Long</code>.
          */
         private StreamUploader(final ContainerModelImpl impl,
                 final ProcessMonitor monitor, final String streamId,
                 final StreamSession streamSession, final Long streamSize) {
             this.impl = impl;
             this.monitor = monitor;
             this.streamId = streamId;
             this.streamSession = streamSession;
             this.streamSize = streamSize;
         }
 
         /**
          * @see com.thinkparity.codebase.model.stream.StreamUploader#upload(java.io.InputStream)
          * 
          */
         public void upload(final InputStream stream) throws IOException {
             final InputStream bufferedStream = new BufferedInputStream(stream,
                     impl.getBufferSize());
             /* NOTE the underlying stream is closed by the document io handler
              * through the document model and is thus not closed here */
             impl.upload(new UploadMonitor() {
                 private long totalChunks = 0;
                 public void chunkUploaded(final int chunkSize) {
                     totalChunks += chunkSize;
                     while (totalChunks >= STEP_SIZE) {
                         totalChunks -= STEP_SIZE;
                         notifyStepEnd(monitor, PublishStep.UPLOAD_STREAM);
                     }
                 }
             }, streamId, streamSession, bufferedStream, streamSize);
         }
     }
 }
