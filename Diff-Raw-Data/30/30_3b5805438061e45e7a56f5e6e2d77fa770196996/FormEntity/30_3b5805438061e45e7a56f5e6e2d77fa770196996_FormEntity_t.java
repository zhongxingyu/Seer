 package models;
 
 import play.*;
 import play.data.validation.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 import java.util.*;
 
 import models.Woman.*;
 
 
 /**
  * The Class FormEntity.
  */
 @Entity
 public class FormEntity extends Model {
     
     /** The start date. */
     @Required
     public Date    start;
     
     /** The scheduled date. */
     @Required
     public Date    scheduled;
     
     /** The status. */
     public Short   status = 0;
     
     /** The done. */
     public Boolean done   = false;
     
     /** The form. */
     @Required
     @ManyToOne(cascade = CascadeType.ALL)
     public Form    form;
     
     /** The woman. */
     @Required
     @ManyToOne(cascade = CascadeType.ALL)
     public Woman   woman;
     
     
     /**
      * The Constructor.
      * 
      * @param scheduled
      *            the scheduled
      * @param form
      *            the form
      * @param woman
      *            the woman
      */
     public FormEntity(Date scheduled, Form form, Woman woman) {
     
         this.start = this.scheduled = scheduled;
         this.form = form;
         this.woman = woman;
     }
     
     /**
      * The Constructor.
      * 
      * @param start
      *            the start
      * @param scheduled
      *            the scheduled
      * @param form
      *            the form
      * @param woman
      *            the woman
      */
     public FormEntity(Date start, Date scheduled, Form form, Woman woman) {
     
         this(scheduled, form, woman);
         this.start = start;
         
     }
     
     /**
      * Update status and outcome, works as Listener.
      * 
      * @param status
      *            the status
      * @param outcome
      *            the outcome
      */
     public void update(Status status, Outcome outcome) {
     
         List<Logic> logics = Logic.find("SELECT l FROM Logic l WHERE l.form=? AND l.status=? AND l.outcome=?", this.form, status, outcome).fetch();
         
         for (Logic logic : logics) {
             Date baseDate = this.woman.getEventDate(logic.base);
             Calendar validUntil = Calendar.getInstance();
             validUntil.setTime(baseDate);
             validUntil.add(Calendar.DATE, this.form.validity);
             
            if (validUntil.getTime().after(new Date()) && logic.status == Status.INCOMPLETE) {
                 Calendar nextWeek = Calendar.getInstance();
                nextWeek.setTime(this.scheduled);
                 nextWeek.add(Calendar.DATE, 7);
                 new FormEntity(nextWeek.getTime(), this.form, this.woman).save();
             }
             else {
                 Date eventDate = this.woman.getEventDate(logic.event);
                 Calendar nextDate = Calendar.getInstance();
                 nextDate.setTime(eventDate);
                 nextDate.add(Calendar.DATE, logic.duration);
                 
                 new FormEntity(nextDate.getTime(), logic.destination, this.woman).save();
             }
         }
         
         this.done = true;
         this.save();
     }
     
 }
