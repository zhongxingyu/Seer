 package org.springsource.examples.crm.services.jpa;
 
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springsource.examples.crm.model.Customer;
 import org.springsource.examples.crm.services.CustomerService;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 @Service
 public class JpaDatabaseCustomerService implements CustomerService {
 
   @PersistenceContext
   private EntityManager entityManager;
 
   @Transactional(readOnly = true)
   public Customer getCustomerById(long id) {
     return this.entityManager.find(Customer.class, id);
   }
 
   @Transactional
   public Customer createCustomer(String fn, String ln) {
     Customer newCustomer = new Customer();
     newCustomer.setFirstName(fn);
     newCustomer.setLastName(ln);
    this.entityManager.persist(newCustomer);
     return newCustomer;
   }
 }
