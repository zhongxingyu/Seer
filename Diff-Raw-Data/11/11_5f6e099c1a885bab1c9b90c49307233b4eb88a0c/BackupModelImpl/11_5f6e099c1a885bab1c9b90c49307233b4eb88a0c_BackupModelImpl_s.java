 /*
  * Generated On: Oct 04 06 10:14:14 AM
  */
 package com.thinkparity.desdemona.model.backup;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.PublishedToEMail;
 import com.thinkparity.codebase.model.contact.OutgoingEMailInvitation;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.InternalModelFactory;
 import com.thinkparity.ophelia.model.container.InternalContainerModel;
 import com.thinkparity.ophelia.model.document.CannotLockException;
 
 import com.thinkparity.desdemona.model.AbstractModelImpl;
 import com.thinkparity.desdemona.model.Constants.Product.Ophelia;
 import com.thinkparity.desdemona.model.artifact.InternalArtifactModel;
 import com.thinkparity.desdemona.model.contact.InternalContactModel;
 import com.thinkparity.desdemona.model.contact.invitation.ContainerVersionAttachment;
 import com.thinkparity.desdemona.model.io.sql.ArtifactSql;
 
 /**
  * <b>Title:</b>thinkParity Backup Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class BackupModelImpl extends AbstractModelImpl implements BackupModel,
         InternalBackupModel {
 
     /** An artifact sql interface */
     private ArtifactSql artifactSql;
 
     /**
      * Create BackupModelImpl.
      * 
      */
     public BackupModelImpl() {
         super();
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#delete(com.thinkparity.codebase.jabber.JabberId,
      *      java.util.UUID)
      * 
      */
     public void delete(final UUID uniqueId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 deleteImpl(user, uniqueId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     public Boolean isBackedUp(final Artifact artifact) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return Boolean.valueOf(isContainerBackedUpImpl(user,
                       artifact.getUniqueId()));
             } else {
                 logger.logWarning("User {0} has no backup feature.");
                 return null;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     public Boolean isBackupEnabled() {
         try {
             return isBackupEnabledImpl(user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readArtifactTeam(java.lang.Long)
      *
      */
     public List<TeamMember> readArtifactTeam(final UUID uniqueId) {
         try {
             return readArtifactTeamImpl(uniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readContainer(java.util.UUID)
      * 
      */
     public Container readContainer(final UUID uniqueId) {
         try {
             return readContainerImpl(uniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerAuth(java.util.UUID)
      *
      */
     @Override
     public Container readContainerAuth(final UUID uniqueId) {
         try {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().read(containerId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerDocumentVersions(java.lang.Long, java.lang.Long)
      *
      */
     public List<DocumentVersion> readContainerDocumentVersions(
             final UUID uniqueId, final Long versionId) {
         try {
             return readContainerDocumentVersionsImpl(uniqueId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerDocumentVersionsAuth(java.util.UUID, java.lang.Long)
      *
      */
     @Override
     public List<DocumentVersion> readContainerDocumentVersionsAuth(
             final UUID uniqueId, final Long versionId) {
         try {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readDocumentVersions(
                     containerId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerLatestVersion(java.lang.Long)
      *
      */
     public ContainerVersion readContainerLatestVersion(final UUID uniqueId) {
         try {
             return readContainerLatestVersionImpl(uniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readContainers(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public List<Container> readContainers() {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainersImpl(user);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainersForDocumentAuth(com.thinkparity.codebase.model.document.Document)
      * 
      */
     public List<Container> readContainersForDocumentAuth(final Document document) {
         try {
             final InternalModelFactory modelFactory = getModelFactory();
             return modelFactory.getContainerModel().readForDocument(document.getId());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerVersion(java.util.UUID, java.lang.Long)
      *
      */
     public ContainerVersion readContainerVersion(final UUID uniqueId,
             final Long versionId) {
         try {
             return readContainerVersionImpl(uniqueId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readContainerVersionAuth(java.util.UUID, java.lang.Long)
      *
      */
     @Override
     public ContainerVersion readContainerVersionAuth(final UUID uniqueId,
             final Long versionId) {
         try {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readVersion(containerId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readContainerVersions(com.thinkparity.codebase.jabber.JabberId, java.util.UUID)
      *
      */
     public List<ContainerVersion> readContainerVersions(final UUID uniqueId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainerVersionsImpl(user, uniqueId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readDocumentAuth(java.util.UUID)
      * 
      */
     public Document readDocumentAuth(final UUID uniqueId) {
         try {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long documentId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getDocumentModel().read(documentId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readDocuments(java.util.UUID,
      *      java.lang.Long)
      * 
      */
     public List<Document> readDocuments(final UUID containerUniqueId,
             final Long containerVersionId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainerDocumentsImpl(user, containerUniqueId,
                         containerVersionId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readContainerDocumentVersions(com.thinkparity.codebase.jabber.JabberId, java.util.UUID, java.lang.Long)
      *
      */
     public List<DocumentVersion> readDocumentVersions(
             final UUID containerUniqueId, final Long containerVersionId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainerDocumentVersionsImpl(user, containerUniqueId, containerVersionId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readContainerPublishedTo(com.thinkparity.codebase.jabber.JabberId, java.util.UUID, java.lang.Long)
      *
      */
     public List<ArtifactReceipt> readPublishedTo(final UUID containerUniqueId,
             final Long containerVersionId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainerPublishedToImpl(user, containerUniqueId,
                         containerVersionId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.InternalBackupModel#readPublishedToAuth(java.util.UUID, java.lang.Long)
      *
      */
     public List<ArtifactReceipt> readPublishedToAuth(final UUID uniqueId, final Long versionId) {
         try {
             /* note that we do not explicity check if backup is enabled here
              * because the "server" needs to know this in order to correctly
              * formulate the artifact receipt events; as well as delayed forward
              * as a result of accepting invitations */
             return readContainerPublishedToImplAuth(user, uniqueId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readPublishedToEMails(java.util.UUID,
      *      java.lang.Long)
      * 
      */
     public List<PublishedToEMail> readPublishedToEMails(
             final UUID containerUniqueId, final Long containerVersionId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readContainerPublishedToEMailsImpl(user,
                         containerUniqueId, containerVersionId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.backup.BackupModel#readTeamIds(com.thinkparity.codebase.jabber.JabberId,
      *      java.util.UUID)
      * 
      */
     public List<JabberId> readTeamIds(final UUID uniqueId) {
         try {
             if (isBackupEnabledImpl(user)) {
                 return readTeamIdsImpl(user, uniqueId);
             } else {
                 logger.logWarning("User {0} has no backup feature.",
                         user.getId());
                 return Collections.emptyList();
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.AbstractModelImpl#initialize()
      *
      */
     @Override
     protected void initialize() {
         this.artifactSql = new ArtifactSql();
     }
 
     /**
      * Delete implementation.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A unique id <code>UUID</code>.
      */
     private void deleteImpl(final User user, final UUID uniqueId)
             throws CannotLockException {
         final Artifact artifact = getArtifactModel().read(uniqueId);
         if (!isContainerBackedUpImpl(uniqueId)) {
             final com.thinkparity.ophelia.model.container.InternalContainerModel
                     containerModel = getModelFactory(user).getContainerModel();
             final com.thinkparity.ophelia.model.artifact.InternalArtifactModel
                     artifactModel = getModelFactory(user).getArtifactModel();
             final Long containerId = artifactModel.readId(uniqueId);
             containerModel.delete(containerId);
         }
         if (0 == artifactSql.readTeamRelCount(artifact.getId())) {
             artifactSql.delete(artifact.getId());
         }
     }
 
     /**
      * Obtain the backup's model factory.
      * 
      * @return An <code>InternalModelFactory</code>.
      */
     private InternalModelFactory getModelFactory() {
         return BackupService.getInstance().getModelFactory();
     }
 
     /**
      * Obtain a user's model factory.
      * 
      * @param user
      *            A <code>User</code>.
      * @return An <code>InternalModelFactory</code>.
      */
     private InternalModelFactory getModelFactory(final User user) {
         if (isBackupEnabledImpl(user)) {
             return BackupService.getInstance().getModelFactory();
         } else {
             throw new BackupException("User {0} has no backup feature.",
                     user.getId());
         }
     }
 
     /**
      * Determine whether or not backup is enabled for the user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return True if backup is enabled.
      */
     private boolean isBackupEnabledImpl(final User user) {
         /* NOTE the product id/name should be read from the interface once the
          * migrator code is complete */
         final List<Feature> features = getUserModel(user).readFeatures(Ophelia.PRODUCT_ID);
         for (final Feature feature : features) {
             if (Ophelia.Feature.BACKUP.equals(feature.getName())) {
                 return Boolean.TRUE;
             }
         }
         return Boolean.FALSE;
     }
 
     /**
      * Determine if the user has backed up the container. We check the server
      * team membership.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            An artifact unique id <code>UUID</code>.
      * @return True if the user is either a team member.
      */
     private boolean isContainerBackedUpImpl(final User user, final UUID uniqueId) {
         return isTeamMember(user, uniqueId);
     }
 
     /**
      * Determine if the container is backed up. We check the server team
      * membership.
      * 
      * @param artifact
      *            An <code>Artifact</code>.
      * @return True if the user is either a team member.
      */
     private boolean isContainerBackedUpImpl(final UUID uniqueId) {
         final Artifact artifact = getArtifactModel().read(uniqueId);
         return 0 < artifactSql.readTeamRelCount(artifact.getId());
     }
 
     /**
      * Determine if the user is a team member.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            An artifact unique id <code>UUID</code>.
      * @return True if the user is a team member.
      */
     private boolean isTeamMember(final User user, final UUID uniqueId) {
         final InternalArtifactModel artifactModel = getArtifactModel();
         final Artifact artifact = artifactModel.read(uniqueId);
         final List<TeamMember> team = artifactModel.readTeam(artifact.getId());
         for (final TeamMember teamMember : team) {
             if (teamMember.getLocalId().equals(user.getLocalId()))
                 return true;
         }
         return false;
     }
 
     private List<TeamMember> readArtifactTeamImpl(final UUID uniqueId) {
         if (isContainerBackedUpImpl(uniqueId)) {
             final Long containerId = getModelFactory().getArtifactModel().readId(uniqueId);
             return getModelFactory().getArtifactModel().readTeam2(containerId);
         } else {
             logger.logWarning("Container {0} is not backed up.", uniqueId);
             return Collections.emptyList();
         }
     }
 
     /**
      * Read a list of backed up container ids for a user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List</code> of container <code>UUID</code>s.
      */
     private List<UUID> readBackedUpContainerIds(final User user) {
         final List<UUID> backedUpContainerIds = getArtifactModel().readTeamArtifactIds(user);
         return backedUpContainerIds;
     }
 
     /**
      * Read the container documents implementation. If the user is a team member
      * read the documents from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List</code> of <code>Document</code>s.
      */
     private List<Document> readContainerDocumentsImpl(final User user,
             final UUID uniqueId, final Long versionId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory(user);
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readDocuments(containerId, versionId);
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
     }
 
     /**
      * Read the container document versions implementation. If the user is a
      * team member read the document versions from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List</code> of <code>DocumentVersion</code>s.
      */
     private List<DocumentVersion> readContainerDocumentVersionsImpl(
             final User user, final UUID uniqueId, final Long versionId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory(user);
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readDocumentVersions(containerId, versionId);
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
     }
 
     private List<DocumentVersion> readContainerDocumentVersionsImpl(
             final UUID uniqueId, final Long versionId) {
         if (isContainerBackedUpImpl(uniqueId)) {
             final Long containerId = getModelFactory().getArtifactModel().readId(uniqueId);
             return getModelFactory().getContainerModel().readDocumentVersions(containerId, versionId);
         } else {
             logger.logWarning("Container {0} is not backed up.", uniqueId);
             return Collections.emptyList();
         }
     }
 
     private Container readContainerImpl(final UUID uniqueId) {
         if (isContainerBackedUpImpl(uniqueId)) {
             final Long containerId = getModelFactory().getArtifactModel().readId(uniqueId);
             return getModelFactory().getContainerModel().read(containerId);
         } else {
             logger.logWarning("Container {0} is not backed up.", uniqueId);
             return null;
         }
     }
 
     /**
      * Read the latest container version implementation. Read the container from
      * the backup.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @return A <code>ContainerVersion</code>.
      */
     private ContainerVersion readContainerLatestVersionImpl(final UUID uniqueId) {
         if (isContainerBackedUpImpl(uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readLatestVersion(containerId);
         } else {
             logger.logWarning("Container {0} is not backed up.", uniqueId);
             return null;
         }
     }
 
     /**
      * Read the container version published to e-mails implementation. If the
      * user is a member of the team read the published to e-mail list is
      * interpolated from the user's outgoing e-mail invitation attachments.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List<PublishedToEMail></code>.
      */
     private List<PublishedToEMail> readContainerPublishedToEMailsImpl(
             final User user, final UUID uniqueId, final Long versionId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final List<PublishedToEMail> publishedTo = new ArrayList<PublishedToEMail>();
             final InternalContactModel contactModel = getContactModel();
             final List<OutgoingEMailInvitation> invitations =
                 getContactModel().readOutgoingEMailInvitations();
             final List<ContainerVersionAttachment> attachments =
                 new ArrayList<ContainerVersionAttachment>();
             for (final OutgoingEMailInvitation invitation : invitations) {
                 attachments.clear();
                 attachments.addAll(
                         contactModel.readContainerVersionInvitationAttachments(
                                 user.getId(), invitation));
                 for (final ContainerVersionAttachment attachment : attachments) {
                    if (attachment.getUniqueId().equals(uniqueId)) {
                         final PublishedToEMail pte = new PublishedToEMail();
                         pte.setEMail(invitation.getInvitationEMail());
                         pte.setPublishedOn(invitation.getCreatedOn());
                         publishedTo.add(pte);
                     }
                 }
             }
             return publishedTo;
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
     }
 
     /**
      * Read the container version published to implementation. If the user is a
      * member of the team read the published to list from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List</code> of <code>ArtifactReceipt</code>s.
      */
     private List<ArtifactReceipt> readContainerPublishedToImpl(final User user,
             final UUID uniqueId, final Long versionId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory(user);
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readPublishedTo(containerId, versionId);
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
 
     }
 
     /**
      * Read the container version published to implementation. If the user is a
      * member of the team read the published to list from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List</code> of <code>ArtifactReceipt</code>s.
      */
     private List<ArtifactReceipt> readContainerPublishedToImplAuth(final User user,
             final UUID uniqueId, final Long versionId) {
         final InternalModelFactory modelFactory = getModelFactory();
         final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
         return modelFactory.getContainerModel().readPublishedTo(containerId, versionId);
     }
 
     /**
      * Read the containers implementation. Read the containers that are being
      * backed up for the user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List</code> of <code>Container</code>s.
      */
     private List<Container> readContainersImpl(final User user) {
         final List<UUID> backedUpContainerIds = readBackedUpContainerIds(user);
         final List<Container> backedUpContainers = new ArrayList<Container>(
                 backedUpContainerIds.size());
         final com.thinkparity.ophelia.model.container.InternalContainerModel
                 containerModel = getModelFactory(user).getContainerModel();
         final com.thinkparity.ophelia.model.artifact.InternalArtifactModel
                 artifactModel = getModelFactory(user).getArtifactModel();
         Long containerId;
         Container container;
         for (final UUID backedUpContainerId : backedUpContainerIds) {
             containerId = artifactModel.readId(backedUpContainerId);
             if (null == containerId) {
                 logger.logWarning("Container {0} no longer exists in the backup.", backedUpContainerId);
             } else {
                 container = containerModel.read(containerId);
                 if (null == container) {
                     logger.logWarning("Container {0} no longer exists in the backup.", containerId);
                 } else {
                     backedUpContainers.add(containerModel.read(containerId));
                 }
             }
         }
         return backedUpContainers;
     }
 
     /**
      * Read a container version implementation. Read the container from the
      * backup.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>ContainerVersion</code>.
      */
     private ContainerVersion readContainerVersionImpl(final UUID uniqueId,
             final Long versionId) {
         if (isContainerBackedUpImpl(uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory();
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getContainerModel().readVersion(containerId, versionId);
         } else {
             logger.logWarning("Container {0} is not backed up.", uniqueId);
             return null;
         }
     }
 
     /**
      * Read the container versions implementation. If the user is a member of
      * the team read the versions from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @return A <code>List</code> of <code>ContainerVersion</code>s.
      */
     private List<ContainerVersion> readContainerVersionsImpl(final User user,
             final UUID uniqueId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory(user);
             final Long containerId = modelFactory.getArtifactModel().readId(uniqueId);
             final InternalContainerModel containerModel = modelFactory.getContainerModel();
             final List<ContainerVersion> versions = containerModel.readVersions(containerId);
             final List<ArtifactReceipt> receipts = new ArrayList<ArtifactReceipt>();
             final Iterator<ContainerVersion> iVersions = versions.iterator();
             ContainerVersion version;
             boolean publishedBy, publishedTo;
             while (iVersions.hasNext()) {
                 version = iVersions.next();
                 receipts.clear();
                 receipts.addAll(containerModel.readPublishedTo(version.getArtifactId(), version.getVersionId()));
                 publishedBy = publishedTo = false;
                 if (version.getCreatedBy().equals(user.getId())) {
                     publishedBy = true;
                 } else {
                     for (final ArtifactReceipt receipt : receipts) {
                         if (receipt.getUser().getId().equals(user.getId())) {
                             publishedTo = true;
                             break;
                         }
                     }
                 }
                 if (!publishedTo && !publishedBy) {
                     iVersions.remove();
                 }                
             }
             return versions;
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
     }
 
     /**
      * Read the team ids implementation. If the user is a member of the team,
      * read the team ids from the backup.
      * 
      * @param user
      *            A <code>User</code>.
      * @param uniqueId
      *            An artifact unique id <code>UUID</code>.
      * @return A <code>List</code> of <code>JabberId</code>s.
      */
     private List<JabberId> readTeamIdsImpl(final User user, final UUID uniqueId) {
         if (isContainerBackedUpImpl(user, uniqueId)) {
             final InternalModelFactory modelFactory = getModelFactory(user);
             final Long artifactId = modelFactory.getArtifactModel().readId(uniqueId);
             return modelFactory.getArtifactModel().readTeamIds(artifactId);
         } else {
             logger.logWarning("Container {0} is not backed up for user {1}.",
                     uniqueId, user.getId());
             return Collections.emptyList();
         }
     }
 }
