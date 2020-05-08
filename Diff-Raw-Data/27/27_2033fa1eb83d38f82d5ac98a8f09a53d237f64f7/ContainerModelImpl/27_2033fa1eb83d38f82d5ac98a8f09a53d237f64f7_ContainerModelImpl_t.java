 /*
  * Generated On: Jun 27 06 12:13:12 PM
  */
 package com.thinkparity.ophelia.model.container;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.artifact.ArtifactState;
 import com.thinkparity.codebase.model.artifact.ArtifactType;
 import com.thinkparity.codebase.model.artifact.ArtifactVersion;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.AbstractModelImpl;
 import com.thinkparity.ophelia.model.Constants.IO;
 import com.thinkparity.ophelia.model.Constants.Versioning;
 import com.thinkparity.ophelia.model.artifact.InternalArtifactModel;
 import com.thinkparity.ophelia.model.audit.HistoryItem;
 import com.thinkparity.ophelia.model.audit.event.AuditEvent;
 import com.thinkparity.ophelia.model.document.Document;
 import com.thinkparity.ophelia.model.document.DocumentVersion;
 import com.thinkparity.ophelia.model.document.InternalDocumentModel;
 import com.thinkparity.ophelia.model.events.ContainerListener;
 import com.thinkparity.ophelia.model.events.ContainerEvent.Source;
 import com.thinkparity.ophelia.model.index.InternalIndexModel;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.handler.ContainerIOHandler;
 import com.thinkparity.ophelia.model.session.Credentials;
 import com.thinkparity.ophelia.model.session.InternalSessionModel;
 import com.thinkparity.ophelia.model.user.InternalUserModel;
 import com.thinkparity.ophelia.model.user.TeamMember;
 import com.thinkparity.ophelia.model.util.Printer;
 import com.thinkparity.ophelia.model.util.UUIDGenerator;
 import com.thinkparity.ophelia.model.util.filter.ArtifactFilterManager;
 import com.thinkparity.ophelia.model.util.filter.Filter;
 import com.thinkparity.ophelia.model.util.filter.UserFilterManager;
 import com.thinkparity.ophelia.model.util.filter.artifact.DefaultFilter;
 import com.thinkparity.ophelia.model.util.filter.history.HistoryFilterManager;
 import com.thinkparity.ophelia.model.util.sort.ComparatorBuilder;
 import com.thinkparity.ophelia.model.util.sort.ModelSorter;
 import com.thinkparity.ophelia.model.util.sort.user.UserComparatorFactory;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 
 /**
  * <b>Title:</b>thinkParity Container Model Implementation</br>
  * <b>Description:</b>
  *
  * @author CreateModel.groovy
  * @version 1.1.2.3
  */
 public class ContainerModelImpl extends AbstractModelImpl {
 
     /** A list of container listeners. */
     private static final List<ContainerListener> LISTENERS = new LinkedList<ContainerListener>();
 
     /**
      * Obtain an apache api log id.
      * @param api The api.
      * @return The log id.
      */
     private static StringBuffer getApiId(final String api) {
         return getModelId("CONTAINER").append(" ").append(api);
     }
 
     /** A container audit generator. */
     private final ContainerAuditor auditor;
 
     /** The container io layer. */
     private final ContainerIOHandler containerIO;
 
     /** A default container comparator. */
     private final Comparator<Artifact> defaultComparator;
 
     /** The default document comparator. */
     private final Comparator<Artifact> defaultDocumentComparator;
 
     /** The default document filter. */
     private final Filter<? super Artifact> defaultDocumentFilter;
 
     /** A default container filter. */
     private final Filter<? super Artifact> defaultFilter;
 
     /** A default history item comparator. */
     private final Comparator<? super HistoryItem> defaultHistoryComparator;
 
     /** A default history item filter. */
     private final Filter<? super HistoryItem> defaultHistoryFilter;
 
     /** A default user comparator. */
     private final Comparator<User> defaultUserComparator;
 
     /** A default user filter. */
     private final Filter<? super User> defaultUserFilter;
 
     /** The default container version comparator. */
     private final Comparator<ArtifactVersion> defaultVersionComparator;
 
     /** The default container version filter. */
     private final Filter<? super ArtifactVersion> defaultVersionFilter;
 
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
     ContainerModelImpl(final Workspace workspace) {
         super(workspace);
         this.auditor = new ContainerAuditor(getContext());
         this.containerIO = IOFactory.getDefault().createContainerHandler();
         this.defaultComparator = new ComparatorBuilder().createByName(Boolean.TRUE);
         this.defaultDocumentComparator = new ComparatorBuilder().createByName(Boolean.TRUE);
         this.defaultDocumentFilter = new DefaultFilter();
         this.defaultFilter = new DefaultFilter();
         this.defaultHistoryComparator = new ComparatorBuilder().createDateDescending();
         this.defaultHistoryFilter = new com.thinkparity.ophelia.model.util.filter.history.DefaultFilter();
         this.defaultUserComparator = UserComparatorFactory.createOrganizationAndName(Boolean.TRUE);
         this.defaultUserFilter = new com.thinkparity.ophelia.model.util.filter.user.DefaultFilter();
         this.defaultVersionComparator = new ComparatorBuilder().createVersionById(Boolean.FALSE);
         this.defaultVersionFilter = new com.thinkparity.ophelia.model.util.filter.container.DefaultVersionFilter();
         this.localEventGenerator = new ContainerEventGenerator(Source.LOCAL);
         this.remoteEventGenerator = new ContainerEventGenerator(Source.REMOTE);
     }
 
     /**
      * Add a document to a container.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      */
     void addDocument(final Long containerId, final Long documentId) {
         logger.info(getApiId("[ADD DOCUMENT]"));
         logger.debug(containerId);
         logger.debug(documentId);
         assertDraftExists(getApiId("[ADD DOCUMENT] [DRAFT DOES NOT EXIST]"), containerId);
         containerIO.createDraftArtifactRel(containerId, documentId, ContainerDraft.ArtifactState.ADDED);
         getIndexModel().indexDocument(containerId, documentId);
         final Container postAdditionContainer = read(containerId);        
         final ContainerDraft postAdditionDraft = readDraft(containerId);
         final Document postAdditionDocument = getInternalDocumentModel().read(documentId);
         notifyDocumentAdded(postAdditionContainer, postAdditionDraft,
                 postAdditionDocument, localEventGenerator);
     }
 
     /**
      * Add a container listener.
      * 
      * @param listener
      *            A container listener.
      */
     void addListener(final ContainerListener listener) {
         logger.info(getApiId("[ADD LISTENER]"));
         logger.debug(listener);
         Assert.assertNotNull(getApiId("[ADD LISTENER] [LISTENER IS NULL]"),
                 listener);
         synchronized(LISTENERS) {
             if(LISTENERS.contains(listener)) { return; }
             LISTENERS.add(listener);
         }
     }
 
     /**
      * Create a container.
      * 
      * @param name
      *            The container name.
      * @return The new container.
      */
     Container create(final String name) {
         logApiId();
         logVariable("name", name);
         try {
             final Credentials credentials = readCredentials();
             final Calendar currentDateTime = currentDateTime();
     
             final Container container = new Container();
             container.setCreatedBy(credentials.getUsername());
             container.setCreatedOn(currentDateTime);
             container.setName(name);
             container.setState(ArtifactState.ACTIVE);
             container.setType(ArtifactType.CONTAINER);
             container.setUniqueId(UUIDGenerator.nextUUID());
             container.setUpdatedBy(credentials.getUsername());
             container.setUpdatedOn(currentDateTime);
             // local create
             containerIO.create(container);
     
             // local key
             final InternalArtifactModel artifactModel = getInternalArtifactModel();
             artifactModel.applyFlagKey(container.getId());
     
             // create remote info
             artifactModel.createRemoteInfo(container.getId(), localUserId(), currentDateTime);
     
             // audit
             auditor.create(container.getId(), localUserId(), currentDateTime);
     
             // index
             getIndexModel().indexContainer(container.getId());
     
             // create team
             final TeamMember teamMember = createTeam(container.getId());
     
             // create first draft
             createFirstDraft(container.getId(), teamMember);
     
             // fire event
             final Container postCreation = read(container.getId());
             notifyContainerCreated(postCreation, localEventGenerator);
             return postCreation;
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Create a <code>ContainerDraft</code>. If the container does not yet
      * exist as an artifact on the server; it will be created. All of the
      * artifact references that exist in the latest version of the container
      * will be copied into the draft. A remote call to create draft will also be
      * made.
      * 
      * @param containerId
      *            The container id.
      * @return A new <code>ContainerDraft</code>.
      * @see #createDistributed(Container)
      * @see #createFirstDraft(Long, TeamMember)
      * @see #isDistributed(Long)
      * @see #localTeamMember(Long)
      * @see #notifyDraftCreated(Container, ContainerDraft,
      *      ContainerEventGenerator)
      * @see #read(Long)
      * @see #readDocuments(Long, Long)
      * @see #readLatestVersion(Long)
      * @see InternalSessionModel#createDraft(UUID)
      */
     ContainerDraft createDraft(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         assertContainerDraftDoesNotExist("DRAFT ALREADY EXISTS", containerId);
         assertOnline("USER NOT ONLINE");
         final Container container = read(containerId);
         if (!isDistributed(container.getId())) {
             createDistributed(container);
         }
         final ContainerVersion latestVersion =
                 readLatestVersion(container.getId());
         final List<Document> documents = readDocuments(
                 latestVersion.getArtifactId(), latestVersion.getVersionId());
         // create
         final ContainerDraft draft = new ContainerDraft();
         draft.setOwner(localTeamMember(containerId));
         draft.setContainerId(containerId);
         for (final Document document : documents) {
             draft.addDocument(document);
             draft.putState(document, ContainerDraft.ArtifactState.NONE);
         }
         containerIO.createDraft(draft);
         getInternalArtifactModel().applyFlagKey(container.getId());
         // remote create
         getInternalSessionModel().createDraft(container.getUniqueId());
         // fire event
         final Container postCreation= read(containerId);
         final ContainerDraft postCreationDraft = readDraft(containerId);
         notifyDraftCreated(postCreation, postCreationDraft, localEventGenerator);
         return postCreationDraft;
     }
 
     /**
      * Delete a container.
      * 
      * @param containerId
      *            A container id.
      */
     void delete(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         try {
             final Container container = read(containerId);
             if (isDistributed(container.getId())) {
                 final TeamMember localTeamMember = localTeamMember(container.getId());
                 deleteLocal(container.getId());
                 getInternalSessionModel().removeTeamMember(
                         container.getUniqueId(), localTeamMember.getId());
             } else {
                 deleteLocal(container.getId());
             }
             // fire event
             notifyContainerDeleted(container, localEventGenerator);
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Delete a draft.
      * 
      * @param containerId
      *            A container id.
      */
     void deleteDraft(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         assertDoesExistLocalDraft("DRAFT DOES NOT EXIST", containerId);
         assertIsDistributed("CANNOT DELETE FIRST DRAFT", containerId);
         assertOnline("USER NOT ONLINE");
         final Container container = read(containerId);
         getInternalSessionModel().deleteDraft(container.getUniqueId());
         // delete local data
         final ContainerDraft draft = readDraft(containerId);
         for (final Artifact artifact : draft.getArtifacts()) {
             containerIO.deleteDraftArtifactRel(containerId, artifact.getId());
         }
         containerIO.deleteDraft(containerId);
         notifyDraftDeleted(container, draft, localEventGenerator);
     }
 
     /**
      * Handle the container artifact published remote event. If the container
      * does not yet exist it will be created; same goes for the version. The
      * artifact will then be passed off to the appropriate model then attached
      * to the version.
      * 
      * @param uniqueId
      *            The container <code>UUID</code>.
      * @param versionId
      *            The container version id <code>Long</code>.
      * @param name
      *            The container name <code>String</code>.
      * @param artifactUniqueId
      *            The artifact <code>UUID</code>
      * @param artifactVersionId
      *            The artifact version id <code>Long</code>.
      * @param artifactName
      *            The artifact name <code>String</code>.
      * @param artifactType
      *            The artifact's <code>ArtifactType</code>.
      * @param artifactChecksum
      *            The artifact checksum <code>String</code>.
      * @param artifactBytes
      *            The artifact bytes <code>byte[]</code>.
      * @param publishedBy
      *            The publisher <code>JabberId</code>.
      * @param publishedOn
      *            The publish date <code>Calendar</code>.
      * 
      * @see #createVersion(Long, Long, JabberId, Calendar)
      * @see #handleDocumentPublished(UUID, Long, String, String, byte[],
      *      JabberId, Calendar)
      * @see #notifyContainerPublished(Container, ContainerDraft,
      *      ContainerVersion, ContainerEventGenerator)
      * @see #read(Long)
      * @see #readVersion(Long, Long)
      * @see ContainerIndexor#create(Long, String)
      * @see InternalArtifactModel#createRemoteInfo(Long, JabberId, Calendar)
      * @see InternalArtifactModel#doesExist(UUID)
      * @see InternalArtifactModel#doesVersionExist(Long, Long)
      * @see InternalArtifactModel#readId(UUID)
      */
     void handleArtifactPublished(final UUID uniqueId, final Long versionId,
             final String name, final UUID artifactUniqueId,
             final Long artifactVersionId, final String artifactName,
             final ArtifactType artifactType, final String artifactChecksum,
             final byte[] artifactBytes, final JabberId publishedBy,
             final Calendar publishedOn) {
         logApiId();
         logVariable("uniqueId", uniqueId);
         logVariable("versionId", versionId);
         logVariable("name", name);
         logVariable("artifactUniqueId", artifactUniqueId);
         logVariable("artifactVersionId", artifactVersionId);
         logVariable("artifactName", artifactName);
         logVariable("artifactType", artifactType);
         logVariable("artifactChecksum", artifactChecksum);
         logVariable("artifactBytes", artifactBytes);
         logVariable("publishedBy", publishedBy);
         logVariable("publishedOn", publishedOn);
         try {
             // determine the existance of the container and the version.
             final InternalArtifactModel artifactModel = getInternalArtifactModel();
             final Boolean doesExist = artifactModel.doesExist(uniqueId);
             final Boolean doesVersionExist;
             final Long containerId;
             final Container container;
             final ContainerVersion version;
             if (doesExist) {
                 containerId = artifactModel.readId(uniqueId);
                 container = read(containerId);
                 doesVersionExist = artifactModel.doesVersionExist(containerId, versionId);
 
                 if (doesVersionExist) {
                     version = readVersion(container.getId(), versionId);
                 } else {
                     version = createVersion(container.getId(),
                             versionId, publishedBy, publishedOn);
                 }
             } else { 
                 doesVersionExist = Boolean.FALSE;
 
                 container = new Container();
                 container.setCreatedBy(publishedBy.getUsername());
                 container.setCreatedOn(publishedOn);
                 container.setName(name);
                 container.setState(ArtifactState.ACTIVE);
                 container.setType(ArtifactType.CONTAINER);
                 container.setUniqueId(uniqueId);
                 container.setUpdatedBy(container.getCreatedBy());
                 container.setUpdatedOn(container.getCreatedOn());
                 // create
                 containerIO.create(container);
 
                 // create version
                 version = createVersion(container.getId(), versionId,
                         publishedBy, publishedOn);
     
                 // create remote info
                 artifactModel.createRemoteInfo(container.getId(), publishedBy, container.getCreatedOn());
     
                 // index
                 getIndexModel().indexContainer(container.getId());
             }
 
             // handle the artifact by specific type
             final ArtifactVersion artifactVersion;
             switch(artifactType) {
             case DOCUMENT:
                 artifactVersion = handleDocumentPublished(artifactUniqueId,
                         artifactVersionId, artifactName, artifactChecksum,
                         artifactBytes, publishedBy, publishedOn);
                 break;
             case CONTAINER:
             default:
                 throw Assert.createUnreachable("UNKNOWN ARTIFACT TYPE");
             }
             containerIO.addVersion(version.getArtifactId(),
                     version.getVersionId(), artifactVersion.getArtifactId(),
                     artifactVersion.getVersionId(),
                     artifactVersion.getArtifactType());
 
             final Container postPublish = read(container.getId());
             final ContainerVersion postPublishVersion =
                 readVersion(version.getArtifactId(), version.getVersionId());
             notifyContainerPublished(postPublish, null, postPublishVersion,
                     remoteEventGenerator);
         }
         catch(final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Handle the artifact sent remote event. If the container does not yet
      * exist it will be created; same goes for the version. The artifact will
      * then be passed off to the appropriate model then attached to the version.
      * 
      * @param uniqueId
      *            The container <code>UUID</code>
      * @param versionId
      *            The container version id <code>Long</code>.
      * @param name
      *            The container name <code>String</code>.
      * @param artifactUniqueId
      *            The artifact <code>UUID</code>.
      * @param artifactVersionId
      *            The artifact version id <code>Long</code>.
      * @param artifactName
      *            The artifact name <code>String</code>.
      * @param artifactType
      *            The artifact's <code>ArtifactType</code>.
      * @param artifactChecksum
      *            The artifact checksum <code>String</code>.
      * @param artifactBytes
      *            The artifact's bytes <code>byte[]</code>.
      * @param sentBy
      *            The sender <code>JabberId</code>.
      * @param sentOn
      *            The sent date <code>Calendar</code>.
      * 
      * @see #createVersion(Long, Long, JabberId, Calendar)
      * @see #read(Long)
      * @see #readVersion(Long, Long)
      * @see ContainerIndexor#create(Long, String)
      * @see InternalArtifactModel#createRemoteInfo(Long, JabberId, Calendar)
      * @see InternalArtifactModel#doesExist(UUID)
      * @see InternalArtifactModel#doesVersionExist(Long, Long)
      * @see InternalArtifactModel#readId(UUID)
      * @see InternalDocumentModel#handleDocumentSent(JabberId, Calendar, UUID,
      *      Long, String, String, InputStream)
      */
     void handleArtifactSent(final UUID uniqueId, final Long versionId,
             final String name, final UUID artifactUniqueId,
             final Long artifactVersionId, final String artifactName,
             final ArtifactType artifactType, final String artifactChecksum,
             final byte[] artifactBytes, final JabberId sentBy,
             final Calendar sentOn) {
         logApiId();
         logVariable("sentBy", sentBy);
         logVariable("sentOn", sentOn);
         logVariable("containerUniqueId", uniqueId);
         logVariable("containerVersionId", versionId);
         logVariable("containerName", name);
         logVariable("artifactUniqueId", artifactUniqueId);
         logVariable("artifactVersionId", artifactVersionId);
         logVariable("artifactName", artifactName);
         logVariable("artifactType", artifactType);
         logVariable("artifactChecksum", artifactChecksum);
         logVariable("artifactBytes", artifactBytes);
         try {
             final InternalArtifactModel artifactModel = getInternalArtifactModel();
             final Container container;
             final ContainerVersion version;
             final Boolean doesExist = artifactModel.doesExist(uniqueId);
             final Boolean doesVersionExist;
             if (doesExist) {
                 container = read(artifactModel.readId(uniqueId));
                 doesVersionExist =
                     artifactModel.doesVersionExist(container.getId(), versionId);
                 if (doesVersionExist) {
                     version = readVersion(container.getId(), versionId);
                 } else {
                     version = createVersion(container.getId(),
                             versionId, sentBy, sentOn);
                 }
             } else {
                 doesVersionExist = Boolean.FALSE;
 
                 container = new Container();
                 container.setCreatedBy(sentBy.getUsername());
                 container.setCreatedOn(sentOn);
                 container.setName(name);
                 container.setState(ArtifactState.ACTIVE);
                 container.setType(ArtifactType.CONTAINER);
                 container.setUniqueId(uniqueId);
                 container.setUpdatedBy(container.getCreatedBy());
                 container.setUpdatedOn(container.getCreatedOn());
                 // create
                 containerIO.create(container);
 
                 // create version
                 version = createVersion(container.getId(), versionId,
                         sentBy, sentOn);
     
                 // create remote info
                 artifactModel.createRemoteInfo(container.getId(), sentBy, container.getCreatedOn());
     
                 // index
                 getIndexModel().indexContainer(container.getId());
             }
     
             // handle the artifact by specific type
             final ArtifactVersion artifactVersion;
             switch (artifactType) {
             case DOCUMENT:
                 artifactVersion = handleDocumentSent(sentBy, sentOn, artifactUniqueId,
                         artifactVersionId, artifactName, artifactChecksum, artifactBytes);
                 break;
             case CONTAINER:
             default:
                 throw Assert.createUnreachable("UNSUPPORTED ARTIFACT TYPE");
             }
 
             // add to the version
             if (!doesVersionExist) {
                 containerIO.addVersion(version.getArtifactId(), version
                         .getVersionId(), artifactVersion.getArtifactId(),
                         artifactVersion.getVersionId(), artifactVersion
                                 .getArtifactType());
             }
         }
         catch(final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Handle the remote draft created event. A
      * <code>ContainerDraft</code is created;
      * and the created by <code>JabberId</code> is set as the owner; and a 
      * notification is fired.
      * 
      * @param containerId
      *            The container id <code>Long</code>.
      * @param createdBy
      *            The creation user <code>JabberId</code>.
      * @param createdOn
      *            The creation date <code>Calendar</code>.
      *            
      * @see #notifyDraftCreated(Container, ContainerDraft, ContainerEventGenerator)
      * @see #readTeam(Long)
      */
     void handleDraftCreated(final Long containerId,
             final JabberId createdBy, final Calendar createdOn) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("createdBy", createdBy);
         logVariable("createdOn", createdOn);
         final ContainerDraft draft = new ContainerDraft();
         draft.setContainerId(containerId);
         final List<TeamMember> team = readTeam(containerId);
         logVariable("team", team);
         draft.setOwner(team.get(indexOf(team, createdBy)));
         containerIO.createDraft(draft);
         // fire event
         notifyDraftCreated(read(containerId), readDraft(containerId),
                 remoteEventGenerator);
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
     void handleDraftDeleted(final Long containerId,
             final JabberId deletedBy, final Calendar deletedOn) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("createdBy", deletedBy);
         logVariable("createdOn", deletedOn);
         final ContainerDraft draft = readDraft(containerId);
         for (final Artifact artifact : draft.getArtifacts()) {
             containerIO.deleteDraftArtifactRel(containerId, artifact.getId());
         }
         containerIO.deleteDraft(containerId);
         // fire event
         notifyDraftDeleted(read(containerId), draft, remoteEventGenerator);
     }
 
     /**
      * Handle the container published event. The local team definition is built
      * from the publishedTo list. The published to list is also saved.
      * 
      * @param uniqueId
      *            The container unique id.
      * @param versionId
      *            The container version id.
      * @param name
      *            The container name.
      * @param artifactCount
      *            The container artifact count.
      * @param publishedBy
      *            The publish date.
      * @param publishedTo
      *            The publish to list.
      * @param publishedOn
      *            The published on date.
      */
     void handlePublished(final UUID uniqueId, final Long versionId,
             final String name, final Integer artifactCount,
             final JabberId publishedBy, final List<JabberId> publishedTo,
             final Calendar publishedOn) {
         logApiId();
         logVariable("uniqueId", uniqueId);
         logVariable("versionId", versionId);
         logVariable("name", name);
         logVariable("artifactCount", artifactCount);
         logVariable("publishedBy", publishedBy);
         logVariable("publishedTo", publishedTo);
         logVariable("publishedOn", publishedOn);
         final InternalArtifactModel artifactModel = getInternalArtifactModel();
         final Long containerId = artifactModel.readId(uniqueId);
         // add to local team
         final InternalUserModel userModel = getInternalUserModel();
         final List<TeamMember> localTeam = artifactModel.readTeam2(containerId);
         final List<User> publishedToUsers = new ArrayList<User>();
         for (final JabberId publishedToId : publishedTo) {
             if (!contains(localTeam, publishedToId)) {
                 artifactModel.addTeamMember(containerId, publishedToId);
             }
             publishedToUsers.add(userModel.read(publishedToId));
         }
        // add the sender as well
        if (!contains(localTeam, publishedBy)) {
            artifactModel.addTeamMember(containerId, publishedBy);
        }
         // create published to list
         containerIO.createPublishedTo(containerId, versionId, publishedToUsers);
         // fire event
         notifyContainerPublished(read(containerId), readDraft(containerId),
                 readVersion(containerId, versionId), remoteEventGenerator);
     }
 
     /**
      * Handle the container shared remote event.  All we're doing here is saving
      * the sent to list and firing an event.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @param name
      *            A container name <code>String</code>.
      * @param artifactCount
      *            An artifact count <code>Integer</code>.
      * @param sentBy
      *            The sent by user's <code>JabberId</code>.
      * @param sentOn
      *            The sent date <code>Calendar</code>.
      * @param sentTo
      *            The sent to <code>List&lt;JabberId&gt;</code>.
      */
     void handleSent(final UUID uniqueId, final Long versionId,
             final String name, final Integer artifactCount,
             final JabberId sentBy, final Calendar sentOn,
             final List<JabberId> sentTo) {
         logApiId();
         logVariable("uniqueId", uniqueId);
         logVariable("versionId", versionId);
         logVariable("name", name);
         logVariable("artifactCount", artifactCount);
         logVariable("sentBy", sentBy);
         logVariable("sentBy", sentBy);
         logVariable("sentTo", sentTo);
         final InternalArtifactModel artifactModel = getInternalArtifactModel();
         final InternalUserModel userModel = getInternalUserModel();
         final Long containerId = artifactModel.readId(uniqueId);
         final List<User> sharedWithUsers = new ArrayList<User>(sentTo.size());
         for (final JabberId sentToId : sentTo) {
             sharedWithUsers.add(userModel.readLazyCreate(sentToId));
         }
         containerIO.createSharedWith(containerId, versionId, sharedWithUsers);
         // fire event
         notifyContainerShared(read(containerId), readVersion(containerId,
                 versionId), remoteEventGenerator);
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
     void printVersion(final Long containerId, final Long versionId,
             final Printer printer) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("printer", printer);
         try {
             final InternalDocumentModel documentModel = getInternalDocumentModel();
             final List<DocumentVersion> documentVersions =
                 containerIO.readDocumentVersions(containerId, versionId);
             for (final DocumentVersion documentVersion : documentVersions) {
                 documentModel.printVersion(documentVersion.getArtifactId(),
                         documentVersion.getVersionId(), printer);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
     /**
      * Print a container draft.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param printer
      *            An <code>Printer</code>.
      */
     void printDraft(final Long containerId, final Printer printer) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("printer", printer);
         try {
             final ContainerDraft draft = readDraft(containerId);
             final InternalDocumentModel documentModel = getInternalDocumentModel();
             for (final Document document : draft.getDocuments()) {
                 documentModel.printDraft(document.getId(), printer);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Publish the container. Publishing involves determining if the working
      * version of a document differes from the latest version and if so creating
      * a new version; then sending the latest version to all team members.
      * 
      * @param containerId
      *            The container id.
      * @param contacts
      *            A list of contacts to publish to.
      * @param teamMembers
      *            A list of team members to publish to.
      */
     void publish(final Long containerId, final List<Contact> contacts,
             final List<TeamMember> teamMembers) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("contacts", contacts);
         logVariable("teamMembers", teamMembers);
         publish(containerId, null, contacts, teamMembers);
     }
 
     /**
      * Publish the container. Publishing involves determining if the working
      * version of a document differes from the latest version and if so creating
      * a new version; then sending the latest version to all team members.
      * 
      * @param containerId
      *            The container id.
      * @param contacts
      *            A list of contacts to publish to.
      * @param teamMembers
      *            A list of team members to publish to.
      */
     void publish(final Long containerId, final String comment,
             final List<Contact> contacts, final List<TeamMember> teamMembers) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("comment", comment);
         logVariable("contacts", contacts);
         logVariable("teamMembers", teamMembers);
         assertOnline("USER NOT ONLINE");
         assertDoesExistLocalDraft("LOCAL DRAFT DOES NOT EXIST", containerId);
         assertDoesNotContain("CANNOT PUBLISH TO SELF", teamMembers, localUser());
         try {
             final Container container = read(containerId);
             final ContainerDraft draft = readDraft(containerId);
             final Calendar currentDateTime = currentDateTime();
             // if the artfiact doesn't exist on the server; create it there
             if (!isDistributed(container.getId())) {
                 createDistributed(container);
             }
             // ensure the user is the key holder
             assertIsKeyHolder("USER NOT KEY HOLDER", containerId);
 
             // create version
             final ContainerVersion version = createVersion(container.getId());
 
             // attach artifacts to the version
             final InternalDocumentModel documentModel = getInternalDocumentModel();
             final List<Document> draftDocuments = draft.getDocuments();
             DocumentVersion draftDocumentLatestVersion;
             for(final Document draftDocument : draftDocuments) {
                 if(ContainerDraft.ArtifactState.REMOVED != draft
                         .getState(draftDocument)) {
                     if(documentModel.isDraftModified(draftDocument.getId())) {
                         draftDocumentLatestVersion =
                             documentModel.createVersion(draftDocument.getId());
                     }
                     else {
                         draftDocumentLatestVersion =
                             documentModel.readLatestVersion(draftDocument.getId());
                     }
                     containerIO.addVersion(
                             version.getArtifactId(), version.getVersionId(),
                             draftDocumentLatestVersion.getArtifactId(),
                             draftDocumentLatestVersion.getVersionId(),
                             draftDocumentLatestVersion.getArtifactType());
                 }
             }
 
             // update local team
             final InternalArtifactModel artifactModel = getInternalArtifactModel();
             for (final Contact contact : contacts)
                 artifactModel.addTeamMember(container.getId(), contact.getId());
 
             // remove local key
             artifactModel.removeFlagKey(container.getId());
 
             // build the publish to list then publish
             final List<JabberId> publishTo = new ArrayList<JabberId>();
             for (final Contact contact : contacts)
                 publishTo.add(contact.getId());
             for (final TeamMember teamMember : teamMembers)
                 publishTo.add(teamMember.getId());
             publish(version, publishTo, localUserId(), currentDateTime);
 
             // update remote team
             final InternalSessionModel sessionModel = getInternalSessionModel();
             for (final Contact contact : contacts)
                 sessionModel.addTeamMember(container.getUniqueId(), contact.getId());
 
             // delete draft
             for(final Artifact artifact : draft.getArtifacts()) {
                 containerIO.deleteDraftArtifactRel(
                         container.getId(), artifact.getId());
             }
             containerIO.deleteDraft(container.getId());
 
             // fire event
             final Container postPublish = read(container.getId());
             final ContainerVersion postPublishVersion = readVersion(
                     version.getArtifactId(), version.getVersionId());
             notifyContainerPublished(postPublish, draft, postPublishVersion,
                     localEventGenerator);
         } catch(final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Read the containers.
      * 
      * @return A list of containers.
      */
     List<Container> read() {
         logger.info(getApiId("[READ]"));
         return read(defaultComparator);
     }
 
     /**
      * Read the containers.
      * 
      * @param comparator
      *            A sort ordering to user.
      * @return A list of containers.
      */
     List<Container> read(final Comparator<Artifact> comparator) {
         logger.info(getApiId("[READ]"));
         logger.debug(comparator);
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
     List<Container> read(final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         logger.info(getApiId("[READ]"));
         logger.debug(comparator);
         logger.debug(filter);
         final List<Container> containers = containerIO.read(localUser());
         ArtifactFilterManager.filter(containers, filter);
         ModelSorter.sortContainers(containers, comparator);
         return containers;        
     }
 
     /**
      * Read the containers.
      * 
      * @param filter
      *            A filter to apply.
      * @return A list of containers.
      */
     List<Container> read(final Filter<? super Artifact> filter) {
         logger.info(getApiId("[READ]"));
         logger.debug(filter);
         return read(defaultComparator, filter);
     }
 
     /**
      * Read a container.
      * 
      * @param containerId
      *            A container id.
      * @return A container.
      */
     Container read(final Long containerId) {
         logger.info(getApiId("[READ]"));
         logger.debug(containerId);
         return containerIO.read(containerId, localUser());
     }
 
     /**
      * Read the list of audit events for a container.
      * 
      * @param containerId
      *            A container id.
      * @return A list of audit events.
      */
     List<AuditEvent> readAuditEvents(final Long containerId) {
         logger.info(getApiId("[READ AUDIT EVENTS]"));
         logger.debug(containerId);
         return getInternalAuditModel().read(containerId);
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
     List<Document> readDocuments(final Long containerId, final Long versionId) {
         logger.info(getApiId("[READ DOCUMENTS]"));
         logger.debug(containerId);
         return readDocuments(containerId, versionId, defaultDocumentComparator, defaultDocumentFilter);
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param comparator
      *            A document comparator.
      * @return A list of documents.
      */
     List<Document> readDocuments(final Long containerId, final Long versionId,
             final Comparator<Artifact> comparator) {
         logger.info(getApiId("[READ DOCUMENTS]"));
         return readDocuments(containerId, versionId, comparator, defaultDocumentFilter);
     }
 
     /**
      * Read the documents for the container.
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
      * @see ArtifactFilterManager#filter(List, Filter)
      * @see ModelSorter#sortDocuments(List, Comparator)
      */
     List<Document> readDocuments(final Long containerId, final Long versionId,
             final Comparator<Artifact> comparator,
             final Filter<? super Artifact> filter) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("comparator", comparator);
         logVariable("filter", filter);
         final List<Document> documents =
                 containerIO.readDocuments(containerId, versionId);
         ArtifactFilterManager.filter(documents, filter);
         ModelSorter.sortDocuments(documents, comparator);
         return documents;
     }
 
     /**
      * Read the documents for the container.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param filter
      *            A document filter.
      * @return A list of documents.
      */
     List<Document> readDocuments(final Long containerId, final Long versionId,
             final Filter<? super Artifact> filter) {
         logger.info(getApiId("[READ DOCUMENTS]"));
         logger.debug(containerId);
         logger.debug(versionId);
         logger.debug(filter);
         return readDocuments(containerId, versionId, defaultDocumentComparator, filter);
     }
 
     /**
      * Read a list of document versions for the latest container version.
      * 
      * @param containerId
      *            The container id.
      * @return A list of document versions.
      */
     List<DocumentVersion> readDocumentVersions(final Long containerId) {
         logger.info(getApiId("[READ DOCUMENT VERSIONS]"));
         logger.debug(containerId);
         final ContainerVersion latestVersion = readLatestVersion(containerId);
         return containerIO.readDocumentVersions(containerId, latestVersion.getVersionId());
     }
 
     /**
      * Read a container draft.
      * 
      * @param containerId
      *            A container id.
      * @return A container draft.
      */
     ContainerDraft readDraft(final Long containerId) {
         logger.info(getApiId("[READ DRAFT]"));
         logger.debug(containerId);
         final InternalDocumentModel documentModel = getInternalDocumentModel();
         final ContainerDraft draft = containerIO.readDraft(containerId);
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
     }
 
     /**
      * Read the container history.
      * 
      * @param containerId
      *            A container id.
      * @return A list of history items.
      */
     List<ContainerHistoryItem> readHistory(final Long containerId) {
         logger.info(getApiId("[READ HISTORY]"));
         logger.debug(containerId);
         return readHistory(containerId, defaultHistoryComparator, defaultHistoryFilter);
     }
 
     /**
      * Read the container history.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A history item comparator
      * @return A list of history items.
      */
     List<ContainerHistoryItem> readHistory(final Long containerId,
             final Comparator<? super HistoryItem> comparator) {
         logger.info(getApiId("[READ HISTORY]"));
         logger.debug(containerId);
         logger.debug(comparator);
         return readHistory(containerId, comparator, defaultHistoryFilter);
     }
 
     /**
      * Read the container history.
      * 
      * @param containerId
      *            A container id.
      * @param comparator
      *            A history item comparator
      * @param filter
      *            A history item filter.
      * @return A list of history items.
      */
     List<ContainerHistoryItem> readHistory(final Long containerId,
             final Comparator<? super HistoryItem> comparator,
             final Filter<? super HistoryItem> filter) {
         logger.info(getApiId("[READ HISTORY]"));
         logger.debug(containerId);
         logger.debug(comparator);
         logger.debug(filter);
         final ContainerHistoryBuilder historyBuilder =
             new ContainerHistoryBuilder(getInternalContainerModel(), l18n);
         final List<ContainerHistoryItem> history = historyBuilder.createHistory(containerId);
         HistoryFilterManager.filter(history, filter);
         ModelSorter.sortHistory(history, defaultHistoryComparator);
         return history;
     }
 
     /**
      * Read the container history.
      * 
      * @param containerId
      *            A container id.
      * @param filter
      *            A history item filter.
      * @return A list of history items.
      */
     List<ContainerHistoryItem> readHistory(final Long containerId,
             final Filter<? super HistoryItem> filter) {
         logger.info(getApiId("[READ HISTORY]"));
         logger.debug(containerId);
         logger.debug(filter);
         return readHistory(containerId, defaultHistoryComparator, filter);
     }
 
     /**
      * Read the latest container version.
      * 
      * @param containerId
      *            A container id.
      * @return A container version.
      */
     ContainerVersion readLatestVersion(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         if (doesExistLatestVersion(containerId)) {
             return containerIO.readLatestVersion(containerId);
         } else {
             return null;
         }
     }
 
     /**
      * Read a list of team members the container version was published to.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readPublishedTo(final Long containerId,
             final Long versionId) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         return readPublishedTo(containerId, versionId, defaultUserComparator,
                 defaultUserFilter);
     }
 
     /**
      * Read a list of team members the container version was published to.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param comparator
      *            A <code>Comparator&lt;User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readPublishedTo(final Long containerId,
             final Long versionId, final Comparator<User> comparator) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("comparator", comparator);
         return readPublishedTo(containerId, versionId, comparator,
                 defaultUserFilter);
     }
 
     /**
      * Read a list of team members the container version was published to.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param comparator
      *            A <code>Comparator&lt;User&gt;</code>.
      * @param filter
      *            A <code>Filter&lt;? super User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readPublishedTo(final Long containerId,
             final Long versionId, final Comparator<User> comparator,
             final Filter<? super User> filter) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("comparator", comparator);
         logVariable("filter", filter);
         final List<User> publishedTo =
             containerIO.readPublishedTo(containerId, versionId);
         UserFilterManager.filter(publishedTo, filter);
         ModelSorter.sortUsers(publishedTo, comparator);
         return publishedTo;
     }
 
     /**
      * Read a list of team members the container version was published to.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param filter
      *            A <code>Filter&lt;? super User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readPublishedTo(final Long containerId,
             final Long versionId, final Filter<? super User> filter) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("filter", filter);
         return readPublishedTo(containerId, versionId, defaultUserComparator,
                 filter); 
     }
 
     /**
      * Read a list of users the container version was shared with.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readSharedWith(final Long containerId,
             final Long versionId) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         return readSharedWith(containerId, versionId, defaultUserComparator,
                 defaultUserFilter);
     }
 
     /**
      * Read a list of users the container version was shared with.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.\
      * @param comparator
      *            A <code>Comparator&lt;User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readSharedWith(final Long containerId,
             final Long versionId, final Comparator<User> comparator) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("comparator", comparator);
         return readSharedWith(containerId, versionId, comparator,
                 defaultUserFilter);
     }
 
     /**
      * Read a list of users the container version was shared with.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param comparator
      *            A <code>Comparator&lt;User&gt;</code>.
      * @param filter
      *            A <code>Filter&lt;? super User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readSharedWith(final Long containerId,
             final Long versionId, final Comparator<User> comparator,
             final Filter<? super User> filter) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("comparator", comparator);
         logVariable("filter", filter);
         final List<User> sharedWith =
             containerIO.readSharedWith(containerId, versionId);
         UserFilterManager.filter(sharedWith, filter);
         ModelSorter.sortUsers(sharedWith, comparator);
         return sharedWith;
     }
 
     /**
      * Read a list of users the container version was shared with.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param filter
      *            A <code>Filter&lt;? super User&gt;</code>.
      * @return A <code>List&lt;User&gt;</code>.
      */
     List<User> readSharedWith(final Long containerId,
             final Long versionId, final Filter<? super User> filter) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("filter", filter);
         return readSharedWith(containerId, versionId, defaultUserComparator,
                 filter);
     }
 
     /**
      * Read the team for the container.
      * 
      * @param containerId
      *            A container id.
      * @return A list of users.
      */
     List<TeamMember> readTeam(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         return getInternalArtifactModel().readTeam2(containerId);
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
     ContainerVersion readVersion(final Long containerId, final Long versionId) {
         logger.info(getApiId("[READ VERSION]"));
         logger.debug(containerId);
         logger.debug(versionId);
         return containerIO.readVersion(containerId, versionId);
     }
 
     /**
      * Read the container versions.
      * 
      * @param containerId
      *            The container id.
      * @return A list of container versions.
      */
     List<ContainerVersion> readVersions(final Long containerId) {
         logger.info(getApiId("[READ VERSIONS]"));
         logger.debug(containerId);
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
     List<ContainerVersion> readVersions(final Long containerId,
             final Comparator<ArtifactVersion> comparator) {
         logger.info(getApiId("[READ VERSIONS]"));
         logger.debug(containerId);
         logger.debug(comparator);
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
     List<ContainerVersion> readVersions(final Long containerId,
             final Comparator<ArtifactVersion> comparator,
             final Filter<? super ArtifactVersion> filter) {
         logger.info(getApiId("[READ VERSIONS]"));
         logger.debug(containerId);
         logger.debug(comparator);
         logger.debug(filter);
         final List<ContainerVersion> versions = containerIO.readVersions(containerId);
         ArtifactFilterManager.filterVersions(versions, filter);
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
     List<ContainerVersion> readVersions(final Long containerId,
             final Filter<? super ArtifactVersion> filter) {
         logger.info(getApiId("[READ VERSIONS]"));
         logger.debug(containerId);
         logger.debug(filter);
         return readVersions(containerId, defaultVersionComparator, filter);
     }
 
     /**
      * Remove a document from a container.
      * 
      * @param containerId
      *            A container id.
      * @param documentId
      *            A document id.
      */
     void removeDocument(final Long containerId, final Long documentId) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("documentId", documentId);
         assertDraftExists("DRAFT DOES NOT EXIST", containerId);
         assertDraftArtifactStateTransition("INVALID DRAFT DOCUMENT STATE",
                 containerId, documentId, ContainerDraft.ArtifactState.REMOVED);
         final ContainerDraft draft = readDraft(containerId);
         final Document document = draft.getDocument(documentId);
         containerIO.deleteDraftArtifactRel(containerId, document.getId());
         switch (draft.getState(document.getId())) {
         case ADDED:     // delete the document
             getInternalDocumentModel().delete(document.getId());
             break;
         case NONE:
             containerIO.createDraftArtifactRel(
                     containerId, document.getId(), ContainerDraft.ArtifactState.REMOVED);
             break;
         case REMOVED:   // fall through
         default:
             Assert.assertUnreachable("UNKNOWN ARTIFACT STATE");
         }
 
         final Container postAdditionContainer = read(containerId);        
         final ContainerDraft postAdditionDraft = readDraft(containerId);
         notifyDocumentRemoved(postAdditionContainer, postAdditionDraft, document, localEventGenerator);
     }
 
     /**
      * Remove a container listener.
      * 
      * @param listener
      *            A container listener.
      */
     void removeListener(final ContainerListener listener) {
         logger.info(getApiId("[REMOVE LISTENER]"));
         logger.debug(listener);
         Assert.assertNotNull(getApiId("[REMOVE LISTENER] [LISTENER IS NULL]"),
                 listener);
         synchronized(LISTENERS) {
             if(!LISTENERS.contains(listener)) { return; }
             LISTENERS.remove(listener);
         }
     }
 
     /**
      * Rename the container.
      * 
      * @param containerId
      *            A container id.
      * @param name
      *            The new container name.
      */
     void rename(final Long containerId, final String name) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("name", name);
         assertIsNotDistributed("CONTAINER HAS BEEN DISTRIBUTED", containerId);
         containerIO.updateName(containerId, name);
         // fire event
         notifyContainerUpdated(read(containerId), localEventGenerator);
     }
 
     /**
      * Revert a document to it's pre-draft state.
      * 
      * @param documentId
      *            A document id.
      */
     void revertDocument(final Long containerId, final Long documentId) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("documentId", documentId);
         assertDraftExists("DRAFT DOES NOT EXIST", containerId);
         assertDraftArtifactStateTransition("INVALID DRAFT DOCUMENT STATE",
                 containerId, documentId, ContainerDraft.ArtifactState.NONE);
         assertDoesExistLatestVersion("LATEST VERSION DOES NOT EXIST", containerId);
         final ContainerDraft draft = readDraft(containerId);
         final Document document = draft.getDocument(documentId);
         containerIO.deleteDraftArtifactRel(containerId, document.getId());
         containerIO.createDraftArtifactRel(containerId, document.getId(),
                 ContainerDraft.ArtifactState.NONE);
         getInternalDocumentModel().revertDraft(documentId);
     }
 
     /**
      * Search for containers.
      * 
      * @param expression
      *            A search expression <code>String</code>.
      * @return A <code>List&lt;Long&gt;</code>.
      */
     List<Long> search(final String expression) {
         logApiId();
         logVariable("expression", expression);
         try {
             final InternalIndexModel indexModel = getIndexModel();
             final List<Long> containerIds = indexModel.searchContainers(expression);
             containerIds.addAll(indexModel.searchDocuments(expression));
             return containerIds;
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * Share a version of the container with a list of users.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A version id.
      * @param contacts
      *            A <code>List&lt;Contact&gt;</code>.
      * @param teamMembers
      *            A <code>List&lt;TeamMember&gt;</code>.
      */
     void share(final Long containerId, final Long versionId,
             final List<Contact> contacts, final List<TeamMember> teamMembers) {
         logApiId();
         logVariable("containerId", containerId);
         logVariable("versionId", versionId);
         logVariable("contacts", contacts);
         logVariable("teamMembers", teamMembers);
         assertDoesExistVersion("VERSION DOES NOT EXIST", containerId, versionId);
         assertOnline("USER NOT ONLINE");
         final List<JabberId> shareWith = new ArrayList<JabberId>(contacts.size() + teamMembers.size());
         for (final Contact contact : contacts) {
             if (!contains(teamMembers, contact)) {
                 shareWith.add(contact.getId());
             }
         }
         for (final TeamMember teamMember : teamMembers) {
             shareWith.add(teamMember.getId());
         }
         getInternalSessionModel().send(
                 readVersion(containerId, versionId),
                 readDocumentVersionStreams(containerId, versionId),
                 shareWith, localUserId(), currentDateTime());
     }
 
     /**
      * Subscribe to the container's team.
      * 
      * @param containerId
      *            A container id.
      */
     void subscribe(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         assertNotTeamMember("USER A TEAM MEMBER", containerId, localUserId());
         final InternalArtifactModel artifactModel = getInternalArtifactModel();
         final UUID containerUniqueId = artifactModel.readUniqueId(containerId);
         artifactModel.removeTeamMember(containerId, localUserId());
         getInternalSessionModel().removeTeamMember(containerUniqueId, localUserId());
     }
 
     /**
      * Unsubscribe from the container's team.
      * 
      * @param containerId
      *            A container id.
      */
     void unsubscribe(final Long containerId) {
         logApiId();
         logVariable("containerId", containerId);
         assertTeamMember("USER NOT A TEAM MEMBER", containerId, localUserId());
         final InternalArtifactModel artifactModel = getInternalArtifactModel();
         final UUID containerUniqueId = artifactModel.readUniqueId(containerId);
         artifactModel.addTeamMember(containerId, localUserId());
         getInternalSessionModel().addTeamMember(containerUniqueId, localUserId());
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
             case NONE:      // valid state
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
      * Assert a draft exists for the container.
      * 
      * @param assertion
      *            An assertion.
      * @param containerId
      *            A container id.
      * @see #assertContainerDraftExists(Object, Long)
      */
     private void assertDraftExists(final Object assertion,
             final Long containerId) {
         assertContainerDraftExists(assertion, containerId);
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
      * Create the container in the distributed network.
      * 
      * @param container
      *            The container.
      */
     private void createDistributed(final Container container) {
         final InternalSessionModel sessionModel = getInternalSessionModel();
         sessionModel.createArtifact(container.getUniqueId());
         sessionModel.addTeamMember(container.getUniqueId(), localUserId());
     }
 
     /**
      * Create the first draft for a cotnainer.
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
         draft.setOwner(owner);
         containerIO.createDraft(draft);
         // fire draft event
         final Container postCreation = read(containerId);
         final ContainerDraft postCreationDraft = readDraft(containerId);
         notifyDraftCreated(postCreation, postCreationDraft, localEventGenerator);
         return postCreationDraft;
     }
 
     /**
      * Create the team for a container. The team will consist of the local user
      * only.
      * 
      * @param containerId
      *            A container id.
      * @return The created <code>TeamMember</code>.
      */
     private TeamMember createTeam(final Long containerId) {
         final List<TeamMember> team =
             getInternalArtifactModel().createTeam(containerId);
         return team.get(0);
     }
 
     /**
      * Create a container version.
      * 
      * @param containerId
      *            A container id.
      * @return A container version.
      */
     private ContainerVersion createVersion(final Long containerId) {
         return createVersion(containerId, readNextVersionId(containerId),
                 localUserId(), currentDateTime());
     }
 
     /**
      * Create a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A container version id.
      * @param createdBy
      *            By whom the container was created.
      * @param createdOn
      *            When the container was created.
      * @return The version.
      */
     private ContainerVersion createVersion(final Long containerId,
             final Long versionId, final JabberId createdBy,
             final Calendar createdOn) {
         final Container container = read(containerId);
         
         final ContainerVersion version = new ContainerVersion();
         version.setArtifactId(container.getId());
         version.setArtifactType(container.getType());
         version.setArtifactUniqueId(container.getUniqueId());
         version.setCreatedBy(createdBy.getUsername());
         version.setCreatedOn(createdOn);
         version.setName(container.getName());
         version.setUpdatedBy(container.getCreatedBy());
         version.setUpdatedOn(container.getCreatedOn());
         version.setVersionId(versionId);
         containerIO.createVersion(version);
 
         return containerIO.readVersion(version.getArtifactId(), version.getVersionId());
         
     }
 
     /**
      * Delete the local info for this container.
      * 
      * @param containerId
      *            The container id.
      */
     private void deleteLocal(final Long containerId) {
         // delete the draft
         final ContainerDraft draft = readDraft(containerId);
         if (null != draft) {
             for(final Artifact artifact : draft.getArtifacts()) {
                 containerIO.deleteDraftArtifactRel(containerId, artifact.getId());
             }
             containerIO.deleteDraft(containerId);
         }
         // delete the team
         final InternalArtifactModel artifactModel = getInternalArtifactModel();
         artifactModel.deleteTeam(containerId);
         // delete the remote info
         artifactModel.deleteRemoteInfo(containerId);
         // delete the audit events
         getInternalAuditModel().delete(containerId);
         // delete versions
         final InternalDocumentModel documentModel = getInternalDocumentModel();
         final List<ContainerVersion> versions = readVersions(containerId);
         List<Document> documents;
         for (final ContainerVersion version : versions) {
             // remove the version's artifact versions
             containerIO.removeVersions(containerId, version.getVersionId());
 
             documents = containerIO.readDocuments(version.getArtifactId(), version.getVersionId());
             for(final Document document : documents) {
                 documentModel.delete(document.getId());
             }
             // delete the version
             containerIO.deleteVersion(containerId, version.getVersionId());
         }
         // delete the index
         getIndexModel().deleteContainer(containerId);
         // delete the container
         containerIO.delete(containerId);
     }
 
     /**
      * Determine whether or not a local draft exists.
      * 
      * @param containerId
      *            A container id.
      * @return True if a draft exists; and the draft owner is the current user.
      */
     private Boolean doesExistLocalDraft(final Long containerId) {
         final ContainerDraft draft = readDraft(containerId);
         if(null != draft) {
             return draft.getOwner().getId().equals(localUserId());
         }
         else { return Boolean.FALSE; }
     }
 
     /**
      * Handle a document published remote event.
      * 
      * @param uniqueId
      *            The document unique id <code>UUID</code>.
      * @param versionId
      *            The document version id <code>Long</code>.
      * @param name
      *            The document name <code>String</code>
      * @param checksum
      *            The document checksum <code>String</code>.
      * @param bytes
      *            The document bytes <code>byte[]</code>.
      * @param publishedBy
      *            The publish user <code>JabberId</code>.
      * @param publishedOn
      *            The publish date <code>Calendar</code>.
      * @return A new <code>DocumentVersion</code>.
      * 
      * @throws IOException
      * 
      * @see InternalDocumentModel#handleDocumentPublished(JabberId, Calendar, UUID, Long, String, String, InputStream)
      */
     private DocumentVersion handleDocumentPublished(final UUID uniqueId,
             final Long versionId, final String name,
             final String checksum, final byte[] bytes, final JabberId publishedBy,
             final Calendar publishedOn) throws IOException {
         final InternalDocumentModel documentModel = getInternalDocumentModel();
         final InputStream inputStream =
             new BufferedInputStream(new ByteArrayInputStream(bytes), IO.BUFFER_SIZE);
         try {
             final DocumentVersion version =
                     documentModel.handleDocumentPublished(publishedBy,
                             publishedOn, uniqueId, versionId, name, checksum,
                             inputStream);
             return version;
         }
         finally { inputStream.close(); }
     }
 
 
     /**
      * Handle the receipt of a document.
      * 
      * @param sentBy
      *            By whom the document was sent.
      * @param sentOn
      *            When the document was sent.
      * @param uniqueId
      *            A unique id.
      * @param name
      *            A name.
      * @param bytes
      *            A byte array.
      * @return A document version.
      * @throws IOException
      */
     private DocumentVersion handleDocumentSent(final JabberId sentBy,
             final Calendar sentOn, final UUID uniqueId, final Long versionId,
             final String name, final String checksum, final byte[] bytes)
             throws IOException {
         final InternalDocumentModel documentModel = getInternalDocumentModel();
         final InputStream inputStream =
             new BufferedInputStream(new ByteArrayInputStream(bytes), IO.BUFFER_SIZE);
         try {
             final DocumentVersion version = documentModel.handleDocumentSent(
                     sentBy, sentOn, uniqueId, versionId, checksum, name, inputStream);
             return version;
         }
         finally { inputStream.close(); }
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
     private Boolean isDistributed(final Long containerId) {
         return getInternalArtifactModel().doesVersionExist(containerId, Versioning.START);
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
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.containerCreated(eventGenerator.generate(container));
             }
         }
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
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.containerDeleted(eventGenerator.generate(container));
             }
         }
     }
 
     /**
      * Fire a container published event.
      * 
      * @param container
      *            A container.
      * @param version
      *            A container version.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerPublished(final Container container,
             final ContainerDraft draft, final ContainerVersion version,
             final ContainerEventGenerator eventGenerator) {
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.draftPublished(eventGenerator.generate(container, draft, version));
             }
         }
     }
 
     /**
      * Fire a container shared event.
      * 
      * @param container
      *            A container.
      * @param version
      *            A container version.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerShared(final Container container,
             final ContainerVersion version,
             final ContainerEventGenerator eventGenerator) {
         synchronized (LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.containerShared(eventGenerator.generate(container, version));
             }
         }
     }
 
     /**
      * Notify that a container has been updated.
      * 
      * @param container
      *            A container.
      * @param eventGenerator
      *            A container event generator.
      */
     private void notifyContainerUpdated(final Container container,
             final ContainerEventGenerator eventGenerator) {
         synchronized (LISTENERS) {
             for (final ContainerListener l : LISTENERS) {
                 l.containerUpdated(eventGenerator.generate(container));
             }
         }
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
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.documentAdded(eventGenerator.generate(container, draft, document));
             }
         }
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
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.documentRemoved(eventGenerator.generate(container, draft, document));
             }
         }
         
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
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.draftCreated(eventGenerator.generate(container, draft));
             }
         }
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
         synchronized (LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.draftDeleted(eventGenerator.generate(container, draft));
             }
         }
     }
 
     /**
      * Notify that a team member was added to the container's team.
      * 
      * @param container
      *            A container.
      * @param teamMember
      *            A team member.
      * @param eventGenerator
      *            An event generator.
      */
     @SuppressWarnings("unused")
     private void notifyTeamMemberAdded(final Container container,
             final TeamMember teamMember,
             final ContainerEventGenerator eventGenerator) {
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.teamMemberAdded(eventGenerator.generate(container, teamMember));
             }
         }
     }
 
     /**
      * Notify that a team member was added to the container's team.
      * 
      * @param container
      *            A container.
      * @param teamMember
      *            A team member.
      * @param eventGenerator
      *            An event generator.
      */
     @SuppressWarnings("unused")
     private void notifyTeamMemberRemoved(final Container container,
             final TeamMember teamMember,
             final ContainerEventGenerator eventGenerator) {
         synchronized(LISTENERS) {
             for(final ContainerListener l : LISTENERS) {
                 l.teamMemberRemoved(eventGenerator.generate(container, teamMember));
             }
         }
     }
 
     /**
      * Publish a container.
      * 
      * @param container
      *            A container.
      * @param publishTo
      *            A list of ids to publish to.
      * @param publishedBy
      *            The publisher.
      * @param publishedOn
      *            The publish date.
      */
     private void publish(final ContainerVersion container,
             final List<JabberId> publishTo, final JabberId publishedBy,
             final Calendar publishedOn) {
         getInternalSessionModel().publish(
                 container,
                 readDocumentVersionStreams(container.getArtifactId(), container
                         .getVersionId()), publishTo, publishedBy, publishedOn);
     }
 
     /**
      * Read the document version streams for a container version.
      * 
      * @param containerId
      *            A container id.
      * @param versionId
      *            A container version id.
      * @return A list of document versions and their input streams.
      */
     private Map<DocumentVersion, InputStream> readDocumentVersionStreams(
             final Long containerId, final Long versionId) {
         final InternalDocumentModel documentModel = getInternalDocumentModel();
         final Map<DocumentVersion, InputStream> documentVersionStreams =
             new HashMap<DocumentVersion, InputStream>();
         final List<Document> documents = readDocuments(containerId, versionId);
         DocumentVersion documentVersion;
         for(final Document document : documents) {
             documentVersion = documentModel.readLatestVersion(document.getId());
             documentVersionStreams.put(documentVersion,
                     documentModel.openVersionStream(
                             documentVersion.getArtifactId(),
                             documentVersion.getVersionId()));
         }
         return documentVersionStreams;
     }
 }
