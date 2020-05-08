 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Beans;
 
 import DB.Database;
 import java.io.Serializable;
 import java.sql.Date;
 import java.sql.Time;
 import java.util.ArrayList;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 import logikk.Dish;
 import logikk.MenuItems;
 import logikk.Order;
 import logikk.User;
 
 /**
  *
  * @author Michael
  */
 @Named(value = "orderBean")
 @SessionScoped
 public class OrderBean implements Serializable{
     private Database db = new Database();
     private MenuItems mi = new MenuItems();
     private ArrayList<Dish> dishes = mi.getOrderList();
     private User user = db.getUser();
     private Date deliverydate;
     private Time time;
     private String description;
 
    public boolean confirmOrder(){
         if(deliverydate != null && user.getAddress() != null && user.getFirstName() != null && user.getSurname() != null && dishes.size() > 0){
        Order order = new Order(deliverydate, time, user.getAddress(), 7, dishes, description, user.getPostnumber());
         if(db.order(order)){
             return true;
             }
         }
         return false;
    }
     public Time getTime() {
         return time;
     }
 
     public Date getDeliverydate() {
         return deliverydate;
     }
 
     public void setDeliverydate(Date deliverydate) {
         this.deliverydate = deliverydate;
     }
 
     public void setTime(Time time) {
         this.time = time;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
     
 }
