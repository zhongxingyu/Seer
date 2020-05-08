 package org.test.bugtracker.starter;
 
import org.hibernate.SessionFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.test.bugtracker.bo.UserBO;
 
 public class ApplicationStarter {
     private ApplicationContext context;
 
     public ApplicationStarter(ApplicationContext context) {
         this.context = context;
     }
 
     public void start() {
        context.getBeanDefinitionNames();
        context.getBean(SessionFactory.class).openSession();
        context.getBean(UserBO.class).findById(0L);
     }
 
     public static void main(String[] args) {
         new ApplicationStarter(new ClassPathXmlApplicationContext(
                 "spring/context.xml")).start();
     }
 }
