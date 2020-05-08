 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.Resources;
 import com.sun.identity.admin.dao.ViewApplicationTypeDao;
 import com.sun.identity.entitlement.Application;
 import com.sun.identity.entitlement.ApplicationManager;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ViewApplication implements Serializable {
     private String name;
     private ViewApplicationType viewApplicationType;
     private List<Resource> resources = new ArrayList<Resource>();
     private List<Action> actions = new ArrayList<Action>();
 
     public ViewApplication(Application a, ViewApplicationType viewApplicationType) {
             setName(a.getName());
 
             setViewApplicationType(viewApplicationType);
 
             // resources
             for (String resourceString : a.getResources()) {
                 Resource r;
                 try {
                     r = (Resource) Class.forName(viewApplicationType.getResourceClassName()).newInstance();
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
 
             // actions
             for (String actionName: a.getActions().keySet()) {
                 Boolean value = a.getActions().get(actionName);
                 BooleanAction ba = new BooleanAction();
                 ba.setName(actionName);
                 ba.setAllow(value.booleanValue());
                 actions.add(ba);
             }
 
             // conditions
             // TODO
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTitle() {
         Resources r = new Resources();
         String title = r.getString(this.getClass(), "title."+name);
         return title;
     }
 
     public ViewApplicationType getViewApplicationType() {
         return viewApplicationType;
     }
 
     public void setViewApplicationType(ViewApplicationType viewApplicationType) {
         this.viewApplicationType = viewApplicationType;
     }
 
     public List<Resource> getResources() {
         return resources;
     }
 
     public void setResources(List<Resource> resources) {
         this.resources = resources;
     }
 
     public List<Action> getActions() {
         return actions;
     }
 
     public void setActions(List<Action> actions) {
         this.actions = actions;
     }
 
     public Application toApplication(ViewApplicationTypeDao viewApplicationTypeDao) {
        // TODO
         //
         // this is really just modifies the applications.
         //
 
         Application app = ApplicationManager.getApplication("/", name);
 
         // resources
         Set<String> resourceStrings = new HashSet<String>();
         for (Resource r: resources) {
             resourceStrings.add(r.getName());
         }
         app.addResources(resourceStrings);
 
         // actions
         Map appActions = app.getActions();
         for (Action action: actions) {
             if (!appActions.containsKey(action.getName())) {
                 app.addAction(action.getName(), (Boolean)action.getValue());
             }
         }
 
         // conditions
         //TODO
 
         return app;
     }
 
 }
