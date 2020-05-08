 package org.test.bugtracker.starter;
 
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
public class ApplicationStarter {
    private ApplicationContext context;
     public static void main(String[] args) {
        new ApplicationStarter().start();
    }
    
    public void start() {
        context = new ClassPathXmlApplicationContext("spring/context.xml");
        context.toString();
     }
 
 }
