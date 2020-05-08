 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.DeepCloneableArrayList;
 import com.sun.identity.admin.handler.BooleanActionsHandler;
 import com.sun.identity.entitlement.Entitlement;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.faces.model.SelectItem;
 
 public class ViewEntitlement implements Serializable {
 
     private List<Resource> resources = new ArrayList<Resource>();
     private List<Resource> exceptions = new ArrayList<Resource>();
     private BooleanActionsBean booleanActionsBean = new BooleanActionsBean();
     private ViewApplication viewApplication;
     private BooleanActionsHandler booleanActionsHandler = new BooleanActionsHandler();;
     private List<Resource> availableResources = new ArrayList<Resource>();
 
     public ViewEntitlement() {
         booleanActionsHandler.setBooleanActionsBean(booleanActionsBean);
     }
 
     public ViewEntitlement(Entitlement e, Map<String, ViewApplication> viewApplications) {
         this();
         
         if (e == null) {
             return;
         }
 
         // application
         viewApplication = viewApplications.get(e.getApplicationName());
 
         // resources
         for (String rs : e.getResourceNames()) {
             String resourceClassName = viewApplication.getViewApplicationType().getResourceClassName();
             Resource r;
             try {
                 r = (Resource) Class.forName(resourceClassName).newInstance();
             } catch (ClassNotFoundException cnfe) {
                 throw new RuntimeException(cnfe);
             } catch (InstantiationException ie) {
                 throw new RuntimeException(ie);
             } catch (IllegalAccessException iae) {
                 throw new RuntimeException(iae);
             }
             r.setName(rs);
             resources.add(r);
         }
 
         resetAvailableResources();
 
         // exceptions
         for (String rs : e.getExcludedResourceNames()) {
             String resourceClassName = viewApplication.getViewApplicationType().getResourceClassName();
             Resource r;
             try {
                 r = (Resource) Class.forName(resourceClassName).newInstance();
             } catch (ClassNotFoundException cnfe) {
                 throw new RuntimeException(cnfe);
             } catch (InstantiationException ie) {
                 throw new RuntimeException(ie);
             } catch (IllegalAccessException iae) {
                 throw new RuntimeException(iae);
             }
             r.setName(rs);
             exceptions.add(r);
         }
         
 
         // actions
         for (String actionName: e.getActionValues().keySet()) {
             Boolean actionValue = e.getActionValues().get(actionName);
             BooleanAction ba = new BooleanAction();
             ba.setName(actionName);
             ba.setAllow(actionValue.booleanValue());
             booleanActionsBean.getActions().add(ba);
         }
     }
 
     private void resetAvailableResources() {
         availableResources = new DeepCloneableArrayList<Resource>(viewApplication.getResources()).deepClone();
         for (Resource r: resources) {
             if (!availableResources.contains(r)) {
                 availableResources.add(r);
             }
         }
     }
 
     public List<Resource> getResources() {
         return resources;
     }
 
     public List<Resource> getExceptions() {
         return exceptions;
     }
 
     public void setResources(List<Resource> resources) {
         this.resources = resources;
         resetAvailableResources();
     }
 
     public Resource[] getResourceArray() {
         return resources.toArray(new Resource[0]);
     }
 
     public void setResourceArray(Resource[] resourceArray) {
         resources = Arrays.asList(resourceArray);
     }
 
     public Entitlement getEntitlement() {
         Entitlement e = new Entitlement();
 
         e.setResourceNames(getResourceSet());
         e.setExcludedResourceNames(getExceptionSet());
         e.setActionValues(getActionMap());
         e.setApplicationName(viewApplication.getName());
 
         return e;
     }
 
     private Set<String> getResourceSet() {
         Set<String> resourceSet = new HashSet<String>();
 
         for (Resource r : resources) {
             resourceSet.add(r.getName());
         }
 
         return resourceSet;
     }
 
     private Set<String> getExceptionSet() {
         Set<String> exceptionSet = new HashSet<String>();
 
         for (Resource r : exceptions) {
             exceptionSet.add(r.getName());
         }
 
         return exceptionSet;
     }
 
     private Map<String, Boolean> getActionMap() {
         Map<String, Boolean> actionMap = new HashMap<String, Boolean>();
 
         for (Action a : booleanActionsBean.getActions()) {
             actionMap.put(a.getName(), (Boolean)a.getValue());
         }
 
         return actionMap;
     }
 
     public ViewApplication getViewApplication() {
         return viewApplication;
     }
 
     public void setViewApplication(ViewApplication viewApplication) {
         this.viewApplication = viewApplication;
         resetAvailableResources();
     }
 
     public String getListToString(List list) {
         StringBuffer b = new StringBuffer();
 
         for (Iterator<Resource> i = list.iterator(); i.hasNext();) {
             b.append(i.next());
             if (i.hasNext()) {
                 b.append(",");
             }
 
         }
 
         return b.toString();
     }
 
     public String getResourcesToString() {
         return getListToString(resources);
     }
 
     public String getExceptionsToString() {
         return getListToString(exceptions);
     }
 
     public String getListToFormattedString(List list) {
         StringBuffer b = new StringBuffer();
 
         for (Iterator<Resource> i = list.iterator(); i.hasNext();) {
             b.append(i.next());
             if (i.hasNext()) {
                 b.append("\n");
             }
 
         }
 
         return b.toString();
     }
 
     public String getResourcesToFormattedString() {
         return getListToFormattedString(resources);
     }
 
     public String getExceptionsToFormattedString() {
         return getListToFormattedString(exceptions);
     }
 
     public BooleanActionsBean getBooleanActionsBean() {
         return booleanActionsBean;
     }
 
     public BooleanActionsHandler getBooleanActionsHandler() {
         return booleanActionsHandler;
     }
 
     public List<Resource> getAvailableResources() {
         return availableResources;
     }
 
     public List<SelectItem> getAvailableResourceItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
        for (Resource r : getAvailableResources()) {
             items.add(new SelectItem(r, r.getName()));
         }
 
         return items;
     }
 }
