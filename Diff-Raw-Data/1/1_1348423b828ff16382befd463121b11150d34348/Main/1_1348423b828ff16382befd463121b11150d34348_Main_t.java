 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.d3velopers.sms_search.main;
 
 import com.d3velopers.sms_search.business.service.QueryHandler;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  *
  * @author boy
  */
 public class Main {
 
     private static final Logger LOG = LoggerFactory.getLogger(Main.class.getName());
 
     public static void main(String[] args) {
         ApplicationContext ctx = new ClassPathXmlApplicationContext(
                 "applicationContext-main.xml",
                 "classpath*:applicationContext-dao.xml",
                 "classpath*:applicationContext-business.xml");
 
         QueryHandler queryHandler = ctx.getBean(QueryHandler.class);
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
         while (true) {
             try {
                 System.out.print("Enter phone number: ");
                 String phoneNumber = br.readLine();
                 if (phoneNumber.equals("-1")) {
                     break;
                 }
 
                 System.out.print("Enter query: ");
                 String query = br.readLine();
                 if (query.equals("-1")) {
                     break;
                 }
 
                 String result = queryHandler.executeQuery(phoneNumber, query);
                 System.out.println("---------------------");
                 System.out.println(result);
                 System.out.println("---------------------");
                 System.out.println();
             } catch (Exception ex) {
                 LOG.error(ex.getMessage(), ex);
             }
         }
         
         System.out.println("--Exit--");
     }
 }
