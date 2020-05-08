 /*
  * Created On:  26-Sep-07 1:48:09 PM
  */
 package com.thinkparity.desdemona.model.profile.delegate;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.StackUtil;
 
 import com.thinkparity.codebase.model.migrator.Error;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.profile.payment.PaymentInfo;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentEvent;
 import com.thinkparity.codebase.model.util.xmpp.event.profile.payment.PaymentPlanArrearsEvent;
 
 import com.thinkparity.ophelia.model.util.UUIDGenerator;
 
 import com.thinkparity.desdemona.model.migrator.InternalMigratorModel;
 import com.thinkparity.desdemona.model.node.Node;
 import com.thinkparity.desdemona.model.profile.ProfileDelegate;
 import com.thinkparity.desdemona.model.profile.ProfileModelImpl.XAContextId;
 import com.thinkparity.desdemona.model.profile.payment.*;
 import com.thinkparity.desdemona.model.profile.payment.provider.Client;
 import com.thinkparity.desdemona.model.profile.payment.provider.Info;
 import com.thinkparity.desdemona.model.profile.payment.provider.Payment;
 import com.thinkparity.desdemona.model.profile.payment.provider.PaymentProvider;
 import com.thinkparity.desdemona.model.profile.payment.provider.Transaction;
 import com.thinkparity.desdemona.util.DateTimeProvider;
 
 /**
  * <b>Title:</b>thinkParity Desdemona Model Process Payment Queue Profile
  * Delegate<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class ProcessPaymentQueue extends ProfileDelegate {
 
     /** A limit on the number of invoices to lock. */
     private static final int LOCK_LIMIT;
 
     /** A locked count. */
     private static int locked;
 
     /** A previously locked invoice id. */
     private static long lockedInvoiceId;
 
     static {
         LOCK_LIMIT = 100;
         locked = 0;
         lockedInvoiceId = -1;
     }
 
     /** The current date/time. */
     private Calendar currentDateTime;
 
     /** A node. */
     private Node node;
 
     /**
      * Create ProcessPayment.
      *
      */
     public ProcessPaymentQueue() {
         super();
     }
 
     /**
      * Process the payment queue.
      * 
      */
     public void processPaymentQueue() throws NotSupportedException,
             SystemException, RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         this.currentDateTime = DateTimeProvider.getCurrentDateTime();
         final List<Invoice> invoiceList = new ArrayList<Invoice>(LOCK_LIMIT);
         reset();
 
         /* lock */
         lockInvoices();
         /* read locked */
         invoiceList.addAll(readLockedInvoices());
         while (0 < invoiceList.size()) {
             /* process */
             for (final Invoice invoice : invoiceList) {
                 lockedInvoiceId = invoice.getId();
                 logger.logVariable("lockedInvoiceId", lockedInvoiceId);
                 logger.logVariable("invoice", invoice);
                 processPayment(invoice);
             }
             /* lock */
             lockInvoices();
             /* read locked */
             invoiceList.retainAll(readLockedInvoices());
         }
     }
 
     /**
      * Set the node.
      *
      * @param node
      *		A <code>Node</code>.
      */
     public void setNode(final Node node) {
         this.node = node;
     }
 
     /**
      * Unlock the invoice.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      */
     private void deleteInvoiceLock(final Invoice invoice) {
         paymentSql.deleteInvoiceLock(node, invoice);
     }
 
     /**
      * Enqueue a payment failed event to the payment plan's owner.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      */
     private void enqueuePaymentFailed(final PaymentPlan plan) {
         final PaymentEvent event = new PaymentEvent();
         event.setSuccess(Boolean.FALSE);
         event.setPaymentOn(currentDateTime);
         createEvent(plan.getOwner(), event, currentDateTime);
     }
 
     /**
      * Enqueue a payment in arrears event to the payment plan's users.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      */
     private void enqueuePlanInArrears(final List<Profile> profileList) {
         final PaymentPlanArrearsEvent event = new PaymentPlanArrearsEvent();
         event.setArrearsOn(currentDateTime);
         event.setInArrears(Boolean.TRUE);
         createEvent(profileList, event, currentDateTime);
     }
 
     /**
      * Lock invoices for processing.
      * 
      * @throws NotSupportedException
      * @throws SystemException
      * @throws RollbackException
      * @throws HeuristicRollbackException
      * @throws HeuristicMixedException
      * @throws Throwable
      */
     private void lockInvoices() throws NotSupportedException, SystemException,
             RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         /* lock invoices for processing; and commit the transaction */
         final Object xaContext = newXAContext(XAContextId.PROFILE_LOCK_INVOICES);
         beginXA(xaContext);
         try {
             if (-1 == lockedInvoiceId) {
                 /* initial lock */
                 logger.logTraceId();
                 logger.logInfo("Initial invoice lock.");
                 final Long firstId = paymentSql.readFirstLockableInvoiceId(
                         currentDateTime, Boolean.TRUE);
                 if (null == firstId) {
                     logger.logInfo("No lockable invoices.");
                     return;
                 } else {
                    lockedInvoiceId = firstId;
                 }
             } else {
                 logger.logTraceId();
                 logger.logInfo("Previous locked invoice id {0}.", lockedInvoiceId);
             }
             /* note that the number of locks obtained is not necessarily
              * equal to the limit requested */
             locked = paymentSql.lockInvoices(node, lockedInvoiceId, LOCK_LIMIT,
                     currentDateTime, Boolean.TRUE);
             logger.logInfo("{0} invoices locked.", locked);
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 
     /**
      * Instantiate an instance of provider client.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @return A <code>Client</code>.
      */
     private Client newClient(final PaymentPlan plan) {
         final Client client = new Client();
         client.setId(plan.getUniqueId().toString());
         return client;
     }
 
     /**
      * Instantiate an instance of provider info.
      * 
      * @param paymentInfo
      *            A <code>PaymentInfo</code>.
      * @return A <code>Info</code>.
      */
     private Info newInfo(final PaymentInfo paymentInfo) {
         final Info info = new Info();
         info.setCardExpiryMonth(paymentInfo.getCardExpiryMonth());
         info.setCardExpiryYear(paymentInfo.getCardExpiryYear());
         info.setCardName(paymentInfo.getCardName().name());
         info.setCardNumber(paymentInfo.getCardNumber());
         return info;
     }
 
     /**
      * Instantiate a provider payment for an invoice and its items.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      * @param itemList
      *            A <code>List<InvoiceItem></code>.
      * @return A <code>Payment</code>.
      */
     private Payment newPayment(final Invoice invoice,
             final List<InvoiceItem> itemList) {
         final Payment payment = new Payment();
         long amount = 0L;
         for (final InvoiceItem item : itemList) {
             amount += item.getAmount();
         }
         payment.setAmount(amount);
         return payment;
     }
 
     /**
      * Instantiate an invoice transaction.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      * @return A <code>Transaction</code>.
      */
     private InvoiceTransaction newTransaction(final Invoice invoice) {
         final InvoiceTransaction transaction = new InvoiceTransaction();
         transaction.setDate(currentDateTime);
         transaction.setUniqueId(UUIDGenerator.nextUUID());
         return transaction;
     }
 
     /**
      * Instantiate a provider transaction.
      * 
      * @param transaction
      *            An <code>InvoiceTransaction</code>.
      * @return A <code>Transaction</code>.
      */
     private Transaction newTransaction(final InvoiceTransaction transaction) {
         final Transaction newTransaction = new Transaction();
         newTransaction.setId(transaction.getUniqueId().toString());
         return newTransaction;
     }
 
     /**
      * If a card is declined we create a payment failed event and queue it for
      * the plan's owner.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param invoice
      *            An <code>Invoice</code>.
      */
     private void notifyCardDeclined(final PaymentPlan plan,
             final Invoice invoice) {
         if (plan.isArrears()) {
             logger.logInfo("Plan {0} still in arrears.", plan);
         } else {
             final List<Profile> profileList = readPaymentPlanProfiles(plan);
             updateProfilesActive(profileList, Boolean.FALSE);
             updatePlanArrears(plan, Boolean.TRUE);
             updateInvoiceRetry(invoice, Boolean.FALSE);
             enqueuePaymentFailed(plan);
             enqueuePlanInArrears(readPaymentPlanProfiles(plan));
         }
     }
 
     /**
      * If a card is expired we create a payment failed event and queue it for
      * the plan's owner.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param invoice
      *            An <code>Invoice</code>.
      */
     private void notifyCardExpired(final PaymentPlan plan, final Invoice invoice) {
         if (plan.isArrears()) {
             logger.logInfo("Plan {0} still in arrears.", plan);
         } else {
             final List<Profile> profileList = readPaymentPlanProfiles(plan);
             updateProfilesActive(profileList, Boolean.FALSE);
             updatePlanArrears(plan, Boolean.TRUE);
             updateInvoiceRetry(invoice, Boolean.FALSE);
             enqueuePaymentFailed(plan);
             enqueuePlanInArrears(profileList);
         }
     }
 
     /**
      * Log the error.
      * 
      * @param px
      *            A <code>PaymentException</code>.
      * @param plan
      *            A <code>Plan</code>.
      */
     private void notifySystemError(final PaymentException px,
             final PaymentPlan plan) {
         final User user = userSql.read(User.THINKPARITY.getId());
         final InternalMigratorModel migratorModel = getMigratorModel(user);
         final Product product = migratorModel.readProduct(getProductName());
         final Release release = migratorModel.readLatestRelease(getProductName(), OSUtil.getOS());
         final Error error = new Error();
         error.setArguments(new String[] { plan.toString() });
         error.setMethod(StackUtil.getCallerMethodName());
         error.setOccuredOn(currentDateTime);
         final StringWriter writer = new StringWriter();
         px.printStackTrace(new PrintWriter(writer));
         error.setStack(writer.toString());
         migratorModel.logError(product, release, error);
     }
 
     /**
      * Process an invoice for payment. Being the transaction; read the plan and
      * info; call the external payment provider; update the invoice payment
      * date.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      * @throws NotSupportedException
      * @throws SystemException
      * @throws RollbackException
      * @throws HeuristicRollbackException
      * @throws HeuristicMixedException
      * @throws Throwable
      */
     private void processPayment(final Invoice invoice)
             throws NotSupportedException, SystemException, RollbackException,
             HeuristicRollbackException, HeuristicMixedException, Throwable {
         final Object xaContext = newXAContext(XAContextId.PROFILE_PROCESS_PAYMENT);
         beginXA(xaContext);
         try {
             /* read plan */
             final PaymentPlan plan = paymentSql.readPlan(invoice);
             /* create transaction */
             final InvoiceTransaction transaction = newTransaction(invoice);
             paymentSql.createTransaction(invoice, transaction);
             /* read info/provider/invoice items */
             final PaymentInfo info = paymentSql.readInfo(plan);
             final PaymentProvider provider = readPaymentProvider(info);
             final List<InvoiceItem> itemList = paymentSql.readInvoiceItems(invoice);
             Boolean didComplete = Boolean.FALSE;
             try {
                 if (provider.isOpen()) {
                     logger.logTraceId();
                     provider.setInfo(newInfo(info));
                     provider.setClient(newClient(plan));
                     provider.charge(newTransaction(transaction),
                             newPayment(invoice, itemList));
                     didComplete = Boolean.TRUE;
                 } else {
                     logger.logWarning("Payment provider is closed.");
                 }
             } catch (final CardDeclinedException cdx) {
                 logger.logInfo("Card for plan {0} was declined.", plan);
                 notifyCardDeclined(plan, invoice);
             } catch (final CardExpiredException cex) {
                 logger.logInfo("Card for plan {0} has expired.", plan);
                 notifyCardExpired(plan, invoice);
             } catch (final PaymentException px) {
                 logger.logError(px, "Error processing payment.  {0}:{1}", plan,
                         invoice);
                 notifySystemError(px, plan);
                 throw px;
             } catch (final Exception x) {
                 logger.logError(x, "Could not process payment.");
                 throw x;
             } finally {
                 updateTransaction(transaction, didComplete);
                 if (didComplete.booleanValue()) {
                     /* notify complete */
                     notifyComplete(plan, invoice, currentDateTime);
                     /* update payment date */
                     updatePaymentDate(invoice);
                     /* delete lock */
                     deleteInvoiceLock(invoice);
                 } else {
                     /* unlock */
                     unlockInvoice(invoice);
                 }
             }
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 
     /**
      * Read the locked invoices.
      * 
      * @return A <code>List<Invoice></code>.
      * @throws NotSupportedException
      * @throws SystemException
      * @throws RollbackException
      * @throws HeuristicRollbackException
      * @throws HeuristicMixedException
      * @throws Throwable
      */
     private List<Invoice> readLockedInvoices() throws NotSupportedException,
             SystemException, RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         final Object xaContext = newXAContext(XAContextId.PROFILE_READ_LOCKED_INVOICES);
         beginXA(xaContext);
         try {
             return paymentSql.readLockedInvoices(node);
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 
     /**
      * Reset the locked invoice id.
      */
     private void reset() {
         lockedInvoiceId = -1;
     }
 
     /**
      * Unlock the invoice.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      */
     private void unlockInvoice(final Invoice invoice) {
         paymentSql.unlockInvoice(node, invoice);
     }
 
     /**
      * Update the invoice payment date.
      * 
      * @param invoice
      *            An <code>Invoice</code>.
      */
     private void updatePaymentDate(final Invoice invoice) {
         invoice.setPaymentDate(currentDateTime);
         paymentSql.updateInvoicePaymentDate(invoice);
     }
 
     /**
      * Update the invoice transaction.
      * 
      * @param transaction
      *            An <code>InvoiceTransaction</code>.
      * @param didComplete
      *            A <code>Boolean</code>.
      */
     private void updateTransaction(final InvoiceTransaction transaction,
             final Boolean didComplete) {
         transaction.setSuccess(didComplete);
         paymentSql.updateTransactionSuccess(transaction);
     }
 }
