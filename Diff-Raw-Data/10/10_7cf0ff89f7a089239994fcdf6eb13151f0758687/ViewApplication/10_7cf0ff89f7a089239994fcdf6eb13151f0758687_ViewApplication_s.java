 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.ManagedBeanResolver;
 import com.sun.identity.admin.Resources;
 import com.sun.identity.admin.dao.ViewApplicationTypeDao;
 import com.sun.identity.entitlement.Application;
 import com.sun.identity.entitlement.ApplicationManager;
 import com.sun.identity.entitlement.EntitlementException;
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
     private List<ConditionType> conditionTypes = new ArrayList<ConditionType>();
     private List<SubjectType> subjectTypes = new ArrayList<SubjectType>();
 
     public ViewApplication(Application a) {
         ManagedBeanResolver mbr = new ManagedBeanResolver();
 
         name = a.getName();
 
         // application type
         Map<String, ViewApplicationType> entitlementApplicationTypeToViewApplicationTypeMap = (Map<String, ViewApplicationType>) mbr.resolve("entitlementApplicationTypeToViewApplicationTypeMap");
         viewApplicationType = entitlementApplicationTypeToViewApplicationTypeMap.get(a.getApplicationType().getName());
 
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
         for (String actionName : a.getActions().keySet()) {
             Boolean value = a.getActions().get(actionName);
             BooleanAction ba = new BooleanAction();
             ba.setName(actionName);
             ba.setAllow(value.booleanValue());
             actions.add(ba);
         }
 
         // conditions
         ConditionTypeFactory ctf = (ConditionTypeFactory) mbr.resolve("conditionTypeFactory");
         for (String viewConditionClassName : a.getConditions()) {
             Class c;
             try {
                 c = Class.forName(viewConditionClassName);
             } catch (ClassNotFoundException cnfe) {
                 // TODO: log
                 continue;
             }
             ConditionType ct = ctf.getConditionType(c);
             assert (ct != null);
             conditionTypes.add(ct);
         }
 
         // subjects
         SubjectFactory sf = (SubjectFactory) mbr.resolve("subjectFactory");
         for (String viewSubjectClassName : a.getSubjects()) {
             SubjectType st = sf.getSubjectType(viewSubjectClassName);
             assert (st != null);
             subjectTypes.add(st);
         }
     }
 
     public List<SubjectContainer> getSubjectContainers() {
         ManagedBeanResolver mbr = new ManagedBeanResolver();
         SubjectFactory sf = (SubjectFactory) mbr.resolve("subjectFactory");
         List<SubjectContainer> subjectContainers = new ArrayList<SubjectContainer>();
         for (SubjectType st : subjectTypes) {
             SubjectContainer sc = sf.getSubjectContainer(st);
             if (sc != null && sc.isVisible()) {
                 subjectContainers.add(sc);
             }
         }
 
         return subjectContainers;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTitle() {
         Resources r = new Resources();
         String title = r.getString(this, "title." + name);
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
         //
         // this is really just modifies the applications.
         //
 
         // TODO: realm
         Application app = ApplicationManager.getApplication("/", name);
 
         // resources
         Set<String> resourceStrings = new HashSet<String>();
         for (Resource r : resources) {
             resourceStrings.add(r.getName());
         }
         app.addResources(resourceStrings);
 
         // actions
         Map appActions = app.getActions();
         for (Action action : actions) {
             if (!appActions.containsKey(action.getName())) {
                 try {
                     app.addAction(action.getName(), (Boolean) action.getValue());
                 } catch (EntitlementException ex) {
                     //TODO
                 }
             }
         }
 
         // conditions
         //TODO
 
         return app;
     }
 
     public List<ConditionType> getConditionTypes() {
         return conditionTypes;
     }
 
     public void setConditionTypes(List<ConditionType> conditionTypes) {
         this.conditionTypes = conditionTypes;
     }
 }
