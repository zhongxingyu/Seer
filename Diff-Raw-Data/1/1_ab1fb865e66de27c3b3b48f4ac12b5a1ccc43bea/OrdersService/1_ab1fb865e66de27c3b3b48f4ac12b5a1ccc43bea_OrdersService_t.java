 /*
  * Copyright 2012 Danylo Vashchilenko
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
 package census.business;
 
 import census.business.api.BusinessException;
 import census.business.api.SecurityException;
 import census.business.api.ValidationException;
 import census.business.dto.OrderDTO;
 import census.business.dto.OrderLineDTO;
 import census.persistence.*;
 import java.math.BigDecimal;
 import java.util.*;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import org.joda.time.DateMidnight;
 
 /**
  * This class provides orders-related service.
  * <p>
  * An order is open if either one of the following statements is
  * true:
  * <ul>
  * <li> it's associated with an attendance and the attendance is open </li>
  * <li> it's associated with a client and was issued today </li>
  * <li> it's not associated with anything and was issued today </li>
  * </ul>
  * 
  * @author Danylo Vashchilenko
  */
 public class OrdersService extends BusinessService {
 
     /**
      * Finds an order by the client and the date. If it does not
      * exist, and createIfDoesNotExist is true, an order will be
      * created. Note that an active transaction is required to 
      * create an order.
      *
      * @param clientId the ID of the client
      * @param date the date to look up
      * @param createIfDoesNotExist if true, the order will be
      * created, if none is found
      * @throws NullPointerException if any of the arguments is null
      * @throws IllegalStateException if a new order is requested,
      * but the transaction is not active; if no session is open.
      * @throws ValidationException if the client's ID provided is invalid
      * @return the ID of the order, or null, if none was found and
      * a new one was not requested.
      */
     public Short findByClientIdAndDate(Short clientId, DateMidnight date, Boolean createIfDoesNotExist)
             throws ValidationException {
 
         assertOpenSessionExists();
 
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         if (date == null) {
             throw new NullPointerException("The date is null."); //NOI18N
         }
 
         if (createIfDoesNotExist == null) {
             throw new NullPointerException("The createIfDoesNotExist is null."); //NOI18N
         }
 
         Client client;
         OrderEntity order;
 
         /*
          * Finds the client.
          */
         client = entityManager.find(Client.class, clientId);
 
         if (client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
 
         /*
          * Finds an order associtead with the client and issued
          * today.
          */
         try {
             order = (OrderEntity) entityManager.createNamedQuery("OrderEntity.findByClientAndDateRecorded") //NOI18N
                     .setParameter("client", client) //NOI18N
                     .setParameter("dateRecorded", date.toDate()) //NOI18N
                     .setMaxResults(1).getSingleResult();
             return order.getId();
         } catch (NoResultException ex) {
             /*
              * If none is found but a new one was requested, creates one.
              */
             if (createIfDoesNotExist) {
                 assertTransactionActive();
 
                 order = new OrderEntity();
                 order.setClient(client);
                 order.setDate(date.toDate());
                 order.setPayment(BigDecimal.ZERO);
                 order.setId(getNextId());
 
                 entityManager.persist(order);
                 entityManager.flush();
 
                 return order.getId();
             }
         }
 
         return null;
     }
 
     /**
      * Finds the today's order for the client. If it does not
      * exist, but a valid card was provided, and createIfDoesNotExist is true, a
      * order will be created. Note that an active transaction is required to 
      * create an order.
      *
      * @param cardId the card of the client
      * @throws NullPointerException if the card or createIfDoesNotExist is null
      * @throws IllegalStateException if a new order is required,
      * but the transaction is not active; if the session is not active
      * @throws ValidationException if the card is invalid
      * @return the ID of the order, or null, if none was found and
      * a new one was not requested.
      */
     public Short findCurrentForClientByCard(Integer card, Boolean createIfDoesNotExist)
             throws IllegalArgumentException, IllegalStateException, ValidationException {
         assertOpenSessionExists();
 
         if (card == null) {
             throw new NullPointerException("The card is null."); //NOI18N
         }
 
         if (createIfDoesNotExist == null) {
             throw new NullPointerException("The createIfDoesNotExist is null"); //NOI18N
         }
 
         Client client;
         OrderEntity order;
 
         try {
             client = (Client) entityManager.createNamedQuery("Client.findByCard") //NOI18N
                     .setParameter("card", card) //NOI18N
                     .setMaxResults(1).getSingleResult();
         } catch (NoResultException ex) {
             throw new ValidationException(bundle.getString("ClientCardInvalid"));
         }
 
         try {
             order = (OrderEntity) entityManager.createNamedQuery("OrderEntity.findByClientAndDateRecorded") //NOI18N
                     .setParameter("client", client) //NOI18N
                     .setParameter("dateRecorded", getToday()) //NOI18N
                     .setMaxResults(1).getSingleResult();
             return order.getId();
         } catch (NoResultException ex) {
             /*
              * If none is found but a new one was requested, create one.
              */
             if (createIfDoesNotExist) {
                 assertTransactionActive();
 
                 order = new OrderEntity();
                 order.setClient(client);
                 order.setDate(getToday());
                 order.setPayment(BigDecimal.ZERO);
                 order.setId(getNextId());
                 
                 entityManager.persist(order);
                 entityManager.flush();
 
                 return order.getId();
             }
         }
         return null;
     }
 
     /**
      * Finds the order associated with the attendance.
      * <p>
      * 
      * <ul>
      * <li> The attendance must be anonymous </li>
      * </ul>
      *
      * @param attendanceId the attendance's ID
      * @throws NullPointerException if the attendance's ID is null
      * @throws IllegalStateException if the session is not active
      * @throws ValidationException if the attendance's ID is invalid
      * @throws BusinessException if current business rules resrict this
      * operation
      * @return the ID of the order or null, if none was found and a
      * new one was not requested.
      */
     public Short findForAttendanceById(Short attendanceId)
             throws IllegalArgumentException, IllegalStateException, ValidationException, BusinessException {
 
         assertOpenSessionExists();
 
         /*
          * Arguments validation.
          */
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
 
         Attendance attendance = null;
         OrderEntity order = null;
 
         attendance = (Attendance) entityManager.find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             throw new ValidationException(bundle.getString("AttendanceIDInvalid"));
         }
 
         if (attendance.getClient() != null) {
             throw new BusinessException(bundle.getString("AttendanceMustBeAnonymous"));
         }
 
         try {
             order = (OrderEntity) entityManager.createNamedQuery("OrderEntity.findByAttendance") //NOI18N
                     .setParameter("attendance", attendance) //NOI18N
                     .setMaxResults(1).getSingleResult();
 
             return order.getId();
         } catch (NoResultException ex) {
         }
 
         return null;
     }
 
     /**
      * Finds the today's default order. It's used to record all
      * operations that are not associated with anybody or anything. If the
      * record does not exist, and createIfDoesNotExist is true, it will be
      * created. Note that an active transaction is required to create a new
      * order.
      *
      * @return the ID of the order, or null, if was not found and a
      * new one was not requested.
      * @throws NullPointerException if the createIfDoesNotExist is null
      * @throws IllegalStateException if a new order is required,
      * but the transaction is not active.
      */
     public Short findCurrentDefault(Boolean createIfDoesNotExist)
             throws IllegalArgumentException, IllegalStateException, ValidationException {
 
         /*
          * Arguments validation.
          */
         if (createIfDoesNotExist == null) {
             throw new NullPointerException("The createIfDoesNotExist is null."); //NOI18N
         }
 
         OrderEntity order;
 
         try {
             order = (OrderEntity) entityManager.createNamedQuery("OrderEntity.findDefaultByDateRecorded") //NOI18N
                     .setParameter("dateRecorded", new Date()) //NOI18N
                     .setMaxResults(1).getSingleResult();
             return order.getId();
         } catch (NoResultException ex) {
             /*
              * If none was found but a new one was requested, create one.
              */
             if (createIfDoesNotExist) {
                 assertTransactionActive();
 
                 order = new OrderEntity();
                 order.setId(getNextId());
                 order.setDate(new Date());
                 order.setPayment(BigDecimal.ZERO);
 
                 
                 entityManager.persist(order);
                 entityManager.flush();
 
                 return order.getId();
             }
         }
         return null;
     }
 
     /**
      * Finds orders by the date.
      * <p>
      * 
      * <ul>
      * <li> If the date is not today, the permissions level has to be PL_ALL </li>
      * </ul>
      *
      * @param date the date
      * @throws IllegalStateException if no session is open
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws NullPointerException if any of the arguments is null
      */
     public List<OrderDTO> findAllByDate(DateMidnight date) throws SecurityException {
         assertOpenSessionExists();
 
         if (date == null) {
             throw new NullPointerException("The begin is null."); //NOI18N
         }
 
         if (!DateMidnight.now().equals(date)
                 && !sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("AccessDenied"));
         }
 
         List<OrderEntity> orders = entityManager.createNamedQuery("OrderEntity.findByDateRecordedOrderByIdDesc") //NOI18N
                 .setParameter("dateRecorded", date.toDate()) //NOI18N
                 .getResultList();
 
         List<OrderDTO> result = new LinkedList<>();
 
         for (OrderEntity order : orders) {
             result.add(wrapOrderEntity(order));
         }
 
         return result;
     }
 
     /**
      * Finds orders for client within specified time period.
      *
      * @param id the client's ID
      * @param begin the beginning date
      * @param end the ending date
      * @throws IllegalStateException if the transaction is not active, or no
      * session is open
      * @throws NullPointerException if any of the arguments is null
      * @throws ValidationException if the beginning date is after the ending
      * date, or the client's ID is invalid
      */
     public List<OrderDTO> findForClientWithinPeriod(Short id, DateMidnight begin, DateMidnight end) throws ValidationException {
         assertOpenSessionExists();
 
         if (id == null) {
             throw new NullPointerException("The id is null."); //NOI18N
         }
 
         if (begin == null) {
             throw new NullPointerException("The begin is null."); //NOI18N
         }
 
         if (end == null) {
             throw new NullPointerException("The end is null."); //NOI18N
         }
 
         if (begin.isAfter(end)) {
             throw new ValidationException(bundle.getString("BeginningDateAfterEndingDate"));
         }
 
         Client client = entityManager.find(Client.class, id);
 
         if (client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
 
         List<OrderEntity> financialActivities = entityManager.createNamedQuery("OrderEntity.findByClientAndDateRecordedRangeOrderByDateRecordedDesc") //NOI18N
                 .setParameter("client", client) //NOI18N
                 .setParameter("rangeBegin", begin.toDate()) //NOI18N
                 .setParameter("rangeEnd", end.toDate()) //NOI18N
                 .getResultList();
 
         List<OrderDTO> result = new LinkedList<>();
 
         for (OrderEntity order : financialActivities) {
             result.add(wrapOrderEntity(order));
         }
 
         return result;
     }
 
     /**
      * Gets the sum of all payments received on the date.
      * 
      * @param date the date
      * @return the sum of all payments
      */
     public BigDecimal getTotalForDate(DateMidnight date) {
         assertOpenSessionExists();
 
         if (date == null) {
             throw new NullPointerException("The date is null."); //NOI18N
         }
 
         BigDecimal result = (BigDecimal) entityManager.createNamedQuery("OrderEntity.sumPaymentsForDateRecorded") //NOI18N
                 .setParameter("dateRecorded", date.toDate()) //NOI18N
                 .getSingleResult();
 
         if (result == null) {
             result = BigDecimal.ZERO;
         }
 
         return result.setScale(2);
     }
 
     /**
      * Finds the order by its ID.
      *
      * @param id the order's ID.
      * @return the order's information.
      * @throws NullPointerException if the order's ID is null
      * @throws ValidationException if the order's ID is invalid
      * @throws IllegalStateException if no session is open
      */
     public OrderDTO getById(Short id) throws ValidationException {
         assertOpenSessionExists();
 
         /*
          * Arguments validation.
          */
         if (id == null) {
             throw new NullPointerException("The id is null."); //NOI18N
         }
 
         /*
          * The idea is to get the financial acivity entity and build OrderDTO
          * from it. The DTO contains some extra fields that the presentation
          * might need.
          */
 
         OrderEntity order = entityManager.find(OrderEntity.class, id);
 
         if (order == null) {
             throw new ValidationException(bundle.getString("OrderEntityIDInvalid"));
         }
 
         OrderDTO result = wrapOrderEntity(order);
 
         /*
          * Debt
          */
         if (order.getClient() != null && isToday(order.getDate())) {
             BigDecimal possibleMoneyBalance = order.getClient().getMoneyBalance().add(result.getTotal());
             if (possibleMoneyBalance.compareTo(BigDecimal.ZERO) < 0) {
                 OrderLineDTO orderLine = new OrderLineDTO();
                 orderLine.setItemId(OrderLineDTO.FAKE_ID_DEBT);
                 orderLine.setItemTitle(bundle.getString("Debt"));
                 orderLine.setItemPrice(possibleMoneyBalance.negate().setScale(2));
                 orderLine.setQuantity((short)1);
                orderLine.setTotal(possibleMoneyBalance.negate().setScale(2));
                 
                 result.getOrderLines().add(orderLine);
                 result.setTotal(result.getTotal().add(orderLine.getItemPrice()));
             }
         }
 
         return result;
     }
 
     /**
      * Adds a purchase to the order.
      * <p>
      * 
      * <ul>
      * <li> If the order is not open, the permissions level has to
      * be PL_ALL</li>
      * <li> If an item subscription is being bought, the order has
      * to be associated with a client</li>
      * </ul>
      *
      * @param financialActivitId the order's ID
      * @param itemId the item's ID
      * @throws BusinessException if current security rules restrict this
      * operation
      * @throws NullPointerException if either of the arguments provided is null
      * @throws ValidationException if either of the IDs provided is invalid
      * @throws IllegalStateException if the transaction is not active; if no
      * session is open
      */
     public void addPurchase(Short orderId, Short itemId, Short discountId)
             throws BusinessException, IllegalArgumentException, IllegalStateException, ValidationException, SecurityException {
         assertOpenSessionExists();
         assertTransactionActive();
 
         /*
          * Arguments validation.
          */
         if (orderId == null) {
             throw new NullPointerException("The finacialActivityId is null."); //NOI18N
         }
 
         if (itemId == null) {
             throw new NullPointerException("The itemId is null."); //NOI18N
         }
 
         OrderEntity order;
         Item item;
         Discount discount;
 
         order = entityManager.find(OrderEntity.class,
                 orderId);
 
         if (order == null) {
             throw new ValidationException(bundle.getString("OrderEntityIDInvalid"));
         }
 
         Boolean requiresAllPermissions = (order.getAttendance() != null
                 && !order.getAttendance().getDatetimeEnd().equals(Attendance.DATETIME_END_UNKNOWN))
                 || !isToday(order.getDate());
 
         if (requiresAllPermissions && !sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("OperationDenied"));
         }
 
         item = entityManager.find(Item.class, itemId);
 
         if (item == null) {
             throw new ValidationException(bundle.getString("ItemIDInvalid"));
         }
 
         if (item.getItemSubscription() != null && order.getClient() == null) {
             throw new BusinessException(bundle.getString("OnlyClientsCanPurchaseSubscriptions"));
         }
         
         if(discountId == null) {
             discount = null;
         } else {
             discount = entityManager.find(Discount.class, discountId);
 
             if(discount == null) {
                 throw new ValidationException(bundle.getString("IDInvalid"));
             }
         }
 
         /*
          * Checks the item's quantity.
          */
         Short quantity = item.getQuantity();
         if (quantity != null) {
             /*
              * If the item is not in stock, notifies the presentation.
              * Otherwise, decreases the quantity.
              */
             if (quantity == 0) {
                 throw new BusinessException(bundle.getString("ItemNotInStock"));
             } else {
                 item.setQuantity(new Integer(quantity - 1).shortValue());
             }
         }
 
         /*
          * Business logic specific to orders associated with
          * clients.
          */
         if (order.getClient() != null) {
             Client client = order.getClient();
 
             /*
              * Charge the Client's account. Checks whether the new value will
              * overreach the precision limit.
              */
             BigDecimal amount = item.getPrice();
             if(discount != null) {
                 amount = amount.divide(new BigDecimal(100));
                 amount = amount.multiply(new BigDecimal(100-discount.getPercent()));
             }
             BigDecimal newMoneyBalance = client.getMoneyBalance().subtract(amount);
             if (newMoneyBalance.precision() > 5) {
                 throw new ValidationException(bundle.getString("LimitReached"));
             }
             client.setMoneyBalance(newMoneyBalance);
 
             if (item.getItemSubscription() != null) {
                 /*
                  * After the client has expired, it's attendances balance is
                  * kept until the client buys another subscription. The
                  * attendance's balance is not zeroed, if he buys another
                  * subscription before the expiration date.
                  */
                 short attendancesBalance = client.getAttendancesBalance();
                 /*
                  * Expiration base is the date from which we count the
                  * expiration date by adding the Item Subscription's term. It's
                  * either today or the Client's current expiration date,
                  * whatever is later.
                  */
                 Date expirationBase = client.getExpirationDate();
 
                 if (hasExpired(client.getExpirationDate())) {
                     attendancesBalance = 0;
                     expirationBase = new Date();
                 }
 
                 attendancesBalance += item.getItemSubscription().getUnits();
                 client.setAttendancesBalance(attendancesBalance);
 
                 client.setExpirationDate(rollExpirationDate(item.getItemSubscription(),
                         expirationBase, true));
 
             }
         }
 
         /*
          * Attemps to find an appropriate order line.
          * Due to JPQL limitations we need a separate query,
          * when the order line's discount is null. Criteria API, JDO?
          */
         OrderLine orderLine;
         
         Query query;
         if(discount == null) {
             query = entityManager.createNamedQuery("OrderLine.findByOrderAndItemAndNoDiscount");
         } else {
             query = entityManager.createNamedQuery("OrderLine.findByOrderAndItemAndDiscount")
                 .setParameter("discount", discount);
         }
         
         query.setParameter("order", order)
              .setParameter("item", item);
         
         try {
             orderLine = (OrderLine) query
                 .setMaxResults(1)
                 .getSingleResult();
         } catch(NoResultException ex) {
             orderLine = null;
         }
 
         /*
          * Creates a new order line, if none was found.
          */
         if (orderLine == null) {
             orderLine = new OrderLine();
             orderLine.setItem(item);
             orderLine.setOrder(order);
             orderLine.setQuantity((short) 1);
             orderLine.setDiscount(discount);
             entityManager.persist(orderLine);
 
             List<OrderLine> orderLines = order.getOrderLines();
             if (orderLines == null) {
                 orderLines = new LinkedList<>();
             }
             orderLines.add(orderLine);
         } else {
             orderLine.setQuantity((short) (orderLine.getQuantity() + 1));
         }
        
         entityManager.flush();
     }
 
     /**
      * Removes one purchase of the item from the order.
      * <p>
      * 
      * <ul>
      * <li> If the order is not open, the permissions level has to
      * be PL_ALL</li>
      * <li> The order has to contain at least one purchase of the
      * item</li>
      * <li> If the item's ID is negative, it's forced and can not be removed </li>
      * <li> The subscriptions can not be removed from the orders
      * associated with attendances </li>
      * </ul>
      *
      * @param orderId the order's ID
      * @param itemId the item's ID
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws NullPointerException if either of the arguments provided is null
      * @throws ValidationException if either of the IDs provided is invalid
      * @throws IllegalStateException if the transaction is not active, or if no
      * session is open
      */
     public void removePurchase(Short orderLineId)
             throws BusinessException, IllegalArgumentException, ValidationException, SecurityException {
         assertOpenSessionExists();
         assertTransactionActive();
 
         /*
          * Arguments validation.
          */
         if (orderLineId == null) {
             throw new NullPointerException("The orderId is null."); //NOI18N
         }
         
         if(orderLineId < 0) {
             throw new BusinessException(bundle.getString("ItemEnforcedCanNotBeRemoved"));
         }
 
         OrderEntity order;
         OrderLine orderLine;
         Item item;
 
         orderLine = entityManager.find(OrderLine.class, orderLineId);
         
         if (orderLine == null) {
             throw new ValidationException(bundle.getString("OrderLineIDInvalid"));
         }
         
         order = orderLine.getOrder();
         item = orderLine.getItem();
 
         Boolean requiresAllPermissions = (order.getAttendance() != null
                 && !order.getAttendance().getDatetimeEnd().equals(Attendance.DATETIME_END_UNKNOWN))
                 || !isToday(order.getDate());
 
         if (requiresAllPermissions && !sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("OperationDenied"));
         }
 
         if (order.getAttendance() != null && item.getItemSubscription() != null) {
             throw new BusinessException(bundle.getString("SubscriptionCanNotBeRemovedFromOrderEntityWithAttendance"));
         }
 
         Property timeRangeMismatch = (Property) entityManager.createNamedQuery("Property.findByName").setParameter("name", "time_range_mismatch_penalty_item_id").getSingleResult();
         if (orderLine.getItem().getId().toString().equals(timeRangeMismatch.getString())) {
             throw new BusinessException(bundle.getString("ItemEnforcedCanNotBeRemoved"));
         }
 
         /*
          * Business logic specific to orders associated with clients.
          */
         if (order.getClient() != null) {
             
             Client client = order.getClient();
 
             /*
              * Give money back to the client.
              */
             client.setMoneyBalance(client.getMoneyBalance().add(item.getPrice()));
 
             if (item.getItemSubscription() != null) {
                 /*
                  * After the client has expired, it's attendances balance is
                  * kept until the client buys another subscription. The
                  * attendance's balance is not zeroed, if he buys another
                  * subscription before the expiration date.
                  */
                 Integer attendancesBalance = client.getAttendancesBalance() - item.getItemSubscription().getUnits();
                 client.setAttendancesBalance(attendancesBalance.shortValue());
 
                 /*
                  * We count the expiration date by substracting the item
                  * subscription's term.
                  */
                 client.setExpirationDate(rollExpirationDate(item.getItemSubscription(),
                         client.getExpirationDate(), false));
 
             }
         }
 
         /*
          * Restores the item's quantity, if it's finite. It's impossible to get
          * overflow here, for the item's quantity counter is being restored to
          * the state it already had before the item was purchased.
          */
         if (item.getQuantity() != null) {
             item.setQuantity((short) (item.getQuantity() + 1));
         }
 
         /*
          * Decreases the quantity on the order line, and removes it, if the
          * quantity is now zero.
          */
         orderLine.setQuantity((short) (orderLine.getQuantity() - 1));
         if (orderLine.getQuantity() == 0) {
             // EntityManager won't remove this relationship upon EntityManager.remove call
             order.getOrderLines().remove(orderLine);
             entityManager.remove(orderLine);
         }
 
         entityManager.flush();
     }
 
     /**
      * Records a payment.
      * <p>
      * 
      * <ul>
      * <li>If the order is closed, the permissions level has to be
      * PL_ALL</li>
      * <li>If the order is associated with a client, a withdrawal
      * is allowed, if it won't make the client's money balance negative</li>
      * </ul>
      *
      * @param orderId the order's ID
      * @param amount the amount paid
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws NullPointerException if either one of the arguments is null
      * @throws ValidationException if order's ID is invalid
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws IllegalStateException if the transaction or the session is not
      * active
      */
     public void recordPayment(Short orderId, BigDecimal amount)
             throws BusinessException, IllegalArgumentException, ValidationException, SecurityException {
         assertOpenSessionExists();
         assertTransactionActive();
 
         /*
          * Checks the arguments.
          */
         if (orderId == null) {
             throw new NullPointerException("The orderId is null."); //NOI18N
         }
 
         if (amount == null) {
             throw new NullPointerException("The amount is null."); //NOI18N
         }
 
         /*
          * Finds the target order.
          */
         OrderEntity order = entityManager.find(OrderEntity.class, orderId);
 
         if (order == null) {
             throw new ValidationException(bundle.getString("OrderEntityIDInvalid"));
         }
 
         Boolean requiresAllPermissions = (order.getAttendance() != null
                 && !order.getAttendance().getDatetimeEnd().equals(Attendance.DATETIME_END_UNKNOWN))
                 || !isToday(order.getDate());
 
         if (requiresAllPermissions && !sessionService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("OperationDenied"));
         }
 
         /*
          * Normalizes the scale, and throws an exception, if the scale is to
          * big.
          */
         if (amount.scale() > 2) {
             throw new ValidationException(bundle.getString("TwoDigitsAfterDecimalPointMax"));
         }
         amount = amount.setScale(2);
 
 
         BigDecimal newTotalPaymentMaid = order.getPayment().add(amount);
 
         if (newTotalPaymentMaid.precision() > 5) {
             throw new ValidationException(bundle.getString("LimitReached"));
         }
 
         /*
          * If the order is associted with a Client, does some
          * checks and alters the Client's money balance.
          */
         if (order.getClient() != null) {
             Client client = order.getClient();
             BigDecimal newMoneyBalance = client.getMoneyBalance().add(amount);
 
             if (newMoneyBalance.precision() > 5) {
                 throw new ValidationException(bundle.getString("LimitReached"));
             }
 
             if (newTotalPaymentMaid.compareTo(BigDecimal.ZERO) < 0) {
                 if (newMoneyBalance.add(amount).compareTo(BigDecimal.ZERO) < 0) {
                     throw new BusinessException(bundle.getString("ClientNotEnoughMoneyToWithdraw"));
                 }
             }
 
             /*
              * Changes the client's money balance. TODO: note change
              */
             client.setMoneyBalance(newMoneyBalance);
         }
 
         
         order.setPayment(newTotalPaymentMaid);
 
     }
 
     /**
      * Returns the amount of payment associated with the order.
      *
      * @param orderId the order's ID
      * @return the amount of payment
      * @throws NullPointerException if the order's ID is null
      * @throws ValidationException if the order's ID is invalid
      * @throws IllegalStateException if the session is not active
      */
     public BigDecimal getPayment(Integer orderId)
             throws ValidationException {
         assertOpenSessionExists();
 
         /*
          * Arguments validation.
          */
         if (orderId == null) {
             throw new NullPointerException("The orderId is null."); //NOI18N
         }
 
         OrderEntity order = entityManager.find(OrderEntity.class,
                 orderId);
 
         if (order == null) {
             throw new ValidationException(bundle.getString("OrderEntityIDInvalid"));
         }
 
         return order.getPayment();
     }
 
     /**
      * Gets the next free ID that can be assigned to an order.
      *
      * @return the ID
      */
     public Short getNextId() {
         try {
             return new Integer((Short) entityManager.createNamedQuery("OrderEntity.findAllIdsOrderByIdDesc") //NOI18N
                     .setMaxResults(1).getSingleResult() + 1).shortValue();
         } catch (NoResultException ex) {
             return 1;
         }
     }
 
     private OrderDTO wrapOrderEntity(OrderEntity order) {
 
         OrderDTO orderDTO = new OrderDTO();
         orderDTO.setId(order.getId());
         orderDTO.setDate(new DateMidnight(order.getDate()));
         orderDTO.setPayment(order.getPayment().setScale(2));
 
         /*
          * Order lines
          */
         List<OrderLineDTO> orderLineDTOs = new LinkedList<>();
 
         /*
          * We count the order's total to calculate the client's debt later.
          */
         BigDecimal total = BigDecimal.ZERO.setScale(2);
 
         if (order.getOrderLines() != null) {
             for (OrderLine orderLine : order.getOrderLines()) {
                 Item item = orderLine.getItem();
                 Discount discount = orderLine.getDiscount();
                 
                 OrderLineDTO orderLineDTO = new OrderLineDTO();
                 orderLineDTO.setId(orderLine.getId());
                 orderLineDTO.setItemId(item.getId());
                 orderLineDTO.setItemTitle(item.getTitle());
                 orderLineDTO.setItemPrice(item.getPrice());
                 if(discount != null) {
                     orderLineDTO.setDiscountPercent(discount.getPercent());
                     orderLineDTO.setDiscountTitle(discount.getTitle());
                 }
                 orderLineDTO.setQuantity(orderLine.getQuantity());
                 orderLineDTO.setTotal(orderLine.getTotal());
                 
                 total = total.add(orderLine.getTotal());
                 
                 orderLineDTOs.add(orderLineDTO);
             }
         }
         orderDTO.setOrderLines(orderLineDTOs);
 
         /*
          * Client
          */
         if (order.getClient() != null) {
             orderDTO.setClientId(order.getClient().getId());
             orderDTO.setClientFullName(order.getClient().getFullName());
         }
 
         /*
          * Attendance
          */
         if (order.getAttendance() != null) {
             orderDTO.setAttendanceId(order.getAttendance().getId());
             orderDTO.setKeyTitle(order.getAttendance().getKey().getTitle());
         }
         
         /*
          * Total
          */
         orderDTO.setTotal(total);
 
         /*
          * Due
          */
         orderDTO.setDue(total.subtract(order.getPayment()));
 
         return orderDTO;
     }
 
     /**
      * Checks whether the provided date is today.
      *
      * @param date the date to check
      * @return true, if the date's time is past the today's midnight.
      */
     private boolean isToday(Date date) {
         DateMidnight today = new DateMidnight();
         DateMidnight tomorrow = today.plusDays(1);
         return today.getMillis() <= date.getTime() && tomorrow.getMillis() > date.getTime();
     }
 
     /**
      * Returns a Date instance that represents the today's midnight.
      *
      * @return a Date instance
      */
     private Date getToday() {
         return new DateMidnight().toDate();
     }
 
     /**
      * Gets whether the expiration date has passed.
      * 
      * @param expirationDate the date to check
      * @return true, if the expiration date has passed
      */
     private boolean hasExpired(Date expirationDate) {
         return !new Date().before(expirationDate);
     }
 
     /**
      * Shifts the date according to the subscription's term.
      * 
      * @param itemSubscription the subscription to use
      * @param date the date to start with
      * @param forward if true, the date will be shifted into the future
      * @return the shifted date
      */
     private Date rollExpirationDate(ItemSubscription itemSubscription, Date date, Boolean forward) {
         Calendar expirationDate = new GregorianCalendar();
         expirationDate.setTime(date);
 
         expirationDate.roll(Calendar.YEAR, forward ? itemSubscription.getTermYears() : -itemSubscription.getTermYears());
         expirationDate.roll(Calendar.MONTH, forward ? itemSubscription.getTermMonths() : -itemSubscription.getTermMonths());
         expirationDate.roll(Calendar.DATE, forward ? itemSubscription.getTermDays() : -itemSubscription.getTermDays());
 
         return expirationDate.getTime();
     }
     
     /**
      * Singleton instance.
      */
     private static OrdersService instance;
 
     /**
      * Returns the instance of this class.
      *
      * @return the instance
      */
     public static OrdersService getInstance() {
         if (instance == null) {
             instance = new OrdersService();
         }
         return instance;
     }
 }
