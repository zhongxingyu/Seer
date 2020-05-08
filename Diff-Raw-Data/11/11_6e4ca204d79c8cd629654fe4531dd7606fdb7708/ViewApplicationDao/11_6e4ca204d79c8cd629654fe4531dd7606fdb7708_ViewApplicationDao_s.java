 package com.sun.identity.admin.dao;
 
 import com.sun.identity.admin.model.Action;
 import com.sun.identity.admin.model.BooleanAction;
 import com.sun.identity.admin.model.Resource;
 import com.sun.identity.admin.model.ViewApplication;
 import com.sun.identity.admin.model.ViewApplicationType;
 import com.sun.identity.entitlement.Application;
 import com.sun.identity.entitlement.ApplicationManager;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ViewApplicationDao implements Serializable {
 
     private ViewApplicationTypeDao viewApplicationTypeDao;
     private Map<String, ViewApplication> viewApplications = new HashMap<String, ViewApplication>();
    private String[] tempResources = new String[]{"http://a/*", "http://b", "https://c", "http://1/*", "http://12", "http://13"};
 
     public ViewApplicationTypeDao getViewApplicationTypeDao() {
         return viewApplicationTypeDao;
     }
 
     public void setViewApplicationTypeDao(ViewApplicationTypeDao viewApplicationTypeDao) {
         this.viewApplicationTypeDao = viewApplicationTypeDao;
 
         for (String name : ApplicationManager.getApplicationNames("/")) {
             // TODO: pass in realm instead of null
             Application a = ApplicationManager.getApplication("/", name);
             ViewApplication va = new ViewApplication();
 
             // name
             va.setName(a.getName());
 
             // application type
             ViewApplicationType vat = viewApplicationTypeDao.getViewApplicationTypes().get(a.getApplicationType().getName());
             if (vat == null) {
                 continue;
             }
             
             va.setViewApplicationType(vat);
 
             // resources
             List<Resource> resources = new ArrayList<Resource>();
            for (String resourceString : tempResources) {
                 Resource r;
                 try {
                     r = (Resource) Class.forName(vat.getResourceClassName()).newInstance();
                 } catch (ClassNotFoundException cnfe) {
                     throw new RuntimeException(cnfe);
                 } catch (InstantiationException ie) {
                     throw new RuntimeException(ie);
                 } catch (IllegalAccessException iae) {
                     throw new RuntimeException(iae);
                 }
                 r.setName(resourceString);
                 resources.add(r);
             }
             va.setResources(resources);
 
             // actions
             List<Action> actions = new ArrayList<Action>();
             for (String actionName: a.getActions().keySet()) {
                 Boolean value = a.getActions().get(actionName);
                 BooleanAction ba = new BooleanAction();
                 ba.setName(actionName);
                 ba.setAllow(value.booleanValue());
                 actions.add(ba);
             }
             va.setActions(actions);
 
             // conditions
             // TODO
 
             getViewApplications().put(va.getName(), va);
         }
     }
 
     public Map<String, ViewApplication> getViewApplications() {
         return viewApplications;
     }
 
     public void setViewApplications(Map<String, ViewApplication> viewApplications) {
         this.viewApplications = viewApplications;
     }
 }
