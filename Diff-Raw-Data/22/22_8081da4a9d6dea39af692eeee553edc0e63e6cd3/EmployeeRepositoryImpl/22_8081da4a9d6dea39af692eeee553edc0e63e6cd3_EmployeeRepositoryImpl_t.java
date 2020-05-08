 package com.fourneth.ims.repository.impl;
 
 import com.fourneth.ims.domain.Employee;
 import com.fourneth.ims.repository.EmployeeRepository;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: romith
  * Date: 2/4/13
  * Time: 11:59 PM
  * To change this template use File | Settings | File Templates.
  */
 @Repository
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
 public class EmployeeRepositoryImpl implements EmployeeRepository {
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @Override
     public void save(Employee e) {
         entityManager.persist(e);
     }
 
     @Override
     public void update(Employee e) {
         entityManager.persist(e);
     }
 
     @Override
     public void delete(Employee e) {
         entityManager.remove(e);
     }
 
     @Override
     public Employee findById(int id) {
         return entityManager.find(Employee.class, id);
     }
 
     @Override
     public List<Employee> findAll() {
         return entityManager.createQuery("select e from EMPLOYEE e").getResultList();
     }
 }
