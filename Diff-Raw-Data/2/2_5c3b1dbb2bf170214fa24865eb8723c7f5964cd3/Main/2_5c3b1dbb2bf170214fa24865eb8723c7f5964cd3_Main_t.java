 package edu.integration.patterns;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import edu.integration.patterns.reseller.Reseller;
 
 /**
  * Entry point
  * 
  * @author Diyan Yordanov
  * 
  */
 public final class Main {
 
     private static final String USERNAME = "dido";
     private static final String PASSWORD = "pass";
     private static final long ITEM_ID = 23542345;
 
     public static void main(String[] args) {
         // Initialize Spring application context
         ApplicationContext ac = new ClassPathXmlApplicationContext(
                 "classpath:META-INF/spring/integration/spring-integration-context.xml");
 
         // Get the reseller bean
         Reseller testService = ac.getBean(Reseller.class);
 
        // Send order to the reseller
         OrderBean order = new OrderBean(USERNAME, PASSWORD, ITEM_ID);
         testService.buy(order);
 
     }
 
 }
