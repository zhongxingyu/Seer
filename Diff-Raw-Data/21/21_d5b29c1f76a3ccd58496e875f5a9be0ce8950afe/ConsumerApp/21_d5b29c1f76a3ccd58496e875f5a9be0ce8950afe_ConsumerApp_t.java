 package de.theess.amq.consumer;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
  * 
  */
 public class ConsumerApp {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
 		new ClassPathXmlApplicationContext(new String[] { "spring/consumer.xml" });
 	}
 }
