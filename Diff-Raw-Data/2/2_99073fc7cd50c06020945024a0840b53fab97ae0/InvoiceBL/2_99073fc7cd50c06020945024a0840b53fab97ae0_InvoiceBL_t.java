 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.server.invoice;
 
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.interfaces.BillingProcessEntityLocal;
 import com.sapienter.jbilling.interfaces.EntityEntityLocal;
 import com.sapienter.jbilling.interfaces.InvoiceEntityLocal;
 import com.sapienter.jbilling.interfaces.InvoiceEntityLocalHome;
 import com.sapienter.jbilling.interfaces.InvoiceLineEntityLocal;
 import com.sapienter.jbilling.interfaces.InvoiceLineEntityLocalHome;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocalHome;
 import com.sapienter.jbilling.interfaces.OrderEntityLocal;
 import com.sapienter.jbilling.interfaces.OrderProcessEntityLocal;
 import com.sapienter.jbilling.interfaces.PaymentInvoiceMapEntityLocal;
 import com.sapienter.jbilling.server.entity.InvoiceDTO;
 import com.sapienter.jbilling.server.item.CurrencyBL;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.list.ResultList;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.order.OrderBL;
 import com.sapienter.jbilling.server.payment.PaymentBL;
 import com.sapienter.jbilling.server.payment.PaymentInvoiceMapDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.BasicPenaltyTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskManager;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.EventLogger;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import com.sapienter.jbilling.server.util.Util;
 
 public class InvoiceBL extends ResultList 
         implements Serializable, InvoiceSQL {
 
     private JNDILookup EJBFactory = null;
     private InvoiceEntityLocalHome invoiceHome = null;
     private InvoiceEntityLocal invoice = null;
     private Logger log = null;
     private EventLogger eLogger = null;
 
     public InvoiceBL(Integer invoiceId) 
             throws NamingException, FinderException {
         init();
         set(invoiceId);
     }
 
     public InvoiceBL() throws NamingException {
         init();
     }
 
     public InvoiceBL(InvoiceEntityLocal invoice) throws NamingException {
         init();
         set(invoice);
     }
 
     private void init() throws NamingException {
         log = Logger.getLogger(InvoiceBL.class);     
         eLogger = EventLogger.getInstance();        
         EJBFactory = JNDILookup.getFactory(false);
         invoiceHome = (InvoiceEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 InvoiceEntityLocalHome.class,
                 InvoiceEntityLocalHome.JNDI_NAME);
     }
 
     public InvoiceEntityLocal getEntity() {
         return invoice;
     }
     
     public InvoiceEntityLocalHome getHome() {
         return invoiceHome;
     }
 
     public void set(Integer id) throws FinderException {
         invoice = invoiceHome.findByPrimaryKey(id);
     }
     
     public void set(InvoiceEntityLocal invoice) {
         this.invoice = invoice;
     }
 
     /**
      * 
      * @param userId
      * @param newInvoice
      * @param process It can be null.
      * @throws CreateException
      */
     public void create(Integer userId, NewInvoiceDTO newInvoice,
             BillingProcessEntityLocal process) 
             throws CreateException, NamingException, FinderException {
         Vector invoiceEntities = new Vector();
         for (Iterator it = newInvoice.getInvoices().iterator(); it.hasNext();) {
             InvoiceDTO dto = (InvoiceDTO) it.next();
             set(dto.getId());
             invoiceEntities.add(invoice);
         }
         
         // find out the entity id 
         PreferenceBL pref = new PreferenceBL();
         UserBL user = null;
         Integer entityId;
         if (process != null) {
             entityId = process.getEntityId();
         } else {
         	// this is a manual invoice, there's no billing process
         	user = new UserBL(userId);
             entityId = user.getEntity().getEntity().getId();
         }
         
         // verify if this entity is using the 'continuous invoice date' preference
         try {
             pref.set(entityId, Constants.PREFERENCE_CONTINUOUS_DATE);
             Date lastDate = com.sapienter.jbilling.common.Util.parseDate(pref.getString());
             if (lastDate.after(newInvoice.getBillingDate())) {
                 newInvoice.setBillingDate(lastDate);
             } else {
                 // update the lastest date only if this is not a review
                 if (newInvoice.getIsReview() == null ||
                         newInvoice.getIsReview().intValue() == 0) {
                     pref.createUpdateForEntity(entityId, 
                             Constants.PREFERENCE_CONTINUOUS_DATE, null,
                             com.sapienter.jbilling.common.Util.parseDate(
                                     newInvoice.getBillingDate()), null);
                 }
             }
         } catch (FinderException e) {
             // not interested, ignore
         }
 
         // in any case, ensure that the due date is => that invoice date
         if (newInvoice.getDueDate().before(newInvoice.getBillingDate())) {
             newInvoice.setDueDate(newInvoice.getBillingDate());
         }
         // ensure that there are only two decimals in the invoice
         if (newInvoice.getTotal() != null) {
             newInvoice.setTotal(new Float(Util.round(
                     newInvoice.getTotal().floatValue(), 2)));
         }
         if (newInvoice.getBalance() != null) {
             newInvoice.setBalance(new Float(Util.round(
                     newInvoice.getBalance().floatValue(), 2)));
         }
         
         // create the invoice row
         invoice = invoiceHome.create(userId, newInvoice, 
                 invoiceEntities, process);
         
         // add the customer notes if it applies
         pref.set(entityId, Constants.PREFERENCE_SHOW_NOTE_IN_INVOICE);
         
         if (pref.getInt() == 1) {
         	if (user == null) {
         		user = new UserBL(userId);
         	}
         	if (user.getEntity().getCustomer() != null && 
                     user.getEntity().getCustomer().getNotes() != null) {
                 // append the notes if there's some text already there
 	        	newInvoice.setCustomerNotes(
                     (newInvoice.getCustomerNotes() == null)
                         ? user.getEntity().getCustomer().getNotes()
                         : newInvoice.getCustomerNotes() + " " +
                             user.getEntity().getCustomer().getNotes());
         	}
         }
         // notes might come from the customer, the orders, or both
         if (newInvoice.getCustomerNotes() != null && 
                 newInvoice.getCustomerNotes().length() > 0) {
             invoice.setCustomerNotes(newInvoice.getCustomerNotes());
         } 
         
         // calculate/compose the number
         String numberStr = null;
         if (newInvoice.getIsReview() != null && 
                 newInvoice.getIsReview().intValue() == 1) {
             // invoices for review will be seen by the entity employees
             // so the entity locale will be used
             EntityBL entity = new EntityBL(entityId);
             ResourceBundle bundle = ResourceBundle.getBundle("entityNotifications", 
                     entity.getLocale());
             numberStr = bundle.getString("invoice.review.number");
         } else if (newInvoice.getNumber() == null || 
                 newInvoice.getNumber().length() == 0) {
             String prefix;
             try {
                 pref.set(entityId, Constants.PREFERENCE_INVOICE_PREFIX);
                 prefix = pref.getString();
                 if (prefix == null) {
                     prefix = "";
                 }
             } catch (FinderException e) {
                 prefix = "";
             }
             int number;
             try {
                 pref.set(entityId, Constants.PREFERENCE_INVOICE_NUMBER);
                 number = pref.getInt();
             } catch (FinderException e1) {
                 number = 1;
             }
             
             numberStr = prefix + number;
             // update for the next time
             number++;
             pref.createUpdateForEntity(entityId, Constants.PREFERENCE_INVOICE_NUMBER,
                     new Integer(number), null, null);
         } else { // for upload of legacy invoices
             numberStr = newInvoice.getNumber();
         }
         
         invoice.setNumber(numberStr);
 
         // set the invoice's contact info with the current user's primary
         ContactBL contact = new ContactBL();
         contact.set(userId);
         contact.createForInvoice(contact.getDTO(), invoice.getId());
     }
     
     public void createLines(NewInvoiceDTO newInvoice) 
             throws NamingException, CreateException {
         Collection invoiceLines = invoice.getInvoiceLines();
 
         // Now create all the invoice lines, from the lines in the DTO
         // put there by the invoice composition pluggable tasks
         InvoiceLineEntityLocalHome invoiceLineHome =
                 (InvoiceLineEntityLocalHome) EJBFactory.lookUpLocalHome(
                 InvoiceLineEntityLocalHome.class,
                 InvoiceLineEntityLocalHome.JNDI_NAME);
 
         // get the result DTO lines
         Iterator dueInvoiceLines = newInvoice.getResultLines().iterator();
         // go over the DTO lines, creating one invoice line for each
         while (dueInvoiceLines.hasNext()) {
             InvoiceLineDTOEx lineToAdd =
                 (InvoiceLineDTOEx) dueInvoiceLines.next();
             // define if the line is a percentage or not
             lineToAdd.setIsPercentage(new Integer(0));
             if (lineToAdd.getItemId() != null) {
                 try {
                     ItemBL item = new ItemBL(lineToAdd.getItemId());
                     if (item.getEntity().getPercentage() != null) {
                         lineToAdd.setIsPercentage(new Integer(1));
                     }
                 } catch (FinderException e) {
                     log.error("Could not find item to create invoice line " + 
                             lineToAdd.getItemId());
                 }
             } 
             // create the database row
             InvoiceLineEntityLocal newLine =
                 invoiceLineHome.create(
                     lineToAdd.getDescription(),
                     lineToAdd.getAmount(),
                     lineToAdd.getQuantity(),
                     lineToAdd.getPrice(),
                     lineToAdd.getTypeId(),
                     lineToAdd.getItemId(),
                     lineToAdd.getSourceUserId(),
                     lineToAdd.getIsPercentage());
             
             // update the invoice-lines relationship
             invoiceLines.add(newLine);
         }
 
     }
     
     /**
      * This will remove all the records (sql delete, not just flag them).
      * It will also update the related orders if applicable
      */
     public void delete() 
             throws SessionInternalError, RemoveException {
         if (invoice == null) {
             throw new SessionInternalError("An invoice has to be set before " +
                     "delete");
         }
         // start by updateing purchase_order.next_billable_day if applicatble
         // for each of the orders included in this invoice
         Iterator it = invoice.getOrders().iterator();
         while (it.hasNext()) {
             OrderProcessEntityLocal orderProcess = (OrderProcessEntityLocal)
                     it.next();
             OrderEntityLocal order = orderProcess.getOrder();
             if (order.getNextBillableDay() == null) {
                 // the next billable day doesn't need updating
                 if (order.getStatusId().equals(
                         Constants.ORDER_STATUS_FINISHED)) {
                     order.setStatusId(Constants.ORDER_STATUS_ACTIVE);
                 }
                 continue;
             }
             // only if this invoice is the responsible for the order's
             // next billable day
             if (order.getNextBillableDay().equals(
                     orderProcess.getPeriodEnd())) {
                 order.setNextBillableDay(orderProcess.getPeriodStart());
                 if (order.getStatusId().equals(
                         Constants.ORDER_STATUS_FINISHED)) {
                     order.setStatusId(Constants.ORDER_STATUS_ACTIVE);
                 }
             }
             
         }
         
         // go over the order process records again just to delete them
         // we are done with this order, delete the process row
         it = invoice.getOrders().iterator();
         while (it.hasNext()) {
             OrderProcessEntityLocal orderProcess = (OrderProcessEntityLocal)
                     it.next();
             orderProcess.remove();
             it = invoice.getOrders().iterator();
         }
         
         // get rid of the contact associated with this invoice
         try {
             ContactBL contact = new ContactBL();
             if (contact.setInvoice(invoice.getId())) {
                 contact.delete();
             }
         } catch (Exception e1) {
             log.error("Exception deleting the contact of an invoice", e1);
         } 
 
         // remove the payment link/s
         PaymentBL payment = new PaymentBL();
         it = invoice.getPaymentMap().iterator();
         while (it.hasNext()) {
             PaymentInvoiceMapEntityLocal map = (PaymentInvoiceMapEntityLocal)
                     it.next();
             payment.removeInvoiceLink(map.getId());
            // needed because the collection has changed
            it = invoice.getPaymentMap().iterator();
         }
 
         // log that this was deleted, otherwise there will be no trace
         eLogger.info(invoice.getUser().getEntity().getId(),
                 invoice.getId(),
                 EventLogger.MODULE_INVOICE_MAINTENANCE, 
                 EventLogger.ROW_DELETED, Constants.TABLE_INVOICE);
 
         // now delete the invoice itself
         invoice.remove();
     }
     
     public void update(NewInvoiceDTO addition)
             throws NamingException, CreateException {
         // add the lines to the invoice first
         createLines(addition);
         // update the inoice record considering the new lines
         invoice.setTotal(calculateTotal()); // new total
         // adjust the balance
         addition.calculateTotal();
         invoice.setBalance(new Float(invoice.getBalance().floatValue() +
                 addition.getTotal().floatValue()));
         if (invoice.getBalance().floatValue() <= 0.001F) {
             invoice.setToProcess(new Integer(0));
         }
     }
     
     private Float calculateTotal() {
         Float retValue = null;
         float total = 0;
         for(Iterator it = invoice.getInvoiceLines().iterator(); 
                 it.hasNext();) {
             InvoiceLineEntityLocal line = (InvoiceLineEntityLocal) it.next();
             total += line.getAmount().floatValue();
         }
         retValue = new Float(total);
         return retValue;
     }
             
     public CachedRowSet getPayableInvoicesByUser(Integer userId) 
             throws SQLException, Exception {
             
         prepareStatement(InvoiceSQL.payableByUser);
         cachedResults.setInt(1,userId.intValue());
         
         execute();
         conn.close();
         return cachedResults;
     }
     
     public float getTotalPaid() {
         float retValue = 0;
         for (Iterator it = invoice.getPaymentMap().iterator(); it.hasNext();) {
             PaymentInvoiceMapEntityLocal paymentMap = (PaymentInvoiceMapEntityLocal) it.next();
             retValue += paymentMap.getAmount().floatValue();
         }
         return retValue;
     }
 
     public CachedRowSet getList(Integer orderId) 
             throws SQLException, Exception {
         prepareStatement(InvoiceSQL.customerList);
         
         // find out the user from the order
         Integer userId;
         OrderBL order = new OrderBL(orderId);
         if (order.getEntity().getUser().getCustomer().getParent() == null) {
             userId = order.getEntity().getUser().getUserId();
         } else {
             userId = order.getEntity().getUser().getCustomer().getParent().
                     getUser().getUserId();
         }
         cachedResults.setInt(1, userId.intValue());
         execute();
         conn.close();
         return cachedResults;
     }
 
     public CachedRowSet getList(Integer entityId, Integer userRole, 
             Integer userId) throws SQLException, Exception {
 
         if(userRole.equals(Constants.TYPE_INTERNAL)) {
             prepareStatement(InvoiceSQL.internalList);
         } else if(userRole.equals(Constants.TYPE_ROOT) ||
                 userRole.equals(Constants.TYPE_CLERK)) {
             prepareStatement(InvoiceSQL.rootClerkList);
             cachedResults.setInt(1, entityId.intValue());
         } else if(userRole.equals(Constants.TYPE_PARTNER)) {
             prepareStatement(InvoiceSQL.partnerList);
             cachedResults.setInt(1,entityId.intValue());
             cachedResults.setInt(2, userId.intValue());
         } else if(userRole.equals(Constants.TYPE_CUSTOMER)) {
             prepareStatement(InvoiceSQL.customerList);
             cachedResults.setInt(1, userId.intValue());
         } else {
             throw new Exception("The invoice list for the type " + userRole + 
                     " is not supported");
         }
            
         execute();
         conn.close();
         return cachedResults;
     }
 
     public CachedRowSet getInvoicesByProcessId(Integer processId) 
             throws SQLException, Exception {
             
         prepareStatement(InvoiceSQL.processList); 
         cachedResults.setInt(1,processId.intValue());
         
         execute();
         conn.close();
         return cachedResults;
     }
 
 	public CachedRowSet getInvoicesByUserId(Integer userId)
 			throws SQLException, Exception {
 
 		prepareStatement(InvoiceSQL.custList);
 		cachedResults.setInt(1, userId.intValue());
 
 		execute();
 		conn.close();
 		return cachedResults;
 	}
 
 	public CachedRowSet getInvoicesByIdRange(Integer from, Integer to, Integer entityId)
 			throws SQLException, Exception {
 
 		prepareStatement(InvoiceSQL.rangeList);
 		cachedResults.setInt(1, from.intValue());
 		cachedResults.setInt(2, to.intValue());
 		cachedResults.setInt(3, entityId.intValue());
 		
 		execute();
 		conn.close();
 		return cachedResults;
 	}
     
     public Integer[] getInvoicesByCreateDateArray(Integer entityId, Date since,
             Date until) 
             throws SQLException, Exception {
             
         cachedResults = getInvoicesByCreateDate(entityId, since, until);
 
         // get ids for return
         Vector ids = new Vector();
         while (cachedResults.next()) {
             ids.add(new Integer(cachedResults.getInt(1)));
         }
         Integer[] retValue = new Integer[ids.size()];
         if (retValue.length > 0) {
             ids.toArray(retValue);
         }
 
         cachedResults.close();
         conn.close();
         return retValue;
     }
 
     public CachedRowSet getInvoicesByCreateDate(Integer entityId, Date since,
             Date until) 
             throws SQLException, Exception {
             
         prepareStatement(InvoiceSQL.getByDate); 
         cachedResults.setInt(1, entityId.intValue());
         cachedResults.setDate(2, new java.sql.Date(since.getTime()));
         // add a day to include the until date
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(until);
         cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
         cachedResults.setDate(3, new java.sql.Date(cal.getTime().getTime()));
         
         execute();
 
         conn.close();
         return cachedResults;
     }
     
     public Integer convertNumberToID(Integer entityId, String number)
             throws SQLException, Exception {
             
         prepareStatement(InvoiceSQL.getIDfromNumber); 
         cachedResults.setInt(1, entityId.intValue());
         cachedResults.setString(2, number);
         
         execute();
 
         conn.close();
         if (cachedResults.wasNull()) {
             return null;
         } else {
             cachedResults.next();
             return new Integer(cachedResults.getInt(1));
         }
     }
     
 
    public Integer getLastByUser(String username, Integer entityId) 
         throws SQLException, NamingException {
 
         Integer retValue = null;
         if (username == null || username.length() == 0) {
             return null;
         }            
         prepareStatement(InvoiceSQL.lastIdbyUser);
         cachedResults.setString(1, username);
         cachedResults.setInt(2, entityId.intValue());
             
         execute();
         if (cachedResults.next()) {
             int value = cachedResults.getInt(1);
             if (!cachedResults.wasNull()) {
                 retValue = new Integer(value);
             }
         } 
         cachedResults.close();
         conn.close();
         return retValue;
     }
 
     public Boolean isUserWithOverdueInvoices(Integer userId, Date today,
             Integer excludeInvoiceId) 
         throws SQLException, NamingException {
 
         Boolean retValue;
         prepareStatement(InvoiceSQL.getOverdueForAgeing);
         cachedResults.setDate(1, new java.sql.Date(today.getTime()));
         cachedResults.setInt(2, userId.intValue());
         if (excludeInvoiceId != null) {
             cachedResults.setInt(3, excludeInvoiceId.intValue());
         } else {
             // nothing to exclude, use an imposible ID (zero)
             cachedResults.setInt(3, 0);
         }
             
         execute();
         if (cachedResults.next()) {
             retValue = new Boolean(true);
             log.debug("user with invoice:" + cachedResults.getInt(1));
         } else {
             retValue = new Boolean(false);
         }
         cachedResults.close();
         conn.close();
         log.debug("user with overdue: " + retValue);
         return retValue;
     }
    
     public Integer[] getManyWS(Integer userId, Integer number)
             throws NamingException, FinderException, SessionInternalError {
         // find the invoice records first
         UserBL user = new UserBL(userId);
         Collection invoices = user.getEntity().getInvoices();
         Vector invoicesVector = new Vector(invoices); // needed to use sort
         Collections.sort(invoicesVector, new InvoiceEntityComparator());
         Collections.reverse(invoicesVector);
         // now convert the entities to WS objects
         Integer retValue[] = new Integer[invoicesVector.size() > 
                                          number.intValue() ? number.intValue() :
                                              invoicesVector.size()];
         for (int f = 0; f < invoicesVector.size() && f < number.intValue(); 
                 f++) {
             invoice = (InvoiceEntityLocal) invoicesVector.get(f);
             retValue[f] = invoice.getId();
         }
         
         return retValue;
     }
     
     public InvoiceWS[] DTOtoWS(Vector dtos) {
         InvoiceWS retValue[] = new InvoiceWS[dtos.size()];
         for (int f = 0; f < retValue.length; f++) {
             retValue[f] = new InvoiceWS((InvoiceDTOEx) dtos.get(f));
         }
         log.debug("converstion " + retValue.length);
 
         return retValue;
     }
     
     public Vector invoiceEJB2DTOEx(Collection invoices, Integer languageId) {
         Vector dtos = new Vector();
         
         for (Iterator it = invoices.iterator(); it.hasNext();) {
             InvoiceEntityLocal invoiceEJB = (InvoiceEntityLocal) it.next();
             try {
                 InvoiceBL invoice = new InvoiceBL(invoiceEJB);
                 dtos.add(invoice.getDTOEx(languageId, false));
             } catch(Exception e) {}
         }
         
         return dtos;
     }
 
     
     public void sendReminders(Date today) 
             throws SQLException, NamingException, FinderException,
                 SessionInternalError, CreateException {
         GregorianCalendar cal = new GregorianCalendar();
         
         EntityBL entity = new EntityBL();
         for (Iterator it = entity.getHome().findEntities().iterator();
                 it.hasNext(); ){
             EntityEntityLocal thisEntity = (EntityEntityLocal) it.next();
             Integer entityId = thisEntity.getId();
             PreferenceBL pref = new PreferenceBL();
             try {
                 pref.set(entityId, 
                         Constants.PREFERENCE_USE_INVOICE_REMINDERS);
             } catch (FinderException e1) {
                 // let it use the defaults
             }
             if (pref.getInt() == 1) {
                 prepareStatement(InvoiceSQL.toRemind);
 
                 cachedResults.setDate(1, new java.sql.Date(today.getTime()));
                 cal.setTime(today);
                 pref.set(entityId,
                         Constants.PREFERENCE_FIRST_REMINDER);
                 cal.add(GregorianCalendar.DAY_OF_MONTH, -pref.getInt());
                 cachedResults.setDate(2, new java.sql.Date(
                         cal.getTimeInMillis()));
                 cal.setTime(today);
                 pref.set(entityId, Constants.PREFERENCE_NEXT_REMINDER);
                 cal.add(GregorianCalendar.DAY_OF_MONTH, -pref.getInt());
                 cachedResults.setDate(3, new java.sql.Date(
                         cal.getTimeInMillis()));
                 
                 cachedResults.setInt(4, entityId.intValue());
                 
                 execute();
                 while (cachedResults.next()) {
                     int invoiceId = cachedResults.getInt(1);
                     set(new Integer(invoiceId));
                     NotificationBL notif = new NotificationBL();
                     long mils = invoice.getDueDate().getTime() - 
                             today.getTime();
                     int days = Math.round(mils / 1000 / 60 / 60 / 24);
                     
                     try {
                         MessageDTO message = notif.getInvoiceRemainderMessage(
                                 entityId, invoice.getUser().getUserId(),
                                 new Integer(days), invoice.getDueDate(),
                                 invoice.getNumber(), invoice.getTotal(),
                                 invoice.getCreateDateTime(),
                                 invoice.getCurrencyId());
                         
                         NotificationSessionLocalHome notificationHome =
                             (NotificationSessionLocalHome) EJBFactory.lookUpLocalHome(
                             NotificationSessionLocalHome.class,
                             NotificationSessionLocalHome.JNDI_NAME);
             
                         NotificationSessionLocal notificationSess = 
                             notificationHome.create();
                         notificationSess.notify(invoice.getUser(), message);
                         
                         invoice.setLastReminder(today);
                     } catch (NotificationNotFoundException e) {
                         log.warn("There are invoice to send reminders, but " +
                                 "the notification message is missing for " +
                                 "entity " + entityId);
                     }
                 }
                 cachedResults.close();
             }
         }
         
         if (conn != null) { // only if somthing run
             conn.close();
         }
 
     }
     
     public void processOverdue(Date today, Integer entityId) 
             throws SQLException, NamingException, PluggableTaskException,
                 SessionInternalError {
         log.debug("Processing overdue invoices for entity " + entityId);
         
         prepareStatement(InvoiceSQL.overdue);
         cachedResults.setInt(1, entityId.intValue());
         cachedResults.setDate(2, new java.sql.Date(today.getTime()));
         execute();
         while (cachedResults.next()) {
             int invoiceId = cachedResults.getInt(1);
             PluggableTaskManager taskManager =
                 new PluggableTaskManager(entityId,
                     Constants.PLUGGABLE_TASK_PENALTY);
             BasicPenaltyTask task =
                 (BasicPenaltyTask) taskManager.getNextClass();
             while (task != null) {
                 try {
                     task.process(new Integer(invoiceId));
                 } catch (TaskException e2) {
                     log.error("Error with penalty task for entity " + entityId);
                     return;
                 }
                 task = (BasicPenaltyTask) taskManager.getNextClass();
             }
         }
         
         cachedResults.close();
         conn.close();
 
     }
 
     public InvoiceWS getWS() 
             throws SessionInternalError{
         InvoiceDTOEx dto;
         try {
             dto = getDTOEx(null, false);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         return new InvoiceWS(dto);
     }
     
     public InvoiceDTOEx getDTOEx(Integer languageId, boolean forDisplay) 
             throws NamingException{
         Integer delegatedInvoice = null;
         if (invoice.getDelegatedInvoice() != null) {
             delegatedInvoice = invoice.getDelegatedInvoice().getId();
         }
        
         InvoiceDTOEx invoiceDTO = new InvoiceDTOEx(getDTO(),
                 delegatedInvoice); 
 
         invoiceDTO.setUserId(invoice.getUser().getUserId());
         invoiceDTO.setBalance(new Float(Util.round(invoice.getBalance().
                 floatValue(), 2)));
         invoiceDTO.setTotal(new Float(Util.round(invoice.getTotal().
                 floatValue(), 2)));
 
         // now add the payment maps
         PaymentBL paymentBL = new PaymentBL();
         Collection payments = invoice.getPaymentMap();
         for (Iterator it = payments.iterator(); it.hasNext();) {
             PaymentInvoiceMapEntityLocal map = (PaymentInvoiceMapEntityLocal) 
                     it.next();
             try {
                 PaymentInvoiceMapDTOEx dto = paymentBL.getMapDTO(map.getId());
                 invoiceDTO.addPayment(dto);
             } catch (FinderException e) {
                 log.error("No map", e);
             }      
         }
         // add also the invoice lines
         boolean hasSubaccounts = false;
         for (Iterator it = invoice.getInvoiceLines().iterator(); 
                 it.hasNext(); ) {
             InvoiceLineEntityLocal line = (InvoiceLineEntityLocal) it.next();
             invoiceDTO.getInvoiceLines().add(getInvoiceLineDTO(line));
             if (!hasSubaccounts && line.getType().getId().equals(
                     Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT)) {
                 hasSubaccounts = true;
             }
         }
         
         if (forDisplay) {
             // make sure that the lines are properly ordered
             Collections.sort(invoiceDTO.getInvoiceLines(), new InvoiceLineComparator());
             try {
                 UserBL userBl = new UserBL(invoice.getUser());
                 Locale locale = userBl.getLocale();
                 ResourceBundle bundle = ResourceBundle.getBundle(
                             "entityNotifications", locale);
 
                 // now add headres and footers if this invoices has subaccount lines
                 if (hasSubaccounts) {
                     addHeadersFooters(invoiceDTO.getInvoiceLines(), bundle);
                 }
                 InvoiceLineDTOEx total = new InvoiceLineDTOEx();
                 total.setDescription(bundle.getString("invoice.line.total"));
                 total.setAmount(invoice.getTotal());
                 invoiceDTO.getInvoiceLines().add(total);
             } catch (Exception e) {
                 log.error("getting locale", e);
             }
             // add a grand total final line
             
         }
         // add also the orders
         try {
             OrderBL bl = new OrderBL();
             for (Iterator it = invoice.getOrders().iterator(); 
                     it.hasNext(); ) {
                 OrderEntityLocal order = ((OrderProcessEntityLocal) 
                         it.next()).getOrder();
                 
                 bl.set(order);
                 invoiceDTO.getOrders().add(bl.getDTO());
             }
         } catch (NamingException e1) {
             log.error("Can't seem to be able to handle the orders of an invoice");
         }
         
         // add all the invoices
         Integer incInvoices[] = null;
         int f = 0;
         for (Iterator it = invoice.getIncludedInvoices().iterator();
                 it.hasNext(); ) {
             if (incInvoices == null) {
                 incInvoices = new Integer[invoice.getIncludedInvoices().size()];
             }
             InvoiceEntityLocal incInvoice = (InvoiceEntityLocal) it.next();
             incInvoices[f++] = incInvoice.getId();
         }
         invoiceDTO.setInvoicesIncluded(incInvoices);
         
         // add some currency info for the human
         try {
             CurrencyBL currency = new CurrencyBL(invoiceDTO.getCurrencyId());
             if (languageId != null) {
                 invoiceDTO.setCurrencyName(currency.getEntity().getDescription(
                         languageId));
             }
             invoiceDTO.setCurrencySymbol(currency.getEntity().getSymbol());
         } catch (Exception e) {
             log.error("Error setting currency of invoice dto", e);
         }
         
         return invoiceDTO;        
         
     }
     
     /**
      * Will add lines with headers and footers to make an invoice with 
      * subaccounts more readable. The lines have to be already sorted.
      * @param lines
      */
     private void addHeadersFooters(Vector lines, ResourceBundle bundle) {
         Integer nowProcessing = new Integer(-1);
         Float total = null;
         int totalLines = lines.size();
         int subaccountNumber = 0;
         
         log.debug("adding headers & footers." + totalLines);
         
         for (int idx = 0; idx < totalLines; idx++) {
             InvoiceLineDTOEx line = (InvoiceLineDTOEx) lines.get(idx);
             if (line.getTypeId().equals(
                     Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT) &&
                     !line.getSourceUserId().equals(nowProcessing)) {
                 // line break
                 nowProcessing = line.getSourceUserId();
                 subaccountNumber++;
                 // put the total first
                 if (total != null) { // it could be the first subaccount
                     InvoiceLineDTOEx totalLine = new InvoiceLineDTOEx();
                     totalLine.setDescription(bundle.getString(
                             "invoice.line.subAccount.footer"));
                     totalLine.setAmount(total);
                     lines.add(idx, totalLine);
                     idx++;
                     totalLines++;
                 }
                 total = new Float(0);
                 
                 // now the header anouncing a new subaccout
                 InvoiceLineDTOEx headerLine = new InvoiceLineDTOEx();
                 try {
                     ContactBL contact = new ContactBL();
                     contact.set(nowProcessing);
                     StringBuffer text = new StringBuffer();
                     text.append(subaccountNumber + " - ");
                     text.append(bundle.getString(
                         "invoice.line.subAccount.header1"));
                     text.append(" " + bundle.getString(
                         "invoice.line.subAccount.header2") + " " + nowProcessing);
                     if (contact.getEntity().getFirstName() != null) {
                         text.append(" " + contact.getEntity().getFirstName());
                     }
                     if (contact.getEntity().getLastName() != null) {
                         text.append(" " + contact.getEntity().getLastName());
                     }
                     headerLine.setDescription(text.toString());
                     lines.add(idx, headerLine);
                     idx++;
                     totalLines++;
                 } catch (Exception e) {
                     log.error("Exception", e);
                     return;
                 }
             }
             
             // update the total
             if (total != null) {
                 // there had been at least one sub-account processed
                 if (line.getTypeId().equals(
                         Constants.INVOICE_LINE_TYPE_SUB_ACCOUNT)) {
                     total = new Float(total.floatValue() + 
                             line.getAmount().floatValue());
                 } else {
                     // this is the last total to display, from now on the
                     // lines are not of subaccounts
                     InvoiceLineDTOEx totalLine = new InvoiceLineDTOEx();
                     totalLine.setDescription(bundle.getString(
                             "invoice.line.subAccount.footer"));
                     totalLine.setAmount(total);
                     lines.add(idx, totalLine);
                     total = null; // to avoid repeating
                 }
             }
         }
         // if there are no lines after the last subaccount, we need
         // a total for it
         if (total != null) { // only if it wasn't added before
             InvoiceLineDTOEx totalLine = new InvoiceLineDTOEx();
             totalLine.setDescription(bundle.getString(
                     "invoice.line.subAccount.footer"));
             totalLine.setAmount(total);
             lines.add(totalLine);
         }
 
         log.debug("done " + lines.size());
     }
     
     public InvoiceLineDTOEx getInvoiceLineDTO(
             InvoiceLineEntityLocal line) {
         InvoiceLineDTOEx dto = new InvoiceLineDTOEx();
         
         dto.setAmount(line.getAmount());
         dto.setDeleted(line.getDeleted());
         dto.setDescription(line.getDescription());
         dto.setId(line.getId());
         dto.setPrice(line.getPrice());
         dto.setQuantity(line.getQuantity());
         dto.setTypeId(line.getType().getId());
         dto.setItemId(line.getItemId());
         dto.setOrderPosition(line.getType().getOrderPosition());
         dto.setSourceUserId(line.getSourceUserId());
         dto.setIsPercentage(line.getIsPercentage());
         
         return dto;
     }
 
     public InvoiceDTO getDTO() {
         InvoiceDTO ret = new InvoiceDTO(invoice.getId(), invoice.getCreateDateTime(),
                 invoice.getCreateTimeStamp(),
                 invoice.getLastReminder(), invoice.getDueDate(),
                 invoice.getTotal(), invoice.getToProcess(),
                 invoice.getBalance(), invoice.getCarriedBalance(),
                 invoice.getInProcessPayment(), 
                 invoice.getDeleted(), invoice.getPaymentAttempts(), 
                 invoice.getIsReview(), invoice.getCurrencyId(),
                 invoice.getCustomerNotes(), invoice.getNumber(),
                 invoice.getOverdueStep()); 
         ret.setBalance(new Float(Util.round(invoice.getBalance().floatValue(), 2)));
         ret.setTotal(new Float(Util.round(invoice.getTotal().floatValue(), 2)));
         return ret;
     }    
 
     // given the current invoice, it will 'rewind' to the previous one
     public void setPrevious() 
             throws SQLException, NamingException, FinderException {
 
         prepareStatement(InvoiceSQL.previous);
         cachedResults.setInt(1, invoice.getUser().getUserId().intValue());
         cachedResults.setInt(2, invoice.getId().intValue());
         boolean found = false;
             
         execute();
         if (cachedResults.next()) {
             int value = cachedResults.getInt(1);
             if (!cachedResults.wasNull()) {
                 set(new Integer(value));
                 found = true;
             }
         } 
         cachedResults.close();
         conn.close();
         
         if (!found) {
             throw new FinderException("No previous invoice found");
         }
     }
 }
