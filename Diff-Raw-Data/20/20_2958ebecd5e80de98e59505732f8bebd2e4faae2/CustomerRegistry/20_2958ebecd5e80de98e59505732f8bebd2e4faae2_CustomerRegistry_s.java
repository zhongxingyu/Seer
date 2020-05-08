 package com.mycompany.booking.core;
 
 import com.mycompany.booking.utils.AbstractDAO;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManagerFactory;
 
 /**
  * All customers
  *
  * @author hajo Details changed to fit our project
  *
  */
 public final class CustomerRegistry
         extends AbstractDAO<Customer, Long> implements ICustomerRegistry {
 
     public CustomerRegistry(EntityManagerFactory emf) {
         super(Customer.class, emf);
     }
 
     // Factory method
     public static ICustomerRegistry newInstance(EntityManagerFactory emf) {
         return new CustomerRegistry(emf);
     }
 
     @Override
     public List<Customer> getByName(String name) {
         List<Customer> found = new ArrayList<Customer>();
         for (Customer c : getRange(0, getCount())) {
             if (c.getFname().equals(name) || c.getLname().equals(name)) {
                 found.add(c);
             }
         }
         return found;
     }
 
/*    @Override
     public boolean authenticate(String name, String password) {
         for (Customer c : getRange(0, getCount())) {
             if (c.getEmail().equalsIgnoreCase(name)) {
                 return c.getPassword().equals(Encrypter.enCrypt(password));
             }
         }
         return false;
    }*/

    public boolean authenticate(String name, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
