 package com.fs.humanResources.employee.controller;
 
 import com.fs.humanResources.employee.model.EmployeeModel;
 import com.fs.humanResources.employee.view.employee.EmployeeViewBean;
import org.apache.log4j.Logger;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @Named
 @RequestScoped
 public class EmployeeController {
 
    Logger log = Logger.getLogger(EmployeeController.class);

     @Inject
     EmployeeModel employeeModel;
 
     public EmployeeViewBean getEmployeeDetails() {
        log.info("Getting Employee Details");
         return employeeModel.getEmployee();
     }
 
 }
