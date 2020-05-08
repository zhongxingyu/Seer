 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Activity;
 import com.thoughtworks.twu.persistence.HibernateConnection;
import org.hibernate.Criteria;
 import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
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
 
         connection = HibernateConnection.getInstance();
         session = connection.getSession();
        searchCriteria = searchCriteria.replaceAll("_", "\\\\_");
 
         String[] searchString = searchCriteria.split("%");
 
        List<Activity>activityList = session.createCriteria(com.thoughtworks.twu.domain.Activity.class).
                 add(Restrictions.disjunction().add(Restrictions.ilike("client", "%" + searchCriteria + "%", MatchMode.EXACT)).
                         add(Restrictions.ilike("project", "%" + searchCriteria + "%",MatchMode.EXACT)).
                        add(Restrictions.ilike("sub_project", "%" + searchCriteria + "%",MatchMode.EXACT)))
                .list();
 
 
        JSONArray jsonArray = new JSONArray();
 
         for (Activity activity : activityList) {
             jsonArray.put(new JSONObject(activity));
         }
         return jsonArray;
     }
 }
