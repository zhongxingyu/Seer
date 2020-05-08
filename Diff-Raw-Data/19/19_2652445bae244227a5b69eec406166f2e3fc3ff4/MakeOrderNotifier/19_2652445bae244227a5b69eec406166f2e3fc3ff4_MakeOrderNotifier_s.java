 package com.exadel.borsch.notifier;
 
 import com.exadel.borsch.dao.MenuItem;
 import com.exadel.borsch.dao.Order;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.MenuManager;
 import com.exadel.borsch.managers.UserManager;
 import com.exadel.borsch.notification.Notification;
 import java.util.ArrayList;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * @author zubr
  */
 @Service
 public class MakeOrderNotifier extends NotifierTask {
     private static final int DAYS_IN_WEEK = 7;
     //private EmailNotification notification;
     @Autowired
     private UserManager userManager;
     @Autowired
     private MenuManager menuManager;
 
     public MakeOrderNotifier(Notification notification) {
         super(notification);
     }
 
     private boolean isWeekOrderComplete(Order order, DateTime startOfWeek, DateTime endOfWeek) {
         boolean[] days = new boolean[DAYS_IN_WEEK];
         for (MenuItem item: order.getOrder()) {
             if (item.getDate().isAfter(startOfWeek) && item.getDate().isBefore(endOfWeek)) {
                 days[item.getDate().getDayOfWeek()] = true;
             }
         }
         for (int i = 0; i < days.length; i++) {
             if (!days[i]) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public void run() {
         DateTime startOfWeek = DateTime.now();
         startOfWeek = startOfWeek.plusDays(DAYS_IN_WEEK - (startOfWeek.getDayOfWeek() - 1));
         DateTime endOfWeek = startOfWeek.plusDays(DAYS_IN_WEEK);
         List<User> checkedUsers = new ArrayList<>();
 
         for (Order order: menuManager.getAllOrders()) {
             checkedUsers.add(order.getOwner());
            if (!isWeekOrderComplete(order, startOfWeek, endOfWeek))
                 getNotification().submit(order.getOwner());
         }
 
         List<User> remeainingUsers = userManager.getAllUsers();
         remeainingUsers.removeAll(checkedUsers);
         getNotification().submit(remeainingUsers);
     }
 
 }
