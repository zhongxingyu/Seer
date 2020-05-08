 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mwr.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import com.mwr.database.*;
 import com.mwr.businesslogic.TokenGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 /**
  *
  * @author Heindrich
  */
 @ManagedBean(name="bean")
 @SessionScoped
 public class DatabaseJSFManagedBean {
 
     private Session session;
     private HibernateUtil helper;
     private Employee employee;
     private Devicenotregistered device_not;
     private Device device;
     private List<Devicenotregistered> waitingList;
    private String mac;
    private String serial;
    private String android;
     
 
     /**
      * Creates a new instance of DatabaseJSFManagedBean
      */
     public DatabaseJSFManagedBean() {
 
         session = HibernateUtil.getSessionFactory().openSession();
         
     }
     
     public void addToWaitingList(String mac, String android, String serial, String make, String model, String username, String password, String idnumber, String name, String surname) throws NoSuchAlgorithmException
     {   
         TokenGenerator gen = new TokenGenerator();
         String token = gen.generateToken(mac, android, serial);    
         DevicenotregisteredId devicePK = new DevicenotregisteredId(mac,android,serial); 
         device_not = new Devicenotregistered(devicePK, make,model, username,  password, idnumber, name, surname,token);
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(device);
         session.getTransaction().commit();
         session.close();
     }
     
   
     public Employee addEmployee(String username, String password, String name, String surname, String idnumber)
     {
         employee = new Employee(username,password,new Date(),name,surname,idnumber);
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(employee);
         session.getTransaction().commit();
         session.close();
         return employee;
     }
     
    
     
     public void addDevice(Devicenotregistered d)
     {
         
         Employee employee = addEmployee(d.getUsername(),d.getPassword(),d.getName(),d.getSurname(),d.getIdnumber());
         DeviceId id = new DeviceId(d.getId());
         Device device = new Device(id, employee,d.getMake(),d.getModel(), new Date());
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         session.save(device);
         session.getTransaction().commit();
         session.close();
     }
     
     
     public List getWaitingList()
     {
         session = helper.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("from Devicenotregistered");
         waitingList = query.list();
         return waitingList;
     }
     
 //    public String getDevices()
 //    {
 //        session = helper.getSessionFactory().openSession();
 //        session.beginTransaction();
 //
 //    }
 }
