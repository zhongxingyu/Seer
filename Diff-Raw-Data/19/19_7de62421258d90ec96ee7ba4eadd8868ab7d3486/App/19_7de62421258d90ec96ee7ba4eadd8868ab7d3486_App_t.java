 package no.helsenorge.nipen;
 
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
 import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Hello world!
  *
  */
@ComponentScan
@EnableAutoConfiguration
 public class App 
 {
     public static void main( String[] args )
     {
        //ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
        org.springframework.boot.SpringApplication.run(App.class, args);
     }
 }
