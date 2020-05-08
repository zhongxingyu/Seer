 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.icrossfit.test.create;
 
 import com.icrossfit.entry.User;
 import com.icrossfit.test.util.TestHelper;
 import com.icrossfit.util.MongoHelper;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 

 /**
  *
  * @author Patrick
  */
 public class Test11 {
     
     public Test11() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
         TestHelper.signon(this);
         
         //Connect to DB - Lazy
         MongoHelper.setDB("icrossfit");
     }
     
     @After
     public void tearDown() {
         TestHelper.signoff(this);
     }
     
     //Create a basic User object and save it to the users collection in iCrossFit DB
     @Test
     public void test() {
         
         //Create New User
         User user1 = new User();
         User user2 = new User();
         
         user1.setFirstName("Bob");
         user1.setLastName("Jones");
         
         user2.setFirstName("Janet");
         user2.setLastName("Miller");
         
         if(!MongoHelper.save(user1,"users")) {
             TestHelper.failed("Save failed.");
         }
         
         if(!MongoHelper.save(user2,"users")) {
             TestHelper.failed("Save failed.");
         }
         
         //Print object and infomation
         System.out.println(user1.toString());
         System.out.println("\nSuccessfully saved USER id = " + user1.getId() + ", Name = " + user1.getFirstName() + " " + user1.getLastName());
         System.out.println("\nSuccessfully saved USER id = " + user2.getId() + ", Name = " + user2.getFirstName() + " " + user2.getLastName());
         
         //Pass test
         TestHelper.passed();
     }
 }
