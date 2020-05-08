 package de.theess.amq.consumer;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
  * 
  */
 public class ConsumerApp {
	public static void main(String[] args) throws InterruptedException {
 		new ClassPathXmlApplicationContext(new String[] { "spring/consumer.xml" });
 	}
 }
