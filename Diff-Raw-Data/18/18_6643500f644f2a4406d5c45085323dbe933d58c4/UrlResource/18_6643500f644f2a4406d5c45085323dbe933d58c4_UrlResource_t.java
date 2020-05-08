 package com.sun.identity.admin.model;
 
 import com.sun.identity.entitlement.Entitlement;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class UrlResource implements Resource, Serializable {
 
     private boolean selected = false;
     private List<Resource> exceptions = new ArrayList<Resource>();
     private String pattern;
     private boolean excepted = false;
     private boolean exceptionsShown = true;
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
     public String getName() {
         return pattern;
     }
 
     public String getPattern() {
         return pattern;
     }
 
     public String getExceptedPattern() {
         if (isExceptable()) {
             return pattern.substring(0, pattern.length() - 2);
         }
         return pattern;
     }
 
     public void setPattern(String pattern) {
         this.pattern = pattern;
     }
 
     public boolean isExcepted() {
         return excepted;
     }
 
     public void setExcepted(boolean excepted) {
         this.excepted = excepted;
     }
 
     public List<Resource> getExceptions() {
         return exceptions;
     }
 
     public void setExceptions(List<Resource> exceptions) {
         this.exceptions = exceptions;
     }
 
     public boolean isExceptionsShown() {
         return exceptionsShown && excepted;
     }
 
     public void setExceptionsShown(boolean exceptionsShown) {
         this.exceptionsShown = exceptionsShown;
     }
 
     public boolean isExceptable() {
         return pattern.endsWith("*");
     }
 
     @Override
     public boolean equals(Object o) {
         UrlResource other = (UrlResource) o;
         if (other.pattern.equals(pattern)) {
             return true;
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return pattern.hashCode();
     }
 
     public Entitlement getEntitlement(Collection<Action> actions) {
         Map<String, Boolean> actionValues = new HashMap<String, Boolean>();
         for (Action a : actions) {
             if (a.getValue() != null) {
                 //TODO: fix to use correct Boolean value
                actionValues.put(a.getName(), a.getValue());
             }
         }
 
         Set<String> excludedResourceNames = new HashSet<String>();
         for (Resource r : exceptions) {
             String excludedResourceName = getExceptedPattern() + "/" + r.getName();
             excludedResourceNames.add(excludedResourceName);
         }
 
         // TODO: use correct service name
         Entitlement e = new Entitlement("iPlanetAMWebAgentService", pattern, actionValues);
         e.setExcludedResourceNames(excludedResourceNames);
 
         return e;
     }
 }
