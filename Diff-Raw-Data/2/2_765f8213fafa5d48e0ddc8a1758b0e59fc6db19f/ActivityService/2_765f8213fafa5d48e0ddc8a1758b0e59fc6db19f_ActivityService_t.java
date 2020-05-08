 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Activity;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import org.hibernate.Session;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.List;
 
 public class ActivityService {
 
     private HibernateConnection connection;
     private Session session;
 
     public ActivityService() {
         connection = HibernateConnection.getInstance();
         session = connection.getSession();
 
     }
 
     public List<Activity> getAllActivities() {
         return session.createQuery("from com.thoughtworks.twu.domain.Activity").list();
     }
 
     public JSONArray getActivities(String searchCriteria) {
         searchCriteria = searchCriteria.toLowerCase();
        List<Activity> activityList = session.createQuery("from com.thoughtworks.twu.domain.Activity where lower (client) like '%" + searchCriteria + "%' or lower (project) like '%" + searchCriteria + "%' or lower (sub_project) like '%" + searchCriteria + "%' escape '!'").list();
         JSONArray jsonArray = new JSONArray();
         for(Activity activity : activityList) {
             jsonArray.put(new JSONObject(activity));
         }
         return jsonArray;
     }
 
 
 }
