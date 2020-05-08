<<<<<<< HEAD
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */

=======
>>>>>>> c245efeed373033fa56c6eb5430525079e9d5403
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
<<<<<<< HEAD
}

=======
 }
>>>>>>> c245efeed373033fa56c6eb5430525079e9d5403
