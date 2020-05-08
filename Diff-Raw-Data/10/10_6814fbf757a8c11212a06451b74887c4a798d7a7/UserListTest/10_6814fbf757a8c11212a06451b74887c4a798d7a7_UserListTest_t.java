 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.server;
 
 import java.util.Scanner;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author w
  */
 public class UserListTest {
 
     public UserListTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of loadStockPoolFromDisk method, of class UserList.
      */
     @Test
     public void testLoadStockPoolFromDisk() {
         System.out.println("loadStockPoolFromDisk");
 //        boolean expResult = false;
 //        boolean result = UserList.loadUserData();
 //        assertEquals(expResult, result);
 //        // TODO review the generated test code and remove the default call to fail.
 //        fail("The test case is a prototype.");
         UserList.loadUserData();
        System.out.println(UserList.fetchByUserName("kate").displayStocksHold());
     }
 }
