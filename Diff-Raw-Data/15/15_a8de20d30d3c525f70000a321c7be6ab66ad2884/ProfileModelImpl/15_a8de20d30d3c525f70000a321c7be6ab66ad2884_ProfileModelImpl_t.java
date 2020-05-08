 /*
  * Generated On: Jul 17 06 11:52:34 AM
  */
 package com.thinkparity.ophelia.model.profile;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import com.thinkparity.codebase.LocaleUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.profile.EMailReservation;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.profile.ProfileEMail;
 import com.thinkparity.codebase.model.profile.ProfileVCard;
 import com.thinkparity.codebase.model.profile.UsernameReservation;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 import com.thinkparity.codebase.model.session.TemporaryCredentials;
 
 import com.thinkparity.ophelia.model.Constants;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.Constants.Product.Features;
 import com.thinkparity.ophelia.model.events.ProfileEvent;
 import com.thinkparity.ophelia.model.events.ProfileListener;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.handler.ProfileIOHandler;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 /**
  * <b>Title:</b>thinkParity Profile Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.15
  */
 public final class ProfileModelImpl extends Model<ProfileListener> implements
         ProfileModel, InternalProfileModel {
 
     /** A <code>ProfileEventGenerator</code> for local events. */
     private final ProfileEventGenerator localEventGenerator;
 
     /** The profile db io. */
     private ProfileIOHandler profileIO;
 
     /**
      * Create ProfileModelImpl.
      *
      */
     public ProfileModelImpl() {
         super();
         this.localEventGenerator = new ProfileEventGenerator(ProfileEvent.Source.LOCAL);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#addEmail(com.thinkparity.codebase.email.EMail)
      * 
      */
     public void addEmail(final EMail email) {
         try {
             final Profile profile = read();
             // add email data
             final ProfileEMail profileEMail = new ProfileEMail();
             profileEMail.setEmail(email);
             profileEMail.setProfileId(profile.getLocalId());
             profileEMail.setVerified(Boolean.FALSE);
             profileIO.createEmail(profile.getLocalId(), profileEMail);
             // add email remotely
             getSessionModel().addProfileEmail(localUserId(), profileEMail);
             // fire event
             notifyEmailAdded(read(), readEmail(profileEMail.getEmailId()),
                     localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
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
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#create(com.thinkparity.codebase.model.profile.Reservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail)
      * 
      */
     public void create(final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final String securityQuestion,
             final String securityAnswer) throws ReservationExpiredException {
         try {
             assertIsValid(profile);
             Assert.assertNotNull(securityQuestion, "Security question cannot be null.");
             Assert.assertNotNull(securityAnswer, "Security question answer cannot be null.");
 
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
 
             /* HACK - ProfileModelImpl#create() - security question/answer are
              * not specified */
             getSessionModel().createProfile(usernameReservation,
                     emailReservation, credentials, profile, email,
                     securityQuestion, securityAnswer);
         } catch (final ReservationExpiredException rex) {
             throw rex;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#createCredentials(java.lang.String, com.thinkparity.codebase.email.EMail, java.lang.String)
      *
      */
     public TemporaryCredentials createCredentials(final String profileKey,
             final String securityAnswer) {
         try {
             return getSessionModel().createProfileCredentials(profileKey,
                     securityAnswer);
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
             return getSessionModel().createProfileEMailReservation(email);
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
             return getSessionModel().createProfileUsernameReservation(username);
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
             assertXMPPOnline();
             final Profile remoteProfile = getSessionModel().readProfile();
             final List<ProfileEMail> remoteEMails =
                 getSessionModel().readProfileEMails();
             profileIO.create(remoteProfile);
             ProfileEMail localEMail;
             for (final ProfileEMail remoteEMail : remoteEMails) {
                 localEMail = new ProfileEMail();
                 localEMail.setEmail(remoteEMail.getEmail());
                 localEMail.setProfileId(remoteProfile.getLocalId());
                 localEMail.setVerified(remoteEMail.isVerified());
                 profileIO.createEmail(remoteProfile.getLocalId(), localEMail);
             }
             getIndexModel().indexProfile();
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
             return getSessionModel().isEmailAvailable(localUserId(), email);
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
      * @see com.thinkparity.ophelia.model.Model#readCredentials()
      * 
      */
     public Credentials readCredentials() {
         return super.readCredentials();
     }
 
     /**
      * Read a profile email.
      * 
      * @param emailId
      *            An email id <code>Long</code>.
      * @return A <code>ProfileEmail</code>.
      */
     public ProfileEMail readEmail(final Long emailId) {
         logger.logApiId();
         logger.logVariable("emailId", emailId);
         try {
             final Profile profile = read();
             return profileIO.readEmail(profile.getLocalId(), emailId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read a list of profile email addresses.
      * 
      * @return A list of email addresses.
      */
     public List<ProfileEMail> readEmails() {
         logger.logApiId();
         try {
             final Profile profile = read();
             return profileIO.readEmails(profile.getLocalId());
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
      * Read the security question.
      * 
      * @return A security question.
      */
     public String readSecurityQuestion() {
         logger.logApiId();
         try {
             final Profile profile = read();
             return getSessionModel().readProfileSecurityQuestion(profile.getId());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#removeEmail(java.lang.Long)
      * 
      */
     public void removeEmail(final Long emailId) {
         try {
             final Profile profile = read();
             final ProfileEMail email = profileIO.readEmail(profile.getLocalId(), emailId);
             // remove email data
             profileIO.deleteEmail(email.getProfileId(), email.getEmailId());
             // remove email remotely
             getSessionModel().removeProfileEmail(localUserId(), email);
             notifyEmailRemoved(read(), email, localEventGenerator);
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
      * Update the logged in user's profile.
      * 
      * @param profile
      *            A profile.
      */
     public void update(final Profile profile) {
         logger.logApiId();
         logger.logVariable("profile", profile);
         try {
             assertIsValid(profile);
             
             // update local data
             profileIO.update(profile);
             getIndexModel().indexProfile();
             getSessionModel().updateProfile(localUserId(), profile);
             notifyProfileUpdated(read(), localEventGenerator);
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
             notifyPasswordUpdated(read(), localEventGenerator);
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#updatePassword(com.thinkparity.codebase.model.session.TemporaryCredentials, java.lang.String)
      *
      */
     public void updatePassword(final TemporaryCredentials credentials,
             final String newPassword) throws InvalidCredentialsException {
         try {
             final Credentials localCredentials = readCredentials();
             validateCredentials(credentials, localCredentials);
 
             /* there are two possible scenarios - the first is that the user has
              * never logged in to this workspace before, the second is that they
              * have */
             if (null == localCredentials) {
                 // update remote password
                 getSessionModel().updateProfilePassword(credentials,
                         newPassword);
             } else {
                 // update local password
                 localCredentials.setPassword(newPassword);
                 updateCredentials(localCredentials);
                 // update remote password
                 getSessionModel().updateProfilePassword(credentials,
                         newPassword);
             }
             notifyPasswordUpdated(read(), localEventGenerator);
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
             try {
                 Thread.sleep(3 * 1000);
             } catch (final InterruptedException ix) {}
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.profile.ProfileModel#verifyEmail(java.lang.Long,
      *      java.lang.String)
      * 
      */
     public void verifyEmail(final Long emailId, final String key) {
         try {
             final Profile profile = read();
             final ProfileEMail email = profileIO.readEmail(profile.getLocalId(), emailId);
             getSessionModel().verifyProfileEmail(localUserId(), email, key);
             profileIO.verifyEmail(email.getProfileId(), email.getEmailId(), Boolean.TRUE);
             notifyEmailVerified(read(), readEmail(email.getEmailId()),
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
         this.profileIO = IOFactory.getDefault(workspace).createProfileHandler();
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
 
     private void assertIsValid(final Profile profile) {
         assertIsValid(profile.getVCard());
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
      * Notify that an email address has been added.
      * 
      * @param profile
      *      A <code>Profile</code>.
      * @param email
      *      An <code>EMail</code> address.
      * @param eventGenerator
      *            An event generator.
      */
     private void notifyEmailAdded(final Profile profile,
             final ProfileEMail email,
             final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.emailAdded(eventGenerator.generate(profile, email));
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
     private void notifyEmailRemoved(final Profile profile,
             final ProfileEMail email,
             final ProfileEventGenerator eventGenerator) {
         notifyListeners(new EventNotifier<ProfileListener>() {
             public void notifyListener(final ProfileListener listener) {
                 listener.emailRemoved(eventGenerator.generate(profile, email));
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
      * Validate the temporary credentials.
      * 
      * @param credentials
      *            A set of <code>TemporaryCredentials</code>.
      * @throws InvalidCredentialsException
      */
     private void validateCredentials(final TemporaryCredentials credentials,
             final Credentials localCredentials)
             throws InvalidCredentialsException {
         try {
             if (null == credentials)
                 throw new InvalidCredentialsException();
             if (null == credentials.getUsername())
                 throw new InvalidCredentialsException();
             if (null == credentials.getToken())
                 throw new InvalidCredentialsException();
             if (null != localCredentials)
                 if (!localCredentials.getUsername().equals(credentials.getUsername()))
                     throw new InvalidCredentialsException();
         } catch (final InvalidCredentialsException icx) {
             try {
                 Thread.sleep(3 * 1000);
             } catch (final InterruptedException ix) {}
             throw icx;
         }
     }
 }
