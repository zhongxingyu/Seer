 package com.masternaut.unitTestHelpers;
 
 import com.masternaut.domain.Customer;
import com.masternaut.domain.PaddingtonMongoUriHelper;
 import com.masternaut.factory.CustomerMongoFactory;
 import com.masternaut.repository.system.CustomerRepository2;
 import com.mongodb.MongoURI;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.util.Assert;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/spring/UnitTest-context.xml")
 public abstract class BaseCustomerRepositoryTest {
 
     @Autowired
     protected CustomerMongoFactory customerMongoFactory;
 
     @Autowired
     protected CustomerRepository2 customerRepository;
 
     protected Customer customer1;
     protected Customer customer2;
     protected Customer customer3;
 
     @Before
     public void baseBefore() {
         customerRepository.deleteAll();
 
         customer1 = createAndSaveTestCustomer(1, customerRepository, null);
         customer2 = createAndSaveTestCustomer(2, customerRepository, null);
         customer3 = createAndSaveTestCustomer(3, customerRepository, "UnitTest_Customer_3");
 
         customerMongoFactory.clearCustomerDatabase();
     }
 
     private Customer createAndSaveTestCustomer(int customerId, CustomerRepository2 customerRepository, String customerDatabaseName) {
         Customer customer = new Customer();
         customer.setId(String.format("MyCustomerId%d", customerId));
         customer.setName(String.format("MyCustomerName%d", customerId));
 
         if (customerDatabaseName != null) {
             String mongoUri = createDefaultLocalConnection(customerDatabaseName);
 
             customer.setMongoUri(mongoUri);
         }
 
         customerRepository.save(customer);
 
         return customer;
     }
 
     private static final String DEFAULT_LOCAL_MONGOURI = MongoURI.MONGODB_PREFIX +  "localhost:27017";
 
    private static String createDefaultLocalConnection(String databaseName) {
         Assert.hasText(databaseName);
 
         return String.format("%s/%s", DEFAULT_LOCAL_MONGOURI, databaseName);
     }
 }
