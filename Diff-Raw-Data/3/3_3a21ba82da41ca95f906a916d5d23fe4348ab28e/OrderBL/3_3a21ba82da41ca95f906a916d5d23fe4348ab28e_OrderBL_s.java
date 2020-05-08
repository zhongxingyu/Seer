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
 
 /*
  * Created on 15-Mar-2003
  *
  * Copyright Sapienter Enterprise Software
  */
 package com.sapienter.jbilling.server.order;
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.EntityEntityLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocal;
 import com.sapienter.jbilling.interfaces.NotificationSessionLocalHome;
 import com.sapienter.jbilling.interfaces.OrderEntityLocal;
 import com.sapienter.jbilling.interfaces.OrderEntityLocalHome;
 import com.sapienter.jbilling.interfaces.OrderLineEntityLocal;
 import com.sapienter.jbilling.interfaces.OrderLineEntityLocalHome;
 import com.sapienter.jbilling.interfaces.OrderLineTypeEntityLocal;
 import com.sapienter.jbilling.interfaces.OrderLineTypeEntityLocalHome;
 import com.sapienter.jbilling.interfaces.OrderPeriodEntityLocal;
 import com.sapienter.jbilling.interfaces.OrderPeriodEntityLocalHome;
 import com.sapienter.jbilling.server.entity.OrderDTO;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PromotionBL;
 import com.sapienter.jbilling.server.list.ResultList;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.pluggableTask.OrderProcessingTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskManager;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.process.ConfigurationBL;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.EntityBL;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.DTOFactory;
 import com.sapienter.jbilling.server.util.EventLogger;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 
 /**
  * @author Emil
  */
 public class OrderBL extends ResultList 
         implements OrderSQL {
     private NewOrderDTO newOrder = null;
     private JNDILookup EJBFactory = null;
     private OrderEntityLocalHome orderHome = null;
     private OrderEntityLocal order = null;
     private OrderLineEntityLocalHome orderLineHome = null;
     private OrderLineTypeEntityLocalHome orderLineTypeHome = null;
     private OrderPeriodEntityLocalHome orderPeriodHome = null;
     private Logger log = null;
     private EventLogger eLogger = null;
 
     public OrderBL(Integer orderId) 
             throws NamingException, FinderException {
         init();
         set(orderId);
     }
 
     public OrderBL() throws NamingException {
         init();
     }
     
     public OrderBL(NewOrderDTO order) throws NamingException {
         newOrder = order;
         init();
     }
 
 
     private void init() throws NamingException {
         log = Logger.getLogger(OrderBL.class);     
         eLogger = EventLogger.getInstance();        
         EJBFactory = JNDILookup.getFactory(false);
         orderHome = (OrderEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 OrderEntityLocalHome.class,
                 OrderEntityLocalHome.JNDI_NAME);
         orderLineHome = (OrderLineEntityLocalHome) EJBFactory.lookUpLocalHome(
                 OrderLineEntityLocalHome.class,
                 OrderLineEntityLocalHome.JNDI_NAME);
 
         orderLineTypeHome = (OrderLineTypeEntityLocalHome) 
                 EJBFactory.lookUpLocalHome(
                 OrderLineTypeEntityLocalHome.class,
                 OrderLineTypeEntityLocalHome.JNDI_NAME);
         orderPeriodHome =
                 (OrderPeriodEntityLocalHome) EJBFactory.lookUpLocalHome(
                 OrderPeriodEntityLocalHome.class,
                 OrderPeriodEntityLocalHome.JNDI_NAME);
     
     }
 
     public OrderEntityLocal getEntity() {
         return order;
     }
     
     public OrderEntityLocalHome getHome() {
         return orderHome;
     }
     
     public OrderPeriodDTOEx getPeriod(Integer language, Integer id) 
             throws FinderException {
         OrderPeriodEntityLocal period = orderPeriodHome.findByPrimaryKey(id);
         OrderPeriodDTOEx dto = new OrderPeriodDTOEx();
         dto.setDescription(period.getDescription(language));
         dto.setEntityId(period.getEntityId());
         dto.setId(period.getId());
         dto.setUnitId(period.getUnitId());
         dto.setValue(period.getValue());
         
         return dto;
     }
 
     public void set(Integer id) throws FinderException {
         order = orderHome.findByPrimaryKey(id);
     }
     
     public void set(OrderEntityLocal newOrder) {
         order = newOrder;
     }
 
 
     public NewOrderDTO getNewOrderDTO() {
         return newOrder;
     }
     
     public OrderWS getWS(Integer languageId) 
             throws FinderException, NamingException {
         OrderWS retValue = new OrderWS(getDTO());
         
         retValue.setPeriod(order.getPeriod().getId());
         retValue.setPeriodStr(order.getPeriod().getDescription(languageId));
         retValue.setBillingTypeStr(DTOFactory.getBillingTypeString(
                 order.getBillingTypeId(), languageId));
         retValue.setUserId(order.getUser().getUserId());
         
         int f = 0;
         Vector lines = new Vector();
         for (Iterator it = order.getOrderLines().iterator(); it.hasNext();) {
             OrderLineEntityLocal line = (OrderLineEntityLocal) it.next();
             if (line.getDeleted().intValue() == 0) {
                 OrderLineWS lineWS = new OrderLineWS(DTOFactory.getOrderLineDTOEx(line));
                 lineWS.setTypeId(line.getType().getId());
                 lines.add(lineWS);
             }
         }
         retValue.setOrderLines(new OrderLineWS[lines.size()]);
         lines.toArray(retValue.getOrderLines());
         
         return retValue;
     }
     
     public OrderDTO getDTO() {
         return new OrderDTO(order.getId(), order.getBillingTypeId(), 
         	order.getNotify(),
             order.getActiveSince(), order.getActiveUntil(), 
             order.getCreateDate(), order.getNextBillableDay(),
             order.getCreatedBy(), order.getStatusId(), order.getDeleted(),
             order.getCurrencyId(), order.getLastNotified(),
             order.getNotificationStep(), order.getDueDateUnitId(),
             order.getDueDateValue(), order.getDfFm(),
             order.getAnticipatePeriods(), order.getOwnInvoice(),
             order.getNotesInInvoice(), order.getNotes());
     }
 
     public void setDTO(NewOrderDTO mOrder) {
         newOrder = mOrder;
     }
 
     public void addItem(ItemDTOEx item, Integer quantity)
         throws SessionInternalError {
 
         // check if the item is already in the order
         OrderLineDTOEx line =
             (OrderLineDTOEx) newOrder.orderLines.get(item.getId());
 
         float additionAmount = (item.getPercentage() == null) ?
                 item.getPrice().floatValue() * quantity.intValue() :
                 item.getPercentage().floatValue();
 
         if (line == null) { // not yet there
             Boolean editable = lookUpEditable(item.getOrderLineTypeId());
 
             line = new OrderLineDTOEx(null, item.getId(), item.getDescription(), 
                     new Float(additionAmount), quantity,
                     (item.getPercentage() == null) ? item.getPrice() :
                         item.getPercentage(), 
                     new Integer(0), null, new Integer(0), 
                     item.getOrderLineTypeId(), editable);          
             line.setItem(item);
             newOrder.orderLines.put(item.getId(), line);
 
         } else {
             // the item is there, I just have to update the quantity
             line.setQuantity(
                 new Integer(
                     line.getQuantity().intValue() + quantity.intValue()));
             // and also the total amount for this order line
             line.setAmount(
                 new Float(line.getAmount().floatValue() + additionAmount));
 
         }
 
     }
 
     public void deleteItem(Integer itemID) {
         newOrder.orderLines.remove(itemID);
     }
     
     public void delete() {
         for (Iterator it = order.getOrderLines().iterator(); it.hasNext();) {
             OrderLineEntityLocal line = (OrderLineEntityLocal) it.next();
             line.setDeleted(new Integer(1));
         }
         
         order.setDeleted(new Integer(1));
     }
 
     /**
      * Method recalculate.
      * Goes over the processing tasks configured in the database for this
      * entity. The NewOrderDTO of this session is then modified.
      */
     public void recalculate(Integer entityId) throws SessionInternalError {
         log = Logger.getLogger(OrderBL.class);
         log.debug("Processing and order for reviewing." + newOrder.getOrderLinesMap().size());
 
         try {
             PluggableTaskManager taskManager = new PluggableTaskManager(
                     entityId, Constants.PLUGGABLE_TASK_PROCESSING_ORDERS);
             OrderProcessingTask task = 
                     (OrderProcessingTask) taskManager.getNextClass();
             while (task != null) {
                 task.doProcessing(newOrder);
                 task = (OrderProcessingTask) taskManager.getNextClass();
             }
 
         } catch (PluggableTaskException e) {
             log.fatal("Problems handling order processing task.", e);
             throw new SessionInternalError("Problems handling order " +
                     "processing task.");
         } catch (TaskException e) {
 			log.fatal("Problems excecuting order processing task.", e);
 			throw new SessionInternalError("Problems executing order processing task.");
         }
     }
 
     public Integer create(Integer entityId, Integer userAgentId,
             NewOrderDTO orderDto) throws SessionInternalError {
         Integer newOrderId = null;
 
         try {
             // if the order is a one-timer, force pre-paid to avoid any
             // confusion
             if (orderDto.getPeriod().equals(Constants.ORDER_PERIOD_ONCE)) {
                 orderDto.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             }
             /*
              * First create the order record
              */
             // create the record 
             order = orderHome.create(entityId, orderDto.getPeriod(),
                     userAgentId, orderDto.getUserId(),
                     orderDto.getBillingTypeId(), orderDto.getCurrencyId());
             order.setActiveUntil(orderDto.getActiveUntil());
             order.setActiveSince(orderDto.getActiveSince());
             order.setNotify(orderDto.getNotify());
             order.setDueDateUnitId(orderDto.getDueDateUnitId());
             order.setDueDateValue(orderDto.getDueDateValue());
             order.setDfFm(orderDto.getDfFm());
             order.setAnticipatePeriods(orderDto.getAnticipatePeriods());
             order.setOwnInvoice(orderDto.getOwnInvoice());
             order.setNotes(orderDto.getNotes());
             order.setNotesInInvoice(orderDto.getNotesInInvoice());
 
             // get the collection of lines to update the fk of the table
             newOrderId = order.getId();
             createLines(orderDto);
             
             // update any promotion involved
             updatePromotion(entityId, orderDto.getPromoCode());
         } catch (Exception e) {
             log.fatal("Create exception creating order entity bean", e);
             throw new SessionInternalError(e);
         } 
         
         return newOrderId;
     }
 
     public void update(Integer executorId, NewOrderDTO dto) 
             throws FinderException, CreateException {
         // update first the order own fields
         if (!Util.equal(order.getActiveUntil(), dto.getActiveUntil())) {
             audit(executorId, order.getActiveUntil());
             order.setActiveUntil(dto.getActiveUntil());
         }
         if (!Util.equal(order.getActiveSince(), dto.getActiveSince())) {
             audit(executorId, order.getActiveSince());
             order.setActiveSince(dto.getActiveSince());
         }
         if (dto.getStatusId() != null && !order.getStatusId().equals(
                 dto.getStatusId())) {
             audit(executorId, order.getStatusId());
             order.setStatusId(dto.getStatusId());
         }
         
         OrderPeriodEntityLocal period = orderPeriodHome.findByPrimaryKey(
                 dto.getPeriod());
         if (order.getPeriod().getId().compareTo(period.getId()) != 0) {
             audit(executorId, order.getPeriod().getId());
             order.setPeriod(period);
         }
         order.setBillingTypeId(dto.getBillingTypeId());
         order.setNotify(dto.getNotify());
         order.setDueDateUnitId(dto.getDueDateUnitId());
         order.setDueDateValue(dto.getDueDateValue());
         order.setDfFm(dto.getDfFm());
         order.setAnticipatePeriods(dto.getAnticipatePeriods());
         order.setOwnInvoice(dto.getOwnInvoice());
         order.setNotes(dto.getNotes());
         order.setNotesInInvoice(dto.getNotesInInvoice());
         
         // now update this order's lines
         // first, mark all the lines as deleted
         for (Iterator it = order.getOrderLines().iterator(); it.hasNext();) {
             OrderLineEntityLocal line = (OrderLineEntityLocal) it.next();
             line.setDeleted(new Integer(1));
         }  
         createLines(dto);
  
         eLogger.audit(executorId, Constants.TABLE_PUCHASE_ORDER, 
                 order.getId(),
                 EventLogger.MODULE_ORDER_MAINTENANCE, 
                 EventLogger.ROW_UPDATED, null,  
                 null, null);
         
     }
     
     private void createLines(NewOrderDTO orderDto) 
             throws FinderException, CreateException {
         Collection orderLines = order.getOrderLines();
 
         /*
          * now go over the order lines
          */
         Collection values = null;
         Hashtable lines = orderDto.getOrderLinesMap();
         if (lines == null) {
             values = orderDto.getRawOrderLines();
         } else {
             values = lines.values();
         }
         for (Iterator i = values.iterator(); i.hasNext();) {
             OrderLineDTOEx line = (OrderLineDTOEx) i.next();
             // get the type id bean for the relationship
             OrderLineTypeEntityLocal lineType =
                 orderLineTypeHome.findByPrimaryKey(line.getTypeId());
 
             // first, create the line record
             OrderLineEntityLocal newOrderLine =
                 orderLineHome.create(
                     line.getItemId(),
                     lineType,
                     line.getDescription(),
                     line.getAmount(),
                     line.getQuantity(),
                     line.getPrice(),
                     line.getItemPrice(),
                     new Integer(0));
             // then update the order fk column
             orderLines.add(newOrderLine);
         }
     }       
     
     private void updatePromotion(Integer entityId, String code) 
             throws NamingException {
         if (code != null && code.length() > 0) {
             PromotionBL promotion = new PromotionBL();
             if (promotion.isPresent(entityId, code)) {
                 promotion.getEntity().getUsers().add(order.getUser());
             } else {
                 log.error("Can't find promotion entity = " + entityId +
                         " code " + code);
             }
         }
     } 
     
     /**
      * Method lookUpEditable.
      * Gets the row from order_line_type for the type specifed
      * @param type 
      * The order line type to look.
      * @return Boolean
      * If it is editable or not
      * @throws SessionInternalError
      * If there was a problem accessing the entity bean
      */
     static public Boolean lookUpEditable(Integer type)
         throws SessionInternalError {
         Boolean editable = null;
         Logger log = Logger.getLogger(OrderBL.class);
 
         try {
 
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             OrderLineTypeEntityLocalHome orderLineTypeHome =
                 (OrderLineTypeEntityLocalHome) EJBFactory.lookUpLocalHome(
                     OrderLineTypeEntityLocalHome.class,
                     OrderLineTypeEntityLocalHome.JNDI_NAME);
 
             OrderLineTypeEntityLocal typeBean =
                 orderLineTypeHome.findByPrimaryKey(type);
             editable = new Boolean(typeBean.getEditable().intValue() == 1);
         } catch (Exception e) {
             log.fatal(
                 "Exception looking up the editable flag of an order "
                     + "line type. Type = "
                     + type,
                 e);
             throw new SessionInternalError("Looking up editable flag");
         }
 
         return editable;
     }
 
     public CachedRowSet getList(Integer entityID, Integer userRole, 
             Integer userId) 
             throws SQLException, Exception{
                 
         if(userRole.equals(Constants.TYPE_INTERNAL) ||
                 userRole.equals(Constants.TYPE_ROOT) ||
                 userRole.equals(Constants.TYPE_CLERK)) {
             prepareStatement(OrderSQL.listInternal);
             cachedResults.setInt(1,entityID.intValue());
         } else if(userRole.equals(Constants.TYPE_PARTNER)) {
             prepareStatement(OrderSQL.listPartner);
             cachedResults.setInt(1, entityID.intValue());
             cachedResults.setInt(2, userId.intValue());
         } else if(userRole.equals(Constants.TYPE_CUSTOMER)) {
             prepareStatement(OrderSQL.listCustomer);
             cachedResults.setInt(1, userId.intValue());
         } else {
             throw new Exception("The orders list for the type " + userRole + 
                     " is not supported");
         }
         
         execute();
         conn.close();
         return cachedResults;
     }
     
     public Integer getLatest(Integer userId) 
             throws SessionInternalError {
         Integer retValue = null;
         try {
             prepareStatement(OrderSQL.getLatest);
             cachedResults.setInt(1, userId.intValue());
             execute();
             if (cachedResults.next()) {
                 int value = cachedResults.getInt(1);
                 if (!cachedResults.wasNull()) {
                     retValue = new Integer(value);
                 }
             } 
             cachedResults.close();
             conn.close();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
         
         return retValue;
     }
 
 
     public CachedRowSet getOrdersByProcessId(Integer processId)
             throws SQLException, Exception{
 
         prepareStatement(OrderSQL.listByProcess);
         cachedResults.setInt(1,processId.intValue());
         execute();
         conn.close();
         return cachedResults;
     }
     
     public void setStatus(Integer executorId, Integer statusId) {
         if (executorId != null) {
             eLogger.audit(executorId, Constants.TABLE_PUCHASE_ORDER, 
                     order.getId(), 
                     EventLogger.MODULE_ORDER_MAINTENANCE, 
                     EventLogger.ORDER_STATUS_CHANGE, 
                     order.getStatusId(), null, null);
         } else {
             eLogger.auditBySystem(order.getUser().getEntity().getId(), 
                     Constants.TABLE_PUCHASE_ORDER, 
                     order.getId(), 
                     EventLogger.MODULE_ORDER_MAINTENANCE, 
                     EventLogger.ORDER_STATUS_CHANGE, 
                     order.getStatusId(), null, null);
 
         }
         order.setStatusId(statusId);
 
     }
     
     /**
      * To be called from the http api, this simply looks for lines
      * in the order that lack some fields, it finds that info based
      * in the item. 
      * @param dto
      */
     public void fillInLines(NewOrderDTO dto, Integer entityId) 
             throws NamingException, FinderException, SessionInternalError {
         /*
          * now go over the order lines
          */
         Hashtable lines = dto.getOrderLinesMap();
         Collection values = lines.values();
         ItemBL itemBl = new ItemBL();
         for (Iterator i = values.iterator(); i.hasNext();) {
             OrderLineDTOEx line = (OrderLineDTOEx) i.next();
             itemBl.set(line.getItemId());
             Integer languageId = itemBl.getEntity().getEntity().
                     getLanguageId();
             // this is needed for the basic pluggable task to work
             line.setItem(itemBl.getDTO(languageId, dto.getUserId(), 
                     entityId, dto.getCurrencyId()));
             if (line.getPrice() == null) {
                 line.setPrice(itemBl.getPrice(dto.getUserId(), 
                         dto.getCurrencyId(), entityId));
             }
             if (line.getDescription() == null) {
                 line.setDescription(itemBl.getEntity().getDescription(
                         languageId));
             }
         }
      }
     
     private void audit(Integer executorId, Date date) {
         eLogger.audit(executorId, Constants.TABLE_PUCHASE_ORDER, 
                 order.getId(),
                 EventLogger.MODULE_ORDER_MAINTENANCE, 
                 EventLogger.ROW_UPDATED, null,  
                 null, date);
     }        
     private void audit(Integer executorId, Integer in) {
         eLogger.audit(executorId, Constants.TABLE_PUCHASE_ORDER, 
                 order.getId(),
                 EventLogger.MODULE_ORDER_MAINTENANCE, 
                 EventLogger.ROW_UPDATED, in,  
                 null, null);
     }        
 
     public static boolean validate(OrderWS dto) {
         boolean retValue = true;
         
         if (dto.getUserId() == null || dto.getPeriod() == null ||
                 dto.getBillingTypeId() == null || 
                 dto.getOrderLines() == null) {
             retValue = false;
         } else {
             for (int f = 0 ; f < dto.getOrderLines().length; f++) {
                 if (!validate(dto.getOrderLines()[f])) {
                     retValue = false;
                     break;
                 }
             }
         }
         return retValue;
     }
     
     public static boolean validate(OrderLineWS dto) {
         boolean retValue = true;
         
         if (dto.getTypeId() == null || dto.getAmount() == null || 
                 dto.getDescription() == null || dto.getQuantity() == null) {
             retValue = false;
         }
         
         return retValue;
     }
     
     public void reviewNotifications(Date today) 
     		throws NamingException, FinderException, SQLException, Exception  {
         NotificationSessionLocalHome notificationHome =
             (NotificationSessionLocalHome) EJBFactory.lookUpLocalHome(
             NotificationSessionLocalHome.class,
             NotificationSessionLocalHome.JNDI_NAME);
 
         NotificationSessionLocal notificationSess = 
             	notificationHome.create();
 
     	EntityBL entity = new EntityBL();
     	Collection entities = entity.getHome().findEntities();
     	for (Iterator it = entities.iterator(); it.hasNext(); ) {
     		EntityEntityLocal ent = (EntityEntityLocal) it.next();
     		// find the orders for this entity
     	    prepareStatement(OrderSQL.getAboutToExpire);
     	    
     	    cachedResults.setDate(1, new java.sql.Date(today.getTime()));
     	    // calculate the until date
     	    
     	    // get the this entity preferences for each of the steps
     	    PreferenceBL pref = new PreferenceBL();
             int totalSteps = 3;
             int stepDays[] = new int[totalSteps];
             boolean config = false;
             int minStep = -1;
             for (int f = 0; f < totalSteps; f++) {
         	    try {
         	    	pref.set(ent.getId(), new Integer(
                             Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1.intValue() +
                             f));
                     if (pref.isNull()) {
                         stepDays[f] = -1;
                     } else {
                     	stepDays[f] = pref.getInt();
                         config = true;
                         if (minStep == -1) {
                         	minStep = f;
                         }
                     }
         	    } catch (FinderException e) {
                     stepDays[f] = -1;
         	    }
             }
             
             if (!config) {
                 log.warn("Preference missing to send a notification for " +
                         "entity " + ent.getId());
                 continue;
             }
 
     	    Calendar cal = Calendar.getInstance();
     	    cal.clear();
     	    cal.setTime(today);
     	    cal.add(Calendar.DAY_OF_MONTH, stepDays[minStep]);
     	    cachedResults.setDate(2, new java.sql.Date(
     	    		cal.getTime().getTime()));
     	    
     	    // the entity
     	    cachedResults.setInt(3, ent.getId().intValue());
             // the total number of steps
             cachedResults.setInt(4, totalSteps);
     	            
     	    execute();
     	    while (cachedResults.next()) {
 		    	int orderId = cachedResults.getInt(1);
                 Date activeUntil = cachedResults.getDate(2);
                 int currentStep = cachedResults.getInt(3);
                 int days = -1;
 
                 // find out how many days apply for this order step
                 for (int f = currentStep; f < totalSteps; f++) {
                     if (stepDays[f] >= 0) {
                         days = stepDays[f];
                         currentStep = f + 1;
                         break;
                     }
                 }
                 
                 if (days == -1) {
                 	throw new SessionInternalError("There are no more steps " +
                             "configured, but the order was selected. Order " +
                             " id = " + orderId);
                 }
                 
                 // check that this order requires a notification
                 cal.setTime(today);
                 cal.add(Calendar.DAY_OF_MONTH, days);
                 if (activeUntil.compareTo(today) >= 0 &&
                         activeUntil.compareTo(cal.getTime()) <= 0) {
                 	/*/ ok
                     log.debug("Selecting order " + orderId + " today = " + 
                             today + " active unitl = " + activeUntil + 
                             " days = " + days);
                     */
                 } else {
                     /*
                     log.debug("Skipping order " + orderId + " today = " + 
                             today + " active unitl = " + activeUntil + 
                             " days = " + days);
                             */
                 	continue;
                 }
 
 		    	set(new Integer(orderId));
 		    	UserBL user = new UserBL(order.getUser());
 		    	try {
 		    		NotificationBL notification = new NotificationBL();
                     ContactBL contact = new ContactBL();
                     contact.set(user.getEntity().getUserId());
                     OrderDTOEx dto = DTOFactory.getOrderDTOEx(
                             new Integer(orderId), new Integer(1));
 		    		MessageDTO message = notification.getOrderNotification(
 		    				ent.getId(), 
                             new Integer(currentStep), 
 		    				user.getEntity().getLanguageIdField(), 
 							order.getActiveSince(),
 							order.getActiveUntil(),
                             user.getEntity().getUserId(),
                             dto.getTotal(), order.getCurrencyId());
                     // update the order record only if the message is sent 
                     if (notificationSess.notify(user.getEntity(), message).
                             booleanValue()) {
     		            // if in the last step, turn the notification off, so
                         // it is skiped in the next process
                         if (currentStep >= totalSteps) {
                         	order.setNotify(new Integer(0));
                         }
                         order.setNotificationStep(new Integer(currentStep));
     		            order.setLastNotified(Calendar.getInstance().getTime());
                     }
 
 		    	} catch (NotificationNotFoundException e) {
 		    		log.warn("Without a message to send, this entity can't" +
 		    				" notify about orders. Skipping");
 		    		break;
 		    	}
 		    	
 		    } 
     	}
 	    cachedResults.close();
         // The connection was found null when testing on Oracle
         if (conn != null) {
             conn.close();
         }
     }
     
     public TimePeriod getDueDate() 
             throws NamingException, FinderException {
         TimePeriod retValue = new TimePeriod();
         if (order.getDueDateValue() == null) {
             // let's go see the customer
             if (order.getUser().getCustomer().getDueDateValue() == null) {
                 // still unset, let's go to the entity
                 ConfigurationBL config = new ConfigurationBL(
                         order.getUser().getEntity().getId());
                 retValue.setUnitId(config.getEntity().getDueDateUnitId());
                 retValue.setValue(config.getEntity().getDueDateValue());
             } else {
                 retValue.setUnitId(order.getUser().getCustomer().
                         getDueDateUnitId());
                 retValue.setValue(order.getUser().getCustomer().getDueDateValue());
             }
         } else {
             retValue.setUnitId(order.getDueDateUnitId());
             retValue.setValue(order.getDueDateValue());
         }
         
         // df fm only applies if the entity uses it
         PreferenceBL preference = new PreferenceBL();
         try {
             preference.set(order.getUser().getEntity().getId(), 
                     Constants.PREFERENCE_USE_DF_FM);
         } catch (FinderException e) {
             // no problem go ahead use the defualts
         }
         if (preference.getInt() == 1) {
             // now all over again for the Df Fm
             if (order.getDfFm() == null) {
                 // let's go see the customer
                 if (order.getUser().getCustomer().getDfFm() == null) {
                     // still unset, let's go to the entity
                     ConfigurationBL config = new ConfigurationBL(
                             order.getUser().getEntity().getId());
                     retValue.setDf_fm(config.getEntity().getDfFm());
                 } else {
                     retValue.setDf_fm(order.getUser().getCustomer().getDfFm());
                 }
             } else {
                 retValue.setDf_fm(order.getDfFm());
             }
         } else {
             retValue.setDf_fm((Boolean) null);
         }
         
         retValue.setOwn_invoice(order.getOwnInvoice());
         
         return retValue;
     }
     
     public Date getInvoicingDate() {
         Date retValue;;
         if (order.getNextBillableDay() != null) {
             retValue = order.getNextBillableDay();
         } else {
             if (order.getActiveSince() != null) {
                 retValue = order.getActiveSince();
             } else {
                 retValue = order.getCreateDate();
             }
         }
             
         return retValue;
     }
     
     public Integer[] getManyWS(Integer userId, Integer number, 
             Integer languageId) 
             throws NamingException, FinderException {
         // find the order records first
         UserBL user = new UserBL(userId);
         Collection orders = user.getEntity().getOrders();
         Vector ordersVector = new Vector(orders); // needed to use sort
         Collections.sort(ordersVector, new OrderEntityComparator());
         Collections.reverse(ordersVector);
         // now convert the entities to WS objects
         Integer retValue[] = new Integer[ordersVector.size() > 
                                          number.intValue() ? number.intValue() :
                                              ordersVector.size()];
         for (int f = 0; f < ordersVector.size() && f < number.intValue(); 
                 f++) {
             order = (OrderEntityLocal) ordersVector.get(f);
             retValue[f] = order.getId();
         }
         
         return retValue;
     }
     
     public Integer[] getByUserAndPeriod(Integer userId, Integer statusId) 
             throws SessionInternalError {
         // find the order records first
         try {
             Vector result = new Vector();
             prepareStatement(OrderSQL.getByUserAndPeriod);
             cachedResults.setInt(1, userId.intValue());
             cachedResults.setInt(2, statusId.intValue());
             execute();
             while (cachedResults.next()) {
                 result.add(new Integer(cachedResults.getInt(1)));
             } 
             cachedResults.close();
             conn.close();
             // now convert the vector to an int array
             Integer retValue[] = new Integer[result.size()];
             result.toArray(retValue);
 
             return retValue;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         } 
     }
 
     public OrderPeriodDTOEx[] getPeriods(Integer entityId, Integer languageId) {
         OrderPeriodDTOEx retValue[] = null;
         try {
             Collection periods = orderPeriodHome.findByEntity(entityId);
             retValue = new OrderPeriodDTOEx[periods.size()];
             int f = 0;
             for (Iterator it = periods.iterator(); it.hasNext(); f++) {
                 OrderPeriodEntityLocal period = 
                     (OrderPeriodEntityLocal) it.next();
                 retValue[f] = new OrderPeriodDTOEx();
                 retValue[f].setId(period.getId());
                 retValue[f].setEntityId(period.getEntityId());
                 retValue[f].setUnitId(period.getUnitId());
                 retValue[f].setValue(period.getValue());
                 retValue[f].setDescription(period.getDescription(languageId));
             }
         } catch (FinderException e) {
             retValue = new OrderPeriodDTOEx[0];
         }
         
         return retValue;
     }
     
     public void updatePeriods(Integer languageId, OrderPeriodDTOEx periods[]) 
             throws FinderException {
         for (int f = 0; f < periods.length; f++) {
             OrderPeriodEntityLocal period = orderPeriodHome.findByPrimaryKey(
                     periods[f].getId());
             period.setUnitId(periods[f].getUnitId());
             period.setValue(periods[f].getValue());
             period.setDescription(periods[f].getDescription(), languageId);
         }
     }
     
     public void addPeriod(Integer entitytId, Integer languageId) 
             throws CreateException {
         orderPeriodHome.create(entitytId, new Integer(1), new Integer(1), " ",
                 languageId);
     }
     
     public boolean deletePeriod(Integer periodId) 
             throws FinderException, RemoveException{
         OrderPeriodEntityLocal period = orderPeriodHome.findByPrimaryKey(
                 periodId);
         if (period.getOrders().size() > 0) {
             return false;
         } else {
             period.remove();
             return true;
         }
     }
     
     public OrderLineWS getOrderLineWS(Integer id) 
             throws FinderException {
         OrderLineEntityLocal line = orderLineHome.findByPrimaryKey(id);
         OrderLineWS retValue = new OrderLineWS(line.getId(),line.getItemId(), 
                 line.getDescription(), line.getAmount(), line.getQuantity(), 
                 line.getPrice(), line.getItemPrice(), line.getCreateDate(), 
                line.getDeleted(), line.getType().getId(), line.getEditable());
         return retValue;
     }
     
     public OrderLineEntityLocal getOrderLine(Integer id)
             throws FinderException {
         return orderLineHome.findByPrimaryKey(id);
     }
     
     public void updateOrderLine(OrderLineWS dto) 
             throws FinderException, RemoveException {
         OrderLineEntityLocal line = orderLineHome.findByPrimaryKey(dto.getId());
         if (dto.getQuantity() != null && dto.getQuantity().intValue() == 0) {
             // deletes the order line if the quantity is 0
             line.remove();
             
         } else {
             line.setAmount(dto.getAmount());
             line.setDeleted(dto.getDeleted());
             line.setDescription(dto.getDescription());
             line.setItemId(dto.getItemId());
             line.setItemPrice(dto.getItemPrice());
             line.setPrice(dto.getPrice());
             line.setQuantity(dto.getQuantity());
         }
     }
 }
