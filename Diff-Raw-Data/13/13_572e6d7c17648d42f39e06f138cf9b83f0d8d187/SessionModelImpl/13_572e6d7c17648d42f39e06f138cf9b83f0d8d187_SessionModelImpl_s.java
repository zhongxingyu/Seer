 /*
  * Created On: Mar 7, 2005
  */
 package com.thinkparity.ophelia.model.session;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.UUID;
 
 import com.thinkparity.codebase.OS;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.DraftExistsException;
 import com.thinkparity.codebase.model.artifact.PublishedToEMail;
 import com.thinkparity.codebase.model.backup.Statistics;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.contact.IncomingEMailInvitation;
 import com.thinkparity.codebase.model.contact.IncomingUserInvitation;
 import com.thinkparity.codebase.model.contact.OutgoingEMailInvitation;
 import com.thinkparity.codebase.model.contact.OutgoingUserInvitation;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.migrator.Error;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.migrator.Resource;
 import com.thinkparity.codebase.model.profile.EMailReservation;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.profile.ProfileEMail;
 import com.thinkparity.codebase.model.profile.SecurityCredentials;
 import com.thinkparity.codebase.model.profile.UsernameReservation;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 import com.thinkparity.codebase.model.session.InvalidLocationException;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.Token;
 
 import com.thinkparity.ophelia.model.Constants;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.artifact.InternalArtifactModel;
 import com.thinkparity.ophelia.model.events.SessionListener;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 import com.thinkparity.service.*;
 import com.thinkparity.service.client.ServiceFactory;
 
 /**
  * <b>Title:</b>thinkParity OpheliaModel Session Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.40
  */
 public final class SessionModelImpl extends Model<SessionListener>
         implements SessionModel, InternalSessionModel {
 
     /**
      * The authentication token's expiry fudge factor. Used when determining
      * whether or not to re-authenticate.
      */
     private static final int AUTH_TOKEN_EXPIRY_FUDGE;
 
     /** Configuration key for the remote release. */
     private static final String CFG_KEY_REMOTE_RELEASE;
 
     /** A workspace attribute key for the authentication token. */
     private static final String WS_ATTRIBUTE_KEY_AUTH_TOKEN;
 
     /** A workspace attribute key defining an <code>OfflineCode</code>. */
     private static final String WS_ATTRIBUTE_KEY_OFFLINE_CODES;
 
     static {
         AUTH_TOKEN_EXPIRY_FUDGE = 3 * 1000;
         CFG_KEY_REMOTE_RELEASE = "SessionModelImpl#remoteRelease";
         WS_ATTRIBUTE_KEY_OFFLINE_CODES = "SessionModelImpl#offlineCodes";
         WS_ATTRIBUTE_KEY_AUTH_TOKEN = "SessionModelImpl#authToken";
     }
 
     /** An artifact web-service interface. */
     private ArtifactService artifactService;
 
     /** A backup web-service interface. */
     private BackupService backupService;
 
     /** A contact web-service interface. */
     private ContactService contactService;
 
     /** A container web-service interface. */
 
     private ContainerService containerService;
 
     /** A migrator web-service interface. */
     private MigratorService migratorService;
 
     /** A profile web-service interface. */
     private ProfileService profileService;
     /** A rule web-service interface. */
     private RuleService ruleService;
 
     /** A service factory. */
     private ServiceFactory serviceFactory;
 
     /** A session web-service interface. */
     private SessionService sessionService;
 
     /** A system web-service interface. */
     private SystemService systemService;
 
     /** A user web-service interface. */
     private UserService userService;
 
     /**
 	 * Create a SessionModelImpl
 	 * 
 	 * @param workspace
 	 *            The parity workspace.
 	 */
 	public SessionModelImpl() {
 		super();
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#acceptIncomingEMailInvitation(com.thinkparity.codebase.model.contact.IncomingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
     public void acceptInvitation(final IncomingEMailInvitation invitation,
             final Calendar acceptedOn) {
         try {
             contactService.acceptInvitation(getAuthToken(), invitation,
                     acceptedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#acceptIncomingUserInvitation(com.thinkparity.codebase.model.contact.IncomingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void acceptInvitation(final IncomingUserInvitation invitation,
             final Calendar acceptedOn) {
         try {
             contactService.acceptInvitation(getAuthToken(), invitation,
                     acceptedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#addListener(com.thinkparity.ophelia.model.util.EventListener)
      * 
      */
     @Override
     public void addListener(final SessionListener listener) {
         super.addListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#addProfileEmail(com.thinkparity.codebase.model.profile.ProfileEMail)
      * 
      */
     public void addProfileEmail(final ProfileEMail email) {
         try {
             profileService.addEMail(getAuthToken(), email.getEmail());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createDraft(java.util.List,
      *      java.util.UUID, java.util.Calendar)
      * 
      */
     public void createDraft(final List<JabberId> team, final UUID uniqueId,
             final Calendar createdOn) throws DraftExistsException {
         try {
             artifactService.createDraft(getAuthToken(), team, uniqueId,
                     createdOn);
         } catch (final DraftExistsException dex) {
             throw dex;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createInvitation(com.thinkparity.codebase.model.contact.OutgoingEMailInvitation)
      * 
      */
     public void createInvitation(final OutgoingEMailInvitation invitation) {
         try {
             contactService.createInvitation(getAuthToken(), invitation);
         } catch(final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createInvitation(com.thinkparity.codebase.model.contact.OutgoingUserInvitation)
      * 
      */
     public void createInvitation(final OutgoingUserInvitation invitation) {
         try {
             contactService.createInvitation(getAuthToken(), invitation);
         } catch(final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createProfile(com.thinkparity.codebase.model.profile.UsernameReservation,
      *      com.thinkparity.codebase.model.profile.EMailReservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail, java.lang.String,
      *      java.lang.String)
      * 
      */
     public void createProfile(final Product product, final Release release,
             final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials) {
         try {
             // HACK - SessionModelImpl#createProfile - no authentication required
             profileService.create(newEmptyAuthToken(), product, release,
                     usernameReservation, emailReservation, credentials,
                     profile, email, securityCredentials);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createProfileEMailReservation(com.thinkparity.codebase.email.EMail)
      *
      */
     public EMailReservation createProfileEMailReservation(
             final EMail email) {
         try {
             return profileService.createEMailReservation(newEmptyAuthToken(),
                     email);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createProfileUsernameReservation(java.lang.String)
      * 
      */
     public UsernameReservation createProfileUsernameReservation(
             final String username) {
         try {
             return profileService.createUsernameReservation(
                     newEmptyAuthToken(), username);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#declineInvitation(com.thinkparity.codebase.model.contact.IncomingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
     public void declineInvitation(final IncomingEMailInvitation invitation,
             final Calendar declinedOn) {
         try {
             contactService.declineInvitation(getAuthToken(), invitation,
                     declinedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#declineInvitation(com.thinkparity.codebase.model.contact.IncomingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void declineInvitation(final IncomingUserInvitation invitation,
             final Calendar declinedOn) {
         try {
             contactService.declineInvitation(getAuthToken(), invitation,
                     declinedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#deleteContact(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public void delete(final JabberId id) {
         try {
             contactService.delete(getAuthToken(), id);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#deleteDraft(java.util.UUID,
      *      java.util.Calendar)
      * 
      */
     public void deleteDraft(final UUID uniqueId, final Calendar deletedOn) {
         try {
             final InternalArtifactModel artifactModel = getArtifactModel();
             final Long artifactId = artifactModel.readId(uniqueId);
             final List<JabberId> team = artifactModel.readTeamIds(artifactId);
             artifactService.deleteDraft(getAuthToken(), team, uniqueId,
                     deletedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#deleteInvitation(com.thinkparity.codebase.model.contact.OutgoingEMailInvitation,
      *      java.util.Calendar)
      * 
      */
     public void deleteInvitation(final OutgoingEMailInvitation invitation,
             final Calendar deletedOn) {
         try {
             contactService.deleteInvitation(getAuthToken(), invitation,
                     deletedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#deleteInvitation(com.thinkparity.codebase.model.contact.OutgoingUserInvitation,
      *      java.util.Calendar)
      * 
      */
     public void deleteInvitation(final OutgoingUserInvitation invitation,
             final Calendar deletedOn) {
         try {
             contactService.deleteInvitation(getAuthToken(), invitation,
                     deletedOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#getAuthToken()
      * 
      */
     public AuthToken getAuthToken() {
         if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN)) {
             /* determine whether or not the authentication token has expired; if
              * so, re-login and set a new authentication token */
             final AuthToken authToken =
                 (AuthToken) workspace.getAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN);
             final long localDateTime =
                 currentDateTime().getTime().getTime() - AUTH_TOKEN_EXPIRY_FUDGE;
             if (localDateTime > authToken.getExpiresOn().getTime()) {
                 logger.logInfo("User session has expired.");
                 logger.logVariable("localDateTime", localDateTime);
                 logger.logVariable("authToken", authToken);
                 try {
                     setAuthToken(sessionService.login(readCredentials()));
                     return getAuthToken();
                 } catch (final InvalidCredentialsException icx) {
                     logger.logFatal(icx, "User credentials have changed.");
                     return null;
                 }
             } else {
                 return authToken;
             }
         } else {
             logger.logError("User session is null.");
             return null;
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.SessionModel#getOfflineCode()
      *
      */
     public OfflineCode getOfflineCode() {
         final OfflineCodes offlineCodes = getOfflineCodes();
         if (null == offlineCodes) {
             return null;
         } else {
             if (offlineCodes.isEmpty()) {
                 return null;
             } else {
                 return offlineCodes.peek();
             }
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#initializeToken()
      *
      */
     public void initializeToken() {
         try {
             createToken(profileService.createToken(getAuthToken()));
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isEmailAvailable(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.email.EMail)
      * 
      */
     public Boolean isEmailAvailable(final JabberId userId, final EMail email) {
         try {
             return profileService.isEMailAvailable(getAuthToken(), email);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isFirstLogin(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public Boolean isFirstLogin() {
         try {
             return null == profileService.readToken(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isInviteRestricted(com.thinkparity.codebase.model.user.User)
      *
      */
     public Boolean isInviteRestricted(User user) {
         try {
             return ruleService.isInviteRestricted(getAuthToken(), user);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.SessionModel#isLoggedIn()
      *
      */
     public Boolean isLoggedIn() {
         try {
             final Boolean isOnline = isOnline();
             logger.logVariable("isOnline", isOnline);
             final Boolean isSetAuthToken = isSetAuthToken();
             logger.logVariable("isSetAuthToken", isSetAuthToken());
             return isOnline.booleanValue() && isSetAuthToken.booleanValue();
         } catch (final Throwable t) {
             throw panic(t);
         }
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.session.SessionModel#isOnline()
      *
      */
     public Boolean isOnline() {
         try {
             if (isSetOfflineCode()) {
                 return Boolean.FALSE;
             } else {
                 return Boolean.TRUE;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isPublishRestricted(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public Boolean isPublishRestricted(final JabberId publishTo) {
         try {
             return ruleService.isPublishRestricted(getAuthToken(), publishTo);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isPublishRestricted(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public Boolean isPublishRestricted(final List<EMail> emails,
             final List<User> users) {
         try {
             return ruleService.isPublishRestricted(getAuthToken(), emails, users);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#logError(com.thinkparity.codebase.model.migrator.Product,
      *      com.thinkparity.codebase.model.migrator.Release,
      *      com.thinkparity.codebase.model.migrator.Error)
      * 
      */
     public void logError(final Product product, final Release release,
             final Error error) {
         try {
             migratorService.logError(getAuthToken(), product, release, error);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#login(com.thinkparity.codebase.model.session.Credentials)
      *
      */
     public void login(final Credentials credentials)
             throws InvalidCredentialsException {
         try {
             if (!environment.isServiceReachable()) {
                 if (!isServerMaintenance()) {
                     notifyServerMaintenance();
                 }
                 return;
             }
 
             // login and save auth token
             setAuthToken(sessionService.login(credentials));
             // save credentials
             createCredentials(credentials);
             // save release
             setRelease();
             // fire event
             removeOfflineCodes();
             notifySessionEstablished();
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.SessionModel#login(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void login(final ProcessMonitor monitor)
             throws InvalidCredentialsException, InvalidLocationException {
         try {
             if (isClientMaintenance())
                 return;
 
             if (!environment.isServiceReachable()) {
                 if (!isServerMaintenance()) {
                     notifyServerMaintenance();
                 }
                 return;
             }
 
             notifyProcessBegin(monitor);
             final Credentials credentials = readCredentials();
             // check that the credentials match
             final Credentials localCredentials = readCredentials();
             Assert.assertTrue(
                     localCredentials.getUsername().equals(credentials.getUsername()) &&
                     localCredentials.getPassword().equals(credentials.getPassword()),
                     "Credentials {0} do not match local credentials {1}.",
                     credentials, localCredentials);
             // login and set auth token
             try {
                 setAuthToken(sessionService.login(credentials));
             } catch (final InvalidCredentialsException icx) {
                 throw icx;
             }
             // ensure environment integrity
             final Token localToken = readToken();
             final Token remoteToken = profileService.readToken(getAuthToken());
             if (localToken.equals(remoteToken)) {
                 /* if the latest release is newer than this software, start a
                  * download */
                 final Release latestRelease = readMigratorLatestRelease(
                         Constants.Product.NAME, OSUtil.getOS());
                 if (latestRelease.getName().equals(Constants.Release.NAME)) {
                     // save release
                     setRelease();
                     // process queued events
                     getQueueModel().process(monitor);
                     // start notification client
                     getQueueModel().startNotificationClient();
                     // fire event
                     removeOfflineCodes();
                     notifySessionEstablished();
                 } else {
                     getMigratorModel().startDownloadRelease();
                 }
             } else {
                 sessionService.logout(getAuthToken());
                 removeAuthToken();
                 throw new InvalidLocationException();
             }
         } catch (final InvalidCredentialsException icx) {
             pushOfflineCode(OfflineCode.OFFLINE);
             getSessionModel().notifySessionTerminated();
 
             throw icx;
         } catch (final InvalidLocationException ilx) {
             pushOfflineCode(OfflineCode.OFFLINE);
             getSessionModel().notifySessionTerminated();
 
             throw ilx;
         } catch (final Throwable t) {
             throw panic(t);
         } finally {
             notifyProcessEnd(monitor);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.SessionModel#logout()
      *
      */
     public void logout() {
 		try {
            // logout
             sessionService.logout(getAuthToken());
             // fire event
             notifySessionTerminated();
 		} catch (final Throwable t) {
 			throw panic(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#notifyClientMaintenance()
      *
      */
     public void notifyClientMaintenance() {
         try {
             // set an offline state
             pushOfflineCode(OfflineCode.CLIENT_MAINTENANCE);
             // fire event
             notifySessionTerminated();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#notifySessionTerminated()
      *
      */
     public void notifySessionTerminated() {
         notifyListeners(new EventNotifier<SessionListener>() {
             public void notifyListener(final SessionListener listener) {
                 listener.sessionTerminated();
             }
         });
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#publish(com.thinkparity.codebase.model.container.ContainerVersion,
      *      java.util.List, java.util.List, java.util.List)
      * 
      */
     public void publish(final ContainerVersion version,
             final List<DocumentVersion> documentVersions,
             final List<EMail> publishToEMails, final List<User> publishToUsers) {
         try {
             containerService.publish(getAuthToken(), version, documentVersions,
                     publishToEMails, publishToUsers);
         } catch(final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#publishVersion(com.thinkparity.codebase.model.container.ContainerVersion,
      *      java.util.List, java.util.List, java.util.List,
      *      com.thinkparity.codebase.jabber.JabberId, java.util.Calendar,
      *      java.util.List, java.util.List)
      * 
      */
     public void publishVersion(final ContainerVersion version,
             final List<DocumentVersion> documentVersions,
             final List<ArtifactReceipt> receivedBy, final Calendar publishedOn,
             final List<EMail> publishToEMails, final List<User> publishToUsers) {
         try {
             containerService.publishVersion(getAuthToken(), version,
                     documentVersions, receivedBy, publishedOn, publishToEMails,
                     publishToUsers);
         } catch(final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#pushOfflineCode(com.thinkparity.ophelia.model.session.OfflineCode)
      *
      */
     public void pushOfflineCode(final OfflineCode offlineCode) {
         try {
             if (!isSetOfflineCodes()) {
                 setOfflineCodes(new OfflineCodes());
             }
             final OfflineCodes offlineCodes = getOfflineCodes();
             offlineCodes.push(offlineCode);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupContainer(java.util.UUID)
      * 
      */
     public Container readBackupContainer(final UUID uniqueId) {
         try {
             return backupService.readContainer(getAuthToken(), uniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupContainers()
      * 
      */
     public List<Container> readBackupContainers() {
         try {
             return backupService.readContainers(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupContainerVersions(java.util.UUID)
      *
      */
     public List<ContainerVersion> readBackupContainerVersions(
             final UUID uniqueId) {
         try {
             return backupService.readContainerVersions(getAuthToken(),
                     uniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
     
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupDocuments(java.util.UUID, java.lang.Long)
      *
      */
     public List<Document> readBackupDocuments(final UUID containerUniqueId,
             final Long containerVersionId) {
         try {
             return backupService.readDocuments(getAuthToken(),
                     containerUniqueId, containerVersionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupDocumentVersions(java.util.UUID, java.lang.Long)
      *
      */
     public List<DocumentVersion> readBackupDocumentVersions(
             final UUID containerUniqueId, final Long containerVersionId) {
         try {
             return backupService.readDocumentVersions(getAuthToken(),
                     containerUniqueId, containerVersionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupPublishedTo(java.util.UUID,
      *      java.lang.Long)
      * 
      */
     public List<ArtifactReceipt> readBackupPublishedTo(
             final UUID containerUniqueId, final Long containerVersionId) {
         try {
             return backupService.readPublishedTo(getAuthToken(),
                     containerUniqueId, containerVersionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupPublishedToEMails(java.util.UUID,
      *      java.lang.Long)
      * 
      */
     public List<PublishedToEMail> readBackupPublishedToEMails(
             final UUID containerUniqueId, final Long containerVersionId) {
         try {
             return backupService.readPublishedToEMails(getAuthToken(),
                     containerUniqueId, containerVersionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readBackupTeamIds(java.util.UUID)
      *
      */
     public List<JabberId> readBackupTeamIds(final UUID containerUniqueId) {
         try {
             return backupService.readTeamIds(getAuthToken(), containerUniqueId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readContact(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public Contact readContact(final JabberId id) {
         try {
             return contactService.read(getAuthToken(), id);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readContactIds()
      *
      */
     public List<Contact> readContacts() {
         try {
             return contactService.read(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readDateTime()
      * 
      */
     public Calendar readDateTime() {
         try {
             return systemService.readDateTime(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readIncomingEMailInvitations()
      * 
      */
     public List<IncomingEMailInvitation> readIncomingEMailInvitations() {
         try {
             return contactService.readIncomingEMailInvitations(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readIncomingUserInvitations()
      * 
      */
     public List<IncomingUserInvitation> readIncomingUserInvitations() {
         try {
             return contactService.readIncomingUserInvitations(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readKeyHolder(java.util.UUID)
      * 
      */
     public JabberId readKeyHolder(final UUID uniqueId) {
 		try {
             return artifactService.readKeyHolder(getAuthToken(), uniqueId);
 		} catch (final Throwable t) {
 			throw panic(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readMigratorLatestRelease(java.lang.String,
      *      com.thinkparity.codebase.OS)
      * 
      */
     public Release readMigratorLatestRelease(final String productName,
             final OS os) {
         try {
             return migratorService.readLatestRelease(getAuthToken(),
                     productName, os);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readMigratorProduct(java.lang.String)
      *
      */
     public Product readMigratorProduct(final String name) {
         try {
             return migratorService.readProduct(getAuthToken(), name);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readMigratorProductFeatures(java.lang.String)
      *
      */
     public List<Feature> readMigratorProductFeatures(final String name) {
         try {
             return migratorService.readProductFeatures(getAuthToken(), name);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readMigratorRelease(java.lang.String,
      *      java.lang.String, com.thinkparity.codebase.OS)
      * 
      */
     public Release readMigratorRelease(final String productName,
             final String name, final OS os) {
         try {
             return migratorService.readRelease(getAuthToken(), name,
                     productName, os);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readMigratorResources(java.lang.String,
      *      java.lang.String, com.thinkparity.codebase.OS)
      * 
      */
     public List<Resource> readMigratorResources(final String productName,
             final String releaseName, final OS os) {
         try {
             return migratorService.readResources(getAuthToken(), productName,
                     releaseName, os);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readOutgoingEMailInvitations()
      *
      */
     public List<OutgoingEMailInvitation> readOutgoingEMailInvitations() {
         try {
             return contactService.readOutgoingEMailInvitations(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readOutgoingUserInvitations()
      *
      */
     public List<OutgoingUserInvitation> readOutgoingUserInvitations() {
         try {
             return contactService.readOutgoingUserInvitations(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readProfile()
      * 
      */
     public Profile readProfile() {
         try {
             return profileService.read(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readProfileEMails()
      * 
      */
     public List<ProfileEMail> readProfileEMails() {
         try {
             return profileService.readEMails(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readProfileFeatures(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public List<Feature> readProfileFeatures() {
         try {
             return profileService.readFeatures(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readStatistics()
      *
      */
     public Statistics readStatistics() {
         try {
             return backupService.readStatistics(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#readUser(com.thinkparity.codebase.jabber.JabberId)
      *
      */
     public User readUser(final JabberId userId) {
         try {
             return userService.read(getAuthToken(), userId);
         } catch (final Throwable t) {
             throw panic(t);
         }
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.Model#removeListener(com.thinkparity.ophelia.model.util.EventListener)
      * 
      */
     @Override
     public void removeListener(final SessionListener listener) {
         super.removeListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#removeProfileEmail(com.thinkparity.codebase.jabber.JabberId, com.thinkparity.codebase.model.profile.ProfileEMail)
      *
      */
     public void removeProfileEmail(final JabberId userId, final ProfileEMail email) {
         try {
             profileService.removeEMail(getAuthToken(), email.getEmail());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#updateProfile(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.profile.Profile)
      * 
      */
     public void updateProfile(final JabberId userId, final Profile profile) {
         try {
             profileService.update(getAuthToken(), profile.getVCard());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#updateProfilePassword(com.thinkparity.codebase.model.session.Credentials,
      *      java.lang.String)
      * 
      */
     public void updateProfilePassword(final Credentials credentials,
             final String password) {
         try {
             profileService.updatePassword(getAuthToken(), credentials,
                     password);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#verifyProfileEmail(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.profile.ProfileEMail,
      *      java.lang.String)
      * 
      */
     public void verifyProfileEmail(final ProfileEMail email, final String token) {
         try {
             profileService.verifyEMail(getAuthToken(), email.getEmail(), token);
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
         serviceFactory = ServiceFactory.getInstance();
         artifactService = serviceFactory.getArtifactService();
         backupService = serviceFactory.getBackupService();
         contactService = serviceFactory.getContactService();
         containerService = serviceFactory.getContainerService();
         migratorService = serviceFactory.getMigratorService();
         ruleService = serviceFactory.getRuleService();
         sessionService = serviceFactory.getSessionService();
         systemService = serviceFactory.getSystemService();
         profileService = serviceFactory.getProfileService();
         userService = serviceFactory.getUserService();
     }
 
     /**
      * Obtain the offline codes workspace attribute.
      * 
      * @return An <code>OfflineCodes</code>.
      */
     private OfflineCodes getOfflineCodes() {
         return (OfflineCodes) workspace.getAttribute(
                 WS_ATTRIBUTE_KEY_OFFLINE_CODES);
     }
 
     /**
      * Determine whether or not we are currently performing client maintenance.
      * 
      * @return True if we are performing client maintenance.
      */
     private boolean isClientMaintenance() {
         final OfflineCodes offlineCodes = getOfflineCodes();
         if (null == offlineCodes) {
             return false;
         } else {
             return offlineCodes.contains(OfflineCode.CLIENT_MAINTENANCE);
         }
     }
 
     /**
      * Determine whether or not we are currently performing server maintenance.
      * 
      * @return True if we are performing server maintenance.
      */
     private boolean isServerMaintenance() {
         final OfflineCodes offlineCodes = getOfflineCodes();
         if (null == offlineCodes) {
             return false;
         } else {
             return offlineCodes.contains(OfflineCode.SERVER_MAINTENANCE);
         }
     }
 
     /**
      * Determine whether or not the authentication token has been set.
      * 
      * @return True if the authentication token has been set.
      */
     private boolean isSetAuthToken() {
         return workspace.isSetAttribute(
                 WS_ATTRIBUTE_KEY_AUTH_TOKEN).booleanValue();
     }
 
     /**
      * Determine whether or not an offline code has been set.
      * 
      * @return True if an offline code has been set.
      */
     private boolean isSetOfflineCode() {
         final OfflineCodes offlineCodes = getOfflineCodes();
         if (null == offlineCodes) {
             return false;
         } else {
             return !offlineCodes.isEmpty();
         }
     }
 
     /**
      * Determine whether or not the offline codes have been set.
      * 
      * @return True if the offline codes have been set.
      */
     private boolean isSetOfflineCodes() {
         return workspace.isSetAttribute(
                 WS_ATTRIBUTE_KEY_OFFLINE_CODES).booleanValue();
     }
 
     /**
      * Create an empty authentication token.
      * 
      * @return An <code>AuthToken</code>.
      */
     private AuthToken newEmptyAuthToken() {
         return new AuthToken();
     }
 
     /**
      * Set a server maintenance offline code and fire a session terminated
      * event.
      * 
      */
     private void notifyServerMaintenance() {
         // set an offline state
         pushOfflineCode(OfflineCode.SERVER_MAINTENANCE);
         // fire event
         notifySessionTerminated();
     }
 
     /**
      * Fire a session established event.
      * 
      */
     private void notifySessionEstablished() {
         notifyListeners(new EventNotifier<SessionListener>() {
             public void notifyListener(final SessionListener listener) {
                 listener.sessionEstablished();
             }
         });
     }
 
     /**
      * Remove the authentication token workspace attribute.
      * 
      * @return The <code>AuthToken</code>.
      */
     private AuthToken removeAuthToken() {
         final AuthToken authToken = getAuthToken();
         if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN)) {
             workspace.removeAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN);
         }
         return authToken;
     }
 
     /**
      * Remove the offline code workspace attribute.
      * 
      * @return The <code>OfflineCodes</code>.
      */
     private OfflineCodes removeOfflineCodes() {
         final OfflineCodes offlineCodes = getOfflineCodes();
         if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_OFFLINE_CODES)) {
             workspace.removeAttribute(WS_ATTRIBUTE_KEY_OFFLINE_CODES);
         }
         return offlineCodes;
     }
 
     private void setAuthToken(final AuthToken authToken) {
         workspace.setAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN, authToken);
     }
 
     /**
      * Set the offline codes workspace attribute.
      * 
      * @param offlineCodes
      *            The <code>OfflineCodes</code>.
      * @return The previous <code>OfflineCodes</code>.
      */
     private OfflineCodes setOfflineCodes(final OfflineCodes offlineCodes) {
         return (OfflineCodes) workspace.setAttribute(
                 WS_ATTRIBUTE_KEY_OFFLINE_CODES, offlineCodes);
     }
 
     /**
      * Set the remote release for the user.
      *
      */
     private void setRelease() {
         final String release = configurationIO.read(CFG_KEY_REMOTE_RELEASE);
         if (Constants.Release.NAME.equals(release)) {
             logger.logInfo("Release info already set.");
         } else {
             // update remotely
             if (null == release) {
                 configurationIO.create(CFG_KEY_REMOTE_RELEASE, Constants.Release.NAME);
             } else {
                 configurationIO.update(CFG_KEY_REMOTE_RELEASE, Constants.Release.NAME);
             }
             getProfileModel().updateProductRelease();
         }
     }
 }
