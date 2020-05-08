 package com.npickard;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import com.npickard.helloworld.Message;
 import com.npickard.hibernate.common.Customer;
 import com.npickard.hibernate.persistence.HibernateUtil;
 
 public class App{
 	// Define a static logger variable so that it references the
 	// Logger instance named "HelloWorld".
 	static Logger logger = Logger.getLogger(App.class);
 
 	static{
 	// Set up a simple configuration that logs on the console.
 		BasicConfigurator.configure();
 	}
 	
     public static void main( String[] args ){
     	App app = new App();
     	//app.runCustomerExample();
     	app.runHelloWorldExample();
     	
     }
     
     
     private void runCustomerExample(){
     	logger.info("Hibernate 4 Customer example");
         Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Customer customer = new Customer();
         customer.setFirstName("John");
         customer.setLastName("Doe");
         session.save(customer);
         session.getTransaction().commit();	
     }
     
     private void runHelloWorldExample(){
     	logger.info("Hibernate 4 Hello World example");
         Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Message message1 = new Message("Hello, message 1!");
         Message message2 = new Message("Hello, message 2!");
         Message message3 = new Message("Hello, message 3!");
         message1.setNextMessage(message2);
         message2.setNextMessage(message3);
         
         session.save(message1);
         session.save(message2);
         session.save(message3);
         session.getTransaction().commit();	
         
         //now get the very first message
        Message message = (Message)session.load(Message.class, new Long(5));
         logger.info("Retrieved message is " + message.toString());
         
     }
 
 }
