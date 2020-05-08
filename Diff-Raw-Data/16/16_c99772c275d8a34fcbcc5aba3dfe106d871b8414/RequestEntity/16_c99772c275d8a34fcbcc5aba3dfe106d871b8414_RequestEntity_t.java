 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ua.epam.entity;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  *
  * @author Ivanov Alexander
  */
 public class RequestEntity {
 
     private int idRequest;
     private GregorianCalendar requestDate;
     private WorkType typeWork;
     private String workScope;
     private GregorianCalendar orderFullfillment;
     private User user;
 
     public RequestEntity() {
     }
 
     public RequestEntity(GregorianCalendar requestDate, WorkType typeWork, String workScope, GregorianCalendar orderFullfillment, User user) {
         this.idRequest = 0;
         this.requestDate = requestDate;
         this.typeWork = typeWork;
         this.workScope = workScope;
         this.orderFullfillment = orderFullfillment;
         this.user = user;
     }
 
     /**
      * @return the idRequest
      */
     public int getIdRequest() {
         return idRequest;
     }
 
     /**
      * @param idRequest the idRequest to set
      */
     public void setIdRequest(int idRequest) {
         this.idRequest = idRequest;
     }
 
     /**
      * @return the requestDate
      */
     public GregorianCalendar getRequestDate() {
         return requestDate;
     }
 
     /**
      * @param requestDate the requestDate to set
      */
     public void setRequestDate(GregorianCalendar requestDate) {
         this.requestDate = requestDate;
     }
 
     /**
      * @return the typeWork
      */
     public WorkType getTypeWork() {
         return typeWork;
     }
 
     /**
      * @param typeWork the typeWork to set
      */
     public void setTypeWork(WorkType typeWork) {
         this.typeWork = typeWork;
     }
 
     /**
      * @return the workScope
      */
     public String getWorkScope() {
         return workScope;
     }
 
     /**
      * @param workScope the workScope to set
      */
     public void setWorkScope(String workScope) {
         this.workScope = workScope;
     }
 
     /**
      * @return the orderFullfillment
      */
     public GregorianCalendar getOrderFullfillment() {
         return orderFullfillment;
     }
 
     /**
      * @param orderFullfillment the orderFullfillment to set
      */
     public void setOrderFullfillment(GregorianCalendar orderFullfillment) {
         this.orderFullfillment = orderFullfillment;
     }
 
     /**
      * @return the user
      */
     public User getUser() {
         return user;
     }
 
     /**
      * @param user the user to set
      */
     public void setUser(User user) {
         this.user = user;
     }
 
    public static String showDate(GregorianCalendar gcal) {
         return " [" + gcal.get(Calendar.YEAR) + "/" + (gcal.get(Calendar.MONTH) + 1)
                 + "/" + gcal.get(Calendar.DAY_OF_MONTH) + "] ";
     }
 
     @Override
     public String toString() {
         return "Request id = " + idRequest + " request date:" + showDate(requestDate)
                 + "<br> --> " + user + "<br> --> |" + typeWork + "| -- |"
                 + workScope + "| -- |" + showDate(orderFullfillment) + "|; ";
     }
 }
