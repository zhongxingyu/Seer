 package Beans;
 
 import DB.Database;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import logikk.Dish;
 
 /**
  *
  * @author Michael
  */
 @Named("menuitems")
 @SessionScoped
 public class MenuItems implements Serializable {
 
     Database db = new Database();
     ArrayList<Dish> items = fillTable();
     ArrayList<Dish> orderList = new ArrayList<Dish>();
     int count;
     private Dish selectedDish;
     private double total_price;
 
     public ArrayList<Dish> fillTable() {
         try {
             return db.getDishes();
         } catch (Exception e) {
             System.out.println("Error");
         }
         return new ArrayList<Dish>();
     }
 
     public Dish getSelectedDish() {
         return selectedDish;
     }
 
     public void setSelectedDish(Dish dish) {
         this.selectedDish = dish;
     }
 
     public void addDish() {
         boolean newdish = true;
         for (int i = 0; i < orderList.size(); i++) {
             if (orderList.get(i).getDishName().equals(selectedDish.getDishName())) {
                 orderList.get(i).setCount(orderList.get(i).getCount() + selectedDish.getCount());
                 newdish = false;
             }
         }
         if (newdish) {
             Dish newDish = new Dish(selectedDish.getDishId(), selectedDish.getDishName(), selectedDish.getPrice(),
                     selectedDish.getCount());
             orderList.add(newDish);
         }
     }
 
     public void removeDish(Dish dish) {
         orderList.remove(dish);
     }
 
     public ArrayList<Dish> getItems() {
         return items;
     }
 
     public ArrayList<Dish> getOrderList() {
         return orderList;
     }
 
     public int getCount() {
         return count;
     }
 
     public void setCount(int count) {
         this.count = count;
     }
 
     public double getTotal_price() {
         total_price = 0.0;
         for (int i = 0; i < orderList.size(); i++) {
             total_price += orderList.get(i).getPrice() * orderList.get(i).getCount();
         }
         return total_price;
     }
 
     public void setTotal_price(double total_price) {
         this.total_price = total_price;
     }
 
     public String order() {
         String returnvalue = "";
        if (db.getRole().equals("customer")) {
             returnvalue = "faces/protected/order.xhtml";
         }
         return returnvalue;
     }
 
     public boolean isLoggedIn() {
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         if (externalContext.getRemoteUser() != null) {
             return true;
         }
         return false;
     }
 }
