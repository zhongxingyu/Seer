 package com.exadel.borsch.managers.impl.jdbc;
 
 import com.exadel.borsch.dao.*;
 import com.exadel.borsch.entity.Dish;
 import com.exadel.borsch.entity.MenuItem;
 import com.exadel.borsch.entity.Order;
 import com.exadel.borsch.entity.User;
 import com.exadel.borsch.managers.OrderManager;
 import com.exadel.borsch.util.DateTimeUtils;
 import com.exadel.borsch.util.Entry;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Vlad
  */
 @Service("jdbcOrderManager")
 @Scope("singleton")
 public class JdbcOrderManager implements OrderManager {
 
     @Autowired
     private MenuItemDao menuItemDao;
 
     @Autowired
     private OrderDao orderDao;
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     private DishDao dishDao;
 
     @Autowired
     private ChoicesDao choicesDao;
 
     @Autowired
     private OrderChangeDao changeDao;
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void updateOrder(Order toUpdate, MenuItem menuItem) {
         menuItemDao.update(menuItem);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void deleteOrderById(Long id) {
         for (MenuItem item : menuItemDao.getAllByOrderId(id)) {
             choicesDao.deleteAllByMenuItemId(item.getId());
             changeDao.deleteAllByMenuItemId(item.getId());
         }
         menuItemDao.deleteAllByOrderId(id);
 
         orderDao.delete(id);
     }
 
     @Override
     @Transactional(readOnly = true)
     public Order getOrderById(Long id) {
         Order order = orderDao.getById(id);
 
         order.addMenuItems(getMenuItemsForOrder(id));
 
         return order;
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void addOrder(Order order) {
         orderDao.save(order);
     }
 
     @Override
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     public List<Order> getAllOrders() {
         List<Order> orders = orderDao.getAll();
         Map<Long, Long> ids = getUsersIdForOrdersId();
 
         for (Order curOrder : orders) {
             curOrder.addMenuItems(
                     getMenuItemsForOrder(curOrder.getId())
             );
             curOrder.setOwner(
                     userDao.getUserById(
                             ids.get(curOrder.getId()
                             )
                     )
             );
         }
 
         return Collections.unmodifiableList(orders);
     }
 
     @Override
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     public List<Order> getAllOrders(DateTime startDate) {
         List<Order> orders = orderDao.getAll();
         List<Order> resultList = new ArrayList<>();
         Map<Long, Long> ids = getUsersIdForOrdersId();
 
         for (Order curOrder : orders) {
             DateTime curStartDate = curOrder.getStartDate();
             if (DateTimeUtils.sameDates(startDate, curStartDate)) {
                 curOrder.addMenuItems(
                        getMenuItemsForOrder(curOrder.getId())
                 );
                 curOrder.setOwner(
                         userDao.getUserById(
                                 ids.get(curOrder.getId()
                                 )
                         )
                 );
                 resultList.add(curOrder);
             }
         }
 
         return Collections.unmodifiableList(resultList);
     }
 
     @Override
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     public List<Order> getOrdersForUser(User user) {
         List<Order> orders = orderDao.getByOwnerId(user.getId());
 
         for (Order order : orders) {
             order.addMenuItems(
                     getMenuItemsForOrder(order.getId())
             );
             order.setOwner(user);
         }
 
         return Collections.unmodifiableList(orders);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public Order getCurrentOrderForUser(User user) {
         List<Order> userOrders = orderDao.getByOwnerId(user.getId());
         if (userOrders.isEmpty()
                || Weeks.weeksBetween(userOrders.get(0).getStartDate(),
                                         DateTimeUtils.getStartOfNextWeek()).getWeeks() >= 1) {
             Order order = new Order();
             order.setOwner(user);
             order.setStartDate(DateTimeUtils.getStartOfNextWeek());
             order.setEndDate(order.getStartDate().plusDays(DateTimeUtils.WORKING_DAYS_IN_WEEK));
 
             orderDao.save(order);
 
             fillOrderWithItems(order);
             return order;
         }
         Order curOrder = userOrders.get(0);
 
         curOrder.addMenuItems(
                     getMenuItemsForOrder(curOrder.getId())
         );
 
         curOrder.setOwner(user);
 
         return curOrder;
     }
 
     public void fillOrderWithItems(Order order) {
         for (int i = 0; i < DateTimeUtils.WORKING_DAYS_IN_WEEK; i++) {
             MenuItem item = new MenuItem();
             item.setDate(order.getStartDate().plusDays(i));
             menuItemDao.save(item);
             menuItemDao.setOrderId(item.getId(), order.getId());
             order.addMenuItem(item);
         }
     }
 
     @Override
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     public Order findOrderAtDateForUser(User user, DateTime date) {
         List<Order> userOrders = getOrdersForUser(user);
         date = DateTimeUtils.getStartOfWeek(date);
         for (Order order : userOrders) {
             if (DateTimeUtils.sameDates(order.getStartDate(), date)) {
                 order.addMenuItems(
                         menuItemDao.getAllByOrderId(
                                 order.getId()
                         )
                 );
                 order.setOwner(user);
                 return order;
             }
         }
         return null;
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void removeDishFormMenuItem(MenuItem menuItem, Dish dish) {
         choicesDao.delete(dish.getId());
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void addDishFormMenuItem(MenuItem menuItem, Dish dish) {
         choicesDao.save(menuItem.getId(), dish.getId());
     }
 
     /**
      * @return Map<Long, Long> - key -> orderId, value -> userId
      * */
     private Map<Long, Long> getUsersIdForOrdersId() {
         return Entry.parseToMap(
                 orderDao.getUsersIdForOrdersId()
         );
     }
 
     @Transactional(readOnly = true, propagation = Propagation.NEVER)
     private List<MenuItem> getMenuItemsForOrder(Long orderId) {
         List<MenuItem> menuItems = menuItemDao.getAllByOrderId(orderId);
 
         for (MenuItem menuItem : menuItems) {
             menuItem.addChoices(
                     dishDao.getAllByMenuItemId(menuItem.getId())
             );
         }
 
         return menuItems;
     }
 }
