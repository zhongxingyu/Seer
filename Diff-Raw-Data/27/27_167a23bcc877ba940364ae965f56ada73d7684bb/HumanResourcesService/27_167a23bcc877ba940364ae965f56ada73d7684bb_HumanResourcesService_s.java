 package com.fs.humanResources.service;
 
 import com.fs.humanResources.dto.employee.EmployeeDTO;
 import com.fs.humanResources.model.employee.dao.EmployeeDAO;
 import com.fs.humanResources.model.employee.entities.Employee;
 import com.fs.humanResources.service.exception.EmployeeNotFoundException;
 
 import javax.persistence.NoResultException;
 
 public class HumanResourcesService {
 
     EmployeeDAO employeeDAO;
 
    public HumanResourcesService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

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
