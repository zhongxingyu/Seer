 package com.fs.humanResources.service;
 
 import com.fs.humanResources.dto.employee.EmployeeDTO;
 import com.fs.humanResources.model.employee.dao.EmployeeDAO;
 import com.fs.humanResources.model.employee.entities.Employee;
 import com.fs.humanResources.service.exception.EmployeeNotFoundException;
 
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
 import javax.persistence.NoResultException;
 
@Named
@Stateless
 public class HumanResourcesService {
 
    @Inject
     EmployeeDAO employeeDAO;
 
     public EmployeeDTO getEmployeeDetails(Long staffNumber) throws EmployeeNotFoundException {
         try {
 
             Employee employee = employeeDAO.getEmployeeDetails(staffNumber);
             return new EmployeeDTO(employee);
 
         } catch (NoResultException e) {
             throw new EmployeeNotFoundException(e);
         }
     }
 
     public void saveEmployeeDetails(EmployeeDTO employeeDTO) {
         employeeDAO.create(employeeDTO.getEntity());
     }
 
     public void updateEmployeeDetails(EmployeeDTO employeeDTO) {
         employeeDAO.update(employeeDTO.getEntity());
     }
 
 }
