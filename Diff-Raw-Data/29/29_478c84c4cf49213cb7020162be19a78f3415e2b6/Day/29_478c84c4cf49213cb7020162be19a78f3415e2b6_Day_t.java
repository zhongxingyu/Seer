 package com.bulbview.recipeplanner.datamodel;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
import com.google.appengine.repackaged.com.google.common.collect.Sets;
 import com.googlecode.objectify.Key;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class Day {
 
    private Date                        date;

    private final Collection<Key<Item>> items;

    public Day() {
        this.items = Sets.newHashSet();
    }
 
     public void addItem(final Item item) {
         items.add(new Key<Item>(Item.class, item.getName()));
     }
 
    public Date getDate() {
        return date;
    }

    public Collection<Key<Item>> getItems() {
        return items;
    }

     public void setDate(final Date date) {
         this.date = date;
     }
 }
