 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Named;
 import logikk.Order;
 import logikk.OrderStatus;
 import logikk.PendingOrders;
 
 /**
  *
  * @author Rino
  */
 @SessionScoped
 @Named("Sales")
 public class SalesmanBean implements Serializable {
 
     private PendingOrders overView = new PendingOrders();
     private List<OrderStatus> tabledata = Collections.synchronizedList(new ArrayList<OrderStatus>());
 
     public synchronized List<OrderStatus> getTabledata() {
         return tabledata;
     }
 
     public synchronized boolean isEmpty() {
         return !(tabledata.size() > 0);
     }
 
     public synchronized void update() {
         ArrayList<Order> temp = overView.getFirstOrdersSalesmen();
         if (tabledata.size() < temp.size()) {
            tabledata.clear();
            for (int i = 0; i < temp.size(); i++) {
                tabledata.add(new OrderStatus(temp.get(i)));
            }
            quickSortDate(0,tabledata.size()-1);
         }
         for (int i = 0; i < tabledata.size(); i++) {
             if (tabledata.get(i).getToBeChanged()) {
                 tabledata.get(i).getOrder().setStatus("PENDING");
                 overView.updateDb(tabledata.get(i).getOrder());
             }
         }
     }
 
     private void quickSortDate(int low, int high) {
         int i = low;
         int j = high;
         Date pivot = tabledata.get(low + (high - low) / 2).getOrder().getFullDate();
         while (i <= j) {
             while (tabledata.get(i).getOrder().getFullDate().before(pivot)) {
                 i++;
             }
             while (tabledata.get(j).getOrder().getFullDate().after(pivot)) {
                 j--;
             }
             if (i <= j) {
                exchange(i, j);i++;j--;
             }
         }
         if (low < j) {
             quickSortDate(low, j);
         }
         if (i < high) {
             quickSortDate(i, high);
         }
     }
     private void exchange(int i, int j) {
         OrderStatus temp = tabledata.get(i);
         tabledata.set(i, tabledata.get(j));
        tabledata.set(j,temp);
     }
 }
