 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Employee;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 
 @Service
 public class EmployeeService {
 
     private HibernateConnection connection;
     private Session session;
 
     public void saveEmployee(Employee employee) {
         session.getTransaction().begin();
         session.save(employee);
         session.getTransaction().commit();
     }
 
     public EmployeeService() {
         connection = HibernateConnection.getInstance();
         session = connection.getSession();
     }
 
     public List<Employee> getAllEmployees() {
         return session.createQuery("from com.thoughtworks.twu.domain.Employee").list();
     }
 
     public Employee getEmployeeByLogin(String loginName) {
 
         Criteria criteria = session.createCriteria(Employee.class);
 
         criteria.add(Restrictions.eq("login", loginName));
 
         List<Employee> employeeList = criteria.list();
         if(employeeList.size() == 0) {
             return null;
         } else {
             return employeeList.get(0);
         }
     }
 }
