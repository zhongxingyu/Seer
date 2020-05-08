 package org.icemobile.samples.springbasic;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  * This is a sample backing bean for the MVC supported state
  * The properties should be the same
  */
 @SessionAttributes("dateTimeSpinnerBean")
 public class DateTimeSpinnerBean {
 
    private String date1;
 
    @ModelAttribute("dateTimeSpinnerBean")
    public DateTimeSpinnerBean createBean() {
        return new DateTimeSpinnerBean();
     }
 
    public String getDate1() {
        return date1;
    }

    public void setDate1(String date) {
        this.date1 = date1;
     }
 }
