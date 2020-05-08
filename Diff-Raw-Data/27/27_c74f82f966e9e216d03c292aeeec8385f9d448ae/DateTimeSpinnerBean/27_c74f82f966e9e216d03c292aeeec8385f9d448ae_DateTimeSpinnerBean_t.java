 package org.icemobile.samples.springbasic;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  * This is a sample backing bean for the MVC supported state
  * The properties should be the same
  */
 @SessionAttributes("dateTimeSpinnerBean")
 public class DateTimeSpinnerBean {
 
    private String dateOne;
 
    public String getDateOne() {
        return dateOne;
     }
 
    public void setDateOne(String date) {
        this.dateOne = date;
     }
 }
