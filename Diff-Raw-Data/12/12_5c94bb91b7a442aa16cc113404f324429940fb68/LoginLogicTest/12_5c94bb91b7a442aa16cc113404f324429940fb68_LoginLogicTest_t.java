 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package login;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Stian
  */
 public class LoginLogicTest {
 
     public LoginLogicTest() {
     }
 
     /**
      * Test of checkLogin method, of class LoginLogic.
      */
     @Test
     public void testCheckLogin() {
         System.out.println("checkLogin");
         String password = "";
         boolean expResult = false;
         boolean result = LoginLogic.checkLogin(password);
         assertEquals(expResult, result);
     }
 }
