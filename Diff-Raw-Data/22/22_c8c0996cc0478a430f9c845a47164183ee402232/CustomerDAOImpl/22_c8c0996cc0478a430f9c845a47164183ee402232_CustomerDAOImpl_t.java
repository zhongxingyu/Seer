 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.dao;
 
 import cz.muni.fi.pa165.jtravelagency.CustomerStatus;
 import cz.muni.fi.pa165.jtravelagency.entity.Customer;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
 import javax.persistence.TypedQuery;
 import org.springframework.stereotype.Repository;
 
 
 /**
  *
  * @author Marian Varaga
  */
 @Repository
 public class CustomerDAOImpl implements CustomerDAO {
     
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
     private EntityManager em;
 
     public CustomerDAOImpl() {
     }
     
     public CustomerDAOImpl(EntityManager em) {
         if(em==null)
             throw new IllegalArgumentException("Entity manager cannot be null");
         this.em = em;
     }
 
     public void createCustomer(Customer customer) {
         if(customer == null) {
             throw new IllegalArgumentException("Customer cannot be null");
         }
         if(customer.getId() != null) {
             throw new IllegalArgumentException("Id has to be null");
         }
         
         validateCustomer(customer);
         em.persist(customer);
     }
     
     public Customer getCustomer(Long id) {
         if(id==null) {
             throw new IllegalArgumentException("Id cannot be null");
         }
         return em.find(Customer.class, id);
     }
 
     public void updateCustomer(Customer customer) {
         if(customer==null){
             throw new IllegalArgumentException("Customer cannot be null");
         }
         if(customer.getId()==null){
             throw new IllegalArgumentException("Id cannot be null");
         }
             
         validateCustomer(customer);
 
         em.merge(customer);
         em.flush();
         em.detach(customer);
     }
 
     public void deleteCustomer(Customer customer) {
         if(customer==null){
             throw new IllegalArgumentException("Customer cannot be null");
         }
         if(customer.getId()==null){
             throw new IllegalArgumentException("Id cannot be null");
         }
         
         validateCustomer(customer);
         
         em.remove(customer);
         em.flush();
         //em.detach(em);
     }
 
     public List<Customer> getAllCustomers() {
         TypedQuery<Customer> query = em.createNamedQuery(
                 "findAllCustomers", Customer.class);
         return query.getResultList();
     }
 
     // todo defenzivne???
     public void setDeletedStatus(Customer customer) {
 //        Customer c = getCustomer(customer.getId());
 //        c.setStatus(CustomerStatus.DELETED);
         customer.setStatus(CustomerStatus.DELETED);
         updateCustomer(customer);
         
     }
 
     private void validateCustomer(Customer customer){
         if(customer.getFirstName()==null)
             throw new IllegalArgumentException("FirstName cannot be null");
         if(customer.getLastName()==null)
             throw new IllegalArgumentException("LastName cannot be null");
         if(customer.getStatus()==null)
             throw new IllegalArgumentException("Status cannot be null");
     }
     
 }
