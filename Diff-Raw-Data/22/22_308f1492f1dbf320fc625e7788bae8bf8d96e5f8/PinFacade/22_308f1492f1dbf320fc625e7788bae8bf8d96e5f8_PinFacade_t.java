 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dataejbopi.facade;
 
 import com.dataejbopi.entity.Payment;
 import com.dataejbopi.entity.Person;
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
 public class PinFacade extends AbstractFacade<Pin> {
     @PersistenceContext(unitName = "DataEjbOpiPU")
     private EntityManager em;
     @EJB
     private PersonFacade personFacade;    
     @EJB
     private PaymentFacade paymentFacade;
 
     @Override
     protected EntityManager getEntityManager() {
         return em;
     }
 
     public PinFacade() {
         super(Pin.class);
     }
     
     public ROb registerPin(Long personCedule, int month){
         ROb rob = new ROb();
         try{
             Person person = (Person) personFacade.findByCedule(personCedule).getData();  
             Double salary = 1000000.0; // Find Saraly from EPS service          
             Date currentDate = new Date();
             Date limitDate = new Date();         
             currentDate.setMonth(month-1);
             limitDate.setMonth(month);
             System.out.println("fecha actual: "+ currentDate.getMonth());
             System.out.println("fecha limite: "+limitDate.getMonth());
             if(person != null){
                 List<Pin> listPin = findAll();
                 for(Pin p:listPin){
                     if(p.getPerson().getCedule()==personCedule && p.getCreationdate().getMonth()==month-1){
                         rob.setSuccess(false);
                         rob.setErr_message("The pin for this month was already registered!");
                         return rob;
                     }
                 }
                 Pin pin = new Pin();
                 pin.setPinstate("UnPaid"); 
                 pin.setCreationdate(currentDate);   
                 pin.setLimitdate(limitDate);       
                 create(pin);
                 
                 listPin = findAll();
                 pin = listPin.get(listPin.size()-1);    
                 Payment payment = (Payment) paymentFacade.createPaymentForPin(pin.getId(), salary).getData();
                 
                 pin.setPerson(person);   
                 pin.setPayment(payment);
                Pin pinVo = new Pin();
                Payment paidVo = new Payment();
                pinVo=pin;
                paidVo=payment;
                paidVo.setPin(null);
                pinVo.setPayment(paidVo);
                 edit(pin);
                 rob.setData(find(pin.getId()));    
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
     
     public ROb findById(Long id){
         ROb rob = new ROb();
         try{
             Pin pin = find(id);
             if(pin==null){
                 rob.setErr_message("Cant Find this Object");
                 rob.setSuccess(false);
             } else {
                //pin.getPayment().setPin(null);
                Pin vo = new Pin();
                vo=pin;
                vo.setPayment(null);
                rob.setData(vo);
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
                 Pin pin = (Pin) rob.getData();
                 remove(pin);
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
