 
 package edu.wctc.distjava.ch3.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * This class returns a string of order data based on what the customer selects
  * @author Spark
  * @version 1.0
  */
 public class OrderData {
     //Retrieves the order data
     public String getOrderInfo(String order){
         //List completeOrder = new ArrayList();
         String completeOrder;
        if (order == null){
            completeOrder = "";
        }
        else if(order.equals("sweet1")){
             completeOrder = "Chocolate Chip Cookie";
         }
         else if(order.equals("cream1")) {
             completeOrder = "Monster Cookie Dough Ice Cream";
         }
         else{
            completeOrder = "";
         }
         return completeOrder;
     }
     
     
     
     
 }
