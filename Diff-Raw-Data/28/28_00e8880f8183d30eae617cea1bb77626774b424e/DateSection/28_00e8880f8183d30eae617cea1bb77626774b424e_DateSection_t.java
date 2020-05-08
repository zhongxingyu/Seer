 package com.bulbview.recipeplanner.datamodel.schedule;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class DateSection extends Section {
    
     private Date       date;
    
     @Autowired
     private DateFormat dateFormatter;
    
     public Date getDate() {
         return date;
     }
    
     public void setDate(final Date date) {
         this.date = date;
     }
    
    public void setDateFormatter(final DateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }
    
     @Override
     public String toString() {
         return dateFormatter.format(date);
     }
 }
