 /*
  * Copyright 2012-2013 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.business.services;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.persistence.LockModeType;
 import javax.persistence.NoResultException;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.LocalTime;
 import org.key2gym.business.api.BusinessException;
 import org.key2gym.business.api.SecurityRoles;
 import org.key2gym.business.api.SecurityViolationException;
 import org.key2gym.business.api.ValidationException;
 import org.key2gym.business.api.dtos.AttendanceDTO;
 import org.key2gym.business.api.services.AttendancesService;
 import org.key2gym.business.api.services.OrdersService;
 import org.key2gym.business.entities.Attendance;
 import org.key2gym.business.entities.Client;
 import org.key2gym.business.entities.Item;
 import org.key2gym.business.entities.ItemSubscription;
 import org.key2gym.business.entities.Key;
 import org.key2gym.business.entities.OrderEntity;
 import org.key2gym.business.entities.TimeSplit;
 import org.key2gym.persistence.OrderLine;
 import org.key2gym.persistence.Property;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 @Service("org.key2gym.business.api.services.AttendancesService")
 @RolesAllowed({ SecurityRoles.JUNIOR_ADMINISTRATOR,
 	SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER })
 @Transactional
 public class AttendancesServiceBean extends BasicBean implements AttendancesService {

     @Override
     public Integer checkInRegisteredClient(Integer clientId, Integer keyId)
 	throws BusinessException, ValidationException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Operation.Denied"));
         }
 
         Client client;
         Key key;
 
         /*
          * Arguments validation.
          */
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); //NOI18N
         }
 
         client = (Client) getEntityManager().find(Client.class, clientId, LockModeType.OPTIMISTIC);
         if (client == null) {
             throw new ValidationException(getString("Invalid.Client.ID"));
         }
 
         key = getEntityManager().find(Key.class, keyId);
         if (key == null) {
             throw new ValidationException(getString("Invalid.Key.ID"));
         }
 
         if (!getEntityManager().createNamedQuery("Key.findAvailable").getResultList().contains(key)) { //NOI18N
             throw new BusinessException(getString("BusinessRule.Key.NotAvailable"));
         }
 
         /*
          * Client should have attendances.
          */
         if (client.getAttendancesBalance() < 1) {
             throw new BusinessException(getString("BusinessRule.Client.NoAttendancesLeft"));
         }
 
         /*
          * Client's can't be expired.
          */
         if (client.getExpirationDate().compareTo(new Date()) < 0) {
             throw new BusinessException(getString("BusinessRule.Client.SubscriptionExpired"));
         }
 
         client.setAttendancesBalance((Integer) (client.getAttendancesBalance() - 1));
 
         /*
          * Builds an attendance record.
          */
         Attendance attendance = Attendance.apply(key, client);
 
         /*
          * Finds the client's current subscription.
          */
         ItemSubscription itemSubscription;
 
         List<ItemSubscription> itemSubscriptions = (List<ItemSubscription>) 
 	    getEntityManager()
 	    .createNamedQuery("ItemSubscription.findByClientOrderByDateRecordedDesc") //NOI18N
 	    .setParameter("client", client) //NOI18N
 	    .getResultList();
 
         if (!itemSubscriptions.isEmpty()) {
             itemSubscription = itemSubscriptions.get(0);
 
 	    List<TimeSplit> splits = (List<TimeSplit>)getEntityManager()
 		.createNamedQuery("TimeSplit.findAll")
 		.getResultList();
             /*
              * Calculates the quantity of penalties to apply.
              */
             int penalties = itemSubscription.getTimeSplit().calculatePenalties(splits, new LocalTime());
 
             /*
              * If there are penalties to apply, does it.
              */
             if (penalties > 0) {
                 
                 Integer orderId = ordersService.findByClientIdAndDate(clientId, new DateMidnight(), true);
                 Integer itemId = getEntityManager().find(Property.class, "time_range_mismatch_penalty_item_id").getInteger();
 
                 ordersService.addPurchase(orderId, itemId, null, penalties);
             }
         }
 
         getEntityManager().persist(attendance);
         client.getAttendances().add(attendance);
         getEntityManager().flush();
 
         return attendance.getId();
     }
 
     @Override
     public Integer checkInCasualClient(Integer keyId)
             throws BusinessException, ValidationException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Access.Denied"));
         }
 
         Attendance attendance;
         Key key;
         OrderEntity order;
 
         /*
          * Arguments validation.
          */
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); //NOI18N
         }
 
         key = getEntityManager().find(Key.class, keyId);
 
         if (key == null) {
             throw new ValidationException(getString("Invalid.Key.ID"));
         }
 
         if (!getEntityManager().createNamedQuery("Key.findAvailable").getResultList().contains(key)) {
             throw new BusinessException(getString("BusinessRule.Key.NotAvailable"));
         }
 
         attendance = Attendance.apply(key);
         order = OrderEntity.apply(attendance);
 
         ItemSubscription itemSubscription;
 
 	List<TimeSplit> timeSplits = getEntityManager()
 	    .createNamedQuery("TimeSplit.findAll") //NOI18N
 	    .getResultList();
 	
 	TimeSplit currentTimeSplit = TimeSplit.selectTimeSplitForTime(timeSplits, new LocalTime());
 
         if (currentTimeSplit == null) {
             throw new BusinessException(getString("BusinessRule.Attendance.Casual.SubscriptionNotAvailable"));
         }
 
         try {
             itemSubscription = (ItemSubscription) getEntityManager()
 		.createNamedQuery("ItemSubscription.findCasualByTimeSplit") //NOI18N
                     .setParameter("timeSplit", currentTimeSplit) //NOI18N
                     .getSingleResult();
         } catch (NoResultException ex) {
             throw new BusinessException(getString("BusinessRule.Attendance.Casual.SubscriptionNotAvailable"));
         }
 
         OrderLine orderLine = new OrderLine();
         orderLine.setItem(itemSubscription.getItem());
         orderLine.setOrder(order);
         orderLine.setQuantity((Integer) 1);
 
         List<OrderLine> orderLines = new LinkedList<OrderLine>();
         orderLines.add(orderLine);
         order.setOrderLines(orderLines);
 
         attendance.setOrder(order);
 
         getEntityManager().persist(orderLine);
         getEntityManager().persist(order);
         getEntityManager().persist(attendance);
         getEntityManager().flush();
 
         return attendance.getId();
     }
 
     @Override
     public AttendanceDTO getAttendanceById(Integer attendanceId) throws SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Access.Denied"));
         }
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
 
         Attendance attendance = getEntityManager().find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             return null;
         }
 
         AttendanceDTO attendanceDTO = wrapAttendance(attendance);
         return attendanceDTO;
     }
 
     @Override
     public List<AttendanceDTO> findAttendancesByDate(DateMidnight date)
             throws SecurityViolationException {
         
         /*
          * Arguments validation.
          */
         if (date == null) {
             throw new NullPointerException("The date is null."); //NOI18N
         }
 
         if (!date.equals(DateMidnight.now()) && !callerHasRole(SecurityRoles.MANAGER)) {
             throw new SecurityException(getString("Security.Access.Denied"));
         }
 
         List<Attendance> attendances = getEntityManager().createNamedQuery("Attendance.findByDatetimeBeginRangeOrderByDateTimeBeginDesc") //NOI18N
                 .setParameter("low", date.toDate()) //NOI18N
                 .setParameter("high", date.plusDays(1).toDate()) //NOI18N
                 .getResultList();
         List<AttendanceDTO> result = new LinkedList<AttendanceDTO>();
 
         for (Attendance attendance : attendances) {
             result.add(wrapAttendance(attendance));
         }
 
         return result;
     }
 
     @Override
     public List<AttendanceDTO> findAttendancesByClient(Integer id)
             throws ValidationException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Access.Denied"));
         }
 
         if (id == null) {
             throw new NullPointerException("The id is null."); //NOI18N
         }
 
         Client client = getEntityManager().find(Client.class, id);
 
         if (client == null) {
             throw new ValidationException(getString("Invalid.Client.ID"));
         }
 
         List<Attendance> attendances = getEntityManager().createNamedQuery("Attendance.findByClientOrderByDateTimeBeginDesc").setParameter("client", client).getResultList();
 
         List<AttendanceDTO> result = new LinkedList<AttendanceDTO>();
 
         for (Attendance attendance : attendances) {
             result.add(wrapAttendance(attendance));
         }
 
         return result;
     }
 
     @Override
     public Boolean isCasual(Integer attendanceId) throws ValidationException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Access.Denied"));
         }
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
 
         Attendance attendance = getEntityManager().find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             throw new ValidationException(getString("Invalid.Attendance.ID"));
         }
 
         return attendance.getClient() == null;
     }
 
     @Override
     public void checkOut(Integer attendanceId) throws BusinessException, ValidationException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Operation.Denied"));
         }
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
         Attendance attendance = getEntityManager().find(Attendance.class, attendanceId, LockModeType.OPTIMISTIC);
 
         if (attendance == null) {
             throw new ValidationException(getString("Invalid.Attendance.ID"));
         }
 
         if (!attendance.isOpen()) {
             throw new BusinessException(getString("BusinessRule.Attendance.AlreadyClosed"));
         }
 
         OrderEntity order = attendance.getOrder();
 
         /*
          * If there is an order associated with the attendance, the attendance
          * is anonymous, and, therefore, the order should have a full payment.
          */
         if (order != null) {
             if (order.getTotal().compareTo(order.getPayment()) != 0) {
                 throw new BusinessException(getString("BusinessRule.Attendance.Casual.ExactPaymentRequiredToClose"));
             }
         }
         
         /*
          * Registered clients-specific logic
          */
         if(attendance.getClient() != null) {
             
             /*
              * Finds the client's current subscription.
              */
             ItemSubscription itemSubscription;
 
             List<ItemSubscription> itemSubscriptions = (List<ItemSubscription>)
 		getEntityManager()
 		.createNamedQuery("ItemSubscription.findByClientOrderByDateRecordedDesc") //NOI18N
                     .setParameter("client", attendance.getClient()) //NOI18N
                     .getResultList();
 
             if (!itemSubscriptions.isEmpty()) {
                 itemSubscription = itemSubscriptions.get(0);
 
 		List<TimeSplit> timeSplits = getEntityManager().createNamedQuery("TimeSplit.findAll") //NOI18N
 		    .getResultList();
 
                 /*
                  * Calculates the quantity of penalties to apply.
                  */
                 int penalties = itemSubscription
 		    .getTimeSplit()
 		    .calculatePenalties(timeSplits, new LocalTime());
 
                 /*
                  * If there are penalties to apply, does it.
                  */
                 if (penalties > 0) {
 
                     Integer orderId = ordersService.findByClientIdAndDate(attendance.getClient().getId(), new DateMidnight(), true);
                     Integer itemId = getEntityManager().find(Property.class, "time_range_mismatch_penalty_item_id").getInteger();
 
                     order = getEntityManager().find(OrderEntity.class, orderId, LockModeType.OPTIMISTIC);
                     OrderLine targetOrderLine = null;
                     
                     /*
                     * Attempts to find the right order line first.
                      */
                     for(OrderLine orderLine : order.getOrderLines()) {
                         if(orderLine.getItem().getId() == itemId) {
                             targetOrderLine = orderLine;
                         }
                     }
                     
                     /*
                      * Creates an order line, if it does not exists, and applies
                     * the penalties.
                      */
                     if(targetOrderLine == null) {
                         targetOrderLine = new OrderLine();
                         targetOrderLine.setDiscount(null);
                         targetOrderLine.setItem(getEntityManager().find(Item.class, itemId));
                         targetOrderLine.setOrder(order);
                         targetOrderLine.setQuantity(penalties);
                         getEntityManager().persist(targetOrderLine);
                         order.getOrderLines().add(targetOrderLine);
                     } else {
                         targetOrderLine.setQuantity(penalties);
                     }                    
                 }
             }
         }
 
         attendance.close();
     }
 
     @Override
     public Integer findOpenAttendanceByKey(Integer keyId) throws ValidationException, BusinessException, SecurityViolationException {
 
         if (!callerHasAnyRole(SecurityRoles.JUNIOR_ADMINISTRATOR, SecurityRoles.SENIOR_ADMINISTRATOR, SecurityRoles.MANAGER)) {
             throw new SecurityViolationException(getString("Security.Access.Denied"));
         }
 
         /*
          * Arguments validation.
          */
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); // NOI18N
         }
 
         Key key = getEntityManager().find(Key.class, keyId);
 
         if (key == null) {
             throw new ValidationException(getString("Invalid.Key.ID"));
         }
 
         Attendance attendance;
         try {
             attendance = (Attendance) getEntityManager().createNamedQuery("Attendance.findOpenByKey") //NOI18N
                     .setParameter("key", key) //NOI18N
                     .getSingleResult();
         } catch (NoResultException ex) {
             throw new BusinessException(getString("Invalid.Key.NoOpenAttendance"));
         }
 
         return attendance.getId();
     }
 
     /**
      * Builds an attendance DTO from an attendance entity.
      *
      * @param attendance the entity to build DTO from
      * @return the DTO
      */
     public AttendanceDTO wrapAttendance(Attendance attendance) {
         AttendanceDTO attendanceDTO = new AttendanceDTO();
         attendanceDTO.setId(attendance.getId());
         attendanceDTO.setDateTimeBegin(new DateTime(attendance.getDatetimeBegin()));
         attendanceDTO.setClientId(attendance.getClient() == null ? null : attendance.getClient().getId());
         attendanceDTO.setClientFullName(attendance.getClient() == null ? null : attendance.getClient().getFullName());
         attendanceDTO.setKeyId(attendance.getKey().getId());
         attendanceDTO.setKeyTitle(attendance.getKey().getTitle());
         attendanceDTO.setDateTimeEnd(attendance.isOpen()
                 ? null
                 : new DateTime(attendance.getDatetimeEnd()));
 
         return attendanceDTO;
     }
 
     @Autowired
     private OrdersService ordersService;
 }
