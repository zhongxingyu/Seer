 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.persistence.FavoriteTimesheet;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import org.hibernate.Session;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FavoriteTimesheetService {
 
    private List<FavoriteTimesheet> favoriteTimesheets = new ArrayList<FavoriteTimesheet>();
 
     public FavoriteTimesheetService() {
     }
 
     public List<FavoriteTimesheet> getFavoriteTimesheets() {
         HibernateConnection connection = HibernateConnection.getInstance();
         Session session = connection.getSession();
 
         favoriteTimesheets =  session.createQuery("from com.thoughtworks.twu.persistence.FavoriteTimesheet").list();
 
         return favoriteTimesheets;
     }
 }
