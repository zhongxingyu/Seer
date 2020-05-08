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
 
 package com.sapienter.jbilling.server.user;
 
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.naming.NamingException;
 
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
 import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
 import com.sapienter.jbilling.server.user.contact.db.ContactMapDAS;
 import com.sapienter.jbilling.server.user.contact.db.ContactMapDTO;
 import com.sapienter.jbilling.server.user.contact.db.ContactTypeDAS;
 import com.sapienter.jbilling.server.user.contact.db.ContactTypeDTO;
 import com.sapienter.jbilling.server.user.event.NewContactEvent;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.Context;
 import com.sapienter.jbilling.server.util.db.JbillingTableDAS;
 import java.util.ArrayList;
 
 public class ContactBL {
     private static final Logger LOG = Logger.getLogger(ContactBL.class);             
 
     // contact types in synch with the table contact_type
     static public final Integer ENTITY = new Integer(1);
     
     // private methods
     private ContactDAS contactDas = null;
     private ContactDTO contact = null;
     private Integer entityId = null;
     private JbillingTableDAS jbDAS = null;
     private EventLogger eLogger = null;
     
     public ContactBL(Integer contactId)
             throws NamingException {
         init();
         contact = contactDas.find(contactId);
     }
     
     public ContactBL() {
         init();
     }
     
     public void set(Integer userId) {
         contact = contactDas.findPrimaryContact(userId);
         //LOG.debug("Found " + contact + " for " + userId);
         setEntityFromUser(userId);
     }
     
     private void setEntityFromUser(Integer userId) {
         // id the entity
         if (userId != null) {
             try {
                 entityId = new UserBL().getEntityId(userId);
             } catch (Exception e) {
                 LOG.error("Finding the entity", e);
             }
         }
     }
  
     public void set(Integer userId, Integer contactTypeId) {
         contact = contactDas.findContact(userId, contactTypeId);
         setEntityFromUser(userId);
     }
 
     public void setEntity(Integer entityId) {
         this.entityId = entityId;
         contact = contactDas.findEntityContact(entityId);
     }
 
     public boolean setInvoice(Integer invoiceId) {
         boolean retValue = false;
         contact = contactDas.findInvoiceContact(invoiceId);
         InvoiceBL invoice = new InvoiceBL(invoiceId);
         if (contact == null) {
             set(invoice.getEntity().getBaseUser().getUserId());
 
         } else {
             entityId = invoice.getEntity().getBaseUser().getCompany().getId();
             retValue = true;
         }

         return retValue;
     }
 
     public Integer getPrimaryType(Integer entityId) {
         return new ContactTypeDAS().findPrimary(entityId).getId();
     }
     
     /**
      * Rather confusing considering the previous method, but necessary
      * to follow the convention
      * @return
      */
     public ContactDTO getEntity() {
         return contact;
     }
     
     
     public ContactDTOEx getVoidDTO(Integer myEntityId) {
         entityId = myEntityId;
         ContactDTOEx retValue = new ContactDTOEx();
         return retValue;
     }
     
     public ContactDTOEx getDTO() {
 
         ContactDTOEx retValue =  new ContactDTOEx(
             contact.getId(),
             contact.getOrganizationName(),
             contact.getAddress1(),
             contact.getAddress2(),
             contact.getCity(),
             contact.getStateProvince(),
             contact.getPostalCode(),
             contact.getCountryCode(),
             contact.getLastName(),
             contact.getFirstName(),
             contact.getInitial(),
             contact.getTitle(),
             contact.getPhoneCountryCode(),
             contact.getPhoneAreaCode(),
             contact.getPhoneNumber(),
             contact.getFaxCountryCode(),
             contact.getFaxAreaCode(),
             contact.getFaxNumber(),
             contact.getEmail(),
             contact.getCreateDate(),
             contact.getDeleted(),
             contact.getInclude());
         
         return retValue;
     }
     
     public List<ContactDTOEx> getAll(Integer userId)  {
         List<ContactDTOEx> retValue = new ArrayList<ContactDTOEx>();
         UserBL user = new UserBL(userId);
         entityId = user.getEntityId(userId);
         for (ContactTypeDTO type: user.getEntity().getEntity().getContactTypes()) {
                 contact = contactDas.findContact(userId, type.getId());
             if (contact != null) {
                 ContactDTOEx dto = getDTO();
                 dto.setType(type.getId());
                 retValue.add(dto);
             }
         }
         return retValue;
     }
 
     private void init() {
         contactDas = new ContactDAS();
         jbDAS = (JbillingTableDAS) Context.getBean(Context.Name.JBILLING_TABLE_DAS);
         eLogger = EventLogger.getInstance();
     }
     
     public Integer createPrimaryForUser(ContactDTOEx dto, Integer userId, Integer entityId, Integer executorUserId) 
             throws SessionInternalError {
         // find which type id is the primary for this entity
         try {
             Integer retValue;
             ContactTypeDTO type = new ContactTypeDAS().findPrimary(entityId);
 
             retValue =  createForUser(dto, userId, type.getId(), executorUserId);
             // this is the primary contact, the only one with a user_id
             // denormilized for performance
             contact.setUserId(userId); 
             return retValue;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
     }
     
     /**
      * Finds what is the next contact type and creates a new
      * contact with it
      * @param dto
      */
     public boolean append(ContactDTOEx dto, Integer userId) 
                 throws SessionInternalError {
         UserBL user = new UserBL(userId);
         for (ContactTypeDTO type: user.getEntity().getEntity().getContactTypes()) {
             set(userId, type.getId());
             if (contact == null) {
                 // this one is available
                 createForUser(dto, userId, type.getId(), null);
                 return true;
             }
         }
         
         return false; // no type was avaiable
     }
     
     public Integer createForUser(ContactDTOEx dto, Integer userId, 
             Integer typeId, Integer executorUserId) throws SessionInternalError {
         try {
             return create(dto, Constants.TABLE_BASE_USER, userId, typeId, executorUserId);
         } catch (Exception e) {
             LOG.debug("Error creating contact for " +
                     "user " + userId);
             throw new SessionInternalError(e);
         }
     }
     
     public Integer createForInvoice(ContactDTOEx dto, Integer invoiceId) {
         return create(dto, Constants.TABLE_INVOICE, invoiceId, new Integer(1), null);
     }
     
     /**
      * 
      * @param dto
      * @param table
      * @param foreignId
      * @param typeId Use 1 if it is not for a user (like and entity or invoice)
      * @return
      * @throws NamingException
      */
     public Integer create(ContactDTOEx dto, String table,  
             Integer foreignId, Integer typeId, Integer executorUserId) {
         // first thing is to create the map to the user
         ContactMapDTO map = new ContactMapDTO();
         map.setJbillingTable(jbDAS.findByName(table));
         map.setContactType(new ContactTypeDAS().find(typeId));
         map.setForeignId(foreignId);
         map = new ContactMapDAS().save(map);
         
         // now the contact itself
         dto.setCreateDate(new Date());
         dto.setDeleted(0);
         dto.setVersionNum(0);
         dto.setId(0);
         
         contact = contactDas.save(new ContactDTO(dto)); // it won't take the Ex
         contact.setContactMap(map);
         map.setContact(contact);
         
         LOG.debug("created " + contact);
 
         // do an event if this is a user contact (invoices, companies, have
         // contacts too)
         if (table.equals(Constants.TABLE_BASE_USER)) {
             NewContactEvent event = new NewContactEvent(contact, entityId);
             EventManager.process(event);
 
             if ( null != executorUserId) {
                 eLogger.audit(executorUserId,
                         contact.getUserId(),
                         Constants.TABLE_CONTACT,
                         contact.getId(),
                         EventLogger.MODULE_USER_MAINTENANCE,
                         EventLogger.ROW_CREATED, null, null, null);
             } else {
                 eLogger.auditBySystem(entityId,
                                   contact.getUserId(),
                                   Constants.TABLE_CONTACT,
                                   contact.getId(),
                                   EventLogger.MODULE_USER_MAINTENANCE,
                                   EventLogger.ROW_CREATED, null, null, null);
             }
         }
 
         return contact.getId();
     }
     
     public void updatePrimaryForUser(ContactDTOEx dto, Integer userId) {
         contact = contactDas.findPrimaryContact(userId);
         update(dto, null);
     }
 
     public void createUpdatePrimaryForUser(ContactDTOEx dto, Integer userId, Integer entityId, Integer executorId) {
         contact = contactDas.findPrimaryContact(userId);
 
         if (contact == null) {
             createPrimaryForUser(dto, userId, entityId, executorId);
         } else {
             update(dto, executorId);
         }
     }
     
     public void updateForUser(ContactDTOEx dto, Integer userId,
             Integer contactTypeId, Integer executorUserId) throws SessionInternalError {
         contact = contactDas.findContact(userId, contactTypeId);
         if (contact != null) {
             if (entityId == null) {
                 setEntityFromUser(userId);
             }
             update(dto, executorUserId);
         } else {
             try {
                 createForUser(dto, userId, contactTypeId, executorUserId);
             } catch (Exception e1) {
                 throw new SessionInternalError(e1);
             }
         } 
     }
     
     private void update(ContactDTOEx dto, Integer executorUserId) {
         contact.setAddress1(dto.getAddress1());
         contact.setAddress2(dto.getAddress2());
         contact.setCity(dto.getCity());
         contact.setCountryCode(dto.getCountryCode());
         contact.setEmail(dto.getEmail());
         contact.setFaxAreaCode(dto.getFaxAreaCode());
         contact.setFaxCountryCode(dto.getFaxCountryCode());
         contact.setFaxNumber(dto.getFaxNumber());
         contact.setFirstName(dto.getFirstName());
         contact.setInitial(dto.getInitial());
         contact.setLastName(dto.getLastName());
         contact.setOrganizationName(dto.getOrganizationName());
         contact.setPhoneAreaCode(dto.getPhoneAreaCode());
         contact.setPhoneCountryCode(dto.getPhoneCountryCode());
         contact.setPhoneNumber(dto.getPhoneNumber());
         contact.setPostalCode(dto.getPostalCode());
         contact.setStateProvince(dto.getStateProvince());
         contact.setTitle(dto.getTitle());
         contact.setInclude(dto.getInclude());
 
         if (entityId == null) {
             setEntityFromUser(contact.getUserId());
         }
 
         NewContactEvent event = new NewContactEvent(contact, entityId);
         EventManager.process(event);
 
         eLogger.auditBySystem(entityId,
                               contact.getUserId(),
                               Constants.TABLE_CONTACT,
                               contact.getId(),
                               EventLogger.MODULE_USER_MAINTENANCE,
                               EventLogger.ROW_UPDATED, null, null, null);
     }
 
     public void delete() {
         
         if (contact == null) return;
         
         LOG.debug("Deleting contact " + contact.getId());
         // delete the map first
         new ContactMapDAS().delete(contact.getContactMap());
 
         // for the logger
         Integer entityId = this.entityId;
         Integer userId = contact.getUserId();
         Integer contactId = contact.getId();
 
         // the contact goes last
         contactDas.delete(contact);
         contact = null;
 
         // log event
         eLogger.auditBySystem(entityId,
                               userId,
                               Constants.TABLE_CONTACT,
                               contactId,
                               EventLogger.MODULE_USER_MAINTENANCE,
                               EventLogger.ROW_DELETED, null, null, null);
     }
     
     /**
      * Sets this contact object to that on the parent, taking the children id
      * as a parameter. 
      * @param customerId
      */
     public void setFromChild(Integer userId) {
         UserBL customer = new UserBL(userId);
         set(customer.getEntity().getCustomer().getParent().getBaseUser().getUserId());
     }
 }
