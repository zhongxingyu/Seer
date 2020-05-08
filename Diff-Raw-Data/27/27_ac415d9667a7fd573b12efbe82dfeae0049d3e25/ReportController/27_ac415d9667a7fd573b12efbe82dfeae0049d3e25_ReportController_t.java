 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.Dish;
 import com.exadel.borsch.dao.MenuItem;
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.OrderManager;
 import com.exadel.borsch.util.DateTimeUtils;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ListMultimap;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.*;
 
 /**
  * @author Andrey Zhilka
  */
 @Controller
 public class ReportController {
     @Autowired
     private ManagerFactory managerFactory;
 
     @Secured("ROLE_PRINT_ORDER")
     @ResponseBody
     @RequestMapping(value = "/report/setPaid/{orderId}/{menuId}", method = RequestMethod.POST)
     public void processAjaxRequest(@PathVariable String orderId, @PathVariable String menuId) {
         OrderManager orderManager = managerFactory.getOrderManager();
         Order order = orderManager.getOrderById(UUID.fromString(orderId));
         MenuItem menuItem = order.getMenuById(UUID.fromString(menuId));
         menuItem.setIsPaid(true);
         orderManager.updateOrder(order);
     }
 
     @Secured("ROLE_PRINT_ORDER")
     @RequestMapping("/report/{week}")
     public String processPageRequest(ModelMap model, @PathVariable Integer week) {
         OrderManager orderManager = managerFactory.getOrderManager();
         ListMultimap<Integer, DailyOrder> report = ArrayListMultimap.create();
         List<Order> allOrders;
 
         DateTime startOfWeek;
         switch (week) {
             case 0 : //previous week
                startOfWeek = DateTimeUtils.getStartOfWeek(DateTime.now().minusDays(DateTimeUtils.DAYS_IN_WEEK));
                 break;
             case 1 : //current week
                 startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
                 break;
             case 2 : //next week
                 startOfWeek = DateTimeUtils.getStartOfNextWeek();
                 break;
             default:
                 startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
                 break;
         }
         allOrders = orderManager.getAllOrders(startOfWeek);
 
         for (Order order : allOrders) {
             for (MenuItem item : order.getOrder()) {
                 if (item.getChoices().isEmpty()) {
                     continue;
                 }
                 DailyOrder daySummary = DailyOrder.mapOrderAndItemToDailyOrder(item, order);
                 report.put(item.getDate().getDayOfWeek() - 1, daySummary);
             }
         }
 
         model.addAttribute("week", week);
         model.addAttribute("report", report.asMap());
 
         return ViewURLs.WEEK_ORDER_REPORT;
     }
 
     @Secured("ROLE_PRINT_ORDER")
     @RequestMapping("/report/summary")
     public String processSummaryRequest(ModelMap model) {
         OrderManager orderManager = managerFactory.getOrderManager();
         int today = DateTime.now().getDayOfWeek();
         Map<Integer, Map<String, Integer>> summary = new TreeMap<>();
         for (int i = today; i <= DateTimeUtils.WORKING_DAYS_IN_WEEK; i++) {
             summary.put(i, new HashMap<String, Integer>());
         }
         DateTime startOfWeek = DateTimeUtils.getStartOfCurrentWeek();
         for (Order order : orderManager.getAllOrders(startOfWeek)) {
             for (MenuItem item : order.getOrder()) {
                 if (item.getDate().isBeforeNow()) {
                     continue; // do not show past orders
                 }
                 Integer dayOfWeek = item.getDate().getDayOfWeek();
                 Map<String, Integer> dayOrders = summary.get(dayOfWeek);
                 for (Dish dish : item.getChoices()) {
                     String dishName = dish.getName();
                     if (!dayOrders.containsKey(dishName)) {
                         dayOrders.put(dishName, 1);
                     } else {
                         dayOrders.put(dishName, dayOrders.get(dishName) + 1);
                     }
                 }
             }
         }
 
         model.addAttribute("summary", summary);
 
         return ViewURLs.ORDERS_SUMMARY_PAGE;
     }
 
     public static class DailyOrder {
         private Integer weekDay;
         private User user;
         private MenuItem menuItem;
         private Integer total;
         private UUID weekOrderId;
 
 
         public Integer getWeekDay() {
             return weekDay;
         }
 
         public void setWeekDay(Integer weekDay) {
             this.weekDay = weekDay;
         }
 
         public UUID getWeekOrderId() {
             return weekOrderId;
         }
 
         public void setWeekOrderId(UUID weekOrderId) {
             this.weekOrderId = weekOrderId;
         }
 
         public Integer getTotal() {
             return total;
         }
 
         public void setTotal(Integer total) {
             this.total = total;
         }
 
         public User getUser() {
             return user;
         }
 
         public void setUser(User user) {
             this.user = user;
         }
 
         public MenuItem getMenuItem() {
             return menuItem;
         }
 
         public void setMenuItem(MenuItem menuItem) {
             this.menuItem = menuItem;
         }
 
         public static DailyOrder mapOrderAndItemToDailyOrder(MenuItem item, Order order) {
             DailyOrder daySummary = new DailyOrder();
             daySummary.setMenuItem(item);
             daySummary.setUser(order.getOwner());
             daySummary.setWeekDay(item.getDate().getDayOfWeek());
             daySummary.setTotal(item.getTotalPrice());
             daySummary.setWeekOrderId(order.getId());
             return daySummary;
         }
     }
 }
