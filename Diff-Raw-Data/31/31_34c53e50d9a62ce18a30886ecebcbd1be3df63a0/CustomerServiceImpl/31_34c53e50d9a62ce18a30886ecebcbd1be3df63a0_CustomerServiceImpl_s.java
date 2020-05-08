 package org.xezz.timeregistration.services.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.xezz.timeregistration.model.Coworker;
 import org.xezz.timeregistration.model.Customer;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.model.TimeSpan;
 import org.xezz.timeregistration.repositories.CustomerRepository;
 import org.xezz.timeregistration.services.CustomerService;
 
 /**
  * User: Xezz
  * Date: 29.04.13
  * Time: 22:44
  */
 @Service
 public class CustomerServiceImpl implements CustomerService {
 
     @Autowired
     CustomerRepository repo;
 
     @Override
     public Iterable<Customer> customerByName(String name) {
         return repo.findByName(name);
     }
 
     @Override
     public Iterable<Customer> customerByNameMatch(String name) {
         // TODO: Revisit this
         // make sure we actually like
         // Should most likely not do it for now :D
         StringBuilder builder = new StringBuilder();
         if (!name.startsWith("%")) {
             builder.append("%");
         }
         builder.append(name);
         if (!name.endsWith("%")) {
             builder.append("%");
         }
         return repo.findByNameLike(builder.toString());
     }
 
     @Override
     public Customer customerByProject(Project p) {
         return p.getCustomer();
     }
 
     @Override
     public Customer customerByTimeFrame(TimeSpan t) {
         return t.getProject().getCustomer();
     }
 
     @Override
     public Iterable<Customer> customerByCoworker(Coworker c) {
         return repo.findCustomersByCoworker(c);
     }
 
     @Override
     public Iterable<Customer> customersAll() {
         return repo.findAllCustomers();
     }
 
     @Override
     public Customer customerById(Long id) {
         return repo.findOne(id);
     }
 
     @Override
     public Customer addNewCustomer(Customer c) {
         return repo.save(c);
     }
 
     @Override
     public Customer updateCustomer(Customer c) {
         if (repo.exists(c.getCustomerId())) {
             return repo.save(c);
         }
         // TODO: Can also save the Customer anyway, instead of discarding
         // This would be against pure REST, since a PUT means a resource exists already
         // On the other hand, might just do it sloppy
         return null;
     }
 }
