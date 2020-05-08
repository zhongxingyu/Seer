 package com.bulbview.recipeplanner.datamodel;
 
 import java.util.Collection;
 
 import javax.persistence.Embedded;
 
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.google.common.collect.Sets;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class Schedule extends Entity {
 
     @Embedded
     private final Collection<Day> days;
 
     public Schedule() {
         this.days = Sets.newHashSet();
     }
 
     public void addDay(final Day day) {
         days.add(day);
     }
 
     public void addItem(final Item item) {
         throw new IllegalStateException("Not implemented");
     }

    public Collection<Day> getDays() {
        return days;
    }
 }
