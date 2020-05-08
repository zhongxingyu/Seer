 package org.opencredo.batch.modules.hibernate;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.math.BigDecimal;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.opencredo.batch.modules.domain.Order;
 import org.opencredo.batch.modules.domain.OrderStatus;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/hibernate-config.xml", "classpath:/batch-infrastructure.xml"})
 @Transactional
 @TransactionConfiguration(transactionManager = "dataTxManager")
 public class HibernateConfigTest {
 
 
     @PersistenceContext
     EntityManager entityManager;
 
     @Test
     public void nothingShouldBlowUpOnPersistAndFlush(){
         assertNotNull(entityManager);
         Order order = new Order(OrderStatus.REQUESTED, new BigDecimal(12.0));
         entityManager.persist(order);
         entityManager.flush();
     }
 
 
 
 }
