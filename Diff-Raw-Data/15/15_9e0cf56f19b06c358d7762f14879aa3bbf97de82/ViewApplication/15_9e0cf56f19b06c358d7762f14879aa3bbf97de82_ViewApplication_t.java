 package com.sun.identity.admin.model;
 
import com.sun.identity.admin.Resources;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ViewApplication implements Serializable {
     private String name;
     private ViewApplicationType viewApplicationType;
     private List<Resource> resources = new ArrayList<Resource>();
     private List<Action> actions = new ArrayList<Action>();
 
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
 
 }
