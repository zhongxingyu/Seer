 package org.jz.domain;
 
 import junit.framework.TestCase;
 
 /**
  * @author Kristian Rosenvold
  */
 public class PersonCustomerTest
     extends TestCase
 {
     private static final PersonName personName = new PersonName( "Kristian", "Rosenvold" );
 
     private final CustomerId customerId = new CustomerId( 123 );
 
     public void testGetDisplayName()
         throws Exception
     {
         PersonCustomer personCustomer = new PersonCustomer( personName, customerId );
         assertEquals( "Kristian Rosenvold", personCustomer.getDisplayName() );
     }
 
     public void testGetFullName()
         throws Exception
     {
         PersonCustomer personCustomer = new PersonCustomer( personName, customerId );
         assertEquals( "Kristian Rosenvold", personCustomer.getFullName() );
     }
 }
