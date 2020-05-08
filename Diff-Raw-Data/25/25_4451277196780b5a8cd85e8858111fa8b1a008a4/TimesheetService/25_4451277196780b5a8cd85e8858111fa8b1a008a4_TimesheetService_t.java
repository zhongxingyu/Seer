 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Timesheet;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import org.hibernate.Session;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
 public class TimesheetService {
 
 
     private HibernateConnection connection;
     private Session session;
 
     public TimesheetService() {
         connection = HibernateConnection.getInstance();
         session = connection.getSession();
 
     }
 
 
 
     public List<Timesheet> getAllTimesheets() {
         return session.createQuery("from com.thoughtworks.twu.domain.Timesheet").list();
     }
 
     public void saveTimesheet(Timesheet timesheet) {
         session.getTransaction().begin();
         session.save(timesheet);
         session.getTransaction().commit();
     }
 
 
     public Timesheet createNewTimesheet() {
        connection = HibernateConnection.getInstance();
        session = connection.getSession();
         session.getTransaction().begin();
         Timesheet timesheet = new Timesheet();
         session.save(timesheet);
         session.getTransaction().commit();
 
         return timesheet;
 
     }
 }
