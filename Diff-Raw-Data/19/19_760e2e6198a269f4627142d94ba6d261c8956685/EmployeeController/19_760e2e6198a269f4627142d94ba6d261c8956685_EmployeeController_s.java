 package com.fs.humanResources.employee.controller;
 
 import com.fs.humanResources.employee.model.EmployeeModel;
 import com.fs.humanResources.employee.view.employee.EmployeeViewBean;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @Named
 @RequestScoped
 public class EmployeeController {
 
     @Inject
     EmployeeModel employeeModel;
 
     public EmployeeViewBean getEmployeeDetails() {
         return employeeModel.getEmployee();
     }
 
 }
