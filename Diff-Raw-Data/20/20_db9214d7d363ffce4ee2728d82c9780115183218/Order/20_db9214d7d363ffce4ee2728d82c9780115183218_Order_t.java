 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lib;
 
 import java.util.Date;
 
 /**
  *
  * @author Taidg
  */
 public class Order {
     
     private String orderID, userID, shipperCompany, paymentMethod, status,
             billingAddressID, shippingAddressID;
     private int orderNumber, trackingNumber;
     private Date orderDate, paymentDate;
     private boolean paymentConfirmed;
     private double totalCost;
     
    public Order() {}
    
     public String getOrderID(){
         return orderID;
     }
     public void setOrderID(String orderID){
         this.orderID = orderID;
     }
     
     public String getUserID(){
         return userID;
     }
     public void setUserID(String userID){
         this.userID = userID;
     }
     
     public String getShipperCompany(){
         return shipperCompany;
     }
     public void setShipperCompany(String shipperCompany){
         this.shipperCompany = shipperCompany;
     }
     
     public String getPaymentMethod(){
         return paymentMethod;
     }
     public void setPaymentMethod(String paymentMethod){
         this.paymentMethod = paymentMethod;
     }
     
     public String getStatus(){
         return status;
     }
     public void setStatus(String status){
         this.status = status;
     }
     
     public String getBillingAddressID(){
         return billingAddressID;
     }
     public void setBillingAddressID(String billingAddressID){
         this.billingAddressID = billingAddressID;
     }
     
     public String getShippingAddressID(){
         return shippingAddressID;
     }
     public void setShippingAddressID(String shippingAddressID){
         this.shippingAddressID = shippingAddressID;
     }
     
     public int getOrderNumber(){
         return orderNumber;
     }
     public void setOrderNumber(int orderNumber){
         this.orderNumber = orderNumber;
     }
     
     public int getTrackingNumber(){
         return trackingNumber;
     }
     public void setTrackingNumber(int trackingNumber){
         this.trackingNumber = trackingNumber;
     }
     
     public Date getOrderDate(){
         return orderDate;
     }
     public void setOrderDate(Date orderDate){
         this.orderDate = orderDate;
     }
     
     public Date getPaymentDate(){
         return paymentDate;
     }
     public void setPaymentDate(Date paymentDate){
         this.paymentDate = paymentDate;
     }
     
     public boolean getPaymentConfirmation(){
         return paymentConfirmed;
     }
     public void setPaymentConfirmation(boolean trueOrFalse){
         paymentConfirmed = trueOrFalse;
     }
     
     public double getTotalCost(){
         return totalCost;
     }
     public void setTotalCost(double totalCost){
         this.totalCost = totalCost;
     }
     
 }
 
