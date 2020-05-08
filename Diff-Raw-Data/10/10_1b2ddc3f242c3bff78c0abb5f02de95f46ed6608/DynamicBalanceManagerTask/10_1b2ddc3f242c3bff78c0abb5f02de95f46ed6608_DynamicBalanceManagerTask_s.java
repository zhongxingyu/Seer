 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.user.balance;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.server.order.event.NewOrderEvent;
 import com.sapienter.jbilling.server.order.event.OrderDeletedEvent;
 import com.sapienter.jbilling.server.order.event.OrderToInvoiceEvent;
 import com.sapienter.jbilling.server.payment.event.PaymentDeletedEvent;
 import com.sapienter.jbilling.server.payment.event.PaymentSuccessfulEvent;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.system.event.Event;
 import com.sapienter.jbilling.server.system.event.task.IInternalEventsTask;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import java.math.BigDecimal;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author emilc
  */
 public class DynamicBalanceManagerTask extends PluggableTask implements IInternalEventsTask {
 
     private static final Class<Event> events[] = new Class[] { 
         PaymentSuccessfulEvent.class,
         OrderDeletedEvent.class,
         NewOrderEvent.class,
         PaymentDeletedEvent.class,
         OrderToInvoiceEvent.class
     };
 
     private static final Logger LOG = Logger.getLogger(DynamicBalanceManagerTask.class);
 
     public Class<Event>[] getSubscribedEvents() {
         return events;
     }
 
     public void process(Event event) throws PluggableTaskException {
         updateDynamicBalance(event.getEntityId(), determineUserId(event), determineAmount(event));
     }
 
     private BigDecimal determineAmount(Event event) {
         if (event instanceof PaymentSuccessfulEvent) {
             PaymentSuccessfulEvent payment = (PaymentSuccessfulEvent) event;
             return new BigDecimal(payment.getPayment().getAmount());
         } else if (event instanceof OrderDeletedEvent) {
             OrderDeletedEvent order = (OrderDeletedEvent) event;
             if (order.getOrder().getOrderPeriod().getId() ==  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                 return order.getOrder().getTotal();
             } else {
                 return BigDecimal.ZERO;
             }
         } else if (event instanceof NewOrderEvent) {
             NewOrderEvent order = (NewOrderEvent) event;
             if (order.getOrder().getOrderPeriod().getId() ==  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                 return order.getOrder().getTotal().multiply(new BigDecimal(-1));
             } else {
                 return BigDecimal.ZERO;
             }
         } else if (event instanceof PaymentDeletedEvent) {
             PaymentDeletedEvent payment = (PaymentDeletedEvent) event;
             return new BigDecimal(payment.getPayment().getAmount()).multiply(new BigDecimal(-1));
         } else if (event instanceof OrderToInvoiceEvent) {
             OrderToInvoiceEvent order = (OrderToInvoiceEvent) event;
             if (order.getOrder().getOrderPeriod().getId() !=  com.sapienter.jbilling.server.util.Constants.ORDER_PERIOD_ONCE) {
                 return order.getOrder().getTotal().multiply(new BigDecimal(-1));
             } else {
                 return BigDecimal.ZERO;
             }
         } else {
             LOG.error("Can not determine amount for event " + event);
             return null;
         }
     }
 
     private int determineUserId(Event event) {
         if (event instanceof PaymentSuccessfulEvent) {
             PaymentSuccessfulEvent payment = (PaymentSuccessfulEvent) event;
             return payment.getPayment().getUserId();
         } else if (event instanceof OrderDeletedEvent) {
             OrderDeletedEvent order = (OrderDeletedEvent) event;
             return order.getOrder().getBaseUserByUserId().getId();
         } else if (event instanceof NewOrderEvent) {
             NewOrderEvent order = (NewOrderEvent) event;
             return order.getOrder().getBaseUserByUserId().getId();
         } else if (event instanceof PaymentDeletedEvent) {
             PaymentDeletedEvent payment = (PaymentDeletedEvent) event;
             return payment.getPayment().getBaseUser().getId();
         } else if (event instanceof OrderToInvoiceEvent) {
             OrderToInvoiceEvent order = (OrderToInvoiceEvent) event;
             return order.getOrder().getBaseUserByUserId().getId();
         } else {
             LOG.error("Can not determine user for event " + event);
             return 0;
         }
     }
 
     private void updateDynamicBalance(Integer entityId, Integer userId, BigDecimal amount) {
         UserDTO user = new UserDAS().find(userId);
 
        if (user.getCustomer().getBalanceType() == Constants.BALANCE_NO_DYNAMIC ||
                 amount.equals(BigDecimal.ZERO)) {
             LOG.debug("Nothing to update");
             return;
         }
 
         LOG.debug("Updating dynamic balance for " + amount);
         if (user.getCustomer().getDynamicBalance() == null) {
             // initialize
             user.getCustomer().setDynamicBalance(BigDecimal.ZERO);
         }
         
         new EventLogger().auditBySystem(entityId, com.sapienter.jbilling.server.util.Constants.TABLE_CUSTOMER,
                 user.getCustomer().getId(), EventLogger.MODULE_USER_MAINTENANCE,
                 EventLogger.DYNAMIC_BALANCE_CHANGE, null,
                 user.getCustomer().getDynamicBalance().toString(), null);
         
         if (user.getCustomer().getBalanceType() == Constants.BALANCE_CREDIT_LIMIT) {
             user.getCustomer().setDynamicBalance(
                     user.getCustomer().getDynamicBalance().subtract(amount));
         } else if (user.getCustomer().getBalanceType() == Constants.BALANCE_PRE_PAID) {
             user.getCustomer().setDynamicBalance(
                     user.getCustomer().getDynamicBalance().add(amount));
         }
     }
 }
