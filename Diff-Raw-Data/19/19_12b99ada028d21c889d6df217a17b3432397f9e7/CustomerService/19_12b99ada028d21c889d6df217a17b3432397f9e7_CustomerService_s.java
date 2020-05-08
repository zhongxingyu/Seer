 package org.jz.services;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.jz.domain.CustomerId;
 import org.jz.domain.PersonCustomer;
 import org.jz.domain.PersonName;
 import org.jz.persistence.PersonCustomerDao;
 
 /**
  * @author Kristian Rosenvold
  */
 public class CustomerService {
     private final PersonCustomerDao personCustomerDao;
 
     @Inject
     public CustomerService( PersonCustomerDao deleteCustomerDao )
     {
         this.personCustomerDao = deleteCustomerDao;
     }
 
 
     public PersonCustomer getPersonCustomer( CustomerId customerId )
         throws SQLException
     {
         return personCustomerDao.getPersonCustomer( customerId );
     }
 
     public List<PersonCustomer> findCustomers( PersonName personName )
     {
         return personCustomerDao.findPersonCustomers( personName );
     }
 
     public void deleteCustomer( CustomerId customerId ){
         personCustomerDao.deleteCustomer( customerId );
     }
 
 
     public void passivateCustomer( CustomerId customerId ){
         personCustomerDao.passivateCustomer( customerId );
     }
 
 
 }
