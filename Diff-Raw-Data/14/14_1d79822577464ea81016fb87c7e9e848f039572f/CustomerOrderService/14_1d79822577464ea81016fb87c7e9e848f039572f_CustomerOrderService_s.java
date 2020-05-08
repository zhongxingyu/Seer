 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.supinfo.suprails.service;
 
import com.sun.xml.rpc.processor.generator.CustomClassGenerator;
 import com.supinfo.suprails.dao.CustomerOrderDao;
 import com.supinfo.suprails.entity.CustomerOrder;
 import javax.ejb.EJB;
import javax.ejb.Local;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author lukas
  */
 @Stateless
 public class CustomerOrderService {
     
     @EJB
     private CustomerOrderDao customerOrderDao;
     
     @EJB
     private MailService mailService;
     
     public void processCustomer(CustomerOrder customerOrder) {
         customerOrderDao.create(customerOrder);
        mailService.sendToCustomer(customerOrder.getCustomer());
     }
     
 }
