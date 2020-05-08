 /*
     jbilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2007 Sapienter Billing Software Corp. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.process;
 
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.ejb.CreateException;
 import javax.ejb.EJBException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.ejb.SessionBean;
 import javax.ejb.SessionContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.BillingProcessEntityLocal;
 import com.sapienter.jbilling.interfaces.BillingProcessEntityLocalHome;
 import com.sapienter.jbilling.interfaces.BillingProcessRunEntityLocal;
 import com.sapienter.jbilling.interfaces.BillingProcessSessionLocal;
 import com.sapienter.jbilling.interfaces.BillingProcessSessionLocalHome;
 import com.sapienter.jbilling.interfaces.EntityEntityLocal;
 import com.sapienter.jbilling.interfaces.EntityEntityLocalHome;
 import com.sapienter.jbilling.interfaces.InvoiceEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocalHome;
 import com.sapienter.jbilling.interfaces.PaperInvoiceBatchEntityLocal;
 import com.sapienter.jbilling.interfaces.UserEntityLocal;
 import com.sapienter.jbilling.server.entity.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.entity.BillingProcessDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.InvoiceDTOEx;
 import com.sapienter.jbilling.server.invoice.PaperInvoiceBatchBL;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.payment.event.EndProcessPaymentEvent;
 import com.sapienter.jbilling.server.payment.event.ProcessPaymentEvent;
 import com.sapienter.jbilling.server.process.event.NoNewInvoiceEvent;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 
 /**
  *
  * This is the session facade for the all the billing process and its 
  * related services. 
  *
  * @ejb:bean name="BillingProcessSession"
  *           display-name="The billing process session facade"
  *           type="Stateless"
  *           transaction-type="Container"
  *           view-type="both"
  *           jndi-name="com/sapienter/jbilling/server/process/BillingProcessSession"
  * 
  */
 public class BillingProcessSessionBean implements SessionBean {
 
     Logger log = null;
     SessionContext ctx = null;
 
     /**
     * @ejb:create-method view-type="remote"
     */
     public void ejbCreate() throws CreateException {
     }
 
     /**
      * Gets the invoices for the specified process id. The returned collection
      * is of extended dtos (InvoiceDTOEx).
      * @param processId
      * @return A collection of InvoiceDTOEx objects
      * @throws SessionInternalError
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      */
     public Collection getGeneratedInvoices(Integer processId) 
             throws SessionInternalError {
         Vector invoices = new Vector();
         try {
         
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             // find the billing_process home interface
             BillingProcessEntityLocalHome processHome =
                 (BillingProcessEntityLocalHome) EJBFactory.lookUpLocalHome(
                     BillingProcessEntityLocalHome.class,
                     BillingProcessEntityLocalHome.JNDI_NAME);
             // find the record 
             BillingProcessEntityLocal record = processHome.
                     findByPrimaryKey(processId);
         
             // get the invoices and convert them to DTOs
             Collection records = record.getInvoices();
             for (Iterator it = records.iterator(); it.hasNext();) {
                 InvoiceEntityLocal invoice = (InvoiceEntityLocal) it.next();
                 InvoiceBL invoiceBl = new InvoiceBL(invoice);
                 invoices.add(invoiceBl.getDTOEx(null, false));
             }
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return invoices;
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      * @param entityId
      * @param languageId
      * @return
      * @throws SessionInternalError
      */
     public AgeingDTOEx[] getAgeingSteps(Integer entityId, 
             Integer executorLanguageId, Integer languageId) 
             throws SessionInternalError {
         try {
             AgeingBL ageing = new AgeingBL();
             return ageing.getSteps(entityId, executorLanguageId, languageId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="RequiresNew"
      * @param entityId
      * @param languageId
      * @param steps
      * @throws SessionInternalError
      */
     public void setAgeingSteps(Integer entityId, Integer languageId, 
             AgeingDTOEx[] steps) 
             throws SessionInternalError {
         try {
             AgeingBL ageing = new AgeingBL();
             ageing.setSteps(entityId, languageId, steps);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     public void generateReview(Integer entityId, Date billingDate,
             Integer periodType, Integer periodValue)
             throws SessionInternalError, NamingException, CreateException {
         log.debug("Generating review entity " + entityId);
         // call using JNDI to use CMT
         JNDILookup EJBFactory = JNDILookup.getFactory(false);
         BillingProcessSessionLocalHome localHome = 
             (BillingProcessSessionLocalHome) EJBFactory
                 .lookUpLocalHome(
                    BillingProcessSessionLocalHome.class,
                    BillingProcessSessionLocalHome.JNDI_NAME);
         BillingProcessSessionLocal local = localHome.create();
 
         local.processEntity(entityId, billingDate, periodType, 
                 periodValue, true);
         // let know this entity that a new reivew is now pending approval
         try {
             String params[] = new String[1];
             params[0] = entityId.toString();
             NotificationBL.sendSapienterEmail(entityId, "process.new_review", 
                     null, params);
         } catch (Exception e) {
             log.warn("Exception sending email to entity", e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      * Creates the billing process record. This has to be done in its own
      * transaction (thus, in its own method), so new invoices can link to
      * an existing process record in the db.
      */
     public Integer createProcessRecord(Integer entityId, Date billingDate,
             Integer periodType, Integer periodValue, boolean isReview,
             Integer retries) 
             throws NamingException, CreateException, RemoveException,
                 FinderException, SQLException {
         BillingProcessBL bpBL = new BillingProcessBL();
         BillingProcessDTO dto = new BillingProcessDTO();
 
         // process can't leave reviews behind, and a review has to 
         // delete the previous one too            
         bpBL.purgeReview(entityId, isReview);
         
         dto.setEntityId(entityId);
         dto.setBillingDate(Util.truncateDate(billingDate));
         dto.setPeriodUnitId(periodType);
         dto.setPeriodValue(periodValue);
         dto.setIsReview(isReview ? new Integer(1) : new Integer(0));
         dto.setRetriesToDo(retries);
         
         bpBL.findOrCreate(dto);
         return bpBL.getEntity().getId();
     }
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public Integer createRetryRun(Integer processId) 
             throws NamingException, FinderException, CreateException {
         BillingProcessBL process = new BillingProcessBL(processId);
         // create a new run record
         BillingProcessRunBL runBL = new BillingProcessRunBL();
         runBL.create(process.getEntity().getBillingDate());
         process.getEntity().getRuns().add(runBL.getEntity());
 
         return runBL.getEntity().getId();
     }
     
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public void processEntity(Integer entityId, Date billingDate,
             Integer periodType, Integer periodValue, boolean isReview)
             throws SessionInternalError {
             
         if (entityId == null || billingDate == null) {
             throw new SessionInternalError(
                 "entityId and billingDate " + "can't be null");
         }
 
         try {
             ConfigurationBL conf = new ConfigurationBL(entityId);
 
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             BillingProcessSessionLocalHome localHome = 
                 (BillingProcessSessionLocalHome) EJBFactory
                     .lookUpLocalHome(
                        BillingProcessSessionLocalHome.class,
                        BillingProcessSessionLocalHome.JNDI_NAME);
             BillingProcessSessionLocal local = localHome.create();
             
             Integer billingProcessId = local.createProcessRecord(
                     entityId, billingDate, periodType, periodValue, isReview,
                     conf.getEntity().getRetries());
             
             ///// start processing users of this entity
             Iterator entityUsers;
             int totalInvoices = 0;
             
             boolean onlyRecurring;
             try {
                 // find out parameters from the configuration
                 onlyRecurring = conf.getEntity().getOnlyRecurring().
                         intValue() == 1;
                 // find the entity home interface
                 EntityEntityLocalHome entityHome =
                     (EntityEntityLocalHome) EJBFactory.lookUpLocalHome(
                         EntityEntityLocalHome.class,
                         EntityEntityLocalHome.JNDI_NAME);
 
                 EntityEntityLocal entity = entityHome.findByPrimaryKey(entityId);
                 entityUsers = entity.getUsers().iterator();
 
                 if (!entityUsers.hasNext()) {
                     log.warn("There are no users for the entity id " + entityId);
                 }
             } catch (FinderException e) {
                 log.error("Billing process started for an unexisting entity:" + 
                         entityId, e);
                 throw new SessionInternalError("Entity doesn't exist " + entityId);
             }
             
             log.debug("**** ENTITY " + entityId + " PROCESSING USERS");
             boolean allGood = true;
 
             while (entityUsers.hasNext()) {
                 UserEntityLocal user = (UserEntityLocal) entityUsers.next();
                 Integer result[] = local.processUser(billingProcessId, user.getUserId(), 
                         isReview, onlyRecurring);
                 if (result != null) {
                     log.debug("User " + user.getUserId() + " done invoice generation.");
                     if (!isReview) {
                         for (int f = 0; f < result.length; f++) {
                             local.emailAndPayment(entityId, result[f], 
                                     billingProcessId,  
                                     conf.getEntity().getAutoPayment().intValue() == 1);
                         }
                         log.debug("User " + user.getUserId() + " done email & payment.");
                     }
                     totalInvoices += result.length;
                 } else {
                     log.debug("User " + user.getUserId() + " NOT done");
                     allGood = false;
                 }
             }
             
             if (allGood) { // only if all got well processed
                 // if some of the invoices were paper invoices, a new file with all
                 // of them has to be generated
                 try {
                     BillingProcessBL process = new BillingProcessBL(billingProcessId);
                     PaperInvoiceBatchEntityLocal batch = process.getEntity().getBatch(); 
                     if (totalInvoices > 0 && batch != null) {
                         PaperInvoiceBatchBL batchBl = new PaperInvoiceBatchBL(batch);
                         batchBl.compileInvoiceFilesForProcess(entityId);
                         
                         // send the file as an attachment 
                         batchBl.sendEmail();
                     }
                 } catch (Exception e) {
                     log.error("Error generetaing batch file", e);
                 }
             }
             
             if (allGood) {
                 // now update the billing proces record 
                 BillingProcessRunBL runBL = new BillingProcessRunBL();
                 runBL.setProcess(billingProcessId);
                 runBL.update(billingDate);
                 // the payment processing is happening in parallel
                 // this event marks the end of it
                 EndProcessPaymentEvent event = new EndProcessPaymentEvent(
                         runBL.getEntity().getId(), entityId);
                 EventManager.process(event);
             }
             
             // and finally the next run date in the config
             if (allGood && !isReview) {
                 GregorianCalendar cal = new GregorianCalendar();
                 cal.setTime(billingDate);
                 cal.add(MapPeriodToCalendar.map(periodType),
                         periodValue.intValue());
                 conf.getEntity().setNextRunDate(cal.getTime());
                 log.debug("Updated run date to " + cal.getTime());
             }
 
             log.debug("**** ENTITY " + entityId + " DONE");
         } catch (Exception e) {
             // no need to specify a rollback, an error in any of the
             // updates would not require the rest to be rolled back.
             // Actually, it's better to keep as far as it went.
             log.error("Error processing entity " + entityId, e);
         } 
     }
     
     /**
      * This method process a payment synchronously. It is a wrapper to the payment processing  
      * so it runs in its own transaction
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public void processPayment(Integer processId, Integer runId, Integer invoiceId) {
         try {
             BillingProcessBL bl = new BillingProcessBL();
             bl.generatePayment(processId, runId, invoiceId);
         } catch (Exception e) {
             log.error("Exception processing a payment ", e);
         }
     }
 
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public boolean verifyIsRetry(Integer processId, int retryDays, Date today) 
             throws FinderException, NamingException {
         GregorianCalendar cal = new GregorianCalendar();
         // find the last run date
         BillingProcessBL process = new BillingProcessBL(processId);
         BillingProcessRunBL runBL = new BillingProcessRunBL();
         BillingProcessRunEntityLocal lastRun = (BillingProcessRunEntityLocal) 
                 Collections.max(process.getEntity().getRuns(),
                     runBL.new DateComparator());
         cal.setTime(Util.truncateDate(lastRun.getStarted()));
         log.debug("Retry evaluation lastrun = " + cal.getTime());
         cal.add(GregorianCalendar.DAY_OF_MONTH, retryDays);
         log.debug("Added days = " + cal.getTime() + " today = " + today);
         if (!cal.getTime().after(today)) {
             return true;
         } else {
             return false;
         }
     }
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public void doRetry(Integer processId, int retryDays, Date today) 
             throws SessionInternalError {
         try {
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             BillingProcessSessionLocalHome processHome =
                 (BillingProcessSessionLocalHome) EJBFactory.lookUpLocalHome(
                 BillingProcessSessionLocalHome.class,
                 BillingProcessSessionLocalHome.JNDI_NAME);
             BillingProcessSessionLocal process = processHome.create();
 
             if (process.verifyIsRetry(processId, retryDays, today)) {
                 // it's time for a retry
                 log.debug("Retring process " + processId);
                 Integer runId = process.createRetryRun(processId); 
                 BillingProcessRunBL runBL = new BillingProcessRunBL(runId);
                 Integer entityId = runBL.getEntity().getProcess().getEntityId();
 
                 // get the invoices yet to be paid from this process
                 InvoiceBL invoiceBL = new InvoiceBL();
                 for (Iterator it = invoiceBL.getHome().findProccesableByProcess(
                         processId).iterator(); it.hasNext();) {
                     InvoiceEntityLocal invoice = (InvoiceEntityLocal) it.next();
                     log.debug("Retrying invoice " + invoice.getId());
 
                     // post the need a a payment process, it'll be done asynchronusly
                     ProcessPaymentEvent event = new ProcessPaymentEvent(invoice.getId(), 
                             null, runId, entityId);
                     EventManager.process(event);
                 }
 
                 // update the run record with the results of the run
                 runBL.getEntity().setFinished(Calendar.getInstance().getTime());
                 runBL.getEntity().setInvoiceGenerated(new Integer(0));
                 // the payment processing is happening in parallel
                 // this event marks the end of it
                 EndProcessPaymentEvent event = new EndProcessPaymentEvent(
                         runBL.getEntity().getId(), entityId);
                 EventManager.process(event);
 
                 
                 // update the process: one less retry to do
                 BillingProcessBL bl = new BillingProcessBL(processId);
                 int now = bl.getEntity().getRetriesToDo().intValue();
                 now--;
                 bl.getEntity().setRetriesToDo(new Integer(now));
             }
             
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
     
     /**
      * @ejb:interface-method view-type="local"
      * @ejb.transaction type="RequiresNew"
      */
     public void emailAndPayment(Integer entityId, Integer invoiceId,
             Integer processId, boolean processPayment) {
         try {
             InvoiceBL invoice = new InvoiceBL(invoiceId);
             Integer userId = invoice.getEntity().getUser().getUserId();
  
             log.debug("email and payment for user " + userId + " invoice " +
                     invoiceId);
             // last but not least, let this user know about his/her new
             // invoice.
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
 
             NotificationBL notif = new NotificationBL();
             
             try {
                 MessageDTO[] invoiceMessage = notif.getInvoiceMessages(entityId,
                         processId, invoice.getEntity().getUser()
                                 .getLanguageIdField(), invoice.getEntity());
 
                 NotificationSessionLocalHome notificationHome = (NotificationSessionLocalHome) EJBFactory
                         .lookUpLocalHome(NotificationSessionLocalHome.class,
                                 NotificationSessionLocalHome.JNDI_NAME);
 
                 NotificationSessionLocal notificationSess = notificationHome
                         .create();
 
                 for (int msg = 0; msg < invoiceMessage.length; msg++) {
                     notificationSess.notify(userId, invoiceMessage[msg]);
                 }
             } catch (NotificationNotFoundException e) {
                 log.warn("Invoice message not defined for entity " + entityId
                         + " Invoice email not sent");
             }
             
             if (processPayment) {
                 ProcessPaymentEvent event = new ProcessPaymentEvent(invoiceId, 
                         processId, null, entityId);
                 EventManager.process(event);
             }
         } catch (Exception e) {
             log.error("sending email and processing payment", e);
             ctx.setRollbackOnly();
         } 
     }
         
 
     /**
      * @ejb.transaction type="RequiresNew"
      * @ejb:interface-method view-type="local" Process a user, generating the
      *                       invoice/s,
      * @param userId
      */
     public Integer[] processUser(Integer processId, Integer userId,
             boolean isReview, boolean onlyRecurring) {
         int invoiceGenerated = 0;
         Integer[] retValue = null;
 
         try {
             UserBL user = new UserBL(userId);
             
             if (!user.canInvoice()) {
                 log.debug("Skipping non-customer / subaccount user " + 
                         userId);
                 return new Integer[0];
             }
 
             BillingProcessBL processBL = new BillingProcessBL(processId);
             BillingProcessEntityLocal process = processBL.getEntity();
             
             // payment and notification only needed if this user gets a 
             // new invoice.
             InvoiceEntityLocal newInvoices[] = processBL.generateInvoice(
                     process, user.getEntity(), isReview, onlyRecurring);
             if (newInvoices == null) {
            	if (!isReview) {
	                NoNewInvoiceEvent event = new NoNewInvoiceEvent(
	                        user.getEntityId(userId), userId, 
	                        process.getBillingDate(), 
	                        user.getEntity().getSubscriptionStatus().getId());
	                EventManager.process(event);
            	}
                 return new Integer[0];
             }
             retValue = new Integer[newInvoices.length];
             for (int f = 0; f < newInvoices.length; f++) {
                 retValue[f] = newInvoices[f].getId();
                 
                 // update this run total for the currency of the new invoice
                 BillingProcessRunBL run = new BillingProcessRunBL();
                 run.setProcess(processId);
                 run.updateNewInvoice(newInvoices[f].getCurrencyId(), 
                         newInvoices[f].getTotal());
                 
                 invoiceGenerated++;
             }
             log.info("The user " + userId + " has been processed."
                     + invoiceGenerated);
         } catch (Exception e) {
             log.error("Exception caught when processing the user " + 
                     userId, e);
             // rollback !
             ctx.setRollbackOnly();
             return null; // the user was not processed
         }
 
         return retValue;
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public BillingProcessDTOEx getDto(Integer processId, Integer languageId) 
             throws SessionInternalError {
         BillingProcessDTOEx retValue = null;
         
         try {
             BillingProcessBL process = new BillingProcessBL(processId);
             retValue = process.getDtoEx(languageId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }            
            
         return retValue;            
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public BillingProcessConfigurationDTO getConfigurationDto(Integer entityId) 
             throws SessionInternalError {
         BillingProcessConfigurationDTO retValue = null;
         
         try {
             ConfigurationBL config = new ConfigurationBL(entityId);
             retValue = config.getDTO();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }            
            
         return retValue;            
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Integer createUpdateConfiguration(Integer executorId,
             BillingProcessConfigurationDTO dto) 
             throws SessionInternalError {
         Integer retValue;
         
         try {
             log.debug("Updating configuration " + dto);
             ConfigurationBL config = new ConfigurationBL();
             retValue = config.createUpdate(executorId, dto);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }            
            
         return retValue;            
     }
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public Integer getLast(Integer entityId) 
             throws SessionInternalError {
         int retValue;
         
         try {
             BillingProcessBL process = new BillingProcessBL();
             retValue = process.getLast(entityId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }            
         
         return retValue > 0 ? new Integer(retValue) : null;
     }  
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public BillingProcessDTOEx getReviewDto(Integer entityId, 
             Integer languageId) throws SessionInternalError {
         BillingProcessDTOEx dto = null;
         try {
             BillingProcessBL process = new BillingProcessBL();
             dto = process.getReviewDTO(entityId, languageId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
         
         return dto;           
     }    
 
     /**
      * @ejb:interface-method view-type="remote"
      */
     public BillingProcessConfigurationDTO setReviewApproval(
             Integer executorId, Integer entityId, 
             Boolean flag) throws SessionInternalError {
         try {
             log.debug("Setting review approval : " + flag);
             ConfigurationBL config = new ConfigurationBL(entityId);
             config.setReviewApproval(executorId, flag.booleanValue());
             return getConfigurationDto(entityId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
     }  
     
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      */
     public void trigger(Date pToday) 
             throws SessionInternalError {
         try {
         
             Date today = Util.truncateDate(pToday);
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             EventLogger eLogger = EventLogger.getInstance();
             BillingProcessBL processBL = new BillingProcessBL();
             GregorianCalendar cal = new GregorianCalendar();  
          
             // call using JNDI to use CMT
             BillingProcessSessionLocalHome localHome = 
                 (BillingProcessSessionLocalHome) EJBFactory
                     .lookUpLocalHome(
                        BillingProcessSessionLocalHome.class,
                        BillingProcessSessionLocalHome.JNDI_NAME);
             BillingProcessSessionLocal local = localHome.create();
 
             // loop over all the entities
             EntityBL entityBL = new EntityBL();
             Integer entityArray[] = entityBL.getAllIDs();
             log.debug("Running trigger. Today = " + today + "[" + today.getTime() +
                     "] entities = " + entityArray.length);
             for (int entityIndex = 0; entityIndex < entityArray.length;
                     entityIndex++) {
                 log.debug("New entity row index " + entityIndex + 
                         " of " + entityArray.length);
                 Integer entityId = entityArray[entityIndex];
                 log.debug("Processing (1) entity " + entityId + " total = " + 
                         entityArray.length);
                 // now process this entity
                 ConfigurationBL configEntity = new ConfigurationBL(entityId);
                 BillingProcessConfigurationDTO config = configEntity.getDTO();
                 if (!config.getNextRunDate().after(today)) {
                     // there should be a run today 
                     boolean doRun = true;
                 	log.debug("A process has to be done for entity " + entityId);
                     // check that: the configuration requires a review
                     // AND, there is no partial run already there (failed)
                     if (config.getGenerateReport().intValue() == 1 && 
                             processBL.present(entityId, new Integer(0), 
                                     config.getNextRunDate()) == null) {
                         // a review had to be done for the run to go ahead
                         boolean reviewPresent = processBL.isReviewPresent(entityId); 
                         if (!reviewPresent) {  // review wasn't generated
                             log.warn("Review is required but not present for " +
                                     "entity " + entityId);
                             eLogger.warning(entityId, config.getId(), 
                                     EventLogger.MODULE_BILLING_PROCESS, 
                                     EventLogger.BILLING_REVIEW_NOT_GENERATED, 
                                     Constants.TABLE_BILLING_PROCESS_CONFIGURATION);
                             
                             generateReview(entityId, config.getNextRunDate(), 
                                     config.getPeriodUnitId(), config.
                                         getPeriodValue());
 
                             doRun = false;
                         } else if (config.getReviewStatus().
                                 equals(Constants.REVIEW_STATUS_GENERATED)) {
                             // the review has to be reviewd yet
                             GregorianCalendar now = new GregorianCalendar();
                             log.warn("Review is required but is not approved" +
                                     ".Entity " + entityId + " hour is " + 
                                     now.get(GregorianCalendar.HOUR_OF_DAY));
                             eLogger.warning(entityId, config.getId(), 
                                     EventLogger.MODULE_BILLING_PROCESS, 
                                     EventLogger.BILLING_REVIEW_NOT_APPROVED, 
                                     Constants.TABLE_BILLING_PROCESS_CONFIGURATION);
                             try {
                                 // only once per day please
                                 if (now.get(GregorianCalendar.HOUR_OF_DAY) < 1) {
                                     String params[] = new String[1];
                                     params[0] = entityId.toString();
                                     NotificationBL.sendSapienterEmail(entityId, 
                                             "process.review_waiting", null, params);
                                 }
                             } catch (Exception e) {
                                 log.warn("Exception sending an entity email", 
                                         e);
                             }
                             doRun = false;
                         } else if (config.getReviewStatus().
                                 equals(Constants.REVIEW_STATUS_DISAPPROVED)) {
                             // is has been disapproved, let's regenerate
                             log.debug("The process should run, but the review " +
                                     "has been disapproved");
                             generateReview(entityId, config.getNextRunDate(), 
                                     config.getPeriodUnitId(), config.
                                         getPeriodValue());
                             doRun = false;
                         }
                     }
                     
                     // do the run
                     if (doRun) {
                         local.processEntity(entityId, config.getNextRunDate(), 
                                 config.getPeriodUnitId(), config.getPeriodValue(),
                                 false);
                     }
                 } else {
                     // no run, may be then a review generation
                 	log.debug("No run scheduled. Next run on " + config.getNextRunDate().getTime());
                     
                     /*
                      * Review generation
                      */
                     if (config.getGenerateReport().intValue() == 1) {
                         cal.setTime(config.getNextRunDate());
                         cal.add(GregorianCalendar.DAY_OF_MONTH, 
                                -config.getDaysForReport().intValue());
                         if (!cal.getTime().after(today)) {
                             boolean reviewPresent = processBL.isReviewPresent(entityId);
                             if (reviewPresent && !config.getReviewStatus().
                                     equals(Constants.REVIEW_STATUS_DISAPPROVED)) {
                                 // there's already a review there, and it's been
                                 // either approved or not yet reviewed
                             } else {
                                 log.debug("Review disapproved. Regeneratting.");
                                 generateReview(entityId, config.getNextRunDate(),
                                         config.getPeriodUnitId(), config.
                                             getPeriodValue());
                             }
                         }
                     }
                 } // else (no run)
                 
                 /*
                  * Retries, only if automatic payment is set
                  */
                 if (config.getAutoPayment().intValue() == 1) {
                     // get the last process
                     try {
                         Integer[] processToRetry = processBL.getToRetry(entityId);
                         for (int f = 0; f < processToRetry.length; f++) {
                             local.doRetry(processToRetry[f], 
                                     config.getDaysForRetry().intValue(), 
                                     today);
                         }
                     } catch (FinderException e) {
                         // it could be that an entity doesn't have any process yet
                     }
                 }
 
             } // for all entities
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     } 
     
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      * @return the id of the invoice generated
      */
     public InvoiceDTOEx generateInvoice(Integer orderId, Integer invoiceId,
             Integer languageId) 
             throws SessionInternalError {
         
         try {
             InvoiceDTOEx dto = null;
             BillingProcessBL process = new BillingProcessBL();
             InvoiceEntityLocal invoice = process.generateInvoice(orderId,
                     invoiceId);
             if (invoice != null) {
                 InvoiceBL invoiceBl = new InvoiceBL(invoice);
                 dto = invoiceBl.getDTOEx(languageId, true);
             }
             
             return dto;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
     }
     
     /**
      * @ejb:interface-method view-type="remote"
      * @ejb.transaction type="Required"
      */
     public void reviewUsersStatus(Date today) 
             throws SessionInternalError {
         try {
             AgeingBL age = new AgeingBL();
             age.reviewAll(today);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }     
       
     // EJB Callbacks ---------------------------------------------------
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbActivate()
      */
     public void ejbActivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbPassivate()
      */
     public void ejbPassivate() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#ejbRemove()
      */
     public void ejbRemove() throws EJBException, RemoteException {
     }
 
     /* (non-Javadoc)
      * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
      */
     public void setSessionContext(SessionContext newCtx)
         throws EJBException, RemoteException {
         log = Logger.getLogger(BillingProcessSessionBean.class);
         ctx = newCtx;
     }
 
 }
