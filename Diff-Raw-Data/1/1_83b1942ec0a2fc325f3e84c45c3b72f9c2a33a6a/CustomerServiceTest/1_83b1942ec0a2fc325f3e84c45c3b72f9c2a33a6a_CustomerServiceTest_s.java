 package org.jz.services;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.jz.domain.CustomerId;
 import org.jz.domain.PersonCustomer;
 import org.jz.domain.PersonName;
 import org.jz.persistence.StubPersonCustomerDao;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 import static junit.framework.Assert.assertTrue;
 
 /**
  * @author Kristian Rosenvold
  */
 public class CustomerServiceTest
 {
 
     private final CustomerService customerService = new CustomerService( new StubPersonCustomerDao() );
 
      public static CustomerId customerId = new CustomerId( 123 );
     public static final CustomerId deletePassivated = new CustomerId( 124 );
 
     @Test
     public void testGetPersonCustomer()
         throws Exception
     {
         final PersonCustomer personCustomer = customerService.getPersonCustomer( customerId );
         assertNotNull( personCustomer);
     }
 
     @Test
     public void testFindCustomer()
         throws Exception
     {
         final List<PersonCustomer> personCustomers = customerService.findCustomers( new PersonName( "Jon", "Smith" ) );
         assertNotNull( personCustomers);
 
     }
 
     @Test
     public void testDeleteCustomer()
         throws Exception
     {
         customerService.deleteCustomer( deletePassivated );
         assertNull( customerService.getPersonCustomer( deletePassivated ) );
     }
 
     @Test
     public void testPassivateCustomer()
         throws Exception
     {
         customerService.passivateCustomer( deletePassivated );
         assertNull( customerService.getPersonCustomer( deletePassivated ) );
 
     }
 
     public void createANewCustomer(){
         final PersonName personName = new PersonName( "Fred", "Astaire" );
         customerService.createPersonCustomer( personName );
         assertTrue( customerService.findCustomers( personName ).size() == 1 );
     }
 }
