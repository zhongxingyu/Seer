 package com.fs.humanResources.model.employee.dao;
 
 import com.fs.humanResources.model.common.dao.BaseDAO;
 import com.fs.humanResources.model.employee.entities.Employee;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 
 public class EmployeeDAO extends BaseDAO<Employee> {
 
     public EmployeeDAO() {
         super(Employee.class);
     }
 
     public EmployeeDAO(EntityManager entityManager) {
         super(Employee.class, entityManager);
     }
 
     public Employee getEmployeeDetails(Long employeeId) throws NoResultException {
         return findById(employeeId);
     }
 }
