 package com.fs.humanResources.employee.model;
 
 import com.fs.humanResources.employee.view.address.AddressViewBean;
 import com.fs.humanResources.employee.view.employee.EmployeeViewBean;
import org.apache.log4j.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 import java.io.Serializable;
 import java.util.Date;
 
 @Named
 @SessionScoped
 public class EmployeeModel implements Serializable {
 
    Logger log = Logger.getLogger(EmployeeModel.class);

     EmployeeViewBean employee;
     AddressViewBean address;
 
     @PostConstruct
     public void setUpEmployee() {
         String firstName = "Joe";
         String lastName = "Smith";
         Date dataOfBirth = new Date();
         Long employeeId = 12345l;
 
         String houseNumber = "50" ;
         String addressFirstLine = "Test Driven Way";
         String addressSecondLine = "Domain Court";
         String townCity = "Progammer City";
         String postCode = "AB1 CDXY";
 
         address = new AddressViewBean(houseNumber,addressFirstLine,addressSecondLine,townCity,postCode);
         employee = new EmployeeViewBean(firstName,lastName,dataOfBirth,employeeId,address);
     }
 
     public EmployeeViewBean getEmployee() {
        log.info("Get Employee");
         return employee;
     }
 }
