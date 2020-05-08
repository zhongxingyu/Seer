 package com.bulbview.recipeplanner.datamodel;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.googlecode.objectify.Key;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class Day {
 
    private Date                  date;
    private Collection<Key<Item>> items;
 
     public void addItem(final Item item) {
         items.add(new Key<Item>(Item.class, item.getName()));
     }
 
     public void setDate(final Date date) {
         this.date = date;
     }
 }
