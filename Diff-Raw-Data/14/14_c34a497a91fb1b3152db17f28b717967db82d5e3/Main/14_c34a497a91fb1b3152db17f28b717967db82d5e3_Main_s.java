 package com.lavida.service;
 
 import com.lavida.service.entity.AuthorityJdo;
 import com.lavida.service.entity.UserJdo;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.jpa.JpaTransactionManager;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Admin
  * Date: 01.08.13
  * Time: 19:29
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     public static void main(String[] args) {
         UserJdo user3 = new UserJdo("login3", "pass3", true, "name3");
         user3.getAuthorities().add(new AuthorityJdo("ROLE_USER", user3));
         String filePath = "spring-context.xml";
         AbstractApplicationContext context = new ClassPathXmlApplicationContext(filePath);
         JpaTransactionManager transactionManager = context.getBean("transactionManager", JpaTransactionManager.class);
         UserService service = context.getBean("userService", UserService.class);
         service.save(user3);
 //        service.update(user2);
         System.out.println(service.getAll());
         System.out.println("Privet!");
     }
 }
