 /*
  * Generated On: Jul 17 06 11:52:34 AM
  */
 package com.thinkparity.ophelia.model.profile;
 
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import com.thinkparity.codebase.LocaleUtil;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.profile.*;
 import com.thinkparity.codebase.model.profile.payment.PaymentInfo;
 import com.thinkparity.codebase.model.profile.payment.PaymentInfoConstraints;
 import com.thinkparity.codebase.model.profile.payment.PaymentPlanCredentials;
 import com.thinkparity.codebase.model.profile.payment.PaymentPlanCredentialsConstraints;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.ActiveEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentPlanArrearsEvent;
 
 import com.thinkparity.ophelia.model.Constants;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.Constants.Product.Features;
 import com.thinkparity.ophelia.model.events.ProfileEvent;
 import com.thinkparity.ophelia.model.events.ProfileListener;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.db.hsqldb.HypersonicException;
 import com.thinkparity.ophelia.model.io.handler.ProfileIOHandler;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 import com.thinkparity.service.AuthToken;
 import com.thinkparity.service.ProfileService;
 import com.thinkparity.service.ServiceFactory;
 import com.thinkparity.service.SessionService;
 
 /**
  * <b>Title:</b>thinkParity Profile Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.15
  */
 public final class ProfileModelImpl extends Model<ProfileListener> implements
         ProfileModel, InternalProfileModel {
 
     /** A temporary user's backup disk-usage allotment. */
     private static final Long DU_BACKUP_ALLOTMENT;
 
     static {
         DU_BACKUP_ALLOTMENT = Long.valueOf(1024L * 1024L * 1024L * 10L);
     }
 
     /**
      * Determine if the hypersonic (database layer) error was caused by an
      * integrity constraint violation.
      * 
      * @param hypersonicException
      *            A <code>HypersonicException</code>.
      * @return True if the error is caused by an integrity constraint.
      */
     private static boolean isSQLIntegrityConstraintViolation(
             final HypersonicException hypersonicException) {
         if (SQLIntegrityConstraintViolationException.class.isAssignableFrom(
                 hypersonicException.getCause().getClass())) {
             return "23503".equals(((SQLException) hypersonicException.getCause()).getSQLState());
         } else {
             return false;
         }
     }
 
     /** A local profile event generator. */
     private final ProfileEventGenerator localEventGen;
 
     /** The profile db io. */
     private ProfileIOHandler profileIO;
 
     /** The profile web-service. */
     private ProfileService profileService;
 
     /** A remote profile event generator. */
     private final ProfileEventGenerator remoteEventGen;
 
     /** The session web-service. */
     private SessionService sessionService;
 
     /**
      * Create ProfileModelImpl.
      *
      */
     public ProfileModelImpl() {
         super();
         this.localEventGen = new ProfileEventGenerator(ProfileEvent.Source.LOCAL);
         this.remoteEventGen = new ProfileEventGenerator(ProfileEvent.Source.REMOTE);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#addListener(com.thinkparity.ophelia.model.events.ProfileListener)
      *
      */
     @Override
     public void addListener(final ProfileListener listener) {
         super.addListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#create(com.thinkparity.codebase.model.profile.UsernameReservation, com.thinkparity.codebase.model.profile.EMailReservation, com.thinkparity.codebase.model.session.Credentials, com.thinkparity.codebase.model.profile.Profile, com.thinkparity.codebase.email.EMail, com.thinkparity.codebase.model.profile.SecurityCredentials)
      *
      */
     @Override
     public void create(final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials)
             throws ReservationExpiredException {
         try {
             validate(profile);
             validate(securityCredentials);
 
             final Product product = new Product();
             product.setName(Constants.Product.NAME);
 
             final Release release = new Release();
             release.setName(Constants.Release.NAME);
             release.setOs(OSUtil.getOs());
 
             final ServiceFactory serviceFactory = workspace.getServiceFactory(newFiniteRetryHandler());
             final ProfileService profileService = serviceFactory.getProfileService();
             profileService.create(newEmptyAuthToken(), product, release,
                     usernameReservation, emailReservation, credentials, profile,
                     email, securityCredentials);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#create(com.thinkparity.codebase.model.profile.UsernameReservation,
      *      com.thinkparity.codebase.model.profile.EMailReservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail,
      *      com.thinkparity.codebase.model.profile.SecurityCredentials,
      *      com.thinkparity.codebase.model.profile.payment.PaymentInfo)
      * 
      */
     public void create(final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials,
             final PaymentInfo paymentInfo) throws ReservationExpiredException {
         try {
             validate(profile);
             validate(securityCredentials);
             validate(paymentInfo);
 
             final long now = currentDateTime().getTimeInMillis();
             if (now > usernameReservation.getExpiresOn().getTimeInMillis())
                 throw new ReservationExpiredException(usernameReservation.getExpiresOn());
             if (now > emailReservation.getExpiresOn().getTimeInMillis())
                 throw new ReservationExpiredException(emailReservation.getExpiresOn());
 
             Assert.assertTrue(usernameReservation.getUsername().equals(credentials.getUsername()),
                     "Reservation username {0} does not match credentials username {1}.",
                     usernameReservation.getUsername(), credentials.getUsername());
             Assert.assertTrue(emailReservation.getEMail().equals(email),
                     "Reservation e-mail address {0} does not match e-mail address {1}.",
                     emailReservation.getEMail(), email);
 
             final Product product = new Product();
             product.setName(Constants.Product.NAME);
 
             final Release release = new Release();
             release.setName(Constants.Release.NAME);
             release.setOs(OSUtil.getOs());
 
             final ServiceFactory serviceFactory = workspace.getServiceFactory(newFiniteRetryHandler());
             final ProfileService profileService = serviceFactory.getProfileService();
             profileService.create(newEmptyAuthToken(), product, release,
                     usernameReservation, emailReservation, credentials, profile,
                     email, securityCredentials, paymentInfo);
         } catch (final ReservationExpiredException rex) {
             throw rex;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#create(com.thinkparity.codebase.model.profile.UsernameReservation, com.thinkparity.codebase.model.profile.EMailReservation, com.thinkparity.codebase.model.session.Credentials, com.thinkparity.codebase.model.profile.Profile, com.thinkparity.codebase.email.EMail, com.thinkparity.codebase.model.profile.SecurityCredentials, com.thinkparity.codebase.model.profile.payment.PaymentPlanCredentials)
      *
      */
     @Override
     public void create(final UsernameReservation usernameReservation,
             final EMailReservation emailReservation, final Credentials credentials,
             final Profile profile, final EMail email,
             final SecurityCredentials securityCredentials,
             final PaymentPlanCredentials paymentPlanCredentials)
             throws ReservationExpiredException, InvalidCredentialsException {
         try {
             validate(profile);
             validate(securityCredentials);
             validate(paymentPlanCredentials);
 
             final long now = currentDateTime().getTimeInMillis();
             if (now > usernameReservation.getExpiresOn().getTimeInMillis())
                 throw new ReservationExpiredException(usernameReservation.getExpiresOn());
             if (now > emailReservation.getExpiresOn().getTimeInMillis())
                 throw new ReservationExpiredException(emailReservation.getExpiresOn());
 
             Assert.assertTrue(usernameReservation.getUsername().equals(credentials.getUsername()),
                     "Reservation username {0} does not match credentials username {1}.",
                     usernameReservation.getUsername(), credentials.getUsername());
             Assert.assertTrue(emailReservation.getEMail().equals(email),
                     "Reservation e-mail address {0} does not match e-mail address {1}.",
                     emailReservation.getEMail(), email);
 
             final Product product = new Product();
             product.setName(Constants.Product.NAME);
 
             final Release release = new Release();
             release.setName(Constants.Release.NAME);
             release.setOs(OSUtil.getOs());
 
             final ServiceFactory serviceFactory = workspace.getServiceFactory(newFiniteRetryHandler());
             final ProfileService profileService = serviceFactory.getProfileService();
             profileService.create(newEmptyAuthToken(), product, release,
                     usernameReservation, emailReservation, credentials, profile,
                     email, securityCredentials, paymentPlanCredentials);
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final ReservationExpiredException rex) {
             throw rex;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#createEMailReservation(com.thinkparity.codebase.email.EMail)
      * 
      */
     public EMailReservation createEMailReservation(final EMail email) {
         try {
             final ServiceFactory serviceFactory = workspace.getServiceFactory(newFiniteRetryHandler());
             final ProfileService profileService = serviceFactory.getProfileService();
             return profileService.createEMailReservation(newEmptyAuthToken(), email);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#createUsernameReservation(java.lang.String)
      *
      */
     public UsernameReservation createUsernameReservation(final String username) {
         try {
             final ServiceFactory serviceFactory = workspace.getServiceFactory(newFiniteRetryHandler());
             final ProfileService profileService = serviceFactory.getProfileService();
             return profileService.createUsernameReservation(newEmptyAuthToken(), username);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.profile.DisabledEvent)
      *
      */
     @Override
     public void handleEvent(final ActiveEvent event) {
         try {
             final Profile profile = profileIO.read(localUserId());
             if (profile.isActive()) {
                 if (event.isActive()) {
                     /* the profile is already active */
                     return;
                 } else {
                     profile.setActive(Boolean.FALSE);
                     profileIO.updateActive(profile);
                 }
             } else {
                 if (event.isActive()) {
                     profile.setActive(Boolean.TRUE);
                     profileIO.updateActive(profile);
                 } else {
                     /* the profile is already passive */
                     return;
                 }
             }
             if (profile.isActive()) {
                 notifyActivated(profile, remoteEventGen);
             } else {
                 notifyPassivated(profile, remoteEventGen);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentFailedEvent)
      *
      */
     @Override
     public void handleEvent(final PaymentEvent event) {
         try {
             final Profile profile = profileIO.read(localUserId());
             if (profile.isActive()) {
                 if (event.isSuccess()) {
                     /* the profile is already passive; do nothing */
                     return;
                 } else {
                     profile.setActive(Boolean.FALSE);
                     profileIO.updateActive(profile);
                 }
             } else {
                 if (event.isSuccess()) {
                     profile.setActive(Boolean.TRUE);
                     profileIO.updateActive(profile);
                 } else {
                     /* the profile is already active; do nothing */
                     return;
                 }
             }
             if (profile.isActive()) {
                 notifyActivated(profile, remoteEventGen);
             } else {
                 notifyPassivated(profile, remoteEventGen);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#handleEvent(com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentPlanInArrearsEvent)
      *
      */
     @Override
     public void handleEvent(final PaymentPlanArrearsEvent event) {
         try {
             final Profile profile = profileIO.read(localUserId());
             if (profile.isActive()) {
                 if (event.isInArrears()) {
                     profile.setActive(Boolean.FALSE);
                     profileIO.updateActive(profile);
                 } else {
                     /* the profile is already passive */
                     return;
                 }
             } else {
                 if (event.isInArrears()) {
                     /* the profile is already active */
                     return;
                 } else {
                     profile.setActive(Boolean.TRUE);
                     profileIO.updateActive(profile);
                 }
             }
             if (profile.isActive()) {
                 notifyActivated(profile, remoteEventGen);
             } else {
                 notifyPassivated(profile, remoteEventGen);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#initialize()
      * 
      */
     public void initialize() {
         try {
             /* download */
             final AuthToken authToken = getAuthToken();
             final Profile profile = profileService.read(authToken);
             final ProfileEMail remoteEMail = profileService.readEMail(authToken);
 
             /* create */
             final User profileUser = getUserModel().readLazyCreate(
                     profile.getId());
             profile.setLocalId(profileUser.getLocalId());
             profileIO.create(profile);
 
             /* create e-mail */
             final ProfileEMail localEMail = new ProfileEMail();
             localEMail.setEmail(remoteEMail.getEmail());
             localEMail.setProfileId(profile.getLocalId());
             localEMail.setVerified(remoteEMail.isVerified());
             profileIO.createEmail(profile.getLocalId(), localEMail);
 
             /* index */
             getIndexModel().indexProfile();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isAccessiblePaymentInfo()
      *
      */
     @Override
     public Boolean isAccessiblePaymentInfo() {
         try {
             return profileService.isAccessiblePaymentInfo(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isAvailable(com.thinkparity.codebase.email.EMail)
      *
      */
     public Boolean isAvailable(final EMail email) {
         try {
             // check contacts first
             final List<Contact> contacts = getContactModel().read();
             final List<EMail> emails = new ArrayList<EMail>();
             for (final Contact contact : contacts)
                 emails.addAll(contact.getEmails());
             for (final EMail contactEmail : emails)
                 if (email.toString().equals(contactEmail.toString()))
                     return Boolean.FALSE;
             // check all users
             return profileService.isEMailAvailable(getAuthToken(), email);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#isBackupAvailable()
      *
      */
     public Boolean isBackupEnabled() {
         try {
             final Profile profile = read();
             final List<Feature> features = profileIO.readFeatures(profile.getLocalId());
             for (final Feature feature : features) {
                 if (Features.BACKUP.equals(feature.getName())) {
                     return Boolean.TRUE;
                 }
             }
             return Boolean.FALSE;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#isBackupAvailable()
      *
      */
     public Boolean isCoreEnabled() {
         try {
             final Profile profile = read();
             final List<Feature> features = profileIO.readFeatures(profile.getLocalId());
             for (final Feature feature : features) {
                 if (Features.CORE.equals(feature.getName())) {
                     return Boolean.TRUE;
                 }
             }
             return Boolean.FALSE;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isInviteAvailable(User)
      * 
      */
     public Boolean isInviteAvailable(final User user) {
         try {
             return !getSessionModel().isInviteRestricted(user).booleanValue();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isRequiredPaymentInfo()
      *
      */
     @Override
     public Boolean isRequiredPaymentInfo() {
         try {
             /* NOTE - ProfileModelImpl#isRequiredPaymentInfo - should only be called
              * prior to initializing the workspace; but after creating the
              * profile; therefore a manual login is required */
             final AuthToken authToken = sessionService.login(readCredentials());
             try {
                 return profileService.isRequiredPaymentInfo(authToken);
             } finally {
                 sessionService.logout(authToken);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isSetPaymentInfo()
      *
      */
     @Override
     public Boolean isSetPaymentInfo() {
         try {
             /* NOTE - ProfileModelImpl#isSetPaymentInfo - should only be called
              * prior to initializing the workspace; but after creating the
              * profile; therefore a manual login is required */
             final AuthToken authToken = sessionService.login(readCredentials());
             try {
                 return profileService.isSetPaymentInfo(getAuthToken());
             } finally {
                 sessionService.logout(authToken);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#isSignUpAvailable()
      * 
      */
     public Boolean isSignUpAvailable() {
         try {
             return Boolean.valueOf(!isCoreEnabled().booleanValue());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the logged in user's profile. Note that the user's profile will
      * always exist remotely. If it does not exist locally (in the form of a row
      * in the user table) create it.
      * 
      * @return A profile.
      */
     public Profile read() {
         logger.logApiId();
         try {
             return profileIO.read(localUserId());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readBackupStatistics()
      *
      */
     public BackupStatistics readBackupStatistics() {
         try {
             Assert.assertTrue(isBackupEnabled(), "Backup is not enabled.");
 
             final BackupStatistics backupStatistics = new BackupStatistics();
             // NOCOMMIT - ProfileModelImpl#readBackupStatistics - Move to server.
             backupStatistics.setDiskUsageAllotment(DU_BACKUP_ALLOTMENT);
             backupStatistics.setDiskUsage(profileIO.readDiskUsage());
             return backupStatistics;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#readCredentials()
      * 
      */
     public Credentials readCredentials() {
         return super.readCredentials();
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readEMail()
      *
      */
     @Override
     public ProfileEMail readEMail() {
         try {
             final List<ProfileEMail> profileEMails = readEMails();
             return profileEMails.get(0);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#readEMails()
      *
      */
     @Override
     public  List<ProfileEMail> readEMails() {
         try {
             final Profile profile = read();
             final List<ProfileEMail> profileEMails = profileIO.readEmails(
                     profile.getLocalId());
             if (1 < profileEMails.size()) {
                 logger.logWarning("Profile contains {0} e-mails.",
                         profileEMails.size());
             }
             return profileEMails;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readFeatures()
      *
      */
     public List<Feature> readFeatures() {
         try {
             return getSessionModel().readMigratorProductFeatures(Constants.Product.NAME);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readIsActive()
      *
      */
     @Override
     public Boolean readIsActive() {
         try {
             return profileIO.isActive(profileIO.read(localUserId()));
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readStatistics()
      *
      */
     public Statistics readStatistics() {
         try {
             final Statistics statistics = new Statistics();
             statistics.setDiskUsage(profileIO.readDiskUsage());
             return statistics;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#readUsername()
      *
      */
     @Override
     public String readUsername() {
         try {
             return readCredentials().getUsername();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#removeListener(com.thinkparity.ophelia.model.events.ProfileListener)
      *
      */
     @Override
     public void removeListener(final ProfileListener listener) {
         super.removeListener(listener);
     }
 
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#restoreBackup(com.thinkparity.ophelia.model.util.ProcessMonitor)
      *
      */
     @Override
     public void restoreBackup(final ProcessMonitor monitor) {
         try {
             final Profile profile = read();
             /* delete e-mail */
             final List<ProfileEMail> emails = readEMails();
             for (final ProfileEMail email : emails) {
                 profileIO.deleteEmail(profile.getLocalId(), email.getEmailId());
             }
 
             /* delete */
             profileIO.delete(profile);
 
             /* create */
             initialize();
 
             /* fire event */
             //notifyProfileUpdated(profile, localEventGen);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#search(java.lang.String)
      *
      */
     public List<JabberId> search(final String expression) {
         try {
             return getIndexModel().searchProfile(expression);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#signUp(com.thinkparity.codebase.model.profile.payment.PaymentInfo)
      *
      */
     @Override
     public void signUp(final PaymentInfo paymentInfo) {
         try {
             validate(paymentInfo);
 
             final List<Feature> featureList = getSessionModel().readMigratorProductFeatures(Constants.Product.NAME);
             final Profile profile = profileIO.read(localUserId());
             final List<Feature> existingFeatureList = profile.getFeatures();
             for (final Feature feature : featureList) {
                if (existingFeatureList.contains(feature)) {
                    continue;
                } else {
                     profile.add(feature);
                 }
             }
             profileIO.updateFeatures(profile);
             profileService.update(getAuthToken(), profile, paymentInfo);
             notifyProfileUpdated(profile, localEventGen);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Update the logged in user's profile.
      * 
      * @param profile
      *            A profile.
      */
     public void update(final Profile profile) {
         logger.logApiId();
         logger.logVariable("profile", profile);
         try {
             validate(profile);
             
             // update local data
             profileIO.update(profile);
             getIndexModel().indexProfile();
             getSessionModel().updateProfile(localUserId(), profile);
             notifyProfileUpdated(read(), localEventGen);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#updateEMail(com.thinkparity.codebase.email.EMail)
      *
      */
     @Override
     public void updateEMail(final EMail email) throws EMailIntegrityException {
         try {
             final Profile profile = read();
             final List<ProfileEMail> profileEMails = readEMails();
             if (1 < profileEMails.size()) {
                 logger.logWarning("Profile contains {0} e-mail addresses.",
                         profileEMails.size());
             }
             for (final ProfileEMail profileEMail : profileEMails) {
                 try {
                     profileIO.deleteEmail(profileEMail.getProfileId(),
                             profileEMail.getEmailId());
                 } catch (final HypersonicException hx) {
                     if (isSQLIntegrityConstraintViolation(hx)) {
                         logger.logWarning("Could not delete e-mail {0}.  {1}",
                                 profileEMail.getEmail(), hx.getMessage());
                         throw new EMailIntegrityException(profileEMail.getEmail());
                     } else {
                         throw hx;
                     }
                 }
             }
             // add email data
             final ProfileEMail profileEMail = new ProfileEMail();
             profileEMail.setEmail(email);
             profileEMail.setProfileId(profile.getLocalId());
             profileEMail.setVerified(Boolean.FALSE);
             profileIO.createEmail(profile.getLocalId(), profileEMail);
             // update remote e-mail
             profileService.updateEMail(getAuthToken(), email);
             // fire event
             notifyEmailUpdated(profile, profileEMail, localEventGen);
         } catch (final EMailIntegrityException emix) {
             throw emix;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#updatePassword(com.thinkparity.codebase.model.session.Credentials,
      *      java.lang.String)
      * 
      */
     public void updatePassword(final Credentials credentials,
             final String newPassword) throws InvalidCredentialsException {
         try {
             validateCredentials(credentials);
 
             // duplicate credentials
             final Credentials orginalCredentials = readCredentials();
 
             // update local data
             final Credentials localCredentials = readCredentials();
             localCredentials.setPassword(newPassword);
             updateCredentials(localCredentials);
 
             // update remote data.
             getSessionModel().updateProfilePassword(orginalCredentials, newPassword);
             notifyPasswordUpdated(read(), localEventGen);
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#updatePaymentInfo(com.thinkparity.codebase.model.profile.payment.PaymentInfo)
      *
      */
     @Override
     public void updatePaymentInfo(final PaymentInfo paymentInfo) {
         try {
             Assert.assertTrue(isAccessiblePaymentInfo(),
                     "Cannot access payment info.");
             validate(paymentInfo);
 
             profileService.updatePaymentInfo(getAuthToken(), paymentInfo);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.InternalProfileModel#updateProductRelease()
      *
      */
     public void updateProductRelease() {
         try {
             final Product product = new Product();
             product.setName(Constants.Product.NAME);
 
             final Release release = new Release();
             release.setName(Constants.Release.NAME);
             release.setOs(OSUtil.getOs());
 
             profileService.updateProductRelease(getAuthToken(), product, release);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#validateCredentials(com.thinkparity.codebase.model.session.Credentials)
      *
      */
     public void validateCredentials(final Credentials credentials) throws InvalidCredentialsException {
         try {
             final Credentials localCredentials = readCredentials();
             if (localCredentials.getUsername().equals(credentials.getUsername()) &&
                     localCredentials.getPassword().equals(credentials.getPassword())) {
             } else {
                 throw new InvalidCredentialsException();
             }
         } catch (final InvalidCredentialsException icx) {
             delay();
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#verifyEMail(java.lang.Long,
      *      java.lang.String)
      * 
      */
     @Override
     public void verifyEMail(final Long emailId, final String key) {
         try {
             final Profile profile = read();
             final ProfileEMail email = profileIO.readEmail(profile.getLocalId(), emailId);
             getSessionModel().verifyProfileEmail(email, key);
             profileIO.verifyEmail(email.getProfileId(), email.getEmailId(), Boolean.TRUE);
             notifyEmailVerified(read(), readEMail(), localEventGen);
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
         this.profileIO = IOFactory.getDefault(workspace).createProfileHandler();
 
         final ServiceFactory serviceFactory = workspace.getServiceFactory(newDefaultRetryHandler());
         this.profileService = serviceFactory.getProfileService();
         this.sessionService = serviceFactory.getSessionService();
     }
 
     /**
      * Assert that a named value is set.
      * 
      * @param name
      *            A value name <code>String</code>..
      * @param value
      *            A value <code>String</code>.
      */
     private void assertIsSet(final String name, final String value) {
        Assert.assertNotNull(value, "Profile field {0} is not set.", name);
     }
 
     private void assertIsValid(final ProfileVCard vcard) {
         assertIsSet("country", vcard.getCountry());
         assertIsValidCountry("country", vcard.getCountry());
         assertIsSet("language", vcard.getLanguage());
         assertIsValidLanguage("language", vcard.getLanguage());
         assertIsSet("name", vcard.getName());
         assertIsSet("organization", vcard.getOrganization());
         assertIsSet("organization country", vcard.getOrganizationCountry());
         assertIsValidCountry("organization country", vcard.getOrganizationCountry());
         assertIsSet("timeZone", vcard.getTimeZone());
         assertIsValidTimeZone("timeZone", vcard.getTimeZone());
         assertIsSet("title", vcard.getTitle());
     }
 
     private void assertIsValidCountry(final String name, final String value) {
         final Locale[] locales = LocaleUtil.getInstance().getAvailableLocales();
         boolean found = false;
         for (final Locale locale : locales) {
             if (locale.getISO3Country().equals(value)) {
                 found = true;
                 break;
             }
         }
         Assert.assertTrue(found, "Profile field {0} is invalid.", name, value);
     }
 
     private void assertIsValidLanguage(final String name, final String value) {
         final Locale[] locales = LocaleUtil.getInstance().getAvailableLocales();
         boolean found = false;
         for (final Locale locale : locales) {
             if (locale.getISO3Language().equals(value)) {
                 found = true;
                 break;
             }
         }
         Assert.assertTrue(found, "Profile field {0} contains invalid value {1}.", name, value);
     }
 
     private void assertIsValidTimeZone(final String name, final String value) {
         final TimeZone timeZone = TimeZone.getTimeZone(value);
         Assert.assertTrue(timeZone.getID().equals(value), "Profile field {0} contains invalid value {1}.", name, value);
     }
 
     /**
      * Delay the current thread for a number of seconds.
      *
      */
     private void delay() {
         try {
             Thread.sleep(3 * 1000);
         } catch (final InterruptedException ix) {}
     }
 
     /**
      * Obtain the web-service authentication token.
      * 
      * @return An <code>AuthToken</code>.
      */
     private AuthToken getAuthToken() {
         return getSessionModel().getAuthToken();
     }
 
     /**
      * Create an empty authentication token.
      * 
      * @return An <code>AuthToken</code>.
      */
     private AuthToken newEmptyAuthToken() {
         /* HACK - ProfileModelImpl#newEmptyAuthToken() - here we need to ensure
          * correct client id; client version */
         return new AuthToken();
     }
 
     /**
      * Fire an event indicating that the profile was activated.
      * 
      * @param profile
      *            A <code>Profile</code>.
      * @param peg
      *            A <code>ProfileEventGenerator</code>.
      */
     private void notifyActivated(final Profile profile,
             final ProfileEventGenerator peg) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.profileActivated(peg.generate(profile));
             }
         });
     }
 
     /**
      * Notify that an email address has been removed.
      * 
      * @param profile
      *      A <code>Profile</code>.
      * @param email
      *      An <code>EMail</code> address.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyEmailUpdated(final Profile profile,
             final ProfileEMail email, final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.emailUpdated(eventGenerator.generate(profile, email));
             }
         });
     }
 
     /**
      * Notify that an email address has been verified.
      * 
      * @param profile
      *      A <code>Profile</code>.
      * @param email
      *      An <code>EMail</code> address.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyEmailVerified(final Profile profile,
             final ProfileEMail email,
             final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.emailVerified(eventGenerator.generate(profile, email));
             }
         });
     }
 
     /**
      * Fire an event indicating that the profile was passivated.
      * 
      * @param profile
      *            A <code>Profile</code>.
      * @param peg
      *            A <code>ProfileEventGenerator</code>.
      */
     private void notifyPassivated(final Profile profile,
             final ProfileEventGenerator peg) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.profilePassivated(peg.generate(profile));
             }
         });
     }
 
     /**
      * Notify that the password has been updated.
      * 
      * @param profile
      *      A <code>Profile</code>.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyPasswordUpdated(final Profile profile,
             final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.passwordUpdated(eventGenerator.generate(profile));
             }
         });
     }
 
     /**
      * Notify that the profile has been updated.
      * 
      * @param profile
      *      A <code>Profile</code>.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyProfileUpdated(final Profile profile,
             final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.profileUpdated(eventGenerator.generate(profile));
             }
         });
     }
 
     /**
      * Validate the payment info.
      * 
      * @param paymentInfo
      *            A <code>PaymentInfo</code>.
      */
     private void validate(final PaymentInfo paymentInfo) {
         final PaymentInfoConstraints constraints = PaymentInfoConstraints.getInstance();
         constraints.getCardExpiryMonth().validate(paymentInfo.getCardExpiryMonth());
         constraints.getCardExpiryYear().validate(paymentInfo.getCardExpiryYear());
         constraints.getCardNumber().validate(paymentInfo.getCardNumber());
         constraints.getCardName().validate(paymentInfo.getCardName());
     }
 
     /**
      * Validate the payment plan credentials.
      * 
      * @param paymentPlanCredentials
      *            A set of <code>PaymentPlanCredentials</code>.
      */
     private void validate(
             final PaymentPlanCredentials paymentPlanCredentials) {
         final PaymentPlanCredentialsConstraints constraints = PaymentPlanCredentialsConstraints.getInstance();
         constraints.getName().validate(paymentPlanCredentials.getName());
         constraints.getPassword().validate(paymentPlanCredentials.getName());
     }
 
     private void validate(final Profile profile) {
         assertIsValid(profile.getVCard());
     }
 
     private void validate(final SecurityCredentials securityCredentials) {
         Assert.assertNotNull(securityCredentials, "Security question cannot be null.");
         Assert.assertNotNull(securityCredentials.getQuestion(), "Security question cannot be null.");
         Assert.assertNotNull(securityCredentials.getAnswer(), "Security question answer cannot be null.");
     }
 }
