 package net.itneering.demo.struts2evolution.service;
 
 import net.itneering.demo.struts2evolution.model.Employee;
 
 /**
  * EmployeeService.
  *
  * @author Rene Gielen
  */
 public class EmployeeService extends AbstractEntityService {
 
     public Class entityClass() {
         return Employee.class;
     }
 
 }
