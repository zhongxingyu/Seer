 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.supinfo.suprails.service;
 
 import com.supinfo.suprails.entity.Customer;
 import java.util.concurrent.Future;
 import javax.ejb.AsyncResult;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author lukas
  */
 @Stateless
 public class MailService {
     
     public Future<Boolean> sendToCustomer(Customer customer) {
         
         System.out.println("Sending email to " + customer.getEmail() +  "...");
         System.out.println("Done !");
         
         return new AsyncResult<Boolean>(true);
     } 
 }
