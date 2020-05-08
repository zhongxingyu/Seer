 /*
  * Generated On: Jul 19 06 03:25:41 PM
  * TODO - Check the state of the previous invoice transaction prior to charging
  * a new transaction.
  * TODO - Figure out who needs to be notified within notify complete so that
  * we're not spamming the client.  Need to know "join dates" of people to the
  * plan.
  * TODO - Switch xa to be cx based.
  * TODO - Test xa boundaries.
  */
 package com.thinkparity.desdemona.model.profile;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.security.GeneralSecurityException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 
 import com.thinkparity.codebase.LocaleUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.jabber.JabberIdBuilder;
 
 import com.thinkparity.codebase.model.ThinkParityException;
 import com.thinkparity.codebase.model.annotation.ThinkParityFilterEvent;
 import com.thinkparity.codebase.model.contact.IncomingEMailInvitation;
 import com.thinkparity.codebase.model.contact.OutgoingEMailInvitation;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.profile.*;
 import com.thinkparity.codebase.model.profile.payment.PaymentInfo;
 import com.thinkparity.codebase.model.profile.payment.PaymentPlanCredentials;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.InvalidCredentialsException;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.Token;
 import com.thinkparity.codebase.model.util.VCardWriter;
 import com.thinkparity.codebase.model.util.codec.MD5Util;
 import com.thinkparity.codebase.model.util.jta.Transaction;
 import com.thinkparity.codebase.model.util.xmpp.event.ContactUpdatedEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.XMPPEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentPlanArrearsEvent;
 
 import com.thinkparity.ophelia.model.util.UUIDGenerator;
 
 import com.thinkparity.desdemona.model.AbstractModelImpl;
 import com.thinkparity.desdemona.model.Constants;
 import com.thinkparity.desdemona.model.Delegate;
 import com.thinkparity.desdemona.model.Constants.Product.Ophelia;
 import com.thinkparity.desdemona.model.contact.InternalContactModel;
 import com.thinkparity.desdemona.model.io.hsqldb.HypersonicException;
 import com.thinkparity.desdemona.model.io.jta.TransactionManager;
 import com.thinkparity.desdemona.model.io.sql.ContactSql;
 import com.thinkparity.desdemona.model.io.sql.PaymentSql;
 import com.thinkparity.desdemona.model.io.sql.QueueSql;
 import com.thinkparity.desdemona.model.io.sql.UserSql;
 import com.thinkparity.desdemona.model.migrator.InternalMigratorModel;
 import com.thinkparity.desdemona.model.node.NodeService;
 import com.thinkparity.desdemona.model.profile.delegate.ProcessInvoiceQueue;
 import com.thinkparity.desdemona.model.profile.delegate.ProcessPaymentQueue;
 import com.thinkparity.desdemona.model.profile.payment.Currency;
 import com.thinkparity.desdemona.model.profile.payment.PaymentPlan;
 import com.thinkparity.desdemona.model.profile.payment.PaymentService;
 import com.thinkparity.desdemona.model.profile.payment.PaymentPlan.InvoicePeriod;
 import com.thinkparity.desdemona.model.profile.payment.provider.PaymentProvider;
 import com.thinkparity.desdemona.model.profile.payment.provider.PaymentProviderFactory;
 import com.thinkparity.desdemona.model.user.InternalUserModel;
 import com.thinkparity.desdemona.util.DateTimeProvider;
 import com.thinkparity.desdemona.util.smtp.SMTPService;
 
 /**
  * <b>Title:</b>thinkParity Profile Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author CreateModel.groovy
  * @version 1.1
  */
 public final class ProfileModelImpl extends AbstractModelImpl implements
         ProfileModel, InternalProfileModel {
 
     /**
      * Determine if the hypersonic (database layer) error was caused by an
      * integrity constraint violation.
      * 
      * @param hypersonicException
      *            A <code>HypersonicException</code>.
      * @return True if the error is caused by an integrity constraint.
      */
     static boolean isSQLIntegrityConstraintViolation(
             final HypersonicException hypersonicException) {
         if (SQLIntegrityConstraintViolationException.class.isAssignableFrom(
                 hypersonicException.getCause().getClass())) {
             return "23503".equals(
                     ((SQLException) hypersonicException.getCause()).getSQLState());
         } else {
             return false;
         }
     }
 
     /** Contact db io. */
     private ContactSql contactSql;
 
     /** A node service. */
     private final NodeService nodeService;
 
     /** The payment processing service. */
     private final PaymentService paymentService;
 
     /** Payment db io. */
     private PaymentSql paymentSql;
 
     /** Queue db io. */
     private QueueSql queueSql;
 
     /** An instance of <code>SMTPService</code>. */
     private final SMTPService smtpService;
 
     /** A transaction. */
     private Transaction transaction;
 
     /** A transaction context. */
     private Object transactionContext;
 
     /** A transaction manager. */
     private TransactionManager transactionManager;
 
     /** User db io. */
     private UserSql userSql;
 
     /**
      * Create ProfileModelImpl.
      * 
      */
     public ProfileModelImpl() {
         super();
         this.nodeService = NodeService.getInstance();
         this.paymentService = PaymentService.getInstance();
         this.smtpService = SMTPService.getInstance();
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#create(com.thinkparity.codebase.model.migrator.Product,
      *      com.thinkparity.codebase.model.migrator.Release,
      *      com.thinkparity.codebase.model.profile.UsernameReservation,
      *      com.thinkparity.codebase.model.profile.EMailReservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail,
      *      com.thinkparity.codebase.model.profile.SecurityCredentials)
      * 
      */
     @Override
     public void create(final Product product, final Release release,
             final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE);
         try {
             validate(product, release, profile);
             beginXA(xaContext);
             try {
                 final Calendar now = DateTimeProvider.getCurrentDateTime();
                 final VerificationKey key = create(now, product, release,
                         usernameReservation, emailReservation, credentials,
                         profile, email, securityCredentials);
                 /* set active */
                 final List<Profile> profileList = new ArrayList<Profile>(1);
                 profileList.add(profile);
                 updateProfilesActive(profileList, Boolean.TRUE);
                 verifyCreate(email, key);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#create(com.thinkparity.codebase.model.profile.UsernameReservation,
      *      com.thinkparity.codebase.model.profile.EMailReservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail,
      *      com.thinkparity.codebase.model.profile.SecurityCredentials)
      * 
      */
     public void create(final Product product, final Release release,
             final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials,
             final PaymentInfo paymentInfo) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE);
         try {
             beginXA(xaContext);
             try {
                 final Calendar now = DateTimeProvider.getCurrentDateTime();
                 /* create profile */
                 final VerificationKey key = create(now, product, release,
                         usernameReservation, emailReservation, credentials,
                         profile, email, securityCredentials);
                 /* create payment info; new plan, new info */
                 final Currency currency = paymentSql.readCurrency(
                         Constants.Currency.USD);
                 createPaymentPlan(currency, paymentInfo, profile, now);
                 /* wake the payment service */
                 wakePaymentService();
                 /* verify e-mail address */
                 verifyCreate(email, key);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#create(com.thinkparity.codebase.model.migrator.Product,
      *      com.thinkparity.codebase.model.migrator.Release,
      *      com.thinkparity.codebase.model.profile.UsernameReservation,
      *      com.thinkparity.codebase.model.profile.EMailReservation,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      com.thinkparity.codebase.model.profile.Profile,
      *      com.thinkparity.codebase.email.EMail,
      *      com.thinkparity.codebase.model.profile.SecurityCredentials,
      *      com.thinkparity.codebase.model.profile.payment.PaymentPlanCredentials)
      * 
      */
     @Override
     public void create(final Product product, final Release release,
             final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials,
             final PaymentPlanCredentials paymentPlanCredentials)
             throws InvalidCredentialsException {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE);
         try {
             beginXA(xaContext);
             try {
                 final Calendar now = DateTimeProvider.getCurrentDateTime();
                 /* create profile */
                 final VerificationKey key = create(now, product, release,
                         usernameReservation, emailReservation, credentials,
                         profile, email, securityCredentials);
                 /* reference payment plan */
                 final PaymentPlan paymentPlan = paymentSql.readPlan(
                         paymentPlanCredentials);
                 if (null == paymentPlan) {
                     throw new InvalidCredentialsException();
                 }
                 userSql.createPaymentPlan(profile, paymentPlan);
                 /* wake the payment service */
                 wakePaymentService();
                 /* verify creation */
                 verifyCreate(email, key);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#createEMailReservation(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.email.EMail, java.util.Calendar)
      * 
      */
     public EMailReservation createEMailReservation(final EMail email) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE_EMAIL_RESERVATION);
         try {
             beginXA(xaContext);
             try {
                 userSql.deleteReservations(DateTimeProvider
                         .getCurrentDateTime());
 
                 // expire in an hour
                 final Calendar createdOn = DateTimeProvider
                         .getCurrentDateTime();
                 final Calendar expiresOn = (Calendar) createdOn.clone();
                 expiresOn.set(Calendar.HOUR, expiresOn.get(Calendar.HOUR) + 1);
 
                 final EMailReservation reservation = new EMailReservation();
                 reservation.setCreatedOn(createdOn);
                 reservation.setExpiresOn(expiresOn);
                 reservation.setEMail(email);
 
                 /* NOTE - there is a deliberate non re-throw of any error */
                 try {
                     reservation.setToken(newToken());
                     userSql.createEMailReservation(reservation);
 
                     // if a user exists with the same e-mail game over
                     if (userSql.doesExist(reservation.getEMail())) {
                         userSql.deleteEMailReservation(reservation.getToken());
                         return null;
                     } else {
                         return reservation;
                     }
                 } catch (final Throwable t) {
                     logger.logError(t,
                             "Could not create e-mail reservation {0}.",
                             reservation);
                     return null;
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#createToken(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public Token createToken() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE_TOKEN);
         try {
             beginXA(xaContext);
             try {
                 final Calendar now = currentDateTime();
                 final Token existingToken = userSql.readProfileToken(user
                         .getId());
                 if (null != existingToken) {
                     getQueueModel().deleteEvents();
                     /*
                      * HACK - up until a point, the "thinkParity" user was not
                      * able to login - now this user does login such that
                      * certain apis can be exercised with an actual user login -
                      * threfore we need to ignore the user in this scenario
                      */
                     if (user.getId().equals(User.THINKPARITY.getId())) {
                         logger.logInfo("Logging in as system user.");
                     } else {
                         getArtifactModel().deleteDrafts(now);
                     }
                 }
                 userSql.updateProfileToken(user.getId(), newToken());
                 return userSql.readProfileToken(user.getId());
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#createUsernameReservation(com.thinkparity.codebase.jabber.JabberId,
      *      java.lang.String, java.util.Calendar)
      * 
      */
     public UsernameReservation createUsernameReservation(final String username) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_USERNAME_RESERVATION_CREATE);
         try {
             beginXA(xaContext);
             try {
                 userSql.deleteReservations(DateTimeProvider
                         .getCurrentDateTime());
 
                 // expire in an hour
                 final Calendar createdOn = DateTimeProvider
                         .getCurrentDateTime();
                 final Calendar expiresOn = (Calendar) createdOn.clone();
                 expiresOn.set(Calendar.HOUR, expiresOn.get(Calendar.HOUR) + 1);
 
                 /*
                  * NOTE - ProfileModelImpl#createReservation - usernames are
                  * case in-sensitive
                  */
                 final UsernameReservation reservation = new UsernameReservation();
                 reservation.setCreatedOn(createdOn);
                 reservation.setExpiresOn(expiresOn);
                 reservation.setUsername(username.toLowerCase());
 
                 /* NOTE - there is a deliberate non re-throw of any error */
                 try {
                     reservation.setToken(newToken());
                     userSql.createUsernameReservation(reservation);
 
                     // if a user exists with the same username game over
                     if (userSql.doesExist(reservation.getUsername())) {
                         userSql.deleteUsernameReservation(reservation
                                 .getToken());
                         return null;
                     } else {
                         return reservation;
                     }
                 } catch (final Throwable t) {
                     logger.logError(t,
                             "Could not create username reservation {0}.",
                             reservation);
                     return null;
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#isAccessiblePaymentInfo()
      * 
      */
     @Override
     public Boolean isAccessiblePaymentInfo() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_IS_ACCESSIBLE_PAYMENT_INFO);
         try {
             beginXA(xaContext);
             try {
                 return Boolean.valueOf(isAccessiblePaymentInfo(xaContext));
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#isEmailAvailable(com.thinkparity.codebase.email.EMail)
      * 
      */
     public Boolean isEmailAvailable(final EMail email) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_IS_EMAIL_AVAILABLE);
         try {
             beginXA(xaContext);
             try {
                 return !userSql.doesExist(email).booleanValue();
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.InternalProfileModel#isEnabled()
      *
      */
     @Override
     public Boolean isQueueReadable() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_IS_QUEUE_READABLE);
         try {
             beginXA(xaContext);
             try {
                 /* in order for the user's queue to be readable; the user needs
                  * to be enabled; the user's payment plan must not be in arrears
                  * and the user must have at least one verified e-mail address
                  */
                 if (userSql.isActive(user)) {
                     final PaymentPlan plan = paymentSql.readPlan(user);
                     /* the plan can be null if the user is a guest */
                     if (null == plan) {
                         return 0 < userSql.readVerifiedEMailCount(user);
                     } else {
                         if (plan.isArrears()) {
                             return Boolean.FALSE;
                         } else {
                             return 0 < userSql.readVerifiedEMailCount(user);
                         }
                     }
                 } else {
                     return Boolean.FALSE;
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#isRequiredPaymentInfo()
      * 
      */
     @Override
     public Boolean isRequiredPaymentInfo() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_IS_REQUIRED_PAYMENT_INFO);
         try {
             beginXA(xaContext);
             try {
                 final List<Feature> features = readFeatures();
                 for (final Feature feature : features) {
                     if (Constants.Product.Ophelia.Feature.CORE.equals(feature
                             .getName())) {
                         return Boolean.TRUE;
                     }
                 }
                 return Boolean.FALSE;
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.InternalProfileModel#isSetPayment()
      * 
      */
     @Override
     public Boolean isSetPaymentInfo() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_IS_SET_PAYMENT_INFO);
         try {
             beginXA(xaContext);
             try {
                 return paymentSql.doesExistPlan(user);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#processInvoiceQueue()
      *
      */
     @Override
     public void processInvoiceQueue() {
         try {
             final ProcessInvoiceQueue delegate =
                 createDelegate(ProcessInvoiceQueue.class);
             delegate.setNode(nodeService.getNode());
             delegate.processInvoiceQueue();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#processPaymentQueue()
      * 
      */
     @Override
     public void processPaymentQueue() {
         try {
             final ProcessPaymentQueue delegate =
                 createDelegate(ProcessPaymentQueue.class);
             delegate.setNode(nodeService.getNode());
             delegate.processPaymentQueue();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#read()
      * 
      */
     public Profile read() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ);
         try {
             beginXA(xaContext);
             try {
                 return read(xaContext);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#readEMails(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public ProfileEMail readEMail() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ_EMAIL);
         try {
             beginXA(xaContext);
             try {
                 final List<ProfileEMail> profileEMails = readEMails();
                 if (0 == profileEMails.size()) {
                     return null;
                 } else {
                     return profileEMails.get(0);
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.InternalProfileModel#readEMails(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.user.User)
      * 
      */
     public List<EMail> readEMails(final JabberId userId, final User user) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ_EMAILS);
         try {
             beginXA(xaContext);
             try {
                 return readVerifiedEMails(user);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#readFeatures()
      * 
      */
     public List<Feature> readFeatures() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ_FEATURES);
         try {
             beginXA(xaContext);
             try {
                 return userSql.readProductFeatures(user, Ophelia.PRODUCT_NAME);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#readToken(com.thinkparity.codebase.jabber.JabberId)
      * 
      */
     public Token readToken() {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ_TOKEN);
         try {
             beginXA(xaContext);
             try {
                 return userSql.readProfileToken(user.getId());
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#createPaymentInfo(com.thinkparity.codebase.model.profile.payment.PaymentInfo)
      * 
      */
     @Override
     public void update(final Profile profile, final PaymentInfo paymentInfo) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE);
         try {
             beginXA(xaContext);
             try {
                 final Calendar now = DateTimeProvider.getCurrentDateTime();
                 final Product product = getMigratorModel().readProduct(Ophelia.PRODUCT_NAME);
                 final List<Feature> featureList = localize(product, profile.getFeatures());
                 userSql.updateFeatures(user, featureList);
                 /* create payment info; new plan, new info */
                 final Currency currency = paymentSql.readCurrency(
                         Constants.Currency.USD);
                 createPaymentPlan(currency, paymentInfo, user, now);
                 /* wake the payment service */
                 wakePaymentService();
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#update(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.profile.ProfileVCard)
      * 
      */
     public void update(final ProfileVCard vcard) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE);
         try {
             beginXA(xaContext);
             try {
                 validate(vcard);
 
                 final Profile profile = read(xaContext);
                 getUserModel(profile).updateVCard(vcard);
                 notifyContactUpdated();
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#updateEMail(com.thinkparity.codebase.email.EMail)
      * 
      */
     @Override
     public void updateEMail(final EMail email) throws EMailIntegrityException {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE_EMAIL);
         try {
             beginXA(xaContext);
             try {
                 final List<ProfileEMail> profileEMails = readEMails();
                 for (final ProfileEMail profileEMail : profileEMails) {
                     try {
                         userSql.deleteEmail(user.getLocalId(), profileEMail
                                 .getEmail());
                     } catch (final HypersonicException hx) {
                         if (isSQLIntegrityConstraintViolation(hx)) {
                             logger.logWarning("Cannot delete e-mail {0}.  {1}",
                                     profileEMail.getEmail(), hx.getMessage());
                             throw new EMailIntegrityException(profileEMail
                                     .getEmail());
                         } else {
                             throw hx;
                         }
                     }
                 }
                 // create remote data
                 final VerificationKey key = VerificationKey.generate(email);
                 userSql.createEmail(user.getLocalId(), email, key);
                 // send verification email
                 final MimeMessage mimeMessage = smtpService.createMessage();
                 createVerification(mimeMessage, email, key);
                 addRecipient(mimeMessage, email);
                 final InternetAddress fromInternetAddress = new InternetAddress();
                 fromInternetAddress
                         .setAddress(Constants.Internet.Mail.FROM_ADDRESS);
                 fromInternetAddress
                         .setPersonal(Constants.Internet.Mail.FROM_PERSONAL);
                 mimeMessage.setFrom(fromInternetAddress);
                 smtpService.deliver(mimeMessage);
                 // notify contacts
                 notifyContactUpdated();
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final EMailIntegrityException emix) {
             throw emix;
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#updatePassword(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.model.session.Credentials,
      *      java.lang.String)
      * 
      */
     public void updatePassword(final Credentials credentials,
             final String newPassword) throws InvalidCredentialsException {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE_PASSWORD);
         try {
             beginXA(xaContext);
             try {
                 final Credentials encrypted = encryptPassword(credentials);
                 final User user = userSql.read(encrypted);
                 if (null == user) {
                     throw new InvalidCredentialsException();
                 }
                 if (this.user.equals(user)) {
                     userSql.updatePassword(user.getId(), encrypted,
                             encrypt(newPassword));
                 } else {
                     throw new InvalidCredentialsException();
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final InvalidCredentialsException icx) {
             throw icx;
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#updatePaymentInfo(com.thinkparity.codebase.model.profile.payment.PaymentInfo)
      * 
      */
     @Override
     public void updatePaymentInfo(final PaymentInfo paymentInfo) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE_PAYMENT_INFO);
         try {
             beginXA(xaContext);
             try {
                 if (isAccessiblePaymentInfo(xaContext)) {
                     final PaymentPlan paymentPlan = paymentSql.readPlan(user);
                     paymentSql.updateInfo(paymentPlan, paymentInfo);
                 } else {
                     logger.logInfo("Payment info is not accessible by {0}.", user);
                 }
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#updateProductRelease(com.thinkparity.codebase.model.migrator.Product,
      *      com.thinkparity.codebase.model.migrator.Release)
      * 
      */
     public void updateProductRelease(final Product product,
             final Release release) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_UPDATE_PRODUCT_RELEASE);
         try {
             beginXA(xaContext);
             try {
                 final InternalMigratorModel migratorModel = getMigratorModel();
                 final Product localProduct = migratorModel.readProduct(product
                         .getName());
                 final Release localRelease = migratorModel.readRelease(
                         localProduct.getName(), release.getName(), release
                                 .getOs());
 
                 userSql.updateProductRelease(user, localProduct, localRelease);
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.profile.ProfileModel#verifyEmail(com.thinkparity.codebase.jabber.JabberId,
      *      com.thinkparity.codebase.email.EMail, java.lang.String)
      * 
      */
     public void verifyEMail(final EMail email, final String key) {
         final Object xaContext = newXAContext(XAContextId.PROFILE_VERIFY_EMAIL);
         try {
             beginXA(xaContext);
             try {
                 final VerificationKey verifiedKey = VerificationKey.create(key);
                 final EMail verifiedEmail = userSql.readEmail(
                         user.getLocalId(), email, verifiedKey);
                 Assert.assertNotNull("VERIFICATION KEY INCORRECT",
                         verifiedEmail);
                 Assert.assertTrue("VERIFICATION KEY INCORRECT", email
                         .equals(verifiedEmail));
                 userSql.verifyEmail(user.getLocalId(), verifiedEmail,
                         verifiedKey);
                 // create invitations
                 final InternalContactModel contactModel = getContactModel();
                 final List<OutgoingEMailInvitation> invitations = contactModel
                         .readOutgoingEMailInvitations(user.getId(), email);
                 IncomingEMailInvitation incomingInvitation;
                 for (final OutgoingEMailInvitation invitation : invitations) {
                     incomingInvitation = new IncomingEMailInvitation();
                     incomingInvitation.setCreatedBy(invitation.getCreatedBy());
                     incomingInvitation.setCreatedOn(invitation.getCreatedOn());
                     incomingInvitation.setExtendedBy(incomingInvitation
                             .getCreatedBy());
                     incomingInvitation.setInvitationEMail(email);
                     contactModel.createInvitation(user.getId(), invitation
                             .getCreatedBy().getId(), incomingInvitation);
                 }
                 // fire event
                 notifyContactUpdated();
                 // flush event queue
                 getQueueModel().flush();
             } catch (final Throwable t) {
                 rollbackXA();
                 throw t;
             } finally {
                 completeXA(xaContext);
             }
         } catch (final Throwable t) {
             throw translateError(t);
         }
     }
 
     /**
      * @see com.thinkparity.desdemona.model.AbstractModelImpl#initialize()
      * 
      */
     @Override
     protected void initialize() {
         this.transactionManager = TransactionManager.getInstance();
 
         this.contactSql = new ContactSql(transactionManager.getDataSource());
         this.userSql = new UserSql(transactionManager.getDataSource());
         this.paymentSql = new PaymentSql(transactionManager.getDataSource());
         this.queueSql = new QueueSql(transactionManager.getDataSource());
     }
 
     /**
      * Begin the transaction.
      * 
      * @param transaction
      *            A <code>Transaction</code>.
      * @throws NotSupportedException
      * @throws SystemException
      */
     void beginXA(final Object xaContext) throws NotSupportedException,
             SystemException {
         if (null == transaction) {
             this.transactionContext = xaContext;
             this.transaction = transactionManager.getTransaction();
             this.transaction.begin();
         } else {
             throw new NotSupportedException(
                     MessageFormat.format("Nested transactions not supported.  {0}",
                     transactionContext));
         }
     }
 
     /**
      * Complete the transaction. If the transaction context matches the local
      * context; the transaction will either be rolled back or completed.
      * 
      * @param xaContext
      *            An <code>Object</code>.
      * @throws SystemException
      * @throws HeuristicMixedException
      * @throws HeuristicRollbackException
      * @throws RollbackException
      * @throws SystemException
      */
     void completeXA(final Object xaContext) throws SystemException,
             HeuristicMixedException, HeuristicRollbackException,
             RollbackException, SystemException {
         if (transactionContext.equals(xaContext)) {
             try {
                 if (transaction.isRollbackOnly()) {
                     transaction.rollback();
                 } else {
                     transaction.commit();
                 }
             } finally {
                 transaction = null;
             }
         }
     }
 
     /**
      * Create an event for a user list.
      * 
      * @param userList
      *            A <code>List<User></code>.
      * @param event
      *            A <code>XMPPEvent</code>.
      */
     void createEvent(final List<Profile> profileList, final XMPPEvent event,
             final Calendar eventDate) {
         event.setDate(eventDate);
         event.setPriority(XMPPEvent.Priority.NORMAL);
         for (final Profile profile : profileList) {
             event.setId(buildUserTimestampId(profile.getId()));
             queueSql.createEvent(profile.getLocalId(), event,
                     isEventFiltered(event));
             getQueueModel(profile).flush();
         }
     }
 
     /**
      * Create an event for a user.
      * 
      * @param user
      *            A <code>User</code>.
      * @param event
      *            A <code>XMPPEvent</code>.
      */
     void createEvent(final User user, final XMPPEvent event,
             final Calendar eventDate) {
         event.setDate(eventDate);
         event.setId(buildUserTimestampId(user.getId()));
         event.setPriority(XMPPEvent.Priority.NORMAL);
         queueSql.createEvent(user.getLocalId(), event,
                 isEventFiltered(event));
         getQueueModel(user).flush();
     }
 
     /**
      * Enqueue a payment failed event to the payment plan's owner.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param succeededOn
      *            A <code>Calendar</code>.
      */
     void enqueuePaymentSucceeded(final PaymentPlan plan,
             final Calendar succeededOn) {
         final PaymentEvent event = new PaymentEvent();
         event.setSuccess(Boolean.TRUE);
         event.setPaymentOn(succeededOn);
         createEvent(plan.getOwner(), event, succeededOn);
     }
 
     /**
      * Obtain the node service.
      * 
      * @return A <code>NodeService</code>.
      */
     NodeService getNodeService() {
         return nodeService;
     }
 
     /**
      * Obtain the payment sql implementation.
      * 
      * @return A <code>PaymentSql</code>.
      */
     PaymentSql getPaymentSql() {
         return paymentSql;
     }
 
     /**
      * Obtain the user sql implementation.
      * 
      * @return A <code>UserSql<code>.
      */
     UserSql getUserSql() {
         return userSql;
     }
 
     /**
      * Obtain a reference to a new transaction context.
      * 
      * @return An <code>Object</code>.
      */
     Object newXAContext(final XAContextId contextId) {
         return new Object() {
             /**
              * @see java.lang.Object#toString()
              *
              */
             @Override
             public String toString() {
                 return contextId.name();
             }
         };
     }
 
     /**
      * If a card is charged we update the plan arrears flag; create a payment
      * succeeded event as well as plan out of arrears events and enqueue them to
      * the plan's owner and the plan members respectively.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      */
     void notifyComplete(final PaymentPlan plan, final Calendar completedOn) {
         final List<Profile> profileList = readPaymentPlanProfiles(plan);
         updateProfilesActive(profileList, Boolean.TRUE);
         updatePlanArrears(plan, Boolean.FALSE);
         enqueuePaymentSucceeded(plan, completedOn);
         enqueuePlanOutOfArrears(profileList, completedOn);
     }
 
     /**
      * Read the features for a user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List<Feature></code>.
      */
     List<Feature> readFeatures(final User user) {
         return userSql.readFeatures(user);
     }
 
     /**
      * Read all profiles attached to a payment plan.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @return A <code>List<Profile></code>.
      */
     List<Profile> readPaymentPlanProfiles(final PaymentPlan plan) {
         final List<User> userList = paymentSql.readPlanUsers(plan);
         final List<Profile> profileList = new ArrayList<Profile>(userList.size());
         Profile profile;
         for (final User user : userList) {
             profile = new Profile();
             profile.setVCard(getUserModel(user).readVCard(new ProfileVCard()));
             profile.setFeatures(userSql.readProductFeatures(user, Ophelia.PRODUCT_NAME));
             inject(profile, getUserModel(user).read(user.getId()));
             profile.setActive(userSql.isActive(user));
             profileList.add(profile);
         }
         return profileList;
     }
 
     /**
      * Read the payment provider for the payment info.
      * 
      * @param paymentInfo
      *            A <code>PaymentInfo</code>.
      * @return A <code>PaymentProvider</code>.
      */
     PaymentProvider readPaymentProvider(final PaymentInfo paymentInfo) {
         return PaymentProviderFactory.createInstance(
                 paymentSql.readProviderClass("Moneris"),
                 paymentSql.readProviderConfiguration("Moneris"));
     }
 
     /**
      * Mark the transaction as rollback only. The actual rollback will be
      * executed by {@link #completeXA(Transaction)}.
      * 
      * @throws SystemException
      */
     void rollbackXA() throws SystemException {
         transaction.setRollbackOnly();
     }
 
     /**
      * Update the plan's arrears.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param arrears
      *            A <code>Boolean</code>.
      */
     void updatePlanArrears(final PaymentPlan plan, final Boolean arrears) {
         plan.setArrears(arrears);
         paymentSql.updatePlanArrears(plan);
     }
 
     /**
      * Set the profiles' active.
      * 
      * @param profileList
      *            A <code>List<Profile></code>.
      * @param active
      *            A <code>Boolean</code>.
      */
     void updateProfilesActive(final List<Profile> profileList,
             final Boolean active) {
         for (final Profile profile : profileList) {
             profile.setActive(active);
         }
         userSql.updateActive(profileList);
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
         Assert.assertTrue(found,
                 "Profile field {0} contains invalid value {1}.", name, value);
     }
 
     private void assertIsValidTimeZone(final String name, final String value) {
         final TimeZone timeZone = TimeZone.getTimeZone(value);
         Assert.assertTrue(timeZone.getID().equals(value),
                 "Profile field {0} contains invalid value {1}.", name, value);
     }
 
     /**
      * Create a profile.
      * 
      * @param product
      *            A <code>Product</code>.
      * @param release
      *            A <code>Release</code>.
      * @param usernameReservation
      *            A <code>UsernameReservation</code>.
      * @param emailReservation
      *            A <code>EMailReservation</code>.
      * @param credentials
      *            A set of <code>Credentials</code>.
      * @param profile
      *            A <code>Profile</code>.
      * @param email
      *            An <code>EMail</code> address.
      * @param securityCredentials
      *            A set of <code>SecurityCredentials</code>.
      * @param paymentPlan
      *            A <code>PaymentPlan</code>.
      * @return A <code>VerificationKey</code>.
      * @throws GeneralSecurityException
      * @throws IOException
      * @throws MessagingException
      */
     private VerificationKey create(final Calendar createdOn,
             final Product product, final Release release,
             final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final Profile profile,
             final EMail email, final SecurityCredentials securityCredentials)
             throws GeneralSecurityException, IOException {
         validate(usernameReservation, emailReservation, credentials, email);
 
         // delete expired reservations
         userSql.deleteReservations(currentDateTime());
 
         // ensure the reservations exist
         Assert.assertTrue(userSql
                 .doesExistUsernameReservation(usernameReservation.getToken()),
                 "Username reservation {0} expired on {1}.", usernameReservation
                         .getToken(), usernameReservation.getExpiresOn());
         Assert.assertTrue(userSql.doesExistEMailReservation(emailReservation
                 .getToken()), "E-mail address reservation {0} expired on {1}.",
                 emailReservation.getToken(), emailReservation.getExpiresOn());
 
         profile.setActive(Boolean.FALSE);
         profile.setLocalId(userSql.create(profile.isActive(), Boolean.FALSE,
                 encryptPassword(credentials),
                 encryptAnswer(securityCredentials), profile.getVCard(),
                 new VCardWriter<ProfileVCard>() {
                     public void write(final ProfileVCard vcard,
                             final Writer writer) throws IOException {
                         final StringWriter stringWriter = new StringWriter();
                         XSTREAM_UTIL.toXML(vcard, stringWriter);
                         try {
                             writer.write(encrypt(stringWriter.toString()));
                         } catch (final GeneralSecurityException gsx) {
                             logger.logError(gsx, "Could not encrypt vcard.");
                             throw new IOException(gsx);
                         }
                     }
                 }, createdOn));
         profile.setId(JabberIdBuilder.parseUsername(credentials.getUsername()));
         // add e-mail address
         final VerificationKey key = VerificationKey.generate(email);
         userSql.createEmail(profile.getLocalId(), email, key);
 
         // add features
         final InternalMigratorModel migratorModel = getMigratorModel();
         final Product localProduct = migratorModel.readProduct(product
                 .getName());
         Feature localFeature;
         for (final Feature feature : profile.getFeatures()) {
             localFeature = migratorModel.readProductFeature(localProduct,
                     feature.getName());
             userSql.createFeature(profile, localFeature);
         }
 
         // set release
         final Release localRelease = migratorModel.readRelease(localProduct
                 .getName(), release.getName(), release.getOs());
         userSql.createProductRelease(profile, localProduct, localRelease);
 
         // remove username reservation
         userSql.deleteUsernameReservation(usernameReservation.getToken());
         userSql.deleteEMailReservation(emailReservation.getToken());
 
         // add support contact
         final InternalUserModel userModel = getUserModel();
         final User support = userModel.read(User.THINKPARITY_SUPPORT.getId());
         contactSql.create(profile, support, profile, createdOn);
         contactSql.create(support, profile, profile, createdOn);
 
         return key;
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
     private <D extends Delegate<ProfileModelImpl>> D createDelegate(
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
      * Create a verification message and attach it to the mime message.
      * 
      * @param mimeMessage
      *            A <code>MimeMessage</code>.
      * @param email
      *            An <code>EMail</code>.
      * @param key
      *            A <code>VerificationKey</code>.
      * @throws MessagingException
      */
     private void createFirstVerification(final MimeMessage mimeMessage,
             final EMail email, final VerificationKey key)
             throws MessagingException {
         final FirstVerificationText text = new FirstVerificationText(Locale
                 .getDefault(), email, key);
         mimeMessage.setSubject(text.getSubject());
 
         final MimeBodyPart verificationBody = new MimeBodyPart();
         verificationBody.setContent(text.getBody(), text.getBodyType());
 
         final Multipart verification = new MimeMultipart();
         verification.addBodyPart(verificationBody);
         mimeMessage.setContent(verification);
     }
 
     /**
      * Create a payment plan.
      * 
      * @param paymentInfo
      *            A <code>PaymentInfo</code>.
      * @param createdBy
      *            A <code>User</code>.
      * @param createdOn
      *            A <code>Calendar</code>.
      */
     private void createPaymentPlan(final Currency currency,
             final PaymentInfo paymentInfo, final User createdBy,
             final Calendar createdOn) {
         final PaymentPlan paymentPlan = newPaymentPlan(currency, createdOn,
                 createdBy);
         paymentSql.createPlan(paymentPlan);
         paymentSql.createInfo(paymentPlan, readPaymentProvider(paymentInfo),
                 paymentInfo);
         userSql.createPaymentPlan(createdBy, paymentPlan);
     }
 
     /**
      * Create a verification message and attach it to the mime message.
      * 
      * @param mimeMessage
      *            A <code>MimeMessage</code>.
      * @param email
      *            An <code>EMail</code>.
      * @param key
      *            A <code>VerificationKey</code>.
      * @throws MessagingException
      */
     private void createVerification(final MimeMessage mimeMessage,
             final EMail email, final VerificationKey key)
             throws MessagingException {
         final VerificationText text = new VerificationText(Locale.getDefault(),
                 email, key);
         mimeMessage.setSubject(text.getSubject());
 
         final MimeBodyPart verificationBody = new MimeBodyPart();
         verificationBody.setContent(text.getBody(), text.getBodyType());
 
         final Multipart verification = new MimeMultipart();
         verification.addBodyPart(verificationBody);
         mimeMessage.setContent(verification);
     }
 
     /**
      * Encrypt the security answer within the credentials.
      * 
      * @param credentials
      *            The <code>Credentials</code>.
      * @return The <code>Credentials</code>.
      */
     private SecurityCredentials encryptAnswer(
             final SecurityCredentials credentials) throws BadPaddingException,
             IOException, IllegalBlockSizeException, InvalidKeyException,
             NoSuchAlgorithmException, NoSuchPaddingException {
         credentials.setAnswer(encrypt(credentials.getAnswer()));
         return credentials;
     }
 
     /**
      * Enqueue a payment in arrears event to the payment plan's users.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      */
     private void enqueuePlanOutOfArrears(final List<Profile> profileList,
             final Calendar outOfArrearsOn) {
         final PaymentPlanArrearsEvent event = new PaymentPlanArrearsEvent();
         event.setArrearsOn(outOfArrearsOn);
         event.setInArrears(Boolean.FALSE);
         createEvent(profileList, event, outOfArrearsOn);
     }
 
     /**
      * Determine if the payment info is accessible by the model user.
      * 
      * @param xaContext
      *            An <code>Object</code>.
      * @return True if it is accessible.
      */
     private boolean isAccessiblePaymentInfo(final Object xaContext) {
         final PaymentPlan plan = paymentSql.readPlan(user);
        return null == plan ? false : plan.getOwner().getLocalId().equals(
                user.getLocalId());
     }
 
     /**
      * Determine if the event is filtered.
      * 
      * @param event
      *            An <code>XMPPEvent</code>.
      * @return True if it is annotated with a filter.
      */
     private Boolean isEventFiltered(final XMPPEvent event) {
         return event.getClass().isAnnotationPresent(ThinkParityFilterEvent.class);
     }
 
     /**
      * Localize a product.
      * 
      * @param product
      *            A <code>Product</code>.
      * @return A <code>Product</code>.
      */
     private Product localize(final Product product) {
         return getMigratorModel().readProduct(product.getName());
     }
 
     /**
      * Localize the features.
      * 
      * @param product
      *            A <code>Product</code>.
      * @param features
      *            A <code>List<Feature></code>.
      * @return A <code>List<Feature></code>.
      */
     private List<Feature> localize(final Product product,
             final List<Feature> features) {
         final List<Feature> localFeatures = getMigratorModel()
                 .readProductFeatures(product.getName());
         final Iterator<Feature> iLocalFeatures = localFeatures.iterator();
         Feature localFeature;
         boolean found;
         while (iLocalFeatures.hasNext()) {
             localFeature = iLocalFeatures.next();
             found = false;
             for (final Feature feature : features) {
                 if (feature.getName().equals(localFeature.getName())) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 iLocalFeatures.remove();
             }
         }
         return localFeatures;
     }
 
     /**
      * Create an instance of a payment plan.
      * 
      * @return A <code>PaymentPlan</code>.
      */
     private PaymentPlan newPaymentPlan(final Currency currency,
             final Calendar createdOn, final User owner) {
         final PaymentPlan plan = new PaymentPlan();
         plan.setArrears(Boolean.TRUE);
         plan.setBillable(Boolean.TRUE);
         plan.setCurrency(currency);
         plan.setInvoicePeriod(InvoicePeriod.MONTH);
         final int invoicePeriodOffset;
         switch (plan.getInvoicePeriod()) {
         case MONTH:
             invoicePeriodOffset = createdOn.get(Calendar.DAY_OF_MONTH);
             break;
         default:
             throw Assert.createUnreachable("Unknown invoice period {0}.",
                     plan.getInvoicePeriod());
         }
         plan.setInvoicePeriodOffset(invoicePeriodOffset);
         plan.setName(MessageFormat.format("{0} - {1,date,yyyy.MM}",
                 owner.getOrganization(), createdOn.getTime()));
         plan.setOwner(owner);
         plan.setPassword(null);
         plan.setUniqueId(UUIDGenerator.nextUUID());
         return plan;
     }
 
     /**
      * Create a new token.
      * 
      * @return A <code>Token</code>.
      */
     private Token newToken() {
         final Token token = new Token();
         token.setValue(MD5Util.md5Base64(String.valueOf(currentDateTime()
                 .getTimeInMillis())));
         return token;
     }
 
     /**
      * Fire the contact updated event for a user. All contacts for that user
      * will be updated.
      * 
      * @param userId
      *            A user id.
      * @throws SQLException
      */
     private void notifyContactUpdated() {
         final List<JabberId> contactIds = contactSql.readIds(user.getLocalId());
         final ContactUpdatedEvent contactUpdated = new ContactUpdatedEvent();
         contactUpdated.setContactId(user.getId());
         contactUpdated.setUpdatedOn(currentDateTime());
         enqueueEvents(contactIds, contactUpdated);
     }
 
     /**
      * Read a profile within a transactional context.
      * 
      * @param xaContext
      *            An <code>Object</code>.
      * @return A <code>Profile</code>.
      */
     private Profile read(final Object xaContext) {
         final Profile profile = new Profile();
         profile.setVCard(getUserModel(user).readVCard(
                 new ProfileVCard()));
         profile.setFeatures(userSql.readProductFeatures(user, Ophelia.PRODUCT_NAME));
         inject(profile, user);
         profile.setActive(userSql.isActive(user));
         return profile;
     }
 
     /**
      * Read the profile's e-mail addresses.
      * 
      * @return A <code>List<ProfileEMail></code>.
      */
     private List<ProfileEMail> readEMails() {
         return userSql.readEMails(user);
     }
 
     /**
      * Determine whether or not payment is required for a feature.
      * 
      * @param feature
      *            A <code>Feature</code>.
      * @return True if payment is required.
      */
     private Boolean readIsPaymentRequired(final Feature feature) {
         return getMigratorModel().readIsPaymentRequired(feature);
     }
 
     /**
      * Read the verified e-mail addresses for a user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List<EMail></code>.
      */
     private List<EMail> readVerifiedEMails(final User user) {
         // read only verified e-mails
         final List<ProfileEMail> profileEMails = userSql.readEMails(user);
         final List<EMail> emails = new ArrayList<EMail>(profileEMails.size());
         for (final ProfileEMail profileEMail : profileEMails) {
             if (profileEMail.isVerified())
                 emails.add(profileEMail.getEmail());
         }
         return emails;
     }
 
     /**
      * Validate a profile.
      * 
      * @param profile
      *            A <code>Profile</code>.
      */
     private void validate(final Product product, final Release release,
             final Profile profile) {
         final List<Feature> localFeatures = localize(localize(product),
                 profile.getFeatures());
 
         for (final Feature localFeature : localFeatures) {
             Assert.assertNotTrue(readIsPaymentRequired(localFeature),
                     "Payment required for {0}.", localFeature.getName());
         }
 
         validate(profile.getVCard());
     }
 
     private void validate(final ProfileVCard vcard) {
         assertIsSet("country", vcard.getCountry());
         assertIsValidCountry("country", vcard.getCountry());
         assertIsSet("language", vcard.getLanguage());
         assertIsValidLanguage("language", vcard.getLanguage());
         assertIsSet("name", vcard.getName());
         assertIsSet("organization", vcard.getOrganization());
         assertIsSet("organization country", vcard.getOrganizationCountry());
         assertIsValidCountry("organization country", vcard
                 .getOrganizationCountry());
         assertIsSet("timeZone", vcard.getTimeZone());
         assertIsValidTimeZone("timeZone", vcard.getTimeZone());
         assertIsSet("title", vcard.getTitle());
     }
 
     /**
      * Validate the reservations against the credentials and e-mail.
      * 
      * @param usernameReservation
      *            A <code>UsernameReservation</code>.
      * @param emailReservation
      *            An <code>EMailReservation</code>.
      * @param credentials
      *            A set of <code>Credentials</code>.
      * @param email
      *            An <code>EMail</code>.
      */
     private void validate(final UsernameReservation usernameReservation,
             final EMailReservation emailReservation,
             final Credentials credentials, final EMail email) {
         Assert.assertTrue(usernameReservation.getUsername().equals(
                 credentials.getUsername()),
                 "Reservation username {0} does not match credentials username {1}.",
                 usernameReservation.getUsername(), credentials.getUsername());
         Assert.assertTrue(emailReservation.getEMail().equals(email),
                 "Reservation e-mail address {0} does not match e-mail address {1}.",
                 emailReservation.getEMail(), email);
     }
 
     /**
      * Verify the creation by sending an e-mail message.
      * 
      * @param email
      *            An <code>EMail</code>.
      * @param key
      *            A <code>VerificationKey</code>.
      * @throws MessagingException
      * @throws UnsupportedEncodingException
      */
     private void verifyCreate(final EMail email, final VerificationKey key)
             throws MessagingException, UnsupportedEncodingException {
         // send verification email
         final MimeMessage mimeMessage = smtpService.createMessage();
         createFirstVerification(mimeMessage, email, key);
         addRecipient(mimeMessage, email);
         final InternetAddress fromInternetAddress = new InternetAddress();
         fromInternetAddress.setAddress(Constants.Internet.Mail.FROM_ADDRESS);
         fromInternetAddress.setPersonal(Constants.Internet.Mail.FROM_PERSONAL);
         mimeMessage.setFrom(fromInternetAddress);
         smtpService.deliver(mimeMessage);
     }
 
     /**
      * Wake up the payment service.
      * 
      */
     private void wakePaymentService() {
         paymentService.wake();
     }
 
     /** <b>Title:</b>XA Context Id<br> */
     public enum XAContextId {
         PROFILE_CREATE, PROFILE_CREATE_EMAIL_RESERVATION,
         PROFILE_CREATE_INVOICE, PROFILE_CREATE_TOKEN,
 
         PROFILE_IS_ACCESSIBLE_PAYMENT_INFO, PROFILE_IS_EMAIL_AVAILABLE,
         PROFILE_IS_QUEUE_READABLE, PROFILE_IS_REQUIRED_PAYMENT_INFO,
         PROFILE_IS_SET_PAYMENT_INFO,
 
         PROFILE_LOCK_INVOICES, PROFILE_LOCK_PLANS,
 
         PROFILE_PROCESS_PAYMENT,
 
         PROFILE_READ, PROFILE_READ_EMAIL, PROFILE_READ_EMAILS,
         PROFILE_READ_FEATURES, PROFILE_READ_LOCKED_INVOICES, PROFILE_READ_TOKEN,
 
         PROFILE_UNLOCK_PLANS,
 
         PROFILE_UPDATE, PROFILE_UPDATE_EMAIL, PROFILE_UPDATE_PASSWORD,
         PROFILE_UPDATE_PAYMENT_INFO, PROFILE_UPDATE_PRODUCT_RELEASE,
         PROFILE_USERNAME_RESERVATION_CREATE,
 
         PROFILE_VERIFY_EMAIL
     }
 }
