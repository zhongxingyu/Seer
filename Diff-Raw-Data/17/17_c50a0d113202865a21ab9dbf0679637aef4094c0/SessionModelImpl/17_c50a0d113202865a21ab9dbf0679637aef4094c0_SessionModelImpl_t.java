 /*
  * Created On: Mar 7, 2005
  */
 package com.thinkparity.ophelia.model.session;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Properties;
 import java.util.UUID;
 
 import com.thinkparity.codebase.OS;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.DraftExistsException;
 import com.thinkparity.codebase.model.artifact.IllegalVersionException;
 import com.thinkparity.codebase.model.artifact.PublishedToEMail;
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
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.profile.ProfileEMail;
 import com.thinkparity.codebase.model.session.Configuration;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 import com.thinkparity.codebase.model.session.InvalidLocationException;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.Token;
 
 import com.thinkparity.ophelia.model.Constants;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.artifact.InternalArtifactModel;
 import com.thinkparity.ophelia.model.events.SessionAdapter;
 import com.thinkparity.ophelia.model.events.SessionListener;
 import com.thinkparity.ophelia.model.session.daemon.SessionReaper;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.util.configuration.ReconfigureEvent;
 import com.thinkparity.ophelia.model.util.configuration.ReconfigureListener;
 import com.thinkparity.ophelia.model.util.service.ServiceRetryHandler;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 import com.thinkparity.service.*;
 
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
 
     /** The configuration name for reaper enabled. */
     private static final String CFG_NAME_REAPER_ENABLED;
 
     /** The default reaper enabled. */
     private static final Boolean DEFAULT_REAPER_ENABLED;
 
     /** A workspace attribute key for the authentication token. */
     private static final String WS_ATTRIBUTE_KEY_AUTH_TOKEN;
 
     /** A workspace attribute key defining the configuration. */
     private static final String WS_ATTRIBUTE_KEY_CONFIGURATION;
 
     /** A workspace attribute key defining an <code>OfflineCode</code>. */
     private static final String WS_ATTRIBUTE_KEY_OFFLINE_CODES;
 
     /** A workspace attribute key for the reaper. */
     private static final String WS_ATTRIBUTE_KEY_REAPER;
 
     /** A workspace attribute key for the reconfigure listeners. */
     private static final String WS_ATTRIBUTE_KEY_RECONFIGURE_LISTENERS;
 
     static {
         AUTH_TOKEN_EXPIRY_FUDGE = 3 * 1000;
         CFG_NAME_REAPER_ENABLED = "com.thinkparity.session.reaper.enabled";
         CFG_KEY_REMOTE_RELEASE = "SessionModelImpl#remoteRelease";
         DEFAULT_REAPER_ENABLED = Boolean.FALSE;
         WS_ATTRIBUTE_KEY_AUTH_TOKEN = "SessionModelImpl#authToken";
         WS_ATTRIBUTE_KEY_CONFIGURATION = "SessionModelImpl#configuration";
         WS_ATTRIBUTE_KEY_OFFLINE_CODES = "SessionModelImpl#offlineCodes";
         WS_ATTRIBUTE_KEY_REAPER = "SessionModelImpl#reaper";
         WS_ATTRIBUTE_KEY_RECONFIGURE_LISTENERS = "SessionModelImpl#reconfigureListeners";
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
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#createDraft(java.util.List,
      *      java.util.UUID, java.util.Calendar)
      * 
      */
     public void createDraft(final List<JabberId> team, final UUID uniqueId,
             final Long latestVersionId, final Calendar createdOn)
             throws DraftExistsException, IllegalVersionException {
         try {
             artifactService.createDraft(getAuthToken(), team, uniqueId,
                     latestVersionId, createdOn);
         } catch (final DraftExistsException dex) {
             throw dex;
         } catch (final IllegalVersionException ivx) {
             throw ivx;
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
            logger.logInfo("Authentication token is not null.");
             /* determine whether or not the authentication token has expired; if
              * so, re-login and set a new authentication token */
             final AuthToken authToken =
                 (AuthToken) workspace.getAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN);
             if (isExpired(authToken)) {
                 logger.logInfo("User session has expired.");
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
            logger.logInfo("Authentication token is null.");
            throw new OfflineException();
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#getConfiguration()
      *
      */
     @Override
     public Properties getConfiguration() {
         return (Configuration) workspace.getAttribute(WS_ATTRIBUTE_KEY_CONFIGURATION);
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
      * @see com.thinkparity.ophelia.model.session.InternalSessionModel#isInviteRestricted(java.util.List)
      *
      */
     @Override
     public Boolean isInviteRestricted(final List<EMail> emailList) {
         return ruleService.isInviteRestricted(getAuthToken(), emailList);
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
             // login and save auth token
             setAuthToken(serviceLogin(credentials));
             // save credentials
             createCredentials(credentials);
             // save release
             setRelease();
             // update configuration
             configure();
             // fire event
             clearOfflineCodes();
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
             if (isClientMaintenance()) {
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
                 setAuthToken(serviceLogin(credentials));
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
                     // update configuration
                     configure();
                     // start notification client
                     getQueueModel().startNotificationClient();
                     // fire event
                     clearOfflineCodes();
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
             if (isOnline()) {
                 pushOfflineCode(OfflineCode.CLIENT_NETWORK_UNAVAILABLE);
                 getSessionModel().notifySessionTerminated();
             }
 
             throw icx;
         } catch (final InvalidLocationException ilx) {
             if (isOnline()) {
                 pushOfflineCode(OfflineCode.CLIENT_NETWORK_UNAVAILABLE);
                 getSessionModel().notifySessionTerminated();
             }
 
             throw ilx;
         } catch (final Throwable t) {
             if (isOnline()) {
                 pushOfflineCode(OfflineCode.CLIENT_NETWORK_UNAVAILABLE);
                 getSessionModel().notifySessionTerminated();
             }
 
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
 		    try {
     		    /* if the session has expired; don't bother logging out */
     	        final AuthToken authToken =
     	            (AuthToken) workspace.getAttribute(WS_ATTRIBUTE_KEY_AUTH_TOKEN);
     	        if (isExpired(authToken)) {
     	            logger.logInfo("User session is expired.");
     	        } else {
     	            sessionService.logout(authToken);
     	        }
 		    } finally {
 		        /* guarantee that the session is terminated by removing the
 		         * token and firing a notification */
 		        removeAuthToken();
 		        final OfflineCodes offlineCodes = getOfflineCodes();
 		        offlineCodes.push(OfflineCode.CLIENT_OFFLINE);
 		        setOfflineCodes(offlineCodes);
 		        notifySessionTerminated();
 		    }
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
             final String password) throws InvalidCredentialsException {
         try {
             profileService.updatePassword(getAuthToken(), credentials,
                     password);
         } catch (final InvalidCredentialsException icx) {
             throw icx;
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
         serviceFactory = workspace.getServiceFactory(newDefaultRetryHandler());
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
 
         /* if the codes are null; they have not been set */
         OfflineCodes offlineCodes = getOfflineCodes();
         if (null == offlineCodes) {
             offlineCodes = new OfflineCodes();
             offlineCodes.push(OfflineCode.CLIENT_OFFLINE);
             setOfflineCodes(offlineCodes);
         }
         /* on first login; start the session reaper */
         addListener(new SessionAdapter() {
             /**
              * @see com.thinkparity.ophelia.model.events.SessionAdapter#sessionEstablished()
              *
              */
             @Override
             public void sessionEstablished() {
                 if (isReaperEnabled()) {
                     logger.logInfo("Session reaper has been enabled.");
                     if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_REAPER)) {
                         logger.logInfo("Session reaper has been started.");
                     } else {
                         logger.logInfo("Session reaper has not been started.");
                         startSessionReaper();
                     }
                 } else {
                     logger.logInfo("Session reaper has not been enabled.");
                 }
             }
         });
     }
 
     /**
      * Fire a reconfigured event.
      * 
      * @param configuration
      *            The <code>Configuration</code>.
      */
     void notifyReconfigured(final Configuration configuration) {
         final ReconfigureListeners listeners = getReconfigureListeners();
         if (null == listeners) {
             logger.logInfo("Notifying 0 reconfigure listeners.");
         } else {
             logger.logInfo("Notifying {0} reconfigure listeners.", listeners.size());
             listeners.notifyListeners(new EventNotifier<ReconfigureListener<Properties>>() {
                 /**
                  * @see com.thinkparity.codebase.event.EventNotifier#notifyListener(com.thinkparity.codebase.event.EventListener)
                  *
                  */
                 @Override
                 public void notifyListener(final ReconfigureListener<Properties> listener) {
                     listener.reconfigure(new ReconfigureEvent<Properties>(
                             getConfiguration(), configuration));
                 }
             });
         }
     }
 
     /**
      * Clear the offline codes.
      * 
      */
     private void clearOfflineCodes() {
         getOfflineCodes().clear();
     }
 
     /**
      * Configure. If the configuration has not yet been set; download it and
      * save it in the workspace.
      * 
      */
     private void configure() {
         if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_CONFIGURATION)) {
             logger.logInfo("Session is configured.");
             return;
         } else {
             logger.logInfo("Session is not configured.  Configuring now.");
             workspace.setAttribute(WS_ATTRIBUTE_KEY_CONFIGURATION,
                     sessionService.readConfiguration(getAuthToken()));
         }
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
      * Obtain the reconfigure listeners workspace attribute.
      * 
      * @return A <code>ReconfigureListeners</code>.
      */
     private ReconfigureListeners getReconfigureListeners() {
         return (ReconfigureListeners) workspace.getAttribute(
                 WS_ATTRIBUTE_KEY_RECONFIGURE_LISTENERS);
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
      * Determine whether or not the authentication token has expired.
      * 
      * @param authToken
      *            An <code>AuthToken</code>.
      * @return True if it has expired; false otherwise.
      */
     private boolean isExpired(final AuthToken authToken) {
         logger.logVariable("authToken", authToken);
         final long localDateTime = currentDateTime().getTime().getTime()
             - AUTH_TOKEN_EXPIRY_FUDGE;
         logger.logVariable("localDateTime", localDateTime);
         return localDateTime > authToken.getExpiresOn().getTime();
     }
 
     /**
      * Determine whether or not the reaper is enabled.
      * 
      * @return True if the reaper is enabled.
      */
     private boolean isReaperEnabled() {
         final Properties configuration = getConfiguration();
         if (null == configuration) {
             return DEFAULT_REAPER_ENABLED.booleanValue();
         } else {
             return Boolean.valueOf(getConfiguration().getProperty(
                     CFG_NAME_REAPER_ENABLED,
                     DEFAULT_REAPER_ENABLED.toString())).booleanValue();
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
         return 0 < getOfflineCodes().size();
     }
 
     /**
      * Instantiate a session reaper.
      * 
      * @return A <code>SessionReaper</code>.
      */
     private SessionReaper newSessionReaper() {
         return new SessionReaper(modelFactory);
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
      * Issue a login over the session service.
      * 
      * @param credentials
      *            A set of <code>Credentials</code>.
      * @return An <code>AuthToken</code>.
      * @throws InvalidCredentialsException
      */
     private AuthToken serviceLogin(final Credentials credentials)
             throws InvalidCredentialsException {
         return workspace.getServiceFactory(new ServiceRetryHandler() {
             @Override
             public Boolean retry() {
                 return Boolean.FALSE;
             }
             @Override
             public Long waitPeriod() {
                 return Long.valueOf(0L);
             }
         }).getSessionService().login(credentials);
     }
 
     /**
      * Set the authentication token workspace attribute.
      * 
      * @param authToken
      *            An <code>AuthToken</code>.
      */
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
     private void setOfflineCodes(final OfflineCodes offlineCodes) {
         workspace.setAttribute(WS_ATTRIBUTE_KEY_OFFLINE_CODES, offlineCodes);
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
 
     /**
      * Start a session reaper.
      * 
      */
     private void startSessionReaper() {
         final SessionReaper sessionReaper = newSessionReaper();
         workspace.setAttribute(WS_ATTRIBUTE_KEY_REAPER, sessionReaper);
         workspace.newThread("SessionReaper", sessionReaper).start();
     }
 }
