 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dataejbopi.facade;
 
 import com.dataejbopi.entity.Payment;
 import com.dataejbopi.entity.Pin;
 import com.dataejbsra.vo.ROb;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author Usuario1
  */
 @Stateless
 public class PaymentFacade extends AbstractFacade<Payment> {
     @PersistenceContext(unitName = "DataEjbOpiPU")
     private EntityManager em;
     @EJB
     private PinFacade pinFacade = new PinFacade();
 
     @Override
     protected EntityManager getEntityManager() {
         return em;
     }
 
     public PaymentFacade() {
         super(Payment.class);
     }
     
     public ROb createPaymentForPin(Long idPin, Double salary){
         ROb rob = new ROb();
         try{     
             Pin pin = (Pin) pinFacade.findById(idPin).getData();
             if(pin != null){
                 Payment payment = new Payment();
                 payment.setHealtServiceValue(salary*0.085);
                 payment.setOpiServiceValue(payment.getHealtServiceValue()*0.01);
                 payment.setTotalvalue(payment.getHealtServiceValue()+payment.getOpiServiceValue());
                 payment.setPin(pin);
                 create(payment);
                 List<Payment> listPayment = findAll();
                 payment = listPayment.get(listPayment.size()-1);
                 rob.setData(payment);
                 rob.setSuccess(true);
             }else{
                 rob.setSuccess(false);
                 rob.setErr_message("Cant find that object!");                
             }          
             return rob;
         }catch(Exception e){
             rob.setSuccess(false);
             rob.setErr_message("Failed Transaction!");
             return rob;
         }
     }
     
     public ROb registerPaymentComplete(Long idPayment, Long idTransaction){
         ROb rob = new ROb();
         try{     
             Payment payment =  (Payment) findById(idPayment).getData();
             // Find and validate with a bank transaciont
             if(payment != null){
                 Pin pin = payment.getPin();
                 payment.setPaydate(new Date());
                 pin.setPinstate("Paid");
                 pin.setPayment(payment);
                edit(payment);         
                 rob.setData(payment);
                 rob.setSuccess(true);
             }else{
                 rob.setSuccess(false);
                 rob.setErr_message("Cant find that object!");                
             }          
             return rob;
         }catch(Exception e){
             rob.setSuccess(false);
             rob.setErr_message("Failed Transaction!");
             return rob;
         }
     }
     
     public ROb updatePaymentExtemporaneous(Long idPayment){
         ROb rob = new ROb();
         try{     
             Payment payment =  (Payment) findById(idPayment).getData();
             Date currentDate = new Date();
             Date newLimitDate;
             Double newHealtServiceValue;
             if(payment != null && payment.getPaydate()==null){
                 while(currentDate.after(payment.getPin().getLimitdate())){
                     newLimitDate=payment.getPin().getLimitdate();
                     newLimitDate.setMonth(newLimitDate.getMonth()+1);
                     newHealtServiceValue = payment.getHealtServiceValue()*1.1;
                     
                     payment.getPin().setLimitdate(newLimitDate);
                     payment.setHealtServiceValue(newHealtServiceValue);
                     payment.setOpiServiceValue(newHealtServiceValue*0.01);
                     payment.setTotalvalue(payment.getHealtServiceValue()+payment.getOpiServiceValue());
                     edit(payment);
                     
                     rob.setData(payment);
                     rob.setSuccess(true);
                 }
             }else{
                 rob.setSuccess(false);
                 rob.setErr_message("Cant Find/Update that object!");                
             }          
             return rob;
         }catch(Exception e){
             rob.setSuccess(false);
             rob.setErr_message("Failed Transaction!");
             return rob;
         }
     }
     
     public ROb findById(Long id){
         ROb rob = new ROb();
         try{
             Payment payment = find(id);
             if(payment==null){
                 rob.setErr_message("Cant Find this Object");
                 rob.setSuccess(false);
             } else {
                 rob.setData(payment);
                 rob.setSuccess(true);
             }
             return rob;
         }catch(Exception e){
             rob.setSuccess(false);
             rob.setErr_message("Failed transaction");
             return rob;
         }
     }
     
     public ROb removeById(Long id){
         ROb rob = new ROb();
         try{
             rob = findById(id);
             if(rob.isSuccess()==true){
                 Payment payment = (Payment) rob.getData();
                 remove(payment);
                 rob.setSuccess(true);
                 rob.setData(null);
             }            
             return rob;
         }catch(Exception e){
             rob.setSuccess(false);
             rob.setErr_message("Failed transaction");
             return rob;
         }
     }  
 }
