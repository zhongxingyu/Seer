 package de.theess.amq.broker;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
  * 
  */
 public class BrokerApp {
	@SuppressWarnings("unused")
 	public static void main(String[] args) throws InterruptedException {
 		new ClassPathXmlApplicationContext(new String[] { "spring/broker.xml" });
 		while(true) {
 			Thread.sleep(5000);
 		}
 	}
 }
