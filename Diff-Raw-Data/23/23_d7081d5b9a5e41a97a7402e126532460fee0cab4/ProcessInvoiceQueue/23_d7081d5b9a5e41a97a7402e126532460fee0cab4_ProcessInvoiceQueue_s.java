 /*
  * Created On:  26-Sep-07 1:48:09 PM
  */
 package com.thinkparity.desdemona.model.profile.delegate;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.desdemona.model.Constants.Invoicing;
 import com.thinkparity.desdemona.model.migrator.Fee;
 import com.thinkparity.desdemona.model.node.Node;
 import com.thinkparity.desdemona.model.profile.ProfileDelegate;
 import com.thinkparity.desdemona.model.profile.ProfileModelImpl.XAContextId;
 import com.thinkparity.desdemona.model.profile.payment.Invoice;
 import com.thinkparity.desdemona.model.profile.payment.InvoiceItem;
 import com.thinkparity.desdemona.model.profile.payment.PaymentPlan;
 import com.thinkparity.desdemona.model.profile.payment.PaymentPlan.InvoicePeriod;
 import com.thinkparity.desdemona.util.DateTimeProvider;
 
 /**
  * <b>Title:</b>thinkParity Desdemona Model Process Payment Queue Profile
  * Delegate<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class ProcessInvoiceQueue extends ProfileDelegate {
 
     /** A limit to the number of plans to lock. */
     private static final int LOCK_LIMIT;
 
     /** The number of plans locked. */
     private static int locked;
 
     /** A previously locked plan id. */
     private static long lockedPlanId;
 
     static {
         LOCK_LIMIT = 100;
         locked = 0;
         lockedPlanId = -1;
     }
 
     /** The current date/time. */
     private Calendar currentDateTime;
 
     /** A server node. */
     private Node node;
 
     /**
      * Create ProcessPayment.
      *
      */
     public ProcessInvoiceQueue() {
         super();
     }
 
     /**
      * Reset the locked plan id.
      * 
      */
     private void reset() {
         lockedPlanId = -1;
     }
 
     /**
      * Process the invoice queue.
      * 
      */
     public void processInvoiceQueue() throws NotSupportedException,
             SystemException, RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         this.currentDateTime = DateTimeProvider.getCurrentDateTime();
         final List<PaymentPlan> planList = new ArrayList<PaymentPlan>(LOCK_LIMIT);
         reset();
 
         /* lock */
         lockPlans();
         try {
             /* read locked */
             planList.addAll(readLockedPlans());
             while (0 < planList.size()) {
                 try {
                     /* process */
                     for (final PaymentPlan plan : planList) {
                         lockedPlanId = plan.getId();
                         logger.logVariable("lockedPlanId", lockedPlanId);
                         logger.logVariable("plan", plan);
                         if (plan.isBillable()) {
                             /* create invoice */
                             createInvoice(plan);
                         } else {
                             /* activate users */
                             notifyComplete(plan, currentDateTime);
                         }
                     }
                 } finally {
                     /* unlock */
                     unlockPlans();
                 }
                 /* lock */
                 lockPlans();
                 /* read locked */
                 planList.retainAll(readLockedPlans());
             }
         } finally {
             /* unlock */
             unlockPlans();
         }
         /* wake up the payment processor */
         wakePaymentProcessor();
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
      * Calculate the invoice day of the month. Given the date and an offset an
      * appropriate day of the month is returned. If the offset exceeds the
      * maximum day of the month; the maximum is used. For example if the offset
      * is 31 and the month is february (a maximum of 28) 28 will be used.
      * 
      * @param date
      *            A <code>Calendar</code>.
      * @param offset
      *            An <code>Integer</code>.
      * @return An <code>Integer</code>.
      */
     private Integer calculateDayOfMonth(final Calendar date,
             final Integer offset) {
         final int dayOfMonthMax = date.getMaximum(Calendar.DAY_OF_MONTH);
         int dayOfMonth = offset;
         if (dayOfMonthMax < dayOfMonth) {
             dayOfMonth = dayOfMonthMax;
         }
         return Integer.valueOf(dayOfMonth);
     }
 
     /**
      * Create the first payment invoice for a payment plan.
      * 
      * @param paymentPlan
      *            A <code>PaymentPlan</code>.
      */
     private void createFirstInvoice(final PaymentPlan plan) {
         /*
          * the invoice date is based upon the payment plan's invoice period; the
          * current date/time; and the the payment plan's invoice period offset
          */
         final InvoicePeriod invoicePeriod = plan.getInvoicePeriod();
         final Calendar now = DateTimeProvider.getCurrentDateTime();
         final Calendar invoiceDate = (Calendar) now.clone();
         invoiceDate.set(Calendar.MILLISECOND, 0);
         invoiceDate.set(Calendar.SECOND, 0);
         invoiceDate.set(Calendar.HOUR, 0);
         switch (invoicePeriod) {
         case MONTH:
             /*
              * set the invoice date to be the current date
              * 
              * if the day of the month specified by the period offset is greater
              * than the day of the month in the invoice date (ie the offset is
              * 31 and the month is february) then we use the maximum for the
              * given month
              * 
              * similarly if we are in december we need to roll-over the month
              * and increment the year
              */
             final int dayOfMonth = calculateDayOfMonth(invoiceDate,
                     plan.getInvoicePeriodOffset());
             final int month = invoiceDate.get(Calendar.MONTH);
             final int year = invoiceDate.get(Calendar.YEAR);
 
             invoiceDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
             invoiceDate.set(Calendar.MONTH, month);
             invoiceDate.set(Calendar.YEAR, year);
 
             final Invoice invoice = new Invoice();
             invoice.setDate(invoiceDate);
             invoice.setNumber(Invoicing.START);
             invoice.setRetry(Boolean.TRUE);
             paymentSql.createInvoice(plan, invoice, newFirstItemList(plan));
             logger.logVariable("invoice", invoice);
             break;
         default:
             throw Assert.createUnreachable("Unknown invoice period {0}.",
                     invoicePeriod);
         }
     }
 
     /**
      * Create a payment invoice for a payment plan. The invoice is based upon
      * the current date and the previous invoice. No gaps in invoicing will be
      * filled out by this automation.
      * 
      * @param paymentPlan
      *            A <code>PaymentPlan</code>.
      */
     private void createInvoice(final PaymentPlan plan)
             throws NotSupportedException, SystemException, RollbackException,
             HeuristicRollbackException, HeuristicMixedException, Throwable {
         final Object xaContext = newXAContext(XAContextId.PROFILE_CREATE_INVOICE);
         beginXA(xaContext);
         try {
             final Invoice latestInvoice = paymentSql.readLatestInvoice(plan);
             if (null == latestInvoice) {
                 logger.logTraceId();
                 createFirstInvoice(plan);
             } else {
                 logger.logTraceId();
                 createInvoice(plan, latestInvoice);
             }
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 
     /**
      * Create an invoice.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param latestInvoice
      *            An <code>Invoice</code>.
      */
     private void createInvoice(final PaymentPlan plan, final Invoice latestInvoice) {
         /*
          * the invoice date is based upon the payment plan's invoice period and
          * the latest invoice date
          */
         final InvoicePeriod invoicePeriod = plan.getInvoicePeriod();
         final Calendar invoiceDate = (Calendar) currentDateTime.clone();
         invoiceDate.set(Calendar.MILLISECOND, 0);
         invoiceDate.set(Calendar.MINUTE, 0);
         invoiceDate.set(Calendar.SECOND, 0);
         invoiceDate.set(Calendar.HOUR, 0);
         switch (invoicePeriod) {
         case MONTH:
             /*
              * set the invoice date to be the current date plus a month
              * 
              * if the day of the month specified by the period offset is greater
              * than the day of the month in the invoice date (ie the offset is
              * 31 and the month is february) then we use the maximum for the
              * given month
              * 
              * similarly if we are in december we need to roll-over the month
              * and increment the year
              */
             final int dayOfMonth = calculateDayOfMonth(invoiceDate,
                     plan.getInvoicePeriodOffset());
             final int monthMax = invoiceDate.getMaximum(Calendar.MONTH);
             int month = invoiceDate.get(Calendar.MONTH) + 1;
             int year = invoiceDate.get(Calendar.YEAR);
             if (monthMax < month) {
                 month = invoiceDate.getMinimum(Calendar.MONTH);
                 year++;
             }
             invoiceDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
             invoiceDate.set(Calendar.MONTH, month);
             invoiceDate.set(Calendar.YEAR, year);
 
             if (paymentSql.doesExistInvoice(plan, invoiceDate)) {
                 logger.logTraceId();
                 logger.logInfo("Invoice {0} {1} exists.", plan, invoiceDate);
             } else {
                 logger.logTraceId();
                 final Invoice invoice = new Invoice();
                 invoice.setDate(invoiceDate);
                 invoice.setNumber(latestInvoice.getNumber() + Invoicing.INCREMENT);
                 invoice.setRetry(Boolean.TRUE);
                 paymentSql.createInvoice(plan, invoice, newSubsequentItemList(plan));
             }
             break;
         default:
             throw Assert.createUnreachable("Unknown invoice period {0}.",
                     invoicePeriod);
         }
     }
 
     /**
      * Determine if the invoice period for the payment plan equals the period on
      * the fee.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @param fee
      *            A <code>Fee</code>.
      * @return True if the fee's period is set; and if they are equal.
      */
     private boolean isPeriodEqual(final PaymentPlan plan, final Fee fee) {
         if (fee.isSetPeriod()) {
             return plan.getInvoicePeriod().name().equals(fee.getPeriod().name());
         } else {
             return false;
         }
     }
 
     /**
      * Lock plans for processing.
      * 
      * @throws NotSupportedException
      * @throws SystemException
      * @throws RollbackException
      * @throws HeuristicRollbackException
      * @throws HeuristicMixedException
      * @throws Throwable
      */
     private void lockPlans() throws NotSupportedException, SystemException,
             RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         /* lock plans for processing; and commit the transaction */
         final Object xaContext = newXAContext(XAContextId.PROFILE_LOCK_PLANS);
         beginXA(xaContext);
         try {
             if (-1 == lockedPlanId) {
                 /* initial lock */
                 logger.logTraceId();
                 logger.logInfo("Initial plan lock.");
                 final Long firstId = paymentSql.readFirstLockablePlanId();
                 if (null == firstId) {
                     logger.logInfo("No lockable plans.");
                     return;
                 } else {
                    lockedPlanId = firstId;
                 }
             } else {
                 logger.logTraceId();
                 logger.logInfo("Previous locked plan id {0}.", lockedPlanId);
             }
             /* note that the number of locks obtained is not necessarily
              * equal to the limit requested */
             locked = paymentSql.lockPlans(node, lockedPlanId, LOCK_LIMIT);
             logger.logInfo("{0} plans locked.", locked);
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 
     /**
      * Instantiate a new invoice item list based upon the plan fees.
      * 
      * @return A <code>List<InvoiceItem></code>.
      */
     private List<InvoiceItem> newFirstItemList(final PaymentPlan plan) {
         final List<Profile> planProfileList = readPaymentPlanProfiles(plan);
         final List<InvoiceItem> itemList = new ArrayList<InvoiceItem>();
         final List<Fee> feeList = new ArrayList<Fee>();
         Fee fee;
         int itemNumber = 1;
         for (final Profile planProfile : planProfileList) {
             feeList.clear();
             feeList.addAll(readFees(planProfile));
             InvoiceItem item;
             for (int i = 0; i < feeList.size(); i++) {
                 fee = feeList.get(i);
                 item = new InvoiceItem();
                 item.setAmount(fee.getAmount());
                 item.setDescription(newItemDescription(fee, planProfile));
                 item.setNumber(itemNumber++);
                 itemList.add(item);
             }
         }
         return itemList;
     }
 
     /**
      * Instantiate an item description for a fee for a user.
      * 
      * @param fee
      *            A <code>Fee</code>.
      * @param user
      *            A <code>User</code>.
      * @return A <code>String</code>.
      */
     private String newItemDescription(final Fee fee, final Profile profile) {
         /* the fee description contains a message format pattern with a single
          * variable for the user's name */
         return MessageFormat.format(fee.getDescription(), profile.getName());
     }
 
     /**
      * Instantiate a new invoice item list based upon the plan fees.
      * 
      * @param plan
      *            A <code>PaymentPlan</code>.
      * @return A <code>List<InvoiceItem></code>.
      */
     private List<InvoiceItem> newSubsequentItemList(final PaymentPlan plan) {
         final List<Profile> planProfileList = readPaymentPlanProfiles(plan);
         final List<InvoiceItem> itemList = new ArrayList<InvoiceItem>();
         final List<Fee> feeList = new ArrayList<Fee>();
         Fee fee;
         int itemNumber = 1;
         for (final Profile planProfile : planProfileList) {
             feeList.clear();
             feeList.addAll(readFees(planProfile));
             InvoiceItem item;
             for (int i = 0; i < feeList.size(); i++) {
                 fee = feeList.get(i);
                 if (isPeriodEqual(plan, fee)) {
                     item = new InvoiceItem();
                     item.setAmount(fee.getAmount());
                     item.setDescription(newItemDescription(fee, planProfile));
                     item.setNumber(itemNumber++);
                     itemList.add(item);
                 }
             }
         }
         return itemList;
     }
 
     /**
      * Read the fees for the user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return A <code>List<Fee></code>.
      */
     private List<Fee> readFees(final User user) {
         return getMigratorModel().readFees(readFeatures(user));
     }
 
     /**
      * Obtain a list of all payment plans.
      * 
      * @return A <code>List<PaymentPlan></code>.
      */
     private List<PaymentPlan> readLockedPlans() {
         return paymentSql.readLockedPlans(node);
     }
 
     /**
      * Unlock the plans.
      * 
      */
     private void unlockPlans() throws NotSupportedException, SystemException,
             RollbackException, HeuristicRollbackException,
             HeuristicMixedException, Throwable {
         /* lock plans for processing; and commit the transaction */
         final Object xaContext = newXAContext(XAContextId.PROFILE_UNLOCK_PLANS);
         beginXA(xaContext);
         try {
             paymentSql.unlockPlans(node);
         } catch (final Throwable t) {
             rollbackXA();
             throw t;
         } finally {
             completeXA(xaContext);
         }
     }
 }
