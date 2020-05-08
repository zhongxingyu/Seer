 package Java;
 /*
  * Class for generating orders from customerinput. 
  */
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Order {
 
    public enum Status {
         PENDING(1, "The order is waiting to be made"),
         UNDER_PREPARATION(2, "The order is currently being made by the chefs"),
         PENDING_DELIVERY(3, "The food is waiting for a driver"),
         ON_THE_ROAD(4, "The food is currently on it's way"),
         FINISHED(5, "The order has been successfully delivered"),
         MISSING(6, "No one knows what happened to the order"),
         NULL(0, "There is no registered order");
         private int code;
         private String description;
 
         private Status(int code, String description) {
             this.code = code;
             this.description = description;
         }
 
         public String getDescription() {
             return description;
         }
 
         public int getCode() {
             return code;
         }
     }
     private int orderId;
     private Date date;
     private int timeOfDelivery;
     private String deliveryAddress;
     private String status;
     private ArrayList<Dish> orderedDish = new ArrayList();
     private double orderPrice = 0.0;
     private static final AtomicInteger sequence = new AtomicInteger(); //making an unique id atomically.
 
     public Order() {
         this.orderId = sequence.getAndIncrement();
     }
 
     public Order(Date date, int timeOfDelivery, String deliveryAddress) {
         this.orderId = sequence.getAndIncrement();
         this.date = date;
         this.timeOfDelivery = timeOfDelivery;
         this.deliveryAddress = deliveryAddress;
         this.status = Status.NULL.toString();
     }
 
     public Order(Date date, int timeOfDelivery, String deliveryAddress, int status) {
         this.orderId = sequence.getAndIncrement();
         this.date = date;
         this.timeOfDelivery = timeOfDelivery;
         this.deliveryAddress = deliveryAddress;
         switch (status) {
             case 1:
                 this.status = Status.PENDING.toString();
                 break;
             case 2:
                 this.status = Status.UNDER_PREPARATION.toString();
                 break;
             case 3:
                 this.status = Status.PENDING_DELIVERY.toString();
                 break;
             case 4:
                 this.status = Status.ON_THE_ROAD.toString();
                 break;
             case 5:
                 this.status = Status.FINISHED.toString();
                 break;
             case 6:
                 this.status = Status.MISSING.toString();
                 break;
         }
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public boolean addDish(Dish dish) {
         if (dish == null) {
             return false;
         }
         for (int i = 0; i < dish.getCount(); i++) {
             orderedDish.add(dish);
             orderPrice += dish.getPrice();
         }
         return true;
     }
 
     public ArrayList<Dish> getOrderedDish() {
         return orderedDish;
     }
 
     public Date getDate() {
         return date;
     }
 
     public double getOrderPrice() {
         return orderPrice;
     }
 
     public String getDeliveryAddress() {
         return deliveryAddress;
     }
 
     public int getOrderId() {
         return orderId;
     }
 
     public int getTimeOfDelivery() {
         return timeOfDelivery;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public void setTimeOfDelivery(int timeOfDelivery) {
         this.timeOfDelivery = timeOfDelivery;
     }
 }
