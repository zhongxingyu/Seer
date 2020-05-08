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
 
 package com.sapienter.jbilling.server.order;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
 import com.sapienter.jbilling.server.order.db.UsageDAS;
 import com.sapienter.jbilling.server.pluggableTask.OrderPeriodTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.process.PeriodOfTime;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.Context;
 import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
 import org.apache.log4j.Logger;
 import org.joda.time.DateMidnight;
 import org.springmodules.cache.CachingModel;
 import org.springmodules.cache.FlushingModel;
 import org.springmodules.cache.provider.CacheProviderFacade;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  * Provides easy access to usage information over the customers natural billing period.
  *
  * @author Brian Cowdery
  * @since 16-08-2010
  */
 public class UsageBL {
     private static final Logger LOG = Logger.getLogger(UsageBL.class);
 
     private static final Integer CURRENT_PERIOD = 1;
 
     private UsageDAS usageDas;
     private Integer userId;
     private Integer periods;
     private UsagePeriod usagePeriod = null;
 
     // working order, order in-memory that contains lines applicable to the usage count
     private OrderDTO workingOrder;
 
     // cache of calculated usage periods
     private CacheProviderFacade cache;
     private CachingModel cacheModel;
     private FlushingModel flushModel;
 
     /**
      * Construct a UsageBL object to calculate usage for the customers most recent/current
      * period.
      *
      * @param userId user id
      */
     public UsageBL(Integer userId) {
         _init();
         set(userId, CURRENT_PERIOD);
     }
 
     /**
      * Constructs a UusageBL object to calculate usage for the customers most recent/current
      * period, with a working order that is to be included in the usage counts.
      *
      * @param userId user id
      * @param order working order (order being edited/created).
      */
     public UsageBL(Integer userId, OrderDTO order) {
         _init();
         setWorkingOrder(order);
         set(userId, CURRENT_PERIOD);
     }
 
     /**
      * Construct a UsageBL object to calculate usage over the given number of
      * periods in the past, where 1 period back is the current period.
      *
      * @param userId user id
      * @param periods number of periods in the past to calculate usage for
      */
     public UsageBL(Integer userId, Integer periods) {
         _init();
         set(userId, periods);
     }
 
     private void _init() {
         usageDas = new UsageDAS();
 
         cache = (CacheProviderFacade) Context.getBean(Context.Name.CACHE);
         cacheModel = (CachingModel) Context.getBean(Context.Name.CACHE_MODEL_RW);
         flushModel = (FlushingModel) Context.getBean(Context.Name.CACHE_FLUSH_MODEL_RW);
     }
 
     public void set(Integer userId, Integer periods) {
         this.userId = userId;
         this.periods = periods;
 
         usagePeriod = (UsagePeriod) cache.getFromCache(getCacheKey(), cacheModel);
 
         // could not load period from cache
         if (usagePeriod == null) {
             OrderDTO mainSubscriptionOrder = null;
             Integer orderId = new OrderBL().getMainOrderId(userId);
            if (orderId != null)
                mainSubscriptionOrder = new OrderBL(orderId).getEntity();
 
             if (mainSubscriptionOrder == null) {
                 usagePeriod = new UsagePeriod();
                 LOG.warn("User " + userId + " does not have main subscription order - all usage will be 0!");
                 return;
             }
 

             // calculate usage cycle start & end dates from the working order
             if (workingOrder != null) {
                 if (workingOrder.getOrderPeriod().getId() == Constants.ORDER_PERIOD_ONCE) {
                     calculateOneTimeUsagePeriod(workingOrder, mainSubscriptionOrder);
                 } else {
                     calculateMonthlyUsagePeriod(workingOrder);
                 }
             }
 
             // calculate usage cycle start & end dates from the main subscription order
             if (workingOrder == null) {
                 calculateMonthlyUsagePeriod(mainSubscriptionOrder);
             }
 
         } else {
             LOG.debug("Cache hit for '" + getCacheKey() + "', usage period: " + usagePeriod);
         }
     }
 
     public void calculateMonthlyUsagePeriod(OrderDTO periodOrder) {
         usagePeriod = new UsagePeriod();
 
         try {
             Integer entityId = periodOrder.getBaseUserByUserId().getCompany().getId();
             PluggableTaskManager manager = new PluggableTaskManager(entityId, Constants.PLUGGABLE_TASK_ORDER_PERIODS);
             OrderPeriodTask periodTask = (OrderPeriodTask) manager.getNextClass();
 
             if (periodTask == null)
                 throw new SessionInternalError("OrderPeriodTask not configured!");
 
             Date cycleStartDate = periodTask.calculateStart(periodOrder);
            Date cycleEndDate = periodTask.calculateEnd(periodOrder,
                    new Date(),
                    periods,
                    cycleStartDate);
 
             List<PeriodOfTime> billingPeriods = periodTask.getPeriods();
 
             if (billingPeriods.isEmpty())
                 throw new SessionInternalError("Could not determine user's billing period!");
 
             // populate usage period object for cache
             usagePeriod.setPeriodOrder(periodOrder);
             usagePeriod.setCycleStartDate(cycleStartDate);
             usagePeriod.setCycleEndDate(cycleEndDate);
             usagePeriod.setBillingPeriods(billingPeriods);
 
            LOG.debug("Caching with key '" + getCacheKey() + "', usage period: " + usagePeriod);
            cache.putInCache(getCacheKey(), cacheModel, usagePeriod);

         } catch (PluggableTaskException e) {
             throw new SessionInternalError("Exception occurred retrieving the configured OrderPeriodTask.", e);
         } catch (TaskException e) {
             throw new SessionInternalError("Exception occurred calculating the customers billing periods.", e);
         }
     }
 
     public void calculateOneTimeUsagePeriod(OrderDTO periodOrder, OrderDTO mainSubscriptionOrder) {
         usagePeriod = new UsagePeriod();
 
         Date cycleStartDate = periodOrder.getActiveSince() != null
                                     ? periodOrder.getActiveSince()
                                     : periodOrder.getCreateDate();
 
         Date cycleEndDate = periodOrder.getActiveUntil();
         if (cycleEndDate == null) {
             OrderPeriodDTO period = mainSubscriptionOrder.getOrderPeriod();
 
             Calendar calendar = GregorianCalendar.getInstance();
             calendar.setTime(cycleStartDate);
             calendar.add(MapPeriodToCalendar.map(period.getUnitId()), period.getValue());
 
             cycleEndDate = calendar.getTime();
         }
 
         usagePeriod.setPeriodOrder(periodOrder);
         usagePeriod.setCycleStartDate(cycleStartDate);
         usagePeriod.setCycleEndDate(cycleEndDate);
 
         periods = 1;
     }
 
     private String getCacheKey() {
         if (workingOrder != null) {
             return "user " + userId + " order " + workingOrder.getId() + " periods " + periods;
         } else {
             return "user " + userId + " main order periods " + periods;
         }
     }
 
     public void invalidateCache() {
         cache.flushCache(flushModel);
     }
 
     public Integer getUserId() {
         return userId;
     }
 
     /**
      * Returns the number of periods spanned by this usage calculation inclusive, where 1
      * denotes the current period, 2 is the current period + 1 etc.
      *
      * Example:
      * <literal>
      *      1 period:
      *      July 1st -> July 30th
      *
      *      2 periods:
      *      June 1st -> July 30th
      *
      *  where July is the current month
      * </literal>
      *
      * @return number of periods spanned by this usage calculation 
      */
     public Integer getPeriods() {
         return periods;
     }
 
     /**
      * Returns the main subscription order for this customer. The users main subscription
      * order defines the billing cycle dates.
      * 
      * @return customers main subscription order.
      */
     public OrderDTO getPeriodOrder() {
         return usagePeriod.getPeriodOrder();
     }
 
     /**
      * Returns the billing cycle start date for this customer. This is the start date of the very
      * first billing period for this customer, effectively the date the main subscription order
      * became active.
      *
      * @return cycle start date
      */
     public Date getCycleStartDate() {
         return usagePeriod.getCycleStartDate();
     }
 
     /**
      * Returns the billing cycle end date for this customer. This is the end of of the customers
      * current billing period, effectively the date the main subscription order will become in-active.
      *
      * @return cycle end date
      */
     public Date getCycleEndDate() {
         return usagePeriod.getCycleEndDate();
     }
 
     /**
      * Returns a list of billing periods of the main subscription order, spanning
      * back N number of periods ({@link #getPeriods()}.
      *
      * @return billing periods
      */
     public List<PeriodOfTime> getBillingPeriods() {
         return usagePeriod.getBillingPeriods();
     }
 
     /**
      * Returns the start date for the defined period of usage (period of time spanning
      * back N number of periods; {@link #getPeriods()}).
      *
      * @return usage period start date.
      */
     public Date getPeriodStart() {
         // one usage periods for one-time orders have no distinct billing periods
         if (usagePeriod.getBillingPeriods() == null) {
             return usagePeriod.getCycleStartDate();
         }
 
         // get the first period entry in the list - will be N number of periods in the past
         PeriodOfTime start = usagePeriod.getBillingPeriods().get(0);
         return start.getStart();
     }
 
     /**
      * Returns the current period end date for this customer. This customer will return
      * end of day today as the end date if the period end date is in the past (can occur
      * if we're processing before the customer's first billing run).
      *
      * @return current period end date
      */
     public Date getPeriodEnd() {
         // one usage periods for one-time orders have no distinct billing periods
         if (usagePeriod.getBillingPeriods() == null) {
             return usagePeriod.getCycleEndDate();
         }
 
         // get the last period entry in the list - will be the most recent period
         PeriodOfTime end = usagePeriod.getBillingPeriods().get(usagePeriod.getBillingPeriods().size() - 1);
 
         // end of day today
         DateMidnight today = new DateMidnight().plusDays(1);
 
         if (new DateMidnight(end.getEnd().getTime()).isBefore(today)) {
             return today.toDate();
         } else {
             return end.getEnd();
         }
     }
 
     public OrderDTO getWorkingOrder() {
         return workingOrder;
     }
 
     /**
      * Sets an OrderDTO as the current working order for this usage calculation. The working order's
      * lines will be rolled into the usage calculation.
      *
      * If persisted (has an ID) this order will be excluded from the usage SQL query to prevent
      * the order from being counted twice.
      *
      * @param workingOrder working order to include in usage calculations
      */
     public void setWorkingOrder(OrderDTO workingOrder) {
         this.workingOrder = workingOrder;
     }
 
     /**
      * Returns the total usage over the set number of periods.
      *
      * @param itemId item id
      * @return usage
      */
     public Usage getItemUsage(Integer itemId) {
         Usage usage;
         if (getPeriodOrder() != null) {
             Integer workingOrderId = getWorkingOrder() != null ? getWorkingOrder().getId() : null;
             Date startDate = getPeriodStart();
             Date endDate = getPeriodEnd();
 
             LOG.debug("Fetching usage of item " + itemId
                       + " for " + periods + " period(s), start: " + startDate + ", end: " + endDate);
             usage = usageDas.findUsageByItem(workingOrderId, itemId, userId, startDate, endDate);
         } else {
             LOG.warn("User has no main subscription order billing period, item " + itemId + " usage set to 0");
             usage = new Usage(userId, itemId, null, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
         }
 
         addWorkingOrder(usage);
         return usage;
     }
 
     /**
      * Returns the total usage over the set number of periods for this customer and
      * all direct sub-accounts.
      *
      * @param itemId item id
      * @return usage
      */
     public Usage getSubAccountItemUsage(Integer itemId) {
         Usage usage;
         if (getPeriodOrder() != null) {
             Integer workingOrderId = getWorkingOrder() != null ? getWorkingOrder().getId() : null;
             Date startDate = getPeriodStart();
             Date endDate = getPeriodEnd();
 
             LOG.debug("Fetching usage (including sub-accounts) of item " + itemId
                       + " for " + periods + " period(s), start: " + startDate + ", end: " + endDate);
             usage = usageDas.findSubAccountUsageByItem(workingOrderId, itemId, userId, startDate, endDate);
         } else {
             LOG.warn("User has no main subscription order billing period, item " + itemId + " usage set to 0");
             usage = new Usage(userId, itemId, null, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
         }
 
         addWorkingOrder(usage);
         return usage;
     }
 
     /**
      * Returns the total usage over the set number of periods.
      * 
      * @param itemTypeId item type id
      * @return usage
      */
     public Usage getItemTypeUsage(Integer itemTypeId) {
         Usage usage;
         if (getPeriodOrder() != null) {
             Integer workingOrderId = getWorkingOrder() != null ? getWorkingOrder().getId() : null;
             Date startDate = getPeriodStart();
             Date endDate = getPeriodEnd();
 
             LOG.debug("Fetching usage of item type " + itemTypeId
                       + " for " + periods + " period(s), start: " + startDate + ", end: " + endDate);
             usage = usageDas.findUsageByItemType(workingOrderId, itemTypeId, userId, startDate, endDate);
         } else {
             LOG.warn("User has no main subscription order billing period, item type " + itemTypeId + " usage set to 0");
             usage = new Usage(userId, null, itemTypeId, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
         }
 
         addWorkingOrder(usage);
         return usage;        
     }
 
     /**
      * Returns the total usage over the set number of periods for this customer and
      * all direct sub-accounts.
      *
      * @param itemTypeId item type id
      * @return usage
      */
     public Usage getSubAccountItemTypeUsage(Integer itemTypeId) {
         Usage usage;
         if (getPeriodOrder() != null) {
             Integer workingOrderId = getWorkingOrder() != null ? getWorkingOrder().getId() : null;
             Date startDate = getPeriodStart();
             Date endDate = getPeriodEnd();
 
             LOG.debug("Fetching usage (including sub-accounts) of item type " + itemTypeId
                       + " for " + periods + " period(s), start: " + startDate + ", end: " + endDate);
             usage = usageDas.findSubAccountUsageByItemType(workingOrderId, itemTypeId, userId, startDate, endDate);
         } else {
             LOG.warn("User has no main subscription order billing period, item type " + itemTypeId + " usage set to 0");
             usage = new Usage(userId, null, itemTypeId, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
         }
 
         addWorkingOrder(usage);
         return usage; 
     }
 
     private void addWorkingOrder(Usage usage) {
         if (getWorkingOrder() != null) {
             for (OrderLineDTO line : getWorkingOrder().getLines()) {
 
                 // add matching line items
                 if (usage.getItemId() != null && usage.getItemId().equals(line.getItemId()))
                     usage.addLine(line);
 
                 // add matching line items of type
                 if (usage.getItemTypeId() != null) {
                     ItemDTO item = new ItemBL(line.getItemId()).getEntity();
                     if (item.hasType(usage.getItemTypeId()))
                         usage.addLine(line);
                 }
             }
         }
     }
 }
