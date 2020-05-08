 package de.theess.amq.producer;
 
import javax.jms.JMSException;

 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
 * 
  */
 public class ProducerApp {
	public static void main(String[] args) throws InterruptedException, JMSException {
 		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring/producer.xml" });
 		Producer producer = (Producer) context.getBean("producer");
 		producer.start();
 		context.close();
 	}
 }
