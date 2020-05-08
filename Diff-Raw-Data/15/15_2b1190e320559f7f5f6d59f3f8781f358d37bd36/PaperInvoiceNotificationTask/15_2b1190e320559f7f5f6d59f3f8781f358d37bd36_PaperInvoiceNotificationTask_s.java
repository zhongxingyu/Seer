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
 
 package com.sapienter.jbilling.server.pluggableTask;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.PaperInvoiceBatchBL;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
 import com.sapienter.jbilling.server.process.db.PaperInvoiceBatchDTO;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 
 /**
  * @author Emil
  */
 public class PaperInvoiceNotificationTask
         extends PluggableTask implements NotificationTask {
 
     private static final Logger LOG = Logger.getLogger(PaperInvoiceNotificationTask.class);
     // pluggable task parameters names
     public static final ParameterDescription PARAMETER_DESIGN = 
     	new ParameterDescription("design", true, ParameterDescription.Type.STR);
     public static final ParameterDescription PARAMETER_LANGUAGE_OPTIONAL = 
     	new ParameterDescription("language", false, ParameterDescription.Type.STR);
     public static final ParameterDescription PARAMETER_SQL_QUERY_OPTIONAL = 
     	new ParameterDescription("sql_query", false, ParameterDescription.Type.STR);
 
 
     //initializer for pluggable params
     { 
     	descriptions.add(PARAMETER_DESIGN);
         descriptions.add(PARAMETER_LANGUAGE_OPTIONAL);
         descriptions.add(PARAMETER_SQL_QUERY_OPTIONAL);
     }
 
 
 
     private String design;
     private boolean language;
     private boolean sqlQuery;
     private ContactBL contact;
     private ContactDTOEx to;
     private Integer entityId;
     private InvoiceDTO invoice;
     private ContactDTOEx from;
 
     /* (non-Javadoc)
      * @see com.sapienter.jbilling.server.pluggableTask.NotificationTask#deliver(com.sapienter.betty.interfaces.UserEntityLocal, com.sapienter.betty.server.notification.MessageDTO)
      */
     private void init(UserDTO user, MessageDTO message)
             throws TaskException {
         design = (String) parameters.get(PARAMETER_DESIGN.getName());
 
         language = Boolean.valueOf((String) parameters.get(
                 PARAMETER_LANGUAGE_OPTIONAL.getName()));
 
         sqlQuery = Boolean.valueOf((String) parameters.get(
                 PARAMETER_SQL_QUERY_OPTIONAL.getName()));
 
         invoice = (InvoiceDTO) message.getParameters().get(
                 "invoiceDto");
         try {
             contact = new ContactBL();
             contact.setInvoice(invoice.getId());
             to = contact.getDTO();
             entityId = user.getEntity().getId();
             contact.setEntity(entityId);
             from = contact.getDTO();
         } catch (Exception e) {
             throw new TaskException(e);
         }
     }
 
     public void deliver(UserDTO user, MessageDTO message)
             throws TaskException {
         if (!message.getTypeId().equals(MessageDTO.TYPE_INVOICE_PAPER)) {
             // this task is only to notify about invoices
             return;
         }
         try {
             init(user, message);
             NotificationBL.generatePaperInvoiceAsFile(getDesign(user), sqlQuery,
                     invoice, from, to, message.getContent()[0].getContent(),
                     message.getContent()[1].getContent(), entityId,
                     user.getUserName(), user.getPassword());
             // update the batch record
             Integer processId = (Integer) message.getParameters().get(
                     "processId");
             PaperInvoiceBatchBL batchBL = new PaperInvoiceBatchBL();
             PaperInvoiceBatchDTO record = batchBL.createGet(processId);
             record.setTotalInvoices(record.getTotalInvoices() + 1);
             // link the batch to this invoice
             // lock the row, the payment MDB will update too
             InvoiceDTO myInvoice = new InvoiceDAS().findForUpdate(invoice.getId());
             myInvoice.setPaperInvoiceBatch(record);
             record.getInvoices().add(myInvoice);
         } catch (Exception e) {
             throw new TaskException(e);
         }
     }
 
     public byte[] getPDF(UserDTO user, MessageDTO message)
             throws SessionInternalError {
         try {
             init(user, message);
             LOG.debug("now message1 = " + message.getContent()[0].getContent());
             return NotificationBL.generatePaperInvoiceAsStream(getDesign(user),
                     sqlQuery, invoice, from, to, 
                     message.getContent()[0].getContent(),
                     message.getContent()[1].getContent(), entityId,
                     user.getUserName(), user.getPassword());
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     public String getPDFFile(UserDTO user, MessageDTO message)
             throws SessionInternalError {
         try {
             init(user, message);
             return NotificationBL.generatePaperInvoiceAsFile(getDesign(user),
                     sqlQuery, invoice, from, to, 
                     message.getContent()[0].getContent(),
                     message.getContent()[1].getContent(), entityId,
                     user.getUserName(), user.getPassword());
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     public int getSections() {
         return 2;
     }
 
     private String getDesign(UserDTO user) {
         if (language) {
             return design + user.getLanguage().getCode();
         } else {
             return design;
         }
     }
 }
