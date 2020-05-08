 package org.jz.services;
 
 import org.jz.domain.CustomerId;
 import org.jz.domain.PersonCustomer;
 import org.jz.domain.PersonName;
 
 /**
  * @author Kristian Rosenvold
  */
 public class CustomerService {
     public PersonCustomer findCustomer( PersonName personName )
     {
         return new PersonCustomer( personName, new CustomerId(personName.hashCode()) );
     }
 }
