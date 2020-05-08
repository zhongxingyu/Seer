 package com.masternaut.repository.system;
 
 import com.masternaut.PaddingtonException;
 import com.masternaut.domain.Customer;
 import com.masternaut.repository.BaseSystemRepositoryTest;
import com.masternaut.unitTestHelpers.BaseCustomerRepositoryTest;
 import com.mongodb.MongoURI;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class CustomerRepository2Test extends BaseSystemRepositoryTest{
     @Autowired
     private CustomerRepository2 customerRepository;
 
     @Before
     public void before() {
         customerRepository.deleteAll();
     }
 
     @Test
     public void save_and_findById() {
         Customer customer = new Customer();
         customer.setName("MyCustomerName");
        customer.setMongoUri(BaseCustomerRepositoryTest.createDefaultLocalConnection("MyDatabaseName"));
 
         customerRepository.save(customer);
 
         Customer loadedCustomer = customerRepository.findById(customer.getId());
 
         assertEquals(customer.getId(), loadedCustomer.getId());
         assertEquals("MyCustomerName", loadedCustomer.getName());
 
         assertEquals("MyDatabaseName", new MongoURI(loadedCustomer.getMongoUri()).getDatabase());
     }
 
     @Test
     public void tryFindById_when_present() {
         Customer customer = new Customer();
         customer.setName("MyCustomerName");
        customer.setMongoUri(BaseCustomerRepositoryTest.createDefaultLocalConnection("MyDatabaseName"));
 
         customerRepository.save(customer);
 
         Customer loadedCustomer = customerRepository.tryFindById(customer.getId());
 
         assertEquals(customer.getId(), loadedCustomer.getId());
     }
 
 
     @Test
     public void loadByInvalidId() {
         try {
             customerRepository.findById("NotAValidCustomerId");
             fail("Exception not thrown");
         } catch (PaddingtonException pe) {
             assertEquals("Cannot find Customer with id of 'NotAValidCustomerId'", pe.getMessage());
         }
     }
 
     @Test
     public void countRecords() {
         Customer customer = new Customer();
         customer.setName("MyCustomerName");
 
         customerRepository.save(customer);
 
         assertEquals(1, customerRepository.count());
     }
 
     @Test
     public void deleteAll() {
         Customer customer = new Customer();
         customerRepository.save(customer);
 
         customerRepository.deleteAll();
 
         long count = customerRepository.count();
 
         assertEquals(0, count);
     }
 
     @Test
     public void findByName() {
         Customer customer1 = new Customer();
         customer1.setName("MyCustomer1");
 
         Customer customer2 = new Customer();
         customer2.setName("MyCustomer2");
 
         customerRepository.save(customer1);
         customerRepository.save(customer2);
 
         Customer foundCustomer = customerRepository.tryFindByName("MyCustomer1");
 
         assertEquals(foundCustomer.getId(), customer1.getId());
         assertEquals(foundCustomer.getName(), customer1.getName());
     }
 
     @Test
     public void findAll(){
         Customer customer1 = new Customer();
         customer1.setName("MyCustomer1");
 
         Customer customer2 = new Customer();
         customer2.setName("MyCustomer2");
 
         customerRepository.save(customer1);
         customerRepository.save(customer2);
 
         List<Customer> customerList = customerRepository.findAll();
 
         assertEquals(2, customerList.size());
         assertEquals("MyCustomer1", customerList.get(0).getName());
         assertEquals("MyCustomer2", customerList.get(1).getName());
     }
 
     @Test
     public void findByNameWhenNoMatch() {
         Customer foundCustomer = customerRepository.tryFindByName("NotACustomer");
         assertNull(foundCustomer);
     }
 
     @Test
     public void findByNameWhenTooManyMatch() {
         String customerName = "MySameCustomerName";
 
         Customer customer1 = new Customer();
         customer1.setName(customerName);
 
         Customer customer2 = new Customer();
         customer2.setName(customerName);
 
         customerRepository.save(customer1);
         customerRepository.save(customer2);
 
         try {
             customerRepository.tryFindByName(customerName);
             fail("exception not thrown");
         } catch (Throwable t) {
             assertEquals(String.format("Too many customers found with name: '%s'.", customerName), t.getMessage());
         }
     }
 
     @Test
     public void caching_findById(){
         Customer customer = new Customer();
         customer.setName("MyName");
         customerRepository.save(customer);
 
         Customer loadedCustomer1 = customerRepository.findById(customer.getId());
         Customer loadedCustomer2 = customerRepository.findById(customer.getId());
 
         assertSame(loadedCustomer1,  loadedCustomer2);
     }
 
     @Test
     public void caching_tryFindById(){
         Customer customer = new Customer();
         customer.setName("MyName");
         customerRepository.save(customer);
 
         Customer loadedCustomer1 = customerRepository.tryFindById(customer.getId());
         Customer loadedCustomer2 = customerRepository.tryFindById(customer.getId());
 
         assertSame(loadedCustomer1,  loadedCustomer2);
     }
 
     @Test
     public void delete(){
         Customer customer1 = new Customer();
         customer1.setName("a");
 
         Customer customer2 = new Customer();
         customer2.setName("b");
 
         customerRepository.save(customer1,  customer2);
 
         customerRepository.deleteById(customer1.getId());
 
         List<Customer> allCustomers = customerRepository.findAll();
         assertEquals(1, allCustomers.size());
         assertEquals("b", allCustomers.get(0).getName());
     }
 }
