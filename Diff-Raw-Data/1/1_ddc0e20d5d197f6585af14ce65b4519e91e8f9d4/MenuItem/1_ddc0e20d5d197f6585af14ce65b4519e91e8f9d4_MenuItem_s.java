 package com.exadel.borsch.dao;
 
import com.exadel.borsch.util.Encoder;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Andrey Zhilka
  */
 public class MenuItem extends Identifiable {
     private DateTime date;
     private List<Dish> choices = new ArrayList<>();
     private boolean isPaid = false;
 
     public MenuItem() {
         super();
     }
 
     public Integer getTotalPrice() {
         Integer total = 0;
         for (Dish dish : choices) {
             total += dish.getPrice();
         }
 
         return total;
     }
 
     public boolean getIsPaid() {
         return isPaid;
     }
 
     public void setIsPaid(boolean status) {
         this.isPaid = status;
     }
 
     public DateTime getDate() {
         return date;
     }
 
     public void setDate(DateTime date) {
         this.date = date;
     }
 
     public List<Dish> getChoices() {
         return Collections.unmodifiableList(choices);
     }
 
     public boolean cancelChoices(List<Dish> forCancel) {
         return choices.removeAll(forCancel);
     }
 
     public boolean addChoices(List<Dish> toAdd) {
         return choices.addAll(toAdd);
     }
 
     public boolean addDish(Dish dish) {
         return choices.add(dish);
     }
 
     public boolean removeDish(Dish dish) {
         return choices.remove(dish);
     }
 
     public void removeDishByIndex(int idx) {
         choices.remove(idx);
     }
 
     @Override
     public boolean equals(Object menuItem) {
         if (menuItem instanceof MenuItem) {
             return new EqualsBuilder()
                     .append(this.getId(), ((MenuItem) menuItem).getId())
                     .append(date, ((MenuItem) menuItem).getDate())
                     .append(this.choices, ((MenuItem) menuItem).getChoices())
                     .isEquals();
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder(42, 6)
                 .append(this.getId())
                 .append(date)
                 .append(this.choices)
                 .toHashCode();
     }
 }
