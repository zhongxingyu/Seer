 package me.alanfoster.employee.webservice;
 
import me.alanfoster.services.employee.models.IEmployee;
 import me.alanfoster.services.employee.models.impl.Employee;
 import me.alanfoster.services.employee.service.IEmployeeService;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import java.util.List;
 
 /**
  * @author Alan Foster
  * @version 1.0.0-SNAPSHOT
  */
 @WebService(
         endpointInterface = "me.alanfoster.employee.webservice.IEmployeeWebservice",
         serviceName = "EmployeeWebService",
         portName = "EmployeeWebservicePort")
 public class EmployeeWebservice implements IEmployeeWebservice {
     @Autowired
     private IEmployeeService employeeService;
 
     @Override
     public final Integer createEmployee(@WebParam(name = "employee") final Employee employee) {
         return employeeService.create(employee);
     }
 
     @Override
     public final Employee getEmployee(@WebParam(name = "employeeId") final Integer employeeId) {
         return (Employee) employeeService.get(employeeId);
     }
 
     @Override
     public final List<Employee> getAllEmployees() {
         return (List) employeeService.getAll();
     }
 
     @Override
     public final void updateEmployee(@WebParam(name = "object") final Employee employee) {
         employeeService.update(employee);
     }
 
     @Override
     public final void deleteEmployee(@WebParam(name = "employee") final Employee employee) {
         employeeService.delete(employee);
     }
 
     @Override
     public final void deleteEmployeeByEmployeeId(@WebParam(name = "employeeId") final Integer employeeId) {
         employeeService.delete(employeeId);
     }
 }
