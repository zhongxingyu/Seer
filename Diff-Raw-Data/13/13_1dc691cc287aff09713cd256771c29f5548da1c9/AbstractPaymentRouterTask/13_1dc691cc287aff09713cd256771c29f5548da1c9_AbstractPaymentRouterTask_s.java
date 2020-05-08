 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.payment.tasks;
 
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
 import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskBL;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 
 /**
  * Abstract class for payment routers. Payment routers must implement
  * the selectDelegate method which returns a PaymentTask to process 
  * the payment. The subclass can optionally override the 
  * getAsyncParameters method if the RouterAsyncParameters plug-in is
  * to be used with it.
  */
 public abstract class AbstractPaymentRouterTask extends PluggableTask 
         implements PaymentTask {
     private static final Logger LOG = Logger.getLogger(AbstractPaymentRouterTask.class);
 
     /**
      * Determines what processor is to process the payment. Takes the
      * payment info and returns a processor. 
      */
     protected abstract PaymentTask selectDelegate(PaymentDTOEx paymentInfo)
             throws PluggableTaskException;
 
     /**
      * Method called by RouterAsyncParameters to add any parameters for
      * concurrent asychronous payment processing.
      */
     public Map<String, String> getAsyncParameters(InvoiceDTO invoice) 
             throws PluggableTaskException {
         return null;
     }
 
     public void failure(Integer userId, Integer retry) {
         // ignore, failure is already forced by broken delegate
     }
 
     public boolean process(PaymentDTOEx paymentInfo)
             throws PluggableTaskException {
         LOG.debug("Routing for " + paymentInfo);
         PaymentTask delegate = selectDelegate(paymentInfo);
         if (delegate == null) {
             // give them a chance
             LOG.error("ATTENTION! Could not find a process to delegate for " +
                     "user : " + paymentInfo.getUserId());
            return false;
         }
 
         delegate.process(paymentInfo);
 
         LOG.debug("done");
         // they already used their chance
         return false;
     }
 
     public boolean preAuth(PaymentDTOEx paymentInfo) 
             throws PluggableTaskException {
         PaymentTask delegate = selectDelegate(paymentInfo);
         delegate.preAuth(paymentInfo);
 
         // they already used their chance
         return false;
     }
 
     public boolean confirmPreAuth(PaymentAuthorizationDTO auth, 
             PaymentDTOEx paymentInfo) throws PluggableTaskException {
         PaymentTask delegate = selectDelegate(paymentInfo);
         if (delegate == null){
             LOG.error("ATTENTION! Delegate is recently changed for user : " + 
                     paymentInfo.getUserId() + " with not captured transaction: " +
                     auth.getTransactionId());
            return false;
         }
         delegate.confirmPreAuth(auth, paymentInfo);
         // they already used their chance
         return false;
     }
 
     protected PaymentTask instantiateTask(Integer taskId)
             throws PluggableTaskException {
         PluggableTaskBL<PaymentTask> taskLoader = 
                 new PluggableTaskBL<PaymentTask>(taskId);
         return taskLoader.instantiateTask();
     }
 
     protected Integer intValueOf(Object object) {
         if (object instanceof Number) {
             return Integer.valueOf(((Number) object).intValue());
         }
         if (object instanceof String) {
             String parseMe = (String) object;
             return Integer.parseInt(parseMe);
         }
         return null;
     }
 }
