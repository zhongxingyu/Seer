 package com.sun.identity.admin.handler;
 
 import javax.faces.event.ActionEvent;
 
 public interface PolicySummaryHandler {
     public void editNameListener(ActionEvent event);
     public void editResourcesListener(ActionEvent event);
     public void editExceptionsListener(ActionEvent event);
     public void editSubjectsListener(ActionEvent event);
     public void editConditionsListener(ActionEvent event);
     public void editActionsListener(ActionEvent event);
    public void editApplicationListener(ActionEvent event);
 }
