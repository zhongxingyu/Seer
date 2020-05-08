 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.domain;
 
 import java.util.Date;
 
 /**
  *
  * @author kristoffer
  */
 public class ObligatoryTask extends Task {
 //    Date dueDate;
 //    Date acceptedDate;
     Long dueDate;
     Long acceptedDate;
 
     public ObligatoryTask(String name) {
         super(name);
     }
     
     @Override
     public void acceptTask() {
         super.acceptTask();
         acceptedDate = new Date().getTime();
     }
 
 //    public Date getAcceptedDate() {
 //        return acceptedDate;
 //    }
 //
 //    public void setAcceptedDate(Date acceptedDate) {
 //        this.acceptedDate = acceptedDate;
 //    }
 //
 //    public Date getDueDate() {
 //        return dueDate;
 //    }
 //
 //    public void setDueDate(Date dueDate) {
 //        this.dueDate = dueDate;
 //    }
 
     public Long getAcceptedDate() {
         return acceptedDate;
     }
 
     public void setAcceptedDate(Long acceptedDate) {
         this.acceptedDate = acceptedDate;
     }
 
     public Long getDueDate() {
         return dueDate;
     }
 
     public void setDueDate(Long dueDate) {
         this.dueDate = dueDate;
     }
 }
