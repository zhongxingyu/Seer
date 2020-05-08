 package no.dusken.annonseweb.service;
 
 import no.dusken.annonseweb.models.Ad;
 import no.dusken.annonseweb.models.Customer;
 import no.dusken.annonseweb.models.PrintedAd;
 import no.dusken.annonseweb.models.Sale;
 import no.dusken.common.model.Person;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.LinkedList;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * @author Marvin B. Lillehaug <lillehau@underdusken.no>
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"/integrationtest-jpa.xml"})
 @TransactionConfiguration
 @Transactional
 public class ServiceIntegrationTest {
 
     @Autowired
     private SalesService salesService;
 
     @Test
     public void testSave(){
 
         Ad ad = new PrintedAd(new BigDecimal("100"), new BigDecimal("10"));
        Customer customer = new Customer("customer", "mail@mail.mail", "12345678", "address", "contactPerson");
         Person createdUser = getPerson();
 
        Sale s = new Sale("Appointment name", new LinkedList<Ad>(Collections.singleton(ad)),
                 customer, createdUser, true);
 
         Sale sale = salesService.saveAndFlush(s);
         assertNotNull(sale);
         assertNotNull(sale.getId());
         assertEquals(ad, sale.getAds().get(0));
         assertEquals(customer, sale.getCustomer());
 
     }
 
     private Person getPerson() {
         Person createdUser = new Person();
         createdUser.setUsername("user");
         createdUser.setFirstname("first");
         createdUser.setSurname("surname");
         createdUser.setEmailAddress("email@email.com");
         return createdUser;
     }
 
 }
