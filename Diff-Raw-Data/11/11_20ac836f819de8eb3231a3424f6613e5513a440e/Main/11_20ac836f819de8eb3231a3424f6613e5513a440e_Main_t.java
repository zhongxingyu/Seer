 package com.tvelykyy.indexator;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Main {
 
     public static void main(String[] args) {
         ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
         Bot bot = context.getBean(Bot.class);
 
         bot.getNotIndexedPagesFromGgl();
     }
 
 }
