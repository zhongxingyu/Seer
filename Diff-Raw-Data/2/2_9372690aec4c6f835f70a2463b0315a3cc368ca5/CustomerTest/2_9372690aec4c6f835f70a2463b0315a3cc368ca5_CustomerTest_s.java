 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package carrental;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *                                      NIELS :) !! Kan du lige fixe sådan at postalcode tingen virker :) Ved ikke om man opretter customer med \n eller hvad?
  * @author CNN
  * @version 2011-12-08
  */
 public class CustomerTest {
 
     private Customer customer; //Should these be static? maybe it doesn't matter??
 
     public CustomerTest() {
         customer = new Customer(1, 24736456, "Poul Poulsen", "Enghavevej 13, 7420 Viborg", "p.poulsen@gmail.com");
     }
 
     /**
      * Test of getID method, of class Customer.
      */
     @Test
     public void testGetID() {
         assertEquals(1, customer.getID());
         //also testing updating of this, to ensure it's not just 1 always. The other getters are tested this way through testUpdateObject()
         customer = new Customer(4, 24736456, "Poul Poulsen", "Enghavevej 13"+"\n"+"7420 Viborg", "p.poulsen@gmail.com");
         assertEquals(4, customer.getID());
     }
 
     /**
      * Test of getTelephone method, of class Customer.
      */
     @Test
     public void testGetTelephone() {
         assertEquals(24736456, customer.getTelephone());
     }
 
     /**
      * Test of getName method, of class Customer.
      */
     @Test
     public void testGetName() {
         assertEquals("Poul Poulsen", customer.getName());
     }
 
     /**
      * Test of getAdress method, of class Customer.
      */
     @Test
     public void testGetAdress() {
         assertEquals("Enghavevej 13, 7420 Viborg", customer.getAdress());
     }
     
     /**
      * Test of getPostalCode method, of class Customer.
      */
     @Test
     public void testGetPostalCode() {
         assertEquals(7420, customer.getPostalCode());
     }
 
     /**
      * Test of getEMail method, of class Customer.
      */
     @Test
     public void testGetEMail() {
         assertEquals("p.poulsen@gmail.com", customer.getEMail());
     }
     
     /**
      * Test of updateObject method, of class Customer.
      */
     @Test
     public void testUpdateObject() {
         customer.updateObject(24736400, "Poul Erik Poulsen", "Enghavevej 15 \n 7500 Viborg", "p.e.poulsen@gmail.com");
         //test that the customer's fields equals the new values.
         assertEquals(24736400, customer.getTelephone());
         assertEquals("Poul Erik Poulsen", customer.getName());
        assertEquals("Enghavevej 15 7420 Viborg", customer.getAdress());
         assertEquals(7500, customer.getPostalCode());
         assertEquals("p.e.poulsen@gmail.com", customer.getEMail());
     }
 }
