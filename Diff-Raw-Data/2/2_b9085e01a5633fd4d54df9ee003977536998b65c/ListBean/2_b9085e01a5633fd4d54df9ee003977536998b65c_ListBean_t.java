 package org.icemobile.samples.springbasic;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * This is a sample backing bean for the MVC supported state
  * The properties should be the same
  */
 @SessionAttributes("ListBean")
 public class ListBean {
 
     @ModelAttribute("listBean")
     public ListBean createBean() {
         return new ListBean();
    }
 
     public Collection getCarCollection() {
         ArrayList cars = new ArrayList();
         cars.add(new Car("Porsche 924", 45000));
         cars.add(new Car("Audi A8", 90000));
         cars.add(new Car("BMW M3", 500000));
         cars.add(new Car("Bugatti Veyron", 2000000));
 
 
         return cars;
     }
 
     public class Car {
         private String title;
         private int cost;
 
         Car(String title, int cost) {
             this.title = title;
             this.cost = cost;
         }
 
         public String getTitle() {
             return title;
         }
 
         public int getCost() {
             return cost;
         }
     }
 
 }
