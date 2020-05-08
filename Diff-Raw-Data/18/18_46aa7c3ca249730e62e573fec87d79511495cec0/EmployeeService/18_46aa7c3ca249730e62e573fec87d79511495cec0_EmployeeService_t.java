 package net.itneering.demo.struts2evolution.service;
 
 import net.itneering.demo.struts2evolution.model.Employee;
 
import java.util.List;

 /**
  * EmployeeService.
  *
  * @author Rene Gielen
  */
 public class EmployeeService extends AbstractEntityService {
 
     public Class entityClass() {
         return Employee.class;
     }
 
    public List<Employee> getEmployeeByName( String name ) {
        return getCurrentSession().getNamedQuery("Employee.getByName")
                .setString("name", name)
                .list();
    }

 }
